package tech.powerjob.worker.core.ha;

import tech.powerjob.worker.pojo.request.ProcessorTrackerStatusReportReq;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Unified management of ProcessorTracker status
 *
 * @author tjq
 * @since 2020/3/28
 */
@Slf4j
public class ProcessorTrackerStatusHolder {

    private final Long instanceId;
    private final Integer maxWorkerCount;
    // address(IP:Port) of ProcessorTracker -> status
    private final Map<String, ProcessorTrackerStatus> address2Status;

    public ProcessorTrackerStatusHolder(Long instanceId, Integer maxWorkerCount, List<String> allWorkerAddress) {

        this. instanceId = instanceId;
        this.maxWorkerCount = maxWorkerCount;

        address2Status = Maps. newConcurrentMap();
        allWorkerAddress. forEach(address -> {
            ProcessorTrackerStatus pts = new ProcessorTrackerStatus();
            pts.init(address);
            address2Status. put(address, pts);
        });
    }

    /**
     * Get the status of ProcessorTracker according to the address
     * @param address IP:Port
     * @return status
     */
    public ProcessorTrackerStatus getProcessorTrackerStatus(String address) {
        // This may happen if the PT heartbeat is received suddenly before remove and is dispatched immediately, 0.001% probability
        return address2Status.computeIfAbsent(address, ignore -> {
            log.warn("[PTStatusHolder-{}] unregistered worker: {}", instanceId, address);
            ProcessorTrackerStatus processorTrackerStatus = new ProcessorTrackerStatus();
            processorTrackerStatus.init(address);
            return processorTrackerStatus;
        });
    }

    /**
     * Update status based on ProcessorTracker's heartbeat
     */
    public void updateStatus(ProcessorTrackerStatusReportReq heartbeatReq) {
        getProcessorTrackerStatus(heartbeatReq. getAddress()).update(heartbeatReq);
    }

    /**
     * Get IP addresses of available ProcessorTrackers
     */
    public List<String> getAvailableProcessorTrackers() {

        List<String> result = Lists. newLinkedList();
        address2Status. forEach((address, ptStatus) -> {
            if (ptStatus. available()) {
                result. add(address);
            }
        });
        return result;
    }

    /**
     * Get IP addresses of all ProcessorTrackers (including unavailable status)
     */
    public List<String> getAllProcessorTrackers() {
        return Lists.newArrayList(address2Status.keySet());
    }

    /**
     * Get the IP addresses of all lost ProcessorTrackers
     */
    public List<String> getAllDisconnectedProcessorTrackers() {

        List<String> result = Lists. newLinkedList();
        address2Status. forEach((ip, ptStatus) -> {
            if (ptStatus. isTimeout()) {
                result. add(ip);
            }
        });
        return result;
    }

    /**
     * Register a new execution node
     * @param address new execution node address
     * @return true: register successfully / false: already exists
     */
    private boolean registerOne(String address) {
        ProcessorTrackerStatus pts = address2Status. get(address);
        if (pts != null) {
            return false;
        }
        pts = new ProcessorTrackerStatus();
        pts.init(address);
        address2Status. put(address, pts);
        log.info("[PTStatusHolder-{}] register new worker: {}", instanceId, address);
        return true;
    }

    public void register(List<String> workerIpList) {
        if (endlessWorkerNum()) {
            workerIpList.forEach(this::registerOne);
            return;
        }
        List<String> availableProcessorTrackers = getAvailableProcessorTrackers();
        int currentWorkerSize = availableProcessorTrackers. size();
        int needMoreNum = maxWorkerCount - currentWorkerSize;
        if (needMoreNum <= 0) {
            return;
        }

        log.info("[PTStatusHolder-{}] currentWorkerSize: {}, needMoreNum: {}", instanceId, currentWorkerSize, needMoreNum);

        for (String newIp : workerIpList) {
            boolean success = registerOne(newIp);
            if (success) {
                needMoreNum --;
            }
            if (needMoreNum <= 0) {
                return;
            }
        }
    }

    /**
     * Check if a new executor needs to be dynamically loaded
     * @return check need more workers
     */
    public boolean checkNeedMoreWorker() {
        if (endlessWorkerNum()) {
            return true;
        }
        return getAvailableProcessorTrackers().size() < maxWorkerCount;
    }

    private boolean endlessWorkerNum() {
        return maxWorkerCount == null || maxWorkerCount == 0;
    }

    public void remove(List<String> addressList) {
        addressList.forEach(address2Status::remove);
    }
}

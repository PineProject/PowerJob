package tech.powerjob.worker.core.ha;

import tech.powerjob.worker.pojo.request.ProcessorTrackerStatusReportReq;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ProcessorTracker status
 *
 * @author tjq
 * @since 2020/3/27
 */
@Data
@NoArgsConstructor
public class ProcessorTrackerStatus {

    private static final int DISPATCH_THRESHOLD = 20;
    private static final int HEARTBEAT_TIMEOUT_MS = 60000;

    // Redundantly store an address address
    private String address;
    // last active time
    private long lastActiveTime;
    // number of tasks waiting to be executed
    private long remainTaskNum;
    // Whether the task has been dispatched
    private boolean dispatched;
    // Whether a heartbeat from ProcessorTracker has been received
    private boolean connected;

    /**
     * Initialize ProcessorTracker, at this time does not hold the actual ProcessorTracker state
     */
    public void init(String address) {
        this.address = address;
        this.lastActiveTime = - 1;
        this.remainTaskNum = 0;
        this. dispatched = false;
        this. connected = false;
    }

    /**
     * After receiving the heartbeat information of ProcessorTracker, update the status
     * @param req ProcessorTracker's heartbeat information
     */
    public void update(ProcessorTrackerStatusReportReq req) {

        // Requests that arrive late, ignore them directly
        if (req. getTime() <= lastActiveTime) {
            return;
        }

        this.address = req.getAddress();
        this.lastActiveTime = req.getTime();
        this.remainTaskNum = req.getRemainTaskNum();
        this. dispatched = true;
        this.connected = true;
    }

    /**
     * it's usable or not
     */
    public boolean available() {

        // Never dispatched, available by default
        if (!dispatched) {
            return true;
        }

        // dispatched but no response received, unavailable
        if (!connected) {
            return false;
        }

        // If the heartbeat message has not been received for a long time, it is unavailable
        if (isTimeout()) {
            return false;
        }

        // If there are too many pending tasks, it is unavailable
        if (remainTaskNum >= DISPATCH_THRESHOLD) {
            return false;
        }

        // TODO: Add information such as machine health and other information for subsequent consideration

        return true;
    }

    /**
     * Whether it timed out (the heartbeat was not received for more than a certain period of time)
     */
    public boolean isTimeout() {
        if (dispatched) {
            return System.currentTimeMillis() - lastActiveTime > HEARTBEAT_TIMEOUT_MS;
        }
        // Machines that have never dispatched tasks do not need to be processed
        return false;
    }

}

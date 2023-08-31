package tech.powerjob.worker.core.processor.runnable;

import com.google.common.base.Stopwatch;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tech.powerjob.common.enums.ExecuteType;
import tech.powerjob.common.serialize.SerializerUtils;
import tech.powerjob.worker.common.ThreadLocalStore;
import tech.powerjob.worker.common.WorkerRuntime;
import tech.powerjob.worker.common.constants.TaskConstant;
import tech.powerjob.worker.common.constants.TaskStatus;
import tech.powerjob.worker.common.utils.TransportUtils;
import tech.powerjob.worker.common.utils.WorkflowContextUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.WorkflowContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.core.processor.sdk.BroadcastProcessor;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.extension.processor.ProcessorBean;
import tech.powerjob.worker.log.OmsLogger;
import tech.powerjob.worker.persistence.TaskDO;
import tech.powerjob.worker.pojo.model.InstanceInfo;
import tech.powerjob.worker.pojo.request.ProcessorReportTaskStatusReq;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Processor executor
 *
 * @author tjq
 * @author Echo009
 * @since 2020/3/23
 */
@Slf4j
@AllArgsConstructor
@SuppressWarnings("squid:S1181")
public class HeavyProcessorRunnable implements Runnable {


    private final InstanceInfo instanceInfo;
    private final String taskTrackerAddress;
    private final TaskDO task;
    private final ProcessorBean processorBean;
    private final OmsLogger omsLogger;
    /**
     * Retry queue, ProcessorTracker will periodically re-report processing results
     */
    private final Queue<ProcessorReportTaskStatusReq> statusReportRetryQueue;
    private final WorkerRuntime workerRuntime;

    public void innerRun() throws InterruptedException {

        final BasicProcessor processor = processorBean.getProcessor();

        String taskId = task.getTaskId();
        Long instanceId = task.getInstanceId();

        log.debug("[ProcessorRunnable-{}] start to run task(taskId={}&taskName={})", instanceId, taskId, task.getTaskName());
        ThreadLocalStore.setTask(task);
        ThreadLocalStore.setRuntimeMeta(workerRuntime);

        // 0. Construct task context
        WorkflowContext workflowContext = constructWorkflowContext();
        TaskContext taskContext = constructTaskContext();
        taskContext.setWorkflowContext(workflowContext);
        // 1. Report execution information
        reportStatus(TaskStatus. WORKER_PROCESSING, null, null, null);

        ProcessResult processResult;
        ExecuteType executeType = ExecuteType. valueOf(instanceInfo. getExecuteType());

        // 2. Root task & broadcast execution special processing
        if (TaskConstant.ROOT_TASK_NAME.equals(task.getTaskName()) && executeType == ExecuteType.BROADCAST) {
            // Broadcast execution: first select the local machine to execute preProcess, and after completion, TaskTracker generates sub-Tasks for all Workers
            handleBroadcastRootTask(instanceId, taskContext);
            return;
        }

        // 3. Special processing for the final task (must be on the same machine as TaskTracker)
        if (TaskConstant.LAST_TASK_NAME.equals(task.getTaskName())) {
            handleLastTask(taskId, instanceId, taskContext, executeType);
            return;
        }

        // 4. Officially submit and run
        try {
            processResult = processor.process(taskContext);
            if (processResult == null) {
                processResult = new ProcessResult(false, "ProcessResult can't be null");
            }
        } catch (Throwable e) {
            log.warn("[ProcessorRunnable-{}] task(id={},name={}) process failed.", instanceId, taskContext.getTaskId(), taskContext.getTaskName(), e);
            processResult = new ProcessResult(false, e.toString());
        }
        reportStatus(processResult.isSuccess() ? TaskStatus.WORKER_PROCESS_SUCCESS : TaskStatus.WORKER_PROCESS_FAILED, suit(processResult.getMsg()), null, workflowContext.getAppendedContextData());
    }


    private TaskContext constructTaskContext() {
        TaskContext taskContext = new TaskContext();
        taskContext.setJobId(instanceInfo.getJobId());
        taskContext.setInstanceId(task.getInstanceId());
        taskContext.setSubInstanceId(task.getSubInstanceId());
        taskContext.setTaskId(task.getTaskId());
        taskContext.setTaskName(task.getTaskName());
        taskContext.setMaxRetryTimes(instanceInfo.getTaskRetryNum());
        taskContext.setCurrentRetryTimes(task.getFailedCnt());
        taskContext.setJobParams(instanceInfo.getJobParams());
        taskContext.setInstanceParams(instanceInfo.getInstanceParams());
        taskContext.setOmsLogger(omsLogger);
        if (task.getTaskContent() != null && task.getTaskContent().length > 0) {
            taskContext.setSubTask(SerializerUtils.deSerialized(task.getTaskContent()));
        }
        taskContext.setUserContext(workerRuntime.getWorkerConfig().getUserContext());
        return taskContext;
    }

    private WorkflowContext constructWorkflowContext() {
        return new WorkflowContext(instanceInfo.getWfInstanceId(), instanceInfo.getInstanceParams());
    }

    /**
     * Handle the final task
     * BROADCAST  => {@link BroadcastProcessor#postProcess}
     * MAP_REDUCE => {@link MapReduceProcessor#reduce}
     */
    private void handleLastTask(String taskId, Long instanceId, TaskContext taskContext, ExecuteType executeType) {
        final BasicProcessor processor = processorBean.getProcessor();
        ProcessResult processResult;
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.debug("[ProcessorRunnable-{}] the last task(taskId={}) start to process.", instanceId, taskId);

        List<TaskResult> taskResults = workerRuntime.getTaskPersistenceService().getAllTaskResult(instanceId, task.getSubInstanceId());
        try {
            switch (executeType) {
                case BROADCAST:

                    if (processor instanceof BroadcastProcessor) {
                        BroadcastProcessor broadcastProcessor = (BroadcastProcessor) processor;
                        processResult = broadcastProcessor.postProcess(taskContext, taskResults);
                    } else {
                        processResult = BroadcastProcessor.defaultResult(taskResults);
                    }
                    break;
                case MAP_REDUCE:

                    if (processor instanceof MapReduceProcessor) {
                        MapReduceProcessor mapReduceProcessor = (MapReduceProcessor) processor;
                        processResult = mapReduceProcessor.reduce(taskContext, taskResults);
                    } else {
                        processResult = new ProcessResult(false, "not implement the MapReduceProcessor");
                    }
                    break;
                default:
                    processResult = new ProcessResult(false, "IMPOSSIBLE OR BUG");
            }
        } catch (Throwable e) {
            processResult = new ProcessResult(false, e.toString());
            log.warn("[ProcessorRunnable-{}] execute last task(taskId={}) failed.", instanceId, taskId, e);
        }

        TaskStatus status = processResult.isSuccess() ? TaskStatus.WORKER_PROCESS_SUCCESS : TaskStatus.WORKER_PROCESS_FAILED;
        reportStatus(status, suit(processResult.getMsg()), null, taskContext.getWorkflowContext().getAppendedContextData());

        log.info("[ProcessorRunnable-{}] the last task execute successfully, using time: {}", instanceId, stopwatch);
    }

    /**
     * Handle the root task for broadcast execution
     * Execute {@link BroadcastProcessor#preProcess} and notify TaskerTracker to create a broadcast subtask
     */
    private void handleBroadcastRootTask(Long instanceId, TaskContext taskContext) {
        BasicProcessor processor = processorBean.getProcessor();
        ProcessResult processResult;
        // The first task executed by the broadcast only executes the preProcess part
        if (processor instanceof BroadcastProcessor) {

            BroadcastProcessor broadcastProcessor = (BroadcastProcessor) processor;
            try {
                processResult = broadcastProcessor.preProcess(taskContext);
            } catch (Throwable e) {
                log.warn("[ProcessorRunnable-{}] broadcast task preProcess failed.", instanceId, e);
                processResult = new ProcessResult(false, e.toString());
            }

        } else {
            processResult = new ProcessResult(true, "NO_PREPOST_TASK");
        }
        // Notify TaskTracker to create a broadcast subtask
        reportStatus(processResult.isSuccess() ? TaskStatus.WORKER_PROCESS_SUCCESS : TaskStatus.WORKER_PROCESS_FAILED, suit(processResult.getMsg()), ProcessorReportTaskStatusReq.BROADCAST, taskContext.getWorkflowContext().getAppendedContextData());

    }

    /**
     * Report status to TaskTracker
     *
     * @param status Task status
     * @param result Execution result, only exists at the end
     * @param cmd Special requirements, such as broadcast execution needs to create a broadcast task
     */
    private void reportStatus(TaskStatus status, String result, Integer cmd, Map<String, String> appendedWfContext) {
        ProcessorReportTaskStatusReq req = new ProcessorReportTaskStatusReq();

        req.setInstanceId(task.getInstanceId());
        req.setSubInstanceId(task.getSubInstanceId());
        req.setTaskId(task.getTaskId());
        req.setStatus(status.getValue());
        req.setResult(result);
        req.setReportTime(System.currentTimeMillis());
        req.setCmd(cmd);
        // Checks if appended context size exceeds limit
        if (instanceInfo.getWfInstanceId() !=null && WorkflowContextUtils.isExceededLengthLimit(appendedWfContext, workerRuntime.getWorkerConfig().getMaxAppendedWfContextLength())) {
            log.warn("[ProcessorRunnable-{}]current length of appended workflow context data is greater than {}, this appended workflow context data will be ignore!",instanceInfo.getInstanceId(), workerRuntime.getWorkerConfig().getMaxAppendedWfContextLength());
            // ignore appended workflow context data
            appendedWfContext = Collections.emptyMap();
        }
        req.setAppendedWfContext(appendedWfContext);

        // The final end state requires reliable delivery
        if (TaskStatus. FINISHED_STATUS. contains(status. getValue())) {
            boolean success = TransportUtils. reliablePtReportTask(req, taskTrackerAddress, workerRuntime);
            if (!success) {
                // Insert into the retry queue, waiting for retry
                statusReportRetryQueue.add(req);
                log.warn("[ProcessorRunnable-{}] report task(id={},status={},result={}) failed, will retry later", task.getInstanceId(), task.getTaskId(), status, result);
            }
        } else {
            TransportUtils.ptReportTask(req, taskTrackerAddress, workerRuntime);
        }
    }

    @Override
    @SuppressWarnings("squid:S2142")
    public void run() {
        // Switch the thread context class loader (otherwise the Worker class loader is used, there is no container class,
        // and ClassNotFoundException will be reported during serialization/deserialization)
        Thread.currentThread().setContextClassLoader(processorBean.getClassLoader());
        try {
            innerRun();
        } catch (InterruptedException ignore) {
            // ignore
        } catch (Throwable e) {
            reportStatus(TaskStatus.WORKER_PROCESS_FAILED, e.toString(), null, null);
            log.error("[ProcessorRunnable-{}] execute failed, please contact the author(@KFCFans) to fix the bug!", task.getInstanceId(), e);
        } finally {
            ThreadLocalStore.clear();
        }
    }

    /**
     * Crop the returned result to a suitable size
     */
    private String suit(String result) {

        if (StringUtils.isEmpty(result)) {
            return "";
        }
        final int maxLength = workerRuntime.getWorkerConfig().getMaxResultLength();
        if (result.length() <= maxLength) {
            return result;
        }
        log.warn("[ProcessorRunnable-{}] task(taskId={})'s result is too large({}>{}), a part will be discarded.",
                task.getInstanceId(), task.getTaskId(), result.length(), maxLength);
        return result.substring(0, maxLength).concat("...");
    }

}

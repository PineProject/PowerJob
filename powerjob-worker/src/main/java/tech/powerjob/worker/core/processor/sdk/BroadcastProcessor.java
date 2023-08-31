package tech.powerjob.worker.core.processor.sdk;

import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;

import java.util.List;

/**
 * Broadcast execution processor, suitable for broadcast execution
 *
 * @author tjq
 * @since 2020/3/18
 */
public interface BroadcastProcessor extends BasicProcessor {

    /**
     * Execute before all nodes broadcast and execute, and only execute once on one machine
     */
    default ProcessResult preProcess(TaskContext context) throws Exception {
        return new ProcessResult(true);
    }
    /**
     * Executed after all node broadcast executions are completed, and will only be executed once on one machine
     */
    default ProcessResult postProcess(TaskContext context, List<TaskResult> taskResults) throws Exception {
        return defaultResult(taskResults);
    }

    static ProcessResult defaultResult(List<TaskResult> taskResults) {
        long succeed = 0, failed = 0;
        for (TaskResult ts : taskResults) {
            if (ts.isSuccess()) {
                succeed ++ ;
            }else {
                failed ++;
            }
        }
        return new ProcessResult(failed == 0, String.format("succeed:%d, failed:%d", succeed, failed));
    }
}

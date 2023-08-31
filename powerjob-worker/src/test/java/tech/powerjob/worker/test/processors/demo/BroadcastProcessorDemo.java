package tech.powerjob.worker.test.processors.demo;

import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.BroadcastProcessor;

import java.util.List;

/**
 * 示例-广播执行处理器
 *
 * @author tjq
 * @since 2020/4/15
 */
public class BroadcastProcessorDemo implements BroadcastProcessor {

    @Override
    public ProcessResult preProcess(TaskContext taskContext) throws Exception {
        // Pre-execution, will be called before all workers execute the process method
        return new ProcessResult(true, "init success");
    }

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        // Write code logic that will be executed by the entire worker cluster
        return new ProcessResult(true, "release resource success");
    }

    @Override
    public ProcessResult postProcess(TaskContext taskContext, List<TaskResult> taskResults) throws Exception {

        // taskResults stores the results of all worker executions (including preProcess)

        // At the end, it will be called after all workers have finished executing the process method, and the result will be used as the final execution result in
        return new ProcessResult(true, "process success");
    }

}

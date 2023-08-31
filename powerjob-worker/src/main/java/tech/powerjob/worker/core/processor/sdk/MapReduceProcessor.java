package tech.powerjob.worker.core.processor.sdk;

import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;

import java.util.List;

/**
 * MapReduce execution processor, suitable for MapReduce tasks
 * Added the method of result collection (reduce) on the basis of MapProcessor
 *
 * @author tjq
 * @since 2020/3/18
 */
public interface MapReduceProcessor extends MapProcessor {

    /**
     * The reduce method will be called after all tasks are finished
     * @param context task context
     * @param taskResults saves the execution results of each sub-task
     * @return The result of reduce will be the final return result of the task
     */
    ProcessResult reduce(TaskContext context, List<TaskResult> taskResults);
}

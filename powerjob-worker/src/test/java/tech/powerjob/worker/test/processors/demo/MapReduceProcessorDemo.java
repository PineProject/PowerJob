package tech.powerjob.worker.test.processors.demo;

import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Example - MapReduce Task Processor
 *
 * @author tjq
 * @since 2020/4/15
 */
public class MapReduceProcessorDemo implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        // Determine whether it is a root task
        if (isRootTask()) {

            // constructor subtask
            List<SubTask> subTaskList = Lists.newLinkedList();

            /*
             * The structure of subtasks is defined by the developer
             * e.g. Now it is necessary to read 100W IDs from the file and process the data corresponding to these IDs in the database, then the steps are as follows:
             * 1. The root task (RootTask) reads the file, stream pulls 1 million IDs, and assembles them into subtasks in batches of 1000 for distribution
             * 2. Non-root tasks obtain subtasks to complete business logic processing
             */

            // Call the map method to dispatch subtasks
            map(subTaskList, "DATA_PROCESS_TASK");
            return new ProcessResult(true, "map successfully");
        }

        // Non-subtask, branch can be judged according to the type of subTask or TaskName
        if (context.getSubTask() instanceof SubTask) {
            // Execute subtasks, note: Subtask operators can map to generate new subtasks, and can build MapReduce processors of any level
            return new ProcessResult(true, "PROCESS_SUB_TASK_SUCCESS");
        }

        return new ProcessResult(false, "UNKNOWN_BUG");
    }

    @Override
    public ProcessResult reduce(TaskContext taskContext, List<TaskResult> taskResults) {

        // After all tasks are executed, reduce will be executed
        // taskResults Save the execution results of all subtasks

        // Usage example, statistical execution results
        AtomicLong successCnt = new AtomicLong(0);
        taskResults.forEach(tr -> {
            if (tr.isSuccess()) {
                successCnt.incrementAndGet();
            }
        });
        return new ProcessResult(true, "success task num:" + successCnt.get());
    }

    // custom subtasks
    private static class SubTask {
        private Long siteId;
        private List<Long> idList;
    }
}

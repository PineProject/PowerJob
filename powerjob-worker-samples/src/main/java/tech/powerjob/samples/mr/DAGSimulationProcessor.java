package tech.powerjob.samples.mr;

import com.google.common.collect.Lists;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;

import java.util.List;

/**
 * Handler that simulates a DAG (before DAG support is officially provided, this method can be used instead)
 * <p>
 * ROOT -> A -> B  -> REDUCE
 * -> C
 *
 * @author tjq
 * @since 2020/5/15
 */
public class DAGSimulationProcessor implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        if (isRootTask()) {
            // L1. execute root task

            // Subtask A is generated after execution, and the parameters to be passed can be passed as attributes of TaskA
            TaskA taskA = new TaskA();
            try {
                map(Lists.newArrayList(taskA), "LEVEL1_TASK_A");
                return new ProcessResult(true, "map success");
            } catch (Exception e) {
                return new ProcessResult(false, "map failed");
            }
        }

        if (context.getSubTask() instanceof TaskA) {
            // L2. Execute A task

            // Subtasks B and C are generated after execution is completed (parallel execution)
            TaskB taskB = new TaskB();
            TaskC taskC = new TaskC();

            try {
                map(Lists.newArrayList(taskB, taskC), "LEVEL2_TASK_BC");
                return new ProcessResult(true, "map success");
            } catch (Exception ignore) {
            }
        }

        if (context.getSubTask() instanceof TaskB) {
            // L3. Execute task B
            return new ProcessResult(true, "xxx");
        }
        if (context.getSubTask() instanceof TaskC) {
            // L3. Execute the C task
            return new ProcessResult(true, "xxx");
        }

        return new ProcessResult(false, "UNKNOWN_TYPE_OF_SUB_TASK");
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        // L4. Execute the final Reduce task, taskResults saves the results of all previous tasks
        taskResults.forEach(taskResult -> {
            // do something...
        });
        return new ProcessResult(true, "reduce success");
    }

    private static class TaskA {
    }

    private static class TaskB {
    }

    private static class TaskC {

    }
}

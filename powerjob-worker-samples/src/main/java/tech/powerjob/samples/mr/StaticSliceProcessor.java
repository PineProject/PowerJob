package tech.powerjob.samples.mr;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.List;
import java.util.Map;

/**
 * MapReduce simulation static sharding
 * A typical way to kill a chicken with a sledgehammer～
 *
 * @author tjq
 * @since 2020/5/21
 */
@Component
public class StaticSliceProcessor implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();

        // root task Responsible for distributing tasks
        if (isRootTask()) {
            // Pass the shard parameters from the console, assuming the format is KV：1=a&2=b&3=c
            String jobParams = context.getJobParams();
            Map<String, String> paramsMap = Splitter.on("&").withKeyValueSeparator("=").split(jobParams);

            List<SubTask> subTasks = Lists.newLinkedList();
            paramsMap.forEach((k, v) -> subTasks.add(new SubTask(Integer.parseInt(k), v)));
            map(subTasks, "SLICE_TASK");
            return new ProcessResult(true, "map successfully");
        }

        Object subTask = context.getSubTask();
        if (subTask instanceof SubTask) {
            // Actual processing
            // Of course, if you think subTask is still too big, you can continue to distribute it

            return new ProcessResult(true, "subTask:" + ((SubTask) subTask).getIndex() + " process successfully");
        }
        return new ProcessResult(false, "UNKNOWN BUG");
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        // Do some statistical work as needed... If you don't need it, just use the Map processor directly
        return new ProcessResult(true, "xxxx");
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SubTask {
        private int index;
        private String params;
    }
}

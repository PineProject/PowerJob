package tech.powerjob.samples.processors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MapReduce Processor Example Console Parameters
 * console parameters：{"batchSize": 100, "batchNum": 2}
 *
 * @author tjq
 * @since 2020/4/17
 */
@Slf4j
@Component
public class MapReduceProcessorDemo implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        OmsLogger omsLogger = context.getOmsLogger();

        log.info("============== TestMapReduceProcessor#process ==============");
        log.info("isRootTask:{}", isRootTask());
        log.info("taskContext:{}", JsonUtils.toJSONString(context));

        // Obtain MR batch and subtask size according to console parameters
        final JSONObject jobParams = Optional.ofNullable(context.getJobParams()).map(JSONObject::parseObject).orElse(new JSONObject());

        Integer batchSize = (Integer) jobParams.getOrDefault("batchSize", 100);
        Integer batchNum = (Integer) jobParams.getOrDefault("batchNum", 10);

        if (isRootTask()) {
            log.info("==== MAP ====");
            omsLogger.info("[DemoMRProcessor] start root task~");
            List<TestSubTask> subTasks = Lists.newLinkedList();
            for (int j = 0; j < batchNum; j++) {
                for (int i = 0; i < batchSize; i++) {
                    int x = j * batchSize + i;
                    subTasks.add(new TestSubTask("name" + x, x));
                }
                map(subTasks, "MAP_TEST_TASK");
                subTasks.clear();
            }
            omsLogger.info("[DemoMRProcessor] map success~");
            return new ProcessResult(true, "MAP_SUCCESS");
        } else {
            log.info("==== NORMAL_PROCESS ====");
            omsLogger.info("[DemoMRProcessor] process subTask: {}.", JSON.toJSONString(context.getSubTask()));
            log.info("subTask: {}", JsonUtils.toJSONString(context.getSubTask()));
            Thread.sleep(1000);
            if (context.getCurrentRetryTimes() == 0) {
                return new ProcessResult(false, "FIRST_FAILED");
            } else {
                return new ProcessResult(true, "PROCESS_SUCCESS");
            }
        }
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        log.info("================ MapReduceProcessorDemo#reduce ================");
        log.info("TaskContext: {}", JSONObject.toJSONString(context));
        log.info("List<TaskResult>: {}", JSONObject.toJSONString(taskResults));
        context.getOmsLogger().info("MapReduce job finished, result is {}.", taskResults);

        boolean success = ThreadLocalRandom.current().nextBoolean();
        return new ProcessResult(success, context + ": " + success);
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSubTask {
        private String name;
        private int age;
    }
}

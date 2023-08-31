package tech.powerjob.official.processors.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import tech.powerjob.common.exception.PowerJobException;
import tech.powerjob.common.utils.NetUtils;
import tech.powerjob.official.processors.CommonBasicProcessor;
import tech.powerjob.official.processors.util.CommonUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.BroadcastProcessor;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Processor for functional verification, helping users quickly verify the functions they want to test
 *
 * @author tjq
 * @since 2023/8/13
 */
public class VerificationProcessor extends CommonBasicProcessor implements MapReduceProcessor, BroadcastProcessor {

    @Override
    protected ProcessResult process0(TaskContext taskContext) throws Exception {

        final OmsLogger omsLogger = taskContext.getOmsLogger();

        final String paramsStr = CommonUtils.parseParams(taskContext);
        final VerificationParam verificationParam = JSONObject.parseObject(paramsStr, VerificationParam.class);

        final Mode mode = Mode.of(verificationParam.getMode());

        switch (mode) {
            case ERROR:
                return new ProcessResult(false, "EXECUTE_FAILED_FOR_TEST");
            case EXCEPTION:
                throw new PowerJobException("exception for test");
            case TIMEOUT:
                final Long sleepMs = Optional.ofNullable(verificationParam.getSleepMs()).orElse(3600000L);
                Thread.sleep(sleepMs);
                return new ProcessResult(true, "AFTER_SLEEP_" + sleepMs);
            case RETRY:
                int currentRetryTimes = taskContext.getCurrentRetryTimes();
                int maxRetryTimes = taskContext.getMaxRetryTimes();
                omsLogger.info("[Retry] currentRetryTimes: {}, maxRetryTimes: {}", currentRetryTimes, maxRetryTimes);
                if (currentRetryTimes < maxRetryTimes) {
                    Thread.sleep(100);
                    omsLogger.info("[Retry] currentRetryTimes[{}] < maxRetryTimes[{}], return failed status!", currentRetryTimes, maxRetryTimes);
                    return new ProcessResult(false, "FAILED_UNTIL_LAST_RETRY_" + currentRetryTimes);
                } else {
                    omsLogger.info("[Retry] last retry, return success status!");
                    return new ProcessResult(true, "RETRY_SUCCESSFULLY!");
                }
            case MR:
                if (isRootTask()) {
                    final int batchNum = Optional.ofNullable(verificationParam.getBatchNum()).orElse(10);
                    final int batchSize = Optional.ofNullable(verificationParam.getBatchSize()).orElse(100);
                    omsLogger.info("[VerificationProcessor] start root task~");
                    List<TestSubTask> subTasks = new ArrayList<>();
                    for (int a = 0; a < batchNum; a++) {
                        for (int b = 0; b < batchSize; b++) {
                            int x = a * batchSize + b;
                            subTasks.add(new TestSubTask("task_" + x, x));
                        }
                        map(subTasks, "MAP_TEST_TASK_" + a);
                        omsLogger.info("[VerificationProcessor] [{}] map one batch successfully~", batchNum);
                        subTasks.clear();
                    }
                    omsLogger.info("[VerificationProcessor] all map successfully!");
                    return new ProcessResult(true, "MAP_SUCCESS");
                } else {
                    String taskId = taskContext.getTaskId();
                    final Double successRate = Optional.ofNullable(verificationParam.getSubTaskSuccessRate()).orElse(0.5);
                    final double rd = ThreadLocalRandom.current().nextDouble(0, 1);
                    boolean success = rd <= successRate;
                    long processCost = ThreadLocalRandom.current().nextLong(277);
                    Thread.sleep(processCost);
                    omsLogger.info("[VerificationProcessor] [MR] taskId:{}, processCost: {}, success:{}", taskId, processCost, success);
                    return new ProcessResult(success, RandomStringUtils.randomAlphanumeric(3));
                }
        }


        String randomMsg = RandomStringUtils.randomAlphanumeric(Optional.ofNullable(verificationParam.getResponseSize()).orElse(10));
        omsLogger.info("generate random string: {}", randomMsg);
        return new ProcessResult(true, "EXECUTE_SUCCESSFULLY_" + randomMsg);
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        List<String> successTaskIds = Lists.newArrayList();
        List<String> failedTaskIds = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        taskResults.forEach(taskResult -> {
            sb.append("tId:").append(taskResult.getTaskId()).append(";")
                    .append("tSuc:").append(taskResult.isSuccess()).append(";")
                    .append("tRes:").append(taskResult.getResult());
            if (taskResult.isSuccess()) {
                successTaskIds.add(taskResult.getTaskId());
            } else {
                failedTaskIds.add(taskResult.getTaskId());
            }
        });

        context.getOmsLogger().info("[Reduce] [summary] successTaskNum: {}, failedTaskNum: {}, successRate: {}",
                successTaskIds.size(), failedTaskIds.size(), 1.0 * successTaskIds.size() / (successTaskIds.size() + failedTaskIds.size()));
        context.getOmsLogger().info("[Reduce] successTaskIds: {}", successTaskIds);
        context.getOmsLogger().info("[Reduce] failedTaskIds: {}", failedTaskIds);

        return new ProcessResult(true, sb.toString());
    }

    /* ************************** 广播任务部分 ************************** */

    @Override
    public ProcessResult preProcess(TaskContext context) throws Exception {
        context.getOmsLogger().info("start to preProcess, current worker IP is {}.", NetUtils.getLocalHost());
        return new ProcessResult(true, "preProcess successfully!");
    }

    @Override
    public ProcessResult postProcess(TaskContext context, List<TaskResult> taskResults) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("start to postProcess, current worker IP is {}.", NetUtils.getLocalHost());
        omsLogger.info("====== All Node's Process Result ======");
        taskResults.forEach(r -> omsLogger.info("taskId:{},success:{},result:{}", r.getTaskId(), r.isSuccess(), r.getResult()));
        return new ProcessResult(true, "postProcess successfully!");
    }

    /* ************************** broadcast task section ************************** */

    enum Mode {
        /**
         * Normal mode, return the response directly
         * {"mode":"BASE","responseSize":12}
         */
        BASE,
        /**
         * Timeout, sleep for a period of time to test timeout control
         * {"mode":"TIMEOUT","sleepMs":3600000}
         */
        TIMEOUT,
        /**
         * Test execution failed, the response returns success = false
         * {"mode":"ERROR"}
         */
        ERROR,
        /**
         * Test execution exception, throw exception
         * {"mode":"EXCEPTION"}
         */
        EXCEPTION,
        /**
         * MapReduce, the console needs to be configured as MapReduce execution mode
         * {"mode":"MR","batchNum": 10, "batchSize": 20,"subTaskSuccessRate":0.7}
         */
        MR,
        /**
         * Success after retries, JOB configures Task retries
         * {"mode":"EXCEPTION"}
         */
        RETRY
        ;

        public static Mode of(String v) {
            for (Mode m : values()) {
                if (m.name().equalsIgnoreCase(v)) {
                    return m;
                }
            }
            return Mode.BASE;
        }
    }

    @Data
    public static class VerificationParam implements Serializable {
        /**
         * verification mode
         */
        private String mode;
        /**
         * sleep time, used for verification timeout
         */
        private Long sleepMs;
        /**
         * [MR] Batch size for validating MapReduce
         */
        private Integer batchSize;
        /**
         * [MR] batchNum
         */
        private Integer batchNum;
        /**
         * [MR] Subtask success rate
         */
        private Double subTaskSuccessRate;

        private Integer responseSize;
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSubTask {
        private String taskName;
        private int id;
    }
}

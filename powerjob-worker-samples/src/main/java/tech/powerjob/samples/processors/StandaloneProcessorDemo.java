package tech.powerjob.samples.processors;

import org.apache.commons.lang3.StringUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Standalone Processor Example
 *
 * @author tjq
 * @since 2020/4/17
 */
@Slf4j
@Component
public class StandaloneProcessorDemo implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("StandaloneProcessorDemo start process,context is {}.", context);
        omsLogger.info("Notice! If you want this job process failed, your jobParams need to be 'failed'");
        omsLogger.info("Let's test the exception~");
        // Test exception log
        try {
            Collections.emptyList().add("277");
        } catch (Exception e) {
            omsLogger.error("oh~it seems that we have an exception~", e);
        }
        log.info("================ StandaloneProcessorDemo#process ================");
        log.info("jobParam:{}", context.getJobParams());
        log.info("instanceParams:{}", context.getInstanceParams());
        String param;
        // When parsing parameters, when not in the workflow, instance parameters are preferred (allowing dynamic [instanceParams] to override static parameters [jobParams])
        if (context.getWorkflowContext() == null) {
            param = StringUtils.isBlank(context.getInstanceParams()) ? context.getJobParams() : context.getInstanceParams();
        } else {
            param = context.getJobParams();
        }
        // Judging whether it is successful according to the parameters
        boolean success = !"failed".equals(param);
        omsLogger.info("StandaloneProcessorDemo finished process,success: {}", success);
        omsLogger.info("anyway, we finished the job successfully~Congratulations!");
        return new ProcessResult(success, context + ": " + success);
    }
}

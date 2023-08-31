package tech.powerjob.worker.test.processors.demo;

import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;
import org.springframework.stereotype.Component;

/**
 * Example - Standalone Task Processor
 *
 * @author tjq
 * @since 2020/4/15
 */
@Component
public class BasicProcessorDemo implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        // Online log function, you can view task logs directly on the console, which is very convenient
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("BasicProcessorDemo start to process, current JobParams is {}.", context.getJobParams());

        // TaskContext It is the context information of the task, including the task metadata entered the console, and the commonly used fields are
        // jobParams (task parameters, entered the console), instanceParams (task instance parameters, which may exist only in task instances triggered by OpenAPI)

        // Do the actual processing...

        // Return the result, the result will be persisted to the database, and can be viewed directly on the front-end page, which is extremely convenient
        return new ProcessResult(true, "result is xxx");
    }
}

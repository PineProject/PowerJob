package tech.powerjob.worker.core.processor.sdk;

import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;

/**
 * Basic processor, suitable for stand-alone execution
 *
 * @author tjq
 * @since 2020/3/18
 */
public interface BasicProcessor {

    /**
     * Core processing logic
     * Workflow context can be obtained through {@link TaskContext#getWorkflowContext()} method
     *
     * @param context task context, console parameters and task instance parameters passed by OpenAPI can be obtained through jobParams and instanceParams respectively
     * @return processing result, msg has a length limit, if it is too long, it will be cropped, and it is not allowed to return null
     * @throws Exception is allowed to throw exceptions, but it is not recommended, and it is best handled by business developers themselves
     */
    ProcessResult process(TaskContext context) throws Exception;
}

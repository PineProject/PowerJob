package tech.powerjob.samples.tester;

import org.springframework.stereotype.Component;
import tech.powerjob.worker.annotation.PowerJobHandler;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.log.OmsLogger;

@Component(value = "springMethodProcessorService")
public class SpringMethodProcessorService {

    /**
     * Processor configuration method 1:
     *      fully qualified class name#method name, such as tech.powerjob.samples.tester.SpringMethodProcessorService#testEmptyReturn
     * Processor configuration method 2:
     *      SpringBean name#method name, such as springMethodProcessorService#testEmptyReturn
     *
     * @param context must have an input parameter TaskContext, and the return value can be null or any other type.
     *                Returning normally means success, and throwing an exception means execution failure
     */
    @PowerJobHandler(name = "testEmptyReturn")
    public void testEmptyReturn(TaskContext context) {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.warn("test log");
    }


    @PowerJobHandler(name = "testNormalReturn")
    public String testNormalReturn(TaskContext context) {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.warn("test log");
        return "testNormalReturn";
    }

    @PowerJobHandler(name = "testThrowException")
    public String testThrowException(TaskContext context) {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.warn("testThrowException");
        throw new IllegalArgumentException("test");
    }
}

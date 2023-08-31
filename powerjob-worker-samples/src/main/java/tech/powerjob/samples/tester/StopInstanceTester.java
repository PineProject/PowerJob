package tech.powerjob.samples.tester;

import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

/**
 * Test the problem reported by users that the instance cannot be stopped (can be interrupted)
 * https://github.com/PowerJob/PowerJob/issues/37
 *
 * @author tjq
 * @since 2020/7/30
 */
@Component
@SuppressWarnings("all")
public class StopInstanceTester implements BasicProcessor {
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        int i = 0;
        while (true) {
            System.out.println(i++);
            // interruptable
            Thread.sleep(10000L);
        }
    }
}

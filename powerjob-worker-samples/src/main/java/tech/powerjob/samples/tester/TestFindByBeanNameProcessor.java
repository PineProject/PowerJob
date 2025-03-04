package tech.powerjob.samples.tester;

import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

/**
 * The test directly uses BeanName to get the processor
 * The console can fill in powerJobTestBeanNameProcessor as processor information
 *
 * @author tjq
 * @since 2023/3/5
 */
@Component(value = "powerJobTestBeanNameProcessor")
public class TestFindByBeanNameProcessor implements BasicProcessor {
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        System.out.println("======== IN =======");
        return new ProcessResult(true, "Welcome to use PowerJob~");
    }
}

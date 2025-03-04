package tech.powerjob.worker.processor.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.extension.processor.ProcessorBean;
import tech.powerjob.worker.extension.processor.ProcessorDefinition;
import tech.powerjob.worker.extension.processor.ProcessorFactory;

import java.util.Set;

/**
 * The built-in default processor factory loads the processor through the fully qualified class name, but cannot enjoy the DI function of the IOC framework
 *
 * @author tjq
 * @since 2023/1/17
 */
@Slf4j
public class BuiltInDefaultProcessorFactory implements ProcessorFactory {

    @Override
    public Set<String> supportTypes() {
        return Sets.newHashSet(ProcessorType.BUILT_IN.name());
    }

    @Override
    public ProcessorBean build(ProcessorDefinition processorDefinition) {

        String className = processorDefinition.getProcessorInfo();

        try {
            Class<?> clz = Class.forName(className);
            BasicProcessor basicProcessor = (BasicProcessor) clz.getDeclaredConstructor().newInstance();
            return new ProcessorBean()
                    .setProcessor(basicProcessor)
                    .setClassLoader(basicProcessor.getClass().getClassLoader());
        }catch (Exception e) {
            log.warn("[ProcessorFactory] load local Processor(className = {}) failed.", className, e);
        }
        return null;
    }
}

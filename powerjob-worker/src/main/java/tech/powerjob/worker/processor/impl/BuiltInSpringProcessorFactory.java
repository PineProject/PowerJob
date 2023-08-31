package tech.powerjob.worker.processor.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.extension.processor.ProcessorBean;
import tech.powerjob.worker.extension.processor.ProcessorDefinition;
import tech.powerjob.worker.extension.processor.ProcessorFactory;

import java.util.Set;

/**
 * Built-in SpringBean processor factory for loading Spring-related beans, non-core dependencies
 *
 * @author tjq
 * @since 2023/1/17
 */
@Slf4j
public class BuiltInSpringProcessorFactory extends AbstractBuildInSpringProcessorFactory {


    public BuiltInSpringProcessorFactory(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public ProcessorBean build(ProcessorDefinition processorDefinition) {

        try {
            boolean canLoad = checkCanLoad();
            if (!canLoad) {
                log.info("[ProcessorFactory] can't find Spring env, this processor can't load by 'BuiltInSpringProcessorFactory'");
                return null;
            }
            String processorInfo = processorDefinition.getProcessorInfo();
            //Parameters used to distinguish method levels
            if (processorInfo.contains("#")) {
                return null;
            }
            BasicProcessor basicProcessor = getBean(processorInfo, applicationContext);
            return new ProcessorBean()
                    .setProcessor(basicProcessor)
                    .setClassLoader(basicProcessor.getClass().getClassLoader());
        } catch (NoSuchBeanDefinitionException ignore) {
            log.warn("[ProcessorFactory] can't find the processor in SPRING");
        } catch (Throwable t) {
            log.warn("[ProcessorFactory] load by BuiltInSpringProcessorFactory failed. If you are using Spring, make sure this bean was managed by Spring", t);
        }

        return null;
    }


}

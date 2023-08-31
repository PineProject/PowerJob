package tech.powerjob.worker;

import com.google.common.collect.Lists;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import tech.powerjob.worker.common.PowerJobWorkerConfig;
import tech.powerjob.worker.extension.processor.ProcessorFactory;
import tech.powerjob.worker.processor.impl.BuildInSpringMethodProcessorFactory;
import tech.powerjob.worker.processor.impl.BuiltInSpringProcessorFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Worker starters in Spring projects
 * Ability to obtain processors managed by the Spring IOC container
 *
 * @author tjq
 * @since 2023/1/20
 */
public class PowerJobSpringWorker implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * Composition is better than inheritance, holding PowerJobWorker, internally resetting ProcessorFactory is more elegant
     */
    private PowerJobWorker powerJobWorker;
    private final PowerJobWorkerConfig config;

    public PowerJobSpringWorker(PowerJobWorkerConfig config) {
        this.config = config;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        powerJobWorker = new PowerJobWorker(config);
        powerJobWorker.init();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        BuiltInSpringProcessorFactory springProcessorFactory = new BuiltInSpringProcessorFactory(applicationContext);

        BuildInSpringMethodProcessorFactory springMethodProcessorFactory = new BuildInSpringMethodProcessorFactory(applicationContext);
        // append BuiltInSpringProcessorFactory

        List<ProcessorFactory> processorFactories = Lists.newArrayList(
                Optional.ofNullable(config.getProcessorFactoryList())
                        .orElse(Collections.emptyList()));
        processorFactories.add(springProcessorFactory);
        processorFactories.add(springMethodProcessorFactory);
        config.setProcessorFactoryList(processorFactories);
    }

    @Override
    public void destroy() throws Exception {
        powerJobWorker.destroy();
    }
}

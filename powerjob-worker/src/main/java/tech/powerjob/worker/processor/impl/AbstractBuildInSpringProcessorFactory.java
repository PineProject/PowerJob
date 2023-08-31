package tech.powerjob.worker.processor.impl;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import tech.powerjob.common.enums.ProcessorType;
import tech.powerjob.worker.extension.processor.ProcessorFactory;

import java.util.Set;

@Slf4j
public abstract class AbstractBuildInSpringProcessorFactory implements ProcessorFactory {

    protected final ApplicationContext applicationContext;

    protected AbstractBuildInSpringProcessorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Set<String> supportTypes() {
        return Sets.newHashSet(ProcessorType.BUILT_IN.name());
    }

    protected boolean checkCanLoad() {
        try {
            ApplicationContext.class.getClassLoader();
            return applicationContext != null;
        } catch (Throwable ignore) {
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    protected static <T> T getBean(String className, ApplicationContext ctx) throws Exception {

        // 0. Try loading directly with the Bean name
        try {
            final Object bean = ctx.getBean(className);
            if (bean != null) {
                return (T) bean;
            }
        } catch (Exception ignore) {
        }

        // 1. ClassLoader If it exists, use clz to load it directly
        ClassLoader classLoader = ctx.getClassLoader();
        if (classLoader != null) {
            return (T) ctx.getBean(classLoader.loadClass(className));
        }
        // 2. ClassLoader does not exist (invisible to system class loader), try to load with lowercase class name
        String[] split = className.split("\\.");
        String beanName = split[split.length - 1];
        // lowercase to uppercase
        char[] cs = beanName.toCharArray();
        cs[0] += 32;
        String beanName0 = String.valueOf(cs);
        log.warn("[SpringUtils] can't get ClassLoader from context[{}], try to load by beanName:{}", ctx, beanName0);
        return (T) ctx.getBean(beanName0);
    }



}

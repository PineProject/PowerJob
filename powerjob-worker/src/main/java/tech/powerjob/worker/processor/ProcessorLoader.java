package tech.powerjob.worker.processor;

import tech.powerjob.worker.extension.processor.ProcessorBean;
import tech.powerjob.worker.extension.processor.ProcessorDefinition;

/**
 * Processor loader used internally
 *
 * @author Echo009
 * @since 2023/1/20
 */
public interface ProcessorLoader {

    ProcessorBean load(ProcessorDefinition definition);
}

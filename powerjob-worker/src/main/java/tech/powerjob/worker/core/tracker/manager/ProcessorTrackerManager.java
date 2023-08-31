package tech.powerjob.worker.core.tracker.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import tech.powerjob.worker.core.tracker.processor.ProcessorTracker;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * holds the Processor object
 * instanceId -> Processor
 *
 * @author tjq
 * @since 2020/3/20
 */
public class ProcessorTrackerManager {

    /**
     * instanceId -> (TaskTrackerAddress -> ProcessorTracker)
     * Handle the situation where there are multiple TaskTrackers in the same Instance in the case of split brain
     */
    private static final Map<Long, Map<String, ProcessorTracker>> PROCESSOR_TRACKER_CONTAINER = Maps.newHashMap();

    /**
     * Get the ProcessorTracker, create it if it doesn't exist
     */
    public static synchronized ProcessorTracker getProcessorTracker(Long instanceId, String address, Supplier<ProcessorTracker> creator) {

        ProcessorTracker processorTracker = PROCESSOR_TRACKER_CONTAINER.getOrDefault(instanceId, Collections.emptyMap()).get(address);
        if (processorTracker == null) {
            processorTracker = creator.get();
            PROCESSOR_TRACKER_CONTAINER.computeIfAbsent(instanceId, ignore -> Maps.newHashMap()).put(address, processorTracker);
        }
        return processorTracker;
    }

    public static synchronized List<ProcessorTracker> removeProcessorTracker(Long instanceId) {

        List<ProcessorTracker> res = Lists.newLinkedList();
        Map<String, ProcessorTracker> ttAddress2Pt = PROCESSOR_TRACKER_CONTAINER.remove(instanceId);
        if (ttAddress2Pt != null) {
            res.addAll(ttAddress2Pt.values());
            ttAddress2Pt.clear();
        }
        return res;
    }
}

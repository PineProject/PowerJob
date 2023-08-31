package tech.powerjob.worker.core.processor;

import tech.powerjob.common.WorkflowContextConstant;
import tech.powerjob.common.serialize.JsonUtils;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Workflow context
 *
 * @author Echo009
 * @since 2/19/2021
 */
@Getter
@Slf4j
public class WorkflowContext {
    /**
     * Workflow instance ID
     */
    private final Long wfInstanceId;
    /**
     * Current workflow context data
     * The data here is actually equivalent to instanceParams in {@link TaskContext}
     */
    private final Map<String, String> data = Maps. newHashMap();
    /**
     * Additional context information
     */
    private final Map<String, String> appendedContextData = Maps.newConcurrentMap();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public WorkflowContext(Long wfInstanceId, String data) {
        this.wfInstanceId = wfInstanceId;
        if (wfInstanceId == null || StringUtils. isBlank(data)) {
            return;
        }
        try {
            Map originMap = JsonUtils. parseObject(data, Map. class);
            originMap.forEach((k, v) -> this.data.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
        } catch (Exception exception) {
            log.warn("[WorkflowContext-{}] parse workflow context failed, {}", wfInstanceId, exception.getMessage());
        }
    }

    /**
     * Get the workflow context (MAP), essentially parsing data into MAP
     * The key of the initial parameter is {@link WorkflowContextConstant#CONTEXT_INIT_PARAMS_KEY}
     * Note that when no initial parameters are passed, the value obtained through CONTEXT_INIT_PARAMS_KEY is null
     *
     * @return workflow context
     * @author Echo009
     * @since 2021/02/04
     */
    public Map<String, String> fetchWorkflowContext() {
        return data;
    }

    /**
     * Add data to workflow context
     * Note: If the key already exists in the current context, it will be directly overwritten
     */
    public void appendData2WfContext(String key, Object value) {
        if (wfInstanceId == null) {
            // Tasks that are not in the workflow, just ignore them
            return;
        }
        String finalValue;
        try {
            // There is no limit to the length here, after the task is completed, it will be verified when it is reported to TaskTracker
            finalValue = JsonUtils.toJSONStringUnsafe(value);
        } catch (Exception e) {
            log.warn("[WorkflowContext-{}] fail to append data to workflow context, key : {}", wfInstanceId, key);
            return;
        }
        appendedContextData. put(key, finalValue);
    }


}

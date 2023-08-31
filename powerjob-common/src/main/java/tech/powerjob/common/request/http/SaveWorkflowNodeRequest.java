package tech.powerjob.common.request.http;

import lombok.Data;
import tech.powerjob.common.enums.WorkflowNodeType;
import tech.powerjob.common.utils.CommonUtils;



/**
 * Save workflow node information request
 * Workflow node's
 *
 * @author zenggonggu
 * @since 2021/02/02
 */
@Data
public class SaveWorkflowNodeRequest {

    private Long id;

    private Long appId;
    /**
     * Node type (default is task node)
     */
    private Integer type;
    /**
     * Task ID
     */
    private Long jobId;
    /**
     * Node alias, the default is the corresponding task name
     */
    private String nodeName;
    /**
     * Node parameters
     */
    private String nodeParams;
    /**
     * Whether to enable or not, it is enabled by default
     */
    private Boolean enable = true;
    /**
     * Whether to allow failure to skip, the default is not allowed
     */
    private Boolean skipWhenFailed = false;

    public void valid() {
        CommonUtils.requireNonNull(this.appId, "appId can't be empty");
        CommonUtils.requireNonNull(this.type, "type can't be empty");
        final WorkflowNodeType workflowNodeType = WorkflowNodeType.of(type);
        if (workflowNodeType == WorkflowNodeType.JOB || workflowNodeType == WorkflowNodeType.NESTED_WORKFLOW) {
            CommonUtils.requireNonNull(this.jobId, "jobId can't be empty");
        }
    }
}

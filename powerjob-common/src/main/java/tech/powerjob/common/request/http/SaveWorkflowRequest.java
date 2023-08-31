package tech.powerjob.common.request.http;

import tech.powerjob.common.enums.TimeExpressionType;
import tech.powerjob.common.model.LifeCycle;
import tech.powerjob.common.model.PEWorkflowDAG;
import tech.powerjob.common.utils.CommonUtils;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Create/modify Workflow request
 *
 * @author tjq
 * @since 2020/5/26
 */
@Data
public class SaveWorkflowRequest implements Serializable {

    private Long id;

    /**
     * Workflow name
     */
    private String wfName;
    /**
     * Workflow description
     */
    private String wfDescription;

    /**
     * The application ID to which it belongs (OpenClient does not need to be filled in by the user, it will be filled in automatically)
     */
    private Long appId;


    /* ************************* Timing parameters ********************* ***** */
    /**
     * Time expression type, only supports CRON and API
     */
    private TimeExpressionType timeExpressionType;
    /**
     * time expression, CRON/NULL/LONG/LONG
     */
    private String timeExpression;

    /**
     * The maximum number of workflows running at the same time, the default is 1
     */
    private Integer maxWfInstanceNum = 1;

    /**
     * ENABLE / DISABLE
     */
    private boolean enable = true;

    /**
     * An alarm for the overall failure of the workflow
     */
    private List<Long> notifyUserIds = Lists. newLinkedList();

    /** dotted notation */
    private PEWorkflowDAG dag;

    private LifeCycle lifeCycle;

    public void valid() {
        CommonUtils.requireNonNull(wfName, "workflow name can't be empty");
        CommonUtils.requireNonNull(appId, "appId can't be empty");
        CommonUtils.requireNonNull(timeExpressionType, "timeExpressionType can't be empty");
    }
}

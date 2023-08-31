package tech.powerjob.common.response;

import tech.powerjob.common.enums.InstanceStatus;
import lombok.Data;

import java.util.Date;

/**
 * instanceInfo Network transmission object
 *
 * @author tjq
 * @since 2020/5/14
 */
@Data
public class InstanceInfoDTO {

    /**
     * Task ID
     */
    private Long jobId;
    /**
     * The ID of the application to which the task belongs, redundancy improves query efficiency
     */
    private Long appId;
    /**
     * Task instance ID
     */
    private Long instanceId;
    /**
     * Workflow instance ID
     */
    private Long wfInstanceId;
    /**
     * task parameters
     */
    private String jobParams;
    /**
     * Task instance parameters
     */
    private String instanceParams;
    /**
     * Task Status {@link InstanceStatus}
     */
    private int status;
    /**
     * The type of the task instance, common/workflow (InstanceType)
     */
    private Integer type;
    /**
     * Results of the
     */
    private String result;
    /**
     * Estimated trigger time
     */
    private Long expectedTriggerTime;
    /**
     * Actual trigger time
     */
    private Long actualTriggerTime;
    /**
     * End Time
     */
    private Long finishedTime;
    /**
     * TaskTracker address
     */
    private String taskTrackerAddress;

    /**
     * The total number of executions (for retry judgment)
     */
    private Long runningTimes;

    private Date gmtCreate;
    private Date gmtModified;
}

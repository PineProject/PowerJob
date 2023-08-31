package tech.powerjob.common.response;

import lombok.Data;
import tech.powerjob.common.model.AlarmConfig;
import tech.powerjob.common.model.LogConfig;

import java.util.Date;

/**
 * jobInfo external output object
 *
 * @author tjq
 * @since 2020/5/14
 */
@Data
public class JobInfoDTO {

    private Long id;

    /* ************************* Basic information of the task******************** ***** */
    /**
     * mission name
     */
    private String jobName;
    /**
     * mission details
     */
    private String jobDescription;
    /**
     * Application ID to which the task belongs
     */
    private Long appId;
    /**
     * Parameters that come with the task
     */
    private String jobParams;

    /* ************************* Timing parameters ********************* ***** */
    /**
     * Time expression type (CRON/API/FIX_RATE/FIX_DELAY)
     */
    private Integer timeExpressionType;
    /**
     * time expression, CRON/NULL/LONG/LONG
     */
    private String timeExpression;

    /* ************************** Implementation modalities******************** ***** */
    /**
     * Execution type, stand-alone/broadcast/MR
     */
    private Integer executeType;
    /**
     * Executor type, Java/Shell
     */
    private Integer processorType;
    /**
     * Actuator information
     */
    private String processorInfo;

    /* ************************* Runtime configuration ********************* ***** */
    /**
     * The maximum number of concurrently running tasks, default 1
     */
    private Integer maxInstanceNum;
    /**
     * Concurrency, the maximum number of threads executing a task at the same time
     */
    private Integer concurrency;
    /**
     * Overall task timeout
     */
    private Long instanceTimeLimit;

    /** *************************** Retry configuration****************** *********** */
    private Integer instanceRetryNum;
    private Integer taskRetryNum;

    /**
     * 1 running normally, 2 stopped (no more scheduling)
     */
    private Integer status;
    /**
     * Next dispatch time
     */
    private Long nextTriggerTime;

    /* *************************** Busy Machine Configuration******************** ***** */
    /**
     * The minimum number of CPU cores, 0 means unlimited
     */
    private double minCpuCores;
    /**
     * The minimum memory space, in GB, 0 means unlimited
     */
    private double minMemorySpace;
    /**
     * The minimum disk space, in GB, 0 means unlimited
     */
    private double minDiskSpace;

    /* *************************** Cluster configuration********************** ***** */
    /**
     * Specify the machine to run, empty means unlimited, non-empty will only use one of the machines to run (multi-valued commas)
     */
    private String designatedWorkers;
    /**
     * Maximum number of machines
     */
    private Integer maxWorkerCount;

    /**
     * Alarm user ID list, multi-value comma separated
     */
    private String notifyUserIds;

    private Date gmtCreate;

    private Date gmtModified;

    private String extra;

    private Integer dispatchStrategy;

    private String lifecycle;

    private AlarmConfig alarmConfig;

    /**
     * Task classification, open to the access party to customize freely
     */
    private String tag;

    /**
     * Log configuration, including configuration information such as log level and log method
     */
    private LogConfig logConfig;

}

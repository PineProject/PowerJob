package tech.powerjob.common.request;

import lombok.Data;
import tech.powerjob.common.PowerSerializable;
import tech.powerjob.common.model.DeployedContainerInfo;
import tech.powerjob.common.model.SystemMetrics;

import java.util.List;


/**
 * Worker reports health information (heartbeat sent regularly by worker)
 *
 * @author tjq
 * @since 2020/3/25
 */
@Data
public class WorkerHeartbeat implements PowerSerializable {

    /**
     * Local machine address -> IP:port
     */
    private String workerAddress;
    /**
     * current appName
     */
    private String appName;
    /**
     * current appId
     */
    private Long appId;
    /**
     * current time
     */
    private long heartbeatTime;
    /**
     * The currently loaded container (container name -> container version)
     */
    private List<DeployedContainerInfo> containerInfos;
    /**
     * worker version information
     */
    private String version;
    /**
     * Communication protocol used AKKA / HTTP
     */
    private String protocol;
    /**
     * worker tag, which identifies a type of cluster under the same worker ISSUE: 226
     */
    private String tag;
    /**
     * client name
     */
    private String client;
    /**
     * no
     */
    private String extra;
    /**
     * Is it overloaded? In case of overload, the server will not send tasks to it for a period of time
     */
    private boolean isOverload;

    private int lightTaskTrackerNum;

    private int heavyTaskTrackerNum;


    private SystemMetrics systemMetrics;
}

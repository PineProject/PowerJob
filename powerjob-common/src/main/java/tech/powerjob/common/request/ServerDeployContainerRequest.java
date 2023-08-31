package tech.powerjob.common.request;

import tech.powerjob.common.PowerSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Worker deployment Container request
 *
 * @author tjq
 * @since 2020/5/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerDeployContainerRequest implements PowerSerializable {

    /**
     * Container ID
     */
    private Long containerId;
    /**
     * container name
     */
    private String containerName;
    /**
     * File name (MD5 value), used for version check and file download
     */
    private String version;
    /**
     * download link
     */
    private String downloadURL;
}

package tech.powerjob.common.request;

import tech.powerjob.common.PowerSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Worker needs to deploy containers and actively request information from Server
 *
 * @author tjq
 * @since 2020/5/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerNeedDeployContainerRequest implements PowerSerializable {
    private Long containerId;
}

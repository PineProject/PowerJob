package tech.powerjob.common.request;

import tech.powerjob.common.PowerSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server destroy container request
 *
 * @author tjq
 * @since 2020/5/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerDestroyContainerRequest implements PowerSerializable {
    private Long containerId;
}

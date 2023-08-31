package tech.powerjob.common.request;

import tech.powerjob.common.PowerSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * worker query executor cluster (required for dynamic online)
 *
 * @author tjq
 * @since 10/17/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerQueryExecutorClusterReq implements PowerSerializable {
    private Long appId;
    private Long jobId;
}

package tech.powerjob.common.request;

import tech.powerjob.common.PowerSerializable;
import tech.powerjob.common.model.InstanceLogContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * log report request
 *
 * @author tjq
 * @since 2020/4/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerLogReportReq implements PowerSerializable {
    private String workerAddress;
    private List<InstanceLogContent> instanceLogContents;
}

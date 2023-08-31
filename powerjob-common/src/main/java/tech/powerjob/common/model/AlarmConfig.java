package tech.powerjob.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Echo009
 * @since 2022/1/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmConfig {
    /**
     * Threshold to trigger an alert
     */
    private Integer alertThreshold;
    /**
     * Statistical window length (s)
     */
    private Integer statisticWindowLen;
    /**
     * Silent time window (s)
     */
    private Integer silenceWindowLen;

}

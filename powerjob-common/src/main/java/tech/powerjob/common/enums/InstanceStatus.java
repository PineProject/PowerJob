package tech.powerjob.common.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Status of the job instance
 *
 * @author tjq
 * @since 2020/3/17
 */
@Getter
@AllArgsConstructor
public enum InstanceStatus {
    /**
     *
     */
    WAITING_DISPATCH(1, "Waiting for dispatch"),
    WAITING_WORKER_RECEIVE(2, "Waiting for Worker to receive"),
    RUNNING(3, "Running"),
    FAILED(4, "Failed"),
    SUCCEED(5, "Success"),
    CANCELED(9, "Cancel"),
    STOPPED(10, "Manual stop");
    private final int v;
    private final String des;

    /**
     * Generalized operating status
     */
    public static final List<Integer> GENERALIZED_RUNNING_STATUS = Lists.newArrayList(WAITING_DISPATCH.v, WAITING_WORKER_RECEIVE.v, RUNNING.v);
    /**
     * end state
     */
    public static final List<Integer> FINISHED_STATUS = Lists.newArrayList(FAILED.v, SUCCEED.v, CANCELED.v, STOPPED.v);

    public static InstanceStatus of(int v) {
        for (InstanceStatus is : values()) {
            if (v == is.v) {
                return is;
            }
        }
        throw new IllegalArgumentException("InstanceStatus has no item for value " + v);
    }
}

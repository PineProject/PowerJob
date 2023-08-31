package tech.powerjob.common.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Workflow task running status
 *
 * @author tjq
 * @since 2020/5/26
 */
@Getter
@AllArgsConstructor
public enum WorkflowInstanceStatus {
    /**
     * The initial state is waiting for scheduling
     */
    WAITING(1, "Waiting for scheduling"),
    RUNNING(2, "Running"),
    FAILED(3, "Failed"),
    SUCCEED(4, "Success"),
    STOPPED(10, "Manual stop");

    /**
     * Generalized operating status
     */
    public static final List<Integer> GENERALIZED_RUNNING_STATUS = Collections. unmodifiableList(Lists. newArrayList(WAITING.v, RUNNING.v));
    /**
     * end state
     */
    public static final List<Integer> FINISHED_STATUS = Collections.unmodifiableList(Lists.newArrayList(FAILED.v, SUCCEED.v, STOPPED.v));

    private final int v;

    private final String des;

    public static WorkflowInstanceStatus of(int v) {
        for (WorkflowInstanceStatus is : values()) {
            if (v == is.v) {
                return is;
            }
        }
        throw new IllegalArgumentException("WorkflowInstanceStatus has no item for value " + v);
    }
}

package tech.powerjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Execution type.
 *
 * @author tjq
 * @since 2020/3/17
 */
@Getter
@AllArgsConstructor
public enum ExecuteType {
    /**
     * Standalone type of task.
     */
    STANDALONE(1, "stand-alone execution"),
    /**
     * Broadcast type of task.
     */
    BROADCAST(2, "Broadcast execution"),
    /**
     * MapReduce type of task.
     */
    MAP_REDUCE(3, "MapReduce"),
    MAP(4, "Map");

    private final int v;
    private final String des;

    public static ExecuteType of(int v) {
        for (ExecuteType type : values()) {
            if (type.v == v) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown ExecuteType of " + v);
    }
}

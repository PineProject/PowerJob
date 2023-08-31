package tech.powerjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * node type
 *
 * @author Echo009
 * @since 2021/3/7
 */
@Getter
@AllArgsConstructor
public enum WorkflowNodeType {
    /**
     * task node
     */
    JOB(1,false),
    /**
     * Judgment node
     */
    DECISION(2,true),
    /**
     * Embedded workflow
     */
    NESTED_WORKFLOW(3, false),

    ;

    private final int code;
    /**
     * Control node
     */
    private final boolean controlNode;

    public static WorkflowNodeType of(int code) {
        for (WorkflowNodeType nodeType : values()) {
            if (nodeType.code == code) {
                return nodeType;
            }
        }
        throw new IllegalArgumentException("unknown WorkflowNodeType of " + code);
    }



}

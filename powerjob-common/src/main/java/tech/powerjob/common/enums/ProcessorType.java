package tech.powerjob.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Task Processor Type
 *
 * @author tjq
 * @since 2020/3/23
 */
@Getter
@AllArgsConstructor
public enum ProcessorType {

    BUILT_IN(1, "Built-in Processor"),
    EXTERNAL(4, "External Processor (Dynamic Loading)"),

    @Deprecated
    SHELL(2, "SHELL script"),
    @Deprecated
    PYTHON(3, "Python script");
    private final int v;
    private final String des;

    public static ProcessorType of(int v) {
        for (ProcessorType type : values()) {
            if (type.v == v) {
                return type;
            }
        }
        throw new IllegalArgumentException("unknown ProcessorType of " + v);
    }
}

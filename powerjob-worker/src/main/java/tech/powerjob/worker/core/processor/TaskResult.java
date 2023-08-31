package tech.powerjob.worker.core.processor;

import lombok.Data;

/**
 * Task execution result
 *
 * @author tjq
 * @since 2020/4/17
 */
@Data
public class TaskResult {

    private String taskId;
    private boolean success;
    private String result;

}

package tech.powerjob.worker.core.processor.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.powerjob.common.exception.PowerJobCheckedException;
import tech.powerjob.common.utils.CollectionUtils;
import tech.powerjob.worker.common.ThreadLocalStore;
import tech.powerjob.worker.common.WorkerRuntime;
import tech.powerjob.worker.common.constants.TaskConstant;
import tech.powerjob.worker.common.utils.TransportUtils;
import tech.powerjob.worker.persistence.TaskDO;
import tech.powerjob.worker.pojo.request.ProcessorMapTaskRequest;

import java.util.List;

/**
 * Map processor, allowing developers to customize split tasks for distributed execution
 *
 * @author tjq
 * @since 2020/4/17
 */
public interface MapProcessor extends BasicProcessor {

    Logger log = LoggerFactory. getLogger(MapProcessor. class);

    int RECOMMEND_BATCH_SIZE = 200;

    /**
     * Distribute subtasks
     * @param taskList Subtask, can be obtained through TaskContext#getSubTask when executing again
     * @param taskName subtask name, that is, the value obtained by TaskContext#getTaskName in the subtask processor
     * @throws PowerJobCheckedException If the map fails, an exception will be thrown
     */
    default void map(List<?> taskList, String taskName) throws PowerJobCheckedException {

        if (CollectionUtils. isEmpty(taskList)) {
            return;
        }

        TaskDO task = ThreadLocalStore. getTask();
        WorkerRuntime workerRuntime = ThreadLocalStore.getRuntimeMeta();

        if (taskList. size() > RECOMMEND_BATCH_SIZE) {
            log.warn("[Map-{}] map task size is too large, network maybe overload... please try to split the tasks.", task.getInstanceId());
        }

        // Fix the problem caused by the name of the map task being consistent with the root task name or the final task name (infinitely generate subtasks or fail directly)
        if (TaskConstant.ROOT_TASK_NAME.equals(taskName) || TaskConstant.LAST_TASK_NAME.equals(taskName)) {
            log.warn("[Map-{}] illegal map task name : {}! please do not use 'OMS_ROOT_TASK' or 'OMS_LAST_TASK' as map task name. As a precaution, it will be renamed 'X-{}' automatically ." ,task.getInstanceId() ,taskName , taskName);
            taskName="X-"+taskName;
        }

        // 1. Construct the request
        ProcessorMapTaskRequest req = new ProcessorMapTaskRequest(task, taskList, taskName);

        // 2. Reliably send the request (the task is not allowed to be lost, you need to use the ask method, and an exception is thrown if it fails)
        boolean requestSucceed = TransportUtils. reliableMapTask(req, task. getAddress(), workerRuntime);

        if (requestSucceed) {
            log.info("[Map-{}] map task[name={},num={}] successfully!", task.getInstanceId(), taskName, taskList.size());
        } else {
            throw new PowerJobCheckedException("map failed for task: " + taskName);
        }
    }

    /**
     * Whether it is a root task
     * @return true -> root task / false -> non-root task
     */
    default boolean isRootTask() {
        TaskDO task = ThreadLocalStore.getTask();
        return TaskConstant.ROOT_TASK_NAME.equals(task.getTaskName());
    }
}

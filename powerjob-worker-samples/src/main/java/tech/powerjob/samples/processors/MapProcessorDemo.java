package tech.powerjob.samples.processors;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.samples.MysteryService;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.MapProcessor;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Map processor example
 *
 * @author tjq
 * @since 2020/4/18
 */
@Component
public class MapProcessorDemo implements MapProcessor {

    @Resource
    private MysteryService mysteryService;

    /**
     * The size of each batch of sending tasks
     */
    private static final int BATCH_SIZE = 100;
    /**
     * batch sent
     */
    private static final int BATCH_NUM = 5;

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        log.info("============== MapProcessorDemo#process ==============");
        log.info("isRootTask:{}", isRootTask());
        log.info("taskContext:{}", JsonUtils.toJSONString(context));
        log.info("{}", mysteryService.hasaki());

        if (isRootTask()) {
            log.info("==== MAP ====");
            List<SubTask> subTasks = Lists.newLinkedList();
            for (int j = 0; j < BATCH_NUM; j++) {
                SubTask subTask = new SubTask();
                subTask.siteId = j;
                subTask.itemIds = Lists.newLinkedList();
                subTasks.add(subTask);
                for (int i = 0; i < BATCH_SIZE; i++) {
                    subTask.itemIds.add(i + j * 100);
                }
            }
            map(subTasks, "MAP_TEST_TASK");
            return new ProcessResult(true, "map successfully");
        } else {
            log.info("==== PROCESS ====");
            SubTask subTask = (SubTask) context.getSubTask();
            for (Integer itemId : subTask.getItemIds()) {
                if (Thread.interrupted()) {
                    // task was interrupted
                    log.info("job has been stop! so stop to process subTask: {} => {}", subTask.getSiteId(), itemId);
                    break;
                }
                log.info("processing subTask: {} => {}", subTask.getSiteId(), itemId);
                int max = Integer.MAX_VALUE >> 7;
                for (int i = 0; ; i++) {
                    // Simulate time-consuming operations
                    if (i > max) {
                        break;
                    }
                }
            }
            // Test append context in Map task
            context.getWorkflowContext().appendData2WfContext("Yasuo", "A sword's poor company for a long road.");
            boolean b = ThreadLocalRandom.current().nextBoolean();
            if (context.getCurrentRetryTimes() >= 1) {
                // It will succeed if you try again
                b = true;
            }
            return new ProcessResult(b, "RESULT:" + b);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTask {
        private Integer siteId;
        private List<Integer> itemIds;
    }
}

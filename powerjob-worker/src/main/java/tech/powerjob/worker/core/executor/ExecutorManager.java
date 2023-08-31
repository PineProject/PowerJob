package tech.powerjob.worker.core.executor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import tech.powerjob.worker.common.PowerJobWorkerConfig;

import java.util.concurrent.*;

/**
 * @author Echo009
 * @since 2022/9/23
 */
@Getter
public class ExecutorManager {
    /**
     * Execute the underlying core tasks of Worker
     */
    private final ScheduledExecutorService coreExecutor;
    /**
     * Execute lightweight task status reporting
     */
    private final ScheduledExecutorService lightweightTaskStatusCheckExecutor;
    /**
     * Perform lightweight tasks
     */
    private final ExecutorService lightweightTaskExecutorService;


    public ExecutorManager(PowerJobWorkerConfig workerConfig){


        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        //Initialize the timing thread pool
        ThreadFactory coreThreadFactory = new ThreadFactoryBuilder().setNameFormat("powerjob-worker-core-%d").build();
        coreExecutor = new ScheduledThreadPoolExecutor(3, coreThreadFactory);

        ThreadFactory lightTaskReportFactory = new ThreadFactoryBuilder().setNameFormat("powerjob-worker-light-task-status-check-%d").build();
        // All are io-intensive tasks
        lightweightTaskStatusCheckExecutor = new ScheduledThreadPoolExecutor(availableProcessors * 10, lightTaskReportFactory);

        ThreadFactory lightTaskExecuteFactory = new ThreadFactoryBuilder().setNameFormat("powerjob-worker-light-task-execute-%d").build();
        // Most tasks are io intensive
        lightweightTaskExecutorService = new ThreadPoolExecutor(availableProcessors * 10,availableProcessors * 10, 120L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>((workerConfig.getMaxLightweightTaskNum() * 2),true), lightTaskExecuteFactory, new ThreadPoolExecutor.AbortPolicy());

    }



    public void shutdown(){
        coreExecutor.shutdownNow();
    }

}

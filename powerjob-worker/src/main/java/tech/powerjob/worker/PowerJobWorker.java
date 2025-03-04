package tech.powerjob.worker;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.common.PowerJobDKey;
import tech.powerjob.common.exception.PowerJobException;
import tech.powerjob.common.response.ResultDTO;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.common.utils.CommonUtils;
import tech.powerjob.common.utils.HttpUtils;
import tech.powerjob.common.utils.NetUtils;
import tech.powerjob.common.utils.PropertyUtils;
import tech.powerjob.remote.framework.base.Address;
import tech.powerjob.remote.framework.base.ServerType;
import tech.powerjob.remote.framework.engine.EngineConfig;
import tech.powerjob.remote.framework.engine.EngineOutput;
import tech.powerjob.remote.framework.engine.RemoteEngine;
import tech.powerjob.remote.framework.engine.impl.PowerJobRemoteEngine;
import tech.powerjob.worker.actors.ProcessorTrackerActor;
import tech.powerjob.worker.actors.TaskTrackerActor;
import tech.powerjob.worker.actors.WorkerActor;
import tech.powerjob.worker.background.OmsLogHandler;
import tech.powerjob.worker.background.ServerDiscoveryService;
import tech.powerjob.worker.background.WorkerHealthReporter;
import tech.powerjob.worker.common.PowerBannerPrinter;
import tech.powerjob.worker.common.PowerJobWorkerConfig;
import tech.powerjob.worker.common.WorkerRuntime;
import tech.powerjob.worker.core.executor.ExecutorManager;
import tech.powerjob.worker.extension.processor.ProcessorFactory;
import tech.powerjob.worker.persistence.TaskPersistenceService;
import tech.powerjob.worker.processor.PowerJobProcessorLoader;
import tech.powerjob.worker.processor.ProcessorLoader;
import tech.powerjob.worker.processor.impl.BuiltInDefaultProcessorFactory;
import tech.powerjob.worker.processor.impl.JarContainerProcessorFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client startup class
 *
 * @author KFCFans
 * @since 2020/3/16
 */
@Slf4j
public class PowerJobWorker {
    private final RemoteEngine remoteEngine;
    protected final WorkerRuntime workerRuntime;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public PowerJobWorker(PowerJobWorkerConfig config) {
        this.workerRuntime = new WorkerRuntime();
        this.remoteEngine = new PowerJobRemoteEngine();
        workerRuntime.setWorkerConfig(config);
    }

    public void init() throws Exception {

        if (!initialized.compareAndSet(false, true)) {
            log.warn("[PowerJobWorker] please do not repeat the initialization");
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("[PowerJobWorker] start to initialize PowerJobWorker...");

        PowerJobWorkerConfig config = workerRuntime.getWorkerConfig();
        CommonUtils.requireNonNull(config, "can't find PowerJobWorkerConfig, please set PowerJobWorkerConfig first");

        try {
            PowerBannerPrinter.print();
            // check appName
            if (!config.isEnableTestMode()) {
                assertAppName();
            } else {
                log.warn("[PowerJobWorker] using TestMode now, it's dangerous if this is production env.");
            }

            // Initialize the network data, and treat the report address and the local binding address differently (use the report address uniformly for external parties)
            String localBindIp = NetUtils.getLocalHost();
            int localBindPort = config.getPort();
            String externalIp = PropertyUtils.readProperty(PowerJobDKey.NT_EXTERNAL_ADDRESS, localBindIp);
            String externalPort = PropertyUtils.readProperty(PowerJobDKey.NT_EXTERNAL_PORT, String.valueOf(localBindPort));
            log.info("[PowerJobWorker] [ADDRESS_INFO] localBindIp: {}, localBindPort: {}; externalIp: {}, externalPort: {}", localBindIp, localBindPort, externalIp, externalPort);
            workerRuntime.setWorkerAddress(Address.toFullAddress(externalIp, Integer.parseInt(externalPort)));

            // Initialize the thread pool
            final ExecutorManager executorManager = new ExecutorManager(workerRuntime.getWorkerConfig());
            workerRuntime.setExecutorManager(executorManager);

            // initialization ProcessorLoader
            ProcessorLoader processorLoader = buildProcessorLoader(workerRuntime);
            workerRuntime.setProcessorLoader(processorLoader);

            // initialization actor
            TaskTrackerActor taskTrackerActor = new TaskTrackerActor(workerRuntime);
            ProcessorTrackerActor processorTrackerActor = new ProcessorTrackerActor(workerRuntime);
            WorkerActor workerActor = new WorkerActor(workerRuntime, taskTrackerActor);

            // Initialize the communication engine
            EngineConfig engineConfig = new EngineConfig()
                    .setType(config.getProtocol().name())
                    .setServerType(ServerType.WORKER)
                    .setBindAddress(new Address().setHost(localBindIp).setPort(localBindPort))
                    .setActorList(Lists.newArrayList(taskTrackerActor, processorTrackerActor, workerActor));

            EngineOutput engineOutput = remoteEngine.start(engineConfig);
            workerRuntime.setTransporter(engineOutput.getTransporter());

            // connect server
            ServerDiscoveryService serverDiscoveryService = new ServerDiscoveryService(workerRuntime.getAppId(), workerRuntime.getWorkerConfig());

            serverDiscoveryService.start(workerRuntime.getExecutorManager().getCoreExecutor());
            workerRuntime.setServerDiscoveryService(serverDiscoveryService);

            log.info("[PowerJobWorker] PowerJobRemoteEngine initialized successfully.");

            // Initialize the logging system
            OmsLogHandler omsLogHandler = new OmsLogHandler(workerRuntime.getWorkerAddress(), workerRuntime.getTransporter(), serverDiscoveryService);
            workerRuntime.setOmsLogHandler(omsLogHandler);

            // Initialize storage
            TaskPersistenceService taskPersistenceService = new TaskPersistenceService(workerRuntime.getWorkerConfig().getStoreStrategy());
            taskPersistenceService.init();
            workerRuntime.setTaskPersistenceService(taskPersistenceService);
            log.info("[PowerJobWorker] local storage initialized successfully.");


            // Initialize scheduled tasks
            workerRuntime.getExecutorManager().getCoreExecutor().scheduleAtFixedRate(new WorkerHealthReporter(workerRuntime), 0, config.getHealthReportInterval(), TimeUnit.SECONDS);
            workerRuntime.getExecutorManager().getCoreExecutor().scheduleWithFixedDelay(omsLogHandler.logSubmitter, 0, 5, TimeUnit.SECONDS);

            log.info("[PowerJobWorker] PowerJobWorker initialized successfully, using time: {}, congratulations!", stopwatch);
        }catch (Exception e) {
            log.error("[PowerJobWorker] initialize PowerJobWorker failed, using {}.", stopwatch, e);
            throw e;
        }
    }

    @SuppressWarnings("rawtypes")
    private void assertAppName() {

        PowerJobWorkerConfig config = workerRuntime.getWorkerConfig();
        String appName = config.getAppName();
        Objects.requireNonNull(appName, "appName can't be empty!");

        String url = "http://%s/server/assert?appName=%s";
        for (String server : config.getServerAddress()) {
            String realUrl = String.format(url, server, appName);
            try {
                String resultDTOStr = CommonUtils.executeWithRetry0(() -> HttpUtils.get(realUrl));
                ResultDTO resultDTO = JsonUtils.parseObject(resultDTOStr, ResultDTO.class);
                if (resultDTO.isSuccess()) {
                    Long appId = Long.valueOf(resultDTO.getData().toString());
                    log.info("[PowerJobWorker] assert appName({}) succeed, the appId for this application is {}.", appName, appId);
                    workerRuntime.setAppId(appId);
                    return;
                }else {
                    log.error("[PowerJobWorker] assert appName failed, this appName is invalid, please register the appName {} first.", appName);
                    throw new PowerJobException(resultDTO.getMessage());
                }
            }catch (PowerJobException oe) {
                throw oe;
            }catch (Exception ignore) {
                log.warn("[PowerJobWorker] assert appName by url({}) failed, please check the server address.", realUrl);
            }
        }
        log.error("[PowerJobWorker] no available server in {}.", config.getServerAddress());
        throw new PowerJobException("no server available!");
    }

    private ProcessorLoader buildProcessorLoader(WorkerRuntime runtime) {
        List<ProcessorFactory> customPF = Optional.ofNullable(runtime.getWorkerConfig().getProcessorFactoryList()).orElse(Collections.emptyList());
        List<ProcessorFactory> finalPF = Lists.newArrayList(customPF);

        // Add 2 systems in the rear ProcessorLoader
        finalPF.add(new BuiltInDefaultProcessorFactory());
        finalPF.add(new JarContainerProcessorFactory(runtime));

        return new PowerJobProcessorLoader(finalPF);
    }

    public void destroy() throws Exception {
        workerRuntime.getExecutorManager().shutdown();
        remoteEngine.close();
    }
}

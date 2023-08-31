package tech.powerjob.samples;

import org.springframework.context.annotation.Configuration;

/**
 * powerjob-worker configuration
 * Code configuration example, the SpringBoot project supports the use of starter, just need to complete the configuration in application.properties
 *
 * @author tjq
 * @since 2020/4/17
 */
@Configuration
public class PowerJobWorkerInitializer {

    /*
     Manual configuration version code
     Regular SpringBoot users can directly use the starter configuration, see application.properties for specific configuration

    @Bean
    public PowerJobSpringWorker initPowerJobSpringWorkerByCode() {

        // Initialize the PowerJob configuration file
        PowerJobWorkerConfig config = new PowerJobWorkerConfig();
        // Transport protocol, new users are recommended to go directly to HTTP
        config.setProtocol(Protocol.HTTP);
        // transport layer port number
        config.setPort(28888);
        // worker grouping, it is recommended to use the project name
        config.setAppName("powerjob-multi-worker-2");
        // server The service discovery address, supports multiple IP or HTTP domain names
        config.setServerAddress(Lists.newArrayList("127.0.0.1:7700", "127.0.0.1:7701"));
        // If there is no need for large Map/MapReduce, it is recommended to use memory to speed up calculation
        config.setStoreStrategy(StoreStrategy.DISK);
        // A custom label for the executor, which can be used to specify a part of the executor to run. Example: Set TAG as the unit name in a multi-unit computer room, and then run on the specified unit in the console
        config.setTag("CENTER");

        // The above is the core configuration, other configurations can directly refer to comments or official documents

        // Note Spring users please use PowerJobSpringWorker instead of PowerJobWorker which cannot use Spring-managed beans as executors
        return new PowerJobSpringWorker(config);
    }

     */
}

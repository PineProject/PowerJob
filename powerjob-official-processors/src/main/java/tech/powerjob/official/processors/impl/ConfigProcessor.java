package tech.powerjob.official.processors.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.official.processors.util.CommonUtils;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BroadcastProcessor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Configure the processor
 * Ultra-simple configuration center, used for configuration distribution, needs to be used with second-level + broadcast tasks!
 * For ultra-low-cost solutions, strong configuration or high SLA scenarios, please use standard configuration management middleware.
 * External call method {@link ConfigProcessor#fetchConfig()}
 *
 * @author tjq
 * @since 2022/9/17
 */
@Slf4j
public class ConfigProcessor implements BroadcastProcessor {

    /**
     * get configuration
     *
     * @return The configuration delivered by the console
     */
    public static Map<String, Object> fetchConfig() {
        if (config == null) {
            return Maps.newHashMap();
        }
        return Optional.ofNullable(config.getConfig()).orElse(Maps.newHashMap());
    }

    private static Config config;

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        Config newCfg = JsonUtils.parseObject(CommonUtils.parseParams(context), Config.class);
        context.getOmsLogger().info("[ConfigProcessor] receive and update config: {}", config);

        // Empty scenes are not updated
        final Map<String, Object> realConfig = newCfg.config;
        if (realConfig == null) {
            return new ProcessResult(false, "CONFIG_IS_NULL");
        }

        config = newCfg;

        if (StringUtils.isNotEmpty(config.persistentFileName)) {
            final File file = new File(config.persistentFileName);

            String content = JSONObject.toJSONString(realConfig);
            FileUtils.copyToFile(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), file);
        }

        return new ProcessResult(true, "UPDATE_SUCCESS");
    }

    @Data
    public static class Config implements Serializable {

        /**
         * original configuration
         */
        private Map<String, Object> config;

        /**
         * The full path name persisted to the local
         */
        private String persistentFileName;
    }
}
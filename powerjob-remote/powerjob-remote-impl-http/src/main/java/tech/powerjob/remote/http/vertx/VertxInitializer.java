package tech.powerjob.remote.http.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tech.powerjob.common.OmsConstant;
import tech.powerjob.common.PowerJobDKey;

/**
 * VertxInitializer
 * PowerJob just uses vertx as a toolkit
 *
 * @author tjq
 * @since 2023/1/1
 */
@Slf4j
public class VertxInitializer {

    /**
     * Persistent connection is enabled by default, and 75S timeout
     */
    private static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 75;

    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private static final int SERVER_IDLE_TIMEOUT_S = 300;

    public static Vertx buildVertx() {
        final int cpuCores = Runtime.getRuntime().availableProcessors();
        VertxOptions options = new VertxOptions()
                .setWorkerPoolSize(Math.max(16, 2 * cpuCores))
                .setInternalBlockingPoolSize(Math.max(32, 4 * cpuCores));
        log.info("[PowerJob-Vertx] use vertx options: {}", options);
        return Vertx.vertx(options);
    }

    public static HttpServer buildHttpServer(Vertx vertx) {
        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setIdleTimeout(SERVER_IDLE_TIMEOUT_S);
        tryEnableCompression(httpServerOptions);
        log.info("[PowerJob-Vertx] use HttpServerOptions: {}", httpServerOptions.toJson());
        return vertx.createHttpServer(httpServerOptions);
    }
    private static void tryEnableCompression(HttpServerOptions httpServerOptions) {
        // Non-core components, not directly dependent on classes (no import), loading errors can be ignored
        try {
            httpServerOptions
                    .addCompressor(io.netty.handler.codec.compression.StandardCompressionOptions.gzip())
                    .setCompressionSupported(true);
            log.warn("[PowerJob-Vertx] enable server side compression successfully!");
        } catch (Throwable t) {
            log.warn("[PowerJob-Vertx] enable server side compression failed. The error is not fatal, but performance may be degraded", t);
        }
    }

    public static HttpClient buildHttpClient(Vertx vertx) {

        HttpClientOptions httpClientOptions = new HttpClientOptions()
                .setMetricsName(OmsConstant.PACKAGE)
                .setConnectTimeout(CONNECTION_TIMEOUT_MS)
                .setMaxPoolSize(Math.max(8, Runtime.getRuntime().availableProcessors()) * 2);

        // Long connection
        String keepaliveTimeout = System.getProperty(PowerJobDKey.TRANSPORTER_KEEP_ALIVE_TIMEOUT, String.valueOf(DEFAULT_KEEP_ALIVE_TIMEOUT));
        int keepaliveTimeoutInt = Integer.parseInt(keepaliveTimeout);
        if (keepaliveTimeoutInt > 0) {
            httpClientOptions.setKeepAlive(true).setKeepAliveTimeout(keepaliveTimeoutInt);
        } else {
            httpClientOptions.setKeepAlive(false);
        }

        // compression decision
        String enableCompressing = System.getProperty(PowerJobDKey.TRANSPORTER_USE_COMPRESSING);
        if (StringUtils.isNotEmpty(enableCompressing)) {
            httpClientOptions.setTryUseCompression(StringUtils.equalsIgnoreCase(enableCompressing, Boolean.TRUE.toString()));
        }

        log.info("[PowerJob-Vertx] use HttpClientOptions: {}", httpClientOptions.toJson());
        return vertx.createHttpClient(httpClientOptions);
    }

}

package tech.powerjob.remote.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RequestBody;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tech.powerjob.common.exception.PowerJobException;
import tech.powerjob.remote.framework.actor.ActorInfo;
import tech.powerjob.remote.framework.actor.HandlerInfo;
import tech.powerjob.remote.framework.actor.ProcessType;
import tech.powerjob.remote.framework.cs.CSInitializer;
import tech.powerjob.remote.framework.cs.CSInitializerConfig;
import tech.powerjob.remote.framework.transporter.Transporter;
import tech.powerjob.remote.framework.utils.RemoteUtils;
import tech.powerjob.remote.http.vertx.VertxInitializer;
import tech.powerjob.remote.http.vertx.VertxTransporter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * HttpCSInitializer
 * After struggling for one night, I finally decided to use vertx as the bottom layer of http instead of using netty directly. The reasons are as follows:
 * - Netty is easy to implement, but performance tuning requires time cost and practical experience, and vertx, as the "direct line" framework of netty, is theoretically proficient in encapsulating netty, and performance is not a problem
 * - The only disadvantage of vertx is that as a relatively upper-level framework, there may be more serious package conflicts, especially for those users who themselves run on vertx-framework
 * - However, this problem can be solved by replacing the protocol. It is expected to provide an implementation based on netty and a custom protocol in the future
 *
 * @author tjq
 * @since 2022/12/31
 */
@Slf4j
public class HttpVertxCSInitializer implements CSInitializer {

    private Vertx vertx;
    private HttpServer httpServer;
    private HttpClient httpClient;

    private CSInitializerConfig config;

    @Override
    public String type() {
        return tech.powerjob.common.enums.Protocol.HTTP.name();
    }

    @Override
    public void init(CSInitializerConfig config) {
        this.config = config;
        vertx = VertxInitializer.buildVertx();
        httpServer = VertxInitializer.buildHttpServer(vertx);
        httpClient = VertxInitializer.buildHttpClient(vertx);
    }

    @Override
    public Transporter buildTransporter() {
        return new VertxTransporter(httpClient);
    }

    @Override
    @SneakyThrows
    public void bindHandlers(List<ActorInfo> actorInfos) {
        Router router = Router.router(vertx);
        // Handle request responses
        router.route().handler(BodyHandler.create());
        actorInfos.forEach(actorInfo -> {
            Optional.ofNullable(actorInfo.getHandlerInfos()).orElse(Collections.emptyList()).forEach(handlerInfo -> {
                String handlerHttpPath = handlerInfo.getLocation().toPath();
                ProcessType processType = handlerInfo.getAnno().processType();

                Handler<RoutingContext> routingContextHandler = buildRequestHandler(actorInfo, handlerInfo);
                Route route = router.post(handlerHttpPath);
                if (processType == ProcessType.BLOCKING) {
                    route.blockingHandler(routingContextHandler, false);
                } else {
                    route.handler(routingContextHandler);
                }
            });
        });

        // start up vertx http server
        final int port = config.getBindAddress().getPort();
        final String host = config.getBindAddress().getHost();

        httpServer.requestHandler(router)
                .exceptionHandler(e -> log.error("[PowerJob] unknown exception in Actor communication!", e))
                .listen(port, host)
                .toCompletionStage()
                .toCompletableFuture()
                .get(1, TimeUnit.MINUTES);

        log.info("[PowerJobRemoteEngine] startup vertx HttpServer successfully!");
    }

    private Handler<RoutingContext> buildRequestHandler(ActorInfo actorInfo, HandlerInfo handlerInfo) {
        Method method = handlerInfo.getMethod();
        Optional<Class<?>> powerSerializeClz = RemoteUtils.findPowerSerialize(method.getParameterTypes());

        // Internal framework, strict mode, direct error when binding fails
        if (!powerSerializeClz.isPresent()) {
            throw new PowerJobException("can't find any 'PowerSerialize' object in handler args: " + handlerInfo.getLocation());
        }

        return ctx -> {
            final RequestBody body = ctx.body();
            final Object convertResult = body.asPojo(powerSerializeClz.get());
            try {
                Object response = method.invoke(actorInfo.getActor(), convertResult);
                if (response != null) {
                    if (response instanceof String) {
                        ctx.end((String) response);
                    } else {
                        ctx.json(JsonObject.mapFrom(response));
                    }
                    return;
                }

                ctx.end();
            } catch (Throwable t) {
                // Note that this is when the framework is actually running, and the log output is in the standard PowerJob format
                log.error("[PowerJob] invoke Handler[{}] failed!", handlerInfo.getLocation(), t);
                ctx.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), t);
            }
        };
    }


    @Override
    public void close() throws IOException {
        httpClient.close();
        httpServer.close();
        vertx.close();
    }
}

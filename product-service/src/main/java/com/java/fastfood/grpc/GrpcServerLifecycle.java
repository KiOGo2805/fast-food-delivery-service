package com.java.fastfood.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(name = "grpc.server.enabled", havingValue = "true")
public class GrpcServerLifecycle implements SmartLifecycle {

    private final int port;
    private final ProductLookupGrpcService lookupService;
    private volatile Server server;

    public GrpcServerLifecycle(
            @Value("${grpc.server.port:9090}") int port,
            ProductLookupGrpcService lookupService) {
        this.port = port;
        this.lookupService = lookupService;
    }

    @Override
    public synchronized void start() {
        if (server != null) {
            return;
        }
        try {
            server = ServerBuilder.forPort(port).addService(lookupService).build().start();
            log.info("Product gRPC server started on port {}", port);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start product gRPC server on port " + port, exception);
        }
    }

    @Override
    public synchronized void stop() {
        if (server == null) {
            return;
        }
        server.shutdown();
        try {
            if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            server.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            server = null;
        }
        log.info("Product gRPC server stopped");
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}

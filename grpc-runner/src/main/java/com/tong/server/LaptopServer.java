package com.tong.server;

import com.tong.service.ImageStoreService;
import com.tong.service.ImageStoreServiceImpl;
import com.tong.service.LaptopService;
import com.tong.service.LaptopStoreService;
import com.tong.service.LaptopStoreServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LaptopServer {

    private final Logger logger = Logger.getLogger(LaptopServer.class.getName());

    private final int PORT;
    private final Server server;

    public LaptopServer(int port, LaptopStoreService laptopStore, ImageStoreService imageStore) {
        this(ServerBuilder.forPort(port), port, laptopStore, imageStore);
    }

    public LaptopServer(ServerBuilder serverBuilder, int port, LaptopStoreService laptopStore, ImageStoreService imageStore) {
        this.PORT = port;
        LaptopService laptopService = new LaptopService(laptopStore, imageStore);
        server = serverBuilder.addService(laptopService).build();
    }

    /**
     * 启动服务
     */
    public void start() throws IOException {
        server.start();
        logger.info("server started on port: " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("shutdown gRPC server because JVM shuts down");
            try {
                stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("server shutdown");
        }));
    }

    /**
     * 停止服务端
     */
    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * 等待服务端关闭
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LaptopStoreService laptopStore = new LaptopStoreServiceImpl();
        ImageStoreService imageStore = new ImageStoreServiceImpl("img");
        LaptopServer server = new LaptopServer(8080, laptopStore, imageStore);
        server.start();
        server.blockUntilShutdown();
    }
}

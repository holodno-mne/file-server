package com.exp.self.server;

import com.exp.self.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private final int port;
    private final RequestHandler requestHandler;
    private final Logger logger;

    public FileServer(int port, RequestHandler requestHandler) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.logger = LoggerFactory.getLogger(FileServer.class);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> requestHandler.handle(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.error("Error starting server: " + e.getMessage());
        }
    }
}

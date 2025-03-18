package com.exp.self.handler;

import com.exp.self.response.ResponseBuilder;
import com.exp.self.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler {
    private final FileService fileService;
    private final ResponseBuilder responseBuilder;
    private final Logger logger;

    public RequestHandler(FileService fileService, ResponseBuilder responseBuilder){
        this.fileService = fileService;
        this.responseBuilder = responseBuilder;
        this.logger = LoggerFactory.getLogger(RequestHandler.class);
    }

    public void handle(Socket clientSocket) {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream()) {
            String requestLine = in.readLine();
            if(requestLine == null) return;

            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            logger.info("Request: " + requestLine);

            if("GET".equals(method)){
                fileService.handleGetRequest(path, out);
            } else if ("POST". equals(method)) {
                fileService.handlePostRequest(in, out);
            } else {
                responseBuilder.sendResponse(out, "HTTP/1.1 400 Bad Request", "Unsupported method");
            }
        } catch (IOException e){
            logger.error("Error handling request: " + e.getMessage());
        } finally {
            try{
                clientSocket.close();
            } catch (IOException e){
                logger.error("Error closing socket: " + e.getMessage());
            }
        }
    }
}

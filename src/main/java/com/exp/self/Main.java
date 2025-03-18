package com.exp.self;

import com.exp.self.handler.RequestHandler;
import com.exp.self.response.ResponseBuilder;
import com.exp.self.server.FileServer;
import com.exp.self.service.FileService;

public class Main {
    public static void main(String[] args) {
        ResponseBuilder responseBuilder = new ResponseBuilder();
        FileService fileService = new FileService(responseBuilder);
        RequestHandler requestHandler = new RequestHandler(fileService, responseBuilder);
        FileServer fileServer = new FileServer(8081, requestHandler);
        fileServer.start();
    }
}
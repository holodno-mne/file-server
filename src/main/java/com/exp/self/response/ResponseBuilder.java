package com.exp.self.response;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseBuilder {
    public void sendResponse(OutputStream out, String status, String message) throws IOException {
        String response = status + "\r\n\r\n" + message;
        out.write(response.getBytes());
    }

    public void sendResponse(OutputStream out, String status, byte[] content, String contentType) throws IOException {
        String responseHeader = status + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Content-Length: " + content.length + "\r\n\r\n";
        out.write(responseHeader.getBytes());
        out.write(content);
    }
}

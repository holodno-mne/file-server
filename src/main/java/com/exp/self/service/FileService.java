package com.exp.self.service;

import com.exp.self.response.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
    private static final String STORAGE_DIR = "storage";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final ResponseBuilder responseBuilder;
    private final Logger logger;

    public FileService(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
        this.logger = LoggerFactory.getLogger(FileService.class);
        createStorageDirectory();
    }

    private void createStorageDirectory() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
            logger.info("Created storage directory: " + STORAGE_DIR);
        } catch (IOException e) {
            logger.error("Error creating storage directory: " + e.getMessage());
        }
    }

    private String generateUniqueFileName(String originalName, Path directory) throws IOException {
        String fileName = originalName;
        int counter = 1;

        while (Files.exists(directory.resolve(fileName))) {
            int dotIndex = originalName.lastIndexOf(".");
            String nameWithoutExtension = (dotIndex == -1) ? originalName : originalName.substring(0, dotIndex);
            String extension = (dotIndex == -1) ? "" : originalName.substring(dotIndex);
            fileName = nameWithoutExtension + "_" + counter + extension; // Добавляем суффикс
            counter++;
        }

        return fileName;
    }

    public void handleGetRequest(String path, OutputStream out) throws IOException {
        Path filePath = Paths.get(STORAGE_DIR, path.substring(1));
        if (!Files.exists(filePath)) {
            logger.error("File not found " + filePath);
            responseBuilder.sendResponse(out, "HTTP/1.1 404 Not Found", "File not found");
            return;
        }

        String mimeType = Files.probeContentType(filePath);
        if (!isSupportedMimeType(mimeType)) {
            logger.error("Unsupported file type" + mimeType);
            responseBuilder.sendResponse(out, "HTTP/1.1 415 Unsupported media type", "Unsupported file type");
            return;
        }

        byte[] fileContent = Files.readAllBytes(filePath);
        responseBuilder.sendResponse(out, "HTTP/1.1 200 OK", fileContent, mimeType);
        logger.info("File served: " + filePath);
    }

    public void handlePostRequest(BufferedReader in, OutputStream out) throws IOException {
        String headerLine;
        int contentLength = 0;
        String contentType = null;
        String originalName = null;

        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            if (headerLine.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(headerLine.substring("Content-Length:".length()).trim());
            } else if (headerLine.startsWith("Content-Type:")) {
                contentType = headerLine.substring("Content-Type:".length()).trim();
            } else if (headerLine.startsWith("Content-Disposition:")) {
                String[] parts = headerLine.split(";");
                for (String part : parts) {
                    if (part.trim().startsWith("filename=")) {
                        originalName = part.trim().substring("filename=".length()).replace("\"", "");
                        break;
                    }
                }
            }
        }

        if (originalName == null) {
            originalName = "uploaded_file" + getFileExtension(contentType);
        }

        if (contentLength > MAX_FILE_SIZE) {
            logger.error("File size exceeds limit: " + contentLength);
            responseBuilder.sendResponse(out, "HTTP/1.1 413 Payload Too Large", "File size exceeds limit");
            return;
        }

        if (!isSupportedMimeType(contentType)) {
            logger.error("Unsupported file type: " + contentType);
            responseBuilder.sendResponse(out, "HTTP/1.1 415 Unsupported Media Type", "Unsupported file type");
            return;
        }

        char[] buffer = new char[contentLength];
        in.read(buffer, 0, contentLength);
        byte[] fileContent = new String(buffer).getBytes();

        String uniqueFileName = generateUniqueFileName(originalName, Paths.get(STORAGE_DIR));
        Path filePath = Paths.get(STORAGE_DIR, uniqueFileName);

        Files.write(filePath, fileContent);

        responseBuilder.sendResponse(out, "HTTP/1.1 201 Created", "File uploaded successfully");
        logger.info("File uploaded: " + filePath);
    }

    private boolean isSupportedMimeType(String mimeType) {
        return mimeType != null && (mimeType.equals("application/pdf") ||
                mimeType.equals("application/zip") ||
                mimeType.equals("image/png") ||
                mimeType.equals("image/jpeg") ||
                mimeType.equals("text/plain"));
    }

    private String getFileExtension(String mimeType) {
        switch (mimeType) {
            case "application/pdf":
                return ".pdf";
            case "application/zip":
                return ".zip";
            case "image/png":
                return ".png";
            case "image/jpeg":
                return ".jpeg";
            case "text/plain":
                return ".txt";
            default:
                return "";
        }
    }

}

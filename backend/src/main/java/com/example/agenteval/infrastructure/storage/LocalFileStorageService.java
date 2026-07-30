package com.example.agenteval.infrastructure.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Local file-system implementation of ObjectStorageService.
 * Used in dev/test mode when no external object storage is configured.
 *
 * Directory layout:
 *   <base-dir>/<bucket>/<key>
 *
 * Example:
 *   /tmp/agent-eval-storage/agent-eval/cases/C001/prompt.txt
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.local.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalFileStorageService implements ObjectStorageService {

    private static final String DEFAULT_BASE_DIR = "/tmp/agent-eval-storage";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Path resolvePath(String uri) {
        // URI format: s3://bucket/key  or  local:///base/bucket/key
        String path;
        if (uri.startsWith("local://")) {
            path = uri.substring("local://".length());
        } else if (uri.startsWith("s3://")) {
            String rest = uri.substring("s3://".length());
            int slash = rest.indexOf('/');
            if (slash > 0) {
                path = DEFAULT_BASE_DIR + "/" + rest.substring(0, slash) + "/" + rest.substring(slash + 1);
            } else {
                path = DEFAULT_BASE_DIR + "/" + rest;
            }
        } else {
            path = DEFAULT_BASE_DIR + "/" + uri;
        }
        return Paths.get(path);
    }

    private String buildUri(String bucket, String key) {
        return "s3://" + bucket + "/" + key;
    }

    @Override
    public String uploadText(String key, String content) {
        try {
            Path filePath = resolvePath(key);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content != null ? content : "");
            String uri = buildUri("agent-eval", key);
            log.debug("Uploaded text: {} ({} chars)", uri, content != null ? content.length() : 0);
            return uri;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload text: " + key, e);
        }
    }

    @Override
    public String downloadText(String uri) {
        try {
            Path filePath = resolvePath(uri);
            if (!Files.exists(filePath)) {
                log.warn("Object not found: {}", uri);
                return null;
            }
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to download text: " + uri, e);
        }
    }

    @Override
    public String upload(String key, byte[] content, String contentType) {
        try {
            Path filePath = resolvePath(key);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content != null ? content : new byte[0]);
            return buildUri("agent-eval", key);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload: " + key, e);
        }
    }

    @Override
    public byte[] download(String uri) {
        try {
            Path filePath = resolvePath(uri);
            if (!Files.exists(filePath)) {
                return null;
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to download: " + uri, e);
        }
    }

    @Override
    public boolean delete(String uri) {
        try {
            Path filePath = resolvePath(uri);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete: {}", uri, e);
            return false;
        }
    }

    @Override
    public boolean exists(String uri) {
        Path filePath = resolvePath(uri);
        return Files.exists(filePath);
    }

    @Override
    public <T> List<T> downloadJsonList(String uri, Class<T> clazz) {
        try {
            Path filePath = resolvePath(uri);
            if (!Files.exists(filePath)) {
                log.warn("Object not found: {}", uri);
                return new ArrayList<>();
            }
            String json = Files.readString(filePath);
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<T>>() {});
        } catch (IOException e) {
            log.error("Failed to download JSON list: {}", uri, e);
            return new ArrayList<>();
        }
    }

    @Override
    public <T> String uploadJsonList(String key, List<T> list) {
        try {
            Path filePath = resolvePath(key);
            Files.createDirectories(filePath.getParent());
            String json = objectMapper.writeValueAsString(list != null ? list : new ArrayList<>());
            Files.writeString(filePath, json);
            return buildUri("agent-eval", key);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to upload JSON list: " + key, e);
        }
    }
}

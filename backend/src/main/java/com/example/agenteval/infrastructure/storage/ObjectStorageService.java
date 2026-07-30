package com.example.agenteval.infrastructure.storage;

import java.util.List;

/**
 * Object storage abstraction for large content (prompts, standard answers).
 *
 * Implementations:
 * - MinIO (S3-compatible, self-hosted)
 * - Alibaba Cloud OSS
 * - AWS S3
 * - LocalFileSystem (dev/test fallback)
 */
public interface ObjectStorageService {

    /**
     * Upload a text file and return its object URI.
     *
     * @param key      object key (e.g. "cases/C001/prompt.txt")
     * @param content  text content
     * @return object URI (e.g. "s3://bucket/cases/C001/prompt.txt")
     */
    String uploadText(String key, String content);

    /**
     * Download a text file by its object URI.
     *
     * @param uri  object URI returned by uploadText()
     * @return text content, or null if not found
     */
    String downloadText(String uri);

    /**
     * Upload a byte array and return its object URI.
     */
    String upload(String key, byte[] content, String contentType);

    /**
     * Download a byte array by its object URI.
     */
    byte[] download(String uri);

    /**
     * Delete an object by URI.
     *
     * @return true if deleted, false if not found
     */
    boolean delete(String uri);

    /**
     * Check whether an object exists.
     */
    boolean exists(String uri);

    /**
     * Download and parse a JSON list from the given URI.
     *
     * @param uri   object URI
     * @param clazz element type
     * @param <T>   element type
     * @return parsed list, or empty list if not found
     */
    <T> List<T> downloadJsonList(String uri, Class<T> clazz);

    /**
     * Upload a list as JSON to object storage.
     *
     * @param key  object key
     * @param list list to serialize as JSON
     * @return object URI
     */
    <T> String uploadJsonList(String key, List<T> list);
}

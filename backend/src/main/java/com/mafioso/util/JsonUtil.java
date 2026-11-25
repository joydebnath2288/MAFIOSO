package com.mafioso.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Thread-safe Jackson JSON serialization and Servlet response helper.
 */
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonUtil() {
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serializes an arbitrary object into a JSON string.
     *
     * @param value object to serialize
     * @return JSON string
     */
    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize JSON response", ex);
        }
    }

    /**
     * Writes a JSON payload with UTF-8 encoding and HTTP status code to the response.
     *
     * @param response servlet response
     * @param statusCode HTTP status code
     * @param payload object to serialize as JSON
     * @throws IOException if writing to the response stream fails
     */
    public static void writeJson(HttpServletResponse response,
                                 int statusCode,
                                 Object payload) throws IOException {
        response.setStatus(statusCode);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        OBJECT_MAPPER.writeValue(response.getWriter(), payload);
    }

    /**
     * Reads and parses a bounded JSON request body (up to 64 KB) using the shared {@link ObjectMapper}.
     *
     * @param request incoming HTTP request
     * @param type target DTO class
     * @param <T> target DTO type
     * @return deserialized request object
     * @throws IOException if reading the request stream fails
     * @throws IllegalArgumentException if the payload exceeds 64 KB, is empty, or is malformed JSON
     */
    public static <T> T readJsonBody(HttpServletRequest request, Class<T> type) throws IOException {
        final int maxBodyBytes = 65536;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int totalRead = 0;
        try (InputStream in = request.getInputStream()) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                totalRead += n;
                if (totalRead > maxBodyBytes) {
                    throw new IllegalArgumentException("Request payload is too large.");
                }
                out.write(buffer, 0, n);
            }
        }
        String raw = new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Request body must be valid JSON.");
        }
        try {
            return OBJECT_MAPPER.readValue(raw, type);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Malformed JSON request body.");
        }
    }
}

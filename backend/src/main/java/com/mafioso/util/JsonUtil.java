package com.mafioso.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
}

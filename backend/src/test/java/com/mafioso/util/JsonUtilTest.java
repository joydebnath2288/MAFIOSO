package com.mafioso.util;

import com.mafioso.dto.HealthResponse;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Jackson JSON serialization via {@link JsonUtil}.
 */
public class JsonUtilTest {

    @Test
    public void serializesHealthResponseWithRequiredFields() {
        HealthResponse response = new HealthResponse(
                true,
                "MAFIOSO",
                "UP",
                "development",
                "0.1.0-SNAPSHOT",
                "2026-11-25T06:30:00Z"
        );

        String json = JsonUtil.toJson(response);

        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"application\":\"MAFIOSO\""));
        assertTrue(json.contains("\"status\":\"UP\""));
    }
}

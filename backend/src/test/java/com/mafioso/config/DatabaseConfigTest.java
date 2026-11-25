package com.mafioso.config;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for environment-based {@link DatabaseConfig}.
 */
public class DatabaseConfigTest {

    @Test
    public void buildsJdbcUrlUsingDockerServiceHost() {
        Map<String, String> env = new HashMap<String, String>();
        env.put("DB_HOST", "db");
        env.put("DB_PORT", "3306");
        env.put("DB_NAME", "mafioso_db");
        env.put("DB_USER", "mafioso_user");
        env.put("DB_PASSWORD", "secret_value");
        env.put("DB_USE_SSL", "false");

        DatabaseConfig config = DatabaseConfig.fromMap(env);
        String jdbcUrl = config.buildJdbcUrl();

        assertEquals("db", config.getHost());
        assertEquals(3306, config.getPort());
        assertEquals("mafioso_db", config.getDatabaseName());
        assertFalse(config.isUseSsl());
        assertTrue(jdbcUrl.startsWith("jdbc:mysql://db:3306/mafioso_db?"));
        assertFalse(jdbcUrl.contains("localhost"));
    }

    @Test(expected = IllegalStateException.class)
    public void failsFastWhenRequiredVariablesAreMissing() {
        Map<String, String> env = new HashMap<String, String>();
        env.put("DB_HOST", "db");
        DatabaseConfig.fromMap(env);
    }
}

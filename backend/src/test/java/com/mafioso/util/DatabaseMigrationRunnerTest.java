package com.mafioso.util;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for SQL statement splitting and checksum computation in {@link DatabaseMigrationRunner}.
 */
public class DatabaseMigrationRunnerTest {

    @Test
    public void splitsSqlStatementsWhileIgnoringLineComments() {
        String script = "-- Comment header\n"
                + "CREATE TABLE IF NOT EXISTS demo (\n"
                + "  id INT PRIMARY KEY\n"
                + ");\n\n"
                + "-- Second statement\n"
                + "INSERT INTO demo (id) VALUES (1);\n";

        List<String> statements = DatabaseMigrationRunner.splitSqlStatements(script);
        assertEquals(2, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE TABLE IF NOT EXISTS demo"));
        assertTrue(statements.get(1).startsWith("INSERT INTO demo"));
    }
}

package com.campus.matching;

import com.campus.matching.db.DatabaseManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    @Test
    public void testSingletonInstance() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();

        assertNotNull(instance1, "DatabaseManager instance should not be null");
        assertSame(instance1, instance2, "DatabaseManager must return the exact same instance every time (Singleton)");
    }

    @Test
    public void testSQLiteConnection() throws SQLException {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        assertTrue(dbManager.testConnection(), "Database connection test should succeed");

        try (Connection conn = dbManager.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        }
    }

    @Test
    public void testSchemaInitialization() {
        com.campus.matching.db.DatabaseInitializer initializer = new com.campus.matching.db.DatabaseInitializer();
        initializer.initializeSchema();

        java.util.List<String> tables = initializer.getExistingTables();
        java.util.List<String> expectedTables = java.util.List.of(
            "students", "skills", "student_skills",
            "interests", "student_interests",
            "roles", "student_roles",
            "projects", "project_skills", "project_roles", "project_interests",
            "teams", "team_members", "invitations"
        );

        for (String expected : expectedTables) {
            assertTrue(tables.contains(expected), "Expected table '" + expected + "' to exist in SQLite database");
        }
    }
}

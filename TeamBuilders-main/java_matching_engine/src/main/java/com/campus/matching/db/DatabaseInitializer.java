package com.campus.matching.db;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service responsible for creating and maintaining the SQLite database schema
 * using pure JDBC statements.
 *
 * Runs automatically upon Spring Boot application startup.
 */
@Component
@Order(1)
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    // DDL statements for the 14 application tables
    private static final List<String> SCHEMA_DDL = List.of(
        // 1. students
        """
        CREATE TABLE IF NOT EXISTS students (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username VARCHAR(150) NOT NULL UNIQUE,
            email VARCHAR(254) NOT NULL UNIQUE,
            password_hash VARCHAR(128) NOT NULL DEFAULT '',
            department VARCHAR(100),
            year INTEGER DEFAULT 1 CHECK (year >= 1 AND year <= 6),
            experience_level VARCHAR(20) DEFAULT 'INTERMEDIATE' CHECK (experience_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
            bio TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        """,

        // 2. skills
        """
        CREATE TABLE IF NOT EXISTS skills (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR(100) NOT NULL UNIQUE
        );
        """,

        // 3. student_skills (many-to-many)
        """
        CREATE TABLE IF NOT EXISTS student_skills (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            skill_id INTEGER NOT NULL,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
            UNIQUE (student_id, skill_id)
        );
        """,

        // 4. interests
        """
        CREATE TABLE IF NOT EXISTS interests (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR(100) NOT NULL UNIQUE
        );
        """,

        // 5. student_interests (many-to-many)
        """
        CREATE TABLE IF NOT EXISTS student_interests (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            interest_id INTEGER NOT NULL,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE,
            UNIQUE (student_id, interest_id)
        );
        """,

        // 6. roles
        """
        CREATE TABLE IF NOT EXISTS roles (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR(100) NOT NULL UNIQUE
        );
        """,

        // 7. student_roles (many-to-many)
        """
        CREATE TABLE IF NOT EXISTS student_roles (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER NOT NULL,
            role_id INTEGER NOT NULL,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
            UNIQUE (student_id, role_id)
        );
        """,

        // 8. projects (supports student ownership via created_by_id)
        """
        CREATE TABLE IF NOT EXISTS projects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name VARCHAR(200) NOT NULL,
            description TEXT NOT NULL,
            project_type VARCHAR(20) NOT NULL DEFAULT 'WEB' CHECK (project_type IN ('WEB', 'ML', 'HARDWARE', 'OTHER')),
            team_size INTEGER NOT NULL DEFAULT 3 CHECK (team_size >= 1 AND team_size <= 20),
            status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
            created_by_id INTEGER NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (created_by_id) REFERENCES students(id) ON DELETE CASCADE
        );
        """,

        // 9. project_skills (many-to-many with required count)
        """
        CREATE TABLE IF NOT EXISTS project_skills (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            project_id INTEGER NOT NULL,
            skill_id INTEGER NOT NULL,
            required_count INTEGER NOT NULL DEFAULT 1 CHECK (required_count >= 1),
            FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
            FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
            UNIQUE (project_id, skill_id)
        );
        """,

        // 10. project_roles (many-to-many with required count)
        """
        CREATE TABLE IF NOT EXISTS project_roles (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            project_id INTEGER NOT NULL,
            role_id INTEGER NOT NULL,
            required_count INTEGER NOT NULL DEFAULT 1 CHECK (required_count >= 1),
            FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
            UNIQUE (project_id, role_id)
        );
        """,

        // 11. project_interests (many-to-many)
        """
        CREATE TABLE IF NOT EXISTS project_interests (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            project_id INTEGER NOT NULL,
            interest_id INTEGER NOT NULL,
            FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
            FOREIGN KEY (interest_id) REFERENCES interests(id) ON DELETE CASCADE,
            UNIQUE (project_id, interest_id)
        );
        """,

        // 12. teams (belongs to a project)
        """
        CREATE TABLE IF NOT EXISTS teams (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            project_id INTEGER NOT NULL UNIQUE,
            name VARCHAR(150) NOT NULL,
            is_finalized BOOLEAN NOT NULL DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
        );
        """,

        // 13. team_members (students <-> teams relationship)
        """
        CREATE TABLE IF NOT EXISTS team_members (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            team_id INTEGER NOT NULL,
            student_id INTEGER NOT NULL,
            role_id INTEGER,
            assigned_role_name VARCHAR(100),
            joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
            FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE SET NULL,
            UNIQUE (team_id, student_id)
        );
        """,

        // 14. invitations (inviting students to projects)
        """
        CREATE TABLE IF NOT EXISTS invitations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            project_id INTEGER NOT NULL,
            sender_id INTEGER NOT NULL,
            receiver_id INTEGER NOT NULL,
            role_name VARCHAR(100),
            status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
            FOREIGN KEY (sender_id) REFERENCES students(id) ON DELETE CASCADE,
            FOREIGN KEY (receiver_id) REFERENCES students(id) ON DELETE CASCADE,
            UNIQUE (project_id, sender_id, receiver_id)
        );
        """
    );

    @Override
    public void run(ApplicationArguments args) {
        initializeSchema();
    }

    /**
     * Executes table creation DDLs over JDBC connection.
     */
    public synchronized void initializeSchema() {
        LOGGER.info("Starting SQLite database schema initialization...");
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key constraint enforcement in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            for (String ddl : SCHEMA_DDL) {
                stmt.execute(ddl);
            }

            // Lightweight JDBC migration for databases created by an earlier Step 1 build.
            try (ResultSet columns = stmt.executeQuery("PRAGMA table_info(students);")) {
                boolean hasPasswordHash = false;
                while (columns.next()) {
                    if ("password_hash".equalsIgnoreCase(columns.getString("name"))) {
                        hasPasswordHash = true;
                        break;
                    }
                }
                if (!hasPasswordHash) {
                    stmt.execute("ALTER TABLE students ADD COLUMN password_hash VARCHAR(128) NOT NULL DEFAULT '';");
                }
            }

            LOGGER.info("Successfully initialized all 14 SQLite database tables.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
            throw new IllegalStateException("Database schema initialization failed", e);
        }
    }

    /**
     * Inspects the database and returns all existing table names.
     */
    public List<String> getExistingTables() {
        List<String> tables = new ArrayList<>();
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name;")) {

            while (rs.next()) {
                tables.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve existing database tables", e);
        }

        return tables;
    }
}

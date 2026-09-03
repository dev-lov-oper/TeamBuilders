package com.campus.matching.db;

import com.campus.matching.auth.PasswordUtil;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inserts realistic sample seed data into the SQLite database.
 * Runs entirely inside the Java application.
 * Uses idempotency (INSERT OR IGNORE) so running multiple times causes no duplicate entries.
 */
@Component
@Order(2)
public class DatabaseDemoDataInitializer implements ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger(DatabaseDemoDataInitializer.class.getName());

    @Override
    public void run(ApplicationArguments args) {
        seedDemoData();
    }

    public synchronized void seedDemoData() {
        LOGGER.info("Starting Java SQLite Demo Data Initialization...");
        DatabaseManager dbManager = DatabaseManager.getInstance();

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Seed Skills (10 skills)
            String[] skills = {
                "Java", "Machine Learning", "React", "Spring Boot", "Docker",
                "SQL", "TensorFlow", "C++", "Figma", "Node.js"
            };
            for (String skill : skills) {
                executeSql(conn, "INSERT OR IGNORE INTO skills (name) VALUES (?);", skill);
            }

            // 2. Seed Interests (8 interests)
            String[] interests = {
                "Artificial Intelligence", "Web Development", "Cloud Computing", "Cybersecurity",
                "Mobile Apps", "IoT & Embedded", "Data Science", "UI/UX Design"
            };
            for (String interest : interests) {
                executeSql(conn, "INSERT OR IGNORE INTO interests (name) VALUES (?);", interest);
            }

            // 3. Seed Roles (6 roles)
            String[] roles = {
                "Frontend Developer", "Backend Developer", "Full Stack Developer",
                "DevOps Engineer", "Data Scientist", "UI/UX Designer"
            };
            for (String role : roles) {
                executeSql(conn, "INSERT OR IGNORE INTO roles (name) VALUES (?);", role);
            }

            // 4. Seed Students (10 students with varied profiles)
            // Student 1: Strong skills, poor role match
            insertStudent(conn, "alex_skills_master", "alex@campus.edu", "Computer Science", 3, "INTERMEDIATE", "Heavy skill focus, minimal role alignment.");
            // Student 2: Strong role match, weak skills
            insertStudent(conn, "sam_role_specialist", "sam@campus.edu", "Software Engineering", 2, "BEGINNER", "Perfect role match for Backend, limited skills.");
            // Student 3: Strong interest match
            insertStudent(conn, "taylor_interest_fanatic", "taylor@campus.edu", "Data Science", 2, "BEGINNER", "High interest overlap in AI & Data Science.");
            // Student 4: Balanced skills, roles, and interests
            insertStudent(conn, "jordan_balanced_pro", "jordan@campus.edu", "Computer Science", 3, "INTERMEDIATE", "Balanced skills, roles, and interests across web.");
            // Student 5: Higher experience (Advanced)
            insertStudent(conn, "dr_morgan_expert", "morgan@campus.edu", "Computer Engineering", 4, "ADVANCED", "Senior student with advanced experience level.");
            // Students 6-10: Diverse complementary profiles
            insertStudent(conn, "chris_frontend_ninja", "chris@campus.edu", "Information Technology", 3, "INTERMEDIATE", "Frontend & UI/UX specialist.");
            insertStudent(conn, "pat_devops_guru", "pat@campus.edu", "Cloud Systems", 4, "ADVANCED", "DevOps & Infrastructure enthusiast.");
            insertStudent(conn, "riley_iot_hacker", "riley@campus.edu", "Electrical Engineering", 3, "INTERMEDIATE", "Embedded systems & C++ developer.");
            insertStudent(conn, "casey_ml_researcher", "casey@campus.edu", "Artificial Intelligence", 4, "ADVANCED", "Machine Learning & PyTorch researcher.");
            insertStudent(conn, "quinn_mobile_dev", "quinn@campus.edu", "Software Engineering", 1, "BEGINNER", "Mobile app developer & designer.");

            // Demo Quick Login Accounts
            insertStudent(conn, "alice_dev", "alice@campus.edu", "Computer Science", 4, "ADVANCED", "AI & ML Specialist.");
            insertStudent(conn, "bob_fe", "bob@campus.edu", "Information Technology", 3, "INTERMEDIATE", "Frontend & Web Developer.");
            insertStudent(conn, "diana_iot", "diana@campus.edu", "Electrical Engineering", 3, "INTERMEDIATE", "IoT & Embedded Systems Engineer.");
            insertStudent(conn, "evan_full", "evan@campus.edu", "Computer Science", 3, "ADVANCED", "Full Stack & EdTech Developer.");

            conn.commit();

            // 5. Seed Student-Skill / Role / Interest Relationships
            linkStudentSkill(conn, "alex_skills_master", "Java", "Machine Learning", "Docker", "SQL", "Spring Boot", "TensorFlow");
            linkStudentRole(conn, "alex_skills_master", "UI/UX Designer");
            linkStudentInterest(conn, "alex_skills_master", "Cloud Computing");

            linkStudentSkill(conn, "sam_role_specialist", "Java");
            linkStudentRole(conn, "sam_role_specialist", "Backend Developer", "Full Stack Developer");
            linkStudentInterest(conn, "sam_role_specialist", "Web Development");

            linkStudentSkill(conn, "taylor_interest_fanatic", "Machine Learning", "SQL");
            linkStudentRole(conn, "taylor_interest_fanatic", "Data Scientist");
            linkStudentInterest(conn, "taylor_interest_fanatic", "Artificial Intelligence", "Data Science", "Web Development", "Cloud Computing");

            linkStudentSkill(conn, "jordan_balanced_pro", "Java", "Spring Boot", "React", "SQL");
            linkStudentRole(conn, "jordan_balanced_pro", "Full Stack Developer", "Backend Developer", "Frontend Developer");
            linkStudentInterest(conn, "jordan_balanced_pro", "Web Development", "Cloud Computing");

            linkStudentSkill(conn, "dr_morgan_expert", "Machine Learning", "TensorFlow", "Docker", "SQL", "C++");
            linkStudentRole(conn, "dr_morgan_expert", "Data Scientist", "DevOps Engineer");
            linkStudentInterest(conn, "dr_morgan_expert", "Artificial Intelligence", "Cloud Computing");

            linkStudentSkill(conn, "chris_frontend_ninja", "React", "Figma", "Node.js");
            linkStudentRole(conn, "chris_frontend_ninja", "Frontend Developer", "UI/UX Designer");
            linkStudentInterest(conn, "chris_frontend_ninja", "UI/UX Design", "Web Development");

            linkStudentSkill(conn, "pat_devops_guru", "Docker", "Machine Learning", "SQL", "Node.js");
            linkStudentRole(conn, "pat_devops_guru", "DevOps Engineer", "Backend Developer");
            linkStudentInterest(conn, "pat_devops_guru", "Cloud Computing", "Cybersecurity");

            linkStudentSkill(conn, "riley_iot_hacker", "C++", "Machine Learning", "Docker");
            linkStudentRole(conn, "riley_iot_hacker", "Backend Developer");
            linkStudentInterest(conn, "riley_iot_hacker", "IoT & Embedded", "Cybersecurity");

            linkStudentSkill(conn, "casey_ml_researcher", "Machine Learning", "TensorFlow", "SQL");
            linkStudentRole(conn, "casey_ml_researcher", "Data Scientist");
            linkStudentInterest(conn, "casey_ml_researcher", "Artificial Intelligence", "Data Science");

            linkStudentSkill(conn, "quinn_mobile_dev", "React", "Node.js", "Figma");
            linkStudentRole(conn, "quinn_mobile_dev", "Frontend Developer");
            linkStudentInterest(conn, "quinn_mobile_dev", "Mobile Apps", "UI/UX Design");

            linkStudentSkill(conn, "alice_dev", "Machine Learning", "TensorFlow", "Java", "SQL");
            linkStudentRole(conn, "alice_dev", "Data Scientist");
            linkStudentInterest(conn, "alice_dev", "Artificial Intelligence", "Data Science");

            linkStudentSkill(conn, "bob_fe", "React", "Figma", "Node.js");
            linkStudentRole(conn, "bob_fe", "Frontend Developer", "UI/UX Designer");
            linkStudentInterest(conn, "bob_fe", "Web Development", "UI/UX Design");

            linkStudentSkill(conn, "diana_iot", "C++", "Docker", "SQL");
            linkStudentRole(conn, "diana_iot", "Backend Developer");
            linkStudentInterest(conn, "diana_iot", "IoT & Embedded", "Cybersecurity");

            linkStudentSkill(conn, "evan_full", "Java", "Spring Boot", "React", "SQL", "Docker");
            linkStudentRole(conn, "evan_full", "Full Stack Developer", "Backend Developer");
            linkStudentInterest(conn, "evan_full", "Web Development", "Cloud Computing");

            conn.commit();

            // 6. Seed Projects (4 projects)
            long owner1 = getStudentId(conn, "jordan_balanced_pro");
            long owner2 = getStudentId(conn, "dr_morgan_expert");
            long owner3 = getStudentId(conn, "riley_iot_hacker");
            long owner4 = getStudentId(conn, "chris_frontend_ninja");

            insertProject(conn, "Full Stack E-Commerce Platform", "Scalable web platform built with Java Spring Boot and React.", "WEB", 3, owner1);
            insertProject(conn, "AI Predictive Analytics Engine", "Machine learning platform for predictive data modeling.", "ML", 4, owner2);
            insertProject(conn, "Smart Campus IoT Monitor", "Embedded sensor system for real-time campus environmental monitoring.", "HARDWARE", 3, owner3);
            insertProject(conn, "Campus Event Social App", "Cross-platform mobile application for student campus events.", "OTHER", 3, owner4);

            conn.commit();

            // 7. Seed Project Requirements
            linkProjectSkill(conn, "Full Stack E-Commerce Platform", "Java", "Spring Boot", "React", "SQL");
            linkProjectRole(conn, "Full Stack E-Commerce Platform", "Backend Developer", "Frontend Developer", "Full Stack Developer");
            linkProjectInterest(conn, "Full Stack E-Commerce Platform", "Web Development", "Cloud Computing");

            linkProjectSkill(conn, "AI Predictive Analytics Engine", "Machine Learning", "TensorFlow", "SQL", "Docker");
            linkProjectRole(conn, "AI Predictive Analytics Engine", "Data Scientist", "Backend Developer", "DevOps Engineer");
            linkProjectInterest(conn, "AI Predictive Analytics Engine", "Artificial Intelligence", "Data Science");

            linkProjectSkill(conn, "Smart Campus IoT Monitor", "C++", "Machine Learning", "Docker");
            linkProjectRole(conn, "Smart Campus IoT Monitor", "Backend Developer", "DevOps Engineer");
            linkProjectInterest(conn, "Smart Campus IoT Monitor", "IoT & Embedded", "Cybersecurity");

            linkProjectSkill(conn, "Campus Event Social App", "React", "Node.js", "Figma");
            linkProjectRole(conn, "Campus Event Social App", "Frontend Developer", "UI/UX Designer");
            linkProjectInterest(conn, "Campus Event Social App", "Mobile Apps", "UI/UX Design");

            conn.commit();
            LOGGER.info("Successfully completed Java SQLite Demo Data Initialization.");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to seed Java SQLite demo data", e);
        }
    }

    private void executeSql(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param);
            pstmt.executeUpdate();
        }
    }

    private void insertStudent(Connection conn, String username, String email, String dept, int year, String exp, String bio) throws SQLException {
        String sql = """
            INSERT INTO students (username, email, password_hash, department, year, experience_level, bio)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(username) DO NOTHING;
            """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, PasswordUtil.hash("password123"));
            pstmt.setString(4, dept);
            pstmt.setInt(5, year);
            pstmt.setString(6, exp);
            pstmt.setString(7, bio);
            pstmt.executeUpdate();
        }
    }

    private void insertProject(Connection conn, String name, String desc, String type, int teamSize, long ownerId) throws SQLException {
        String checkSql = "SELECT id FROM projects WHERE name = ?;";
        try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, name);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next()) return; // Project already exists
            }
        }

        String sql = """
            INSERT INTO projects (name, description, project_type, team_size, status, created_by_id)
            VALUES (?, ?, ?, ?, 'OPEN', ?);
            """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, desc);
            pstmt.setString(3, type);
            pstmt.setInt(4, teamSize);
            pstmt.setLong(5, ownerId > 0 ? ownerId : 1);
            pstmt.executeUpdate();
        }
    }

    private long getStudentId(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM students WHERE username = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return 1L;
    }

    private long getProjectId(Connection conn, String name) throws SQLException {
        String sql = "SELECT id FROM projects WHERE name = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return 1L;
    }

    private void linkStudentSkill(Connection conn, String username, String... skills) throws SQLException {
        long studentId = getStudentId(conn, username);
        String sql = """
            INSERT OR IGNORE INTO student_skills (student_id, skill_id)
            SELECT ?, id FROM skills WHERE name = ?;
            """;
        for (String skill : skills) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, studentId);
                pstmt.setString(2, skill);
                pstmt.executeUpdate();
            }
        }
    }

    private void linkStudentRole(Connection conn, String username, String... roles) throws SQLException {
        long studentId = getStudentId(conn, username);
        String sql = """
            INSERT OR IGNORE INTO student_roles (student_id, role_id)
            SELECT ?, id FROM roles WHERE name = ?;
            """;
        for (String role : roles) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, studentId);
                pstmt.setString(2, role);
                pstmt.executeUpdate();
            }
        }
    }

    private void linkStudentInterest(Connection conn, String username, String... interests) throws SQLException {
        long studentId = getStudentId(conn, username);
        String sql = """
            INSERT OR IGNORE INTO student_interests (student_id, interest_id)
            SELECT ?, id FROM interests WHERE name = ?;
            """;
        for (String interest : interests) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, studentId);
                pstmt.setString(2, interest);
                pstmt.executeUpdate();
            }
        }
    }

    private void linkProjectSkill(Connection conn, String projectName, String... skills) throws SQLException {
        long projectId = getProjectId(conn, projectName);
        String sql = """
            INSERT OR IGNORE INTO project_skills (project_id, skill_id, required_count)
            SELECT ?, id, 1 FROM skills WHERE name = ?;
            """;
        for (String skill : skills) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, projectId);
                pstmt.setString(2, skill);
                pstmt.executeUpdate();
            }
        }
    }

    private void linkProjectRole(Connection conn, String projectName, String... roles) throws SQLException {
        long projectId = getProjectId(conn, projectName);
        String sql = """
            INSERT OR IGNORE INTO project_roles (project_id, role_id, required_count)
            SELECT ?, id, 1 FROM roles WHERE name = ?;
            """;
        for (String role : roles) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, projectId);
                pstmt.setString(2, role);
                pstmt.executeUpdate();
            }
        }
    }

    private void linkProjectInterest(Connection conn, String projectName, String... interests) throws SQLException {
        long projectId = getProjectId(conn, projectName);
        String sql = """
            INSERT OR IGNORE INTO project_interests (project_id, interest_id)
            SELECT ?, id FROM interests WHERE name = ?;
            """;
        for (String interest : interests) {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, projectId);
                pstmt.setString(2, interest);
                pstmt.executeUpdate();
            }
        }
    }
}

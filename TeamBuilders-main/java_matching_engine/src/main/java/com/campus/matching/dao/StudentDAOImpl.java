package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Interest;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;
import com.campus.matching.model.Student;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ================================
 *  DESIGN PATTERN: DATA ACCESS OBJECT (DAO) IMPLEMENTATION
 * ================================
 * Concrete implementation of StudentDAO using pure JDBC PreparedStatement queries.
 * Connects to SQLite database via DatabaseManager (Singleton).
 */
@Repository
public class StudentDAOImpl implements StudentDAO {

    private static final Logger LOGGER = Logger.getLogger(StudentDAOImpl.class.getName());

    @Override
    public Student createStudent(Student student) {
        String sql = """
            INSERT INTO students (username, email, password_hash, department, year, experience_level, bio)
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getUsername());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPasswordHash() != null ? student.getPasswordHash() : "");
            stmt.setString(4, student.getDepartment());
            stmt.setInt(5, student.getYear() > 0 ? student.getYear() : 1);
            stmt.setString(6, student.getExperienceLevel() != null ? student.getExperienceLevel() : "INTERMEDIATE");
            stmt.setString(7, student.getBio());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating student failed, no rows inserted.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    student.setId(generatedKeys.getLong(1));
                }
            }

            // Save associated skills, interests, and preferred roles
            saveSkills(conn, student.getId(), student.getSkills());
            saveInterests(conn, student.getId(), student.getInterests());
            saveRoles(conn, student.getId(), student.getPreferredRoles());

            LOGGER.info("Student created successfully with ID: " + student.getId());
            return getStudentById(student.getId()).orElse(student);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating student: " + student.getUsername(), e);
            throw new RuntimeException("Database error creating student", e);
        }
    }

    @Override
    public Optional<Student> getStudentById(Long id) {
        String sql = "SELECT id, username, email, department, year, experience_level, bio, created_at FROM students WHERE id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = mapRowToStudent(rs);
                    student.setSkills(getSkillsForStudent(conn, id));
                    student.setInterests(getInterestsForStudent(conn, id));
                    student.setPreferredRoles(getRolesForStudent(conn, id));
                    return Optional.of(student);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching student by ID: " + id, e);
            throw new RuntimeException("Database error fetching student", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Student> getAllStudents() {
        String sql = "SELECT id, username, email, department, year, experience_level, bio, created_at FROM students ORDER BY id ASC;";
        List<Student> students = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Student student = mapRowToStudent(rs);
                student.setSkills(getSkillsForStudent(conn, student.getId()));
                student.setInterests(getInterestsForStudent(conn, student.getId()));
                student.setPreferredRoles(getRolesForStudent(conn, student.getId()));
                students.add(student);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all students", e);
            throw new RuntimeException("Database error retrieving students", e);
        }

        return students;
    }

    @Override
    public Student updateStudent(Student student) {
        if (student.getId() == null) {
            throw new IllegalArgumentException("Cannot update student without an ID");
        }

        String sql = """
            UPDATE students
            SET username = ?, email = ?, department = ?, year = ?, experience_level = ?, bio = ?
            WHERE id = ?;
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, student.getUsername());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getDepartment());
            stmt.setInt(4, student.getYear() > 0 ? student.getYear() : 1);
            stmt.setString(5, student.getExperienceLevel() != null ? student.getExperienceLevel() : "INTERMEDIATE");
            stmt.setString(6, student.getBio());
            stmt.setLong(7, student.getId());

            stmt.executeUpdate();

            // Clear old join mappings and insert fresh ones
            deleteStudentJoinTables(conn, student.getId());
            saveSkills(conn, student.getId(), student.getSkills());
            saveInterests(conn, student.getId(), student.getInterests());
            saveRoles(conn, student.getId(), student.getPreferredRoles());

            LOGGER.info("Student updated successfully with ID: " + student.getId());
            return getStudentById(student.getId()).orElse(student);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating student ID: " + student.getId(), e);
            throw new RuntimeException("Database error updating student", e);
        }
    }

    @Override
    public boolean deleteStudent(Long id) {
        String sql = "DELETE FROM students WHERE id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsDeleted = stmt.executeUpdate();
            LOGGER.info("Deleted student ID: " + id + " (rows affected: " + rowsDeleted + ")");
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting student ID: " + id, e);
            throw new RuntimeException("Database error deleting student", e);
        }
    }

    // Helper: Map ResultSet row to Student POJO
    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getLong("id"));
        student.setUsername(rs.getString("username"));
        student.setEmail(rs.getString("email"));
        student.setDepartment(rs.getString("department"));
        student.setYear(rs.getInt("year"));
        student.setExperienceLevel(rs.getString("experience_level"));
        student.setBio(rs.getString("bio"));
        student.setCreatedAt(rs.getString("created_at"));
        return student;
    }

    // Helper: Delete student relationships
    private void deleteStudentJoinTables(Connection conn, Long studentId) throws SQLException {
        try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM student_skills WHERE student_id = ?;");
             PreparedStatement s2 = conn.prepareStatement("DELETE FROM student_interests WHERE student_id = ?;");
             PreparedStatement s3 = conn.prepareStatement("DELETE FROM student_roles WHERE student_id = ?;")) {

            s1.setLong(1, studentId); s1.executeUpdate();
            s2.setLong(1, studentId); s2.executeUpdate();
            s3.setLong(1, studentId); s3.executeUpdate();
        }
    }

    // Helper: Save Skills & Student-Skill relations
    private void saveSkills(Connection conn, Long studentId, List<Skill> skills) throws SQLException {
        if (skills == null || skills.isEmpty()) return;

        String insertSkillSql = "INSERT OR IGNORE INTO skills (name) VALUES (?);";
        String selectSkillSql = "SELECT id FROM skills WHERE LOWER(name) = LOWER(?);";
        String insertRelationSql = "INSERT OR IGNORE INTO student_skills (student_id, skill_id) VALUES (?, ?);";

        for (Skill skill : skills) {
            if (skill == null || skill.getName() == null || skill.getName().isBlank()) continue;

            Long skillId = skill.getId();
            if (skillId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertSkillSql)) {
                    stmt.setString(1, skill.getName().trim());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(selectSkillSql)) {
                    stmt.setString(1, skill.getName().trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) skillId = rs.getLong("id");
                    }
                }
            }

            if (skillId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRelationSql)) {
                    stmt.setLong(1, studentId);
                    stmt.setLong(2, skillId);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Helper: Save Interests & Student-Interest relations
    private void saveInterests(Connection conn, Long studentId, List<Interest> interests) throws SQLException {
        if (interests == null || interests.isEmpty()) return;

        String insertInterestSql = "INSERT OR IGNORE INTO interests (name) VALUES (?);";
        String selectInterestSql = "SELECT id FROM interests WHERE LOWER(name) = LOWER(?);";
        String insertRelationSql = "INSERT OR IGNORE INTO student_interests (student_id, interest_id) VALUES (?, ?);";

        for (Interest interest : interests) {
            if (interest == null || interest.getName() == null || interest.getName().isBlank()) continue;

            Long interestId = interest.getId();
            if (interestId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertInterestSql)) {
                    stmt.setString(1, interest.getName().trim());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(selectInterestSql)) {
                    stmt.setString(1, interest.getName().trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) interestId = rs.getLong("id");
                    }
                }
            }

            if (interestId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRelationSql)) {
                    stmt.setLong(1, studentId);
                    stmt.setLong(2, interestId);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Helper: Save Roles & Student-Role relations
    private void saveRoles(Connection conn, Long studentId, List<Role> roles) throws SQLException {
        if (roles == null || roles.isEmpty()) return;

        String insertRoleSql = "INSERT OR IGNORE INTO roles (name) VALUES (?);";
        String selectRoleSql = "SELECT id FROM roles WHERE LOWER(name) = LOWER(?);";
        String insertRelationSql = "INSERT OR IGNORE INTO student_roles (student_id, role_id) VALUES (?, ?);";

        for (Role role : roles) {
            if (role == null || role.getName() == null || role.getName().isBlank()) continue;

            Long roleId = role.getId();
            if (roleId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRoleSql)) {
                    stmt.setString(1, role.getName().trim());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(selectRoleSql)) {
                    stmt.setString(1, role.getName().trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) roleId = rs.getLong("id");
                    }
                }
            }

            if (roleId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRelationSql)) {
                    stmt.setLong(1, studentId);
                    stmt.setLong(2, roleId);
                    stmt.executeUpdate();
                }
            }
        }
    }

    // Helper: Fetch Skills for Student
    private List<Skill> getSkillsForStudent(Connection conn, Long studentId) throws SQLException {
        String sql = """
            SELECT s.id, s.name
            FROM skills s
            JOIN student_skills ss ON s.id = ss.skill_id
            WHERE ss.student_id = ?;
            """;
        List<Skill> skills = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    skills.add(new Skill(rs.getLong("id"), rs.getString("name")));
                }
            }
        }
        return skills;
    }

    // Helper: Fetch Interests for Student
    private List<Interest> getInterestsForStudent(Connection conn, Long studentId) throws SQLException {
        String sql = """
            SELECT i.id, i.name
            FROM interests i
            JOIN student_interests si ON i.id = si.interest_id
            WHERE si.student_id = ?;
            """;
        List<Interest> interests = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interests.add(new Interest(rs.getLong("id"), rs.getString("name")));
                }
            }
        }
        return interests;
    }

    // Helper: Fetch Roles for Student
    private List<Role> getRolesForStudent(Connection conn, Long studentId) throws SQLException {
        String sql = """
            SELECT r.id, r.name
            FROM roles r
            JOIN student_roles sr ON r.id = sr.role_id
            WHERE sr.student_id = ?;
            """;
        List<Role> roles = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(new Role(rs.getLong("id"), rs.getString("name")));
                }
            }
        }
        return roles;
    }

    // ============================================================
    // PUBLIC RELATIONSHIP METHODS
    // ============================================================

    @Override
    public boolean addSkillToStudent(Long studentId, Long skillId) {
        String sql = "INSERT OR IGNORE INTO student_skills (student_id, skill_id) VALUES (?, ?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, skillId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding skill ID " + skillId + " to student ID " + studentId, e);
            return false;
        }
    }

    @Override
    public boolean removeSkillFromStudent(Long studentId, Long skillId) {
        String sql = "DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, skillId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing skill ID " + skillId + " from student ID " + studentId, e);
            return false;
        }
    }

    @Override
    public List<Skill> getStudentSkills(Long studentId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return getSkillsForStudent(conn, studentId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving skills for student ID " + studentId, e);
            return List.of();
        }
    }

    @Override
    public boolean addInterestToStudent(Long studentId, Long interestId) {
        String sql = "INSERT OR IGNORE INTO student_interests (student_id, interest_id) VALUES (?, ?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, interestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding interest ID " + interestId + " to student ID " + studentId, e);
            return false;
        }
    }

    @Override
    public boolean removeInterestFromStudent(Long studentId, Long interestId) {
        String sql = "DELETE FROM student_interests WHERE student_id = ? AND interest_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, interestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing interest ID " + interestId + " from student ID " + studentId, e);
            return false;
        }
    }

    @Override
    public List<Interest> getStudentInterests(Long studentId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return getInterestsForStudent(conn, studentId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving interests for student ID " + studentId, e);
            return List.of();
        }
    }

    @Override
    public boolean addRoleToStudent(Long studentId, Long roleId) {
        String sql = "INSERT OR IGNORE INTO student_roles (student_id, role_id) VALUES (?, ?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, roleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding role ID " + roleId + " to student ID " + studentId, e);
            return false;
        }
    }

    @Override
    public boolean removeRoleFromStudent(Long studentId, Long roleId) {
        String sql = "DELETE FROM student_roles WHERE student_id = ? AND role_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            stmt.setLong(2, roleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing role ID " + roleId + " from student ID " + studentId, e);
            return false;
        }
    }

    @Override
    public List<Role> getStudentRoles(Long studentId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return getRolesForStudent(conn, studentId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving roles for student ID " + studentId, e);
            return List.of();
        }
    }
    @Override
    public Optional<Student> authenticate(String username, String passwordHash) {
        String sql = "SELECT id, username, email, department, year, experience_level, bio, created_at FROM students WHERE username = ? AND password_hash = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student(rs.getLong("id"), rs.getString("username"), rs.getString("email"),
                            rs.getString("department"), rs.getInt("year"), rs.getString("experience_level"), rs.getString("bio"));
                    student.setCreatedAt(rs.getString("created_at"));
                    student.setSkills(getSkillsForStudent(conn, student.getId()));
                    student.setInterests(getInterestsForStudent(conn, student.getId()));
                    student.setPreferredRoles(getRolesForStudent(conn, student.getId()));
                    return Optional.of(student);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication database error for user: " + username, e);
            throw new RuntimeException("Database error during authentication", e);
        }
        return Optional.empty();
    }

    @Override
    public Student updateProfile(Student student) {
        String sql = "UPDATE students SET department = ?, year = ?, experience_level = ?, bio = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getDepartment());
            stmt.setInt(2, student.getYear());
            stmt.setString(3, student.getExperienceLevel());
            stmt.setString(4, student.getBio());
            stmt.setLong(5, student.getId());
            stmt.executeUpdate();
            return getStudentById(student.getId()).orElse(student);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating profile ID: " + student.getId(), e);
            throw new RuntimeException("Database error updating profile", e);
        }
    }

}

package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Interest;
import com.campus.matching.model.Project;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete JDBC implementation of ProjectDAO.
 * Manages project data persistence and requirements across SQLite tables.
 */
@Repository
public class ProjectDAOImpl implements ProjectDAO {

    private static final Logger LOGGER = Logger.getLogger(ProjectDAOImpl.class.getName());

    @Override
    public Project createProject(Project project) {
        String sql = """
                INSERT INTO projects (name, description, project_type, team_size, status, created_by_id)
                VALUES (?, ?, ?, ?, ?, ?);
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription() != null ? project.getDescription() : "");
            stmt.setString(3, project.getProjectType() != null ? project.getProjectType() : "WEB");
            stmt.setInt(4, project.getTeamSize() > 0 ? project.getTeamSize() : 3);
            stmt.setString(5, project.getStatus() != null ? project.getStatus() : "OPEN");
            stmt.setLong(6, project.getCreatedById() != null ? project.getCreatedById() : 1L);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating project failed, no rows inserted.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    project.setId(rs.getLong(1));
                }
            }

            // Save requirements
            saveProjectSkills(conn, project.getId(), project.getRequiredSkills(), project.getRequiredSkillCounts());
            saveProjectRoles(conn, project.getId(), project.getRequiredRoles(), project.getRequiredRoleCounts());
            saveProjectInterests(conn, project.getId(), project.getInterests());

            LOGGER.info("Project created successfully with ID: " + project.getId());
            return getProjectById(project.getId()).orElse(project);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating project: " + project.getName(), e);
            throw new RuntimeException("Database error creating project", e);
        }
    }

    @Override
    public Optional<Project> getProjectById(Long id) {
        String sql = """
                SELECT p.id, p.name, p.description, p.project_type, p.team_size, p.status, p.created_by_id, p.created_at,
                       s.username AS created_by_username
                FROM projects p
                LEFT JOIN students s ON p.created_by_id = s.id
                WHERE p.id = ?;
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Project project = mapRowToProject(rs);
                    loadProjectRequirements(conn, project);
                    return Optional.of(project);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching project ID: " + id, e);
            throw new RuntimeException("Database error fetching project", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Project> getAllProjects() {
        String sql = """
                SELECT p.id, p.name, p.description, p.project_type, p.team_size, p.status, p.created_by_id, p.created_at,
                       s.username AS created_by_username
                FROM projects p
                LEFT JOIN students s ON p.created_by_id = s.id
                ORDER BY p.id DESC;
                """;
        List<Project> projects = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Project project = mapRowToProject(rs);
                loadProjectRequirements(conn, project);
                projects.add(project);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing projects", e);
            throw new RuntimeException("Database error listing projects", e);
        }

        return projects;
    }

    @Override
    public Project updateProject(Project project) {
        if (project.getId() == null) {
            throw new IllegalArgumentException("Cannot update project without an ID");
        }

        String sql = """
                UPDATE projects
                SET name = ?, description = ?, project_type = ?, team_size = ?, status = ?, created_by_id = ?
                WHERE id = ?;
                """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, project.getName());
            stmt.setString(2, project.getDescription());
            stmt.setString(3, project.getProjectType() != null ? project.getProjectType() : "WEB");
            stmt.setInt(4, project.getTeamSize() > 0 ? project.getTeamSize() : 3);
            stmt.setString(5, project.getStatus() != null ? project.getStatus() : "OPEN");
            stmt.setLong(6, project.getCreatedById());
            stmt.setLong(7, project.getId());

            stmt.executeUpdate();

            // Clear old requirements & insert updated ones only if requirements exist in
            // payload
            boolean hasSkills = project.getRequiredSkills() != null && !project.getRequiredSkills().isEmpty();
            boolean hasRoles = project.getRequiredRoles() != null && !project.getRequiredRoles().isEmpty();
            boolean hasInterests = project.getInterests() != null && !project.getInterests().isEmpty();

            if (hasSkills || hasRoles || hasInterests) {
                clearProjectRequirements(conn, project.getId());
                saveProjectSkills(conn, project.getId(), project.getRequiredSkills(), project.getRequiredSkillCounts());
                saveProjectRoles(conn, project.getId(), project.getRequiredRoles(), project.getRequiredRoleCounts());
                saveProjectInterests(conn, project.getId(), project.getInterests());
            }

            LOGGER.info("Updated project ID: " + project.getId());
            return getProjectById(project.getId()).orElse(project);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating project ID: " + project.getId(), e);
            throw new RuntimeException("Database error updating project", e);
        }
    }

    @Override
    public boolean deleteProject(Long id) {
        String sql = "DELETE FROM projects WHERE id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsDeleted = stmt.executeUpdate();
            LOGGER.info("Deleted project ID: " + id + " (rows affected: " + rowsDeleted + ")");
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting project ID: " + id, e);
            throw new RuntimeException("Database error deleting project", e);
        }
    }

    // ============================================================
    // REQUIREMENT METHODS
    // ============================================================

    @Override
    public boolean addProjectSkill(Long projectId, Long skillId, int requiredCount) {
        String sql = "INSERT INTO project_skills (project_id, skill_id, required_count) VALUES (?, ?, ?) ON CONFLICT(project_id, skill_id) DO UPDATE SET required_count = excluded.required_count;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            stmt.setLong(2, skillId);
            stmt.setInt(3, Math.max(1, requiredCount));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding skill ID " + skillId + " to project ID " + projectId, e);
            return false;
        }
    }

    @Override
    public boolean removeProjectSkill(Long projectId, Long skillId) {
        String sql = "DELETE FROM project_skills WHERE project_id = ? AND skill_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            stmt.setLong(2, skillId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing skill ID " + skillId + " from project ID " + projectId, e);
            return false;
        }
    }

    @Override
    public List<Skill> getProjectSkills(Long projectId) {
        String sql = """
                SELECT s.id, s.name
                FROM skills s
                JOIN project_skills ps ON s.id = ps.skill_id
                WHERE ps.project_id = ?;
                """;
        List<Skill> skills = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    skills.add(new Skill(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving skills for project ID " + projectId, e);
        }
        return skills;
    }

    @Override
    public boolean addProjectRole(Long projectId, Long roleId, int requiredCount) {
        String sql = "INSERT INTO project_roles (project_id, role_id, required_count) VALUES (?, ?, ?) ON CONFLICT(project_id, role_id) DO UPDATE SET required_count = excluded.required_count;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            stmt.setLong(2, roleId);
            stmt.setInt(3, Math.max(1, requiredCount));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding role ID " + roleId + " to project ID " + projectId, e);
            return false;
        }
    }

    @Override
    public boolean removeProjectRole(Long projectId, Long roleId) {
        String sql = "DELETE FROM project_roles WHERE project_id = ? AND role_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            stmt.setLong(2, roleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing role ID " + roleId + " from project ID " + projectId, e);
            return false;
        }
    }

    @Override
    public List<Role> getProjectRoles(Long projectId) {
        String sql = """
                SELECT r.id, r.name
                FROM roles r
                JOIN project_roles pr ON r.id = pr.role_id
                WHERE pr.project_id = ?;
                """;
        List<Role> roles = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roles.add(new Role(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving roles for project ID " + projectId, e);
        }
        return roles;
    }

    @Override
    public boolean addProjectInterest(Long projectId, Long interestId) {
        String sql = "INSERT OR IGNORE INTO project_interests (project_id, interest_id) VALUES (?, ?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            stmt.setLong(2, interestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding interest ID " + interestId + " to project ID " + projectId, e);
            return false;
        }
    }

    @Override
    public boolean removeProjectInterest(Long projectId, Long interestId) {
        String sql = "DELETE FROM project_interests WHERE project_id = ? AND interest_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            stmt.setLong(2, interestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing interest ID " + interestId + " from project ID " + projectId, e);
            return false;
        }
    }

    @Override
    public List<Interest> getProjectInterests(Long projectId) {
        String sql = """
                SELECT i.id, i.name
                FROM interests i
                JOIN project_interests pi ON i.id = pi.interest_id
                WHERE pi.project_id = ?;
                """;
        List<Interest> interests = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interests.add(new Interest(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving interests for project ID " + projectId, e);
        }
        return interests;
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private Project mapRowToProject(ResultSet rs) throws SQLException {
        Project project = new Project();
        project.setId(rs.getLong("id"));
        project.setName(rs.getString("name"));
        project.setDescription(rs.getString("description"));
        project.setProjectType(rs.getString("project_type"));
        project.setTeamSize(rs.getInt("team_size"));
        project.setStatus(rs.getString("status"));
        project.setCreatedById(rs.getLong("created_by_id"));
        project.setCreatedByUsername(rs.getString("created_by_username"));
        project.setCreatedAt(rs.getString("created_at"));
        return project;
    }

    private void loadProjectRequirements(Connection conn, Project project) throws SQLException {
        // Load skills & counts
        String skillSql = """
                SELECT s.id, s.name, ps.required_count
                FROM skills s
                JOIN project_skills ps ON s.id = ps.skill_id
                WHERE ps.project_id = ?;
                """;
        List<Skill> skills = new ArrayList<>();
        Map<String, Integer> skillCounts = new LinkedHashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(skillSql)) {
            stmt.setLong(1, project.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Skill s = new Skill(rs.getLong("id"), rs.getString("name"));
                    skills.add(s);
                    skillCounts.put(s.getName(), rs.getInt("required_count"));
                }
            }
        }
        project.setRequiredSkills(skills);
        project.setRequiredSkillCounts(skillCounts);

        // Load roles & counts
        String roleSql = """
                SELECT r.id, r.name, pr.required_count
                FROM roles r
                JOIN project_roles pr ON r.id = pr.role_id
                WHERE pr.project_id = ?;
                """;
        List<Role> roles = new ArrayList<>();
        Map<String, Integer> roleCounts = new LinkedHashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(roleSql)) {
            stmt.setLong(1, project.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Role r = new Role(rs.getLong("id"), rs.getString("name"));
                    roles.add(r);
                    roleCounts.put(r.getName(), rs.getInt("required_count"));
                }
            }
        }
        project.setRequiredRoles(roles);
        project.setRequiredRoleCounts(roleCounts);

        // Load interests
        String interestSql = """
                SELECT i.id, i.name
                FROM interests i
                JOIN project_interests pi ON i.id = pi.interest_id
                WHERE pi.project_id = ?;
                """;
        List<Interest> interests = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(interestSql)) {
            stmt.setLong(1, project.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interests.add(new Interest(rs.getLong("id"), rs.getString("name")));
                }
            }
        }
        project.setInterests(interests);
    }

    private void clearProjectRequirements(Connection conn, Long projectId) throws SQLException {
        try (PreparedStatement s1 = conn.prepareStatement("DELETE FROM project_skills WHERE project_id = ?;");
                PreparedStatement s2 = conn.prepareStatement("DELETE FROM project_roles WHERE project_id = ?;");
                PreparedStatement s3 = conn.prepareStatement("DELETE FROM project_interests WHERE project_id = ?;")) {

            s1.setLong(1, projectId);
            s1.executeUpdate();
            s2.setLong(1, projectId);
            s2.executeUpdate();
            s3.setLong(1, projectId);
            s3.executeUpdate();
        }
    }

    private void saveProjectSkills(Connection conn, Long projectId, List<Skill> skills, Map<String, Integer> counts)
            throws SQLException {
        if (skills == null || skills.isEmpty())
            return;

        String insertSkill = "INSERT OR IGNORE INTO skills (name) VALUES (?);";
        String selectSkill = "SELECT id FROM skills WHERE LOWER(name) = LOWER(?);";
        String insertRelation = "INSERT INTO project_skills (project_id, skill_id, required_count) VALUES (?, ?, ?) ON CONFLICT(project_id, skill_id) DO UPDATE SET required_count = excluded.required_count;";

        for (Skill skill : skills) {
            if (skill == null || skill.getName() == null || skill.getName().isBlank())
                continue;

            Long skillId = skill.getId();
            if (skillId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertSkill)) {
                    stmt.setString(1, skill.getName().trim());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(selectSkill)) {
                    stmt.setString(1, skill.getName().trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next())
                            skillId = rs.getLong("id");
                    }
                }
            }

            if (skillId != null) {
                int reqCount = (counts != null && counts.containsKey(skill.getName())) ? counts.get(skill.getName())
                        : 1;
                try (PreparedStatement stmt = conn.prepareStatement(insertRelation)) {
                    stmt.setLong(1, projectId);
                    stmt.setLong(2, skillId);
                    stmt.setInt(3, Math.max(1, reqCount));
                    stmt.executeUpdate();
                }
            }
        }
    }

    private void saveProjectRoles(Connection conn, Long projectId, List<Role> roles, Map<String, Integer> counts)
            throws SQLException {
        if (roles == null || roles.isEmpty())
            return;

        String insertRole = "INSERT OR IGNORE INTO roles (name) VALUES (?);";
        String selectRole = "SELECT id FROM roles WHERE LOWER(name) = LOWER(?);";
        String insertRelation = "INSERT INTO project_roles (project_id, role_id, required_count) VALUES (?, ?, ?) ON CONFLICT(project_id, role_id) DO UPDATE SET required_count = excluded.required_count;";

        for (Role role : roles) {
            if (role == null || role.getName() == null || role.getName().isBlank())
                continue;

            Long roleId = role.getId();
            if (roleId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRole)) {
                    stmt.setString(1, role.getName().trim());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(selectRole)) {
                    stmt.setString(1, role.getName().trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next())
                            roleId = rs.getLong("id");
                    }
                }
            }

            if (roleId != null) {
                int reqCount = (counts != null && counts.containsKey(role.getName())) ? counts.get(role.getName()) : 1;
                try (PreparedStatement stmt = conn.prepareStatement(insertRelation)) {
                    stmt.setLong(1, projectId);
                    stmt.setLong(2, roleId);
                    stmt.setInt(3, Math.max(1, reqCount));
                    stmt.executeUpdate();
                }
            }
        }
    }

    private void saveProjectInterests(Connection conn, Long projectId, List<Interest> interests) throws SQLException {
        if (interests == null || interests.isEmpty())
            return;

        String insertInterest = "INSERT OR IGNORE INTO interests (name) VALUES (?);";
        String selectInterest = "SELECT id FROM interests WHERE LOWER(name) = LOWER(?);";
        String insertRelation = "INSERT OR IGNORE INTO project_interests (project_id, interest_id) VALUES (?, ?);";

        for (Interest interest : interests) {
            if (interest == null || interest.getName() == null || interest.getName().isBlank())
                continue;

            Long interestId = interest.getId();
            if (interestId == null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertInterest)) {
                    stmt.setString(1, interest.getName().trim());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement(selectInterest)) {
                    stmt.setString(1, interest.getName().trim());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next())
                            interestId = rs.getLong("id");
                    }
                }
            }

            if (interestId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(insertRelation)) {
                    stmt.setLong(1, projectId);
                    stmt.setLong(2, interestId);
                    stmt.executeUpdate();
                }
            }
        }
    }
}

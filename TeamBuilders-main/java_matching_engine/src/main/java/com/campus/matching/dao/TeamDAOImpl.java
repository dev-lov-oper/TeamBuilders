package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Team;
import com.campus.matching.model.TeamMember;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete JDBC implementation of TeamDAO.
 * Enforces business rules (team size limits, duplicate prevention, valid entities)
 * and utilizes transactional blocks for multi-operation database tasks.
 */
@Repository
public class TeamDAOImpl implements TeamDAO {

    private static final Logger LOGGER = Logger.getLogger(TeamDAOImpl.class.getName());

    @Override
    public Team createTeam(Team team) {
        if (team.getProjectId() == null) {
            throw new IllegalArgumentException("Team must belong to a valid project ID");
        }

        String insertTeamSql = "INSERT INTO teams (project_id, name, is_finalized) VALUES (?, ?, ?);";
        String insertMemberSql = "INSERT INTO team_members (team_id, student_id, role_id, assigned_role_name) VALUES (?, ?, ?, ?);";

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin JDBC Transaction

            // Rule 5: Verify project exists and fetch team size limit
            int maxTeamSize = validateProjectExists(conn, team.getProjectId());

            // Validate member count if initial members provided
            if (team.getMembers() != null && team.getMembers().size() > maxTeamSize) {
                throw new IllegalStateException("Initial member count (" + team.getMembers().size() + ") exceeds project team size limit (" + maxTeamSize + ").");
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertTeamSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, team.getProjectId());
                stmt.setString(2, team.getName() != null ? team.getName().trim() : "Project Team");
                stmt.setBoolean(3, team.isFinalized());
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        team.setId(rs.getLong(1));
                    }
                }
            }

            // Insert initial members if provided
            if (team.getMembers() != null && !team.getMembers().isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(insertMemberSql)) {
                    for (TeamMember m : team.getMembers()) {
                        validateStudentExists(conn, m.getStudentId()); // Rule 4
                        stmt.setLong(1, team.getId());
                        stmt.setLong(2, m.getStudentId());
                        if (m.getRoleId() != null) stmt.setLong(3, m.getRoleId()); else stmt.setNull(3, Types.INTEGER);
                        stmt.setString(4, m.getAssignedRoleName());
                        stmt.executeUpdate();
                    }
                }
            }

            // Synchronize project status if team is finalized
            if (team.isFinalized()) {
                updateProjectStatus(conn, team.getProjectId(), "CLOSED");
            }

            conn.commit(); // Commit Transaction
            LOGGER.info("Successfully created team ID: " + team.getId() + " under project ID: " + team.getProjectId());
            return getTeamById(team.getId()).orElse(team);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback Transaction on Error
                    LOGGER.warning("Rolled back team creation transaction for project ID: " + team.getProjectId());
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Failed to rollback transaction", rollbackEx);
                }
            }
            LOGGER.log(Level.SEVERE, "Error creating team", e);
            throw new RuntimeException("Database error creating team: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    LOGGER.log(Level.SEVERE, "Failed to close connection", closeEx);
                }
            }
        }
    }

    @Override
    public Optional<Team> getTeamById(Long id) {
        String sql = """
            SELECT t.id, t.project_id, t.name, t.is_finalized, t.created_at, p.name AS project_name, p.created_by_id AS project_created_by
            FROM teams t
            LEFT JOIN projects p ON t.project_id = p.id
            WHERE t.id = ?;
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Team team = mapRowToTeam(rs);
                    team.setMembers(getTeamMembers(conn, team.getId()));
                    return Optional.of(team);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching team ID: " + id, e);
            throw new RuntimeException("Database error fetching team", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Team> getTeamByProjectId(Long projectId) {
        String sql = """
            SELECT t.id, t.project_id, t.name, t.is_finalized, t.created_at, p.name AS project_name, p.created_by_id AS project_created_by
            FROM teams t
            LEFT JOIN projects p ON t.project_id = p.id
            WHERE t.project_id = ?;
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Team team = mapRowToTeam(rs);
                    team.setMembers(getTeamMembers(conn, team.getId()));
                    return Optional.of(team);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching team by project ID: " + projectId, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Team> getAllTeams() {
        String sql = """
            SELECT t.id, t.project_id, t.name, t.is_finalized, t.created_at, p.name AS project_name, p.created_by_id AS project_created_by
            FROM teams t
            LEFT JOIN projects p ON t.project_id = p.id
            ORDER BY t.id DESC;
            """;
        List<Team> teams = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Team team = mapRowToTeam(rs);
                team.setMembers(getTeamMembers(conn, team.getId()));
                teams.add(team);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all teams", e);
            throw new RuntimeException("Database error retrieving teams", e);
        }

        return teams;
    }

    @Override
    public Team updateTeam(Team team) {
        if (team.getId() == null) {
            throw new IllegalArgumentException("Cannot update team without an ID");
        }

        String sql = "UPDATE teams SET name = ?, is_finalized = ? WHERE id = ?;";

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, team.getName().trim());
                stmt.setBoolean(2, team.isFinalized());
                stmt.setLong(3, team.getId());
                stmt.executeUpdate();
            }

            // Sync project status based on is_finalized
            if (team.getProjectId() != null) {
                updateProjectStatus(conn, team.getProjectId(), team.isFinalized() ? "CLOSED" : "OPEN");
            }

            conn.commit();
            LOGGER.info("Updated team ID: " + team.getId());
            return getTeamById(team.getId()).orElse(team);

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error updating team ID: " + team.getId(), e);
            throw new RuntimeException("Database error updating team", e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Close failed", ex); }
            }
        }
    }

    @Override
    public boolean deleteTeam(Long id) {
        String sql = "DELETE FROM teams WHERE id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int rowsDeleted = stmt.executeUpdate();
            LOGGER.info("Deleted team ID: " + id + " (rows affected: " + rowsDeleted + ")");
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting team ID: " + id, e);
            throw new RuntimeException("Database error deleting team", e);
        }
    }

    // ============================================================
    // MEMBERSHIP METHODS & BUSINESS RULE ENFORCEMENT
    // ============================================================

    @Override
    public boolean addMemberToTeam(Long teamId, Long studentId, Long roleId, String assignedRoleName) {
        String sql = "INSERT INTO team_members (team_id, student_id, role_id, assigned_role_name) VALUES (?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            // Rule 5 & 2: Fetch project ID and team size limit
            Long projectId = getProjectIdForTeam(conn, teamId);
            int maxTeamSize = validateProjectExists(conn, projectId);

            // Rule 4: Verify student exists
            validateStudentExists(conn, studentId);

            // Rule 1 & 3: Check if student is already in this team
            if (isStudentInTeam(conn, teamId, studentId)) {
                throw new IllegalStateException("Student ID " + studentId + " is already a member of this team.");
            }

            // Rule 2: Check team size limit
            int currentMemberCount = getMemberCount(conn, teamId);
            if (currentMemberCount >= maxTeamSize) {
                throw new IllegalStateException("Team size limit (" + maxTeamSize + ") reached for project ID " + projectId + ".");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, teamId);
                stmt.setLong(2, studentId);
                if (roleId != null) stmt.setLong(3, roleId); else stmt.setNull(3, Types.INTEGER);
                stmt.setString(4, assignedRoleName != null ? assignedRoleName.trim() : "Team Member");
                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding member to team ID: " + teamId, e);
            throw new RuntimeException("Database error adding team member: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeMemberFromTeam(Long teamId, Long studentId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND student_id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teamId);
            stmt.setLong(2, studentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing member ID " + studentId + " from team ID " + teamId, e);
            return false;
        }
    }

    @Override
    public List<TeamMember> getTeamMembers(Long teamId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return getTeamMembers(conn, teamId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving team members for team ID: " + teamId, e);
            return List.of();
        }
    }

    // ============================================================
    // PRIVATE VALIDATION & HELPER METHODS
    // ============================================================

    private Team mapRowToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getLong("id"));
        team.setProjectId(rs.getLong("project_id"));
        team.setProjectName(rs.getString("project_name"));
        team.setProjectCreatedBy(rs.getLong("project_created_by"));
        team.setName(rs.getString("name"));
        team.setFinalized(rs.getBoolean("is_finalized"));
        team.setCreatedAt(rs.getString("created_at"));
        return team;
    }

    private List<TeamMember> getTeamMembers(Connection conn, Long teamId) throws SQLException {
        String sql = """
            SELECT tm.id, tm.team_id, tm.student_id, tm.role_id, tm.assigned_role_name, tm.joined_at,
                   s.username AS student_name
            FROM team_members tm
            JOIN students s ON tm.student_id = s.id
            WHERE tm.team_id = ?;
            """;
        List<TeamMember> members = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new TeamMember(
                        rs.getLong("id"),
                        rs.getLong("team_id"),
                        rs.getLong("student_id"),
                        rs.getString("student_name"),
                        rs.getObject("role_id") != null ? rs.getLong("role_id") : null,
                        rs.getString("assigned_role_name"),
                        rs.getString("joined_at")
                    ));
                }
            }
        }
        return members;
    }

    private int validateProjectExists(Connection conn, Long projectId) throws SQLException {
        String sql = "SELECT team_size FROM projects WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("team_size");
                }
            }
        }
        throw new IllegalArgumentException("Project with ID " + projectId + " does not exist.");
    }

    private void validateStudentExists(Connection conn, Long studentId) throws SQLException {
        String sql = "SELECT id FROM students WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Student with ID " + studentId + " does not exist.");
                }
            }
        }
    }

    private boolean isStudentInTeam(Connection conn, Long teamId, Long studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ? AND student_id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            stmt.setLong(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private int getMemberCount(Connection conn, Long teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Long getProjectIdForTeam(Connection conn, Long teamId) throws SQLException {
        String sql = "SELECT project_id FROM teams WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("project_id");
                }
            }
        }
        throw new IllegalArgumentException("Team with ID " + teamId + " does not exist.");
    }

    private void updateProjectStatus(Connection conn, Long projectId, String status) throws SQLException {
        String sql = "UPDATE projects SET status = ? WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, projectId);
            stmt.executeUpdate();
        }
    }
    @Override
    public boolean updateMemberRole(Long teamId, Long studentId, String roleName) {
        String sql = "UPDATE team_members SET assigned_role_name = ? WHERE team_id = ? AND student_id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            stmt.setLong(2, teamId);
            stmt.setLong(3, studentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating member role", e);
            return false;
        }
    }

}

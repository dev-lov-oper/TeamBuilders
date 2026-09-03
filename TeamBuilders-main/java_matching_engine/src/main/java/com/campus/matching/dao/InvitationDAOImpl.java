package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Invitation;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Concrete JDBC implementation of InvitationDAO.
 * Enforces business rules (duplicate pending check, student/project validation)
 * and uses JDBC transactions to atomically link invitation acceptance with team membership.
 */
@Repository
public class InvitationDAOImpl implements InvitationDAO {

    private static final Logger LOGGER = Logger.getLogger(InvitationDAOImpl.class.getName());

    @Override
    public Invitation createInvitation(Invitation invitation) {
        if (invitation.getProjectId() == null || invitation.getReceiverId() == null) {
            throw new IllegalArgumentException("Invitation requires a valid project ID and receiver ID.");
        }

        String sql = "INSERT INTO invitations (project_id, sender_id, receiver_id, role_name, status) VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {

            // Rule 3: Validate project exists
            validateProjectExists(conn, invitation.getProjectId());

            // Rule 2: Validate receiver (and sender if provided) exist
            validateStudentExists(conn, invitation.getReceiverId());
            if (invitation.getSenderId() != null) {
                validateStudentExists(conn, invitation.getSenderId());
            }

            // Rule 1: Prevent duplicate pending invitations
            if (hasPendingInvitation(conn, invitation.getProjectId(), invitation.getReceiverId())) {
                throw new IllegalStateException("A pending invitation already exists for student ID " + invitation.getReceiverId() + " on project ID " + invitation.getProjectId() + ".");
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, invitation.getProjectId());
                if (invitation.getSenderId() != null) stmt.setLong(2, invitation.getSenderId()); else stmt.setNull(2, Types.INTEGER);
                stmt.setLong(3, invitation.getReceiverId());
                stmt.setString(4, invitation.getRoleName() != null ? invitation.getRoleName().trim() : "Team Member");
                stmt.setString(5, invitation.getStatus() != null ? invitation.getStatus() : "PENDING");
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        invitation.setId(rs.getLong(1));
                    }
                }
            }

            LOGGER.info("Created invitation ID: " + invitation.getId() + " for project ID: " + invitation.getProjectId());
            return getInvitationById(invitation.getId()).orElse(invitation);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating invitation", e);
            throw new RuntimeException("Database error creating invitation: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Invitation> getInvitationById(Long id) {
        String sql = """
            SELECT i.id, i.project_id, i.sender_id, i.receiver_id, i.role_name, i.status, i.created_at,
                   p.name AS project_name,
                   s1.username AS sender_name,
                   s2.username AS receiver_name
            FROM invitations i
            LEFT JOIN projects p ON i.project_id = p.id
            LEFT JOIN students s1 ON i.sender_id = s1.id
            LEFT JOIN students s2 ON i.receiver_id = s2.id
            WHERE i.id = ?;
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToInvitation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching invitation ID: " + id, e);
            throw new RuntimeException("Database error fetching invitation", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Invitation> getAllInvitations() {
        String sql = """
            SELECT i.id, i.project_id, i.sender_id, i.receiver_id, i.role_name, i.status, i.created_at,
                   p.name AS project_name,
                   s1.username AS sender_name,
                   s2.username AS receiver_name
            FROM invitations i
            LEFT JOIN projects p ON i.project_id = p.id
            LEFT JOIN students s1 ON i.sender_id = s1.id
            LEFT JOIN students s2 ON i.receiver_id = s2.id
            ORDER BY i.id DESC;
            """;
        List<Invitation> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToInvitation(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing all invitations", e);
            throw new RuntimeException("Database error listing invitations", e);
        }

        return list;
    }

    @Override
    public List<Invitation> getInvitationsForStudent(Long studentId) {
        String sql = """
            SELECT i.id, i.project_id, i.sender_id, i.receiver_id, i.role_name, i.status, i.created_at,
                   p.name AS project_name,
                   s1.username AS sender_name,
                   s2.username AS receiver_name
            FROM invitations i
            LEFT JOIN projects p ON i.project_id = p.id
            LEFT JOIN students s1 ON i.sender_id = s1.id
            LEFT JOIN students s2 ON i.receiver_id = s2.id
            WHERE i.receiver_id = ? OR i.sender_id = ?
            ORDER BY i.id DESC;
            """;
        List<Invitation> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToInvitation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching invitations for student ID: " + studentId, e);
        }

        return list;
    }

    @Override
    public List<Invitation> getInvitationsForProject(Long projectId) {
        String sql = """
            SELECT i.id, i.project_id, i.sender_id, i.receiver_id, i.role_name, i.status, i.created_at,
                   p.name AS project_name,
                   s1.username AS sender_name,
                   s2.username AS receiver_name
            FROM invitations i
            LEFT JOIN projects p ON i.project_id = p.id
            LEFT JOIN students s1 ON i.sender_id = s1.id
            LEFT JOIN students s2 ON i.receiver_id = s2.id
            WHERE i.project_id = ?
            ORDER BY i.id DESC;
            """;
        List<Invitation> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToInvitation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching invitations for project ID: " + projectId, e);
        }

        return list;
    }

    @Override
    public Invitation updateInvitation(Invitation invitation) {
        String sql = "UPDATE invitations SET status = ? WHERE id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, invitation.getStatus());
            stmt.setLong(2, invitation.getId());
            stmt.executeUpdate();

            return getInvitationById(invitation.getId()).orElse(invitation);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating invitation ID: " + invitation.getId(), e);
            throw new RuntimeException("Database error updating invitation", e);
        }
    }

    @Override
    public boolean respondToInvitation(Long invitationId, String status) {
        String newStatus = status != null ? status.toUpperCase().trim() : "REJECTED";

        Optional<Invitation> invOpt = getInvitationById(invitationId);
        if (invOpt.isEmpty()) {
            throw new IllegalArgumentException("Invitation ID " + invitationId + " does not exist.");
        }
        Invitation invitation = invOpt.get();

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin Transaction

            // 1. Update Invitation Status
            String updateSql = "UPDATE invitations SET status = ? WHERE id = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, newStatus);
                stmt.setLong(2, invitationId);
                stmt.executeUpdate();
            }

            // Rule 4: If ACCEPTED, add student to team atomically
            if ("ACCEPTED".equalsIgnoreCase(newStatus)) {
                Long teamId = getOrCreateTeamForProject(conn, invitation.getProjectId(), invitation.getProjectName());

                // Insert into team_members if not already present
                String addMemberSql = "INSERT OR IGNORE INTO team_members (team_id, student_id, assigned_role_name) VALUES (?, ?, ?);";
                try (PreparedStatement stmt = conn.prepareStatement(addMemberSql)) {
                    stmt.setLong(1, teamId);
                    stmt.setLong(2, invitation.getReceiverId());
                    stmt.setString(3, invitation.getRoleName() != null ? invitation.getRoleName() : "Team Member");
                    stmt.executeUpdate();
                }
            }

            conn.commit(); // Commit Transaction
            LOGGER.info("Responded to invitation ID: " + invitationId + " with status: " + newStatus);
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error responding to invitation ID: " + invitationId, e);
            throw new RuntimeException("Database error responding to invitation: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Close failed", ex); }
            }
        }
    }

    @Override
    public boolean deleteInvitation(Long id) {
        String sql = "DELETE FROM invitations WHERE id = ?;";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting invitation ID: " + id, e);
            return false;
        }
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private Invitation mapRowToInvitation(ResultSet rs) throws SQLException {
        Invitation invitation = new Invitation();
        invitation.setId(rs.getLong("id"));
        invitation.setProjectId(rs.getLong("project_id"));
        invitation.setProjectName(rs.getString("project_name"));
        invitation.setSenderId(rs.getObject("sender_id") != null ? rs.getLong("sender_id") : null);
        invitation.setSenderName(rs.getString("sender_name"));
        invitation.setReceiverId(rs.getLong("receiver_id"));
        invitation.setReceiverName(rs.getString("receiver_name"));
        invitation.setRoleName(rs.getString("role_name"));
        invitation.setStatus(rs.getString("status"));
        invitation.setCreatedAt(rs.getString("created_at"));
        return invitation;
    }

    private void validateProjectExists(Connection conn, Long projectId) throws SQLException {
        String sql = "SELECT id FROM projects WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Project with ID " + projectId + " does not exist.");
                }
            }
        }
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

    private boolean hasPendingInvitation(Connection conn, Long projectId, Long receiverId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invitations WHERE project_id = ? AND receiver_id = ? AND status = 'PENDING';";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, projectId);
            stmt.setLong(2, receiverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Long getOrCreateTeamForProject(Connection conn, Long projectId, String projectName) throws SQLException {
        String selectTeam = "SELECT id FROM teams WHERE project_id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(selectTeam)) {
            stmt.setLong(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        // Create new team if none exists
        String insertTeam = "INSERT INTO teams (project_id, name, is_finalized) VALUES (?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(insertTeam, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, projectId);
            stmt.setString(2, (projectName != null ? projectName : "Project") + " Team");
            stmt.setBoolean(3, false);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to create team for project ID: " + projectId);
    }
}

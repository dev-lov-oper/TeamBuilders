package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Role;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class RoleDAOImpl implements RoleDAO {

    private static final Logger LOGGER = Logger.getLogger(RoleDAOImpl.class.getName());

    @Override
    public Role createRole(Role role) {
        String sql = "INSERT INTO roles (name) VALUES (?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, role.getName().trim());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    role.setId(rs.getLong(1));
                }
            }
            return getRoleById(role.getId()).orElse(role);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating role: " + role.getName(), e);
            throw new RuntimeException("Database error creating role", e);
        }
    }

    @Override
    public Optional<Role> getRoleById(Long id) {
        String sql = "SELECT id, name FROM roles WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Role(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching role ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Role> getRoleByName(String name) {
        String sql = "SELECT id, name FROM roles WHERE LOWER(name) = LOWER(?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Role(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching role by name: " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Role> getAllRoles() {
        String sql = "SELECT id, name FROM roles ORDER BY name ASC;";
        List<Role> roles = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                roles.add(new Role(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing roles", e);
        }
        return roles;
    }

    @Override
    public Role updateRole(Role role) {
        String sql = "UPDATE roles SET name = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.getName().trim());
            stmt.setLong(2, role.getId());
            stmt.executeUpdate();
            return getRoleById(role.getId()).orElse(role);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating role ID: " + role.getId(), e);
            throw new RuntimeException("Database error updating role", e);
        }
    }

    @Override
    public boolean deleteRole(Long id) {
        String sql = "DELETE FROM roles WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting role ID: " + id, e);
            return false;
        }
    }
}

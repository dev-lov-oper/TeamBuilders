package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Interest;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class InterestDAOImpl implements InterestDAO {

    private static final Logger LOGGER = Logger.getLogger(InterestDAOImpl.class.getName());

    @Override
    public Interest createInterest(Interest interest) {
        String sql = "INSERT INTO interests (name) VALUES (?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, interest.getName().trim());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    interest.setId(rs.getLong(1));
                }
            }
            return getInterestById(interest.getId()).orElse(interest);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating interest: " + interest.getName(), e);
            throw new RuntimeException("Database error creating interest", e);
        }
    }

    @Override
    public Optional<Interest> getInterestById(Long id) {
        String sql = "SELECT id, name FROM interests WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Interest(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching interest ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Interest> getInterestByName(String name) {
        String sql = "SELECT id, name FROM interests WHERE LOWER(name) = LOWER(?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Interest(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching interest by name: " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Interest> getAllInterests() {
        String sql = "SELECT id, name FROM interests ORDER BY name ASC;";
        List<Interest> interests = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                interests.add(new Interest(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing interests", e);
        }
        return interests;
    }

    @Override
    public Interest updateInterest(Interest interest) {
        String sql = "UPDATE interests SET name = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, interest.getName().trim());
            stmt.setLong(2, interest.getId());
            stmt.executeUpdate();
            return getInterestById(interest.getId()).orElse(interest);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating interest ID: " + interest.getId(), e);
            throw new RuntimeException("Database error updating interest", e);
        }
    }

    @Override
    public boolean deleteInterest(Long id) {
        String sql = "DELETE FROM interests WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting interest ID: " + id, e);
            return false;
        }
    }
}

package com.campus.matching.dao;

import com.campus.matching.db.DatabaseManager;
import com.campus.matching.model.Skill;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class SkillDAOImpl implements SkillDAO {

    private static final Logger LOGGER = Logger.getLogger(SkillDAOImpl.class.getName());

    @Override
    public Skill createSkill(Skill skill) {
        String sql = "INSERT INTO skills (name) VALUES (?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, skill.getName().trim());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    skill.setId(rs.getLong(1));
                }
            }
            return getSkillById(skill.getId()).orElse(skill);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating skill: " + skill.getName(), e);
            throw new RuntimeException("Database error creating skill", e);
        }
    }

    @Override
    public Optional<Skill> getSkillById(Long id) {
        String sql = "SELECT id, name FROM skills WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Skill(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching skill ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Skill> getSkillByName(String name) {
        String sql = "SELECT id, name FROM skills WHERE LOWER(name) = LOWER(?);";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Skill(rs.getLong("id"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching skill by name: " + name, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Skill> getAllSkills() {
        String sql = "SELECT id, name FROM skills ORDER BY name ASC;";
        List<Skill> skills = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                skills.add(new Skill(rs.getLong("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing skills", e);
        }
        return skills;
    }

    @Override
    public Skill updateSkill(Skill skill) {
        String sql = "UPDATE skills SET name = ? WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, skill.getName().trim());
            stmt.setLong(2, skill.getId());
            stmt.executeUpdate();
            return getSkillById(skill.getId()).orElse(skill);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating skill ID: " + skill.getId(), e);
            throw new RuntimeException("Database error updating skill", e);
        }
    }

    @Override
    public boolean deleteSkill(Long id) {
        String sql = "DELETE FROM skills WHERE id = ?;";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting skill ID: " + id, e);
            return false;
        }
    }
}

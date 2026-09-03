package com.campus.matching.dao;

import com.campus.matching.model.Team;
import com.campus.matching.model.TeamMember;

import java.util.List;
import java.util.Optional;

/**
 * ================================
 *  DESIGN PATTERN: DATA ACCESS OBJECT (DAO)
 * ================================
 * Interface defining CRUD, membership, and transaction operations for Team entities.
 */
public interface TeamDAO {

    Team createTeam(Team team);

    Optional<Team> getTeamById(Long id);

    Optional<Team> getTeamByProjectId(Long projectId);

    List<Team> getAllTeams();

    Team updateTeam(Team team);

    boolean deleteTeam(Long id);

    // ============================================================
    // MEMBERSHIP METHODS (team_members)
    // ============================================================

    boolean addMemberToTeam(Long teamId, Long studentId, Long roleId, String assignedRoleName);

    boolean removeMemberFromTeam(Long teamId, Long studentId);
    boolean updateMemberRole(Long teamId, Long studentId, String roleName);

    List<TeamMember> getTeamMembers(Long teamId);
}

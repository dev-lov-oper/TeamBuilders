package com.campus.matching.controller;

import com.campus.matching.dao.TeamDAO;
import com.campus.matching.dao.ProjectDAO;
import com.campus.matching.model.Team;
import com.campus.matching.model.TeamMember;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller exposing HTTP endpoints for Team CRUD, membership, and finalization.
 * Delegates database persistence and business rule checks to TeamDAO.
 */
@RestController
@RequestMapping("/api/teams")
@CrossOrigin
public class TeamController {

    private final TeamDAO teamDAO;
    private final ProjectDAO projectDAO;

    public TeamController(TeamDAO teamDAO, ProjectDAO projectDAO) {
        this.teamDAO = teamDAO;
        this.projectDAO = projectDAO;
    }

    /**
     * POST /api/teams - Create a new team under a project.
     */
    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        if (team == null || team.getProjectId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Team created = teamDAO.createTeam(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/teams - List all teams.
     */
    @GetMapping
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamDAO.getAllTeams());
    }

    /**
     * GET /api/teams/{id} - Get team by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Optional<Team> teamOpt = teamDAO.getTeamById(id);
        return teamOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/teams/project/{projectId} - Get team by Project ID.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Team> getTeamByProjectId(@PathVariable Long projectId) {
        Optional<Team> teamOpt = teamDAO.getTeamByProjectId(projectId);
        return teamOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/teams/{id} - Update team settings.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody Team team) {
        if (team == null) {
            return ResponseEntity.badRequest().build();
        }
        if (teamDAO.getTeamById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        team.setId(id);
        return ResponseEntity.ok(teamDAO.updateTeam(team));
    }

    /**
     * DELETE /api/teams/{id} - Delete team by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        boolean deleted = teamDAO.deleteTeam(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * POST /api/teams/{id}/members - Add a student member to a team.
     */
    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMemberToTeam(@PathVariable Long id, @RequestBody TeamMember member) {
        if (member == null || member.getStudentId() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean added = teamDAO.addMemberToTeam(id, member.getStudentId(), member.getRoleId(), member.getAssignedRoleName());
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * DELETE /api/teams/{id}/members/{studentId} - Remove a student from a team.
     */
    @DeleteMapping("/{id}/members/{studentId}")
    public ResponseEntity<Void> removeMemberFromTeam(@PathVariable Long id, @PathVariable Long studentId) {
        boolean removed = teamDAO.removeMemberFromTeam(id, studentId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * POST /api/teams/{id}/finalize - Finalize a team.
     */
    @PutMapping("/{id}/members/{studentId}/role")
    public ResponseEntity<Void> updateMemberRole(@PathVariable Long id, @PathVariable Long studentId, @RequestBody java.util.Map<String,String> body) {
        String roleName = body.getOrDefault("role_name", "Team Member");
        return teamDAO.updateMemberRole(id, studentId, roleName) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/unfinalize")
    public ResponseEntity<Team> unfinalizeTeam(@PathVariable Long id) {
        Optional<Team> teamOpt = teamDAO.getTeamById(id);
        if (teamOpt.isEmpty()) return ResponseEntity.notFound().build();
        Team team = teamOpt.get();
        team.setFinalized(false);
        return ResponseEntity.ok(teamDAO.updateTeam(team));
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<Team> finalizeTeam(@PathVariable Long id) {
        Optional<Team> teamOpt = teamDAO.getTeamById(id);
        if (teamOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Team team = teamOpt.get();
        team.setFinalized(true);
        Team updated = teamDAO.updateTeam(team);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/form/{projectId}")
    public ResponseEntity<Team> formTeam(@PathVariable Long projectId, @RequestBody java.util.Map<String,Object> body) {
        com.campus.matching.model.Project project = projectDAO.getProjectById(projectId)
                .orElseThrow(() -> new com.campus.matching.exception.ResourceNotFoundException("Project not found."));
        if ("CLOSED".equalsIgnoreCase(project.getStatus())) {
            throw new com.campus.matching.exception.BadRequestException("Project is closed.");
        }
        Team team = new Team();
        team.setProjectId(projectId);
        team.setName(body.get("team_name") == null ? "Project Team" : String.valueOf(body.get("team_name")));
        team.setFinalized(Boolean.parseBoolean(String.valueOf(body.getOrDefault("finalize", "false"))));
        Object rawMembers = body.get("members");
        if (rawMembers instanceof java.util.List<?> list) {
            for (Object raw : list) {
                if (raw instanceof java.util.Map<?,?> m && m.get("student_id") != null) {
                    TeamMember member = new TeamMember();
                    member.setStudentId(Long.valueOf(String.valueOf(m.get("student_id"))));
                    member.setAssignedRoleName(m.get("role_name") == null ? "Team Member" : String.valueOf(m.get("role_name")));
                    team.getMembers().add(member);
                }
            }
        }
        Team created = teamDAO.createTeam(team);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

}

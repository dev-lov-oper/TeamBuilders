package com.campus.matching.controller;

import com.campus.matching.dao.InterestDAO;
import com.campus.matching.dao.ProjectDAO;
import com.campus.matching.dao.SkillDAO;
import com.campus.matching.dao.RoleDAO;
import com.campus.matching.model.Interest;
import com.campus.matching.model.Project;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller exposing HTTP CRUD and requirement management endpoints for Projects.
 * Delegates database persistence tasks to ProjectDAO (DAO pattern).
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin
public class ProjectController {

    private final ProjectDAO projectDAO;
    private final SkillDAO skillDAO;
    private final RoleDAO roleDAO;
    private final InterestDAO interestDAO;

    public ProjectController(ProjectDAO projectDAO, SkillDAO skillDAO, RoleDAO roleDAO, InterestDAO interestDAO) {
        this.projectDAO = projectDAO;
        this.skillDAO = skillDAO;
        this.roleDAO = roleDAO;
        this.interestDAO = interestDAO;
    }

    /**
     * POST /api/projects - Create a new project.
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project, HttpSession session) {
        if (project == null || project.getName() == null || project.getName().isBlank()) {
            throw new com.campus.matching.exception.BadRequestException("Project name is required.");
        }
        if (project.getTeamSize() <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid team size: " + project.getTeamSize() + ". Must be at least 1.");
        }
        Object userId = session.getAttribute("teamBuildersUserId");
        if (!(userId instanceof Number)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        project.setCreatedById(((Number) userId).longValue());
        Project created = projectDAO.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/projects - List all projects.
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectDAO.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/{id} - Get project by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid project ID: " + id);
        }
        Optional<Project> projectOpt = projectDAO.getProjectById(id);
        return projectOpt.map(ResponseEntity::ok)
                .orElseThrow(() -> new com.campus.matching.exception.ResourceNotFoundException("Project with ID " + id + " does not exist."));
    }

    /**
     * PUT /api/projects/{id} - Update project by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        if (id == null || id <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid project ID: " + id);
        }
        if (project == null) {
            throw new com.campus.matching.exception.BadRequestException("Project payload is required.");
        }
        if (project.getTeamSize() <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid team size: " + project.getTeamSize());
        }

        if (projectDAO.getProjectById(id).isEmpty()) {
            throw new com.campus.matching.exception.ResourceNotFoundException("Project with ID " + id + " does not exist.");
        }

        Project existing = projectDAO.getProjectById(id).orElseThrow(() -> new com.campus.matching.exception.ResourceNotFoundException("Project not found."));
        project.setId(id);
        project.setCreatedById(existing.getCreatedById());
        if (project.getStatus() == null) project.setStatus(existing.getStatus());
        if (project.getProjectType() == null) project.setProjectType(existing.getProjectType());

        if (project.getRequiredSkills() == null || project.getRequiredSkills().isEmpty()) {
            project.setRequiredSkills(existing.getRequiredSkills());
            project.setRequiredSkillCounts(existing.getRequiredSkillCounts());
        }
        if (project.getRequiredRoles() == null || project.getRequiredRoles().isEmpty()) {
            project.setRequiredRoles(existing.getRequiredRoles());
            project.setRequiredRoleCounts(existing.getRequiredRoleCounts());
        }
        if (project.getInterests() == null || project.getInterests().isEmpty()) {
            project.setInterests(existing.getInterests());
        }

        Project updated = projectDAO.updateProject(project);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/projects/{id} - Delete project by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new com.campus.matching.exception.BadRequestException("Invalid project ID: " + id);
        }
        boolean deleted = projectDAO.deleteProject(id);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        throw new com.campus.matching.exception.ResourceNotFoundException("Project with ID " + id + " does not exist.");
    }

    // ============================================================
    // REQUIREMENT ENDPOINTS (project_skills, project_roles, project_interests)
    // ============================================================

    @PostMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> addProjectSkill(@PathVariable Long id, @PathVariable Long skillId, @RequestParam(defaultValue = "1") int count) {
        boolean added = projectDAO.addProjectSkill(id, skillId, count);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Void> removeProjectSkill(@PathVariable Long id, @PathVariable Long skillId) {
        boolean removed = projectDAO.removeProjectSkill(id, skillId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<Skill>> getProjectSkills(@PathVariable Long id) {
        return ResponseEntity.ok(projectDAO.getProjectSkills(id));
    }

    @PostMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Void> addProjectRole(@PathVariable Long id, @PathVariable Long roleId, @RequestParam(defaultValue = "1") int count) {
        boolean added = projectDAO.addProjectRole(id, roleId, count);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Void> removeProjectRole(@PathVariable Long id, @PathVariable Long roleId) {
        boolean removed = projectDAO.removeProjectRole(id, roleId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Role>> getProjectRoles(@PathVariable Long id) {
        return ResponseEntity.ok(projectDAO.getProjectRoles(id));
    }

    @PostMapping("/{id}/interests/{interestId}")
    public ResponseEntity<Void> addProjectInterest(@PathVariable Long id, @PathVariable Long interestId) {
        boolean added = projectDAO.addProjectInterest(id, interestId);
        return added ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}/interests/{interestId}")
    public ResponseEntity<Void> removeProjectInterest(@PathVariable Long id, @PathVariable Long interestId) {
        boolean removed = projectDAO.removeProjectInterest(id, interestId);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/interests")
    public ResponseEntity<List<Interest>> getProjectInterests(@PathVariable Long id) {
        return ResponseEntity.ok(projectDAO.getProjectInterests(id));
    }
    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Project> toggleStatus(@PathVariable Long id) {
        Project project = projectDAO.getProjectById(id).orElseThrow(() -> new com.campus.matching.exception.ResourceNotFoundException("Project not found."));
        project.setStatus("OPEN".equalsIgnoreCase(project.getStatus()) ? "CLOSED" : "OPEN");
        return ResponseEntity.ok(projectDAO.updateProject(project));
    }

    @PostMapping("/{id}/skills/by-name")
    public ResponseEntity<Void> addSkillByName(@PathVariable Long id, @RequestBody java.util.Map<String,Object> body) {
        String name = body.get("name") == null ? "" : String.valueOf(body.get("name")).trim();
        int count = body.get("required_count") == null ? 1 : Integer.parseInt(String.valueOf(body.get("required_count")));
        if (name.isBlank()) return ResponseEntity.badRequest().build();
        Skill skill = skillDAO.getSkillByName(name).orElseGet(() -> skillDAO.createSkill(new Skill(null, name)));
        return projectDAO.addProjectSkill(id, skill.getId(), count) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/roles/by-name")
    public ResponseEntity<Void> addRoleByName(@PathVariable Long id, @RequestBody java.util.Map<String,Object> body) {
        String name = body.get("name") == null ? "" : String.valueOf(body.get("name")).trim();
        int count = body.get("required_count") == null ? 1 : Integer.parseInt(String.valueOf(body.get("required_count")));
        if (name.isBlank()) return ResponseEntity.badRequest().build();
        Role role = roleDAO.getRoleByName(name).orElseGet(() -> roleDAO.createRole(new Role(null, name)));
        return projectDAO.addProjectRole(id, role.getId(), count) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/interests/by-name")
    public ResponseEntity<Void> addInterestByName(@PathVariable Long id, @RequestBody java.util.Map<String,Object> body) {
        String name = body.get("name") == null ? "" : String.valueOf(body.get("name")).trim();
        if (name.isBlank()) return ResponseEntity.badRequest().build();
        Interest interest = interestDAO.getInterestByName(name).orElseGet(() -> interestDAO.createInterest(new Interest(null, name)));
        return projectDAO.addProjectInterest(id, interest.getId()) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

}

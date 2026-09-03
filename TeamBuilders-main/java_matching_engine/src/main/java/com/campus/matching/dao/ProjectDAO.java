package com.campus.matching.dao;

import com.campus.matching.model.Interest;
import com.campus.matching.model.Project;
import com.campus.matching.model.Role;
import com.campus.matching.model.Skill;

import java.util.List;
import java.util.Optional;

/**
 * ================================
 *  DESIGN PATTERN: DATA ACCESS OBJECT (DAO)
 * ================================
 * Interface defining CRUD and requirement management operations for Project entities.
 */
public interface ProjectDAO {

    Project createProject(Project project);

    Optional<Project> getProjectById(Long id);

    List<Project> getAllProjects();

    Project updateProject(Project project);

    boolean deleteProject(Long id);

    // ============================================================
    // PROJECT REQUIREMENT METHODS (project_skills, project_roles, project_interests)
    // ============================================================

    boolean addProjectSkill(Long projectId, Long skillId, int requiredCount);

    boolean removeProjectSkill(Long projectId, Long skillId);

    List<Skill> getProjectSkills(Long projectId);

    boolean addProjectRole(Long projectId, Long roleId, int requiredCount);

    boolean removeProjectRole(Long projectId, Long roleId);

    List<Role> getProjectRoles(Long projectId);

    boolean addProjectInterest(Long projectId, Long interestId);

    boolean removeProjectInterest(Long projectId, Long interestId);

    List<Interest> getProjectInterests(Long projectId);
}

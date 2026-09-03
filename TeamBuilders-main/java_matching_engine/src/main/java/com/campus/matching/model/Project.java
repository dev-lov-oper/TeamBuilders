package com.campus.matching.model;

import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Model representing a campus project.
 * Maps to SQLite table 'projects'.
 */
public class Project {

    private Long id;
    private String name;
    private String description;
    private String projectType = "WEB";
    private int teamSize = 3;
    private String status = "OPEN";
    private Long createdById;
    private String createdByUsername;
    private String createdAt;

    // Relational Requirements & Interests
    private List<Skill> requiredSkills = new ArrayList<>();
    private Map<String, Integer> requiredSkillCounts = new LinkedHashMap<>();
    private List<Role> requiredRoles = new ArrayList<>();
    private Map<String, Integer> requiredRoleCounts = new LinkedHashMap<>();
    private List<Interest> interests = new ArrayList<>();

    public Project() {
    }

    public Project(Long id, String name, String description, String projectType, int teamSize, String status, Long createdById) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.projectType = projectType != null ? projectType : "WEB";
        this.teamSize = teamSize;
        this.status = status != null ? status : "OPEN";
        this.createdById = createdById;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("project_type")
    public String getProjectTypeSnake() { return projectType; }

    @JsonProperty("team_size")
    public int getTeamSizeSnake() { return teamSize; }

    public String getProjectType() {
        return projectType;
    }

    @JsonAlias("project_type")
    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public int getTeamSize() {
        return teamSize;
    }

    @JsonAlias("team_size")
    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("created_by")
    public Long getCreatedBy() { return createdById; }

    @JsonProperty("created_by_name")
    public String getCreatedByName() { return createdByUsername; }

    @JsonProperty("skill_requirements")
    public List<Map<String, Object>> getSkillRequirements() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Skill skill : requiredSkills) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", skill.getId());
            item.put("skill_name", skill.getName());
            item.put("required_count", requiredSkillCounts.getOrDefault(skill.getName(), 1));
            result.add(item);
        }
        return result;
    }

    @JsonProperty("role_requirements")
    public List<Map<String, Object>> getRoleRequirements() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Role role : requiredRoles) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", role.getId());
            item.put("role_name", role.getName());
            item.put("required_count", requiredRoleCounts.getOrDefault(role.getName(), 1));
            result.add(item);
        }
        return result;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<Skill> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
    }

    public Map<String, Integer> getRequiredSkillCounts() {
        return requiredSkillCounts;
    }

    public void setRequiredSkillCounts(Map<String, Integer> requiredSkillCounts) {
        this.requiredSkillCounts = requiredSkillCounts != null ? requiredSkillCounts : new LinkedHashMap<>();
    }

    public List<Role> getRequiredRoles() {
        return requiredRoles;
    }

    public void setRequiredRoles(List<Role> requiredRoles) {
        this.requiredRoles = requiredRoles != null ? requiredRoles : new ArrayList<>();
    }

    public Map<String, Integer> getRequiredRoleCounts() {
        return requiredRoleCounts;
    }

    public void setRequiredRoleCounts(Map<String, Integer> requiredRoleCounts) {
        this.requiredRoleCounts = requiredRoleCounts != null ? requiredRoleCounts : new LinkedHashMap<>();
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    /**
     * Converts this POJO into ProjectData record format for matching engine processing.
     */
    public ProjectData toProjectData() {
        List<String> skillNames = requiredSkills.stream().map(Skill::getName).collect(Collectors.toList());
        List<String> roleNames = requiredRoles.stream().map(Role::getName).collect(Collectors.toList());
        List<String> interestNames = interests.stream().map(Interest::getName).collect(Collectors.toList());

        return new ProjectData(
            id,
            name,
            projectType,
            teamSize,
            skillNames,
            roleNames,
            interestNames,
            requiredSkillCounts,
            requiredRoleCounts
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name='" + name + "', projectType='" + projectType + "', teamSize=" + teamSize + '}';
    }
}

package com.campus.matching.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing a project team.
 * Maps to SQLite table 'teams'.
 */
public class Team {

    private Long id;
    private Long projectId;
    private String projectName;
    private String name;
    private boolean isFinalized;
    private String createdAt;
    private Long projectCreatedBy;

    private List<TeamMember> members = new ArrayList<>();

    public Team() {
    }

    public Team(Long projectId, String name, boolean isFinalized) {
        this.projectId = projectId;
        this.name = name;
        this.isFinalized = isFinalized;
    }

    public Team(Long id, Long projectId, String name, boolean isFinalized, String createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.isFinalized = isFinalized;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("project_created_by")
    public Long getProjectCreatedBy() { return projectCreatedBy; }

    public void setProjectCreatedBy(Long projectCreatedBy) { this.projectCreatedBy = projectCreatedBy; }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("project_name")
    public String getProjectNameSnake() { return projectName; }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("is_finalized")
    public boolean getIsFinalized() { return isFinalized; }

    public boolean isFinalized() {
        return isFinalized;
    }

    public void setFinalized(boolean finalized) {
        isFinalized = finalized;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{id=" + id + ", projectId=" + projectId + ", name='" + name + "', isFinalized=" + isFinalized + '}';
    }
}

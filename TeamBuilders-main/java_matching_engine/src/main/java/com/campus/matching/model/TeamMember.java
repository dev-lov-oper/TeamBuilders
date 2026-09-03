package com.campus.matching.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Model representing a student membership within a project team.
 * Maps to SQLite table 'team_members'.
 */
public class TeamMember {

    private Long id;
    private Long teamId;
    private Long studentId;
    private String studentName;
    private Long roleId;
    private String assignedRoleName;
    private String joinedAt;

    public TeamMember() {
    }

    public TeamMember(Long teamId, Long studentId, Long roleId, String assignedRoleName) {
        this.teamId = teamId;
        this.studentId = studentId;
        this.roleId = roleId;
        this.assignedRoleName = assignedRoleName;
    }

    public TeamMember(Long id, Long teamId, Long studentId, String studentName, Long roleId, String assignedRoleName, String joinedAt) {
        this.id = id;
        this.teamId = teamId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.roleId = roleId;
        this.assignedRoleName = assignedRoleName;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    @JsonProperty("student")
    public Long getStudent() { return studentId; }

    @JsonProperty("role_name")
    public String getRoleName() { return assignedRoleName; }

    public Long getStudentId() {
        return studentId;
    }

    @JsonAlias("student_id")
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    @JsonProperty("student_name")
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getAssignedRoleName() {
        return assignedRoleName;
    }

    @JsonAlias("role_name")
    public void setAssignedRoleName(String assignedRoleName) {
        this.assignedRoleName = assignedRoleName;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(teamId, that.teamId) && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, studentId);
    }

    @Override
    public String toString() {
        return "TeamMember{teamId=" + teamId + ", studentId=" + studentId + ", assignedRoleName='" + assignedRoleName + "'}";
    }
}

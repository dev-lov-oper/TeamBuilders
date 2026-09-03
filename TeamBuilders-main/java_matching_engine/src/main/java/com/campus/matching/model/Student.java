package com.campus.matching.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing a student user in the campus team building system.
 * Maps to SQLite table 'students'.
 */
public class Student {

    private Long id;
    private String username;
    private String email;
    private String department;
    private int year = 1;
    private String experienceLevel = "INTERMEDIATE";
    private String bio;
    private String createdAt;

    @JsonIgnore
    private String passwordHash;

    // Relational collections
    private List<Skill> skills = new ArrayList<>();
    private List<Interest> interests = new ArrayList<>();
    private List<Role> preferredRoles = new ArrayList<>();

    public Student() {
    }

    public Student(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Student(Long id, String username, String email, String department, int year, String experienceLevel, String bio) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.department = department;
        this.year = year;
        this.experienceLevel = experienceLevel != null ? experienceLevel : "INTERMEDIATE";
        this.bio = bio;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @JsonProperty("experience_level")
    public String getExperienceLevelSnake() { return experienceLevel; }

    @JsonProperty("preferred_roles")
    public List<Role> getPreferredRolesSnake() { return preferredRoles; }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    @JsonAlias("experience_level")
    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();
    }

    public List<Interest> getInterests() {
        return interests;
    }

    public void setInterests(List<Interest> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public List<Role> getPreferredRoles() {
        return preferredRoles;
    }

    public void setPreferredRoles(List<Role> preferredRoles) {
        this.preferredRoles = preferredRoles != null ? preferredRoles : new ArrayList<>();
    }

    /**
     * Converts this POJO into StudentData record format for matching engine processing.
     */
    public StudentData toStudentData() {
        List<String> skillNames = skills.stream().map(Skill::getName).collect(Collectors.toList());
        List<String> interestNames = interests.stream().map(Interest::getName).collect(Collectors.toList());
        List<String> roleNames = preferredRoles.stream().map(Role::getName).collect(Collectors.toList());

        return new StudentData(
            id,
            username,
            skillNames,
            interestNames,
            roleNames,
            experienceLevel
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(id, student.id) || Objects.equals(username, student.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", username='" + username + "', department='" + department + "'}";
    }
}

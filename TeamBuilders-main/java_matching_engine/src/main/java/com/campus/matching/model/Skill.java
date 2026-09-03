package com.campus.matching.model;

import java.util.Objects;

/**
 * Model representing a student or project skill.
 * Maps to SQLite table 'skills'.
 */
public class Skill {

    private Long id;
    private String name;

    public Skill() {
    }

    public Skill(String name) {
        this.name = name;
    }

    public Skill(Long id, String name) {
        this.id = id;
        this.name = name;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(id, skill.id) ||
               (name != null && name.equalsIgnoreCase(skill.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name != null ? name.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return "Skill{id=" + id + ", name='" + name + "'}";
    }
}

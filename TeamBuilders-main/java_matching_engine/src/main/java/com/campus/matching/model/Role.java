package com.campus.matching.model;

import java.util.Objects;

/**
 * Model representing a preferred student role or required project position.
 * Maps to SQLite table 'roles'.
 */
public class Role {

    private Long id;
    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(Long id, String name) {
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
        Role role = (Role) o;
        return Objects.equals(id, role.id) ||
               (name != null && name.equalsIgnoreCase(role.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name != null ? name.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "'}";
    }
}

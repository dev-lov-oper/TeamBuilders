package com.campus.matching.dao;

import com.campus.matching.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleDAO {
    Role createRole(Role role);
    Optional<Role> getRoleById(Long id);
    Optional<Role> getRoleByName(String name);
    List<Role> getAllRoles();
    Role updateRole(Role role);
    boolean deleteRole(Long id);
}

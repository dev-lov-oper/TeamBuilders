package com.campus.matching.bridge;

import java.util.List;

/**
 * ================================
 *  DESIGN PATTERN: BRIDGE
 * ================================
 * This is the "Abstraction" side of the Bridge pattern.
 *
 * StudentRoleBridge does not care HOW a role is stored - it only
 * talks to the ProjectRole interface. That's the whole point of
 * Bridge: the "what a role means to the app" (this class) is kept
 * separate from "how a role is represented" (ConcreteProjectRole).
 *
 * This class also does the actual job of deciding which role a
 * student should be assigned within a team:
 *   1. Prefer a role the student has that the project also needs.
 *   2. Otherwise, use the student's first listed role.
 *   3. Otherwise, use the project's first required role.
 *   4. Otherwise, fall back to a default hint role.
 *   5. Otherwise, just call them "Team Member".
 */
public class StudentRoleBridge {

    private final ProjectRole projectRole;

    public StudentRoleBridge(ProjectRole projectRole) {
        this.projectRole = projectRole;
    }

    public String roleName() {
        return projectRole != null ? projectRole.name() : "Team Member";
    }

    public static String resolveAssignedRole(List<String> studentRoles, List<String> projectRoles, String defaultHintRole) {

        // 1. Try to find a role that BOTH the student and the project have.
        if (projectRoles != null && !projectRoles.isEmpty()) {
            for (String projectRoleName : projectRoles) {
                if (studentHasRole(studentRoles, projectRoleName)) {
                    return wrap(projectRoleName);
                }
            }
        }

        // 2. No shared role found - just use the student's first role, if any.
        if (studentRoles != null && !studentRoles.isEmpty()) {
            return wrap(studentRoles.get(0));
        }

        // 3. Student listed no roles - fall back to the project's first role.
        if (projectRoles != null && !projectRoles.isEmpty()) {
            return wrap(projectRoles.get(0));
        }

        // 4. Nothing from student or project - use the given default hint.
        if (defaultHintRole != null && !defaultHintRole.isBlank()) {
            return wrap(defaultHintRole);
        }

        // 5. Absolute last resort.
        return wrap("Team Member");
    }

    // Small helper that builds a bridge around a plain role name
    // and reads the name back out through the Bridge.
    private static String wrap(String roleName) {
        return new StudentRoleBridge(new ConcreteProjectRole(roleName)).roleName();
    }

    private static boolean studentHasRole(List<String> studentRoles, String roleName) {
        if (studentRoles == null) {
            return false;
        }
        for (String role : studentRoles) {
            if (role.equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }
}

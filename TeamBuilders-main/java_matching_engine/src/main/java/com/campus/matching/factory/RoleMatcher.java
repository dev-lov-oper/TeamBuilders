package com.campus.matching.factory;

import com.campus.matching.model.ProjectData;
import com.campus.matching.model.StudentData;

import java.util.List;
import java.util.Map;

/**
 * FACTORY METHOD PATTERN - one of the concrete "Products".
 *
 * Scores a student on how well their roles (e.g. "Frontend Developer")
 * match the roles the project needs.
 *
 * Score = (required roles covered) / (required roles) * 100
 */
public class RoleMatcher implements Matcher {

    @Override
    public double score(ProjectData project, StudentData student) {
        Map<String, Integer> requiredRoleCounts = project.requiredRoleCounts();
        List<String> requiredRoles = project.requiredRoles();
        List<String> studentRoles = student.roles();

        boolean noRequirementsAtAll =
            (requiredRoleCounts == null || requiredRoleCounts.isEmpty())
                && (requiredRoles == null || requiredRoles.isEmpty());

        if (noRequirementsAtAll) {
            return 100.0;
        }

        if (studentRoles == null || studentRoles.isEmpty()) {
            return 0.0;
        }

        // Case 1: project specifies how MANY people are needed per role.
        if (requiredRoleCounts != null && !requiredRoleCounts.isEmpty()) {
            int requiredSlots = 0;
            int filledSlots = 0;

            for (Map.Entry<String, Integer> entry : requiredRoleCounts.entrySet()) {
                String roleName = entry.getKey();
                int slotsNeeded = Math.max(1, entry.getValue());
                requiredSlots += slotsNeeded;

                if (studentHasRole(studentRoles, roleName)) {
                    filledSlots += 1;
                }
            }

            return requiredSlots == 0 ? 100.0 : (100.0 * filledSlots) / requiredSlots;
        }

        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return 100.0;
        }

        // Case 2: project just lists required roles, no counts.
        int matchedCount = 0;
        for (String requiredRole : requiredRoles) {
            if (studentHasRole(studentRoles, requiredRole)) {
                matchedCount++;
            }
        }

        return (100.0 * matchedCount) / requiredRoles.size();
    }

    private boolean studentHasRole(List<String> studentRoles, String roleName) {
        for (String role : studentRoles) {
            if (role.equalsIgnoreCase(roleName)) {
                return true;
            }
        }
        return false;
    }
}

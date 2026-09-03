package com.campus.matching.factory;

import com.campus.matching.model.ProjectData;
import com.campus.matching.model.StudentData;

import java.util.List;
import java.util.Map;

/**
 * FACTORY METHOD PATTERN - one of the concrete "Products".
 *
 * Scores a student on how many of the project's required skills
 * they have.
 *
 * Score = (skills the student has) / (skills the project needs) * 100
 */
public class SkillMatcher implements Matcher {

    @Override
    public double score(ProjectData project, StudentData student) {
        Map<String, Integer> requiredSkillCounts = project.requiredSkillCounts();
        List<String> requiredSkills = project.requiredSkills();
        List<String> studentSkills = student.skills();

        boolean noRequirementsAtAll =
            (requiredSkillCounts == null || requiredSkillCounts.isEmpty())
                && (requiredSkills == null || requiredSkills.isEmpty());

        if (noRequirementsAtAll) {
            // Project doesn't ask for any specific skill -> everyone qualifies.
            return 100.0;
        }

        if (studentSkills == null || studentSkills.isEmpty()) {
            // Project needs skills but student listed none.
            return 0.0;
        }

        // Case 1: project specifies how MANY people are needed per skill.
        if (requiredSkillCounts != null && !requiredSkillCounts.isEmpty()) {
            int requiredSlots = 0;
            int filledSlots = 0;

            for (Map.Entry<String, Integer> entry : requiredSkillCounts.entrySet()) {
                String skillName = entry.getKey();
                int slotsNeeded = Math.max(1, entry.getValue());
                requiredSlots += slotsNeeded;

                if (studentHasSkill(studentSkills, skillName)) {
                    filledSlots += 1;
                }
            }

            return requiredSlots == 0 ? 100.0 : (100.0 * filledSlots) / requiredSlots;
        }

        // Case 2: project just lists required skills, no counts.
        int matchedCount = 0;
        for (String requiredSkill : requiredSkills) {
            if (studentHasSkill(studentSkills, requiredSkill)) {
                matchedCount++;
            }
        }

        return (100.0 * matchedCount) / requiredSkills.size();
    }

    // Helper: checks if the student's skill list contains this skill
    // (case-insensitive).
    private boolean studentHasSkill(List<String> studentSkills, String skillName) {
        for (String skill : studentSkills) {
            if (skill.equalsIgnoreCase(skillName)) {
                return true;
            }
        }
        return false;
    }
}

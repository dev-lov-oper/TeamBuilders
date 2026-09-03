package com.campus.matching.factory;

import com.campus.matching.abstractfactory.ProjectTypeFactoryProvider;
import com.campus.matching.abstractfactory.RequirementBundle;
import com.campus.matching.model.ProjectData;
import com.campus.matching.model.StudentData;

import java.util.List;

/**
 * FACTORY METHOD PATTERN - one of the concrete "Products".
 *
 * Scores a student on how well their interests match the project.
 *
 * Two ways this is checked:
 *  1. If the project explicitly lists interests, compare directly.
 *  2. Otherwise, fall back to the Abstract Factory's interest hints
 *     for that project type (WEB / ML / HARDWARE / OTHER) and check
 *     the project name too.
 */
public class InterestMatcher implements Matcher {

    @Override
    public double score(ProjectData project, StudentData student) {
        List<String> studentInterests = student.interests();

        if (studentInterests == null || studentInterests.isEmpty()) {
            return 0.0;
        }

        List<String> projectInterests = project.interests();

        // Case 1: project explicitly lists the interests it cares about.
        if (projectInterests != null && !projectInterests.isEmpty()) {
            int matchedCount = 0;
            for (String projectInterest : projectInterests) {
                if (studentHasInterest(studentInterests, projectInterest)) {
                    matchedCount++;
                }
            }
            return (100.0 * matchedCount) / projectInterests.size();
        }

        // Case 2: fall back to Abstract Factory hints + project name.
        String projectNameLower = project.name() != null ? project.name().toLowerCase() : "";
        RequirementBundle hints = ProjectTypeFactoryProvider.get(project.projectType()).createRequirements();

        boolean foundAnyMatch = false;
        for (String interest : studentInterests) {
            String interestLower = interest.toLowerCase();

            boolean nameMatch = projectNameLower.contains(interestLower);
            boolean hintMatch = hints.interestHints() != null && studentHasInterest(hints.interestHints(), interest);

            if (nameMatch || hintMatch) {
                foundAnyMatch = true;
                break;
            }
        }

        return foundAnyMatch ? 100.0 : 50.0;
    }

    private boolean studentHasInterest(List<String> list, String value) {
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}

package com.campus.matching.factory;

import com.campus.matching.model.ProjectData;
import com.campus.matching.model.StudentData;

/**
 * FACTORY METHOD PATTERN - one of the concrete "Products".
 *
 * Scores a student purely on their experience level.
 * More experience = higher score.
 */
public class ExperienceMatcher implements Matcher {

    @Override
    public double score(ProjectData project, StudentData student) {
        String experience = student.experience();

        if (experience == null) {
            return 40.0; // treat unknown experience as beginner-level
        }

        if (experience.equalsIgnoreCase("ADVANCED")) {
            return 100.0;
        } else if (experience.equalsIgnoreCase("INTERMEDIATE")) {
            return 70.0;
        } else {
            // "BEGINNER" or anything else we don't recognise
            return 40.0;
        }
    }
}

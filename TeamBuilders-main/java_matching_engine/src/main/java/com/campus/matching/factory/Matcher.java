package com.campus.matching.factory;

import com.campus.matching.model.ProjectData;
import com.campus.matching.model.StudentData;

/**
 * ================================
 *  DESIGN PATTERN: FACTORY METHOD
 * ================================
 * This is the "Product" interface of the Factory Method pattern.
 *
 * Every kind of matcher (skill, role, interest, experience) is a
 * different way of scoring how well ONE student fits ONE project.
 * They all promise the same thing: a score() method that returns
 * a number from 0 to 100.
 *
 * Because they all share this interface, the rest of the code can
 * treat any matcher the same way, without caring which concrete
 * matcher it actually is.
 */
public interface Matcher {
    double score(ProjectData project, StudentData student);
}

package com.campus.matching.factory;

/**
 * ================================
 *  DESIGN PATTERN: FACTORY METHOD
 * ================================
 * This is the "Factory" of the Factory Method pattern.
 *
 * Instead of the rest of the app writing "new SkillMatcher()",
 * "new RoleMatcher()" etc. directly, everyone just asks this
 * factory for the matcher they want by name:
 *
 *      Matcher m = MatcherFactory.create("SKILL");
 *
 * Benefit: if we ever add a new type of matcher, we only change
 * it here in ONE place, instead of hunting through the whole codebase.
 */
public class MatcherFactory {

    public static Matcher create(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Matcher type cannot be null");
        }

        switch (type.toUpperCase()) {
            case "SKILL":
                return new SkillMatcher();
            case "ROLE":
                return new RoleMatcher();
            case "INTEREST":
                return new InterestMatcher();
            case "EXPERIENCE":
                return new ExperienceMatcher();
            default:
                throw new IllegalArgumentException("Unknown matcher: " + type);
        }
    }
}

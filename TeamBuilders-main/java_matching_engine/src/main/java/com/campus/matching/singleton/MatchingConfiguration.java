package com.campus.matching.singleton;

/**
 * ================================
 *  DESIGN PATTERN: SINGLETON
 * ================================
 * Purpose: Make sure ONLY ONE object of MatchingConfiguration ever
 * exists in the whole application, and give every class a single
 * shared way to access it.
 *
 * How it works:
 *  1. The constructor is PRIVATE, so no other class can do
 *     "new MatchingConfiguration()".
 *  2. The one and only object is created inside this class itself
 *     (INSTANCE) as soon as the class is loaded.
 *  3. Everyone who needs it calls getInstance(), which always
 *     hands back that same object.
 *
 * Why use it here:
 * The scoring weights (how important skill/role/interest/experience
 * are) should be the SAME everywhere in the app. Singleton avoids
 * creating multiple copies of this configuration with different values.
 */
public final class MatchingConfiguration {

    // Step 1: the single instance, created only once.
    private static final MatchingConfiguration INSTANCE = new MatchingConfiguration();

    // Scoring weights used when combining the four matcher scores.
    // They add up to 1.0 (100%).
    private final double skillWeight = 0.45;
    private final double roleWeight = 0.25;
    private final double interestWeight = 0.20;
    private final double experienceWeight = 0.10;

    // Step 2: private constructor -> nobody outside can create a new object.
    private MatchingConfiguration() {
    }

    // Step 3: the only way to get the object from outside this class.
    public static MatchingConfiguration getInstance() {
        return INSTANCE;
    }

    public double skillWeight() {
        return skillWeight;
    }

    public double roleWeight() {
        return roleWeight;
    }

    public double interestWeight() {
        return interestWeight;
    }

    public double experienceWeight() {
        return experienceWeight;
    }
}

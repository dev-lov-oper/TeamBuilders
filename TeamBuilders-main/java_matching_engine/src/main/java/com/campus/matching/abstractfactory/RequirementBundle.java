package com.campus.matching.abstractfactory;

import java.util.List;

/**
 * ABSTRACT FACTORY PATTERN - the "Product" produced by every factory.
 *
 * A simple bundle grouping together the three hint lists that
 * belong to one project type: typical skills, typical roles and
 * typical interests.
 */
public record RequirementBundle(
    List<String> skillHints,
    List<String> roleHints,
    List<String> interestHints
) {
}

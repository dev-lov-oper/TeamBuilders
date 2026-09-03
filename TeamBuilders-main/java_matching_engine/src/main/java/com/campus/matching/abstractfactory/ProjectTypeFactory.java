package com.campus.matching.abstractfactory;

/**
 * ================================
 *  DESIGN PATTERN: ABSTRACT FACTORY
 * ================================
 * This is the "Abstract Factory" interface.
 *
 * Each project type (WEB, ML, HARDWARE, OTHER) has its own "family"
 * of typical requirements - skills, roles and interests that usually
 * go together for that kind of project. Instead of scattering
 * if/else chains everywhere, each family is packaged into one
 * factory class that knows how to build its own RequirementBundle.
 */
public interface ProjectTypeFactory {
    RequirementBundle createRequirements();
}

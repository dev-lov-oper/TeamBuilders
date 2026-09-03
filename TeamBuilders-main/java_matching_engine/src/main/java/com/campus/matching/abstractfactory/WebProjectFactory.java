package com.campus.matching.abstractfactory;

import java.util.List;

/**
 * ABSTRACT FACTORY PATTERN - a "Concrete Factory".
 * Builds the requirement bundle typical of a WEB project.
 */
public class WebProjectFactory implements ProjectTypeFactory {
    @Override
    public RequirementBundle createRequirements() {
        return new RequirementBundle(
            List.of("HTML/CSS", "JavaScript", "Node.js"),
            List.of("Frontend Developer", "Backend Developer", "UI/UX Designer"),
            List.of("Web Development", "Software Engineering")
        );
    }
}

package com.campus.matching.abstractfactory;

import java.util.List;

/**
 * ABSTRACT FACTORY PATTERN - a "Concrete Factory".
 * Fallback factory used when the project type is unknown or "OTHER" -
 * it has no specific hints to offer.
 */
public class OtherProjectFactory implements ProjectTypeFactory {
    @Override
    public RequirementBundle createRequirements() {
        return new RequirementBundle(List.of(), List.of(), List.of());
    }
}

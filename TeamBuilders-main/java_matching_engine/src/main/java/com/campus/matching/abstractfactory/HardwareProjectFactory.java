package com.campus.matching.abstractfactory;

import java.util.List;

/**
 * ABSTRACT FACTORY PATTERN - a "Concrete Factory".
 * Builds the requirement bundle typical of a HARDWARE project.
 */
public class HardwareProjectFactory implements ProjectTypeFactory {
    @Override
    public RequirementBundle createRequirements() {
        return new RequirementBundle(
                List.of("C/C++", "Arduino", "Raspberry Pi"),
                List.of("Hardware Engineer", "Backend Developer", "Full Stack Developer"),
                List.of("Internet of Things", "Robotics", "Hardware Systems"));
    }
}

package com.campus.matching.abstractfactory;

import java.util.List;

/**
 * ABSTRACT FACTORY PATTERN - a "Concrete Factory".
 * Builds the requirement bundle typical of a MACHINE LEARNING project.
 */
public class MlProjectFactory implements ProjectTypeFactory {
    @Override
    public RequirementBundle createRequirements() {
        return new RequirementBundle(
            List.of("Machine Learning", "TensorFlow", "PyTorch"),
            List.of("ML Engineer", "Data Scientist", "Data Engineer"),
            List.of("Artificial Intelligence", "Machine Learning", "Data Science")
        );
    }
}

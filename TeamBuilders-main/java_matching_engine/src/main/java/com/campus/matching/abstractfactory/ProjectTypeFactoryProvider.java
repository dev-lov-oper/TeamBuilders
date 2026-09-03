package com.campus.matching.abstractfactory;

/**
 * ABSTRACT FACTORY PATTERN - picks the right Concrete Factory.
 *
 * The rest of the app never writes "new WebProjectFactory()" by
 * hand. It just says:
 *
 *      ProjectTypeFactoryProvider.get("WEB")
 *
 * and gets back the correct factory for that project type.
 */
public final class ProjectTypeFactoryProvider {

    private ProjectTypeFactoryProvider() {
        // utility class - no instances needed
    }

    public static ProjectTypeFactory get(String type) {
        if (type == null) {
            return new OtherProjectFactory();
        }

        switch (type.toUpperCase()) {
            case "WEB":
                return new WebProjectFactory();
            case "ML":
                return new MlProjectFactory();
            case "HARDWARE":
                return new HardwareProjectFactory();
            default:
                return new OtherProjectFactory();
        }
    }
}

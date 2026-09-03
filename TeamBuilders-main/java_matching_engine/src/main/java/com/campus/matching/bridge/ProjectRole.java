package com.campus.matching.bridge;

/**
 * ================================
 *  DESIGN PATTERN: BRIDGE
 * ================================
 * This is the "Implementor" side of the Bridge pattern.
 *
 * It defines WHAT a role can do (give back its name), without
 * saying HOW that role is represented. StudentRoleBridge (the
 * "Abstraction" side) talks to this interface only - it never
 * needs to know the concrete class behind it.
 */
public interface ProjectRole {
    String name();
}

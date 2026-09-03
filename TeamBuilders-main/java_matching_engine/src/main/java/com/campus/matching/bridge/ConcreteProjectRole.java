package com.campus.matching.bridge;

/**
 * BRIDGE PATTERN - a "Concrete Implementor".
 *
 * The simplest possible implementation of ProjectRole: it just
 * wraps a plain String, e.g. "Frontend Developer".
 */
public record ConcreteProjectRole(String name) implements ProjectRole {
}

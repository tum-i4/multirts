package edu.tum.sse.multirts.rts;

/**
 * A hybrid strategy for selective testing and selective build (i.e. incremental build).
 * The strategy selects tests that are affected by changes to (1) the build system (Maven) and
 * (2) to files that are used to generate code at compile-time.
 * Additionally, all Maven modules that are directly or transitively affected by changes are computed.
 */
public class BuildSystemAwareTestModuleSelection {
    // (1) Should take as input a set of selected tests -> find all modules for them
    // (2) Should select all tests of changed Maven module and from all transitive downstream modules
    // (3) Should find all tests from modules with compile-time files (.wsdl, .xsd) changes
    // (4) Should find all parent modules of tests
    // (5) Should find all modules that transitively depend on changed modules
}

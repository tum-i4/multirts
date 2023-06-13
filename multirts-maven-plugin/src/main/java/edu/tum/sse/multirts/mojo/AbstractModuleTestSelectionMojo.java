package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Base mojo for change-based module and test selection mojos
 * that use change information from the root git repository.
 */
public abstract class AbstractModuleTestSelectionMojo extends AbstractMultiRTSMojo {

    final static String MODULE_FILE = "modules.txt";

    /**
     * Git repository root.
     */
    @Parameter(property = "multirts.git", defaultValue = "${basedir}")
    File gitRepositoryRoot;

    /**
     * The target revision (commit identifier or branch name) into which the changes are to be integrated.
     */
    @Parameter(property = "multirts.targetRevision", defaultValue = "main")
    String targetRevision;

    /**
     * The source revision (commit identifier or branch name) where the changes are currently versioned.
     */
    @Parameter(property = "multirts.sourceRevision", defaultValue = "HEAD")
    String sourceRevision;
}

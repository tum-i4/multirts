package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Base mojo for change-based module and test selection mojos
 * that use change information from the root git repository.
 */
public abstract class AbstractModuleTestSelectionMojo extends AbstractMultiRTSMojo {

    /**
     * Git repository root.
     */
    @Parameter(property = "multirts.git", defaultValue = "${basedir}")
    File gitRepositoryRoot;

    final static String MODULE_FILE = "modules.txt";
}

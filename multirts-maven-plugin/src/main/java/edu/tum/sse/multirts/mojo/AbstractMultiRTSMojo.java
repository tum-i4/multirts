package edu.tum.sse.multirts.mojo;

import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Base class for MultiRTS Mojos.
 */
public abstract class AbstractMultiRTSMojo extends AbstractMojo {

    /**
     * Enable debug output.
     */
    @Parameter(property = "multirts.debug", readonly = true, defaultValue = "false")
    boolean debug;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    /**
     * Output directory for generated artifacts.
     */
    @Parameter(property = "multirts.output", defaultValue = "${basedir}/target/.multirts")
    File outputDirectory;

    @Parameter(defaultValue = "${session}")
    MavenSession session;

    abstract String getLabel();

    void log(String message) {
        if (debug) {
            getLog().warn(message);
        } else {
            getLog().debug(message);
        }
    }
}

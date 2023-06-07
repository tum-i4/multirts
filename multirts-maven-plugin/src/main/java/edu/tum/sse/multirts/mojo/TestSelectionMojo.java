package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * This Mojo is used to perform change-based test selection.
 */
@Mojo(name = "test-selection", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class TestSelectionMojo extends AbstractModuleTestSelectionMojo {

    // TODO parameters:

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log("test-selection called");
    }
}

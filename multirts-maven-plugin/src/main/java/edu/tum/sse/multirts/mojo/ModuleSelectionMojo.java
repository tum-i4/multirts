package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "module-selection", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class ModuleSelectionMojo extends AbstractModuleTestSelectionMojo {

    // TODO parameters:
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        log("module-selection called");
    }
}

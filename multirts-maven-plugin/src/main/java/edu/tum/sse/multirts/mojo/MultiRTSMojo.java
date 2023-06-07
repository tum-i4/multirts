package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * MultiRTS Mojo.
 */
@Mojo(name = "multirts", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class MultiRTSMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}

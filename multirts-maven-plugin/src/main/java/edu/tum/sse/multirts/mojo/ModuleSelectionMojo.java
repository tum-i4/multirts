package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static edu.tum.sse.jtec.util.IOUtils.writeToFile;

@Mojo(name = "module-selection", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, aggregator = true)
public class ModuleSelectionMojo extends AbstractModuleTestSelectionMojo {

    /**
     * Directories to strip away and use their parent modules.
     * Mostly relevant for edge cases only.
     */
    @Parameter(property = "multirts.stripDirs", defaultValue = "p2,feature")
    private List<String> stripDirectories;

    /**
     * File paths which trigger a full build.
     */
    @Parameter(property = "multirts.fullBuild", defaultValue = "/base/")
    private List<String> fullBuildPaths;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (session.getCurrentProject().isExecutionRoot()) {
                log("module-selection called");
                StringBuilder builder = new StringBuilder();
                Path selectedModulesFile = outputDirectory.toPath().resolve("modules").resolve(MODULE_FILE);
                writeToFile(selectedModulesFile, builder.toString(), false, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (final Exception exception) {
            getLog().error("Failed to run MultiRTS module selection in project " + project.getName());
            exception.printStackTrace();
            throw new MojoFailureException(exception.getMessage());
        }
    }
}

package edu.tum.sse.multirts.mojo;

import edu.tum.sse.multirts.modules.ChangeBasedModuleSelection;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Set;

import static edu.tum.sse.jtec.util.IOUtils.writeToFile;
import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.POM_XML;

@Mojo(name = "module-selection", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true, aggregator = true)
public class ModuleSelectionMojo extends AbstractModuleTestSelectionMojo {

    /**
     * Label which is used for naming generated file artifacts.
     */
    @Parameter(property = "multirts.label", defaultValue = "modules", required = true, readonly = true)
    String label;
    /**
     * Directories to strip away and use their parent modules.
     * Mostly relevant for edge cases only.
     */
    @Parameter(property = "multirts.stripDirs", defaultValue = "p2,feature", readonly = true)
    private List<String> stripDirectories;
    /**
     * File paths which trigger a full build.
     */
    @Parameter(property = "multirts.fullBuild", defaultValue = "/base/", readonly = true)
    private List<String> fullBuildPaths;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (session.getCurrentProject().isExecutionRoot()) {
                GitClient gitClient = new GitClient(gitRepositoryRoot.toPath());
                ChangeBasedModuleSelection moduleSelection = new ChangeBasedModuleSelection(gitClient, project, fullBuildPaths);
                Set<String> selectedModules = moduleSelection.execute(gitClient.getDiff(sourceRevision, targetRevision));
                writeToFile(outputDirectory.toPath().resolve(getLabel()).resolve(MODULE_FILE), buildModulesString(selectedModules), false, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (final Exception exception) {
            getLog().error("Failed to run MultiRTS module selection in project " + project.getName());
            exception.printStackTrace();
            throw new MojoFailureException(exception.getMessage());
        }
    }

    private String buildModulesString(Set<String> selectedModules) {
        StringBuilder builder = new StringBuilder();
        for (String module : selectedModules) {
            for (String stripDir : stripDirectories) {
                builder.append(module.replace(stripDir + "/" + POM_XML, POM_XML));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    String getLabel() {
        return label;
    }
}

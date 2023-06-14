package edu.tum.sse.multirts.mojo;

import edu.tum.sse.multirts.modules.ChangeBasedModuleSelection;
import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.tum.sse.jtec.util.IOUtils.createFileAndEnclosingDir;
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
    List<String> stripDirectories;
    /**
     * File paths which trigger a full build.
     */
    @Parameter(property = "multirts.fullBuild", defaultValue = "/base/,\\base\\", readonly = true)
    List<String> fullBuildPaths;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (session.getCurrentProject().isExecutionRoot()) {
                GitClient gitClient = getGitClient();
                Set<ChangeSetItem> changeSet = getChangeset(gitClient);
                ChangeBasedModuleSelection moduleSelection = new ChangeBasedModuleSelection(gitClient, project, fullBuildPaths);
                Set<String> selectedModules = moduleSelection.execute(changeSet);
                Path includedModules = outputDirectory.toPath().resolve(getLabel()).resolve(MODULE_FILE);
                createFileAndEnclosingDir(includedModules);
                writeToFile(includedModules, buildModulesString(new ArrayList<>(selectedModules)), false, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (final Exception exception) {
            getLog().error("Failed to run MultiRTS module selection in project " + project.getName());
            exception.printStackTrace();
            throw new MojoFailureException(exception.getMessage());
        }
    }

    String buildModulesString(List<String> selectedModules) {
        for (int i = 0; i < selectedModules.size(); i++) {
            String module = selectedModules.get(i);
            for (String stripDir : stripDirectories) {
                String toReplace = stripDir + File.separator + POM_XML;
                if (module.contains(toReplace)) {
                    selectedModules.set(i, module.replace(toReplace, POM_XML));
                    break;
                }
            }
        }
        return String.join("\n", selectedModules);
    }

    @Override
    String getLabel() {
        return label;
    }
}

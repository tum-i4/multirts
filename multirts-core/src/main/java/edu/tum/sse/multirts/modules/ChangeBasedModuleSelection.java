package edu.tum.sse.multirts.modules;

import edu.tum.sse.multirts.util.CollectionUtils;
import edu.tum.sse.multirts.util.PathUtils;
import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.POM_XML;
import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.findParentPOM;

/**
 * Incremental build for Maven projects.
 */
public class ChangeBasedModuleSelection {

    private static final String FULL_BUILD_MARKER = "*";

    private static final List<String> RELEVANT_FILE_EXTENSIONS = CollectionUtils.newList(
            ".xsd",
            ".wsdl",
            ".java",
            ".jsp",
            ".ts",
            ".js",
            ".tsx",
            ".jsx",
            ".html",
            ".scss",
            ".sass",
            ".less",
            ".json"
    );
    private final GitClient gitClient;
    private final MavenProject mavenRootProject;
    private final List<String> fullBuildPaths;

    public ChangeBasedModuleSelection(final GitClient gitClient, final MavenProject mavenRootProject, final List<String> fullBuildPaths) {
        this.gitClient = gitClient;
        this.mavenRootProject = mavenRootProject;
        this.fullBuildPaths = fullBuildPaths;
    }

    public Set<String> execute(final Set<ChangeSetItem> changeSet) {
        Set<String> selectedModules = new HashSet<>();
        for (ChangeSetItem item : changeSet) {
            Path filePath = gitClient.getRoot().resolve(item.getPath()).toAbsolutePath();
            // Check for full build.
            for (String fullBuildPath : fullBuildPaths) {
                if (filePath.toString().contains(fullBuildPath)) {
                    return CollectionUtils.newSet(FULL_BUILD_MARKER);
                }
            }
            // We are only interested in changes to certain files that are descendants of the maven root project.
            if (filePath.startsWith(mavenRootProject.getBasedir().toPath().toAbsolutePath()) && (
                    PathUtils.hasFilename(filePath, POM_XML) || PathUtils.hasAnyExtension(filePath, RELEVANT_FILE_EXTENSIONS))
            ) {
                Optional<Path> parentPOM = findParentPOM(filePath.getParent());
                parentPOM.ifPresent(path -> selectedModules.add(mavenRootProject.getBasedir().toPath().relativize(path.toAbsolutePath()).toString()));
            }
        }
        return selectedModules;
    }
}

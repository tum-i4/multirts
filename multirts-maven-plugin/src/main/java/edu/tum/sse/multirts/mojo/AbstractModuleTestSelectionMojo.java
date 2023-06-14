package edu.tum.sse.multirts.mojo;

import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

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
     * Regular expression to filter files in git changelist.
     * By default, all files are included.
     */
    @Parameter(property = "multirts.fileFilter", defaultValue = ".*")
    String fileFilter;

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

    GitClient getGitClient() {
        return new GitClient(gitRepositoryRoot.toPath().normalize().toAbsolutePath());
    }

    Set<ChangeSetItem> getChangeset(GitClient gitClient) {
        return gitClient
                .getDiff(targetRevision, sourceRevision)  // results in: target...source; e.g., main...HEAD
                .stream()
                .filter(item -> item.getPath().toString().matches(fileFilter))
                .collect(Collectors.toSet());
    }
}

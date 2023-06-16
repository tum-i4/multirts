package edu.tum.sse.multirts.modules;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dependency analyzer for Maven reactor.
 */
public class MavenDependencyAnalyzer {
    private final MavenSession session;
    private final Set<MavenProject> selectedProjects;

    public MavenDependencyAnalyzer(MavenSession session) {
        this.session = session;
        this.selectedProjects = new HashSet<>();
    }

    public Set<MavenProject> getSelectedProjects() {
        return selectedProjects;
    }

    private List<MavenProject> getProjectsAtPaths(Set<Path> paths) {
        return session.getAllProjects().stream()
                .filter(project -> paths.contains(project.getBasedir().toPath().normalize().toAbsolutePath()))
                .collect(Collectors.toList());
    }

    /**
     * Select all transitive downstream Maven modules from the given (changed) Maven project paths.
     *
     * @param projectPaths Project paths that have been changed
     */
    public void selectDownstreamModules(List<Path> projectPaths) {
        List<MavenProject> mavenProjects = getProjectsAtPaths(new HashSet<>(projectPaths));
        // Always add modules themselves first.
        selectedProjects.addAll(mavenProjects);

        // Transitively select all downstream modules.
        List<MavenProject> downstreamModules = getDownstreamProjects(mavenProjects, true);
        selectedProjects.addAll(downstreamModules);
    }

    public List<MavenProject> getDownstreamProjects(List<MavenProject> mavenProjects, boolean transitive) {
        return mavenProjects.stream()
                .map(project -> session.getProjectDependencyGraph().getDownstreamProjects(project, transitive))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}


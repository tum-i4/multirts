package edu.tum.sse.multirts.maven;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enables computing sets of transitively or directly dependent down- and upstream projects in a Maven reactor.
 */
public class ModuleSelector {
    private final MavenSession session;
    private final Set<MavenProject> selectedProjects;

    public ModuleSelector(MavenSession session) {
        this.session = session;
        this.selectedProjects = new HashSet<>();
    }

    public Set<MavenProject> getSelectedProjects() {
        return selectedProjects;
    }

    private List<MavenProject> getProjectsAtPaths(List<Path> paths) {
        return session.getAllProjects().stream()
                .filter(project -> paths.contains(project.getBasedir().toPath().normalize().toAbsolutePath()))
                .collect(Collectors.toList());
    }

    /**
     * Select all Maven modules that are transitively dependent from given (changed) projects.
     * This includes (transitive) upstream projects, direct downstream projects, and (transitive) upstream of downstream projects.
     * Example: If module M has changed, this will select
     * (1) M itself,
     * (2) all modules M requires to be built (transitive upstream modules),
     * (3) all modules that depend on M (direct downstream modules, as they could potentially break by the change to M),
     * (4) all modules that are required to build the modules from (3) (i.e., their transitive upstream modules).
     *
     * @param projectPaths Project paths that have been changed
     */
    public void selectTransitiveProjects(List<Path> projectPaths) {
        List<MavenProject> mavenProjects = getProjectsAtPaths(projectPaths);
        // Always add modules themselves first.
        selectedProjects.addAll(mavenProjects);

        // Transitively select all upstream modules.
        List<MavenProject> upstreamProjects = getUpstreamProjects(mavenProjects, true);
        selectedProjects.addAll(upstreamProjects);

        // Select direct downstream modules.
        List<MavenProject> downstreamProjects = getDownstreamProjects(mavenProjects, false);
        selectedProjects.addAll(downstreamProjects);

        // Transitively select upstream modules of downstream modules.
        List<MavenProject> upOfDownstreamModules = getUpstreamProjects(downstreamProjects, true);
        selectedProjects.addAll(upOfDownstreamModules);
    }

    /**
     * Select all transitive upstream Maven modules from the given (changed) Maven project paths.
     *
     * @param projectPaths Project paths that have been changed
     */
    public void selectUpstreamProjects(List<Path> projectPaths) {
        List<MavenProject> mavenProjects = getProjectsAtPaths(projectPaths);
        // Always add modules themselves first.
        selectedProjects.addAll(mavenProjects);

        // Transitively select all upstream modules.
        List<MavenProject> upstreamProjects = getUpstreamProjects(mavenProjects, true);
        selectedProjects.addAll(upstreamProjects);
    }

    /**
     * Select all transitive downstream Maven modules from the given (changed) Maven project paths.
     *
     * @param projectPaths Project paths that have been changed
     */
    public void selectDownstreamModules(List<Path> projectPaths) {
        List<MavenProject> mavenProjects = getProjectsAtPaths(projectPaths);
        // Always add modules themselves first.
        selectedProjects.addAll(mavenProjects);

        // Transitively select all downstream modules.
        List<MavenProject> downstreamModules = getDownstreamProjects(mavenProjects, true);
        selectedProjects.addAll(downstreamModules);
    }

    public List<MavenProject> getUpstreamProjects(List<MavenProject> mavenProjects, boolean transitive) {
        return mavenProjects.stream()
                .map(project -> session.getProjectDependencyGraph().getUpstreamProjects(project, transitive))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<MavenProject> getDownstreamProjects(List<MavenProject> mavenProjects, boolean transitive) {
        return mavenProjects.stream()
                .map(project -> session.getProjectDependencyGraph().getDownstreamProjects(project, transitive))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}


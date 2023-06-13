package edu.tum.sse.multirts.modules;

import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.ChangeType;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static edu.tum.sse.multirts.util.CollectionUtils.newList;
import static edu.tum.sse.multirts.util.CollectionUtils.newSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeBasedModuleSelectionTest {
    @Mock
    GitClient gitClient;

    @Mock
    MavenProject mavenProject;

    @Test
    void shouldSelectModuleOnChange() throws URISyntaxException {
        // given
        Path sampleProjectRoot = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("module-selection/sample-project")).toURI());
        Set<ChangeSetItem> changeSet = newSet(new ChangeSetItem(ChangeType.MODIFIED, sampleProjectRoot.resolve("f/g/src/main/java/a/b/c/A.java")));
        when(mavenProject.getBasedir()).thenReturn(sampleProjectRoot.toFile());
        when(gitClient.getRoot()).thenReturn(sampleProjectRoot);

        // when
        ChangeBasedModuleSelection moduleSelection = new ChangeBasedModuleSelection(gitClient, mavenProject, Collections.emptyList());
        Set<String> modules = moduleSelection.execute(changeSet);

        // then
        assertEquals(modules.size(), 1);
        assertTrue(modules.containsAll(newSet("f/g/pom.xml")));
    }

    @Test
    void shouldTriggerFullBuild() throws URISyntaxException {
        // given
        Path sampleProjectRoot = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("module-selection/sample-project")).toURI());
        Set<ChangeSetItem> changeSet = newSet(new ChangeSetItem(ChangeType.MODIFIED, sampleProjectRoot.resolve("f/g/src/main/java/a/b/c/A.java")));
        when(gitClient.getRoot()).thenReturn(sampleProjectRoot);

        // when
        ChangeBasedModuleSelection moduleSelection = new ChangeBasedModuleSelection(gitClient, mavenProject, newList("/main/"));
        Set<String> modules = moduleSelection.execute(changeSet);

        // then
        assertEquals(modules.size(), 1);
        assertTrue(modules.containsAll(newSet("*")));
    }

    @Test
    void shouldSelectEmptySet() throws URISyntaxException {
        // given
        Path sampleProjectRoot = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("module-selection/sample-project")).toURI());
        Set<ChangeSetItem> changeSet = newSet(new ChangeSetItem(ChangeType.MODIFIED, sampleProjectRoot.resolve("f/g/src/main/java/a/b")));
        when(mavenProject.getBasedir()).thenReturn(sampleProjectRoot.toFile());
        when(gitClient.getRoot()).thenReturn(sampleProjectRoot);

        // when
        ChangeBasedModuleSelection moduleSelection = new ChangeBasedModuleSelection(gitClient, mavenProject, Collections.emptyList());
        Set<String> modules = moduleSelection.execute(changeSet);

        // then
        assertEquals(modules.size(), 0);
    }
}
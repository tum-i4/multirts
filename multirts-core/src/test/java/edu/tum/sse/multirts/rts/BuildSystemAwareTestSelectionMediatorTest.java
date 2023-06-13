package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.ChangeType;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

import static edu.tum.sse.multirts.util.CollectionUtils.newList;
import static edu.tum.sse.multirts.util.CollectionUtils.newSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildSystemAwareTestSelectionMediatorTest {

    @Mock
    TestSelectionStrategy testSelectionStrategy;

    @Mock
    MavenSession mavenSession;

    @Mock
    ProjectDependencyGraph projectDependencyGraph;

    @Mock
    MavenProject mavenProject1;

    @Mock
    MavenProject mavenProject2;

    @Test
    void shouldSelectTestsForPOMChange() throws URISyntaxException, IOException {
        // given
        Path sampleProjectRoot = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("test-module-selection/sample-project")).toURI());
        when(testSelectionStrategy.execute(any(), any())).thenReturn(null);
        when(mavenProject1.getBasedir()).thenReturn(sampleProjectRoot.resolve("e").toFile());
        when(mavenProject2.getBasedir()).thenReturn(sampleProjectRoot.resolve("h").toFile());
        when(mavenSession.getProjectDependencyGraph()).thenReturn(projectDependencyGraph);
        when(mavenSession.getAllProjects()).thenReturn(newList(mavenProject1, mavenProject2));
        when(projectDependencyGraph.getDownstreamProjects(any(), anyBoolean())).thenReturn(newList(mavenProject1));
        BuildSystemAwareTestSelectionMediator mediator = new BuildSystemAwareTestSelectionMediator(
                sampleProjectRoot,
                testSelectionStrategy,
                mavenSession);
        TestSuite testSuite1 = new TestSuite();
        testSuite1.setTestId("a.b.c.e.TestE");
        TestSuite testSuite2 = new TestSuite();
        testSuite2.setTestId("a.b.c.h.TestH");

        // when
        mediator.executeTestSelection(newSet(new ChangeSetItem(ChangeType.MODIFIED, sampleProjectRoot.resolve("h/pom.xml"))));

        // then
        verify(testSelectionStrategy, times(1)).execute(any(), eq(newSet(new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite1), new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite2))));
    }

    @Test
    void shouldGetModulesForSelectedTests() throws IOException, URISyntaxException {
        // given
        Path sampleProjectRoot = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("test-module-selection/sample-project")).toURI());
        BuildSystemAwareTestSelectionMediator mediator = new BuildSystemAwareTestSelectionMediator(
                sampleProjectRoot,
                testSelectionStrategy,
                mavenSession);
        TestSuite testSuite1 = new TestSuite();
        testSuite1.setTestId("a.b.c.e.TestE");
        TestSuite testSuite2 = new TestSuite();
        testSuite2.setTestId("a.b.c.h.TestH");
        Set<String> expected = newSet(sampleProjectRoot.resolve("h/pom.xml").toString(), sampleProjectRoot.resolve("e/pom.xml").toString());

        // when
        Set<String> actual = mediator.getModulesForTests(newList(new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite1), new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite2)));

        // then
        assertEquals(expected, actual);
    }


    @Test
    void shouldSelectTestsForCompileTimeFileChange() throws URISyntaxException, IOException {
        // given
        Path sampleProjectRoot = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("test-module-selection/sample-project")).toURI());
        when(testSelectionStrategy.execute(any(), any())).thenReturn(null);
        when(mavenProject1.getBasedir()).thenReturn(sampleProjectRoot.resolve("e").toFile());
        when(mavenProject2.getBasedir()).thenReturn(sampleProjectRoot.resolve("h").toFile());
        when(mavenSession.getProjectDependencyGraph()).thenReturn(projectDependencyGraph);
        when(mavenSession.getAllProjects()).thenReturn(newList(mavenProject1, mavenProject2));
        when(projectDependencyGraph.getDownstreamProjects(any(), anyBoolean())).thenReturn(newList(mavenProject1));
        BuildSystemAwareTestSelectionMediator mediator = new BuildSystemAwareTestSelectionMediator(
                sampleProjectRoot,
                testSelectionStrategy,
                mavenSession);
        TestSuite testSuite1 = new TestSuite();
        testSuite1.setTestId("a.b.c.e.TestE");
        TestSuite testSuite2 = new TestSuite();
        testSuite2.setTestId("a.b.c.h.TestH");

        // when
        mediator.executeTestSelection(newSet(new ChangeSetItem(ChangeType.MODIFIED, sampleProjectRoot.resolve("h/src/main/test.wsdl"))));

        // then
        verify(testSelectionStrategy, times(1)).execute(any(), eq(newSet(new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite1), new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite2))));
    }
}
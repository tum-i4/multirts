package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.multirts.modules.MavenDependencyAnalyzer;
import edu.tum.sse.multirts.util.PathUtils;
import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.ChangeType;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.POM_XML;
import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.findParentPOM;
import static edu.tum.sse.multirts.util.CollectionUtils.newList;

/**
 * Mediator class to steer the build system aware test (and test module) selection.
 */
public class BuildSystemAwareTestSelectionMediator {

    private static final List<String> COMPILE_TIME_EXTENSIONS = newList(".wsdl", ".xsd");

    private final MavenSession mavenSession;
    private final Path repositoryRoot;
    private final TestSelectionStrategy testSelectionStrategy;
    private final Path mavenRoot;
    private TestSuiteFileMap lazyTestSuiteMapping = null;

    public BuildSystemAwareTestSelectionMediator(final Path mavenRoot,
                                                 final Path repositoryRoot,
                                                 final TestSelectionStrategy testSelectionStrategy,
                                                 final MavenSession mavenSession) {
        this.mavenRoot = mavenRoot;
        this.repositoryRoot = repositoryRoot;
        this.testSelectionStrategy = testSelectionStrategy;
        this.mavenSession = mavenSession;
    }

    public TestSelectionResult executeTestSelection(Set<ChangeSetItem> changeSetItems) throws IOException {
        Set<Path> modifiedMavenProjectDirs = new HashSet<>();
        for (ChangeSetItem item : changeSetItems) {
            if (item.getChangeType() != ChangeType.DELETED) {
                if (PathUtils.hasFilename(item.getPath(), POM_XML)) {
                    modifiedMavenProjectDirs.add(repositoryRoot.resolve(item.getPath().getParent()).toAbsolutePath());
                } else if (PathUtils.hasAnyExtension(item.getPath(), COMPILE_TIME_EXTENSIONS)) {
                    Path parentPOM = findParentPOM(item.getPath().getParent());
                    modifiedMavenProjectDirs.add(repositoryRoot.resolve(parentPOM.getParent()).toAbsolutePath());
                }
            }
        }
        Set<SelectedTestSuite> preSelectedTestSuites = new HashSet<>();
        if (!modifiedMavenProjectDirs.isEmpty() && mavenSession != null) {
            MavenDependencyAnalyzer moduleSelector = new MavenDependencyAnalyzer(mavenSession);
            // We need to select all transitive downstream modules, as a change in the build system configuration
            // (i.e. to Maven or any compile-time file) could potentially affect tests from downstream modules.
            moduleSelector.selectDownstreamModules(new ArrayList<>(modifiedMavenProjectDirs));
            for (MavenProject project : moduleSelector.getSelectedProjects()) {
                Set<String> testSuiteNames = getTestSuiteMapping()
                        .getAllTestsInPath(project.getBasedir().toPath());
                for (String testSuiteName : testSuiteNames) {
                    TestSuite testSuite = new TestSuite();
                    testSuite.setTestId(testSuiteName);
                    preSelectedTestSuites.add(new SelectedTestSuite(SelectionCause.BUILD_CHANGE, testSuite));
                }
            }
        }
        return testSelectionStrategy.execute(changeSetItems, preSelectedTestSuites);
    }

    public Set<String> getModulesForTests(List<SelectedTestSuite> selectedTestSuites) throws IOException {
        Set<String> mavenModules = new HashSet<>();
        for (SelectedTestSuite testSuite : selectedTestSuites) {
            Optional<Path> testFile = getTestSuiteMapping().getFile(testSuite.getTestSuite().getTestId());
            if (testFile.isPresent()) {
                Path testSuiteParentPOM = findParentPOM(testFile.get().getParent());
                mavenModules.add(mavenRoot.relativize(testSuiteParentPOM).toString());
            }
        }
        return mavenModules;
    }

    private TestSuiteFileMap getTestSuiteMapping() throws IOException {
        if (lazyTestSuiteMapping == null) {
            lazyTestSuiteMapping = getTestSuiteMapping(mavenRoot);
        }
        return lazyTestSuiteMapping;
    }

    private TestSuiteFileMap getTestSuiteMapping(Path path) {
        // FIXME: We could further optimize by not using the full identifier here.
        return new TestSuiteFileMap(path, true);
    }
}

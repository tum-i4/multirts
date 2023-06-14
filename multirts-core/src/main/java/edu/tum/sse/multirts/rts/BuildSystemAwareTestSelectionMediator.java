package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.multirts.modules.ModuleSelector;
import edu.tum.sse.multirts.parser.JavaSourceCodeParser;
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
    private final TestSelectionStrategy testSelectionStrategy;
    private final Path mavenRoot;
    private Map<String, Path> lazyTestSuiteMapping = null;

    public BuildSystemAwareTestSelectionMediator(final Path mavenRoot,
                                                 final TestSelectionStrategy testSelectionStrategy,
                                                 final MavenSession mavenSession) {
        this.mavenRoot = mavenRoot;
        this.testSelectionStrategy = testSelectionStrategy;
        this.mavenSession = mavenSession;
    }

    public TestSelectionResult executeTestSelection(Set<ChangeSetItem> changeSetItems) throws IOException {
        Set<Path> modifiedMavenProjectDirs = new HashSet<>();
        for (ChangeSetItem item : changeSetItems) {
            if (item.getChangeType() != ChangeType.DELETED) {
                if (PathUtils.hasFilename(item.getPath(), POM_XML)) {
                    modifiedMavenProjectDirs.add(item.getPath().getParent());
                } else if (PathUtils.hasAnyExtension(item.getPath(), COMPILE_TIME_EXTENSIONS)) {
                    Path parentPOM = findParentPOM(item.getPath().getParent());
                    modifiedMavenProjectDirs.add(parentPOM.getParent());
                }
            }
        }
        Set<SelectedTestSuite> preSelectedTestSuites = new HashSet<>();
        if (!modifiedMavenProjectDirs.isEmpty() && mavenSession != null) {
            ModuleSelector moduleSelector = new ModuleSelector(mavenSession);
            // We need to select all transitive downstream modules, as a change in the build system configuration
            // (i.e. to Maven or any compile-time file) could potentially affect tests from downstream modules.
            moduleSelector.selectDownstreamModules(new ArrayList<>(modifiedMavenProjectDirs));
            for (MavenProject project : moduleSelector.getSelectedProjects()) {
                Set<String> testSuiteNames = getTestSuiteMapping()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().toAbsolutePath().toString().contains(project.getBasedir().getPath()))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
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
            if (getTestSuiteMapping().containsKey(testSuite.getTestSuite().getTestId())) {
                Path testSuiteParentPOM = findParentPOM(getTestSuiteMapping().get(testSuite.getTestSuite().getTestId()).getParent());
                mavenModules.add(mavenRoot.relativize(testSuiteParentPOM).toString());
            }
        }
        return mavenModules;
    }

    private Map<String, Path> getTestSuiteMapping() throws IOException {
        if (lazyTestSuiteMapping == null) {
            lazyTestSuiteMapping = getTestSuiteMapping(mavenRoot);
        }
        return lazyTestSuiteMapping;
    }

    private Map<String, Path> getTestSuiteMapping(Path path) throws IOException {
        Set<Path> testFiles = JavaSourceCodeParser.findAllJavaTestFiles(path);
        Map<String, Path> testSuiteMapping = new HashMap<>();
        for (Path testFile : testFiles) {
            String testSuiteName = JavaSourceCodeParser.getFullyQualifiedTypeName(testFile);
            testSuiteMapping.put(testSuiteName, testFile);
        }
        return testSuiteMapping;
    }
}

package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.multirts.modules.ModuleSelector;
import edu.tum.sse.multirts.parser.JavaSourceCodeParser;
import edu.tum.sse.multirts.util.CollectionUtils;
import edu.tum.sse.multirts.util.PathUtils;
import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.ChangeType;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.POM_XML;
import static edu.tum.sse.multirts.modules.MavenProjectLocationCache.findParentPOM;

/**
 * Mediator class to steer the build system aware test (and test module) selection.
 */
public class BuildSystemAwareTestSelectionMediator {

    private static final List<String> COMPILE_TIME_EXTENSIONS = CollectionUtils.newList(".wsdl", ".xsd");

    private final MavenSession mavenSession;
    private final GitClient gitClient;
    private final TestReport testReport;
    private final String sourceRevision;
    private final String targetRevision;
    private final Map<String, Set<String>> additionalFileMapping;
    private final Path root;
    private Map<String, Path> testSuiteMappingCache = null;

    private Map<String, Path> getTestSuiteMapping() throws IOException {
        if (testSuiteMappingCache == null) {
             testSuiteMappingCache = getTestSuiteMapping(root);
        }
        return testSuiteMappingCache;
    }

    public BuildSystemAwareTestSelectionMediator(final Path root,
                                                 final GitClient gitClient,
                                                 final TestReport testReport,
                                                 final String sourceRevision,
                                                 final String targetRevision,
                                                 final Map<String, Set<String>> additionalFileMapping,
                                                 final MavenSession mavenSession) {
        this.root = root;
        this.gitClient = gitClient;
        this.testReport = testReport;
        this.sourceRevision = sourceRevision;
        this.targetRevision = targetRevision;
        this.additionalFileMapping = additionalFileMapping;
        this.mavenSession = mavenSession;
    }

    public TestSelectionResult executeTestSelection(Set<ChangeSetItem> changeSetItems) throws IOException {
        Set<Path> modifiedPOMs = new HashSet<>();
        for (ChangeSetItem item : changeSetItems) {
            if (item.getChangeType() != ChangeType.DELETED) {
                if (PathUtils.hasFilename(item.getPath(), POM_XML)) {
                    modifiedPOMs.add(item.getPath());
                } else if (PathUtils.hasAnyExtension(item.getPath(), COMPILE_TIME_EXTENSIONS)) {
                    Path parentPOM = findParentPOM(item.getPath().getParent());
                    modifiedPOMs.add(parentPOM);
                }
            }
        }
        Set<SelectedTestSuite> preSelectedTestSuites = new HashSet<>();
        if (!modifiedPOMs.isEmpty() && mavenSession != null) {
            ModuleSelector moduleSelector = new ModuleSelector(mavenSession);
            // We need to select all transitive downstream modules, as a change in the build system configuration
            // (i.e. to Maven or any compile-time dependency) could potentially affect tests from downstream modules.
            moduleSelector.selectDownstreamModules(new ArrayList<>(modifiedPOMs));
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
        return new FileLevelTestSelection(
                testReport,
                gitClient,
                targetRevision,
                additionalFileMapping,
                preSelectedTestSuites).execute(changeSetItems);
    }

    public Set<String> getModulesForTests(List<SelectedTestSuite> selectedTestSuites) throws IOException {
        Set<String> mavenModules = new HashSet<>();
        for (SelectedTestSuite testSuite : selectedTestSuites) {
            if (getTestSuiteMapping().containsKey(testSuite.getTestSuite().getTestId())) {
                Path testSuiteParentPOM = findParentPOM(getTestSuiteMapping().get(testSuite.getTestSuite().getTestId()).getParent());
                mavenModules.add(testSuiteParentPOM.toString());
            }
        }
        return mavenModules;
    }

    private Map<String, Path> getTestSuiteMapping(Path root) throws IOException {
        Set<Path> testFiles = JavaSourceCodeParser.findAllJavaTestFiles(root);
        Map<String, Path> testSuiteMapping = new HashMap<>();
        for (Path testFile : testFiles) {
            String testSuiteName = JavaSourceCodeParser.getFullyQualifiedTypeName(testFile);
            testSuiteMapping.put(testSuiteName, testFile);
        }
        return testSuiteMapping;
    }
}

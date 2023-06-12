package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.multirts.parser.JavaSourceCodeParser;
import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.ChangeType;
import edu.tum.sse.multirts.vcs.GitClient;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static edu.tum.sse.multirts.parser.CppSourceCodeParser.isCppFile;
import static edu.tum.sse.multirts.parser.JavaSourceCodeParser.isJavaFile;
import static edu.tum.sse.multirts.parser.JavaSourceCodeParser.isTestFile;

/**
 * Test selection based on file-level test traces as collected with JTeC.
 */
public class FileLevelTestSelection extends AbstractChangeBasedTestSelection {

    /**
     * Static file mapping for properly accounting changes to source files to binary files.
     * For instance, a source-to-DLL mapping of the form:
     * {
     * "path/to/foo.cpp": {"lib_foo.dll", "lib_bar.dll"}
     * }
     * Here, the source file "/path/to/foo.cpp" is part of the two DLLs "lib_foo.dll" and "lib_bar.dll".
     */
    private final Map<String, Set<String>> additionalFileMapping;

    public FileLevelTestSelection(final TestReport testReport, final GitClient gitClient, final String targetRevision, final Map<String, Set<String>> additionalFileMapping) {
        super(testReport, gitClient, targetRevision);
        this.additionalFileMapping = additionalFileMapping;
    }

    private Optional<SelectedTestSuite> checkTestSuiteAffected(final TestSuite testSuite, final AffectedInfo affectedInfo) {
        Optional<SelectedTestSuite> affectedTestSuite = Optional.empty();
        // Check if test suite affected by any affected file.
        // We check if an opened file contains the affected filename.
        for (final String affectedFile : affectedInfo.affectedFiles) {
            // Check for full string match.
            if (testSuite.getOpenedFiles().contains(affectedFile)) {
                return Optional.of(new SelectedTestSuite(SelectionCause.AFFECTED.setReason(affectedFile), testSuite));
            }
            // Check for substring match.
            for (final String openedFile : testSuite.getOpenedFiles()) {
                if (openedFile.contains(affectedFile)) {
                    return Optional.of(new SelectedTestSuite(SelectionCause.AFFECTED.setReason(affectedFile), testSuite));
                }
            }
        }
        // Check if test suite affected by any affected coverage entity.
        // We need to iterate all covered entities here, to check for anonymous classes as well.
        // For instance, the class "a.b.c.Foo$1" can only be part of the test suite's entities,
        // but not in our affected entities as we currently cannot reliably detect them at compile-time.
        for (final String affectedEntity : affectedInfo.affectedCoverageEntities) {
            for (final String coveredEntity : testSuite.getCoveredEntities()) {
                if (coveredEntity.contains(affectedEntity)) {
                    return Optional.of(new SelectedTestSuite(SelectionCause.AFFECTED.setReason(affectedEntity), testSuite));
                }
            }
        }
        return affectedTestSuite;
    }

    private TestSelectionResult computeTestSelection(final AffectedInfo affectedInfo) {
        final List<TestSuite> allTestSuites = testReport.getTestSuites();
        final List<SelectedTestSuite> selectedTestSuites = new ArrayList<>();
        final Set<String> selectedTestSuiteNames = new HashSet<>(affectedInfo.changedTestSuiteNames);
        for (final TestSuite testSuite : allTestSuites) {
            Optional<SelectedTestSuite> maybeSelectedTestSuite = checkTestSuiteAffected(testSuite, affectedInfo);
            maybeSelectedTestSuite.ifPresent(ts -> {
                selectedTestSuites.add(ts);
                selectedTestSuiteNames.add(ts.getTestSuite().getTestId());
            });
        }
        final List<TestSuite> excludedTestSuites = allTestSuites.stream().filter(ts -> !selectedTestSuiteNames.contains(ts.getTestId())).collect(Collectors.toList());
        return new TestSelectionResult(selectedTestSuites, excludedTestSuites);
    }

    private AffectedInfo analyzeChangeSetItem(final ChangeSetItem item) throws IOException, JavaSourceCodeParser.JavaParserException {
        AffectedInfo affectedInfo = new AffectedInfo();
        if (isJavaFile(item.getPath())) {
            // In case the file existed before, we add all previously existing class names.
            if (item.getChangeType() != ChangeType.ADDED) {
                final String oldContent = gitClient.getFileContentAtRevision(item.getPath(), targetRevision);
                final Set<String> allTypeNames = JavaSourceCodeParser.getAllFullyQualifiedTypeNames(oldContent);
                affectedInfo.affectedCoverageEntities.addAll(allTypeNames);
            }
            // In case the file does still exist, we need to add all currently existing class names.
            if (item.getChangeType() != ChangeType.DELETED) {
                final Path filePath = gitClient.getRoot().resolve(item.getPath());
                final Set<String> allTypeNames = JavaSourceCodeParser.getAllFullyQualifiedTypeNames(filePath);
                affectedInfo.affectedCoverageEntities.addAll(allTypeNames);
                // In case of changed/added test suites, we add them here explicitly.
                if (isTestFile(filePath)) {
                    affectedInfo.changedTestSuiteNames.add(JavaSourceCodeParser.getFullyQualifiedTypeName(filePath));
                }
            }
        } else if (isCppFile(item.getPath())) {
            if (additionalFileMapping.containsKey(item.getPath().toString())) {
                affectedInfo.affectedFiles.addAll(additionalFileMapping.get(item.getPath().toString()));
            }
        } else {
            affectedInfo.affectedFiles.add(item.getPath().getFileName().toString());
        }
        return affectedInfo;
    }

    @Override
    public TestSelectionResult execute(final Set<ChangeSetItem> changeSet) {
        try {
            AffectedInfo affectedInfo = new AffectedInfo();
            for (final ChangeSetItem item : changeSet) {
                AffectedInfo currentAffectedPair = analyzeChangeSetItem(item);
                affectedInfo.affectedFiles.addAll(currentAffectedPair.affectedFiles);
                affectedInfo.affectedCoverageEntities.addAll(currentAffectedPair.affectedCoverageEntities);
                affectedInfo.changedTestSuiteNames.addAll(currentAffectedPair.changedTestSuiteNames);
            }
            return computeTestSelection(affectedInfo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TestSelectionException("Failed to execute FileLevelTestSelection with exception: " + e.getMessage());
        }
    }

    static class AffectedInfo {
        Set<String> affectedFiles = new HashSet<>();
        Set<String> affectedCoverageEntities = new HashSet<>();
        Set<String> changedTestSuiteNames = new HashSet<>();
    }
}

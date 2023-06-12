package edu.tum.sse.multirts.mojo;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.jtec.util.JSONUtils;
import edu.tum.sse.multirts.rts.FileLevelTestSelection;
import edu.tum.sse.multirts.rts.TestSelectionResult;
import edu.tum.sse.multirts.util.CollectionUtils;
import edu.tum.sse.multirts.vcs.GitClient;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static edu.tum.sse.jtec.util.IOUtils.writeToFile;
import static edu.tum.sse.multirts.rts.SelectedTestSuite.toTestSuites;

/**
 * This Mojo is used to perform change-based test selection.
 */
@Mojo(name = "test-selection", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class TestSelectionMojo extends AbstractModuleTestSelectionMojo {

    private static final String CSV_SEPARATOR = ";";

    /**
     * Additional file mappings in CSV format (e.g. DLL-source-mapping).
     * Expects ';' delimiter.
     */
    @Parameter(name = "multirts.testReport")
    File testReportFile;

    /**
     * Additional file mappings in CSV format (e.g. DLL-source-mapping).
     * Expects ';' delimiter.
     */
    @Parameter(name = "multirts.additionalFileMapping")
    File additionalFileMapping;

    private Map<String, Set<String>> readFileMapping() throws MojoFailureException {
        Map<String, Set<String>> fileMapping = new HashMap<>();
        if (additionalFileMapping != null && additionalFileMapping.isFile()) {
            try (Stream<String> lines = Files.lines(additionalFileMapping.toPath())) {
                lines.filter(line -> line.contains(CSV_SEPARATOR))
                        .forEach(line -> {
                            String[] keyValuePair = line.split(CSV_SEPARATOR, 2);
                            String key = keyValuePair[0];
                            String value = keyValuePair[1];
                            if (fileMapping.containsValue(key)) {
                                fileMapping.get(key).add(value);
                            } else {
                                fileMapping.put(key, CollectionUtils.newSet(value));
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
                throw new MojoFailureException("Failed to read CSV file at " + additionalFileMapping.getPath());
            }
        }
        return fileMapping;
    }

    private TestReport readReport() throws MojoFailureException {
        TestReport testReport;
        try {
            testReport = JSONUtils.fromJson(testReportFile.toPath(), TestReport.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("Invalid test report provided at " + testReportFile.getPath());
        }
        return testReport;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (session.getCurrentProject().isExecutionRoot()) {
                log("test-selection called");
                final GitClient gitClient = new GitClient(gitRepositoryRoot.toPath());
                final TestReport testReport = readReport();
                final Map<String, Set<String>> fileMapping = readFileMapping();
                final TestSelectionResult testSelectionResult = new FileLevelTestSelection(testReport, gitClient, targetRevision, fileMapping)
                        .execute(gitClient.getDiff(sourceRevision, targetRevision));
                final List<TestSuite> selectedTestSuites = toTestSuites(testSelectionResult.getSelectedTestSuites());
                StringBuilder builder = new StringBuilder();
                Path selectedModulesFile = outputDirectory.toPath().resolve("modules").resolve(MODULE_FILE);
                writeToFile(selectedModulesFile, builder.toString(), false, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (final Exception exception) {
            getLog().error("Failed to run MultiRTS module selection in project " + project.getName());
            exception.printStackTrace();
            throw new MojoFailureException(exception.getMessage());
        }
    }
}

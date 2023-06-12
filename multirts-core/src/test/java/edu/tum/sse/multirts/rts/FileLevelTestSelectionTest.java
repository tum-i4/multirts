package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.jtec.reporting.TestSuite;
import edu.tum.sse.multirts.vcs.GitClient;
import edu.tum.sse.multirts.vcs.GitTestUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static edu.tum.sse.jtec.util.IOUtils.writeToFile;
import static edu.tum.sse.multirts.util.CollectionUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileLevelTestSelectionTest {

    final String targetBranch = "main";
    final String sourceBranch = "feature";
    Path tmpDir;
    Git repo;
    GitClient gitClient;
    TestSuite testSuite1;
    TestSuite testSuite2;
    TestReport testReport;

    private static TestSuite createTestSuite(final String testId, final Set<String> openedFiles, final Set<String> coveredEntities) {
        TestSuite testSuite = new TestSuite();
        testSuite.setTestId(testId);
        testSuite.setOpenedFiles(openedFiles);
        testSuite.setCoveredEntities(coveredEntities);
        return testSuite;
    }

    @BeforeEach
    void setUp() throws IOException, GitAPIException {
        tmpDir = Files.createTempDirectory("tmpDirPrefix").toAbsolutePath();
        repo = Git.init().setDirectory(tmpDir.toFile()).setInitialBranch(targetBranch).call();
        gitClient = new GitClient(repo);

        // Set up test report.
        testSuite1 = createTestSuite("FooTest", newSet("foo.txt"), newSet("foo.Foo", "foo.FooTest"));
        testSuite2 = createTestSuite("BarTest", newSet("lib_bar.dll"), newSet("foo.Bar", "foo.BarTest"));
        testReport = new TestReport(
                "report-1",
                0x42,
                0x42,
                newList(testSuite1, testSuite2)
        );

        // Initialize repo.
        writeToFile(tmpDir.resolve("Foo.java"), "package foo; public class Foo { void foo(){} }", false);
        writeToFile(tmpDir.resolve("FooTest.java"), "package foo; public class FooTest { void testFoo(){ Foo f; } }", false);
        writeToFile(tmpDir.resolve("foo.txt"), "test", false);
        writeToFile(tmpDir.resolve("Bar.java"), "package foo; public class Bar { void baz(){} }", false);
        writeToFile(tmpDir.resolve("BarTest.java"), "package foo; public class BarTest { void testBaz(){} }", false);
        writeToFile(tmpDir.resolve("Bar.cpp"), "class Bar {};", false);
        GitTestUtils.commitEverything(repo);
        GitTestUtils.checkout(repo, sourceBranch, true);
    }

    @AfterEach
    void tearDown() {
        repo.getRepository().close();
        tmpDir.toFile().delete();
    }

    @Test
    void shouldSelectTestsForModifiedJavaClass() throws GitAPIException, IOException {
        // given
        writeToFile(tmpDir.resolve("Foo.java"), "package foo; public class Foo { void bar(){} }", false);
        GitTestUtils.commitEverything(repo);
        TestSelectionResult expectedResult = new TestSelectionResult(
                newList(new SelectedTestSuite(SelectionCause.AFFECTED.setReason("foo.Foo"), testSuite1)),
                newList(testSuite2)
        );

        // when
        FileLevelTestSelection rts = new FileLevelTestSelection(testReport, gitClient, targetBranch, Collections.emptyMap(), Collections.emptySet());
        TestSelectionResult actual = rts.execute(gitClient.getDiff(targetBranch, sourceBranch));

        // then
        assertEquals(expectedResult, actual);
    }

    @Test
    void shouldSelectTestsForRemovedFile() throws GitAPIException, IOException {
        // given
        tmpDir.resolve("Foo.java").toFile().delete();
        repo.add().addFilepattern(".").call();
        repo.add().setUpdate(true).addFilepattern(".").call();
        GitTestUtils.commit(repo);
        TestSelectionResult expectedResult = new TestSelectionResult(
                newList(new SelectedTestSuite(SelectionCause.AFFECTED.setReason("foo.Foo"), testSuite1)),
                newList(testSuite2)
        );

        // when
        FileLevelTestSelection rts = new FileLevelTestSelection(testReport, gitClient, targetBranch, Collections.emptyMap(), Collections.emptySet());
        TestSelectionResult actual = rts.execute(gitClient.getDiff(targetBranch, sourceBranch));

        // then
        assertEquals(expectedResult, actual);
    }

    @Test
    void shouldSelectTestsForChangedExternalFile() throws GitAPIException, IOException {
        // given
        writeToFile(tmpDir.resolve("foo.txt"), "bar", false);
        GitTestUtils.commitEverything(repo);
        TestSelectionResult expectedResult = new TestSelectionResult(
                newList(new SelectedTestSuite(SelectionCause.AFFECTED.setReason("foo.txt"), testSuite1)),
                newList(testSuite2)
        );

        // when
        FileLevelTestSelection rts = new FileLevelTestSelection(testReport, gitClient, targetBranch, Collections.emptyMap(), Collections.emptySet());
        TestSelectionResult actual = rts.execute(gitClient.getDiff(targetBranch, sourceBranch));

        // then
        assertEquals(expectedResult, actual);
    }

    @Test
    void shouldSelectTestsForRemovedExternalFile() throws GitAPIException, IOException {
        // given
        tmpDir.resolve("foo.txt").toFile().delete();
        repo.add().addFilepattern(".").call();
        repo.add().setUpdate(true).addFilepattern(".").call();
        GitTestUtils.commit(repo);
        TestSelectionResult expectedResult = new TestSelectionResult(
                newList(new SelectedTestSuite(SelectionCause.AFFECTED.setReason("foo.txt"), testSuite1)),
                newList(testSuite2)
        );

        // when
        FileLevelTestSelection rts = new FileLevelTestSelection(testReport, gitClient, targetBranch, Collections.emptyMap(), Collections.emptySet());
        TestSelectionResult actual = rts.execute(gitClient.getDiff(targetBranch, sourceBranch));

        // then
        assertEquals(expectedResult, actual);
    }

    @Test
    void shouldSelectTestsForModifiedCppFile() throws GitAPIException, IOException {
        // given
        writeToFile(tmpDir.resolve("Bar.cpp"), "struct Bar {};", false);
        GitTestUtils.commitEverything(repo);
        TestSelectionResult expectedResult = new TestSelectionResult(
                newList(new SelectedTestSuite(SelectionCause.AFFECTED.setReason("lib_bar.dll"), testSuite2)),
                newList(testSuite1)
        );

        // when
        FileLevelTestSelection rts = new FileLevelTestSelection(testReport, gitClient, targetBranch, newMap(new AbstractMap.SimpleEntry<>("Bar.cpp", newSet("lib_bar.dll"))), Collections.emptySet());
        TestSelectionResult actual = rts.execute(gitClient.getDiff(targetBranch, sourceBranch));

        // then
        assertEquals(expectedResult, actual);
    }
}

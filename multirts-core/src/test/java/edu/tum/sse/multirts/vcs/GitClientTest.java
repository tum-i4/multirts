package edu.tum.sse.multirts.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.tum.sse.jtec.util.IOUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GitClientTest {

    Path tmpDir;
    Git repo;
    GitClient gitClient;

    @BeforeEach
    void setUp() throws IOException, GitAPIException {
        tmpDir = Files.createTempDirectory("tmpDirPrefix").toAbsolutePath();
        repo = Git.init().setDirectory(tmpDir.toFile()).call();
        gitClient = new GitClient(repo);
    }

    @AfterEach
    void tearDown() {
        repo.getRepository().close();
        tmpDir.toFile().delete();
    }

    @Test
    void shouldReturnOldContentForGitShow() throws GitAPIException, IOException {
        // given
        final Path file = tmpDir.resolve("foo.txt");
        final String oldContent = "abc";
        final String newContent = "bce";
        createFileAndEnclosingDir(file);
        writeToFile(file, oldContent, false);
        RevCommit sourceRev = GitTestUtils.commitEverything(repo);
        writeToFile(file, newContent, false);
        GitTestUtils.commitEverything(repo);

        // when
        String actualContent = gitClient.getFileContentAtRevision(file, sourceRev.getName());

        // then
        assertEquals(oldContent, actualContent);
    }

    @Test
    void shouldReturnCurrentContentForGitShow() throws GitAPIException, IOException {
        // given
        final Path file = tmpDir.resolve("foo.txt");
        final String oldContent = "abc";
        final String newContent = "bce";
        createFileAndEnclosingDir(file);
        writeToFile(file, oldContent, false);
        GitTestUtils.commitEverything(repo);
        writeToFile(file, newContent, false);
        RevCommit targetRev = GitTestUtils.commitEverything(repo);

        // when
        String actualContent = gitClient.getFileContentAtRevision(file, targetRev.getName());

        // then
        assertEquals(newContent, actualContent);
    }

    @Test
    void shouldReturnAddedFileForGitDiff() throws GitAPIException, IOException {
        // given
        Path file = tmpDir.resolve("foo.txt");
        RevCommit sourceRev = GitTestUtils.commitEverything(repo);
        createFileAndEnclosingDir(file);
        RevCommit targetRev = GitTestUtils.commitEverything(repo);
        Set<ChangeSetItem> expectedChangeSet = Stream.of(new ChangeSetItem(ChangeType.ADDED, tmpDir.relativize(file))).collect(Collectors.toCollection(HashSet::new));

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getDiff(sourceRev.getName(), targetRev.getName());

        // then
        assertEquals(expectedChangeSet, actualChangeSet);
    }

    @Test
    void shouldHandleRenameForGitDiff() throws GitAPIException, IOException {
        // given
        Path file = tmpDir.resolve("foo.txt");
        createFileAndEnclosingDir(file);
        RevCommit sourceRev = GitTestUtils.commitEverything(repo);
        Path newFile = tmpDir.resolve("foo_new.txt");
        Files.move(file, file.resolveSibling(newFile));
        // hack to stage renamed/moved file correctly with jgit
        repo.add().addFilepattern(".").call();
        repo.add().setUpdate(true).addFilepattern(".").call();
        RevCommit targetRev = GitTestUtils.commit(repo);
        Set<ChangeSetItem> expectedChangeSet = Stream.of(
                new ChangeSetItem(ChangeType.DELETED, tmpDir.relativize(file)),
                new ChangeSetItem(ChangeType.ADDED, tmpDir.relativize(newFile))
        ).collect(Collectors.toCollection(HashSet::new));

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getDiff(sourceRev.getName(), targetRev.getName());

        // then
        assertEquals(expectedChangeSet, actualChangeSet);
    }

    @Test
    void shouldIgnoreWhitespaceModificationForGitDiff() throws GitAPIException, IOException {
        // given
        Path file = tmpDir.resolve("foo.txt");
        createFileAndEnclosingDir(file);
        writeToFile(file, "abc", false);
        RevCommit sourceRev = GitTestUtils.commitEverything(repo);
        writeToFile(file, "abc\n", false);
        RevCommit targetRev = GitTestUtils.commitEverything(repo);

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getDiff(sourceRev.getName(), targetRev.getName());

        // then
        assertEquals(actualChangeSet.size(), 0);
    }

    @Test
    void shouldReturnModifiedFileForGitDiff() throws GitAPIException, IOException {
        // given
        Path file = tmpDir.resolve("foo.txt");
        createFileAndEnclosingDir(file);
        RevCommit sourceRev = GitTestUtils.commitEverything(repo);
        appendToFile(file, "test", true);
        RevCommit targetRev = GitTestUtils.commitEverything(repo);
        Set<ChangeSetItem> expectedChangeSet = Stream.of(new ChangeSetItem(ChangeType.MODIFIED, tmpDir.relativize(file))).collect(Collectors.toCollection(HashSet::new));

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getDiff(sourceRev.getName(), targetRev.getName());

        // then
        assertEquals(expectedChangeSet, actualChangeSet);
    }

    @Test
    void shouldReturnModifiedFileForGitStatus() throws GitAPIException, IOException {
        // given
        Path file = tmpDir.resolve("foo").resolve("bar.txt");
        createFileAndEnclosingDir(file);
        GitTestUtils.commitEverything(repo);
        appendToFile(file, "test", false);
        Set<ChangeSetItem> expectedChangeSet = Stream.of(new ChangeSetItem(ChangeType.MODIFIED, tmpDir.relativize(file))).collect(Collectors.toCollection(HashSet::new));

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getStatus();

        // then
        assertEquals(expectedChangeSet, actualChangeSet);
    }

    @Test
    void shouldReturnUntrackedFileAsAddedForGitStatus() throws GitAPIException {
        // given
        Path file = tmpDir.resolve("foo").resolve("bar.txt");
        createFileAndEnclosingDir(file);
        repo.add().addFilepattern(".").call();
        Set<ChangeSetItem> expectedChangeSet = Stream.of(new ChangeSetItem(ChangeType.ADDED, tmpDir.relativize(file))).collect(Collectors.toCollection(HashSet::new));

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getStatus();

        // then
        assertEquals(expectedChangeSet, actualChangeSet);
    }

    @Test
    void shouldReturnAddedFileForGitStatus() throws GitAPIException {
        // given
        Path file = tmpDir.resolve("foo").resolve("bar.txt");
        createFileAndEnclosingDir(file);
        repo.add().addFilepattern(".").call();
        Set<ChangeSetItem> expectedChangeSet = Stream.of(new ChangeSetItem(ChangeType.ADDED, tmpDir.relativize(file))).collect(Collectors.toCollection(HashSet::new));

        // when
        Set<ChangeSetItem> actualChangeSet = gitClient.getStatus();

        // then
        assertEquals(expectedChangeSet, actualChangeSet);
    }
}

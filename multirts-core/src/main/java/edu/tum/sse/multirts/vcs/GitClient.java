package edu.tum.sse.multirts.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.DiffInterruptedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class GitClient {
    private final static Pattern diffPattern = Pattern.compile("^diff --git a/(.*) b/.*$");
    private final static String[] diffOptions = new String[]{
            "--no-renames",
            "--unified=0",
            "--no-color",
            "--ignore-cr-at-eol",
            "--ignore-space-at-eol",
            "--ignore-space-change",
            "--ignore-all-space",
    };
    private final Path root;
    // To prevent re-querying the git index, we cache the results from `git (show|diff)` commands.
    private final Map<String, String> showCache = new HashMap<>();
    private final Map<String, Set<ChangeSetItem>> diffCache = new HashMap<>();
    private Git gitRepo;

    public GitClient(final Path root) {
        this.root = root.toAbsolutePath();
        try {
            gitRepo = Git.open(this.root.toFile());
        } catch (IOException e) {
            gitRepo = null;
            System.err.println("Unable to initialize git repository at " + root + ". May raise NPE for using JGit APIs.");
        }
    }

    public GitClient(final Git repo) {
        this.gitRepo = repo;
        this.root = repo.getRepository().getWorkTree().toPath().toAbsolutePath();
    }

    public Path getRoot() {
        return root;
    }

    private List<String> runProcessAndReturnOutput(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("Failed to execute command " + command);
        }
        return lines;
    }

    /**
     * Queries the index for the content of the file {@code path} at a given {@code revision}.
     *
     * @param path     file path to look up
     * @param revision commit hash
     * @return file content
     */
    public String getFileContentAtRevision(Path path, final String revision) {
        if (path.isAbsolute())
            path = root.relativize(path);
        final String relativePath = path.toString().replace(File.separatorChar, '/');
        final String gitObject = revision + ":" + relativePath;
        if (showCache.containsKey(gitObject))
            return showCache.get(gitObject);
        final String command = String.format(
                "git -P -C %s show %s",
                root.toString(),
                gitObject
        );
        String content = "";
        try {
            List<String> lines = runProcessAndReturnOutput(command);
            // TODO: check if this works when BOM is present
            content = lines.stream().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to execute command " + command + " with exception: " + e.getMessage());
        }
        showCache.put(gitObject, content);
        return content;
    }

    private Set<ChangeSetItem> parseDiffOutput(final List<String> diffLines) {
        final Set<ChangeSetItem> diffItems = new HashSet<>();
        for (int i = 0, diffLinesSize = diffLines.size(); i < diffLinesSize; i++) {
            String line = diffLines.get(i);
            // Don't waste time with regex matching if the line doesn't start with the substring.
            if (!line.startsWith("diff --git"))
                continue;
            final Matcher matcher = diffPattern.matcher(line);
            if (matcher.matches()) {
                final MatchResult result = matcher.toMatchResult();
                final String filePath = result.group(1);
                ChangeType changeType = ChangeType.MODIFIED;
                if (i < diffLinesSize - 1) {
                    line = diffLines.get(++i);  // get next line
                    if (line.contains("new file mode")) {
                        changeType = ChangeType.ADDED;
                    } else if (line.contains("deleted file mode")) {
                        changeType = ChangeType.DELETED;
                    }
                }
                diffItems.add(new ChangeSetItem(
                        changeType, Paths.get(filePath)
                ));
            }
        }
        return diffItems;
    }

    /**
     * Computes the diff between two revisions using the "git diff" command.
     * We don't use the {@link Git} object here, as it overcomplicates things for our use case.
     * Instead, we invoke "git diff" manually and ignore all pure whitespace changes as they're irrelevant for us.
     *
     * @param fromRevision commit hash for diff comparison
     * @param toRevision   commit hash for diff comparison
     * @return set of file paths
     */
    public Set<ChangeSetItem> getDiff(String fromRevision, String toRevision) {
        // We use the three-dot diff (A...B) here to figure out the diff between the latest common ancestor and the source branch.
        // This will make sure to only consider changes made inside a line of commits (e.g. in a pull request) and no changes from the target branch.
        final String diffRange = fromRevision + "..." + toRevision;
        if (diffCache.containsKey(diffRange)) {
            return diffCache.get(diffRange);
        }
        // The reason we don't simply use --name-status or --name-only is that these options are
        // incompatible with ignoring only whitespace changes (which we would then still need to filter out).
        final String command = String.format(
                "git -P -C %s diff %s %s",
                root.toString(),
                String.join(" ", diffOptions),
                diffRange
        );
        final Set<ChangeSetItem> diffItems;
        try {
            List<String> lines = runProcessAndReturnOutput(command);
            diffItems = parseDiffOutput(lines);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DiffInterruptedException("Failed to execute command " + command + " with exception: " + e.getMessage());
        }
        diffCache.put(diffRange, diffItems);
        return diffItems;
    }

    /**
     * Collects all added, modified, and deleted file paths from the "git status" command.
     * Notably, we anticipate what would happen if one called "git add ." on the working copy.
     * E.g., if a file is created but not yet added (untracked) it will still have the {@link ChangeType} ADDED.
     *
     * @return set of file paths
     */
    public Set<ChangeSetItem> getStatus() throws GitAPIException {
        Set<ChangeSetItem> changeSet = new HashSet<>();
        if (gitRepo != null) {
            Status status = gitRepo.status().call();
            changeSet.addAll(status.getAdded().stream()
                    .map((path) -> new ChangeSetItem(ChangeType.ADDED, Paths.get(path))).collect(Collectors.toSet()));
            changeSet.addAll(status.getUntracked().stream()
                    .map((path) -> new ChangeSetItem(ChangeType.ADDED, Paths.get(path))).collect(Collectors.toSet()));
            changeSet.addAll(status.getModified().stream()
                    .map((path) -> new ChangeSetItem(ChangeType.MODIFIED, Paths.get(path))).collect(Collectors.toSet()));
            changeSet.addAll(status.getChanged().stream()
                    .map((path) -> new ChangeSetItem(ChangeType.MODIFIED, Paths.get(path))).collect(Collectors.toSet()));
            changeSet.addAll(status.getRemoved().stream()
                    .map((path) -> new ChangeSetItem(ChangeType.DELETED, Paths.get(path))).collect(Collectors.toSet()));
            changeSet.addAll(status.getChanged().stream()
                    .map((path) -> new ChangeSetItem(ChangeType.DELETED, Paths.get(path))).collect(Collectors.toSet()));
        }
        return changeSet;
    }
}

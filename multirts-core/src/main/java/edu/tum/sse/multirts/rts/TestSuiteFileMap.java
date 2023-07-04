package edu.tum.sse.multirts.rts;

import edu.tum.sse.multirts.parser.JavaSourceCodeParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps track of a test suite->file path mapping.
 * Avoids parsing files where possible.
 */
public class TestSuiteFileMap {
    final Map<String, Path> testSuiteNameMapping = new HashMap<>();
    final Map<String, Path> testSuiteIdentifierMapping = new HashMap<>();
    final Path root;
    Set<Path> testFiles = new HashSet<>();

    /**
     * If enabled, will always parse the files to find the
     * full identifier of the test suite (including package name).
     * This is slower but may be desired.
     */
    boolean useFullIdentifier;

    public TestSuiteFileMap(Path root, boolean useFullIdentifier) {
        this.root = root;
        this.useFullIdentifier = useFullIdentifier;
        populate();
    }

    public TestSuiteFileMap(Path root) {
        this(root, false);
    }

    private void populateWithFullIdentifier() throws IOException {
        for (Path testFile : testFiles) {
            testSuiteIdentifierMapping.put(JavaSourceCodeParser.getFullyQualifiedTypeName(testFile), testFile);
        }
    }

    private void populate() {
        try {
            testFiles = JavaSourceCodeParser.findAllJavaTestFiles(root);
            if (useFullIdentifier) {
                populateWithFullIdentifier();
                return;
            }
            Set<String> alreadySeenTestSuiteNames = new HashSet<>(testFiles.size());
            for (Path testFile : testFiles) {
                String testSuiteName = JavaSourceCodeParser.getPrimaryTypeName(testFile);
                if (alreadySeenTestSuiteNames.contains(testSuiteName)) {
                    Path existingFile = testSuiteNameMapping.remove(testSuiteName);
                    if (existingFile != null) {
                        testSuiteIdentifierMapping.put(JavaSourceCodeParser.getFullyQualifiedTypeName(existingFile), existingFile);
                    }
                    testSuiteIdentifierMapping.put(JavaSourceCodeParser.getFullyQualifiedTypeName(testFile), testFile);
                } else {
                    testSuiteNameMapping.put(testSuiteName, testFile);
                    alreadySeenTestSuiteNames.add(testSuiteName);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    Optional<Path> getFile(String testSuiteIdentifier) {
        if (useFullIdentifier) {
            return Optional.ofNullable(testSuiteIdentifierMapping.getOrDefault(testSuiteIdentifier, null));
        }
        String testSuiteName = testSuiteIdentifier.substring(testSuiteIdentifier.lastIndexOf(".") + 1);
        return Optional.ofNullable(testSuiteNameMapping.getOrDefault(testSuiteName, testSuiteIdentifierMapping.getOrDefault(testSuiteIdentifier, null)));
    }

    public Set<String> getAllTestsInPath(Path parent) {
        Stream<Map.Entry<String, Path>> stream;
        if (useFullIdentifier) {
            stream = testSuiteIdentifierMapping
                    .entrySet()
                    .stream();
        } else {
            stream = Stream.concat(
                    testSuiteNameMapping
                            .entrySet()
                            .stream(),
                    testSuiteIdentifierMapping
                            .entrySet()
                            .stream()
            );
        }
        return stream
                .filter(entry -> entry.getValue().toAbsolutePath().startsWith(parent))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}

package edu.tum.sse.multirts.rts;

import edu.tum.sse.multirts.parser.JavaSourceCodeParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps track of a test suite->file path mapping.
 * This enables faster lookups as full test suite names are lazily determined with minimal effort
 * (e.g. by avoiding parsing files where possible).
 */
public class TestSuiteFileMap {
    final Map<String, Path> testSuiteNameMapping = new HashMap<>();
    final Map<String, Path> testSuiteIdentifierMapping = new HashMap<>();
    final Path root;
    Set<Path> testFiles = new HashSet<>();


    public TestSuiteFileMap(Path root) {
        this.root = root;
        populate();
    }

    private void populate() {
        try {
            testFiles = JavaSourceCodeParser.findAllJavaTestFiles(root);
            Set<String> alreadySeenTestSuiteNames = new HashSet<>(testFiles.size());
            for (Path testFile : testFiles) {
                String testSuiteName = JavaSourceCodeParser.getPrimaryTypeName(testFile);
                if (alreadySeenTestSuiteNames.contains(testSuiteName)) {
                    Path existingFile = testSuiteNameMapping.remove(testSuiteName);
                    testSuiteIdentifierMapping.put(JavaSourceCodeParser.getFullyQualifiedTypeName(existingFile), existingFile);
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
        String testSuiteName = testSuiteIdentifier.substring(testSuiteIdentifier.lastIndexOf(".") + 1);
        return Optional.ofNullable(testSuiteNameMapping.getOrDefault(testSuiteName, testSuiteIdentifierMapping.getOrDefault(testSuiteIdentifier, null)));
    }

    public Set<String> getAllTestsInPath(String parent) {
        return Stream.concat(
                        testSuiteNameMapping
                                .entrySet()
                                .stream(),
                        testSuiteIdentifierMapping
                                .entrySet()
                                .stream()
                )
                .filter(entry -> entry.getValue().toAbsolutePath().toString().contains(parent))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}

package edu.tum.sse.multirts.parser;

import edu.tum.sse.jtec.util.IOUtils;
import edu.tum.sse.multirts.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Facilities to parse Java source code.
 * We rely on regex-based parsing which is generally more robust across Java versions-
 */
public class JavaSourceCodeParser {
    // Regex based on Maven Surefire:
    // https://maven.apache.org/surefire/maven-surefire-plugin/examples/inclusion-exclusion.html
    public static String TEST_FILE_PATTERN = "(Test.*|.*Test|TestCase.*|.*TestCase|.*Tests).java";

    public static Set<Path> findAllJavaTestFiles(Path root) throws IOException {
        Set<Path> testFiles = new HashSet<>();
        Files.walkFileTree(root, new JavaTestFileWalker(testFiles));
        return testFiles;
    }

    public static boolean isJavaFile(final Path path) {
        return path.toString().toLowerCase().endsWith(".java");
    }

    public static boolean isTestFile(final Path path) {
        return path.getFileName().toString().matches(TEST_FILE_PATTERN);
    }

    public static Set<String> getAllFullyQualifiedTypeNames(final String code) {
        JavaSourceFile file = new JavaSourceFile(code);
        return file.getAllFullyQualifiedTypeNames();
    }

    public static Set<String> getAllFullyQualifiedTypeNames(final Path path) throws IOException {
        return getAllFullyQualifiedTypeNames(IOUtils.readFromFile(path));
    }

    public static String getFullyQualifiedTypeName(Path path) throws IOException {
        JavaSourceFile file = new JavaSourceFile(IOUtils.readFromFile(path), path);
        return file.getFullPrimaryType();
    }

    public static String getPrimaryTypeName(Path path) {
        return path.getFileName().toString().split("\\.java")[0];
    }

    static class JavaTestFileWalker implements FileVisitor<Path> {
        private final Set<Path> testFiles;

        public JavaTestFileWalker(Set<Path> testFiles) {
            this.testFiles = testFiles;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (isJavaFile(file) && attrs.isRegularFile() && isTestFile(file)) {
                testFiles.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (exc != null) {
                exc.printStackTrace();
                testFiles.remove(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                exc.printStackTrace();
            }
            return FileVisitResult.CONTINUE;
        }
    }

    static class JavaSourceFile {
        final static String typeRegex = ".*?(?:class|interface|enum)\\s+([a-zA-Z_$][a-zA-Z\\d_$]*).*?";
        final static Pattern typePattern = Pattern.compile(typeRegex, Pattern.MULTILINE);
        final static String packageRegex = "package\\s+(\\S*)\\s*;";
        final static Pattern packagePattern = Pattern.compile(packageRegex, Pattern.MULTILINE);
        final String code;
        final Path path;
        private String packageName;
        private String primaryTypeName = "";

        JavaSourceFile(String code) {
            this(code, null);
        }

        JavaSourceFile(String code, Path path) {
            this.code = code;
            this.path = path;
        }

        String getPackage() {
            if (packageName == null) {
                final Matcher matcher = packagePattern.matcher(code);
                if (matcher.find() && matcher.groupCount() >= 1) {
                    packageName = matcher.group(1);
                } else {
                    packageName = "";
                }
            }
            return packageName;
        }

        Set<String> getAllFullyQualifiedTypeNames() {
            final Matcher matcher = typePattern.matcher(code);
            final Set<String> names = CollectionUtils.newSet();
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    names.add(getPackage().isEmpty() ? matcher.group(i) : getPackage() + "." + matcher.group(i)); // FIXME: maybe replace '.' with '/' ?
                }
            }
            return names;
        }

        String getPrimaryTypeName() {
            if (primaryTypeName.isEmpty()) {
                if (path != null) {
                    primaryTypeName = path.getFileName().toString().split("\\.java")[0]; // FIXME: better solution?
                } else {
                    final Matcher matcher = typePattern.matcher(code);
                    if (matcher.find() && matcher.groupCount() >= 1) {
                        primaryTypeName = matcher.group(1);
                    } else {
                        throw new RuntimeException("Invalid Java source file without valid type found.");
                    }
                }
            }
            return primaryTypeName;
        }

        public String getFullPrimaryType() {
            return getPackage().isEmpty() ? getPrimaryTypeName() : getPackage() + "." + getPrimaryTypeName();
        }
    }
}

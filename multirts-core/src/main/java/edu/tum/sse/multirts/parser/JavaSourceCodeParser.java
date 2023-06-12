package edu.tum.sse.multirts.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import edu.tum.sse.jtec.util.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class JavaSourceCodeParser {

    // Regex based on Maven Surefire:
    // https://maven.apache.org/surefire/maven-surefire-plugin/examples/inclusion-exclusion.html
    public static String TEST_FILE_PATTERN = ".*Test.*.java";

    public static boolean isJavaFile(final Path path) {
        return path.toString().toLowerCase().endsWith(".java");
    }

    public static boolean isTestFile(final Path path) {
        return path.toString().matches(TEST_FILE_PATTERN);
    }

    public static Set<String> getAllFullyQualifiedTypeNames(final String code) {
        // TODO: we could also just return the top-level types here (getTypes())
        CompilationUnit compilationUnit = StaticJavaParser.parse(code);
        Stream<Optional<String>> typeStream = Stream.concat(Stream.concat(
                        compilationUnit
                                .findAll(ClassOrInterfaceDeclaration.class)
                                .stream()
                                .map(TypeDeclaration::getFullyQualifiedName),
                        compilationUnit
                                .findAll(EnumDeclaration.class)
                                .stream()
                                .map(TypeDeclaration::getFullyQualifiedName)
                ),
                compilationUnit
                        .findAll(AnnotationDeclaration.class)
                        .stream()
                        .map(TypeDeclaration::getFullyQualifiedName));
        return typeStream.filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    public static Set<String> getAllFullyQualifiedTypeNames(final Path path) throws IOException {
        return getAllFullyQualifiedTypeNames(IOUtils.readFromFile(path));
    }

    public static String getFullyQualifiedTypeName(Path path) throws IOException, JavaParserException {
        CompilationUnit compilationUnit = StaticJavaParser.parse(path);
        if (!compilationUnit.getPrimaryTypeName().isPresent() || !compilationUnit.getPackageDeclaration().isPresent()) {
            throw new JavaParserException();
        }
        return compilationUnit.getPackageDeclaration().get().getName() + "." + compilationUnit.getPrimaryTypeName().get();
    }

    public static class JavaParserException extends Exception {
    }
}

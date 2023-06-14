package edu.tum.sse.multirts.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.tum.sse.jtec.util.IOUtils.createFileAndEnclosingDir;
import static edu.tum.sse.jtec.util.IOUtils.writeToFile;
import static edu.tum.sse.multirts.util.CollectionUtils.newSet;
import static org.junit.jupiter.api.Assertions.*;

class JavaSourceCodeParserTest {

    @Test
    void shouldDetectJavaFiles() {
        assertTrue(JavaSourceCodeParser.isJavaFile(Paths.get("/some/Foo.java")));
        assertTrue(JavaSourceCodeParser.isJavaFile(Paths.get("/some/Foo.JAVA")));
        assertFalse(JavaSourceCodeParser.isJavaFile(Paths.get("/some/Bar.jav")));
        assertFalse(JavaSourceCodeParser.isJavaFile(Paths.get("/some/Bar.class")));
    }

    @Test
    void shouldDetectTestFiles() {
        assertTrue(JavaSourceCodeParser.isTestFile(Paths.get("/some/FooTest.java")));
        assertTrue(JavaSourceCodeParser.isTestFile(Paths.get("/some/TestFoo.java")));
        assertTrue(JavaSourceCodeParser.isTestFile(Paths.get("/some/TestsFoo.java")));
        assertTrue(JavaSourceCodeParser.isTestFile(Paths.get("/some/FooTestCase.java")));
        assertFalse(JavaSourceCodeParser.isTestFile(Paths.get("/some/Foo.JAVA")));
        assertFalse(JavaSourceCodeParser.isTestFile(Paths.get("/some/Bar.jav")));
        assertFalse(JavaSourceCodeParser.isTestFile(Paths.get("/some/Bar.class")));
    }

    @Test
    void shouldFindFullyQualifiedName() throws IOException, URISyntaxException {
        final Path javaFile = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("Sample.java")).toURI());
        final String expected = "foo.Sample";

        // when
        final String actual = JavaSourceCodeParser.getFullyQualifiedTypeName(javaFile);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldFindAllClassInterfaceEnumTypes() {
        // given
        final String code = "package a.b.c;\n" +
                "import a.b.d.Foo;\n" +
                "public class A { void foo(Foo f) {}}\n" +
                "interface D {}\n" +
                "class B { class C {} static class F {} }\n" +
                "enum E {}";
        final Set<String> expected = Stream.of("a.b.c.A", "a.b.c.D", "a.b.c.B", "a.b.c.B.C", "a.b.c.B.F", "a.b.c.E")
                .collect(Collectors.toSet());

        // when
        final Set<String> actual = JavaSourceCodeParser.getAllFullyQualifiedTypeNames(code);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldFindAllTypesFromFile() throws IOException, URISyntaxException {
        // given
        final Path javaFile = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("Sample.java")).toURI());
        final Set<String> expected = Stream.of("foo.Sample.Bar", "foo.Anno", "foo.Sample.X", "foo.Sample.Y", "foo.Sample", "foo.Sample.Foo", "foo.Bar")
                .collect(Collectors.toSet());

        // when
        final Set<String> actual = JavaSourceCodeParser.getAllFullyQualifiedTypeNames(javaFile);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldFindAllJavaTestFiles(@TempDir Path tempDir) throws IOException {
        // given
        Files.createDirectories(tempDir.resolve("a"));
        Files.createDirectories(tempDir.resolve("b"));
        writeToFile(tempDir.resolve("a/TestA.java"), "package a; public class TestA {}", false);
        writeToFile(tempDir.resolve("a/A.java"), "package a; public class A {}", false);
        writeToFile(tempDir.resolve("b/B.java"), "package b; public class B {}", false);
        writeToFile(tempDir.resolve("b/BTest.java"), "package b; public class BTest {}", false);

        // when
        Set<Path> paths = JavaSourceCodeParser.findAllJavaTestFiles(tempDir);

        // then
        assertEquals(paths.size(), 2);
        assertTrue(paths.containsAll(newSet(tempDir.resolve("a/TestA.java"), tempDir.resolve("b/BTest.java"))));
    }
}

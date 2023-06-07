package edu.tum.sse.multirts.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final Set<String> expected = Stream.of("foo.Sample.Bar", "foo.Anno", "foo.Sample.X", "foo.Sample.Y", "foo.Sample", "foo.Sample.Foo")
                .collect(Collectors.toSet());

        // when
        final Set<String> actual = JavaSourceCodeParser.getAllFullyQualifiedTypeNames(javaFile);

        // then
        assertEquals(expected, actual);
    }
}

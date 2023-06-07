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

class CppSourceCodeParserTest {

    @Test
    void shouldDetectCppFiles() {
        assertTrue(CppSourceCodeParser.isCppFile(Paths.get("/some/Foo.cpp")));
        assertTrue(CppSourceCodeParser.isCppFile(Paths.get("/some/Foo.hpp")));
        assertFalse(CppSourceCodeParser.isCppFile(Paths.get("/some/Foo.java")));
    }
}

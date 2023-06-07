package edu.tum.sse.multirts.parser;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class CppSourceCodeParser {
    /**
     * Possible C/C++-like file extensions (including headers).
     */
    public static final List<String> CPP_EXTENSIONS = Arrays.asList(
            ".c",
            ".cc",
            ".cxx",
            ".c++",
            ".cpp",
            ".ipp",
            ".tpp",
            ".tcc",
            ".inl",
            ".inc",
            ".h",
            ".hh",
            ".hpp",
            ".hxx",
            ".h++"
    );

    public static boolean isCppFile(final Path path) {
        final String fileName = path.getFileName().toString().toLowerCase();
        return fileName.contains(".") && CPP_EXTENSIONS.contains(fileName.substring(fileName.lastIndexOf(".")));
    }
}

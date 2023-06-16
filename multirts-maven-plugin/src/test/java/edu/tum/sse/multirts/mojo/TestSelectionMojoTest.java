package edu.tum.sse.multirts.mojo;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static edu.tum.sse.multirts.util.CollectionUtils.newList;
import static edu.tum.sse.multirts.util.CollectionUtils.newSet;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSelectionMojoTest {
    @Test
    void shouldReadFileMappings() throws URISyntaxException, MojoFailureException {
        // given
        Path dllMapping1 = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("dll-mapping1.csv")).toURI());
        Path dllMapping2 = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("dll-mapping2.csv")).toURI());
        Path dllMapping3 = Paths.get("dll-mapping3.csv");  // non-existing file should be ignored
        TestSelectionMojo mojo = new TestSelectionMojo();
        mojo.additionalFileMappings = newList(dllMapping1.toFile(), dllMapping2.toFile(), dllMapping3.toFile());
        Map<String, Set<String>> expected = new HashMap<>();
        expected.put("/some/path/mb2cpp/proj-x/src/test.cpp", newSet("/some/other/path/lib_test.dll"));
        expected.put("/some/path/mb2cpp/proj-x/src/xxx.cpp", newSet("/some/other/path/lib_test.dll"));
        expected.put("/some/path/mb2cpp/proj-z/src/z.cpp", newSet("/some/other/path/lib_test.dll"));
        expected.put("/some/path/mb2cpp/proj-y/src/test.cpp", newSet("/some/other/path/lib_xxx.dll"));
        expected.put("/some/path/mb2cpp/proj-z/src/lol.cpp", newSet("/some/other/path/lib_test.dll"));

        // when
        Map<String, Set<String>> fileMapping = mojo.readFileMapping();

        // then
        assertEquals(expected, fileMapping);
    }
}
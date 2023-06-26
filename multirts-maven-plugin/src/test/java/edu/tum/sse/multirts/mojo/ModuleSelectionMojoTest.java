package edu.tum.sse.multirts.mojo;

import edu.tum.sse.multirts.vcs.ChangeSetItem;
import edu.tum.sse.multirts.vcs.ChangeType;
import edu.tum.sse.multirts.vcs.GitClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Paths;
import java.util.Set;

import static edu.tum.sse.multirts.util.CollectionUtils.newList;
import static edu.tum.sse.multirts.util.CollectionUtils.newSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModuleSelectionMojoTest {

    @Mock
    GitClient gitClient;

    @Test
    void shouldReturnFilteredChangeSet() {
        // given
        when(gitClient.getDiff(any(), any())).thenReturn(newSet(new ChangeSetItem(ChangeType.MODIFIED, Paths.get("a/c/A.java")), new ChangeSetItem(ChangeType.MODIFIED, Paths.get("a/b/B.java"))));
        ModuleSelectionMojo mojo = new ModuleSelectionMojo();
        mojo.fileFilter = "^(?!.*c[\\\\|/]).*$";
        Set<ChangeSetItem> expected = newSet(new ChangeSetItem(ChangeType.MODIFIED, Paths.get("a/b/B.java")));

        // when
        Set<ChangeSetItem> actual = mojo.getChangeset(gitClient);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldBuildModulesString() {
        // given
        ModuleSelectionMojo mojo = new ModuleSelectionMojo();
        mojo.stripDirectories = newList("bla");

        // when
        String result = mojo.buildModulesString(newList(String.join(File.separator, "a", "b", "c", "pom.xml"), String.join(File.separator, "a", "b", "bla", "pom.xml")));

        // then
        assertEquals(result, String.join(File.separator, "a", "b", "c", "pom.xml") + System.lineSeparator() + String.join(File.separator, "a", "b", "pom.xml"));
    }
}

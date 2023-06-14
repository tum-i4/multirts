package edu.tum.sse.multirts.util;

import java.nio.file.Path;
import java.util.List;

public class PathUtils {

    public static boolean hasAnyExtension(Path path, List<String> extensions) {
        boolean result = false;
        for (String extension : extensions) {
            if (hasExtension(path, extension)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean hasExtension(Path path, String extension) {
        return path.getFileName().toString().toLowerCase().endsWith(extension.toLowerCase());
    }

    public static boolean hasFilename(Path path, String filename) {
        return path.getFileName().toString().equalsIgnoreCase(filename);
    }
}

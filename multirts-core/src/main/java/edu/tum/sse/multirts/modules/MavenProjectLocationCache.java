package edu.tum.sse.multirts.modules;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to make lookups for Maven projects for given file paths in a Maven reactor faster.
 */
public final class MavenProjectLocationCache {

    public static final String POM_XML = "pom.xml";

    private static final Map<Path, Path> mavenPOMCache = new HashMap<>();

    public static Path findParentPOM(Path path) {
        if (mavenPOMCache.containsKey(path)) {
            return mavenPOMCache.get(path);
        }
        Path pom = path.resolve(POM_XML);
        if (!pom.toFile().exists()) {
            pom = findParentPOM(path.getParent());
        }
        mavenPOMCache.put(path, pom);
        return pom;
    }
}

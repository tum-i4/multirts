package edu.tum.sse.multirts.modules;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class to make lookups for Maven projects for given file paths in a Maven reactor faster.
 */
public final class MavenProjectLocationCache {

    public static final String POM_XML = "pom.xml";

    private static final Map<Path, Optional<Path>> mavenPOMCache = new HashMap<>();

    public static Optional<Path> findParentPOM(Path path) {
        if (path == null) {
            return Optional.empty();
        }
        if (mavenPOMCache.containsKey(path)) {
            return mavenPOMCache.get(path);
        }
        Optional<Path> pom = Optional.of(path.resolve(POM_XML));
        if (!pom.get().toFile().exists()) {
            pom = findParentPOM(path.getParent());
        }
        mavenPOMCache.put(path, pom);
        return pom;
    }
}

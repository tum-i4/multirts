package edu.tum.sse.multirts.vcs;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Depicts a file that is part of a change set (e.g., in a commit).
 */
public final class ChangeSetItem {
    private final ChangeType changeType;
    private final Path path;

    public ChangeSetItem(final ChangeType changeType, final Path path) {
        this.changeType = changeType;
        this.path = path;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ChangeSetItem that = (ChangeSetItem) o;
        return changeType == that.changeType && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeType, path);
    }
}

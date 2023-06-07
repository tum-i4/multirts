package edu.tum.sse.multirts.rts;

import edu.tum.sse.multirts.vcs.ChangeSetItem;

import java.util.Set;

public interface TestSelectionStrategy {
    TestSelectionResult execute(final Set<ChangeSetItem> changeSet);
}

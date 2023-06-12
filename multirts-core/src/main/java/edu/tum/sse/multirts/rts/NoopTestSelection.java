package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.multirts.vcs.ChangeSetItem;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Essentially a retest-all strategy that doesn't perform any test selection.
 */
public class NoopTestSelection implements TestSelectionStrategy {

    private final static SelectionCause SELECTION_CAUSE = SelectionCause.RETEST_ALL.setReason("noop");

    private final TestReport testReport;

    public NoopTestSelection(final TestReport testReport) {
        this.testReport = testReport;
    }

    @Override
    public TestSelectionResult execute(final Set<ChangeSetItem> changeSet) {
        List<SelectedTestSuite> selectedTestSuites = testReport.getTestSuites()
                .stream()
                .map(testSuite -> new SelectedTestSuite(SELECTION_CAUSE, testSuite))
                .collect(Collectors.toList());
        return new TestSelectionResult(
                selectedTestSuites,
                Collections.emptyList());
    }
}

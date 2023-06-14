package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestSuite;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SelectedTestSuite {
    private final TestSuite testSuite;
    private final SelectionCause selectionCause;

    public SelectedTestSuite(final SelectionCause selectionCause, final TestSuite testSuite) {
        this.testSuite = testSuite;
        this.selectionCause = selectionCause;
    }

    public static List<TestSuite> toTestSuites(final List<SelectedTestSuite> selectedTestSuites) {
        return selectedTestSuites.stream().map(SelectedTestSuite::getTestSuite).collect(Collectors.toList());
    }

    public SelectionCause getSelectionCause() {
        return selectionCause;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SelectedTestSuite that = (SelectedTestSuite) o;
        return testSuite.getTestId().equals(that.testSuite.getTestId()) && selectionCause.equals(that.selectionCause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testSuite.getTestId(), selectionCause);
    }
}

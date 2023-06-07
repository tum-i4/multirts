package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestSuite;

import java.util.List;
import java.util.Objects;

public final class TestSelectionResult {
    private final List<SelectedTestSuite> selectedTestSuites;
    private final List<TestSuite> excludedTestSuites;

    public TestSelectionResult(final List<SelectedTestSuite> selectedTestSuites, final List<TestSuite> excludedTestSuites) {
        this.selectedTestSuites = selectedTestSuites;
        this.excludedTestSuites = excludedTestSuites;
    }

    public List<SelectedTestSuite> getSelectedTestSuites() {
        return selectedTestSuites;
    }

    public List<TestSuite> getExcludedTestSuites() {
        return excludedTestSuites;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TestSelectionResult that = (TestSelectionResult) o;
        return selectedTestSuites.equals(that.selectedTestSuites) && excludedTestSuites.equals(that.excludedTestSuites);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectedTestSuites, excludedTestSuites);
    }
}

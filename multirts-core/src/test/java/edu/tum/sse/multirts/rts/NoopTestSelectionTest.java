package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.jtec.reporting.TestSuite;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoopTestSelectionTest {

    @Test
    void shouldSelectAllTestSuites() {
        // given
        TestSuite ts1 = new TestSuite();
        ts1.setTestId("ts1");
        TestSuite ts2 = new TestSuite();
        ts2.setTestId("ts2");
        List<SelectedTestSuite> selectedTestSuites = Arrays.asList(
                new SelectedTestSuite(SelectionCause.RETEST_ALL.setReason("noop"), ts1), new SelectedTestSuite(SelectionCause.RETEST_ALL.setReason("noop"), ts2)
        );
        TestSelectionResult expectedResult = new TestSelectionResult(selectedTestSuites, Collections.emptyList());
        TestReport testReport = new TestReport("report-1", 0x42, 0x42, Arrays.asList(ts1, ts2));


        // when
        TestSelectionResult actualResult = new NoopTestSelection(testReport).execute(Collections.emptySet());

        // then
        assertEquals(expectedResult, actualResult);
    }
}

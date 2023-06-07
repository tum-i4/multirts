package edu.tum.sse.multirts.rts;

import edu.tum.sse.jtec.reporting.TestReport;
import edu.tum.sse.multirts.vcs.GitClient;

/**
 * Abstract representation of change-based RTS algorithms (e.g., file-level or function-level).
 */
public abstract class AbstractChangeBasedTestSelection implements TestSelectionStrategy {

    TestReport testReport;
    GitClient gitClient;

    /**
     * The "target" revision (i.e., commit SHA or branch name) into which the change set is to be integrated.
     */
    String targetRevision;

    public AbstractChangeBasedTestSelection(final TestReport testReport, final GitClient gitClient, final String targetRevision) {
        this.testReport = testReport;
        this.gitClient = gitClient;
        this.targetRevision = targetRevision;
    }
}

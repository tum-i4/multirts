package edu.tum.sse.multirts.rts;

public enum SelectionCause {
    RETEST_ALL, AFFECTED;

    private String reason;

    public String getReason() {
        return reason;
    }

    public SelectionCause setReason(final String reason) {
        this.reason = reason;
        return this;
    }
}

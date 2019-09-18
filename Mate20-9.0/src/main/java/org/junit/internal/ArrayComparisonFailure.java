package org.junit.internal;

import java.util.ArrayList;
import java.util.List;

public class ArrayComparisonFailure extends AssertionError {
    private static final long serialVersionUID = 1;
    private final List<Integer> fIndices = new ArrayList();
    private final String fMessage;

    public ArrayComparisonFailure(String message, AssertionError cause, int index) {
        this.fMessage = message;
        initCause(cause);
        addDimension(index);
    }

    public void addDimension(int index) {
        this.fIndices.add(0, Integer.valueOf(index));
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (this.fMessage != null) {
            sb.append(this.fMessage);
        }
        sb.append("arrays first differed at element ");
        for (Integer intValue : this.fIndices) {
            int each = intValue.intValue();
            sb.append("[");
            sb.append(each);
            sb.append("]");
        }
        sb.append("; ");
        sb.append(getCause().getMessage());
        return sb.toString();
    }

    public String toString() {
        return getMessage();
    }
}

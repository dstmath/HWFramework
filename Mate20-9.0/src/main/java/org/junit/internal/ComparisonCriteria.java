package org.junit.internal;

import java.lang.reflect.Array;
import java.util.Arrays;
import org.junit.Assert;

public abstract class ComparisonCriteria {
    /* access modifiers changed from: protected */
    public abstract void assertElementsEqual(Object obj, Object obj2);

    public void arrayEquals(String message, Object expecteds, Object actuals) throws ArrayComparisonFailure {
        String header;
        if (expecteds != actuals) {
            if (!Arrays.deepEquals(new Object[]{expecteds}, new Object[]{actuals})) {
                if (message == null) {
                    header = "";
                } else {
                    header = message + ": ";
                }
                int expectedsLength = assertArraysAreSameLength(expecteds, actuals, header);
                for (int i = 0; i < expectedsLength; i++) {
                    Object expected = Array.get(expecteds, i);
                    Object actual = Array.get(actuals, i);
                    if (!isArray(expected) || !isArray(actual)) {
                        try {
                            assertElementsEqual(expected, actual);
                        } catch (AssertionError e) {
                            throw new ArrayComparisonFailure(header, e, i);
                        }
                    } else {
                        try {
                            arrayEquals(message, expected, actual);
                        } catch (ArrayComparisonFailure e2) {
                            e2.addDimension(i);
                            throw e2;
                        }
                    }
                }
            }
        }
    }

    private boolean isArray(Object expected) {
        return expected != null && expected.getClass().isArray();
    }

    private int assertArraysAreSameLength(Object expecteds, Object actuals, String header) {
        if (expecteds == null) {
            Assert.fail(header + "expected array was null");
        }
        if (actuals == null) {
            Assert.fail(header + "actual array was null");
        }
        int actualsLength = Array.getLength(actuals);
        int expectedsLength = Array.getLength(expecteds);
        if (actualsLength != expectedsLength) {
            Assert.fail(header + "array lengths differed, expected.length=" + expectedsLength + " actual.length=" + actualsLength);
        }
        return expectedsLength;
    }
}

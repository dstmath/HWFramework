package org.junit.internal;

import org.junit.Assert;

public class ExactComparisonCriteria extends ComparisonCriteria {
    /* access modifiers changed from: protected */
    @Override // org.junit.internal.ComparisonCriteria
    public void assertElementsEqual(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }
}

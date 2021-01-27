package org.hamcrest.internal;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;

public class SelfDescribingValue<T> implements SelfDescribing {
    private T value;

    public SelfDescribingValue(T value2) {
        this.value = value2;
    }

    @Override // org.hamcrest.SelfDescribing
    public void describeTo(Description description) {
        description.appendValue(this.value);
    }
}

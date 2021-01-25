package org.junit.rules;

import org.junit.runner.Description;

public class TestName extends TestWatcher {
    private String name;

    /* access modifiers changed from: protected */
    @Override // org.junit.rules.TestWatcher
    public void starting(Description d) {
        this.name = d.getMethodName();
    }

    public String getMethodName() {
        return this.name;
    }
}

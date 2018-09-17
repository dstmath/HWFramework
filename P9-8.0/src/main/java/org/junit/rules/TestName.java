package org.junit.rules;

import org.junit.runner.Description;

public class TestName extends TestWatcher {
    private String name;

    protected void starting(Description d) {
        this.name = d.getMethodName();
    }

    public String getMethodName() {
        return this.name;
    }
}

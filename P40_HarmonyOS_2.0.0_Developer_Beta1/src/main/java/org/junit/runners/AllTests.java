package org.junit.runners;

import org.junit.internal.runners.SuiteMethod;

public class AllTests extends SuiteMethod {
    public AllTests(Class<?> klass) throws Throwable {
        super(klass);
    }
}

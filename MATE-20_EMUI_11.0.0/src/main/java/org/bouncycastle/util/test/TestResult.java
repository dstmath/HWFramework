package org.bouncycastle.util.test;

public interface TestResult {
    Throwable getException();

    boolean isSuccessful();

    String toString();
}

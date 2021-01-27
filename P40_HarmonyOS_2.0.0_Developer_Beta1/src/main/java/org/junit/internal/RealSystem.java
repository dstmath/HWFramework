package org.junit.internal;

import java.io.PrintStream;

public class RealSystem implements JUnitSystem {
    @Override // org.junit.internal.JUnitSystem
    @Deprecated
    public void exit(int code) {
        System.exit(code);
    }

    @Override // org.junit.internal.JUnitSystem
    public PrintStream out() {
        return System.out;
    }
}

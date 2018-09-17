package org.junit.internal.runners;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class InitializationError extends Exception {
    private static final long serialVersionUID = 1;
    private final List<Throwable> fErrors;

    public InitializationError(List<Throwable> errors) {
        this.fErrors = errors;
    }

    public InitializationError(Throwable... errors) {
        this(Arrays.asList(errors));
    }

    public InitializationError(String string) {
        this(new Exception(string));
    }

    public List<Throwable> getCauses() {
        return this.fErrors;
    }
}

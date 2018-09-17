package com.android.internal.util;

public class FunctionalUtils {

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    private FunctionalUtils() {
    }
}

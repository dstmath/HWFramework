package com.android.internal.util.function.pooled;

public final class ArgumentPlaceholder<R> {
    static final ArgumentPlaceholder<?> INSTANCE = new ArgumentPlaceholder<>();

    private ArgumentPlaceholder() {
    }

    public String toString() {
        return "_";
    }
}

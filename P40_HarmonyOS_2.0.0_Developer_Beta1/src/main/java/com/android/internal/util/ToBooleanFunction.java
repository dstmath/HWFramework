package com.android.internal.util;

@FunctionalInterface
public interface ToBooleanFunction<T> {
    boolean apply(T t);
}

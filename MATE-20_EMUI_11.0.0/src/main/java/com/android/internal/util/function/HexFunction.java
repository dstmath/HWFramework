package com.android.internal.util.function;

public interface HexFunction<A, B, C, D, E, F, R> {
    R apply(A a, B b, C c, D d, E e, F f);
}

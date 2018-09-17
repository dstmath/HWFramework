package com.android.org.bouncycastle.util;

public interface Selector<T> extends Cloneable {
    Object clone();

    boolean match(T t);
}

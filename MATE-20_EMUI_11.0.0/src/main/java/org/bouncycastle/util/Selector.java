package org.bouncycastle.util;

public interface Selector<T> extends Cloneable {
    @Override // java.lang.Object
    Object clone();

    boolean match(T t);
}

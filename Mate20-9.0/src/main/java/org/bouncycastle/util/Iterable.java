package org.bouncycastle.util;

import java.util.Iterator;

public interface Iterable<T> extends Iterable<T> {
    Iterator<T> iterator();
}

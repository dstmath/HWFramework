package org.hamcrest.internal;

import java.lang.reflect.Array;
import java.util.Iterator;

public class ArrayIterator implements Iterator<Object> {
    private final Object array;
    private int currentIndex = 0;

    public ArrayIterator(Object array) {
        if (array.getClass().isArray()) {
            this.array = array;
            return;
        }
        throw new IllegalArgumentException("not an array");
    }

    public boolean hasNext() {
        return this.currentIndex < Array.getLength(this.array);
    }

    public Object next() {
        Object obj = this.array;
        int i = this.currentIndex;
        this.currentIndex = i + 1;
        return Array.get(obj, i);
    }

    public void remove() {
        throw new UnsupportedOperationException("cannot remove items from an array");
    }
}

package android.util;

import java.util.AbstractSet;
import java.util.Iterator;

public final class FastImmutableArraySet<T> extends AbstractSet<T> {
    T[] mContents;
    FastIterator<T> mIterator;

    private static final class FastIterator<T> implements Iterator<T> {
        private final T[] mContents;
        int mIndex;

        public FastIterator(T[] contents) {
            this.mContents = contents;
        }

        public boolean hasNext() {
            return this.mIndex != this.mContents.length;
        }

        public T next() {
            Object[] objArr = this.mContents;
            int i = this.mIndex;
            this.mIndex = i + 1;
            return objArr[i];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public FastImmutableArraySet(T[] contents) {
        this.mContents = contents;
    }

    public Iterator<T> iterator() {
        FastIterator<T> it = this.mIterator;
        if (it == null) {
            it = new FastIterator(this.mContents);
            this.mIterator = it;
            return it;
        }
        it.mIndex = 0;
        return it;
    }

    public int size() {
        return this.mContents.length;
    }
}

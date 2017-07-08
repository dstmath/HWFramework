package java.util;

import java.lang.reflect.Array;
import sun.util.logging.PlatformLogger;

public abstract class AbstractCollection<E> implements Collection<E> {
    private static final int MAX_ARRAY_SIZE = 2147483639;

    public abstract Iterator<E> iterator();

    public abstract int size();

    protected AbstractCollection() {
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object o) {
        Iterator<E> it = iterator();
        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    return true;
                }
            }
        }
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                return true;
            }
        }
        return false;
    }

    public Object[] toArray() {
        Object[] r = new Object[size()];
        Iterator<E> it = iterator();
        for (int i = 0; i < r.length; i++) {
            if (!it.hasNext()) {
                return Arrays.copyOf(r, i);
            }
            r[i] = it.next();
        }
        if (it.hasNext()) {
            r = finishToArray(r, it);
        }
        return r;
    }

    public <T> T[] toArray(T[] a) {
        T[] r;
        int size = size();
        if (a.length >= size) {
            r = a;
        } else {
            Object[] r2 = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
        }
        Iterator<E> it = iterator();
        int i = 0;
        while (i < r.length) {
            if (it.hasNext()) {
                r[i] = it.next();
                i++;
            } else {
                if (a == r) {
                    r[i] = null;
                } else if (a.length < i) {
                    return Arrays.copyOf((Object[]) r, i);
                } else {
                    System.arraycopy((Object) r, 0, (Object) a, 0, i);
                    if (a.length > i) {
                        a[i] = null;
                    }
                }
                return a;
            }
        }
        if (it.hasNext()) {
            r = finishToArray(r, it);
        }
        return r;
    }

    private static <T> T[] finishToArray(T[] r, Iterator<?> it) {
        Object[] r2;
        int i = r.length;
        while (it.hasNext()) {
            int cap = r2.length;
            if (i == cap) {
                int newCap = ((cap >> 1) + cap) + 1;
                if (newCap - MAX_ARRAY_SIZE > 0) {
                    newCap = hugeCapacity(cap + 1);
                }
                r2 = Arrays.copyOf(r2, newCap);
            }
            int i2 = i + 1;
            r2[i] = it.next();
            i = i2;
        }
        return i == r2.length ? r2 : Arrays.copyOf(r2, i);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError("Required array size too large");
        } else if (minCapacity > MAX_ARRAY_SIZE) {
            return PlatformLogger.OFF;
        } else {
            return MAX_ARRAY_SIZE;
        }
    }

    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        Iterator<E> it = iterator();
        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    it.remove();
                    return true;
                }
            }
        }
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    public void clear() {
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
    }

    public String toString() {
        Iterator<E> it = iterator();
        if (!it.hasNext()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (true) {
            Object e = it.next();
            if (e == this) {
                e = "(this Collection)";
            }
            sb.append(e);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }
}

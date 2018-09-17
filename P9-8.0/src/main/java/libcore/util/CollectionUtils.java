package libcore.util;

import java.lang.ref.Reference;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> Iterable<T> dereferenceIterable(final Iterable<? extends Reference<T>> iterable, final boolean trim) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                final Iterable iterable = iterable;
                final boolean z = trim;
                return new Iterator<T>() {
                    private final Iterator<? extends Reference<T>> delegate = iterable.iterator();
                    private T next;
                    private boolean removeIsOkay;

                    private void computeNext() {
                        this.removeIsOkay = false;
                        while (this.next == null && this.delegate.hasNext()) {
                            this.next = ((Reference) this.delegate.next()).get();
                            if (z && this.next == null) {
                                this.delegate.remove();
                            }
                        }
                    }

                    public boolean hasNext() {
                        computeNext();
                        return this.next != null;
                    }

                    public T next() {
                        if (hasNext()) {
                            T result = this.next;
                            this.removeIsOkay = true;
                            this.next = null;
                            return result;
                        }
                        throw new IllegalStateException();
                    }

                    public void remove() {
                        if (this.removeIsOkay) {
                            this.delegate.remove();
                            return;
                        }
                        throw new IllegalStateException();
                    }
                };
            }
        };
    }

    public static <T> void removeDuplicates(List<T> list, Comparator<? super T> comparator) {
        Collections.sort(list, comparator);
        int j = 1;
        for (int i = 1; i < list.size(); i++) {
            if (comparator.compare(list.get(j - 1), list.get(i)) != 0) {
                int j2 = j + 1;
                list.set(j, list.get(i));
                j = j2;
            }
        }
        if (j < list.size()) {
            list.subList(j, list.size()).clear();
        }
    }
}

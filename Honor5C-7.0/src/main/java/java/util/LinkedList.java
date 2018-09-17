package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.function.Consumer;

public class LinkedList<E> extends AbstractSequentialList<E> implements List<E>, Deque<E>, Cloneable, Serializable {
    private static final long serialVersionUID = 876323262645176354L;
    transient Node<E> first;
    transient Node<E> last;
    transient int size;

    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr;

        private DescendingIterator() {
            this.itr = new ListItr(LinkedList.this, LinkedList.this.size());
        }

        public boolean hasNext() {
            return this.itr.hasPrevious();
        }

        public E next() {
            return this.itr.previous();
        }

        public void remove() {
            this.itr.remove();
        }
    }

    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1024;
        static final int MAX_BATCH = 33554432;
        int batch;
        Node<E> current;
        int est;
        int expectedModCount;
        final LinkedList<E> list;

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s = this.est;
            if (s >= 0) {
                return s;
            }
            LinkedList<E> lst = this.list;
            if (lst == null) {
                this.est = 0;
                return 0;
            }
            this.expectedModCount = lst.modCount;
            this.current = lst.first;
            s = lst.size;
            this.est = s;
            return s;
        }

        public long estimateSize() {
            return (long) getEst();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Spliterator<E> trySplit() {
            int s = getEst();
            if (s > 1) {
                Node<E> p = this.current;
                if (p != null) {
                    int j;
                    int n = this.batch + BATCH_UNIT;
                    if (n > s) {
                        n = s;
                    }
                    if (n > MAX_BATCH) {
                        n = MAX_BATCH;
                    }
                    Object[] a = new Object[n];
                    int i = 0;
                    while (true) {
                        j = i + 1;
                        a[i] = p.item;
                        p = p.next;
                        if (p == null || j >= n) {
                            this.current = p;
                            this.batch = j;
                            this.est = s - j;
                        } else {
                            i = j;
                        }
                    }
                    this.current = p;
                    this.batch = j;
                    this.est = s - j;
                    return Spliterators.spliterator(a, 0, j, 16);
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int n = getEst();
            if (n > 0) {
                Node<E> p = this.current;
                if (p != null) {
                    this.current = null;
                    this.est = 0;
                    do {
                        E e = p.item;
                        p = p.next;
                        action.accept(e);
                        if (p == null) {
                            break;
                        }
                        n--;
                    } while (n > 0);
                }
            }
            if (this.list.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (getEst() > 0) {
                Node<E> p = this.current;
                if (p != null) {
                    this.est--;
                    E e = p.item;
                    this.current = p.next;
                    action.accept(e);
                    if (this.list.modCount == this.expectedModCount) {
                        return true;
                    }
                    throw new ConcurrentModificationException();
                }
            }
            return false;
        }

        public int characteristics() {
            return 16464;
        }
    }

    private class ListItr implements ListIterator<E> {
        private int expectedModCount;
        private Node<E> lastReturned;
        private Node<E> next;
        private int nextIndex;
        final /* synthetic */ LinkedList this$0;

        ListItr(LinkedList this$0, int index) {
            Node node = null;
            this.this$0 = this$0;
            this.lastReturned = null;
            this.expectedModCount = this.this$0.modCount;
            if (index != this$0.size) {
                node = this$0.node(index);
            }
            this.next = node;
            this.nextIndex = index;
        }

        public boolean hasNext() {
            return this.nextIndex < this.this$0.size;
        }

        public E next() {
            checkForComodification();
            if (hasNext()) {
                this.lastReturned = this.next;
                this.next = this.next.next;
                this.nextIndex++;
                return this.lastReturned.item;
            }
            throw new NoSuchElementException();
        }

        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (hasPrevious()) {
                Node node = this.next == null ? this.this$0.last : this.next.prev;
                this.next = node;
                this.lastReturned = node;
                this.nextIndex--;
                return this.lastReturned.item;
            }
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return this.nextIndex;
        }

        public int previousIndex() {
            return this.nextIndex - 1;
        }

        public void remove() {
            checkForComodification();
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }
            Node<E> lastNext = this.lastReturned.next;
            this.this$0.unlink(this.lastReturned);
            if (this.next == this.lastReturned) {
                this.next = lastNext;
            } else {
                this.nextIndex--;
            }
            this.lastReturned = null;
            this.expectedModCount++;
        }

        public void set(E e) {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            }
            checkForComodification();
            this.lastReturned.item = e;
        }

        public void add(E e) {
            checkForComodification();
            this.lastReturned = null;
            if (this.next == null) {
                this.this$0.linkLast(e);
            } else {
                this.this$0.linkBefore(e, this.next);
            }
            this.nextIndex++;
            this.expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (this.this$0.modCount == this.expectedModCount && this.nextIndex < this.this$0.size) {
                action.accept(this.next.item);
                this.lastReturned = this.next;
                this.next = this.next.next;
                this.nextIndex++;
            }
            checkForComodification();
        }

        final void checkForComodification() {
            if (this.this$0.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    public LinkedList() {
        this.size = 0;
    }

    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    private void linkFirst(E e) {
        Node<E> f = this.first;
        Node<E> newNode = new Node(null, e, f);
        this.first = newNode;
        if (f == null) {
            this.last = newNode;
        } else {
            f.prev = newNode;
        }
        this.size++;
        this.modCount++;
    }

    void linkLast(E e) {
        Node<E> l = this.last;
        Node<E> newNode = new Node(l, e, null);
        this.last = newNode;
        if (l == null) {
            this.first = newNode;
        } else {
            l.next = newNode;
        }
        this.size++;
        this.modCount++;
    }

    void linkBefore(E e, Node<E> succ) {
        Node<E> pred = succ.prev;
        Node<E> newNode = new Node(pred, e, succ);
        succ.prev = newNode;
        if (pred == null) {
            this.first = newNode;
        } else {
            pred.next = newNode;
        }
        this.size++;
        this.modCount++;
    }

    private E unlinkFirst(Node<E> f) {
        E element = f.item;
        Node<E> next = f.next;
        f.item = null;
        f.next = null;
        this.first = next;
        if (next == null) {
            this.last = null;
        } else {
            next.prev = null;
        }
        this.size--;
        this.modCount++;
        return element;
    }

    private E unlinkLast(Node<E> l) {
        E element = l.item;
        Node<E> prev = l.prev;
        l.item = null;
        l.prev = null;
        this.last = prev;
        if (prev == null) {
            this.first = null;
        } else {
            prev.next = null;
        }
        this.size--;
        this.modCount++;
        return element;
    }

    E unlink(Node<E> x) {
        E element = x.item;
        Node<E> next = x.next;
        Node<E> prev = x.prev;
        if (prev == null) {
            this.first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }
        if (next == null) {
            this.last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }
        x.item = null;
        this.size--;
        this.modCount++;
        return element;
    }

    public E getFirst() {
        Node<E> f = this.first;
        if (f != null) {
            return f.item;
        }
        throw new NoSuchElementException();
    }

    public E getLast() {
        Node<E> l = this.last;
        if (l != null) {
            return l.item;
        }
        throw new NoSuchElementException();
    }

    public E removeFirst() {
        Node<E> f = this.first;
        if (f != null) {
            return unlinkFirst(f);
        }
        throw new NoSuchElementException();
    }

    public E removeLast() {
        Node<E> l = this.last;
        if (l != null) {
            return unlinkLast(l);
        }
        throw new NoSuchElementException();
    }

    public void addFirst(E e) {
        linkFirst(e);
    }

    public void addLast(E e) {
        linkLast(e);
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public int size() {
        return this.size;
    }

    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    public boolean remove(Object o) {
        Node<E> x;
        if (o == null) {
            for (x = this.first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (x = this.first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        return addAll(this.size, c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        int i = 0;
        checkPositionIndex(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0) {
            return false;
        }
        Node succ;
        Node<E> pred;
        if (index == this.size) {
            succ = null;
            pred = this.last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }
        int length = a.length;
        while (i < length) {
            E o = a[i];
            E e = o;
            Node<E> newNode = new Node(pred, o, null);
            if (pred == null) {
                this.first = newNode;
            } else {
                pred.next = newNode;
            }
            pred = newNode;
            i++;
        }
        if (succ == null) {
            this.last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }
        this.size += numNew;
        this.modCount++;
        return true;
    }

    public void clear() {
        Node<E> x = this.first;
        while (x != null) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        this.last = null;
        this.first = null;
        this.size = 0;
        this.modCount++;
    }

    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }

    public void add(int index, E element) {
        checkPositionIndex(index);
        if (index == this.size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index));
        }
    }

    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    private boolean isElementIndex(int index) {
        return index >= 0 && index < this.size;
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= this.size;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + this.size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index)) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index)) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    Node<E> node(int index) {
        Node<E> x;
        int i;
        if (index < (this.size >> 1)) {
            x = this.first;
            for (i = 0; i < index; i++) {
                x = x.next;
            }
            return x;
        }
        x = this.last;
        for (i = this.size - 1; i > index; i--) {
            x = x.prev;
        }
        return x;
    }

    public int indexOf(Object o) {
        int index = 0;
        Node<E> x;
        if (o == null) {
            for (x = this.first; x != null; x = x.next) {
                if (x.item == null) {
                    return index;
                }
                index++;
            }
        } else {
            for (x = this.first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        int index = this.size;
        Node<E> x;
        if (o == null) {
            for (x = this.last; x != null; x = x.prev) {
                index--;
                if (x.item == null) {
                    return index;
                }
            }
        } else {
            for (x = this.last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item)) {
                    return index;
                }
            }
        }
        return -1;
    }

    public E peek() {
        Node<E> f = this.first;
        if (f == null) {
            return null;
        }
        return f.item;
    }

    public E element() {
        return getFirst();
    }

    public E poll() {
        Node<E> f = this.first;
        if (f == null) {
            return null;
        }
        return unlinkFirst(f);
    }

    public E remove() {
        return removeFirst();
    }

    public boolean offer(E e) {
        return add(e);
    }

    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    public E peekFirst() {
        Node<E> f = this.first;
        if (f == null) {
            return null;
        }
        return f.item;
    }

    public E peekLast() {
        Node<E> l = this.last;
        if (l == null) {
            return null;
        }
        return l.item;
    }

    public E pollFirst() {
        Node<E> f = this.first;
        if (f == null) {
            return null;
        }
        return unlinkFirst(f);
    }

    public E pollLast() {
        Node<E> l = this.last;
        if (l == null) {
            return null;
        }
        return unlinkLast(l);
    }

    public void push(E e) {
        addFirst(e);
    }

    public E pop() {
        return removeFirst();
    }

    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    public boolean removeLastOccurrence(Object o) {
        Node<E> x;
        if (o == null) {
            for (x = this.last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (x = this.last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(this, index);
    }

    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    private LinkedList<E> superClone() {
        try {
            return (LinkedList) super.clone();
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public Object clone() {
        LinkedList<E> clone = superClone();
        clone.last = null;
        clone.first = null;
        clone.size = 0;
        clone.modCount = 0;
        for (Node<E> x = this.first; x != null; x = x.next) {
            clone.add(x.item);
        }
        return clone;
    }

    public Object[] toArray() {
        Object[] result = new Object[this.size];
        Node<E> x = this.first;
        int i = 0;
        while (x != null) {
            int i2 = i + 1;
            result[i] = x.item;
            x = x.next;
            i = i2;
        }
        return result;
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < this.size) {
            a = (Object[]) Array.newInstance(a.getClass().getComponentType(), this.size);
        }
        T[] result = a;
        Node<E> x = this.first;
        int i = 0;
        while (x != null) {
            int i2 = i + 1;
            result[i] = x.item;
            x = x.next;
            i = i2;
        }
        if (a.length > this.size) {
            a[this.size] = null;
        }
        return a;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        for (Node<E> x = this.first; x != null; x = x.next) {
            s.writeObject(x.item);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        for (int i = 0; i < size; i++) {
            linkLast(s.readObject());
        }
    }

    public Spliterator<E> spliterator() {
        return new LLSpliterator(this, -1, 0);
    }
}

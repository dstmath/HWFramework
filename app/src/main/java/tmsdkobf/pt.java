package tmsdkobf;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;

/* compiled from: Unknown */
public class pt<T> {
    private TreeMap<T, LinkedList<T>> ID;

    public pt(Comparator<T> comparator) {
        this.ID = null;
        this.ID = new TreeMap(comparator);
    }

    private LinkedList<T> hl() {
        return new LinkedList();
    }

    public synchronized void add(T t) {
        LinkedList linkedList = (LinkedList) this.ID.get(t);
        if (linkedList == null) {
            linkedList = hl();
            this.ID.put(t, linkedList);
        }
        linkedList.addLast(t);
    }

    public synchronized void clear() {
        this.ID.clear();
    }

    public synchronized T get() {
        if (isEmpty()) {
            return null;
        }
        return ((LinkedList) this.ID.get(this.ID.firstKey())).getFirst();
    }

    public synchronized boolean isEmpty() {
        return this.ID.isEmpty();
    }

    public synchronized T poll() {
        if (isEmpty()) {
            return null;
        }
        Object firstKey = this.ID.firstKey();
        LinkedList linkedList = (LinkedList) this.ID.get(firstKey);
        T poll = linkedList.poll();
        if (linkedList.size() <= 0) {
            this.ID.remove(firstKey);
        }
        return poll;
    }
}

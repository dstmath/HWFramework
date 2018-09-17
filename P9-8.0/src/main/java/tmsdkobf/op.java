package tmsdkobf;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;

public class op<T> {
    private TreeMap<T, LinkedList<T>> IC = null;

    public op(Comparator<T> comparator) {
        this.IC = new TreeMap(comparator);
    }

    private LinkedList<T> he() {
        return new LinkedList();
    }

    public synchronized void add(T t) {
        LinkedList linkedList = (LinkedList) this.IC.get(t);
        if (linkedList == null) {
            linkedList = he();
            this.IC.put(t, linkedList);
        }
        linkedList.addLast(t);
    }

    public synchronized void clear() {
        this.IC.clear();
    }

    public synchronized boolean isEmpty() {
        return this.IC.isEmpty();
    }

    /* JADX WARNING: Missing block: B:7:0x0020, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized T poll() {
        if (isEmpty()) {
            return null;
        }
        Object firstKey = this.IC.firstKey();
        LinkedList linkedList = (LinkedList) this.IC.get(firstKey);
        T poll = linkedList.poll();
        if (linkedList.size() <= 0) {
            this.IC.remove(firstKey);
        }
    }
}

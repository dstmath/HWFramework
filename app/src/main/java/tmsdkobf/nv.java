package tmsdkobf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/* compiled from: Unknown */
public class nv<E> {
    private final int Dw;
    private final ConcurrentLinkedQueue<E> wh;

    public nv(int i) {
        this.wh = new ConcurrentLinkedQueue();
        this.Dw = i;
    }

    public boolean addAll(Collection<? extends E> collection) {
        synchronized (this.wh) {
            if (collection != null) {
                boolean addAll = this.wh.addAll(collection);
                return addAll;
            }
            return false;
        }
    }

    public void clear() {
        this.wh.clear();
    }

    public Queue<E> fw() {
        return this.wh;
    }

    public ArrayList<E> fx() {
        ArrayList<E> arrayList;
        synchronized (this.wh) {
            arrayList = new ArrayList();
            Iterator it = this.wh.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
        }
        return arrayList;
    }

    public boolean offer(E e) {
        synchronized (this.wh) {
            if (e != null) {
                if (this.wh.size() >= this.Dw) {
                    this.wh.poll();
                }
                boolean offer = this.wh.offer(e);
                return offer;
            }
            return false;
        }
    }

    public boolean removeAll(Collection<?> collection) {
        boolean removeAll;
        synchronized (this.wh) {
            removeAll = this.wh.removeAll(collection);
        }
        return removeAll;
    }

    public int size() {
        return this.wh.size();
    }
}

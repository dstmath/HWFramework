package tmsdkobf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class mr<E> {
    private final int AY;
    private final ConcurrentLinkedQueue<E> tk = new ConcurrentLinkedQueue();

    public mr(int i) {
        this.AY = i;
    }

    public boolean addAll(Collection<? extends E> collection) {
        synchronized (this.tk) {
            if (collection != null) {
                boolean addAll = this.tk.addAll(collection);
                return addAll;
            }
            return false;
        }
    }

    public void clear() {
        this.tk.clear();
    }

    public Queue<E> fc() {
        return this.tk;
    }

    public ArrayList<E> fd() {
        ArrayList<E> arrayList;
        synchronized (this.tk) {
            arrayList = new ArrayList();
            Iterator it = this.tk.iterator();
            while (it.hasNext()) {
                arrayList.add(it.next());
            }
        }
        return arrayList;
    }

    public boolean offer(E e) {
        synchronized (this.tk) {
            if (e != null) {
                if (this.tk.size() >= this.AY) {
                    this.tk.poll();
                }
                boolean offer = this.tk.offer(e);
                return offer;
            }
            return false;
        }
    }

    public boolean removeAll(Collection<?> collection) {
        boolean removeAll;
        synchronized (this.tk) {
            removeAll = this.tk.removeAll(collection);
        }
        return removeAll;
    }

    public int size() {
        return this.tk.size();
    }
}

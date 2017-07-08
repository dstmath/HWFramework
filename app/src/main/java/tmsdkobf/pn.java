package tmsdkobf;

import java.util.Iterator;
import java.util.LinkedHashSet;

/* compiled from: Unknown */
public class pn<T> {
    private int Dw;
    private LinkedHashSet<T> Io;

    public pn(int i) {
        this.Dw = -1;
        this.Io = new LinkedHashSet();
        this.Dw = i;
    }

    public synchronized boolean c(T t) {
        return this.Io.contains(t);
    }

    public synchronized T poll() {
        if (this.Io != null) {
            Iterator it = this.Io.iterator();
            if (it != null && it.hasNext()) {
                T next = it.next();
                this.Io.remove(next);
                return next;
            }
        }
        return null;
    }

    public synchronized void push(T t) {
        if (this.Io.size() >= this.Dw) {
            poll();
        }
        this.Io.add(t);
    }
}

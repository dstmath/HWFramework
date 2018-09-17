package tmsdkobf;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class ok<T> {
    private int AY = -1;
    private LinkedHashSet<T> Ip = new LinkedHashSet();

    public ok(int i) {
        this.AY = i;
    }

    public synchronized boolean d(T t) {
        return this.Ip.contains(t);
    }

    /* JADX WARNING: Missing block: B:6:0x0007, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized T poll() {
        if (this.Ip != null) {
            Iterator it = this.Ip.iterator();
            if (it != null && it.hasNext()) {
                T next = it.next();
                this.Ip.remove(next);
                return next;
            }
        }
    }

    public synchronized void push(T t) {
        if (this.Ip.size() >= this.AY) {
            poll();
        }
        this.Ip.add(t);
    }
}

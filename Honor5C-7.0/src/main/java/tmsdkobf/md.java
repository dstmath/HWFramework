package tmsdkobf;

import java.util.Iterator;
import java.util.LinkedList;

/* compiled from: Unknown */
public class md {
    private static md Az;
    private LinkedList<a> Ay;

    /* compiled from: Unknown */
    public interface a {
        void eJ();
    }

    public md() {
        this.Ay = new LinkedList();
    }

    public static md eH() {
        if (Az == null) {
            synchronized (md.class) {
                if (Az == null) {
                    Az = new md();
                }
            }
        }
        return Az;
    }

    public void a(a aVar) {
        synchronized (this.Ay) {
            this.Ay.add(aVar);
        }
    }

    public void b(a aVar) {
        synchronized (this.Ay) {
            this.Ay.remove(aVar);
        }
    }

    public void eI() {
        synchronized (this.Ay) {
            LinkedList linkedList = (LinkedList) this.Ay.clone();
        }
        if (linkedList != null) {
            na.s("ccrManager", "copy.size() : " + linkedList.size());
            Iterator it = linkedList.iterator();
            while (it.hasNext()) {
                ((a) it.next()).eJ();
            }
        }
    }
}

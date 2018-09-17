package tmsdkobf;

import java.util.Iterator;
import java.util.LinkedList;

public class kx {
    private static kx xX;
    private LinkedList<a> xW = new LinkedList();

    public interface a {
        void dT();
    }

    public static kx dR() {
        if (xX == null) {
            Class cls = kx.class;
            synchronized (kx.class) {
                if (xX == null) {
                    xX = new kx();
                }
            }
        }
        return xX;
    }

    public void a(a aVar) {
        synchronized (this.xW) {
            this.xW.add(aVar);
        }
    }

    public void b(a aVar) {
        synchronized (this.xW) {
            this.xW.remove(aVar);
        }
    }

    public synchronized void dS() {
        try {
            LinkedList linkedList;
            synchronized (this.xW) {
                linkedList = (LinkedList) this.xW.clone();
            }
            if (linkedList != null) {
                kv.n("ccrManager", "copy.size() : " + linkedList.size());
                Iterator it = linkedList.iterator();
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    if (aVar != null) {
                        aVar.dT();
                    }
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
        }
    }
}

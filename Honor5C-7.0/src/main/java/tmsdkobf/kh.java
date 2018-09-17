package tmsdkobf;

import android.os.Debug;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdkobf.kg.a;
import tmsdkobf.qg.c;

/* compiled from: Unknown */
public class kh implements a, ki {
    private HashMap<Thread, c> vD;
    private qg.a vE;
    private final ThreadGroup vi;
    private final AtomicInteger vj;

    public kh() {
        this.vj = new AtomicInteger(1);
        this.vD = new HashMap();
        this.vi = new ThreadGroup("TMS_FREE_POOL_" + vI.getAndIncrement());
    }

    public Thread a(Runnable runnable, String str, long j) {
        if (str == null || str.length() == 0) {
            str = runnable.getClass().getName();
        }
        Thread kgVar = new kg(this.vi, runnable, "FreeThread-" + this.vj.getAndIncrement() + "-" + str, j);
        kgVar.a(this);
        if (kgVar.isDaemon()) {
            kgVar.setDaemon(false);
        }
        if (kgVar.getPriority() != 5) {
            kgVar.setPriority(5);
        }
        return kgVar;
    }

    public void a(Thread thread, Runnable runnable) {
        c cVar = new c();
        cVar.Jk = 2;
        cVar.dI = ((kg) thread).bI();
        cVar.name = thread.getName();
        cVar.priority = thread.getPriority();
        cVar.Jm = -1;
        cVar.Jn = -1;
        this.vD.put(thread, cVar);
        if (this.vE != null) {
            this.vE.a(cVar, activeCount());
        }
    }

    public void a(qg.a aVar) {
        this.vE = aVar;
    }

    public int activeCount() {
        return this.vD.size();
    }

    public void b(Thread thread, Runnable runnable) {
        c cVar = (c) this.vD.remove(thread);
        if (cVar != null) {
            cVar.Jm = System.currentTimeMillis() - cVar.Jm;
            cVar.Jn = Debug.threadCpuTimeNanos() - cVar.Jn;
            if (this.vE != null) {
                this.vE.b(cVar);
            }
        }
    }

    public void beforeExecute(Thread thread, Runnable runnable) {
        c cVar = (c) this.vD.get(thread);
        if (cVar != null) {
            if (this.vE != null) {
                this.vE.a(cVar);
            }
            cVar.Jm = System.currentTimeMillis();
            cVar.Jn = Debug.threadCpuTimeNanos();
        }
    }
}

package tmsdkobf;

import android.os.Debug;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdkobf.iz.a;
import tmsdkobf.pd.c;

public class ja implements a, jb {
    private HashMap<Thread, c> sJ = new HashMap();
    private pd.a sK;
    private final ThreadGroup so = new ThreadGroup("TMS_FREE_POOL_" + sO.getAndIncrement());
    private final AtomicInteger sp = new AtomicInteger(1);

    public Thread a(Runnable runnable, String str, long j) {
        if (str == null || str.length() == 0) {
            str = runnable.getClass().getName();
        }
        Thread izVar = new iz(this.so, runnable, "FreeThread-" + this.sp.getAndIncrement() + "-" + str, j);
        izVar.a(this);
        if (izVar.isDaemon()) {
            izVar.setDaemon(false);
        }
        if (izVar.getPriority() != 5) {
            izVar.setPriority(5);
        }
        return izVar;
    }

    public void a(Thread thread, Runnable runnable) {
        c cVar = new c();
        cVar.Jk = 2;
        cVar.eA = ((iz) thread).bL();
        cVar.name = thread.getName();
        cVar.priority = thread.getPriority();
        cVar.Jm = -1;
        cVar.Jn = -1;
        this.sJ.put(thread, cVar);
        if (this.sK != null) {
            this.sK.a(cVar, activeCount());
        }
    }

    public void a(pd.a aVar) {
        this.sK = aVar;
    }

    public int activeCount() {
        return this.sJ.size();
    }

    public void b(Thread thread, Runnable runnable) {
        c cVar = (c) this.sJ.remove(thread);
        if (cVar != null) {
            cVar.Jm = System.currentTimeMillis() - cVar.Jm;
            cVar.Jn = Debug.threadCpuTimeNanos() - cVar.Jn;
            if (this.sK != null) {
                this.sK.b(cVar);
            }
        }
    }

    public void beforeExecute(Thread thread, Runnable runnable) {
        c cVar = (c) this.sJ.get(thread);
        if (cVar != null) {
            if (this.sK != null) {
                this.sK.a(cVar);
            }
            cVar.Jm = System.currentTimeMillis();
            cVar.Jn = Debug.threadCpuTimeNanos();
        }
    }
}

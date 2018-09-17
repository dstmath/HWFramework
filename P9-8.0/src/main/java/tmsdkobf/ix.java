package tmsdkobf;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import tmsdk.common.creator.BaseManagerC;
import tmsdkobf.pd.c;

public class ix extends BaseManagerC implements tmsdkobf.iw.a, pd {
    private static long sC = 0;
    private static long sD = 0;
    private static tmsdkobf.pd.a sG = new tmsdkobf.pd.a() {
        public void a(c cVar) {
            Iterator it = ix.st.iterator();
            while (it.hasNext()) {
                ((tmsdkobf.pd.a) it.next()).a(cVar);
            }
        }

        public void a(c cVar, int i) {
            Iterator it = ix.st.iterator();
            while (it.hasNext()) {
                ((tmsdkobf.pd.a) it.next()).a(cVar, i);
            }
        }

        public void b(c cVar) {
            Iterator it = ix.st.iterator();
            while (it.hasNext()) {
                ((tmsdkobf.pd.a) it.next()).b(cVar);
            }
        }
    };
    private static ArrayList<tmsdkobf.pd.a> st = new ArrayList();
    private boolean isActive = false;
    private Object mLock = new Object();
    private HandlerThread sA;
    private b sB;
    private volatile boolean sE = false;
    private ja sF;
    private ArrayList<tmsdkobf.pd.b> ss = new ArrayList();
    protected PriorityBlockingQueue<Runnable> su = new PriorityBlockingQueue(5);
    protected LinkedList<a> sv = new LinkedList();
    protected ArrayList<a> sw = new ArrayList();
    protected HashMap<a, Thread> sx = new HashMap();
    private int sy;
    protected iw sz = null;

    class a implements Comparable<a>, Runnable {
        private c sI = new c();

        public a(int i, Runnable runnable, String str, long j, boolean z, Object obj) {
            if (str == null || str.length() == 0) {
                str = runnable.getClass().getName();
            }
            this.sI.Jk = 1;
            this.sI.priority = i;
            this.sI.name = str;
            this.sI.eA = j;
            this.sI.Jp = runnable;
            this.sI.Jo = z;
            this.sI.Jq = obj;
            this.sI.Jl = System.currentTimeMillis();
        }

        /* renamed from: a */
        public int compareTo(a aVar) {
            int abs = (int) (Math.abs(System.currentTimeMillis() - this.sI.Jl) / 200);
            int i = this.sI.priority;
            if (abs > 0) {
                i += abs;
            }
            return aVar.sI.priority - i;
        }

        public c cm() {
            return this.sI;
        }

        public void run() {
            if (this.sI != null && this.sI.Jp != null) {
                this.sI.Jp.run();
            }
        }
    }

    class b extends Handler {
        public b(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = 0;
            switch (message.what) {
                case 1:
                    removeMessages(message.what);
                    if (ix.this.cg()) {
                        mb.n("ThreadPool", "thread pool is pause");
                        long currentTimeMillis = System.currentTimeMillis();
                        if ((ix.sC <= 0 ? 1 : 0) == 0) {
                            if (Math.abs(ix.sD - currentTimeMillis) <= ix.sC) {
                                i = 1;
                            }
                            if (i == 0) {
                                mb.n("ThreadPool", "thread pool is auto wakeup");
                                ix.this.cf();
                            }
                        }
                        sendEmptyMessageDelayed(1, 1000);
                        return;
                    }
                    ix.this.cd();
                    return;
                default:
                    return;
            }
        }
    }

    private int cb() {
        int availableProcessors = (Runtime.getRuntime().availableProcessors() * 4) + 2;
        return availableProcessors <= 16 ? availableProcessors : 16;
    }

    private int cc() {
        return cb() * 2;
    }

    private void cd() {
        synchronized (this.mLock) {
            if (!this.sv.isEmpty()) {
                Iterator it = this.sv.iterator();
                if (it != null && it.hasNext()) {
                    a aVar = (a) it.next();
                    it.remove();
                    ce();
                    this.sz.execute(aVar);
                    Iterator it2 = st.iterator();
                    while (it2.hasNext()) {
                        ((tmsdkobf.pd.a) it2.next()).a(aVar.cm(), this.sz.getActiveCount());
                    }
                }
            }
            if (!this.sv.isEmpty()) {
                this.sB.sendEmptyMessage(1);
            }
        }
    }

    private void ce() {
        if (this.sz.getCorePoolSize() < this.sy) {
            this.sz.setCorePoolSize(this.sy);
            this.sz.setMaximumPoolSize(this.sy);
        }
    }

    private boolean cg() {
        return this.sE;
    }

    private void ch() {
        if (this.sF == null) {
            this.sF = new ja();
            this.sF.a(new tmsdkobf.pd.a() {
                public void a(c cVar) {
                    Iterator it = ix.st.iterator();
                    while (it.hasNext()) {
                        ((tmsdkobf.pd.a) it.next()).a(cVar);
                    }
                }

                public void a(c cVar, int i) {
                    Iterator it = ix.st.iterator();
                    while (it.hasNext()) {
                        ((tmsdkobf.pd.a) it.next()).a(cVar, i);
                    }
                }

                public void b(c cVar) {
                    Iterator it = ix.st.iterator();
                    while (it.hasNext()) {
                        ((tmsdkobf.pd.a) it.next()).b(cVar);
                    }
                }
            });
        }
    }

    public static tmsdkobf.pd.a ci() {
        return sG;
    }

    public HandlerThread a(String str, int i, long j) {
        return iy.a(str, i, j);
    }

    public Thread a(Runnable runnable, String str, long j) {
        ch();
        return this.sF.a(runnable, str, j);
    }

    public void a(int i, Runnable runnable, String str, long j, boolean z, Object obj) {
        synchronized (this.mLock) {
            a aVar = new a(i, runnable, str, j, z, obj);
            this.sv.add(aVar);
            this.sw.add(aVar);
            this.sB.sendEmptyMessage(1);
        }
    }

    public void a(Runnable runnable) {
        synchronized (this.mLock) {
            Thread c = c(runnable);
            if (c == null) {
                b(runnable);
            } else {
                c.interrupt();
            }
        }
    }

    public void a(Runnable runnable, String str, long j, boolean z, Object obj) {
        a(5, runnable, str, j, z, obj);
    }

    public void afterExecute(Runnable runnable, Throwable th) {
        synchronized (this.mLock) {
            a aVar = (a) runnable;
            Iterator it = this.sx.keySet().iterator();
            if (it != null) {
                Object obj = null;
                while (it.hasNext()) {
                    a aVar2 = (a) it.next();
                    if (aVar2 != null && aVar2.equals(aVar)) {
                        it.remove();
                        obj = 1;
                        break;
                    }
                }
                if (obj != null) {
                    aVar.cm().Jm = System.currentTimeMillis() - aVar.cm().Jm;
                    aVar.cm().Jn = Debug.threadCpuTimeNanos() - aVar.cm().Jn;
                    Iterator it2 = st.iterator();
                    while (it2.hasNext()) {
                        ((tmsdkobf.pd.a) it2.next()).b(aVar.cm());
                    }
                }
            }
            int activeCount = this.sz.getActiveCount();
            int size = this.sz.getQueue().size();
            int corePoolSize = this.sz.getCorePoolSize();
            if (activeCount == 1 && size == 0) {
                if (corePoolSize > 0) {
                    this.sy = cb();
                    this.sz.setCorePoolSize(0);
                    this.sz.setMaximumPoolSize(this.sy + 2);
                    mb.n("ThreadPool", "shrink core pool size: " + this.sz.getCorePoolSize());
                }
                Iterator it3 = this.ss.iterator();
                while (it3.hasNext()) {
                    ((tmsdkobf.pd.b) it3.next()).hG();
                }
                this.isActive = false;
            }
        }
    }

    public void b(Runnable runnable, String str, long j, boolean z, Object obj) {
        synchronized (this.mLock) {
            Object aVar = new a(Integer.MAX_VALUE, runnable, str, j, z, obj);
            this.sw.add(aVar);
            this.sz.execute(aVar);
            if (this.sz.getActiveCount() >= this.sy && this.sy < cc()) {
                this.sy++;
                this.sz.setCorePoolSize(this.sy);
                this.sz.setMaximumPoolSize(this.sy);
                mb.n("ThreadPool", "expand urgent core pool size: " + this.sy);
            } else {
                ce();
            }
            Iterator it = st.iterator();
            while (it.hasNext()) {
                ((tmsdkobf.pd.a) it.next()).a(aVar.cm(), this.sz.getActiveCount());
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x000f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean b(Runnable runnable) {
        if (runnable == null) {
            return false;
        }
        synchronized (this.mLock) {
            Iterator it = this.sw.iterator();
            if (it != null) {
                Runnable runnable2 = null;
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    if (aVar != null && aVar.cm() != null && runnable.equals(aVar.cm().Jp)) {
                        it.remove();
                        Object runnable22 = aVar;
                        break;
                    }
                }
                if (runnable22 != null) {
                    this.sz.remove(runnable22);
                    return true;
                }
            }
        }
    }

    public void beforeExecute(Thread thread, Runnable runnable) {
        synchronized (this.mLock) {
            Iterator it = this.sw.iterator();
            if (it != null) {
                a aVar = (a) runnable;
                int i = aVar.cm().priority;
                if (i < 1) {
                    i = 1;
                } else if (i > 10) {
                    i = 10;
                }
                thread.setPriority(i);
                Object obj = null;
                while (it.hasNext()) {
                    a aVar2 = (a) it.next();
                    if (aVar2 != null && aVar2.equals(aVar)) {
                        it.remove();
                        obj = 1;
                        break;
                    }
                }
                if (obj != null) {
                    Iterator it2;
                    if (!this.isActive) {
                        it2 = this.ss.iterator();
                        while (it2.hasNext()) {
                            ((tmsdkobf.pd.b) it2.next()).hF();
                        }
                    }
                    it2 = st.iterator();
                    while (it2.hasNext()) {
                        ((tmsdkobf.pd.a) it2.next()).a(aVar.cm());
                    }
                    aVar.cm().Jm = System.currentTimeMillis();
                    aVar.cm().Jn = Debug.threadCpuTimeNanos();
                    this.sx.put(aVar, thread);
                    thread.setName(aVar.cm().name);
                    this.isActive = true;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0013, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Thread c(Runnable runnable) {
        if (runnable == null) {
            return null;
        }
        synchronized (this.mLock) {
            Iterator it = this.sx.keySet().iterator();
            if (it != null) {
                Object obj = null;
                while (it.hasNext()) {
                    a aVar = (a) it.next();
                    if (aVar != null && aVar.cm() != null && runnable.equals(aVar.cm().Jp)) {
                        a obj2 = aVar;
                        break;
                    }
                }
                if (obj2 != null) {
                    Thread thread = (Thread) this.sx.get(obj2);
                    return thread;
                }
            }
        }
    }

    public void cf() {
        synchronized (this.mLock) {
            this.sE = false;
            sD = 0;
            sC = 0;
            mb.n("ThreadPool", "wake up threa pool");
        }
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.sy = cb();
        this.sz = new iw(0, this.sy + 2, 3, TimeUnit.SECONDS, this.su, new CallerRunsPolicy());
        this.sz.a(this);
        this.sA = new HandlerThread("TMS_THREAD_POOL_HANDLER");
        this.sA.start();
        this.sB = new b(this.sA.getLooper());
    }
}

package tmsdkobf;

import android.os.Debug;
import android.os.HandlerThread;
import java.util.HashMap;
import tmsdkobf.pd.a;
import tmsdkobf.pd.c;

public class iy {
    private static HashMap<Thread, c> sJ = new HashMap();
    private static a sK;
    private static iz.a sL = new iz.a() {
        public void a(Thread thread, Runnable runnable) {
            c cVar = new c();
            cVar.Jk = 3;
            cVar.eA = ((pc) thread).bL();
            cVar.name = thread.getName();
            cVar.priority = thread.getPriority();
            cVar.Jm = -1;
            cVar.Jn = -1;
            iy.sJ.put(thread, cVar);
            iy.cn();
            iy.sK.a(cVar, iy.activeCount());
        }

        public void b(Thread thread, Runnable runnable) {
            c cVar = (c) iy.sJ.remove(thread);
            if (cVar != null) {
                cVar.Jm = System.currentTimeMillis() - cVar.Jm;
                cVar.Jn = Debug.threadCpuTimeNanos() - cVar.Jn;
                iy.cn();
                iy.sK.b(cVar);
            }
        }

        public void beforeExecute(Thread thread, Runnable runnable) {
            c cVar = (c) iy.sJ.get(thread);
            if (cVar != null) {
                iy.cn();
                iy.sK.a(cVar);
                cVar.Jm = System.currentTimeMillis();
                cVar.Jn = Debug.threadCpuTimeNanos();
            }
        }
    };

    public static HandlerThread a(String str, int i, long j) {
        return new pc(str, i, j);
    }

    public static int activeCount() {
        return sJ.size();
    }

    private static void cn() {
        if (sK == null) {
            sK = ix.ci();
        }
    }

    public static iz.a co() {
        return sL;
    }
}

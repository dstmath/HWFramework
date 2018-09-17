package com.huawei.android.pushagent.utils.threadpool;

import com.huawei.android.pushagent.utils.d.c;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class a {
    private static Map<AsyncExec$ThreadType, ExecutorService> el;

    private static synchronized void on() {
        synchronized (a.class) {
            if (el == null) {
                Map hashMap = new HashMap();
                ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new b("IO"));
                threadPoolExecutor.allowCoreThreadTimeOut(true);
                ThreadPoolExecutor threadPoolExecutor2 = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new b("Net"));
                threadPoolExecutor2.allowCoreThreadTimeOut(true);
                ThreadPoolExecutor threadPoolExecutor3 = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new b("SeqNet"));
                threadPoolExecutor3.allowCoreThreadTimeOut(true);
                ThreadPoolExecutor threadPoolExecutor4 = new ThreadPoolExecutor(3, 5, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new b("Cal"));
                threadPoolExecutor4.allowCoreThreadTimeOut(true);
                ThreadPoolExecutor threadPoolExecutor5 = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new b("Seq"));
                ThreadPoolExecutor threadPoolExecutor6 = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), new b("Report"));
                hashMap.put(AsyncExec$ThreadType.IO, threadPoolExecutor);
                hashMap.put(AsyncExec$ThreadType.NETWORK, threadPoolExecutor2);
                hashMap.put(AsyncExec$ThreadType.SEQNETWORK, threadPoolExecutor3);
                hashMap.put(AsyncExec$ThreadType.CALCULATION, threadPoolExecutor4);
                hashMap.put(AsyncExec$ThreadType.SEQUENCE, threadPoolExecutor5);
                hashMap.put(AsyncExec$ThreadType.REPORT_SEQ, threadPoolExecutor6);
                el = hashMap;
            }
        }
    }

    static {
        on();
    }

    static void oo(Runnable runnable, AsyncExec$ThreadType asyncExec$ThreadType) {
        if (runnable != null) {
            ExecutorService executorService = (ExecutorService) el.get(asyncExec$ThreadType);
            if (executorService != null) {
                executorService.execute(new c(runnable));
            } else {
                c.sj("PushLog2951", "no executor for type: " + asyncExec$ThreadType);
            }
        }
    }

    public static void op(Runnable runnable) {
        oo(runnable, AsyncExec$ThreadType.IO);
    }

    public static void oq(Runnable runnable) {
        oo(runnable, AsyncExec$ThreadType.NETWORK);
    }

    public static void os(Runnable runnable) {
        oo(runnable, AsyncExec$ThreadType.SEQNETWORK);
    }

    public static void or(Runnable runnable) {
        oo(runnable, AsyncExec$ThreadType.REPORT_SEQ);
    }
}

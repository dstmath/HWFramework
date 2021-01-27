package com.android.server.rms.dump;

import android.content.Context;
import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class DumpActivityManagerService {
    private static final int LOCK_AMS_TIME = 20000;
    private static final String TAG = "DumpActivityManagerService";
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(10));

    private DumpActivityManagerService() {
    }

    public static void lockAms(Context context, PrintWriter pw, String[] args) {
        pw.println("--dump-ActivityManagerService");
        threadPool.execute(new Runnable() {
            /* class com.android.server.rms.dump.DumpActivityManagerService.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                synchronized (HwActivityManagerService.self()) {
                    Log.d(DumpActivityManagerService.TAG, "new thread:--dump-ActivityManagerService");
                    try {
                        Thread.currentThread();
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        Log.d(DumpActivityManagerService.TAG, "InterruptedException");
                    }
                }
            }
        });
    }
}

package com.android.server.rms.dump;

import android.content.Context;
import android.util.Log;
import com.android.server.am.HwActivityManagerService;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import java.io.PrintWriter;

public final class DumpActivityManagerService {
    private static final String TAG = "DumpActivityManagerService";

    public static void lockAms(Context context, PrintWriter pw, String[] args) {
        pw.println("--dump-ActivityManagerService");
        new Thread(new Runnable() {
            public void run() {
                synchronized (HwActivityManagerService.self()) {
                    Log.d(DumpActivityManagerService.TAG, "new thread:--dump-ActivityManagerService");
                    try {
                        Thread.currentThread();
                        Thread.sleep(HwRecentsTaskUtils.MAX_REMOVE_TASK_TIME);
                    } catch (InterruptedException e) {
                        Log.d(DumpActivityManagerService.TAG, "InterruptedException");
                    }
                }
            }
        }).start();
    }
}

package com.huawei.ace.activity;

import android.app.Application;
import com.huawei.ace.runtime.ALog;
import com.huawei.ace.runtime.AceEnv;
import com.huawei.ace.runtime.DeviceInfoHelper;
import com.huawei.ace.runtime.LibraryLoader;

public class AceApplication extends Application {
    private static final String LOG_TAG = "AceApplication";
    private static Thread loadSoThread;

    @Override // android.app.Application
    public void onCreate() {
        ALog.setLogger(new Logger());
        ALog.i(LOG_TAG, "AceApplication::onCreate called");
        if (DeviceInfoHelper.isWatchType(getApplicationContext())) {
            LibraryLoader.setUseWatchLib();
            loadSoThread = new Thread($$Lambda$AceApplication$8DY_OpkxxDjT8zVFTlKm9rXtYuU.INSTANCE);
            try {
                loadSoThread.setPriority(10);
            } catch (IllegalArgumentException unused) {
                ALog.w(LOG_TAG, "set load so thread priority failed");
            }
            loadSoThread.start();
        }
        LibraryLoader.setJoinLibarayLoadCallback($$Lambda$AceApplication$Uv6AHZkNnRpi2uV41klAFusvio.INSTANCE);
        super.onCreate();
    }

    static /* synthetic */ void lambda$onCreate$0() {
        if (!AceEnv.getInstance().isLibraryLoaded()) {
            ALog.e(LOG_TAG, "Load ace library failed.");
        }
    }

    static /* synthetic */ void lambda$onCreate$1() {
        try {
            if (loadSoThread != null) {
                loadSoThread.join();
            }
        } catch (InterruptedException unused) {
            ALog.e(LOG_TAG, "join load so thread failed");
        } finally {
            loadSoThread = null;
        }
    }
}

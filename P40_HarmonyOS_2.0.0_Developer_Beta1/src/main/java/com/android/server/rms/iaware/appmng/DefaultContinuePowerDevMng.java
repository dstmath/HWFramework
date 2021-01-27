package com.android.server.rms.iaware.appmng;

import android.content.Context;
import android.os.HandlerThread;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultContinuePowerDevMng {
    public static final int LAUNCHER_PRELOAD_OPT = 101;
    private static final Object LOCK = new Object();
    public static final int PRELOADAPP_COMM_FAIL = -1;
    public static final int PRELOADAPP_PASS = 0;
    public static final int PRELOADAPP_PROCEXIST_FAIL = -2;
    private static DefaultContinuePowerDevMng sInstance = null;

    public static DefaultContinuePowerDevMng getInstance() {
        DefaultContinuePowerDevMng defaultContinuePowerDevMng;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new DefaultContinuePowerDevMng();
            }
            defaultContinuePowerDevMng = sInstance;
        }
        return defaultContinuePowerDevMng;
    }

    public void keyDownEvent(int keyCode, boolean down) {
    }

    public void init(HandlerThread handlerThread, Context context) {
    }

    public boolean startPreLoadApplication(String pkg, int userId, int preloadType) {
        return false;
    }
}

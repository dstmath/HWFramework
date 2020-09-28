package com.huawei.hsm.permission.monitor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class PermRecordHandler extends Handler {
    private static final String TAG = "PermRecordHandler";
    private static PermRecordHandler sPermHandler;

    private PermRecordHandler(Looper looper) {
        super(looper);
    }

    public static synchronized PermRecordHandler getHandleInstance() {
        PermRecordHandler permRecordHandler;
        synchronized (PermRecordHandler.class) {
            if (sPermHandler == null) {
                HandlerThread handlerThread = new HandlerThread(TAG);
                handlerThread.start();
                sPermHandler = new PermRecordHandler(handlerThread.getLooper());
            }
            permRecordHandler = sPermHandler;
        }
        return permRecordHandler;
    }

    public synchronized void accessPermission(String pkg, String permission, boolean isAllow, int uid, String desStr) {
    }

    public synchronized void accessPermission(int uid, int pid, int permType, String desStr) {
    }
}

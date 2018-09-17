package com.huawei.systemmanager.rainbow.comm.request.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class HttpsWakeLockHelper {
    private static final String TAG = "HttpsWakeLockHelper";
    private static final long WAKELOCK_TIME_OUT = 15000;
    private WakeLock mWakeLock = null;

    public void createWakeLockAndAcquire(Context context) {
        if (context == null) {
            Log.e(TAG, "The SystemManagerApplication context is null");
            return;
        }
        if (this.mWakeLock == null) {
            this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, TAG);
            this.mWakeLock.setReferenceCounted(false);
        }
        if (this.mWakeLock != null) {
            this.mWakeLock.acquire(WAKELOCK_TIME_OUT);
        }
    }

    public void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }
}

package com.huawei.android.app;

import android.app.AppOpsManager;

public class AppOpsManagerExt {
    public static final int OP_CAMERA = 26;
    public static final int OP_TOAST_WINDOW = 45;

    public static int checkOpNoThrow(AppOpsManager appOps, int op, int uid, String packageName) {
        if (appOps == null) {
            return -1;
        }
        return appOps.checkOpNoThrow(op, uid, packageName);
    }

    public static void startWatchingActive(AppOpsManager appOps, int[] ops, OnOpActiveChangedListenerEx callback) {
        if (appOps != null && callback != null) {
            appOps.startWatchingActive(ops, callback.getListener());
        }
    }

    public static void stopWatchingActive(AppOpsManager appOps, OnOpActiveChangedListenerEx callback) {
        if (appOps != null && callback != null) {
            appOps.stopWatchingActive(callback.getListener());
        }
    }

    public static class OnOpActiveChangedListenerEx {
        private AppOpsManager.OnOpActiveChangedListener mListener = new AppOpsManager.OnOpActiveChangedListener() {
            /* class com.huawei.android.app.AppOpsManagerExt.OnOpActiveChangedListenerEx.AnonymousClass1 */

            public void onOpActiveChanged(int code, int uid, String packageName, boolean active) {
                OnOpActiveChangedListenerEx.this.onOpActiveChanged(code, uid, packageName, active);
            }
        };

        public void onOpActiveChanged(int code, int uid, String packageName, boolean active) {
        }

        public AppOpsManager.OnOpActiveChangedListener getListener() {
            return this.mListener;
        }
    }
}

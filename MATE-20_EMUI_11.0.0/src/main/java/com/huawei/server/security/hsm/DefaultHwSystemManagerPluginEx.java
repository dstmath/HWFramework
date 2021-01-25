package com.huawei.server.security.hsm;

import android.content.Context;

public class DefaultHwSystemManagerPluginEx {
    private static final Object SERVICE_LOCK = new Object();
    private static DefaultHwSystemManagerPluginEx sInstance;

    public static DefaultHwSystemManagerPluginEx getInstance(Context context) {
        DefaultHwSystemManagerPluginEx defaultHwSystemManagerPluginEx;
        synchronized (SERVICE_LOCK) {
            if (sInstance == null) {
                sInstance = new DefaultHwSystemManagerPluginEx();
            }
            defaultHwSystemManagerPluginEx = sInstance;
        }
        return defaultHwSystemManagerPluginEx;
    }

    public boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }
}

package com.huawei.server.security.hsm;

import android.content.Context;

public class HwSystemManagerPluginEx extends DefaultHwSystemManagerPluginEx {
    private static final Object LOCK = new Object();
    private static volatile HwSystemManagerPluginEx sInstance;
    private Context mContext;

    private HwSystemManagerPluginEx(Context context) {
        this.mContext = context;
    }

    public static HwSystemManagerPluginEx getInstance(Context context) {
        HwSystemManagerPluginEx hwSystemManagerPluginEx;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwSystemManagerPluginEx(context);
            }
            hwSystemManagerPluginEx = sInstance;
        }
        return hwSystemManagerPluginEx;
    }

    public boolean shouldPreventStartComponent(int type, String calleePackage, int callerUid, int callerPid, String callerPackage, int userId) {
        return HwSystemManagerPlugin.getInstance(this.mContext).shouldPreventStartComponent(type, calleePackage, callerUid, callerPid, callerPackage, userId);
    }
}

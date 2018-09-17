package com.android.server;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import huawei.android.net.IConnectivityExManager.Stub;

public class HwConnectivityExService extends Stub {
    private static final String TAG = "HwConnectivityExService";
    static String mSmartKeyguardLevel = "normal_level";
    static boolean useCtrlSocket = SystemProperties.getBoolean("ro.config.hw_useCtrlSocket", false);
    private Context mContext;

    public HwConnectivityExService(Context context) {
        this.mContext = context;
    }

    public void setSmartKeyguardLevel(String level) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        setStaticSmartKeyguardLevel(level);
    }

    private static void setStaticSmartKeyguardLevel(String level) {
        mSmartKeyguardLevel = level;
        Slog.d(TAG, "set mSmartKeyguardLevel = " + mSmartKeyguardLevel);
    }

    private static void setUseCtrlSocketStatic(boolean flag) {
        useCtrlSocket = flag;
    }

    public void setUseCtrlSocket(boolean flag) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", TAG);
        setUseCtrlSocketStatic(flag);
        Slog.d(TAG, "set useCtrlSocket = " + useCtrlSocket);
    }
}

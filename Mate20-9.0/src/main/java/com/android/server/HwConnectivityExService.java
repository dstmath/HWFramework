package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.util.Slog;
import huawei.android.net.IConnectivityExManager;

public class HwConnectivityExService extends IConnectivityExManager.Stub {
    private static final String TAG = "HwConnectivityExService";
    static String mSmartKeyguardLevel = "normal_level";
    static boolean useCtrlSocket = SystemProperties.getBoolean("ro.config.hw_useCtrlSocket", false);
    private Context mContext;
    private boolean mIsFixedAddress = false;

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

    public void setApIpv4AddressFixed(boolean isFixed) {
        Slog.d(TAG, "Calling pid is " + Binder.getCallingPid() + " isFixed " + isFixed);
        int checkCallingOrSelfPermission = this.mContext.checkCallingOrSelfPermission("com.huawei.wifi.permission.WIFI_APIPV4FIXED");
        this.mContext.getPackageManager();
        if (checkCallingOrSelfPermission != 0) {
            Slog.e(TAG, "No com.huawei.wifi.permission.WIFI_APIPV4FIXED permission");
        } else {
            this.mIsFixedAddress = isFixed;
        }
    }

    public boolean isApIpv4AddressFixed() {
        Slog.d(TAG, "Calling pid is " + Binder.getCallingPid() + " isFixed " + this.mIsFixedAddress);
        int checkCallingOrSelfPermission = this.mContext.checkCallingOrSelfPermission("com.huawei.wifi.permission.WIFI_APIPV4FIXED");
        this.mContext.getPackageManager();
        if (checkCallingOrSelfPermission == 0) {
            return this.mIsFixedAddress;
        }
        Slog.e(TAG, "No com.huawei.wifi.permission.WIFI_APIPV4FIXED permission");
        return false;
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

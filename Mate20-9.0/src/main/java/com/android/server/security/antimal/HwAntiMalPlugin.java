package com.android.server.security.antimal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.security.IHwAntiMalPlugin;

public class HwAntiMalPlugin extends IHwAntiMalPlugin.Stub implements IHwSecurityPlugin {
    private static final String ANTIMAL_KEY_LAUNCHER_NAME = "launcher_name";
    private static final String ANTIMAL_KEY_PROTECT_TYPE = "protect_type";
    private static final int ANTIMAL_PROTECT_TYPE_DEFAULT = 0;
    private static final int ANTIMAL_PROTECT_TYPE_LAUNCHER = 2;
    private static final int ANTIMAL_PROTECT_TYPE_NEW_DIVECE = 1;
    public static final IHwSecurityPlugin.Creator CREATOR = new IHwSecurityPlugin.Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            Slog.d(HwAntiMalPlugin.TAG, "createPlugin");
            return new HwAntiMalPlugin(context);
        }

        public String getPluginPermission() {
            return HwAntiMalPlugin.MANAGE_USE_SECURITY;
        }
    };
    private static final String DEFAULT_HW_LAUNCHER = "com.huawei.android.launcher";
    private static final boolean IS_CHINA_AREA = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    private static final boolean IS_DOMESTIC_RELEASE = "1".equalsIgnoreCase(SystemProperties.get("ro.logsystem.usertype", "3"));
    private static final boolean IS_ROOT = "0".equalsIgnoreCase(SystemProperties.get("ro.secure", "1"));
    private static final boolean IS_TABLET = "tablet".equalsIgnoreCase(SystemProperties.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final String TAG = "HwAntiMalPlugin";
    private static final boolean mAntimalProtection = "true".equalsIgnoreCase(SystemProperties.get("ro.product.antimal_protection", "true"));
    private Context mContext;
    private HwAntiMalStatus mHwAntiMalStatus = null;

    public HwAntiMalPlugin(Context context) {
        this.mContext = context;
        this.mHwAntiMalStatus = new HwAntiMalStatus(this.mContext);
        this.mHwAntiMalStatus.checkIfTopAppsDisabled();
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.security.antimal.HwAntiMalPlugin, android.os.IBinder] */
    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public boolean isAntiMalProtectionOn(Bundle params) {
        if (params == null) {
            Slog.e(TAG, "Invalid input params!");
            return false;
        } else if (!IS_CHINA_AREA) {
            Slog.d(TAG, "Not in valid area!");
            return false;
        } else if (!mAntimalProtection) {
            Slog.d(TAG, "AntimalProtection control is close!");
            return false;
        } else {
            int protect_type = params.getInt(ANTIMAL_KEY_PROTECT_TYPE, 0);
            String launcher = params.getString(ANTIMAL_KEY_LAUNCHER_NAME, "");
            Slog.d(TAG, "protect_type:" + protect_type + " launcher:" + launcher);
            return isAntiMalProtectionOnByType(protect_type, launcher);
        }
    }

    private boolean isAntiMalProtectionOnByType(int type, String launcher) {
        if (type == 2) {
            if (TextUtils.isEmpty(launcher)) {
                Slog.d(TAG, "Input is empty!");
                launcher = "com.huawei.android.launcher";
            }
            try {
                return !this.mHwAntiMalStatus.isAllowedSetHomeActivityForAntiMalInternal(this.mContext.getPackageManager().getPackageInfo(launcher, 0), 0, false);
            } catch (PackageManager.NameNotFoundException e) {
                Slog.d(TAG, "Not found name:" + launcher);
                return true;
            }
        } else if (IS_ROOT) {
            Slog.d(TAG, "Device is root !");
            return false;
        } else if (!IS_DOMESTIC_RELEASE) {
            Slog.d(TAG, "Beta version !");
            return false;
        } else if (!IS_TABLET) {
            return this.mHwAntiMalStatus.isNeedRestrictForAntimal(true);
        } else {
            Slog.d(TAG, "Is tablet!");
            return false;
        }
    }
}

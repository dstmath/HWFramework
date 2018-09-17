package com.android.server.pm;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.notch.HwNotchScreenWhiteConfig;

public final class HwPackageManagerServiceEx implements IHwPackageManagerServiceEx {
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", "").equals(""));
    static final String TAG = "HwPackageManagerServiceEx";
    final Context mContext;
    IHwPackageManagerInner mIPmsInner = null;

    public HwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        this.mIPmsInner = pms;
        this.mContext = context;
    }

    public void updateNochScreenWhite(String packageName, String flag, int versionCode) {
        if (IS_NOTCH_PROP) {
            HwNotchScreenWhiteConfig.getInstance().updateVersionCodeInNoch(packageName, flag, versionCode);
        }
    }
}

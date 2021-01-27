package com.huawei.internal.telephony;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

public class PackageManagerExt {
    private static final int INVALID_UID = -1;
    private Context mContext;
    private PackageManager mPm = this.mContext.getPackageManager();

    public PackageManagerExt(Context context) {
        this.mContext = context;
    }

    public Resources getResourcesForApplicationAsUser(String appPackage, int userId) {
        try {
            if (this.mPm == null) {
                return null;
            }
            return this.mPm.getResourcesForApplicationAsUser(appPackage, userId);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        PackageManager packageManager = this.mPm;
        if (packageManager != null) {
            return packageManager.getText(packageName, resid, appInfo);
        }
        return null;
    }

    public int getPackageUidAsUser(String packageName, int userId) {
        PackageManager packageManager = this.mPm;
        if (packageManager == null) {
            return -1;
        }
        try {
            return packageManager.getPackageUidAsUser(packageName, userId);
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }
}

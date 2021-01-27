package com.huawei.information;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.android.app.HiViewEx;
import java.util.Locale;

public class HwDeviceInfo {
    private static final int HIVIEW_ID = 992770101;
    private static final Object LOCK = new Object();
    private static final String PNAME_ID = "signtool";
    private static final String PVERSION_ID = "11.0.1";
    private static final String TAG = "HwDeviceInfo";
    private static final String UNKNOWN_PACKAGE = "unknown_package";

    private static native String getEmmcId();

    static {
        try {
            System.loadLibrary("hwdeviceinfo");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Load libarary hwdeviceinfo failed");
        }
    }

    public static String getEMMCID() {
        String emmcId;
        synchronized (LOCK) {
            try {
                String callPackageName = getCallPackageName();
                reportInformation(callPackageName, "getEmmcId");
                Log.d(TAG, "HwDeviceInfo 64bits so, getEMMCID,callPackageName:" + callPackageName);
                emmcId = getEmmcId();
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Libarary hwdeviceinfo get emmc id failed");
                return null;
            } catch (Throwable th) {
                throw th;
            }
        }
        return emmcId;
    }

    private static String getCallPackageName() {
        Application application = ActivityThreadEx.currentApplication();
        if (application == null) {
            Log.d(TAG, "Get current application failed");
            return UNKNOWN_PACKAGE;
        }
        Context context = application.getApplicationContext();
        if (context == null) {
            Log.d(TAG, "Get context failed");
            return UNKNOWN_PACKAGE;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.d(TAG, "getPackageManager failed");
            return UNKNOWN_PACKAGE;
        }
        String[] pkgList = packageManager.getPackagesForUid(Binder.getCallingUid());
        if (pkgList != null && pkgList.length != 0) {
            return pkgList[0];
        }
        Log.d(TAG, "Package list is null");
        return UNKNOWN_PACKAGE;
    }

    private static void reportInformation(String packageName, String funcName) {
        Log.d(TAG, "packageName:" + packageName + "funcName:" + funcName);
        HiViewEx.report(HiViewEx.byJson((int) HIVIEW_ID, String.format(Locale.ROOT, "{PNAMEID:%s,PVERSIONID:%s,PACKAGENAME:%s,FUNCNAME:%s}", PNAME_ID, PVERSION_ID, packageName, funcName)));
    }
}

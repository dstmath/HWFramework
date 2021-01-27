package com.huawei.security.fileprotect;

import android.util.Log;

public class HwDataProtect {
    private static final int NATIVE_FAILED = -1;
    private static final int NATIVE_OK = 0;
    private static final String TAG = "HwDataProtect";
    private static final int VERSION_NOT_SUPPORTED = 0;

    private static native int nativeGetHwdpsVersion();

    private static native int nativeHwAppAuthClassInit();

    private static native void nativeInitTee();

    private static native int nativeInstallPackage(HwdpsPackageInfo hwdpsPackageInfo);

    private static native int nativeSyncInstalledPackages(HwdpsPackageInfo[] hwdpsPackageInfoArr);

    private static native int nativeUninstallPackage(HwdpsPackageInfo hwdpsPackageInfo);

    static {
        try {
            System.loadLibrary("hwdps_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "hwdps_jni library not found!");
        }
    }

    private HwDataProtect() {
    }

    public static boolean prepare() {
        try {
            Log.d(TAG, "prepare jni");
            if (nativeHwAppAuthClassInit() == 0) {
                return true;
            }
            return false;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, prepare failed");
            return false;
        }
    }

    public static void initTee() {
        try {
            Log.d(TAG, "initTee");
            nativeInitTee();
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, initTee failed");
        }
    }

    public static boolean isFeatureSupported() {
        try {
            return nativeGetHwdpsVersion() != 0;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, isFeatureSupported failed");
            return false;
        }
    }

    public static void syncInstalledPackages(HwdpsPackageInfo[] packageInfos) {
        try {
            Log.d(TAG, "syncInstalledPackages");
            if (nativeSyncInstalledPackages(packageInfos) != 0) {
                Log.e(TAG, "syncInstalledPackages ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, syncInstalledPackages failed");
        }
    }

    public static void installPackage(HwdpsPackageInfo packageInfo) {
        try {
            Log.d(TAG, "installPackage");
            if (nativeInstallPackage(packageInfo) != 0) {
                Log.e(TAG, "installPackage ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, installPackage failed");
        }
    }

    public static void uninstallPackage(HwdpsPackageInfo packageInfo) {
        try {
            Log.d(TAG, "uninstallPackage");
            if (nativeUninstallPackage(packageInfo) != 0) {
                Log.e(TAG, "uninstallPackage ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, uninstallPackage failed:");
        }
    }
}

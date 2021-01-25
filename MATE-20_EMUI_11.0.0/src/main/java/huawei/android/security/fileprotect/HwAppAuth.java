package huawei.android.security.fileprotect;

import android.util.Log;

public class HwAppAuth {
    private static final int NATIVE_FAILED = -1;
    private static final int NATIVE_OK = 0;
    private static final String TAG = "HwAppAuth";
    private static final int VERSION_NOT_SUPPORTED = 0;

    private static native int nativeGetHwaaVersion();

    private static native int nativeHwAppAuthClassInit();

    private static native void nativeInitTee();

    private static native int nativeInstallPackage(HwaaPackageInfo hwaaPackageInfo);

    private static native int nativeSyncInstalledPackages(HwaaPackageInfo[] hwaaPackageInfoArr);

    private static native int nativeUninstallPackage(HwaaPackageInfo hwaaPackageInfo);

    static {
        try {
            System.loadLibrary("hwaa_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "hwaa_jni library not found!");
        }
    }

    private HwAppAuth() {
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
            return nativeGetHwaaVersion() != 0;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, isFeatureSupported failed");
            return false;
        }
    }

    public static void syncInstalledPackages(HwaaPackageInfo[] packageInfos) {
        try {
            Log.d(TAG, "syncInstalledPackages");
            if (nativeSyncInstalledPackages(packageInfos) != 0) {
                Log.e(TAG, "syncInstalledPackages ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, syncInstalledPackages failed");
        }
    }

    public static void installPackage(HwaaPackageInfo packageInfo) {
        try {
            Log.d(TAG, "installPackage");
            if (nativeInstallPackage(packageInfo) != 0) {
                Log.e(TAG, "installPackage ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "error, installPackage failed");
        }
    }

    public static void uninstallPackage(HwaaPackageInfo packageInfo) {
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

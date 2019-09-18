package huawei.android.security.fileprotect;

import android.util.Slog;

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
            Slog.e(TAG, "hwaa_jni library not found!");
        }
    }

    private HwAppAuth() {
    }

    public static boolean prepare() {
        boolean z = false;
        try {
            Slog.d(TAG, "prepare jni");
            if (nativeHwAppAuthClassInit() == 0) {
                z = true;
            }
            return z;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "error, prepare failed");
            return false;
        }
    }

    public static void initTee() {
        try {
            Slog.d(TAG, "initTee");
            nativeInitTee();
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "error, initTee failed");
        }
    }

    public static boolean isFeatureSupported() {
        boolean z = false;
        try {
            if (nativeGetHwaaVersion() != 0) {
                z = true;
            }
            return z;
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "error, isFeatureSupported failed");
            return false;
        }
    }

    public static void syncInstalledPackages(HwaaPackageInfo[] packageInfos) {
        try {
            Slog.d(TAG, "syncInstalledPackages");
            if (nativeSyncInstalledPackages(packageInfos) != 0) {
                Slog.e(TAG, "syncInstalledPackages ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "error, syncInstalledPackages failed");
        }
    }

    public static void installPackage(HwaaPackageInfo packageInfo) {
        try {
            Slog.d(TAG, "installPackage");
            if (nativeInstallPackage(packageInfo) != 0) {
                Slog.e(TAG, "installPackage ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "error, installPackage failed");
        }
    }

    public static void uninstallPackage(HwaaPackageInfo packageInfo) {
        try {
            Slog.d(TAG, "uninstallPackage");
            if (nativeUninstallPackage(packageInfo) != 0) {
                Slog.e(TAG, "uninstallPackage ret not ok");
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "error, uninstallPackage failed:");
        }
    }
}

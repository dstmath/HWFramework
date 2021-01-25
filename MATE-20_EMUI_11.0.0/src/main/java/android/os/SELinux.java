package android.os;

import android.annotation.UnsupportedAppUsage;
import android.util.Slog;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class SELinux {
    private static final int SELINUX_ANDROID_RESTORECON_CROSS_FILESYSTEMS = 64;
    private static final int SELINUX_ANDROID_RESTORECON_DATADATA = 16;
    private static final int SELINUX_ANDROID_RESTORECON_FORCE = 8;
    private static final int SELINUX_ANDROID_RESTORECON_NOCHANGE = 1;
    private static final int SELINUX_ANDROID_RESTORECON_RECURSE = 4;
    private static final int SELINUX_ANDROID_RESTORECON_SKIPCE = 32;
    private static final int SELINUX_ANDROID_RESTORECON_SKIP_SEHASH = 128;
    private static final int SELINUX_ANDROID_RESTORECON_VERBOSE = 2;
    private static final String TAG = "SELinux";

    @UnsupportedAppUsage
    public static final native boolean checkSELinuxAccess(String str, String str2, String str3, String str4);

    public static final native String fileSelabelLookup(String str);

    @UnsupportedAppUsage
    public static final native String getContext();

    public static final native String getFileContext(FileDescriptor fileDescriptor);

    @UnsupportedAppUsage
    public static final native String getFileContext(String str);

    public static final native String getPeerContext(FileDescriptor fileDescriptor);

    @UnsupportedAppUsage
    public static final native String getPidContext(int i);

    @UnsupportedAppUsage
    public static final native boolean isSELinuxEnabled();

    @UnsupportedAppUsage
    public static final native boolean isSELinuxEnforced();

    private static native boolean native_restorecon(String str, int i);

    public static final native boolean setFSCreateContext(String str);

    public static final native boolean setFileContext(String str, String str2);

    public static boolean restorecon(String pathname) throws NullPointerException {
        if (pathname != null) {
            return native_restorecon(pathname, 0);
        }
        throw new NullPointerException();
    }

    public static boolean restorecon(File file) throws NullPointerException {
        try {
            return native_restorecon(file.getCanonicalPath(), 0);
        } catch (IOException e) {
            Slog.e(TAG, "Error getting canonical path. Restorecon failed for " + file.getPath(), e);
            return false;
        }
    }

    @UnsupportedAppUsage
    public static boolean restoreconRecursive(File file) {
        try {
            return native_restorecon(file.getCanonicalPath(), 132);
        } catch (IOException e) {
            Slog.e(TAG, "Error getting canonical path. Restorecon failed for " + file.getPath(), e);
            return false;
        }
    }
}

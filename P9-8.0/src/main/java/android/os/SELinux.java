package android.os;

import android.util.Slog;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

public class SELinux {
    private static final int SELINUX_ANDROID_RESTORECON_DATADATA = 16;
    private static final int SELINUX_ANDROID_RESTORECON_FORCE = 8;
    private static final int SELINUX_ANDROID_RESTORECON_NOCHANGE = 1;
    private static final int SELINUX_ANDROID_RESTORECON_RECURSE = 4;
    private static final int SELINUX_ANDROID_RESTORECON_VERBOSE = 2;
    private static final String TAG = "SELinux";

    public static final native boolean checkSELinuxAccess(String str, String str2, String str3, String str4);

    public static final native String getContext();

    public static final native String getFileContext(String str);

    public static final native String getPeerContext(FileDescriptor fileDescriptor);

    public static final native String getPidContext(int i);

    public static final native boolean isSELinuxEnabled();

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

    public static boolean restoreconRecursive(File file) {
        try {
            return native_restorecon(file.getCanonicalPath(), 4);
        } catch (IOException e) {
            Slog.e(TAG, "Error getting canonical path. Restorecon failed for " + file.getPath(), e);
            return false;
        }
    }
}

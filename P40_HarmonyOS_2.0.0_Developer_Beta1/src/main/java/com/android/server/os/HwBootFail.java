package com.android.server.os;

import android.os.Binder;
import android.os.FileUtils;
import android.os.SELinux;
import android.os.SystemProperties;
import android.util.Slog;
import java.io.File;
import java.util.ArrayList;

public class HwBootFail {
    public static final int ACCOUNTS_DB_FILE_DAMAGED = 83886086;
    public static final int ANDROID_FRAMEWORK_ERRNO_START = 83886080;
    public static final int ANDROID_FRAMEWORK_STAGE = 5;
    public static final int ANDROID_FRAMEWORK_STAGE_START = 83886080;
    public static final int COMMON_PLATFORM = 0;
    private static final boolean DOMESTIC_COMMERCIAL_BETA;
    public static final int DO_NOTHING = 1;
    private static final int DROPBOX_MAX_SIZE = 262144;
    public static final String FRAMEWORK_LOG_PATH = "/data/anr/framework_boot_fail.log";
    public static final int HISI_PLATFORM = 1;
    public static final int NO_SUGGESTION = 0;
    public static final int PACKAGE_MANAGER_PACKAGE_LIST_FILE_DAMAGED = 83886087;
    public static final int PACKAGE_MANAGER_SETTING_FILE_DAMAGED = 83886084;
    public static final int PREBOOT_BROADCAST_FAIL = 83886082;
    public static final int QUALCOMM_PLATFORM = 2;
    public static final int RUNTIME_PERMISSION_SETTING_FILE_DAMAGED = 83886085;
    public static final int STAGE_FRAMEWORK_START = 83886081;
    public static final int STAGE_FRAMEWORK_SUCCESS = 83886082;
    public static final int SYSTEM_SERVICE_CRASH = 83886088;
    public static final int SYSTEM_SERVICE_LOAD_FAIL = 83886081;
    private static final String TAG = "HwBootFail";
    public static final int VM_OAT_FILE_DAMAGED = 83886083;

    private static native int nativeBootFailError(int i, int i2, String str, String str2, String str3, String str4);

    private static native int nativeDisableTimer();

    private static native int nativeEnableTimer();

    private static native int nativeGetFrameworkBadEnvErrno();

    private static native int nativeGetFrameworkCoreDevFaultErrno();

    private static native int nativeGetFrameworkDataDamagedErrno();

    private static native int nativeGetFrameworkNonCoreDevFaultErrno();

    private static native int nativeGetFrameworkServiceCrashErrno();

    private static native int nativeGetFrameworkServiceFreezeErrno();

    private static native int nativeGetFrameworkSubSysFaultErrno();

    private static native int nativeGetFrameworkSystemCrashErrno();

    private static native int nativeGetFrameworkSystemFreezeErrno();

    private static native int nativeSetBootSuccessStage();

    private static native int nativeSetFrameworkStage();

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        DOMESTIC_COMMERCIAL_BETA = z;
        try {
            System.loadLibrary("hwbootdetector_jni");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "libhwbfm_jni library not found!");
        }
    }

    public static void setBootTimer(boolean start) {
        if (10000 <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
        } else if (start) {
            try {
                nativeEnableTimer();
            } catch (Exception e) {
                Slog.e(TAG, "failed to set timer");
            }
        } else {
            nativeDisableTimer();
        }
    }

    public static void setFrameworkBootStage(int stage) {
        if (10000 <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        if (stage == 83886081) {
            try {
                nativeSetFrameworkStage();
                SystemProperties.set("sys.hw_boot_success", "0");
            } catch (Exception e) {
                Slog.e(TAG, "set framework boot stage exception");
                return;
            }
        }
        if (stage == 83886082) {
            nativeSetBootSuccessStage();
            HwBootCheck.bootSceneEnd(102);
            SystemProperties.set("sys.hw_boot_success", "1");
            HwBootCheck.isBootSuccess = true;
            HwBootCheck.bootCheckThreadQuit();
        }
    }

    public static boolean isBootSuccess() {
        if (10000 > Binder.getCallingUid()) {
            return SystemProperties.get("sys.hw_boot_success", "0").equals("1");
        }
        Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
        return false;
    }

    private static int getBootFailErrno(int type) {
        if (10000 <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return type;
        }
        switch (type) {
            case 83886081:
                return nativeGetFrameworkServiceFreezeErrno();
            case 83886082:
            case VM_OAT_FILE_DAMAGED /* 83886083 */:
            default:
                return type;
            case PACKAGE_MANAGER_SETTING_FILE_DAMAGED /* 83886084 */:
            case RUNTIME_PERMISSION_SETTING_FILE_DAMAGED /* 83886085 */:
            case ACCOUNTS_DB_FILE_DAMAGED /* 83886086 */:
            case PACKAGE_MANAGER_PACKAGE_LIST_FILE_DAMAGED /* 83886087 */:
                return nativeGetFrameworkDataDamagedErrno();
            case SYSTEM_SERVICE_CRASH /* 83886088 */:
                try {
                    return nativeGetFrameworkServiceCrashErrno();
                } catch (UnsatisfiedLinkError e) {
                    Slog.e(TAG, "get error code fail");
                    return type;
                }
        }
    }

    public static void bootFailError(int errNo, int suggestedRcvMethod, String exceptionInfo, ArrayList<String> logFilePath) {
        String logPath3 = "";
        String logPath1 = logFilePath.size() > 0 ? logFilePath.get(0) : logPath3;
        String logPath2 = logFilePath.size() > 1 ? logFilePath.get(1) : logPath3;
        if (logFilePath.size() > 2) {
            logPath3 = logFilePath.get(2);
        }
        if (10000 <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        int errNo2 = getBootFailErrno(errNo);
        if (!DOMESTIC_COMMERCIAL_BETA) {
            exceptionInfo = "none";
        }
        try {
            Slog.i(TAG, "Framework boot fail error: 0x" + Integer.toHexString(errNo2));
            nativeBootFailError(errNo2, suggestedRcvMethod, exceptionInfo, logPath1, logPath2, logPath3);
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "failed to set boot fail error");
        }
    }

    public static String creatFrameworkBootFailLog(File logFile, String bootinfo) {
        if (10000 <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return null;
        }
        StringBuilder sb = new StringBuilder(1024);
        sb.append(bootinfo);
        if (logFile != null) {
            try {
                if ("1".equals(SystemProperties.get("ro.debuggable"))) {
                    sb.append(FileUtils.readTextFile(logFile, 0, null));
                } else {
                    sb.append(FileUtils.readTextFile(logFile, 262144, "\n\n[[TRUNCATED]]"));
                }
            } catch (Exception e) {
                Slog.e(TAG, "Error reading " + logFile);
            }
        }
        File tracesFile = new File(FRAMEWORK_LOG_PATH);
        try {
            File tracesDir = tracesFile.getParentFile();
            if (!tracesDir.exists() && tracesDir.mkdirs()) {
                if (!SELinux.restorecon(tracesDir)) {
                    return null;
                }
                FileUtils.setPermissions(tracesDir.getPath(), 509, -1, -1);
            }
            if (tracesFile.exists() && !tracesFile.delete()) {
                Slog.w(TAG, "Unable to delete boot fail traces file");
            }
            if (tracesFile.createNewFile()) {
                FileUtils.stringToFile(FRAMEWORK_LOG_PATH, sb.toString());
                FileUtils.setPermissions(tracesFile.getPath(), 436, -1, -1);
            }
            return tracesFile.getPath();
        } catch (Exception e2) {
            Slog.e(TAG, "failed to write to /data/anr/framework_boot_fail.log");
            return null;
        }
    }

    public static void brokenFileBootFail(int brokenFileType, String brokenFilePath, Throwable t) {
        try {
            StackTraceElement ste = t.getStackTrace()[0];
            HwBootCheck.addBootInfo("BrokenFilePath : " + brokenFilePath);
            HwBootCheck.addBootInfo("JavaFileName : " + ste.getFileName());
            HwBootCheck.addBootInfo("MethodName : " + ste.getMethodName());
            HwBootCheck.addBootInfo("LineNumber : " + ste.getLineNumber());
            String logPath = creatFrameworkBootFailLog(null, HwBootCheck.getBootInfo());
            ArrayList<String> logPaths = new ArrayList<>(3);
            logPaths.add(logPath);
            bootFailError(brokenFileType, 1, HwBootCheck.getBootInfo(), logPaths);
        } catch (Exception e) {
            Slog.e(TAG, "exception in adding broken file bootfail info");
        }
    }

    public static void notifyBootSuccess() {
        new Thread() {
            /* class com.android.server.os.HwBootFail.AnonymousClass1 */

            @Override // java.lang.Thread, java.lang.Runnable
            public void run() {
                super.run();
                while ("0".equals(SystemProperties.get("service.bootanim.exit", "1"))) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                HwBootFail.setFrameworkBootStage(83886082);
            }
        }.start();
    }
}

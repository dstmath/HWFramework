package com.android.internal.os;

import android.os.Binder;
import android.os.FileUtils;
import android.os.SELinux;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.telephony.RILConstants;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.log.LogPower;
import java.io.File;
import java.io.IOException;
import javax.microedition.khronos.opengles.GL10;

public class HwBootFail {
    public static final int ACCOUNTS_DB_FILE_DAMAGED = 83886086;
    public static final int ANDROID_FRAMEWORK_ERRNO_START = 83886080;
    public static final int ANDROID_FRAMEWORK_STAGE = 5;
    public static final int ANDROID_FRAMEWORK_STAGE_START = 83886080;
    public static final int BL1_STAGE = 1;
    public static final int BL2_STAGE = 2;
    public static final String BOOT_BFM_CTL_DEVICE_CODE = "/sys/class/misc/hw_bfm/bfm_ctl";
    public static final String BOOT_COMMAND_BOOT_STAGE = "0x00000001";
    public static final String BOOT_COMMAND_SET_TIMER = "0x00000003";
    public static final String BOOT_COMMAND_SET_TIMEROUT = "0x00000004";
    public static final String BOOT_COMMAND_TRIGGER_ERROR = "0x00000002";
    public static final String BOOT_COMMAND_VALUE_SET_TIMER_DISABLE = "0x00000000";
    public static final String BOOT_COMMAND_VALUE_SET_TIMER_ENABLE = "0x00000001";
    public static final int COMMON_PLATFORM = 0;
    public static final int DO_NOTHING = 1;
    private static final int DROPBOX_MAX_SIZE = 262144;
    public static final String FRAMEWORK_LOG_PATH = "/data/anr/framework_boot_fail.log";
    public static final int HISI_PLATFORM = 1;
    public static final int KERNEL_STAGE = 3;
    public static final int NATIVE_STAGE = 4;
    public static final int NO_SUGGESTION = 0;
    public static final int PACKAGE_MANAGER_PACKAGE_LIST_FILE_DAMAGED = 83886087;
    public static final int PACKAGE_MANAGER_SETTING_FILE_DAMAGED = 83886084;
    public static final int PBL_STAGE = 0;
    public static final int PHASE_ACTIVITY_MANAGER_READY = 550;
    public static final int PHASE_BOOT_COMPLETED = 1000;
    public static final int PHASE_LOCK_SETTINGS_READY = 480;
    public static final int PHASE_SYSTEM_SERVICES_READY = 500;
    public static final int PHASE_THIRD_PARTY_APPS_CAN_START = 600;
    public static final int PHASE_WAIT_FOR_DEFAULT_DISPLAY = 100;
    public static final int PREBOOT_BROADCAST_FAIL = 83886082;
    public static final int QUALCOMM_PLATFORM = 2;
    public static final int RUNTIME_PERMISSION_SETTING_FILE_DAMAGED = 83886085;
    public static final int STAGE_APP_DEXOPT_END = 83886091;
    public static final int STAGE_APP_DEXOPT_START = 83886090;
    public static final int STAGE_BOOT_SUCCESS = Integer.MAX_VALUE;
    public static final int STAGE_FRAMEWORK_JAR_DEXOPT_END = 83886089;
    public static final int STAGE_FRAMEWORK_JAR_DEXOPT_START = 83886088;
    public static final int STAGE_PHASE_ACTIVITY_MANAGER_READY = 83886086;
    public static final int STAGE_PHASE_LOCK_SETTINGS_READY = 83886084;
    public static final int STAGE_PHASE_SYSTEM_SERVICES_READY = 83886085;
    public static final int STAGE_PHASE_THIRD_PARTY_APPS_CAN_START = 83886087;
    public static final int STAGE_PHASE_WAIT_FOR_DEFAULT_DISPLAY = 83886083;
    public static final int STAGE_VM_START = 83886082;
    public static final int STAGE_ZYGOTE_START = 83886081;
    public static final int SYSTEM_SERVICE_LOAD_FAIL = 83886081;
    private static final String TAG = "HwBootFail";
    public static final int VM_OAT_FILE_DAMAGED = 83886083;

    public static int changeBootStage(int phase) {
        if (phase == PHASE_WAIT_FOR_DEFAULT_DISPLAY) {
            return VM_OAT_FILE_DAMAGED;
        }
        if (phase == PHASE_LOCK_SETTINGS_READY) {
            return STAGE_PHASE_LOCK_SETTINGS_READY;
        }
        if (phase == PHASE_SYSTEM_SERVICES_READY) {
            return STAGE_PHASE_SYSTEM_SERVICES_READY;
        }
        if (phase == PHASE_ACTIVITY_MANAGER_READY) {
            return STAGE_PHASE_ACTIVITY_MANAGER_READY;
        }
        if (phase == PHASE_THIRD_PARTY_APPS_CAN_START) {
            return STAGE_PHASE_THIRD_PARTY_APPS_CAN_START;
        }
        return -1;
    }

    public static int getBootStage() {
        int currStage = -1;
        if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return currStage;
        }
        try {
            FileUtils.stringToFile(BOOT_BFM_CTL_DEVICE_CODE, BOOT_COMMAND_VALUE_SET_TIMER_ENABLE);
            currStage = Integer.valueOf(FileUtils.readTextFile(new File(BOOT_BFM_CTL_DEVICE_CODE), PBL_STAGE, null).trim()).intValue();
        } catch (Exception e) {
            Slog.e(TAG, "failed to read /sys/class/misc/hw_bfm/bfm_ctl");
        }
        return currStage;
    }

    public static boolean isBootSuccess() {
        if (PGAction.PG_ID_DEFAULT_FRONT > Binder.getCallingUid()) {
            return SystemProperties.get("sys.hw_boot_success", "0").equals("1");
        }
        Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
        return false;
    }

    public static void setBootStage(int bootStageValue) {
        if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
        } else if (bootStageValue >= 0) {
            if (STAGE_FRAMEWORK_JAR_DEXOPT_START == bootStageValue || STAGE_APP_DEXOPT_START == bootStageValue) {
                try {
                    FileUtils.stringToFile(BOOT_BFM_CTL_DEVICE_CODE, "0x00000003 0x00000000");
                } catch (IOException e) {
                    Slog.e(TAG, "failed to write to /sys/class/misc/hw_bfm/bfm_ctl");
                }
            } else if (STAGE_FRAMEWORK_JAR_DEXOPT_END == bootStageValue || STAGE_APP_DEXOPT_END == bootStageValue) {
                try {
                    FileUtils.stringToFile(BOOT_BFM_CTL_DEVICE_CODE, "0x00000003 0x00000001");
                } catch (IOException e2) {
                    Slog.e(TAG, "failed to write to /sys/class/misc/hw_bfm/bfm_ctl");
                }
            }
            try {
                FileUtils.stringToFile(BOOT_BFM_CTL_DEVICE_CODE, "0x00000001 0x" + Integer.toHexString(bootStageValue));
            } catch (IOException e3) {
                Slog.e(TAG, "failed to write to /sys/class/misc/hw_bfm/bfm_ctl");
            }
            Slog.w(TAG, "bootStageValue = STAGE_ZYGOTE_START" + (bootStageValue - SYSTEM_SERVICE_LOAD_FAIL));
            if (bootStageValue == STAGE_BOOT_SUCCESS) {
                try {
                    HwBootCheck.bootSceneEnd(LogPower.WEBVIEW_RESUMED);
                    SystemProperties.set("sys.hw_boot_success", "1");
                    HwBootCheck.bootCheckThreadQuit();
                } catch (Exception ex) {
                    Slog.e(TAG, "has ex:" + ex);
                }
            } else {
                SystemProperties.set("sys.hw_boot_success", "0");
            }
        }
    }

    public static void bootFailError(int errNo, int suggestedRcvMethod, String logFilePath) {
        String boot_fail_info = "0x" + Integer.toHexString(errNo) + " " + suggestedRcvMethod + " " + logFilePath;
        if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return;
        }
        try {
            FileUtils.stringToFile(BOOT_BFM_CTL_DEVICE_CODE, "0x00000002 " + boot_fail_info);
        } catch (Exception e) {
            Slog.e(TAG, "failed to write to /sys/class/misc/hw_bfm/bfm_ctl");
        }
    }

    public static String creatFrameworkBootFailLog(File logFile, String bootinfo) {
        if (PGAction.PG_ID_DEFAULT_FRONT <= Binder.getCallingUid()) {
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return null;
        }
        StringBuilder sb = new StringBuilder(GL10.GL_STENCIL_BUFFER_BIT);
        sb.append(bootinfo);
        if (logFile != null) {
            try {
                if ("1".equals(SystemProperties.get("ro.debuggable"))) {
                    sb.append(FileUtils.readTextFile(logFile, PBL_STAGE, null));
                } else {
                    sb.append(FileUtils.readTextFile(logFile, DROPBOX_MAX_SIZE, "\n\n[[TRUNCATED]]"));
                }
            } catch (Exception e) {
                Slog.e(TAG, "Error reading " + logFile, e);
            }
        }
        File tracesFile = new File(FRAMEWORK_LOG_PATH);
        try {
            File tracesDir = tracesFile.getParentFile();
            if (!tracesDir.exists()) {
                tracesDir.mkdirs();
                if (!SELinux.restorecon(tracesDir)) {
                    return null;
                }
                FileUtils.setPermissions(tracesDir.getPath(), RILConstants.OEM_ERROR_9, -1, -1);
            }
            if (tracesFile.exists()) {
                tracesFile.delete();
            }
            tracesFile.createNewFile();
            FileUtils.stringToFile(FRAMEWORK_LOG_PATH, sb.toString());
            FileUtils.setPermissions(tracesFile.getPath(), 432, -1, -1);
            return tracesFile.getPath();
        } catch (Exception e2) {
            Slog.e(TAG, "failed to write to /data/anr/framework_boot_fail.log");
            return null;
        }
    }

    public static void brokenFileBootFail(int brokenFileType, String brokenFilePath, Throwable t) {
        try {
            StackTraceElement ste = t.getStackTrace()[PBL_STAGE];
            HwBootCheck.addBootInfo("BrokenFilePath : " + brokenFilePath);
            HwBootCheck.addBootInfo("JavaFileName : " + ste.getFileName());
            HwBootCheck.addBootInfo("MethodName : " + ste.getMethodName());
            HwBootCheck.addBootInfo("LineNumber : " + ste.getLineNumber());
            bootFailError(brokenFileType, HISI_PLATFORM, creatFrameworkBootFailLog(null, HwBootCheck.getBootInfo()));
        } catch (Exception ex) {
            Slog.e(TAG, "exception in adding broken file bootfail info", ex);
        }
    }

    public static void notifyBootSuccess() {
        new Thread() {
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
                HwBootFail.setBootStage(HwBootFail.STAGE_BOOT_SUCCESS);
            }
        }.start();
    }
}

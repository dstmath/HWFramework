package com.android.server.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogBufferUtil {
    private static final String GMS_CORE_PACKAGENAME = "com.google.android.gms";
    private static final String HWLOG_SWITCH_PATH = "/dev/hwlog_switch";
    private static final String LOGBUFFER_DISABLE = "sys.logbuffer.disable";
    private static final int LOGSWITCH_STATUS_ON = 1;
    private static final String PROJECT_MENU_APLOG = "persist.sys.huawei.debug.on";
    private static final int PROJECT_MENU_APLOG_ON = 1;
    private static final String TAG = "LogBufferUtil";
    private static final int USER_TYPE_DOMESTIC_COMMERCIAL = 1;

    private static boolean isNologAndLite() {
        return 1 == SystemProperties.getInt("ro.logsystem.usertype", 0) && SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
    }

    private static boolean isHwLogSwitchOn() {
        int logSwitch = 1;
        BufferedReader hwLogReader = null;
        try {
            BufferedReader hwLogReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(HWLOG_SWITCH_PATH), "UTF-8"));
            String tempString = hwLogReader2.readLine();
            if (tempString != null) {
                logSwitch = Integer.parseInt(tempString);
            }
            Slog.i(TAG, "/dev/hwlog_switch = " + logSwitch);
            try {
                hwLogReader2.close();
            } catch (IOException e) {
                Slog.e(TAG, "hwLogReader close failed", e);
            }
        } catch (FileNotFoundException e2) {
            Slog.e(TAG, "/dev/hwlog_switch not exist", e2);
            if (0 != 0) {
                hwLogReader.close();
            }
        } catch (IOException e3) {
            Slog.e(TAG, "logswitch read failed", e3);
            if (0 != 0) {
                hwLogReader.close();
            }
        } catch (Exception e4) {
            Slog.e(TAG, "logswitch read exception", e4);
            if (0 != 0) {
                hwLogReader.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    hwLogReader.close();
                } catch (IOException e5) {
                    Slog.e(TAG, "hwLogReader close failed", e5);
                }
            }
            throw th;
        }
        if (1 == logSwitch) {
            return true;
        }
        return false;
    }

    private static boolean isGmsCoreInstalled(Context context) {
        try {
            if (context.getPackageManager().getPackageInfo(GMS_CORE_PACKAGENAME, 0) == null) {
                return false;
            }
            Slog.i(TAG, "hwLogBuffer: GmsCore installed.");
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Slog.i(TAG, "hwLogBuffer: GmsCore not installed.");
            return false;
        }
    }

    private static boolean adbEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "adb_enabled", 0) > 0 || SystemProperties.getInt(PROJECT_MENU_APLOG, 0) == 1;
    }

    private static boolean needCloseLogBuffer(Context context) {
        return !adbEnabled(context) && !isGmsCoreInstalled(context) && !isHwLogSwitchOn();
    }

    public static void closeLogBufferAsNeed(Context context) {
        boolean current;
        if (isNologAndLite() && needCloseLogBuffer(context) != (current = SystemProperties.getBoolean(LOGBUFFER_DISABLE, false))) {
            SystemProperties.set(LOGBUFFER_DISABLE, current ? "false" : "true");
        }
    }
}

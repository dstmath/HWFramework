package com.android.server.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.provider.Settings.Global;
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
        if (1 == SystemProperties.getInt("ro.logsystem.usertype", 0)) {
            return SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0093 A:{SYNTHETIC, Splitter: B:36:0x0093} */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0078 A:{SYNTHETIC, Splitter: B:28:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x005d A:{SYNTHETIC, Splitter: B:20:0x005d} */
    /* JADX WARNING: Removed duplicated region for block: B:56:? A:{SYNTHETIC, RETURN, ORIG_RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a5 A:{SYNTHETIC, Splitter: B:42:0x00a5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isHwLogSwitchOn() {
        IOException e;
        FileNotFoundException e2;
        Exception e3;
        Throwable th;
        int logSwitch = 0;
        BufferedReader hwLogReader = null;
        try {
            BufferedReader hwLogReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(HWLOG_SWITCH_PATH), "UTF-8"));
            try {
                String tempString = hwLogReader2.readLine();
                if (tempString != null) {
                    logSwitch = Integer.parseInt(tempString);
                }
                Slog.i(TAG, "/dev/hwlog_switch = " + logSwitch);
                if (hwLogReader2 != null) {
                    try {
                        hwLogReader2.close();
                    } catch (IOException e4) {
                        Slog.e(TAG, "hwLogReader close failed", e4);
                    }
                }
                hwLogReader = hwLogReader2;
            } catch (FileNotFoundException e5) {
                e2 = e5;
                hwLogReader = hwLogReader2;
                Slog.e(TAG, "/dev/hwlog_switch not exist", e2);
                if (hwLogReader != null) {
                }
                if (1 == logSwitch) {
                }
            } catch (IOException e6) {
                e4 = e6;
                hwLogReader = hwLogReader2;
                Slog.e(TAG, "logswitch read failed", e4);
                if (hwLogReader != null) {
                }
                if (1 == logSwitch) {
                }
            } catch (Exception e7) {
                e3 = e7;
                hwLogReader = hwLogReader2;
                try {
                    Slog.e(TAG, "logswitch read exception", e3);
                    if (hwLogReader != null) {
                    }
                    if (1 == logSwitch) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (hwLogReader != null) {
                        try {
                            hwLogReader.close();
                        } catch (IOException e42) {
                            Slog.e(TAG, "hwLogReader close failed", e42);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                hwLogReader = hwLogReader2;
                if (hwLogReader != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e2 = e8;
            Slog.e(TAG, "/dev/hwlog_switch not exist", e2);
            if (hwLogReader != null) {
                try {
                    hwLogReader.close();
                } catch (IOException e422) {
                    Slog.e(TAG, "hwLogReader close failed", e422);
                }
            }
            if (1 == logSwitch) {
            }
        } catch (IOException e9) {
            e422 = e9;
            Slog.e(TAG, "logswitch read failed", e422);
            if (hwLogReader != null) {
                try {
                    hwLogReader.close();
                } catch (IOException e4222) {
                    Slog.e(TAG, "hwLogReader close failed", e4222);
                }
            }
            if (1 == logSwitch) {
            }
        } catch (Exception e10) {
            e3 = e10;
            Slog.e(TAG, "logswitch read exception", e3);
            if (hwLogReader != null) {
                try {
                    hwLogReader.close();
                } catch (IOException e42222) {
                    Slog.e(TAG, "hwLogReader close failed", e42222);
                }
            }
            if (1 == logSwitch) {
            }
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
        } catch (NameNotFoundException e) {
            Slog.i(TAG, "hwLogBuffer: GmsCore not installed.");
            return false;
        }
    }

    private static boolean adbEnabled(Context context) {
        return Global.getInt(context.getContentResolver(), "adb_enabled", 0) > 0 || SystemProperties.getInt(PROJECT_MENU_APLOG, 0) == 1;
    }

    private static boolean needCloseLogBuffer(Context context) {
        return (adbEnabled(context) || (isGmsCoreInstalled(context) ^ 1) == 0) ? false : isHwLogSwitchOn() ^ 1;
    }

    public static void closeLogBufferAsNeed(Context context) {
        if (isNologAndLite()) {
            boolean current = SystemProperties.getBoolean(LOGBUFFER_DISABLE, false);
            if (needCloseLogBuffer(context) != current) {
                SystemProperties.set(LOGBUFFER_DISABLE, current ? "false" : "true");
            }
        }
    }
}

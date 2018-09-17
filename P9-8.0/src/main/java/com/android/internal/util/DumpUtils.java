package com.android.internal.util;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class DumpUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "DumpUtils";

    public interface Dump {
        void dump(PrintWriter printWriter, String str);
    }

    private DumpUtils() {
    }

    public static void dumpAsync(Handler handler, final Dump dump, PrintWriter pw, final String prefix, long timeout) {
        final StringWriter sw = new StringWriter();
        if (handler.runWithScissors(new Runnable() {
            public void run() {
                PrintWriter lpw = new FastPrintWriter(sw);
                dump.dump(lpw, prefix);
                lpw.close();
            }
        }, timeout)) {
            pw.print(sw.toString());
        } else {
            pw.println("... timed out");
        }
    }

    private static void logMessage(PrintWriter pw, String msg) {
        pw.println(msg);
    }

    public static boolean checkDumpPermission(Context context, String tag, PrintWriter pw) {
        if (context.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
            return true;
        }
        logMessage(pw, "Permission Denial: can't dump " + tag + " from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " due to missing android.permission.DUMP permission");
        return false;
    }

    public static boolean checkUsageStatsPermission(Context context, String tag, PrintWriter pw) {
        int uid = Binder.getCallingUid();
        switch (uid) {
            case 0:
            case 1000:
            case 2000:
                return true;
            default:
                if (context.checkCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                    logMessage(pw, "Permission Denial: can't dump " + tag + " from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " due to missing android.permission.PACKAGE_USAGE_STATS permission");
                    return false;
                }
                AppOpsManager appOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
                String[] pkgs = context.getPackageManager().getPackagesForUid(uid);
                if (pkgs != null) {
                    int length = pkgs.length;
                    int i = 0;
                    while (i < length) {
                        switch (appOps.checkOpNoThrow(43, uid, pkgs[i])) {
                            case 0:
                                return true;
                            case 3:
                                return true;
                            default:
                                i++;
                        }
                    }
                }
                logMessage(pw, "Permission Denial: can't dump " + tag + " from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " due to android:get_usage_stats app-op not allowed");
                return false;
        }
    }

    public static boolean checkDumpAndUsageStatsPermission(Context context, String tag, PrintWriter pw) {
        return checkDumpPermission(context, tag, pw) ? checkUsageStatsPermission(context, tag, pw) : false;
    }
}

package com.android.server.mtm.test;

import android.app.mtm.MultiTaskUtils;
import android.os.SystemClock;
import com.android.server.mtm.utils.InnerUtils;
import java.io.PrintWriter;

public final class TestMtmUtils {
    public static final void test(PrintWriter pw, String[] args) {
        if (args[1] != null && pw != null) {
            String cmd = args[1];
            if ("getAppType".equals(cmd)) {
                runGetAppType(pw, args);
            } else if ("getPackageNameByUid".equals(cmd)) {
                runGetPackageNameByUid(pw, args);
            } else if ("checkPackageNameByUid".equals(cmd)) {
                runCheckPackageNameByUid(pw, args);
            } else if ("checkPackageNameContainsByUid".equals(cmd)) {
                runcheckPackageNameContainsByUid(pw, args);
            } else {
                pw.println("Bad command :" + cmd);
            }
        }
    }

    private static void runGetAppType(PrintWriter pw, String[] args) {
        if (args.length != 5 || args[2] == null || args[3] == null || args[4] == null) {
            pw.println("args is invalid");
            return;
        }
        int pid = Integer.parseInt(args[2]);
        int uid = Integer.parseInt(args[3]);
        String packagename = args[4];
        long starttime = SystemClock.uptimeMillis();
        long durtime = SystemClock.uptimeMillis() - starttime;
        pw.println("Apptype is: " + MultiTaskUtils.getAppType(pid, uid, packagename) + "(1:SYSTEM_SERVER;2:SYSTEM_APP;3:HW_INSTALL;4:THIRDPARTY)");
        pw.println("total time:" + durtime + "(ms)");
    }

    private static void runGetPackageNameByUid(PrintWriter pw, String[] args) {
        if (args.length != 3 || args[2] == null) {
            pw.println("args is invalid");
            return;
        }
        int uid = Integer.parseInt(args[2]);
        long starttime = SystemClock.uptimeMillis();
        long durtime = SystemClock.uptimeMillis() - starttime;
        pw.println("PackageName is: " + InnerUtils.getPackageNameByUid(uid));
        pw.println("total time:" + durtime + "(ms)");
    }

    private static void runCheckPackageNameByUid(PrintWriter pw, String[] args) {
        if (args.length != 4 || args[2] == null || args[3] == null) {
            pw.println("args is invalid");
            return;
        }
        int uid = Integer.parseInt(args[2]);
        String packagename = args[3];
        long starttime = SystemClock.uptimeMillis();
        long durtime = SystemClock.uptimeMillis() - starttime;
        pw.println("CheckResult is: " + InnerUtils.checkPackageNameByUid(uid, packagename));
        pw.println("total time:" + durtime + "(ms)");
    }

    private static void runcheckPackageNameContainsByUid(PrintWriter pw, String[] args) {
        if (args.length != 4 || args[2] == null || args[3] == null) {
            pw.println("args is invalid");
            return;
        }
        int uid = Integer.parseInt(args[2]);
        String packagename = args[3];
        long starttime = SystemClock.uptimeMillis();
        long durtime = SystemClock.uptimeMillis() - starttime;
        pw.println("CheckResult is: " + InnerUtils.checkPackageNameContainsByUid(uid, packagename));
        pw.println("total time:" + durtime + "(ms)");
    }
}

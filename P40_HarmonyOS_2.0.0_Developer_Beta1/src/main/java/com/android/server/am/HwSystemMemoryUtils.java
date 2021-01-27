package com.android.server.am;

import android.os.Debug;
import android.os.Handler;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.os.ZygoteInit;
import com.android.server.appactcontrol.AppActConstant;
import com.huawei.server.HwPartIawareUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class HwSystemMemoryUtils {
    private static final String ARM64_BIT = "arm64";
    private static final int HIGH_MEMORY_THRESHOLD = 409600;
    private static final int INVALID_FLAG = -1;
    private static final boolean IS_WATCH = "watch".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    private static final int SIZE_OF_STRING_BUFFER = 64;
    private static final long SLIM_DELAY_TIME = 90000;
    static final String TAG = "HwSystemMemoryUtils";

    private static void dumpMemoryUsage() {
        try {
            Slog.i(TAG, "start dumpMemoryUsage");
            Slog.e(TAG, "system_server memory is abnormal, usage dump: " + ((String) Class.forName("maple.system.VMDebug").getDeclaredMethod("dumpRCAndGCPerformanceInfo", new Class[0]).invoke(null, new Object[0])));
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Slog.w(TAG, "maple.system.VMDebug.dumpRCAndGCPerformanceInfo reflect failed");
        }
    }

    private static void dumpStartWindowCache() {
        String dumpStartWindow = HwPartIawareUtil.dumpHwStartWindowCache();
        Slog.e(TAG, "startWindowCache in system_server memory dump: " + dumpStartWindow);
    }

    public static void monitorMemory() {
        if (ZygoteInit.sIsMygote) {
            Debug.MemoryInfo info = new Debug.MemoryInfo();
            Debug.getMemoryInfo(Process.myPid(), info);
            int pss = info.getTotalPss();
            Slog.i(TAG, "system_server total pss(kb): " + pss + " dalvikPss: " + info.dalvikPss + " nativePss: " + info.nativePss + " otherPss: " + info.otherPss + " totalSwappedOutPss: " + info.getTotalSwappedOutPss());
            if (pss > HIGH_MEMORY_THRESHOLD) {
                dumpStartWindowCache();
                dumpMemoryUsage();
            }
        }
    }

    public static void slimForZygote(Handler workHandler) {
        if (IS_WATCH && isNeedSlimZygote()) {
            try {
                int pid = obtainPidForGivenName("zygote");
                if (workHandler != null) {
                    slimAsync(workHandler, pid);
                    return;
                }
                Slog.i(TAG, "execute slim for zygote, pid is " + pid);
                SystemProperties.set("sys.reclaim.pid", String.valueOf(pid));
            } catch (Exception e) {
                Slog.e(TAG, "slimForzygote failed");
            }
        }
    }

    public static void slimForSystem(Handler workHandler) {
        if (IS_WATCH) {
            try {
                int pid = Process.myPid();
                if (workHandler != null) {
                    slimAsync(workHandler, pid);
                    return;
                }
                Slog.i(TAG, "execute slim for system_server, pid is " + pid);
                SystemProperties.set("sys.reclaim.pid", String.valueOf(pid));
            } catch (Exception e) {
                Slog.e(TAG, "slimForSystem failed");
            }
        }
    }

    private static void slimAsync(Handler workHandler, int pid) {
        workHandler.postDelayed(new Runnable(pid) {
            /* class com.android.server.am.$$Lambda$HwSystemMemoryUtils$lI_zTywPPmvJcIMf8YPhKscBqYY */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwSystemMemoryUtils.lambda$slimAsync$0(this.f$0);
            }
        }, SLIM_DELAY_TIME);
    }

    static /* synthetic */ void lambda$slimAsync$0(int pid) {
        try {
            Slog.i(TAG, "execute slim for pid " + pid);
            SystemProperties.set("sys.reclaim.pid", String.valueOf(pid));
        } catch (Exception e) {
            Slog.e(TAG, "slim pid " + pid + " failed");
        }
    }

    private static boolean isNeedSlimZygote() {
        return SystemProperties.get("ro.product.cpu.abilist64", "UNKNOWN").contains(ARM64_BIT);
    }

    private static int obtainPidForGivenName(String processName) {
        try {
            InputStream in = Runtime.getRuntime().exec("pidof " + processName).getInputStream();
            StringBuffer buffer = new StringBuffer(64);
            while (true) {
                int ch = in.read();
                if (ch == -1) {
                    return Integer.parseInt(buffer.toString().replaceAll("\\s*|\t|\r|\n", ""));
                }
                buffer.append((char) ch);
            }
        } catch (IOException | NumberFormatException e) {
            Slog.w(TAG, "obtainPidForGivenName: [" + processName + "]pidString=" + ((String) null) + " " + e.getMessage());
            return -1;
        }
    }
}

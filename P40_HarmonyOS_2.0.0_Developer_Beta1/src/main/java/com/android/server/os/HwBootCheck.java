package com.android.server.os;

import android.app.ActivityManagerInternal;
import android.os.Binder;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.LocalServices;
import com.android.server.SystemServiceManager;
import com.android.server.Watchdog;
import com.android.server.am.ActivityManagerService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class HwBootCheck {
    private static final boolean DEBUG_MAIN_THREAD_BLOCK;
    public static final int MESSAGE_CHECK_AFTER_AMS_INIT = 100;
    public static final int MESSAGE_CHECK_AFTER_BOOT_DEXOPT = 102;
    public static final int MESSAGE_CHECK_AFTER_PMS_INIT = 101;
    public static final int MESSAGE_CHECK_APP_DEXOPT = 104;
    public static final int MESSAGE_CHECK_FRAMEWORK_JAR_DEXOPT = 105;
    public static final int MESSAGE_CHECK_PERFORM_SYSTEM_SERVER_DEXOPT = 103;
    private static final String TAG = "HwBootFail";
    static boolean isBootSuccess = false;
    private static Handler mBootCheckHandler;
    private static HandlerThread mBootCheckThread;
    private static StringBuilder mBootInfo = new StringBuilder();

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3) {
            z = true;
        }
        DEBUG_MAIN_THREAD_BLOCK = z;
    }

    /* access modifiers changed from: private */
    public static final class BootCheckHandler extends Handler {
        public BootCheckHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                case 101:
                case 102:
                case 103:
                case 104:
                case 105:
                    try {
                        Slog.i(HwBootCheck.TAG, "mBootCheckHandler: " + msg.what);
                        HwBootCheck.addBootInfo("performSystemServerDexOpt: installer.waitForConnection");
                        HwBootCheck.addBootInfo("currBootScene is: " + msg.what);
                        HwBootCheck.addBootInfo("mSystemReady is: " + ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isSystemReady());
                        HwBootCheck.bootSceneEnd(msg.what);
                        if (HwBootCheck.DEBUG_MAIN_THREAD_BLOCK && !HwBootCheck.isBootSuccess) {
                            HwBootCheck.addBootFailedLog();
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        Slog.e(HwBootCheck.TAG, "BootCheckHandler exception");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public static void addBootFailedLog() {
        ArrayList<Integer> pids = new ArrayList<>();
        pids.add(Integer.valueOf(Process.myPid()));
        int[] nativePidsInt = Process.getPidsForCommands(Watchdog.NATIVE_STACKS_OF_INTEREST);
        ArrayList<Integer> nativePids = null;
        if (nativePidsInt != null) {
            nativePids = new ArrayList<>(nativePidsInt.length);
            for (int i : nativePidsInt) {
                nativePids.add(Integer.valueOf(i));
            }
        }
        File stack = ActivityManagerService.dumpStackTraces(pids, (ProcessCpuTracker) null, (SparseArray<Boolean>) null, nativePids);
        if (stack == null) {
            stack = dumpStackTraces();
        }
        Watchdog.getInstance().addKernelLog();
        addBootInfo(((SystemServiceManager) LocalServices.getService(SystemServiceManager.class)).dumpInfo());
        SystemClock.sleep(2000);
        String framework_log_path = HwBootFail.creatFrameworkBootFailLog(stack, getBootInfo());
        ArrayList<String> logPaths = new ArrayList<>(3);
        logPaths.add(framework_log_path);
        HwBootFail.bootFailError(83886081, 1, getBootInfo(), logPaths);
    }

    private static File dumpStackTraces() {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", (String) null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        File tracesFile = new File(tracesPath);
        try {
            if (tracesFile.exists() && tracesFile.delete()) {
                Slog.w(TAG, "Unable to delete boot fail traces file");
            }
            if (tracesFile.createNewFile()) {
                FileUtils.setPermissions(tracesFile.getPath(), 438, -1, -1);
            }
            Process.sendSignal(Process.myPid(), 3);
            return tracesFile;
        } catch (IOException e) {
            Slog.w(TAG, "Unable to prepare boot fail traces file");
            return null;
        }
    }

    private static void ensureThreadLocked() {
        if (mBootCheckThread == null) {
            mBootCheckThread = new HandlerThread("bootCheck");
            mBootCheckThread.start();
            mBootCheckHandler = new BootCheckHandler(mBootCheckThread.getLooper());
        }
    }

    public static HandlerThread getHandlerThread() {
        HandlerThread handlerThread;
        synchronized (HwBootCheck.class) {
            ensureThreadLocked();
            handlerThread = mBootCheckThread;
        }
        return handlerThread;
    }

    public static Handler getHandler() {
        Handler handler;
        synchronized (HwBootCheck.class) {
            ensureThreadLocked();
            handler = mBootCheckHandler;
        }
        return handler;
    }

    public static void bootCheckThreadQuit() {
        StringBuilder sb = mBootInfo;
        sb.delete(0, sb.length());
        getHandlerThread().quit();
    }

    public static boolean bootSceneStart(int sceneId, long maxTime) {
        try {
            if (10000 <= Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            } else if (!getHandlerThread().isAlive()) {
                Slog.w(TAG, "mBootCheckThread is not alive");
                return false;
            } else {
                Slog.i(TAG, "bootSceneStart :" + sceneId + ", waittime=" + maxTime);
                if (mBootCheckHandler.hasMessages(sceneId)) {
                    return true;
                }
                mBootCheckHandler.sendEmptyMessageDelayed(sceneId, maxTime);
                return true;
            }
        } catch (Exception e) {
            Slog.e(TAG, "set boot scene start fail");
            return false;
        }
    }

    public static boolean bootSceneEnd(int sceneId) {
        try {
            if (10000 <= Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            }
            Slog.i(TAG, "bootSceneEnd :" + sceneId);
            if (!mBootCheckHandler.hasMessages(sceneId)) {
                return true;
            }
            mBootCheckHandler.removeMessages(sceneId);
            return true;
        } catch (Exception e) {
            Slog.e(TAG, "set boot scene end fail");
            return false;
        }
    }

    public static String getBootInfo() {
        try {
            if (10000 > Binder.getCallingUid()) {
                return mBootInfo.toString();
            }
            Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "get boot info fail");
            return null;
        }
    }

    public static boolean addBootInfo(String info) {
        try {
            if (isBootSuccess) {
                return false;
            }
            if (10000 <= Binder.getCallingUid()) {
                Slog.e(TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return false;
            } else if (mBootInfo == null) {
                return false;
            } else {
                StringBuilder sb = mBootInfo;
                sb.append(info);
                sb.append("\n");
                return true;
            }
        } catch (Exception e) {
            Slog.e(TAG, "exception add boot info");
            return false;
        }
    }
}

package com.android.server;

import android.app.IActivityController;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hidl.manager.V1_0.IServiceManager;
import android.hidl.manager.V1_0.IServiceManager.InstanceDebugInfo;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.ExitCatch;
import com.android.server.HwServiceFactory.IHwBinderMonitor;
import com.android.server.HwServiceFactory.ISystemBlockMonitor;
import com.android.server.am.ActivityManagerService;
import com.android.server.rms.IDaemonRecoverHandler;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Watchdog extends Thread {
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    private static final String[] DAEMONS_TO_CHECK = new String[]{"/system/bin/surfaceflinger"};
    static final boolean DB = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList(new String[]{"android.hardware.audio@2.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.graphics.composer@2.1::IComposer", "android.hardware.vr@1.0::IVr", "android.hardware.media.omx@1.0::IOmx"});
    static final String HUNGTASK_DISABLE = "off";
    static final String HUNGTASK_ENABLE = "on";
    static final String HUNGTASK_FILE = "/sys/kernel/hungtask/vm_heart";
    static final String HUNGTASK_KICK = "kick";
    public static final String[] NATIVE_STACKS_OF_INTEREST = new String[]{"/system/bin/netd", "/system/bin/HwServiceHost", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/installd", "/system/bin/sdcard", "/system/bin/surfaceflinger", "system/bin/displayengineserver", "media.log", "/system/bin/keystore", "media.codec", "media.extractor", "media.codec", "com.android.bluetooth", "/vendor/bin/hw/android.hardware.audio@2.0-service"};
    static final int OVERDUE = 3;
    static final boolean RECORD_KERNEL_THREADS = true;
    static final String TAG = "Watchdog";
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    static Watchdog sWatchdog;
    ActivityManagerService mActivity;
    int mActivityControllerPid;
    boolean mAllowRestart = true;
    IActivityController mController;
    private IDaemonRecoverHandler mDaemonRecoverHandler;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList();
    private boolean mHungtaskExist = false;
    int mHwSystemServerPid;
    private IHwBinderMonitor mIBinderM;
    private IHwIpcChecker mIpcChecker;
    final HandlerChecker mMonitorChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", 60000);
    Monitor mOverdueMonitor;
    int mPhonePid;
    ContentResolver mResolver;
    private ISystemBlockMonitor mSystemBlockMonitor;
    int mSystemUiPid;

    public interface Monitor {
        void monitor();
    }

    private static final class BinderThreadMonitor implements Monitor {
        /* synthetic */ BinderThreadMonitor(BinderThreadMonitor -this0) {
            this();
        }

        private BinderThreadMonitor() {
        }

        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Monitor> mMonitors = new ArrayList();
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        public void addMonitor(Monitor monitor) {
            this.mMonitors.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public boolean isOverdueLocked() {
            return !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + this.mWaitMax;
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            if (latency < this.mWaitMax / 2) {
                return 1;
            }
            if (latency < this.mWaitMax) {
                return 2;
            }
            return 3;
        }

        public Thread getThread() {
            return this.mHandler.getLooper().getThread();
        }

        public String getName() {
            return this.mName;
        }

        public String describeBlockedStateLocked() {
            if (this.mCurrentMonitor == null) {
                return "Blocked in handler on " + this.mName + " (" + getThread().getName() + ")";
            }
            return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName() + " on " + this.mName + " (" + getThread().getName() + ")";
        }

        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (Watchdog.this) {
                    this.mCurrentMonitor = (Monitor) this.mMonitors.get(i);
                }
                Watchdog.this.mOverdueMonitor = this.mCurrentMonitor;
                this.mCurrentMonitor.monitor();
                Watchdog.this.mOverdueMonitor = null;
            }
            synchronized (Watchdog.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }
    }

    final class RebootRequestReceiver extends BroadcastReceiver {
        RebootRequestReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (intent.getIntExtra("nowait", 0) != 0) {
                Watchdog.this.rebootSystem("Received ACTION_REBOOT broadcast");
            } else {
                Slog.w(Watchdog.TAG, "Unsupported ACTION_REBOOT broadcast: " + intent);
            }
        }
    }

    private native void native_dumpKernelStacks(String str);

    public static Watchdog getInstance() {
        if (sWatchdog == null) {
            sWatchdog = new Watchdog();
        }
        return sWatchdog;
    }

    private Watchdog() {
        super("watchdog");
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mHandlerCheckers.add(new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(UiThread.getHandler(), "ui thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(IoThread.getHandler(), "i/o thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(DisplayThread.getHandler(), "display thread", 60000));
        this.mSystemBlockMonitor = HwServiceFactory.getISystemBlockMonitor();
        addMonitor(new BinderThreadMonitor());
        this.mIBinderM = HwServiceFactory.getIHwBinderMonitor();
        this.mIpcChecker = HwServiceFactory.getIHwIpcChecker(this, FgThread.getHandler(), CHECK_INTERVAL);
        this.mDaemonRecoverHandler = HwServiceFactory.getIDaemonRecoverHandler();
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mResolver = context.getContentResolver();
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
        if (this.mSystemBlockMonitor != null) {
            this.mSystemBlockMonitor.init(context, activity);
        }
        this.mHungtaskExist = isHungtaskExist();
        if (this.mHungtaskExist) {
            writeHungtask(HUNGTASK_ENABLE);
        }
    }

    public void processStarted(String name, int pid) {
        synchronized (this) {
            if ("com.android.phone".equals(name)) {
                this.mPhonePid = pid;
            } else if ("com.android.systemui".equals(name)) {
                this.mSystemUiPid = pid;
            } else if ("ActivityController".equals(name)) {
                this.mActivityControllerPid = pid;
            } else if ("com.huawei.systemserver".equals(name)) {
                this.mHwSystemServerPid = pid;
            }
        }
    }

    public void setActivityController(IActivityController controller) {
        synchronized (this) {
            this.mController = controller;
        }
    }

    public void setAllowRestart(boolean allowRestart) {
        synchronized (this) {
            this.mAllowRestart = allowRestart;
        }
    }

    public void addMonitor(Monitor monitor) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Monitors can't be added once the Watchdog is running");
            }
            this.mMonitorChecker.addMonitor(monitor);
        }
        if (this.mSystemBlockMonitor != null) {
            this.mSystemBlockMonitor.addMonitor(monitor);
        }
    }

    public void addIpcMonitor(IHwIpcMonitor monitor) {
        if (monitor != null) {
            synchronized (this) {
                if (this.mIpcChecker != null) {
                    this.mIpcChecker.addMonitor(monitor);
                }
            }
        }
    }

    public void addThread(Handler thread) {
        addThread(thread, 60000);
        if (this.mSystemBlockMonitor != null) {
            this.mSystemBlockMonitor.addThread(thread);
        }
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            if (isAlive()) {
                throw new RuntimeException("Threads can't be added once the Watchdog is running");
            }
            this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
        }
    }

    void rebootSystem(String reason) {
        Slog.i(TAG, "Rebooting system because: " + reason);
        try {
            ((IPowerManager) ServiceManager.getService("power")).reboot(false, reason, false);
        } catch (RemoteException e) {
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            state = Math.max(state, ((HandlerChecker) this.mHandlerCheckers.get(i)).getCompletionStateLocked());
        }
        return state;
    }

    private ArrayList<HandlerChecker> getBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList();
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
            if (hc.isOverdueLocked()) {
                checkers.add(hc);
            }
        }
        return checkers;
    }

    private String describeCheckersLocked(ArrayList<HandlerChecker> checkers) {
        StringBuilder builder = new StringBuilder(128);
        for (int i = 0; i < checkers.size(); i++) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(((HandlerChecker) checkers.get(i)).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    private ArrayList<Integer> getInterestingHalPids() {
        try {
            ArrayList<InstanceDebugInfo> dump = IServiceManager.getService().debugDump();
            HashSet<Integer> pids = new HashSet();
            for (InstanceDebugInfo info : dump) {
                if (info.pid != -1 && HAL_INTERFACES_OF_INTEREST.contains(info.interfaceName)) {
                    pids.add(Integer.valueOf(info.pid));
                }
            }
            return new ArrayList(pids);
        } catch (RemoteException e) {
            return new ArrayList();
        }
    }

    private ArrayList<Integer> getInterestingNativePids() {
        ArrayList<Integer> pids = getInterestingHalPids();
        int[] nativePids = Process.getPidsForCommands(NATIVE_STACKS_OF_INTEREST);
        if (nativePids != null) {
            pids.ensureCapacity(pids.size() + nativePids.length);
            for (int i : nativePids) {
                pids.add(Integer.valueOf(i));
            }
        }
        return pids;
    }

    public void run() {
        if (this.mSystemBlockMonitor != null) {
            this.mSystemBlockMonitor.startRun();
        }
        boolean waitedHalf = false;
        while (true) {
            int debuggerWasConnected = 0;
            synchronized (this) {
                int i;
                if (this.mHungtaskExist) {
                    writeHungtask(HUNGTASK_KICK);
                }
                for (i = 0; i < this.mHandlerCheckers.size(); i++) {
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                }
                if (this.mIpcChecker != null) {
                    this.mIpcChecker.scheduleCheckLocked();
                }
                long start = SystemClock.uptimeMillis();
                for (long timeout = CHECK_INTERVAL; timeout > 0; timeout = CHECK_INTERVAL - (SystemClock.uptimeMillis() - start)) {
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        Log.wtf(TAG, e);
                    }
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                }
                int waitState = evaluateCheckerCompletionLocked();
                if (waitState == 0) {
                    waitedHalf = false;
                } else if (waitState != 1) {
                    ArrayList pids;
                    if (waitState != 2) {
                        IActivityController controller;
                        ArrayList<HandlerChecker> blockedCheckers = getBlockedCheckersLocked();
                        String subject = describeCheckersLocked(blockedCheckers);
                        boolean allowRestart = this.mAllowRestart;
                        if (this.mHungtaskExist) {
                            writeHungtask(HUNGTASK_DISABLE);
                        }
                        EventLog.writeEvent(2802, subject);
                        pids = new ArrayList();
                        pids.add(Integer.valueOf(Process.myPid()));
                        if (this.mPhonePid > 0) {
                            pids.add(Integer.valueOf(this.mPhonePid));
                        }
                        if (this.mSystemUiPid > 0) {
                            pids.add(Integer.valueOf(this.mSystemUiPid));
                        }
                        if (this.mHwSystemServerPid > 0) {
                            pids.add(Integer.valueOf(this.mHwSystemServerPid));
                        }
                        if (this.mIBinderM != null) {
                            this.mIBinderM.addBinderPid(pids, Process.myPid());
                        }
                        if (this.mActivityControllerPid > 0) {
                            pids.add(Integer.valueOf(this.mActivityControllerPid));
                        }
                        File stack = ActivityManagerService.dumpStackTraces(waitedHalf ^ 1, pids, null, null, getInterestingNativePids());
                        SystemClock.sleep(2000);
                        dumpKernelStackTraces();
                        doSysRq('w');
                        doSysRq('l');
                        final String str = subject;
                        final File file = stack;
                        Thread dropboxThread = new Thread("watchdogWriteToDropbox") {
                            public void run() {
                                Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, str, null, file, null);
                            }
                        };
                        dropboxThread.start();
                        try {
                            dropboxThread.join(2000);
                        } catch (InterruptedException e2) {
                        }
                        synchronized (this) {
                            controller = this.mController;
                        }
                        if (controller != null) {
                            Slog.i(TAG, "Reporting stuck state to activity controller");
                            try {
                                Binder.setDumpDisabled("Service dumps disabled due to hung system process.");
                                if (controller.systemNotResponding(subject) >= 0) {
                                    Slog.i(TAG, "Activity controller requested to coninue to wait");
                                    waitedHalf = false;
                                }
                            } catch (RemoteException e3) {
                            }
                        }
                        if (Debug.isDebuggerConnected()) {
                            debuggerWasConnected = 2;
                        }
                        if (debuggerWasConnected >= 2) {
                            Slog.w(TAG, "Debugger connected: Watchdog is *not* killing the system process");
                        } else if (debuggerWasConnected > 0) {
                            Slog.w(TAG, "Debugger was connected: Watchdog is *not* killing the system process");
                        } else if (allowRestart) {
                            Slog.w(TAG, "*** WATCHDOG KILLING SYSTEM PROCESS: " + subject);
                            for (i = 0; i < blockedCheckers.size(); i++) {
                                Slog.w(TAG, ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                                for (StackTraceElement element : ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace()) {
                                    Slog.w(TAG, "    at " + element);
                                }
                            }
                            Slog.w(TAG, "EXITCATCH system_server disable generating tombstone when watchdog happens");
                            ExitCatch.disable(Process.myPid());
                            if (this.mDaemonRecoverHandler.isProcessBlocked(DAEMONS_TO_CHECK)) {
                                lowLevelReboot("Daemons blocked, reboot to recover!");
                            } else {
                                Slog.w(TAG, "*** GOODBYE!");
                                Process.killProcess(Process.myPid());
                                System.exit(10);
                            }
                        } else {
                            Slog.w(TAG, "Restart not allowed: Watchdog is *not* killing the system process");
                        }
                        waitedHalf = false;
                    } else if (!waitedHalf) {
                        if (this.mHungtaskExist) {
                            writeHungtask(HUNGTASK_KICK);
                        }
                        pids = new ArrayList();
                        pids.add(Integer.valueOf(Process.myPid()));
                        ActivityManagerService.dumpStackTraces(true, pids, null, null, getInterestingNativePids());
                        waitedHalf = true;
                        if (this.mIpcChecker != null) {
                            ipcMonitorRecoveryLocked();
                        }
                    }
                }
            }
        }
    }

    private void lowLevelReboot(String reason) {
        Slog.i(TAG, "low level reboot~");
        SystemProperties.set("sys.powerctl", "reboot," + reason);
    }

    private void doSysRq(char c) {
        try {
            FileWriter sysrq_trigger = new FileWriter("/proc/sysrq-trigger");
            sysrq_trigger.write(c);
            sysrq_trigger.close();
        } catch (IOException e) {
            Slog.w(TAG, "Failed to write to /proc/sysrq-trigger", e);
        }
    }

    private File dumpKernelStackTraces() {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        native_dumpKernelStacks(tracesPath);
        return new File(tracesPath);
    }

    public void addKernelLog() {
        dumpKernelStackTraces();
    }

    private boolean isHungtaskExist() {
        if (new File(HUNGTASK_FILE).exists()) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004c A:{SYNTHETIC, Splitter: B:15:0x004c} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005e A:{SYNTHETIC, Splitter: B:21:0x005e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeHungtask(String cmd) {
        IOException e;
        Throwable th;
        OutputStreamWriter fw = null;
        try {
            OutputStreamWriter fw2 = new OutputStreamWriter(new FileOutputStream(HUNGTASK_FILE), "UTF-8");
            try {
                fw2.write(cmd);
                Slog.w(TAG, "hungtask: writing " + cmd);
                if (fw2 != null) {
                    try {
                        fw2.close();
                    } catch (IOException e2) {
                        Slog.e(TAG, "Failed to close /sys/kernel/hungtask/vm_heart", e2);
                    }
                }
                fw = fw2;
            } catch (IOException e3) {
                e2 = e3;
                fw = fw2;
                try {
                    Slog.e(TAG, "Failed to write to /sys/kernel/hungtask/vm_heart", e2);
                    if (fw == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e22) {
                            Slog.e(TAG, "Failed to close /sys/kernel/hungtask/vm_heart", e22);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fw = fw2;
                if (fw != null) {
                }
                throw th;
            }
        } catch (IOException e4) {
            e22 = e4;
            Slog.e(TAG, "Failed to write to /sys/kernel/hungtask/vm_heart", e22);
            if (fw == null) {
                try {
                    fw.close();
                } catch (IOException e222) {
                    Slog.e(TAG, "Failed to close /sys/kernel/hungtask/vm_heart", e222);
                }
            }
        }
    }

    private void ipcMonitorRecoveryLocked() {
        IHwIpcMonitor ipcMonitor = this.mIpcChecker.getCurrentIpcMonitor();
        if (ipcMonitor.getMonitorName() == null) {
            ipcMonitor.action(this.mOverdueMonitor);
        } else {
            ipcMonitor.action();
        }
    }
}

package com.android.server;

import android.app.IActivityController;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.biometrics.face.V1_0.IBiometricsFace;
import android.hardware.health.V2_0.IHealth;
import android.hidl.manager.V1_0.IServiceManager;
import android.os.Binder;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructRlimit;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.os.ExitCatch;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.am.ActivityManagerService;
import com.android.server.wm.SurfaceAnimationThread;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Watchdog extends Thread {
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    static final boolean DB = false;
    public static final boolean DEBUG = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList("android.hardware.audio@2.0::IDevicesFactory", "android.hardware.audio@4.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.graphics.allocator@2.0::IAllocator", "android.hardware.graphics.composer@2.1::IComposer", IHealth.kInterfaceName, "android.hardware.media.c2@1.0::IComponentStore", "android.hardware.media.omx@1.0::IOmx", "android.hardware.media.omx@1.0::IOmxStore", "android.hardware.sensors@1.0::ISensors", "android.hardware.vr@1.0::IVr", IBiometricsFace.kInterfaceName);
    public static final String[] NATIVE_STACKS_OF_INTEREST = {"/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/sdcard", "/system/bin/surfaceflinger", "/system/bin/vold", "/system/bin/installd", "/system/bin/dexoptanalyzer", "media.extractor", "media.metrics", "media.codec", "media.swcodec", "com.android.bluetooth", "/system/bin/statsd", "/vendor/bin/hw/vendor.huawei.hardware.audio@5.0-service"};
    static final int OVERDUE = 3;
    static final String TAG = "Watchdog";
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    static Watchdog sWatchdog;
    ActivityManagerService mActivity;
    boolean mAllowRestart = true;
    private IZrHung mAppEyeBinderBlock;
    IActivityController mController;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList<>();
    final HandlerChecker mMonitorChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", 60000);
    final OpenFdMonitor mOpenFdMonitor;
    int mPhonePid;
    private IZrHung mZrHungAppEyeFwkBlock;
    private IZrHung mZrHungSysHungVmWTG;

    public interface Monitor {
        void monitor();
    }

    public final class HandlerChecker implements Runnable {
        private boolean mCompleted;
        private Monitor mCurrentMonitor;
        private final Handler mHandler;
        private final ArrayList<Monitor> mMonitorQueue = new ArrayList<>();
        private final ArrayList<Monitor> mMonitors = new ArrayList<>();
        private final String mName;
        private int mPauseCount;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = true;
        }

        /* access modifiers changed from: package-private */
        public void addMonitorLocked(Monitor monitor) {
            this.mMonitorQueue.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mCompleted) {
                this.mMonitors.addAll(this.mMonitorQueue);
                this.mMonitorQueue.clear();
            }
            if ((this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) || this.mPauseCount > 0) {
                this.mCompleted = true;
            } else if (this.mCompleted) {
                this.mCompleted = false;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isOverdueLocked() {
            return !this.mCompleted && SystemClock.uptimeMillis() > this.mStartTime + this.mWaitMax;
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return 0;
            }
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            long j = this.mWaitMax;
            if (latency < j / 2) {
                return 1;
            }
            if (latency < j) {
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

        /* access modifiers changed from: package-private */
        public String describeBlockedStateLocked() {
            if (this.mCurrentMonitor == null) {
                return "Blocked in handler on " + this.mName + " (" + getThread().getName() + ")";
            }
            return "Blocked in monitor " + this.mCurrentMonitor.getClass().getName() + " on " + this.mName + " (" + getThread().getName() + ")";
        }

        @Override // java.lang.Runnable
        public void run() {
            int size = this.mMonitors.size();
            for (int i = 0; i < size; i++) {
                synchronized (Watchdog.this) {
                    this.mCurrentMonitor = this.mMonitors.get(i);
                }
                this.mCurrentMonitor.monitor();
            }
            synchronized (Watchdog.this) {
                this.mCompleted = true;
                this.mCurrentMonitor = null;
            }
        }

        public void pauseLocked(String reason) {
            this.mPauseCount++;
            this.mCompleted = true;
            Slog.i(Watchdog.TAG, "Pausing HandlerChecker: " + this.mName + " for reason: " + reason + ". Pause count: " + this.mPauseCount);
        }

        public void resumeLocked(String reason) {
            int i = this.mPauseCount;
            if (i > 0) {
                this.mPauseCount = i - 1;
                Slog.i(Watchdog.TAG, "Resuming HandlerChecker: " + this.mName + " for reason: " + reason + ". Pause count: " + this.mPauseCount);
                return;
            }
            Slog.wtf(Watchdog.TAG, "Already resumed HandlerChecker: " + this.mName);
        }
    }

    /* access modifiers changed from: package-private */
    public final class RebootRequestReceiver extends BroadcastReceiver {
        RebootRequestReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context c, Intent intent) {
            if (intent.getIntExtra("nowait", 0) != 0) {
                Watchdog.this.rebootSystem("Received ACTION_REBOOT broadcast");
                return;
            }
            Slog.w(Watchdog.TAG, "Unsupported ACTION_REBOOT broadcast: " + intent);
        }
    }

    private static final class BinderThreadMonitor implements Monitor {
        private BinderThreadMonitor() {
        }

        @Override // com.android.server.Watchdog.Monitor
        public void monitor() {
            Binder.blockUntilThreadAvailable();
        }
    }

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
        this.mHandlerCheckers.add(new HandlerChecker(AnimationThread.getHandler(), "animation thread", 60000));
        this.mHandlerCheckers.add(new HandlerChecker(SurfaceAnimationThread.getHandler(), "surface animation thread", 60000));
        this.mZrHungAppEyeFwkBlock = HwFrameworkFactory.getZrHung("appeye_frameworkblock");
        this.mAppEyeBinderBlock = HwFrameworkFactory.getZrHung("appeye_ssbinderfull");
        addMonitor(new BinderThreadMonitor());
        this.mOpenFdMonitor = OpenFdMonitor.create();
        this.mZrHungSysHungVmWTG = HwFrameworkFactory.getZrHung("zrhung_wp_vm_watchdog");
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
    }

    public void processStarted(String name, int pid) {
        synchronized (this) {
            if ("com.android.phone".equals(name)) {
                this.mPhonePid = pid;
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
            this.mMonitorChecker.addMonitorLocked(monitor);
        }
    }

    public void addThread(Handler thread) {
        addThread(thread, 60000);
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            this.mHandlerCheckers.add(new HandlerChecker(thread, thread.getLooper().getThread().getName(), timeoutMillis));
        }
    }

    public void pauseWatchingCurrentThread(String reason) {
        synchronized (this) {
            Iterator<HandlerChecker> it = this.mHandlerCheckers.iterator();
            while (it.hasNext()) {
                HandlerChecker hc = it.next();
                if (Thread.currentThread().equals(hc.getThread())) {
                    hc.pauseLocked(reason);
                }
            }
        }
    }

    public void resumeWatchingCurrentThread(String reason) {
        synchronized (this) {
            Iterator<HandlerChecker> it = this.mHandlerCheckers.iterator();
            while (it.hasNext()) {
                HandlerChecker hc = it.next();
                if (Thread.currentThread().equals(hc.getThread())) {
                    hc.resumeLocked(reason);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void rebootSystem(String reason) {
        Slog.i(TAG, "Rebooting system because: " + reason);
        try {
            ServiceManager.getService("power").reboot(false, reason, false);
        } catch (RemoteException e) {
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = 0;
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            state = Math.max(state, this.mHandlerCheckers.get(i).getCompletionStateLocked());
        }
        return state;
    }

    private ArrayList<HandlerChecker> getBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList<>();
        for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
            HandlerChecker hc = this.mHandlerCheckers.get(i);
            if (hc.isOverdueLocked()) {
                checkers.add(hc);
            }
        }
        return checkers;
    }

    private String describeCheckersLocked(List<HandlerChecker> checkers) {
        StringBuilder builder = new StringBuilder(128);
        for (int i = 0; i < checkers.size(); i++) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(checkers.get(i).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    private static ArrayList<Integer> getInterestingHalPids() {
        try {
            ArrayList<IServiceManager.InstanceDebugInfo> dump = IServiceManager.getService().debugDump();
            HashSet<Integer> pids = new HashSet<>();
            Iterator<IServiceManager.InstanceDebugInfo> it = dump.iterator();
            while (it.hasNext()) {
                IServiceManager.InstanceDebugInfo info = it.next();
                if (info.pid != -1) {
                    if (HAL_INTERFACES_OF_INTEREST.contains(info.interfaceName)) {
                        pids.add(Integer.valueOf(info.pid));
                    }
                }
            }
            return new ArrayList<>(pids);
        } catch (RemoteException e) {
            return new ArrayList<>();
        }
    }

    static ArrayList<Integer> getInterestingNativePids() {
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

    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        Throwable th;
        List<HandlerChecker> blockedCheckers;
        final String subject;
        boolean allowRestart;
        IActivityController controller;
        IZrHung iZrHung = this.mZrHungAppEyeFwkBlock;
        IActivityController controller2 = null;
        if (iZrHung != null) {
            iZrHung.start((ZrHungData) null);
        }
        boolean waitedHalf = false;
        while (true) {
            int debuggerWasConnected = 0;
            synchronized (this) {
                for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
                    try {
                        try {
                            this.mHandlerCheckers.get(i).scheduleCheckLocked();
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
                if (0 > 0) {
                    debuggerWasConnected = 0 - 1;
                }
                long start = SystemClock.uptimeMillis();
                for (long timeout = 30000; timeout > 0; timeout = 30000 - (SystemClock.uptimeMillis() - start)) {
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
                boolean fdLimitTriggered = false;
                if (this.mOpenFdMonitor != null) {
                    fdLimitTriggered = this.mOpenFdMonitor.monitor();
                }
                if (!fdLimitTriggered) {
                    int waitState = evaluateCheckerCompletionLocked();
                    if (waitState == 0) {
                        waitedHalf = false;
                    } else if (waitState != 1) {
                        if (waitState != 2) {
                            blockedCheckers = getBlockedCheckersLocked();
                            subject = describeCheckersLocked(blockedCheckers);
                        } else if (!waitedHalf) {
                            Slog.i(TAG, "WAITED_HALF");
                            ArrayList<Integer> pids = new ArrayList<>();
                            pids.add(Integer.valueOf(Process.myPid()));
                            ActivityManagerService.dumpStackTraces(pids, (ProcessCpuTracker) controller2, (SparseArray<Boolean>) controller2, getInterestingNativePids());
                            waitedHalf = true;
                            if (this.mZrHungSysHungVmWTG != null) {
                                ZrHungData arg = new ZrHungData();
                                arg.putInt("waitState", 2);
                                this.mZrHungSysHungVmWTG.check(arg);
                            }
                        }
                    }
                } else {
                    blockedCheckers = Collections.emptyList();
                    subject = "Open FD high water mark reached";
                }
                allowRestart = this.mAllowRestart;
            }
            EventLog.writeEvent((int) EventLogTags.WATCHDOG, subject);
            ArrayList<Integer> pids2 = new ArrayList<>();
            pids2.add(Integer.valueOf(Process.myPid()));
            int i2 = this.mPhonePid;
            if (i2 > 0) {
                pids2.add(Integer.valueOf(i2));
            }
            ArrayList<Integer> nativePids = getInterestingNativePids();
            if ((this.mAppEyeBinderBlock == null || blockedCheckers == null || blockedCheckers.size() <= 0) ? false : true) {
                int blockedTid = (int) blockedCheckers.get(0).getThread().getId();
                ZrHungData data = new ZrHungData();
                data.putString("method", "addBinderPid");
                data.putIntegerArrayList("notnativepids", pids2);
                data.putIntegerArrayList("nativepids", nativePids);
                data.putInt("pid", Process.myPid());
                data.putInt("tid", blockedTid);
                this.mAppEyeBinderBlock.check(data);
            }
            final File stack = ActivityManagerService.dumpStackTraces(pids2, (ProcessCpuTracker) controller2, (SparseArray<Boolean>) controller2, getInterestingNativePids());
            SystemClock.sleep(5000);
            doSysRq('w');
            doSysRq('l');
            Thread dropboxThread = new Thread("watchdogWriteToDropbox") {
                /* class com.android.server.Watchdog.AnonymousClass1 */

                @Override // java.lang.Thread, java.lang.Runnable
                public void run() {
                    if (Watchdog.this.mActivity != null) {
                        Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, null, subject, null, stack, null);
                    }
                    StatsLog.write(185, subject);
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
                        controller2 = null;
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
            } else if (!allowRestart) {
                Slog.w(TAG, "Restart not allowed: Watchdog is *not* killing the system process");
            } else {
                Slog.w(TAG, "*** WATCHDOG KILLING SYSTEM PROCESS: " + subject);
                WatchdogDiagnostics.diagnoseCheckers(blockedCheckers);
                if (this.mZrHungSysHungVmWTG != null) {
                    ZrHungData arg2 = new ZrHungData();
                    arg2.putInt("waitState", 3);
                    if (!this.mZrHungSysHungVmWTG.check(arg2)) {
                        Slog.w(TAG, "EXITCATCH system_server disable when watchdog");
                        ExitCatch.disable(Process.myPid());
                        Slog.w(TAG, "*** GOODBYE!");
                        Process.killProcess(Process.myPid());
                        System.exit(10);
                    }
                }
            }
            waitedHalf = false;
            controller2 = null;
        }
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

    public static final class OpenFdMonitor {
        private static final int FD_HIGH_WATER_MARK = 12;
        private final File mDumpDir;
        private final File mFdHighWaterMark;

        public static OpenFdMonitor create() {
            if (!Build.IS_DEBUGGABLE) {
                return null;
            }
            try {
                StructRlimit rlimit = Os.getrlimit(OsConstants.RLIMIT_NOFILE);
                return new OpenFdMonitor(new File(ActivityManagerService.ANR_TRACE_DIR), new File("/proc/self/fd/" + (rlimit.rlim_cur - 12)));
            } catch (ErrnoException errno) {
                Slog.w(Watchdog.TAG, "Error thrown from getrlimit(RLIMIT_NOFILE)", errno);
                return null;
            }
        }

        OpenFdMonitor(File dumpDir, File fdThreshold) {
            this.mDumpDir = dumpDir;
            this.mFdHighWaterMark = fdThreshold;
        }

        private void dumpOpenDescriptors() {
            String resolvedPath;
            List<String> dumpInfo = new ArrayList<>();
            String fdDirPath = String.format("/proc/%d/fd/", Integer.valueOf(Process.myPid()));
            File[] fds = new File(fdDirPath).listFiles();
            if (fds == null) {
                dumpInfo.add("Unable to list " + fdDirPath);
            } else {
                for (File f : fds) {
                    String fdSymLink = f.getAbsolutePath();
                    try {
                        resolvedPath = Os.readlink(fdSymLink);
                    } catch (ErrnoException ex) {
                        resolvedPath = ex.getMessage();
                    }
                    dumpInfo.add(fdSymLink + "\t" + resolvedPath);
                }
            }
            try {
                Files.write(Paths.get(File.createTempFile("anr_fd_", "", this.mDumpDir).getAbsolutePath(), new String[0]), dumpInfo, StandardCharsets.UTF_8, new OpenOption[0]);
            } catch (IOException ex2) {
                Slog.w(Watchdog.TAG, "Unable to write open descriptors to file: " + ex2);
            }
        }

        public boolean monitor() {
            if (!this.mFdHighWaterMark.exists()) {
                return false;
            }
            dumpOpenDescriptors();
            return true;
        }
    }

    public void addKernelLog() {
    }
}

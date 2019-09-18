package com.android.server;

import android.app.IActivityController;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructRlimit;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.os.ExitCatch;
import com.android.internal.os.ProcessCpuTracker;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityManagerService;
import com.android.server.rms.IHwIpcChecker;
import com.android.server.rms.IHwIpcMonitor;
import com.android.server.zrhung.IZRHungService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Watchdog extends Thread {
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    private static final String[] DAEMONS_TO_CHECK = {"/system/bin/surfaceflinger"};
    static final boolean DB = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final List<String> HAL_INTERFACES_OF_INTEREST = Arrays.asList(new String[]{"android.hardware.audio@2.0::IDevicesFactory", "android.hardware.audio@4.0::IDevicesFactory", "android.hardware.bluetooth@1.0::IBluetoothHci", "android.hardware.camera.provider@2.4::ICameraProvider", "android.hardware.graphics.composer@2.1::IComposer", "android.hardware.media.omx@1.0::IOmx", "android.hardware.media.omx@1.0::IOmxStore", "android.hardware.sensors@1.0::ISensors", "android.hardware.vr@1.0::IVr", "vendor.huawei.hardware.motion@1.0::IMotion"});
    static final String HUNGTASK_DISABLE = "off";
    static final String HUNGTASK_ENABLE = "on";
    static final String HUNGTASK_FILE = "/sys/kernel/hungtask/vm_heart";
    static final String HUNGTASK_KICK = "kick";
    public static final String[] NATIVE_STACKS_OF_INTEREST = {"/system/bin/netd", "/system/bin/HwServiceHost", "/system/bin/audioserver", "/system/bin/cameraserver", "/system/bin/drmserver", "/system/bin/mediadrmserver", "/system/bin/mediaserver", "/system/bin/installd", "/system/bin/sdcard", "/system/bin/surfaceflinger", "media.log", "/system/bin/keystore", "media.codec", "media.extractor", "media.metrics", "media.codec", "com.android.bluetooth", "statsd", "/vendor/bin/hw/vendor.huawei.hardware.audio@4.0-service"};
    static final int OVERDUE = 3;
    static final boolean RECORD_KERNEL_THREADS = true;
    static final String TAG = "Watchdog";
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    static Watchdog sWatchdog;
    ActivityManagerService mActivity;
    int mActivityControllerPid;
    boolean mAllowAddToDropbox = true;
    boolean mAllowRestart = true;
    private IZrHung mAppEyeBinderBlock;
    IActivityController mController;
    final ArrayList<HandlerChecker> mHandlerCheckers = new ArrayList<>();
    private boolean mHungtaskExist = false;
    int mHwSystemServerPid;
    private HwServiceFactory.IHwBinderMonitor mIBinderM;
    private IHwIpcChecker mIpcChecker;
    final HandlerChecker mMonitorChecker;
    final OpenFdMonitor mOpenFdMonitor;
    Monitor mOverdueMonitor;
    int mPhonePid;
    ContentResolver mResolver;
    int mSystemUiPid;
    private IZrHung mZrHungAppEyeFwkBlock;
    private IZrHung mZrHungSysHungVmWTG;

    private static final class BinderThreadMonitor implements Monitor {
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
        private final ArrayList<Monitor> mMonitors = new ArrayList<>();
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
                    this.mCurrentMonitor = this.mMonitors.get(i);
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

    public interface Monitor {
        void monitor();
    }

    public static final class OpenFdMonitor {
        private static final int FD_HIGH_WATER_MARK = 12;
        private final File mDumpDir;
        private final File mFdHighWaterMark;

        public static OpenFdMonitor create() {
            if (!Build.IS_DEBUGGABLE) {
                return null;
            }
            String dumpDirStr = SystemProperties.get("dalvik.vm.stack-trace-dir", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            if (dumpDirStr.isEmpty()) {
                return null;
            }
            try {
                StructRlimit rlimit = Os.getrlimit(OsConstants.RLIMIT_NOFILE);
                return new OpenFdMonitor(new File(dumpDirStr), new File("/proc/self/fd/" + (rlimit.rlim_cur - 12)));
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
            try {
                File dumpFile = File.createTempFile("anr_fd_", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, this.mDumpDir);
                int returnCode = new ProcessBuilder(new String[0]).command(new String[]{"/system/bin/lsof", "-p", String.valueOf(Process.myPid())}).redirectErrorStream(true).redirectOutput(dumpFile).start().waitFor();
                if (returnCode != 0) {
                    Slog.w(Watchdog.TAG, "Unable to dump open descriptors, lsof return code: " + returnCode);
                    dumpFile.delete();
                }
            } catch (IOException | InterruptedException ex) {
                Slog.w(Watchdog.TAG, "Unable to dump open descriptors: " + ex);
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

    final class RebootRequestReceiver extends BroadcastReceiver {
        RebootRequestReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (intent.getIntExtra("nowait", 0) != 0) {
                Watchdog.this.rebootSystem("Received ACTION_REBOOT broadcast");
                return;
            }
            Slog.w(Watchdog.TAG, "Unsupported ACTION_REBOOT broadcast: " + intent);
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
        HandlerChecker handlerChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", 60000);
        this.mMonitorChecker = handlerChecker;
        this.mHandlerCheckers.add(this.mMonitorChecker);
        ArrayList<HandlerChecker> arrayList = this.mHandlerCheckers;
        HandlerChecker handlerChecker2 = new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", 60000);
        arrayList.add(handlerChecker2);
        ArrayList<HandlerChecker> arrayList2 = this.mHandlerCheckers;
        HandlerChecker handlerChecker3 = new HandlerChecker(UiThread.getHandler(), "ui thread", 60000);
        arrayList2.add(handlerChecker3);
        ArrayList<HandlerChecker> arrayList3 = this.mHandlerCheckers;
        HandlerChecker handlerChecker4 = new HandlerChecker(IoThread.getHandler(), "i/o thread", 60000);
        arrayList3.add(handlerChecker4);
        ArrayList<HandlerChecker> arrayList4 = this.mHandlerCheckers;
        HandlerChecker handlerChecker5 = new HandlerChecker(DisplayThread.getHandler(), "display thread", 60000);
        arrayList4.add(handlerChecker5);
        this.mZrHungAppEyeFwkBlock = HwFrameworkFactory.getZrHung("appeye_frameworkblock");
        this.mAppEyeBinderBlock = HwFrameworkFactory.getZrHung("appeye_ssbinderfull");
        addMonitor(new BinderThreadMonitor());
        this.mOpenFdMonitor = OpenFdMonitor.create();
        this.mIBinderM = HwServiceFactory.getIHwBinderMonitor();
        this.mIpcChecker = HwServiceFactory.getIHwIpcChecker(this, FgThread.getHandler(), 30000);
        this.mZrHungSysHungVmWTG = HwFrameworkFactory.getZrHung("zrhung_wp_vm_watchdog");
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mResolver = context.getContentResolver();
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
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
            this.mAllowAddToDropbox = true;
        }
    }

    public void setAllowRestart(boolean allowRestart) {
        synchronized (this) {
            this.mAllowRestart = allowRestart;
        }
    }

    public void addMonitor(Monitor monitor) {
        synchronized (this) {
            if (!isAlive()) {
                this.mMonitorChecker.addMonitor(monitor);
            } else {
                throw new RuntimeException("Monitors can't be added once the Watchdog is running");
            }
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
    }

    public void addThread(Handler thread, long timeoutMillis) {
        synchronized (this) {
            if (!isAlive()) {
                String name = thread.getLooper().getThread().getName();
                ArrayList<HandlerChecker> arrayList = this.mHandlerCheckers;
                HandlerChecker handlerChecker = new HandlerChecker(thread, name, timeoutMillis);
                arrayList.add(handlerChecker);
            } else {
                throw new RuntimeException("Threads can't be added once the Watchdog is running");
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

    private ArrayList<Integer> getInterestingHalPids() {
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
        List<HandlerChecker> blockedCheckers;
        final String subject;
        IActivityController controller;
        ProcessCpuTracker processCpuTracker = null;
        if (this.mZrHungAppEyeFwkBlock != null) {
            this.mZrHungAppEyeFwkBlock.start(null);
        }
        boolean fdLimitTriggered = false;
        while (true) {
            boolean waitedHalf = fdLimitTriggered;
            int debuggerWasConnected = 0;
            synchronized (this) {
                if (this.mHungtaskExist) {
                    writeHungtask(HUNGTASK_KICK);
                }
                for (int i = 0; i < this.mHandlerCheckers.size(); i++) {
                    this.mHandlerCheckers.get(i).scheduleCheckLocked();
                }
                if (this.mIpcChecker != null) {
                    this.mIpcChecker.scheduleCheckLocked();
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
                        InterruptedException interruptedException = e;
                        Log.wtf(TAG, e);
                    }
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = 2;
                    }
                }
                boolean fdLimitTriggered2 = false;
                if (this.mOpenFdMonitor != null) {
                    fdLimitTriggered2 = this.mOpenFdMonitor.monitor();
                }
                if (!fdLimitTriggered2) {
                    int waitState = evaluateCheckerCompletionLocked();
                    if (waitState == 0) {
                        waitedHalf = false;
                    } else if (waitState != 1) {
                        if (waitState != 2) {
                            blockedCheckers = getBlockedCheckersLocked();
                            subject = describeCheckersLocked(blockedCheckers);
                        } else if (!waitedHalf) {
                            if (this.mHungtaskExist) {
                                writeHungtask(HUNGTASK_KICK);
                            }
                            ArrayList<Integer> pids = new ArrayList<>();
                            pids.add(Integer.valueOf(Process.myPid()));
                            ActivityManagerService.dumpStackTraces(true, pids, processCpuTracker, (SparseArray<Boolean>) processCpuTracker, getInterestingNativePids());
                            waitedHalf = true;
                            if (this.mIpcChecker != null) {
                                ipcMonitorRecoveryLocked();
                            }
                            if (this.mZrHungSysHungVmWTG != null) {
                                ZrHungData arg = new ZrHungData();
                                arg.putInt("waitState", 2);
                                this.mZrHungSysHungVmWTG.check(arg);
                            }
                        }
                    }
                    fdLimitTriggered = waitedHalf;
                } else {
                    blockedCheckers = Collections.emptyList();
                    subject = "Open FD high water mark reached";
                }
                boolean allowRestart = this.mAllowRestart;
                List<HandlerChecker> list = blockedCheckers;
                if (this.mHungtaskExist) {
                    writeHungtask(HUNGTASK_DISABLE);
                }
                EventLog.writeEvent(EventLogTags.WATCHDOG, subject);
                ArrayList arrayList = new ArrayList();
                arrayList.add(Integer.valueOf(Process.myPid()));
                if (this.mPhonePid > 0) {
                    arrayList.add(Integer.valueOf(this.mPhonePid));
                }
                if (this.mSystemUiPid > 0) {
                    arrayList.add(Integer.valueOf(this.mSystemUiPid));
                }
                if (this.mHwSystemServerPid > 0) {
                    arrayList.add(Integer.valueOf(this.mHwSystemServerPid));
                }
                if (this.mActivityControllerPid > 0) {
                    arrayList.add(Integer.valueOf(this.mActivityControllerPid));
                }
                ArrayList<Integer> nativePids = getInterestingNativePids();
                if (this.mAppEyeBinderBlock != null) {
                    int blockedTid = (int) list.get(0).getThread().getId();
                    ZrHungData data = new ZrHungData();
                    data.putString("method", "addBinderPid");
                    data.putIntegerArrayList("notnativepids", arrayList);
                    data.putIntegerArrayList("nativepids", nativePids);
                    data.putInt(IZRHungService.PARAM_PID, Process.myPid());
                    data.putInt("tid", blockedTid);
                    this.mAppEyeBinderBlock.check(data);
                }
                final File stack = ActivityManagerService.dumpStackTraces(!waitedHalf, (ArrayList<Integer>) arrayList, processCpuTracker, (SparseArray<Boolean>) processCpuTracker, nativePids);
                SystemClock.sleep(2000);
                doSysRq('w');
                doSysRq('l');
                if (this.mAllowAddToDropbox) {
                    Thread dropboxThread = new Thread("watchdogWriteToDropbox") {
                        public void run() {
                            Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, subject, null, stack, null);
                        }
                    };
                    dropboxThread.start();
                    try {
                        dropboxThread.join(2000);
                    } catch (InterruptedException e2) {
                    }
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
                            this.mAllowAddToDropbox = false;
                            fdLimitTriggered = false;
                            processCpuTracker = null;
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
                    Slog.w(TAG, "EXITCATCH system_server disable generating tombstone when watchdog happens");
                    ExitCatch.disable(Process.myPid());
                    if (this.mZrHungSysHungVmWTG != null) {
                        ZrHungData arg2 = new ZrHungData();
                        arg2.putInt("waitState", 3);
                        if (!this.mZrHungSysHungVmWTG.check(arg2)) {
                            Slog.w(TAG, "*** GOODBYE!");
                            Process.killProcess(Process.myPid());
                            System.exit(10);
                        }
                    }
                }
                fdLimitTriggered = false;
                processCpuTracker = null;
            }
        }
        while (true) {
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

    private File dumpKernelStackTraces() {
        String tracesPath = SystemProperties.get("dalvik.vm.stack-trace-file", null);
        if (tracesPath == null || tracesPath.length() == 0) {
            return null;
        }
        return new File(tracesPath);
    }

    public void addKernelLog() {
    }

    private boolean isHungtaskExist() {
        if (!new File(HUNGTASK_FILE).exists()) {
            return false;
        }
        return true;
    }

    private void writeHungtask(String cmd) {
        OutputStreamWriter fw = null;
        try {
            fw = new OutputStreamWriter(new FileOutputStream(HUNGTASK_FILE), "UTF-8");
            fw.write(cmd);
            Slog.w(TAG, "hungtask: writing " + cmd);
            try {
                fw.close();
            } catch (IOException e) {
                Slog.e(TAG, "Failed to close /sys/kernel/hungtask/vm_heart", e);
            }
        } catch (IOException e2) {
            Slog.e(TAG, "Failed to write to /sys/kernel/hungtask/vm_heart", e2);
            if (fw != null) {
                fw.close();
            }
        } catch (Throwable th) {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e3) {
                    Slog.e(TAG, "Failed to close /sys/kernel/hungtask/vm_heart", e3);
                }
            }
            throw th;
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

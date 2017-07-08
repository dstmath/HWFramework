package com.android.server;

import android.app.IActivityController;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.android.server.HwServiceFactory.IHwBinderMonitor;
import com.android.server.am.ActivityManagerService;
import com.android.server.radar.FrameworkRadar;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Watchdog extends Thread {
    static final long CHECK_INTERVAL = 30000;
    static final int COMPLETED = 0;
    static final boolean DB = false;
    static final long DEFAULT_TIMEOUT = 60000;
    public static final String[] NATIVE_STACKS_OF_INTEREST = null;
    static final int OVERDUE = 3;
    static final boolean RECORD_KERNEL_THREADS = true;
    static final String TAG = "Watchdog";
    static final int WAITED_HALF = 2;
    static final int WAITING = 1;
    static Watchdog sWatchdog;
    ActivityManagerService mActivity;
    int mActivityControllerPid;
    boolean mAllowRestart;
    IActivityController mController;
    final ArrayList<HandlerChecker> mHandlerCheckers;
    private IHwBinderMonitor mIBinderM;
    final HandlerChecker mMonitorChecker;
    int mPhonePid;
    ContentResolver mResolver;

    public interface Monitor {
        void monitor();
    }

    /* renamed from: com.android.server.Watchdog.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ File val$stack;
        final /* synthetic */ String val$subject;

        AnonymousClass1(String $anonymous0, String val$subject, File val$stack) {
            this.val$subject = val$subject;
            this.val$stack = val$stack;
            super($anonymous0);
        }

        public void run() {
            Watchdog.this.mActivity.addErrorToDropBox("watchdog", null, "system_server", null, null, this.val$subject, null, this.val$stack, null);
        }
    }

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
        private final ArrayList<Monitor> mMonitors;
        private final String mName;
        private long mStartTime;
        private final long mWaitMax;

        HandlerChecker(Handler handler, String name, long waitMaxMillis) {
            this.mMonitors = new ArrayList();
            this.mHandler = handler;
            this.mName = name;
            this.mWaitMax = waitMaxMillis;
            this.mCompleted = Watchdog.RECORD_KERNEL_THREADS;
        }

        public void addMonitor(Monitor monitor) {
            this.mMonitors.add(monitor);
        }

        public void scheduleCheckLocked() {
            if (this.mMonitors.size() == 0 && this.mHandler.getLooper().getQueue().isPolling()) {
                this.mCompleted = Watchdog.RECORD_KERNEL_THREADS;
            } else if (this.mCompleted) {
                this.mCompleted = Watchdog.DB;
                this.mCurrentMonitor = null;
                this.mStartTime = SystemClock.uptimeMillis();
                this.mHandler.postAtFrontOfQueue(this);
            }
        }

        public boolean isOverdueLocked() {
            return (this.mCompleted || SystemClock.uptimeMillis() <= this.mStartTime + this.mWaitMax) ? Watchdog.DB : Watchdog.RECORD_KERNEL_THREADS;
        }

        public int getCompletionStateLocked() {
            if (this.mCompleted) {
                return Watchdog.COMPLETED;
            }
            long latency = SystemClock.uptimeMillis() - this.mStartTime;
            if (latency < this.mWaitMax / 2) {
                return Watchdog.WAITING;
            }
            if (latency < this.mWaitMax) {
                return Watchdog.WAITED_HALF;
            }
            return Watchdog.OVERDUE;
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
            for (int i = Watchdog.COMPLETED; i < size; i += Watchdog.WAITING) {
                synchronized (Watchdog.this) {
                    this.mCurrentMonitor = (Monitor) this.mMonitors.get(i);
                }
                this.mCurrentMonitor.monitor();
            }
            synchronized (Watchdog.this) {
                this.mCompleted = Watchdog.RECORD_KERNEL_THREADS;
                this.mCurrentMonitor = null;
            }
        }
    }

    final class RebootRequestReceiver extends BroadcastReceiver {
        RebootRequestReceiver() {
        }

        public void onReceive(Context c, Intent intent) {
            if (intent.getIntExtra("nowait", Watchdog.COMPLETED) != 0) {
                Watchdog.this.rebootSystem("Received ACTION_REBOOT broadcast");
            } else {
                Slog.w(Watchdog.TAG, "Unsupported ACTION_REBOOT broadcast: " + intent);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.Watchdog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.Watchdog.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.Watchdog.<clinit>():void");
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
        this.mHandlerCheckers = new ArrayList();
        this.mAllowRestart = RECORD_KERNEL_THREADS;
        this.mMonitorChecker = new HandlerChecker(FgThread.getHandler(), "foreground thread", DEFAULT_TIMEOUT);
        this.mHandlerCheckers.add(this.mMonitorChecker);
        this.mHandlerCheckers.add(new HandlerChecker(new Handler(Looper.getMainLooper()), "main thread", DEFAULT_TIMEOUT));
        this.mHandlerCheckers.add(new HandlerChecker(UiThread.getHandler(), "ui thread", DEFAULT_TIMEOUT));
        this.mHandlerCheckers.add(new HandlerChecker(IoThread.getHandler(), "i/o thread", DEFAULT_TIMEOUT));
        this.mHandlerCheckers.add(new HandlerChecker(DisplayThread.getHandler(), "display thread", DEFAULT_TIMEOUT));
        addMonitor(new BinderThreadMonitor());
        this.mIBinderM = HwServiceFactory.getIHwBinderMonitor();
    }

    public void init(Context context, ActivityManagerService activity) {
        this.mResolver = context.getContentResolver();
        this.mActivity = activity;
        context.registerReceiver(new RebootRequestReceiver(), new IntentFilter("android.intent.action.REBOOT"), "android.permission.REBOOT", null);
    }

    public void processStarted(String name, int pid) {
        synchronized (this) {
            if ("com.android.phone".equals(name)) {
                this.mPhonePid = pid;
            } else if ("ActivityController".equals(name)) {
                this.mActivityControllerPid = pid;
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
    }

    public void addThread(Handler thread) {
        addThread(thread, DEFAULT_TIMEOUT);
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
            ((IPowerManager) ServiceManager.getService("power")).reboot(DB, reason, DB);
        } catch (RemoteException e) {
        }
    }

    private int evaluateCheckerCompletionLocked() {
        int state = COMPLETED;
        for (int i = COMPLETED; i < this.mHandlerCheckers.size(); i += WAITING) {
            state = Math.max(state, ((HandlerChecker) this.mHandlerCheckers.get(i)).getCompletionStateLocked());
        }
        return state;
    }

    private ArrayList<HandlerChecker> getBlockedCheckersLocked() {
        ArrayList<HandlerChecker> checkers = new ArrayList();
        for (int i = COMPLETED; i < this.mHandlerCheckers.size(); i += WAITING) {
            HandlerChecker hc = (HandlerChecker) this.mHandlerCheckers.get(i);
            if (hc.isOverdueLocked()) {
                checkers.add(hc);
            }
        }
        return checkers;
    }

    private String describeCheckersLocked(ArrayList<HandlerChecker> checkers) {
        StringBuilder builder = new StringBuilder(DumpState.DUMP_PACKAGES);
        for (int i = COMPLETED; i < checkers.size(); i += WAITING) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(((HandlerChecker) checkers.get(i)).describeBlockedStateLocked());
        }
        return builder.toString();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        boolean waitedHalf = DB;
        loop0:
        while (true) {
            int debuggerWasConnected = COMPLETED;
            synchronized (this) {
                int i = COMPLETED;
                while (true) {
                    if (i >= this.mHandlerCheckers.size()) {
                        break;
                    }
                    ((HandlerChecker) this.mHandlerCheckers.get(i)).scheduleCheckLocked();
                    i += WAITING;
                }
                long start = SystemClock.uptimeMillis();
                for (long timeout = CHECK_INTERVAL; timeout > 0; timeout = CHECK_INTERVAL - (SystemClock.uptimeMillis() - start)) {
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = WAITED_HALF;
                    }
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        Log.wtf(TAG, e);
                    }
                    if (Debug.isDebuggerConnected()) {
                        debuggerWasConnected = WAITED_HALF;
                    }
                }
                int waitState = evaluateCheckerCompletionLocked();
                if (waitState == 0) {
                    waitedHalf = DB;
                } else if (waitState != WAITING) {
                    ArrayList pids;
                    if (waitState != WAITED_HALF) {
                        IActivityController controller;
                        ArrayList<HandlerChecker> blockedCheckers = getBlockedCheckersLocked();
                        String subject = describeCheckersLocked(blockedCheckers);
                        boolean allowRestart = this.mAllowRestart;
                        EventLog.writeEvent(FrameworkRadar.RADAR_FWK_ERR_APP_CRASH_AT_START, subject);
                        pids = new ArrayList();
                        pids.add(Integer.valueOf(Process.myPid()));
                        if (this.mPhonePid > 0) {
                            pids.add(Integer.valueOf(this.mPhonePid));
                        }
                        if (this.mIBinderM != null) {
                            this.mIBinderM.addBinderPid(pids, Process.myPid());
                        }
                        if (this.mActivityControllerPid > 0) {
                            pids.add(Integer.valueOf(this.mActivityControllerPid));
                        }
                        File stack = ActivityManagerService.dumpStackTraces(waitedHalf ? DB : RECORD_KERNEL_THREADS, pids, null, null, NATIVE_STACKS_OF_INTEREST);
                        SystemClock.sleep(2000);
                        dumpKernelStackTraces();
                        doSysRq('w');
                        doSysRq('l');
                        Thread dropboxThread = new AnonymousClass1("watchdogWriteToDropbox", subject, stack);
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
                                    waitedHalf = DB;
                                }
                            } catch (RemoteException e3) {
                            }
                        }
                        if (Debug.isDebuggerConnected()) {
                            debuggerWasConnected = WAITED_HALF;
                        }
                        if (debuggerWasConnected >= WAITED_HALF) {
                            Slog.w(TAG, "Debugger connected: Watchdog is *not* killing the system process");
                        } else if (debuggerWasConnected > 0) {
                            Slog.w(TAG, "Debugger was connected: Watchdog is *not* killing the system process");
                        } else if (allowRestart) {
                            Slog.w(TAG, "*** WATCHDOG KILLING SYSTEM PROCESS: " + subject);
                            for (i = COMPLETED; i < blockedCheckers.size(); i += WAITING) {
                                Slog.w(TAG, ((HandlerChecker) blockedCheckers.get(i)).getName() + " stack trace:");
                                StackTraceElement[] stackTrace = ((HandlerChecker) blockedCheckers.get(i)).getThread().getStackTrace();
                                int length = stackTrace.length;
                                for (int i2 = COMPLETED; i2 < length; i2 += WAITING) {
                                    Slog.w(TAG, "    at " + stackTrace[i2]);
                                }
                            }
                            Slog.w(TAG, "*** GOODBYE!");
                            Process.killProcess(Process.myPid());
                            System.exit(10);
                        } else {
                            Slog.w(TAG, "Restart not allowed: Watchdog is *not* killing the system process");
                        }
                        waitedHalf = DB;
                    } else if (!waitedHalf) {
                        pids = new ArrayList();
                        pids.add(Integer.valueOf(Process.myPid()));
                        ActivityManagerService.dumpStackTraces(RECORD_KERNEL_THREADS, pids, null, null, NATIVE_STACKS_OF_INTEREST);
                        waitedHalf = RECORD_KERNEL_THREADS;
                    }
                }
            }
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
        native_dumpKernelStacks(tracesPath);
        return new File(tracesPath);
    }

    public void addKernelLog() {
        dumpKernelStackTraces();
    }
}

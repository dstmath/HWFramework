package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityContainer;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.app.usage.ConfigurationStats;
import android.app.usage.IUsageStatsManager;
import android.content.ComponentName;
import android.content.IIntentReceiver.Stub;
import android.content.Intent;
import android.content.Intent.CommandOptionHandler;
import android.content.pm.IPackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.DisplayMetrics;
import com.android.internal.util.HexDump;
import com.android.internal.util.Preconditions;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class ActivityManagerShellCommand extends ShellCommand {
    private static final boolean GREATER_THAN_TARGET = true;
    private static final boolean MOVING_FORWARD = true;
    private static final boolean MOVING_HORIZONTALLY = true;
    public static final String NO_CLASS_ERROR_CODE = "Error type 3";
    private static final String SHELL_PACKAGE_NAME = "com.android.shell";
    private static final int STACK_BOUNDS_INSET = 10;
    private boolean mAutoStop;
    private int mDisplayId;
    final boolean mDumping;
    final IActivityManager mInterface;
    final ActivityManagerService mInternal;
    private boolean mIsTaskOverlay;
    final IPackageManager mPm;
    private String mProfileFile;
    private String mReceiverPermission;
    private int mRepeat = 0;
    private int mSamplingInterval;
    private int mStackId;
    private int mStartFlags = 0;
    private boolean mStopOption = false;
    private boolean mStreaming;
    private int mTaskId;
    private int mUserId;
    private boolean mWaitOption = false;

    static final class IntentReceiver extends Stub {
        private boolean mFinished = false;
        private final PrintWriter mPw;

        IntentReceiver(PrintWriter pw) {
            this.mPw = pw;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
            String line = "Broadcast completed: result=" + resultCode;
            if (data != null) {
                line = line + ", data=\"" + data + "\"";
            }
            if (extras != null) {
                line = line + ", extras: " + extras;
            }
            this.mPw.println(line);
            this.mPw.flush();
            synchronized (this) {
                this.mFinished = true;
                notifyAll();
            }
        }

        public synchronized void waitForFinish() {
            while (!this.mFinished) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    static final class MyActivityController extends IActivityController.Stub {
        static final int RESULT_ANR_DIALOG = 0;
        static final int RESULT_ANR_KILL = 1;
        static final int RESULT_ANR_WAIT = 1;
        static final int RESULT_CRASH_DIALOG = 0;
        static final int RESULT_CRASH_KILL = 1;
        static final int RESULT_DEFAULT = 0;
        static final int RESULT_EARLY_ANR_CONTINUE = 0;
        static final int RESULT_EARLY_ANR_KILL = 1;
        static final int STATE_ANR = 3;
        static final int STATE_CRASHED = 1;
        static final int STATE_EARLY_ANR = 2;
        static final int STATE_NORMAL = 0;
        final String mGdbPort;
        Process mGdbProcess;
        Thread mGdbThread;
        boolean mGotGdbPrint;
        final InputStream mInput;
        final IActivityManager mInterface;
        final boolean mMonkey;
        final PrintWriter mPw;
        int mResult;
        int mState;

        MyActivityController(IActivityManager iam, PrintWriter pw, InputStream input, String gdbPort, boolean monkey) {
            this.mInterface = iam;
            this.mPw = pw;
            this.mInput = input;
            this.mGdbPort = gdbPort;
            this.mMonkey = monkey;
        }

        public boolean activityResuming(String pkg) {
            synchronized (this) {
                this.mPw.println("** Activity resuming: " + pkg);
                this.mPw.flush();
            }
            return true;
        }

        public boolean activityStarting(Intent intent, String pkg) {
            synchronized (this) {
                this.mPw.println("** Activity starting: " + pkg);
                this.mPw.flush();
            }
            return true;
        }

        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) {
            boolean z = true;
            synchronized (this) {
                this.mPw.println("** ERROR: PROCESS CRASHED");
                this.mPw.println("processName: " + processName);
                this.mPw.println("processPid: " + pid);
                this.mPw.println("shortMsg: " + shortMsg);
                this.mPw.println("longMsg: " + longMsg);
                this.mPw.println("timeMillis: " + timeMillis);
                this.mPw.println("stack:");
                this.mPw.print(stackTrace);
                this.mPw.println("#");
                this.mPw.flush();
                if (waitControllerLocked(pid, 1) == 1) {
                    z = false;
                }
            }
            return z;
        }

        public int appEarlyNotResponding(String processName, int pid, String annotation) {
            synchronized (this) {
                this.mPw.println("** ERROR: EARLY PROCESS NOT RESPONDING");
                this.mPw.println("processName: " + processName);
                this.mPw.println("processPid: " + pid);
                this.mPw.println("annotation: " + annotation);
                this.mPw.flush();
                if (waitControllerLocked(pid, 2) == 1) {
                    return -1;
                }
                return 0;
            }
        }

        public int appNotResponding(String processName, int pid, String processStats) {
            synchronized (this) {
                this.mPw.println("** ERROR: PROCESS NOT RESPONDING");
                this.mPw.println("processName: " + processName);
                this.mPw.println("processPid: " + pid);
                this.mPw.println("processStats:");
                this.mPw.print(processStats);
                this.mPw.println("#");
                this.mPw.flush();
                int result = waitControllerLocked(pid, 3);
                if (result == 1) {
                    return -1;
                } else if (result == 1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

        public int systemNotResponding(String message) {
            synchronized (this) {
                this.mPw.println("** ERROR: PROCESS NOT RESPONDING");
                this.mPw.println("message: " + message);
                this.mPw.println("#");
                this.mPw.println("Allowing system to die.");
                this.mPw.flush();
            }
            return -1;
        }

        void killGdbLocked() {
            this.mGotGdbPrint = false;
            if (this.mGdbProcess != null) {
                this.mPw.println("Stopping gdbserver");
                this.mPw.flush();
                this.mGdbProcess.destroy();
                this.mGdbProcess = null;
            }
            if (this.mGdbThread != null) {
                this.mGdbThread.interrupt();
                this.mGdbThread = null;
            }
        }

        int waitControllerLocked(int pid, int state) {
            if (this.mGdbPort != null) {
                killGdbLocked();
                try {
                    this.mPw.println("Starting gdbserver on port " + this.mGdbPort);
                    this.mPw.println("Do the following:");
                    this.mPw.println("  adb forward tcp:" + this.mGdbPort + " tcp:" + this.mGdbPort);
                    this.mPw.println("  gdbclient app_process :" + this.mGdbPort);
                    this.mPw.flush();
                    this.mGdbProcess = Runtime.getRuntime().exec(new String[]{"gdbserver", ":" + this.mGdbPort, "--attach", Integer.toString(pid)});
                    final InputStreamReader converter = new InputStreamReader(this.mGdbProcess.getInputStream());
                    this.mGdbThread = new Thread() {
                        /* JADX WARNING: Missing block: B:14:?, code:
            r3 = r2.readLine();
     */
                        /* JADX WARNING: Missing block: B:15:0x0025, code:
            if (r3 != null) goto L_0x002b;
     */
                        /* JADX WARNING: Missing block: B:16:0x0027, code:
            return;
     */
                        /* JADX WARNING: Missing block: B:21:?, code:
            r7.this$1.mPw.println("GDB: " + r3);
            r7.this$1.mPw.flush();
     */
                        /* JADX WARNING: Missing block: B:24:0x0051, code:
            return;
     */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void run() {
                            BufferedReader in = new BufferedReader(converter);
                            int count = 0;
                            while (true) {
                                synchronized (MyActivityController.this) {
                                    if (MyActivityController.this.mGdbThread == null) {
                                        return;
                                    } else if (count == 2) {
                                        MyActivityController.this.mGotGdbPrint = true;
                                        MyActivityController.this.notifyAll();
                                    }
                                }
                                count++;
                            }
                        }
                    };
                    this.mGdbThread.start();
                    try {
                        wait(500);
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e2) {
                    this.mPw.println("Failure starting gdbserver: " + e2);
                    this.mPw.flush();
                    killGdbLocked();
                }
            }
            this.mState = state;
            this.mPw.println("");
            printMessageForState();
            this.mPw.flush();
            while (this.mState != 0) {
                try {
                    wait();
                } catch (InterruptedException e3) {
                }
            }
            killGdbLocked();
            return this.mResult;
        }

        void resumeController(int result) {
            synchronized (this) {
                this.mState = 0;
                this.mResult = result;
                notifyAll();
            }
        }

        void printMessageForState() {
            switch (this.mState) {
                case 0:
                    this.mPw.println("Monitoring activity manager...  available commands:");
                    break;
                case 1:
                    this.mPw.println("Waiting after crash...  available commands:");
                    this.mPw.println("(c)ontinue: show crash dialog");
                    this.mPw.println("(k)ill: immediately kill app");
                    break;
                case 2:
                    this.mPw.println("Waiting after early ANR...  available commands:");
                    this.mPw.println("(c)ontinue: standard ANR processing");
                    this.mPw.println("(k)ill: immediately kill app");
                    break;
                case 3:
                    this.mPw.println("Waiting after ANR...  available commands:");
                    this.mPw.println("(c)ontinue: show ANR dialog");
                    this.mPw.println("(k)ill: immediately kill app");
                    this.mPw.println("(w)ait: wait some more");
                    break;
            }
            this.mPw.println("(q)uit: finish monitoring");
        }

        void run() throws RemoteException {
            try {
                printMessageForState();
                this.mPw.flush();
                this.mInterface.setActivityController(this, this.mMonkey);
                this.mState = 0;
                BufferedReader in = new BufferedReader(new InputStreamReader(this.mInput));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    boolean addNewline = true;
                    if (line.length() <= 0) {
                        addNewline = false;
                    } else if ("q".equals(line) || "quit".equals(line)) {
                        resumeController(0);
                    } else if (this.mState == 1) {
                        if ("c".equals(line) || "continue".equals(line)) {
                            resumeController(0);
                        } else if ("k".equals(line) || "kill".equals(line)) {
                            resumeController(1);
                        } else {
                            this.mPw.println("Invalid command: " + line);
                        }
                    } else if (this.mState == 3) {
                        if ("c".equals(line) || "continue".equals(line)) {
                            resumeController(0);
                        } else if ("k".equals(line) || "kill".equals(line)) {
                            resumeController(1);
                        } else if ("w".equals(line) || "wait".equals(line)) {
                            resumeController(1);
                        } else {
                            this.mPw.println("Invalid command: " + line);
                        }
                    } else if (this.mState != 2) {
                        this.mPw.println("Invalid command: " + line);
                    } else if ("c".equals(line) || "continue".equals(line)) {
                        resumeController(0);
                    } else if ("k".equals(line) || "kill".equals(line)) {
                        resumeController(1);
                    } else {
                        this.mPw.println("Invalid command: " + line);
                    }
                    synchronized (this) {
                        if (addNewline) {
                            this.mPw.println("");
                        }
                        printMessageForState();
                        this.mPw.flush();
                    }
                }
                resumeController(0);
                this.mInterface.setActivityController(null, this.mMonkey);
            } catch (IOException e) {
                e.printStackTrace(this.mPw);
                this.mPw.flush();
                this.mInterface.setActivityController(null, this.mMonkey);
            } catch (Throwable th) {
                this.mInterface.setActivityController(null, this.mMonkey);
            }
        }
    }

    static final class StopUserCallback extends IStopUserCallback.Stub {
        private boolean mFinished = false;

        StopUserCallback() {
        }

        public synchronized void waitForFinish() {
            while (!this.mFinished) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        public synchronized void userStopped(int userId) {
            this.mFinished = true;
            notifyAll();
        }

        public synchronized void userStopAborted(int userId) {
            this.mFinished = true;
            notifyAll();
        }
    }

    ActivityManagerShellCommand(ActivityManagerService service, boolean dumping) {
        this.mInterface = service;
        this.mInternal = service;
        this.mPm = AppGlobals.getPackageManager();
        this.mDumping = dumping;
    }

    public int onCommand(String cmd) {
        if (cmd == null) {
            return handleDefaultCommands(cmd);
        }
        PrintWriter pw = getOutPrintWriter();
        try {
            if (cmd.equals("start") || cmd.equals("start-activity")) {
                return runStartActivity(pw);
            }
            if (cmd.equals("startservice") || cmd.equals("start-service")) {
                return runStartService(pw, false);
            }
            if (cmd.equals("startforegroundservice") || cmd.equals("startfgservice") || cmd.equals("start-foreground-service") || cmd.equals("start-fg-service")) {
                return runStartService(pw, true);
            }
            if (cmd.equals("stopservice") || cmd.equals("stop-service")) {
                return runStopService(pw);
            }
            if (cmd.equals("broadcast")) {
                return runSendBroadcast(pw);
            }
            if (cmd.equals("instrument")) {
                getOutPrintWriter().println("Error: must be invoked through 'am instrument'.");
                return -1;
            } else if (cmd.equals("trace-ipc")) {
                return runTraceIpc(pw);
            } else {
                if (cmd.equals("profile")) {
                    return runProfile(pw);
                }
                if (cmd.equals("dumpheap")) {
                    return runDumpHeap(pw);
                }
                if (cmd.equals("set-debug-app")) {
                    return runSetDebugApp(pw);
                }
                if (cmd.equals("clear-debug-app")) {
                    return runClearDebugApp(pw);
                }
                if (cmd.equals("set-watch-heap")) {
                    return runSetWatchHeap(pw);
                }
                if (cmd.equals("clear-watch-heap")) {
                    return runClearWatchHeap(pw);
                }
                if (cmd.equals("bug-report")) {
                    return runBugReport(pw);
                }
                if (cmd.equals("force-stop")) {
                    return runForceStop(pw);
                }
                if (cmd.equals("crash")) {
                    return runCrash(pw);
                }
                if (cmd.equals("kill")) {
                    return runKill(pw);
                }
                if (cmd.equals("kill-all")) {
                    return runKillAll(pw);
                }
                if (cmd.equals("make-uid-idle")) {
                    return runMakeIdle(pw);
                }
                if (cmd.equals("monitor")) {
                    return runMonitor(pw);
                }
                if (cmd.equals("hang")) {
                    return runHang(pw);
                }
                if (cmd.equals("restart")) {
                    return runRestart(pw);
                }
                if (cmd.equals("idle-maintenance")) {
                    return runIdleMaintenance(pw);
                }
                if (cmd.equals("screen-compat")) {
                    return runScreenCompat(pw);
                }
                if (cmd.equals("package-importance")) {
                    return runPackageImportance(pw);
                }
                if (cmd.equals("to-uri")) {
                    return runToUri(pw, 0);
                }
                if (cmd.equals("to-intent-uri")) {
                    return runToUri(pw, 1);
                }
                if (cmd.equals("to-app-uri")) {
                    return runToUri(pw, 2);
                }
                if (cmd.equals("switch-user")) {
                    return runSwitchUser(pw);
                }
                if (cmd.equals("get-current-user")) {
                    return runGetCurrentUser(pw);
                }
                if (cmd.equals("start-user")) {
                    return runStartUser(pw);
                }
                if (cmd.equals("unlock-user")) {
                    return runUnlockUser(pw);
                }
                if (cmd.equals("stop-user")) {
                    return runStopUser(pw);
                }
                if (cmd.equals("is-user-stopped")) {
                    return runIsUserStopped(pw);
                }
                if (cmd.equals("get-started-user-state")) {
                    return runGetStartedUserState(pw);
                }
                if (cmd.equals("track-associations")) {
                    return runTrackAssociations(pw);
                }
                if (cmd.equals("untrack-associations")) {
                    return runUntrackAssociations(pw);
                }
                if (cmd.equals("get-uid-state")) {
                    return getUidState(pw);
                }
                if (cmd.equals("get-config")) {
                    return runGetConfig(pw);
                }
                if (cmd.equals("suppress-resize-config-changes")) {
                    return runSuppressResizeConfigChanges(pw);
                }
                if (cmd.equals("set-inactive")) {
                    return runSetInactive(pw);
                }
                if (cmd.equals("get-inactive")) {
                    return runGetInactive(pw);
                }
                if (cmd.equals("send-trim-memory")) {
                    return runSendTrimMemory(pw);
                }
                if (cmd.equals("display")) {
                    return runDisplay(pw);
                }
                if (cmd.equals("stack")) {
                    return runStack(pw);
                }
                if (cmd.equals("task")) {
                    return runTask(pw);
                }
                if (cmd.equals("write")) {
                    return runWrite(pw);
                }
                if (cmd.equals("attach-agent")) {
                    return runAttachAgent(pw);
                }
                if (cmd.equals("supports-multiwindow")) {
                    return runSupportsMultiwindow(pw);
                }
                if (cmd.equals("supports-split-screen-multi-window")) {
                    return runSupportsSplitScreenMultiwindow(pw);
                }
                if (cmd.equals("update-appinfo")) {
                    return runUpdateApplicationInfo(pw);
                }
                if (cmd.equals("no-home-screen")) {
                    return runNoHomeScreen(pw);
                }
                if (cmd.equals("wait-for-broadcast-idle")) {
                    return runWaitForBroadcastIdle(pw);
                }
                return handleDefaultCommands(cmd);
            }
        } catch (RemoteException e) {
            pw.println("Remote exception: " + e);
            return -1;
        }
    }

    private Intent makeIntent(int defUser) throws URISyntaxException {
        this.mStartFlags = 0;
        this.mWaitOption = false;
        this.mStopOption = false;
        this.mRepeat = 0;
        this.mProfileFile = null;
        this.mSamplingInterval = 0;
        this.mAutoStop = false;
        this.mStreaming = false;
        this.mUserId = defUser;
        this.mDisplayId = -1;
        this.mStackId = -1;
        this.mTaskId = -1;
        this.mIsTaskOverlay = false;
        return Intent.parseCommandArgs(this, new CommandOptionHandler() {
            public boolean handleOption(String opt, ShellCommand cmd) {
                ActivityManagerShellCommand activityManagerShellCommand;
                if (opt.equals("-D")) {
                    activityManagerShellCommand = ActivityManagerShellCommand.this;
                    activityManagerShellCommand.mStartFlags = activityManagerShellCommand.mStartFlags | 2;
                } else if (opt.equals("-N")) {
                    activityManagerShellCommand = ActivityManagerShellCommand.this;
                    activityManagerShellCommand.mStartFlags = activityManagerShellCommand.mStartFlags | 8;
                } else if (opt.equals("-W")) {
                    ActivityManagerShellCommand.this.mWaitOption = true;
                } else if (opt.equals("-P")) {
                    ActivityManagerShellCommand.this.mProfileFile = ActivityManagerShellCommand.this.getNextArgRequired();
                    ActivityManagerShellCommand.this.mAutoStop = true;
                } else if (opt.equals("--start-profiler")) {
                    ActivityManagerShellCommand.this.mProfileFile = ActivityManagerShellCommand.this.getNextArgRequired();
                    ActivityManagerShellCommand.this.mAutoStop = false;
                } else if (opt.equals("--sampling")) {
                    ActivityManagerShellCommand.this.mSamplingInterval = Integer.parseInt(ActivityManagerShellCommand.this.getNextArgRequired());
                } else if (opt.equals("--streaming")) {
                    ActivityManagerShellCommand.this.mStreaming = true;
                } else if (opt.equals("-R")) {
                    ActivityManagerShellCommand.this.mRepeat = Integer.parseInt(ActivityManagerShellCommand.this.getNextArgRequired());
                } else if (opt.equals("-S")) {
                    ActivityManagerShellCommand.this.mStopOption = true;
                } else if (opt.equals("--track-allocation")) {
                    activityManagerShellCommand = ActivityManagerShellCommand.this;
                    activityManagerShellCommand.mStartFlags = activityManagerShellCommand.mStartFlags | 4;
                } else if (opt.equals("--user")) {
                    ActivityManagerShellCommand.this.mUserId = UserHandle.parseUserArg(ActivityManagerShellCommand.this.getNextArgRequired());
                } else if (opt.equals("--receiver-permission")) {
                    ActivityManagerShellCommand.this.mReceiverPermission = ActivityManagerShellCommand.this.getNextArgRequired();
                } else if (opt.equals("--display")) {
                    ActivityManagerShellCommand.this.mDisplayId = Integer.parseInt(ActivityManagerShellCommand.this.getNextArgRequired());
                } else if (opt.equals("--stack")) {
                    ActivityManagerShellCommand.this.mStackId = Integer.parseInt(ActivityManagerShellCommand.this.getNextArgRequired());
                } else if (opt.equals("--task")) {
                    ActivityManagerShellCommand.this.mTaskId = Integer.parseInt(ActivityManagerShellCommand.this.getNextArgRequired());
                } else if (!opt.equals("--task-overlay")) {
                    return false;
                } else {
                    ActivityManagerShellCommand.this.mIsTaskOverlay = true;
                }
                return true;
            }
        });
    }

    int runStartActivity(PrintWriter pw) throws RemoteException {
        try {
            Intent intent = makeIntent(-2);
            if (this.mUserId == -1) {
                getErrPrintWriter().println("Error: Can't start service with user 'all'");
                return 1;
            }
            String mimeType = intent.getType();
            if (mimeType == null && intent.getData() != null && "content".equals(intent.getData().getScheme())) {
                mimeType = this.mInterface.getProviderMimeType(intent.getData(), this.mUserId);
            }
            do {
                int res;
                if (this.mStopOption) {
                    String packageName;
                    if (intent.getComponent() != null) {
                        packageName = intent.getComponent().getPackageName();
                    } else {
                        List<ResolveInfo> activities = this.mPm.queryIntentActivities(intent, mimeType, 0, this.mUserId).getList();
                        if (activities == null || activities.size() <= 0) {
                            getErrPrintWriter().println("Error: Intent does not match any activities: " + intent);
                            return 1;
                        } else if (activities.size() > 1) {
                            getErrPrintWriter().println("Error: Intent matches multiple activities; can't stop: " + intent);
                            return 1;
                        } else {
                            packageName = ((ResolveInfo) activities.get(0)).activityInfo.packageName;
                        }
                    }
                    pw.println("Stopping: " + packageName);
                    pw.flush();
                    this.mInterface.forceStopPackage(packageName, this.mUserId);
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                    }
                }
                ProfilerInfo profilerInfo = null;
                if (this.mProfileFile != null) {
                    ParcelFileDescriptor fd = openOutputFileForSystem(this.mProfileFile);
                    if (fd == null) {
                        return 1;
                    }
                    profilerInfo = new ProfilerInfo(this.mProfileFile, fd, this.mSamplingInterval, this.mAutoStop, this.mStreaming);
                }
                pw.println("Starting: " + intent);
                pw.flush();
                intent.addFlags(268435456);
                WaitResult result = null;
                long startTime = SystemClock.uptimeMillis();
                ActivityOptions options = null;
                if (this.mDisplayId != -1) {
                    options = ActivityOptions.makeBasic();
                    options.setLaunchDisplayId(this.mDisplayId);
                }
                if (this.mStackId != -1) {
                    options = ActivityOptions.makeBasic();
                    options.setLaunchStackId(this.mStackId);
                }
                if (this.mTaskId != -1) {
                    options = ActivityOptions.makeBasic();
                    options.setLaunchTaskId(this.mTaskId);
                    if (this.mIsTaskOverlay) {
                        options.setTaskOverlay(true, true);
                    }
                }
                if (this.mWaitOption) {
                    result = this.mInterface.startActivityAndWait(null, null, intent, mimeType, null, null, 0, this.mStartFlags, profilerInfo, options != null ? options.toBundle() : null, this.mUserId);
                    res = result.result;
                } else {
                    res = this.mInterface.startActivityAsUser(null, null, intent, mimeType, null, null, 0, this.mStartFlags, profilerInfo, options != null ? options.toBundle() : null, this.mUserId);
                }
                long endTime = SystemClock.uptimeMillis();
                PrintWriter out = this.mWaitOption ? pw : getErrPrintWriter();
                boolean launched = false;
                switch (res) {
                    case -98:
                        out.println("Error: Not allowed to start background user activity that shouldn't be displayed for all users.");
                        break;
                    case -97:
                        out.println("Error: Activity not started, voice control not allowed for: " + intent);
                        break;
                    case -94:
                        out.println("Error: Activity not started, you do not have permission to access it.");
                        break;
                    case -93:
                        out.println("Error: Activity not started, you requested to both forward and receive its result");
                        break;
                    case -92:
                        out.println(NO_CLASS_ERROR_CODE);
                        out.println("Error: Activity class " + intent.getComponent().toShortString() + " does not exist.");
                        break;
                    case -91:
                        out.println("Error: Activity not started, unable to resolve " + intent.toString());
                        break;
                    case 0:
                        launched = true;
                        break;
                    case 1:
                        launched = true;
                        out.println("Warning: Activity not started because intent should be handled by the caller");
                        break;
                    case 2:
                        launched = true;
                        out.println("Warning: Activity not started, its current task has been brought to the front");
                        break;
                    case 3:
                        launched = true;
                        out.println("Warning: Activity not started, intent has been delivered to currently running top-most instance.");
                        break;
                    case 100:
                        launched = true;
                        out.println("Warning: Activity not started because the  current activity is being kept for the user.");
                        break;
                    default:
                        out.println("Error: Activity not started, unknown error code " + res);
                        break;
                }
                out.flush();
                if (this.mWaitOption && launched) {
                    if (result == null) {
                        result = new WaitResult();
                        result.who = intent.getComponent();
                    }
                    pw.println("Status: " + (result.timeout ? "timeout" : "ok"));
                    if (result.who != null) {
                        pw.println("Activity: " + result.who.flattenToShortString());
                    }
                    if (result.thisTime >= 0) {
                        pw.println("ThisTime: " + result.thisTime);
                    }
                    if (result.totalTime >= 0) {
                        pw.println("TotalTime: " + result.totalTime);
                    }
                    pw.println("WaitTime: " + (endTime - startTime));
                    pw.println("Complete");
                    pw.flush();
                }
                this.mRepeat--;
                if (this.mRepeat > 0) {
                    this.mInterface.unhandledBack();
                }
            } while (this.mRepeat > 0);
            return 0;
        } catch (Throwable e2) {
            throw new RuntimeException(e2.getMessage(), e2);
        }
    }

    int runStartService(PrintWriter pw, boolean asForeground) throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        try {
            Intent intent = makeIntent(-2);
            if (this.mUserId == -1) {
                err.println("Error: Can't start activity with user 'all'");
                return -1;
            }
            pw.println("Starting service: " + intent);
            pw.flush();
            ComponentName cn = this.mInterface.startService(null, intent, intent.getType(), asForeground, SHELL_PACKAGE_NAME, this.mUserId);
            if (cn == null) {
                err.println("Error: Not found; no service started.");
                return -1;
            } else if (cn.getPackageName().equals("!")) {
                err.println("Error: Requires permission " + cn.getClassName());
                return -1;
            } else if (cn.getPackageName().equals("!!")) {
                err.println("Error: " + cn.getClassName());
                return -1;
            } else if (!cn.getPackageName().equals("?")) {
                return 0;
            } else {
                err.println("Error: " + cn.getClassName());
                return -1;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    int runStopService(PrintWriter pw) throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        try {
            Intent intent = makeIntent(-2);
            if (this.mUserId == -1) {
                err.println("Error: Can't stop activity with user 'all'");
                return -1;
            }
            pw.println("Stopping service: " + intent);
            pw.flush();
            int result = this.mInterface.stopService(null, intent, intent.getType(), this.mUserId);
            if (result == 0) {
                err.println("Service not stopped: was not running.");
                return -1;
            } else if (result == 1) {
                err.println("Service stopped");
                return -1;
            } else if (result != -1) {
                return 0;
            } else {
                err.println("Error stopping service");
                return -1;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    int runSendBroadcast(PrintWriter pw) throws RemoteException {
        try {
            Intent intent = makeIntent(-2);
            intent.addFlags(DumpState.DUMP_CHANGES);
            IntentReceiver receiver = new IntentReceiver(pw);
            String[] requiredPermissions = this.mReceiverPermission == null ? null : new String[]{this.mReceiverPermission};
            pw.println("Broadcasting: " + intent);
            pw.flush();
            this.mInterface.broadcastIntent(null, intent, null, receiver, 0, null, null, requiredPermissions, -1, null, true, false, this.mUserId);
            receiver.waitForFinish();
            return 0;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    int runTraceIpc(PrintWriter pw) throws RemoteException {
        String op = getNextArgRequired();
        if (op.equals("start")) {
            return runTraceIpcStart(pw);
        }
        if (op.equals("stop")) {
            return runTraceIpcStop(pw);
        }
        getErrPrintWriter().println("Error: unknown trace ipc command '" + op + "'");
        return -1;
    }

    int runTraceIpcStart(PrintWriter pw) throws RemoteException {
        pw.println("Starting IPC tracing.");
        pw.flush();
        this.mInterface.startBinderTracking();
        return 0;
    }

    int runTraceIpcStop(PrintWriter pw) throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        String filename = null;
        while (true) {
            String opt = getNextOption();
            if (opt != null) {
                if (opt.equals("--dump-file")) {
                    filename = getNextArgRequired();
                } else {
                    err.println("Error: Unknown option: " + opt);
                    return -1;
                }
            } else if (filename == null) {
                err.println("Error: Specify filename to dump logs to.");
                return -1;
            } else {
                new File(filename).delete();
                ParcelFileDescriptor fd = openOutputFileForSystem(filename);
                if (fd == null) {
                    return -1;
                }
                if (this.mInterface.stopBinderTrackingAndDump(fd)) {
                    pw.println("Stopped IPC tracing. Dumping logs to: " + filename);
                    return 0;
                }
                err.println("STOP TRACE FAILED.");
                return -1;
            }
        }
    }

    static void removeWallOption() {
        String props = SystemProperties.get("dalvik.vm.extra-opts");
        if (props != null && props.contains("-Xprofile:wallclock")) {
            SystemProperties.set("dalvik.vm.extra-opts", props.replace("-Xprofile:wallclock", "").trim());
        }
    }

    private int runProfile(PrintWriter pw) throws RemoteException {
        String process;
        PrintWriter err = getErrPrintWriter();
        boolean start = false;
        boolean wall = false;
        int userId = -2;
        this.mSamplingInterval = 0;
        this.mStreaming = false;
        String cmd = getNextArgRequired();
        String opt;
        if ("start".equals(cmd)) {
            start = true;
            while (true) {
                opt = getNextOption();
                if (opt == null) {
                    process = getNextArgRequired();
                    break;
                } else if (opt.equals("--user")) {
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                } else if (opt.equals("--wall")) {
                    wall = true;
                } else if (opt.equals("--streaming")) {
                    this.mStreaming = true;
                } else if (opt.equals("--sampling")) {
                    this.mSamplingInterval = Integer.parseInt(getNextArgRequired());
                } else {
                    err.println("Error: Unknown option: " + opt);
                    return -1;
                }
            }
        } else if ("stop".equals(cmd)) {
            while (true) {
                opt = getNextOption();
                if (opt == null) {
                    process = getNextArg();
                    break;
                } else if (opt.equals("--user")) {
                    userId = UserHandle.parseUserArg(getNextArgRequired());
                } else {
                    err.println("Error: Unknown option: " + opt);
                    return -1;
                }
            }
        } else {
            process = cmd;
            cmd = getNextArgRequired();
            if ("start".equals(cmd)) {
                start = true;
            } else if (!"stop".equals(cmd)) {
                throw new IllegalArgumentException("Profile command " + process + " not valid");
            }
        }
        if (userId == -1) {
            err.println("Error: Can't profile with user 'all'");
            return -1;
        }
        ProfilerInfo profilerInfo = null;
        if (start) {
            String profileFile = getNextArgRequired();
            ParcelFileDescriptor fd = openOutputFileForSystem(profileFile);
            if (fd == null) {
                return -1;
            }
            profilerInfo = new ProfilerInfo(profileFile, fd, this.mSamplingInterval, false, this.mStreaming);
        }
        if (wall) {
            String props = SystemProperties.get("dalvik.vm.extra-opts");
            if (props == null || (props.contains("-Xprofile:wallclock") ^ 1) != 0) {
                props = props + " -Xprofile:wallclock";
            }
        }
        if (this.mInterface.profileControl(process, userId, start, profilerInfo, 0)) {
            return 0;
        }
        err.println("PROFILE FAILED on process " + process);
        return -1;
    }

    int runDumpHeap(PrintWriter pw) throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        boolean managed = true;
        int userId = -2;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                String process = getNextArgRequired();
                String heapFile = getNextArgRequired();
                new File(heapFile).delete();
                ParcelFileDescriptor fd = openOutputFileForSystem(heapFile);
                if (fd == null) {
                    return -1;
                }
                if (this.mInterface.dumpHeap(process, userId, managed, heapFile, fd)) {
                    return 0;
                }
                err.println("HEAP DUMP FAILED on process " + process);
                return -1;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
                if (userId == -1) {
                    err.println("Error: Can't dump heap with user 'all'");
                    return -1;
                }
            } else if (opt.equals("-n")) {
                managed = false;
            } else {
                err.println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runSetDebugApp(PrintWriter pw) throws RemoteException {
        boolean wait = false;
        boolean persistent = false;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.setDebugApp(getNextArgRequired(), wait, persistent);
                return 0;
            } else if (opt.equals("-w")) {
                wait = true;
            } else if (opt.equals("--persistent")) {
                persistent = true;
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runClearDebugApp(PrintWriter pw) throws RemoteException {
        this.mInterface.setDebugApp(null, false, true);
        return 0;
    }

    int runSetWatchHeap(PrintWriter pw) throws RemoteException {
        this.mInterface.setDumpHeapDebugLimit(getNextArgRequired(), 0, Long.parseLong(getNextArgRequired()), null);
        return 0;
    }

    int runClearWatchHeap(PrintWriter pw) throws RemoteException {
        this.mInterface.setDumpHeapDebugLimit(getNextArgRequired(), 0, -1, null);
        return 0;
    }

    int runBugReport(PrintWriter pw) throws RemoteException {
        int bugreportType = 0;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.requestBugReport(bugreportType);
                pw.println("Your lovely bug report is being created; please be patient.");
                return 0;
            } else if (opt.equals("--progress")) {
                bugreportType = 1;
            } else if (opt.equals("--telephony")) {
                bugreportType = 4;
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runForceStop(PrintWriter pw) throws RemoteException {
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.forceStopPackage(getNextArgRequired(), userId);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runCrash(PrintWriter pw) throws RemoteException {
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                int pid = -1;
                String packageName = null;
                String arg = getNextArgRequired();
                try {
                    pid = Integer.parseInt(arg);
                } catch (NumberFormatException e) {
                    packageName = arg;
                }
                this.mInterface.crashApplication(-1, pid, packageName, userId, "shell-induced crash");
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runKill(PrintWriter pw) throws RemoteException {
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.killBackgroundProcesses(getNextArgRequired(), userId);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runKillAll(PrintWriter pw) throws RemoteException {
        this.mInterface.killAllBackgroundProcesses();
        return 0;
    }

    int runMakeIdle(PrintWriter pw) throws RemoteException {
        int userId = -1;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                this.mInterface.makePackageIdle(getNextArgRequired(), userId);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runMonitor(PrintWriter pw) throws RemoteException {
        String gdbPort = null;
        boolean monkey = false;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                new MyActivityController(this.mInterface, pw, getRawInputStream(), gdbPort, monkey).run();
                return 0;
            } else if (opt.equals("--gdb")) {
                gdbPort = getNextArgRequired();
            } else if (opt.equals("-m")) {
                monkey = true;
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runHang(PrintWriter pw) throws RemoteException {
        boolean allowRestart = false;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                pw.println("Hanging the system...");
                pw.flush();
                this.mInterface.hang(new Binder(), allowRestart);
                return 0;
            } else if (opt.equals("--allow-restart")) {
                allowRestart = true;
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runRestart(PrintWriter pw) throws RemoteException {
        String opt = getNextOption();
        if (opt != null) {
            getErrPrintWriter().println("Error: Unknown option: " + opt);
            return -1;
        }
        pw.println("Restart the system...");
        pw.flush();
        this.mInterface.restart();
        return 0;
    }

    int runIdleMaintenance(PrintWriter pw) throws RemoteException {
        String opt = getNextOption();
        if (opt != null) {
            getErrPrintWriter().println("Error: Unknown option: " + opt);
            return -1;
        }
        pw.println("Performing idle maintenance...");
        this.mInterface.sendIdleJobTrigger();
        return 0;
    }

    int runScreenCompat(PrintWriter pw) throws RemoteException {
        boolean enabled;
        String mode = getNextArgRequired();
        if ("on".equals(mode)) {
            enabled = true;
        } else if ("off".equals(mode)) {
            enabled = false;
        } else {
            getErrPrintWriter().println("Error: enabled mode must be 'on' or 'off' at " + mode);
            return -1;
        }
        String packageName = getNextArgRequired();
        do {
            try {
                int i;
                IActivityManager iActivityManager = this.mInterface;
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                iActivityManager.setPackageScreenCompatMode(packageName, i);
            } catch (RemoteException e) {
            }
            packageName = getNextArg();
        } while (packageName != null);
        return 0;
    }

    int runPackageImportance(PrintWriter pw) throws RemoteException {
        pw.println(RunningAppProcessInfo.procStateToImportance(this.mInterface.getPackageProcessState(getNextArgRequired(), SHELL_PACKAGE_NAME)));
        return 0;
    }

    int runToUri(PrintWriter pw, int flags) throws RemoteException {
        try {
            pw.println(makeIntent(-2).toUri(flags));
            return 0;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    int runSwitchUser(PrintWriter pw) throws RemoteException {
        this.mInterface.switchUser(Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    int runGetCurrentUser(PrintWriter pw) throws RemoteException {
        pw.println(((UserInfo) Preconditions.checkNotNull(this.mInterface.getCurrentUser(), "Current user not set")).id);
        return 0;
    }

    int runStartUser(PrintWriter pw) throws RemoteException {
        if (this.mInterface.startUserInBackground(Integer.parseInt(getNextArgRequired()))) {
            pw.println("Success: user started");
        } else {
            getErrPrintWriter().println("Error: could not start user");
        }
        return 0;
    }

    private static byte[] argToBytes(String arg) {
        if (arg.equals("!")) {
            return null;
        }
        return HexDump.hexStringToByteArray(arg);
    }

    int runUnlockUser(PrintWriter pw) throws RemoteException {
        if (this.mInterface.unlockUser(Integer.parseInt(getNextArgRequired()), argToBytes(getNextArgRequired()), argToBytes(getNextArgRequired()), null)) {
            pw.println("Success: user unlocked");
        } else {
            getErrPrintWriter().println("Error: could not unlock user");
        }
        return 0;
    }

    int runStopUser(PrintWriter pw) throws RemoteException {
        boolean wait = false;
        boolean force = false;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                int user = Integer.parseInt(getNextArgRequired());
                Object callback = wait ? new StopUserCallback() : null;
                int res = this.mInterface.stopUser(user, force, callback);
                if (res != 0) {
                    String txt = "";
                    switch (res) {
                        case -4:
                            txt = " (Can't stop user " + user + " - one of its related users can't be stopped)";
                            break;
                        case -3:
                            txt = " (System user cannot be stopped)";
                            break;
                        case -2:
                            txt = " (Can't stop current user)";
                            break;
                        case -1:
                            txt = " (Unknown user " + user + ")";
                            break;
                    }
                    getErrPrintWriter().println("Switch failed: " + res + txt);
                    return -1;
                }
                if (callback != null) {
                    callback.waitForFinish();
                }
                return 0;
            } else if ("-w".equals(opt)) {
                wait = true;
            } else if ("-f".equals(opt)) {
                force = true;
            } else {
                getErrPrintWriter().println("Error: unknown option: " + opt);
                return -1;
            }
        }
    }

    int runIsUserStopped(PrintWriter pw) {
        pw.println(this.mInternal.isUserStopped(UserHandle.parseUserArg(getNextArgRequired())));
        return 0;
    }

    int runGetStartedUserState(PrintWriter pw) throws RemoteException {
        this.mInternal.enforceCallingPermission("android.permission.DUMP", "runGetStartedUserState()");
        int userId = Integer.parseInt(getNextArgRequired());
        try {
            pw.println(this.mInternal.getStartedUserState(userId));
        } catch (NullPointerException e) {
            pw.println("User is not started: " + userId);
        }
        return 0;
    }

    int runTrackAssociations(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        synchronized (this.mInternal) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mInternal.mTrackingAssociations) {
                    pw.println("Association tracking already enabled.");
                } else {
                    this.mInternal.mTrackingAssociations = true;
                    pw.println("Association tracking started.");
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return 0;
    }

    int runUntrackAssociations(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        synchronized (this.mInternal) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (this.mInternal.mTrackingAssociations) {
                    this.mInternal.mTrackingAssociations = false;
                    this.mInternal.mAssociations.clear();
                    pw.println("Association tracking stopped.");
                } else {
                    pw.println("Association tracking not running.");
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        return 0;
    }

    int getUidState(PrintWriter pw) throws RemoteException {
        this.mInternal.enforceCallingPermission("android.permission.DUMP", "getUidState()");
        int state = this.mInternal.getUidState(Integer.parseInt(getNextArgRequired()));
        pw.print(state);
        pw.print(" (");
        pw.printf(DebugUtils.valueToString(ActivityManager.class, "PROCESS_STATE_", state), new Object[0]);
        pw.println(")");
        return 0;
    }

    private List<Configuration> getRecentConfigurations(int days) {
        IUsageStatsManager usm = IUsageStatsManager.Stub.asInterface(ServiceManager.getService("usagestats"));
        long now = System.currentTimeMillis();
        try {
            ParceledListSlice<ConfigurationStats> configStatsSlice = usm.queryConfigurationStats(4, now - ((long) ((((days * 24) * 60) * 60) * 1000)), now, SHELL_PACKAGE_NAME);
            if (configStatsSlice == null) {
                return Collections.emptyList();
            }
            ArrayMap<Configuration, Integer> recentConfigs = new ArrayMap();
            List<ConfigurationStats> configStatsList = configStatsSlice.getList();
            int configStatsListSize = configStatsList.size();
            for (int i = 0; i < configStatsListSize; i++) {
                ConfigurationStats stats = (ConfigurationStats) configStatsList.get(i);
                int indexOfKey = recentConfigs.indexOfKey(stats.getConfiguration());
                if (indexOfKey < 0) {
                    recentConfigs.put(stats.getConfiguration(), Integer.valueOf(stats.getActivationCount()));
                } else {
                    recentConfigs.setValueAt(indexOfKey, Integer.valueOf(((Integer) recentConfigs.valueAt(indexOfKey)).intValue() + stats.getActivationCount()));
                }
            }
            final ArrayMap<Configuration, Integer> arrayMap = recentConfigs;
            Comparator<Configuration> comparator = new Comparator<Configuration>() {
                public int compare(Configuration a, Configuration b) {
                    return ((Integer) arrayMap.get(b)).compareTo((Integer) arrayMap.get(a));
                }
            };
            ArrayList<Configuration> configs = new ArrayList(recentConfigs.size());
            configs.addAll(recentConfigs.keySet());
            Collections.sort(configs, comparator);
            return configs;
        } catch (RemoteException e) {
            return Collections.emptyList();
        }
    }

    int runGetConfig(PrintWriter pw) throws RemoteException {
        int days = 14;
        String option = getNextOption();
        if (option != null) {
            if (option.equals("--days")) {
                days = Integer.parseInt(getNextArgRequired());
                if (days <= 0) {
                    throw new IllegalArgumentException("--days must be a positive integer");
                }
            }
            throw new IllegalArgumentException("unrecognized option " + option);
        }
        Configuration config = this.mInterface.getConfiguration();
        if (config == null) {
            getErrPrintWriter().println("Activity manager has no configuration");
            return -1;
        }
        pw.println("config: " + Configuration.resourceQualifierString(config));
        pw.println("abi: " + TextUtils.join(",", Build.SUPPORTED_ABIS));
        List<Configuration> recentConfigs = getRecentConfigurations(days);
        int recentConfigSize = recentConfigs.size();
        if (recentConfigSize > 0) {
            pw.println("recentConfigs:");
        }
        for (int i = 0; i < recentConfigSize; i++) {
            pw.println("  config: " + Configuration.resourceQualifierString((Configuration) recentConfigs.get(i)));
        }
        return 0;
    }

    int runSuppressResizeConfigChanges(PrintWriter pw) throws RemoteException {
        this.mInterface.suppressResizeConfigChanges(Boolean.valueOf(getNextArgRequired()).booleanValue());
        return 0;
    }

    int runSetInactive(PrintWriter pw) throws RemoteException {
        int userId = -2;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                IUsageStatsManager.Stub.asInterface(ServiceManager.getService("usagestats")).setAppInactive(getNextArgRequired(), Boolean.parseBoolean(getNextArgRequired()), userId);
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runGetInactive(PrintWriter pw) throws RemoteException {
        int userId = -2;
        while (true) {
            String opt = getNextOption();
            if (opt == null) {
                pw.println("Idle=" + IUsageStatsManager.Stub.asInterface(ServiceManager.getService("usagestats")).isAppInactive(getNextArgRequired(), userId));
                return 0;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        }
    }

    int runSendTrimMemory(PrintWriter pw) throws RemoteException {
        int userId = -2;
        do {
            String opt = getNextOption();
            if (opt == null) {
                int level;
                String proc = getNextArgRequired();
                String levelArg = getNextArgRequired();
                if (levelArg.equals("HIDDEN")) {
                    level = 20;
                } else if (levelArg.equals("RUNNING_MODERATE")) {
                    level = 5;
                } else if (levelArg.equals("BACKGROUND")) {
                    level = 40;
                } else if (levelArg.equals("RUNNING_LOW")) {
                    level = 10;
                } else if (levelArg.equals("MODERATE")) {
                    level = 60;
                } else if (levelArg.equals("RUNNING_CRITICAL")) {
                    level = 15;
                } else if (levelArg.equals("COMPLETE")) {
                    level = 80;
                } else {
                    getErrPrintWriter().println("Error: Unknown level option: " + levelArg);
                    return -1;
                }
                if (this.mInterface.setProcessMemoryTrimLevel(proc, userId, level)) {
                    return 0;
                }
                getErrPrintWriter().println("Unknown error: failed to set trim level");
                return -1;
            } else if (opt.equals("--user")) {
                userId = UserHandle.parseUserArg(getNextArgRequired());
            } else {
                getErrPrintWriter().println("Error: Unknown option: " + opt);
                return -1;
            }
        } while (userId != -1);
        getErrPrintWriter().println("Error: Can't use user 'all'");
        return -1;
    }

    int runDisplay(PrintWriter pw) throws RemoteException {
        String op = getNextArgRequired();
        if (op.equals("move-stack")) {
            return runDisplayMoveStack(pw);
        }
        getErrPrintWriter().println("Error: unknown command '" + op + "'");
        return -1;
    }

    int runStack(PrintWriter pw) throws RemoteException {
        String op = getNextArgRequired();
        if (op.equals("start")) {
            return runStackStart(pw);
        }
        if (op.equals("move-task")) {
            return runStackMoveTask(pw);
        }
        if (op.equals("resize")) {
            return runStackResize(pw);
        }
        if (op.equals("resize-animated")) {
            return runStackResizeAnimated(pw);
        }
        if (op.equals("resize-docked-stack")) {
            return runStackResizeDocked(pw);
        }
        if (op.equals("positiontask")) {
            return runStackPositionTask(pw);
        }
        if (op.equals("list")) {
            return runStackList(pw);
        }
        if (op.equals("info")) {
            return runStackInfo(pw);
        }
        if (op.equals("move-top-activity-to-pinned-stack")) {
            return runMoveTopActivityToPinnedStack(pw);
        }
        if (op.equals("size-docked-stack-test")) {
            return runStackSizeDockedStackTest(pw);
        }
        if (op.equals("remove")) {
            return runStackRemove(pw);
        }
        getErrPrintWriter().println("Error: unknown command '" + op + "'");
        return -1;
    }

    private Rect getBounds() {
        String leftStr = getNextArgRequired();
        int left = Integer.parseInt(leftStr);
        String topStr = getNextArgRequired();
        int top = Integer.parseInt(topStr);
        String rightStr = getNextArgRequired();
        int right = Integer.parseInt(rightStr);
        String bottomStr = getNextArgRequired();
        int bottom = Integer.parseInt(bottomStr);
        if (left < 0) {
            getErrPrintWriter().println("Error: bad left arg: " + leftStr);
            return null;
        } else if (top < 0) {
            getErrPrintWriter().println("Error: bad top arg: " + topStr);
            return null;
        } else if (right <= 0) {
            getErrPrintWriter().println("Error: bad right arg: " + rightStr);
            return null;
        } else if (bottom > 0) {
            return new Rect(left, top, right, bottom);
        } else {
            getErrPrintWriter().println("Error: bad bottom arg: " + bottomStr);
            return null;
        }
    }

    int runDisplayMoveStack(PrintWriter pw) throws RemoteException {
        this.mInterface.moveStackToDisplay(Integer.parseInt(getNextArgRequired()), Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    int runStackStart(PrintWriter pw) throws RemoteException {
        int displayId = Integer.parseInt(getNextArgRequired());
        try {
            Intent intent = makeIntent(-2);
            IActivityContainer container = this.mInterface.createStackOnDisplay(displayId);
            if (container != null) {
                container.startActivity(intent);
            }
            return 0;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    int runStackMoveTask(PrintWriter pw) throws RemoteException {
        boolean toTop;
        int taskId = Integer.parseInt(getNextArgRequired());
        int stackId = Integer.parseInt(getNextArgRequired());
        String toTopStr = getNextArgRequired();
        if ("true".equals(toTopStr)) {
            toTop = true;
        } else if ("false".equals(toTopStr)) {
            toTop = false;
        } else {
            getErrPrintWriter().println("Error: bad toTop arg: " + toTopStr);
            return -1;
        }
        this.mInterface.moveTaskToStack(taskId, stackId, toTop);
        return 0;
    }

    int runStackResize(PrintWriter pw) throws RemoteException {
        int stackId = Integer.parseInt(getNextArgRequired());
        Rect bounds = getBounds();
        if (bounds != null) {
            return resizeStack(stackId, bounds, 0);
        }
        getErrPrintWriter().println("Error: invalid input bounds");
        return -1;
    }

    int runStackResizeAnimated(PrintWriter pw) throws RemoteException {
        Rect bounds;
        int stackId = Integer.parseInt(getNextArgRequired());
        if ("null".equals(peekNextArg())) {
            bounds = null;
        } else {
            bounds = getBounds();
            if (bounds == null) {
                getErrPrintWriter().println("Error: invalid input bounds");
                return -1;
            }
        }
        return resizeStackUnchecked(stackId, bounds, 0, true);
    }

    int resizeStackUnchecked(int stackId, Rect bounds, int delayMs, boolean animate) throws RemoteException {
        try {
            this.mInterface.resizeStack(stackId, bounds, false, false, animate, -1);
            Thread.sleep((long) delayMs);
        } catch (InterruptedException e) {
        }
        return 0;
    }

    int runStackResizeDocked(PrintWriter pw) throws RemoteException {
        Rect bounds = getBounds();
        Rect taskBounds = getBounds();
        if (bounds == null || taskBounds == null) {
            getErrPrintWriter().println("Error: invalid input bounds");
            return -1;
        }
        this.mInterface.resizeDockedStack(bounds, taskBounds, null, null, null);
        return 0;
    }

    int resizeStack(int stackId, Rect bounds, int delayMs) throws RemoteException {
        if (bounds != null) {
            return resizeStackUnchecked(stackId, bounds, delayMs, false);
        }
        getErrPrintWriter().println("Error: invalid input bounds");
        return -1;
    }

    int runStackPositionTask(PrintWriter pw) throws RemoteException {
        this.mInterface.positionTaskInStack(Integer.parseInt(getNextArgRequired()), Integer.parseInt(getNextArgRequired()), Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    int runStackList(PrintWriter pw) throws RemoteException {
        for (StackInfo info : this.mInterface.getAllStackInfos()) {
            pw.println(info);
        }
        return 0;
    }

    int runStackInfo(PrintWriter pw) throws RemoteException {
        pw.println(this.mInterface.getStackInfo(Integer.parseInt(getNextArgRequired())));
        return 0;
    }

    int runStackRemove(PrintWriter pw) throws RemoteException {
        this.mInterface.removeStack(Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    int runMoveTopActivityToPinnedStack(PrintWriter pw) throws RemoteException {
        int stackId = Integer.parseInt(getNextArgRequired());
        Rect bounds = getBounds();
        if (bounds == null) {
            getErrPrintWriter().println("Error: invalid input bounds");
            return -1;
        } else if (this.mInterface.moveTopActivityToPinnedStack(stackId, bounds)) {
            return 0;
        } else {
            getErrPrintWriter().println("Didn't move top activity to pinned stack.");
            return -1;
        }
    }

    int runStackSizeDockedStackTest(PrintWriter pw) throws RemoteException {
        PrintWriter err = getErrPrintWriter();
        int stepSize = Integer.parseInt(getNextArgRequired());
        String side = getNextArgRequired();
        String delayStr = getNextArg();
        int delayMs = delayStr != null ? Integer.parseInt(delayStr) : 0;
        StackInfo info = this.mInterface.getStackInfo(3);
        if (info == null) {
            err.println("Docked stack doesn't exist");
            return -1;
        } else if (info.bounds == null) {
            err.println("Docked stack doesn't have a bounds");
            return -1;
        } else {
            int currentPoint;
            int res;
            Rect bounds = info.bounds;
            int changeSize = (!"l".equals(side) ? "r".equals(side) : true ? bounds.width() : bounds.height()) / 2;
            if (side.equals("l")) {
                currentPoint = bounds.left;
            } else if (side.equals("r")) {
                currentPoint = bounds.right;
            } else if (side.equals("t")) {
                currentPoint = bounds.top;
            } else if (side.equals("b")) {
                currentPoint = bounds.bottom;
            } else {
                err.println("Unknown growth side: " + side);
                return -1;
            }
            int startPoint = currentPoint;
            int minPoint = currentPoint - changeSize;
            int maxPoint = currentPoint + changeSize;
            pw.println("Shrinking docked stack side=" + side);
            pw.flush();
            while (currentPoint > minPoint) {
                currentPoint -= Math.min(stepSize, currentPoint - minPoint);
                setBoundsSide(bounds, side, currentPoint);
                res = resizeStack(3, bounds, delayMs);
                if (res < 0) {
                    return res;
                }
            }
            pw.println("Growing docked stack side=" + side);
            pw.flush();
            while (currentPoint < maxPoint) {
                currentPoint += Math.min(stepSize, maxPoint - currentPoint);
                setBoundsSide(bounds, side, currentPoint);
                res = resizeStack(3, bounds, delayMs);
                if (res < 0) {
                    return res;
                }
            }
            pw.println("Back to Original size side=" + side);
            pw.flush();
            while (currentPoint > startPoint) {
                currentPoint -= Math.min(stepSize, currentPoint - startPoint);
                setBoundsSide(bounds, side, currentPoint);
                res = resizeStack(3, bounds, delayMs);
                if (res < 0) {
                    return res;
                }
            }
            return 0;
        }
    }

    void setBoundsSide(Rect bounds, String side, int value) {
        if (side.equals("l")) {
            bounds.left = value;
        } else if (side.equals("r")) {
            bounds.right = value;
        } else if (side.equals("t")) {
            bounds.top = value;
        } else if (side.equals("b")) {
            bounds.bottom = value;
        } else {
            getErrPrintWriter().println("Unknown set side: " + side);
        }
    }

    int runTask(PrintWriter pw) throws RemoteException {
        String op = getNextArgRequired();
        if (op.equals("lock")) {
            return runTaskLock(pw);
        }
        if (op.equals("resizeable")) {
            return runTaskResizeable(pw);
        }
        if (op.equals("resize")) {
            return runTaskResize(pw);
        }
        if (op.equals("drag-task-test")) {
            return runTaskDragTaskTest(pw);
        }
        if (op.equals("size-task-test")) {
            return runTaskSizeTaskTest(pw);
        }
        if (op.equals("focus")) {
            return runTaskFocus(pw);
        }
        getErrPrintWriter().println("Error: unknown command '" + op + "'");
        return -1;
    }

    int runTaskLock(PrintWriter pw) throws RemoteException {
        String taskIdStr = getNextArgRequired();
        if (taskIdStr.equals("stop")) {
            this.mInterface.stopLockTaskMode();
        } else {
            this.mInterface.startSystemLockTaskMode(Integer.parseInt(taskIdStr));
        }
        pw.println("Activity manager is " + (this.mInterface.isInLockTaskMode() ? "" : "not ") + "in lockTaskMode");
        return 0;
    }

    int runTaskResizeable(PrintWriter pw) throws RemoteException {
        this.mInterface.setTaskResizeable(Integer.parseInt(getNextArgRequired()), Integer.parseInt(getNextArgRequired()));
        return 0;
    }

    int runTaskResize(PrintWriter pw) throws RemoteException {
        int taskId = Integer.parseInt(getNextArgRequired());
        Rect bounds = getBounds();
        if (bounds == null) {
            getErrPrintWriter().println("Error: invalid input bounds");
            return -1;
        }
        taskResize(taskId, bounds, 0, false);
        return 0;
    }

    void taskResize(int taskId, Rect bounds, int delay_ms, boolean pretendUserResize) throws RemoteException {
        this.mInterface.resizeTask(taskId, bounds, pretendUserResize ? 1 : 0);
        try {
            Thread.sleep((long) delay_ms);
        } catch (InterruptedException e) {
        }
    }

    int runTaskDragTaskTest(PrintWriter pw) throws RemoteException {
        int taskId = Integer.parseInt(getNextArgRequired());
        int stepSize = Integer.parseInt(getNextArgRequired());
        String delayStr = getNextArg();
        int delay_ms = delayStr != null ? Integer.parseInt(delayStr) : 0;
        StackInfo stackInfo = this.mInterface.getStackInfo(this.mInterface.getFocusedStackId());
        Rect taskBounds = this.mInterface.getTaskBounds(taskId);
        Rect stackBounds = stackInfo.bounds;
        int travelRight = stackBounds.width() - taskBounds.width();
        int travelLeft = -travelRight;
        int travelDown = stackBounds.height() - taskBounds.height();
        int travelUp = -travelDown;
        for (int passes = 0; passes < 2; passes++) {
            pw.println("Moving right...");
            pw.flush();
            travelRight = moveTask(taskId, taskBounds, stackBounds, stepSize, travelRight, true, true, delay_ms);
            pw.println("Still need to travel right by " + travelRight);
            pw.println("Moving down...");
            pw.flush();
            travelDown = moveTask(taskId, taskBounds, stackBounds, stepSize, travelDown, true, false, delay_ms);
            pw.println("Still need to travel down by " + travelDown);
            pw.println("Moving left...");
            pw.flush();
            travelLeft = moveTask(taskId, taskBounds, stackBounds, stepSize, travelLeft, false, true, delay_ms);
            pw.println("Still need to travel left by " + travelLeft);
            pw.println("Moving up...");
            pw.flush();
            travelUp = moveTask(taskId, taskBounds, stackBounds, stepSize, travelUp, false, false, delay_ms);
            pw.println("Still need to travel up by " + travelUp);
            taskBounds = this.mInterface.getTaskBounds(taskId);
        }
        return 0;
    }

    int moveTask(int taskId, Rect taskRect, Rect stackRect, int stepSize, int maxToTravel, boolean movingForward, boolean horizontal, int delay_ms) throws RemoteException {
        int maxMove;
        if (movingForward) {
            while (maxToTravel > 0 && ((horizontal && taskRect.right < stackRect.right) || (!horizontal && taskRect.bottom < stackRect.bottom))) {
                if (horizontal) {
                    maxMove = Math.min(stepSize, stackRect.right - taskRect.right);
                    maxToTravel -= maxMove;
                    taskRect.right += maxMove;
                    taskRect.left += maxMove;
                } else {
                    maxMove = Math.min(stepSize, stackRect.bottom - taskRect.bottom);
                    maxToTravel -= maxMove;
                    taskRect.top += maxMove;
                    taskRect.bottom += maxMove;
                }
                taskResize(taskId, taskRect, delay_ms, false);
            }
        } else {
            while (maxToTravel < 0 && ((horizontal && taskRect.left > stackRect.left) || (!horizontal && taskRect.top > stackRect.top))) {
                if (horizontal) {
                    maxMove = Math.min(stepSize, taskRect.left - stackRect.left);
                    maxToTravel -= maxMove;
                    taskRect.right -= maxMove;
                    taskRect.left -= maxMove;
                } else {
                    maxMove = Math.min(stepSize, taskRect.top - stackRect.top);
                    maxToTravel -= maxMove;
                    taskRect.top -= maxMove;
                    taskRect.bottom -= maxMove;
                }
                taskResize(taskId, taskRect, delay_ms, false);
            }
        }
        return maxToTravel;
    }

    int getStepSize(int current, int target, int inStepSize, boolean greaterThanTarget) {
        int stepSize = 0;
        if (greaterThanTarget && target < current) {
            current -= inStepSize;
            stepSize = inStepSize;
            if (target > current) {
                stepSize = inStepSize - (target - current);
            }
        }
        if (greaterThanTarget || target <= current) {
            return stepSize;
        }
        current += inStepSize;
        stepSize = inStepSize;
        if (target < current) {
            return inStepSize + (current - target);
        }
        return stepSize;
    }

    int runTaskSizeTaskTest(PrintWriter pw) throws RemoteException {
        int taskId = Integer.parseInt(getNextArgRequired());
        int stepSize = Integer.parseInt(getNextArgRequired());
        String delayStr = getNextArg();
        int delay_ms = delayStr != null ? Integer.parseInt(delayStr) : 0;
        StackInfo stackInfo = this.mInterface.getStackInfo(this.mInterface.getFocusedStackId());
        Rect initialTaskBounds = this.mInterface.getTaskBounds(taskId);
        Rect stackBounds = stackInfo.bounds;
        stackBounds.inset(10, 10);
        Rect currentTaskBounds = new Rect(initialTaskBounds);
        pw.println("Growing top-left");
        pw.flush();
        while (true) {
            currentTaskBounds.top -= getStepSize(currentTaskBounds.top, stackBounds.top, stepSize, true);
            currentTaskBounds.left -= getStepSize(currentTaskBounds.left, stackBounds.left, stepSize, true);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (stackBounds.top >= currentTaskBounds.top && stackBounds.left >= currentTaskBounds.left) {
                break;
            }
        }
        pw.println("Shrinking top-left");
        pw.flush();
        while (true) {
            currentTaskBounds.top += getStepSize(currentTaskBounds.top, initialTaskBounds.top, stepSize, false);
            currentTaskBounds.left += getStepSize(currentTaskBounds.left, initialTaskBounds.left, stepSize, false);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (initialTaskBounds.top <= currentTaskBounds.top && initialTaskBounds.left <= currentTaskBounds.left) {
                break;
            }
        }
        pw.println("Growing top-right");
        pw.flush();
        while (true) {
            currentTaskBounds.top -= getStepSize(currentTaskBounds.top, stackBounds.top, stepSize, true);
            currentTaskBounds.right += getStepSize(currentTaskBounds.right, stackBounds.right, stepSize, false);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (stackBounds.top >= currentTaskBounds.top && stackBounds.right <= currentTaskBounds.right) {
                break;
            }
        }
        pw.println("Shrinking top-right");
        pw.flush();
        while (true) {
            currentTaskBounds.top += getStepSize(currentTaskBounds.top, initialTaskBounds.top, stepSize, false);
            currentTaskBounds.right -= getStepSize(currentTaskBounds.right, initialTaskBounds.right, stepSize, true);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (initialTaskBounds.top <= currentTaskBounds.top && initialTaskBounds.right >= currentTaskBounds.right) {
                break;
            }
        }
        pw.println("Growing bottom-left");
        pw.flush();
        while (true) {
            currentTaskBounds.bottom += getStepSize(currentTaskBounds.bottom, stackBounds.bottom, stepSize, false);
            currentTaskBounds.left -= getStepSize(currentTaskBounds.left, stackBounds.left, stepSize, true);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (stackBounds.bottom <= currentTaskBounds.bottom && stackBounds.left >= currentTaskBounds.left) {
                break;
            }
        }
        pw.println("Shrinking bottom-left");
        pw.flush();
        while (true) {
            currentTaskBounds.bottom -= getStepSize(currentTaskBounds.bottom, initialTaskBounds.bottom, stepSize, true);
            currentTaskBounds.left += getStepSize(currentTaskBounds.left, initialTaskBounds.left, stepSize, false);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (initialTaskBounds.bottom >= currentTaskBounds.bottom && initialTaskBounds.left <= currentTaskBounds.left) {
                break;
            }
        }
        pw.println("Growing bottom-right");
        pw.flush();
        while (true) {
            currentTaskBounds.bottom += getStepSize(currentTaskBounds.bottom, stackBounds.bottom, stepSize, false);
            currentTaskBounds.right += getStepSize(currentTaskBounds.right, stackBounds.right, stepSize, false);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (stackBounds.bottom <= currentTaskBounds.bottom && stackBounds.right <= currentTaskBounds.right) {
                break;
            }
        }
        pw.println("Shrinking bottom-right");
        pw.flush();
        while (true) {
            currentTaskBounds.bottom -= getStepSize(currentTaskBounds.bottom, initialTaskBounds.bottom, stepSize, true);
            currentTaskBounds.right -= getStepSize(currentTaskBounds.right, initialTaskBounds.right, stepSize, true);
            taskResize(taskId, currentTaskBounds, delay_ms, true);
            if (initialTaskBounds.bottom >= currentTaskBounds.bottom && initialTaskBounds.right >= currentTaskBounds.right) {
                return 0;
            }
        }
    }

    int runTaskFocus(PrintWriter pw) throws RemoteException {
        int taskId = Integer.parseInt(getNextArgRequired());
        pw.println("Setting focus to task " + taskId);
        this.mInterface.setFocusedTask(taskId);
        return 0;
    }

    int runWrite(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "registerUidObserver()");
        this.mInternal.mRecentTasks.flush();
        pw.println("All tasks persisted.");
        return 0;
    }

    int runAttachAgent(PrintWriter pw) {
        this.mInternal.enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "attach-agent");
        String process = getNextArgRequired();
        String agent = getNextArgRequired();
        String opt = getNextArg();
        if (opt != null) {
            pw.println("Error: Unknown option: " + opt);
            return -1;
        }
        this.mInternal.attachAgent(process, agent);
        return 0;
    }

    int runSupportsMultiwindow(PrintWriter pw) throws RemoteException {
        if (getResources(pw) == null) {
            return -1;
        }
        pw.println(ActivityManager.supportsMultiWindow(this.mInternal.mContext));
        return 0;
    }

    int runSupportsSplitScreenMultiwindow(PrintWriter pw) throws RemoteException {
        if (getResources(pw) == null) {
            return -1;
        }
        pw.println(ActivityManager.supportsSplitScreenMultiWindow(this.mInternal.mContext));
        return 0;
    }

    int runUpdateApplicationInfo(PrintWriter pw) throws RemoteException {
        int userid = UserHandle.parseUserArg(getNextArgRequired());
        ArrayList<String> packages = new ArrayList();
        packages.add(getNextArgRequired());
        while (true) {
            String packageName = getNextArg();
            if (packageName != null) {
                packages.add(packageName);
            } else {
                this.mInternal.scheduleApplicationInfoChanged(packages, userid);
                pw.println("Packages updated with most recent ApplicationInfos.");
                return 0;
            }
        }
    }

    int runNoHomeScreen(PrintWriter pw) throws RemoteException {
        Resources res = getResources(pw);
        if (res == null) {
            return -1;
        }
        pw.println(res.getBoolean(17956984));
        return 0;
    }

    int runWaitForBroadcastIdle(PrintWriter pw) throws RemoteException {
        this.mInternal.waitForBroadcastIdle(pw);
        return 0;
    }

    private Resources getResources(PrintWriter pw) throws RemoteException {
        Configuration config = this.mInterface.getConfiguration();
        if (config == null) {
            pw.println("Error: Activity manager has no configuration");
            return null;
        }
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        return new Resources(AssetManager.getSystem(), metrics, config);
    }

    public void onHelp() {
        dumpHelp(getOutPrintWriter(), this.mDumping);
    }

    static void dumpHelp(PrintWriter pw, boolean dumping) {
        if (dumping) {
            pw.println("Activity manager dump options:");
            pw.println("  [-a] [-c] [-p PACKAGE] [-h] [WHAT] ...");
            pw.println("  WHAT may be one of:");
            pw.println("    a[ctivities]: activity stack state");
            pw.println("    r[recents]: recent activities state");
            pw.println("    b[roadcasts] [PACKAGE_NAME] [history [-s]]: broadcast state");
            pw.println("    broadcast-stats [PACKAGE_NAME]: aggregated broadcast statistics");
            pw.println("    i[ntents] [PACKAGE_NAME]: pending intent state");
            pw.println("    p[rocesses] [PACKAGE_NAME]: process state");
            pw.println("    o[om]: out of memory management");
            pw.println("    perm[issions]: URI permission grant state");
            pw.println("    prov[iders] [COMP_SPEC ...]: content provider state");
            pw.println("    provider [COMP_SPEC]: provider client-side state");
            pw.println("    s[ervices] [COMP_SPEC ...]: service state");
            pw.println("    as[sociations]: tracked app associations");
            pw.println("    settings: currently applied config settings");
            pw.println("    service [COMP_SPEC]: service client-side state");
            pw.println("    package [PACKAGE_NAME]: all state related to given package");
            pw.println("    all: dump all activities");
            pw.println("    top: dump the top activity");
            pw.println("  WHAT may also be a COMP_SPEC to dump activities.");
            pw.println("  COMP_SPEC may be a component name (com.foo/.myApp),");
            pw.println("    a partial substring in a component name, a");
            pw.println("    hex object identifier.");
            pw.println("  -a: include all available server state.");
            pw.println("  -c: include client state.");
            pw.println("  -p: limit output to given package.");
            pw.println("  --checkin: output checkin format, resetting data.");
            pw.println("  --C: output checkin format, not resetting data.");
            return;
        }
        pw.println("Activity manager (activity) commands:");
        pw.println("  help");
        pw.println("      Print this help text.");
        pw.println("  start-activity [-D] [-N] [-W] [-P <FILE>] [--start-profiler <FILE>]");
        pw.println("          [--sampling INTERVAL] [--streaming] [-R COUNT] [-S]");
        pw.println("          [--track-allocation] [--user <USER_ID> | current] <INTENT>");
        pw.println("      Start an Activity.  Options are:");
        pw.println("      -D: enable debugging");
        pw.println("      -N: enable native debugging");
        pw.println("      -W: wait for launch to complete");
        pw.println("      --start-profiler <FILE>: start profiler and send results to <FILE>");
        pw.println("      --sampling INTERVAL: use sample profiling with INTERVAL microseconds");
        pw.println("          between samples (use with --start-profiler)");
        pw.println("      --streaming: stream the profiling output to the specified file");
        pw.println("          (use with --start-profiler)");
        pw.println("      -P <FILE>: like above, but profiling stops when app goes idle");
        pw.println("      -R: repeat the activity launch <COUNT> times.  Prior to each repeat,");
        pw.println("          the top activity will be finished.");
        pw.println("      -S: force stop the target app before starting the activity");
        pw.println("      --track-allocation: enable tracking of object allocations");
        pw.println("      --user <USER_ID> | current: Specify which user to run as; if not");
        pw.println("          specified then run as the current user.");
        pw.println("      --stack <STACK_ID>: Specify into which stack should the activity be put.");
        pw.println("  start-service [--user <USER_ID> | current] <INTENT>");
        pw.println("      Start a Service.  Options are:");
        pw.println("      --user <USER_ID> | current: Specify which user to run as; if not");
        pw.println("          specified then run as the current user.");
        pw.println("  start-foreground-service [--user <USER_ID> | current] <INTENT>");
        pw.println("      Start a foreground Service.  Options are:");
        pw.println("      --user <USER_ID> | current: Specify which user to run as; if not");
        pw.println("          specified then run as the current user.");
        pw.println("  stop-service [--user <USER_ID> | current] <INTENT>");
        pw.println("      Stop a Service.  Options are:");
        pw.println("      --user <USER_ID> | current: Specify which user to run as; if not");
        pw.println("          specified then run as the current user.");
        pw.println("  broadcast [--user <USER_ID> | all | current] <INTENT>");
        pw.println("      Send a broadcast Intent.  Options are:");
        pw.println("      --user <USER_ID> | all | current: Specify which user to send to; if not");
        pw.println("          specified then send to all users.");
        pw.println("      --receiver-permission <PERMISSION>: Require receiver to hold permission.");
        pw.println("  instrument [-r] [-e <NAME> <VALUE>] [-p <FILE>] [-w]");
        pw.println("          [--user <USER_ID> | current]");
        pw.println("          [--no-window-animation] [--abi <ABI>] <COMPONENT>");
        pw.println("      Start an Instrumentation.  Typically this target <COMPONENT> is in the");
        pw.println("      form <TEST_PACKAGE>/<RUNNER_CLASS> or only <TEST_PACKAGE> if there");
        pw.println("      is only one instrumentation.  Options are:");
        pw.println("      -r: print raw results (otherwise decode REPORT_KEY_STREAMRESULT).  Use with");
        pw.println("          [-e perf true] to generate raw output for performance measurements.");
        pw.println("      -e <NAME> <VALUE>: set argument <NAME> to <VALUE>.  For test runners a");
        pw.println("          common form is [-e <testrunner_flag> <value>[,<value>...]].");
        pw.println("      -p <FILE>: write profiling data to <FILE>");
        pw.println("      -m: Write output as protobuf (machine readable)");
        pw.println("      -w: wait for instrumentation to finish before returning.  Required for");
        pw.println("          test runners.");
        pw.println("      --user <USER_ID> | current: Specify user instrumentation runs in;");
        pw.println("          current user if not specified.");
        pw.println("      --no-window-animation: turn off window animations while running.");
        pw.println("      --abi <ABI>: Launch the instrumented process with the selected ABI.");
        pw.println("          This assumes that the process supports the selected ABI.");
        pw.println("  trace-ipc [start|stop] [--dump-file <FILE>]");
        pw.println("      Trace IPC transactions.");
        pw.println("      start: start tracing IPC transactions.");
        pw.println("      stop: stop tracing IPC transactions and dump the results to file.");
        pw.println("      --dump-file <FILE>: Specify the file the trace should be dumped to.");
        pw.println("  profile [start|stop] [--user <USER_ID> current] [--sampling INTERVAL]");
        pw.println("          [--streaming] <PROCESS> <FILE>");
        pw.println("      Start and stop profiler on a process.  The given <PROCESS> argument");
        pw.println("        may be either a process name or pid.  Options are:");
        pw.println("      --user <USER_ID> | current: When supplying a process name,");
        pw.println("          specify user of process to profile; uses current user if not specified.");
        pw.println("      --sampling INTERVAL: use sample profiling with INTERVAL microseconds");
        pw.println("          between samples");
        pw.println("      --streaming: stream the profiling output to the specified file");
        pw.println("  dumpheap [--user <USER_ID> current] [-n] <PROCESS> <FILE>");
        pw.println("      Dump the heap of a process.  The given <PROCESS> argument may");
        pw.println("        be either a process name or pid.  Options are:");
        pw.println("      -n: dump native heap instead of managed heap");
        pw.println("      --user <USER_ID> | current: When supplying a process name,");
        pw.println("          specify user of process to dump; uses current user if not specified.");
        pw.println("  set-debug-app [-w] [--persistent] <PACKAGE>");
        pw.println("      Set application <PACKAGE> to debug.  Options are:");
        pw.println("      -w: wait for debugger when application starts");
        pw.println("      --persistent: retain this value");
        pw.println("  clear-debug-app");
        pw.println("      Clear the previously set-debug-app.");
        pw.println("  set-watch-heap <PROCESS> <MEM-LIMIT>");
        pw.println("      Start monitoring pss size of <PROCESS>, if it is at or");
        pw.println("      above <HEAP-LIMIT> then a heap dump is collected for the user to report.");
        pw.println("  clear-watch-heap");
        pw.println("      Clear the previously set-watch-heap.");
        pw.println("  bug-report [--progress | --telephony]");
        pw.println("      Request bug report generation; will launch a notification");
        pw.println("        when done to select where it should be delivered. Options are:");
        pw.println("     --progress: will launch a notification right away to show its progress.");
        pw.println("     --telephony: will dump only telephony sections.");
        pw.println("  force-stop [--user <USER_ID> | all | current] <PACKAGE>");
        pw.println("      Completely stop the given application package.");
        pw.println("  crash [--user <USER_ID>] <PACKAGE|PID>");
        pw.println("      Induce a VM crash in the specified package or process");
        pw.println("  kill [--user <USER_ID> | all | current] <PACKAGE>");
        pw.println("      Kill all processes associated with the given application.");
        pw.println("  kill-all");
        pw.println("      Kill all processes that are safe to kill (cached, etc).");
        pw.println("  make-uid-idle [--user <USER_ID> | all | current] <PACKAGE>");
        pw.println("      If the given application's uid is in the background and waiting to");
        pw.println("      become idle (not allowing background services), do that now.");
        pw.println("  monitor [--gdb <port>]");
        pw.println("      Start monitoring for crashes or ANRs.");
        pw.println("      --gdb: start gdbserv on the given port at crash/ANR");
        pw.println("  hang [--allow-restart]");
        pw.println("      Hang the system.");
        pw.println("      --allow-restart: allow watchdog to perform normal system restart");
        pw.println("  restart");
        pw.println("      Restart the user-space system.");
        pw.println("  idle-maintenance");
        pw.println("      Perform idle maintenance now.");
        pw.println("  screen-compat [on|off] <PACKAGE>");
        pw.println("      Control screen compatibility mode of <PACKAGE>.");
        pw.println("  package-importance <PACKAGE>");
        pw.println("      Print current importance of <PACKAGE>.");
        pw.println("  to-uri [INTENT]");
        pw.println("      Print the given Intent specification as a URI.");
        pw.println("  to-intent-uri [INTENT]");
        pw.println("      Print the given Intent specification as an intent: URI.");
        pw.println("  to-app-uri [INTENT]");
        pw.println("      Print the given Intent specification as an android-app: URI.");
        pw.println("  switch-user <USER_ID>");
        pw.println("      Switch to put USER_ID in the foreground, starting");
        pw.println("      execution of that user if it is currently stopped.");
        pw.println("  get-current-user");
        pw.println("      Returns id of the current foreground user.");
        pw.println("  start-user <USER_ID>");
        pw.println("      Start USER_ID in background if it is currently stopped;");
        pw.println("      use switch-user if you want to start the user in foreground");
        pw.println("  unlock-user <USER_ID> [TOKEN_HEX]");
        pw.println("      Attempt to unlock the given user using the given authorization token.");
        pw.println("  stop-user [-w] [-f] <USER_ID>");
        pw.println("      Stop execution of USER_ID, not allowing it to run any");
        pw.println("      code until a later explicit start or switch to it.");
        pw.println("      -w: wait for stop-user to complete.");
        pw.println("      -f: force stop even if there are related users that cannot be stopped.");
        pw.println("  is-user-stopped <USER_ID>");
        pw.println("      Returns whether <USER_ID> has been stopped or not.");
        pw.println("  get-started-user-state <USER_ID>");
        pw.println("      Gets the current state of the given started user.");
        pw.println("  track-associations");
        pw.println("      Enable association tracking.");
        pw.println("  untrack-associations");
        pw.println("      Disable and clear association tracking.");
        pw.println("  get-uid-state <UID>");
        pw.println("      Gets the process state of an app given its <UID>.");
        pw.println("  attach-agent <PROCESS> <FILE>");
        pw.println("    Attach an agent to the specified <PROCESS>, which may be either a process name or a PID.");
        pw.println("  get-config");
        pw.println("      Rtrieve the configuration and any recent configurations of the device.");
        pw.println("  supports-multiwindow");
        pw.println("      Returns true if the device supports multiwindow.");
        pw.println("  supports-split-screen-multi-window");
        pw.println("      Returns true if the device supports split screen multiwindow.");
        pw.println("  suppress-resize-config-changes <true|false>");
        pw.println("      Suppresses configuration changes due to user resizing an activity/task.");
        pw.println("  set-inactive [--user <USER_ID>] <PACKAGE> true|false");
        pw.println("      Sets the inactive state of an app.");
        pw.println("  get-inactive [--user <USER_ID>] <PACKAGE>");
        pw.println("      Returns the inactive state of an app.");
        pw.println("  send-trim-memory [--user <USER_ID>] <PROCESS>");
        pw.println("          [HIDDEN|RUNNING_MODERATE|BACKGROUND|RUNNING_LOW|MODERATE|RUNNING_CRITICAL|COMPLETE]");
        pw.println("      Send a memory trim event to a <PROCESS>.");
        pw.println("  display [COMMAND] [...]: sub-commands for operating on displays.");
        pw.println("       move-stack <STACK_ID> <DISPLAY_ID>");
        pw.println("           Move <STACK_ID> from its current display to <DISPLAY_ID>.");
        pw.println("  stack [COMMAND] [...]: sub-commands for operating on activity stacks.");
        pw.println("       start <DISPLAY_ID> <INTENT>");
        pw.println("           Start a new activity on <DISPLAY_ID> using <INTENT>");
        pw.println("       move-task <TASK_ID> <STACK_ID> [true|false]");
        pw.println("           Move <TASK_ID> from its current stack to the top (true) or");
        pw.println("           bottom (false) of <STACK_ID>.");
        pw.println("       resize <STACK_ID> <LEFT,TOP,RIGHT,BOTTOM>");
        pw.println("           Change <STACK_ID> size and position to <LEFT,TOP,RIGHT,BOTTOM>.");
        pw.println("       resize-animated <STACK_ID> <LEFT,TOP,RIGHT,BOTTOM>");
        pw.println("           Same as resize, but allow animation.");
        pw.println("       resize-docked-stack <LEFT,TOP,RIGHT,BOTTOM> [<TASK_LEFT,TASK_TOP,TASK_RIGHT,TASK_BOTTOM>]");
        pw.println("           Change docked stack to <LEFT,TOP,RIGHT,BOTTOM>");
        pw.println("           and supplying temporary different task bounds indicated by");
        pw.println("           <TASK_LEFT,TOP,RIGHT,BOTTOM>");
        pw.println("       size-docked-stack-test: <STEP_SIZE> <l|t|r|b> [DELAY_MS]");
        pw.println("           Test command for sizing docked stack by");
        pw.println("           <STEP_SIZE> increments from the side <l>eft, <t>op, <r>ight, or <b>ottom");
        pw.println("           applying the optional [DELAY_MS] between each step.");
        pw.println("       move-top-activity-to-pinned-stack: <STACK_ID> <LEFT,TOP,RIGHT,BOTTOM>");
        pw.println("           Moves the top activity from");
        pw.println("           <STACK_ID> to the pinned stack using <LEFT,TOP,RIGHT,BOTTOM> for the");
        pw.println("           bounds of the pinned stack.");
        pw.println("       positiontask <TASK_ID> <STACK_ID> <POSITION>");
        pw.println("           Place <TASK_ID> in <STACK_ID> at <POSITION>");
        pw.println("       list");
        pw.println("           List all of the activity stacks and their sizes.");
        pw.println("       info <STACK_ID>");
        pw.println("           Display the information about activity stack <STACK_ID>.");
        pw.println("       remove <STACK_ID>");
        pw.println("           Remove stack <STACK_ID>.");
        pw.println("  task [COMMAND] [...]: sub-commands for operating on activity tasks.");
        pw.println("       lock <TASK_ID>");
        pw.println("           Bring <TASK_ID> to the front and don't allow other tasks to run.");
        pw.println("       lock stop");
        pw.println("           End the current task lock.");
        pw.println("       resizeable <TASK_ID> [0|1|2|3]");
        pw.println("           Change resizeable mode of <TASK_ID> to one of the following:");
        pw.println("           0: unresizeable");
        pw.println("           1: crop_windows");
        pw.println("           2: resizeable");
        pw.println("           3: resizeable_and_pipable");
        pw.println("       resize <TASK_ID> <LEFT,TOP,RIGHT,BOTTOM>");
        pw.println("           Makes sure <TASK_ID> is in a stack with the specified bounds.");
        pw.println("           Forces the task to be resizeable and creates a stack if no existing stack");
        pw.println("           has the specified bounds.");
        pw.println("       drag-task-test <TASK_ID> <STEP_SIZE> [DELAY_MS]");
        pw.println("           Test command for dragging/moving <TASK_ID> by");
        pw.println("           <STEP_SIZE> increments around the screen applying the optional [DELAY_MS]");
        pw.println("           between each step.");
        pw.println("       size-task-test <TASK_ID> <STEP_SIZE> [DELAY_MS]");
        pw.println("           Test command for sizing <TASK_ID> by <STEP_SIZE>");
        pw.println("           increments within the screen applying the optional [DELAY_MS] between");
        pw.println("           each step.");
        pw.println("  update-appinfo <USER_ID> <PACKAGE_NAME> [<PACKAGE_NAME>...]");
        pw.println("      Update the ApplicationInfo objects of the listed packages for <USER_ID>");
        pw.println("      without restarting any processes.");
        pw.println("  write");
        pw.println("      Write all pending state to storage.");
        pw.println();
        Intent.printIntentArgsHelp(pw, "");
    }
}

package com.android.commands.am;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.StackInfo;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.IActivityContainer;
import android.app.IActivityController;
import android.app.IActivityManager;
import android.app.IActivityManager.WaitResult;
import android.app.IInstrumentationWatcher.Stub;
import android.app.IStopUserCallback;
import android.app.IUiAutomationConnection;
import android.app.ProfilerInfo;
import android.app.UiAutomationConnection;
import android.app.usage.ConfigurationStats;
import android.app.usage.IUsageStatsManager;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.Intent.CommandOptionHandler;
import android.content.pm.IPackageManager;
import android.content.pm.InstrumentationInfo;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.ArrayMap;
import android.view.IWindowManager;
import com.android.internal.os.BaseCommand;
import com.android.internal.util.HexDump;
import com.android.internal.util.Preconditions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Am extends BaseCommand {
    private static final boolean GREATER_THAN_TARGET = true;
    private static final boolean MOVING_FORWARD = true;
    private static final boolean MOVING_HORIZONTALLY = true;
    private static final String SHELL_PACKAGE_NAME = "com.android.shell";
    private static final int STACK_BOUNDS_INSET = 10;
    private IActivityManager mAm;
    private boolean mAutoStop;
    private IPackageManager mPm;
    private String mProfileFile;
    private String mReceiverPermission;
    private int mRepeat;
    private int mSamplingInterval;
    private int mStackId;
    private int mStartFlags;
    private boolean mStopOption;
    private int mUserId;
    private boolean mWaitOption;

    /* renamed from: com.android.commands.am.Am.2 */
    class AnonymousClass2 implements Comparator<Configuration> {
        final /* synthetic */ ArrayMap val$recentConfigs;

        AnonymousClass2(ArrayMap val$recentConfigs) {
            this.val$recentConfigs = val$recentConfigs;
        }

        public int compare(Configuration a, Configuration b) {
            return ((Integer) this.val$recentConfigs.get(b)).compareTo((Integer) this.val$recentConfigs.get(a));
        }
    }

    private class InstrumentationWatcher extends Stub {
        private boolean mFinished;
        private boolean mRawMode;

        private InstrumentationWatcher() {
            this.mFinished = false;
            this.mRawMode = false;
        }

        public void setRawOutput(boolean rawMode) {
            this.mRawMode = rawMode;
        }

        public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                String pretty = null;
                if (!(this.mRawMode || results == null)) {
                    pretty = results.getString("stream");
                }
                if (pretty != null) {
                    System.out.print(pretty);
                } else {
                    if (results != null) {
                        for (String key : results.keySet()) {
                            System.out.println("INSTRUMENTATION_STATUS: " + key + "=" + results.get(key));
                        }
                    }
                    System.out.println("INSTRUMENTATION_STATUS_CODE: " + resultCode);
                }
                notifyAll();
            }
        }

        public void instrumentationFinished(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                String pretty = null;
                if (!(this.mRawMode || results == null)) {
                    pretty = results.getString("stream");
                }
                if (pretty != null) {
                    System.out.println(pretty);
                } else {
                    if (results != null) {
                        for (String key : results.keySet()) {
                            System.out.println("INSTRUMENTATION_RESULT: " + key + "=" + results.get(key));
                        }
                    }
                    System.out.println("INSTRUMENTATION_CODE: " + resultCode);
                }
                this.mFinished = Am.MOVING_HORIZONTALLY;
                notifyAll();
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean waitForFinish() {
            synchronized (this) {
                while (true) {
                    if (this.mFinished) {
                        return Am.MOVING_HORIZONTALLY;
                    }
                    try {
                        if (Am.this.mAm.asBinder().pingBinder()) {
                            wait(1000);
                        } else {
                            return false;
                        }
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }

    private class IntentReceiver extends IIntentReceiver.Stub {
        private boolean mFinished;

        private IntentReceiver() {
            this.mFinished = false;
        }

        public void performReceive(Intent intent, int resultCode, String data, Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
            String line = "Broadcast completed: result=" + resultCode;
            if (data != null) {
                line = line + ", data=\"" + data + "\"";
            }
            if (extras != null) {
                line = line + ", extras: " + extras;
            }
            System.out.println(line);
            synchronized (this) {
                this.mFinished = Am.MOVING_HORIZONTALLY;
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

    class MyActivityController extends IActivityController.Stub {
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
        final boolean mMonkey;
        int mResult;
        int mState;

        /* renamed from: com.android.commands.am.Am.MyActivityController.1 */
        class AnonymousClass1 extends Thread {
            final /* synthetic */ InputStreamReader val$converter;

            AnonymousClass1(InputStreamReader val$converter) {
                this.val$converter = val$converter;
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                BufferedReader in = new BufferedReader(this.val$converter);
                int count = MyActivityController.RESULT_EARLY_ANR_CONTINUE;
                while (true) {
                    synchronized (MyActivityController.this) {
                        if (MyActivityController.this.mGdbThread == null) {
                            return;
                        }
                        if (count == MyActivityController.STATE_EARLY_ANR) {
                            MyActivityController.this.mGotGdbPrint = Am.MOVING_HORIZONTALLY;
                            MyActivityController.this.notifyAll();
                        }
                        try {
                            String line = in.readLine();
                            if (line != null) {
                                System.out.println("GDB: " + line);
                                count += MyActivityController.STATE_CRASHED;
                            } else {
                                return;
                            }
                        } catch (IOException e) {
                            return;
                        }
                    }
                }
            }
        }

        MyActivityController(String gdbPort, boolean monkey) {
            this.mGdbPort = gdbPort;
            this.mMonkey = monkey;
        }

        public boolean activityResuming(String pkg) {
            synchronized (this) {
                System.out.println("** Activity resuming: " + pkg);
            }
            return Am.MOVING_HORIZONTALLY;
        }

        public boolean activityStarting(Intent intent, String pkg) {
            synchronized (this) {
                System.out.println("** Activity starting: " + pkg);
            }
            return Am.MOVING_HORIZONTALLY;
        }

        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) {
            boolean z = Am.MOVING_HORIZONTALLY;
            synchronized (this) {
                System.out.println("** ERROR: PROCESS CRASHED");
                System.out.println("processName: " + processName);
                System.out.println("processPid: " + pid);
                System.out.println("shortMsg: " + shortMsg);
                System.out.println("longMsg: " + longMsg);
                System.out.println("timeMillis: " + timeMillis);
                System.out.println("stack:");
                System.out.print(stackTrace);
                System.out.println("#");
                if (waitControllerLocked(pid, STATE_CRASHED) == STATE_CRASHED) {
                    z = false;
                }
            }
            return z;
        }

        public int appEarlyNotResponding(String processName, int pid, String annotation) {
            synchronized (this) {
                System.out.println("** ERROR: EARLY PROCESS NOT RESPONDING");
                System.out.println("processName: " + processName);
                System.out.println("processPid: " + pid);
                System.out.println("annotation: " + annotation);
                if (waitControllerLocked(pid, STATE_EARLY_ANR) == STATE_CRASHED) {
                    return -1;
                }
                return RESULT_EARLY_ANR_CONTINUE;
            }
        }

        public int appNotResponding(String processName, int pid, String processStats) {
            synchronized (this) {
                System.out.println("** ERROR: PROCESS NOT RESPONDING");
                System.out.println("processName: " + processName);
                System.out.println("processPid: " + pid);
                System.out.println("processStats:");
                System.out.print(processStats);
                System.out.println("#");
                int result = waitControllerLocked(pid, STATE_ANR);
                if (result == STATE_CRASHED) {
                    return -1;
                } else if (result == STATE_CRASHED) {
                    return STATE_CRASHED;
                } else {
                    return RESULT_EARLY_ANR_CONTINUE;
                }
            }
        }

        public int systemNotResponding(String message) {
            synchronized (this) {
                System.out.println("** ERROR: PROCESS NOT RESPONDING");
                System.out.println("message: " + message);
                System.out.println("#");
                System.out.println("Allowing system to die.");
            }
            return -1;
        }

        void killGdbLocked() {
            this.mGotGdbPrint = false;
            if (this.mGdbProcess != null) {
                System.out.println("Stopping gdbserver");
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
                    System.out.println("Starting gdbserver on port " + this.mGdbPort);
                    System.out.println("Do the following:");
                    System.out.println("  adb forward tcp:" + this.mGdbPort + " tcp:" + this.mGdbPort);
                    System.out.println("  gdbclient app_process :" + this.mGdbPort);
                    this.mGdbProcess = Runtime.getRuntime().exec(new String[]{"gdbserver", ":" + this.mGdbPort, "--attach", Integer.toString(pid)});
                    this.mGdbThread = new AnonymousClass1(new InputStreamReader(this.mGdbProcess.getInputStream()));
                    this.mGdbThread.start();
                    try {
                        wait(500);
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e2) {
                    System.err.println("Failure starting gdbserver: " + e2);
                    killGdbLocked();
                }
            }
            this.mState = state;
            System.out.println("");
            printMessageForState();
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
                this.mState = RESULT_EARLY_ANR_CONTINUE;
                this.mResult = result;
                notifyAll();
            }
        }

        void printMessageForState() {
            switch (this.mState) {
                case RESULT_EARLY_ANR_CONTINUE /*0*/:
                    System.out.println("Monitoring activity manager...  available commands:");
                    break;
                case STATE_CRASHED /*1*/:
                    System.out.println("Waiting after crash...  available commands:");
                    System.out.println("(c)ontinue: show crash dialog");
                    System.out.println("(k)ill: immediately kill app");
                    break;
                case STATE_EARLY_ANR /*2*/:
                    System.out.println("Waiting after early ANR...  available commands:");
                    System.out.println("(c)ontinue: standard ANR processing");
                    System.out.println("(k)ill: immediately kill app");
                    break;
                case STATE_ANR /*3*/:
                    System.out.println("Waiting after ANR...  available commands:");
                    System.out.println("(c)ontinue: show ANR dialog");
                    System.out.println("(k)ill: immediately kill app");
                    System.out.println("(w)ait: wait some more");
                    break;
            }
            System.out.println("(q)uit: finish monitoring");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void run() throws RemoteException {
            printMessageForState();
            Am.this.mAm.setActivityController(this, this.mMonkey);
            this.mState = RESULT_EARLY_ANR_CONTINUE;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                boolean addNewline = Am.MOVING_HORIZONTALLY;
                if (line.length() <= 0) {
                    addNewline = false;
                } else if ("q".equals(line) || "quit".equals(line)) {
                    resumeController(RESULT_EARLY_ANR_CONTINUE);
                } else if (this.mState == STATE_CRASHED) {
                    if ("c".equals(line) || "continue".equals(line)) {
                        resumeController(RESULT_EARLY_ANR_CONTINUE);
                    } else {
                        try {
                            if ("k".equals(line) || "kill".equals(line)) {
                                resumeController(STATE_CRASHED);
                            } else {
                                System.out.println("Invalid command: " + line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Am.this.mAm.setActivityController(null, this.mMonkey);
                            return;
                        } catch (Throwable th) {
                            Am.this.mAm.setActivityController(null, this.mMonkey);
                        }
                    }
                } else if (this.mState == STATE_ANR) {
                    if ("c".equals(line) || "continue".equals(line)) {
                        resumeController(RESULT_EARLY_ANR_CONTINUE);
                    } else if ("k".equals(line) || "kill".equals(line)) {
                        resumeController(STATE_CRASHED);
                    } else if ("w".equals(line) || "wait".equals(line)) {
                        resumeController(STATE_CRASHED);
                    } else {
                        System.out.println("Invalid command: " + line);
                    }
                } else if (this.mState != STATE_EARLY_ANR) {
                    System.out.println("Invalid command: " + line);
                } else if ("c".equals(line) || "continue".equals(line)) {
                    resumeController(RESULT_EARLY_ANR_CONTINUE);
                } else if ("k".equals(line) || "kill".equals(line)) {
                    resumeController(STATE_CRASHED);
                } else {
                    System.out.println("Invalid command: " + line);
                }
                synchronized (this) {
                    if (addNewline) {
                        System.out.println("");
                    }
                    printMessageForState();
                }
            }
            resumeController(RESULT_EARLY_ANR_CONTINUE);
            Am.this.mAm.setActivityController(null, this.mMonkey);
        }
    }

    private static class StopUserCallback extends IStopUserCallback.Stub {
        private boolean mFinished;

        private StopUserCallback() {
            this.mFinished = false;
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
            this.mFinished = Am.MOVING_HORIZONTALLY;
            notifyAll();
        }

        public synchronized void userStopAborted(int userId) {
            this.mFinished = Am.MOVING_HORIZONTALLY;
            notifyAll();
        }
    }

    public Am() {
        this.mStartFlags = 0;
        this.mWaitOption = false;
        this.mStopOption = false;
        this.mRepeat = 0;
    }

    public static void main(String[] args) {
        new Am().run(args);
    }

    public void onShowUsage(PrintStream out) {
        PrintWriter pw = new PrintWriter(out);
        pw.println("usage: am [subcommand] [options]\nusage: am start [-D] [-N] [-W] [-P <FILE>] [--start-profiler <FILE>]\n               [--sampling INTERVAL] [-R COUNT] [-S]\n               [--track-allocation] [--user <USER_ID> | current] <INTENT>\n       am startservice [--user <USER_ID> | current] <INTENT>\n       am stopservice [--user <USER_ID> | current] <INTENT>\n       am force-stop [--user <USER_ID> | all | current] <PACKAGE>\n       am kill [--user <USER_ID> | all | current] <PACKAGE>\n       am kill-all\n       am broadcast [--user <USER_ID> | all | current] <INTENT>\n       am instrument [-r] [-e <NAME> <VALUE>] [-p <FILE>] [-w]\n               [--user <USER_ID> | current]\n               [--no-window-animation] [--abi <ABI>] <COMPONENT>\n       am profile start [--user <USER_ID> current] [--sampling INTERVAL] <PROCESS> <FILE>\n       am profile stop [--user <USER_ID> current] [<PROCESS>]\n       am dumpheap [--user <USER_ID> current] [-n] <PROCESS> <FILE>\n       am set-debug-app [-w] [--persistent] <PACKAGE>\n       am clear-debug-app\n       am set-watch-heap <PROCESS> <MEM-LIMIT>\n       am clear-watch-heap\n       am bug-report [--progress]\n       am monitor [--gdb <port>]\n       am hang [--allow-restart]\n       am restart\n       am idle-maintenance\n       am screen-compat [on|off] <PACKAGE>\n       am package-importance <PACKAGE>\n       am to-uri [INTENT]\n       am to-intent-uri [INTENT]\n       am to-app-uri [INTENT]\n       am switch-user <USER_ID>\n       am start-user <USER_ID>\n       am unlock-user <USER_ID> [TOKEN_HEX]\n       am stop-user [-w] [-f] <USER_ID>\n       am stack start <DISPLAY_ID> <INTENT>\n       am stack movetask <TASK_ID> <STACK_ID> [true|false]\n       am stack resize <STACK_ID> <LEFT,TOP,RIGHT,BOTTOM>\n       am stack resize-animated <STACK_ID> <LEFT,TOP,RIGHT,BOTTOM>\n       am stack resize-docked-stack <LEFT,TOP,RIGHT,BOTTOM> [<TASK_LEFT,TASK_TOP,TASK_RIGHT,TASK_BOTTOM>]\n       am stack size-docked-stack-test: <STEP_SIZE> <l|t|r|b> [DELAY_MS]\n       am stack move-top-activity-to-pinned-stack: <STACK_ID> <LEFT,TOP,RIGHT,BOTTOM>\n       am stack positiontask <TASK_ID> <STACK_ID> <POSITION>\n       am stack list\n       am stack info <STACK_ID>\n       am stack remove <STACK_ID>\n       am task lock <TASK_ID>\n       am task lock stop\n       am task resizeable <TASK_ID> [0 (unresizeable) | 1 (crop_windows) | 2 (resizeable) | 3 (resizeable_and_pipable)]\n       am task resize <TASK_ID> <LEFT,TOP,RIGHT,BOTTOM>\n       am task drag-task-test <TASK_ID> <STEP_SIZE> [DELAY_MS] \n       am task size-task-test <TASK_ID> <STEP_SIZE> [DELAY_MS] \n       am get-config\n       am suppress-resize-config-changes <true|false>\n       am set-inactive [--user <USER_ID>] <PACKAGE> true|false\n       am get-inactive [--user <USER_ID>] <PACKAGE>\n       am send-trim-memory [--user <USER_ID>] <PROCESS>\n               [HIDDEN|RUNNING_MODERATE|BACKGROUND|RUNNING_LOW|MODERATE|RUNNING_CRITICAL|COMPLETE]\n       am get-current-user\n\nam start: start an Activity.  Options are:\n    -D: enable debugging\n    -N: enable native debugging\n    -W: wait for launch to complete\n    --start-profiler <FILE>: start profiler and send results to <FILE>\n    --sampling INTERVAL: use sample profiling with INTERVAL microseconds\n        between samples (use with --start-profiler)\n    -P <FILE>: like above, but profiling stops when app goes idle\n    -R: repeat the activity launch <COUNT> times.  Prior to each repeat,\n        the top activity will be finished.\n    -S: force stop the target app before starting the activity\n    --track-allocation: enable tracking of object allocations\n    --user <USER_ID> | current: Specify which user to run as; if not\n        specified then run as the current user.\n    --stack <STACK_ID>: Specify into which stack should the activity be put.\nam startservice: start a Service.  Options are:\n    --user <USER_ID> | current: Specify which user to run as; if not\n        specified then run as the current user.\n\nam stopservice: stop a Service.  Options are:\n    --user <USER_ID> | current: Specify which user to run as; if not\n        specified then run as the current user.\n\nam force-stop: force stop everything associated with <PACKAGE>.\n    --user <USER_ID> | all | current: Specify user to force stop;\n        all users if not specified.\n\nam kill: Kill all processes associated with <PACKAGE>.  Only kills.\n  processes that are safe to kill -- that is, will not impact the user\n  experience.\n    --user <USER_ID> | all | current: Specify user whose processes to kill;\n        all users if not specified.\n\nam kill-all: Kill all background processes.\n\nam broadcast: send a broadcast Intent.  Options are:\n    --user <USER_ID> | all | current: Specify which user to send to; if not\n        specified then send to all users.\n    --receiver-permission <PERMISSION>: Require receiver to hold permission.\n\nam instrument: start an Instrumentation.  Typically this target <COMPONENT>\n  is the form <TEST_PACKAGE>/<RUNNER_CLASS> or only <TEST_PACKAGE> if there \n  is only one instrumentation.  Options are:\n    -r: print raw results (otherwise decode REPORT_KEY_STREAMRESULT).  Use with\n        [-e perf true] to generate raw output for performance measurements.\n    -e <NAME> <VALUE>: set argument <NAME> to <VALUE>.  For test runners a\n        common form is [-e <testrunner_flag> <value>[,<value>...]].\n    -p <FILE>: write profiling data to <FILE>\n    -w: wait for instrumentation to finish before returning.  Required for\n        test runners.\n    --user <USER_ID> | current: Specify user instrumentation runs in;\n        current user if not specified.\n    --no-window-animation: turn off window animations while running.\n    --abi <ABI>: Launch the instrumented process with the selected ABI.\n        This assumes that the process supports the selected ABI.\n\nam trace-ipc: Trace IPC transactions.\n  start: start tracing IPC transactions.\n  stop: stop tracing IPC transactions and dump the results to file.\n    --dump-file <FILE>: Specify the file the trace should be dumped to.\n\nam profile: start and stop profiler on a process.  The given <PROCESS> argument\n  may be either a process name or pid.  Options are:\n    --user <USER_ID> | current: When supplying a process name,\n        specify user of process to profile; uses current user if not specified.\n\nam dumpheap: dump the heap of a process.  The given <PROCESS> argument may\n  be either a process name or pid.  Options are:\n    -n: dump native heap instead of managed heap\n    --user <USER_ID> | current: When supplying a process name,\n        specify user of process to dump; uses current user if not specified.\n\nam set-debug-app: set application <PACKAGE> to debug.  Options are:\n    -w: wait for debugger when application starts\n    --persistent: retain this value\n\nam clear-debug-app: clear the previously set-debug-app.\n\nam set-watch-heap: start monitoring pss size of <PROCESS>, if it is at or\n    above <HEAP-LIMIT> then a heap dump is collected for the user to report\n\nam clear-watch-heap: clear the previously set-watch-heap.\n\nam bug-report: request bug report generation; will launch a notification\n    when done to select where it should be delivered. Options are: \n   --progress: will launch a notification right away to show its progress.\n\nam monitor: start monitoring for crashes or ANRs.\n    --gdb: start gdbserv on the given port at crash/ANR\n\nam hang: hang the system.\n    --allow-restart: allow watchdog to perform normal system restart\n\nam restart: restart the user-space system.\n\nam idle-maintenance: perform idle maintenance now.\n\nam screen-compat: control screen compatibility mode of <PACKAGE>.\n\nam package-importance: print current importance of <PACKAGE>.\n\nam to-uri: print the given Intent specification as a URI.\n\nam to-intent-uri: print the given Intent specification as an intent: URI.\n\nam to-app-uri: print the given Intent specification as an android-app: URI.\n\nam switch-user: switch to put USER_ID in the foreground, starting\n  execution of that user if it is currently stopped.\n\nam start-user: start USER_ID in background if it is currently stopped,\n  use switch-user if you want to start the user in foreground.\n\nam stop-user: stop execution of USER_ID, not allowing it to run any\n  code until a later explicit start or switch to it.\n  -w: wait for stop-user to complete.\n  -f: force stop even if there are related users that cannot be stopped.\n\nam stack start: start a new activity on <DISPLAY_ID> using <INTENT>.\n\nam stack movetask: move <TASK_ID> from its current stack to the top (true) or   bottom (false) of <STACK_ID>.\n\nam stack resize: change <STACK_ID> size and position to <LEFT,TOP,RIGHT,BOTTOM>.\n\nam stack resize-docked-stack: change docked stack to <LEFT,TOP,RIGHT,BOTTOM>\n   and supplying temporary different task bounds indicated by\n   <TASK_LEFT,TOP,RIGHT,BOTTOM>\n\nam stack size-docked-stack-test: test command for sizing docked stack by\n   <STEP_SIZE> increments from the side <l>eft, <t>op, <r>ight, or <b>ottom\n   applying the optional [DELAY_MS] between each step.\n\nam stack move-top-activity-to-pinned-stack: moves the top activity from\n   <STACK_ID> to the pinned stack using <LEFT,TOP,RIGHT,BOTTOM> for the\n   bounds of the pinned stack.\n\nam stack positiontask: place <TASK_ID> in <STACK_ID> at <POSITION>\nam stack list: list all of the activity stacks and their sizes.\n\nam stack info: display the information about activity stack <STACK_ID>.\n\nam stack remove: remove stack <STACK_ID>.\n\nam task lock: bring <TASK_ID> to the front and don't allow other tasks to run.\n\nam task lock stop: end the current task lock.\n\nam task resizeable: change resizeable mode of <TASK_ID>.\n   0 (unresizeable) | 1 (crop_windows) | 2 (resizeable) | 3 (resizeable_and_pipable)\n\nam task resize: makes sure <TASK_ID> is in a stack with the specified bounds.\n   Forces the task to be resizeable and creates a stack if no existing stack\n   has the specified bounds.\n\nam task drag-task-test: test command for dragging/moving <TASK_ID> by\n   <STEP_SIZE> increments around the screen applying the optional [DELAY_MS]\n   between each step.\n\nam task size-task-test: test command for sizing <TASK_ID> by <STEP_SIZE>   increments within the screen applying the optional [DELAY_MS] between\n   each step.\n\nam get-config: retrieve the configuration and any recent configurations\n  of the device.\nam suppress-resize-config-changes: suppresses configuration changes due to\n  user resizing an activity/task.\n\nam set-inactive: sets the inactive state of an app.\n\nam get-inactive: returns the inactive state of an app.\n\nam send-trim-memory: send a memory trim event to a <PROCESS>.\n\nam get-current-user: returns id of the current foreground user.\n\n");
        Intent.printIntentArgsHelp(pw, "");
        pw.flush();
    }

    public void onRun() throws Exception {
        this.mAm = ActivityManagerNative.getDefault();
        if (this.mAm == null) {
            System.err.println("Error type 2");
            throw new AndroidException("Can't connect to activity manager; is the system running?");
        }
        this.mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (this.mPm == null) {
            System.err.println("Error type 2");
            throw new AndroidException("Can't connect to package manager; is the system running?");
        }
        String op = nextArgRequired();
        if (op.equals("start")) {
            runStart();
        } else if (op.equals("startservice")) {
            runStartService();
        } else if (op.equals("stopservice")) {
            runStopService();
        } else if (op.equals("force-stop")) {
            runForceStop();
        } else if (op.equals("kill")) {
            runKill();
        } else if (op.equals("kill-all")) {
            runKillAll();
        } else if (op.equals("instrument")) {
            runInstrument();
        } else if (op.equals("trace-ipc")) {
            runTraceIpc();
        } else if (op.equals("broadcast")) {
            sendBroadcast();
        } else if (op.equals("profile")) {
            runProfile();
        } else if (op.equals("dumpheap")) {
            runDumpHeap();
        } else if (op.equals("set-debug-app")) {
            runSetDebugApp();
        } else if (op.equals("clear-debug-app")) {
            runClearDebugApp();
        } else if (op.equals("set-watch-heap")) {
            runSetWatchHeap();
        } else if (op.equals("clear-watch-heap")) {
            runClearWatchHeap();
        } else if (op.equals("bug-report")) {
            runBugReport();
        } else if (op.equals("monitor")) {
            runMonitor();
        } else if (op.equals("hang")) {
            runHang();
        } else if (op.equals("restart")) {
            runRestart();
        } else if (op.equals("idle-maintenance")) {
            runIdleMaintenance();
        } else if (op.equals("screen-compat")) {
            runScreenCompat();
        } else if (op.equals("package-importance")) {
            runPackageImportance();
        } else if (op.equals("to-uri")) {
            runToUri(0);
        } else if (op.equals("to-intent-uri")) {
            runToUri(1);
        } else if (op.equals("to-app-uri")) {
            runToUri(2);
        } else if (op.equals("switch-user")) {
            runSwitchUser();
        } else if (op.equals("start-user")) {
            runStartUserInBackground();
        } else if (op.equals("unlock-user")) {
            runUnlockUser();
        } else if (op.equals("stop-user")) {
            runStopUser();
        } else if (op.equals("stack")) {
            runStack();
        } else if (op.equals("task")) {
            runTask();
        } else if (op.equals("get-config")) {
            runGetConfig();
        } else if (op.equals("suppress-resize-config-changes")) {
            runSuppressResizeConfigChanges();
        } else if (op.equals("set-inactive")) {
            runSetInactive();
        } else if (op.equals("get-inactive")) {
            runGetInactive();
        } else if (op.equals("send-trim-memory")) {
            runSendTrimMemory();
        } else if (op.equals("get-current-user")) {
            runGetCurrentUser();
        } else {
            showError("Error: unknown command '" + op + "'");
        }
    }

    int parseUserArg(String arg) {
        if ("all".equals(arg)) {
            return -1;
        }
        if ("current".equals(arg) || "cur".equals(arg)) {
            return -2;
        }
        return Integer.parseInt(arg);
    }

    private Intent makeIntent(int defUser) throws URISyntaxException {
        this.mStartFlags = 0;
        this.mWaitOption = false;
        this.mStopOption = false;
        this.mRepeat = 0;
        this.mProfileFile = null;
        this.mSamplingInterval = 0;
        this.mAutoStop = false;
        this.mUserId = defUser;
        this.mStackId = -1;
        return Intent.parseCommandArgs(this.mArgs, new CommandOptionHandler() {
            public boolean handleOption(String opt, ShellCommand cmd) {
                Am am;
                if (opt.equals("-D")) {
                    am = Am.this;
                    am.mStartFlags = am.mStartFlags | 2;
                } else if (opt.equals("-N")) {
                    am = Am.this;
                    am.mStartFlags = am.mStartFlags | 8;
                } else if (opt.equals("-W")) {
                    Am.this.mWaitOption = Am.MOVING_HORIZONTALLY;
                } else if (opt.equals("-P")) {
                    Am.this.mProfileFile = Am.this.nextArgRequired();
                    Am.this.mAutoStop = Am.MOVING_HORIZONTALLY;
                } else if (opt.equals("--start-profiler")) {
                    Am.this.mProfileFile = Am.this.nextArgRequired();
                    Am.this.mAutoStop = false;
                } else if (opt.equals("--sampling")) {
                    Am.this.mSamplingInterval = Integer.parseInt(Am.this.nextArgRequired());
                } else if (opt.equals("-R")) {
                    Am.this.mRepeat = Integer.parseInt(Am.this.nextArgRequired());
                } else if (opt.equals("-S")) {
                    Am.this.mStopOption = Am.MOVING_HORIZONTALLY;
                } else if (opt.equals("--track-allocation")) {
                    am = Am.this;
                    am.mStartFlags = am.mStartFlags | 4;
                } else if (opt.equals("--user")) {
                    Am.this.mUserId = Am.this.parseUserArg(Am.this.nextArgRequired());
                } else if (opt.equals("--receiver-permission")) {
                    Am.this.mReceiverPermission = Am.this.nextArgRequired();
                } else if (!opt.equals("--stack")) {
                    return false;
                } else {
                    Am.this.mStackId = Integer.parseInt(Am.this.nextArgRequired());
                }
                return Am.MOVING_HORIZONTALLY;
            }
        });
    }

    private void runStartService() throws Exception {
        Intent intent = makeIntent(-2);
        if (this.mUserId == -1) {
            System.err.println("Error: Can't start activity with user 'all'");
            return;
        }
        System.out.println("Starting service: " + intent);
        ComponentName cn = this.mAm.startService(null, intent, intent.getType(), SHELL_PACKAGE_NAME, this.mUserId);
        if (cn == null) {
            System.err.println("Error: Not found; no service started.");
        } else if (cn.getPackageName().equals("!")) {
            System.err.println("Error: Requires permission " + cn.getClassName());
        } else if (cn.getPackageName().equals("!!")) {
            System.err.println("Error: " + cn.getClassName());
        }
    }

    private void runStopService() throws Exception {
        Intent intent = makeIntent(-2);
        if (this.mUserId == -1) {
            System.err.println("Error: Can't stop activity with user 'all'");
            return;
        }
        System.out.println("Stopping service: " + intent);
        int result = this.mAm.stopService(null, intent, intent.getType(), this.mUserId);
        if (result == 0) {
            System.err.println("Service not stopped: was not running.");
        } else if (result == 1) {
            System.err.println("Service stopped");
        } else if (result == -1) {
            System.err.println("Error stopping service");
        }
    }

    private void runStart() throws Exception {
        Intent intent = makeIntent(-2);
        if (this.mUserId == -1) {
            System.err.println("Error: Can't start service with user 'all'");
            return;
        }
        String mimeType = intent.getType();
        if (mimeType == null && intent.getData() != null && "content".equals(intent.getData().getScheme())) {
            mimeType = this.mAm.getProviderMimeType(intent.getData(), this.mUserId);
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
                        System.err.println("Error: Intent does not match any activities: " + intent);
                        return;
                    } else if (activities.size() > 1) {
                        System.err.println("Error: Intent matches multiple activities; can't stop: " + intent);
                        return;
                    } else {
                        packageName = ((ResolveInfo) activities.get(0)).activityInfo.packageName;
                    }
                }
                System.out.println("Stopping: " + packageName);
                this.mAm.forceStopPackage(packageName, this.mUserId);
                Thread.sleep(250);
            }
            System.out.println("Starting: " + intent);
            intent.addFlags(268435456);
            ProfilerInfo profilerInfo = null;
            if (this.mProfileFile != null) {
                try {
                    profilerInfo = new ProfilerInfo(this.mProfileFile, openForSystemServer(new File(this.mProfileFile), 738197504), this.mSamplingInterval, this.mAutoStop);
                } catch (FileNotFoundException e) {
                    System.err.println("Error: Unable to open file: " + this.mProfileFile);
                    System.err.println("Consider using a file under /data/local/tmp/");
                    return;
                }
            }
            WaitResult waitResult = null;
            long startTime = SystemClock.uptimeMillis();
            ActivityOptions activityOptions = null;
            if (this.mStackId != -1) {
                activityOptions = ActivityOptions.makeBasic();
                activityOptions.setLaunchStackId(this.mStackId);
            }
            if (this.mWaitOption) {
                waitResult = this.mAm.startActivityAndWait(null, null, intent, mimeType, null, null, 0, this.mStartFlags, profilerInfo, activityOptions != null ? activityOptions.toBundle() : null, this.mUserId);
                res = waitResult.result;
            } else {
                res = this.mAm.startActivityAsUser(null, null, intent, mimeType, null, null, 0, this.mStartFlags, profilerInfo, activityOptions != null ? activityOptions.toBundle() : null, this.mUserId);
            }
            long endTime = SystemClock.uptimeMillis();
            PrintStream out = this.mWaitOption ? System.out : System.err;
            boolean launched = false;
            switch (res) {
                case -8:
                    out.println("Error: Not allowed to start background user activity that shouldn't be displayed for all users.");
                    break;
                case -7:
                    out.println("Error: Activity not started, voice control not allowed for: " + intent);
                    break;
                case -4:
                    out.println("Error: Activity not started, you do not have permission to access it.");
                    break;
                case -3:
                    out.println("Error: Activity not started, you requested to both forward and receive its result");
                    break;
                case -2:
                    out.println("Error type 3");
                    out.println("Error: Activity class " + intent.getComponent().toShortString() + " does not exist.");
                    break;
                case -1:
                    out.println("Error: Activity not started, unable to resolve " + intent.toString());
                    break;
                case 0:
                    launched = MOVING_HORIZONTALLY;
                    break;
                case 1:
                    launched = MOVING_HORIZONTALLY;
                    out.println("Warning: Activity not started because intent should be handled by the caller");
                    break;
                case 2:
                    launched = MOVING_HORIZONTALLY;
                    out.println("Warning: Activity not started, its current task has been brought to the front");
                    break;
                case 3:
                    launched = MOVING_HORIZONTALLY;
                    out.println("Warning: Activity not started, intent has been delivered to currently running top-most instance.");
                    break;
                case 4:
                    launched = MOVING_HORIZONTALLY;
                    out.println("Warning: Activity not started because the  current activity is being kept for the user.");
                    break;
                default:
                    out.println("Error: Activity not started, unknown error code " + res);
                    break;
            }
            if (this.mWaitOption && launched) {
                if (waitResult == null) {
                    waitResult = new WaitResult();
                    waitResult.who = intent.getComponent();
                }
                System.out.println("Status: " + (waitResult.timeout ? "timeout" : "ok"));
                if (waitResult.who != null) {
                    System.out.println("Activity: " + waitResult.who.flattenToShortString());
                }
                if (waitResult.thisTime >= 0) {
                    System.out.println("ThisTime: " + waitResult.thisTime);
                }
                if (waitResult.totalTime >= 0) {
                    System.out.println("TotalTime: " + waitResult.totalTime);
                }
                System.out.println("WaitTime: " + (endTime - startTime));
                System.out.println("Complete");
            }
            this.mRepeat--;
            if (this.mRepeat > 1) {
                this.mAm.unhandledBack();
            }
        } while (this.mRepeat > 1);
    }

    private void runForceStop() throws Exception {
        int userId = -1;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                this.mAm.forceStopPackage(nextArgRequired(), userId);
                return;
            } else if (opt.equals("--user")) {
                userId = parseUserArg(nextArgRequired());
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runKill() throws Exception {
        int userId = -1;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                this.mAm.killBackgroundProcesses(nextArgRequired(), userId);
                return;
            } else if (opt.equals("--user")) {
                userId = parseUserArg(nextArgRequired());
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runKillAll() throws Exception {
        this.mAm.killAllBackgroundProcesses();
    }

    private void sendBroadcast() throws Exception {
        Intent intent = makeIntent(-2);
        IntentReceiver receiver = new IntentReceiver();
        String[] strArr = this.mReceiverPermission == null ? null : new String[]{this.mReceiverPermission};
        System.out.println("Broadcasting: " + intent);
        this.mAm.broadcastIntent(null, intent, null, receiver, 0, null, null, strArr, -1, null, MOVING_HORIZONTALLY, false, this.mUserId);
        receiver.waitForFinish();
    }

    private void runInstrument() throws Exception {
        String profileFile = null;
        boolean wait = false;
        boolean rawMode = false;
        boolean no_window_animation = false;
        int userId = -2;
        Bundle args = new Bundle();
        IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        String str = null;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                break;
            }
            if (opt.equals("-p")) {
                profileFile = nextArgRequired();
            } else {
                if (opt.equals("-w")) {
                    wait = MOVING_HORIZONTALLY;
                } else {
                    if (opt.equals("-r")) {
                        rawMode = MOVING_HORIZONTALLY;
                    } else {
                        if (opt.equals("-e")) {
                            args.putString(nextArgRequired(), nextArgRequired());
                        } else {
                            if (!opt.equals("--no_window_animation")) {
                                if (!opt.equals("--no-window-animation")) {
                                    if (opt.equals("--user")) {
                                        userId = parseUserArg(nextArgRequired());
                                    } else {
                                        if (opt.equals("--abi")) {
                                            str = nextArgRequired();
                                        } else {
                                            System.err.println("Error: Unknown option: " + opt);
                                            return;
                                        }
                                    }
                                }
                            }
                            no_window_animation = MOVING_HORIZONTALLY;
                        }
                    }
                }
            }
        }
        if (userId == -1) {
            System.err.println("Error: Can't start instrumentation with user 'all'");
            return;
        }
        String cnArg = nextArgRequired();
        ComponentName cn;
        if (cnArg.contains("/")) {
            cn = ComponentName.unflattenFromString(cnArg);
            if (cn == null) {
                throw new IllegalArgumentException("Bad component name: " + cnArg);
            }
        }
        int i;
        List<InstrumentationInfo> infos = this.mPm.queryInstrumentation(null, 0).getList();
        int numInfos = infos == null ? 0 : infos.size();
        List<ComponentName> cns = new ArrayList();
        for (i = 0; i < numInfos; i++) {
            InstrumentationInfo info = (InstrumentationInfo) infos.get(i);
            ComponentName c = new ComponentName(info.packageName, info.name);
            if (cnArg.equals(info.packageName)) {
                cns.add(c);
            }
        }
        if (cns.size() == 0) {
            throw new IllegalArgumentException("No instrumentation found for: " + cnArg);
        } else if (cns.size() == 1) {
            cn = (ComponentName) cns.get(0);
        } else {
            StringBuilder cnsStr = new StringBuilder();
            int numCns = cns.size();
            for (i = 0; i < numCns; i++) {
                cnsStr.append(((ComponentName) cns.get(i)).flattenToString());
                cnsStr.append(", ");
            }
            cnsStr.setLength(cnsStr.length() - 2);
            throw new IllegalArgumentException("Found multiple instrumentations: " + cnsStr.toString());
        }
        Object obj = null;
        IUiAutomationConnection iUiAutomationConnection = null;
        if (wait) {
            Am am = this;
            obj = new InstrumentationWatcher();
            obj.setRawOutput(rawMode);
            iUiAutomationConnection = new UiAutomationConnection();
        }
        float[] fArr = null;
        if (no_window_animation) {
            fArr = wm.getAnimationScales();
            wm.setAnimationScale(0, 0.0f);
            wm.setAnimationScale(1, 0.0f);
        }
        if (str != null) {
            boolean matched = false;
            for (String equals : Build.SUPPORTED_ABIS) {
                if (equals.equals(str)) {
                    matched = MOVING_HORIZONTALLY;
                    break;
                }
            }
            if (!matched) {
                throw new AndroidException("INSTRUMENTATION_FAILED: Unsupported instruction set " + str);
            }
        }
        if (this.mAm.startInstrumentation(cn, profileFile, 0, args, obj, iUiAutomationConnection, userId, str)) {
            if (!(obj == null || obj.waitForFinish())) {
                System.out.println("INSTRUMENTATION_ABORTED: System has crashed.");
            }
            if (fArr != null) {
                wm.setAnimationScales(fArr);
            }
            return;
        }
        throw new AndroidException("INSTRUMENTATION_FAILED: " + cn.flattenToString());
    }

    private void runTraceIpc() throws Exception {
        String op = nextArgRequired();
        if (op.equals("start")) {
            runTraceIpcStart();
        } else if (op.equals("stop")) {
            runTraceIpcStop();
        } else {
            showError("Error: unknown command '" + op + "'");
        }
    }

    private void runTraceIpcStart() throws Exception {
        System.out.println("Starting IPC tracing.");
        this.mAm.startBinderTracking();
    }

    private void runTraceIpcStop() throws Exception {
        String str = null;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                break;
            } else if (opt.equals("--dump-file")) {
                str = nextArgRequired();
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
        if (str == null) {
            System.err.println("Error: Specify filename to dump logs to.");
            return;
        }
        try {
            File file = new File(str);
            file.delete();
            if (this.mAm.stopBinderTrackingAndDump(openForSystemServer(file, 738197504))) {
                System.out.println("Stopped IPC tracing. Dumping logs to: " + str);
                return;
            }
            throw new AndroidException("STOP TRACE FAILED.");
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to open file: " + str);
            System.err.println("Consider using a file under /data/local/tmp/");
        }
    }

    static void removeWallOption() {
        String props = SystemProperties.get("dalvik.vm.extra-opts");
        if (props != null && props.contains("-Xprofile:wallclock")) {
            SystemProperties.set("dalvik.vm.extra-opts", props.replace("-Xprofile:wallclock", "").trim());
        }
    }

    private void runProfile() throws Exception {
        String process;
        boolean start = false;
        boolean wall = false;
        int userId = -2;
        this.mSamplingInterval = 0;
        String cmd = nextArgRequired();
        String opt;
        if ("start".equals(cmd)) {
            start = MOVING_HORIZONTALLY;
            while (true) {
                opt = nextOption();
                if (opt == null) {
                    break;
                } else if (opt.equals("--user")) {
                    userId = parseUserArg(nextArgRequired());
                } else if (opt.equals("--wall")) {
                    wall = MOVING_HORIZONTALLY;
                } else if (opt.equals("--sampling")) {
                    this.mSamplingInterval = Integer.parseInt(nextArgRequired());
                } else {
                    System.err.println("Error: Unknown option: " + opt);
                    return;
                }
            }
            process = nextArgRequired();
        } else if ("stop".equals(cmd)) {
            while (true) {
                opt = nextOption();
                if (opt == null) {
                    break;
                } else if (opt.equals("--user")) {
                    userId = parseUserArg(nextArgRequired());
                } else {
                    System.err.println("Error: Unknown option: " + opt);
                    return;
                }
            }
            process = nextArg();
        } else {
            process = cmd;
            cmd = nextArgRequired();
            if ("start".equals(cmd)) {
                start = MOVING_HORIZONTALLY;
            } else if (!"stop".equals(cmd)) {
                throw new IllegalArgumentException("Profile command " + process + " not valid");
            }
        }
        if (userId == -1) {
            System.err.println("Error: Can't profile with user 'all'");
            return;
        }
        ProfilerInfo profilerInfo = null;
        if (start) {
            String profileFile = nextArgRequired();
            try {
                profilerInfo = new ProfilerInfo(profileFile, openForSystemServer(new File(profileFile), 738197504), this.mSamplingInterval, false);
            } catch (FileNotFoundException e) {
                System.err.println("Error: Unable to open file: " + profileFile);
                System.err.println("Consider using a file under /data/local/tmp/");
                return;
            }
        }
        if (wall) {
            try {
                String props = SystemProperties.get("dalvik.vm.extra-opts");
                if (props == null || !props.contains("-Xprofile:wallclock")) {
                    props = props + " -Xprofile:wallclock";
                }
            } catch (Throwable th) {
                if (wall) {
                }
            }
        } else if (start) {
        }
        if (!this.mAm.profileControl(process, userId, start, profilerInfo, 0)) {
            throw new AndroidException("PROFILE FAILED on process " + process);
        } else if (!wall) {
        }
    }

    private void runDumpHeap() throws Exception {
        boolean managed = MOVING_HORIZONTALLY;
        int userId = -2;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                break;
            } else if (opt.equals("--user")) {
                userId = parseUserArg(nextArgRequired());
                if (userId == -1) {
                    System.err.println("Error: Can't dump heap with user 'all'");
                    return;
                }
            } else if (opt.equals("-n")) {
                managed = false;
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
        String process = nextArgRequired();
        String heapFile = nextArgRequired();
        try {
            File file = new File(heapFile);
            file.delete();
            if (!this.mAm.dumpHeap(process, userId, managed, heapFile, openForSystemServer(file, 738197504))) {
                throw new AndroidException("HEAP DUMP FAILED on process " + process);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: Unable to open file: " + heapFile);
            System.err.println("Consider using a file under /data/local/tmp/");
        }
    }

    private void runSetDebugApp() throws Exception {
        boolean wait = false;
        boolean persistent = false;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                this.mAm.setDebugApp(nextArgRequired(), wait, persistent);
                return;
            } else if (opt.equals("-w")) {
                wait = MOVING_HORIZONTALLY;
            } else if (opt.equals("--persistent")) {
                persistent = MOVING_HORIZONTALLY;
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runClearDebugApp() throws Exception {
        this.mAm.setDebugApp(null, false, MOVING_HORIZONTALLY);
    }

    private void runSetWatchHeap() throws Exception {
        this.mAm.setDumpHeapDebugLimit(nextArgRequired(), 0, Long.parseLong(nextArgRequired()), null);
    }

    private void runClearWatchHeap() throws Exception {
        this.mAm.setDumpHeapDebugLimit(nextArgRequired(), 0, -1, null);
    }

    private void runBugReport() throws Exception {
        int bugreportType = 0;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                this.mAm.requestBugReport(bugreportType);
                System.out.println("Your lovely bug report is being created; please be patient.");
                return;
            } else if (opt.equals("--progress")) {
                bugreportType = 1;
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runSwitchUser() throws Exception {
        this.mAm.switchUser(Integer.parseInt(nextArgRequired()));
    }

    private void runStartUserInBackground() throws Exception {
        if (this.mAm.startUserInBackground(Integer.parseInt(nextArgRequired()))) {
            System.out.println("Success: user started");
        } else {
            System.err.println("Error: could not start user");
        }
    }

    private byte[] argToBytes(String arg) {
        if (arg.equals("!")) {
            return null;
        }
        return HexDump.hexStringToByteArray(arg);
    }

    private void runUnlockUser() throws Exception {
        if (this.mAm.unlockUser(Integer.parseInt(nextArgRequired()), argToBytes(nextArgRequired()), argToBytes(nextArgRequired()), null)) {
            System.out.println("Success: user unlocked");
        } else {
            System.err.println("Error: could not unlock user");
        }
    }

    private void runStopUser() throws Exception {
        boolean wait = false;
        boolean force = false;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                break;
            } else if ("-w".equals(opt)) {
                wait = MOVING_HORIZONTALLY;
            } else if ("-f".equals(opt)) {
                force = MOVING_HORIZONTALLY;
            } else {
                System.err.println("Error: unknown option: " + opt);
                return;
            }
        }
        int user = Integer.parseInt(nextArgRequired());
        Object stopUserCallback = wait ? new StopUserCallback() : null;
        int res = this.mAm.stopUser(user, force, stopUserCallback);
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
            System.err.println("Switch failed: " + res + txt);
        } else if (stopUserCallback != null) {
            stopUserCallback.waitForFinish();
        }
    }

    private void runMonitor() throws Exception {
        String gdbPort = null;
        boolean monkey = false;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                new MyActivityController(gdbPort, monkey).run();
                return;
            } else if (opt.equals("--gdb")) {
                gdbPort = nextArgRequired();
            } else if (opt.equals("-m")) {
                monkey = MOVING_HORIZONTALLY;
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runHang() throws Exception {
        boolean allowRestart = false;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                System.out.println("Hanging the system...");
                this.mAm.hang(new Binder(), allowRestart);
                return;
            } else if (opt.equals("--allow-restart")) {
                allowRestart = MOVING_HORIZONTALLY;
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runRestart() throws Exception {
        String opt = nextOption();
        if (opt != null) {
            System.err.println("Error: Unknown option: " + opt);
            return;
        }
        System.out.println("Restart the system...");
        this.mAm.restart();
    }

    private void runIdleMaintenance() throws Exception {
        String opt = nextOption();
        if (opt != null) {
            System.err.println("Error: Unknown option: " + opt);
            return;
        }
        System.out.println("Performing idle maintenance...");
        try {
            this.mAm.sendIdleJobTrigger();
        } catch (RemoteException e) {
        }
    }

    private void runScreenCompat() throws Exception {
        boolean enabled;
        String mode = nextArgRequired();
        if ("on".equals(mode)) {
            enabled = MOVING_HORIZONTALLY;
        } else if ("off".equals(mode)) {
            enabled = false;
        } else {
            System.err.println("Error: enabled mode must be 'on' or 'off' at " + mode);
            return;
        }
        String packageName = nextArgRequired();
        do {
            try {
                int i;
                IActivityManager iActivityManager = this.mAm;
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                iActivityManager.setPackageScreenCompatMode(packageName, i);
            } catch (RemoteException e) {
            }
            packageName = nextArg();
        } while (packageName != null);
    }

    private void runPackageImportance() throws Exception {
        try {
            System.out.println(RunningAppProcessInfo.procStateToImportance(this.mAm.getPackageProcessState(nextArgRequired(), SHELL_PACKAGE_NAME)));
        } catch (RemoteException e) {
        }
    }

    private void runToUri(int flags) throws Exception {
        System.out.println(makeIntent(-2).toUri(flags));
    }

    private void runStack() throws Exception {
        String op = nextArgRequired();
        if (op.equals("start")) {
            runStackStart();
        } else if (op.equals("movetask")) {
            runStackMoveTask();
        } else if (op.equals("resize")) {
            runStackResize();
        } else if (op.equals("resize-animated")) {
            runStackResizeAnimated();
        } else if (op.equals("resize-docked-stack")) {
            runStackResizeDocked();
        } else if (op.equals("positiontask")) {
            runStackPositionTask();
        } else if (op.equals("list")) {
            runStackList();
        } else if (op.equals("info")) {
            runStackInfo();
        } else if (op.equals("move-top-activity-to-pinned-stack")) {
            runMoveTopActivityToPinnedStack();
        } else if (op.equals("size-docked-stack-test")) {
            runStackSizeDockedStackTest();
        } else if (op.equals("remove")) {
            runStackRemove();
        } else {
            showError("Error: unknown command '" + op + "'");
        }
    }

    private void runStackStart() throws Exception {
        int displayId = Integer.parseInt(nextArgRequired());
        Intent intent = makeIntent(-2);
        try {
            IActivityContainer container = this.mAm.createStackOnDisplay(displayId);
            if (container != null) {
                container.startActivity(intent);
            }
        } catch (RemoteException e) {
        }
    }

    private void runStackMoveTask() throws Exception {
        boolean toTop;
        int taskId = Integer.parseInt(nextArgRequired());
        int stackId = Integer.parseInt(nextArgRequired());
        String toTopStr = nextArgRequired();
        if ("true".equals(toTopStr)) {
            toTop = MOVING_HORIZONTALLY;
        } else if ("false".equals(toTopStr)) {
            toTop = false;
        } else {
            System.err.println("Error: bad toTop arg: " + toTopStr);
            return;
        }
        try {
            this.mAm.moveTaskToStack(taskId, stackId, toTop);
        } catch (RemoteException e) {
        }
    }

    private void runStackResize() throws Exception {
        int stackId = Integer.parseInt(nextArgRequired());
        Rect bounds = getBounds();
        if (bounds == null) {
            System.err.println("Error: invalid input bounds");
        } else {
            resizeStack(stackId, bounds, 0);
        }
    }

    private void runStackResizeAnimated() throws Exception {
        Rect rect;
        int stackId = Integer.parseInt(nextArgRequired());
        if ("null".equals(this.mArgs.peekNextArg())) {
            rect = null;
        } else {
            rect = getBounds();
            if (rect == null) {
                System.err.println("Error: invalid input bounds");
                return;
            }
        }
        resizeStackUnchecked(stackId, rect, 0, MOVING_HORIZONTALLY);
    }

    private void resizeStackUnchecked(int stackId, Rect bounds, int delayMs, boolean animate) {
        try {
            this.mAm.resizeStack(stackId, bounds, false, false, animate, -1);
            Thread.sleep((long) delayMs);
        } catch (RemoteException e) {
            showError("Error: resizing stack " + e);
        } catch (InterruptedException e2) {
        }
    }

    private void runStackResizeDocked() throws Exception {
        Rect bounds = getBounds();
        Rect taskBounds = getBounds();
        if (bounds == null || taskBounds == null) {
            System.err.println("Error: invalid input bounds");
            return;
        }
        try {
            this.mAm.resizeDockedStack(bounds, taskBounds, null, null, null);
        } catch (RemoteException e) {
            showError("Error: resizing docked stack " + e);
        }
    }

    private void resizeStack(int stackId, Rect bounds, int delayMs) throws Exception {
        if (bounds == null) {
            showError("Error: invalid input bounds");
        } else {
            resizeStackUnchecked(stackId, bounds, delayMs, false);
        }
    }

    private void runStackPositionTask() throws Exception {
        try {
            this.mAm.positionTaskInStack(Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired()));
        } catch (RemoteException e) {
        }
    }

    private void runStackList() throws Exception {
        try {
            for (StackInfo info : this.mAm.getAllStackInfos()) {
                System.out.println(info);
            }
        } catch (RemoteException e) {
        }
    }

    private void runStackInfo() throws Exception {
        try {
            System.out.println(this.mAm.getStackInfo(Integer.parseInt(nextArgRequired())));
        } catch (RemoteException e) {
        }
    }

    private void runStackRemove() throws Exception {
        this.mAm.removeStack(Integer.parseInt(nextArgRequired()));
    }

    private void runMoveTopActivityToPinnedStack() throws Exception {
        int stackId = Integer.parseInt(nextArgRequired());
        Rect bounds = getBounds();
        if (bounds == null) {
            System.err.println("Error: invalid input bounds");
            return;
        }
        try {
            if (!this.mAm.moveTopActivityToPinnedStack(stackId, bounds)) {
                showError("Didn't move top activity to pinned stack.");
            }
        } catch (RemoteException e) {
            showError("Unable to move top activity: " + e);
        }
    }

    private void runStackSizeDockedStackTest() throws Exception {
        int stepSize = Integer.parseInt(nextArgRequired());
        String side = nextArgRequired();
        String delayStr = nextArg();
        int delayMs = delayStr != null ? Integer.parseInt(delayStr) : 0;
        try {
            StackInfo info = this.mAm.getStackInfo(3);
            if (info == null) {
                showError("Docked stack doesn't exist");
            } else if (info.bounds == null) {
                showError("Docked stack doesn't have a bounds");
            } else {
                int currentPoint;
                Rect bounds = info.bounds;
                int changeSize = (!"l".equals(side) ? "r".equals(side) : MOVING_HORIZONTALLY ? bounds.width() : bounds.height()) / 2;
                if (side.equals("l")) {
                    currentPoint = bounds.left;
                } else if (side.equals("r")) {
                    currentPoint = bounds.right;
                } else if (side.equals("t")) {
                    currentPoint = bounds.top;
                } else if (side.equals("b")) {
                    currentPoint = bounds.bottom;
                } else {
                    showError("Unknown growth side: " + side);
                    return;
                }
                int startPoint = currentPoint;
                int minPoint = currentPoint - changeSize;
                int maxPoint = currentPoint + changeSize;
                System.out.println("Shrinking docked stack side=" + side);
                while (currentPoint > minPoint) {
                    currentPoint -= Math.min(stepSize, currentPoint - minPoint);
                    setBoundsSide(bounds, side, currentPoint);
                    resizeStack(3, bounds, delayMs);
                }
                System.out.println("Growing docked stack side=" + side);
                while (currentPoint < maxPoint) {
                    currentPoint += Math.min(stepSize, maxPoint - currentPoint);
                    setBoundsSide(bounds, side, currentPoint);
                    resizeStack(3, bounds, delayMs);
                }
                System.out.println("Back to Original size side=" + side);
                while (currentPoint > startPoint) {
                    currentPoint -= Math.min(stepSize, currentPoint - startPoint);
                    setBoundsSide(bounds, side, currentPoint);
                    resizeStack(3, bounds, delayMs);
                }
            }
        } catch (RemoteException e) {
            showError("Unable to get docked stack info:" + e);
        }
    }

    private void setBoundsSide(Rect bounds, String side, int value) {
        if (side.equals("l")) {
            bounds.left = value;
        } else if (side.equals("r")) {
            bounds.right = value;
        } else if (side.equals("t")) {
            bounds.top = value;
        } else if (side.equals("b")) {
            bounds.bottom = value;
        } else {
            showError("Unknown set side: " + side);
        }
    }

    private void runTask() throws Exception {
        String op = nextArgRequired();
        if (op.equals("lock")) {
            runTaskLock();
        } else if (op.equals("resizeable")) {
            runTaskResizeable();
        } else if (op.equals("resize")) {
            runTaskResize();
        } else if (op.equals("drag-task-test")) {
            runTaskDragTaskTest();
        } else if (op.equals("size-task-test")) {
            runTaskSizeTaskTest();
        } else {
            showError("Error: unknown command '" + op + "'");
        }
    }

    private void runTaskLock() throws Exception {
        String taskIdStr = nextArgRequired();
        try {
            if (taskIdStr.equals("stop")) {
                this.mAm.stopLockTaskMode();
            } else {
                this.mAm.startLockTaskMode(Integer.parseInt(taskIdStr));
            }
            System.err.println("Activity manager is " + (this.mAm.isInLockTaskMode() ? "" : "not ") + "in lockTaskMode");
        } catch (RemoteException e) {
        }
    }

    private void runTaskResizeable() throws Exception {
        try {
            this.mAm.setTaskResizeable(Integer.parseInt(nextArgRequired()), Integer.parseInt(nextArgRequired()));
        } catch (RemoteException e) {
        }
    }

    private void runTaskResize() throws Exception {
        int taskId = Integer.parseInt(nextArgRequired());
        Rect bounds = getBounds();
        if (bounds == null) {
            System.err.println("Error: invalid input bounds");
        } else {
            taskResize(taskId, bounds, 0, false);
        }
    }

    private void taskResize(int taskId, Rect bounds, int delay_ms, boolean pretendUserResize) {
        try {
            this.mAm.resizeTask(taskId, bounds, pretendUserResize ? 1 : 0);
            Thread.sleep((long) delay_ms);
        } catch (RemoteException e) {
            System.err.println("Error changing task bounds: " + e);
        } catch (InterruptedException e2) {
        }
    }

    private void runTaskDragTaskTest() {
        int taskId = Integer.parseInt(nextArgRequired());
        int stepSize = Integer.parseInt(nextArgRequired());
        String delayStr = nextArg();
        int delay_ms = delayStr != null ? Integer.parseInt(delayStr) : 0;
        try {
            StackInfo stackInfo = this.mAm.getStackInfo(this.mAm.getFocusedStackId());
            Rect taskBounds = this.mAm.getTaskBounds(taskId);
            Rect stackBounds = stackInfo.bounds;
            int travelRight = stackBounds.width() - taskBounds.width();
            int travelLeft = -travelRight;
            int travelDown = stackBounds.height() - taskBounds.height();
            int travelUp = -travelDown;
            int passes = 0;
            while (passes < 2) {
                System.out.println("Moving right...");
                travelRight = moveTask(taskId, taskBounds, stackBounds, stepSize, travelRight, MOVING_HORIZONTALLY, MOVING_HORIZONTALLY, delay_ms);
                System.out.println("Still need to travel right by " + travelRight);
                System.out.println("Moving down...");
                travelDown = moveTask(taskId, taskBounds, stackBounds, stepSize, travelDown, MOVING_HORIZONTALLY, false, delay_ms);
                System.out.println("Still need to travel down by " + travelDown);
                System.out.println("Moving left...");
                travelLeft = moveTask(taskId, taskBounds, stackBounds, stepSize, travelLeft, false, MOVING_HORIZONTALLY, delay_ms);
                System.out.println("Still need to travel left by " + travelLeft);
                System.out.println("Moving up...");
                travelUp = moveTask(taskId, taskBounds, stackBounds, stepSize, travelUp, false, false, delay_ms);
                System.out.println("Still need to travel up by " + travelUp);
                try {
                    taskBounds = this.mAm.getTaskBounds(taskId);
                    passes++;
                } catch (RemoteException e) {
                    System.err.println("Error getting task bounds: " + e);
                    return;
                }
            }
        } catch (RemoteException e2) {
            System.err.println("Error getting focus stack info or task bounds: " + e2);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int moveTask(int taskId, Rect taskRect, Rect stackRect, int stepSize, int maxToTravel, boolean movingForward, boolean horizontal, int delay_ms) {
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
            while (maxToTravel < 0) {
                if (!horizontal || taskRect.left <= stackRect.left) {
                    if (!horizontal && taskRect.top > stackRect.top) {
                    }
                }
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

    private void runTaskSizeTaskTest() {
        int taskId = Integer.parseInt(nextArgRequired());
        int stepSize = Integer.parseInt(nextArgRequired());
        String delayStr = nextArg();
        int delay_ms = delayStr != null ? Integer.parseInt(delayStr) : 0;
        try {
            StackInfo stackInfo = this.mAm.getStackInfo(this.mAm.getFocusedStackId());
            Rect initialTaskBounds = this.mAm.getTaskBounds(taskId);
            Rect stackBounds = stackInfo.bounds;
            stackBounds.inset(STACK_BOUNDS_INSET, STACK_BOUNDS_INSET);
            Rect currentTaskBounds = new Rect(initialTaskBounds);
            System.out.println("Growing top-left");
            while (true) {
                currentTaskBounds.top -= getStepSize(currentTaskBounds.top, stackBounds.top, stepSize, MOVING_HORIZONTALLY);
                currentTaskBounds.left -= getStepSize(currentTaskBounds.left, stackBounds.left, stepSize, MOVING_HORIZONTALLY);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (stackBounds.top >= currentTaskBounds.top && stackBounds.left >= currentTaskBounds.left) {
                    break;
                }
            }
            System.out.println("Shrinking top-left");
            while (true) {
                currentTaskBounds.top += getStepSize(currentTaskBounds.top, initialTaskBounds.top, stepSize, false);
                currentTaskBounds.left += getStepSize(currentTaskBounds.left, initialTaskBounds.left, stepSize, false);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (initialTaskBounds.top <= currentTaskBounds.top && initialTaskBounds.left <= currentTaskBounds.left) {
                    break;
                }
            }
            System.out.println("Growing top-right");
            while (true) {
                currentTaskBounds.top -= getStepSize(currentTaskBounds.top, stackBounds.top, stepSize, MOVING_HORIZONTALLY);
                currentTaskBounds.right += getStepSize(currentTaskBounds.right, stackBounds.right, stepSize, false);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (stackBounds.top >= currentTaskBounds.top && stackBounds.right <= currentTaskBounds.right) {
                    break;
                }
            }
            System.out.println("Shrinking top-right");
            while (true) {
                currentTaskBounds.top += getStepSize(currentTaskBounds.top, initialTaskBounds.top, stepSize, false);
                currentTaskBounds.right -= getStepSize(currentTaskBounds.right, initialTaskBounds.right, stepSize, MOVING_HORIZONTALLY);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (initialTaskBounds.top <= currentTaskBounds.top && initialTaskBounds.right >= currentTaskBounds.right) {
                    break;
                }
            }
            System.out.println("Growing bottom-left");
            while (true) {
                currentTaskBounds.bottom += getStepSize(currentTaskBounds.bottom, stackBounds.bottom, stepSize, false);
                currentTaskBounds.left -= getStepSize(currentTaskBounds.left, stackBounds.left, stepSize, MOVING_HORIZONTALLY);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (stackBounds.bottom <= currentTaskBounds.bottom && stackBounds.left >= currentTaskBounds.left) {
                    break;
                }
            }
            System.out.println("Shrinking bottom-left");
            while (true) {
                currentTaskBounds.bottom -= getStepSize(currentTaskBounds.bottom, initialTaskBounds.bottom, stepSize, MOVING_HORIZONTALLY);
                currentTaskBounds.left += getStepSize(currentTaskBounds.left, initialTaskBounds.left, stepSize, false);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (initialTaskBounds.bottom >= currentTaskBounds.bottom && initialTaskBounds.left <= currentTaskBounds.left) {
                    break;
                }
            }
            System.out.println("Growing bottom-right");
            while (true) {
                currentTaskBounds.bottom += getStepSize(currentTaskBounds.bottom, stackBounds.bottom, stepSize, false);
                currentTaskBounds.right += getStepSize(currentTaskBounds.right, stackBounds.right, stepSize, false);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (stackBounds.bottom <= currentTaskBounds.bottom && stackBounds.right <= currentTaskBounds.right) {
                    break;
                }
            }
            System.out.println("Shrinking bottom-right");
            while (true) {
                currentTaskBounds.bottom -= getStepSize(currentTaskBounds.bottom, initialTaskBounds.bottom, stepSize, MOVING_HORIZONTALLY);
                currentTaskBounds.right -= getStepSize(currentTaskBounds.right, initialTaskBounds.right, stepSize, MOVING_HORIZONTALLY);
                taskResize(taskId, currentTaskBounds, delay_ms, MOVING_HORIZONTALLY);
                if (initialTaskBounds.bottom >= currentTaskBounds.bottom && initialTaskBounds.right >= currentTaskBounds.right) {
                    return;
                }
            }
        } catch (RemoteException e) {
            System.err.println("Error getting focus stack info or task bounds: " + e);
        }
    }

    private int getStepSize(int current, int target, int inStepSize, boolean greaterThanTarget) {
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
            Comparator<Configuration> comparator = new AnonymousClass2(recentConfigs);
            ArrayList<Configuration> configs = new ArrayList(recentConfigs.size());
            configs.addAll(recentConfigs.keySet());
            Collections.sort(configs, comparator);
            return configs;
        } catch (RemoteException e) {
            return Collections.emptyList();
        }
    }

    private void runGetConfig() throws Exception {
        int days = 14;
        String option = nextOption();
        if (option != null) {
            if (option.equals("--days")) {
                days = Integer.parseInt(nextArgRequired());
                if (days <= 0) {
                    throw new IllegalArgumentException("--days must be a positive integer");
                }
            }
            throw new IllegalArgumentException("unrecognized option " + option);
        }
        try {
            Configuration config = this.mAm.getConfiguration();
            if (config == null) {
                System.err.println("Activity manager has no configuration");
                return;
            }
            System.out.println("config: " + Configuration.resourceQualifierString(config));
            System.out.println("abi: " + TextUtils.join(",", Build.SUPPORTED_ABIS));
            List<Configuration> recentConfigs = getRecentConfigurations(days);
            int recentConfigSize = recentConfigs.size();
            if (recentConfigSize > 0) {
                System.out.println("recentConfigs:");
            }
            for (int i = 0; i < recentConfigSize; i++) {
                System.out.println("  config: " + Configuration.resourceQualifierString((Configuration) recentConfigs.get(i)));
            }
        } catch (RemoteException e) {
        }
    }

    private void runSuppressResizeConfigChanges() throws Exception {
        try {
            this.mAm.suppressResizeConfigChanges(Boolean.valueOf(nextArgRequired()).booleanValue());
        } catch (RemoteException e) {
            System.err.println("Error suppressing resize config changes: " + e);
        }
    }

    private void runSetInactive() throws Exception {
        int userId = -2;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                IUsageStatsManager.Stub.asInterface(ServiceManager.getService("usagestats")).setAppInactive(nextArgRequired(), Boolean.parseBoolean(nextArgRequired()), userId);
                return;
            } else if (opt.equals("--user")) {
                userId = parseUserArg(nextArgRequired());
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runGetInactive() throws Exception {
        int userId = -2;
        while (true) {
            String opt = nextOption();
            if (opt == null) {
                System.out.println("Idle=" + IUsageStatsManager.Stub.asInterface(ServiceManager.getService("usagestats")).isAppInactive(nextArgRequired(), userId));
                return;
            } else if (opt.equals("--user")) {
                userId = parseUserArg(nextArgRequired());
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        }
    }

    private void runSendTrimMemory() throws Exception {
        int userId = -2;
        do {
            String opt = nextOption();
            if (opt == null) {
                int level;
                String proc = nextArgRequired();
                String levelArg = nextArgRequired();
                if (levelArg.equals("HIDDEN")) {
                    level = 20;
                } else if (levelArg.equals("RUNNING_MODERATE")) {
                    level = 5;
                } else if (levelArg.equals("BACKGROUND")) {
                    level = 40;
                } else if (levelArg.equals("RUNNING_LOW")) {
                    level = STACK_BOUNDS_INSET;
                } else if (levelArg.equals("MODERATE")) {
                    level = 60;
                } else if (levelArg.equals("RUNNING_CRITICAL")) {
                    level = 15;
                } else if (levelArg.equals("COMPLETE")) {
                    level = 80;
                } else {
                    System.err.println("Error: Unknown level option: " + levelArg);
                    return;
                }
                if (!this.mAm.setProcessMemoryTrimLevel(proc, userId, level)) {
                    System.err.println("Error: Failure to set the level - probably Unknown Process: " + proc);
                }
                return;
            } else if (opt.equals("--user")) {
                userId = parseUserArg(nextArgRequired());
            } else {
                System.err.println("Error: Unknown option: " + opt);
                return;
            }
        } while (userId != -1);
        System.err.println("Error: Can't use user 'all'");
    }

    private void runGetCurrentUser() throws Exception {
        System.out.println(((UserInfo) Preconditions.checkNotNull(this.mAm.getCurrentUser(), "Current user not set")).id);
    }

    private static ParcelFileDescriptor openForSystemServer(File file, int mode) throws FileNotFoundException {
        ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, mode);
        String tcon = SELinux.getFileContext(file.getAbsolutePath());
        if (SELinux.checkSELinuxAccess("u:r:system_server:s0", tcon, "file", "read")) {
            return fd;
        }
        throw new FileNotFoundException("System server has no access to file context " + tcon);
    }

    private Rect getBounds() {
        String leftStr = nextArgRequired();
        int left = Integer.parseInt(leftStr);
        String topStr = nextArgRequired();
        int top = Integer.parseInt(topStr);
        String rightStr = nextArgRequired();
        int right = Integer.parseInt(rightStr);
        String bottomStr = nextArgRequired();
        int bottom = Integer.parseInt(bottomStr);
        if (left < 0) {
            System.err.println("Error: bad left arg: " + leftStr);
            return null;
        } else if (top < 0) {
            System.err.println("Error: bad top arg: " + topStr);
            return null;
        } else if (right <= 0) {
            System.err.println("Error: bad right arg: " + rightStr);
            return null;
        } else if (bottom > 0) {
            return new Rect(left, top, right, bottom);
        } else {
            System.err.println("Error: bad bottom arg: " + bottomStr);
            return null;
        }
    }
}

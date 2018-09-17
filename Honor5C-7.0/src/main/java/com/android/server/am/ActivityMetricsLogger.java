package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.content.Context;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;

class ActivityMetricsLogger {
    private static final long INVALID_START_TIME = -1;
    private static final String TAG = null;
    private static final String[] TRON_WINDOW_STATE_VARZ_STRINGS = null;
    private static final int WINDOW_STATE_FREEFORM = 2;
    private static final int WINDOW_STATE_INVALID = -1;
    private static final int WINDOW_STATE_SIDE_BY_SIDE = 1;
    private static final int WINDOW_STATE_STANDARD = 0;
    private final Context mContext;
    private long mCurrentTransitionStartTime;
    private long mLastLogTimeSecs;
    private boolean mLoggedStartingWindowDrawn;
    private boolean mLoggedTransitionStarting;
    private boolean mLoggedWindowsDrawn;
    private final ActivityStackSupervisor mSupervisor;
    private int mWindowState;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityMetricsLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActivityMetricsLogger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityMetricsLogger.<clinit>():void");
    }

    ActivityMetricsLogger(ActivityStackSupervisor supervisor, Context context) {
        this.mWindowState = 0;
        this.mCurrentTransitionStartTime = INVALID_START_TIME;
        this.mLastLogTimeSecs = SystemClock.elapsedRealtime() / 1000;
        this.mSupervisor = supervisor;
        this.mContext = context;
    }

    void logWindowState() {
        long now = SystemClock.elapsedRealtime() / 1000;
        if (this.mWindowState != WINDOW_STATE_INVALID) {
            MetricsLogger.count(this.mContext, TRON_WINDOW_STATE_VARZ_STRINGS[this.mWindowState], (int) (now - this.mLastLogTimeSecs));
        }
        this.mLastLogTimeSecs = now;
        ActivityStack stack = this.mSupervisor.getStack(3);
        if (stack == null || stack.getStackVisibilityLocked(null) == 0) {
            this.mWindowState = WINDOW_STATE_INVALID;
            stack = this.mSupervisor.getFocusedStack();
            if (stack.mStackId == 4) {
                stack = this.mSupervisor.findStackBehind(stack);
            }
            if (stack.mStackId == 0 || stack.mStackId == WINDOW_STATE_SIDE_BY_SIDE) {
                this.mWindowState = 0;
            } else if (stack.mStackId == 3) {
                Slog.wtf(TAG, "Docked stack shouldn't be the focused stack, because it reported not being visible.");
                this.mWindowState = WINDOW_STATE_INVALID;
            } else if (stack.mStackId == WINDOW_STATE_FREEFORM) {
                this.mWindowState = WINDOW_STATE_FREEFORM;
            } else if (StackId.isStaticStack(stack.mStackId)) {
                throw new IllegalStateException("Unknown stack=" + stack);
            }
            return;
        }
        this.mWindowState = WINDOW_STATE_SIDE_BY_SIDE;
    }

    void notifyActivityLaunching() {
        this.mCurrentTransitionStartTime = System.currentTimeMillis();
    }

    void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity) {
        String str;
        boolean processSwitch = true;
        ProcessRecord processRecord = null;
        if (launchedActivity != null) {
            processRecord = (ProcessRecord) this.mSupervisor.mService.mProcessNames.get(launchedActivity.processName, launchedActivity.appInfo.uid);
        }
        boolean processRunning = processRecord != null;
        if (launchedActivity != null) {
            str = launchedActivity.shortComponentName;
        } else {
            str = null;
        }
        if (processRecord != null && hasStartedActivity(processRecord, launchedActivity)) {
            processSwitch = false;
        }
        notifyActivityLaunched(resultCode, str, processRunning, processSwitch);
    }

    private boolean hasStartedActivity(ProcessRecord record, ActivityRecord launchedActivity) {
        ArrayList<ActivityRecord> activities = record.activities;
        for (int i = activities.size() + WINDOW_STATE_INVALID; i >= 0; i += WINDOW_STATE_INVALID) {
            ActivityRecord activity = (ActivityRecord) activities.get(i);
            if (launchedActivity != activity && !activity.stopped) {
                return true;
            }
        }
        return false;
    }

    private void notifyActivityLaunched(int resultCode, String componentName, boolean processRunning, boolean processSwitch) {
        if (resultCode < 0 || componentName == null || !processSwitch) {
            reset();
            return;
        }
        MetricsLogger.action(this.mContext, 323, componentName);
        MetricsLogger.action(this.mContext, 324, processRunning);
        MetricsLogger.action(this.mContext, 325, (int) (SystemClock.uptimeMillis() / 1000));
    }

    void notifyWindowsDrawn() {
        if (isTransitionActive() && !this.mLoggedWindowsDrawn) {
            MetricsLogger.action(this.mContext, 322, calculateCurrentDelay());
            this.mLoggedWindowsDrawn = true;
            if (this.mLoggedTransitionStarting) {
                reset();
            }
        }
    }

    void notifyStartingWindowDrawn() {
        if (isTransitionActive() && !this.mLoggedStartingWindowDrawn) {
            this.mLoggedStartingWindowDrawn = true;
            MetricsLogger.action(this.mContext, 321, calculateCurrentDelay());
        }
    }

    void notifyTransitionStarting(int reason) {
        if (isTransitionActive() && !this.mLoggedTransitionStarting) {
            MetricsLogger.action(this.mContext, 320, reason);
            MetricsLogger.action(this.mContext, 319, calculateCurrentDelay());
            this.mLoggedTransitionStarting = true;
            if (this.mLoggedWindowsDrawn) {
                reset();
            }
        }
    }

    private boolean isTransitionActive() {
        return this.mCurrentTransitionStartTime != INVALID_START_TIME;
    }

    private void reset() {
        this.mCurrentTransitionStartTime = INVALID_START_TIME;
        this.mLoggedWindowsDrawn = false;
        this.mLoggedTransitionStarting = false;
        this.mLoggedStartingWindowDrawn = false;
    }

    private int calculateCurrentDelay() {
        return (int) (System.currentTimeMillis() - this.mCurrentTransitionStartTime);
    }
}

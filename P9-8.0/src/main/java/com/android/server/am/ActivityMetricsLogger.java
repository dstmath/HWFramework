package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.content.Context;
import android.metrics.LogMaker;
import android.os.SystemClock;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.internal.logging.MetricsLogger;
import java.util.ArrayList;

class ActivityMetricsLogger {
    private static final long INVALID_START_TIME = -1;
    private static final String TAG = "ActivityManager";
    private static final String[] TRON_WINDOW_STATE_VARZ_STRINGS = new String[]{"window_time_0", "window_time_1", "window_time_2", "window_time_3"};
    private static final int WINDOW_STATE_ASSISTANT = 3;
    private static final int WINDOW_STATE_FREEFORM = 2;
    private static final int WINDOW_STATE_INVALID = -1;
    private static final int WINDOW_STATE_SIDE_BY_SIDE = 1;
    private static final int WINDOW_STATE_STANDARD = 0;
    private final Context mContext;
    private int mCurrentTransitionDelayMs;
    private int mCurrentTransitionDeviceUptime;
    private long mCurrentTransitionStartTime = -1;
    private long mLastLogTimeSecs = (SystemClock.elapsedRealtime() / 1000);
    private boolean mLoggedTransitionStarting;
    private final MetricsLogger mMetricsLogger = new MetricsLogger();
    private final SparseArray<StackTransitionInfo> mStackTransitionInfo = new SparseArray();
    private final ActivityStackSupervisor mSupervisor;
    private int mWindowState = 0;

    private final class StackTransitionInfo {
        private int bindApplicationDelayMs;
        private boolean currentTransitionProcessRunning;
        private ActivityRecord launchedActivity;
        private boolean loggedStartingWindowDrawn;
        private boolean loggedWindowsDrawn;
        private int reason;
        private int startResult;
        private int startingWindowDelayMs;
        private int windowsDrawnDelayMs;

        /* synthetic */ StackTransitionInfo(ActivityMetricsLogger this$0, StackTransitionInfo -this1) {
            this();
        }

        private StackTransitionInfo() {
            this.startingWindowDelayMs = -1;
            this.bindApplicationDelayMs = -1;
            this.reason = 3;
        }
    }

    ActivityMetricsLogger(ActivityStackSupervisor supervisor, Context context) {
        this.mSupervisor = supervisor;
        this.mContext = context;
    }

    void logWindowState() {
        long now = SystemClock.elapsedRealtime() / 1000;
        if (this.mWindowState != -1) {
            MetricsLogger.count(this.mContext, TRON_WINDOW_STATE_VARZ_STRINGS[this.mWindowState], (int) (now - this.mLastLogTimeSecs));
        }
        this.mLastLogTimeSecs = now;
        ActivityStack stack = this.mSupervisor.getStack(3);
        if (stack == null || stack.shouldBeVisible(null) == 0) {
            this.mWindowState = -1;
            stack = this.mSupervisor.getFocusedStack();
            if (stack.mStackId == 4) {
                stack = this.mSupervisor.findStackBehind(stack);
            }
            if (StackId.isHomeOrRecentsStack(stack.mStackId) || stack.mStackId == 1) {
                this.mWindowState = 0;
            } else if (stack.mStackId == 3) {
                Slog.wtf(TAG, "Docked stack shouldn't be the focused stack, because it reported not being visible.");
                this.mWindowState = -1;
            } else if (stack.mStackId == 2) {
                this.mWindowState = 2;
            } else if (stack.mStackId == 6) {
                this.mWindowState = 3;
            } else if (StackId.isStaticStack(stack.mStackId)) {
                throw new IllegalStateException("Unknown stack=" + stack);
            }
            return;
        }
        this.mWindowState = 1;
    }

    void notifyActivityLaunching() {
        if (!isAnyTransitionActive()) {
            this.mCurrentTransitionStartTime = SystemClock.uptimeMillis();
        }
    }

    void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity) {
        ProcessRecord processRecord;
        boolean processSwitch;
        if (launchedActivity != null) {
            processRecord = (ProcessRecord) this.mSupervisor.mService.mProcessNames.get(launchedActivity.processName, launchedActivity.appInfo.uid);
        } else {
            processRecord = null;
        }
        boolean processRunning = processRecord != null;
        if (processRecord != null) {
            processSwitch = hasStartedActivity(processRecord, launchedActivity) ^ 1;
        } else {
            processSwitch = true;
        }
        notifyActivityLaunched(resultCode, launchedActivity, processRunning, processSwitch);
    }

    private boolean hasStartedActivity(ProcessRecord record, ActivityRecord launchedActivity) {
        ArrayList<ActivityRecord> activities = record.activities;
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityRecord activity = (ActivityRecord) activities.get(i);
            if (launchedActivity != activity && !activity.stopped) {
                return true;
            }
        }
        return false;
    }

    private void notifyActivityLaunched(int resultCode, ActivityRecord launchedActivity, boolean processRunning, boolean processSwitch) {
        int stackId;
        if (launchedActivity == null || launchedActivity.getStack() == null) {
            stackId = -1;
        } else {
            stackId = launchedActivity.getStack().mStackId;
        }
        if (this.mCurrentTransitionStartTime != -1) {
            StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.get(stackId);
            if (launchedActivity == null || info == null) {
                boolean otherStacksLaunching = this.mStackTransitionInfo.size() > 0 && info == null;
                if ((resultCode < 0 || launchedActivity == null || (processSwitch ^ 1) != 0 || stackId == -1) && (otherStacksLaunching ^ 1) != 0) {
                    reset(true);
                    return;
                } else if (!otherStacksLaunching) {
                    StackTransitionInfo newInfo = new StackTransitionInfo(this, null);
                    newInfo.launchedActivity = launchedActivity;
                    newInfo.currentTransitionProcessRunning = processRunning;
                    newInfo.startResult = resultCode;
                    this.mStackTransitionInfo.append(stackId, newInfo);
                    this.mCurrentTransitionDeviceUptime = (int) (SystemClock.uptimeMillis() / 1000);
                    return;
                } else {
                    return;
                }
            }
            info.launchedActivity = launchedActivity;
        }
    }

    void notifyWindowsDrawn(int stackId, long timestamp) {
        StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.get(stackId);
        if (info != null && !info.loggedWindowsDrawn) {
            info.windowsDrawnDelayMs = calculateDelay(timestamp);
            info.loggedWindowsDrawn = true;
            if (allStacksWindowsDrawn() && this.mLoggedTransitionStarting) {
                reset(false);
            }
        }
    }

    void notifyStartingWindowDrawn(int stackId, long timestamp) {
        StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.get(stackId);
        if (info != null && !info.loggedStartingWindowDrawn) {
            info.loggedStartingWindowDrawn = true;
            info.startingWindowDelayMs = calculateDelay(timestamp);
        }
    }

    void notifyTransitionStarting(SparseIntArray stackIdReasons, long timestamp) {
        if (isAnyTransitionActive() && !this.mLoggedTransitionStarting) {
            this.mCurrentTransitionDelayMs = calculateDelay(timestamp);
            this.mLoggedTransitionStarting = true;
            for (int index = stackIdReasons.size() - 1; index >= 0; index--) {
                StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.get(stackIdReasons.keyAt(index));
                if (info != null) {
                    info.reason = stackIdReasons.valueAt(index);
                }
            }
            if (allStacksWindowsDrawn()) {
                reset(false);
            }
        }
    }

    void notifyVisibilityChanged(ActivityRecord activityRecord, boolean visible) {
        StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.get(activityRecord.getStackId());
        if (info != null && (visible ^ 1) != 0 && info.launchedActivity == activityRecord) {
            this.mStackTransitionInfo.remove(activityRecord.getStackId());
            if (this.mStackTransitionInfo.size() == 0) {
                reset(true);
            }
        }
    }

    void notifyBindApplication(ProcessRecord app) {
        for (int i = this.mStackTransitionInfo.size() - 1; i >= 0; i--) {
            StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.valueAt(i);
            if (info.launchedActivity.appInfo == app.info) {
                info.bindApplicationDelayMs = calculateCurrentDelay();
            }
        }
    }

    private boolean allStacksWindowsDrawn() {
        for (int index = this.mStackTransitionInfo.size() - 1; index >= 0; index--) {
            if (!((StackTransitionInfo) this.mStackTransitionInfo.valueAt(index)).loggedWindowsDrawn) {
                return false;
            }
        }
        return true;
    }

    private boolean isAnyTransitionActive() {
        if (this.mCurrentTransitionStartTime == -1 || this.mStackTransitionInfo.size() <= 0) {
            return false;
        }
        return true;
    }

    private void reset(boolean abort) {
        if (!abort && isAnyTransitionActive()) {
            logAppTransitionMultiEvents();
        }
        this.mCurrentTransitionStartTime = -1;
        this.mCurrentTransitionDelayMs = -1;
        this.mLoggedTransitionStarting = false;
        this.mStackTransitionInfo.clear();
    }

    private int calculateCurrentDelay() {
        return (int) (SystemClock.uptimeMillis() - this.mCurrentTransitionStartTime);
    }

    private int calculateDelay(long timestamp) {
        return (int) (timestamp - this.mCurrentTransitionStartTime);
    }

    private void logAppTransitionMultiEvents() {
        int index = this.mStackTransitionInfo.size() - 1;
        while (index >= 0) {
            StackTransitionInfo info = (StackTransitionInfo) this.mStackTransitionInfo.valueAt(index);
            int type = getTransitionType(info);
            if (type != -1) {
                LogMaker builder = new LogMaker(761);
                builder.setPackageName(info.launchedActivity.packageName);
                builder.setType(type);
                builder.addTaggedData(871, info.launchedActivity.info.name);
                boolean isInstantApp = info.launchedActivity.info.applicationInfo.isInstantApp();
                if (isInstantApp && info.launchedActivity.launchedFromPackage != null) {
                    builder.addTaggedData(904, info.launchedActivity.launchedFromPackage);
                }
                if (info.launchedActivity.info.launchToken != null) {
                    builder.addTaggedData(903, info.launchedActivity.info.launchToken);
                    info.launchedActivity.info.launchToken = null;
                }
                builder.addTaggedData(905, Integer.valueOf(isInstantApp ? 1 : 0));
                builder.addTaggedData(325, Integer.valueOf(this.mCurrentTransitionDeviceUptime));
                builder.addTaggedData(319, Integer.valueOf(this.mCurrentTransitionDelayMs));
                builder.setSubtype(info.reason);
                if (info.startingWindowDelayMs != -1) {
                    builder.addTaggedData(321, Integer.valueOf(info.startingWindowDelayMs));
                }
                if (info.bindApplicationDelayMs != -1) {
                    builder.addTaggedData(945, Integer.valueOf(info.bindApplicationDelayMs));
                }
                builder.addTaggedData(322, Integer.valueOf(info.windowsDrawnDelayMs));
                this.mMetricsLogger.write(builder);
                index--;
            } else {
                return;
            }
        }
    }

    private int getTransitionType(StackTransitionInfo info) {
        if (info.currentTransitionProcessRunning) {
            if (info.startResult == 0) {
                return 8;
            }
            if (info.startResult == 2) {
                return 9;
            }
        } else if (info.startResult == 0) {
            return 7;
        }
        return -1;
    }
}

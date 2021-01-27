package com.android.server.power;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManagerInternal;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.metrics.LogMaker;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.StatsLog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.NsdService;
import com.android.server.hdmi.HdmiCecKeycode;
import com.android.server.inputmethod.InputMethodManagerInternal;
import com.android.server.pg.PGManagerInternal;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.statusbar.StatusBarManagerInternal;

@VisibleForTesting
public class Notifier {
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int INTERACTIVE_STATE_ASLEEP = 2;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_UNKNOWN = 0;
    private static final boolean IS_SUPPORT_AOD = "1".equals(SystemProperties.get("ro.config.support_aod", (String) null));
    private static final boolean IS_SUPPORT_AP = "2".equals(SystemProperties.get("ro.config.support_aod", (String) null));
    private static final int MSG_BROADCAST = 2;
    private static final int MSG_PROFILE_TIMED_OUT = 5;
    private static final int MSG_RESUME_SYSTEM_BRIGHTNESS = 100;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED = 4;
    private static final int MSG_USER_ACTIVITY = 1;
    private static final int MSG_WIRED_CHARGING_STARTED = 6;
    private static final int MSG_WIRELESS_CHARGING_STARTED = 3;
    private static final String TAG = "PowerManagerNotifier";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).build();
    private static final VibrationEffect WIRELESS_CHARGING_VIBRATION_EFFECT = VibrationEffect.createWaveform(WIRELESS_VIBRATION_TIME, WIRELESS_VIBRATION_AMPLITUDE, -1);
    private static final int[] WIRELESS_VIBRATION_AMPLITUDE = {1, 4, 11, 25, 44, 67, 91, HdmiCecKeycode.CEC_KEYCODE_F2_RED, 123, 103, 79, 55, 34, 17, 7, 2};
    private static final long[] WIRELESS_VIBRATION_TIME = {40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40};
    private static final boolean mFactoryModeEnable = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    private final ActivityManagerInternal mActivityManagerInternal;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStats;
    private boolean mBrightnessModeChangeNoClearOffset = false;
    private boolean mBroadcastInProgress;
    private long mBroadcastStartTime;
    private int mBroadcastedInteractiveState;
    private final Context mContext;
    private final BroadcastReceiver mGoToSleepBroadcastDone = new BroadcastReceiver() {
        /* class com.android.server.power.Notifier.AnonymousClass8 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_DONE, 0, Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), 1);
            Notifier.this.sendNextBroadcast();
        }
    };
    private final NotifierHandler mHandler;
    private final InputManagerInternal mInputManagerInternal;
    private final InputMethodManagerInternal mInputMethodManagerInternal;
    private boolean mInteractive = true;
    private int mInteractiveChangeReason;
    private long mInteractiveChangeStartTime;
    private boolean mInteractiveChanging;
    private final Object mLock = new Object();
    private PGManagerInternal mPGManagerInternal;
    private boolean mPendingGoToSleepBroadcast;
    private int mPendingInteractiveState;
    private boolean mPendingWakeUpBroadcast;
    private final WindowManagerPolicy mPolicy;
    private final BroadcastReceiver mScreeBrightnessBoostChangedDone = new BroadcastReceiver() {
        /* class com.android.server.power.Notifier.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Notifier.this.mSuspendBlocker.release();
        }
    };
    private final Intent mScreenBrightnessBoostIntent;
    private final Intent mScreenOffIntent;
    private final Intent mScreenOnIntent;
    private final StatusBarManagerInternal mStatusBarManagerInternal;
    private final SuspendBlocker mSuspendBlocker;
    private final boolean mSuspendWhenScreenOffDueToProximityConfig;
    private final TrustManager mTrustManager;
    private boolean mUserActivityPending;
    private final Vibrator mVibrator;
    private final BroadcastReceiver mWakeUpBroadcastDone = new BroadcastReceiver() {
        /* class com.android.server.power.Notifier.AnonymousClass7 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_DONE, 1, Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), 1);
            Notifier.this.sendNextBroadcast();
        }
    };

    public Notifier(Looper looper, Context context, IBatteryStats batteryStats, SuspendBlocker suspendBlocker, WindowManagerPolicy policy) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mSuspendBlocker = suspendBlocker;
        this.mPolicy = policy;
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        this.mStatusBarManagerInternal = (StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class);
        this.mTrustManager = (TrustManager) this.mContext.getSystemService(TrustManager.class);
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        this.mHandler = new NotifierHandler(looper);
        this.mScreenOnIntent = new Intent("android.intent.action.SCREEN_ON");
        this.mScreenOnIntent.addFlags(1344274432);
        this.mScreenOffIntent = new Intent("android.intent.action.SCREEN_OFF");
        this.mScreenOffIntent.addFlags(1478492160);
        this.mScreenBrightnessBoostIntent = new Intent("android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED");
        this.mScreenBrightnessBoostIntent.addFlags(1342177280);
        this.mSuspendWhenScreenOffDueToProximityConfig = context.getResources().getBoolean(17891546);
        try {
            this.mBatteryStats.noteInteractive(true);
        } catch (RemoteException e) {
        }
        StatsLog.write(33, 1);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:16:0x0040 */
    /* JADX WARN: Type inference failed for: r9v3 */
    /* JADX WARN: Type inference failed for: r9v4 */
    /* JADX WARN: Type inference failed for: r9v5 */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void onWakeLockAcquired(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        WorkSource workSource2;
        ?? r9;
        if (this.mPGManagerInternal == null) {
            this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        }
        PGManagerInternal pGManagerInternal = this.mPGManagerInternal;
        if (pGManagerInternal != null) {
            r9 = 160;
            pGManagerInternal.notifyWakelock(flags, tag, ownerUid, ownerPid, workSource, 160);
        }
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        if (monitorType >= 0) {
            boolean unimportantForLogging = ownerUid == 1000 && (flags & 1073741824) != 0;
            if (workSource != null) {
                try {
                    r9 = workSource;
                    try {
                        this.mBatteryStats.noteStartWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                        workSource2 = r9;
                    } catch (RemoteException e) {
                        return;
                    }
                } catch (RemoteException e2) {
                    return;
                }
            } else {
                workSource2 = workSource;
                try {
                    this.mBatteryStats.noteStartWakelock(ownerUid, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                    this.mAppOps.startOpNoThrow(40, ownerUid, packageName);
                } catch (RemoteException e3) {
                    return;
                }
            }
            if (monitorType != 0) {
                return;
            }
            if (this.mPGManagerInternal != null) {
                try {
                    this.mPGManagerInternal.noteStartWakeLock(tag, workSource2, packageName, ownerUid);
                } catch (RemoteException e4) {
                }
            }
        }
    }

    public void onLongPartialWakeLockStart(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockStart: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                this.mBatteryStats.noteLongPartialWakelockStartFromSource(tag, historyTag, workSource);
                StatsLog.write(11, workSource, tag, historyTag, 1);
            } catch (RemoteException e) {
            }
        } else {
            this.mBatteryStats.noteLongPartialWakelockStart(tag, historyTag, ownerUid);
            StatsLog.write_non_chained(11, ownerUid, null, tag, historyTag, 1);
        }
    }

    public void onLongPartialWakeLockFinish(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockFinish: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                this.mBatteryStats.noteLongPartialWakelockFinishFromSource(tag, historyTag, workSource);
                StatsLog.write(11, workSource, tag, historyTag, 0);
            } catch (RemoteException e) {
            }
        } else {
            this.mBatteryStats.noteLongPartialWakelockFinish(tag, historyTag, ownerUid);
            StatsLog.write_non_chained(11, ownerUid, null, tag, historyTag, 0);
        }
    }

    public void onWakeLockChanging(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag, int newFlags, String newTag, String newPackageName, int newOwnerUid, int newOwnerPid, WorkSource newWorkSource, String newHistoryTag) {
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        int newMonitorType = getBatteryStatsWakeLockMonitorType(newFlags);
        if (this.mPGManagerInternal == null) {
            this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        }
        if (workSource == null || newWorkSource == null || monitorType < 0 || newMonitorType < 0) {
            onWakeLockReleased(flags, tag, packageName, ownerUid, ownerPid, workSource, historyTag);
            onWakeLockAcquired(newFlags, newTag, newPackageName, newOwnerUid, newOwnerPid, newWorkSource, newHistoryTag);
            return;
        }
        PGManagerInternal pGManagerInternal = this.mPGManagerInternal;
        if (pGManagerInternal != null) {
            pGManagerInternal.notifyWakelock(flags, tag, ownerUid, ownerPid, workSource, newWorkSource);
        }
        if (DEBUG) {
            Slog.d(TAG, "onWakeLockChanging: flags=" + newFlags + ", tag=\"" + newTag + "\", packageName=" + newPackageName + ", ownerUid=" + newOwnerUid + ", ownerPid=" + newOwnerPid + ", workSource=" + newWorkSource);
        }
        try {
            this.mBatteryStats.noteChangeWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, newWorkSource, newOwnerPid, newTag, newHistoryTag, newMonitorType, newOwnerUid == 1000 && (1073741824 & newFlags) != 0);
            if (monitorType == 0 && this.mPGManagerInternal != null) {
                this.mPGManagerInternal.noteChangeWakeLock(tag, workSource, packageName, ownerUid, newTag, newWorkSource, newPackageName, newOwnerUid);
            }
        } catch (RemoteException e) {
        }
    }

    public void onWakeLockReleased(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        if (this.mPGManagerInternal == null) {
            this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
        }
        PGManagerInternal pGManagerInternal = this.mPGManagerInternal;
        if (pGManagerInternal != null) {
            pGManagerInternal.notifyWakelock(flags, tag, ownerUid, ownerPid, workSource, 161);
        }
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        if (monitorType >= 0) {
            if (workSource != null) {
                try {
                    this.mBatteryStats.noteStopWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType);
                } catch (RemoteException e) {
                    return;
                }
            } else {
                this.mBatteryStats.noteStopWakelock(ownerUid, ownerPid, tag, historyTag, monitorType);
                this.mAppOps.finishOp(40, ownerUid, packageName);
            }
            if (monitorType != 0) {
                return;
            }
            if (this.mPGManagerInternal != null) {
                try {
                    this.mPGManagerInternal.noteStopWakeLock(tag, workSource, packageName, ownerUid);
                } catch (RemoteException e2) {
                }
            }
        }
    }

    private int getBatteryStatsWakeLockMonitorType(int flags) {
        int i = 65535 & flags;
        if (i == 1) {
            return 0;
        }
        if (i == 6 || i == 10) {
            return 1;
        }
        if (i == 32) {
            return this.mSuspendWhenScreenOffDueToProximityConfig ? -1 : 0;
        }
        if (i == 64 || i != 128) {
            return -1;
        }
        return 18;
    }

    public void onWakefulnessChangeStarted(final int wakefulness, int reason, long eventTime) {
        int i;
        boolean interactive = PowerManagerInternal.isInteractive(wakefulness);
        Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power onWakefulnessChangeStarted: wakefulness=" + wakefulness + ", reason=" + reason + ", interactive=" + interactive + ", mInteractive=" + this.mInteractive + ", mInteractiveChanging=" + this.mInteractiveChanging);
        this.mHandler.post(new Runnable() {
            /* class com.android.server.power.Notifier.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Notifier.this.mActivityManagerInternal.onWakefulnessChanged(wakefulness);
            }
        });
        if (this.mInteractive != interactive) {
            if (this.mInteractiveChanging) {
                handleLateInteractiveChange();
            }
            this.mInputManagerInternal.setInteractive(interactive);
            this.mInputMethodManagerInternal.setInteractive(interactive);
            try {
                this.mBatteryStats.noteInteractive(interactive);
            } catch (RemoteException e) {
            }
            if (interactive) {
                i = 1;
            } else {
                i = 0;
            }
            StatsLog.write(33, i);
            this.mInteractive = interactive;
            this.mInteractiveChangeReason = reason;
            this.mInteractiveChangeStartTime = eventTime;
            this.mInteractiveChanging = true;
            handleEarlyInteractiveChange();
        }
    }

    public void onWakefulnessChangeFinished() {
        Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power onWakefulnessChangeFinished mInteractiveChanging=" + this.mInteractiveChanging + ", mInteractive=" + this.mInteractive);
        if (this.mInteractiveChanging) {
            this.mInteractiveChanging = false;
            handleLateInteractiveChange();
            if (this.mInteractive) {
                Jlog.d(8, "JL_PMS_WAKEUP_FINISHED");
            }
        }
    }

    private void handleEarlyInteractiveChange() {
        synchronized (this.mLock) {
            if (this.mInteractive) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Notifier.this.mPolicy.startedWakingUp(Notifier.translateOnReason(Notifier.this.mInteractiveChangeReason));
                    }
                });
                if (IS_SUPPORT_AOD || IS_SUPPORT_AP) {
                    this.mPolicy.setAodState(100);
                }
                this.mPendingInteractiveState = 1;
                this.mPendingWakeUpBroadcast = true;
                updatePendingBroadcastLocked();
            } else {
                final int why = translateOffReason(this.mInteractiveChangeReason);
                this.mPolicy.startedGoingToSleepSync(why);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass3 */

                    @Override // java.lang.Runnable
                    public void run() {
                        Notifier.this.mPolicy.startedGoingToSleep(why);
                    }
                });
                if (IS_SUPPORT_AOD || IS_SUPPORT_AP) {
                    this.mPolicy.setAodState(101);
                }
            }
        }
    }

    private void handleLateInteractiveChange() {
        synchronized (this.mLock) {
            final int interactiveChangeLatency = (int) (SystemClock.uptimeMillis() - this.mInteractiveChangeStartTime);
            if (this.mInteractive) {
                final int why = translateOnReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass4 */

                    @Override // java.lang.Runnable
                    public void run() {
                        LogMaker log = new LogMaker(198);
                        log.setType(1);
                        log.setSubtype(why);
                        log.setLatency((long) interactiveChangeLatency);
                        log.addTaggedData(1694, Integer.valueOf(Notifier.this.mInteractiveChangeReason));
                        MetricsLogger.action(log);
                        EventLogTags.writePowerScreenState(1, 0, 0, 0, interactiveChangeLatency);
                        Notifier.this.mPolicy.finishedWakingUp(why);
                    }
                });
            } else {
                if (this.mUserActivityPending) {
                    this.mUserActivityPending = false;
                    this.mHandler.removeMessages(1);
                }
                final int why2 = translateOffReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.power.Notifier.AnonymousClass5 */

                    @Override // java.lang.Runnable
                    public void run() {
                        LogMaker log = new LogMaker(198);
                        log.setType(2);
                        log.setSubtype(why2);
                        log.setLatency((long) interactiveChangeLatency);
                        log.addTaggedData(1695, Integer.valueOf(Notifier.this.mInteractiveChangeReason));
                        MetricsLogger.action(log);
                        EventLogTags.writePowerScreenState(0, why2, 0, 0, interactiveChangeLatency);
                        Notifier.this.mPolicy.finishedGoingToSleep(why2);
                    }
                });
                this.mPendingInteractiveState = 2;
                this.mPendingGoToSleepBroadcast = true;
                updatePendingBroadcastLocked();
            }
        }
    }

    private static int translateOffReason(int reason) {
        if (reason == 1) {
            return 1;
        }
        if (reason == 2) {
            return 3;
        }
        if (reason == 100) {
            return 6;
        }
        if (reason != 102) {
            return 2;
        }
        return 7;
    }

    /* access modifiers changed from: private */
    public static int translateOnReason(int reason) {
        switch (reason) {
            case 1:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 9:
                return 1;
            case 2:
                return 2;
            case 8:
            default:
                return 3;
        }
    }

    public void onScreenBrightnessBoostChanged() {
        if (DEBUG) {
            Slog.d(TAG, "onScreenBrightnessBoostChanged");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(4);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    public void onUserActivity(int event, int uid) {
        try {
            this.mBatteryStats.noteUserActivity(uid, event);
        } catch (RemoteException e) {
        }
        synchronized (this.mLock) {
            if (!this.mUserActivityPending) {
                this.mUserActivityPending = true;
                Message msg = this.mHandler.obtainMessage(1);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    public void onWakeUp(int reason, String details, int reasonUid, String opPackageName, int opUid) {
        Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power onWakeUp: reason=" + PowerManager.wakeReasonToString(reason) + ", details=" + details + ", reasonUid=" + reasonUid + " opPackageName=" + opPackageName + " opUid=" + opUid);
        try {
            this.mBatteryStats.noteWakeUp(details, reasonUid);
            if (opPackageName != null) {
                this.mAppOps.noteOpNoThrow(61, opUid, opPackageName);
            }
        } catch (RemoteException e) {
        }
    }

    public void onProfileTimeout(int userId) {
        Message msg = this.mHandler.obtainMessage(5);
        msg.setAsynchronous(true);
        msg.arg1 = userId;
        this.mHandler.sendMessage(msg);
    }

    public void onWirelessChargingStarted(int batteryLevel, int userId) {
        if (DEBUG) {
            Slog.d(TAG, "onWirelessChargingStarted");
        }
    }

    public void onWiredChargingStarted(int userId) {
        if (DEBUG) {
            Slog.d(TAG, "onWiredChargingStarted");
        }
    }

    private void updatePendingBroadcastLocked() {
        int i;
        if (!this.mBroadcastInProgress && (i = this.mPendingInteractiveState) != 0) {
            if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || i != this.mBroadcastedInteractiveState) {
                this.mBroadcastInProgress = true;
                this.mSuspendBlocker.acquire();
                Message msg = this.mHandler.obtainMessage(2);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void finishPendingBroadcastLocked() {
        this.mBroadcastInProgress = false;
        this.mSuspendBlocker.release();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendUserActivity() {
        synchronized (this.mLock) {
            if (this.mUserActivityPending) {
                this.mUserActivityPending = false;
                this.mPolicy.userActivity();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNextBroadcast() {
        int powerState;
        synchronized (this.mLock) {
            if (this.mBroadcastedInteractiveState == 0) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
            } else if (this.mBroadcastedInteractiveState == 1) {
                if (!this.mPendingWakeUpBroadcast && !this.mPendingGoToSleepBroadcast) {
                    if (this.mPendingInteractiveState != 2) {
                        finishPendingBroadcastLocked();
                        return;
                    }
                }
                this.mPendingGoToSleepBroadcast = false;
                this.mBroadcastedInteractiveState = 2;
            } else {
                if (!this.mPendingWakeUpBroadcast && !this.mPendingGoToSleepBroadcast) {
                    if (this.mPendingInteractiveState != 1) {
                        finishPendingBroadcastLocked();
                        return;
                    }
                }
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
            }
            this.mBroadcastStartTime = SystemClock.uptimeMillis();
            powerState = this.mBroadcastedInteractiveState;
        }
        EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_SEND, 1);
        if (powerState == 1) {
            sendWakeUpBroadcast();
        } else {
            sendGoToSleepBroadcast();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBrightnessBoostChangedBroadcast() {
        Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending brightness boost changed broadcast.");
        this.mContext.sendOrderedBroadcastAsUser(this.mScreenBrightnessBoostIntent, UserHandle.ALL, null, this.mScreeBrightnessBoostChangedDone, this.mHandler, 0, null, null);
    }

    private void sendWakeUpBroadcast() {
        Flog.i((int) NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Sending wake up broadcast.");
        if (this.mActivityManagerInternal.isSystemReady()) {
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOnIntent, UserHandle.ALL, null, this.mWakeUpBroadcastDone, this.mHandler, 0, null, null);
            return;
        }
        EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_STOP, 2, 1);
        sendNextBroadcast();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resumeSystemBrightness() {
        ContentResolver cr = this.mContext.getContentResolver();
        int userHandle = ActivityManager.getCurrentUser();
        String mode = Settings.System.getStringForUser(cr, "hw_screen_brightness_mode_value", userHandle);
        String modeCurrent = Settings.System.getStringForUser(cr, "screen_brightness_mode", userHandle);
        if (!(mode == null || modeCurrent == null)) {
            if ((modeCurrent.equals("0") && mode.equals("1")) || (modeCurrent.equals("1") && mode.equals("0"))) {
                Slog.i(TAG, "resumeSystemBrightness modeCurrent=" + modeCurrent + ",mode=" + mode);
                Settings.System.putStringForUser(cr, "screen_brightness_mode", mode, userHandle);
            }
            if (mode.equals("0")) {
                String lastBrightness = Settings.System.getStringForUser(cr, "hw_screen_temp_brightness", userHandle);
                String lastBrightnessTmp = Settings.System.getStringForUser(cr, "screen_brightness", userHandle);
                if (!(lastBrightness == null || lastBrightnessTmp == null || lastBrightness.equals(lastBrightnessTmp))) {
                    Slog.i(TAG, "resumeSystemBrightness lastBrightnessTmp=" + lastBrightnessTmp + ",setlastBrightness=" + lastBrightness);
                    Settings.System.putStringForUser(cr, "screen_brightness", lastBrightness, userHandle);
                }
            }
        }
        if (modeCurrent != null && mode != null) {
            if (!modeCurrent.equals("0") || !mode.equals("1")) {
                this.mBrightnessModeChangeNoClearOffset = false;
                return;
            }
            Settings.System.putStringForUser(cr, "screen_auto_brightness", "0", userHandle);
            if (DEBUG) {
                Slog.d(TAG, "manul2auto set mBrightnessModeChangeNoClearOffset=" + this.mBrightnessModeChangeNoClearOffset);
            }
            this.mBrightnessModeChangeNoClearOffset = true;
        }
    }

    public boolean getBrightnessModeChangeNoClearOffset() {
        return this.mBrightnessModeChangeNoClearOffset;
    }

    public void setBrightnessModeChangeNoClearOffset(boolean enable) {
        this.mBrightnessModeChangeNoClearOffset = enable;
    }

    private void sendGoToSleepBroadcast() {
        Flog.i((int) NsdService.NativeResponseCode.SERVICE_LOST, "UL_Power Sending go to sleep broadcast.");
        if (this.mActivityManagerInternal.isSystemReady()) {
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOffIntent, UserHandle.ALL, null, this.mGoToSleepBroadcastDone, this.mHandler, 0, null, null);
            if (mFactoryModeEnable) {
                Slog.i(TAG, "no resumeSystemBrightness,mFactoryModeEnable=" + mFactoryModeEnable);
                return;
            }
            Message msg = this.mHandler.obtainMessage(100);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
            return;
        }
        EventLog.writeEvent((int) EventLogTags.POWER_SCREEN_BROADCAST_STOP, 3, 1);
        sendNextBroadcast();
    }

    private void playChargingStartedFeedback(int userId) {
        Ringtone sfx;
        playChargingStartedVibration(userId);
        String soundPath = Settings.Global.getString(this.mContext.getContentResolver(), "wireless_charging_started_sound");
        if (isChargingFeedbackEnabled(userId) && soundPath != null) {
            Uri soundUri = Uri.parse("file://" + soundPath);
            if (soundUri != null && (sfx = RingtoneManager.getRingtone(this.mContext, soundUri)) != null) {
                sfx.setStreamType(1);
                sfx.play();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWirelessChargingStarted(int batteryLevel, int userId) {
        playChargingStartedFeedback(userId);
        StatusBarManagerInternal statusBarManagerInternal = this.mStatusBarManagerInternal;
        if (statusBarManagerInternal != null) {
            statusBarManagerInternal.showChargingAnimation(batteryLevel);
        }
        this.mSuspendBlocker.release();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showWiredChargingStarted(int userId) {
        playChargingStartedFeedback(userId);
        this.mSuspendBlocker.release();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lockProfile(int userId) {
        this.mTrustManager.setDeviceLockedForUser(userId, true);
    }

    private void playChargingStartedVibration(int userId) {
        boolean vibrateEnabled = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "charging_vibration_enabled", 1, userId) == 0) {
            vibrateEnabled = false;
        }
        if (vibrateEnabled && isChargingFeedbackEnabled(userId)) {
            this.mVibrator.vibrate(WIRELESS_CHARGING_VIBRATION_EFFECT, VIBRATION_ATTRIBUTES);
        }
    }

    private boolean isChargingFeedbackEnabled(int userId) {
        return (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "charging_sounds_enabled", 1, userId) != 0) && (Settings.Global.getInt(this.mContext.getContentResolver(), "zen_mode", 1) == 0);
    }

    /* access modifiers changed from: private */
    public final class NotifierHandler extends Handler {
        public NotifierHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 100) {
                switch (i) {
                    case 1:
                        Notifier.this.sendUserActivity();
                        return;
                    case 2:
                        Notifier.this.sendNextBroadcast();
                        return;
                    case 3:
                        Notifier.this.showWirelessChargingStarted(msg.arg1, msg.arg2);
                        return;
                    case 4:
                        Notifier.this.sendBrightnessBoostChangedBroadcast();
                        return;
                    case 5:
                        Notifier.this.lockProfile(msg.arg1);
                        return;
                    case 6:
                        Notifier.this.showWiredChargingStarted(msg.arg1);
                        return;
                    default:
                        return;
                }
            } else {
                Notifier.this.resumeSystemBrightness();
            }
        }
    }
}

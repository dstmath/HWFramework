package com.android.server.power;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.RetailDemoModeServiceInternal;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManagerInternal;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.metrics.LogMaker;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManagerPolicy;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.logging.MetricsLogger;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.pg.PGManagerInternal;
import com.huawei.pgmng.common.Utils;

final class Notifier {
    private static boolean DEBUG = false;
    private static final int INTERACTIVE_STATE_ASLEEP = 2;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_UNKNOWN = 0;
    private static final int MSG_BROADCAST = 2;
    private static final int MSG_RESUME_SYSTEM_BRIGHTNESS = 5;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED = 4;
    private static final int MSG_USER_ACTIVITY = 1;
    private static final int MSG_WIRELESS_CHARGING_STARTED = 3;
    private static final String TAG = "PowerManagerNotifier";
    private final ActivityManagerInternal mActivityManagerInternal;
    private final IAppOpsService mAppOps;
    private final IBatteryStats mBatteryStats;
    private boolean mBrightnessModeChangeNoClearOffset = false;
    private boolean mBroadcastInProgress;
    private long mBroadcastStartTime;
    private int mBroadcastedInteractiveState;
    private final Context mContext;
    private final BroadcastReceiver mGoToSleepBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, new Object[]{Integer.valueOf(0), Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), Integer.valueOf(1)});
            Notifier.this.sendNextBroadcast();
        }
    };
    private final NotifierHandler mHandler;
    private final InputManagerInternal mInputManagerInternal;
    private final InputMethodManagerInternal mInputMethodManagerInternal;
    private boolean mInteractive = true;
    private int mInteractiveChangeReason;
    private boolean mInteractiveChanging;
    private final Object mLock = new Object();
    private PGManagerInternal mPGManagerInternal;
    private boolean mPendingGoToSleepBroadcast;
    private int mPendingInteractiveState;
    private boolean mPendingWakeUpBroadcast;
    private final WindowManagerPolicy mPolicy;
    private final RetailDemoModeServiceInternal mRetailDemoModeServiceInternal;
    private final BroadcastReceiver mScreeBrightnessBoostChangedDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Notifier.this.mSuspendBlocker.release();
        }
    };
    private final Intent mScreenBrightnessBoostIntent;
    private final Intent mScreenOffIntent;
    private final Intent mScreenOnIntent;
    private final SuspendBlocker mSuspendBlocker;
    private final boolean mSuspendWhenScreenOffDueToProximityConfig;
    private boolean mUserActivityPending;
    private final BroadcastReceiver mWakeUpBroadcastDone = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, new Object[]{Integer.valueOf(1), Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime), Integer.valueOf(1)});
            Notifier.this.sendNextBroadcast();
        }
    };

    private final class NotifierHandler extends Handler {
        public NotifierHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Notifier.this.sendUserActivity();
                    return;
                case 2:
                    Notifier.this.sendNextBroadcast();
                    return;
                case 3:
                    Notifier.this.playWirelessChargingStartedSound();
                    return;
                case 4:
                    Notifier.this.sendBrightnessBoostChangedBroadcast();
                    return;
                case 5:
                    Notifier.this.resumeSystemBrightness();
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public Notifier(Looper looper, Context context, IBatteryStats batteryStats, IAppOpsService appOps, SuspendBlocker suspendBlocker, WindowManagerPolicy policy) {
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mAppOps = appOps;
        this.mSuspendBlocker = suspendBlocker;
        this.mPolicy = policy;
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        this.mRetailDemoModeServiceInternal = (RetailDemoModeServiceInternal) LocalServices.getService(RetailDemoModeServiceInternal.class);
        this.mHandler = new NotifierHandler(looper);
        this.mScreenOnIntent = new Intent("android.intent.action.SCREEN_ON");
        this.mScreenOnIntent.addFlags(1344274432);
        this.mScreenOffIntent = new Intent("android.intent.action.SCREEN_OFF");
        this.mScreenOffIntent.addFlags(1344274432);
        this.mScreenBrightnessBoostIntent = new Intent("android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED");
        this.mScreenBrightnessBoostIntent.addFlags(1342177280);
        this.mSuspendWhenScreenOffDueToProximityConfig = context.getResources().getBoolean(17957027);
        try {
            this.mBatteryStats.noteInteractive(true);
        } catch (RemoteException e) {
        }
    }

    public void onWakeLockAcquired(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        Utils.noteWakelock(flags, tag, ownerUid, ownerPid, workSource, 160);
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        if (monitorType >= 0) {
            boolean unimportantForLogging = ownerUid == 1000 ? (1073741824 & flags) != 0 : false;
            if (workSource != null) {
                try {
                    this.mBatteryStats.noteStartWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                } catch (RemoteException e) {
                    return;
                }
            } else {
                this.mBatteryStats.noteStartWakelock(ownerUid, ownerPid, tag, historyTag, monitorType, unimportantForLogging);
                this.mAppOps.startOperation(AppOpsManager.getToken(this.mAppOps), 40, ownerUid, packageName);
            }
            if (monitorType == 0) {
                if (this.mPGManagerInternal == null) {
                    this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
                }
                if (this.mPGManagerInternal != null) {
                    this.mPGManagerInternal.noteStartWakeLock(tag, workSource, packageName, ownerUid);
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
                int N = workSource.size();
                for (int i = 0; i < N; i++) {
                    this.mBatteryStats.noteLongPartialWakelockStart(tag, historyTag, workSource.get(i));
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mBatteryStats.noteLongPartialWakelockStart(tag, historyTag, ownerUid);
    }

    public void onLongPartialWakeLockFinish(String tag, int ownerUid, WorkSource workSource, String historyTag) {
        if (DEBUG) {
            Slog.d(TAG, "onLongPartialWakeLockFinish: ownerUid=" + ownerUid + ", workSource=" + workSource);
        }
        if (workSource != null) {
            try {
                int N = workSource.size();
                for (int i = 0; i < N; i++) {
                    this.mBatteryStats.noteLongPartialWakelockFinish(tag, historyTag, workSource.get(i));
                }
                return;
            } catch (RemoteException e) {
                return;
            }
        }
        this.mBatteryStats.noteLongPartialWakelockFinish(tag, historyTag, ownerUid);
    }

    public void onWakeLockChanging(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag, int newFlags, String newTag, String newPackageName, int newOwnerUid, int newOwnerPid, WorkSource newWorkSource, String newHistoryTag) {
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        int newMonitorType = getBatteryStatsWakeLockMonitorType(newFlags);
        if (workSource == null || newWorkSource == null || monitorType < 0 || newMonitorType < 0) {
            onWakeLockReleased(flags, tag, packageName, ownerUid, ownerPid, workSource, historyTag);
            onWakeLockAcquired(newFlags, newTag, newPackageName, newOwnerUid, newOwnerPid, newWorkSource, newHistoryTag);
            return;
        }
        Utils.noteWakelock(flags, tag, ownerUid, ownerPid, workSource, newWorkSource);
        if (DEBUG) {
            Slog.d(TAG, "onWakeLockChanging: flags=" + newFlags + ", tag=\"" + newTag + "\", packageName=" + newPackageName + ", ownerUid=" + newOwnerUid + ", ownerPid=" + newOwnerPid + ", workSource=" + newWorkSource);
        }
        boolean unimportantForLogging = newOwnerUid == 1000 ? (1073741824 & newFlags) != 0 : false;
        try {
            this.mBatteryStats.noteChangeWakelockFromSource(workSource, ownerPid, tag, historyTag, monitorType, newWorkSource, newOwnerPid, newTag, newHistoryTag, newMonitorType, unimportantForLogging);
            if (monitorType == 0) {
                if (this.mPGManagerInternal == null) {
                    this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
                }
                if (this.mPGManagerInternal != null) {
                    this.mPGManagerInternal.noteChangeWakeLock(tag, workSource, packageName, ownerUid, newTag, newWorkSource, newPackageName, newOwnerUid);
                }
            }
        } catch (RemoteException e) {
        }
    }

    public void onWakeLockReleased(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        Utils.noteWakelock(flags, tag, ownerUid, ownerPid, workSource, 161);
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
                this.mAppOps.finishOperation(AppOpsManager.getToken(this.mAppOps), 40, ownerUid, packageName);
            }
            if (monitorType == 0) {
                if (this.mPGManagerInternal == null) {
                    this.mPGManagerInternal = (PGManagerInternal) LocalServices.getService(PGManagerInternal.class);
                }
                if (this.mPGManagerInternal != null) {
                    this.mPGManagerInternal.noteStopWakeLock(tag, workSource, packageName, ownerUid);
                }
            }
        }
    }

    private int getBatteryStatsWakeLockMonitorType(int flags) {
        switch (NetworkConstants.ARP_HWTYPE_RESERVED_HI & flags) {
            case 1:
                return 0;
            case 6:
            case 10:
                return 1;
            case 32:
                return this.mSuspendWhenScreenOffDueToProximityConfig ? -1 : 0;
            case 64:
                return -1;
            case 128:
                return 18;
            default:
                return -1;
        }
    }

    public void onWakefulnessChangeStarted(final int wakefulness, int reason) {
        boolean interactive = PowerManagerInternal.isInteractive(wakefulness);
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier onWakefulnessChangeStarted: wakefulness=" + wakefulness + ", reason=" + reason + ", interactive=" + interactive + ", mInteractive=" + this.mInteractive + ", mInteractiveChanging=" + this.mInteractiveChanging);
        this.mHandler.post(new Runnable() {
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
            this.mInteractive = interactive;
            this.mInteractiveChangeReason = reason;
            this.mInteractiveChanging = true;
            handleEarlyInteractiveChange();
        }
    }

    public void onWakefulnessChangeFinished() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier onWakefulnessChangeFinished mInteractiveChanging=" + this.mInteractiveChanging + ", mInteractive=" + this.mInteractive);
        if (this.mInteractiveChanging) {
            this.mInteractiveChanging = false;
            handleLateInteractiveChange();
            if (Jlog.isPerfTest()) {
                Jlog.i(2207, "JL_PWRSCRON_NOTIFIER_WAKEFINISH");
            }
            if (this.mInteractive) {
                Jlog.d(8, "JL_PMS_WAKEUP_FINISHED");
            }
        }
    }

    private void handleEarlyInteractiveChange() {
        synchronized (this.mLock) {
            if (this.mInteractive) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Notifier.this.mPolicy.startedWakingUp();
                    }
                });
                this.mPendingInteractiveState = 1;
                this.mPendingWakeUpBroadcast = true;
                updatePendingBroadcastLocked();
            } else {
                final int why = translateOffReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Notifier.this.mPolicy.startedGoingToSleep(why);
                    }
                });
            }
        }
    }

    private void handleLateInteractiveChange() {
        synchronized (this.mLock) {
            if (this.mInteractive) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        Notifier.this.mPolicy.finishedWakingUp();
                    }
                });
            } else {
                if (this.mUserActivityPending) {
                    this.mUserActivityPending = false;
                    this.mHandler.removeMessages(1);
                }
                final int why = translateOffReason(this.mInteractiveChangeReason);
                this.mHandler.post(new Runnable() {
                    public void run() {
                        LogMaker log = new LogMaker(198);
                        log.setType(2);
                        log.setSubtype(why);
                        MetricsLogger.action(log);
                        EventLogTags.writePowerScreenState(0, why, 0, 0, 0);
                        Notifier.this.mPolicy.finishedGoingToSleep(why);
                    }
                });
                this.mPendingInteractiveState = 2;
                this.mPendingGoToSleepBroadcast = true;
                updatePendingBroadcastLocked();
            }
        }
    }

    private static int translateOffReason(int reason) {
        switch (reason) {
            case 1:
                return 1;
            case 2:
                return 3;
            case 7:
                return 6;
            case 9:
                return 7;
            default:
                return 2;
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
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier onUserActivity: event=" + event + ", uid=" + uid);
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

    public void onWakeUp(String reason, int reasonUid, String opPackageName, int opUid) {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier onWakeUp: event=" + reason + ", reasonUid=" + reasonUid + " opPackageName=" + opPackageName + " opUid=" + opUid);
        try {
            this.mBatteryStats.noteWakeUp(reason, reasonUid);
            if (opPackageName != null) {
                this.mAppOps.noteOperation(61, opUid, opPackageName);
            }
        } catch (RemoteException e) {
        }
    }

    public void onWirelessChargingStarted() {
        if (DEBUG) {
            Slog.d(TAG, "onWirelessChargingStarted");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(3);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    private void updatePendingBroadcastLocked() {
        if (!this.mBroadcastInProgress && this.mPendingInteractiveState != 0) {
            if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState != this.mBroadcastedInteractiveState) {
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

    /* JADX WARNING: Missing block: B:12:0x000f, code:
            if (r2.mRetailDemoModeServiceInternal == null) goto L_0x0016;
     */
    /* JADX WARNING: Missing block: B:13:0x0011, code:
            r2.mRetailDemoModeServiceInternal.onUserActivity();
     */
    /* JADX WARNING: Missing block: B:14:0x0016, code:
            r2.mPolicy.userActivity();
     */
    /* JADX WARNING: Missing block: B:15:0x001b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendUserActivity() {
        synchronized (this.mLock) {
            if (this.mUserActivityPending) {
                this.mUserActivityPending = false;
            }
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0018, code:
            android.util.EventLog.writeEvent(com.android.server.EventLogTags.POWER_SCREEN_BROADCAST_SEND, 1);
     */
    /* JADX WARNING: Missing block: B:9:0x001d, code:
            if (r0 != 1) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:10:0x001f, code:
            sendWakeUpBroadcast();
     */
    /* JADX WARNING: Missing block: B:11:0x0022, code:
            return;
     */
    /* JADX WARNING: Missing block: B:40:0x005a, code:
            sendGoToSleepBroadcast();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendNextBroadcast() {
        synchronized (this.mLock) {
            if (this.mBroadcastedInteractiveState == 0) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
            } else if (this.mBroadcastedInteractiveState == 1) {
                if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState == 2) {
                    this.mPendingGoToSleepBroadcast = false;
                    this.mBroadcastedInteractiveState = 2;
                } else {
                    finishPendingBroadcastLocked();
                    return;
                }
            } else if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState == 1) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = 1;
            } else {
                finishPendingBroadcastLocked();
                return;
            }
            this.mBroadcastStartTime = SystemClock.uptimeMillis();
            int powerState = this.mBroadcastedInteractiveState;
        }
    }

    private void sendBrightnessBoostChangedBroadcast() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending brightness boost changed broadcast.");
        this.mContext.sendOrderedBroadcastAsUser(this.mScreenBrightnessBoostIntent, UserHandle.ALL, null, this.mScreeBrightnessBoostChangedDone, this.mHandler, 0, null, null);
    }

    private void sendWakeUpBroadcast() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending wake up broadcast.");
        if (this.mActivityManagerInternal.isSystemReady()) {
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOnIntent, UserHandle.ALL, null, this.mWakeUpBroadcastDone, this.mHandler, 0, null, null);
            return;
        }
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, new Object[]{Integer.valueOf(2), Integer.valueOf(1)});
        sendNextBroadcast();
    }

    private void resumeSystemBrightness() {
        ContentResolver cr = this.mContext.getContentResolver();
        int userHandle = ActivityManager.getCurrentUser();
        String mode = System.getStringForUser(cr, "hw_screen_brightness_mode_value", userHandle);
        String modeCurrent = System.getStringForUser(cr, "screen_brightness_mode", userHandle);
        if (mode != null) {
            System.putStringForUser(cr, "screen_brightness_mode", mode, userHandle);
            if (mode.equals("0")) {
                String lastBrightness = System.getStringForUser(cr, "hw_screen_temp_brightness", userHandle);
                if (lastBrightness != null) {
                    System.putStringForUser(cr, "screen_brightness", lastBrightness, userHandle);
                }
            }
        }
        if (modeCurrent != null && mode != null) {
            if (modeCurrent.equals("0") && mode.equals("1")) {
                System.putStringForUser(cr, "screen_auto_brightness", "0", userHandle);
                if (DEBUG) {
                    Slog.d(TAG, "manul2auto set mBrightnessModeChangeNoClearOffset=" + this.mBrightnessModeChangeNoClearOffset);
                }
                this.mBrightnessModeChangeNoClearOffset = true;
                return;
            }
            this.mBrightnessModeChangeNoClearOffset = false;
        }
    }

    public boolean getBrightnessModeChangeNoClearOffset() {
        return this.mBrightnessModeChangeNoClearOffset;
    }

    public void setBrightnessModeChangeNoClearOffset(boolean enable) {
        this.mBrightnessModeChangeNoClearOffset = enable;
    }

    private void sendGoToSleepBroadcast() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending go to sleep broadcast.");
        if (this.mActivityManagerInternal.isSystemReady()) {
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOffIntent, UserHandle.ALL, null, this.mGoToSleepBroadcastDone, this.mHandler, 0, null, null);
            Message msg = this.mHandler.obtainMessage(5);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
            return;
        }
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, new Object[]{Integer.valueOf(3), Integer.valueOf(1)});
        sendNextBroadcast();
    }

    private void playWirelessChargingStartedSound() {
        boolean enabled = Global.getInt(this.mContext.getContentResolver(), "charging_sounds_enabled", 1) != 0;
        String soundPath = Global.getString(this.mContext.getContentResolver(), "wireless_charging_started_sound");
        if (enabled && soundPath != null) {
            Uri soundUri = Uri.parse("file://" + soundPath);
            if (soundUri != null) {
                Ringtone sfx = RingtoneManager.getRingtone(this.mContext, soundUri);
                if (sfx != null) {
                    sfx.setStreamType(1);
                    sfx.play();
                }
            }
        }
        this.mSuspendBlocker.release();
    }
}

package com.android.server.power;

import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputManagerInternal;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Slog;
import android.view.WindowManagerPolicy;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.am.ProcessList;
import com.android.server.pg.PGManagerInternal;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService.H;
import com.huawei.pgmng.common.Utils;

final class Notifier {
    private static boolean DEBUG = false;
    private static final int INTERACTIVE_STATE_ASLEEP = 2;
    private static final int INTERACTIVE_STATE_AWAKE = 1;
    private static final int INTERACTIVE_STATE_UNKNOWN = 0;
    private static final int MSG_BROADCAST = 2;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED = 4;
    private static final int MSG_USER_ACTIVITY = 1;
    private static final int MSG_WIRELESS_CHARGING_STARTED = 3;
    private static final String TAG = "PowerManagerNotifier";
    private final ActivityManagerInternal mActivityManagerInternal;
    private final IAppOpsService mAppOps;
    private final IBatteryStats mBatteryStats;
    private boolean mBroadcastInProgress;
    private long mBroadcastStartTime;
    private int mBroadcastedInteractiveState;
    private final Context mContext;
    private final BroadcastReceiver mGoToSleepBroadcastDone;
    private final NotifierHandler mHandler;
    private final InputManagerInternal mInputManagerInternal;
    private final InputMethodManagerInternal mInputMethodManagerInternal;
    private boolean mInteractive;
    private int mInteractiveChangeReason;
    private boolean mInteractiveChanging;
    private final Object mLock;
    private PGManagerInternal mPGManagerInternal;
    private boolean mPendingGoToSleepBroadcast;
    private int mPendingInteractiveState;
    private boolean mPendingWakeUpBroadcast;
    private final WindowManagerPolicy mPolicy;
    private final BroadcastReceiver mScreeBrightnessBoostChangedDone;
    private final Intent mScreenBrightnessBoostIntent;
    private final Intent mScreenOffIntent;
    private final Intent mScreenOnIntent;
    private final SuspendBlocker mSuspendBlocker;
    private final boolean mSuspendWhenScreenOffDueToProximityConfig;
    private boolean mUserActivityPending;
    private final BroadcastReceiver mWakeUpBroadcastDone;

    /* renamed from: com.android.server.power.Notifier.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ int val$wakefulness;

        AnonymousClass4(int val$wakefulness) {
            this.val$wakefulness = val$wakefulness;
        }

        public void run() {
            Notifier.this.mActivityManagerInternal.onWakefulnessChanged(this.val$wakefulness);
        }
    }

    /* renamed from: com.android.server.power.Notifier.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ int val$why;

        AnonymousClass6(int val$why) {
            this.val$why = val$why;
        }

        public void run() {
            Notifier.this.mPolicy.startedGoingToSleep(this.val$why);
        }
    }

    /* renamed from: com.android.server.power.Notifier.8 */
    class AnonymousClass8 implements Runnable {
        final /* synthetic */ int val$why;

        AnonymousClass8(int val$why) {
            this.val$why = val$why;
        }

        public void run() {
            Object[] objArr = new Object[Notifier.MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED];
            objArr[Notifier.INTERACTIVE_STATE_UNKNOWN] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
            objArr[Notifier.MSG_USER_ACTIVITY] = Integer.valueOf(this.val$why);
            objArr[Notifier.MSG_BROADCAST] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
            objArr[Notifier.MSG_WIRELESS_CHARGING_STARTED] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_STATE, objArr);
            Notifier.this.mPolicy.finishedGoingToSleep(this.val$why);
        }
    }

    private final class NotifierHandler extends Handler {
        public NotifierHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Notifier.MSG_USER_ACTIVITY /*1*/:
                    Notifier.this.sendUserActivity();
                case Notifier.MSG_BROADCAST /*2*/:
                    Notifier.this.sendNextBroadcast();
                case Notifier.MSG_WIRELESS_CHARGING_STARTED /*3*/:
                    Notifier.this.playWirelessChargingStartedSound();
                case Notifier.MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED /*4*/:
                    Notifier.this.sendBrightnessBoostChangedBroadcast();
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.power.Notifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.power.Notifier.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.Notifier.<clinit>():void");
    }

    public Notifier(Looper looper, Context context, IBatteryStats batteryStats, IAppOpsService appOps, SuspendBlocker suspendBlocker, WindowManagerPolicy policy) {
        this.mLock = new Object();
        this.mInteractive = true;
        this.mScreeBrightnessBoostChangedDone = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Notifier.this.mSuspendBlocker.release();
            }
        };
        this.mWakeUpBroadcastDone = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Object[] objArr = new Object[Notifier.MSG_WIRELESS_CHARGING_STARTED];
                objArr[Notifier.INTERACTIVE_STATE_UNKNOWN] = Integer.valueOf(Notifier.MSG_USER_ACTIVITY);
                objArr[Notifier.MSG_USER_ACTIVITY] = Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime);
                objArr[Notifier.MSG_BROADCAST] = Integer.valueOf(Notifier.MSG_USER_ACTIVITY);
                EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, objArr);
                Notifier.this.sendNextBroadcast();
            }
        };
        this.mGoToSleepBroadcastDone = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Object[] objArr = new Object[Notifier.MSG_WIRELESS_CHARGING_STARTED];
                objArr[Notifier.INTERACTIVE_STATE_UNKNOWN] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
                objArr[Notifier.MSG_USER_ACTIVITY] = Long.valueOf(SystemClock.uptimeMillis() - Notifier.this.mBroadcastStartTime);
                objArr[Notifier.MSG_BROADCAST] = Integer.valueOf(Notifier.MSG_USER_ACTIVITY);
                EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_DONE, objArr);
                Notifier.this.sendNextBroadcast();
            }
        };
        this.mContext = context;
        this.mBatteryStats = batteryStats;
        this.mAppOps = appOps;
        this.mSuspendBlocker = suspendBlocker;
        this.mPolicy = policy;
        this.mActivityManagerInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
        this.mInputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
        this.mHandler = new NotifierHandler(looper);
        this.mScreenOnIntent = new Intent("android.intent.action.SCREEN_ON");
        this.mScreenOnIntent.addFlags(1342177280);
        this.mScreenOffIntent = new Intent("android.intent.action.SCREEN_OFF");
        this.mScreenOffIntent.addFlags(1342177280);
        this.mScreenBrightnessBoostIntent = new Intent("android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED");
        this.mScreenBrightnessBoostIntent.addFlags(1342177280);
        this.mSuspendWhenScreenOffDueToProximityConfig = context.getResources().getBoolean(17956929);
        try {
            this.mBatteryStats.noteInteractive(true);
        } catch (RemoteException e) {
        }
    }

    public void onWakeLockAcquired(int flags, String tag, String packageName, int ownerUid, int ownerPid, WorkSource workSource, String historyTag) {
        Utils.noteWakelock(flags, tag, ownerUid, ownerPid, workSource, HdmiCecKeycode.UI_SOUND_PRESENTATION_SELECT_AUDIO_AUTO_EQUALIZER);
        int monitorType = getBatteryStatsWakeLockMonitorType(flags);
        if (monitorType >= 0) {
            boolean unimportantForLogging = ownerUid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE ? (1073741824 & flags) != 0 : false;
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
        switch (65535 & flags) {
            case MSG_USER_ACTIVITY /*1*/:
                return INTERACTIVE_STATE_UNKNOWN;
            case H.REMOVE_STARTING /*6*/:
            case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                return MSG_USER_ACTIVITY;
            case H.NOTIFY_ACTIVITY_DRAWN /*32*/:
                return this.mSuspendWhenScreenOffDueToProximityConfig ? -1 : INTERACTIVE_STATE_UNKNOWN;
            case DumpState.DUMP_PERMISSIONS /*64*/:
                return -1;
            case DumpState.DUMP_PACKAGES /*128*/:
                return 18;
            default:
                return -1;
        }
    }

    public void onWakefulnessChangeStarted(int wakefulness, int reason) {
        boolean interactive = PowerManagerInternal.isInteractive(wakefulness);
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier onWakefulnessChangeStarted: wakefulness=" + wakefulness + ", reason=" + reason + ", interactive=" + interactive + ", mInteractive=" + this.mInteractive + ", mInteractiveChanging=" + this.mInteractiveChanging);
        this.mHandler.post(new AnonymousClass4(wakefulness));
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
                        Object[] objArr = new Object[Notifier.MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED];
                        objArr[Notifier.INTERACTIVE_STATE_UNKNOWN] = Integer.valueOf(Notifier.MSG_USER_ACTIVITY);
                        objArr[Notifier.MSG_USER_ACTIVITY] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
                        objArr[Notifier.MSG_BROADCAST] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
                        objArr[Notifier.MSG_WIRELESS_CHARGING_STARTED] = Integer.valueOf(Notifier.INTERACTIVE_STATE_UNKNOWN);
                        EventLog.writeEvent(EventLogTags.POWER_SCREEN_STATE, objArr);
                        Notifier.this.mPolicy.startedWakingUp();
                    }
                });
                this.mPendingInteractiveState = MSG_USER_ACTIVITY;
                this.mPendingWakeUpBroadcast = true;
                updatePendingBroadcastLocked();
            } else {
                this.mHandler.post(new AnonymousClass6(translateOffReason(this.mInteractiveChangeReason)));
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
                    this.mHandler.removeMessages(MSG_USER_ACTIVITY);
                }
                this.mHandler.post(new AnonymousClass8(translateOffReason(this.mInteractiveChangeReason)));
                this.mPendingInteractiveState = MSG_BROADCAST;
                this.mPendingGoToSleepBroadcast = true;
                updatePendingBroadcastLocked();
            }
        }
    }

    private static int translateOffReason(int reason) {
        switch (reason) {
            case MSG_USER_ACTIVITY /*1*/:
                return MSG_USER_ACTIVITY;
            case MSG_BROADCAST /*2*/:
                return MSG_WIRELESS_CHARGING_STARTED;
            case H.FINISHED_STARTING /*7*/:
                return 6;
            case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                return 7;
            default:
                return MSG_BROADCAST;
        }
    }

    public void onScreenBrightnessBoostChanged() {
        if (DEBUG) {
            Slog.d(TAG, "onScreenBrightnessBoostChanged");
        }
        this.mSuspendBlocker.acquire();
        Message msg = this.mHandler.obtainMessage(MSG_SCREEN_BRIGHTNESS_BOOST_CHANGED);
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
                Message msg = this.mHandler.obtainMessage(MSG_USER_ACTIVITY);
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
        Message msg = this.mHandler.obtainMessage(MSG_WIRELESS_CHARGING_STARTED);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
    }

    private void updatePendingBroadcastLocked() {
        if (!this.mBroadcastInProgress && this.mPendingInteractiveState != 0) {
            if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState != this.mBroadcastedInteractiveState) {
                this.mBroadcastInProgress = true;
                this.mSuspendBlocker.acquire();
                Message msg = this.mHandler.obtainMessage(MSG_BROADCAST);
                msg.setAsynchronous(true);
                this.mHandler.sendMessage(msg);
            }
        }
    }

    private void finishPendingBroadcastLocked() {
        this.mBroadcastInProgress = false;
        this.mSuspendBlocker.release();
    }

    private void sendUserActivity() {
        synchronized (this.mLock) {
            if (this.mUserActivityPending) {
                this.mUserActivityPending = false;
                this.mPolicy.userActivity();
                return;
            }
        }
    }

    private void sendNextBroadcast() {
        synchronized (this.mLock) {
            if (this.mBroadcastedInteractiveState == 0) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = MSG_USER_ACTIVITY;
            } else if (this.mBroadcastedInteractiveState == MSG_USER_ACTIVITY) {
                if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState == MSG_BROADCAST) {
                    this.mPendingGoToSleepBroadcast = false;
                    this.mBroadcastedInteractiveState = MSG_BROADCAST;
                } else {
                    finishPendingBroadcastLocked();
                    return;
                }
            } else if (this.mPendingWakeUpBroadcast || this.mPendingGoToSleepBroadcast || this.mPendingInteractiveState == MSG_USER_ACTIVITY) {
                this.mPendingWakeUpBroadcast = false;
                this.mBroadcastedInteractiveState = MSG_USER_ACTIVITY;
            } else {
                finishPendingBroadcastLocked();
                return;
            }
            this.mBroadcastStartTime = SystemClock.uptimeMillis();
            int powerState = this.mBroadcastedInteractiveState;
            EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_SEND, MSG_USER_ACTIVITY);
            if (powerState == MSG_USER_ACTIVITY) {
                sendWakeUpBroadcast();
            } else {
                sendGoToSleepBroadcast();
            }
        }
    }

    private void sendBrightnessBoostChangedBroadcast() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending brightness boost changed broadcast.");
        this.mContext.sendOrderedBroadcastAsUser(this.mScreenBrightnessBoostIntent, UserHandle.ALL, null, this.mScreeBrightnessBoostChangedDone, this.mHandler, INTERACTIVE_STATE_UNKNOWN, null, null);
    }

    private void sendWakeUpBroadcast() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending wake up broadcast.");
        if (ActivityManagerNative.isSystemReady()) {
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOnIntent, UserHandle.ALL, null, this.mWakeUpBroadcastDone, this.mHandler, INTERACTIVE_STATE_UNKNOWN, null, null);
            return;
        }
        Object[] objArr = new Object[MSG_BROADCAST];
        objArr[INTERACTIVE_STATE_UNKNOWN] = Integer.valueOf(MSG_BROADCAST);
        objArr[MSG_USER_ACTIVITY] = Integer.valueOf(MSG_USER_ACTIVITY);
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, objArr);
        sendNextBroadcast();
    }

    private void sendGoToSleepBroadcast() {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerNotifier Sending go to sleep broadcast.");
        if (ActivityManagerNative.isSystemReady()) {
            this.mContext.sendOrderedBroadcastAsUser(this.mScreenOffIntent, UserHandle.ALL, null, this.mGoToSleepBroadcastDone, this.mHandler, INTERACTIVE_STATE_UNKNOWN, null, null);
            return;
        }
        Object[] objArr = new Object[MSG_BROADCAST];
        objArr[INTERACTIVE_STATE_UNKNOWN] = Integer.valueOf(MSG_WIRELESS_CHARGING_STARTED);
        objArr[MSG_USER_ACTIVITY] = Integer.valueOf(MSG_USER_ACTIVITY);
        EventLog.writeEvent(EventLogTags.POWER_SCREEN_BROADCAST_STOP, objArr);
        sendNextBroadcast();
    }

    private void playWirelessChargingStartedSound() {
        boolean enabled = Global.getInt(this.mContext.getContentResolver(), "charging_sounds_enabled", MSG_USER_ACTIVITY) != 0;
        String soundPath = Global.getString(this.mContext.getContentResolver(), "wireless_charging_started_sound");
        if (enabled && soundPath != null) {
            Uri soundUri = Uri.parse("file://" + soundPath);
            if (soundUri != null) {
                Ringtone sfx = RingtoneManager.getRingtone(this.mContext, soundUri);
                if (sfx != null) {
                    sfx.setStreamType(MSG_USER_ACTIVITY);
                    sfx.play();
                }
            }
        }
        this.mSuspendBlocker.release();
    }
}

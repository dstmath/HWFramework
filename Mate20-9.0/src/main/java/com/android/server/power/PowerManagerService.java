package com.android.server.power;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.SynchronousUserSwitchObserver;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.hardware.SystemSensorManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManagerInternal;
import android.iawareperf.UniPerf;
import android.metrics.LogMaker;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.BatteryManager;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IAodStateCallback;
import android.os.IBinder;
import android.os.IHwBrightnessCallback;
import android.os.IPowerManager;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.pc.IHwPCManager;
import android.provider.Settings;
import android.service.dreams.DreamManagerInternal;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.PrintWriterPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.zrhung.IZrHung;
import android.zrhung.ZrHungData;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceExFactory;
import com.android.server.LockGuard;
import com.android.server.NsdService;
import com.android.server.ServiceThread;
import com.android.server.Watchdog;
import com.android.server.am.BatteryStatsService;
import com.android.server.backup.BackupAgentTimeoutParameters;
import com.android.server.job.controllers.JobStatus;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.pm.PackageManagerService;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.power.batterysaver.BatterySaverController;
import com.android.server.power.batterysaver.BatterySaverStateMachine;
import com.android.server.power.batterysaver.BatterySavingStats;
import com.android.server.utils.PriorityDump;
import com.huawei.android.os.IHwPowerDAMonitorCallback;
import com.huawei.android.os.IHwPowerManager;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PowerManagerService extends AbsPowerManagerService implements Watchdog.Monitor, IHwPowerManagerInner {
    private static final String AOD_MODE_CMD = "/sys/class/graphics/fb0/alpm_setting";
    private static final String AOD_STATE_CMD = "/sys/class/graphics/fb0/alpm_function";
    private static final int BACK_SENSOR_COVER_MODE_BEIGHTNESS = -3;
    private static final String CHARGE_LIMIT_SCENE = "iin";
    protected static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean DEBUG_ALL;
    /* access modifiers changed from: private */
    public static boolean DEBUG_Controller = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    protected static final boolean DEBUG_SPEW = DEBUG;
    private static final int DEFAULT_DOUBLE_TAP_TO_WAKE = 0;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int DEFAULT_SLEEP_TIMEOUT = -1;
    private static final int DIRTY_ACTUAL_DISPLAY_POWER_STATE_UPDATED = 8;
    private static final int DIRTY_BATTERY_STATE = 256;
    private static final int DIRTY_BOOT_COMPLETED = 16;
    private static final int DIRTY_DOCK_STATE = 1024;
    private static final int DIRTY_IS_POWERED = 64;
    private static final int DIRTY_PROXIMITY_POSITIVE = 512;
    private static final int DIRTY_QUIESCENT = 4096;
    private static final int DIRTY_SCREEN_BRIGHTNESS_BOOST = 2048;
    private static final int DIRTY_SETTINGS = 32;
    private static final int DIRTY_STAY_ON = 128;
    private static final int DIRTY_USER_ACTIVITY = 4;
    private static final int DIRTY_VR_MODE_CHANGED = 8192;
    protected static final int DIRTY_WAIT_BRIGHT_MODE = 16384;
    protected static final int DIRTY_WAKEFULNESS = 2;
    protected static final int DIRTY_WAKE_LOCKS = 1;
    private static final int EYE_PROTECTIION_OFF = 0;
    private static final int EYE_PROTECTIION_ON = 1;
    private static final int EYE_PROTECTIION_ON_BY_USER = 3;
    private static final int HALT_MODE_REBOOT = 1;
    private static final int HALT_MODE_REBOOT_SAFE_MODE = 2;
    private static final int HALT_MODE_SHUTDOWN = 0;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final String LAST_REBOOT_PROPERTY = "persist.sys.boot.reason";
    private static final HashSet<String> LOG_DROP_SET = new HashSet<String>() {
        {
            add("RILJ1001");
            add("LocationManagerService1000");
            add("*alarm*1000");
            add("*dexopt*1000");
            add("bluetooth_timer1002");
            add("GnssLocationProvider1000");
        }
    };
    static final long MIN_LONG_WAKE_CHECK_INTERVAL = 60000;
    private static final long MIN_TIME_FACE_DETECT_BEFORE_DIM = 1000;
    private static final int MSG_CHECK_FOR_LONG_WAKELOCKS = 4;
    private static final int MSG_CUSTOM_USER_ACTIVITY_TIMEOUT = 105;
    private static final int MSG_FACE_DETECT_BEFORE_DIM = 103;
    private static final int MSG_PROXIMITY_POSITIVE = 5;
    private static final int MSG_RECORD_WAKEUP = 104;
    private static final int MSG_SANDMAN = 2;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 3;
    private static final int MSG_USER_ACTIVITY_TIMEOUT = 1;
    protected static final int MSG_WAIT_BRIGHT_TIMEOUT = 101;
    private static final String POWER_CHARGE_SCENE = "chrg_enable";
    private static final int POWER_FEATURE_DOUBLE_TAP_TO_WAKE = 1;
    private static final String POWER_STATE_CHARGE_DISABLE = "0";
    private static final String POWER_STATE_CHARGE_ENABLE = "1";
    private static final String REASON_BATTERY_THERMAL_STATE = "shutdown,thermal,battery";
    private static final String REASON_LOW_BATTERY = "shutdown,battery";
    private static final String REASON_REBOOT = "reboot";
    private static final String REASON_SHUTDOWN = "shutdown";
    private static final String REASON_THERMAL_SHUTDOWN = "shutdown,thermal";
    private static final String REASON_USERREQUESTED = "shutdown,userrequested";
    private static final int SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 5000;
    private static final int SCREEN_ON_LATENCY_WARNING_MS = 200;
    private static final String SYSTEM_PROPERTY_QUIESCENT = "ro.boot.quiescent";
    private static final String SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED = "sys.retaildemo.enabled";
    private static final String TAG = "PowerManagerService";
    private static final String TAG_PowerMS = "PowerMS";
    private static final String TRACE_SCREEN_ON = "Screen turning on";
    private static final int USER_ACTIVITY_SCREEN_BRIGHT = 1;
    private static final int USER_ACTIVITY_SCREEN_DIM = 2;
    private static final int USER_ACTIVITY_SCREEN_DREAM = 4;
    private static final String WAKEUP_REASON = "WakeUpReason";
    private static final int WAKE_LOCK_BUTTON_BRIGHT = 8;
    private static final int WAKE_LOCK_CPU = 1;
    private static final int WAKE_LOCK_DOZE = 64;
    private static final int WAKE_LOCK_DRAW = 128;
    private static final int WAKE_LOCK_PROXIMITY_SCREEN_OFF = 16;
    private static final int WAKE_LOCK_SCREEN_BRIGHT = 2;
    private static final int WAKE_LOCK_SCREEN_DIM = 4;
    private static final int WAKE_LOCK_STAY_AWAKE = 32;
    protected static final int WAKE_UP_TYPE_DOUBLIECLICK = 5;
    protected static final int WAKE_UP_TYPE_FINGERPRINT = 3;
    protected static final int WAKE_UP_TYPE_MAGNETOMETER = 4;
    protected static final int WAKE_UP_TYPE_PHONECALL = 6;
    protected static final int WAKE_UP_TYPE_PICK_UP = 2;
    protected static final int WAKE_UP_TYPE_POWERKEY = 0;
    protected static final int WAKE_UP_TYPE_SWITCH_SCREEN = 1;
    private static final String incalluiPackageName = "com.android.incallui";
    /* access modifiers changed from: private */
    public static final boolean mSupportAod = POWER_STATE_CHARGE_ENABLE.equals(SystemProperties.get("ro.config.support_aod", null));
    protected static final boolean mSupportFaceDetect = (SystemProperties.getInt("ro.config.face_detect", 0) == 1 && SystemProperties.getBoolean("ro.config.face_smart_keepon", true));
    private static final String machineCarPackageName = "com.huawei.vdrive";
    private static boolean sQuiescent;
    private static final boolean sSupportFaceRecognition = SystemProperties.getBoolean("ro.config.face_recognition", false);
    private boolean inVdriveBackLightMode = false;
    protected boolean mAdjustTimeNextUserActivity = false;
    /* access modifiers changed from: private */
    public int mAlpmState = -1;
    private boolean mAlwaysOnEnabled;
    private final AmbientDisplayConfiguration mAmbientDisplayConfiguration;
    private IAppOpsService mAppOps;
    private Light mAttentionLight;
    protected boolean mAuthSucceeded = false;
    private int mAutoBrightnessLevel;
    /* access modifiers changed from: private */
    public Light mBackLight;
    PowerManager.BacklightBrightness mBacklightBrightness = new PowerManager.BacklightBrightness(255, 0, 128);
    private int mBatteryLevel;
    private boolean mBatteryLevelLow;
    private int mBatteryLevelWhenDreamStarted;
    private BatteryManagerInternal mBatteryManagerInternal;
    /* access modifiers changed from: private */
    public final BatterySaverController mBatterySaverController;
    /* access modifiers changed from: private */
    public final BatterySaverPolicy mBatterySaverPolicy;
    private final BatterySaverStateMachine mBatterySaverStateMachine;
    private final BatterySavingStats mBatterySavingStats;
    private IBatteryStats mBatteryStats;
    protected boolean mBootCompleted;
    private Runnable[] mBootCompletedRunnables;
    private boolean mBrightnessUseTwilight;
    protected boolean mBrightnessWaitModeEnabled;
    protected boolean mBrightnessWaitRet;
    final Constants mConstants;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCoverModeBrightness;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    private int mCustomUserActivityTimeout = 0;
    /* access modifiers changed from: private */
    public boolean mDecoupleHalAutoSuspendModeFromDisplayConfig;
    /* access modifiers changed from: private */
    public boolean mDecoupleHalInteractiveModeFromDisplayConfig;
    private boolean mDeviceIdleMode;
    int[] mDeviceIdleTempWhitelist = new int[0];
    int[] mDeviceIdleWhitelist = new int[0];
    protected int mDirty;
    protected DisplayManagerInternal mDisplayManagerInternal;
    private final DisplayManagerInternal.DisplayPowerCallbacks mDisplayPowerCallbacks = new DisplayManagerInternal.DisplayPowerCallbacks() {
        private int mDisplayState = 0;

        public void onStateChanged() {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mDirty |= 8;
                PowerManagerService.this.updatePowerStateLocked();
            }
        }

        public void hwBrightnessOnStateChanged(String what, int arg1, int arg2, Bundle data) {
            PowerManagerService.this.notifyHwBrightnessCallbacks(what, arg1, arg2, data);
        }

        public void onProximityPositive() {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mProximityPositive = true;
                PowerManagerService.this.mDirty |= 512;
                if (PowerManagerService.this.isPhoneHeldWakeLock()) {
                    if (!HwPCUtils.isPcCastModeInServer()) {
                        if (PowerManagerService.this.goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 100, 0, 1000)) {
                            Flog.i(NsdService.NativeResponseCode.SERVICE_LOST, "UL_Power onProximityPositivebyPhone -> updatePowerStateLocked");
                            PowerManagerService.this.updatePowerStateLocked();
                        }
                        Message msg = PowerManagerService.this.mHandler.obtainMessage(5);
                        msg.setAsynchronous(true);
                        PowerManagerService.this.mHandler.sendMessage(msg);
                    }
                }
                Flog.i(NsdService.NativeResponseCode.SERVICE_LOST, "UL_Power onProximityPositive -> updatePowerStateLocked");
                PowerManagerService.this.updatePowerStateLocked();
                Message msg2 = PowerManagerService.this.mHandler.obtainMessage(5);
                msg2.setAsynchronous(true);
                PowerManagerService.this.mHandler.sendMessage(msg2);
            }
        }

        public void onProximityNegative() {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mProximityPositive = false;
                PowerManagerService.this.mDirty |= 512;
                Jlog.d(77, "JL_WAKEUP_REASON_PROX");
                Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power onProximityNegative -> updatePowerStateLocked");
                PowerManagerService.this.userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 0, 1000);
                if (PowerManagerService.this.wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:POWER", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000) || PowerManagerService.this.wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "onProximityNegative", 1000, PowerManagerService.this.mContext.getOpPackageName(), 1000) || HwPCUtils.isPcCastModeInServer()) {
                    Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power onProximityNegative by Phone");
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }
            PowerManagerService.this.stopWakeLockedSensor(false);
        }

        /* JADX WARNING: Removed duplicated region for block: B:26:0x0054  */
        public void onDisplayStateChange(int state) {
            synchronized (PowerManagerService.this.mLock) {
                if (this.mDisplayState != state) {
                    this.mDisplayState = state;
                    if (state != 1) {
                        if (!PowerManagerService.mSupportAod || state != 4) {
                            if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                                PowerManagerService.this.setHalAutoSuspendModeLocked(false);
                            }
                            if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                                PowerManagerService.this.setHalInteractiveModeLocked(true);
                            }
                            if (PowerManagerService.mSupportAod) {
                                PowerManagerService.this.mPolicy.onPowerStateChange(state);
                            }
                        }
                    }
                    if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                        PowerManagerService.this.setHalInteractiveModeLocked(false);
                    }
                    if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                        PowerManagerService.this.setHalAutoSuspendModeLocked(true);
                    }
                    if (PowerManagerService.mSupportAod) {
                    }
                }
            }
        }

        public void acquireSuspendBlocker() {
            PowerManagerService.this.mDisplaySuspendBlocker.acquire();
        }

        public void releaseSuspendBlocker() {
            PowerManagerService.this.mDisplaySuspendBlocker.release();
        }

        public String toString() {
            String str;
            synchronized (this) {
                str = "state=" + Display.stateToString(this.mDisplayState);
            }
            return str;
        }
    };
    private final DisplayManagerInternal.DisplayPowerRequest mDisplayPowerRequest = new DisplayManagerInternal.DisplayPowerRequest();
    private boolean mDisplayReady;
    /* access modifiers changed from: private */
    public final SuspendBlocker mDisplaySuspendBlocker;
    /* access modifiers changed from: private */
    public int mDockState = 0;
    private boolean mDoubleTapWakeEnabled;
    private boolean mDozeAfterScreenOff;
    private int mDozeScreenBrightnessOverrideFromDreamManager = -1;
    private int mDozeScreenStateOverrideFromDreamManager = 0;
    private boolean mDrawWakeLockOverrideFromSidekick;
    private DreamManagerInternal mDreamManager;
    private boolean mDreamsActivateOnDockSetting;
    private boolean mDreamsActivateOnSleepSetting;
    private boolean mDreamsActivatedOnDockByDefaultConfig;
    private boolean mDreamsActivatedOnSleepByDefaultConfig;
    private int mDreamsBatteryLevelDrainCutoffConfig;
    private int mDreamsBatteryLevelMinimumWhenNotPoweredConfig;
    private int mDreamsBatteryLevelMinimumWhenPoweredConfig;
    private boolean mDreamsEnabledByDefaultConfig;
    private boolean mDreamsEnabledOnBatteryConfig;
    private boolean mDreamsEnabledSetting;
    private boolean mDreamsSupportedConfig;
    private int mEyesProtectionMode = 0;
    private boolean mFirstBoot = true;
    /* access modifiers changed from: private */
    public boolean mForceDoze = false;
    /* access modifiers changed from: private */
    public int mForegroundProfile;
    private boolean mHalAutoSuspendModeEnabled;
    private boolean mHalInteractiveModeEnabled;
    protected final PowerManagerHandler mHandler;
    private final ServiceThread mHandlerThread;
    private boolean mHoldingDisplaySuspendBlocker;
    private boolean mHoldingWakeLockSuspendBlocker;
    HwInnerPowerManagerService mHwInnerService = new HwInnerPowerManagerService(this);
    IHwPowerManagerServiceEx mHwPowerEx = null;
    /* access modifiers changed from: private */
    public InputManagerInternal mInputManagerInternal;
    /* access modifiers changed from: private */
    public boolean mIsCoverModeEnabled;
    private boolean mIsPowered;
    /* access modifiers changed from: private */
    public boolean mIsVrModeEnabled;
    private boolean mKeyguardLocked = false;
    private KeyguardManager mKeyguardManager = null;
    private long mLastInteractivePowerHintTime;
    private long mLastOneSecActivityTime = 0;
    private long mLastScreenBrightnessBoostTime;
    protected long mLastSleepTime;
    protected long mLastSleepTimeDuoToFastFP = 0;
    private long mLastUserActivityTime;
    private long mLastUserActivityTimeNoChangeLights;
    protected long mLastWakeTime;
    /* access modifiers changed from: private */
    public long mLastWarningAboutUserActivityPermission = Long.MIN_VALUE;
    private boolean mLightDeviceIdleMode;
    protected LightsManager mLightsManager;
    protected final Object mLock = LockGuard.installNewLock(1);
    private final ArrayList<PowerManagerInternal.LowPowerModeListener> mLowPowerModeListeners = new ArrayList<>();
    private long mMaximumScreenDimDurationConfig;
    private float mMaximumScreenDimRatioConfig;
    private long mMaximumScreenOffTimeoutFromDeviceAdmin = JobStatus.NO_LATEST_RUNTIME;
    private long mMinimumScreenOffTimeoutConfig;
    protected Notifier mNotifier;
    private long mNotifyLongDispatched;
    private long mNotifyLongNextCheck;
    private long mNotifyLongScheduled;
    private long mOverriddenTimeout = -1;
    private int mPlugType;
    /* access modifiers changed from: private */
    public WindowManagerPolicy mPolicy;
    HwPowerDAMonitorProxy mPowerProxy = new HwPowerDAMonitorProxy();
    private final SparseArray<ProfilePowerState> mProfilePowerState = new SparseArray<>();
    protected boolean mProximityPositive;
    protected boolean mRequestWaitForNegativeProximity;
    private boolean mSandmanScheduled;
    private boolean mSandmanSummoned;
    private boolean mScreenBrightnessBoostInProgress;
    private int mScreenBrightnessModeSetting;
    private int mScreenBrightnessOverrideFromWindowManager = -1;
    private int mScreenBrightnessSetting;
    private int mScreenBrightnessSettingDefault;
    private int mScreenBrightnessSettingMaximum;
    private int mScreenBrightnessSettingMinimum;
    private long mScreenOffTimeoutSetting;
    private boolean mScreenTimeoutFlag;
    private SettingsObserver mSettingsObserver;
    protected boolean mSkipWaitKeyguardDismiss;
    private long mSleepTimeoutSetting;
    private int mSmartBacklightEnableSetting;
    private boolean mStayOn;
    private int mStayOnWhilePluggedInSetting;
    private boolean mSupportsDoubleTapWakeConfig;
    private final ArrayList<SuspendBlocker> mSuspendBlockers = new ArrayList<>();
    private boolean mSuspendWhenScreenOffDueToProximityConfig;
    /* access modifiers changed from: private */
    public boolean mSystemReady;
    private int mTemporaryScreenAutoBrightnessSettingOverride = -1;
    private boolean mTheaterModeEnabled;
    private final SparseArray<UidState> mUidState = new SparseArray<>();
    private boolean mUidsChanged;
    private boolean mUidsChanging;
    private boolean mUpdateBacklightBrightnessFlag = false;
    /* access modifiers changed from: private */
    public int mUserActivitySummary;
    private long mUserActivityTimeoutOverrideFromWindowManager = -1;
    private boolean mUserFirstBoot = true;
    private boolean mUserInactiveOverrideFromWindowManager;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        public void onVrStateChanged(boolean enabled) {
            PowerManagerService.this.powerHintInternal(7, enabled);
            synchronized (PowerManagerService.this.mLock) {
                if (PowerManagerService.this.mIsVrModeEnabled != enabled) {
                    PowerManagerService.this.setVrModeEnabled(enabled);
                    PowerManagerService.this.mDirty |= 8192;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mWakeLockSummary;
    private final SuspendBlocker mWakeLockSuspendBlocker;
    protected final ArrayList<WakeLock> mWakeLocks = new ArrayList<>();
    private boolean mWakeUpWhenPluggedOrUnpluggedConfig;
    private boolean mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig;
    protected int mWakefulness;
    private boolean mWakefulnessChanging;
    private WirelessChargerDetector mWirelessChargerDetector;

    private final class BatteryReceiver extends BroadcastReceiver {
        private BatteryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                if (!HwFoldScreenState.isFoldScreenDevice() || !PowerManagerService.this.needWakeup()) {
                    PowerManagerService.this.handleBatteryStateChangedLocked();
                } else {
                    Slog.d(PowerManagerService.TAG, "BatteryReceiver to wakeUp");
                    PowerManagerService.this.mHwPowerEx.prepareWakeupEx(0, 1000, PowerManagerService.this.mContext.getOpPackageName(), "android.server.power:POWER");
                }
            }
        }
    }

    private final class BinderService extends IPowerManager.Stub {
        private static final int MAX_DEFAULT_BRIGHTNESS = 255;
        private static final int SEEK_BAR_RANGE = 10000;
        private double mCovertFactor;
        private int mMaximumBrightness;
        private int mMinimumBrightness;
        ArrayList<String> mWakeLockPackageNameList;

        private BinderService() {
            this.mCovertFactor = 1.7999999523162842d;
            this.mMinimumBrightness = 4;
            this.mMaximumBrightness = 255;
            this.mWakeLockPackageNameList = new ArrayList<>();
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
        /* JADX WARNING: Multi-variable type inference failed */
        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            new PowerManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
        }

        public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uid) {
            if (uid < 0) {
                uid = Binder.getCallingUid();
            }
            acquireWakeLock(lock, flags, tag, packageName, new WorkSource(uid), null);
        }

        public void setMirrorLinkPowerStatus(boolean status) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.mBackLight.setMirrorLinkBrightnessStatus(false);
            if (PowerManagerService.this.mInputManagerInternal != null) {
                PowerManagerService.this.mInputManagerInternal.setMirrorLinkInputStatus(false);
            }
            Slog.d(PowerManagerService.TAG, "setMirrorLinkPowerStatus status" + status);
        }

        public void powerHint(int hintId, int data) {
            if (PowerManagerService.this.mSystemReady) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                PowerManagerService.this.powerHintInternal(hintId, data);
            }
        }

        public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) {
            long ident;
            int flags2 = flags;
            String str = tag;
            String str2 = packageName;
            WorkSource workSource = ws;
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            } else if (str2 == null) {
                throw new IllegalArgumentException("packageName must not be null");
            } else if (PowerManagerService.this.isAppCanGetDrawWakeLock(flags2, str2, workSource, str) && !PowerManagerService.this.isAppWakeLockFilterTag(flags2, str2, workSource)) {
                PowerManager.validateWakeLockParameters(flags, tag);
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                if ((flags2 & 64) != 0) {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                }
                if (workSource == null || ws.isEmpty()) {
                    workSource = null;
                } else {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                }
                WorkSource ws2 = workSource;
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                long ident2 = Binder.clearCallingIdentity();
                if ((268435456 & flags2) != 0 && PowerManagerService.this.mHwPowerEx.isAwarePreventScreenOn(str2, str)) {
                    flags2 &= -268435457;
                }
                int flags3 = PowerManagerService.this.mHwPowerEx.addWakeLockFlagsForPC(str2, uid, flags2);
                try {
                    String str3 = str;
                    ident = ident2;
                    try {
                        if (true == PowerManagerService.this.acquireProxyWakeLock(lock, flags3, str3, str2, ws2, historyTag, uid, pid)) {
                            Binder.restoreCallingIdentity(ident);
                            return;
                        }
                        PowerManagerService.this.acquireWakeLockInternal(lock, flags3, tag, packageName, ws2, historyTag, uid, pid);
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        th = th;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    ident = ident2;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
        }

        public void releaseWakeLock(IBinder lock, int flags) {
            if (lock != null) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.releaseProxyWakeLock(lock);
                    PowerManagerService.this.releaseWakeLockInternal(lock, flags);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("lock must not be null");
            }
        }

        public int setColorTemperature(int colorTemper) {
            if (1000 != UserHandle.getAppId(Binder.getCallingUid())) {
                Slog.e(PowerManagerService.TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return -1;
            }
            Slog.d(PowerManagerService.TAG, "setColorTemperature" + colorTemper);
            return PowerManagerService.this.setColorTemperatureInternal(colorTemper);
        }

        public int updateRgbGamma(float red, float green, float blue) {
            if (1000 == UserHandle.getAppId(Binder.getCallingUid())) {
                return PowerManagerService.this.updateRgbGammaInternal(red, green, blue);
            }
            Slog.e(PowerManagerService.TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        }

        public void updateWakeLockUids(IBinder lock, int[] uids) {
            WorkSource ws = null;
            if (uids != null) {
                ws = new WorkSource();
                for (int add : uids) {
                    ws.add(add);
                }
            }
            updateWakeLockWorkSource(lock, ws, null);
        }

        public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) {
            if (lock != null) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                if (ws == null || ws.isEmpty()) {
                    ws = null;
                } else {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                }
                int callingUid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (true == PowerManagerService.this.updateProxyWakeLockWorkSource(lock, ws, historyTag, callingUid)) {
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    PowerManagerService.this.updateWakeLockWorkSourceInternal(lock, ws, historyTag, callingUid);
                    Binder.restoreCallingIdentity(ident);
                } catch (IllegalArgumentException e) {
                    Slog.e(PowerManagerService.TAG, "Exception when search wack lock :" + e);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } else {
                throw new IllegalArgumentException("lock must not be null");
            }
        }

        public boolean isWakeLockLevelSupported(int level) {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isWakeLockLevelSupportedInternal(level);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void userActivity(long eventTime, int event, int flags) {
            long now = SystemClock.uptimeMillis();
            if (PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0 && PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.USER_ACTIVITY") != 0) {
                synchronized (PowerManagerService.this.mLock) {
                    if (now >= PowerManagerService.this.mLastWarningAboutUserActivityPermission + BackupAgentTimeoutParameters.DEFAULT_FULL_BACKUP_AGENT_TIMEOUT_MILLIS) {
                        long unused = PowerManagerService.this.mLastWarningAboutUserActivityPermission = now;
                        Slog.w(PowerManagerService.TAG, "Ignoring call to PowerManager.userActivity() because the caller does not have DEVICE_POWER or USER_ACTIVITY permission.  Please fix your app!   pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid());
                    }
                }
            } else if (eventTime <= now) {
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.userActivityInternal(eventTime, event, flags, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void wakeUp(long eventTime, String reason, String opPackageName) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                if (Jlog.isPerfTest()) {
                    Jlog.i(2202, "JL_PWRSCRON_PMS_WAKEUP");
                }
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (HwFoldScreenState.isFoldScreenDevice()) {
                        PowerManagerService.this.mHwPowerEx.prepareWakeupEx(0, uid, opPackageName, reason);
                    } else {
                        PowerManagerService.this.wakeUpInternal(eventTime, reason, uid, opPackageName, uid);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void goToSleep(long eventTime, int reason, int flags) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (HwFoldScreenState.isFoldScreenDevice()) {
                        PowerManagerService.this.mHwPowerEx.notifySleepEx();
                    }
                    PowerManagerService.this.goToSleepInternal(eventTime, reason, flags, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void nap(long eventTime) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.napInternal(eventTime, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public boolean isInteractive() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isInteractiveInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mBatterySaverController.isEnabled();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public PowerSaveState getPowerSaveState(int serviceType) {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(serviceType, PowerManagerService.this.mBatterySaverController.isEnabled());
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setPowerSaveMode(boolean enabled) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.setLowPowerModeInternal(enabled);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isDeviceIdleModeInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isLightDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isLightDeviceIdleModeInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getLastShutdownReason() {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.getLastShutdownReasonInternal(PowerManagerService.LAST_REBOOT_PROPERTY);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void reboot(boolean confirm, String reason, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            if ("recovery".equals(reason) || "recovery-update".equals(reason)) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.RECOVERY", null);
            }
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            Flog.e(1600, "PowerManagerService reboot_reason:" + reason + ", uid=" + uid + ", pid=" + pid);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(1, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void rebootSafeMode(boolean confirm, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(2, confirm, "safemode", wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void shutdown(boolean confirm, String reason, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            Flog.e(1600, "PowerManagerService shutdown  uid=" + uid + ", pid=" + pid);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(0, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void crash(String message) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.crashInternal(message);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setStayOnSetting(int val) {
            int uid = Binder.getCallingUid();
            if (uid == 0 || Settings.checkAndNoteWriteSettingsOperation(PowerManagerService.this.mContext, uid, Settings.getPackageNameForUid(PowerManagerService.this.mContext, uid), true)) {
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.setStayOnSettingInternal(val);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void setTemporaryScreenBrightnessSettingOverride(int brightness) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.sendTempBrightnessToMonitor("tempManualBrightness", brightness);
            long ident = Binder.clearCallingIdentity();
            try {
                if (PowerManagerService.this.mDisplayManagerInternal != null) {
                    PowerManagerService.this.mDisplayManagerInternal.setTemporaryScreenBrightnessSettingOverride(brightness);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(float adj) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            Binder.restoreCallingIdentity(Binder.clearCallingIdentity());
        }

        public void setTemporaryScreenAutoBrightnessSettingOverride(int brightness) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.sendTempBrightnessToMonitor("tempAutoBrightness", brightness);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setTemporaryScreenAutoBrightnessSettingOverrideInternal(brightness);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int convertSeekbarProgressToBrightness(int progress) {
            if (progress < 0) {
                if (PowerManagerService.DEBUG) {
                    Slog.i(PowerManagerService.TAG, "brightnessSeekbar progress=" + progress + " <min=0");
                }
                progress = 0;
            }
            if (progress > 10000) {
                if (PowerManagerService.DEBUG) {
                    Slog.i(PowerManagerService.TAG, "brightnessSeekbar progress=" + progress + " >max=" + 10000);
                }
                progress = 10000;
            }
            if (SystemProperties.getInt("ro.config.hw_high_bright_mode", 1) == 1) {
                this.mMaximumBrightness = PowerManagerService.this.mDisplayManagerInternal.getMaxBrightnessForSeekbar();
            } else {
                this.mMaximumBrightness = 255;
            }
            return Math.round((((float) Math.pow((double) (((float) progress) / 10000.0f), this.mCovertFactor)) * ((float) (this.mMaximumBrightness - this.mMinimumBrightness))) + ((float) this.mMinimumBrightness));
        }

        public float convertBrightnessToSeekbarPercentage(float brightness) {
            if (SystemProperties.getInt("ro.config.hw_high_bright_mode", 1) == 1) {
                this.mMaximumBrightness = PowerManagerService.this.mDisplayManagerInternal.getMaxBrightnessForSeekbar();
            } else {
                this.mMaximumBrightness = 255;
            }
            if (brightness > ((float) this.mMaximumBrightness)) {
                if (PowerManagerService.DEBUG) {
                    Slog.i(PowerManagerService.TAG, "HBM brightness=" + brightness + " >Max=" + this.mMaximumBrightness);
                }
                brightness = (float) this.mMaximumBrightness;
            }
            if (brightness < ((float) this.mMinimumBrightness)) {
                if (PowerManagerService.DEBUG) {
                    Slog.i(PowerManagerService.TAG, "brightnessSeekbar brightness=" + brightness + " <min=" + this.mMinimumBrightness);
                }
                brightness = (float) this.mMinimumBrightness;
            }
            return (float) Math.pow((double) ((brightness - ((float) this.mMinimumBrightness)) / ((float) (this.mMaximumBrightness - this.mMinimumBrightness))), 1.0d / this.mCovertFactor);
        }

        public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.CONFIG_BRIGHTNESS", null);
            if (PowerManagerService.DEBUG) {
                Slog.i(PowerManagerService.TAG, "setAnimationTime animationEnabled=" + animationEnabled + ",millisecond=" + millisecond);
            }
            PowerManagerService.this.mDisplayManagerInternal.setBrightnessAnimationTime(animationEnabled, millisecond);
        }

        public void onCoverModeChanged(boolean iscovered) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            synchronized (PowerManagerService.this.mLock) {
                if (PowerManagerService.this.mIsCoverModeEnabled != iscovered) {
                    PowerManagerService.this.updatePowerStateLocked();
                }
                boolean unused = PowerManagerService.this.mIsCoverModeEnabled = iscovered;
                if (PowerManagerService.DEBUG) {
                    Slog.d(PowerManagerService.TAG, "coverModeChange mIsCoverModeEnabled=" + PowerManagerService.this.mIsCoverModeEnabled);
                }
            }
        }

        public int getCoverModeBrightnessFromLastScreenBrightness() {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return PowerManagerService.this.mDisplayManagerInternal.getCoverModeBrightnessFromLastScreenBrightness();
        }

        public void setMaxBrightnessFromThermal(int brightness) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.mDisplayManagerInternal.setMaxBrightnessFromThermal(brightness);
        }

        public void setBrightnessNoLimit(int brightness, int time) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.mDisplayManagerInternal.setBrightnessNoLimit(brightness, time);
        }

        public void setModeToAutoNoClearOffsetEnable(boolean enable) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.mDisplayManagerInternal.setModeToAutoNoClearOffsetEnable(enable);
        }

        public void setAodAlpmState(int globalState) {
            int unused = PowerManagerService.this.mAlpmState = globalState;
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            PowerManagerService.this.mDisplayManagerInternal.setAodAlpmState(globalState);
        }

        public void setAttentionLight(boolean on, int color) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setAttentionLightInternal(on, color);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setDozeAfterScreenOff(boolean on) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setDozeAfterScreenOffInternal(on);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void boostScreenBrightness(long eventTime) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.boostScreenBrightnessInternal(eventTime, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public boolean isScreenBrightnessBoosted() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.isScreenBrightnessBoostedInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(PowerManagerService.this.mContext, PowerManagerService.TAG, pw)) {
                long ident = Binder.clearCallingIdentity();
                boolean isDumpProto = false;
                for (String arg : args) {
                    if (arg.equals(PriorityDump.PROTO_ARG)) {
                        isDumpProto = true;
                    }
                }
                if (isDumpProto) {
                    try {
                        PowerManagerService.this.dumpProto(fd);
                    } finally {
                        Binder.restoreCallingIdentity(ident);
                    }
                } else {
                    PowerManagerService.this.dumpInternal(pw);
                }
            }
        }

        public void startWakeUpReady(long eventTime, String opPackageName) {
            if (eventTime <= SystemClock.uptimeMillis()) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (HwFoldScreenState.isFoldScreenDevice()) {
                        PowerManagerService.this.mHwPowerEx.prepareWakeupEx(3, uid, opPackageName, "startWakeUpReady");
                    } else {
                        PowerManagerService.this.startWakeUpReadyInternal(eventTime, uid, opPackageName);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("event time must not be in the future");
            }
        }

        public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                if (!HwFoldScreenState.isFoldScreenDevice()) {
                    PowerManagerService.this.stopWakeUpReadyInternal(eventTime, uid, enableBright, opPackageName);
                } else if (enableBright) {
                    PowerManagerService.this.mHwPowerEx.startWakeupEx(3, uid, opPackageName, "stopWakeUpReady");
                } else {
                    PowerManagerService.this.mHwPowerEx.notifySleepEx();
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setAuthSucceeded() {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setAuthSucceededInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int getDisplayPanelType() {
            long ident = Binder.clearCallingIdentity();
            try {
                return PowerManagerService.this.getDisplayPanelTypeInternal();
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int hwBrightnessSetData(String name, Bundle data) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return PowerManagerService.this.hwBrightnessSetDataInternal(name, data);
        }

        public int hwBrightnessGetData(String name, Bundle data) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return PowerManagerService.this.hwBrightnessGetDataInternal(name, data);
        }

        public int hwBrightnessRegisterCallback(IHwBrightnessCallback cb, List<String> filter) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return PowerManagerService.this.hwBrightnessRegisterCallbackInternal(cb, filter);
        }

        public int hwBrightnessUnregisterCallback(IHwBrightnessCallback cb) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            return PowerManagerService.this.hwBrightnessUnregisterCallbackInternal(cb);
        }

        public void updateBlockedUids(int uid, boolean isBlocked) {
        }

        public boolean isUsingSkipWakeLock(int uid, String tag) {
            return PowerManagerService.this.isSkipWakeLockUsing(uid, tag);
        }

        public void regeditAodStateCallback(IAodStateCallback callback) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService regeditAodStateCallback()");
            if (PowerManagerService.mSupportAod) {
                PowerManagerService.this.mPolicy.regeditAodStateCallback(callback);
            }
        }

        public void unregeditAodStateCallback(IAodStateCallback callback) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService unregeditAodStateCallback()");
            if (PowerManagerService.mSupportAod) {
                PowerManagerService.this.mPolicy.unregeditAodStateCallback(callback);
            }
        }

        public void setAodState(int globalState, int alpmMode) {
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService setAodState()");
            if (PowerManagerService.mSupportAod) {
                Slog.d(PowerManagerService.TAG, "setAodStateBySysfs:  globalState=" + globalState + ", AlpmMode=" + alpmMode);
                if (globalState != -1) {
                    PowerManagerService.this.setAodStateBySysfs(PowerManagerService.AOD_STATE_CMD, globalState);
                }
                if (alpmMode != -1) {
                    PowerManagerService.this.setAodStateBySysfs(PowerManagerService.AOD_MODE_CMD, alpmMode);
                }
            }
        }

        public int getAodState(String file) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService getAodState()");
            if (!PowerManagerService.mSupportAod) {
                return -1;
            }
            return PowerManagerService.this.getAodStateBySysfs(file);
        }

        public void setDozeOverrideFromAod(int screenState, int screenBrightness, IBinder binder) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService setDozeOverrideFromAod()");
            if (PowerManagerService.mSupportAod) {
                synchronized (PowerManagerService.this.mLock) {
                    switch (screenState) {
                        case 0:
                        case 2:
                            boolean unused = PowerManagerService.this.mForceDoze = false;
                            break;
                        case 1:
                            boolean unused2 = PowerManagerService.this.mForceDoze = false;
                            PowerManagerService.this.setWakefulnessLocked(0, 0);
                            break;
                        case 3:
                            if (!PowerManagerService.this.mForceDoze && !PowerManagerInternal.isInteractive(PowerManagerService.this.mWakefulness)) {
                                PowerManagerService.this.setWakefulnessLocked(3, 0);
                                boolean unused3 = PowerManagerService.this.mForceDoze = true;
                                break;
                            }
                        case 4:
                            break;
                        default:
                            boolean unused4 = PowerManagerService.this.mForceDoze = false;
                            break;
                    }
                    if (screenBrightness < -1 || screenBrightness > 255) {
                        screenBrightness = -1;
                    }
                    Slog.d(PowerManagerService.TAG, "setDozeOverrideFromAod screenState = " + screenState + ", Brightness = " + screenBrightness + ", ForceDoze = " + PowerManagerService.this.mForceDoze + ", wakefulness = " + PowerManagerInternal.isInteractive(PowerManagerService.this.mWakefulness));
                    PowerManagerService.this.setDozeOverrideFromAodLocked(screenState, screenBrightness);
                }
            }
        }

        public List<String> getWakeLockPackageName() {
            ArrayList<String> arrayList;
            synchronized (PowerManagerService.this.mLock) {
                this.mWakeLockPackageNameList.clear();
                for (int i = 0; i < PowerManagerService.this.mWakeLocks.size(); i++) {
                    WakeLock wakeLock = PowerManagerService.this.mWakeLocks.get(i);
                    if (wakeLock != null && ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 6 || (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 10 || (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 26)) {
                        this.mWakeLockPackageNameList.add(wakeLock.mPackageName);
                    }
                }
                arrayList = this.mWakeLockPackageNameList;
            }
            return arrayList;
        }

        /* JADX WARNING: type inference failed for: r0v1, types: [com.android.server.power.PowerManagerService$HwInnerPowerManagerService, android.os.IBinder] */
        public IBinder getHwInnerService() {
            return PowerManagerService.this.mHwInnerService;
        }
    }

    private final class Constants extends ContentObserver {
        private static final boolean DEFAULT_NO_CACHED_WAKE_LOCKS = true;
        private static final String KEY_NO_CACHED_WAKE_LOCKS = "no_cached_wake_locks";
        public boolean NO_CACHED_WAKE_LOCKS = true;
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private ContentResolver mResolver;

        public Constants(Handler handler) {
            super(handler);
        }

        public void start(ContentResolver resolver) {
            this.mResolver = resolver;
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("power_manager_constants"), false, this);
            updateConstants();
        }

        public void onChange(boolean selfChange, Uri uri) {
            updateConstants();
        }

        private void updateConstants() {
            synchronized (PowerManagerService.this.mLock) {
                try {
                    this.mParser.setString(Settings.Global.getString(this.mResolver, "power_manager_constants"));
                } catch (IllegalArgumentException e) {
                    Slog.e(PowerManagerService.TAG, "Bad alarm manager settings", e);
                }
                this.NO_CACHED_WAKE_LOCKS = this.mParser.getBoolean(KEY_NO_CACHED_WAKE_LOCKS, true);
            }
        }

        /* access modifiers changed from: package-private */
        public void dump(PrintWriter pw) {
            pw.println("  Settings power_manager_constants:");
            pw.print("    ");
            pw.print(KEY_NO_CACHED_WAKE_LOCKS);
            pw.print("=");
            pw.println(this.NO_CACHED_WAKE_LOCKS);
        }

        /* access modifiers changed from: package-private */
        public void dumpProto(ProtoOutputStream proto) {
            long constantsToken = proto.start(1146756268033L);
            proto.write(1133871366145L, this.NO_CACHED_WAKE_LOCKS);
            proto.end(constantsToken);
        }
    }

    private final class DockReceiver extends BroadcastReceiver {
        private DockReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", 0);
                if (PowerManagerService.this.mDockState != dockState) {
                    int unused = PowerManagerService.this.mDockState = dockState;
                    PowerManagerService.this.mDirty |= 1024;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }
        }
    }

    private final class DreamReceiver extends BroadcastReceiver {
        private DreamReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            SystemClock.sleep(50);
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.scheduleSandmanLocked();
            }
        }
    }

    private final class ForegroundProfileObserver extends SynchronousUserSwitchObserver {
        private ForegroundProfileObserver() {
        }

        public void onUserSwitching(int newUserId) throws RemoteException {
        }

        public void onForegroundProfileSwitch(int newProfileId) throws RemoteException {
            long now = SystemClock.uptimeMillis();
            synchronized (PowerManagerService.this.mLock) {
                int unused = PowerManagerService.this.mForegroundProfile = newProfileId;
                PowerManagerService.this.maybeUpdateForegroundProfileLastActivityLocked(now);
            }
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface HaltMode {
    }

    public class HwInnerPowerManagerService extends IHwPowerManager.Stub {
        PowerManagerService mPMS;

        HwInnerPowerManagerService(PowerManagerService pms) {
            this.mPMS = pms;
        }

        public boolean registerPowerMonitorCallback(IHwPowerDAMonitorCallback callback) {
            PowerManagerService.this.mPowerProxy.registerPowerMonitorCallback(callback);
            return true;
        }

        public void setChargeLimit(String limitValue) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (limitValue != null) {
                Slog.i(PowerManagerService.TAG, "setChargeLimit :" + limitValue);
                PowerManagerService.nativeSetPowerState(PowerManagerService.CHARGE_LIMIT_SCENE, limitValue);
                return;
            }
            Slog.e(PowerManagerService.TAG, "setChargeLimit failed:" + limitValue);
        }

        public void setPowerState(boolean state) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            Slog.i(PowerManagerService.TAG, "setPowerState :" + state);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.nativeSetPowerState(PowerManagerService.POWER_CHARGE_SCENE, state ? PowerManagerService.POWER_STATE_CHARGE_ENABLE : PowerManagerService.POWER_STATE_CHARGE_DISABLE);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void requestNoUserActivityNotification(int timeout) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.USER_ACTIVITY", null);
            if (PowerManagerService.this.mHwPowerEx != null) {
                PowerManagerService.this.mHwPowerEx.requestNoUserActivityNotification(timeout);
            }
        }

        public void suspendSystem(boolean suspend, boolean forceUpdate) {
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.suspendSystemInternal(suspend, forceUpdate);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isSystemSuspending() {
            return PowerManagerService.this.isSystemSuspendingInternal();
        }
    }

    private final class LocalService extends PowerManagerInternal {
        private LocalService() {
        }

        public void setScreenBrightnessOverrideFromWindowManager(int screenBrightness) {
            if (screenBrightness < -1) {
                screenBrightness = -1;
            }
            PowerManagerService.this.setScreenBrightnessOverrideFromWindowManagerInternal(screenBrightness);
        }

        public void setDozeOverrideFromDreamManager(int screenState, int screenBrightness) {
            switch (screenState) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    break;
                default:
                    screenState = 0;
                    break;
            }
            if (screenBrightness < -1 || screenBrightness > 255) {
                screenBrightness = -1;
            }
            PowerManagerService.this.setDozeOverrideFromDreamManagerInternal(screenState, screenBrightness);
        }

        public void setUserInactiveOverrideFromWindowManager() {
            PowerManagerService.this.setUserInactiveOverrideFromWindowManagerInternal();
        }

        public void setUserActivityTimeoutOverrideFromWindowManager(long timeoutMillis) {
            PowerManagerService.this.setUserActivityTimeoutOverrideFromWindowManagerInternal(timeoutMillis);
        }

        public void setDrawWakeLockOverrideFromSidekick(boolean keepState) {
            PowerManagerService.this.setDrawWakeLockOverrideFromSidekickInternal(keepState);
        }

        public void setMaximumScreenOffTimeoutFromDeviceAdmin(int userId, long timeMs) {
            PowerManagerService.this.setMaximumScreenOffTimeoutFromDeviceAdminInternal(userId, timeMs);
        }

        public PowerSaveState getLowPowerState(int serviceType) {
            return PowerManagerService.this.mBatterySaverPolicy.getBatterySaverPolicy(serviceType, PowerManagerService.this.mBatterySaverController.isEnabled());
        }

        public void registerLowPowerModeObserver(PowerManagerInternal.LowPowerModeListener listener) {
            PowerManagerService.this.mBatterySaverController.addListener(listener);
        }

        public boolean setDeviceIdleMode(boolean enabled) {
            return PowerManagerService.this.setDeviceIdleModeInternal(enabled);
        }

        public boolean setLightDeviceIdleMode(boolean enabled) {
            return PowerManagerService.this.setLightDeviceIdleModeInternal(enabled);
        }

        public void setDeviceIdleWhitelist(int[] appids) {
            PowerManagerService.this.setDeviceIdleWhitelistInternal(appids);
        }

        public void setDeviceIdleTempWhitelist(int[] appids) {
            PowerManagerService.this.setDeviceIdleTempWhitelistInternal(appids);
        }

        public void startUidChanges() {
            PowerManagerService.this.startUidChangesInternal();
        }

        public void finishUidChanges() {
            PowerManagerService.this.finishUidChangesInternal();
        }

        public void updateUidProcState(int uid, int procState) {
            PowerManagerService.this.updateUidProcStateInternal(uid, procState);
        }

        public void uidGone(int uid) {
            PowerManagerService.this.uidGoneInternal(uid);
        }

        public void uidActive(int uid) {
            PowerManagerService.this.uidActiveInternal(uid);
        }

        public void uidIdle(int uid) {
            PowerManagerService.this.uidIdleInternal(uid);
        }

        public void powerHint(int hintId, int data) {
            PowerManagerService.this.powerHintInternal(hintId, data);
        }

        public boolean isUserActivityScreenDimOrDream() {
            return ((PowerManagerService.this.mUserActivitySummary & 2) == 0 && (PowerManagerService.this.mUserActivitySummary & 4) == 0) ? false : true;
        }

        public boolean shouldUpdatePCScreenState() {
            boolean should = true;
            if (PowerManagerService.this.mProximityPositive && (PowerManagerService.this.mWakeLockSummary & 16) != 0 && PowerManagerService.this.mWakefulness == 1) {
                should = false;
            }
            return should;
        }

        public void powerWakeup(long eventTime, String reason, int uid, String opPackageName, int opUid) {
            PowerManagerService.this.wakeUpInternal(eventTime, reason, uid, opPackageName, opUid);
        }
    }

    protected final class PowerManagerHandler extends Handler {
        public PowerManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 101) {
                switch (i) {
                    case 1:
                        PowerManagerService.this.handleUserActivityTimeout();
                        HwFrameworkFactory.getHwApsImpl().StopSdrForSpecial("autosleep", -1);
                        return;
                    case 2:
                        PowerManagerService.this.handleSandman();
                        return;
                    case 3:
                        PowerManagerService.this.handleScreenBrightnessBoostTimeout();
                        return;
                    case 4:
                        PowerManagerService.this.checkForLongWakeLocks();
                        return;
                    case 5:
                        PowerManagerService.this.mPolicy.onProximityPositive();
                        return;
                    default:
                        switch (i) {
                            case 103:
                                PowerManagerService.this.registerFaceDetect();
                                return;
                            case 104:
                                Slog.d(PowerManagerService.TAG, "Face Dectect wakeUpInternal type:" + msg.obj);
                                Settings.Global.putString(PowerManagerService.this.mContext.getContentResolver(), PowerManagerService.WAKEUP_REASON, msg.obj == null ? "unknow" : msg.obj.toString());
                                return;
                            case 105:
                                PowerManagerService.this.handleCustomUserInActivityTimeout();
                                return;
                            default:
                                return;
                        }
                }
            } else {
                PowerManagerService.this.handleWaitBrightTimeout();
            }
        }
    }

    private static final class ProfilePowerState {
        long mLastUserActivityTime = SystemClock.uptimeMillis();
        boolean mLockingNotified;
        long mScreenOffTimeout;
        final int mUserId;
        int mWakeLockSummary;

        public ProfilePowerState(int userId, long screenOffTimeout) {
            this.mUserId = userId;
            this.mScreenOffTimeout = screenOffTimeout;
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleSettingsChangedLocked();
            }
        }
    }

    private final class SuspendBlockerImpl implements SuspendBlocker {
        private final String mName;
        private int mReferenceCount;
        private final String mTraceName;

        public SuspendBlockerImpl(String name) {
            this.mName = name;
            this.mTraceName = "SuspendBlocker (" + name + ")";
        }

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            try {
                if (this.mReferenceCount != 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was finalized without being released!");
                    this.mReferenceCount = 0;
                    PowerManagerService.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                }
            } finally {
                super.finalize();
            }
        }

        public void acquire() {
            synchronized (this) {
                this.mReferenceCount++;
                if (this.mReferenceCount == 1) {
                    if (PowerManagerService.DEBUG_Controller) {
                        Slog.d(PowerManagerService.TAG, "Acquiring suspend blocker \"" + this.mName + "\".");
                    }
                    Trace.asyncTraceBegin(131072, this.mTraceName, 0);
                    PowerManagerService.nativeAcquireSuspendBlocker(this.mName);
                }
            }
        }

        public void release() {
            synchronized (this) {
                this.mReferenceCount--;
                if (this.mReferenceCount == 0) {
                    if (PowerManagerService.DEBUG_Controller) {
                        Slog.d(PowerManagerService.TAG, "Releasing suspend blocker \"" + this.mName + "\".");
                    }
                    PowerManagerService.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                } else if (this.mReferenceCount < 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was released without being acquired!", new Throwable());
                    this.mReferenceCount = 0;
                }
            }
        }

        public String toString() {
            String str;
            synchronized (this) {
                str = this.mName + ": ref count=" + this.mReferenceCount;
            }
            return str;
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long sbToken = proto.start(fieldId);
            synchronized (this) {
                proto.write(1138166333441L, this.mName);
                proto.write(1120986464258L, this.mReferenceCount);
            }
            proto.end(sbToken);
        }
    }

    static final class UidState {
        boolean mActive;
        int mNumWakeLocks;
        int mProcState;
        final int mUid;

        UidState(int uid) {
            this.mUid = uid;
        }
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        private UserSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                int unused = PowerManagerService.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                if (PowerManagerService.DEBUG) {
                    Slog.d(PowerManagerService.TAG, "user changed:mCurrentUserId=" + PowerManagerService.this.mCurrentUserId);
                }
                PowerManagerService.this.handleSettingsChangedLocked();
            }
            if (PowerManagerService.mSupportFaceDetect) {
                PowerManagerService.this.unregisterFaceDetect();
                PowerManagerService.this.stopIntelliService();
            }
            PowerManagerService.this.setColorTemperatureAccordingToSetting();
        }
    }

    protected final class WakeLock implements IBinder.DeathRecipient {
        public long mAcquireTime;
        public boolean mDisabled;
        public int mFlags;
        public String mHistoryTag;
        public final IBinder mLock;
        public boolean mNotifiedAcquired;
        public boolean mNotifiedLong;
        public final int mOwnerPid;
        public final int mOwnerUid;
        public final String mPackageName;
        public String mTag;
        public final UidState mUidState;
        public WorkSource mWorkSource;

        public WakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid, UidState uidState) {
            this.mLock = lock;
            this.mFlags = flags;
            this.mTag = tag;
            this.mPackageName = packageName;
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
            this.mHistoryTag = historyTag;
            this.mOwnerUid = ownerUid;
            this.mOwnerPid = ownerPid;
            this.mUidState = uidState;
        }

        public void binderDied() {
            PowerManagerService.this.handleWakeLockDeath(this);
        }

        public boolean hasSameProperties(int flags, String tag, WorkSource workSource, int ownerUid, int ownerPid) {
            return this.mFlags == flags && this.mTag.equals(tag) && hasSameWorkSource(workSource) && this.mOwnerUid == ownerUid && this.mOwnerPid == ownerPid;
        }

        public void updateProperties(int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid) {
            if (!this.mPackageName.equals(packageName)) {
                throw new IllegalStateException("Existing wake lock package name changed: " + this.mPackageName + " to " + packageName);
            } else if (this.mOwnerUid != ownerUid) {
                throw new IllegalStateException("Existing wake lock uid changed: " + this.mOwnerUid + " to " + ownerUid);
            } else if (this.mOwnerPid == ownerPid) {
                this.mFlags = flags;
                this.mTag = tag;
                updateWorkSource(workSource);
                this.mHistoryTag = historyTag;
            } else {
                throw new IllegalStateException("Existing wake lock pid changed: " + this.mOwnerPid + " to " + ownerPid);
            }
        }

        public boolean hasSameWorkSource(WorkSource workSource) {
            return Objects.equals(this.mWorkSource, workSource);
        }

        public void updateWorkSource(WorkSource workSource) {
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("mLock:" + Objects.hashCode(this.mLock) + " ");
            sb.append(getLockLevelString());
            sb.append(" '");
            sb.append(this.mTag);
            sb.append("'");
            sb.append(getLockFlagsString());
            if (this.mDisabled) {
                sb.append(" DISABLED");
            }
            if (this.mNotifiedAcquired) {
                sb.append(" ACQ=");
                TimeUtils.formatDuration(this.mAcquireTime - SystemClock.uptimeMillis(), sb);
            }
            if (this.mNotifiedLong) {
                sb.append(" LONG");
            }
            sb.append(" (uid=");
            sb.append(this.mOwnerUid);
            if (this.mOwnerPid != 0) {
                sb.append(" pid=");
                sb.append(this.mOwnerPid);
            }
            if (this.mWorkSource != null) {
                sb.append(" ws=");
                sb.append(this.mWorkSource);
            }
            sb.append(")");
            return sb.toString();
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            long wakeLockToken = proto.start(fieldId);
            proto.write(1159641169921L, this.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI);
            proto.write(1138166333442L, this.mTag);
            long wakeLockFlagsToken = proto.start(1146756268035L);
            boolean z = false;
            proto.write(1133871366145L, (this.mFlags & 268435456) != 0);
            if ((this.mFlags & 536870912) != 0) {
                z = true;
            }
            proto.write(1133871366146L, z);
            proto.end(wakeLockFlagsToken);
            proto.write(1133871366148L, this.mDisabled);
            if (this.mNotifiedAcquired) {
                proto.write(1112396529669L, this.mAcquireTime);
            }
            proto.write(1133871366150L, this.mNotifiedLong);
            proto.write(1120986464263L, this.mOwnerUid);
            proto.write(1120986464264L, this.mOwnerPid);
            if (this.mWorkSource != null) {
                this.mWorkSource.writeToProto(proto, 1146756268041L);
            }
            proto.end(wakeLockToken);
        }

        private String getLockLevelString() {
            int i = this.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI;
            if (i == 1) {
                return "PARTIAL_WAKE_LOCK             ";
            }
            if (i == 6) {
                return "SCREEN_DIM_WAKE_LOCK          ";
            }
            if (i == 10) {
                return "SCREEN_BRIGHT_WAKE_LOCK       ";
            }
            if (i == 26) {
                return "FULL_WAKE_LOCK                ";
            }
            if (i == 32) {
                return "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
            }
            if (i == 64) {
                return "DOZE_WAKE_LOCK                ";
            }
            if (i != 128) {
                return "???                           ";
            }
            return "DRAW_WAKE_LOCK                ";
        }

        private String getLockFlagsString() {
            String result = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            if ((this.mFlags & 268435456) != 0) {
                result = result + " ACQUIRE_CAUSES_WAKEUP";
            }
            if ((this.mFlags & 536870912) == 0) {
                return result;
            }
            return result + " ON_AFTER_RELEASE";
        }
    }

    /* access modifiers changed from: private */
    public static native void nativeAcquireSuspendBlocker(String str);

    private native void nativeInit();

    /* access modifiers changed from: private */
    public static native void nativeReleaseSuspendBlocker(String str);

    private static native void nativeSendPowerHint(int i, int i2);

    private static native void nativeSetAutoSuspend(boolean z);

    private static native void nativeSetFeature(int i, int i2);

    public static native void nativeSetFsEnable(boolean z);

    private static native void nativeSetInteractive(boolean z);

    /* access modifiers changed from: private */
    public static native void nativeSetPowerState(String str, String str2);

    static {
        boolean z = false;
        if (Log.HWLog || !SystemProperties.getBoolean("ro.config.pms_log_filter_enable", true)) {
            z = true;
        }
        DEBUG_ALL = z;
    }

    public PowerManagerService(Context context) {
        super(context);
        this.mHwPowerEx = HwServiceExFactory.getHwPowerManagerServiceEx(this, context);
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mAmbientDisplayConfiguration = new AmbientDisplayConfiguration(this.mContext);
        this.mBatterySavingStats = new BatterySavingStats(this.mLock);
        this.mBatterySaverPolicy = new BatterySaverPolicy(this.mLock, this.mContext, this.mBatterySavingStats);
        BatterySaverController batterySaverController = new BatterySaverController(this.mLock, this.mContext, BackgroundThread.get().getLooper(), this.mBatterySaverPolicy, this.mBatterySavingStats);
        this.mBatterySaverController = batterySaverController;
        this.mBatterySaverStateMachine = new BatterySaverStateMachine(this.mLock, this.mContext, this.mBatterySaverController);
        synchronized (this.mLock) {
            this.mWakeLockSuspendBlocker = createSuspendBlockerLocked("PowerManagerService.WakeLocks");
            this.mDisplaySuspendBlocker = createSuspendBlockerLocked("PowerManagerService.Display");
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
            this.mHalAutoSuspendModeEnabled = false;
            this.mHalInteractiveModeEnabled = true;
            this.mWakefulness = 1;
            sQuiescent = SystemProperties.get(SYSTEM_PROPERTY_QUIESCENT, POWER_STATE_CHARGE_DISABLE).equals(POWER_STATE_CHARGE_ENABLE);
            nativeInit();
            nativeSetAutoSuspend(false);
            nativeSetInteractive(true);
            nativeSetFeature(1, 0);
        }
    }

    @VisibleForTesting
    PowerManagerService(Context context, BatterySaverPolicy batterySaverPolicy) {
        super(context);
        this.mContext = context;
        this.mHandlerThread = new ServiceThread(TAG, -4, false);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        this.mConstants = new Constants(this.mHandler);
        this.mAmbientDisplayConfiguration = new AmbientDisplayConfiguration(this.mContext);
        this.mDisplaySuspendBlocker = null;
        this.mWakeLockSuspendBlocker = null;
        this.mBatterySavingStats = new BatterySavingStats(this.mLock);
        this.mBatterySaverPolicy = batterySaverPolicy;
        BatterySaverController batterySaverController = new BatterySaverController(this.mLock, context, BackgroundThread.getHandler().getLooper(), batterySaverPolicy, this.mBatterySavingStats);
        this.mBatterySaverController = batterySaverController;
        this.mBatterySaverStateMachine = new BatterySaverStateMachine(this.mLock, this.mContext, this.mBatterySaverController);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.power.PowerManagerService$BinderService, android.os.IBinder] */
    public void onStart() {
        publishBinderService("power", new BinderService());
        publishLocalService(PowerManagerInternal.class, new LocalService());
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
    }

    public void onBootPhase(int phase) {
        synchronized (this.mLock) {
            if (phase == 600) {
                try {
                    incrementBootCount();
                } catch (Throwable th) {
                    throw th;
                }
            } else if (phase == 1000) {
                long now = SystemClock.uptimeMillis();
                this.mBootCompleted = true;
                this.mDirty |= 16;
                this.mBatterySaverStateMachine.onBootCompleted();
                userActivityNoUpdateLocked(now, 0, 0, 1000);
                updatePowerStateLocked();
                postAfterBootCompleted(new Runnable() {
                    public void run() {
                        PowerManagerService.this.sendBootCompletedToMonitor();
                    }
                });
                if (!ArrayUtils.isEmpty(this.mBootCompletedRunnables)) {
                    Slog.d(TAG, "Posting " + this.mBootCompletedRunnables.length + " delayed runnables");
                    for (Runnable r : this.mBootCompletedRunnables) {
                        BackgroundThread.getHandler().post(r);
                    }
                }
                this.mBootCompletedRunnables = null;
            }
        }
    }

    public void systemReady(IAppOpsService appOps) {
        synchronized (this.mLock) {
            this.mSystemReady = true;
            this.mAppOps = appOps;
            this.mDreamManager = (DreamManagerInternal) getLocalService(DreamManagerInternal.class);
            this.mDisplayManagerInternal = (DisplayManagerInternal) getLocalService(DisplayManagerInternal.class);
            this.mPolicy = (WindowManagerPolicy) getLocalService(WindowManagerPolicy.class);
            this.mBatteryManagerInternal = (BatteryManagerInternal) getLocalService(BatteryManagerInternal.class);
            PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
            this.mScreenBrightnessSettingMinimum = pm.getMinimumScreenBrightnessSetting();
            this.mScreenBrightnessSettingMaximum = pm.getMaximumScreenBrightnessSetting();
            this.mScreenBrightnessSettingDefault = pm.getDefaultScreenBrightnessSetting();
            SensorManager sensorManager = new SystemSensorManager(this.mContext, this.mHandler.getLooper());
            this.mBatteryStats = BatteryStatsService.getService();
            Notifier notifier = new Notifier(Looper.getMainLooper(), this.mContext, this.mBatteryStats, createSuspendBlockerLocked("PowerManagerService.Broadcasts"), this.mPolicy);
            this.mNotifier = notifier;
            this.mWirelessChargerDetector = new WirelessChargerDetector(sensorManager, createSuspendBlockerLocked("PowerManagerService.WirelessChargerDetector"), this.mHandler);
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            this.mLightsManager = (LightsManager) getLocalService(LightsManager.class);
            this.mAttentionLight = this.mLightsManager.getLight(5);
            this.mBackLight = this.mLightsManager.getLight(0);
            this.mInputManagerInternal = (InputManagerInternal) getLocalService(InputManagerInternal.class);
            this.mDisplayManagerInternal.initPowerManagement(this.mDisplayPowerCallbacks, this.mHandler, sensorManager);
            try {
                ActivityManager.getService().registerUserSwitchObserver(new ForegroundProfileObserver(), TAG);
            } catch (RemoteException e) {
            }
            readConfigurationLocked();
            updateSettingsLocked();
            this.mDirty |= 256;
            updatePowerStateLocked();
        }
        ContentResolver resolver = this.mContext.getContentResolver();
        this.mConstants.start(resolver);
        this.mBatterySaverController.systemReady();
        this.mBatterySaverPolicy.systemReady();
        resolver.registerContentObserver(Settings.Secure.getUriFor("screensaver_enabled"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("screensaver_activate_on_sleep"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("screensaver_activate_on_dock"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor("screen_off_timeout"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("sleep_timeout"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("stay_on_while_plugged_in"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor("screen_brightness_mode"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor("screen_auto_brightness_adj"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("theater_mode_on"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("doze_always_on"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Secure.getUriFor("double_tap_to_wake"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.Global.getUriFor("device_demo_mode"), false, this.mSettingsObserver, 0);
        resolver.registerContentObserver(Settings.System.getUriFor("smart_backlight_enable"), false, this.mSettingsObserver, -1);
        resolver.registerContentObserver(Settings.System.getUriFor(KEY_EYES_PROTECTION), false, this.mSettingsObserver, -2);
        IVrManager vrManager = getBinderService("vrmanager");
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e2) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e2);
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.setPriority(1000);
        this.mContext.registerReceiver(new BatteryReceiver(), filter, null, this.mHandler);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.DREAMING_STARTED");
        filter2.addAction("android.intent.action.DREAMING_STOPPED");
        this.mContext.registerReceiver(new DreamReceiver(), filter2, null, this.mHandler);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(new UserSwitchedReceiver(), filter3, null, this.mHandler);
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("android.intent.action.DOCK_EVENT");
        this.mContext.registerReceiver(new DockReceiver(), filter4, null, this.mHandler);
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
    }

    private void readConfigurationLocked() {
        Resources resources = this.mContext.getResources();
        this.mDecoupleHalAutoSuspendModeFromDisplayConfig = resources.getBoolean(17957002);
        this.mDecoupleHalInteractiveModeFromDisplayConfig = resources.getBoolean(17957003);
        this.mWakeUpWhenPluggedOrUnpluggedConfig = resources.getBoolean(17957055);
        this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig = resources.getBoolean(17956885);
        this.mSuspendWhenScreenOffDueToProximityConfig = resources.getBoolean(17957045);
        this.mDreamsSupportedConfig = resources.getBoolean(17956943);
        this.mDreamsEnabledByDefaultConfig = resources.getBoolean(17956941);
        this.mDreamsActivatedOnSleepByDefaultConfig = resources.getBoolean(17956940);
        this.mDreamsActivatedOnDockByDefaultConfig = resources.getBoolean(17956939);
        this.mDreamsEnabledOnBatteryConfig = resources.getBoolean(17956942);
        this.mDreamsBatteryLevelMinimumWhenPoweredConfig = resources.getInteger(17694785);
        this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = resources.getInteger(17694784);
        this.mDreamsBatteryLevelDrainCutoffConfig = resources.getInteger(17694783);
        this.mDozeAfterScreenOff = resources.getBoolean(17956936);
        this.mMinimumScreenOffTimeoutConfig = (long) resources.getInteger(17694819);
        this.mMaximumScreenDimDurationConfig = (long) resources.getInteger(17694814);
        this.mMaximumScreenDimRatioConfig = resources.getFraction(18022402, 1, 1);
        this.mSupportsDoubleTapWakeConfig = resources.getBoolean(17957036);
    }

    private void updateSettingsLocked() {
        String brightMode;
        ContentResolver resolver = this.mContext.getContentResolver();
        boolean z = true;
        this.mDreamsEnabledSetting = Settings.Secure.getIntForUser(resolver, "screensaver_enabled", this.mDreamsEnabledByDefaultConfig ? 1 : 0, -2) != 0;
        this.mDreamsActivateOnSleepSetting = Settings.Secure.getIntForUser(resolver, "screensaver_activate_on_sleep", this.mDreamsActivatedOnSleepByDefaultConfig ? 1 : 0, -2) != 0;
        this.mDreamsActivateOnDockSetting = Settings.Secure.getIntForUser(resolver, "screensaver_activate_on_dock", this.mDreamsActivatedOnDockByDefaultConfig ? 1 : 0, -2) != 0;
        this.mScreenOffTimeoutSetting = (long) Settings.System.getIntForUser(resolver, "screen_off_timeout", 15000, -2);
        this.mSleepTimeoutSetting = (long) Settings.Secure.getIntForUser(resolver, "sleep_timeout", -1, -2);
        this.mStayOnWhilePluggedInSetting = Settings.Global.getInt(resolver, "stay_on_while_plugged_in", 3);
        this.mTheaterModeEnabled = Settings.Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", 0) == 1;
        this.mAlwaysOnEnabled = this.mAmbientDisplayConfiguration.alwaysOnEnabled(-2);
        if (this.mSupportsDoubleTapWakeConfig) {
            boolean doubleTapWakeEnabled = Settings.Secure.getIntForUser(resolver, "double_tap_to_wake", 0, -2) != 0;
            if (doubleTapWakeEnabled != this.mDoubleTapWakeEnabled) {
                this.mDoubleTapWakeEnabled = doubleTapWakeEnabled;
                nativeSetFeature(1, this.mDoubleTapWakeEnabled ? 1 : 0);
            }
        }
        String retailDemoValue = UserManager.isDeviceInDemoMode(this.mContext) ? POWER_STATE_CHARGE_ENABLE : POWER_STATE_CHARGE_DISABLE;
        if (!retailDemoValue.equals(SystemProperties.get(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED))) {
            SystemProperties.set(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, retailDemoValue);
        }
        int oldScreenBrightnessSetting = this.mScreenBrightnessSetting;
        this.mScreenBrightnessSetting = Settings.System.getIntForUser(resolver, "screen_brightness", this.mScreenBrightnessSettingDefault, this.mCurrentUserId);
        if (oldScreenBrightnessSetting != this.mScreenBrightnessSetting) {
            if (DEBUG) {
                Slog.d(TAG, "mScreenBrightnessSetting=" + this.mScreenBrightnessSetting + ",userid=" + this.mCurrentUserId);
            }
            sendManualBrightnessToMonitor(this.mScreenBrightnessSetting, PackageManagerService.PLATFORM_PACKAGE_NAME);
        }
        int oldScreenBrightnessModeSetting = this.mScreenBrightnessModeSetting;
        if (!this.mFirstBoot || !getRebootAutoModeEnable()) {
            this.mScreenBrightnessModeSetting = Settings.System.getIntForUser(resolver, "screen_brightness_mode", 0, this.mCurrentUserId);
        } else {
            if (Settings.System.getIntForUser(resolver, "screen_brightness_mode", 0, this.mCurrentUserId) == 0) {
                Settings.System.putIntForUser(resolver, "screen_brightness_mode", 1, this.mCurrentUserId);
                Settings.System.putIntForUser(resolver, "hw_screen_brightness_mode_value", 1, this.mCurrentUserId);
                Slog.i(TAG, "RebootAutoMode, set autoBrightnessMode=1, origMode=" + autoBrightnessMode + ",mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting);
            }
            this.mScreenBrightnessModeSetting = 1;
            this.mFirstBoot = false;
        }
        if (oldScreenBrightnessModeSetting != this.mScreenBrightnessModeSetting) {
            if (DEBUG) {
                Slog.d(TAG, "mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting + ",userid=" + this.mCurrentUserId);
            }
            if (this.mScreenBrightnessModeSetting != 0) {
                z = false;
            }
            sendBrightnessModeToMonitor(z, PackageManagerService.PLATFORM_PACKAGE_NAME);
        }
        if (this.mUserFirstBoot) {
            if (Settings.System.getStringForUser(resolver, "hw_screen_brightness_mode_value", -2) == null) {
                Slog.i(TAG, "Firstboot get SCREEN_BRIGHTNESS_MODE=" + brightMode);
                Settings.System.putStringForUser(resolver, "hw_screen_brightness_mode_value", brightMode, -2);
            }
            this.mUserFirstBoot = false;
        }
        this.mSmartBacklightEnableSetting = Settings.System.getIntForUser(resolver, "smart_backlight_enable", 0, 0);
        this.mEyesProtectionMode = Settings.System.getIntForUser(this.mContext.getContentResolver(), KEY_EYES_PROTECTION, 0, -2);
        this.mDirty |= 32;
    }

    private void postAfterBootCompleted(Runnable r) {
        if (this.mBootCompleted) {
            BackgroundThread.getHandler().post(r);
            return;
        }
        Slog.d(TAG, "Delaying runnable until system is booted");
        this.mBootCompletedRunnables = (Runnable[]) ArrayUtils.appendElement(Runnable.class, this.mBootCompletedRunnables, r);
    }

    /* access modifiers changed from: private */
    public void handleSettingsChangedLocked() {
        updateSettingsLocked();
        updatePowerStateLocked();
    }

    private boolean shouldDropLogs(String tag, String packageName, int uid) {
        if (DEBUG_ALL) {
            return false;
        }
        HashSet<String> hashSet = LOG_DROP_SET;
        if (hashSet.contains(tag + uid)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0080 A[SYNTHETIC, Splitter:B:18:0x0080] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00cd A[Catch:{ all -> 0x00c5, all -> 0x013d }] */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x012a A[Catch:{ RemoteException -> 0x012f, all -> 0x0146 }] */
    public void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        Object obj;
        WorkSource workSource;
        int i;
        int index;
        int i2;
        WakeLock wakeLock;
        WakeLock wakeLock2;
        String str = tag;
        String str2 = packageName;
        int i3 = uid;
        if (this.mSystemReady) {
            Object obj2 = this.mLock;
            synchronized (obj2) {
                try {
                    if (!isScreenOrProximityLock(flags)) {
                        if (!DEBUG_SPEW || shouldDropLogs(str, str2, i3)) {
                            workSource = ws;
                            i = pid;
                            index = findWakeLockIndexLocked(lock);
                            if (index < 0) {
                                try {
                                    WakeLock wakeLock3 = this.mWakeLocks.get(index);
                                    if (!wakeLock3.hasSameProperties(flags, str, workSource, i3, i)) {
                                        int i4 = index;
                                        obj = obj2;
                                        notifyWakeLockChangingLocked(wakeLock3, flags, str, str2, i3, pid, ws, historyTag);
                                        wakeLock3.updateProperties(flags, str, str2, ws, historyTag, uid, pid);
                                    } else {
                                        obj = obj2;
                                    }
                                    wakeLock2 = null;
                                    IBinder iBinder = lock;
                                    wakeLock = wakeLock3;
                                    i2 = uid;
                                } catch (Throwable th) {
                                    ex = th;
                                    IBinder iBinder2 = lock;
                                    int i5 = uid;
                                    throw ex;
                                }
                            } else {
                                int i6 = index;
                                obj = obj2;
                                i2 = uid;
                                try {
                                    UidState state = this.mUidState.get(i2);
                                    if (state == null) {
                                        state = new UidState(i2);
                                        state.mProcState = 19;
                                        this.mUidState.put(i2, state);
                                    }
                                    UidState state2 = state;
                                    state2.mNumWakeLocks++;
                                    WakeLock wakeLock4 = new WakeLock(lock, flags, str, str2, ws, historyTag, i2, pid, state2);
                                    WakeLock wakeLock5 = wakeLock4;
                                    try {
                                        lock.linkToDeath(wakeLock5, 0);
                                        this.mWakeLocks.add(wakeLock5);
                                        setWakeLockDisabledStateLocked(wakeLock5);
                                        wakeLock = wakeLock5;
                                        wakeLock2 = 1;
                                    } catch (RemoteException e) {
                                        RemoteException remoteException = e;
                                        throw new IllegalArgumentException("Wake lock is already dead.");
                                    } catch (Throwable th2) {
                                        ex = th2;
                                        throw ex;
                                    }
                                } catch (Throwable th3) {
                                    ex = th3;
                                    IBinder iBinder3 = lock;
                                    throw ex;
                                }
                            }
                            applyWakeLockFlagsOnAcquireLocked(wakeLock, i2);
                            this.mDirty = 1 | this.mDirty;
                            updatePowerStateLocked();
                            if (wakeLock2 != null) {
                                notifyWakeLockAcquiredLocked(wakeLock);
                            }
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("acquire:L=");
                    sb.append(Objects.hashCode(lock));
                    sb.append(",F=0x");
                    sb.append(Integer.toHexString(flags));
                    sb.append(",T=\"");
                    sb.append(str);
                    sb.append("\",N=");
                    sb.append(str2);
                    sb.append(",WS=");
                    workSource = ws;
                    sb.append(workSource);
                    sb.append(",U=");
                    sb.append(i3);
                    sb.append(",P=");
                    i = pid;
                    sb.append(i);
                    Slog.d(TAG_PowerMS, sb.toString());
                    index = findWakeLockIndexLocked(lock);
                    if (index < 0) {
                    }
                    applyWakeLockFlagsOnAcquireLocked(wakeLock, i2);
                    this.mDirty = 1 | this.mDirty;
                    updatePowerStateLocked();
                    if (wakeLock2 != null) {
                    }
                } catch (Throwable th4) {
                    ex = th4;
                    IBinder iBinder4 = lock;
                    obj = obj2;
                    int i7 = i3;
                    throw ex;
                }
            }
        }
    }

    protected static boolean isScreenOrProximityLock(int flags) {
        int i = 65535 & flags;
        if (i == 6 || i == 10 || i == 26 || i == 32) {
            return true;
        }
        return false;
    }

    private static boolean isScreenLock(WakeLock wakeLock) {
        int i = wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI;
        if (i == 6 || i == 10 || i == 26) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void applyWakeLockFlagsOnAcquireLocked(WakeLock wakeLock, int uid) {
        String opPackageName;
        int i;
        if ((wakeLock.mFlags & 268435456) != 0 && isScreenLock(wakeLock)) {
            if (wakeLock.mWorkSource == null || wakeLock.mWorkSource.getName(0) == null) {
                opPackageName = wakeLock.mPackageName;
                if (wakeLock.mWorkSource != null) {
                    i = wakeLock.mWorkSource.get(0);
                } else {
                    i = wakeLock.mOwnerUid;
                }
            } else {
                opPackageName = wakeLock.mWorkSource.getName(0);
                i = wakeLock.mWorkSource.get(0);
            }
            int opUid = i;
            wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), wakeLock.mTag, opUid, opPackageName, opUid);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003a, code lost:
        return;
     */
    public void releaseWakeLockInternal(IBinder lock, int flags) {
        if (this.mSystemReady) {
            synchronized (this.mLock) {
                int index = findWakeLockIndexLocked(lock);
                if (index >= 0) {
                    WakeLock wakeLock = this.mWakeLocks.get(index);
                    if (isScreenOrProximityLock(wakeLock.mFlags) || (DEBUG_SPEW && !shouldDropLogs(wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid))) {
                        Slog.d(TAG_PowerMS, "release:L=" + Objects.hashCode(lock) + ",F=0x" + Integer.toHexString(flags) + ",T=\"" + wakeLock.mTag + "\",N=" + wakeLock.mPackageName + "\",WS=" + wakeLock.mWorkSource + ",U=" + wakeLock.mOwnerUid + ",P=" + wakeLock.mOwnerPid);
                    }
                    if ((flags & 1) != 0) {
                        this.mRequestWaitForNegativeProximity = true;
                    }
                    wakeLock.mLock.unlinkToDeath(wakeLock, 0);
                    removeWakeLockLocked(wakeLock, index);
                } else if (DEBUG_SPEW) {
                    Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
                }
            }
        }
    }

    public void handleWakeLockDeath(WakeLock wakeLock) {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleWakeLockDeath: lock=" + Objects.hashCode(wakeLock.mLock) + " [" + wakeLock.mTag + "]");
            }
            int index = this.mWakeLocks.indexOf(wakeLock);
            if (index >= 0) {
                removeWakeLockLocked(wakeLock, index);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeWakeLockLocked(WakeLock wakeLock, int index) {
        this.mWakeLocks.remove(index);
        UidState state = wakeLock.mUidState;
        state.mNumWakeLocks--;
        if (state.mNumWakeLocks <= 0 && state.mProcState == 19) {
            this.mUidState.remove(state.mUid);
        }
        notifyWakeLockReleasedLocked(wakeLock);
        applyWakeLockFlagsOnReleaseLocked(wakeLock);
        this.mDirty |= 1;
        updatePowerStateLocked();
    }

    private void applyWakeLockFlagsOnReleaseLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & 536870912) != 0 && isScreenLock(wakeLock)) {
            userActivityNoUpdateLocked(SystemClock.uptimeMillis(), 0, 1, wakeLock.mOwnerUid);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c3, code lost:
        return;
     */
    public void updateWakeLockWorkSourceInternal(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        WorkSource workSource = ws;
        synchronized (this.mLock) {
            try {
                int index = findWakeLockIndexLocked(lock);
                if (index < 0) {
                    try {
                        if (DEBUG_Controller) {
                            Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [not found], ws=" + workSource);
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("Wake lock not active: ");
                        try {
                            sb.append(lock);
                            sb.append(" from uid ");
                            sb.append(callingUid);
                            throw new IllegalArgumentException(sb.toString());
                        } catch (Throwable th) {
                            th = th;
                            String str = historyTag;
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        IBinder iBinder = lock;
                        int i = callingUid;
                        String str2 = historyTag;
                        throw th;
                    }
                } else {
                    IBinder iBinder2 = lock;
                    int i2 = callingUid;
                    WakeLock wakeLock = this.mWakeLocks.get(index);
                    if (DEBUG_Controller) {
                        Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + workSource);
                    }
                    if (!wakeLock.hasSameWorkSource(workSource)) {
                        notifyWakeLockChangingLocked(wakeLock, wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, workSource, historyTag);
                        wakeLock.mHistoryTag = historyTag;
                        wakeLock.updateWorkSource(workSource);
                    } else {
                        String str3 = historyTag;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public int findWakeLockIndexLocked(IBinder lock) {
        int count = this.mWakeLocks.size();
        for (int i = 0; i < count; i++) {
            if (this.mWakeLocks.get(i).mLock == lock) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void notifyWakeLockAcquiredLocked(WakeLock wakeLock) {
        if (this.mSystemReady && !wakeLock.mDisabled) {
            wakeLock.mNotifiedAcquired = true;
            this.mNotifier.onWakeLockAcquired(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            restartNofifyLongTimerLocked(wakeLock);
            if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 26 || (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 10) {
                notifyWakeLockToIAware(wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mPackageName, wakeLock.mTag);
            }
            if (this.mHwPowerEx != null) {
                this.mHwPowerEx.notifyWakeLockAcquiredToDubai(wakeLock.mFlags, Objects.hashCode(wakeLock.mLock), wakeLock.mTag, wakeLock.mWorkSource, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mPackageName);
            }
        }
    }

    private void enqueueNotifyLongMsgLocked(long time) {
        this.mNotifyLongScheduled = time;
        Message msg = this.mHandler.obtainMessage(4);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageAtTime(msg, time);
    }

    private void restartNofifyLongTimerLocked(WakeLock wakeLock) {
        wakeLock.mAcquireTime = SystemClock.uptimeMillis();
        if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1 && this.mNotifyLongScheduled == 0) {
            enqueueNotifyLongMsgLocked(wakeLock.mAcquireTime + 60000);
        }
    }

    private void notifyWakeLockLongStartedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && !wakeLock.mDisabled) {
            wakeLock.mNotifiedLong = true;
            this.mNotifier.onLongPartialWakeLockStart(wakeLock.mTag, wakeLock.mOwnerUid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
        }
    }

    private void notifyWakeLockLongFinishedLocked(WakeLock wakeLock) {
        if (wakeLock.mNotifiedLong) {
            wakeLock.mNotifiedLong = false;
            this.mNotifier.onLongPartialWakeLockFinish(wakeLock.mTag, wakeLock.mOwnerUid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyWakeLockChangingLocked(WakeLock wakeLock, int flags, String tag, String packageName, int uid, int pid, WorkSource ws, String historyTag) {
        WakeLock wakeLock2 = wakeLock;
        if (this.mSystemReady && wakeLock2.mNotifiedAcquired) {
            this.mNotifier.onWakeLockChanging(wakeLock2.mFlags, wakeLock2.mTag, wakeLock2.mPackageName, wakeLock2.mOwnerUid, wakeLock2.mOwnerPid, wakeLock2.mWorkSource, wakeLock2.mHistoryTag, flags, tag, packageName, uid, pid, ws, historyTag);
            notifyWakeLockLongFinishedLocked(wakeLock);
            restartNofifyLongTimerLocked(wakeLock);
            if (this.mHwPowerEx != null) {
                this.mHwPowerEx.notifyWakeLockReleasedToDubai(wakeLock2.mFlags, Objects.hashCode(wakeLock2.mLock));
                this.mHwPowerEx.notifyWakeLockAcquiredToDubai(flags, Objects.hashCode(wakeLock2.mLock), tag, ws, uid, pid, packageName);
            }
        }
    }

    private void notifyWakeLockReleasedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && wakeLock.mNotifiedAcquired) {
            wakeLock.mNotifiedAcquired = false;
            wakeLock.mAcquireTime = 0;
            this.mNotifier.onWakeLockReleased(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
            notifyWakeLockLongFinishedLocked(wakeLock);
            if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 26 || (wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 10) {
                notifyWakeLockReleaseToIAware(wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mPackageName, wakeLock.mTag);
            }
            if (this.mHwPowerEx != null) {
                this.mHwPowerEx.notifyWakeLockReleasedToDubai(wakeLock.mFlags, Objects.hashCode(wakeLock.mLock));
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0031, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0033, code lost:
        return true;
     */
    public boolean isWakeLockLevelSupportedInternal(int level) {
        synchronized (this.mLock) {
            boolean z = true;
            if (!(level == 1 || level == 6 || level == 10 || level == 26)) {
                if (level != 32) {
                    if (!(level == 64 || level == 128)) {
                        try {
                            return false;
                        } catch (Throwable th) {
                            throw th;
                        }
                    }
                } else if (!this.mSystemReady || !this.mDisplayManagerInternal.isProximitySensorAvailable()) {
                    z = false;
                }
            }
        }
    }

    private void userActivityFromNative(long eventTime, int event, int flags) {
        userActivityInternal(eventTime, event, flags, 1000);
        resetCustomUserInActivityNotification(eventTime);
    }

    /* access modifiers changed from: protected */
    public void userActivityInternal(long eventTime, int event, int flags, int uid) {
        synchronized (this.mLock) {
            if (userActivityNoUpdateLocked(eventTime, event, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean userActivityNoUpdateLocked(long eventTime, int event, int flags, int uid) {
        if (DEBUG_SPEW && eventTime - this.mLastOneSecActivityTime >= 1000) {
            this.mLastOneSecActivityTime = eventTime;
            Slog.d(TAG, "userActivity:eventTime=" + eventTime + ",event=" + event + ",flags=0x" + Integer.toHexString(flags) + ",uid=" + uid);
        }
        if ((eventTime < this.mLastSleepTime && !this.mAdjustTimeNextUserActivity) || eventTime < this.mLastWakeTime || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        if (this.mAdjustTimeNextUserActivity) {
            this.mAdjustTimeNextUserActivity = false;
        }
        Trace.traceBegin(131072, "userActivity");
        try {
            if (eventTime > this.mLastInteractivePowerHintTime) {
                powerHintInternal(2, 0);
                this.mLastInteractivePowerHintTime = eventTime;
            }
            this.mNotifier.onUserActivity(event, uid);
            if (this.mUserInactiveOverrideFromWindowManager) {
                this.mUserInactiveOverrideFromWindowManager = false;
                this.mOverriddenTimeout = -1;
            }
            if (!(this.mWakefulness == 0 || this.mWakefulness == 3)) {
                if ((flags & 2) == 0) {
                    maybeUpdateForegroundProfileLastActivityLocked(eventTime);
                    if ((flags & 1) != 0) {
                        if (eventTime > this.mLastUserActivityTimeNoChangeLights && eventTime > this.mLastUserActivityTime) {
                            if (mSupportFaceDetect) {
                                unregisterFaceDetect();
                            }
                            this.mLastUserActivityTimeNoChangeLights = eventTime;
                            this.mDirty |= 4;
                            if (event == 1) {
                                this.mDirty |= 4096;
                            }
                            Trace.traceEnd(131072);
                            return true;
                        }
                    } else if (eventTime > this.mLastUserActivityTime) {
                        if (mSupportFaceDetect) {
                            unregisterFaceDetect();
                        }
                        this.mLastUserActivityTime = eventTime;
                        this.mDirty |= 4;
                        if (event == 1) {
                            this.mDirty |= 4096;
                        }
                        Trace.traceEnd(131072);
                        return true;
                    }
                    Trace.traceEnd(131072);
                    return false;
                }
            }
            return false;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    /* access modifiers changed from: private */
    public void maybeUpdateForegroundProfileLastActivityLocked(long eventTime) {
        ProfilePowerState profile = this.mProfilePowerState.get(this.mForegroundProfile);
        if (profile != null && eventTime > profile.mLastUserActivityTime) {
            profile.mLastUserActivityTime = eventTime;
        }
    }

    /* access modifiers changed from: private */
    public void wakeUpInternal(long eventTime, String reason, int uid, String opPackageName, int opUid) {
        synchronized (this.mLock) {
            if (Jlog.isPerfTest()) {
                Jlog.i(2203, "JL_PWRSCRON_PMS_WAKEUPINTERNAL");
            }
            if (wakeUpNoUpdateLocked(eventTime, reason, uid, opPackageName, opUid)) {
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean wakeUpNoUpdateLocked(long eventTime, String reason, int reasonUid, String opPackageName, int opUid) {
        long j;
        long j2 = eventTime;
        String str = reason;
        int i = reasonUid;
        String str2 = opPackageName;
        Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power wakeUpNoUpdateLocked: eventTime=" + j2 + ", uid=" + i + ", pkg=" + str2);
        stopPickupTrunOff();
        boolean reasonPower = "android.policy:POWER".equalsIgnoreCase(str) || "android.policy:POWER_FINGERPRINT".equalsIgnoreCase(str);
        if ((j2 >= this.mLastSleepTime || (this.mLastSleepTimeDuoToFastFP == this.mLastSleepTime && reasonPower)) && ((this.mWakefulness != 1 || this.mBrightnessWaitModeEnabled) && this.mBootCompleted && this.mSystemReady && (!this.mProximityPositive || reasonPower))) {
            if (j2 < this.mLastSleepTime) {
                this.mAdjustTimeNextUserActivity = this.mLastSleepTimeDuoToFastFP == this.mLastSleepTime && reasonPower;
            }
            if (mSupportFaceDetect) {
                startIntelliService();
            }
            IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
            if (iZrHung != null) {
                ZrHungData arg = new ZrHungData();
                arg.putString("addScreenOnInfo", "wakeUpNoUpdateLocked: reason=" + str + ", uid=" + i);
                iZrHung.addInfo(arg);
                if (reasonPower) {
                    iZrHung.start(null);
                }
            }
            Trace.asyncTraceBegin(131072, TRACE_SCREEN_ON, 0);
            Trace.traceBegin(131072, "wakeUp");
            UniPerf.getInstance().uniPerfEvent(4102, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, new int[0]);
            try {
                int i2 = this.mWakefulness;
                if (i2 != 0) {
                    switch (i2) {
                        case 2:
                            Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Waking up from dream (uid=" + i + " reason=" + str + ")...");
                            Jlog.d(6, "JL_PMS_WAKEFULNESS_DREAMING");
                            break;
                        case 3:
                            try {
                                Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Waking up from dozing (uid=" + i + " reason=" + str + ")...");
                                Jlog.d(7, "JL_PMS_WAKEFULNESS_NAPPING");
                                break;
                            } catch (Throwable th) {
                                th = th;
                                IZrHung iZrHung2 = iZrHung;
                                j = 131072;
                                Trace.traceEnd(j);
                                throw th;
                            }
                    }
                } else {
                    Flog.i(NsdService.NativeResponseCode.SERVICE_FOUND, "UL_Power Waking up from sleep (uid=" + i + " reason=" + str + ")...");
                    if (Jlog.isPerfTest()) {
                        Jlog.i(2204, "JL_PWRSCRON_PMS_ASLEEP");
                    }
                    Jlog.d(5, "JL_PMS_WAKEFULNESS_ASLEEP");
                }
                this.mForceDoze = false;
                this.mLastWakeTime = j2;
                setWakefulnessLocked(1, 0);
                disableBrightnessWaitLocked(false);
                this.mNotifier.onWakeUp(str, i, str2, opUid);
                j = 131072;
                IZrHung iZrHung3 = iZrHung;
                try {
                    userActivityNoUpdateLocked(j2, 0, 0, i);
                    Trace.traceEnd(131072);
                    if (mSupportFaceDetect || sSupportFaceRecognition) {
                        PowerManagerHandler powerManagerHandler = this.mHandler;
                        powerManagerHandler.obtainMessage(104, 0, 0, str + "#" + i).sendToTarget();
                    }
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    Trace.traceEnd(j);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                IZrHung iZrHung4 = iZrHung;
                j = 131072;
                Trace.traceEnd(j);
                throw th;
            }
        } else {
            notifyWakeupResult(false);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0060, code lost:
        return;
     */
    public void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
        synchronized (this.mLock) {
            if (isCarMachineHeldWakeLock()) {
                if (this.inVdriveBackLightMode) {
                    this.mBackLight.setMirrorLinkBrightness(255);
                    this.inVdriveBackLightMode = false;
                    this.mBackLight.setMirrorLinkBrightnessStatus(false);
                    if (this.mInputManagerInternal != null) {
                        this.mInputManagerInternal.setMirrorLinkInputStatus(false);
                    }
                } else {
                    this.mBackLight.setMirrorLinkBrightness(0);
                    this.inVdriveBackLightMode = true;
                    this.mBackLight.setMirrorLinkBrightnessStatus(true);
                    if (this.mInputManagerInternal != null) {
                        this.mInputManagerInternal.setMirrorLinkInputStatus(true);
                    }
                }
                Slog.d(TAG, "VCar mode goToSleepInternal inVdriveBackLightMode=" + this.inVdriveBackLightMode);
            } else if (goToSleepNoUpdateLocked(eventTime, reason, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean lightScreenIfBlack() {
        try {
            IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
            if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                HwPCUtils.log(TAG, "screen from OFF to ON");
                pcMgr.setScreenPower(true);
                return true;
            }
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "lightScreenIfBlack " + e);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean goToSleepNoUpdateLocked(long eventTime, int reason, int flags, int uid) {
        int i;
        int i2;
        long j = eventTime;
        int reason2 = reason;
        Flog.i(NsdService.NativeResponseCode.SERVICE_LOST, "UL_Power goToSleepNoUpdateLocked: eventTime=" + j + ", reason=" + reason2 + ", flags=" + i + ", uid=" + i2);
        if (j < this.mLastWakeTime || this.mWakefulness == 0 || this.mWakefulness == 3 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        if (this.mWakefulness == 1 && this.mWakefulnessChanging && reason2 == 4) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "the current screen status is not really screen-on");
            }
            return false;
        }
        IZrHung iZrHung = HwFrameworkFactory.getZrHung("zrhung_wp_screenon_framework");
        if (iZrHung != null) {
            ZrHungData arg = new ZrHungData();
            arg.putString("addScreenOnInfo", "goToSleepNoUpdateLocked: reason=" + reason2 + ", uid=" + i2);
            iZrHung.addInfo(arg);
        }
        if (HwPCUtils.isPcCastModeInServer()) {
            if (102 == reason2) {
                Slog.d(TAG, "Do not gotosleep by incallui when on the phone in PcCastMode");
                return false;
            } else if (4 == reason2 && lightScreenIfBlack()) {
                return false;
            }
        }
        if (mSupportFaceDetect) {
            unregisterFaceDetect();
            stopIntelliService();
        }
        if (!HwFrameworkFactory.getVRSystemServiceManager().isVRDeviceConnected() || reason2 != 4) {
            if (this.mCustomUserActivityTimeout > 0) {
                this.mCustomUserActivityTimeout = 0;
                this.mHandler.removeMessages(105);
            }
            Trace.traceBegin(131072, "goToSleep");
            switch (reason2) {
                case 1:
                    Slog.i(TAG, "UL_Power Going to sleep due to device administration policy (uid " + i2 + ")...");
                    break;
                case 2:
                    Slog.i(TAG, "UL_Power Going to sleep due to screen timeout (uid " + i2 + ")...");
                    if (HwFoldScreenState.isFoldScreenDevice()) {
                        this.mHwPowerEx.notifySleepEx();
                    }
                    Jlog.d(79, "goToSleep due to screen timeout");
                    break;
                case 3:
                    Slog.i(TAG, "UL_Power Going to sleep due to lid switch (uid " + i2 + ")...");
                    Jlog.d(79, "goToSleep due to lid");
                    break;
                case 4:
                    Slog.i(TAG, "UL_Power Going to sleep due to power button (uid " + i2 + ")...");
                    Jlog.d(15, "goToSleep due to powerkey");
                    break;
                case 5:
                    Slog.i(TAG, "UL_Power Going to sleep due to HDMI standby (uid " + i2 + ")...");
                    Jlog.d(79, "goToSleep due to HDMI");
                    break;
                case 6:
                    Slog.i(TAG, "UL_Power Going to sleep due to sleep button (uid " + i2 + ")...");
                    Jlog.d(15, "goToSleep due to sleepbutton");
                    break;
                case 7:
                    Slog.i(TAG, "Going to sleep by an accessibility service request (uid " + i2 + ")...");
                    break;
                default:
                    switch (reason2) {
                        case 100:
                            Slog.i(TAG, "UL_Power Going to sleep due to proximity...");
                            Jlog.d(78, "goToSleep due to proximity");
                            break;
                        case 101:
                            Slog.i(TAG, "UL_Power Going to sleep due to wait brightness timeout...");
                            Jlog.d(79, "gotoToSleep due to wait brightness timeout");
                            break;
                        case 102:
                            Slog.i(TAG, "UL_Power Going to sleep due to called by incallui when on the phone...");
                            Jlog.d(79, "goToSleep due to called by incallui when on the phone");
                            break;
                        default:
                            try {
                                Slog.i(TAG, "UL_Power Going to sleep by application request (uid " + i2 + ")...");
                                Jlog.d(79, "goToSleep by app");
                                reason2 = 0;
                                break;
                            } catch (Throwable th) {
                                Trace.traceEnd(131072);
                                throw th;
                            }
                    }
            }
            this.mLastSleepTime = j;
            this.mSandmanSummoned = true;
            setWakefulnessLocked(3, reason2);
            int numWakeLocks = this.mWakeLocks.size();
            int numWakeLocksCleared = 0;
            for (int i3 = 0; i3 < numWakeLocks; i3++) {
                int i4 = this.mWakeLocks.get(i3).mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI;
                if (i4 == 6 || i4 == 10 || i4 == 26) {
                    numWakeLocksCleared++;
                }
            }
            EventLogTags.writePowerSleepRequested(numWakeLocksCleared);
            if ((i & 1) != 0 || this.mBrightnessWaitModeEnabled) {
                if (this.mBrightnessWaitModeEnabled) {
                    this.mLastSleepTimeDuoToFastFP = j;
                    if (DEBUG) {
                        Slog.d(TAG, "goToSleep mLastSleepTimeDuoToFastFP=" + this.mLastSleepTimeDuoToFastFP);
                    }
                }
                disableBrightnessWaitLocked(false);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                if (!mSupportAod || !this.mForceDoze) {
                    reallyGoToSleepNoUpdateLocked(j, i2);
                }
            }
            Trace.traceEnd(131072);
            return true;
        }
        Slog.d(TAG, "VR mode enabled, skipping gotoSleep by power key.");
        return false;
    }

    /* access modifiers changed from: private */
    public void napInternal(long eventTime, int uid) {
        synchronized (this.mLock) {
            if (napNoUpdateLocked(eventTime, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    private boolean napNoUpdateLocked(long eventTime, int uid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "napNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness != 1 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(131072, "nap");
        try {
            Slog.i(TAG, "Nap time (uid " + uid + ")...");
            this.mSandmanSummoned = true;
            setWakefulnessLocked(2, 0);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    /* JADX INFO: finally extract failed */
    private boolean reallyGoToSleepNoUpdateLocked(long eventTime, int uid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "reallyGoToSleepNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || !this.mBootCompleted || !this.mSystemReady) {
            return false;
        }
        Trace.traceBegin(131072, "reallyGoToSleep");
        try {
            Slog.i(TAG, "Sleeping (uid " + uid + ")...");
            setWakefulnessLocked(0, 2);
            Trace.traceEnd(131072);
            return true;
        } catch (Throwable th) {
            Trace.traceEnd(131072);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setWakefulnessLocked(int wakefulness, int reason) {
        if (this.mWakefulness != wakefulness || this.mBrightnessWaitModeEnabled) {
            this.mWakefulness = wakefulness;
            this.mWakefulnessChanging = true;
            this.mDirty |= 2;
            if (this.mNotifier != null) {
                this.mNotifier.onWakefulnessChangeStarted(wakefulness, reason);
            }
            notifyWakeupResult(true);
            return;
        }
        notifyWakeupResult(false);
    }

    private void logSleepTimeoutRecapturedLocked() {
        long savedWakeTimeMs = this.mOverriddenTimeout - SystemClock.uptimeMillis();
        if (savedWakeTimeMs >= 0) {
            EventLogTags.writePowerSoftSleepRequested(savedWakeTimeMs);
            this.mOverriddenTimeout = -1;
        }
    }

    private void logScreenOn() {
        Trace.asyncTraceEnd(131072, TRACE_SCREEN_ON, 0);
        int latencyMs = (int) (SystemClock.uptimeMillis() - this.mLastWakeTime);
        LogMaker log = new LogMaker(198);
        log.setType(1);
        log.setSubtype(0);
        log.setLatency((long) latencyMs);
        MetricsLogger.action(log);
        EventLogTags.writePowerScreenState(1, 0, 0, 0, latencyMs);
        if (latencyMs >= 200) {
            Slog.w(TAG, "Screen on took " + latencyMs + " ms");
        }
    }

    private void finishWakefulnessChangeIfNeededLocked() {
        if (this.mWakefulnessChanging && this.mDisplayReady && (this.mWakefulness != 3 || (this.mWakeLockSummary & 64) != 0)) {
            if (this.mWakefulness == 3 || this.mWakefulness == 0) {
                logSleepTimeoutRecapturedLocked();
            }
            if (this.mWakefulness == 1) {
                logScreenOn();
            }
            this.mWakefulnessChanging = false;
            this.mNotifier.onWakefulnessChangeFinished();
        }
    }

    /* access modifiers changed from: protected */
    public void updatePowerStateLocked() {
        int dirtyPhase1;
        if (this.mSystemReady && this.mDirty != 0) {
            if (!Thread.holdsLock(this.mLock)) {
                Slog.wtf(TAG, "Power manager lock was not held when calling updatePowerStateLocked");
            }
            Trace.traceBegin(131072, "updatePowerState");
            try {
                updateIsPoweredLocked(this.mDirty);
                updateStayOnLocked(this.mDirty);
                updateScreenBrightnessBoostLocked(this.mDirty);
                long now = SystemClock.uptimeMillis();
                int dirtyPhase2 = 0;
                do {
                    dirtyPhase1 = this.mDirty;
                    dirtyPhase2 |= dirtyPhase1;
                    this.mDirty = 0;
                    updateWakeLockSummaryLocked(dirtyPhase1);
                    updateUserActivitySummaryLocked(now, dirtyPhase1);
                } while (updateWakefulnessLocked(dirtyPhase1));
                updateProfilesLocked(now);
                updateDreamLocked(dirtyPhase2, updateDisplayPowerStateLocked(dirtyPhase2));
                finishWakefulnessChangeIfNeededLocked();
                updateSuspendBlockerLocked();
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    private void updateProfilesLocked(long now) {
        int numProfiles = this.mProfilePowerState.size();
        for (int i = 0; i < numProfiles; i++) {
            ProfilePowerState profile = this.mProfilePowerState.valueAt(i);
            if (isProfileBeingKeptAwakeLocked(profile, now)) {
                profile.mLockingNotified = false;
            } else if (!profile.mLockingNotified) {
                profile.mLockingNotified = true;
                this.mNotifier.onProfileTimeout(profile.mUserId);
            }
        }
    }

    private boolean isProfileBeingKeptAwakeLocked(ProfilePowerState profile, long now) {
        return profile.mLastUserActivityTime + profile.mScreenOffTimeout > now || (profile.mWakeLockSummary & 32) != 0 || (this.mProximityPositive && (profile.mWakeLockSummary & 16) != 0);
    }

    private void updateIsPoweredLocked(int dirty) {
        if ((dirty & 256) != 0) {
            boolean wasPowered = this.mIsPowered;
            int oldPlugType = this.mPlugType;
            boolean z = this.mBatteryLevelLow;
            this.mIsPowered = this.mBatteryManagerInternal.isPowered(7);
            this.mPlugType = this.mBatteryManagerInternal.getPlugType();
            this.mBatteryLevel = this.mBatteryManagerInternal.getBatteryLevel();
            this.mBatteryLevelLow = this.mBatteryManagerInternal.getBatteryLevelLow();
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateIsPoweredLocked: wasPowered=" + wasPowered + ", mIsPowered=" + this.mIsPowered + ", oldPlugType=" + oldPlugType + ", mPlugType=" + this.mPlugType + ", mBatteryLevel=" + this.mBatteryLevel);
            }
            if (!(wasPowered == this.mIsPowered && oldPlugType == this.mPlugType)) {
                this.mDirty |= 64;
                boolean dockedOnWirelessCharger = this.mWirelessChargerDetector.update(this.mIsPowered, this.mPlugType);
                long now = SystemClock.uptimeMillis();
                if (shouldWakeUpWhenPluggedOrUnpluggedLocked(wasPowered, oldPlugType, dockedOnWirelessCharger)) {
                    wakeUpNoUpdateLocked(now, "android.server.power:POWER", 1000, this.mContext.getOpPackageName(), 1000);
                }
                userActivityNoUpdateLocked(now, 0, 0, 1000);
                if (this.mBootCompleted) {
                    if (this.mIsPowered && !BatteryManager.isPlugWired(oldPlugType) && BatteryManager.isPlugWired(this.mPlugType)) {
                        this.mNotifier.onWiredChargingStarted();
                    } else if (dockedOnWirelessCharger) {
                        this.mNotifier.onWirelessChargingStarted(this.mBatteryLevel);
                    }
                }
            }
            this.mBatterySaverStateMachine.setBatteryStatus(this.mIsPowered, this.mBatteryLevel, this.mBatteryLevelLow);
        }
    }

    private boolean shouldWakeUpWhenPluggedOrUnpluggedLocked(boolean wasPowered, int oldPlugType, boolean dockedOnWirelessCharger) {
        if (!this.mWakeUpWhenPluggedOrUnpluggedConfig) {
            return false;
        }
        if (wasPowered && !this.mIsPowered && oldPlugType == 4) {
            Slog.d(TAG, "Need to wake when undocked from wireless charger");
        }
        if (!wasPowered && this.mIsPowered && this.mPlugType == 4 && !dockedOnWirelessCharger) {
            Slog.d(TAG, "Need to wake when docked on wireless charger");
        }
        if (this.mIsPowered && this.mWakefulness == 2) {
            return false;
        }
        if (this.mTheaterModeEnabled && !this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig) {
            return false;
        }
        if (!this.mAlwaysOnEnabled || this.mWakefulness != 3) {
            return true;
        }
        return false;
    }

    private void updateStayOnLocked(int dirty) {
        if ((dirty & 288) != 0) {
            boolean wasStayOn = this.mStayOn;
            if (this.mStayOnWhilePluggedInSetting == 0 || isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
                this.mStayOn = false;
            } else {
                this.mStayOn = this.mBatteryManagerInternal.isPowered(this.mStayOnWhilePluggedInSetting);
            }
            if (this.mStayOn != wasStayOn) {
                this.mDirty |= 128;
            }
        }
    }

    private void updateWakeLockSummaryLocked(int dirty) {
        if ((dirty & 3) != 0) {
            this.mWakeLockSummary = 0;
            int numProfiles = this.mProfilePowerState.size();
            for (int i = 0; i < numProfiles; i++) {
                this.mProfilePowerState.valueAt(i).mWakeLockSummary = 0;
            }
            int numWakeLocks = this.mWakeLocks.size();
            for (int i2 = 0; i2 < numWakeLocks; i2++) {
                WakeLock wakeLock = this.mWakeLocks.get(i2);
                int wakeLockFlags = getWakeLockSummaryFlags(wakeLock);
                this.mWakeLockSummary |= wakeLockFlags;
                for (int j = 0; j < numProfiles; j++) {
                    ProfilePowerState profile = this.mProfilePowerState.valueAt(j);
                    if (wakeLockAffectsUser(wakeLock, profile.mUserId)) {
                        profile.mWakeLockSummary |= wakeLockFlags;
                    }
                }
            }
            this.mWakeLockSummary = adjustWakeLockSummaryLocked(this.mWakeLockSummary);
            for (int i3 = 0; i3 < numProfiles; i3++) {
                ProfilePowerState profile2 = this.mProfilePowerState.valueAt(i3);
                profile2.mWakeLockSummary = adjustWakeLockSummaryLocked(profile2.mWakeLockSummary);
            }
            if (DEBUG_Controller != 0) {
                Slog.d(TAG, "updateWakeLockSummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary));
            }
        }
    }

    private int adjustWakeLockSummaryLocked(int wakeLockSummary) {
        if (this.mWakefulness != 3) {
            wakeLockSummary &= -193;
        }
        if (this.mWakefulness == 0 || (wakeLockSummary & 64) != 0) {
            wakeLockSummary &= -15;
        }
        if ((wakeLockSummary & 6) != 0) {
            if (this.mWakefulness == 1) {
                wakeLockSummary |= 33;
            } else if (this.mWakefulness == 2) {
                wakeLockSummary |= 1;
            }
        }
        if ((wakeLockSummary & 128) != 0) {
            return wakeLockSummary | 1;
        }
        return wakeLockSummary;
    }

    private int getWakeLockSummaryFlags(WakeLock wakeLock) {
        int i = wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI;
        if (i != 1) {
            if (i == 6) {
                return 4;
            }
            if (i == 10) {
                return 2;
            }
            if (i == 26) {
                return 10;
            }
            if (i == 32) {
                return 16;
            }
            if (i == 64) {
                return 64;
            }
            if (i == 128) {
                return 128;
            }
        } else if (!wakeLock.mDisabled) {
            return 1;
        }
        return 0;
    }

    private boolean wakeLockAffectsUser(WakeLock wakeLock, int userId) {
        boolean z = false;
        if (wakeLock.mWorkSource != null) {
            for (int k = 0; k < wakeLock.mWorkSource.size(); k++) {
                if (userId == UserHandle.getUserId(wakeLock.mWorkSource.get(k))) {
                    return true;
                }
            }
            ArrayList<WorkSource.WorkChain> workChains = wakeLock.mWorkSource.getWorkChains();
            if (workChains != null) {
                for (int k2 = 0; k2 < workChains.size(); k2++) {
                    if (userId == UserHandle.getUserId(workChains.get(k2).getAttributionUid())) {
                        return true;
                    }
                }
            }
        }
        if (userId == UserHandle.getUserId(wakeLock.mOwnerUid)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void checkForLongWakeLocks() {
        synchronized (this.mLock) {
            long now = SystemClock.uptimeMillis();
            this.mNotifyLongDispatched = now;
            long when = now - 60000;
            long nextCheckTime = JobStatus.NO_LATEST_RUNTIME;
            int numWakeLocks = this.mWakeLocks.size();
            for (int i = 0; i < numWakeLocks; i++) {
                WakeLock wakeLock = this.mWakeLocks.get(i);
                if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1 && wakeLock.mNotifiedAcquired && !wakeLock.mNotifiedLong) {
                    if (wakeLock.mAcquireTime < when) {
                        notifyWakeLockLongStartedLocked(wakeLock);
                    } else {
                        long checkTime = wakeLock.mAcquireTime + 60000;
                        if (checkTime < nextCheckTime) {
                            nextCheckTime = checkTime;
                        }
                    }
                }
            }
            this.mNotifyLongScheduled = 0;
            this.mHandler.removeMessages(4);
            if (nextCheckTime != JobStatus.NO_LATEST_RUNTIME) {
                this.mNotifyLongNextCheck = nextCheckTime;
                enqueueNotifyLongMsgLocked(nextCheckTime);
            } else {
                this.mNotifyLongNextCheck = 0;
            }
        }
    }

    private void updateUserActivitySummaryLocked(long now, int dirty) {
        long nextTimeout;
        long nextTimeout2;
        boolean startNoChangeLights;
        if ((dirty & 39) != 0) {
            this.mHandler.removeMessages(1);
            if (mSupportFaceDetect) {
                this.mHandler.removeMessages(103);
            }
            if (this.mWakefulness == 1 || this.mWakefulness == 2 || this.mWakefulness == 3) {
                long sleepTimeout = getSleepTimeoutLocked();
                long screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
                long screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
                boolean userInactiveOverride = this.mUserInactiveOverrideFromWindowManager;
                long screenOffTimeout2 = screenOffTimeout;
                long nextProfileTimeout = getNextProfileTimeoutLocked(now);
                this.mUserActivitySummary = 0;
                boolean startNoChangeLights2 = false;
                long nextProfileTimeout2 = nextProfileTimeout;
                if (this.mLastUserActivityTime >= this.mLastWakeTime) {
                    nextTimeout2 = (this.mLastUserActivityTime + screenOffTimeout2) - screenDimDuration;
                    if (now < nextTimeout2) {
                        this.mUserActivitySummary = 1;
                    } else {
                        long nextTimeout3 = this.mLastUserActivityTime + screenOffTimeout2;
                        if (now < nextTimeout3) {
                            this.mUserActivitySummary = 2;
                        }
                        nextTimeout2 = nextTimeout3;
                    }
                } else {
                    nextTimeout2 = 0;
                }
                if (this.mUserActivitySummary == 0 && this.mLastUserActivityTimeNoChangeLights >= this.mLastWakeTime) {
                    nextTimeout2 = this.mLastUserActivityTimeNoChangeLights + screenOffTimeout2;
                    if (now < nextTimeout2) {
                        if (this.mDisplayPowerRequest.policy == 3 || this.mDisplayPowerRequest.policy == 4) {
                            this.mUserActivitySummary = 1;
                            startNoChangeLights = true;
                        } else if (this.mDisplayPowerRequest.policy == 2) {
                            this.mUserActivitySummary = 2;
                            startNoChangeLights = true;
                        }
                        startNoChangeLights2 = startNoChangeLights;
                    }
                }
                if (this.mUserActivitySummary == 0) {
                    if (sleepTimeout >= 0) {
                        long anyUserActivity = Math.max(this.mLastUserActivityTime, this.mLastUserActivityTimeNoChangeLights);
                        if (anyUserActivity >= this.mLastWakeTime) {
                            nextTimeout2 = anyUserActivity + sleepTimeout;
                            if (now < nextTimeout2) {
                                this.mUserActivitySummary = 4;
                            }
                        }
                    } else {
                        this.mUserActivitySummary = 4;
                        nextTimeout2 = -1;
                    }
                }
                if (this.mUserActivitySummary != 4 && userInactiveOverride) {
                    if ((this.mUserActivitySummary & 3) != 0 && nextTimeout2 >= now && this.mOverriddenTimeout == -1) {
                        this.mOverriddenTimeout = nextTimeout2;
                    }
                    this.mUserActivitySummary = 4;
                    nextTimeout2 = -1;
                }
                if (nextProfileTimeout2 > 0) {
                    nextTimeout = Math.min(nextTimeout2, nextProfileTimeout2);
                } else {
                    nextTimeout = nextTimeout2;
                }
                if (this.mUserActivitySummary != 0 && nextTimeout >= 0) {
                    scheduleUserInactivityTimeout(nextTimeout);
                    boolean z = userInactiveOverride;
                    if (needFaceDetect(nextTimeout, now, startNoChangeLights2)) {
                        Message msg1 = this.mHandler.obtainMessage(103);
                        msg1.setAsynchronous(true);
                        this.mHandler.sendMessageAtTime(msg1, nextTimeout - 1000);
                    }
                }
                boolean z2 = startNoChangeLights2;
            } else {
                this.mUserActivitySummary = 0;
                nextTimeout = 0;
            }
            if (DEBUG_Controller) {
                Slog.d(TAG, "updateUserActivitySummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", nextTimeout=" + TimeUtils.formatUptime(nextTimeout));
            }
        }
    }

    private void resetCustomUserInActivityNotification(long eventTime) {
        if (this.mCustomUserActivityTimeout > 0) {
            this.mHandler.removeMessages(105);
            scheduleCustomUserInactivityTimeout(((long) this.mCustomUserActivityTimeout) + eventTime);
        }
    }

    private void scheduleCustomUserInactivityTimeout(long timeMs) {
        Message msg = this.mHandler.obtainMessage(105);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageAtTime(msg, timeMs);
    }

    private void scheduleUserInactivityTimeout(long timeMs) {
        Message msg = this.mHandler.obtainMessage(1);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageAtTime(msg, timeMs);
    }

    private long getNextProfileTimeoutLocked(long now) {
        long nextTimeout = -1;
        int numProfiles = this.mProfilePowerState.size();
        for (int i = 0; i < numProfiles; i++) {
            ProfilePowerState profile = this.mProfilePowerState.valueAt(i);
            long timeout = profile.mLastUserActivityTime + profile.mScreenOffTimeout;
            if (timeout > now && (nextTimeout == -1 || timeout < nextTimeout)) {
                nextTimeout = timeout;
            }
        }
        return nextTimeout;
    }

    /* access modifiers changed from: private */
    public void handleUserActivityTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "UL_Power handleUserActivityTimeout");
            }
            this.mDirty |= 4;
            this.mScreenTimeoutFlag = true;
            updatePowerStateLocked();
            this.mScreenTimeoutFlag = false;
        }
        if ((this.mWakeLockSummary & 16) != 0 && (this.mWakeLockSummary & 32) == 0) {
            Slog.d(TAG, "stopWakeLockedSensor");
            stopWakeLockedSensor(true);
        }
    }

    /* access modifiers changed from: private */
    public void handleCustomUserInActivityTimeout() {
        Slog.i(TAG, "handleCustomUserInActivityTimeout send broadcast !");
        Intent intent = new Intent("android.intent.action.USER_INACTIVITY_NOTIFICATION");
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.USER_ACTIVITY");
        this.mCustomUserActivityTimeout = 0;
    }

    private long getSleepTimeoutLocked() {
        long timeout = this.mSleepTimeoutSetting;
        if (timeout <= 0) {
            return -1;
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private long getScreenOffTimeoutLocked(long sleepTimeout) {
        long timeout = this.mScreenOffTimeoutSetting;
        if (isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
            timeout = Math.min(timeout, this.mMaximumScreenOffTimeoutFromDeviceAdmin);
        }
        if (this.mUserActivityTimeoutOverrideFromWindowManager >= 0) {
            timeout = Math.min(timeout, this.mUserActivityTimeoutOverrideFromWindowManager);
        }
        if (sleepTimeout >= 0) {
            timeout = Math.min(timeout, sleepTimeout);
        }
        if (getAdjustedMaxTimeout((int) timeout, (int) this.mMinimumScreenOffTimeoutConfig) > 0) {
            return Math.min(timeout, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private long getScreenDimDurationLocked(long screenOffTimeout) {
        int maxDimRatio = Integer.parseInt(SystemProperties.get("sys.aps.maxDimRatio", "-1"));
        long dimDuration = -1;
        if (maxDimRatio != -1) {
            dimDuration = (long) HwFrameworkFactory.getHwApsImpl().getCustScreenDimDurationLocked((int) screenOffTimeout);
        }
        if (dimDuration == -1 || maxDimRatio == -1) {
            return Math.min(this.mMaximumScreenDimDurationConfig, (long) (((float) screenOffTimeout) * this.mMaximumScreenDimRatioConfig));
        }
        return dimDuration;
    }

    private boolean updateWakefulnessLocked(int dirty) {
        if ((dirty & 1687) == 0 || this.mWakefulness != 1 || !isItBedTimeYetLocked()) {
            return false;
        }
        if (DEBUG_SPEW) {
            Slog.d(TAG, "UL_Power updateWakefulnessLocked: Bed time...");
        }
        long time = SystemClock.uptimeMillis();
        if (shouldNapAtBedTimeLocked()) {
            return napNoUpdateLocked(time, 1000);
        }
        return goToSleepNoUpdateLocked(time, 2, 0, 1000);
    }

    private boolean shouldNapAtBedTimeLocked() {
        return this.mDreamsActivateOnSleepSetting || (this.mDreamsActivateOnDockSetting && this.mDockState != 0);
    }

    private boolean isItBedTimeYetLocked() {
        boolean keepAwake = isBeingKeptAwakeLocked();
        if (this.mScreenTimeoutFlag && keepAwake) {
            Slog.i(TAG, "UL_Power Screen timeout occured. mStayOn = " + this.mStayOn + ", mProximityPositive = " + this.mProximityPositive + ", mWakeLockSummary = 0x" + Integer.toHexString(this.mWakeLockSummary) + ", mUserActivitySummary = 0x" + Integer.toHexString(this.mUserActivitySummary) + ", mScreenBrightnessBoostInProgress = " + this.mScreenBrightnessBoostInProgress);
            if ((this.mWakeLockSummary & 32) != 0) {
                Slog.i(TAG, "Wake Locks: size = " + this.mWakeLocks.size());
                Iterator<WakeLock> it = this.mWakeLocks.iterator();
                while (it.hasNext()) {
                    Slog.i(TAG, "WakeLock:" + it.next().toString());
                }
            }
        }
        if (mSupportFaceDetect && this.mScreenTimeoutFlag && ((this.mWakeLockSummary & 32) != 0 || this.mStayOn)) {
            unregisterFaceDetect();
        }
        return this.mBootCompleted && !keepAwake;
    }

    private boolean isBeingKeptAwakeLocked() {
        return this.mStayOn || (this.mProximityPositive && !isPhoneHeldWakeLock()) || (this.mWakeLockSummary & 32) != 0 || (this.mUserActivitySummary & 3) != 0 || this.mScreenBrightnessBoostInProgress;
    }

    private void updateDreamLocked(int dirty, boolean displayBecameReady) {
        if (((dirty & 1015) != 0 || displayBecameReady) && this.mDisplayReady) {
            scheduleSandmanLocked();
        }
    }

    /* access modifiers changed from: private */
    public void scheduleSandmanLocked() {
        if (!this.mSandmanScheduled) {
            this.mSandmanScheduled = true;
            Message msg = this.mHandler.obtainMessage(2);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b8, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0102, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0104, code lost:
        if (r4 == false) goto L_0x010b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0106, code lost:
        r14.mDreamManager.stopDream(false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x010b, code lost:
        return;
     */
    public void handleSandman() {
        int wakefulness;
        boolean z;
        boolean startDreaming;
        boolean isDreaming;
        synchronized (this.mLock) {
            this.mSandmanScheduled = false;
            wakefulness = this.mWakefulness;
            z = true;
            if (!this.mSandmanSummoned || !this.mDisplayReady) {
                startDreaming = false;
            } else {
                if (!canDreamLocked()) {
                    if (!canDozeLocked()) {
                        startDreaming = false;
                        this.mSandmanSummoned = false;
                    }
                }
                startDreaming = true;
                this.mSandmanSummoned = false;
            }
        }
        if (this.mDreamManager != null) {
            if (startDreaming) {
                this.mDreamManager.stopDream(false);
                DreamManagerInternal dreamManagerInternal = this.mDreamManager;
                if (wakefulness != 3) {
                    z = false;
                }
                dreamManagerInternal.startDream(z);
            }
            isDreaming = this.mDreamManager.isDreaming();
        } else {
            isDreaming = false;
        }
        boolean isDreaming2 = isDreaming;
        synchronized (this.mLock) {
            if (startDreaming && isDreaming2) {
                this.mBatteryLevelWhenDreamStarted = this.mBatteryLevel;
                if (wakefulness == 3) {
                    Slog.i(TAG, "Dozing...");
                } else {
                    Slog.i(TAG, "Dreaming...");
                }
            }
            if (!this.mSandmanSummoned) {
                if (this.mWakefulness == wakefulness) {
                    if (wakefulness == 2) {
                        if (isDreaming2 && canDreamLocked()) {
                            if (this.mDreamsBatteryLevelDrainCutoffConfig >= 0 && this.mBatteryLevel < this.mBatteryLevelWhenDreamStarted - this.mDreamsBatteryLevelDrainCutoffConfig && !isBeingKeptAwakeLocked()) {
                                Slog.i(TAG, "Stopping dream because the battery appears to be draining faster than it is charging.  Battery level when dream started: " + this.mBatteryLevelWhenDreamStarted + "%.  Battery level now: " + this.mBatteryLevel + "%.");
                            } else if (mSupportFaceDetect) {
                                unregisterFaceDetect();
                            }
                        }
                        if (isItBedTimeYetLocked()) {
                            goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 2, 0, 1000);
                            updatePowerStateLocked();
                        } else {
                            wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:DREAM", 1000, this.mContext.getOpPackageName(), 1000);
                            updatePowerStateLocked();
                        }
                    } else if (wakefulness == 3) {
                        if (!isDreaming2) {
                            if (!mSupportAod || !this.mForceDoze) {
                                reallyGoToSleepNoUpdateLocked(SystemClock.uptimeMillis(), 1000);
                                updatePowerStateLocked();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canDreamLocked() {
        if (this.mWakefulness != 2 || !this.mDreamsSupportedConfig || !this.mDreamsEnabledSetting || !this.mDisplayPowerRequest.isBrightOrDim() || this.mDisplayPowerRequest.isVr() || (this.mUserActivitySummary & 7) == 0 || !this.mBootCompleted) {
            return false;
        }
        if (!isBeingKeptAwakeLocked()) {
            if (!this.mIsPowered && !this.mDreamsEnabledOnBatteryConfig) {
                return false;
            }
            if (!this.mIsPowered && this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig >= 0 && this.mBatteryLevel < this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig) {
                return false;
            }
            if (this.mIsPowered && this.mDreamsBatteryLevelMinimumWhenPoweredConfig >= 0 && this.mBatteryLevel < this.mDreamsBatteryLevelMinimumWhenPoweredConfig) {
                return false;
            }
        }
        return true;
    }

    private boolean canDozeLocked() {
        return this.mWakefulness == 3;
    }

    private boolean updateDisplayPowerStateLocked(int dirty) {
        boolean autoBrightness;
        int screenBrightnessOverride;
        boolean oldDisplayReady = this.mDisplayReady;
        if ((dirty & 30783) != 0) {
            int newScreenState = getDesiredScreenPolicyLocked();
            boolean eyeprotectionMode = this.mEyesProtectionMode == 1 || this.mEyesProtectionMode == 3;
            if (newScreenState == 3 && this.mDisplayPowerRequest.policy == 0 && !eyeprotectionMode) {
                Slog.d(TAG, "setColorTemperatureAccordingToSetting");
                setColorTemperatureAccordingToSetting();
            }
            this.mDisplayPowerRequest.policy = newScreenState;
            if (this.mIsCoverModeEnabled) {
                screenBrightnessOverride = getCoverModeBrightness();
                updateAutoBrightnessDBforSeekbar(screenBrightnessOverride, newScreenState);
                autoBrightness = false;
            } else if (isValidBrightness(this.mScreenBrightnessOverrideFromWindowManager)) {
                autoBrightness = false;
                screenBrightnessOverride = this.mScreenBrightnessOverrideFromWindowManager;
                updateAutoBrightnessDBforSeekbar(screenBrightnessOverride, newScreenState);
            } else {
                autoBrightness = this.mScreenBrightnessModeSetting == 1;
                screenBrightnessOverride = -1;
            }
            boolean keyguardLocked = getKeyguardLockedStatus();
            if (this.mKeyguardLocked != keyguardLocked) {
                this.mDisplayManagerInternal.setKeyguardLockedStatus(keyguardLocked);
            }
            this.mKeyguardLocked = keyguardLocked;
            boolean updateBacklightBrightnessFlag = false;
            if (this.mScreenBrightnessOverrideFromWindowManager > 255 || this.mScreenBrightnessOverrideFromWindowManager == -1) {
                updateBacklightBrightnessFlag = this.mBacklightBrightness.updateBacklightBrightness(this.mScreenBrightnessOverrideFromWindowManager);
            }
            this.mDisplayManagerInternal.setBacklightBrightness(this.mBacklightBrightness);
            if (this.mUpdateBacklightBrightnessFlag != updateBacklightBrightnessFlag) {
                this.mDisplayManagerInternal.setCameraModeBrightnessLineEnable(updateBacklightBrightnessFlag);
            }
            this.mUpdateBacklightBrightnessFlag = updateBacklightBrightnessFlag;
            if (screenBrightnessOverride >= 0) {
                screenBrightnessOverride = Math.max(Math.min(screenBrightnessOverride, this.mScreenBrightnessSettingMaximum), this.mScreenBrightnessSettingMinimum);
            }
            this.mDisplayPowerRequest.screenBrightnessOverride = screenBrightnessOverride;
            if (this.mNotifier.getBrightnessModeChangeNoClearOffset()) {
                this.mDisplayManagerInternal.setPoweroffModeChangeAutoEnable(true);
                this.mNotifier.setBrightnessModeChangeNoClearOffset(false);
            }
            this.mDisplayPowerRequest.useAutoBrightness = autoBrightness;
            this.mDisplayPowerRequest.useSmartBacklight = this.mSmartBacklightEnableSetting == 1;
            this.mDisplayPowerRequest.screenAutoBrightness = this.mTemporaryScreenAutoBrightnessSettingOverride;
            this.mDisplayPowerRequest.useProximitySensor = shouldUseProximitySensorLocked();
            if (this.mDisplayPowerRequest.useProximitySensor) {
                this.mDisplayPowerRequest.useProximitySensorbyPhone = isPhoneHeldWakeLock() && !HwPCUtils.isPcCastModeInServer();
            }
            this.mDisplayPowerRequest.boostScreenBrightness = shouldBoostScreenBrightness();
            this.mDisplayPowerRequest.userId = this.mCurrentUserId;
            updatePowerRequestFromBatterySaverPolicy(this.mDisplayPowerRequest);
            if (this.mDisplayPowerRequest.policy == 1) {
                this.mDisplayPowerRequest.dozeScreenState = this.mDozeScreenStateOverrideFromDreamManager;
                if ((this.mWakeLockSummary & 128) != 0 && !this.mDrawWakeLockOverrideFromSidekick) {
                    if (this.mDisplayPowerRequest.dozeScreenState == 4) {
                        this.mDisplayPowerRequest.dozeScreenState = 3;
                    }
                    if (this.mDisplayPowerRequest.dozeScreenState == 6) {
                        this.mDisplayPowerRequest.dozeScreenState = 2;
                    }
                }
                this.mDisplayPowerRequest.dozeScreenBrightness = this.mDozeScreenBrightnessOverrideFromDreamManager;
            } else {
                this.mDisplayPowerRequest.dozeScreenState = 0;
                this.mDisplayPowerRequest.dozeScreenBrightness = -1;
            }
            this.mDisplayPowerRequest.brightnessWaitMode = this.mBrightnessWaitModeEnabled;
            this.mDisplayPowerRequest.brightnessWaitRet = this.mBrightnessWaitRet;
            this.mDisplayPowerRequest.skipWaitKeyguardDismiss = this.mSkipWaitKeyguardDismiss;
            this.mDisplayReady = this.mDisplayManagerInternal.requestPowerState(this.mDisplayPowerRequest, this.mRequestWaitForNegativeProximity);
            this.mRequestWaitForNegativeProximity = false;
            if ((dirty & 4096) != 0) {
                sQuiescent = false;
            }
            if (DEBUG_Controller) {
                Slog.d(TAG, "ready=" + this.mDisplayReady + ",policy=" + this.mDisplayPowerRequest.policy + ",wkful=" + this.mWakefulness + ",wlsum=0x" + Integer.toHexString(this.mWakeLockSummary) + ",uasum=0x" + Integer.toHexString(this.mUserActivitySummary) + ",boostinprogress=" + this.mScreenBrightnessBoostInProgress + ",waitmodeenable=" + this.mBrightnessWaitModeEnabled + ",mode=" + this.mDisplayPowerRequest.useAutoBrightness + ",userId=" + this.mDisplayPowerRequest.userId + ",mIsVrModeEnabled=" + this.mIsVrModeEnabled + ",sQuiescent=" + sQuiescent);
            }
        }
        if (this.mDisplayReady == 0 || oldDisplayReady) {
            return false;
        }
        return true;
    }

    private void updateScreenBrightnessBoostLocked(int dirty) {
        if ((dirty & 2048) != 0 && this.mScreenBrightnessBoostInProgress) {
            long now = SystemClock.uptimeMillis();
            this.mHandler.removeMessages(3);
            if (this.mLastScreenBrightnessBoostTime > this.mLastSleepTime) {
                long boostTimeout = this.mLastScreenBrightnessBoostTime + 5000;
                if (boostTimeout > now) {
                    Message msg = this.mHandler.obtainMessage(3);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageAtTime(msg, boostTimeout);
                    return;
                }
            }
            this.mScreenBrightnessBoostInProgress = false;
            this.mNotifier.onScreenBrightnessBoostChanged();
            userActivityNoUpdateLocked(now, 0, 0, 1000);
        }
    }

    private boolean shouldBoostScreenBrightness() {
        return !this.mIsVrModeEnabled && this.mScreenBrightnessBoostInProgress;
    }

    private static boolean isValidBrightness(int value) {
        return value >= 0 && value <= 255;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getDesiredScreenPolicyLocked() {
        if (this.mWakefulness == 0 || sQuiescent) {
            return 0;
        }
        if (this.mWakefulness == 3) {
            if ((this.mWakeLockSummary & 64) != 0) {
                return 1;
            }
            if (this.mDozeAfterScreenOff) {
                return 0;
            }
        }
        if (this.mIsVrModeEnabled) {
            return 4;
        }
        if ((this.mWakeLockSummary & 2) != 0 || (this.mUserActivitySummary & 1) != 0 || !this.mBootCompleted || this.mScreenBrightnessBoostInProgress) {
            return 3;
        }
        return 2;
    }

    /* access modifiers changed from: protected */
    public void notifyHwBrightnessCallbacks(String what, int arg1, int arg2, Bundle data) {
    }

    private boolean shouldUseProximitySensorLocked() {
        return !this.mIsVrModeEnabled && (this.mWakeLockSummary & 16) != 0;
    }

    private void updateSuspendBlockerLocked() {
        boolean needWakeLockSuspendBlocker = (this.mWakeLockSummary & 1) != 0;
        boolean needDisplaySuspendBlocker = needDisplaySuspendBlockerLocked();
        boolean autoSuspend = !needDisplaySuspendBlocker;
        boolean interactive = this.mDisplayPowerRequest.isBrightOrDim();
        if (!autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(false);
        }
        if (needWakeLockSuspendBlocker && !this.mHoldingWakeLockSuspendBlocker) {
            this.mWakeLockSuspendBlocker.acquire();
            this.mHoldingWakeLockSuspendBlocker = true;
        }
        if (needDisplaySuspendBlocker && !this.mHoldingDisplaySuspendBlocker) {
            Slog.i(TAG, "need acquire DisplaySuspendBlocker");
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
        }
        if (this.mDecoupleHalInteractiveModeFromDisplayConfig && (interactive || this.mDisplayReady)) {
            setHalInteractiveModeLocked(interactive);
        }
        if (!needWakeLockSuspendBlocker && this.mHoldingWakeLockSuspendBlocker) {
            this.mWakeLockSuspendBlocker.release();
            this.mHoldingWakeLockSuspendBlocker = false;
        }
        if (!needDisplaySuspendBlocker && this.mHoldingDisplaySuspendBlocker) {
            Slog.i(TAG, "need release DisplaySuspendBlocker");
            this.mDisplaySuspendBlocker.release();
            this.mHoldingDisplaySuspendBlocker = false;
        }
        if (autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(true);
        }
    }

    private boolean needDisplaySuspendBlockerLocked() {
        if (!this.mDisplayReady) {
            return true;
        }
        if ((!this.mDisplayPowerRequest.isBrightOrDim() || (this.mDisplayPowerRequest.useProximitySensor && this.mProximityPositive && this.mSuspendWhenScreenOffDueToProximityConfig)) && !this.mScreenBrightnessBoostInProgress) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setHalAutoSuspendModeLocked(boolean enable) {
        if (enable != this.mHalAutoSuspendModeEnabled) {
            if (DEBUG) {
                Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable);
            }
            this.mHalAutoSuspendModeEnabled = enable;
            Trace.traceBegin(131072, "setHalAutoSuspend(" + enable + ")");
            try {
                nativeSetAutoSuspend(enable);
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setHalInteractiveModeLocked(boolean enable) {
        if (enable != this.mHalInteractiveModeEnabled) {
            if (DEBUG) {
                Slog.d(TAG, "Setting HAL interactive mode to " + enable);
            }
            this.mHalInteractiveModeEnabled = enable;
            Trace.traceBegin(131072, "setHalInteractive(" + enable + ")");
            try {
                nativeSetInteractive(enable);
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isInteractiveInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = PowerManagerInternal.isInteractive(this.mWakefulness) && (!this.mBrightnessWaitModeEnabled || this.mAuthSucceeded);
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean setLowPowerModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "setLowPowerModeInternal " + enabled + " mIsPowered=" + this.mIsPowered);
            }
            if (this.mIsPowered) {
                return false;
            }
            this.mBatterySaverStateMachine.setBatterySaverEnabledManually(enabled);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceIdleMode;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isLightDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLightDeviceIdleMode;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void handleBatteryStateChangedLocked() {
        this.mDirty |= 256;
        updatePowerStateLocked();
    }

    /* access modifiers changed from: private */
    /*  JADX ERROR: JadxRuntimeException in pass: BlockFinish
        jadx.core.utils.exceptions.JadxRuntimeException: Dominance frontier not set for block: B:12:0x0030
        	at jadx.core.dex.nodes.BlockNode.lock(BlockNode.java:75)
        	at jadx.core.utils.ImmutableList.forEach(ImmutableList.java:108)
        	at jadx.core.dex.nodes.MethodNode.finishBasicBlocks(MethodNode.java:461)
        	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:27)
        */
    public void shutdownOrRebootInternal(int r4, boolean r5, java.lang.String r6, boolean r7) {
        /*
            r3 = this;
            com.android.server.power.PowerManagerService$PowerManagerHandler r0 = r3.mHandler
            if (r0 == 0) goto L_0x0008
            boolean r0 = r3.mSystemReady
            if (r0 != 0) goto L_0x0011
        L_0x0008:
            boolean r0 = com.android.server.RescueParty.isAttemptingFactoryReset()
            if (r0 == 0) goto L_0x0036
            lowLevelReboot(r6)
        L_0x0011:
            com.android.server.power.PowerManagerService$4 r0 = new com.android.server.power.PowerManagerService$4
            r0.<init>(r4, r5, r6)
            android.os.Handler r1 = com.android.server.UiThread.getHandler()
            android.os.Message r1 = android.os.Message.obtain(r1, r0)
            r2 = 1
            r1.setAsynchronous(r2)
            android.os.Handler r2 = com.android.server.UiThread.getHandler()
            r2.sendMessage(r1)
            if (r7 == 0) goto L_0x0035
            monitor-enter(r0)
        L_0x002c:
            r0.wait()     // Catch:{ InterruptedException -> 0x0033 }
        L_0x002f:
            goto L_0x002c
        L_0x0030:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0030 }
            throw r2
        L_0x0033:
            r2 = move-exception
            goto L_0x002f
        L_0x0035:
            return
        L_0x0036:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "Too early to call shutdown() or reboot()"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.PowerManagerService.shutdownOrRebootInternal(int, boolean, java.lang.String, boolean):void");
    }

    /* access modifiers changed from: private */
    public void crashInternal(final String message) {
        Thread t = new Thread("PowerManagerService.crash()") {
            public void run() {
                throw new RuntimeException(message);
            }
        };
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            Slog.wtf(TAG, e);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updatePowerRequestFromBatterySaverPolicy(DisplayManagerInternal.DisplayPowerRequest displayPowerRequest) {
        PowerSaveState state = this.mBatterySaverPolicy.getBatterySaverPolicy(7, this.mBatterySaverController.isEnabled());
        displayPowerRequest.lowPowerMode = state.batterySaverEnabled;
        displayPowerRequest.screenLowPowerBrightnessFactor = state.brightnessFactor;
    }

    /* access modifiers changed from: package-private */
    public void setStayOnSettingInternal(int val) {
        Settings.Global.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", val);
    }

    /* access modifiers changed from: package-private */
    public void setMaximumScreenOffTimeoutFromDeviceAdminInternal(int userId, long timeMs) {
        if (userId < 0) {
            Slog.wtf(TAG, "Attempt to set screen off timeout for invalid user: " + userId);
            return;
        }
        synchronized (this.mLock) {
            if (userId == 0) {
                try {
                    this.mMaximumScreenOffTimeoutFromDeviceAdmin = timeMs;
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (timeMs != JobStatus.NO_LATEST_RUNTIME) {
                    if (timeMs != 0) {
                        ProfilePowerState profile = this.mProfilePowerState.get(userId);
                        if (profile != null) {
                            profile.mScreenOffTimeout = timeMs;
                        } else {
                            this.mProfilePowerState.put(userId, new ProfilePowerState(userId, timeMs));
                            this.mDirty |= 1;
                        }
                    }
                }
                this.mProfilePowerState.delete(userId);
            }
            this.mDirty |= 32;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0010, code lost:
        if (r3 == false) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0012, code lost:
        com.android.server.EventLogTags.writeDeviceIdleOnPhase("power");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        com.android.server.EventLogTags.writeDeviceIdleOffPhase("power");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0020, code lost:
        return true;
     */
    public boolean setDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode == enabled) {
                return false;
            }
            this.mDeviceIdleMode = enabled;
            updateWakeLockDisabledStatesLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setLightDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mLightDeviceIdleMode == enabled) {
                return false;
            }
            this.mLightDeviceIdleMode = enabled;
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceIdleWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setDeviceIdleTempWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleTempWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startUidChangesInternal() {
        synchronized (this.mLock) {
            this.mUidsChanging = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void finishUidChangesInternal() {
        synchronized (this.mLock) {
            this.mUidsChanging = false;
            if (this.mUidsChanged) {
                updateWakeLockDisabledStatesLocked();
                this.mUidsChanged = false;
            }
        }
    }

    private void handleUidStateChangeLocked() {
        if (this.mUidsChanging) {
            this.mUidsChanged = true;
        } else {
            updateWakeLockDisabledStatesLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateUidProcStateInternal(int uid, int procState) {
        synchronized (this.mLock) {
            UidState state = this.mUidState.get(uid);
            if (state == null) {
                state = new UidState(uid);
                this.mUidState.put(uid, state);
            }
            boolean z = false;
            boolean oldShouldAllow = state.mProcState <= 10;
            state.mProcState = procState;
            if (state.mNumWakeLocks > 0) {
                if (this.mDeviceIdleMode) {
                    handleUidStateChangeLocked();
                } else if (!state.mActive) {
                    if (procState <= 10) {
                        z = true;
                    }
                    if (oldShouldAllow != z) {
                        handleUidStateChangeLocked();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void uidGoneInternal(int uid) {
        synchronized (this.mLock) {
            int index = this.mUidState.indexOfKey(uid);
            if (index >= 0) {
                UidState state = this.mUidState.valueAt(index);
                state.mProcState = 19;
                state.mActive = false;
                this.mUidState.removeAt(index);
                if (this.mDeviceIdleMode && state.mNumWakeLocks > 0) {
                    handleUidStateChangeLocked();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void uidActiveInternal(int uid) {
        synchronized (this.mLock) {
            UidState state = this.mUidState.get(uid);
            if (state == null) {
                state = new UidState(uid);
                state.mProcState = 18;
                this.mUidState.put(uid, state);
            }
            state.mActive = true;
            if (state.mNumWakeLocks > 0) {
                handleUidStateChangeLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void uidIdleInternal(int uid) {
        synchronized (this.mLock) {
            UidState state = this.mUidState.get(uid);
            if (state != null) {
                state.mActive = false;
                if (state.mNumWakeLocks > 0) {
                    handleUidStateChangeLocked();
                }
            }
        }
    }

    private void updateWakeLockDisabledStatesLocked() {
        boolean changed = false;
        int numWakeLocks = this.mWakeLocks.size();
        for (int i = 0; i < numWakeLocks; i++) {
            WakeLock wakeLock = this.mWakeLocks.get(i);
            if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1 && setWakeLockDisabledStateLocked(wakeLock)) {
                changed = true;
                if (wakeLock.mDisabled) {
                    notifyWakeLockReleasedLocked(wakeLock);
                } else {
                    notifyWakeLockAcquiredLocked(wakeLock);
                }
            }
        }
        if (changed) {
            this.mDirty |= 1;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: protected */
    public boolean setWakeLockDisabledStateLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 1) {
            boolean disabled = false;
            int appid = UserHandle.getAppId(wakeLock.mOwnerUid);
            if (appid >= 10000) {
                if (this.mConstants.NO_CACHED_WAKE_LOCKS) {
                    disabled = !wakeLock.mUidState.mActive && wakeLock.mUidState.mProcState != 19 && wakeLock.mUidState.mProcState > 10;
                }
                if (this.mDeviceIdleMode) {
                    UidState state = wakeLock.mUidState;
                    if (Arrays.binarySearch(this.mDeviceIdleWhitelist, appid) < 0 && Arrays.binarySearch(this.mDeviceIdleTempWhitelist, appid) < 0 && state.mProcState != 19 && state.mProcState > 4) {
                        disabled = true;
                    }
                }
            }
            if (wakeLock.mDisabled != disabled) {
                Slog.d(TAG, "wakeLock.mDisabled:" + disabled + ", mDeviceIdleMode:" + this.mDeviceIdleMode + ", mProcState:" + wakeLock.mUidState.mProcState);
                wakeLock.mDisabled = disabled;
                return true;
            }
        }
        return false;
    }

    private boolean isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() {
        return this.mMaximumScreenOffTimeoutFromDeviceAdmin >= 0 && this.mMaximumScreenOffTimeoutFromDeviceAdmin < JobStatus.NO_LATEST_RUNTIME;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x000e, code lost:
        if (r5 == false) goto L_0x0012;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0010, code lost:
        r3 = 3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0012, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0013, code lost:
        r1.setFlashing(r6, 2, r3, 0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0016, code lost:
        return;
     */
    public void setAttentionLightInternal(boolean on, int color) {
        synchronized (this.mLock) {
            if (this.mSystemReady) {
                Light light = this.mAttentionLight;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDozeAfterScreenOffInternal(boolean on) {
        synchronized (this.mLock) {
            this.mDozeAfterScreenOff = on;
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004f, code lost:
        return;
     */
    public void boostScreenBrightnessInternal(long eventTime, int uid) {
        synchronized (this.mLock) {
            if (this.mSystemReady && this.mWakefulness != 0) {
                if (eventTime >= this.mLastScreenBrightnessBoostTime) {
                    Slog.i(TAG, "Brightness boost activated (uid " + uid + ")...");
                    this.mLastScreenBrightnessBoostTime = eventTime;
                    if (!this.mScreenBrightnessBoostInProgress) {
                        this.mScreenBrightnessBoostInProgress = true;
                        this.mNotifier.onScreenBrightnessBoostChanged();
                    }
                    this.mDirty |= 2048;
                    userActivityNoUpdateLocked(eventTime, 0, 0, uid);
                    updatePowerStateLocked();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isScreenBrightnessBoostedInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenBrightnessBoostInProgress;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void handleScreenBrightnessBoostTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleScreenBrightnessBoostTimeout");
            }
            this.mDirty |= 2048;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: private */
    public void setScreenBrightnessOverrideFromWindowManagerInternal(int brightness) {
        synchronized (this.mLock) {
            int brightness2 = setScreenBrightnessMappingtoIndoorMax(brightness);
            if (this.mScreenBrightnessOverrideFromWindowManager != brightness2) {
                this.mScreenBrightnessOverrideFromWindowManager = brightness2;
                if (DEBUG) {
                    Slog.d(TAG, "mScreenBrightnessOverrideFromWindowManager=" + this.mScreenBrightnessOverrideFromWindowManager);
                }
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        if (this.mDisplayManagerInternal != null) {
            return this.mDisplayManagerInternal.setScreenBrightnessMappingtoIndoorMax(brightness);
        }
        return brightness;
    }

    /* access modifiers changed from: private */
    public void setUserInactiveOverrideFromWindowManagerInternal() {
        synchronized (this.mLock) {
            this.mUserInactiveOverrideFromWindowManager = true;
            this.mDirty |= 4;
            updatePowerStateLocked();
        }
    }

    /* access modifiers changed from: private */
    public void setUserActivityTimeoutOverrideFromWindowManagerInternal(long timeoutMillis) {
        synchronized (this.mLock) {
            if (this.mUserActivityTimeoutOverrideFromWindowManager != timeoutMillis) {
                if (DEBUG) {
                    Slog.d(TAG, "mUserActivityTimeoutOverrideFromWindowManager=" + timeoutMillis);
                }
                this.mUserActivityTimeoutOverrideFromWindowManager = timeoutMillis;
                EventLogTags.writeUserActivityTimeoutOverride(timeoutMillis);
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setTemporaryScreenAutoBrightnessSettingOverrideInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mTemporaryScreenAutoBrightnessSettingOverride != brightness) {
                if (brightness == -1 && this.mDisplayPowerRequest.useAutoBrightness) {
                    this.mDisplayManagerInternal.updateAutoBrightnessAdjustFactor(((float) this.mTemporaryScreenAutoBrightnessSettingOverride) / 255.0f);
                }
                this.mTemporaryScreenAutoBrightnessSettingOverride = brightness;
                this.mDirty |= 32;
                if (DEBUG) {
                    Slog.d(TAG, "mTemporaryScreenAutoBrightnessSettingOverride=" + this.mTemporaryScreenAutoBrightnessSettingOverride + ",auto=" + this.mDisplayPowerRequest.useAutoBrightness);
                }
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDozeOverrideFromDreamManagerInternal(int screenState, int screenBrightness) {
        synchronized (this.mLock) {
            if (!(this.mDozeScreenStateOverrideFromDreamManager == screenState && this.mDozeScreenBrightnessOverrideFromDreamManager == screenBrightness)) {
                this.mDozeScreenStateOverrideFromDreamManager = screenState;
                this.mDozeScreenBrightnessOverrideFromDreamManager = screenBrightness;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDrawWakeLockOverrideFromSidekickInternal(boolean keepState) {
        synchronized (this.mLock) {
            if (this.mDrawWakeLockOverrideFromSidekick != keepState) {
                this.mDrawWakeLockOverrideFromSidekick = keepState;
                this.mDirty |= 32;
                updatePowerStateLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setVrModeEnabled(boolean enabled) {
        this.mIsVrModeEnabled = enabled;
    }

    /* access modifiers changed from: private */
    public void powerHintInternal(int hintId, int data) {
        if (hintId != 8 || data != 1 || !this.mBatterySaverController.isLaunchBoostDisabled()) {
            nativeSendPowerHint(hintId, data);
        }
    }

    public static void lowLevelShutdown(String reason) {
        if (reason == null) {
            reason = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        SystemProperties.set("sys.powerctl", "shutdown," + reason);
    }

    public static void lowLevelReboot(String reason) {
        if (reason == null) {
            reason = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        if (reason.equals("quiescent")) {
            sQuiescent = true;
            reason = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        } else if (reason.endsWith(",quiescent")) {
            sQuiescent = true;
            reason = reason.substring(0, (reason.length() - "quiescent".length()) - 1);
        }
        if (reason.equals("recovery") || reason.equals("recovery-update")) {
            reason = "recovery";
        }
        if (sQuiescent) {
            reason = reason + ",quiescent";
        }
        SystemProperties.set("sys.powerctl", "reboot," + reason);
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Slog.wtf(TAG, "Unexpected return from lowLevelReboot!");
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: protected */
    public void dumpInternal(PrintWriter pw) {
        int numProfiles;
        WirelessChargerDetector wcd;
        pw.println("POWER MANAGER (dumpsys power)\n");
        synchronized (this.mLock) {
            pw.println("Power Manager State:");
            this.mConstants.dump(pw);
            pw.println("  mDirty=0x" + Integer.toHexString(this.mDirty));
            pw.println("  mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness));
            pw.println("  mWakefulnessChanging=" + this.mWakefulnessChanging);
            pw.println("  mIsPowered=" + this.mIsPowered);
            pw.println("  mPlugType=" + this.mPlugType);
            pw.println("  mBatteryLevel=" + this.mBatteryLevel);
            pw.println("  mBatteryLevelWhenDreamStarted=" + this.mBatteryLevelWhenDreamStarted);
            pw.println("  mDockState=" + this.mDockState);
            pw.println("  mStayOn=" + this.mStayOn);
            pw.println("  mProximityPositive=" + this.mProximityPositive);
            pw.println("  mBootCompleted=" + this.mBootCompleted);
            pw.println("  mSystemReady=" + this.mSystemReady);
            pw.println("  mHalAutoSuspendModeEnabled=" + this.mHalAutoSuspendModeEnabled);
            pw.println("  mHalInteractiveModeEnabled=" + this.mHalInteractiveModeEnabled);
            pw.println("  mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary));
            pw.print("  mNotifyLongScheduled=");
            if (this.mNotifyLongScheduled == 0) {
                pw.print("(none)");
            } else {
                TimeUtils.formatDuration(this.mNotifyLongScheduled, SystemClock.uptimeMillis(), pw);
            }
            pw.println();
            pw.print("  mNotifyLongDispatched=");
            if (this.mNotifyLongDispatched == 0) {
                pw.print("(none)");
            } else {
                TimeUtils.formatDuration(this.mNotifyLongDispatched, SystemClock.uptimeMillis(), pw);
            }
            pw.println();
            pw.print("  mNotifyLongNextCheck=");
            if (this.mNotifyLongNextCheck == 0) {
                pw.print("(none)");
            } else {
                TimeUtils.formatDuration(this.mNotifyLongNextCheck, SystemClock.uptimeMillis(), pw);
            }
            pw.println();
            pw.println("  mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary));
            pw.println("  mRequestWaitForNegativeProximity=" + this.mRequestWaitForNegativeProximity);
            pw.println("  mSandmanScheduled=" + this.mSandmanScheduled);
            pw.println("  mSandmanSummoned=" + this.mSandmanSummoned);
            pw.println("  mBatteryLevelLow=" + this.mBatteryLevelLow);
            pw.println("  mLightDeviceIdleMode=" + this.mLightDeviceIdleMode);
            pw.println("  mDeviceIdleMode=" + this.mDeviceIdleMode);
            pw.println("  mDeviceIdleWhitelist=" + Arrays.toString(this.mDeviceIdleWhitelist));
            pw.println("  mDeviceIdleTempWhitelist=" + Arrays.toString(this.mDeviceIdleTempWhitelist));
            pw.println("  mLastWakeTime=" + TimeUtils.formatUptime(this.mLastWakeTime));
            pw.println("  mLastSleepTime=" + TimeUtils.formatUptime(this.mLastSleepTime));
            pw.println("  mLastUserActivityTime=" + TimeUtils.formatUptime(this.mLastUserActivityTime));
            pw.println("  mLastUserActivityTimeNoChangeLights=" + TimeUtils.formatUptime(this.mLastUserActivityTimeNoChangeLights));
            pw.println("  mLastInteractivePowerHintTime=" + TimeUtils.formatUptime(this.mLastInteractivePowerHintTime));
            pw.println("  mLastScreenBrightnessBoostTime=" + TimeUtils.formatUptime(this.mLastScreenBrightnessBoostTime));
            pw.println("  mScreenBrightnessBoostInProgress=" + this.mScreenBrightnessBoostInProgress);
            pw.println("  mDisplayReady=" + this.mDisplayReady);
            pw.println("  mHoldingWakeLockSuspendBlocker=" + this.mHoldingWakeLockSuspendBlocker);
            pw.println("  mHoldingDisplaySuspendBlocker=" + this.mHoldingDisplaySuspendBlocker);
            pw.println("  mBrightnessWaitModeEnabled=" + this.mBrightnessWaitModeEnabled);
            pw.println();
            pw.println("Settings and Configuration:");
            pw.println("  mDecoupleHalAutoSuspendModeFromDisplayConfig=" + this.mDecoupleHalAutoSuspendModeFromDisplayConfig);
            pw.println("  mDecoupleHalInteractiveModeFromDisplayConfig=" + this.mDecoupleHalInteractiveModeFromDisplayConfig);
            pw.println("  mWakeUpWhenPluggedOrUnpluggedConfig=" + this.mWakeUpWhenPluggedOrUnpluggedConfig);
            pw.println("  mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig=" + this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig);
            pw.println("  mTheaterModeEnabled=" + this.mTheaterModeEnabled);
            pw.println("  mSuspendWhenScreenOffDueToProximityConfig=" + this.mSuspendWhenScreenOffDueToProximityConfig);
            pw.println("  mDreamsSupportedConfig=" + this.mDreamsSupportedConfig);
            pw.println("  mDreamsEnabledByDefaultConfig=" + this.mDreamsEnabledByDefaultConfig);
            pw.println("  mDreamsActivatedOnSleepByDefaultConfig=" + this.mDreamsActivatedOnSleepByDefaultConfig);
            pw.println("  mDreamsActivatedOnDockByDefaultConfig=" + this.mDreamsActivatedOnDockByDefaultConfig);
            pw.println("  mDreamsEnabledOnBatteryConfig=" + this.mDreamsEnabledOnBatteryConfig);
            pw.println("  mDreamsBatteryLevelMinimumWhenPoweredConfig=" + this.mDreamsBatteryLevelMinimumWhenPoweredConfig);
            pw.println("  mDreamsBatteryLevelMinimumWhenNotPoweredConfig=" + this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            pw.println("  mDreamsBatteryLevelDrainCutoffConfig=" + this.mDreamsBatteryLevelDrainCutoffConfig);
            pw.println("  mDreamsEnabledSetting=" + this.mDreamsEnabledSetting);
            pw.println("  mDreamsActivateOnSleepSetting=" + this.mDreamsActivateOnSleepSetting);
            pw.println("  mDreamsActivateOnDockSetting=" + this.mDreamsActivateOnDockSetting);
            pw.println("  mDozeAfterScreenOff=" + this.mDozeAfterScreenOff);
            pw.println("  mMinimumScreenOffTimeoutConfig=" + this.mMinimumScreenOffTimeoutConfig);
            pw.println("  mMaximumScreenDimDurationConfig=" + this.mMaximumScreenDimDurationConfig);
            pw.println("  mMaximumScreenDimRatioConfig=" + this.mMaximumScreenDimRatioConfig);
            pw.println("  mScreenOffTimeoutSetting=" + this.mScreenOffTimeoutSetting);
            pw.println("  mSleepTimeoutSetting=" + this.mSleepTimeoutSetting);
            pw.println("  mMaximumScreenOffTimeoutFromDeviceAdmin=" + this.mMaximumScreenOffTimeoutFromDeviceAdmin + " (enforced=" + isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("  mStayOnWhilePluggedInSetting=");
            sb.append(this.mStayOnWhilePluggedInSetting);
            pw.println(sb.toString());
            pw.println("  mScreenBrightnessSetting=" + this.mScreenBrightnessSetting);
            pw.println("  mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting);
            pw.println("  mScreenBrightnessOverrideFromWindowManager=" + this.mScreenBrightnessOverrideFromWindowManager);
            pw.println("  mUserActivityTimeoutOverrideFromWindowManager=" + this.mUserActivityTimeoutOverrideFromWindowManager);
            pw.println("  mUserInactiveOverrideFromWindowManager=" + this.mUserInactiveOverrideFromWindowManager);
            pw.println("  mDozeScreenStateOverrideFromDreamManager=" + this.mDozeScreenStateOverrideFromDreamManager);
            pw.println("  mDrawWakeLockOverrideFromSidekick=" + this.mDrawWakeLockOverrideFromSidekick);
            pw.println("  mDozeScreenBrightnessOverrideFromDreamManager=" + this.mDozeScreenBrightnessOverrideFromDreamManager);
            pw.println("  mScreenBrightnessSettingMinimum=" + this.mScreenBrightnessSettingMinimum);
            pw.println("  mScreenBrightnessSettingMaximum=" + this.mScreenBrightnessSettingMaximum);
            pw.println("  mScreenBrightnessSettingDefault=" + this.mScreenBrightnessSettingDefault);
            pw.println("  mDoubleTapWakeEnabled=" + this.mDoubleTapWakeEnabled);
            pw.println("  mIsVrModeEnabled=" + this.mIsVrModeEnabled);
            pw.println("  mForegroundProfile=" + this.mForegroundProfile);
            long screenDimDuration = getScreenDimDurationLocked(getScreenOffTimeoutLocked(getSleepTimeoutLocked()));
            pw.println();
            pw.println("Sleep timeout: " + sleepTimeout + " ms");
            pw.println("Screen off timeout: " + screenOffTimeout + " ms");
            pw.println("Screen dim duration: " + screenDimDuration + " ms");
            pw.println();
            pw.print("UID states (changing=");
            pw.print(this.mUidsChanging);
            pw.print(" changed=");
            pw.print(this.mUidsChanged);
            pw.println("):");
            for (int i = 0; i < this.mUidState.size(); i++) {
                UidState state = this.mUidState.valueAt(i);
                pw.print("  UID ");
                UserHandle.formatUid(pw, this.mUidState.keyAt(i));
                pw.print(": ");
                if (state.mActive) {
                    pw.print("  ACTIVE ");
                } else {
                    pw.print("INACTIVE ");
                }
                pw.print(" count=");
                pw.print(state.mNumWakeLocks);
                pw.print(" state=");
                pw.println(state.mProcState);
            }
            pw.println();
            pw.println("Looper state:");
            this.mHandler.getLooper().dump(new PrintWriterPrinter(pw), "  ");
            pw.println();
            pw.println("Wake Locks: size=" + this.mWakeLocks.size());
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                pw.println("  " + it.next());
            }
            pw.println();
            pw.println("Suspend Blockers: size=" + this.mSuspendBlockers.size());
            Iterator<SuspendBlocker> it2 = this.mSuspendBlockers.iterator();
            while (it2.hasNext()) {
                pw.println("  " + it2.next());
            }
            pw.println();
            pw.println("Display Power: " + this.mDisplayPowerCallbacks);
            this.mBatterySaverPolicy.dump(pw);
            this.mBatterySaverStateMachine.dump(pw);
            pw.println();
            pw.println("Profile power states: size=" + numProfiles);
            for (int i2 = 0; i2 < numProfiles; i2++) {
                ProfilePowerState profile = this.mProfilePowerState.valueAt(i2);
                pw.print("  mUserId=");
                pw.print(profile.mUserId);
                pw.print(" mScreenOffTimeout=");
                pw.print(profile.mScreenOffTimeout);
                pw.print(" mWakeLockSummary=");
                pw.print(profile.mWakeLockSummary);
                pw.print(" mLastUserActivityTime=");
                pw.print(profile.mLastUserActivityTime);
                pw.print(" mLockingNotified=");
                pw.println(profile.mLockingNotified);
            }
            wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.dump(pw);
        }
    }

    /* access modifiers changed from: private */
    public void dumpProto(FileDescriptor fd) {
        WirelessChargerDetector wcd;
        ProtoOutputStream proto = new ProtoOutputStream(fd);
        synchronized (this.mLock) {
            this.mConstants.dumpProto(proto);
            proto.write(1120986464258L, this.mDirty);
            proto.write(1159641169923L, this.mWakefulness);
            proto.write(1133871366148L, this.mWakefulnessChanging);
            proto.write(1133871366149L, this.mIsPowered);
            proto.write(1159641169926L, this.mPlugType);
            proto.write(1120986464263L, this.mBatteryLevel);
            proto.write(1120986464264L, this.mBatteryLevelWhenDreamStarted);
            proto.write(1159641169929L, this.mDockState);
            proto.write(1133871366154L, this.mStayOn);
            proto.write(1133871366155L, this.mProximityPositive);
            proto.write(1133871366156L, this.mBootCompleted);
            proto.write(1133871366157L, this.mSystemReady);
            proto.write(1133871366158L, this.mHalAutoSuspendModeEnabled);
            proto.write(1133871366159L, this.mHalInteractiveModeEnabled);
            long activeWakeLocksToken = proto.start(1146756268048L);
            int i = 0;
            proto.write(1133871366145L, (this.mWakeLockSummary & 1) != 0);
            proto.write(1133871366146L, (this.mWakeLockSummary & 2) != 0);
            proto.write(1133871366147L, (this.mWakeLockSummary & 4) != 0);
            proto.write(1133871366148L, (this.mWakeLockSummary & 8) != 0);
            proto.write(1133871366149L, (this.mWakeLockSummary & 16) != 0);
            proto.write(1133871366150L, (this.mWakeLockSummary & 32) != 0);
            proto.write(1133871366151L, (this.mWakeLockSummary & 64) != 0);
            proto.write(1133871366152L, (this.mWakeLockSummary & 128) != 0);
            proto.end(activeWakeLocksToken);
            proto.write(1112396529681L, this.mNotifyLongScheduled);
            proto.write(1112396529682L, this.mNotifyLongDispatched);
            proto.write(1112396529683L, this.mNotifyLongNextCheck);
            long userActivityToken = proto.start(1146756268052L);
            proto.write(1133871366145L, (this.mUserActivitySummary & 1) != 0);
            proto.write(1133871366146L, (this.mUserActivitySummary & 2) != 0);
            proto.write(1133871366147L, (this.mUserActivitySummary & 4) != 0);
            proto.end(userActivityToken);
            proto.write(1133871366165L, this.mRequestWaitForNegativeProximity);
            proto.write(1133871366166L, this.mSandmanScheduled);
            proto.write(1133871366167L, this.mSandmanSummoned);
            proto.write(1133871366168L, this.mBatteryLevelLow);
            proto.write(1133871366169L, this.mLightDeviceIdleMode);
            proto.write(1133871366170L, this.mDeviceIdleMode);
            for (int id : this.mDeviceIdleWhitelist) {
                proto.write(2220498092059L, id);
            }
            for (int id2 : this.mDeviceIdleTempWhitelist) {
                proto.write(2220498092060L, id2);
            }
            proto.write(1112396529693L, this.mLastWakeTime);
            proto.write(1112396529694L, this.mLastSleepTime);
            proto.write(1112396529695L, this.mLastUserActivityTime);
            proto.write(1112396529696L, this.mLastUserActivityTimeNoChangeLights);
            proto.write(1112396529697L, this.mLastInteractivePowerHintTime);
            proto.write(1112396529698L, this.mLastScreenBrightnessBoostTime);
            proto.write(1133871366179L, this.mScreenBrightnessBoostInProgress);
            proto.write(1133871366180L, this.mDisplayReady);
            proto.write(1133871366181L, this.mHoldingWakeLockSuspendBlocker);
            proto.write(1133871366182L, this.mHoldingDisplaySuspendBlocker);
            long settingsAndConfigurationToken = proto.start(1146756268071L);
            proto.write(1133871366145L, this.mDecoupleHalAutoSuspendModeFromDisplayConfig);
            proto.write(1133871366146L, this.mDecoupleHalInteractiveModeFromDisplayConfig);
            proto.write(1133871366147L, this.mWakeUpWhenPluggedOrUnpluggedConfig);
            proto.write(1133871366148L, this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig);
            proto.write(1133871366149L, this.mTheaterModeEnabled);
            proto.write(1133871366150L, this.mSuspendWhenScreenOffDueToProximityConfig);
            proto.write(1133871366151L, this.mDreamsSupportedConfig);
            proto.write(1133871366152L, this.mDreamsEnabledByDefaultConfig);
            proto.write(1133871366153L, this.mDreamsActivatedOnSleepByDefaultConfig);
            proto.write(1133871366154L, this.mDreamsActivatedOnDockByDefaultConfig);
            proto.write(1133871366155L, this.mDreamsEnabledOnBatteryConfig);
            proto.write(1172526071820L, this.mDreamsBatteryLevelMinimumWhenPoweredConfig);
            proto.write(1172526071821L, this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig);
            proto.write(1172526071822L, this.mDreamsBatteryLevelDrainCutoffConfig);
            proto.write(1133871366159L, this.mDreamsEnabledSetting);
            proto.write(1133871366160L, this.mDreamsActivateOnSleepSetting);
            proto.write(1133871366161L, this.mDreamsActivateOnDockSetting);
            proto.write(1133871366162L, this.mDozeAfterScreenOff);
            proto.write(1120986464275L, this.mMinimumScreenOffTimeoutConfig);
            proto.write(1120986464276L, this.mMaximumScreenDimDurationConfig);
            proto.write(1108101562389L, this.mMaximumScreenDimRatioConfig);
            proto.write(1120986464278L, this.mScreenOffTimeoutSetting);
            proto.write(1172526071831L, this.mSleepTimeoutSetting);
            proto.write(1120986464280L, Math.min(this.mMaximumScreenOffTimeoutFromDeviceAdmin, 2147483647L));
            proto.write(1133871366169L, isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked());
            long stayOnWhilePluggedInToken = proto.start(1146756268058L);
            proto.write(1133871366145L, (this.mStayOnWhilePluggedInSetting & 1) != 0);
            proto.write(1133871366146L, (this.mStayOnWhilePluggedInSetting & 2) != 0);
            proto.write(1133871366147L, (this.mStayOnWhilePluggedInSetting & 4) != 0);
            proto.end(stayOnWhilePluggedInToken);
            proto.write(1159641169947L, this.mScreenBrightnessModeSetting);
            proto.write(1172526071836L, this.mScreenBrightnessOverrideFromWindowManager);
            long j = activeWakeLocksToken;
            proto.write(1176821039133L, this.mUserActivityTimeoutOverrideFromWindowManager);
            proto.write(1133871366174L, this.mUserInactiveOverrideFromWindowManager);
            proto.write(1159641169951L, this.mDozeScreenStateOverrideFromDreamManager);
            proto.write(1133871366180L, this.mDrawWakeLockOverrideFromSidekick);
            proto.write(1108101562400L, this.mDozeScreenBrightnessOverrideFromDreamManager);
            long screenBrightnessSettingLimitsToken = proto.start(1146756268065L);
            proto.write(1120986464257L, this.mScreenBrightnessSettingMinimum);
            proto.write(1120986464258L, this.mScreenBrightnessSettingMaximum);
            proto.write(1120986464259L, this.mScreenBrightnessSettingDefault);
            proto.end(screenBrightnessSettingLimitsToken);
            proto.write(1133871366178L, this.mDoubleTapWakeEnabled);
            proto.write(1133871366179L, this.mIsVrModeEnabled);
            proto.end(settingsAndConfigurationToken);
            long sleepTimeout = getSleepTimeoutLocked();
            long j2 = screenBrightnessSettingLimitsToken;
            long screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            long j3 = userActivityToken;
            proto.write(1172526071848L, sleepTimeout);
            proto.write(1120986464297L, screenOffTimeout);
            long j4 = screenOffTimeout;
            long screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
            proto.write(1120986464298L, screenDimDuration);
            proto.write(1133871366187L, this.mUidsChanging);
            proto.write(1133871366188L, this.mUidsChanged);
            while (true) {
                int i2 = i;
                if (i2 >= this.mUidState.size()) {
                    break;
                }
                UidState state = this.mUidState.valueAt(i2);
                long screenDimDuration2 = screenDimDuration;
                long uIDToken = proto.start(2246267895853L);
                int uid = this.mUidState.keyAt(i2);
                proto.write(1120986464257L, uid);
                proto.write(1138166333442L, UserHandle.formatUid(uid));
                proto.write(1133871366147L, state.mActive);
                proto.write(1120986464260L, state.mNumWakeLocks);
                proto.write(1159641169925L, ActivityManager.processStateAmToProto(state.mProcState));
                proto.end(uIDToken);
                i = i2 + 1;
                screenDimDuration = screenDimDuration2;
                settingsAndConfigurationToken = settingsAndConfigurationToken;
                stayOnWhilePluggedInToken = stayOnWhilePluggedInToken;
            }
            long j5 = settingsAndConfigurationToken;
            long j6 = stayOnWhilePluggedInToken;
            this.mBatterySaverStateMachine.dumpProto(proto, 1146756268082L);
            this.mHandler.getLooper().writeToProto(proto, 1146756268078L);
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                it.next().writeToProto(proto, 2246267895855L);
            }
            Iterator<SuspendBlocker> it2 = this.mSuspendBlockers.iterator();
            while (it2.hasNext()) {
                it2.next().writeToProto(proto, 2246267895856L);
            }
            wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.writeToProto(proto, 1146756268081L);
        }
        proto.flush();
    }

    private SuspendBlocker createSuspendBlockerLocked(String name) {
        SuspendBlocker suspendBlocker = new SuspendBlockerImpl(name);
        this.mSuspendBlockers.add(suspendBlocker);
        return suspendBlocker;
    }

    private void incrementBootCount() {
        int count;
        synchronized (this.mLock) {
            try {
                count = Settings.Global.getInt(getContext().getContentResolver(), "boot_count");
            } catch (Settings.SettingNotFoundException e) {
                count = 0;
            }
            Settings.Global.putInt(getContext().getContentResolver(), "boot_count", count + 1);
        }
    }

    /* access modifiers changed from: private */
    public static WorkSource copyWorkSource(WorkSource workSource) {
        if (workSource != null) {
            return new WorkSource(workSource);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean needWakeup() {
        boolean wasPowered = this.mIsPowered;
        int oldPlugType = this.mPlugType;
        boolean dockedOnWirelessCharger = this.mWirelessChargerDetector.update(this.mIsPowered, this.mPlugType);
        this.mIsPowered = this.mBatteryManagerInternal.isPowered(7);
        this.mPlugType = this.mBatteryManagerInternal.getPlugType();
        if (wasPowered == this.mIsPowered && oldPlugType == this.mPlugType) {
            return false;
        }
        return shouldWakeUpWhenPluggedOrUnpluggedLocked(wasPowered, oldPlugType, dockedOnWirelessCharger);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getLastShutdownReasonInternal(String lastRebootReasonProperty) {
        String line = SystemProperties.get(lastRebootReasonProperty);
        if (line == null) {
            return 0;
        }
        char c = 65535;
        switch (line.hashCode()) {
            case -2117951935:
                if (line.equals(REASON_THERMAL_SHUTDOWN)) {
                    c = 3;
                    break;
                }
                break;
            case -1099647817:
                if (line.equals(REASON_LOW_BATTERY)) {
                    c = 4;
                    break;
                }
                break;
            case -934938715:
                if (line.equals(REASON_REBOOT)) {
                    c = 1;
                    break;
                }
                break;
            case -852189395:
                if (line.equals(REASON_USERREQUESTED)) {
                    c = 2;
                    break;
                }
                break;
            case -169343402:
                if (line.equals(REASON_SHUTDOWN)) {
                    c = 0;
                    break;
                }
                break;
            case 1218064802:
                if (line.equals(REASON_BATTERY_THERMAL_STATE)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 4;
            case 4:
                return 5;
            case 5:
                return 6;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isPhoneHeldWakeLock() {
        if ((this.mWakeLockSummary & 16) != 0) {
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                WakeLock wl = it.next();
                if (incalluiPackageName.equals(wl.mPackageName) && (wl.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 32) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isCarMachineHeldWakeLock() {
        if ((this.mWakeLockSummary & 2) != 0) {
            Iterator<WakeLock> it = this.mWakeLocks.iterator();
            while (it.hasNext()) {
                WakeLock wl = it.next();
                if (machineCarPackageName.equals(wl.mPackageName) && (wl.mFlags & NetworkConstants.ARP_HWTYPE_RESERVED_HI) == 10) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void setAodStateBySysfs(String file, int command) {
        String str;
        StringBuilder sb;
        Slog.i(TAG, "AOD PowerManagerService setAodStateBySysfs()");
        if (mSupportAod) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            String strCmd = Integer.toString(command);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(strCmd.getBytes());
                fileOutputStream.flush();
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e = e;
                    str = TAG;
                    sb = new StringBuilder();
                }
            } catch (FileNotFoundException e2) {
                Slog.e(TAG, "File not found: " + e2.toString());
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e3) {
                        e = e3;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (IOException e4) {
                Slog.e(TAG, "Error accessing file: " + e4.toString());
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e5) {
                        e = e5;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Exception e6) {
                Slog.e(TAG, "Exception occur: " + e6.toString());
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e7) {
                        e = e7;
                        str = TAG;
                        sb = new StringBuilder();
                    }
                }
            } catch (Throwable th) {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e8) {
                        Slog.e(TAG, "Error closing file: " + e8.toString());
                    }
                }
                throw th;
            }
        }
        return;
        sb.append("Error closing file: ");
        sb.append(e.toString());
        Slog.e(str, sb.toString());
    }

    /* access modifiers changed from: private */
    public int getAodStateBySysfs(String file) {
        StringBuilder sb;
        String str;
        int aodState = -1;
        Slog.i(TAG, "AOD PowerManagerService getAodStateBySysfs()");
        if (!mSupportAod) {
            return -1;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
            String tempString = reader2.readLine();
            Slog.i(TAG, "getAodStateBySysfs:tempString " + tempString);
            if (tempString != null && tempString.startsWith("aod_function =")) {
                tempString = tempString.substring("aod_function =".length()).trim();
            }
            aodState = Integer.parseInt(tempString);
            try {
                reader2.close();
            } catch (IOException e) {
                Slog.e(TAG, "getAodStateBySysfs IOException" + e.toString());
            }
            try {
                fis2.close();
            } catch (IOException e2) {
                e1 = e2;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (NumberFormatException e3) {
            Slog.e(TAG, "NumberFormatException: " + e3.toString());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "getAodStateBySysfs IOException" + e4.toString());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e5) {
                    e1 = e5;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (FileNotFoundException e6) {
            Slog.e(TAG, "File not found: " + e6.toString());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e7) {
                    Slog.e(TAG, "getAodStateBySysfs IOException" + e7.toString());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e8) {
                    e1 = e8;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e9) {
            Slog.e(TAG, "Error accessing file: " + e9.toString());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e10) {
                    Slog.e(TAG, "getAodStateBySysfs IOException" + e10.toString());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e11) {
                    e1 = e11;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Exception e12) {
            Slog.e(TAG, "Exception occur: " + e12.toString());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e13) {
                    Slog.e(TAG, "getAodStateBySysfs IOException" + e13.toString());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e14) {
                    e1 = e14;
                    str = TAG;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable e1) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e15) {
                    Slog.e(TAG, "getAodStateBySysfs IOException" + e15.toString());
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e16) {
                    Slog.e(TAG, "e1 IOException " + e16.toString());
                }
            }
            throw e1;
        }
        return aodState;
        sb.append("e1 IOException ");
        sb.append(e1.toString());
        Slog.e(str, sb.toString());
        return aodState;
    }

    /* access modifiers changed from: private */
    public boolean isAppCanGetDrawWakeLock(int flags, String packageName, WorkSource ws, String tag) {
        if (this.mAlpmState != 0 || (flags & 128) == 0 || !"Window:StatusBar".equals(tag) || ws == null || !"com.android.systemui".equals(ws.getName(0))) {
            return true;
        }
        Slog.e(TAG, "isCanGetWakeLock:systemui statusbar canot get draw_wake_lock in aod mode");
        return false;
    }

    /* access modifiers changed from: private */
    public void setDozeOverrideFromAodLocked(int screenState, int screenBrightness) {
        Slog.i(TAG, "AOD PowerManagerService setDozeOverrideFromAodLocked()");
        if (mSupportAod) {
            if (!(this.mDozeScreenStateOverrideFromDreamManager == screenState && this.mDozeScreenBrightnessOverrideFromDreamManager == screenBrightness)) {
                this.mDozeScreenStateOverrideFromDreamManager = screenState;
                this.mDozeScreenBrightnessOverrideFromDreamManager = screenBrightness;
                this.mDirty |= 32;
                updatePowerStateLocked();
                if (screenState == 2 || screenState == 3) {
                    this.mDisplayManagerInternal.forceDisplayState(screenState, (screenBrightness * 10000) / 255);
                }
            }
        }
    }

    public void regeditAodStateCallback(IAodStateCallback callback) {
        Slog.i(TAG, "AOD PowerManagerService regeditAodStateCallback()");
        if (mSupportAod) {
            this.mPolicy.regeditAodStateCallback(callback);
        }
    }

    public void unregeditAodStateCallback(IAodStateCallback callback) {
        Slog.i(TAG, "AOD PowerManagerService unregeditAodStateCallback()");
        if (mSupportAod) {
            this.mPolicy.unregeditAodStateCallback(callback);
        }
    }

    /* access modifiers changed from: protected */
    public void sendTempBrightnessToMonitor(String paramType, int brightness) {
    }

    /* access modifiers changed from: protected */
    public void sendBrightnessModeToMonitor(boolean manualMode, String packageName) {
    }

    /* access modifiers changed from: protected */
    public void sendManualBrightnessToMonitor(int brightness, String packageName) {
    }

    /* access modifiers changed from: protected */
    public void sendBootCompletedToMonitor() {
    }

    public boolean getKeyguardLockedStatus() {
        if (this.mKeyguardManager != null) {
            return this.mKeyguardManager.isKeyguardLocked();
        }
        Slog.e(TAG, "mKeyguardManager=null");
        return false;
    }

    private int getCoverModeBrightness() {
        int coverModeBrightness = this.mScreenBrightnessSettingDefault;
        if (this.mDisplayManagerInternal != null) {
            coverModeBrightness = this.mDisplayManagerInternal.getCoverModeBrightnessFromLastScreenBrightness();
            if (coverModeBrightness != -3 && !isValidBrightness(coverModeBrightness)) {
                Slog.e(TAG, "not valid coverModeBrightness=" + coverModeBrightness + ",setDefault=" + this.mScreenBrightnessSettingDefault);
                coverModeBrightness = this.mScreenBrightnessSettingDefault;
            }
        }
        if (DEBUG && coverModeBrightness != this.mCoverModeBrightness) {
            Slog.d(TAG, "coverModeBrightness=" + coverModeBrightness + ",lastcoverModeBrightness=" + this.mCoverModeBrightness);
        }
        this.mCoverModeBrightness = coverModeBrightness;
        return this.mCoverModeBrightness;
    }

    private void updateAutoBrightnessDBforSeekbar(int level, int state) {
        ContentResolver resolver = this.mContext.getContentResolver();
        int autoBrightnessMode = Settings.System.getIntForUser(resolver, "screen_brightness_mode", 0, this.mCurrentUserId);
        int autoBrightnessDB = Settings.System.getIntForUser(resolver, "screen_auto_brightness", 0, this.mCurrentUserId);
        if (autoBrightnessMode != 1) {
            return;
        }
        if ((this.mAutoBrightnessLevel != level || autoBrightnessDB != level) && level >= 0 && state != 0) {
            if (DEBUG) {
                Slog.d(TAG, "LabcCoverMode mAutoBrightnessLevel=" + this.mAutoBrightnessLevel + ",level=" + level + ",autoBrightnessDB=" + autoBrightnessDB + ",state=" + state);
            }
            Settings.System.putIntForUser(resolver, "screen_auto_brightness", level, this.mCurrentUserId);
            this.mAutoBrightnessLevel = level;
        }
    }

    public boolean getRebootAutoModeEnable() {
        return this.mDisplayManagerInternal.getRebootAutoModeEnable();
    }

    private boolean needFaceDetect(long nextTimeout, long now, boolean startNoChangeLights) {
        if (!mSupportFaceDetect) {
            return false;
        }
        boolean hasNoChangeLights = this.mLastUserActivityTimeNoChangeLights >= this.mLastWakeTime && this.mLastUserActivityTimeNoChangeLights > this.mLastUserActivityTime;
        if (((this.mUserActivitySummary != 1 || hasNoChangeLights) && !startNoChangeLights) || nextTimeout - now < 1000 || isKeyguardLocked() || (this.mWakeLockSummary & 32) != 0 || this.mStayOn) {
            return false;
        }
        return true;
    }

    private boolean isKeyguardLocked() {
        if (this.mPolicy == null || !this.mPolicy.isKeyguardLocked()) {
            return false;
        }
        return true;
    }

    public HwPowerDAMonitorProxy getPowerMonitor() {
        return this.mPowerProxy;
    }

    public void sendNoUserActivityNotification(int customActivityTimeout) {
        Slog.i(TAG, "sendNoUserActivityNotification customActivityTimeout :" + customActivityTimeout);
        boolean z = true;
        if (!(this.mWakefulness == 1 || this.mWakefulness == 2)) {
            z = false;
        }
        boolean isScreenOn = z;
        if (isScreenOn && this.mCustomUserActivityTimeout == 0) {
            this.mCustomUserActivityTimeout = customActivityTimeout;
            long nextCustomActivityTimeout = ((long) this.mCustomUserActivityTimeout) + SystemClock.uptimeMillis();
            Slog.i(TAG, "sendNoUserActivityNotification isScreenOn = " + isScreenOn + " nextCustomActivityTimeout = " + nextCustomActivityTimeout);
            this.mHandler.removeMessages(105);
            scheduleCustomUserInactivityTimeout(nextCustomActivityTimeout);
        }
    }

    /* access modifiers changed from: private */
    public void suspendSystemInternal(boolean suspend, boolean forceUpdate) {
        if (this.mDisplayManagerInternal != null) {
            this.mDisplayManagerInternal.suspendSystem(suspend, forceUpdate);
        }
    }

    public boolean isSystemSuspendingInternal() {
        if (this.mDisplayManagerInternal != null) {
            return this.mDisplayManagerInternal.isSystemSuspending();
        }
        return false;
    }
}

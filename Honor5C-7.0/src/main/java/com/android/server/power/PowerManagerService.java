package com.android.server.power;

import android.annotation.IntDef;
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
import android.hardware.display.DisplayManagerInternal.DisplayPowerCallbacks;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.BatteryManagerInternal;
import android.os.Binder;
import android.os.Handler;
import android.os.IAodStateCallback;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IPowerManager.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.BacklightBrightness;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.view.Display;
import android.view.WindowManagerPolicy;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.ServiceThread;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.BatteryStatsService;
import com.android.server.am.IHwPowerInfoService;
import com.android.server.am.ProcessList;
import com.android.server.display.RampAnimator;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.vr.VrManagerService;
import com.android.server.wm.AppTransition;
import com.android.server.wm.WindowManagerService.H;
import huawei.cust.HwCustUtils;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import libcore.util.Objects;

public class PowerManagerService extends AbsPowerManagerService implements Monitor {
    private static final String AOD_MODE_CMD = "/sys/class/graphics/fb0/alpm_setting";
    private static final String AOD_STATE_CMD = "/sys/class/graphics/fb0/alpm_function";
    protected static final boolean DEBUG = false;
    private static final boolean DEBUG_ALL = false;
    private static boolean DEBUG_Controller = false;
    protected static final boolean DEBUG_SPEW = false;
    private static final int DEFAULT_DOUBLE_TAP_TO_WAKE = 0;
    private static final int DEFAULT_SCREEN_OFF_TIMEOUT = 15000;
    private static final int DEFAULT_SLEEP_TIMEOUT = -1;
    private static final int DIRTY_ACTUAL_DISPLAY_POWER_STATE_UPDATED = 8;
    private static final int DIRTY_BATTERY_STATE = 256;
    private static final int DIRTY_BOOT_COMPLETED = 16;
    private static final int DIRTY_DOCK_STATE = 1024;
    private static final int DIRTY_IS_POWERED = 64;
    private static final int DIRTY_PROXIMITY_POSITIVE = 512;
    private static final int DIRTY_SCREEN_BRIGHTNESS_BOOST = 2048;
    private static final int DIRTY_SETTINGS = 32;
    private static final int DIRTY_STAY_ON = 128;
    private static final int DIRTY_USER_ACTIVITY = 4;
    protected static final int DIRTY_WAIT_BRIGHT_MODE = 4096;
    protected static final int DIRTY_WAKEFULNESS = 2;
    protected static final int DIRTY_WAKE_LOCKS = 1;
    private static final int HALT_MODE_REBOOT = 1;
    private static final int HALT_MODE_REBOOT_SAFE_MODE = 2;
    private static final int HALT_MODE_SHUTDOWN = 0;
    private static final int HIGH_PRECISION_MAX_BRIGHTNESS = 10000;
    private static final HashSet<String> LOG_DROP_SET = null;
    private static final int MSG_POWERKEY_WAKEUP = 5;
    private static final int MSG_SANDMAN = 2;
    private static final int MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 3;
    private static final int MSG_USER_ACTIVITY_TIMEOUT = 1;
    protected static final int MSG_WAIT_BRIGHT_TIMEOUT = 4;
    private static final int POWER_FEATURE_DOUBLE_TAP_TO_WAKE = 1;
    private static final int POWER_HINT_LOW_POWER = 5;
    private static final int POWER_HINT_VR_MODE = 7;
    private static final int SCREEN_BRIGHTNESS_BOOST_TIMEOUT = 5000;
    private static final String TAG = "PowerManagerService";
    private static final int USER_ACTIVITY_SCREEN_BRIGHT = 1;
    private static final int USER_ACTIVITY_SCREEN_DIM = 2;
    private static final int USER_ACTIVITY_SCREEN_DREAM = 4;
    private static final int WAKE_LOCK_BUTTON_BRIGHT = 8;
    private static final int WAKE_LOCK_CPU = 1;
    private static final int WAKE_LOCK_DOZE = 64;
    private static final int WAKE_LOCK_DRAW = 128;
    private static final int WAKE_LOCK_PROXIMITY_SCREEN_OFF = 16;
    private static final int WAKE_LOCK_SCREEN_BRIGHT = 2;
    private static final int WAKE_LOCK_SCREEN_DIM = 4;
    private static final int WAKE_LOCK_STAY_AWAKE = 32;
    private static final String incalluiPackageName = "com.android.incallui";
    private static final boolean mIsPowerTurnTorchOff = false;
    private static final boolean mSupportAod = false;
    private static final String machineCarPackageName = "com.huawei.vdrive";
    private boolean inVdriveBackLightMode;
    private IAppOpsService mAppOps;
    private Light mAttentionLight;
    private boolean mAutoLowPowerModeConfigured;
    private boolean mAutoLowPowerModeSnoozing;
    BacklightBrightness mBacklightBrightness;
    private int mBatteryLevel;
    private boolean mBatteryLevelLow;
    private int mBatteryLevelWhenDreamStarted;
    private BatteryManagerInternal mBatteryManagerInternal;
    private IBatteryStats mBatteryStats;
    protected boolean mBootCompleted;
    private Runnable[] mBootCompletedRunnables;
    private boolean mBrightnessUseTwilight;
    protected boolean mBrightnessWaitModeEnabled;
    protected boolean mBrightnessWaitRet;
    private final Context mContext;
    private int mCurrentUserId;
    private HwCustPowerManagerService mCust;
    private boolean mDecoupleHalAutoSuspendModeFromDisplayConfig;
    private boolean mDecoupleHalInteractiveModeFromDisplayConfig;
    private boolean mDeviceIdleMode;
    int[] mDeviceIdleTempWhitelist;
    int[] mDeviceIdleWhitelist;
    protected int mDirty;
    private DisplayManagerInternal mDisplayManagerInternal;
    private final DisplayPowerCallbacks mDisplayPowerCallbacks;
    private final DisplayPowerRequest mDisplayPowerRequest;
    private boolean mDisplayReady;
    private final SuspendBlocker mDisplaySuspendBlocker;
    private int mDockState;
    private boolean mDoubleTapWakeEnabled;
    private boolean mDozeAfterScreenOffConfig;
    private int mDozeScreenBrightnessOverrideFromDreamManager;
    private int mDozeScreenStateOverrideFromDreamManager;
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
    private boolean mForceDoze;
    private boolean mHalAutoSuspendModeEnabled;
    private boolean mHalInteractiveModeEnabled;
    protected final PowerManagerHandler mHandler;
    private final ServiceThread mHandlerThread;
    private boolean mHoldingDisplaySuspendBlocker;
    private boolean mHoldingWakeLockSuspendBlocker;
    private IHwPowerInfoService mHwPowerInfoService;
    private boolean mIsPowered;
    private long mLastInteractivePowerHintTime;
    private long mLastScreenBrightnessBoostTime;
    protected long mLastSleepTime;
    private long mLastUserActivityTime;
    private long mLastUserActivityTimeNoChangeLights;
    protected long mLastWakeTime;
    private long mLastWarningAboutUserActivityPermission;
    private boolean mLightDeviceIdleMode;
    protected LightsManager mLightsManager;
    protected final Object mLock;
    private boolean mLowPowerModeEnabled;
    private final ArrayList<LowPowerModeListener> mLowPowerModeListeners;
    private boolean mLowPowerModeSetting;
    private int mMaximumScreenDimDurationConfig;
    private float mMaximumScreenDimRatioConfig;
    private int mMaximumScreenOffTimeoutFromDeviceAdmin;
    private int mMinimumScreenOffTimeoutConfig;
    protected Notifier mNotifier;
    private long mOverriddenTimeout;
    private int mPlugType;
    private WindowManagerPolicy mPolicy;
    protected boolean mProximityPositive;
    protected boolean mRequestWaitForNegativeProximity;
    private boolean mSandmanScheduled;
    private boolean mSandmanSummoned;
    private float mScreenAutoBrightnessAdjustmentSetting;
    private boolean mScreenBrightnessBoostInProgress;
    private int mScreenBrightnessModeSetting;
    private int mScreenBrightnessOverrideFromWindowManager;
    private int mScreenBrightnessSetting;
    private int mScreenBrightnessSettingDefault;
    private int mScreenBrightnessSettingMaximum;
    private int mScreenBrightnessSettingMinimum;
    private int mScreenOffTimeoutSetting;
    private boolean mScreenTimeoutFlag;
    private SettingsObserver mSettingsObserver;
    private int mSleepTimeoutSetting;
    private int mSmartBacklightEnableSetting;
    private boolean mStayOn;
    private int mStayOnWhilePluggedInSetting;
    private boolean mSupportsDoubleTapWakeConfig;
    private final ArrayList<SuspendBlocker> mSuspendBlockers;
    private boolean mSuspendWhenScreenOffDueToProximityConfig;
    private boolean mSystemReady;
    private float mTemporaryScreenAutoBrightnessAdjustmentSettingOverride;
    private int mTemporaryScreenAutoBrightnessSettingOverride;
    private int mTemporaryScreenBrightnessSettingOverride;
    private boolean mTheaterModeEnabled;
    private final SparseIntArray mUidState;
    private int mUserActivitySummary;
    private long mUserActivityTimeoutOverrideFromWindowManager;
    private boolean mUserInactiveOverrideFromWindowManager;
    private final IVrStateCallbacks mVrStateCallbacks;
    private int mWakeLockSummary;
    private final SuspendBlocker mWakeLockSuspendBlocker;
    protected final ArrayList<WakeLock> mWakeLocks;
    private boolean mWakeUpWhenPluggedOrUnpluggedConfig;
    private boolean mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig;
    protected int mWakefulness;
    private boolean mWakefulnessChanging;
    private WirelessChargerDetector mWirelessChargerDetector;
    private boolean misBetaUser;

    /* renamed from: com.android.server.power.PowerManagerService.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ boolean val$lowPowerModeEnabled;

        AnonymousClass4(boolean val$lowPowerModeEnabled) {
            this.val$lowPowerModeEnabled = val$lowPowerModeEnabled;
        }

        public void run() {
            PowerManagerService.this.mContext.sendBroadcast(new Intent("android.os.action.POWER_SAVE_MODE_CHANGING").putExtra("mode", PowerManagerService.this.mLowPowerModeEnabled).addFlags(1073741824));
            synchronized (PowerManagerService.this.mLock) {
                ArrayList<LowPowerModeListener> listeners = new ArrayList(PowerManagerService.this.mLowPowerModeListeners);
            }
            for (int i = PowerManagerService.HALT_MODE_SHUTDOWN; i < listeners.size(); i += PowerManagerService.WAKE_LOCK_CPU) {
                ((LowPowerModeListener) listeners.get(i)).onLowPowerModeChanged(this.val$lowPowerModeEnabled);
            }
            Intent intent = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED");
            intent.addFlags(1073741824);
            PowerManagerService.this.mContext.sendBroadcast(intent);
            PowerManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL"), UserHandle.ALL, "android.permission.DEVICE_POWER");
        }
    }

    /* renamed from: com.android.server.power.PowerManagerService.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ boolean val$confirm;
        final /* synthetic */ int val$haltMode;
        final /* synthetic */ String val$reason;

        AnonymousClass5(int val$haltMode, boolean val$confirm, String val$reason) {
            this.val$haltMode = val$haltMode;
            this.val$confirm = val$confirm;
            this.val$reason = val$reason;
        }

        public void run() {
            synchronized (this) {
                if (this.val$haltMode == PowerManagerService.WAKE_LOCK_SCREEN_BRIGHT) {
                    ShutdownThread.rebootSafeMode(PowerManagerService.this.mContext, this.val$confirm);
                } else if (this.val$haltMode == PowerManagerService.WAKE_LOCK_CPU) {
                    ShutdownThread.reboot(PowerManagerService.this.mContext, this.val$reason, this.val$confirm);
                } else {
                    ShutdownThread.shutdown(PowerManagerService.this.mContext, this.val$reason, this.val$confirm);
                }
            }
        }
    }

    /* renamed from: com.android.server.power.PowerManagerService.6 */
    class AnonymousClass6 extends Thread {
        final /* synthetic */ String val$message;

        AnonymousClass6(String $anonymous0, String val$message) {
            this.val$message = val$message;
            super($anonymous0);
        }

        public void run() {
            throw new RuntimeException(this.val$message);
        }
    }

    private final class BatteryReceiver extends BroadcastReceiver {
        private BatteryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.handleBatteryStateChangedLocked();
                if (PowerManagerService.this.mCust != null) {
                    PowerManagerService.this.mCust.handleDreamLocked();
                }
            }
        }
    }

    private final class BinderService extends Stub {
        private static final int MAX_DEFAULT_BRIGHTNESS = 255;
        private static final int SEEK_BAR_RANGE = 10000;
        private double mCovertFactor;
        private int mMaximumBrightness;
        private int mMinimumBrightness;

        private BinderService() {
            this.mCovertFactor = 1.7999999523162842d;
            this.mMinimumBrightness = PowerManagerService.WAKE_LOCK_SCREEN_DIM;
            this.mMaximumBrightness = MAX_DEFAULT_BRIGHTNESS;
        }

        public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uid) {
            if (uid < 0) {
                uid = Binder.getCallingUid();
            }
            acquireWakeLock(lock, flags, tag, packageName, new WorkSource(uid), null);
        }

        public void setStartDreamFromOtherFlag(boolean flag) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (PowerManagerService.this.mCust != null) {
                PowerManagerService.this.mCust.setStartDreamFromUser(flag);
            }
        }

        public void setMirrorLinkPowerStatus(boolean status) {
            ((LightsManager) PowerManagerService.this.getLocalService(LightsManager.class)).getLight(PowerManagerService.HALT_MODE_SHUTDOWN).setMirrorLinkBrightnessStatus(PowerManagerService.mSupportAod);
            InputManager.getInstance().setMirrorLinkInputStatus(PowerManagerService.mSupportAod);
            Slog.d(PowerManagerService.TAG, "setMirrorLinkPowerStatus status" + status);
        }

        public boolean startDream() {
            boolean z = PowerManagerService.mSupportAod;
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (PowerManagerService.this.mCust == null) {
                return PowerManagerService.mSupportAod;
            }
            HwCustPowerManagerService -get2 = PowerManagerService.this.mCust;
            if (PowerManagerService.this.mWakefulness == PowerManagerService.MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                z = true;
            }
            return -get2.startDream(z);
        }

        public boolean stopDream() {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (PowerManagerService.this.mCust != null) {
                return PowerManagerService.this.mCust.stopDream();
            }
            return PowerManagerService.mSupportAod;
        }

        public void powerHint(int hintId, int data) {
            if (PowerManagerService.this.mSystemReady) {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                PowerManagerService.this.powerHintInternal(hintId, data);
            }
        }

        public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) {
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            } else if (packageName == null) {
                throw new IllegalArgumentException("packageName must not be null");
            } else if (!PowerManagerService.this.isAppWakeLockFilterTag(flags, packageName, ws)) {
                PowerManager.validateWakeLockParameters(flags, tag);
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
                if ((flags & PowerManagerService.WAKE_LOCK_DOZE) != 0) {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
                }
                if (ws == null || ws.size() == 0) {
                    ws = null;
                } else {
                    PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                }
                int uid = Binder.getCallingUid();
                int pid = Binder.getCallingPid();
                long ident = Binder.clearCallingIdentity();
                try {
                    if (!PowerManagerService.this.acquireProxyWakeLock(lock, flags, tag, packageName, ws, historyTag, uid, pid)) {
                        PowerManagerService.this.acquireWakeLockInternal(lock, flags, tag, packageName, ws, historyTag, uid, pid);
                        Binder.restoreCallingIdentity(ident);
                    }
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void releaseWakeLock(IBinder lock, int flags) {
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.releaseProxyWakeLock(lock);
                PowerManagerService.this.releaseWakeLockInternal(lock, flags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public int setColorTemperature(int colorTemper) {
            if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE != UserHandle.getAppId(Binder.getCallingUid())) {
                Slog.e(PowerManagerService.TAG, "permission not allowed. uid = " + Binder.getCallingUid());
                return PowerManagerService.DEFAULT_SLEEP_TIMEOUT;
            }
            Slog.d(PowerManagerService.TAG, "setColorTemperature" + colorTemper);
            return PowerManagerService.this.setColorTemperatureInternal(colorTemper);
        }

        public int updateRgbGamma(float red, float green, float blue) {
            if (ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE == UserHandle.getAppId(Binder.getCallingUid())) {
                return PowerManagerService.this.updateRgbGammaInternal(red, green, blue);
            }
            Slog.e(PowerManagerService.TAG, "permission not allowed. uid = " + Binder.getCallingUid());
            return PowerManagerService.DEFAULT_SLEEP_TIMEOUT;
        }

        public void updateWakeLockUids(IBinder lock, int[] uids) {
            WorkSource ws = null;
            if (uids != null) {
                ws = new WorkSource();
                for (int i = PowerManagerService.HALT_MODE_SHUTDOWN; i < uids.length; i += PowerManagerService.WAKE_LOCK_CPU) {
                    ws.add(uids[i]);
                }
            }
            updateWakeLockWorkSource(lock, ws, null);
        }

        public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) {
            if (lock == null) {
                throw new IllegalArgumentException("lock must not be null");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.WAKE_LOCK", null);
            if (ws == null || ws.size() == 0) {
                ws = null;
            } else {
                PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
            }
            int callingUid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                if (!PowerManagerService.this.updateProxyWakeLockWorkSource(lock, ws, historyTag, callingUid)) {
                    PowerManagerService.this.updateWakeLockWorkSourceInternal(lock, ws, historyTag, callingUid);
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (IllegalArgumentException e) {
                Slog.e(PowerManagerService.TAG, "Exception when search wack lock :" + e);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isWakeLockLevelSupported(int level) {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap4 = PowerManagerService.this.isWakeLockLevelSupportedInternal(level);
                return -wrap4;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void userActivity(long eventTime, int event, int flags) {
            long now = SystemClock.uptimeMillis();
            if (PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0 && PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.USER_ACTIVITY") != 0) {
                synchronized (PowerManagerService.this.mLock) {
                    if (now >= PowerManagerService.this.mLastWarningAboutUserActivityPermission + 300000) {
                        PowerManagerService.this.mLastWarningAboutUserActivityPermission = now;
                        Slog.w(PowerManagerService.TAG, "Ignoring call to PowerManager.userActivity() because the caller does not have DEVICE_POWER or USER_ACTIVITY permission.  Please fix your app!   pid=" + Binder.getCallingPid() + " uid=" + Binder.getCallingUid());
                    }
                }
            } else if (eventTime > now) {
                throw new IllegalArgumentException("event time must not be in the future");
            } else {
                int uid = Binder.getCallingUid();
                long ident = Binder.clearCallingIdentity();
                try {
                    PowerManagerService.this.userActivityInternal(eventTime, event, flags, uid);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }

        public void wakeUp(long eventTime, String reason, String opPackageName) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            if (Jlog.isPerfTest()) {
                Jlog.i(2202, "JL_PWRSCRON_PMS_WAKEUP");
            }
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.wakeUpInternal(eventTime, reason, uid, opPackageName, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void goToSleep(long eventTime, int reason, int flags) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.goToSleepInternal(eventTime, reason, flags, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void nap(long eventTime) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.napInternal(eventTime, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isInteractive() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap1 = PowerManagerService.this.isInteractiveInternal();
                return -wrap1;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isPowerSaveMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap2 = PowerManagerService.this.isLowPowerModeInternal();
                return -wrap2;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean setPowerSaveMode(boolean mode) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap5 = PowerManagerService.this.setLowPowerModeInternal(mode);
                return -wrap5;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean isDeviceIdleModeInternal = PowerManagerService.this.isDeviceIdleModeInternal();
                return isDeviceIdleModeInternal;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isLightDeviceIdleMode() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean isLightDeviceIdleModeInternal = PowerManagerService.this.isLightDeviceIdleModeInternal();
                return isLightDeviceIdleModeInternal;
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
            Flog.e(1600, "PowerManagerService reboot_reason:" + reason + ", uid=" + uid + ", pid=" + Binder.getCallingPid());
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(PowerManagerService.WAKE_LOCK_CPU, confirm, reason, wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void rebootSafeMode(boolean confirm, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(PowerManagerService.WAKE_LOCK_SCREEN_BRIGHT, confirm, "safemode", wait);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void shutdown(boolean confirm, String reason, boolean wait) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.REBOOT", null);
            int uid = Binder.getCallingUid();
            Flog.e(1600, "PowerManagerService shutdown  uid=" + uid + ", pid=" + Binder.getCallingPid());
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.shutdownOrRebootInternal(PowerManagerService.HALT_MODE_SHUTDOWN, confirm, reason, wait);
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
                PowerManagerService.this.setTemporaryScreenBrightnessSettingOverrideInternal(brightness);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void setTemporaryScreenAutoBrightnessAdjustmentSettingOverride(float adj) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal(adj);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
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
            if (SystemProperties.getInt("ro.config.hw_high_bright_mode", PowerManagerService.WAKE_LOCK_CPU) == PowerManagerService.WAKE_LOCK_CPU) {
                this.mMaximumBrightness = PowerManagerService.this.mDisplayManagerInternal.getMaxBrightnessForSeekbar();
            } else {
                this.mMaximumBrightness = MAX_DEFAULT_BRIGHTNESS;
            }
            return Math.round((((float) Math.pow((double) (((float) progress) / 10000.0f), this.mCovertFactor)) * ((float) (this.mMaximumBrightness - this.mMinimumBrightness))) + ((float) this.mMinimumBrightness));
        }

        public float convertBrightnessToSeekbarPercentage(float brightness) {
            if (SystemProperties.getInt("ro.config.hw_high_bright_mode", PowerManagerService.WAKE_LOCK_CPU) == PowerManagerService.WAKE_LOCK_CPU) {
                this.mMaximumBrightness = PowerManagerService.this.mDisplayManagerInternal.getMaxBrightnessForSeekbar();
            } else {
                this.mMaximumBrightness = MAX_DEFAULT_BRIGHTNESS;
            }
            if (brightness > ((float) this.mMaximumBrightness)) {
                if (PowerManagerService.DEBUG) {
                    Slog.i(PowerManagerService.TAG, "HBM brightness=" + brightness + " >Max=" + this.mMaximumBrightness);
                }
                brightness = (float) this.mMaximumBrightness;
            }
            return (float) Math.pow((double) ((brightness - ((float) this.mMinimumBrightness)) / ((float) (this.mMaximumBrightness - this.mMinimumBrightness))), 1.0d / this.mCovertFactor);
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

        public void boostScreenBrightness(long eventTime) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.boostScreenBrightnessInternal(eventTime, uid);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public boolean isScreenBrightnessBoosted() {
            long ident = Binder.clearCallingIdentity();
            try {
                boolean -wrap3 = PowerManagerService.this.isScreenBrightnessBoostedInternal();
                return -wrap3;
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (PowerManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump PowerManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.dumpInternal(pw);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void startWakeUpReady(long eventTime, String opPackageName) {
            if (eventTime > SystemClock.uptimeMillis()) {
                throw new IllegalArgumentException("event time must not be in the future");
            }
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.startWakeUpReadyInternal(eventTime, uid, opPackageName);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void stopWakeUpReady(long eventTime, boolean enableBright, String opPackageName) {
            PowerManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                PowerManagerService.this.stopWakeUpReadyInternal(eventTime, uid, enableBright, opPackageName);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        public void updateBlockedUids(int uid, boolean isBlocked) {
        }

        public boolean isHighPrecision() {
            boolean isHighPrecision;
            synchronized (PowerManagerService.this.mLock) {
                isHighPrecision = PowerManagerService.this.mLightsManager.getLight(PowerManagerService.HALT_MODE_SHUTDOWN).isHighPrecision();
            }
            return isHighPrecision;
        }

        public boolean isUsingSkipWakeLock(int uid, String tag) {
            return PowerManagerService.this.isSkipWakeLockUsing(uid, tag);
        }

        public void regeditAodStateCallback(IAodStateCallback callback) {
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService regeditAodStateCallback()");
            if (PowerManagerService.mSupportAod) {
                PowerManagerService.this.mPolicy.regeditAodStateCallback(callback);
            }
        }

        public void unregeditAodStateCallback(IAodStateCallback callback) {
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService unregeditAodStateCallback()");
            if (PowerManagerService.mSupportAod) {
                PowerManagerService.this.mPolicy.unregeditAodStateCallback(callback);
            }
        }

        public void setAodState(int globalState, int alpmMode) {
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService setAodState()");
            if (PowerManagerService.mSupportAod) {
                Slog.d(PowerManagerService.TAG, "setAodStateBySysfs:  globalState=" + globalState + ", AlpmMode=" + alpmMode);
                if (globalState != PowerManagerService.DEFAULT_SLEEP_TIMEOUT) {
                    PowerManagerService.this.setAodStateBySysfs(PowerManagerService.AOD_STATE_CMD, globalState);
                }
                if (alpmMode != PowerManagerService.DEFAULT_SLEEP_TIMEOUT) {
                    PowerManagerService.this.setAodStateBySysfs(PowerManagerService.AOD_MODE_CMD, alpmMode);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setDozeOverrideFromAod(int screenState, int screenBrightness, IBinder binder) {
            Slog.i(PowerManagerService.TAG, "AOD PowerManagerService setDozeOverrideFromAod()");
            if (PowerManagerService.mSupportAod) {
                synchronized (PowerManagerService.this.mLock) {
                    switch (screenState) {
                        case PowerManagerService.HALT_MODE_SHUTDOWN /*0*/:
                        case PowerManagerService.WAKE_LOCK_SCREEN_BRIGHT /*2*/:
                            PowerManagerService.this.mForceDoze = PowerManagerService.mSupportAod;
                            break;
                        case PowerManagerService.WAKE_LOCK_CPU /*1*/:
                            PowerManagerService.this.mForceDoze = PowerManagerService.mSupportAod;
                            PowerManagerService.this.setWakefulnessLocked(PowerManagerService.HALT_MODE_SHUTDOWN, PowerManagerService.HALT_MODE_SHUTDOWN);
                            break;
                        case PowerManagerService.MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT /*3*/:
                            if (!(PowerManagerService.this.mForceDoze || PowerManagerInternal.isInteractive(PowerManagerService.this.mWakefulness))) {
                                PowerManagerService.this.setWakefulnessLocked(PowerManagerService.MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, PowerManagerService.HALT_MODE_SHUTDOWN);
                                PowerManagerService.this.mForceDoze = true;
                                break;
                            }
                        case PowerManagerService.WAKE_LOCK_SCREEN_DIM /*4*/:
                            break;
                        default:
                            PowerManagerService.this.mForceDoze = PowerManagerService.mSupportAod;
                            break;
                    }
                }
            }
        }
    }

    private final class DockReceiver extends BroadcastReceiver {
        private DockReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                int dockState = intent.getIntExtra("android.intent.extra.DOCK_STATE", PowerManagerService.HALT_MODE_SHUTDOWN);
                if (PowerManagerService.this.mDockState != dockState) {
                    PowerManagerService.this.mDockState = dockState;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= PowerManagerService.DIRTY_DOCK_STATE;
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

    @IntDef({0, 1, 2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HaltMode {
    }

    private final class LocalService extends PowerManagerInternal {
        private LocalService() {
        }

        public void setScreenBrightnessOverrideFromWindowManager(int screenBrightness) {
            if (screenBrightness < PowerManagerService.DEFAULT_SLEEP_TIMEOUT) {
                screenBrightness = PowerManagerService.DEFAULT_SLEEP_TIMEOUT;
            }
            PowerManagerService.this.setScreenBrightnessOverrideFromWindowManagerInternal(screenBrightness);
        }

        public void setButtonBrightnessOverrideFromWindowManager(int screenBrightness) {
        }

        public void setDozeOverrideFromDreamManager(int screenState, int screenBrightness) {
            switch (screenState) {
                case PowerManagerService.HALT_MODE_SHUTDOWN /*0*/:
                case PowerManagerService.WAKE_LOCK_CPU /*1*/:
                case PowerManagerService.WAKE_LOCK_SCREEN_BRIGHT /*2*/:
                case PowerManagerService.MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT /*3*/:
                case PowerManagerService.WAKE_LOCK_SCREEN_DIM /*4*/:
                    break;
                default:
                    screenState = PowerManagerService.HALT_MODE_SHUTDOWN;
                    break;
            }
            if (screenBrightness < PowerManagerService.DEFAULT_SLEEP_TIMEOUT || screenBrightness > RampAnimator.DEFAULT_MAX_BRIGHTNESS) {
                screenBrightness = PowerManagerService.DEFAULT_SLEEP_TIMEOUT;
            }
            PowerManagerService.this.setDozeOverrideFromDreamManagerInternal(screenState, screenBrightness);
        }

        public void setUserInactiveOverrideFromWindowManager() {
            PowerManagerService.this.setUserInactiveOverrideFromWindowManagerInternal();
        }

        public void setUserActivityTimeoutOverrideFromWindowManager(long timeoutMillis) {
            PowerManagerService.this.setUserActivityTimeoutOverrideFromWindowManagerInternal(timeoutMillis);
        }

        public void setMaximumScreenOffTimeoutFromDeviceAdmin(int timeMs) {
            PowerManagerService.this.setMaximumScreenOffTimeoutFromDeviceAdminInternal(timeMs);
        }

        public boolean getLowPowerModeEnabled() {
            boolean -get10;
            synchronized (PowerManagerService.this.mLock) {
                -get10 = PowerManagerService.this.mLowPowerModeEnabled;
            }
            return -get10;
        }

        public void registerLowPowerModeObserver(LowPowerModeListener listener) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mLowPowerModeListeners.add(listener);
            }
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

        public void updateUidProcState(int uid, int procState) {
            PowerManagerService.this.updateUidProcStateInternal(uid, procState);
        }

        public void uidGone(int uid) {
            PowerManagerService.this.uidGoneInternal(uid);
        }

        public void powerHint(int hintId, int data) {
            PowerManagerService.this.powerHintInternal(hintId, data);
        }
    }

    protected final class PowerManagerHandler extends Handler {
        public PowerManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PowerManagerService.WAKE_LOCK_CPU /*1*/:
                    PowerManagerService.this.handleUserActivityTimeout();
                    HwFrameworkFactory.getHwNsdImpl().StopSdrForSpecial("autosleep", PowerManagerService.DEFAULT_SLEEP_TIMEOUT);
                case PowerManagerService.WAKE_LOCK_SCREEN_BRIGHT /*2*/:
                    PowerManagerService.this.handleSandman();
                case PowerManagerService.MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT /*3*/:
                    PowerManagerService.this.handleScreenBrightnessBoostTimeout();
                case PowerManagerService.WAKE_LOCK_SCREEN_DIM /*4*/:
                    PowerManagerService.this.handleWaitBrightTimeout();
                case PowerManagerService.POWER_HINT_LOW_POWER /*5*/:
                    PowerManagerService.this.mContext.sendBroadcast(new Intent("wakeup_by_power"), "com.android.keyguard.permission.POWER_KEY_WAKEUP");
                default:
            }
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

        protected void finalize() throws Throwable {
            try {
                if (this.mReferenceCount != 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was finalized without being released!");
                    this.mReferenceCount = PowerManagerService.HALT_MODE_SHUTDOWN;
                    PowerManagerService.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, PowerManagerService.HALT_MODE_SHUTDOWN);
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }

        public void acquire() {
            synchronized (this) {
                this.mReferenceCount += PowerManagerService.WAKE_LOCK_CPU;
                if (this.mReferenceCount == PowerManagerService.WAKE_LOCK_CPU) {
                    if (PowerManagerService.DEBUG_SPEW) {
                        Slog.d(PowerManagerService.TAG, "Acquiring suspend blocker \"" + this.mName + "\".");
                    }
                    Trace.asyncTraceBegin(131072, this.mTraceName, PowerManagerService.HALT_MODE_SHUTDOWN);
                    PowerManagerService.nativeAcquireSuspendBlocker(this.mName);
                }
            }
        }

        public void release() {
            synchronized (this) {
                this.mReferenceCount += PowerManagerService.DEFAULT_SLEEP_TIMEOUT;
                if (this.mReferenceCount == 0) {
                    if (PowerManagerService.DEBUG_SPEW) {
                        Slog.d(PowerManagerService.TAG, "Releasing suspend blocker \"" + this.mName + "\".");
                    }
                    PowerManagerService.nativeReleaseSuspendBlocker(this.mName);
                    Trace.asyncTraceEnd(131072, this.mTraceName, PowerManagerService.HALT_MODE_SHUTDOWN);
                } else if (this.mReferenceCount < 0) {
                    Slog.wtf(PowerManagerService.TAG, "Suspend blocker \"" + this.mName + "\" was released without being acquired!", new Throwable());
                    this.mReferenceCount = PowerManagerService.HALT_MODE_SHUTDOWN;
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
    }

    private final class UserSwitchedReceiver extends BroadcastReceiver {
        private UserSwitchedReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            synchronized (PowerManagerService.this.mLock) {
                PowerManagerService.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", PowerManagerService.HALT_MODE_SHUTDOWN);
                if (PowerManagerService.DEBUG) {
                    Slog.d(PowerManagerService.TAG, "user changed:mCurrentUserId=" + PowerManagerService.this.mCurrentUserId);
                }
                PowerManagerService.this.handleSettingsChangedLocked();
            }
            PowerManagerService.this.setColorTemperatureAccordingToSetting();
        }
    }

    protected final class WakeLock implements DeathRecipient {
        public boolean mDisabled;
        public int mFlags;
        public String mHistoryTag;
        public final IBinder mLock;
        public boolean mNotifiedAcquired;
        public final int mOwnerPid;
        public final int mOwnerUid;
        public final String mPackageName;
        public String mTag;
        public WorkSource mWorkSource;

        public WakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid) {
            this.mLock = lock;
            this.mFlags = flags;
            this.mTag = tag;
            this.mPackageName = packageName;
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
            this.mHistoryTag = historyTag;
            this.mOwnerUid = ownerUid;
            this.mOwnerPid = ownerPid;
        }

        public void binderDied() {
            PowerManagerService.this.handleWakeLockDeath(this);
        }

        public boolean hasSameProperties(int flags, String tag, WorkSource workSource, int ownerUid, int ownerPid) {
            if (this.mFlags == flags && this.mTag.equals(tag) && hasSameWorkSource(workSource) && this.mOwnerUid == ownerUid && this.mOwnerPid == ownerPid) {
                return true;
            }
            return PowerManagerService.mSupportAod;
        }

        public void updateProperties(int flags, String tag, String packageName, WorkSource workSource, String historyTag, int ownerUid, int ownerPid) {
            if (!this.mPackageName.equals(packageName)) {
                throw new IllegalStateException("Existing wake lock package name changed: " + this.mPackageName + " to " + packageName);
            } else if (this.mOwnerUid != ownerUid) {
                throw new IllegalStateException("Existing wake lock uid changed: " + this.mOwnerUid + " to " + ownerUid);
            } else if (this.mOwnerPid != ownerPid) {
                throw new IllegalStateException("Existing wake lock pid changed: " + this.mOwnerPid + " to " + ownerPid);
            } else {
                this.mFlags = flags;
                this.mTag = tag;
                updateWorkSource(workSource);
                this.mHistoryTag = historyTag;
            }
        }

        public boolean hasSameWorkSource(WorkSource workSource) {
            return Objects.equal(this.mWorkSource, workSource);
        }

        public void updateWorkSource(WorkSource workSource) {
            this.mWorkSource = PowerManagerService.copyWorkSource(workSource);
        }

        public String toString() {
            return "mLock:" + Objects.hashCode(this.mLock) + " " + getLockLevelString() + " '" + this.mTag + "'" + getLockFlagsString() + (this.mDisabled ? " DISABLED" : "") + " (uid=" + this.mOwnerUid + ", pid=" + this.mOwnerPid + ", ws=" + this.mWorkSource + ")";
        }

        private String getLockLevelString() {
            switch (this.mFlags & 65535) {
                case PowerManagerService.WAKE_LOCK_CPU /*1*/:
                    return "PARTIAL_WAKE_LOCK             ";
                case H.REMOVE_STARTING /*6*/:
                    return "SCREEN_DIM_WAKE_LOCK          ";
                case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                    return "SCREEN_BRIGHT_WAKE_LOCK       ";
                case H.DO_ANIMATION_CALLBACK /*26*/:
                    return "FULL_WAKE_LOCK                ";
                case PowerManagerService.WAKE_LOCK_STAY_AWAKE /*32*/:
                    return "PROXIMITY_SCREEN_OFF_WAKE_LOCK";
                case PowerManagerService.WAKE_LOCK_DOZE /*64*/:
                    return "DOZE_WAKE_LOCK                ";
                case PowerManagerService.WAKE_LOCK_DRAW /*128*/:
                    return "DRAW_WAKE_LOCK                ";
                default:
                    return "???                           ";
            }
        }

        private String getLockFlagsString() {
            String result = "";
            if ((this.mFlags & 268435456) != 0) {
                result = result + " ACQUIRE_CAUSES_WAKEUP";
            }
            if ((this.mFlags & 536870912) != 0) {
                return result + " ON_AFTER_RELEASE";
            }
            return result;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.power.PowerManagerService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.power.PowerManagerService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.PowerManagerService.<clinit>():void");
    }

    private static native void nativeAcquireSuspendBlocker(String str);

    private native void nativeInit();

    private static native void nativeReleaseSuspendBlocker(String str);

    private static native void nativeSendPowerHint(int i, int i2);

    private static native void nativeSetAutoSuspend(boolean z);

    private static native void nativeSetFeature(int i, int i2);

    public static native void nativeSetFsEnable(boolean z);

    private static native void nativeSetInteractive(boolean z);

    public PowerManagerService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mSuspendBlockers = new ArrayList();
        this.mWakeLocks = new ArrayList();
        this.mDisplayPowerRequest = new DisplayPowerRequest();
        this.mDockState = HALT_MODE_SHUTDOWN;
        this.mMaximumScreenOffTimeoutFromDeviceAdmin = Integer.MAX_VALUE;
        this.mScreenBrightnessOverrideFromWindowManager = DEFAULT_SLEEP_TIMEOUT;
        this.mOverriddenTimeout = -1;
        this.mUserActivityTimeoutOverrideFromWindowManager = -1;
        this.mTemporaryScreenBrightnessSettingOverride = DEFAULT_SLEEP_TIMEOUT;
        this.mTemporaryScreenAutoBrightnessSettingOverride = DEFAULT_SLEEP_TIMEOUT;
        this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
        this.mBacklightBrightness = new BacklightBrightness(RampAnimator.DEFAULT_MAX_BRIGHTNESS, HALT_MODE_SHUTDOWN, WAKE_LOCK_DRAW);
        this.mDozeScreenStateOverrideFromDreamManager = HALT_MODE_SHUTDOWN;
        this.mDozeScreenBrightnessOverrideFromDreamManager = DEFAULT_SLEEP_TIMEOUT;
        this.mLastWarningAboutUserActivityPermission = Long.MIN_VALUE;
        this.mDeviceIdleWhitelist = new int[HALT_MODE_SHUTDOWN];
        this.mDeviceIdleTempWhitelist = new int[HALT_MODE_SHUTDOWN];
        this.mUidState = new SparseIntArray();
        this.mLowPowerModeListeners = new ArrayList();
        this.mCurrentUserId = HALT_MODE_SHUTDOWN;
        this.misBetaUser = mSupportAod;
        this.mDisplayPowerCallbacks = new DisplayPowerCallbacks() {
            private int mDisplayState;

            {
                this.mDisplayState = PowerManagerService.HALT_MODE_SHUTDOWN;
            }

            public void onStateChanged() {
                synchronized (PowerManagerService.this.mLock) {
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= PowerManagerService.WAKE_LOCK_BUTTON_BRIGHT;
                    PowerManagerService.this.updatePowerStateLocked();
                }
            }

            public void onProximityPositive() {
                synchronized (PowerManagerService.this.mLock) {
                    PowerManagerService.this.mProximityPositive = true;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= PowerManagerService.DIRTY_PROXIMITY_POSITIVE;
                    if (!PowerManagerService.this.isPhoneHeldWakeLock()) {
                        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerServiceonProximityPositive -> updatePowerStateLocked");
                        PowerManagerService.this.updatePowerStateLocked();
                    } else if (PowerManagerService.this.goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), PowerManagerService.POWER_HINT_VR_MODE, PowerManagerService.HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE)) {
                        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerServiceonProximityPositivebyPhone -> updatePowerStateLocked");
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }

            public void onProximityNegative() {
                synchronized (PowerManagerService.this.mLock) {
                    PowerManagerService.this.mProximityPositive = PowerManagerService.mSupportAod;
                    PowerManagerService powerManagerService = PowerManagerService.this;
                    powerManagerService.mDirty |= PowerManagerService.DIRTY_PROXIMITY_POSITIVE;
                    Jlog.d(77, "JL_WAKEUP_REASON_PROX");
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerServiceonProximityNegative -> updatePowerStateLocked");
                    PowerManagerService.this.userActivityNoUpdateLocked(SystemClock.uptimeMillis(), PowerManagerService.HALT_MODE_SHUTDOWN, PowerManagerService.HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                    if (PowerManagerService.this.wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:POWER", ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, PowerManagerService.this.mContext.getOpPackageName(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) || PowerManagerService.this.wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "onProximityNegative", ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, PowerManagerService.this.mContext.getOpPackageName(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE)) {
                        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerServiceonProximityNegative by Phone");
                        PowerManagerService.this.updatePowerStateLocked();
                    }
                }
            }

            public void onDisplayStateChange(int state) {
                synchronized (PowerManagerService.this.mLock) {
                    if (this.mDisplayState != state) {
                        this.mDisplayState = state;
                        if (state == PowerManagerService.WAKE_LOCK_CPU || (PowerManagerService.mSupportAod && state == PowerManagerService.WAKE_LOCK_SCREEN_DIM)) {
                            if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                                PowerManagerService.this.setHalInteractiveModeLocked(PowerManagerService.mSupportAod);
                            }
                            if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                                PowerManagerService.this.setHalAutoSuspendModeLocked(true);
                            }
                        } else {
                            if (!PowerManagerService.this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
                                PowerManagerService.this.setHalAutoSuspendModeLocked(PowerManagerService.mSupportAod);
                            }
                            if (!PowerManagerService.this.mDecoupleHalInteractiveModeFromDisplayConfig) {
                                PowerManagerService.this.setHalInteractiveModeLocked(true);
                            }
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
        this.mVrStateCallbacks = new IVrStateCallbacks.Stub() {
            public void onVrStateChanged(boolean enabled) {
                PowerManagerService.this.powerHintInternal(PowerManagerService.POWER_HINT_VR_MODE, enabled ? PowerManagerService.WAKE_LOCK_CPU : PowerManagerService.HALT_MODE_SHUTDOWN);
            }
        };
        this.inVdriveBackLightMode = mSupportAod;
        this.mForceDoze = mSupportAod;
        this.mContext = context;
        Object[] objArr = new Object[WAKE_LOCK_CPU];
        objArr[HALT_MODE_SHUTDOWN] = this.mContext;
        this.mCust = (HwCustPowerManagerService) HwCustUtils.createObj(HwCustPowerManagerService.class, objArr);
        this.mHandlerThread = new ServiceThread(TAG, -4, mSupportAod);
        this.mHandlerThread.start();
        this.mHandler = new PowerManagerHandler(this.mHandlerThread.getLooper());
        synchronized (this.mLock) {
            this.mWakeLockSuspendBlocker = createSuspendBlockerLocked("PowerManagerService.WakeLocks");
            this.mDisplaySuspendBlocker = createSuspendBlockerLocked("PowerManagerService.Display");
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
            this.mHalAutoSuspendModeEnabled = mSupportAod;
            this.mHalInteractiveModeEnabled = true;
            this.mWakefulness = WAKE_LOCK_CPU;
            nativeInit();
            nativeSetAutoSuspend(mSupportAod);
            nativeSetInteractive(true);
            nativeSetFeature(WAKE_LOCK_CPU, HALT_MODE_SHUTDOWN);
        }
    }

    public void onStart() {
        publishBinderService("power", new BinderService());
        publishLocalService(PowerManagerInternal.class, new LocalService());
        Watchdog.getInstance().addMonitor(this);
        Watchdog.getInstance().addThread(this.mHandler);
    }

    public void onBootPhase(int phase) {
        synchronized (this.mLock) {
            if (phase == NetdResponseCode.InterfaceChange) {
                incrementBootCount();
            } else if (phase == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE) {
                long now = SystemClock.uptimeMillis();
                this.mBootCompleted = true;
                this.mDirty |= WAKE_LOCK_PROXIMITY_SCREEN_OFF;
                userActivityNoUpdateLocked(now, HALT_MODE_SHUTDOWN, HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                updatePowerStateLocked();
                if (!ArrayUtils.isEmpty(this.mBootCompletedRunnables)) {
                    Slog.d(TAG, "Posting " + this.mBootCompletedRunnables.length + " delayed runnables");
                    Runnable[] runnableArr = this.mBootCompletedRunnables;
                    int length = runnableArr.length;
                    for (int i = HALT_MODE_SHUTDOWN; i < length; i += WAKE_LOCK_CPU) {
                        BackgroundThread.getHandler().post(runnableArr[i]);
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
            this.mNotifier = new Notifier(Looper.getMainLooper(), this.mContext, this.mBatteryStats, this.mAppOps, createSuspendBlockerLocked("PowerManagerService.Broadcasts"), this.mPolicy);
            this.mWirelessChargerDetector = new WirelessChargerDetector(sensorManager, createSuspendBlockerLocked("PowerManagerService.WirelessChargerDetector"), this.mHandler);
            this.mSettingsObserver = new SettingsObserver(this.mHandler);
            this.mLightsManager = (LightsManager) getLocalService(LightsManager.class);
            this.mAttentionLight = this.mLightsManager.getLight(POWER_HINT_LOW_POWER);
            this.mDisplayManagerInternal.initPowerManagement(this.mDisplayPowerCallbacks, this.mHandler, sensorManager);
            ContentResolver resolver = this.mContext.getContentResolver();
            resolver.registerContentObserver(Secure.getUriFor("screensaver_enabled"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Secure.getUriFor("screensaver_activate_on_sleep"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Secure.getUriFor("screensaver_activate_on_dock"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(System.getUriFor("screen_off_timeout"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Secure.getUriFor("sleep_timeout"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Global.getUriFor("stay_on_while_plugged_in"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(System.getUriFor("screen_brightness"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(System.getUriFor("screen_brightness_mode"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(System.getUriFor("screen_auto_brightness_adj"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Global.getUriFor("low_power"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Global.getUriFor("low_power_trigger_level"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Global.getUriFor("theater_mode_on"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Secure.getUriFor("double_tap_to_wake"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(Secure.getUriFor("brightness_use_twilight"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            resolver.registerContentObserver(System.getUriFor("smart_backlight_enable"), mSupportAod, this.mSettingsObserver, DEFAULT_SLEEP_TIMEOUT);
            if (this.mCust != null) {
                this.mCust.systemReady(this.mBatteryManagerInternal, this.mDreamManager, this.mSettingsObserver);
            }
            try {
                ((IVrManager) getBinderService(VrManagerService.VR_MANAGER_BINDER_SERVICE)).registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e) {
                Slog.e(TAG, "Failed to register VR mode state listener: " + e);
            }
            readConfigurationLocked();
            updateSettingsLocked();
            this.mDirty |= DIRTY_BATTERY_STATE;
            updatePowerStateLocked();
            if (this.mCust != null && this.mCust.isDelayEnanbled()) {
                this.mCust.init(this.mContext);
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.setPriority(ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
        this.mContext.registerReceiver(new BatteryReceiver(), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DREAMING_STARTED");
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        this.mContext.registerReceiver(new DreamReceiver(), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(new UserSwitchedReceiver(), filter, null, this.mHandler);
        filter = new IntentFilter();
        filter.addAction("android.intent.action.DOCK_EVENT");
        this.mContext.registerReceiver(new DockReceiver(), filter, null, this.mHandler);
        if (SystemProperties.getBoolean("ro.control.sleeplog", mSupportAod) && MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT == SystemProperties.getInt("ro.logsystem.usertype", HALT_MODE_SHUTDOWN)) {
            this.misBetaUser = true;
        }
        if (this.misBetaUser) {
            this.mHwPowerInfoService = HwServiceFactory.getHwPowerInfoService(this.mContext, true);
        }
    }

    private void readConfigurationLocked() {
        Resources resources = this.mContext.getResources();
        this.mDecoupleHalAutoSuspendModeFromDisplayConfig = resources.getBoolean(17956979);
        this.mDecoupleHalInteractiveModeFromDisplayConfig = resources.getBoolean(17956980);
        this.mWakeUpWhenPluggedOrUnpluggedConfig = resources.getBoolean(17956902);
        this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig = resources.getBoolean(17956907);
        this.mSuspendWhenScreenOffDueToProximityConfig = resources.getBoolean(17956929);
        this.mDreamsSupportedConfig = resources.getBoolean(17956973);
        this.mDreamsEnabledByDefaultConfig = resources.getBoolean(17956974);
        this.mDreamsActivatedOnSleepByDefaultConfig = resources.getBoolean(17956976);
        this.mDreamsActivatedOnDockByDefaultConfig = resources.getBoolean(17956975);
        this.mDreamsEnabledOnBatteryConfig = resources.getBoolean(17956977);
        this.mDreamsBatteryLevelMinimumWhenPoweredConfig = resources.getInteger(17694855);
        this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig = resources.getInteger(17694856);
        this.mDreamsBatteryLevelDrainCutoffConfig = resources.getInteger(17694857);
        this.mDozeAfterScreenOffConfig = resources.getBoolean(17956978);
        this.mMinimumScreenOffTimeoutConfig = resources.getInteger(17694858);
        this.mMaximumScreenDimDurationConfig = resources.getInteger(17694859);
        this.mMaximumScreenDimRatioConfig = resources.getFraction(18022403, WAKE_LOCK_CPU, WAKE_LOCK_CPU);
        this.mSupportsDoubleTapWakeConfig = resources.getBoolean(17957028);
        if (this.mCust != null) {
            this.mDreamsSupportedConfig = this.mCust.readConfigurationLocked(this.mDreamsSupportedConfig);
        }
    }

    private void updateSettingsLocked() {
        int i;
        boolean z;
        boolean z2 = true;
        ContentResolver resolver = this.mContext.getContentResolver();
        String str = "screensaver_enabled";
        if (this.mDreamsEnabledByDefaultConfig) {
            i = WAKE_LOCK_CPU;
        } else {
            i = HALT_MODE_SHUTDOWN;
        }
        if (Secure.getIntForUser(resolver, str, i, -2) != 0) {
            z = true;
        } else {
            z = mSupportAod;
        }
        this.mDreamsEnabledSetting = z;
        str = "screensaver_activate_on_sleep";
        if (this.mDreamsActivatedOnSleepByDefaultConfig) {
            i = WAKE_LOCK_CPU;
        } else {
            i = HALT_MODE_SHUTDOWN;
        }
        if (Secure.getIntForUser(resolver, str, i, -2) != 0) {
            z = true;
        } else {
            z = mSupportAod;
        }
        this.mDreamsActivateOnSleepSetting = z;
        str = "screensaver_activate_on_dock";
        if (this.mDreamsActivatedOnDockByDefaultConfig) {
            i = WAKE_LOCK_CPU;
        } else {
            i = HALT_MODE_SHUTDOWN;
        }
        if (Secure.getIntForUser(resolver, str, i, -2) != 0) {
            z = true;
        } else {
            z = mSupportAod;
        }
        this.mDreamsActivateOnDockSetting = z;
        this.mScreenOffTimeoutSetting = System.getIntForUser(resolver, "screen_off_timeout", DEFAULT_SCREEN_OFF_TIMEOUT, -2);
        this.mSleepTimeoutSetting = Secure.getIntForUser(resolver, "sleep_timeout", DEFAULT_SLEEP_TIMEOUT, -2);
        this.mStayOnWhilePluggedInSetting = Global.getInt(resolver, "stay_on_while_plugged_in", MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT);
        if (this.mCust != null) {
            this.mCust.updateSettingsLocked();
        }
        this.mTheaterModeEnabled = Global.getInt(this.mContext.getContentResolver(), "theater_mode_on", HALT_MODE_SHUTDOWN) == WAKE_LOCK_CPU ? true : mSupportAod;
        if (this.mSupportsDoubleTapWakeConfig) {
            boolean doubleTapWakeEnabled = Secure.getIntForUser(resolver, "double_tap_to_wake", HALT_MODE_SHUTDOWN, -2) != 0 ? true : mSupportAod;
            if (doubleTapWakeEnabled != this.mDoubleTapWakeEnabled) {
                this.mDoubleTapWakeEnabled = doubleTapWakeEnabled;
                if (this.mDoubleTapWakeEnabled) {
                    i = WAKE_LOCK_CPU;
                } else {
                    i = HALT_MODE_SHUTDOWN;
                }
                nativeSetFeature(WAKE_LOCK_CPU, i);
            }
        }
        int oldScreenBrightnessSetting = this.mScreenBrightnessSetting;
        this.mScreenBrightnessSetting = System.getIntForUser(resolver, "screen_brightness", this.mScreenBrightnessSettingDefault, this.mCurrentUserId);
        if (oldScreenBrightnessSetting != this.mScreenBrightnessSetting) {
            this.mTemporaryScreenBrightnessSettingOverride = DEFAULT_SLEEP_TIMEOUT;
            if (DEBUG) {
                Slog.d(TAG, "mScreenBrightnessSetting=" + this.mScreenBrightnessSetting + ",mTemporaryScreenBrightnessSettingOverride=" + this.mTemporaryScreenBrightnessSettingOverride + ",userid=" + this.mCurrentUserId);
            }
            sendManualBrightnessToMonitor(this.mScreenBrightnessSetting);
        }
        float oldScreenAutoBrightnessAdjustmentSetting = this.mScreenAutoBrightnessAdjustmentSetting;
        this.mScreenAutoBrightnessAdjustmentSetting = System.getFloatForUser(resolver, "screen_auto_brightness_adj", 0.0f, this.mCurrentUserId);
        if (oldScreenAutoBrightnessAdjustmentSetting != this.mScreenAutoBrightnessAdjustmentSetting) {
            this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = Float.NaN;
            if (DEBUG) {
                Slog.d(TAG, ",mScreenAutoBrightnessAdjustmentSetting=" + this.mScreenAutoBrightnessAdjustmentSetting + ",mTemporaryScreenAutoBrightnessAdjustmentSettingOverride=" + this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride + ",userid=" + this.mCurrentUserId);
            }
        }
        int oldScreenBrightnessModeSetting = this.mScreenBrightnessModeSetting;
        this.mScreenBrightnessModeSetting = System.getIntForUser(resolver, "screen_brightness_mode", HALT_MODE_SHUTDOWN, this.mCurrentUserId);
        if (oldScreenBrightnessModeSetting != this.mScreenBrightnessModeSetting) {
            if (DEBUG) {
                Slog.d(TAG, "mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting + ",userid=" + this.mCurrentUserId);
            }
            if (this.mScreenBrightnessModeSetting == 0) {
                z = true;
            } else {
                z = mSupportAod;
            }
            sendBrightnessModeToMonitor(z);
        }
        this.mSmartBacklightEnableSetting = System.getIntForUser(resolver, "smart_backlight_enable", HALT_MODE_SHUTDOWN, HALT_MODE_SHUTDOWN);
        if (Secure.getIntForUser(resolver, "brightness_use_twilight", HALT_MODE_SHUTDOWN, -2) == 0) {
            z2 = mSupportAod;
        }
        this.mBrightnessUseTwilight = z2;
        boolean lowPowerModeEnabled = Global.getInt(resolver, "low_power", HALT_MODE_SHUTDOWN) != 0 ? true : mSupportAod;
        boolean autoLowPowerModeConfigured = Global.getInt(resolver, "low_power_trigger_level", HALT_MODE_SHUTDOWN) != 0 ? true : mSupportAod;
        if (!(lowPowerModeEnabled == this.mLowPowerModeSetting && autoLowPowerModeConfigured == this.mAutoLowPowerModeConfigured)) {
            this.mLowPowerModeSetting = lowPowerModeEnabled;
            this.mAutoLowPowerModeConfigured = autoLowPowerModeConfigured;
            updateLowPowerModeLocked();
        }
        this.mDirty |= WAKE_LOCK_STAY_AWAKE;
    }

    private void postAfterBootCompleted(Runnable r) {
        if (this.mBootCompleted) {
            BackgroundThread.getHandler().post(r);
            return;
        }
        Slog.d(TAG, "Delaying runnable until system is booted");
        this.mBootCompletedRunnables = (Runnable[]) ArrayUtils.appendElement(Runnable.class, this.mBootCompletedRunnables, r);
    }

    private void updateLowPowerModeLocked() {
        boolean z;
        int i = HALT_MODE_SHUTDOWN;
        if (this.mIsPowered && this.mLowPowerModeSetting) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateLowPowerModeLocked: powered, turning setting off");
            }
            Global.putInt(this.mContext.getContentResolver(), "low_power", HALT_MODE_SHUTDOWN);
            this.mLowPowerModeSetting = mSupportAod;
        }
        if (this.mIsPowered || !this.mAutoLowPowerModeConfigured || this.mAutoLowPowerModeSnoozing) {
            z = mSupportAod;
        } else {
            z = this.mBatteryLevelLow;
        }
        boolean z2 = !this.mLowPowerModeSetting ? z : true;
        if (this.mLowPowerModeEnabled != z2) {
            this.mLowPowerModeEnabled = z2;
            if (z2) {
                i = WAKE_LOCK_CPU;
            }
            powerHintInternal(POWER_HINT_LOW_POWER, i);
            postAfterBootCompleted(new AnonymousClass4(z2));
        }
    }

    private void handleSettingsChangedLocked() {
        updateSettingsLocked();
        updatePowerStateLocked();
    }

    private boolean shouldDropLogs(String tag, String packageName, int uid) {
        if (!DEBUG_ALL && LOG_DROP_SET.contains(tag + uid)) {
            return true;
        }
        return mSupportAod;
    }

    private void acquireWakeLockInternal(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag, int uid, int pid) {
        if (this.mSystemReady) {
            synchronized (this.mLock) {
                WakeLock wakeLock;
                boolean notifyAcquire;
                if (DEBUG_SPEW && !shouldDropLogs(tag, packageName, uid)) {
                    Slog.d(TAG, "acquire lock=" + Objects.hashCode(lock) + ", flags=0x" + Integer.toHexString(flags) + ", tag=\"" + tag + "\", name=" + packageName + ", ws=" + ws + ", uid=" + uid + ", pid=" + pid);
                }
                int index = findWakeLockIndexLocked(lock);
                if (index >= 0) {
                    wakeLock = (WakeLock) this.mWakeLocks.get(index);
                    if (!wakeLock.hasSameProperties(flags, tag, ws, uid, pid)) {
                        notifyWakeLockChangingLocked(wakeLock, flags, tag, packageName, uid, pid, ws, historyTag);
                        wakeLock.updateProperties(flags, tag, packageName, ws, historyTag, uid, pid);
                    }
                    notifyAcquire = mSupportAod;
                } else {
                    wakeLock = new WakeLock(lock, flags, tag, packageName, ws, historyTag, uid, pid);
                    try {
                        lock.linkToDeath(wakeLock, HALT_MODE_SHUTDOWN);
                        this.mWakeLocks.add(wakeLock);
                        setWakeLockDisabledStateLocked(wakeLock);
                        notifyAcquire = true;
                    } catch (RemoteException e) {
                        throw new IllegalArgumentException("Wake lock is already dead.");
                    }
                }
                applyWakeLockFlagsOnAcquireLocked(wakeLock, uid);
                this.mDirty |= WAKE_LOCK_CPU;
                updatePowerStateLocked();
                if (notifyAcquire) {
                    notifyWakeLockAcquiredLocked(wakeLock);
                }
            }
        }
    }

    private static boolean isScreenLock(WakeLock wakeLock) {
        switch (wakeLock.mFlags & 65535) {
            case H.REMOVE_STARTING /*6*/:
            case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return true;
            default:
                return mSupportAod;
        }
    }

    protected void applyWakeLockFlagsOnAcquireLocked(WakeLock wakeLock, int uid) {
        if ((wakeLock.mFlags & 268435456) != 0 && isScreenLock(wakeLock)) {
            String opPackageName;
            int opUid;
            if (wakeLock.mWorkSource == null || wakeLock.mWorkSource.getName(HALT_MODE_SHUTDOWN) == null) {
                opPackageName = wakeLock.mPackageName;
                if (wakeLock.mWorkSource != null) {
                    opUid = wakeLock.mWorkSource.get(HALT_MODE_SHUTDOWN);
                } else {
                    opUid = wakeLock.mOwnerUid;
                }
            } else {
                opPackageName = wakeLock.mWorkSource.getName(HALT_MODE_SHUTDOWN);
                opUid = wakeLock.mWorkSource.get(HALT_MODE_SHUTDOWN);
            }
            wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), wakeLock.mTag, opUid, opPackageName, opUid);
        }
    }

    private void releaseWakeLockInternal(IBinder lock, int flags) {
        if (this.mSystemReady) {
            synchronized (this.mLock) {
                int index = findWakeLockIndexLocked(lock);
                if (index < 0) {
                    if (DEBUG_SPEW) {
                        Slog.d(TAG, "releaseWakeLockInternal: lock=" + Objects.hashCode(lock) + " [not found], flags=0x" + Integer.toHexString(flags));
                    }
                    return;
                }
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(index);
                if (this.mCust != null && this.mCust.isDelayEnanbled()) {
                    this.mCust.checkDelay(wakeLock.mTag);
                }
                if (DEBUG_SPEW && !shouldDropLogs(wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid)) {
                    Slog.d(TAG, "release:lock=" + Objects.hashCode(lock) + ", flg=0x" + Integer.toHexString(flags) + ", tag=\"" + wakeLock.mTag + "\", name=" + wakeLock.mPackageName + "\", ws=" + wakeLock.mWorkSource + ", uid=" + wakeLock.mOwnerUid + ", pid=" + wakeLock.mOwnerPid);
                }
                if ((flags & WAKE_LOCK_CPU) != 0) {
                    this.mRequestWaitForNegativeProximity = true;
                }
                wakeLock.mLock.unlinkToDeath(wakeLock, HALT_MODE_SHUTDOWN);
                removeWakeLockLocked(wakeLock, index);
            }
        }
    }

    private void handleWakeLockDeath(WakeLock wakeLock) {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleWakeLockDeath: lock=" + Objects.hashCode(wakeLock.mLock) + " [" + wakeLock.mTag + "]");
            }
            int index = this.mWakeLocks.indexOf(wakeLock);
            if (index < 0) {
                return;
            }
            removeWakeLockLocked(wakeLock, index);
        }
    }

    protected void removeWakeLockLocked(WakeLock wakeLock, int index) {
        this.mWakeLocks.remove(index);
        notifyWakeLockReleasedLocked(wakeLock);
        applyWakeLockFlagsOnReleaseLocked(wakeLock);
        this.mDirty |= WAKE_LOCK_CPU;
        updatePowerStateLocked();
    }

    private void applyWakeLockFlagsOnReleaseLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & 536870912) != 0 && isScreenLock(wakeLock)) {
            userActivityNoUpdateLocked(SystemClock.uptimeMillis(), HALT_MODE_SHUTDOWN, WAKE_LOCK_CPU, wakeLock.mOwnerUid);
        }
    }

    private void updateWakeLockWorkSourceInternal(IBinder lock, WorkSource ws, String historyTag, int callingUid) {
        synchronized (this.mLock) {
            int index = findWakeLockIndexLocked(lock);
            if (index < 0) {
                if (DEBUG_Controller) {
                    Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [not found], ws=" + ws);
                }
                if (ws == null) {
                    Slog.e(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [not found], ws=" + ws);
                    return;
                }
                throw new IllegalArgumentException("Wake lock not active: " + lock + " from uid " + callingUid);
            }
            WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(index);
            if (DEBUG_Controller) {
                Slog.d(TAG, "updateWakeLockWorkSourceInternal: lock=" + Objects.hashCode(lock) + " [" + wakeLock.mTag + "], ws=" + ws);
            }
            if (!wakeLock.hasSameWorkSource(ws)) {
                notifyWakeLockChangingLocked(wakeLock, wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, ws, historyTag);
                wakeLock.mHistoryTag = historyTag;
                wakeLock.updateWorkSource(ws);
            }
            return;
        }
    }

    protected int findWakeLockIndexLocked(IBinder lock) {
        int count = this.mWakeLocks.size();
        for (int i = HALT_MODE_SHUTDOWN; i < count; i += WAKE_LOCK_CPU) {
            if (((WakeLock) this.mWakeLocks.get(i)).mLock == lock) {
                return i;
            }
        }
        return DEFAULT_SLEEP_TIMEOUT;
    }

    protected void notifyWakeLockAcquiredLocked(WakeLock wakeLock) {
        if (this.mSystemReady && !wakeLock.mDisabled) {
            wakeLock.mNotifiedAcquired = true;
            this.mNotifier.onWakeLockAcquired(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
        }
    }

    protected void notifyWakeLockChangingLocked(WakeLock wakeLock, int flags, String tag, String packageName, int uid, int pid, WorkSource ws, String historyTag) {
        if (this.mSystemReady && wakeLock.mNotifiedAcquired) {
            this.mNotifier.onWakeLockChanging(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag, flags, tag, packageName, uid, pid, ws, historyTag);
        }
    }

    private void notifyWakeLockReleasedLocked(WakeLock wakeLock) {
        if (this.mSystemReady && wakeLock.mNotifiedAcquired) {
            wakeLock.mNotifiedAcquired = mSupportAod;
            this.mNotifier.onWakeLockReleased(wakeLock.mFlags, wakeLock.mTag, wakeLock.mPackageName, wakeLock.mOwnerUid, wakeLock.mOwnerPid, wakeLock.mWorkSource, wakeLock.mHistoryTag);
        }
    }

    private boolean isWakeLockLevelSupportedInternal(int level) {
        boolean z = mSupportAod;
        synchronized (this.mLock) {
            switch (level) {
                case WAKE_LOCK_CPU /*1*/:
                case H.REMOVE_STARTING /*6*/:
                case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                case H.DO_ANIMATION_CALLBACK /*26*/:
                case WAKE_LOCK_DOZE /*64*/:
                case WAKE_LOCK_DRAW /*128*/:
                    return true;
                case WAKE_LOCK_STAY_AWAKE /*32*/:
                    if (this.mSystemReady) {
                        z = this.mDisplayManagerInternal.isProximitySensorAvailable();
                    }
                    return z;
                default:
                    return mSupportAod;
            }
        }
    }

    private void userActivityFromNative(long eventTime, int event, int flags) {
        userActivityInternal(eventTime, event, flags, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
    }

    private void userActivityInternal(long eventTime, int event, int flags, int uid) {
        synchronized (this.mLock) {
            if (userActivityNoUpdateLocked(eventTime, event, flags, uid)) {
                updatePowerStateLocked();
            }
        }
    }

    protected boolean userActivityNoUpdateLocked(long eventTime, int event, int flags, int uid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "userActivityNoUpdateLocked: eventTime=" + eventTime + ", event=" + event + ", flags=0x" + Integer.toHexString(flags) + ", uid=" + uid);
        }
        if (eventTime < this.mLastSleepTime || eventTime < this.mLastWakeTime || !this.mBootCompleted || !this.mSystemReady) {
            return mSupportAod;
        }
        Trace.traceBegin(131072, "userActivity");
        try {
            if (eventTime > this.mLastInteractivePowerHintTime) {
                powerHintInternal(WAKE_LOCK_SCREEN_BRIGHT, HALT_MODE_SHUTDOWN);
                this.mLastInteractivePowerHintTime = eventTime;
            }
            this.mNotifier.onUserActivity(event, uid);
            if (this.mUserInactiveOverrideFromWindowManager) {
                this.mUserInactiveOverrideFromWindowManager = mSupportAod;
                this.mOverriddenTimeout = -1;
            }
            if (this.mWakefulness == 0 || this.mWakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT || (flags & WAKE_LOCK_SCREEN_BRIGHT) != 0) {
                Trace.traceEnd(131072);
                return mSupportAod;
            }
            if ((flags & WAKE_LOCK_CPU) != 0) {
                if (eventTime > this.mLastUserActivityTimeNoChangeLights && eventTime > this.mLastUserActivityTime) {
                    this.mLastUserActivityTimeNoChangeLights = eventTime;
                    this.mDirty |= WAKE_LOCK_SCREEN_DIM;
                    Trace.traceEnd(131072);
                    return true;
                }
            } else if (eventTime > this.mLastUserActivityTime) {
                this.mLastUserActivityTime = eventTime;
                this.mDirty |= WAKE_LOCK_SCREEN_DIM;
                Trace.traceEnd(131072);
                return true;
            }
            Trace.traceEnd(131072);
            return mSupportAod;
        } catch (Throwable th) {
            Trace.traceEnd(131072);
        }
    }

    private void wakeUpInternal(long eventTime, String reason, int uid, String opPackageName, int opUid) {
        synchronized (this.mLock) {
            if (Jlog.isPerfTest()) {
                Jlog.i(2203, "JL_PWRSCRON_PMS_WAKEUPINTERNAL");
            }
            if (wakeUpNoUpdateLocked(eventTime, reason, uid, opPackageName, opUid)) {
                updatePowerStateLocked();
            }
        }
    }

    protected boolean wakeUpNoUpdateLocked(long eventTime, String reason, int reasonUid, String opPackageName, int opUid) {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerService wakeUpNoUpdateLocked: eventTime=" + eventTime + ", uid=" + reasonUid);
        boolean reasonPower = "android.policy:POWER".equalsIgnoreCase(reason);
        if (eventTime < this.mLastSleepTime || ((this.mWakefulness == WAKE_LOCK_CPU && !this.mBrightnessWaitModeEnabled) || !this.mBootCompleted || !this.mSystemReady || (this.mProximityPositive && !reasonPower))) {
            return mSupportAod;
        }
        Trace.traceBegin(131072, "wakeUp");
        Jlog.perfEvent(9, "", new int[HALT_MODE_SHUTDOWN]);
        try {
            switch (this.mWakefulness) {
                case HALT_MODE_SHUTDOWN /*0*/:
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerService Waking up from sleep (uid " + reasonUid + ")...");
                    if (Jlog.isPerfTest()) {
                        Jlog.i(2204, "JL_PWRSCRON_PMS_ASLEEP");
                    }
                    Jlog.d(POWER_HINT_LOW_POWER, "JL_PMS_WAKEFULNESS_ASLEEP");
                    break;
                case WAKE_LOCK_SCREEN_BRIGHT /*2*/:
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerService Waking up from dream (uid " + reasonUid + ")...");
                    Jlog.d(6, "JL_PMS_WAKEFULNESS_DREAMING");
                    break;
                case MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT /*3*/:
                    Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerService Waking up from dozing (uid " + reasonUid + ")...");
                    Jlog.d(POWER_HINT_VR_MODE, "JL_PMS_WAKEFULNESS_NAPPING");
                    break;
            }
            this.mForceDoze = mSupportAod;
            this.mLastWakeTime = eventTime;
            setWakefulnessLocked(WAKE_LOCK_CPU, HALT_MODE_SHUTDOWN);
            disableBrightnessWaitLocked(mSupportAod);
            this.mNotifier.onWakeUp(reason, reasonUid, opPackageName, opUid);
            userActivityNoUpdateLocked(eventTime, HALT_MODE_SHUTDOWN, HALT_MODE_SHUTDOWN, reasonUid);
            sendPowerkeyWakeupMsg(reasonPower);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    private void sendPowerkeyWakeupMsg(boolean reasonPower) {
        if (reasonPower && mIsPowerTurnTorchOff) {
            this.mHandler.sendEmptyMessage(POWER_HINT_LOW_POWER);
        }
    }

    private void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
        synchronized (this.mLock) {
            if (isCarMachineHeldWakeLock()) {
                Light backLight = this.mLightsManager.getLight(HALT_MODE_SHUTDOWN);
                if (this.inVdriveBackLightMode) {
                    backLight.setMirrorLinkBrightness(RampAnimator.DEFAULT_MAX_BRIGHTNESS);
                    this.inVdriveBackLightMode = mSupportAod;
                    backLight.setMirrorLinkBrightnessStatus(mSupportAod);
                    InputManager.getInstance().setMirrorLinkInputStatus(mSupportAod);
                } else {
                    backLight.setMirrorLinkBrightness(HALT_MODE_SHUTDOWN);
                    this.inVdriveBackLightMode = true;
                    backLight.setMirrorLinkBrightnessStatus(true);
                    InputManager.getInstance().setMirrorLinkInputStatus(true);
                }
                Slog.d(TAG, "VCar mode goToSleepInternal inVdriveBackLightMode=" + this.inVdriveBackLightMode);
                return;
            }
            if (goToSleepNoUpdateLocked(eventTime, reason, flags, uid)) {
                updatePowerStateLocked();
            }
            return;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean goToSleepNoUpdateLocked(long eventTime, int reason, int flags, int uid) {
        Flog.i(NativeResponseCode.SERVICE_FOUND, "PowerManagerService goToSleepNoUpdateLocked: eventTime=" + eventTime + ", reason=" + reason + ", flags=" + flags + ", uid=" + uid);
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || this.mWakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT || !this.mBootCompleted || !this.mSystemReady) {
            return mSupportAod;
        }
        if (this.mWakefulness == WAKE_LOCK_CPU && this.mWakefulnessChanging && reason == WAKE_LOCK_SCREEN_DIM) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "the current screen status is not really screen-on");
            }
            return mSupportAod;
        }
        int numWakeLocksCleared;
        int numWakeLocks;
        int i;
        Trace.traceBegin(131072, "goToSleep");
        switch (reason) {
            case WAKE_LOCK_CPU /*1*/:
                Slog.i(TAG, "Going to sleep due to device administration policy (uid " + uid + ")...");
            case WAKE_LOCK_SCREEN_BRIGHT /*2*/:
                Slog.i(TAG, "Going to sleep due to screen timeout (uid " + uid + ")...");
                Jlog.d(79, "goToSleep due to screen timeout");
            case MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT /*3*/:
                Slog.i(TAG, "Going to sleep due to lid switch (uid " + uid + ")...");
                Jlog.d(79, "goToSleep due to lid");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                if ((flags & WAKE_LOCK_CPU) != 0 || this.mBrightnessWaitModeEnabled) {
                    disableBrightnessWaitLocked(mSupportAod);
                    Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                    if (!(mSupportAod && this.mForceDoze)) {
                        reallyGoToSleepNoUpdateLocked(eventTime, uid);
                    }
                }
                Trace.traceEnd(131072);
                return true;
            case WAKE_LOCK_SCREEN_DIM /*4*/:
                Slog.i(TAG, "Going to sleep due to power button (uid " + uid + ")...");
                Jlog.d(15, "goToSleep due to powerkey");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                disableBrightnessWaitLocked(mSupportAod);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
                Trace.traceEnd(131072);
                return true;
            case POWER_HINT_LOW_POWER /*5*/:
                Slog.i(TAG, "Going to sleep due to HDMI standby (uid " + uid + ")...");
                Jlog.d(79, "goToSleep due to HDMI");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                disableBrightnessWaitLocked(mSupportAod);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
                Trace.traceEnd(131072);
                return true;
            case H.REMOVE_STARTING /*6*/:
                Slog.i(TAG, "Going to sleep due to sleep button (uid " + uid + ")...");
                Jlog.d(15, "goToSleep due to sleepbutton");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                disableBrightnessWaitLocked(mSupportAod);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
                Trace.traceEnd(131072);
                return true;
            case POWER_HINT_VR_MODE /*7*/:
                Slog.i(TAG, "Going to sleep due to proximity...");
                Jlog.d(78, "goToSleep due to proximity");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                disableBrightnessWaitLocked(mSupportAod);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
                Trace.traceEnd(131072);
                return true;
            case WAKE_LOCK_BUTTON_BRIGHT /*8*/:
                Slog.i(TAG, "Going to sleep due to wait brightness timeout...");
                Jlog.d(79, "gotoToSleep due to wait brightness timeout");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                disableBrightnessWaitLocked(mSupportAod);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
                Trace.traceEnd(131072);
                return true;
            case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                Slog.i(TAG, "Going to sleep due to called by incallui when on the phone...");
                Jlog.d(79, "goToSleep due to called by incallui when on the phone");
                this.mLastSleepTime = eventTime;
                this.mSandmanSummoned = true;
                setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
                numWakeLocksCleared = HALT_MODE_SHUTDOWN;
                numWakeLocks = this.mWakeLocks.size();
                for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                    switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                        case H.REMOVE_STARTING /*6*/:
                        case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        case H.DO_ANIMATION_CALLBACK /*26*/:
                            numWakeLocksCleared += WAKE_LOCK_CPU;
                            break;
                        default:
                            break;
                    }
                }
                EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
                disableBrightnessWaitLocked(mSupportAod);
                Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
                reallyGoToSleepNoUpdateLocked(eventTime, uid);
                Trace.traceEnd(131072);
                return true;
            default:
                try {
                    Slog.i(TAG, "Going to sleep by application request (uid " + uid + ")...");
                    Jlog.d(79, "goToSleep by app");
                    reason = HALT_MODE_SHUTDOWN;
                } catch (Throwable th) {
                    Trace.traceEnd(131072);
                }
        }
        this.mLastSleepTime = eventTime;
        this.mSandmanSummoned = true;
        setWakefulnessLocked(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT, reason);
        numWakeLocksCleared = HALT_MODE_SHUTDOWN;
        numWakeLocks = this.mWakeLocks.size();
        for (i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
            switch (((WakeLock) this.mWakeLocks.get(i)).mFlags & 65535) {
                case H.REMOVE_STARTING /*6*/:
                case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                case H.DO_ANIMATION_CALLBACK /*26*/:
                    numWakeLocksCleared += WAKE_LOCK_CPU;
                    break;
                default:
                    break;
            }
        }
        EventLog.writeEvent(EventLogTags.POWER_SLEEP_REQUESTED, numWakeLocksCleared);
        disableBrightnessWaitLocked(mSupportAod);
        Slog.i(TAG, "AOD goToSleepNoUpdateLocked mForceDoze=" + this.mForceDoze);
        reallyGoToSleepNoUpdateLocked(eventTime, uid);
        Trace.traceEnd(131072);
        return true;
    }

    private void napInternal(long eventTime, int uid) {
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
        if (eventTime < this.mLastWakeTime || this.mWakefulness != WAKE_LOCK_CPU || !this.mBootCompleted || !this.mSystemReady) {
            return mSupportAod;
        }
        Trace.traceBegin(131072, "nap");
        try {
            Slog.i(TAG, "Nap time (uid " + uid + ")...");
            this.mSandmanSummoned = true;
            setWakefulnessLocked(WAKE_LOCK_SCREEN_BRIGHT, HALT_MODE_SHUTDOWN);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    private boolean reallyGoToSleepNoUpdateLocked(long eventTime, int uid) {
        if (DEBUG_SPEW) {
            Slog.d(TAG, "reallyGoToSleepNoUpdateLocked: eventTime=" + eventTime + ", uid=" + uid);
        }
        if (eventTime < this.mLastWakeTime || this.mWakefulness == 0 || !this.mBootCompleted || !this.mSystemReady) {
            return mSupportAod;
        }
        Trace.traceBegin(131072, "reallyGoToSleep");
        try {
            Slog.i(TAG, "Sleeping (uid " + uid + ")...");
            setWakefulnessLocked(HALT_MODE_SHUTDOWN, WAKE_LOCK_SCREEN_BRIGHT);
            return true;
        } finally {
            Trace.traceEnd(131072);
        }
    }

    protected void setWakefulnessLocked(int wakefulness, int reason) {
        if (this.mWakefulness != wakefulness || this.mBrightnessWaitModeEnabled) {
            this.mWakefulness = wakefulness;
            this.mWakefulnessChanging = true;
            this.mDirty |= WAKE_LOCK_SCREEN_BRIGHT;
            this.mNotifier.onWakefulnessChangeStarted(wakefulness, reason);
        }
    }

    private void logSleepTimeoutRecapturedLocked() {
        long savedWakeTimeMs = this.mOverriddenTimeout - SystemClock.uptimeMillis();
        if (savedWakeTimeMs >= 0) {
            EventLog.writeEvent(EventLogTags.POWER_SOFT_SLEEP_REQUESTED, savedWakeTimeMs);
            this.mOverriddenTimeout = -1;
        }
    }

    private void finishWakefulnessChangeIfNeededLocked() {
        if (this.mWakefulnessChanging && this.mDisplayReady && (this.mWakefulness != MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT || (this.mWakeLockSummary & WAKE_LOCK_DOZE) != 0)) {
            if (this.mWakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT || this.mWakefulness == 0) {
                logSleepTimeoutRecapturedLocked();
            }
            this.mWakefulnessChanging = mSupportAod;
            this.mNotifier.onWakefulnessChangeFinished();
        }
    }

    protected void updatePowerStateLocked() {
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
                int dirtyPhase2 = HALT_MODE_SHUTDOWN;
                while (true) {
                    int dirtyPhase1 = this.mDirty;
                    dirtyPhase2 |= dirtyPhase1;
                    this.mDirty = HALT_MODE_SHUTDOWN;
                    updateWakeLockSummaryLocked(dirtyPhase1);
                    updateUserActivitySummaryLocked(now, dirtyPhase1);
                    if (!updateWakefulnessLocked(dirtyPhase1)) {
                        break;
                    }
                }
                updateDreamLocked(dirtyPhase2, updateDisplayPowerStateLocked(dirtyPhase2));
                finishWakefulnessChangeIfNeededLocked();
                updateSuspendBlockerLocked();
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    private void updateIsPoweredLocked(int dirty) {
        if ((dirty & DIRTY_BATTERY_STATE) != 0) {
            boolean wasPowered = this.mIsPowered;
            int oldPlugType = this.mPlugType;
            boolean oldLevelLow = this.mBatteryLevelLow;
            this.mIsPowered = this.mBatteryManagerInternal.isPowered(POWER_HINT_VR_MODE);
            this.mPlugType = this.mBatteryManagerInternal.getPlugType();
            this.mBatteryLevel = this.mBatteryManagerInternal.getBatteryLevel();
            this.mBatteryLevelLow = this.mBatteryManagerInternal.getBatteryLevelLow();
            if (DEBUG_SPEW) {
                Slog.d(TAG, "updateIsPoweredLocked: wasPowered=" + wasPowered + ", mIsPowered=" + this.mIsPowered + ", oldPlugType=" + oldPlugType + ", mPlugType=" + this.mPlugType + ", mBatteryLevel=" + this.mBatteryLevel);
            }
            if (!(wasPowered == this.mIsPowered && oldPlugType == this.mPlugType)) {
                this.mDirty |= WAKE_LOCK_DOZE;
                boolean dockedOnWirelessCharger = this.mWirelessChargerDetector.update(this.mIsPowered, this.mPlugType, this.mBatteryLevel);
                long now = SystemClock.uptimeMillis();
                if (shouldWakeUpWhenPluggedOrUnpluggedLocked(wasPowered, oldPlugType, dockedOnWirelessCharger)) {
                    wakeUpNoUpdateLocked(now, "android.server.power:POWER", ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, this.mContext.getOpPackageName(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                }
                userActivityNoUpdateLocked(now, HALT_MODE_SHUTDOWN, HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                if (dockedOnWirelessCharger) {
                    this.mNotifier.onWirelessChargingStarted();
                }
            }
            if (wasPowered != this.mIsPowered || oldLevelLow != this.mBatteryLevelLow) {
                if (!(oldLevelLow == this.mBatteryLevelLow || this.mBatteryLevelLow)) {
                    if (DEBUG_SPEW) {
                        Slog.d(TAG, "updateIsPoweredLocked: resetting low power snooze");
                    }
                    this.mAutoLowPowerModeSnoozing = mSupportAod;
                }
                updateLowPowerModeLocked();
            }
        }
    }

    private boolean shouldWakeUpWhenPluggedOrUnpluggedLocked(boolean wasPowered, int oldPlugType, boolean dockedOnWirelessCharger) {
        if (!this.mWakeUpWhenPluggedOrUnpluggedConfig) {
            return mSupportAod;
        }
        if (wasPowered && !this.mIsPowered && oldPlugType == WAKE_LOCK_SCREEN_DIM) {
            return mSupportAod;
        }
        if (!wasPowered && this.mIsPowered && this.mPlugType == WAKE_LOCK_SCREEN_DIM && !dockedOnWirelessCharger) {
            return mSupportAod;
        }
        if (this.mIsPowered && this.mWakefulness == WAKE_LOCK_SCREEN_BRIGHT) {
            return mSupportAod;
        }
        if (!this.mTheaterModeEnabled || this.mWakeUpWhenPluggedOrUnpluggedInTheaterModeConfig) {
            return true;
        }
        return mSupportAod;
    }

    private void updateStayOnLocked(int dirty) {
        if ((dirty & 288) != 0) {
            boolean wasStayOn = this.mStayOn;
            if (this.mStayOnWhilePluggedInSetting == 0 || isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
                this.mStayOn = mSupportAod;
            } else {
                this.mStayOn = this.mBatteryManagerInternal.isPowered(this.mStayOnWhilePluggedInSetting);
            }
            if (this.mStayOn != wasStayOn) {
                this.mDirty |= WAKE_LOCK_DRAW;
            }
        }
    }

    private void updateWakeLockSummaryLocked(int dirty) {
        if ((dirty & MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) != 0) {
            this.mWakeLockSummary = HALT_MODE_SHUTDOWN;
            int numWakeLocks = this.mWakeLocks.size();
            for (int i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
                WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
                switch (wakeLock.mFlags & 65535) {
                    case WAKE_LOCK_CPU /*1*/:
                        if (!wakeLock.mDisabled) {
                            this.mWakeLockSummary |= WAKE_LOCK_CPU;
                            break;
                        }
                        break;
                    case H.REMOVE_STARTING /*6*/:
                        this.mWakeLockSummary |= WAKE_LOCK_SCREEN_DIM;
                        break;
                    case AppTransition.TRANSIT_TASK_TO_FRONT /*10*/:
                        this.mWakeLockSummary |= WAKE_LOCK_SCREEN_BRIGHT;
                        break;
                    case H.DO_ANIMATION_CALLBACK /*26*/:
                        this.mWakeLockSummary |= 10;
                        break;
                    case WAKE_LOCK_STAY_AWAKE /*32*/:
                        this.mWakeLockSummary |= WAKE_LOCK_PROXIMITY_SCREEN_OFF;
                        break;
                    case WAKE_LOCK_DOZE /*64*/:
                        this.mWakeLockSummary |= WAKE_LOCK_DOZE;
                        break;
                    case WAKE_LOCK_DRAW /*128*/:
                        this.mWakeLockSummary |= WAKE_LOCK_DRAW;
                        break;
                    default:
                        break;
                }
            }
            if (this.mWakefulness != MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                this.mWakeLockSummary &= -193;
            }
            if (this.mWakefulness == 0 || (this.mWakeLockSummary & WAKE_LOCK_DOZE) != 0) {
                this.mWakeLockSummary &= -15;
            }
            if ((this.mWakeLockSummary & 6) != 0) {
                if (this.mWakefulness == WAKE_LOCK_CPU) {
                    this.mWakeLockSummary |= 33;
                } else if (this.mWakefulness == WAKE_LOCK_SCREEN_BRIGHT) {
                    this.mWakeLockSummary |= WAKE_LOCK_CPU;
                }
            }
            if ((this.mWakeLockSummary & WAKE_LOCK_DRAW) != 0) {
                this.mWakeLockSummary |= WAKE_LOCK_CPU;
            }
            if (DEBUG_Controller) {
                Slog.d(TAG, "updateWakeLockSummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mWakeLockSummary=0x" + Integer.toHexString(this.mWakeLockSummary));
            }
        }
    }

    private void updateUserActivitySummaryLocked(long now, int dirty) {
        if ((dirty & 39) != 0) {
            this.mHandler.removeMessages(WAKE_LOCK_CPU);
            long nextTimeout = 0;
            if (this.mWakefulness == WAKE_LOCK_CPU || this.mWakefulness == WAKE_LOCK_SCREEN_BRIGHT || this.mWakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                int sleepTimeout = getSleepTimeoutLocked();
                int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
                int screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
                boolean userInactiveOverride = this.mUserInactiveOverrideFromWindowManager;
                this.mUserActivitySummary = HALT_MODE_SHUTDOWN;
                if (this.mLastUserActivityTime >= this.mLastWakeTime) {
                    nextTimeout = (this.mLastUserActivityTime + ((long) screenOffTimeout)) - ((long) screenDimDuration);
                    if (now < nextTimeout) {
                        this.mUserActivitySummary = WAKE_LOCK_CPU;
                    } else {
                        nextTimeout = this.mLastUserActivityTime + ((long) screenOffTimeout);
                        if (now < nextTimeout) {
                            this.mUserActivitySummary = WAKE_LOCK_SCREEN_BRIGHT;
                        }
                    }
                }
                if (this.mUserActivitySummary == 0 && this.mLastUserActivityTimeNoChangeLights >= this.mLastWakeTime) {
                    nextTimeout = this.mLastUserActivityTimeNoChangeLights + ((long) screenOffTimeout);
                    if (now < nextTimeout) {
                        if (this.mDisplayPowerRequest.policy == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                            this.mUserActivitySummary = WAKE_LOCK_CPU;
                        } else if (this.mDisplayPowerRequest.policy == WAKE_LOCK_SCREEN_BRIGHT) {
                            this.mUserActivitySummary = WAKE_LOCK_SCREEN_BRIGHT;
                        }
                    }
                }
                if (this.mUserActivitySummary == 0) {
                    if (sleepTimeout >= 0) {
                        long anyUserActivity = Math.max(this.mLastUserActivityTime, this.mLastUserActivityTimeNoChangeLights);
                        if (anyUserActivity >= this.mLastWakeTime) {
                            nextTimeout = anyUserActivity + ((long) sleepTimeout);
                            if (now < nextTimeout) {
                                this.mUserActivitySummary = WAKE_LOCK_SCREEN_DIM;
                            }
                        }
                    } else {
                        this.mUserActivitySummary = WAKE_LOCK_SCREEN_DIM;
                        nextTimeout = -1;
                    }
                }
                if (this.mUserActivitySummary != WAKE_LOCK_SCREEN_DIM && userInactiveOverride) {
                    if ((this.mUserActivitySummary & MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) != 0 && nextTimeout >= now && this.mOverriddenTimeout == -1) {
                        this.mOverriddenTimeout = nextTimeout;
                    }
                    this.mUserActivitySummary = WAKE_LOCK_SCREEN_DIM;
                    nextTimeout = -1;
                }
                if (this.mUserActivitySummary != 0 && nextTimeout >= 0) {
                    Message msg = this.mHandler.obtainMessage(WAKE_LOCK_CPU);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageAtTime(msg, nextTimeout);
                }
            } else {
                this.mUserActivitySummary = HALT_MODE_SHUTDOWN;
            }
            if (DEBUG_Controller) {
                Slog.d(TAG, "updateUserActivitySummaryLocked: mWakefulness=" + PowerManagerInternal.wakefulnessToString(this.mWakefulness) + ", mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ", nextTimeout=" + TimeUtils.formatUptime(nextTimeout));
            }
        }
    }

    private void handleUserActivityTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleUserActivityTimeout");
            }
            this.mDirty |= WAKE_LOCK_SCREEN_DIM;
            this.mScreenTimeoutFlag = true;
            updatePowerStateLocked();
            this.mScreenTimeoutFlag = mSupportAod;
        }
    }

    private int getSleepTimeoutLocked() {
        int timeout = this.mSleepTimeoutSetting;
        if (timeout <= 0) {
            return DEFAULT_SLEEP_TIMEOUT;
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private int getScreenOffTimeoutLocked(int sleepTimeout) {
        int timeout = this.mScreenOffTimeoutSetting;
        if (isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked()) {
            timeout = Math.min(timeout, this.mMaximumScreenOffTimeoutFromDeviceAdmin);
        }
        if (this.mUserActivityTimeoutOverrideFromWindowManager >= 0) {
            timeout = (int) Math.min((long) timeout, this.mUserActivityTimeoutOverrideFromWindowManager);
        }
        if (sleepTimeout >= 0) {
            timeout = Math.min(timeout, sleepTimeout);
        }
        if (getAdjustedMaxTimeout(timeout, this.mMinimumScreenOffTimeoutConfig) > 0) {
            return Math.min(timeout, HIGH_PRECISION_MAX_BRIGHTNESS);
        }
        return Math.max(timeout, this.mMinimumScreenOffTimeoutConfig);
    }

    private int getScreenDimDurationLocked(int screenOffTimeout) {
        int maxDimRatio = Integer.parseInt(SystemProperties.get("sys.aps.maxDimRatio", "-1"));
        int dimDuration = DEFAULT_SLEEP_TIMEOUT;
        if (maxDimRatio != DEFAULT_SLEEP_TIMEOUT) {
            dimDuration = HwFrameworkFactory.getHwNsdImpl().getCustScreenDimDurationLocked(screenOffTimeout);
        }
        if (dimDuration == DEFAULT_SLEEP_TIMEOUT || maxDimRatio == DEFAULT_SLEEP_TIMEOUT) {
            return Math.min(this.mMaximumScreenDimDurationConfig, (int) (((float) screenOffTimeout) * this.mMaximumScreenDimRatioConfig));
        }
        return dimDuration;
    }

    private boolean updateWakefulnessLocked(int dirty) {
        if ((dirty & 1687) == 0 || this.mWakefulness != WAKE_LOCK_CPU || !isItBedTimeYetLocked()) {
            return mSupportAod;
        }
        if (DEBUG_SPEW) {
            Slog.d(TAG, "updateWakefulnessLocked: Bed time...");
        }
        long time = SystemClock.uptimeMillis();
        if (shouldNapAtBedTimeLocked()) {
            return napNoUpdateLocked(time, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
        }
        return goToSleepNoUpdateLocked(time, WAKE_LOCK_SCREEN_BRIGHT, HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
    }

    private boolean shouldNapAtBedTimeLocked() {
        if (this.mDreamsActivateOnSleepSetting) {
            return true;
        }
        if (this.mDreamsActivateOnDockSetting) {
            return this.mDockState != 0 ? true : mSupportAod;
        } else {
            return mSupportAod;
        }
    }

    private boolean isItBedTimeYetLocked() {
        boolean keepAwake = isBeingKeptAwakeLocked();
        if (DEBUG && this.mScreenTimeoutFlag && keepAwake) {
            Slog.i(TAG, "Screen timeout occured. mStayOn = " + this.mStayOn + ", mProximityPositive = " + this.mProximityPositive + ", mWakeLockSummary = 0x" + Integer.toHexString(this.mWakeLockSummary) + ", mUserActivitySummary = 0x" + Integer.toHexString(this.mUserActivitySummary) + ", mScreenBrightnessBoostInProgress = " + this.mScreenBrightnessBoostInProgress);
            if ((this.mWakeLockSummary & WAKE_LOCK_STAY_AWAKE) != 0) {
                Slog.i(TAG, "Wake Locks: size = " + this.mWakeLocks.size());
                for (WakeLock wl : this.mWakeLocks) {
                    Slog.i(TAG, "WakeLock:" + wl.toString());
                }
            }
        }
        if (!this.mBootCompleted || keepAwake) {
            return mSupportAod;
        }
        return true;
    }

    private boolean isBeingKeptAwakeLocked() {
        if (this.mStayOn || ((this.mProximityPositive && !isPhoneHeldWakeLock()) || (this.mWakeLockSummary & WAKE_LOCK_STAY_AWAKE) != 0 || (this.mUserActivitySummary & MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) != 0)) {
            return true;
        }
        return this.mScreenBrightnessBoostInProgress;
    }

    private void updateDreamLocked(int dirty, boolean displayBecameReady) {
        if (((dirty & 1015) != 0 || displayBecameReady) && this.mDisplayReady) {
            scheduleSandmanLocked();
        }
    }

    private void scheduleSandmanLocked() {
        if (!this.mSandmanScheduled) {
            this.mSandmanScheduled = true;
            Message msg = this.mHandler.obtainMessage(WAKE_LOCK_SCREEN_BRIGHT);
            msg.setAsynchronous(true);
            this.mHandler.sendMessage(msg);
        }
    }

    private void handleSandman() {
        boolean isDreaming;
        synchronized (this.mLock) {
            boolean z;
            this.mSandmanScheduled = mSupportAod;
            int wakefulness = this.mWakefulness;
            if (this.mSandmanSummoned && this.mDisplayReady) {
                boolean isStartDreamFromUser = (this.mCust == null || !this.mCust.isChargingAlbumSupported()) ? mSupportAod : this.mCust.isStartDreamFromUser();
                z = (canDreamLocked() || canDozeLocked()) ? true : isStartDreamFromUser;
                Slog.e(TAG, "startDreaming = " + z);
                if (this.mCust != null) {
                    this.mCust.setStartDreamFromUser(mSupportAod);
                }
                this.mSandmanSummoned = mSupportAod;
            } else {
                z = mSupportAod;
            }
        }
        if (this.mDreamManager != null) {
            if (z) {
                boolean z2;
                this.mDreamManager.stopDream(mSupportAod);
                DreamManagerInternal dreamManagerInternal = this.mDreamManager;
                if (wakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                    z2 = true;
                } else {
                    z2 = mSupportAod;
                }
                dreamManagerInternal.startDream(z2);
            }
            isDreaming = this.mDreamManager.isDreaming();
        } else {
            isDreaming = mSupportAod;
        }
        synchronized (this.mLock) {
            if (z && isDreaming) {
                this.mBatteryLevelWhenDreamStarted = this.mBatteryLevel;
                if (wakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                    Slog.i(TAG, "Dozing...");
                } else {
                    Slog.i(TAG, "Dreaming...");
                }
            }
            if (this.mSandmanSummoned || this.mWakefulness != wakefulness) {
                return;
            }
            if (wakefulness == WAKE_LOCK_SCREEN_BRIGHT) {
                if (isDreaming) {
                    if (canDreamLocked()) {
                        if (this.mDreamsBatteryLevelDrainCutoffConfig < 0 || this.mBatteryLevel >= this.mBatteryLevelWhenDreamStarted - this.mDreamsBatteryLevelDrainCutoffConfig || isBeingKeptAwakeLocked()) {
                            return;
                        }
                        Slog.i(TAG, "Stopping dream because the battery appears to be draining faster than it is charging.  Battery level when dream started: " + this.mBatteryLevelWhenDreamStarted + "%.  " + "Battery level now: " + this.mBatteryLevel + "%.");
                    }
                }
                if (isItBedTimeYetLocked()) {
                    goToSleepNoUpdateLocked(SystemClock.uptimeMillis(), WAKE_LOCK_SCREEN_BRIGHT, HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                    updatePowerStateLocked();
                } else {
                    wakeUpNoUpdateLocked(SystemClock.uptimeMillis(), "android.server.power:DREAM", ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, this.mContext.getOpPackageName(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                    updatePowerStateLocked();
                }
            } else if (wakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                if (isDreaming || (mSupportAod && this.mForceDoze)) {
                    return;
                } else {
                    reallyGoToSleepNoUpdateLocked(SystemClock.uptimeMillis(), ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
                    updatePowerStateLocked();
                }
            }
            if (isDreaming) {
                if (this.mCust == null || !this.mCust.isChargingAlbumSupported()) {
                    this.mDreamManager.stopDream(mSupportAod);
                } else if (!this.mCust.isStartDreamFromUser()) {
                    this.mDreamManager.stopDream(mSupportAod);
                }
            }
        }
    }

    private boolean canDreamLocked() {
        if (this.mWakefulness != WAKE_LOCK_SCREEN_BRIGHT || !this.mDreamsSupportedConfig || ((!this.mDreamsEnabledSetting && (this.mCust == null || !this.mCust.isChargingAlbumEnabled() || !this.mCust.isChargingAlbumSupported())) || !this.mDisplayPowerRequest.isBrightOrDim() || (this.mUserActivitySummary & POWER_HINT_VR_MODE) == 0 || !this.mBootCompleted)) {
            return mSupportAod;
        }
        if (this.mCust != null && this.mCust.canDreamLocked()) {
            return mSupportAod;
        }
        if (!isBeingKeptAwakeLocked()) {
            if (!this.mIsPowered && !this.mDreamsEnabledOnBatteryConfig) {
                return mSupportAod;
            }
            if (!this.mIsPowered && this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig >= 0 && this.mBatteryLevel < this.mDreamsBatteryLevelMinimumWhenNotPoweredConfig) {
                return mSupportAod;
            }
            if (this.mIsPowered && this.mDreamsBatteryLevelMinimumWhenPoweredConfig >= 0 && this.mBatteryLevel < this.mDreamsBatteryLevelMinimumWhenPoweredConfig) {
                return mSupportAod;
            }
        }
        if (this.mCust == null || !this.mCust.isChargingAlbumSupported() || this.mCust.isChargingAlbumEnabled()) {
            return true;
        }
        return mSupportAod;
    }

    private boolean canDozeLocked() {
        return this.mWakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT ? true : mSupportAod;
    }

    private boolean updateDisplayPowerStateLocked(int dirty) {
        boolean oldDisplayReady = this.mDisplayReady;
        if ((dirty & 6207) != 0) {
            boolean z;
            int newScreenState = getDesiredScreenPolicyLocked();
            if (newScreenState == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT && this.mDisplayPowerRequest.policy == 0) {
                Slog.d(TAG, "setColorTemperatureAccordingToSetting");
                setColorTemperatureAccordingToSetting();
            }
            this.mDisplayPowerRequest.policy = newScreenState;
            boolean brightnessSetByUser = true;
            int screenBrightness = this.mScreenBrightnessSettingDefault;
            float screenAutoBrightnessAdjustment = 0.0f;
            boolean autoBrightness = this.mScreenBrightnessModeSetting == WAKE_LOCK_CPU ? true : mSupportAod;
            if (isValidBrightness(this.mScreenBrightnessOverrideFromWindowManager)) {
                screenBrightness = this.mScreenBrightnessOverrideFromWindowManager;
                autoBrightness = mSupportAod;
                brightnessSetByUser = mSupportAod;
            } else if (isValidBrightness(this.mTemporaryScreenBrightnessSettingOverride)) {
                screenBrightness = this.mTemporaryScreenBrightnessSettingOverride;
            } else if (isValidBrightness(this.mScreenBrightnessSetting)) {
                screenBrightness = this.mScreenBrightnessSetting;
            }
            if (autoBrightness) {
                screenBrightness = this.mScreenBrightnessSettingDefault;
                if (isValidAutoBrightnessAdjustment(this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride)) {
                    screenAutoBrightnessAdjustment = this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride;
                } else if (isValidAutoBrightnessAdjustment(this.mScreenAutoBrightnessAdjustmentSetting)) {
                    screenAutoBrightnessAdjustment = this.mScreenAutoBrightnessAdjustmentSetting;
                }
            }
            boolean updateBacklightBrightnessFlag = this.mBacklightBrightness.updateBacklightBrightness(this.mScreenBrightnessOverrideFromWindowManager);
            this.mDisplayManagerInternal.setBacklightBrightness(this.mBacklightBrightness);
            if (autoBrightness && updateBacklightBrightnessFlag) {
                screenBrightness = this.mBacklightBrightness.level;
                screenAutoBrightnessAdjustment = ((((float) this.mBacklightBrightness.level) * 2.0f) / 255.0f) - 1.0f;
            }
            screenBrightness = Math.max(Math.min(screenBrightness, this.mScreenBrightnessSettingMaximum), this.mScreenBrightnessSettingMinimum);
            screenAutoBrightnessAdjustment = Math.max(Math.min(screenAutoBrightnessAdjustment, 1.0f), -1.0f);
            this.mDisplayPowerRequest.screenBrightness = screenBrightness;
            this.mDisplayPowerRequest.screenAutoBrightnessAdjustment = screenAutoBrightnessAdjustment;
            this.mDisplayPowerRequest.brightnessSetByUser = brightnessSetByUser;
            this.mDisplayPowerRequest.useAutoBrightness = autoBrightness;
            DisplayPowerRequest displayPowerRequest = this.mDisplayPowerRequest;
            if (this.mSmartBacklightEnableSetting == WAKE_LOCK_CPU) {
                z = true;
            } else {
                z = mSupportAod;
            }
            displayPowerRequest.useSmartBacklight = z;
            this.mDisplayPowerRequest.screenAutoBrightness = this.mTemporaryScreenAutoBrightnessSettingOverride;
            this.mDisplayPowerRequest.useProximitySensor = shouldUseProximitySensorLocked();
            if (this.mDisplayPowerRequest.useProximitySensor) {
                this.mDisplayPowerRequest.useProximitySensorbyPhone = isPhoneHeldWakeLock();
            }
            this.mDisplayPowerRequest.lowPowerMode = this.mLowPowerModeEnabled;
            this.mDisplayPowerRequest.boostScreenBrightness = this.mScreenBrightnessBoostInProgress;
            this.mDisplayPowerRequest.useTwilight = this.mBrightnessUseTwilight;
            this.mDisplayPowerRequest.userId = this.mCurrentUserId;
            if (this.mDisplayPowerRequest.policy == WAKE_LOCK_CPU) {
                this.mDisplayPowerRequest.dozeScreenState = this.mDozeScreenStateOverrideFromDreamManager;
                if (this.mDisplayPowerRequest.dozeScreenState == WAKE_LOCK_SCREEN_DIM && (this.mWakeLockSummary & WAKE_LOCK_DRAW) != 0) {
                    this.mDisplayPowerRequest.dozeScreenState = MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT;
                }
                this.mDisplayPowerRequest.dozeScreenBrightness = this.mDozeScreenBrightnessOverrideFromDreamManager;
            } else {
                this.mDisplayPowerRequest.dozeScreenState = HALT_MODE_SHUTDOWN;
                this.mDisplayPowerRequest.dozeScreenBrightness = DEFAULT_SLEEP_TIMEOUT;
            }
            this.mDisplayPowerRequest.brightnessWaitMode = this.mBrightnessWaitModeEnabled;
            this.mDisplayPowerRequest.brightnessWaitRet = this.mBrightnessWaitRet;
            this.mDisplayReady = this.mDisplayManagerInternal.requestPowerState(this.mDisplayPowerRequest, this.mRequestWaitForNegativeProximity);
            this.mRequestWaitForNegativeProximity = mSupportAod;
            if (DEBUG_SPEW) {
                Slog.d(TAG, "ready=" + this.mDisplayReady + ",policy=" + this.mDisplayPowerRequest.policy + ",wakefulness=" + this.mWakefulness + ",wksummary=0x" + Integer.toHexString(this.mWakeLockSummary) + ",uasummary=0x" + Integer.toHexString(this.mUserActivitySummary) + ",bootcompleted=" + this.mBootCompleted + ",boostinprogress=" + this.mScreenBrightnessBoostInProgress + ",waitmodeenable=" + this.mBrightnessWaitModeEnabled + ",mode=" + this.mDisplayPowerRequest.useAutoBrightness + ",manual=" + this.mDisplayPowerRequest.screenBrightness + ",auto=" + this.mDisplayPowerRequest.screenAutoBrightness + ",adj=" + this.mDisplayPowerRequest.screenAutoBrightnessAdjustment + "userId=" + this.mDisplayPowerRequest.userId);
            }
        }
        if (!this.mDisplayReady || oldDisplayReady) {
            return mSupportAod;
        }
        return true;
    }

    private void updateScreenBrightnessBoostLocked(int dirty) {
        if ((dirty & DIRTY_SCREEN_BRIGHTNESS_BOOST) != 0 && this.mScreenBrightnessBoostInProgress) {
            long now = SystemClock.uptimeMillis();
            this.mHandler.removeMessages(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT);
            if (this.mLastScreenBrightnessBoostTime > this.mLastSleepTime) {
                long boostTimeout = this.mLastScreenBrightnessBoostTime + 5000;
                if (boostTimeout > now) {
                    Message msg = this.mHandler.obtainMessage(MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT);
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageAtTime(msg, boostTimeout);
                    return;
                }
            }
            this.mScreenBrightnessBoostInProgress = mSupportAod;
            this.mNotifier.onScreenBrightnessBoostChanged();
            userActivityNoUpdateLocked(now, HALT_MODE_SHUTDOWN, HALT_MODE_SHUTDOWN, ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE);
        }
    }

    private static boolean isValidBrightness(int value) {
        return (value < 0 || value > RampAnimator.DEFAULT_MAX_BRIGHTNESS) ? mSupportAod : true;
    }

    private static boolean isValidAutoBrightnessAdjustment(float value) {
        return (value < -1.0f || value > 1.0f) ? mSupportAod : true;
    }

    private int getDesiredScreenPolicyLocked() {
        if (this.mWakefulness == 0) {
            return HALT_MODE_SHUTDOWN;
        }
        if (this.mWakefulness == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
            if ((this.mWakeLockSummary & WAKE_LOCK_DOZE) != 0) {
                return WAKE_LOCK_CPU;
            }
            if (this.mDozeAfterScreenOffConfig) {
                return HALT_MODE_SHUTDOWN;
            }
        }
        if ((this.mWakeLockSummary & WAKE_LOCK_SCREEN_BRIGHT) == 0 && (this.mUserActivitySummary & WAKE_LOCK_CPU) == 0 && this.mBootCompleted && !this.mScreenBrightnessBoostInProgress) {
            return WAKE_LOCK_SCREEN_BRIGHT;
        }
        return MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT;
    }

    private boolean shouldUseProximitySensorLocked() {
        return (this.mWakeLockSummary & WAKE_LOCK_PROXIMITY_SCREEN_OFF) != 0 ? true : mSupportAod;
    }

    private void updateSuspendBlockerLocked() {
        boolean needWakeLockSuspendBlocker = (this.mWakeLockSummary & WAKE_LOCK_CPU) != 0 ? true : mSupportAod;
        boolean needDisplaySuspendBlocker = needDisplaySuspendBlockerLocked();
        boolean autoSuspend = needDisplaySuspendBlocker ? mSupportAod : true;
        boolean interactive = this.mDisplayPowerRequest.isBrightOrDim();
        if (!autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(mSupportAod);
        }
        if (needWakeLockSuspendBlocker && !this.mHoldingWakeLockSuspendBlocker) {
            this.mWakeLockSuspendBlocker.acquire();
            this.mHoldingWakeLockSuspendBlocker = true;
        }
        if (needDisplaySuspendBlocker && !this.mHoldingDisplaySuspendBlocker) {
            this.mDisplaySuspendBlocker.acquire();
            this.mHoldingDisplaySuspendBlocker = true;
        }
        if (this.mDecoupleHalInteractiveModeFromDisplayConfig && (interactive || this.mDisplayReady)) {
            setHalInteractiveModeLocked(interactive);
        }
        if (!needWakeLockSuspendBlocker && this.mHoldingWakeLockSuspendBlocker) {
            this.mWakeLockSuspendBlocker.release();
            this.mHoldingWakeLockSuspendBlocker = mSupportAod;
        }
        if (!needDisplaySuspendBlocker && this.mHoldingDisplaySuspendBlocker) {
            this.mDisplaySuspendBlocker.release();
            this.mHoldingDisplaySuspendBlocker = mSupportAod;
        }
        if (autoSuspend && this.mDecoupleHalAutoSuspendModeFromDisplayConfig) {
            setHalAutoSuspendModeLocked(true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean needDisplaySuspendBlockerLocked() {
        if (!this.mDisplayReady) {
            return true;
        }
        if ((!this.mDisplayPowerRequest.isBrightOrDim() || (this.mDisplayPowerRequest.useProximitySensor && this.mProximityPositive && this.mSuspendWhenScreenOffDueToProximityConfig)) && !this.mScreenBrightnessBoostInProgress) {
            return mSupportAod;
        }
        return true;
    }

    private void setHalAutoSuspendModeLocked(boolean enable) {
        if (enable != this.mHalAutoSuspendModeEnabled) {
            if (DEBUG) {
                Slog.d(TAG, "Setting HAL auto-suspend mode to " + enable);
            }
            this.mHalAutoSuspendModeEnabled = enable;
            Trace.traceBegin(131072, "setHalAutoSuspend(" + enable + ")");
            try {
                nativeSetAutoSuspend(enable);
                if (this.misBetaUser) {
                    synchronized (this.mHwPowerInfoService) {
                        this.mHwPowerInfoService.notePowerInfoSuspendState(enable);
                    }
                }
            } finally {
                Trace.traceEnd(131072);
            }
        }
    }

    private void setHalInteractiveModeLocked(boolean enable) {
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

    private boolean isInteractiveInternal() {
        boolean z = mSupportAod;
        synchronized (this.mLock) {
            if (PowerManagerInternal.isInteractive(this.mWakefulness) && !this.mBrightnessWaitModeEnabled) {
                z = true;
            }
        }
        return z;
    }

    private boolean isLowPowerModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLowPowerModeEnabled;
        }
        return z;
    }

    private boolean setLowPowerModeInternal(boolean mode) {
        int i = HALT_MODE_SHUTDOWN;
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "setLowPowerModeInternal " + mode + " mIsPowered=" + this.mIsPowered);
            }
            if (this.mIsPowered) {
                return mSupportAod;
            }
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "low_power";
            if (mode) {
                i = WAKE_LOCK_CPU;
            }
            Global.putInt(contentResolver, str, i);
            this.mLowPowerModeSetting = mode;
            if (this.mAutoLowPowerModeConfigured && this.mBatteryLevelLow) {
                if (mode && this.mAutoLowPowerModeSnoozing) {
                    if (DEBUG_SPEW) {
                        Slog.d(TAG, "setLowPowerModeInternal: clearing low power mode snooze");
                    }
                    this.mAutoLowPowerModeSnoozing = mSupportAod;
                } else if (!mode) {
                    if (!this.mAutoLowPowerModeSnoozing) {
                        if (DEBUG_SPEW) {
                            Slog.d(TAG, "setLowPowerModeInternal: snoozing low power mode");
                        }
                        this.mAutoLowPowerModeSnoozing = true;
                    }
                }
            }
            updateLowPowerModeLocked();
            return true;
        }
    }

    boolean isDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mDeviceIdleMode;
        }
        return z;
    }

    boolean isLightDeviceIdleModeInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mLightDeviceIdleMode;
        }
        return z;
    }

    private void handleBatteryStateChangedLocked() {
        this.mDirty |= DIRTY_BATTERY_STATE;
        updatePowerStateLocked();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void shutdownOrRebootInternal(int haltMode, boolean confirm, String reason, boolean wait) {
        if (this.mHandler == null || !this.mSystemReady) {
            throw new IllegalStateException("Too early to call shutdown() or reboot()");
        }
        Runnable runnable = new AnonymousClass5(haltMode, confirm, reason);
        Message msg = Message.obtain(this.mHandler, runnable);
        msg.setAsynchronous(true);
        this.mHandler.sendMessage(msg);
        if (wait) {
            synchronized (runnable) {
                while (true) {
                    try {
                    } catch (InterruptedException e) {
                    }
                }
            }
            runnable.wait();
        }
    }

    private void crashInternal(String message) {
        Thread t = new AnonymousClass6("PowerManagerService.crash()", message);
        try {
            t.start();
            t.join();
        } catch (InterruptedException e) {
            Slog.wtf(TAG, e);
        }
    }

    void setStayOnSettingInternal(int val) {
        Global.putInt(this.mContext.getContentResolver(), "stay_on_while_plugged_in", val);
    }

    void setMaximumScreenOffTimeoutFromDeviceAdminInternal(int timeMs) {
        synchronized (this.mLock) {
            this.mMaximumScreenOffTimeoutFromDeviceAdmin = timeMs;
            this.mDirty |= WAKE_LOCK_STAY_AWAKE;
            updatePowerStateLocked();
        }
    }

    boolean setDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode != enabled) {
                this.mDeviceIdleMode = enabled;
                updateWakeLockDisabledStatesLocked();
                if (enabled) {
                    EventLogTags.writeDeviceIdleOnPhase("power");
                } else {
                    EventLogTags.writeDeviceIdleOffPhase("power");
                }
                return true;
            }
            return mSupportAod;
        }
    }

    boolean setLightDeviceIdleModeInternal(boolean enabled) {
        synchronized (this.mLock) {
            if (this.mLightDeviceIdleMode != enabled) {
                this.mLightDeviceIdleMode = enabled;
                return true;
            }
            return mSupportAod;
        }
    }

    void setDeviceIdleWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    void setDeviceIdleTempWhitelistInternal(int[] appids) {
        synchronized (this.mLock) {
            this.mDeviceIdleTempWhitelist = appids;
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    void updateUidProcStateInternal(int uid, int procState) {
        synchronized (this.mLock) {
            this.mUidState.put(uid, procState);
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    void uidGoneInternal(int uid) {
        synchronized (this.mLock) {
            this.mUidState.delete(uid);
            if (this.mDeviceIdleMode) {
                updateWakeLockDisabledStatesLocked();
            }
        }
    }

    private void updateWakeLockDisabledStatesLocked() {
        boolean changed = mSupportAod;
        int numWakeLocks = this.mWakeLocks.size();
        for (int i = HALT_MODE_SHUTDOWN; i < numWakeLocks; i += WAKE_LOCK_CPU) {
            WakeLock wakeLock = (WakeLock) this.mWakeLocks.get(i);
            if ((wakeLock.mFlags & 65535) == WAKE_LOCK_CPU && setWakeLockDisabledStateLocked(wakeLock)) {
                changed = true;
                if (wakeLock.mDisabled) {
                    notifyWakeLockReleasedLocked(wakeLock);
                } else {
                    notifyWakeLockAcquiredLocked(wakeLock);
                }
            }
        }
        if (changed) {
            this.mDirty |= WAKE_LOCK_CPU;
            updatePowerStateLocked();
        }
    }

    protected boolean setWakeLockDisabledStateLocked(WakeLock wakeLock) {
        if ((wakeLock.mFlags & 65535) == WAKE_LOCK_CPU) {
            boolean disabled = mSupportAod;
            if (this.mDeviceIdleMode) {
                int appid = UserHandle.getAppId(wakeLock.mOwnerUid);
                if (appid >= HIGH_PRECISION_MAX_BRIGHTNESS && Arrays.binarySearch(this.mDeviceIdleWhitelist, appid) < 0 && Arrays.binarySearch(this.mDeviceIdleTempWhitelist, appid) < 0 && this.mUidState.get(wakeLock.mOwnerUid, WAKE_LOCK_PROXIMITY_SCREEN_OFF) > WAKE_LOCK_SCREEN_DIM) {
                    disabled = true;
                }
            }
            if (wakeLock.mDisabled != disabled) {
                wakeLock.mDisabled = disabled;
                return true;
            }
        }
        return mSupportAod;
    }

    private boolean isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() {
        if (this.mMaximumScreenOffTimeoutFromDeviceAdmin < 0 || this.mMaximumScreenOffTimeoutFromDeviceAdmin >= Integer.MAX_VALUE) {
            return mSupportAod;
        }
        return true;
    }

    private void setAttentionLightInternal(boolean on, int color) {
        synchronized (this.mLock) {
            if (this.mSystemReady) {
                int i;
                Light light = this.mAttentionLight;
                if (on) {
                    i = MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT;
                } else {
                    i = HALT_MODE_SHUTDOWN;
                }
                light.setFlashing(color, WAKE_LOCK_SCREEN_BRIGHT, i, HALT_MODE_SHUTDOWN);
                return;
            }
        }
    }

    private void boostScreenBrightnessInternal(long eventTime, int uid) {
        synchronized (this.mLock) {
            if (this.mSystemReady && this.mWakefulness != 0) {
                if (eventTime >= this.mLastScreenBrightnessBoostTime) {
                    Slog.i(TAG, "Brightness boost activated (uid " + uid + ")...");
                    this.mLastScreenBrightnessBoostTime = eventTime;
                    if (!this.mScreenBrightnessBoostInProgress) {
                        this.mScreenBrightnessBoostInProgress = true;
                        this.mNotifier.onScreenBrightnessBoostChanged();
                    }
                    this.mDirty |= DIRTY_SCREEN_BRIGHTNESS_BOOST;
                    userActivityNoUpdateLocked(eventTime, HALT_MODE_SHUTDOWN, HALT_MODE_SHUTDOWN, uid);
                    updatePowerStateLocked();
                    return;
                }
            }
        }
    }

    private boolean isScreenBrightnessBoostedInternal() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mScreenBrightnessBoostInProgress;
        }
        return z;
    }

    private void handleScreenBrightnessBoostTimeout() {
        synchronized (this.mLock) {
            if (DEBUG_SPEW) {
                Slog.d(TAG, "handleScreenBrightnessBoostTimeout");
            }
            this.mDirty |= DIRTY_SCREEN_BRIGHTNESS_BOOST;
            updatePowerStateLocked();
        }
    }

    private void setScreenBrightnessOverrideFromWindowManagerInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mScreenBrightnessOverrideFromWindowManager != brightness) {
                this.mScreenBrightnessOverrideFromWindowManager = brightness;
                if (DEBUG) {
                    Slog.d(TAG, "mScreenBrightnessOverrideFromWindowManager=" + this.mScreenBrightnessOverrideFromWindowManager);
                }
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                updatePowerStateLocked();
            }
        }
    }

    private void setUserInactiveOverrideFromWindowManagerInternal() {
        synchronized (this.mLock) {
            this.mUserInactiveOverrideFromWindowManager = true;
            this.mDirty |= WAKE_LOCK_SCREEN_DIM;
            updatePowerStateLocked();
        }
    }

    private void setUserActivityTimeoutOverrideFromWindowManagerInternal(long timeoutMillis) {
        synchronized (this.mLock) {
            if (this.mUserActivityTimeoutOverrideFromWindowManager != timeoutMillis) {
                this.mUserActivityTimeoutOverrideFromWindowManager = timeoutMillis;
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                updatePowerStateLocked();
            }
        }
    }

    private void setTemporaryScreenBrightnessSettingOverrideInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mTemporaryScreenBrightnessSettingOverride != brightness) {
                this.mTemporaryScreenBrightnessSettingOverride = brightness;
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                if (DEBUG) {
                    Slog.d(TAG, "mTemporaryScreenBrightnessSettingOverride=" + this.mTemporaryScreenBrightnessSettingOverride);
                }
                updatePowerStateLocked();
            }
        }
    }

    private void setTemporaryScreenAutoBrightnessSettingOverrideInternal(int brightness) {
        synchronized (this.mLock) {
            if (this.mTemporaryScreenAutoBrightnessSettingOverride != brightness && this.mDisplayPowerRequest.brightnessSetByUser) {
                if (brightness == DEFAULT_SLEEP_TIMEOUT) {
                    this.mDisplayManagerInternal.updateAutoBrightnessAdjustFactor(((float) this.mTemporaryScreenAutoBrightnessSettingOverride) / 255.0f);
                }
                this.mTemporaryScreenAutoBrightnessSettingOverride = brightness;
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                if (DEBUG) {
                    Slog.d(TAG, "mTemporaryScreenAutoBrightnessSettingOverride=" + this.mTemporaryScreenAutoBrightnessSettingOverride);
                }
                updatePowerStateLocked();
            }
        }
    }

    private void setTemporaryScreenAutoBrightnessAdjustmentSettingOverrideInternal(float adj) {
        synchronized (this.mLock) {
            if (this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride != adj) {
                this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride = adj;
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                if (DEBUG) {
                    Slog.d(TAG, "mTemporaryScreenAutoBrightnessAdjustmentSettingOverride=" + this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride);
                }
                updatePowerStateLocked();
            }
        }
    }

    private void setDozeOverrideFromDreamManagerInternal(int screenState, int screenBrightness) {
        synchronized (this.mLock) {
            if (!(this.mDozeScreenStateOverrideFromDreamManager == screenState && this.mDozeScreenBrightnessOverrideFromDreamManager == screenBrightness)) {
                this.mDozeScreenStateOverrideFromDreamManager = screenState;
                this.mDozeScreenBrightnessOverrideFromDreamManager = screenBrightness;
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                updatePowerStateLocked();
            }
        }
    }

    private void powerHintInternal(int hintId, int data) {
        nativeSendPowerHint(hintId, data);
    }

    public static void lowLevelShutdown(String reason) {
        if (reason == null) {
            reason = "";
        }
        SystemProperties.set("sys.powerctl", "shutdown," + reason);
    }

    public static void lowLevelReboot(String reason) {
        if (reason == null) {
            reason = "";
        }
        if (reason.equals("recovery") || reason.equals("recovery-update")) {
            SystemProperties.set("sys.powerctl", "reboot,recovery");
        } else {
            SystemProperties.set("sys.powerctl", "reboot," + reason);
        }
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

    protected void dumpInternal(PrintWriter pw) {
        pw.println("POWER MANAGER (dumpsys power)\n");
        synchronized (this.mLock) {
            pw.println("Power Manager State:");
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
            pw.println("  mUserActivitySummary=0x" + Integer.toHexString(this.mUserActivitySummary));
            pw.println("  mRequestWaitForNegativeProximity=" + this.mRequestWaitForNegativeProximity);
            pw.println("  mSandmanScheduled=" + this.mSandmanScheduled);
            pw.println("  mSandmanSummoned=" + this.mSandmanSummoned);
            pw.println("  mLowPowerModeEnabled=" + this.mLowPowerModeEnabled);
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
            pw.println("  mDozeAfterScreenOffConfig=" + this.mDozeAfterScreenOffConfig);
            pw.println("  mLowPowerModeSetting=" + this.mLowPowerModeSetting);
            pw.println("  mAutoLowPowerModeConfigured=" + this.mAutoLowPowerModeConfigured);
            pw.println("  mAutoLowPowerModeSnoozing=" + this.mAutoLowPowerModeSnoozing);
            pw.println("  mMinimumScreenOffTimeoutConfig=" + this.mMinimumScreenOffTimeoutConfig);
            pw.println("  mMaximumScreenDimDurationConfig=" + this.mMaximumScreenDimDurationConfig);
            pw.println("  mMaximumScreenDimRatioConfig=" + this.mMaximumScreenDimRatioConfig);
            pw.println("  mScreenOffTimeoutSetting=" + this.mScreenOffTimeoutSetting);
            pw.println("  mSleepTimeoutSetting=" + this.mSleepTimeoutSetting);
            pw.println("  mMaximumScreenOffTimeoutFromDeviceAdmin=" + this.mMaximumScreenOffTimeoutFromDeviceAdmin + " (enforced=" + isMaximumScreenOffTimeoutFromDeviceAdminEnforcedLocked() + ")");
            pw.println("  mStayOnWhilePluggedInSetting=" + this.mStayOnWhilePluggedInSetting);
            pw.println("  mScreenBrightnessSetting=" + this.mScreenBrightnessSetting);
            pw.println("  mScreenAutoBrightnessAdjustmentSetting=" + this.mScreenAutoBrightnessAdjustmentSetting);
            pw.println("  mScreenBrightnessModeSetting=" + this.mScreenBrightnessModeSetting);
            pw.println("  mScreenBrightnessOverrideFromWindowManager=" + this.mScreenBrightnessOverrideFromWindowManager);
            pw.println("  mUserActivityTimeoutOverrideFromWindowManager=" + this.mUserActivityTimeoutOverrideFromWindowManager);
            pw.println("  mUserInactiveOverrideFromWindowManager=" + this.mUserInactiveOverrideFromWindowManager);
            pw.println("  mTemporaryScreenBrightnessSettingOverride=" + this.mTemporaryScreenBrightnessSettingOverride);
            pw.println("  mTemporaryScreenAutoBrightnessAdjustmentSettingOverride=" + this.mTemporaryScreenAutoBrightnessAdjustmentSettingOverride);
            pw.println("  mDozeScreenStateOverrideFromDreamManager=" + this.mDozeScreenStateOverrideFromDreamManager);
            pw.println("  mDozeScreenBrightnessOverrideFromDreamManager=" + this.mDozeScreenBrightnessOverrideFromDreamManager);
            pw.println("  mScreenBrightnessSettingMinimum=" + this.mScreenBrightnessSettingMinimum);
            pw.println("  mScreenBrightnessSettingMaximum=" + this.mScreenBrightnessSettingMaximum);
            pw.println("  mScreenBrightnessSettingDefault=" + this.mScreenBrightnessSettingDefault);
            pw.println("  mDoubleTapWakeEnabled=" + this.mDoubleTapWakeEnabled);
            int sleepTimeout = getSleepTimeoutLocked();
            int screenOffTimeout = getScreenOffTimeoutLocked(sleepTimeout);
            int screenDimDuration = getScreenDimDurationLocked(screenOffTimeout);
            pw.println();
            pw.println("Sleep timeout: " + sleepTimeout + " ms");
            pw.println("Screen off timeout: " + screenOffTimeout + " ms");
            pw.println("Screen dim duration: " + screenDimDuration + " ms");
            pw.println();
            pw.println("UID states:");
            for (int i = HALT_MODE_SHUTDOWN; i < this.mUidState.size(); i += WAKE_LOCK_CPU) {
                pw.print("  UID ");
                UserHandle.formatUid(pw, this.mUidState.keyAt(i));
                pw.print(": ");
                pw.println(this.mUidState.valueAt(i));
            }
            pw.println();
            pw.println("Wake Locks: size=" + this.mWakeLocks.size());
            for (WakeLock wl : this.mWakeLocks) {
                pw.println("  " + wl);
            }
            pw.println();
            pw.println("Suspend Blockers: size=" + this.mSuspendBlockers.size());
            for (SuspendBlocker sb : this.mSuspendBlockers) {
                pw.println("  " + sb);
            }
            pw.println();
            pw.println("Display Power: " + this.mDisplayPowerCallbacks);
            WirelessChargerDetector wcd = this.mWirelessChargerDetector;
        }
        if (wcd != null) {
            wcd.dump(pw);
        }
    }

    private SuspendBlocker createSuspendBlockerLocked(String name) {
        SuspendBlocker suspendBlocker = new SuspendBlockerImpl(name);
        this.mSuspendBlockers.add(suspendBlocker);
        return suspendBlocker;
    }

    private void incrementBootCount() {
        synchronized (this.mLock) {
            int count;
            try {
                count = Global.getInt(getContext().getContentResolver(), "boot_count");
            } catch (SettingNotFoundException e) {
                count = HALT_MODE_SHUTDOWN;
            }
            Global.putInt(getContext().getContentResolver(), "boot_count", count + WAKE_LOCK_CPU);
        }
    }

    private static WorkSource copyWorkSource(WorkSource workSource) {
        return workSource != null ? new WorkSource(workSource) : null;
    }

    protected boolean isPhoneHeldWakeLock() {
        if ((this.mWakeLockSummary & WAKE_LOCK_PROXIMITY_SCREEN_OFF) != 0) {
            for (WakeLock wl : this.mWakeLocks) {
                if (incalluiPackageName.equals(wl.mPackageName) && (wl.mFlags & 65535) == WAKE_LOCK_STAY_AWAKE) {
                    return true;
                }
            }
        }
        return mSupportAod;
    }

    protected boolean isCarMachineHeldWakeLock() {
        if ((this.mWakeLockSummary & WAKE_LOCK_SCREEN_BRIGHT) != 0) {
            for (WakeLock wl : this.mWakeLocks) {
                if (machineCarPackageName.equals(wl.mPackageName) && (wl.mFlags & 65535) == 10) {
                    return true;
                }
            }
        }
        return mSupportAod;
    }

    private void setAodStateBySysfs(String file, int command) {
        IOException e;
        FileNotFoundException e2;
        Exception e3;
        Throwable th;
        Slog.i(TAG, "AOD PowerManagerService setAodStateBySysfs()");
        if (mSupportAod) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.DEVICE_POWER", null);
            String strCmd = Integer.toString(command);
            FileOutputStream fileOutputStream = null;
            try {
                FileOutputStream fileOutputStream2 = new FileOutputStream(file);
                try {
                    fileOutputStream2.write(strCmd.getBytes());
                    fileOutputStream2.flush();
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (IOException e4) {
                            Slog.e(TAG, "Error closing file: " + e4.toString());
                        }
                    }
                    fileOutputStream = fileOutputStream2;
                } catch (FileNotFoundException e5) {
                    e2 = e5;
                    fileOutputStream = fileOutputStream2;
                    Slog.e(TAG, "File not found: " + e2.toString());
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e42) {
                            Slog.e(TAG, "Error closing file: " + e42.toString());
                        }
                    }
                } catch (IOException e6) {
                    e42 = e6;
                    fileOutputStream = fileOutputStream2;
                    Slog.e(TAG, "Error accessing file: " + e42.toString());
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e422) {
                            Slog.e(TAG, "Error closing file: " + e422.toString());
                        }
                    }
                } catch (Exception e7) {
                    e3 = e7;
                    fileOutputStream = fileOutputStream2;
                    try {
                        Slog.e(TAG, "Exception occur: " + e3.toString());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e4222) {
                                Slog.e(TAG, "Error closing file: " + e4222.toString());
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e42222) {
                                Slog.e(TAG, "Error closing file: " + e42222.toString());
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fileOutputStream2;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            } catch (FileNotFoundException e8) {
                e2 = e8;
                Slog.e(TAG, "File not found: " + e2.toString());
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e9) {
                e42222 = e9;
                Slog.e(TAG, "Error accessing file: " + e42222.toString());
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e10) {
                e3 = e10;
                Slog.e(TAG, "Exception occur: " + e3.toString());
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
        }
    }

    private void setDozeOverrideFromAodLocked(int screenState, int screenBrightness) {
        Slog.i(TAG, "AOD PowerManagerService setDozeOverrideFromAodLocked()");
        if (mSupportAod) {
            if (!(this.mDozeScreenStateOverrideFromDreamManager == screenState && this.mDozeScreenBrightnessOverrideFromDreamManager == screenBrightness)) {
                this.mDozeScreenStateOverrideFromDreamManager = screenState;
                this.mDozeScreenBrightnessOverrideFromDreamManager = screenBrightness;
                this.mDirty |= WAKE_LOCK_STAY_AWAKE;
                updatePowerStateLocked();
                if (screenState == WAKE_LOCK_SCREEN_BRIGHT || screenState == MSG_SCREEN_BRIGHTNESS_BOOST_TIMEOUT) {
                    Light backLight = this.mLightsManager.getLight(HALT_MODE_SHUTDOWN);
                    if (backLight != null && backLight.isHighPrecision()) {
                        screenBrightness = (screenBrightness * HIGH_PRECISION_MAX_BRIGHTNESS) / RampAnimator.DEFAULT_MAX_BRIGHTNESS;
                    }
                    this.mDisplayManagerInternal.forceDisplayState(screenState, screenBrightness);
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

    protected void sendTempBrightnessToMonitor(String paramType, int brightness) {
    }

    protected void sendBrightnessModeToMonitor(boolean manualMode) {
    }

    protected void sendManualBrightnessToMonitor(int brightness) {
    }
}

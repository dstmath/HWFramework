package android.os;

import android.content.Context;
import android.os.IDeviceIdleController.Stub;
import android.util.HwPCUtils;
import android.util.Log;

public final class PowerManager {
    public static final int ACQUIRE_CAUSES_WAKEUP = 268435456;
    public static final String ACTION_DEVICE_IDLE_MODE_CHANGED = "android.os.action.DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED = "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED = "android.os.action.POWER_SAVE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED_INTERNAL = "android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL";
    public static final String ACTION_POWER_SAVE_MODE_CHANGING = "android.os.action.POWER_SAVE_MODE_CHANGING";
    public static final String ACTION_POWER_SAVE_TEMP_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED";
    public static final String ACTION_POWER_SAVE_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_WHITELIST_CHANGED";
    public static final String ACTION_SCREEN_BRIGHTNESS_BOOST_CHANGED = "android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED";
    public static final int BRIGHTNESS_DEFAULT = -1;
    public static final int BRIGHTNESS_OFF = 0;
    public static final int BRIGHTNESS_ON = 255;
    public static final int DOZE_WAKE_LOCK = 64;
    public static final int DRAW_WAKE_LOCK = 128;
    public static final String EXTRA_POWER_SAVE_MODE = "mode";
    @Deprecated
    public static final int FULL_WAKE_LOCK = 26;
    public static final int GO_TO_SLEEP_FLAG_NO_DOZE = 1;
    public static final int GO_TO_SLEEP_REASON_APPLICATION = 0;
    public static final int GO_TO_SLEEP_REASON_DEVICE_ADMIN = 1;
    public static final int GO_TO_SLEEP_REASON_HDMI = 5;
    public static final int GO_TO_SLEEP_REASON_LID_SWITCH = 3;
    public static final int GO_TO_SLEEP_REASON_PHONE_CALL = 9;
    public static final int GO_TO_SLEEP_REASON_POWER_BUTTON = 4;
    public static final int GO_TO_SLEEP_REASON_SLEEP_BUTTON = 6;
    public static final int GO_TO_SLEEP_REASON_TIMEOUT = 2;
    public static final int GO_TO_SLEEP_REASON_WAIT_BRIGHTNESS_TIMEOUT = 8;
    public static final int OFF_BECAUSE_OF_PROX_SENSOR = 7;
    public static final int ON_AFTER_RELEASE = 536870912;
    public static final int PARTIAL_WAKE_LOCK = 1;
    public static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
    public static final String REBOOT_QUIESCENT = "quiescent";
    public static final String REBOOT_RECOVERY = "recovery";
    public static final String REBOOT_RECOVERY_UPDATE = "recovery-update";
    public static final String REBOOT_REQUESTED_BY_DEVICE_OWNER = "deviceowner";
    public static final String REBOOT_SAFE_MODE = "safemode";
    public static final int RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY = 1;
    @Deprecated
    public static final int SCREEN_BRIGHT_WAKE_LOCK = 10;
    @Deprecated
    public static final int SCREEN_DIM_WAKE_LOCK = 6;
    public static final int SHUTDOWN_REASON_REBOOT = 2;
    public static final int SHUTDOWN_REASON_SHUTDOWN = 1;
    public static final int SHUTDOWN_REASON_THERMAL_SHUTDOWN = 4;
    public static final int SHUTDOWN_REASON_UNKNOWN = 0;
    public static final int SHUTDOWN_REASON_USER_REQUESTED = 3;
    public static final String SHUTDOWN_USER_REQUESTED = "userrequested";
    private static final String TAG = "PowerManager";
    public static final int UNIMPORTANT_FOR_LOGGING = 1073741824;
    public static final int USER_ACTIVITY_EVENT_ACCESSIBILITY = 3;
    public static final int USER_ACTIVITY_EVENT_BUTTON = 1;
    public static final int USER_ACTIVITY_EVENT_OTHER = 0;
    public static final int USER_ACTIVITY_EVENT_TOUCH = 2;
    public static final int USER_ACTIVITY_FLAG_INDIRECT = 2;
    public static final int USER_ACTIVITY_FLAG_NO_CHANGE_LIGHTS = 1;
    public static final int WAKE_LOCK_LEVEL_MASK = 65535;
    final Context mContext;
    final Handler mHandler;
    IDeviceIdleController mIDeviceIdleController;
    final IPowerManager mService;

    public static class BacklightBrightness {
        public static final int MAX_BRIGHTNESS = 255;
        public static final int MIN_BRIGHTNESS = 0;
        public int brightness;
        public int level;
        public int max;
        public int min;

        public BacklightBrightness(int max, int min, int level) {
            this.max = max;
            this.min = min;
            this.level = level;
            this.brightness = (((max & 255) << 16) | ((min & 255) << 8)) | (level & 255);
        }

        public BacklightBrightness(int brightness) {
            this.max = (brightness >> 16) & 255;
            this.min = (brightness >> 8) & 255;
            this.level = brightness & 255;
            this.brightness = brightness;
        }

        public boolean isValid() {
            if (this.max < 0 || this.max > 255 || this.min < 0 || this.min > 255 || this.level < 0 || this.level > 255 || this.brightness < 0 || this.min >= this.max) {
                return false;
            }
            return true;
        }

        public boolean updateBacklightBrightness(int brightness) {
            this.max = (brightness >> 16) & 255;
            this.min = (brightness >> 8) & 255;
            this.level = brightness & 255;
            this.brightness = brightness;
            if (isValid()) {
                return true;
            }
            this.max = Integer.MAX_VALUE;
            this.min = 0;
            return false;
        }
    }

    public final class WakeLock {
        private int mCount;
        private int mFlags;
        private boolean mHeld;
        private String mHistoryTag;
        private final String mPackageName;
        private boolean mRefCounted = true;
        private final Runnable mReleaser = new Runnable() {
            public void run() {
                WakeLock.this.release();
            }
        };
        private String mTag;
        private final IBinder mToken;
        private final String mTraceName;
        private WorkSource mWorkSource;

        WakeLock(int flags, String tag, String packageName) {
            this.mFlags = flags;
            this.mTag = tag;
            this.mPackageName = packageName;
            this.mToken = new Binder();
            this.mTraceName = "WakeLock (" + this.mTag + ")";
        }

        protected void finalize() throws Throwable {
            synchronized (this.mToken) {
                if (this.mHeld) {
                    Log.wtf(PowerManager.TAG, "WakeLock finalized while still held: " + this.mTag);
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                    try {
                        PowerManager.this.mService.releaseWakeLock(this.mToken, 0);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }

        public void setReferenceCounted(boolean value) {
            synchronized (this.mToken) {
                this.mRefCounted = value;
            }
        }

        public void acquire() {
            synchronized (this.mToken) {
                acquireLocked();
            }
        }

        public void acquire(long timeout) {
            synchronized (this.mToken) {
                acquireLocked();
                PowerManager.this.mHandler.postDelayed(this.mReleaser, timeout);
            }
        }

        private void acquireLocked() {
            if (this.mRefCounted) {
                int i = this.mCount;
                this.mCount = i + 1;
                if (i != 0) {
                    return;
                }
            }
            PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
            Trace.asyncTraceBegin(131072, this.mTraceName, 0);
            try {
                PowerManager.this.mService.acquireWakeLock(this.mToken, this.mFlags, this.mTag, this.mPackageName, this.mWorkSource, this.mHistoryTag);
                this.mHeld = true;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void release() {
            release(0);
        }

        /* JADX WARNING: Missing block: B:6:0x000d, code:
            if (r1 == 0) goto L_0x000f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void release(int flags) {
            synchronized (this.mToken) {
                if (this.mRefCounted) {
                    int i = this.mCount - 1;
                    this.mCount = i;
                }
                PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                if (this.mHeld) {
                    Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                    try {
                        PowerManager.this.mService.releaseWakeLock(this.mToken, flags);
                        this.mHeld = false;
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
                if (this.mCount < 0) {
                    throw new RuntimeException("WakeLock under-locked " + this.mTag);
                }
            }
        }

        public boolean isHeld() {
            boolean z;
            synchronized (this.mToken) {
                z = this.mHeld;
            }
            return z;
        }

        public void setWorkSource(WorkSource ws) {
            synchronized (this.mToken) {
                boolean changed;
                if (ws != null) {
                    if (ws.size() == 0) {
                        ws = null;
                    }
                }
                if (ws == null) {
                    changed = this.mWorkSource != null;
                    this.mWorkSource = null;
                } else if (this.mWorkSource == null) {
                    changed = true;
                    this.mWorkSource = new WorkSource(ws);
                } else {
                    changed = this.mWorkSource.diff(ws);
                    if (changed) {
                        this.mWorkSource.set(ws);
                    }
                }
                if (changed && this.mHeld) {
                    try {
                        PowerManager.this.mService.updateWakeLockWorkSource(this.mToken, this.mWorkSource, this.mHistoryTag);
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }

        public void setTag(String tag) {
            this.mTag = tag;
        }

        public String getTag() {
            return this.mTag;
        }

        public void setHistoryTag(String tag) {
            this.mHistoryTag = tag;
        }

        public void setUnimportantForLogging(boolean state) {
            if (state) {
                this.mFlags |= 1073741824;
            } else {
                this.mFlags &= -1073741825;
            }
        }

        public String toString() {
            String str;
            synchronized (this.mToken) {
                str = "WakeLock{" + Integer.toHexString(System.identityHashCode(this)) + " held=" + this.mHeld + ", refCount=" + this.mCount + "}";
            }
            return str;
        }

        public Runnable wrap(Runnable r) {
            acquire();
            return new -$Lambda$OsaxDBgigpqjZN1F4C6nYRYm1YQ(this, r);
        }

        /* synthetic */ void lambda$-android_os_PowerManager$WakeLock_63386(Runnable r) {
            try {
                r.run();
            } finally {
                release();
            }
        }
    }

    public PowerManager(Context context, IPowerManager service, Handler handler) {
        this.mContext = context;
        this.mService = service;
        this.mHandler = handler;
    }

    public boolean isHighPrecision() {
        boolean flag = false;
        try {
            return this.mService.isHighPrecision();
        } catch (RemoteException e) {
            return flag;
        }
    }

    public int getMinimumScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(17694849);
    }

    public int getMaximumScreenBrightnessSetting() {
        if (SystemProperties.getBoolean("ro.config.power", false)) {
            return 80;
        }
        return this.mContext.getResources().getInteger(17694848);
    }

    public int getDefaultScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(17694847);
    }

    public int getMinimumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694846);
    }

    public int getMaximumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694845);
    }

    public int getDefaultScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694844);
    }

    public WakeLock newWakeLock(int levelAndFlags, String tag) {
        if (this.mContext != null && HwPCUtils.isValidExtDisplayId(this.mContext) && (HwPCUtils.enabledInPad() ^ 1) != 0 && (65535 & levelAndFlags) == 10) {
            levelAndFlags = (levelAndFlags & -11) | 6;
        }
        validateWakeLockParameters(levelAndFlags, tag);
        return new WakeLock(levelAndFlags, tag, this.mContext.getOpPackageName());
    }

    public static void validateWakeLockParameters(int levelAndFlags, String tag) {
        switch (65535 & levelAndFlags) {
            case 1:
            case 6:
            case 10:
            case 26:
            case 32:
            case 64:
            case 128:
                if (tag == null) {
                    throw new IllegalArgumentException("The tag must not be null.");
                }
                return;
            default:
                throw new IllegalArgumentException("Must specify a valid wake lock level.");
        }
    }

    @Deprecated
    public void userActivity(long when, boolean noChangeLights) {
        int i;
        if (noChangeLights) {
            i = 1;
        } else {
            i = 0;
        }
        userActivity(when, 0, i);
    }

    public void userActivity(long when, int event, int flags) {
        try {
            this.mService.userActivity(when, event, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void goToSleep(long time) {
        goToSleep(time, 0, 0);
    }

    public void goToSleep(long time, int reason, int flags) {
        try {
            this.mService.goToSleep(time, reason, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setStartDreamFromOtherFlag(boolean flag) {
        try {
            this.mService.setStartDreamFromOtherFlag(flag);
        } catch (RemoteException e) {
        }
    }

    public void onCoverModeChanged(boolean iscovered) {
        try {
            this.mService.onCoverModeChanged(iscovered);
        } catch (RemoteException e) {
        }
    }

    public void setMirrorLinkPowerStatus(boolean status) {
        try {
            this.mService.setMirrorLinkPowerStatus(status);
        } catch (RemoteException e) {
        }
    }

    public void setModeToAutoNoClearOffsetEnable(boolean enable) {
        try {
            this.mService.setModeToAutoNoClearOffsetEnable(enable);
        } catch (RemoteException e) {
        }
    }

    public boolean startDream() {
        try {
            return this.mService.startDream();
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean stopDream() {
        try {
            return this.mService.stopDream();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void wakeUp(long time) {
        try {
            this.mService.wakeUp(time, "wakeUp", this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void wakeUp(long time, String reason) {
        try {
            this.mService.wakeUp(time, reason, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void startWakeUpReady(long eventTime) {
        try {
            this.mService.startWakeUpReady(eventTime, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
        }
    }

    public void stopWakeUpReady(long eventTime, boolean enableBright) {
        try {
            this.mService.stopWakeUpReady(eventTime, enableBright, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
        }
    }

    public void setAuthSucceeded() {
        try {
            this.mService.setAuthSucceeded();
        } catch (RemoteException e) {
        }
    }

    public int getDisplayPanelType() {
        int type = -1;
        try {
            return this.mService.getDisplayPanelType();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return type;
        }
    }

    public void nap(long time) {
        try {
            this.mService.nap(time);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void boostScreenBrightness(long time) {
        try {
            this.mService.boostScreenBrightness(time);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isScreenBrightnessBoosted() {
        try {
            return this.mService.isScreenBrightnessBoosted();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBacklightBrightness(int brightness) {
        try {
            this.mService.setTemporaryScreenBrightnessSettingOverride(brightness);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setBrightnessAnimationTime(boolean animationEnabled, int millisecond) {
        try {
            this.mService.setBrightnessAnimationTime(animationEnabled, millisecond);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int transformBrightness(int max, int min, int level) {
        return new BacklightBrightness(max, min, level).brightness;
    }

    public boolean isWakeLockLevelSupported(int level) {
        try {
            return this.mService.isWakeLockLevelSupported(level);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public boolean isScreenOn() {
        return isInteractive();
    }

    public boolean isInteractive() {
        try {
            return this.mService.isInteractive();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reboot(String reason) {
        try {
            this.mService.reboot(false, reason, true);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void rebootSafeMode() {
        try {
            this.mService.rebootSafeMode(false, true);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isPowerSaveMode() {
        try {
            return this.mService.isPowerSaveMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setPowerSaveMode(boolean mode) {
        try {
            return this.mService.setPowerSaveMode(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public PowerSaveState getPowerSaveState(int serviceType) {
        try {
            return this.mService.getPowerSaveState(serviceType);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isDeviceIdleMode() {
        try {
            return this.mService.isDeviceIdleMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isLightDeviceIdleMode() {
        try {
            return this.mService.isLightDeviceIdleMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isIgnoringBatteryOptimizations(String packageName) {
        synchronized (this) {
            if (this.mIDeviceIdleController == null) {
                this.mIDeviceIdleController = Stub.asInterface(ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
            }
        }
        try {
            return this.mIDeviceIdleController.isPowerSaveWhitelistApp(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void shutdown(boolean confirm, String reason, boolean wait) {
        try {
            this.mService.shutdown(confirm, reason, wait);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isSustainedPerformanceModeSupported() {
        return this.mContext.getResources().getBoolean(17957028);
    }

    public int getLastShutdownReason() {
        try {
            return this.mService.getLastShutdownReason();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUsingSkipWakeLock(int uid, String tag) {
        boolean ret = false;
        try {
            return this.mService.isUsingSkipWakeLock(uid, tag);
        } catch (RemoteException e) {
            return ret;
        }
    }

    public void setAodState(int globalState, int alpmMode) {
        try {
            this.mService.setAodState(globalState, alpmMode);
        } catch (RemoteException e) {
        }
    }

    public void setDozeOverrideFromAod(int screenState, int screenBrightness, IBinder binder) {
        try {
            this.mService.setDozeOverrideFromAod(screenState, screenBrightness, binder);
        } catch (RemoteException e) {
        }
    }

    public void regeditAodStateCallback(IAodStateCallback callback) {
        try {
            this.mService.regeditAodStateCallback(callback);
        } catch (RemoteException e) {
        }
    }

    public void unregeditAodStateCallback(IAodStateCallback callback) {
        try {
            this.mService.unregeditAodStateCallback(callback);
        } catch (RemoteException e) {
        }
    }
}

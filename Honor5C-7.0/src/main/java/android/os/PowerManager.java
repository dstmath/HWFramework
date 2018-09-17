package android.os;

import android.content.Context;
import android.os.IDeviceIdleController.Stub;
import android.preference.Preference;
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
    public static final String REBOOT_RECOVERY = "recovery";
    public static final String REBOOT_RECOVERY_UPDATE = "recovery-update";
    public static final String REBOOT_REQUESTED_BY_DEVICE_OWNER = "deviceowner";
    public static final String REBOOT_SAFE_MODE = "safemode";
    public static final int RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY = 1;
    @Deprecated
    public static final int SCREEN_BRIGHT_WAKE_LOCK = 10;
    @Deprecated
    public static final int SCREEN_DIM_WAKE_LOCK = 6;
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
            this.brightness = (((max & MAX_BRIGHTNESS) << 16) | ((min & MAX_BRIGHTNESS) << PowerManager.GO_TO_SLEEP_REASON_WAIT_BRIGHTNESS_TIMEOUT)) | (level & MAX_BRIGHTNESS);
        }

        public BacklightBrightness(int brightness) {
            this.max = (brightness >> 16) & MAX_BRIGHTNESS;
            this.min = (brightness >> PowerManager.GO_TO_SLEEP_REASON_WAIT_BRIGHTNESS_TIMEOUT) & MAX_BRIGHTNESS;
            this.level = brightness & MAX_BRIGHTNESS;
            this.brightness = brightness;
        }

        public boolean isValid() {
            if (this.max < 0 || this.max > MAX_BRIGHTNESS || this.min < 0 || this.min > MAX_BRIGHTNESS || this.level < 0 || this.level > MAX_BRIGHTNESS || this.brightness < 0 || this.min >= this.max) {
                return false;
            }
            return true;
        }

        public boolean updateBacklightBrightness(int brightness) {
            this.max = (brightness >> 16) & MAX_BRIGHTNESS;
            this.min = (brightness >> PowerManager.GO_TO_SLEEP_REASON_WAIT_BRIGHTNESS_TIMEOUT) & MAX_BRIGHTNESS;
            this.level = brightness & MAX_BRIGHTNESS;
            this.brightness = brightness;
            if (isValid()) {
                return true;
            }
            this.max = Preference.DEFAULT_ORDER;
            this.min = PowerManager.USER_ACTIVITY_EVENT_OTHER;
            return false;
        }
    }

    public final class WakeLock {
        private int mCount;
        private int mFlags;
        private boolean mHeld;
        private String mHistoryTag;
        private final String mPackageName;
        private boolean mRefCounted;
        private final Runnable mReleaser;
        private String mTag;
        private final IBinder mToken;
        private final String mTraceName;
        private WorkSource mWorkSource;

        WakeLock(int flags, String tag, String packageName) {
            this.mRefCounted = true;
            this.mReleaser = new Runnable() {
                public void run() {
                    WakeLock.this.release();
                }
            };
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
                    Trace.asyncTraceEnd(Trace.TRACE_TAG_POWER, this.mTraceName, PowerManager.USER_ACTIVITY_EVENT_OTHER);
                    try {
                        PowerManager.this.mService.releaseWakeLock(this.mToken, PowerManager.USER_ACTIVITY_EVENT_OTHER);
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
                this.mCount = i + PowerManager.USER_ACTIVITY_FLAG_NO_CHANGE_LIGHTS;
                if (i != 0) {
                    return;
                }
            }
            PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
            Trace.asyncTraceBegin(Trace.TRACE_TAG_POWER, this.mTraceName, PowerManager.USER_ACTIVITY_EVENT_OTHER);
            try {
                PowerManager.this.mService.acquireWakeLock(this.mToken, this.mFlags, this.mTag, this.mPackageName, this.mWorkSource, this.mHistoryTag);
                this.mHeld = true;
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void release() {
            release(PowerManager.USER_ACTIVITY_EVENT_OTHER);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void release(int flags) {
            synchronized (this.mToken) {
                if (this.mRefCounted) {
                    int i = this.mCount + PowerManager.BRIGHTNESS_DEFAULT;
                    this.mCount = i;
                }
                PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                if (this.mHeld) {
                    Trace.asyncTraceEnd(Trace.TRACE_TAG_POWER, this.mTraceName, PowerManager.USER_ACTIVITY_EVENT_OTHER);
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

        public void setHistoryTag(String tag) {
            this.mHistoryTag = tag;
        }

        public void setUnimportantForLogging(boolean state) {
            if (state) {
                this.mFlags |= PowerManager.UNIMPORTANT_FOR_LOGGING;
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
    }

    public PowerManager(Context context, IPowerManager service, Handler handler) {
        this.mContext = context;
        this.mService = service;
        this.mHandler = handler;
    }

    public boolean isHighPrecision() {
        boolean flag = false;
        try {
            flag = this.mService.isHighPrecision();
        } catch (RemoteException e) {
        }
        return flag;
    }

    public int getMinimumScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(17694819);
    }

    public int getMaximumScreenBrightnessSetting() {
        if (SystemProperties.getBoolean("ro.config.power", false)) {
            return 80;
        }
        return this.mContext.getResources().getInteger(17694820);
    }

    public int getDefaultScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(17694821);
    }

    public static boolean useTwilightAdjustmentFeature() {
        return SystemProperties.getBoolean("persist.power.usetwilightadj", false);
    }

    public WakeLock newWakeLock(int levelAndFlags, String tag) {
        validateWakeLockParameters(levelAndFlags, tag);
        return new WakeLock(levelAndFlags, tag, this.mContext.getOpPackageName());
    }

    public static void validateWakeLockParameters(int levelAndFlags, String tag) {
        switch (WAKE_LOCK_LEVEL_MASK & levelAndFlags) {
            case USER_ACTIVITY_FLAG_NO_CHANGE_LIGHTS /*1*/:
            case SCREEN_DIM_WAKE_LOCK /*6*/:
            case SCREEN_BRIGHT_WAKE_LOCK /*10*/:
            case FULL_WAKE_LOCK /*26*/:
            case PROXIMITY_SCREEN_OFF_WAKE_LOCK /*32*/:
            case DOZE_WAKE_LOCK /*64*/:
            case DRAW_WAKE_LOCK /*128*/:
                if (tag == null) {
                    throw new IllegalArgumentException("The tag must not be null.");
                }
            default:
                throw new IllegalArgumentException("Must specify a valid wake lock level.");
        }
    }

    @Deprecated
    public void userActivity(long when, boolean noChangeLights) {
        int i;
        if (noChangeLights) {
            i = USER_ACTIVITY_FLAG_NO_CHANGE_LIGHTS;
        } else {
            i = USER_ACTIVITY_EVENT_OTHER;
        }
        userActivity(when, USER_ACTIVITY_EVENT_OTHER, i);
    }

    public void userActivity(long when, int event, int flags) {
        try {
            this.mService.userActivity(when, event, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void goToSleep(long time) {
        goToSleep(time, USER_ACTIVITY_EVENT_OTHER, USER_ACTIVITY_EVENT_OTHER);
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

    public void setMirrorLinkPowerStatus(boolean status) {
        try {
            this.mService.setMirrorLinkPowerStatus(status);
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
        return this.mContext.getResources().getBoolean(17957052);
    }

    public boolean isUsingSkipWakeLock(int uid, String tag) {
        boolean ret = false;
        try {
            ret = this.mService.isUsingSkipWakeLock(uid, tag);
        } catch (RemoteException e) {
        }
        return ret;
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

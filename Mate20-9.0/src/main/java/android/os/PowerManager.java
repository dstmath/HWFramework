package android.os;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.IDeviceIdleController;
import android.os.PowerManager;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public final class PowerManager {
    public static final int ACQUIRE_CAUSES_WAKEUP = 268435456;
    public static final String ACTION_DEVICE_IDLE_MODE_CHANGED = "android.os.action.DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED = "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED = "android.os.action.POWER_SAVE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED_INTERNAL = "android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL";
    public static final String ACTION_POWER_SAVE_MODE_CHANGING = "android.os.action.POWER_SAVE_MODE_CHANGING";
    public static final String ACTION_POWER_SAVE_TEMP_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED";
    public static final String ACTION_POWER_SAVE_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_WHITELIST_CHANGED";
    @SystemApi
    @Deprecated
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
    public static final int GO_TO_SLEEP_REASON_ACCESSIBILITY = 7;
    public static final int GO_TO_SLEEP_REASON_APPLICATION = 0;
    public static final int GO_TO_SLEEP_REASON_DEVICE_ADMIN = 1;
    public static final int GO_TO_SLEEP_REASON_HDMI = 5;
    public static final int GO_TO_SLEEP_REASON_LID_SWITCH = 3;
    public static final int GO_TO_SLEEP_REASON_PHONE_CALL = 102;
    public static final int GO_TO_SLEEP_REASON_POWER_BUTTON = 4;
    public static final int GO_TO_SLEEP_REASON_SLEEP_BUTTON = 6;
    public static final int GO_TO_SLEEP_REASON_TIMEOUT = 2;
    public static final int GO_TO_SLEEP_REASON_WAIT_BRIGHTNESS_TIMEOUT = 101;
    public static final int LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF = 2;
    public static final int LOCATION_MODE_FOREGROUND_ONLY = 3;
    public static final int LOCATION_MODE_GPS_DISABLED_WHEN_SCREEN_OFF = 1;
    public static final int LOCATION_MODE_NO_CHANGE = 0;
    public static final int OFF_BECAUSE_OF_PROX_SENSOR = 100;
    public static final int ON_AFTER_RELEASE = 536870912;
    public static final int PARTIAL_WAKE_LOCK = 1;
    public static final int PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32;
    public static final String REBOOT_QUIESCENT = "quiescent";
    public static final String REBOOT_RECOVERY = "recovery";
    public static final String REBOOT_RECOVERY_UPDATE = "recovery-update";
    public static final String REBOOT_REQUESTED_BY_DEVICE_OWNER = "deviceowner";
    public static final String REBOOT_SAFE_MODE = "safemode";
    public static final int RELEASE_FLAG_TIMEOUT = 65536;
    public static final int RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY = 1;
    @Deprecated
    public static final int SCREEN_BRIGHT_WAKE_LOCK = 10;
    @Deprecated
    public static final int SCREEN_DIM_WAKE_LOCK = 6;
    public static final String SHUTDOWN_BATTERY_THERMAL_STATE = "thermal,battery";
    public static final String SHUTDOWN_LOW_BATTERY = "battery";
    public static final int SHUTDOWN_REASON_BATTERY_THERMAL = 6;
    public static final int SHUTDOWN_REASON_LOW_BATTERY = 5;
    public static final int SHUTDOWN_REASON_REBOOT = 2;
    public static final int SHUTDOWN_REASON_SHUTDOWN = 1;
    public static final int SHUTDOWN_REASON_THERMAL_SHUTDOWN = 4;
    public static final int SHUTDOWN_REASON_UNKNOWN = 0;
    public static final int SHUTDOWN_REASON_USER_REQUESTED = 3;
    public static final String SHUTDOWN_USER_REQUESTED = "userrequested";
    private static final String TAG = "PowerManager";
    public static final int UNIMPORTANT_FOR_LOGGING = 1073741824;
    @SystemApi
    public static final int USER_ACTIVITY_EVENT_ACCESSIBILITY = 3;
    @SystemApi
    public static final int USER_ACTIVITY_EVENT_BUTTON = 1;
    @SystemApi
    public static final int USER_ACTIVITY_EVENT_OTHER = 0;
    @SystemApi
    public static final int USER_ACTIVITY_EVENT_TOUCH = 2;
    @SystemApi
    public static final int USER_ACTIVITY_FLAG_INDIRECT = 2;
    @SystemApi
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

        public BacklightBrightness() {
        }

        public BacklightBrightness(int max2, int min2, int level2) {
            this.max = max2;
            this.min = min2;
            this.level = level2;
            this.brightness = ((max2 & 255) << 16) | ((min2 & 255) << 8) | (level2 & 255);
        }

        public BacklightBrightness(int brightness2) {
            this.max = (brightness2 >> 16) & 255;
            this.min = (brightness2 >> 8) & 255;
            this.level = brightness2 & 255;
            this.brightness = brightness2;
        }

        public boolean isValid() {
            if (this.max < 0 || this.max > 255 || this.min < 0 || this.min > 255 || this.level < 0 || this.level > 255 || this.brightness < 0 || this.min >= this.max) {
                return false;
            }
            return true;
        }

        public boolean updateBacklightBrightness(int brightness2) {
            this.max = (brightness2 >> 16) & 255;
            this.min = (brightness2 >> 8) & 255;
            this.level = brightness2 & 255;
            this.brightness = brightness2;
            if (isValid()) {
                return true;
            }
            this.max = Integer.MAX_VALUE;
            this.min = 0;
            return false;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LocationPowerSaveMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceType {
        public static final int ANIMATION = 3;
        public static final int AOD = 14;
        public static final int BATTERY_STATS = 9;
        public static final int DATA_SAVER = 10;
        public static final int FORCE_ALL_APPS_STANDBY = 11;
        public static final int FORCE_BACKGROUND_CHECK = 12;
        public static final int FULL_BACKUP = 4;
        public static final int GPS = 1;
        public static final int KEYVALUE_BACKUP = 5;
        public static final int NETWORK_FIREWALL = 6;
        public static final int NULL = 0;
        public static final int OPTIONAL_SENSORS = 13;
        public static final int SCREEN_BRIGHTNESS = 7;
        public static final int SOUND = 8;
        public static final int VIBRATION = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ShutdownReason {
    }

    public final class WakeLock {
        private int mExternalCount;
        private int mFlags;
        private boolean mHeld;
        private String mHistoryTag;
        private int mInternalCount;
        private final String mPackageName;
        private boolean mRefCounted = true;
        private final Runnable mReleaser = new Runnable() {
            public void run() {
                WakeLock.this.release(65536);
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

        /* access modifiers changed from: protected */
        public void finalize() throws Throwable {
            synchronized (this.mToken) {
                if (this.mHeld) {
                    Log.wtf(PowerManager.TAG, "WakeLock finalized while still held: " + this.mTag);
                    Trace.asyncTraceEnd(Trace.TRACE_TAG_POWER, this.mTraceName, 0);
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
            this.mInternalCount++;
            this.mExternalCount++;
            if (!this.mRefCounted || this.mInternalCount == 1) {
                PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                Trace.asyncTraceBegin(Trace.TRACE_TAG_POWER, this.mTraceName, 0);
                try {
                    PowerManager.this.mService.acquireWakeLock(this.mToken, this.mFlags, this.mTag, this.mPackageName, this.mWorkSource, this.mHistoryTag);
                    this.mHeld = true;
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        }

        public void release() {
            release(0);
        }

        public void release(int flags) {
            synchronized (this.mToken) {
                if (this.mInternalCount > 0) {
                    this.mInternalCount--;
                }
                if ((65536 & flags) == 0) {
                    this.mExternalCount--;
                }
                if (!this.mRefCounted || this.mInternalCount == 0) {
                    PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                    if (this.mHeld) {
                        Trace.asyncTraceEnd(Trace.TRACE_TAG_POWER, this.mTraceName, 0);
                        try {
                            PowerManager.this.mService.releaseWakeLock(this.mToken, flags);
                            this.mHeld = false;
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                }
                if (this.mRefCounted) {
                    if (this.mExternalCount < 0) {
                        throw new RuntimeException("WakeLock under-locked " + this.mTag);
                    }
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
                if (ws != null) {
                    try {
                        if (ws.isEmpty()) {
                            ws = null;
                        }
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                boolean changed = true;
                if (ws == null) {
                    if (this.mWorkSource == null) {
                        changed = false;
                    }
                    this.mWorkSource = null;
                } else if (this.mWorkSource == null) {
                    changed = true;
                    this.mWorkSource = new WorkSource(ws);
                } else {
                    changed = true ^ this.mWorkSource.equals(ws);
                    if (changed) {
                        this.mWorkSource.set(ws);
                    }
                }
                if (changed && this.mHeld) {
                    PowerManager.this.mService.updateWakeLockWorkSource(this.mToken, this.mWorkSource, this.mHistoryTag);
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
                str = "WakeLock{" + Integer.toHexString(System.identityHashCode(this)) + " held=" + this.mHeld + ", refCount=" + this.mInternalCount + "}";
            }
            return str;
        }

        public void writeToProto(ProtoOutputStream proto, long fieldId) {
            synchronized (this.mToken) {
                long token = proto.start(fieldId);
                proto.write(1138166333441L, this.mTag);
                proto.write(1138166333442L, this.mPackageName);
                proto.write(1133871366147L, this.mHeld);
                proto.write(1120986464260L, this.mInternalCount);
                if (this.mWorkSource != null) {
                    this.mWorkSource.writeToProto(proto, 1146756268037L);
                }
                proto.end(token);
            }
        }

        public Runnable wrap(Runnable r) {
            acquire();
            return new Runnable(r) {
                private final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PowerManager.WakeLock.lambda$wrap$0(PowerManager.WakeLock.this, this.f$1);
                }
            };
        }

        public static /* synthetic */ void lambda$wrap$0(WakeLock wakeLock, Runnable r) {
            try {
                r.run();
            } finally {
                wakeLock.release();
            }
        }
    }

    public PowerManager(Context context, IPowerManager service, Handler handler) {
        this.mContext = context;
        this.mService = service;
        this.mHandler = handler;
    }

    public int getMinimumScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(17694862);
    }

    public int getMaximumScreenBrightnessSetting() {
        if (SystemProperties.getBoolean("ro.config.power", false)) {
            return 80;
        }
        return this.mContext.getResources().getInteger(17694861);
    }

    public int getDefaultScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(17694860);
    }

    public int getMinimumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694859);
    }

    public int getMaximumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694858);
    }

    public int getDefaultScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(17694857);
    }

    public WakeLock newWakeLock(int levelAndFlags, String tag) {
        validateWakeLockParameters(levelAndFlags, tag);
        return new WakeLock(levelAndFlags, tag, this.mContext.getOpPackageName());
    }

    public static void validateWakeLockParameters(int levelAndFlags, String tag) {
        int i = 65535 & levelAndFlags;
        if (i != 1 && i != 6 && i != 10 && i != 26 && i != 32 && i != 64 && i != 128) {
            throw new IllegalArgumentException("Must specify a valid wake lock level.");
        } else if (tag == null) {
            throw new IllegalArgumentException("The tag must not be null.");
        }
    }

    @Deprecated
    public void userActivity(long when, boolean noChangeLights) {
        userActivity(when, 0, noChangeLights);
    }

    @SystemApi
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
        try {
            return this.mService.getDisplayPanelType();
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return -1;
        }
    }

    public int hwBrightnessSetData(String name, Bundle data) {
        try {
            return this.mService.hwBrightnessSetData(name, data);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int hwBrightnessGetData(String name, Bundle data) {
        try {
            return this.mService.hwBrightnessGetData(name, data);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int hwBrightnessRegisterCallback(IHwBrightnessCallback cb, List<String> filter) {
        try {
            return this.mService.hwBrightnessRegisterCallback(cb, filter);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public int hwBrightnessUnregisterCallback(IHwBrightnessCallback cb) {
        try {
            return this.mService.hwBrightnessUnregisterCallback(cb);
        } catch (RemoteException e) {
            return -1;
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

    @SystemApi
    @Deprecated
    public boolean isScreenBrightnessBoosted() {
        return false;
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

    public int getLocationPowerSaveMode() {
        PowerSaveState powerSaveState = getPowerSaveState(1);
        if (!powerSaveState.globalBatterySaverEnabled) {
            return 0;
        }
        return powerSaveState.gpsMode;
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
                this.mIDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
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
        return this.mContext.getResources().getBoolean(17957046);
    }

    public void setDozeAfterScreenOff(boolean dozeAfterScreenOf) {
        try {
            this.mService.setDozeAfterScreenOff(dozeAfterScreenOf);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getLastShutdownReason() {
        try {
            return this.mService.getLastShutdownReason();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isUsingSkipWakeLock(int uid, String tag) {
        try {
            return this.mService.isUsingSkipWakeLock(uid, tag);
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setAodState(int globalState, int alpmMode) {
        try {
            this.mService.setAodState(globalState, alpmMode);
        } catch (RemoteException e) {
        }
    }

    public int getAodState(String file) {
        try {
            return this.mService.getAodState(file);
        } catch (RemoteException e) {
            Log.e(TAG, "getAodState RemoteException" + e.toString());
            return -1;
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

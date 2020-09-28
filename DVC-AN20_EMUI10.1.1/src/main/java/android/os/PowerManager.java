package android.os;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.media.AudioSystem;
import android.os.IDeviceIdleController;
import android.os.IThermalService;
import android.os.IThermalStatusListener;
import android.os.PowerManager;
import android.service.dreams.Sandman;
import android.util.ArrayMap;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.R;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.util.Preconditions;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;

public final class PowerManager {
    public static final int ACQUIRE_CAUSES_WAKEUP = 268435456;
    public static final String ACTION_DEVICE_IDLE_MODE_CHANGED = "android.os.action.DEVICE_IDLE_MODE_CHANGED";
    @UnsupportedAppUsage
    public static final String ACTION_LIGHT_DEVICE_IDLE_MODE_CHANGED = "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED = "android.os.action.POWER_SAVE_MODE_CHANGED";
    public static final String ACTION_POWER_SAVE_MODE_CHANGED_INTERNAL = "android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL";
    @UnsupportedAppUsage
    public static final String ACTION_POWER_SAVE_MODE_CHANGING = "android.os.action.POWER_SAVE_MODE_CHANGING";
    public static final String ACTION_POWER_SAVE_TEMP_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_TEMP_WHITELIST_CHANGED";
    public static final String ACTION_POWER_SAVE_WHITELIST_CHANGED = "android.os.action.POWER_SAVE_WHITELIST_CHANGED";
    @SystemApi
    @Deprecated
    public static final String ACTION_SCREEN_BRIGHTNESS_BOOST_CHANGED = "android.os.action.SCREEN_BRIGHTNESS_BOOST_CHANGED";
    public static final int BRIGHTNESS_DEFAULT = -1;
    public static final int BRIGHTNESS_OFF = 0;
    @UnsupportedAppUsage
    public static final int BRIGHTNESS_ON = 255;
    public static final int DOZE_WAKE_LOCK = 64;
    public static final int DRAW_WAKE_LOCK = 128;
    @UnsupportedAppUsage
    public static final String EXTRA_POWER_SAVE_MODE = "mode";
    @Deprecated
    public static final int FULL_WAKE_LOCK = 26;
    public static final int GO_TO_SLEEP_FLAG_NO_DOZE = 1;
    public static final int GO_TO_SLEEP_FLAG_SHUTDOWN = 65536;
    public static final int GO_TO_SLEEP_REASON_ACCESSIBILITY = 7;
    public static final int GO_TO_SLEEP_REASON_APPLICATION = 0;
    public static final int GO_TO_SLEEP_REASON_DEVICE_ADMIN = 1;
    public static final int GO_TO_SLEEP_REASON_FORCE_SUSPEND = 8;
    public static final int GO_TO_SLEEP_REASON_HDMI = 5;
    public static final int GO_TO_SLEEP_REASON_LID_SWITCH = 3;
    public static final int GO_TO_SLEEP_REASON_MAX = 102;
    public static final int GO_TO_SLEEP_REASON_MIN = 0;
    public static final int GO_TO_SLEEP_REASON_PHONE_CALL = 102;
    public static final int GO_TO_SLEEP_REASON_POWER_BUTTON = 4;
    public static final int GO_TO_SLEEP_REASON_SLEEP_BUTTON = 6;
    @UnsupportedAppUsage
    public static final int GO_TO_SLEEP_REASON_TIMEOUT = 2;
    public static final int GO_TO_SLEEP_REASON_WAIT_BRIGHTNESS_TIMEOUT = 101;
    public static final int LOCATION_MODE_ALL_DISABLED_WHEN_SCREEN_OFF = 2;
    public static final int LOCATION_MODE_FOREGROUND_ONLY = 3;
    public static final int LOCATION_MODE_GPS_DISABLED_WHEN_SCREEN_OFF = 1;
    public static final int LOCATION_MODE_NO_CHANGE = 0;
    public static final int LOCATION_MODE_THROTTLE_REQUESTS_WHEN_SCREEN_OFF = 4;
    public static final int MAX_LOCATION_MODE = 4;
    public static final int MIN_LOCATION_MODE = 0;
    public static final int OFF_BECAUSE_OF_PROX_SENSOR = 100;
    public static final int ON_AFTER_RELEASE = 536870912;
    public static final int PARTIAL_WAKE_LOCK = 1;
    @SystemApi
    public static final int POWER_SAVE_MODE_TRIGGER_DYNAMIC = 1;
    @SystemApi
    public static final int POWER_SAVE_MODE_TRIGGER_PERCENTAGE = 0;
    public static final int PRE_IDLE_TIMEOUT_MODE_LONG = 1;
    public static final int PRE_IDLE_TIMEOUT_MODE_NORMAL = 0;
    public static final int PRE_IDLE_TIMEOUT_MODE_SHORT = 2;
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
    public static final String SHUTDOWN_THERMAL_STATE = "thermal";
    public static final String SHUTDOWN_USER_REQUESTED = "userrequested";
    private static final String TAG = "PowerManager";
    public static final int THERMAL_STATUS_CRITICAL = 4;
    public static final int THERMAL_STATUS_EMERGENCY = 5;
    public static final int THERMAL_STATUS_LIGHT = 1;
    public static final int THERMAL_STATUS_MODERATE = 2;
    public static final int THERMAL_STATUS_NONE = 0;
    public static final int THERMAL_STATUS_SEVERE = 3;
    public static final int THERMAL_STATUS_SHUTDOWN = 6;
    public static final int UNIMPORTANT_FOR_LOGGING = 1073741824;
    @SystemApi
    public static final int USER_ACTIVITY_EVENT_ACCESSIBILITY = 3;
    public static final int USER_ACTIVITY_EVENT_ATTENTION = 4;
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
    public static final int WAKE_REASON_APPLICATION = 2;
    public static final int WAKE_REASON_CAMERA_LAUNCH = 5;
    public static final int WAKE_REASON_FINGER_PRINT = 101;
    public static final int WAKE_REASON_GESTURE = 4;
    public static final int WAKE_REASON_HDMI = 8;
    public static final int WAKE_REASON_LID = 9;
    public static final int WAKE_REASON_PICKUP = 102;
    public static final int WAKE_REASON_PLUGGED_IN = 3;
    public static final int WAKE_REASON_POWER_BUTTON = 1;
    public static final int WAKE_REASON_PROXIMITY = 100;
    public static final int WAKE_REASON_UNKNOWN = 0;
    public static final int WAKE_REASON_WAKE_KEY = 6;
    public static final int WAKE_REASON_WAKE_MOTION = 7;
    final Context mContext;
    final Handler mHandler;
    IDeviceIdleController mIDeviceIdleController;
    private final ArrayMap<OnThermalStatusChangedListener, IThermalStatusListener> mListenerMap = new ArrayMap<>();
    @UnsupportedAppUsage
    final IPowerManager mService;
    IThermalService mThermalService;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AutoPowerSaveModeTriggers {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface LocationPowerSaveMode {
    }

    public interface OnThermalStatusChangedListener {
        void onThermalStatusChanged(int i);
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
        public static final int KEYVALUE_BACKUP = 5;
        public static final int LOCATION = 1;
        public static final int NETWORK_FIREWALL = 6;
        public static final int NIGHT_MODE = 16;
        public static final int NULL = 0;
        public static final int OPTIONAL_SENSORS = 13;
        public static final int QUICK_DOZE = 15;
        public static final int SCREEN_BRIGHTNESS = 7;
        public static final int SOUND = 8;
        public static final int VIBRATION = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ShutdownReason {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ThermalStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WakeReason {
    }

    public static String sleepReasonToString(int sleepReason) {
        switch (sleepReason) {
            case 0:
                return "application";
            case 1:
                return "device_admin";
            case 2:
                return GpsNetInitiatedHandler.NI_INTENT_KEY_TIMEOUT;
            case 3:
                return "lid_switch";
            case 4:
                return "power_button";
            case 5:
                return AudioSystem.DEVICE_OUT_HDMI_NAME;
            case 6:
                return "sleep_button";
            case 7:
                return Context.ACCESSIBILITY_SERVICE;
            case 8:
                return "force_suspend";
            default:
                switch (sleepReason) {
                    case 100:
                        return "proximity_sensor";
                    case 101:
                        return "wait_brightness_timeout";
                    case 102:
                        return "phone_call";
                    default:
                        return Integer.toString(sleepReason);
                }
        }
    }

    public static String wakeReasonToString(int wakeReason) {
        switch (wakeReason) {
            case 0:
                return "WAKE_REASON_UNKNOWN";
            case 1:
                return "WAKE_REASON_POWER_BUTTON";
            case 2:
                return "WAKE_REASON_APPLICATION";
            case 3:
                return "WAKE_REASON_PLUGGED_IN";
            case 4:
                return "WAKE_REASON_GESTURE";
            case 5:
                return "WAKE_REASON_CAMERA_LAUNCH";
            case 6:
                return "WAKE_REASON_WAKE_KEY";
            case 7:
                return "WAKE_REASON_WAKE_MOTION";
            case 8:
                return "WAKE_REASON_HDMI";
            case 9:
                return "WAKE_REASON_LID";
            default:
                switch (wakeReason) {
                    case 100:
                        return "WAKE_REASON_PROXIMITY";
                    case 101:
                        return "WAKE_REASON_FINGER_PRINT";
                    case 102:
                        return "WAKE_REASON_PICKUP";
                    default:
                        return Integer.toString(wakeReason);
                }
        }
    }

    public static class WakeData {
        public int wakeReason;
        public long wakeTime;

        public WakeData(long wakeTime2, int wakeReason2) {
            this.wakeTime = wakeTime2;
            this.wakeReason = wakeReason2;
        }
    }

    public static String locationPowerSaveModeToString(int mode) {
        if (mode == 0) {
            return "NO_CHANGE";
        }
        if (mode == 1) {
            return "GPS_DISABLED_WHEN_SCREEN_OFF";
        }
        if (mode == 2) {
            return "ALL_DISABLED_WHEN_SCREEN_OFF";
        }
        if (mode == 3) {
            return "FOREGROUND_ONLY";
        }
        if (mode != 4) {
            return Integer.toString(mode);
        }
        return "THROTTLE_REQUESTS_WHEN_SCREEN_OFF";
    }

    public PowerManager(Context context, IPowerManager service, Handler handler) {
        this.mContext = context;
        this.mService = service;
        this.mHandler = handler;
    }

    @UnsupportedAppUsage
    public int getMinimumScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(R.integer.config_screenBrightnessSettingMinimum);
    }

    @UnsupportedAppUsage
    public int getMaximumScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(R.integer.config_screenBrightnessSettingMaximum);
    }

    @UnsupportedAppUsage
    public int getDefaultScreenBrightnessSetting() {
        return this.mContext.getResources().getInteger(R.integer.config_screenBrightnessSettingDefault);
    }

    public int getMinimumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(R.integer.config_screenBrightnessForVrSettingMinimum);
    }

    public int getMaximumScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(R.integer.config_screenBrightnessForVrSettingMaximum);
    }

    public int getDefaultScreenBrightnessForVrSetting() {
        return this.mContext.getResources().getInteger(R.integer.config_screenBrightnessForVrSettingDefault);
    }

    public WakeLock newWakeLock(int levelAndFlags, String tag) {
        validateWakeLockParameters(levelAndFlags, tag);
        return new WakeLock(levelAndFlags, tag, this.mContext.getOpPackageName());
    }

    @UnsupportedAppUsage
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
        userActivity(when, 0, noChangeLights ? 1 : 0);
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

    @UnsupportedAppUsage
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

    public void setModeToAutoNoClearOffsetEnable(boolean enable) {
        try {
            this.mService.setModeToAutoNoClearOffsetEnable(enable);
        } catch (RemoteException e) {
        }
    }

    @Deprecated
    public void wakeUp(long time) {
        wakeUp(time, 0, "wakeUp");
    }

    @UnsupportedAppUsage
    @Deprecated
    public void wakeUp(long time, String details) {
        wakeUp(time, 0, details);
    }

    public void wakeUp(long time, int reason, String details) {
        try {
            this.mService.wakeUp(time, reason, details, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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

    public void nap(long time) {
        try {
            this.mService.nap(time);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void dream(long time) {
        Sandman.startDreamByUserRequest(this.mContext);
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
            int i;
            int i2;
            int i3 = this.max;
            if (i3 < 0 || i3 > 255 || (i = this.min) < 0 || i > 255 || (i2 = this.level) < 0 || i2 > 255 || this.brightness < 0 || i >= i3) {
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

    @SystemApi
    public boolean setPowerSaveModeEnabled(boolean mode) {
        try {
            return this.mService.setPowerSaveModeEnabled(mode);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold) {
        try {
            return this.mService.setDynamicPowerSaveHint(powerSaveHint, disableThreshold);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig config) {
        try {
            return this.mService.setAdaptivePowerSavePolicy(config);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean setAdaptivePowerSaveEnabled(boolean enabled) {
        try {
            return this.mService.setAdaptivePowerSaveEnabled(enabled);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public int getPowerSaveModeTrigger() {
        try {
            return this.mService.getPowerSaveModeTrigger();
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
        if (!powerSaveState.batterySaverEnabled) {
            return 0;
        }
        return powerSaveState.locationMode;
    }

    public boolean isDeviceIdleMode() {
        try {
            return this.mService.isDeviceIdleMode();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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
                this.mIDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService(Context.DEVICE_IDLE_CONTROLLER));
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
        return this.mContext.getResources().getBoolean(R.bool.config_sustainedPerformanceModeSupported);
    }

    public int getCurrentThermalStatus() {
        int currentThermalStatus;
        synchronized (this) {
            if (this.mThermalService == null) {
                this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService(Context.THERMAL_SERVICE));
            }
            try {
                currentThermalStatus = this.mThermalService.getCurrentThermalStatus();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return currentThermalStatus;
    }

    public void addThermalStatusListener(OnThermalStatusChangedListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        synchronized (this) {
            if (this.mThermalService == null) {
                this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService(Context.THERMAL_SERVICE));
            }
            addThermalStatusListener(this.mContext.getMainExecutor(), listener);
        }
    }

    public void addThermalStatusListener(final Executor executor, final OnThermalStatusChangedListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        Preconditions.checkNotNull(executor, "executor cannot be null");
        synchronized (this) {
            if (this.mThermalService == null) {
                this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService(Context.THERMAL_SERVICE));
            }
            boolean z = !this.mListenerMap.containsKey(listener);
            Preconditions.checkArgument(z, "Listener already registered: " + listener);
            IThermalStatusListener internalListener = new IThermalStatusListener.Stub() {
                /* class android.os.PowerManager.AnonymousClass1 */

                @Override // android.os.IThermalStatusListener
                public void onStatusChange(int status) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        executor.execute(new Runnable(status) {
                            /* class android.os.$$Lambda$PowerManager$1$RL9hKNKSaGL1mmREjQCm9KuA */
                            private final /* synthetic */ int f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                PowerManager.OnThermalStatusChangedListener.this.onThermalStatusChanged(this.f$1);
                            }
                        });
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                }
            };
            try {
                if (this.mThermalService.registerThermalStatusListener(internalListener)) {
                    this.mListenerMap.put(listener, internalListener);
                } else {
                    throw new RuntimeException("Listener failed to set");
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void removeThermalStatusListener(OnThermalStatusChangedListener listener) {
        Preconditions.checkNotNull(listener, "listener cannot be null");
        synchronized (this) {
            if (this.mThermalService == null) {
                this.mThermalService = IThermalService.Stub.asInterface(ServiceManager.getService(Context.THERMAL_SERVICE));
            }
            IThermalStatusListener internalListener = this.mListenerMap.get(listener);
            Preconditions.checkArgument(internalListener != null, "Listener was not added");
            try {
                if (this.mThermalService.unregisterThermalStatusListener(internalListener)) {
                    this.mListenerMap.remove(listener);
                } else {
                    throw new RuntimeException("Listener failed to remove");
                }
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
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

    public int getLastSleepReason() {
        try {
            return this.mService.getLastSleepReason();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public boolean forceSuspend() {
        try {
            return this.mService.forceSuspend();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public final class WakeLock {
        private int mExternalCount;
        @UnsupportedAppUsage
        private int mFlags;
        private boolean mHeld;
        private String mHistoryTag;
        private int mInternalCount;
        private final String mPackageName;
        private boolean mRefCounted = true;
        private final Runnable mReleaser = new Runnable() {
            /* class android.os.PowerManager.WakeLock.AnonymousClass1 */

            public void run() {
                WakeLock.this.release(65536);
            }
        };
        @UnsupportedAppUsage
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
            this.mInternalCount++;
            this.mExternalCount++;
            if (!this.mRefCounted || this.mInternalCount == 1) {
                PowerManager.this.mHandler.removeCallbacks(this.mReleaser);
                Trace.asyncTraceBegin(131072, this.mTraceName, 0);
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
                        Trace.asyncTraceEnd(131072, this.mTraceName, 0);
                        try {
                            PowerManager.this.mService.releaseWakeLock(this.mToken, flags);
                            this.mHeld = false;
                        } catch (RemoteException e) {
                            throw e.rethrowFromSystemServer();
                        }
                    }
                }
                if (this.mRefCounted && this.mExternalCount < 0) {
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
                if (ws != null) {
                    try {
                        if (ws.isEmpty()) {
                            ws = null;
                        }
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
                /* class android.os.$$Lambda$PowerManager$WakeLock$VvFzmRZ4ZGlXx7u3lSAJ_TYUjw */
                private final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PowerManager.WakeLock.this.lambda$wrap$0$PowerManager$WakeLock(this.f$1);
                }
            };
        }

        public /* synthetic */ void lambda$wrap$0$PowerManager$WakeLock(Runnable r) {
            try {
                r.run();
            } finally {
                release();
            }
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
}

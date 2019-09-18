package com.android.server;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.MutableBoolean;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.wm.WindowManagerInternal;

public class GestureLauncherService extends SystemService {
    @VisibleForTesting
    static final long CAMERA_POWER_DOUBLE_TAP_MAX_TIME_MS = 300;
    private static final boolean DBG = false;
    private static final boolean DBG_CAMERA_LIFT = false;
    @VisibleForTesting
    static final long POWER_SHORT_TAP_SEQUENCE_MAX_INTERVAL_MS = 500;
    private static final String TAG = "GestureLauncherService";
    private boolean mCameraDoubleTapPowerEnabled;
    /* access modifiers changed from: private */
    public long mCameraGestureLastEventTime;
    /* access modifiers changed from: private */
    public long mCameraGestureOnTimeMs;
    /* access modifiers changed from: private */
    public long mCameraGestureSensor1LastOnTimeMs;
    /* access modifiers changed from: private */
    public long mCameraGestureSensor2LastOnTimeMs;
    /* access modifiers changed from: private */
    public int mCameraLaunchLastEventExtra;
    /* access modifiers changed from: private */
    public boolean mCameraLaunchRegistered;
    /* access modifiers changed from: private */
    public Sensor mCameraLaunchSensor;
    /* access modifiers changed from: private */
    public boolean mCameraLiftRegistered;
    /* access modifiers changed from: private */
    public final CameraLiftTriggerEventListener mCameraLiftTriggerListener;
    /* access modifiers changed from: private */
    public Sensor mCameraLiftTriggerSensor;
    /* access modifiers changed from: private */
    public Context mContext;
    private final GestureEventListener mGestureListener;
    private long mLastPowerDown;
    /* access modifiers changed from: private */
    public final MetricsLogger mMetricsLogger;
    private int mPowerButtonConsecutiveTaps;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public final ContentObserver mSettingObserver;
    /* access modifiers changed from: private */
    public int mUserId;
    private final BroadcastReceiver mUserReceiver;
    private PowerManager.WakeLock mWakeLock;
    /* access modifiers changed from: private */
    public WindowManagerInternal mWindowManagerInternal;

    private final class CameraLiftTriggerEventListener extends TriggerEventListener {
        private CameraLiftTriggerEventListener() {
        }

        public void onTrigger(TriggerEvent event) {
            if (GestureLauncherService.this.mCameraLiftRegistered && event.sensor == GestureLauncherService.this.mCameraLiftTriggerSensor) {
                Resources resources = GestureLauncherService.this.mContext.getResources();
                SensorManager sensorManager = (SensorManager) GestureLauncherService.this.mContext.getSystemService("sensor");
                boolean keyguardShowingAndNotOccluded = GestureLauncherService.this.mWindowManagerInternal.isKeyguardShowingAndNotOccluded();
                boolean interactive = GestureLauncherService.this.mPowerManager.isInteractive();
                if ((keyguardShowingAndNotOccluded || !interactive) && GestureLauncherService.this.handleCameraGesture(true, 2)) {
                    MetricsLogger.action(GestureLauncherService.this.mContext, 989);
                }
                boolean unused = GestureLauncherService.this.mCameraLiftRegistered = sensorManager.requestTriggerSensor(GestureLauncherService.this.mCameraLiftTriggerListener, GestureLauncherService.this.mCameraLiftTriggerSensor);
            }
        }
    }

    private final class GestureEventListener implements SensorEventListener {
        private GestureEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            if (GestureLauncherService.this.mCameraLaunchRegistered && event.sensor == GestureLauncherService.this.mCameraLaunchSensor && GestureLauncherService.this.handleCameraGesture(true, 0)) {
                GestureLauncherService.this.mMetricsLogger.action(256);
                trackCameraLaunchEvent(event);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        private void trackCameraLaunchEvent(SensorEvent event) {
            long now = SystemClock.elapsedRealtime();
            long totalDuration = now - GestureLauncherService.this.mCameraGestureOnTimeMs;
            float[] values = event.values;
            long sensor1OnTime = (long) (((double) totalDuration) * ((double) values[0]));
            long sensor2OnTime = (long) (((double) totalDuration) * ((double) values[1]));
            int extra = (int) values[2];
            long gestureOnTimeDiff = now - GestureLauncherService.this.mCameraGestureLastEventTime;
            long sensor1OnTimeDiff = sensor1OnTime - GestureLauncherService.this.mCameraGestureSensor1LastOnTimeMs;
            long sensor2OnTimeDiff = sensor2OnTime - GestureLauncherService.this.mCameraGestureSensor2LastOnTimeMs;
            int extraDiff = extra - GestureLauncherService.this.mCameraLaunchLastEventExtra;
            if (gestureOnTimeDiff >= 0 && sensor1OnTimeDiff >= 0 && sensor2OnTimeDiff >= 0) {
                EventLogTags.writeCameraGestureTriggered(gestureOnTimeDiff, sensor1OnTimeDiff, sensor2OnTimeDiff, extraDiff);
                long unused = GestureLauncherService.this.mCameraGestureLastEventTime = now;
                long unused2 = GestureLauncherService.this.mCameraGestureSensor1LastOnTimeMs = sensor1OnTime;
                long unused3 = GestureLauncherService.this.mCameraGestureSensor2LastOnTimeMs = sensor2OnTime;
                int unused4 = GestureLauncherService.this.mCameraLaunchLastEventExtra = extra;
            }
        }
    }

    public GestureLauncherService(Context context) {
        this(context, new MetricsLogger());
    }

    @VisibleForTesting
    GestureLauncherService(Context context, MetricsLogger metricsLogger) {
        super(context);
        this.mGestureListener = new GestureEventListener();
        this.mCameraLiftTriggerListener = new CameraLiftTriggerEventListener();
        this.mCameraGestureOnTimeMs = 0;
        this.mCameraGestureLastEventTime = 0;
        this.mCameraGestureSensor1LastOnTimeMs = 0;
        this.mCameraGestureSensor2LastOnTimeMs = 0;
        this.mCameraLaunchLastEventExtra = 0;
        this.mUserReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    int unused = GestureLauncherService.this.mUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    GestureLauncherService.this.mContext.getContentResolver().unregisterContentObserver(GestureLauncherService.this.mSettingObserver);
                    GestureLauncherService.this.registerContentObservers();
                    GestureLauncherService.this.updateCameraRegistered();
                    GestureLauncherService.this.updateCameraDoubleTapPowerEnabled();
                }
            }
        };
        this.mSettingObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (userId == GestureLauncherService.this.mUserId) {
                    GestureLauncherService.this.updateCameraRegistered();
                    GestureLauncherService.this.updateCameraDoubleTapPowerEnabled();
                }
            }
        };
        this.mContext = context;
        this.mMetricsLogger = metricsLogger;
    }

    public void onStart() {
        LocalServices.addService(GestureLauncherService.class, this);
    }

    public void onBootPhase(int phase) {
        if (phase == 600 && isGestureLauncherEnabled(this.mContext.getResources())) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            this.mWakeLock = this.mPowerManager.newWakeLock(1, TAG);
            updateCameraRegistered();
            updateCameraDoubleTapPowerEnabled();
            this.mUserId = ActivityManager.getCurrentUser();
            this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
            registerContentObservers();
        }
    }

    /* access modifiers changed from: private */
    public void registerContentObservers() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("camera_gesture_disabled"), false, this.mSettingObserver, this.mUserId);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("camera_double_tap_power_gesture_disabled"), false, this.mSettingObserver, this.mUserId);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("camera_lift_trigger_enabled"), false, this.mSettingObserver, this.mUserId);
    }

    /* access modifiers changed from: private */
    public void updateCameraRegistered() {
        Resources resources = this.mContext.getResources();
        if (isCameraLaunchSettingEnabled(this.mContext, this.mUserId)) {
            registerCameraLaunchGesture(resources);
        } else {
            unregisterCameraLaunchGesture();
        }
        if (isCameraLiftTriggerSettingEnabled(this.mContext, this.mUserId)) {
            registerCameraLiftTrigger(resources);
        } else {
            unregisterCameraLiftTrigger();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateCameraDoubleTapPowerEnabled() {
        boolean enabled = isCameraDoubleTapPowerSettingEnabled(this.mContext, this.mUserId);
        synchronized (this) {
            this.mCameraDoubleTapPowerEnabled = enabled;
        }
    }

    private void unregisterCameraLaunchGesture() {
        if (this.mCameraLaunchRegistered) {
            this.mCameraLaunchRegistered = false;
            this.mCameraGestureOnTimeMs = 0;
            this.mCameraGestureLastEventTime = 0;
            this.mCameraGestureSensor1LastOnTimeMs = 0;
            this.mCameraGestureSensor2LastOnTimeMs = 0;
            this.mCameraLaunchLastEventExtra = 0;
            ((SensorManager) this.mContext.getSystemService("sensor")).unregisterListener(this.mGestureListener);
        }
    }

    private void registerCameraLaunchGesture(Resources resources) {
        if (!this.mCameraLaunchRegistered) {
            this.mCameraGestureOnTimeMs = SystemClock.elapsedRealtime();
            this.mCameraGestureLastEventTime = this.mCameraGestureOnTimeMs;
            SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            int cameraLaunchGestureId = resources.getInteger(17694752);
            if (cameraLaunchGestureId != -1) {
                this.mCameraLaunchRegistered = false;
                String sensorName = resources.getString(17039769);
                this.mCameraLaunchSensor = sensorManager.getDefaultSensor(cameraLaunchGestureId, true);
                if (this.mCameraLaunchSensor != null) {
                    if (sensorName.equals(this.mCameraLaunchSensor.getStringType())) {
                        this.mCameraLaunchRegistered = sensorManager.registerListener(this.mGestureListener, this.mCameraLaunchSensor, 0);
                    } else {
                        throw new RuntimeException(String.format("Wrong configuration. Sensor type and sensor string type don't match: %s in resources, %s in the sensor.", new Object[]{sensorName, this.mCameraLaunchSensor.getStringType()}));
                    }
                }
            }
        }
    }

    private void unregisterCameraLiftTrigger() {
        if (this.mCameraLiftRegistered) {
            this.mCameraLiftRegistered = false;
            ((SensorManager) this.mContext.getSystemService("sensor")).cancelTriggerSensor(this.mCameraLiftTriggerListener, this.mCameraLiftTriggerSensor);
        }
    }

    private void registerCameraLiftTrigger(Resources resources) {
        if (!this.mCameraLiftRegistered) {
            SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            int cameraLiftTriggerId = resources.getInteger(17694753);
            if (cameraLiftTriggerId != -1) {
                this.mCameraLiftRegistered = false;
                String sensorName = resources.getString(17039770);
                this.mCameraLiftTriggerSensor = sensorManager.getDefaultSensor(cameraLiftTriggerId, true);
                if (this.mCameraLiftTriggerSensor != null) {
                    if (sensorName.equals(this.mCameraLiftTriggerSensor.getStringType())) {
                        this.mCameraLiftRegistered = sensorManager.requestTriggerSensor(this.mCameraLiftTriggerListener, this.mCameraLiftTriggerSensor);
                    } else {
                        throw new RuntimeException(String.format("Wrong configuration. Sensor type and sensor string type don't match: %s in resources, %s in the sensor.", new Object[]{sensorName, this.mCameraLiftTriggerSensor.getStringType()}));
                    }
                }
            }
        }
    }

    public static boolean isCameraLaunchSettingEnabled(Context context, int userId) {
        if (!isCameraLaunchEnabled(context.getResources()) || Settings.Secure.getIntForUser(context.getContentResolver(), "camera_gesture_disabled", 0, userId) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isCameraDoubleTapPowerSettingEnabled(Context context, int userId) {
        if (!isCameraDoubleTapPowerEnabled(context.getResources()) || Settings.Secure.getIntForUser(context.getContentResolver(), "camera_double_tap_power_gesture_disabled", 0, userId) != 0) {
            return false;
        }
        return true;
    }

    public static boolean isCameraLiftTriggerSettingEnabled(Context context, int userId) {
        if (!isCameraLiftTriggerEnabled(context.getResources()) || Settings.Secure.getIntForUser(context.getContentResolver(), "camera_lift_trigger_enabled", 1, userId) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isCameraLaunchEnabled(Resources resources) {
        if (!(resources.getInteger(17694752) != -1) || SystemProperties.getBoolean("gesture.disable_camera_launch", false)) {
            return false;
        }
        return true;
    }

    public static boolean isCameraDoubleTapPowerEnabled(Resources resources) {
        return resources.getBoolean(17956908);
    }

    public static boolean isCameraLiftTriggerEnabled(Resources resources) {
        return resources.getInteger(17694753) != -1;
    }

    public static boolean isGestureLauncherEnabled(Resources resources) {
        return isCameraLaunchEnabled(resources) || isCameraDoubleTapPowerEnabled(resources) || isCameraLiftTriggerEnabled(resources);
    }

    public boolean interceptPowerKeyDown(KeyEvent event, boolean interactive, MutableBoolean outLaunched) {
        long powerTapInterval;
        boolean launched = false;
        boolean intercept = false;
        synchronized (this) {
            powerTapInterval = event.getEventTime() - this.mLastPowerDown;
            if (this.mCameraDoubleTapPowerEnabled && powerTapInterval < CAMERA_POWER_DOUBLE_TAP_MAX_TIME_MS) {
                launched = true;
                intercept = interactive;
                this.mPowerButtonConsecutiveTaps++;
            } else if (powerTapInterval < 500) {
                this.mPowerButtonConsecutiveTaps++;
            } else {
                this.mPowerButtonConsecutiveTaps = 1;
            }
            this.mLastPowerDown = event.getEventTime();
        }
        if (launched) {
            Slog.i(TAG, "Power button double tap gesture detected, launching camera. Interval=" + powerTapInterval + "ms");
            launched = handleCameraGesture(false, 1);
            if (launched) {
                this.mMetricsLogger.action(255, (int) powerTapInterval);
            }
        }
        this.mMetricsLogger.histogram("power_consecutive_short_tap_count", this.mPowerButtonConsecutiveTaps);
        this.mMetricsLogger.histogram("power_double_tap_interval", (int) powerTapInterval);
        outLaunched.value = launched;
        if (!intercept || !launched) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean handleCameraGesture(boolean useWakelock, int source) {
        if (!(Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0)) {
            return false;
        }
        if (useWakelock) {
            this.mWakeLock.acquire(500);
        }
        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).onCameraLaunchGestureDetected(source);
        return true;
    }
}

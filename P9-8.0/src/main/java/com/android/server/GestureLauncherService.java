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
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.MutableBoolean;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.internal.logging.MetricsLogger;
import com.android.server.statusbar.StatusBarManagerInternal;

public class GestureLauncherService extends SystemService {
    private static final long CAMERA_POWER_DOUBLE_TAP_MAX_TIME_MS = 300;
    private static final boolean DBG = false;
    private static final String TAG = "GestureLauncherService";
    private boolean mCameraDoubleTapPowerEnabled;
    private long mCameraGestureLastEventTime = 0;
    private long mCameraGestureOnTimeMs = 0;
    private long mCameraGestureSensor1LastOnTimeMs = 0;
    private long mCameraGestureSensor2LastOnTimeMs = 0;
    private int mCameraLaunchLastEventExtra = 0;
    private Sensor mCameraLaunchSensor;
    private Context mContext;
    private final GestureEventListener mGestureListener = new GestureEventListener(this, null);
    private long mLastPowerDown;
    private boolean mRegistered;
    private final ContentObserver mSettingObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (userId == GestureLauncherService.this.mUserId) {
                GestureLauncherService.this.updateCameraRegistered();
                GestureLauncherService.this.updateCameraDoubleTapPowerEnabled();
            }
        }
    };
    private int mUserId;
    private final BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                GestureLauncherService.this.mUserId = intent.getIntExtra("android.intent.extra.user_handle", 0);
                GestureLauncherService.this.mContext.getContentResolver().unregisterContentObserver(GestureLauncherService.this.mSettingObserver);
                GestureLauncherService.this.registerContentObservers();
                GestureLauncherService.this.updateCameraRegistered();
                GestureLauncherService.this.updateCameraDoubleTapPowerEnabled();
            }
        }
    };
    private WakeLock mWakeLock;

    private final class GestureEventListener implements SensorEventListener {
        /* synthetic */ GestureEventListener(GestureLauncherService this$0, GestureEventListener -this1) {
            this();
        }

        private GestureEventListener() {
        }

        public void onSensorChanged(SensorEvent event) {
            if (GestureLauncherService.this.mRegistered && event.sensor == GestureLauncherService.this.mCameraLaunchSensor && GestureLauncherService.this.handleCameraLaunchGesture(true, 0)) {
                MetricsLogger.action(GestureLauncherService.this.mContext, 256);
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
                GestureLauncherService.this.mCameraGestureLastEventTime = now;
                GestureLauncherService.this.mCameraGestureSensor1LastOnTimeMs = sensor1OnTime;
                GestureLauncherService.this.mCameraGestureSensor2LastOnTimeMs = sensor2OnTime;
                GestureLauncherService.this.mCameraLaunchLastEventExtra = extra;
            }
        }
    }

    public GestureLauncherService(Context context) {
        super(context);
        this.mContext = context;
    }

    public void onStart() {
        LocalServices.addService(GestureLauncherService.class, this);
    }

    public void onBootPhase(int phase) {
        if (phase == 600 && isGestureLauncherEnabled(this.mContext.getResources())) {
            this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, TAG);
            updateCameraRegistered();
            updateCameraDoubleTapPowerEnabled();
            this.mUserId = ActivityManager.getCurrentUser();
            this.mContext.registerReceiver(this.mUserReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"));
            registerContentObservers();
        }
    }

    private void registerContentObservers() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("camera_gesture_disabled"), false, this.mSettingObserver, this.mUserId);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("camera_double_tap_power_gesture_disabled"), false, this.mSettingObserver, this.mUserId);
    }

    private void updateCameraRegistered() {
        Resources resources = this.mContext.getResources();
        if (isCameraLaunchSettingEnabled(this.mContext, this.mUserId)) {
            registerCameraLaunchGesture(resources);
        } else {
            unregisterCameraLaunchGesture();
        }
    }

    private void updateCameraDoubleTapPowerEnabled() {
        boolean enabled = isCameraDoubleTapPowerSettingEnabled(this.mContext, this.mUserId);
        synchronized (this) {
            this.mCameraDoubleTapPowerEnabled = enabled;
        }
    }

    private void unregisterCameraLaunchGesture() {
        if (this.mRegistered) {
            this.mRegistered = false;
            this.mCameraGestureOnTimeMs = 0;
            this.mCameraGestureLastEventTime = 0;
            this.mCameraGestureSensor1LastOnTimeMs = 0;
            this.mCameraGestureSensor2LastOnTimeMs = 0;
            this.mCameraLaunchLastEventExtra = 0;
            ((SensorManager) this.mContext.getSystemService("sensor")).unregisterListener(this.mGestureListener);
        }
    }

    private void registerCameraLaunchGesture(Resources resources) {
        if (!this.mRegistered) {
            this.mCameraGestureOnTimeMs = SystemClock.elapsedRealtime();
            this.mCameraGestureLastEventTime = this.mCameraGestureOnTimeMs;
            SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
            int cameraLaunchGestureId = resources.getInteger(17694754);
            if (cameraLaunchGestureId != -1) {
                this.mRegistered = false;
                String sensorName = resources.getString(17039753);
                this.mCameraLaunchSensor = sensorManager.getDefaultSensor(cameraLaunchGestureId, true);
                if (this.mCameraLaunchSensor != null) {
                    if (sensorName.equals(this.mCameraLaunchSensor.getStringType())) {
                        this.mRegistered = sensorManager.registerListener(this.mGestureListener, this.mCameraLaunchSensor, 0);
                    } else {
                        throw new RuntimeException(String.format("Wrong configuration. Sensor type and sensor string type don't match: %s in resources, %s in the sensor.", new Object[]{sensorName, this.mCameraLaunchSensor.getStringType()}));
                    }
                }
            }
        }
    }

    public static boolean isCameraLaunchSettingEnabled(Context context, int userId) {
        if (isCameraLaunchEnabled(context.getResources()) && Secure.getIntForUser(context.getContentResolver(), "camera_gesture_disabled", 0, userId) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isCameraDoubleTapPowerSettingEnabled(Context context, int userId) {
        if (isCameraDoubleTapPowerEnabled(context.getResources()) && Secure.getIntForUser(context.getContentResolver(), "camera_double_tap_power_gesture_disabled", 0, userId) == 0) {
            return true;
        }
        return false;
    }

    public static boolean isCameraLaunchEnabled(Resources resources) {
        if (resources.getInteger(17694754) != -1) {
            return SystemProperties.getBoolean("gesture.disable_camera_launch", false) ^ 1;
        }
        return false;
    }

    public static boolean isCameraDoubleTapPowerEnabled(Resources resources) {
        return resources.getBoolean(17956906);
    }

    public static boolean isGestureLauncherEnabled(Resources resources) {
        return !isCameraLaunchEnabled(resources) ? isCameraDoubleTapPowerEnabled(resources) : true;
    }

    public boolean interceptPowerKeyDown(KeyEvent event, boolean interactive, MutableBoolean outLaunched) {
        long doubleTapInterval;
        boolean launched = false;
        boolean intercept = false;
        synchronized (this) {
            doubleTapInterval = event.getEventTime() - this.mLastPowerDown;
            if (this.mCameraDoubleTapPowerEnabled && doubleTapInterval < CAMERA_POWER_DOUBLE_TAP_MAX_TIME_MS) {
                launched = true;
                intercept = interactive;
            }
            this.mLastPowerDown = event.getEventTime();
        }
        if (launched) {
            Slog.i(TAG, "Power button double tap gesture detected, launching camera. Interval=" + doubleTapInterval + "ms");
            launched = handleCameraLaunchGesture(false, 1);
            if (launched) {
                MetricsLogger.action(this.mContext, 255, (int) doubleTapInterval);
            }
        }
        MetricsLogger.histogram(this.mContext, "power_double_tap_interval", (int) doubleTapInterval);
        outLaunched.value = launched;
        return intercept ? launched : false;
    }

    private boolean handleCameraLaunchGesture(boolean useWakelock, int source) {
        if (!(Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0)) {
            return false;
        }
        if (useWakelock) {
            this.mWakeLock.acquire(500);
        }
        ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).onCameraLaunchGestureDetected(source);
        return true;
    }
}

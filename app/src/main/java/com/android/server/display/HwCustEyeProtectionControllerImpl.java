package com.android.server.display;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IPowerManager.Stub;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.Slog;
import com.android.server.input.HwCustInputManagerServiceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HwCustEyeProtectionControllerImpl extends HwCustEyeProtectionController {
    private static final String COLOR_TEMPERATURE = "color_temperature";
    private static final int COLOR_TEMPERATURE_CLOUDY = 127;
    private static final int COLOR_TEMPERATURE_DEFAULT = 128;
    private static final int COLOR_TEMPERATURE_INDOOR = 64;
    private static String COLOR_TEMPERATURE_MODE = null;
    private static int COLOR_TEMPERATURE_MODE_DEFAULT = 0;
    private static int COLOR_TEMPERATURE_MODE_MANUAL = 0;
    private static final int COLOR_TEMPERATURE_NIGHT = 0;
    private static final String COLOR_TEMPERATURE_RGB = "color_temperature_rgb";
    private static final int COLOR_TEMPERATURE_SUNNY = 191;
    private static final boolean DEBUG = false;
    private static final int EYE_PROTECTIION_MODE;
    private static final int EYE_PROTECTIION_OFF = 0;
    private static final int EYE_PROTECTIION_OFF_FROM_SUPER_POWERMODE = 2;
    private static final int EYE_PROTECTIION_ON = 1;
    private static final String HW_NOTIFICATION_BACKGROUND_INDEX = "huawei.notification.backgroundIndex";
    private static final String HW_NOTIFICATION_CONTENT_ICON = "huawei.notification.contentIcon";
    private static final String HW_NOTIFICATION_REPLACE_ICONID = "huawei.notification.replace.iconId";
    private static final String HW_NOTIFICATION_REPLACE_LOCATION = "huawei.notification.replace.location";
    private static final String KEY_EYES_PROTECTION = "eyes_protection_mode";
    private static final float[] LuxLevel;
    private static final int MAXINUM_TEMPERATURE = 255;
    private static final int MODE_BACKLIGHT = 2;
    private static final int MODE_BLUE_LIGHT = 1;
    private static final int MODE_COLOR_TEMPERATURE = 4;
    private static final int MODE_COLOR_TEMP_3_DIMENSION = 1;
    private static final int MODE_DEFAULT = 0;
    private static final int MSG_SET_COLOR_TEMPERATURE = 0;
    private static final int MSG_SET_FILTER_BLUE_LIGHT = 1;
    private static final int MSG_UPDATE_BACKLIGHT = 2;
    private static final String TAG = "EyeProtectionController";
    private static final int VALUE_ANIMATION_MAX_TIMES = 20;
    private static final int VALUE_ANIMATION_MSG_DELAYED = 40;
    private static final int VALUE_BLUELIGHT_FILTER_DEFAULT = 0;
    private static final int VALUE_BLUELIGHT_FILTER_REAL = 30;
    private static boolean mLoadLibraryFailed;
    private final String ACTION_SUPER_POWERMODE;
    private float AMBIRNT_LUX_THRESHOLD;
    private float blue;
    private float green;
    private float mAmbientLux;
    private int mBluelightAnimationTarget;
    private int mBluelightAnimationTimes;
    private int mBluelightBeforeAnimation;
    private int mColorBeforeAnimation;
    private int mColorTemperatureTarget;
    private int mColorTemperatureTimes;
    private int mCurrentColorTemperature;
    private int mCurrentFilterValue;
    private int mCurrentUserId;
    private int mEyesProtectionMode;
    private SmartDisplayHandler mHandler;
    private HandlerThread mHandlerThread;
    private Sensor mLightSensor;
    private final SensorEventListener mLightSensorListener;
    private final int mLightSensorRate;
    private NotificationManager mNotificationManager;
    private boolean mNotificationShown;
    private ContentObserver mProtectionModeObserver;
    private ScreenStateReceiver mScreenStateReceiver;
    private SensorManager mSensorManager;
    private boolean mfirstSceneSwitchOn;
    private float red;

    /* renamed from: com.android.server.display.HwCustEyeProtectionControllerImpl.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode = System.getIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT, -2);
            Slog.i(HwCustEyeProtectionControllerImpl.TAG, "Eyes-Protect mode in Settings changed, mEyesProtectionMode =" + HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode + ", user =" + -2);
            if (HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode == HwCustEyeProtectionControllerImpl.MSG_SET_FILTER_BLUE_LIGHT) {
                HwCustEyeProtectionControllerImpl.this.registerLightSensor();
            } else {
                HwCustEyeProtectionControllerImpl.this.unregisterLightSensor();
            }
            HwCustEyeProtectionControllerImpl.this.updateGlobalSceneState();
        }
    }

    private class ScreenStateReceiver extends BroadcastReceiver {
        public ScreenStateReceiver() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(HwCustInputManagerServiceImpl.ACTION_USER_SWITCHED);
            HwCustEyeProtectionControllerImpl.this.mContext.registerReceiver(this, filter);
            filter.addAction("huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION");
            HwCustEyeProtectionControllerImpl.this.mContext.registerReceiver(this, filter);
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            filter.setPriority(1000);
            HwCustEyeProtectionControllerImpl.this.mContext.registerReceiver(this, filter);
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                    HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode = System.getIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT, -2);
                    if (HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode == HwCustEyeProtectionControllerImpl.MSG_SET_FILTER_BLUE_LIGHT) {
                        HwCustEyeProtectionControllerImpl.this.registerLightSensor();
                        HwCustEyeProtectionControllerImpl.this.updateGlobalSceneState();
                    }
                    if (HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode == HwCustEyeProtectionControllerImpl.MSG_UPDATE_BACKLIGHT) {
                        System.putIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.MSG_SET_FILTER_BLUE_LIGHT, -2);
                    }
                }
                if ("huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION".equals(intent.getAction())) {
                    if (HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode == HwCustEyeProtectionControllerImpl.MSG_SET_FILTER_BLUE_LIGHT && intent.getBooleanExtra("enable", HwCustEyeProtectionControllerImpl.DEBUG)) {
                        System.putIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.MSG_UPDATE_BACKLIGHT, -2);
                    } else {
                        HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode = System.getIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT, -2);
                        if (HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode == HwCustEyeProtectionControllerImpl.MSG_UPDATE_BACKLIGHT && !intent.getBooleanExtra("enable", HwCustEyeProtectionControllerImpl.DEBUG)) {
                            System.putIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.MSG_SET_FILTER_BLUE_LIGHT, -2);
                        }
                    }
                }
                if (HwCustInputManagerServiceImpl.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                    HwCustEyeProtectionControllerImpl.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT);
                    HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode = System.getIntForUser(HwCustEyeProtectionControllerImpl.this.mContext.getContentResolver(), HwCustEyeProtectionControllerImpl.KEY_EYES_PROTECTION, HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT, HwCustEyeProtectionControllerImpl.this.mCurrentUserId);
                    HwCustEyeProtectionControllerImpl.this.updateGlobalSceneState();
                }
                Slog.i(HwCustEyeProtectionControllerImpl.TAG, "onReceive intent action = " + intent.getAction() + ", mEyesProtectionMode =" + HwCustEyeProtectionControllerImpl.this.mEyesProtectionMode + ", user =" + HwCustEyeProtectionControllerImpl.this.mCurrentUserId);
            }
        }
    }

    private final class SmartDisplayHandler extends Handler {
        public SmartDisplayHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT /*0*/:
                    HwCustEyeProtectionControllerImpl.this.colorTemperatureAnimationTo(HwCustEyeProtectionControllerImpl.this.mColorTemperatureTarget, HwCustEyeProtectionControllerImpl.VALUE_ANIMATION_MSG_DELAYED);
                case HwCustEyeProtectionControllerImpl.MSG_SET_FILTER_BLUE_LIGHT /*1*/:
                    HwCustEyeProtectionControllerImpl.this.blueLightAnimationTo(HwCustEyeProtectionControllerImpl.this.mBluelightAnimationTarget, HwCustEyeProtectionControllerImpl.VALUE_ANIMATION_MSG_DELAYED);
                case HwCustEyeProtectionControllerImpl.MSG_UPDATE_BACKLIGHT /*2*/:
                    if (HwCustEyeProtectionControllerImpl.this.mAutomaticBrightnessController != null) {
                        HwCustEyeProtectionControllerImpl.this.mAutomaticBrightnessController.updateAutoBrightness(true);
                        Slog.i(HwCustEyeProtectionControllerImpl.TAG, "updateAutoBrightness.");
                    }
                default:
                    Slog.e(HwCustEyeProtectionControllerImpl.TAG, "Invalid message");
            }
        }
    }

    private static native void finalize_native();

    private static native void init_native();

    protected native int nativeFilterBlueLight(int i);

    protected native int nativeGetDisplayFeatureSupported(int i);

    protected native int nativeSetColorTemperatureNew(int i);

    static {
        EYE_PROTECTIION_MODE = SystemProperties.getInt("ro.config.hw_eyes_protection", VALUE_BLUELIGHT_FILTER_DEFAULT);
        LuxLevel = new float[]{0.0f, 100.0f, 1000.0f, 3000.0f};
        mLoadLibraryFailed = DEBUG;
        try {
            System.loadLibrary("custeyeprotection_jni");
            Slog.d(TAG, "libcusteyeprotection_jni library load!");
        } catch (UnsatisfiedLinkError e) {
            mLoadLibraryFailed = true;
            Slog.d(TAG, "libcusteyeprotection_jni library not found!");
        }
        COLOR_TEMPERATURE_MODE = "color_temperature_mode";
        COLOR_TEMPERATURE_MODE_MANUAL = VALUE_BLUELIGHT_FILTER_DEFAULT;
        COLOR_TEMPERATURE_MODE_DEFAULT = MSG_SET_FILTER_BLUE_LIGHT;
    }

    protected void finalize() {
        if (!mLoadLibraryFailed) {
            finalize_native();
        }
        try {
            super.finalize();
        } catch (Throwable th) {
        }
    }

    private void registerLightSensor() {
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, 300000, null);
    }

    private void unregisterLightSensor() {
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
        }
    }

    private void handleLightSensorEvent(long time, float lux) {
        if (this.mfirstSceneSwitchOn && this.mEyesProtectionMode == MSG_SET_FILTER_BLUE_LIGHT) {
            this.mAmbientLux = lux;
            updateColorTemperature();
            this.mfirstSceneSwitchOn = DEBUG;
        } else if (this.mEyesProtectionMode != MSG_SET_FILTER_BLUE_LIGHT) {
        } else {
            if (lux > this.mAmbientLux * (this.AMBIRNT_LUX_THRESHOLD + 1.0f) || lux < this.mAmbientLux * (1.0f - this.AMBIRNT_LUX_THRESHOLD)) {
                this.mAmbientLux = lux;
                updateColorTemperature();
            }
        }
    }

    private void updateColorTemperature() {
        if (isFunctionExist(MODE_COLOR_TEMPERATURE)) {
            if (this.mAmbientLux >= LuxLevel[VALUE_BLUELIGHT_FILTER_DEFAULT] && this.mAmbientLux < LuxLevel[MSG_SET_FILTER_BLUE_LIGHT]) {
                this.mColorTemperatureTarget = VALUE_BLUELIGHT_FILTER_DEFAULT;
            }
            if (this.mAmbientLux >= LuxLevel[MSG_SET_FILTER_BLUE_LIGHT] && this.mAmbientLux < LuxLevel[MSG_UPDATE_BACKLIGHT]) {
                this.mColorTemperatureTarget = COLOR_TEMPERATURE_INDOOR;
            }
            if (this.mAmbientLux < 0.0f || (this.mAmbientLux >= LuxLevel[MSG_UPDATE_BACKLIGHT] && this.mAmbientLux < LuxLevel[3])) {
                this.mColorTemperatureTarget = COLOR_TEMPERATURE_CLOUDY;
            }
            if (this.mAmbientLux >= LuxLevel[3]) {
                this.mColorTemperatureTarget = COLOR_TEMPERATURE_SUNNY;
            }
            if (this.mColorTemperatureTarget != this.mCurrentColorTemperature) {
                this.mColorTemperatureTimes = MSG_SET_FILTER_BLUE_LIGHT;
                this.mColorBeforeAnimation = this.mCurrentColorTemperature;
                colorTemperatureAnimationTo(this.mColorTemperatureTarget, VALUE_ANIMATION_MSG_DELAYED);
                Slog.i(TAG, "updateColorTemperature mAmbientLux = " + this.mAmbientLux + ", target =" + this.mColorTemperatureTarget);
            }
        }
    }

    private void setColorTemperatureAccordingToSetting() {
        Slog.d(TAG, "setColorTemperatureAccordingToSetting");
        int operation;
        if (isDisplayFeatureSupported(MSG_SET_FILTER_BLUE_LIGHT)) {
            Slog.d(TAG, "setColorTemperatureAccordingToSetting new.");
            try {
                String ctNewRGB = System.getStringForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE_RGB, -2);
                if (ctNewRGB != null) {
                    List<String> rgbarryList = new ArrayList(Arrays.asList(ctNewRGB.split(",")));
                    float red = Float.valueOf((String) rgbarryList.get(VALUE_BLUELIGHT_FILTER_DEFAULT)).floatValue();
                    float green = Float.valueOf((String) rgbarryList.get(MSG_SET_FILTER_BLUE_LIGHT)).floatValue();
                    float blue = Float.valueOf((String) rgbarryList.get(MSG_UPDATE_BACKLIGHT)).floatValue();
                    Slog.d(TAG, "ColorTemperature read from setting:" + ctNewRGB + red + green + blue);
                    updateRgbGamma(red, green, blue);
                } else {
                    operation = System.getIntForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE, COLOR_TEMPERATURE_DEFAULT, -2);
                    Slog.d(TAG, "ColorTemperature read from old setting:" + operation);
                    setColorTemperature(operation);
                }
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "ColorTemperature read from setting exception!");
                updateRgbGamma(1.0f, 1.0f, 1.0f);
            }
        } else {
            operation = System.getIntForUser(this.mContext.getContentResolver(), COLOR_TEMPERATURE, COLOR_TEMPERATURE_DEFAULT, -2);
            Slog.d(TAG, "setColorTemperatureAccordingToSetting old:" + operation);
            setColorTemperature(operation);
        }
    }

    public boolean isDisplayFeatureSupported(int feature) {
        boolean z = DEBUG;
        Slog.d(TAG, "isDisplayFeatureSupported feature:" + feature);
        try {
            if (mLoadLibraryFailed) {
                Slog.d(TAG, "Display feature not supported because of library not found!");
                return DEBUG;
            }
            if (nativeGetDisplayFeatureSupported(feature) != 0) {
                z = true;
            }
            return z;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "Display feature not supported because of exception!");
            return DEBUG;
        }
    }

    private int updateRgbGamma(float red, float green, float blue) {
        Slog.d(TAG, "updateRgbGamma:red=" + red + " green=" + green + " blue=" + blue);
        try {
            return Stub.asInterface(ServiceManager.getService("power")).updateRgbGamma(red, green, blue);
        } catch (RemoteException e) {
            return -1;
        }
    }

    private int setColorTemperature(int colorTemper) {
        Slog.d(TAG, "setColorTemperature:" + colorTemper);
        try {
            return Stub.asInterface(ServiceManager.getService("power")).setColorTemperature(colorTemper);
        } catch (RemoteException e) {
            return -1;
        }
    }

    private int setColorTemperatureNew(int colorTemper) {
        Slog.d(TAG, "setColorTemperature:" + colorTemper);
        try {
            if (!mLoadLibraryFailed) {
                return nativeSetColorTemperatureNew(colorTemper);
            }
            Slog.d(TAG, "nativeSetColorTemperatureNew not valid!");
            return VALUE_BLUELIGHT_FILTER_DEFAULT;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "nativeSetColorTemperatureNew not found!");
            return -1;
        }
    }

    private int filterBlueLight(int value) {
        Slog.d(TAG, "filterBlueLight:" + value);
        try {
            if (!mLoadLibraryFailed) {
                return nativeFilterBlueLight(value);
            }
            Slog.d(TAG, "filterBlueLight not valid!");
            return VALUE_BLUELIGHT_FILTER_DEFAULT;
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "filterBlueLight not found!");
            return -1;
        }
    }

    private void updateBrightness() {
        if (isFunctionExist(MSG_UPDATE_BACKLIGHT)) {
            this.mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BACKLIGHT, 200);
        }
    }

    private void updateGlobalSceneState() {
        Slog.i(TAG, "updateGlobalSceneState, mEyesProtectionMode =" + this.mEyesProtectionMode);
        if (this.mEyesProtectionMode == MSG_SET_FILTER_BLUE_LIGHT) {
            this.mfirstSceneSwitchOn = true;
            updateBlueLightLevel(VALUE_BLUELIGHT_FILTER_REAL);
        } else {
            this.mfirstSceneSwitchOn = DEBUG;
            updateBlueLightLevel(VALUE_BLUELIGHT_FILTER_DEFAULT);
            this.mAmbientLux = -1.0f;
            updateColorTemperature();
        }
        updateBrightness();
        updateNotification();
    }

    private void updateBlueLightLevel(int level) {
        if (isFunctionExist(MSG_SET_FILTER_BLUE_LIGHT)) {
            Slog.i(TAG, "blue light animationTo target = " + level);
            this.mBluelightAnimationTarget = level;
            this.mBluelightAnimationTimes = MSG_SET_FILTER_BLUE_LIGHT;
            this.mBluelightBeforeAnimation = this.mCurrentFilterValue;
            this.mHandler.sendEmptyMessageDelayed(MSG_SET_FILTER_BLUE_LIGHT, 0);
        }
    }

    public void onScreenStateChanged(boolean powerStatus) {
        Slog.i(TAG, "ScreenStateChanged powerStatus =" + powerStatus);
        if (powerStatus && this.mEyesProtectionMode == MSG_SET_FILTER_BLUE_LIGHT) {
            registerLightSensor();
        } else if (this.mEyesProtectionMode == MSG_SET_FILTER_BLUE_LIGHT) {
            this.mHandler.removeMessages(VALUE_BLUELIGHT_FILTER_DEFAULT);
            unregisterLightSensor();
        }
    }

    private boolean isFunctionExist(int mode) {
        if ((EYE_PROTECTIION_MODE & mode) != 0) {
            return true;
        }
        return DEBUG;
    }

    public HwCustEyeProtectionControllerImpl(Context context, AutomaticBrightnessController automaticBrightnessController) {
        super(context, automaticBrightnessController);
        this.mfirstSceneSwitchOn = true;
        this.mProtectionModeObserver = new AnonymousClass1(new Handler());
        this.mLightSensorRate = 300;
        this.mAmbientLux = -1.0f;
        this.AMBIRNT_LUX_THRESHOLD = 0.1f;
        this.mLightSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                long time = SystemClock.uptimeMillis();
                float lux = event.values[HwCustEyeProtectionControllerImpl.VALUE_BLUELIGHT_FILTER_DEFAULT];
                long timeStamp = event.timestamp;
                HwCustEyeProtectionControllerImpl.this.handleLightSensorEvent(time, lux);
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.red = 1.0f;
        this.green = 1.0f;
        this.blue = 1.0f;
        this.ACTION_SUPER_POWERMODE = "huawei.intent.action.PG_EXTREME_MODE_ENABLE_ACTION";
        this.mNotificationShown = DEBUG;
        this.mColorTemperatureTimes = MSG_SET_FILTER_BLUE_LIGHT;
        this.mAmbientLux = -1.0f;
        this.mCurrentFilterValue = VALUE_BLUELIGHT_FILTER_DEFAULT;
        this.mCurrentColorTemperature = COLOR_TEMPERATURE_CLOUDY;
        this.mColorTemperatureTarget = COLOR_TEMPERATURE_CLOUDY;
        if (isFunctionExist(MSG_SET_FILTER_BLUE_LIGHT) || isFunctionExist(MODE_COLOR_TEMPERATURE) || isFunctionExist(MSG_UPDATE_BACKLIGHT)) {
            this.mScreenStateReceiver = new ScreenStateReceiver();
            this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_EYES_PROTECTION), true, this.mProtectionModeObserver, -1);
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
            this.mHandler = new SmartDisplayHandler(this.mHandlerThread.getLooper());
            this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        }
        try {
            if (mLoadLibraryFailed) {
                Slog.d(TAG, "init_native not valid!");
            } else {
                init_native();
            }
        } catch (UnsatisfiedLinkError e) {
            Slog.d(TAG, "init_native not found!");
        }
    }

    private void updateNotification() {
        if (this.mNotificationManager != null) {
            if (this.mEyesProtectionMode == MSG_SET_FILTER_BLUE_LIGHT) {
                if (!this.mNotificationShown) {
                    Resources r = this.mContext.getResources();
                    CharSequence title = r.getText(33685878);
                    CharSequence message = r.getText(33685879);
                    Notification notification = new Builder(this.mContext).setSmallIcon(33751077).setLargeIcon(BitmapFactory.decodeResource(r, 33751077)).setWhen(0).setOngoing(true).setTicker(title).setDefaults(VALUE_BLUELIGHT_FILTER_DEFAULT).setPriority(-2).setColor(this.mContext.getColor(17170519)).setContentTitle(title).setContentText(message).setContentIntent(PendingIntent.getActivityAsUser(this.mContext, VALUE_BLUELIGHT_FILTER_DEFAULT, Intent.makeRestartActivityTask(new ComponentName("com.android.settings", "com.android.settings.EyeCareMainActivity")), VALUE_BLUELIGHT_FILTER_DEFAULT, null, UserHandle.CURRENT)).setVisibility(MSG_SET_FILTER_BLUE_LIGHT).build();
                    notification.extras = getNotificationThemeData(33751076, -1, 6, 15);
                    this.mNotificationShown = true;
                    this.mNotificationManager.notifyAsUser(null, 33685878, notification, UserHandle.ALL);
                }
            } else if (this.mNotificationShown) {
                this.mNotificationShown = DEBUG;
                this.mNotificationManager.cancelAsUser(null, 33685878, UserHandle.ALL);
            }
        }
    }

    public static Bundle getNotificationThemeData(int contIconId, int repIconId, int bgIndex, int repLocation) {
        return getNotificationThemeData(null, contIconId, repIconId, bgIndex, repLocation);
    }

    public static Bundle getNotificationThemeData(Bundle bundle, int contIconId, int repIconId, int bgIndex, int repLocation) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (contIconId > 0) {
            bundle.putInt(HW_NOTIFICATION_CONTENT_ICON, contIconId);
        }
        if (repIconId > 0) {
            bundle.putInt(HW_NOTIFICATION_REPLACE_ICONID, repIconId);
        }
        if (bgIndex >= 0) {
            bundle.putInt(HW_NOTIFICATION_BACKGROUND_INDEX, bgIndex);
        }
        if (repLocation > 0) {
            bundle.putInt(HW_NOTIFICATION_REPLACE_LOCATION, repLocation);
        }
        return bundle;
    }

    private void blueLightAnimationTo(int target, int delayed) {
        this.mHandler.removeMessages(MSG_SET_FILTER_BLUE_LIGHT);
        int value = target;
        if (this.mBluelightAnimationTarget > this.mCurrentFilterValue) {
            value = (this.mBluelightAnimationTarget * this.mBluelightAnimationTimes) / VALUE_ANIMATION_MAX_TIMES;
            if (value > this.mBluelightAnimationTarget) {
                value = this.mBluelightAnimationTarget;
            }
        } else if (this.mBluelightAnimationTarget < this.mCurrentFilterValue) {
            value = (this.mBluelightBeforeAnimation * (20 - this.mBluelightAnimationTimes)) / VALUE_ANIMATION_MAX_TIMES;
            if (value < this.mBluelightAnimationTarget) {
                value = this.mBluelightAnimationTarget;
            }
        } else {
            return;
        }
        filterBlueLight(value);
        this.mBluelightAnimationTimes += MSG_SET_FILTER_BLUE_LIGHT;
        this.mCurrentFilterValue = value;
        if (this.mBluelightAnimationTimes <= VALUE_ANIMATION_MAX_TIMES && this.mBluelightAnimationTarget != this.mCurrentFilterValue) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(MSG_SET_FILTER_BLUE_LIGHT, this.mBluelightAnimationTarget, this.mCurrentFilterValue), (long) delayed);
        }
    }

    private void colorTemperatureAnimationTo(int target, int delayed) {
        this.mHandler.removeMessages(VALUE_BLUELIGHT_FILTER_DEFAULT);
        int value = target;
        if (this.mColorTemperatureTarget > this.mColorBeforeAnimation) {
            value = this.mColorBeforeAnimation + (((this.mColorTemperatureTarget - this.mColorBeforeAnimation) * this.mColorTemperatureTimes) / VALUE_ANIMATION_MAX_TIMES);
            if (value > this.mColorTemperatureTarget) {
                value = this.mColorTemperatureTarget;
            }
        } else if (this.mColorTemperatureTarget < this.mColorBeforeAnimation) {
            value = this.mColorBeforeAnimation - (((this.mColorBeforeAnimation - this.mColorTemperatureTarget) * this.mColorTemperatureTimes) / VALUE_ANIMATION_MAX_TIMES);
            if (value < this.mColorTemperatureTarget) {
                value = this.mColorTemperatureTarget;
            }
        }
        setColorTemperatureNew(value);
        this.mColorTemperatureTimes += MSG_SET_FILTER_BLUE_LIGHT;
        this.mCurrentColorTemperature = value;
        if (this.mColorTemperatureTimes <= VALUE_ANIMATION_MAX_TIMES && this.mColorTemperatureTarget != this.mCurrentColorTemperature) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(VALUE_BLUELIGHT_FILTER_DEFAULT, this.mColorTemperatureTarget, this.mCurrentColorTemperature), (long) delayed);
        } else if (this.mAmbientLux < 0.0f) {
            setColorTemperatureAccordingToSetting();
        }
    }
}

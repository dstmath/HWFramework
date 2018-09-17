package com.huawei.facerecognition.task;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.huawei.facerecognition.utils.LogUtil;

public class ScreenLighter {
    private static final String TAG = "ScreenLighter";
    private Context mContext;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private static class BrightnessHandler extends Handler {
        private static final int[] BRIGHTNESS_ARRAY = new int[]{4, 4, 5, 5, 6, 6, 7, 8, 9, 9, 10, 11, 13, 14, 15, 17, 18, 20, 22, 24, 27, 30, 33, 36, 39, 43, 48, 52, 58, 63, 70};
        private static final int BRIGHTNESS_SLOT = SystemProperties.getInt("ro.config.face_light_slot", 75);
        private static final int BRIGHTNESS_THRESHOLD = SystemProperties.getInt("ro.config.face_rise_light", 0);
        public static final int MSG_DO_LIGHTER = 10;
        public static final int MSG_INIT_SENSOR = 12;
        public static final int MSG_RELEASE_SENSOR = 13;
        public static final int MSG_STOP_LIGHTER = 11;
        private static final float SENSOR_LIGHT_THRESHOLD = 1.0f;
        private static final String TAG = "BrightnessHandler";
        private ContentResolver mContentResolver;
        private Context mContext;
        private int mFirstIndex;
        private boolean mIsBrightnessModified;
        private SensorEventListener mLightListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
                float lux = sensorEvent.values[0];
                LogUtil.d(BrightnessHandler.TAG, "current lux is " + lux + ", threshold is " + BrightnessHandler.SENSOR_LIGHT_THRESHOLD);
                synchronized (BrightnessHandler.this.mSensorLock) {
                    if (BrightnessHandler.this.mNeedStartLight && lux < BrightnessHandler.SENSOR_LIGHT_THRESHOLD) {
                        LogUtil.d(BrightnessHandler.TAG, "start Do Lighter");
                        BrightnessHandler.this.sendEmptyMessageDelayed(10, (long) BrightnessHandler.BRIGHTNESS_SLOT);
                        BrightnessHandler.this.mNeedStartLight = false;
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        private Sensor mLightSensor;
        private volatile boolean mNeedStartLight;
        private int mScreenBrightness;
        private int mScreenMode;
        private Object mSensorLock = new Object();
        private SensorManager mSensorManager;

        public BrightnessHandler(Looper looper, Context context) {
            super(looper);
            this.mContext = context;
            this.mIsBrightnessModified = false;
            this.mContentResolver = this.mContext.getContentResolver();
        }

        public void handleMessage(Message msg) {
            LogUtil.d(TAG, "receive message : " + msg.what);
            switch (msg.what) {
                case 10:
                    int brightness = getScreenBrightness();
                    LogUtil.d(TAG, "try to adjust brightness from " + brightness + " to " + BRIGHTNESS_THRESHOLD);
                    if (brightness < BRIGHTNESS_THRESHOLD) {
                        if (!this.mIsBrightnessModified) {
                            this.mScreenMode = getScreenMode();
                            this.mScreenBrightness = getScreenBrightness();
                            this.mFirstIndex = getFirstBrightnessIndex(this.mScreenBrightness);
                            this.mIsBrightnessModified = true;
                        } else if (this.mFirstIndex < BRIGHTNESS_ARRAY.length) {
                            int[] iArr = BRIGHTNESS_ARRAY;
                            int i = this.mFirstIndex;
                            this.mFirstIndex = i + 1;
                            setScreenBrightness(iArr[i]);
                        }
                        sendEmptyMessageDelayed(10, (long) BRIGHTNESS_SLOT);
                        break;
                    }
                    break;
                case 11:
                    removeMessages(10);
                    if (this.mIsBrightnessModified) {
                        LogUtil.d(TAG, "recover Brightness Settings");
                        setScreenBrightness(this.mScreenBrightness);
                        setScreenMode(this.mScreenMode);
                        break;
                    }
                    break;
                case 12:
                    this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
                    if (this.mSensorManager != null) {
                        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
                        if (this.mLightSensor != null) {
                            this.mNeedStartLight = true;
                            this.mSensorManager.registerListener(this.mLightListener, this.mLightSensor, 3);
                            break;
                        }
                        LogUtil.w(TAG, "Light Sensor not Found!");
                        return;
                    }
                    LogUtil.w(TAG, "Sensor Manager not Found!");
                    return;
                case 13:
                    synchronized (this.mSensorLock) {
                        this.mNeedStartLight = false;
                        if (this.mSensorManager != null) {
                            this.mSensorManager.unregisterListener(this.mLightListener);
                            this.mSensorManager = null;
                        }
                        this.mLightSensor = null;
                    }
            }
        }

        private int getScreenBrightness() {
            return System.getInt(this.mContentResolver, "screen_brightness", 255);
        }

        private void setScreenBrightness(int brightness) {
            if (brightness < 0 || brightness > 255) {
                LogUtil.w(TAG, "invalid brightness to set");
                return;
            }
            if (getScreenMode() != 0) {
                setScreenMode(0);
            }
            System.putInt(this.mContentResolver, "screen_brightness", brightness);
        }

        private int getScreenMode() {
            int mode = 1;
            try {
                return System.getInt(this.mContentResolver, "screen_brightness_mode");
            } catch (SettingNotFoundException e) {
                LogUtil.w(TAG, "Setting Not Found!");
                return mode;
            }
        }

        private void setScreenMode(int mode) {
            if (mode == 1 || mode == 0) {
                if (mode == 1) {
                    PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
                    if (powerManager != null) {
                        powerManager.setModeToAutoNoClearOffsetEnable(true);
                    }
                }
                System.putInt(this.mContentResolver, "screen_brightness_mode", mode);
                return;
            }
            LogUtil.w(TAG, "invalid brightness mode to set");
        }

        private int getFirstBrightnessIndex(int brightness) {
            for (int i = 0; i < BRIGHTNESS_ARRAY.length; i++) {
                if (BRIGHTNESS_ARRAY[i] > brightness) {
                    return i;
                }
            }
            return 0;
        }
    }

    ScreenLighter(Context context) {
        this.mContext = context;
    }

    void onStart() {
        this.mHandlerThread = new HandlerThread("screen_lighter");
        this.mHandlerThread.start();
        this.mHandler = new BrightnessHandler(this.mHandlerThread.getLooper(), this.mContext);
        this.mHandler.sendEmptyMessage(12);
    }

    void onStop() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(13);
            this.mHandler.sendEmptyMessage(11);
        }
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quitSafely();
        }
        this.mHandlerThread = null;
        this.mHandler = null;
    }
}

package com.android.server.display;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;

public class HwProximitySensorDetector {
    private static final long DEFAUL_INIT_SYSTEM_TIME = -1;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int MSG_PROXIMITY_ENABLE_STATE = 3;
    private static final int MSG_PROXIMITY_SENSOR_DEBOUNCED = 2;
    private static final int MSG_REPORT_PROXIMITY_STATE = 1;
    private static final int PROXIMITY_EVENT_DISTANCE_INDEX = 0;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final long PROXIMITY_PENDING_DEFAULT_TIME = -1;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final float PROXIMITY_POSITIVE_MAX_DISTANCE = 5.0f;
    private static final float PROXIMITY_POSITIVE_MIN_DISTANCE = 0.0f;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final String TAG = "HwProximitySensorDetector";
    private static final int TIME_DELAYED_USING_PROXIMITY_STATE = 500;
    private Callbacks mCallbacks;
    private final HwBrightnessXmlLoader.Data mData;
    private HwAmbientLuxFilterAlgo mHwAmbientLuxFilterAlgo;
    private final HwProximitySensorDetectorHandler mHwProximitySensorDetectorHandler;
    private final HandlerThread mHwProximitySensorDetectorThread;
    private boolean mIsCurrentProximityEnable = false;
    boolean mIsFirstBrightnessAfterProximityNegative = false;
    private boolean mIsGameDisableAutoBrightnessModeEnable = false;
    private boolean mIsProximityPositive = false;
    boolean mIsProximitySceneModeOpened = false;
    private boolean mIsProximitySensorEnabled = false;
    boolean mIsWakeupFromSleep = false;
    private long mLightSensorEnableTime = 0;
    private int mPendingProximity = -1;
    private long mPendingProximityDebounceTime = -1;
    int mProximity = -1;
    private int mProximityForCallBack = -1;
    private long mProximityReportTime = 0;
    private Sensor mProximitySensor;
    private final SensorEventListener mProximitySensorListener = new SensorEventListener() {
        /* class com.android.server.display.HwProximitySensorDetector.AnonymousClass1 */

        @Override // android.hardware.SensorEventListener
        public void onSensorChanged(SensorEvent event) {
            if (!HwProximitySensorDetector.this.mIsProximitySensorEnabled) {
                return;
            }
            if (event != null) {
                boolean z = false;
                float distance = event.values[0];
                HwProximitySensorDetector.this.mProximityReportTime = SystemClock.uptimeMillis();
                HwProximitySensorDetector hwProximitySensorDetector = HwProximitySensorDetector.this;
                if (distance >= 0.0f && distance < HwProximitySensorDetector.PROXIMITY_POSITIVE_MAX_DISTANCE) {
                    z = true;
                }
                hwProximitySensorDetector.mIsProximityPositive = z;
                if (HwProximitySensorDetector.HWFLOW) {
                    Slog.i(HwProximitySensorDetector.TAG, "HwBrightnessProximity onSensorChanged: time=" + HwProximitySensorDetector.this.mProximityReportTime + ",distance=" + distance + ",mIsWakeupFromSleep=" + HwProximitySensorDetector.this.mIsWakeupFromSleep);
                }
                if (!HwProximitySensorDetector.this.mIsWakeupFromSleep && HwProximitySensorDetector.this.mProximityReportTime - HwProximitySensorDetector.this.mLightSensorEnableTime > 500) {
                    HwProximitySensorDetector.this.mHwProximitySensorDetectorHandler.sendEmptyMessage(1);
                }
            } else if (HwProximitySensorDetector.HWDEBUG) {
                Slog.e(HwProximitySensorDetector.TAG, "onSensorChanged event==null");
            }
        }

        @Override // android.hardware.SensorEventListener
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private final SensorManager mSensorManager;

    public interface Callbacks {
        void updateFirstBrightnessAfterProximityNegative(boolean z);

        void updateProximityState(boolean z);
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    public HwProximitySensorDetector(Callbacks callbacks, SensorManager sensorManager, HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo) {
        this.mCallbacks = callbacks;
        this.mHwAmbientLuxFilterAlgo = hwAmbientLuxFilterAlgo;
        this.mSensorManager = sensorManager;
        this.mData = HwBrightnessXmlLoader.getData();
        this.mHwProximitySensorDetectorThread = new HandlerThread(TAG);
        this.mHwProximitySensorDetectorThread.start();
        this.mHwProximitySensorDetectorHandler = new HwProximitySensorDetectorHandler(this.mHwProximitySensorDetectorThread.getLooper());
        initProximitySensor();
        Slog.i(TAG, "Init HwProximitySensorDetector");
    }

    private void initProximitySensor() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager == null) {
            Slog.w(TAG, "Init proximitySensor failed,mSensorManager==null");
        } else {
            this.mProximitySensor = sensorManager.getDefaultSensor(8);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateWakeupFromSleep(boolean isWakeupFromSleep) {
        if (this.mData.proximitySceneModeEnable || this.mData.allowLabcUseProximity) {
            this.mIsWakeupFromSleep = isWakeupFromSleep;
            Slog.i(TAG, "HwBrightnessProximity updateWakeupFromSleep,isWakeupFromSleep=" + isWakeupFromSleep);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateLightSensorEnableTime(long enableTime) {
        if (this.mData.proximitySceneModeEnable || this.mData.allowLabcUseProximity) {
            this.mLightSensorEnableTime = enableTime;
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity updateLightSensorEnableTime=" + this.mLightSensorEnableTime);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setProximitySceneMode(boolean isProximitySceneMode) {
        if (this.mData.proximitySceneModeEnable && isProximitySceneMode != this.mIsProximitySceneModeOpened) {
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity setProximitySceneMode isProximitySceneMode=" + isProximitySceneMode + ",mData.proximitySceneModeEnable=" + this.mData.proximitySceneModeEnable);
            }
            this.mIsProximitySceneModeOpened = isProximitySceneMode;
            setProximitySensorEnabled(isProximitySceneMode);
            if (!isProximitySceneMode) {
                setHwAmbientLuxFilterAlgoProximityState(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateProximitySensorEnabledMsg(boolean isProximityEnable) {
        if (this.mIsCurrentProximityEnable != isProximityEnable || (this.mData.proximitySceneModeEnable && this.mIsProximitySceneModeOpened != isProximityEnable)) {
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity updateProximitySensorEnabledMsg,isProximityEnable=" + isProximityEnable);
            }
            this.mIsCurrentProximityEnable = isProximityEnable;
            if (this.mData.proximitySceneModeEnable && this.mIsGameDisableAutoBrightnessModeEnable) {
                this.mIsProximitySceneModeOpened = isProximityEnable;
            }
            this.mHwProximitySensorDetectorHandler.removeMessages(3);
            this.mHwProximitySensorDetectorHandler.sendEmptyMessage(3);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateIsGameDisableAutoBrightnessModeEnable(boolean isGameDisableAutoBrightnessModeEnable) {
        this.mIsGameDisableAutoBrightnessModeEnable = isGameDisableAutoBrightnessModeEnable;
    }

    /* access modifiers changed from: package-private */
    public void setProximitySensorEnabled(boolean isProximitySensorEnabled) {
        Sensor sensor;
        if (isProximitySensorEnabled != this.mIsProximitySensorEnabled) {
            if (isProximitySensorEnabled) {
                if (HWFLOW) {
                    Slog.i(TAG, "open proximity sensor start ...");
                }
                this.mIsProximitySensorEnabled = true;
                SensorManager sensorManager = this.mSensorManager;
                if (sensorManager != null && (sensor = this.mProximitySensor) != null) {
                    sensorManager.registerListener(this.mProximitySensorListener, sensor, 3, this.mHwProximitySensorDetectorHandler);
                    if (HWFLOW) {
                        Slog.i(TAG, "open proximity sensor");
                    }
                } else if (HWFLOW) {
                    Slog.w(TAG, "open proximity sensor failed, mSensorManager==null");
                }
            } else {
                if (HWFLOW) {
                    Slog.i(TAG, "close proximity sensor start ...");
                }
                this.mIsProximitySensorEnabled = false;
                this.mProximity = -1;
                this.mProximityForCallBack = -1;
                this.mPendingProximity = -1;
                SensorManager sensorManager2 = this.mSensorManager;
                if (sensorManager2 != null) {
                    sensorManager2.unregisterListener(this.mProximitySensorListener);
                    this.mHwProximitySensorDetectorHandler.removeMessages(2);
                    Callbacks callbacks = this.mCallbacks;
                    if (callbacks != null) {
                        callbacks.updateProximityState(false);
                    }
                    if (HWFLOW) {
                        Slog.i(TAG, "close proximity sensor");
                    }
                }
            }
        }
    }

    private void processProximityStateForCallBack() {
        if (this.mCallbacks == null) {
            Slog.e(TAG, "processProximityStateForCallBack failed, mCallbacks==null");
        } else if (this.mProximityForCallBack != this.mProximity) {
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity mProximityForCallBack=" + this.mProximityForCallBack + "-->mProximity=" + this.mProximity);
            }
            if (this.mProximityForCallBack == 1 && this.mProximity == 0) {
                this.mIsFirstBrightnessAfterProximityNegative = true;
                this.mCallbacks.updateFirstBrightnessAfterProximityNegative(true);
            }
            this.mProximityForCallBack = this.mProximity;
            int i = this.mProximityForCallBack;
            if (i == 1) {
                this.mCallbacks.updateProximityState(true);
            } else if (i == 0) {
                this.mCallbacks.updateProximityState(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProximitySensorEvent() {
        handleProximitySensorEventInternal(this.mProximityReportTime, this.mIsProximityPositive);
        processProximityStateForCallBack();
    }

    private void handleProximitySensorEventInternal(long time, boolean isProximityPositive) {
        if (this.mPendingProximity == 0 && !isProximityPositive) {
            return;
        }
        if (this.mPendingProximity != 1 || !isProximityPositive) {
            this.mHwProximitySensorDetectorHandler.removeMessages(2);
            if (isProximityPositive) {
                this.mPendingProximity = 1;
                this.mPendingProximityDebounceTime = ((long) this.mData.proximityPositiveDebounceTime) + time;
            } else {
                this.mPendingProximity = 0;
                this.mPendingProximityDebounceTime = ((long) this.mData.proximityNegativeDebounceTime) + time;
            }
            if (HWFLOW) {
                Slog.i(TAG, "HwBrightnessProximity mPendingProximity=" + this.mPendingProximity + ",time=" + time + ",mPendingProximityDebounceTime=" + this.mPendingProximityDebounceTime);
            }
            debounceProximitySensorStateInternal();
        }
    }

    private void debounceProximitySensorStateInternal() {
        if (this.mPendingProximity != -1 && this.mPendingProximityDebounceTime >= 0) {
            long now = SystemClock.uptimeMillis();
            long j = this.mPendingProximityDebounceTime;
            if (j <= now) {
                if (this.mProximity != this.mPendingProximity) {
                    if (HWFLOW) {
                        Slog.i(TAG, "HwBrightnessProximity mProximity=" + this.mProximity + "-->mPendingProximity=" + this.mPendingProximity);
                    }
                    this.mProximity = this.mPendingProximity;
                }
                boolean z = true;
                if (this.mProximity != 1) {
                    z = false;
                }
                setHwAmbientLuxFilterAlgoProximityState(z);
                clearPendingProximityDebounceTime();
                return;
            }
            this.mHwProximitySensorDetectorHandler.sendEmptyMessageAtTime(2, j);
            if (HWDEBUG) {
                Slog.d(TAG, "HwBrightnessProximity MSG_PROXIMITY_SENSOR_DEBOUNCED,mPendingTime=" + this.mPendingProximityDebounceTime);
            }
        }
    }

    private void setHwAmbientLuxFilterAlgoProximityState(boolean isProximityPositiveState) {
        HwAmbientLuxFilterAlgo hwAmbientLuxFilterAlgo = this.mHwAmbientLuxFilterAlgo;
        if (hwAmbientLuxFilterAlgo == null) {
            Slog.w(TAG, "mHwAmbientLuxFilterAlgo==null, isProximityPositiveState=" + isProximityPositiveState);
            return;
        }
        hwAmbientLuxFilterAlgo.setProximityState(isProximityPositiveState);
    }

    private void clearPendingProximityDebounceTime() {
        if (this.mPendingProximityDebounceTime >= 0) {
            this.mPendingProximityDebounceTime = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void debounceProximitySensor() {
        debounceProximitySensorStateInternal();
        processProximityStateForCallBack();
    }

    /* access modifiers changed from: package-private */
    public boolean getProximityPositive() {
        return this.mIsProximityPositive;
    }

    /* access modifiers changed from: package-private */
    public boolean isProximitySensorEnabled() {
        return this.mIsProximitySensorEnabled;
    }

    /* access modifiers changed from: private */
    public final class HwProximitySensorDetectorHandler extends Handler {
        HwProximitySensorDetectorHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg == null) {
                Slog.w(HwProximitySensorDetector.TAG, "HwProximitySensorDetectorHandler msg==null");
                return;
            }
            int i = msg.what;
            if (i == 1) {
                HwProximitySensorDetector.this.handleProximitySensorEvent();
            } else if (i == 2) {
                HwProximitySensorDetector.this.debounceProximitySensor();
            } else if (i == 3) {
                HwProximitySensorDetector hwProximitySensorDetector = HwProximitySensorDetector.this;
                hwProximitySensorDetector.setProximitySensorEnabled(hwProximitySensorDetector.mIsCurrentProximityEnable);
            }
        }
    }
}

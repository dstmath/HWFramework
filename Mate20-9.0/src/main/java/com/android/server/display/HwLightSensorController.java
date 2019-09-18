package com.android.server.display;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class HwLightSensorController {
    /* access modifiers changed from: private */
    public static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MSG_TIMER = 1;
    private static final String TAG = "HwLightSensorController";
    /* access modifiers changed from: private */
    public int SENSOR_OPTION;
    /* access modifiers changed from: private */
    public int mBackSensorValue;
    /* access modifiers changed from: private */
    public final LightSensorCallbacks mCallbacks;
    private final Context mContext;
    private List<Integer> mCtDataList;
    private List<Integer> mDataList;
    /* access modifiers changed from: private */
    public boolean mEnable;
    /* access modifiers changed from: private */
    public long mEnableTime;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HwDualSensorEventListenerImpl mHwDualSensorEventListenerImpl;
    private int mLastSensorCtValue;
    private int mLastSensorValue;
    private Sensor mLightSensor;
    /* access modifiers changed from: private */
    public int mRateMillis;
    private final SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;
    private SensorObserver mSensorObserver;
    /* access modifiers changed from: private */
    public boolean mWarmUpFlg;

    public interface LightSensorCallbacks {
        void processSensorData(long j, int i, int i2);
    }

    private class SensorObserver implements Observer {
        public SensorObserver() {
        }

        public void update(Observable o, Object arg) {
            long[] data = (long[]) arg;
            int lux = (int) data[0];
            int cct = (int) data[1];
            long timeStamp = data[2];
            if (HwLightSensorController.this.SENSOR_OPTION == 2) {
                int unused = HwLightSensorController.this.mBackSensorValue = (int) data[5];
            } else if (HwLightSensorController.this.SENSOR_OPTION == 1) {
                int unused2 = HwLightSensorController.this.mBackSensorValue = lux;
            }
            if (HwLightSensorController.this.mEnable && lux >= 0 && cct >= 0 && timeStamp >= 0) {
                HwLightSensorController.this.mCallbacks.processSensorData(timeStamp, lux, cct);
            }
        }
    }

    public HwLightSensorController(Context context, LightSensorCallbacks callbacks, SensorManager sensorManager, int sensorRateMillis) {
        this(context, callbacks, sensorManager, sensorRateMillis, TAG);
    }

    public HwLightSensorController(Context context, LightSensorCallbacks callbacks, SensorManager sensorManager, int sensorRateMillis, String TagForDualSensor) {
        this.mLastSensorValue = -1;
        this.mLastSensorCtValue = -1;
        this.mWarmUpFlg = true;
        this.mRateMillis = 300;
        HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl = this.mHwDualSensorEventListenerImpl;
        this.SENSOR_OPTION = -1;
        this.mBackSensorValue = -1;
        this.mSensorEventListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (HwLightSensorController.this.mEnable) {
                    int lux = (int) event.values[0];
                    int cct = (int) event.values[1];
                    long timeStamp = event.timestamp / 1000000;
                    if (!HwLightSensorController.this.mWarmUpFlg) {
                        HwLightSensorController.this.setSensorData(lux);
                        HwLightSensorController.this.setSensorCtData(cct);
                    } else if (timeStamp < HwLightSensorController.this.mEnableTime) {
                        if (HwLightSensorController.DEBUG) {
                            Slog.i(HwLightSensorController.TAG, "sensor not ready yet");
                        }
                    } else {
                        HwLightSensorController.this.setSensorData(lux);
                        HwLightSensorController.this.setSensorCtData(cct);
                        boolean unused = HwLightSensorController.this.mWarmUpFlg = false;
                        HwLightSensorController.this.mHandler.sendEmptyMessage(1);
                    }
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what != 1) {
                    Slog.e(HwLightSensorController.TAG, "Invalid message");
                    return;
                }
                int lux = HwLightSensorController.this.getSensorData();
                int cct = HwLightSensorController.this.getSensorCtData();
                if (lux >= 0 && cct >= 0) {
                    try {
                        HwLightSensorController.this.mCallbacks.processSensorData(SystemClock.elapsedRealtime(), lux, cct);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (HwLightSensorController.this.mEnable) {
                    sendEmptyMessageDelayed(1, (long) HwLightSensorController.this.mRateMillis);
                }
            }
        };
        this.mContext = context;
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mRateMillis = sensorRateMillis;
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mDataList = new ArrayList();
        this.mCtDataList = new ArrayList();
        this.mHwDualSensorEventListenerImpl = HwDualSensorEventListenerImpl.getInstance(this.mSensorManager, this.mContext);
        this.SENSOR_OPTION = this.mHwDualSensorEventListenerImpl.getModuleSensorOption(TagForDualSensor);
        this.mSensorObserver = new SensorObserver();
    }

    public void enableSensor() {
        if (!this.mEnable) {
            this.mEnable = true;
            this.mWarmUpFlg = true;
            this.mEnableTime = SystemClock.elapsedRealtime();
            int i = this.SENSOR_OPTION;
            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl = this.mHwDualSensorEventListenerImpl;
            if (i == -1) {
                this.mSensorManager.registerListener(this.mSensorEventListener, this.mLightSensor, this.mRateMillis * 1000);
                return;
            }
            int i2 = this.SENSOR_OPTION;
            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl2 = this.mHwDualSensorEventListenerImpl;
            if (i2 == 0) {
                this.mHwDualSensorEventListenerImpl.attachFrontSensorData(this.mSensorObserver);
                return;
            }
            int i3 = this.SENSOR_OPTION;
            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl3 = this.mHwDualSensorEventListenerImpl;
            if (i3 == 1) {
                this.mHwDualSensorEventListenerImpl.attachBackSensorData(this.mSensorObserver);
                return;
            }
            int i4 = this.SENSOR_OPTION;
            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl4 = this.mHwDualSensorEventListenerImpl;
            if (i4 == 2) {
                this.mHwDualSensorEventListenerImpl.attachFusedSensorData(this.mSensorObserver);
            }
        }
    }

    public void disableSensor() {
        if (this.mEnable) {
            this.mEnable = false;
            int i = this.SENSOR_OPTION;
            HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl = this.mHwDualSensorEventListenerImpl;
            if (i == -1) {
                this.mSensorManager.unregisterListener(this.mSensorEventListener);
                this.mHandler.removeMessages(1);
            } else {
                int i2 = this.SENSOR_OPTION;
                HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl2 = this.mHwDualSensorEventListenerImpl;
                if (i2 == 0) {
                    this.mHwDualSensorEventListenerImpl.detachFrontSensorData(this.mSensorObserver);
                } else {
                    int i3 = this.SENSOR_OPTION;
                    HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl3 = this.mHwDualSensorEventListenerImpl;
                    if (i3 == 1) {
                        this.mHwDualSensorEventListenerImpl.detachBackSensorData(this.mSensorObserver);
                    } else {
                        int i4 = this.SENSOR_OPTION;
                        HwDualSensorEventListenerImpl hwDualSensorEventListenerImpl4 = this.mHwDualSensorEventListenerImpl;
                        if (i4 == 2) {
                            this.mHwDualSensorEventListenerImpl.detachFusedSensorData(this.mSensorObserver);
                        }
                    }
                }
            }
            clearSensorData();
            clearSensorCtData();
        }
    }

    public boolean isBackSensorEnable() {
        return this.SENSOR_OPTION == 2 || this.SENSOR_OPTION == 1;
    }

    public int getBackSensorValue() {
        return this.mBackSensorValue;
    }

    /* access modifiers changed from: private */
    public void setSensorData(int lux) {
        synchronized (this.mDataList) {
            this.mDataList.add(Integer.valueOf(lux));
        }
    }

    /* access modifiers changed from: private */
    public void setSensorCtData(int cct) {
        synchronized (this.mCtDataList) {
            this.mCtDataList.add(Integer.valueOf(cct));
        }
    }

    /* access modifiers changed from: private */
    public int getSensorData() {
        synchronized (this.mDataList) {
            if (this.mDataList.isEmpty()) {
                int i = this.mLastSensorValue;
                return i;
            }
            int count = 0;
            int sum = 0;
            for (Integer data : this.mDataList) {
                sum += data.intValue();
                count++;
            }
            if (count != 0) {
                int average = sum / count;
                if (average >= 0) {
                    this.mLastSensorValue = average;
                }
            }
            this.mDataList.clear();
            int i2 = this.mLastSensorValue;
            return i2;
        }
    }

    /* access modifiers changed from: private */
    public int getSensorCtData() {
        synchronized (this.mCtDataList) {
            if (this.mCtDataList.isEmpty()) {
                int i = this.mLastSensorCtValue;
                return i;
            }
            int count = 0;
            int sum = 0;
            for (Integer data : this.mCtDataList) {
                sum += data.intValue();
                count++;
            }
            if (count != 0) {
                int average = sum / count;
                if (average >= 0) {
                    this.mLastSensorCtValue = average;
                }
            }
            this.mCtDataList.clear();
            int i2 = this.mLastSensorCtValue;
            return i2;
        }
    }

    private void clearSensorData() {
        synchronized (this.mDataList) {
            this.mDataList.clear();
            this.mLastSensorValue = -1;
        }
    }

    private void clearSensorCtData() {
        synchronized (this.mCtDataList) {
            this.mCtDataList.clear();
            this.mLastSensorCtValue = -1;
        }
    }
}

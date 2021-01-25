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
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/* access modifiers changed from: package-private */
public class HwLightSensorListener {
    private static final String DEFAULT_SENSOR_RIGISTER_TAG = "HwLightSensorController";
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final int INVALID_SENSOR_VALUE = -1;
    private static final int MSG_TIMER = 1;
    private static final String TAG = "HwLightSensorListener";
    private static final int US_PER_MS = 1000;
    private int mBackSensorValue;
    private final LightSensorCallbacks mCallbacks;
    private final SensorData mCctData;
    private final Context mContext;
    private int mCurrentDisplayMode;
    private long mEnableTime;
    private final HwFoldScreenManagerEx.FoldDisplayModeListener mFoldDisplayModeListener;
    private final Handler mHandler;
    private final HwDualSensorEventListenerImpl mHwDualSensorEventListenerImpl;
    private boolean mIsDisplayModeListenerEnabled;
    private boolean mIsEnable;
    private boolean mIsInwardFoldScreenEnable;
    private boolean mIsWarmUpFlg;
    private final Sensor mLightSensor;
    private final SensorData mLuxData;
    private final int mRateMillis;
    private final SensorEventListener mSensorEventListener;
    private final SensorManager mSensorManager;
    private final Observer mSensorObserver;
    private final int mSensorOption;

    /* access modifiers changed from: package-private */
    public interface LightSensorCallbacks {
        void processSensorData(long j, int i, int i2);
    }

    static {
        boolean z = false;
        if (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4))) {
            z = true;
        }
        HWFLOW = z;
    }

    /* access modifiers changed from: private */
    public class SensorData {
        private final List<Integer> mDataList;
        private int mLastData;

        private SensorData() {
            this.mDataList = new ArrayList();
            this.mLastData = -1;
        }

        /* access modifiers changed from: package-private */
        public void set(int value) {
            synchronized (this.mDataList) {
                this.mDataList.add(Integer.valueOf(value));
            }
        }

        /* access modifiers changed from: package-private */
        public int get() {
            int average;
            synchronized (this.mDataList) {
                if (this.mDataList.isEmpty()) {
                    return this.mLastData;
                }
                int count = 0;
                int sum = 0;
                for (Integer num : this.mDataList) {
                    sum += num.intValue();
                    count++;
                }
                if (count != 0 && (average = sum / count) >= 0) {
                    this.mLastData = average;
                }
                this.mDataList.clear();
                return this.mLastData;
            }
        }

        /* access modifiers changed from: package-private */
        public void clear() {
            synchronized (this.mDataList) {
                this.mDataList.clear();
                this.mLastData = -1;
            }
        }
    }

    HwLightSensorListener(Context context, LightSensorCallbacks callbacks, SensorManager sensorManager, int sensorRateMillis) {
        this(context, callbacks, sensorManager, sensorRateMillis, DEFAULT_SENSOR_RIGISTER_TAG);
    }

    HwLightSensorListener(Context context, LightSensorCallbacks callbacks, SensorManager sensorManager, int sensorRateMillis, String tagForDualSensor) {
        this.mLuxData = new SensorData();
        this.mCctData = new SensorData();
        this.mBackSensorValue = -1;
        this.mIsWarmUpFlg = true;
        this.mCurrentDisplayMode = 0;
        this.mIsDisplayModeListenerEnabled = false;
        this.mIsInwardFoldScreenEnable = false;
        this.mFoldDisplayModeListener = new HwFoldScreenManagerEx.FoldDisplayModeListener() {
            /* class com.android.server.display.HwLightSensorListener.AnonymousClass1 */

            public void onScreenDisplayModeChange(int displayMode) {
                if (HwLightSensorListener.HWDEBUG) {
                    Slog.i(HwLightSensorListener.TAG, "onScreenDisplayModeChange displayMode=" + displayMode);
                }
                if (HwLightSensorListener.this.mCurrentDisplayMode != displayMode) {
                    if (HwLightSensorListener.HWFLOW) {
                        Slog.i(HwLightSensorListener.TAG, "mCurrentDisplayMode=" + HwLightSensorListener.this.mCurrentDisplayMode + "-->displayMode=" + displayMode);
                    }
                    HwLightSensorListener.this.mCurrentDisplayMode = displayMode;
                }
            }
        };
        this.mSensorEventListener = new SensorEventListener() {
            /* class com.android.server.display.HwLightSensorListener.AnonymousClass2 */
            private static final int INDEX_CCT = 1;
            private static final int INDEX_LUX = 0;
            private static final long NS_PER_MS = 1000000;

            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent event) {
                if (HwLightSensorListener.this.mIsEnable) {
                    int lux = (int) event.values[0];
                    int cct = (int) event.values[1];
                    long timeStamp = event.timestamp / NS_PER_MS;
                    if (!HwLightSensorListener.this.mIsWarmUpFlg) {
                        HwLightSensorListener.this.mLuxData.set(lux);
                        HwLightSensorListener.this.mCctData.set(cct);
                    } else if (timeStamp >= HwLightSensorListener.this.mEnableTime) {
                        HwLightSensorListener.this.mLuxData.set(lux);
                        HwLightSensorListener.this.mCctData.set(cct);
                        HwLightSensorListener.this.mIsWarmUpFlg = false;
                        HwLightSensorListener.this.mHandler.sendEmptyMessage(1);
                    } else if (HwLightSensorListener.HWFLOW) {
                        Slog.i(HwLightSensorListener.TAG, "sensor not ready yet");
                    }
                }
            }

            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        this.mHandler = new Handler() {
            /* class com.android.server.display.HwLightSensorListener.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    int lux = HwLightSensorListener.this.mLuxData.get();
                    int cct = HwLightSensorListener.this.mCctData.get();
                    if (lux >= 0 && cct >= 0) {
                        HwLightSensorListener.this.mCallbacks.processSensorData(SystemClock.elapsedRealtime(), lux, cct);
                    }
                    if (HwLightSensorListener.this.mIsEnable) {
                        sendEmptyMessageDelayed(1, (long) HwLightSensorListener.this.mRateMillis);
                    }
                } else if (HwLightSensorListener.HWFLOW) {
                    Slog.e(HwLightSensorListener.TAG, "Invalid message");
                }
            }
        };
        this.mSensorObserver = new Observer() {
            /* class com.android.server.display.HwLightSensorListener.AnonymousClass4 */
            private static final int INDEX_BACK_RAW = 5;
            private static final int INDEX_CCT = 1;
            private static final int INDEX_FRONT_RAW = 4;
            private static final int INDEX_LUX = 0;
            private static final int INDEX_TIME_STAMP = 2;

            @Override // java.util.Observer
            public void update(Observable obs, Object arg) {
                if (arg instanceof long[]) {
                    long[] data = (long[]) arg;
                    int cct = (int) data[1];
                    long timeStamp = data[2];
                    int lux = updateLuxFromFoldScreen((int) data[0], data);
                    if (HwLightSensorListener.this.mSensorOption == 2) {
                        HwLightSensorListener.this.mBackSensorValue = (int) data[5];
                    }
                    if (HwLightSensorListener.this.mSensorOption == 1) {
                        HwLightSensorListener.this.mBackSensorValue = lux;
                    }
                    if (HwLightSensorListener.this.mIsEnable && lux >= 0 && cct >= 0 && timeStamp >= 0) {
                        HwLightSensorListener.this.mCallbacks.processSensorData(timeStamp, lux, cct);
                    }
                } else if (HwLightSensorListener.HWFLOW) {
                    Slog.e(HwLightSensorListener.TAG, "update() arg is invalid");
                }
            }

            private int updateLuxFromFoldScreen(int lux, long[] data) {
                if (!HwLightSensorListener.this.mIsInwardFoldScreenEnable || data == null || data.length <= 5) {
                    return lux;
                }
                int fusedLux = (int) data[0];
                int frontLux = (int) data[4];
                int backLux = (int) data[5];
                int i = HwLightSensorListener.this.mCurrentDisplayMode;
                if (i != 1) {
                    return i != 2 ? lux : backLux;
                }
                return fusedLux;
            }
        };
        this.mContext = context;
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mRateMillis = sensorRateMillis;
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
        this.mHwDualSensorEventListenerImpl = HwDualSensorEventListenerImpl.getInstance(this.mSensorManager, this.mContext);
        this.mSensorOption = this.mHwDualSensorEventListenerImpl.getModuleSensorOption(tagForDualSensor);
        this.mIsInwardFoldScreenEnable = this.mHwDualSensorEventListenerImpl.isInwardFoldScreenEnable();
        if (HWFLOW) {
            Slog.i(TAG, "mSensorOption=" + this.mSensorOption + ",mIsInwardFoldScreenEnable=" + this.mIsInwardFoldScreenEnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void enableSensor() {
        if (!this.mIsEnable) {
            this.mIsEnable = true;
            this.mIsWarmUpFlg = true;
            this.mEnableTime = SystemClock.elapsedRealtime();
            int i = this.mSensorOption;
            if (i == -1) {
                this.mSensorManager.registerListener(this.mSensorEventListener, this.mLightSensor, this.mRateMillis * 1000);
            } else if (i == 0) {
                this.mHwDualSensorEventListenerImpl.attachFrontSensorData(this.mSensorObserver);
            } else if (i == 1) {
                this.mHwDualSensorEventListenerImpl.attachBackSensorData(this.mSensorObserver);
            } else if (i == 2) {
                this.mHwDualSensorEventListenerImpl.attachFusedSensorData(this.mSensorObserver);
            } else if (HWFLOW) {
                Slog.e(TAG, "enableSensor() invalid mSensorOption = " + this.mSensorOption);
            }
            if (this.mIsInwardFoldScreenEnable && this.mSensorOption == 2) {
                setFoldDisplayModeEnable(this.mIsEnable);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void disableSensor() {
        if (this.mIsEnable) {
            this.mIsEnable = false;
            int i = this.mSensorOption;
            if (i == -1) {
                this.mSensorManager.unregisterListener(this.mSensorEventListener);
                this.mHandler.removeMessages(1);
            } else if (i == 0) {
                this.mHwDualSensorEventListenerImpl.detachFrontSensorData(this.mSensorObserver);
            } else if (i == 1) {
                this.mHwDualSensorEventListenerImpl.detachBackSensorData(this.mSensorObserver);
            } else if (i == 2) {
                this.mHwDualSensorEventListenerImpl.detachFusedSensorData(this.mSensorObserver);
            } else if (HWFLOW) {
                Slog.e(TAG, "disableSensor() invalid mSensorOption = " + this.mSensorOption);
            }
            this.mLuxData.clear();
            this.mCctData.clear();
            if (this.mIsInwardFoldScreenEnable && this.mSensorOption == 2) {
                setFoldDisplayModeEnable(this.mIsEnable);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setFoldDisplayModeEnable(boolean isDisplayModeListenerEnabled) {
        if (isDisplayModeListenerEnabled) {
            if (!this.mIsDisplayModeListenerEnabled) {
                if (HWFLOW) {
                    Slog.i(TAG, "open FoldDisplayModeListener start ...");
                }
                this.mIsDisplayModeListenerEnabled = true;
                HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayModeListener);
                this.mCurrentDisplayMode = HwFoldScreenManagerEx.getDisplayMode();
                if (HWFLOW) {
                    Slog.i(TAG, "open FoldDisplayModeListener,mCurrentDisplayMode=" + this.mCurrentDisplayMode);
                }
            }
        } else if (this.mIsDisplayModeListenerEnabled) {
            this.mIsDisplayModeListenerEnabled = false;
            HwFoldScreenManagerEx.unregisterFoldDisplayMode(this.mFoldDisplayModeListener);
            if (HWFLOW) {
                Slog.i(TAG, "close FoldDisplayModeListener");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isBackSensorEnable() {
        int i = this.mSensorOption;
        return i == 2 || i == 1;
    }

    /* access modifiers changed from: package-private */
    public int getBackSensorValue() {
        return this.mBackSensorValue;
    }
}

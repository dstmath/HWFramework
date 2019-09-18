package com.android.server.display;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManagerInternal;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.EventLog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import com.android.server.BatteryService;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.job.controllers.JobStatus;
import com.android.server.slice.SliceClientPermissions;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;

public class AutomaticBrightnessController {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_LIGHT_LONG_HORIZON_MILLIS = 10000;
    private static final long AMBIENT_LIGHT_PREDICTION_TIME_MILLIS = 100;
    private static final int AMBIENT_LIGHT_SHORT_HORIZON_MILLIS = 2000;
    protected static long BRIGHTENING_LIGHT_DEBOUNCE = 2000;
    private static final long BRIGHTENING_LIGHT_DEBOUNCE_MORE_QUICKLLY = 1000;
    protected static float BRIGHTENING_LIGHT_HYSTERESIS = 0.1f;
    private static final int BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS = 10000;
    private static float BrightenDebounceTimePara = 0.0f;
    private static float BrightenDebounceTimeParaBig = 0.6f;
    private static float BrightenDeltaLuxMax = 0.0f;
    private static float BrightenDeltaLuxMin = 0.0f;
    private static float BrightenDeltaLuxPara = 0.0f;
    protected static float DARKENING_LIGHT_HYSTERESIS = 0.2f;
    /* access modifiers changed from: private */
    public static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final boolean DEBUG_CONTROLLER = false;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    private static float DarkenDebounceTimePara = 1.0f;
    private static float DarkenDebounceTimeParaBig = 0.0f;
    private static float DarkenDeltaLuxMax = 0.0f;
    private static float DarkenDeltaLuxMin = 0.0f;
    private static float DarkenDeltaLuxPara = 1.0f;
    protected static final int INT_BRIGHTNESS_COVER_MODE = SystemProperties.getInt("ro.config.hw_cover_brightness", 60);
    private static final int LIGHT_SENSOR_RATE_MILLIS = 300;
    protected static final int MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE = 2;
    protected static final int MSG_INVALIDATE_SHORT_TERM_MODEL = 3;
    protected static final int MSG_UPDATE_AMBIENT_LUX = 1;
    protected static final int MSG_UPDATE_BRIGHTNESS = 4;
    protected static final boolean NEED_NEW_FILTER_ALGORITHM = needNewFilterAlgorithm();
    private static final float SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA = 3.0f;
    private static final int SHORT_TERM_MODEL_TIMEOUT_MILLIS = 30000;
    private static float Stability = 0.0f;
    private static final String TAG = "AutomaticBrightnessController";
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = false;
    protected long DARKENING_LIGHT_DEBOUNCE = 8000;
    private float SHORT_TERM_MODEL_THRESHOLD_RATIO = 0.6f;
    private final int mAmbientLightHorizon;
    protected AmbientLightRingBuffer mAmbientLightRingBuffer;
    protected AmbientLightRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    protected boolean mAmbientLuxValid;
    private final long mBrighteningLightDebounceConfig;
    private float mBrighteningLuxThreshold;
    private int mBrightnessAdjustmentSampleOldBrightness;
    private float mBrightnessAdjustmentSampleOldLux;
    private boolean mBrightnessAdjustmentSamplePending;
    protected boolean mBrightnessEnlarge = false;
    private final BrightnessMappingStrategy mBrightnessMapper;
    protected int mBrightnessNoLimitSetByApp = -1;
    protected final Callbacks mCallbacks;
    protected int mCurrentLightSensorRate;
    private final long mDarkeningLightDebounceConfig;
    private float mDarkeningLuxThreshold;
    private int mDisplayPolicy = 0;
    private final float mDozeScaleFactor;
    protected boolean mFirstAutoBrightness;
    protected boolean mFirstBrightnessAfterProximityNegative = false;
    protected AutomaticBrightnessHandler mHandler;
    private final HysteresisLevels mHysteresisLevels;
    protected final int mInitialLightSensorRate;
    private float mLastObservedLux;
    private long mLastObservedLuxTime;
    protected final Sensor mLightSensor;
    protected long mLightSensorEnableElapsedTimeNanos;
    protected long mLightSensorEnableTime;
    protected boolean mLightSensorEnabled;
    protected final SensorEventListener mLightSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (AutomaticBrightnessController.this.mLightSensorEnabled) {
                long time = SystemClock.uptimeMillis();
                float lux = event.values[0];
                long timeStamp = event.timestamp;
                if (AutomaticBrightnessController.DEBUG && time - AutomaticBrightnessController.this.mLightSensorEnableTime < 4000) {
                    Slog.d(AutomaticBrightnessController.TAG, "ambient lux=" + lux + ",timeStamp =" + timeStamp);
                }
                if ((!HwServiceFactory.shouldFilteInvalidSensorVal(lux) || AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE != 0) && !AutomaticBrightnessController.this.interceptHandleLightSensorEvent(timeStamp, lux)) {
                    AutomaticBrightnessController.this.handleLightSensorEvent(time, lux);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    protected int mLightSensorWarmUpTimeConfig;
    protected int mMaxBrightnessSetByCryogenic = 255;
    protected boolean mMaxBrightnessSetByCryogenicBypass = false;
    protected boolean mMaxBrightnessSetByCryogenicBypassDelayed = false;
    protected int mMaxBrightnessSetByThermal = 255;
    private final int mNormalLightSensorRate;
    protected long mPowerOffTimestamp = 0;
    protected long mPowerOnTimestamp = 0;
    private long mPrintLogTime = 0;
    protected int mRecentLightSamples;
    protected final boolean mResetAmbientLuxAfterWarmUpConfig;
    protected int mScreenAutoBrightness = -1;
    protected Spline mScreenAutoBrightnessSpline;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private int mScreenBrightnessRangeSetByAppMax;
    private int mScreenBrightnessRangeSetByAppMin;
    protected final SensorManager mSensorManager;
    protected boolean mSetbrightnessImmediateEnable = false;
    private float mShortTermModelAnchor;
    private boolean mShortTermModelValid;
    protected int mUpdateAutoBrightnessCount;
    private boolean mUseTwilight;
    protected boolean mWakeupFromSleep = true;
    private final int mWeightingIntercept;

    protected static final class AmbientLightRingBuffer {
        private static final float BUFFER_SLACK = 1.5f;
        private static final boolean DEBUG = false;
        private static final String TAG = "AmbientLightRingBuffer";
        private static final int mLargemSmallStabilityTimeConstant = 10;
        private static float mLuxBufferAvg = 0.0f;
        private static float mLuxBufferAvgMax = 0.0f;
        private static float mLuxBufferAvgMin = 0.0f;
        private static final int mSmallStabilityTimeConstant = 20;
        private static float mStability = 0.0f;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux;
        private long[] mRingTime;
        private int mStart;

        public AmbientLightRingBuffer(long lightSensorRate, int ambientLightHorizon) {
            if (AutomaticBrightnessController.NEED_NEW_FILTER_ALGORITHM) {
                this.mCapacity = (int) Math.ceil((double) ((((float) ambientLightHorizon) * BUFFER_SLACK) / ((float) lightSensorRate)));
            } else {
                this.mCapacity = 50;
            }
            this.mRingLux = new float[this.mCapacity];
            this.mRingTime = new long[this.mCapacity];
        }

        public float getLux(int index) {
            return this.mRingLux[offsetOf(index)];
        }

        public long getTime(int index) {
            return this.mRingTime[offsetOf(index)];
        }

        public void push(long time, float lux) {
            int next = this.mEnd;
            if (this.mCount == this.mCapacity) {
                int newSize = this.mCapacity * 2;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int length = this.mCapacity - this.mStart;
                System.arraycopy(this.mRingLux, this.mStart, newRingLux, 0, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
                if (this.mStart != 0) {
                    System.arraycopy(this.mRingLux, 0, newRingLux, length, this.mStart);
                    System.arraycopy(this.mRingTime, 0, newRingTime, length, this.mStart);
                }
                this.mRingLux = newRingLux;
                this.mRingTime = newRingTime;
                next = this.mCapacity;
                this.mCapacity = newSize;
                this.mStart = 0;
            }
            this.mRingTime[next] = time;
            this.mRingLux[next] = lux;
            this.mEnd = next + 1;
            if (this.mEnd == this.mCapacity) {
                this.mEnd = 0;
            }
            this.mCount++;
        }

        public void prune(long horizon) {
            if (this.mCount != 0) {
                while (this.mCount > 1) {
                    int next = this.mStart + 1;
                    if (next >= this.mCapacity) {
                        next -= this.mCapacity;
                    }
                    if (this.mRingTime[next] > horizon) {
                        break;
                    }
                    this.mStart = next;
                    this.mCount--;
                }
                if (this.mRingTime[this.mStart] < horizon) {
                    this.mRingTime[this.mStart] = horizon;
                }
            }
        }

        public int size() {
            return this.mCount;
        }

        public void clear() {
            this.mStart = 0;
            this.mEnd = 0;
            this.mCount = 0;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append('[');
            for (int i = 0; i < this.mCount; i++) {
                long next = i + 1 < this.mCount ? getTime(i + 1) : SystemClock.uptimeMillis();
                if (i != 0) {
                    buf.append(", ");
                }
                buf.append(getLux(i));
                buf.append(" / ");
                buf.append(next - getTime(i));
                buf.append("ms");
            }
            buf.append(']');
            return buf.toString();
        }

        public String toString(int n) {
            StringBuffer buf = new StringBuffer();
            buf.append('[');
            int i = this.mCount - n;
            while (i >= 0 && i < this.mCount) {
                if (i != this.mCount - n) {
                    buf.append(", ");
                }
                buf.append(getLux(i));
                buf.append(SliceClientPermissions.SliceAuthority.DELIMITER);
                buf.append(getTime(i));
                i++;
            }
            buf.append(']');
            return buf.toString();
        }

        private int offsetOf(int index) {
            if (index >= this.mCount || index < 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            int index2 = index + this.mStart;
            if (index2 >= this.mCapacity) {
                return index2 - this.mCapacity;
            }
            return index2;
        }

        public float calculateStability() {
            int indexMax;
            int T1;
            int index2;
            int index1;
            int T2;
            float tmp;
            float Stability1;
            float Stability2;
            float luxT2;
            if (this.mCount == 0) {
                return 0.0f;
            }
            float currentLux = getLux(this.mCount - 1);
            calculateAvg();
            float f = currentLux;
            int indexMax2 = 0;
            float f2 = currentLux;
            float luxT2Max = currentLux;
            int indexMin = 0;
            float f3 = currentLux;
            float luxT1Min = currentLux;
            int index = 0;
            int index3 = 0;
            int T12 = 0;
            float luxT22 = currentLux;
            float luxT23 = currentLux;
            int j = 0;
            while (true) {
                if (j >= this.mCount - 1) {
                    float luxT1 = luxT23;
                    float f4 = luxT22;
                    indexMax = indexMax2;
                    break;
                }
                float lux1 = getLux((this.mCount - 1) - j);
                float luxT12 = luxT23;
                float lux2 = getLux(((this.mCount - 1) - j) - 1);
                if (((mLuxBufferAvg <= lux1 && mLuxBufferAvg >= lux2) || (mLuxBufferAvg >= lux1 && mLuxBufferAvg <= lux2)) && !(mLuxBufferAvg == lux1 && mLuxBufferAvg == lux2)) {
                    luxT12 = lux1;
                    index = j;
                    index3 = ((this.mCount - 1) - j) - 1;
                    T12 = (this.mCount - 1) - j;
                    luxT22 = lux2;
                }
                if ((mLuxBufferAvgMin > lux1 || mLuxBufferAvgMin < lux2) && (mLuxBufferAvgMin < lux1 || mLuxBufferAvgMin > lux2)) {
                    luxT2 = luxT22;
                } else if (mLuxBufferAvgMin == lux1 && mLuxBufferAvgMin == lux2) {
                    luxT2 = luxT22;
                } else {
                    luxT1Min = lux1;
                    float luxT2Min = lux2;
                    luxT2 = luxT22;
                    indexMin = j;
                    int indexMin2 = ((this.mCount - 1) - j) - 1;
                    int T2Min = (this.mCount - 1) - j;
                }
                if (((mLuxBufferAvgMax > lux1 || mLuxBufferAvgMax < lux2) && (mLuxBufferAvgMax < lux1 || mLuxBufferAvgMax > lux2)) || (mLuxBufferAvgMax == lux1 && mLuxBufferAvgMax == lux2)) {
                    indexMax = indexMax2;
                } else {
                    float luxT2Max2 = lux1;
                    indexMax = j;
                    int indexMax3 = ((this.mCount - 1) - j) - 1;
                    int T2Max = (this.mCount - 1) - j;
                    float f5 = lux2;
                }
                if (index != 0 && ((indexMin != 0 || indexMax != 0) && ((index <= indexMin && index >= indexMax) || (index >= indexMin && index <= indexMax)))) {
                    break;
                }
                j++;
                indexMax2 = indexMax;
                luxT23 = luxT12;
                luxT22 = luxT2;
            }
            if (indexMax <= indexMin) {
                index1 = indexMax;
                index2 = indexMin;
            } else {
                index1 = indexMin;
                index2 = indexMax;
            }
            int k1 = (this.mCount - 1) - index1;
            while (true) {
                int index12 = index1;
                if (k1 > this.mCount - 1) {
                    T2 = index3;
                    break;
                } else if (k1 == this.mCount - 1) {
                    T2 = index3;
                    break;
                } else {
                    float luxk1 = getLux(k1);
                    T2 = index3;
                    float luxk2 = getLux(k1 + 1);
                    if (indexMax > indexMin) {
                        if (luxk1 <= luxk2) {
                            break;
                        }
                        T1 = k1 + 1;
                    } else if (luxk1 >= luxk2) {
                        break;
                    } else {
                        T1 = k1 + 1;
                    }
                    k1++;
                    index1 = index12;
                    index3 = T2;
                }
            }
            int k3 = (this.mCount - 1) - index2;
            int T22 = T2;
            while (true) {
                if (k3 < 0) {
                    break;
                } else if (k3 == 0) {
                    int i = index2;
                    break;
                } else {
                    float luxk3 = getLux(k3);
                    int index22 = index2;
                    float luxk4 = getLux(k3 - 1);
                    if (indexMax > indexMin) {
                        if (luxk3 >= luxk4) {
                            break;
                        }
                        T22 = k3 - 1;
                    } else if (luxk3 <= luxk4) {
                        break;
                    } else {
                        T22 = k3 - 1;
                    }
                    k3--;
                    index2 = index22;
                }
            }
            int t1 = (this.mCount - 1) - T1;
            int t2 = T22;
            float s1 = calculateStabilityFactor(T1, this.mCount - 1);
            int i2 = indexMax;
            float avg1 = calcluateAvg(T1, this.mCount - 1);
            int i3 = index;
            float s2 = calculateStabilityFactor(0, T22);
            float f6 = luxT1Min;
            float luxT1Min2 = Math.abs(avg1 - calcluateAvg(0, T22));
            float k = Math.abs((getLux(T1) - getLux(T22)) / ((float) (T1 - T22)));
            if (k < 10.0f / (k + 5.0f)) {
                tmp = k;
            } else {
                tmp = 10.0f / (k + 5.0f);
            }
            if (tmp > 20.0f / (luxT1Min2 + 10.0f)) {
                tmp = 20.0f / (luxT1Min2 + 10.0f);
            }
            float f7 = k;
            if (t1 > 20) {
                Stability1 = s1;
                int i4 = T22;
                float f8 = avg1;
            } else {
                int i5 = T22;
                float f9 = avg1;
                float a1 = (float) Math.exp((double) (t1 - 20));
                float b1 = (float) (20 - t1);
                Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
            }
            if (t2 > 10) {
                Stability2 = s2;
            } else {
                float a2 = (float) Math.exp((double) (t2 - 10));
                float b2 = (float) (10 - t2);
                Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
            }
            if (t1 > 20) {
                mStability = Stability1;
            } else {
                float a = (float) Math.exp((double) (t1 - 20));
                float b = (float) (20 - t1);
                mStability = ((a * Stability1) + (b * Stability2)) / (a + b);
            }
            return mStability;
        }

        private void calculateAvg() {
            if (this.mCount != 0) {
                float currentLux = getLux(this.mCount - 1);
                float luxBufferSum = 0.0f;
                float luxBufferMin = currentLux;
                float luxBufferMax = currentLux;
                for (int i = this.mCount - 1; i >= 0; i--) {
                    float lux = getLux(i);
                    if (lux > luxBufferMax) {
                        luxBufferMax = lux;
                    }
                    if (lux < luxBufferMin) {
                        luxBufferMin = lux;
                    }
                    luxBufferSum += lux;
                }
                mLuxBufferAvg = luxBufferSum / ((float) this.mCount);
                mLuxBufferAvgMax = (mLuxBufferAvg + luxBufferMax) / 2.0f;
                mLuxBufferAvgMin = (mLuxBufferAvg + luxBufferMin) / 2.0f;
            }
        }

        private float calcluateAvg(int start, int end) {
            float sum = 0.0f;
            for (int i = start; i <= end; i++) {
                sum += getLux(i);
            }
            if (end < start) {
                return 0.0f;
            }
            return sum / ((float) ((end - start) + 1));
        }

        private float calculateStabilityFactor(int start, int end) {
            int size = (end - start) + 1;
            float sum = 0.0f;
            float sigma = 0.0f;
            if (size <= 1) {
                return 0.0f;
            }
            for (int i = start; i <= end; i++) {
                sum += getLux(i);
            }
            float avg = sum / ((float) size);
            for (int i2 = start; i2 <= end; i2++) {
                sigma += (getLux(i2) - avg) * (getLux(i2) - avg);
            }
            float ss = sigma / ((float) (size - 1));
            if (avg == 0.0f) {
                return 0.0f;
            }
            return ss / avg;
        }
    }

    protected final class AutomaticBrightnessHandler extends Handler {
        public AutomaticBrightnessHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AutomaticBrightnessController.this.updateAmbientLux();
                    return;
                case 2:
                    AutomaticBrightnessController.this.collectBrightnessAdjustmentSample();
                    return;
                case 3:
                    AutomaticBrightnessController.this.invalidateShortTermModel();
                    return;
                case 4:
                    AutomaticBrightnessController.this.updateBrightnessIfNoAmbientLuxReported();
                    return;
                default:
                    return;
            }
        }
    }

    public interface Callbacks {
        void updateBrightness();

        void updateProximityState(boolean z);
    }

    public void setBacklightBrightness(PowerManager.BacklightBrightness backlightBrightness) {
        this.mScreenBrightnessRangeSetByAppMin = backlightBrightness.min;
        this.mScreenBrightnessRangeSetByAppMax = backlightBrightness.max;
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
    }

    public static boolean needNewFilterAlgorithm() {
        String product = getProductName();
        if (product == null) {
            return false;
        }
        boolean flag = product.contains("next");
        Slog.e(TAG, "NEWFILTER flag = " + flag);
        return flag;
    }

    public static String getProductName() {
        String productName;
        String boardname = readFileByChars("/proc/device-tree/hisi,boardname").trim();
        if (boardname == null) {
            return BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
        }
        String[] arrays = boardname.split("_");
        if (arrays == null || arrays.length < 2) {
            productName = BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
        } else {
            productName = arrays[0] + "_" + arrays[1];
        }
        return productName.toLowerCase();
    }

    private static String readFileByChars(String fileName) {
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            Slog.d(TAG, "file is exists : " + file.exists() + " file can read : " + file.canRead());
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        Reader reader = null;
        char[] tempChars = new char[512];
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader2 = new InputStreamReader(new FileInputStream(fileName));
            while (true) {
                int read = reader2.read(tempChars);
                int charRead = read;
                if (read != -1) {
                    sb.append(tempChars, 0, charRead);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                }
            }
            reader2.close();
        } catch (IOException e1) {
            Slog.e(TAG, "read file name error, file name is:" + fileName);
            e1.printStackTrace();
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
        return sb.toString();
    }

    public AutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, BrightnessMappingStrategy mapper, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, HysteresisLevels hysteresisLevels) {
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mBrightnessMapper = mapper;
        this.mScreenBrightnessRangeMinimum = brightnessMin;
        this.mScreenBrightnessRangeMaximum = brightnessMax;
        this.mScreenBrightnessRangeSetByAppMin = this.mScreenBrightnessRangeMinimum;
        this.mScreenBrightnessRangeSetByAppMax = this.mScreenBrightnessRangeMaximum;
        this.mLightSensorWarmUpTimeConfig = lightSensorWarmUpTime;
        this.mDozeScaleFactor = dozeScaleFactor;
        this.mNormalLightSensorRate = lightSensorRate;
        this.mInitialLightSensorRate = initialLightSensorRate;
        this.mCurrentLightSensorRate = -1;
        this.mBrighteningLightDebounceConfig = brighteningLightDebounceConfig;
        this.mDarkeningLightDebounceConfig = darkeningLightDebounceConfig;
        this.mResetAmbientLuxAfterWarmUpConfig = resetAmbientLuxAfterWarmUpConfig;
        this.mAmbientLightHorizon = 10000;
        this.mWeightingIntercept = 10000;
        this.mHysteresisLevels = hysteresisLevels;
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = -1.0f;
        this.mHandler = new AutomaticBrightnessHandler(looper);
        this.mAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        if (NEED_NEW_FILTER_ALGORITHM) {
            this.mAmbientLightRingBufferFilter = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        }
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
    }

    public int getAutomaticScreenBrightness() {
        int brightness = this.mScreenAutoBrightness;
        if (brightness >= 0) {
            brightness = MathUtils.constrain(brightness, this.mScreenBrightnessRangeSetByAppMin, this.mScreenBrightnessRangeSetByAppMax);
        }
        if (this.mBrightnessNoLimitSetByApp > 0) {
            return this.mBrightnessNoLimitSetByApp;
        }
        if (!this.mMaxBrightnessSetByCryogenicBypass && !this.mMaxBrightnessSetByCryogenicBypassDelayed && brightness > this.mMaxBrightnessSetByCryogenic) {
            brightness = this.mMaxBrightnessSetByCryogenic;
        }
        setBrightnessLimitedByThermal(brightness > this.mMaxBrightnessSetByThermal);
        if (brightness > this.mMaxBrightnessSetByThermal) {
            brightness = this.mMaxBrightnessSetByThermal;
        }
        int brightness2 = getAutoBrightnessBaseInOutDoorLimit(brightness);
        if (this.mDisplayPolicy == 1) {
            return (int) (((float) brightness2) * this.mDozeScaleFactor);
        }
        return brightness2;
    }

    public float getAutomaticScreenBrightnessAdjustment() {
        return this.mBrightnessMapper.getAutoBrightnessAdjustment();
    }

    public void configure(boolean enable, BrightnessConfiguration configuration, float brightness, boolean userChangedBrightness, float adjustment, boolean userChangedAutoBrightnessAdjustment, int displayPolicy) {
        boolean z = true;
        boolean dozing = displayPolicy == 1;
        boolean changed = setBrightnessConfiguration(configuration) | setDisplayPolicy(displayPolicy);
        if (userChangedAutoBrightnessAdjustment) {
            changed |= setAutoBrightnessAdjustment(adjustment);
        }
        if (userChangedBrightness && enable) {
            changed |= setScreenBrightnessByUser(brightness);
        }
        if ((userChangedBrightness || userChangedAutoBrightnessAdjustment) && enable && !dozing) {
            prepareBrightnessAdjustmentSample();
        }
        if (!enable || dozing) {
            z = false;
        }
        if (setLightSensorEnabled(z) || changed) {
            updateAutoBrightness(false);
        }
    }

    public boolean hasUserDataPoints() {
        return this.mBrightnessMapper.hasUserDataPoints();
    }

    public boolean isDefaultConfig() {
        return this.mBrightnessMapper.isDefaultConfig();
    }

    public BrightnessConfiguration getDefaultConfig() {
        return this.mBrightnessMapper.getDefaultConfig();
    }

    private boolean setDisplayPolicy(int policy) {
        if (this.mDisplayPolicy == policy) {
            return false;
        }
        int oldPolicy = this.mDisplayPolicy;
        this.mDisplayPolicy = policy;
        if (DEBUG) {
            Slog.d(TAG, "Display policy transitioning from " + oldPolicy + " to " + policy);
        }
        if (!isInteractivePolicy(policy) && isInteractivePolicy(oldPolicy)) {
            this.mHandler.sendEmptyMessageDelayed(3, 30000);
        } else if (isInteractivePolicy(policy) && !isInteractivePolicy(oldPolicy)) {
            this.mHandler.removeMessages(3);
        }
        return true;
    }

    private static boolean isInteractivePolicy(int policy) {
        return policy == 3 || policy == 2 || policy == 4;
    }

    private boolean setScreenBrightnessByUser(float brightness) {
        if (!this.mAmbientLuxValid) {
            return false;
        }
        this.mBrightnessMapper.addUserDataPoint(this.mAmbientLux, brightness);
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = this.mAmbientLux;
        if (DEBUG) {
            Slog.d(TAG, "ShortTermModel: anchor=" + this.mShortTermModelAnchor);
        }
        return true;
    }

    public void resetShortTermModel() {
        this.mBrightnessMapper.clearUserDataPoints();
        this.mShortTermModelValid = true;
        this.mShortTermModelAnchor = -1.0f;
    }

    /* access modifiers changed from: private */
    public void invalidateShortTermModel() {
        if (DEBUG) {
            Slog.d(TAG, "ShortTermModel: invalidate user data");
        }
        this.mShortTermModelValid = false;
    }

    public boolean setBrightnessConfiguration(BrightnessConfiguration configuration) {
        if (!this.mBrightnessMapper.setBrightnessConfiguration(configuration)) {
            return false;
        }
        resetShortTermModel();
        return true;
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Automatic Brightness Controller Configuration:");
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mScreenBrightnessRangeSetByAppMin=" + this.mScreenBrightnessRangeSetByAppMin);
        pw.println("  mScreenBrightnessRangeSetByAppMax=" + this.mScreenBrightnessRangeSetByAppMax);
        pw.println("  mDozeScaleFactor=" + this.mDozeScaleFactor);
        pw.println("  mInitialLightSensorRate=" + this.mInitialLightSensorRate);
        pw.println("  mNormalLightSensorRate=" + this.mNormalLightSensorRate);
        pw.println("  mLightSensorWarmUpTimeConfig=" + this.mLightSensorWarmUpTimeConfig);
        pw.println("  mBrighteningLightDebounceConfig=" + this.mBrighteningLightDebounceConfig);
        pw.println("  mDarkeningLightDebounceConfig=" + this.mDarkeningLightDebounceConfig);
        pw.println("  mResetAmbientLuxAfterWarmUpConfig=" + this.mResetAmbientLuxAfterWarmUpConfig);
        pw.println("  mAmbientLightHorizon=" + this.mAmbientLightHorizon);
        pw.println("  mWeightingIntercept=" + this.mWeightingIntercept);
        pw.println();
        pw.println("Automatic Brightness Controller State:");
        pw.println("  mLightSensor=" + this.mLightSensor);
        pw.println("  mLightSensorEnabled=" + this.mLightSensorEnabled);
        pw.println("  mLightSensorEnableTime=" + TimeUtils.formatUptime(this.mLightSensorEnableTime));
        pw.println("  mCurrentLightSensorRate=" + this.mCurrentLightSensorRate);
        pw.println("  mAmbientLux=" + this.mAmbientLux);
        pw.println("  mAmbientLuxValid=" + this.mAmbientLuxValid);
        pw.println("  mBrighteningLuxThreshold=" + this.mBrighteningLuxThreshold);
        pw.println("  mDarkeningLuxThreshold=" + this.mDarkeningLuxThreshold);
        pw.println("  mLastObservedLux=" + this.mLastObservedLux);
        pw.println("  mLastObservedLuxTime=" + TimeUtils.formatUptime(this.mLastObservedLuxTime));
        pw.println("  mRecentLightSamples=" + this.mRecentLightSamples);
        pw.println("  mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer);
        pw.println("  mScreenAutoBrightness=" + this.mScreenAutoBrightness);
        pw.println("  mDisplayPolicy=" + DisplayManagerInternal.DisplayPowerRequest.policyToString(this.mDisplayPolicy));
        pw.println("  mShortTermModelAnchor=" + this.mShortTermModelAnchor);
        pw.println("  mShortTermModelValid=" + this.mShortTermModelValid);
        pw.println("  mBrightnessAdjustmentSamplePending=" + this.mBrightnessAdjustmentSamplePending);
        pw.println("  mBrightnessAdjustmentSampleOldLux=" + this.mBrightnessAdjustmentSampleOldLux);
        pw.println("  mBrightnessAdjustmentSampleOldBrightness=" + this.mBrightnessAdjustmentSampleOldBrightness);
        pw.println("  mShortTermModelValid=" + this.mShortTermModelValid);
        pw.println();
        this.mBrightnessMapper.dump(pw);
        pw.println();
        this.mHysteresisLevels.dump(pw);
    }

    /* access modifiers changed from: protected */
    public boolean setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                this.mFirstAutoBrightness = true;
                this.mUpdateAutoBrightnessCount = 0;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                this.mLightSensorEnableElapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
                this.mCurrentLightSensorRate = this.mInitialLightSensorRate;
                this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mCurrentLightSensorRate * 1000, this.mHandler);
                if (this.mWakeupFromSleep) {
                    this.mHandler.sendEmptyMessageAtTime(4, this.mLightSensorEnableTime + 200);
                }
                if (DEBUG) {
                    Slog.d(TAG, "Enable LightSensor at time:mLightSensorEnableTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableElapsedTimeNanos=" + this.mLightSensorEnableElapsedTimeNanos);
                }
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mFirstAutoBrightness = false;
            this.mAmbientLuxValid = !this.mResetAmbientLuxAfterWarmUpConfig;
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            clearFilterAlgoParas();
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.clear();
            }
            this.mCurrentLightSensorRate = -1;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(4);
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            if (DEBUG) {
                Slog.d(TAG, "Disable LightSensor at time:" + SystemClock.uptimeMillis());
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleLightSensorEvent(long time, float lux) {
        Trace.traceCounter(131072, "ALS", (int) lux);
        this.mHandler.removeMessages(1);
        if (this.mAmbientLightRingBuffer.size() == 0) {
            adjustLightSensorRate(this.mNormalLightSensorRate);
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    /* access modifiers changed from: protected */
    public boolean getSetbrightnessImmediateEnableForCaliTest() {
        return this.mSetbrightnessImmediateEnable;
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mRecentLightSamples++;
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
        this.mLastObservedLuxTime = time;
    }

    private void adjustLightSensorRate(int lightSensorRate) {
        if (lightSensorRate != this.mCurrentLightSensorRate) {
            if (DEBUG) {
                Slog.d(TAG, "adjustLightSensorRate: previousRate=" + this.mCurrentLightSensorRate + ", currentRate=" + lightSensorRate);
            }
            this.mCurrentLightSensorRate = lightSensorRate;
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, lightSensorRate * 1000, this.mHandler);
        }
    }

    /* access modifiers changed from: protected */
    public boolean setAutoBrightnessAdjustment(float adjustment) {
        return this.mBrightnessMapper.setAutoBrightnessAdjustment(adjustment);
    }

    private void setAmbientLux(float lux) {
        if (DEBUG) {
            Slog.d(TAG, "setAmbientLux(" + lux + ")");
        }
        if (lux < 0.0f) {
            Slog.w(TAG, "Ambient lux was negative, ignoring and setting to 0");
            lux = 0.0f;
        }
        this.mAmbientLux = lux;
        updatepara(this.mAmbientLightRingBuffer);
        if (NEED_NEW_FILTER_ALGORITHM) {
            updatepara(this.mAmbientLightRingBuffer.calculateStability());
            setDarkenThreshold();
            setBrightenThreshold();
        }
        this.mBrighteningLuxThreshold = this.mHysteresisLevels.getBrighteningThreshold(lux);
        this.mDarkeningLuxThreshold = this.mHysteresisLevels.getDarkeningThreshold(lux);
        if (!this.mShortTermModelValid && this.mShortTermModelAnchor != -1.0f) {
            float minAmbientLux = this.mShortTermModelAnchor - (this.mShortTermModelAnchor * this.SHORT_TERM_MODEL_THRESHOLD_RATIO);
            float maxAmbientLux = this.mShortTermModelAnchor + (this.mShortTermModelAnchor * this.SHORT_TERM_MODEL_THRESHOLD_RATIO);
            if (minAmbientLux >= this.mAmbientLux || this.mAmbientLux >= maxAmbientLux) {
                Slog.d(TAG, "ShortTermModel: reset data, ambient lux is " + this.mAmbientLux + "(" + minAmbientLux + ", " + maxAmbientLux + ")");
                resetShortTermModel();
                return;
            }
            if (DEBUG) {
                Slog.d(TAG, "ShortTermModel: re-validate user data, ambient lux is " + minAmbientLux + " < " + this.mAmbientLux + " < " + maxAmbientLux);
            }
            this.mShortTermModelValid = true;
        }
    }

    private float calculateAmbientLux(long now, long horizon) {
        int endIndex;
        int N;
        AutomaticBrightnessController automaticBrightnessController = this;
        long j = now;
        long j2 = horizon;
        if (DEBUG) {
            Slog.d(TAG, "calculateAmbientLux(" + j + ", " + j2 + ")");
        }
        if (NEED_NEW_FILTER_ALGORITHM) {
            return calculateAmbientLuxForNewPolicy(now);
        }
        int N2 = automaticBrightnessController.mAmbientLightRingBuffer.size();
        if (N2 == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        }
        int endIndex2 = 0;
        long horizonStartTime = j - j2;
        int i = 0;
        while (i < N2 - 1 && automaticBrightnessController.mAmbientLightRingBuffer.getTime(i + 1) <= horizonStartTime) {
            endIndex2++;
            i++;
        }
        if (DEBUG != 0) {
            Slog.d(TAG, "calculateAmbientLux: selected endIndex=" + endIndex2 + ", point=(" + automaticBrightnessController.mAmbientLightRingBuffer.getTime(endIndex2) + ", " + automaticBrightnessController.mAmbientLightRingBuffer.getLux(endIndex2) + ")");
        }
        float sum = 0.0f;
        float totalWeight = 0.0f;
        long endTime = AMBIENT_LIGHT_PREDICTION_TIME_MILLIS;
        int i2 = N2 - 1;
        while (true) {
            if (i2 < endIndex2) {
                int i3 = endIndex2;
                break;
            }
            long eventTime = automaticBrightnessController.mAmbientLightRingBuffer.getTime(i2);
            if (i2 == endIndex2 && eventTime < horizonStartTime) {
                eventTime = horizonStartTime;
            }
            long startTime = eventTime - j;
            float weight = automaticBrightnessController.calculateWeight(startTime, endTime);
            if (weight < 0.0f) {
                int i4 = N2;
                int i5 = endIndex2;
                break;
            }
            float lux = automaticBrightnessController.mAmbientLightRingBuffer.getLux(i2);
            if (DEBUG) {
                N = N2;
                StringBuilder sb = new StringBuilder();
                endIndex = endIndex2;
                sb.append("calculateAmbientLux: [");
                sb.append(startTime);
                sb.append(", ");
                sb.append(endTime);
                sb.append("]: lux=");
                sb.append(lux);
                sb.append(", weight=");
                sb.append(weight);
                Slog.d(TAG, sb.toString());
            } else {
                N = N2;
                endIndex = endIndex2;
            }
            totalWeight += weight;
            sum += lux * weight;
            endTime = startTime;
            i2--;
            N2 = N;
            endIndex2 = endIndex;
            automaticBrightnessController = this;
            j = now;
            long j3 = horizon;
        }
        if (DEBUG) {
            Slog.d(TAG, "calculateAmbientLux: totalWeight=" + totalWeight + ", newAmbientLux=" + (sum / totalWeight));
        }
        return sum / totalWeight;
    }

    private float calculateAmbientLuxForNewPolicy(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        } else if (N < 5) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMin = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMax = this.mAmbientLightRingBuffer.getLux(N - 1);
            for (int i = N - 2; i >= (N - 1) - 4; i--) {
                if (luxMin > this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMin = this.mAmbientLightRingBuffer.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMax = this.mAmbientLightRingBuffer.getLux(i);
                }
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA;
        }
    }

    private float calculateWeight(long startDelta, long endDelta) {
        return weightIntegral(endDelta) - weightIntegral(startDelta);
    }

    private float weightIntegral(long x) {
        return ((float) x) * ((((float) x) * 0.5f * SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA) + ((float) this.mWeightingIntercept));
    }

    private void updatepara(float stability) {
        Stability = stability;
        if (Stability > 100.0f) {
            Stability = 100.0f;
            DarkenDebounceTimeParaBig = 1.0f;
            BRIGHTENING_LIGHT_HYSTERESIS = Stability / 100.0f;
        } else if (Stability < 5.0f) {
            Stability = 5.0f;
            DarkenDebounceTimeParaBig = 0.1f;
            BRIGHTENING_LIGHT_HYSTERESIS = Stability / 100.0f;
        } else {
            DarkenDebounceTimeParaBig = 1.0f;
            BRIGHTENING_LIGHT_HYSTERESIS = Stability / 100.0f;
        }
        BRIGHTENING_LIGHT_DEBOUNCE = (long) (((double) (800.0f * BrightenDebounceTimeParaBig)) * ((((double) (BrightenDebounceTimePara * Stability)) / 100.0d) + 1.0d));
        this.DARKENING_LIGHT_DEBOUNCE = (long) (4000.0d * (1.0d + (((double) (DarkenDebounceTimePara * Stability)) / 100.0d)));
    }

    private void setDarkenThreshold() {
        if (this.mAmbientLux >= 1000.0f) {
            DarkenDeltaLuxMax = this.mAmbientLux - 500.0f;
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
            Stability = 5.0f;
        } else if (this.mAmbientLux >= 500.0f) {
            DarkenDeltaLuxMax = Math.min(this.mAmbientLux - 100.0f, 500.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
            Stability = 5.0f;
        } else if (this.mAmbientLux >= 100.0f) {
            DarkenDeltaLuxMax = Math.min(this.mAmbientLux - 10.0f, 400.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        } else if (this.mAmbientLux >= 10.0f) {
            DarkenDeltaLuxMax = Math.min(this.mAmbientLux - 5.0f, 95.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        } else {
            DarkenDeltaLuxMax = Math.min(this.mAmbientLux, 5.0f);
            DarkenDeltaLuxMin = DarkenDeltaLuxMax;
        }
        DarkenDeltaLuxMax *= 1.0f + ((DarkenDeltaLuxPara * (Stability - 5.0f)) / 100.0f);
    }

    public void setBrightenThreshold() {
        if (this.mAmbientLux >= 1000.0f) {
            BrightenDeltaLuxMax = 989.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 500.0f) {
            BrightenDeltaLuxMax = (0.5f * this.mAmbientLux) + 489.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 100.0f) {
            BrightenDeltaLuxMax = (0.5f * this.mAmbientLux) + 489.0f;
            BrightenDeltaLuxMin = (1.375f * this.mAmbientLux) + 51.5f;
        } else if (this.mAmbientLux >= 10.0f) {
            BrightenDeltaLuxMax = Math.min((20.0f * this.mAmbientLux) - 181.0f, (4.0f * this.mAmbientLux) + 139.0f);
            BrightenDeltaLuxMin = Math.min((this.mAmbientLux * 5.0f) - 31.0f, (1.5f * this.mAmbientLux) + 39.0f);
        } else if (this.mAmbientLux >= 2.0f) {
            BrightenDeltaLuxMax = (0.5f * this.mAmbientLux) + 14.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else {
            BrightenDeltaLuxMax = 15.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        }
        BrightenDeltaLuxMax *= 1.0f + ((BrightenDeltaLuxPara * (Stability - 5.0f)) / 100.0f);
    }

    /* access modifiers changed from: protected */
    public long nextAmbientLightBrighteningTransition(long time) {
        if (NEED_NEW_FILTER_ALGORITHM) {
            return nextAmbientLightBrighteningTransitionForNewPolicy(time);
        }
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && this.mAmbientLightRingBuffer.getLux(i) > this.mBrighteningLuxThreshold) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long nextAmbientLightBrighteningTransitionForNewPolicy(long time) {
        boolean BrightenChange;
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            float BrightenDeltaLux = this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux;
            if (BrightenDeltaLux > BrightenDeltaLuxMax) {
                BrightenChange = true;
            } else if (BrightenDeltaLux <= BrightenDeltaLuxMin || Stability >= 50.0f) {
                BrightenChange = false;
            } else {
                BrightenChange = true;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return this.mBrighteningLightDebounceConfig + earliestValidTime;
    }

    /* access modifiers changed from: protected */
    public long nextAmbientLightDarkeningTransition(long time) {
        if (NEED_NEW_FILTER_ALGORITHM) {
            return nextAmbientLightDarkeningTransitionForNewPolicy(time);
        }
        long earliestValidTime = time;
        int i = this.mAmbientLightRingBuffer.size() - 1;
        while (i >= 0 && this.mAmbientLightRingBuffer.getLux(i) < this.mDarkeningLuxThreshold) {
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
            i--;
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    private long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        boolean DarkenChange;
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            float DeltaLux = this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i);
            Slog.d("filter", " mAmbientLux =" + this.mAmbientLux + ",mAmbientLightRingBufferFilter.getLux(i)=" + this.mAmbientLightRingBufferFilter.getLux(i) + ", Stability=" + Stability);
            if (DeltaLux >= DarkenDeltaLuxMax && Stability < 15.0f) {
                DarkenChange = true;
            } else if (DeltaLux < DarkenDeltaLuxMin || Stability >= 5.0f) {
                DarkenChange = false;
            } else {
                DarkenChange = true;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return this.mDarkeningLightDebounceConfig + earliestValidTime;
    }

    /* access modifiers changed from: protected */
    public void updateAmbientLux() {
        long time = SystemClock.uptimeMillis();
        this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        updateAmbientLux(time);
    }

    private void updateAmbientLux(long time) {
        boolean needToBrighten;
        long nextTransitionTime;
        boolean needToBrighten2;
        boolean needToDarken;
        long j = time;
        boolean needToDarken2 = false;
        if (!this.mAmbientLuxValid) {
            long timeWhenSensorWarmedUp = ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime;
            if (j < timeWhenSensorWarmedUp) {
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientLux: Sensor not  ready yet: time=" + j + ", timeWhenSensorWarmedUp=" + timeWhenSensorWarmedUp);
                }
                this.mHandler.sendEmptyMessageAtTime(1, timeWhenSensorWarmedUp);
                return;
            }
            float ambientLux = calculateAmbientLux(j, 2000);
            updateBuffer(j, ambientLux, 10000);
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.push(j, ambientLux);
                this.mAmbientLightRingBufferFilter.prune(j - ((long) this.mAmbientLightHorizon));
            }
            setAmbientLux(ambientLux);
            this.mAmbientLuxValid = true;
            if (this.mWakeupFromSleep) {
                this.mWakeupFromSleep = false;
                this.mFirstAutoBrightness = true;
            }
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
        float ambientLux2 = calculateAmbientLux(j, 2000);
        updateBuffer(j, ambientLux2, 10000);
        if (NEED_NEW_FILTER_ALGORITHM) {
            this.mAmbientLightRingBufferFilter.push(j, ambientLux2);
            this.mAmbientLightRingBufferFilter.prune(j - ((long) this.mAmbientLightHorizon));
            updatepara(this.mAmbientLightRingBuffer.calculateStability());
            setDarkenThreshold();
            setBrightenThreshold();
        }
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        float slowAmbientLux = calculateAmbientLux(j, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        float fastAmbientLux = calculateAmbientLux(j, 2000);
        if (NEED_NEW_FILTER_ALGORITHM) {
            if (slowAmbientLux - this.mAmbientLux >= BrightenDeltaLuxMax) {
                needToBrighten2 = true;
            } else if (slowAmbientLux - this.mAmbientLux < BrightenDeltaLuxMin || Stability >= 50.0f) {
                needToBrighten2 = false;
            } else {
                needToBrighten2 = true;
            }
            needToBrighten = needToBrighten2 && nextBrightenTransition <= j;
            if (this.mAmbientLux - slowAmbientLux >= DarkenDeltaLuxMax) {
                needToDarken = true;
            } else if (this.mAmbientLux - slowAmbientLux < DarkenDeltaLuxMin || Stability >= 5.0f) {
                needToDarken = false;
            } else {
                needToDarken = true;
            }
            if (needToDarken && nextDarkenTransition <= j) {
                needToDarken2 = true;
            }
        } else {
            needToBrighten = slowAmbientLux >= this.mBrighteningLuxThreshold && fastAmbientLux >= this.mBrighteningLuxThreshold && nextBrightenTransition <= j;
            if (slowAmbientLux <= this.mDarkeningLuxThreshold && fastAmbientLux <= this.mDarkeningLuxThreshold && nextDarkenTransition <= j) {
                needToDarken2 = true;
            }
        }
        if (needToBrighten || needToDarken2) {
            setAmbientLux(fastAmbientLux);
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("updateAmbientLux: ");
                sb.append(fastAmbientLux > this.mAmbientLux ? "Brightened" : "Darkened");
                sb.append(": mBrighteningLuxThreshold=");
                sb.append(this.mBrighteningLuxThreshold);
                sb.append(", mAmbientLightRingBuffer=");
                sb.append(this.mAmbientLightRingBuffer);
                sb.append(", mAmbientLux=");
                sb.append(this.mAmbientLux);
                Slog.d(TAG, sb.toString());
            }
            updateAutoBrightness(true);
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        long nextTransitionTime2 = Math.min(nextDarkenTransition, nextBrightenTransition);
        if (nextTransitionTime2 > j) {
            long j2 = nextBrightenTransition;
            nextTransitionTime = nextTransitionTime2;
        } else {
            long j3 = nextBrightenTransition;
            nextTransitionTime = ((long) this.mNormalLightSensorRate) + j;
        }
        boolean z = DEBUG;
        this.mHandler.sendEmptyMessageAtTime(1, nextTransitionTime);
    }

    /* access modifiers changed from: protected */
    public void updateAutoBrightness(boolean sendUpdate) {
        if (!this.mAmbientLuxValid) {
            if (DEBUG) {
                Slog.d(TAG, "mAmbientLuxValid= false,sensor is not ready");
            }
            return;
        }
        int newScreenAutoBrightness = getAdjustLightValByPgMode(getPowerSavingBrightness(clampScreenBrightness(Math.round(255.0f * this.mScreenAutoBrightnessSpline.interpolate(this.mAmbientLux)))));
        if (this.mScreenAutoBrightness != newScreenAutoBrightness || this.mFirstAutoBrightness || this.mFirstBrightnessAfterProximityNegative) {
            if (DEBUG) {
                Slog.d(TAG, "updateAutoBrightness: mScreenAutoBrightness=" + this.mScreenAutoBrightness + ", newScreenAutoBrightness=" + newScreenAutoBrightness);
            }
            if (newScreenAutoBrightness > this.mScreenAutoBrightness) {
                this.mBrightnessEnlarge = true;
            } else {
                this.mBrightnessEnlarge = false;
            }
            this.mScreenAutoBrightness = newScreenAutoBrightness;
            this.mFirstAutoBrightness = false;
            this.mFirstBrightnessAfterProximityNegative = false;
            this.mUpdateAutoBrightnessCount++;
            if (this.mUpdateAutoBrightnessCount == Integer.MAX_VALUE) {
                this.mUpdateAutoBrightnessCount = 2;
                Slog.i(TAG, "mUpdateAutoBrightnessCount == Integer.MAX_VALUE,so set it be 2");
            }
            if (sendUpdate) {
                this.mCallbacks.updateBrightness();
            }
        }
    }

    public void updateAutoDBWhenSameBrightness(int brightness) {
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void prepareBrightnessAdjustmentSample() {
        if (!this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = true;
            this.mBrightnessAdjustmentSampleOldLux = this.mAmbientLuxValid ? this.mAmbientLux : -1.0f;
            this.mBrightnessAdjustmentSampleOldBrightness = this.mScreenAutoBrightness;
        } else {
            this.mHandler.removeMessages(2);
        }
        this.mHandler.sendEmptyMessageDelayed(2, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    private void cancelBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            this.mHandler.removeMessages(2);
        }
    }

    /* access modifiers changed from: private */
    public void collectBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            if (this.mAmbientLuxValid && this.mScreenAutoBrightness >= 0) {
                if (DEBUG) {
                    Slog.d(TAG, "Auto-brightness adjustment changed by user: lux=" + this.mAmbientLux + ", brightness=" + this.mScreenAutoBrightness + ", ring=" + this.mAmbientLightRingBuffer);
                }
                EventLog.writeEvent(EventLogTags.AUTO_BRIGHTNESS_ADJ, new Object[]{Float.valueOf(this.mBrightnessAdjustmentSampleOldLux), Integer.valueOf(this.mBrightnessAdjustmentSampleOldBrightness), Float.valueOf(this.mAmbientLux), Integer.valueOf(this.mScreenAutoBrightness)});
            }
        }
    }

    /* access modifiers changed from: protected */
    public float getBrighteningLuxThreshold() {
        return this.mBrighteningLuxThreshold;
    }

    /* access modifiers changed from: protected */
    public float getDarkeningLuxThreshold() {
        return this.mDarkeningLuxThreshold;
    }

    /* access modifiers changed from: protected */
    public float getDarkeningLightHystersis() {
        return DARKENING_LIGHT_HYSTERESIS;
    }

    /* access modifiers changed from: protected */
    public float getBrighteningLightHystersis() {
        return BRIGHTENING_LIGHT_HYSTERESIS;
    }

    /* access modifiers changed from: protected */
    public boolean calcNeedToBrighten(float ambient) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean calcNeedToDarken(float ambient) {
        return true;
    }

    /* access modifiers changed from: protected */
    public long getNextAmbientLightBrighteningTime(long earliedtime) {
        return BRIGHTENING_LIGHT_DEBOUNCE + earliedtime;
    }

    /* access modifiers changed from: protected */
    public long getNextAmbientLightDarkeningTime(long earliedtime) {
        return this.DARKENING_LIGHT_DEBOUNCE + earliedtime;
    }

    public void setPowerStatus(boolean powerStatus) {
    }

    /* access modifiers changed from: protected */
    public boolean interceptHandleLightSensorEvent(long time, float lux) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateIntervenedAutoBrightness(int brightness) {
        this.mScreenAutoBrightness = brightness;
        if (DEBUG) {
            Slog.d(TAG, "update IntervenedAutoBrightness:mScreenAutoBrightness = " + this.mScreenAutoBrightness);
        }
    }

    /* access modifiers changed from: protected */
    public void clearFilterAlgoParas() {
    }

    /* access modifiers changed from: protected */
    public void updatepara(AmbientLightRingBuffer mAmbientLightRingBuffer2) {
    }

    /* access modifiers changed from: protected */
    public void updateBuffer(long time, float ambientLux, int horizon) {
    }

    /* access modifiers changed from: protected */
    public boolean decideToBrighten(float ambientLux) {
        return ambientLux >= this.mBrighteningLuxThreshold && calcNeedToBrighten(ambientLux);
    }

    /* access modifiers changed from: protected */
    public boolean decideToDarken(float ambientLux) {
        return ambientLux <= this.mDarkeningLuxThreshold && calcNeedToDarken(ambientLux);
    }

    /* access modifiers changed from: protected */
    public float getLuxStability() {
        return 0.0f;
    }

    public long getLightSensorEnableTime() {
        return this.mLightSensorEnableTime;
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessIfNoAmbientLuxReported() {
    }

    public int getUpdateAutoBrightnessCount() {
        return this.mUpdateAutoBrightnessCount;
    }

    public void updateCurrentUserId(int userId) {
    }

    /* access modifiers changed from: protected */
    public SensorManager getSensorManager() {
        return this.mSensorManager;
    }

    public void updatePowerPolicy(int policy) {
    }

    public boolean getPowerStatus() {
        return false;
    }

    public void setCoverModeStatus(boolean isclosed) {
    }

    public boolean getCoverModeFastResponseFlag() {
        return false;
    }

    public void setBackSensorCoverModeBrightness(int brightness) {
    }

    public void setCameraModeBrightnessLineEnable(boolean cameraModeBrightnessLineEnable) {
    }

    public boolean getCameraModeChangeAnimationEnable() {
        return false;
    }

    public boolean getReadingModeChangeAnimationEnable() {
        return false;
    }

    public boolean getReadingModeBrightnessLineEnable() {
        return false;
    }

    public boolean getRebootAutoModeEnable() {
        return false;
    }

    public void setKeyguardLockedStatus(boolean isLocked) {
    }

    public boolean getOutdoorAnimationFlag() {
        return false;
    }

    public int getCoverModeBrightnessFromLastScreenBrightness() {
        return 0;
    }

    public void setMaxBrightnessFromThermal(int brightness) {
    }

    public void setMaxBrightnessFromCryogenic(int brightness) {
    }

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        return brightness;
    }

    public void setManualModeEnableForPg(boolean manualModeEnableForPg) {
    }

    public boolean getRebootFirstBrightnessAnimationEnable() {
        return false;
    }

    /* access modifiers changed from: protected */
    public int getAdjustLightValByPgMode(int rawLightVal) {
        return rawLightVal;
    }

    /* access modifiers changed from: protected */
    public int getPowerSavingBrightness(int brightness) {
        return brightness;
    }

    /* access modifiers changed from: protected */
    public void setBrightnessLimitedByThermal(boolean isLimited) {
    }

    public void setBrightnessNoLimit(int brightness, int time) {
    }

    public int getAutoBrightnessBaseInOutDoorLimit(int brightness) {
        return brightness;
    }

    public boolean getDarkAdaptDimmingEnable() {
        return false;
    }

    public void clearDarkAdaptDimmingEnable() {
    }

    public boolean getAutoPowerSavingUseManualAnimationTimeEnable() {
        return false;
    }

    public boolean getAutoPowerSavingAnimationEnable() {
        return false;
    }

    public void setAutoPowerSavingAnimationEnable(boolean enable) {
    }

    public void getUserDragInfo(Bundle data) {
    }

    public void setPersonalizedBrightnessCurveLevel(int curveLevel) {
    }

    public void updateNewBrightnessCurveTmp() {
    }

    public void updateNewBrightnessCurve() {
    }

    public List<PointF> getCurrentDefaultNewCurveLine() {
        return null;
    }

    public boolean getAnimationGameChangeEnable() {
        return false;
    }

    public float getAutomaticScreenBrightnessAdjustmentNew(int brightness) {
        return 0.0f;
    }

    public void updateBrightnessModeChangeManualState(boolean enable) {
    }

    public boolean getFastDarkenDimmingEnable() {
        return false;
    }

    public void setVideoPlayStatus(boolean isVideoPlay) {
    }
}

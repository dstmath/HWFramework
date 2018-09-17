package com.android.server.display;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager.BacklightBrightness;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.EventLog;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import android.util.TimeUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;

public class AutomaticBrightnessController {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final long AMBIENT_LIGHT_PREDICTION_TIME_MILLIS = 100;
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
    private static final boolean DEBUG;
    private static final boolean DEBUG_CONTROLLER = false;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    private static float DarkenDebounceTimePara = 1.0f;
    private static float DarkenDebounceTimeParaBig = 0.0f;
    private static float DarkenDeltaLuxMax = 0.0f;
    private static float DarkenDeltaLuxMin = 0.0f;
    private static float DarkenDeltaLuxPara = 1.0f;
    private static final int INT_BRIGHTNESS_COVER_MODE = SystemProperties.getInt("ro.config.hw_cover_brightness", 60);
    private static final int LIGHT_SENSOR_RATE_MILLIS = 300;
    private static final int MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE = 2;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final int MSG_UPDATE_BRIGHTNESS = 3;
    private static final boolean NEED_NEW_FILTER_ALGORITHM = needNewFilterAlgorithm();
    private static final float SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA = 3.0f;
    private static float Stability = 0.0f;
    private static final String TAG = "AutomaticBrightnessController";
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = false;
    protected long DARKENING_LIGHT_DEBOUNCE = 8000;
    private final int mAmbientLightHorizon;
    protected AmbientLightRingBuffer mAmbientLightRingBuffer;
    private AmbientLightRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    protected boolean mAmbientLuxValid;
    private final long mBrighteningLightDebounceConfig;
    private float mBrighteningLuxThreshold;
    private float mBrightnessAdjustmentSampleOldAdjustment;
    private int mBrightnessAdjustmentSampleOldBrightness;
    private float mBrightnessAdjustmentSampleOldGamma;
    private float mBrightnessAdjustmentSampleOldLux;
    private boolean mBrightnessAdjustmentSamplePending;
    protected boolean mBrightnessEnlarge = false;
    protected final Callbacks mCallbacks;
    private int mCurrentLightSensorRate;
    private final long mDarkeningLightDebounceConfig;
    private float mDarkeningLuxThreshold;
    private final float mDozeScaleFactor;
    private boolean mDozing;
    private final HysteresisLevels mDynamicHysteresis;
    protected boolean mFirstAutoBrightness;
    protected boolean mFirstBrightnessAfterProximityNegative = false;
    private AutomaticBrightnessHandler mHandler;
    private AmbientLightRingBuffer mInitialHorizonAmbientLightRingBuffer;
    private final int mInitialLightSensorRate;
    private float mLastObservedLux;
    private long mLastObservedLuxTime;
    private float mLastScreenAutoBrightnessGamma = 1.0f;
    private final Sensor mLightSensor;
    protected long mLightSensorEnableElapsedTimeNanos;
    protected long mLightSensorEnableTime;
    protected boolean mLightSensorEnabled;
    private final SensorEventListener mLightSensorListener = new SensorEventListener() {
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
    protected int mMaxBrightnessSetByThermal = 255;
    private final int mNormalLightSensorRate;
    private long mPrintLogTime = 0;
    private int mRecentLightSamples;
    private final boolean mResetAmbientLuxAfterWarmUpConfig;
    protected int mScreenAutoBrightness = -1;
    private float mScreenAutoBrightnessAdjustment = 0.0f;
    private float mScreenAutoBrightnessAdjustmentMaxGamma;
    private final Spline mScreenAutoBrightnessSpline;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private int mScreenBrightnessRangeSetByAppMax;
    private int mScreenBrightnessRangeSetByAppMin;
    private final SensorManager mSensorManager;
    protected boolean mSetbrightnessImmediateEnable = false;
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
                buf.append("/");
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
            index += this.mStart;
            if (index >= this.mCapacity) {
                return index - this.mCapacity;
            }
            return index;
        }

        public float calculateStability() {
            if (this.mCount == 0) {
                return 0.0f;
            }
            int index1;
            int index2;
            float tmp;
            float Stability1;
            float Stability2;
            float currentLux = getLux(this.mCount - 1);
            calculateAvg();
            float luxT1 = currentLux;
            float luxT2 = currentLux;
            int T1 = 0;
            int T2 = 0;
            int index = 0;
            float luxT1Min = currentLux;
            float luxT2Min = currentLux;
            int indexMin = 0;
            float luxT1Max = currentLux;
            float luxT2Max = currentLux;
            int indexMax = 0;
            for (int j = 0; j < this.mCount - 1; j++) {
                Object obj;
                float lux1 = getLux((this.mCount - 1) - j);
                float lux2 = getLux(((this.mCount - 1) - j) - 1);
                if ((mLuxBufferAvg <= lux1 && mLuxBufferAvg >= lux2) || (mLuxBufferAvg >= lux1 && mLuxBufferAvg <= lux2)) {
                    obj = (mLuxBufferAvg == lux1 && mLuxBufferAvg == lux2) ? 1 : null;
                    if (obj == null) {
                        luxT1 = lux1;
                        luxT2 = lux2;
                        T1 = (this.mCount - 1) - j;
                        T2 = ((this.mCount - 1) - j) - 1;
                        index = j;
                    }
                }
                if ((mLuxBufferAvgMin <= lux1 && mLuxBufferAvgMin >= lux2) || (mLuxBufferAvgMin >= lux1 && mLuxBufferAvgMin <= lux2)) {
                    obj = (mLuxBufferAvgMin == lux1 && mLuxBufferAvgMin == lux2) ? 1 : null;
                    if (obj == null) {
                        luxT1Min = lux1;
                        luxT2Min = lux2;
                        int T1Min = (this.mCount - 1) - j;
                        int T2Min = ((this.mCount - 1) - j) - 1;
                        indexMin = j;
                    }
                }
                if ((mLuxBufferAvgMax <= lux1 && mLuxBufferAvgMax >= lux2) || (mLuxBufferAvgMax >= lux1 && mLuxBufferAvgMax <= lux2)) {
                    obj = (mLuxBufferAvgMax == lux1 && mLuxBufferAvgMax == lux2) ? 1 : null;
                    if (obj == null) {
                        luxT1Max = lux1;
                        luxT2Max = lux2;
                        int T1Max = (this.mCount - 1) - j;
                        int T2Max = ((this.mCount - 1) - j) - 1;
                        indexMax = j;
                    }
                }
                if (index != 0 && ((indexMin != 0 || indexMax != 0) && ((index <= indexMin && index >= indexMax) || (index >= indexMin && index <= indexMax)))) {
                    break;
                }
            }
            if (indexMax <= indexMin) {
                index1 = indexMax;
                index2 = indexMin;
            } else {
                index1 = indexMin;
                index2 = indexMax;
            }
            int k1 = (this.mCount - 1) - index1;
            while (k1 <= this.mCount - 1 && k1 != this.mCount - 1) {
                float luxk1 = getLux(k1);
                float luxk2 = getLux(k1 + 1);
                if (indexMax > indexMin) {
                    if (luxk1 <= luxk2) {
                        break;
                    }
                } else if (luxk1 >= luxk2) {
                    break;
                }
                T1 = k1 + 1;
                k1++;
            }
            int k3 = (this.mCount - 1) - index2;
            while (k3 >= 0 && k3 != 0) {
                float luxk3 = getLux(k3);
                float luxk4 = getLux(k3 - 1);
                if (indexMax > indexMin) {
                    if (luxk3 >= luxk4) {
                        break;
                    }
                } else if (luxk3 <= luxk4) {
                    break;
                }
                T2 = k3 - 1;
                k3--;
            }
            int t1 = (this.mCount - 1) - T1;
            int t2 = T2;
            float s1 = calculateStabilityFactor(T1, this.mCount - 1);
            float avg1 = calcluateAvg(T1, this.mCount - 1);
            float s2 = calculateStabilityFactor(0, T2);
            float deltaAvg = Math.abs(avg1 - calcluateAvg(0, T2));
            float k = Math.abs((getLux(T1) - getLux(T2)) / ((float) (T1 - T2)));
            if (k < 10.0f / (5.0f + k)) {
                tmp = k;
            } else {
                tmp = 10.0f / (5.0f + k);
            }
            if (tmp > 20.0f / (10.0f + deltaAvg)) {
                tmp = 20.0f / (10.0f + deltaAvg);
            }
            if (t1 > 20) {
                Stability1 = s1;
            } else {
                float a1 = (float) Math.exp((double) (t1 - 20));
                float b1 = (float) (20 - t1);
                float s3 = tmp;
                Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
            }
            if (t2 > 10) {
                Stability2 = s2;
            } else {
                float a2 = (float) Math.exp((double) (t2 - 10));
                float b2 = (float) (10 - t2);
                float s4 = tmp;
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
            int i;
            for (i = start; i <= end; i++) {
                sum += getLux(i);
            }
            float avg = sum / ((float) size);
            for (i = start; i <= end; i++) {
                sigma += (getLux(i) - avg) * (getLux(i) - avg);
            }
            float ss = sigma / ((float) (size - 1));
            if (avg == 0.0f) {
                return 0.0f;
            }
            return ss / avg;
        }
    }

    private final class AutomaticBrightnessHandler extends Handler {
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

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public void setBacklightBrightness(BacklightBrightness backlightBrightness) {
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
        String boardname = readFileByChars("/proc/device-tree/hisi,boardname").trim();
        String productName = "";
        if (boardname == null) {
            return "default";
        }
        String[] arrays = boardname.split("_");
        if (arrays == null || arrays.length < 2) {
            productName = "default";
        } else {
            productName = arrays[0] + "_" + arrays[1];
        }
        return productName.toLowerCase();
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0086 A:{SYNTHETIC, Splitter: B:19:0x0086} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009c A:{SYNTHETIC, Splitter: B:31:0x009c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String readFileByChars(String fileName) {
        IOException e1;
        Throwable th;
        File file = new File(fileName);
        if (file.exists() && (file.canRead() ^ 1) == 0) {
            Reader reader = null;
            char[] tempChars = new char[512];
            StringBuilder sb = new StringBuilder();
            try {
                Reader reader2 = new InputStreamReader(new FileInputStream(fileName));
                while (true) {
                    try {
                        int charRead = reader2.read(tempChars);
                        if (charRead == -1) {
                            break;
                        }
                        sb.append(tempChars, 0, charRead);
                    } catch (IOException e) {
                        e1 = e;
                        reader = reader2;
                        try {
                            Slog.e(TAG, "read file name error, file name is:" + fileName);
                            e1.printStackTrace();
                            if (reader != null) {
                            }
                            return sb.toString();
                        } catch (Throwable th2) {
                            th = th2;
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e2) {
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        reader = reader2;
                        if (reader != null) {
                        }
                        throw th;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                e1 = e4;
                Slog.e(TAG, "read file name error, file name is:" + fileName);
                e1.printStackTrace();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e5) {
                    }
                }
                return sb.toString();
            }
            return sb.toString();
        }
        Slog.d(TAG, "file is exists : " + file.exists() + " file can read : " + file.canRead());
        return "";
    }

    public AutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, int initialLightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, int ambientLightHorizon, float autoBrightnessAdjustmentMaxGamma, HysteresisLevels dynamicHysteresis) {
        this.mCallbacks = callbacks;
        this.mSensorManager = sensorManager;
        this.mScreenAutoBrightnessSpline = autoBrightnessSpline;
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
        this.mAmbientLightHorizon = ambientLightHorizon;
        this.mWeightingIntercept = ambientLightHorizon;
        this.mScreenAutoBrightnessAdjustmentMaxGamma = autoBrightnessAdjustmentMaxGamma;
        this.mDynamicHysteresis = dynamicHysteresis;
        this.mHandler = new AutomaticBrightnessHandler(looper);
        this.mAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        if (NEED_NEW_FILTER_ALGORITHM) {
            this.mAmbientLightRingBufferFilter = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        }
        this.mInitialHorizonAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mNormalLightSensorRate, this.mAmbientLightHorizon);
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
    }

    public int getAutomaticScreenBrightness() {
        int brightness = this.mScreenAutoBrightness;
        if (brightness >= 0) {
            brightness = MathUtils.constrain(brightness, this.mScreenBrightnessRangeSetByAppMin, this.mScreenBrightnessRangeSetByAppMax);
        }
        if (brightness > this.mMaxBrightnessSetByThermal) {
            brightness = this.mMaxBrightnessSetByThermal;
        }
        if (this.mDozing) {
            return (int) (((float) brightness) * this.mDozeScaleFactor);
        }
        return brightness;
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange) {
        boolean z;
        this.mDozing = dozing;
        if (enable) {
            z = dozing ^ 1;
        } else {
            z = false;
        }
        if (setLightSensorEnabled(z) | setScreenAutoBrightnessAdjustment(adjustment)) {
            updateAutoBrightness(false);
        }
        if (enable && (dozing ^ 1) != 0 && userInitiatedChange) {
            prepareBrightnessAdjustmentSample();
        }
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Automatic Brightness Controller Configuration:");
        pw.println("  mScreenAutoBrightnessSpline=" + this.mScreenAutoBrightnessSpline);
        pw.println("  mScreenBrightnessRangeMinimum=" + this.mScreenBrightnessRangeMinimum);
        pw.println("  mScreenBrightnessRangeMaximum=" + this.mScreenBrightnessRangeMaximum);
        pw.println("  mScreenBrightnessRangeSetByAppMin=" + this.mScreenBrightnessRangeSetByAppMin);
        pw.println("  mScreenBrightnessRangeSetByAppMax=" + this.mScreenBrightnessRangeSetByAppMax);
        pw.println("  mLightSensorWarmUpTimeConfig=" + this.mLightSensorWarmUpTimeConfig);
        pw.println("  mBrighteningLightDebounceConfig=" + this.mBrighteningLightDebounceConfig);
        pw.println("  mDarkeningLightDebounceConfig=" + this.mDarkeningLightDebounceConfig);
        pw.println("  mResetAmbientLuxAfterWarmUpConfig=" + this.mResetAmbientLuxAfterWarmUpConfig);
        pw.println();
        pw.println("Automatic Brightness Controller State:");
        pw.println("  mLightSensor=" + this.mLightSensor);
        pw.println("  mLightSensorEnabled=" + this.mLightSensorEnabled);
        pw.println("  mLightSensorEnableTime=" + TimeUtils.formatUptime(this.mLightSensorEnableTime));
        pw.println("  mAmbientLux=" + this.mAmbientLux);
        pw.println("  mAmbientLightHorizon=" + this.mAmbientLightHorizon);
        pw.println("  mBrighteningLuxThreshold=" + this.mBrighteningLuxThreshold);
        pw.println("  mDarkeningLuxThreshold=" + this.mDarkeningLuxThreshold);
        pw.println("  mLastObservedLux=" + this.mLastObservedLux);
        pw.println("  mLastObservedLuxTime=" + TimeUtils.formatUptime(this.mLastObservedLuxTime));
        pw.println("  mRecentLightSamples=" + this.mRecentLightSamples);
        pw.println("  mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer);
        pw.println("  mInitialHorizonAmbientLightRingBuffer=" + this.mInitialHorizonAmbientLightRingBuffer);
        pw.println("  mScreenAutoBrightness=" + this.mScreenAutoBrightness);
        pw.println("  mScreenAutoBrightnessAdjustment=" + this.mScreenAutoBrightnessAdjustment);
        pw.println("  mScreenAutoBrightnessAdjustmentMaxGamma=" + this.mScreenAutoBrightnessAdjustmentMaxGamma);
        pw.println("  mLastScreenAutoBrightnessGamma=" + this.mLastScreenAutoBrightnessGamma);
        pw.println("  mDozing=" + this.mDozing);
    }

    private boolean setLightSensorEnabled(boolean enable) {
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
                    this.mHandler.sendEmptyMessageAtTime(3, this.mLightSensorEnableTime + 200);
                }
                if (DEBUG) {
                    Slog.d(TAG, "Enable LightSensor at time:mLightSensorEnableTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableElapsedTimeNanos=" + this.mLightSensorEnableElapsedTimeNanos);
                }
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = false;
            this.mFirstAutoBrightness = false;
            this.mAmbientLuxValid = this.mResetAmbientLuxAfterWarmUpConfig ^ 1;
            this.mRecentLightSamples = 0;
            this.mAmbientLightRingBuffer.clear();
            clearFilterAlgoParas();
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.clear();
            }
            this.mInitialHorizonAmbientLightRingBuffer.clear();
            this.mCurrentLightSensorRate = -1;
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(3);
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            if (DEBUG) {
                Slog.d(TAG, "Disable LightSensor at time:" + SystemClock.uptimeMillis());
            }
        }
        return false;
    }

    protected void handleLightSensorEvent(long time, float lux) {
        this.mHandler.removeMessages(1);
        if (this.mAmbientLightRingBuffer.size() == 0) {
            adjustLightSensorRate(this.mNormalLightSensorRate);
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    protected boolean getSetbrightnessImmediateEnableForCaliTest() {
        return this.mSetbrightnessImmediateEnable;
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mRecentLightSamples++;
        if (time <= this.mLightSensorEnableTime + ((long) this.mAmbientLightHorizon)) {
            this.mInitialHorizonAmbientLightRingBuffer.push(time, lux);
        }
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

    protected boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        if (adjustment == this.mScreenAutoBrightnessAdjustment) {
            return false;
        }
        this.mScreenAutoBrightnessAdjustment = adjustment;
        return true;
    }

    private void setAmbientLux(float lux) {
        this.mAmbientLux = lux;
        updatepara(this.mAmbientLightRingBuffer);
        if (NEED_NEW_FILTER_ALGORITHM) {
            updatepara(this.mAmbientLightRingBuffer.calculateStability());
            setDarkenThreshold();
            setBrightenThreshold();
        }
        this.mBrighteningLuxThreshold = this.mDynamicHysteresis.getBrighteningThreshold(lux);
        this.mDarkeningLuxThreshold = this.mDynamicHysteresis.getDarkeningThreshold(lux);
    }

    protected float calculateAmbientLux(long now) {
        if (NEED_NEW_FILTER_ALGORITHM) {
            return calculateAmbientLuxForNewPolicy(now);
        }
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        }
        float sum = 0.0f;
        float totalWeight = 0.0f;
        long endTime = AMBIENT_LIGHT_PREDICTION_TIME_MILLIS;
        for (int i = N - 1; i >= 0; i--) {
            long startTime = this.mAmbientLightRingBuffer.getTime(i) - now;
            float weight = calculateWeight(startTime, endTime);
            if (weight < 0.0f) {
                break;
            }
            totalWeight += weight;
            sum += this.mAmbientLightRingBuffer.getLux(i) * weight;
            endTime = startTime;
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
        return ((float) x) * (((((float) x) * 0.5f) * SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA) + ((float) this.mWeightingIntercept));
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
        BRIGHTENING_LIGHT_DEBOUNCE = (long) (((double) (BrightenDebounceTimeParaBig * 800.0f)) * ((((double) (BrightenDebounceTimePara * Stability)) / 100.0d) + 1.0d));
        this.DARKENING_LIGHT_DEBOUNCE = (long) (((((double) (DarkenDebounceTimePara * Stability)) / 100.0d) + 1.0d) * 4000.0d);
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
        DarkenDeltaLuxMax *= ((DarkenDeltaLuxPara * (Stability - 5.0f)) / 100.0f) + 1.0f;
    }

    public void setBrightenThreshold() {
        if (this.mAmbientLux >= 1000.0f) {
            BrightenDeltaLuxMax = 989.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 500.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * 0.5f) + 489.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 100.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * 0.5f) + 489.0f;
            BrightenDeltaLuxMin = (this.mAmbientLux * 1.375f) + 51.5f;
        } else if (this.mAmbientLux >= 10.0f) {
            BrightenDeltaLuxMax = Math.min((this.mAmbientLux * 20.0f) - 181.0f, (this.mAmbientLux * 4.0f) + 139.0f);
            BrightenDeltaLuxMin = Math.min((this.mAmbientLux * 5.0f) - 31.0f, (this.mAmbientLux * 1.5f) + 39.0f);
        } else if (this.mAmbientLux >= 2.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * 0.5f) + 14.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else {
            BrightenDeltaLuxMax = 15.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        }
        BrightenDeltaLuxMax *= ((BrightenDeltaLuxPara * (Stability - 5.0f)) / 100.0f) + 1.0f;
    }

    protected long nextAmbientLightBrighteningTransition(long time) {
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
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean BrightenChange;
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

    protected long nextAmbientLightDarkeningTransition(long time) {
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
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBufferFilter.size() - 1; i >= 0; i--) {
            boolean DarkenChange;
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

    private void updateAmbientLux() {
        long time = SystemClock.uptimeMillis();
        this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        updateAmbientLux(time);
    }

    private void updateAmbientLux(long time) {
        float ambientLux;
        boolean needToBrighten;
        boolean needToDarken;
        if (!this.mAmbientLuxValid) {
            long timeWhenSensorWarmedUp = ((long) this.mLightSensorWarmUpTimeConfig) + this.mLightSensorEnableTime;
            if (time < timeWhenSensorWarmedUp) {
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientLux: Sensor not  ready yet: time=" + time + ", timeWhenSensorWarmedUp=" + timeWhenSensorWarmedUp);
                }
                this.mHandler.sendEmptyMessageAtTime(1, timeWhenSensorWarmedUp);
                return;
            }
            ambientLux = calculateAmbientLux(time);
            updateBuffer(time, ambientLux, 10000);
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.push(time, ambientLux);
                this.mAmbientLightRingBufferFilter.prune(time - ((long) this.mAmbientLightHorizon));
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
        ambientLux = calculateAmbientLux(time);
        updateBuffer(time, ambientLux, 10000);
        if (NEED_NEW_FILTER_ALGORITHM) {
            this.mAmbientLightRingBufferFilter.push(time, ambientLux);
            this.mAmbientLightRingBufferFilter.prune(time - ((long) this.mAmbientLightHorizon));
            updatepara(this.mAmbientLightRingBuffer.calculateStability());
            setDarkenThreshold();
            setBrightenThreshold();
        }
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        if (NEED_NEW_FILTER_ALGORITHM) {
            if (ambientLux - this.mAmbientLux >= BrightenDeltaLuxMax) {
                needToBrighten = true;
            } else if (ambientLux - this.mAmbientLux < BrightenDeltaLuxMin || Stability >= 50.0f) {
                needToBrighten = false;
            } else {
                needToBrighten = true;
            }
            needToBrighten = needToBrighten && nextBrightenTransition <= time;
            if (this.mAmbientLux - ambientLux >= DarkenDeltaLuxMax) {
                needToDarken = true;
            } else if (this.mAmbientLux - ambientLux < DarkenDeltaLuxMin || Stability >= 5.0f) {
                needToDarken = false;
            } else {
                needToDarken = true;
            }
            needToDarken = needToDarken && nextDarkenTransition <= time;
        } else {
            needToBrighten = ambientLux >= this.mBrighteningLuxThreshold && nextBrightenTransition <= time;
            needToDarken = ambientLux <= this.mDarkeningLuxThreshold && nextDarkenTransition <= time;
        }
        if ((needToBrighten | needToDarken) != 0) {
            setAmbientLux(ambientLux);
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: " + (ambientLux > this.mAmbientLux ? "Brightened" : "Darkened") + ": " + "mBrighteningLuxThreshold=" + this.mBrighteningLuxThreshold + ", mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        long nextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
        if (nextTransitionTime <= time) {
            nextTransitionTime = time + ((long) this.mNormalLightSensorRate);
        }
        boolean z = DEBUG;
        this.mHandler.sendEmptyMessageAtTime(1, nextTransitionTime);
    }

    protected void updateAutoBrightness(boolean sendUpdate) {
        if (this.mAmbientLuxValid) {
            int newScreenAutoBrightness = getAdjustLightValByPgMode(clampScreenBrightness(Math.round(255.0f * this.mScreenAutoBrightnessSpline.interpolate(this.mAmbientLux))));
            updateAutoDBWhenSameBrightness(newScreenAutoBrightness);
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
                this.mLastScreenAutoBrightnessGamma = 1.0f;
                this.mFirstAutoBrightness = false;
                this.mFirstBrightnessAfterProximityNegative = false;
                this.mUpdateAutoBrightnessCount++;
                if (this.mUpdateAutoBrightnessCount == HwBootFail.STAGE_BOOT_SUCCESS) {
                    this.mUpdateAutoBrightnessCount = 2;
                    Slog.i(TAG, "mUpdateAutoBrightnessCount == Integer.MAX_VALUE,so set it be 2");
                }
                if (sendUpdate) {
                    this.mCallbacks.updateBrightness();
                }
            }
            return;
        }
        if (DEBUG) {
            Slog.d(TAG, "mAmbientLuxValid= false,sensor is not ready");
        }
    }

    public void updateAutoDBWhenSameBrightness(int brightness) {
    }

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void prepareBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mHandler.removeMessages(2);
        } else {
            this.mBrightnessAdjustmentSamplePending = true;
            this.mBrightnessAdjustmentSampleOldAdjustment = this.mScreenAutoBrightnessAdjustment;
            this.mBrightnessAdjustmentSampleOldLux = this.mAmbientLuxValid ? this.mAmbientLux : -1.0f;
            this.mBrightnessAdjustmentSampleOldBrightness = this.mScreenAutoBrightness;
            this.mBrightnessAdjustmentSampleOldGamma = this.mLastScreenAutoBrightnessGamma;
        }
        this.mHandler.sendEmptyMessageDelayed(2, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    private void cancelBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            this.mHandler.removeMessages(2);
        }
    }

    private void collectBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = false;
            if (this.mAmbientLuxValid && this.mScreenAutoBrightness >= 0) {
                if (DEBUG) {
                    Slog.d(TAG, "Auto-brightness adjustment changed by user: adj=" + this.mScreenAutoBrightnessAdjustment + ", lux=" + this.mAmbientLux + ", brightness=" + this.mScreenAutoBrightness + ", gamma=" + this.mLastScreenAutoBrightnessGamma + ", ring=" + this.mAmbientLightRingBuffer);
                }
                EventLog.writeEvent(EventLogTags.AUTO_BRIGHTNESS_ADJ, new Object[]{Float.valueOf(this.mBrightnessAdjustmentSampleOldAdjustment), Float.valueOf(this.mBrightnessAdjustmentSampleOldLux), Integer.valueOf(this.mBrightnessAdjustmentSampleOldBrightness), Float.valueOf(this.mBrightnessAdjustmentSampleOldGamma), Float.valueOf(this.mScreenAutoBrightnessAdjustment), Float.valueOf(this.mAmbientLux), Integer.valueOf(this.mScreenAutoBrightness), Float.valueOf(this.mLastScreenAutoBrightnessGamma)});
            }
        }
    }

    protected float getBrighteningLuxThreshold() {
        return this.mBrighteningLuxThreshold;
    }

    protected float getDarkeningLuxThreshold() {
        return this.mDarkeningLuxThreshold;
    }

    protected float getDarkeningLightHystersis() {
        return DARKENING_LIGHT_HYSTERESIS;
    }

    protected float getBrighteningLightHystersis() {
        return BRIGHTENING_LIGHT_HYSTERESIS;
    }

    protected boolean calcNeedToBrighten(float ambient) {
        return true;
    }

    protected boolean calcNeedToDarken(float ambient) {
        return true;
    }

    protected long getNextAmbientLightBrighteningTime(long earliedtime) {
        return BRIGHTENING_LIGHT_DEBOUNCE + earliedtime;
    }

    protected long getNextAmbientLightDarkeningTime(long earliedtime) {
        return this.DARKENING_LIGHT_DEBOUNCE + earliedtime;
    }

    public void setPowerStatus(boolean powerStatus) {
    }

    protected boolean interceptHandleLightSensorEvent(long time, float lux) {
        return false;
    }

    protected void saveOffsetAlgorithmParas() {
    }

    protected void updateIntervenedAutoBrightness(int brightness) {
        this.mScreenAutoBrightness = brightness;
        if (DEBUG) {
            Slog.d(TAG, "update IntervenedAutoBrightness:mScreenAutoBrightness = " + this.mScreenAutoBrightness);
        }
    }

    protected void clearFilterAlgoParas() {
    }

    protected void updatepara(AmbientLightRingBuffer mAmbientLightRingBuffer) {
    }

    protected void updateBuffer(long time, float ambientLux, int horizon) {
    }

    protected boolean decideToBrighten(float ambientLux) {
        return ambientLux >= this.mBrighteningLuxThreshold ? calcNeedToBrighten(ambientLux) : false;
    }

    protected boolean decideToDarken(float ambientLux) {
        return ambientLux <= this.mDarkeningLuxThreshold ? calcNeedToDarken(ambientLux) : false;
    }

    protected float getLuxStability() {
        return 0.0f;
    }

    public long getLightSensorEnableTime() {
        return this.mLightSensorEnableTime;
    }

    protected void updateBrightnessIfNoAmbientLuxReported() {
    }

    public int getUpdateAutoBrightnessCount() {
        return this.mUpdateAutoBrightnessCount;
    }

    public void updateCurrentUserId(int userId) {
    }

    protected SensorManager getSensorManager() {
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

    public void setCameraModeBrightnessLineEnable(boolean cameraModeBrightnessLineEnable) {
    }

    public boolean getCameraModeChangeAnimationEnable() {
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

    public int setScreenBrightnessMappingtoIndoorMax(int brightness) {
        return brightness;
    }

    public void setManualModeEnableForPg(boolean manualModeEnableForPg) {
    }

    public boolean getRebootFirstBrightnessAnimationEnable() {
        return false;
    }

    protected int getAdjustLightValByPgMode(int rawLightVal) {
        return rawLightVal;
    }

    public boolean getAutoPowerSavingUseManualAnimationTimeEnable() {
        return false;
    }

    public boolean getAutoPowerSavingAnimationEnable() {
        return false;
    }

    public void setAutoPowerSavingAnimationEnable(boolean enable) {
    }
}

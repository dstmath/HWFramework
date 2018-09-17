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
import android.util.EventLog;
import android.util.MathUtils;
import android.util.Slog;
import android.util.Spline;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.am.ProcessList;
import com.android.server.job.controllers.JobStatus;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class AutomaticBrightnessController {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final long AMBIENT_LIGHT_PREDICTION_TIME_MILLIS = 100;
    protected static long BRIGHTENING_LIGHT_DEBOUNCE = 0;
    private static final long BRIGHTENING_LIGHT_DEBOUNCE_MORE_QUICKLLY = 1000;
    protected static float BRIGHTENING_LIGHT_HYSTERESIS = 0.0f;
    private static final int BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS = 10000;
    private static float BrightenDebounceTimePara = 0.0f;
    private static float BrightenDebounceTimeParaBig = 0.0f;
    private static float BrightenDeltaLuxMax = 0.0f;
    private static float BrightenDeltaLuxMin = 0.0f;
    private static float BrightenDeltaLuxPara = 0.0f;
    protected static float DARKENING_LIGHT_HYSTERESIS = 0.0f;
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_CONTROLLER = false;
    private static final boolean DEBUG_PRETEND_LIGHT_SENSOR_ABSENT = false;
    private static float DarkenDebounceTimePara = 0.0f;
    private static float DarkenDebounceTimeParaBig = 0.0f;
    private static float DarkenDeltaLuxMax = 0.0f;
    private static float DarkenDeltaLuxMin = 0.0f;
    private static float DarkenDeltaLuxPara = 0.0f;
    private static final int INT_BRIGHTNESS_COVER_MODE = 0;
    private static final int LIGHT_SENSOR_RATE_MILLIS = 300;
    private static final int MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE = 2;
    private static final int MSG_UPDATE_AMBIENT_LUX = 1;
    private static final int MSG_UPDATE_BRIGHTNESS = 3;
    private static final boolean NEED_NEW_FILTER_ALGORITHM = false;
    private static final float SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA = 3.0f;
    private static float Stability = 0.0f;
    private static final String TAG = "AutomaticBrightnessController";
    private static final float TWILIGHT_ADJUSTMENT_MAX_GAMMA = 1.0f;
    private static final boolean USE_SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT = false;
    protected long DARKENING_LIGHT_DEBOUNCE;
    private final int mAmbientLightHorizon;
    protected AmbientLightRingBuffer mAmbientLightRingBuffer;
    private AmbientLightRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    protected boolean mAmbientLuxValid;
    private BacklightBrightness mBacklightBrightness;
    private final long mBrighteningLightDebounceConfig;
    private float mBrighteningLuxThreshold;
    private float mBrightnessAdjustmentSampleOldAdjustment;
    private int mBrightnessAdjustmentSampleOldBrightness;
    private float mBrightnessAdjustmentSampleOldGamma;
    private float mBrightnessAdjustmentSampleOldLux;
    private boolean mBrightnessAdjustmentSamplePending;
    protected boolean mBrightnessEnlarge;
    protected final Callbacks mCallbacks;
    private final long mDarkeningLightDebounceConfig;
    private float mDarkeningLuxThreshold;
    private final float mDozeScaleFactor;
    private boolean mDozing;
    protected boolean mFirstAutoBrightness;
    protected boolean mFirstBrightnessAfterProximityNegative;
    private AutomaticBrightnessHandler mHandler;
    private AmbientLightRingBuffer mInitialHorizonAmbientLightRingBuffer;
    private float mLastObservedLux;
    private long mLastObservedLuxTime;
    private float mLastScreenAutoBrightnessGamma;
    private final Sensor mLightSensor;
    protected long mLightSensorEnableElapsedTimeNanos;
    protected long mLightSensorEnableTime;
    protected boolean mLightSensorEnabled;
    private final SensorEventListener mLightSensorListener;
    private final int mLightSensorRate;
    protected int mLightSensorWarmUpTimeConfig;
    private long mPrintLogTime;
    private int mRecentLightSamples;
    private final boolean mResetAmbientLuxAfterWarmUpConfig;
    private int mScreenAutoBrightness;
    private float mScreenAutoBrightnessAdjustment;
    private float mScreenAutoBrightnessAdjustmentMaxGamma;
    private final Spline mScreenAutoBrightnessSpline;
    private final int mScreenBrightnessRangeMaximum;
    private final int mScreenBrightnessRangeMinimum;
    private final SensorManager mSensorManager;
    protected boolean mSetbrightnessImmediateEnable;
    private final TwilightManager mTwilight;
    private final TwilightListener mTwilightListener;
    protected int mUpdateAutoBrightnessCount;
    private boolean mUseTwilight;
    protected boolean mWakeupFromSleep;
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
        private static float mStability;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux;
        private long[] mRingTime;
        private int mStart;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.AutomaticBrightnessController.AmbientLightRingBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.AutomaticBrightnessController.AmbientLightRingBuffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.AutomaticBrightnessController.AmbientLightRingBuffer.<clinit>():void");
        }

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
                int newSize = this.mCapacity * AutomaticBrightnessController.MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int length = this.mCapacity - this.mStart;
                System.arraycopy(this.mRingLux, this.mStart, newRingLux, AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE, length);
                if (this.mStart != 0) {
                    System.arraycopy(this.mRingLux, AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE, newRingLux, length, this.mStart);
                    System.arraycopy(this.mRingTime, AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE, newRingTime, length, this.mStart);
                }
                this.mRingLux = newRingLux;
                this.mRingTime = newRingTime;
                next = this.mCapacity;
                this.mCapacity = newSize;
                this.mStart = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            }
            this.mRingTime[next] = time;
            this.mRingLux[next] = lux;
            this.mEnd = next + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
            if (this.mEnd == this.mCapacity) {
                this.mEnd = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            }
            this.mCount += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
        }

        public void prune(long horizon) {
            if (this.mCount != 0) {
                while (this.mCount > AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) {
                    int next = this.mStart + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
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
            this.mStart = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            this.mEnd = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            this.mCount = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
        }

        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append('[');
            for (int i = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE; i < this.mCount; i += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) {
                long next = i + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX < this.mCount ? getTime(i + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) : SystemClock.uptimeMillis();
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
                i += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
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
            int T1 = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            int T2 = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            int index = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            float luxT1Min = currentLux;
            float luxT2Min = currentLux;
            int indexMin = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            float luxT1Max = currentLux;
            float luxT2Max = currentLux;
            int indexMax = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            int j = AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE;
            while (true) {
                if (j >= this.mCount - 1) {
                    break;
                }
                Object obj;
                int T1Max;
                int T2Max;
                float lux1 = getLux((this.mCount - 1) - j);
                float lux2 = getLux(((this.mCount - 1) - j) - 1);
                if (mLuxBufferAvg > lux1 || mLuxBufferAvg < lux2) {
                    if (mLuxBufferAvg >= lux1 && mLuxBufferAvg <= lux2) {
                    }
                    if (mLuxBufferAvgMin > lux1 || mLuxBufferAvgMin < lux2) {
                        if (mLuxBufferAvgMin >= lux1 && mLuxBufferAvgMin <= lux2) {
                        }
                        if (mLuxBufferAvgMax > lux1 || mLuxBufferAvgMax < lux2) {
                            if (mLuxBufferAvgMax >= lux1 && mLuxBufferAvgMax <= lux2) {
                            }
                            if (!(index == 0 || (indexMin == 0 && indexMax == 0))) {
                                if (index <= indexMin || index < indexMax) {
                                    if (index >= indexMin && index <= indexMax) {
                                        break;
                                    }
                                }
                                break;
                            }
                            j += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
                        }
                        obj = (mLuxBufferAvgMax == lux1 || mLuxBufferAvgMax != lux2) ? null : AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
                        if (obj == null) {
                            luxT1Max = lux1;
                            luxT2Max = lux2;
                            T1Max = (this.mCount - 1) - j;
                            T2Max = ((this.mCount - 1) - j) - 1;
                            indexMax = j;
                        }
                        if (index <= indexMin) {
                        }
                        break;
                    }
                    obj = (mLuxBufferAvgMin == lux1 || mLuxBufferAvgMin != lux2) ? null : AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
                    if (obj == null) {
                        luxT1Min = lux1;
                        luxT2Min = lux2;
                        int T1Min = (this.mCount - 1) - j;
                        int T2Min = ((this.mCount - 1) - j) - 1;
                        indexMin = j;
                    }
                    if (mLuxBufferAvgMax == lux1) {
                    }
                    if (obj == null) {
                        luxT1Max = lux1;
                        luxT2Max = lux2;
                        T1Max = (this.mCount - 1) - j;
                        T2Max = ((this.mCount - 1) - j) - 1;
                        indexMax = j;
                    }
                    if (index <= indexMin) {
                    }
                    break;
                }
                obj = (mLuxBufferAvg == lux1 && mLuxBufferAvg == lux2) ? AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX : null;
                if (obj == null) {
                    luxT1 = lux1;
                    luxT2 = lux2;
                    T1 = (this.mCount - 1) - j;
                    T2 = ((this.mCount - 1) - j) - 1;
                    index = j;
                }
                if (mLuxBufferAvgMin == lux1) {
                }
                if (obj == null) {
                    luxT1Min = lux1;
                    luxT2Min = lux2;
                    int T1Min2 = (this.mCount - 1) - j;
                    int T2Min2 = ((this.mCount - 1) - j) - 1;
                    indexMin = j;
                }
                if (mLuxBufferAvgMax == lux1) {
                }
                if (obj == null) {
                    luxT1Max = lux1;
                    luxT2Max = lux2;
                    T1Max = (this.mCount - 1) - j;
                    T2Max = ((this.mCount - 1) - j) - 1;
                    indexMax = j;
                }
                if (index <= indexMin) {
                }
                break;
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
                if (k1 > this.mCount - 1) {
                    break;
                }
                if (k1 == this.mCount - 1) {
                    break;
                }
                float luxk1 = getLux(k1);
                float luxk2 = getLux(k1 + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX);
                if (indexMax > indexMin) {
                    if (luxk1 <= luxk2) {
                        break;
                    }
                    T1 = k1 + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
                } else if (luxk1 >= luxk2) {
                    break;
                } else {
                    T1 = k1 + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
                }
                k1 += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
            }
            int k3 = (this.mCount - 1) - index2;
            while (k3 >= 0 && k3 != 0) {
                float luxk3 = getLux(k3);
                float luxk4 = getLux(k3 - 1);
                if (indexMax > indexMin) {
                    if (luxk3 >= luxk4) {
                        break;
                    }
                    T2 = k3 - 1;
                } else if (luxk3 <= luxk4) {
                    break;
                } else {
                    T2 = k3 - 1;
                }
                k3--;
            }
            int t1 = (this.mCount - 1) - T1;
            int t2 = T2;
            float s1 = calculateStabilityFactor(T1, this.mCount - 1);
            float avg1 = calcluateAvg(T1, this.mCount - 1);
            float s2 = calculateStabilityFactor(AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE, T2);
            float deltaAvg = Math.abs(avg1 - calcluateAvg(AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE, T2));
            float k = Math.abs((getLux(T1) - getLux(T2)) / ((float) (T1 - T2)));
            if (k < 10.0f / (5.0f + k)) {
                tmp = k;
            } else {
                tmp = 10.0f / (5.0f + k);
            }
            if (tmp > 20.0f / (10.0f + deltaAvg)) {
                tmp = 20.0f / (10.0f + deltaAvg);
            }
            if (t1 > mSmallStabilityTimeConstant) {
                Stability1 = s1;
            } else {
                float a1 = (float) Math.exp((double) (t1 - 20));
                float b1 = (float) (20 - t1);
                float s3 = tmp;
                Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
            }
            if (t2 > mLargemSmallStabilityTimeConstant) {
                Stability2 = s2;
            } else {
                float a2 = (float) Math.exp((double) (t2 - 10));
                float b2 = (float) (10 - t2);
                float s4 = tmp;
                Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
            }
            if (t1 > mSmallStabilityTimeConstant) {
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
            for (int i = start; i <= end; i += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) {
                sum += getLux(i);
            }
            if (end < start) {
                return 0.0f;
            }
            return sum / ((float) ((end - start) + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX));
        }

        private float calculateStabilityFactor(int start, int end) {
            int size = (end - start) + AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX;
            float sum = 0.0f;
            float sigma = 0.0f;
            if (size <= AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) {
                return 0.0f;
            }
            int i;
            for (i = start; i <= end; i += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) {
                sum += getLux(i);
            }
            float avg = sum / ((float) size);
            for (i = start; i <= end; i += AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX) {
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
        final /* synthetic */ AutomaticBrightnessController this$0;

        public AutomaticBrightnessHandler(AutomaticBrightnessController this$0, Looper looper) {
            this.this$0 = this$0;
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AutomaticBrightnessController.MSG_UPDATE_AMBIENT_LUX /*1*/:
                    this.this$0.updateAmbientLux();
                case AutomaticBrightnessController.MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE /*2*/:
                    this.this$0.collectBrightnessAdjustmentSample();
                case AutomaticBrightnessController.MSG_UPDATE_BRIGHTNESS /*3*/:
                    this.this$0.updateBrightnessIfNoAmbientLuxReported();
                default:
            }
        }
    }

    public interface Callbacks {
        void updateBrightness();

        void updateProximityState(boolean z);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.AutomaticBrightnessController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.AutomaticBrightnessController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.AutomaticBrightnessController.<clinit>():void");
    }

    public void dump(java.io.PrintWriter r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.AutomaticBrightnessController.dump(java.io.PrintWriter):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.AutomaticBrightnessController.dump(java.io.PrintWriter):void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.AutomaticBrightnessController.dump(java.io.PrintWriter):void");
    }

    public void setBacklightBrightness(BacklightBrightness backlightBrightness) {
        this.mBacklightBrightness = backlightBrightness;
    }

    public void updateAutoBrightnessAdjustFactor(float adjustFactor) {
    }

    public static boolean needNewFilterAlgorithm() {
        String product = getProductName();
        if (product == null) {
            return NEED_NEW_FILTER_ALGORITHM;
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
        if (arrays == null || arrays.length < MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE) {
            productName = "default";
        } else {
            productName = arrays[INT_BRIGHTNESS_COVER_MODE] + "_" + arrays[MSG_UPDATE_AMBIENT_LUX];
        }
        return productName.toLowerCase();
    }

    private static String readFileByChars(String fileName) {
        IOException e1;
        Throwable th;
        File file = new File(fileName);
        if (file.exists() && file.canRead()) {
            Reader reader = null;
            char[] tempChars = new char[DumpState.DUMP_MESSAGES];
            StringBuilder sb = new StringBuilder();
            try {
                Reader reader2 = new InputStreamReader(new FileInputStream(fileName));
                while (true) {
                    try {
                        int charRead = reader2.read(tempChars);
                        if (charRead == -1) {
                            break;
                        }
                        sb.append(tempChars, INT_BRIGHTNESS_COVER_MODE, charRead);
                    } catch (IOException e) {
                        e1 = e;
                        reader = reader2;
                    } catch (Throwable th2) {
                        th = th2;
                        reader = reader2;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                e1 = e3;
                try {
                    Slog.e(TAG, "read file name error, file name is:" + fileName);
                    e1.printStackTrace();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                        }
                    }
                    return sb.toString();
                } catch (Throwable th3) {
                    th = th3;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            }
            return sb.toString();
        }
        Slog.d(TAG, "file is exists : " + file.exists() + " file can read : " + file.canRead());
        return "";
    }

    public AutomaticBrightnessController(Callbacks callbacks, Looper looper, SensorManager sensorManager, Spline autoBrightnessSpline, int lightSensorWarmUpTime, int brightnessMin, int brightnessMax, float dozeScaleFactor, int lightSensorRate, long brighteningLightDebounceConfig, long darkeningLightDebounceConfig, boolean resetAmbientLuxAfterWarmUpConfig, int ambientLightHorizon, float autoBrightnessAdjustmentMaxGamma) {
        this.DARKENING_LIGHT_DEBOUNCE = 8000;
        this.mPrintLogTime = 0;
        this.mScreenAutoBrightness = -1;
        this.mScreenAutoBrightnessAdjustment = 0.0f;
        this.mLastScreenAutoBrightnessGamma = TWILIGHT_ADJUSTMENT_MAX_GAMMA;
        this.mBacklightBrightness = null;
        this.mWakeupFromSleep = true;
        this.mBrightnessEnlarge = NEED_NEW_FILTER_ALGORITHM;
        this.mFirstBrightnessAfterProximityNegative = NEED_NEW_FILTER_ALGORITHM;
        this.mSetbrightnessImmediateEnable = NEED_NEW_FILTER_ALGORITHM;
        this.mLightSensorListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                if (AutomaticBrightnessController.this.mLightSensorEnabled) {
                    long time = SystemClock.uptimeMillis();
                    float lux = event.values[AutomaticBrightnessController.INT_BRIGHTNESS_COVER_MODE];
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
        this.mTwilightListener = new TwilightListener() {
            public void onTwilightStateChanged() {
                AutomaticBrightnessController.this.updateAutoBrightness(true);
            }
        };
        this.mCallbacks = callbacks;
        this.mTwilight = (TwilightManager) LocalServices.getService(TwilightManager.class);
        this.mSensorManager = sensorManager;
        this.mScreenAutoBrightnessSpline = autoBrightnessSpline;
        this.mScreenBrightnessRangeMinimum = brightnessMin;
        this.mScreenBrightnessRangeMaximum = brightnessMax;
        this.mLightSensorWarmUpTimeConfig = lightSensorWarmUpTime;
        this.mDozeScaleFactor = dozeScaleFactor;
        this.mLightSensorRate = lightSensorRate;
        this.mBrighteningLightDebounceConfig = brighteningLightDebounceConfig;
        this.mDarkeningLightDebounceConfig = darkeningLightDebounceConfig;
        this.mResetAmbientLuxAfterWarmUpConfig = resetAmbientLuxAfterWarmUpConfig;
        this.mAmbientLightHorizon = ambientLightHorizon;
        this.mWeightingIntercept = ambientLightHorizon;
        this.mScreenAutoBrightnessAdjustmentMaxGamma = autoBrightnessAdjustmentMaxGamma;
        this.mHandler = new AutomaticBrightnessHandler(this, looper);
        this.mAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mLightSensorRate, this.mAmbientLightHorizon);
        if (NEED_NEW_FILTER_ALGORITHM) {
            this.mAmbientLightRingBufferFilter = new AmbientLightRingBuffer((long) this.mLightSensorRate, this.mAmbientLightHorizon);
        }
        this.mInitialHorizonAmbientLightRingBuffer = new AmbientLightRingBuffer((long) this.mLightSensorRate, this.mAmbientLightHorizon);
        this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
    }

    public int getAutomaticScreenBrightness() {
        int brightness = this.mScreenAutoBrightness;
        if (brightness >= 0) {
            brightness = MathUtils.constrain(brightness, this.mBacklightBrightness.min, this.mBacklightBrightness.max);
        }
        if (this.mDozing) {
            return (int) (((float) brightness) * this.mDozeScaleFactor);
        }
        return brightness;
    }

    public void configure(boolean enable, float adjustment, boolean dozing, boolean userInitiatedChange, boolean useTwilight) {
        this.mDozing = dozing;
        boolean z = (!enable || dozing) ? NEED_NEW_FILTER_ALGORITHM : true;
        if ((setLightSensorEnabled(z) | setScreenAutoBrightnessAdjustment(adjustment)) | setUseTwilight(useTwilight)) {
            updateAutoBrightness(NEED_NEW_FILTER_ALGORITHM);
        }
        if (enable && !dozing && userInitiatedChange) {
            prepareBrightnessAdjustmentSample();
        }
    }

    private boolean setUseTwilight(boolean useTwilight) {
        if (this.mUseTwilight == useTwilight) {
            return NEED_NEW_FILTER_ALGORITHM;
        }
        if (useTwilight) {
            this.mTwilight.registerListener(this.mTwilightListener, this.mHandler);
        } else {
            this.mTwilight.unregisterListener(this.mTwilightListener);
        }
        this.mUseTwilight = useTwilight;
        return true;
    }

    private boolean setLightSensorEnabled(boolean enable) {
        if (enable) {
            if (!this.mLightSensorEnabled) {
                this.mLightSensorEnabled = true;
                this.mFirstAutoBrightness = true;
                this.mUpdateAutoBrightnessCount = INT_BRIGHTNESS_COVER_MODE;
                this.mLightSensorEnableTime = SystemClock.uptimeMillis();
                this.mLightSensorEnableElapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
                this.mSensorManager.registerListener(this.mLightSensorListener, this.mLightSensor, this.mLightSensorRate * ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE, this.mHandler);
                if (this.mWakeupFromSleep) {
                    this.mHandler.sendEmptyMessageAtTime(MSG_UPDATE_BRIGHTNESS, this.mLightSensorEnableTime + 200);
                }
                if (DEBUG) {
                    Slog.d(TAG, "Enable LightSensor at time:mLightSensorEnableTime=" + SystemClock.uptimeMillis() + ",mLightSensorEnableElapsedTimeNanos=" + this.mLightSensorEnableElapsedTimeNanos);
                }
                return true;
            }
        } else if (this.mLightSensorEnabled) {
            this.mLightSensorEnabled = NEED_NEW_FILTER_ALGORITHM;
            this.mFirstAutoBrightness = NEED_NEW_FILTER_ALGORITHM;
            this.mAmbientLuxValid = this.mResetAmbientLuxAfterWarmUpConfig ? NEED_NEW_FILTER_ALGORITHM : true;
            this.mRecentLightSamples = INT_BRIGHTNESS_COVER_MODE;
            this.mAmbientLightRingBuffer.clear();
            clearFilterAlgoParas();
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.clear();
            }
            this.mInitialHorizonAmbientLightRingBuffer.clear();
            this.mHandler.removeMessages(MSG_UPDATE_AMBIENT_LUX);
            this.mHandler.removeMessages(MSG_UPDATE_BRIGHTNESS);
            this.mSensorManager.unregisterListener(this.mLightSensorListener);
            if (DEBUG) {
                Slog.d(TAG, "Disable LightSensor at time:" + SystemClock.uptimeMillis());
            }
        }
        return NEED_NEW_FILTER_ALGORITHM;
    }

    protected void handleLightSensorEvent(long time, float lux) {
        this.mHandler.removeMessages(MSG_UPDATE_AMBIENT_LUX);
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    protected boolean getSetbrightnessImmediateEnableForCaliTest() {
        return this.mSetbrightnessImmediateEnable;
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mRecentLightSamples += MSG_UPDATE_AMBIENT_LUX;
        if (time <= this.mLightSensorEnableTime + ((long) this.mAmbientLightHorizon)) {
            this.mInitialHorizonAmbientLightRingBuffer.push(time, lux);
        }
        this.mAmbientLightRingBuffer.prune(time - ((long) this.mAmbientLightHorizon));
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
        this.mLastObservedLuxTime = time;
    }

    protected boolean setScreenAutoBrightnessAdjustment(float adjustment) {
        if (adjustment == this.mScreenAutoBrightnessAdjustment) {
            return NEED_NEW_FILTER_ALGORITHM;
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
        this.mBrighteningLuxThreshold = this.mAmbientLux * (BRIGHTENING_LIGHT_HYSTERESIS + TWILIGHT_ADJUSTMENT_MAX_GAMMA);
        this.mDarkeningLuxThreshold = this.mAmbientLux * (TWILIGHT_ADJUSTMENT_MAX_GAMMA - DARKENING_LIGHT_HYSTERESIS);
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
        return ((float) x) * (((((float) x) * TaskPositioner.RESIZING_HINT_ALPHA) * SCREEN_AUTO_BRIGHTNESS_ADJUSTMENT_MAX_GAMMA) + ((float) this.mWeightingIntercept));
    }

    private void updatepara(float stability) {
        Stability = stability;
        if (Stability > 100.0f) {
            Stability = 100.0f;
            DarkenDebounceTimeParaBig = TWILIGHT_ADJUSTMENT_MAX_GAMMA;
            BRIGHTENING_LIGHT_HYSTERESIS = Stability / 100.0f;
        } else if (Stability < 5.0f) {
            Stability = 5.0f;
            DarkenDebounceTimeParaBig = 0.1f;
            BRIGHTENING_LIGHT_HYSTERESIS = Stability / 100.0f;
        } else {
            DarkenDebounceTimeParaBig = TWILIGHT_ADJUSTMENT_MAX_GAMMA;
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
        DarkenDeltaLuxMax *= ((DarkenDeltaLuxPara * (Stability - 5.0f)) / 100.0f) + TWILIGHT_ADJUSTMENT_MAX_GAMMA;
    }

    public void setBrightenThreshold() {
        if (this.mAmbientLux >= 1000.0f) {
            BrightenDeltaLuxMax = 989.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 500.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * TaskPositioner.RESIZING_HINT_ALPHA) + 489.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else if (this.mAmbientLux >= 100.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * TaskPositioner.RESIZING_HINT_ALPHA) + 489.0f;
            BrightenDeltaLuxMin = (this.mAmbientLux * 1.375f) + 51.5f;
        } else if (this.mAmbientLux >= 10.0f) {
            BrightenDeltaLuxMax = Math.min((this.mAmbientLux * 20.0f) - 181.0f, (this.mAmbientLux * 4.0f) + 139.0f);
            BrightenDeltaLuxMin = Math.min((this.mAmbientLux * 5.0f) - 31.0f, (this.mAmbientLux * 1.5f) + 39.0f);
        } else if (this.mAmbientLux >= 2.0f) {
            BrightenDeltaLuxMax = (this.mAmbientLux * TaskPositioner.RESIZING_HINT_ALPHA) + 14.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        } else {
            BrightenDeltaLuxMax = 15.0f;
            BrightenDeltaLuxMin = BrightenDeltaLuxMax;
        }
        BrightenDeltaLuxMax *= ((BrightenDeltaLuxPara * (Stability - 5.0f)) / 100.0f) + TWILIGHT_ADJUSTMENT_MAX_GAMMA;
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
                BrightenChange = NEED_NEW_FILTER_ALGORITHM;
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
                DarkenChange = NEED_NEW_FILTER_ALGORITHM;
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
                this.mHandler.sendEmptyMessageAtTime(MSG_UPDATE_AMBIENT_LUX, timeWhenSensorWarmedUp);
                return;
            }
            ambientLux = calculateAmbientLux(time);
            updateBuffer(time, ambientLux, BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS);
            if (NEED_NEW_FILTER_ALGORITHM) {
                this.mAmbientLightRingBufferFilter.push(time, ambientLux);
                this.mAmbientLightRingBufferFilter.prune(time - ((long) this.mAmbientLightHorizon));
            }
            setAmbientLux(ambientLux);
            this.mAmbientLuxValid = true;
            if (this.mWakeupFromSleep) {
                this.mWakeupFromSleep = NEED_NEW_FILTER_ALGORITHM;
                this.mFirstAutoBrightness = true;
            }
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux);
            }
            updateAutoBrightness(true);
        }
        ambientLux = calculateAmbientLux(time);
        updateBuffer(time, ambientLux, BRIGHTNESS_ADJUSTMENT_SAMPLE_DEBOUNCE_MILLIS);
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
                needToBrighten = NEED_NEW_FILTER_ALGORITHM;
            } else {
                needToBrighten = true;
            }
            needToBrighten = (!needToBrighten || nextBrightenTransition > time) ? NEED_NEW_FILTER_ALGORITHM : true;
            if (this.mAmbientLux - ambientLux >= DarkenDeltaLuxMax) {
                needToDarken = true;
            } else if (this.mAmbientLux - ambientLux < DarkenDeltaLuxMin || Stability >= 5.0f) {
                needToDarken = NEED_NEW_FILTER_ALGORITHM;
            } else {
                needToDarken = true;
            }
            needToDarken = (!needToDarken || nextDarkenTransition > time) ? NEED_NEW_FILTER_ALGORITHM : true;
        } else {
            needToBrighten = (ambientLux < this.mBrighteningLuxThreshold || nextBrightenTransition > time) ? NEED_NEW_FILTER_ALGORITHM : true;
            needToDarken = (ambientLux > this.mDarkeningLuxThreshold || nextDarkenTransition > time) ? NEED_NEW_FILTER_ALGORITHM : true;
        }
        if ((needToBrighten | needToDarken) != 0) {
            setAmbientLux(ambientLux);
            if (DEBUG) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("updateAmbientLux: ");
                String str2 = ambientLux > this.mAmbientLux ? "Brightened" : "Darkened";
                float f = this.mBrighteningLuxThreshold;
                String str3 = str;
                Slog.d(str3, append.append(str2).append(": ").append("mBrighteningLuxThreshold=").append(f).append(", mAmbientLightRingBuffer=").append(this.mAmbientLightRingBuffer).append(", mAmbientLux=").append(this.mAmbientLux).toString());
            }
            updateAutoBrightness(true);
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        long nextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
        if (nextTransitionTime <= time) {
            nextTransitionTime = time + ((long) this.mLightSensorRate);
        }
        if (DEBUG) {
            this.mHandler.sendEmptyMessageAtTime(MSG_UPDATE_AMBIENT_LUX, nextTransitionTime);
        } else {
            this.mHandler.sendEmptyMessageAtTime(MSG_UPDATE_AMBIENT_LUX, nextTransitionTime);
        }
    }

    protected void updateAutoBrightness(boolean sendUpdate) {
        if (this.mAmbientLuxValid) {
            float value = this.mScreenAutoBrightnessSpline.interpolate(this.mAmbientLux);
            float gamma = TWILIGHT_ADJUSTMENT_MAX_GAMMA;
            if (this.mUseTwilight) {
                TwilightState state = this.mTwilight.getCurrentState();
                if (state != null && state.isNight()) {
                    long now = System.currentTimeMillis();
                    gamma = TWILIGHT_ADJUSTMENT_MAX_GAMMA * ((state.getAmount() * TWILIGHT_ADJUSTMENT_MAX_GAMMA) + TWILIGHT_ADJUSTMENT_MAX_GAMMA);
                    if (DEBUG) {
                        Slog.d(TAG, "updateAutoBrightness: twilight amount=" + state.getAmount());
                    }
                }
            }
            if (gamma != TWILIGHT_ADJUSTMENT_MAX_GAMMA) {
                float in = value;
                value = MathUtils.pow(value, gamma);
                if (DEBUG) {
                    Slog.d(TAG, "updateAutoBrightness: gamma=" + gamma + ", in=" + in + ", out=" + value);
                }
            }
            int newScreenAutoBrightness = clampScreenBrightness(Math.round(255.0f * value));
            if (this.mScreenAutoBrightness != newScreenAutoBrightness || this.mFirstAutoBrightness || this.mFirstBrightnessAfterProximityNegative) {
                if (DEBUG) {
                    Slog.d(TAG, "updateAutoBrightness: mScreenAutoBrightness=" + this.mScreenAutoBrightness + ", newScreenAutoBrightness=" + newScreenAutoBrightness);
                }
                if (newScreenAutoBrightness > this.mScreenAutoBrightness) {
                    this.mBrightnessEnlarge = true;
                } else {
                    this.mBrightnessEnlarge = NEED_NEW_FILTER_ALGORITHM;
                }
                this.mScreenAutoBrightness = newScreenAutoBrightness;
                this.mLastScreenAutoBrightnessGamma = gamma;
                this.mFirstAutoBrightness = NEED_NEW_FILTER_ALGORITHM;
                this.mFirstBrightnessAfterProximityNegative = NEED_NEW_FILTER_ALGORITHM;
                this.mUpdateAutoBrightnessCount += MSG_UPDATE_AMBIENT_LUX;
                if (this.mUpdateAutoBrightnessCount == Integer.MAX_VALUE) {
                    this.mUpdateAutoBrightnessCount = MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE;
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

    private int clampScreenBrightness(int value) {
        return MathUtils.constrain(value, this.mScreenBrightnessRangeMinimum, this.mScreenBrightnessRangeMaximum);
    }

    private void prepareBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mHandler.removeMessages(MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE);
        } else {
            this.mBrightnessAdjustmentSamplePending = true;
            this.mBrightnessAdjustmentSampleOldAdjustment = this.mScreenAutoBrightnessAdjustment;
            this.mBrightnessAdjustmentSampleOldLux = this.mAmbientLuxValid ? this.mAmbientLux : -1.0f;
            this.mBrightnessAdjustmentSampleOldBrightness = this.mScreenAutoBrightness;
            this.mBrightnessAdjustmentSampleOldGamma = this.mLastScreenAutoBrightnessGamma;
        }
        this.mHandler.sendEmptyMessageDelayed(MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
    }

    private void cancelBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = NEED_NEW_FILTER_ALGORITHM;
            this.mHandler.removeMessages(MSG_BRIGHTNESS_ADJUSTMENT_SAMPLE);
        }
    }

    private void collectBrightnessAdjustmentSample() {
        if (this.mBrightnessAdjustmentSamplePending) {
            this.mBrightnessAdjustmentSamplePending = NEED_NEW_FILTER_ALGORITHM;
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
        return NEED_NEW_FILTER_ALGORITHM;
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
        return ambientLux >= this.mBrighteningLuxThreshold ? calcNeedToBrighten(ambientLux) : NEED_NEW_FILTER_ALGORITHM;
    }

    protected boolean decideToDarken(float ambientLux) {
        return ambientLux <= this.mDarkeningLuxThreshold ? calcNeedToDarken(ambientLux) : NEED_NEW_FILTER_ALGORITHM;
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
        return NEED_NEW_FILTER_ALGORITHM;
    }

    public void setCoverModeStatus(boolean isclosed) {
    }

    public boolean getCoverModeFastResponseFlag() {
        return NEED_NEW_FILTER_ALGORITHM;
    }
}

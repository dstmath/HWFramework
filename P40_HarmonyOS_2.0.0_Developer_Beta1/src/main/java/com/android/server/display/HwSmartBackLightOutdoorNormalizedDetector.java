package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwSmartBackLightXmlLoader;
import java.util.List;

public class HwSmartBackLightOutdoorNormalizedDetector {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int APICAL_INDOOR_UI = 1;
    private static final int APICAL_OUTDOOR_UI = 2;
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static String TAG = "HwSmartBackLightOutdoorNormalizedDetector";
    private boolean mAmChangeFlagSBL = false;
    private AmbientLightRingBufferForSbl mAmbientLightRingBuffer;
    private AmbientLightRingBufferForSbl mAmbientLightRingBufferFilter;
    private float mAmbientLux;
    private int mBrighenDebounceTime;
    private float mBrightenDeltaLuxMax = 0.0f;
    private List<PointF> mBrightenLinePointsList;
    private List<PointF> mDarkLinePointsList;
    private int mDarkenDebounceTime;
    private float mDarkenDeltaLuxMax = 0.0f;
    private int mInDoorThreshold;
    private boolean mInoutFlag = false;
    private int mLastApicalFlag = 1;
    private int mOutDoorThreshold;

    public HwSmartBackLightOutdoorNormalizedDetector() {
        getConfig();
        this.mAmbientLightRingBuffer = new AmbientLightRingBufferForSbl(this);
        this.mAmbientLightRingBufferFilter = new AmbientLightRingBufferForSbl(this);
    }

    private void getConfig() {
        HwSmartBackLightXmlLoader.Data data = HwSmartBackLightXmlLoader.getData();
        this.mInDoorThreshold = data.inDoorThreshold;
        this.mOutDoorThreshold = data.outDoorThreshold;
        this.mBrighenDebounceTime = data.brighenDebounceTime;
        this.mDarkenDebounceTime = data.darkenDebounceTime;
        this.mBrightenLinePointsList = data.brightenLinePoints;
        this.mDarkLinePointsList = data.darkenLinePoints;
    }

    public void clearAmbientLightRingBuffer() {
        this.mAmbientLightRingBuffer.clear();
        this.mAmbientLightRingBufferFilter.clear();
    }

    public void handleLightSensorEvent(long time, float lux) {
        if (lux > 40000.0f) {
            if (DEBUG) {
                String str = TAG;
                Slog.i(str, "lux >= max, lux=" + lux);
            }
            lux = 40000.0f;
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - 10000);
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private float calculateAmbientLuxForNewPolicy(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            if (!DEBUG) {
                return -1.0f;
            }
            Slog.v(TAG, "calculateAmbientLux: No ambient light readings available");
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
            return ((sum - luxMin) - luxMax) / 3.0f;
        }
    }

    private long nextAmbientLightBrighteningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax)) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mDarkenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (!(this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax)) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightBrighteningTransitionForOldPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) > ((float) this.mOutDoorThreshold))) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForOldPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mDarkenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) < ((float) this.mInDoorThreshold))) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForInOut(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) > ((float) this.mInDoorThreshold) && this.mAmbientLightRingBufferFilter.getLux(i) < ((float) this.mOutDoorThreshold))) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    public void setBrightenThresholdNew() {
        List<PointF> list = this.mBrightenLinePointsList;
        if (list != null) {
            PointF temp1 = null;
            for (PointF temp : list) {
                if (temp1 == null) {
                    temp1 = temp;
                }
                if (this.mAmbientLux >= temp.x) {
                    temp1 = temp;
                    this.mBrightenDeltaLuxMax = temp1.y;
                } else if (temp.x <= temp1.x) {
                    this.mBrightenDeltaLuxMax = 1.0f;
                    if (DEBUG) {
                        String str = TAG;
                        Slog.i(str, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                } else {
                    this.mBrightenDeltaLuxMax = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
                    return;
                }
            }
        }
    }

    private void setDarkenThresholdNew() {
        List<PointF> list = this.mDarkLinePointsList;
        if (list != null) {
            PointF temp1 = null;
            for (PointF temp : list) {
                if (temp1 == null) {
                    temp1 = temp;
                }
                if (this.mAmbientLux < temp.x) {
                    float f = 1.0f;
                    if (temp.x <= temp1.x) {
                        this.mDarkenDeltaLuxMax = 1.0f;
                        if (DEBUG) {
                            String str = TAG;
                            Slog.i(str, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                            return;
                        }
                        return;
                    }
                    float DarkenDeltaLuxMaxtmp = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
                    if (DarkenDeltaLuxMaxtmp > 1.0f) {
                        f = DarkenDeltaLuxMaxtmp;
                    }
                    this.mDarkenDeltaLuxMax = f;
                    return;
                }
                temp1 = temp;
                this.mDarkenDeltaLuxMax = temp1.y;
            }
        }
    }

    private void updateAmbientLux(long time) {
        float value = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == 1) {
            String str = TAG;
            Slog.i(str, "fist sensor lux and filteredlux=" + value + ",time=" + time);
        }
        this.mAmbientLightRingBufferFilter.push(time, value);
        this.mAmbientLightRingBufferFilter.prune(time - 10000);
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        long nextBrightenTransitionOld = nextAmbientLightBrighteningTransitionForOldPolicy(time);
        long nextDarkenTransitionOld = nextAmbientLightDarkeningTransitionForOldPolicy(time);
        long nextInOutTransition = nextAmbientLightDarkeningTransitionForInOut(time);
        this.mAmChangeFlagSBL = false;
        boolean updateFlag = (nextBrightenTransition <= time && nextDarkenTransitionOld <= time) || (nextDarkenTransition <= time && nextBrightenTransitionOld <= time) || ((nextBrightenTransition <= time && nextInOutTransition <= time) || ((nextDarkenTransition <= time && nextInOutTransition <= time) || ((nextBrightenTransitionOld <= time && this.mInoutFlag) || (nextDarkenTransitionOld <= time && this.mInoutFlag))));
        if ((nextBrightenTransition <= time && nextBrightenTransitionOld <= time) || ((nextDarkenTransition <= time && nextDarkenTransitionOld <= time) || updateFlag)) {
            if (DEBUG) {
                String str2 = TAG;
                Slog.i(str2, "updateSBL_Flag=" + updateFlag + ",filteredlux=" + value + ",time=" + time + ",nextBTime=" + nextBrightenTransition + ",nextDTime=" + nextDarkenTransition + ",nextBTimeOld=" + nextBrightenTransitionOld + ",nextDTimeOld=" + nextDarkenTransitionOld + ",nextInOutTime=" + nextInOutTransition);
            }
            updateParaForSBL(value);
        }
    }

    private void updateParaForSBL(float lux) {
        if (lux >= ((float) this.mOutDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastApicalFlag = 2;
            this.mAmChangeFlagSBL = true;
            this.mInoutFlag = false;
        }
        if (lux < ((float) this.mInDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastApicalFlag = 1;
            this.mAmChangeFlagSBL = true;
            this.mInoutFlag = false;
        }
        if (lux < ((float) this.mOutDoorThreshold) && lux >= ((float) this.mInDoorThreshold)) {
            this.mAmbientLux = lux;
            if (this.mAmbientLightRingBufferFilter.size() == 1) {
                this.mLastApicalFlag = 1;
            }
            this.mAmChangeFlagSBL = true;
            this.mInoutFlag = true;
        }
        setBrightenThresholdNew();
        setDarkenThresholdNew();
        if (DEBUG) {
            String str = TAG;
            Slog.i(str, "updateSBL_lux =" + this.mAmbientLux + ",IN_OUT_DoorFlag =" + this.mLastApicalFlag + ",mBrightenDeltaLuxMax=" + this.mBrightenDeltaLuxMax + ",mDarkenDeltaLuxMax=" + this.mDarkenDeltaLuxMax);
        }
    }

    public float getAmbientLuxForSBL() {
        return this.mAmbientLux;
    }

    public boolean getLuxChangedFlagForSBL() {
        return this.mAmChangeFlagSBL;
    }

    public int getIndoorOutdoorFlagForSBL() {
        return this.mLastApicalFlag;
    }

    /* access modifiers changed from: private */
    public final class AmbientLightRingBufferForSbl {
        private static final float BUFFER_SLACK = 1.5f;
        private static final int DEFAULT_CAPACITY = 50;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux;
        private long[] mRingTime;
        private int mStart;

        public AmbientLightRingBufferForSbl(HwSmartBackLightOutdoorNormalizedDetector hwSmartBackLightOutdoorNormalizedDetector) {
            this(50);
        }

        public AmbientLightRingBufferForSbl(int initialCapacity) {
            this.mCapacity = initialCapacity;
            int i = this.mCapacity;
            this.mRingLux = new float[i];
            this.mRingTime = new long[i];
        }

        public float getLux(int index) {
            return this.mRingLux[offsetOf(index)];
        }

        public long getTime(int index) {
            return this.mRingTime[offsetOf(index)];
        }

        public void push(long time, float lux) {
            int next = this.mEnd;
            int i = this.mCount;
            int i2 = this.mCapacity;
            if (i == i2) {
                int newSize = i2 * 2;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int i3 = this.mStart;
                int length = i2 - i3;
                System.arraycopy(this.mRingLux, i3, newRingLux, 0, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
                int i4 = this.mStart;
                if (i4 != 0) {
                    System.arraycopy(this.mRingLux, 0, newRingLux, length, i4);
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
                    int i = this.mCapacity;
                    if (next >= i) {
                        next -= i;
                    }
                    if (this.mRingTime[next] > horizon) {
                        break;
                    }
                    this.mStart = next;
                    this.mCount--;
                }
                long[] jArr = this.mRingTime;
                int i2 = this.mStart;
                if (jArr[i2] < horizon) {
                    jArr[i2] = horizon;
                }
            }
        }

        public int size() {
            return this.mCount;
        }

        public boolean isEmpty() {
            return this.mCount == 0;
        }

        public void clear() {
            this.mStart = 0;
            this.mEnd = 0;
            this.mCount = 0;
        }

        private int offsetOf(int index) {
            if (index >= this.mCount || index < 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            int index2 = index + this.mStart;
            int i = this.mCapacity;
            if (index2 >= i) {
                return index2 - i;
            }
            return index2;
        }
    }
}

package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.gesture.GestureNavConst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwNormalizedManualBrightnessThresholdDetector {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int INDOOR_UI = 1;
    private static final int OUTDOOR_UI = 2;
    private static String TAG = "HwNormalizedManualBrightnessThresholdDetector";
    protected boolean mAmChangeFlagForHBM = false;
    protected HwRingBuffer mAmbientLightRingBuffer;
    protected HwRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    private float mBrightenDeltaLuxMax = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private float mDarkenDeltaLuxMax = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private final HwBrightnessXmlLoader.Data mData;
    private float mFilterLux;
    private int mLastInOutState = 1;

    public HwNormalizedManualBrightnessThresholdDetector(HwBrightnessXmlLoader.Data data) {
        this.mData = data;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
    }

    public void clearAmbientLightRingBuffer() {
        this.mAmbientLightRingBuffer.clear();
        this.mAmbientLightRingBufferFilter.clear();
    }

    public void handleLightSensorEvent(long time, float lux) {
        if (lux > 40000.0f) {
            lux = 40000.0f;
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private float calculateAmbientLuxForNewPolicy(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            if (DEBUG) {
                Slog.v(TAG, "calculateAmbientLux: No ambient light readings available");
            }
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
        boolean BrightenChange;
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        boolean DarkenChange;
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualDarkenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForInOut(long time) {
        boolean DarkenChange;
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualBrighenDebounceTime;
        if (1 == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            if (this.mAmbientLightRingBufferFilter.getLux(i) <= ((float) this.mData.inDoorThreshold) || this.mAmbientLightRingBufferFilter.getLux(i) >= ((float) this.mData.outDoorThreshold)) {
                DarkenChange = false;
            } else {
                DarkenChange = true;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private void setBrightenThresholdNew() {
        PointF temp1 = null;
        for (PointF temp : this.mData.manualBrightenlinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                PointF temp2 = temp;
                if (temp2.x <= temp1.x) {
                    this.mBrightenDeltaLuxMax = 1.0f;
                    if (DEBUG) {
                        String str = TAG;
                        Slog.i(str, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                }
                this.mBrightenDeltaLuxMax = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
                return;
            }
            temp1 = temp;
            this.mBrightenDeltaLuxMax = temp1.y;
        }
    }

    private void setDarkenThresholdNew() {
        PointF temp1 = null;
        for (PointF temp : this.mData.manualDarkenlinePoints) {
            if (temp1 == null) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                PointF temp2 = temp;
                float f = 1.0f;
                if (temp2.x <= temp1.x) {
                    this.mDarkenDeltaLuxMax = 1.0f;
                    if (DEBUG) {
                        String str = TAG;
                        Slog.i(str, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                }
                float DarkenDeltaLuxMaxtmp = (((temp2.y - temp1.y) / (temp2.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
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

    private void updateAmbientLux(long time) {
        this.mFilterLux = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == 1) {
            Slog.i(TAG, "fist sensor lux and filteredlux=" + this.mFilterLux + ",time=" + time);
        }
        this.mAmbientLightRingBufferFilter.push(time, this.mFilterLux);
        this.mAmbientLightRingBufferFilter.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        long nextInOutTransition = nextAmbientLightDarkeningTransitionForInOut(time);
        boolean updateFlag = false;
        this.mAmChangeFlagForHBM = false;
        if (nextBrightenTransition <= time || nextDarkenTransition <= time || ((nextBrightenTransition <= time && nextInOutTransition <= time) || (nextDarkenTransition <= time && nextInOutTransition <= time))) {
            updateFlag = true;
        }
        if (nextBrightenTransition <= time || nextDarkenTransition <= time || updateFlag) {
            if (DEBUG) {
                Slog.i(TAG, "update_Flag=" + updateFlag + ",filteredlux=" + this.mFilterLux + ",time=" + time + ",nextBTime=" + nextBrightenTransition + ",nextDTime=" + nextDarkenTransition + ",nextInOutTime=" + nextInOutTransition);
            }
            updateParaForHBM(this.mFilterLux);
        }
    }

    private void updateParaForHBM(float lux) {
        float mAmbientLuxTmp = lux;
        if (mAmbientLuxTmp >= ((float) this.mData.outDoorThreshold)) {
            this.mAmbientLux = mAmbientLuxTmp;
            this.mLastInOutState = 2;
            this.mAmChangeFlagForHBM = true;
        }
        if (mAmbientLuxTmp < ((float) this.mData.inDoorThreshold)) {
            this.mAmbientLux = mAmbientLuxTmp;
            this.mLastInOutState = 1;
            this.mAmChangeFlagForHBM = true;
        }
        if (mAmbientLuxTmp < ((float) this.mData.outDoorThreshold) && mAmbientLuxTmp >= ((float) this.mData.inDoorThreshold)) {
            this.mAmbientLux = mAmbientLuxTmp;
            if (this.mAmbientLightRingBufferFilter.size() == 1) {
                this.mLastInOutState = 1;
            }
            this.mAmChangeFlagForHBM = true;
        }
        setBrightenThresholdNew();
        setDarkenThresholdNew();
        if (DEBUG) {
            String str = TAG;
            Slog.i(str, "update_lux =" + this.mAmbientLux + ",IN_OUT_DoorFlag =" + this.mLastInOutState + ",mBrightenDeltaLuxMax=" + this.mBrightenDeltaLuxMax + ",mDarkenDeltaLuxMax=" + this.mDarkenDeltaLuxMax);
        }
    }

    public float getAmbientLuxForHBM() {
        return this.mAmbientLux;
    }

    public boolean getLuxChangedFlagForHBM() {
        return this.mAmChangeFlagForHBM;
    }

    public void setLuxChangedFlagForHBM() {
        this.mAmChangeFlagForHBM = false;
    }

    public int getIndoorOutdoorFlagForHBM() {
        return this.mLastInOutState;
    }

    public float getFilterLuxFromManualMode() {
        return this.mFilterLux;
    }
}

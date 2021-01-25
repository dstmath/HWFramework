package com.android.server.display;

import android.graphics.PointF;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.HwBrightnessXmlLoader;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;

public class HwNormalizedManualBrightnessThresholdDetector {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int DEFAULT_RING_BUFFER = 50;
    private static final int FAILED_RETURN_VALUE = -1;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int INDOOR_UI = 1;
    private static final float MIN_AMBIENT_LUX_DELTA = 1.0f;
    private static final int OUTDOOR_UI = 2;
    private static final String TAG = "HwNormalizedManualBrightnessThresholdDetector";
    private static final int TOTAL_BUFFER_LENGTH = 5;
    private HwRingBuffer mAmbientLightRingBuffer;
    private HwRingBuffer mAmbientLightRingBufferFilter;
    private float mAmbientLux;
    private float mAmbientLuxForFrontCamera;
    private float mBrightenDeltaLuxMax = 0.0f;
    private float mDarkenDeltaLuxMax = 0.0f;
    private final HwBrightnessXmlLoader.Data mData;
    private float mFilterLux;
    private HwAmbientLightTransition mHwAmbientLightTransition;
    private boolean mIsCurrentLuxUpForFrontCameraEnable;
    private boolean mIsLuxChangeFlagForHbm = false;
    private int mLastInOutState = 1;
    private final Object mLock = new Object();

    public HwNormalizedManualBrightnessThresholdDetector(HwBrightnessXmlLoader.Data data) {
        this.mData = data;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        this.mHwAmbientLightTransition = new HwAmbientLightTransition();
    }

    /* access modifiers changed from: package-private */
    public void clearAmbientLightRingBuffer() {
        synchronized (this.mLock) {
            this.mAmbientLightRingBuffer.clear();
            this.mAmbientLightRingBufferFilter.clear();
            Slog.i(TAG, "clearAmbientLightRingBuffer");
        }
    }

    /* access modifiers changed from: package-private */
    public void handleLightSensorEvent(long time, float lux) {
        synchronized (this.mLock) {
            float luxTemp = lux;
            if (lux > getValidMaxAmbientLux()) {
                luxTemp = getValidMaxAmbientLux();
            }
            applyLightSensorMeasurement(time, luxTemp);
            updateAmbientLux(time);
        }
    }

    private float getValidMaxAmbientLux() {
        if (this.mData.maxValidAmbientLux > 40000.0f) {
            return this.mData.maxValidAmbientLux;
        }
        return 40000.0f;
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private float calculateAmbientLuxForNewPolicy(long now) {
        int bufferLength = this.mAmbientLightRingBuffer.size();
        if (bufferLength == 0) {
            if (!HWFLOW) {
                return -1.0f;
            }
            Slog.w(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        } else if (bufferLength < 5) {
            return this.mAmbientLightRingBuffer.getLux(bufferLength - 1);
        } else {
            float sum = this.mAmbientLightRingBuffer.getLux(bufferLength - 1);
            float luxMin = this.mAmbientLightRingBuffer.getLux(bufferLength - 1);
            float luxMax = this.mAmbientLightRingBuffer.getLux(bufferLength - 1);
            int startIndex = bufferLength - 2;
            int endIndex = bufferLength - 5;
            for (int i = startIndex; i >= endIndex; i--) {
                if (luxMin > this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMin = this.mAmbientLightRingBuffer.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMax = this.mAmbientLightRingBuffer.getLux(i);
                }
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / ((float) (startIndex - endIndex));
        }
    }

    private long nextAmbientLightBrighteningTransitionForNewPolicy(long time) {
        int bufferFilterSize = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualBrighenDebounceTime;
        if (bufferFilterSize == 1) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = bufferFilterSize - 1; i >= 0; i--) {
            if (!(this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax)) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        int bufferFilterSize = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mData.manualDarkenDebounceTime;
        if (bufferFilterSize == 1) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        int i = bufferFilterSize - 1;
        while (i >= 0 && this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax) {
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
            i--;
        }
        return earliestValidTime + debounceTime;
    }

    private void setBrightenThresholdNew() {
        PointF prePoint = null;
        for (PointF curPoint : this.mData.manualBrightenlinePoints) {
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (this.mAmbientLux >= curPoint.x) {
                prePoint = curPoint;
                this.mBrightenDeltaLuxMax = prePoint.y;
            } else if (curPoint.x <= prePoint.x) {
                this.mBrightenDeltaLuxMax = 1.0f;
                Slog.w(TAG, "Brighten_prePoint.x <= nexPoint.x,x=" + curPoint.x + ",y= " + curPoint.y);
                return;
            } else {
                this.mBrightenDeltaLuxMax = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (this.mAmbientLux - prePoint.x)) + prePoint.y;
                return;
            }
        }
    }

    private void setDarkenThresholdNew() {
        PointF prePoint = null;
        for (PointF curPoint : this.mData.manualDarkenlinePoints) {
            if (prePoint == null) {
                prePoint = curPoint;
            }
            if (this.mAmbientLux < curPoint.x) {
                float f = 1.0f;
                if (curPoint.x <= prePoint.x) {
                    this.mDarkenDeltaLuxMax = 1.0f;
                    Slog.w(TAG, "Darken_prePoint.x <= nexPoint.x,x=" + curPoint.x + ",y=" + curPoint.y);
                    return;
                }
                float darkenDeltaLuxMaxTmp = (((curPoint.y - prePoint.y) / (curPoint.x - prePoint.x)) * (this.mAmbientLux - prePoint.x)) + prePoint.y;
                if (darkenDeltaLuxMaxTmp > 1.0f) {
                    f = darkenDeltaLuxMaxTmp;
                }
                this.mDarkenDeltaLuxMax = f;
                return;
            }
            prePoint = curPoint;
            this.mDarkenDeltaLuxMax = prePoint.y;
        }
    }

    private void updateAmbientLux(long time) {
        this.mFilterLux = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == 1) {
            Slog.i(TAG, "fist sensor lux and filteredlux=" + this.mFilterLux + ",time=" + time);
            if (this.mData.frontCameraMaxBrightnessEnable) {
                updateAmbientLuxForFrontCamera(this.mFilterLux);
            }
        }
        this.mAmbientLightRingBufferFilter.push(time, this.mFilterLux);
        this.mAmbientLightRingBufferFilter.prune(time - HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        this.mIsLuxChangeFlagForHbm = false;
        updateAmbientLuxParametersForFrontCamera(time, this.mFilterLux);
        if (nextBrightenTransition <= time || nextDarkenTransition <= time) {
            if (HWFLOW) {
                Slog.i(TAG, "filteredlux=" + this.mFilterLux + ",time=" + time + ",nextBTime=" + nextBrightenTransition + ",nextDTime=" + nextDarkenTransition);
            }
            updateParaForHbm(this.mFilterLux);
        }
    }

    private void updateParaForHbm(float lux) {
        if (lux >= ((float) this.mData.outDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastInOutState = 2;
            this.mIsLuxChangeFlagForHbm = true;
        }
        if (lux < ((float) this.mData.inDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastInOutState = 1;
            this.mIsLuxChangeFlagForHbm = true;
        }
        if (lux < ((float) this.mData.outDoorThreshold) && lux >= ((float) this.mData.inDoorThreshold)) {
            this.mAmbientLux = lux;
            if (this.mAmbientLightRingBufferFilter.size() == 1) {
                this.mLastInOutState = 1;
            }
            this.mIsLuxChangeFlagForHbm = true;
        }
        setBrightenThresholdNew();
        setDarkenThresholdNew();
        if (HWFLOW) {
            Slog.i(TAG, "updateLux=" + this.mAmbientLux + ",IN_OUT_DoorFlag=" + this.mLastInOutState + ",mBrightenDeltaLuxMax=" + this.mBrightenDeltaLuxMax + ",mDarkenDeltaLuxMax=" + this.mDarkenDeltaLuxMax);
        }
    }

    /* access modifiers changed from: package-private */
    public float getAmbientLuxForHbm() {
        return this.mAmbientLux;
    }

    /* access modifiers changed from: package-private */
    public boolean getLuxChangedFlagForHbm() {
        return this.mIsLuxChangeFlagForHbm;
    }

    /* access modifiers changed from: package-private */
    public void setLuxChangedFlagForHbm() {
        this.mIsLuxChangeFlagForHbm = false;
    }

    /* access modifiers changed from: package-private */
    public int getIndoorOutdoorFlagForHbm() {
        return this.mLastInOutState;
    }

    /* access modifiers changed from: package-private */
    public float getFilterLuxFromManualMode() {
        return this.mFilterLux;
    }

    public int getCurrentFilteredAmbientLux() {
        return (int) this.mFilterLux;
    }

    private void updateAmbientLuxForFrontCamera(float lux) {
        this.mAmbientLuxForFrontCamera = lux;
        this.mIsCurrentLuxUpForFrontCameraEnable = this.mAmbientLuxForFrontCamera >= this.mData.frontCameraLuxThreshold;
        if (HWFLOW) {
            Slog.i(TAG, "updateAmbientLuxForFrontCamera,lux=" + lux + ",mCurrentLuxUpEnable=" + this.mIsCurrentLuxUpForFrontCameraEnable);
        }
    }

    private void updateAmbientLuxParametersForFrontCamera(long time, float ambientLux) {
        HwAmbientLightTransition hwAmbientLightTransition;
        if (this.mData.frontCameraMaxBrightnessEnable && (hwAmbientLightTransition = this.mHwAmbientLightTransition) != null) {
            long nextBrightenTransitionForFrontCamera = hwAmbientLightTransition.getNextAmbientLightTransitionTime(this.mAmbientLightRingBufferFilter, time, true);
            long nextDarkenTransitionForFrontCamera = this.mHwAmbientLightTransition.getNextAmbientLightTransitionTime(this.mAmbientLightRingBufferFilter, time, false);
            if (!this.mIsCurrentLuxUpForFrontCameraEnable && ambientLux >= this.mData.frontCameraBrightenLuxThreshold && nextBrightenTransitionForFrontCamera <= time) {
                updateAmbientLuxForFrontCamera(ambientLux);
            }
            if (this.mIsCurrentLuxUpForFrontCameraEnable && ambientLux < this.mData.frontCameraDarkenLuxThreshold && nextDarkenTransitionForFrontCamera <= time) {
                updateAmbientLuxForFrontCamera(ambientLux);
            }
        }
    }

    public float getAmbientLuxForFrontCamera() {
        return this.mAmbientLuxForFrontCamera;
    }
}

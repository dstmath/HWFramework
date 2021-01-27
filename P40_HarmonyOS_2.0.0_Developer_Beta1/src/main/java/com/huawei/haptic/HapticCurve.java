package com.huawei.haptic;

import com.huawei.annotation.HwSystemApi;
import com.huawei.haptic.HwHapticCurve;
import java.util.ArrayList;
import java.util.List;

@HwSystemApi
public class HapticCurve {
    public static final int CURVE_TYPE_INTENSITY = 1;
    public static final int CURVE_TYPE_SHARPNESS = 2;
    public List<AdjustPoint> mAdjustPoints = new ArrayList();

    protected HapticCurve(Builder builder) {
        this.mAdjustPoints = builder.mAdjustPoints;
    }

    static HwHapticCurve createHwHapticCurve(HapticCurve curve) {
        if (curve == null) {
            return null;
        }
        HwHapticCurve hwHapticCurve = new HwHapticCurve();
        for (AdjustPoint point : curve.mAdjustPoints) {
            hwHapticCurve.mAdjustPoints.add(new HwHapticCurve.HwAdjustPoint(point.mTimeStamp, point.mValue));
        }
        return hwHapticCurve;
    }

    public static class AdjustPoint {
        private final int mTimeStamp;
        private final float mValue;

        public AdjustPoint(int time, float value) {
            this.mTimeStamp = time;
            this.mValue = value;
        }
    }

    public static class Builder {
        private final List<AdjustPoint> mAdjustPoints = new ArrayList();

        public Builder addAdjustPoint(int time, float value) {
            this.mAdjustPoints.add(new AdjustPoint(time, value));
            return this;
        }

        public Builder addAdjustPoint(AdjustPoint point) {
            this.mAdjustPoints.add(point);
            return this;
        }

        public HapticCurve build() {
            return new HapticCurve(this);
        }
    }
}

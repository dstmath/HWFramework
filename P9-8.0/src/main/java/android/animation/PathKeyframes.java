package android.animation;

import android.animation.Keyframes.FloatKeyframes;
import android.animation.Keyframes.IntKeyframes;
import android.graphics.Path;
import android.graphics.PointF;
import android.hardware.camera2.params.TonemapCurve;
import java.util.ArrayList;

public class PathKeyframes implements Keyframes {
    private static final ArrayList<Keyframe> EMPTY_KEYFRAMES = new ArrayList();
    private static final int FRACTION_OFFSET = 0;
    private static final int NUM_COMPONENTS = 3;
    private static final int X_OFFSET = 1;
    private static final int Y_OFFSET = 2;
    private float[] mKeyframeData;
    private PointF mTempPointF;

    private static abstract class SimpleKeyframes implements Keyframes {
        /* synthetic */ SimpleKeyframes(SimpleKeyframes -this0) {
            this();
        }

        private SimpleKeyframes() {
        }

        public void setEvaluator(TypeEvaluator evaluator) {
        }

        public ArrayList<Keyframe> getKeyframes() {
            return PathKeyframes.EMPTY_KEYFRAMES;
        }

        public Keyframes clone() {
            Keyframes clone = null;
            try {
                return (Keyframes) super.clone();
            } catch (CloneNotSupportedException e) {
                return clone;
            }
        }
    }

    static abstract class FloatKeyframesBase extends SimpleKeyframes implements FloatKeyframes {
        FloatKeyframesBase() {
            super();
        }

        public Class getType() {
            return Float.class;
        }

        public Object getValue(float fraction) {
            return Float.valueOf(getFloatValue(fraction));
        }
    }

    static abstract class IntKeyframesBase extends SimpleKeyframes implements IntKeyframes {
        IntKeyframesBase() {
            super();
        }

        public Class getType() {
            return Integer.class;
        }

        public Object getValue(float fraction) {
            return Integer.valueOf(getIntValue(fraction));
        }
    }

    public PathKeyframes(Path path) {
        this(path, 0.5f);
    }

    public PathKeyframes(Path path, float error) {
        this.mTempPointF = new PointF();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("The path must not be null or empty");
        }
        this.mKeyframeData = path.approximate(error);
    }

    public ArrayList<Keyframe> getKeyframes() {
        return EMPTY_KEYFRAMES;
    }

    public Object getValue(float fraction) {
        int numPoints = this.mKeyframeData.length / 3;
        if (fraction < TonemapCurve.LEVEL_BLACK) {
            return interpolateInRange(fraction, 0, 1);
        }
        if (fraction > 1.0f) {
            return interpolateInRange(fraction, numPoints - 2, numPoints - 1);
        }
        if (fraction == TonemapCurve.LEVEL_BLACK) {
            return pointForIndex(0);
        }
        if (fraction == 1.0f) {
            return pointForIndex(numPoints - 1);
        }
        int low = 0;
        int high = numPoints - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            float midFraction = this.mKeyframeData[(mid * 3) + 0];
            if (fraction < midFraction) {
                high = mid - 1;
            } else if (fraction <= midFraction) {
                return pointForIndex(mid);
            } else {
                low = mid + 1;
            }
        }
        return interpolateInRange(fraction, high, low);
    }

    private PointF interpolateInRange(float fraction, int startIndex, int endIndex) {
        int startBase = startIndex * 3;
        int endBase = endIndex * 3;
        float startFraction = this.mKeyframeData[startBase + 0];
        float intervalFraction = (fraction - startFraction) / (this.mKeyframeData[endBase + 0] - startFraction);
        float startX = this.mKeyframeData[startBase + 1];
        float endX = this.mKeyframeData[endBase + 1];
        float startY = this.mKeyframeData[startBase + 2];
        float endY = this.mKeyframeData[endBase + 2];
        this.mTempPointF.set(interpolate(intervalFraction, startX, endX), interpolate(intervalFraction, startY, endY));
        return this.mTempPointF;
    }

    public void setEvaluator(TypeEvaluator evaluator) {
    }

    public Class getType() {
        return PointF.class;
    }

    public Keyframes clone() {
        Keyframes clone = null;
        try {
            return (Keyframes) super.clone();
        } catch (CloneNotSupportedException e) {
            return clone;
        }
    }

    private PointF pointForIndex(int index) {
        int base = index * 3;
        this.mTempPointF.set(this.mKeyframeData[base + 1], this.mKeyframeData[base + 2]);
        return this.mTempPointF;
    }

    private static float interpolate(float fraction, float startValue, float endValue) {
        return ((endValue - startValue) * fraction) + startValue;
    }

    public FloatKeyframes createXFloatKeyframes() {
        return new FloatKeyframesBase() {
            public float getFloatValue(float fraction) {
                return ((PointF) PathKeyframes.this.getValue(fraction)).x;
            }
        };
    }

    public FloatKeyframes createYFloatKeyframes() {
        return new FloatKeyframesBase() {
            public float getFloatValue(float fraction) {
                return ((PointF) PathKeyframes.this.getValue(fraction)).y;
            }
        };
    }

    public IntKeyframes createXIntKeyframes() {
        return new IntKeyframesBase() {
            public int getIntValue(float fraction) {
                return Math.round(((PointF) PathKeyframes.this.getValue(fraction)).x);
            }
        };
    }

    public IntKeyframes createYIntKeyframes() {
        return new IntKeyframesBase() {
            public int getIntValue(float fraction) {
                return Math.round(((PointF) PathKeyframes.this.getValue(fraction)).y);
            }
        };
    }
}

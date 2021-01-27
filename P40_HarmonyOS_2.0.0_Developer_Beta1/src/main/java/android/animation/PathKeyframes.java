package android.animation;

import android.animation.Keyframes;
import android.graphics.Path;
import android.graphics.PointF;
import java.util.ArrayList;

public class PathKeyframes implements Keyframes {
    private static final ArrayList<Keyframe> EMPTY_KEYFRAMES = new ArrayList<>();
    private static final int FRACTION_OFFSET = 0;
    private static final int NUM_COMPONENTS = 3;
    private static final int X_OFFSET = 1;
    private static final int Y_OFFSET = 2;
    private float[] mKeyframeData;
    private PointF mTempPointF;

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

    @Override // android.animation.Keyframes
    public ArrayList<Keyframe> getKeyframes() {
        return EMPTY_KEYFRAMES;
    }

    @Override // android.animation.Keyframes
    public Object getValue(float fraction) {
        int numPoints = this.mKeyframeData.length / 3;
        if (fraction < 0.0f) {
            return interpolateInRange(fraction, 0, 1);
        }
        if (fraction > 1.0f) {
            return interpolateInRange(fraction, numPoints - 2, numPoints - 1);
        }
        if (fraction == 0.0f) {
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
        float[] fArr = this.mKeyframeData;
        float startFraction = fArr[startBase + 0];
        float intervalFraction = (fraction - startFraction) / (fArr[endBase + 0] - startFraction);
        float startX = fArr[startBase + 1];
        float endX = fArr[endBase + 1];
        float startY = fArr[startBase + 2];
        float endY = fArr[endBase + 2];
        this.mTempPointF.set(interpolate(intervalFraction, startX, endX), interpolate(intervalFraction, startY, endY));
        return this.mTempPointF;
    }

    @Override // android.animation.Keyframes
    public void setEvaluator(TypeEvaluator evaluator) {
    }

    @Override // android.animation.Keyframes
    public Class getType() {
        return PointF.class;
    }

    @Override // android.animation.Keyframes, java.lang.Object
    public Keyframes clone() {
        try {
            return (Keyframes) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    private PointF pointForIndex(int index) {
        int base = index * 3;
        PointF pointF = this.mTempPointF;
        float[] fArr = this.mKeyframeData;
        pointF.set(fArr[base + 1], fArr[base + 2]);
        return this.mTempPointF;
    }

    private static float interpolate(float fraction, float startValue, float endValue) {
        return ((endValue - startValue) * fraction) + startValue;
    }

    public Keyframes.FloatKeyframes createXFloatKeyframes() {
        return new FloatKeyframesBase() {
            /* class android.animation.PathKeyframes.AnonymousClass1 */

            @Override // android.animation.Keyframes.FloatKeyframes
            public float getFloatValue(float fraction) {
                return ((PointF) PathKeyframes.this.getValue(fraction)).x;
            }
        };
    }

    public Keyframes.FloatKeyframes createYFloatKeyframes() {
        return new FloatKeyframesBase() {
            /* class android.animation.PathKeyframes.AnonymousClass2 */

            @Override // android.animation.Keyframes.FloatKeyframes
            public float getFloatValue(float fraction) {
                return ((PointF) PathKeyframes.this.getValue(fraction)).y;
            }
        };
    }

    public Keyframes.IntKeyframes createXIntKeyframes() {
        return new IntKeyframesBase() {
            /* class android.animation.PathKeyframes.AnonymousClass3 */

            @Override // android.animation.Keyframes.IntKeyframes
            public int getIntValue(float fraction) {
                return Math.round(((PointF) PathKeyframes.this.getValue(fraction)).x);
            }
        };
    }

    public Keyframes.IntKeyframes createYIntKeyframes() {
        return new IntKeyframesBase() {
            /* class android.animation.PathKeyframes.AnonymousClass4 */

            @Override // android.animation.Keyframes.IntKeyframes
            public int getIntValue(float fraction) {
                return Math.round(((PointF) PathKeyframes.this.getValue(fraction)).y);
            }
        };
    }

    private static abstract class SimpleKeyframes implements Keyframes {
        private SimpleKeyframes() {
        }

        @Override // android.animation.Keyframes
        public void setEvaluator(TypeEvaluator evaluator) {
        }

        @Override // android.animation.Keyframes
        public ArrayList<Keyframe> getKeyframes() {
            return PathKeyframes.EMPTY_KEYFRAMES;
        }

        @Override // android.animation.Keyframes, java.lang.Object
        public Keyframes clone() {
            try {
                return (Keyframes) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
    }

    static abstract class IntKeyframesBase extends SimpleKeyframes implements Keyframes.IntKeyframes {
        IntKeyframesBase() {
            super();
        }

        @Override // android.animation.Keyframes
        public Class getType() {
            return Integer.class;
        }

        @Override // android.animation.Keyframes
        public Object getValue(float fraction) {
            return Integer.valueOf(getIntValue(fraction));
        }
    }

    static abstract class FloatKeyframesBase extends SimpleKeyframes implements Keyframes.FloatKeyframes {
        FloatKeyframesBase() {
            super();
        }

        @Override // android.animation.Keyframes
        public Class getType() {
            return Float.class;
        }

        @Override // android.animation.Keyframes
        public Object getValue(float fraction) {
            return Float.valueOf(getFloatValue(fraction));
        }
    }
}

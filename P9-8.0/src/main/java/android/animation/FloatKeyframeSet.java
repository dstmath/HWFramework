package android.animation;

import android.animation.Keyframes.FloatKeyframes;
import android.hardware.camera2.params.TonemapCurve;
import java.util.List;

class FloatKeyframeSet extends KeyframeSet implements FloatKeyframes {
    public FloatKeyframeSet(FloatKeyframe... keyframes) {
        super(keyframes);
    }

    public Object getValue(float fraction) {
        return Float.valueOf(getFloatValue(fraction));
    }

    public FloatKeyframeSet clone() {
        List<Keyframe> keyframes = this.mKeyframes;
        int numKeyframes = this.mKeyframes.size();
        FloatKeyframe[] newKeyframes = new FloatKeyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; i++) {
            newKeyframes[i] = (FloatKeyframe) ((Keyframe) keyframes.get(i)).clone();
        }
        return new FloatKeyframeSet(newKeyframes);
    }

    public float getFloatValue(float fraction) {
        FloatKeyframe prevKeyframe;
        FloatKeyframe nextKeyframe;
        float prevValue;
        float nextValue;
        float prevFraction;
        float nextFraction;
        TimeInterpolator interpolator;
        float intervalFraction;
        float f;
        if (fraction <= TonemapCurve.LEVEL_BLACK) {
            prevKeyframe = (FloatKeyframe) this.mKeyframes.get(0);
            nextKeyframe = (FloatKeyframe) this.mKeyframes.get(1);
            prevValue = prevKeyframe.getFloatValue();
            nextValue = nextKeyframe.getFloatValue();
            prevFraction = prevKeyframe.getFraction();
            nextFraction = nextKeyframe.getFraction();
            interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            if (this.mEvaluator == null) {
                f = ((nextValue - prevValue) * intervalFraction) + prevValue;
            } else {
                f = ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
            }
            return f;
        } else if (fraction >= 1.0f) {
            prevKeyframe = (FloatKeyframe) this.mKeyframes.get(this.mNumKeyframes - 2);
            nextKeyframe = (FloatKeyframe) this.mKeyframes.get(this.mNumKeyframes - 1);
            prevValue = prevKeyframe.getFloatValue();
            nextValue = nextKeyframe.getFloatValue();
            prevFraction = prevKeyframe.getFraction();
            nextFraction = nextKeyframe.getFraction();
            interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            if (this.mEvaluator == null) {
                f = ((nextValue - prevValue) * intervalFraction) + prevValue;
            } else {
                f = ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
            }
            return f;
        } else {
            prevKeyframe = (FloatKeyframe) this.mKeyframes.get(0);
            for (int i = 1; i < this.mNumKeyframes; i++) {
                nextKeyframe = (FloatKeyframe) this.mKeyframes.get(i);
                if (fraction < nextKeyframe.getFraction()) {
                    interpolator = nextKeyframe.getInterpolator();
                    intervalFraction = (fraction - prevKeyframe.getFraction()) / (nextKeyframe.getFraction() - prevKeyframe.getFraction());
                    prevValue = prevKeyframe.getFloatValue();
                    nextValue = nextKeyframe.getFloatValue();
                    if (interpolator != null) {
                        intervalFraction = interpolator.getInterpolation(intervalFraction);
                    }
                    if (this.mEvaluator == null) {
                        f = ((nextValue - prevValue) * intervalFraction) + prevValue;
                    } else {
                        f = ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
                    }
                    return f;
                }
                prevKeyframe = nextKeyframe;
            }
            return ((Number) ((Keyframe) this.mKeyframes.get(this.mNumKeyframes - 1)).getValue()).floatValue();
        }
    }

    public Class getType() {
        return Float.class;
    }
}

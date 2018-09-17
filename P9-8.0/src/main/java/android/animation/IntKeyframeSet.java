package android.animation;

import android.animation.Keyframes.IntKeyframes;
import android.hardware.camera2.params.TonemapCurve;
import java.util.List;

class IntKeyframeSet extends KeyframeSet implements IntKeyframes {
    public IntKeyframeSet(IntKeyframe... keyframes) {
        super(keyframes);
    }

    public Object getValue(float fraction) {
        return Integer.valueOf(getIntValue(fraction));
    }

    public IntKeyframeSet clone() {
        List<Keyframe> keyframes = this.mKeyframes;
        int numKeyframes = this.mKeyframes.size();
        IntKeyframe[] newKeyframes = new IntKeyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; i++) {
            newKeyframes[i] = (IntKeyframe) ((Keyframe) keyframes.get(i)).clone();
        }
        return new IntKeyframeSet(newKeyframes);
    }

    public int getIntValue(float fraction) {
        IntKeyframe prevKeyframe;
        IntKeyframe nextKeyframe;
        int prevValue;
        int nextValue;
        float prevFraction;
        float nextFraction;
        TimeInterpolator interpolator;
        float intervalFraction;
        int i;
        if (fraction <= TonemapCurve.LEVEL_BLACK) {
            prevKeyframe = (IntKeyframe) this.mKeyframes.get(0);
            nextKeyframe = (IntKeyframe) this.mKeyframes.get(1);
            prevValue = prevKeyframe.getIntValue();
            nextValue = nextKeyframe.getIntValue();
            prevFraction = prevKeyframe.getFraction();
            nextFraction = nextKeyframe.getFraction();
            interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            if (this.mEvaluator == null) {
                i = ((int) (((float) (nextValue - prevValue)) * intervalFraction)) + prevValue;
            } else {
                i = ((Number) this.mEvaluator.evaluate(intervalFraction, Integer.valueOf(prevValue), Integer.valueOf(nextValue))).intValue();
            }
            return i;
        } else if (fraction >= 1.0f) {
            prevKeyframe = (IntKeyframe) this.mKeyframes.get(this.mNumKeyframes - 2);
            nextKeyframe = (IntKeyframe) this.mKeyframes.get(this.mNumKeyframes - 1);
            prevValue = prevKeyframe.getIntValue();
            nextValue = nextKeyframe.getIntValue();
            prevFraction = prevKeyframe.getFraction();
            nextFraction = nextKeyframe.getFraction();
            interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            if (this.mEvaluator == null) {
                i = ((int) (((float) (nextValue - prevValue)) * intervalFraction)) + prevValue;
            } else {
                i = ((Number) this.mEvaluator.evaluate(intervalFraction, Integer.valueOf(prevValue), Integer.valueOf(nextValue))).intValue();
            }
            return i;
        } else {
            prevKeyframe = (IntKeyframe) this.mKeyframes.get(0);
            for (int i2 = 1; i2 < this.mNumKeyframes; i2++) {
                nextKeyframe = (IntKeyframe) this.mKeyframes.get(i2);
                if (fraction < nextKeyframe.getFraction()) {
                    interpolator = nextKeyframe.getInterpolator();
                    intervalFraction = (fraction - prevKeyframe.getFraction()) / (nextKeyframe.getFraction() - prevKeyframe.getFraction());
                    prevValue = prevKeyframe.getIntValue();
                    nextValue = nextKeyframe.getIntValue();
                    if (interpolator != null) {
                        intervalFraction = interpolator.getInterpolation(intervalFraction);
                    }
                    if (this.mEvaluator == null) {
                        i = ((int) (((float) (nextValue - prevValue)) * intervalFraction)) + prevValue;
                    } else {
                        i = ((Number) this.mEvaluator.evaluate(intervalFraction, Integer.valueOf(prevValue), Integer.valueOf(nextValue))).intValue();
                    }
                    return i;
                }
                prevKeyframe = nextKeyframe;
            }
            return ((Number) ((Keyframe) this.mKeyframes.get(this.mNumKeyframes - 1)).getValue()).intValue();
        }
    }

    public Class getType() {
        return Integer.class;
    }
}

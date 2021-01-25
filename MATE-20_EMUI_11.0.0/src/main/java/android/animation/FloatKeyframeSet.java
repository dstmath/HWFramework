package android.animation;

import android.animation.Keyframe;
import android.animation.Keyframes;
import java.util.List;

/* access modifiers changed from: package-private */
public class FloatKeyframeSet extends KeyframeSet implements Keyframes.FloatKeyframes {
    public FloatKeyframeSet(Keyframe.FloatKeyframe... keyframes) {
        super(keyframes);
    }

    @Override // android.animation.KeyframeSet, android.animation.Keyframes
    public Object getValue(float fraction) {
        return Float.valueOf(getFloatValue(fraction));
    }

    @Override // android.animation.KeyframeSet, android.animation.Keyframes, java.lang.Object
    public FloatKeyframeSet clone() {
        List<Keyframe> keyframes = this.mKeyframes;
        int numKeyframes = this.mKeyframes.size();
        Keyframe.FloatKeyframe[] newKeyframes = new Keyframe.FloatKeyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; i++) {
            newKeyframes[i] = (Keyframe.FloatKeyframe) keyframes.get(i).clone();
        }
        return new FloatKeyframeSet(newKeyframes);
    }

    @Override // android.animation.Keyframes.FloatKeyframes
    public float getFloatValue(float fraction) {
        if (fraction <= 0.0f) {
            Keyframe.FloatKeyframe prevKeyframe = (Keyframe.FloatKeyframe) this.mKeyframes.get(0);
            Keyframe.FloatKeyframe nextKeyframe = (Keyframe.FloatKeyframe) this.mKeyframes.get(1);
            float prevValue = prevKeyframe.getFloatValue();
            float nextValue = nextKeyframe.getFloatValue();
            float prevFraction = prevKeyframe.getFraction();
            float nextFraction = nextKeyframe.getFraction();
            TimeInterpolator interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            float intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            if (this.mEvaluator == null) {
                return ((nextValue - prevValue) * intervalFraction) + prevValue;
            }
            return ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
        } else if (fraction >= 1.0f) {
            Keyframe.FloatKeyframe prevKeyframe2 = (Keyframe.FloatKeyframe) this.mKeyframes.get(this.mNumKeyframes - 2);
            Keyframe.FloatKeyframe nextKeyframe2 = (Keyframe.FloatKeyframe) this.mKeyframes.get(this.mNumKeyframes - 1);
            float prevValue2 = prevKeyframe2.getFloatValue();
            float nextValue2 = nextKeyframe2.getFloatValue();
            float prevFraction2 = prevKeyframe2.getFraction();
            float nextFraction2 = nextKeyframe2.getFraction();
            TimeInterpolator interpolator2 = nextKeyframe2.getInterpolator();
            if (interpolator2 != null) {
                fraction = interpolator2.getInterpolation(fraction);
            }
            float intervalFraction2 = (fraction - prevFraction2) / (nextFraction2 - prevFraction2);
            if (this.mEvaluator == null) {
                return ((nextValue2 - prevValue2) * intervalFraction2) + prevValue2;
            }
            return ((Number) this.mEvaluator.evaluate(intervalFraction2, Float.valueOf(prevValue2), Float.valueOf(nextValue2))).floatValue();
        } else {
            Keyframe.FloatKeyframe prevKeyframe3 = (Keyframe.FloatKeyframe) this.mKeyframes.get(0);
            for (int i = 1; i < this.mNumKeyframes; i++) {
                Keyframe.FloatKeyframe nextKeyframe3 = (Keyframe.FloatKeyframe) this.mKeyframes.get(i);
                if (fraction < nextKeyframe3.getFraction()) {
                    TimeInterpolator interpolator3 = nextKeyframe3.getInterpolator();
                    float intervalFraction3 = (fraction - prevKeyframe3.getFraction()) / (nextKeyframe3.getFraction() - prevKeyframe3.getFraction());
                    float prevValue3 = prevKeyframe3.getFloatValue();
                    float nextValue3 = nextKeyframe3.getFloatValue();
                    if (interpolator3 != null) {
                        intervalFraction3 = interpolator3.getInterpolation(intervalFraction3);
                    }
                    if (this.mEvaluator == null) {
                        return ((nextValue3 - prevValue3) * intervalFraction3) + prevValue3;
                    }
                    return ((Number) this.mEvaluator.evaluate(intervalFraction3, Float.valueOf(prevValue3), Float.valueOf(nextValue3))).floatValue();
                }
                prevKeyframe3 = nextKeyframe3;
            }
            return ((Number) ((Keyframe) this.mKeyframes.get(this.mNumKeyframes - 1)).getValue()).floatValue();
        }
    }

    @Override // android.animation.KeyframeSet, android.animation.Keyframes
    public Class getType() {
        return Float.class;
    }
}

package android.animation;

import android.animation.Keyframe;
import android.animation.Keyframes;
import java.util.List;

/* access modifiers changed from: package-private */
public class IntKeyframeSet extends KeyframeSet implements Keyframes.IntKeyframes {
    public IntKeyframeSet(Keyframe.IntKeyframe... keyframes) {
        super(keyframes);
    }

    @Override // android.animation.KeyframeSet, android.animation.Keyframes
    public Object getValue(float fraction) {
        return Integer.valueOf(getIntValue(fraction));
    }

    @Override // java.lang.Object, android.animation.KeyframeSet, android.animation.KeyframeSet, android.animation.KeyframeSet, android.animation.Keyframes
    public IntKeyframeSet clone() {
        List<Keyframe> keyframes = this.mKeyframes;
        int numKeyframes = this.mKeyframes.size();
        Keyframe.IntKeyframe[] newKeyframes = new Keyframe.IntKeyframe[numKeyframes];
        for (int i = 0; i < numKeyframes; i++) {
            newKeyframes[i] = (Keyframe.IntKeyframe) keyframes.get(i).clone();
        }
        return new IntKeyframeSet(newKeyframes);
    }

    @Override // android.animation.Keyframes.IntKeyframes
    public int getIntValue(float fraction) {
        if (fraction <= 0.0f) {
            Keyframe.IntKeyframe prevKeyframe = (Keyframe.IntKeyframe) this.mKeyframes.get(0);
            Keyframe.IntKeyframe nextKeyframe = (Keyframe.IntKeyframe) this.mKeyframes.get(1);
            int prevValue = prevKeyframe.getIntValue();
            int nextValue = nextKeyframe.getIntValue();
            float prevFraction = prevKeyframe.getFraction();
            float nextFraction = nextKeyframe.getFraction();
            TimeInterpolator interpolator = nextKeyframe.getInterpolator();
            if (interpolator != null) {
                fraction = interpolator.getInterpolation(fraction);
            }
            float intervalFraction = (fraction - prevFraction) / (nextFraction - prevFraction);
            if (this.mEvaluator == null) {
                return ((int) (((float) (nextValue - prevValue)) * intervalFraction)) + prevValue;
            }
            return ((Number) this.mEvaluator.evaluate(intervalFraction, Integer.valueOf(prevValue), Integer.valueOf(nextValue))).intValue();
        } else if (fraction >= 1.0f) {
            Keyframe.IntKeyframe prevKeyframe2 = (Keyframe.IntKeyframe) this.mKeyframes.get(this.mNumKeyframes - 2);
            Keyframe.IntKeyframe nextKeyframe2 = (Keyframe.IntKeyframe) this.mKeyframes.get(this.mNumKeyframes - 1);
            int prevValue2 = prevKeyframe2.getIntValue();
            int nextValue2 = nextKeyframe2.getIntValue();
            float prevFraction2 = prevKeyframe2.getFraction();
            float nextFraction2 = nextKeyframe2.getFraction();
            TimeInterpolator interpolator2 = nextKeyframe2.getInterpolator();
            if (interpolator2 != null) {
                fraction = interpolator2.getInterpolation(fraction);
            }
            float intervalFraction2 = (fraction - prevFraction2) / (nextFraction2 - prevFraction2);
            if (this.mEvaluator == null) {
                return ((int) (((float) (nextValue2 - prevValue2)) * intervalFraction2)) + prevValue2;
            }
            return ((Number) this.mEvaluator.evaluate(intervalFraction2, Integer.valueOf(prevValue2), Integer.valueOf(nextValue2))).intValue();
        } else {
            Keyframe.IntKeyframe prevKeyframe3 = (Keyframe.IntKeyframe) this.mKeyframes.get(0);
            for (int i = 1; i < this.mNumKeyframes; i++) {
                Keyframe.IntKeyframe nextKeyframe3 = (Keyframe.IntKeyframe) this.mKeyframes.get(i);
                if (fraction < nextKeyframe3.getFraction()) {
                    TimeInterpolator interpolator3 = nextKeyframe3.getInterpolator();
                    float intervalFraction3 = (fraction - prevKeyframe3.getFraction()) / (nextKeyframe3.getFraction() - prevKeyframe3.getFraction());
                    int prevValue3 = prevKeyframe3.getIntValue();
                    int nextValue3 = nextKeyframe3.getIntValue();
                    if (interpolator3 != null) {
                        intervalFraction3 = interpolator3.getInterpolation(intervalFraction3);
                    }
                    if (this.mEvaluator == null) {
                        return ((int) (((float) (nextValue3 - prevValue3)) * intervalFraction3)) + prevValue3;
                    }
                    return ((Number) this.mEvaluator.evaluate(intervalFraction3, Integer.valueOf(prevValue3), Integer.valueOf(nextValue3))).intValue();
                }
                prevKeyframe3 = nextKeyframe3;
            }
            return ((Number) ((Keyframe) this.mKeyframes.get(this.mNumKeyframes - 1)).getValue()).intValue();
        }
    }

    @Override // android.animation.KeyframeSet, android.animation.Keyframes
    public Class getType() {
        return Integer.class;
    }
}

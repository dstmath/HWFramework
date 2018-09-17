package android.animation;

import android.animation.Keyframes.FloatKeyframes;
import android.speech.tts.TextToSpeech.Engine;
import java.util.List;

class FloatKeyframeSet extends KeyframeSet implements FloatKeyframes {
    private float deltaValue;
    private boolean firstTime;
    private float firstValue;
    private float lastValue;

    public FloatKeyframeSet(FloatKeyframe... keyframes) {
        super(keyframes);
        this.firstTime = true;
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

    public void invalidateCache() {
        this.firstTime = true;
    }

    public float getFloatValue(float fraction) {
        if (this.mNumKeyframes == 2) {
            if (this.firstTime) {
                this.firstTime = false;
                this.firstValue = ((FloatKeyframe) this.mKeyframes.get(0)).getFloatValue();
                this.lastValue = ((FloatKeyframe) this.mKeyframes.get(1)).getFloatValue();
                this.deltaValue = this.lastValue - this.firstValue;
            }
            if (this.mInterpolator != null) {
                fraction = this.mInterpolator.getInterpolation(fraction);
            }
            if (this.mEvaluator == null) {
                return this.firstValue + (this.deltaValue * fraction);
            }
            return ((Number) this.mEvaluator.evaluate(fraction, Float.valueOf(this.firstValue), Float.valueOf(this.lastValue))).floatValue();
        } else if (fraction <= 0.0f) {
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
                r9 = ((nextValue - prevValue) * intervalFraction) + prevValue;
            } else {
                r9 = ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
            }
            return r9;
        } else if (fraction >= Engine.DEFAULT_VOLUME) {
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
                r9 = ((nextValue - prevValue) * intervalFraction) + prevValue;
            } else {
                r9 = ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
            }
            return r9;
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
                        r9 = ((nextValue - prevValue) * intervalFraction) + prevValue;
                    } else {
                        r9 = ((Number) this.mEvaluator.evaluate(intervalFraction, Float.valueOf(prevValue), Float.valueOf(nextValue))).floatValue();
                    }
                    return r9;
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

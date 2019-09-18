package android.view.animation;

public class TranslateXAnimation extends TranslateAnimation {
    float[] mTmpValues = new float[9];

    public TranslateXAnimation(float fromXDelta, float toXDelta) {
        super(fromXDelta, toXDelta, 0.0f, 0.0f);
    }

    public TranslateXAnimation(int fromXType, float fromXValue, int toXType, float toXValue) {
        super(fromXType, fromXValue, toXType, toXValue, 0, 0.0f, 0, 0.0f);
    }

    /* access modifiers changed from: protected */
    public void applyTransformation(float interpolatedTime, Transformation t) {
        t.getMatrix().getValues(this.mTmpValues);
        t.getMatrix().setTranslate(this.mFromXDelta + ((this.mToXDelta - this.mFromXDelta) * interpolatedTime), this.mTmpValues[5]);
    }
}

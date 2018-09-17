package android.view.animation;

public class TranslateXAnimation extends TranslateAnimation {
    float[] mTmpValues;

    public TranslateXAnimation(float fromXDelta, float toXDelta) {
        super(fromXDelta, toXDelta, 0.0f, 0.0f);
        this.mTmpValues = new float[9];
    }

    public TranslateXAnimation(int fromXType, float fromXValue, int toXType, float toXValue) {
        super(fromXType, fromXValue, toXType, toXValue, 0, 0.0f, 0, 0.0f);
        this.mTmpValues = new float[9];
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        t.getMatrix().getValues(this.mTmpValues);
        t.getMatrix().setTranslate(this.mFromXDelta + ((this.mToXDelta - this.mFromXDelta) * interpolatedTime), this.mTmpValues[5]);
    }
}

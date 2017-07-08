package android.view.animation;

public class TranslateYAnimation extends TranslateAnimation {
    float[] mTmpValues;

    public TranslateYAnimation(float fromYDelta, float toYDelta) {
        super(0.0f, 0.0f, fromYDelta, toYDelta);
        this.mTmpValues = new float[9];
    }

    public TranslateYAnimation(int fromYType, float fromYValue, int toYType, float toYValue) {
        super(0, 0.0f, 0, 0.0f, fromYType, fromYValue, toYType, toYValue);
        this.mTmpValues = new float[9];
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        t.getMatrix().getValues(this.mTmpValues);
        t.getMatrix().setTranslate(this.mTmpValues[2], this.mFromYDelta + ((this.mToYDelta - this.mFromYDelta) * interpolatedTime));
    }
}

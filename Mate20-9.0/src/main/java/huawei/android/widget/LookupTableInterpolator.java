package huawei.android.widget;

import android.view.animation.Interpolator;

abstract class LookupTableInterpolator implements Interpolator {
    private final float mStepSize = (1.0f / ((float) (this.mValues.length - 1)));
    private final float[] mValues;

    public LookupTableInterpolator(float[] values) {
        this.mValues = values;
    }

    public float getInterpolation(float input) {
        if (input >= 1.0f) {
            return 1.0f;
        }
        if (input <= 0.0f) {
            return 0.0f;
        }
        int firstInput = (int) (((float) (this.mValues.length - 1)) * input);
        int sendInput = this.mValues.length - 2;
        int position = firstInput < sendInput ? firstInput : sendInput;
        return this.mValues[position] + ((this.mValues[position + 1] - this.mValues[position]) * ((input - (((float) position) * this.mStepSize)) / this.mStepSize));
    }
}

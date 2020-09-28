package huawei.android.widget;

import android.util.Log;
import android.view.animation.Interpolator;

public abstract class LookupTableInterpolator implements Interpolator {
    private static final int INDEX_BOUNDS = 2;
    private static final String TAG = "LookupTableInterpolator";
    private final float mStepSize;
    private final float[] mValues;

    public LookupTableInterpolator(float[] values) {
        this.mValues = values;
        float[] fArr = this.mValues;
        if (fArr == null || fArr.length <= 1) {
            this.mStepSize = 1.0f;
            Log.e(TAG, "mValues length must > 1");
            return;
        }
        this.mStepSize = 1.0f / ((float) (fArr.length - 1));
    }

    public float getInterpolation(float input) {
        float[] fArr = this.mValues;
        if (fArr == null || fArr.length <= 1) {
            Log.e(TAG, "mValues length must > 1");
            return 0.0f;
        } else if (input >= 1.0f) {
            return 1.0f;
        } else {
            if (input <= 0.0f) {
                return 0.0f;
            }
            int firstInput = (int) (((float) (fArr.length - 1)) * input);
            int sendInput = fArr.length - 2;
            int position = firstInput < sendInput ? firstInput : sendInput;
            float f = this.mStepSize;
            float[] fArr2 = this.mValues;
            return fArr2[position] + ((fArr2[position + 1] - fArr2[position]) * ((input - (((float) position) * f)) / f));
        }
    }
}

package com.android.server.wm;

import android.animation.TimeInterpolator;
import android.util.Log;
import android.view.animation.Interpolator;

public class PhaseInterpolator implements Interpolator {
    private static final float[] DE_VALUES = {0.0f, 1.0f};
    private static final float[] IN_VALUES = {0.0f, 1.0f};
    private static final boolean IS_DB = false;
    private static final String TAG = "PhaseInterpolator";
    private float[] mDeValues;
    private float[] mInValues;
    private TimeInterpolator[] mInterpolators;
    private boolean mIsLegal;
    private boolean mIsStrictIncrement;

    public PhaseInterpolator() {
        this(IN_VALUES, DE_VALUES, null);
    }

    public PhaseInterpolator(float[] inValues, float[] deValues, TimeInterpolator[] interpolators) {
        boolean z = true;
        if (inValues == null || deValues == null || !isStrictMonotonic(inValues)) {
            StringBuilder sb = new StringBuilder();
            sb.append("invalide param, inValues : ");
            sb.append(inValues == null);
            sb.append(", deValues : ");
            sb.append(deValues == null);
            sb.append(", interpolators : ");
            sb.append(interpolators != null ? false : z);
            Log.w(TAG, sb.toString());
        } else if (inValues.length == deValues.length && (interpolators == null || inValues.length == interpolators.length + 1)) {
            this.mInValues = inValues;
            this.mDeValues = deValues;
            this.mInterpolators = interpolators;
            this.mIsStrictIncrement = isStrictIncrement(inValues);
            this.mIsLegal = true;
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("invalide param, inValues : ");
            sb2.append(false);
            sb2.append(", deValues : ");
            sb2.append(false);
            sb2.append(", interpolators : ");
            sb2.append(interpolators != null ? false : z);
            Log.w(TAG, sb2.toString());
        }
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float input) {
        if (input < 0.0f || !this.mIsLegal) {
            return 0.0f;
        }
        float[] fArr = this.mInValues;
        float current = fArr[0] + ((fArr[fArr.length - 1] - fArr[0]) * input);
        int idx = location(current, fArr);
        float[] fArr2 = this.mInValues;
        float startIn = fArr2[idx - 1];
        float endIn = fArr2[idx];
        float[] fArr3 = this.mDeValues;
        float startDe = fArr3[idx - 1];
        float endDe = fArr3[idx];
        float p = (current - startIn) / (endIn - startIn);
        TimeInterpolator[] timeInterpolatorArr = this.mInterpolators;
        if (!(timeInterpolatorArr == null || timeInterpolatorArr[idx - 1] == null)) {
            p = timeInterpolatorArr[idx - 1].getInterpolation(p);
        }
        return ((endDe - startDe) * p) + startDe;
    }

    private int location(float current, float[] array) {
        if (array == null) {
            Log.e(TAG, "location in a null array");
            return 1;
        }
        int length = array.length;
        if (this.mIsStrictIncrement) {
            for (int i = 1; i < length; i++) {
                if (current <= array[i]) {
                    return i;
                }
            }
        } else {
            for (int i2 = 1; i2 < length; i2++) {
                if (current >= array[i2]) {
                    return i2;
                }
            }
        }
        return length - 1;
    }

    private boolean isStrictMonotonic(float[] array) {
        return isStrictIncrement(array) || isStrictDecrease(array);
    }

    private boolean isStrictIncrement(float[] array) {
        if (array == null || array.length <= 1) {
            return false;
        }
        int length = array.length;
        float last = array[0];
        for (int i = 1; i < length; i++) {
            float current = array[i];
            if (last >= current) {
                return false;
            }
            last = current;
        }
        return true;
    }

    private boolean isStrictDecrease(float[] array) {
        if (array == null || array.length <= 1) {
            return false;
        }
        int length = array.length;
        float last = array[0];
        for (int i = 1; i < length; i++) {
            float current = array[i];
            if (last <= current) {
                return false;
            }
            last = current;
        }
        return true;
    }

    public boolean isLegal() {
        return this.mIsLegal;
    }
}

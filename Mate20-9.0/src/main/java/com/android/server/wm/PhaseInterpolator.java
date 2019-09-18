package com.android.server.wm;

import android.animation.TimeInterpolator;
import android.util.Log;
import android.view.animation.Interpolator;

public class PhaseInterpolator implements Interpolator {
    private static final boolean DB = false;
    private static final float[] DE_VALUES = {0.0f, 1.0f};
    private static final float[] IN_VALUES = {0.0f, 1.0f};
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
        if (inValues == null || deValues == null || !isStrictMonotonic(inValues) || inValues.length != deValues.length || !(interpolators == null || inValues.length == interpolators.length + 1)) {
            StringBuilder sb = new StringBuilder();
            sb.append("invalide param, inValues : ");
            sb.append(inValues == null);
            sb.append(", deValues : ");
            sb.append(deValues == null);
            sb.append(", interpolators : ");
            sb.append(interpolators != null ? false : z);
            Log.w(TAG, sb.toString());
            return;
        }
        this.mInValues = inValues;
        this.mDeValues = deValues;
        this.mInterpolators = interpolators;
        this.mIsStrictIncrement = isStrictIncrement(inValues);
        this.mIsLegal = true;
    }

    public float getInterpolation(float input) {
        if (input < 0.0f || !this.mIsLegal) {
            return 0.0f;
        }
        float current = this.mInValues[0] + ((this.mInValues[this.mInValues.length - 1] - this.mInValues[0]) * input);
        int idx = location(current, this.mInValues);
        float startIn = this.mInValues[idx - 1];
        float endIn = this.mInValues[idx];
        float startDe = this.mDeValues[idx - 1];
        float endDe = this.mDeValues[idx];
        float p = (current - startIn) / (endIn - startIn);
        if (!(this.mInterpolators == null || this.mInterpolators[idx - 1] == null)) {
            p = this.mInterpolators[idx - 1].getInterpolation(p);
        }
        return ((endDe - startDe) * p) + startDe;
    }

    private int location(float current, float[] array) {
        int i = 1;
        if (array == null) {
            Log.e(TAG, "location in a null array");
            return 1;
        }
        int length = array.length;
        if (this.mIsStrictIncrement) {
            while (i < length) {
                if (current <= array[i]) {
                    return i;
                }
                i++;
            }
        } else {
            while (i < length) {
                if (current >= array[i]) {
                    return i;
                }
                i++;
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

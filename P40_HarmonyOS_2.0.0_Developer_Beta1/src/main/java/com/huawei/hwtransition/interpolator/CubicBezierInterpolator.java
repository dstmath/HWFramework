package com.huawei.hwtransition.interpolator;

import android.util.Log;
import android.view.animation.Interpolator;

public class CubicBezierInterpolator implements Interpolator {
    public static final boolean IS_DEBUG = true;
    static final long MAX_RESOLUTION = 4000;
    static final float SEARCH_STEP = 2.5E-4f;
    private static final String TAG = "CubicBezierInterpolator";
    float mControlPoint1x = 0.0f;
    float mControlPoint1y = 0.0f;
    float mControlPoint2x = 0.0f;
    float mControlPoint2y = 0.0f;

    public CubicBezierInterpolator(float controlFirstPointX, float controlFirstPointY, float controlSecondPointX, float controlSecondPointY) {
        this.mControlPoint1x = controlFirstPointX;
        this.mControlPoint1y = controlFirstPointY;
        this.mControlPoint2x = controlSecondPointX;
        this.mControlPoint2y = controlSecondPointY;
        Log.d(TAG, getPointString());
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float input) {
        return getCubicBezierY(SEARCH_STEP * ((float) binarySearch(input)));
    }

    private float getCubicBezierX(float time) {
        return ((1.0f - time) * 3.0f * (1.0f - time) * time * this.mControlPoint1x) + ((1.0f - time) * 3.0f * time * time * this.mControlPoint2x) + (time * time * time);
    }

    /* access modifiers changed from: protected */
    public float getCubicBezierY(float time) {
        return ((1.0f - time) * 3.0f * (1.0f - time) * time * this.mControlPoint1y) + ((1.0f - time) * 3.0f * time * time * this.mControlPoint2y) + (time * time * time);
    }

    /* access modifiers changed from: package-private */
    public long binarySearch(float key) {
        long low = 0;
        long high = MAX_RESOLUTION;
        while (low <= high) {
            long middle = (low + high) >>> 1;
            float approximation = getCubicBezierX(SEARCH_STEP * ((float) middle));
            if (approximation < key) {
                low = middle + 1;
            } else if (approximation <= key) {
                return middle;
            } else {
                high = middle - 1;
            }
        }
        return low;
    }

    @Override // java.lang.Object
    public String toString() {
        return getPointString();
    }

    private String getPointString() {
        StringBuffer pointValue = new StringBuffer(TAG);
        pointValue.append("  mControlPoint1x = ");
        pointValue.append(this.mControlPoint1x);
        pointValue.append(", mControlPoint1y = ");
        pointValue.append(this.mControlPoint1y);
        pointValue.append(", mControlPoint2x = ");
        pointValue.append(this.mControlPoint2x);
        pointValue.append(", mControlPoint2y = ");
        pointValue.append(this.mControlPoint2y);
        return pointValue.toString();
    }
}

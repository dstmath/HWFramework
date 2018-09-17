package com.huawei.hwanimation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.AttributeSet;

public class CubicBezierReverseInterpolator extends CubicBezierInterpolator {
    private static final String TAG = "CubicBezierReverseInterpolator";

    public CubicBezierReverseInterpolator(float cx1, float cy1, float cx2, float cy2) {
        super(cx1, cy1, cx2, cy2);
    }

    public CubicBezierReverseInterpolator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CubicBezierReverseInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        super(res, theme, attrs);
    }

    public float getInterpolation(float input) {
        return 1.0f - getCubicBezierY(((float) binarySearch(1.0f - input)) * 2.5E-4f);
    }
}

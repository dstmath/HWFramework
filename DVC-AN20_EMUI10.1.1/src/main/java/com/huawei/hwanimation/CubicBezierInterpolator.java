package com.huawei.hwanimation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.BaseInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.R;
import java.math.BigDecimal;

public class CubicBezierInterpolator extends BaseInterpolator implements Interpolator {
    static final BigDecimal BASE_NUMBER = new BigDecimal(Float.toString(1.0f));
    public static final boolean DEBUG = false;
    static final int DECIMAL_COUNT = 20;
    static final long MAX_RESOLUTION = 4000;
    static final BigDecimal MAX_RESOLUTION_NUMBER = new BigDecimal(Long.toString(MAX_RESOLUTION));
    static final float SEARCH_STEP = BASE_NUMBER.divide(MAX_RESOLUTION_NUMBER, 20, 4).floatValue();
    private static final String TAG = "CubicBezierInterpolator";
    float mControlPoint1x;
    float mControlPoint1y;
    float mControlPoint2x;
    float mControlPoint2y;

    public CubicBezierInterpolator(float cx1, float cy1, float cx2, float cy2) {
        this.mControlPoint1x = 0.0f;
        this.mControlPoint1y = 0.0f;
        this.mControlPoint2x = 0.0f;
        this.mControlPoint2y = 0.0f;
        this.mControlPoint1x = cx1;
        this.mControlPoint1y = cy1;
        this.mControlPoint2x = cx2;
        this.mControlPoint2y = cy2;
    }

    public CubicBezierInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public CubicBezierInterpolator(Resources res, Resources.Theme theme, AttributeSet attrs) {
        TypedArray a;
        this.mControlPoint1x = 0.0f;
        this.mControlPoint1y = 0.0f;
        this.mControlPoint2x = 0.0f;
        this.mControlPoint2y = 0.0f;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.TranslateAnimation, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.TranslateAnimation);
        }
        this.mControlPoint1x = parseValue(a.peekValue(0));
        this.mControlPoint1y = parseValue(a.peekValue(2));
        this.mControlPoint2x = parseValue(a.peekValue(1));
        this.mControlPoint2y = parseValue(a.peekValue(3));
        a.recycle();
    }

    private float parseValue(TypedValue value) {
        if (value == null) {
            return 1.0f;
        }
        if (value.type == 6) {
            return TypedValue.complexToFloat(value.data);
        }
        if (value.type == 4) {
            return value.getFloat();
        }
        if (value.type < 16 || value.type > 31) {
            return 1.0f;
        }
        return (float) value.data;
    }

    public float getInterpolation(float input) {
        return getCubicBezierY(SEARCH_STEP * ((float) binarySearch(input)));
    }

    private float getCubicBezierX(float t) {
        return ((1.0f - t) * 3.0f * (1.0f - t) * t * this.mControlPoint1x) + ((1.0f - t) * 3.0f * t * t * this.mControlPoint2x) + (t * t * t);
    }

    /* access modifiers changed from: protected */
    public float getCubicBezierY(float t) {
        return ((1.0f - t) * 3.0f * (1.0f - t) * t * this.mControlPoint1y) + ((1.0f - t) * 3.0f * t * t * this.mControlPoint2y) + (t * t * t);
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

    public String toString() {
        return getPointString();
    }

    private String getPointString() {
        StringBuffer sb = new StringBuffer(TAG);
        sb.append("  mControlPoint1x = ");
        sb.append(this.mControlPoint1x);
        sb.append(", mControlPoint1y = ");
        sb.append(this.mControlPoint1y);
        sb.append(", mControlPoint2x = ");
        sb.append(this.mControlPoint2x);
        sb.append(", mControlPoint2y = ");
        sb.append(this.mControlPoint2y);
        return sb.toString();
    }
}

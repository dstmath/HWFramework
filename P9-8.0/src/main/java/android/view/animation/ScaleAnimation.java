package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.android.internal.R;

public class ScaleAnimation extends Animation {
    private float mFromX;
    private int mFromXData;
    private int mFromXType;
    private float mFromY;
    private int mFromYData;
    private int mFromYType;
    private float mPivotX;
    private int mPivotXType;
    private float mPivotXValue;
    private float mPivotY;
    private int mPivotYType;
    private float mPivotYValue;
    private final Resources mResources;
    private float mToX;
    private int mToXData;
    private int mToXType;
    private float mToY;
    private int mToYData;
    private int mToYType;

    public ScaleAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXData = 0;
        this.mToXData = 0;
        this.mFromYData = 0;
        this.mToYData = 0;
        this.mPivotXType = 0;
        this.mPivotYType = 0;
        this.mPivotXValue = 0.0f;
        this.mPivotYValue = 0.0f;
        this.mResources = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScaleAnimation);
        TypedValue tv = a.peekValue(2);
        this.mFromX = 0.0f;
        if (tv != null) {
            if (tv.type == 4) {
                this.mFromX = tv.getFloat();
            } else {
                this.mFromXType = tv.type;
                this.mFromXData = tv.data;
            }
        }
        tv = a.peekValue(3);
        this.mToX = 0.0f;
        if (tv != null) {
            if (tv.type == 4) {
                this.mToX = tv.getFloat();
            } else {
                this.mToXType = tv.type;
                this.mToXData = tv.data;
            }
        }
        tv = a.peekValue(4);
        this.mFromY = 0.0f;
        if (tv != null) {
            if (tv.type == 4) {
                this.mFromY = tv.getFloat();
            } else {
                this.mFromYType = tv.type;
                this.mFromYData = tv.data;
            }
        }
        tv = a.peekValue(5);
        this.mToY = 0.0f;
        if (tv != null) {
            if (tv.type == 4) {
                this.mToY = tv.getFloat();
            } else {
                this.mToYType = tv.type;
                this.mToYData = tv.data;
            }
        }
        Description d = Description.parseValue(a.peekValue(0));
        this.mPivotXType = d.type;
        this.mPivotXValue = d.value;
        d = Description.parseValue(a.peekValue(1));
        this.mPivotYType = d.type;
        this.mPivotYValue = d.value;
        a.recycle();
        initializePivotPoint();
    }

    public ScaleAnimation(float fromX, float toX, float fromY, float toY) {
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXData = 0;
        this.mToXData = 0;
        this.mFromYData = 0;
        this.mToYData = 0;
        this.mPivotXType = 0;
        this.mPivotYType = 0;
        this.mPivotXValue = 0.0f;
        this.mPivotYValue = 0.0f;
        this.mResources = null;
        this.mFromX = fromX;
        this.mToX = toX;
        this.mFromY = fromY;
        this.mToY = toY;
        this.mPivotX = 0.0f;
        this.mPivotY = 0.0f;
    }

    public ScaleAnimation(float fromX, float toX, float fromY, float toY, float pivotX, float pivotY) {
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXData = 0;
        this.mToXData = 0;
        this.mFromYData = 0;
        this.mToYData = 0;
        this.mPivotXType = 0;
        this.mPivotYType = 0;
        this.mPivotXValue = 0.0f;
        this.mPivotYValue = 0.0f;
        this.mResources = null;
        this.mFromX = fromX;
        this.mToX = toX;
        this.mFromY = fromY;
        this.mToY = toY;
        this.mPivotXType = 0;
        this.mPivotYType = 0;
        this.mPivotXValue = pivotX;
        this.mPivotYValue = pivotY;
        initializePivotPoint();
    }

    public ScaleAnimation(float fromX, float toX, float fromY, float toY, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXData = 0;
        this.mToXData = 0;
        this.mFromYData = 0;
        this.mToYData = 0;
        this.mPivotXType = 0;
        this.mPivotYType = 0;
        this.mPivotXValue = 0.0f;
        this.mPivotYValue = 0.0f;
        this.mResources = null;
        this.mFromX = fromX;
        this.mToX = toX;
        this.mFromY = fromY;
        this.mToY = toY;
        this.mPivotXValue = pivotXValue;
        this.mPivotXType = pivotXType;
        this.mPivotYValue = pivotYValue;
        this.mPivotYType = pivotYType;
        initializePivotPoint();
    }

    private void initializePivotPoint() {
        if (this.mPivotXType == 0) {
            this.mPivotX = this.mPivotXValue;
        }
        if (this.mPivotYType == 0) {
            this.mPivotY = this.mPivotYValue;
        }
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float sx = 1.0f;
        float sy = 1.0f;
        float scale = getScaleFactor();
        if (!(this.mFromX == 1.0f && this.mToX == 1.0f)) {
            sx = this.mFromX + ((this.mToX - this.mFromX) * interpolatedTime);
        }
        if (!(this.mFromY == 1.0f && this.mToY == 1.0f)) {
            sy = this.mFromY + ((this.mToY - this.mFromY) * interpolatedTime);
        }
        if (this.mPivotX == 0.0f && this.mPivotY == 0.0f) {
            t.getMatrix().setScale(sx, sy);
        } else {
            t.getMatrix().setScale(sx, sy, this.mPivotX * scale, this.mPivotY * scale);
        }
    }

    float resolveScale(float scale, int type, int data, int size, int psize) {
        float targetSize;
        if (type == 6) {
            targetSize = TypedValue.complexToFraction(data, (float) size, (float) psize);
        } else if (type != 5) {
            return scale;
        } else {
            targetSize = TypedValue.complexToDimension(data, this.mResources.getDisplayMetrics());
        }
        if (size == 0) {
            return 1.0f;
        }
        return targetSize / ((float) size);
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.mFromX = resolveScale(this.mFromX, this.mFromXType, this.mFromXData, width, parentWidth);
        this.mToX = resolveScale(this.mToX, this.mToXType, this.mToXData, width, parentWidth);
        this.mFromY = resolveScale(this.mFromY, this.mFromYType, this.mFromYData, height, parentHeight);
        this.mToY = resolveScale(this.mToY, this.mToYType, this.mToYData, height, parentHeight);
        this.mPivotX = resolveSize(this.mPivotXType, this.mPivotXValue, width, parentWidth);
        this.mPivotY = resolveSize(this.mPivotYType, this.mPivotYValue, height, parentHeight);
    }
}

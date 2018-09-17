package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.internal.R;

public class RotateAnimation extends Animation {
    private float mFromDegrees;
    private float mPivotX;
    private int mPivotXType = 0;
    private float mPivotXValue = 0.0f;
    private float mPivotY;
    private int mPivotYType = 0;
    private float mPivotYValue = 0.0f;
    private float mToDegrees;

    public RotateAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotateAnimation);
        this.mFromDegrees = a.getFloat(0, 0.0f);
        this.mToDegrees = a.getFloat(1, 0.0f);
        Description d = Description.parseValue(a.peekValue(2));
        this.mPivotXType = d.type;
        this.mPivotXValue = d.value;
        d = Description.parseValue(a.peekValue(3));
        this.mPivotYType = d.type;
        this.mPivotYValue = d.value;
        a.recycle();
        initializePivotPoint();
    }

    public RotateAnimation(float fromDegrees, float toDegrees) {
        this.mFromDegrees = fromDegrees;
        this.mToDegrees = toDegrees;
        this.mPivotX = 0.0f;
        this.mPivotY = 0.0f;
    }

    public RotateAnimation(float fromDegrees, float toDegrees, float pivotX, float pivotY) {
        this.mFromDegrees = fromDegrees;
        this.mToDegrees = toDegrees;
        this.mPivotXType = 0;
        this.mPivotYType = 0;
        this.mPivotXValue = pivotX;
        this.mPivotYValue = pivotY;
        initializePivotPoint();
    }

    public RotateAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue) {
        this.mFromDegrees = fromDegrees;
        this.mToDegrees = toDegrees;
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
        float degrees = this.mFromDegrees + ((this.mToDegrees - this.mFromDegrees) * interpolatedTime);
        float scale = getScaleFactor();
        if (this.mPivotX == 0.0f && this.mPivotY == 0.0f) {
            t.getMatrix().setRotate(degrees);
        } else {
            t.getMatrix().setRotate(degrees, this.mPivotX * scale, this.mPivotY * scale);
        }
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.mPivotX = resolveSize(this.mPivotXType, this.mPivotXValue, width, parentWidth);
        this.mPivotY = resolveSize(this.mPivotYType, this.mPivotYValue, height, parentHeight);
    }
}

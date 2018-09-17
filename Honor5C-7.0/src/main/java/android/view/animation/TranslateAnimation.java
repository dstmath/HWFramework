package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.internal.R;

public class TranslateAnimation extends Animation {
    protected float mFromXDelta;
    private int mFromXType;
    protected float mFromXValue;
    protected float mFromYDelta;
    private int mFromYType;
    protected float mFromYValue;
    protected float mToXDelta;
    private int mToXType;
    protected float mToXValue;
    protected float mToYDelta;
    private int mToYType;
    protected float mToYValue;

    public TranslateAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXValue = 0.0f;
        this.mToXValue = 0.0f;
        this.mFromYValue = 0.0f;
        this.mToYValue = 0.0f;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TranslateAnimation);
        Description d = Description.parseValue(a.peekValue(0));
        this.mFromXType = d.type;
        this.mFromXValue = d.value;
        d = Description.parseValue(a.peekValue(1));
        this.mToXType = d.type;
        this.mToXValue = d.value;
        d = Description.parseValue(a.peekValue(2));
        this.mFromYType = d.type;
        this.mFromYValue = d.value;
        d = Description.parseValue(a.peekValue(3));
        this.mToYType = d.type;
        this.mToYValue = d.value;
        a.recycle();
    }

    public TranslateAnimation(float fromXDelta, float toXDelta, float fromYDelta, float toYDelta) {
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXValue = 0.0f;
        this.mToXValue = 0.0f;
        this.mFromYValue = 0.0f;
        this.mToYValue = 0.0f;
        this.mFromXValue = fromXDelta;
        this.mToXValue = toXDelta;
        this.mFromYValue = fromYDelta;
        this.mToYValue = toYDelta;
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
    }

    public TranslateAnimation(int fromXType, float fromXValue, int toXType, float toXValue, int fromYType, float fromYValue, int toYType, float toYValue) {
        this.mFromXType = 0;
        this.mToXType = 0;
        this.mFromYType = 0;
        this.mToYType = 0;
        this.mFromXValue = 0.0f;
        this.mToXValue = 0.0f;
        this.mFromYValue = 0.0f;
        this.mToYValue = 0.0f;
        this.mFromXValue = fromXValue;
        this.mToXValue = toXValue;
        this.mFromYValue = fromYValue;
        this.mToYValue = toYValue;
        this.mFromXType = fromXType;
        this.mToXType = toXType;
        this.mFromYType = fromYType;
        this.mToYType = toYType;
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float dx = this.mFromXDelta;
        float dy = this.mFromYDelta;
        if (this.mFromXDelta != this.mToXDelta) {
            dx = this.mFromXDelta + ((this.mToXDelta - this.mFromXDelta) * interpolatedTime);
        }
        if (this.mFromYDelta != this.mToYDelta) {
            dy = this.mFromYDelta + ((this.mToYDelta - this.mFromYDelta) * interpolatedTime);
        }
        t.getMatrix().setTranslate(dx, dy);
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        this.mFromXDelta = resolveSize(this.mFromXType, this.mFromXValue, width, parentWidth);
        this.mToXDelta = resolveSize(this.mToXType, this.mToXValue, width, parentWidth);
        this.mFromYDelta = resolveSize(this.mFromYType, this.mFromYValue, height, parentHeight);
        this.mToYDelta = resolveSize(this.mToYType, this.mToYValue, height, parentHeight);
    }
}

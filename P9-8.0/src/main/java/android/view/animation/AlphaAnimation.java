package android.view.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.internal.R;

public class AlphaAnimation extends Animation {
    private float mFromAlpha;
    private float mToAlpha;

    public AlphaAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlphaAnimation);
        this.mFromAlpha = a.getFloat(0, 1.0f);
        this.mToAlpha = a.getFloat(1, 1.0f);
        a.recycle();
    }

    public AlphaAnimation(float fromAlpha, float toAlpha) {
        this.mFromAlpha = fromAlpha;
        this.mToAlpha = toAlpha;
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float alpha = this.mFromAlpha;
        t.setAlpha(((this.mToAlpha - alpha) * interpolatedTime) + alpha);
    }

    public boolean willChangeTransformationMatrix() {
        return false;
    }

    public boolean willChangeBounds() {
        return false;
    }

    public boolean hasAlpha() {
        return true;
    }
}

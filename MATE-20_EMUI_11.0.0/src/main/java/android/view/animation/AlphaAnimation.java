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

    /* access modifiers changed from: protected */
    @Override // android.view.animation.Animation
    public void applyTransformation(float interpolatedTime, Transformation t) {
        float alpha = this.mFromAlpha;
        t.setAlpha(((this.mToAlpha - alpha) * interpolatedTime) + alpha);
    }

    @Override // android.view.animation.Animation
    public boolean willChangeTransformationMatrix() {
        return false;
    }

    @Override // android.view.animation.Animation
    public boolean willChangeBounds() {
        return false;
    }

    @Override // android.view.animation.Animation
    public boolean hasAlpha() {
        return true;
    }
}

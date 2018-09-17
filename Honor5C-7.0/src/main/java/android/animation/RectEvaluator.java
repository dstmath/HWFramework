package android.animation;

import android.graphics.Rect;

public class RectEvaluator implements TypeEvaluator<Rect> {
    private Rect mRect;

    public RectEvaluator(Rect reuseRect) {
        this.mRect = reuseRect;
    }

    public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
        int left = startValue.left + ((int) (((float) (endValue.left - startValue.left)) * fraction));
        int top = startValue.top + ((int) (((float) (endValue.top - startValue.top)) * fraction));
        int right = startValue.right + ((int) (((float) (endValue.right - startValue.right)) * fraction));
        int bottom = startValue.bottom + ((int) (((float) (endValue.bottom - startValue.bottom)) * fraction));
        if (this.mRect == null) {
            return new Rect(left, top, right, bottom);
        }
        this.mRect.set(left, top, right, bottom);
        return this.mRect;
    }
}

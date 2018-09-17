package android.animation;

import android.graphics.PointF;

public class PointFEvaluator implements TypeEvaluator<PointF> {
    private PointF mPoint;

    public PointFEvaluator(PointF reuse) {
        this.mPoint = reuse;
    }

    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        float x = startValue.x + ((endValue.x - startValue.x) * fraction);
        float y = startValue.y + ((endValue.y - startValue.y) * fraction);
        if (this.mPoint == null) {
            return new PointF(x, y);
        }
        this.mPoint.set(x, y);
        return this.mPoint;
    }
}

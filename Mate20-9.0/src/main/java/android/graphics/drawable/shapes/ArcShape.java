package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;

public class ArcShape extends RectShape {
    private final float mStartAngle;
    private final float mSweepAngle;

    public ArcShape(float startAngle, float sweepAngle) {
        this.mStartAngle = startAngle;
        this.mSweepAngle = sweepAngle;
    }

    public final float getStartAngle() {
        return this.mStartAngle;
    }

    public final float getSweepAngle() {
        return this.mSweepAngle;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawArc(rect(), this.mStartAngle, this.mSweepAngle, true, paint);
    }

    public void getOutline(Outline outline) {
    }

    public ArcShape clone() throws CloneNotSupportedException {
        return (ArcShape) super.clone();
    }
}

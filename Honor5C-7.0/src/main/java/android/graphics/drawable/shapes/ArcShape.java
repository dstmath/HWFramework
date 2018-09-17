package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;

public class ArcShape extends RectShape {
    private float mStart;
    private float mSweep;

    public ArcShape(float startAngle, float sweepAngle) {
        this.mStart = startAngle;
        this.mSweep = sweepAngle;
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawArc(rect(), this.mStart, this.mSweep, true, paint);
    }

    public void getOutline(Outline outline) {
    }
}

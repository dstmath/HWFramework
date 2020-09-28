package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import java.util.Objects;

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

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.RectShape
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawArc(rect(), this.mStartAngle, this.mSweepAngle, true, paint);
    }

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.RectShape
    public void getOutline(Outline outline) {
    }

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.Shape, java.lang.Object, android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.RectShape
    public ArcShape clone() throws CloneNotSupportedException {
        return (ArcShape) super.clone();
    }

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.RectShape
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        ArcShape arcShape = (ArcShape) o;
        if (Float.compare(arcShape.mStartAngle, this.mStartAngle) == 0 && Float.compare(arcShape.mSweepAngle, this.mSweepAngle) == 0) {
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.shapes.Shape, android.graphics.drawable.shapes.RectShape
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Float.valueOf(this.mStartAngle), Float.valueOf(this.mSweepAngle));
    }
}

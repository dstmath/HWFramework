package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.camera2.params.TonemapCurve;

public class RectShape extends Shape {
    private RectF mRect = new RectF();

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawRect(this.mRect, paint);
    }

    public void getOutline(Outline outline) {
        RectF rect = rect();
        outline.setRect((int) Math.ceil((double) rect.left), (int) Math.ceil((double) rect.top), (int) Math.floor((double) rect.right), (int) Math.floor((double) rect.bottom));
    }

    protected void onResize(float width, float height) {
        this.mRect.set(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, width, height);
    }

    protected final RectF rect() {
        return this.mRect;
    }

    public RectShape clone() throws CloneNotSupportedException {
        RectShape shape = (RectShape) super.clone();
        shape.mRect = new RectF(this.mRect);
        return shape;
    }
}

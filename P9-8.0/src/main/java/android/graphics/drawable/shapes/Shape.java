package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.hardware.camera2.params.TonemapCurve;

public abstract class Shape implements Cloneable {
    private float mHeight;
    private float mWidth;

    public abstract void draw(Canvas canvas, Paint paint);

    public final float getWidth() {
        return this.mWidth;
    }

    public final float getHeight() {
        return this.mHeight;
    }

    public final void resize(float width, float height) {
        if (width < TonemapCurve.LEVEL_BLACK) {
            width = TonemapCurve.LEVEL_BLACK;
        }
        if (height < TonemapCurve.LEVEL_BLACK) {
            height = TonemapCurve.LEVEL_BLACK;
        }
        if (this.mWidth != width || this.mHeight != height) {
            this.mWidth = width;
            this.mHeight = height;
            onResize(width, height);
        }
    }

    public boolean hasAlpha() {
        return true;
    }

    protected void onResize(float width, float height) {
    }

    public void getOutline(Outline outline) {
    }

    public Shape clone() throws CloneNotSupportedException {
        return (Shape) super.clone();
    }
}

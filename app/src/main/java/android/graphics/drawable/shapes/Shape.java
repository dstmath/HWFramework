package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;

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
        if (width < 0.0f) {
            width = 0.0f;
        }
        if (height < 0.0f) {
            height = 0.0f;
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

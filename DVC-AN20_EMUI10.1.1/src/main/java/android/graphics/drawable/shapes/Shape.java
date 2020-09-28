package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import java.util.Objects;

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

    /* access modifiers changed from: protected */
    public void onResize(float width, float height) {
    }

    public void getOutline(Outline outline) {
    }

    @Override // java.lang.Object
    public Shape clone() throws CloneNotSupportedException {
        return (Shape) super.clone();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Shape shape = (Shape) o;
        if (Float.compare(shape.mWidth, this.mWidth) == 0 && Float.compare(shape.mHeight, this.mHeight) == 0) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.mWidth), Float.valueOf(this.mHeight));
    }
}

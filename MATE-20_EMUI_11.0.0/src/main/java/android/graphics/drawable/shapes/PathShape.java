package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import java.util.Objects;

public class PathShape extends Shape {
    private Path mPath;
    private float mScaleX;
    private float mScaleY;
    private final float mStdHeight;
    private final float mStdWidth;

    public PathShape(Path path, float stdWidth, float stdHeight) {
        this.mPath = path;
        this.mStdWidth = stdWidth;
        this.mStdHeight = stdHeight;
    }

    @Override // android.graphics.drawable.shapes.Shape
    public void draw(Canvas canvas, Paint paint) {
        canvas.save();
        canvas.scale(this.mScaleX, this.mScaleY);
        canvas.drawPath(this.mPath, paint);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.shapes.Shape
    public void onResize(float width, float height) {
        this.mScaleX = width / this.mStdWidth;
        this.mScaleY = height / this.mStdHeight;
    }

    @Override // android.graphics.drawable.shapes.Shape, java.lang.Object
    public PathShape clone() throws CloneNotSupportedException {
        PathShape shape = (PathShape) super.clone();
        shape.mPath = new Path(this.mPath);
        return shape;
    }

    @Override // android.graphics.drawable.shapes.Shape, java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        PathShape pathShape = (PathShape) o;
        if (Float.compare(pathShape.mStdWidth, this.mStdWidth) == 0 && Float.compare(pathShape.mStdHeight, this.mStdHeight) == 0 && Float.compare(pathShape.mScaleX, this.mScaleX) == 0 && Float.compare(pathShape.mScaleY, this.mScaleY) == 0 && Objects.equals(this.mPath, pathShape.mPath)) {
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.shapes.Shape, java.lang.Object
    public int hashCode() {
        return Objects.hash(Integer.valueOf(super.hashCode()), Float.valueOf(this.mStdWidth), Float.valueOf(this.mStdHeight), this.mPath, Float.valueOf(this.mScaleX), Float.valueOf(this.mScaleY));
    }
}

package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Objects;

public class RoundRectShape extends RectShape {
    private float[] mInnerRadii;
    private RectF mInnerRect;
    private RectF mInset;
    private float[] mOuterRadii;
    private Path mPath;

    public RoundRectShape(float[] outerRadii, RectF inset, float[] innerRadii) {
        if (outerRadii != null && outerRadii.length < 8) {
            throw new ArrayIndexOutOfBoundsException("outer radii must have >= 8 values");
        } else if (innerRadii == null || innerRadii.length >= 8) {
            this.mOuterRadii = outerRadii;
            this.mInset = inset;
            this.mInnerRadii = innerRadii;
            if (inset != null) {
                this.mInnerRect = new RectF();
            }
            this.mPath = new Path();
        } else {
            throw new ArrayIndexOutOfBoundsException("inner radii must have >= 8 values");
        }
    }

    @Override // android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.Shape
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(this.mPath, paint);
    }

    @Override // android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.Shape
    public void getOutline(Outline outline) {
        if (this.mInnerRect == null) {
            float radius = 0.0f;
            float[] fArr = this.mOuterRadii;
            if (fArr != null) {
                radius = fArr[0];
                for (int i = 1; i < 8; i++) {
                    if (this.mOuterRadii[i] != radius) {
                        outline.setConvexPath(this.mPath);
                        return;
                    }
                }
            }
            RectF rect = rect();
            outline.setRoundRect((int) Math.ceil((double) rect.left), (int) Math.ceil((double) rect.top), (int) Math.floor((double) rect.right), (int) Math.floor((double) rect.bottom), radius);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.Shape
    public void onResize(float w, float h) {
        super.onResize(w, h);
        RectF r = rect();
        this.mPath.reset();
        float[] fArr = this.mOuterRadii;
        if (fArr != null) {
            this.mPath.addRoundRect(r, fArr, Path.Direction.CW);
        } else {
            this.mPath.addRect(r, Path.Direction.CW);
        }
        RectF rectF = this.mInnerRect;
        if (rectF != null) {
            rectF.set(r.left + this.mInset.left, r.top + this.mInset.top, r.right - this.mInset.right, r.bottom - this.mInset.bottom);
            if (this.mInnerRect.width() < w && this.mInnerRect.height() < h) {
                float[] fArr2 = this.mInnerRadii;
                if (fArr2 != null) {
                    this.mPath.addRoundRect(this.mInnerRect, fArr2, Path.Direction.CCW);
                } else {
                    this.mPath.addRect(this.mInnerRect, Path.Direction.CCW);
                }
            }
        }
    }

    @Override // android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.Shape, java.lang.Object
    public RoundRectShape clone() throws CloneNotSupportedException {
        RoundRectShape shape = (RoundRectShape) super.clone();
        float[] fArr = this.mOuterRadii;
        float[] fArr2 = null;
        shape.mOuterRadii = fArr != null ? (float[]) fArr.clone() : null;
        float[] fArr3 = this.mInnerRadii;
        if (fArr3 != null) {
            fArr2 = (float[]) fArr3.clone();
        }
        shape.mInnerRadii = fArr2;
        shape.mInset = new RectF(this.mInset);
        shape.mInnerRect = new RectF(this.mInnerRect);
        shape.mPath = new Path(this.mPath);
        return shape;
    }

    @Override // android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.Shape, java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass() || !super.equals(o)) {
            return false;
        }
        RoundRectShape that = (RoundRectShape) o;
        if (!Arrays.equals(this.mOuterRadii, that.mOuterRadii) || !Objects.equals(this.mInset, that.mInset) || !Arrays.equals(this.mInnerRadii, that.mInnerRadii) || !Objects.equals(this.mInnerRect, that.mInnerRect) || !Objects.equals(this.mPath, that.mPath)) {
            return false;
        }
        return true;
    }

    @Override // android.graphics.drawable.shapes.RectShape, android.graphics.drawable.shapes.Shape, java.lang.Object
    public int hashCode() {
        return (((Objects.hash(Integer.valueOf(super.hashCode()), this.mInset, this.mInnerRect, this.mPath) * 31) + Arrays.hashCode(this.mOuterRadii)) * 31) + Arrays.hashCode(this.mInnerRadii);
    }
}

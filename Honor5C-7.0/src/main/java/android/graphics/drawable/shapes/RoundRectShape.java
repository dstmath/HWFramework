package android.graphics.drawable.shapes;

import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;

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

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawPath(this.mPath, paint);
    }

    public void getOutline(Outline outline) {
        if (this.mInnerRect == null) {
            float radius = 0.0f;
            if (this.mOuterRadii != null) {
                radius = this.mOuterRadii[0];
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

    protected void onResize(float w, float h) {
        super.onResize(w, h);
        RectF r = rect();
        this.mPath.reset();
        if (this.mOuterRadii != null) {
            this.mPath.addRoundRect(r, this.mOuterRadii, Direction.CW);
        } else {
            this.mPath.addRect(r, Direction.CW);
        }
        if (this.mInnerRect != null) {
            this.mInnerRect.set(r.left + this.mInset.left, r.top + this.mInset.top, r.right - this.mInset.right, r.bottom - this.mInset.bottom);
            if (this.mInnerRect.width() < w && this.mInnerRect.height() < h) {
                if (this.mInnerRadii != null) {
                    this.mPath.addRoundRect(this.mInnerRect, this.mInnerRadii, Direction.CCW);
                } else {
                    this.mPath.addRect(this.mInnerRect, Direction.CCW);
                }
            }
        }
    }

    public RoundRectShape clone() throws CloneNotSupportedException {
        float[] fArr;
        RoundRectShape shape = (RoundRectShape) super.clone();
        shape.mOuterRadii = this.mOuterRadii != null ? (float[]) this.mOuterRadii.clone() : null;
        if (this.mInnerRadii != null) {
            fArr = (float[]) this.mInnerRadii.clone();
        } else {
            fArr = null;
        }
        shape.mInnerRadii = fArr;
        shape.mInset = new RectF(this.mInset);
        shape.mInnerRect = new RectF(this.mInnerRect);
        shape.mPath = new Path(this.mPath);
        return shape;
    }
}

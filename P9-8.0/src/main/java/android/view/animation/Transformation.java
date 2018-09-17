package android.view.animation;

import android.graphics.Matrix;
import android.graphics.Rect;
import java.io.PrintWriter;

public class Transformation {
    public static final int TYPE_ALPHA = 1;
    public static final int TYPE_BOTH = 3;
    public static final int TYPE_IDENTITY = 0;
    public static final int TYPE_MATRIX = 2;
    protected float mAlpha;
    private Rect mClipRect = new Rect();
    private boolean mHasClipRect;
    protected Matrix mMatrix;
    protected int mTransformationType;

    public Transformation() {
        clear();
    }

    public void clear() {
        if (this.mMatrix == null) {
            this.mMatrix = new Matrix();
        } else {
            this.mMatrix.reset();
        }
        this.mClipRect.setEmpty();
        this.mHasClipRect = false;
        this.mAlpha = 1.0f;
        this.mTransformationType = 3;
    }

    public int getTransformationType() {
        return this.mTransformationType;
    }

    public void setTransformationType(int transformationType) {
        this.mTransformationType = transformationType;
    }

    public void set(Transformation t) {
        this.mAlpha = t.getAlpha();
        this.mMatrix.set(t.getMatrix());
        if (t.mHasClipRect) {
            setClipRect(t.getClipRect());
        } else {
            this.mHasClipRect = false;
            this.mClipRect.setEmpty();
        }
        this.mTransformationType = t.getTransformationType();
    }

    public void compose(Transformation t) {
        this.mAlpha *= t.getAlpha();
        this.mMatrix.preConcat(t.getMatrix());
        if (t.mHasClipRect) {
            Rect bounds = t.getClipRect();
            if (this.mHasClipRect) {
                setClipRect(this.mClipRect.left + bounds.left, this.mClipRect.top + bounds.top, this.mClipRect.right + bounds.right, this.mClipRect.bottom + bounds.bottom);
            } else {
                setClipRect(bounds);
            }
        }
    }

    public void postCompose(Transformation t) {
        this.mAlpha *= t.getAlpha();
        this.mMatrix.postConcat(t.getMatrix());
        if (t.mHasClipRect) {
            Rect bounds = t.getClipRect();
            if (this.mHasClipRect) {
                setClipRect(this.mClipRect.left + bounds.left, this.mClipRect.top + bounds.top, this.mClipRect.right + bounds.right, this.mClipRect.bottom + bounds.bottom);
            } else {
                setClipRect(bounds);
            }
        }
    }

    public Matrix getMatrix() {
        return this.mMatrix;
    }

    public void setAlpha(float alpha) {
        this.mAlpha = alpha;
    }

    public void setClipRect(Rect r) {
        setClipRect(r.left, r.top, r.right, r.bottom);
    }

    public void setClipRect(int l, int t, int r, int b) {
        this.mClipRect.set(l, t, r, b);
        this.mHasClipRect = true;
    }

    public Rect getClipRect() {
        return this.mClipRect;
    }

    public boolean hasClipRect() {
        return this.mHasClipRect;
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Transformation");
        toShortString(sb);
        return sb.toString();
    }

    public String toShortString() {
        StringBuilder sb = new StringBuilder(64);
        toShortString(sb);
        return sb.toString();
    }

    public void toShortString(StringBuilder sb) {
        sb.append("{alpha=");
        sb.append(this.mAlpha);
        sb.append(" matrix=");
        this.mMatrix.toShortString(sb);
        sb.append('}');
    }

    public void printShortString(PrintWriter pw) {
        pw.print("{alpha=");
        pw.print(this.mAlpha);
        pw.print(" matrix=");
        this.mMatrix.printShortString(pw);
        pw.print('}');
    }
}

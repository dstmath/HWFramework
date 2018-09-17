package android.view.animation;

import android.graphics.Rect;

public class ClipRectAnimation extends Animation {
    protected Rect mFromRect = new Rect();
    protected Rect mToRect = new Rect();

    public ClipRectAnimation(Rect fromClip, Rect toClip) {
        if (fromClip == null || toClip == null) {
            throw new RuntimeException("Expected non-null animation clip rects");
        }
        this.mFromRect.set(fromClip);
        this.mToRect.set(toClip);
    }

    public ClipRectAnimation(int fromL, int fromT, int fromR, int fromB, int toL, int toT, int toR, int toB) {
        this.mFromRect.set(fromL, fromT, fromR, fromB);
        this.mToRect.set(toL, toT, toR, toB);
    }

    protected void applyTransformation(float it, Transformation tr) {
        tr.setClipRect(this.mFromRect.left + ((int) (((float) (this.mToRect.left - this.mFromRect.left)) * it)), this.mFromRect.top + ((int) (((float) (this.mToRect.top - this.mFromRect.top)) * it)), this.mFromRect.right + ((int) (((float) (this.mToRect.right - this.mFromRect.right)) * it)), this.mFromRect.bottom + ((int) (((float) (this.mToRect.bottom - this.mFromRect.bottom)) * it)));
    }

    public boolean willChangeTransformationMatrix() {
        return false;
    }
}

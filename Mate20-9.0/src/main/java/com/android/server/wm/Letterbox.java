package com.android.server.wm;

import android.graphics.Rect;
import android.view.SurfaceControl;
import java.lang.annotation.RCUnownedThisRef;
import java.util.function.Supplier;

public class Letterbox {
    private static final Rect EMPTY_RECT = new Rect();
    private final LetterboxSurface mBottom = new LetterboxSurface("bottom");
    /* access modifiers changed from: private */
    public final Supplier<SurfaceControl.Builder> mFactory;
    private final Rect mInner = new Rect();
    private final LetterboxSurface mLeft = new LetterboxSurface("left");
    private final Rect mOuter = new Rect();
    private final LetterboxSurface mRight = new LetterboxSurface("right");
    private final LetterboxSurface mTop = new LetterboxSurface("top");

    @RCUnownedThisRef
    private class LetterboxSurface {
        private final Rect mLayoutFrame = new Rect();
        private SurfaceControl mSurface;
        private final Rect mSurfaceFrame = new Rect();
        private final String mType;

        public LetterboxSurface(String type) {
            this.mType = type;
        }

        public void layout(int left, int top, int right, int bottom) {
            if (this.mLayoutFrame.left != left || this.mLayoutFrame.top != top || this.mLayoutFrame.right != right || this.mLayoutFrame.bottom != bottom) {
                this.mLayoutFrame.set(left, top, right, bottom);
            }
        }

        private void createSurface() {
            this.mSurface = ((SurfaceControl.Builder) Letterbox.this.mFactory.get()).setName("Letterbox - " + this.mType).setFlags(4).setColorLayer(true).build();
            this.mSurface.setLayer(-1);
            this.mSurface.setColor(new float[]{0.0f, 0.0f, 0.0f});
        }

        public void destroy() {
            if (this.mSurface != null) {
                this.mSurface.destroy();
                this.mSurface = null;
            }
        }

        public int getWidth() {
            return Math.max(0, this.mLayoutFrame.width());
        }

        public int getHeight() {
            return Math.max(0, this.mLayoutFrame.height());
        }

        public boolean isOverlappingWith(Rect rect) {
            if (getWidth() <= 0 || getHeight() <= 0) {
                return false;
            }
            return Rect.intersects(rect, this.mLayoutFrame);
        }

        public void applySurfaceChanges(SurfaceControl.Transaction t) {
            if (!this.mSurfaceFrame.equals(this.mLayoutFrame)) {
                this.mSurfaceFrame.set(this.mLayoutFrame);
                if (!this.mSurfaceFrame.isEmpty()) {
                    if (this.mSurface == null) {
                        createSurface();
                    }
                    t.setPosition(this.mSurface, (float) this.mSurfaceFrame.left, (float) this.mSurfaceFrame.top);
                    t.setSize(this.mSurface, this.mSurfaceFrame.width(), this.mSurfaceFrame.height());
                    t.setSurfaceLowResolutionInfo(this.mSurface, 1.0f, 2);
                    t.show(this.mSurface);
                } else if (this.mSurface != null) {
                    t.hide(this.mSurface);
                }
            }
        }

        public boolean needsApplySurfaceChanges() {
            return !this.mSurfaceFrame.equals(this.mLayoutFrame);
        }
    }

    public Letterbox(Supplier<SurfaceControl.Builder> surfaceControlFactory) {
        this.mFactory = surfaceControlFactory;
    }

    public void layout(Rect outer, Rect inner) {
        this.mOuter.set(outer);
        this.mInner.set(inner);
        this.mTop.layout(outer.left, outer.top, inner.right, inner.top);
        this.mLeft.layout(outer.left, inner.top, inner.left, outer.bottom);
        this.mBottom.layout(inner.left, inner.bottom, outer.right, outer.bottom);
        this.mRight.layout(inner.right, outer.top, outer.right, inner.bottom);
    }

    public Rect getInsets() {
        return new Rect(this.mLeft.getWidth(), this.mTop.getHeight(), this.mRight.getWidth(), this.mBottom.getHeight());
    }

    public boolean isOverlappingWith(Rect rect) {
        return this.mTop.isOverlappingWith(rect) || this.mLeft.isOverlappingWith(rect) || this.mBottom.isOverlappingWith(rect) || this.mRight.isOverlappingWith(rect);
    }

    public void hide() {
        layout(EMPTY_RECT, EMPTY_RECT);
    }

    public void destroy() {
        this.mOuter.setEmpty();
        this.mInner.setEmpty();
        this.mTop.destroy();
        this.mLeft.destroy();
        this.mBottom.destroy();
        this.mRight.destroy();
    }

    public boolean needsApplySurfaceChanges() {
        return this.mTop.needsApplySurfaceChanges() || this.mLeft.needsApplySurfaceChanges() || this.mBottom.needsApplySurfaceChanges() || this.mRight.needsApplySurfaceChanges();
    }

    public void applySurfaceChanges(SurfaceControl.Transaction t) {
        this.mTop.applySurfaceChanges(t);
        this.mLeft.applySurfaceChanges(t);
        this.mBottom.applySurfaceChanges(t);
        this.mRight.applySurfaceChanges(t);
    }
}

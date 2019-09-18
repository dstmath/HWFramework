package com.android.server.wm;

import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.Slog;
import android.view.Surface;
import android.view.SurfaceControl;

class InsetSurfaceOverlay {
    private static final String TAG = "WindowManager";
    private DisplayContent mDisplayContent;
    private int mHeight;
    private SurfaceControl mParent;
    private int mPositionX;
    private int mPositionY;
    private final Surface mSurface = new Surface();
    private SurfaceControl mSurfaceControl;
    private boolean mVisible;
    private int mWidth;
    private int mZOrder;

    public InsetSurfaceOverlay(DisplayContent dc, int zOrder, int width, int height, int positionX, int positionY, SurfaceControl parent) {
        this.mDisplayContent = dc;
        this.mZOrder = zOrder;
        this.mWidth = width;
        this.mHeight = height;
        this.mPositionX = positionX;
        this.mPositionY = positionY;
        this.mParent = parent;
    }

    public void createSurface() {
        if (this.mSurfaceControl == null) {
            SurfaceControl ctrl = null;
            try {
                Slog.i(TAG, "InsetSurfaceOverlay create");
                ctrl = this.mDisplayContent.makeOverlay().setName("InsetSurfaceOverlay").setSize(this.mWidth, this.mHeight).setParent(this.mParent).build();
                ctrl.setPosition((float) this.mPositionX, (float) this.mPositionY);
                this.mSurface.copyFrom(ctrl);
            } catch (Surface.OutOfResourcesException e) {
            }
            this.mSurfaceControl = ctrl;
        }
    }

    public void updateSurface(WindowState win) {
        if (this.mSurfaceControl == null) {
            createSurface();
        }
        if (this.mSurfaceControl != null) {
            Slog.i(TAG, "InsetSurfaceOverlay updateSurface");
            this.mSurfaceControl.setSize(win.mFrame.width(), this.mHeight);
            this.mSurfaceControl.setPosition((float) win.mFrame.left, (float) ((win.mFrame.bottom - this.mHeight) - win.mLastSurfacePosition.y));
        }
    }

    private void drawIfNeeded() {
        Canvas c = null;
        try {
            c = this.mSurface.lockCanvas(null);
        } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
        }
        if (c != null) {
            c.drawColor(-3091492, PorterDuff.Mode.SRC);
            this.mSurface.unlockCanvasAndPost(c);
        }
    }

    public void show() {
        createSurface();
        if (this.mSurfaceControl != null && !this.mVisible) {
            Slog.i(TAG, "InsetSurfaceOverlay show");
            drawIfNeeded();
            this.mSurfaceControl.show();
            this.mVisible = true;
        }
    }

    public void hide() {
        if (this.mSurfaceControl != null && this.mVisible) {
            Slog.i(TAG, "InsetSurfaceOverlay hide");
            this.mSurfaceControl.hide();
            this.mVisible = false;
        }
    }

    public void hide(SurfaceControl.Transaction transaction) {
        if (this.mSurfaceControl != null && this.mVisible) {
            try {
                Slog.i(TAG, "InsetSurfaceOverlay hide in transaction");
                transaction.hide(this.mSurfaceControl);
                this.mVisible = false;
            } catch (RuntimeException e) {
                Slog.w(TAG, "Exception hiding surface in " + this);
            }
        }
    }

    public void destroy() {
        if (this.mSurfaceControl != null) {
            Slog.i(TAG, "InsetSurfaceOverlay destroy");
            this.mSurfaceControl.destroy();
            this.mSurfaceControl = null;
            this.mVisible = false;
        }
    }
}

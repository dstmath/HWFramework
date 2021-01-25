package com.android.server.wm;

import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceControl;

public class InsetSurface {
    private static final int MULTIPLE = 101;
    private static final String TAG = "InsetSurface";
    private final Rect mCurrentSurfacePosition = new Rect();
    private final Rect mCurrentSurfaceRect = new Rect();
    private DisplayContent mDc;
    private final Rect mLastSurfacePosition = new Rect();
    private SurfaceControl mSurfaceControl;
    private final String mType;

    public InsetSurface(String type) {
        this.mType = type;
    }

    public void layout(int left, int top, int right, int bottom) {
        this.mCurrentSurfacePosition.set(left, top, right, bottom);
        if (!this.mCurrentSurfacePosition.isEmpty()) {
            Log.i(TAG, "layout SurfacePosition:" + this.mCurrentSurfacePosition + " mType:" + this.mType);
        }
    }

    public void setDisplayContent(DisplayContent dc) {
        this.mDc = dc;
    }

    private void createSurface(Rect rect) {
        SurfaceControl ctrl = null;
        try {
            SurfaceControl.Builder makeOverlay = this.mDc.makeOverlay();
            ctrl = makeOverlay.setName("InsetSurface " + this.mType).setColorLayer().build();
            ctrl.setLayer(WindowManagerConstants.INSETSURFACE_LAYER);
            ctrl.setPosition((float) rect.left, (float) rect.top);
            ctrl.setWindowCrop(rect.width(), rect.height());
            ctrl.setColor(new float[]{0.0f, 0.0f, 0.0f});
            this.mCurrentSurfaceRect.set(rect);
            ctrl.setLowResolutionInfo(1.0f, 2);
            Log.i(TAG, "createSurface " + this.mType);
        } catch (Surface.OutOfResourcesException e) {
            Log.e(TAG, "createSurface fail");
        }
        this.mSurfaceControl = ctrl;
    }

    public void remove() {
        if (this.mSurfaceControl != null) {
            Log.i(TAG, "remove " + this.mType);
            this.mLastSurfacePosition.setEmpty();
            this.mCurrentSurfaceRect.setEmpty();
            this.mSurfaceControl.remove();
            this.mSurfaceControl = null;
        }
    }

    private void reset() {
        if (this.mSurfaceControl != null) {
            Log.i(TAG, "reset " + this.mType);
            this.mCurrentSurfaceRect.setEmpty();
            this.mSurfaceControl.remove();
            this.mSurfaceControl = null;
        }
    }

    public void show(SurfaceControl relativeTo) {
        show(relativeTo, null);
    }

    public void show(SurfaceControl relativeTo, SurfaceControl.Transaction transaction) {
        if (!this.mLastSurfacePosition.equals(this.mCurrentSurfacePosition)) {
            this.mLastSurfacePosition.set(this.mCurrentSurfacePosition);
            if (!this.mLastSurfacePosition.isEmpty()) {
                if (this.mSurfaceControl == null) {
                    createSurface(this.mLastSurfacePosition);
                } else if (!this.mLastSurfacePosition.equals(this.mCurrentSurfaceRect)) {
                    reset();
                    createSurface(this.mLastSurfacePosition);
                }
                Log.i(TAG, "show " + this.mType);
                if (transaction != null) {
                    transaction.setLayer(this.mSurfaceControl, 0);
                    transaction.show(this.mSurfaceControl);
                    return;
                }
                if (relativeTo != null) {
                    this.mSurfaceControl.setRelativeLayer(relativeTo, 0);
                }
                this.mSurfaceControl.show();
            }
        }
    }

    public void setLayer(SurfaceControl.Transaction transaction, SurfaceControl relativeTo, int layer) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            return;
        }
        if (relativeTo != null) {
            transaction.setRelativeLayer(surfaceControl, relativeTo, layer);
        } else {
            transaction.setLayer(surfaceControl, 0);
        }
    }

    public void hide(SurfaceControl.Transaction t) {
        if (this.mSurfaceControl != null) {
            Log.i(TAG, "hide " + this.mType);
            this.mLastSurfacePosition.setEmpty();
            t.hide(this.mSurfaceControl);
        }
    }
}

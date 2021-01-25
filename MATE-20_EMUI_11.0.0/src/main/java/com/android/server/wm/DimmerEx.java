package com.android.server.wm;

import android.graphics.Rect;
import android.view.SurfaceControl;

public class DimmerEx {
    private Dimmer mDimmer;

    public void setDimmer(Dimmer dimmer) {
        this.mDimmer = dimmer;
    }

    public Dimmer getDimmer() {
        return this.mDimmer;
    }

    public void resetDimStates() {
        this.mDimmer.resetDimStates();
    }

    public void dimBelow(SurfaceControl.Transaction transaction, WindowStateEx windowStateEx, float alpha) {
        this.mDimmer.dimBelow(transaction, windowStateEx == null ? null : windowStateEx.getWindowState(), alpha);
    }

    public boolean isDimStateVisible() {
        if (this.mDimmer.mDimState != null) {
            return this.mDimmer.mDimState.isVisible;
        }
        return false;
    }

    public void stopDim(SurfaceControl.Transaction transaction) {
        this.mDimmer.stopDim(transaction);
    }

    public boolean updateDims(SurfaceControl.Transaction transaction, Rect bounds) {
        return this.mDimmer.updateDims(transaction, bounds);
    }
}

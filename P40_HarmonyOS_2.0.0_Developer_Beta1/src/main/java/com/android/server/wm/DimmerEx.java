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

    public void dimBelow(WindowStateEx windowStateEx, float alpha) {
        Dimmer dimmer = this.mDimmer;
        if (dimmer != null && windowStateEx != null) {
            dimmer.dimBelow(windowStateEx.getPendingTransaction(), windowStateEx.getWindowState(), alpha);
        }
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

    public void stopDim(WindowStateEx windowStateEx) {
        Dimmer dimmer = this.mDimmer;
        if (dimmer != null && windowStateEx != null) {
            dimmer.stopDim(windowStateEx.getPendingTransaction());
        }
    }

    public boolean updateDims(SurfaceControl.Transaction transaction, Rect bounds) {
        return this.mDimmer.updateDims(transaction, bounds);
    }

    public boolean updateDims(WindowStateEx windowStateEx, Rect bounds) {
        Dimmer dimmer = this.mDimmer;
        if (dimmer == null || windowStateEx == null) {
            return false;
        }
        return dimmer.updateDims(windowStateEx.getPendingTransaction(), bounds);
    }
}

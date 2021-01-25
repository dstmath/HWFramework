package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.Display;
import java.util.List;

public class DisplayContentBridge extends DisplayContent {
    private DisplayContentBridgeEx mDisplayContentEx;

    public DisplayContentBridge(Display display, WindowManagerService service, ActivityDisplay activityDisplay, DisplayContentBridgeEx displayContentBridgeEx) {
        super(display, service, activityDisplay);
        this.mDisplayContentEx = displayContentBridgeEx;
        this.mDisplayContentEx.initDisplayRoundCorner(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mDisplayInfo.logicalDensityDpi);
    }

    public DisplayContentBridgeEx getDisplayContentBridgeEx() {
        return this.mDisplayContentEx;
    }

    public void setDisplayContentEx(DisplayContentBridgeEx displayContentEx) {
        this.mDisplayContentEx = displayContentEx;
    }

    public void computeScreenConfiguration(Configuration config) {
        this.mDisplayContentEx.computeScreenConfiguration(config);
    }

    public void aospComputeScreenConfiguration(Configuration config) {
        DisplayContentBridge.super.computeScreenConfiguration(config);
    }

    public void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        DisplayContentBridgeEx displayContentBridgeEx = this.mDisplayContentEx;
        if (displayContentBridgeEx == null) {
            DisplayContentBridge.super.updateBaseDisplayMetrics(baseWidth, baseHeight, baseDensity);
        } else {
            displayContentBridgeEx.updateBaseDisplayMetrics(baseWidth, baseHeight, baseDensity);
        }
    }

    public void aospUpdateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        DisplayContentBridge.super.updateBaseDisplayMetrics(baseWidth, baseHeight, baseDensity);
    }

    public Rect getSafeInsetsByType(int type) {
        return this.mDisplayContentEx.getSafeInsetsByType(type);
    }

    public List<Rect> getBoundsByType(int type) {
        return this.mDisplayContentEx.getBoundsByType(type);
    }

    /* access modifiers changed from: package-private */
    public List taskIdFromTop() {
        return this.mDisplayContentEx.taskIdFromTop();
    }

    public void setDisplayRotationFR(int rotation) {
        this.mDisplayContentEx.setDisplayRotationFR(rotation);
    }

    public void togglePCMode(boolean isPcMode) {
        this.mDisplayContentEx.togglePCMode(isPcMode);
    }

    public boolean updateRotationUnchecked(boolean isForceUpdate) {
        return this.mDisplayContentEx.updateRotationUnchecked(isForceUpdate);
    }

    public boolean aospUpdateRotationUnchecked(boolean isForceUpdate) {
        return DisplayContentBridge.super.updateRotationUnchecked(isForceUpdate);
    }

    public void performLayout(boolean isInitial, boolean isUpdateInputWindows) {
        this.mDisplayContentEx.performLayout(isInitial, isUpdateInputWindows);
    }

    public void aospPerformLayout(boolean isInitial, boolean isUpdateInputWindows) {
        DisplayContentBridge.super.performLayout(isInitial, isUpdateInputWindows);
    }

    /* access modifiers changed from: protected */
    public void uploadOrientation(int rotation) {
        this.mDisplayContentEx.uploadOrientation(rotation);
    }

    public boolean shouldDropMotionEventForTouchPad(float floatX, float floatY) {
        return this.mDisplayContentEx.shouldDropMotionEventForTouchPad(floatX, floatY);
    }

    public boolean hasLighterViewInPCCastMode() {
        return this.mDisplayContentEx.hasLighterViewInPCCastMode();
    }
}

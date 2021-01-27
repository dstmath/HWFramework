package com.android.server.wm;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.IBinder;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.server.policy.HwPhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.annotation.HwSystemApi;

public class WindowStateEx {
    private AppWindowTokenExt mAppWindowTokenExt;
    private WindowState mWindowState;
    private WindowManagerServiceEx mWmsEx;

    public WindowStateEx() {
    }

    public WindowStateEx(WindowState windowState) {
        this.mWindowState = windowState;
        if (windowState != null && windowState.mAppToken != null) {
            this.mAppWindowTokenExt = new AppWindowTokenExt();
            this.mAppWindowTokenExt.setAppWindowToken(this.mWindowState.mAppToken);
        }
    }

    public void setWindowState(WindowState windowState) {
        this.mWindowState = windowState;
        if (windowState != null && windowState.mAppToken != null) {
            this.mAppWindowTokenExt = new AppWindowTokenExt();
            this.mAppWindowTokenExt.setAppWindowToken(this.mWindowState.mAppToken);
        }
    }

    public WindowState getWindowState() {
        return this.mWindowState;
    }

    public void resetWindowState(Object windowState) {
        if (windowState != null && (windowState instanceof WindowState)) {
            this.mWindowState = (WindowState) windowState;
        }
    }

    public int getDisplayId() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getDisplayId();
        }
        return 0;
    }

    public WindowManager.LayoutParams getAttrs() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getAttrs();
        }
        return null;
    }

    public Rect getVisibleFrameLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getVisibleFrameLw();
        }
        return null;
    }

    public Rect getDisplayFrameLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getDisplayFrameLw();
        }
        return null;
    }

    public Rect getVisibleInsets() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getVisibleInsets();
        }
        return null;
    }

    public Rect getContentFrameLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getContentFrameLw();
        }
        return null;
    }

    public Rect getFrameLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getFrameLw();
        }
        return null;
    }

    public Rect getGivenContentInsetsLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getGivenContentInsetsLw();
        }
        return null;
    }

    public WindowManager.LayoutParams getOriginAttrs() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getOriginAttrs();
        }
        return null;
    }

    public IBinder getAppToken() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getAppToken();
        }
        return null;
    }

    public AppWindowToken getAppWindowToken() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.mAppToken;
        }
        return null;
    }

    public AppWindowTokenExt getAppWindowTokenEx() {
        AppWindowTokenExt appWindowTokenExt = this.mAppWindowTokenExt;
        if (appWindowTokenExt != null) {
            return appWindowTokenExt;
        }
        WindowState windowState = this.mWindowState;
        if (windowState == null || windowState.mAppToken == null) {
            return null;
        }
        this.mAppWindowTokenExt = new AppWindowTokenExt();
        this.mAppWindowTokenExt.setAppWindowToken(this.mWindowState.mAppToken);
        return this.mAppWindowTokenExt;
    }

    public TaskEx getTaskEx() {
        WindowState windowState = this.mWindowState;
        if (windowState == null || windowState.getTask() == null) {
            return null;
        }
        TaskEx taskEx = new TaskEx();
        taskEx.setTask(this.mWindowState.getTask());
        return taskEx;
    }

    public boolean inHwMagicWindowingMode() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.inHwMagicWindowingMode();
        }
        return false;
    }

    public Rect getBounds() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getBounds();
        }
        return null;
    }

    public boolean isMwIsCornerCropSet() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.mMwIsCornerCropSet;
        }
        return false;
    }

    public void setMwIsCornerCropSet(boolean mMwIsCornerCropSet) {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.mMwIsCornerCropSet = mMwIsCornerCropSet;
        }
    }

    public WindowStateAnimator getWindowStateAnimator() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.mWinAnimator;
        }
        return null;
    }

    public float getMwUsedScaleFactor() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.mMwUsedScaleFactor;
        }
        return 0.0f;
    }

    public void setMwUsedScaleFactor(float mMwUsedScaleFactor) {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.mMwUsedScaleFactor = mMwUsedScaleFactor;
        }
    }

    public float getMwScaleRatioConfig() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.mMwScaleRatioConfig;
        }
        return 0.0f;
    }

    public void setMwScaleRatioConfig(float mMwScaleRatioConfig) {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.mMwScaleRatioConfig = mMwScaleRatioConfig;
        }
    }

    public boolean isMwScaleEnabled() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.mMwScaleEnabled;
        }
        return false;
    }

    public void setMwScaleEnabled(boolean mMwScaleEnabled) {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.mMwScaleEnabled = mMwScaleEnabled;
        }
    }

    public void getTouchableRegion(Region outRegion) {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.getTouchableRegion(outRegion);
        }
    }

    public boolean isVisible() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.isVisible();
        }
        return false;
    }

    public boolean isVisibleLw() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.isVisibleLw();
        }
        return false;
    }

    public int getWindowingMode() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getWindowingMode();
        }
        return 0;
    }

    public boolean isAppTokenNotEqual(ActivityRecordEx activityRecordEx) {
        ActivityRecord ar = activityRecordEx.getActivityRecord();
        WindowState windowState = this.mWindowState;
        if (windowState == null || windowState.mAppToken == null || ar == null || ar.mAppWindowToken == null || this.mWindowState.mAppToken.appToken == ar.mAppWindowToken.appToken) {
            return false;
        }
        return true;
    }

    public boolean isChildWindow() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.isChildWindow();
        }
        return false;
    }

    public WindowStateEx getTopParentWindow() {
        WindowState windowState = this.mWindowState;
        if (windowState == null || windowState.getTopParentWindow() == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(this.mWindowState.getTopParentWindow());
        return windowStateEx;
    }

    public boolean equalsWindowState(WindowStateEx windowStateEx) {
        if (this.mWindowState == null || windowStateEx.getWindowState() == null || this.mWindowState != windowStateEx.getWindowState()) {
            return false;
        }
        return true;
    }

    public void setHwFlags(int hwFlags) {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.getAttrs().hwFlags = hwFlags;
        }
    }

    public int getHwFlags() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getAttrs().hwFlags;
        }
        return 0;
    }

    public int getSubtreeSystemUiVisibility() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getAttrs().subtreeSystemUiVisibility;
        }
        return 0;
    }

    public WindowStateEx getParentWindow() {
        WindowState windowState = this.mWindowState;
        if (windowState == null || windowState.getParentWindow() == null) {
            return null;
        }
        WindowStateEx pWindowStateEx = new WindowStateEx();
        pWindowStateEx.setWindowState(this.mWindowState.getParentWindow());
        return pWindowStateEx;
    }

    public void setWinAnimatorOpaqueLocked(boolean opaque) {
        WindowState windowState = this.mWindowState;
        if (windowState != null && windowState.mWinAnimator != null) {
            this.mWindowState.mWinAnimator.setOpaqueLocked(opaque);
        }
    }

    public static WindowStateEx getWindowStateEx(WindowManagerPolicyEx.WindowStateEx windowStateEx) {
        if (windowStateEx == null) {
            return null;
        }
        WindowManagerPolicy.WindowState ws = windowStateEx.getWindowState();
        WindowState imeWin = null;
        if (ws instanceof WindowState) {
            imeWin = (WindowState) ws;
        }
        WindowStateEx stateEx = new WindowStateEx();
        stateEx.setWindowState(imeWin);
        return stateEx;
    }

    public int getBaseType() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getBaseType();
        }
        return 0;
    }

    public static WindowStateEx getFocusedWindow(HwPhoneWindowManager policy) {
        WindowStateEx windowStateEx = new WindowStateEx();
        WindowManagerPolicy.WindowState windowState = policy.getFocusedWindow();
        if (windowState == null || !(windowState instanceof WindowState)) {
            return null;
        }
        windowStateEx.setWindowState((WindowState) windowState);
        return windowStateEx;
    }

    public boolean isWinStateNull() {
        return this.mWindowState == null;
    }

    /* access modifiers changed from: protected */
    public int identityHashCode() {
        return System.identityHashCode(this.mWindowState);
    }

    public boolean inFreeformWindowingMode() {
        return this.mWindowState.inFreeformWindowingMode();
    }

    public DisplayContentEx getDisplayContentEx() {
        return new DisplayContentEx(this.mWindowState.getDisplayContent());
    }

    public boolean isAppWindowTokenNotNull() {
        WindowState windowState = this.mWindowState;
        if (windowState == null || windowState.mAppToken == null) {
            return false;
        }
        return true;
    }

    public boolean isParentNotNull() {
        if (this.mWindowState.getParent() != null) {
            return true;
        }
        return false;
    }

    public WindowContainer getParent() {
        WindowState windowState = this.mWindowState;
        if (windowState == null) {
            return null;
        }
        return windowState.getParent();
    }

    public DimmerEx getMwDimmer() {
        Dimmer dimmer;
        WindowState windowState = this.mWindowState;
        if (windowState == null || (dimmer = windowState.mMWDimmer) == null) {
            return null;
        }
        DimmerEx dimmerEx = new DimmerEx();
        dimmerEx.setDimmer(dimmer);
        return dimmerEx;
    }

    public void setMwDimmer(DimmerEx dimmerEx) {
        if (dimmerEx == null) {
            this.mWindowState.mMWDimmer = null;
            return;
        }
        this.mWindowState.mMWDimmer = dimmerEx.getDimmer();
    }

    public DimmerEx getDimmer() {
        Dimmer dimmer = this.mWindowState.getDimmer();
        if (dimmer == null) {
            return null;
        }
        DimmerEx dimmerEx = new DimmerEx();
        dimmerEx.setDimmer(dimmer);
        return dimmerEx;
    }

    public boolean isVisibleNow() {
        return this.mWindowState.isVisibleNow();
    }

    public SurfaceControl.Transaction getPendingTransaction() {
        return this.mWindowState.getPendingTransaction();
    }

    public WindowFramesEx getWindowFrames() {
        WindowFrames winFrames = this.mWindowState.getWindowFrames();
        WindowFramesEx winFrameEx = new WindowFramesEx();
        winFrameEx.setWindowFrames(winFrames);
        return winFrameEx;
    }

    public float getGlobalScale() {
        return this.mWindowState.mGlobalScale;
    }

    public void scheduleAnimation() {
        this.mWindowState.scheduleAnimation();
    }

    public boolean isHidden() {
        return this.mWindowState.mHidden;
    }

    public boolean isForceSeamlesslyRotate() {
        return this.mWindowState.mForceSeamlesslyRotate;
    }

    public Matrix getTmpMatrix() {
        return this.mWindowState.mTmpMatrix;
    }

    public boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        return this.mWindowState.isIdentityMatrix(dsdx, dtdx, dsdy, dtdy);
    }

    public boolean isDragResizeChanged() {
        return this.mWindowState.isDragResizeChanged();
    }

    public WindowContainerEx getParentEx() {
        WindowState windowState = this.mWindowState;
        if (windowState == null) {
            return null;
        }
        return new WindowContainerEx(windowState.getParent());
    }

    public int getHightFromBounds() {
        return this.mWindowState.getBounds().height();
    }

    public int getWidthFromBounds() {
        return this.mWindowState.getBounds().width();
    }

    public String getTokenString() {
        return this.mWindowState.mToken.toString();
    }

    public WindowManagerServiceEx getWmServicesEx() {
        WindowState windowState;
        if (!(this.mWmsEx != null || (windowState = this.mWindowState) == null || windowState.mWmService == null)) {
            this.mWmsEx = new WindowManagerServiceEx();
            this.mWmsEx.setWindowManagerService(this.mWindowState.mWmService);
        }
        return this.mWmsEx;
    }

    public int getRequestedWidth() {
        return this.mWindowState.mRequestedWidth;
    }

    public int getRequestedHeight() {
        return this.mWindowState.mRequestedHeight;
    }

    public int getLayer() {
        return this.mWindowState.mLayer;
    }

    public int getBaseLayer() {
        return this.mWindowState.mBaseLayer;
    }

    public int getSubLayer() {
        return this.mWindowState.mSubLayer;
    }

    public int getAppOp() {
        return this.mWindowState.mAppOp;
    }

    public SessionAospEx getSessionEx() {
        if (this.mWindowState == null) {
            return null;
        }
        SessionAospEx sessionEx = new SessionAospEx();
        sessionEx.setSession(this.mWindowState.mSession);
        return sessionEx;
    }

    public String getOwningPackage() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            return windowState.getOwningPackage();
        }
        return "";
    }

    public static int getInsizeDp() {
        return 10;
    }

    public String toString() {
        WindowState state = getWindowState();
        return state != null ? state.toString() : "null";
    }

    @HwSystemApi
    public boolean isEntranceAnimation() {
        WindowState windowState = this.mWindowState;
        return (windowState == null || windowState.mWinAnimator == null || !this.mWindowState.mWinAnimator.mAnimationIsEntrance) ? false : true;
    }

    @HwSystemApi
    public void cancelAnimation() {
        WindowState windowState = this.mWindowState;
        if (windowState != null) {
            windowState.cancelAnimation();
        }
    }
}

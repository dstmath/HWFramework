package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.view.IWindowLayoutObserver;
import android.view.IWindowManager.Stub;

public abstract class AbsWindowManagerService extends Stub {
    public static final int TOP_LAYER = 400000;
    protected static boolean mUsingHwNavibar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    Configuration mCurNaviConfiguration;
    protected boolean mIgnoreFrozen = false;
    public int mLazyModeOn;
    protected boolean mPCLauncherFocused = false;

    void setPCLauncherFocused(boolean focus) {
    }

    boolean getPCLauncherFocused() {
        return this.mPCLauncherFocused;
    }

    public int getLazyMode() {
        return 0;
    }

    public void setLazyMode(int lazyMode) {
    }

    public int getPCScreenDisplayMode() {
        return 0;
    }

    protected void setCropOnSingleHandMode(int singleHandleMode, boolean isMultiWindowApp, int dw, int dh, Rect crop) {
    }

    protected void hwProcessOnMatrix(int rotation, int width, int height, Rect frame, Matrix outMatrix) {
    }

    public boolean isCoverOpen() {
        return true;
    }

    public void setCoverManagerState(boolean isCoverOpen) {
    }

    public void freezeOrThawRotation(int rotation) {
    }

    protected void sendUpdateAppOpsState() {
    }

    protected void setAppOpHideHook(WindowState win, boolean visible) {
    }

    protected void setAppOpVisibilityLwHook(WindowState win, int mode) {
    }

    protected void setVisibleFromParent(WindowState win) {
    }

    public void setNaviBarFlag() {
    }

    public void setFocusedAppForNavi(IBinder token) {
    }

    protected void updateInputImmersiveMode() {
    }

    public void reevaluateStatusBarSize(boolean layoutNaviBar) {
    }

    public Configuration getCurNaviConfiguration() {
        return null;
    }

    protected void addWindowReport(WindowState win, int mode) {
    }

    protected void removeWindowReport(WindowState win) {
    }

    protected void updateAppOpsStateReport(int ops, String packageName) {
    }

    protected void checkKeyguardDismissDoneLocked() {
    }

    public void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
    }

    public void updateResourceConfiguration(int displayId, int density, int width, int height) {
    }

    public boolean isSplitMode() {
        return false;
    }

    public void setSplittable(boolean splittable) {
    }

    public int getLayerIndex(String appName, int windowType) {
        return 0;
    }

    protected boolean shouldHideIMExitAnim(WindowState win) {
        return false;
    }

    public void registerWindowObserver(IWindowLayoutObserver observer, long period) throws RemoteException {
    }

    public void unRegisterWindowObserver(IWindowLayoutObserver observer) throws RemoteException {
    }

    public void showWallpaperIfNeed(WindowState w) {
    }

    protected boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
        return false;
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, int pid, String processName) {
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, String componentName) {
    }

    protected void setHwSecureScreen(WindowState win) {
    }

    public void startIntelliServiceFR(int orientation) {
    }
}

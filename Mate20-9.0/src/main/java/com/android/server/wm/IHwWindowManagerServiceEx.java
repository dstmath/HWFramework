package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.MergedConfiguration;
import android.view.WindowManager;
import com.huawei.android.view.HwTaskSnapshotWrapper;
import java.util.ArrayList;
import java.util.List;

public interface IHwWindowManagerServiceEx {
    public static final int NOTCH_MODE_ALWAYS = 1;
    public static final int NOTCH_MODE_NEVER = 2;

    void addWindowReport(WindowState windowState, int i);

    void adjustWindowPosForPadPC(Rect rect, Rect rect2, WindowState windowState, WindowState windowState2, WindowState windowState3);

    boolean checkAppOrientationForForceRotation(AppWindowToken appWindowToken);

    void checkSingleHandMode(AppWindowToken appWindowToken, AppWindowToken appWindowToken2);

    void computeShownFrameLockedByPCScreenDpMode(int i);

    boolean detectSafeMode();

    void freezeOrThawRotation(int i);

    void getAppDisplayRect(float f, Rect rect, int i, int i2);

    int getAppUseNotchMode(String str);

    Configuration getCurNaviConfiguration();

    void getCurrFocusedWinInExtDisplay(Bundle bundle);

    float getDefaultNonFullMaxRatio();

    float getDeviceMaxRatio();

    float getExclusionNavBarMaxRatio();

    int getFocusWindowWidth(WindowState windowState, WindowState windowState2);

    Rect getFocuseWindowVisibleFrame(WindowManagerService windowManagerService);

    HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(TaskSnapshotController taskSnapshotController, WindowState windowState, boolean z);

    boolean getIgnoreFrozen();

    int getLazyModeEx();

    float getLazyModeScale();

    List<String> getNotchSystemApps();

    int getPCScreenDisplayMode();

    float getPCScreenScale();

    boolean getSafeMode();

    ArrayList<WindowState> getSecureScreenWindow();

    Rect getTopAppDisplayBounds(float f, int i, int i2);

    String getTopAppPackageByWindowMode(int i, RootWindowContainer rootWindowContainer);

    List<Bundle> getVisibleWindows(int i);

    void handleNewDisplayConfiguration(Configuration configuration, int i);

    boolean hasLighterViewInPCCastMode();

    void hwSystemReady();

    boolean isCoverOpen();

    boolean isDisplayOkForAnimation(int i, int i2, int i3, AppWindowToken appWindowToken);

    boolean isFullScreenDevice();

    boolean isInFoldFullDisplayMode();

    boolean isInNotchAppWhitelist(WindowState windowState);

    boolean isSupportSingleHand();

    void layoutWindowForPadPCMode(WindowState windowState, WindowState windowState2, WindowState windowState3, Rect rect, Rect rect2, Rect rect3, Rect rect4, int i);

    void notifyFingerWinCovered(boolean z, Rect rect);

    void onChangeConfiguration(MergedConfiguration mergedConfiguration, WindowState windowState);

    void performhwLayoutAndPlaceSurfacesLocked();

    void preAddWindow(WindowManager.LayoutParams layoutParams);

    void prepareForForceRotation(IBinder iBinder, String str, int i, String str2);

    void reevaluateStatusBarSize(boolean z);

    void removeSecureScreenWindow(WindowState windowState);

    void removeWindowReport(WindowState windowState);

    void reportLazyModeToIAware(int i);

    void sendUpdateAppOpsState();

    void setAppOpHideHook(WindowState windowState, boolean z);

    void setCoverManagerState(boolean z);

    void setCurrentUser(int i, int[] iArr);

    void setForcedDisplayDensityAndSize(int i, int i2, int i3, int i4);

    void setHwSecureScreenShot(WindowState windowState);

    void setIgnoreFrozen(boolean z);

    void setLazyModeEx(int i);

    void setNaviBarFlag();

    void setNotchHeight(int i);

    void setVisibleFromParent(WindowState windowState);

    boolean shouldDropMotionEventForTouchPad(float f, float f2);

    void showWallpaperIfNeed(WindowState windowState);

    void takeTaskSnapshot(IBinder iBinder);

    void updateAppOpsStateReport(int i, String str);

    void updateDimPositionForPCMode(WindowContainer windowContainer, Rect rect);

    void updateHwStartWindowRecord(int i);

    Point updateLazyModePoint(int i, Point point);

    void updateResourceConfiguration(int i, int i2, int i3, int i4);

    void updateSurfacePositionForPCMode(WindowState windowState, Point point);

    void updateWindowReport(WindowState windowState, int i, int i2);
}

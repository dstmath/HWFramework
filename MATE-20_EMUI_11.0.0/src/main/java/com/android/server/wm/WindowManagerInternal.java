package com.android.server.wm;

import android.content.ClipData;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.IInputFilter;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.WindowInfo;
import com.android.server.input.InputManagerService;
import java.util.List;

public abstract class WindowManagerInternal {

    public interface MagnificationCallbacks {
        void onMagnificationRegionChanged(Region region);

        void onRectangleOnScreenRequested(int i, int i2, int i3, int i4);

        void onRotationChanged(int i);

        void onUserContextChanged();
    }

    public interface OnHardKeyboardStatusChangeListener {
        void onHardKeyboardStatusChange(boolean z);
    }

    public interface WindowsForAccessibilityCallback {
        void onWindowsForAccessibilityChanged(List<WindowInfo> list);
    }

    public abstract void addNonHighRefreshRatePackage(String str);

    public abstract void addWindowToken(IBinder iBinder, int i, int i2);

    public abstract void clearForcedDisplaySize(int i);

    public abstract void computeWindowsForAccessibility();

    public abstract WindowState findVisibleUnfloatingModeWindow(WindowState windowState);

    public abstract void freezeFoldRotation(int i);

    public abstract ActivityTaskManagerService getActivityTaskManagerService();

    public abstract MagnificationSpec getCompatibleMagnificationSpecForWindow(IBinder iBinder);

    public abstract int getDisplayIdForWindow(IBinder iBinder);

    public abstract int getFocusedAppWindowMode();

    public abstract int getFocusedDisplayId();

    public abstract IBinder getFocusedWindowToken();

    public abstract int getFoldDisplayMode();

    public abstract String getFullStackTopWindow();

    public abstract int getInputMethodWindowVisibleHeight(int i);

    public abstract boolean getKeyguardGoingAway();

    public abstract int getLazyMode();

    public abstract void getMagnificationRegion(int i, Region region);

    public abstract int getTopFocusedDisplayId();

    public abstract void getWindowFrame(IBinder iBinder, Rect rect);

    public abstract int getWindowOwnerUserId(IBinder iBinder);

    public abstract boolean isCoverOpen();

    public abstract boolean isHardKeyboardAvailable();

    public abstract boolean isInputMethodClientFocus(int i, int i2, int i3);

    public abstract boolean isKeyguardLocked();

    public abstract boolean isKeyguardShowingAndNotOccluded();

    public abstract boolean isMinimizedDock();

    public abstract boolean isNeedLandAni();

    public abstract boolean isPcFreeFormWinClient(IBinder iBinder);

    public abstract boolean isStackVisibleLw(int i);

    public abstract boolean isSuperWallpaper();

    public abstract boolean isUidAllowedOnDisplay(int i, int i2);

    public abstract boolean isUidFocused(int i);

    public abstract void lockNow();

    public abstract void registerAppTransitionListener(AppTransitionListener appTransitionListener);

    public abstract void registerDragDropControllerCallback(IDragDropCallback iDragDropCallback);

    public abstract void removeNonHighRefreshRatePackage(String str);

    public abstract void removeWindowToken(IBinder iBinder, boolean z, int i);

    public abstract void reportPasswordChanged(int i);

    public abstract void requestTraversalFromDisplayManager();

    public abstract void setDockedStackDividerRotation(int i);

    public abstract void setFocusedDisplay(int i, boolean z, String str);

    public abstract void setFoldSwitchState(boolean z);

    public abstract void setForceShowMagnifiableBounds(int i, boolean z);

    public abstract void setForcedDisplaySize(int i, int i2, int i3);

    public abstract void setForcedDisplaySizeAndDensity(boolean z, int i, int i2, int i3, int i4, Bundle bundle);

    public abstract void setImeHolder(IBinder iBinder);

    public abstract void setInputFilter(IInputFilter iInputFilter);

    public abstract boolean setMagnificationCallbacks(int i, MagnificationCallbacks magnificationCallbacks);

    public abstract void setMagnificationSpec(int i, MagnificationSpec magnificationSpec);

    public abstract void setNotchRoundCornerVisibility(boolean z);

    public abstract void setOnHardKeyboardStatusChangeListener(OnHardKeyboardStatusChangeListener onHardKeyboardStatusChangeListener);

    public abstract void setScreenSideBoxVisibility(int i, boolean z);

    public abstract void setSecureImeHolder(IBinder iBinder);

    public abstract void setVr2dDisplayId(int i);

    public abstract void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback windowsForAccessibilityCallback);

    public abstract boolean shouldShowIme(int i);

    public abstract boolean shouldShowSystemDecorOnDisplay(int i);

    public abstract void showGlobalActions();

    public abstract void showOrHideInsetSurface(MotionEvent motionEvent);

    public abstract void transactionApply(SurfaceControl.Transaction transaction);

    public abstract void travelsalPCWindowsAndFindOne(int i);

    public abstract void unFreezeFoldRotation();

    public abstract void updateInputMethodTargetWindow(IBinder iBinder, IBinder iBinder2);

    public abstract void updateInputMethodWindowStatus(IBinder iBinder, boolean z, boolean z2);

    public abstract void waitForAllWindowsDrawn(Runnable runnable, long j);

    public static abstract class AppTransitionListener {
        public void onAppTransitionPendingLocked() {
        }

        public void onAppTransitionCancelledLocked(int transit) {
        }

        public int onAppTransitionStartingLocked(int transit, long duration, long statusBarAnimationStartTime, long statusBarAnimationDuration) {
            return 0;
        }

        public void onAppTransitionFinishedLocked(IBinder token) {
        }
    }

    public interface IDragDropCallback {
        default boolean registerInputChannel(DragState state, Display display, InputManagerService service, InputChannel source) {
            state.mTransferTouchFromToken = source.getToken();
            state.register(display);
            return true;
        }

        default boolean prePerformDrag(IWindow window, IBinder dragToken, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
            return true;
        }

        default void postPerformDrag() {
        }

        default void preReportDropResult(IWindow window, boolean consumed) {
        }

        default void postReportDropResult() {
        }

        default void preCancelDragAndDrop(IBinder dragToken) {
        }

        default void postCancelDragAndDrop() {
        }
    }
}

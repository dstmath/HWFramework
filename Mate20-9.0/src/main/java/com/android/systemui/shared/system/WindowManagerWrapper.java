package com.android.systemui.shared.system;

import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManagerGlobal;
import com.android.systemui.shared.recents.view.AppTransitionAnimationSpecsFuture;
import com.android.systemui.shared.recents.view.RecentsTransition;

public class WindowManagerWrapper {
    public static final int ACTIVITY_TYPE_STANDARD = 1;
    public static final int NAV_BAR_POS_BOTTOM = 4;
    public static final int NAV_BAR_POS_INVALID = -1;
    public static final int NAV_BAR_POS_LEFT = 1;
    public static final int NAV_BAR_POS_RIGHT = 2;
    private static final String TAG = "WindowManagerWrapper";
    public static final int TRANSIT_ACTIVITY_CLOSE = 7;
    public static final int TRANSIT_ACTIVITY_OPEN = 6;
    public static final int TRANSIT_ACTIVITY_RELAUNCH = 18;
    public static final int TRANSIT_DOCK_TASK_FROM_RECENTS = 19;
    public static final int TRANSIT_KEYGUARD_GOING_AWAY = 20;
    public static final int TRANSIT_KEYGUARD_GOING_AWAY_ON_WALLPAPER = 21;
    public static final int TRANSIT_KEYGUARD_OCCLUDE = 22;
    public static final int TRANSIT_KEYGUARD_UNOCCLUDE = 23;
    public static final int TRANSIT_NONE = 0;
    public static final int TRANSIT_TASK_CLOSE = 9;
    public static final int TRANSIT_TASK_IN_PLACE = 17;
    public static final int TRANSIT_TASK_OPEN = 8;
    public static final int TRANSIT_TASK_OPEN_BEHIND = 16;
    public static final int TRANSIT_TASK_TO_BACK = 11;
    public static final int TRANSIT_TASK_TO_FRONT = 10;
    public static final int TRANSIT_UNSET = -1;
    public static final int TRANSIT_WALLPAPER_CLOSE = 12;
    public static final int TRANSIT_WALLPAPER_INTRA_CLOSE = 15;
    public static final int TRANSIT_WALLPAPER_INTRA_OPEN = 14;
    public static final int TRANSIT_WALLPAPER_OPEN = 13;
    public static final int WINDOWING_MODE_FREEFORM = 5;
    public static final int WINDOWING_MODE_FULLSCREEN = 1;
    public static final int WINDOWING_MODE_PINNED = 2;
    public static final int WINDOWING_MODE_SPLIT_SCREEN_PRIMARY = 3;
    public static final int WINDOWING_MODE_SPLIT_SCREEN_SECONDARY = 4;
    public static final int WINDOWING_MODE_UNDEFINED = 0;
    private static final WindowManagerWrapper sInstance = new WindowManagerWrapper();

    public static WindowManagerWrapper getInstance() {
        return sInstance;
    }

    public void getStableInsets(Rect outStableInsets) {
        try {
            WindowManagerGlobal.getWindowManagerService().getStableInsets(0, outStableInsets);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get stable insets", e);
        }
    }

    public void overridePendingAppTransitionMultiThumbFuture(AppTransitionAnimationSpecsFuture animationSpecFuture, Runnable animStartedCallback, Handler animStartedCallbackHandler, boolean scaleUp) {
        try {
            WindowManagerGlobal.getWindowManagerService().overridePendingAppTransitionMultiThumbFuture(animationSpecFuture.getFuture(), RecentsTransition.wrapStartedListener(animStartedCallbackHandler, animStartedCallback), scaleUp);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to override pending app transition (multi-thumbnail future): ", e);
        }
    }

    public void overridePendingAppTransitionRemote(RemoteAnimationAdapterCompat remoteAnimationAdapter) {
        try {
            WindowManagerGlobal.getWindowManagerService().overridePendingAppTransitionRemote(remoteAnimationAdapter.getWrapped());
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to override pending app transition (remote): ", e);
        }
    }

    public void setNavBarVirtualKeyHapticFeedbackEnabled(boolean enabled) {
        try {
            WindowManagerGlobal.getWindowManagerService().setNavBarVirtualKeyHapticFeedbackEnabled(enabled);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to enable or disable navigation bar button haptics: ", e);
        }
    }

    public void setShelfHeight(boolean visible, int shelfHeight) {
        try {
            WindowManagerGlobal.getWindowManagerService().setShelfHeight(visible, shelfHeight);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to set shelf height");
        }
    }

    public void setRecentsVisibility(boolean visible) {
        try {
            WindowManagerGlobal.getWindowManagerService().setRecentsVisibility(visible);
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to set recents visibility");
        }
    }

    public int getNavBarPosition() {
        try {
            return WindowManagerGlobal.getWindowManagerService().getNavBarPosition();
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get nav bar position");
            return -1;
        }
    }
}

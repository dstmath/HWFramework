package com.android.server.wm;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.animation.Animation;

class WallpaperWindowToken extends WindowToken {
    private static final String TAG = "WindowManager";

    WallpaperWindowToken(WindowManagerService service, IBinder token, boolean explicit, DisplayContent dc, boolean ownerCanManageAppTokens) {
        super(service, token, 2013, explicit, dc, ownerCanManageAppTokens);
        dc.mWallpaperController.addWallpaperToken(this);
    }

    void setExiting() {
        super.setExiting();
        this.mDisplayContent.mWallpaperController.removeWallpaperToken(this);
    }

    void hideWallpaperToken(boolean wasDeferred, String reason) {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            ((WindowState) this.mChildren.get(j)).hideWallpaperWindow(wasDeferred, reason);
        }
        this.hidden = true;
    }

    void sendWindowWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
        for (int wallpaperNdx = this.mChildren.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
            try {
                ((WindowState) this.mChildren.get(wallpaperNdx)).mClient.dispatchWallpaperCommand(action, x, y, z, extras, sync);
                sync = false;
            } catch (RemoteException e) {
            }
        }
    }

    void updateWallpaperOffset(int dw, int dh, boolean sync) {
        WallpaperController wallpaperController = this.mDisplayContent.mWallpaperController;
        for (int wallpaperNdx = this.mChildren.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
            WindowState wallpaper = (WindowState) this.mChildren.get(wallpaperNdx);
            if (wallpaperController.updateWallpaperOffset(wallpaper, dw, dh, sync)) {
                WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                winAnimator.computeShownFrameLocked();
                winAnimator.setWallpaperOffset(wallpaper.mShownPosition);
                sync = false;
            }
        }
    }

    void updateWallpaperVisibility(boolean visible) {
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        if (this.hidden == visible) {
            this.hidden = visible ^ 1;
            this.mDisplayContent.setLayoutNeeded();
        }
        WallpaperController wallpaperController = this.mDisplayContent.mWallpaperController;
        for (int wallpaperNdx = this.mChildren.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
            WindowState wallpaper = (WindowState) this.mChildren.get(wallpaperNdx);
            if (visible) {
                wallpaperController.updateWallpaperOffset(wallpaper, dw, dh, false);
            }
            wallpaper.dispatchWallpaperVisibility(visible);
        }
    }

    void startAnimation(Animation anim) {
        for (int ndx = this.mChildren.size() - 1; ndx >= 0; ndx--) {
            ((WindowState) this.mChildren.get(ndx)).mWinAnimator.setAnimation(anim);
        }
    }

    void updateWallpaperWindows(boolean visible, int animLayerAdj) {
        if (this.hidden == visible) {
            this.hidden = visible ^ 1;
            this.mDisplayContent.setLayoutNeeded();
        }
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        WallpaperController wallpaperController = this.mDisplayContent.mWallpaperController;
        for (int wallpaperNdx = this.mChildren.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
            WindowState wallpaper = (WindowState) this.mChildren.get(wallpaperNdx);
            if (visible) {
                wallpaperController.updateWallpaperOffset(wallpaper, dw, dh, false);
            }
            wallpaper.dispatchWallpaperVisibility(visible);
        }
    }

    boolean hasVisibleNotDrawnWallpaper() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowState) this.mChildren.get(j)).hasVisibleNotDrawnWallpaper()) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        if (this.stringName == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("WallpaperWindowToken{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" token=");
            sb.append(this.token);
            sb.append('}');
            this.stringName = sb.toString();
        }
        return this.stringName;
    }
}

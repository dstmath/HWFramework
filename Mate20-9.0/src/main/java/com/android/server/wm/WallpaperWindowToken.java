package com.android.server.wm;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.animation.Animation;

class WallpaperWindowToken extends WindowToken {
    private static final String TAG = "WindowManager";

    WallpaperWindowToken(WindowManagerService service, IBinder token, boolean explicit, DisplayContent dc, boolean ownerCanManageAppTokens) {
        super(service, token, 2013, explicit, dc, ownerCanManageAppTokens);
        dc.mWallpaperController.addWallpaperToken(this);
    }

    /* access modifiers changed from: package-private */
    public void setExiting() {
        super.setExiting();
        this.mDisplayContent.mWallpaperController.removeWallpaperToken(this);
    }

    /* access modifiers changed from: package-private */
    public void hideWallpaperToken(boolean wasDeferred, String reason) {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            ((WindowState) this.mChildren.get(j)).hideWallpaperWindow(wasDeferred, reason);
        }
        setHidden(true);
    }

    /* access modifiers changed from: package-private */
    public void sendWindowWallpaperCommand(String action, int x, int y, int z, Bundle extras, boolean sync) {
        for (int wallpaperNdx = this.mChildren.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
            try {
                ((WindowState) this.mChildren.get(wallpaperNdx)).mClient.dispatchWallpaperCommand(action, x, y, z, extras, sync);
                sync = false;
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperOffset(int dw, int dh, boolean sync) {
        WallpaperController wallpaperController = this.mDisplayContent.mWallpaperController;
        for (int wallpaperNdx = this.mChildren.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
            if (wallpaperController.updateWallpaperOffset((WindowState) this.mChildren.get(wallpaperNdx), dw, dh, sync)) {
                sync = false;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperVisibility(boolean visible) {
        DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        if (isHidden() == visible) {
            setHidden(!visible);
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

    /* access modifiers changed from: package-private */
    public void startAnimation(Animation anim) {
        for (int ndx = this.mChildren.size() - 1; ndx >= 0; ndx--) {
            ((WindowState) this.mChildren.get(ndx)).startAnimation(anim);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperWindows(boolean visible) {
        if (isHidden() == visible) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.d(TAG, "Wallpaper token " + this.token + " hidden=" + (!visible));
            }
            setHidden(!visible);
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
            if (WindowManagerDebugConfig.DEBUG_LAYERS || WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                Slog.v(TAG, "adjustWallpaper win " + wallpaper + " anim layer: " + wallpaper.mWinAnimator.mAnimLayer);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasVisibleNotDrawnWallpaper() {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowState) this.mChildren.get(j)).hasVisibleNotDrawnWallpaper()) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        if (this.stringName == null) {
            this.stringName = "WallpaperWindowToken{" + Integer.toHexString(System.identityHashCode(this)) + " token=" + this.token + '}';
        }
        return this.stringName;
    }
}

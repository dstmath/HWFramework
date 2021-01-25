package com.android.server.wm;

import android.os.IBinder;
import android.os.RemoteException;
import android.view.IWindowLayoutObserver;
import android.view.IWindowManager;

public abstract class AbsWindowManagerService extends IWindowManager.Stub {
    public static final int TOP_LAYER = 400000;
    protected boolean mPCLauncherFocused = false;

    /* access modifiers changed from: package-private */
    public void setPCLauncherFocused(boolean isFocus) {
    }

    /* access modifiers changed from: package-private */
    public boolean getPCLauncherFocused() {
        return this.mPCLauncherFocused;
    }

    public int getPCScreenDisplayMode() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void updateInputImmersiveMode() {
    }

    /* access modifiers changed from: protected */
    public void checkKeyguardDismissDoneLocked() {
    }

    public boolean isSplitMode() {
        return false;
    }

    public void setSplittable(boolean isSplittable) {
    }

    public int getLayerIndex(String appName, int windowType) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean shouldHideIMExitAnim(WindowState win) {
        return false;
    }

    public void registerWindowObserver(IWindowLayoutObserver observer, long period) throws RemoteException {
    }

    public void unRegisterWindowObserver(IWindowLayoutObserver observer) throws RemoteException {
    }

    public void showWallpaperIfNeed(WindowState ws) {
    }

    /* access modifiers changed from: protected */
    public boolean checkAppOrientationForForceRotation(AppWindowToken aToken) {
        return false;
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, int pid, String processName) {
    }

    public void prepareForForceRotation(IBinder appToken, String packageName, String componentName) {
    }
}

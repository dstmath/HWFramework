package com.android.server.wm;

import android.os.IBinder;

public class RootWindowContainerEx {
    private RootWindowContainer mRootWindowContainer;

    public RootWindowContainerEx() {
    }

    public RootWindowContainerEx(RootWindowContainer rootWindowContainer) {
        this.mRootWindowContainer = rootWindowContainer;
    }

    public RootWindowContainer getRootWindowContainer() {
        return this.mRootWindowContainer;
    }

    public void setRootWindowContainer(RootWindowContainer rootWindowContainer) {
        this.mRootWindowContainer = rootWindowContainer;
    }

    public DisplayContentEx getDisplayContentEx(int displayId) {
        DisplayContent displayContent;
        RootWindowContainer rootWindowContainer = this.mRootWindowContainer;
        if (rootWindowContainer == null || (displayContent = rootWindowContainer.getDisplayContent(displayId)) == null) {
            return null;
        }
        DisplayContentEx displayContentEx = new DisplayContentEx();
        displayContentEx.setDisplayContent(displayContent);
        return displayContentEx;
    }

    public WindowStateEx getCurrentInputMethodWindow() {
        if (this.mRootWindowContainer == null) {
            return null;
        }
        WindowStateEx windowStateEx = new WindowStateEx();
        windowStateEx.setWindowState(this.mRootWindowContainer.getCurrentInputMethodWindow());
        return windowStateEx;
    }

    public AppWindowTokenExt getAppWindowTokenEx(IBinder binder) {
        AppWindowToken appWindowToken;
        RootWindowContainer rootWindowContainer = this.mRootWindowContainer;
        if (rootWindowContainer == null || (appWindowToken = rootWindowContainer.getAppWindowToken(binder)) == null) {
            return null;
        }
        AppWindowTokenExt appWindowTokenEx = new AppWindowTokenExt();
        appWindowTokenEx.setAppWindowToken(appWindowToken);
        return appWindowTokenEx;
    }

    public boolean isWallpaperActionPending() {
        RootWindowContainer rootWindowContainer = this.mRootWindowContainer;
        if (rootWindowContainer != null) {
            return rootWindowContainer.mWallpaperActionPending;
        }
        return false;
    }
}

package com.huawei.android.app;

import android.app.IWallpaperManagerCallback;
import android.app.WallpaperColors;
import android.os.RemoteException;

public class IWallpaperManagerCallbackEx {
    private IWallpaperManagerCallbackBridge mBridge = new IWallpaperManagerCallbackBridge();

    public IWallpaperManagerCallbackEx() {
        this.mBridge.setWallpaperManagerCallbackEx(this);
    }

    public void onWallpaperChanged() throws RemoteException {
    }

    public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) throws RemoteException {
    }

    public void onBlurWallpaperChanged() throws RemoteException {
    }

    public IWallpaperManagerCallback getIWallpaperManagerCallback() {
        return this.mBridge;
    }
}

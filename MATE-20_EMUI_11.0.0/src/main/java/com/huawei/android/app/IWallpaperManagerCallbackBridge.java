package com.huawei.android.app;

import android.app.IWallpaperManagerCallback;
import android.app.WallpaperColors;
import android.os.RemoteException;

public class IWallpaperManagerCallbackBridge extends IWallpaperManagerCallback.Stub {
    private IWallpaperManagerCallbackEx mWallpaperManagerCallbackEx = null;

    public void setWallpaperManagerCallbackEx(IWallpaperManagerCallbackEx mWallpaperManagerCallbackEx2) {
        this.mWallpaperManagerCallbackEx = mWallpaperManagerCallbackEx2;
    }

    public void onWallpaperChanged() throws RemoteException {
    }

    public void onWallpaperColorsChanged(WallpaperColors colors, int which, int userId) throws RemoteException {
    }

    public void onBlurWallpaperChanged() throws RemoteException {
        IWallpaperManagerCallbackEx iWallpaperManagerCallbackEx = this.mWallpaperManagerCallbackEx;
        if (iWallpaperManagerCallbackEx != null) {
            iWallpaperManagerCallbackEx.onBlurWallpaperChanged();
        }
    }
}

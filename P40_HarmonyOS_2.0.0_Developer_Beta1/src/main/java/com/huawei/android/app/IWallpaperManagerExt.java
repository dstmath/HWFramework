package com.huawei.android.app;

import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.os.RemoteException;
import android.os.ServiceManager;

public class IWallpaperManagerExt {
    public static WallpaperInfo getWallpaperInfo(int uid) throws RemoteException {
        IWallpaperManager wallpaper = IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper"));
        if (wallpaper != null) {
            return wallpaper.getWallpaperInfo(uid);
        }
        return null;
    }
}

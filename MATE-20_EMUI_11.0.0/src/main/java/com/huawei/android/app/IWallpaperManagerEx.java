package com.huawei.android.app;

import android.app.IWallpaperManager;
import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class IWallpaperManagerEx {
    public IWallpaperManager mService;

    public IWallpaperManagerEx(IWallpaperManager service) {
        this.mService = service;
    }

    @HwSystemApi
    public void setCurrOffsets(int[] offsets) throws RemoteException {
        this.mService.setCurrOffsets(offsets);
    }

    @HwSystemApi
    public void setNextOffsets(int[] offsets) throws RemoteException {
        this.mService.setNextOffsets(offsets);
    }

    @HwSystemApi
    public int[] getCurrOffsets() throws RemoteException {
        return this.mService.getCurrOffsets();
    }

    @HwSystemApi
    public int getWallpaperUserId() throws RemoteException {
        return this.mService.getWallpaperUserId();
    }
}

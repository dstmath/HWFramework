package com.android.server.wallpaper;

import android.app.WallpaperInfo;
import android.util.SparseArray;
import com.android.server.wallpaper.WallpaperManagerService;

public interface IHwWallpaperManagerInner {
    int getCurrentUserId();

    Object getLock();

    WallpaperInfo getWallpaperInfo(int i);

    SparseArray<WallpaperManagerService.WallpaperData> getWallpaperMap();

    void restartLiveWallpaperService();
}

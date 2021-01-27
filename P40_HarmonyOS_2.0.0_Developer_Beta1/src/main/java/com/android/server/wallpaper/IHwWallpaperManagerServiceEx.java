package com.android.server.wallpaper;

import android.app.IWallpaperManagerCallback;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import com.android.server.wallpaper.WallpaperManagerService;

public interface IHwWallpaperManagerServiceEx {
    ParcelFileDescriptor getBlurWallpaper(IWallpaperManagerCallback iWallpaperManagerCallback);

    int[] getCurrOffsets();

    int getWallpaperUserId();

    void handleWallpaperObserverEvent(WallpaperManagerService.WallpaperData wallpaperData);

    void hwSystemReady();

    void reportWallpaper(ComponentName componentName);

    ParcelFileDescriptor scaleBitmaptoFileDescriptor(Bitmap bitmap, float f, Bitmap.Config config);

    Bitmap scaleWallpaperBitmapToScreenSize(Bitmap bitmap);

    void setCurrOffsets(int[] iArr);

    void setNextOffsets(int[] iArr);

    void updateWallpaperOffsets(WallpaperManagerService.WallpaperData wallpaperData);

    void wallpaperManagerServiceReady();
}

package com.android.server.wallpaper;

import com.android.server.wallpaper.WallpaperManagerService;
import java.util.function.Consumer;

/* renamed from: com.android.server.wallpaper.-$$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGk-z4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGkz4 implements Consumer {
    public static final /* synthetic */ $$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGkz4 INSTANCE = new $$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGkz4();

    private /* synthetic */ $$Lambda$WallpaperManagerService$WallpaperConnection$87DhM3RJJxRNtgkHmd_gtnGkz4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WallpaperManagerService.WallpaperConnection.DisplayConnector) obj).mEngine = null;
    }
}

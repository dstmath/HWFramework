package com.android.server.wallpaper;

import com.android.server.wallpaper.WallpaperManagerService;
import java.util.function.Consumer;

/* renamed from: com.android.server.wallpaper.-$$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE implements Consumer {
    public static final /* synthetic */ $$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE INSTANCE = new $$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE();

    private /* synthetic */ $$Lambda$havGP5uMdRgWQrLydPeIOu1qDGE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WallpaperManagerService.WallpaperConnection.DisplayConnector) obj).disconnectLocked();
    }
}

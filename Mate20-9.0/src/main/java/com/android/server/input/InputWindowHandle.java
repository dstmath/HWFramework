package com.android.server.input;

import android.graphics.Region;
import android.view.IWindow;
import android.view.InputChannel;
import java.lang.annotation.RCUnownedRef;

public final class InputWindowHandle {
    public boolean canReceiveKeys;
    public final IWindow clientWindow;
    public long dispatchingTimeoutNanos;
    public int displayId;
    public int frameBottom;
    public int frameLeft;
    public int frameRight;
    public int frameTop;
    public boolean hasFocus;
    public boolean hasWallpaper;
    public final InputApplicationHandle inputApplicationHandle;
    public InputChannel inputChannel;
    public int inputFeatures;
    public boolean isFreeform;
    public int layer;
    public int layoutParamsFlags;
    public int layoutParamsPrivateFlags;
    public int layoutParamsType;
    public String name;
    public int ownerPid;
    public int ownerUid;
    public boolean paused;
    private long ptr;
    public float scaleFactor;
    public final Region touchableRegion = new Region();
    public boolean visible;
    @RCUnownedRef
    public final Object windowState;

    private native void nativeDispose();

    public InputWindowHandle(InputApplicationHandle inputApplicationHandle2, Object windowState2, IWindow clientWindow2, int displayId2) {
        this.inputApplicationHandle = inputApplicationHandle2;
        this.windowState = windowState2;
        this.clientWindow = clientWindow2;
        this.displayId = displayId2;
    }

    public String toString() {
        return this.name + ", layer=" + this.layer + ", frame=[" + this.frameLeft + "," + this.frameTop + "," + this.frameRight + "," + this.frameBottom + "]" + ", touchableRegion=" + this.touchableRegion + ", visible=" + this.visible;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDispose();
        } finally {
            super.finalize();
        }
    }
}

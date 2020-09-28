package android.view;

import android.graphics.Region;
import android.os.IBinder;
import android.telephony.SmsManager;
import java.lang.ref.WeakReference;

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
    public int inputFeatures;
    public int layer;
    public int layoutParamsFlags;
    public int layoutParamsPrivateFlags;
    public int layoutParamsType;
    public String name;
    public int ownerPid;
    public int ownerUid;
    public boolean paused;
    public int portalToDisplayId = -1;
    private long ptr;
    public boolean replaceTouchableRegionWithCrop;
    public float scaleFactor;
    public int surfaceInset;
    public IBinder token;
    public final Region touchableRegion = new Region();
    public WeakReference<IBinder> touchableRegionCropHandle = new WeakReference<>(null);
    public boolean visible;

    private native void nativeDispose();

    public InputWindowHandle(InputApplicationHandle inputApplicationHandle2, IWindow clientWindow2, int displayId2) {
        this.inputApplicationHandle = inputApplicationHandle2;
        this.clientWindow = clientWindow2;
        this.displayId = displayId2;
    }

    public String toString() {
        String str = this.name;
        if (str == null) {
            str = "";
        }
        return str + ", layer=" + this.layer + ", frame=[" + this.frameLeft + SmsManager.REGEX_PREFIX_DELIMITER + this.frameTop + SmsManager.REGEX_PREFIX_DELIMITER + this.frameRight + SmsManager.REGEX_PREFIX_DELIMITER + this.frameBottom + "]" + ", touchableRegion=" + this.touchableRegion + ", visible=" + this.visible;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            nativeDispose();
        } finally {
            super.finalize();
        }
    }

    public void replaceTouchableRegionWithCrop(SurfaceControl bounds) {
        setTouchableRegionCrop(bounds);
        this.replaceTouchableRegionWithCrop = true;
    }

    public void setTouchableRegionCrop(SurfaceControl bounds) {
        if (bounds != null) {
            this.touchableRegionCropHandle = new WeakReference<>(bounds.getHandle());
        }
    }
}

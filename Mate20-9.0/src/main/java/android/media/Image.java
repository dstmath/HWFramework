package android.media;

import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import java.nio.ByteBuffer;

public abstract class Image implements AutoCloseable {
    private Rect mCropRect;
    protected boolean mIsImageValid = false;

    public static abstract class Plane {
        public abstract ByteBuffer getBuffer();

        public abstract int getPixelStride();

        public abstract int getRowStride();

        protected Plane() {
        }
    }

    public abstract void close();

    public abstract int getFormat();

    public abstract int getHeight();

    public abstract Plane[] getPlanes();

    public abstract int getScalingMode();

    public abstract long getTimestamp();

    public abstract int getTransform();

    public abstract int getWidth();

    protected Image() {
    }

    /* access modifiers changed from: protected */
    public void throwISEIfImageIsInvalid() {
        if (!this.mIsImageValid) {
            throw new IllegalStateException("Image is already closed");
        }
    }

    public HardwareBuffer getHardwareBuffer() {
        throwISEIfImageIsInvalid();
        return null;
    }

    public void setTimestamp(long timestamp) {
        throwISEIfImageIsInvalid();
    }

    public Rect getCropRect() {
        throwISEIfImageIsInvalid();
        if (this.mCropRect == null) {
            return new Rect(0, 0, getWidth(), getHeight());
        }
        return new Rect(this.mCropRect);
    }

    public void setCropRect(Rect cropRect) {
        throwISEIfImageIsInvalid();
        if (cropRect != null) {
            cropRect = new Rect(cropRect);
            if (!cropRect.intersect(0, 0, getWidth(), getHeight())) {
                cropRect.setEmpty();
            }
        }
        this.mCropRect = cropRect;
    }

    /* access modifiers changed from: package-private */
    public boolean isAttachable() {
        throwISEIfImageIsInvalid();
        return false;
    }

    /* access modifiers changed from: package-private */
    public Object getOwner() {
        throwISEIfImageIsInvalid();
        return null;
    }

    /* access modifiers changed from: package-private */
    public long getNativeContext() {
        throwISEIfImageIsInvalid();
        return 0;
    }
}

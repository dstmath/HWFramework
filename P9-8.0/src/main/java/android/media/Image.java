package android.media;

import android.graphics.Rect;
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

    public abstract long getTimestamp();

    public abstract int getWidth();

    protected Image() {
    }

    protected void throwISEIfImageIsInvalid() {
        if (!this.mIsImageValid) {
            throw new IllegalStateException("Image is already closed");
        }
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
            Rect cropRect2 = new Rect(cropRect);
            if (cropRect2.intersect(0, 0, getWidth(), getHeight())) {
                cropRect = cropRect2;
            } else {
                cropRect2.setEmpty();
                cropRect = cropRect2;
            }
        }
        this.mCropRect = cropRect;
    }

    boolean isAttachable() {
        throwISEIfImageIsInvalid();
        return false;
    }

    Object getOwner() {
        throwISEIfImageIsInvalid();
        return null;
    }

    long getNativeContext() {
        throwISEIfImageIsInvalid();
        return 0;
    }
}

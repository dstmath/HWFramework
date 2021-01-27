package android.filterfw.core;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
import java.nio.ByteBuffer;

public abstract class Frame {
    public static final int NO_BINDING = 0;
    public static final long TIMESTAMP_NOT_SET = -2;
    public static final long TIMESTAMP_UNKNOWN = -1;
    private long mBindingId = 0;
    private int mBindingType = 0;
    private FrameFormat mFormat;
    private FrameManager mFrameManager;
    private boolean mReadOnly = false;
    private int mRefCount = 1;
    private boolean mReusable = false;
    private long mTimestamp = -2;

    @UnsupportedAppUsage
    public abstract Bitmap getBitmap();

    public abstract ByteBuffer getData();

    public abstract float[] getFloats();

    public abstract int[] getInts();

    public abstract Object getObjectValue();

    /* access modifiers changed from: protected */
    public abstract boolean hasNativeAllocation();

    /* access modifiers changed from: protected */
    public abstract void releaseNativeAllocation();

    public abstract void setBitmap(Bitmap bitmap);

    public abstract void setData(ByteBuffer byteBuffer, int i, int i2);

    public abstract void setFloats(float[] fArr);

    @UnsupportedAppUsage
    public abstract void setInts(int[] iArr);

    Frame(FrameFormat format, FrameManager frameManager) {
        this.mFormat = format.mutableCopy();
        this.mFrameManager = frameManager;
    }

    Frame(FrameFormat format, FrameManager frameManager, int bindingType, long bindingId) {
        this.mFormat = format.mutableCopy();
        this.mFrameManager = frameManager;
        this.mBindingType = bindingType;
        this.mBindingId = bindingId;
    }

    @UnsupportedAppUsage
    public FrameFormat getFormat() {
        return this.mFormat;
    }

    public int getCapacity() {
        return getFormat().getSize();
    }

    public boolean isReadOnly() {
        return this.mReadOnly;
    }

    public int getBindingType() {
        return this.mBindingType;
    }

    public long getBindingId() {
        return this.mBindingId;
    }

    public void setObjectValue(Object object) {
        assertFrameMutable();
        if (object instanceof int[]) {
            setInts((int[]) object);
        } else if (object instanceof float[]) {
            setFloats((float[]) object);
        } else if (object instanceof ByteBuffer) {
            setData((ByteBuffer) object);
        } else if (object instanceof Bitmap) {
            setBitmap((Bitmap) object);
        } else {
            setGenericObjectValue(object);
        }
    }

    public void setData(ByteBuffer buffer) {
        setData(buffer, 0, buffer.limit());
    }

    public void setData(byte[] bytes, int offset, int length) {
        setData(ByteBuffer.wrap(bytes, offset, length));
    }

    @UnsupportedAppUsage
    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    @UnsupportedAppUsage
    public long getTimestamp() {
        return this.mTimestamp;
    }

    public void setDataFromFrame(Frame frame) {
        setData(frame.getData());
    }

    /* access modifiers changed from: protected */
    public boolean requestResize(int[] newDimensions) {
        return false;
    }

    public int getRefCount() {
        return this.mRefCount;
    }

    @UnsupportedAppUsage
    public Frame release() {
        FrameManager frameManager = this.mFrameManager;
        if (frameManager != null) {
            return frameManager.releaseFrame(this);
        }
        return this;
    }

    public Frame retain() {
        FrameManager frameManager = this.mFrameManager;
        if (frameManager != null) {
            return frameManager.retainFrame(this);
        }
        return this;
    }

    public FrameManager getFrameManager() {
        return this.mFrameManager;
    }

    /* access modifiers changed from: protected */
    public void assertFrameMutable() {
        if (isReadOnly()) {
            throw new RuntimeException("Attempting to modify read-only frame!");
        }
    }

    /* access modifiers changed from: protected */
    public void setReusable(boolean reusable) {
        this.mReusable = reusable;
    }

    /* access modifiers changed from: protected */
    public void setFormat(FrameFormat format) {
        this.mFormat = format.mutableCopy();
    }

    /* access modifiers changed from: protected */
    public void setGenericObjectValue(Object value) {
        throw new RuntimeException("Cannot set object value of unsupported type: " + value.getClass());
    }

    protected static Bitmap convertBitmapToRGBA(Bitmap bitmap) {
        if (bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
            return bitmap;
        }
        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        if (result == null) {
            throw new RuntimeException("Error converting bitmap to RGBA!");
        } else if (result.getRowBytes() == result.getWidth() * 4) {
            return result;
        } else {
            throw new RuntimeException("Unsupported row byte count in bitmap!");
        }
    }

    /* access modifiers changed from: protected */
    public void reset(FrameFormat newFormat) {
        this.mFormat = newFormat.mutableCopy();
        this.mReadOnly = false;
        this.mRefCount = 1;
    }

    /* access modifiers changed from: protected */
    public void onFrameStore() {
    }

    /* access modifiers changed from: protected */
    public void onFrameFetch() {
    }

    /* access modifiers changed from: package-private */
    public final int incRefCount() {
        this.mRefCount++;
        return this.mRefCount;
    }

    /* access modifiers changed from: package-private */
    public final int decRefCount() {
        this.mRefCount--;
        return this.mRefCount;
    }

    /* access modifiers changed from: package-private */
    public final boolean isReusable() {
        return this.mReusable;
    }

    /* access modifiers changed from: package-private */
    public final void markReadOnly() {
        this.mReadOnly = true;
    }
}

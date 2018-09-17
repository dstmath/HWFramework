package android.filterfw.core;

public class NativeBuffer {
    private Frame mAttachedFrame;
    private long mDataPointer = 0;
    private boolean mOwnsData = false;
    private int mRefCount = 1;
    private int mSize = 0;

    private native boolean allocate(int i);

    private native boolean deallocate(boolean z);

    private native boolean nativeCopyTo(NativeBuffer nativeBuffer);

    public NativeBuffer(int count) {
        allocate(getElementSize() * count);
        this.mOwnsData = true;
    }

    public NativeBuffer mutableCopy() {
        try {
            NativeBuffer result = (NativeBuffer) getClass().newInstance();
            if (this.mSize <= 0 || (nativeCopyTo(result) ^ 1) == 0) {
                return result;
            }
            throw new RuntimeException("Failed to copy NativeBuffer to mutable instance!");
        } catch (Exception e) {
            throw new RuntimeException("Unable to allocate a copy of " + getClass() + "! Make " + "sure the class has a default constructor!");
        }
    }

    public int size() {
        return this.mSize;
    }

    public int count() {
        return this.mDataPointer != 0 ? this.mSize / getElementSize() : 0;
    }

    public int getElementSize() {
        return 1;
    }

    public NativeBuffer retain() {
        if (this.mAttachedFrame != null) {
            this.mAttachedFrame.retain();
        } else if (this.mOwnsData) {
            this.mRefCount++;
        }
        return this;
    }

    public NativeBuffer release() {
        boolean doDealloc = false;
        if (this.mAttachedFrame != null) {
            doDealloc = this.mAttachedFrame.release() == null;
        } else if (this.mOwnsData) {
            this.mRefCount--;
            doDealloc = this.mRefCount == 0;
        }
        if (!doDealloc) {
            return this;
        }
        deallocate(this.mOwnsData);
        return null;
    }

    public boolean isReadOnly() {
        return this.mAttachedFrame != null ? this.mAttachedFrame.isReadOnly() : false;
    }

    static {
        System.loadLibrary("filterfw");
    }

    void attachToFrame(Frame frame) {
        this.mAttachedFrame = frame;
    }

    protected void assertReadable() {
        if (this.mDataPointer == 0 || this.mSize == 0 || !(this.mAttachedFrame == null || (this.mAttachedFrame.hasNativeAllocation() ^ 1) == 0)) {
            throw new NullPointerException("Attempting to read from null data frame!");
        }
    }

    protected void assertWritable() {
        if (isReadOnly()) {
            throw new RuntimeException("Attempting to modify read-only native (structured) data!");
        }
    }
}

package android.opengl;

public abstract class EGLObjectHandle {
    private final long mHandle;

    @Deprecated
    protected EGLObjectHandle(int handle) {
        this.mHandle = (long) handle;
    }

    protected EGLObjectHandle(long handle) {
        this.mHandle = handle;
    }

    @Deprecated
    public int getHandle() {
        if ((this.mHandle & 4294967295L) == this.mHandle) {
            return (int) this.mHandle;
        }
        throw new UnsupportedOperationException();
    }

    public long getNativeHandle() {
        return this.mHandle;
    }

    public int hashCode() {
        return (31 * 17) + ((int) (this.mHandle ^ (this.mHandle >>> 32)));
    }
}

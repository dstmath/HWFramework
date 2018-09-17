package android.opengl;

import android.security.keymaster.KeymasterArguments;

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
        if ((this.mHandle & KeymasterArguments.UINT32_MAX_VALUE) == this.mHandle) {
            return (int) this.mHandle;
        }
        throw new UnsupportedOperationException();
    }

    public long getNativeHandle() {
        return this.mHandle;
    }

    public int hashCode() {
        return ((int) (this.mHandle ^ (this.mHandle >>> 32))) + 527;
    }
}

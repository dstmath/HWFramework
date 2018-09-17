package android.opengl;

import android.net.wifi.hotspot2.pps.UpdateParameter;

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
        if ((this.mHandle & UpdateParameter.UPDATE_CHECK_INTERVAL_NEVER) == this.mHandle) {
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

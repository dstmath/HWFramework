package com.google.android.gles_jni;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import javax.microedition.khronos.egl.EGLSurface;

public class EGLSurfaceImpl extends EGLSurface {
    long mEGLSurface;
    private long mNativePixelRef;

    public EGLSurfaceImpl() {
        this.mEGLSurface = 0;
        this.mNativePixelRef = 0;
    }

    public EGLSurfaceImpl(long surface) {
        this.mEGLSurface = surface;
        this.mNativePixelRef = 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.mEGLSurface != ((EGLSurfaceImpl) o).mEGLSurface) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((int) (this.mEGLSurface ^ (this.mEGLSurface >>> 32))) + MetricsEvent.DIALOG_SUPPORT_PHONE;
    }
}

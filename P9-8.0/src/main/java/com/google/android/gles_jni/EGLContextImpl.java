package com.google.android.gles_jni;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL;

public class EGLContextImpl extends EGLContext {
    long mEGLContext;
    private GLImpl mGLContext = new GLImpl();

    public EGLContextImpl(long ctx) {
        this.mEGLContext = ctx;
    }

    public GL getGL() {
        return this.mGLContext;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.mEGLContext != ((EGLContextImpl) o).mEGLContext) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return ((int) (this.mEGLContext ^ (this.mEGLContext >>> 32))) + MetricsEvent.DIALOG_SUPPORT_PHONE;
    }
}

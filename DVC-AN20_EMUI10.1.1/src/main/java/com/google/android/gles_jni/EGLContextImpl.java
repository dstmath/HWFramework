package com.google.android.gles_jni;

import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL;

public class EGLContextImpl extends EGLContext {
    long mEGLContext;
    private GLImpl mGLContext = new GLImpl();

    public EGLContextImpl(long ctx) {
        this.mEGLContext = ctx;
    }

    @Override // javax.microedition.khronos.egl.EGLContext
    public GL getGL() {
        return this.mGLContext;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this.mEGLContext == ((EGLContextImpl) o).mEGLContext) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        long j = this.mEGLContext;
        return (17 * 31) + ((int) (j ^ (j >>> 32)));
    }
}

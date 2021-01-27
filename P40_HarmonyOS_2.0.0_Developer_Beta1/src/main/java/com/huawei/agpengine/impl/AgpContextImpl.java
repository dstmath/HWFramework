package com.huawei.agpengine.impl;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES32;
import com.huawei.agpengine.impl.CoreDeviceCreateInfo;
import java.util.Map;

final class AgpContextImpl {
    private static final String TAG = "core: AgpContextImpl";
    private CoreDevice mDevice;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglTmpContext;
    private EGLDisplay mEglTmpDisplay;
    private EGLSurface mEglTmpDrawSurface;
    private EGLSurface mEglTmpReadSurface;
    private CoreEnginePtr mEngine;
    private CoreGraphicsContextPtr mGraphicsContext = CoreGraphicsContextPtr.create(this.mEngine.get().getPluginRegister());

    private AgpContextImpl(Context androidContext, String name, int versionMajor, int versionMinor, int versionPatch) {
        createGlContext();
        this.mEngine = Core.createEngine(androidContext, new CoreVersionInfo(name, versionMajor, versionMinor, versionPatch));
    }

    static AgpContextImpl createAgpContext(Context androidContext, String name, int versionMajor, int versionMinor, int versionPatch) {
        return new AgpContextImpl(androidContext, name, versionMajor, versionMinor, versionPatch);
    }

    private void createGlContext() {
        this.mEglDisplay = EGL14.eglGetDisplay(0);
        EGL14.eglInitialize(this.mEglDisplay, null, 0, null, 0);
        EGLConfig[] configs = new EGLConfig[1];
        EGL14.eglChooseConfig(this.mEglDisplay, new int[]{12352, 64, 12344}, 0, configs, 0, 1, new int[]{0}, 0);
        this.mEglContext = EGL14.eglCreateContext(this.mEglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 3, 12344}, 0);
        this.mEglSurface = EGL14.eglCreatePbufferSurface(this.mEglDisplay, configs[0], new int[]{12375, 1, 12374, 1, 12344}, 0);
    }

    private void releaseGlContext() {
        EGLDisplay eGLDisplay = this.mEglDisplay;
        if (eGLDisplay != null) {
            EGLSurface eGLSurface = this.mEglSurface;
            if (eGLSurface != null) {
                EGL14.eglDestroySurface(eGLDisplay, eGLSurface);
                this.mEglSurface = null;
            }
            EGLContext eGLContext = this.mEglContext;
            if (eGLContext != null) {
                EGL14.eglDestroyContext(this.mEglDisplay, eGLContext);
                this.mEglContext = null;
            }
            this.mEglDisplay = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void init() {
        CoreEnginePtr coreEnginePtr = this.mEngine;
        if (coreEnginePtr == null || coreEnginePtr.get() == null) {
            throw new IllegalStateException("Engine not available.");
        }
        this.mEngine.get().init();
        CoreGraphicsContextPtr coreGraphicsContextPtr = this.mGraphicsContext;
        if (coreGraphicsContextPtr == null || coreGraphicsContextPtr.get() == null) {
            throw new IllegalStateException("Internal graphics engine error");
        }
        this.mGraphicsContext.get().init();
    }

    /* access modifiers changed from: package-private */
    public void activateGlContext() {
        EGLContext eGLContext;
        EGLSurface eGLSurface;
        if (this.mEglTmpDisplay == null) {
            this.mEglTmpDisplay = EGL14.eglGetCurrentDisplay();
            this.mEglTmpDrawSurface = EGL14.eglGetCurrentSurface(12377);
            this.mEglTmpReadSurface = EGL14.eglGetCurrentSurface(12378);
            this.mEglTmpContext = EGL14.eglGetCurrentContext();
            EGLDisplay eGLDisplay = this.mEglDisplay;
            if (eGLDisplay != null && (eGLContext = this.mEglContext) != null && (eGLSurface = this.mEglSurface) != null && !EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, eGLContext)) {
                throw new IllegalStateException("Error making GL context active.");
            }
            return;
        }
        throw new IllegalStateException("Previous context not deactivated.");
    }

    /* access modifiers changed from: package-private */
    public void deactivateGlContext() {
        if (this.mEglTmpDisplay == null || this.mEglTmpContext == null || EGL14.EGL_NO_CONTEXT.equals(this.mEglTmpContext) || EGL14.eglMakeCurrent(this.mEglTmpDisplay, this.mEglTmpDrawSurface, this.mEglTmpReadSurface, this.mEglTmpContext)) {
            this.mEglTmpDisplay = null;
            this.mEglTmpDrawSurface = null;
            this.mEglTmpReadSurface = null;
            this.mEglTmpContext = null;
            return;
        }
        throw new IllegalStateException("Error making GL context active.");
    }

    /* access modifiers changed from: package-private */
    public int createTextureOes() {
        activateGlContext();
        int[] textures = new int[1];
        GLES32.glGenTextures(1, textures, 0);
        GLES32.glBindTexture(36197, textures[0]);
        GLES32.glTexParameteri(36197, 10242, 33071);
        GLES32.glTexParameteri(36197, 10243, 33071);
        GLES32.glTexParameteri(36197, 10241, 9728);
        GLES32.glTexParameteri(36197, 10240, 9728);
        deactivateGlContext();
        return textures[0];
    }

    /* access modifiers changed from: package-private */
    public void deleteTextureOes(int textureId) {
        if (textureId != 0) {
            activateGlContext();
            int[] textures = {textureId};
            GLES32.glDeleteTextures(textures.length, textures, 0);
            deactivateGlContext();
        }
    }

    private int getExtraInt(Map<String, Object> backendExtra, String key, int defaultValue) {
        if (backendExtra == null) {
            return defaultValue;
        }
        Object value = backendExtra.getOrDefault(key, Integer.valueOf(defaultValue));
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        throw new IllegalArgumentException("Backend extra '" + key + "': Integer expected, got " + value.getClass().toString());
    }

    /* access modifiers changed from: package-private */
    public void initDevice(CoreDeviceCreateInfo.CoreBackend backend, Map<String, Object> backendExtra) {
        long applicationContext;
        int msaaSampleCount = getExtraInt(backendExtra, "MSAA_SAMPLE_COUNT", 0);
        int depthBufferBits = getExtraInt(backendExtra, "DEPTH_BUFFER_BITS", 24);
        int alphaBits = getExtraInt(backendExtra, "ALPHA_BITS", 8);
        int stencilBits = getExtraInt(backendExtra, "STENCIL_BITS", 0);
        EGLContext eGLContext = this.mEglContext;
        if (eGLContext != null) {
            long nativeEglContext = eGLContext.getNativeHandle();
            if (backend == CoreDeviceCreateInfo.CoreBackend.OPENGLES) {
                applicationContext = EGL14.EGL_NO_CONTEXT.getNativeHandle();
            } else {
                applicationContext = 0;
            }
            this.mDevice = Core.createDevice(this.mEngine.get(), backend, applicationContext, nativeEglContext, msaaSampleCount, depthBufferBits, alphaBits, stencilBits);
            return;
        }
        throw new IllegalStateException("mEglContext is null during initDevice.");
    }

    /* access modifiers changed from: package-private */
    public void release() {
        CoreGraphicsContextPtr coreGraphicsContextPtr = this.mGraphicsContext;
        if (coreGraphicsContextPtr != null) {
            coreGraphicsContextPtr.delete();
            this.mGraphicsContext = null;
        }
        CoreEnginePtr coreEnginePtr = this.mEngine;
        if (coreEnginePtr != null) {
            coreEnginePtr.delete();
            this.mEngine = null;
        }
        releaseGlContext();
    }

    /* access modifiers changed from: package-private */
    public CorePlatform getPlatform() {
        return this.mEngine.get().getPlatform();
    }

    /* access modifiers changed from: package-private */
    public CoreEngine getEngine() {
        return this.mEngine.get();
    }

    /* access modifiers changed from: package-private */
    public CoreDevice getDevice() {
        return this.mDevice;
    }

    /* access modifiers changed from: package-private */
    public CoreGraphicsContext getGraphicsContext() {
        return this.mGraphicsContext.get();
    }
}

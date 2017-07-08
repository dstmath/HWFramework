package com.android.server.display;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayManagerInternal.DisplayTransactionListener;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.server.LocalServices;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import libcore.io.Streams;

final class ColorFade {
    private static final int COLOR_FADE_LAYER = 1073741825;
    private static final boolean DEBUG = false;
    private static final int DEJANK_FRAMES = 3;
    public static final int MODE_COOL_DOWN = 1;
    public static final int MODE_FADE = 2;
    public static final int MODE_WARM_UP = 0;
    private static final String TAG = "ColorFade";
    private boolean mCreatedResources;
    private int mDisplayHeight;
    private final int mDisplayId;
    private int mDisplayLayerStack;
    private final DisplayManagerInternal mDisplayManagerInternal;
    private int mDisplayWidth;
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private final int[] mGLBuffers;
    private int mGammaLoc;
    private int mMode;
    private int mOpacityLoc;
    private boolean mPrepared;
    private int mProgram;
    private final float[] mProjMatrix;
    private int mProjMatrixLoc;
    private int mSaturationLoc;
    private int mScaleLoc;
    private Surface mSurface;
    private float mSurfaceAlpha;
    private SurfaceControl mSurfaceControl;
    private NaturalSurfaceLayout mSurfaceLayout;
    private SurfaceSession mSurfaceSession;
    private boolean mSurfaceVisible;
    private final FloatBuffer mTexCoordBuffer;
    private int mTexCoordLoc;
    private final float[] mTexMatrix;
    private int mTexMatrixLoc;
    private final int[] mTexNames;
    private boolean mTexNamesGenerated;
    private int mTexUnitLoc;
    private final FloatBuffer mVertexBuffer;
    private int mVertexLoc;

    private static final class NaturalSurfaceLayout implements DisplayTransactionListener {
        private final int mDisplayId;
        private final DisplayManagerInternal mDisplayManagerInternal;
        private SurfaceControl mSurfaceControl;

        public NaturalSurfaceLayout(DisplayManagerInternal displayManagerInternal, int displayId, SurfaceControl surfaceControl) {
            this.mDisplayManagerInternal = displayManagerInternal;
            this.mDisplayId = displayId;
            this.mSurfaceControl = surfaceControl;
            this.mDisplayManagerInternal.registerDisplayTransactionListener(this);
        }

        public void dispose() {
            synchronized (this) {
                this.mSurfaceControl = null;
            }
            this.mDisplayManagerInternal.unregisterDisplayTransactionListener(this);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onDisplayTransaction() {
            synchronized (this) {
                if (this.mSurfaceControl != null) {
                    DisplayInfo displayInfo = this.mDisplayManagerInternal.getDisplayInfo(this.mDisplayId);
                    switch (displayInfo.rotation) {
                        case ColorFade.MODE_WARM_UP /*0*/:
                            this.mSurfaceControl.setPosition(0.0f, 0.0f);
                            this.mSurfaceControl.setMatrix(1.0f, 0.0f, 0.0f, 1.0f);
                            break;
                        case ColorFade.MODE_COOL_DOWN /*1*/:
                            this.mSurfaceControl.setPosition(0.0f, (float) displayInfo.logicalHeight);
                            this.mSurfaceControl.setMatrix(0.0f, -1.0f, 1.0f, 0.0f);
                            break;
                        case ColorFade.MODE_FADE /*2*/:
                            this.mSurfaceControl.setPosition((float) displayInfo.logicalWidth, (float) displayInfo.logicalHeight);
                            this.mSurfaceControl.setMatrix(-1.0f, 0.0f, 0.0f, -1.0f);
                            break;
                        case ColorFade.DEJANK_FRAMES /*3*/:
                            this.mSurfaceControl.setPosition((float) displayInfo.logicalWidth, 0.0f);
                            this.mSurfaceControl.setMatrix(0.0f, 1.0f, -1.0f, 0.0f);
                            break;
                    }
                }
            }
        }
    }

    public ColorFade(int displayId) {
        this.mTexNames = new int[MODE_COOL_DOWN];
        this.mTexMatrix = new float[16];
        this.mProjMatrix = new float[16];
        this.mGLBuffers = new int[MODE_FADE];
        this.mVertexBuffer = createNativeFloatBuffer(8);
        this.mTexCoordBuffer = createNativeFloatBuffer(8);
        this.mDisplayId = displayId;
        this.mDisplayManagerInternal = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
    }

    public boolean prepare(Context context, int mode) {
        boolean captureScreenshotTextureAndSetViewport;
        this.mMode = mode;
        DisplayInfo displayInfo = this.mDisplayManagerInternal.getDisplayInfo(this.mDisplayId);
        this.mDisplayLayerStack = displayInfo.layerStack;
        this.mDisplayWidth = displayInfo.getNaturalWidth();
        this.mDisplayHeight = displayInfo.getNaturalHeight();
        if (createSurface() && createEglContext() && createEglSurface()) {
            captureScreenshotTextureAndSetViewport = captureScreenshotTextureAndSetViewport();
        } else {
            captureScreenshotTextureAndSetViewport = DEBUG;
        }
        if (!captureScreenshotTextureAndSetViewport) {
            dismiss();
            return DEBUG;
        } else if (!attachEglContext()) {
            return DEBUG;
        } else {
            try {
                if (initGLShaders(context) && initGLBuffers() && !checkGlErrors("prepare")) {
                    detachEglContext();
                    this.mCreatedResources = true;
                    this.mPrepared = true;
                    if (mode == MODE_COOL_DOWN) {
                        for (int i = MODE_WARM_UP; i < DEJANK_FRAMES; i += MODE_COOL_DOWN) {
                            draw(1.0f);
                        }
                    }
                    return true;
                }
                detachEglContext();
                dismiss();
                detachEglContext();
                return DEBUG;
            } catch (Throwable th) {
                detachEglContext();
            }
        }
    }

    private String readFile(Context context, int resourceId) {
        try {
            return new String(Streams.readFully(new InputStreamReader(context.getResources().openRawResource(resourceId))));
        } catch (IOException e) {
            Slog.e(TAG, "Unrecognized shader " + Integer.toString(resourceId));
            throw new RuntimeException(e);
        }
    }

    private int loadShader(Context context, int resourceId, int type) {
        String source = readFile(context, resourceId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[MODE_COOL_DOWN];
        GLES20.glGetShaderiv(shader, 35713, compiled, MODE_WARM_UP);
        if (compiled[MODE_WARM_UP] != 0) {
            return shader;
        }
        Slog.e(TAG, "Could not compile shader " + shader + ", " + type + ":");
        Slog.e(TAG, GLES20.glGetShaderSource(shader));
        Slog.e(TAG, GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return MODE_WARM_UP;
    }

    private boolean initGLShaders(Context context) {
        int vshader = loadShader(context, 17825796, 35633);
        int fshader = loadShader(context, 17825795, 35632);
        GLES20.glReleaseShaderCompiler();
        if (vshader == 0 || fshader == 0) {
            return DEBUG;
        }
        this.mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.mProgram, vshader);
        GLES20.glAttachShader(this.mProgram, fshader);
        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);
        GLES20.glLinkProgram(this.mProgram);
        this.mVertexLoc = GLES20.glGetAttribLocation(this.mProgram, "position");
        this.mTexCoordLoc = GLES20.glGetAttribLocation(this.mProgram, "uv");
        this.mProjMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "proj_matrix");
        this.mTexMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "tex_matrix");
        this.mOpacityLoc = GLES20.glGetUniformLocation(this.mProgram, "opacity");
        this.mGammaLoc = GLES20.glGetUniformLocation(this.mProgram, "gamma");
        this.mSaturationLoc = GLES20.glGetUniformLocation(this.mProgram, "saturation");
        this.mScaleLoc = GLES20.glGetUniformLocation(this.mProgram, "scale");
        this.mTexUnitLoc = GLES20.glGetUniformLocation(this.mProgram, "texUnit");
        GLES20.glUseProgram(this.mProgram);
        GLES20.glUniform1i(this.mTexUnitLoc, MODE_WARM_UP);
        GLES20.glUseProgram(MODE_WARM_UP);
        return true;
    }

    private void destroyGLShaders() {
        GLES20.glDeleteProgram(this.mProgram);
        checkGlErrors("glDeleteProgram");
    }

    private boolean initGLBuffers() {
        setQuad(this.mVertexBuffer, 0.0f, 0.0f, (float) this.mDisplayWidth, (float) this.mDisplayHeight);
        GLES20.glBindTexture(36197, this.mTexNames[MODE_WARM_UP]);
        GLES20.glTexParameteri(36197, 10240, 9728);
        GLES20.glTexParameteri(36197, 10241, 9728);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        GLES20.glBindTexture(36197, MODE_WARM_UP);
        GLES20.glGenBuffers(MODE_FADE, this.mGLBuffers, MODE_WARM_UP);
        GLES20.glBindBuffer(34962, this.mGLBuffers[MODE_WARM_UP]);
        GLES20.glBufferData(34962, this.mVertexBuffer.capacity() * 4, this.mVertexBuffer, 35044);
        GLES20.glBindBuffer(34962, this.mGLBuffers[MODE_COOL_DOWN]);
        GLES20.glBufferData(34962, this.mTexCoordBuffer.capacity() * 4, this.mTexCoordBuffer, 35044);
        GLES20.glBindBuffer(34962, MODE_WARM_UP);
        return true;
    }

    private void destroyGLBuffers() {
        GLES20.glDeleteBuffers(MODE_FADE, this.mGLBuffers, MODE_WARM_UP);
        checkGlErrors("glDeleteBuffers");
    }

    private static void setQuad(FloatBuffer vtx, float x, float y, float w, float h) {
        vtx.put(MODE_WARM_UP, x);
        vtx.put(MODE_COOL_DOWN, y);
        vtx.put(MODE_FADE, x);
        vtx.put(DEJANK_FRAMES, y + h);
        vtx.put(4, x + w);
        vtx.put(5, y + h);
        vtx.put(6, x + w);
        vtx.put(7, y);
    }

    public void dismissResources() {
        if (this.mCreatedResources) {
            attachEglContext();
            try {
                destroyScreenshotTexture();
                destroyGLShaders();
                destroyGLBuffers();
                destroyEglSurface();
                GLES20.glFlush();
                this.mCreatedResources = DEBUG;
            } finally {
                detachEglContext();
            }
        }
    }

    public void dismiss() {
        if (this.mPrepared) {
            dismissResources();
            destroySurface();
            this.mPrepared = DEBUG;
        }
    }

    public boolean draw(float level) {
        if (!this.mPrepared) {
            return DEBUG;
        }
        if (this.mMode == MODE_FADE) {
            return showSurface(1.0f - level);
        }
        if (!attachEglContext()) {
            return DEBUG;
        }
        try {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(DumpState.DUMP_KEYSETS);
            double one_minus_level = (double) (1.0f - level);
            double cos = Math.cos(3.141592653589793d * one_minus_level);
            drawFaded(((float) (-Math.pow(one_minus_level, 2.0d))) + 1.0f, 1.0f / ((float) (((((0.5d * ((double) (cos < 0.0d ? -1 : MODE_COOL_DOWN))) * Math.pow(cos, 2.0d)) + 0.5d) * 0.9d) + 0.1d)), (float) Math.pow((double) level, 4.0d), (float) ((((-Math.pow(one_minus_level, 2.0d)) + 1.0d) * 0.1d) + 0.9d));
            if (checkGlErrors("drawFrame")) {
                return DEBUG;
            }
            EGL14.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
            detachEglContext();
            return showSurface(1.0f);
        } finally {
            detachEglContext();
        }
    }

    private void drawFaded(float opacity, float gamma, float saturation, float scale) {
        GLES20.glUseProgram(this.mProgram);
        GLES20.glUniformMatrix4fv(this.mProjMatrixLoc, MODE_COOL_DOWN, DEBUG, this.mProjMatrix, MODE_WARM_UP);
        GLES20.glUniformMatrix4fv(this.mTexMatrixLoc, MODE_COOL_DOWN, DEBUG, this.mTexMatrix, MODE_WARM_UP);
        GLES20.glUniform1f(this.mOpacityLoc, opacity);
        GLES20.glUniform1f(this.mGammaLoc, gamma);
        GLES20.glUniform1f(this.mSaturationLoc, saturation);
        GLES20.glUniform1f(this.mScaleLoc, scale);
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(36197, this.mTexNames[MODE_WARM_UP]);
        GLES20.glBindBuffer(34962, this.mGLBuffers[MODE_WARM_UP]);
        GLES20.glEnableVertexAttribArray(this.mVertexLoc);
        GLES20.glVertexAttribPointer(this.mVertexLoc, MODE_FADE, 5126, DEBUG, MODE_WARM_UP, MODE_WARM_UP);
        GLES20.glBindBuffer(34962, this.mGLBuffers[MODE_COOL_DOWN]);
        GLES20.glEnableVertexAttribArray(this.mTexCoordLoc);
        GLES20.glVertexAttribPointer(this.mTexCoordLoc, MODE_FADE, 5126, DEBUG, MODE_WARM_UP, MODE_WARM_UP);
        GLES20.glDrawArrays(6, MODE_WARM_UP, 4);
        GLES20.glBindTexture(36197, MODE_WARM_UP);
        GLES20.glBindBuffer(34962, MODE_WARM_UP);
    }

    private void ortho(float left, float right, float bottom, float top, float znear, float zfar) {
        this.mProjMatrix[MODE_WARM_UP] = 2.0f / (right - left);
        this.mProjMatrix[MODE_COOL_DOWN] = 0.0f;
        this.mProjMatrix[MODE_FADE] = 0.0f;
        this.mProjMatrix[DEJANK_FRAMES] = 0.0f;
        this.mProjMatrix[4] = 0.0f;
        this.mProjMatrix[5] = 2.0f / (top - bottom);
        this.mProjMatrix[6] = 0.0f;
        this.mProjMatrix[7] = 0.0f;
        this.mProjMatrix[8] = 0.0f;
        this.mProjMatrix[9] = 0.0f;
        this.mProjMatrix[10] = -2.0f / (zfar - znear);
        this.mProjMatrix[11] = 0.0f;
        this.mProjMatrix[12] = (-(right + left)) / (right - left);
        this.mProjMatrix[13] = (-(top + bottom)) / (top - bottom);
        this.mProjMatrix[14] = (-(zfar + znear)) / (zfar - znear);
        this.mProjMatrix[15] = 1.0f;
    }

    private boolean captureScreenshotTextureAndSetViewport() {
        if (!attachEglContext()) {
            return DEBUG;
        }
        SurfaceTexture st;
        Surface s;
        try {
            if (!this.mTexNamesGenerated) {
                GLES20.glGenTextures(MODE_COOL_DOWN, this.mTexNames, MODE_WARM_UP);
                if (checkGlErrors("glGenTextures")) {
                    detachEglContext();
                    return DEBUG;
                }
                this.mTexNamesGenerated = true;
            }
            st = new SurfaceTexture(this.mTexNames[MODE_WARM_UP]);
            s = new Surface(st);
            SurfaceControl.screenshot(SurfaceControl.getBuiltInDisplay(MODE_WARM_UP), s);
            st.updateTexImage();
            st.getTransformMatrix(this.mTexMatrix);
            s.release();
            st.release();
            this.mTexCoordBuffer.put(MODE_WARM_UP, 0.0f);
            this.mTexCoordBuffer.put(MODE_COOL_DOWN, 0.0f);
            this.mTexCoordBuffer.put(MODE_FADE, 0.0f);
            this.mTexCoordBuffer.put(DEJANK_FRAMES, 1.0f);
            this.mTexCoordBuffer.put(4, 1.0f);
            this.mTexCoordBuffer.put(5, 1.0f);
            this.mTexCoordBuffer.put(6, 1.0f);
            this.mTexCoordBuffer.put(7, 0.0f);
            GLES20.glViewport(MODE_WARM_UP, MODE_WARM_UP, this.mDisplayWidth, this.mDisplayHeight);
            ortho(0.0f, (float) this.mDisplayWidth, 0.0f, (float) this.mDisplayHeight, -1.0f, 1.0f);
            detachEglContext();
            return true;
        } catch (Throwable th) {
            detachEglContext();
        }
    }

    private void destroyScreenshotTexture() {
        if (this.mTexNamesGenerated) {
            this.mTexNamesGenerated = DEBUG;
            GLES20.glDeleteTextures(MODE_COOL_DOWN, this.mTexNames, MODE_WARM_UP);
            checkGlErrors("glDeleteTextures");
        }
    }

    private boolean createEglContext() {
        if (this.mEglDisplay == null) {
            this.mEglDisplay = EGL14.eglGetDisplay(MODE_WARM_UP);
            if (this.mEglDisplay == EGL14.EGL_NO_DISPLAY) {
                logEglError("eglGetDisplay");
                return DEBUG;
            }
            int[] version = new int[MODE_FADE];
            if (!EGL14.eglInitialize(this.mEglDisplay, version, MODE_WARM_UP, version, MODE_COOL_DOWN)) {
                this.mEglDisplay = null;
                logEglError("eglInitialize");
                return DEBUG;
            }
        }
        if (this.mEglConfig == null) {
            EGLConfig[] eglConfigs = new EGLConfig[MODE_COOL_DOWN];
            if (EGL14.eglChooseConfig(this.mEglDisplay, new int[]{12352, 4, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12344}, MODE_WARM_UP, eglConfigs, MODE_WARM_UP, eglConfigs.length, new int[MODE_COOL_DOWN], MODE_WARM_UP)) {
                this.mEglConfig = eglConfigs[MODE_WARM_UP];
            } else {
                logEglError("eglChooseConfig");
                return DEBUG;
            }
        }
        if (this.mEglContext == null) {
            this.mEglContext = EGL14.eglCreateContext(this.mEglDisplay, this.mEglConfig, EGL14.EGL_NO_CONTEXT, new int[]{12440, MODE_FADE, 12344}, MODE_WARM_UP);
            if (this.mEglContext == null) {
                logEglError("eglCreateContext");
                return DEBUG;
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean createSurface() {
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
        }
        SurfaceControl.openTransaction();
        try {
            if (this.mSurfaceControl == null) {
                int flags;
                if (this.mMode == MODE_FADE) {
                    flags = 131076;
                } else {
                    flags = 1028;
                }
                this.mSurfaceControl = new SurfaceControl(this.mSurfaceSession, TAG, this.mDisplayWidth, this.mDisplayHeight, -1, flags);
                this.mSurfaceControl.setLayerStack(this.mDisplayLayerStack);
                this.mSurfaceControl.setSize(this.mDisplayWidth, this.mDisplayHeight);
                this.mSurface = new Surface();
                this.mSurface.copyFrom(this.mSurfaceControl);
                this.mSurfaceLayout = new NaturalSurfaceLayout(this.mDisplayManagerInternal, this.mDisplayId, this.mSurfaceControl);
                this.mSurfaceLayout.onDisplayTransaction();
            }
            SurfaceControl.closeTransaction();
            return true;
        } catch (OutOfResourcesException ex) {
            Slog.e(TAG, "Unable to create surface.", ex);
            return DEBUG;
        } catch (Throwable th) {
            SurfaceControl.closeTransaction();
        }
    }

    private boolean createEglSurface() {
        if (this.mEglSurface == null) {
            int[] eglSurfaceAttribList = new int[MODE_COOL_DOWN];
            eglSurfaceAttribList[MODE_WARM_UP] = 12344;
            this.mEglSurface = EGL14.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, this.mSurface, eglSurfaceAttribList, MODE_WARM_UP);
            if (this.mEglSurface == null) {
                logEglError("eglCreateWindowSurface");
                return DEBUG;
            }
        }
        return true;
    }

    private void destroyEglSurface() {
        if (this.mEglSurface != null) {
            if (!EGL14.eglDestroySurface(this.mEglDisplay, this.mEglSurface)) {
                logEglError("eglDestroySurface");
            }
            this.mEglSurface = null;
        }
    }

    private void destroySurface() {
        if (this.mSurfaceControl != null) {
            this.mSurfaceLayout.dispose();
            this.mSurfaceLayout = null;
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.destroy();
                this.mSurface.release();
                this.mSurfaceControl = null;
                this.mSurfaceVisible = DEBUG;
                this.mSurfaceAlpha = 0.0f;
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    private boolean showSurface(float alpha) {
        if (!(this.mSurfaceVisible && this.mSurfaceAlpha == alpha)) {
            SurfaceControl.openTransaction();
            try {
                this.mSurfaceControl.setLayer(COLOR_FADE_LAYER);
                this.mSurfaceControl.setAlpha(alpha);
                this.mSurfaceControl.show();
                this.mSurfaceVisible = true;
                this.mSurfaceAlpha = alpha;
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
        return true;
    }

    private boolean attachEglContext() {
        if (this.mEglSurface == null) {
            return DEBUG;
        }
        if (EGL14.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
            return true;
        }
        logEglError("eglMakeCurrent");
        return DEBUG;
    }

    private void detachEglContext() {
        if (this.mEglDisplay != null) {
            EGL14.eglMakeCurrent(this.mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }
    }

    private static FloatBuffer createNativeFloatBuffer(int size) {
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }

    private static void logEglError(String func) {
        Slog.e(TAG, func + " failed: error " + EGL14.eglGetError(), new Throwable());
    }

    private static boolean checkGlErrors(String func) {
        return checkGlErrors(func, true);
    }

    private static boolean checkGlErrors(String func, boolean log) {
        boolean hadError = DEBUG;
        while (true) {
            int error = GLES20.glGetError();
            if (error == 0) {
                return hadError;
            }
            if (log) {
                Slog.e(TAG, func + " failed: error " + error, new Throwable());
            }
            hadError = true;
        }
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Color Fade State:");
        pw.println("  mPrepared=" + this.mPrepared);
        pw.println("  mMode=" + this.mMode);
        pw.println("  mDisplayLayerStack=" + this.mDisplayLayerStack);
        pw.println("  mDisplayWidth=" + this.mDisplayWidth);
        pw.println("  mDisplayHeight=" + this.mDisplayHeight);
        pw.println("  mSurfaceVisible=" + this.mSurfaceVisible);
        pw.println("  mSurfaceAlpha=" + this.mSurfaceAlpha);
    }
}

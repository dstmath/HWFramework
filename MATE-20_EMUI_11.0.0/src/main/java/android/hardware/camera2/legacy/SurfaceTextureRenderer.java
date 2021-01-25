package android.hardware.camera2.legacy;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.legacy.LegacyExceptionUtils;
import android.net.wifi.WifiEnterpriseConfig;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;
import android.text.format.Time;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.Surface;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SurfaceTextureRenderer {
    private static final boolean DEBUG = false;
    private static final int EGL_COLOR_BITLENGTH = 8;
    private static final int EGL_RECORDABLE_ANDROID = 12610;
    private static final int FLIP_TYPE_BOTH = 3;
    private static final int FLIP_TYPE_HORIZONTAL = 1;
    private static final int FLIP_TYPE_NONE = 0;
    private static final int FLIP_TYPE_VERTICAL = 2;
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n";
    private static final int GLES_VERSION = 2;
    private static final int GL_MATRIX_SIZE = 16;
    private static final String LEGACY_PERF_PROPERTY = "persist.camera.legacy_perf";
    private static final int PBUFFER_PIXEL_BYTES = 4;
    private static final String TAG = SurfaceTextureRenderer.class.getSimpleName();
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 20;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final int VERTEX_POS_SIZE = 3;
    private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n  gl_Position = uMVPMatrix * aPosition;\n  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n";
    private static final int VERTEX_UV_SIZE = 2;
    private static final float[] sBothFlipTriangleVertices = {-1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f};
    private static final float[] sHorizontalFlipTriangleVertices = {-1.0f, -1.0f, 0.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] sRegularTriangleVertices = {-1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
    private static final float[] sVerticalFlipTriangleVertices = {-1.0f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f};
    private FloatBuffer mBothFlipTriangleVertices;
    private EGLConfig mConfigs;
    private List<EGLSurfaceHolder> mConversionSurfaces = new ArrayList();
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private final int mFacing;
    private FloatBuffer mHorizontalFlipTriangleVertices;
    private float[] mMVPMatrix = new float[16];
    private ByteBuffer mPBufferPixels;
    private PerfMeasurement mPerfMeasurer = null;
    private int mProgram;
    private FloatBuffer mRegularTriangleVertices;
    private float[] mSTMatrix = new float[16];
    private volatile SurfaceTexture mSurfaceTexture;
    private List<EGLSurfaceHolder> mSurfaces = new ArrayList();
    private int mTextureID = 0;
    private FloatBuffer mVerticalFlipTriangleVertices;
    private int maPositionHandle;
    private int maTextureHandle;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;

    /* access modifiers changed from: private */
    public class EGLSurfaceHolder {
        EGLSurface eglSurface;
        int height;
        Surface surface;
        int width;

        private EGLSurfaceHolder() {
        }
    }

    public SurfaceTextureRenderer(int facing) {
        this.mFacing = facing;
        this.mRegularTriangleVertices = ByteBuffer.allocateDirect(sRegularTriangleVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mRegularTriangleVertices.put(sRegularTriangleVertices).position(0);
        this.mHorizontalFlipTriangleVertices = ByteBuffer.allocateDirect(sHorizontalFlipTriangleVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mHorizontalFlipTriangleVertices.put(sHorizontalFlipTriangleVertices).position(0);
        this.mVerticalFlipTriangleVertices = ByteBuffer.allocateDirect(sVerticalFlipTriangleVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mVerticalFlipTriangleVertices.put(sVerticalFlipTriangleVertices).position(0);
        this.mBothFlipTriangleVertices = ByteBuffer.allocateDirect(sBothFlipTriangleVertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mBothFlipTriangleVertices.put(sBothFlipTriangleVertices).position(0);
        Matrix.setIdentityM(this.mSTMatrix, 0);
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        String str = TAG;
        Log.e(str, "Could not compile shader " + shaderType + SettingsStringUtil.DELIMITER);
        String str2 = TAG;
        Log.e(str2, WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        throw new IllegalStateException("Could not compile shader " + shaderType);
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int pixelShader;
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0 || (pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)) == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 1) {
            return program;
        }
        Log.e(TAG, "Could not link program: ");
        Log.e(TAG, GLES20.glGetProgramInfoLog(program));
        GLES20.glDeleteProgram(program);
        throw new IllegalStateException("Could not link program");
    }

    private void drawFrame(SurfaceTexture st, int width, int height, int flipType) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        FloatBuffer triangleVertices;
        checkGlError("onDrawFrame start");
        st.getTransformMatrix(this.mSTMatrix);
        Matrix.setIdentityM(this.mMVPMatrix, 0);
        try {
            Size dimens = LegacyCameraDevice.getTextureSize(st);
            float texWidth = (float) dimens.getWidth();
            float texHeight = (float) dimens.getHeight();
            if (texWidth <= 0.0f || texHeight <= 0.0f) {
                throw new IllegalStateException("Illegal intermediate texture with dimension of 0");
            }
            RectF intermediate = new RectF(0.0f, 0.0f, texWidth, texHeight);
            RectF output = new RectF(0.0f, 0.0f, (float) width, (float) height);
            android.graphics.Matrix boxingXform = new android.graphics.Matrix();
            boxingXform.setRectToRect(output, intermediate, Matrix.ScaleToFit.CENTER);
            boxingXform.mapRect(output);
            android.opengl.Matrix.scaleM(this.mMVPMatrix, 0, intermediate.width() / output.width(), intermediate.height() / output.height(), 1.0f);
            GLES20.glViewport(0, 0, width, height);
            GLES20.glUseProgram(this.mProgram);
            checkGlError("glUseProgram");
            GLES20.glActiveTexture(33984);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, this.mTextureID);
            if (flipType == 1) {
                triangleVertices = this.mHorizontalFlipTriangleVertices;
            } else if (flipType == 2) {
                triangleVertices = this.mVerticalFlipTriangleVertices;
            } else if (flipType != 3) {
                triangleVertices = this.mRegularTriangleVertices;
            } else {
                triangleVertices = this.mBothFlipTriangleVertices;
            }
            triangleVertices.position(0);
            GLES20.glVertexAttribPointer(this.maPositionHandle, 3, 5126, false, 20, (Buffer) triangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(this.maPositionHandle);
            checkGlError("glEnableVertexAttribArray maPositionHandle");
            triangleVertices.position(3);
            GLES20.glVertexAttribPointer(this.maTextureHandle, 2, 5126, false, 20, (Buffer) triangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(this.maTextureHandle);
            checkGlError("glEnableVertexAttribArray maTextureHandle");
            GLES20.glUniformMatrix4fv(this.muMVPMatrixHandle, 1, false, this.mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(this.muSTMatrixHandle, 1, false, this.mSTMatrix, 0);
            GLES20.glDrawArrays(5, 0, 4);
            checkGlDrawError("glDrawArrays");
        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
            throw new IllegalStateException("Surface abandoned, skipping drawFrame...", e);
        }
    }

    private void initializeGLState() {
        this.mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        int i = this.mProgram;
        if (i != 0) {
            this.maPositionHandle = GLES20.glGetAttribLocation(i, "aPosition");
            checkGlError("glGetAttribLocation aPosition");
            if (this.maPositionHandle != -1) {
                this.maTextureHandle = GLES20.glGetAttribLocation(this.mProgram, "aTextureCoord");
                checkGlError("glGetAttribLocation aTextureCoord");
                if (this.maTextureHandle != -1) {
                    this.muMVPMatrixHandle = GLES20.glGetUniformLocation(this.mProgram, "uMVPMatrix");
                    checkGlError("glGetUniformLocation uMVPMatrix");
                    if (this.muMVPMatrixHandle != -1) {
                        this.muSTMatrixHandle = GLES20.glGetUniformLocation(this.mProgram, "uSTMatrix");
                        checkGlError("glGetUniformLocation uSTMatrix");
                        if (this.muSTMatrixHandle != -1) {
                            int[] textures = new int[1];
                            GLES20.glGenTextures(1, textures, 0);
                            this.mTextureID = textures[0];
                            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, this.mTextureID);
                            checkGlError("glBindTexture mTextureID");
                            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 10241, 9728.0f);
                            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 10240, 9729.0f);
                            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 10242, 33071);
                            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 10243, 33071);
                            checkGlError("glTexParameter");
                            return;
                        }
                        throw new IllegalStateException("Could not get attrib location for uSTMatrix");
                    }
                    throw new IllegalStateException("Could not get attrib location for uMVPMatrix");
                }
                throw new IllegalStateException("Could not get attrib location for aTextureCoord");
            }
            throw new IllegalStateException("Could not get attrib location for aPosition");
        }
        throw new IllegalStateException("failed creating program");
    }

    private int getTextureId() {
        return this.mTextureID;
    }

    private void clearState() {
        this.mSurfaces.clear();
        for (EGLSurfaceHolder holder : this.mConversionSurfaces) {
            try {
                LegacyCameraDevice.disconnectSurface(holder.surface);
            } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                Log.w(TAG, "Surface abandoned, skipping...", e);
            }
        }
        this.mConversionSurfaces.clear();
        this.mPBufferPixels = null;
        if (this.mSurfaceTexture != null) {
            this.mSurfaceTexture.release();
        }
        this.mSurfaceTexture = null;
    }

    private void configureEGLContext() {
        this.mEGLDisplay = EGL14.eglGetDisplay(0);
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            int[] version = new int[2];
            if (EGL14.eglInitialize(this.mEGLDisplay, version, 0, version, 1)) {
                EGLConfig[] configs = new EGLConfig[1];
                EGL14.eglChooseConfig(this.mEGLDisplay, new int[]{12324, 8, 12323, 8, 12322, 8, 12352, 4, 12610, 1, 12339, 5, 12344}, 0, configs, 0, configs.length, new int[1], 0);
                checkEglError("eglCreateContext RGB888+recordable ES2");
                this.mConfigs = configs[0];
                this.mEGLContext = EGL14.eglCreateContext(this.mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT, new int[]{12440, 2, 12344}, 0);
                checkEglError("eglCreateContext");
                if (this.mEGLContext == EGL14.EGL_NO_CONTEXT) {
                    throw new IllegalStateException("No EGLContext could be made");
                }
                return;
            }
            throw new IllegalStateException("Cannot initialize EGL14");
        }
        throw new IllegalStateException("No EGL14 display");
    }

    private void configureEGLOutputSurfaces(Collection<EGLSurfaceHolder> surfaces) {
        if (surfaces == null || surfaces.size() == 0) {
            throw new IllegalStateException("No Surfaces were provided to draw to");
        }
        int[] surfaceAttribs = {12344};
        for (EGLSurfaceHolder holder : surfaces) {
            holder.eglSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, this.mConfigs, holder.surface, surfaceAttribs, 0);
            checkEglError("eglCreateWindowSurface");
        }
    }

    private void configureEGLPbufferSurfaces(Collection<EGLSurfaceHolder> surfaces) {
        if (surfaces == null || surfaces.size() == 0) {
            throw new IllegalStateException("No Surfaces were provided to draw to");
        }
        int maxLength = 0;
        for (EGLSurfaceHolder holder : surfaces) {
            int length = holder.width * holder.height;
            maxLength = length > maxLength ? length : maxLength;
            holder.eglSurface = EGL14.eglCreatePbufferSurface(this.mEGLDisplay, this.mConfigs, new int[]{12375, holder.width, 12374, holder.height, 12344}, 0);
            checkEglError("eglCreatePbufferSurface");
        }
        this.mPBufferPixels = ByteBuffer.allocateDirect(maxLength * 4).order(ByteOrder.nativeOrder());
    }

    private void releaseEGLContext() {
        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            dumpGlTiming();
            List<EGLSurfaceHolder> list = this.mSurfaces;
            if (list != null) {
                for (EGLSurfaceHolder holder : list) {
                    if (holder.eglSurface != null) {
                        EGL14.eglDestroySurface(this.mEGLDisplay, holder.eglSurface);
                    }
                }
            }
            List<EGLSurfaceHolder> list2 = this.mConversionSurfaces;
            if (list2 != null) {
                for (EGLSurfaceHolder holder2 : list2) {
                    if (holder2.eglSurface != null) {
                        EGL14.eglDestroySurface(this.mEGLDisplay, holder2.eglSurface);
                    }
                }
            }
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.mEGLDisplay);
        }
        this.mConfigs = null;
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLContext = EGL14.EGL_NO_CONTEXT;
        clearState();
    }

    private void makeCurrent(EGLSurface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        EGL14.eglMakeCurrent(this.mEGLDisplay, surface, surface, this.mEGLContext);
        checkEglDrawError("makeCurrent");
    }

    private boolean swapBuffers(EGLSurface surface) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        boolean result = EGL14.eglSwapBuffers(this.mEGLDisplay, surface);
        int error = EGL14.eglGetError();
        if (error == 12288) {
            return result;
        }
        if (error == 12299 || error == 12301) {
            throw new LegacyExceptionUtils.BufferQueueAbandonedException();
        }
        throw new IllegalStateException("swapBuffers: EGL error: 0x" + Integer.toHexString(error));
    }

    private void checkEglDrawError(String msg) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        if (EGL14.eglGetError() != 12299) {
            int error = EGL14.eglGetError();
            if (error != 12288) {
                throw new IllegalStateException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
            return;
        }
        throw new LegacyExceptionUtils.BufferQueueAbandonedException();
    }

    private void checkEglError(String msg) {
        int error = EGL14.eglGetError();
        if (error != 12288) {
            throw new IllegalStateException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    private void checkGlError(String msg) {
        int error = GLES20.glGetError();
        if (error != 0) {
            throw new IllegalStateException(msg + ": GLES20 error: 0x" + Integer.toHexString(error));
        }
    }

    private void checkGlDrawError(String msg) throws LegacyExceptionUtils.BufferQueueAbandonedException {
        int error;
        boolean surfaceAbandoned = false;
        boolean glError = false;
        while (true) {
            error = GLES20.glGetError();
            if (error == 0) {
                break;
            } else if (error == 1285) {
                surfaceAbandoned = true;
            } else {
                glError = true;
            }
        }
        if (glError) {
            throw new IllegalStateException(msg + ": GLES20 error: 0x" + Integer.toHexString(error));
        } else if (surfaceAbandoned) {
            throw new LegacyExceptionUtils.BufferQueueAbandonedException();
        }
    }

    private void dumpGlTiming() {
        if (this.mPerfMeasurer != null) {
            File legacyStorageDir = new File(Environment.getExternalStorageDirectory(), "CameraLegacy");
            if (legacyStorageDir.exists() || legacyStorageDir.mkdirs()) {
                StringBuilder path = new StringBuilder(legacyStorageDir.getPath());
                path.append(File.separator);
                path.append("durations_");
                Time now = new Time();
                now.setToNow();
                path.append(now.format2445());
                path.append("_S");
                for (EGLSurfaceHolder surface : this.mSurfaces) {
                    path.append(String.format("_%d_%d", Integer.valueOf(surface.width), Integer.valueOf(surface.height)));
                }
                path.append("_C");
                for (EGLSurfaceHolder surface2 : this.mConversionSurfaces) {
                    path.append(String.format("_%d_%d", Integer.valueOf(surface2.width), Integer.valueOf(surface2.height)));
                }
                path.append(".txt");
                this.mPerfMeasurer.dumpPerformanceData(path.toString());
                return;
            }
            Log.e(TAG, "Failed to create directory for data dump");
        }
    }

    private void setupGlTiming() {
        if (PerfMeasurement.isGlTimingSupported()) {
            Log.d(TAG, "Enabling GL performance measurement");
            this.mPerfMeasurer = new PerfMeasurement();
            return;
        }
        Log.d(TAG, "GL performance measurement not supported on this device");
        this.mPerfMeasurer = null;
    }

    private void beginGlTiming() {
        PerfMeasurement perfMeasurement = this.mPerfMeasurer;
        if (perfMeasurement != null) {
            perfMeasurement.startTimer();
        }
    }

    private void addGlTimestamp(long timestamp) {
        PerfMeasurement perfMeasurement = this.mPerfMeasurer;
        if (perfMeasurement != null) {
            perfMeasurement.addTimestamp(timestamp);
        }
    }

    private void endGlTiming() {
        PerfMeasurement perfMeasurement = this.mPerfMeasurer;
        if (perfMeasurement != null) {
            perfMeasurement.stopTimer();
        }
    }

    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTexture;
    }

    public void configureSurfaces(Collection<Pair<Surface, Size>> surfaces) {
        EGLSurface eGLSurface;
        releaseEGLContext();
        if (surfaces == null || surfaces.size() == 0) {
            Log.w(TAG, "No output surfaces configured for GL drawing.");
            return;
        }
        for (Pair<Surface, Size> p : surfaces) {
            Surface s = p.first;
            Size surfaceSize = p.second;
            try {
                EGLSurfaceHolder holder = new EGLSurfaceHolder();
                holder.surface = s;
                holder.width = surfaceSize.getWidth();
                holder.height = surfaceSize.getHeight();
                if (LegacyCameraDevice.needsConversion(s)) {
                    this.mConversionSurfaces.add(holder);
                    LegacyCameraDevice.connectSurface(s);
                } else {
                    this.mSurfaces.add(holder);
                }
            } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                Log.w(TAG, "Surface abandoned, skipping configuration... ", e);
            }
        }
        configureEGLContext();
        if (this.mSurfaces.size() > 0) {
            configureEGLOutputSurfaces(this.mSurfaces);
        }
        if (this.mConversionSurfaces.size() > 0) {
            configureEGLPbufferSurfaces(this.mConversionSurfaces);
        }
        try {
            if (this.mSurfaces.size() > 0) {
                eGLSurface = this.mSurfaces.get(0).eglSurface;
            } else {
                eGLSurface = this.mConversionSurfaces.get(0).eglSurface;
            }
            makeCurrent(eGLSurface);
        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e2) {
            Log.w(TAG, "Surface abandoned, skipping configuration... ", e2);
        }
        initializeGLState();
        this.mSurfaceTexture = new SurfaceTexture(getTextureId());
        if (SystemProperties.getBoolean(LEGACY_PERF_PROPERTY, false)) {
            setupGlTiming();
        }
    }

    public void drawIntoSurfaces(CaptureCollector targetCollector) {
        long timestamp;
        List<EGLSurfaceHolder> list = this.mSurfaces;
        if (list == null || list.size() == 0) {
            List<EGLSurfaceHolder> list2 = this.mConversionSurfaces;
            if (list2 == null) {
                return;
            }
            if (list2.size() == 0) {
                return;
            }
        }
        boolean doTiming = targetCollector.hasPendingPreviewCaptures();
        checkGlError("before updateTexImage");
        if (doTiming) {
            beginGlTiming();
        }
        this.mSurfaceTexture.updateTexImage();
        long timestamp2 = this.mSurfaceTexture.getTimestamp();
        Pair<RequestHolder, Long> captureHolder = targetCollector.previewCaptured(timestamp2);
        if (captureHolder != null) {
            RequestHolder request = captureHolder.first;
            Collection<Surface> targetSurfaces = request.getHolderTargets();
            if (doTiming) {
                addGlTimestamp(timestamp2);
            }
            List<Long> targetSurfaceIds = new ArrayList<>();
            try {
                targetSurfaceIds = LegacyCameraDevice.getSurfaceIds(targetSurfaces);
            } catch (LegacyExceptionUtils.BufferQueueAbandonedException e) {
                Log.w(TAG, "Surface abandoned, dropping frame. ", e);
                request.setOutputAbandoned();
            }
            List<EGLSurfaceHolder> list3 = this.mSurfaces;
            if (list3 != null) {
                for (EGLSurfaceHolder holder : list3) {
                    if (LegacyCameraDevice.containsSurfaceId(holder.surface, targetSurfaceIds)) {
                        try {
                            LegacyCameraDevice.setSurfaceDimens(holder.surface, holder.width, holder.height);
                            makeCurrent(holder.eglSurface);
                            LegacyCameraDevice.setNextTimestamp(holder.surface, captureHolder.second.longValue());
                            drawFrame(this.mSurfaceTexture, holder.width, holder.height, this.mFacing == 0 ? 1 : 0);
                            swapBuffers(holder.eglSurface);
                        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e2) {
                            Log.w(TAG, "Surface abandoned, dropping frame. ", e2);
                            request.setOutputAbandoned();
                        }
                    }
                }
            }
            List<EGLSurfaceHolder> list4 = this.mConversionSurfaces;
            if (list4 != null) {
                for (EGLSurfaceHolder holder2 : list4) {
                    if (LegacyCameraDevice.containsSurfaceId(holder2.surface, targetSurfaceIds)) {
                        try {
                            makeCurrent(holder2.eglSurface);
                            drawFrame(this.mSurfaceTexture, holder2.width, holder2.height, this.mFacing == 0 ? 3 : 2);
                            this.mPBufferPixels.clear();
                            timestamp = timestamp2;
                            GLES20.glReadPixels(0, 0, holder2.width, holder2.height, 6408, 5121, this.mPBufferPixels);
                            checkGlError("glReadPixels");
                            try {
                                int format = LegacyCameraDevice.detectSurfaceType(holder2.surface);
                                LegacyCameraDevice.setSurfaceDimens(holder2.surface, holder2.width, holder2.height);
                                LegacyCameraDevice.setNextTimestamp(holder2.surface, captureHolder.second.longValue());
                                LegacyCameraDevice.produceFrame(holder2.surface, this.mPBufferPixels.array(), holder2.width, holder2.height, format);
                            } catch (LegacyExceptionUtils.BufferQueueAbandonedException e3) {
                                Log.w(TAG, "Surface abandoned, dropping frame. ", e3);
                                request.setOutputAbandoned();
                            }
                        } catch (LegacyExceptionUtils.BufferQueueAbandonedException e4) {
                            throw new IllegalStateException("Surface abandoned, skipping drawFrame...", e4);
                        }
                    } else {
                        timestamp = timestamp2;
                    }
                    timestamp2 = timestamp;
                }
            }
            targetCollector.previewProduced();
            if (doTiming) {
                endGlTiming();
            }
        } else if (doTiming) {
            endGlTiming();
        }
    }

    public void cleanupEGLContext() {
        releaseEGLContext();
    }

    public void flush() {
        Log.e(TAG, "Flush not yet implemented.");
    }
}

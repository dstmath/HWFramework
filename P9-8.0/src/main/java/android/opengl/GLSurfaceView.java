package android.opengl;

import android.content.Context;
import android.net.ProxyInfo;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback2;
import android.view.SurfaceView;
import com.huawei.pgmng.log.LogPower;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceView extends SurfaceView implements Callback2 {
    public static final int DEBUG_CHECK_GL_ERROR = 1;
    public static final int DEBUG_LOG_GL_CALLS = 2;
    private static final boolean LOG_ATTACH_DETACH = false;
    private static final boolean LOG_EGL = false;
    private static final boolean LOG_PAUSE_RESUME = false;
    private static final boolean LOG_RENDERER = false;
    private static final boolean LOG_RENDERER_DRAW_FRAME = false;
    private static final boolean LOG_SURFACE = false;
    private static final boolean LOG_THREADS = false;
    public static final int RENDERMODE_CONTINUOUSLY = 1;
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    private static final String TAG = "GLSurfaceView";
    private static final long VSYNC_SPAN = 16666667;
    private static final GLThreadManager sGLThreadManager = new GLThreadManager();
    private int mDebugFlags;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private int mEGLContextClientVersion;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLThread mGLThread;
    private GLWrapper mGLWrapper;
    private boolean mPreserveEGLContextOnPause;
    private Renderer mRenderer;
    private final WeakReference<GLSurfaceView> mThisWeakRef = new WeakReference(this);

    public interface EGLConfigChooser {
        EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay);
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        abstract EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public BaseConfigChooser(int[] configSpec) {
            this.mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, 0, num_config)) {
                int numConfigs = num_config[0];
                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                }
                EGLConfig[] configs = new EGLConfig[numConfigs];
                if (egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config)) {
                    EGLConfig config = chooseConfig(egl, display, configs);
                    if (config != null) {
                        return config;
                    }
                    throw new IllegalArgumentException("No config chosen");
                }
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        private int[] filterConfigSpec(int[] configSpec) {
            if (GLSurfaceView.this.mEGLContextClientVersion != 2 && GLSurfaceView.this.mEGLContextClientVersion != 3) {
                return configSpec;
            }
            int len = configSpec.length;
            int[] newConfigSpec = new int[(len + 2)];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL14.EGL_RENDERABLE_TYPE;
            if (GLSurfaceView.this.mEGLContextClientVersion == 2) {
                newConfigSpec[len] = 4;
            } else {
                newConfigSpec[len] = 64;
            }
            newConfigSpec[len + 1] = EGL14.EGL_NONE;
            return newConfigSpec;
        }
    }

    private class ComponentSizeChooser extends BaseConfigChooser {
        protected int mAlphaSize;
        protected int mBlueSize;
        protected int mDepthSize;
        protected int mGreenSize;
        protected int mRedSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{EGL14.EGL_RED_SIZE, redSize, EGL14.EGL_GREEN_SIZE, greenSize, EGL14.EGL_BLUE_SIZE, blueSize, EGL14.EGL_ALPHA_SIZE, alphaSize, EGL14.EGL_DEPTH_SIZE, depthSize, EGL14.EGL_STENCIL_SIZE, stencilSize, EGL14.EGL_NONE});
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config, EGL14.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config, EGL14.EGL_STENCIL_SIZE, 0);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, EGL14.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config, EGL14.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config, EGL14.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config, EGL14.EGL_ALPHA_SIZE, 0);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
                return this.mValue[0];
            }
            return defaultValue;
        }
    }

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void destroyContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION;

        /* synthetic */ DefaultContextFactory(GLSurfaceView this$0, DefaultContextFactory -this1) {
            this();
        }

        private DefaultContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = 12440;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = new int[]{this.EGL_CONTEXT_CLIENT_VERSION, GLSurfaceView.this.mEGLContextClientVersion, EGL14.EGL_NONE};
            EGLContext eGLContext = EGL10.EGL_NO_CONTEXT;
            if (GLSurfaceView.this.mEGLContextClientVersion == 0) {
                attrib_list = null;
            }
            return egl.eglCreateContext(display, config, eGLContext, attrib_list);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void destroySurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
        /* synthetic */ DefaultWindowSurfaceFactory(DefaultWindowSurfaceFactory -this0) {
            this();
        }

        private DefaultWindowSurfaceFactory() {
        }

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                Log.e(GLSurfaceView.TAG, "eglCreateWindowSurface", e);
                return result;
            }
        }

        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private static class EglHelper {
        EGL10 mEgl;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;

        public EglHelper(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
            this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }

        public void start() {
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }
            if (this.mEgl.eglInitialize(this.mEglDisplay, new int[2])) {
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view == null) {
                    this.mEglConfig = null;
                    this.mEglContext = null;
                } else {
                    this.mEglConfig = view.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);
                    this.mEglContext = view.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                }
                if (this.mEglContext == null || this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    this.mEglContext = null;
                    throwEglException("createContext");
                }
                this.mEglSurface = null;
                return;
            }
            throw new RuntimeException("eglInitialize failed");
        }

        public boolean createSurface() {
            if (this.mEgl == null) {
                throw new RuntimeException("egl not initialized");
            } else if (this.mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            } else if (this.mEglConfig == null) {
                throw new RuntimeException("mEglConfig not initialized");
            } else {
                destroySurfaceImp();
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    this.mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, view.getHolder());
                } else {
                    this.mEglSurface = null;
                }
                if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                    if (this.mEgl.eglGetError() == 12299) {
                        Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    }
                    return false;
                } else if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                    return true;
                } else {
                    logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", this.mEgl.eglGetError());
                    return false;
                }
            }
        }

        GL createGL() {
            GL gl = this.mEglContext.getGL();
            GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
            if (view == null) {
                return gl;
            }
            if (view.mGLWrapper != null) {
                gl = view.mGLWrapper.wrap(gl);
            }
            if ((view.mDebugFlags & 3) == 0) {
                return gl;
            }
            int configFlags = 0;
            Writer writer = null;
            if ((view.mDebugFlags & 1) != 0) {
                configFlags = 1;
            }
            if ((view.mDebugFlags & 2) != 0) {
                writer = new LogWriter();
            }
            return GLDebugHelper.wrap(gl, configFlags, writer);
        }

        public int swap() {
            if (this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
                return 12288;
            }
            return this.mEgl.eglGetError();
        }

        public void destroySurface() {
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
                }
                this.mEglSurface = null;
            }
        }

        public void finish() {
            if (this.mEglContext != null) {
                GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
                }
                this.mEglContext = null;
            }
            if (this.mEglDisplay != null) {
                this.mEgl.eglTerminate(this.mEglDisplay);
                this.mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, this.mEgl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            throw new RuntimeException(formatEglError(function, error));
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + EGLLogWrapper.getErrorString(error);
        }
    }

    static class GLThread extends Thread {
        private EglHelper mEglHelper;
        private ArrayList<Runnable> mEventQueue = new ArrayList();
        private boolean mExited;
        private Runnable mFinishDrawingRunnable = null;
        private boolean mFinishedCreatingEglSurface;
        private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
        private boolean mHasSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mHeight = 0;
        private boolean mPaused;
        private boolean mRenderComplete;
        private int mRenderMode = 1;
        private boolean mRequestPaused;
        private boolean mRequestRender = true;
        private boolean mShouldExit;
        private boolean mShouldReleaseEglContext;
        private boolean mSizeChanged = true;
        private boolean mSurfaceIsBad;
        private boolean mWaitingForSurface;
        private boolean mWantRenderNotification = false;
        private int mWidth = 0;

        GLThread(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
            this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }

        public void run() {
            setName("GLThread " + getId());
            try {
                guardedRun();
            } catch (InterruptedException e) {
            } finally {
                GLSurfaceView.sGLThreadManager.threadExiting(this);
            }
        }

        private void stopEglSurfaceLocked() {
            if (this.mHaveEglSurface) {
                this.mHaveEglSurface = false;
                this.mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (this.mHaveEglContext) {
                this.mEglHelper.finish();
                this.mHaveEglContext = false;
                GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
            }
        }

        /* JADX WARNING: Missing block: B:29:0x00a1, code:
            if (r12 == null) goto L_0x0275;
     */
        /* JADX WARNING: Missing block: B:30:0x00a3, code:
            r12.run();
     */
        /* JADX WARNING: Missing block: B:31:0x00a6, code:
            r12 = null;
     */
        /* JADX WARNING: Missing block: B:116:0x0275, code:
            if (r9 == false) goto L_0x029d;
     */
        /* JADX WARNING: Missing block: B:119:0x0281, code:
            if (r42.mEglHelper.createSurface() == false) goto L_0x0395;
     */
        /* JADX WARNING: Missing block: B:120:0x0283, code:
            r38 = android.opengl.GLSurfaceView.-get8();
     */
        /* JADX WARNING: Missing block: B:121:0x0287, code:
            monitor-enter(r38);
     */
        /* JADX WARNING: Missing block: B:124:?, code:
            r42.mFinishedCreatingEglSurface = true;
            android.opengl.GLSurfaceView.-get8().notifyAll();
            r4 = java.lang.System.nanoTime();
     */
        /* JADX WARNING: Missing block: B:126:?, code:
            monitor-exit(r38);
     */
        /* JADX WARNING: Missing block: B:127:0x029c, code:
            r9 = false;
     */
        /* JADX WARNING: Missing block: B:128:0x029d, code:
            if (r10 == false) goto L_0x02af;
     */
        /* JADX WARNING: Missing block: B:129:0x029f, code:
            r14 = (javax.microedition.khronos.opengles.GL10) r42.mEglHelper.createGL();
            r10 = false;
     */
        /* JADX WARNING: Missing block: B:130:0x02af, code:
            if (r8 == false) goto L_0x02e8;
     */
        /* JADX WARNING: Missing block: B:131:0x02b1, code:
            r34 = (android.opengl.GLSurfaceView) r42.mGLSurfaceViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:132:0x02bd, code:
            if (r34 == null) goto L_0x02e7;
     */
        /* JADX WARNING: Missing block: B:134:?, code:
            android.os.Trace.traceBegin(8, "onSurfaceCreated");
            android.opengl.GLSurfaceView.-get7(r34).onSurfaceCreated(r14, r42.mEglHelper.mEglConfig);
     */
        /* JADX WARNING: Missing block: B:137:?, code:
            android.os.Trace.traceEnd(8);
     */
        /* JADX WARNING: Missing block: B:138:0x02e7, code:
            r8 = false;
     */
        /* JADX WARNING: Missing block: B:139:0x02e8, code:
            if (r26 == false) goto L_0x0316;
     */
        /* JADX WARNING: Missing block: B:140:0x02ea, code:
            r34 = (android.opengl.GLSurfaceView) r42.mGLSurfaceViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:141:0x02f6, code:
            if (r34 == null) goto L_0x0314;
     */
        /* JADX WARNING: Missing block: B:143:?, code:
            android.os.Trace.traceBegin(8, "onSurfaceChanged");
            android.opengl.GLSurfaceView.-get7(r34).onSurfaceChanged(r14, r35, r15);
     */
        /* JADX WARNING: Missing block: B:146:?, code:
            android.os.Trace.traceEnd(8);
     */
        /* JADX WARNING: Missing block: B:147:0x0314, code:
            r26 = false;
     */
        /* JADX WARNING: Missing block: B:148:0x0316, code:
            r30 = java.lang.System.nanoTime();
            r34 = (android.opengl.GLSurfaceView) r42.mGLSurfaceViewWeakRef.get();
     */
        /* JADX WARNING: Missing block: B:149:0x0326, code:
            if (r34 == null) goto L_0x0348;
     */
        /* JADX WARNING: Missing block: B:151:?, code:
            android.os.Trace.traceBegin(8, "onDrawFrame");
            android.opengl.GLSurfaceView.-get7(r34).onDrawFrame(r14);
     */
        /* JADX WARNING: Missing block: B:152:0x033d, code:
            if (r13 == null) goto L_0x0343;
     */
        /* JADX WARNING: Missing block: B:153:0x033f, code:
            r13.run();
     */
        /* JADX WARNING: Missing block: B:154:0x0342, code:
            r13 = null;
     */
        /* JADX WARNING: Missing block: B:157:?, code:
            android.os.Trace.traceEnd(8);
     */
        /* JADX WARNING: Missing block: B:158:0x0348, code:
            r27 = r42.mEglHelper.swap();
     */
        /* JADX WARNING: Missing block: B:159:0x0352, code:
            switch(r27) {
                case 12288: goto L_0x0379;
                case android.opengl.EGL14.EGL_CONTEXT_LOST :int: goto L_0x03cc;
                default: goto L_0x0355;
            };
     */
        /* JADX WARNING: Missing block: B:160:0x0355, code:
            android.opengl.GLSurfaceView.EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", r27);
            r38 = android.opengl.GLSurfaceView.-get8();
     */
        /* JADX WARNING: Missing block: B:161:0x0368, code:
            monitor-enter(r38);
     */
        /* JADX WARNING: Missing block: B:164:?, code:
            r42.mSurfaceIsBad = true;
            android.opengl.GLSurfaceView.-get8().notifyAll();
     */
        /* JADX WARNING: Missing block: B:166:?, code:
            monitor-exit(r38);
     */
        /* JADX WARNING: Missing block: B:167:0x0379, code:
            r22 = java.lang.System.nanoTime();
     */
        /* JADX WARNING: Missing block: B:168:0x037d, code:
            if (r7 != false) goto L_0x03d2;
     */
        /* JADX WARNING: Missing block: B:170:0x0388, code:
            if ((r22 - r4) < 60000000000L) goto L_0x038b;
     */
        /* JADX WARNING: Missing block: B:171:0x038a, code:
            r7 = true;
     */
        /* JADX WARNING: Missing block: B:172:0x038b, code:
            if (r36 == false) goto L_0x0061;
     */
        /* JADX WARNING: Missing block: B:173:0x038d, code:
            r11 = true;
            r36 = false;
     */
        /* JADX WARNING: Missing block: B:177:0x0395, code:
            r38 = android.opengl.GLSurfaceView.-get8();
     */
        /* JADX WARNING: Missing block: B:178:0x0399, code:
            monitor-enter(r38);
     */
        /* JADX WARNING: Missing block: B:181:?, code:
            r42.mFinishedCreatingEglSurface = true;
            r42.mSurfaceIsBad = true;
            android.opengl.GLSurfaceView.-get8().notifyAll();
     */
        /* JADX WARNING: Missing block: B:183:?, code:
            monitor-exit(r38);
     */
        /* JADX WARNING: Missing block: B:197:0x03cc, code:
            r18 = true;
     */
        /* JADX WARNING: Missing block: B:201:0x03d2, code:
            r28 = (r22 - r30) / android.opengl.GLSurfaceView.VSYNC_SPAN;
     */
        /* JADX WARNING: Missing block: B:202:0x03dd, code:
            if (r28 < 30) goto L_0x038b;
     */
        /* JADX WARNING: Missing block: B:203:0x03df, code:
            android.os.Trace.traceBegin(8, "jank_event_sync: start_ts=" + r30 + ",end_ts=" + r22 + ", appid=" + r19);
            android.os.Trace.traceEnd(8);
            android.util.Jlog.d(311, "#P:" + r20 + "#SK:" + r28 + "#IP:0" + "#FRT:" + (r30 / android.app.job.JobInfo.MIN_BACKOFF_MILLIS) + "#DNT:" + (r22 / android.app.job.JobInfo.MIN_BACKOFF_MILLIS));
            android.util.Log.i(android.opengl.GLSurfaceView.TAG, "OpenGL:" + r20 + " Skipped " + r28 + " frames!");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void guardedRun() throws InterruptedException {
            this.mEglHelper = new EglHelper(this.mGLSurfaceViewWeakRef);
            this.mHaveEglContext = false;
            this.mHaveEglSurface = false;
            this.mWantRenderNotification = false;
            GL10 gl = null;
            boolean createEglContext = false;
            boolean createEglSurface = false;
            boolean createGlInterface = false;
            boolean lostEglContext = false;
            boolean sizeChanged = false;
            boolean wantRenderNotification = false;
            boolean doRenderNotification = false;
            boolean askedToReleaseEglContext = false;
            int w = 0;
            int h = 0;
            Runnable event = null;
            Runnable finishDrawingRunnable = null;
            boolean checkskipframe = false;
            long SurfaceCreateTime = Long.MAX_VALUE;
            try {
                int mPackageId = Process.myPid();
                String mPackageName = ProxyInfo.LOCAL_EXCL_LIST;
                GLSurfaceView jankview = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                if (jankview != null) {
                    Context jankcontext = jankview.getContext();
                    if (jankcontext != null) {
                        mPackageName = jankcontext.getPackageName();
                    }
                }
                while (true) {
                    synchronized (GLSurfaceView.sGLThreadManager) {
                        while (!this.mShouldExit) {
                            if (this.mEventQueue.isEmpty()) {
                                boolean pausing = false;
                                if (this.mPaused != this.mRequestPaused) {
                                    pausing = this.mRequestPaused;
                                    this.mPaused = this.mRequestPaused;
                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                }
                                if (this.mShouldReleaseEglContext) {
                                    stopEglSurfaceLocked();
                                    stopEglContextLocked();
                                    this.mShouldReleaseEglContext = false;
                                    askedToReleaseEglContext = true;
                                }
                                if (lostEglContext) {
                                    stopEglSurfaceLocked();
                                    stopEglContextLocked();
                                    lostEglContext = false;
                                }
                                if (pausing && this.mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                if (pausing && this.mHaveEglContext) {
                                    GLSurfaceView view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                                    if (!(view == null ? false : view.mPreserveEGLContextOnPause)) {
                                        stopEglContextLocked();
                                    }
                                }
                                if (!(this.mHasSurface || (this.mWaitingForSurface ^ 1) == 0)) {
                                    if (this.mHaveEglSurface) {
                                        stopEglSurfaceLocked();
                                    }
                                    this.mWaitingForSurface = true;
                                    this.mSurfaceIsBad = false;
                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                }
                                if (this.mHasSurface && this.mWaitingForSurface) {
                                    this.mWaitingForSurface = false;
                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                }
                                if (doRenderNotification) {
                                    this.mWantRenderNotification = false;
                                    doRenderNotification = false;
                                    this.mRenderComplete = true;
                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                }
                                if (this.mFinishDrawingRunnable != null) {
                                    finishDrawingRunnable = this.mFinishDrawingRunnable;
                                    this.mFinishDrawingRunnable = null;
                                }
                                if (readyToDraw()) {
                                    if (!this.mHaveEglContext) {
                                        if (askedToReleaseEglContext) {
                                            askedToReleaseEglContext = false;
                                        } else {
                                            this.mEglHelper.start();
                                            this.mHaveEglContext = true;
                                            createEglContext = true;
                                            GLSurfaceView.sGLThreadManager.notifyAll();
                                        }
                                    }
                                    if (this.mHaveEglContext && (this.mHaveEglSurface ^ 1) != 0) {
                                        this.mHaveEglSurface = true;
                                        createEglSurface = true;
                                        createGlInterface = true;
                                        sizeChanged = true;
                                    }
                                    if (this.mHaveEglSurface) {
                                        if (this.mSizeChanged) {
                                            sizeChanged = true;
                                            w = this.mWidth;
                                            h = this.mHeight;
                                            this.mWantRenderNotification = true;
                                            createEglSurface = true;
                                            this.mSizeChanged = false;
                                        }
                                        this.mRequestRender = false;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                        if (this.mWantRenderNotification) {
                                            wantRenderNotification = true;
                                        }
                                    }
                                } else if (finishDrawingRunnable != null) {
                                    Log.w(GLSurfaceView.TAG, "Warning, !readyToDraw() but waiting for draw finished! Early reporting draw finished.");
                                    finishDrawingRunnable.run();
                                    finishDrawingRunnable = null;
                                }
                                GLSurfaceView.sGLThreadManager.wait();
                            } else {
                                event = (Runnable) this.mEventQueue.remove(0);
                            }
                        }
                        synchronized (GLSurfaceView.sGLThreadManager) {
                            stopEglSurfaceLocked();
                            stopEglContextLocked();
                        }
                        return;
                    }
                }
            } catch (RuntimeException t) {
                GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
                throw t;
            } catch (Throwable th) {
                synchronized (GLSurfaceView.sGLThreadManager) {
                    stopEglSurfaceLocked();
                    stopEglContextLocked();
                }
            }
        }

        public boolean ableToDraw() {
            return (this.mHaveEglContext && this.mHaveEglSurface) ? readyToDraw() : false;
        }

        private boolean readyToDraw() {
            if (this.mPaused || !this.mHasSurface || (this.mSurfaceIsBad ^ 1) == 0 || this.mWidth <= 0 || this.mHeight <= 0) {
                return false;
            }
            return this.mRequestRender || this.mRenderMode == 1;
        }

        public void setRenderMode(int renderMode) {
            if (renderMode < 0 || renderMode > 1) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mRenderMode = renderMode;
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
        }

        public int getRenderMode() {
            int i;
            synchronized (GLSurfaceView.sGLThreadManager) {
                i = this.mRenderMode;
            }
            return i;
        }

        public void requestRender() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mRequestRender = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
        }

        public void requestRenderAndNotify(Runnable finishDrawing) {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (Thread.currentThread() == this) {
                    return;
                }
                this.mWantRenderNotification = true;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                this.mFinishDrawingRunnable = finishDrawing;
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
        }

        public void surfaceCreated() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mHasSurface = true;
                this.mFinishedCreatingEglSurface = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (this.mWaitingForSurface && (this.mFinishedCreatingEglSurface ^ 1) != 0 && (this.mExited ^ 1) != 0) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mHasSurface = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mWaitingForSurface && (this.mExited ^ 1) != 0) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mRequestPaused = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && (this.mPaused ^ 1) != 0) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mRequestPaused = false;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && this.mPaused && (this.mRenderComplete ^ 1) != 0) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:28:0x004c, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onWindowResize(int w, int h) {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mWidth = w;
                this.mHeight = h;
                this.mSizeChanged = true;
                this.mRequestRender = true;
                this.mRenderComplete = false;
                if (Thread.currentThread() == this) {
                    return;
                }
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && (this.mPaused ^ 1) != 0 && (this.mRenderComplete ^ 1) != 0 && ableToDraw()) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestExitAndWait() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mShouldExit = true;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            this.mShouldReleaseEglContext = true;
            GLSurfaceView.sGLThreadManager.notifyAll();
        }

        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mEventQueue.add(r);
                GLSurfaceView.sGLThreadManager.notifyAll();
            }
        }
    }

    private static class GLThreadManager {
        private static String TAG = "GLThreadManager";

        /* synthetic */ GLThreadManager(GLThreadManager -this0) {
            this();
        }

        private GLThreadManager() {
        }

        public synchronized void threadExiting(GLThread thread) {
            thread.mExited = true;
            notifyAll();
        }

        public void releaseEglContextLocked(GLThread thread) {
            notifyAll();
        }
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    static class LogWriter extends Writer {
        private StringBuilder mBuilder = new StringBuilder();

        LogWriter() {
        }

        public void close() {
            flushBuilder();
        }

        public void flush() {
            flushBuilder();
        }

        public void write(char[] buf, int offset, int count) {
            for (int i = 0; i < count; i++) {
                char c = buf[offset + i];
                if (c == 10) {
                    flushBuilder();
                } else {
                    this.mBuilder.append(c);
                }
            }
        }

        private void flushBuilder() {
            if (this.mBuilder.length() > 0) {
                Log.v(GLSurfaceView.TAG, this.mBuilder.toString());
                this.mBuilder.delete(0, this.mBuilder.length());
            }
        }
    }

    public interface Renderer {
        void onDrawFrame(GL10 gl10);

        void onSurfaceChanged(GL10 gl10, int i, int i2);

        void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig);
    }

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            int i;
            if (withDepthBuffer) {
                i = 16;
            } else {
                i = 0;
            }
            super(8, 8, 8, 0, i, 0);
        }
    }

    public GLSurfaceView(Context context) {
        super(context);
        init();
    }

    public GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mGLThread != null) {
                this.mGLThread.requestExitAndWait();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void init() {
        getHolder().addCallback(this);
    }

    public void setGLWrapper(GLWrapper glWrapper) {
        this.mGLWrapper = glWrapper;
    }

    public void setDebugFlags(int debugFlags) {
        this.mDebugFlags = debugFlags;
    }

    public int getDebugFlags() {
        return this.mDebugFlags;
    }

    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        this.mPreserveEGLContextOnPause = preserveOnPause;
    }

    public boolean getPreserveEGLContextOnPause() {
        return this.mPreserveEGLContextOnPause;
    }

    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();
        if (this.mEGLConfigChooser == null) {
            this.mEGLConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory(this, null);
        }
        if (this.mEGLWindowSurfaceFactory == null) {
            this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        this.mRenderer = renderer;
        this.mGLThread = new GLThread(this.mThisWeakRef);
        this.mGLThread.start();
    }

    public void setEGLContextFactory(EGLContextFactory factory) {
        checkRenderThreadState();
        this.mEGLContextFactory = factory;
    }

    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        this.mEGLWindowSurfaceFactory = factory;
    }

    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        this.mEGLConfigChooser = configChooser;
    }

    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
    }

    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        this.mEGLContextClientVersion = version;
    }

    public void setRenderMode(int renderMode) {
        this.mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return this.mGLThread.getRenderMode();
    }

    public void requestRender() {
        this.mGLThread.requestRender();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.mGLThread.surfaceCreated();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mGLThread.surfaceDestroyed();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        this.mGLThread.onWindowResize(w, h);
    }

    public void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable finishDrawing) {
        if (this.mGLThread != null) {
            this.mGLThread.requestRenderAndNotify(finishDrawing);
        }
    }

    @Deprecated
    public void surfaceRedrawNeeded(SurfaceHolder holder) {
    }

    public void onPause() {
        LogPower.push(107, this.mContext.getPackageName());
        this.mGLThread.onPause();
    }

    public void onResume() {
        this.mGLThread.onResume();
        LogPower.push(106, this.mContext.getPackageName());
    }

    public void queueEvent(Runnable r) {
        this.mGLThread.queueEvent(r);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDetached && this.mRenderer != null) {
            int renderMode = 1;
            if (this.mGLThread != null) {
                renderMode = this.mGLThread.getRenderMode();
            }
            this.mGLThread = new GLThread(this.mThisWeakRef);
            if (renderMode != 1) {
                this.mGLThread.setRenderMode(renderMode);
            }
            this.mGLThread.start();
        }
        this.mDetached = false;
    }

    protected void onDetachedFromWindow() {
        if (this.mGLThread != null) {
            this.mGLThread.requestExitAndWait();
        }
        this.mDetached = true;
        super.onDetachedFromWindow();
    }

    private void checkRenderThreadState() {
        if (this.mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }
}

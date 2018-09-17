package android.opengl;

import android.content.Context;
import android.media.MediaFile;
import android.net.LinkQualityInfo;
import android.net.ProxyInfo;
import android.net.wifi.ScanResult.InformationElement;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Jlog;
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
    private static final GLThreadManager sGLThreadManager = null;
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
    private final WeakReference<GLSurfaceView> mThisWeakRef;

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
            int[] num_config = new int[GLSurfaceView.RENDERMODE_CONTINUOUSLY];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, GLSurfaceView.RENDERMODE_WHEN_DIRTY, num_config)) {
                int numConfigs = num_config[GLSurfaceView.RENDERMODE_WHEN_DIRTY];
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
            if (GLSurfaceView.this.mEGLContextClientVersion != GLSurfaceView.DEBUG_LOG_GL_CALLS && GLSurfaceView.this.mEGLContextClientVersion != 3) {
                return configSpec;
            }
            int len = configSpec.length;
            int[] newConfigSpec = new int[(len + GLSurfaceView.DEBUG_LOG_GL_CALLS)];
            System.arraycopy(configSpec, GLSurfaceView.RENDERMODE_WHEN_DIRTY, newConfigSpec, GLSurfaceView.RENDERMODE_WHEN_DIRTY, len - 1);
            newConfigSpec[len - 1] = EGL14.EGL_RENDERABLE_TYPE;
            if (GLSurfaceView.this.mEGLContextClientVersion == GLSurfaceView.DEBUG_LOG_GL_CALLS) {
                newConfigSpec[len] = 4;
            } else {
                newConfigSpec[len] = 64;
            }
            newConfigSpec[len + GLSurfaceView.RENDERMODE_CONTINUOUSLY] = EGL14.EGL_NONE;
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
        private int[] mValue;

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{EGL14.EGL_RED_SIZE, redSize, EGL14.EGL_GREEN_SIZE, greenSize, EGL14.EGL_BLUE_SIZE, blueSize, EGL14.EGL_ALPHA_SIZE, alphaSize, EGL14.EGL_DEPTH_SIZE, depthSize, EGL14.EGL_STENCIL_SIZE, stencilSize, EGL14.EGL_NONE});
            this.mValue = new int[GLSurfaceView.RENDERMODE_CONTINUOUSLY];
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            int length = configs.length;
            for (int i = GLSurfaceView.RENDERMODE_WHEN_DIRTY; i < length; i += GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
                EGLConfig config = configs[i];
                int d = findConfigAttrib(egl, display, config, EGL14.EGL_DEPTH_SIZE, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                int s = findConfigAttrib(egl, display, config, EGL14.EGL_STENCIL_SIZE, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, EGL14.EGL_RED_SIZE, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    int g = findConfigAttrib(egl, display, config, EGL14.EGL_GREEN_SIZE, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    int b = findConfigAttrib(egl, display, config, EGL14.EGL_BLUE_SIZE, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    int a = findConfigAttrib(egl, display, config, EGL14.EGL_ALPHA_SIZE, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
                return this.mValue[GLSurfaceView.RENDERMODE_WHEN_DIRTY];
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

        private DefaultContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = EGLExt.EGL_CONTEXT_MAJOR_VERSION_KHR;
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
        private DefaultWindowSurfaceFactory() {
        }

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                Log.e(GLSurfaceView.TAG, "eglCreateWindowSurface", e);
            }
            return result;
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
            if (this.mEgl.eglInitialize(this.mEglDisplay, new int[GLSurfaceView.DEBUG_LOG_GL_CALLS])) {
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
                    if (this.mEgl.eglGetError() == EGL14.EGL_BAD_NATIVE_WINDOW) {
                        Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    }
                    return GLSurfaceView.LOG_THREADS;
                } else if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                    return true;
                } else {
                    logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", this.mEgl.eglGetError());
                    return GLSurfaceView.LOG_THREADS;
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
            int configFlags = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
            Writer writer = null;
            if ((view.mDebugFlags & GLSurfaceView.RENDERMODE_CONTINUOUSLY) != 0) {
                configFlags = GLSurfaceView.RENDERMODE_CONTINUOUSLY;
            }
            if ((view.mDebugFlags & GLSurfaceView.DEBUG_LOG_GL_CALLS) != 0) {
                writer = new LogWriter();
            }
            return GLDebugHelper.wrap(gl, configFlags, writer);
        }

        public int swap() {
            if (this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
                return GLES11.GL_CLIP_PLANE0;
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
        private ArrayList<Runnable> mEventQueue;
        private boolean mExited;
        private boolean mFinishedCreatingEglSurface;
        private WeakReference<GLSurfaceView> mGLSurfaceViewWeakRef;
        private boolean mHasSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mHeight;
        private boolean mPaused;
        private boolean mRenderComplete;
        private int mRenderMode;
        private boolean mRequestPaused;
        private boolean mRequestRender;
        private boolean mShouldExit;
        private boolean mShouldReleaseEglContext;
        private boolean mSizeChanged;
        private boolean mSurfaceIsBad;
        private boolean mWaitingForSurface;
        private boolean mWantRenderNotification;
        private int mWidth;

        GLThread(WeakReference<GLSurfaceView> glSurfaceViewWeakRef) {
            this.mEventQueue = new ArrayList();
            this.mSizeChanged = true;
            this.mWidth = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
            this.mHeight = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
            this.mRequestRender = true;
            this.mRenderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY;
            this.mWantRenderNotification = GLSurfaceView.LOG_THREADS;
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
                this.mHaveEglSurface = GLSurfaceView.LOG_THREADS;
                this.mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (this.mHaveEglContext) {
                this.mEglHelper.finish();
                this.mHaveEglContext = GLSurfaceView.LOG_THREADS;
                GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
            }
        }

        private void guardedRun() throws InterruptedException {
            this.mEglHelper = new EglHelper(this.mGLSurfaceViewWeakRef);
            this.mHaveEglContext = GLSurfaceView.LOG_THREADS;
            this.mHaveEglSurface = GLSurfaceView.LOG_THREADS;
            this.mWantRenderNotification = GLSurfaceView.LOG_THREADS;
            GL10 gl = null;
            boolean createEglContext = GLSurfaceView.LOG_THREADS;
            boolean createEglSurface = GLSurfaceView.LOG_THREADS;
            boolean createGlInterface = GLSurfaceView.LOG_THREADS;
            boolean lostEglContext = GLSurfaceView.LOG_THREADS;
            boolean sizeChanged = GLSurfaceView.LOG_THREADS;
            boolean wantRenderNotification = GLSurfaceView.LOG_THREADS;
            boolean doRenderNotification = GLSurfaceView.LOG_THREADS;
            boolean askedToReleaseEglContext = GLSurfaceView.LOG_THREADS;
            int w = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
            int h = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
            Runnable event = null;
            boolean checkskipframe = GLSurfaceView.LOG_THREADS;
            long SurfaceCreateTime = LinkQualityInfo.UNKNOWN_LONG;
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
                    while (true) {
                        if (this.mShouldExit) {
                            synchronized (GLSurfaceView.sGLThreadManager) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                            }
                            return;
                        }
                        GLSurfaceView view;
                        if (this.mEventQueue.isEmpty()) {
                            boolean z = GLSurfaceView.LOG_THREADS;
                            if (this.mPaused != this.mRequestPaused) {
                                z = this.mRequestPaused;
                                this.mPaused = this.mRequestPaused;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (this.mShouldReleaseEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                this.mShouldReleaseEglContext = GLSurfaceView.LOG_THREADS;
                                askedToReleaseEglContext = true;
                            }
                            if (lostEglContext) {
                                stopEglSurfaceLocked();
                                stopEglContextLocked();
                                lostEglContext = GLSurfaceView.LOG_THREADS;
                            }
                            if (z && this.mHaveEglSurface) {
                                stopEglSurfaceLocked();
                            }
                            if (z && this.mHaveEglContext) {
                                view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                                if (!(view == null ? GLSurfaceView.LOG_THREADS : view.mPreserveEGLContextOnPause) || GLSurfaceView.sGLThreadManager.shouldReleaseEGLContextWhenPausing()) {
                                    stopEglContextLocked();
                                }
                            }
                            if (z && GLSurfaceView.sGLThreadManager.shouldTerminateEGLWhenPausing()) {
                                this.mEglHelper.finish();
                            }
                            if (!(this.mHasSurface || this.mWaitingForSurface)) {
                                if (this.mHaveEglSurface) {
                                    stopEglSurfaceLocked();
                                }
                                this.mWaitingForSurface = true;
                                this.mSurfaceIsBad = GLSurfaceView.LOG_THREADS;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (this.mHasSurface && this.mWaitingForSurface) {
                                this.mWaitingForSurface = GLSurfaceView.LOG_THREADS;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (doRenderNotification) {
                                this.mWantRenderNotification = GLSurfaceView.LOG_THREADS;
                                doRenderNotification = GLSurfaceView.LOG_THREADS;
                                this.mRenderComplete = true;
                                GLSurfaceView.sGLThreadManager.notifyAll();
                            }
                            if (readyToDraw()) {
                                if (!this.mHaveEglContext) {
                                    if (askedToReleaseEglContext) {
                                        askedToReleaseEglContext = GLSurfaceView.LOG_THREADS;
                                    } else {
                                        if (GLSurfaceView.sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                            try {
                                                this.mEglHelper.start();
                                                this.mHaveEglContext = true;
                                                createEglContext = true;
                                                GLSurfaceView.sGLThreadManager.notifyAll();
                                            } catch (RuntimeException t) {
                                                GLSurfaceView.sGLThreadManager.releaseEglContextLocked(this);
                                                throw t;
                                            } catch (Throwable th) {
                                                synchronized (GLSurfaceView.sGLThreadManager) {
                                                }
                                                stopEglSurfaceLocked();
                                                stopEglContextLocked();
                                            }
                                        }
                                    }
                                }
                                if (this.mHaveEglContext && !this.mHaveEglSurface) {
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
                                        this.mSizeChanged = GLSurfaceView.LOG_THREADS;
                                    }
                                    this.mRequestRender = GLSurfaceView.LOG_THREADS;
                                    GLSurfaceView.sGLThreadManager.notifyAll();
                                    if (this.mWantRenderNotification) {
                                        wantRenderNotification = true;
                                    }
                                }
                            }
                            GLSurfaceView.sGLThreadManager.wait();
                        } else {
                            event = (Runnable) this.mEventQueue.remove(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        }
                        if (event != null) {
                            event.run();
                            event = null;
                        } else {
                            if (createEglSurface) {
                                if (this.mEglHelper.createSurface()) {
                                    synchronized (GLSurfaceView.sGLThreadManager) {
                                        this.mFinishedCreatingEglSurface = true;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                        SurfaceCreateTime = System.nanoTime();
                                    }
                                    createEglSurface = GLSurfaceView.LOG_THREADS;
                                } else {
                                    synchronized (GLSurfaceView.sGLThreadManager) {
                                        this.mFinishedCreatingEglSurface = true;
                                        this.mSurfaceIsBad = true;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                    }
                                }
                            }
                            if (createGlInterface) {
                                gl = (GL10) this.mEglHelper.createGL();
                                GLSurfaceView.sGLThreadManager.checkGLDriver(gl);
                                createGlInterface = GLSurfaceView.LOG_THREADS;
                            }
                            if (createEglContext) {
                                view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                                if (view != null) {
                                    Trace.traceBegin(8, "onSurfaceCreated");
                                    view.mRenderer.onSurfaceCreated(gl, this.mEglHelper.mEglConfig);
                                    Trace.traceEnd(8);
                                }
                                createEglContext = GLSurfaceView.LOG_THREADS;
                            }
                            if (sizeChanged) {
                                view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                                if (view != null) {
                                    Trace.traceBegin(8, "onSurfaceChanged");
                                    view.mRenderer.onSurfaceChanged(gl, w, h);
                                    Trace.traceEnd(8);
                                }
                                sizeChanged = GLSurfaceView.LOG_THREADS;
                            }
                            long startNanos = System.nanoTime();
                            view = (GLSurfaceView) this.mGLSurfaceViewWeakRef.get();
                            if (view != null) {
                                Trace.traceBegin(8, "onDrawFrame");
                                view.mRenderer.onDrawFrame(gl);
                                Trace.traceEnd(8);
                            }
                            int swapError = this.mEglHelper.swap();
                            switch (swapError) {
                                case GLES11.GL_CLIP_PLANE0 /*12288*/:
                                    break;
                                case EGL14.EGL_CONTEXT_LOST /*12302*/:
                                    lostEglContext = true;
                                    break;
                                default:
                                    EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", swapError);
                                    synchronized (GLSurfaceView.sGLThreadManager) {
                                        this.mSurfaceIsBad = true;
                                        GLSurfaceView.sGLThreadManager.notifyAll();
                                        break;
                                    }
                            }
                            long nowNanos = System.nanoTime();
                            if (checkskipframe) {
                                long skippedFrames = (nowNanos - startNanos) / GLSurfaceView.VSYNC_SPAN;
                                if (skippedFrames >= 30) {
                                    Trace.traceBegin(8, "jank_event_sync: start_ts=" + startNanos + ",end_ts=" + nowNanos + ", appid=" + mPackageId);
                                    Trace.traceEnd(8);
                                    Jlog.d(311, "#P:" + mPackageName + "#SK:" + skippedFrames + "#IP:0" + "#FRT:" + (startNanos / 10000) + "#DNT:" + (nowNanos / 10000));
                                    Log.i(GLSurfaceView.TAG, "OpenGL:" + mPackageName + " Skipped " + skippedFrames + " frames!");
                                }
                            } else if (nowNanos - SurfaceCreateTime >= 60000000000L) {
                                checkskipframe = true;
                            }
                            if (wantRenderNotification) {
                                doRenderNotification = true;
                                wantRenderNotification = GLSurfaceView.LOG_THREADS;
                            }
                        }
                    }
                }
            }
        }

        public boolean ableToDraw() {
            return (this.mHaveEglContext && this.mHaveEglSurface) ? readyToDraw() : GLSurfaceView.LOG_THREADS;
        }

        private boolean readyToDraw() {
            if (this.mPaused || !this.mHasSurface || this.mSurfaceIsBad || this.mWidth <= 0 || this.mHeight <= 0) {
                return GLSurfaceView.LOG_THREADS;
            }
            if (this.mRequestRender || this.mRenderMode == GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
                return true;
            }
            return GLSurfaceView.LOG_THREADS;
        }

        public void setRenderMode(int renderMode) {
            if (renderMode < 0 || renderMode > GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void requestRenderAndWait() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                if (Thread.currentThread() == this) {
                    return;
                }
                this.mWantRenderNotification = true;
                this.mRequestRender = true;
                this.mRenderComplete = GLSurfaceView.LOG_THREADS;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && !this.mPaused) {
                    if (!this.mRenderComplete && ableToDraw()) {
                        try {
                            GLSurfaceView.sGLThreadManager.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }

        public void surfaceCreated() {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mHasSurface = true;
                this.mFinishedCreatingEglSurface = GLSurfaceView.LOG_THREADS;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (this.mWaitingForSurface && !this.mFinishedCreatingEglSurface) {
                    if (this.mExited) {
                        break;
                    }
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
                this.mHasSurface = GLSurfaceView.LOG_THREADS;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mWaitingForSurface && !this.mExited) {
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
                while (!this.mExited && !this.mPaused) {
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
                this.mRequestPaused = GLSurfaceView.LOG_THREADS;
                this.mRequestRender = true;
                this.mRenderComplete = GLSurfaceView.LOG_THREADS;
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && this.mPaused && !this.mRenderComplete) {
                    try {
                        GLSurfaceView.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onWindowResize(int w, int h) {
            synchronized (GLSurfaceView.sGLThreadManager) {
                this.mWidth = w;
                this.mHeight = h;
                this.mSizeChanged = true;
                this.mRequestRender = true;
                this.mRenderComplete = GLSurfaceView.LOG_THREADS;
                if (Thread.currentThread() == this) {
                    return;
                }
                GLSurfaceView.sGLThreadManager.notifyAll();
                while (!this.mExited && !this.mPaused) {
                    if (!this.mRenderComplete && ableToDraw()) {
                        try {
                            GLSurfaceView.sGLThreadManager.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
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
        private static String TAG = null;
        private static final int kGLES_20 = 131072;
        private static final String kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 ";
        private GLThread mEglOwner;
        private boolean mGLESDriverCheckComplete;
        private int mGLESVersion;
        private boolean mGLESVersionCheckComplete;
        private boolean mLimitedGLESContexts;
        private boolean mMultipleGLESContextsAllowed;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.opengl.GLSurfaceView.GLThreadManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.opengl.GLSurfaceView.GLThreadManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.GLThreadManager.<clinit>():void");
        }

        private GLThreadManager() {
        }

        public synchronized void threadExiting(GLThread thread) {
            thread.mExited = true;
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }
            notifyAll();
        }

        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (this.mEglOwner == thread || this.mEglOwner == null) {
                this.mEglOwner = thread;
                notifyAll();
                return true;
            }
            checkGLESVersion();
            if (this.mMultipleGLESContextsAllowed) {
                return true;
            }
            if (this.mEglOwner != null) {
                this.mEglOwner.requestReleaseEglContextLocked();
            }
            return GLSurfaceView.LOG_THREADS;
        }

        public void releaseEglContextLocked(GLThread thread) {
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            return this.mLimitedGLESContexts;
        }

        public synchronized boolean shouldTerminateEGLWhenPausing() {
            checkGLESVersion();
            return this.mMultipleGLESContextsAllowed ? GLSurfaceView.LOG_THREADS : true;
        }

        public synchronized void checkGLDriver(GL10 gl) {
            boolean z = GLSurfaceView.LOG_THREADS;
            synchronized (this) {
                if (!this.mGLESDriverCheckComplete) {
                    checkGLESVersion();
                    String renderer = gl.glGetString(GLES20.GL_RENDERER);
                    if (this.mGLESVersion < kGLES_20) {
                        boolean z2;
                        if (renderer.startsWith(kMSM7K_RENDERER_PREFIX)) {
                            z2 = GLSurfaceView.LOG_THREADS;
                        } else {
                            z2 = true;
                        }
                        this.mMultipleGLESContextsAllowed = z2;
                        notifyAll();
                    }
                    if (!this.mMultipleGLESContextsAllowed) {
                        z = true;
                    }
                    this.mLimitedGLESContexts = z;
                    this.mGLESDriverCheckComplete = true;
                }
            }
        }

        private void checkGLESVersion() {
            if (!this.mGLESVersionCheckComplete) {
                this.mGLESVersion = SystemProperties.getInt("ro.opengles.version", GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                if (this.mGLESVersion >= kGLES_20) {
                    this.mMultipleGLESContextsAllowed = true;
                }
                this.mGLESVersionCheckComplete = true;
            }
        }
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    static class LogWriter extends Writer {
        private StringBuilder mBuilder;

        LogWriter() {
            this.mBuilder = new StringBuilder();
        }

        public void close() {
            flushBuilder();
        }

        public void flush() {
            flushBuilder();
        }

        public void write(char[] buf, int offset, int count) {
            for (int i = GLSurfaceView.RENDERMODE_WHEN_DIRTY; i < count; i += GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
                char c = buf[offset + i];
                if (c == '\n') {
                    flushBuilder();
                } else {
                    this.mBuilder.append(c);
                }
            }
        }

        private void flushBuilder() {
            if (this.mBuilder.length() > 0) {
                Log.v(GLSurfaceView.TAG, this.mBuilder.toString());
                this.mBuilder.delete(GLSurfaceView.RENDERMODE_WHEN_DIRTY, this.mBuilder.length());
            }
        }
    }

    public interface Renderer {
        void onDrawFrame(GL10 gl10);

        void onSurfaceChanged(GL10 gl10, int i, int i2);

        void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig);
    }

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        final /* synthetic */ GLSurfaceView this$0;

        public SimpleEGLConfigChooser(GLSurfaceView this$0, boolean withDepthBuffer) {
            int i;
            this.this$0 = this$0;
            if (withDepthBuffer) {
                i = 16;
            } else {
                i = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
            }
            super(8, 8, 8, GLSurfaceView.RENDERMODE_WHEN_DIRTY, i, GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.opengl.GLSurfaceView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.opengl.GLSurfaceView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.opengl.GLSurfaceView.<clinit>():void");
    }

    public GLSurfaceView(Context context) {
        super(context);
        this.mThisWeakRef = new WeakReference(this);
        init();
    }

    public GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mThisWeakRef = new WeakReference(this);
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
            this.mEGLConfigChooser = new SimpleEGLConfigChooser(this, true);
        }
        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory();
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
        setEGLConfigChooser(new SimpleEGLConfigChooser(this, needDepth));
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

    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        if (this.mGLThread != null) {
            this.mGLThread.requestRenderAndWait();
        }
    }

    public void onPause() {
        LogPower.push(InformationElement.EID_INTERWORKING, this.mContext.getPackageName());
        this.mGLThread.onPause();
    }

    public void onResume() {
        this.mGLThread.onResume();
        LogPower.push(MediaFile.FILE_TYPE_MS_POWERPOINT, this.mContext.getPackageName());
    }

    public void queueEvent(Runnable r) {
        this.mGLThread.queueEvent(r);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDetached && this.mRenderer != null) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (this.mGLThread != null) {
                renderMode = this.mGLThread.getRenderMode();
            }
            this.mGLThread = new GLThread(this.mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                this.mGLThread.setRenderMode(renderMode);
            }
            this.mGLThread.start();
        }
        this.mDetached = LOG_THREADS;
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

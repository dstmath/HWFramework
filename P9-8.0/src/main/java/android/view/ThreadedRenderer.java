package android.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable.VectorDrawableAnimatorRT;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.util.Log;
import android.view.IGraphicsStatsCallback.Stub;
import android.view.Surface.OutOfResourcesException;
import com.android.internal.R;
import com.android.internal.util.VirtualRefBasePtr;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ThreadedRenderer {
    private static final String CACHE_PATH_SHADERS = "com.android.opengl.shaders_cache";
    public static final String DEBUG_DIRTY_REGIONS_PROPERTY = "debug.hwui.show_dirty_regions";
    public static final String DEBUG_OVERDRAW_PROPERTY = "debug.hwui.overdraw";
    public static final String DEBUG_RENDERER_PROPERTY = "debug.hwui.renderer";
    public static final String DEBUG_SHOW_LAYERS_UPDATES_PROPERTY = "debug.hwui.show_layers_updates";
    public static final String DEBUG_SHOW_NON_RECTANGULAR_CLIP_PROPERTY = "debug.hwui.show_non_rect_clip";
    private static final int FLAG_DUMP_FRAMESTATS = 1;
    private static final int FLAG_DUMP_RESET = 2;
    private static final String LOG_TAG = "ThreadedRenderer";
    public static final String OVERDRAW_PROPERTY_SHOW = "show";
    static final String PRINT_CONFIG_PROPERTY = "debug.hwui.print_config";
    static final String PROFILE_MAXFRAMES_PROPERTY = "debug.hwui.profile.maxframes";
    public static final String PROFILE_PROPERTY = "debug.hwui.profile";
    public static final String PROFILE_PROPERTY_VISUALIZE_BARS = "visual_bars";
    private static final int SYNC_CONTEXT_IS_STOPPED = 4;
    private static final int SYNC_INVALIDATE_REQUIRED = 1;
    private static final int SYNC_LOST_SURFACE_REWARD_IF_FOUND = 2;
    private static final int SYNC_OK = 0;
    private static final String[] VISUALIZERS = new String[]{PROFILE_PROPERTY_VISUALIZE_BARS};
    public static boolean sRendererDisabled = false;
    private static boolean sSupportsOpenGL = nSupportsOpenGL();
    public static boolean sSystemRendererDisabled = false;
    public static boolean sTrimForeground = false;
    private final int mAmbientShadowAlpha;
    private Choreographer mChoreographer;
    private HwCustRenderThreadMonitor mCustMonitor;
    private boolean mEnabled;
    private boolean mHasInsets;
    private int mHeight;
    private boolean mInitialized = false;
    private int mInsetLeft;
    private int mInsetTop;
    private boolean mIsOpaque = false;
    private long[] mJankDrawData = new long[4];
    private final float mLightRadius;
    private final float mLightY;
    private final float mLightZ;
    private long mNativeProxy;
    private boolean mRequested = true;
    private RenderNode mRootNode;
    private boolean mRootNodeNeedsUpdate;
    private final int mSpotShadowAlpha;
    private int mSurfaceHeight;
    private int mSurfaceWidth;
    private int mWidth;

    interface DrawCallbacks {
        void onPostDraw(DisplayListCanvas displayListCanvas);

        void onPreDraw(DisplayListCanvas displayListCanvas);
    }

    private static class ProcessInitializer {
        static ProcessInitializer sInstance = new ProcessInitializer();
        private Context mAppContext;
        private IGraphicsStatsCallback mGraphicsStatsCallback = new Stub() {
            public void onRotateGraphicsStatsBuffer() throws RemoteException {
                ProcessInitializer.this.rotateBuffer();
            }
        };
        private IGraphicsStats mGraphicsStatsService;
        private boolean mInitialized = false;

        private ProcessInitializer() {
        }

        synchronized void init(Context context, long renderProxy) {
            if (!this.mInitialized) {
                this.mInitialized = true;
                this.mAppContext = context.getApplicationContext();
                initSched(context, renderProxy);
                initGraphicsStats();
            }
        }

        private void initSched(Context context, long renderProxy) {
            try {
                ActivityManager.getService().setRenderThread(ThreadedRenderer.nGetRenderThreadTid(renderProxy));
            } catch (Throwable t) {
                Log.w(ThreadedRenderer.LOG_TAG, "Failed to set scheduler for RenderThread", t);
            }
        }

        private void initGraphicsStats() {
            try {
                IBinder binder = ServiceManager.getService("graphicsstats");
                if (binder != null) {
                    this.mGraphicsStatsService = IGraphicsStats.Stub.asInterface(binder);
                    requestBuffer();
                }
            } catch (Throwable t) {
                Log.w(ThreadedRenderer.LOG_TAG, "Could not acquire gfx stats buffer", t);
            }
        }

        private void rotateBuffer() {
            ThreadedRenderer.nRotateProcessStatsBuffer();
            requestBuffer();
        }

        private void requestBuffer() {
            try {
                ParcelFileDescriptor pfd = this.mGraphicsStatsService.requestBufferForProcess(this.mAppContext.getApplicationInfo().packageName, this.mGraphicsStatsCallback);
                ThreadedRenderer.nSetProcessStatsBuffer(pfd.getFd());
                pfd.close();
            } catch (Throwable t) {
                Log.w(ThreadedRenderer.LOG_TAG, "Could not acquire gfx stats buffer", t);
            }
        }
    }

    public static native void disableVsync();

    private static native long nAddFrameMetricsObserver(long j, FrameMetricsObserver frameMetricsObserver);

    private static native void nAddRenderNode(long j, long j2, boolean z);

    private static native void nAllocateBuffers(long j, Surface surface);

    private static native void nBuildLayer(long j, long j2);

    private static native void nCancelLayerUpdate(long j, long j2);

    private static native boolean nCopyLayerInto(long j, long j2, Bitmap bitmap);

    private static native int nCopySurfaceInto(Surface surface, int i, int i2, int i3, int i4, Bitmap bitmap);

    private static native Bitmap nCreateHardwareBitmap(long j, int i, int i2);

    private static native long nCreateProxy(boolean z, long j);

    private static native long nCreateRootRenderNode();

    private static native long nCreateTextureLayer(long j);

    private static native void nDeleteProxy(long j);

    private static native void nDestroy(long j, long j2);

    private static native void nDestroyHardwareResources(long j);

    private static native void nDetachSurfaceTexture(long j, long j2);

    private static native void nDrawRenderNode(long j, long j2);

    private static native void nDumpProfileInfo(long j, FileDescriptor fileDescriptor, int i);

    private static native void nFence(long j);

    private static native int nGetRenderThreadTid(long j);

    private static native void nInitialize(long j, Surface surface);

    private static native void nInvokeFunctor(long j, boolean z);

    private static native boolean nLoadSystemProperties(long j);

    private static native void nNotifyFramePending(long j);

    private static native void nOverrideProperty(String str, String str2);

    private static native boolean nPauseSurface(long j, Surface surface);

    private static native void nPushLayerUpdate(long j, long j2);

    private static native void nRegisterAnimatingRenderNode(long j, long j2);

    private static native void nRegisterVectorDrawableAnimator(long j, long j2);

    private static native void nRemoveFrameMetricsObserver(long j, long j2);

    private static native void nRemoveRenderNode(long j, long j2);

    private static native void nRotateProcessStatsBuffer();

    private static native void nSerializeDisplayListTree(long j);

    private static native void nSetContentDrawBounds(long j, int i, int i2, int i3, int i4);

    private static native void nSetLightCenter(long j, float f, float f2, float f3);

    private static native void nSetName(long j, String str);

    private static native void nSetOpaque(long j, boolean z);

    private static native void nSetProcessStatsBuffer(int i);

    private static native void nSetStopped(long j, boolean z);

    private static native void nSetup(long j, float f, int i, int i2);

    private static native void nStopDrawing(long j);

    private static native boolean nSupportsOpenGL();

    private static native int nSyncAndDrawFrame(long j, long[] jArr, int i);

    private static native void nTrimMemory(int i);

    private static native void nUpdateSurface(long j, Surface surface);

    static native void setupShadersDiskCache(String str);

    public static void disable(boolean system) {
        sRendererDisabled = true;
        if (system) {
            sSystemRendererDisabled = true;
        }
    }

    public static void enableForegroundTrimming() {
        sTrimForeground = true;
    }

    public static boolean isAvailable() {
        return sSupportsOpenGL;
    }

    public static void setupDiskCache(File cacheDir) {
        setupShadersDiskCache(new File(cacheDir, CACHE_PATH_SHADERS).getAbsolutePath());
    }

    public static ThreadedRenderer create(Context context, boolean translucent, String name) {
        if (isAvailable()) {
            return new ThreadedRenderer(context, translucent, name);
        }
        return null;
    }

    public static void trimMemory(int level) {
        nTrimMemory(level);
    }

    public static void overrideProperty(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("name and value must be non-null");
        }
        nOverrideProperty(name, value);
    }

    ThreadedRenderer(Context context, boolean translucent, String name) {
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.Lighting, 0, 0);
        this.mLightY = a.getDimension(3, 0.0f);
        this.mLightZ = a.getDimension(4, 0.0f);
        this.mLightRadius = a.getDimension(2, 0.0f);
        this.mAmbientShadowAlpha = (int) ((a.getFloat(0, 0.0f) * 255.0f) + 0.5f);
        this.mSpotShadowAlpha = (int) ((a.getFloat(1, 0.0f) * 255.0f) + 0.5f);
        a.recycle();
        long rootNodePtr = nCreateRootRenderNode();
        this.mRootNode = RenderNode.adopt(rootNodePtr);
        this.mRootNode.setClipToBounds(false);
        this.mIsOpaque = translucent ^ 1;
        this.mNativeProxy = nCreateProxy(translucent, rootNodePtr);
        nSetName(this.mNativeProxy, name);
        ProcessInitializer.sInstance.init(context, this.mNativeProxy);
        loadSystemProperties();
        if (HwCustRenderThreadMonitor.shouldStartMonitot(context)) {
            this.mCustMonitor = (HwCustRenderThreadMonitor) HwCustUtils.createObj(HwCustRenderThreadMonitor.class, context);
        }
    }

    void destroy() {
        this.mInitialized = false;
        updateEnabledState(null);
        if (this.mCustMonitor != null) {
            this.mCustMonitor.renderMonitorStart(1);
        }
        nDestroy(this.mNativeProxy, this.mRootNode.mNativeRenderNode);
        if (this.mCustMonitor != null) {
            this.mCustMonitor.renderMonitorStop(1);
        }
    }

    boolean isEnabled() {
        return this.mEnabled;
    }

    void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    boolean isRequested() {
        return this.mRequested;
    }

    void setRequested(boolean requested) {
        this.mRequested = requested;
    }

    private void updateEnabledState(Surface surface) {
        if (surface == null || (surface.isValid() ^ 1) != 0) {
            setEnabled(false);
        } else {
            setEnabled(this.mInitialized);
        }
    }

    boolean initialize(Surface surface) throws OutOfResourcesException {
        boolean status = this.mInitialized ^ 1;
        this.mInitialized = true;
        updateEnabledState(surface);
        nInitialize(this.mNativeProxy, surface);
        return status;
    }

    void allocateBuffers(Surface surface) throws OutOfResourcesException {
        nAllocateBuffers(this.mNativeProxy, surface);
    }

    boolean initializeIfNeeded(int width, int height, AttachInfo attachInfo, Surface surface, Rect surfaceInsets) throws OutOfResourcesException {
        if (!isRequested() || isEnabled() || !initialize(surface)) {
            return false;
        }
        setup(width, height, attachInfo, surfaceInsets);
        return true;
    }

    void updateSurface(Surface surface) throws OutOfResourcesException {
        updateEnabledState(surface);
        nUpdateSurface(this.mNativeProxy, surface);
    }

    boolean pauseSurface(Surface surface) {
        return nPauseSurface(this.mNativeProxy, surface);
    }

    void setStopped(boolean stopped) {
        nSetStopped(this.mNativeProxy, stopped);
    }

    void destroyHardwareResources(View view) {
        destroyResources(view);
        nDestroyHardwareResources(this.mNativeProxy);
    }

    private static void destroyResources(View view) {
        view.destroyHardwareResources();
    }

    void detachSurfaceTexture(long hardwareLayer) {
        nDetachSurfaceTexture(this.mNativeProxy, hardwareLayer);
    }

    void setup(int width, int height, AttachInfo attachInfo, Rect surfaceInsets) {
        this.mWidth = width;
        this.mHeight = height;
        if (surfaceInsets == null || (surfaceInsets.left == 0 && surfaceInsets.right == 0 && surfaceInsets.top == 0 && surfaceInsets.bottom == 0)) {
            this.mHasInsets = false;
            this.mInsetLeft = 0;
            this.mInsetTop = 0;
            this.mSurfaceWidth = width;
            this.mSurfaceHeight = height;
        } else {
            this.mHasInsets = true;
            this.mInsetLeft = surfaceInsets.left;
            this.mInsetTop = surfaceInsets.top;
            this.mSurfaceWidth = (this.mInsetLeft + width) + surfaceInsets.right;
            this.mSurfaceHeight = (this.mInsetTop + height) + surfaceInsets.bottom;
            setOpaque(false);
        }
        this.mRootNode.setLeftTopRightBottom(-this.mInsetLeft, -this.mInsetTop, this.mSurfaceWidth, this.mSurfaceHeight);
        nSetup(this.mNativeProxy, this.mLightRadius, this.mAmbientShadowAlpha, this.mSpotShadowAlpha);
        setLightCenter(attachInfo);
    }

    void setLightCenter(AttachInfo attachInfo) {
        Point displaySize = attachInfo.mPoint;
        attachInfo.mDisplay.getRealSize(displaySize);
        nSetLightCenter(this.mNativeProxy, (((float) displaySize.x) / 2.0f) - ((float) attachInfo.mWindowLeft), this.mLightY - ((float) attachInfo.mWindowTop), this.mLightZ);
    }

    void setOpaque(boolean opaque) {
        this.mIsOpaque = opaque ? this.mHasInsets ^ 1 : false;
        nSetOpaque(this.mNativeProxy, this.mIsOpaque);
    }

    boolean isOpaque() {
        return this.mIsOpaque;
    }

    int getWidth() {
        return this.mWidth;
    }

    int getHeight() {
        return this.mHeight;
    }

    long getJankDrawData(int index) {
        if (index < 0 || index > 3 || this.mJankDrawData == null) {
            return -1;
        }
        return this.mJankDrawData[index];
    }

    void dumpGfxInfo(PrintWriter pw, FileDescriptor fd, String[] args) {
        pw.flush();
        int flags = 0;
        for (String str : args) {
            if (str.equals("framestats")) {
                flags |= 1;
            } else if (str.equals("reset")) {
                flags |= 2;
            }
        }
        nDumpProfileInfo(this.mNativeProxy, fd, flags);
    }

    boolean loadSystemProperties() {
        boolean changed = nLoadSystemProperties(this.mNativeProxy);
        if (changed) {
            invalidateRoot();
        }
        return changed;
    }

    private void updateViewTreeDisplayList(View view) {
        view.mPrivateFlags |= 32;
        view.mRecreateDisplayList = (view.mPrivateFlags & Integer.MIN_VALUE) == Integer.MIN_VALUE;
        view.mPrivateFlags &= Integer.MAX_VALUE;
        view.updateDisplayListIfDirty();
        view.mRecreateDisplayList = false;
    }

    private void updateRootDisplayList(View view, DrawCallbacks callbacks) {
        Trace.traceBegin(8, "Record View#draw()");
        updateViewTreeDisplayList(view);
        if (this.mRootNodeNeedsUpdate || (this.mRootNode.isValid() ^ 1) != 0) {
            DisplayListCanvas canvas = this.mRootNode.start(this.mSurfaceWidth, this.mSurfaceHeight);
            try {
                int saveCount = canvas.save();
                canvas.translate((float) this.mInsetLeft, (float) this.mInsetTop);
                callbacks.onPreDraw(canvas);
                canvas.insertReorderBarrier();
                canvas.drawRenderNode(view.updateDisplayListIfDirty());
                canvas.insertInorderBarrier();
                callbacks.onPostDraw(canvas);
                canvas.restoreToCount(saveCount);
                this.mRootNodeNeedsUpdate = false;
            } finally {
                this.mRootNode.end(canvas);
            }
        }
        Trace.traceEnd(8);
    }

    public void addRenderNode(RenderNode node, boolean placeFront) {
        nAddRenderNode(this.mNativeProxy, node.mNativeRenderNode, placeFront);
    }

    public void removeRenderNode(RenderNode node) {
        nRemoveRenderNode(this.mNativeProxy, node.mNativeRenderNode);
    }

    public void drawRenderNode(RenderNode node) {
        nDrawRenderNode(this.mNativeProxy, node.mNativeRenderNode);
    }

    public void setContentDrawBounds(int left, int top, int right, int bottom) {
        nSetContentDrawBounds(this.mNativeProxy, left, top, right, bottom);
    }

    void invalidateRoot() {
        this.mRootNodeNeedsUpdate = true;
    }

    void draw(View view, AttachInfo attachInfo, DrawCallbacks callbacks) {
        attachInfo.mIgnoreDirtyState = true;
        long startdraw = System.nanoTime();
        Choreographer choreographer = attachInfo.mViewRootImpl.mChoreographer;
        choreographer.mFrameInfo.markDrawStart();
        updateRootDisplayList(view, callbacks);
        long step1time = System.nanoTime();
        attachInfo.mIgnoreDirtyState = false;
        if (attachInfo.mPendingAnimatingRenderNodes != null) {
            int count = attachInfo.mPendingAnimatingRenderNodes.size();
            for (int i = 0; i < count; i++) {
                registerAnimatingRenderNode((RenderNode) attachInfo.mPendingAnimatingRenderNodes.get(i));
            }
            attachInfo.mPendingAnimatingRenderNodes.clear();
            attachInfo.mPendingAnimatingRenderNodes = null;
        }
        long[] frameInfo = choreographer.mFrameInfo.mFrameInfo;
        if (this.mCustMonitor != null) {
            this.mCustMonitor.renderMonitorStart(0);
        }
        int syncResult = nSyncAndDrawFrame(this.mNativeProxy, frameInfo, frameInfo.length);
        if (this.mCustMonitor != null) {
            this.mCustMonitor.renderMonitorStop(0);
        }
        if ((syncResult & 2) != 0) {
            setEnabled(false);
            attachInfo.mViewRootImpl.mSurface.release();
            attachInfo.mViewRootImpl.invalidate();
        }
        long step2time = System.nanoTime();
        if ((syncResult & 1) != 0) {
            attachInfo.mViewRootImpl.invalidate();
        }
        long step3time = System.nanoTime();
        this.mJankDrawData[0] = step1time - startdraw;
        this.mJankDrawData[1] = step2time - step1time;
        this.mJankDrawData[2] = step3time - step2time;
        this.mJankDrawData[3] = step3time - startdraw;
    }

    static void invokeFunctor(long functor, boolean waitForCompletion) {
        nInvokeFunctor(functor, waitForCompletion);
    }

    HardwareLayer createTextureLayer() {
        return HardwareLayer.adoptTextureLayer(this, nCreateTextureLayer(this.mNativeProxy));
    }

    void buildLayer(RenderNode node) {
        nBuildLayer(this.mNativeProxy, node.getNativeDisplayList());
    }

    boolean copyLayerInto(HardwareLayer layer, Bitmap bitmap) {
        return nCopyLayerInto(this.mNativeProxy, layer.getDeferredLayerUpdater(), bitmap);
    }

    void pushLayerUpdate(HardwareLayer layer) {
        nPushLayerUpdate(this.mNativeProxy, layer.getDeferredLayerUpdater());
    }

    void onLayerDestroyed(HardwareLayer layer) {
        nCancelLayerUpdate(this.mNativeProxy, layer.getDeferredLayerUpdater());
    }

    void fence() {
        nFence(this.mNativeProxy);
    }

    void stopDrawing() {
        nStopDrawing(this.mNativeProxy);
    }

    public void notifyFramePending() {
        nNotifyFramePending(this.mNativeProxy);
    }

    void registerAnimatingRenderNode(RenderNode animator) {
        nRegisterAnimatingRenderNode(this.mRootNode.mNativeRenderNode, animator.mNativeRenderNode);
    }

    void registerVectorDrawableAnimator(VectorDrawableAnimatorRT animator) {
        nRegisterVectorDrawableAnimator(this.mRootNode.mNativeRenderNode, animator.getAnimatorNativePtr());
    }

    public void serializeDisplayListTree() {
        nSerializeDisplayListTree(this.mNativeProxy);
    }

    public static int copySurfaceInto(Surface surface, Rect srcRect, Bitmap bitmap) {
        if (srcRect == null) {
            return nCopySurfaceInto(surface, 0, 0, 0, 0, bitmap);
        }
        return nCopySurfaceInto(surface, srcRect.left, srcRect.top, srcRect.right, srcRect.bottom, bitmap);
    }

    public static Bitmap createHardwareBitmap(RenderNode node, int width, int height) {
        return nCreateHardwareBitmap(node.getNativeDisplayList(), width, height);
    }

    protected void finalize() throws Throwable {
        try {
            nDeleteProxy(this.mNativeProxy);
            this.mNativeProxy = 0;
        } finally {
            super.finalize();
        }
    }

    void addFrameMetricsObserver(FrameMetricsObserver observer) {
        observer.mNative = new VirtualRefBasePtr(nAddFrameMetricsObserver(this.mNativeProxy, observer));
    }

    void removeFrameMetricsObserver(FrameMetricsObserver observer) {
        nRemoveFrameMetricsObserver(this.mNativeProxy, observer.mNative.get());
        observer.mNative = null;
    }
}

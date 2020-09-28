package android.graphics;

import android.app.ActivityManager;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.TimeUtils;
import android.view.FrameMetricsObserver;
import android.view.IGraphicsStats;
import android.view.IGraphicsStatsCallback;
import android.view.NativeVectorDrawableAnimator;
import android.view.Surface;
import android.view.TextureLayer;
import android.view.animation.AnimationUtils;
import com.android.internal.util.VirtualRefBasePtr;
import java.io.File;
import java.io.FileDescriptor;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;
import sun.misc.Cleaner;

public class HardwareRenderer {
    private static final String CACHE_PATH_SHADERS = "com.android.opengl.shaders_cache";
    private static final String CACHE_PATH_SKIASHADERS = "com.android.skia.shaders_cache";
    public static final int FLAG_DUMP_ALL = 1;
    public static final int FLAG_DUMP_FRAMESTATS = 1;
    public static final int FLAG_DUMP_IGNORE_LIMIT = 128;
    public static final int FLAG_DUMP_RESET = 2;
    private static final String LOG_TAG = "HardwareRenderer";
    public static final int SYNC_CONTEXT_IS_STOPPED = 4;
    public static final int SYNC_FRAME_DROPPED = 8;
    public static final int SYNC_LOST_SURFACE_REWARD_IF_FOUND = 2;
    public static final int SYNC_OK = 0;
    public static final int SYNC_REDRAW_REQUESTED = 1;
    private boolean mForceDark = false;
    private boolean mIsWideGamut = false;
    private final long mNativeProxy;
    private boolean mOpaque = true;
    private FrameRenderRequest mRenderRequest = new FrameRenderRequest();
    protected RenderNode mRootNode = RenderNode.adopt(nCreateRootRenderNode());

    @Retention(RetentionPolicy.SOURCE)
    public @interface DumpFlags {
    }

    public interface FrameCompleteCallback {
        void onFrameComplete(long j);
    }

    public interface FrameDrawingCallback {
        void onFrameDraw(long j);
    }

    public interface PictureCapturedCallback {
        void onPictureCaptured(Picture picture);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncAndDrawResult {
    }

    public static native void disableVsync();

    private static native long nAddFrameMetricsObserver(long j, FrameMetricsObserver frameMetricsObserver);

    private static native void nAddRenderNode(long j, long j2, boolean z);

    private static native void nAllocateBuffers(long j);

    private static native void nBuildLayer(long j, long j2);

    private static native void nCancelLayerUpdate(long j, long j2);

    private static native boolean nCopyLayerInto(long j, long j2, long j3);

    private static native int nCopySurfaceInto(Surface surface, int i, int i2, int i3, int i4, long j);

    private static native Bitmap nCreateHardwareBitmap(long j, int i, int i2);

    private static native long nCreateProxy(boolean z, long j);

    private static native long nCreateRootRenderNode();

    private static native long nCreateTextureLayer(long j);

    /* access modifiers changed from: private */
    public static native void nDeleteProxy(long j);

    private static native void nDestroy(long j, long j2);

    private static native void nDestroyHardwareResources(long j);

    private static native void nDetachSurfaceTexture(long j, long j2);

    private static native void nDrawRenderNode(long j, long j2);

    private static native void nDumpProfileInfo(long j, FileDescriptor fileDescriptor, int i);

    private static native void nFence(long j);

    /* access modifiers changed from: private */
    public static native int nGetRenderThreadTid(long j);

    private static native void nHackySetRTAnimationsEnabled(boolean z);

    private static native void nInvokeFunctor(long j, boolean z);

    private static native boolean nLoadSystemProperties(long j);

    private static native void nNotifyFramePending(long j);

    private static native void nOverrideProperty(String str, String str2);

    private static native boolean nPause(long j);

    private static native void nPushLayerUpdate(long j, long j2);

    private static native void nRegisterAnimatingRenderNode(long j, long j2);

    private static native void nRegisterVectorDrawableAnimator(long j, long j2);

    private static native void nRemoveFrameMetricsObserver(long j, long j2);

    private static native void nRemoveRenderNode(long j, long j2);

    /* access modifiers changed from: private */
    public static native void nRotateProcessStatsBuffer();

    private static native void nSetContentDrawBounds(long j, int i, int i2, int i3, int i4);

    private static native void nSetContextPriority(int i);

    private static native void nSetDebuggingEnabled(boolean z);

    private static native void nSetForceDark(long j, boolean z);

    private static native void nSetFrameCallback(long j, FrameDrawingCallback frameDrawingCallback);

    private static native void nSetFrameCompleteCallback(long j, FrameCompleteCallback frameCompleteCallback);

    private static native void nSetHighContrastText(boolean z);

    private static native void nSetIsolatedProcess(boolean z);

    private static native void nSetLightAlpha(long j, float f, float f2);

    private static native void nSetLightGeometry(long j, float f, float f2, float f3, float f4);

    private static native void nSetName(long j, String str);

    private static native void nSetOpaque(long j, boolean z);

    private static native void nSetPictureCaptureCallback(long j, PictureCapturedCallback pictureCapturedCallback);

    /* access modifiers changed from: private */
    public static native void nSetProcessStatsBuffer(int i);

    private static native void nSetStopped(long j, boolean z);

    private static native void nSetSurface(long j, Surface surface);

    private static native void nSetWideGamut(long j, boolean z);

    private static native void nStopDrawing(long j);

    private static native int nSyncAndDrawFrame(long j, long[] jArr, int i);

    private static native void nTrimMemory(int i);

    public static native void preload();

    protected static native void setupShadersDiskCache(String str, String str2);

    public HardwareRenderer() {
        this.mRootNode.setClipToBounds(false);
        this.mNativeProxy = nCreateProxy(true ^ this.mOpaque, this.mRootNode.mNativeRenderNode);
        long j = this.mNativeProxy;
        if (j != 0) {
            Cleaner.create(this, new DestroyContextRunnable(j));
            ProcessInitializer.sInstance.init(this.mNativeProxy);
            return;
        }
        throw new OutOfMemoryError("Unable to create hardware renderer");
    }

    public void destroy() {
        nDestroy(this.mNativeProxy, this.mRootNode.mNativeRenderNode);
    }

    public void setName(String name) {
        nSetName(this.mNativeProxy, name);
    }

    public void setLightSourceGeometry(float lightX, float lightY, float lightZ, float lightRadius) {
        validateFinite(lightX, "lightX");
        validateFinite(lightY, "lightY");
        validatePositive(lightZ, "lightZ");
        validatePositive(lightRadius, "lightRadius");
        nSetLightGeometry(this.mNativeProxy, lightX, lightY, lightZ, lightRadius);
    }

    public void setLightSourceAlpha(float ambientShadowAlpha, float spotShadowAlpha) {
        validateAlpha(ambientShadowAlpha, "ambientShadowAlpha");
        validateAlpha(spotShadowAlpha, "spotShadowAlpha");
        nSetLightAlpha(this.mNativeProxy, ambientShadowAlpha, spotShadowAlpha);
    }

    public void setContentRoot(RenderNode content) {
        RecordingCanvas canvas = this.mRootNode.beginRecording();
        if (content != null) {
            canvas.drawRenderNode(content);
        }
        this.mRootNode.endRecording();
    }

    public void setSurface(Surface surface) {
        if (surface == null || surface.isValid()) {
            nSetSurface(this.mNativeProxy, surface);
            return;
        }
        throw new IllegalArgumentException("Surface is invalid. surface.isValid() == false.");
    }

    public final class FrameRenderRequest {
        private FrameInfo mFrameInfo;
        private boolean mWaitForPresent;

        private FrameRenderRequest() {
            this.mFrameInfo = new FrameInfo();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void reset() {
            this.mWaitForPresent = false;
            HardwareRenderer.this.mRenderRequest.setVsyncTime(AnimationUtils.currentAnimationTimeMillis() * TimeUtils.NANOS_PER_MS);
        }

        public void setFrameInfo(FrameInfo info) {
            System.arraycopy(info.frameInfo, 0, this.mFrameInfo.frameInfo, 0, info.frameInfo.length);
        }

        public FrameRenderRequest setVsyncTime(long vsyncTime) {
            this.mFrameInfo.setVsync(vsyncTime, vsyncTime);
            this.mFrameInfo.addFlags(4);
            return this;
        }

        public FrameRenderRequest setFrameCommitCallback(Executor executor, Runnable frameCommitCallback) {
            HardwareRenderer.this.setFrameCompleteCallback(new FrameCompleteCallback(executor, frameCommitCallback) {
                /* class android.graphics.$$Lambda$HardwareRenderer$FrameRenderRequest$dejdYejpuxp3nc7eP6FZ2zBu778 */
                private final /* synthetic */ Executor f$0;
                private final /* synthetic */ Runnable f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // android.graphics.HardwareRenderer.FrameCompleteCallback
                public final void onFrameComplete(long j) {
                    this.f$0.execute(this.f$1);
                }
            });
            return this;
        }

        public FrameRenderRequest setWaitForPresent(boolean shouldWait) {
            this.mWaitForPresent = shouldWait;
            return this;
        }

        public int syncAndDraw() {
            int syncResult = HardwareRenderer.this.syncAndDrawFrame(this.mFrameInfo);
            if (this.mWaitForPresent && (syncResult & 8) == 0) {
                HardwareRenderer.this.fence();
            }
            return syncResult;
        }
    }

    public FrameRenderRequest createRenderRequest() {
        this.mRenderRequest.reset();
        return this.mRenderRequest;
    }

    public int syncAndDrawFrame(FrameInfo frameInfo) {
        return nSyncAndDrawFrame(this.mNativeProxy, frameInfo.frameInfo, frameInfo.frameInfo.length);
    }

    public boolean pause() {
        return nPause(this.mNativeProxy);
    }

    public void setStopped(boolean stopped) {
        nSetStopped(this.mNativeProxy, stopped);
    }

    public void stop() {
        nSetStopped(this.mNativeProxy, true);
    }

    public void start() {
        nSetStopped(this.mNativeProxy, false);
    }

    public void clearContent() {
        nDestroyHardwareResources(this.mNativeProxy);
    }

    public boolean setForceDark(boolean enable) {
        if (this.mForceDark == enable) {
            return false;
        }
        this.mForceDark = enable;
        nSetForceDark(this.mNativeProxy, enable);
        return true;
    }

    public void allocateBuffers() {
        nAllocateBuffers(this.mNativeProxy);
    }

    public void notifyFramePending() {
        nNotifyFramePending(this.mNativeProxy);
    }

    public void setOpaque(boolean opaque) {
        if (this.mOpaque != opaque) {
            this.mOpaque = opaque;
            nSetOpaque(this.mNativeProxy, this.mOpaque);
        }
    }

    public boolean isOpaque() {
        return this.mOpaque;
    }

    public void setFrameCompleteCallback(FrameCompleteCallback callback) {
        nSetFrameCompleteCallback(this.mNativeProxy, callback);
    }

    public void addFrameMetricsObserver(FrameMetricsObserver observer) {
        observer.mNative = new VirtualRefBasePtr(nAddFrameMetricsObserver(this.mNativeProxy, observer));
    }

    public void removeFrameMetricsObserver(FrameMetricsObserver observer) {
        nRemoveFrameMetricsObserver(this.mNativeProxy, observer.mNative.get());
        observer.mNative = null;
    }

    public void setWideGamut(boolean wideGamut) {
        this.mIsWideGamut = wideGamut;
        nSetWideGamut(this.mNativeProxy, wideGamut);
    }

    public void fence() {
        nFence(this.mNativeProxy);
    }

    public void registerAnimatingRenderNode(RenderNode animator) {
        nRegisterAnimatingRenderNode(this.mRootNode.mNativeRenderNode, animator.mNativeRenderNode);
    }

    public void registerVectorDrawableAnimator(NativeVectorDrawableAnimator animator) {
        nRegisterVectorDrawableAnimator(this.mRootNode.mNativeRenderNode, animator.getAnimatorNativePtr());
    }

    public void stopDrawing() {
        nStopDrawing(this.mNativeProxy);
    }

    public TextureLayer createTextureLayer() {
        return TextureLayer.adoptTextureLayer(this, nCreateTextureLayer(this.mNativeProxy));
    }

    public void detachSurfaceTexture(long hardwareLayer) {
        nDetachSurfaceTexture(this.mNativeProxy, hardwareLayer);
    }

    public void buildLayer(RenderNode node) {
        if (node.hasDisplayList()) {
            nBuildLayer(this.mNativeProxy, node.mNativeRenderNode);
        }
    }

    public boolean copyLayerInto(TextureLayer layer, Bitmap bitmap) {
        return nCopyLayerInto(this.mNativeProxy, layer.getDeferredLayerUpdater(), bitmap.getNativeInstance());
    }

    public void pushLayerUpdate(TextureLayer layer) {
        nPushLayerUpdate(this.mNativeProxy, layer.getDeferredLayerUpdater());
    }

    public void onLayerDestroyed(TextureLayer layer) {
        nCancelLayerUpdate(this.mNativeProxy, layer.getDeferredLayerUpdater());
    }

    public void setFrameCallback(FrameDrawingCallback callback) {
        nSetFrameCallback(this.mNativeProxy, callback);
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

    public boolean loadSystemProperties() {
        return nLoadSystemProperties(this.mNativeProxy);
    }

    public void dumpProfileInfo(FileDescriptor fd, int dumpFlags) {
        nDumpProfileInfo(this.mNativeProxy, fd, dumpFlags);
    }

    public void setContentDrawBounds(int left, int top, int right, int bottom) {
        nSetContentDrawBounds(this.mNativeProxy, left, top, right, bottom);
    }

    public void setPictureCaptureCallback(PictureCapturedCallback callback) {
        nSetPictureCaptureCallback(this.mNativeProxy, callback);
    }

    public boolean isWideGamut() {
        return this.mIsWideGamut;
    }

    static void invokePictureCapturedCallback(long picturePtr, PictureCapturedCallback callback) {
        callback.onPictureCaptured(new Picture(picturePtr));
    }

    private static void validateAlpha(float alpha, String argumentName) {
        if (alpha < 0.0f || alpha > 1.0f) {
            throw new IllegalArgumentException(argumentName + " must be a valid alpha, " + alpha + " is not in the range of 0.0f to 1.0f");
        }
    }

    private static void validatePositive(float f, String argumentName) {
        if (!Float.isFinite(f) || f < 0.0f) {
            throw new IllegalArgumentException(argumentName + " must be a finite positive, given=" + f);
        }
    }

    private static void validateFinite(float f, String argumentName) {
        if (!Float.isFinite(f)) {
            throw new IllegalArgumentException(argumentName + " must be finite, given=" + f);
        }
    }

    public static void invokeFunctor(long functor, boolean waitForCompletion) {
        nInvokeFunctor(functor, waitForCompletion);
    }

    public static void setFPSDivisor(int divisor) {
        boolean z = true;
        if (divisor > 1) {
            z = false;
        }
        nHackySetRTAnimationsEnabled(z);
    }

    public static void setContextPriority(int priority) {
        nSetContextPriority(priority);
    }

    public static void setHighContrastText(boolean highContrastText) {
        nSetHighContrastText(highContrastText);
    }

    public static void setIsolatedProcess(boolean isIsolated) {
        nSetIsolatedProcess(isIsolated);
    }

    public static void setDebuggingEnabled(boolean enable) {
        nSetDebuggingEnabled(enable);
    }

    public static int copySurfaceInto(Surface surface, Rect srcRect, Bitmap bitmap) {
        if (srcRect == null) {
            return nCopySurfaceInto(surface, 0, 0, 0, 0, bitmap.getNativeInstance());
        }
        return nCopySurfaceInto(surface, srcRect.left, srcRect.top, srcRect.right, srcRect.bottom, bitmap.getNativeInstance());
    }

    public static Bitmap createHardwareBitmap(RenderNode node, int width, int height) {
        return nCreateHardwareBitmap(node.mNativeRenderNode, width, height);
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

    public static void setupDiskCache(File cacheDir) {
        setupShadersDiskCache(new File(cacheDir, CACHE_PATH_SHADERS).getAbsolutePath(), new File(cacheDir, CACHE_PATH_SKIASHADERS).getAbsolutePath());
    }

    public static void setPackageName(String packageName) {
        ProcessInitializer.sInstance.setPackageName(packageName);
    }

    private static final class DestroyContextRunnable implements Runnable {
        private final long mNativeInstance;

        DestroyContextRunnable(long nativeInstance) {
            this.mNativeInstance = nativeInstance;
        }

        public void run() {
            HardwareRenderer.nDeleteProxy(this.mNativeInstance);
        }
    }

    /* access modifiers changed from: private */
    public static class ProcessInitializer {
        static ProcessInitializer sInstance = new ProcessInitializer();
        private IGraphicsStatsCallback mGraphicsStatsCallback = new IGraphicsStatsCallback.Stub() {
            /* class android.graphics.HardwareRenderer.ProcessInitializer.AnonymousClass1 */

            @Override // android.view.IGraphicsStatsCallback
            public void onRotateGraphicsStatsBuffer() throws RemoteException {
                ProcessInitializer.this.rotateBuffer();
            }
        };
        private IGraphicsStats mGraphicsStatsService;
        private boolean mInitialized = false;
        private String mPackageName;

        private ProcessInitializer() {
        }

        /* access modifiers changed from: package-private */
        public synchronized void setPackageName(String name) {
            if (!this.mInitialized) {
                this.mPackageName = name;
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void init(long renderProxy) {
            if (!this.mInitialized) {
                this.mInitialized = true;
                initSched(renderProxy);
                initGraphicsStats();
            }
        }

        private void initSched(long renderProxy) {
            try {
                ActivityManager.getService().setRenderThread(HardwareRenderer.nGetRenderThreadTid(renderProxy));
            } catch (Throwable t) {
                Log.w(HardwareRenderer.LOG_TAG, "Failed to set scheduler for RenderThread", t);
            }
        }

        private void initGraphicsStats() {
            if (this.mPackageName != null) {
                try {
                    IBinder binder = ServiceManager.getService("graphicsstats");
                    if (binder != null) {
                        this.mGraphicsStatsService = IGraphicsStats.Stub.asInterface(binder);
                        requestBuffer();
                    }
                } catch (Throwable t) {
                    Log.w(HardwareRenderer.LOG_TAG, "Could not acquire gfx stats buffer", t);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void rotateBuffer() {
            HardwareRenderer.nRotateProcessStatsBuffer();
            requestBuffer();
        }

        private void requestBuffer() {
            try {
                ParcelFileDescriptor pfd = this.mGraphicsStatsService.requestBufferForProcess(this.mPackageName, this.mGraphicsStatsCallback);
                HardwareRenderer.nSetProcessStatsBuffer(pfd.getFd());
                pfd.close();
            } catch (Throwable t) {
                Log.w(HardwareRenderer.LOG_TAG, "Could not acquire gfx stats buffer", t);
            }
        }
    }
}

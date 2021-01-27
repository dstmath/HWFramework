package android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.HardwareRenderer;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.TimeUtils;
import android.view.Surface;
import android.view.View;
import android.view.animation.AnimationUtils;
import com.android.internal.R;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public final class ThreadedRenderer extends HardwareRenderer {
    public static final String DEBUG_DIRTY_REGIONS_PROPERTY = "debug.hwui.show_dirty_regions";
    public static final String DEBUG_FORCE_DARK = "debug.hwui.force_dark";
    public static final String DEBUG_FPS_DIVISOR = "debug.hwui.fps_divisor";
    public static final String DEBUG_OVERDRAW_PROPERTY = "debug.hwui.overdraw";
    public static final String DEBUG_SHOW_LAYERS_UPDATES_PROPERTY = "debug.hwui.show_layers_updates";
    public static final String DEBUG_SHOW_NON_RECTANGULAR_CLIP_PROPERTY = "debug.hwui.show_non_rect_clip";
    public static int EGL_CONTEXT_PRIORITY_HIGH_IMG = 12545;
    public static int EGL_CONTEXT_PRIORITY_LOW_IMG = 12547;
    public static int EGL_CONTEXT_PRIORITY_MEDIUM_IMG = 12546;
    public static final String OVERDRAW_PROPERTY_SHOW = "show";
    static final String PRINT_CONFIG_PROPERTY = "debug.hwui.print_config";
    static final String PROFILE_MAXFRAMES_PROPERTY = "debug.hwui.profile.maxframes";
    public static final String PROFILE_PROPERTY = "debug.hwui.profile";
    public static final String PROFILE_PROPERTY_VISUALIZE_BARS = "visual_bars";
    private static final String[] VISUALIZERS = {PROFILE_PROPERTY_VISUALIZE_BARS};
    public static boolean sRendererDisabled = false;
    private static Boolean sSupportsOpenGL;
    public static boolean sSystemRendererDisabled = false;
    public static boolean sTrimForeground = false;
    private boolean mEnabled;
    private boolean mHasInsets;
    private int mHeight;
    private boolean mInitialized = false;
    private int mInsetLeft;
    private int mInsetTop;
    private long[] mJankDrawData = new long[4];
    private final float mLightRadius;
    private final float mLightY;
    private final float mLightZ;
    private HardwareRenderer.FrameDrawingCallback mNextRtFrameCallback;
    private boolean mRequested = true;
    private boolean mRootNodeNeedsUpdate;
    private int mSurfaceHeight;
    private int mSurfaceWidth;
    private int mWidth;

    /* access modifiers changed from: package-private */
    public interface DrawCallbacks {
        void onPostDraw(RecordingCanvas recordingCanvas);

        void onPreDraw(RecordingCanvas recordingCanvas);
    }

    static {
        isAvailable();
    }

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
        Boolean bool = sSupportsOpenGL;
        if (bool != null) {
            return bool.booleanValue();
        }
        boolean z = false;
        if (SystemProperties.getInt("ro.kernel.qemu", 0) == 0) {
            sSupportsOpenGL = true;
            return true;
        }
        int qemu_gles = SystemProperties.getInt("qemu.gles", -1);
        if (qemu_gles == -1) {
            return false;
        }
        if (qemu_gles > 0) {
            z = true;
        }
        sSupportsOpenGL = Boolean.valueOf(z);
        return sSupportsOpenGL.booleanValue();
    }

    public static ThreadedRenderer create(Context context, boolean translucent, String name) {
        if (isAvailable()) {
            return new ThreadedRenderer(context, translucent, name);
        }
        return null;
    }

    ThreadedRenderer(Context context, boolean translucent, String name) {
        setName(name);
        setOpaque(!translucent);
        TypedArray a = context.obtainStyledAttributes(null, R.styleable.Lighting, 0, 0);
        this.mLightY = a.getDimension(3, 0.0f);
        this.mLightZ = a.getDimension(4, 0.0f);
        this.mLightRadius = a.getDimension(2, 0.0f);
        float ambientShadowAlpha = a.getFloat(0, 0.0f);
        float spotShadowAlpha = a.getFloat(1, 0.0f);
        a.recycle();
        setLightSourceAlpha(ambientShadowAlpha, spotShadowAlpha);
    }

    @Override // android.graphics.HardwareRenderer
    public void destroy() {
        this.mInitialized = false;
        updateEnabledState(null);
        super.destroy();
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        return this.mEnabled;
    }

    /* access modifiers changed from: package-private */
    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    /* access modifiers changed from: package-private */
    public boolean isRequested() {
        return this.mRequested;
    }

    /* access modifiers changed from: package-private */
    public void setRequested(boolean requested) {
        this.mRequested = requested;
    }

    private void updateEnabledState(Surface surface) {
        if (surface == null || !surface.isValid()) {
            setEnabled(false);
        } else {
            setEnabled(this.mInitialized);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean initialize(Surface surface) throws Surface.OutOfResourcesException {
        boolean status = !this.mInitialized;
        this.mInitialized = true;
        updateEnabledState(surface);
        setSurface(surface);
        return status;
    }

    /* access modifiers changed from: package-private */
    public boolean initializeIfNeeded(int width, int height, View.AttachInfo attachInfo, Surface surface, Rect surfaceInsets) throws Surface.OutOfResourcesException {
        if (!isRequested() || isEnabled() || !initialize(surface)) {
            return false;
        }
        setup(width, height, attachInfo, surfaceInsets);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateSurface(Surface surface) throws Surface.OutOfResourcesException {
        updateEnabledState(surface);
        setSurface(surface);
    }

    @Override // android.graphics.HardwareRenderer
    public void setSurface(Surface surface) {
        if (surface == null || !surface.isValid()) {
            super.setSurface(null);
        } else {
            super.setSurface(surface);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerRtFrameCallback(HardwareRenderer.FrameDrawingCallback callback) {
        this.mNextRtFrameCallback = callback;
    }

    /* access modifiers changed from: package-private */
    public void destroyHardwareResources(View view) {
        destroyResources(view);
        clearContent();
    }

    private static void destroyResources(View view) {
        view.destroyHardwareResources();
    }

    /* access modifiers changed from: package-private */
    public void setup(int width, int height, View.AttachInfo attachInfo, Rect surfaceInsets) {
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
            this.mSurfaceWidth = this.mInsetLeft + width + surfaceInsets.right;
            this.mSurfaceHeight = this.mInsetTop + height + surfaceInsets.bottom;
            setOpaque(false);
        }
        this.mRootNode.setLeftTopRightBottom(-this.mInsetLeft, -this.mInsetTop, this.mSurfaceWidth, this.mSurfaceHeight);
        setLightCenter(attachInfo);
    }

    /* access modifiers changed from: package-private */
    public void setLightCenter(View.AttachInfo attachInfo) {
        Point displaySize = attachInfo.mPoint;
        attachInfo.mDisplay.getRealSize(displaySize);
        setLightSourceGeometry((((float) displaySize.x) / 2.0f) - ((float) attachInfo.mWindowLeft), this.mLightY - ((float) attachInfo.mWindowTop), this.mLightZ, this.mLightRadius);
    }

    /* access modifiers changed from: package-private */
    public int getWidth() {
        return this.mWidth;
    }

    /* access modifiers changed from: package-private */
    public int getHeight() {
        return this.mHeight;
    }

    /* access modifiers changed from: package-private */
    public long[] getJankDrawData() {
        return this.mJankDrawData;
    }

    /* access modifiers changed from: package-private */
    public void dumpGfxInfo(PrintWriter pw, FileDescriptor fd, String[] args) {
        pw.flush();
        int flags = (args == null || args.length == 0) ? 1 : 0;
        for (String str : args) {
            char c = 65535;
            int hashCode = str.hashCode();
            if (hashCode != -252053678) {
                if (hashCode != 1492) {
                    if (hashCode != 108404047) {
                        if (hashCode == 1550262569 && str.equals("ignorelimit")) {
                            c = 3;
                        }
                    } else if (str.equals("reset")) {
                        c = 1;
                    }
                } else if (str.equals("-a")) {
                    c = 2;
                }
            } else if (str.equals("framestats")) {
                c = 0;
            }
            if (c == 0) {
                flags |= 1;
            } else if (c == 1) {
                flags |= 2;
            } else if (c == 2) {
                flags = 1;
            } else if (c == 3) {
                flags = 128;
            }
        }
        dumpProfileInfo(fd, flags);
    }

    /* access modifiers changed from: package-private */
    public Picture captureRenderingCommands() {
        return null;
    }

    @Override // android.graphics.HardwareRenderer
    public boolean loadSystemProperties() {
        boolean changed = super.loadSystemProperties();
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
        HardwareRenderer.FrameDrawingCallback callback = this.mNextRtFrameCallback;
        this.mNextRtFrameCallback = null;
        if (callback != null) {
            setFrameCallback(callback);
        }
        if (this.mRootNodeNeedsUpdate || !this.mRootNode.hasDisplayList()) {
            RecordingCanvas canvas = this.mRootNode.beginRecording(this.mSurfaceWidth, this.mSurfaceHeight);
            try {
                int saveCount = canvas.save();
                canvas.translate((float) this.mInsetLeft, (float) this.mInsetTop);
                callbacks.onPreDraw(canvas);
                canvas.enableZ();
                canvas.drawRenderNode(view.updateDisplayListIfDirty());
                canvas.disableZ();
                callbacks.onPostDraw(canvas);
                canvas.restoreToCount(saveCount);
                this.mRootNodeNeedsUpdate = false;
            } finally {
                this.mRootNode.endRecording();
            }
        }
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: package-private */
    public void invalidateRoot() {
        this.mRootNodeNeedsUpdate = true;
    }

    /* access modifiers changed from: package-private */
    public void draw(View view, View.AttachInfo attachInfo, DrawCallbacks callbacks) {
        long startdraw = System.nanoTime();
        Choreographer choreographer = attachInfo.mViewRootImpl.mChoreographer;
        choreographer.mFrameInfo.markDrawStart();
        updateRootDisplayList(view, callbacks);
        long step1time = System.nanoTime();
        if (attachInfo.mPendingAnimatingRenderNodes != null) {
            int count = attachInfo.mPendingAnimatingRenderNodes.size();
            for (int i = 0; i < count; i++) {
                registerAnimatingRenderNode(attachInfo.mPendingAnimatingRenderNodes.get(i));
            }
            attachInfo.mPendingAnimatingRenderNodes.clear();
            attachInfo.mPendingAnimatingRenderNodes = null;
        }
        if (attachInfo.mViewRootImpl.mHwBlurWindowManager != null) {
            attachInfo.mViewRootImpl.mHwBlurWindowManager.performDrawBlurLayer(attachInfo.mViewRootImpl, attachInfo.mViewRootImpl.mView);
        }
        int syncResult = syncAndDrawFrame(choreographer.mFrameInfo);
        if (attachInfo.mViewRootImpl.mHwBlurWindowManager != null) {
            attachInfo.mViewRootImpl.mHwBlurWindowManager.updateWindowBlurDrawOp(attachInfo.mViewRootImpl, true);
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
        long[] jArr = this.mJankDrawData;
        jArr[0] = step1time - startdraw;
        jArr[1] = step2time - step1time;
        jArr[2] = step3time - step2time;
        jArr[3] = step3time - startdraw;
    }

    public RenderNode getRootNode() {
        return this.mRootNode;
    }

    public static class SimpleRenderer extends HardwareRenderer {
        private final float mLightRadius;
        private final float mLightY;
        private final float mLightZ;

        public SimpleRenderer(Context context, String name, Surface surface) {
            setName(name);
            setOpaque(false);
            setSurface(surface);
            TypedArray a = context.obtainStyledAttributes(null, R.styleable.Lighting, 0, 0);
            this.mLightY = a.getDimension(3, 0.0f);
            this.mLightZ = a.getDimension(4, 0.0f);
            this.mLightRadius = a.getDimension(2, 0.0f);
            float ambientShadowAlpha = a.getFloat(0, 0.0f);
            float spotShadowAlpha = a.getFloat(1, 0.0f);
            a.recycle();
            setLightSourceAlpha(ambientShadowAlpha, spotShadowAlpha);
        }

        public void setLightCenter(Display display, int windowLeft, int windowTop) {
            Point displaySize = new Point();
            display.getRealSize(displaySize);
            setLightSourceGeometry((((float) displaySize.x) / 2.0f) - ((float) windowLeft), this.mLightY - ((float) windowTop), this.mLightZ, this.mLightRadius);
        }

        public RenderNode getRootNode() {
            return this.mRootNode;
        }

        public void draw(HardwareRenderer.FrameDrawingCallback callback) {
            long vsync = AnimationUtils.currentAnimationTimeMillis() * TimeUtils.NANOS_PER_MS;
            if (callback != null) {
                setFrameCallback(callback);
            }
            createRenderRequest().setVsyncTime(vsync).syncAndDraw();
        }
    }
}

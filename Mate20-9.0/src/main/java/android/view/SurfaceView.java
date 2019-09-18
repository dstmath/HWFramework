package android.view;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import com.android.internal.view.SurfaceCallbackHelper;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class SurfaceView extends View implements ViewRootImpl.WindowStoppedCallback {
    private static final boolean DEBUG = false;
    private static final String GALLERY = "com.android.gallery3d";
    private static final String TAG = "SurfaceView";
    private boolean mAttachedToWindow;
    final ArrayList<SurfaceHolder.Callback> mCallbacks;
    final Configuration mConfiguration;
    SurfaceControl mDeferredDestroySurfaceControl;
    boolean mDelayDestory;
    boolean mDrawFinished;
    private final ViewTreeObserver.OnPreDrawListener mDrawListener;
    boolean mDrawingStopped;
    int mFormat;
    boolean mFromStopped;
    private boolean mGlobalListenersAdded;
    boolean mHaveFrame;
    boolean mIsCreating;
    long mLastLockTime;
    int mLastSurfaceHeight;
    int mLastSurfaceWidth;
    boolean mLastWindowVisibility;
    final int[] mLocation;
    private int mPendingReportDraws;
    private Rect mRTLastReportedPosition;
    int mRequestedFormat;
    int mRequestedHeight;
    boolean mRequestedVisible;
    int mRequestedWidth;
    private volatile boolean mRtHandlingPositionUpdates;
    private SurfaceControl.Transaction mRtTransaction;
    final Rect mScreenRect;
    private final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener;
    int mSubLayer;
    final Surface mSurface;
    SurfaceControlWithBackground mSurfaceControl;
    boolean mSurfaceCreated;
    private int mSurfaceFlags;
    final Rect mSurfaceFrame;
    int mSurfaceHeight;
    private final SurfaceHolder mSurfaceHolder;
    final ReentrantLock mSurfaceLock;
    SurfaceSession mSurfaceSession;
    int mSurfaceWidth;
    final Rect mTmpRect;
    private CompatibilityInfo.Translator mTranslator;
    boolean mViewVisibility;
    boolean mVisible;
    boolean mVisiblityChangeState;
    int mWindowSpaceLeft;
    int mWindowSpaceTop;
    boolean mWindowStopped;
    boolean mWindowVisibility;
    private Object sObjectLock;

    class SurfaceControlWithBackground extends SurfaceControl {
        SurfaceControl mBackgroundControl;
        private boolean mOpaque = true;
        public boolean mVisible = false;

        public SurfaceControlWithBackground(String name, boolean opaque, SurfaceControl.Builder b) throws Exception {
            super(b.setName(name).build());
            this.mBackgroundControl = b.setName("Background for -" + name).setFormat(1024).setColorLayer(true).build();
            this.mOpaque = opaque;
        }

        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
            this.mBackgroundControl.setAlpha(alpha);
        }

        public void setLayer(int zorder) {
            super.setLayer(zorder);
            this.mBackgroundControl.setLayer(-3);
        }

        public void setPosition(float x, float y) {
            super.setPosition(x, y);
            this.mBackgroundControl.setPosition(x, y);
        }

        public void setSize(int w, int h) {
            super.setSize(w, h);
            this.mBackgroundControl.setSize(w, h);
        }

        public void setWindowCrop(Rect crop) {
            super.setWindowCrop(crop);
            this.mBackgroundControl.setWindowCrop(crop);
        }

        public void setFinalCrop(Rect crop) {
            super.setFinalCrop(crop);
            this.mBackgroundControl.setFinalCrop(crop);
        }

        public void setLayerStack(int layerStack) {
            super.setLayerStack(layerStack);
            this.mBackgroundControl.setLayerStack(layerStack);
        }

        public void setOpaque(boolean isOpaque) {
            super.setOpaque(isOpaque);
            this.mOpaque = isOpaque;
            updateBackgroundVisibility();
        }

        public void setSecure(boolean isSecure) {
            super.setSecure(isSecure);
        }

        public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
            super.setMatrix(dsdx, dtdx, dsdy, dtdy);
            this.mBackgroundControl.setMatrix(dsdx, dtdx, dsdy, dtdy);
        }

        public void hide() {
            super.hide();
            this.mVisible = false;
            updateBackgroundVisibility();
        }

        public void show() {
            super.show();
            this.mVisible = true;
            updateBackgroundVisibility();
        }

        public void destroy() {
            super.destroy();
            this.mBackgroundControl.destroy();
        }

        public void release() {
            super.release();
            this.mBackgroundControl.release();
        }

        public void setTransparentRegionHint(Region region) {
            super.setTransparentRegionHint(region);
            this.mBackgroundControl.setTransparentRegionHint(region);
        }

        public void deferTransactionUntil(IBinder handle, long frame) {
            super.deferTransactionUntil(handle, frame);
            this.mBackgroundControl.deferTransactionUntil(handle, frame);
        }

        public void deferTransactionUntil(Surface barrier, long frame) {
            super.deferTransactionUntil(barrier, frame);
            this.mBackgroundControl.deferTransactionUntil(barrier, frame);
        }

        /* access modifiers changed from: private */
        public void setBackgroundColor(int bgColor) {
            float[] colorComponents = {((float) Color.red(bgColor)) / 255.0f, ((float) Color.green(bgColor)) / 255.0f, ((float) Color.blue(bgColor)) / 255.0f};
            SurfaceControl.openTransaction();
            try {
                this.mBackgroundControl.setColor(colorComponents);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }

        /* access modifiers changed from: package-private */
        public void updateBackgroundVisibility() {
            if (!this.mOpaque || !this.mVisible) {
                this.mBackgroundControl.hide();
            } else {
                this.mBackgroundControl.show();
            }
        }
    }

    public SurfaceView(Context context) {
        this(context, null);
    }

    public SurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mCallbacks = new ArrayList<>();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mDrawingStopped = true;
        this.mDrawFinished = false;
        this.mScreenRect = new Rect();
        this.sObjectLock = new Object();
        this.mTmpRect = new Rect();
        this.mConfiguration = new Configuration();
        this.mSubLayer = -2;
        this.mIsCreating = false;
        this.mRtHandlingPositionUpdates = false;
        this.mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            public void onScrollChanged() {
                SurfaceView.this.updateSurface();
            }
        };
        this.mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                SurfaceView.this.mHaveFrame = SurfaceView.this.getWidth() > 0 && SurfaceView.this.getHeight() > 0;
                SurfaceView.this.updateSurface();
                return true;
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
        this.mVisiblityChangeState = false;
        this.mDelayDestory = false;
        this.mFromStopped = false;
        this.mLastWindowVisibility = false;
        this.mViewVisibility = false;
        this.mWindowStopped = false;
        this.mRequestedWidth = -1;
        this.mRequestedHeight = -1;
        this.mRequestedFormat = 4;
        this.mHaveFrame = false;
        this.mSurfaceCreated = false;
        this.mLastLockTime = 0;
        this.mVisible = false;
        this.mWindowSpaceLeft = -1;
        this.mWindowSpaceTop = -1;
        this.mSurfaceWidth = -1;
        this.mSurfaceHeight = -1;
        this.mFormat = -1;
        this.mSurfaceFrame = new Rect();
        this.mLastSurfaceWidth = -1;
        this.mLastSurfaceHeight = -1;
        this.mSurfaceFlags = 4;
        this.mRtTransaction = new SurfaceControl.Transaction();
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView.this.mRequestedWidth = width;
                    SurfaceView.this.mRequestedHeight = height;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    SurfaceView.this.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    SurfaceView.this.requestLayout();
                }
            }

            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView.this.mRequestedFormat = format;
                if (SurfaceView.this.mSurfaceControl != null) {
                    SurfaceView.this.updateSurface();
                }
            }

            @Deprecated
            public void setType(int type) {
            }

            public void setKeepScreenOn(boolean screenOn) {
                SurfaceView.this.runOnUiThread(new Runnable(screenOn) {
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        SurfaceView.this.setKeepScreenOn(this.f$1);
                    }
                });
            }

            public Canvas lockCanvas() {
                return internalLockCanvas(null, false);
            }

            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty, false);
            }

            public Canvas lockHardwareCanvas() {
                return internalLockCanvas(null, true);
            }

            private Canvas internalLockCanvas(Rect dirty, boolean hardware) {
                SurfaceView.this.mSurfaceLock.lock();
                Canvas c = null;
                if (!SurfaceView.this.mDrawingStopped && SurfaceView.this.mSurfaceControl != null) {
                    if (hardware) {
                        try {
                            c = SurfaceView.this.mSurface.lockHardwareCanvas();
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Exception locking surface", e);
                        }
                    } else {
                        c = SurfaceView.this.mSurface.lockCanvas(dirty);
                    }
                }
                if (c != null) {
                    SurfaceView.this.mLastLockTime = SystemClock.uptimeMillis();
                    return c;
                }
                long now = SystemClock.uptimeMillis();
                long nextTime = SurfaceView.this.mLastLockTime + 100;
                if (nextTime > now) {
                    try {
                        Thread.sleep(nextTime - now);
                    } catch (InterruptedException e2) {
                    }
                    now = SystemClock.uptimeMillis();
                }
                SurfaceView.this.mLastLockTime = now;
                SurfaceView.this.mSurfaceLock.unlock();
                return null;
            }

            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
                IHwApsImpl hwApsImpl = HwFrameworkFactory.getHwApsImpl();
                if (hwApsImpl.isSupportAps() && hwApsImpl.isAPSReady()) {
                    hwApsImpl.powerCtroll();
                }
            }

            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        this.mRenderNode.requestPositionUpdates(this);
        setWillNotDraw(true);
    }

    public SurfaceHolder getHolder() {
        return this.mSurfaceHolder;
    }

    private void updateRequestedVisibility() {
        this.mRequestedVisible = this.mViewVisibility && this.mWindowVisibility && !this.mWindowStopped;
    }

    public void windowStopped(boolean stopped) {
        this.mWindowStopped = stopped;
        updateRequestedVisibility();
        this.mFromStopped = true;
        updateSurface();
        this.mFromStopped = false;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewRootImpl().addWindowStoppedCallback(this);
        boolean z = false;
        this.mWindowStopped = false;
        if (getVisibility() == 0) {
            z = true;
        }
        this.mViewVisibility = z;
        updateRequestedVisibility();
        this.mAttachedToWindow = true;
        this.mParent.requestTransparentRegion(this);
        if (!this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnScrollChangedListener(this.mScrollChangedListener);
            observer.addOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mWindowVisibility = visibility == 0;
        updateRequestedVisibility();
        if (!this.mWindowVisibility) {
            this.mVisiblityChangeState = true;
        }
        updateSurface();
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean newRequestedVisible = false;
        this.mViewVisibility = visibility == 0;
        if (this.mWindowVisibility && this.mViewVisibility && !this.mWindowStopped) {
            newRequestedVisible = true;
        }
        if (newRequestedVisible != this.mRequestedVisible) {
            requestLayout();
        }
        this.mRequestedVisible = newRequestedVisible;
        updateSurface();
    }

    /* access modifiers changed from: private */
    public void performDrawFinished() {
        if (this.mPendingReportDraws > 0) {
            this.mDrawFinished = true;
            if (this.mAttachedToWindow) {
                notifyDrawFinished();
                invalidate();
                return;
            }
            return;
        }
        Log.e(TAG, System.identityHashCode(this) + "finished drawing but no pending report draw (extra call to draw completion runnable?)");
    }

    /* access modifiers changed from: package-private */
    public void notifyDrawFinished() {
        ViewRootImpl viewRoot = getViewRootImpl();
        if (viewRoot != null) {
            viewRoot.pendingDrawFinished();
        }
        this.mPendingReportDraws--;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        ViewRootImpl viewRoot = getViewRootImpl();
        if (viewRoot != null) {
            viewRoot.removeWindowStoppedCallback(this);
        }
        this.mAttachedToWindow = false;
        if (this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.removeOnScrollChangedListener(this.mScrollChangedListener);
            observer.removeOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = false;
        }
        while (this.mPendingReportDraws > 0) {
            notifyDrawFinished();
        }
        this.mRequestedVisible = false;
        updateSurface();
        synchronized (this.sObjectLock) {
            if (this.mSurfaceControl != null) {
                this.mSurfaceControl.destroy();
            }
            this.mSurfaceControl = null;
        }
        this.mHaveFrame = false;
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        if (this.mRequestedWidth >= 0) {
            width = resolveSizeAndState(this.mRequestedWidth, widthMeasureSpec, 0);
        } else {
            width = getDefaultSize(0, widthMeasureSpec);
        }
        if (this.mRequestedHeight >= 0) {
            height = resolveSizeAndState(this.mRequestedHeight, heightMeasureSpec, 0);
        } else {
            height = getDefaultSize(0, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int left, int top, int right, int bottom) {
        boolean result = super.setFrame(left, top, right, bottom);
        updateSurface();
        return result;
    }

    public boolean gatherTransparentRegion(Region region) {
        if (isAboveParent() || !this.mDrawFinished) {
            return super.gatherTransparentRegion(region);
        }
        boolean opaque = true;
        if ((this.mPrivateFlags & 128) == 0) {
            opaque = super.gatherTransparentRegion(region);
        } else if (region != null) {
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                getLocationInWindow(this.mLocation);
                int l = this.mLocation[0];
                int t = this.mLocation[1];
                region.op(l, t, l + w, t + h, Region.Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            opaque = false;
        }
        return opaque;
    }

    public void draw(Canvas canvas) {
        if (this.mDrawFinished && !isAboveParent() && (this.mPrivateFlags & 128) == 0) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        super.draw(canvas);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        if (this.mDrawFinished && !isAboveParent() && (this.mPrivateFlags & 128) == 128) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        super.dispatchDraw(canvas);
    }

    public void setZOrderMediaOverlay(boolean isMediaOverlay) {
        this.mSubLayer = isMediaOverlay ? -1 : -2;
    }

    public void setZOrderOnTop(boolean onTop) {
        if (onTop) {
            this.mSubLayer = 1;
        } else {
            this.mSubLayer = -2;
        }
    }

    public void setSecure(boolean isSecure) {
        if (isSecure) {
            this.mSurfaceFlags |= 128;
        } else {
            this.mSurfaceFlags &= -129;
        }
    }

    private void updateOpaqueFlag() {
        if (!PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            this.mSurfaceFlags |= 1024;
        } else {
            this.mSurfaceFlags &= -1025;
        }
    }

    private Rect getParentSurfaceInsets() {
        ViewRootImpl root = getViewRootImpl();
        if (root == null) {
            return null;
        }
        return root.mWindowAttributes.surfaceInsets;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:133:0x0269, code lost:
        if (r1.mRtHandlingPositionUpdates == false) goto L_0x026b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0364 A[SYNTHETIC, Splitter:B:192:0x0364] */
    /* JADX WARNING: Removed duplicated region for block: B:203:0x0379  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x03d1 A[Catch:{ all -> 0x03c6, all -> 0x03d9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x03f7 A[SYNTHETIC, Splitter:B:233:0x03f7] */
    /* JADX WARNING: Removed duplicated region for block: B:268:0x0461 A[Catch:{ all -> 0x0469 }] */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x04aa A[Catch:{ Exception -> 0x01c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:303:0x04bd A[Catch:{ Exception -> 0x01c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x04f1 A[Catch:{ Exception -> 0x01c7 }] */
    /* JADX WARNING: Removed duplicated region for block: B:330:0x0504  */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:301:0x04bc=Splitter:B:301:0x04bc, B:348:0x052c=Splitter:B:348:0x052c} */
    public void updateSurface() {
        boolean redrawNeeded;
        boolean redrawNeeded2;
        boolean z;
        boolean realSizeChanged;
        boolean z2;
        if (this.mHaveFrame) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot == null || viewRoot.mSurface == null || !viewRoot.mSurface.isValid()) {
                if (viewRoot != null && viewRoot.mSurface != null && !viewRoot.mSurface.isValid() && this.mSurfaceControl != null && this.mFromStopped && this.mDelayDestory) {
                    Log.i(TAG, "need destroy surface control");
                    synchronized (this.sObjectLock) {
                        this.mSurfaceControl.destroy();
                        this.mSurfaceControl = null;
                    }
                    this.mDelayDestory = false;
                }
                return;
            }
            this.mTranslator = viewRoot.mTranslator;
            if (this.mTranslator != null) {
                this.mSurface.setCompatibilityTranslator(this.mTranslator);
            }
            int myWidth = this.mRequestedWidth;
            if (myWidth <= 0) {
                myWidth = getWidth();
            }
            int myWidth2 = myWidth;
            int myHeight = this.mRequestedHeight;
            if (myHeight <= 0) {
                myHeight = getHeight();
            }
            int myHeight2 = myHeight;
            boolean formatChanged = this.mFormat != this.mRequestedFormat;
            boolean visibleChanged = this.mVisible != this.mRequestedVisible;
            boolean creating = (this.mSurfaceControl == null || formatChanged || visibleChanged) && this.mRequestedVisible;
            boolean sizeChanged = (this.mSurfaceWidth == myWidth2 && this.mSurfaceHeight == myHeight2) ? false : true;
            boolean windowVisibleChanged = this.mWindowVisibility != this.mLastWindowVisibility;
            if (creating || formatChanged || sizeChanged || visibleChanged) {
            } else if (windowVisibleChanged) {
                boolean z3 = windowVisibleChanged;
            } else {
                getLocationInSurface(this.mLocation);
                boolean positionChanged = (this.mWindowSpaceLeft == this.mLocation[0] && this.mWindowSpaceTop == this.mLocation[1]) ? false : true;
                boolean layoutSizeChanged = (getWidth() == this.mScreenRect.width() && getHeight() == this.mScreenRect.height()) ? false : true;
                if (positionChanged || layoutSizeChanged) {
                    this.mWindowSpaceLeft = this.mLocation[0];
                    this.mWindowSpaceTop = this.mLocation[1];
                    this.mLocation[0] = getWidth();
                    this.mLocation[1] = getHeight();
                    boolean z4 = positionChanged;
                    boolean z5 = windowVisibleChanged;
                    this.mScreenRect.set(this.mWindowSpaceLeft, this.mWindowSpaceTop, this.mWindowSpaceLeft + this.mLocation[0], this.mWindowSpaceTop + this.mLocation[1]);
                    if (getDisplay() != null && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
                        ViewRootImpl impl = getViewRootImpl();
                        if (!(impl == null || impl.mBasePackageName == null || !impl.mBasePackageName.equals(GALLERY))) {
                            ViewRootImpl viewRootImpl = impl;
                            this.mScreenRect.set(this.mWindowSpaceLeft, this.mWindowSpaceTop, this.mWindowSpaceLeft + this.mLocation[0], this.mWindowSpaceTop + this.mLocation[1]);
                        }
                    }
                    if (this.mTranslator != null) {
                        this.mTranslator.translateRectInAppWindowToScreen(this.mScreenRect);
                    }
                    if (this.mSurfaceControl != null) {
                        if (!isHardwareAccelerated() || !this.mRtHandlingPositionUpdates) {
                            try {
                                setParentSpaceRectangle(this.mScreenRect, -1);
                            } catch (Exception ex) {
                                Log.e(TAG, "Exception configuring surface", ex);
                            }
                        }
                    }
                    return;
                }
                boolean z6 = windowVisibleChanged;
            }
            getLocationInWindow(this.mLocation);
            try {
                boolean z7 = this.mRequestedVisible;
                this.mVisible = z7;
                boolean visible = z7;
                this.mWindowSpaceLeft = this.mLocation[0];
                this.mWindowSpaceTop = this.mLocation[1];
                this.mSurfaceWidth = myWidth2;
                this.mSurfaceHeight = myHeight2;
                this.mFormat = this.mRequestedFormat;
                this.mLastWindowVisibility = this.mWindowVisibility;
                this.mScreenRect.left = this.mWindowSpaceLeft;
                this.mScreenRect.top = this.mWindowSpaceTop;
                this.mScreenRect.right = this.mWindowSpaceLeft + getWidth();
                this.mScreenRect.bottom = this.mWindowSpaceTop + getHeight();
                if (this.mTranslator != null) {
                    try {
                        this.mTranslator.translateRectInAppWindowToScreen(this.mScreenRect);
                    } catch (Exception e) {
                        ex = e;
                    }
                }
                Rect surfaceInsets = getParentSurfaceInsets();
                this.mScreenRect.offset(surfaceInsets.left, surfaceInsets.top);
                if (creating) {
                    this.mSurfaceSession = new SurfaceSession(viewRoot.mSurface);
                    this.mDeferredDestroySurfaceControl = this.mSurfaceControl;
                    updateOpaqueFlag();
                    redrawNeeded = false;
                    try {
                        this.mSurfaceControl = new SurfaceControlWithBackground("SurfaceView - " + viewRoot.getTitle().toString(), (this.mSurfaceFlags & 1024) != 0, new SurfaceControl.Builder(this.mSurfaceSession).setSize(this.mSurfaceWidth, this.mSurfaceHeight).setFormat(this.mFormat).setFlags(this.mSurfaceFlags));
                    } catch (Exception e2) {
                        ex = e2;
                        Log.e(TAG, "Exception configuring surface", ex);
                    }
                } else {
                    redrawNeeded = false;
                    if (this.mSurfaceControl == null) {
                        return;
                    }
                }
                this.mSurfaceLock.lock();
                try {
                    this.mDrawingStopped = !visible;
                    SurfaceControl.openTransaction();
                    try {
                        this.mSurfaceControl.setLayer(this.mSubLayer);
                        if (this.mViewVisibility) {
                            try {
                                this.mSurfaceControl.show();
                            } catch (Throwable th) {
                                th = th;
                                Rect rect = surfaceInsets;
                            }
                        } else {
                            this.mSurfaceControl.hide();
                        }
                        if (!sizeChanged && !creating) {
                        }
                        this.mSurfaceControl.setPosition((float) this.mScreenRect.left, (float) this.mScreenRect.top);
                        this.mSurfaceControl.setMatrix(((float) this.mScreenRect.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) this.mScreenRect.height()) / ((float) this.mSurfaceHeight));
                        if (getDisplay() != null) {
                            if (HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
                                ViewRootImpl impl2 = getViewRootImpl();
                                if (!(impl2 == null || impl2.mBasePackageName == null || !impl2.mBasePackageName.equals(GALLERY))) {
                                    setWindowCrop(this.mScreenRect, this.mSurfaceControl, surfaceInsets);
                                }
                            }
                        }
                        float resolutionRatio = -1.0f;
                        if (HwFrameworkFactory.getApsManager() != null) {
                            resolutionRatio = HwFrameworkFactory.getApsManager().getResolution(viewRoot.mBasePackageName);
                        }
                        if (0.0f < resolutionRatio && resolutionRatio < 1.0f) {
                            this.mSurfaceControl.setSurfaceLowResolutionInfo(1.0f / resolutionRatio, 2);
                        }
                    } catch (Exception e3) {
                        Slog.e(TAG, "Exception is thrown when get/set resolution ratio", e3);
                    } catch (Throwable th2) {
                        th = th2;
                        Rect rect2 = surfaceInsets;
                        try {
                            SurfaceControl.closeTransaction();
                            throw th;
                        } catch (Throwable th3) {
                            th = th3;
                            boolean z8 = redrawNeeded;
                            this.mSurfaceLock.unlock();
                            throw th;
                        }
                    }
                    if (sizeChanged) {
                        this.mSurfaceControl.setSize(this.mSurfaceWidth, this.mSurfaceHeight);
                    }
                    SurfaceControl.closeTransaction();
                    if (sizeChanged || creating) {
                        redrawNeeded2 = true;
                    } else {
                        redrawNeeded2 = redrawNeeded;
                    }
                    try {
                        this.mSurfaceFrame.left = 0;
                        this.mSurfaceFrame.top = 0;
                        if (this.mTranslator == null) {
                            try {
                                this.mSurfaceFrame.right = this.mSurfaceWidth;
                                this.mSurfaceFrame.bottom = this.mSurfaceHeight;
                            } catch (Throwable th4) {
                                th = th4;
                                Rect rect3 = surfaceInsets;
                            }
                        } else {
                            float appInvertedScale = this.mTranslator.applicationInvertedScale;
                            this.mSurfaceFrame.right = (int) ((((float) this.mSurfaceWidth) * appInvertedScale) + 0.5f);
                            this.mSurfaceFrame.bottom = (int) ((((float) this.mSurfaceHeight) * appInvertedScale) + 0.5f);
                        }
                        int surfaceWidth = this.mSurfaceFrame.right;
                        int surfaceHeight = this.mSurfaceFrame.bottom;
                        if (this.mLastSurfaceWidth == surfaceWidth) {
                            if (this.mLastSurfaceHeight == surfaceHeight) {
                                z = false;
                                realSizeChanged = z;
                                this.mLastSurfaceWidth = surfaceWidth;
                                this.mLastSurfaceHeight = surfaceHeight;
                                this.mSurfaceLock.unlock();
                                if (visible) {
                                    try {
                                        if (!this.mDrawFinished) {
                                            z2 = true;
                                            boolean redrawNeeded3 = redrawNeeded2 | z2;
                                            SurfaceHolder.Callback[] callbacks = null;
                                            boolean surfaceChanged = creating;
                                            if (this.mSurfaceCreated) {
                                                if (surfaceChanged || (!visible && visibleChanged)) {
                                                    try {
                                                        this.mSurfaceCreated = false;
                                                        if (this.mSurface.isValid()) {
                                                            SurfaceHolder.Callback[] callbacks2 = getSurfaceCallbacks();
                                                            int length = callbacks2.length;
                                                            int i = 0;
                                                            while (i < length) {
                                                                SurfaceHolder.Callback[] callbacks3 = callbacks2;
                                                                Rect surfaceInsets2 = surfaceInsets;
                                                                callbacks2[i].surfaceDestroyed(this.mSurfaceHolder);
                                                                i++;
                                                                callbacks2 = callbacks3;
                                                                surfaceInsets = surfaceInsets2;
                                                            }
                                                            SurfaceHolder.Callback[] callbacks4 = callbacks2;
                                                            Rect rect4 = surfaceInsets;
                                                            if (this.mSurface.isValid()) {
                                                                this.mSurface.forceScopedDisconnect();
                                                            }
                                                            LogPower.push(142);
                                                            callbacks = callbacks4;
                                                            if (creating) {
                                                                this.mSurface.copyFrom(this.mSurfaceControl);
                                                            }
                                                            if (sizeChanged && getContext().getApplicationInfo().targetSdkVersion < 26) {
                                                                this.mSurface.createFrom(this.mSurfaceControl);
                                                            }
                                                            if (visible) {
                                                                try {
                                                                    if (this.mSurface.isValid()) {
                                                                        if (!this.mSurfaceCreated && (surfaceChanged || visibleChanged)) {
                                                                            this.mSurfaceCreated = true;
                                                                            this.mIsCreating = true;
                                                                            if (callbacks == null) {
                                                                                callbacks = getSurfaceCallbacks();
                                                                            }
                                                                            int length2 = callbacks.length;
                                                                            int i2 = 0;
                                                                            while (i2 < length2) {
                                                                                callbacks[i2].surfaceCreated(this.mSurfaceHolder);
                                                                                i2++;
                                                                                callbacks = callbacks;
                                                                            }
                                                                            LogPower.push(141);
                                                                            callbacks = callbacks;
                                                                        }
                                                                        if (!creating && !formatChanged && !sizeChanged && !visibleChanged) {
                                                                            if (!realSizeChanged) {
                                                                                boolean z9 = realSizeChanged;
                                                                                if (redrawNeeded3) {
                                                                                    if (callbacks == null) {
                                                                                        callbacks = getSurfaceCallbacks();
                                                                                    }
                                                                                    this.mPendingReportDraws++;
                                                                                    viewRoot.drawPending();
                                                                                    new SurfaceCallbackHelper(new Runnable() {
                                                                                        public final void run() {
                                                                                            SurfaceView.this.onDrawFinished();
                                                                                        }
                                                                                    }).dispatchSurfaceRedrawNeededAsync(this.mSurfaceHolder, callbacks);
                                                                                }
                                                                                this.mIsCreating = false;
                                                                                if (this.mSurfaceControl != null && !this.mSurfaceCreated) {
                                                                                    this.mSurface.release();
                                                                                    if (!this.mWindowStopped || (this.mFromStopped && this.mDelayDestory)) {
                                                                                        if (!this.mVisiblityChangeState) {
                                                                                            synchronized (this.sObjectLock) {
                                                                                                try {
                                                                                                    this.mSurfaceControl.destroy();
                                                                                                    this.mSurfaceControl = null;
                                                                                                } catch (Throwable th5) {
                                                                                                    while (true) {
                                                                                                        th = th5;
                                                                                                    }
                                                                                                    throw th;
                                                                                                }
                                                                                            }
                                                                                            this.mDelayDestory = false;
                                                                                        } else {
                                                                                            Log.i(TAG, "delay destroy surface control");
                                                                                            this.mDelayDestory = true;
                                                                                        }
                                                                                        this.mVisiblityChangeState = false;
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                        if (callbacks == null) {
                                                                            callbacks = getSurfaceCallbacks();
                                                                        }
                                                                        int length3 = callbacks.length;
                                                                        int i3 = 0;
                                                                        while (i3 < length3) {
                                                                            SurfaceHolder.Callback[] callbacks5 = callbacks;
                                                                            boolean realSizeChanged2 = realSizeChanged;
                                                                            try {
                                                                                callbacks[i3].surfaceChanged(this.mSurfaceHolder, this.mFormat, myWidth2, myHeight2);
                                                                                i3++;
                                                                                callbacks = callbacks5;
                                                                                realSizeChanged = realSizeChanged2;
                                                                            } catch (Throwable th6) {
                                                                                th = th6;
                                                                                this.mIsCreating = false;
                                                                                this.mSurface.release();
                                                                                if (this.mVisiblityChangeState) {
                                                                                }
                                                                                this.mVisiblityChangeState = false;
                                                                                throw th;
                                                                            }
                                                                        }
                                                                        boolean z10 = realSizeChanged;
                                                                        if (redrawNeeded3) {
                                                                        }
                                                                        this.mIsCreating = false;
                                                                        this.mSurface.release();
                                                                        if (!this.mVisiblityChangeState) {
                                                                        }
                                                                        this.mVisiblityChangeState = false;
                                                                    }
                                                                } catch (Throwable th7) {
                                                                    th = th7;
                                                                    boolean z11 = realSizeChanged;
                                                                    this.mIsCreating = false;
                                                                    if (this.mSurfaceControl != null && !this.mSurfaceCreated) {
                                                                        this.mSurface.release();
                                                                        if (!this.mWindowStopped || (this.mFromStopped && this.mDelayDestory)) {
                                                                            if (this.mVisiblityChangeState) {
                                                                                synchronized (this.sObjectLock) {
                                                                                    try {
                                                                                        this.mSurfaceControl.destroy();
                                                                                        this.mSurfaceControl = null;
                                                                                    } catch (Throwable th8) {
                                                                                        while (true) {
                                                                                            th = th8;
                                                                                        }
                                                                                        throw th;
                                                                                    }
                                                                                }
                                                                                this.mDelayDestory = false;
                                                                            } else {
                                                                                Log.i(TAG, "delay destroy surface control");
                                                                                this.mDelayDestory = true;
                                                                            }
                                                                            this.mVisiblityChangeState = false;
                                                                        }
                                                                    }
                                                                    throw th;
                                                                }
                                                            }
                                                            this.mIsCreating = false;
                                                            this.mSurface.release();
                                                            if (!this.mVisiblityChangeState) {
                                                            }
                                                            this.mVisiblityChangeState = false;
                                                        }
                                                    } catch (Throwable th9) {
                                                        th = th9;
                                                        boolean z12 = realSizeChanged;
                                                        this.mIsCreating = false;
                                                        this.mSurface.release();
                                                        if (this.mVisiblityChangeState) {
                                                        }
                                                        this.mVisiblityChangeState = false;
                                                        throw th;
                                                    }
                                                } else {
                                                    Rect rect5 = surfaceInsets;
                                                    if (creating) {
                                                    }
                                                    this.mSurface.createFrom(this.mSurfaceControl);
                                                    if (visible) {
                                                    }
                                                    this.mIsCreating = false;
                                                    this.mSurface.release();
                                                    if (!this.mVisiblityChangeState) {
                                                    }
                                                    this.mVisiblityChangeState = false;
                                                }
                                            }
                                            if (creating) {
                                            }
                                            this.mSurface.createFrom(this.mSurfaceControl);
                                            if (visible) {
                                            }
                                            this.mIsCreating = false;
                                            this.mSurface.release();
                                            if (!this.mVisiblityChangeState) {
                                            }
                                            this.mVisiblityChangeState = false;
                                        }
                                    } catch (Throwable th10) {
                                        th = th10;
                                        boolean z13 = realSizeChanged;
                                        Rect rect6 = surfaceInsets;
                                        this.mIsCreating = false;
                                        this.mSurface.release();
                                        if (this.mVisiblityChangeState) {
                                        }
                                        this.mVisiblityChangeState = false;
                                        throw th;
                                    }
                                }
                                z2 = false;
                                boolean redrawNeeded32 = redrawNeeded2 | z2;
                                SurfaceHolder.Callback[] callbacks6 = null;
                                boolean surfaceChanged2 = creating;
                                if (this.mSurfaceCreated) {
                                }
                                if (creating) {
                                }
                                this.mSurface.createFrom(this.mSurfaceControl);
                                if (visible) {
                                }
                                this.mIsCreating = false;
                                this.mSurface.release();
                                if (!this.mVisiblityChangeState) {
                                }
                                this.mVisiblityChangeState = false;
                            }
                        }
                        z = true;
                        realSizeChanged = z;
                        try {
                            this.mLastSurfaceWidth = surfaceWidth;
                            this.mLastSurfaceHeight = surfaceHeight;
                            this.mSurfaceLock.unlock();
                            if (visible) {
                            }
                            z2 = false;
                            boolean redrawNeeded322 = redrawNeeded2 | z2;
                            SurfaceHolder.Callback[] callbacks62 = null;
                            boolean surfaceChanged22 = creating;
                            try {
                                if (this.mSurfaceCreated) {
                                }
                                if (creating) {
                                }
                                this.mSurface.createFrom(this.mSurfaceControl);
                                if (visible) {
                                }
                                this.mIsCreating = false;
                                this.mSurface.release();
                                if (!this.mVisiblityChangeState) {
                                }
                                this.mVisiblityChangeState = false;
                            } catch (Throwable th11) {
                                th = th11;
                                boolean z14 = realSizeChanged;
                                Rect rect7 = surfaceInsets;
                                this.mIsCreating = false;
                                this.mSurface.release();
                                if (this.mVisiblityChangeState) {
                                }
                                this.mVisiblityChangeState = false;
                                throw th;
                            }
                        } catch (Throwable th12) {
                            th = th12;
                            boolean z15 = realSizeChanged;
                            Rect rect8 = surfaceInsets;
                            this.mSurfaceLock.unlock();
                            throw th;
                        }
                    } catch (Throwable th13) {
                        th = th13;
                        Rect rect9 = surfaceInsets;
                        this.mSurfaceLock.unlock();
                        throw th;
                    }
                } catch (Throwable th14) {
                    th = th14;
                    Rect rect10 = surfaceInsets;
                    boolean z16 = redrawNeeded;
                }
            } catch (Exception e4) {
                ex = e4;
                Log.e(TAG, "Exception configuring surface", ex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onDrawFinished() {
        if (this.mDeferredDestroySurfaceControl != null) {
            this.mDeferredDestroySurfaceControl.destroy();
            this.mDeferredDestroySurfaceControl = null;
        }
        runOnUiThread(new Runnable() {
            public final void run() {
                SurfaceView.this.performDrawFinished();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void applyChildSurfaceTransaction_renderWorker(SurfaceControl.Transaction t, Surface viewRootSurface, long nextViewRootFrameNumber) {
    }

    private void applySurfaceTransforms(SurfaceControl surface, Rect position, long frameNumber) {
        if (frameNumber > 0) {
            this.mRtTransaction.deferTransactionUntilSurface(surface, getViewRootImpl().mSurface, frameNumber);
        }
        synchronized (this.sObjectLock) {
            this.mRtTransaction.setPosition(surface, (float) position.left, (float) position.top);
        }
        this.mRtTransaction.setMatrix(surface, ((float) position.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) position.height()) / ((float) this.mSurfaceHeight));
    }

    private void setParentSpaceRectangle(Rect position, long frameNumber) {
        ViewRootImpl viewRoot = getViewRootImpl();
        applySurfaceTransforms(this.mSurfaceControl, position, frameNumber);
        applySurfaceTransforms(this.mSurfaceControl.mBackgroundControl, position, frameNumber);
        applyChildSurfaceTransaction_renderWorker(this.mRtTransaction, viewRoot.mSurface, frameNumber);
        this.mRtTransaction.apply();
    }

    private void setWindowCrop(Rect position, SurfaceControl surfaceControl, Rect surfaceInsets) {
        this.mTmpRect.set(0, 0, this.mSurfaceWidth, this.mSurfaceHeight);
        if (position.left < surfaceInsets.left) {
            if (position.left + this.mSurfaceWidth > surfaceInsets.left) {
                this.mTmpRect.left = surfaceInsets.left - position.left;
                this.mTmpRect.right = this.mSurfaceWidth;
            } else {
                this.mTmpRect.left = 0;
                this.mTmpRect.right = this.mSurfaceWidth - surfaceInsets.left;
            }
        } else if (position.left > this.mSurfaceWidth + surfaceInsets.left) {
            this.mTmpRect.left = surfaceInsets.right;
            this.mTmpRect.right = this.mSurfaceWidth;
        } else {
            this.mTmpRect.left = 0;
            this.mTmpRect.right = (this.mSurfaceWidth + surfaceInsets.left) - position.left;
        }
        surfaceControl.setWindowCrop(this.mTmpRect);
    }

    public final void updateSurfacePosition_renderWorker(long frameNumber, int left, int top, int right, int bottom) {
        if (this.mSurfaceControl != null) {
            this.mRtHandlingPositionUpdates = true;
            if (this.mRTLastReportedPosition.left != left || this.mRTLastReportedPosition.top != top || this.mRTLastReportedPosition.right != right || this.mRTLastReportedPosition.bottom != bottom) {
                try {
                    this.mRTLastReportedPosition.set(left, top, right, bottom);
                    setParentSpaceRectangle(this.mRTLastReportedPosition, frameNumber);
                } catch (Exception ex) {
                    Log.e(TAG, "Exception from repositionChild", ex);
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002a, code lost:
        if (r4.mRtHandlingPositionUpdates == false) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002c, code lost:
        r4.mRtHandlingPositionUpdates = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        if (r4.mScreenRect.isEmpty() != false) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003f, code lost:
        if (r4.mScreenRect.equals(r4.mRTLastReportedPosition) != false) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        setParentSpaceRectangle(r4.mScreenRect, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0047, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0048, code lost:
        android.util.Log.e(TAG, "Exception configuring surface", r0);
     */
    public final void surfacePositionLost_uiRtSync(long frameNumber) {
        this.mRTLastReportedPosition.setEmpty();
        synchronized (this.sObjectLock) {
            if (this.mSurfaceControl == null) {
                Log.e(TAG, "surfacePositionLost_uiRtSync  mSurfaceControl = " + this.mSurfaceControl);
            }
        }
    }

    private SurfaceHolder.Callback[] getSurfaceCallbacks() {
        SurfaceHolder.Callback[] callbacks;
        synchronized (this.mCallbacks) {
            callbacks = new SurfaceHolder.Callback[this.mCallbacks.size()];
            this.mCallbacks.toArray(callbacks);
        }
        return callbacks;
    }

    /* access modifiers changed from: private */
    public void runOnUiThread(Runnable runnable) {
        Handler handler = getHandler();
        if (handler == null || handler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public boolean isFixedSize() {
        return (this.mRequestedWidth == -1 && this.mRequestedHeight == -1) ? false : true;
    }

    private boolean isAboveParent() {
        return this.mSubLayer >= 0;
    }

    public void setResizeBackgroundColor(int bgColor) {
        this.mSurfaceControl.setBackgroundColor(bgColor);
    }
}

package android.view;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.CompatibilityInfo.Translator;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.view.SurfaceHolder.Callback;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import com.android.internal.view.SurfaceCallbackHelper;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class SurfaceView extends View implements WindowStoppedCallback {
    private static final boolean DEBUG = false;
    private static final String GALLERY = "com.android.gallery3d";
    private static final String TAG = "SurfaceView";
    private boolean mAttachedToWindow;
    final ArrayList<Callback> mCallbacks;
    final Configuration mConfiguration;
    SurfaceControl mDeferredDestroySurfaceControl;
    boolean mDrawFinished;
    private final OnPreDrawListener mDrawListener;
    boolean mDrawingStopped;
    int mFormat;
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
    final Rect mScreenRect;
    private final OnScrollChangedListener mScrollChangedListener;
    int mSubLayer;
    final Surface mSurface;
    SurfaceControl mSurfaceControl;
    boolean mSurfaceCreated;
    private int mSurfaceFlags;
    final Rect mSurfaceFrame;
    int mSurfaceHeight;
    private final SurfaceHolder mSurfaceHolder;
    final ReentrantLock mSurfaceLock;
    SurfaceSession mSurfaceSession;
    int mSurfaceWidth;
    final Rect mTmpRect;
    private Translator mTranslator;
    boolean mViewVisibility;
    boolean mVisible;
    int mWindowSpaceLeft;
    int mWindowSpaceTop;
    boolean mWindowStopped;
    boolean mWindowVisibility;

    class SurfaceControlWithBackground extends SurfaceControl {
        private SurfaceControl mBackgroundControl;
        private boolean mOpaque = true;
        public boolean mVisible = false;

        public SurfaceControlWithBackground(SurfaceSession s, String name, int w, int h, int format, int flags) throws Exception {
            super(s, name, w, h, format, flags);
            this.mBackgroundControl = new SurfaceControl(s, "Background for - " + name, w, h, -1, flags | 131072);
            this.mOpaque = (flags & 1024) != 0;
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

        void updateBackgroundVisibility() {
            if (this.mOpaque && this.mVisible) {
                this.mBackgroundControl.show();
            } else {
                this.mBackgroundControl.hide();
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
        this.mCallbacks = new ArrayList();
        this.mLocation = new int[2];
        this.mSurfaceLock = new ReentrantLock();
        this.mSurface = new Surface();
        this.mDrawingStopped = true;
        this.mDrawFinished = false;
        this.mScreenRect = new Rect();
        this.mTmpRect = new Rect();
        this.mConfiguration = new Configuration();
        this.mSubLayer = -2;
        this.mIsCreating = false;
        this.mRtHandlingPositionUpdates = false;
        this.mScrollChangedListener = new OnScrollChangedListener() {
            public void onScrollChanged() {
                SurfaceView.this.updateSurface();
            }
        };
        this.mDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                boolean z = false;
                SurfaceView surfaceView = SurfaceView.this;
                if (SurfaceView.this.getWidth() > 0 && SurfaceView.this.getHeight() > 0) {
                    z = true;
                }
                surfaceView.mHaveFrame = z;
                SurfaceView.this.updateSurface();
                return true;
            }
        };
        this.mRequestedVisible = false;
        this.mWindowVisibility = false;
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
        this.mRTLastReportedPosition = new Rect();
        this.mSurfaceHolder = new SurfaceHolder() {
            private static final String LOG_TAG = "SurfaceHolder";

            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            public void addCallback(Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            public void removeCallback(Callback callback) {
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

            /* synthetic */ void lambda$-android_view_SurfaceView$3_46478(boolean screenOn) {
                SurfaceView.this.setKeepScreenOn(screenOn);
            }

            public void setKeepScreenOn(boolean screenOn) {
                SurfaceView.this.runOnUiThread(new android.view.-$Lambda$P6MTGFSudLpwrqb6oVD8FdorW1c.AnonymousClass2(screenOn, this));
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
                if (!(SurfaceView.this.mDrawingStopped || SurfaceView.this.mSurfaceControl == null)) {
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
                if (HwFrameworkFactory.getHwNsdImpl().isSupportAps() && HwFrameworkFactory.getHwNsdImpl().isAPSReady()) {
                    HwFrameworkFactory.getHwNsdImpl().powerCtroll();
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
        boolean z = (this.mViewVisibility && this.mWindowVisibility) ? this.mWindowStopped ^ 1 : false;
        this.mRequestedVisible = z;
    }

    public void windowStopped(boolean stopped) {
        this.mWindowStopped = stopped;
        updateRequestedVisibility();
        updateSurface();
    }

    protected void onAttachedToWindow() {
        boolean z = false;
        super.onAttachedToWindow();
        getViewRootImpl().addWindowStoppedCallback(this);
        this.mWindowStopped = false;
        if (getVisibility() == 0) {
            z = true;
        }
        this.mViewVisibility = z;
        updateRequestedVisibility();
        this.mAttachedToWindow = true;
        if (!this.mGlobalListenersAdded) {
            ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnScrollChangedListener(this.mScrollChangedListener);
            observer.addOnPreDrawListener(this.mDrawListener);
            this.mGlobalListenersAdded = true;
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        boolean z = false;
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mWindowVisibility = z;
        updateRequestedVisibility();
        updateSurface();
    }

    public void setVisibility(int visibility) {
        boolean z = false;
        super.setVisibility(visibility);
        if (visibility == 0) {
            z = true;
        }
        this.mViewVisibility = z;
        boolean newRequestedVisible = (this.mWindowVisibility && this.mViewVisibility) ? this.mWindowStopped ^ 1 : false;
        if (newRequestedVisible != this.mRequestedVisible) {
            requestLayout();
        }
        this.mRequestedVisible = newRequestedVisible;
        updateSurface();
    }

    private void performDrawFinished() {
        if (this.mPendingReportDraws > 0) {
            this.mDrawFinished = true;
            if (this.mAttachedToWindow) {
                this.mParent.requestTransparentRegion(this);
                notifyDrawFinished();
                invalidate();
                return;
            }
            return;
        }
        Log.e(TAG, System.identityHashCode(this) + "finished drawing" + " but no pending report draw (extra call" + " to draw completion runnable?)");
    }

    void notifyDrawFinished() {
        ViewRootImpl viewRoot = getViewRootImpl();
        if (viewRoot != null) {
            viewRoot.pendingDrawFinished();
        }
        this.mPendingReportDraws--;
    }

    protected void onDetachedFromWindow() {
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
        if (this.mSurfaceControl != null) {
            this.mSurfaceControl.destroy();
        }
        this.mSurfaceControl = null;
        this.mHaveFrame = false;
        super.onDetachedFromWindow();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        if (this.mRequestedWidth >= 0) {
            width = View.resolveSizeAndState(this.mRequestedWidth, widthMeasureSpec, 0);
        } else {
            width = View.getDefaultSize(0, widthMeasureSpec);
        }
        if (this.mRequestedHeight >= 0) {
            height = View.resolveSizeAndState(this.mRequestedHeight, heightMeasureSpec, 0);
        } else {
            height = View.getDefaultSize(0, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        boolean result = super.setFrame(left, top, right, bottom);
        updateSurface();
        return result;
    }

    public boolean gatherTransparentRegion(Region region) {
        if (isAboveParent() || (this.mDrawFinished ^ 1) != 0) {
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
                region.op(l, t, l + w, t + h, Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            opaque = false;
        }
        return opaque;
    }

    public void draw(Canvas canvas) {
        if (this.mDrawFinished && (isAboveParent() ^ 1) != 0 && (this.mPrivateFlags & 128) == 0) {
            canvas.drawColor(0, Mode.CLEAR);
        }
        super.draw(canvas);
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mDrawFinished && (isAboveParent() ^ 1) != 0 && (this.mPrivateFlags & 128) == 128) {
            canvas.drawColor(0, Mode.CLEAR);
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
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            this.mSurfaceFlags &= -1025;
        } else {
            this.mSurfaceFlags |= 1024;
        }
    }

    private Rect getParentSurfaceInsets() {
        ViewRootImpl root = getViewRootImpl();
        if (root == null) {
            return null;
        }
        return root.mWindowAttributes.surfaceInsets;
    }

    protected void updateSurface() {
        if (this.mHaveFrame) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null && viewRoot.mSurface != null && (viewRoot.mSurface.isValid() ^ 1) == 0) {
                boolean creating;
                this.mTranslator = viewRoot.mTranslator;
                if (this.mTranslator != null) {
                    this.mSurface.setCompatibilityTranslator(this.mTranslator);
                }
                int myWidth = this.mRequestedWidth;
                if (myWidth <= 0) {
                    myWidth = getWidth();
                }
                int myHeight = this.mRequestedHeight;
                if (myHeight <= 0) {
                    myHeight = getHeight();
                }
                boolean formatChanged = this.mFormat != this.mRequestedFormat;
                boolean visibleChanged = this.mVisible != this.mRequestedVisible;
                if (this.mSurfaceControl == null || formatChanged || visibleChanged) {
                    creating = this.mRequestedVisible;
                } else {
                    creating = false;
                }
                boolean sizeChanged = (this.mSurfaceWidth == myWidth && this.mSurfaceHeight == myHeight) ? false : true;
                boolean windowVisibleChanged = this.mWindowVisibility != this.mLastWindowVisibility;
                boolean redrawNeeded = false;
                ViewRootImpl impl;
                if (creating || formatChanged || sizeChanged || visibleChanged || windowVisibleChanged) {
                    getLocationInWindow(this.mLocation);
                    try {
                        boolean visible = this.mRequestedVisible;
                        this.mVisible = visible;
                        this.mWindowSpaceLeft = this.mLocation[0];
                        this.mWindowSpaceTop = this.mLocation[1];
                        this.mSurfaceWidth = myWidth;
                        this.mSurfaceHeight = myHeight;
                        this.mFormat = this.mRequestedFormat;
                        this.mLastWindowVisibility = this.mWindowVisibility;
                        this.mScreenRect.left = this.mWindowSpaceLeft;
                        this.mScreenRect.top = this.mWindowSpaceTop;
                        this.mScreenRect.right = this.mWindowSpaceLeft + getWidth();
                        this.mScreenRect.bottom = this.mWindowSpaceTop + getHeight();
                        if (this.mTranslator != null) {
                            this.mTranslator.translateRectInAppWindowToScreen(this.mScreenRect);
                        }
                        Rect surfaceInsets = getParentSurfaceInsets();
                        this.mScreenRect.offset(surfaceInsets.left, surfaceInsets.top);
                        if (creating) {
                            this.mSurfaceSession = new SurfaceSession(viewRoot.mSurface);
                            this.mDeferredDestroySurfaceControl = this.mSurfaceControl;
                            updateOpaqueFlag();
                            this.mSurfaceControl = new SurfaceControlWithBackground(this.mSurfaceSession, "SurfaceView - " + viewRoot.getTitle().toString(), this.mSurfaceWidth, this.mSurfaceHeight, this.mFormat, this.mSurfaceFlags);
                        } else if (this.mSurfaceControl == null) {
                            return;
                        }
                        this.mSurfaceLock.lock();
                        try {
                            this.mDrawingStopped = visible ^ 1;
                            SurfaceControl.openTransaction();
                            this.mSurfaceControl.setLayer(this.mSubLayer);
                            if (this.mViewVisibility) {
                                this.mSurfaceControl.show();
                            } else {
                                this.mSurfaceControl.hide();
                            }
                            if (sizeChanged || creating || (this.mRtHandlingPositionUpdates ^ 1) != 0) {
                                this.mSurfaceControl.setPosition((float) this.mScreenRect.left, (float) this.mScreenRect.top);
                                this.mSurfaceControl.setMatrix(((float) this.mScreenRect.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) this.mScreenRect.height()) / ((float) this.mSurfaceHeight));
                                if (getDisplay() != null && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
                                    impl = getViewRootImpl();
                                    if (!(impl == null || impl.mBasePackageName == null || !impl.mBasePackageName.equals(GALLERY))) {
                                        setWindowCrop(this.mScreenRect, this.mSurfaceControl, surfaceInsets);
                                    }
                                }
                                float resolutionRatio = -1.0f;
                                try {
                                    if (HwFrameworkFactory.getApsManager() != null) {
                                        resolutionRatio = HwFrameworkFactory.getApsManager().getResolution(viewRoot.mBasePackageName);
                                    }
                                    if (0.0f < resolutionRatio && resolutionRatio < 1.0f) {
                                        this.mSurfaceControl.setSurfaceLowResolutionInfo(1.0f / resolutionRatio, 2);
                                    }
                                } catch (Exception e) {
                                    Slog.e(TAG, "Exception is thrown when get/set resolution ratio", e);
                                }
                            }
                            if (sizeChanged) {
                                this.mSurfaceControl.setSize(this.mSurfaceWidth, this.mSurfaceHeight);
                            }
                            SurfaceControl.closeTransaction();
                            if (sizeChanged || creating) {
                                redrawNeeded = true;
                            }
                            this.mSurfaceFrame.left = 0;
                            this.mSurfaceFrame.top = 0;
                            if (this.mTranslator == null) {
                                this.mSurfaceFrame.right = this.mSurfaceWidth;
                                this.mSurfaceFrame.bottom = this.mSurfaceHeight;
                            } else {
                                float appInvertedScale = this.mTranslator.applicationInvertedScale;
                                this.mSurfaceFrame.right = (int) ((((float) this.mSurfaceWidth) * appInvertedScale) + 0.5f);
                                this.mSurfaceFrame.bottom = (int) ((((float) this.mSurfaceHeight) * appInvertedScale) + 0.5f);
                            }
                            int surfaceWidth = this.mSurfaceFrame.right;
                            int surfaceHeight = this.mSurfaceFrame.bottom;
                            boolean realSizeChanged = this.mLastSurfaceWidth == surfaceWidth ? this.mLastSurfaceHeight != surfaceHeight : true;
                            this.mLastSurfaceWidth = surfaceWidth;
                            this.mLastSurfaceHeight = surfaceHeight;
                            this.mSurfaceLock.unlock();
                            redrawNeeded |= visible ? this.mDrawFinished ^ 1 : 0;
                            Callback[] callbacks = null;
                            boolean surfaceChanged = creating;
                            if (this.mSurfaceCreated && (surfaceChanged || (!visible && visibleChanged))) {
                                this.mSurfaceCreated = false;
                                if (this.mSurface.isValid()) {
                                    callbacks = getSurfaceCallbacks();
                                    for (Callback c : callbacks) {
                                        c.surfaceDestroyed(this.mSurfaceHolder);
                                    }
                                    if (this.mSurface.isValid()) {
                                        this.mSurface.forceScopedDisconnect();
                                    }
                                    LogPower.push(142);
                                }
                            }
                            if (creating) {
                                this.mSurface.copyFrom(this.mSurfaceControl);
                            }
                            if (sizeChanged && getContext().getApplicationInfo().targetSdkVersion < 26) {
                                this.mSurface.createFrom(this.mSurfaceControl);
                            }
                            if (visible && this.mSurface.isValid()) {
                                if (!this.mSurfaceCreated && (surfaceChanged || visibleChanged)) {
                                    this.mSurfaceCreated = true;
                                    this.mIsCreating = true;
                                    if (callbacks == null) {
                                        callbacks = getSurfaceCallbacks();
                                    }
                                    for (Callback c2 : callbacks) {
                                        c2.surfaceCreated(this.mSurfaceHolder);
                                    }
                                    LogPower.push(141);
                                }
                                if (creating || formatChanged || sizeChanged || visibleChanged || realSizeChanged) {
                                    if (callbacks == null) {
                                        callbacks = getSurfaceCallbacks();
                                    }
                                    for (Callback c22 : callbacks) {
                                        c22.surfaceChanged(this.mSurfaceHolder, this.mFormat, myWidth, myHeight);
                                    }
                                }
                                if (redrawNeeded) {
                                    if (callbacks == null) {
                                        callbacks = getSurfaceCallbacks();
                                    }
                                    this.mPendingReportDraws++;
                                    viewRoot.drawPending();
                                    new SurfaceCallbackHelper(new android.view.-$Lambda$P6MTGFSudLpwrqb6oVD8FdorW1c.AnonymousClass1(this)).dispatchSurfaceRedrawNeededAsync(this.mSurfaceHolder, callbacks);
                                }
                            }
                            this.mIsCreating = false;
                            if (!(this.mSurfaceControl == null || (this.mSurfaceCreated ^ 1) == 0)) {
                                this.mSurface.release();
                                if (!this.mWindowStopped) {
                                    this.mSurfaceControl.destroy();
                                    this.mSurfaceControl = null;
                                }
                            }
                        } catch (Throwable th) {
                            this.mSurfaceLock.unlock();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Exception configuring surface", ex);
                    } catch (Throwable th2) {
                        this.mIsCreating = false;
                        if (!(this.mSurfaceControl == null || (this.mSurfaceCreated ^ 1) == 0)) {
                            this.mSurface.release();
                            if (!this.mWindowStopped) {
                                this.mSurfaceControl.destroy();
                                this.mSurfaceControl = null;
                            }
                        }
                    }
                } else {
                    getLocationInSurface(this.mLocation);
                    boolean positionChanged = this.mWindowSpaceLeft == this.mLocation[0] ? this.mWindowSpaceTop != this.mLocation[1] : true;
                    boolean layoutSizeChanged = getWidth() == this.mScreenRect.width() ? getHeight() != this.mScreenRect.height() : true;
                    if (positionChanged || layoutSizeChanged) {
                        this.mWindowSpaceLeft = this.mLocation[0];
                        this.mWindowSpaceTop = this.mLocation[1];
                        this.mLocation[0] = getWidth();
                        this.mLocation[1] = getHeight();
                        this.mScreenRect.set(this.mWindowSpaceLeft, this.mWindowSpaceTop, this.mLocation[0], this.mLocation[1]);
                        if (getDisplay() != null && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
                            impl = getViewRootImpl();
                            if (!(impl == null || impl.mBasePackageName == null || !impl.mBasePackageName.equals(GALLERY))) {
                                this.mScreenRect.set(this.mWindowSpaceLeft, this.mWindowSpaceTop, this.mWindowSpaceLeft + this.mLocation[0], this.mWindowSpaceTop + this.mLocation[1]);
                            }
                        }
                        if (this.mTranslator != null) {
                            this.mTranslator.translateRectInAppWindowToScreen(this.mScreenRect);
                        }
                        if (this.mSurfaceControl != null) {
                            if (!(isHardwareAccelerated() && (this.mRtHandlingPositionUpdates ^ 1) == 0)) {
                                try {
                                    setParentSpaceRectangle(this.mScreenRect, -1);
                                } catch (Exception ex2) {
                                    Log.e(TAG, "Exception configuring surface", ex2);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void onDrawFinished() {
        if (this.mDeferredDestroySurfaceControl != null) {
            this.mDeferredDestroySurfaceControl.destroy();
            this.mDeferredDestroySurfaceControl = null;
        }
        runOnUiThread(new -$Lambda$P6MTGFSudLpwrqb6oVD8FdorW1c(this));
    }

    private void setParentSpaceRectangle(Rect position, long frameNumber) {
        ViewRootImpl viewRoot = getViewRootImpl();
        SurfaceControl.openTransaction();
        if (frameNumber > 0) {
            try {
                this.mSurfaceControl.deferTransactionUntil(viewRoot.mSurface, frameNumber);
            } catch (Throwable th) {
                SurfaceControl.closeTransaction();
            }
        }
        this.mSurfaceControl.setPosition((float) position.left, (float) position.top);
        this.mSurfaceControl.setMatrix(((float) position.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) position.height()) / ((float) this.mSurfaceHeight));
        if (getDisplay() != null && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
            Rect surfaceInsets = getParentSurfaceInsets();
            if (surfaceInsets == null) {
                surfaceInsets = new Rect();
            }
            ViewRootImpl impl = getViewRootImpl();
            if (!(impl == null || impl.mBasePackageName == null || !impl.mBasePackageName.equals(GALLERY))) {
                setWindowCrop(position, this.mSurfaceControl, surfaceInsets);
            }
        }
        SurfaceControl.closeTransaction();
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

    public final void surfacePositionLost_uiRtSync(long frameNumber) {
        this.mRTLastReportedPosition.setEmpty();
        if (this.mSurfaceControl != null && this.mRtHandlingPositionUpdates) {
            this.mRtHandlingPositionUpdates = false;
            if (!(this.mScreenRect.isEmpty() || (this.mScreenRect.equals(this.mRTLastReportedPosition) ^ 1) == 0)) {
                try {
                    setParentSpaceRectangle(this.mScreenRect, frameNumber);
                } catch (Exception ex) {
                    Log.e(TAG, "Exception configuring surface", ex);
                }
            }
        }
    }

    private Callback[] getSurfaceCallbacks() {
        Callback[] callbacks;
        synchronized (this.mCallbacks) {
            callbacks = new Callback[this.mCallbacks.size()];
            this.mCallbacks.toArray(callbacks);
        }
        return callbacks;
    }

    @Deprecated
    public void setWindowType(int type) {
        if (getContext().getApplicationInfo().targetSdkVersion >= 26) {
            throw new UnsupportedOperationException("SurfaceView#setWindowType() has never been a public API.");
        } else if (type == 1000) {
            Log.e(TAG, "If you are calling SurfaceView#setWindowType(TYPE_APPLICATION_PANEL) just to make the SurfaceView to be placed on top of its window, you must call setZOrderOnTop(true) instead.", new Throwable());
            setZOrderOnTop(true);
        } else {
            Log.e(TAG, "SurfaceView#setWindowType(int) is deprecated and now does nothing. type=" + type, new Throwable());
        }
    }

    private void runOnUiThread(Runnable runnable) {
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
}

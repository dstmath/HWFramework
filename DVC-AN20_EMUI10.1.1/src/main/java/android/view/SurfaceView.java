package android.view;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RenderNode;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import com.android.internal.view.SurfaceCallbackHelper;
import com.huawei.android.fsm.HwFoldScreenManager;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class SurfaceView extends View implements ViewRootImpl.WindowStoppedCallback {
    private static final String COLLABORATE_SURFACE_VIEW_TAG = "tag_collaborate_surface_view";
    private static final boolean DEBUG = false;
    private static final String NEW_GALLERY = "com.huawei.photos";
    private static final String OLD_GALLERY = "com.android.gallery3d";
    private static final String TAG = "SurfaceView";
    private boolean mAttachedToWindow;
    SurfaceControl mBackgroundControl;
    @UnsupportedAppUsage
    final ArrayList<SurfaceHolder.Callback> mCallbacks;
    final Configuration mConfiguration;
    float mCornerRadius;
    SurfaceControl mDeferredDestroySurfaceControl;
    boolean mDrawFinished;
    @UnsupportedAppUsage
    private final ViewTreeObserver.OnPreDrawListener mDrawListener;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    boolean mDrawingStopped;
    @UnsupportedAppUsage
    int mFormat;
    private boolean mGlobalListenersAdded;
    @UnsupportedAppUsage
    boolean mHaveFrame;
    private Matrix mHorizonMirrorMatrix;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    boolean mIsCreating;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    long mLastLockTime;
    int mLastSurfaceHeight;
    int mLastSurfaceWidth;
    boolean mLastWindowVisibility;
    final int[] mLocation;
    private int mPendingReportDraws;
    private RenderNode.PositionUpdateListener mPositionListener;
    private Rect mRTLastReportedPosition;
    @UnsupportedAppUsage
    int mRequestedFormat;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    int mRequestedHeight;
    boolean mRequestedVisible;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    int mRequestedWidth;
    Paint mRoundedViewportPaint;
    private volatile boolean mRtHandlingPositionUpdates;
    private SurfaceControl.Transaction mRtTransaction;
    final Rect mScreenRect;
    private final ViewTreeObserver.OnScrollChangedListener mScrollChangedListener;
    int mSubLayer;
    @UnsupportedAppUsage
    final Surface mSurface;
    SurfaceControl mSurfaceControl;
    boolean mSurfaceCreated;
    private int mSurfaceFlags;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    final Rect mSurfaceFrame;
    int mSurfaceHeight;
    @UnsupportedAppUsage
    private final SurfaceHolder mSurfaceHolder;
    @UnsupportedAppUsage
    final ReentrantLock mSurfaceLock;
    SurfaceSession mSurfaceSession;
    int mSurfaceWidth;
    final Rect mTmpRect;
    private CompatibilityInfo.Translator mTranslator;
    boolean mViewVisibility;
    boolean mVisible;
    int mWindowSpaceLeft;
    int mWindowSpaceTop;
    boolean mWindowStopped;
    boolean mWindowVisibility;

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
        this.mTmpRect = new Rect();
        this.mConfiguration = new Configuration();
        this.mSubLayer = -2;
        this.mIsCreating = false;
        this.mRtHandlingPositionUpdates = false;
        this.mScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            /* class android.view.SurfaceView.AnonymousClass1 */

            @Override // android.view.ViewTreeObserver.OnScrollChangedListener
            public void onScrollChanged() {
                SurfaceView.this.updateSurface();
            }
        };
        this.mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            /* class android.view.SurfaceView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                SurfaceView surfaceView = SurfaceView.this;
                surfaceView.mHaveFrame = surfaceView.getWidth() > 0 && SurfaceView.this.getHeight() > 0;
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
        this.mHorizonMirrorMatrix = new Matrix();
        this.mRtTransaction = new SurfaceControl.Transaction();
        this.mRTLastReportedPosition = new Rect();
        this.mPositionListener = new RenderNode.PositionUpdateListener() {
            /* class android.view.SurfaceView.AnonymousClass3 */

            @Override // android.graphics.RenderNode.PositionUpdateListener
            public void positionChanged(long frameNumber, int left, int top, int right, int bottom) {
                if (SurfaceView.this.mSurfaceControl != null) {
                    SurfaceView.this.mRtHandlingPositionUpdates = true;
                    if (SurfaceView.this.mRTLastReportedPosition.left != left || SurfaceView.this.mRTLastReportedPosition.top != top || SurfaceView.this.mRTLastReportedPosition.right != right || SurfaceView.this.mRTLastReportedPosition.bottom != bottom) {
                        try {
                            SurfaceView.this.mRTLastReportedPosition.set(left, top, right, bottom);
                            SurfaceView.this.setParentSpaceRectangle(SurfaceView.this.mRTLastReportedPosition, frameNumber);
                        } catch (Exception ex) {
                            Log.e(SurfaceView.TAG, "Exception from repositionChild", ex);
                        }
                    }
                }
            }

            @Override // android.graphics.RenderNode.PositionUpdateListener
            public void positionLost(long frameNumber) {
                SurfaceView.this.mRTLastReportedPosition.setEmpty();
                if (SurfaceView.this.mSurfaceControl != null) {
                    if (frameNumber > 0) {
                        SurfaceView.this.mRtTransaction.deferTransactionUntilSurface(SurfaceView.this.mSurfaceControl, SurfaceView.this.getViewRootImpl().mSurface, frameNumber);
                    }
                    SurfaceView.this.mRtTransaction.hide(SurfaceView.this.mSurfaceControl);
                    SurfaceView.this.mRtTransaction.apply();
                }
            }
        };
        this.mSurfaceHolder = new SurfaceHolder() {
            /* class android.view.SurfaceView.AnonymousClass4 */
            private static final String LOG_TAG = "SurfaceHolder";

            @Override // android.view.SurfaceHolder
            public boolean isCreating() {
                return SurfaceView.this.mIsCreating;
            }

            @Override // android.view.SurfaceHolder
            public void addCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    if (!SurfaceView.this.mCallbacks.contains(callback)) {
                        SurfaceView.this.mCallbacks.add(callback);
                    }
                }
            }

            @Override // android.view.SurfaceHolder
            public void removeCallback(SurfaceHolder.Callback callback) {
                synchronized (SurfaceView.this.mCallbacks) {
                    SurfaceView.this.mCallbacks.remove(callback);
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFixedSize(int width, int height) {
                if (SurfaceView.this.mRequestedWidth != width || SurfaceView.this.mRequestedHeight != height) {
                    SurfaceView surfaceView = SurfaceView.this;
                    surfaceView.mRequestedWidth = width;
                    surfaceView.mRequestedHeight = height;
                    surfaceView.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setSizeFromLayout() {
                if (SurfaceView.this.mRequestedWidth != -1 || SurfaceView.this.mRequestedHeight != -1) {
                    SurfaceView surfaceView = SurfaceView.this;
                    surfaceView.mRequestedHeight = -1;
                    surfaceView.mRequestedWidth = -1;
                    surfaceView.requestLayout();
                }
            }

            @Override // android.view.SurfaceHolder
            public void setFormat(int format) {
                if (format == -1) {
                    format = 4;
                }
                SurfaceView surfaceView = SurfaceView.this;
                surfaceView.mRequestedFormat = format;
                if (surfaceView.mSurfaceControl != null) {
                    SurfaceView.this.updateSurface();
                }
            }

            @Override // android.view.SurfaceHolder
            @Deprecated
            public void setType(int type) {
            }

            public /* synthetic */ void lambda$setKeepScreenOn$0$SurfaceView$4(boolean screenOn) {
                SurfaceView.this.setKeepScreenOn(screenOn);
            }

            @Override // android.view.SurfaceHolder
            public void setKeepScreenOn(boolean screenOn) {
                SurfaceView.this.runOnUiThread(new Runnable(screenOn) {
                    /* class android.view.$$Lambda$SurfaceView$4$wAwzCgpoBmqWbw6GlT0xJXSxjm4 */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        SurfaceView.AnonymousClass4.this.lambda$setKeepScreenOn$0$SurfaceView$4(this.f$1);
                    }
                });
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas() {
                return internalLockCanvas(null, false);
            }

            @Override // android.view.SurfaceHolder
            public Canvas lockCanvas(Rect inOutDirty) {
                return internalLockCanvas(inOutDirty, false);
            }

            @Override // android.view.SurfaceHolder
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
                SurfaceView surfaceView = SurfaceView.this;
                surfaceView.mLastLockTime = now;
                surfaceView.mSurfaceLock.unlock();
                return null;
            }

            @Override // android.view.SurfaceHolder
            public void unlockCanvasAndPost(Canvas canvas) {
                SurfaceView.this.mSurface.unlockCanvasAndPost(canvas);
                SurfaceView.this.mSurfaceLock.unlock();
                HwFrameworkFactory.getHwApsImpl().powerCtroll();
            }

            @Override // android.view.SurfaceHolder
            public Surface getSurface() {
                return SurfaceView.this.mSurface;
            }

            @Override // android.view.SurfaceHolder
            public Rect getSurfaceFrame() {
                return SurfaceView.this.mSurfaceFrame;
            }
        };
        this.mRenderNode.addPositionUpdateListener(this.mPositionListener);
        setWillNotDraw(true);
    }

    public SurfaceHolder getHolder() {
        return this.mSurfaceHolder;
    }

    private void updateRequestedVisibility() {
        this.mRequestedVisible = this.mViewVisibility && this.mWindowVisibility && !this.mWindowStopped;
    }

    @Override // android.view.ViewRootImpl.WindowStoppedCallback
    public void windowStopped(boolean stopped) {
        this.mWindowStopped = stopped;
        updateRequestedVisibility();
        updateSurface();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mWindowVisibility = visibility == 0;
        updateRequestedVisibility();
        updateSurface();
    }

    @Override // android.view.View
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean newRequestedVisible = true;
        this.mViewVisibility = visibility == 0;
        if (!this.mWindowVisibility || !this.mViewVisibility || this.mWindowStopped) {
            newRequestedVisible = false;
        }
        if (newRequestedVisible != this.mRequestedVisible) {
            requestLayout();
        }
        this.mRequestedVisible = newRequestedVisible;
        updateSurface();
    }

    /* access modifiers changed from: private */
    /* renamed from: performDrawFinished */
    public void lambda$onDrawFinished$0$SurfaceView() {
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
    @Override // android.view.View
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
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
        }
        this.mSurfaceControl = null;
        this.mHaveFrame = false;
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        int i = this.mRequestedWidth;
        if (i >= 0) {
            width = resolveSizeAndState(i, widthMeasureSpec, 0);
        } else {
            width = getDefaultSize(0, widthMeasureSpec);
        }
        int i2 = this.mRequestedHeight;
        if (i2 >= 0) {
            height = resolveSizeAndState(i2, heightMeasureSpec, 0);
        } else {
            height = getDefaultSize(0, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    @UnsupportedAppUsage
    public boolean setFrame(int left, int top, int right, int bottom) {
        boolean result = super.setFrame(left, top, right, bottom);
        updateSurface();
        return result;
    }

    @Override // android.view.View
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
                int[] iArr = this.mLocation;
                int l = iArr[0];
                int t = iArr[1];
                region.op(l, t, l + w, t + h, Region.Op.UNION);
            }
        }
        if (PixelFormat.formatHasAlpha(this.mRequestedFormat)) {
            return false;
        }
        return opaque;
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (this.mDrawFinished && !isAboveParent() && (this.mPrivateFlags & 128) == 0) {
            clearSurfaceViewPort(canvas);
        }
        super.draw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void dispatchDraw(Canvas canvas) {
        if (this.mDrawFinished && !isAboveParent() && (this.mPrivateFlags & 128) == 128) {
            clearSurfaceViewPort(canvas);
        }
        super.dispatchDraw(canvas);
    }

    private void clearSurfaceViewPort(Canvas canvas) {
        if (this.mCornerRadius > 0.0f) {
            canvas.getClipBounds(this.mTmpRect);
            float f = this.mCornerRadius;
            canvas.drawRoundRect((float) this.mTmpRect.left, (float) this.mTmpRect.top, (float) this.mTmpRect.right, (float) this.mTmpRect.bottom, f, f, this.mRoundedViewportPaint);
            return;
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
    }

    public void setCornerRadius(float cornerRadius) {
        this.mCornerRadius = cornerRadius;
        if (this.mCornerRadius > 0.0f && this.mRoundedViewportPaint == null) {
            this.mRoundedViewportPaint = new Paint(1);
            this.mRoundedViewportPaint.setBlendMode(BlendMode.CLEAR);
            this.mRoundedViewportPaint.setColor(0);
        }
        invalidate();
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

    private void updateBackgroundVisibilityInTransaction(SurfaceControl viewRoot) {
        SurfaceControl surfaceControl = this.mBackgroundControl;
        if (surfaceControl != null) {
            if (this.mSubLayer >= 0 || (this.mSurfaceFlags & 1024) == 0) {
                this.mBackgroundControl.hide();
                return;
            }
            surfaceControl.show();
            this.mBackgroundControl.setRelativeLayer(viewRoot, Integer.MIN_VALUE);
        }
    }

    private void releaseSurfaces() {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
            this.mSurfaceControl = null;
        }
        SurfaceControl surfaceControl2 = this.mBackgroundControl;
        if (surfaceControl2 != null) {
            surfaceControl2.remove();
            this.mBackgroundControl = null;
        }
    }

    /* JADX INFO: Multiple debug info for r0v8 int: [D('myWidth' int), D('myHeight' int)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x0449  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x049d A[Catch:{ all -> 0x0499, all -> 0x04a9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x04a1 A[Catch:{ all -> 0x0499, all -> 0x04a9 }] */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x04c9 A[SYNTHETIC, Splitter:B:235:0x04c9] */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x0551 A[Catch:{ all -> 0x0571 }] */
    /* JADX WARNING: Removed duplicated region for block: B:291:0x0588 A[Catch:{ Exception -> 0x0602 }] */
    /* JADX WARNING: Removed duplicated region for block: B:332:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    public void updateSurface() {
        ViewRootImpl viewRoot;
        boolean redrawNeeded;
        String str;
        boolean redrawNeeded2;
        int surfaceWidth;
        int surfaceHeight;
        boolean realSizeChanged;
        boolean z;
        boolean redrawNeeded3;
        if (this.mHaveFrame && (viewRoot = getViewRootImpl()) != null && viewRoot.mSurface != null && viewRoot.mSurface.isValid()) {
            this.mTranslator = viewRoot.mTranslator;
            CompatibilityInfo.Translator translator = this.mTranslator;
            if (translator != null) {
                this.mSurface.setCompatibilityTranslator(translator);
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
            boolean creating = (this.mSurfaceControl == null || formatChanged || visibleChanged) && this.mRequestedVisible;
            boolean sizeChanged = (this.mSurfaceWidth == myWidth && this.mSurfaceHeight == myHeight) ? false : true;
            boolean windowVisibleChanged = this.mWindowVisibility != this.mLastWindowVisibility;
            if (creating || formatChanged || sizeChanged || visibleChanged) {
                redrawNeeded = false;
            } else if (windowVisibleChanged) {
                redrawNeeded = false;
            } else {
                getLocationInSurface(this.mLocation);
                int i = this.mWindowSpaceLeft;
                int[] iArr = this.mLocation;
                boolean positionChanged = (i == iArr[0] && this.mWindowSpaceTop == iArr[1]) ? false : true;
                boolean layoutSizeChanged = (getWidth() == this.mScreenRect.width() && getHeight() == this.mScreenRect.height()) ? false : true;
                if (positionChanged || layoutSizeChanged) {
                    int[] iArr2 = this.mLocation;
                    this.mWindowSpaceLeft = iArr2[0];
                    this.mWindowSpaceTop = iArr2[1];
                    iArr2[0] = getWidth();
                    this.mLocation[1] = getHeight();
                    Rect rect = this.mScreenRect;
                    int i2 = this.mWindowSpaceLeft;
                    int i3 = this.mWindowSpaceTop;
                    int[] iArr3 = this.mLocation;
                    redrawNeeded3 = false;
                    rect.set(i2, i3, i2 + iArr3[0], iArr3[1] + i3);
                    if (getDisplay() != null && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
                        ViewRootImpl impl = getViewRootImpl();
                        if (impl != null && impl.mBasePackageName != null) {
                            if (impl.mBasePackageName.equals(NEW_GALLERY) || impl.mBasePackageName.equals(OLD_GALLERY)) {
                                Rect rect2 = this.mScreenRect;
                                int i4 = this.mWindowSpaceLeft;
                                int i5 = this.mWindowSpaceTop;
                                int[] iArr4 = this.mLocation;
                                rect2.set(i4, i5, i4 + iArr4[0], iArr4[1] + i5);
                            }
                        }
                    }
                    CompatibilityInfo.Translator translator2 = this.mTranslator;
                    if (translator2 != null) {
                        translator2.translateRectInAppWindowToScreen(this.mScreenRect);
                    }
                    if (this.mSurfaceControl == null) {
                        return;
                    }
                    if (!isHardwareAccelerated() || !this.mRtHandlingPositionUpdates) {
                        try {
                            setParentSpaceRectangle(this.mScreenRect, -1);
                        } catch (Exception ex) {
                            Log.e(TAG, "Exception configuring surface", ex);
                        }
                    }
                } else {
                    redrawNeeded3 = false;
                }
                return;
            }
            getLocationInWindow(this.mLocation);
            try {
                boolean z2 = this.mRequestedVisible;
                this.mVisible = z2;
                boolean visible = z2;
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
                    try {
                        this.mTranslator.translateRectInAppWindowToScreen(this.mScreenRect);
                    } catch (Exception e) {
                        ex = e;
                        str = "Exception configuring surface";
                    }
                }
                Rect surfaceInsets = getParentSurfaceInsets();
                this.mScreenRect.offset(surfaceInsets.left, surfaceInsets.top);
                if (creating) {
                    viewRoot.createBoundsSurface(this.mSubLayer);
                    this.mSurfaceSession = new SurfaceSession();
                    this.mDeferredDestroySurfaceControl = this.mSurfaceControl;
                    updateOpaqueFlag();
                    String name = "SurfaceView - " + viewRoot.getTitle().toString();
                    this.mSurfaceControl = new SurfaceControl.Builder(this.mSurfaceSession).setName(name).setOpaque((this.mSurfaceFlags & 1024) != 0).setBufferSize(this.mSurfaceWidth, this.mSurfaceHeight).setFormat(this.mFormat).setParent(viewRoot.getSurfaceControl()).setFlags(this.mSurfaceFlags).build();
                    this.mBackgroundControl = new SurfaceControl.Builder(this.mSurfaceSession).setName("Background for -" + name).setOpaque(true).setColorLayer().setParent(this.mSurfaceControl).build();
                } else if (this.mSurfaceControl == null) {
                    return;
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
                            }
                        } else {
                            this.mSurfaceControl.hide();
                        }
                        updateBackgroundVisibilityInTransaction(viewRoot.getSurfaceControl());
                        if (sizeChanged || creating || !this.mRtHandlingPositionUpdates) {
                            this.mSurfaceControl.setPosition((float) this.mScreenRect.left, (float) this.mScreenRect.top);
                            if (HwFoldScreenManager.isFoldable()) {
                                try {
                                    Object viewTag = getTag();
                                    if (viewTag != null && (viewTag instanceof String)) {
                                        if (((String) viewTag).equals(COLLABORATE_SURFACE_VIEW_TAG)) {
                                            try {
                                                this.mHorizonMirrorMatrix.setScale(-1.0f, 1.0f, ((float) this.mSurfaceWidth) / 2.0f, ((float) this.mSurfaceHeight) / 2.0f);
                                                this.mSurfaceControl.setMatrix(this.mHorizonMirrorMatrix, new float[9]);
                                                Log.i(TAG, " collaborate surfaceview updateSurface");
                                            } catch (Throwable th2) {
                                                th = th2;
                                                try {
                                                    SurfaceControl.closeTransaction();
                                                    throw th;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    this.mSurfaceLock.unlock();
                                                    throw th;
                                                }
                                            }
                                        }
                                    }
                                    this.mSurfaceControl.setMatrix(((float) this.mScreenRect.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) this.mScreenRect.height()) / ((float) this.mSurfaceHeight));
                                } catch (Throwable th4) {
                                    th = th4;
                                    SurfaceControl.closeTransaction();
                                    throw th;
                                }
                            } else {
                                try {
                                    this.mSurfaceControl.setMatrix(((float) this.mScreenRect.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) this.mScreenRect.height()) / ((float) this.mSurfaceHeight));
                                } catch (Throwable th5) {
                                    th = th5;
                                    SurfaceControl.closeTransaction();
                                    throw th;
                                }
                            }
                            this.mSurfaceControl.setWindowCrop(this.mSurfaceWidth, this.mSurfaceHeight);
                            CompatibilityInfo compatInfo = getResources().getCompatibilityInfo();
                            if (!compatInfo.supportsScreen()) {
                                this.mSurfaceControl.setLowResolutionInfo(compatInfo.getSdrLowResolutionRatio(), 2);
                            }
                        }
                        this.mSurfaceControl.setCornerRadius(this.mCornerRadius);
                        if (sizeChanged && !creating) {
                            this.mSurfaceControl.setBufferSize(this.mSurfaceWidth, this.mSurfaceHeight);
                        }
                        try {
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
                                    } catch (Throwable th6) {
                                        th = th6;
                                    }
                                } else {
                                    float appInvertedScale = this.mTranslator.applicationInvertedScale;
                                    this.mSurfaceFrame.right = (int) ((((float) this.mSurfaceWidth) * appInvertedScale) + 0.5f);
                                    this.mSurfaceFrame.bottom = (int) ((((float) this.mSurfaceHeight) * appInvertedScale) + 0.5f);
                                }
                                surfaceWidth = this.mSurfaceFrame.right;
                                surfaceHeight = this.mSurfaceFrame.bottom;
                                realSizeChanged = (this.mLastSurfaceWidth == surfaceWidth && this.mLastSurfaceHeight == surfaceHeight) ? false : true;
                            } catch (Throwable th7) {
                                th = th7;
                                this.mSurfaceLock.unlock();
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                        }
                        try {
                            this.mLastSurfaceWidth = surfaceWidth;
                            this.mLastSurfaceHeight = surfaceHeight;
                            try {
                                this.mSurfaceLock.unlock();
                                if (visible) {
                                    try {
                                        if (!this.mDrawFinished) {
                                            z = true;
                                            boolean redrawNeeded4 = redrawNeeded2 | z;
                                            SurfaceHolder.Callback[] callbacks = null;
                                            if (this.mSurfaceCreated) {
                                                str = "Exception configuring surface";
                                            } else if (creating || (!visible && visibleChanged)) {
                                                try {
                                                    this.mSurfaceCreated = false;
                                                    if (this.mSurface.isValid()) {
                                                        SurfaceHolder.Callback[] callbacks2 = getSurfaceCallbacks();
                                                        str = "Exception configuring surface";
                                                        int i6 = 0;
                                                        for (int length = callbacks2.length; i6 < length; length = length) {
                                                            callbacks2[i6].surfaceDestroyed(this.mSurfaceHolder);
                                                            i6++;
                                                            callbacks2 = callbacks2;
                                                        }
                                                        if (this.mSurface.isValid()) {
                                                            this.mSurface.forceScopedDisconnect();
                                                        }
                                                        LogPower.push(142);
                                                        callbacks = callbacks2;
                                                    } else {
                                                        str = "Exception configuring surface";
                                                    }
                                                } catch (Throwable th9) {
                                                    th = th9;
                                                    this.mIsCreating = false;
                                                    this.mSurface.release();
                                                    releaseSurfaces();
                                                    throw th;
                                                }
                                            } else {
                                                str = "Exception configuring surface";
                                            }
                                            if (creating) {
                                                this.mSurface.copyFrom(this.mSurfaceControl);
                                            }
                                            if (sizeChanged && getContext().getApplicationInfo().targetSdkVersion < 26) {
                                                this.mSurface.createFrom(this.mSurfaceControl);
                                            }
                                            if (visible) {
                                                try {
                                                    if (this.mSurface.isValid()) {
                                                        if (!this.mSurfaceCreated) {
                                                            if (creating || visibleChanged) {
                                                                try {
                                                                    this.mSurfaceCreated = true;
                                                                    this.mIsCreating = true;
                                                                    if (callbacks == null) {
                                                                        callbacks = getSurfaceCallbacks();
                                                                    }
                                                                    int length2 = callbacks.length;
                                                                    int i7 = 0;
                                                                    while (i7 < length2) {
                                                                        callbacks[i7].surfaceCreated(this.mSurfaceHolder);
                                                                        i7++;
                                                                        callbacks = callbacks;
                                                                        visible = visible;
                                                                    }
                                                                    LogPower.push(141);
                                                                    callbacks = callbacks;
                                                                } catch (Throwable th10) {
                                                                    th = th10;
                                                                    this.mIsCreating = false;
                                                                    this.mSurface.release();
                                                                    releaseSurfaces();
                                                                    throw th;
                                                                }
                                                            }
                                                        }
                                                        if (!creating && !formatChanged && !sizeChanged && !visibleChanged) {
                                                            if (!realSizeChanged) {
                                                                if (redrawNeeded4) {
                                                                    if (callbacks == null) {
                                                                        callbacks = getSurfaceCallbacks();
                                                                    }
                                                                    this.mPendingReportDraws++;
                                                                    viewRoot.drawPending();
                                                                    new SurfaceCallbackHelper(new Runnable() {
                                                                        /* class android.view.$$Lambda$SurfaceView$SyyzxOgxKwZMRgiiTGcRYbOU5JY */

                                                                        public final void run() {
                                                                            SurfaceView.this.onDrawFinished();
                                                                        }
                                                                    }).dispatchSurfaceRedrawNeededAsync(this.mSurfaceHolder, callbacks);
                                                                }
                                                                this.mIsCreating = false;
                                                                if (!(this.mSurfaceControl == null || this.mSurfaceCreated)) {
                                                                    this.mSurface.release();
                                                                    releaseSurfaces();
                                                                    return;
                                                                }
                                                                return;
                                                            }
                                                        }
                                                        if (callbacks == null) {
                                                            callbacks = getSurfaceCallbacks();
                                                        }
                                                        try {
                                                            int length3 = callbacks.length;
                                                            int i8 = 0;
                                                            while (i8 < length3) {
                                                                try {
                                                                    callbacks[i8].surfaceChanged(this.mSurfaceHolder, this.mFormat, myWidth, myHeight);
                                                                    i8++;
                                                                    callbacks = callbacks;
                                                                    formatChanged = formatChanged;
                                                                } catch (Throwable th11) {
                                                                    th = th11;
                                                                    this.mIsCreating = false;
                                                                    this.mSurface.release();
                                                                    releaseSurfaces();
                                                                    throw th;
                                                                }
                                                            }
                                                            if (redrawNeeded4) {
                                                            }
                                                            this.mIsCreating = false;
                                                            if (this.mSurfaceControl == null) {
                                                                return;
                                                            }
                                                            return;
                                                        } catch (Throwable th12) {
                                                            th = th12;
                                                            this.mIsCreating = false;
                                                            this.mSurface.release();
                                                            releaseSurfaces();
                                                            throw th;
                                                        }
                                                    }
                                                } catch (Throwable th13) {
                                                    th = th13;
                                                    this.mIsCreating = false;
                                                    this.mSurface.release();
                                                    releaseSurfaces();
                                                    throw th;
                                                }
                                            }
                                            this.mIsCreating = false;
                                            if (this.mSurfaceControl == null) {
                                            }
                                        }
                                    } catch (Throwable th14) {
                                        th = th14;
                                        this.mIsCreating = false;
                                        this.mSurface.release();
                                        releaseSurfaces();
                                        throw th;
                                    }
                                }
                                z = false;
                                boolean redrawNeeded42 = redrawNeeded2 | z;
                                SurfaceHolder.Callback[] callbacks3 = null;
                                try {
                                    if (this.mSurfaceCreated) {
                                    }
                                    if (creating) {
                                    }
                                    this.mSurface.createFrom(this.mSurfaceControl);
                                    if (visible) {
                                    }
                                } catch (Throwable th15) {
                                    th = th15;
                                    this.mIsCreating = false;
                                    this.mSurface.release();
                                    releaseSurfaces();
                                    throw th;
                                }
                            } catch (Exception e2) {
                                ex = e2;
                                str = "Exception configuring surface";
                            }
                        } catch (Throwable th16) {
                            th = th16;
                            this.mSurfaceLock.unlock();
                            throw th;
                        }
                    } catch (Throwable th17) {
                        th = th17;
                        SurfaceControl.closeTransaction();
                        throw th;
                    }
                } catch (Throwable th18) {
                    th = th18;
                    this.mSurfaceLock.unlock();
                    throw th;
                }
                try {
                    this.mIsCreating = false;
                    if (this.mSurfaceControl == null) {
                    }
                } catch (Exception e3) {
                    ex = e3;
                    Log.e(TAG, str, ex);
                }
            } catch (Exception e4) {
                ex = e4;
                str = "Exception configuring surface";
                Log.e(TAG, str, ex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onDrawFinished() {
        SurfaceControl surfaceControl = this.mDeferredDestroySurfaceControl;
        if (surfaceControl != null) {
            surfaceControl.remove();
            this.mDeferredDestroySurfaceControl = null;
        }
        runOnUiThread(new Runnable() {
            /* class android.view.$$Lambda$SurfaceView$Cs7TGTdA1lXf9qW8VOJAfEsMjdk */

            public final void run() {
                SurfaceView.this.lambda$onDrawFinished$0$SurfaceView();
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
        this.mRtTransaction.setPosition(surface, (float) position.left, (float) position.top);
        if (HwFoldScreenManager.isFoldable()) {
            Object viewTag = getTag();
            if (viewTag == null || !(viewTag instanceof String) || !((String) viewTag).equals(COLLABORATE_SURFACE_VIEW_TAG)) {
                this.mRtTransaction.setMatrix(surface, ((float) position.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) position.height()) / ((float) this.mSurfaceHeight));
            } else {
                this.mHorizonMirrorMatrix.setScale(-1.0f, 1.0f, ((float) this.mSurfaceWidth) / 2.0f, ((float) this.mSurfaceHeight) / 2.0f);
                this.mRtTransaction.setMatrix(surface, this.mHorizonMirrorMatrix, new float[9]);
                Log.i(TAG, " collaborate surfaceview applySurfaceTransforms");
            }
        } else {
            this.mRtTransaction.setMatrix(surface, ((float) position.width()) / ((float) this.mSurfaceWidth), 0.0f, 0.0f, ((float) position.height()) / ((float) this.mSurfaceHeight));
        }
        if (this.mViewVisibility) {
            this.mRtTransaction.show(surface);
        }
        if (getDisplay() != null && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
            setWindowCrop(position, this.mRtTransaction, surface, getParentSurfaceInsets());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setParentSpaceRectangle(Rect position, long frameNumber) {
        ViewRootImpl viewRoot = getViewRootImpl();
        applySurfaceTransforms(this.mSurfaceControl, position, frameNumber);
        applyChildSurfaceTransaction_renderWorker(this.mRtTransaction, viewRoot.mSurface, frameNumber);
        this.mRtTransaction.apply();
    }

    private void setWindowCrop(Rect position, SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect surfaceInsets) {
        if (surfaceInsets != null) {
            this.mTmpRect.set(0, 0, this.mSurfaceWidth, this.mSurfaceHeight);
            if (position.left < surfaceInsets.left) {
                if (position.left + this.mSurfaceWidth > surfaceInsets.left) {
                    this.mTmpRect.left = surfaceInsets.left - position.left;
                    this.mTmpRect.right = this.mSurfaceWidth;
                } else {
                    Rect rect = this.mTmpRect;
                    rect.left = 0;
                    rect.right = this.mSurfaceWidth - surfaceInsets.left;
                }
            }
            transaction.setWindowCrop(surfaceControl, this.mTmpRect);
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
    /* access modifiers changed from: public */
    private void runOnUiThread(Runnable runnable) {
        Handler handler = getHandler();
        if (handler == null || handler.getLooper() == Looper.myLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    @UnsupportedAppUsage
    public boolean isFixedSize() {
        return (this.mRequestedWidth == -1 && this.mRequestedHeight == -1) ? false : true;
    }

    private boolean isAboveParent() {
        return this.mSubLayer >= 0;
    }

    public void setResizeBackgroundColor(int bgColor) {
        if (this.mBackgroundControl != null) {
            float[] colorComponents = {((float) Color.red(bgColor)) / 255.0f, ((float) Color.green(bgColor)) / 255.0f, ((float) Color.blue(bgColor)) / 255.0f};
            SurfaceControl.openTransaction();
            try {
                this.mBackgroundControl.setColor(colorComponents);
            } finally {
                SurfaceControl.closeTransaction();
            }
        }
    }

    public SurfaceControl getSurfaceControl() {
        return this.mSurfaceControl;
    }
}

package com.android.server.wm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.HwPCUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.MagnificationSpec;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.SurfaceControl;
import android.view.ViewConfiguration;
import android.view.WindowInfo;
import android.view.WindowManager;
import android.view.WindowManagerInternal.MagnificationCallbacks;
import android.view.WindowManagerInternal.WindowsForAccessibilityCallback;
import android.view.WindowManagerPolicy;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.os.SomeArgs;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.wm.-$Lambda$VrxrRGaWeDA63X9yoVs2zDEaoRI.AnonymousClass1;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

final class AccessibilityController {
    private static final float[] sTempFloats = new float[9];
    private DisplayMagnifier mDisplayMagnifier;
    private final WindowManagerService mWindowManagerService;
    private WindowsForAccessibilityObserver mWindowsForAccessibilityObserver;

    private static final class DisplayMagnifier {
        private static final boolean DEBUG_LAYERS = false;
        private static final boolean DEBUG_RECTANGLE_REQUESTED = false;
        private static final boolean DEBUG_ROTATION = false;
        private static final boolean DEBUG_VIEWPORT_WINDOW = false;
        private static final boolean DEBUG_WINDOW_TRANSITIONS = false;
        private static final String LOG_TAG = "WindowManager";
        private final MagnificationCallbacks mCallbacks;
        private final Context mContext;
        private boolean mForceShowMagnifiableBounds = false;
        private final Handler mHandler;
        private final long mLongAnimationDuration;
        private final MagnifiedViewport mMagnifedViewport;
        private final Rect mTempRect1 = new Rect();
        private final Rect mTempRect2 = new Rect();
        private final Region mTempRegion1 = new Region();
        private final Region mTempRegion2 = new Region();
        private final Region mTempRegion3 = new Region();
        private final Region mTempRegion4 = new Region();
        private final WindowManagerService mWindowManagerService;

        private final class MagnifiedViewport {
            private final float mBorderWidth;
            private final Path mCircularPath;
            private final int mDrawBorderInset;
            private boolean mFullRedrawNeeded;
            private final int mHalfBorderWidth;
            private final Region mMagnificationRegion = new Region();
            private final MagnificationSpec mMagnificationSpec = MagnificationSpec.obtain();
            private final Region mOldMagnificationRegion = new Region();
            private final Matrix mTempMatrix = new Matrix();
            private final Point mTempPoint = new Point();
            private final RectF mTempRectF = new RectF();
            private final SparseArray<WindowState> mTempWindowStates = new SparseArray();
            private final ViewportWindow mWindow;
            private final WindowManager mWindowManager;

            private final class ViewportWindow {
                private static final String SURFACE_TITLE = "Magnification Overlay";
                private int mAlpha;
                private final AnimationController mAnimationController;
                private final Region mBounds = new Region();
                private Context mContext = null;
                private final Rect mDirtyRect = new Rect();
                private boolean mInvalidated;
                private boolean mLastInPCMode = false;
                private final Paint mPaint = new Paint();
                private boolean mShown;
                private final Surface mSurface = new Surface();
                private final SurfaceControl mSurfaceControl;

                private final class AnimationController extends Handler {
                    private static final int MAX_ALPHA = 255;
                    private static final int MIN_ALPHA = 0;
                    private static final int MSG_FRAME_SHOWN_STATE_CHANGED = 1;
                    private static final String PROPERTY_NAME_ALPHA = "alpha";
                    private final ValueAnimator mShowHideFrameAnimator;

                    public AnimationController(Context context, Looper looper) {
                        super(looper);
                        this.mShowHideFrameAnimator = ObjectAnimator.ofInt(ViewportWindow.this, PROPERTY_NAME_ALPHA, new int[]{0, 255});
                        long longAnimationDuration = (long) context.getResources().getInteger(17694722);
                        this.mShowHideFrameAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
                        this.mShowHideFrameAnimator.setDuration(longAnimationDuration);
                    }

                    public void onFrameShownStateChanged(boolean shown, boolean animate) {
                        int i;
                        int i2 = 0;
                        if (shown) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        if (animate) {
                            i2 = 1;
                        }
                        obtainMessage(1, i, i2).sendToTarget();
                    }

                    public void handleMessage(Message message) {
                        switch (message.what) {
                            case 1:
                                boolean shown = message.arg1 == 1;
                                if (!(message.arg2 == 1)) {
                                    this.mShowHideFrameAnimator.cancel();
                                    if (shown) {
                                        ViewportWindow.this.setAlpha(255);
                                        return;
                                    } else {
                                        ViewportWindow.this.setAlpha(0);
                                        return;
                                    }
                                } else if (this.mShowHideFrameAnimator.isRunning()) {
                                    this.mShowHideFrameAnimator.reverse();
                                    return;
                                } else if (shown) {
                                    this.mShowHideFrameAnimator.start();
                                    return;
                                } else {
                                    this.mShowHideFrameAnimator.reverse();
                                    return;
                                }
                            default:
                                return;
                        }
                    }
                }

                public ViewportWindow(Context context) {
                    SurfaceControl surfaceControl;
                    try {
                        MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getRealSize(MagnifiedViewport.this.mTempPoint);
                        surfaceControl = new SurfaceControl(DisplayMagnifier.this.mWindowManagerService.mFxSession, SURFACE_TITLE, MagnifiedViewport.this.mTempPoint.x, MagnifiedViewport.this.mTempPoint.y, -3, 4);
                    } catch (OutOfResourcesException e) {
                        surfaceControl = null;
                    }
                    this.mSurfaceControl = surfaceControl;
                    this.mSurfaceControl.setLayerStack(MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getLayerStack());
                    this.mSurfaceControl.setLayer(DisplayMagnifier.this.mWindowManagerService.mPolicy.getWindowLayerFromTypeLw(2027) * 10000);
                    this.mSurfaceControl.setPosition(0.0f, 0.0f);
                    this.mSurface.copyFrom(this.mSurfaceControl);
                    this.mAnimationController = new AnimationController(context, DisplayMagnifier.this.mWindowManagerService.mH.getLooper());
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(16843664, typedValue, true);
                    int borderColor = context.getColor(typedValue.resourceId);
                    this.mPaint.setStyle(Style.STROKE);
                    this.mPaint.setStrokeWidth(MagnifiedViewport.this.mBorderWidth);
                    this.mPaint.setColor(borderColor);
                    this.mInvalidated = true;
                    this.mContext = context;
                }

                public void setShown(boolean shown, boolean animate) {
                    synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mShown == shown) {
                            } else {
                                this.mShown = shown;
                                this.mAnimationController.onFrameShownStateChanged(shown, animate);
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }

                public int getAlpha() {
                    int i;
                    synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            i = this.mAlpha;
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                    return i;
                }

                public void setAlpha(int alpha) {
                    synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mAlpha == alpha) {
                            } else {
                                this.mAlpha = alpha;
                                invalidate(null);
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }

                public void setBounds(Region bounds) {
                    synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mBounds.equals(bounds)) {
                            } else {
                                this.mBounds.set(bounds);
                                invalidate(this.mDirtyRect);
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }

                public void updateSize() {
                    synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getRealSize(MagnifiedViewport.this.mTempPoint);
                            this.mSurfaceControl.setSize(MagnifiedViewport.this.mTempPoint.x, MagnifiedViewport.this.mTempPoint.y);
                            invalidate(this.mDirtyRect);
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }

                public void invalidate(Rect dirtyRect) {
                    if (dirtyRect != null) {
                        this.mDirtyRect.set(dirtyRect);
                    } else {
                        this.mDirtyRect.setEmpty();
                    }
                    this.mInvalidated = true;
                    DisplayMagnifier.this.mWindowManagerService.scheduleAnimationLocked();
                }

                private void setLayerStackForPC() {
                    if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() != this.mLastInPCMode) {
                        if (HwPCUtils.isPcCastModeInServer()) {
                            DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService("display");
                            Display display = null;
                            if (displayManager != null) {
                                display = displayManager.getDisplay(HwPCUtils.getPCDisplayID());
                            }
                            if (display != null) {
                                HwPCUtils.log(AccessibilityController.class.getName(), "set pc display layerstack:" + display);
                                this.mSurfaceControl.setLayerStack(display.getLayerStack());
                            } else {
                                this.mSurfaceControl.setLayerStack(MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getLayerStack());
                            }
                        } else {
                            this.mSurfaceControl.setLayerStack(MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getLayerStack());
                        }
                        this.mLastInPCMode = HwPCUtils.isPcCastModeInServer();
                    }
                }

                /* JADX WARNING: Missing block: B:24:0x0076, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
                /* JADX WARNING: Missing block: B:25:0x0079, code:
            return;
     */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void drawIfNeeded() {
                    synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mInvalidated) {
                                setLayerStackForPC();
                                this.mInvalidated = false;
                                Canvas canvas = null;
                                try {
                                    if (this.mDirtyRect.isEmpty()) {
                                        this.mBounds.getBounds(this.mDirtyRect);
                                    }
                                    this.mDirtyRect.inset(-MagnifiedViewport.this.mHalfBorderWidth, -MagnifiedViewport.this.mHalfBorderWidth);
                                    canvas = this.mSurface.lockCanvas(this.mDirtyRect);
                                } catch (IllegalArgumentException e) {
                                } catch (OutOfResourcesException e2) {
                                }
                                if (canvas == null) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                canvas.drawColor(0, Mode.CLEAR);
                                this.mPaint.setAlpha(this.mAlpha);
                                canvas.drawPath(this.mBounds.getBoundaryPath(), this.mPaint);
                                this.mSurface.unlockCanvasAndPost(canvas);
                                if (this.mAlpha > 0) {
                                    this.mSurfaceControl.show();
                                } else {
                                    this.mSurfaceControl.hide();
                                }
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }

                public void releaseSurface() {
                    this.mSurfaceControl.release();
                    this.mSurface.release();
                }
            }

            public MagnifiedViewport() {
                this.mWindowManager = (WindowManager) DisplayMagnifier.this.mContext.getSystemService("window");
                this.mBorderWidth = DisplayMagnifier.this.mContext.getResources().getDimension(17104903);
                this.mHalfBorderWidth = (int) Math.ceil((double) (this.mBorderWidth / 2.0f));
                this.mDrawBorderInset = ((int) this.mBorderWidth) / 2;
                this.mWindow = new ViewportWindow(DisplayMagnifier.this.mContext);
                if (DisplayMagnifier.this.mContext.getResources().getConfiguration().isScreenRound()) {
                    this.mCircularPath = new Path();
                    this.mWindowManager.getDefaultDisplay().getRealSize(this.mTempPoint);
                    int centerXY = this.mTempPoint.x / 2;
                    this.mCircularPath.addCircle((float) centerXY, (float) centerXY, (float) centerXY, Direction.CW);
                } else {
                    this.mCircularPath = null;
                }
                recomputeBoundsLocked();
            }

            public void getMagnificationRegionLocked(Region outMagnificationRegion) {
                outMagnificationRegion.set(this.mMagnificationRegion);
            }

            public void updateMagnificationSpecLocked(MagnificationSpec spec) {
                if (spec != null) {
                    this.mMagnificationSpec.initialize(spec.scale, spec.offsetX, spec.offsetY);
                } else {
                    this.mMagnificationSpec.clear();
                }
                if (!DisplayMagnifier.this.mHandler.hasMessages(5)) {
                    boolean z;
                    if (isMagnifyingLocked()) {
                        z = true;
                    } else {
                        z = DisplayMagnifier.this.isForceShowingMagnifiableBoundsLocked();
                    }
                    setMagnifiedRegionBorderShownLocked(z, true);
                }
            }

            public void recomputeBoundsLocked() {
                this.mWindowManager.getDefaultDisplay().getRealSize(this.mTempPoint);
                int screenWidth = this.mTempPoint.x;
                int screenHeight = this.mTempPoint.y;
                this.mMagnificationRegion.set(0, 0, 0, 0);
                Region availableBounds = DisplayMagnifier.this.mTempRegion1;
                availableBounds.set(0, 0, screenWidth, screenHeight);
                if (this.mCircularPath != null) {
                    availableBounds.setPath(this.mCircularPath, availableBounds);
                }
                Region nonMagnifiedBounds = DisplayMagnifier.this.mTempRegion4;
                nonMagnifiedBounds.set(0, 0, 0, 0);
                SparseArray<WindowState> visibleWindows = this.mTempWindowStates;
                visibleWindows.clear();
                populateWindowsOnScreenLocked(visibleWindows);
                for (int i = visibleWindows.size() - 1; i >= 0; i--) {
                    WindowState windowState = (WindowState) visibleWindows.valueAt(i);
                    if (windowState.mAttrs.type != 2027) {
                        Matrix matrix = this.mTempMatrix;
                        AccessibilityController.populateTransformationMatrixLocked(windowState, matrix);
                        Region touchableRegion = DisplayMagnifier.this.mTempRegion3;
                        windowState.getTouchableRegion(touchableRegion);
                        Rect touchableFrame = DisplayMagnifier.this.mTempRect1;
                        touchableRegion.getBounds(touchableFrame);
                        RectF windowFrame = this.mTempRectF;
                        windowFrame.set(touchableFrame);
                        windowFrame.offset((float) (-windowState.mFrame.left), (float) (-windowState.mFrame.top));
                        matrix.mapRect(windowFrame);
                        Region windowBounds = DisplayMagnifier.this.mTempRegion2;
                        windowBounds.set((int) windowFrame.left, (int) windowFrame.top, (int) windowFrame.right, (int) windowFrame.bottom);
                        Region portionOfWindowAlreadyAccountedFor = DisplayMagnifier.this.mTempRegion3;
                        portionOfWindowAlreadyAccountedFor.set(this.mMagnificationRegion);
                        portionOfWindowAlreadyAccountedFor.op(nonMagnifiedBounds, Op.UNION);
                        windowBounds.op(portionOfWindowAlreadyAccountedFor, Op.DIFFERENCE);
                        if (DisplayMagnifier.this.mWindowManagerService.mPolicy.canMagnifyWindow(windowState.mAttrs.type)) {
                            this.mMagnificationRegion.op(windowBounds, Op.UNION);
                            this.mMagnificationRegion.op(availableBounds, Op.INTERSECT);
                        } else {
                            nonMagnifiedBounds.op(windowBounds, Op.UNION);
                            availableBounds.op(windowBounds, Op.DIFFERENCE);
                        }
                        Region accountedBounds = DisplayMagnifier.this.mTempRegion2;
                        accountedBounds.set(this.mMagnificationRegion);
                        accountedBounds.op(nonMagnifiedBounds, Op.UNION);
                        accountedBounds.op(0, 0, screenWidth, screenHeight, Op.INTERSECT);
                        if (accountedBounds.isRect()) {
                            Rect accountedFrame = DisplayMagnifier.this.mTempRect1;
                            accountedBounds.getBounds(accountedFrame);
                            if (accountedFrame.width() == screenWidth && accountedFrame.height() == screenHeight) {
                                break;
                            }
                        }
                        continue;
                    }
                }
                visibleWindows.clear();
                this.mMagnificationRegion.op(this.mDrawBorderInset, this.mDrawBorderInset, screenWidth - this.mDrawBorderInset, screenHeight - this.mDrawBorderInset, Op.INTERSECT);
                if (this.mOldMagnificationRegion.equals(this.mMagnificationRegion) ^ 1) {
                    this.mWindow.setBounds(this.mMagnificationRegion);
                    Rect dirtyRect = DisplayMagnifier.this.mTempRect1;
                    if (this.mFullRedrawNeeded) {
                        this.mFullRedrawNeeded = false;
                        dirtyRect.set(this.mDrawBorderInset, this.mDrawBorderInset, screenWidth - this.mDrawBorderInset, screenHeight - this.mDrawBorderInset);
                        this.mWindow.invalidate(dirtyRect);
                    } else {
                        Region dirtyRegion = DisplayMagnifier.this.mTempRegion3;
                        dirtyRegion.set(this.mMagnificationRegion);
                        dirtyRegion.op(this.mOldMagnificationRegion, Op.UNION);
                        dirtyRegion.op(nonMagnifiedBounds, Op.INTERSECT);
                        dirtyRegion.getBounds(dirtyRect);
                        this.mWindow.invalidate(dirtyRect);
                    }
                    this.mOldMagnificationRegion.set(this.mMagnificationRegion);
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = Region.obtain(this.mMagnificationRegion);
                    DisplayMagnifier.this.mHandler.obtainMessage(1, args).sendToTarget();
                }
            }

            public void onRotationChangedLocked() {
                if (isMagnifyingLocked() || DisplayMagnifier.this.isForceShowingMagnifiableBoundsLocked()) {
                    setMagnifiedRegionBorderShownLocked(false, false);
                    long delay = (long) (((float) DisplayMagnifier.this.mLongAnimationDuration) * DisplayMagnifier.this.mWindowManagerService.getWindowAnimationScaleLocked());
                    DisplayMagnifier.this.mHandler.sendMessageDelayed(DisplayMagnifier.this.mHandler.obtainMessage(5), delay);
                }
                recomputeBoundsLocked();
                this.mWindow.updateSize();
            }

            public void setMagnifiedRegionBorderShownLocked(boolean shown, boolean animate) {
                if (shown) {
                    this.mFullRedrawNeeded = true;
                    this.mOldMagnificationRegion.set(0, 0, 0, 0);
                }
                this.mWindow.setShown(shown, animate);
            }

            public void getMagnifiedFrameInContentCoordsLocked(Rect rect) {
                MagnificationSpec spec = this.mMagnificationSpec;
                this.mMagnificationRegion.getBounds(rect);
                rect.offset((int) (-spec.offsetX), (int) (-spec.offsetY));
                rect.scale(1.0f / spec.scale);
            }

            public boolean isMagnifyingLocked() {
                return this.mMagnificationSpec.scale > 1.0f;
            }

            public MagnificationSpec getMagnificationSpecLocked() {
                return this.mMagnificationSpec;
            }

            public void drawWindowIfNeededLocked() {
                recomputeBoundsLocked();
                this.mWindow.drawIfNeeded();
            }

            public void destroyWindow() {
                this.mWindow.releaseSurface();
            }

            private void populateWindowsOnScreenLocked(SparseArray<WindowState> outWindows) {
                DisplayContent dc = DisplayMagnifier.this.mWindowManagerService.getDefaultDisplayContentLocked();
                if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && DisplayMagnifier.this.mWindowManagerService.mRoot != null) {
                    dc = DisplayMagnifier.this.mWindowManagerService.mRoot.getDisplayContent(HwPCUtils.getPCDisplayID());
                }
                dc.forAllWindows((Consumer) new -$Lambda$VrxrRGaWeDA63X9yoVs2zDEaoRI(outWindows), false);
            }

            static /* synthetic */ void lambda$-com_android_server_wm_AccessibilityController$DisplayMagnifier$MagnifiedViewport_30455(SparseArray outWindows, WindowState w) {
                if (w.isOnScreen() && w.isVisibleLw() && (w.mWinAnimator.mEnterAnimationPending ^ 1) != 0) {
                    outWindows.put(w.mLayer, w);
                }
            }
        }

        private class MyHandler extends Handler {
            public static final int MESSAGE_NOTIFY_MAGNIFICATION_REGION_CHANGED = 1;
            public static final int MESSAGE_NOTIFY_RECTANGLE_ON_SCREEN_REQUESTED = 2;
            public static final int MESSAGE_NOTIFY_ROTATION_CHANGED = 4;
            public static final int MESSAGE_NOTIFY_USER_CONTEXT_CHANGED = 3;
            public static final int MESSAGE_SHOW_MAGNIFIED_REGION_BOUNDS_IF_NEEDED = 5;

            public MyHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        Region magnifiedBounds = message.obj.arg1;
                        DisplayMagnifier.this.mCallbacks.onMagnificationRegionChanged(magnifiedBounds);
                        magnifiedBounds.recycle();
                        return;
                    case 2:
                        SomeArgs args = (SomeArgs) message.obj;
                        DisplayMagnifier.this.mCallbacks.onRectangleOnScreenRequested(args.argi1, args.argi2, args.argi3, args.argi4);
                        args.recycle();
                        return;
                    case 3:
                        DisplayMagnifier.this.mCallbacks.onUserContextChanged();
                        return;
                    case 4:
                        DisplayMagnifier.this.mCallbacks.onRotationChanged(message.arg1);
                        return;
                    case 5:
                        synchronized (DisplayMagnifier.this.mWindowManagerService.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                if (DisplayMagnifier.this.mMagnifedViewport.isMagnifyingLocked() || DisplayMagnifier.this.isForceShowingMagnifiableBoundsLocked()) {
                                    DisplayMagnifier.this.mMagnifedViewport.setMagnifiedRegionBorderShownLocked(true, true);
                                    DisplayMagnifier.this.mWindowManagerService.scheduleAnimationLocked();
                                }
                            } finally {
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        }
                        return;
                    default:
                        return;
                }
            }
        }

        public DisplayMagnifier(WindowManagerService windowManagerService, MagnificationCallbacks callbacks) {
            this.mContext = windowManagerService.mContext;
            this.mWindowManagerService = windowManagerService;
            this.mCallbacks = callbacks;
            this.mHandler = new MyHandler(this.mWindowManagerService.mH.getLooper());
            this.mMagnifedViewport = new MagnifiedViewport();
            this.mLongAnimationDuration = (long) this.mContext.getResources().getInteger(17694722);
        }

        public void setMagnificationSpecLocked(MagnificationSpec spec) {
            this.mMagnifedViewport.updateMagnificationSpecLocked(spec);
            this.mMagnifedViewport.recomputeBoundsLocked();
            this.mWindowManagerService.scheduleAnimationLocked();
        }

        public void setForceShowMagnifiableBoundsLocked(boolean show) {
            this.mForceShowMagnifiableBounds = show;
            this.mMagnifedViewport.setMagnifiedRegionBorderShownLocked(show, true);
        }

        public boolean isForceShowingMagnifiableBoundsLocked() {
            return this.mForceShowMagnifiableBounds;
        }

        public void onRectangleOnScreenRequestedLocked(Rect rectangle) {
            if (this.mMagnifedViewport.isMagnifyingLocked()) {
                Rect magnifiedRegionBounds = this.mTempRect2;
                this.mMagnifedViewport.getMagnifiedFrameInContentCoordsLocked(magnifiedRegionBounds);
                if (!magnifiedRegionBounds.contains(rectangle)) {
                    SomeArgs args = SomeArgs.obtain();
                    args.argi1 = rectangle.left;
                    args.argi2 = rectangle.top;
                    args.argi3 = rectangle.right;
                    args.argi4 = rectangle.bottom;
                    this.mHandler.obtainMessage(2, args).sendToTarget();
                }
            }
        }

        public void onWindowLayersChangedLocked() {
            this.mMagnifedViewport.recomputeBoundsLocked();
            this.mWindowManagerService.scheduleAnimationLocked();
        }

        public void onRotationChangedLocked(DisplayContent displayContent) {
            this.mMagnifedViewport.onRotationChangedLocked();
            this.mHandler.sendEmptyMessage(4);
        }

        public void onAppWindowTransitionLocked(WindowState windowState, int transition) {
            if (this.mMagnifedViewport.isMagnifyingLocked()) {
                switch (transition) {
                    case 6:
                    case 8:
                    case 10:
                    case 12:
                    case 13:
                    case 14:
                        this.mHandler.sendEmptyMessage(3);
                        return;
                    default:
                        return;
                }
            }
        }

        public void onWindowTransitionLocked(WindowState windowState, int transition) {
            boolean magnifying = this.mMagnifedViewport.isMagnifyingLocked();
            int type = windowState.mAttrs.type;
            switch (transition) {
                case 1:
                case 3:
                    if (magnifying) {
                        switch (type) {
                            case 2:
                            case 4:
                            case 1000:
                            case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE /*1001*/:
                            case 1002:
                            case 1003:
                            case 1005:
                            case 2001:
                            case 2002:
                            case 2003:
                            case 2005:
                            case 2006:
                            case 2007:
                            case 2008:
                            case 2009:
                            case 2010:
                            case 2020:
                            case 2024:
                            case 2035:
                            case 2038:
                                Rect magnifiedRegionBounds = this.mTempRect2;
                                this.mMagnifedViewport.getMagnifiedFrameInContentCoordsLocked(magnifiedRegionBounds);
                                Rect touchableRegionBounds = this.mTempRect1;
                                windowState.getTouchableRegion(this.mTempRegion1);
                                this.mTempRegion1.getBounds(touchableRegionBounds);
                                if (!magnifiedRegionBounds.intersect(touchableRegionBounds)) {
                                    this.mCallbacks.onRectangleOnScreenRequested(touchableRegionBounds.left, touchableRegionBounds.top, touchableRegionBounds.right, touchableRegionBounds.bottom);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    }
                    return;
                default:
                    return;
            }
        }

        public MagnificationSpec getMagnificationSpecForWindowLocked(WindowState windowState) {
            MagnificationSpec spec = this.mMagnifedViewport.getMagnificationSpecLocked();
            if (!(spec == null || (spec.isNop() ^ 1) == 0)) {
                WindowManagerPolicy policy = this.mWindowManagerService.mPolicy;
                int windowType = windowState.mAttrs.type;
                if ((policy.isTopLevelWindow(windowType) || !windowState.isChildWindow() || (policy.canMagnifyWindow(windowType) ^ 1) == 0) && policy.canMagnifyWindow(windowState.mAttrs.type)) {
                    return spec;
                }
                return null;
            }
            return spec;
        }

        public void getMagnificationRegionLocked(Region outMagnificationRegion) {
            this.mMagnifedViewport.getMagnificationRegionLocked(outMagnificationRegion);
        }

        public void destroyLocked() {
            this.mMagnifedViewport.destroyWindow();
        }

        public void showMagnificationBoundsIfNeeded() {
            this.mHandler.obtainMessage(5).sendToTarget();
        }

        public void drawMagnifiedRegionBorderIfNeededLocked() {
            this.mMagnifedViewport.drawWindowIfNeededLocked();
        }
    }

    private static final class WindowsForAccessibilityObserver {
        private static final boolean DEBUG = false;
        private static final String LOG_TAG = "WindowManager";
        private final WindowsForAccessibilityCallback mCallback;
        private final Context mContext;
        private final Handler mHandler;
        private final List<WindowInfo> mOldWindows = new ArrayList();
        private final long mRecurringAccessibilityEventsIntervalMillis;
        private final Set<IBinder> mTempBinderSet = new ArraySet();
        private final Matrix mTempMatrix = new Matrix();
        private final Point mTempPoint = new Point();
        private final Rect mTempRect = new Rect();
        private final RectF mTempRectF = new RectF();
        private final Region mTempRegion = new Region();
        private final Region mTempRegion1 = new Region();
        private final SparseArray<WindowState> mTempWindowStates = new SparseArray();
        private final WindowManagerService mWindowManagerService;

        private class MyHandler extends Handler {
            public static final int MESSAGE_COMPUTE_CHANGED_WINDOWS = 1;

            public MyHandler(Looper looper) {
                super(looper, null, false);
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        WindowsForAccessibilityObserver.this.computeChangedWindows();
                        return;
                    default:
                        return;
                }
            }
        }

        public WindowsForAccessibilityObserver(WindowManagerService windowManagerService, WindowsForAccessibilityCallback callback) {
            this.mContext = windowManagerService.mContext;
            this.mWindowManagerService = windowManagerService;
            this.mCallback = callback;
            this.mHandler = new MyHandler(this.mWindowManagerService.mH.getLooper());
            this.mRecurringAccessibilityEventsIntervalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
            computeChangedWindows();
        }

        public void performComputeChangedWindowsNotLocked() {
            this.mHandler.removeMessages(1);
            computeChangedWindows();
        }

        public void scheduleComputeChangedWindowsLocked() {
            if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, this.mRecurringAccessibilityEventsIntervalMillis);
            }
        }

        /* JADX WARNING: Missing block: B:65:0x0210, code:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
        /* JADX WARNING: Missing block: B:66:0x0213, code:
            if (r23 == false) goto L_0x0222;
     */
        /* JADX WARNING: Missing block: B:67:0x0215, code:
            r27.mCallback.onWindowsForAccessibilityChanged(r22);
     */
        /* JADX WARNING: Missing block: B:68:0x0222, code:
            clearAndRecycleWindows(r22);
     */
        /* JADX WARNING: Missing block: B:69:0x0225, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void computeChangedWindows() {
            boolean windowsChanged = false;
            List<WindowInfo> windows = new ArrayList();
            synchronized (this.mWindowManagerService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mWindowManagerService.mCurrentFocus == null) {
                    } else {
                        int i;
                        WindowState windowState;
                        Rect boundsInScreen;
                        WindowInfo window;
                        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealSize(this.mTempPoint);
                        int screenWidth = this.mTempPoint.x;
                        int screenHeight = this.mTempPoint.y;
                        Region unaccountedSpace = this.mTempRegion;
                        unaccountedSpace.set(0, 0, screenWidth, screenHeight);
                        SparseArray<WindowState> visibleWindows = this.mTempWindowStates;
                        populateVisibleWindowsOnScreenLocked(visibleWindows);
                        Set<IBinder> addedWindows = this.mTempBinderSet;
                        addedWindows.clear();
                        boolean focusedWindowAdded = false;
                        int visibleWindowCount = visibleWindows.size();
                        HashSet<Integer> skipRemainingWindowsForTasks = new HashSet();
                        for (i = visibleWindowCount - 1; i >= 0; i--) {
                            windowState = (WindowState) visibleWindows.valueAt(i);
                            int flags = windowState.mAttrs.flags;
                            Task task = windowState.getTask();
                            if ((task == null || !skipRemainingWindowsForTasks.contains(Integer.valueOf(task.mTaskId))) && (flags & 16) == 0) {
                                boundsInScreen = this.mTempRect;
                                computeWindowBoundsInScreen(windowState, boundsInScreen);
                                if (unaccountedSpace.quickReject(boundsInScreen)) {
                                    continue;
                                } else {
                                    if (isReportedWindowType(windowState.mAttrs.type)) {
                                        window = obtainPopulatedWindowInfo(windowState, boundsInScreen);
                                        addedWindows.add(window.token);
                                        windows.add(window);
                                        if (windowState.isFocused()) {
                                            focusedWindowAdded = true;
                                        }
                                    }
                                    if (windowState.mAttrs.type != 2032) {
                                        unaccountedSpace.op(boundsInScreen, unaccountedSpace, Op.REVERSE_DIFFERENCE);
                                    }
                                    if ((flags & 40) == 0) {
                                        unaccountedSpace.op(windowState.getDisplayFrameLw(), unaccountedSpace, Op.REVERSE_DIFFERENCE);
                                        if (task == null) {
                                            break;
                                        }
                                        skipRemainingWindowsForTasks.add(Integer.valueOf(task.mTaskId));
                                    } else if (unaccountedSpace.isEmpty()) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (!focusedWindowAdded) {
                            for (i = visibleWindowCount - 1; i >= 0; i--) {
                                windowState = (WindowState) visibleWindows.valueAt(i);
                                if (windowState.isFocused()) {
                                    boundsInScreen = this.mTempRect;
                                    computeWindowBoundsInScreen(windowState, boundsInScreen);
                                    window = obtainPopulatedWindowInfo(windowState, boundsInScreen);
                                    addedWindows.add(window.token);
                                    windows.add(window);
                                    break;
                                }
                            }
                        }
                        int windowCount = windows.size();
                        for (i = 0; i < windowCount; i++) {
                            window = (WindowInfo) windows.get(i);
                            if (!addedWindows.contains(window.parentToken)) {
                                window.parentToken = null;
                            }
                            if (window.childTokens != null) {
                                for (int j = window.childTokens.size() - 1; j >= 0; j--) {
                                    if (!addedWindows.contains(window.childTokens.get(j))) {
                                        window.childTokens.remove(j);
                                    }
                                }
                            }
                        }
                        visibleWindows.clear();
                        addedWindows.clear();
                        if (this.mOldWindows.size() != windows.size()) {
                            windowsChanged = true;
                        } else if (!this.mOldWindows.isEmpty() || (windows.isEmpty() ^ 1) != 0) {
                            for (i = 0; i < windowCount; i++) {
                                if (windowChangedNoLayer((WindowInfo) this.mOldWindows.get(i), (WindowInfo) windows.get(i))) {
                                    windowsChanged = true;
                                    break;
                                }
                            }
                        }
                        if (windowsChanged) {
                            cacheWindows(windows);
                        }
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        private void computeWindowBoundsInScreen(WindowState windowState, Rect outBounds) {
            Region touchableRegion = this.mTempRegion1;
            windowState.getTouchableRegion(touchableRegion);
            Rect touchableFrame = this.mTempRect;
            touchableRegion.getBounds(touchableFrame);
            RectF windowFrame = this.mTempRectF;
            windowFrame.set(touchableFrame);
            windowFrame.offset((float) (-windowState.mFrame.left), (float) (-windowState.mFrame.top));
            Matrix matrix = this.mTempMatrix;
            AccessibilityController.populateTransformationMatrixLocked(windowState, matrix);
            matrix.mapRect(windowFrame);
            outBounds.set((int) windowFrame.left, (int) windowFrame.top, (int) windowFrame.right, (int) windowFrame.bottom);
        }

        private static WindowInfo obtainPopulatedWindowInfo(WindowState windowState, Rect boundsInScreen) {
            WindowInfo window = windowState.getWindowInfo();
            window.boundsInScreen.set(boundsInScreen);
            return window;
        }

        private void cacheWindows(List<WindowInfo> windows) {
            int i;
            for (i = this.mOldWindows.size() - 1; i >= 0; i--) {
                ((WindowInfo) this.mOldWindows.remove(i)).recycle();
            }
            int newWindowCount = windows.size();
            for (i = 0; i < newWindowCount; i++) {
                this.mOldWindows.add(WindowInfo.obtain((WindowInfo) windows.get(i)));
            }
        }

        private boolean windowChangedNoLayer(WindowInfo oldWindow, WindowInfo newWindow) {
            if (oldWindow == newWindow) {
                return false;
            }
            if (oldWindow == null || newWindow == null || oldWindow.type != newWindow.type || oldWindow.focused != newWindow.focused) {
                return true;
            }
            if (oldWindow.token == null) {
                if (newWindow.token != null) {
                    return true;
                }
            } else if (!oldWindow.token.equals(newWindow.token)) {
                return true;
            }
            if (oldWindow.parentToken == null) {
                if (newWindow.parentToken != null) {
                    return true;
                }
            } else if (!oldWindow.parentToken.equals(newWindow.parentToken)) {
                return true;
            }
            if (oldWindow.boundsInScreen.equals(newWindow.boundsInScreen)) {
                return ((oldWindow.childTokens == null || newWindow.childTokens == null || (oldWindow.childTokens.equals(newWindow.childTokens) ^ 1) == 0) && TextUtils.equals(oldWindow.title, newWindow.title) && oldWindow.accessibilityIdOfAnchor == newWindow.accessibilityIdOfAnchor) ? false : true;
            } else {
                return true;
            }
        }

        private static void clearAndRecycleWindows(List<WindowInfo> windows) {
            for (int i = windows.size() - 1; i >= 0; i--) {
                ((WindowInfo) windows.remove(i)).recycle();
            }
        }

        private static boolean isReportedWindowType(int windowType) {
            return (windowType == 2013 || windowType == 2021 || windowType == 2026 || windowType == 2016 || windowType == 2022 || windowType == 2018 || windowType == 2027 || windowType == 1004 || windowType == 2015 || windowType == 2030) ? false : true;
        }

        private void populateVisibleWindowsOnScreenLocked(SparseArray<WindowState> outWindows) {
            DisplayContent dc = this.mWindowManagerService.getDefaultDisplayContentLocked();
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && this.mWindowManagerService.mRoot != null) {
                dc = this.mWindowManagerService.mRoot.getDisplayContent(HwPCUtils.getPCDisplayID());
            }
            dc.forAllWindows((Consumer) new AnonymousClass1(outWindows), false);
        }

        static /* synthetic */ void lambda$-com_android_server_wm_AccessibilityController$WindowsForAccessibilityObserver_62351(SparseArray outWindows, WindowState w) {
            if (w.isVisibleLw()) {
                outWindows.put(w.mLayer, w);
            }
        }
    }

    public AccessibilityController(WindowManagerService service) {
        this.mWindowManagerService = service;
    }

    public void setMagnificationCallbacksLocked(MagnificationCallbacks callbacks) {
        if (callbacks != null) {
            if (this.mDisplayMagnifier != null) {
                throw new IllegalStateException("Magnification callbacks already set!");
            }
            this.mDisplayMagnifier = new DisplayMagnifier(this.mWindowManagerService, callbacks);
        } else if (this.mDisplayMagnifier == null) {
            throw new IllegalStateException("Magnification callbacks already cleared!");
        } else {
            this.mDisplayMagnifier.destroyLocked();
            this.mDisplayMagnifier = null;
        }
    }

    public void setWindowsForAccessibilityCallback(WindowsForAccessibilityCallback callback) {
        if (callback != null) {
            if (this.mWindowsForAccessibilityObserver != null) {
                throw new IllegalStateException("Windows for accessibility callback already set!");
            }
            this.mWindowsForAccessibilityObserver = new WindowsForAccessibilityObserver(this.mWindowManagerService, callback);
        } else if (this.mWindowsForAccessibilityObserver == null) {
            throw new IllegalStateException("Windows for accessibility callback already cleared!");
        } else {
            this.mWindowsForAccessibilityObserver = null;
        }
    }

    public void performComputeChangedWindowsNotLocked() {
        WindowsForAccessibilityObserver observer;
        synchronized (this.mWindowManagerService) {
            observer = this.mWindowsForAccessibilityObserver;
        }
        if (observer != null) {
            observer.performComputeChangedWindowsNotLocked();
        }
    }

    public void setMagnificationSpecLocked(MagnificationSpec spec) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.setMagnificationSpecLocked(spec);
        }
        if (this.mWindowsForAccessibilityObserver != null) {
            this.mWindowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void getMagnificationRegionLocked(Region outMagnificationRegion) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.getMagnificationRegionLocked(outMagnificationRegion);
        }
    }

    public void onRectangleOnScreenRequestedLocked(Rect rectangle) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.onRectangleOnScreenRequestedLocked(rectangle);
        }
    }

    public void onWindowLayersChangedLocked() {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.onWindowLayersChangedLocked();
        }
        if (this.mWindowsForAccessibilityObserver != null) {
            this.mWindowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void onRotationChangedLocked(DisplayContent displayContent) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.onRotationChangedLocked(displayContent);
        }
        if (this.mWindowsForAccessibilityObserver != null) {
            this.mWindowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void onAppWindowTransitionLocked(WindowState windowState, int transition) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.onAppWindowTransitionLocked(windowState, transition);
        }
    }

    public void onWindowTransitionLocked(WindowState windowState, int transition) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.onWindowTransitionLocked(windowState, transition);
        }
        if (this.mWindowsForAccessibilityObserver != null) {
            this.mWindowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void onWindowFocusChangedNotLocked() {
        WindowsForAccessibilityObserver observer;
        synchronized (this.mWindowManagerService) {
            observer = this.mWindowsForAccessibilityObserver;
        }
        if (observer != null) {
            observer.performComputeChangedWindowsNotLocked();
        }
    }

    public void onSomeWindowResizedOrMovedLocked() {
        if (this.mWindowsForAccessibilityObserver != null) {
            this.mWindowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void drawMagnifiedRegionBorderIfNeededLocked() {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.drawMagnifiedRegionBorderIfNeededLocked();
        }
    }

    public MagnificationSpec getMagnificationSpecForWindowLocked(WindowState windowState) {
        if (this.mDisplayMagnifier != null) {
            return this.mDisplayMagnifier.getMagnificationSpecForWindowLocked(windowState);
        }
        return null;
    }

    public boolean hasCallbacksLocked() {
        if (this.mDisplayMagnifier == null && this.mWindowsForAccessibilityObserver == null) {
            return false;
        }
        return true;
    }

    public void setForceShowMagnifiableBoundsLocked(boolean show) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.setForceShowMagnifiableBoundsLocked(show);
            this.mDisplayMagnifier.showMagnificationBoundsIfNeeded();
        }
    }

    private static void populateTransformationMatrixLocked(WindowState windowState, Matrix outMatrix) {
        sTempFloats[0] = windowState.mWinAnimator.mDsDx;
        sTempFloats[3] = windowState.mWinAnimator.mDtDx;
        sTempFloats[1] = windowState.mWinAnimator.mDtDy;
        sTempFloats[4] = windowState.mWinAnimator.mDsDy;
        sTempFloats[2] = (float) windowState.mShownPosition.x;
        sTempFloats[5] = (float) windowState.mShownPosition.y;
        sTempFloats[6] = 0.0f;
        sTempFloats[7] = 0.0f;
        sTempFloats[8] = 1.0f;
        outMatrix.setValues(sTempFloats);
    }
}

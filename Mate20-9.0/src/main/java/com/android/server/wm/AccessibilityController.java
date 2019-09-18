package com.android.server.wm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
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
import android.view.SurfaceControl;
import android.view.ViewConfiguration;
import android.view.WindowInfo;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import com.android.internal.os.SomeArgs;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.pm.DumpState;
import com.android.server.wm.AccessibilityController;
import com.android.server.wm.WindowManagerInternal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

final class AccessibilityController {
    private static final float[] sTempFloats = new float[9];
    private DisplayMagnifier mDisplayMagnifier;
    private final WindowManagerService mService;
    private WindowsForAccessibilityObserver mWindowsForAccessibilityObserver;

    private static final class DisplayMagnifier {
        private static final boolean DEBUG_LAYERS = false;
        private static final boolean DEBUG_RECTANGLE_REQUESTED = false;
        private static final boolean DEBUG_ROTATION = false;
        private static final boolean DEBUG_VIEWPORT_WINDOW = false;
        private static final boolean DEBUG_WINDOW_TRANSITIONS = false;
        private static final String LOG_TAG = "WindowManager";
        /* access modifiers changed from: private */
        public final WindowManagerInternal.MagnificationCallbacks mCallbacks;
        /* access modifiers changed from: private */
        public final Context mContext;
        private boolean mForceShowMagnifiableBounds = false;
        /* access modifiers changed from: private */
        public final Handler mHandler;
        /* access modifiers changed from: private */
        public final long mLongAnimationDuration;
        /* access modifiers changed from: private */
        public final MagnifiedViewport mMagnifedViewport;
        /* access modifiers changed from: private */
        public final WindowManagerService mService;
        /* access modifiers changed from: private */
        public final Rect mTempRect1 = new Rect();
        private final Rect mTempRect2 = new Rect();
        /* access modifiers changed from: private */
        public final Region mTempRegion1 = new Region();
        /* access modifiers changed from: private */
        public final Region mTempRegion2 = new Region();
        /* access modifiers changed from: private */
        public final Region mTempRegion3 = new Region();
        /* access modifiers changed from: private */
        public final Region mTempRegion4 = new Region();

        private final class MagnifiedViewport {
            /* access modifiers changed from: private */
            public final float mBorderWidth;
            private final Path mCircularPath;
            private final int mDrawBorderInset;
            private boolean mFullRedrawNeeded;
            /* access modifiers changed from: private */
            public final int mHalfBorderWidth;
            private final Region mMagnificationRegion = new Region();
            private final MagnificationSpec mMagnificationSpec = MagnificationSpec.obtain();
            private final Region mOldMagnificationRegion = new Region();
            private int mTempLayer = 0;
            private final Matrix mTempMatrix = new Matrix();
            /* access modifiers changed from: private */
            public final Point mTempPoint = new Point();
            private final RectF mTempRectF = new RectF();
            private final SparseArray<WindowState> mTempWindowStates = new SparseArray<>();
            private final ViewportWindow mWindow;
            /* access modifiers changed from: private */
            public final WindowManager mWindowManager;

            private final class ViewportWindow {
                private static final String SURFACE_TITLE = "Magnification Overlay";
                private int mAlpha;
                private final AnimationController mAnimationController;
                private final Region mBounds = new Region();
                private Context mContext;
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
                        this.mShowHideFrameAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
                        this.mShowHideFrameAnimator.setDuration((long) context.getResources().getInteger(17694722));
                    }

                    public void onFrameShownStateChanged(boolean shown, boolean animate) {
                        obtainMessage(1, shown, animate).sendToTarget();
                    }

                    public void handleMessage(Message message) {
                        boolean animate = true;
                        if (message.what == 1) {
                            boolean shown = message.arg1 == 1;
                            if (message.arg2 != 1) {
                                animate = false;
                            }
                            if (!animate) {
                                this.mShowHideFrameAnimator.cancel();
                                if (shown) {
                                    ViewportWindow.this.setAlpha(255);
                                } else {
                                    ViewportWindow.this.setAlpha(0);
                                }
                            } else if (this.mShowHideFrameAnimator.isRunning()) {
                                this.mShowHideFrameAnimator.reverse();
                            } else if (shown) {
                                this.mShowHideFrameAnimator.start();
                            } else {
                                this.mShowHideFrameAnimator.reverse();
                            }
                        }
                    }
                }

                public ViewportWindow(Context context) {
                    SurfaceControl surfaceControl = null;
                    this.mContext = null;
                    try {
                        MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getRealSize(MagnifiedViewport.this.mTempPoint);
                        surfaceControl = DisplayMagnifier.this.mService.getDefaultDisplayContentLocked().makeOverlay().setName(SURFACE_TITLE).setSize(MagnifiedViewport.this.mTempPoint.x, MagnifiedViewport.this.mTempPoint.y).setFormat(-3).build();
                    } catch (Surface.OutOfResourcesException e) {
                    }
                    this.mSurfaceControl = surfaceControl;
                    this.mSurfaceControl.setLayer(DisplayMagnifier.this.mService.mPolicy.getWindowLayerFromTypeLw(2027) * 10000);
                    this.mSurfaceControl.setPosition(0.0f, 0.0f);
                    this.mSurface.copyFrom(this.mSurfaceControl);
                    this.mAnimationController = new AnimationController(context, DisplayMagnifier.this.mService.mH.getLooper());
                    TypedValue typedValue = new TypedValue();
                    context.getTheme().resolveAttribute(16843664, typedValue, true);
                    int borderColor = context.getColor(typedValue.resourceId);
                    this.mPaint.setStyle(Paint.Style.STROKE);
                    this.mPaint.setStrokeWidth(MagnifiedViewport.this.mBorderWidth);
                    this.mPaint.setColor(borderColor);
                    this.mInvalidated = true;
                    this.mContext = context;
                }

                public void setShown(boolean shown, boolean animate) {
                    synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mShown == shown) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            this.mShown = shown;
                            this.mAnimationController.onFrameShownStateChanged(shown, animate);
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                }

                public int getAlpha() {
                    int i;
                    synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            i = this.mAlpha;
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return i;
                }

                public void setAlpha(int alpha) {
                    synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mAlpha == alpha) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            this.mAlpha = alpha;
                            invalidate(null);
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                }

                public void setBounds(Region bounds) {
                    synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mBounds.equals(bounds)) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            this.mBounds.set(bounds);
                            invalidate(this.mDirtyRect);
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                }

                public void updateSize() {
                    synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getRealSize(MagnifiedViewport.this.mTempPoint);
                            this.mSurfaceControl.setSize(MagnifiedViewport.this.mTempPoint.x, MagnifiedViewport.this.mTempPoint.y);
                            invalidate(this.mDirtyRect);
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }

                public void invalidate(Rect dirtyRect) {
                    if (dirtyRect != null) {
                        this.mDirtyRect.set(dirtyRect);
                    } else {
                        this.mDirtyRect.setEmpty();
                    }
                    this.mInvalidated = true;
                    DisplayMagnifier.this.mService.scheduleAnimationLocked();
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
                                String name = AccessibilityController.class.getName();
                                HwPCUtils.log(name, "set pc display layerstack:" + display);
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

                /* JADX WARNING: Code restructure failed: missing block: B:28:0x0081, code lost:
                    com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:29:0x0084, code lost:
                    return;
                 */
                public void drawIfNeeded() {
                    synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (!this.mInvalidated) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                            setLayerStackForPC();
                            this.mInvalidated = false;
                            if (this.mAlpha > 0) {
                                Canvas canvas = null;
                                try {
                                    if (this.mDirtyRect.isEmpty()) {
                                        this.mBounds.getBounds(this.mDirtyRect);
                                    }
                                    this.mDirtyRect.inset(-MagnifiedViewport.this.mHalfBorderWidth, -MagnifiedViewport.this.mHalfBorderWidth);
                                    canvas = this.mSurface.lockCanvas(this.mDirtyRect);
                                } catch (Surface.OutOfResourcesException | IllegalArgumentException e) {
                                }
                                if (canvas == null) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    return;
                                }
                                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                                this.mPaint.setAlpha(this.mAlpha);
                                canvas.drawPath(this.mBounds.getBoundaryPath(), this.mPaint);
                                this.mSurface.unlockCanvasAndPost(canvas);
                                this.mSurfaceControl.show();
                            } else {
                                this.mSurfaceControl.hide();
                            }
                        } catch (Throwable th) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
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
                    this.mCircularPath.addCircle((float) centerXY, (float) centerXY, (float) centerXY, Path.Direction.CW);
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
                    setMagnifiedRegionBorderShownLocked(isMagnifyingLocked() || DisplayMagnifier.this.isForceShowingMagnifiableBoundsLocked(), true);
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
                int i = visibleWindows.size() - 1;
                while (true) {
                    int i2 = i;
                    if (i2 < 0) {
                        break;
                    }
                    WindowState windowState = visibleWindows.valueAt(i2);
                    if (windowState.mAttrs.type != 2027 && (windowState.mAttrs.privateFlags & DumpState.DUMP_DEXOPT) == 0) {
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
                        Rect rect = touchableFrame;
                        Region region = touchableRegion;
                        windowBounds.set((int) windowFrame.left, (int) windowFrame.top, (int) windowFrame.right, (int) windowFrame.bottom);
                        Region portionOfWindowAlreadyAccountedFor = DisplayMagnifier.this.mTempRegion3;
                        portionOfWindowAlreadyAccountedFor.set(this.mMagnificationRegion);
                        portionOfWindowAlreadyAccountedFor.op(nonMagnifiedBounds, Region.Op.UNION);
                        windowBounds.op(portionOfWindowAlreadyAccountedFor, Region.Op.DIFFERENCE);
                        if (windowState.shouldMagnify()) {
                            this.mMagnificationRegion.op(windowBounds, Region.Op.UNION);
                            this.mMagnificationRegion.op(availableBounds, Region.Op.INTERSECT);
                        } else {
                            nonMagnifiedBounds.op(windowBounds, Region.Op.UNION);
                            availableBounds.op(windowBounds, Region.Op.DIFFERENCE);
                        }
                        Region accountedBounds = DisplayMagnifier.this.mTempRegion2;
                        accountedBounds.set(this.mMagnificationRegion);
                        accountedBounds.op(nonMagnifiedBounds, Region.Op.UNION);
                        Region region2 = windowBounds;
                        RectF rectF = windowFrame;
                        Matrix matrix2 = matrix;
                        accountedBounds.op(0, 0, screenWidth, screenHeight, Region.Op.INTERSECT);
                        Region accountedBounds2 = accountedBounds;
                        if (accountedBounds2.isRect()) {
                            Rect accountedFrame = DisplayMagnifier.this.mTempRect1;
                            accountedBounds2.getBounds(accountedFrame);
                            if (accountedFrame.width() == screenWidth && accountedFrame.height() == screenHeight) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                    i = i2 - 1;
                }
                visibleWindows.clear();
                this.mMagnificationRegion.op(this.mDrawBorderInset, this.mDrawBorderInset, screenWidth - this.mDrawBorderInset, screenHeight - this.mDrawBorderInset, Region.Op.INTERSECT);
                if (!this.mOldMagnificationRegion.equals(this.mMagnificationRegion)) {
                    this.mWindow.setBounds(this.mMagnificationRegion);
                    Rect dirtyRect = DisplayMagnifier.this.mTempRect1;
                    if (this.mFullRedrawNeeded) {
                        this.mFullRedrawNeeded = false;
                        dirtyRect.set(this.mDrawBorderInset, this.mDrawBorderInset, screenWidth - this.mDrawBorderInset, screenHeight - this.mDrawBorderInset);
                        this.mWindow.invalidate(dirtyRect);
                    } else {
                        Region dirtyRegion = DisplayMagnifier.this.mTempRegion3;
                        dirtyRegion.set(this.mMagnificationRegion);
                        dirtyRegion.op(this.mOldMagnificationRegion, Region.Op.UNION);
                        dirtyRegion.op(nonMagnifiedBounds, Region.Op.INTERSECT);
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
                    Message message = DisplayMagnifier.this.mHandler.obtainMessage(5);
                    DisplayMagnifier.this.mHandler.sendMessageDelayed(message, (long) (((float) DisplayMagnifier.this.mLongAnimationDuration) * DisplayMagnifier.this.mService.getWindowAnimationScaleLocked()));
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
                DisplayContent dc = DisplayMagnifier.this.mService.getDefaultDisplayContentLocked();
                if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && DisplayMagnifier.this.mService.mRoot != null) {
                    dc = DisplayMagnifier.this.mService.mRoot.getDisplayContent(HwPCUtils.getPCDisplayID());
                    if (dc == null) {
                        HwPCUtils.log(DisplayMagnifier.LOG_TAG, "pc displaycontent is null");
                        return;
                    }
                }
                this.mTempLayer = 0;
                dc.forAllWindows((Consumer<WindowState>) 
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0043: INVOKE  (r0v3 'dc' com.android.server.wm.DisplayContent), (wrap: com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA
                      0x0040: CONSTRUCTOR  (r2v0 com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA) = (r3v0 'this' com.android.server.wm.AccessibilityController$DisplayMagnifier$MagnifiedViewport A[THIS]), (r4v0 'outWindows' android.util.SparseArray<com.android.server.wm.WindowState>) com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA.<init>(com.android.server.wm.AccessibilityController$DisplayMagnifier$MagnifiedViewport, android.util.SparseArray):void CONSTRUCTOR), false com.android.server.wm.DisplayContent.forAllWindows(java.util.function.Consumer, boolean):void type: VIRTUAL in method: com.android.server.wm.AccessibilityController.DisplayMagnifier.MagnifiedViewport.populateWindowsOnScreenLocked(android.util.SparseArray):void, dex: services_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0040: CONSTRUCTOR  (r2v0 com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA) = (r3v0 'this' com.android.server.wm.AccessibilityController$DisplayMagnifier$MagnifiedViewport A[THIS]), (r4v0 'outWindows' android.util.SparseArray<com.android.server.wm.WindowState>) com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA.<init>(com.android.server.wm.AccessibilityController$DisplayMagnifier$MagnifiedViewport, android.util.SparseArray):void CONSTRUCTOR in method: com.android.server.wm.AccessibilityController.DisplayMagnifier.MagnifiedViewport.populateWindowsOnScreenLocked(android.util.SparseArray):void, dex: services_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    com.android.server.wm.AccessibilityController$DisplayMagnifier r0 = com.android.server.wm.AccessibilityController.DisplayMagnifier.this
                    com.android.server.wm.WindowManagerService r0 = r0.mService
                    com.android.server.wm.DisplayContent r0 = r0.getDefaultDisplayContentLocked()
                    boolean r1 = android.util.HwPCUtils.enabledInPad()
                    if (r1 == 0) goto L_0x003b
                    boolean r1 = android.util.HwPCUtils.isPcCastModeInServer()
                    if (r1 == 0) goto L_0x003b
                    com.android.server.wm.AccessibilityController$DisplayMagnifier r1 = com.android.server.wm.AccessibilityController.DisplayMagnifier.this
                    com.android.server.wm.WindowManagerService r1 = r1.mService
                    com.android.server.wm.RootWindowContainer r1 = r1.mRoot
                    if (r1 == 0) goto L_0x003b
                    com.android.server.wm.AccessibilityController$DisplayMagnifier r1 = com.android.server.wm.AccessibilityController.DisplayMagnifier.this
                    com.android.server.wm.WindowManagerService r1 = r1.mService
                    com.android.server.wm.RootWindowContainer r1 = r1.mRoot
                    int r2 = android.util.HwPCUtils.getPCDisplayID()
                    com.android.server.wm.DisplayContent r0 = r1.getDisplayContent(r2)
                    if (r0 != 0) goto L_0x003b
                    java.lang.String r1 = "WindowManager"
                    java.lang.String r2 = "pc displaycontent is null"
                    android.util.HwPCUtils.log(r1, r2)
                    return
                L_0x003b:
                    r1 = 0
                    r3.mTempLayer = r1
                    com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA r2 = new com.android.server.wm.-$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGy-UXiWV1D2yZGvH-9qN0AA
                    r2.<init>(r3, r4)
                    r0.forAllWindows((java.util.function.Consumer<com.android.server.wm.WindowState>) r2, (boolean) r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.AccessibilityController.DisplayMagnifier.MagnifiedViewport.populateWindowsOnScreenLocked(android.util.SparseArray):void");
            }

            public static /* synthetic */ void lambda$populateWindowsOnScreenLocked$0(MagnifiedViewport magnifiedViewport, SparseArray outWindows, WindowState w) {
                if (w.isOnScreen() && w.isVisibleLw() && w.mAttrs.alpha != 0.0f && !w.mWinAnimator.mEnterAnimationPending) {
                    magnifiedViewport.mTempLayer++;
                    outWindows.put(magnifiedViewport.mTempLayer, w);
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
                        Region magnifiedBounds = (Region) ((SomeArgs) message.obj).arg1;
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
                        synchronized (DisplayMagnifier.this.mService.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                if (DisplayMagnifier.this.mMagnifedViewport.isMagnifyingLocked() || DisplayMagnifier.this.isForceShowingMagnifiableBoundsLocked()) {
                                    DisplayMagnifier.this.mMagnifedViewport.setMagnifiedRegionBorderShownLocked(true, true);
                                    DisplayMagnifier.this.mService.scheduleAnimationLocked();
                                }
                            } catch (Throwable th) {
                                while (true) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                    break;
                                }
                            }
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    default:
                        return;
                }
            }
        }

        public DisplayMagnifier(WindowManagerService windowManagerService, WindowManagerInternal.MagnificationCallbacks callbacks) {
            this.mContext = windowManagerService.mContext;
            this.mService = windowManagerService;
            this.mCallbacks = callbacks;
            this.mHandler = new MyHandler(this.mService.mH.getLooper());
            this.mMagnifedViewport = new MagnifiedViewport();
            this.mLongAnimationDuration = (long) this.mContext.getResources().getInteger(17694722);
        }

        public void setMagnificationSpecLocked(MagnificationSpec spec) {
            this.mMagnifedViewport.updateMagnificationSpecLocked(spec);
            this.mMagnifedViewport.recomputeBoundsLocked();
            this.mService.applyMagnificationSpec(spec);
            this.mService.scheduleAnimationLocked();
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
            this.mService.scheduleAnimationLocked();
        }

        public void onRotationChangedLocked(DisplayContent displayContent) {
            this.mMagnifedViewport.onRotationChangedLocked();
            this.mHandler.sendEmptyMessage(4);
        }

        public void onAppWindowTransitionLocked(WindowState windowState, int transition) {
            if (this.mMagnifedViewport.isMagnifyingLocked()) {
                if (!(transition == 6 || transition == 8 || transition == 10)) {
                    switch (transition) {
                        case 12:
                        case 13:
                        case 14:
                            break;
                        default:
                            return;
                    }
                }
                this.mHandler.sendEmptyMessage(3);
            }
        }

        public void onWindowTransitionLocked(WindowState windowState, int transition) {
            boolean magnifying = this.mMagnifedViewport.isMagnifyingLocked();
            int type = windowState.mAttrs.type;
            if ((transition == 1 || transition == 3) && magnifying) {
                if (!(type == 2 || type == 4 || type == 1005 || type == 2020 || type == 2024 || type == 2035 || type == 2038)) {
                    switch (type) {
                        case 1000:
                        case NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE:
                        case 1002:
                        case 1003:
                            break;
                        default:
                            switch (type) {
                                case 2001:
                                case 2002:
                                case 2003:
                                    break;
                                default:
                                    switch (type) {
                                        case 2005:
                                        case 2006:
                                        case 2007:
                                        case 2008:
                                        case 2009:
                                        case 2010:
                                            break;
                                        default:
                                            return;
                                    }
                            }
                    }
                }
                Rect magnifiedRegionBounds = this.mTempRect2;
                this.mMagnifedViewport.getMagnifiedFrameInContentCoordsLocked(magnifiedRegionBounds);
                Rect touchableRegionBounds = this.mTempRect1;
                windowState.getTouchableRegion(this.mTempRegion1);
                this.mTempRegion1.getBounds(touchableRegionBounds);
                if (!magnifiedRegionBounds.intersect(touchableRegionBounds)) {
                    this.mCallbacks.onRectangleOnScreenRequested(touchableRegionBounds.left, touchableRegionBounds.top, touchableRegionBounds.right, touchableRegionBounds.bottom);
                }
            }
        }

        public MagnificationSpec getMagnificationSpecForWindowLocked(WindowState windowState) {
            MagnificationSpec spec = this.mMagnifedViewport.getMagnificationSpecLocked();
            if (spec == null || spec.isNop() || windowState.shouldMagnify()) {
                return spec;
            }
            return null;
        }

        public void getMagnificationRegionLocked(Region outMagnificationRegion) {
            this.mMagnifedViewport.recomputeBoundsLocked();
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
        private final WindowManagerInternal.WindowsForAccessibilityCallback mCallback;
        private final Context mContext;
        private final Handler mHandler;
        private final List<WindowInfo> mOldWindows = new ArrayList();
        private final long mRecurringAccessibilityEventsIntervalMillis;
        private final WindowManagerService mService;
        private final Set<IBinder> mTempBinderSet = new ArraySet();
        private int mTempLayer = 0;
        private final Matrix mTempMatrix = new Matrix();
        private final Point mTempPoint = new Point();
        private final Rect mTempRect = new Rect();
        private final RectF mTempRectF = new RectF();
        private final Region mTempRegion = new Region();
        private final Region mTempRegion1 = new Region();
        private final SparseArray<WindowState> mTempWindowStates = new SparseArray<>();

        private class MyHandler extends Handler {
            public static final int MESSAGE_COMPUTE_CHANGED_WINDOWS = 1;

            public MyHandler(Looper looper) {
                super(looper, null, false);
            }

            public void handleMessage(Message message) {
                if (message.what == 1) {
                    WindowsForAccessibilityObserver.this.computeChangedWindows();
                }
            }
        }

        public WindowsForAccessibilityObserver(WindowManagerService windowManagerService, WindowManagerInternal.WindowsForAccessibilityCallback callback) {
            this.mContext = windowManagerService.mContext;
            this.mService = windowManagerService;
            this.mCallback = callback;
            this.mHandler = new MyHandler(this.mService.mH.getLooper());
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

        /* JADX WARNING: Code restructure failed: missing block: B:87:0x019e, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:88:0x01a1, code lost:
            if (r2 == false) goto L_0x01a8;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:89:0x01a3, code lost:
            r1.mCallback.onWindowsForAccessibilityChanged(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:90:0x01a8, code lost:
            clearAndRecycleWindows(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:91:0x01ab, code lost:
            return;
         */
        public void computeChangedWindows() {
            boolean windowsChanged;
            boolean windowsChanged2;
            int screenHeight;
            int screenWidth;
            boolean windowsChanged3 = false;
            List<WindowInfo> windows = new ArrayList<>();
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mService.mCurrentFocus == null) {
                        try {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        } catch (Throwable th) {
                            th = th;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
                        windowManager.getDefaultDisplay().getRealSize(this.mTempPoint);
                        int screenWidth2 = this.mTempPoint.x;
                        int screenHeight2 = this.mTempPoint.y;
                        Region unaccountedSpace = this.mTempRegion;
                        unaccountedSpace.set(0, 0, screenWidth2, screenHeight2);
                        SparseArray<WindowState> visibleWindows = this.mTempWindowStates;
                        populateVisibleWindowsOnScreenLocked(visibleWindows);
                        Set<IBinder> addedWindows = this.mTempBinderSet;
                        addedWindows.clear();
                        boolean focusedWindowAdded = false;
                        int visibleWindowCount = visibleWindows.size();
                        HashSet<Integer> skipRemainingWindowsForTasks = new HashSet<>();
                        int i = visibleWindowCount - 1;
                        while (true) {
                            if (i < 0) {
                                windowsChanged = windowsChanged3;
                                int i2 = screenWidth2;
                                int i3 = screenHeight2;
                                break;
                            }
                            WindowState windowState = visibleWindows.valueAt(i);
                            int flags = windowState.mAttrs.flags;
                            WindowManager windowManager2 = windowManager;
                            Task task = windowState.getTask();
                            if (task != null) {
                                windowsChanged = windowsChanged3;
                                try {
                                    if (skipRemainingWindowsForTasks.contains(Integer.valueOf(task.mTaskId))) {
                                        screenWidth = screenWidth2;
                                        screenHeight = screenHeight2;
                                        i--;
                                        windowManager = windowManager2;
                                        windowsChanged3 = windowsChanged;
                                        screenWidth2 = screenWidth;
                                        screenHeight2 = screenHeight;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    boolean z = windowsChanged;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            } else {
                                windowsChanged = windowsChanged3;
                            }
                            if (flags != false && true) {
                                screenWidth = screenWidth2;
                                if (windowState.mAttrs.type != 2034) {
                                    screenHeight = screenHeight2;
                                    i--;
                                    windowManager = windowManager2;
                                    windowsChanged3 = windowsChanged;
                                    screenWidth2 = screenWidth;
                                    screenHeight2 = screenHeight;
                                }
                            } else {
                                screenWidth = screenWidth2;
                            }
                            Rect boundsInScreen = this.mTempRect;
                            computeWindowBoundsInScreen(windowState, boundsInScreen);
                            if (!unaccountedSpace.quickReject(boundsInScreen)) {
                                if (isReportedWindowType(windowState.mAttrs.type)) {
                                    addPopulatedWindowInfo(windowState, boundsInScreen, windows, addedWindows);
                                    if (windowState.isFocused()) {
                                        focusedWindowAdded = true;
                                    }
                                }
                                screenHeight = screenHeight2;
                                if (windowState.mAttrs.type != 2032) {
                                    unaccountedSpace.op(boundsInScreen, unaccountedSpace, Region.Op.REVERSE_DIFFERENCE);
                                    if ((flags & 40) == 0) {
                                        unaccountedSpace.op(windowState.getDisplayFrameLw(), unaccountedSpace, Region.Op.REVERSE_DIFFERENCE);
                                        if (task == null) {
                                            break;
                                        }
                                        skipRemainingWindowsForTasks.add(Integer.valueOf(task.mTaskId));
                                        i--;
                                        windowManager = windowManager2;
                                        windowsChanged3 = windowsChanged;
                                        screenWidth2 = screenWidth;
                                        screenHeight2 = screenHeight;
                                    }
                                }
                                if (unaccountedSpace.isEmpty()) {
                                    break;
                                }
                                i--;
                                windowManager = windowManager2;
                                windowsChanged3 = windowsChanged;
                                screenWidth2 = screenWidth;
                                screenHeight2 = screenHeight;
                            }
                            screenHeight = screenHeight2;
                            i--;
                            windowManager = windowManager2;
                            windowsChanged3 = windowsChanged;
                            screenWidth2 = screenWidth;
                            screenHeight2 = screenHeight;
                        }
                        if (!focusedWindowAdded) {
                            int i4 = visibleWindowCount - 1;
                            while (true) {
                                if (i4 < 0) {
                                    break;
                                }
                                WindowState windowState2 = visibleWindows.valueAt(i4);
                                if (windowState2.isFocused()) {
                                    Rect boundsInScreen2 = this.mTempRect;
                                    computeWindowBoundsInScreen(windowState2, boundsInScreen2);
                                    addPopulatedWindowInfo(windowState2, boundsInScreen2, windows, addedWindows);
                                    break;
                                }
                                i4--;
                            }
                        }
                        int i5 = windows.size();
                        for (int i6 = 0; i6 < i5; i6++) {
                            WindowInfo window = windows.get(i6);
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
                            windowsChanged2 = true;
                        } else {
                            if (!this.mOldWindows.isEmpty() || !windows.isEmpty()) {
                                int i7 = 0;
                                while (true) {
                                    int i8 = i7;
                                    if (i8 >= i5) {
                                        break;
                                    } else if (windowChangedNoLayer(this.mOldWindows.get(i8), windows.get(i8))) {
                                        windowsChanged2 = true;
                                        break;
                                    } else {
                                        i7 = i8 + 1;
                                    }
                                }
                            }
                            windowsChanged2 = windowsChanged;
                        }
                        if (windowsChanged2) {
                            cacheWindows(windows);
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
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

        private static void addPopulatedWindowInfo(WindowState windowState, Rect boundsInScreen, List<WindowInfo> out, Set<IBinder> tokenOut) {
            WindowInfo window = windowState.getWindowInfo();
            window.boundsInScreen.set(boundsInScreen);
            window.layer = tokenOut.size();
            out.add(window);
            tokenOut.add(window.token);
        }

        private void cacheWindows(List<WindowInfo> windows) {
            for (int i = this.mOldWindows.size() - 1; i >= 0; i--) {
                this.mOldWindows.remove(i).recycle();
            }
            int i2 = windows.size();
            for (int i3 = 0; i3 < i2; i3++) {
                this.mOldWindows.add(WindowInfo.obtain(windows.get(i3)));
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
            if (!oldWindow.boundsInScreen.equals(newWindow.boundsInScreen)) {
                return true;
            }
            if ((oldWindow.childTokens == null || newWindow.childTokens == null || oldWindow.childTokens.equals(newWindow.childTokens)) && TextUtils.equals(oldWindow.title, newWindow.title) && oldWindow.accessibilityIdOfAnchor == newWindow.accessibilityIdOfAnchor) {
                return false;
            }
            return true;
        }

        private static void clearAndRecycleWindows(List<WindowInfo> windows) {
            for (int i = windows.size() - 1; i >= 0; i--) {
                windows.remove(i).recycle();
            }
        }

        private static boolean isReportedWindowType(int windowType) {
            return (windowType == 2013 || windowType == 2021 || windowType == 2026 || windowType == 2016 || windowType == 2022 || windowType == 2018 || windowType == 2027 || windowType == 1004 || windowType == 2015 || windowType == 2030) ? false : true;
        }

        private void populateVisibleWindowsOnScreenLocked(SparseArray<WindowState> outWindows) {
            DisplayContent dc = this.mService.getDefaultDisplayContentLocked();
            if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && this.mService.mRoot != null) {
                dc = this.mService.mRoot.getDisplayContent(HwPCUtils.getPCDisplayID());
                if (dc == null) {
                    HwPCUtils.log(LOG_TAG, "pc displaycontent is null");
                    return;
                }
            }
            this.mTempLayer = 0;
            dc.forAllWindows((Consumer<WindowState>) new Consumer(outWindows) {
                private final /* synthetic */ SparseArray f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    AccessibilityController.WindowsForAccessibilityObserver.lambda$populateVisibleWindowsOnScreenLocked$0(AccessibilityController.WindowsForAccessibilityObserver.this, this.f$1, (WindowState) obj);
                }
            }, false);
        }

        public static /* synthetic */ void lambda$populateVisibleWindowsOnScreenLocked$0(WindowsForAccessibilityObserver windowsForAccessibilityObserver, SparseArray outWindows, WindowState w) {
            if (w.isVisibleLw()) {
                int i = windowsForAccessibilityObserver.mTempLayer;
                windowsForAccessibilityObserver.mTempLayer = i + 1;
                outWindows.put(i, w);
            }
        }
    }

    public AccessibilityController(WindowManagerService service) {
        this.mService = service;
    }

    public void setMagnificationCallbacksLocked(WindowManagerInternal.MagnificationCallbacks callbacks) {
        if (callbacks != null) {
            if (this.mDisplayMagnifier == null) {
                this.mDisplayMagnifier = new DisplayMagnifier(this.mService, callbacks);
                return;
            }
            throw new IllegalStateException("Magnification callbacks already set!");
        } else if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.destroyLocked();
            this.mDisplayMagnifier = null;
        } else {
            throw new IllegalStateException("Magnification callbacks already cleared!");
        }
    }

    public void setWindowsForAccessibilityCallback(WindowManagerInternal.WindowsForAccessibilityCallback callback) {
        if (callback != null) {
            if (this.mWindowsForAccessibilityObserver == null) {
                this.mWindowsForAccessibilityObserver = new WindowsForAccessibilityObserver(this.mService, callback);
                return;
            }
            throw new IllegalStateException("Windows for accessibility callback already set!");
        } else if (this.mWindowsForAccessibilityObserver != null) {
            this.mWindowsForAccessibilityObserver = null;
        } else {
            throw new IllegalStateException("Windows for accessibility callback already cleared!");
        }
    }

    public void performComputeChangedWindowsNotLocked() {
        WindowsForAccessibilityObserver observer;
        synchronized (this.mService) {
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
        synchronized (this.mService) {
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
        return (this.mDisplayMagnifier == null && this.mWindowsForAccessibilityObserver == null) ? false : true;
    }

    public void setForceShowMagnifiableBoundsLocked(boolean show) {
        if (this.mDisplayMagnifier != null) {
            this.mDisplayMagnifier.setForceShowMagnifiableBoundsLocked(show);
            this.mDisplayMagnifier.showMagnificationBoundsIfNeeded();
        }
    }

    /* access modifiers changed from: private */
    public static void populateTransformationMatrixLocked(WindowState windowState, Matrix outMatrix) {
        windowState.getTransformationMatrix(sTempFloats, outMatrix);
    }
}

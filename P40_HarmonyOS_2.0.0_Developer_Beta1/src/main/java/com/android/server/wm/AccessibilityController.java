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
import com.android.server.wm.AccessibilityController;
import com.android.server.wm.WindowManagerInternal;
import com.android.server.wm.WindowManagerService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public final class AccessibilityController {
    private static final float[] sTempFloats = new float[9];
    private SparseArray<DisplayMagnifier> mDisplayMagnifiers = new SparseArray<>();
    private final WindowManagerService mService;
    private WindowsForAccessibilityObserver mWindowsForAccessibilityObserver;

    public AccessibilityController(WindowManagerService service) {
        this.mService = service;
    }

    public boolean setMagnificationCallbacksLocked(int displayId, WindowManagerInternal.MagnificationCallbacks callbacks) {
        Display display;
        if (callbacks == null) {
            DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
            if (displayMagnifier != null) {
                displayMagnifier.destroyLocked();
                this.mDisplayMagnifiers.remove(displayId);
                return true;
            }
            throw new IllegalStateException("Magnification callbacks already cleared!");
        } else if (this.mDisplayMagnifiers.get(displayId) == null) {
            DisplayContent dc = this.mService.mRoot.getDisplayContent(displayId);
            if (dc == null || (display = dc.getDisplay()) == null || display.getType() == 4) {
                return false;
            }
            this.mDisplayMagnifiers.put(displayId, new DisplayMagnifier(this.mService, dc, display, callbacks));
            return true;
        } else {
            throw new IllegalStateException("Magnification callbacks already set!");
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

    public void performComputeChangedWindowsNotLocked(boolean forceSend) {
        WindowsForAccessibilityObserver observer;
        synchronized (this.mService) {
            observer = this.mWindowsForAccessibilityObserver;
        }
        if (observer != null) {
            observer.performComputeChangedWindowsNotLocked(forceSend);
        }
    }

    public void setMagnificationSpecLocked(int displayId, MagnificationSpec spec) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.setMagnificationSpecLocked(spec);
        }
        WindowsForAccessibilityObserver windowsForAccessibilityObserver = this.mWindowsForAccessibilityObserver;
        if (windowsForAccessibilityObserver != null && displayId == 0) {
            windowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void getMagnificationRegionLocked(int displayId, Region outMagnificationRegion) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.getMagnificationRegionLocked(outMagnificationRegion);
        }
    }

    public void onRectangleOnScreenRequestedLocked(int displayId, Rect rectangle) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.onRectangleOnScreenRequestedLocked(rectangle);
        }
    }

    public void onWindowLayersChangedLocked(int displayId) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.onWindowLayersChangedLocked();
        }
        WindowsForAccessibilityObserver windowsForAccessibilityObserver = this.mWindowsForAccessibilityObserver;
        if (windowsForAccessibilityObserver != null && displayId == 0) {
            windowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void onRotationChangedLocked(DisplayContent displayContent) {
        int displayId = displayContent.getDisplayId();
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.onRotationChangedLocked(displayContent);
        }
        WindowsForAccessibilityObserver windowsForAccessibilityObserver = this.mWindowsForAccessibilityObserver;
        if (windowsForAccessibilityObserver != null && displayId == 0) {
            windowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void onAppWindowTransitionLocked(WindowState windowState, int transition) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(windowState.getDisplayId());
        if (displayMagnifier != null) {
            displayMagnifier.onAppWindowTransitionLocked(windowState, transition);
        }
    }

    public void onWindowTransitionLocked(WindowState windowState, int transition) {
        int displayId = windowState.getDisplayId();
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.onWindowTransitionLocked(windowState, transition);
        }
        WindowsForAccessibilityObserver windowsForAccessibilityObserver = this.mWindowsForAccessibilityObserver;
        if (windowsForAccessibilityObserver != null && displayId == 0) {
            windowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void onWindowFocusChangedNotLocked() {
        WindowsForAccessibilityObserver observer;
        synchronized (this.mService) {
            observer = this.mWindowsForAccessibilityObserver;
        }
        if (observer != null) {
            observer.performComputeChangedWindowsNotLocked(false);
        }
    }

    public void onSomeWindowResizedOrMovedLocked() {
        WindowsForAccessibilityObserver windowsForAccessibilityObserver = this.mWindowsForAccessibilityObserver;
        if (windowsForAccessibilityObserver != null) {
            windowsForAccessibilityObserver.scheduleComputeChangedWindowsLocked();
        }
    }

    public void drawMagnifiedRegionBorderIfNeededLocked(int displayId) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.drawMagnifiedRegionBorderIfNeededLocked();
        }
    }

    public MagnificationSpec getMagnificationSpecForWindowLocked(WindowState windowState) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(windowState.getDisplayId());
        if (displayMagnifier != null) {
            return displayMagnifier.getMagnificationSpecForWindowLocked(windowState);
        }
        return null;
    }

    public boolean hasCallbacksLocked() {
        return this.mDisplayMagnifiers.size() > 0 || this.mWindowsForAccessibilityObserver != null;
    }

    public void setForceShowMagnifiableBoundsLocked(int displayId, boolean show) {
        DisplayMagnifier displayMagnifier = this.mDisplayMagnifiers.get(displayId);
        if (displayMagnifier != null) {
            displayMagnifier.setForceShowMagnifiableBoundsLocked(show);
            displayMagnifier.showMagnificationBoundsIfNeeded();
        }
    }

    /* access modifiers changed from: private */
    public static void populateTransformationMatrixLocked(WindowState windowState, Matrix outMatrix) {
        windowState.getTransformationMatrix(sTempFloats, outMatrix);
    }

    /* access modifiers changed from: private */
    public static final class DisplayMagnifier {
        private static final boolean DEBUG_LAYERS = false;
        private static final boolean DEBUG_RECTANGLE_REQUESTED = false;
        private static final boolean DEBUG_ROTATION = false;
        private static final boolean DEBUG_VIEWPORT_WINDOW = false;
        private static final boolean DEBUG_WINDOW_TRANSITIONS = false;
        private static final String LOG_TAG = "WindowManager";
        private final WindowManagerInternal.MagnificationCallbacks mCallbacks;
        private final Context mContext;
        private final Display mDisplay;
        private final DisplayContent mDisplayContent;
        private boolean mForceShowMagnifiableBounds = false;
        private final Handler mHandler;
        private final long mLongAnimationDuration;
        private final MagnifiedViewport mMagnifedViewport;
        private final WindowManagerService mService;
        private final Rect mTempRect1 = new Rect();
        private final Rect mTempRect2 = new Rect();
        private final Region mTempRegion1 = new Region();
        private final Region mTempRegion2 = new Region();
        private final Region mTempRegion3 = new Region();
        private final Region mTempRegion4 = new Region();

        public DisplayMagnifier(WindowManagerService windowManagerService, DisplayContent displayContent, Display display, WindowManagerInternal.MagnificationCallbacks callbacks) {
            this.mContext = windowManagerService.mContext;
            this.mService = windowManagerService;
            this.mCallbacks = callbacks;
            this.mDisplayContent = displayContent;
            this.mDisplay = display;
            this.mHandler = new MyHandler(this.mService.mH.getLooper());
            this.mMagnifedViewport = new MagnifiedViewport();
            this.mLongAnimationDuration = (long) this.mContext.getResources().getInteger(17694722);
        }

        public void setMagnificationSpecLocked(MagnificationSpec spec) {
            this.mMagnifedViewport.updateMagnificationSpecLocked(spec);
            this.mMagnifedViewport.recomputeBoundsLocked();
            this.mService.applyMagnificationSpecLocked(this.mDisplay.getDisplayId(), spec);
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
                switch (transition) {
                    case 6:
                    case 8:
                    case 10:
                    case 12:
                    case 13:
                    case WindowManagerService.H.PERSIST_ANIMATION_SCALE /* 14 */:
                        this.mHandler.sendEmptyMessage(3);
                        return;
                    case 7:
                    case 9:
                    case WindowManagerService.H.WINDOW_FREEZE_TIMEOUT /* 11 */:
                    default:
                        return;
                }
            }
        }

        public void onWindowTransitionLocked(WindowState windowState, int transition) {
            boolean magnifying = this.mMagnifedViewport.isMagnifyingLocked();
            int type = windowState.mAttrs.type;
            if ((transition == 1 || transition == 3) && magnifying) {
                if (!(type == 2 || type == 4 || type == 1005 || type == 2020 || type == 2024 || type == 2035 || type == 2038)) {
                    switch (type) {
                        case 1000:
                        case 1001:
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

        /* access modifiers changed from: private */
        public final class MagnifiedViewport {
            private final float mBorderWidth;
            private final Path mCircularPath;
            private final int mDrawBorderInset;
            private boolean mFullRedrawNeeded;
            private final int mHalfBorderWidth;
            private final Region mMagnificationRegion = new Region();
            private final MagnificationSpec mMagnificationSpec = MagnificationSpec.obtain();
            private final Region mOldMagnificationRegion = new Region();
            private int mTempLayer = 0;
            private final Matrix mTempMatrix = new Matrix();
            private final Point mTempPoint = new Point();
            private final RectF mTempRectF = new RectF();
            private final SparseArray<WindowState> mTempWindowStates = new SparseArray<>();
            private final ViewportWindow mWindow;
            private final WindowManager mWindowManager;

            public MagnifiedViewport() {
                this.mWindowManager = (WindowManager) DisplayMagnifier.this.mContext.getSystemService("window");
                this.mBorderWidth = DisplayMagnifier.this.mContext.getResources().getDimension(17104906);
                this.mHalfBorderWidth = (int) Math.ceil((double) (this.mBorderWidth / 2.0f));
                this.mDrawBorderInset = ((int) this.mBorderWidth) / 2;
                this.mWindow = new ViewportWindow(DisplayMagnifier.this.mContext);
                if (DisplayMagnifier.this.mContext.getResources().getConfiguration().isScreenRound()) {
                    this.mCircularPath = new Path();
                    DisplayMagnifier.this.mDisplay.getRealSize(this.mTempPoint);
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
                DisplayMagnifier.this.mDisplay.getRealSize(this.mTempPoint);
                int screenWidth = this.mTempPoint.x;
                int screenHeight = this.mTempPoint.y;
                this.mMagnificationRegion.set(0, 0, 0, 0);
                Region availableBounds = DisplayMagnifier.this.mTempRegion1;
                availableBounds.set(0, 0, screenWidth, screenHeight);
                Path path = this.mCircularPath;
                if (path != null) {
                    availableBounds.setPath(path, availableBounds);
                }
                Region nonMagnifiedBounds = DisplayMagnifier.this.mTempRegion4;
                nonMagnifiedBounds.set(0, 0, 0, 0);
                SparseArray<WindowState> visibleWindows = this.mTempWindowStates;
                visibleWindows.clear();
                populateWindowsOnScreenLocked(visibleWindows);
                for (int i = visibleWindows.size() - 1; i >= 0; i--) {
                    WindowState windowState = visibleWindows.valueAt(i);
                    if (windowState.mAttrs.type != 2027 && (windowState.mAttrs.privateFlags & 1048576) == 0) {
                        Matrix matrix = this.mTempMatrix;
                        AccessibilityController.populateTransformationMatrixLocked(windowState, matrix);
                        Region touchableRegion = DisplayMagnifier.this.mTempRegion3;
                        windowState.getTouchableRegion(touchableRegion);
                        Rect touchableFrame = DisplayMagnifier.this.mTempRect1;
                        touchableRegion.getBounds(touchableFrame);
                        RectF windowFrame = this.mTempRectF;
                        windowFrame.set(touchableFrame);
                        windowFrame.offset((float) (-windowState.getFrameLw().left), (float) (-windowState.getFrameLw().top));
                        matrix.mapRect(windowFrame);
                        Region windowBounds = DisplayMagnifier.this.mTempRegion2;
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
                        if (windowState.isLetterboxedForDisplayCutoutLw()) {
                            Region letterboxBounds = getLetterboxBounds(windowState);
                            nonMagnifiedBounds.op(letterboxBounds, Region.Op.UNION);
                            availableBounds.op(letterboxBounds, Region.Op.DIFFERENCE);
                        }
                        Region accountedBounds = DisplayMagnifier.this.mTempRegion2;
                        accountedBounds.set(this.mMagnificationRegion);
                        accountedBounds.op(nonMagnifiedBounds, Region.Op.UNION);
                        accountedBounds.op(0, 0, screenWidth, screenHeight, Region.Op.INTERSECT);
                        if (accountedBounds.isRect()) {
                            Rect accountedFrame = DisplayMagnifier.this.mTempRect1;
                            accountedBounds.getBounds(accountedFrame);
                            if (accountedFrame.width() == screenWidth && accountedFrame.height() == screenHeight) {
                                break;
                            }
                        }
                    }
                }
                visibleWindows.clear();
                Region region = this.mMagnificationRegion;
                int i2 = this.mDrawBorderInset;
                region.op(i2, i2, screenWidth - i2, screenHeight - i2, Region.Op.INTERSECT);
                if (!this.mOldMagnificationRegion.equals(this.mMagnificationRegion)) {
                    this.mWindow.setBounds(this.mMagnificationRegion);
                    Rect dirtyRect = DisplayMagnifier.this.mTempRect1;
                    if (this.mFullRedrawNeeded) {
                        this.mFullRedrawNeeded = false;
                        int i3 = this.mDrawBorderInset;
                        dirtyRect.set(i3, i3, screenWidth - i3, screenHeight - i3);
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

            private Region getLetterboxBounds(WindowState windowState) {
                AppWindowToken appToken = windowState.mAppToken;
                if (appToken == null) {
                    return new Region();
                }
                DisplayMagnifier.this.mDisplay.getRealSize(this.mTempPoint);
                Rect letterboxInsets = appToken.getLetterboxInsets();
                int screenWidth = this.mTempPoint.x;
                int screenHeight = this.mTempPoint.y;
                Rect nonLetterboxRect = DisplayMagnifier.this.mTempRect1;
                Region letterboxBounds = DisplayMagnifier.this.mTempRegion3;
                nonLetterboxRect.set(0, 0, screenWidth, screenHeight);
                nonLetterboxRect.inset(letterboxInsets);
                letterboxBounds.set(0, 0, screenWidth, screenHeight);
                letterboxBounds.op(nonLetterboxRect, Region.Op.DIFFERENCE);
                return letterboxBounds;
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
                if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || DisplayMagnifier.this.mService.mRoot == null || DisplayMagnifier.this.mService.mRoot.getDisplayContent(HwPCUtils.getPCDisplayID()) != null) {
                    this.mTempLayer = 0;
                    DisplayMagnifier.this.mDisplayContent.forAllWindows((Consumer<WindowState>) new Consumer(outWindows) {
                        /* class com.android.server.wm.$$Lambda$AccessibilityController$DisplayMagnifier$MagnifiedViewport$ZNyFGyUXiWV1D2yZGvH9qN0AA */
                        private final /* synthetic */ SparseArray f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            AccessibilityController.DisplayMagnifier.MagnifiedViewport.this.lambda$populateWindowsOnScreenLocked$0$AccessibilityController$DisplayMagnifier$MagnifiedViewport(this.f$1, (WindowState) obj);
                        }
                    }, false);
                    return;
                }
                HwPCUtils.log(DisplayMagnifier.LOG_TAG, "pc displaycontent is null");
            }

            public /* synthetic */ void lambda$populateWindowsOnScreenLocked$0$AccessibilityController$DisplayMagnifier$MagnifiedViewport(SparseArray outWindows, WindowState w) {
                if (w.isOnScreen() && w.isVisibleLw() && w.mAttrs.alpha != 0.0f && !w.mWinAnimator.mEnterAnimationPending) {
                    this.mTempLayer++;
                    outWindows.put(this.mTempLayer, w);
                }
            }

            /* access modifiers changed from: private */
            public final class ViewportWindow {
                private static final String SURFACE_TITLE = "Magnification Overlay";
                private int mAlpha;
                private final AnimationController mAnimationController;
                private final Region mBounds = new Region();
                private Context mContext = null;
                private final Rect mDirtyRect = new Rect();
                private boolean mInvalidated;
                private boolean mIsLastInPCMode = false;
                private final Paint mPaint = new Paint();
                private boolean mShown;
                private final Surface mSurface = new Surface();
                private final SurfaceControl mSurfaceControl;

                public ViewportWindow(Context context) {
                    SurfaceControl surfaceControl = null;
                    try {
                        DisplayMagnifier.this.mDisplay.getRealSize(MagnifiedViewport.this.mTempPoint);
                        surfaceControl = DisplayMagnifier.this.mDisplayContent.makeOverlay().setName(SURFACE_TITLE).setBufferSize(MagnifiedViewport.this.mTempPoint.x, MagnifiedViewport.this.mTempPoint.y).setFormat(-3).build();
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
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mShown != shown) {
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
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
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
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mAlpha != alpha) {
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
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (!this.mBounds.equals(bounds)) {
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
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            MagnifiedViewport.this.mWindowManager.getDefaultDisplay().getRealSize(MagnifiedViewport.this.mTempPoint);
                            this.mSurfaceControl.setBufferSize(MagnifiedViewport.this.mTempPoint.x, MagnifiedViewport.this.mTempPoint.y);
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
                    DisplayMagnifier.this.mService.scheduleAnimationLocked();
                }

                private void setLayerStackForPC() {
                    if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() != this.mIsLastInPCMode) {
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
                        this.mIsLastInPCMode = HwPCUtils.isPcCastModeInServer();
                    }
                }

                public void drawIfNeeded() {
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (this.mInvalidated) {
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
                                WindowManagerService.resetPriorityAfterLockedSection();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }

                public void releaseSurface() {
                    this.mSurfaceControl.remove();
                    this.mSurface.release();
                }

                /* access modifiers changed from: private */
                public final class AnimationController extends Handler {
                    private static final int MAX_ALPHA = 255;
                    private static final int MIN_ALPHA = 0;
                    private static final int MSG_FRAME_SHOWN_STATE_CHANGED = 1;
                    private static final String PROPERTY_NAME_ALPHA = "alpha";
                    private final ValueAnimator mShowHideFrameAnimator;

                    public AnimationController(Context context, Looper looper) {
                        super(looper);
                        this.mShowHideFrameAnimator = ObjectAnimator.ofInt(ViewportWindow.this, PROPERTY_NAME_ALPHA, 0, MAX_ALPHA);
                        this.mShowHideFrameAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
                        this.mShowHideFrameAnimator.setDuration((long) context.getResources().getInteger(17694722));
                    }

                    public void onFrameShownStateChanged(boolean shown, boolean animate) {
                        obtainMessage(1, shown ? 1 : 0, animate ? 1 : 0).sendToTarget();
                    }

                    @Override // android.os.Handler
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
                                    ViewportWindow.this.setAlpha(MAX_ALPHA);
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

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    Region magnifiedBounds = (Region) ((SomeArgs) message.obj).arg1;
                    DisplayMagnifier.this.mCallbacks.onMagnificationRegionChanged(magnifiedBounds);
                    magnifiedBounds.recycle();
                } else if (i == 2) {
                    SomeArgs args = (SomeArgs) message.obj;
                    DisplayMagnifier.this.mCallbacks.onRectangleOnScreenRequested(args.argi1, args.argi2, args.argi3, args.argi4);
                    args.recycle();
                } else if (i == 3) {
                    DisplayMagnifier.this.mCallbacks.onUserContextChanged();
                } else if (i == 4) {
                    DisplayMagnifier.this.mCallbacks.onRotationChanged(message.arg1);
                } else if (i == 5) {
                    synchronized (DisplayMagnifier.this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (DisplayMagnifier.this.mMagnifedViewport.isMagnifyingLocked() || DisplayMagnifier.this.isForceShowingMagnifiableBoundsLocked()) {
                                DisplayMagnifier.this.mMagnifedViewport.setMagnifiedRegionBorderShownLocked(true, true);
                                DisplayMagnifier.this.mService.scheduleAnimationLocked();
                            }
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class WindowsForAccessibilityObserver {
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

        public WindowsForAccessibilityObserver(WindowManagerService windowManagerService, WindowManagerInternal.WindowsForAccessibilityCallback callback) {
            this.mContext = windowManagerService.mContext;
            this.mService = windowManagerService;
            this.mCallback = callback;
            this.mHandler = new MyHandler(this.mService.mH.getLooper());
            this.mRecurringAccessibilityEventsIntervalMillis = ViewConfiguration.getSendRecurringAccessibilityEventsInterval();
            computeChangedWindows(true);
        }

        public void performComputeChangedWindowsNotLocked(boolean forceSend) {
            this.mHandler.removeMessages(1);
            computeChangedWindows(forceSend);
        }

        public void scheduleComputeChangedWindowsLocked() {
            if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, this.mRecurringAccessibilityEventsIntervalMillis);
            }
        }

        public void computeChangedWindows(boolean forceSend) {
            Throwable th;
            boolean windowsChanged;
            boolean windowsChanged2;
            boolean windowsChanged3 = false;
            List<WindowInfo> windows = new ArrayList<>();
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mService.getDefaultDisplayContentLocked().mCurrentFocus == null) {
                        try {
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return;
                        } catch (Throwable th2) {
                            th = th2;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } else {
                        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
                        windowManager.getDefaultDisplay().getRealSize(this.mTempPoint);
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
                        HashSet<Integer> skipRemainingWindowsForTasks = new HashSet<>();
                        for (int i = visibleWindowCount - 1; i >= 0; i--) {
                            WindowState windowState = visibleWindows.valueAt(i);
                            Rect boundsInScreen = this.mTempRect;
                            computeWindowBoundsInScreen(windowState, boundsInScreen);
                            if (windowMattersToAccessibility(windowState, boundsInScreen, unaccountedSpace, skipRemainingWindowsForTasks)) {
                                addPopulatedWindowInfo(windowState, boundsInScreen, windows, addedWindows);
                                updateUnaccountedSpace(windowState, boundsInScreen, unaccountedSpace, skipRemainingWindowsForTasks);
                                focusedWindowAdded |= windowState.isFocused();
                            }
                            if (unaccountedSpace.isEmpty() && focusedWindowAdded) {
                                break;
                            }
                        }
                        int windowCount = windows.size();
                        int i2 = 0;
                        while (i2 < windowCount) {
                            WindowInfo window = windows.get(i2);
                            if (!addedWindows.contains(window.parentToken)) {
                                window.parentToken = null;
                            }
                            if (window.childTokens != null) {
                                int j = window.childTokens.size() - 1;
                                while (j >= 0) {
                                    try {
                                        if (!addedWindows.contains(window.childTokens.get(j))) {
                                            window.childTokens.remove(j);
                                        }
                                        j--;
                                        windowsChanged3 = windowsChanged3;
                                    } catch (Throwable th3) {
                                        th = th3;
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                                windowsChanged2 = windowsChanged3;
                            } else {
                                windowsChanged2 = windowsChanged3;
                            }
                            i2++;
                            windowManager = windowManager;
                            windowsChanged3 = windowsChanged2;
                        }
                        visibleWindows.clear();
                        addedWindows.clear();
                        if (!forceSend) {
                            if (this.mOldWindows.size() != windows.size()) {
                                windowsChanged = true;
                            } else if (!this.mOldWindows.isEmpty() || !windows.isEmpty()) {
                                int i3 = 0;
                                while (true) {
                                    if (i3 >= windowCount) {
                                        break;
                                    } else if (windowChangedNoLayer(this.mOldWindows.get(i3), windows.get(i3))) {
                                        windowsChanged = true;
                                        break;
                                    } else {
                                        i3++;
                                    }
                                }
                            }
                            if (forceSend || windowsChanged) {
                                cacheWindows(windows);
                            }
                        }
                        windowsChanged = windowsChanged3;
                        cacheWindows(windows);
                    }
                } catch (Throwable th4) {
                    th = th4;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (forceSend || windowsChanged) {
                this.mCallback.onWindowsForAccessibilityChanged(windows);
            }
            clearAndRecycleWindows(windows);
        }

        private boolean windowMattersToAccessibility(WindowState windowState, Rect boundsInScreen, Region unaccountedSpace, HashSet<Integer> skipRemainingWindowsForTasks) {
            if (windowState.isFocused()) {
                return true;
            }
            Task task = windowState.getTask();
            if (task != null && skipRemainingWindowsForTasks.contains(Integer.valueOf(task.mTaskId))) {
                return false;
            }
            if (((windowState.mAttrs.flags & 16) == 0 || windowState.mAttrs.type == 2034) && !unaccountedSpace.quickReject(boundsInScreen) && isReportedWindowType(windowState.mAttrs.type)) {
                return true;
            }
            return false;
        }

        private void updateUnaccountedSpace(WindowState windowState, Rect boundsInScreen, Region unaccountedSpace, HashSet<Integer> skipRemainingWindowsForTasks) {
            if (windowState.mAttrs.type != 2032) {
                unaccountedSpace.op(boundsInScreen, unaccountedSpace, Region.Op.REVERSE_DIFFERENCE);
                if ((windowState.mAttrs.flags & 40) == 0) {
                    unaccountedSpace.op(windowState.getDisplayFrameLw(), unaccountedSpace, Region.Op.REVERSE_DIFFERENCE);
                    Task task = windowState.getTask();
                    if (task != null) {
                        skipRemainingWindowsForTasks.add(Integer.valueOf(task.mTaskId));
                    } else {
                        unaccountedSpace.setEmpty();
                    }
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
            windowFrame.offset((float) (-windowState.getFrameLw().left), (float) (-windowState.getFrameLw().top));
            Matrix matrix = this.mTempMatrix;
            AccessibilityController.populateTransformationMatrixLocked(windowState, matrix);
            Matrix tmpMatrix = new Matrix(matrix);
            tmpMatrix.preScale(windowState.mInvGlobalScale, windowState.mInvGlobalScale);
            tmpMatrix.mapRect(windowFrame);
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
            int newWindowCount = windows.size();
            for (int i2 = 0; i2 < newWindowCount; i2++) {
                this.mOldWindows.add(WindowInfo.obtain(windows.get(i2)));
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
            if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || this.mService.mRoot == null || (dc = this.mService.mRoot.getDisplayContent(HwPCUtils.getPCDisplayID())) != null) {
                this.mTempLayer = 0;
                dc.forAllWindows((Consumer<WindowState>) new Consumer(outWindows) {
                    /* class com.android.server.wm.$$Lambda$AccessibilityController$WindowsForAccessibilityObserver$vRhBz0DqTZWNemKfoIyId7HacTk */
                    private final /* synthetic */ SparseArray f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        AccessibilityController.WindowsForAccessibilityObserver.this.lambda$populateVisibleWindowsOnScreenLocked$0$AccessibilityController$WindowsForAccessibilityObserver(this.f$1, (WindowState) obj);
                    }
                }, false);
                this.mService.mRoot.forAllWindows((Consumer<WindowState>) new Consumer(outWindows) {
                    /* class com.android.server.wm.$$Lambda$AccessibilityController$WindowsForAccessibilityObserver$B_IsvCwgODOzAT2_hQ1EOsBsJg */
                    private final /* synthetic */ SparseArray f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        AccessibilityController.WindowsForAccessibilityObserver.this.lambda$populateVisibleWindowsOnScreenLocked$1$AccessibilityController$WindowsForAccessibilityObserver(this.f$1, (WindowState) obj);
                    }
                }, false);
                return;
            }
            HwPCUtils.log(LOG_TAG, "pc displaycontent is null");
        }

        public /* synthetic */ void lambda$populateVisibleWindowsOnScreenLocked$0$AccessibilityController$WindowsForAccessibilityObserver(SparseArray outWindows, WindowState w) {
            if (w.isVisibleLw()) {
                int i = this.mTempLayer;
                this.mTempLayer = i + 1;
                outWindows.put(i, w);
            }
        }

        public /* synthetic */ void lambda$populateVisibleWindowsOnScreenLocked$1$AccessibilityController$WindowsForAccessibilityObserver(SparseArray outWindows, WindowState w) {
            WindowState win = findRootDisplayParentWindow(w);
            if (win != null && win.getDisplayContent().isDefaultDisplay && w.isVisibleLw()) {
                int i = this.mTempLayer;
                this.mTempLayer = i + 1;
                outWindows.put(i, w);
            }
        }

        private WindowState findRootDisplayParentWindow(WindowState win) {
            WindowState displayParentWindow = win.getDisplayContent().getParentWindow();
            if (displayParentWindow == null) {
                return null;
            }
            WindowState candidate = displayParentWindow;
            while (candidate != null) {
                displayParentWindow = candidate;
                candidate = displayParentWindow.getDisplayContent().getParentWindow();
            }
            return displayParentWindow;
        }

        private class MyHandler extends Handler {
            public static final int MESSAGE_COMPUTE_CHANGED_WINDOWS = 1;

            public MyHandler(Looper looper) {
                super(looper, null, false);
            }

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (message.what == 1) {
                    WindowsForAccessibilityObserver.this.computeChangedWindows(false);
                }
            }
        }
    }
}

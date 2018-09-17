package com.android.server.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Slog;
import android.view.DisplayInfo;
import android.view.IDockedStackListener;
import android.view.SurfaceControl;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManagerInternal;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DockedDividerUtils;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerService.H;
import java.io.PrintWriter;
import java.util.ArrayList;

public class DockedStackDividerController implements DimLayerUser {
    private static final float CLIP_REVEAL_MEET_EARLIEST = 0.6f;
    private static final float CLIP_REVEAL_MEET_FRACTION_MAX = 0.8f;
    private static final float CLIP_REVEAL_MEET_FRACTION_MIN = 0.4f;
    private static final float CLIP_REVEAL_MEET_LAST = 1.0f;
    private static final int DIVIDER_WIDTH_INACTIVE_DP = 4;
    private static final long IME_ADJUST_ANIM_DURATION = 280;
    private static final long IME_ADJUST_DRAWN_TIMEOUT = 200;
    private static final Interpolator IME_ADJUST_ENTRY_INTERPOLATOR = null;
    private static final String TAG = null;
    private boolean mAdjustedForDivider;
    private boolean mAdjustedForIme;
    private boolean mAnimatingForIme;
    private boolean mAnimatingForMinimizedDockedStack;
    private long mAnimationDuration;
    private float mAnimationStart;
    private boolean mAnimationStartDelayed;
    private long mAnimationStartTime;
    private boolean mAnimationStarted;
    private float mAnimationTarget;
    private WindowState mDelayedImeWin;
    private final DimLayer mDimLayer;
    private final DisplayContent mDisplayContent;
    private float mDividerAnimationStart;
    private float mDividerAnimationTarget;
    private int mDividerInsets;
    private int mDividerWindowWidth;
    private int mDividerWindowWidthInactive;
    private final RemoteCallbackList<IDockedStackListener> mDockedStackListeners;
    private final Handler mHandler;
    private int mImeHeight;
    private boolean mImeHideRequested;
    private float mLastAnimationProgress;
    private float mLastDividerProgress;
    private final Rect mLastRect;
    private boolean mLastVisibility;
    private float mMaximizeMeetFraction;
    private boolean mMinimizedDock;
    private final Interpolator mMinimizedDockInterpolator;
    private boolean mResizing;
    private int mRotation;
    private final WindowManagerService mService;
    private final DividerSnapAlgorithm[] mSnapAlgorithmForRotation;
    private final Rect mTmpRect;
    private final Rect mTmpRect2;
    private final Rect mTmpRect3;
    private final Rect mTouchRegion;
    private WindowState mWindow;

    final /* synthetic */ class -void_startImeAdjustAnimation_boolean_adjustedForIme_boolean_adjustedForDivider_com_android_server_wm_WindowState_imeWin_LambdaImpl0 implements Runnable {
        private /* synthetic */ boolean val$adjustedForDivider;
        private /* synthetic */ boolean val$adjustedForIme;
        private /* synthetic */ DockedStackDividerController val$this;

        public /* synthetic */ -void_startImeAdjustAnimation_boolean_adjustedForIme_boolean_adjustedForDivider_com_android_server_wm_WindowState_imeWin_LambdaImpl0(DockedStackDividerController dockedStackDividerController, boolean z, boolean z2) {
            this.val$this = dockedStackDividerController;
            this.val$adjustedForIme = z;
            this.val$adjustedForDivider = z2;
        }

        public void run() {
            this.val$this.-com_android_server_wm_DockedStackDividerController_lambda$1(this.val$adjustedForIme, this.val$adjustedForDivider);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.DockedStackDividerController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.DockedStackDividerController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.DockedStackDividerController.<clinit>():void");
    }

    DockedStackDividerController(WindowManagerService service, DisplayContent displayContent) {
        this.mTmpRect = new Rect();
        this.mTmpRect2 = new Rect();
        this.mTmpRect3 = new Rect();
        this.mLastRect = new Rect();
        this.mLastVisibility = false;
        this.mDockedStackListeners = new RemoteCallbackList();
        this.mTouchRegion = new Rect();
        this.mSnapAlgorithmForRotation = new DividerSnapAlgorithm[DIVIDER_WIDTH_INACTIVE_DP];
        this.mRotation = 0;
        this.mHandler = new Handler();
        this.mService = service;
        this.mDisplayContent = displayContent;
        Context context = service.mContext;
        this.mDimLayer = new DimLayer(displayContent.mService, this, displayContent.getDisplayId(), "DockedStackDim");
        this.mMinimizedDockInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        loadDimens();
    }

    int getSmallestWidthDpForBounds(Rect bounds) {
        DisplayInfo di = this.mDisplayContent.getDisplayInfo();
        if (bounds == null || (bounds.left == 0 && bounds.top == 0 && bounds.right == di.logicalWidth && bounds.bottom == di.logicalHeight)) {
            return this.mService.mCurConfiguration.smallestScreenWidthDp;
        }
        int baseDisplayWidth = this.mDisplayContent.mBaseDisplayWidth;
        int baseDisplayHeight = this.mDisplayContent.mBaseDisplayHeight;
        int minWidth = Integer.MAX_VALUE;
        int rotation = 0;
        while (rotation < DIVIDER_WIDTH_INACTIVE_DP) {
            int i;
            int i2;
            int orientation;
            this.mTmpRect.set(bounds);
            this.mDisplayContent.rotateBounds(di.rotation, rotation, this.mTmpRect);
            boolean rotated = rotation == 1 || rotation == 3;
            Rect rect = this.mTmpRect2;
            if (rotated) {
                i = baseDisplayHeight;
            } else {
                i = baseDisplayWidth;
            }
            if (rotated) {
                i2 = baseDisplayWidth;
            } else {
                i2 = baseDisplayHeight;
            }
            rect.set(0, 0, i, i2);
            if (this.mTmpRect2.width() <= this.mTmpRect2.height()) {
                orientation = 1;
            } else {
                orientation = 2;
            }
            int dockSide = TaskStack.getDockSideUnchecked(this.mTmpRect, this.mTmpRect2, orientation);
            DockedDividerUtils.calculateBoundsForPosition(this.mSnapAlgorithmForRotation[rotation].calculateNonDismissingSnapTarget(DockedDividerUtils.calculatePositionForBounds(this.mTmpRect, dockSide, getContentWidth())).position, dockSide, this.mTmpRect, this.mTmpRect2.width(), this.mTmpRect2.height(), getContentWidth());
            this.mService.mPolicy.getStableInsetsLw(rotation, this.mTmpRect2.width(), this.mTmpRect2.height(), this.mTmpRect3);
            this.mService.subtractInsets(this.mTmpRect2, this.mTmpRect3, this.mTmpRect);
            minWidth = Math.min(this.mTmpRect.width(), minWidth);
            rotation++;
        }
        return (int) (((float) minWidth) / this.mDisplayContent.getDisplayMetrics().density);
    }

    private void initSnapAlgorithmForRotations() {
        Configuration baseConfig = this.mService.mCurConfiguration;
        Configuration config = new Configuration();
        int rotation = 0;
        while (rotation < DIVIDER_WIDTH_INACTIVE_DP) {
            boolean rotated;
            int dw;
            int dh;
            boolean z;
            if (rotation == 1 || rotation == 3) {
                rotated = true;
            } else {
                rotated = false;
            }
            if (rotated) {
                dw = this.mDisplayContent.mBaseDisplayHeight;
            } else {
                dw = this.mDisplayContent.mBaseDisplayWidth;
            }
            if (rotated) {
                dh = this.mDisplayContent.mBaseDisplayWidth;
            } else {
                dh = this.mDisplayContent.mBaseDisplayHeight;
            }
            this.mService.mPolicy.getStableInsetsLw(rotation, dw, dh, this.mTmpRect);
            config.setToDefaults();
            config.orientation = dw <= dh ? 1 : 2;
            config.screenWidthDp = (int) (((float) this.mService.mPolicy.getConfigDisplayWidth(dw, dh, rotation, baseConfig.uiMode)) / this.mDisplayContent.getDisplayMetrics().density);
            config.screenHeightDp = (int) (((float) this.mService.mPolicy.getConfigDisplayHeight(dw, dh, rotation, baseConfig.uiMode)) / this.mDisplayContent.getDisplayMetrics().density);
            Context rotationContext = this.mService.mContext.createConfigurationContext(config);
            DividerSnapAlgorithm[] dividerSnapAlgorithmArr = this.mSnapAlgorithmForRotation;
            Resources resources = rotationContext.getResources();
            int contentWidth = getContentWidth();
            if (config.orientation == 1) {
                z = true;
            } else {
                z = false;
            }
            dividerSnapAlgorithmArr[rotation] = new DividerSnapAlgorithm(resources, dw, dh, contentWidth, z, this.mTmpRect);
            rotation++;
        }
    }

    private void loadDimens() {
        Context context = this.mService.mContext;
        this.mDividerWindowWidth = context.getResources().getDimensionPixelSize(17104929);
        this.mDividerInsets = context.getResources().getDimensionPixelSize(17104930);
        this.mDividerWindowWidthInactive = WindowManagerService.dipToPixel(DIVIDER_WIDTH_INACTIVE_DP, this.mDisplayContent.getDisplayMetrics());
        initSnapAlgorithmForRotations();
    }

    void onConfigurationChanged() {
        loadDimens();
    }

    boolean isResizing() {
        return this.mResizing;
    }

    int getContentWidth() {
        return this.mDividerWindowWidth - (this.mDividerInsets * 2);
    }

    int getContentInsets() {
        return this.mDividerInsets;
    }

    int getContentWidthInactive() {
        return this.mDividerWindowWidthInactive;
    }

    void setResizing(boolean resizing) {
        if (this.mResizing != resizing) {
            this.mResizing = resizing;
            resetDragResizingChangeReported();
            if (!this.mResizing && this.mRotation != this.mDisplayContent.getDisplay().getRotation()) {
                this.mHandler.post(new Runnable() {
                    public void run() {
                        DockedStackDividerController.this.mService.updateRotation(false, false);
                    }
                });
            }
        }
    }

    void setTouchRegion(Rect touchRegion) {
        this.mTouchRegion.set(touchRegion);
    }

    void getTouchRegion(Rect outRegion) {
        outRegion.set(this.mTouchRegion);
        outRegion.offset(this.mWindow.getFrameLw().left, this.mWindow.getFrameLw().top);
    }

    private void resetDragResizingChangeReported() {
        WindowList windowList = this.mDisplayContent.getWindowList();
        for (int i = windowList.size() - 1; i >= 0; i--) {
            ((WindowState) windowList.get(i)).resetDragResizingChangeReported();
        }
    }

    void setWindow(WindowState window) {
        this.mWindow = window;
        reevaluateVisibility(false);
    }

    void reevaluateVisibility(boolean force) {
        if (this.mWindow != null) {
            boolean visible = ((TaskStack) this.mDisplayContent.mService.mStackIdToStack.get(3)) != null;
            if (this.mLastVisibility != visible || force) {
                this.mLastVisibility = visible;
                notifyDockedDividerVisibilityChanged(visible);
                if (!visible) {
                    setResizeDimLayer(false, -1, 0.0f);
                }
            }
        }
    }

    boolean wasVisible() {
        return this.mLastVisibility;
    }

    void setAdjustedForIme(boolean adjustedForIme, boolean adjustedForDivider, boolean animate, WindowState imeWin, int imeHeight) {
        if (this.mAdjustedForIme == adjustedForIme && (!adjustedForIme || this.mImeHeight == imeHeight)) {
            if (this.mAdjustedForDivider == adjustedForDivider) {
                return;
            }
        }
        if (!animate || this.mAnimatingForMinimizedDockedStack) {
            notifyAdjustedForImeChanged(!adjustedForIme ? adjustedForDivider : true, 0);
        } else {
            startImeAdjustAnimation(adjustedForIme, adjustedForDivider, imeWin);
        }
        this.mAdjustedForIme = adjustedForIme;
        this.mImeHeight = imeHeight;
        this.mAdjustedForDivider = adjustedForDivider;
    }

    int getImeHeightAdjustedFor() {
        return this.mImeHeight;
    }

    void positionDockedStackedDivider(Rect frame) {
        TaskStack stack = this.mDisplayContent.getDockedStackLocked();
        if (stack == null) {
            frame.set(this.mLastRect);
            return;
        }
        stack.getDimBounds(this.mTmpRect);
        switch (stack.getDockSide()) {
            case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                frame.set(this.mTmpRect.right - this.mDividerInsets, frame.top, (this.mTmpRect.right + frame.width()) - this.mDividerInsets, frame.bottom);
                break;
            case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                frame.set(frame.left, this.mTmpRect.bottom - this.mDividerInsets, this.mTmpRect.right, (this.mTmpRect.bottom + frame.height()) - this.mDividerInsets);
                break;
            case H.REPORT_LOSING_FOCUS /*3*/:
                frame.set((this.mTmpRect.left - frame.width()) + this.mDividerInsets, frame.top, this.mTmpRect.left + this.mDividerInsets, frame.bottom);
                break;
            case DIVIDER_WIDTH_INACTIVE_DP /*4*/:
                frame.set(frame.left, (this.mTmpRect.top - frame.height()) + this.mDividerInsets, frame.right, this.mTmpRect.top + this.mDividerInsets);
                break;
        }
        this.mLastRect.set(frame);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void notifyDockedDividerVisibilityChanged(boolean visible) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDividerVisibilityChanged(visible);
                } catch (RemoteException e) {
                    Slog.e("WindowManager", "Error delivering divider visibility changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void notifyDockedStackExistsChanged(boolean exists) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDockedStackExistsChanged(exists);
                } catch (RemoteException e) {
                    Slog.e("WindowManager", "Error delivering docked stack exists changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
        if (exists) {
            InputMethodManagerInternal inputMethodManagerInternal = (InputMethodManagerInternal) LocalServices.getService(InputMethodManagerInternal.class);
            if (inputMethodManagerInternal != null) {
                inputMethodManagerInternal.hideCurrentInputMethod();
                this.mImeHideRequested = true;
                return;
            }
            return;
        }
        this.mMinimizedDock = false;
        setMinimizedDockedStack(false);
    }

    void resetImeHideRequested() {
        this.mImeHideRequested = false;
    }

    boolean isImeHideRequested() {
        return this.mImeHideRequested;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void notifyDockedStackMinimizedChanged(boolean minimizedDock, long animDuration) {
        int i;
        this.mService.mH.removeMessages(53);
        H h = this.mService.mH;
        if (minimizedDock) {
            i = 1;
        } else {
            i = 0;
        }
        h.obtainMessage(53, i, 0).sendToTarget();
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i2 = 0; i2 < size; i2++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i2)).onDockedStackMinimizedChanged(minimizedDock, animDuration);
                } catch (RemoteException e) {
                    Slog.e("WindowManager", "Error delivering minimized dock changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void notifyDockSideChanged(int newDockSide) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onDockSideChanged(newDockSide);
                } catch (RemoteException e) {
                    Slog.e("WindowManager", "Error delivering dock side changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void notifyAdjustedForImeChanged(boolean adjustedForIme, long animDuration) {
        synchronized (this.mDockedStackListeners) {
            int size = this.mDockedStackListeners.beginBroadcast();
            for (int i = 0; i < size; i++) {
                try {
                    ((IDockedStackListener) this.mDockedStackListeners.getBroadcastItem(i)).onAdjustedForImeChanged(adjustedForIme, animDuration);
                } catch (RemoteException e) {
                    Slog.e("WindowManager", "Error delivering adjusted for ime changed event.", e);
                }
            }
            this.mDockedStackListeners.finishBroadcast();
        }
    }

    void registerDockedStackListener(IDockedStackListener listener) {
        synchronized (this.mDockedStackListeners) {
            this.mDockedStackListeners.register(listener);
        }
        notifyDockedDividerVisibilityChanged(wasVisible());
        notifyDockedStackExistsChanged(this.mDisplayContent.mService.mStackIdToStack.get(3) != null);
        notifyDockedStackMinimizedChanged(this.mMinimizedDock, 0);
        notifyAdjustedForImeChanged(this.mAdjustedForIme, 0);
    }

    void setResizeDimLayer(boolean visible, int targetStackId, float alpha) {
        boolean z = false;
        SurfaceControl.openTransaction();
        TaskStack stack = (TaskStack) this.mDisplayContent.mService.mStackIdToStack.get(targetStackId);
        TaskStack dockedStack = this.mDisplayContent.getDockedStackLocked();
        if (!(!visible || stack == null || dockedStack == null)) {
            z = true;
        }
        boolean visibleAndValid = z;
        if (visibleAndValid) {
            stack.getDimBounds(this.mTmpRect);
            if (this.mTmpRect.height() <= 0 || this.mTmpRect.width() <= 0) {
                visibleAndValid = false;
            } else {
                this.mDimLayer.setBounds(this.mTmpRect);
                this.mDimLayer.show(this.mService.mLayersController.getResizeDimLayer(), alpha, 0);
            }
        }
        if (!visibleAndValid) {
            this.mDimLayer.hide();
        }
        SurfaceControl.closeTransaction();
    }

    void notifyAppVisibilityChanged() {
        checkMinimizeChanged(false);
    }

    void notifyAppTransitionStarting() {
        checkMinimizeChanged(true);
    }

    boolean isMinimizedDock() {
        return this.mMinimizedDock;
    }

    private void checkMinimizeChanged(boolean animate) {
        boolean z = false;
        if (this.mDisplayContent.getDockedStackVisibleForUserLocked() != null) {
            TaskStack homeStack = this.mDisplayContent.getHomeStack();
            if (homeStack != null) {
                Task homeTask = homeStack.findHomeTask();
                if (homeTask != null && isWithinDisplay(homeTask)) {
                    TaskStack fullscreenStack = (TaskStack) this.mService.mStackIdToStack.get(1);
                    ArrayList<Task> homeStackTasks = homeStack.getTasks();
                    Task topHomeStackTask = (Task) homeStackTasks.get(homeStackTasks.size() - 1);
                    boolean homeVisible = homeTask.getTopVisibleAppToken() != null;
                    boolean homeBehind = (fullscreenStack == null || !fullscreenStack.isVisibleLocked()) ? homeStackTasks.size() > 1 && topHomeStackTask != homeTask : true;
                    if (homeVisible && !homeBehind) {
                        z = true;
                    }
                    setMinimizedDockedStack(z, animate);
                }
            }
        }
    }

    private boolean isWithinDisplay(Task task) {
        task.mStack.getBounds(this.mTmpRect);
        this.mDisplayContent.getLogicalDisplayRect(this.mTmpRect2);
        return this.mTmpRect.intersect(this.mTmpRect2);
    }

    private void setMinimizedDockedStack(boolean minimizedDock, boolean animate) {
        boolean wasMinimized = this.mMinimizedDock;
        this.mMinimizedDock = minimizedDock;
        if (minimizedDock != wasMinimized) {
            clearImeAdjustAnimation();
            if (minimizedDock) {
                if (animate) {
                    startAdjustAnimation(0.0f, CLIP_REVEAL_MEET_LAST);
                } else {
                    setMinimizedDockedStack(true);
                }
            } else if (animate) {
                startAdjustAnimation(CLIP_REVEAL_MEET_LAST, 0.0f);
            } else {
                setMinimizedDockedStack(false);
            }
        }
    }

    private void clearImeAdjustAnimation() {
        ArrayList<TaskStack> stacks = this.mDisplayContent.getStacks();
        for (int i = stacks.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) stacks.get(i);
            if (stack != null && stack.isAdjustedForIme()) {
                stack.resetAdjustedForIme(true);
            }
        }
        this.mAnimatingForIme = false;
    }

    private void startAdjustAnimation(float from, float to) {
        this.mAnimatingForMinimizedDockedStack = true;
        this.mAnimationStarted = false;
        this.mAnimationStart = from;
        this.mAnimationTarget = to;
    }

    private void startImeAdjustAnimation(boolean adjustedForIme, boolean adjustedForDivider, WindowState imeWin) {
        int i;
        int i2 = 0;
        boolean z = true;
        if (this.mAnimatingForIme) {
            this.mAnimationStart = this.mLastAnimationProgress;
            this.mDividerAnimationStart = this.mLastDividerProgress;
        } else {
            if (this.mAdjustedForIme) {
                i = 1;
            } else {
                i = 0;
            }
            this.mAnimationStart = (float) i;
            if (this.mAdjustedForDivider) {
                i = 1;
            } else {
                i = 0;
            }
            this.mDividerAnimationStart = (float) i;
            this.mLastAnimationProgress = this.mAnimationStart;
            this.mLastDividerProgress = this.mDividerAnimationStart;
        }
        this.mAnimatingForIme = true;
        this.mAnimationStarted = false;
        if (adjustedForIme) {
            i = 1;
        } else {
            i = 0;
        }
        this.mAnimationTarget = (float) i;
        if (adjustedForDivider) {
            i2 = 1;
        }
        this.mDividerAnimationTarget = (float) i2;
        ArrayList<TaskStack> stacks = this.mDisplayContent.getStacks();
        for (int i3 = stacks.size() - 1; i3 >= 0; i3--) {
            TaskStack stack = (TaskStack) stacks.get(i3);
            if (stack.isVisibleLocked() && stack.isAdjustedForIme()) {
                stack.beginImeAdjustAnimation();
            }
        }
        if (this.mService.mWaitingForDrawn.isEmpty()) {
            if (!adjustedForIme) {
                z = adjustedForDivider;
            }
            notifyAdjustedForImeChanged(z, IME_ADJUST_ANIM_DURATION);
            return;
        }
        this.mService.mH.removeMessages(24);
        this.mService.mH.sendEmptyMessageDelayed(24, IME_ADJUST_DRAWN_TIMEOUT);
        this.mAnimationStartDelayed = true;
        if (imeWin != null) {
            if (this.mDelayedImeWin != null) {
                this.mDelayedImeWin.mWinAnimator.endDelayingAnimationStart();
            }
            this.mDelayedImeWin = imeWin;
            imeWin.mWinAnimator.startDelayingAnimationStart();
        }
        this.mService.mWaitingForDrawnCallback = new -void_startImeAdjustAnimation_boolean_adjustedForIme_boolean_adjustedForDivider_com_android_server_wm_WindowState_imeWin_LambdaImpl0(this, adjustedForIme, adjustedForDivider);
    }

    /* synthetic */ void -com_android_server_wm_DockedStackDividerController_lambda$1(boolean adjustedForIme, boolean adjustedForDivider) {
        this.mAnimationStartDelayed = false;
        if (this.mDelayedImeWin != null) {
            this.mDelayedImeWin.mWinAnimator.endDelayingAnimationStart();
        }
        if (adjustedForIme) {
            adjustedForDivider = true;
        }
        notifyAdjustedForImeChanged(adjustedForDivider, IME_ADJUST_ANIM_DURATION);
    }

    private void setMinimizedDockedStack(boolean minimized) {
        TaskStack stack = this.mDisplayContent.getDockedStackVisibleForUserLocked();
        notifyDockedStackMinimizedChanged(minimized, 0);
        if (stack != null) {
            if (stack.setAdjustedForMinimizedDock(minimized ? CLIP_REVEAL_MEET_LAST : 0.0f)) {
                this.mService.mWindowPlacerLocked.performSurfacePlacement();
            }
        }
    }

    private boolean isAnimationMaximizing() {
        return this.mAnimationTarget == 0.0f;
    }

    public boolean animate(long now) {
        if (this.mWindow == null) {
            return false;
        }
        if (this.mAnimatingForMinimizedDockedStack) {
            return animateForMinimizedDockedStack(now);
        }
        if (this.mAnimatingForIme) {
            return animateForIme(now);
        }
        if (this.mDimLayer != null && this.mDimLayer.isDimming()) {
            this.mDimLayer.setLayer(this.mService.mLayersController.getResizeDimLayer());
        }
        return false;
    }

    private boolean animateForIme(long now) {
        if (!this.mAnimationStarted || this.mAnimationStartDelayed) {
            this.mAnimationStarted = true;
            this.mAnimationStartTime = now;
            this.mAnimationDuration = (long) (this.mService.getWindowAnimationScaleLocked() * 280.0f);
        }
        float t = (this.mAnimationTarget == CLIP_REVEAL_MEET_LAST ? IME_ADJUST_ENTRY_INTERPOLATOR : AppTransition.TOUCH_RESPONSE_INTERPOLATOR).getInterpolation(Math.min(CLIP_REVEAL_MEET_LAST, ((float) (now - this.mAnimationStartTime)) / ((float) this.mAnimationDuration)));
        ArrayList<TaskStack> stacks = this.mDisplayContent.getStacks();
        boolean updated = false;
        for (int i = stacks.size() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) stacks.get(i);
            if (stack != null && stack.isAdjustedForIme()) {
                if (t >= CLIP_REVEAL_MEET_LAST && this.mAnimationTarget == 0.0f && this.mDividerAnimationTarget == 0.0f) {
                    stack.resetAdjustedForIme(true);
                    updated = true;
                } else {
                    this.mLastAnimationProgress = getInterpolatedAnimationValue(t);
                    this.mLastDividerProgress = getInterpolatedDividerValue(t);
                    updated |= stack.updateAdjustForIme(this.mLastAnimationProgress, this.mLastDividerProgress, false);
                }
                if (t >= CLIP_REVEAL_MEET_LAST) {
                    stack.endImeAdjustAnimation();
                }
            }
        }
        if (updated) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
        if (t < CLIP_REVEAL_MEET_LAST) {
            return true;
        }
        this.mLastAnimationProgress = this.mAnimationTarget;
        this.mLastDividerProgress = this.mDividerAnimationTarget;
        this.mAnimatingForIme = false;
        return false;
    }

    private boolean animateForMinimizedDockedStack(long now) {
        TaskStack stack = (TaskStack) this.mService.mStackIdToStack.get(3);
        if (!this.mAnimationStarted) {
            long transitionDuration;
            this.mAnimationStarted = true;
            this.mAnimationStartTime = now;
            if (isAnimationMaximizing()) {
                transitionDuration = this.mService.mAppTransition.getLastClipRevealTransitionDuration();
            } else {
                transitionDuration = 250;
            }
            this.mAnimationDuration = (long) (((float) transitionDuration) * this.mService.getTransitionAnimationScaleLocked());
            this.mMaximizeMeetFraction = getClipRevealMeetFraction(stack);
            notifyDockedStackMinimizedChanged(this.mMinimizedDock, (long) (((float) this.mAnimationDuration) * this.mMaximizeMeetFraction));
        }
        float t = (isAnimationMaximizing() ? AppTransition.TOUCH_RESPONSE_INTERPOLATOR : this.mMinimizedDockInterpolator).getInterpolation(Math.min(CLIP_REVEAL_MEET_LAST, ((float) (now - this.mAnimationStartTime)) / ((float) this.mAnimationDuration)));
        if (stack != null && stack.setAdjustedForMinimizedDock(getMinimizeAmount(stack, t))) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
        if (t < CLIP_REVEAL_MEET_LAST) {
            return true;
        }
        this.mAnimatingForMinimizedDockedStack = false;
        return false;
    }

    private float getInterpolatedAnimationValue(float t) {
        return (this.mAnimationTarget * t) + ((CLIP_REVEAL_MEET_LAST - t) * this.mAnimationStart);
    }

    private float getInterpolatedDividerValue(float t) {
        return (this.mDividerAnimationTarget * t) + ((CLIP_REVEAL_MEET_LAST - t) * this.mDividerAnimationStart);
    }

    private float getMinimizeAmount(TaskStack stack, float t) {
        float naturalAmount = getInterpolatedAnimationValue(t);
        if (isAnimationMaximizing()) {
            return adjustMaximizeAmount(stack, t, naturalAmount);
        }
        return naturalAmount;
    }

    private float adjustMaximizeAmount(TaskStack stack, float t, float naturalAmount) {
        if (this.mMaximizeMeetFraction == CLIP_REVEAL_MEET_LAST) {
            return naturalAmount;
        }
        float amountPrime = (this.mAnimationTarget * t) + ((CLIP_REVEAL_MEET_LAST - t) * (((float) this.mService.mAppTransition.getLastClipRevealMaxTranslation()) / ((float) stack.getMinimizeDistance())));
        float t2 = Math.min(t / this.mMaximizeMeetFraction, CLIP_REVEAL_MEET_LAST);
        return (amountPrime * t2) + ((CLIP_REVEAL_MEET_LAST - t2) * naturalAmount);
    }

    private float getClipRevealMeetFraction(TaskStack stack) {
        if (!isAnimationMaximizing() || stack == null || !this.mService.mAppTransition.hadClipRevealAnimation()) {
            return CLIP_REVEAL_MEET_LAST;
        }
        return ((CLIP_REVEAL_MEET_LAST - Math.max(0.0f, Math.min(CLIP_REVEAL_MEET_LAST, ((((float) Math.abs(this.mService.mAppTransition.getLastClipRevealMaxTranslation())) / ((float) stack.getMinimizeDistance())) - CLIP_REVEAL_MEET_FRACTION_MIN) / CLIP_REVEAL_MEET_FRACTION_MIN))) * 0.39999998f) + CLIP_REVEAL_MEET_EARLIEST;
    }

    public boolean dimFullscreen() {
        return false;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayContent.getDisplayInfo();
    }

    public void getDimBounds(Rect outBounds) {
    }

    public String toShortString() {
        return TAG;
    }

    WindowState getWindow() {
        return this.mWindow;
    }

    void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "DockedStackDividerController");
        pw.println(prefix + "  mLastVisibility=" + this.mLastVisibility);
        pw.println(prefix + "  mMinimizedDock=" + this.mMinimizedDock);
        pw.println(prefix + "  mAdjustedForIme=" + this.mAdjustedForIme);
        pw.println(prefix + "  mAdjustedForDivider=" + this.mAdjustedForDivider);
        if (this.mDimLayer.isDimming()) {
            pw.println(prefix + "  Dim layer is dimming: ");
            this.mDimLayer.printTo(prefix + "    ", pw);
        }
    }

    public void setDockedStackDividerRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void adjustBoundsForSingleHand() {
        this.mDimLayer.adjustBoundsForSingleHand();
    }
}

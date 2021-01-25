package com.android.server.wm;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManagerInternal;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.DragEvent;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputChannel;
import android.view.InputWindowHandle;
import android.view.SurfaceControl;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.internal.view.IDragAndDropPermissions;
import com.android.server.LocalServices;
import com.huawei.android.view.HwWindowManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
public class DragState {
    private static final String ANIMATED_PROPERTY_ALPHA = "alpha";
    private static final String ANIMATED_PROPERTY_SCALE = "scale";
    private static final String ANIMATED_PROPERTY_X = "x";
    private static final String ANIMATED_PROPERTY_Y = "y";
    private static final long DISAPPEAR_ANIM_DURATION = 250;
    private static final long DOCK_ICON_RETURN_DURATION = 200;
    private static final int DRAG_FLAGS_URI_ACCESS = 3;
    private static final int DRAG_FLAGS_URI_PERMISSIONS = 195;
    private static final long HW_MULTI_WINDOW_DISAPPEAR_ANIM_START_DELAY = 100;
    private static final long HW_PC_DRAG_END_DELAY = 50;
    private static final long MAX_ANIMATION_DURATION_MS = 375;
    private static final long MIN_ANIMATION_DURATION_MS = 195;
    private static final long REMOVE_HW_MULTI_WINDOW_DRAG_SURFACE_TIME_OUT_MS = 150;
    private static final long REMOVE_HW_MULTI_WINDOW_FREE_FORM_OR_SPLIT_SCREEN_DRAG_SURFACE_TIME_OUT_MS = 700;
    private static final float SURFACE_SIZE_DIVIDER = 2.0f;
    private static final long TRANSLATION_ANIM_DURATION = 350;
    volatile boolean mAnimationCompleted = false;
    private Animator mAnimator;
    boolean mCrossProfileCopyAllowed;
    private final Interpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    float mCurrentX;
    float mCurrentY;
    ClipData mData;
    ClipDescription mDataDescription;
    DisplayContent mDisplayContent;
    private Point mDisplaySize = new Point();
    final DragDropController mDragDropController;
    boolean mDragInProgress;
    boolean mDragResult;
    private final Interpolator mFastOutSlowInInterpolator = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    int mFlags;
    InputInterceptor mInputInterceptor;
    SurfaceControl mInputSurface;
    private boolean mIsClosing;
    boolean mIsDropSuccessAnimEnabled = false;
    IBinder mLocalWin;
    ArrayList<WindowState> mNotifiedWindows;
    float mOriginalAlpha;
    int mOriginalDragViewCenterX = 0;
    int mOriginalDragViewCenterY = 0;
    float mOriginalX;
    float mOriginalY;
    int mPendingDragEndedLocX = 0;
    int mPendingDragEndedLocY = 0;
    int mPid;
    final WindowManagerService mService;
    int mSourceUserId;
    SurfaceControl mSurfaceControl;
    float mSurfaceScale = 1.0f;
    WindowState mTargetWindow;
    float mThumbOffsetX;
    float mThumbOffsetY;
    private final Rect mTmpClipRect = new Rect();
    IBinder mToken;
    int mTouchSource;
    private final SurfaceControl.Transaction mTransaction;
    IBinder mTransferTouchFromToken;
    int mUid;
    float mWinLeft = 0.0f;
    float mWinTop = 0.0f;

    DragState(WindowManagerService service, DragDropController controller, IBinder token, SurfaceControl surface, int flags, IBinder localWin) {
        this.mService = service;
        this.mDragDropController = controller;
        this.mToken = token;
        this.mSurfaceControl = surface;
        this.mFlags = flags;
        this.mLocalWin = localWin;
        this.mNotifiedWindows = new ArrayList<>();
        this.mTransaction = service.mTransactionFactory.make();
    }

    /* access modifiers changed from: package-private */
    public boolean isClosing() {
        return this.mIsClosing;
    }

    private void hideInputSurface() {
        SurfaceControl surfaceControl = this.mInputSurface;
        if (surfaceControl != null) {
            this.mTransaction.hide(surfaceControl).apply();
        }
    }

    private void showInputSurface() {
        if (this.mInputSurface == null) {
            WindowManagerService windowManagerService = this.mService;
            this.mInputSurface = windowManagerService.makeSurfaceBuilder(windowManagerService.mRoot.getDisplayContent(this.mDisplayContent.getDisplayId()).getSession()).setContainerLayer().setName("Drag and Drop Input Consumer").build();
        }
        InputWindowHandle h = getInputWindowHandle();
        if (h == null) {
            Slog.w("WindowManager", "Drag is in progress but there is no drag window handle.");
            return;
        }
        this.mTransaction.show(this.mInputSurface);
        this.mTransaction.setInputWindowInfo(this.mInputSurface, h);
        this.mTransaction.setLayer(this.mInputSurface, Integer.MAX_VALUE);
        this.mTmpClipRect.set(0, 0, this.mDisplaySize.x, this.mDisplaySize.y);
        this.mTransaction.setWindowCrop(this.mInputSurface, this.mTmpClipRect);
        this.mTransaction.transferTouchFocus(this.mTransferTouchFromToken, h.token);
        this.mTransferTouchFromToken = null;
        this.mTransaction.syncInputWindows();
        this.mTransaction.apply();
    }

    /* access modifiers changed from: package-private */
    public void closeLocked() {
        long delay;
        float y;
        float y2;
        boolean isDragHwFreeFormOrSplit = true;
        this.mIsClosing = true;
        InputInterceptor inputInterceptor = this.mInputInterceptor;
        if (inputInterceptor != null) {
            this.mDragDropController.sendHandlerMessage(1, inputInterceptor);
            this.mInputInterceptor = null;
        }
        hideInputSurface();
        if (this.mDragInProgress) {
            int myPid = Process.myPid();
            Iterator<WindowState> it = this.mNotifiedWindows.iterator();
            while (it.hasNext()) {
                WindowState ws = it.next();
                if (this.mDragResult || ws.mSession.mPid != this.mPid) {
                    y = 0.0f;
                    y2 = 0.0f;
                } else {
                    float x = this.mCurrentX;
                    y = this.mCurrentY;
                    y2 = x;
                }
                DragEvent evt = DragEvent.obtain(4, y2, y, null, null, null, null, this.mDragResult);
                try {
                    ws.mClient.dispatchDragEvent(evt);
                } catch (RemoteException e) {
                    Slog.w("WindowManager", "Unable to drag-end window " + ws);
                }
                if (myPid != ws.mSession.mPid) {
                    evt.recycle();
                }
            }
            this.mNotifiedWindows.clear();
            this.mDragInProgress = false;
        }
        if (isFromSource(8194)) {
            this.mService.restorePointerIconLocked(this.mDisplayContent, this.mCurrentX, this.mCurrentY);
            this.mTouchSource = 0;
        }
        if (this.mSurfaceControl != null) {
            if (this.mIsDropSuccessAnimEnabled) {
                if ((this.mFlags & 1073741824) == 0) {
                    isDragHwFreeFormOrSplit = false;
                }
                if (isDragHwFreeFormOrSplit) {
                    delay = REMOVE_HW_MULTI_WINDOW_FREE_FORM_OR_SPLIT_SCREEN_DRAG_SURFACE_TIME_OUT_MS;
                    this.mDragDropController.mPendingRemoveSurface = this.mSurfaceControl;
                } else {
                    delay = REMOVE_HW_MULTI_WINDOW_DRAG_SURFACE_TIME_OUT_MS;
                }
                this.mDragDropController.sendTimeoutMessageDelayed(3, this.mSurfaceControl, delay);
            } else if (HwPCUtils.isInSinkWindowsCastMode() || HwPCUtils.isInBasicMode()) {
                Slog.i("WindowManager", "sinkWindowCast mode, clear surface later...");
                this.mDragDropController.sendTimeoutMessageDelayed(4, this.mSurfaceControl, HW_MULTI_WINDOW_DISAPPEAR_ANIM_START_DELAY);
                HwWindowManager.updateDragState(1);
                HwWindowManager.unregisterHwMultiDisplayDragStateListener();
            } else if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer()) {
                Slog.i("WindowManager", "remove drag surface");
                this.mTransaction.remove(this.mSurfaceControl).apply();
                this.mSurfaceControl = null;
            } else {
                Slog.i("WindowManager", "remove pc drag surface");
                this.mDragDropController.sendTimeoutMessageDelayed(4, this.mSurfaceControl, HW_PC_DRAG_END_DELAY);
            }
        }
        if (this.mAnimator != null && !this.mAnimationCompleted) {
            Slog.wtf("WindowManager", "Unexpectedly destroying mSurfaceControl while animation is running");
        }
        this.mFlags = 0;
        this.mLocalWin = null;
        this.mToken = null;
        this.mData = null;
        this.mThumbOffsetY = 0.0f;
        this.mThumbOffsetX = 0.0f;
        this.mNotifiedWindows = null;
        this.mDragDropController.onDragStateClosedLocked(this);
    }

    /* access modifiers changed from: package-private */
    public class InputInterceptor {
        InputChannel mClientChannel;
        InputApplicationHandle mDragApplicationHandle = new InputApplicationHandle(new Binder());
        InputWindowHandle mDragWindowHandle;
        DragInputEventReceiver mInputEventReceiver;
        InputChannel mServerChannel;

        InputInterceptor(Display display) {
            InputChannel[] channels = InputChannel.openInputChannelPair("drag");
            this.mServerChannel = channels[0];
            this.mClientChannel = channels[1];
            DragState.this.mService.mInputManager.registerInputChannel(this.mServerChannel, (IBinder) null);
            this.mInputEventReceiver = new DragInputEventReceiver(this.mClientChannel, DragState.this.mService.mH.getLooper(), DragState.this.mDragDropController);
            InputApplicationHandle inputApplicationHandle = this.mDragApplicationHandle;
            inputApplicationHandle.name = "drag";
            inputApplicationHandle.dispatchingTimeoutNanos = 5000000000L;
            this.mDragWindowHandle = new InputWindowHandle(inputApplicationHandle, (IWindow) null, display.getDisplayId());
            InputWindowHandle inputWindowHandle = this.mDragWindowHandle;
            inputWindowHandle.name = "drag";
            inputWindowHandle.token = this.mServerChannel.getToken();
            this.mDragWindowHandle.layer = DragState.this.getDragLayerLocked();
            InputWindowHandle inputWindowHandle2 = this.mDragWindowHandle;
            inputWindowHandle2.layoutParamsFlags = 0;
            inputWindowHandle2.layoutParamsType = 2016;
            inputWindowHandle2.dispatchingTimeoutNanos = 5000000000L;
            inputWindowHandle2.visible = true;
            inputWindowHandle2.canReceiveKeys = false;
            inputWindowHandle2.hasFocus = true;
            inputWindowHandle2.hasWallpaper = false;
            inputWindowHandle2.paused = false;
            inputWindowHandle2.ownerPid = Process.myPid();
            this.mDragWindowHandle.ownerUid = Process.myUid();
            InputWindowHandle inputWindowHandle3 = this.mDragWindowHandle;
            inputWindowHandle3.inputFeatures = 0;
            inputWindowHandle3.scaleFactor = 1.0f;
            inputWindowHandle3.touchableRegion.setEmpty();
            InputWindowHandle inputWindowHandle4 = this.mDragWindowHandle;
            inputWindowHandle4.frameLeft = 0;
            inputWindowHandle4.frameTop = 0;
            inputWindowHandle4.frameRight = DragState.this.mDisplaySize.x;
            this.mDragWindowHandle.frameBottom = DragState.this.mDisplaySize.y;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "Pausing rotation during drag");
            }
            DragState.this.mDisplayContent.pauseRotationLocked();
        }

        /* access modifiers changed from: package-private */
        public void tearDown() {
            DragState.this.mService.mInputManager.unregisterInputChannel(this.mServerChannel);
            this.mInputEventReceiver.dispose();
            this.mInputEventReceiver = null;
            this.mClientChannel.dispose();
            this.mServerChannel.dispose();
            this.mClientChannel = null;
            this.mServerChannel = null;
            this.mDragWindowHandle = null;
            this.mDragApplicationHandle = null;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d("WindowManager", "Resuming rotation after drag");
            }
            DragState.this.mDisplayContent.resumeRotationLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public InputChannel getInputChannel() {
        InputInterceptor inputInterceptor = this.mInputInterceptor;
        if (inputInterceptor == null) {
            return null;
        }
        return inputInterceptor.mServerChannel;
    }

    /* access modifiers changed from: package-private */
    public InputWindowHandle getInputWindowHandle() {
        InputInterceptor inputInterceptor = this.mInputInterceptor;
        if (inputInterceptor == null) {
            return null;
        }
        return inputInterceptor.mDragWindowHandle;
    }

    /* access modifiers changed from: package-private */
    public void register(Display display) {
        display.getRealSize(this.mDisplaySize);
        if (this.mInputInterceptor != null) {
            Slog.e("WindowManager", "Duplicate register of drag input channel");
            return;
        }
        this.mInputInterceptor = new InputInterceptor(display);
        showInputSurface();
    }

    /* access modifiers changed from: package-private */
    public int getDragLayerLocked() {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2016) * 10000) + 1000;
    }

    /* access modifiers changed from: package-private */
    public void setActTouchPoints(float actTouchX, float actTouchY) {
        this.mOriginalX = actTouchX;
        this.mOriginalY = actTouchY;
    }

    /* access modifiers changed from: package-private */
    public void broadcastDragStartedLocked(float touchX, float touchY) {
        this.mCurrentX = touchX;
        this.mOriginalX = touchX;
        this.mCurrentY = touchY;
        this.mOriginalY = touchY;
        ClipData clipData = this.mData;
        this.mDataDescription = clipData != null ? clipData.getDescription() : null;
        this.mNotifiedWindows.clear();
        this.mDragInProgress = true;
        this.mSourceUserId = UserHandle.getUserId(this.mUid);
        this.mCrossProfileCopyAllowed = true ^ ((UserManagerInternal) LocalServices.getService(UserManagerInternal.class)).getUserRestriction(this.mSourceUserId, "no_cross_profile_copy_paste");
        this.mDisplayContent.forAllWindows((Consumer<WindowState>) new Consumer(touchX, touchY) {
            /* class com.android.server.wm.$$Lambda$DragState$yUFIMrhYYccZ0gwd6eVcpAE93o */
            private final /* synthetic */ float f$1;
            private final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DragState.this.lambda$broadcastDragStartedLocked$0$DragState(this.f$1, this.f$2, (WindowState) obj);
            }
        }, false);
    }

    public /* synthetic */ void lambda$broadcastDragStartedLocked$0$DragState(float touchX, float touchY, WindowState w) {
        sendDragStartedLocked(w, touchX, touchY, this.mDataDescription);
    }

    private void sendDragStartedLocked(WindowState newWin, float touchX, float touchY, ClipDescription desc) {
        if (this.mDragInProgress && isValidDropTarget(newWin)) {
            DragEvent event = obtainDragEvent(newWin, 1, touchX, touchY, null, desc, null, null, false);
            try {
                newWin.mClient.dispatchDragEvent(event);
                this.mNotifiedWindows.add(newWin);
                if (Process.myPid() == newWin.mSession.mPid) {
                    return;
                }
            } catch (RemoteException e) {
                Slog.w("WindowManager", "Unable to drag-start window " + newWin);
                if (Process.myPid() == newWin.mSession.mPid) {
                    return;
                }
            } catch (Throwable th) {
                if (Process.myPid() != newWin.mSession.mPid) {
                    event.recycle();
                }
                throw th;
            }
            event.recycle();
        }
    }

    private boolean isValidDropTarget(WindowState targetWin) {
        if (targetWin == null || !targetWin.isPotentialDragTarget()) {
            return false;
        }
        DisplayContent displayContent = this.mDisplayContent;
        int displayId = displayContent == null ? -1 : displayContent.getDisplayId();
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId) && displayId != targetWin.getDisplayId()) {
            return false;
        }
        if (((this.mFlags & 256) == 0 || !targetWindowSupportsGlobalDrag(targetWin)) && this.mLocalWin != targetWin.mClient.asBinder()) {
            return false;
        }
        if (this.mCrossProfileCopyAllowed || this.mSourceUserId == UserHandle.getUserId(targetWin.getOwningUid())) {
            return true;
        }
        return false;
    }

    private boolean targetWindowSupportsGlobalDrag(WindowState targetWin) {
        return targetWin.mAppToken == null || targetWin.mAppToken.mTargetSdk >= 24;
    }

    /* access modifiers changed from: package-private */
    public void sendDragStartedIfNeededLocked(WindowState newWin) {
        if (this.mDragInProgress && !isWindowNotified(newWin)) {
            sendDragStartedLocked(newWin, this.mCurrentX, this.mCurrentY, this.mDataDescription);
        }
    }

    private boolean isWindowNotified(WindowState newWin) {
        Iterator<WindowState> it = this.mNotifiedWindows.iterator();
        while (it.hasNext()) {
            if (it.next() == newWin) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void endDragLocked() {
        ValueAnimator valueAnimator;
        if (this.mAnimator != null) {
            Slog.i("WindowManager", "endDragLocked return because mAnimator is not null");
        } else if (!this.mDragResult) {
            if (this.mIsDropSuccessAnimEnabled) {
                valueAnimator = createDockIconReturnAnimationLocked();
            } else {
                valueAnimator = createReturnAnimationLocked();
            }
            this.mAnimator = valueAnimator;
            Slog.i("WindowManager", "end drag and create return animation, enabled=" + this.mIsDropSuccessAnimEnabled);
        } else if (this.mIsDropSuccessAnimEnabled) {
            this.mAnimator = createDragEndedAnimationLocked();
            Slog.i("WindowManager", "end drag and create drag ended animation");
        } else {
            Slog.i("WindowManager", "end drag");
            closeLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void setPendingDragEndedLoc(int x, int y) {
        this.mPendingDragEndedLocX = x;
        this.mPendingDragEndedLocY = y;
    }

    /* access modifiers changed from: package-private */
    public void setOriginalDragViewCenter(int x, int y) {
        this.mOriginalDragViewCenterX = x;
        this.mOriginalDragViewCenterY = y;
    }

    public void setIsDropSuccessAnimEnabled(boolean isDropSuccessAnimEnabled) {
        Slog.i("WindowManager", "setIsDropSuccessAnimEnabled = " + isDropSuccessAnimEnabled);
        this.mIsDropSuccessAnimEnabled = isDropSuccessAnimEnabled;
    }

    /* access modifiers changed from: package-private */
    public void cancelDragLocked(boolean skipAnimation) {
        if (this.mAnimator == null) {
            if (!this.mDragInProgress || skipAnimation) {
                Slog.i("WindowManager", "cancel drag");
                closeLocked();
                return;
            }
            Slog.i("WindowManager", "cancel drag and create cancel animation");
            this.mAnimator = createCancelAnimationLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyMoveLocked(float x, float y) {
        if (this.mAnimator == null) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            float scale = 1.0f;
            float offsetX = 0.0f;
            float pointY = y;
            DisplayContent displayContent = this.mDisplayContent;
            int rotation = displayContent == null ? -1 : displayContent.getDisplayInfo().rotation;
            if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode()) {
                scale = this.mService.mSubFoldModeScale;
                if (rotation == 1) {
                    DisplayContent displayContent2 = this.mDisplayContent;
                    pointY = (((float) (displayContent2 == null ? 0 : displayContent2.getDisplayInfo().logicalHeight)) * (1.0f - (1.0f / scale))) + y;
                } else if (rotation == 3) {
                    DisplayContent displayContent3 = this.mDisplayContent;
                    offsetX = displayContent3 == null ? 0.0f : ((float) displayContent3.getDisplayInfo().logicalWidth) * (1.0f - scale);
                }
            }
            this.mTransaction.setPosition(this.mSurfaceControl, (x * scale) - this.mThumbOffsetX, (y * scale) - this.mThumbOffsetY).apply();
            notifyLocationLocked(x - (offsetX / scale), pointY);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyLocationLocked(float x, float y) {
        WindowState touchedWin;
        float pointY;
        float pointX;
        WindowState touchedWin2 = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
        if (touchedWin2 == null || isWindowNotified(touchedWin2)) {
            touchedWin = touchedWin2;
        } else {
            touchedWin = null;
        }
        try {
            int myPid = Process.myPid();
            if (!(touchedWin == this.mTargetWindow || this.mTargetWindow == null)) {
                DragEvent evt = obtainDragEvent(this.mTargetWindow, 6, 0.0f, 0.0f, null, null, null, null, false);
                this.mTargetWindow.mClient.dispatchDragEvent(evt);
                if (myPid != this.mTargetWindow.mSession.mPid) {
                    evt.recycle();
                }
            }
            if (touchedWin != null) {
                if (touchedWin.mAppToken == null || touchedWin.mAppToken.getStack() == null) {
                    pointX = x;
                    pointY = y;
                } else {
                    Rect rect = touchedWin.getBounds();
                    float winScale = touchedWin.mAppToken.getStack().mHwStackScale;
                    float pointX2 = ((float) rect.left) + ((x - ((float) rect.left)) / winScale);
                    pointY = ((float) rect.top) + ((y - ((float) rect.top)) / winScale);
                    pointX = pointX2;
                }
                DragEvent evt2 = obtainDragEvent(touchedWin, 2, pointX, pointY, null, null, null, null, false);
                touchedWin.mClient.dispatchDragEvent(evt2);
                if (myPid != touchedWin.mSession.mPid) {
                    evt2.recycle();
                }
            }
        } catch (RemoteException e) {
            Slog.w("WindowManager", "can't send drag notification to windows");
        }
        this.mTargetWindow = touchedWin;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0111, code lost:
        if (r11 != r13.mSession.mPid) goto L_0x0113;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0113, code lost:
        r4.recycle();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0139, code lost:
        if (r5 == r13.mSession.mPid) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x013c, code lost:
        r22.mToken = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x013e, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0146  */
    public void notifyDropLocked(float x, float y) {
        IDragAndDropPermissions iDragAndDropPermissions;
        float pointY;
        float pointX;
        int myPid;
        Throwable th;
        ClipData clipData;
        ClipData clipData2;
        if (this.mAnimator == null) {
            this.mCurrentX = x;
            this.mCurrentY = y;
            if (HwPCUtils.isInSinkWindowsCastMode() || HwPCUtils.isInBasicMode()) {
                HwWindowManager.setOriginalDropPoint(x, y);
                this.mService.setTouchWinState(null);
            }
            WindowState touchedWin = this.mDisplayContent.getTouchableWinAtPointLocked(x, y);
            if (!isWindowNotified(touchedWin)) {
                Slog.i("WindowManager", "drop outside a valid window");
                this.mDragResult = false;
                endDragLocked();
                return;
            }
            Slog.i("WindowManager", "sending DROP to " + touchedWin);
            int targetUserId = UserHandle.getUserId(touchedWin.getOwningUid());
            int i = this.mFlags;
            if ((i & 256) == 0 || (i & 3) == 0 || (clipData2 = this.mData) == null) {
                iDragAndDropPermissions = null;
            } else {
                iDragAndDropPermissions = new DragAndDropPermissionsHandler(clipData2, this.mUid, touchedWin.getOwningPackage(), this.mFlags & DRAG_FLAGS_URI_PERMISSIONS, this.mSourceUserId, targetUserId);
            }
            int i2 = this.mSourceUserId;
            if (!(i2 == targetUserId || (clipData = this.mData) == null)) {
                clipData.fixUris(i2);
            }
            int myPid2 = Process.myPid();
            IBinder token = touchedWin.mClient.asBinder();
            if (touchedWin.inMultiWindowMode() && (HwPCUtils.isInSinkWindowsCastMode() || HwPCUtils.isInBasicMode())) {
                this.mService.setTouchWinState(touchedWin);
            }
            if (touchedWin.mAppToken == null || touchedWin.mAppToken.getStack() == null) {
                pointX = x;
                pointY = y;
            } else {
                Rect rect = touchedWin.getBounds();
                float winScale = touchedWin.mAppToken.getStack().mHwStackScale;
                float pointX2 = ((float) rect.left) + ((x - ((float) rect.left)) / winScale);
                pointY = ((float) rect.top) + ((y - ((float) rect.top)) / winScale);
                pointX = pointX2;
            }
            DragEvent evt = obtainDragEvent(touchedWin, 3, pointX, pointY, null, null, this.mData, iDragAndDropPermissions, false);
            try {
                touchedWin.mClient.dispatchDragEvent(evt);
                this.mDragDropController.sendTimeoutMessage(0, token);
            } catch (RemoteException e) {
                myPid = myPid2;
                Slog.w("WindowManager", "can't send drop notification to win " + touchedWin);
                endDragLocked();
            } catch (Throwable th2) {
                th = th2;
                if (myPid != touchedWin.mSession.mPid) {
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInProgress() {
        return this.mDragInProgress;
    }

    /* access modifiers changed from: package-private */
    public void setDragShadowVisibleLocked(boolean visible) {
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl == null) {
            Slog.w("WindowManager", "setDragShadowVisible but mSurfaceControl is null, return!");
        } else if (visible) {
            this.mTransaction.show(surfaceControl).apply();
        } else {
            this.mTransaction.hide(surfaceControl).apply();
        }
    }

    private static DragEvent obtainDragEvent(WindowState win, int action, float x, float y, Object localState, ClipDescription description, ClipData data, IDragAndDropPermissions dragAndDropPermissions, boolean result) {
        return DragEvent.obtain(action, win.translateToWindowX(x), win.translateToWindowY(y), localState, description, data, dragAndDropPermissions, result);
    }

    private ValueAnimator createReturnAnimationLocked() {
        float f = this.mCurrentX;
        float f2 = this.mThumbOffsetX;
        float[] fArr = {f - f2, this.mOriginalX - f2};
        float f3 = this.mCurrentY;
        float f4 = this.mThumbOffsetY;
        float[] fArr2 = {f3 - f4, this.mOriginalY - f4};
        float f5 = this.mOriginalAlpha;
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, fArr), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, fArr2), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, 1.0f), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, f5, f5 / SURFACE_SIZE_DIVIDER));
        float translateX = this.mOriginalX - this.mCurrentX;
        float translateY = this.mOriginalY - this.mCurrentY;
        long duration = ((long) ((Math.sqrt((double) ((translateX * translateX) + (translateY * translateY))) / Math.sqrt((double) ((this.mDisplaySize.x * this.mDisplaySize.x) + (this.mDisplaySize.y * this.mDisplaySize.y)))) * 180.0d)) + MIN_ANIMATION_DURATION_MS;
        AnimationListener listener = new AnimationListener();
        animator.setDuration(duration);
        animator.setInterpolator(this.mCubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new Runnable(animator) {
            /* class com.android.server.wm.$$Lambda$DragState$4E4tzlfJ9AKYEiVk7F8SFlBLwPc */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.start();
            }
        });
        return animator;
    }

    private Animator createDragEndedAnimationLocked() {
        Slog.d("WindowManager", "createDragEndedAnimationLocked, mPendingDragEndedLocX = " + this.mPendingDragEndedLocX + ", mPendingDragEndedLocY = " + this.mPendingDragEndedLocY);
        int surfaceWidth = 0;
        int surfaceHeight = 0;
        if (this.mSurfaceControl != null) {
            Slog.d("WindowManager", "createDragEndedAnimationLocked surface = " + this.mSurfaceControl + ", width = " + this.mSurfaceControl.getWidth() + ", height = " + this.mSurfaceControl.getHeight());
            surfaceWidth = this.mSurfaceControl.getWidth();
            surfaceHeight = this.mSurfaceControl.getHeight();
        } else {
            Slog.w("WindowManager", "createDragEndedAnimationLocked surface is null!");
        }
        float fromTranslationX = this.mCurrentX - this.mThumbOffsetX;
        float toTranslationX = ((float) this.mPendingDragEndedLocX) - (((float) surfaceWidth) / SURFACE_SIZE_DIVIDER);
        float fromTranslationY = this.mCurrentY - this.mThumbOffsetY;
        float toTranslationY = ((float) this.mPendingDragEndedLocY) - (((float) surfaceHeight) / SURFACE_SIZE_DIVIDER);
        ValueAnimator translateAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, fromTranslationX, toTranslationX), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, fromTranslationY, toTranslationY), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, 1.0f), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, 1.0f, 1.0f));
        AnimationListener listener = new AnimationListener();
        translateAnimator.setDuration(TRANSLATION_ANIM_DURATION);
        translateAnimator.setInterpolator(this.mFastOutSlowInInterpolator);
        translateAnimator.addUpdateListener(listener);
        ValueAnimator disappearAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, toTranslationX, toTranslationX), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, toTranslationY, toTranslationY), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, 1.0f), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, 1.0f, 1.0f));
        disappearAnimator.setDuration(DISAPPEAR_ANIM_DURATION);
        disappearAnimator.setInterpolator(this.mFastOutSlowInInterpolator);
        disappearAnimator.addUpdateListener(listener);
        disappearAnimator.addListener(listener);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(translateAnimator, disappearAnimator);
        this.mService.mAnimationHandler.post(new Runnable(animatorSet) {
            /* class com.android.server.wm.$$Lambda$DragState$BrIZ9gihUTWarCexW9Ty5GAL1k */
            private final /* synthetic */ AnimatorSet f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.start();
            }
        });
        Slog.d("WindowManager", "createDragEndedAnimationLocked: animation start!");
        return animatorSet;
    }

    private ValueAnimator createDockIconReturnAnimationLocked() {
        int surfaceWidth = 0;
        int surfaceHeight = 0;
        SurfaceControl surfaceControl = this.mSurfaceControl;
        if (surfaceControl != null) {
            surfaceWidth = surfaceControl.getWidth();
            surfaceHeight = this.mSurfaceControl.getHeight();
        } else {
            Slog.w("WindowManager", "createDockIconReturnAnimationLocked surface is null!");
        }
        float[] fArr = {this.mCurrentX - this.mThumbOffsetX, ((float) this.mOriginalDragViewCenterX) - (((float) surfaceWidth) / SURFACE_SIZE_DIVIDER)};
        float[] fArr2 = {this.mCurrentY - this.mThumbOffsetY, ((float) this.mOriginalDragViewCenterY) - (((float) surfaceHeight) / SURFACE_SIZE_DIVIDER)};
        float f = this.mOriginalAlpha;
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, fArr), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, fArr2), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, 1.0f), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, f, f));
        AnimationListener listener = new AnimationListener();
        animator.setDuration(DOCK_ICON_RETURN_DURATION);
        animator.setInterpolator(this.mFastOutSlowInInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new Runnable(animator) {
            /* class com.android.server.wm.$$Lambda$DragState$rQP1d1b_iRAWF25NMqdNNxe7yBc */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.start();
            }
        });
        return animator;
    }

    private ValueAnimator createCancelAnimationLocked() {
        float f = this.mCurrentX;
        float[] fArr = {f - this.mThumbOffsetX, f};
        float f2 = this.mCurrentY;
        ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_X, fArr), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_Y, f2 - this.mThumbOffsetY, f2), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_SCALE, 1.0f, 0.0f), PropertyValuesHolder.ofFloat(ANIMATED_PROPERTY_ALPHA, this.mOriginalAlpha, 0.0f));
        AnimationListener listener = new AnimationListener();
        animator.setDuration(MIN_ANIMATION_DURATION_MS);
        animator.setInterpolator(this.mCubicEaseOutInterpolator);
        animator.addListener(listener);
        animator.addUpdateListener(listener);
        this.mService.mAnimationHandler.post(new Runnable(animator) {
            /* class com.android.server.wm.$$Lambda$DragState$nEp6P9sRn0QOqaCKgUgFzckYvw */
            private final /* synthetic */ ValueAnimator f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.start();
            }
        });
        return animator;
    }

    private boolean isFromSource(int source) {
        return (this.mTouchSource & source) == source;
    }

    /* access modifiers changed from: package-private */
    public void overridePointerIconLocked(int touchSource) {
        this.mTouchSource = touchSource;
        if (isFromSource(8194)) {
            DisplayContent displayContent = this.mDisplayContent;
            int displayid = displayContent == null ? -1 : displayContent.getDisplayId();
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(displayid)) {
                InputManager.getInstance().setPointerIconType(1021);
            } else {
                InputManager.getInstance().setPointerIconType(1000);
            }
        }
    }

    /* access modifiers changed from: private */
    public class AnimationListener implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {
        private AnimationListener() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
            r1.close();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0065, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0066, code lost:
            r0.addSuppressed(r3);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0069, code lost:
            throw r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0060, code lost:
            r2 = move-exception;
         */
        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            transaction.setPosition(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_X)).floatValue(), ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_Y)).floatValue());
            transaction.setAlpha(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_ALPHA)).floatValue());
            transaction.setMatrix(DragState.this.mSurfaceControl, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_SCALE)).floatValue(), 0.0f, 0.0f, ((Float) animation.getAnimatedValue(DragState.ANIMATED_PROPERTY_SCALE)).floatValue());
            transaction.apply();
            transaction.close();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            DragState dragState = DragState.this;
            dragState.mAnimationCompleted = true;
            dragState.mDragDropController.sendHandlerMessage(2, null);
        }
    }
}

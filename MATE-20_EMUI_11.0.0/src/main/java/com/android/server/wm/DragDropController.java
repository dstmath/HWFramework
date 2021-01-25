package com.android.server.wm;

import android.app.WindowConfiguration;
import android.content.ClipData;
import android.filterfw.geometry.Point;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.internal.util.Preconditions;
import com.android.server.input.InputManagerService;
import com.android.server.wm.DragState;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.view.HwWindowManager;
import java.util.concurrent.atomic.AtomicReference;

/* access modifiers changed from: package-private */
public class DragDropController {
    private static final long DELAY_CANCEL_HW_MULTI_WINDOW_DRAG = 800;
    private static final long DRAG_DELAYED_TIMEOUT_MS = 3000;
    private static final float DRAG_SHADOW_ALPHA_HW = 0.95f;
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    private static final long DRAG_TIMEOUT_MS = 5000;
    private static final String HW_FREE_FROM_PRE_DRAG_INPUT_CHANNEL = "hwFreeFormPreDragInputChannel";
    static final int MSG_ANIMATION_END = 2;
    private static final int MSG_CANCEL_DRAG = 5;
    static final int MSG_DRAG_END_TIMEOUT = 0;
    static final int MSG_REMOVE_HW_MULTI_WINDOW_DRAG_SURFACE = 3;
    static final int MSG_REMOVE_HW_MULTI_WINDOW_DROP_SURFACE = 4;
    static final int MSG_TEAR_DOWN_DRAG_AND_DROP_INPUT = 1;
    private WindowState draggingWin;
    private AtomicReference<WindowManagerInternal.IDragDropCallback> mCallback = new AtomicReference<>(new WindowManagerInternal.IDragDropCallback() {
        /* class com.android.server.wm.DragDropController.AnonymousClass1 */
    });
    private DragState mDragState;
    private final Handler mHandler;
    private boolean mIsCancelDragMsgExist = false;
    SurfaceControl mPendingRemoveSurface;
    private WindowManagerService mService;

    /* access modifiers changed from: package-private */
    public boolean dragDropActiveLocked() {
        DragState dragState = this.mDragState;
        return dragState != null && !dragState.isClosing();
    }

    /* access modifiers changed from: package-private */
    public void registerCallback(WindowManagerInternal.IDragDropCallback callback) {
        Preconditions.checkNotNull(callback);
        this.mCallback.set(callback);
    }

    DragDropController(WindowManagerService service, Looper looper) {
        this.mService = service;
        this.mHandler = new DragHandler(service, looper);
    }

    /* access modifiers changed from: package-private */
    public void sendDragStartedIfNeededLocked(WindowState window) {
        this.mDragState.sendDragStartedIfNeededLocked(window);
    }

    /* JADX INFO: Multiple debug info for r15v11 int: [D('alpha' float), D('dh' int)] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x01f4  */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x01f6  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01fd A[SYNTHETIC, Splitter:B:107:0x01fd] */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x022f  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x023b A[Catch:{ all -> 0x046d }] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x023d A[Catch:{ all -> 0x046d }] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x028a  */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x028d  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0297  */
    /* JADX WARNING: Removed duplicated region for block: B:155:0x02d1  */
    /* JADX WARNING: Removed duplicated region for block: B:207:0x0416 A[Catch:{ all -> 0x0462 }] */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x0437  */
    /* JADX WARNING: Removed duplicated region for block: B:213:0x043d A[SYNTHETIC, Splitter:B:213:0x043d] */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x04b6  */
    public IBinder performDrag(SurfaceSession session, int callerPid, int callerUid, IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        Throwable th;
        SurfaceControl surface2;
        Point touchPoint;
        Throwable th2;
        float f;
        float f2;
        InputChannel custInputChannel;
        Display display;
        WindowManagerInternal.IDragDropCallback iDragDropCallback;
        DragState dragState;
        InputManagerService inputManagerService;
        IBinder dragToken;
        float thumbCenterY2;
        float thumbCenterX2;
        IBinder dragToken2 = new Binder();
        boolean callbackResult = this.mCallback.get().prePerformDrag(window, dragToken2, touchSource, touchX, touchY, thumbCenterX, thumbCenterY, data);
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!callbackResult) {
                        try {
                            Slog.w("WindowManager", "IDragDropCallback rejects the performDrag request");
                            if (surface != null) {
                                try {
                                    surface.release();
                                } catch (Throwable th3) {
                                    th2 = th3;
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th2;
                                }
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postPerformDrag();
                            return null;
                        } catch (Throwable th4) {
                            touchPoint = th4;
                            surface2 = surface;
                        }
                    } else if (dragDropActiveLocked()) {
                        Slog.w("WindowManager", "Drag already in progress");
                        if (surface != null) {
                            surface.release();
                        }
                        if (this.mDragState != null && !this.mDragState.isInProgress()) {
                            this.mDragState.closeLocked();
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        this.mCallback.get().postPerformDrag();
                        return null;
                    } else {
                        WindowState callingWin = this.mService.windowForClientLocked((Session) null, window, false);
                        if (callingWin == null) {
                            Slog.w("WindowManager", "Bad requesting window " + window);
                            if (surface != null) {
                                surface.release();
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postPerformDrag();
                            return null;
                        }
                        if (HwPCUtils.isInSinkWindowsCastMode() || HwPCUtils.isInBasicMode()) {
                            this.mService.setDragWinState(callingWin);
                        }
                        Slog.i("WindowManager", "performDrag callingWin=" + callingWin);
                        DisplayContent displayContent = callingWin.getDisplayContent();
                        if (displayContent == null) {
                            Slog.w("WindowManager", "display content is null");
                            if (surface != null) {
                                surface.release();
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postPerformDrag();
                            return null;
                        }
                        if (isDragOnRemoteDisplay() && isTextOrImageData(data)) {
                            HwWindowManager.dragStartForMultiDisplay(data);
                        }
                        float alpha = (flags & 512) == 0 ? (4194304 & flags) == 0 ? DRAG_SHADOW_ALPHA_HW : DRAG_SHADOW_ALPHA_TRANSPARENT : 1.0f;
                        try {
                            try {
                                this.mDragState = new DragState(this.mService, this, new Binder(), surface, flags, window.asBinder());
                                surface2 = null;
                            } catch (Throwable th5) {
                                touchPoint = th5;
                                surface2 = surface;
                                if (surface2 != null) {
                                }
                                this.mDragState.closeLocked();
                                throw touchPoint;
                            }
                        } catch (Throwable th6) {
                            touchPoint = th6;
                            surface2 = surface;
                            if (surface2 != null) {
                            }
                            this.mDragState.closeLocked();
                            throw touchPoint;
                        }
                        try {
                            this.draggingWin = callingWin;
                            this.mDragState.mPid = callerPid;
                            this.mDragState.mUid = callerUid;
                            if (HwPCUtils.isInWindowsCastMode()) {
                                try {
                                    if (this.mService.getFocusedDisplayId() == HwPCUtils.getWindowsCastDisplayId() && isTextOrImageData(data)) {
                                        this.mDragState.mOriginalAlpha = 0.0f;
                                        this.mDragState.mToken = dragToken2;
                                        this.mDragState.mDisplayContent = displayContent;
                                        this.mIsCancelDragMsgExist = ((536870912 & flags) | (flags & 1024)) == 0;
                                        if (!this.mIsCancelDragMsgExist) {
                                            try {
                                                this.mHandler.removeMessages(5);
                                                f2 = touchX;
                                                f = touchY;
                                                try {
                                                    sendTimeoutMessageDelayed(5, new Point(f2, f), DELAY_CANCEL_HW_MULTI_WINDOW_DRAG);
                                                } catch (Throwable th7) {
                                                    touchPoint = th7;
                                                }
                                            } catch (Throwable th8) {
                                                touchPoint = th8;
                                                if (surface2 != null) {
                                                    surface2.release();
                                                }
                                                if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                    this.mDragState.closeLocked();
                                                }
                                                throw touchPoint;
                                            }
                                        } else {
                                            f2 = touchX;
                                            f = touchY;
                                        }
                                        this.mDragState.setIsDropSuccessAnimEnabled((flags & 1024) == 0);
                                        custInputChannel = null;
                                        if (!((1073741824 & flags) == 0 || data == null || data.getItemAt(0) == null || data.getItemAt(0).getIntent() == null)) {
                                            try {
                                                custInputChannel = (InputChannel) data.getItemAt(0).getIntent().getParcelableExtra(HW_FREE_FROM_PRE_DRAG_INPUT_CHANNEL);
                                            } catch (ClassCastException e) {
                                                Slog.w("WindowManager", "get a wrong input channel, maybe put a wrong clip data.");
                                            }
                                        }
                                        display = displayContent.getDisplay();
                                        iDragDropCallback = this.mCallback.get();
                                        dragState = this.mDragState;
                                        inputManagerService = this.mService.mInputManager;
                                        if (custInputChannel != null) {
                                            custInputChannel = callingWin.mInputChannel;
                                        }
                                        if (iDragDropCallback.registerInputChannel(dragState, display, inputManagerService, custInputChannel)) {
                                            Slog.e("WindowManager", "Unable to transfer touch focus");
                                            if (0 != 0) {
                                                try {
                                                    surface2.release();
                                                } catch (Throwable th9) {
                                                    th2 = th9;
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw th2;
                                                }
                                            }
                                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                this.mDragState.closeLocked();
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            this.mCallback.get().postPerformDrag();
                                            return null;
                                        }
                                        Rect rect = callingWin.getBounds();
                                        if (rect == null) {
                                            Slog.w("WindowManager", "Bad rect");
                                            if (0 != 0) {
                                                surface2.release();
                                            }
                                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                this.mDragState.closeLocked();
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            this.mCallback.get().postPerformDrag();
                                            return null;
                                        }
                                        float scale = 1.0f;
                                        if (!(callingWin.mAppToken == null || callingWin.mAppToken.getStack() == null)) {
                                            scale = callingWin.mAppToken.getStack().mHwStackScale;
                                            if (WindowConfiguration.isHwSplitScreenWindowingMode(callingWin.mAppToken.getStack().getWindowingMode())) {
                                                rect.left = 0;
                                                rect.top = 0;
                                            }
                                        }
                                        this.mDragState.mSurfaceScale = scale;
                                        float combinedScale = callingWin.mGlobalScale * scale;
                                        float actTouchX = ((f2 - ((float) rect.left)) * combinedScale) + ((float) rect.left);
                                        float actTouchY = ((f - ((float) rect.top)) * combinedScale) + ((float) rect.top);
                                        SurfaceControl surfaceControl = this.mDragState.mSurfaceControl;
                                        if (display.getDisplayId() == 0) {
                                            try {
                                                if (this.mService.getLazyMode() != 0) {
                                                    DisplayInfo displayInfo = new DisplayInfo();
                                                    display.getDisplayInfo(displayInfo);
                                                    int dw = displayInfo.logicalWidth;
                                                    int dh = displayInfo.logicalHeight;
                                                    dragToken = dragToken2;
                                                    if (1 == this.mService.getLazyMode()) {
                                                        actTouchX *= 0.75f;
                                                    } else {
                                                        try {
                                                            if (2 == this.mService.getLazyMode()) {
                                                                actTouchX = (actTouchX * 0.75f) + (((float) dw) * 0.25f);
                                                            }
                                                        } catch (Throwable th10) {
                                                            touchPoint = th10;
                                                            if (surface2 != null) {
                                                            }
                                                            this.mDragState.closeLocked();
                                                            throw touchPoint;
                                                        }
                                                    }
                                                    actTouchY = (actTouchY * 0.75f) + (((float) dh) * 0.25f);
                                                    thumbCenterX2 = thumbCenterX * 0.75f;
                                                    thumbCenterY2 = thumbCenterY * 0.75f;
                                                    this.mDragState.mData = data;
                                                    this.mDragState.broadcastDragStartedLocked(f2, f);
                                                    this.mDragState.setActTouchPoints(actTouchX, actTouchY);
                                                    this.mDragState.overridePointerIconLocked(touchSource);
                                                    this.mDragState.mThumbOffsetX = thumbCenterX2;
                                                    this.mDragState.mThumbOffsetY = thumbCenterY2;
                                                    SurfaceControl.Transaction transaction = callingWin.getPendingTransaction();
                                                    transaction.setAlpha(surfaceControl, this.mDragState.mOriginalAlpha);
                                                    try {
                                                        transaction.setPosition(surfaceControl, actTouchX - thumbCenterX2, actTouchY - thumbCenterY2);
                                                        transaction.show(surfaceControl);
                                                        displayContent.reparentToOverlay(transaction, surfaceControl);
                                                        callingWin.scheduleAnimation();
                                                        this.mDragState.notifyLocationLocked(f2, f);
                                                        if (!HwPCUtils.isPcCastModeInServer()) {
                                                            if (HwPCUtils.getPCDisplayID() == display.getDisplayId()) {
                                                                this.mHandler.removeMessages(0);
                                                                this.mHandler.sendEmptyMessageDelayed(0, DRAG_DELAYED_TIMEOUT_MS);
                                                            }
                                                        }
                                                        if (0 != 0) {
                                                            try {
                                                                surface2.release();
                                                            } catch (Throwable th11) {
                                                                th2 = th11;
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th2;
                                                            }
                                                        }
                                                        if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                            this.mDragState.closeLocked();
                                                        }
                                                    } catch (Throwable th12) {
                                                        touchPoint = th12;
                                                        if (surface2 != null) {
                                                        }
                                                        this.mDragState.closeLocked();
                                                        throw touchPoint;
                                                    }
                                                    try {
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                        this.mCallback.get().postPerformDrag();
                                                        return dragToken;
                                                    } catch (Throwable th13) {
                                                        th = th13;
                                                        this.mCallback.get().postPerformDrag();
                                                        throw th;
                                                    }
                                                } else {
                                                    dragToken = dragToken2;
                                                }
                                            } catch (Throwable th14) {
                                                touchPoint = th14;
                                                if (surface2 != null) {
                                                }
                                                this.mDragState.closeLocked();
                                                throw touchPoint;
                                            }
                                        } else {
                                            dragToken = dragToken2;
                                        }
                                        thumbCenterX2 = thumbCenterX;
                                        thumbCenterY2 = thumbCenterY;
                                        try {
                                            this.mDragState.mData = data;
                                            this.mDragState.broadcastDragStartedLocked(f2, f);
                                            this.mDragState.setActTouchPoints(actTouchX, actTouchY);
                                        } catch (Throwable th15) {
                                            touchPoint = th15;
                                            if (surface2 != null) {
                                            }
                                            this.mDragState.closeLocked();
                                            throw touchPoint;
                                        }
                                        try {
                                            this.mDragState.overridePointerIconLocked(touchSource);
                                            this.mDragState.mThumbOffsetX = thumbCenterX2;
                                            this.mDragState.mThumbOffsetY = thumbCenterY2;
                                            SurfaceControl.Transaction transaction2 = callingWin.getPendingTransaction();
                                            transaction2.setAlpha(surfaceControl, this.mDragState.mOriginalAlpha);
                                            transaction2.setPosition(surfaceControl, actTouchX - thumbCenterX2, actTouchY - thumbCenterY2);
                                            transaction2.show(surfaceControl);
                                            displayContent.reparentToOverlay(transaction2, surfaceControl);
                                            callingWin.scheduleAnimation();
                                            this.mDragState.notifyLocationLocked(f2, f);
                                            if (!HwPCUtils.isPcCastModeInServer()) {
                                            }
                                            if (0 != 0) {
                                            }
                                            this.mDragState.closeLocked();
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            this.mCallback.get().postPerformDrag();
                                            return dragToken;
                                        } catch (Throwable th16) {
                                            touchPoint = th16;
                                            if (surface2 != null) {
                                            }
                                            this.mDragState.closeLocked();
                                            throw touchPoint;
                                        }
                                    }
                                } catch (Throwable th17) {
                                    touchPoint = th17;
                                    if (surface2 != null) {
                                    }
                                    this.mDragState.closeLocked();
                                    throw touchPoint;
                                }
                            }
                            this.mDragState.mOriginalAlpha = alpha;
                            this.mDragState.mToken = dragToken2;
                            this.mDragState.mDisplayContent = displayContent;
                            this.mIsCancelDragMsgExist = ((536870912 & flags) | (flags & 1024)) == 0;
                            if (!this.mIsCancelDragMsgExist) {
                            }
                            try {
                                this.mDragState.setIsDropSuccessAnimEnabled((flags & 1024) == 0);
                                custInputChannel = null;
                                custInputChannel = (InputChannel) data.getItemAt(0).getIntent().getParcelableExtra(HW_FREE_FROM_PRE_DRAG_INPUT_CHANNEL);
                                display = displayContent.getDisplay();
                                iDragDropCallback = this.mCallback.get();
                                dragState = this.mDragState;
                                inputManagerService = this.mService.mInputManager;
                                if (custInputChannel != null) {
                                }
                                if (iDragDropCallback.registerInputChannel(dragState, display, inputManagerService, custInputChannel)) {
                                }
                            } catch (Throwable th18) {
                                touchPoint = th18;
                                if (surface2 != null) {
                                }
                                this.mDragState.closeLocked();
                                throw touchPoint;
                            }
                        } catch (Throwable th19) {
                            touchPoint = th19;
                            if (surface2 != null) {
                            }
                            this.mDragState.closeLocked();
                            throw touchPoint;
                        }
                    }
                } catch (Throwable th20) {
                    touchPoint = th20;
                    surface2 = surface;
                    if (surface2 != null) {
                    }
                    this.mDragState.closeLocked();
                    throw touchPoint;
                }
            }
        } catch (Throwable th21) {
            th = th21;
            this.mCallback.get().postPerformDrag();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void reportDropResult(IWindow window, boolean consumed) {
        IBinder token = window.asBinder();
        this.mCallback.get().preReportDropResult(window, consumed);
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mDragState == null) {
                        Slog.w("WindowManager", "Drop result given but no drag in progress");
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } else if (this.mDragState.mToken == token || HwPCUtils.isInSinkWindowsCastMode() || HwPCUtils.isInWindowsCastMode()) {
                        this.mHandler.removeMessages(0, window.asBinder());
                        if (this.mService.windowForClientLocked((Session) null, window, false) == null) {
                            Slog.w("WindowManager", "Bad result-reporting window " + window);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            this.mCallback.get().postReportDropResult();
                            return;
                        }
                        this.mDragState.mDragResult = consumed;
                        Slog.i("WindowManager", "report drop result : " + consumed);
                        this.mDragState.endDragLocked();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        this.mCallback.get().postReportDropResult();
                    } else {
                        Slog.w("WindowManager", "Invalid drop-result claim by " + window);
                        throw new IllegalStateException("reportDropResult() by non-recipient");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        } finally {
            this.mCallback.get().postReportDropResult();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void cancelDragAndDrop(IBinder dragToken, boolean skipAnimation) {
        this.mCallback.get().preCancelDragAndDrop(dragToken);
        try {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mDragState == null) {
                        Slog.w("WindowManager", "cancelDragAndDrop() without prepareDrag()");
                        throw new IllegalStateException("cancelDragAndDrop() without prepareDrag()");
                    } else if (this.mDragState.mToken == dragToken) {
                        this.mDragState.mDragResult = false;
                        this.mDragState.cancelDragLocked(skipAnimation);
                    } else {
                        Slog.w("WindowManager", "cancelDragAndDrop() does not match prepareDrag()");
                        throw new IllegalStateException("cancelDragAndDrop() does not match prepareDrag()");
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
        } finally {
            this.mCallback.get().postCancelDragAndDrop();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleMotionEvent(boolean keepHandling, float newX, float newY) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (dragDropActiveLocked()) {
                    if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.getPCDisplayID() == this.mDragState.mDisplayContent.getDisplayId()) {
                        this.mHandler.removeMessages(0);
                    }
                    if (this.mIsCancelDragMsgExist) {
                        this.mHandler.removeMessages(5);
                        this.mIsCancelDragMsgExist = false;
                    }
                    float scale = 1.0f;
                    if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode()) {
                        scale = this.mService.mSubFoldModeScale;
                    }
                    if (keepHandling) {
                        this.mDragState.notifyMoveLocked(newX / scale, newY / scale);
                    } else {
                        this.mDragState.notifyDropLocked(newX / scale, newY / scale);
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dragRecipientEntered(IWindow window) {
    }

    /* access modifiers changed from: package-private */
    public void dragRecipientExited(IWindow window) {
    }

    /* access modifiers changed from: package-private */
    public void setPendingDragEndedLoc(IWindow window, int x, int y) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mDragState == null) {
                    Slog.i("WindowManager", "mDragState is null, not to setPendingDragEndedLoc");
                    return;
                }
                this.mDragState.setPendingDragEndedLoc(x, y);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setOriginalDragViewCenter(IWindow window, int x, int y) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mDragState == null) {
                    Slog.i("WindowManager", "mDragState is null, not to setOriginalDragViewCenter");
                    return;
                }
                this.mDragState.setOriginalDragViewCenter(x, y);
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dragRecipientFeedback(IWindow window, boolean isEntered, int mimeTypeSupportState, int acceptableItemCnt) {
        try {
            if (this.draggingWin != null && this.draggingWin.mClient != null) {
                this.draggingWin.mClient.notifyDragEnterExitState(isEntered, mimeTypeSupportState, acceptableItemCnt);
            }
        } catch (RemoteException e) {
            Slog.w("WindowManager", "Unable to Feedback window " + this.draggingWin);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendHandlerMessage(int what, Object arg) {
        this.mHandler.obtainMessage(what, arg).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void sendTimeoutMessage(int what, Object arg) {
        this.mHandler.removeMessages(what, arg);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg), DRAG_TIMEOUT_MS);
    }

    /* access modifiers changed from: package-private */
    public void sendTimeoutMessageDelayed(int what, Object arg, long delayMillis) {
        this.mHandler.removeMessages(what, arg);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg), delayMillis);
    }

    /* access modifiers changed from: package-private */
    public void removeHwMultiWindowDragSurfaceNow() {
        if (this.mPendingRemoveSurface != null) {
            this.mHandler.removeMessages(3);
            sendHandlerMessage(3, this.mPendingRemoveSurface);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDragStateClosedLocked(DragState dragState) {
        if (this.mDragState != dragState) {
            Slog.wtf("WindowManager", "Unknown drag state is closed");
        } else {
            this.mDragState = null;
        }
    }

    private class DragHandler extends Handler {
        private final WindowManagerService mService;

        DragHandler(WindowManagerService service, Looper looper) {
            super(looper);
            this.mService = service;
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                Slog.w("WindowManager", "Timeout ending drag to win " + ((IBinder) msg.obj));
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (DragDropController.this.mDragState != null) {
                            DragDropController.this.mDragState.mDragResult = false;
                            DragDropController.this.mDragState.endDragLocked();
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 1) {
                DragState.InputInterceptor interceptor = (DragState.InputInterceptor) msg.obj;
                if (interceptor != null) {
                    synchronized (this.mService.mGlobalLock) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            interceptor.tearDown();
                        } finally {
                            WindowManagerService.resetPriorityAfterLockedSection();
                        }
                    }
                }
            } else if (i == 2) {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        if (DragDropController.this.mDragState == null) {
                            Slog.wtf("WindowManager", "mDragState unexpectedly became null while plyaing animation");
                            return;
                        }
                        DragDropController.this.mDragState.closeLocked();
                        WindowManagerService.resetPriorityAfterLockedSection();
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 3) {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        Object object = msg.obj;
                        if (object instanceof SurfaceControl) {
                            SurfaceControl surfaceControl = (SurfaceControl) object;
                            if (surfaceControl.isValid()) {
                                SurfaceControl.Transaction transaction = this.mService.mTransactionFactory.make();
                                transaction.remove(surfaceControl).apply();
                                transaction.close();
                                DragDropController.this.mPendingRemoveSurface = null;
                                Slog.i("WindowManager", "removeDragSurface executed");
                            }
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 4) {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        Object object2 = msg.obj;
                        if (object2 instanceof SurfaceControl) {
                            SurfaceControl.Transaction transaction2 = this.mService.mTransactionFactory.make();
                            Slog.d("WindowManager", "try to clear surface in handler.");
                            transaction2.remove((SurfaceControl) object2).apply();
                            transaction2.close();
                        }
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            } else if (i == 5) {
                Object object3 = msg.obj;
                if (object3 instanceof Point) {
                    Point touchPoint = (Point) object3;
                    DragDropController.this.handleMotionEvent(false, touchPoint.x, touchPoint.y);
                }
            }
        }
    }

    private boolean isDragOnRemoteDisplay() {
        if ((!HwPCUtils.isInWindowsCastMode() || this.mService.getFocusedDisplayId() != HwPCUtils.getWindowsCastDisplayId()) && !HwPCUtils.isInSinkWindowsCastMode()) {
            return false;
        }
        Slog.d("WindowManager", "do not need draw shadow on phone when drag on remote dispaly.");
        return true;
    }

    private boolean isTextOrImageData(ClipData data) {
        CharSequence text;
        if (data == null) {
            return false;
        }
        ClipData.Item item = data.getItemAt(0);
        if (!(item == null || (text = item.getText()) == null || text.length() == 0)) {
            return true;
        }
        int itemCount = data.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            ClipData.Item item2 = data.getItemAt(i);
            if (!(item2 == null || item2.getUri() == null)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setDragShadowVisibleLocked(boolean visible) {
        DragState dragState = this.mDragState;
        if (dragState == null) {
            Slog.w("WindowManager", "setDragShadowVisibleLocked with null drag state, return!");
        } else {
            dragState.setDragShadowVisibleLocked(visible);
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelDragAndDrop(boolean skipAnimation) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mDragState == null) {
                    Slog.w("WindowManager", "cancelDragAndDrop without prepareDrag, return!");
                } else if (this.mDragState.mToken == null) {
                    Slog.w("WindowManager", "No active drag to cancel, return!");
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else {
                    IBinder dragToken = this.mDragState.mToken;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    cancelDragAndDrop(dragToken, skipAnimation);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }
}

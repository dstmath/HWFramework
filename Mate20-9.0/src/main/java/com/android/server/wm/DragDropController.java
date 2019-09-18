package com.android.server.wm;

import android.content.ClipData;
import android.graphics.Point;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.IWindow;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import com.android.internal.util.Preconditions;
import com.android.server.input.InputWindowHandle;
import com.android.server.wm.DragState;
import com.android.server.wm.WindowManagerInternal;
import java.util.concurrent.atomic.AtomicReference;

class DragDropController {
    private static final float DRAG_SHADOW_ALPHA_TRANSPARENT = 0.7071f;
    private static final long DRAG_TIMEOUT_MS = 5000;
    static final int MSG_ANIMATION_END = 2;
    static final int MSG_DRAG_END_TIMEOUT = 0;
    static final int MSG_TEAR_DOWN_DRAG_AND_DROP_INPUT = 1;
    private AtomicReference<WindowManagerInternal.IDragDropCallback> mCallback = new AtomicReference<>(new WindowManagerInternal.IDragDropCallback() {
    });
    /* access modifiers changed from: private */
    public DragState mDragState;
    private final Handler mHandler;
    private WindowManagerService mService;

    private class DragHandler extends Handler {
        private final WindowManagerService mService;

        DragHandler(WindowManagerService service, Looper looper) {
            super(looper);
            this.mService = service;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002e, code lost:
            com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
         */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Object obj = msg.obj;
                    synchronized (this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (DragDropController.this.mDragState != null) {
                                DragDropController.this.mDragState.mDragResult = false;
                                DragDropController.this.mDragState.endDragLocked();
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
                    break;
                case 1:
                    DragState.InputInterceptor interceptor = (DragState.InputInterceptor) msg.obj;
                    if (interceptor != null) {
                        synchronized (this.mService.mWindowMap) {
                            try {
                                WindowManagerService.boostPriorityForLockedSection();
                                interceptor.tearDown();
                            } catch (Throwable th2) {
                                while (true) {
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th2;
                                    break;
                                }
                            }
                        }
                        WindowManagerService.resetPriorityAfterLockedSection();
                        break;
                    } else {
                        return;
                    }
                case 2:
                    synchronized (this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (DragDropController.this.mDragState != null) {
                                DragDropController.this.mDragState.closeLocked();
                                break;
                            } else {
                                Slog.wtf("WindowManager", "mDragState unexpectedly became null while plyaing animation");
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return;
                            }
                        } catch (Throwable th3) {
                            while (true) {
                                WindowManagerService.resetPriorityAfterLockedSection();
                                throw th3;
                                break;
                            }
                        }
                    }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dragDropActiveLocked() {
        return this.mDragState != null;
    }

    /* access modifiers changed from: package-private */
    public InputWindowHandle getInputWindowHandleLocked() {
        return this.mDragState.getInputWindowHandle();
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

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x01ae, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r8.mCallback.get().postPerformDrag();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x01bd, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:?, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x02e9, code lost:
        r8.mCallback.get().postPerformDrag();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02f5, code lost:
        return r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02f6, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x02f7, code lost:
        r18 = r1;
        r14 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x0354, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:199:0x0355, code lost:
        r4 = r1;
        r14 = r3;
        r6 = r5;
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005e, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r8.mCallback.get().postPerformDrag();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x006c, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x009e, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r8.mCallback.get().postPerformDrag();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ac, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00e5, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r8.mCallback.get().postPerformDrag();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f3, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x011a, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        r8.mCallback.get().postPerformDrag();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0128, code lost:
        return null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:183:0x0331 A[SYNTHETIC, Splitter:B:183:0x0331] */
    public IBinder performDrag(SurfaceSession session, int callerPid, int callerUid, IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        float touchY2;
        float touchX2;
        float thumbCenterY2;
        float thumbCenterX2;
        SurfaceControl surface2;
        float touchY3;
        float touchX3;
        float touchX4;
        IWindow iWindow = window;
        float touchX5 = touchX;
        float thumbCenterX3 = touchY;
        float thumbCenterX4 = thumbCenterX;
        float thumbCenterY3 = thumbCenterY;
        IBinder binder = new Binder();
        boolean callbackResult = this.mCallback.get().prePerformDrag(iWindow, binder, touchSource, touchX5, thumbCenterX3, thumbCenterX4, thumbCenterY3, data);
        try {
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (!callbackResult) {
                        try {
                            Slog.w("WindowManager", "IDragDropCallback rejects the performDrag request");
                            if (surface != null) {
                                try {
                                    surface.release();
                                } catch (Throwable th) {
                                    th = th;
                                    SurfaceControl surfaceControl = surface;
                                    boolean z = callbackResult;
                                    IBinder iBinder = binder;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    WindowManagerService.resetPriorityAfterLockedSection();
                                    throw th;
                                }
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            touchY2 = thumbCenterX3;
                            touchX2 = touchX5;
                            boolean z2 = callbackResult;
                            thumbCenterX2 = thumbCenterX4;
                            IBinder iBinder2 = binder;
                            thumbCenterY2 = thumbCenterY3;
                        }
                    } else if (dragDropActiveLocked()) {
                        Slog.w("WindowManager", "Drag already in progress");
                        if (surface != null) {
                            surface.release();
                        }
                        if (this.mDragState != null && !this.mDragState.isInProgress()) {
                            this.mDragState.closeLocked();
                        }
                    } else {
                        WindowState callingWin = this.mService.windowForClientLocked((Session) null, iWindow, false);
                        if (callingWin == null) {
                            Slog.w("WindowManager", "Bad requesting window " + iWindow);
                            if (surface != null) {
                                surface.release();
                            }
                            if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                this.mDragState.closeLocked();
                            }
                        } else {
                            DisplayContent displayContent = callingWin.getDisplayContent();
                            if (displayContent == null) {
                                Slog.w("WindowManager", "display content is null");
                                if (surface != null) {
                                    surface.release();
                                }
                                if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                    this.mDragState.closeLocked();
                                }
                            } else {
                                int i = flags;
                                float alpha = (i & 512) == 0 ? DRAG_SHADOW_ALPHA_TRANSPARENT : 1.0f;
                                IBinder winBinder = window.asBinder();
                                boolean z3 = callbackResult;
                                IBinder dragToken = binder;
                                Binder binder2 = new Binder();
                                try {
                                    r1 = r1;
                                    try {
                                        DragState dragState = new DragState(this.mService, this, binder2, surface, i, winBinder);
                                        this.mDragState = dragState;
                                        surface2 = null;
                                        try {
                                            this.mDragState.mPid = callerPid;
                                            this.mDragState.mUid = callerUid;
                                            this.mDragState.mOriginalAlpha = alpha;
                                            this.mDragState.mToken = dragToken;
                                            Display display = displayContent.getDisplay();
                                            if (!this.mCallback.get().registerInputChannel(this.mDragState, display, this.mService.mInputManager, callingWin.mInputChannel)) {
                                                Slog.e("WindowManager", "Unable to transfer touch focus");
                                                if (surface2 != null) {
                                                    try {
                                                        surface2.release();
                                                    } catch (Throwable th4) {
                                                        th = th4;
                                                        SurfaceControl surfaceControl2 = surface2;
                                                        thumbCenterY3 = thumbCenterY;
                                                        thumbCenterX4 = thumbCenterX;
                                                        thumbCenterX3 = touchY;
                                                        touchX5 = touchX;
                                                        while (true) {
                                                            break;
                                                        }
                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                        throw th;
                                                    }
                                                }
                                                if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                    this.mDragState.closeLocked();
                                                }
                                            } else {
                                                this.mDragState.mDisplayContent = displayContent;
                                                this.mDragState.mData = data;
                                                touchX2 = touchX;
                                                touchY2 = touchY;
                                                try {
                                                    this.mDragState.broadcastDragStartedLocked(touchX2, touchY2);
                                                    this.mDragState.overridePointerIconLocked(touchSource);
                                                    thumbCenterX2 = thumbCenterX;
                                                    try {
                                                        this.mDragState.mThumbOffsetX = thumbCenterX2;
                                                        Binder binder3 = binder2;
                                                        thumbCenterY2 = thumbCenterY;
                                                    } catch (Throwable th5) {
                                                        th = th5;
                                                        thumbCenterY2 = thumbCenterY;
                                                        if (surface2 != null) {
                                                            try {
                                                                surface2.release();
                                                            } catch (Throwable th6) {
                                                                th = th6;
                                                                SurfaceControl surfaceControl3 = surface2;
                                                                thumbCenterX4 = thumbCenterX2;
                                                                thumbCenterY3 = thumbCenterY2;
                                                                touchX5 = touchX2;
                                                                thumbCenterX3 = touchY2;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                        }
                                                        if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                            this.mDragState.closeLocked();
                                                        }
                                                        throw th;
                                                    }
                                                } catch (Throwable th7) {
                                                    th = th7;
                                                    thumbCenterX2 = thumbCenterX;
                                                    thumbCenterY2 = thumbCenterY;
                                                    if (surface2 != null) {
                                                    }
                                                    this.mDragState.closeLocked();
                                                    throw th;
                                                }
                                                try {
                                                    this.mDragState.mThumbOffsetY = thumbCenterY2;
                                                    SurfaceControl surfaceControl4 = this.mDragState.mSurfaceControl;
                                                    if (display.getDisplayId() == 0) {
                                                        int lazyMode = this.mService.getLazyMode();
                                                        if (lazyMode != 0) {
                                                            int dw = display.getWidth();
                                                            int dh = display.getHeight();
                                                            int i2 = lazyMode;
                                                            IBinder iBinder3 = winBinder;
                                                            if (1 == this.mService.getLazyMode()) {
                                                                touchX4 = touchX2 * 0.75f;
                                                                int i3 = dw;
                                                            } else if (2 == this.mService.getLazyMode()) {
                                                                touchX4 = (touchX2 * 0.75f) + (((float) dw) * 0.25f);
                                                            } else {
                                                                touchX4 = touchX2;
                                                            }
                                                            touchX3 = touchX4;
                                                            touchY3 = (((float) dh) * 0.25f) + (touchY2 * 0.75f);
                                                            thumbCenterX2 *= 0.75f;
                                                            thumbCenterY2 *= 0.75f;
                                                        } else {
                                                            touchX3 = touchX2;
                                                            touchY3 = touchY2;
                                                        }
                                                        Display display2 = display;
                                                        touchX2 = touchX3;
                                                    } else {
                                                        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(display.getDisplayId())) {
                                                            int pcMode = this.mService.getPCScreenDisplayMode();
                                                            if (pcMode != 0) {
                                                                Point point = new Point();
                                                                display.getRealSize(point);
                                                                int dw2 = point.x;
                                                                Display display3 = display;
                                                                int dh2 = point.y;
                                                                Point point2 = point;
                                                                float pcDisplayScale = pcMode == 1 ? 0.95f : 0.9f;
                                                                int i4 = pcMode;
                                                                touchY3 = ((((float) dh2) * (1.0f - pcDisplayScale)) / 2.0f) + (touchY2 * pcDisplayScale);
                                                                thumbCenterX2 *= pcDisplayScale;
                                                                thumbCenterY2 *= pcDisplayScale;
                                                                touchX2 = (touchX2 * pcDisplayScale) + ((((float) dw2) * (1.0f - pcDisplayScale)) / 2.0f);
                                                            }
                                                        }
                                                        touchY3 = touchY2;
                                                    }
                                                    try {
                                                        SurfaceControl.Transaction transaction = callingWin.getPendingTransaction();
                                                        transaction.setAlpha(surfaceControl4, this.mDragState.mOriginalAlpha);
                                                        transaction.setPosition(surfaceControl4, touchX2 - thumbCenterX2, touchY3 - thumbCenterY2);
                                                        transaction.show(surfaceControl4);
                                                        displayContent.reparentToOverlay(transaction, surfaceControl4);
                                                        callingWin.scheduleAnimation();
                                                        this.mDragState.notifyLocationLocked(touchX2, touchY3);
                                                        if (surface2 != null) {
                                                            try {
                                                                surface2.release();
                                                            } catch (Throwable th8) {
                                                                th = th8;
                                                                SurfaceControl surfaceControl5 = surface2;
                                                                thumbCenterX4 = thumbCenterX2;
                                                                thumbCenterY3 = thumbCenterY2;
                                                                thumbCenterX3 = touchY3;
                                                                touchX5 = touchX2;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                        }
                                                        if (this.mDragState != null && !this.mDragState.isInProgress()) {
                                                            this.mDragState.closeLocked();
                                                        }
                                                    } catch (Throwable th9) {
                                                        th = th9;
                                                        touchY2 = touchY3;
                                                        if (surface2 != null) {
                                                        }
                                                        this.mDragState.closeLocked();
                                                        throw th;
                                                    }
                                                } catch (Throwable th10) {
                                                    th = th10;
                                                    if (surface2 != null) {
                                                    }
                                                    this.mDragState.closeLocked();
                                                    throw th;
                                                }
                                            }
                                        } catch (Throwable th11) {
                                            th = th11;
                                            thumbCenterX2 = thumbCenterX;
                                            thumbCenterY2 = thumbCenterY;
                                            touchX2 = touchX;
                                            touchY2 = touchY;
                                            if (surface2 != null) {
                                            }
                                            this.mDragState.closeLocked();
                                            throw th;
                                        }
                                    } catch (Throwable th12) {
                                        th = th12;
                                        thumbCenterX2 = thumbCenterX;
                                        thumbCenterY2 = thumbCenterY;
                                        touchX2 = touchX;
                                        touchY2 = touchY;
                                        surface2 = surface;
                                        if (surface2 != null) {
                                        }
                                        this.mDragState.closeLocked();
                                        throw th;
                                    }
                                } catch (Throwable th13) {
                                    th = th13;
                                    thumbCenterY2 = thumbCenterY3;
                                    touchY2 = thumbCenterX3;
                                    touchX2 = touchX5;
                                    thumbCenterX2 = thumbCenterX4;
                                    surface2 = surface;
                                    if (surface2 != null) {
                                    }
                                    this.mDragState.closeLocked();
                                    throw th;
                                }
                            }
                        }
                    }
                } catch (Throwable th14) {
                    th = th14;
                    touchY2 = thumbCenterX3;
                    touchX2 = touchX5;
                    boolean z4 = callbackResult;
                    thumbCenterX2 = thumbCenterX4;
                    IBinder iBinder4 = binder;
                    thumbCenterY2 = thumbCenterY3;
                    surface2 = surface;
                    if (surface2 != null) {
                    }
                    this.mDragState.closeLocked();
                    throw th;
                }
            }
        } catch (Throwable th15) {
            th = th15;
            float f = thumbCenterX3;
            float f2 = touchX5;
            boolean z5 = callbackResult;
            float f3 = thumbCenterX4;
            IBinder iBinder5 = binder;
            float f4 = thumbCenterY3;
            SurfaceControl surfaceControl6 = surface;
            this.mCallback.get().postPerformDrag();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void reportDropResult(IWindow window, boolean consumed) {
        IBinder token = window.asBinder();
        this.mCallback.get().preReportDropResult(window, consumed);
        try {
            synchronized (this.mService.mWindowMap) {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mDragState == null) {
                    Slog.w("WindowManager", "Drop result given but no drag in progress");
                    WindowManagerService.resetPriorityAfterLockedSection();
                    this.mCallback.get().postReportDropResult();
                } else if (this.mDragState.mToken == token) {
                    this.mHandler.removeMessages(0, window.asBinder());
                    if (this.mService.windowForClientLocked((Session) null, window, false) == null) {
                        Slog.w("WindowManager", "Bad result-reporting window " + window);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        this.mCallback.get().postReportDropResult();
                        return;
                    }
                    this.mDragState.mDragResult = consumed;
                    this.mDragState.endDragLocked();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    this.mCallback.get().postReportDropResult();
                } else {
                    Slog.w("WindowManager", "Invalid drop-result claim by " + window);
                    throw new IllegalStateException("reportDropResult() by non-recipient");
                }
            }
        } catch (Throwable th) {
            this.mCallback.get().postReportDropResult();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelDragAndDrop(IBinder dragToken) {
        this.mCallback.get().preCancelDragAndDrop(dragToken);
        try {
            synchronized (this.mService.mWindowMap) {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mDragState == null) {
                    Slog.w("WindowManager", "cancelDragAndDrop() without prepareDrag()");
                    throw new IllegalStateException("cancelDragAndDrop() without prepareDrag()");
                } else if (this.mDragState.mToken == dragToken) {
                    this.mDragState.mDragResult = false;
                    this.mDragState.cancelDragLocked();
                } else {
                    Slog.w("WindowManager", "cancelDragAndDrop() does not match prepareDrag()");
                    throw new IllegalStateException("cancelDragAndDrop() does not match prepareDrag()");
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            this.mCallback.get().postCancelDragAndDrop();
        } catch (Throwable th) {
            this.mCallback.get().postCancelDragAndDrop();
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0021, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
        return;
     */
    public void handleMotionEvent(boolean keepHandling, float newX, float newY) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!dragDropActiveLocked()) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (keepHandling) {
                    this.mDragState.notifyMoveLocked(newX, newY);
                } else {
                    this.mDragState.notifyDropLocked(newX, newY);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
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
    public void sendHandlerMessage(int what, Object arg) {
        this.mHandler.obtainMessage(what, arg).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void sendTimeoutMessage(int what, Object arg) {
        this.mHandler.removeMessages(what, arg);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(what, arg), DRAG_TIMEOUT_MS);
    }

    /* access modifiers changed from: package-private */
    public void onDragStateClosedLocked(DragState dragState) {
        if (this.mDragState != dragState) {
            Slog.wtf("WindowManager", "Unknown drag state is closed");
        } else {
            this.mDragState = null;
        }
    }
}

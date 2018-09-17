package com.android.server.wm;

import android.content.ClipData;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.util.HwPCUtils;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.view.Display;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession.Stub;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager.LayoutParams;
import com.android.internal.view.IInputContext;
import com.android.internal.view.IInputMethodClient;
import com.android.internal.view.IInputMethodManager;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class Session extends Stub implements DeathRecipient {
    private AlertWindowNotification mAlertWindowNotification;
    private final Set<WindowSurfaceController> mAlertWindowSurfaces = new HashSet();
    private final Set<WindowSurfaceController> mAppOverlaySurfaces = new HashSet();
    final IWindowSessionCallback mCallback;
    final boolean mCanAddInternalSystemWindow;
    final boolean mCanHideNonSystemOverlayWindows;
    final IInputMethodClient mClient;
    private boolean mClientDead = false;
    private float mLastReportedAnimatorScale;
    private int mNumWindow = 0;
    private String mPackageName;
    final int mPid;
    private String mRelayoutTag;
    final WindowManagerService mService;
    private boolean mShowingAlertWindowNotificationAllowed;
    private final String mStringName;
    SurfaceSession mSurfaceSession;
    final int mUid;

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0117 A:{Splitter: B:17:0x00b9, ExcHandler: all (th java.lang.Throwable)} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:34:0x010a, code:
            if (r10.mService.mInputMethodManager != null) goto L_0x010c;
     */
    /* JADX WARNING: Missing block: B:35:0x010c, code:
            r10.mService.mInputMethodManager.removeClient(r13);
     */
    /* JADX WARNING: Missing block: B:36:0x0113, code:
            android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Missing block: B:38:0x0118, code:
            android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Session(WindowManagerService service, IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        boolean z;
        boolean z2 = true;
        this.mService = service;
        this.mCallback = callback;
        this.mClient = client;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
        this.mLastReportedAnimatorScale = service.getCurrentAnimatorScale();
        if (service.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0) {
            z = true;
        } else {
            z = false;
        }
        this.mCanAddInternalSystemWindow = z;
        if (service.mContext.checkCallingOrSelfPermission("android.permission.HIDE_NON_SYSTEM_OVERLAY_WINDOWS") != 0) {
            z2 = false;
        }
        this.mCanHideNonSystemOverlayWindows = z2;
        this.mShowingAlertWindowNotificationAllowed = this.mService.mShowAlertWindowNotifications;
        StringBuilder sb = new StringBuilder();
        sb.append("Session{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" ");
        sb.append(this.mPid);
        if (this.mUid < 10000) {
            sb.append(":");
            sb.append(this.mUid);
        } else {
            sb.append(":u");
            sb.append(UserHandle.getUserId(this.mUid));
            sb.append('a');
            sb.append(UserHandle.getAppId(this.mUid));
        }
        sb.append("}");
        this.mStringName = sb.toString();
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mService.mInputMethodManager == null && this.mService.mHaveInputMethods) {
                    IBinder b = ServiceManager.getService("input_method");
                    this.mService.mInputMethodManager = IInputMethodManager.Stub.asInterface(b);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        long ident = Binder.clearCallingIdentity();
        try {
            if (this.mService.mInputMethodManager != null) {
                this.mService.mInputMethodManager.addClient(client, inputContext, this.mUid, this.mPid);
            } else {
                client.setUsingInputMethod(false);
            }
            client.asBinder().linkToDeath(this, 0);
            Binder.restoreCallingIdentity(ident);
        } catch (RemoteException e) {
        } catch (Throwable th) {
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf("WindowManager", "Window Session Crash", e);
            }
            throw e;
        }
    }

    public void binderDied() {
        try {
            if (this.mService.mInputMethodManager != null) {
                this.mService.mInputMethodManager.removeClient(this.mClient);
            }
        } catch (RemoteException e) {
        }
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mClient.asBinder().unlinkToDeath(this, 0);
                this.mClientDead = true;
                killSessionLocked();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int add(IWindow window, int seq, LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets, InputChannel outInputChannel) {
        return addToDisplay(window, seq, attrs, viewVisibility, 0, outContentInsets, outStableInsets, null, outInputChannel);
    }

    public int addToDisplay(IWindow window, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, InputChannel outInputChannel) {
        return this.mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, outContentInsets, outStableInsets, outOutsets, outInputChannel);
    }

    public int addWithoutInputChannel(IWindow window, int seq, LayoutParams attrs, int viewVisibility, Rect outContentInsets, Rect outStableInsets) {
        return addToDisplayWithoutInputChannel(window, seq, attrs, viewVisibility, 0, outContentInsets, outStableInsets);
    }

    public int addToDisplayWithoutInputChannel(IWindow window, int seq, LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets) {
        return this.mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, outContentInsets, outStableInsets, null, null);
    }

    public void remove(IWindow window) {
        this.mService.removeWindow(this, window);
    }

    public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) {
        this.mService.setWillReplaceWindows(appToken, childrenOnly);
    }

    public int relayout(IWindow window, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewFlags, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outsets, Rect outBackdropFrame, MergedConfiguration mergedConfiguration, Surface outSurface) {
        Trace.traceBegin(32, this.mRelayoutTag);
        int res = this.mService.relayoutWindow(this, window, seq, attrs, requestedWidth, requestedHeight, viewFlags, flags, outFrame, outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outsets, outBackdropFrame, mergedConfiguration, outSurface);
        Trace.traceEnd(32);
        return res;
    }

    public boolean outOfMemory(IWindow window) {
        return this.mService.outOfMemoryWindow(this, window);
    }

    public void setTransparentRegion(IWindow window, Region region) {
        this.mService.setTransparentRegionWindow(this, window, region);
    }

    public void setInsets(IWindow window, int touchableInsets, Rect contentInsets, Rect visibleInsets, Region touchableArea) {
        this.mService.setInsetsWindow(this, window, touchableInsets, contentInsets, visibleInsets, touchableArea);
    }

    public void getDisplayFrame(IWindow window, Rect outDisplayFrame) {
        this.mService.getWindowDisplayFrame(this, window, outDisplayFrame);
    }

    public void finishDrawing(IWindow window) {
        this.mService.finishDrawingWindow(this, window);
    }

    public void setInTouchMode(boolean mode) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mInTouchMode = mode;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean getInTouchMode() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mService.mInTouchMode;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public boolean performHapticFeedback(IWindow window, int effectId, boolean always) {
        boolean performHapticFeedbackLw;
        synchronized (this.mService.mWindowMap) {
            long ident;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                performHapticFeedbackLw = this.mService.mPolicy.performHapticFeedbackLw(this.mService.windowForClientLocked(this, window, true), effectId, always);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return performHapticFeedbackLw;
    }

    public IBinder prepareDrag(IWindow window, int flags, int width, int height, Surface outSurface) {
        return this.mService.prepareDragSurface(window, this.mSurfaceSession, flags, width, height, outSurface);
    }

    public boolean performDrag(IWindow window, IBinder dragToken, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mService.mDragState == null) {
                    Slog.w("WindowManager", "No drag prepared");
                    throw new IllegalStateException("performDrag() without prepareDrag()");
                } else if (dragToken != this.mService.mDragState.mToken) {
                    Slog.w("WindowManager", "Performing mismatched drag");
                    throw new IllegalStateException("performDrag() does not match prepareDrag()");
                } else {
                    WindowState callingWin = this.mService.windowForClientLocked(null, window, false);
                    if (callingWin == null) {
                        Slog.w("WindowManager", "Bad requesting window " + window);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    this.mService.mH.removeMessages(20, window.asBinder());
                    DisplayContent displayContent = callingWin.getDisplayContent();
                    if (displayContent == null) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return false;
                    }
                    Display display = displayContent.getDisplay();
                    this.mService.mDragState.register(display);
                    if (this.mService.mInputManager.transferTouchFocus(callingWin.mInputChannel, this.mService.mDragState.getInputChannel())) {
                        this.mService.mDragState.mDisplayContent = displayContent;
                        this.mService.mDragState.mData = data;
                        this.mService.mDragState.broadcastDragStartedLw(touchX, touchY);
                        this.mService.mDragState.overridePointerIconLw(touchSource);
                        this.mService.mDragState.mThumbOffsetX = thumbCenterX;
                        this.mService.mDragState.mThumbOffsetY = thumbCenterY;
                        SurfaceControl surfaceControl = this.mService.mDragState.mSurfaceControl;
                        this.mService.openSurfaceTransaction();
                        int dw;
                        int dh;
                        if (display.getDisplayId() == 0) {
                            if (this.mService.getLazyMode() != 0) {
                                dw = display.getWidth();
                                dh = display.getHeight();
                                if (1 == this.mService.getLazyMode()) {
                                    touchX *= 0.75f;
                                } else if (2 == this.mService.getLazyMode()) {
                                    touchX = (0.75f * touchX) + (((float) dw) * 0.25f);
                                }
                                touchY = (0.75f * touchY) + (((float) dh) * 0.25f);
                                thumbCenterX *= 0.75f;
                                thumbCenterY *= 0.75f;
                            }
                        } else if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(display.getDisplayId())) {
                            int pcMode = this.mService.getPCScreenDisplayMode();
                            if (pcMode != 0) {
                                Point point = new Point();
                                display.getRealSize(point);
                                dw = point.x;
                                dh = point.y;
                                float pcDisplayScale = pcMode == 1 ? 0.95f : 0.9f;
                                touchX = (touchX * pcDisplayScale) + ((((float) dw) * (1.0f - pcDisplayScale)) / 2.0f);
                                touchY = (touchY * pcDisplayScale) + ((((float) dh) * (1.0f - pcDisplayScale)) / 2.0f);
                                thumbCenterX *= pcDisplayScale;
                                thumbCenterY *= pcDisplayScale;
                            }
                        }
                        surfaceControl.setPosition(touchX - thumbCenterX, touchY - thumbCenterY);
                        surfaceControl.setLayer(this.mService.mDragState.getDragLayerLw());
                        surfaceControl.setLayerStack(display.getLayerStack());
                        surfaceControl.show();
                        this.mService.closeSurfaceTransaction();
                        this.mService.mDragState.notifyLocationLw(touchX, touchY);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return true;
                    }
                    Slog.e("WindowManager", "Unable to transfer touch focus");
                    this.mService.mDragState.unregister();
                    this.mService.mDragState.reset();
                    this.mService.mDragState = null;
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean startMovingTask(IWindow window, float startX, float startY) {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean startMovingTask = this.mService.startMovingTask(window, startX, startY);
            return startMovingTask;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void reportDropResult(IWindow window, boolean consumed) {
        IBinder token = window.asBinder();
        synchronized (this.mService.mWindowMap) {
            long ident;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                if (this.mService.mDragState == null) {
                    Slog.w("WindowManager", "Drop result given but no drag in progress");
                    Binder.restoreCallingIdentity(ident);
                    WindowManagerService.resetPriorityAfterLockedSection();
                } else if (this.mService.mDragState.mToken != token) {
                    Slog.w("WindowManager", "Invalid drop-result claim by " + window);
                    throw new IllegalStateException("reportDropResult() by non-recipient");
                } else {
                    this.mService.mH.removeMessages(21, window.asBinder());
                    if (this.mService.windowForClientLocked(null, window, false) == null) {
                        Slog.w("WindowManager", "Bad result-reporting window " + window);
                        Binder.restoreCallingIdentity(ident);
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return;
                    }
                    this.mService.mDragState.mDragResult = consumed;
                    this.mService.mDragState.endDragLw();
                    Binder.restoreCallingIdentity(ident);
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void cancelDragAndDrop(IBinder dragToken) {
        synchronized (this.mService.mWindowMap) {
            long ident;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                if (this.mService.mDragState == null) {
                    Slog.w("WindowManager", "cancelDragAndDrop() without prepareDrag()");
                    throw new IllegalStateException("cancelDragAndDrop() without prepareDrag()");
                } else if (this.mService.mDragState.mToken != dragToken) {
                    Slog.w("WindowManager", "cancelDragAndDrop() does not match prepareDrag()");
                    throw new IllegalStateException("cancelDragAndDrop() does not match prepareDrag()");
                } else {
                    this.mService.mDragState.mDragResult = false;
                    this.mService.mDragState.cancelDragLw();
                    Binder.restoreCallingIdentity(ident);
                }
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void dragRecipientEntered(IWindow window) {
    }

    public void dragRecipientExited(IWindow window) {
    }

    public void setWallpaperPosition(IBinder window, float x, float y, float xStep, float yStep) {
        synchronized (this.mService.mWindowMap) {
            long ident;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                this.mService.mRoot.mWallpaperController.setWindowWallpaperPosition(this.mService.windowForClientLocked(this, window, true), x, y, xStep, yStep);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void wallpaperOffsetsComplete(IBinder window) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mRoot.mWallpaperController.wallpaperOffsetsComplete(window);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setWallpaperDisplayOffset(IBinder window, int x, int y) {
        synchronized (this.mService.mWindowMap) {
            long ident;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                this.mService.mRoot.mWallpaperController.setWindowWallpaperDisplayOffset(this.mService.windowForClientLocked(this, window, true), x, y);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        Bundle sendWindowWallpaperCommand;
        synchronized (this.mService.mWindowMap) {
            long ident;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                ident = Binder.clearCallingIdentity();
                sendWindowWallpaperCommand = this.mService.mRoot.mWallpaperController.sendWindowWallpaperCommand(this.mService.windowForClientLocked(this, window, true), action, x, y, z, extras, sync);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return sendWindowWallpaperCommand;
    }

    public void wallpaperCommandComplete(IBinder window, Bundle result) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mRoot.mWallpaperController.wallpaperCommandComplete(window);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        synchronized (this.mService.mWindowMap) {
            long identity;
            try {
                WindowManagerService.boostPriorityForLockedSection();
                identity = Binder.clearCallingIdentity();
                this.mService.onRectangleOnScreenRequested(token, rectangle);
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public IWindowId getWindowId(IBinder window) {
        return this.mService.getWindowId(window);
    }

    public void pokeDrawLock(IBinder window) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mService.pokeDrawLock(this, window);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void updatePointerIcon(IWindow window) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mService.updatePointerIcon(window);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    void windowAddedLocked(String packageName) {
        this.mPackageName = packageName;
        this.mRelayoutTag = "relayoutWindow: " + this.mPackageName;
        if (this.mSurfaceSession == null) {
            this.mSurfaceSession = new SurfaceSession();
            this.mService.mSessions.add(this);
            if (this.mLastReportedAnimatorScale != this.mService.getCurrentAnimatorScale()) {
                this.mService.dispatchNewAnimatorScaleLocked(this);
            }
        }
        this.mNumWindow++;
    }

    void windowRemovedLocked() {
        this.mNumWindow--;
        killSessionLocked();
    }

    void onWindowSurfaceVisibilityChanged(WindowSurfaceController surfaceController, boolean visible, int type) {
        if (LayoutParams.isSystemAlertWindowType(type)) {
            boolean changed;
            if (!this.mCanAddInternalSystemWindow) {
                if (visible) {
                    changed = this.mAlertWindowSurfaces.add(surfaceController);
                } else {
                    changed = this.mAlertWindowSurfaces.remove(surfaceController);
                }
                if (changed) {
                    if (this.mAlertWindowSurfaces.isEmpty()) {
                        cancelAlertWindowNotification();
                    } else if (this.mAlertWindowNotification == null) {
                        this.mAlertWindowNotification = new AlertWindowNotification(this.mService, this.mPackageName);
                        if (this.mShowingAlertWindowNotificationAllowed) {
                            this.mAlertWindowNotification.post();
                        }
                    }
                }
            }
            if (type == 2038) {
                if (visible) {
                    changed = this.mAppOverlaySurfaces.add(surfaceController);
                } else {
                    changed = this.mAppOverlaySurfaces.remove(surfaceController);
                }
                if (changed) {
                    setHasOverlayUi(this.mAppOverlaySurfaces.isEmpty() ^ 1);
                }
            }
        }
    }

    void setShowingAlertWindowNotificationAllowed(boolean allowed) {
        this.mShowingAlertWindowNotificationAllowed = allowed;
        if (this.mAlertWindowNotification == null) {
            return;
        }
        if (allowed) {
            this.mAlertWindowNotification.post();
        } else {
            this.mAlertWindowNotification.cancel();
        }
    }

    private void killSessionLocked() {
        if (this.mNumWindow <= 0 && (this.mClientDead ^ 1) == 0) {
            this.mService.mSessions.remove(this);
            if (this.mSurfaceSession != null) {
                try {
                    this.mSurfaceSession.kill();
                } catch (Exception e) {
                    Slog.w("WindowManager", "Exception thrown when killing surface session " + this.mSurfaceSession + " in session " + this + ": " + e.toString());
                }
                this.mSurfaceSession = null;
                this.mAlertWindowSurfaces.clear();
                this.mAppOverlaySurfaces.clear();
                setHasOverlayUi(false);
                cancelAlertWindowNotification();
            }
        }
    }

    private void setHasOverlayUi(boolean hasOverlayUi) {
        this.mService.mH.obtainMessage(58, this.mPid, hasOverlayUi ? 1 : 0).sendToTarget();
    }

    private void cancelAlertWindowNotification() {
        if (this.mAlertWindowNotification != null) {
            this.mAlertWindowNotification.cancel();
            this.mAlertWindowNotification = null;
        }
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mNumWindow=");
        pw.print(this.mNumWindow);
        pw.print(" mCanAddInternalSystemWindow=");
        pw.print(this.mCanAddInternalSystemWindow);
        pw.print(" mAppOverlaySurfaces=");
        pw.print(this.mAppOverlaySurfaces);
        pw.print(" mAlertWindowSurfaces=");
        pw.print(this.mAlertWindowSurfaces);
        pw.print(" mClientDead=");
        pw.print(this.mClientDead);
        pw.print(" mSurfaceSession=");
        pw.println(this.mSurfaceSession);
        pw.print(prefix);
        pw.print("mPackageName=");
        pw.println(this.mPackageName);
    }

    public String toString() {
        return this.mStringName;
    }
}

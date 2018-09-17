package com.android.server.wm;

import android.content.ClipData;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
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
import com.android.server.power.AbsPowerManagerService;
import java.io.PrintWriter;

final class Session extends Stub implements DeathRecipient {
    final IWindowSessionCallback mCallback;
    final IInputMethodClient mClient;
    boolean mClientDead;
    final IInputContext mInputContext;
    float mLastReportedAnimatorScale;
    int mNumWindow;
    final int mPid;
    final WindowManagerService mService;
    final String mStringName;
    SurfaceSession mSurfaceSession;
    final int mUid;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Session(WindowManagerService service, IWindowSessionCallback callback, IInputMethodClient client, IInputContext inputContext) {
        this.mNumWindow = 0;
        this.mClientDead = false;
        this.mService = service;
        this.mCallback = callback;
        this.mClient = client;
        this.mInputContext = inputContext;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
        this.mLastReportedAnimatorScale = service.getCurrentAnimatorScale();
        StringBuilder sb = new StringBuilder();
        sb.append("Session{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" ");
        sb.append(this.mPid);
        if (this.mUid < AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) {
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
            if (this.mService.mInputMethodManager == null && this.mService.mHaveInputMethods) {
                IBinder b = ServiceManager.getService("input_method");
                this.mService.mInputMethodManager = IInputMethodManager.Stub.asInterface(b);
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
            Binder.restoreCallingIdentity(ident);
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
            this.mClient.asBinder().unlinkToDeath(this, 0);
            this.mClientDead = true;
            killSessionLocked();
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

    public void repositionChild(IWindow window, int left, int top, int right, int bottom, long deferTransactionUntilFrame, Rect outFrame) {
        this.mService.repositionChild(this, window, left, top, right, bottom, deferTransactionUntilFrame, outFrame);
    }

    public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) {
        this.mService.setReplacingWindows(appToken, childrenOnly);
    }

    public int relayout(IWindow window, int seq, LayoutParams attrs, int requestedWidth, int requestedHeight, int viewFlags, int flags, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outsets, Rect outBackdropFrame, Configuration outConfig, Surface outSurface) {
        return this.mService.relayoutWindow(this, window, seq, attrs, requestedWidth, requestedHeight, viewFlags, flags, outFrame, outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outsets, outBackdropFrame, outConfig, outSurface);
    }

    public void performDeferredDestroy(IWindow window) {
        this.mService.performDeferredDestroyWindow(this, window);
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
            this.mService.mInTouchMode = mode;
        }
    }

    public boolean getInTouchMode() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            z = this.mService.mInTouchMode;
        }
        return z;
    }

    public boolean performHapticFeedback(IWindow window, int effectId, boolean always) {
        boolean performHapticFeedbackLw;
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                performHapticFeedbackLw = this.mService.mPolicy.performHapticFeedbackLw(this.mService.windowForClientLocked(this, window, true), effectId, always);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return performHapticFeedbackLw;
    }

    public IBinder prepareDrag(IWindow window, int flags, int width, int height, Surface outSurface) {
        return this.mService.prepareDragSurface(window, this.mSurfaceSession, flags, width, height, outSurface);
    }

    public boolean performDrag(IWindow window, IBinder dragToken, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        synchronized (this.mService.mWindowMap) {
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
                    return false;
                }
                this.mService.mH.removeMessages(20, window.asBinder());
                DisplayContent displayContent = callingWin.getDisplayContent();
                if (displayContent == null) {
                    return false;
                }
                Display display = displayContent.getDisplay();
                this.mService.mDragState.register(display);
                this.mService.mInputMonitor.updateInputWindowsLw(true);
                if (this.mService.mInputManager.transferTouchFocus(callingWin.mInputChannel, this.mService.mDragState.mServerChannel)) {
                    this.mService.mDragState.mData = data;
                    this.mService.mDragState.broadcastDragStartedLw(touchX, touchY);
                    this.mService.mDragState.overridePointerIconLw(touchSource);
                    this.mService.mDragState.mThumbOffsetX = thumbCenterX;
                    this.mService.mDragState.mThumbOffsetY = thumbCenterY;
                    SurfaceControl surfaceControl = this.mService.mDragState.mSurfaceControl;
                    SurfaceControl.openTransaction();
                    try {
                        if (this.mService.getLazyMode() != 0) {
                            int dw = display.getWidth();
                            int dh = display.getHeight();
                            if (1 == this.mService.getLazyMode()) {
                                touchX *= 0.75f;
                            } else if (2 == this.mService.getLazyMode()) {
                                touchX = (0.75f * touchX) + (((float) dw) * 0.25f);
                            }
                            touchY = (0.75f * touchY) + (((float) dh) * 0.25f);
                            thumbCenterX *= 0.75f;
                            thumbCenterY *= 0.75f;
                        }
                        surfaceControl.setPosition(touchX - thumbCenterX, touchY - thumbCenterY);
                        surfaceControl.setLayer(this.mService.mDragState.getDragLayerLw());
                        surfaceControl.setLayerStack(display.getLayerStack());
                        surfaceControl.show();
                        this.mService.mDragState.notifyLocationLw(touchX, touchY);
                        return true;
                    } finally {
                        SurfaceControl.closeTransaction();
                    }
                } else {
                    Slog.e("WindowManager", "Unable to transfer touch focus");
                    this.mService.mDragState.unregister();
                    this.mService.mDragState = null;
                    this.mService.mInputMonitor.updateInputWindowsLw(true);
                    return false;
                }
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
            long ident = Binder.clearCallingIdentity();
            try {
                if (this.mService.mDragState == null) {
                    Slog.w("WindowManager", "Drop result given but no drag in progress");
                    Binder.restoreCallingIdentity(ident);
                    return;
                } else if (this.mService.mDragState.mToken != token) {
                    Slog.w("WindowManager", "Invalid drop-result claim by " + window);
                    throw new IllegalStateException("reportDropResult() by non-recipient");
                } else {
                    this.mService.mH.removeMessages(21, window.asBinder());
                    if (this.mService.windowForClientLocked(null, window, false) == null) {
                        Slog.w("WindowManager", "Bad result-reporting window " + window);
                        Binder.restoreCallingIdentity(ident);
                        return;
                    }
                    this.mService.mDragState.mDragResult = consumed;
                    this.mService.mDragState.endDragLw();
                    Binder.restoreCallingIdentity(ident);
                    return;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void cancelDragAndDrop(IBinder dragToken) {
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
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
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void dragRecipientEntered(IWindow window) {
    }

    public void dragRecipientExited(IWindow window) {
    }

    public void setWallpaperPosition(IBinder window, float x, float y, float xStep, float yStep) {
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mService.mWallpaperControllerLocked.setWindowWallpaperPosition(this.mService.windowForClientLocked(this, window, true), x, y, xStep, yStep);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public void wallpaperOffsetsComplete(IBinder window) {
        synchronized (this.mService.mWindowMap) {
            this.mService.mWallpaperControllerLocked.wallpaperOffsetsComplete(window);
        }
    }

    public void setWallpaperDisplayOffset(IBinder window, int x, int y) {
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mService.mWallpaperControllerLocked.setWindowWallpaperDisplayOffset(this.mService.windowForClientLocked(this, window, true), x, y);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        Bundle sendWindowWallpaperCommand;
        synchronized (this.mService.mWindowMap) {
            long ident = Binder.clearCallingIdentity();
            try {
                sendWindowWallpaperCommand = this.mService.mWallpaperControllerLocked.sendWindowWallpaperCommand(this.mService.windowForClientLocked(this, window, true), action, x, y, z, extras, sync);
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return sendWindowWallpaperCommand;
    }

    public void wallpaperCommandComplete(IBinder window, Bundle result) {
        synchronized (this.mService.mWindowMap) {
            this.mService.mWallpaperControllerLocked.wallpaperCommandComplete(window);
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        synchronized (this.mService.mWindowMap) {
            long identity = Binder.clearCallingIdentity();
            try {
                this.mService.onRectangleOnScreenRequested(token, rectangle);
                Binder.restoreCallingIdentity(identity);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity);
            }
        }
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

    void windowAddedLocked() {
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

    void killSessionLocked() {
        if (this.mNumWindow <= 0 && this.mClientDead) {
            this.mService.mSessions.remove(this);
            if (this.mSurfaceSession != null) {
                try {
                    this.mSurfaceSession.kill();
                } catch (Exception e) {
                    Slog.w("WindowManager", "Exception thrown when killing surface session " + this.mSurfaceSession + " in session " + this + ": " + e.toString());
                }
                this.mSurfaceSession = null;
            }
        }
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mNumWindow=");
        pw.print(this.mNumWindow);
        pw.print(" mClientDead=");
        pw.print(this.mClientDead);
        pw.print(" mSurfaceSession=");
        pw.println(this.mSurfaceSession);
    }

    public String toString() {
        return this.mStringName;
    }
}

package com.android.server.wm;

import android.content.ClipData;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.view.DisplayCutout;
import android.view.IWindow;
import android.view.IWindowId;
import android.view.IWindowSession;
import android.view.IWindowSessionCallback;
import android.view.InputChannel;
import android.view.InsetsState;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManager;
import com.android.internal.os.logging.MetricsLoggerWrapper;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/* access modifiers changed from: package-private */
public class Session extends IWindowSession.Stub implements IBinder.DeathRecipient {
    private AlertWindowNotification mAlertWindowNotification;
    private final Set<WindowSurfaceController> mAlertWindowSurfaces = new HashSet();
    private final Set<WindowSurfaceController> mAppOverlaySurfaces = new HashSet();
    final IWindowSessionCallback mCallback;
    final boolean mCanAcquireSleepToken;
    final boolean mCanAddInternalSystemWindow;
    final boolean mCanHideNonSystemOverlayWindows;
    private boolean mClientDead = false;
    private final DragDropController mDragDropController;
    private float mLastReportedAnimatorScale;
    private int mNumWindow = 0;
    private String mPackageName;
    final int mPid;
    private String mRelayoutTag = "";
    final WindowManagerService mService;
    private boolean mShowingAlertWindowNotificationAllowed;
    private final String mStringName;
    SurfaceSession mSurfaceSession;
    final int mUid;

    public Session(WindowManagerService service, IWindowSessionCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mUid = Binder.getCallingUid();
        this.mPid = Binder.getCallingPid();
        this.mLastReportedAnimatorScale = service.getCurrentAnimatorScale();
        boolean z = true;
        this.mCanAddInternalSystemWindow = service.mContext.checkCallingOrSelfPermission("android.permission.INTERNAL_SYSTEM_WINDOW") == 0;
        this.mCanHideNonSystemOverlayWindows = service.mContext.checkCallingOrSelfPermission("android.permission.HIDE_NON_SYSTEM_OVERLAY_WINDOWS") == 0;
        this.mCanAcquireSleepToken = service.mContext.checkCallingOrSelfPermission("android.permission.DEVICE_POWER") != 0 ? false : z;
        this.mShowingAlertWindowNotificationAllowed = this.mService.mShowAlertWindowNotifications;
        this.mDragDropController = this.mService.mDragDropController;
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
        try {
            this.mCallback.asBinder().linkToDeath(this, 0);
        } catch (RemoteException e) {
        }
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        try {
            return Session.super.onTransact(code, data, reply, flags);
        } catch (RuntimeException e) {
            if (!(e instanceof SecurityException)) {
                Slog.wtf("WindowManager", "Window Session Crash", e);
            }
            throw e;
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mCallback.asBinder().unlinkToDeath(this, 0);
                this.mClientDead = true;
                killSessionLocked();
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public int addToDisplay(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outFrame, Rect outContentInsets, Rect outStableInsets, Rect outOutsets, DisplayCutout.ParcelableWrapper outDisplayCutout, InputChannel outInputChannel, InsetsState outInsetsState) {
        return this.mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, outFrame, outContentInsets, outStableInsets, outOutsets, outDisplayCutout, outInputChannel, outInsetsState);
    }

    public int addToDisplayWithoutInputChannel(IWindow window, int seq, WindowManager.LayoutParams attrs, int viewVisibility, int displayId, Rect outContentInsets, Rect outStableInsets, InsetsState outInsetsState) {
        return this.mService.addWindow(this, window, seq, attrs, viewVisibility, displayId, new Rect(), outContentInsets, outStableInsets, null, new DisplayCutout.ParcelableWrapper(), null, outInsetsState);
    }

    public void remove(IWindow window) {
        this.mService.removeWindow(this, window);
    }

    public void prepareToReplaceWindows(IBinder appToken, boolean childrenOnly) {
        this.mService.setWillReplaceWindows(appToken, childrenOnly);
    }

    public int relayout(IWindow window, int seq, WindowManager.LayoutParams attrs, int requestedWidth, int requestedHeight, int viewFlags, int flags, long frameNumber, Rect outFrame, Rect outOverscanInsets, Rect outContentInsets, Rect outVisibleInsets, Rect outStableInsets, Rect outsets, Rect outBackdropFrame, DisplayCutout.ParcelableWrapper cutout, MergedConfiguration mergedConfiguration, SurfaceControl outSurfaceControl, InsetsState outInsetsState) {
        Trace.traceBegin(32, this.mRelayoutTag);
        int res = this.mService.relayoutWindow(this, window, seq, attrs, requestedWidth, requestedHeight, viewFlags, flags, frameNumber, outFrame, outOverscanInsets, outContentInsets, outVisibleInsets, outStableInsets, outsets, outBackdropFrame, cutout, mergedConfiguration, outSurfaceControl, outInsetsState);
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
        synchronized (this.mService.mGlobalLock) {
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
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mService.mInTouchMode;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    public boolean performHapticFeedback(int effectId, boolean always) {
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mService.mPolicy.performHapticFeedback(this.mUid, this.mPackageName, effectId, always, (String) null);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public IBinder performDrag(IWindow window, int flags, SurfaceControl surface, int touchSource, float touchX, float touchY, float thumbCenterX, float thumbCenterY, ClipData data) {
        int callerPid = Binder.getCallingPid();
        int callerUid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mDragDropController.performDrag(this.mSurfaceSession, callerPid, callerUid, window, flags, surface, touchSource, touchX, touchY, thumbCenterX, thumbCenterY, data);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void reportDropResult(IWindow window, boolean consumed) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mDragDropController.reportDropResult(window, consumed);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void cancelDragAndDrop(IBinder dragToken, boolean skipAnimation) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mDragDropController.cancelDragAndDrop(dragToken, skipAnimation);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void dragRecipientEntered(IWindow window) {
        this.mDragDropController.dragRecipientEntered(window);
    }

    public void dragRecipientExited(IWindow window) {
        this.mDragDropController.dragRecipientExited(window);
    }

    public void setPendingDragEndedLoc(IWindow window, int x, int y) {
        this.mDragDropController.setPendingDragEndedLoc(window, x, y);
    }

    public void setOriginalDragViewCenter(IWindow window, int x, int y) {
        DragDropController dragDropController = this.mDragDropController;
        if (dragDropController != null) {
            dragDropController.setOriginalDragViewCenter(window, x, y);
        }
    }

    public void dragRecipientFeedback(IWindow window, boolean isEntered, int mimeTypeSupportState, int acceptableItemCnt) {
        this.mDragDropController.dragRecipientFeedback(window, isEntered, mimeTypeSupportState, acceptableItemCnt);
    }

    public boolean startMovingTask(IWindow window, float startX, float startY) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "startMovingTask: {" + startX + "," + startY + "}");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            return this.mService.mTaskPositioningController.startMovingTask(window, startX, startY);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void finishMovingTask(IWindow window) {
        if (WindowManagerDebugConfig.DEBUG_TASK_POSITIONING) {
            Slog.d("WindowManager", "finishMovingTask");
        }
        long ident = Binder.clearCallingIdentity();
        try {
            this.mService.mTaskPositioningController.finishTaskPositioning(window);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void reportSystemGestureExclusionChanged(IWindow window, List<Rect> exclusionRects) {
        long ident = Binder.clearCallingIdentity();
        try {
            this.mService.reportSystemGestureExclusionChanged(this, window, exclusionRects);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void actionOnWallpaper(IBinder window, BiConsumer<WallpaperController, WindowState> action) {
        WindowState windowState = this.mService.windowForClientLocked(this, window, true);
        action.accept(windowState.getDisplayContent().mWallpaperController, windowState);
    }

    public void setWallpaperPosition(IBinder window, float x, float y, float xStep, float yStep) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    actionOnWallpaper(window, new BiConsumer(x, y, xStep, yStep) {
                        /* class com.android.server.wm.$$Lambda$Session$zgdcs0nAb8hCdS6ugnFMadbhU8 */
                        private final /* synthetic */ float f$0;
                        private final /* synthetic */ float f$1;
                        private final /* synthetic */ float f$2;
                        private final /* synthetic */ float f$3;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                        }

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            ((WallpaperController) obj).setWindowWallpaperPosition((WindowState) obj2, this.f$0, this.f$1, this.f$2, this.f$3);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void wallpaperOffsetsComplete(IBinder window) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                actionOnWallpaper(window, new BiConsumer(window) {
                    /* class com.android.server.wm.$$Lambda$Session$15hO_YO9_yR6FTMdPPe87fZzL1c */
                    private final /* synthetic */ IBinder f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.BiConsumer
                    public final void accept(Object obj, Object obj2) {
                        WindowState windowState = (WindowState) obj2;
                        ((WallpaperController) obj).wallpaperOffsetsComplete(this.f$0);
                    }
                });
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void setWallpaperDisplayOffset(IBinder window, int x, int y) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    actionOnWallpaper(window, new BiConsumer(x, y) {
                        /* class com.android.server.wm.$$Lambda$Session$3q7E1KtcKfO8_a7pOH0nnVURP8w */
                        private final /* synthetic */ int f$0;
                        private final /* synthetic */ int f$1;

                        {
                            this.f$0 = r1;
                            this.f$1 = r2;
                        }

                        @Override // java.util.function.BiConsumer
                        public final void accept(Object obj, Object obj2) {
                            ((WallpaperController) obj).setWindowWallpaperDisplayOffset((WindowState) obj2, this.f$0, this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public Bundle sendWallpaperCommand(IBinder window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        Throwable th;
        Throwable th2;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long ident = Binder.clearCallingIdentity();
                try {
                    try {
                        WindowState windowState = this.mService.windowForClientLocked(this, window, true);
                        Bundle sendWindowWallpaperCommand = windowState.getDisplayContent().mWallpaperController.sendWindowWallpaperCommand(windowState, action, x, y, z, extras, sync);
                        try {
                            Binder.restoreCallingIdentity(ident);
                            WindowManagerService.resetPriorityAfterLockedSection();
                            return sendWindowWallpaperCommand;
                        } catch (Throwable th3) {
                            th = th3;
                            WindowManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                        Binder.restoreCallingIdentity(ident);
                        throw th2;
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    Binder.restoreCallingIdentity(ident);
                    throw th2;
                }
            } catch (Throwable th6) {
                th = th6;
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
    }

    public void wallpaperCommandComplete(IBinder window, Bundle result) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                actionOnWallpaper(window, new BiConsumer(window) {
                    /* class com.android.server.wm.$$Lambda$Session$6cG7louvKZjAfcc7DtiA7aAzr7U */
                    private final /* synthetic */ IBinder f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.BiConsumer
                    public final void accept(Object obj, Object obj2) {
                        WindowState windowState = (WindowState) obj2;
                        ((WallpaperController) obj).wallpaperCommandComplete(this.f$0);
                    }
                });
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onRectangleOnScreenRequested(IBinder token, Rect rectangle) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                long identity = Binder.clearCallingIdentity();
                try {
                    this.mService.onRectangleOnScreenRequested(token, rectangle);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
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

    public void reparentDisplayContent(IWindow window, SurfaceControl sc, int displayId) {
        this.mService.reparentDisplayContent(window, sc, displayId);
    }

    public void updateDisplayContentLocation(IWindow window, int x, int y, int displayId) {
        this.mService.updateDisplayContentLocation(window, x, y, displayId);
    }

    public void updateTapExcludeRegion(IWindow window, int regionId, Region region) {
        long identity = Binder.clearCallingIdentity();
        try {
            this.mService.updateTapExcludeRegion(window, regionId, region);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void insetsModified(IWindow window, InsetsState state) {
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                WindowState windowState = this.mService.windowForClientLocked(this, window, false);
                if (windowState != null) {
                    windowState.getDisplayContent().getInsetsStateController().onInsetsModified(windowState, state);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void windowAddedLocked(String packageName) {
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

    /* access modifiers changed from: package-private */
    public void windowRemovedLocked() {
        this.mNumWindow--;
        killSessionLocked();
    }

    /* access modifiers changed from: package-private */
    public void onWindowSurfaceVisibilityChanged(WindowSurfaceController surfaceController, boolean visible, int type) {
        boolean changed;
        boolean changed2;
        if (WindowManager.LayoutParams.isSystemAlertWindowType(type)) {
            if (!this.mCanAddInternalSystemWindow) {
                if (visible) {
                    changed2 = this.mAlertWindowSurfaces.add(surfaceController);
                    MetricsLoggerWrapper.logAppOverlayEnter(this.mUid, this.mPackageName, changed2, type, true);
                } else {
                    changed2 = this.mAlertWindowSurfaces.remove(surfaceController);
                    MetricsLoggerWrapper.logAppOverlayExit(this.mUid, this.mPackageName, changed2, type, true);
                }
                if (changed2) {
                    if (this.mAlertWindowSurfaces.isEmpty()) {
                        cancelAlertWindowNotification();
                    } else if (this.mAlertWindowNotification == null) {
                        this.mAlertWindowNotification = new AlertWindowNotification(this.mService, this.mPackageName);
                        if (this.mShowingAlertWindowNotificationAllowed) {
                            this.mAlertWindowNotification.post();
                            this.mService.mAtmService.mHwATMSEx.dispatchSystemAlertWindowChange(this.mPackageName, this.mUid, true);
                        }
                    }
                }
            }
            if (type == 2038) {
                if (visible) {
                    changed = this.mAppOverlaySurfaces.add(surfaceController);
                    MetricsLoggerWrapper.logAppOverlayEnter(this.mUid, this.mPackageName, changed, type, false);
                } else {
                    changed = this.mAppOverlaySurfaces.remove(surfaceController);
                    MetricsLoggerWrapper.logAppOverlayExit(this.mUid, this.mPackageName, changed, type, false);
                }
                if (changed) {
                    setHasOverlayUi(!this.mAppOverlaySurfaces.isEmpty());
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setShowingAlertWindowNotificationAllowed(boolean allowed) {
        this.mShowingAlertWindowNotificationAllowed = allowed;
        AlertWindowNotification alertWindowNotification = this.mAlertWindowNotification;
        if (alertWindowNotification == null) {
            return;
        }
        if (allowed) {
            alertWindowNotification.post();
        } else {
            alertWindowNotification.cancel(false);
        }
    }

    private void killSessionLocked() {
        if (this.mNumWindow <= 0 && this.mClientDead) {
            this.mService.mSessions.remove(this);
            SurfaceSession surfaceSession = this.mSurfaceSession;
            if (surfaceSession != null) {
                try {
                    surfaceSession.kill();
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
        AlertWindowNotification alertWindowNotification = this.mAlertWindowNotification;
        if (alertWindowNotification != null) {
            alertWindowNotification.cancel(true);
            this.mAlertWindowNotification = null;
            this.mService.mAtmService.mHwATMSEx.dispatchSystemAlertWindowChange(this.mPackageName, this.mUid, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
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

    @Override // java.lang.Object
    public String toString() {
        return this.mStringName;
    }

    /* access modifiers changed from: package-private */
    public boolean hasAlertWindowSurfaces() {
        return !this.mAlertWindowSurfaces.isEmpty();
    }
}

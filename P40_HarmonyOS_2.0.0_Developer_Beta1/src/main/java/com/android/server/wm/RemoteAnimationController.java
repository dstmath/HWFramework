package com.android.server.wm;

import android.app.WindowConfiguration;
import android.common.HwFrameworkFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.IRemoteAnimationFinishedCallback;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationTarget;
import android.view.SurfaceControl;
import com.android.internal.util.FastPrintWriter;
import com.android.server.wm.SurfaceAnimator;
import com.android.server.wm.utils.InsetUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

/* access modifiers changed from: package-private */
public class RemoteAnimationController implements IBinder.DeathRecipient {
    private static final String TAG = "RemoteAnimationController";
    private static final long TIMEOUT_MS = 2000;
    private boolean mCanceled;
    private FinishedCallback mFinishedCallback;
    private final Handler mHandler;
    private boolean mLinkedToDeathOfRunner;
    private final ArrayList<RemoteAnimationRecord> mPendingAnimations = new ArrayList<>();
    private final RemoteAnimationAdapter mRemoteAnimationAdapter;
    private final WindowManagerService mService;
    private final Runnable mTimeoutRunnable = new Runnable() {
        /* class com.android.server.wm.$$Lambda$RemoteAnimationController$uQS8vaPKQE3x_9G8NCxPQmw1fw */

        @Override // java.lang.Runnable
        public final void run() {
            RemoteAnimationController.this.lambda$new$0$RemoteAnimationController();
        }
    };
    private final Rect mTmpRect = new Rect();

    public /* synthetic */ void lambda$new$0$RemoteAnimationController() {
        cancelAnimation("timeoutRunnable");
    }

    RemoteAnimationController(WindowManagerService service, RemoteAnimationAdapter remoteAnimationAdapter, Handler handler) {
        this.mService = service;
        this.mRemoteAnimationAdapter = remoteAnimationAdapter;
        this.mHandler = handler;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationRecord createRemoteAnimationRecord(AppWindowToken appWindowToken, Point position, Rect stackBounds, Rect startBounds) {
        Slog.d(TAG, "createAnimationAdapter(): token=" + appWindowToken);
        RemoteAnimationRecord adapters = new RemoteAnimationRecord(appWindowToken, position, stackBounds, startBounds);
        this.mPendingAnimations.add(adapters);
        return adapters;
    }

    /* access modifiers changed from: package-private */
    public void goodToGo() {
        Slog.d(TAG, "goodToGo()");
        if (this.mPendingAnimations.isEmpty() || this.mCanceled) {
            if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                Slog.d(TAG, "goodToGo(): Animation finished already, canceled=" + this.mCanceled + " mPendingAnimations=" + this.mPendingAnimations.size());
            }
            onAnimationFinished();
            return;
        }
        this.mHandler.postDelayed(this.mTimeoutRunnable, (long) (this.mService.getCurrentAnimatorScale() * 2000.0f));
        this.mFinishedCallback = new FinishedCallback(this);
        RemoteAnimationTarget[] animations = createAnimations();
        if (animations.length == 0) {
            if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                Slog.d(TAG, "goodToGo(): No apps to animate");
            }
            onAnimationFinished();
            return;
        }
        this.mService.mAnimator.addAfterPrepareSurfacesRunnable(new Runnable(animations) {
            /* class com.android.server.wm.$$Lambda$RemoteAnimationController$f_Hsu4PN7pGOiq9Nl8vxzEA3wa0 */
            private final /* synthetic */ RemoteAnimationTarget[] f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                RemoteAnimationController.this.lambda$goodToGo$1$RemoteAnimationController(this.f$1);
            }
        });
        sendRunningRemoteAnimation(true);
    }

    public /* synthetic */ void lambda$goodToGo$1$RemoteAnimationController(RemoteAnimationTarget[] animations) {
        try {
            linkToDeathOfRunner();
            if (!this.mService.isInWallpaperEffect()) {
                Slog.d(TAG, "RAC addAfterPrepareSurfacesRunnable prepareLauncherLeashVisible");
                for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
                    RemoteAnimationRecord wrappers = this.mPendingAnimations.get(i);
                    if (wrappers.mAppWindowToken.getName().contains(DisplayPolicy.LAUNCHER_PACKAGE_NAME)) {
                        Slog.d(TAG, "when launcher case, set the surface alpha value of launcher to 1.0f, and the value of leash parent to 0.0f");
                        this.mService.prepareLauncherLeashVisible(wrappers.mAdapter.mCapturedLeash, wrappers.mAppWindowToken);
                    }
                }
            }
            this.mRemoteAnimationAdapter.getRunner().onAnimationStart(animations, this.mFinishedCallback);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to start remote animation", e);
            onAnimationFinished();
        }
        Slog.d(TAG, "startAnimation(): Notify animation start:");
        if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
            writeStartDebugStatement();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation(String reason) {
        Slog.d(TAG, "cancelAnimation(): reason=" + reason);
        synchronized (this.mService.getWindowManagerLock()) {
            if (!this.mCanceled) {
                this.mCanceled = true;
                onAnimationFinished();
                invokeAnimationCancelled();
            }
        }
    }

    private void writeStartDebugStatement() {
        Slog.i(TAG, "Starting remote animation");
        StringWriter sw = new StringWriter();
        FastPrintWriter pw = new FastPrintWriter(sw);
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            this.mPendingAnimations.get(i).mAdapter.dump(pw, "");
        }
        pw.close();
        Slog.i(TAG, sw.toString());
    }

    private RemoteAnimationTarget[] createAnimations() {
        Slog.d(TAG, "createAnimations()");
        ArrayList<RemoteAnimationTarget> targets = new ArrayList<>();
        for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
            RemoteAnimationRecord wrappers = this.mPendingAnimations.get(i);
            RemoteAnimationTarget target = wrappers.createRemoteAnimationTarget();
            if (target != null) {
                if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                    Slog.d(TAG, "\tAdd token=" + wrappers.mAppWindowToken);
                }
                targets.add(target);
            } else {
                if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                    Slog.d(TAG, "\tRemove token=" + wrappers.mAppWindowToken);
                }
                if (!(wrappers.mAdapter == null || wrappers.mAdapter.mCapturedFinishCallback == null)) {
                    wrappers.mAdapter.mCapturedFinishCallback.onAnimationFinished(wrappers.mAdapter);
                }
                if (!(wrappers.mThumbnailAdapter == null || wrappers.mThumbnailAdapter.mCapturedFinishCallback == null)) {
                    wrappers.mThumbnailAdapter.mCapturedFinishCallback.onAnimationFinished(wrappers.mThumbnailAdapter);
                }
                this.mPendingAnimations.remove(i);
            }
        }
        return (RemoteAnimationTarget[]) targets.toArray(new RemoteAnimationTarget[targets.size()]);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAnimationFinished() {
        if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
            Slog.d(TAG, "onAnimationFinished(): mPendingAnimations=" + this.mPendingAnimations.size());
        }
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                unlinkToDeathOfRunner();
                releaseFinishedCallback();
                this.mService.openSurfaceTransaction();
                try {
                    Slog.d(TAG, "onAnimationFinished(): Notify animation finished:");
                    if (!this.mService.isInWallpaperEffect()) {
                        this.mService.setLauncherVisibleInFingerBoost(true, true);
                    }
                    for (int i = this.mPendingAnimations.size() - 1; i >= 0; i--) {
                        RemoteAnimationRecord adapters = this.mPendingAnimations.get(i);
                        if (adapters.mAdapter != null) {
                            adapters.mAdapter.mCapturedFinishCallback.onAnimationFinished(adapters.mAdapter);
                        }
                        if (adapters.mThumbnailAdapter != null) {
                            adapters.mThumbnailAdapter.mCapturedFinishCallback.onAnimationFinished(adapters.mThumbnailAdapter);
                        }
                        if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                            Slog.d(TAG, "\t" + adapters.mAppWindowToken);
                        }
                    }
                    this.mService.closeSurfaceTransaction("RemoteAnimationController#finished");
                } catch (Exception e) {
                    Slog.e(TAG, "Failed to finish remote animation", e);
                    throw e;
                } catch (Throwable th) {
                    this.mService.closeSurfaceTransaction("RemoteAnimationController#finished");
                    throw th;
                }
            } catch (Throwable th2) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th2;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        sendRunningRemoteAnimation(false);
        if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
            Slog.i(TAG, "Finishing remote animation");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void invokeAnimationCancelled() {
        try {
            this.mRemoteAnimationAdapter.getRunner().onAnimationCancelled();
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to notify cancel", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void releaseFinishedCallback() {
        FinishedCallback finishedCallback = this.mFinishedCallback;
        if (finishedCallback != null) {
            finishedCallback.release();
            this.mFinishedCallback = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRunningRemoteAnimation(boolean running) {
        int pid = this.mRemoteAnimationAdapter.getCallingPid();
        if (pid != 0) {
            this.mService.sendSetRunningRemoteAnimation(pid, running);
            return;
        }
        throw new RuntimeException("Calling pid of remote animation was null");
    }

    private void linkToDeathOfRunner() throws RemoteException {
        if (!this.mLinkedToDeathOfRunner) {
            this.mRemoteAnimationAdapter.getRunner().asBinder().linkToDeath(this, 0);
            this.mLinkedToDeathOfRunner = true;
        }
    }

    private void unlinkToDeathOfRunner() {
        if (this.mLinkedToDeathOfRunner) {
            this.mRemoteAnimationAdapter.getRunner().asBinder().unlinkToDeath(this, 0);
            this.mLinkedToDeathOfRunner = false;
        }
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        cancelAnimation("binderDied");
    }

    /* access modifiers changed from: private */
    public static final class FinishedCallback extends IRemoteAnimationFinishedCallback.Stub {
        RemoteAnimationController mOuter;
        private final WindowManagerService mService;

        FinishedCallback(RemoteAnimationController outer) {
            this.mOuter = outer;
            this.mService = outer.mService;
        }

        /* JADX INFO: finally extract failed */
        public void onAnimationFinished() throws RemoteException {
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        Slog.d(RemoteAnimationController.TAG, "app-onAnimationFinished(): mOuter=" + this.mOuter);
                        if (this.mOuter != null) {
                            this.mOuter.onAnimationFinished();
                            this.mOuter = null;
                        }
                    } catch (Throwable th) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                        Slog.d(RemoteAnimationController.TAG, "app-release(): mOuter=" + this.mOuter);
                    }
                    this.mOuter = null;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public class RemoteAnimationRecord {
        RemoteAnimationAdapterWrapper mAdapter;
        final AppWindowToken mAppWindowToken;
        final Rect mStartBounds;
        RemoteAnimationTarget mTarget;
        RemoteAnimationAdapterWrapper mThumbnailAdapter = null;

        RemoteAnimationRecord(AppWindowToken appWindowToken, Point endPos, Rect endBounds, Rect startBounds) {
            this.mAppWindowToken = appWindowToken;
            this.mAdapter = new RemoteAnimationAdapterWrapper(this, endPos, endBounds);
            if (startBounds != null) {
                this.mStartBounds = new Rect(startBounds);
                RemoteAnimationController.this.mTmpRect.set(startBounds);
                RemoteAnimationController.this.mTmpRect.offsetTo(0, 0);
                if (RemoteAnimationController.this.mRemoteAnimationAdapter.getChangeNeedsSnapshot()) {
                    this.mThumbnailAdapter = new RemoteAnimationAdapterWrapper(this, new Point(0, 0), RemoteAnimationController.this.mTmpRect);
                    return;
                }
                return;
            }
            this.mStartBounds = null;
        }

        /* access modifiers changed from: package-private */
        public RemoteAnimationTarget createRemoteAnimationTarget() {
            RemoteAnimationAdapterWrapper remoteAnimationAdapterWrapper;
            Task task = this.mAppWindowToken.getTask();
            WindowState mainWindow = this.mAppWindowToken.findMainWindow();
            SurfaceControl surfaceControl = null;
            if (task != null && mainWindow != null && (remoteAnimationAdapterWrapper = this.mAdapter) != null) {
                if (remoteAnimationAdapterWrapper.mCapturedFinishCallback != null) {
                    if (this.mAdapter.mCapturedLeash != null) {
                        Rect insets = new Rect();
                        mainWindow.getContentInsets(insets);
                        if (HwFrameworkFactory.getHwApsImpl() != null) {
                            HwFrameworkFactory.getHwApsImpl().scaleInsetsWhenSdrUpInRog(mainWindow.getOwningPackage(), insets);
                        }
                        InsetUtils.addInsets(insets, this.mAppWindowToken.getLetterboxInsets());
                        int i = task.mTaskId;
                        int mode = getMode();
                        SurfaceControl surfaceControl2 = this.mAdapter.mCapturedLeash;
                        boolean z = !this.mAppWindowToken.fillsParent();
                        Rect rect = mainWindow.mWinAnimator.mLastClipRect;
                        int prefixOrderIndex = this.mAppWindowToken.getPrefixOrderIndex();
                        Point point = this.mAdapter.mPosition;
                        Rect rect2 = this.mAdapter.mStackBounds;
                        WindowConfiguration windowConfiguration = task.getWindowConfiguration();
                        RemoteAnimationAdapterWrapper remoteAnimationAdapterWrapper2 = this.mThumbnailAdapter;
                        if (remoteAnimationAdapterWrapper2 != null) {
                            surfaceControl = remoteAnimationAdapterWrapper2.mCapturedLeash;
                        }
                        this.mTarget = new RemoteAnimationTarget(i, mode, surfaceControl2, z, rect, insets, prefixOrderIndex, point, rect2, windowConfiguration, false, surfaceControl, this.mStartBounds);
                        return this.mTarget;
                    }
                }
            }
            return null;
        }

        private int getMode() {
            DisplayContent dc = this.mAppWindowToken.getDisplayContent();
            if (dc.mOpeningApps.contains(this.mAppWindowToken)) {
                return 0;
            }
            if (dc.mChangingApps.contains(this.mAppWindowToken)) {
                return 2;
            }
            return 1;
        }
    }

    /* access modifiers changed from: private */
    public class RemoteAnimationAdapterWrapper implements AnimationAdapter {
        private SurfaceAnimator.OnAnimationFinishedCallback mCapturedFinishCallback;
        SurfaceControl mCapturedLeash;
        private final Point mPosition = new Point();
        private final RemoteAnimationRecord mRecord;
        private final Rect mStackBounds = new Rect();

        RemoteAnimationAdapterWrapper(RemoteAnimationRecord record, Point position, Rect stackBounds) {
            this.mRecord = record;
            this.mPosition.set(position.x, position.y);
            this.mStackBounds.set(stackBounds);
        }

        @Override // com.android.server.wm.AnimationAdapter
        public boolean getShowWallpaper() {
            return false;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public int getBackgroundColor() {
            return 0;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void startAnimation(SurfaceControl animationLeash, SurfaceControl.Transaction t, SurfaceAnimator.OnAnimationFinishedCallback finishCallback) {
            if (WindowManagerDebugConfig.DEBUG_REMOTE_ANIMATIONS) {
                Slog.d(RemoteAnimationController.TAG, "startAnimation");
            }
            t.setLayer(animationLeash, this.mRecord.mAppWindowToken.getPrefixOrderIndex());
            t.setPosition(animationLeash, (float) this.mPosition.x, (float) this.mPosition.y);
            RemoteAnimationController.this.mTmpRect.set(this.mStackBounds);
            RemoteAnimationController.this.mTmpRect.offsetTo(0, 0);
            t.setWindowCrop(animationLeash, RemoteAnimationController.this.mTmpRect);
            this.mCapturedLeash = animationLeash;
            this.mCapturedFinishCallback = finishCallback;
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void onAnimationCancelled(SurfaceControl animationLeash) {
            if (this.mRecord.mAdapter == this) {
                this.mRecord.mAdapter = null;
            } else {
                this.mRecord.mThumbnailAdapter = null;
            }
            if (this.mRecord.mAdapter == null && this.mRecord.mThumbnailAdapter == null) {
                RemoteAnimationController.this.mPendingAnimations.remove(this.mRecord);
            }
            if (RemoteAnimationController.this.mPendingAnimations.isEmpty()) {
                RemoteAnimationController.this.mHandler.removeCallbacks(RemoteAnimationController.this.mTimeoutRunnable);
                RemoteAnimationController.this.releaseFinishedCallback();
                RemoteAnimationController.this.invokeAnimationCancelled();
                RemoteAnimationController.this.sendRunningRemoteAnimation(false);
            }
        }

        @Override // com.android.server.wm.AnimationAdapter
        public long getDurationHint() {
            return RemoteAnimationController.this.mRemoteAnimationAdapter.getDuration();
        }

        @Override // com.android.server.wm.AnimationAdapter
        public long getStatusBarTransitionsStartTime() {
            return SystemClock.uptimeMillis() + RemoteAnimationController.this.mRemoteAnimationAdapter.getStatusBarTransitionDelay();
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print("token=");
            pw.println(this.mRecord.mAppWindowToken);
            if (this.mRecord.mTarget != null) {
                pw.print(prefix);
                pw.println("Target:");
                RemoteAnimationTarget remoteAnimationTarget = this.mRecord.mTarget;
                remoteAnimationTarget.dump(pw, prefix + "  ");
                return;
            }
            pw.print(prefix);
            pw.println("Target: null");
        }

        @Override // com.android.server.wm.AnimationAdapter
        public void writeToProto(ProtoOutputStream proto) {
            long token = proto.start(1146756268034L);
            if (this.mRecord.mTarget != null) {
                this.mRecord.mTarget.writeToProto(proto, 1146756268033L);
            }
            proto.end(token);
        }
    }
}

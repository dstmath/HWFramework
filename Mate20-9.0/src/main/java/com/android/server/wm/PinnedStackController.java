package com.android.server.wm;

import android.app.RemoteAction;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.Slog;
import android.util.TypedValue;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import com.android.internal.policy.PipSnapAlgorithm;
import com.android.server.UiThread;
import com.android.server.wm.PinnedStackController;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class PinnedStackController {
    public static final float INVALID_SNAP_FRACTION = -1.0f;
    private static final String TAG = "WindowManager";
    private ArrayList<RemoteAction> mActions = new ArrayList<>();
    private float mAspectRatio = -1.0f;
    private final PinnedStackControllerCallback mCallbacks = new PinnedStackControllerCallback();
    /* access modifiers changed from: private */
    public int mCurrentMinSize;
    private float mDefaultAspectRatio;
    /* access modifiers changed from: private */
    public int mDefaultMinSize;
    private int mDefaultStackGravity;
    private final DisplayContent mDisplayContent;
    /* access modifiers changed from: private */
    public final DisplayInfo mDisplayInfo = new DisplayInfo();
    /* access modifiers changed from: private */
    public final Handler mHandler = UiThread.getHandler();
    private int mImeHeight;
    private boolean mIsImeShowing;
    /* access modifiers changed from: private */
    public boolean mIsMinimized;
    private boolean mIsShelfShowing;
    private WeakReference<AppWindowToken> mLastPipActivity = null;
    private float mMaxAspectRatio;
    private float mMinAspectRatio;
    /* access modifiers changed from: private */
    public IPinnedStackListener mPinnedStackListener;
    /* access modifiers changed from: private */
    public final PinnedStackListenerDeathHandler mPinnedStackListenerDeathHandler = new PinnedStackListenerDeathHandler();
    private float mReentrySnapFraction = -1.0f;
    private Point mScreenEdgeInsets;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    private int mShelfHeight;
    /* access modifiers changed from: private */
    public final PipSnapAlgorithm mSnapAlgorithm;
    private final Rect mStableInsets = new Rect();
    private final Rect mTmpAnimatingBoundsRect = new Rect();
    private final Point mTmpDisplaySize = new Point();
    private final Rect mTmpInsets = new Rect();
    private final DisplayMetrics mTmpMetrics = new DisplayMetrics();
    private final Rect mTmpRect = new Rect();

    private class PinnedStackControllerCallback extends IPinnedStackController.Stub {
        private PinnedStackControllerCallback() {
        }

        public void setIsMinimized(boolean isMinimized) {
            PinnedStackController.this.mHandler.post(new Runnable(isMinimized) {
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PinnedStackController.PinnedStackControllerCallback.lambda$setIsMinimized$0(PinnedStackController.PinnedStackControllerCallback.this, this.f$1);
                }
            });
        }

        public static /* synthetic */ void lambda$setIsMinimized$0(PinnedStackControllerCallback pinnedStackControllerCallback, boolean isMinimized) {
            boolean unused = PinnedStackController.this.mIsMinimized = isMinimized;
            PinnedStackController.this.mSnapAlgorithm.setMinimized(isMinimized);
        }

        public void setMinEdgeSize(int minEdgeSize) {
            PinnedStackController.this.mHandler.post(new Runnable(minEdgeSize) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    int unused = PinnedStackController.this.mCurrentMinSize = Math.max(PinnedStackController.this.mDefaultMinSize, this.f$1);
                }
            });
        }

        public int getDisplayRotation() {
            int i;
            synchronized (PinnedStackController.this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    i = PinnedStackController.this.mDisplayInfo.rotation;
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
    }

    private class PinnedStackListenerDeathHandler implements IBinder.DeathRecipient {
        private PinnedStackListenerDeathHandler() {
        }

        public void binderDied() {
            if (PinnedStackController.this.mPinnedStackListener != null) {
                PinnedStackController.this.mPinnedStackListener.asBinder().unlinkToDeath(PinnedStackController.this.mPinnedStackListenerDeathHandler, 0);
            }
            IPinnedStackListener unused = PinnedStackController.this.mPinnedStackListener = null;
        }
    }

    PinnedStackController(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
        this.mSnapAlgorithm = new PipSnapAlgorithm(service.mContext);
        this.mDisplayInfo.copyFrom(this.mDisplayContent.getDisplayInfo());
        reloadResources();
        this.mAspectRatio = this.mDefaultAspectRatio;
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        reloadResources();
    }

    private void reloadResources() {
        Size screenEdgeInsetsDp;
        Point point;
        Resources res = this.mService.mContext.getResources();
        this.mDefaultMinSize = res.getDimensionPixelSize(17105016);
        this.mCurrentMinSize = this.mDefaultMinSize;
        this.mDefaultAspectRatio = res.getFloat(17104968);
        String screenEdgeInsetsDpString = res.getString(17039790);
        if (!screenEdgeInsetsDpString.isEmpty()) {
            screenEdgeInsetsDp = Size.parseSize(screenEdgeInsetsDpString);
        } else {
            screenEdgeInsetsDp = null;
        }
        this.mDefaultStackGravity = res.getInteger(17694772);
        this.mDisplayContent.getDisplay().getRealMetrics(this.mTmpMetrics);
        if (screenEdgeInsetsDp == null) {
            point = new Point();
        } else {
            point = new Point(dpToPx((float) screenEdgeInsetsDp.getWidth(), this.mTmpMetrics), dpToPx((float) screenEdgeInsetsDp.getHeight(), this.mTmpMetrics));
        }
        this.mScreenEdgeInsets = point;
        this.mMinAspectRatio = res.getFloat(17104971);
        this.mMaxAspectRatio = res.getFloat(17104970);
    }

    /* access modifiers changed from: package-private */
    public void registerPinnedStackListener(IPinnedStackListener listener) {
        try {
            listener.asBinder().linkToDeath(this.mPinnedStackListenerDeathHandler, 0);
            listener.onListenerRegistered(this.mCallbacks);
            this.mPinnedStackListener = listener;
            notifyImeVisibilityChanged(this.mIsImeShowing, this.mImeHeight);
            notifyShelfVisibilityChanged(this.mIsShelfShowing, this.mShelfHeight);
            notifyMovementBoundsChanged(false, false);
            notifyActionsChanged(this.mActions);
            notifyMinimizeChanged(this.mIsMinimized);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register pinned stack listener", e);
        }
    }

    public boolean isValidPictureInPictureAspectRatio(float aspectRatio) {
        return Float.compare(this.mMinAspectRatio, aspectRatio) <= 0 && Float.compare(aspectRatio, this.mMaxAspectRatio) <= 0;
    }

    /* access modifiers changed from: package-private */
    public Rect transformBoundsToAspectRatio(Rect stackBounds, float aspectRatio, boolean useCurrentMinEdgeSize) {
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(stackBounds, getMovementBounds(stackBounds));
        Size size = this.mSnapAlgorithm.getSizeForAspectRatio(aspectRatio, (float) (useCurrentMinEdgeSize ? this.mCurrentMinSize : this.mDefaultMinSize), this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight);
        int left = (int) (((float) stackBounds.centerX()) - (((float) size.getWidth()) / 2.0f));
        int top = (int) (((float) stackBounds.centerY()) - (((float) size.getHeight()) / 2.0f));
        stackBounds.set(left, top, size.getWidth() + left, size.getHeight() + top);
        this.mSnapAlgorithm.applySnapFraction(stackBounds, getMovementBounds(stackBounds), snapFraction);
        if (this.mIsMinimized) {
            applyMinimizedOffset(stackBounds, getMovementBounds(stackBounds));
        }
        return stackBounds;
    }

    /* access modifiers changed from: package-private */
    public void saveReentrySnapFraction(AppWindowToken token, Rect stackBounds) {
        this.mReentrySnapFraction = getSnapFraction(stackBounds);
        this.mLastPipActivity = new WeakReference<>(token);
    }

    /* access modifiers changed from: package-private */
    public void resetReentrySnapFraction(AppWindowToken token) {
        if (this.mLastPipActivity != null && this.mLastPipActivity.get() == token) {
            this.mReentrySnapFraction = -1.0f;
            this.mLastPipActivity = null;
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getDefaultOrLastSavedBounds() {
        return getDefaultBounds(this.mReentrySnapFraction);
    }

    /* access modifiers changed from: package-private */
    public Rect getDefaultBounds(float snapFraction) {
        Rect defaultBounds;
        int i;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Rect insetBounds = new Rect();
                getInsetBounds(insetBounds);
                defaultBounds = new Rect();
                Size size = this.mSnapAlgorithm.getSizeForAspectRatio(this.mDefaultAspectRatio, (float) this.mDefaultMinSize, this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight);
                int i2 = 0;
                if (snapFraction != -1.0f) {
                    defaultBounds.set(0, 0, size.getWidth(), size.getHeight());
                    this.mSnapAlgorithm.applySnapFraction(defaultBounds, getMovementBounds(defaultBounds), snapFraction);
                } else {
                    int i3 = this.mDefaultStackGravity;
                    int width = size.getWidth();
                    int height = size.getHeight();
                    if (this.mIsImeShowing) {
                        i = this.mImeHeight;
                    } else {
                        i = 0;
                    }
                    if (this.mIsShelfShowing) {
                        i2 = this.mShelfHeight;
                    }
                    Gravity.apply(i3, width, height, insetBounds, 0, Math.max(i, i2), defaultBounds);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return defaultBounds;
    }

    /* access modifiers changed from: package-private */
    public synchronized void onDisplayInfoChanged() {
        this.mDisplayInfo.copyFrom(this.mDisplayContent.getDisplayInfo());
        notifyMovementBoundsChanged(false, false);
    }

    /* access modifiers changed from: package-private */
    public boolean onTaskStackBoundsChanged(Rect targetBounds, Rect outBounds) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
                if (this.mDisplayInfo.equals(displayInfo)) {
                    outBounds.setEmpty();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else if (targetBounds.isEmpty()) {
                    this.mDisplayInfo.copyFrom(displayInfo);
                    outBounds.setEmpty();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else {
                    this.mTmpRect.set(targetBounds);
                    Rect postChangeStackBounds = this.mTmpRect;
                    float snapFraction = getSnapFraction(postChangeStackBounds);
                    this.mDisplayInfo.copyFrom(displayInfo);
                    Rect postChangeMovementBounds = getMovementBounds(postChangeStackBounds, false, false);
                    this.mSnapAlgorithm.applySnapFraction(postChangeStackBounds, postChangeMovementBounds, snapFraction);
                    if (this.mIsMinimized) {
                        applyMinimizedOffset(postChangeStackBounds, postChangeMovementBounds);
                    }
                    notifyMovementBoundsChanged(false, false);
                    outBounds.set(postChangeStackBounds);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return true;
                }
            } catch (Throwable displayInfo2) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw displayInfo2;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAdjustedForIme(boolean adjustedForIme, int imeHeight) {
        boolean imeShowing = adjustedForIme && imeHeight > 0;
        int imeHeight2 = imeShowing ? imeHeight : 0;
        if (imeShowing != this.mIsImeShowing || imeHeight2 != this.mImeHeight) {
            this.mIsImeShowing = imeShowing;
            this.mImeHeight = imeHeight2;
            notifyImeVisibilityChanged(imeShowing, imeHeight2);
            notifyMovementBoundsChanged(true, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAdjustedForShelf(boolean adjustedForShelf, int shelfHeight) {
        boolean shelfShowing = adjustedForShelf && shelfHeight > 0;
        if (shelfShowing != this.mIsShelfShowing || shelfHeight != this.mShelfHeight) {
            this.mIsShelfShowing = shelfShowing;
            this.mShelfHeight = shelfHeight;
            notifyShelfVisibilityChanged(shelfShowing, shelfHeight);
            notifyMovementBoundsChanged(false, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAspectRatio(float aspectRatio) {
        if (Float.compare(this.mAspectRatio, aspectRatio) != 0) {
            this.mAspectRatio = aspectRatio;
            notifyMovementBoundsChanged(false, false);
        }
    }

    /* access modifiers changed from: package-private */
    public float getAspectRatio() {
        return this.mAspectRatio;
    }

    /* access modifiers changed from: package-private */
    public void setActions(List<RemoteAction> actions) {
        this.mActions.clear();
        if (actions != null) {
            this.mActions.addAll(actions);
        }
        notifyActionsChanged(this.mActions);
    }

    private void notifyImeVisibilityChanged(boolean imeVisible, int imeHeight) {
        if (this.mPinnedStackListener != null) {
            try {
                this.mPinnedStackListener.onImeVisibilityChanged(imeVisible, imeHeight);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error delivering bounds changed event.", e);
            }
        }
    }

    private void notifyShelfVisibilityChanged(boolean shelfVisible, int shelfHeight) {
        if (this.mPinnedStackListener != null) {
            try {
                this.mPinnedStackListener.onShelfVisibilityChanged(shelfVisible, shelfHeight);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error delivering bounds changed event.", e);
            }
        }
    }

    private void notifyMinimizeChanged(boolean isMinimized) {
        if (this.mPinnedStackListener != null) {
            try {
                this.mPinnedStackListener.onMinimizedStateChanged(isMinimized);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error delivering minimize changed event.", e);
            }
        }
    }

    private void notifyActionsChanged(List<RemoteAction> actions) {
        if (this.mPinnedStackListener != null) {
            try {
                this.mPinnedStackListener.onActionsChanged(new ParceledListSlice(actions));
            } catch (RemoteException e) {
                Slog.e(TAG, "Error delivering actions changed event.", e);
            }
        }
    }

    private void notifyMovementBoundsChanged(boolean fromImeAdjustment, boolean fromShelfAdjustment) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mPinnedStackListener == null) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return;
                }
                Rect insetBounds = new Rect();
                getInsetBounds(insetBounds);
                Rect normalBounds = getDefaultBounds(-1.0f);
                if (isValidPictureInPictureAspectRatio(this.mAspectRatio)) {
                    transformBoundsToAspectRatio(normalBounds, this.mAspectRatio, false);
                }
                Rect animatingBounds = this.mTmpAnimatingBoundsRect;
                TaskStack pinnedStack = this.mDisplayContent.getPinnedStack();
                if (pinnedStack != null) {
                    pinnedStack.getAnimationOrCurrentBounds(animatingBounds);
                } else {
                    animatingBounds.set(normalBounds);
                }
                this.mPinnedStackListener.onMovementBoundsChanged(insetBounds, normalBounds, animatingBounds, fromImeAdjustment, fromShelfAdjustment, this.mDisplayInfo.rotation);
            } catch (RemoteException e) {
                Slog.e(TAG, "Error delivering actions changed event.", e);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    private void getInsetBounds(Rect outRect) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mPolicy.getStableInsetsLw(this.mDisplayInfo.rotation, this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mDisplayInfo.displayCutout, this.mTmpInsets);
                outRect.set(this.mTmpInsets.left + this.mScreenEdgeInsets.x, this.mTmpInsets.top + this.mScreenEdgeInsets.y, (this.mDisplayInfo.logicalWidth - this.mTmpInsets.right) - this.mScreenEdgeInsets.x, (this.mDisplayInfo.logicalHeight - this.mTmpInsets.bottom) - this.mScreenEdgeInsets.y);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    private Rect getMovementBounds(Rect stackBounds) {
        Rect movementBounds;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                movementBounds = getMovementBounds(stackBounds, true, true);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return movementBounds;
    }

    private Rect getMovementBounds(Rect stackBounds, boolean adjustForIme, boolean adjustForShelf) {
        Rect movementBounds;
        int i;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                movementBounds = new Rect();
                getInsetBounds(movementBounds);
                PipSnapAlgorithm pipSnapAlgorithm = this.mSnapAlgorithm;
                int i2 = 0;
                if (!adjustForIme || !this.mIsImeShowing) {
                    i = 0;
                } else {
                    i = this.mImeHeight;
                }
                if (adjustForShelf && this.mIsShelfShowing) {
                    i2 = this.mShelfHeight;
                }
                pipSnapAlgorithm.getMovementBounds(stackBounds, movementBounds, movementBounds, Math.max(i, i2));
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return movementBounds;
    }

    private void applyMinimizedOffset(Rect stackBounds, Rect movementBounds) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mTmpDisplaySize.set(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight);
                this.mService.getStableInsetsLocked(this.mDisplayContent.getDisplayId(), this.mStableInsets);
                this.mSnapAlgorithm.applyMinimizedOffset(stackBounds, movementBounds, this.mTmpDisplaySize, this.mStableInsets);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    private float getSnapFraction(Rect stackBounds) {
        return this.mSnapAlgorithm.getSnapFraction(stackBounds, getMovementBounds(stackBounds));
    }

    private int dpToPx(float dpValue, DisplayMetrics dm) {
        return (int) TypedValue.applyDimension(1, dpValue, dm);
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "PinnedStackController");
        pw.print(prefix + "  defaultBounds=");
        getDefaultBounds(-1.0f).printShortString(pw);
        pw.println();
        this.mService.getStackBounds(2, 1, this.mTmpRect);
        pw.print(prefix + "  movementBounds=");
        getMovementBounds(this.mTmpRect).printShortString(pw);
        pw.println();
        pw.println(prefix + "  mIsImeShowing=" + this.mIsImeShowing);
        pw.println(prefix + "  mImeHeight=" + this.mImeHeight);
        pw.println(prefix + "  mIsShelfShowing=" + this.mIsShelfShowing);
        pw.println(prefix + "  mShelfHeight=" + this.mShelfHeight);
        pw.println(prefix + "  mReentrySnapFraction=" + this.mReentrySnapFraction);
        pw.println(prefix + "  mIsMinimized=" + this.mIsMinimized);
        if (this.mActions.isEmpty()) {
            pw.println(prefix + "  mActions=[]");
        } else {
            pw.println(prefix + "  mActions=[");
            for (int i = 0; i < this.mActions.size(); i++) {
                pw.print(prefix + "    Action[" + i + "]: ");
                this.mActions.get(i).dump(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, pw);
            }
            pw.println(prefix + "  ]");
        }
        pw.println(prefix + " mDisplayInfo=" + this.mDisplayInfo);
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        getDefaultBounds(-1.0f).writeToProto(proto, 1146756268033L);
        this.mService.getStackBounds(2, 1, this.mTmpRect);
        getMovementBounds(this.mTmpRect).writeToProto(proto, 1146756268034L);
        proto.end(token);
    }
}

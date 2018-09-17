package com.android.server.wm;

import android.app.RemoteAction;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder.DeathRecipient;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.Slog;
import android.util.TypedValue;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IPinnedStackController.Stub;
import android.view.IPinnedStackListener;
import com.android.internal.policy.PipSnapAlgorithm;
import com.android.server.UiThread;
import com.android.server.wm.-$Lambda$JE-Xd_mgkfFanNxg9Cy6vl62umY.AnonymousClass1;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class PinnedStackController {
    private static final String TAG = "WindowManager";
    private ArrayList<RemoteAction> mActions = new ArrayList();
    private float mAspectRatio = -1.0f;
    private final PinnedStackControllerCallback mCallbacks = new PinnedStackControllerCallback(this, null);
    private int mCurrentMinSize;
    private float mDefaultAspectRatio;
    private int mDefaultMinSize;
    private int mDefaultStackGravity;
    private final DisplayContent mDisplayContent;
    private final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final Handler mHandler = UiThread.getHandler();
    private int mImeHeight;
    private boolean mIsImeShowing;
    private boolean mIsMinimized;
    private float mMaxAspectRatio;
    private float mMinAspectRatio;
    private IPinnedStackListener mPinnedStackListener;
    private final PinnedStackListenerDeathHandler mPinnedStackListenerDeathHandler = new PinnedStackListenerDeathHandler(this, null);
    private Point mScreenEdgeInsets;
    private final WindowManagerService mService;
    private final PipSnapAlgorithm mSnapAlgorithm;
    private final Rect mStableInsets = new Rect();
    private final Rect mTmpAnimatingBoundsRect = new Rect();
    private final Point mTmpDisplaySize = new Point();
    private final Rect mTmpInsets = new Rect();
    private final DisplayMetrics mTmpMetrics = new DisplayMetrics();
    private final Rect mTmpRect = new Rect();

    private class PinnedStackControllerCallback extends Stub {
        /* synthetic */ PinnedStackControllerCallback(PinnedStackController this$0, PinnedStackControllerCallback -this1) {
            this();
        }

        private PinnedStackControllerCallback() {
        }

        public void setIsMinimized(boolean isMinimized) {
            PinnedStackController.this.mHandler.post(new AnonymousClass1(isMinimized, this));
        }

        /* synthetic */ void lambda$-com_android_server_wm_PinnedStackController$PinnedStackControllerCallback_4738(boolean isMinimized) {
            PinnedStackController.this.mIsMinimized = isMinimized;
            PinnedStackController.this.mSnapAlgorithm.setMinimized(isMinimized);
        }

        public void setMinEdgeSize(int minEdgeSize) {
            PinnedStackController.this.mHandler.post(new -$Lambda$JE-Xd_mgkfFanNxg9Cy6vl62umY(minEdgeSize, this));
        }

        /* synthetic */ void lambda$-com_android_server_wm_PinnedStackController$PinnedStackControllerCallback_4973(int minEdgeSize) {
            PinnedStackController.this.mCurrentMinSize = Math.max(PinnedStackController.this.mDefaultMinSize, minEdgeSize);
        }

        public int getDisplayRotation() {
            int i;
            synchronized (PinnedStackController.this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    i = PinnedStackController.this.mDisplayInfo.rotation;
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            return i;
        }
    }

    private class PinnedStackListenerDeathHandler implements DeathRecipient {
        /* synthetic */ PinnedStackListenerDeathHandler(PinnedStackController this$0, PinnedStackListenerDeathHandler -this1) {
            this();
        }

        private PinnedStackListenerDeathHandler() {
        }

        public void binderDied() {
            PinnedStackController.this.mPinnedStackListener = null;
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

    void onConfigurationChanged() {
        reloadResources();
    }

    private void reloadResources() {
        Size screenEdgeInsetsDp;
        Point point;
        Resources res = this.mService.mContext.getResources();
        this.mDefaultMinSize = res.getDimensionPixelSize(17104997);
        this.mCurrentMinSize = this.mDefaultMinSize;
        this.mDefaultAspectRatio = res.getFloat(17104953);
        String screenEdgeInsetsDpString = res.getString(17039772);
        if (screenEdgeInsetsDpString.isEmpty()) {
            screenEdgeInsetsDp = null;
        } else {
            screenEdgeInsetsDp = Size.parseSize(screenEdgeInsetsDpString);
        }
        this.mDefaultStackGravity = res.getInteger(17694771);
        this.mDisplayContent.getDisplay().getRealMetrics(this.mTmpMetrics);
        if (screenEdgeInsetsDp == null) {
            point = new Point();
        } else {
            point = new Point(dpToPx((float) screenEdgeInsetsDp.getWidth(), this.mTmpMetrics), dpToPx((float) screenEdgeInsetsDp.getHeight(), this.mTmpMetrics));
        }
        this.mScreenEdgeInsets = point;
        this.mMinAspectRatio = res.getFloat(17104956);
        this.mMaxAspectRatio = res.getFloat(17104955);
    }

    void registerPinnedStackListener(IPinnedStackListener listener) {
        try {
            listener.asBinder().linkToDeath(this.mPinnedStackListenerDeathHandler, 0);
            listener.onListenerRegistered(this.mCallbacks);
            this.mPinnedStackListener = listener;
            notifyImeVisibilityChanged(this.mIsImeShowing, this.mImeHeight);
            notifyMovementBoundsChanged(false);
            notifyActionsChanged(this.mActions);
            notifyMinimizeChanged(this.mIsMinimized);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register pinned stack listener", e);
        }
    }

    public boolean isValidPictureInPictureAspectRatio(float aspectRatio) {
        if (Float.compare(this.mMinAspectRatio, aspectRatio) > 0 || Float.compare(aspectRatio, this.mMaxAspectRatio) > 0) {
            return false;
        }
        return true;
    }

    Rect transformBoundsToAspectRatio(Rect stackBounds, float aspectRatio, boolean useCurrentMinEdgeSize) {
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

    Rect getDefaultBounds() {
        Rect defaultBounds;
        int i = 0;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                Rect insetBounds = new Rect();
                getInsetBounds(insetBounds);
                defaultBounds = new Rect();
                Size size = this.mSnapAlgorithm.getSizeForAspectRatio(this.mDefaultAspectRatio, (float) this.mDefaultMinSize, this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight);
                int i2 = this.mDefaultStackGravity;
                int width = size.getWidth();
                int height = size.getHeight();
                if (this.mIsImeShowing) {
                    i = this.mImeHeight;
                }
                Gravity.apply(i2, width, height, insetBounds, 0, i, defaultBounds);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return defaultBounds;
    }

    synchronized void onDisplayInfoChanged() {
        this.mDisplayInfo.copyFrom(this.mDisplayContent.getDisplayInfo());
        notifyMovementBoundsChanged(false);
    }

    boolean onTaskStackBoundsChanged(Rect targetBounds, Rect outBounds) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                DisplayInfo displayInfo = this.mDisplayContent.getDisplayInfo();
                if (this.mDisplayInfo.equals(displayInfo)) {
                    outBounds.setEmpty();
                } else if (targetBounds.isEmpty()) {
                    this.mDisplayInfo.copyFrom(displayInfo);
                    outBounds.setEmpty();
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return false;
                } else {
                    this.mTmpRect.set(targetBounds);
                    Rect postChangeStackBounds = this.mTmpRect;
                    float snapFraction = this.mSnapAlgorithm.getSnapFraction(postChangeStackBounds, getMovementBounds(postChangeStackBounds));
                    this.mDisplayInfo.copyFrom(displayInfo);
                    Rect postChangeMovementBounds = getMovementBounds(postChangeStackBounds, false);
                    this.mSnapAlgorithm.applySnapFraction(postChangeStackBounds, postChangeMovementBounds, snapFraction);
                    if (this.mIsMinimized) {
                        applyMinimizedOffset(postChangeStackBounds, postChangeMovementBounds);
                    }
                    notifyMovementBoundsChanged(false);
                    outBounds.set(postChangeStackBounds);
                    WindowManagerService.resetPriorityAfterLockedSection();
                    return true;
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return false;
    }

    void setAdjustedForIme(boolean adjustedForIme, int imeHeight) {
        if (this.mIsImeShowing != adjustedForIme || this.mImeHeight != imeHeight) {
            this.mIsImeShowing = adjustedForIme;
            this.mImeHeight = imeHeight;
            notifyImeVisibilityChanged(adjustedForIme, imeHeight);
            notifyMovementBoundsChanged(true);
        }
    }

    void setAspectRatio(float aspectRatio) {
        if (Float.compare(this.mAspectRatio, aspectRatio) != 0) {
            this.mAspectRatio = aspectRatio;
            notifyMovementBoundsChanged(false);
        }
    }

    float getAspectRatio() {
        return this.mAspectRatio;
    }

    void setActions(List<RemoteAction> actions) {
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

    private void notifyMovementBoundsChanged(boolean fromImeAdjustement) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mPinnedStackListener != null) {
                    Rect insetBounds = new Rect();
                    getInsetBounds(insetBounds);
                    Rect normalBounds = getDefaultBounds();
                    if (isValidPictureInPictureAspectRatio(this.mAspectRatio)) {
                        transformBoundsToAspectRatio(normalBounds, this.mAspectRatio, false);
                    }
                    Rect animatingBounds = this.mTmpAnimatingBoundsRect;
                    TaskStack pinnedStack = this.mDisplayContent.getStackById(4);
                    if (pinnedStack != null) {
                        pinnedStack.getAnimationOrCurrentBounds(animatingBounds);
                    } else {
                        animatingBounds.set(normalBounds);
                    }
                    this.mPinnedStackListener.onMovementBoundsChanged(insetBounds, normalBounds, animatingBounds, fromImeAdjustement, this.mDisplayInfo.rotation);
                }
            } catch (RemoteException e) {
                Slog.e(TAG, "Error delivering actions changed event.", e);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    private void getInsetBounds(Rect outRect) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mPolicy.getStableInsetsLw(this.mDisplayInfo.rotation, this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mTmpInsets);
                outRect.set(this.mTmpInsets.left + this.mScreenEdgeInsets.x, this.mTmpInsets.top + this.mScreenEdgeInsets.y, (this.mDisplayInfo.logicalWidth - this.mTmpInsets.right) - this.mScreenEdgeInsets.x, (this.mDisplayInfo.logicalHeight - this.mTmpInsets.bottom) - this.mScreenEdgeInsets.y);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private Rect getMovementBounds(Rect stackBounds) {
        Rect movementBounds;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                movementBounds = getMovementBounds(stackBounds, true);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return movementBounds;
    }

    private Rect getMovementBounds(Rect stackBounds, boolean adjustForIme) {
        Rect movementBounds;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                movementBounds = new Rect();
                getInsetBounds(movementBounds);
                PipSnapAlgorithm pipSnapAlgorithm = this.mSnapAlgorithm;
                int i = (adjustForIme && this.mIsImeShowing) ? this.mImeHeight : 0;
                pipSnapAlgorithm.getMovementBounds(stackBounds, movementBounds, movementBounds, i);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return movementBounds;
    }

    private void applyMinimizedOffset(Rect stackBounds, Rect movementBounds) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mTmpDisplaySize.set(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight);
                this.mService.getStableInsetsLocked(this.mDisplayContent.getDisplayId(), this.mStableInsets);
                this.mSnapAlgorithm.applyMinimizedOffset(stackBounds, movementBounds, this.mTmpDisplaySize, this.mStableInsets);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private int dpToPx(float dpValue, DisplayMetrics dm) {
        return (int) TypedValue.applyDimension(1, dpValue, dm);
    }

    void dump(String prefix, PrintWriter pw) {
        pw.println(prefix + "PinnedStackController");
        pw.print(prefix + "  defaultBounds=");
        getDefaultBounds().printShortString(pw);
        pw.println();
        this.mService.getStackBounds(4, this.mTmpRect);
        pw.print(prefix + "  movementBounds=");
        getMovementBounds(this.mTmpRect).printShortString(pw);
        pw.println();
        pw.println(prefix + "  mIsImeShowing=" + this.mIsImeShowing);
        pw.println(prefix + "  mIsMinimized=" + this.mIsMinimized);
        if (this.mActions.isEmpty()) {
            pw.println(prefix + "  mActions=[]");
            return;
        }
        pw.println(prefix + "  mActions=[");
        for (int i = 0; i < this.mActions.size(); i++) {
            RemoteAction action = (RemoteAction) this.mActions.get(i);
            pw.print(prefix + "    Action[" + i + "]: ");
            action.dump("", pw);
        }
        pw.println(prefix + "  ]");
    }
}

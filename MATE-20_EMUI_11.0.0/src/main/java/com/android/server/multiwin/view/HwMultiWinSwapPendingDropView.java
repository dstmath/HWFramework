package com.android.server.multiwin.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import com.android.server.wm.HwMultiWindowManager;

public class HwMultiWinSwapPendingDropView extends HwMultiWinHotAreaView {
    private static final boolean DBG = false;
    private static final String TAG = "PendingDropView";
    private Rect mDropTargetBound;
    private boolean mHasSwapHotAreaResized = false;
    private HwMultiWinSwapAcceptView mHostSwapAcceptView;
    private HwMultiWinSwapAcceptView mHwFreeFormSwapRegion;
    private boolean mIsLeft;
    private float mLastDragX;
    private float mLastDragY;
    private Rect mSwapAcceptBound;
    private ViewGroup mSwapAcceptParent;
    private HwMultiWinHotAreaView mSwapTarget;

    public HwMultiWinSwapPendingDropView(Context context) {
        super(context);
    }

    public HwMultiWinSwapPendingDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinSwapPendingDropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwMultiWinSwapPendingDropView(Context context, boolean isLeft, Rect swapAcceptBound, int splitMode, HwMultiWinHotAreaView swapTarget) {
        super(context);
        this.mIsLeft = isLeft;
        this.mSwapAcceptBound = swapAcceptBound;
        this.mSplitMode = splitMode;
        this.mSwapTarget = swapTarget;
        this.mIsToDrawBorder = false;
    }

    public void setDropTargetBound(Rect dropTargetBound) {
        this.mDropTargetBound = new Rect(dropTargetBound);
    }

    public void setSwapAcceptParent(ViewGroup swapAcceptParent) {
        this.mSwapAcceptParent = swapAcceptParent;
    }

    public void updateSwapAcceptBound() {
        ViewGroup viewGroup = this.mSwapAcceptParent;
        if (viewGroup != null) {
            ViewParent parent = viewGroup.getParent();
            if (parent instanceof ViewGroup) {
                this.mSwapAcceptBound.bottom = ((ViewGroup) parent).getBottom();
            }
        }
    }

    public void setHostSwapAcceptView(HwMultiWinSwapAcceptView hostSwapAcceptView) {
        this.mHostSwapAcceptView = hostSwapAcceptView;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragEntered(DragEvent dragEvent, int dragSurfaceAnimType) {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragLocation(DragEvent dragEvent) {
        super.handleDragLocation(dragEvent);
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        float dragX = dragEvent.getX() + ((float) loc[0]);
        float dragY = dragEvent.getY() + ((float) loc[1]);
        if (!isLandScape()) {
            if (isDragOutOfSwapAcceptBound(dragX, dragY)) {
                removeSelf();
            } else if (isDragIntoSwapAcceptBound(dragX, dragY)) {
                addSelf();
            }
        } else if (this.mSplitMode != 5) {
            if (isDragOutOfSwapAcceptBound(dragX, dragY) && !isDragOutOfSwapAcceptBoundOnY(dragY)) {
                Log.i(TAG, "handleDragLocation: remove swap pending drop view! split mode = " + this.mSplitMode);
                removeSelf();
            } else if (isDragOutOfSwapAcceptBoundOnY(dragY) && !this.mHasSwapHotAreaResized) {
                Point originalSizes = resizeSwapHotArea();
                addFreeFormSwapRegion(originalSizes.x, originalSizes.y);
                this.mHasSwapHotAreaResized = true;
            }
            HwMultiWinSwapAcceptView hwMultiWinSwapAcceptView = this.mHostSwapAcceptView;
            HwMultiWinSwapAcceptView hwFreeFormRegion = hwMultiWinSwapAcceptView != null ? hwMultiWinSwapAcceptView.getFreeFormSwapRegion() : null;
            if (hwFreeFormRegion != null && isDragOutOfSwapAcceptBoundOnY(dragY)) {
                Log.i(TAG, "handleDragLocation: split mode = " + this.mSplitMode + ", bring free form swap region to front");
                hwFreeFormRegion.bringToFront();
            }
        } else if (isDragOutOfSwapAcceptBound(dragX, dragY)) {
            Log.i(TAG, "handleDragLocation: remove swap pending drop view! split mode = " + this.mSplitMode);
            removeSelf();
        } else if (isDragIntoSwapAcceptBound(dragX, dragY)) {
            addSelf();
        }
        this.mLastDragX = dragX;
        this.mLastDragY = dragY;
    }

    private void addFreeFormSwapRegion(int freeFormRegionHeight, int swapAcceptRegionHeight) {
        if (this.mHwMultiWinHotAreaConfigListener != null) {
            this.mHwFreeFormSwapRegion = this.mHwMultiWinHotAreaConfigListener.onAddHwFreeFormSwapRegion(freeFormRegionHeight, swapAcceptRegionHeight, this.mSwapTarget);
            ViewGroup viewGroup = this.mSwapAcceptParent;
            int childCount = viewGroup != null ? viewGroup.getChildCount() : 0;
            for (int i = 0; i < childCount; i++) {
                View child = this.mSwapAcceptParent.getChildAt(i);
                if (child instanceof HwMultiWinSwapAcceptView) {
                    ((HwMultiWinSwapAcceptView) child).setFreeFormSwapRegion(this.mHwFreeFormSwapRegion);
                }
            }
            updateSwapAcceptBound();
        }
    }

    private Point resizeSwapHotArea() {
        Point originalSize = new Point();
        ViewGroup.LayoutParams p = this.mSwapAcceptParent.getLayoutParams();
        if (!(p instanceof LinearLayout.LayoutParams)) {
            Log.w(TAG, "resizeSwapHotArea failed, cause p is not LinearLayout.LayoutParams.");
            return originalSize;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) p;
        originalSize.y = this.mSwapAcceptParent.getHeight();
        params.weight = 1.0f;
        ViewParent parent = this.mSwapAcceptParent.getParent();
        if (!(parent instanceof ViewGroup)) {
            Log.w(TAG, "resizeSwapHotArea failed, cause parent is not view group.");
            return originalSize;
        }
        View other = ((ViewGroup) parent).getChildAt(1);
        if (other == null) {
            Log.w(TAG, "resizeSwapHotArea failed, cause other is null.");
            return originalSize;
        }
        ViewGroup.LayoutParams op = other.getLayoutParams();
        if (!(op instanceof LinearLayout.LayoutParams)) {
            Log.w(TAG, "resizeSwapHotArea failed, cause op is not LinearLayout.LayoutParams.");
            return originalSize;
        }
        LinearLayout.LayoutParams otherParams = (LinearLayout.LayoutParams) op;
        originalSize.x = other.getHeight();
        otherParams.weight = 0.0f;
        other.setLayoutParams(otherParams);
        this.mSwapAcceptParent.setLayoutParams(params);
        return originalSize;
    }

    private boolean isDragIntoSwapAcceptBound(float dragX, float dragY) {
        return isInSwapAcceptBound(dragX, dragY);
    }

    private boolean isDragOutOfSwapAcceptBound(float dragX, float dragY) {
        return !isInSwapAcceptBound(dragX, dragY);
    }

    private boolean isInSwapAcceptBound(float x, float y) {
        Rect rect = this.mSwapAcceptBound;
        if (rect == null) {
            Log.w(TAG, "isInSwapAcceptBound return false, cause mSwapAcceptBound is null");
            return false;
        } else if (x < ((float) rect.left) || x > ((float) this.mSwapAcceptBound.right) || y < ((float) this.mSwapAcceptBound.top) || y > ((float) this.mSwapAcceptBound.bottom)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isDragOutOfSwapAcceptBoundOnY(float dragY) {
        return isInSwapAcceptBoundOnY(this.mLastDragY) && !isInSwapAcceptBoundOnY(dragY);
    }

    private boolean isInSwapAcceptBoundOnY(float y) {
        Rect rect = this.mSwapAcceptBound;
        if (rect == null) {
            Log.w(TAG, "isInSwapAcceptBound return false, cause mSwapAcceptBound is null");
            return false;
        } else if (y < ((float) rect.top) || y > ((float) this.mSwapAcceptBound.bottom)) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragExited(DragEvent dragEvent) {
        super.handleDragExited(dragEvent);
        removeSelf();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDrop(DragEvent dragEvent, int dragSurfaceAnimType) {
        Log.d(TAG, "handleDrop: mSplitMode = " + this.mSplitMode);
        int animType = this.mSplitMode != 5 ? 9 : dragSurfaceAnimType;
        if (this.mSwapTarget != null && HwMultiWindowManager.IS_NOTCH_PROP && this.mSplitMode == 5 && this.mOriginalNotchStatus == 0) {
            this.mSwapTarget.playRoundCornerDismissAnimation();
        }
        super.handleDrop(dragEvent, animType);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public Rect getDropBound() {
        if (this.mSplitMode != 5) {
            Log.d(TAG, "getDropBound: left-right split, bound = " + super.getDropBound());
            return super.getDropBound();
        }
        Log.d(TAG, "getDropBound: free-form split, bound = " + this.mDropTargetBound);
        Rect rect = this.mDropTargetBound;
        return rect != null ? rect : new Rect();
    }
}

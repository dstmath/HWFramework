package com.android.server.multiwin.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.ViewGroup;

public class HwMultiWinNotchPendingDropView extends HwMultiWinHotAreaView {
    private static final boolean DBG = false;
    private static final int OUT_OF_ACCEPT_BOUND_COUNT_THRESHOLD = 2;
    private static final String TAG = "HwMultiWinNotchPendingDropView";
    private Rect mAcceptBound;
    private HwMultiWinHotAreaView mExecutorView;
    private int mOutOfAcceptBoundCount = 0;
    private Rect mPendingDropBound;

    public HwMultiWinNotchPendingDropView(Context context) {
        super(context);
    }

    public HwMultiWinNotchPendingDropView(Context context, Rect acceptBound, Rect pendingDropBound) {
        super(context);
        this.mAcceptBound = acceptBound;
        this.mPendingDropBound = pendingDropBound;
    }

    public HwMultiWinNotchPendingDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinNotchPendingDropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void updateLpSize(int width, int height) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = width;
        lp.height = height;
        setLayoutParams(lp);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragEntered(DragEvent dragEvent, int dragSurfaceAnimType) {
        Rect rect = this.mPendingDropBound;
        if (rect != null) {
            layout(rect.left, this.mPendingDropBound.top, this.mPendingDropBound.right, this.mPendingDropBound.bottom);
            updateLpSize(this.mPendingDropBound.width(), this.mPendingDropBound.height());
        }
        HwMultiWinHotAreaView hwMultiWinHotAreaView = this.mExecutorView;
        if (hwMultiWinHotAreaView != null) {
            hwMultiWinHotAreaView.handleDragEntered(dragEvent, dragSurfaceAnimType);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragLocation(DragEvent dragEvent) {
        if (this.mAcceptBound == null || this.mPendingDropBound == null) {
            Log.w(TAG, "handleDragLocation failed, cause mAcceptBound mPendingDropBound is null!");
            return;
        }
        if (!isInAcceptBound(dragEvent.getX() + ((float) getLeft()), dragEvent.getY() + ((float) getTop()))) {
            this.mOutOfAcceptBoundCount++;
        } else {
            this.mOutOfAcceptBoundCount = 0;
        }
        if (this.mOutOfAcceptBoundCount >= 2) {
            layout(this.mAcceptBound.left, this.mAcceptBound.top, this.mAcceptBound.right, this.mAcceptBound.bottom);
            updateLpSize(this.mAcceptBound.width(), this.mAcceptBound.height());
            this.mOutOfAcceptBoundCount = 0;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragExited(DragEvent dragEvent) {
        Rect rect = this.mAcceptBound;
        if (rect != null) {
            layout(rect.left, this.mAcceptBound.top, this.mAcceptBound.right, this.mAcceptBound.bottom);
            updateLpSize(this.mAcceptBound.width(), this.mAcceptBound.height());
        }
    }

    private boolean isInAcceptBound(float x, float y) {
        Rect rect = this.mAcceptBound;
        if (rect == null) {
            Log.w(TAG, "isInAcceptBound return false, cause mAcceptBound is null");
            return false;
        } else if (x < ((float) rect.left) || x > ((float) this.mAcceptBound.right) || y < ((float) this.mAcceptBound.top) || y > ((float) this.mAcceptBound.bottom)) {
            return false;
        } else {
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDrop(DragEvent dragEvent, int dragSurfaceAnimType) {
        HwMultiWinHotAreaView hwMultiWinHotAreaView = this.mExecutorView;
        if (hwMultiWinHotAreaView == null) {
            super.handleDrop(dragEvent, dragSurfaceAnimType);
        } else if (hwMultiWinHotAreaView instanceof HwMultiWinSwapAcceptView) {
            this.mIsDropOnThisView = true;
            this.mDragAnimationListener.onDrop(this, dragEvent, getSplitMode(), getDropBound(), 9);
        } else if (hwMultiWinHotAreaView instanceof HwMultiWinPushAcceptView) {
            this.mIsDropOnThisView = true;
            ((HwMultiWinPushAcceptView) hwMultiWinHotAreaView).getPushPendingDropView().handleDrop(dragEvent, dragSurfaceAnimType);
        } else {
            hwMultiWinHotAreaView.handleDrop(dragEvent, 6);
        }
    }

    public void setExecutorView(HwMultiWinHotAreaView executorView) {
        this.mExecutorView = executorView;
    }
}

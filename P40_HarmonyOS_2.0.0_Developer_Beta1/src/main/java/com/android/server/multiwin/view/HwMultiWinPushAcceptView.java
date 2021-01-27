package com.android.server.multiwin.view;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import com.android.server.wm.HwMultiWindowManager;

public class HwMultiWinPushAcceptView extends HwMultiWinHotAreaView {
    private static final String TAG = "HwMultiWinPushAcceptView";
    private HwMultiWinPushPendingDropView mPushPendingDropView;
    private HwMultiWinClipImageView mPushTarget;

    public HwMultiWinPushAcceptView(Context context) {
        super(context);
        this.mIsToDrawBorder = false;
    }

    public HwMultiWinPushAcceptView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinPushAcceptView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragEntered(DragEvent dragEvent, int dragSurfaceAnimType) {
        super.handleDragEntered(dragEvent, dragSurfaceAnimType);
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mPushTarget;
        if (hwMultiWinClipImageView != null) {
            hwMultiWinClipImageView.playPrePushAnimation(this.mSplitMode);
        }
        if (this.mSplitMode != 5) {
            addSplitPendingDropView();
        }
        if (this.mSplitBarController != null) {
            this.mSplitBarController.showSplitBarWithAnimation();
        }
    }

    private void addSplitPendingDropView() {
        HwMultiWinPushPendingDropView hwMultiWinPushPendingDropView = this.mPushPendingDropView;
        if (hwMultiWinPushPendingDropView == null || hwMultiWinPushPendingDropView.getParent() == null) {
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                ViewParent dropViewParent = viewGroup.getParent();
                ViewGroup dropViewGroup = null;
                if (dropViewParent instanceof ViewGroup) {
                    dropViewGroup = (ViewGroup) dropViewParent;
                }
                Rect pushAcceptBound = new Rect(getLeft(), getTop(), getRight(), getBottom());
                if (dropViewGroup != null) {
                    pushAcceptBound.left += dropViewGroup.getLeft();
                    pushAcceptBound.top += dropViewGroup.getTop();
                    pushAcceptBound.right += dropViewGroup.getLeft();
                    pushAcceptBound.bottom += dropViewGroup.getTop();
                }
                int splitMargin = (int) (((float) getContext().getResources().getDimensionPixelSize(34472627)) / 2.0f);
                int width = isLandScape() ? (int) ((((float) viewGroup.getWidth()) / 2.0f) - ((float) splitMargin)) : viewGroup.getWidth();
                int height = isLandScape() ? viewGroup.getHeight() : (int) ((((float) viewGroup.getHeight()) / 2.0f) - ((float) splitMargin));
                Point pendingDropSize = new Point(width, height);
                RelativeLayout.LayoutParams lp = getSplitPendingDropViewLayoutParams(width, height);
                this.mPushPendingDropView = new HwMultiWinPushPendingDropView(getContext(), pushAcceptBound, this.mPushTarget, this.mSplitMode, pendingDropSize);
                this.mPushPendingDropView.setDragAnimationListener(this.mDragAnimationListener);
                this.mPushPendingDropView.setLayoutParams(lp);
                this.mPushPendingDropView.setSplitBarController(this.mSplitBarController);
                this.mPushPendingDropView.setNotchPos(this.mNotchPos);
                if (dropViewGroup != null) {
                    dropViewGroup.addView(this.mPushPendingDropView);
                    this.mPushPendingDropView.setHasRemovedSelf(false);
                    this.mPushPendingDropView.playShadowShowAnimation();
                    return;
                }
                return;
            }
            Log.w(TAG, "addSplitPendingDropView failed cause parent view group is null!");
        }
    }

    /* access modifiers changed from: package-private */
    public HwMultiWinPushPendingDropView getPushPendingDropView() {
        return this.mPushPendingDropView;
    }

    private RelativeLayout.LayoutParams getSplitPendingDropViewLayoutParams(int width, int height) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        if (isLandScape()) {
            if (this.mSplitMode == 1) {
                lp.addRule(9);
            } else {
                lp.addRule(11);
            }
        } else if (this.mSplitMode == 3) {
            lp.addRule(10);
        } else {
            lp.addRule(12);
        }
        lp.width = width;
        lp.height = height;
        return lp;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDrop(DragEvent dragEvent, int dragSurfaceAnimType) {
        super.handleDrop(dragEvent, dragSurfaceAnimType);
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mPushTarget;
        if (hwMultiWinClipImageView != null) {
            hwMultiWinClipImageView.playFinalPushAnimation(this.mSplitMode);
            if (HwMultiWindowManager.IS_NOTCH_PROP && this.mSplitMode == 5 && this.mOriginalNotchStatus == 0) {
                this.mPushTarget.playRoundCornerDismissAnimation();
            }
        }
    }

    public void setPushTarget(HwMultiWinClipImageView pushTarget) {
        this.mPushTarget = pushTarget;
    }
}

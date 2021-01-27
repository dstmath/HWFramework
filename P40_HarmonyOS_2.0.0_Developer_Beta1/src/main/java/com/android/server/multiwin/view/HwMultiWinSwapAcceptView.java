package com.android.server.multiwin.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.multiwin.listener.BlurListener;

public class HwMultiWinSwapAcceptView extends HwMultiWinHotAreaView {
    private static final boolean DBG = false;
    private static final String TAG = "HwMultiWinSwapAcceptView";
    private Rect mDropTargetBound = new Rect();
    private HwMultiWinHotAreaView mDropTargetView;
    private HwMultiWinSwapPendingDropView mFreeFormPendingDropView;
    private HwMultiWinSwapAcceptView mFreeFormSwapRegion;
    private Rect mNotchBound = new Rect();
    private int mNotchPos = -1;
    private HwMultiWinSwapAcceptView mOtherSplitSwapAcceptView;
    private HwMultiWinSwapPendingDropView mSplitPendingDropView;
    private HwMultiWinClipImageView mSwapTarget;
    private Rect mSwapTargetBound = new Rect();

    public HwMultiWinSwapAcceptView(Context context, int splitMode) {
        super(context);
        this.mSplitMode = splitMode;
        this.mIsToDrawBorder = false;
    }

    public HwMultiWinSwapAcceptView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwMultiWinSwapAcceptView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private float getSplitSwapSize() {
        HwMultiWinSwapAcceptView hwMultiWinSwapAcceptView = this.mOtherSplitSwapAcceptView;
        if (hwMultiWinSwapAcceptView == null) {
            Log.w(TAG, "getSwapSize failed, cause mOtherSplitSwapAcceptView is null!");
            return 0.0f;
        }
        Rect otherDropTargetBound = hwMultiWinSwapAcceptView.getDropTargetBound();
        if (otherDropTargetBound == null) {
            Log.w(TAG, "getSwapSize failed, cause or otherDropTargetBound is null!");
            return 0.0f;
        }
        return (float) (this.mContext.getResources().getDimensionPixelSize(34472627) + (isLandScape() ? otherDropTargetBound.width() : otherDropTargetBound.height()));
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    @SuppressLint({"NewApi"})
    public void handleDragEntered(DragEvent dragEvent, int dragSurfaceAnimType) {
        int animType;
        if (this.mSplitMode == 5) {
            animType = 7;
        } else {
            animType = 8;
        }
        super.handleDragEntered(dragEvent, animType);
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mSwapTarget;
        if (hwMultiWinClipImageView != null) {
            if (!hwMultiWinClipImageView.isHasBeenResizedWithoutNavBar() && HwMultiWinUtils.isNeedToResizeWithoutNavBar(this.mSwapTarget.getOriginalInSwapMode(), this.mNavBarPos)) {
                resizeSwapTargetWithoutNavBar();
                this.mSwapTarget.setHasBeenResizedWithoutNavBar(true);
            }
            if (this.mSplitMode == 5 && this.mSwapTarget.getSwapBlurCover() == null) {
                this.mSwapTarget.setSwapBlurCover(addBlurCoverForSwapTarget());
                startToBlurSwapTarget();
            }
            this.mSwapTarget.playSwapAnimation(this.mSplitMode, getSplitSwapSize(), new AnimatorListenerAdapter() {
                /* class com.android.server.multiwin.view.HwMultiWinSwapAcceptView.AnonymousClass1 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    if (HwMultiWinSwapAcceptView.this.mSplitBarController != null) {
                        HwMultiWinSwapAcceptView.this.mSplitBarController.hideSplitBarWithAnimation();
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    if (HwMultiWinSwapAcceptView.this.mSplitBarController != null) {
                        if (HwMultiWinSwapAcceptView.this.mSplitMode != 5) {
                            boolean isUpdateToSwapMargin = false;
                            if ((HwMultiWinSwapAcceptView.this.mSwapTarget != null ? HwMultiWinSwapAcceptView.this.mSwapTarget.getOriginalInSwapMode() : 0) == HwMultiWinSwapAcceptView.this.mSplitMode) {
                                isUpdateToSwapMargin = true;
                            }
                            HwMultiWinSwapAcceptView.this.mSplitBarController.updateMargin(isUpdateToSwapMargin);
                            HwMultiWinSwapAcceptView.this.mSplitBarController.showSplitBarWithAnimation();
                            return;
                        }
                        HwMultiWinSwapAcceptView.this.mSplitBarController.hideSplitBarWithAnimation();
                    }
                }
            });
        }
        addSplitDropTargetView();
    }

    private void startToBlurSwapTarget() {
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mSwapTarget;
        if (hwMultiWinClipImageView != null) {
            HwMultiWinUtils.blurForScreenShot(hwMultiWinClipImageView, hwMultiWinClipImageView, (BlurListener) null, ImageView.ScaleType.FIT_START);
        }
    }

    private View addBlurCoverForSwapTarget() {
        if (this.mHotAreaLayout == null) {
            Log.w(TAG, "addBlurCoverForSwapTarget failed, cause mHotAreaLayout is null!");
            return null;
        }
        float scaleX = this.mSwapTarget.getScaleX();
        Rect rect = this.mSwapTargetBound;
        int coverWidth = (int) (scaleX * ((float) (rect != null ? rect.width() : this.mSwapTarget.getWidth())));
        float scaleY = this.mSwapTarget.getScaleY();
        Rect rect2 = this.mSwapTargetBound;
        int coverHeight = (int) (scaleY * ((float) (rect2 != null ? rect2.height() : this.mSwapTarget.getHeight())));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(coverWidth, coverHeight);
        if (isLandScape()) {
            if (this.mSwapTarget.getOriginalInSwapMode() == 1) {
                lp.addRule(9);
                if (this.mNavBarPos == 1 && this.mNavBarBound != null) {
                    lp.leftMargin = this.mNavBarBound.width();
                }
            } else {
                lp.addRule(11);
                if (this.mNavBarPos == 2 && this.mNavBarBound != null) {
                    lp.rightMargin = this.mNavBarBound.width();
                }
            }
        } else if (this.mSwapTarget.getOriginalInSwapMode() == 3) {
            lp.addRule(10);
        } else {
            lp.addRule(12);
        }
        lp.width = coverWidth;
        lp.height = coverHeight;
        HwMultiWinClipImageView cover = new HwMultiWinClipImageView(this.mContext);
        cover.setScaleType(ImageView.ScaleType.FIT_XY);
        cover.setImageDrawable(this.mSwapTarget.getDrawable());
        cover.setLayoutParams(lp);
        cover.setTranslationX(this.mSwapTarget.getTranslationX());
        cover.setTranslationY(this.mSwapTarget.getTranslationY());
        this.mHotAreaLayout.addView(cover);
        return cover;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDrop(DragEvent dragEvent, int dragSurfaceAnimType) {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.multiwin.view.HwMultiWinHotAreaView
    public void handleDragExited(DragEvent dragEvent) {
        super.handleDragExited(dragEvent);
    }

    private void addSplitDropTargetView() {
        if (this.mHotAreaLayout == null) {
            Log.w(TAG, "addSplitDropTargetView failed cause mHotAreaLayout is null!");
        } else if (this.mSplitMode != 5) {
            addSplitPendingDropView();
        } else {
            addFreeFormPendingDropView();
        }
    }

    private int getSplitPendingDropViewSize(Point size, Point leftAndRightMargin) {
        Rect bound = this.mDropTargetBound;
        if (bound.isEmpty()) {
            return 0;
        }
        int width = bound.width();
        int height = bound.height();
        int leftMargin = 0;
        int rightMargin = 0;
        int bottomMargin = 0;
        if (this.mNavBarPos == 4 && ((isLandScape() || this.mSplitMode == 4) && this.mSplitMode == 4)) {
            bottomMargin = this.mNavBarBound.height();
        }
        if (this.mNavBarPos == 1 && ((!isLandScape() || this.mSplitMode == 1) && this.mSplitMode == 1)) {
            leftMargin = this.mNavBarBound.width();
        }
        if (this.mNavBarPos == 2 && ((!isLandScape() || this.mSplitMode == 2) && this.mSplitMode == 2)) {
            rightMargin = this.mNavBarBound.width();
        }
        size.x = width;
        size.y = height;
        leftAndRightMargin.x = leftMargin;
        leftAndRightMargin.y = rightMargin;
        return bottomMargin;
    }

    private void setRulesForSplitPendingDropView(RelativeLayout.LayoutParams lp) {
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
    }

    private void addSplitPendingDropView() {
        Rect rect;
        Rect rect2;
        Point size = new Point();
        Point leftAndRightMargins = new Point();
        int bottomMargin = getSplitPendingDropViewSize(size, leftAndRightMargins);
        int width = size.x;
        int height = size.y;
        int leftMargin = leftAndRightMargins.x;
        int rightMargin = leftAndRightMargins.y;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, height);
        setRulesForSplitPendingDropView(lp);
        lp.width = width;
        lp.height = height;
        lp.leftMargin = leftMargin;
        lp.rightMargin = rightMargin;
        lp.bottomMargin = bottomMargin;
        adjustSplitPendingDropViewLp(lp);
        ViewParent parent = getParent();
        Rect swapAcceptBound = new Rect(getLeft(), getTop(), getRight(), getBottom());
        ViewGroup swapAcceptParent = null;
        if (parent instanceof ViewGroup) {
            swapAcceptParent = (ViewGroup) parent;
        }
        if (swapAcceptParent != null) {
            swapAcceptBound.left += swapAcceptParent.getLeft();
            swapAcceptBound.top += swapAcceptParent.getTop();
            swapAcceptBound.right += swapAcceptParent.getLeft();
            swapAcceptBound.bottom += swapAcceptParent.getTop();
        }
        if (this.mNotchPos == 1 && (rect2 = this.mNotchBound) != null) {
            swapAcceptBound.offset(rect2.width(), 0);
        }
        if (this.mNotchPos == 0 && (rect = this.mNotchBound) != null) {
            swapAcceptBound.offset(0, rect.height());
        }
        HwMultiWinHotAreaView swapTarget = null;
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mSwapTarget;
        if (hwMultiWinClipImageView instanceof HwMultiWinHotAreaView) {
            swapTarget = (HwMultiWinHotAreaView) hwMultiWinClipImageView;
        }
        if (this.mSplitPendingDropView == null) {
            createSplitPendingDropView(swapAcceptBound, swapTarget, swapAcceptParent);
        }
        this.mSplitPendingDropView.setLayoutParams(lp);
        removeLastSwapPendingDropView();
        if (this.mSplitPendingDropView.getParent() instanceof ViewGroup) {
            ((ViewGroup) this.mSplitPendingDropView.getParent()).removeView(this.mSplitPendingDropView);
        }
        this.mHotAreaLayout.addView(this.mSplitPendingDropView);
        HwMultiWinSwapAcceptView hwMultiWinSwapAcceptView = this.mFreeFormSwapRegion;
        if (hwMultiWinSwapAcceptView != null) {
            hwMultiWinSwapAcceptView.bringToFront();
        }
        Log.i(TAG, "addSplitPendingDropView: split mode = " + this.mSplitMode);
        this.mSplitPendingDropView.setHasRemovedSelf(false);
    }

    private void createSplitPendingDropView(Rect swapAcceptBound, HwMultiWinHotAreaView swapTarget, ViewGroup swapAcceptParent) {
        this.mSplitPendingDropView = new HwMultiWinSwapPendingDropView(getContext(), isSwapToRight(), swapAcceptBound, this.mSplitMode, swapTarget);
        this.mSplitPendingDropView.setHotAreaLayout(this.mHotAreaLayout);
        this.mSplitPendingDropView.setDragAnimationListener(this.mDragAnimationListener);
        this.mSplitPendingDropView.setIsLandScape(isLandScape());
        this.mSplitPendingDropView.setSplitBarController(this.mSplitBarController);
        this.mSplitPendingDropView.setSwapAcceptParent(swapAcceptParent);
        this.mSplitPendingDropView.setHwMultiWinHotAreaConfigListener(this.mHwMultiWinHotAreaConfigListener);
        this.mSplitPendingDropView.setHostSwapAcceptView(this);
    }

    private void adjustSplitPendingDropViewLp(RelativeLayout.LayoutParams lp) {
        Rect swapAcceptBound = new Rect(getLeft(), getTop(), getRight(), getBottom());
        Rect dropViewBound = this.mDropTargetBound;
        int dropViewSize = isLandScape() ? dropViewBound.width() : dropViewBound.height();
        int swapAcceptSize = isLandScape() ? swapAcceptBound.width() : swapAcceptBound.height();
        if (dropViewSize < swapAcceptSize) {
            int adjustSize = (int) ((((float) swapAcceptSize) * 2.0f) - ((float) dropViewSize));
            int adjustMargin = swapAcceptSize - dropViewSize;
            if (isLandScape()) {
                lp.width = adjustSize;
            } else {
                lp.height = adjustSize;
            }
            if (lp.getRule(9) == -1) {
                lp.leftMargin -= adjustMargin;
            } else if (lp.getRule(11) == -1) {
                lp.rightMargin -= adjustMargin;
            } else if (lp.getRule(10) == -1) {
                lp.topMargin -= adjustMargin;
            } else if (lp.getRule(12) == -1) {
                lp.bottomMargin -= adjustMargin;
            }
        }
    }

    private void removeLastSwapPendingDropView() {
        int lastViewIndex = this.mHotAreaLayout.getChildCount() - 1;
        View lastView = this.mHotAreaLayout.getChildAt(lastViewIndex);
        if (lastViewIndex >= 0 && (lastView instanceof HwMultiWinSwapPendingDropView)) {
            this.mHotAreaLayout.removeViewAt(lastViewIndex);
            ((HwMultiWinSwapPendingDropView) lastView).setHasRemovedSelf(true);
        }
    }

    private void addFreeFormPendingDropView() {
        Rect rect;
        Rect rect2;
        Rect swapAcceptBound = new Rect(getLeft(), getTop(), getRight(), getBottom());
        HwMultiWinHotAreaView swapTarget = null;
        HwMultiWinClipImageView hwMultiWinClipImageView = this.mSwapTarget;
        if (hwMultiWinClipImageView instanceof HwMultiWinHotAreaView) {
            swapTarget = (HwMultiWinHotAreaView) hwMultiWinClipImageView;
        }
        if (this.mNotchPos == 1 && (rect2 = this.mNotchBound) != null) {
            swapAcceptBound.offset(rect2.width(), 0);
        }
        if (this.mNotchPos == 0 && (rect = this.mNotchBound) != null) {
            swapAcceptBound.offset(0, rect.height());
        }
        if (this.mFreeFormPendingDropView == null) {
            this.mFreeFormPendingDropView = new HwMultiWinSwapPendingDropView(getContext(), isSwapToRight(), swapAcceptBound, this.mSplitMode, swapTarget);
            this.mFreeFormPendingDropView.setHotAreaLayout(this.mHotAreaLayout);
            this.mFreeFormPendingDropView.setDragAnimationListener(this.mDragAnimationListener);
            this.mFreeFormPendingDropView.setIsLandScape(isLandScape());
            Rect rect3 = this.mDropTargetBound;
            if (rect3 != null) {
                this.mFreeFormPendingDropView.setDropTargetBound(rect3);
            }
        }
        this.mFreeFormPendingDropView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
        removeLastSwapPendingDropView();
        this.mHotAreaLayout.addView(this.mFreeFormPendingDropView);
        Log.i(TAG, "addFreeFormPendingDropView: split mode = " + this.mSplitMode);
        this.mFreeFormPendingDropView.setHasRemovedSelf(false);
    }

    public void initSplitSwapAnimation(HwMultiWinHotAreaView dropTarget, HwMultiWinClipImageView swapTarget) {
        if (dropTarget == null) {
            Log.w(TAG, "initSplitSwapAnimation failed, dropTarget is null!");
            return;
        }
        if ((this.mSplitMode == 1 || this.mSplitMode == 2 || this.mSplitMode == 3 || this.mSplitMode == 4) && (swapTarget instanceof HwMultiWinHotAreaView)) {
            HwMultiWinHotAreaView hwMultiWinHotAreaView = (HwMultiWinHotAreaView) swapTarget;
            hwMultiWinHotAreaView.setInSwapMode(hwMultiWinHotAreaView.getSplitMode());
        }
        this.mDropTargetView = dropTarget;
        getDropTargetBoundOnLayout();
        this.mSwapTarget = swapTarget;
        getSwapTargetBoundOnLayout();
    }

    public void initSplitSwapAnimation(Rect dropTargetBound, HwMultiWinClipImageView swapTarget) {
        if ((this.mSplitMode == 1 || this.mSplitMode == 2 || this.mSplitMode == 3 || this.mSplitMode == 4) && (swapTarget instanceof HwMultiWinHotAreaView)) {
            HwMultiWinHotAreaView hwMultiWinHotAreaView = (HwMultiWinHotAreaView) swapTarget;
            hwMultiWinHotAreaView.setInSwapMode(hwMultiWinHotAreaView.getSplitMode());
        }
        this.mDropTargetBound = new Rect(dropTargetBound);
        this.mSwapTarget = swapTarget;
        getSwapTargetBoundOnLayout();
    }

    private void getSwapTargetBoundOnLayout() {
        this.mSwapTarget.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class com.android.server.multiwin.view.HwMultiWinSwapAcceptView.AnonymousClass2 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                HwMultiWinSwapAcceptView.this.mSwapTargetBound.set(HwMultiWinSwapAcceptView.this.mSwapTarget.getLeft(), HwMultiWinSwapAcceptView.this.mSwapTarget.getTop(), HwMultiWinSwapAcceptView.this.mSwapTarget.getRight(), HwMultiWinSwapAcceptView.this.mSwapTarget.getBottom());
                HwMultiWinSwapAcceptView.this.mSwapTarget.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void getDropTargetBoundOnLayout() {
        if (this.mSplitMode != 5) {
            this.mDropTargetView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                /* class com.android.server.multiwin.view.HwMultiWinSwapAcceptView.AnonymousClass3 */

                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                public void onGlobalLayout() {
                    HwMultiWinSwapAcceptView.this.mDropTargetBound.set(HwMultiWinSwapAcceptView.this.mDropTargetView.getLeft(), HwMultiWinSwapAcceptView.this.mDropTargetView.getTop(), HwMultiWinSwapAcceptView.this.mDropTargetView.getRight(), HwMultiWinSwapAcceptView.this.mDropTargetView.getBottom());
                    if (HwMultiWinUtils.isNeedToResizeWithoutNavBar(HwMultiWinSwapAcceptView.this.mDropTargetView.getSplitMode(), HwMultiWinSwapAcceptView.this.mNavBarPos)) {
                        HwMultiWinSwapAcceptView hwMultiWinSwapAcceptView = HwMultiWinSwapAcceptView.this;
                        hwMultiWinSwapAcceptView.mDropTargetBound = HwMultiWinUtils.getBoundWithoutNavBar(hwMultiWinSwapAcceptView.mNavBarPos, HwMultiWinSwapAcceptView.this.mNavBarBound, HwMultiWinSwapAcceptView.this.mDropTargetBound);
                    }
                    HwMultiWinSwapAcceptView.this.mDropTargetView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    private void resizeSwapTargetWithoutNavBar() {
        Rect srcBound = this.mSwapTargetBound;
        Rect dstBound = HwMultiWinUtils.getBoundWithoutNavBar(this.mNavBarPos, this.mNavBarBound, srcBound);
        ViewGroup.LayoutParams layoutParams = this.mSwapTarget.getLayoutParams();
        if (!(layoutParams instanceof LinearLayout.LayoutParams)) {
            Log.w(TAG, "resizeSwapTargetWithoutNavBar failed, cause layoutParams is not LinearLayout.LayoutParams.");
            return;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutParams;
        Bitmap swapTargetScreenShot = HwMultiWinUtils.drawable2Bitmap(this.mSwapTarget.getDrawable());
        if (swapTargetScreenShot == null) {
            Slog.w(TAG, "resizeSwapTargetWithoutNavBar failed, cause swapTargetScreenShot is null!");
            return;
        }
        params.leftMargin += dstBound.left - srcBound.left;
        params.rightMargin += srcBound.right - dstBound.right;
        params.bottomMargin += srcBound.bottom - dstBound.bottom;
        float scaleFactor = this.mSwapTarget.getWidth() > 0 ? ((float) swapTargetScreenShot.getWidth()) / ((float) this.mSwapTarget.getWidth()) : 1.0f;
        this.mSwapTarget.setLayoutParams(params);
        this.mSwapTarget.setImageBitmap(HwMultiWinUtils.getScreenShotBmpWithoutNavBar(swapTargetScreenShot, this.mNavBarPos, this.mNavBarBound, scaleFactor));
        this.mSwapTargetBound.set(dstBound);
    }

    /* access modifiers changed from: package-private */
    public boolean isSwapToRight() {
        return this.mSplitMode == 1;
    }

    public void setOtherSplitSwapAcceptView(HwMultiWinSwapAcceptView otherSplitSwapAcceptView) {
        this.mOtherSplitSwapAcceptView = otherSplitSwapAcceptView;
    }

    public Rect getDropTargetBound() {
        return this.mDropTargetBound;
    }

    /* access modifiers changed from: package-private */
    public void setFreeFormSwapRegion(HwMultiWinSwapAcceptView freeFormSwapRegion) {
        this.mFreeFormSwapRegion = freeFormSwapRegion;
    }

    /* access modifiers changed from: package-private */
    public HwMultiWinSwapAcceptView getFreeFormSwapRegion() {
        return this.mFreeFormSwapRegion;
    }

    public void setNotchInfo(int notchPos, Rect notchBound) {
        this.mNotchPos = notchPos;
        if (this.mNotchBound == null) {
            this.mNotchBound = new Rect();
        }
        this.mNotchBound.set(notchBound);
    }
}

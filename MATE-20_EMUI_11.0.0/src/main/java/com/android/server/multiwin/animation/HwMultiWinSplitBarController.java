package com.android.server.multiwin.animation;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.server.multiwin.HwMultiWinUtils;
import com.android.server.multiwin.animation.interpolator.SharpCurveInterpolator;

public class HwMultiWinSplitBarController {
    private static final long SPLIT_BAR_ALPHA_HIDE_ANIM_DURATION = 20;
    private static final long SPLIT_BAR_ALPHA_SHOW_ANIM_DURATION = 100;
    private static final float SPLIT_BAR_MAX_ALPHA = 1.0f;
    private static final float SPLIT_BAR_MIN_ALPHA = 0.0f;
    private static final String TAG = "HwMultiWinSplitBarController";
    private Context mContext;
    private boolean mIsLandScape;
    private float mLastToAlpha;
    private int mOriginalMargin;
    private View mSplitBar;
    private ObjectAnimator mSplitBarAlphaAnimator;
    private int mSwapMargin;

    public HwMultiWinSplitBarController(View splitBar, boolean isLandScape) {
        this.mSplitBar = splitBar;
        this.mIsLandScape = isLandScape;
        if (splitBar != null) {
            this.mContext = splitBar.getContext();
        }
    }

    public void setMargins(int originalMargin, int swapMargin) {
        this.mOriginalMargin = originalMargin;
        this.mSwapMargin = swapMargin;
    }

    public void updateMargin(boolean isUpdateToSwapMargin) {
        if (this.mSplitBar == null) {
            Log.w(TAG, "updateMargin failed, cause mSplitBar is null!");
            return;
        }
        int newMargin = isUpdateToSwapMargin ? this.mSwapMargin : this.mOriginalMargin;
        ViewGroup.LayoutParams lp = this.mSplitBar.getLayoutParams();
        if (!(lp instanceof RelativeLayout.LayoutParams)) {
            Log.w(TAG, "updateMargin failed, cause mSplitBar's lp is not instance of RelativeLayout.LayoutParams!");
            return;
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lp;
        if (this.mIsLandScape) {
            params.leftMargin = newMargin;
        } else {
            params.topMargin = newMargin;
        }
        this.mSplitBar.setLayoutParams(params);
    }

    public void showSplitBar() {
        this.mSplitBar.setAlpha(1.0f);
    }

    public void hideSplitBar() {
        this.mSplitBar.setAlpha(0.0f);
    }

    public void showSplitBarWithAnimation() {
        playSplitBarAnimation(1.0f, SPLIT_BAR_ALPHA_SHOW_ANIM_DURATION);
    }

    public void hideSplitBarWithAnimation() {
        playSplitBarAnimation(0.0f, SPLIT_BAR_ALPHA_HIDE_ANIM_DURATION);
    }

    private void playSplitBarAnimation(float toAlpha, long duration) {
        if (this.mSplitBar == null) {
            Log.w(TAG, "playSplitBarAnimation failed, cause mSplitBar is null!");
        } else if (HwMultiWinUtils.floatEquals(this.mLastToAlpha, toAlpha)) {
            Log.i(TAG, "don't need to start mSplitBarAlphaAnimator, cause mLastToAlpha is equal to toAlpha");
        } else {
            this.mLastToAlpha = toAlpha;
            ObjectAnimator objectAnimator = this.mSplitBarAlphaAnimator;
            if (objectAnimator != null && objectAnimator.isStarted()) {
                this.mSplitBarAlphaAnimator.cancel();
            }
            this.mSplitBarAlphaAnimator = ObjectAnimator.ofFloat(this.mSplitBar, View.ALPHA, this.mSplitBar.getAlpha(), toAlpha);
            this.mSplitBarAlphaAnimator.setDuration(duration);
            this.mSplitBarAlphaAnimator.setInterpolator(new SharpCurveInterpolator());
            this.mSplitBarAlphaAnimator.start();
        }
    }
}

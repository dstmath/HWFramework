package com.huawei.emui.hiexperience.hwperf.imageview;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.huawei.emui.hiexperience.hwperf.HwPerfBase;

public class HwPerfImageEffect extends HwPerfBase {
    private boolean mEffectEnable = false;
    private ImageView mImageView;

    public HwPerfImageEffect(Context context) {
    }

    public void enableShowEffect(ImageView imageview, boolean enable) {
        if (enable) {
            this.mImageView = imageview;
            setAlphaAnimation(500, 0.0f, 1.0f, new DecelerateInterpolator());
        }
    }

    private void setAlphaAnimation(int durationMillis, float alphaBegin, float alphaEnd, Interpolator interpolator) {
        if (this.mImageView != null) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(alphaBegin, alphaEnd);
            alphaAnimation.setInterpolator(interpolator);
            alphaAnimation.setDuration((long) durationMillis);
            this.mImageView.startAnimation(alphaAnimation);
        }
    }
}

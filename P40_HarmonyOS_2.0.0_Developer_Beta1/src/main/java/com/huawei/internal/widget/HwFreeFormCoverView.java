package com.huawei.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;

public class HwFreeFormCoverView extends ImageView {
    private static final float ALPHA_OPAQUE = 1.0f;
    private static final float ALPHA_TRANSPARENT = 0.0f;
    private static final float COVER_CONTROL_X1 = 0.4f;
    private static final float COVER_CONTROL_X2 = 0.2f;
    private static final float COVER_CONTROL_Y1 = 0.0f;
    private static final float COVER_CONTROL_Y2 = 1.0f;
    private static final long COVER_KEEP_DURATION_LONG = 350;
    private static final long COVER_KEEP_DURATION_SHORT = 200;
    public static final int DURATION_LONG = 1;
    public static final int DURATION_SHORT = 0;
    private static final float ICON_CONTROL_X1 = 0.33f;
    private static final float ICON_CONTROL_X2 = 0.67f;
    private static final float ICON_CONTROL_Y1 = 0.0f;
    private static final float ICON_CONTROL_Y2 = 1.0f;
    private static final float ICON_SCALE_FACTOR = 1.2f;
    private static final long ICON_SHOW_DURATION = 350;
    private static final float ICON_SHOW_SIZE_DP = 58.7f;
    private static final int MAX_CHANNEL_VALUE = 255;
    private static final String PROP_ICON_ALPHA = "HwFreeFormCoverView_Icon_Alpha";
    public static final String TAG = "HwFreeFormCoverView";
    private Drawable mAppIcon;
    private ValueAnimator mAppIconAnimator;
    private ObjectAnimator mDismissAnimator;
    private View mParent;

    public HwFreeFormCoverView(Context context) {
        super(context);
    }

    public HwFreeFormCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwFreeFormCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HwFreeFormCoverView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setParent(View parent) {
        this.mParent = parent;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clear() {
        ValueAnimator valueAnimator = this.mAppIconAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mAppIconAnimator = null;
        }
        ObjectAnimator objectAnimator = this.mDismissAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
            this.mDismissAnimator = null;
        }
        this.mAppIcon = null;
        setImageDrawable(null);
        setVisibility(8);
        View view = this.mParent;
        if (view != null) {
            view.setVisibility(8);
        }
        setAlpha(1.0f);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.ImageView, android.view.View
    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
            Drawable drawable = this.mAppIcon;
            if (drawable != null && drawable.getAlpha() > 0) {
                canvas.save();
                float scaleX = ICON_SCALE_FACTOR / getScaleX();
                float scaleY = ICON_SCALE_FACTOR / getScaleY();
                canvas.translate((((float) getWidth()) - (((float) this.mAppIcon.getBounds().width()) * scaleX)) / 2.0f, (((float) getHeight()) - (((float) this.mAppIcon.getBounds().height()) * scaleY)) / 2.0f);
                canvas.scale(scaleX, scaleY);
                this.mAppIcon.draw(canvas);
                canvas.restore();
            }
        }
    }

    public void setIcon(Drawable icon) {
        this.mAppIcon = icon;
        Drawable drawable = this.mAppIcon;
        if (drawable != null) {
            drawable.setBounds(0, 0, HwCaptionViewUtils.dipToPx(getContext(), ICON_SHOW_SIZE_DP), HwCaptionViewUtils.dipToPx(getContext(), ICON_SHOW_SIZE_DP));
            this.mAppIcon.setAlpha(0);
        }
    }

    public void playIconShowAnimation() {
        Log.d(TAG, "playIconShowAnimation");
        if (this.mAppIcon == null) {
            Log.d(TAG, "playIconShowAnimation: mAppIcon is null, just cancel!");
            return;
        }
        ValueAnimator valueAnimator = this.mAppIconAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            Log.d(TAG, "playIconShowAnimation: just cancel!");
            this.mAppIconAnimator.cancel();
        }
        this.mAppIconAnimator = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat(PROP_ICON_ALPHA, ((float) this.mAppIcon.getAlpha()) / 255.0f, 1.0f));
        this.mAppIconAnimator.setDuration(350L);
        this.mAppIconAnimator.setInterpolator(new PathInterpolator(ICON_CONTROL_X1, 0.0f, ICON_CONTROL_X2, 1.0f));
        this.mAppIconAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.huawei.internal.widget.HwFreeFormCoverView.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (HwFreeFormCoverView.this.mAppIcon != null && animation != null) {
                    Object alphaObj = animation.getAnimatedValue(HwFreeFormCoverView.PROP_ICON_ALPHA);
                    float alpha = 1.0f;
                    if (alphaObj instanceof Float) {
                        alpha = ((Float) alphaObj).floatValue();
                    }
                    HwFreeFormCoverView.this.mAppIcon.setAlpha((int) (255.0f * alpha));
                    HwFreeFormCoverView.this.invalidate();
                }
            }
        });
        this.mAppIconAnimator.start();
    }

    public void playDismissAnimation(int durationType) {
        ObjectAnimator objectAnimator = this.mDismissAnimator;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            Log.d(TAG, "playDismissAnimation: just cancel!");
            this.mDismissAnimator.cancel();
        }
        this.mDismissAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
        this.mDismissAnimator.setDuration(durationType == 1 ? 350 : COVER_KEEP_DURATION_SHORT);
        this.mDismissAnimator.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f));
        this.mDismissAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.huawei.internal.widget.HwFreeFormCoverView.AnonymousClass2 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                HwFreeFormCoverView.this.clear();
            }
        });
        this.mDismissAnimator.start();
    }

    public boolean isAnimating() {
        ObjectAnimator objectAnimator = this.mDismissAnimator;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            return true;
        }
        ValueAnimator valueAnimator = this.mAppIconAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            return false;
        }
        return true;
    }
}

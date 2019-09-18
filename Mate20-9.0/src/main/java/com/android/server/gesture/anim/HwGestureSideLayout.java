package com.android.server.gesture.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.server.gesture.GestureNavConst;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwGestureSideLayout extends RelativeLayout {
    public static final boolean CHINA_REGION = "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    public static final int HIVISION = 1;
    public static final int HIVOICE = 0;
    private static final int ICON_ALPHA_DURATION = 200;
    private static final float ICON_INIT_SCALE = 0.2f;
    private static final int ICON_MAX_TRANSLATE_DISTANCE_DP = 72;
    private static final int ICON_SCALE_DURATION = 250;
    private static final int ICON_SIDE_MARGIN_DP = 16;
    private static final int ICON_SIZE_DP = 60;
    private static final int ICON_TRANSLATION_DURATION = 250;
    private static final int INNER_MASK_DURATION = 300;
    private static final int INNER_MASK_INIT_X_DP = 316;
    private static final int INNER_MASK_INIT_Y_DP = 72;
    private static final float INNER_MASK_MAX_SCALE = 14.0f;
    private static final int INNER_MASK_TRANSLATE_X_DP = -136;
    private static final int INNER_MASK_TRANSLATE_Y_DP = -263;
    public static final int LEFT = 0;
    private static final int OUTER_MASK_ALPHA_DURATION1 = 350;
    private static final int OUTER_MASK_ALPHA_DURATION2 = 100;
    private static final int OUTER_MASK_SCALE_DURATION = 300;
    private static final int OUTER_MASK_START_DELAY_IN_QUICK_START = 450;
    private static final int OUTER_MASK_START_DELAY_IN_SLOW_START = 250;
    private static final float OUT_MASK_FINAL_SCALE = 25.0f;
    private static final float OUT_MASK_INIT_ALPHA = 0.3f;
    private static final float OUT_MASK_INIT_SCALE = 20.0f;
    private static final float RECT_MASK_ALPHA_WHEN_ICON_ANIMATION_END = 0.3f;
    private static final int RECT_MASK_ALPHA_WITH_CIRCLE_MASK_DURATION = 450;
    private static final int RECT_MASK_ALPHA_WITH_ICON_DURATION = 350;
    public static final int RIGHT = 1;
    private static final String TAG = "HwGestureSideLayout";
    private TimeInterpolator mAccelerationInterpolator;
    private float mIconAlphaAnimationEndDistance;
    private float mIconMaxTranslateDistance;
    private ImageView mLeftIcon;
    private ImageView mLeftInnerCircleMask;
    private ImageView mRightIcon;
    private ImageView mRightInnerCircleMask;
    private TimeInterpolator mSharpInterpolator;
    private boolean mSlidingOnLeft;
    private TimeInterpolator mStandardInterpolator;

    public HwGestureSideLayout(Context context) {
        this(context, null);
    }

    public HwGestureSideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwGestureSideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HwGestureSideLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Context context2 = context;
        this.mSlidingOnLeft = true;
        float density = context.getResources().getDisplayMetrics().density;
        this.mIconMaxTranslateDistance = density * 72.0f;
        this.mIconAlphaAnimationEndDistance = (this.mIconMaxTranslateDistance * 200.0f) / 250.0f;
        this.mLeftIcon = new ImageView(context2);
        if (CHINA_REGION) {
            this.mLeftIcon.setImageResource(33752014);
        } else {
            this.mLeftIcon.setImageResource(33752015);
        }
        this.mLeftIcon.setVisibility(4);
        RelativeLayout.LayoutParams leftIconLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (density * 60.0f));
        leftIconLayoutParams.addRule(9, -1);
        leftIconLayoutParams.addRule(12, -1);
        leftIconLayoutParams.leftMargin = ((int) density) * 16;
        addView(this.mLeftIcon, leftIconLayoutParams);
        this.mRightIcon = new ImageView(context2);
        if (CHINA_REGION) {
            this.mRightIcon.setImageResource(33752012);
        } else {
            this.mRightIcon.setImageResource(33752015);
        }
        this.mRightIcon.setVisibility(4);
        RelativeLayout.LayoutParams rightIconLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (density * 60.0f));
        rightIconLayoutParams.addRule(11, -1);
        rightIconLayoutParams.addRule(12, -1);
        rightIconLayoutParams.rightMargin = ((int) density) * 16;
        addView(this.mRightIcon, rightIconLayoutParams);
        this.mRightInnerCircleMask = new ImageView(context2);
        if (CHINA_REGION) {
            this.mRightInnerCircleMask.setImageResource(33752056);
        } else {
            this.mRightInnerCircleMask.setImageResource(33752057);
        }
        this.mRightInnerCircleMask.setVisibility(4);
        this.mRightInnerCircleMask.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        RelativeLayout.LayoutParams innerMaskLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (density * 60.0f));
        innerMaskLayoutParams.addRule(11, -1);
        innerMaskLayoutParams.addRule(12, -1);
        innerMaskLayoutParams.rightMargin = ((int) density) * 16;
        this.mRightInnerCircleMask.setTranslationY((-density) * 72.0f);
        addView(this.mRightInnerCircleMask, innerMaskLayoutParams);
        this.mLeftInnerCircleMask = new ImageView(context2);
        if (CHINA_REGION) {
            this.mLeftInnerCircleMask.setImageResource(33752055);
        } else {
            this.mLeftInnerCircleMask.setImageResource(33752057);
        }
        this.mLeftInnerCircleMask.setVisibility(4);
        this.mLeftInnerCircleMask.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        RelativeLayout.LayoutParams leftInnerMaskLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (60.0f * density));
        leftInnerMaskLayoutParams.addRule(9, -1);
        leftInnerMaskLayoutParams.addRule(12, -1);
        leftInnerMaskLayoutParams.leftMargin = ((int) density) * 16;
        this.mLeftInnerCircleMask.setTranslationY((-density) * 72.0f);
        addView(this.mLeftInnerCircleMask, leftInnerMaskLayoutParams);
        this.mStandardInterpolator = AnimationUtils.loadInterpolator(context2, 17563661);
        this.mAccelerationInterpolator = AnimationUtils.loadInterpolator(context2, 17563663);
        this.mSharpInterpolator = AnimationUtils.loadInterpolator(context2, 34078724);
    }

    public void applySlowStartAnimation(float progress, int animationType, int animationTarget) {
        Log.d(TAG, "applySlowStartAnimation invoked,  progress: " + progress + " animationType: " + animationType + " animationTarget: " + animationTarget);
        ImageView icon = getTargetIcon(animationTarget);
        if (icon != null) {
            icon.setVisibility(0);
            switch (animationType) {
                case 0:
                    applySlowSwapAnimationHivoiceStyle(progress, animationTarget);
                    break;
                case 1:
                    applySlowSwapAnimationHivisionStyle(progress, animationTarget);
                    break;
            }
        }
    }

    public void onSlowStartAnimationEnd(int animationType, int animationTarget, boolean startMaskAnimation, final Runnable runnable) {
        final ImageView icon = getTargetIcon(animationTarget);
        if (startMaskAnimation) {
            AnimatorSet maskAnim = getMaskAnimationInSlowStart(animationTarget);
            if (maskAnim == null) {
                Log.w(TAG, "onSlowStartAnimationEnd: maskAnim is null");
            } else {
                maskAnim.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (runnable != null) {
                            runnable.run();
                        }
                    }

                    public void onAnimationStart(Animator animation) {
                        if (icon != null) {
                            icon.setVisibility(4);
                        }
                        super.onAnimationStart(animation);
                    }
                });
                maskAnim.start();
            }
        } else {
            if (icon != null) {
                icon.setVisibility(4);
            }
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public void applyQuickStartAnimation(int animationType, int animationTarget, Runnable runnable) {
        Log.d(TAG, "applyQuickStartAnimation begin");
        ImageView icon = getTargetIcon(animationTarget);
        if (icon != null) {
            quickStartAnimationHivisonStyle(icon, animationTarget, runnable);
        }
    }

    private void quickStartAnimationHivoiceStyle(ImageView icon, final Runnable runnable) {
        AnimatorSet iconAnim = getIconAnimatorSet(icon);
        AnimatorSet quickStartAnim = new AnimatorSet();
        quickStartAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        quickStartAnim.play(iconAnim);
        quickStartAnim.start();
    }

    private void quickStartAnimationHivisonStyle(ImageView icon, int animationTarget, final Runnable runnable) {
        ImageView innerCircleMask;
        if (animationTarget == 0) {
            innerCircleMask = this.mLeftInnerCircleMask;
        } else if (animationTarget == 1) {
            innerCircleMask = this.mRightInnerCircleMask;
        } else {
            Log.d(TAG, "quickStartAnimationHivisonStyle wrong animationTarget");
            return;
        }
        AnimatorSet iconAnim = getIconAnimatorSet(icon);
        AnimatorSet innerMaskAnimator = getInnerCircleMaskAnimatorSet(innerCircleMask);
        innerMaskAnimator.setStartDelay(250);
        AnimatorSet quickStartAnim = new AnimatorSet();
        quickStartAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        quickStartAnim.playTogether(new Animator[]{iconAnim, innerMaskAnimator});
        quickStartAnim.start();
    }

    private AnimatorSet getIconAnimatorSet(final ImageView icon) {
        ObjectAnimator iconTransitionAnim = ObjectAnimator.ofFloat(icon, "translationY", new float[]{0.0f, -this.mIconMaxTranslateDistance});
        iconTransitionAnim.setInterpolator(this.mStandardInterpolator);
        iconTransitionAnim.setDuration(250);
        ObjectAnimator iconScaleXAnim = ObjectAnimator.ofFloat(icon, "ScaleX", new float[]{0.2f, 1.0f});
        iconScaleXAnim.setInterpolator(this.mStandardInterpolator);
        iconScaleXAnim.setDuration(250);
        ObjectAnimator iconScaleYAnim = ObjectAnimator.ofFloat(icon, "ScaleY", new float[]{0.2f, 1.0f});
        iconScaleYAnim.setInterpolator(this.mStandardInterpolator);
        iconScaleYAnim.setDuration(250);
        ObjectAnimator iconAlphaAnim = ObjectAnimator.ofFloat(icon, "Alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f});
        iconAlphaAnim.setInterpolator(this.mStandardInterpolator);
        iconAlphaAnim.setDuration(200);
        AnimatorSet iconQuickSwapAnim = new AnimatorSet();
        iconQuickSwapAnim.playTogether(new Animator[]{iconTransitionAnim, iconScaleXAnim, iconScaleYAnim, iconAlphaAnim});
        iconQuickSwapAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                icon.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                icon.setVisibility(4);
            }
        });
        return iconQuickSwapAnim;
    }

    private void applySlowSwapAnimationHivoiceStyle(float progress, int animationTarget) {
        ImageView icon = getTargetIcon(animationTarget);
        if (icon != null) {
            float dy = this.mIconMaxTranslateDistance * progress;
            float scale = 0.2f + (0.8f * progress);
            float alpha = dy / this.mIconAlphaAnimationEndDistance;
            if (Float.isNaN(scale)) {
                Log.w(TAG, "scale is Float.NaN. progress = " + progress);
                return;
            }
            icon.setTranslationY(-dy);
            icon.setScaleX(scale);
            icon.setScaleY(scale);
            icon.setAlpha(alpha);
        }
    }

    private void applySlowSwapAnimationHivisionStyle(float progress, int animationTarget) {
        applySlowSwapAnimationHivoiceStyle(progress, animationTarget);
    }

    private ImageView getTargetIcon(int animationTarget) {
        if (animationTarget == 0) {
            return this.mLeftIcon;
        }
        if (animationTarget == 1) {
            return this.mRightIcon;
        }
        return null;
    }

    private AnimatorSet getMaskAnimationInSlowStart(int animationTarget) {
        ImageView innerCircleMask;
        if (animationTarget == 0) {
            innerCircleMask = this.mLeftInnerCircleMask;
        } else if (animationTarget != 1) {
            return null;
        } else {
            innerCircleMask = this.mRightInnerCircleMask;
        }
        return getInnerCircleMaskAnimatorSet(innerCircleMask);
    }

    private AnimatorSet getRectMaskAnimatorSet(int animationType, final RelativeLayout reckMask) {
        ObjectAnimator rectMaskAlphaAnimWithIcon = ObjectAnimator.ofFloat(reckMask, "Alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.3f});
        rectMaskAlphaAnimWithIcon.setDuration(350);
        rectMaskAlphaAnimWithIcon.setInterpolator(this.mStandardInterpolator);
        ObjectAnimator rectMaskAlphaAnimWithCircleMask = ObjectAnimator.ofFloat(reckMask, "Alpha", new float[]{0.3f, 1.0f});
        rectMaskAlphaAnimWithCircleMask.setDuration(450);
        rectMaskAlphaAnimWithCircleMask.setInterpolator(this.mAccelerationInterpolator);
        rectMaskAlphaAnimWithCircleMask.setStartDelay(350);
        AnimatorSet rectMaskAnimator = new AnimatorSet();
        switch (animationType) {
            case 0:
                rectMaskAnimator.play(rectMaskAlphaAnimWithIcon);
                break;
            case 1:
                rectMaskAnimator.playTogether(new Animator[]{rectMaskAlphaAnimWithIcon, rectMaskAlphaAnimWithCircleMask});
                break;
        }
        rectMaskAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                reckMask.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                reckMask.setVisibility(4);
            }
        });
        return rectMaskAnimator;
    }

    private AnimatorSet getOuterCircleMaskAnimatorSet(final ImageView outerMask) {
        ObjectAnimator outerMaskAlpha1 = ObjectAnimator.ofFloat(outerMask, "Alpha", new float[]{0.3f, 1.0f});
        outerMaskAlpha1.setDuration(350);
        outerMaskAlpha1.setInterpolator(this.mStandardInterpolator);
        ObjectAnimator outerMaskAlpha2 = ObjectAnimator.ofFloat(outerMask, "Alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
        outerMaskAlpha2.setDuration(100);
        outerMaskAlpha2.setInterpolator(this.mStandardInterpolator);
        outerMaskAlpha2.setStartDelay(350);
        ObjectAnimator outerMaskScaleX = ObjectAnimator.ofFloat(outerMask, "ScaleX", new float[]{OUT_MASK_INIT_SCALE, OUT_MASK_FINAL_SCALE});
        outerMaskScaleX.setDuration(300);
        outerMaskScaleX.setInterpolator(this.mStandardInterpolator);
        ObjectAnimator outerMaskScaleY = ObjectAnimator.ofFloat(outerMask, "ScaleY", new float[]{OUT_MASK_INIT_SCALE, OUT_MASK_FINAL_SCALE});
        outerMaskScaleY.setDuration(300);
        outerMaskScaleY.setInterpolator(this.mStandardInterpolator);
        AnimatorSet outerMaskAnimator = new AnimatorSet();
        outerMaskAnimator.playTogether(new Animator[]{outerMaskAlpha1, outerMaskAlpha2, outerMaskScaleX, outerMaskScaleY});
        outerMaskAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                outerMask.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                outerMask.setVisibility(4);
            }
        });
        return outerMaskAnimator;
    }

    private AnimatorSet getInnerCircleMaskAnimatorSet(ImageView innerMask) {
        final ImageView imageView = innerMask;
        float density = getContext().getResources().getDisplayMetrics().density;
        float transX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (imageView == this.mLeftInnerCircleMask) {
            transX = (-density) * -136.0f;
        } else if (imageView == this.mRightInnerCircleMask) {
            transX = density * -136.0f;
        }
        ObjectAnimator innerMaskAlpha = ObjectAnimator.ofFloat(imageView, "Alpha", new float[]{1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
        innerMaskAlpha.setInterpolator(this.mSharpInterpolator);
        innerMaskAlpha.setDuration(300);
        ObjectAnimator innerMaskScaleX = ObjectAnimator.ofFloat(imageView, "ScaleX", new float[]{1.0f, INNER_MASK_MAX_SCALE});
        innerMaskScaleX.setInterpolator(this.mSharpInterpolator);
        innerMaskScaleX.setDuration(300);
        ObjectAnimator innerMaskScaleY = ObjectAnimator.ofFloat(imageView, "ScaleY", new float[]{1.0f, INNER_MASK_MAX_SCALE});
        innerMaskScaleY.setInterpolator(this.mSharpInterpolator);
        innerMaskScaleY.setDuration(300);
        ObjectAnimator innerMaskTransitionX = ObjectAnimator.ofFloat(imageView, "translationX", new float[]{0.0f, transX});
        innerMaskTransitionX.setInterpolator(this.mSharpInterpolator);
        innerMaskTransitionX.setDuration(300);
        ObjectAnimator innerMaskTransitionY = ObjectAnimator.ofFloat(imageView, "translationY", new float[]{(-density) * 72.0f, -263.0f * density});
        innerMaskTransitionY.setInterpolator(this.mSharpInterpolator);
        innerMaskTransitionY.setDuration(300);
        AnimatorSet innerMaskAnimator = new AnimatorSet();
        innerMaskAnimator.playTogether(new Animator[]{innerMaskAlpha, innerMaskScaleX, innerMaskScaleY, innerMaskTransitionX, innerMaskTransitionY});
        innerMaskAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                imageView.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                imageView.setVisibility(4);
            }
        });
        return innerMaskAnimator;
    }

    public void setSlidingSide(boolean onLeft, int appType) {
        this.mSlidingOnLeft = onLeft;
        if (CHINA_REGION && !onLeft && appType == 1) {
            this.mRightIcon.setImageResource(33752014);
            this.mRightInnerCircleMask.setImageResource(33752055);
        }
    }

    public ImageView getLogo() {
        return this.mSlidingOnLeft ? this.mLeftIcon : this.mRightIcon;
    }

    public void setSlidingProcess(float process) {
        applySlowStartAnimation(process, (int) (this.mSlidingOnLeft ^ 1), (int) (this.mSlidingOnLeft ^ 1));
    }

    public void startEnterAnimation() {
        applySlowStartAnimation(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, (int) (this.mSlidingOnLeft ^ 1), (int) (this.mSlidingOnLeft ^ 1));
    }

    public void startExitAnimation(Runnable runnable, boolean isFastSlide, boolean startMaskAnimation) {
        Log.d(TAG, "startExitAnimation isFastSlide: " + isFastSlide);
        int target = this.mSlidingOnLeft ^ 1;
        int type = this.mSlidingOnLeft ^ 1;
        if (isFastSlide) {
            applyQuickStartAnimation(type, target, runnable);
            return;
        }
        onSlowStartAnimationEnd((int) type, (int) target, startMaskAnimation, runnable);
    }
}

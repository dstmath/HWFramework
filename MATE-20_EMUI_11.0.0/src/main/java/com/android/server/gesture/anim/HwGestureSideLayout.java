package com.android.server.gesture.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.Optional;

public class HwGestureSideLayout extends RelativeLayout {
    private static final String ALPHA = "Alpha";
    public static final String GOOGLE_VOICE_ASSISTANT = "com.google.android.googlequicksearchbox/com.google.android.voiceinteraction.GsaVoiceInteractionService";
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
    public static final boolean IS_CHINA_REGION = "CN".equalsIgnoreCase(SystemPropertiesEx.get("ro.product.locale.region", ""));
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
    private static final String SCALE_X = "ScaleX";
    private static final String SCALE_Y = "ScaleY";
    private static final String TAG = "HwGestureSideLayout";
    private static final String TRANSLATION_X = "translationX";
    private static final String TRANSLATION_Y = "translationY";
    private TimeInterpolator mAccelerationInterpolator;
    private float mIconAlphaAnimationEndDistance;
    private float mIconMaxTranslateDistance;
    private boolean mIsSlidingOnLeft;
    private ImageView mLeftIcon;
    private ImageView mLeftInnerCircleMask;
    private ImageView mRightIcon;
    private ImageView mRightInnerCircleMask;
    private TimeInterpolator mSharpInterpolator;
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

    public HwGestureSideLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIsSlidingOnLeft = true;
        float density = context.getResources().getDisplayMetrics().density;
        this.mIconMaxTranslateDistance = 72.0f * density;
        this.mIconAlphaAnimationEndDistance = (this.mIconMaxTranslateDistance * 200.0f) / 250.0f;
        this.mLeftIcon = new ImageView(context);
        this.mRightIcon = new ImageView(context);
        setVoiceIcon(GOOGLE_VOICE_ASSISTANT.equals(Settings.Secure.getString(getContext().getContentResolver(), "assistant")));
        this.mLeftIcon.setVisibility(4);
        configLeftIconLayoutParams(density);
        this.mRightIcon.setVisibility(4);
        configRightIconLayoutParams(density);
        this.mRightInnerCircleMask = new ImageView(context);
        if (IS_CHINA_REGION) {
            this.mRightInnerCircleMask.setImageResource(HwPartResourceUtils.getResourceId("mask_circle2"));
        } else {
            this.mRightInnerCircleMask.setImageResource(HwPartResourceUtils.getResourceId("mask_circle_oversea"));
        }
        this.mRightInnerCircleMask.setVisibility(4);
        this.mRightInnerCircleMask.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        configInnerMaskLayoutParams(density);
        this.mLeftInnerCircleMask = new ImageView(context);
        if (IS_CHINA_REGION) {
            this.mLeftInnerCircleMask.setImageResource(HwPartResourceUtils.getResourceId("mask_circle1"));
        } else {
            this.mLeftInnerCircleMask.setImageResource(HwPartResourceUtils.getResourceId("mask_circle_oversea"));
        }
        this.mLeftInnerCircleMask.setVisibility(4);
        this.mLeftInnerCircleMask.setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        configLeftInnerMaskLayoutParams(density);
        this.mStandardInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        this.mAccelerationInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mSharpInterpolator = AnimationUtils.loadInterpolator(context, HwPartResourceUtils.getResourceId("cubic_bezier_interpolator_type_33_33"));
    }

    public void setVoiceIcon(boolean isGoogleMode) {
        if (IS_CHINA_REGION) {
            this.mLeftIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice"));
            this.mRightIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice"));
        } else if (isGoogleMode) {
            this.mLeftIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice_oversea"));
            this.mRightIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice_oversea"));
        } else {
            this.mLeftIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice_oversea_app"));
            this.mRightIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice_oversea_app"));
        }
    }

    private void configLeftIconLayoutParams(float density) {
        RelativeLayout.LayoutParams leftIconLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (60.0f * density));
        leftIconLayoutParams.addRule(9, -1);
        leftIconLayoutParams.addRule(12, -1);
        leftIconLayoutParams.leftMargin = ((int) density) * 16;
        addView(this.mLeftIcon, leftIconLayoutParams);
    }

    private void configRightIconLayoutParams(float density) {
        RelativeLayout.LayoutParams rightIconLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (60.0f * density));
        rightIconLayoutParams.addRule(11, -1);
        rightIconLayoutParams.addRule(12, -1);
        rightIconLayoutParams.rightMargin = ((int) density) * 16;
        addView(this.mRightIcon, rightIconLayoutParams);
    }

    private void configInnerMaskLayoutParams(float density) {
        RelativeLayout.LayoutParams innerMaskLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (60.0f * density));
        innerMaskLayoutParams.addRule(11, -1);
        innerMaskLayoutParams.addRule(12, -1);
        innerMaskLayoutParams.rightMargin = ((int) density) * 16;
        this.mRightInnerCircleMask.setTranslationY((-density) * 72.0f);
        addView(this.mRightInnerCircleMask, innerMaskLayoutParams);
    }

    private void configLeftInnerMaskLayoutParams(float density) {
        RelativeLayout.LayoutParams leftInnerMaskLayoutParams = new RelativeLayout.LayoutParams((int) (density * 60.0f), (int) (60.0f * density));
        leftInnerMaskLayoutParams.addRule(9, -1);
        leftInnerMaskLayoutParams.addRule(12, -1);
        leftInnerMaskLayoutParams.leftMargin = ((int) density) * 16;
        this.mLeftInnerCircleMask.setTranslationY((-density) * 72.0f);
        addView(this.mLeftInnerCircleMask, leftInnerMaskLayoutParams);
    }

    public void applySlowStartAnimation(float progress, int animationType, int animationTarget) {
        Log.d(TAG, "applySlowStartAnimation invoked,  progress: " + progress + " animationType: " + animationType + " animationTarget: " + animationTarget);
        ImageView icon = getTargetIcon(animationTarget);
        if (icon != null) {
            icon.setVisibility(0);
            if (animationType == 0) {
                applySlowSwapAnimationHivoiceStyle(progress, animationTarget);
            } else if (animationType == 1) {
                applySlowSwapAnimationHivisionStyle(progress, animationTarget);
            }
        }
    }

    public void onSlowStartAnimationEnd(int animationType, int animationTarget, boolean isStartMaskAnimation, final Runnable runnable) {
        final ImageView icon = getTargetIcon(animationTarget);
        if (isStartMaskAnimation) {
            Optional<AnimatorSet> maskAnim = getMaskAnimationInSlowStart(animationTarget);
            if (!maskAnim.isPresent()) {
                Log.w(TAG, "onSlowStartAnimationEnd: maskAnim is null");
                return;
            }
            maskAnim.get().addListener(new AnimatorListenerAdapter() {
                /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass1 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    ImageView imageView = icon;
                    if (imageView != null) {
                        imageView.setVisibility(4);
                    }
                    super.onAnimationStart(animation);
                }
            });
            maskAnim.get().start();
            return;
        }
        if (icon != null) {
            icon.setVisibility(4);
        }
        if (runnable != null) {
            runnable.run();
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
            /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass2 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Runnable runnable = runnable;
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
            /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass3 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        quickStartAnim.playTogether(iconAnim, innerMaskAnimator);
        quickStartAnim.start();
    }

    private AnimatorSet getIconAnimatorSet(final ImageView icon) {
        ObjectAnimator iconTransitionAnim = ObjectAnimator.ofFloat(icon, TRANSLATION_Y, 0.0f, -this.mIconMaxTranslateDistance);
        iconTransitionAnim.setInterpolator(this.mStandardInterpolator);
        iconTransitionAnim.setDuration(250L);
        ObjectAnimator iconScaleXAnim = ObjectAnimator.ofFloat(icon, SCALE_X, ICON_INIT_SCALE, 1.0f);
        iconScaleXAnim.setInterpolator(this.mStandardInterpolator);
        iconScaleXAnim.setDuration(250L);
        ObjectAnimator iconScaleYAnim = ObjectAnimator.ofFloat(icon, SCALE_Y, ICON_INIT_SCALE, 1.0f);
        iconScaleYAnim.setInterpolator(this.mStandardInterpolator);
        iconScaleYAnim.setDuration(250L);
        ObjectAnimator iconAlphaAnim = ObjectAnimator.ofFloat(icon, ALPHA, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f);
        iconAlphaAnim.setInterpolator(this.mStandardInterpolator);
        iconAlphaAnim.setDuration(200L);
        AnimatorSet iconQuickSwapAnim = new AnimatorSet();
        iconQuickSwapAnim.playTogether(iconTransitionAnim, iconScaleXAnim, iconScaleYAnim, iconAlphaAnim);
        iconQuickSwapAnim.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass4 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                icon.setVisibility(0);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
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
            float scale = (0.8f * progress) + ICON_INIT_SCALE;
            if (Float.isNaN(scale)) {
                Log.w(TAG, "scale is Float.NaN. progress = " + progress);
                return;
            }
            icon.setTranslationY(-dy);
            icon.setScaleX(scale);
            icon.setScaleY(scale);
            icon.setAlpha(dy / this.mIconAlphaAnimationEndDistance);
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
        Log.w(TAG, "animationTarget is not LEFT or RIGHT");
        return null;
    }

    private Optional<AnimatorSet> getMaskAnimationInSlowStart(int animationTarget) {
        ImageView innerCircleMask;
        if (animationTarget == 0) {
            innerCircleMask = this.mLeftInnerCircleMask;
        } else if (animationTarget != 1) {
            return Optional.empty();
        } else {
            innerCircleMask = this.mRightInnerCircleMask;
        }
        return Optional.ofNullable(getInnerCircleMaskAnimatorSet(innerCircleMask));
    }

    private AnimatorSet getRectMaskAnimatorSet(int animationType, final RelativeLayout reckMask) {
        ObjectAnimator rectMaskAlphaAnimWithIcon = ObjectAnimator.ofFloat(reckMask, ALPHA, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.3f);
        rectMaskAlphaAnimWithIcon.setDuration(350L);
        rectMaskAlphaAnimWithIcon.setInterpolator(this.mStandardInterpolator);
        ObjectAnimator rectMaskAlphaAnimWithCircleMask = ObjectAnimator.ofFloat(reckMask, ALPHA, 0.3f, 1.0f);
        rectMaskAlphaAnimWithCircleMask.setDuration(450L);
        rectMaskAlphaAnimWithCircleMask.setInterpolator(this.mAccelerationInterpolator);
        rectMaskAlphaAnimWithCircleMask.setStartDelay(350);
        AnimatorSet rectMaskAnimator = new AnimatorSet();
        if (animationType == 0) {
            rectMaskAnimator.play(rectMaskAlphaAnimWithIcon);
        } else if (animationType == 1) {
            rectMaskAnimator.playTogether(rectMaskAlphaAnimWithIcon, rectMaskAlphaAnimWithCircleMask);
        }
        rectMaskAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass5 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                reckMask.setVisibility(0);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                reckMask.setVisibility(4);
            }
        });
        return rectMaskAnimator;
    }

    private AnimatorSet getOuterCircleMaskAnimatorSet(final ImageView outerMask) {
        ObjectAnimator outerMaskAlpha1 = ObjectAnimator.ofFloat(outerMask, ALPHA, 0.3f, 1.0f);
        outerMaskAlpha1.setDuration(350L);
        outerMaskAlpha1.setInterpolator(this.mStandardInterpolator);
        ObjectAnimator outerMaskAlpha2 = ObjectAnimator.ofFloat(outerMask, ALPHA, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        outerMaskAlpha2.setDuration(100L);
        outerMaskAlpha2.setInterpolator(this.mStandardInterpolator);
        outerMaskAlpha2.setStartDelay(350);
        ObjectAnimator outerMaskScaleX = ObjectAnimator.ofFloat(outerMask, SCALE_X, OUT_MASK_INIT_SCALE, OUT_MASK_FINAL_SCALE);
        outerMaskScaleX.setDuration(300L);
        outerMaskScaleX.setInterpolator(this.mStandardInterpolator);
        ObjectAnimator outerMaskScaleY = ObjectAnimator.ofFloat(outerMask, SCALE_Y, OUT_MASK_INIT_SCALE, OUT_MASK_FINAL_SCALE);
        outerMaskScaleY.setDuration(300L);
        outerMaskScaleY.setInterpolator(this.mStandardInterpolator);
        AnimatorSet outerMaskAnimator = new AnimatorSet();
        outerMaskAnimator.playTogether(outerMaskAlpha1, outerMaskAlpha2, outerMaskScaleX, outerMaskScaleY);
        outerMaskAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass6 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                outerMask.setVisibility(0);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                outerMask.setVisibility(4);
            }
        });
        return outerMaskAnimator;
    }

    private AnimatorSet getInnerCircleMaskAnimatorSet(final ImageView innerMask) {
        float density = getContext().getResources().getDisplayMetrics().density;
        float transX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        if (innerMask == this.mLeftInnerCircleMask) {
            transX = (-density) * -136.0f;
        } else if (innerMask == this.mRightInnerCircleMask) {
            transX = density * -136.0f;
        } else {
            Log.w(TAG, "innerMask not right");
        }
        ObjectAnimator innerMaskAlpha = ObjectAnimator.ofFloat(innerMask, ALPHA, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        innerMaskAlpha.setInterpolator(this.mSharpInterpolator);
        innerMaskAlpha.setDuration(300L);
        ObjectAnimator innerMaskScaleX = ObjectAnimator.ofFloat(innerMask, SCALE_X, 1.0f, INNER_MASK_MAX_SCALE);
        innerMaskScaleX.setInterpolator(this.mSharpInterpolator);
        innerMaskScaleX.setDuration(300L);
        ObjectAnimator innerMaskScaleY = ObjectAnimator.ofFloat(innerMask, SCALE_Y, 1.0f, INNER_MASK_MAX_SCALE);
        innerMaskScaleY.setInterpolator(this.mSharpInterpolator);
        innerMaskScaleY.setDuration(300L);
        ObjectAnimator innerMaskTransitionX = ObjectAnimator.ofFloat(innerMask, TRANSLATION_X, 0.0f, transX);
        innerMaskTransitionX.setInterpolator(this.mSharpInterpolator);
        innerMaskTransitionX.setDuration(300L);
        ObjectAnimator innerMaskTransitionY = ObjectAnimator.ofFloat(innerMask, TRANSLATION_Y, (-density) * 72.0f, -263.0f * density);
        innerMaskTransitionY.setInterpolator(this.mSharpInterpolator);
        innerMaskTransitionY.setDuration(300L);
        AnimatorSet innerMaskAnimator = new AnimatorSet();
        innerMaskAnimator.playTogether(innerMaskAlpha, innerMaskScaleX, innerMaskScaleY, innerMaskTransitionX, innerMaskTransitionY);
        innerMaskAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.server.gesture.anim.HwGestureSideLayout.AnonymousClass7 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                innerMask.setVisibility(0);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                innerMask.setVisibility(4);
            }
        });
        return innerMaskAnimator;
    }

    public void setSlidingSide(boolean isOnLeft, int appType) {
        this.mIsSlidingOnLeft = isOnLeft;
        if (IS_CHINA_REGION && !isOnLeft && appType == 1) {
            this.mRightIcon.setImageResource(HwPartResourceUtils.getResourceId("ic_hivoice"));
            this.mRightInnerCircleMask.setImageResource(HwPartResourceUtils.getResourceId("mask_circle1"));
        }
    }

    public ImageView getLogo() {
        return this.mIsSlidingOnLeft ? this.mLeftIcon : this.mRightIcon;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void setSlidingProcess(float process) {
        boolean z = this.mIsSlidingOnLeft;
        applySlowStartAnimation(process, !z, !z ? 1 : 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void startEnterAnimation() {
        boolean z = this.mIsSlidingOnLeft;
        applySlowStartAnimation(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, !z, !z ? 1 : 0);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public void startExitAnimation(Runnable runnable, boolean isFastSlide, boolean isStartMaskAnimation) {
        Log.d(TAG, "startExitAnimation isFastSlide: " + isFastSlide);
        boolean z = this.mIsSlidingOnLeft;
        int target = !z ? 1 : 0;
        int type = z ^ 1;
        if (isFastSlide) {
            applyQuickStartAnimation(type, target, runnable);
        } else {
            onSlowStartAnimationEnd(type, target, isStartMaskAnimation, runnable);
        }
    }
}

package huawei.android.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import com.huawei.anim.dynamicanimation.interpolator.SpringInterpolator;
import huawei.android.view.dynamicanimation.DynamicAnimation;
import huawei.android.view.dynamicanimation.SpringAnimation;

public class HwClickAnimationUtils {
    private static final float DAMPING_DEFAULT = 1.03f;
    private static final float DAMPING_HEAVY = 28.0f;
    private static final float DAMPING_LIGHT = 38.0f;
    private static final float DAMPING_MIDDLE = 35.0f;
    private static final float DEFAULT_SCALE_HEAVY = 0.95f;
    private static final float DEFAULT_SCALE_LIGHT = 0.9f;
    private static final float DEFAULT_SCALE_MIDDLE = 0.95f;
    public static final int EFFECT_HEAVY = 0;
    public static final int EFFECT_LIGHT = 2;
    public static final int EFFECT_MIDDLE = 1;
    private static final float STIFFNESS_DEFAULT = 1800.0f;
    private static final float STIFFNESS_HEAVY = 240.0f;
    private static final float STIFFNESS_LIGHT = 410.0f;
    private static final float STIFFNESS_MIDDLE = 350.0f;
    private static final String TAG = "HwClickAnimationUtils";
    private static final float VALUE_THRESHOLD = 0.002f;
    private static final float VELOCITY_DEFAULT = 0.0f;
    private static final float VELOCITY_HEAVY = 0.0f;
    private static final float VELOCITY_LIGHT = 1.0f;
    private static final float VELOCITY_MIDDLE = 0.5f;
    private static SpringAnimation sActionDownAnimation;
    private static SpringAnimation sActionUpAnimation;

    private HwClickAnimationUtils() {
    }

    public static void startActionDownAnimation(final View view, float minScale) {
        SpringAnimation springAnimation = sActionUpAnimation;
        if (springAnimation != null) {
            springAnimation.cancel();
            sActionUpAnimation = null;
        }
        if (view == null) {
            Log.e(TAG, "startActionDownAnimation: target view is null.");
        } else if (minScale >= 1.0f || minScale < 0.0f) {
            Log.e(TAG, "startActionDownAnimation: minScale is illegal.");
        } else {
            SpringAnimation scaleDown = new SpringAnimation(view, DynamicAnimation.SCALE_X, minScale);
            scaleDown.setStartValue(view.getScaleX());
            scaleDown.setStartVelocity(0.0f);
            scaleDown.getSpring().setDampingRatio(DAMPING_DEFAULT).setStiffness(STIFFNESS_DEFAULT);
            scaleDown.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                /* class huawei.android.view.HwClickAnimationUtils.AnonymousClass1 */

                @Override // huawei.android.view.dynamicanimation.DynamicAnimation.OnAnimationUpdateListener
                public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                    view.setScaleY(value);
                }
            });
            sActionDownAnimation = scaleDown;
            scaleDown.start();
        }
    }

    public static void startActionUpAnimation(final View view) {
        SpringAnimation springAnimation = sActionDownAnimation;
        if (springAnimation != null) {
            springAnimation.cancel();
            sActionDownAnimation = null;
        }
        if (view == null) {
            Log.e(TAG, "startActionUpAnimation: target view is null.");
            return;
        }
        SpringAnimation scaleUp = new SpringAnimation(view, DynamicAnimation.SCALE_X, 1.0f);
        scaleUp.setStartValue(view.getScaleX());
        scaleUp.setStartVelocity(0.0f);
        scaleUp.getSpring().setDampingRatio(DAMPING_DEFAULT).setStiffness(STIFFNESS_DEFAULT);
        scaleUp.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            /* class huawei.android.view.HwClickAnimationUtils.AnonymousClass2 */

            @Override // huawei.android.view.dynamicanimation.DynamicAnimation.OnAnimationUpdateListener
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                view.setScaleY(value);
            }
        });
        sActionUpAnimation = scaleUp;
        scaleUp.start();
    }

    public static AnimatorSet getActionDownAnimation(View view, int type) {
        float scale;
        if (type == 0) {
            scale = 0.95f;
        } else if (type == 1) {
            scale = 0.95f;
        } else if (type != 2) {
            scale = 0.95f;
        } else {
            scale = DEFAULT_SCALE_LIGHT;
        }
        return getSpringAnimatorSet(view, type, scale);
    }

    public static AnimatorSet getActionDownAnimation(View view, int type, float destScale) {
        return getSpringAnimatorSet(view, type, destScale);
    }

    public static AnimatorSet getActionUpAnimation(View view, int type) {
        return getSpringAnimatorSet(view, type, 1.0f);
    }

    private static AnimatorSet getSpringAnimatorSet(View view, int type, float destScale) {
        float scale;
        if (destScale > 1.0f) {
            scale = 1.0f;
        } else if (destScale < 0.0f) {
            scale = 0.0f;
        } else {
            scale = destScale;
        }
        SpringInterpolator interpolator = getSpringInterpolatorByType(view.getScaleX(), scale, type);
        long duration = (long) interpolator.getDuration();
        Animator scaleX = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), scale);
        scaleX.setInterpolator(interpolator);
        scaleX.setDuration(duration);
        Animator scaleY = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), scale);
        scaleY.setInterpolator(interpolator);
        scaleY.setDuration(duration);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        return animatorSet;
    }

    private static SpringInterpolator getSpringInterpolatorByType(float currentScale, float destScale, int type) {
        float endPosition = Math.abs(currentScale - destScale);
        if (Float.compare(endPosition, 0.0f) == 0) {
            endPosition = 0.050000012f;
        }
        if (type == 0) {
            return new SpringInterpolator((float) STIFFNESS_HEAVY, (float) DAMPING_HEAVY, endPosition, 0.0f, 0.002f);
        }
        if (type == 1) {
            return new SpringInterpolator((float) STIFFNESS_MIDDLE, (float) DAMPING_MIDDLE, endPosition, 0.5f, 0.002f);
        }
        if (type != 2) {
            return new SpringInterpolator((float) STIFFNESS_MIDDLE, (float) DAMPING_MIDDLE, endPosition, 0.5f, 0.002f);
        }
        return new SpringInterpolator((float) STIFFNESS_LIGHT, (float) DAMPING_LIGHT, endPosition, 1.0f, 0.002f);
    }
}

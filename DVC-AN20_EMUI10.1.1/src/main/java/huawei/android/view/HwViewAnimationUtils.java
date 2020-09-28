package huawei.android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.PathInterpolator;

public class HwViewAnimationUtils {
    private static final long FAB_ANIM_DURATION = 100;
    private static final float FAB_ANIM_SCALE_BIG = 2.0f;
    private static final float FALOT_CONTROL_X = 0.2f;
    private static final int INT_DICHMTOMY = 2;
    private static final int LOCATION_SIZE = 2;
    private static final long MASK_ANIM_ENTER_DURATION = 300;
    private static final long MASK_ANIM_EXIT_DURATION = 300;
    private static final long MASK_ANIM_EXIT_START_OFFSET = 50;
    private static final long REVEAL_ANIM_DURATION = 300;
    private static final String TAG = "HwViewAnimationUtils";

    private HwViewAnimationUtils() {
    }

    public static Animator createCircularRevealAnimator(boolean isEnter, View fab, View content, View mask) {
        return createCircularRevealAnimator(isEnter, fab, content, mask, -1);
    }

    public static Animator createCircularRevealAnimator(boolean isEnter, final View fab, final View content, final View mask, int maskColor) {
        AnimatorSet animatorSet;
        if (fab != null) {
            if (content != null) {
                AnimatorSet fabAnimatorSet = new AnimatorSet();
                float[] fArr = new float[2];
                fArr[0] = isEnter ? 1.0f : 0.0f;
                fArr[1] = isEnter ? 0.0f : 1.0f;
                Animator animatorFabAlpha = ObjectAnimator.ofFloat(fab, "alpha", fArr);
                float[] fArr2 = new float[2];
                float f = FAB_ANIM_SCALE_BIG;
                fArr2[0] = isEnter ? 1.0f : 2.0f;
                fArr2[1] = isEnter ? 2.0f : 1.0f;
                Animator animatorFabScaleX = ObjectAnimator.ofFloat(fab, "scaleX", fArr2);
                float[] fArr3 = new float[2];
                fArr3[0] = isEnter ? 1.0f : 2.0f;
                if (!isEnter) {
                    f = 1.0f;
                }
                fArr3[1] = f;
                Animator animatorFabScaleY = ObjectAnimator.ofFloat(fab, "scaleY", fArr3);
                fabAnimatorSet.playTogether(animatorFabAlpha, animatorFabScaleX, animatorFabScaleY);
                animatorFabAlpha.setDuration(FAB_ANIM_DURATION);
                animatorFabScaleX.setDuration(FAB_ANIM_DURATION);
                animatorFabScaleY.setDuration(FAB_ANIM_DURATION);
                if (!isEnter) {
                    fabAnimatorSet.setStartDelay(200);
                } else {
                    addIsEnterListener(fab, fabAnimatorSet);
                }
                TimeInterpolator frictionInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
                Animator revealAnimator = getAnimator(content, fab, isEnter, frictionInterpolator);
                if (!isEnter) {
                    addNonEnterListener(content, revealAnimator);
                }
                Animator maskAlphaAnimator = null;
                if (mask != null) {
                    maskAlphaAnimator = maskNonNullOperation(isEnter, mask, maskColor, frictionInterpolator);
                }
                AnimatorSet animatorSet2 = new AnimatorSet();
                if (maskAlphaAnimator != null) {
                    animatorSet2.playTogether(fabAnimatorSet, revealAnimator, maskAlphaAnimator);
                    animatorSet = animatorSet2;
                } else {
                    animatorSet = animatorSet2;
                    animatorSet.playTogether(fabAnimatorSet, revealAnimator);
                }
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    /* class huawei.android.view.HwViewAnimationUtils.AnonymousClass1 */

                    public void onAnimationStart(Animator animation) {
                        fab.setVisibility(0);
                        content.setVisibility(0);
                        View view = mask;
                        if (view != null) {
                            view.setVisibility(0);
                        }
                    }
                });
                return animatorSet;
            }
        }
        Log.w(TAG, "createCircularRevealAnimator:fab and content should not be null.");
        return null;
    }

    private static Animator getAnimator(View content, View fab, boolean isEnter, TimeInterpolator frictionInterpolator) {
        int[] contentLocation = new int[2];
        int[] fabLocation = new int[2];
        content.getLocationInWindow(contentLocation);
        fab.getLocationInWindow(fabLocation);
        int centerX = (fabLocation[0] - contentLocation[0]) + ((int) ((((float) fab.getWidth()) * fab.getScaleX()) / FAB_ANIM_SCALE_BIG));
        int centerY = (fabLocation[1] - contentLocation[1]) + ((int) ((((float) fab.getHeight()) * fab.getScaleY()) / FAB_ANIM_SCALE_BIG));
        float hypot = getMaxValue(getMaxValue(getMaxValue((float) Math.hypot((double) centerX, (double) centerY), (float) Math.hypot((double) centerX, (double) (centerY - content.getWidth()))), (float) Math.hypot((double) (centerX - content.getWidth()), (double) centerY)), (float) Math.hypot((double) (centerX - content.getWidth()), (double) (centerY - content.getHeight())));
        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(content, centerX, centerY, isEnter ? (float) (fab.getWidth() / 2) : hypot, isEnter ? hypot : (float) (fab.getWidth() / 2));
        revealAnimator.setDuration(300);
        revealAnimator.setInterpolator(frictionInterpolator);
        return revealAnimator;
    }

    private static float getMaxValue(float value1, float value2) {
        if (value1 > value2) {
            return value1;
        }
        return value2;
    }

    private static Animator maskNonNullOperation(boolean isEnter, final View mask, int maskColor, TimeInterpolator frictionInterpolator) {
        if (maskColor >= 0) {
            mask.setBackgroundColor(maskColor);
        }
        float[] fArr = new float[2];
        float f = 1.0f;
        fArr[0] = isEnter ? 1.0f : 0.0f;
        if (isEnter) {
            f = 0.0f;
        }
        fArr[1] = f;
        Animator maskAlphaAnimator = ObjectAnimator.ofFloat(mask, "alpha", fArr);
        if (isEnter) {
            maskAlphaAnimator.setDuration(300);
            maskAlphaAnimator.setInterpolator(frictionInterpolator);
        } else {
            maskAlphaAnimator.setDuration(300);
            maskAlphaAnimator.setStartDelay(MASK_ANIM_EXIT_START_OFFSET);
            maskAlphaAnimator.setInterpolator(frictionInterpolator);
        }
        if (isEnter) {
            maskAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                /* class huawei.android.view.HwViewAnimationUtils.AnonymousClass2 */

                public void onAnimationEnd(Animator animation) {
                    mask.setVisibility(4);
                }
            });
        }
        return maskAlphaAnimator;
    }

    private static void addNonEnterListener(final View content, Animator revealAnimator) {
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwViewAnimationUtils.AnonymousClass3 */

            public void onAnimationEnd(Animator animation) {
                content.setVisibility(4);
            }
        });
    }

    private static void addIsEnterListener(final View fab, AnimatorSet fabAnimatorSet) {
        fabAnimatorSet.addListener(new AnimatorListenerAdapter() {
            /* class huawei.android.view.HwViewAnimationUtils.AnonymousClass4 */

            public void onAnimationEnd(Animator animation) {
                fab.setVisibility(4);
            }
        });
    }
}

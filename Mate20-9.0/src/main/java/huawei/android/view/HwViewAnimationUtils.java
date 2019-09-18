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
    private static final long MASK_ANIM_ENTER_DURATION = 300;
    private static final long MASK_ANIM_EXIT_DURATION = 300;
    private static final long MASK_ANIM_EXIT_START_OFFSET = 50;
    private static final long REVEAL_ANIM_DURATION = 300;
    private static final String TAG = "HwViewAnimationUtils";

    public static Animator createCircularRevealAnimator(boolean isEnter, View fab, View content, View mask) {
        return createCircularRevealAnimator(isEnter, fab, content, mask, -1);
    }

    public static Animator createCircularRevealAnimator(boolean isEnter, View fab, View content, View mask, int maskColor) {
        int i;
        float startRadius;
        Animator maskAlphaAnimator;
        final View view = fab;
        final View view2 = content;
        final View view3 = mask;
        if (view == null || view2 == null) {
            Log.w(TAG, "createCircularRevealAnimator:  fab and content should not be null.");
            return null;
        }
        TimeInterpolator frictionInterpolator = new PathInterpolator(0.2f, 0.0f, 0.2f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSet fabAnimatorSet = new AnimatorSet();
        float[] fArr = new float[2];
        fArr[0] = isEnter ? 1.0f : 0.0f;
        fArr[1] = isEnter ? 0.0f : 1.0f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "alpha", fArr);
        float[] fArr2 = new float[2];
        fArr2[0] = isEnter ? 1.0f : FAB_ANIM_SCALE_BIG;
        fArr2[1] = isEnter ? FAB_ANIM_SCALE_BIG : 1.0f;
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, "scaleX", fArr2);
        float[] fArr3 = new float[2];
        fArr3[0] = isEnter ? 1.0f : FAB_ANIM_SCALE_BIG;
        fArr3[1] = isEnter ? FAB_ANIM_SCALE_BIG : 1.0f;
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(view, "scaleY", fArr3);
        fabAnimatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3});
        ofFloat.setDuration(FAB_ANIM_DURATION);
        ofFloat2.setDuration(FAB_ANIM_DURATION);
        ofFloat3.setDuration(FAB_ANIM_DURATION);
        if (!isEnter) {
            fabAnimatorSet.setStartDelay(200);
        }
        if (isEnter) {
            fabAnimatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(4);
                }
            });
        }
        int[] contentLocation = new int[2];
        int[] fabLocation = new int[2];
        view2.getLocationInWindow(contentLocation);
        view.getLocationInWindow(fabLocation);
        int centerX = (fabLocation[0] - contentLocation[0]) + ((int) ((((float) fab.getWidth()) * fab.getScaleX()) / FAB_ANIM_SCALE_BIG));
        int centerY = ((int) ((((float) fab.getHeight()) * fab.getScaleY()) / FAB_ANIM_SCALE_BIG)) + (fabLocation[1] - contentLocation[1]);
        int[] iArr = fabLocation;
        ObjectAnimator objectAnimator = ofFloat2;
        ObjectAnimator objectAnimator2 = ofFloat3;
        int[] iArr2 = contentLocation;
        float hypot2 = (float) Math.hypot((double) centerX, (double) (centerY - content.getWidth()));
        AnimatorSet fabAnimatorSet2 = fabAnimatorSet;
        float hypot3 = (float) Math.hypot((double) (centerX - content.getWidth()), (double) centerY);
        ObjectAnimator objectAnimator3 = ofFloat;
        float hypot4 = (float) Math.hypot((double) (centerX - content.getWidth()), (double) (centerY - content.getHeight()));
        float hypot = Math.max(Math.max(Math.max((float) Math.hypot((double) centerX, (double) centerY), hypot2), hypot3), hypot4);
        if (isEnter) {
            i = 2;
            startRadius = (float) (fab.getWidth() / 2);
        } else {
            i = 2;
            startRadius = hypot;
        }
        Animator revealAnimator = ViewAnimationUtils.createCircularReveal(view2, centerX, centerY, startRadius, isEnter ? hypot : (float) (fab.getWidth() / i));
        float f = hypot2;
        float f2 = hypot3;
        revealAnimator.setDuration(300);
        revealAnimator.setInterpolator(frictionInterpolator);
        if (!isEnter) {
            revealAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    view2.setVisibility(4);
                }
            });
        }
        if (view3 != null) {
            if (maskColor >= 0) {
                mask.setBackgroundColor(maskColor);
            }
            float f3 = hypot4;
            float[] fArr4 = new float[2];
            fArr4[0] = isEnter ? 1.0f : 0.0f;
            fArr4[1] = isEnter ? 0.0f : 1.0f;
            Animator maskAlphaAnimator2 = ObjectAnimator.ofFloat(view3, "alpha", fArr4);
            if (isEnter) {
                maskAlphaAnimator2.setDuration(300);
                maskAlphaAnimator2.setInterpolator(frictionInterpolator);
            } else {
                maskAlphaAnimator2.setDuration(300);
                maskAlphaAnimator2.setStartDelay(MASK_ANIM_EXIT_START_OFFSET);
                maskAlphaAnimator2.setInterpolator(frictionInterpolator);
            }
            if (isEnter) {
                maskAlphaAnimator2.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        view3.setVisibility(4);
                    }
                });
            }
            maskAlphaAnimator = maskAlphaAnimator2;
        } else {
            maskAlphaAnimator = null;
            float f4 = hypot4;
        }
        if (maskAlphaAnimator != null) {
            animatorSet.playTogether(new Animator[]{fabAnimatorSet2, revealAnimator, maskAlphaAnimator});
        } else {
            animatorSet.playTogether(new Animator[]{fabAnimatorSet2, revealAnimator});
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                view.setVisibility(0);
                view2.setVisibility(0);
                if (view3 != null) {
                    view3.setVisibility(0);
                }
            }
        });
        return animatorSet;
    }
}

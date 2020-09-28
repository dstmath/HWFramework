package huawei.android.view;

import android.util.Log;
import android.view.View;
import huawei.android.view.dynamicanimation.DynamicAnimation;
import huawei.android.view.dynamicanimation.SpringAnimation;

public class HwClickAnimationUtils {
    private static final float DAMPING_DEFAULT = 1.03f;
    private static final float STIFFNESS_DEFAULT = 1800.0f;
    private static final String TAG = "HwClickAnimationUtils";
    private static final float VELOCITY_DEFAULT = 0.0f;
    private static SpringAnimation mActionDownAnimation;
    private static SpringAnimation mActionUpAnimation;

    private HwClickAnimationUtils() {
    }

    public static void startActionDownAnimation(final View view, float minScale) {
        SpringAnimation springAnimation = mActionUpAnimation;
        if (springAnimation != null) {
            springAnimation.cancel();
            mActionUpAnimation = null;
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
            mActionDownAnimation = scaleDown;
            scaleDown.start();
        }
    }

    public static void startActionUpAnimation(final View view) {
        SpringAnimation springAnimation = mActionDownAnimation;
        if (springAnimation != null) {
            springAnimation.cancel();
            mActionDownAnimation = null;
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
        mActionUpAnimation = scaleUp;
        scaleUp.start();
    }
}

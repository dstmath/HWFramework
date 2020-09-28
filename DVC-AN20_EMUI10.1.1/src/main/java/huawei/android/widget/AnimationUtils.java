package huawei.android.widget;

import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import huawei.android.widget.appbar.FastOutLinearInInterpolator;
import huawei.android.widget.appbar.LinearOutSlowInInterpolator;

public class AnimationUtils {
    public static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    public static final Interpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final Interpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();

    private AnimationUtils() {
    }

    public static float lerp(float startValue, float endValue, float fraction) {
        return ((endValue - startValue) * fraction) + startValue;
    }

    public static int lerp(int startValue, int endValue, float fraction) {
        return Math.round(((float) (endValue - startValue)) * fraction) + startValue;
    }
}

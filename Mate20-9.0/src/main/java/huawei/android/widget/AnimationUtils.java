package huawei.android.widget;

import android.view.animation.Interpolator;

public class AnimationUtils {
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    static float lerp(float startValue, float endValue, float fraction) {
        return ((endValue - startValue) * fraction) + startValue;
    }

    static int lerp(int startValue, int endValue, float fraction) {
        return Math.round(((float) (endValue - startValue)) * fraction) + startValue;
    }
}

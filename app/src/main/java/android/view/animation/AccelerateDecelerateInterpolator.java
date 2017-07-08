package android.view.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager.LayoutParams;
import com.android.internal.view.animation.HasNativeInterpolator;
import com.android.internal.view.animation.NativeInterpolatorFactory;
import com.android.internal.view.animation.NativeInterpolatorFactoryHelper;

@HasNativeInterpolator
public class AccelerateDecelerateInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    public AccelerateDecelerateInterpolator(Context context, AttributeSet attrs) {
    }

    public float getInterpolation(float input) {
        return ((float) (Math.cos(((double) (LayoutParams.BRIGHTNESS_OVERRIDE_FULL + input)) * 3.141592653589793d) / 2.0d)) + 0.5f;
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createAccelerateDecelerateInterpolator();
    }
}

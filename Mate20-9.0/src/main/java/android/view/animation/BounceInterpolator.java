package android.view.animation;

import android.content.Context;
import android.util.AttributeSet;
import com.android.internal.view.animation.HasNativeInterpolator;
import com.android.internal.view.animation.NativeInterpolatorFactory;
import com.android.internal.view.animation.NativeInterpolatorFactoryHelper;

@HasNativeInterpolator
public class BounceInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    public BounceInterpolator() {
    }

    public BounceInterpolator(Context context, AttributeSet attrs) {
    }

    private static float bounce(float t) {
        return t * t * 8.0f;
    }

    public float getInterpolation(float t) {
        float t2 = t * 1.1226f;
        if (t2 < 0.3535f) {
            return bounce(t2);
        }
        if (t2 < 0.7408f) {
            return bounce(t2 - 0.54719f) + 0.7f;
        }
        if (t2 < 0.9644f) {
            return bounce(t2 - 0.8526f) + 0.9f;
        }
        return bounce(t2 - 1.0435f) + 0.95f;
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createBounceInterpolator();
    }
}

package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.WindowManager.LayoutParams;
import com.android.internal.R;
import com.android.internal.view.animation.HasNativeInterpolator;
import com.android.internal.view.animation.NativeInterpolatorFactory;
import com.android.internal.view.animation.NativeInterpolatorFactoryHelper;

@HasNativeInterpolator
public class DecelerateInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    private float mFactor;

    public DecelerateInterpolator() {
        this.mFactor = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
    }

    public DecelerateInterpolator(float factor) {
        this.mFactor = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mFactor = factor;
    }

    public DecelerateInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public DecelerateInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        this.mFactor = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.DecelerateInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.DecelerateInterpolator);
        }
        this.mFactor = a.getFloat(0, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        setChangingConfiguration(a.getChangingConfigurations());
        a.recycle();
    }

    public float getInterpolation(float input) {
        if (this.mFactor == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return LayoutParams.BRIGHTNESS_OVERRIDE_FULL - ((LayoutParams.BRIGHTNESS_OVERRIDE_FULL - input) * (LayoutParams.BRIGHTNESS_OVERRIDE_FULL - input));
        }
        return (float) (1.0d - Math.pow((double) (LayoutParams.BRIGHTNESS_OVERRIDE_FULL - input), (double) (this.mFactor * 2.0f)));
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createDecelerateInterpolator(this.mFactor);
    }
}

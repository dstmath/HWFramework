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
public class AccelerateInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    private final double mDoubleFactor;
    private final float mFactor;

    public AccelerateInterpolator() {
        this.mFactor = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        this.mDoubleFactor = 2.0d;
    }

    public AccelerateInterpolator(float factor) {
        this.mFactor = factor;
        this.mDoubleFactor = (double) (this.mFactor * 2.0f);
    }

    public AccelerateInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public AccelerateInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.AccelerateInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.AccelerateInterpolator);
        }
        this.mFactor = a.getFloat(0, LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        this.mDoubleFactor = (double) (this.mFactor * 2.0f);
        setChangingConfiguration(a.getChangingConfigurations());
        a.recycle();
    }

    public float getInterpolation(float input) {
        if (this.mFactor == LayoutParams.BRIGHTNESS_OVERRIDE_FULL) {
            return input * input;
        }
        return (float) Math.pow((double) input, this.mDoubleFactor);
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createAccelerateInterpolator(this.mFactor);
    }
}

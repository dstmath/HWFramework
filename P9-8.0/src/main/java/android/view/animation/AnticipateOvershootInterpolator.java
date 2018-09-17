package android.view.animation;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.internal.R;
import com.android.internal.view.animation.HasNativeInterpolator;
import com.android.internal.view.animation.NativeInterpolatorFactory;
import com.android.internal.view.animation.NativeInterpolatorFactoryHelper;

@HasNativeInterpolator
public class AnticipateOvershootInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    private final float mTension;

    public AnticipateOvershootInterpolator() {
        this.mTension = 3.0f;
    }

    public AnticipateOvershootInterpolator(float tension) {
        this.mTension = 1.5f * tension;
    }

    public AnticipateOvershootInterpolator(float tension, float extraTension) {
        this.mTension = tension * extraTension;
    }

    public AnticipateOvershootInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public AnticipateOvershootInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.AnticipateOvershootInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.AnticipateOvershootInterpolator);
        }
        this.mTension = a.getFloat(0, 2.0f) * a.getFloat(1, 1.5f);
        setChangingConfiguration(a.getChangingConfigurations());
        a.recycle();
    }

    private static float a(float t, float s) {
        return (t * t) * (((1.0f + s) * t) - s);
    }

    private static float o(float t, float s) {
        return (t * t) * (((1.0f + s) * t) + s);
    }

    public float getInterpolation(float t) {
        if (t < 0.5f) {
            return a(t * 2.0f, this.mTension) * 0.5f;
        }
        return (o((t * 2.0f) - 2.0f, this.mTension) + 2.0f) * 0.5f;
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createAnticipateOvershootInterpolator(this.mTension);
    }
}

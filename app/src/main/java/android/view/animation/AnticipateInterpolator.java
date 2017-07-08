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
public class AnticipateInterpolator extends BaseInterpolator implements NativeInterpolatorFactory {
    private final float mTension;

    public AnticipateInterpolator() {
        this.mTension = 2.0f;
    }

    public AnticipateInterpolator(float tension) {
        this.mTension = tension;
    }

    public AnticipateInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    public AnticipateInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.AnticipateInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.AnticipateInterpolator);
        }
        this.mTension = a.getFloat(0, 2.0f);
        setChangingConfiguration(a.getChangingConfigurations());
        a.recycle();
    }

    public float getInterpolation(float t) {
        return (t * t) * (((this.mTension + LayoutParams.BRIGHTNESS_OVERRIDE_FULL) * t) - this.mTension);
    }

    public long createNativeInterpolator() {
        return NativeInterpolatorFactoryHelper.createAnticipateInterpolator(this.mTension);
    }
}

package android.graphics.drawable;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.FloatProperty;
import android.view.animation.LinearInterpolator;

/* access modifiers changed from: package-private */
public class RippleBackground extends RippleComponent {
    private static final TimeInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final BackgroundProperty OPACITY = new BackgroundProperty("opacity") {
        /* class android.graphics.drawable.RippleBackground.AnonymousClass1 */

        public void setValue(RippleBackground object, float value) {
            object.mOpacity = value;
            object.invalidateSelf();
        }

        public Float get(RippleBackground object) {
            return Float.valueOf(object.mOpacity);
        }
    };
    private static final int OPACITY_DURATION = 80;
    private ObjectAnimator mAnimator;
    private boolean mFocused = false;
    private boolean mHovered = false;
    private boolean mIsBounded;
    private float mOpacity = 0.0f;

    public RippleBackground(RippleDrawable owner, Rect bounds, boolean isBounded) {
        super(owner, bounds);
        this.mIsBounded = isBounded;
    }

    public boolean isVisible() {
        return this.mOpacity > 0.0f;
    }

    public void draw(Canvas c, Paint p) {
        int origAlpha = p.getAlpha();
        int alpha = Math.min((int) ((((float) origAlpha) * this.mOpacity) + 0.5f), 255);
        if (alpha > 0) {
            p.setAlpha(alpha);
            c.drawCircle(0.0f, 0.0f, this.mTargetRadius, p);
            p.setAlpha(origAlpha);
        }
    }

    public void setState(boolean focused, boolean hovered, boolean pressed) {
        boolean z = true;
        if (!this.mFocused) {
            focused = focused && !pressed;
        }
        if (!this.mHovered) {
            if (!hovered || pressed) {
                z = false;
            }
            hovered = z;
        }
        if (this.mHovered != hovered || this.mFocused != focused) {
            this.mHovered = hovered;
            this.mFocused = focused;
            onStateChanged();
        }
    }

    private void onStateChanged() {
        float newOpacity = this.mFocused ? 0.6f : this.mHovered ? 0.2f : 0.0f;
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
            this.mAnimator = null;
        }
        this.mAnimator = ObjectAnimator.ofFloat(this, OPACITY, newOpacity);
        this.mAnimator.setDuration(80L);
        this.mAnimator.setInterpolator(LINEAR_INTERPOLATOR);
        this.mAnimator.start();
    }

    public void jumpToFinal() {
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.end();
            this.mAnimator = null;
        }
    }

    /* access modifiers changed from: private */
    public static abstract class BackgroundProperty extends FloatProperty<RippleBackground> {
        public BackgroundProperty(String name) {
            super(name);
        }
    }
}

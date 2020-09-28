package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;

public class TransitionDrawable extends LayerDrawable implements Drawable.Callback {
    private static final int TRANSITION_NONE = 2;
    private static final int TRANSITION_RUNNING = 1;
    private static final int TRANSITION_STARTING = 0;
    @UnsupportedAppUsage
    private int mAlpha;
    @UnsupportedAppUsage
    private boolean mCrossFade;
    private int mDuration;
    private int mFrom;
    private int mOriginalDuration;
    private boolean mReverse;
    private long mStartTimeMillis;
    @UnsupportedAppUsage
    private int mTo;
    private int mTransitionState;

    public TransitionDrawable(Drawable[] layers) {
        this(new TransitionState(null, null, null), layers);
    }

    TransitionDrawable() {
        this(new TransitionState(null, null, null), (Resources) null);
    }

    private TransitionDrawable(TransitionState state, Resources res) {
        super(state, res);
        this.mTransitionState = 2;
        this.mAlpha = 0;
    }

    private TransitionDrawable(TransitionState state, Drawable[] layers) {
        super(layers, state);
        this.mTransitionState = 2;
        this.mAlpha = 0;
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.LayerDrawable
    public LayerDrawable.LayerState createConstantState(LayerDrawable.LayerState state, Resources res) {
        return new TransitionState((TransitionState) state, this, res);
    }

    public void startTransition(int durationMillis) {
        this.mFrom = 0;
        this.mTo = 255;
        this.mAlpha = 0;
        this.mOriginalDuration = durationMillis;
        this.mDuration = durationMillis;
        this.mReverse = false;
        this.mTransitionState = 0;
        invalidateSelf();
    }

    public void showSecondLayer() {
        this.mAlpha = 255;
        this.mReverse = false;
        this.mTransitionState = 2;
        invalidateSelf();
    }

    public void resetTransition() {
        this.mAlpha = 0;
        this.mTransitionState = 2;
        invalidateSelf();
    }

    public void reverseTransition(int duration) {
        long j;
        long time = SystemClock.uptimeMillis();
        int i = 255;
        if (time - this.mStartTimeMillis > ((long) this.mDuration)) {
            if (this.mTo == 0) {
                this.mFrom = 0;
                this.mTo = 255;
                this.mAlpha = 0;
                this.mReverse = false;
            } else {
                this.mFrom = 255;
                this.mTo = 0;
                this.mAlpha = 255;
                this.mReverse = true;
            }
            this.mOriginalDuration = duration;
            this.mDuration = duration;
            this.mTransitionState = 0;
            invalidateSelf();
            return;
        }
        this.mReverse = !this.mReverse;
        this.mFrom = this.mAlpha;
        if (this.mReverse) {
            i = 0;
        }
        this.mTo = i;
        if (this.mReverse) {
            j = time - this.mStartTimeMillis;
        } else {
            j = ((long) this.mOriginalDuration) - (time - this.mStartTimeMillis);
        }
        this.mDuration = (int) j;
        this.mTransitionState = 0;
    }

    @Override // android.graphics.drawable.LayerDrawable, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        boolean done = true;
        int i = this.mTransitionState;
        if (i == 0) {
            this.mStartTimeMillis = SystemClock.uptimeMillis();
            done = false;
            this.mTransitionState = 1;
        } else if (i == 1 && this.mStartTimeMillis >= 0) {
            float normalized = ((float) (SystemClock.uptimeMillis() - this.mStartTimeMillis)) / ((float) this.mDuration);
            done = normalized >= 1.0f;
            float normalized2 = Math.min(normalized, 1.0f);
            int i2 = this.mFrom;
            this.mAlpha = (int) (((float) i2) + (((float) (this.mTo - i2)) * normalized2));
        }
        int alpha = this.mAlpha;
        boolean crossFade = this.mCrossFade;
        LayerDrawable.ChildDrawable[] array = this.mLayerState.mChildren;
        if (done) {
            if (!crossFade || alpha == 0) {
                array[0].mDrawable.draw(canvas);
            }
            if (alpha == 255) {
                array[1].mDrawable.draw(canvas);
                return;
            }
            return;
        }
        Drawable d = array[0].mDrawable;
        if (crossFade) {
            d.setAlpha(255 - alpha);
        }
        d.draw(canvas);
        if (crossFade) {
            d.setAlpha(255);
        }
        if (alpha > 0) {
            Drawable d2 = array[1].mDrawable;
            d2.setAlpha(alpha);
            d2.draw(canvas);
            d2.setAlpha(255);
        }
        if (!done) {
            invalidateSelf();
        }
    }

    public void setCrossFadeEnabled(boolean enabled) {
        this.mCrossFade = enabled;
    }

    public boolean isCrossFadeEnabled() {
        return this.mCrossFade;
    }

    static class TransitionState extends LayerDrawable.LayerState {
        TransitionState(TransitionState orig, TransitionDrawable owner, Resources res) {
            super(orig, owner, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public Drawable newDrawable() {
            return new TransitionDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public Drawable newDrawable(Resources res) {
            return new TransitionDrawable(this, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.LayerDrawable.LayerState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations;
        }
    }
}

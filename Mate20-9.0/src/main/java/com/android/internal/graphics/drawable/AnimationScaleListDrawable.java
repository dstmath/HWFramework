package com.android.internal.graphics.drawable;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimationScaleListDrawable extends DrawableContainer implements Animatable {
    private static final String TAG = "AnimationScaleListDrawable";
    private AnimationScaleListState mAnimationScaleListState;
    private boolean mMutated;

    static class AnimationScaleListState extends DrawableContainer.DrawableContainerState {
        int mAnimatableDrawableIndex = -1;
        int mStaticDrawableIndex = -1;
        int[] mThemeAttrs = null;

        AnimationScaleListState(AnimationScaleListState orig, AnimationScaleListDrawable owner, Resources res) {
            super(orig, owner, res);
            if (orig != null) {
                this.mThemeAttrs = orig.mThemeAttrs;
                this.mStaticDrawableIndex = orig.mStaticDrawableIndex;
                this.mAnimatableDrawableIndex = orig.mAnimatableDrawableIndex;
            }
        }

        /* access modifiers changed from: package-private */
        public void mutate() {
            this.mThemeAttrs = this.mThemeAttrs != null ? (int[]) this.mThemeAttrs.clone() : null;
        }

        /* access modifiers changed from: package-private */
        public int addDrawable(Drawable drawable) {
            int pos = addChild(drawable);
            if (drawable instanceof Animatable) {
                this.mAnimatableDrawableIndex = pos;
            } else {
                this.mStaticDrawableIndex = pos;
            }
            return pos;
        }

        public Drawable newDrawable() {
            return new AnimationScaleListDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new AnimationScaleListDrawable(this, res);
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs != null || super.canApplyTheme();
        }

        public int getCurrentDrawableIndexBasedOnScale() {
            if (ValueAnimator.getDurationScale() == 0.0f) {
                return this.mStaticDrawableIndex;
            }
            return this.mAnimatableDrawableIndex;
        }
    }

    public AnimationScaleListDrawable() {
        this(null, null);
    }

    private AnimationScaleListDrawable(AnimationScaleListState state, Resources res) {
        setConstantState(new AnimationScaleListState(state, this, res));
        onStateChange(getState());
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] stateSet) {
        return selectDrawable(this.mAnimationScaleListState.getCurrentDrawableIndexBasedOnScale()) || super.onStateChange(stateSet);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationScaleListDrawable);
        updateDensity(r);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        onStateChange(getState());
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int next;
        int type;
        AnimationScaleListState state = this.mAnimationScaleListState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int next2 = parser.next();
            int type2 = next2;
            if (next2 != 1) {
                int depth = parser.getDepth();
                int depth2 = depth;
                if (depth < innerDepth && type2 == 3) {
                    return;
                }
                if (type2 == 2 && depth2 <= innerDepth && parser.getName().equals("item")) {
                    TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationScaleListDrawableItem);
                    Drawable dr = a.getDrawable(0);
                    a.recycle();
                    if (dr == null) {
                        do {
                            next = parser.next();
                            type = next;
                        } while (next == 4);
                        if (type == 2) {
                            dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
                        } else {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or child tag defining a drawable");
                        }
                    }
                    state.addDrawable(dr);
                }
            } else {
                return;
            }
        }
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mAnimationScaleListState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    public void start() {
        Drawable dr = getCurrent();
        if (dr != null && (dr instanceof Animatable)) {
            ((Animatable) dr).start();
        }
    }

    public void stop() {
        Drawable dr = getCurrent();
        if (dr != null && (dr instanceof Animatable)) {
            ((Animatable) dr).stop();
        }
    }

    public boolean isRunning() {
        Drawable dr = getCurrent();
        if (dr == null || !(dr instanceof Animatable)) {
            return false;
        }
        return ((Animatable) dr).isRunning();
    }

    public void applyTheme(Resources.Theme theme) {
        super.applyTheme(theme);
        onStateChange(getState());
    }

    /* access modifiers changed from: protected */
    public void setConstantState(DrawableContainer.DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof AnimationScaleListState) {
            this.mAnimationScaleListState = (AnimationScaleListState) state;
        }
    }
}

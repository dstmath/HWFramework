package com.android.internal.graphics.drawable;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.DrawableContainer.DrawableContainerState;
import android.util.AttributeSet;
import com.android.ims.ImsConfig;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimationScaleListDrawable extends DrawableContainer implements Animatable {
    private static final String TAG = "AnimationScaleListDrawable";
    private AnimationScaleListState mAnimationScaleListState;
    private boolean mMutated;

    static class AnimationScaleListState extends DrawableContainerState {
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

        void mutate() {
            int[] iArr = null;
            if (this.mThemeAttrs != null) {
                iArr = (int[]) this.mThemeAttrs.clone();
            }
            this.mThemeAttrs = iArr;
        }

        int addDrawable(Drawable drawable) {
            int pos = addChild(drawable);
            if (drawable instanceof Animatable) {
                this.mAnimatableDrawableIndex = pos;
            } else {
                this.mStaticDrawableIndex = pos;
            }
            return pos;
        }

        public Drawable newDrawable() {
            return new AnimationScaleListDrawable(this, null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new AnimationScaleListDrawable(this, res, null);
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs == null ? super.canApplyTheme() : true;
        }

        public int getCurrentDrawableIndexBasedOnScale() {
            if (ValueAnimator.getDurationScale() == 0.0f) {
                return this.mStaticDrawableIndex;
            }
            return this.mAnimatableDrawableIndex;
        }
    }

    /* synthetic */ AnimationScaleListDrawable(AnimationScaleListState state, Resources res, AnimationScaleListDrawable -this2) {
        this(state, res);
    }

    public AnimationScaleListDrawable() {
        this(null, null);
    }

    private AnimationScaleListDrawable(AnimationScaleListState state, Resources res) {
        setConstantState(new AnimationScaleListState(state, this, res));
        onStateChange(getState());
    }

    protected boolean onStateChange(int[] stateSet) {
        return !selectDrawable(this.mAnimationScaleListState.getCurrentDrawableIndexBasedOnScale()) ? super.onStateChange(stateSet) : true;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationScaleListDrawable);
        updateDensity(r);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        onStateChange(getState());
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        AnimationScaleListState state = this.mAnimationScaleListState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (type == 2 && depth <= innerDepth && (parser.getName().equals(ImsConfig.EXTRA_CHANGED_ITEM) ^ 1) == 0) {
                    TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationScaleListDrawableItem);
                    Drawable dr = a.getDrawable(0);
                    a.recycle();
                    if (dr == null) {
                        do {
                            type = parser.next();
                        } while (type == 4);
                        if (type != 2) {
                            throw new XmlPullParserException(parser.getPositionDescription() + ": <item> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
                        }
                        dr = Drawable.createFromXmlInner(r, parser, attrs, theme);
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

    public void applyTheme(Theme theme) {
        super.applyTheme(theme);
        onStateChange(getState());
    }

    protected void setConstantState(DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof AnimationScaleListState) {
            this.mAnimationScaleListState = (AnimationScaleListState) state;
        }
    }
}

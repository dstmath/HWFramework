package com.android.internal.graphics.drawable;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
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

    public AnimationScaleListDrawable() {
        this(null, null);
    }

    private AnimationScaleListDrawable(AnimationScaleListState state, Resources res) {
        setConstantState(new AnimationScaleListState(state, this, res));
        onStateChange(getState());
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        return selectDrawable(this.mAnimationScaleListState.getCurrentDrawableIndexBasedOnScale()) || super.onStateChange(stateSet);
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationScaleListDrawable);
        updateDensity(r);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        onStateChange(getState());
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int type;
        AnimationScaleListState state = this.mAnimationScaleListState;
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type2 = parser.next();
            if (type2 != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type2 == 3) {
                    return;
                }
                if (type2 == 2 && depth <= innerDepth && parser.getName().equals(ImsConfig.EXTRA_CHANGED_ITEM)) {
                    TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimationScaleListDrawableItem);
                    Drawable dr = a.getDrawable(0);
                    a.recycle();
                    if (dr == null) {
                        do {
                            type = parser.next();
                        } while (type == 4);
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

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mAnimationScaleListState.mutate();
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    @Override // android.graphics.drawable.Animatable
    public void start() {
        Drawable dr = getCurrent();
        if (dr != null && (dr instanceof Animatable)) {
            ((Animatable) dr).start();
        }
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        Drawable dr = getCurrent();
        if (dr != null && (dr instanceof Animatable)) {
            ((Animatable) dr).stop();
        }
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        Drawable dr = getCurrent();
        if (dr == null || !(dr instanceof Animatable)) {
            return false;
        }
        return ((Animatable) dr).isRunning();
    }

    /* access modifiers changed from: package-private */
    public static class AnimationScaleListState extends DrawableContainer.DrawableContainerState {
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
            int[] iArr = this.mThemeAttrs;
            this.mThemeAttrs = iArr != null ? (int[]) iArr.clone() : null;
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

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new AnimationScaleListDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new AnimationScaleListDrawable(this, res);
        }

        @Override // android.graphics.drawable.DrawableContainer.DrawableContainerState, android.graphics.drawable.Drawable.ConstantState
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

    @Override // android.graphics.drawable.DrawableContainer, android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme theme) {
        super.applyTheme(theme);
        onStateChange(getState());
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableContainer
    public void setConstantState(DrawableContainer.DrawableContainerState state) {
        super.setConstantState(state);
        if (state instanceof AnimationScaleListState) {
            this.mAnimationScaleListState = (AnimationScaleListState) state;
        }
    }
}

package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.MathUtils;

public class ColorStateListDrawable extends Drawable implements Drawable.Callback {
    private ColorDrawable mColorDrawable;
    private boolean mMutated;
    private ColorStateListDrawableState mState;

    public ColorStateListDrawable() {
        this.mMutated = false;
        this.mState = new ColorStateListDrawableState();
        initializeColorDrawable();
    }

    public ColorStateListDrawable(ColorStateList colorStateList) {
        this.mMutated = false;
        this.mState = new ColorStateListDrawableState();
        initializeColorDrawable();
        setColorStateList(colorStateList);
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        this.mColorDrawable.draw(canvas);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mColorDrawable.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return this.mState.isStateful();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean hasFocusStateSpecified() {
        return this.mState.hasFocusStateSpecified();
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable getCurrent() {
        return this.mColorDrawable;
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        if (this.mState.mColor != null) {
            setColorStateList(this.mState.mColor.obtainForTheme(t));
        }
        if (this.mState.mTint != null) {
            setTintList(this.mState.mTint.obtainForTheme(t));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        return super.canApplyTheme() || this.mState.canApplyTheme();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        this.mState.mAlpha = alpha;
        onStateChange(getState());
    }

    public void clearAlpha() {
        this.mState.mAlpha = -1;
        onStateChange(getState());
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        this.mState.mTint = tint;
        this.mColorDrawable.setTintList(tint);
        onStateChange(getState());
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintBlendMode(BlendMode blendMode) {
        this.mState.mBlendMode = blendMode;
        this.mColorDrawable.setTintBlendMode(blendMode);
        onStateChange(getState());
    }

    @Override // android.graphics.drawable.Drawable
    public ColorFilter getColorFilter() {
        return this.mColorDrawable.getColorFilter();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mColorDrawable.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return this.mColorDrawable.getOpacity();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mColorDrawable.setBounds(bounds);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] state) {
        if (this.mState.mColor == null) {
            return false;
        }
        int color = this.mState.mColor.getColorForState(state, this.mState.mColor.getDefaultColor());
        if (this.mState.mAlpha != -1) {
            color = (16777215 & color) | (MathUtils.constrain(this.mState.mAlpha, 0, 255) << 24);
        }
        if (color == this.mColorDrawable.getColor()) {
            return this.mColorDrawable.setState(state);
        }
        this.mColorDrawable.setColor(color);
        this.mColorDrawable.setState(state);
        return true;
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(Drawable who) {
        if (who == this.mColorDrawable && getCallback() != null) {
            getCallback().invalidateDrawable(this);
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (who == this.mColorDrawable && getCallback() != null) {
            getCallback().scheduleDrawable(this, what, when);
        }
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (who == this.mColorDrawable && getCallback() != null) {
            getCallback().unscheduleDrawable(this, what);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        this.mState.mChangingConfigurations |= getChangingConfigurations() & (~this.mState.getChangingConfigurations());
        return this.mState;
    }

    public ColorStateList getColorStateList() {
        if (this.mState.mColor == null) {
            return ColorStateList.valueOf(this.mColorDrawable.getColor());
        }
        return this.mState.mColor;
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mState.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mState = new ColorStateListDrawableState(this.mState);
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    public void setColorStateList(ColorStateList colorStateList) {
        this.mState.mColor = colorStateList;
        onStateChange(getState());
    }

    /* access modifiers changed from: package-private */
    public static final class ColorStateListDrawableState extends Drawable.ConstantState {
        int mAlpha = -1;
        BlendMode mBlendMode = Drawable.DEFAULT_BLEND_MODE;
        int mChangingConfigurations = 0;
        ColorStateList mColor = null;
        ColorStateList mTint = null;

        ColorStateListDrawableState() {
        }

        ColorStateListDrawableState(ColorStateListDrawableState state) {
            this.mColor = state.mColor;
            this.mTint = state.mTint;
            this.mAlpha = state.mAlpha;
            this.mBlendMode = state.mBlendMode;
            this.mChangingConfigurations = state.mChangingConfigurations;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new ColorStateListDrawable(this);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            int i = this.mChangingConfigurations;
            ColorStateList colorStateList = this.mColor;
            int i2 = 0;
            int changingConfigurations = i | (colorStateList != null ? colorStateList.getChangingConfigurations() : 0);
            ColorStateList colorStateList2 = this.mTint;
            if (colorStateList2 != null) {
                i2 = colorStateList2.getChangingConfigurations();
            }
            return changingConfigurations | i2;
        }

        public boolean isStateful() {
            ColorStateList colorStateList;
            ColorStateList colorStateList2 = this.mColor;
            return (colorStateList2 != null && colorStateList2.isStateful()) || ((colorStateList = this.mTint) != null && colorStateList.isStateful());
        }

        public boolean hasFocusStateSpecified() {
            ColorStateList colorStateList;
            ColorStateList colorStateList2 = this.mColor;
            return (colorStateList2 != null && colorStateList2.hasFocusStateSpecified()) || ((colorStateList = this.mTint) != null && colorStateList.hasFocusStateSpecified());
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            ColorStateList colorStateList;
            ColorStateList colorStateList2 = this.mColor;
            return (colorStateList2 != null && colorStateList2.canApplyTheme()) || ((colorStateList = this.mTint) != null && colorStateList.canApplyTheme());
        }
    }

    private ColorStateListDrawable(ColorStateListDrawableState state) {
        this.mMutated = false;
        this.mState = state;
        initializeColorDrawable();
    }

    private void initializeColorDrawable() {
        this.mColorDrawable = new ColorDrawable();
        this.mColorDrawable.setCallback(this);
        if (this.mState.mTint != null) {
            this.mColorDrawable.setTintList(this.mState.mTint);
        }
        if (this.mState.mBlendMode != DEFAULT_BLEND_MODE) {
            this.mColorDrawable.setTintBlendMode(this.mState.mBlendMode);
        }
    }
}

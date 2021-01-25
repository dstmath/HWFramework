package android.support.v4.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

class WrappedDrawableApi14 extends Drawable implements Drawable.Callback, WrappedDrawable, TintAwareDrawable {
    static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;
    private boolean mColorFilterSet;
    private int mCurrentColor;
    private PorterDuff.Mode mCurrentMode;
    Drawable mDrawable;
    private boolean mMutated;
    DrawableWrapperState mState;

    WrappedDrawableApi14(@NonNull DrawableWrapperState state, @Nullable Resources res) {
        this.mState = state;
        updateLocalState(res);
    }

    WrappedDrawableApi14(@Nullable Drawable dr) {
        this.mState = mutateConstantState();
        setWrappedDrawable(dr);
    }

    private void updateLocalState(@Nullable Resources res) {
        if (this.mState != null && this.mState.mDrawableState != null) {
            setWrappedDrawable(this.mState.mDrawableState.newDrawable(res));
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void jumpToCurrentState() {
        this.mDrawable.jumpToCurrentState();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(@NonNull Canvas canvas) {
        this.mDrawable.draw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        if (this.mDrawable != null) {
            this.mDrawable.setBounds(bounds);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setChangingConfigurations(int configs) {
        this.mDrawable.setChangingConfigurations(configs);
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | (this.mState != null ? this.mState.getChangingConfigurations() : 0) | this.mDrawable.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        this.mDrawable.setDither(dither);
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean filter) {
        this.mDrawable.setFilterBitmap(filter);
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        this.mDrawable.setAlpha(alpha);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        this.mDrawable.setColorFilter(cf);
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        ColorStateList tintList = (!isCompatTintEnabled() || this.mState == null) ? null : this.mState.mTint;
        return (tintList != null && tintList.isStateful()) || this.mDrawable.isStateful();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean setState(@NonNull int[] stateSet) {
        return updateTint(stateSet) || this.mDrawable.setState(stateSet);
    }

    @Override // android.graphics.drawable.Drawable
    @NonNull
    public int[] getState() {
        return this.mDrawable.getState();
    }

    @Override // android.graphics.drawable.Drawable
    @NonNull
    public Drawable getCurrent() {
        return this.mDrawable.getCurrent();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) || this.mDrawable.setVisible(visible, restart);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return this.mDrawable.getOpacity();
    }

    @Override // android.graphics.drawable.Drawable
    public Region getTransparentRegion() {
        return this.mDrawable.getTransparentRegion();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mDrawable.getIntrinsicWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mDrawable.getIntrinsicHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumWidth() {
        return this.mDrawable.getMinimumWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumHeight() {
        return this.mDrawable.getMinimumHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(@NonNull Rect padding) {
        return this.mDrawable.getPadding(padding);
    }

    @Override // android.graphics.drawable.Drawable
    @RequiresApi(19)
    public void setAutoMirrored(boolean mirrored) {
        this.mDrawable.setAutoMirrored(mirrored);
    }

    @Override // android.graphics.drawable.Drawable
    @RequiresApi(19)
    public boolean isAutoMirrored() {
        return this.mDrawable.isAutoMirrored();
    }

    @Override // android.graphics.drawable.Drawable
    @Nullable
    public Drawable.ConstantState getConstantState() {
        if (this.mState == null || !this.mState.canConstantState()) {
            return null;
        }
        this.mState.mChangingConfigurations = getChangingConfigurations();
        return this.mState;
    }

    @Override // android.graphics.drawable.Drawable
    @NonNull
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mState = mutateConstantState();
            if (this.mDrawable != null) {
                this.mDrawable.mutate();
            }
            if (this.mState != null) {
                this.mState.mDrawableState = this.mDrawable != null ? this.mDrawable.getConstantState() : null;
            }
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    @NonNull
    public DrawableWrapperState mutateConstantState() {
        return new DrawableWrapperStateBase(this.mState, null);
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override // android.graphics.drawable.Drawable.Callback
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        return this.mDrawable.setLevel(level);
    }

    @Override // android.graphics.drawable.Drawable, android.support.v4.graphics.drawable.TintAwareDrawable
    public void setTint(int tint) {
        setTintList(ColorStateList.valueOf(tint));
    }

    @Override // android.graphics.drawable.Drawable, android.support.v4.graphics.drawable.TintAwareDrawable
    public void setTintList(ColorStateList tint) {
        this.mState.mTint = tint;
        updateTint(getState());
    }

    @Override // android.graphics.drawable.Drawable, android.support.v4.graphics.drawable.TintAwareDrawable
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        this.mState.mTintMode = tintMode;
        updateTint(getState());
    }

    private boolean updateTint(int[] state) {
        if (!isCompatTintEnabled()) {
            return false;
        }
        ColorStateList tintList = this.mState.mTint;
        PorterDuff.Mode tintMode = this.mState.mTintMode;
        if (tintList == null || tintMode == null) {
            this.mColorFilterSet = false;
            clearColorFilter();
        } else {
            int color = tintList.getColorForState(state, tintList.getDefaultColor());
            if (!(this.mColorFilterSet && color == this.mCurrentColor && tintMode == this.mCurrentMode)) {
                setColorFilter(color, tintMode);
                this.mCurrentColor = color;
                this.mCurrentMode = tintMode;
                this.mColorFilterSet = true;
                return true;
            }
        }
        return false;
    }

    @Override // android.support.v4.graphics.drawable.WrappedDrawable
    public final Drawable getWrappedDrawable() {
        return this.mDrawable;
    }

    @Override // android.support.v4.graphics.drawable.WrappedDrawable
    public final void setWrappedDrawable(Drawable dr) {
        if (this.mDrawable != null) {
            this.mDrawable.setCallback(null);
        }
        this.mDrawable = dr;
        if (dr != null) {
            dr.setCallback(this);
            setVisible(dr.isVisible(), true);
            setState(dr.getState());
            setLevel(dr.getLevel());
            setBounds(dr.getBounds());
            if (this.mState != null) {
                this.mState.mDrawableState = dr.getConstantState();
            }
        }
        invalidateSelf();
    }

    /* access modifiers changed from: protected */
    public boolean isCompatTintEnabled() {
        return true;
    }

    /* access modifiers changed from: protected */
    public static abstract class DrawableWrapperState extends Drawable.ConstantState {
        int mChangingConfigurations;
        Drawable.ConstantState mDrawableState;
        ColorStateList mTint = null;
        PorterDuff.Mode mTintMode = WrappedDrawableApi14.DEFAULT_TINT_MODE;

        @Override // android.graphics.drawable.Drawable.ConstantState
        @NonNull
        public abstract Drawable newDrawable(@Nullable Resources resources);

        DrawableWrapperState(@Nullable DrawableWrapperState orig, @Nullable Resources res) {
            if (orig != null) {
                this.mChangingConfigurations = orig.mChangingConfigurations;
                this.mDrawableState = orig.mDrawableState;
                this.mTint = orig.mTint;
                this.mTintMode = orig.mTintMode;
            }
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        @NonNull
        public Drawable newDrawable() {
            return newDrawable(null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            return this.mChangingConfigurations | (this.mDrawableState != null ? this.mDrawableState.getChangingConfigurations() : 0);
        }

        /* access modifiers changed from: package-private */
        public boolean canConstantState() {
            return this.mDrawableState != null;
        }
    }

    /* access modifiers changed from: private */
    public static class DrawableWrapperStateBase extends DrawableWrapperState {
        DrawableWrapperStateBase(@Nullable DrawableWrapperState orig, @Nullable Resources res) {
            super(orig, res);
        }

        @Override // android.support.v4.graphics.drawable.WrappedDrawableApi14.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        @NonNull
        public Drawable newDrawable(@Nullable Resources res) {
            return new WrappedDrawableApi14(this, res);
        }
    }
}

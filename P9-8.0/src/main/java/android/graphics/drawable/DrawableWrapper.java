package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.Drawable.ConstantState;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public abstract class DrawableWrapper extends Drawable implements Callback {
    private Drawable mDrawable;
    private boolean mMutated;
    private DrawableWrapperState mState;

    static abstract class DrawableWrapperState extends ConstantState {
        int mChangingConfigurations;
        int mDensity = 160;
        ConstantState mDrawableState;
        int mSrcDensityOverride = 0;
        private int[] mThemeAttrs;

        public abstract Drawable newDrawable(Resources resources);

        DrawableWrapperState(DrawableWrapperState orig, Resources res) {
            int density;
            if (orig != null) {
                this.mThemeAttrs = orig.mThemeAttrs;
                this.mChangingConfigurations = orig.mChangingConfigurations;
                this.mDrawableState = orig.mDrawableState;
                this.mSrcDensityOverride = orig.mSrcDensityOverride;
            }
            if (res != null) {
                density = res.getDisplayMetrics().densityDpi;
            } else if (orig != null) {
                density = orig.mDensity;
            } else {
                density = 0;
            }
            if (density == 0) {
                density = 160;
            }
            this.mDensity = density;
        }

        public final void setDensity(int targetDensity) {
            if (this.mDensity != targetDensity) {
                int sourceDensity = this.mDensity;
                this.mDensity = targetDensity;
                onDensityChanged(sourceDensity, targetDensity);
            }
        }

        void onDensityChanged(int sourceDensity, int targetDensity) {
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs != null || (this.mDrawableState != null && this.mDrawableState.canApplyTheme())) {
                return true;
            }
            return super.canApplyTheme();
        }

        public Drawable newDrawable() {
            return newDrawable(null);
        }

        public int getChangingConfigurations() {
            return (this.mDrawableState != null ? this.mDrawableState.getChangingConfigurations() : 0) | this.mChangingConfigurations;
        }

        public boolean canConstantState() {
            return this.mDrawableState != null;
        }
    }

    DrawableWrapper(DrawableWrapperState state, Resources res) {
        this.mState = state;
        updateLocalState(res);
    }

    public DrawableWrapper(Drawable dr) {
        this.mState = null;
        this.mDrawable = dr;
    }

    private void updateLocalState(Resources res) {
        if (this.mState != null && this.mState.mDrawableState != null) {
            setDrawable(this.mState.mDrawableState.newDrawable(res));
        }
    }

    public void setDrawable(Drawable dr) {
        if (this.mDrawable != null) {
            this.mDrawable.setCallback(null);
        }
        this.mDrawable = dr;
        if (dr != null) {
            dr.setCallback(this);
            dr.setVisible(isVisible(), true);
            dr.setState(getState());
            dr.setLevel(getLevel());
            dr.setBounds(getBounds());
            dr.setLayoutDirection(getLayoutDirection());
            if (this.mState != null) {
                this.mState.mDrawableState = dr.getConstantState();
            }
        }
        invalidateSelf();
    }

    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        DrawableWrapperState state = this.mState;
        if (state != null) {
            int densityDpi = r.getDisplayMetrics().densityDpi;
            state.setDensity(densityDpi == 0 ? 160 : densityDpi);
            state.mSrcDensityOverride = this.mSrcDensityOverride;
            TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.DrawableWrapper);
            updateStateFromTypedArray(a);
            a.recycle();
            inflateChildDrawable(r, parser, attrs, theme);
        }
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        if (this.mDrawable != null && this.mDrawable.canApplyTheme()) {
            this.mDrawable.applyTheme(t);
        }
        DrawableWrapperState state = this.mState;
        if (state != null) {
            int densityDpi = t.getResources().getDisplayMetrics().densityDpi;
            state.setDensity(densityDpi == 0 ? 160 : densityDpi);
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.DrawableWrapper);
                updateStateFromTypedArray(a);
                a.recycle();
            }
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        DrawableWrapperState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            state.mThemeAttrs = a.extractThemeAttrs();
            if (a.hasValueOrEmpty(0)) {
                setDrawable(a.getDrawable(0));
            }
        }
    }

    public boolean canApplyTheme() {
        return (this.mState == null || !this.mState.canApplyTheme()) ? super.canApplyTheme() : true;
    }

    public void invalidateDrawable(Drawable who) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    public void draw(Canvas canvas) {
        if (this.mDrawable != null) {
            this.mDrawable.draw(canvas);
        }
    }

    public int getChangingConfigurations() {
        return ((this.mState != null ? this.mState.getChangingConfigurations() : 0) | super.getChangingConfigurations()) | this.mDrawable.getChangingConfigurations();
    }

    public boolean getPadding(Rect padding) {
        return this.mDrawable != null ? this.mDrawable.getPadding(padding) : false;
    }

    public Insets getOpticalInsets() {
        return this.mDrawable != null ? this.mDrawable.getOpticalInsets() : Insets.NONE;
    }

    public void setHotspot(float x, float y) {
        if (this.mDrawable != null) {
            this.mDrawable.setHotspot(x, y);
        }
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
        if (this.mDrawable != null) {
            this.mDrawable.setHotspotBounds(left, top, right, bottom);
        }
    }

    public void getHotspotBounds(Rect outRect) {
        if (this.mDrawable != null) {
            this.mDrawable.getHotspotBounds(outRect);
        } else {
            outRect.set(getBounds());
        }
    }

    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) | (this.mDrawable != null ? this.mDrawable.setVisible(visible, restart) : 0);
    }

    public void setAlpha(int alpha) {
        if (this.mDrawable != null) {
            this.mDrawable.setAlpha(alpha);
        }
    }

    public int getAlpha() {
        return this.mDrawable != null ? this.mDrawable.getAlpha() : 255;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        if (this.mDrawable != null) {
            this.mDrawable.setColorFilter(colorFilter);
        }
    }

    public void setTintList(ColorStateList tint) {
        if (this.mDrawable != null) {
            this.mDrawable.setTintList(tint);
        }
    }

    public void setTintMode(Mode tintMode) {
        if (this.mDrawable != null) {
            this.mDrawable.setTintMode(tintMode);
        }
    }

    public boolean onLayoutDirectionChanged(int layoutDirection) {
        return this.mDrawable != null ? this.mDrawable.setLayoutDirection(layoutDirection) : false;
    }

    public int getOpacity() {
        return this.mDrawable != null ? this.mDrawable.getOpacity() : -2;
    }

    public boolean isStateful() {
        return this.mDrawable != null ? this.mDrawable.isStateful() : false;
    }

    public boolean hasFocusStateSpecified() {
        return this.mDrawable != null ? this.mDrawable.hasFocusStateSpecified() : false;
    }

    protected boolean onStateChange(int[] state) {
        if (this.mDrawable == null || !this.mDrawable.isStateful()) {
            return false;
        }
        boolean changed = this.mDrawable.setState(state);
        if (changed) {
            onBoundsChange(getBounds());
        }
        return changed;
    }

    protected boolean onLevelChange(int level) {
        return this.mDrawable != null ? this.mDrawable.setLevel(level) : false;
    }

    protected void onBoundsChange(Rect bounds) {
        if (this.mDrawable != null) {
            this.mDrawable.setBounds(bounds);
        }
    }

    public int getIntrinsicWidth() {
        return this.mDrawable != null ? this.mDrawable.getIntrinsicWidth() : -1;
    }

    public int getIntrinsicHeight() {
        return this.mDrawable != null ? this.mDrawable.getIntrinsicHeight() : -1;
    }

    public void getOutline(Outline outline) {
        if (this.mDrawable != null) {
            this.mDrawable.getOutline(outline);
        } else {
            super.getOutline(outline);
        }
    }

    public ConstantState getConstantState() {
        if (this.mState == null || !this.mState.canConstantState()) {
            return null;
        }
        this.mState.mChangingConfigurations = getChangingConfigurations();
        return this.mState;
    }

    public Drawable mutate() {
        ConstantState constantState = null;
        if (!this.mMutated && super.mutate() == this) {
            this.mState = mutateConstantState();
            if (this.mDrawable != null) {
                this.mDrawable.mutate();
            }
            if (this.mState != null) {
                DrawableWrapperState drawableWrapperState = this.mState;
                if (this.mDrawable != null) {
                    constantState = this.mDrawable.getConstantState();
                }
                drawableWrapperState.mDrawableState = constantState;
            }
            this.mMutated = true;
        }
        return this;
    }

    DrawableWrapperState mutateConstantState() {
        return this.mState;
    }

    public void clearMutated() {
        super.clearMutated();
        if (this.mDrawable != null) {
            this.mDrawable.clearMutated();
        }
        this.mMutated = false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0023  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inflateChildDrawable(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        Drawable dr = null;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (dr == null) {
                    setDrawable(dr);
                    return;
                }
                return;
            } else if (type == 2) {
                dr = Drawable.createFromXmlInnerForDensity(r, parser, attrs, this.mState.mSrcDensityOverride, theme);
            }
        }
        if (dr == null) {
        }
    }
}

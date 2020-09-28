package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class InsetDrawable extends DrawableWrapper {
    @UnsupportedAppUsage
    private InsetState mState;
    private final Rect mTmpInsetRect;
    private final Rect mTmpRect;

    InsetDrawable() {
        this(new InsetState(null, null), (Resources) null);
    }

    public InsetDrawable(Drawable drawable, int inset) {
        this(drawable, inset, inset, inset, inset);
    }

    public InsetDrawable(Drawable drawable, float inset) {
        this(drawable, inset, inset, inset, inset);
    }

    public InsetDrawable(Drawable drawable, int insetLeft, int insetTop, int insetRight, int insetBottom) {
        this(new InsetState(null, null), (Resources) null);
        this.mState.mInsetLeft = new InsetValue(0.0f, insetLeft);
        this.mState.mInsetTop = new InsetValue(0.0f, insetTop);
        this.mState.mInsetRight = new InsetValue(0.0f, insetRight);
        this.mState.mInsetBottom = new InsetValue(0.0f, insetBottom);
        setDrawable(drawable);
    }

    public InsetDrawable(Drawable drawable, float insetLeftFraction, float insetTopFraction, float insetRightFraction, float insetBottomFraction) {
        this(new InsetState(null, null), (Resources) null);
        this.mState.mInsetLeft = new InsetValue(insetLeftFraction, 0);
        this.mState.mInsetTop = new InsetValue(insetTopFraction, 0);
        this.mState.mInsetRight = new InsetValue(insetRightFraction, 0);
        this.mState.mInsetBottom = new InsetValue(insetBottomFraction, 0);
        setDrawable(drawable);
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.InsetDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        InsetState state = this.mState;
        if (state != null && state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.InsetDrawable);
            try {
                updateStateFromTypedArray(a);
                verifyRequiredAttributes(a);
            } catch (XmlPullParserException e) {
                rethrowAsRuntimeException(e);
            } catch (Throwable th) {
                a.recycle();
                throw th;
            }
            a.recycle();
        }
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (getDrawable() != null) {
            return;
        }
        if (this.mState.mThemeAttrs == null || this.mState.mThemeAttrs[1] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <inset> tag requires a 'drawable' attribute or child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        InsetState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            state.mThemeAttrs = a.extractThemeAttrs();
            if (a.hasValue(6)) {
                InsetValue inset = getInset(a, 6, new InsetValue());
                state.mInsetLeft = inset;
                state.mInsetTop = inset;
                state.mInsetRight = inset;
                state.mInsetBottom = inset;
            }
            state.mInsetLeft = getInset(a, 2, state.mInsetLeft);
            state.mInsetTop = getInset(a, 4, state.mInsetTop);
            state.mInsetRight = getInset(a, 3, state.mInsetRight);
            state.mInsetBottom = getInset(a, 5, state.mInsetBottom);
        }
    }

    private InsetValue getInset(TypedArray a, int index, InsetValue defaultValue) {
        if (a.hasValue(index)) {
            TypedValue tv = a.peekValue(index);
            if (tv.type == 6) {
                float f = tv.getFraction(1.0f, 1.0f);
                if (f < 1.0f) {
                    return new InsetValue(f, 0);
                }
                throw new IllegalStateException("Fraction cannot be larger than 1");
            }
            int dimension = a.getDimensionPixelOffset(index, 0);
            if (dimension != 0) {
                return new InsetValue(0.0f, dimension);
            }
        }
        return defaultValue;
    }

    private void getInsets(Rect out) {
        Rect b = getBounds();
        out.left = this.mState.mInsetLeft.getDimension(b.width());
        out.right = this.mState.mInsetRight.getDimension(b.width());
        out.top = this.mState.mInsetTop.getDimension(b.height());
        out.bottom = this.mState.mInsetBottom.getDimension(b.height());
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public boolean getPadding(Rect padding) {
        boolean pad = super.getPadding(padding);
        getInsets(this.mTmpInsetRect);
        padding.left += this.mTmpInsetRect.left;
        padding.right += this.mTmpInsetRect.right;
        padding.top += this.mTmpInsetRect.top;
        padding.bottom += this.mTmpInsetRect.bottom;
        return pad || (((this.mTmpInsetRect.left | this.mTmpInsetRect.right) | this.mTmpInsetRect.top) | this.mTmpInsetRect.bottom) != 0;
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public Insets getOpticalInsets() {
        Insets contentInsets = super.getOpticalInsets();
        getInsets(this.mTmpInsetRect);
        return Insets.of(contentInsets.left + this.mTmpInsetRect.left, contentInsets.top + this.mTmpInsetRect.top, contentInsets.right + this.mTmpInsetRect.right, contentInsets.bottom + this.mTmpInsetRect.bottom);
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public int getOpacity() {
        InsetState insetState = this.mState;
        int opacity = getDrawable().getOpacity();
        getInsets(this.mTmpInsetRect);
        if (opacity != -1 || (this.mTmpInsetRect.left <= 0 && this.mTmpInsetRect.top <= 0 && this.mTmpInsetRect.right <= 0 && this.mTmpInsetRect.bottom <= 0)) {
            return opacity;
        }
        return -3;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void onBoundsChange(Rect bounds) {
        Rect r = this.mTmpRect;
        r.set(bounds);
        r.left += this.mState.mInsetLeft.getDimension(bounds.width());
        r.top += this.mState.mInsetTop.getDimension(bounds.height());
        r.right -= this.mState.mInsetRight.getDimension(bounds.width());
        r.bottom -= this.mState.mInsetBottom.getDimension(bounds.height());
        super.onBoundsChange(r);
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public int getIntrinsicWidth() {
        int childWidth = getDrawable().getIntrinsicWidth();
        float fraction = this.mState.mInsetLeft.mFraction + this.mState.mInsetRight.mFraction;
        if (childWidth < 0 || fraction >= 1.0f) {
            return -1;
        }
        return ((int) (((float) childWidth) / (1.0f - fraction))) + this.mState.mInsetLeft.mDimension + this.mState.mInsetRight.mDimension;
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public int getIntrinsicHeight() {
        int childHeight = getDrawable().getIntrinsicHeight();
        float fraction = this.mState.mInsetTop.mFraction + this.mState.mInsetBottom.mFraction;
        if (childHeight < 0 || fraction >= 1.0f) {
            return -1;
        }
        return ((int) (((float) childHeight) / (1.0f - fraction))) + this.mState.mInsetTop.mDimension + this.mState.mInsetBottom.mDimension;
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void getOutline(Outline outline) {
        getDrawable().getOutline(outline);
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.DrawableWrapper
    public DrawableWrapper.DrawableWrapperState mutateConstantState() {
        this.mState = new InsetState(this.mState, null);
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public static final class InsetState extends DrawableWrapper.DrawableWrapperState {
        InsetValue mInsetBottom;
        InsetValue mInsetLeft;
        InsetValue mInsetRight;
        InsetValue mInsetTop;
        private int[] mThemeAttrs;

        InsetState(InsetState orig, Resources res) {
            super(orig, res);
            if (orig != null) {
                this.mInsetLeft = orig.mInsetLeft.clone();
                this.mInsetTop = orig.mInsetTop.clone();
                this.mInsetRight = orig.mInsetRight.clone();
                this.mInsetBottom = orig.mInsetBottom.clone();
                if (orig.mDensity != this.mDensity) {
                    applyDensityScaling(orig.mDensity, this.mDensity);
                    return;
                }
                return;
            }
            this.mInsetLeft = new InsetValue();
            this.mInsetTop = new InsetValue();
            this.mInsetRight = new InsetValue();
            this.mInsetBottom = new InsetValue();
        }

        /* access modifiers changed from: package-private */
        @Override // android.graphics.drawable.DrawableWrapper.DrawableWrapperState
        public void onDensityChanged(int sourceDensity, int targetDensity) {
            super.onDensityChanged(sourceDensity, targetDensity);
            applyDensityScaling(sourceDensity, targetDensity);
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            this.mInsetLeft.scaleFromDensity(sourceDensity, targetDensity);
            this.mInsetTop.scaleFromDensity(sourceDensity, targetDensity);
            this.mInsetRight.scaleFromDensity(sourceDensity, targetDensity);
            this.mInsetBottom.scaleFromDensity(sourceDensity, targetDensity);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.DrawableWrapper.DrawableWrapperState
        public Drawable newDrawable(Resources res) {
            InsetState state;
            if (res != null) {
                int densityDpi = res.getDisplayMetrics().densityDpi;
                if ((densityDpi == 0 ? 160 : densityDpi) != this.mDensity) {
                    state = new InsetState(this, res);
                } else {
                    state = this;
                }
            } else {
                state = this;
            }
            return new InsetDrawable(state, res);
        }
    }

    /* access modifiers changed from: package-private */
    public static final class InsetValue implements Cloneable {
        int mDimension;
        final float mFraction;

        public InsetValue() {
            this(0.0f, 0);
        }

        public InsetValue(float fraction, int dimension) {
            this.mFraction = fraction;
            this.mDimension = dimension;
        }

        /* access modifiers changed from: package-private */
        public int getDimension(int boundSize) {
            return ((int) (((float) boundSize) * this.mFraction)) + this.mDimension;
        }

        /* access modifiers changed from: package-private */
        public void scaleFromDensity(int sourceDensity, int targetDensity) {
            int i = this.mDimension;
            if (i != 0) {
                this.mDimension = Bitmap.scaleFromDensity(i, sourceDensity, targetDensity);
            }
        }

        @Override // java.lang.Object
        public InsetValue clone() {
            return new InsetValue(this.mFraction, this.mDimension);
        }
    }

    private InsetDrawable(InsetState state, Resources res) {
        super(state, res);
        this.mTmpRect = new Rect();
        this.mTmpInsetRect = new Rect();
        this.mState = state;
    }
}

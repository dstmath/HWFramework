package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class InsetDrawable extends DrawableWrapper {
    private InsetState mState;
    private final Rect mTmpRect;

    static final class InsetState extends DrawableWrapperState {
        int mInsetBottom;
        int mInsetLeft;
        int mInsetRight;
        int mInsetTop;
        private int[] mThemeAttrs;

        InsetState(InsetState orig, Resources res) {
            super(orig, res);
            this.mInsetLeft = 0;
            this.mInsetTop = 0;
            this.mInsetRight = 0;
            this.mInsetBottom = 0;
            if (orig != null) {
                this.mInsetLeft = orig.mInsetLeft;
                this.mInsetTop = orig.mInsetTop;
                this.mInsetRight = orig.mInsetRight;
                this.mInsetBottom = orig.mInsetBottom;
                if (orig.mDensity != this.mDensity) {
                    applyDensityScaling(orig.mDensity, this.mDensity);
                }
            }
        }

        void onDensityChanged(int sourceDensity, int targetDensity) {
            super.onDensityChanged(sourceDensity, targetDensity);
            applyDensityScaling(sourceDensity, targetDensity);
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            this.mInsetLeft = Bitmap.scaleFromDensity(this.mInsetLeft, sourceDensity, targetDensity);
            this.mInsetTop = Bitmap.scaleFromDensity(this.mInsetTop, sourceDensity, targetDensity);
            this.mInsetRight = Bitmap.scaleFromDensity(this.mInsetRight, sourceDensity, targetDensity);
            this.mInsetBottom = Bitmap.scaleFromDensity(this.mInsetBottom, sourceDensity, targetDensity);
        }

        public Drawable newDrawable(Resources res) {
            InsetState state;
            if (res != null) {
                int densityDpi = res.getDisplayMetrics().densityDpi;
                if ((densityDpi == 0 ? Const.CODE_G3_RANGE_START : densityDpi) != this.mDensity) {
                    state = new InsetState(this, res);
                } else {
                    state = this;
                }
            } else {
                state = this;
            }
            return new InsetDrawable(res, null);
        }
    }

    InsetDrawable() {
        this(new InsetState(null, null), null);
    }

    public InsetDrawable(Drawable drawable, int inset) {
        this(drawable, inset, inset, inset, inset);
    }

    public InsetDrawable(Drawable drawable, int insetLeft, int insetTop, int insetRight, int insetBottom) {
        this(new InsetState(null, null), null);
        this.mState.mInsetLeft = insetLeft;
        this.mState.mInsetTop = insetTop;
        this.mState.mInsetRight = insetRight;
        this.mState.mInsetBottom = insetBottom;
        setDrawable(drawable);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.InsetDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        InsetState state = this.mState;
        if (!(state == null || state.mThemeAttrs == null)) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.InsetDrawable);
            try {
                updateStateFromTypedArray(a);
                verifyRequiredAttributes(a);
            } catch (XmlPullParserException e) {
                Drawable.rethrowAsRuntimeException(e);
            } finally {
                a.recycle();
            }
        }
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (getDrawable() != null) {
            return;
        }
        if (this.mState.mThemeAttrs == null || this.mState.mThemeAttrs[1] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <inset> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        InsetState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            state.mThemeAttrs = a.extractThemeAttrs();
            if (a.hasValue(6)) {
                int inset = a.getDimensionPixelOffset(6, 0);
                state.mInsetLeft = inset;
                state.mInsetTop = inset;
                state.mInsetRight = inset;
                state.mInsetBottom = inset;
            }
            state.mInsetLeft = a.getDimensionPixelOffset(2, state.mInsetLeft);
            state.mInsetRight = a.getDimensionPixelOffset(3, state.mInsetRight);
            state.mInsetTop = a.getDimensionPixelOffset(4, state.mInsetTop);
            state.mInsetBottom = a.getDimensionPixelOffset(5, state.mInsetBottom);
        }
    }

    public boolean getPadding(Rect padding) {
        boolean pad = super.getPadding(padding);
        padding.left += this.mState.mInsetLeft;
        padding.right += this.mState.mInsetRight;
        padding.top += this.mState.mInsetTop;
        padding.bottom += this.mState.mInsetBottom;
        if (pad || (((this.mState.mInsetLeft | this.mState.mInsetRight) | this.mState.mInsetTop) | this.mState.mInsetBottom) != 0) {
            return true;
        }
        return false;
    }

    public Insets getOpticalInsets() {
        Insets contentInsets = super.getOpticalInsets();
        return Insets.of(contentInsets.left + this.mState.mInsetLeft, contentInsets.top + this.mState.mInsetTop, contentInsets.right + this.mState.mInsetRight, contentInsets.bottom + this.mState.mInsetBottom);
    }

    public int getOpacity() {
        InsetState state = this.mState;
        int opacity = getDrawable().getOpacity();
        if (opacity != -1 || (state.mInsetLeft <= 0 && state.mInsetTop <= 0 && state.mInsetRight <= 0 && state.mInsetBottom <= 0)) {
            return opacity;
        }
        return -3;
    }

    protected void onBoundsChange(Rect bounds) {
        Rect r = this.mTmpRect;
        r.set(bounds);
        r.left += this.mState.mInsetLeft;
        r.top += this.mState.mInsetTop;
        r.right -= this.mState.mInsetRight;
        r.bottom -= this.mState.mInsetBottom;
        super.onBoundsChange(r);
    }

    public int getIntrinsicWidth() {
        int childWidth = getDrawable().getIntrinsicWidth();
        if (childWidth < 0) {
            return -1;
        }
        return (this.mState.mInsetLeft + childWidth) + this.mState.mInsetRight;
    }

    public int getIntrinsicHeight() {
        int childHeight = getDrawable().getIntrinsicHeight();
        if (childHeight < 0) {
            return -1;
        }
        return (this.mState.mInsetTop + childHeight) + this.mState.mInsetBottom;
    }

    public void getOutline(Outline outline) {
        getDrawable().getOutline(outline);
    }

    DrawableWrapperState mutateConstantState() {
        this.mState = new InsetState(this.mState, null);
        return this.mState;
    }

    private InsetDrawable(InsetState state, Resources res) {
        super(state, res);
        this.mTmpRect = new Rect();
        this.mState = state;
    }
}

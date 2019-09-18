package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewDebug;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorDrawable extends Drawable {
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "state_")
    private ColorState mColorState;
    private boolean mMutated;
    private final Paint mPaint;
    private PorterDuffColorFilter mTintFilter;

    static final class ColorState extends Drawable.ConstantState {
        int mBaseColor;
        int mChangingConfigurations;
        int[] mThemeAttrs;
        ColorStateList mTint = null;
        PorterDuff.Mode mTintMode = Drawable.DEFAULT_TINT_MODE;
        @ViewDebug.ExportedProperty
        int mUseColor;

        ColorState() {
        }

        ColorState(ColorState state) {
            this.mThemeAttrs = state.mThemeAttrs;
            this.mBaseColor = state.mBaseColor;
            this.mUseColor = state.mUseColor;
            this.mChangingConfigurations = state.mChangingConfigurations;
            this.mTint = state.mTint;
            this.mTintMode = state.mTintMode;
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs != null || (this.mTint != null && this.mTint.canApplyTheme());
        }

        public Drawable newDrawable() {
            return new ColorDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            return new ColorDrawable(this, res);
        }

        public int getChangingConfigurations() {
            return this.mChangingConfigurations | (this.mTint != null ? this.mTint.getChangingConfigurations() : 0);
        }
    }

    public ColorDrawable() {
        this.mPaint = new Paint(1);
        this.mColorState = new ColorState();
    }

    public ColorDrawable(int color) {
        this.mPaint = new Paint(1);
        this.mColorState = new ColorState();
        setColor(color);
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mColorState.getChangingConfigurations();
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mColorState = new ColorState(this.mColorState);
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    public void draw(Canvas canvas) {
        ColorFilter colorFilter = this.mPaint.getColorFilter();
        if ((this.mColorState.mUseColor >>> 24) != 0 || colorFilter != null || this.mTintFilter != null) {
            if (colorFilter == null) {
                this.mPaint.setColorFilter(this.mTintFilter);
            }
            this.mPaint.setColor(this.mColorState.mUseColor);
            canvas.drawRect(getBounds(), this.mPaint);
            this.mPaint.setColorFilter(colorFilter);
        }
    }

    public int getColor() {
        return this.mColorState.mUseColor;
    }

    public void setColor(int color) {
        if (this.mColorState.mBaseColor != color || this.mColorState.mUseColor != color) {
            ColorState colorState = this.mColorState;
            this.mColorState.mUseColor = color;
            colorState.mBaseColor = color;
            invalidateSelf();
        }
    }

    public int getAlpha() {
        return this.mColorState.mUseColor >>> 24;
    }

    public void setAlpha(int alpha) {
        int useColor = ((this.mColorState.mBaseColor << 8) >>> 8) | ((((this.mColorState.mBaseColor >>> 24) * (alpha + (alpha >> 7))) >> 8) << 24);
        if (this.mColorState.mUseColor != useColor) {
            this.mColorState.mUseColor = useColor;
            invalidateSelf();
        }
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    public void setTintList(ColorStateList tint) {
        this.mColorState.mTint = tint;
        this.mTintFilter = updateTintFilter(this.mTintFilter, tint, this.mColorState.mTintMode);
        invalidateSelf();
    }

    public void setTintMode(PorterDuff.Mode tintMode) {
        this.mColorState.mTintMode = tintMode;
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mColorState.mTint, tintMode);
        invalidateSelf();
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] stateSet) {
        ColorState state = this.mColorState;
        if (state.mTint == null || state.mTintMode == null) {
            return false;
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        return true;
    }

    public boolean isStateful() {
        return this.mColorState.mTint != null && this.mColorState.mTint.isStateful();
    }

    public boolean hasFocusStateSpecified() {
        return this.mColorState.mTint != null && this.mColorState.mTint.hasFocusStateSpecified();
    }

    public void setXfermode(Xfermode mode) {
        this.mPaint.setXfermode(mode);
        invalidateSelf();
    }

    public Xfermode getXfermode() {
        return this.mPaint.getXfermode();
    }

    public int getOpacity() {
        if (this.mTintFilter != null || this.mPaint.getColorFilter() != null) {
            return -3;
        }
        int i = this.mColorState.mUseColor >>> 24;
        if (i == 0) {
            return -2;
        }
        if (i != 255) {
            return -3;
        }
        return -1;
    }

    public void getOutline(Outline outline) {
        outline.setRect(getBounds());
        outline.setAlpha(((float) getAlpha()) / 255.0f);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.ColorDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        updateLocalState(r);
    }

    private void updateStateFromTypedArray(TypedArray a) {
        ColorState state = this.mColorState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mBaseColor = a.getColor(0, state.mBaseColor);
        state.mUseColor = state.mBaseColor;
    }

    public boolean canApplyTheme() {
        return this.mColorState.canApplyTheme() || super.canApplyTheme();
    }

    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        ColorState state = this.mColorState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ColorDrawable);
                updateStateFromTypedArray(a);
                a.recycle();
            }
            if (state.mTint != null && state.mTint.canApplyTheme()) {
                state.mTint = state.mTint.obtainForTheme(t);
            }
            updateLocalState(t.getResources());
        }
    }

    public Drawable.ConstantState getConstantState() {
        return this.mColorState;
    }

    private ColorDrawable(ColorState state, Resources res) {
        this.mPaint = new Paint(1);
        this.mColorState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources r) {
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mColorState.mTint, this.mColorState.mTintMode);
    }
}

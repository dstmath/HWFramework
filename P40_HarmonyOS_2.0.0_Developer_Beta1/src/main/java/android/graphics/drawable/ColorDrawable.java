package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewDebug;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorDrawable extends Drawable {
    private BlendModeColorFilter mBlendModeColorFilter;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "state_")
    private ColorState mColorState;
    private boolean mMutated;
    @UnsupportedAppUsage
    private final Paint mPaint;

    public ColorDrawable() {
        this.mPaint = new Paint(1);
        this.mColorState = new ColorState();
    }

    public ColorDrawable(int color) {
        this.mPaint = new Paint(1);
        this.mColorState = new ColorState();
        setColor(color);
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mColorState.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mColorState = new ColorState(this.mColorState);
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        ColorFilter colorFilter = this.mPaint.getColorFilter();
        if ((this.mColorState.mUseColor >>> 24) != 0 || colorFilter != null || this.mBlendModeColorFilter != null) {
            if (colorFilter == null) {
                this.mPaint.setColorFilter(this.mBlendModeColorFilter);
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
            colorState.mUseColor = color;
            colorState.mBaseColor = color;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mColorState.mUseColor >>> 24;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        int useColor = ((this.mColorState.mBaseColor << 8) >>> 8) | ((((this.mColorState.mBaseColor >>> 24) * (alpha + (alpha >> 7))) >> 8) << 24);
        if (this.mColorState.mUseColor != useColor) {
            this.mColorState.mUseColor = useColor;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mPaint.setColorFilter(colorFilter);
    }

    @Override // android.graphics.drawable.Drawable
    public ColorFilter getColorFilter() {
        return this.mPaint.getColorFilter();
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        ColorState colorState = this.mColorState;
        colorState.mTint = tint;
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, tint, colorState.mBlendMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintBlendMode(BlendMode blendMode) {
        ColorState colorState = this.mColorState;
        colorState.mBlendMode = blendMode;
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, colorState.mTint, blendMode);
        invalidateSelf();
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        ColorState state = this.mColorState;
        if (state.mTint == null || state.mBlendMode == null) {
            return false;
        }
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, state.mTint, state.mBlendMode);
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        return this.mColorState.mTint != null && this.mColorState.mTint.isStateful();
    }

    @Override // android.graphics.drawable.Drawable
    public boolean hasFocusStateSpecified() {
        return this.mColorState.mTint != null && this.mColorState.mTint.hasFocusStateSpecified();
    }

    @Override // android.graphics.drawable.Drawable
    public void setXfermode(Xfermode mode) {
        this.mPaint.setXfermode(mode);
        invalidateSelf();
    }

    public Xfermode getXfermode() {
        return this.mPaint.getXfermode();
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        if (this.mBlendModeColorFilter != null || this.mPaint.getColorFilter() != null) {
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

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        outline.setRect(getBounds());
        outline.setAlpha(((float) getAlpha()) / 255.0f);
    }

    @Override // android.graphics.drawable.Drawable
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

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        return this.mColorState.canApplyTheme() || super.canApplyTheme();
    }

    @Override // android.graphics.drawable.Drawable
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

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        return this.mColorState;
    }

    /* access modifiers changed from: package-private */
    public static final class ColorState extends Drawable.ConstantState {
        int mBaseColor;
        BlendMode mBlendMode = Drawable.DEFAULT_BLEND_MODE;
        int mChangingConfigurations;
        int[] mThemeAttrs;
        ColorStateList mTint = null;
        @UnsupportedAppUsage
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
            this.mBlendMode = state.mBlendMode;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            ColorStateList colorStateList;
            return this.mThemeAttrs != null || ((colorStateList = this.mTint) != null && colorStateList.canApplyTheme());
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new ColorDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new ColorDrawable(this, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            int i = this.mChangingConfigurations;
            ColorStateList colorStateList = this.mTint;
            return i | (colorStateList != null ? colorStateList.getChangingConfigurations() : 0);
        }
    }

    private ColorDrawable(ColorState state, Resources res) {
        this.mPaint = new Paint(1);
        this.mColorState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources r) {
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, this.mColorState.mTint, this.mColorState.mBlendMode);
    }
}

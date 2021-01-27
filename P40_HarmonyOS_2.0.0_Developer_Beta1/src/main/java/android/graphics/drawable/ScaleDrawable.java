package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import android.view.Gravity;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ScaleDrawable extends DrawableWrapper {
    private static final int MAX_LEVEL = 10000;
    @UnsupportedAppUsage
    private ScaleState mState;
    private final Rect mTmpRect;

    ScaleDrawable() {
        this(new ScaleState(null, null), null);
    }

    public ScaleDrawable(Drawable drawable, int gravity, float scaleWidth, float scaleHeight) {
        this(new ScaleState(null, null), null);
        ScaleState scaleState = this.mState;
        scaleState.mGravity = gravity;
        scaleState.mScaleWidth = scaleWidth;
        scaleState.mScaleHeight = scaleHeight;
        setDrawable(drawable);
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.ScaleDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
        updateLocalState();
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        ScaleState state = this.mState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ScaleDrawable);
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
            updateLocalState();
        }
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (getDrawable() != null) {
            return;
        }
        if (this.mState.mThemeAttrs == null || this.mState.mThemeAttrs[0] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <scale> tag requires a 'drawable' attribute or child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        ScaleState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            state.mThemeAttrs = a.extractThemeAttrs();
            state.mScaleWidth = getPercent(a, 1, state.mScaleWidth);
            state.mScaleHeight = getPercent(a, 2, state.mScaleHeight);
            state.mGravity = a.getInt(3, state.mGravity);
            state.mUseIntrinsicSizeAsMin = a.getBoolean(4, state.mUseIntrinsicSizeAsMin);
            state.mInitialLevel = a.getInt(5, state.mInitialLevel);
        }
    }

    private static float getPercent(TypedArray a, int index, float defaultValue) {
        int type = a.getType(index);
        if (type == 6 || type == 0) {
            return a.getFraction(index, 1, 1, defaultValue);
        }
        String s = a.getString(index);
        if (s == null || !s.endsWith("%")) {
            return defaultValue;
        }
        return Float.parseFloat(s.substring(0, s.length() - 1)) / 100.0f;
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        Drawable d = getDrawable();
        if (d != null && d.getLevel() != 0) {
            d.draw(canvas);
        }
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public int getOpacity() {
        Drawable d = getDrawable();
        if (d.getLevel() == 0) {
            return -2;
        }
        int opacity = d.getOpacity();
        if (opacity != -1 || d.getLevel() >= 10000) {
            return opacity;
        }
        return -3;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        super.onLevelChange(level);
        onBoundsChange(getBounds());
        invalidateSelf();
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void onBoundsChange(Rect bounds) {
        int w;
        int h;
        Drawable d = getDrawable();
        Rect r = this.mTmpRect;
        boolean min = this.mState.mUseIntrinsicSizeAsMin;
        int level = getLevel();
        int w2 = bounds.width();
        int ih = 0;
        if (this.mState.mScaleWidth > 0.0f) {
            w = w2 - ((int) ((((float) ((w2 - (min ? d.getIntrinsicWidth() : 0)) * (10000 - level))) * this.mState.mScaleWidth) / 10000.0f));
        } else {
            w = w2;
        }
        int h2 = bounds.height();
        if (this.mState.mScaleHeight > 0.0f) {
            if (min) {
                ih = d.getIntrinsicHeight();
            }
            h = h2 - ((int) ((((float) ((h2 - ih) * (10000 - level))) * this.mState.mScaleHeight) / 10000.0f));
        } else {
            h = h2;
        }
        Gravity.apply(this.mState.mGravity, w, h, bounds, r, getLayoutDirection());
        if (w > 0 && h > 0) {
            d.setBounds(r.left, r.top, r.right, r.bottom);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.DrawableWrapper
    public DrawableWrapper.DrawableWrapperState mutateConstantState() {
        this.mState = new ScaleState(this.mState, null);
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public static final class ScaleState extends DrawableWrapper.DrawableWrapperState {
        private static final float DO_NOT_SCALE = -1.0f;
        int mGravity = 3;
        int mInitialLevel = 0;
        float mScaleHeight = -1.0f;
        float mScaleWidth = -1.0f;
        private int[] mThemeAttrs;
        boolean mUseIntrinsicSizeAsMin = false;

        ScaleState(ScaleState orig, Resources res) {
            super(orig, res);
            if (orig != null) {
                this.mScaleWidth = orig.mScaleWidth;
                this.mScaleHeight = orig.mScaleHeight;
                this.mGravity = orig.mGravity;
                this.mUseIntrinsicSizeAsMin = orig.mUseIntrinsicSizeAsMin;
                this.mInitialLevel = orig.mInitialLevel;
            }
        }

        @Override // android.graphics.drawable.DrawableWrapper.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new ScaleDrawable(this, res);
        }
    }

    private ScaleDrawable(ScaleState state, Resources res) {
        super(state, res);
        this.mTmpRect = new Rect();
        this.mState = state;
        updateLocalState();
    }

    private void updateLocalState() {
        setLevel(this.mState.mInitialLevel);
    }
}

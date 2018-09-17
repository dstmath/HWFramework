package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.camera2.params.TonemapCurve;
import android.util.AttributeSet;
import android.view.Gravity;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ScaleDrawable extends DrawableWrapper {
    private static final int MAX_LEVEL = 10000;
    private ScaleState mState;
    private final Rect mTmpRect;

    static final class ScaleState extends DrawableWrapperState {
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

        public Drawable newDrawable(Resources res) {
            return new ScaleDrawable(this, res, null);
        }
    }

    /* synthetic */ ScaleDrawable(ScaleState state, Resources res, ScaleDrawable -this2) {
        this(state, res);
    }

    ScaleDrawable() {
        this(new ScaleState(null, null), null);
    }

    public ScaleDrawable(Drawable drawable, int gravity, float scaleWidth, float scaleHeight) {
        this(new ScaleState(null, null), null);
        this.mState.mGravity = gravity;
        this.mState.mScaleWidth = scaleWidth;
        this.mState.mScaleHeight = scaleHeight;
        setDrawable(drawable);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.ScaleDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
        updateLocalState();
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        ScaleState state = this.mState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ScaleDrawable);
                try {
                    updateStateFromTypedArray(a);
                    verifyRequiredAttributes(a);
                } catch (XmlPullParserException e) {
                    Drawable.rethrowAsRuntimeException(e);
                } finally {
                    a.recycle();
                }
            }
            updateLocalState();
        }
    }

    private void verifyRequiredAttributes(TypedArray a) throws XmlPullParserException {
        if (getDrawable() != null) {
            return;
        }
        if (this.mState.mThemeAttrs == null || this.mState.mThemeAttrs[0] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <scale> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
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

    public void draw(Canvas canvas) {
        Drawable d = getDrawable();
        if (d != null && d.getLevel() != 0) {
            d.draw(canvas);
        }
    }

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

    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        onBoundsChange(getBounds());
        invalidateSelf();
        return true;
    }

    protected void onBoundsChange(Rect bounds) {
        Drawable d = getDrawable();
        Rect r = this.mTmpRect;
        boolean min = this.mState.mUseIntrinsicSizeAsMin;
        int level = getLevel();
        int w = bounds.width();
        if (this.mState.mScaleWidth > TonemapCurve.LEVEL_BLACK) {
            w -= (int) ((((float) ((w - (min ? d.getIntrinsicWidth() : 0)) * (10000 - level))) * this.mState.mScaleWidth) / 10000.0f);
        }
        int h = bounds.height();
        if (this.mState.mScaleHeight > TonemapCurve.LEVEL_BLACK) {
            h -= (int) ((((float) ((h - (min ? d.getIntrinsicHeight() : 0)) * (10000 - level))) * this.mState.mScaleHeight) / 10000.0f);
        }
        Gravity.apply(this.mState.mGravity, w, h, bounds, r, getLayoutDirection());
        if (w > 0 && h > 0) {
            d.setBounds(r.left, r.top, r.right, r.bottom);
        }
    }

    DrawableWrapperState mutateConstantState() {
        this.mState = new ScaleState(this.mState, null);
        return this.mState;
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

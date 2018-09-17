package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ClipDrawable extends DrawableWrapper {
    public static final int HORIZONTAL = 1;
    private static final int MAX_LEVEL = 10000;
    public static final int VERTICAL = 2;
    private ClipState mState;
    private final Rect mTmpRect;

    static final class ClipState extends DrawableWrapperState {
        int mGravity = 3;
        int mOrientation = 1;
        private int[] mThemeAttrs;

        ClipState(ClipState orig, Resources res) {
            super(orig, res);
            if (orig != null) {
                this.mOrientation = orig.mOrientation;
                this.mGravity = orig.mGravity;
            }
        }

        public Drawable newDrawable(Resources res) {
            return new ClipDrawable(this, res, null);
        }
    }

    ClipDrawable() {
        this(new ClipState(null, null), null);
    }

    public ClipDrawable(Drawable drawable, int gravity, int orientation) {
        this(new ClipState(null, null), null);
        this.mState.mGravity = gravity;
        this.mState.mOrientation = orientation;
        setDrawable(drawable);
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.ClipDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        ClipState state = this.mState;
        if (!(state == null || state.mThemeAttrs == null)) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ClipDrawable);
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
            throw new XmlPullParserException(a.getPositionDescription() + ": <clip> tag requires a 'drawable' attribute or " + "child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        ClipState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            state.mThemeAttrs = a.extractThemeAttrs();
            state.mOrientation = a.getInt(2, state.mOrientation);
            state.mGravity = a.getInt(0, state.mGravity);
        }
    }

    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        invalidateSelf();
        return true;
    }

    public int getOpacity() {
        Drawable dr = getDrawable();
        if (dr.getOpacity() == -2 || dr.getLevel() == 0) {
            return -2;
        }
        if (getLevel() >= 10000) {
            return dr.getOpacity();
        }
        return -3;
    }

    public void draw(Canvas canvas) {
        Drawable dr = getDrawable();
        if (dr.getLevel() != 0) {
            Rect r = this.mTmpRect;
            Rect bounds = getBounds();
            int level = getLevel();
            int w = bounds.width();
            if ((this.mState.mOrientation & 1) != 0) {
                w -= ((w + 0) * (10000 - level)) / 10000;
            }
            int h = bounds.height();
            if ((this.mState.mOrientation & 2) != 0) {
                h -= ((h + 0) * (10000 - level)) / 10000;
            }
            Gravity.apply(this.mState.mGravity, w, h, bounds, r, getLayoutDirection());
            if (w > 0 && h > 0) {
                canvas.save();
                canvas.clipRect(r);
                dr.draw(canvas);
                canvas.restore();
            }
        }
    }

    DrawableWrapperState mutateConstantState() {
        this.mState = new ClipState(this.mState, null);
        return this.mState;
    }

    private ClipDrawable(ClipState state, Resources res) {
        super(state, res);
        this.mTmpRect = new Rect();
        this.mState = state;
    }
}

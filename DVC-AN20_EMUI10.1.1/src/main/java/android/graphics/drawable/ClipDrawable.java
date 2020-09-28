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

public class ClipDrawable extends DrawableWrapper {
    public static final int HORIZONTAL = 1;
    private static final int MAX_LEVEL = 10000;
    public static final int VERTICAL = 2;
    @UnsupportedAppUsage
    private ClipState mState;
    private final Rect mTmpRect;

    ClipDrawable() {
        this(new ClipState(null, null), null);
    }

    public ClipDrawable(Drawable drawable, int gravity, int orientation) {
        this(new ClipState(null, null), null);
        ClipState clipState = this.mState;
        clipState.mGravity = gravity;
        clipState.mOrientation = orientation;
        setDrawable(drawable);
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.ClipDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        ClipState state = this.mState;
        if (state != null && state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ClipDrawable);
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
            throw new XmlPullParserException(a.getPositionDescription() + ": <clip> tag requires a 'drawable' attribute or child tag defining a drawable");
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

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public boolean onLevelChange(int level) {
        super.onLevelChange(level);
        invalidateSelf();
        return true;
    }

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
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

    @Override // android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
    public void draw(Canvas canvas) {
        int w;
        int h;
        Drawable dr = getDrawable();
        if (dr.getLevel() != 0) {
            Rect r = this.mTmpRect;
            Rect bounds = getBounds();
            int level = getLevel();
            int w2 = bounds.width();
            if ((this.mState.mOrientation & 1) != 0) {
                w = w2 - (((w2 + 0) * (10000 - level)) / 10000);
            } else {
                w = w2;
            }
            int h2 = bounds.height();
            if ((this.mState.mOrientation & 2) != 0) {
                h = h2 - (((h2 + 0) * (10000 - level)) / 10000);
            } else {
                h = h2;
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

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.DrawableWrapper
    public DrawableWrapper.DrawableWrapperState mutateConstantState() {
        this.mState = new ClipState(this.mState, null);
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public static final class ClipState extends DrawableWrapper.DrawableWrapperState {
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

        @Override // android.graphics.drawable.Drawable.ConstantState, android.graphics.drawable.DrawableWrapper.DrawableWrapperState
        public Drawable newDrawable(Resources res) {
            return new ClipDrawable(this, res);
        }
    }

    private ClipDrawable(ClipState state, Resources res) {
        super(state, res);
        this.mTmpRect = new Rect();
        this.mState = state;
    }
}

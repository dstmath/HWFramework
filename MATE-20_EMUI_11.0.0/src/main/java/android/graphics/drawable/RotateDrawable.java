package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.DrawableWrapper;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class RotateDrawable extends DrawableWrapper {
    private static final int MAX_LEVEL = 10000;
    @UnsupportedAppUsage
    private RotateState mState;

    public RotateDrawable() {
        this(new RotateState(null, null), null);
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.RotateDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        RotateState state = this.mState;
        if (state != null && state.mThemeAttrs != null) {
            TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.RotateDrawable);
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
            throw new XmlPullParserException(a.getPositionDescription() + ": <rotate> tag requires a 'drawable' attribute or child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        RotateState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            state.mThemeAttrs = a.extractThemeAttrs();
            boolean z = true;
            if (a.hasValue(4)) {
                TypedValue tv = a.peekValue(4);
                state.mPivotXRel = tv.type == 6;
                state.mPivotX = state.mPivotXRel ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
            }
            if (a.hasValue(5)) {
                TypedValue tv2 = a.peekValue(5);
                if (tv2.type != 6) {
                    z = false;
                }
                state.mPivotYRel = z;
                state.mPivotY = state.mPivotYRel ? tv2.getFraction(1.0f, 1.0f) : tv2.getFloat();
            }
            state.mFromDegrees = a.getFloat(2, state.mFromDegrees);
            state.mToDegrees = a.getFloat(3, state.mToDegrees);
            state.mCurrentDegrees = state.mFromDegrees;
        }
    }

    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        Drawable d = getDrawable();
        Rect bounds = d.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;
        RotateState st = this.mState;
        float px = st.mPivotXRel ? ((float) w) * st.mPivotX : st.mPivotX;
        float py = st.mPivotYRel ? ((float) h) * st.mPivotY : st.mPivotY;
        int saveCount = canvas.save();
        canvas.rotate(st.mCurrentDegrees, ((float) bounds.left) + px, ((float) bounds.top) + py);
        d.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void setFromDegrees(float fromDegrees) {
        if (this.mState.mFromDegrees != fromDegrees) {
            this.mState.mFromDegrees = fromDegrees;
            invalidateSelf();
        }
    }

    public float getFromDegrees() {
        return this.mState.mFromDegrees;
    }

    public void setToDegrees(float toDegrees) {
        if (this.mState.mToDegrees != toDegrees) {
            this.mState.mToDegrees = toDegrees;
            invalidateSelf();
        }
    }

    public float getToDegrees() {
        return this.mState.mToDegrees;
    }

    public void setPivotX(float pivotX) {
        if (this.mState.mPivotX != pivotX) {
            this.mState.mPivotX = pivotX;
            invalidateSelf();
        }
    }

    public float getPivotX() {
        return this.mState.mPivotX;
    }

    public void setPivotXRelative(boolean relative) {
        if (this.mState.mPivotXRel != relative) {
            this.mState.mPivotXRel = relative;
            invalidateSelf();
        }
    }

    public boolean isPivotXRelative() {
        return this.mState.mPivotXRel;
    }

    public void setPivotY(float pivotY) {
        if (this.mState.mPivotY != pivotY) {
            this.mState.mPivotY = pivotY;
            invalidateSelf();
        }
    }

    public float getPivotY() {
        return this.mState.mPivotY;
    }

    public void setPivotYRelative(boolean relative) {
        if (this.mState.mPivotYRel != relative) {
            this.mState.mPivotYRel = relative;
            invalidateSelf();
        }
    }

    public boolean isPivotYRelative() {
        return this.mState.mPivotYRel;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.DrawableWrapper, android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        super.onLevelChange(level);
        this.mState.mCurrentDegrees = MathUtils.lerp(this.mState.mFromDegrees, this.mState.mToDegrees, ((float) level) / 10000.0f);
        invalidateSelf();
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.drawable.DrawableWrapper
    public DrawableWrapper.DrawableWrapperState mutateConstantState() {
        this.mState = new RotateState(this.mState, null);
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public static final class RotateState extends DrawableWrapper.DrawableWrapperState {
        float mCurrentDegrees = 0.0f;
        float mFromDegrees = 0.0f;
        float mPivotX = 0.5f;
        boolean mPivotXRel = true;
        float mPivotY = 0.5f;
        boolean mPivotYRel = true;
        private int[] mThemeAttrs;
        float mToDegrees = 360.0f;

        RotateState(RotateState orig, Resources res) {
            super(orig, res);
            if (orig != null) {
                this.mPivotXRel = orig.mPivotXRel;
                this.mPivotX = orig.mPivotX;
                this.mPivotYRel = orig.mPivotYRel;
                this.mPivotY = orig.mPivotY;
                this.mFromDegrees = orig.mFromDegrees;
                this.mToDegrees = orig.mToDegrees;
                this.mCurrentDegrees = orig.mCurrentDegrees;
            }
        }

        @Override // android.graphics.drawable.DrawableWrapper.DrawableWrapperState, android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            return new RotateDrawable(this, res);
        }
    }

    private RotateDrawable(RotateState state, Resources res) {
        super(state, res);
        this.mState = state;
    }
}

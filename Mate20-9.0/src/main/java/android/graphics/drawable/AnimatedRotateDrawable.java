package android.graphics.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.DrawableWrapper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AnimatedRotateDrawable extends DrawableWrapper implements Animatable {
    /* access modifiers changed from: private */
    public float mCurrentDegrees;
    /* access modifiers changed from: private */
    public float mIncrement;
    private final Runnable mNextFrame;
    private boolean mRunning;
    private AnimatedRotateState mState;

    static final class AnimatedRotateState extends DrawableWrapper.DrawableWrapperState {
        int mFrameDuration = 150;
        int mFramesCount = 12;
        float mPivotX = 0.0f;
        boolean mPivotXRel = false;
        float mPivotY = 0.0f;
        boolean mPivotYRel = false;
        /* access modifiers changed from: private */
        public int[] mThemeAttrs;

        public AnimatedRotateState(AnimatedRotateState orig, Resources res) {
            super(orig, res);
            if (orig != null) {
                this.mPivotXRel = orig.mPivotXRel;
                this.mPivotX = orig.mPivotX;
                this.mPivotYRel = orig.mPivotYRel;
                this.mPivotY = orig.mPivotY;
                this.mFramesCount = orig.mFramesCount;
                this.mFrameDuration = orig.mFrameDuration;
            }
        }

        public Drawable newDrawable(Resources res) {
            return new AnimatedRotateDrawable(this, res);
        }
    }

    static /* synthetic */ float access$216(AnimatedRotateDrawable x0, float x1) {
        float f = x0.mCurrentDegrees + x1;
        x0.mCurrentDegrees = f;
        return f;
    }

    public AnimatedRotateDrawable() {
        this(new AnimatedRotateState(null, null), null);
    }

    public void draw(Canvas canvas) {
        Drawable drawable = getDrawable();
        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;
        AnimatedRotateState st = this.mState;
        float px = st.mPivotXRel ? ((float) w) * st.mPivotX : st.mPivotX;
        float py = st.mPivotYRel ? ((float) h) * st.mPivotY : st.mPivotY;
        int saveCount = canvas.save();
        canvas.rotate(this.mCurrentDegrees, ((float) bounds.left) + px, ((float) bounds.top) + py);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    public void start() {
        if (!this.mRunning) {
            this.mRunning = true;
            nextFrame();
        }
    }

    public void stop() {
        this.mRunning = false;
        unscheduleSelf(this.mNextFrame);
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    /* access modifiers changed from: private */
    public void nextFrame() {
        unscheduleSelf(this.mNextFrame);
        scheduleSelf(this.mNextFrame, SystemClock.uptimeMillis() + ((long) this.mState.mFrameDuration));
    }

    public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (!visible) {
            unscheduleSelf(this.mNextFrame);
        } else if (changed || restart) {
            this.mCurrentDegrees = 0.0f;
            nextFrame();
        }
        return changed;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.AnimatedRotateDrawable);
        super.inflate(r, parser, attrs, theme);
        updateStateFromTypedArray(a);
        verifyRequiredAttributes(a);
        a.recycle();
        updateLocalState();
    }

    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        AnimatedRotateState state = this.mState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.AnimatedRotateDrawable);
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
        if (this.mState.mThemeAttrs == null || this.mState.mThemeAttrs[1] == 0) {
            throw new XmlPullParserException(a.getPositionDescription() + ": <animated-rotate> tag requires a 'drawable' attribute or child tag defining a drawable");
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        AnimatedRotateState state = this.mState;
        if (state != null) {
            state.mChangingConfigurations |= a.getChangingConfigurations();
            int[] unused = state.mThemeAttrs = a.extractThemeAttrs();
            boolean z = false;
            if (a.hasValue(2)) {
                TypedValue tv = a.peekValue(2);
                state.mPivotXRel = tv.type == 6;
                state.mPivotX = state.mPivotXRel ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
            }
            if (a.hasValue(3)) {
                TypedValue tv2 = a.peekValue(3);
                if (tv2.type == 6) {
                    z = true;
                }
                state.mPivotYRel = z;
                state.mPivotY = state.mPivotYRel ? tv2.getFraction(1.0f, 1.0f) : tv2.getFloat();
            }
            setFramesCount(a.getInt(5, state.mFramesCount));
            setFramesDuration(a.getInt(4, state.mFrameDuration));
        }
    }

    public void setFramesCount(int framesCount) {
        this.mState.mFramesCount = framesCount;
        this.mIncrement = 360.0f / ((float) this.mState.mFramesCount);
    }

    public void setFramesDuration(int framesDuration) {
        this.mState.mFrameDuration = framesDuration;
    }

    /* access modifiers changed from: package-private */
    public DrawableWrapper.DrawableWrapperState mutateConstantState() {
        this.mState = new AnimatedRotateState(this.mState, null);
        return this.mState;
    }

    private AnimatedRotateDrawable(AnimatedRotateState state, Resources res) {
        super(state, res);
        this.mNextFrame = new Runnable() {
            public void run() {
                AnimatedRotateDrawable.access$216(AnimatedRotateDrawable.this, AnimatedRotateDrawable.this.mIncrement);
                if (AnimatedRotateDrawable.this.mCurrentDegrees > 360.0f - AnimatedRotateDrawable.this.mIncrement) {
                    float unused = AnimatedRotateDrawable.this.mCurrentDegrees = 0.0f;
                }
                AnimatedRotateDrawable.this.invalidateSelf();
                AnimatedRotateDrawable.this.nextFrame();
            }
        };
        this.mState = state;
        updateLocalState();
    }

    private void updateLocalState() {
        this.mIncrement = 360.0f / ((float) this.mState.mFramesCount);
        Drawable drawable = getDrawable();
        if (drawable != null) {
            drawable.setFilterBitmap(true);
            if (drawable instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable).setAntiAlias(true);
            }
        }
    }
}

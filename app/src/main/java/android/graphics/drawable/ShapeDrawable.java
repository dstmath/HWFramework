package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.shapes.Shape;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ShapeDrawable extends Drawable {
    private boolean mMutated;
    private ShapeState mShapeState;
    private PorterDuffColorFilter mTintFilter;

    public static abstract class ShaderFactory {
        public abstract Shader resize(int i, int i2);
    }

    static final class ShapeState extends ConstantState {
        int mAlpha;
        int mChangingConfigurations;
        int mIntrinsicHeight;
        int mIntrinsicWidth;
        Rect mPadding;
        Paint mPaint;
        ShaderFactory mShaderFactory;
        Shape mShape;
        int[] mThemeAttrs;
        ColorStateList mTint;
        Mode mTintMode;

        ShapeState(ShapeState orig) {
            this.mTint = null;
            this.mTintMode = ShapeDrawable.DEFAULT_TINT_MODE;
            this.mAlpha = Process.PROC_TERM_MASK;
            if (orig != null) {
                this.mThemeAttrs = orig.mThemeAttrs;
                this.mPaint = orig.mPaint;
                this.mShape = orig.mShape;
                this.mTint = orig.mTint;
                this.mTintMode = orig.mTintMode;
                this.mPadding = orig.mPadding;
                this.mIntrinsicWidth = orig.mIntrinsicWidth;
                this.mIntrinsicHeight = orig.mIntrinsicHeight;
                this.mAlpha = orig.mAlpha;
                this.mShaderFactory = orig.mShaderFactory;
                return;
            }
            this.mPaint = new Paint(1);
        }

        public boolean canApplyTheme() {
            if (this.mThemeAttrs == null) {
                return this.mTint != null ? this.mTint.canApplyTheme() : false;
            } else {
                return true;
            }
        }

        public Drawable newDrawable() {
            return new ShapeDrawable(null, null);
        }

        public Drawable newDrawable(Resources res) {
            return new ShapeDrawable(res, null);
        }

        public int getChangingConfigurations() {
            return (this.mTint != null ? this.mTint.getChangingConfigurations() : 0) | this.mChangingConfigurations;
        }
    }

    public ShapeDrawable() {
        this(new ShapeState(null), null);
    }

    public ShapeDrawable(Shape s) {
        this(new ShapeState(null), null);
        this.mShapeState.mShape = s;
    }

    public Shape getShape() {
        return this.mShapeState.mShape;
    }

    public void setShape(Shape s) {
        this.mShapeState.mShape = s;
        updateShape();
    }

    public void setShaderFactory(ShaderFactory fact) {
        this.mShapeState.mShaderFactory = fact;
    }

    public ShaderFactory getShaderFactory() {
        return this.mShapeState.mShaderFactory;
    }

    public Paint getPaint() {
        return this.mShapeState.mPaint;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        if ((((left | top) | right) | bottom) == 0) {
            this.mShapeState.mPadding = null;
        } else {
            if (this.mShapeState.mPadding == null) {
                this.mShapeState.mPadding = new Rect();
            }
            this.mShapeState.mPadding.set(left, top, right, bottom);
        }
        invalidateSelf();
    }

    public void setPadding(Rect padding) {
        if (padding == null) {
            this.mShapeState.mPadding = null;
        } else {
            if (this.mShapeState.mPadding == null) {
                this.mShapeState.mPadding = new Rect();
            }
            this.mShapeState.mPadding.set(padding);
        }
        invalidateSelf();
    }

    public void setIntrinsicWidth(int width) {
        this.mShapeState.mIntrinsicWidth = width;
        invalidateSelf();
    }

    public void setIntrinsicHeight(int height) {
        this.mShapeState.mIntrinsicHeight = height;
        invalidateSelf();
    }

    public int getIntrinsicWidth() {
        return this.mShapeState.mIntrinsicWidth;
    }

    public int getIntrinsicHeight() {
        return this.mShapeState.mIntrinsicHeight;
    }

    public boolean getPadding(Rect padding) {
        if (this.mShapeState.mPadding == null) {
            return super.getPadding(padding);
        }
        padding.set(this.mShapeState.mPadding);
        return true;
    }

    private static int modulateAlpha(int paintAlpha, int alpha) {
        return (paintAlpha * (alpha + (alpha >>> 7))) >>> 8;
    }

    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
        shape.draw(canvas, paint);
    }

    public void draw(Canvas canvas) {
        boolean clearColorFilter;
        Rect r = getBounds();
        ShapeState state = this.mShapeState;
        Paint paint = state.mPaint;
        int prevAlpha = paint.getAlpha();
        paint.setAlpha(modulateAlpha(prevAlpha, state.mAlpha));
        if (paint.getAlpha() == 0 && paint.getXfermode() == null) {
            if (paint.hasShadowLayer()) {
            }
            paint.setAlpha(prevAlpha);
        }
        if (this.mTintFilter == null || paint.getColorFilter() != null) {
            clearColorFilter = false;
        } else {
            paint.setColorFilter(this.mTintFilter);
            clearColorFilter = true;
        }
        if (state.mShape != null) {
            int count = canvas.save();
            canvas.translate((float) r.left, (float) r.top);
            onDraw(state.mShape, canvas, paint);
            canvas.restoreToCount(count);
        } else {
            canvas.drawRect(r, paint);
        }
        if (clearColorFilter) {
            paint.setColorFilter(null);
        }
        paint.setAlpha(prevAlpha);
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mShapeState.getChangingConfigurations();
    }

    public void setAlpha(int alpha) {
        this.mShapeState.mAlpha = alpha;
        invalidateSelf();
    }

    public int getAlpha() {
        return this.mShapeState.mAlpha;
    }

    public void setTintList(ColorStateList tint) {
        this.mShapeState.mTint = tint;
        this.mTintFilter = updateTintFilter(this.mTintFilter, tint, this.mShapeState.mTintMode);
        invalidateSelf();
    }

    public void setTintMode(Mode tintMode) {
        this.mShapeState.mTintMode = tintMode;
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mShapeState.mTint, tintMode);
        invalidateSelf();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mShapeState.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    public int getOpacity() {
        if (this.mShapeState.mShape == null) {
            Paint p = this.mShapeState.mPaint;
            if (p.getXfermode() == null) {
                int alpha = p.getAlpha();
                if (alpha == 0) {
                    return -2;
                }
                if (alpha == Process.PROC_TERM_MASK) {
                    return -1;
                }
            }
        }
        return -3;
    }

    public void setDither(boolean dither) {
        this.mShapeState.mPaint.setDither(dither);
        invalidateSelf();
    }

    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateShape();
    }

    protected boolean onStateChange(int[] stateSet) {
        ShapeState state = this.mShapeState;
        if (state.mTint == null || state.mTintMode == null) {
            return false;
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        return true;
    }

    public boolean isStateful() {
        ShapeState s = this.mShapeState;
        if (super.isStateful()) {
            return true;
        }
        return s.mTint != null ? s.mTint.isStateful() : false;
    }

    protected boolean inflateTag(String name, Resources r, XmlPullParser parser, AttributeSet attrs) {
        if (!"padding".equals(name)) {
            return false;
        }
        TypedArray a = r.obtainAttributes(attrs, R.styleable.ShapeDrawablePadding);
        setPadding(a.getDimensionPixelOffset(0, 0), a.getDimensionPixelOffset(1, 0), a.getDimensionPixelOffset(2, 0), a.getDimensionPixelOffset(3, 0));
        a.recycle();
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.ShapeDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                updateLocalState(r);
            } else if (type == 2) {
                String name = parser.getName();
                if (!inflateTag(name, r, parser, attrs)) {
                    Log.w("drawable", "Unknown element: " + name + " for ShapeDrawable " + this);
                }
            }
        }
        updateLocalState(r);
    }

    public void applyTheme(Theme t) {
        super.applyTheme(t);
        ShapeState state = this.mShapeState;
        if (state != null) {
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.ShapeDrawable);
                updateStateFromTypedArray(a);
                a.recycle();
            }
            if (state.mTint != null && state.mTint.canApplyTheme()) {
                state.mTint = state.mTint.obtainForTheme(t);
            }
            updateLocalState(t.getResources());
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        ShapeState state = this.mShapeState;
        Paint paint = state.mPaint;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        paint.setColor(a.getColor(4, paint.getColor()));
        paint.setDither(a.getBoolean(0, paint.isDither()));
        setIntrinsicWidth((int) a.getDimension(3, (float) state.mIntrinsicWidth));
        setIntrinsicHeight((int) a.getDimension(2, (float) state.mIntrinsicHeight));
        int tintMode = a.getInt(5, -1);
        if (tintMode != -1) {
            state.mTintMode = Drawable.parseTintMode(tintMode, Mode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(1);
        if (tint != null) {
            state.mTint = tint;
        }
    }

    private void updateShape() {
        if (this.mShapeState.mShape != null) {
            Rect r = getBounds();
            int w = r.width();
            int h = r.height();
            this.mShapeState.mShape.resize((float) w, (float) h);
            if (this.mShapeState.mShaderFactory != null) {
                this.mShapeState.mPaint.setShader(this.mShapeState.mShaderFactory.resize(w, h));
            }
        }
        invalidateSelf();
    }

    public void getOutline(Outline outline) {
        if (this.mShapeState.mShape != null) {
            this.mShapeState.mShape.getOutline(outline);
            outline.setAlpha(((float) getAlpha()) / 255.0f);
        }
    }

    public ConstantState getConstantState() {
        this.mShapeState.mChangingConfigurations = getChangingConfigurations();
        return this.mShapeState;
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            if (this.mShapeState.mPaint != null) {
                this.mShapeState.mPaint = new Paint(this.mShapeState.mPaint);
            } else {
                this.mShapeState.mPaint = new Paint(1);
            }
            if (this.mShapeState.mPadding != null) {
                this.mShapeState.mPadding = new Rect(this.mShapeState.mPadding);
            } else {
                this.mShapeState.mPadding = new Rect();
            }
            try {
                this.mShapeState.mShape = this.mShapeState.mShape.clone();
                this.mMutated = true;
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    private ShapeDrawable(ShapeState state, Resources res) {
        this.mShapeState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources res) {
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mShapeState.mTint, this.mShapeState.mTintMode);
    }
}

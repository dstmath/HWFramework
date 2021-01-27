package android.graphics.drawable;

import android.annotation.UnsupportedAppUsage;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Insets;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import com.android.internal.R;
import com.android.internal.app.DumpHeapActivity;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GradientDrawable extends Drawable {
    private static final float DEFAULT_INNER_RADIUS_RATIO = 3.0f;
    private static final float DEFAULT_THICKNESS_RATIO = 9.0f;
    public static final int LINE = 2;
    public static final int LINEAR_GRADIENT = 0;
    public static final int OVAL = 1;
    public static final int RADIAL_GRADIENT = 1;
    private static final int RADIUS_TYPE_FRACTION = 1;
    private static final int RADIUS_TYPE_FRACTION_PARENT = 2;
    private static final int RADIUS_TYPE_PIXELS = 0;
    public static final int RECTANGLE = 0;
    public static final int RING = 3;
    public static final int SWEEP_GRADIENT = 2;
    private int mAlpha;
    private BlendModeColorFilter mBlendModeColorFilter;
    private ColorFilter mColorFilter;
    @UnsupportedAppUsage
    private final Paint mFillPaint;
    private boolean mGradientIsDirty;
    private float mGradientRadius;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
    private GradientState mGradientState;
    private Paint mLayerPaint;
    private boolean mMutated;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124051827)
    private Rect mPadding;
    private final Path mPath;
    private boolean mPathIsDirty;
    private final RectF mRect;
    private Path mRingPath;
    @UnsupportedAppUsage
    private Paint mStrokePaint;

    @Retention(RetentionPolicy.SOURCE)
    public @interface GradientType {
    }

    public enum Orientation {
        TOP_BOTTOM,
        TR_BL,
        RIGHT_LEFT,
        BR_TL,
        BOTTOM_TOP,
        BL_TR,
        LEFT_RIGHT,
        TL_BR
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RadiusType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Shape {
    }

    public GradientDrawable() {
        this(new GradientState(Orientation.TOP_BOTTOM, (int[]) null), (Resources) null);
    }

    public GradientDrawable(Orientation orientation, int[] colors) {
        this(new GradientState(orientation, colors), (Resources) null);
    }

    @Override // android.graphics.drawable.Drawable
    public boolean getPadding(Rect padding) {
        Rect rect = this.mPadding;
        if (rect == null) {
            return super.getPadding(padding);
        }
        padding.set(rect);
        return true;
    }

    public void setCornerRadii(float[] radii) {
        this.mGradientState.setCornerRadii(radii);
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public float[] getCornerRadii() {
        return (float[]) this.mGradientState.mRadiusArray.clone();
    }

    public void setCornerRadius(float radius) {
        this.mGradientState.setCornerRadius(radius);
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public float getCornerRadius() {
        return this.mGradientState.mRadius;
    }

    public void setStroke(int width, int color) {
        setStroke(width, color, 0.0f, 0.0f);
    }

    public void setStroke(int width, ColorStateList colorStateList) {
        setStroke(width, colorStateList, 0.0f, 0.0f);
    }

    public void setStroke(int width, int color, float dashWidth, float dashGap) {
        this.mGradientState.setStroke(width, ColorStateList.valueOf(color), dashWidth, dashGap);
        setStrokeInternal(width, color, dashWidth, dashGap);
    }

    public void setStroke(int width, ColorStateList colorStateList, float dashWidth, float dashGap) {
        int color;
        this.mGradientState.setStroke(width, colorStateList, dashWidth, dashGap);
        if (colorStateList == null) {
            color = 0;
        } else {
            color = colorStateList.getColorForState(getState(), 0);
        }
        setStrokeInternal(width, color, dashWidth, dashGap);
    }

    private void setStrokeInternal(int width, int color, float dashWidth, float dashGap) {
        if (this.mStrokePaint == null) {
            this.mStrokePaint = new Paint(1);
            this.mStrokePaint.setStyle(Paint.Style.STROKE);
        }
        this.mStrokePaint.setStrokeWidth((float) width);
        this.mStrokePaint.setColor(color);
        DashPathEffect e = null;
        if (dashWidth > 0.0f) {
            e = new DashPathEffect(new float[]{dashWidth, dashGap}, 0.0f);
        }
        this.mStrokePaint.setPathEffect(e);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public void setSize(int width, int height) {
        this.mGradientState.setSize(width, height);
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public void setShape(int shape) {
        this.mRingPath = null;
        this.mPathIsDirty = true;
        this.mGradientState.setShape(shape);
        invalidateSelf();
    }

    public int getShape() {
        return this.mGradientState.mShape;
    }

    public void setGradientType(int gradient) {
        this.mGradientState.setGradientType(gradient);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public int getGradientType() {
        return this.mGradientState.mGradient;
    }

    public void setGradientCenter(float x, float y) {
        this.mGradientState.setGradientCenter(x, y);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public float getGradientCenterX() {
        return this.mGradientState.mCenterX;
    }

    public float getGradientCenterY() {
        return this.mGradientState.mCenterY;
    }

    public void setGradientRadius(float gradientRadius) {
        this.mGradientState.setGradientRadius(gradientRadius, 0);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public float getGradientRadius() {
        if (this.mGradientState.mGradient != 1) {
            return 0.0f;
        }
        ensureValidRect();
        return this.mGradientRadius;
    }

    public void setUseLevel(boolean useLevel) {
        this.mGradientState.mUseLevel = useLevel;
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public boolean getUseLevel() {
        return this.mGradientState.mUseLevel;
    }

    private int modulateAlpha(int alpha) {
        int i = this.mAlpha;
        return (alpha * (i + (i >> 7))) >> 8;
    }

    public Orientation getOrientation() {
        return this.mGradientState.getOrientation();
    }

    public void setOrientation(Orientation orientation) {
        this.mGradientState.setOrientation(orientation);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public void setColors(int[] colors) {
        setColors(colors, null);
    }

    public void setColors(int[] colors, float[] offsets) {
        this.mGradientState.setGradientColors(colors);
        this.mGradientState.mPositions = offsets;
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public int[] getColors() {
        if (this.mGradientState.mGradientColors == null) {
            return null;
        }
        return (int[]) this.mGradientState.mGradientColors.clone();
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        GradientState st;
        ColorFilter colorFilter;
        Paint paint;
        if (this.mHwShader != null) {
            this.mHwShader.draw(canvas);
        } else if (ensureValidRect()) {
            int prevFillAlpha = this.mFillPaint.getAlpha();
            Paint paint2 = this.mStrokePaint;
            boolean useLayer = false;
            int prevStrokeAlpha = paint2 != null ? paint2.getAlpha() : 0;
            int currFillAlpha = modulateAlpha(prevFillAlpha);
            int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
            boolean haveStroke = currStrokeAlpha > 0 && (paint = this.mStrokePaint) != null && paint.getStrokeWidth() > 0.0f;
            boolean haveFill = currFillAlpha > 0;
            GradientState st2 = this.mGradientState;
            ColorFilter colorFilter2 = this.mColorFilter;
            if (colorFilter2 == null) {
                colorFilter2 = this.mBlendModeColorFilter;
            }
            if (haveStroke && haveFill && st2.mShape != 2 && currStrokeAlpha < 255 && (this.mAlpha < 255 || colorFilter2 != null)) {
                useLayer = true;
            }
            if (useLayer) {
                if (this.mLayerPaint == null) {
                    this.mLayerPaint = new Paint();
                }
                this.mLayerPaint.setDither(st2.mDither);
                this.mLayerPaint.setAlpha(this.mAlpha);
                this.mLayerPaint.setColorFilter(colorFilter2);
                float rad = this.mStrokePaint.getStrokeWidth();
                colorFilter = colorFilter2;
                st = st2;
                canvas.saveLayer(this.mRect.left - rad, this.mRect.top - rad, this.mRect.right + rad, this.mRect.bottom + rad, this.mLayerPaint);
                this.mFillPaint.setColorFilter(null);
                this.mStrokePaint.setColorFilter(null);
            } else {
                colorFilter = colorFilter2;
                st = st2;
                this.mFillPaint.setAlpha(currFillAlpha);
                this.mFillPaint.setDither(st.mDither);
                this.mFillPaint.setColorFilter(colorFilter);
                if (colorFilter != null && st.mSolidColors == null) {
                    this.mFillPaint.setColor(this.mAlpha << 24);
                }
                if (haveStroke) {
                    this.mStrokePaint.setAlpha(currStrokeAlpha);
                    this.mStrokePaint.setDither(st.mDither);
                    this.mStrokePaint.setColorFilter(colorFilter);
                }
            }
            int i = st.mShape;
            if (i != 0) {
                if (i == 1) {
                    canvas.drawOval(this.mRect, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawOval(this.mRect, this.mStrokePaint);
                    }
                } else if (i == 2) {
                    RectF r = this.mRect;
                    float y = r.centerY();
                    if (haveStroke) {
                        canvas.drawLine(r.left, y, r.right, y, this.mStrokePaint);
                    }
                } else if (i == 3) {
                    Path path = buildRing(st);
                    canvas.drawPath(path, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawPath(path, this.mStrokePaint);
                    }
                }
            } else if (st.mRadiusArray != null) {
                buildPathIfDirty();
                canvas.drawPath(this.mPath, this.mFillPaint);
                if (haveStroke) {
                    canvas.drawPath(this.mPath, this.mStrokePaint);
                }
            } else if (st.mRadius > 0.0f) {
                float rad2 = Math.min(st.mRadius, Math.min(this.mRect.width(), this.mRect.height()) * 0.5f);
                canvas.drawRoundRect(this.mRect, rad2, rad2, this.mFillPaint);
                if (haveStroke) {
                    canvas.drawRoundRect(this.mRect, rad2, rad2, this.mStrokePaint);
                }
            } else {
                if (!(this.mFillPaint.getColor() == 0 && colorFilter == null && this.mFillPaint.getShader() == null)) {
                    canvas.drawRect(this.mRect, this.mFillPaint);
                }
                if (haveStroke) {
                    canvas.drawRect(this.mRect, this.mStrokePaint);
                }
            }
            if (useLayer) {
                canvas.restore();
                return;
            }
            this.mFillPaint.setAlpha(prevFillAlpha);
            if (haveStroke) {
                this.mStrokePaint.setAlpha(prevStrokeAlpha);
            }
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setXfermode(Xfermode mode) {
        super.setXfermode(mode);
        this.mFillPaint.setXfermode(mode);
    }

    public void setAntiAlias(boolean aa) {
        this.mFillPaint.setAntiAlias(aa);
    }

    private void buildPathIfDirty() {
        GradientState st = this.mGradientState;
        if (this.mPathIsDirty) {
            ensureValidRect();
            this.mPath.reset();
            this.mPath.addRoundRect(this.mRect, st.mRadiusArray, Path.Direction.CW);
            this.mPathIsDirty = false;
        }
    }

    public void setInnerRadiusRatio(float innerRadiusRatio) {
        if (innerRadiusRatio > 0.0f) {
            this.mGradientState.mInnerRadiusRatio = innerRadiusRatio;
            this.mPathIsDirty = true;
            invalidateSelf();
            return;
        }
        throw new IllegalArgumentException("Ratio must be greater than zero");
    }

    public float getInnerRadiusRatio() {
        return this.mGradientState.mInnerRadiusRatio;
    }

    public void setInnerRadius(int innerRadius) {
        this.mGradientState.mInnerRadius = innerRadius;
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public int getInnerRadius() {
        return this.mGradientState.mInnerRadius;
    }

    public void setThicknessRatio(float thicknessRatio) {
        if (thicknessRatio > 0.0f) {
            this.mGradientState.mThicknessRatio = thicknessRatio;
            this.mPathIsDirty = true;
            invalidateSelf();
            return;
        }
        throw new IllegalArgumentException("Ratio must be greater than zero");
    }

    public float getThicknessRatio() {
        return this.mGradientState.mThicknessRatio;
    }

    public void setThickness(int thickness) {
        this.mGradientState.mThickness = thickness;
        this.mPathIsDirty = true;
        invalidateSelf();
    }

    public int getThickness() {
        return this.mGradientState.mThickness;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        if (this.mGradientState.mPadding == null) {
            this.mGradientState.mPadding = new Rect();
        }
        this.mGradientState.mPadding.set(left, top, right, bottom);
        this.mPadding = this.mGradientState.mPadding;
        invalidateSelf();
    }

    private Path buildRing(GradientState st) {
        if (this.mRingPath != null && (!st.mUseLevelForShape || !this.mPathIsDirty)) {
            return this.mRingPath;
        }
        this.mPathIsDirty = false;
        float sweep = st.mUseLevelForShape ? (((float) getLevel()) * 360.0f) / 10000.0f : 360.0f;
        RectF bounds = new RectF(this.mRect);
        float x = bounds.width() / 2.0f;
        float y = bounds.height() / 2.0f;
        float thickness = st.mThickness != -1 ? (float) st.mThickness : bounds.width() / st.mThicknessRatio;
        float radius = st.mInnerRadius != -1 ? (float) st.mInnerRadius : bounds.width() / st.mInnerRadiusRatio;
        RectF innerBounds = new RectF(bounds);
        innerBounds.inset(x - radius, y - radius);
        RectF bounds2 = new RectF(innerBounds);
        bounds2.inset(-thickness, -thickness);
        Path path = this.mRingPath;
        if (path == null) {
            this.mRingPath = new Path();
        } else {
            path.reset();
        }
        Path ringPath = this.mRingPath;
        if (sweep >= 360.0f || sweep <= -360.0f) {
            ringPath.addOval(bounds2, Path.Direction.CW);
            ringPath.addOval(innerBounds, Path.Direction.CCW);
        } else {
            ringPath.setFillType(Path.FillType.EVEN_ODD);
            ringPath.moveTo(x + radius, y);
            ringPath.lineTo(x + radius + thickness, y);
            ringPath.arcTo(bounds2, 0.0f, sweep, false);
            ringPath.arcTo(innerBounds, sweep, -sweep, false);
            ringPath.close();
        }
        return ringPath;
    }

    public void setColor(int argb) {
        this.mGradientState.setSolidColors(ColorStateList.valueOf(argb));
        this.mFillPaint.setColor(argb);
        invalidateSelf();
    }

    public void setColor(ColorStateList colorStateList) {
        if (colorStateList == null) {
            setColor(0);
            return;
        }
        int color = colorStateList.getColorForState(getState(), 0);
        this.mGradientState.setSolidColors(colorStateList);
        this.mFillPaint.setColor(color);
        invalidateSelf();
    }

    public ColorStateList getColor() {
        return this.mGradientState.mSolidColors;
    }

    /* JADX INFO: Multiple debug info for r4v0 android.graphics.Paint: [D('strokePaint' android.graphics.Paint), D('newColor' int)] */
    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onStateChange(int[] stateSet) {
        ColorStateList strokeColors;
        int newColor;
        int newColor2;
        boolean invalidateSelf = false;
        GradientState s = this.mGradientState;
        ColorStateList solidColors = s.mSolidColors;
        if (!(solidColors == null || this.mFillPaint.getColor() == (newColor2 = solidColors.getColorForState(stateSet, 0)))) {
            this.mFillPaint.setColor(newColor2);
            invalidateSelf = true;
        }
        Paint strokePaint = this.mStrokePaint;
        if (!(strokePaint == null || (strokeColors = s.mStrokeColors) == null || strokePaint.getColor() == (newColor = strokeColors.getColorForState(stateSet, 0)))) {
            strokePaint.setColor(newColor);
            invalidateSelf = true;
        }
        if (!(s.mTint == null || s.mBlendMode == null)) {
            this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, s.mTint, s.mBlendMode);
            invalidateSelf = true;
        }
        if (!invalidateSelf) {
            return false;
        }
        invalidateSelf();
        return true;
    }

    @Override // android.graphics.drawable.Drawable
    public boolean isStateful() {
        GradientState s = this.mGradientState;
        return super.isStateful() || (s.mSolidColors != null && s.mSolidColors.isStateful()) || ((s.mStrokeColors != null && s.mStrokeColors.isStateful()) || (s.mTint != null && s.mTint.isStateful()));
    }

    @Override // android.graphics.drawable.Drawable
    public boolean hasFocusStateSpecified() {
        GradientState s = this.mGradientState;
        return (s.mSolidColors != null && s.mSolidColors.hasFocusStateSpecified()) || (s.mStrokeColors != null && s.mStrokeColors.hasFocusStateSpecified()) || (s.mTint != null && s.mTint.hasFocusStateSpecified());
    }

    @Override // android.graphics.drawable.Drawable
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mGradientState.getChangingConfigurations();
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        if (alpha != this.mAlpha) {
            this.mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        if (dither != this.mGradientState.mDither) {
            this.mGradientState.mDither = dither;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        if (colorFilter != this.mColorFilter) {
            this.mColorFilter = colorFilter;
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintList(ColorStateList tint) {
        GradientState gradientState = this.mGradientState;
        gradientState.mTint = tint;
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, tint, gradientState.mBlendMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setTintBlendMode(BlendMode blendMode) {
        GradientState gradientState = this.mGradientState;
        gradientState.mBlendMode = blendMode;
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, gradientState.mTint, blendMode);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return (this.mAlpha != 255 || !this.mGradientState.mOpaqueOverBounds || !isOpaqueForState()) ? -3 : -1;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        if (this.mHwShader != null) {
            this.mHwShader.setBounds(r);
        }
        this.mRingPath = null;
        this.mPathIsDirty = true;
        this.mGradientIsDirty = true;
    }

    /* access modifiers changed from: protected */
    @Override // android.graphics.drawable.Drawable
    public boolean onLevelChange(int level) {
        super.onLevelChange(level);
        this.mGradientIsDirty = true;
        this.mPathIsDirty = true;
        invalidateSelf();
        return true;
    }

    private boolean ensureValidRect() {
        float[] tempPositions;
        float radius;
        float y1;
        float x1;
        float y0;
        float x0;
        if (this.mGradientIsDirty) {
            this.mGradientIsDirty = false;
            Rect bounds = getBounds();
            float inset = 0.0f;
            Paint paint = this.mStrokePaint;
            if (paint != null) {
                inset = paint.getStrokeWidth() * 0.5f;
            }
            GradientState st = this.mGradientState;
            this.mRect.set(((float) bounds.left) + inset, ((float) bounds.top) + inset, ((float) bounds.right) - inset, ((float) bounds.bottom) - inset);
            int[] gradientColors = st.mGradientColors;
            if (gradientColors != null) {
                RectF r = this.mRect;
                float level = 1.0f;
                if (st.mGradient == 0) {
                    if (st.mUseLevel) {
                        level = ((float) getLevel()) / 10000.0f;
                    }
                    switch (st.getOrientation()) {
                        case TOP_BOTTOM:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = x0;
                            y1 = r.bottom * level;
                            break;
                        case TR_BL:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = r.left * level;
                            y1 = r.bottom * level;
                            break;
                        case RIGHT_LEFT:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = r.left * level;
                            y1 = y0;
                            break;
                        case BR_TL:
                            x0 = r.right;
                            y0 = r.bottom;
                            x1 = r.left * level;
                            y1 = r.top * level;
                            break;
                        case BOTTOM_TOP:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = x0;
                            y1 = r.top * level;
                            break;
                        case BL_TR:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = r.right * level;
                            y1 = r.top * level;
                            break;
                        case LEFT_RIGHT:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = r.right * level;
                            y1 = y0;
                            break;
                        default:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = r.right * level;
                            y1 = r.bottom * level;
                            break;
                    }
                    this.mFillPaint.setShader(new LinearGradient(x0, y0, x1, y1, gradientColors, st.mPositions, Shader.TileMode.CLAMP));
                } else if (st.mGradient == 1) {
                    float x02 = r.left + ((r.right - r.left) * st.mCenterX);
                    float y02 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    float radius2 = st.mGradientRadius;
                    if (st.mGradientRadiusType == 1) {
                        radius2 *= Math.min(st.mWidth >= 0 ? (float) st.mWidth : r.width(), st.mHeight >= 0 ? (float) st.mHeight : r.height());
                    } else if (st.mGradientRadiusType == 2) {
                        radius2 *= Math.min(r.width(), r.height());
                    }
                    if (st.mUseLevel) {
                        radius2 *= ((float) getLevel()) / 10000.0f;
                    }
                    this.mGradientRadius = radius2;
                    if (radius2 <= 0.0f) {
                        radius = 0.001f;
                    } else {
                        radius = radius2;
                    }
                    this.mFillPaint.setShader(new RadialGradient(x02, y02, radius, gradientColors, (float[]) null, Shader.TileMode.CLAMP));
                } else if (st.mGradient == 2) {
                    float x03 = r.left + ((r.right - r.left) * st.mCenterX);
                    float y03 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    int[] tempColors = gradientColors;
                    if (st.mUseLevel) {
                        tempColors = st.mTempColors;
                        int length = gradientColors.length;
                        if (tempColors == null || tempColors.length != length + 1) {
                            int[] iArr = new int[(length + 1)];
                            st.mTempColors = iArr;
                            tempColors = iArr;
                        }
                        System.arraycopy(gradientColors, 0, tempColors, 0, length);
                        tempColors[length] = gradientColors[length - 1];
                        tempPositions = st.mTempPositions;
                        float fraction = 1.0f / ((float) (length - 1));
                        if (tempPositions == null || tempPositions.length != length + 1) {
                            float[] fArr = new float[(length + 1)];
                            st.mTempPositions = fArr;
                            tempPositions = fArr;
                        }
                        float level2 = ((float) getLevel()) / 10000.0f;
                        for (int i = 0; i < length; i++) {
                            tempPositions[i] = ((float) i) * fraction * level2;
                        }
                        tempPositions[length] = 1.0f;
                    } else {
                        tempPositions = null;
                    }
                    this.mFillPaint.setShader(new SweepGradient(x03, y03, tempColors, tempPositions));
                }
                if (st.mSolidColors == null) {
                    this.mFillPaint.setColor(-16777216);
                }
            }
        }
        return !this.mRect.isEmpty();
    }

    @Override // android.graphics.drawable.Drawable
    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        this.mGradientState.setDensity(Drawable.resolveDensity(r, 0));
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        updateLocalState(r);
    }

    @Override // android.graphics.drawable.Drawable
    public void applyTheme(Resources.Theme t) {
        super.applyTheme(t);
        GradientState state = this.mGradientState;
        if (state != null) {
            state.setDensity(Drawable.resolveDensity(t.getResources(), 0));
            if (state.mThemeAttrs != null) {
                TypedArray a = t.resolveAttributes(state.mThemeAttrs, R.styleable.GradientDrawable);
                updateStateFromTypedArray(a);
                a.recycle();
            }
            if (state.mTint != null && state.mTint.canApplyTheme()) {
                state.mTint = state.mTint.obtainForTheme(t);
            }
            if (state.mSolidColors != null && state.mSolidColors.canApplyTheme()) {
                state.mSolidColors = state.mSolidColors.obtainForTheme(t);
            }
            if (state.mStrokeColors != null && state.mStrokeColors.canApplyTheme()) {
                state.mStrokeColors = state.mStrokeColors.obtainForTheme(t);
            }
            applyThemeChildElements(t);
            updateLocalState(t.getResources());
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        GradientState state = this.mGradientState;
        state.mChangingConfigurations |= a.getChangingConfigurations();
        state.mThemeAttrs = a.extractThemeAttrs();
        state.mShape = a.getInt(3, state.mShape);
        state.mDither = a.getBoolean(0, state.mDither);
        if (state.mShape == 3) {
            state.mInnerRadius = a.getDimensionPixelSize(7, state.mInnerRadius);
            if (state.mInnerRadius == -1) {
                state.mInnerRadiusRatio = a.getFloat(4, state.mInnerRadiusRatio);
            }
            state.mThickness = a.getDimensionPixelSize(8, state.mThickness);
            if (state.mThickness == -1) {
                state.mThicknessRatio = a.getFloat(5, state.mThicknessRatio);
            }
            state.mUseLevelForShape = a.getBoolean(6, state.mUseLevelForShape);
        }
        int tintMode = a.getInt(9, -1);
        if (tintMode != -1) {
            state.mBlendMode = Drawable.parseBlendMode(tintMode, BlendMode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(1);
        if (tint != null) {
            state.mTint = tint;
        }
        state.mOpticalInsets = Insets.of(a.getDimensionPixelSize(10, state.mOpticalInsets.left), a.getDimensionPixelSize(11, state.mOpticalInsets.top), a.getDimensionPixelSize(12, state.mOpticalInsets.right), a.getDimensionPixelSize(13, state.mOpticalInsets.bottom));
    }

    @Override // android.graphics.drawable.Drawable
    public boolean canApplyTheme() {
        GradientState gradientState = this.mGradientState;
        return (gradientState != null && gradientState.canApplyTheme()) || super.canApplyTheme();
    }

    private void applyThemeChildElements(Resources.Theme t) {
        GradientState st = this.mGradientState;
        if (st.mAttrSize != null) {
            TypedArray a = t.resolveAttributes(st.mAttrSize, R.styleable.GradientDrawableSize);
            updateGradientDrawableSize(a);
            a.recycle();
        }
        if (st.mAttrGradient != null) {
            TypedArray a2 = t.resolveAttributes(st.mAttrGradient, R.styleable.GradientDrawableGradient);
            try {
                updateGradientDrawableGradient(t.getResources(), a2);
            } finally {
                a2.recycle();
            }
        }
        if (st.mAttrSolid != null) {
            TypedArray a3 = t.resolveAttributes(st.mAttrSolid, R.styleable.GradientDrawableSolid);
            updateGradientDrawableSolid(a3);
            a3.recycle();
        }
        if (st.mAttrStroke != null) {
            TypedArray a4 = t.resolveAttributes(st.mAttrStroke, R.styleable.GradientDrawableStroke);
            updateGradientDrawableStroke(a4);
            a4.recycle();
        }
        if (st.mAttrCorners != null) {
            TypedArray a5 = t.resolveAttributes(st.mAttrCorners, R.styleable.DrawableCorners);
            updateDrawableCorners(a5);
            a5.recycle();
        }
        if (st.mAttrPadding != null) {
            TypedArray a6 = t.resolveAttributes(st.mAttrPadding, R.styleable.GradientDrawablePadding);
            updateGradientDrawablePadding(a6);
            a6.recycle();
        }
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        int innerDepth = parser.getDepth() + 1;
        while (true) {
            int type = parser.next();
            if (type != 1) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (type == 2 && depth <= innerDepth) {
                    String name = parser.getName();
                    if (name.equals(DumpHeapActivity.KEY_SIZE)) {
                        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSize);
                        updateGradientDrawableSize(a);
                        a.recycle();
                    } else if (name.equals("gradient")) {
                        TypedArray a2 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableGradient);
                        updateGradientDrawableGradient(r, a2);
                        a2.recycle();
                    } else if (name.equals("solid")) {
                        TypedArray a3 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSolid);
                        updateGradientDrawableSolid(a3);
                        a3.recycle();
                    } else if (name.equals("stroke")) {
                        TypedArray a4 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableStroke);
                        updateGradientDrawableStroke(a4);
                        a4.recycle();
                    } else if (name.equals("corners")) {
                        TypedArray a5 = obtainAttributes(r, theme, attrs, R.styleable.DrawableCorners);
                        updateDrawableCorners(a5);
                        a5.recycle();
                    } else if (name.equals("padding")) {
                        TypedArray a6 = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawablePadding);
                        updateGradientDrawablePadding(a6);
                        a6.recycle();
                    } else {
                        Log.w("drawable", "Bad element under <shape>: " + name);
                    }
                }
            } else {
                return;
            }
        }
    }

    private void updateGradientDrawablePadding(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrPadding = a.extractThemeAttrs();
        if (st.mPadding == null) {
            st.mPadding = new Rect();
        }
        Rect pad = st.mPadding;
        pad.set(a.getDimensionPixelOffset(0, pad.left), a.getDimensionPixelOffset(1, pad.top), a.getDimensionPixelOffset(2, pad.right), a.getDimensionPixelOffset(3, pad.bottom));
        this.mPadding = pad;
    }

    private void updateDrawableCorners(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrCorners = a.extractThemeAttrs();
        int radius = a.getDimensionPixelSize(0, (int) st.mRadius);
        setCornerRadius((float) radius);
        int topLeftRadius = a.getDimensionPixelSize(1, radius);
        int topRightRadius = a.getDimensionPixelSize(2, radius);
        int bottomLeftRadius = a.getDimensionPixelSize(3, radius);
        int bottomRightRadius = a.getDimensionPixelSize(4, radius);
        if (topLeftRadius != radius || topRightRadius != radius || bottomLeftRadius != radius || bottomRightRadius != radius) {
            setCornerRadii(new float[]{(float) topLeftRadius, (float) topLeftRadius, (float) topRightRadius, (float) topRightRadius, (float) bottomRightRadius, (float) bottomRightRadius, (float) bottomLeftRadius, (float) bottomLeftRadius});
        }
    }

    private void updateGradientDrawableStroke(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrStroke = a.extractThemeAttrs();
        int width = a.getDimensionPixelSize(0, Math.max(0, st.mStrokeWidth));
        float dashWidth = a.getDimension(2, st.mStrokeDashWidth);
        ColorStateList colorStateList = a.getColorStateList(1);
        if (colorStateList == null) {
            colorStateList = st.mStrokeColors;
        }
        if (dashWidth != 0.0f) {
            setStroke(width, colorStateList, dashWidth, a.getDimension(3, st.mStrokeDashGap));
        } else {
            setStroke(width, colorStateList);
        }
    }

    private void updateGradientDrawableSolid(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrSolid = a.extractThemeAttrs();
        ColorStateList colorStateList = a.getColorStateList(0);
        if (colorStateList != null) {
            setColor(colorStateList);
        }
    }

    private void updateGradientDrawableGradient(Resources r, TypedArray a) {
        int prevEnd;
        int radiusType;
        float radius;
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrGradient = a.extractThemeAttrs();
        st.mCenterX = getFloatOrFraction(a, 5, st.mCenterX);
        st.mCenterY = getFloatOrFraction(a, 6, st.mCenterY);
        st.mUseLevel = a.getBoolean(2, st.mUseLevel);
        st.mGradient = a.getInt(4, st.mGradient);
        boolean hasGradientColors = st.mGradientColors != null;
        boolean hasGradientCenter = st.hasCenterColor();
        int prevStart = hasGradientColors ? st.mGradientColors[0] : 0;
        int prevCenter = hasGradientCenter ? st.mGradientColors[1] : 0;
        if (st.hasCenterColor()) {
            prevEnd = st.mGradientColors[2];
        } else if (hasGradientColors) {
            prevEnd = st.mGradientColors[1];
        } else {
            prevEnd = 0;
        }
        int startColor = a.getColor(0, prevStart);
        boolean hasCenterColor = a.hasValue(8) || hasGradientCenter;
        int centerColor = a.getColor(8, prevCenter);
        int endColor = a.getColor(1, prevEnd);
        if (hasCenterColor) {
            st.mGradientColors = new int[3];
            st.mGradientColors[0] = startColor;
            st.mGradientColors[1] = centerColor;
            st.mGradientColors[2] = endColor;
            st.mPositions = new float[3];
            st.mPositions[0] = 0.0f;
            st.mPositions[1] = st.mCenterX != 0.5f ? st.mCenterX : st.mCenterY;
            st.mPositions[2] = 1.0f;
        } else {
            st.mGradientColors = new int[2];
            st.mGradientColors[0] = startColor;
            st.mGradientColors[1] = endColor;
        }
        st.mAngle = ((((int) a.getFloat(3, (float) st.mAngle)) % 360) + 360) % 360;
        TypedValue tv = a.peekValue(7);
        if (tv != null) {
            if (tv.type == 6) {
                radius = tv.getFraction(1.0f, 1.0f);
                if (((tv.data >> 0) & 15) == 1) {
                    radiusType = 2;
                } else {
                    radiusType = 1;
                }
            } else if (tv.type == 5) {
                radius = tv.getDimension(r.getDisplayMetrics());
                radiusType = 0;
            } else {
                radius = tv.getFloat();
                radiusType = 0;
            }
            st.mGradientRadius = radius;
            st.mGradientRadiusType = radiusType;
        }
    }

    private void updateGradientDrawableSize(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrSize = a.extractThemeAttrs();
        st.mWidth = a.getDimensionPixelSize(1, st.mWidth);
        st.mHeight = a.getDimensionPixelSize(0, st.mHeight);
    }

    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
        TypedValue tv = a.peekValue(index);
        if (tv == null) {
            return defaultValue;
        }
        return tv.type == 6 ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mGradientState.mWidth;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mGradientState.mHeight;
    }

    @Override // android.graphics.drawable.Drawable
    public Insets getOpticalInsets() {
        return this.mGradientState.mOpticalInsets;
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable.ConstantState getConstantState() {
        this.mGradientState.mChangingConfigurations = getChangingConfigurations();
        return this.mGradientState;
    }

    private boolean isOpaqueForState() {
        Paint paint;
        if (this.mGradientState.mStrokeWidth >= 0 && (paint = this.mStrokePaint) != null && !isOpaque(paint.getColor())) {
            return false;
        }
        if (this.mGradientState.mGradientColors != null || isOpaque(this.mFillPaint.getColor())) {
            return true;
        }
        return false;
    }

    @Override // android.graphics.drawable.Drawable
    public void getOutline(Outline outline) {
        float f;
        Paint paint;
        GradientState st = this.mGradientState;
        Rect bounds = getBounds();
        if (st.mOpaqueOverShape && (this.mGradientState.mStrokeWidth <= 0 || (paint = this.mStrokePaint) == null || paint.getAlpha() == this.mFillPaint.getAlpha())) {
            f = ((float) modulateAlpha(this.mFillPaint.getAlpha())) / 255.0f;
        } else {
            f = 0.0f;
        }
        outline.setAlpha(f);
        int i = st.mShape;
        if (i != 0) {
            if (i == 1) {
                outline.setOval(bounds);
            } else if (i == 2) {
                Paint paint2 = this.mStrokePaint;
                float halfStrokeWidth = paint2 == null ? 1.0E-4f : paint2.getStrokeWidth() * 0.5f;
                float centerY = (float) bounds.centerY();
                outline.setRect(bounds.left, (int) Math.floor((double) (centerY - halfStrokeWidth)), bounds.right, (int) Math.ceil((double) (centerY + halfStrokeWidth)));
            }
        } else if (st.mRadiusArray != null) {
            buildPathIfDirty();
            outline.setConvexPath(this.mPath);
        } else {
            float rad = 0.0f;
            if (st.mRadius > 0.0f) {
                rad = Math.min(st.mRadius, ((float) Math.min(bounds.width(), bounds.height())) * 0.5f);
            }
            outline.setRoundRect(bounds, rad);
        }
    }

    @Override // android.graphics.drawable.Drawable
    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mGradientState = new GradientState(this.mGradientState, (Resources) null);
            updateLocalState(null);
            this.mMutated = true;
        }
        return this;
    }

    @Override // android.graphics.drawable.Drawable
    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
    }

    /* access modifiers changed from: package-private */
    public static final class GradientState extends Drawable.ConstantState {
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int mAngle = 0;
        int[] mAttrCorners;
        int[] mAttrGradient;
        int[] mAttrPadding;
        int[] mAttrSize;
        int[] mAttrSolid;
        int[] mAttrStroke;
        BlendMode mBlendMode = Drawable.DEFAULT_BLEND_MODE;
        float mCenterX = 0.5f;
        float mCenterY = 0.5f;
        public int mChangingConfigurations;
        int mDensity = 160;
        public boolean mDither = false;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int mGradient = 0;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int[] mGradientColors;
        float mGradientRadius = 0.5f;
        int mGradientRadiusType = 0;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int mHeight = -1;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int mInnerRadius = -1;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public float mInnerRadiusRatio = GradientDrawable.DEFAULT_INNER_RADIUS_RATIO;
        boolean mOpaqueOverBounds;
        boolean mOpaqueOverShape;
        public Insets mOpticalInsets = Insets.NONE;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public Orientation mOrientation;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public Rect mPadding = null;
        @UnsupportedAppUsage
        public float[] mPositions;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public float mRadius = 0.0f;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public float[] mRadiusArray = null;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int mShape = 0;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public ColorStateList mSolidColors;
        public ColorStateList mStrokeColors;
        @UnsupportedAppUsage(trackingBug = 124050917)
        public float mStrokeDashGap = 0.0f;
        @UnsupportedAppUsage(trackingBug = 124050917)
        public float mStrokeDashWidth = 0.0f;
        @UnsupportedAppUsage(trackingBug = 124050917)
        public int mStrokeWidth = -1;
        public int[] mTempColors;
        public float[] mTempPositions;
        int[] mThemeAttrs;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050218)
        public int mThickness = -1;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050218)
        public float mThicknessRatio = GradientDrawable.DEFAULT_THICKNESS_RATIO;
        ColorStateList mTint = null;
        boolean mUseLevel = false;
        boolean mUseLevelForShape = true;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050917)
        public int mWidth = -1;

        public GradientState(Orientation orientation, int[] gradientColors) {
            setOrientation(orientation);
            setGradientColors(gradientColors);
        }

        public GradientState(GradientState orig, Resources res) {
            this.mChangingConfigurations = orig.mChangingConfigurations;
            this.mShape = orig.mShape;
            this.mGradient = orig.mGradient;
            this.mAngle = orig.mAngle;
            this.mOrientation = orig.mOrientation;
            this.mSolidColors = orig.mSolidColors;
            int[] iArr = orig.mGradientColors;
            if (iArr != null) {
                this.mGradientColors = (int[]) iArr.clone();
            }
            float[] fArr = orig.mPositions;
            if (fArr != null) {
                this.mPositions = (float[]) fArr.clone();
            }
            this.mStrokeColors = orig.mStrokeColors;
            this.mStrokeWidth = orig.mStrokeWidth;
            this.mStrokeDashWidth = orig.mStrokeDashWidth;
            this.mStrokeDashGap = orig.mStrokeDashGap;
            this.mRadius = orig.mRadius;
            float[] fArr2 = orig.mRadiusArray;
            if (fArr2 != null) {
                this.mRadiusArray = (float[]) fArr2.clone();
            }
            Rect rect = orig.mPadding;
            if (rect != null) {
                this.mPadding = new Rect(rect);
            }
            this.mWidth = orig.mWidth;
            this.mHeight = orig.mHeight;
            this.mInnerRadiusRatio = orig.mInnerRadiusRatio;
            this.mThicknessRatio = orig.mThicknessRatio;
            this.mInnerRadius = orig.mInnerRadius;
            this.mThickness = orig.mThickness;
            this.mDither = orig.mDither;
            this.mOpticalInsets = orig.mOpticalInsets;
            this.mCenterX = orig.mCenterX;
            this.mCenterY = orig.mCenterY;
            this.mGradientRadius = orig.mGradientRadius;
            this.mGradientRadiusType = orig.mGradientRadiusType;
            this.mUseLevel = orig.mUseLevel;
            this.mUseLevelForShape = orig.mUseLevelForShape;
            this.mOpaqueOverBounds = orig.mOpaqueOverBounds;
            this.mOpaqueOverShape = orig.mOpaqueOverShape;
            this.mTint = orig.mTint;
            this.mBlendMode = orig.mBlendMode;
            this.mThemeAttrs = orig.mThemeAttrs;
            this.mAttrSize = orig.mAttrSize;
            this.mAttrGradient = orig.mAttrGradient;
            this.mAttrSolid = orig.mAttrSolid;
            this.mAttrStroke = orig.mAttrStroke;
            this.mAttrCorners = orig.mAttrCorners;
            this.mAttrPadding = orig.mAttrPadding;
            this.mDensity = Drawable.resolveDensity(res, orig.mDensity);
            int i = orig.mDensity;
            int i2 = this.mDensity;
            if (i != i2) {
                applyDensityScaling(i, i2);
            }
        }

        public final void setDensity(int targetDensity) {
            if (this.mDensity != targetDensity) {
                int sourceDensity = this.mDensity;
                this.mDensity = targetDensity;
                applyDensityScaling(sourceDensity, targetDensity);
            }
        }

        public boolean hasCenterColor() {
            int[] iArr = this.mGradientColors;
            return iArr != null && iArr.length == 3;
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            int i = this.mInnerRadius;
            if (i > 0) {
                this.mInnerRadius = Drawable.scaleFromDensity(i, sourceDensity, targetDensity, true);
            }
            int i2 = this.mThickness;
            if (i2 > 0) {
                this.mThickness = Drawable.scaleFromDensity(i2, sourceDensity, targetDensity, true);
            }
            if (this.mOpticalInsets != Insets.NONE) {
                this.mOpticalInsets = Insets.of(Drawable.scaleFromDensity(this.mOpticalInsets.left, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(this.mOpticalInsets.top, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(this.mOpticalInsets.right, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(this.mOpticalInsets.bottom, sourceDensity, targetDensity, true));
            }
            Rect rect = this.mPadding;
            if (rect != null) {
                rect.left = Drawable.scaleFromDensity(rect.left, sourceDensity, targetDensity, false);
                Rect rect2 = this.mPadding;
                rect2.top = Drawable.scaleFromDensity(rect2.top, sourceDensity, targetDensity, false);
                Rect rect3 = this.mPadding;
                rect3.right = Drawable.scaleFromDensity(rect3.right, sourceDensity, targetDensity, false);
                Rect rect4 = this.mPadding;
                rect4.bottom = Drawable.scaleFromDensity(rect4.bottom, sourceDensity, targetDensity, false);
            }
            float f = this.mRadius;
            if (f > 0.0f) {
                this.mRadius = Drawable.scaleFromDensity(f, sourceDensity, targetDensity);
            }
            float[] fArr = this.mRadiusArray;
            if (fArr != null) {
                fArr[0] = (float) Drawable.scaleFromDensity((int) fArr[0], sourceDensity, targetDensity, true);
                float[] fArr2 = this.mRadiusArray;
                fArr2[1] = (float) Drawable.scaleFromDensity((int) fArr2[1], sourceDensity, targetDensity, true);
                float[] fArr3 = this.mRadiusArray;
                fArr3[2] = (float) Drawable.scaleFromDensity((int) fArr3[2], sourceDensity, targetDensity, true);
                float[] fArr4 = this.mRadiusArray;
                fArr4[3] = (float) Drawable.scaleFromDensity((int) fArr4[3], sourceDensity, targetDensity, true);
            }
            int i3 = this.mStrokeWidth;
            if (i3 > 0) {
                this.mStrokeWidth = Drawable.scaleFromDensity(i3, sourceDensity, targetDensity, true);
            }
            if (this.mStrokeDashWidth > 0.0f) {
                this.mStrokeDashWidth = Drawable.scaleFromDensity(this.mStrokeDashGap, sourceDensity, targetDensity);
            }
            float f2 = this.mStrokeDashGap;
            if (f2 > 0.0f) {
                this.mStrokeDashGap = Drawable.scaleFromDensity(f2, sourceDensity, targetDensity);
            }
            if (this.mGradientRadiusType == 0) {
                this.mGradientRadius = Drawable.scaleFromDensity(this.mGradientRadius, sourceDensity, targetDensity);
            }
            int i4 = this.mWidth;
            if (i4 > 0) {
                this.mWidth = Drawable.scaleFromDensity(i4, sourceDensity, targetDensity, true);
            }
            int i5 = this.mHeight;
            if (i5 > 0) {
                this.mHeight = Drawable.scaleFromDensity(i5, sourceDensity, targetDensity, true);
            }
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public boolean canApplyTheme() {
            ColorStateList colorStateList;
            ColorStateList colorStateList2;
            ColorStateList colorStateList3;
            return (this.mThemeAttrs == null && this.mAttrSize == null && this.mAttrGradient == null && this.mAttrSolid == null && this.mAttrStroke == null && this.mAttrCorners == null && this.mAttrPadding == null && ((colorStateList = this.mTint) == null || !colorStateList.canApplyTheme()) && (((colorStateList2 = this.mStrokeColors) == null || !colorStateList2.canApplyTheme()) && (((colorStateList3 = this.mSolidColors) == null || !colorStateList3.canApplyTheme()) && !super.canApplyTheme()))) ? false : true;
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable() {
            return new GradientDrawable(this, null);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public Drawable newDrawable(Resources res) {
            GradientState state;
            if (Drawable.resolveDensity(res, this.mDensity) != this.mDensity) {
                state = new GradientState(this, res);
            } else {
                state = this;
            }
            return new GradientDrawable(state, res);
        }

        @Override // android.graphics.drawable.Drawable.ConstantState
        public int getChangingConfigurations() {
            int i = this.mChangingConfigurations;
            ColorStateList colorStateList = this.mStrokeColors;
            int i2 = 0;
            int changingConfigurations = i | (colorStateList != null ? colorStateList.getChangingConfigurations() : 0);
            ColorStateList colorStateList2 = this.mSolidColors;
            int changingConfigurations2 = changingConfigurations | (colorStateList2 != null ? colorStateList2.getChangingConfigurations() : 0);
            ColorStateList colorStateList3 = this.mTint;
            if (colorStateList3 != null) {
                i2 = colorStateList3.getChangingConfigurations();
            }
            return changingConfigurations2 | i2;
        }

        public void setShape(int shape) {
            this.mShape = shape;
            computeOpacity();
        }

        public void setGradientType(int gradient) {
            this.mGradient = gradient;
        }

        public void setGradientCenter(float x, float y) {
            this.mCenterX = x;
            this.mCenterY = y;
        }

        public void setOrientation(Orientation orientation) {
            this.mAngle = getAngleFromOrientation(orientation);
            this.mOrientation = orientation;
        }

        public Orientation getOrientation() {
            updateGradientStateOrientation();
            return this.mOrientation;
        }

        private void updateGradientStateOrientation() {
            Orientation orientation;
            if (this.mGradient == 0) {
                int angle = this.mAngle;
                if (angle % 45 == 0) {
                    if (angle == 0) {
                        orientation = Orientation.LEFT_RIGHT;
                    } else if (angle == 45) {
                        orientation = Orientation.BL_TR;
                    } else if (angle == 90) {
                        orientation = Orientation.BOTTOM_TOP;
                    } else if (angle == 135) {
                        orientation = Orientation.BR_TL;
                    } else if (angle == 180) {
                        orientation = Orientation.RIGHT_LEFT;
                    } else if (angle == 225) {
                        orientation = Orientation.TR_BL;
                    } else if (angle == 270) {
                        orientation = Orientation.TOP_BOTTOM;
                    } else if (angle != 315) {
                        orientation = Orientation.LEFT_RIGHT;
                    } else {
                        orientation = Orientation.TL_BR;
                    }
                    this.mOrientation = orientation;
                    return;
                }
                throw new IllegalArgumentException("Linear gradient requires 'angle' attribute to be a multiple of 45");
            }
        }

        private int getAngleFromOrientation(Orientation orientation) {
            if (orientation == null) {
                return 0;
            }
            switch (orientation) {
                case TOP_BOTTOM:
                    return 270;
                case TR_BL:
                    return 225;
                case RIGHT_LEFT:
                    return 180;
                case BR_TL:
                    return 135;
                case BOTTOM_TOP:
                    return 90;
                case BL_TR:
                    return 45;
                case LEFT_RIGHT:
                default:
                    return 0;
                case TL_BR:
                    return 315;
            }
        }

        public void setGradientColors(int[] colors) {
            this.mGradientColors = colors;
            this.mSolidColors = null;
            computeOpacity();
        }

        public void setSolidColors(ColorStateList colors) {
            this.mGradientColors = null;
            this.mSolidColors = colors;
            computeOpacity();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void computeOpacity() {
            boolean z = false;
            this.mOpaqueOverBounds = false;
            this.mOpaqueOverShape = false;
            if (this.mGradientColors != null) {
                int i = 0;
                while (true) {
                    int[] iArr = this.mGradientColors;
                    if (i >= iArr.length) {
                        break;
                    } else if (GradientDrawable.isOpaque(iArr[i])) {
                        i++;
                    } else {
                        return;
                    }
                }
            }
            if (this.mGradientColors != null || this.mSolidColors != null) {
                this.mOpaqueOverShape = true;
                if (this.mShape == 0 && this.mRadius <= 0.0f && this.mRadiusArray == null) {
                    z = true;
                }
                this.mOpaqueOverBounds = z;
            }
        }

        public void setStroke(int width, ColorStateList colors, float dashWidth, float dashGap) {
            this.mStrokeWidth = width;
            this.mStrokeColors = colors;
            this.mStrokeDashWidth = dashWidth;
            this.mStrokeDashGap = dashGap;
            computeOpacity();
        }

        public void setCornerRadius(float radius) {
            if (radius < 0.0f) {
                radius = 0.0f;
            }
            this.mRadius = radius;
            this.mRadiusArray = null;
            computeOpacity();
        }

        public void setCornerRadii(float[] radii) {
            this.mRadiusArray = radii;
            if (radii == null) {
                this.mRadius = 0.0f;
            }
            computeOpacity();
        }

        public void setSize(int width, int height) {
            this.mWidth = width;
            this.mHeight = height;
        }

        public void setGradientRadius(float gradientRadius, int type) {
            this.mGradientRadius = gradientRadius;
            this.mGradientRadiusType = type;
        }
    }

    static boolean isOpaque(int color) {
        return ((color >> 24) & 255) == 255;
    }

    private GradientDrawable(GradientState state, Resources res) {
        this.mFillPaint = new Paint(1);
        this.mAlpha = 255;
        this.mPath = new Path();
        this.mRect = new RectF();
        this.mPathIsDirty = true;
        this.mGradientState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources res) {
        GradientState state = this.mGradientState;
        if (state.mSolidColors != null) {
            this.mFillPaint.setColor(state.mSolidColors.getColorForState(getState(), 0));
        } else if (state.mGradientColors == null) {
            this.mFillPaint.setColor(0);
        } else {
            this.mFillPaint.setColor(-16777216);
        }
        this.mPadding = state.mPadding;
        if (state.mStrokeWidth >= 0) {
            this.mStrokePaint = new Paint(1);
            this.mStrokePaint.setStyle(Paint.Style.STROKE);
            this.mStrokePaint.setStrokeWidth((float) state.mStrokeWidth);
            if (state.mStrokeColors != null) {
                this.mStrokePaint.setColor(state.mStrokeColors.getColorForState(getState(), 0));
            }
            if (state.mStrokeDashWidth != 0.0f) {
                this.mStrokePaint.setPathEffect(new DashPathEffect(new float[]{state.mStrokeDashWidth, state.mStrokeDashGap}, 0.0f));
            }
        }
        this.mBlendModeColorFilter = updateBlendModeFilter(this.mBlendModeColorFilter, state.mTint, state.mBlendMode);
        this.mGradientIsDirty = true;
        state.computeOpacity();
    }
}

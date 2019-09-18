package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Insets;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
    private ColorFilter mColorFilter;
    private final Paint mFillPaint;
    private boolean mGradientIsDirty;
    private float mGradientRadius;
    private GradientState mGradientState;
    private Paint mLayerPaint;
    private boolean mMutated;
    private Rect mPadding;
    private final Path mPath;
    private boolean mPathIsDirty;
    private final RectF mRect;
    private Path mRingPath;
    private Paint mStrokePaint;
    private PorterDuffColorFilter mTintFilter;

    static final class GradientState extends Drawable.ConstantState {
        public int mAngle = 0;
        int[] mAttrCorners;
        int[] mAttrGradient;
        int[] mAttrPadding;
        int[] mAttrSize;
        int[] mAttrSolid;
        int[] mAttrStroke;
        float mCenterX = 0.5f;
        float mCenterY = 0.5f;
        public int mChangingConfigurations;
        int mDensity = 160;
        public boolean mDither = false;
        public int mGradient = 0;
        public int[] mGradientColors;
        float mGradientRadius = 0.5f;
        int mGradientRadiusType = 0;
        public int mHeight = -1;
        public int mInnerRadius = -1;
        public float mInnerRadiusRatio = GradientDrawable.DEFAULT_INNER_RADIUS_RATIO;
        boolean mOpaqueOverBounds;
        boolean mOpaqueOverShape;
        public Insets mOpticalInsets = Insets.NONE;
        public Orientation mOrientation;
        public Rect mPadding = null;
        public float[] mPositions;
        public float mRadius = 0.0f;
        public float[] mRadiusArray = null;
        public int mShape = 0;
        public ColorStateList mSolidColors;
        public ColorStateList mStrokeColors;
        public float mStrokeDashGap = 0.0f;
        public float mStrokeDashWidth = 0.0f;
        public int mStrokeWidth = -1;
        public int[] mTempColors;
        public float[] mTempPositions;
        int[] mThemeAttrs;
        public int mThickness = -1;
        public float mThicknessRatio = GradientDrawable.DEFAULT_THICKNESS_RATIO;
        ColorStateList mTint = null;
        PorterDuff.Mode mTintMode = Drawable.DEFAULT_TINT_MODE;
        boolean mUseLevel = false;
        boolean mUseLevelForShape = true;
        public int mWidth = -1;

        public GradientState(Orientation orientation, int[] gradientColors) {
            this.mOrientation = orientation;
            setGradientColors(gradientColors);
        }

        public GradientState(GradientState orig, Resources res) {
            this.mChangingConfigurations = orig.mChangingConfigurations;
            this.mShape = orig.mShape;
            this.mGradient = orig.mGradient;
            this.mAngle = orig.mAngle;
            this.mOrientation = orig.mOrientation;
            this.mSolidColors = orig.mSolidColors;
            if (orig.mGradientColors != null) {
                this.mGradientColors = (int[]) orig.mGradientColors.clone();
            }
            if (orig.mPositions != null) {
                this.mPositions = (float[]) orig.mPositions.clone();
            }
            this.mStrokeColors = orig.mStrokeColors;
            this.mStrokeWidth = orig.mStrokeWidth;
            this.mStrokeDashWidth = orig.mStrokeDashWidth;
            this.mStrokeDashGap = orig.mStrokeDashGap;
            this.mRadius = orig.mRadius;
            if (orig.mRadiusArray != null) {
                this.mRadiusArray = (float[]) orig.mRadiusArray.clone();
            }
            if (orig.mPadding != null) {
                this.mPadding = new Rect(orig.mPadding);
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
            this.mTintMode = orig.mTintMode;
            this.mThemeAttrs = orig.mThemeAttrs;
            this.mAttrSize = orig.mAttrSize;
            this.mAttrGradient = orig.mAttrGradient;
            this.mAttrSolid = orig.mAttrSolid;
            this.mAttrStroke = orig.mAttrStroke;
            this.mAttrCorners = orig.mAttrCorners;
            this.mAttrPadding = orig.mAttrPadding;
            this.mDensity = Drawable.resolveDensity(res, orig.mDensity);
            if (orig.mDensity != this.mDensity) {
                applyDensityScaling(orig.mDensity, this.mDensity);
            }
        }

        public final void setDensity(int targetDensity) {
            if (this.mDensity != targetDensity) {
                int sourceDensity = this.mDensity;
                this.mDensity = targetDensity;
                applyDensityScaling(sourceDensity, targetDensity);
            }
        }

        private void applyDensityScaling(int sourceDensity, int targetDensity) {
            if (this.mInnerRadius > 0) {
                this.mInnerRadius = Drawable.scaleFromDensity(this.mInnerRadius, sourceDensity, targetDensity, true);
            }
            if (this.mThickness > 0) {
                this.mThickness = Drawable.scaleFromDensity(this.mThickness, sourceDensity, targetDensity, true);
            }
            if (this.mOpticalInsets != Insets.NONE) {
                this.mOpticalInsets = Insets.of(Drawable.scaleFromDensity(this.mOpticalInsets.left, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(this.mOpticalInsets.top, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(this.mOpticalInsets.right, sourceDensity, targetDensity, true), Drawable.scaleFromDensity(this.mOpticalInsets.bottom, sourceDensity, targetDensity, true));
            }
            if (this.mPadding != null) {
                this.mPadding.left = Drawable.scaleFromDensity(this.mPadding.left, sourceDensity, targetDensity, false);
                this.mPadding.top = Drawable.scaleFromDensity(this.mPadding.top, sourceDensity, targetDensity, false);
                this.mPadding.right = Drawable.scaleFromDensity(this.mPadding.right, sourceDensity, targetDensity, false);
                this.mPadding.bottom = Drawable.scaleFromDensity(this.mPadding.bottom, sourceDensity, targetDensity, false);
            }
            if (this.mRadius > 0.0f) {
                this.mRadius = Drawable.scaleFromDensity(this.mRadius, sourceDensity, targetDensity);
            }
            if (this.mRadiusArray != null) {
                this.mRadiusArray[0] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[0], sourceDensity, targetDensity, true);
                this.mRadiusArray[1] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[1], sourceDensity, targetDensity, true);
                this.mRadiusArray[2] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[2], sourceDensity, targetDensity, true);
                this.mRadiusArray[3] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[3], sourceDensity, targetDensity, true);
            }
            if (this.mStrokeWidth > 0) {
                this.mStrokeWidth = Drawable.scaleFromDensity(this.mStrokeWidth, sourceDensity, targetDensity, true);
            }
            if (this.mStrokeDashWidth > 0.0f) {
                this.mStrokeDashWidth = Drawable.scaleFromDensity(this.mStrokeDashGap, sourceDensity, targetDensity);
            }
            if (this.mStrokeDashGap > 0.0f) {
                this.mStrokeDashGap = Drawable.scaleFromDensity(this.mStrokeDashGap, sourceDensity, targetDensity);
            }
            if (this.mGradientRadiusType == 0) {
                this.mGradientRadius = Drawable.scaleFromDensity(this.mGradientRadius, sourceDensity, targetDensity);
            }
            if (this.mWidth > 0) {
                this.mWidth = Drawable.scaleFromDensity(this.mWidth, sourceDensity, targetDensity, true);
            }
            if (this.mHeight > 0) {
                this.mHeight = Drawable.scaleFromDensity(this.mHeight, sourceDensity, targetDensity, true);
            }
        }

        public boolean canApplyTheme() {
            return (this.mThemeAttrs == null && this.mAttrSize == null && this.mAttrGradient == null && this.mAttrSolid == null && this.mAttrStroke == null && this.mAttrCorners == null && this.mAttrPadding == null && (this.mTint == null || !this.mTint.canApplyTheme()) && ((this.mStrokeColors == null || !this.mStrokeColors.canApplyTheme()) && ((this.mSolidColors == null || !this.mSolidColors.canApplyTheme()) && !super.canApplyTheme()))) ? false : true;
        }

        public Drawable newDrawable() {
            return new GradientDrawable(this, null);
        }

        public Drawable newDrawable(Resources res) {
            GradientState state;
            if (Drawable.resolveDensity(res, this.mDensity) != this.mDensity) {
                state = new GradientState(this, res);
            } else {
                state = this;
            }
            return new GradientDrawable(state, res);
        }

        public int getChangingConfigurations() {
            int i = 0;
            int changingConfigurations = this.mChangingConfigurations | (this.mStrokeColors != null ? this.mStrokeColors.getChangingConfigurations() : 0) | (this.mSolidColors != null ? this.mSolidColors.getChangingConfigurations() : 0);
            if (this.mTint != null) {
                i = this.mTint.getChangingConfigurations();
            }
            return changingConfigurations | i;
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
        public void computeOpacity() {
            boolean z = false;
            this.mOpaqueOverBounds = false;
            this.mOpaqueOverShape = false;
            if (this.mGradientColors != null) {
                int i = 0;
                while (i < this.mGradientColors.length) {
                    if (GradientDrawable.isOpaque(this.mGradientColors[i])) {
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

    public boolean getPadding(Rect padding) {
        if (this.mPadding == null) {
            return super.getPadding(padding);
        }
        padding.set(this.mPadding);
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
        return (alpha * (this.mAlpha + (this.mAlpha >> 7))) >> 8;
    }

    public Orientation getOrientation() {
        return this.mGradientState.mOrientation;
    }

    public void setOrientation(Orientation orientation) {
        this.mGradientState.mOrientation = orientation;
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public void setColors(int[] colors) {
        this.mGradientState.setGradientColors(colors);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public int[] getColors() {
        if (this.mGradientState.mGradientColors == null) {
            return null;
        }
        return (int[]) this.mGradientState.mGradientColors.clone();
    }

    public void draw(Canvas canvas) {
        ColorFilter colorFilter;
        Canvas canvas2 = canvas;
        if (ensureValidRect()) {
            int prevFillAlpha = this.mFillPaint.getAlpha();
            int prevStrokeAlpha = this.mStrokePaint != null ? this.mStrokePaint.getAlpha() : 0;
            int currFillAlpha = modulateAlpha(prevFillAlpha);
            int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
            boolean z = true;
            boolean haveStroke = currStrokeAlpha > 0 && this.mStrokePaint != null && this.mStrokePaint.getStrokeWidth() > 0.0f;
            boolean haveFill = currFillAlpha > 0;
            GradientState st = this.mGradientState;
            ColorFilter colorFilter2 = this.mColorFilter != null ? this.mColorFilter : this.mTintFilter;
            if (!haveStroke || !haveFill || st.mShape == 2 || currStrokeAlpha >= 255 || (this.mAlpha >= 255 && colorFilter2 == null)) {
                z = false;
            }
            boolean useLayer = z;
            if (useLayer) {
                if (this.mLayerPaint == null) {
                    this.mLayerPaint = new Paint();
                }
                this.mLayerPaint.setDither(st.mDither);
                this.mLayerPaint.setAlpha(this.mAlpha);
                this.mLayerPaint.setColorFilter(colorFilter2);
                float rad = this.mStrokePaint.getStrokeWidth();
                colorFilter = colorFilter2;
                canvas2.saveLayer(this.mRect.left - rad, this.mRect.top - rad, this.mRect.right + rad, this.mRect.bottom + rad, this.mLayerPaint);
                this.mFillPaint.setColorFilter(null);
                this.mStrokePaint.setColorFilter(null);
            } else {
                colorFilter = colorFilter2;
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
            switch (st.mShape) {
                case 0:
                    if (st.mRadiusArray == null) {
                        if (st.mRadius <= 0.0f) {
                            if (!(this.mFillPaint.getColor() == 0 && colorFilter == null && this.mFillPaint.getShader() == null)) {
                                canvas2.drawRect(this.mRect, this.mFillPaint);
                            }
                            if (haveStroke) {
                                canvas2.drawRect(this.mRect, this.mStrokePaint);
                                break;
                            }
                        } else {
                            float rad2 = Math.min(st.mRadius, Math.min(this.mRect.width(), this.mRect.height()) * 0.5f);
                            canvas2.drawRoundRect(this.mRect, rad2, rad2, this.mFillPaint);
                            if (haveStroke) {
                                canvas2.drawRoundRect(this.mRect, rad2, rad2, this.mStrokePaint);
                                break;
                            }
                        }
                    } else {
                        buildPathIfDirty();
                        canvas2.drawPath(this.mPath, this.mFillPaint);
                        if (haveStroke) {
                            canvas2.drawPath(this.mPath, this.mStrokePaint);
                            break;
                        }
                    }
                    break;
                case 1:
                    canvas2.drawOval(this.mRect, this.mFillPaint);
                    if (haveStroke) {
                        canvas2.drawOval(this.mRect, this.mStrokePaint);
                        break;
                    }
                    break;
                case 2:
                    RectF r = this.mRect;
                    float y = r.centerY();
                    if (haveStroke) {
                        RectF rectF = r;
                        canvas2.drawLine(r.left, y, r.right, y, this.mStrokePaint);
                        break;
                    }
                    break;
                case 3:
                    Path path = buildRing(st);
                    canvas2.drawPath(path, this.mFillPaint);
                    if (haveStroke) {
                        canvas2.drawPath(path, this.mStrokePaint);
                        break;
                    }
                    break;
            }
            if (useLayer) {
                canvas.restore();
            } else {
                this.mFillPaint.setAlpha(prevFillAlpha);
                if (haveStroke) {
                    this.mStrokePaint.setAlpha(prevStrokeAlpha);
                }
            }
        }
    }

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
        if (this.mRingPath == null) {
            this.mRingPath = new Path();
        } else {
            this.mRingPath.reset();
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
        int color;
        this.mGradientState.setSolidColors(colorStateList);
        if (colorStateList == null) {
            color = 0;
        } else {
            color = colorStateList.getColorForState(getState(), 0);
        }
        this.mFillPaint.setColor(color);
        invalidateSelf();
    }

    public ColorStateList getColor() {
        return this.mGradientState.mSolidColors;
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] stateSet) {
        boolean invalidateSelf = false;
        GradientState s = this.mGradientState;
        ColorStateList solidColors = s.mSolidColors;
        if (solidColors != null) {
            int newColor = solidColors.getColorForState(stateSet, 0);
            if (this.mFillPaint.getColor() != newColor) {
                this.mFillPaint.setColor(newColor);
                invalidateSelf = true;
            }
        }
        Paint strokePaint = this.mStrokePaint;
        if (strokePaint != null) {
            ColorStateList strokeColors = s.mStrokeColors;
            if (strokeColors != null) {
                int newColor2 = strokeColors.getColorForState(stateSet, 0);
                if (strokePaint.getColor() != newColor2) {
                    strokePaint.setColor(newColor2);
                    invalidateSelf = true;
                }
            }
        }
        if (!(s.mTint == null || s.mTintMode == null)) {
            this.mTintFilter = updateTintFilter(this.mTintFilter, s.mTint, s.mTintMode);
            invalidateSelf = true;
        }
        if (!invalidateSelf) {
            return false;
        }
        invalidateSelf();
        return true;
    }

    public boolean isStateful() {
        GradientState s = this.mGradientState;
        return super.isStateful() || (s.mSolidColors != null && s.mSolidColors.isStateful()) || ((s.mStrokeColors != null && s.mStrokeColors.isStateful()) || (s.mTint != null && s.mTint.isStateful()));
    }

    public boolean hasFocusStateSpecified() {
        GradientState s = this.mGradientState;
        return (s.mSolidColors != null && s.mSolidColors.hasFocusStateSpecified()) || (s.mStrokeColors != null && s.mStrokeColors.hasFocusStateSpecified()) || (s.mTint != null && s.mTint.hasFocusStateSpecified());
    }

    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | this.mGradientState.getChangingConfigurations();
    }

    public void setAlpha(int alpha) {
        if (alpha != this.mAlpha) {
            this.mAlpha = alpha;
            invalidateSelf();
        }
    }

    public int getAlpha() {
        return this.mAlpha;
    }

    public void setDither(boolean dither) {
        if (dither != this.mGradientState.mDither) {
            this.mGradientState.mDither = dither;
            invalidateSelf();
        }
    }

    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        if (colorFilter != this.mColorFilter) {
            this.mColorFilter = colorFilter;
            invalidateSelf();
        }
    }

    public void setTintList(ColorStateList tint) {
        this.mGradientState.mTint = tint;
        this.mTintFilter = updateTintFilter(this.mTintFilter, tint, this.mGradientState.mTintMode);
        invalidateSelf();
    }

    public void setTintMode(PorterDuff.Mode tintMode) {
        this.mGradientState.mTintMode = tintMode;
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mGradientState.mTint, tintMode);
        invalidateSelf();
    }

    public int getOpacity() {
        return (this.mAlpha != 255 || !this.mGradientState.mOpaqueOverBounds || !isOpaqueForState()) ? -3 : -1;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        this.mRingPath = null;
        this.mPathIsDirty = true;
        this.mGradientIsDirty = true;
    }

    /* access modifiers changed from: protected */
    public boolean onLevelChange(int level) {
        super.onLevelChange(level);
        this.mGradientIsDirty = true;
        this.mPathIsDirty = true;
        invalidateSelf();
        return true;
    }

    private boolean ensureValidRect() {
        float radius;
        float x0;
        float y0;
        float x1;
        float f;
        if (this.mGradientIsDirty) {
            this.mGradientIsDirty = false;
            Rect bounds = getBounds();
            float inset = 0.0f;
            if (this.mStrokePaint != null) {
                inset = this.mStrokePaint.getStrokeWidth() * 0.5f;
            }
            GradientState st = this.mGradientState;
            this.mRect.set(((float) bounds.left) + inset, ((float) bounds.top) + inset, ((float) bounds.right) - inset, ((float) bounds.bottom) - inset);
            int[] gradientColors = st.mGradientColors;
            if (gradientColors != null) {
                RectF r = this.mRect;
                float f2 = 1.0f;
                if (st.mGradient == 0) {
                    if (st.mUseLevel) {
                        f2 = ((float) getLevel()) / 10000.0f;
                    }
                    float level = f2;
                    switch (st.mOrientation) {
                        case TOP_BOTTOM:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = x0;
                            f = r.bottom * level;
                            break;
                        case TR_BL:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = r.left * level;
                            f = r.bottom * level;
                            break;
                        case RIGHT_LEFT:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = r.left * level;
                            f = y0;
                            break;
                        case BR_TL:
                            x0 = r.right;
                            y0 = r.bottom;
                            x1 = r.left * level;
                            f = r.top * level;
                            break;
                        case BOTTOM_TOP:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = x0;
                            f = r.top * level;
                            break;
                        case BL_TR:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = r.right * level;
                            f = r.top * level;
                            break;
                        case LEFT_RIGHT:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = r.right * level;
                            f = y0;
                            break;
                        default:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = r.right * level;
                            f = r.bottom * level;
                            break;
                    }
                    float y1 = f;
                    Paint paint = this.mFillPaint;
                    LinearGradient linearGradient = r11;
                    float f3 = level;
                    LinearGradient linearGradient2 = new LinearGradient(x0, y0, x1, y1, gradientColors, st.mPositions, Shader.TileMode.CLAMP);
                    paint.setShader(linearGradient);
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
                    Paint paint2 = this.mFillPaint;
                    RadialGradient radialGradient = new RadialGradient(x02, y02, radius, gradientColors, (float[]) null, Shader.TileMode.CLAMP);
                    paint2.setShader(radialGradient);
                } else if (st.mGradient == 2) {
                    float x03 = r.left + ((r.right - r.left) * st.mCenterX);
                    float y03 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    int[] tempColors = gradientColors;
                    float[] tempPositions = null;
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
                            float[] tempPositions2 = new float[(length + 1)];
                            st.mTempPositions = tempPositions2;
                            tempPositions = tempPositions2;
                        }
                        float level2 = ((float) getLevel()) / 10000.0f;
                        int i = 0;
                        while (true) {
                            int i2 = i;
                            if (i2 >= length) {
                                break;
                            }
                            tempPositions[i2] = ((float) i2) * fraction * level2;
                            i = i2 + 1;
                        }
                        tempPositions[length] = 1.0f;
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

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        this.mGradientState.setDensity(Drawable.resolveDensity(r, 0));
        TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.GradientDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        updateLocalState(r);
    }

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
            state.mTintMode = Drawable.parseTintMode(tintMode, PorterDuff.Mode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(1);
        if (tint != null) {
            state.mTint = tint;
        }
        state.mOpticalInsets = Insets.of(a.getDimensionPixelSize(11, state.mOpticalInsets.left), a.getDimensionPixelSize(13, state.mOpticalInsets.top), a.getDimensionPixelSize(12, state.mOpticalInsets.right), a.getDimensionPixelSize(10, state.mOpticalInsets.bottom));
    }

    public boolean canApplyTheme() {
        return (this.mGradientState != null && this.mGradientState.canApplyTheme()) || super.canApplyTheme();
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
            } catch (XmlPullParserException e) {
                rethrowAsRuntimeException(e);
            } catch (Throwable th) {
                a2.recycle();
                throw th;
            }
            a2.recycle();
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
            int next = parser.next();
            int type = next;
            if (next != 1) {
                int depth = parser.getDepth();
                int depth2 = depth;
                if (depth < innerDepth && type == 3) {
                    return;
                }
                if (type == 2 && depth2 <= innerDepth) {
                    String name = parser.getName();
                    if (name.equals("size")) {
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

    private void updateGradientDrawableGradient(Resources r, TypedArray a) throws XmlPullParserException {
        float radius;
        TypedArray typedArray = a;
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrGradient = a.extractThemeAttrs();
        st.mCenterX = getFloatOrFraction(typedArray, 5, st.mCenterX);
        st.mCenterY = getFloatOrFraction(typedArray, 6, st.mCenterY);
        st.mUseLevel = typedArray.getBoolean(2, st.mUseLevel);
        st.mGradient = typedArray.getInt(4, st.mGradient);
        int unit = 0;
        int startColor = typedArray.getColor(0, 0);
        boolean hasCenterColor = typedArray.hasValue(8);
        int centerColor = typedArray.getColor(8, 0);
        int radiusType = 1;
        int endColor = typedArray.getColor(1, 0);
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
        if (st.mGradient == 0) {
            int angle = ((int) typedArray.getFloat(3, (float) st.mAngle)) % 360;
            if (angle % 45 == 0) {
                st.mAngle = angle;
                if (angle == 0) {
                    st.mOrientation = Orientation.LEFT_RIGHT;
                } else if (angle == 45) {
                    st.mOrientation = Orientation.BL_TR;
                } else if (angle == 90) {
                    st.mOrientation = Orientation.BOTTOM_TOP;
                } else if (angle == 135) {
                    st.mOrientation = Orientation.BR_TL;
                } else if (angle == 180) {
                    st.mOrientation = Orientation.RIGHT_LEFT;
                } else if (angle == 225) {
                    st.mOrientation = Orientation.TR_BL;
                } else if (angle == 270) {
                    st.mOrientation = Orientation.TOP_BOTTOM;
                } else if (angle == 315) {
                    st.mOrientation = Orientation.TL_BR;
                }
            } else {
                throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'angle' attribute to be a multiple of 45");
            }
        } else {
            TypedValue tv = typedArray.peekValue(7);
            if (tv != null) {
                if (tv.type == 6) {
                    radius = tv.getFraction(1.0f, 1.0f);
                    if (((tv.data >> 0) & 15) == 1) {
                        radiusType = 2;
                    }
                    unit = radiusType;
                } else if (tv.type == 5) {
                    radius = tv.getDimension(r.getDisplayMetrics());
                    unit = 0;
                } else {
                    radius = tv.getFloat();
                }
                st.mGradientRadius = radius;
                st.mGradientRadiusType = unit;
            } else if (st.mGradient == 1) {
                throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'gradientRadius' attribute with radial type");
            }
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
        float v = defaultValue;
        if (tv == null) {
            return v;
        }
        return tv.type == 6 ? tv.getFraction(1.0f, 1.0f) : tv.getFloat();
    }

    public int getIntrinsicWidth() {
        return this.mGradientState.mWidth;
    }

    public int getIntrinsicHeight() {
        return this.mGradientState.mHeight;
    }

    public Insets getOpticalInsets() {
        return this.mGradientState.mOpticalInsets;
    }

    public Drawable.ConstantState getConstantState() {
        this.mGradientState.mChangingConfigurations = getChangingConfigurations();
        return this.mGradientState;
    }

    private boolean isOpaqueForState() {
        if (this.mGradientState.mStrokeWidth >= 0 && this.mStrokePaint != null && !isOpaque(this.mStrokePaint.getColor())) {
            return false;
        }
        if (this.mGradientState.mGradientColors != null || isOpaque(this.mFillPaint.getColor())) {
            return true;
        }
        return false;
    }

    public void getOutline(Outline outline) {
        float f;
        GradientState st = this.mGradientState;
        Rect bounds = getBounds();
        if (st.mOpaqueOverShape && (this.mGradientState.mStrokeWidth <= 0 || this.mStrokePaint == null || this.mStrokePaint.getAlpha() == this.mFillPaint.getAlpha())) {
            f = ((float) modulateAlpha(this.mFillPaint.getAlpha())) / 255.0f;
        } else {
            f = 0.0f;
        }
        outline.setAlpha(f);
        switch (st.mShape) {
            case 0:
                if (st.mRadiusArray != null) {
                    buildPathIfDirty();
                    outline.setConvexPath(this.mPath);
                    return;
                }
                float rad = 0.0f;
                if (st.mRadius > 0.0f) {
                    rad = Math.min(st.mRadius, ((float) Math.min(bounds.width(), bounds.height())) * 0.5f);
                }
                outline.setRoundRect(bounds, rad);
                return;
            case 1:
                outline.setOval(bounds);
                return;
            case 2:
                float halfStrokeWidth = this.mStrokePaint == null ? 1.0E-4f : this.mStrokePaint.getStrokeWidth() * 0.5f;
                float centerY = (float) bounds.centerY();
                outline.setRect(bounds.left, (int) Math.floor((double) (centerY - halfStrokeWidth)), bounds.right, (int) Math.ceil((double) (centerY + halfStrokeWidth)));
                return;
            default:
                return;
        }
    }

    public Drawable mutate() {
        if (!this.mMutated && super.mutate() == this) {
            this.mGradientState = new GradientState(this.mGradientState, (Resources) null);
            updateLocalState(null);
            this.mMutated = true;
        }
        return this;
    }

    public void clearMutated() {
        super.clearMutated();
        this.mMutated = false;
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
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        this.mGradientIsDirty = true;
        state.computeOpacity();
    }
}

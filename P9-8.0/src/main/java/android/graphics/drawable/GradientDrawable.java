package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Insets;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.PathEffect;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable.ConstantState;
import android.hardware.camera2.params.TonemapCurve;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import com.android.internal.R;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class GradientDrawable extends Drawable {
    private static final /* synthetic */ int[] -android-graphics-drawable-GradientDrawable$OrientationSwitchesValues = null;
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

    static final class GradientState extends ConstantState {
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
        public float mRadius = TonemapCurve.LEVEL_BLACK;
        public float[] mRadiusArray = null;
        public int mShape = 0;
        public ColorStateList mSolidColors;
        public ColorStateList mStrokeColors;
        public float mStrokeDashGap = TonemapCurve.LEVEL_BLACK;
        public float mStrokeDashWidth = TonemapCurve.LEVEL_BLACK;
        public int mStrokeWidth = -1;
        public int[] mTempColors;
        public float[] mTempPositions;
        int[] mThemeAttrs;
        public int mThickness = -1;
        public float mThicknessRatio = GradientDrawable.DEFAULT_THICKNESS_RATIO;
        ColorStateList mTint = null;
        Mode mTintMode = GradientDrawable.DEFAULT_TINT_MODE;
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
            if (this.mRadius > TonemapCurve.LEVEL_BLACK) {
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
            if (this.mStrokeDashWidth > TonemapCurve.LEVEL_BLACK) {
                this.mStrokeDashWidth = Drawable.scaleFromDensity(this.mStrokeDashGap, sourceDensity, targetDensity);
            }
            if (this.mStrokeDashGap > TonemapCurve.LEVEL_BLACK) {
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
            if (this.mThemeAttrs == null && this.mAttrSize == null && this.mAttrGradient == null && this.mAttrSolid == null && this.mAttrStroke == null && this.mAttrCorners == null && this.mAttrPadding == null && ((this.mTint == null || !this.mTint.canApplyTheme()) && ((this.mStrokeColors == null || !this.mStrokeColors.canApplyTheme()) && (this.mSolidColors == null || !this.mSolidColors.canApplyTheme())))) {
                return super.canApplyTheme();
            }
            return true;
        }

        public Drawable newDrawable() {
            return new GradientDrawable(this, null, null);
        }

        public Drawable newDrawable(Resources res) {
            GradientState state;
            if (Drawable.resolveDensity(res, this.mDensity) != this.mDensity) {
                state = new GradientState(this, res);
            } else {
                state = this;
            }
            return new GradientDrawable(state, res, null);
        }

        public int getChangingConfigurations() {
            int changingConfigurations;
            int i = 0;
            int i2 = this.mChangingConfigurations;
            if (this.mStrokeColors != null) {
                changingConfigurations = this.mStrokeColors.getChangingConfigurations();
            } else {
                changingConfigurations = 0;
            }
            i2 |= changingConfigurations;
            if (this.mSolidColors != null) {
                changingConfigurations = this.mSolidColors.getChangingConfigurations();
            } else {
                changingConfigurations = 0;
            }
            changingConfigurations |= i2;
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

        private void computeOpacity() {
            boolean z = true;
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
                if (this.mShape != 0 || this.mRadius > TonemapCurve.LEVEL_BLACK) {
                    z = false;
                } else if (this.mRadiusArray != null) {
                    z = false;
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
            if (radius < TonemapCurve.LEVEL_BLACK) {
                radius = TonemapCurve.LEVEL_BLACK;
            }
            this.mRadius = radius;
            this.mRadiusArray = null;
        }

        public void setCornerRadii(float[] radii) {
            this.mRadiusArray = radii;
            if (radii == null) {
                this.mRadius = TonemapCurve.LEVEL_BLACK;
            }
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

    private static /* synthetic */ int[] -getandroid-graphics-drawable-GradientDrawable$OrientationSwitchesValues() {
        if (-android-graphics-drawable-GradientDrawable$OrientationSwitchesValues != null) {
            return -android-graphics-drawable-GradientDrawable$OrientationSwitchesValues;
        }
        int[] iArr = new int[Orientation.values().length];
        try {
            iArr[Orientation.BL_TR.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Orientation.BOTTOM_TOP.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Orientation.BR_TL.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Orientation.LEFT_RIGHT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Orientation.RIGHT_LEFT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Orientation.TL_BR.ordinal()] = 8;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Orientation.TOP_BOTTOM.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Orientation.TR_BL.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        -android-graphics-drawable-GradientDrawable$OrientationSwitchesValues = iArr;
        return iArr;
    }

    /* synthetic */ GradientDrawable(GradientState state, Resources res, GradientDrawable -this2) {
        this(state, res);
    }

    public GradientDrawable() {
        this(new GradientState(Orientation.TOP_BOTTOM, null), null);
    }

    public GradientDrawable(Orientation orientation, int[] colors) {
        this(new GradientState(orientation, colors), null);
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
        setStroke(width, color, (float) TonemapCurve.LEVEL_BLACK, (float) TonemapCurve.LEVEL_BLACK);
    }

    public void setStroke(int width, ColorStateList colorStateList) {
        setStroke(width, colorStateList, (float) TonemapCurve.LEVEL_BLACK, (float) TonemapCurve.LEVEL_BLACK);
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
            this.mStrokePaint.setStyle(Style.STROKE);
        }
        this.mStrokePaint.setStrokeWidth((float) width);
        this.mStrokePaint.setColor(color);
        PathEffect pathEffect = null;
        if (dashWidth > TonemapCurve.LEVEL_BLACK) {
            pathEffect = new DashPathEffect(new float[]{dashWidth, dashGap}, TonemapCurve.LEVEL_BLACK);
        }
        this.mStrokePaint.setPathEffect(pathEffect);
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
            return TonemapCurve.LEVEL_BLACK;
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
        if (ensureValidRect()) {
            float rad;
            int prevFillAlpha = this.mFillPaint.getAlpha();
            int prevStrokeAlpha = this.mStrokePaint != null ? this.mStrokePaint.getAlpha() : 0;
            int currFillAlpha = modulateAlpha(prevFillAlpha);
            int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
            boolean haveStroke = (currStrokeAlpha <= 0 || this.mStrokePaint == null) ? false : this.mStrokePaint.getStrokeWidth() > TonemapCurve.LEVEL_BLACK;
            boolean haveFill = currFillAlpha > 0;
            GradientState st = this.mGradientState;
            ColorFilter colorFilter = this.mColorFilter != null ? this.mColorFilter : this.mTintFilter;
            boolean useLayer = (!haveStroke || !haveFill || st.mShape == 2 || currStrokeAlpha >= 255) ? false : this.mAlpha < 255 || colorFilter != null;
            if (useLayer) {
                if (this.mLayerPaint == null) {
                    this.mLayerPaint = new Paint();
                }
                this.mLayerPaint.setDither(st.mDither);
                this.mLayerPaint.setAlpha(this.mAlpha);
                this.mLayerPaint.setColorFilter(colorFilter);
                rad = this.mStrokePaint.getStrokeWidth();
                canvas.saveLayer(this.mRect.left - rad, this.mRect.top - rad, this.mRect.right + rad, this.mRect.bottom + rad, this.mLayerPaint);
                this.mFillPaint.setColorFilter(null);
                this.mStrokePaint.setColorFilter(null);
            } else {
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
                        if (st.mRadius <= TonemapCurve.LEVEL_BLACK) {
                            if (!(this.mFillPaint.getColor() == 0 && colorFilter == null && this.mFillPaint.getShader() == null)) {
                                canvas.drawRect(this.mRect, this.mFillPaint);
                            }
                            if (haveStroke) {
                                canvas.drawRect(this.mRect, this.mStrokePaint);
                                break;
                            }
                        }
                        rad = Math.min(st.mRadius, Math.min(this.mRect.width(), this.mRect.height()) * 0.5f);
                        canvas.drawRoundRect(this.mRect, rad, rad, this.mFillPaint);
                        if (haveStroke) {
                            canvas.drawRoundRect(this.mRect, rad, rad, this.mStrokePaint);
                            break;
                        }
                    }
                    buildPathIfDirty();
                    canvas.drawPath(this.mPath, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawPath(this.mPath, this.mStrokePaint);
                        break;
                    }
                    break;
                case 1:
                    canvas.drawOval(this.mRect, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawOval(this.mRect, this.mStrokePaint);
                        break;
                    }
                    break;
                case 2:
                    RectF r = this.mRect;
                    float y = r.centerY();
                    if (haveStroke) {
                        canvas.drawLine(r.left, y, r.right, y, this.mStrokePaint);
                        break;
                    }
                    break;
                case 3:
                    Path path = buildRing(st);
                    canvas.drawPath(path, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawPath(path, this.mStrokePaint);
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

    private void buildPathIfDirty() {
        GradientState st = this.mGradientState;
        if (this.mPathIsDirty) {
            ensureValidRect();
            this.mPath.reset();
            this.mPath.addRoundRect(this.mRect, st.mRadiusArray, Direction.CW);
            this.mPathIsDirty = false;
        }
    }

    private Path buildRing(GradientState st) {
        if (this.mRingPath != null && (!st.mUseLevelForShape || (this.mPathIsDirty ^ 1) != 0)) {
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
        bounds = new RectF(innerBounds);
        bounds.inset(-thickness, -thickness);
        if (this.mRingPath == null) {
            this.mRingPath = new Path();
        } else {
            this.mRingPath.reset();
        }
        Path ringPath = this.mRingPath;
        if (sweep >= 360.0f || sweep <= -360.0f) {
            ringPath.addOval(bounds, Direction.CW);
            ringPath.addOval(innerBounds, Direction.CCW);
        } else {
            ringPath.setFillType(FillType.EVEN_ODD);
            ringPath.moveTo(x + radius, y);
            ringPath.lineTo((x + radius) + thickness, y);
            ringPath.arcTo(bounds, TonemapCurve.LEVEL_BLACK, sweep, false);
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

    protected boolean onStateChange(int[] stateSet) {
        int newColor;
        boolean invalidateSelf = false;
        GradientState s = this.mGradientState;
        ColorStateList solidColors = s.mSolidColors;
        if (solidColors != null) {
            newColor = solidColors.getColorForState(stateSet, 0);
            if (this.mFillPaint.getColor() != newColor) {
                this.mFillPaint.setColor(newColor);
                invalidateSelf = true;
            }
        }
        Paint strokePaint = this.mStrokePaint;
        if (strokePaint != null) {
            ColorStateList strokeColors = s.mStrokeColors;
            if (strokeColors != null) {
                newColor = strokeColors.getColorForState(stateSet, 0);
                if (strokePaint.getColor() != newColor) {
                    strokePaint.setColor(newColor);
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
        if (super.isStateful() || ((s.mSolidColors != null && s.mSolidColors.isStateful()) || (s.mStrokeColors != null && s.mStrokeColors.isStateful()))) {
            return true;
        }
        return s.mTint != null ? s.mTint.isStateful() : false;
    }

    public boolean hasFocusStateSpecified() {
        GradientState s = this.mGradientState;
        if ((s.mSolidColors == null || !s.mSolidColors.hasFocusStateSpecified()) && (s.mStrokeColors == null || !s.mStrokeColors.hasFocusStateSpecified())) {
            return s.mTint != null ? s.mTint.hasFocusStateSpecified() : false;
        } else {
            return true;
        }
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

    public void setTintMode(Mode tintMode) {
        this.mGradientState.mTintMode = tintMode;
        this.mTintFilter = updateTintFilter(this.mTintFilter, this.mGradientState.mTint, tintMode);
        invalidateSelf();
    }

    public int getOpacity() {
        return (this.mAlpha == 255 && this.mGradientState.mOpaqueOverBounds && isOpaqueForState()) ? -1 : -3;
    }

    protected void onBoundsChange(Rect r) {
        super.onBoundsChange(r);
        this.mRingPath = null;
        this.mPathIsDirty = true;
        this.mGradientIsDirty = true;
    }

    protected boolean onLevelChange(int level) {
        super.onLevelChange(level);
        this.mGradientIsDirty = true;
        this.mPathIsDirty = true;
        invalidateSelf();
        return true;
    }

    private boolean ensureValidRect() {
        if (this.mGradientIsDirty) {
            this.mGradientIsDirty = false;
            Rect bounds = getBounds();
            float inset = TonemapCurve.LEVEL_BLACK;
            if (this.mStrokePaint != null) {
                inset = this.mStrokePaint.getStrokeWidth() * 0.5f;
            }
            GradientState st = this.mGradientState;
            this.mRect.set(((float) bounds.left) + inset, ((float) bounds.top) + inset, ((float) bounds.right) - inset, ((float) bounds.bottom) - inset);
            int[] gradientColors = st.mGradientColors;
            if (gradientColors != null) {
                RectF r = this.mRect;
                float level;
                float x0;
                float y0;
                if (st.mGradient == 0) {
                    float x1;
                    float y1;
                    level = st.mUseLevel ? ((float) getLevel()) / 10000.0f : 1.0f;
                    switch (-getandroid-graphics-drawable-GradientDrawable$OrientationSwitchesValues()[st.mOrientation.ordinal()]) {
                        case 1:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = level * r.right;
                            y1 = level * r.top;
                            break;
                        case 2:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = x0;
                            y1 = level * r.top;
                            break;
                        case 3:
                            x0 = r.right;
                            y0 = r.bottom;
                            x1 = level * r.left;
                            y1 = level * r.top;
                            break;
                        case 4:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = y0;
                            break;
                        case 5:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = y0;
                            break;
                        case 6:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = x0;
                            y1 = level * r.bottom;
                            break;
                        case 7:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = level * r.bottom;
                            break;
                        default:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = level * r.bottom;
                            break;
                    }
                    this.mFillPaint.setShader(new LinearGradient(x0, y0, x1, y1, gradientColors, st.mPositions, TileMode.CLAMP));
                } else if (st.mGradient == 1) {
                    x0 = r.left + ((r.right - r.left) * st.mCenterX);
                    y0 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    float radius = st.mGradientRadius;
                    if (st.mGradientRadiusType == 1) {
                        radius *= Math.min(st.mWidth >= 0 ? (float) st.mWidth : r.width(), st.mHeight >= 0 ? (float) st.mHeight : r.height());
                    } else if (st.mGradientRadiusType == 2) {
                        radius *= Math.min(r.width(), r.height());
                    }
                    if (st.mUseLevel) {
                        radius *= ((float) getLevel()) / 10000.0f;
                    }
                    this.mGradientRadius = radius;
                    if (radius <= TonemapCurve.LEVEL_BLACK) {
                        radius = 0.001f;
                    }
                    this.mFillPaint.setShader(new RadialGradient(x0, y0, radius, gradientColors, null, TileMode.CLAMP));
                } else if (st.mGradient == 2) {
                    x0 = r.left + ((r.right - r.left) * st.mCenterX);
                    y0 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    int[] tempColors = gradientColors;
                    float[] tempPositions = null;
                    if (st.mUseLevel) {
                        tempColors = st.mTempColors;
                        int length = gradientColors.length;
                        if (tempColors == null || tempColors.length != length + 1) {
                            tempColors = new int[(length + 1)];
                            st.mTempColors = tempColors;
                        }
                        System.arraycopy(gradientColors, 0, tempColors, 0, length);
                        tempColors[length] = gradientColors[length - 1];
                        tempPositions = st.mTempPositions;
                        float fraction = 1.0f / ((float) (length - 1));
                        if (tempPositions == null || tempPositions.length != length + 1) {
                            tempPositions = new float[(length + 1)];
                            st.mTempPositions = tempPositions;
                        }
                        level = ((float) getLevel()) / 10000.0f;
                        for (int i = 0; i < length; i++) {
                            tempPositions[i] = (((float) i) * fraction) * level;
                        }
                        tempPositions[length] = 1.0f;
                    }
                    this.mFillPaint.setShader(new SweepGradient(x0, y0, tempColors, tempPositions));
                }
                if (st.mSolidColors == null) {
                    this.mFillPaint.setColor(-16777216);
                }
            }
        }
        return this.mRect.isEmpty() ^ 1;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        this.mGradientState.setDensity(Drawable.resolveDensity(r, 0));
        TypedArray a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.GradientDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
        inflateChildElements(r, parser, attrs, theme);
        updateLocalState(r);
    }

    public void applyTheme(Theme t) {
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
            state.mTintMode = Drawable.parseTintMode(tintMode, Mode.SRC_IN);
        }
        ColorStateList tint = a.getColorStateList(1);
        if (tint != null) {
            state.mTint = tint;
        }
        state.mOpticalInsets = Insets.of(a.getDimensionPixelSize(11, state.mOpticalInsets.left), a.getDimensionPixelSize(13, state.mOpticalInsets.top), a.getDimensionPixelSize(12, state.mOpticalInsets.right), a.getDimensionPixelSize(10, state.mOpticalInsets.bottom));
    }

    public boolean canApplyTheme() {
        return (this.mGradientState == null || !this.mGradientState.canApplyTheme()) ? super.canApplyTheme() : true;
    }

    private void applyThemeChildElements(Theme t) {
        TypedArray a;
        GradientState st = this.mGradientState;
        if (st.mAttrSize != null) {
            a = t.resolveAttributes(st.mAttrSize, R.styleable.GradientDrawableSize);
            updateGradientDrawableSize(a);
            a.recycle();
        }
        if (st.mAttrGradient != null) {
            a = t.resolveAttributes(st.mAttrGradient, R.styleable.GradientDrawableGradient);
            try {
                updateGradientDrawableGradient(t.getResources(), a);
            } catch (XmlPullParserException e) {
                Drawable.rethrowAsRuntimeException(e);
            } finally {
                a.recycle();
            }
        }
        if (st.mAttrSolid != null) {
            a = t.resolveAttributes(st.mAttrSolid, R.styleable.GradientDrawableSolid);
            updateGradientDrawableSolid(a);
            a.recycle();
        }
        if (st.mAttrStroke != null) {
            a = t.resolveAttributes(st.mAttrStroke, R.styleable.GradientDrawableStroke);
            updateGradientDrawableStroke(a);
            a.recycle();
        }
        if (st.mAttrCorners != null) {
            a = t.resolveAttributes(st.mAttrCorners, R.styleable.DrawableCorners);
            updateDrawableCorners(a);
            a.recycle();
        }
        if (st.mAttrPadding != null) {
            a = t.resolveAttributes(st.mAttrPadding, R.styleable.GradientDrawablePadding);
            updateGradientDrawablePadding(a);
            a.recycle();
        }
    }

    private void inflateChildElements(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
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
                    TypedArray a;
                    if (name.equals("size")) {
                        a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSize);
                        updateGradientDrawableSize(a);
                        a.recycle();
                    } else if (name.equals("gradient")) {
                        a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableGradient);
                        updateGradientDrawableGradient(r, a);
                        a.recycle();
                    } else if (name.equals("solid")) {
                        a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableSolid);
                        updateGradientDrawableSolid(a);
                        a.recycle();
                    } else if (name.equals("stroke")) {
                        a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.GradientDrawableStroke);
                        updateGradientDrawableStroke(a);
                        a.recycle();
                    } else if (name.equals("corners")) {
                        a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.DrawableCorners);
                        updateDrawableCorners(a);
                        a.recycle();
                    } else if (name.equals("padding")) {
                        a = Drawable.obtainAttributes(r, theme, attrs, R.styleable.GradientDrawablePadding);
                        updateGradientDrawablePadding(a);
                        a.recycle();
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
        if (dashWidth != TonemapCurve.LEVEL_BLACK) {
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
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrGradient = a.extractThemeAttrs();
        st.mCenterX = getFloatOrFraction(a, 5, st.mCenterX);
        st.mCenterY = getFloatOrFraction(a, 6, st.mCenterY);
        st.mUseLevel = a.getBoolean(2, st.mUseLevel);
        st.mGradient = a.getInt(4, st.mGradient);
        int startColor = a.getColor(0, 0);
        boolean hasCenterColor = a.hasValue(8);
        int centerColor = a.getColor(8, 0);
        int endColor = a.getColor(1, 0);
        if (hasCenterColor) {
            st.mGradientColors = new int[3];
            st.mGradientColors[0] = startColor;
            st.mGradientColors[1] = centerColor;
            st.mGradientColors[2] = endColor;
            st.mPositions = new float[3];
            st.mPositions[0] = TonemapCurve.LEVEL_BLACK;
            st.mPositions[1] = st.mCenterX != 0.5f ? st.mCenterX : st.mCenterY;
            st.mPositions[2] = 1.0f;
        } else {
            st.mGradientColors = new int[2];
            st.mGradientColors[0] = startColor;
            st.mGradientColors[1] = endColor;
        }
        if (st.mGradient == 0) {
            int angle = ((int) a.getFloat(3, (float) st.mAngle)) % 360;
            if (angle % 45 != 0) {
                throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'angle' attribute to " + "be a multiple of 45");
            }
            st.mAngle = angle;
            switch (angle) {
                case 0:
                    st.mOrientation = Orientation.LEFT_RIGHT;
                    return;
                case 45:
                    st.mOrientation = Orientation.BL_TR;
                    return;
                case 90:
                    st.mOrientation = Orientation.BOTTOM_TOP;
                    return;
                case 135:
                    st.mOrientation = Orientation.BR_TL;
                    return;
                case 180:
                    st.mOrientation = Orientation.RIGHT_LEFT;
                    return;
                case 225:
                    st.mOrientation = Orientation.TR_BL;
                    return;
                case 270:
                    st.mOrientation = Orientation.TOP_BOTTOM;
                    return;
                case 315:
                    st.mOrientation = Orientation.TL_BR;
                    return;
                default:
                    return;
            }
        }
        TypedValue tv = a.peekValue(7);
        if (tv != null) {
            float radius;
            int radiusType;
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
        } else if (st.mGradient == 1) {
            throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'gradientRadius' " + "attribute with radial type");
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

    public ConstantState getConstantState() {
        this.mGradientState.mChangingConfigurations = getChangingConfigurations();
        return this.mGradientState;
    }

    private boolean isOpaqueForState() {
        if (this.mGradientState.mStrokeWidth >= 0 && this.mStrokePaint != null && (isOpaque(this.mStrokePaint.getColor()) ^ 1) != 0) {
            return false;
        }
        if (this.mGradientState.mGradientColors != null || (isOpaque(this.mFillPaint.getColor()) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    public void getOutline(Outline outline) {
        GradientState st = this.mGradientState;
        Rect bounds = getBounds();
        boolean useFillOpacity = st.mOpaqueOverShape ? (this.mGradientState.mStrokeWidth <= 0 || this.mStrokePaint == null) ? true : this.mStrokePaint.getAlpha() == this.mFillPaint.getAlpha() : false;
        outline.setAlpha(useFillOpacity ? ((float) modulateAlpha(this.mFillPaint.getAlpha())) / 255.0f : TonemapCurve.LEVEL_BLACK);
        switch (st.mShape) {
            case 0:
                if (st.mRadiusArray != null) {
                    buildPathIfDirty();
                    outline.setConvexPath(this.mPath);
                    return;
                }
                float rad = TonemapCurve.LEVEL_BLACK;
                if (st.mRadius > TonemapCurve.LEVEL_BLACK) {
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
            this.mGradientState = new GradientState(this.mGradientState, null);
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
            this.mStrokePaint.setStyle(Style.STROKE);
            this.mStrokePaint.setStrokeWidth((float) state.mStrokeWidth);
            if (state.mStrokeColors != null) {
                this.mStrokePaint.setColor(state.mStrokeColors.getColorForState(getState(), 0));
            }
            if (state.mStrokeDashWidth != TonemapCurve.LEVEL_BLACK) {
                this.mStrokePaint.setPathEffect(new DashPathEffect(new float[]{state.mStrokeDashWidth, state.mStrokeDashGap}, TonemapCurve.LEVEL_BLACK));
            }
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        this.mGradientIsDirty = true;
        state.computeOpacity();
    }
}

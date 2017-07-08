package android.graphics.drawable;

import android.app.IActivityManager;
import android.bluetooth.BluetoothAssignedNumbers;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.hardware.SensorManager;
import android.media.ToneGenerator;
import android.net.wifi.AnqpInformationElement;
import android.net.wifi.ScanResult.InformationElement;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.Process;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech.Engine;
import android.telecom.AudioState;
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
        public int mAngle;
        int[] mAttrCorners;
        int[] mAttrGradient;
        int[] mAttrPadding;
        int[] mAttrSize;
        int[] mAttrSolid;
        int[] mAttrStroke;
        float mCenterX;
        float mCenterY;
        public int mChangingConfigurations;
        int mDensity;
        public boolean mDither;
        public int mGradient;
        public int[] mGradientColors;
        float mGradientRadius;
        int mGradientRadiusType;
        public int mHeight;
        public int mInnerRadius;
        public float mInnerRadiusRatio;
        boolean mOpaqueOverBounds;
        boolean mOpaqueOverShape;
        public Insets mOpticalInsets;
        public Orientation mOrientation;
        public Rect mPadding;
        public float[] mPositions;
        public float mRadius;
        public float[] mRadiusArray;
        public int mShape;
        public ColorStateList mSolidColors;
        public ColorStateList mStrokeColors;
        public float mStrokeDashGap;
        public float mStrokeDashWidth;
        public int mStrokeWidth;
        public int[] mTempColors;
        public float[] mTempPositions;
        int[] mThemeAttrs;
        public int mThickness;
        public float mThicknessRatio;
        ColorStateList mTint;
        Mode mTintMode;
        boolean mUseLevel;
        boolean mUseLevelForShape;
        public int mWidth;

        public GradientState(Orientation orientation, int[] gradientColors) {
            this.mShape = GradientDrawable.RECTANGLE;
            this.mGradient = GradientDrawable.RECTANGLE;
            this.mAngle = GradientDrawable.RECTANGLE;
            this.mStrokeWidth = -1;
            this.mStrokeDashWidth = 0.0f;
            this.mStrokeDashGap = 0.0f;
            this.mRadius = 0.0f;
            this.mRadiusArray = null;
            this.mPadding = null;
            this.mWidth = -1;
            this.mHeight = -1;
            this.mInnerRadiusRatio = GradientDrawable.DEFAULT_INNER_RADIUS_RATIO;
            this.mThicknessRatio = GradientDrawable.DEFAULT_THICKNESS_RATIO;
            this.mInnerRadius = -1;
            this.mThickness = -1;
            this.mDither = false;
            this.mOpticalInsets = Insets.NONE;
            this.mCenterX = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            this.mCenterY = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            this.mGradientRadius = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            this.mGradientRadiusType = GradientDrawable.RECTANGLE;
            this.mUseLevel = false;
            this.mUseLevelForShape = true;
            this.mTint = null;
            this.mTintMode = GradientDrawable.DEFAULT_TINT_MODE;
            this.mDensity = Const.CODE_G3_RANGE_START;
            this.mOrientation = orientation;
            setGradientColors(gradientColors);
        }

        public GradientState(GradientState orig, Resources res) {
            this.mShape = GradientDrawable.RECTANGLE;
            this.mGradient = GradientDrawable.RECTANGLE;
            this.mAngle = GradientDrawable.RECTANGLE;
            this.mStrokeWidth = -1;
            this.mStrokeDashWidth = 0.0f;
            this.mStrokeDashGap = 0.0f;
            this.mRadius = 0.0f;
            this.mRadiusArray = null;
            this.mPadding = null;
            this.mWidth = -1;
            this.mHeight = -1;
            this.mInnerRadiusRatio = GradientDrawable.DEFAULT_INNER_RADIUS_RATIO;
            this.mThicknessRatio = GradientDrawable.DEFAULT_THICKNESS_RATIO;
            this.mInnerRadius = -1;
            this.mThickness = -1;
            this.mDither = false;
            this.mOpticalInsets = Insets.NONE;
            this.mCenterX = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            this.mCenterY = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            this.mGradientRadius = NetworkHistoryUtils.RECOVERY_PERCENTAGE;
            this.mGradientRadiusType = GradientDrawable.RECTANGLE;
            this.mUseLevel = false;
            this.mUseLevelForShape = true;
            this.mTint = null;
            this.mTintMode = GradientDrawable.DEFAULT_TINT_MODE;
            this.mDensity = Const.CODE_G3_RANGE_START;
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
                this.mRadiusArray[GradientDrawable.RECTANGLE] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[GradientDrawable.RECTANGLE], sourceDensity, targetDensity, true);
                this.mRadiusArray[GradientDrawable.RADIUS_TYPE_FRACTION] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[GradientDrawable.RADIUS_TYPE_FRACTION], sourceDensity, targetDensity, true);
                this.mRadiusArray[GradientDrawable.SWEEP_GRADIENT] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[GradientDrawable.SWEEP_GRADIENT], sourceDensity, targetDensity, true);
                this.mRadiusArray[GradientDrawable.RING] = (float) Drawable.scaleFromDensity((int) this.mRadiusArray[GradientDrawable.RING], sourceDensity, targetDensity, true);
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
            int i = GradientDrawable.RECTANGLE;
            int i2 = this.mChangingConfigurations;
            if (this.mStrokeColors != null) {
                changingConfigurations = this.mStrokeColors.getChangingConfigurations();
            } else {
                changingConfigurations = GradientDrawable.RECTANGLE;
            }
            i2 |= changingConfigurations;
            if (this.mSolidColors != null) {
                changingConfigurations = this.mSolidColors.getChangingConfigurations();
            } else {
                changingConfigurations = GradientDrawable.RECTANGLE;
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
                int i = GradientDrawable.RECTANGLE;
                while (i < this.mGradientColors.length) {
                    if (GradientDrawable.isOpaque(this.mGradientColors[i])) {
                        i += GradientDrawable.RADIUS_TYPE_FRACTION;
                    } else {
                        return;
                    }
                }
            }
            if (this.mGradientColors != null || this.mSolidColors != null) {
                this.mOpaqueOverShape = true;
                if (this.mShape != 0 || this.mRadius > 0.0f) {
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
            if (radius < 0.0f) {
                radius = 0.0f;
            }
            this.mRadius = radius;
            this.mRadiusArray = null;
        }

        public void setCornerRadii(float[] radii) {
            this.mRadiusArray = radii;
            if (radii == null) {
                this.mRadius = 0.0f;
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
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.drawable.GradientDrawable.Orientation.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.drawable.GradientDrawable.Orientation.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.drawable.GradientDrawable.Orientation.<clinit>():void");
        }
    }

    private static /* synthetic */ int[] -getandroid-graphics-drawable-GradientDrawable$OrientationSwitchesValues() {
        if (-android-graphics-drawable-GradientDrawable$OrientationSwitchesValues != null) {
            return -android-graphics-drawable-GradientDrawable$OrientationSwitchesValues;
        }
        int[] iArr = new int[Orientation.values().length];
        try {
            iArr[Orientation.BL_TR.ordinal()] = RADIUS_TYPE_FRACTION;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Orientation.BOTTOM_TOP.ordinal()] = SWEEP_GRADIENT;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Orientation.BR_TL.ordinal()] = RING;
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

    /* synthetic */ GradientDrawable(GradientState state, Resources res, GradientDrawable gradientDrawable) {
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
            color = RECTANGLE;
        } else {
            color = colorStateList.getColorForState(getState(), RECTANGLE);
        }
        setStrokeInternal(width, color, dashWidth, dashGap);
    }

    private void setStrokeInternal(int width, int color, float dashWidth, float dashGap) {
        if (this.mStrokePaint == null) {
            this.mStrokePaint = new Paint((int) RADIUS_TYPE_FRACTION);
            this.mStrokePaint.setStyle(Style.STROKE);
        }
        this.mStrokePaint.setStrokeWidth((float) width);
        this.mStrokePaint.setColor(color);
        PathEffect pathEffect = null;
        if (dashWidth > 0.0f) {
            float[] fArr = new float[SWEEP_GRADIENT];
            fArr[RECTANGLE] = dashWidth;
            fArr[RADIUS_TYPE_FRACTION] = dashGap;
            pathEffect = new DashPathEffect(fArr, 0.0f);
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
        this.mGradientState.setGradientRadius(gradientRadius, RECTANGLE);
        this.mGradientIsDirty = true;
        invalidateSelf();
    }

    public float getGradientRadius() {
        if (this.mGradientState.mGradient != RADIUS_TYPE_FRACTION) {
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
        if (ensureValidRect()) {
            boolean useLayer;
            float rad;
            int prevFillAlpha = this.mFillPaint.getAlpha();
            int prevStrokeAlpha = this.mStrokePaint != null ? this.mStrokePaint.getAlpha() : RECTANGLE;
            int currFillAlpha = modulateAlpha(prevFillAlpha);
            int currStrokeAlpha = modulateAlpha(prevStrokeAlpha);
            boolean haveStroke = (currStrokeAlpha <= 0 || this.mStrokePaint == null) ? false : this.mStrokePaint.getStrokeWidth() > 0.0f;
            boolean haveFill = currFillAlpha > 0;
            GradientState st = this.mGradientState;
            ColorFilter colorFilter = this.mColorFilter != null ? this.mColorFilter : this.mTintFilter;
            if (!haveStroke || !haveFill || st.mShape == SWEEP_GRADIENT || currStrokeAlpha >= Process.PROC_TERM_MASK) {
                useLayer = false;
            } else {
                boolean z = this.mAlpha < Process.PROC_TERM_MASK || colorFilter != null;
                useLayer = z;
            }
            if (useLayer) {
                if (this.mLayerPaint == null) {
                    this.mLayerPaint = new Paint();
                }
                this.mLayerPaint.setDither(st.mDither);
                this.mLayerPaint.setAlpha(this.mAlpha);
                this.mLayerPaint.setColorFilter(colorFilter);
                rad = this.mStrokePaint.getStrokeWidth();
                canvas.saveLayer(this.mRect.left - rad, this.mRect.top - rad, this.mRect.right + rad, this.mRect.bottom + rad, this.mLayerPaint, 4);
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
                case RECTANGLE /*0*/:
                    if (st.mRadiusArray != null) {
                        buildPathIfDirty();
                        canvas.drawPath(this.mPath, this.mFillPaint);
                        if (haveStroke) {
                            canvas.drawPath(this.mPath, this.mStrokePaint);
                            break;
                        }
                    } else if (st.mRadius > 0.0f) {
                        rad = Math.min(st.mRadius, Math.min(this.mRect.width(), this.mRect.height()) * NetworkHistoryUtils.RECOVERY_PERCENTAGE);
                        canvas.drawRoundRect(this.mRect, rad, rad, this.mFillPaint);
                        if (haveStroke) {
                            canvas.drawRoundRect(this.mRect, rad, rad, this.mStrokePaint);
                            break;
                        }
                    } else {
                        if (this.mFillPaint.getColor() == 0 && colorFilter == null) {
                            if (this.mFillPaint.getShader() != null) {
                            }
                            if (haveStroke) {
                                canvas.drawRect(this.mRect, this.mStrokePaint);
                                break;
                            }
                        }
                        canvas.drawRect(this.mRect, this.mFillPaint);
                        if (haveStroke) {
                            canvas.drawRect(this.mRect, this.mStrokePaint);
                        }
                    }
                    break;
                case RADIUS_TYPE_FRACTION /*1*/:
                    canvas.drawOval(this.mRect, this.mFillPaint);
                    if (haveStroke) {
                        canvas.drawOval(this.mRect, this.mStrokePaint);
                        break;
                    }
                    break;
                case SWEEP_GRADIENT /*2*/:
                    RectF r = this.mRect;
                    float y = r.centerY();
                    if (haveStroke) {
                        canvas.drawLine(r.left, y, r.right, y, this.mStrokePaint);
                        break;
                    }
                    break;
                case RING /*3*/:
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
        if (this.mRingPath != null && (!st.mUseLevelForShape || !this.mPathIsDirty)) {
            return this.mRingPath;
        }
        this.mPathIsDirty = false;
        float sweep = st.mUseLevelForShape ? (((float) getLevel()) * 360.0f) / SensorManager.LIGHT_OVERCAST : 360.0f;
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
            ringPath.arcTo(bounds, 0.0f, sweep, false);
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
            color = RECTANGLE;
        } else {
            color = colorStateList.getColorForState(getState(), RECTANGLE);
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
            newColor = solidColors.getColorForState(stateSet, RECTANGLE);
            if (this.mFillPaint.getColor() != newColor) {
                this.mFillPaint.setColor(newColor);
                invalidateSelf = true;
            }
        }
        Paint strokePaint = this.mStrokePaint;
        if (strokePaint != null) {
            ColorStateList strokeColors = s.mStrokeColors;
            if (strokeColors != null) {
                newColor = strokeColors.getColorForState(stateSet, RECTANGLE);
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
        return (this.mAlpha == Process.PROC_TERM_MASK && this.mGradientState.mOpaqueOverBounds && isOpaqueForState()) ? -1 : -3;
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
            float inset = 0.0f;
            if (this.mStrokePaint != null) {
                inset = this.mStrokePaint.getStrokeWidth() * NetworkHistoryUtils.RECOVERY_PERCENTAGE;
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
                    level = st.mUseLevel ? ((float) getLevel()) / SensorManager.LIGHT_OVERCAST : Engine.DEFAULT_VOLUME;
                    switch (-getandroid-graphics-drawable-GradientDrawable$OrientationSwitchesValues()[st.mOrientation.ordinal()]) {
                        case RADIUS_TYPE_FRACTION /*1*/:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = level * r.right;
                            y1 = level * r.top;
                            break;
                        case SWEEP_GRADIENT /*2*/:
                            x0 = r.left;
                            y0 = r.bottom;
                            x1 = x0;
                            y1 = level * r.top;
                            break;
                        case RING /*3*/:
                            x0 = r.right;
                            y0 = r.bottom;
                            x1 = level * r.left;
                            y1 = level * r.top;
                            break;
                        case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = level * r.right;
                            y1 = y0;
                            break;
                        case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
                            x0 = r.right;
                            y0 = r.top;
                            x1 = level * r.left;
                            y1 = y0;
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                            x0 = r.left;
                            y0 = r.top;
                            x1 = x0;
                            y1 = level * r.bottom;
                            break;
                        case SpeechRecognizer.ERROR_NO_MATCH /*7*/:
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
                } else if (st.mGradient == RADIUS_TYPE_FRACTION) {
                    x0 = r.left + ((r.right - r.left) * st.mCenterX);
                    y0 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    float radius = st.mGradientRadius;
                    if (st.mGradientRadiusType == RADIUS_TYPE_FRACTION) {
                        radius *= Math.min(st.mWidth >= 0 ? (float) st.mWidth : r.width(), st.mHeight >= 0 ? (float) st.mHeight : r.height());
                    } else if (st.mGradientRadiusType == SWEEP_GRADIENT) {
                        radius *= Math.min(r.width(), r.height());
                    }
                    if (st.mUseLevel) {
                        radius *= ((float) getLevel()) / SensorManager.LIGHT_OVERCAST;
                    }
                    this.mGradientRadius = radius;
                    if (radius <= 0.0f) {
                        radius = SensorManager.LIGHT_NO_MOON;
                    }
                    this.mFillPaint.setShader(new RadialGradient(x0, y0, radius, gradientColors, null, TileMode.CLAMP));
                } else if (st.mGradient == SWEEP_GRADIENT) {
                    x0 = r.left + ((r.right - r.left) * st.mCenterX);
                    y0 = r.top + ((r.bottom - r.top) * st.mCenterY);
                    int[] tempColors = gradientColors;
                    float[] fArr = null;
                    if (st.mUseLevel) {
                        tempColors = st.mTempColors;
                        int length = gradientColors.length;
                        if (tempColors == null || tempColors.length != length + RADIUS_TYPE_FRACTION) {
                            tempColors = new int[(length + RADIUS_TYPE_FRACTION)];
                            st.mTempColors = tempColors;
                        }
                        System.arraycopy(gradientColors, RECTANGLE, tempColors, RECTANGLE, length);
                        tempColors[length] = gradientColors[length - 1];
                        fArr = st.mTempPositions;
                        float fraction = Engine.DEFAULT_VOLUME / ((float) (length - 1));
                        if (fArr == null || fArr.length != length + RADIUS_TYPE_FRACTION) {
                            fArr = new float[(length + RADIUS_TYPE_FRACTION)];
                            st.mTempPositions = fArr;
                        }
                        level = ((float) getLevel()) / SensorManager.LIGHT_OVERCAST;
                        for (int i = RECTANGLE; i < length; i += RADIUS_TYPE_FRACTION) {
                            fArr[i] = (((float) i) * fraction) * level;
                        }
                        fArr[length] = Engine.DEFAULT_VOLUME;
                    }
                    this.mFillPaint.setShader(new SweepGradient(x0, y0, tempColors, fArr));
                }
                if (st.mSolidColors == null) {
                    this.mFillPaint.setColor(Color.BLACK);
                }
            }
        }
        if (this.mRect.isEmpty()) {
            return false;
        }
        return true;
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        this.mGradientState.setDensity(Drawable.resolveDensity(r, RECTANGLE));
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
            state.setDensity(Drawable.resolveDensity(t.getResources(), RECTANGLE));
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
        state.mShape = a.getInt(RING, state.mShape);
        state.mDither = a.getBoolean(RECTANGLE, state.mDither);
        if (state.mShape == RING) {
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
        ColorStateList tint = a.getColorStateList(RADIUS_TYPE_FRACTION);
        if (tint != null) {
            state.mTint = tint;
        }
        state.mOpticalInsets = Insets.of(a.getDimensionPixelSize(10, state.mOpticalInsets.left), a.getDimensionPixelSize(11, state.mOpticalInsets.top), a.getDimensionPixelSize(12, state.mOpticalInsets.right), a.getDimensionPixelSize(13, state.mOpticalInsets.bottom));
    }

    public boolean canApplyTheme() {
        return (this.mGradientState == null || !this.mGradientState.canApplyTheme()) ? super.canApplyTheme() : true;
    }

    private void applyThemeChildElements(Theme t) {
        GradientState st = this.mGradientState;
        if (st.mAttrSize != null) {
            TypedArray a = t.resolveAttributes(st.mAttrSize, R.styleable.GradientDrawableSize);
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
        int innerDepth = parser.getDepth() + RADIUS_TYPE_FRACTION;
        while (true) {
            int type = parser.next();
            if (type != RADIUS_TYPE_FRACTION) {
                int depth = parser.getDepth();
                if (depth < innerDepth && type == RING) {
                    return;
                }
                if (type == SWEEP_GRADIENT && depth <= innerDepth) {
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
        pad.set(a.getDimensionPixelOffset(RECTANGLE, pad.left), a.getDimensionPixelOffset(RADIUS_TYPE_FRACTION, pad.top), a.getDimensionPixelOffset(SWEEP_GRADIENT, pad.right), a.getDimensionPixelOffset(RING, pad.bottom));
        this.mPadding = pad;
    }

    private void updateDrawableCorners(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrCorners = a.extractThemeAttrs();
        int radius = a.getDimensionPixelSize(RECTANGLE, (int) st.mRadius);
        setCornerRadius((float) radius);
        int topLeftRadius = a.getDimensionPixelSize(RADIUS_TYPE_FRACTION, radius);
        int topRightRadius = a.getDimensionPixelSize(SWEEP_GRADIENT, radius);
        int bottomLeftRadius = a.getDimensionPixelSize(RING, radius);
        int bottomRightRadius = a.getDimensionPixelSize(4, radius);
        if (topLeftRadius == radius && topRightRadius == radius && bottomLeftRadius == radius) {
            if (bottomRightRadius == radius) {
                return;
            }
        }
        setCornerRadii(new float[]{(float) topLeftRadius, (float) topLeftRadius, (float) topRightRadius, (float) topRightRadius, (float) bottomRightRadius, (float) bottomRightRadius, (float) bottomLeftRadius, (float) bottomLeftRadius});
    }

    private void updateGradientDrawableStroke(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrStroke = a.extractThemeAttrs();
        int width = a.getDimensionPixelSize(RECTANGLE, Math.max(RECTANGLE, st.mStrokeWidth));
        float dashWidth = a.getDimension(SWEEP_GRADIENT, st.mStrokeDashWidth);
        ColorStateList colorStateList = a.getColorStateList(RADIUS_TYPE_FRACTION);
        if (colorStateList == null) {
            colorStateList = st.mStrokeColors;
        }
        if (dashWidth != 0.0f) {
            setStroke(width, colorStateList, dashWidth, a.getDimension(RING, st.mStrokeDashGap));
        } else {
            setStroke(width, colorStateList);
        }
    }

    private void updateGradientDrawableSolid(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrSolid = a.extractThemeAttrs();
        ColorStateList colorStateList = a.getColorStateList(RECTANGLE);
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
        st.mUseLevel = a.getBoolean(SWEEP_GRADIENT, st.mUseLevel);
        st.mGradient = a.getInt(4, st.mGradient);
        int startColor = a.getColor(RECTANGLE, RECTANGLE);
        boolean hasCenterColor = a.hasValue(8);
        int centerColor = a.getColor(8, RECTANGLE);
        int endColor = a.getColor(RADIUS_TYPE_FRACTION, RECTANGLE);
        if (hasCenterColor) {
            st.mGradientColors = new int[RING];
            st.mGradientColors[RECTANGLE] = startColor;
            st.mGradientColors[RADIUS_TYPE_FRACTION] = centerColor;
            st.mGradientColors[SWEEP_GRADIENT] = endColor;
            st.mPositions = new float[RING];
            st.mPositions[RECTANGLE] = 0.0f;
            st.mPositions[RADIUS_TYPE_FRACTION] = st.mCenterX != NetworkHistoryUtils.RECOVERY_PERCENTAGE ? st.mCenterX : st.mCenterY;
            st.mPositions[SWEEP_GRADIENT] = Engine.DEFAULT_VOLUME;
        } else {
            st.mGradientColors = new int[SWEEP_GRADIENT];
            st.mGradientColors[RECTANGLE] = startColor;
            st.mGradientColors[RADIUS_TYPE_FRACTION] = endColor;
        }
        if (st.mGradient == 0) {
            int angle = ((int) a.getFloat(RING, (float) st.mAngle)) % IActivityManager.SET_VR_MODE_TRANSACTION;
            if (angle % 45 != 0) {
                throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'angle' attribute to " + "be a multiple of 45");
            }
            st.mAngle = angle;
            switch (angle) {
                case RECTANGLE /*0*/:
                    st.mOrientation = Orientation.LEFT_RIGHT;
                    return;
                case InformationElement.EID_HT_CAP /*45*/:
                    st.mOrientation = Orientation.BL_TR;
                    return;
                case ToneGenerator.TONE_CDMA_PRESSHOLDKEY_LITE /*90*/:
                    st.mOrientation = Orientation.BOTTOM_TOP;
                    return;
                case Const.CODE_C3_SKIP4_RANGE_END /*135*/:
                    st.mOrientation = Orientation.BR_TL;
                    return;
                case BluetoothAssignedNumbers.BDE_TECHNOLOGY /*180*/:
                    st.mOrientation = Orientation.RIGHT_LEFT;
                    return;
                case BluetoothAssignedNumbers.DANLERS /*225*/:
                    st.mOrientation = Orientation.TR_BL;
                    return;
                case AnqpInformationElement.ANQP_TDLS_CAP /*270*/:
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
                radius = tv.getFraction(Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME);
                if (((tv.data >> RECTANGLE) & 15) == RADIUS_TYPE_FRACTION) {
                    radiusType = SWEEP_GRADIENT;
                } else {
                    radiusType = RADIUS_TYPE_FRACTION;
                }
            } else if (tv.type == 5) {
                radius = tv.getDimension(r.getDisplayMetrics());
                radiusType = RECTANGLE;
            } else {
                radius = tv.getFloat();
                radiusType = RECTANGLE;
            }
            st.mGradientRadius = radius;
            st.mGradientRadiusType = radiusType;
        } else if (st.mGradient == RADIUS_TYPE_FRACTION) {
            throw new XmlPullParserException(a.getPositionDescription() + "<gradient> tag requires 'gradientRadius' " + "attribute with radial type");
        }
    }

    private void updateGradientDrawableSize(TypedArray a) {
        GradientState st = this.mGradientState;
        st.mChangingConfigurations |= a.getChangingConfigurations();
        st.mAttrSize = a.extractThemeAttrs();
        st.mWidth = a.getDimensionPixelSize(RADIUS_TYPE_FRACTION, st.mWidth);
        st.mHeight = a.getDimensionPixelSize(RECTANGLE, st.mHeight);
    }

    private static float getFloatOrFraction(TypedArray a, int index, float defaultValue) {
        TypedValue tv = a.peekValue(index);
        float v = defaultValue;
        if (tv == null) {
            return v;
        }
        return tv.type == 6 ? tv.getFraction(Engine.DEFAULT_VOLUME, Engine.DEFAULT_VOLUME) : tv.getFloat();
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
        if (this.mGradientState.mStrokeWidth >= 0 && this.mStrokePaint != null && !isOpaque(this.mStrokePaint.getColor())) {
            return false;
        }
        if (this.mGradientState.mGradientColors != null || isOpaque(this.mFillPaint.getColor())) {
            return true;
        }
        return false;
    }

    public void getOutline(Outline outline) {
        float modulateAlpha;
        boolean useFillOpacity = true;
        GradientState st = this.mGradientState;
        Rect bounds = getBounds();
        if (!st.mOpaqueOverShape) {
            useFillOpacity = false;
        } else if (!(this.mGradientState.mStrokeWidth <= 0 || this.mStrokePaint == null || this.mStrokePaint.getAlpha() == this.mFillPaint.getAlpha())) {
            useFillOpacity = false;
        }
        if (useFillOpacity) {
            modulateAlpha = ((float) modulateAlpha(this.mFillPaint.getAlpha())) / 255.0f;
        } else {
            modulateAlpha = 0.0f;
        }
        outline.setAlpha(modulateAlpha);
        switch (st.mShape) {
            case RECTANGLE /*0*/:
                if (st.mRadiusArray != null) {
                    buildPathIfDirty();
                    outline.setConvexPath(this.mPath);
                    return;
                }
                float rad = 0.0f;
                if (st.mRadius > 0.0f) {
                    rad = Math.min(st.mRadius, ((float) Math.min(bounds.width(), bounds.height())) * NetworkHistoryUtils.RECOVERY_PERCENTAGE);
                }
                outline.setRoundRect(bounds, rad);
            case RADIUS_TYPE_FRACTION /*1*/:
                outline.setOval(bounds);
            case SWEEP_GRADIENT /*2*/:
                float halfStrokeWidth = this.mStrokePaint == null ? 1.0E-4f : this.mStrokePaint.getStrokeWidth() * NetworkHistoryUtils.RECOVERY_PERCENTAGE;
                float centerY = (float) bounds.centerY();
                outline.setRect(bounds.left, (int) Math.floor((double) (centerY - halfStrokeWidth)), bounds.right, (int) Math.ceil((double) (centerY + halfStrokeWidth)));
            default:
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
        return ((color >> 24) & Process.PROC_TERM_MASK) == Process.PROC_TERM_MASK;
    }

    private GradientDrawable(GradientState state, Resources res) {
        this.mFillPaint = new Paint((int) RADIUS_TYPE_FRACTION);
        this.mAlpha = Process.PROC_TERM_MASK;
        this.mPath = new Path();
        this.mRect = new RectF();
        this.mPathIsDirty = true;
        this.mGradientState = state;
        updateLocalState(res);
    }

    private void updateLocalState(Resources res) {
        GradientState state = this.mGradientState;
        if (state.mSolidColors != null) {
            this.mFillPaint.setColor(state.mSolidColors.getColorForState(getState(), RECTANGLE));
        } else if (state.mGradientColors == null) {
            this.mFillPaint.setColor(RECTANGLE);
        } else {
            this.mFillPaint.setColor(Color.BLACK);
        }
        this.mPadding = state.mPadding;
        if (state.mStrokeWidth >= 0) {
            this.mStrokePaint = new Paint((int) RADIUS_TYPE_FRACTION);
            this.mStrokePaint.setStyle(Style.STROKE);
            this.mStrokePaint.setStrokeWidth((float) state.mStrokeWidth);
            if (state.mStrokeColors != null) {
                this.mStrokePaint.setColor(state.mStrokeColors.getColorForState(getState(), RECTANGLE));
            }
            if (state.mStrokeDashWidth != 0.0f) {
                float[] fArr = new float[SWEEP_GRADIENT];
                fArr[RECTANGLE] = state.mStrokeDashWidth;
                fArr[RADIUS_TYPE_FRACTION] = state.mStrokeDashGap;
                this.mStrokePaint.setPathEffect(new DashPathEffect(fArr, 0.0f));
            }
        }
        this.mTintFilter = updateTintFilter(this.mTintFilter, state.mTint, state.mTintMode);
        this.mGradientIsDirty = true;
        state.computeOpacity();
    }
}

package huawei.android.widget.appbar;

import android.R;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import huawei.android.graphics.drawable.HwEventBadge;
import huawei.android.widget.AnimationUtils;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class CollapsingTextHelper {
    private static final int BUILD_VERSION_SDK_EIGHTEEN = 18;
    private static final int BUILD_VERSION_SDK_SIXTEEN = 16;
    private static final float COMPARE_VALUE = 0.001f;
    private static final boolean DEBUG_DRAW = false;
    private static final Paint DEBUG_DRAW_PAINT = null;
    private static final int DEFAULT_TEXT_SIZE = 15;
    private static final float FLOAT_COMPARE_VALUE = 1.0E-6f;
    private static final int HEIGHT_DICHOTOMY = 2;
    private static final float INVALID_COLLAPSED_TEXT_TOP = 0.0f;
    private static final float INVALID_OVERSCROLL_FRACTION = -1.0f;
    private static final float INVALID_TEXT_BASELINE = -1.0f;
    private static final int MAX_VALUE = 255;
    private static final float OVERSCROLL_FRACTION_MAX = 1.1f;
    static final int SUBTITLE_BEHAVIOR_DEFAULT_SHOW = 0;
    static final int SUBTITLE_BEHAVIOR_GRADUALLY_HIDDEN = 1;
    private static final String TAG = "CollapsingTextHelper";
    private static final boolean USE_SCALING_TEXTURE = (Build.VERSION.SDK_INT < 18);
    private float mBadgeMargin;
    private boolean mBoundsChanged;
    private final Rect mCollapsedBounds;
    private float mCollapsedDrawEyebrowX;
    private float mCollapsedDrawSubX;
    private float mCollapsedDrawX;
    private float mCollapsedDrawY;
    private int mCollapsedShadowColor;
    private float mCollapsedShadowDx;
    private float mCollapsedShadowDy;
    private float mCollapsedShadowRadius;
    private float mCollapsedSubTextSize = 15.0f;
    private ColorStateList mCollapsedTextColor;
    private int mCollapsedTextGravity = 16;
    private float mCollapsedTextSize = 15.0f;
    private float mCollapsedTextTop = INVALID_COLLAPSED_TEXT_TOP;
    private Typeface mCollapsedTypeface;
    private final RectF mCurrentBounds;
    private float mCurrentDrawEyebrowX;
    private float mCurrentDrawSubX;
    private float mCurrentDrawX;
    private float mCurrentDrawY;
    private float mCurrentTextSize;
    private Typeface mCurrentTypeface;
    private ViewGroup mCustomView;
    private final Rect mExpandedBounds;
    private float mExpandedDrawEyebrowX;
    private float mExpandedDrawSubX;
    private float mExpandedDrawX;
    private float mExpandedDrawY;
    private float mExpandedFraction;
    private int mExpandedShadowColor;
    private float mExpandedShadowDx;
    private float mExpandedShadowDy;
    private float mExpandedShadowRadius;
    private float mExpandedSubTextSize = 15.0f;
    private float mExpandedTextBaselineY = -1.0f;
    private ColorStateList mExpandedTextColor;
    private int mExpandedTextGravity = 16;
    private float mExpandedTextSize = 15.0f;
    private float mExpandedTextWidth = INVALID_COLLAPSED_TEXT_TOP;
    private Bitmap mExpandedTitleTexture;
    private Typeface mExpandedTypeface;
    private CharSequence mEyeTextToDraw;
    private CharSequence mEyebrowText;
    private int mEyebrowTextColorAlpha = MAX_VALUE;
    private final TextPaint mEyebrowTextPaint;
    private HwEventBadge mHwEventBadge;
    private boolean mIsAppBarMoved = false;
    private boolean mIsAppBarOverScrolled = false;
    private boolean mIsDrawTitle;
    private boolean mIsRtl;
    private boolean mIsSmoothScaleEnabled = false;
    private Interpolator mPositionInterpolator;
    private float mScale;
    private int[] mState;
    private CharSequence mSubText;
    private int mSubTextBehaviorFlag = 0;
    private int mSubTextColorAlpha = MAX_VALUE;
    private final TextPaint mSubTextPaint;
    private CharSequence mSubTextToDraw;
    private float mTargetOverScrollFraction = -1.0f;
    private CharSequence mText;
    private final TextPaint mTextPaint;
    private Interpolator mTextSizeInterpolator;
    private CharSequence mTextToDraw;
    private float mTextureAscent;
    private float mTextureDescent;
    private Paint mTexturePaint;
    private float mTitleMargin;
    private boolean mUseTexture;
    private final View mView;

    static {
        Paint paint = DEBUG_DRAW_PAINT;
        if (paint != null) {
            paint.setAntiAlias(true);
            DEBUG_DRAW_PAINT.setColor(-65281);
        }
    }

    public CollapsingTextHelper(View view) {
        this.mView = view;
        int badgeMarginId = ResLoader.getInstance().getIdentifier(this.mView.getContext(), ResLoaderUtil.DIMEN, "margin_m");
        this.mTitleMargin = this.mView.getContext().getResources().getDimension(ResLoader.getInstance().getIdentifier(this.mView.getContext(), ResLoaderUtil.DIMEN, "margin_xs"));
        this.mBadgeMargin = this.mView.getContext().getResources().getDimension(badgeMarginId);
        this.mTextPaint = new TextPaint(129);
        this.mSubTextPaint = new TextPaint(129);
        this.mEyebrowTextPaint = new TextPaint(129);
        this.mCollapsedBounds = new Rect();
        this.mExpandedBounds = new Rect();
        this.mCurrentBounds = new RectF();
    }

    /* access modifiers changed from: package-private */
    public void setTextSizeInterpolator(Interpolator interpolator) {
        this.mTextSizeInterpolator = interpolator;
        recalculate();
    }

    /* access modifiers changed from: package-private */
    public void setPositionInterpolator(Interpolator interpolator) {
        this.mPositionInterpolator = interpolator;
        recalculate();
    }

    /* access modifiers changed from: package-private */
    public void setExpandedTextSize(float textSize) {
        if (!isEquals(textSize, INVALID_COLLAPSED_TEXT_TOP) && !isClose(this.mExpandedTextSize, textSize)) {
            this.mExpandedTextSize = textSize;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedTextSize(float textSize) {
        if (!isClose(this.mCollapsedTextSize, textSize)) {
            this.mCollapsedTextSize = textSize;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedTextColor(ColorStateList textColor) {
        if (this.mCollapsedTextColor != textColor) {
            this.mCollapsedTextColor = textColor;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setExpandedTextColor(ColorStateList textColor) {
        if (this.mExpandedTextColor != textColor) {
            this.mExpandedTextColor = textColor;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setExpandedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(this.mExpandedBounds, left, top, right, bottom)) {
            this.mExpandedBounds.set(left, top, right, bottom);
            this.mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedBounds(int left, int top, int right, int bottom) {
        if (!rectEquals(this.mCollapsedBounds, left, top, right, bottom)) {
            this.mCollapsedBounds.set(left, top, right, bottom);
            this.mBoundsChanged = true;
            onBoundsChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public void onBoundsChanged() {
        this.mIsDrawTitle = this.mCollapsedBounds.width() > 0 && this.mCollapsedBounds.height() > 0 && this.mExpandedBounds.width() > 0 && this.mExpandedBounds.height() > 0;
    }

    /* access modifiers changed from: package-private */
    public void setExpandedTextGravity(int gravity) {
        if (this.mExpandedTextGravity != gravity) {
            this.mExpandedTextGravity = gravity;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public int getExpandedTextGravity() {
        return this.mExpandedTextGravity;
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedTextGravity(int gravity) {
        if (this.mCollapsedTextGravity != gravity) {
            this.mCollapsedTextGravity = gravity;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public int getCollapsedTextGravity() {
        return this.mCollapsedTextGravity;
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedTextAppearance(int resId) {
        Resources.Theme theme = this.mView.getContext().getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(resId, R.styleable.TextAppearance);
            this.mCollapsedTextColor = a.getColorStateList(3);
            this.mCollapsedTextSize = (float) a.getDimensionPixelSize(0, (int) this.mCollapsedTextSize);
            this.mCollapsedShadowColor = a.getInt(7, 0);
            this.mCollapsedShadowDx = a.getFloat(8, INVALID_COLLAPSED_TEXT_TOP);
            this.mCollapsedShadowDy = a.getFloat(9, INVALID_COLLAPSED_TEXT_TOP);
            this.mCollapsedShadowRadius = a.getFloat(10, INVALID_COLLAPSED_TEXT_TOP);
            a.recycle();
            if (Build.VERSION.SDK_INT >= 16) {
                this.mCollapsedTypeface = readFontFamilyTypeface(resId);
            }
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setExpandedTextAppearance(int resId) {
        Resources.Theme theme = this.mView.getContext().getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(resId, R.styleable.TextAppearance);
            this.mExpandedTextColor = a.getColorStateList(3);
            this.mExpandedTextSize = (float) a.getDimensionPixelSize(0, (int) this.mExpandedTextSize);
            this.mExpandedShadowColor = a.getInt(7, 0);
            this.mExpandedShadowDx = a.getFloat(8, INVALID_COLLAPSED_TEXT_TOP);
            this.mExpandedShadowDy = a.getFloat(9, INVALID_COLLAPSED_TEXT_TOP);
            this.mExpandedShadowRadius = a.getFloat(10, INVALID_COLLAPSED_TEXT_TOP);
            a.recycle();
            if (Build.VERSION.SDK_INT >= 16) {
                this.mExpandedTypeface = readFontFamilyTypeface(resId);
            }
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setSubTextAppearance(int resId) {
        Resources.Theme theme = this.mView.getContext().getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(resId, R.styleable.TextAppearance);
            this.mSubTextPaint.setColor(a.getColor(3, 0));
            this.mSubTextColorAlpha = this.mSubTextPaint.getAlpha();
            this.mExpandedSubTextSize = a.getDimension(0, this.mExpandedSubTextSize);
            float f = this.mExpandedSubTextSize;
            this.mCollapsedSubTextSize = f;
            this.mSubTextPaint.setTextSize(f);
            a.recycle();
            if (Build.VERSION.SDK_INT >= 16) {
                this.mSubTextPaint.setTypeface(readFontFamilyTypeface(resId));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setEyebrowTextAppearance(int resId) {
        Resources.Theme theme = this.mView.getContext().getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(resId, R.styleable.TextAppearance);
            this.mEyebrowTextPaint.setColor(a.getColor(3, 0));
            this.mEyebrowTextColorAlpha = this.mEyebrowTextPaint.getAlpha();
            this.mEyebrowTextPaint.setTextSize((float) a.getDimensionPixelSize(0, (int) this.mExpandedTextSize));
            a.recycle();
            if (Build.VERSION.SDK_INT >= 16) {
                this.mEyebrowTextPaint.setTypeface(readFontFamilyTypeface(resId));
            }
        }
    }

    private Typeface readFontFamilyTypeface(int resId) {
        TypedArray a = this.mView.getContext().obtainStyledAttributes(resId, new int[]{16843692});
        try {
            String family = a.getString(0);
            if (family != null) {
                return Typeface.create(family, 0);
            }
            a.recycle();
            return Typeface.DEFAULT;
        } finally {
            a.recycle();
        }
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedTypeface(Typeface typeface) {
        if (areTypefacesDifferent(this.mCollapsedTypeface, typeface)) {
            this.mCollapsedTypeface = typeface;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setExpandedTypeface(Typeface typeface) {
        if (areTypefacesDifferent(this.mExpandedTypeface, typeface)) {
            this.mExpandedTypeface = typeface;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setTypefaces(Typeface typeface) {
        this.mExpandedTypeface = typeface;
        this.mCollapsedTypeface = typeface;
        recalculate();
    }

    /* access modifiers changed from: package-private */
    public Typeface getCollapsedTypeface() {
        Typeface typeface = this.mCollapsedTypeface;
        return typeface != null ? typeface : Typeface.DEFAULT;
    }

    /* access modifiers changed from: package-private */
    public Typeface getExpandedTypeface() {
        Typeface typeface = this.mExpandedTypeface;
        return typeface != null ? typeface : Typeface.DEFAULT;
    }

    /* access modifiers changed from: package-private */
    public void setExpansionFraction(float fraction) {
        float realFraction = MathUtils.clamp(fraction, (float) INVALID_COLLAPSED_TEXT_TOP, 1.0f);
        if (realFraction != this.mExpandedFraction) {
            this.mExpandedFraction = realFraction;
            calculateCurrentOffsets();
        }
    }

    /* access modifiers changed from: package-private */
    public void setOverScrollParameters(float overScrollFraction, float appBarOverScrollY) {
        this.mIsAppBarOverScrolled = !isEquals(overScrollFraction, INVALID_COLLAPSED_TEXT_TOP);
        this.mTargetOverScrollFraction = MathUtils.clamp(overScrollFraction, (float) INVALID_COLLAPSED_TEXT_TOP, 1.0f);
        this.mCurrentDrawY = this.mExpandedTextBaselineY + appBarOverScrollY;
    }

    /* access modifiers changed from: package-private */
    public void setIsAppBarMoved(boolean isAppBarMoved) {
        this.mIsAppBarMoved = isAppBarMoved;
    }

    /* access modifiers changed from: package-private */
    public void resetTargetOverScrollFraction() {
        this.mTargetOverScrollFraction = -1.0f;
    }

    /* access modifiers changed from: package-private */
    public void calculateTextSizeOnOverScroll() {
        calculateUsingTextSize(transformedTextSize(this.mTargetOverScrollFraction));
        setCustomViewOffsetsPadding();
        if (this.mCustomView != null) {
            drawCustomView(this.mCurrentDrawX, this.mCurrentDrawY);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean setState(int[] state) {
        this.mState = state;
        if (!isStateful()) {
            return false;
        }
        recalculate();
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean isStateful() {
        ColorStateList colorStateList;
        ColorStateList colorStateList2 = this.mCollapsedTextColor;
        return (colorStateList2 != null && colorStateList2.isStateful()) || ((colorStateList = this.mExpandedTextColor) != null && colorStateList.isStateful());
    }

    /* access modifiers changed from: package-private */
    public float getExpansionFraction() {
        return this.mExpandedFraction;
    }

    /* access modifiers changed from: package-private */
    public float getCollapsedTextSize() {
        return this.mCollapsedTextSize;
    }

    /* access modifiers changed from: package-private */
    public float getExpandedTextSize() {
        return this.mExpandedTextSize;
    }

    private void calculateCurrentOffsets() {
        calculateOffsets(this.mExpandedFraction);
    }

    private void calculateOffsets(float fraction) {
        interpolateBounds(fraction);
        this.mCurrentDrawX = lerp(this.mExpandedDrawX, this.mCollapsedDrawX, fraction, this.mPositionInterpolator);
        this.mCurrentDrawSubX = lerp(this.mExpandedDrawSubX, this.mCollapsedDrawSubX, fraction, this.mPositionInterpolator);
        this.mCurrentDrawEyebrowX = lerp(this.mExpandedDrawEyebrowX, this.mCollapsedDrawEyebrowX, fraction, this.mPositionInterpolator);
        this.mCurrentDrawY = lerp(this.mExpandedDrawY, this.mCollapsedDrawY, fraction, this.mPositionInterpolator);
        float currentSubTextSize = lerp(this.mExpandedSubTextSize, this.mCollapsedSubTextSize, fraction, this.mTextSizeInterpolator);
        this.mSubTextPaint.setTextSize(currentSubTextSize);
        this.mSubTextPaint.setLinearText(!isEquals(currentSubTextSize / this.mExpandedSubTextSize, 1.0f));
        setInterpolatedTextSize(lerp(this.mExpandedTextSize, this.mCollapsedTextSize, fraction, this.mTextSizeInterpolator));
        setCustomViewOffsetsPadding();
        if (this.mCustomView != null) {
            drawCustomView(this.mCurrentDrawX, this.mCurrentDrawY);
        }
        if (this.mCollapsedTextColor != this.mExpandedTextColor) {
            this.mTextPaint.setColor(blendColors(getCurrentExpandedTextColor(), getCurrentCollapsedTextColor(), fraction));
        } else {
            this.mTextPaint.setColor(getCurrentCollapsedTextColor());
        }
        this.mTextPaint.setShadowLayer(lerp(this.mExpandedShadowRadius, this.mCollapsedShadowRadius, fraction, null), lerp(this.mExpandedShadowDx, this.mCollapsedShadowDx, fraction, null), lerp(this.mExpandedShadowDy, this.mCollapsedShadowDy, fraction, null), blendColors(this.mExpandedShadowColor, this.mCollapsedShadowColor, fraction));
        this.mView.postInvalidateOnAnimation();
    }

    private boolean isEquals(float value, float targetValue) {
        return Math.abs(value - targetValue) < FLOAT_COMPARE_VALUE;
    }

    private void setCustomViewOffsetsPadding() {
        float badgeWidth = this.mHwEventBadge != null ? ((float) getBadgeWidth()) + this.mBadgeMargin : INVALID_COLLAPSED_TEXT_TOP;
        ViewGroup viewGroup = this.mCustomView;
        if (viewGroup != null) {
            int collaspingOffsets = 0;
            float actualWidth = ((((float) viewGroup.getWidth()) * this.mCollapsedTextSize) / this.mExpandedTextSize) + badgeWidth;
            if (actualWidth > ((float) this.mCollapsedBounds.width())) {
                collaspingOffsets = (int) (actualWidth - ((float) this.mCollapsedBounds.width()));
            }
            float f = this.mExpandedFraction;
            int offsetsPadding = (int) (((((float) (this.mCustomView.getWidth() - this.mExpandedBounds.width())) + badgeWidth) * (1.0f - f)) + (((float) collaspingOffsets) * f));
            if (this.mIsRtl) {
                ViewGroup viewGroup2 = this.mCustomView;
                viewGroup2.setPadding(offsetsPadding, viewGroup2.getPaddingTop(), this.mCustomView.getPaddingRight(), this.mCustomView.getPaddingBottom());
                return;
            }
            ViewGroup viewGroup3 = this.mCustomView;
            viewGroup3.setPadding(viewGroup3.getPaddingLeft(), this.mCustomView.getPaddingTop(), offsetsPadding, this.mCustomView.getPaddingBottom());
        }
    }

    private int getCurrentExpandedTextColor() {
        int[] iArr = this.mState;
        if (iArr != null) {
            return this.mExpandedTextColor.getColorForState(iArr, 0);
        }
        return this.mExpandedTextColor.getDefaultColor();
    }

    private int getCurrentCollapsedTextColor() {
        int[] iArr = this.mState;
        if (iArr != null) {
            return this.mCollapsedTextColor.getColorForState(iArr, 0);
        }
        return this.mCollapsedTextColor.getDefaultColor();
    }

    private void calculateBaseOffsets() {
        float currentTextSize = this.mCurrentTextSize;
        calculateUsingTextSize(this.mCollapsedTextSize);
        CharSequence charSequence = this.mTextToDraw;
        float width = charSequence != null ? this.mTextPaint.measureText(charSequence, 0, charSequence.length()) : INVALID_COLLAPSED_TEXT_TOP;
        CharSequence charSequence2 = this.mSubTextToDraw;
        float subWidth = charSequence2 != null ? this.mSubTextPaint.measureText(charSequence2, 0, charSequence2.length()) : INVALID_COLLAPSED_TEXT_TOP;
        CharSequence charSequence3 = this.mEyeTextToDraw;
        float eyebrowWidth = charSequence3 != null ? this.mEyebrowTextPaint.measureText(charSequence3, 0, charSequence3.length()) : INVALID_COLLAPSED_TEXT_TOP;
        int collapsedAbsGravity = Gravity.getAbsoluteGravity(this.mCollapsedTextGravity, this.mIsRtl ? 1 : 0);
        updateCollapsedY(collapsedAbsGravity);
        int i = (collapsedAbsGravity | 8388608) & 8388615;
        if (i == 1) {
            this.mCollapsedDrawX = ((float) this.mCollapsedBounds.centerX()) - (width / 2.0f);
            this.mCollapsedDrawSubX = ((float) this.mCollapsedBounds.centerX()) - (subWidth / 2.0f);
            this.mCollapsedDrawEyebrowX = ((float) this.mCollapsedBounds.centerX()) - (eyebrowWidth / 2.0f);
        } else if (i != 8388613) {
            this.mCollapsedDrawX = (float) this.mCollapsedBounds.left;
            this.mCollapsedDrawSubX = (float) this.mCollapsedBounds.left;
            this.mCollapsedDrawEyebrowX = (float) this.mCollapsedBounds.left;
        } else {
            this.mCollapsedDrawX = (float) this.mCollapsedBounds.right;
            this.mCollapsedDrawSubX = ((float) this.mCollapsedBounds.right) - subWidth;
            this.mCollapsedDrawEyebrowX = ((float) this.mCollapsedBounds.right) - eyebrowWidth;
        }
        calculateUsingTextSize(this.mExpandedTextSize);
        CharSequence charSequence4 = this.mTextToDraw;
        float width2 = charSequence4 != null ? this.mTextPaint.measureText(charSequence4, 0, charSequence4.length()) : INVALID_COLLAPSED_TEXT_TOP;
        this.mExpandedTextWidth = width2;
        CharSequence charSequence5 = this.mSubTextToDraw;
        float subWidth2 = charSequence5 != null ? this.mSubTextPaint.measureText(charSequence5, 0, charSequence5.length()) : INVALID_COLLAPSED_TEXT_TOP;
        CharSequence charSequence6 = this.mEyeTextToDraw;
        float eyebrowWidth2 = charSequence6 != null ? this.mEyebrowTextPaint.measureText(charSequence6, 0, charSequence6.length()) : INVALID_COLLAPSED_TEXT_TOP;
        int expandedAbsGravity = Gravity.getAbsoluteGravity(this.mExpandedTextGravity, this.mIsRtl ? 1 : 0);
        updateExpandedY(expandedAbsGravity);
        int i2 = (expandedAbsGravity | 8388608) & 8388615;
        if (i2 == 1) {
            this.mExpandedDrawX = ((float) this.mExpandedBounds.centerX()) - (width2 / 2.0f);
            this.mExpandedDrawSubX = ((float) this.mExpandedBounds.centerX()) - (subWidth2 / 2.0f);
            this.mExpandedDrawEyebrowX = ((float) this.mExpandedBounds.centerX()) - (eyebrowWidth2 / 2.0f);
        } else if (i2 != 8388613) {
            this.mExpandedDrawX = (float) this.mExpandedBounds.left;
            this.mExpandedDrawSubX = (float) this.mExpandedBounds.left;
            this.mExpandedDrawEyebrowX = (float) this.mExpandedBounds.left;
        } else {
            this.mExpandedDrawX = (float) this.mExpandedBounds.right;
            this.mExpandedDrawSubX = ((float) this.mExpandedBounds.right) - subWidth2;
            this.mExpandedDrawEyebrowX = ((float) this.mExpandedBounds.right) - eyebrowWidth2;
        }
        clearTexture();
        setInterpolatedTextSize(currentTextSize);
    }

    private void updateCollapsedY(int collapsedAbsGravity) {
        int i = collapsedAbsGravity & 112;
        if (i == 48) {
            this.mCollapsedDrawY = ((float) this.mCollapsedBounds.top) - this.mTextPaint.ascent();
        } else if (i != 80) {
            this.mCollapsedDrawY = ((float) this.mCollapsedBounds.centerY()) + (((this.mTextPaint.descent() - this.mTextPaint.ascent()) / 2.0f) - this.mTextPaint.descent());
        } else {
            this.mCollapsedDrawY = (float) this.mCollapsedBounds.bottom;
        }
    }

    private void updateExpandedY(int expandedAbsGravity) {
        int i = expandedAbsGravity & 112;
        if (i == 48) {
            this.mExpandedDrawY = ((float) this.mExpandedBounds.top) - this.mTextPaint.ascent();
        } else if (i != 80) {
            this.mExpandedDrawY = ((float) this.mExpandedBounds.centerY()) + (((this.mTextPaint.descent() - this.mTextPaint.ascent()) / 2.0f) - this.mTextPaint.descent());
        } else {
            this.mExpandedDrawY = (float) this.mExpandedBounds.bottom;
        }
    }

    private void interpolateBounds(float fraction) {
        this.mCurrentBounds.left = lerp((float) this.mExpandedBounds.left, (float) this.mCollapsedBounds.left, fraction, this.mPositionInterpolator);
        this.mCurrentBounds.top = lerp(this.mExpandedDrawY, this.mCollapsedDrawY, fraction, this.mPositionInterpolator);
        this.mCurrentBounds.right = lerp((float) this.mExpandedBounds.right, (float) this.mCollapsedBounds.right, fraction, this.mPositionInterpolator);
        this.mCurrentBounds.bottom = lerp((float) this.mExpandedBounds.bottom, (float) this.mCollapsedBounds.bottom, fraction, this.mPositionInterpolator);
    }

    public void draw(Canvas canvas) {
        float ascent;
        float y;
        float textRtlOffset;
        float textOffset;
        float y2;
        CharSequence charSequence;
        int saveCount = canvas.save();
        boolean drawTexture = true;
        if (((this.mTextToDraw == null && this.mCustomView == null) ? false : true) && this.mIsDrawTitle) {
            float x = this.mCurrentDrawX;
            float y3 = this.mCurrentDrawY;
            new RectF();
            if (this.mTextToDraw != null && !isEquals(this.mCollapsedTextTop, INVALID_COLLAPSED_TEXT_TOP)) {
                y3 += this.mCollapsedTextTop - computeTextRectF().top;
            }
            if (!this.mUseTexture || this.mExpandedTitleTexture == null) {
                drawTexture = false;
            }
            if (drawTexture) {
                float f = this.mTextureAscent;
                float f2 = this.mScale;
                float f3 = this.mTextureDescent * f2;
                ascent = f * f2;
            } else {
                float ascent2 = this.mTextPaint.ascent() * this.mScale;
                float descent = this.mScale * this.mTextPaint.descent();
                ascent = ascent2;
            }
            if (drawTexture) {
                y = y3 + ascent;
            } else {
                y = y3;
            }
            drawOtherText(canvas, y);
            if (drawTexture) {
                canvas.drawBitmap(this.mExpandedTitleTexture, x, y, this.mTexturePaint);
            } else {
                ViewGroup viewGroup = this.mCustomView;
                if (viewGroup == null || viewGroup.getChildAt(0) == null) {
                    CharSequence charSequence2 = this.mTextToDraw;
                    if (charSequence2 != null) {
                        textRtlOffset = this.mTextPaint.measureText(charSequence2, 0, charSequence2.length());
                    } else {
                        textRtlOffset = 0.0f;
                    }
                } else {
                    textRtlOffset = ((float) this.mCustomView.getChildAt(0).getWidth()) * this.mScale;
                }
                if (this.mIsRtl) {
                    textOffset = textRtlOffset;
                } else {
                    textOffset = 0.0f;
                }
                if (this.mCustomView != null || (charSequence = this.mTextToDraw) == null) {
                    y2 = y;
                } else {
                    y2 = y;
                    canvas.drawText(charSequence, 0, charSequence.length(), x - textOffset, y, this.mTextPaint);
                }
                drawBadge(canvas, x - textOffset, y2);
            }
        }
        canvas.restoreToCount(saveCount);
    }

    private void drawBadge(Canvas canvas, float x, float y) {
        if (this.mHwEventBadge != null) {
            float badgeOffsetX = INVALID_COLLAPSED_TEXT_TOP;
            ViewGroup viewGroup = this.mCustomView;
            if (viewGroup != null) {
                badgeOffsetX = (((float) viewGroup.getChildAt(0).getWidth()) * this.mScale) + this.mBadgeMargin;
            } else {
                CharSequence charSequence = this.mTextToDraw;
                if (charSequence != null) {
                    badgeOffsetX = this.mTextPaint.measureText(charSequence, 0, charSequence.length()) + this.mBadgeMargin;
                }
            }
            if (this.mIsRtl) {
                badgeOffsetX = ((float) (-getBadgeWidth())) - this.mBadgeMargin;
            }
            canvas.translate(x + badgeOffsetX, y + (((this.mTextPaint.ascent() + this.mTextPaint.descent()) / 2.0f) - ((float) ((this.mHwEventBadge.getBounds().bottom - this.mHwEventBadge.getBounds().top) / 2))));
            this.mHwEventBadge.draw(canvas);
        }
    }

    private float getCollapsedTextTop() {
        Path textPath = new Path();
        this.mTextPaint.setTextSize(this.mCollapsedTextSize);
        this.mTextPaint.getTextPath(this.mTextToDraw.toString(), 0, this.mTextToDraw.length(), INVALID_COLLAPSED_TEXT_TOP, INVALID_COLLAPSED_TEXT_TOP, textPath);
        RectF boundsPath = new RectF();
        textPath.computeBounds(boundsPath, true);
        this.mTextPaint.setTextSize(this.mCurrentTextSize);
        return boundsPath.top;
    }

    private RectF computeTextRectF() {
        Path textPath = new Path();
        this.mTextPaint.getTextPath(this.mTextToDraw.toString(), 0, this.mTextToDraw.length(), INVALID_COLLAPSED_TEXT_TOP, INVALID_COLLAPSED_TEXT_TOP, textPath);
        RectF boundsPath = new RectF();
        textPath.computeBounds(boundsPath, true);
        return boundsPath;
    }

    private void drawCustomView(float x, float y) {
        if (this.mIsSmoothScaleEnabled) {
            ViewGroup viewGroup = this.mCustomView;
            if (viewGroup instanceof SmoothScaleLinearLayout) {
                ((SmoothScaleLinearLayout) viewGroup).setSmoothScale(checkAppBarIsActive());
            }
        }
        if (this.mIsRtl) {
            ViewGroup viewGroup2 = this.mCustomView;
            viewGroup2.setTranslationX(x - ((float) viewGroup2.getWidth()));
        } else {
            this.mCustomView.setTranslationX(x);
        }
        Paint.FontMetricsInt textMetricsInt = this.mTextPaint.getFontMetricsInt();
        this.mCustomView.setTranslationY(y + ((float) textMetricsInt.ascent) + ((((float) (textMetricsInt.descent - textMetricsInt.ascent)) - (((float) this.mCustomView.getHeight()) * this.mScale)) / 2.0f));
        if (this.mIsRtl) {
            ViewGroup viewGroup3 = this.mCustomView;
            viewGroup3.setPivotX((float) viewGroup3.getWidth());
        } else {
            this.mCustomView.setPivotX(INVALID_COLLAPSED_TEXT_TOP);
        }
        this.mCustomView.setPivotY(INVALID_COLLAPSED_TEXT_TOP);
        this.mCustomView.setScaleX(this.mScale);
        this.mCustomView.setScaleY(this.mScale);
        this.mCustomView.invalidate();
    }

    private boolean checkAppBarIsActive() {
        return this.mIsAppBarOverScrolled || this.mIsAppBarMoved;
    }

    private void drawOtherText(Canvas canvas, float y) {
        int i;
        if (this.mSubTextToDraw != null) {
            Paint.FontMetrics fmInt = this.mSubTextPaint.getFontMetrics();
            Paint.FontMetrics fmTetInt = this.mTextPaint.getFontMetrics();
            int i2 = this.mSubTextBehaviorFlag;
            if (i2 == 1) {
                this.mSubTextPaint.setAlpha((int) ((1.0f - this.mExpandedFraction) * ((float) this.mSubTextColorAlpha)));
            } else if (i2 == 0 && this.mSubTextPaint.getAlpha() != (i = this.mSubTextColorAlpha)) {
                this.mSubTextPaint.setAlpha(i);
            }
            CharSequence charSequence = this.mSubTextToDraw;
            canvas.drawText(charSequence, 0, charSequence.length(), this.mCurrentDrawSubX, y + ((fmTetInt.bottom + this.mTitleMargin) - fmInt.top), this.mSubTextPaint);
        }
        if (this.mEyebrowText != null) {
            Paint.FontMetrics fmInt3 = this.mTextPaint.getFontMetrics();
            Paint.FontMetrics fmInt2 = this.mEyebrowTextPaint.getFontMetrics();
            this.mEyebrowTextPaint.setAlpha((int) ((1.0f - this.mExpandedFraction) * ((float) this.mEyebrowTextColorAlpha)));
            float f = fmInt3.top;
            float f2 = this.mTitleMargin;
            CharSequence charSequence2 = this.mEyeTextToDraw;
            canvas.drawText(charSequence2, 0, charSequence2.length(), this.mCurrentDrawEyebrowX, y + (((f - f2) - (f2 / 2.0f)) - fmInt2.bottom), this.mEyebrowTextPaint);
        }
    }

    private boolean calculateIsRtl(CharSequence text) {
        TextDirectionHeuristic textDirectionHeuristic;
        boolean defaultIsRtl = true;
        if (this.mView.getLayoutDirection() != 1) {
            defaultIsRtl = false;
        }
        if (defaultIsRtl) {
            textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_RTL;
        } else {
            textDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR;
        }
        return textDirectionHeuristic.isRtl(text, 0, text.length());
    }

    private float transformedTextSize(float fraction) {
        float f = this.mExpandedTextSize;
        return lerp(f, OVERSCROLL_FRACTION_MAX * f, fraction, this.mTextSizeInterpolator);
    }

    private void setInterpolatedTextSize(float textSize) {
        calculateUsingTextSize(textSize);
        this.mUseTexture = USE_SCALING_TEXTURE && this.mScale != 1.0f;
        if (this.mUseTexture) {
            ensureExpandedTexture();
        }
        this.mView.postInvalidateOnAnimation();
    }

    private boolean areTypefacesDifferent(Typeface first, Typeface second) {
        return (first != null && !first.equals(second)) || (first == null && second != null);
    }

    private void calculateUsingTextSize(float textSize) {
        float availableWidth;
        float newTextSize;
        if (this.mText != null || this.mCustomView != null) {
            float collapsedWidth = (float) this.mCollapsedBounds.width();
            float expandedWidth = (float) this.mExpandedBounds.width();
            boolean updateDrawText = false;
            if (isClose(textSize, this.mCollapsedTextSize)) {
                newTextSize = this.mCollapsedTextSize;
                this.mScale = newTextSize / this.mExpandedTextSize;
                if (areTypefacesDifferent(this.mCurrentTypeface, this.mCollapsedTypeface)) {
                    this.mCurrentTypeface = this.mCollapsedTypeface;
                    updateDrawText = true;
                }
                availableWidth = collapsedWidth;
            } else {
                if (areTypefacesDifferent(this.mCurrentTypeface, this.mExpandedTypeface)) {
                    this.mCurrentTypeface = this.mExpandedTypeface;
                    updateDrawText = true;
                }
                if (isClose(textSize, this.mExpandedTextSize)) {
                    newTextSize = this.mExpandedTextSize;
                    this.mScale = 1.0f;
                    availableWidth = expandedWidth;
                } else {
                    newTextSize = textSize;
                    this.mScale = textSize / this.mExpandedTextSize;
                    availableWidth = expandedWidth - ((expandedWidth - collapsedWidth) * this.mExpandedFraction);
                }
            }
            dealOtherText(availableWidth);
            float availableWidth2 = availableWidth - (this.mHwEventBadge != null ? ((float) getBadgeWidth()) + this.mBadgeMargin : 0.0f);
            boolean z = false;
            if (availableWidth2 > INVALID_COLLAPSED_TEXT_TOP) {
                updateDrawText = this.mCurrentTextSize != newTextSize || this.mBoundsChanged || updateDrawText;
                this.mCurrentTextSize = newTextSize;
                this.mBoundsChanged = false;
            }
            if (this.mTextToDraw == null || updateDrawText) {
                this.mTextPaint.setTextSize(this.mCurrentTextSize);
                adjustTextSizeWhenAppBarOverScroll();
                this.mTextPaint.setTypeface(this.mCurrentTypeface);
                this.mTextPaint.setLinearText(this.mScale != 1.0f);
                if (this.mCustomView != null) {
                    if (this.mView.getLayoutDirection() == 1) {
                        z = true;
                    }
                    this.mIsRtl = z;
                    return;
                }
                CharSequence charSequence = this.mText;
                if (charSequence != null) {
                    CharSequence title = TextUtils.ellipsize(charSequence, this.mTextPaint, availableWidth2, TextUtils.TruncateAt.END);
                    if (!TextUtils.equals(title, this.mTextToDraw)) {
                        this.mTextToDraw = title;
                        this.mIsRtl = calculateIsRtl(this.mTextToDraw);
                    }
                }
            }
        }
    }

    private void adjustTextSizeWhenAppBarOverScroll() {
        CharSequence charSequence;
        if (this.mIsAppBarOverScrolled && this.mExpandedTextWidth > INVALID_COLLAPSED_TEXT_TOP && this.mCurrentTextSize > this.mExpandedTextSize && (charSequence = this.mTextToDraw) != null && this.mTextPaint.measureText(charSequence, 0, charSequence.length()) < this.mExpandedTextWidth) {
            this.mCurrentTextSize = this.mExpandedTextSize;
            this.mTextPaint.setTextSize(this.mCurrentTextSize);
        }
    }

    private void dealOtherText(float availableWidth) {
        if (availableWidth > INVALID_COLLAPSED_TEXT_TOP) {
            CharSequence charSequence = this.mSubText;
            if (charSequence != null) {
                CharSequence subTitle = TextUtils.ellipsize(charSequence, this.mSubTextPaint, availableWidth, TextUtils.TruncateAt.END);
                if (!TextUtils.equals(subTitle, this.mSubTextToDraw)) {
                    this.mSubTextToDraw = subTitle;
                }
            }
            CharSequence subTitle2 = this.mEyebrowText;
            if (subTitle2 != null) {
                CharSequence eyebrowTitle = TextUtils.ellipsize(subTitle2, this.mEyebrowTextPaint, availableWidth, TextUtils.TruncateAt.END);
                if (!TextUtils.equals(eyebrowTitle, this.mEyeTextToDraw)) {
                    this.mEyeTextToDraw = eyebrowTitle;
                }
            }
        }
    }

    private void ensureExpandedTexture() {
        if (this.mExpandedTitleTexture == null && !this.mExpandedBounds.isEmpty() && !TextUtils.isEmpty(this.mTextToDraw)) {
            calculateOffsets(INVALID_COLLAPSED_TEXT_TOP);
            this.mTextureAscent = this.mTextPaint.ascent();
            this.mTextureDescent = this.mTextPaint.descent();
            TextPaint textPaint = this.mTextPaint;
            CharSequence charSequence = this.mTextToDraw;
            int w = Math.round(textPaint.measureText(charSequence, 0, charSequence.length()));
            int h = Math.round(this.mTextureDescent - this.mTextureAscent);
            if (w > 0 && h > 0) {
                this.mExpandedTitleTexture = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(this.mExpandedTitleTexture);
                CharSequence charSequence2 = this.mTextToDraw;
                c.drawText(charSequence2, 0, charSequence2.length(), INVALID_COLLAPSED_TEXT_TOP, ((float) h) - this.mTextPaint.descent(), this.mTextPaint);
                if (this.mTexturePaint == null) {
                    this.mTexturePaint = new Paint(3);
                }
            }
        }
    }

    public void recalculate() {
        if (this.mView.getHeight() > 0 && this.mView.getWidth() > 0) {
            calculateBaseOffsets();
            if (this.mTextToDraw != null && isEquals(this.mCollapsedTextTop, INVALID_COLLAPSED_TEXT_TOP)) {
                this.mCollapsedTextTop = getCollapsedTextTop();
            }
            setExpandedTextBaselineY(this.mExpandedDrawY);
            if (!this.mIsAppBarOverScrolled) {
                calculateCurrentOffsets();
            } else if (!isEquals(this.mTargetOverScrollFraction, -1.0f)) {
                calculateTextSizeOnOverScroll();
            }
            resetTargetOverScrollFraction();
        }
    }

    /* access modifiers changed from: package-private */
    public void setText(CharSequence text) {
        if (text == null || !text.equals(this.mText)) {
            this.mText = text;
            this.mTextToDraw = null;
            clearTexture();
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setSubText(CharSequence subText) {
        if (subText == null || !subText.equals(this.mSubText)) {
            this.mSubText = subText;
            this.mSubTextToDraw = null;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public void setEyebrowText(CharSequence eyebrowText) {
        if (eyebrowText == null || !eyebrowText.equals(this.mEyebrowText)) {
            this.mEyebrowText = eyebrowText;
            this.mEyeTextToDraw = null;
            recalculate();
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence getText() {
        return this.mText;
    }

    /* access modifiers changed from: package-private */
    public CharSequence getSubText() {
        return this.mSubText;
    }

    /* access modifiers changed from: package-private */
    public CharSequence getEyebrowText() {
        return this.mEyebrowText;
    }

    private void clearTexture() {
        Bitmap bitmap = this.mExpandedTitleTexture;
        if (bitmap != null) {
            bitmap.recycle();
            this.mExpandedTitleTexture = null;
        }
    }

    private static boolean isClose(float value, float targetValue) {
        return Math.abs(value - targetValue) < COMPARE_VALUE;
    }

    /* access modifiers changed from: package-private */
    public ColorStateList getExpandedTextColor() {
        return this.mExpandedTextColor;
    }

    /* access modifiers changed from: package-private */
    public ColorStateList getCollapsedTextColor() {
        return this.mCollapsedTextColor;
    }

    private static int blendColors(int color1, int color2, float ratio) {
        float inverseRatio = 1.0f - ratio;
        return Color.argb((int) ((((float) Color.alpha(color1)) * inverseRatio) + (((float) Color.alpha(color2)) * ratio)), (int) ((((float) Color.red(color1)) * inverseRatio) + (((float) Color.red(color2)) * ratio)), (int) ((((float) Color.green(color1)) * inverseRatio) + (((float) Color.green(color2)) * ratio)), (int) ((((float) Color.blue(color1)) * inverseRatio) + (((float) Color.blue(color2)) * ratio)));
    }

    private static float lerp(float startValue, float endValue, float fraction, Interpolator interpolator) {
        float realFraction = fraction;
        if (interpolator != null) {
            realFraction = interpolator.getInterpolation(fraction);
        }
        return AnimationUtils.lerp(startValue, endValue, realFraction);
    }

    private static boolean rectEquals(Rect r, int left, int top, int right, int bottom) {
        return r.left == left && r.top == top && r.right == right && r.bottom == bottom;
    }

    public void setBubbleCount(int count) {
        if (count <= 0) {
            this.mHwEventBadge = null;
        } else {
            if (this.mHwEventBadge == null) {
                this.mHwEventBadge = new HwEventBadge(this.mView.getContext());
                TypedArray typedArray = this.mView.getContext().getTheme().obtainStyledAttributes(new int[]{16843829});
                this.mHwEventBadge.setBackgoundColor(typedArray.getColor(0, 0));
                typedArray.recycle();
            }
            this.mHwEventBadge.setBadgeCount(count);
        }
        recalculate();
    }

    private int getBadgeWidth() {
        HwEventBadge hwEventBadge = this.mHwEventBadge;
        if (hwEventBadge != null) {
            return hwEventBadge.getBounds().right - this.mHwEventBadge.getBounds().left;
        }
        return 0;
    }

    private void setExpandedTextBaselineY(float expandedDrawY) {
        if (isEquals(this.mExpandedTextBaselineY, -1.0f)) {
            this.mExpandedTextBaselineY = expandedDrawY;
        }
    }

    /* access modifiers changed from: package-private */
    public void setCustomView(ViewGroup customView) {
        this.mCustomView = customView;
    }

    /* access modifiers changed from: package-private */
    public void setCustomViewSmoothScaleEnabled(boolean isSmoothScaleEnabled) {
        this.mIsSmoothScaleEnabled = isSmoothScaleEnabled;
    }

    /* access modifiers changed from: package-private */
    public void setCollapsedSubTextSize(float subTextSize) {
        this.mCollapsedSubTextSize = subTextSize;
    }

    /* access modifiers changed from: package-private */
    public void setSubTextBehavior(int behaviorFlag) {
        this.mSubTextBehaviorFlag = behaviorFlag;
    }

    /* access modifiers changed from: package-private */
    public int getSubTextBehavior() {
        return this.mSubTextBehaviorFlag;
    }
}

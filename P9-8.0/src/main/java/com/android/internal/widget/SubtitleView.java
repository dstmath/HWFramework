package com.android.internal.widget;

import android.R;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.LogException;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.accessibility.CaptioningManager.CaptionStyle;

public class SubtitleView extends View {
    private static final int COLOR_BEVEL_DARK = Integer.MIN_VALUE;
    private static final int COLOR_BEVEL_LIGHT = -2130706433;
    private static final float INNER_PADDING_RATIO = 0.125f;
    private Alignment mAlignment;
    private int mBackgroundColor;
    private final float mCornerRadius;
    private int mEdgeColor;
    private int mEdgeType;
    private int mForegroundColor;
    private boolean mHasMeasurements;
    private int mInnerPaddingX;
    private int mLastMeasuredWidth;
    private StaticLayout mLayout;
    private final RectF mLineBounds;
    private final float mOutlineWidth;
    private Paint mPaint;
    private final float mShadowOffsetX;
    private final float mShadowOffsetY;
    private final float mShadowRadius;
    private float mSpacingAdd;
    private float mSpacingMult;
    private final SpannableStringBuilder mText;
    private TextPaint mTextPaint;

    public SubtitleView(Context context) {
        this(context, null);
    }

    public SubtitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SubtitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.mLineBounds = new RectF();
        this.mText = new SpannableStringBuilder();
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mInnerPaddingX = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextView, defStyleAttr, defStyleRes);
        CharSequence text = LogException.NO_VALUE;
        int textSize = 15;
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 0:
                    textSize = a.getDimensionPixelSize(attr, textSize);
                    break;
                case 18:
                    text = a.getText(attr);
                    break;
                case 53:
                    this.mSpacingAdd = (float) a.getDimensionPixelSize(attr, (int) this.mSpacingAdd);
                    break;
                case 54:
                    this.mSpacingMult = a.getFloat(attr, this.mSpacingMult);
                    break;
                default:
                    break;
            }
        }
        Resources res = getContext().getResources();
        this.mCornerRadius = (float) res.getDimensionPixelSize(com.android.internal.R.dimen.subtitle_corner_radius);
        this.mOutlineWidth = (float) res.getDimensionPixelSize(com.android.internal.R.dimen.subtitle_outline_width);
        this.mShadowRadius = (float) res.getDimensionPixelSize(com.android.internal.R.dimen.subtitle_shadow_radius);
        this.mShadowOffsetX = (float) res.getDimensionPixelSize(com.android.internal.R.dimen.subtitle_shadow_offset);
        this.mShadowOffsetY = this.mShadowOffsetX;
        this.mTextPaint = new TextPaint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setSubpixelText(true);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        setText(text);
        setTextSize((float) textSize);
    }

    public void setText(int resId) {
        setText(getContext().getText(resId));
    }

    public void setText(CharSequence text) {
        this.mText.clear();
        this.mText.append(text);
        this.mHasMeasurements = false;
        requestLayout();
        invalidate();
    }

    public void setForegroundColor(int color) {
        this.mForegroundColor = color;
        invalidate();
    }

    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
        invalidate();
    }

    public void setEdgeType(int edgeType) {
        this.mEdgeType = edgeType;
        invalidate();
    }

    public void setEdgeColor(int color) {
        this.mEdgeColor = color;
        invalidate();
    }

    public void setTextSize(float size) {
        if (this.mTextPaint.getTextSize() != size) {
            this.mTextPaint.setTextSize(size);
            this.mInnerPaddingX = (int) ((INNER_PADDING_RATIO * size) + 0.5f);
            this.mHasMeasurements = false;
            requestLayout();
            invalidate();
        }
    }

    public void setTypeface(Typeface typeface) {
        if (this.mTextPaint.getTypeface() != typeface) {
            this.mTextPaint.setTypeface(typeface);
            this.mHasMeasurements = false;
            requestLayout();
            invalidate();
        }
    }

    public void setAlignment(Alignment textAlignment) {
        if (this.mAlignment != textAlignment) {
            this.mAlignment = textAlignment;
            this.mHasMeasurements = false;
            requestLayout();
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (computeMeasurements(MeasureSpec.getSize(widthMeasureSpec))) {
            StaticLayout layout = this.mLayout;
            -wrap3(layout.getWidth() + ((this.mPaddingLeft + this.mPaddingRight) + (this.mInnerPaddingX * 2)), (layout.getHeight() + this.mPaddingTop) + this.mPaddingBottom);
            return;
        }
        -wrap3(16777216, 16777216);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        computeMeasurements(r - l);
    }

    private boolean computeMeasurements(int maxWidth) {
        if (this.mHasMeasurements && maxWidth == this.mLastMeasuredWidth) {
            return true;
        }
        maxWidth -= (this.mPaddingLeft + this.mPaddingRight) + (this.mInnerPaddingX * 2);
        if (maxWidth <= 0) {
            return false;
        }
        this.mHasMeasurements = true;
        this.mLastMeasuredWidth = maxWidth;
        this.mLayout = new StaticLayout(this.mText, this.mTextPaint, maxWidth, this.mAlignment, this.mSpacingMult, this.mSpacingAdd, true);
        return true;
    }

    public void setStyle(int styleId) {
        CaptionStyle style;
        ContentResolver cr = this.mContext.getContentResolver();
        if (styleId == -1) {
            style = CaptionStyle.getCustomStyle(cr);
        } else {
            style = CaptionStyle.PRESETS[styleId];
        }
        CaptionStyle defStyle = CaptionStyle.DEFAULT;
        this.mForegroundColor = style.hasForegroundColor() ? style.foregroundColor : defStyle.foregroundColor;
        this.mBackgroundColor = style.hasBackgroundColor() ? style.backgroundColor : defStyle.backgroundColor;
        this.mEdgeType = style.hasEdgeType() ? style.edgeType : defStyle.edgeType;
        this.mEdgeColor = style.hasEdgeColor() ? style.edgeColor : defStyle.edgeColor;
        this.mHasMeasurements = false;
        setTypeface(style.getTypeface());
        requestLayout();
    }

    protected void onDraw(Canvas c) {
        StaticLayout layout = this.mLayout;
        if (layout != null) {
            int i;
            int saveCount = c.save();
            int innerPaddingX = this.mInnerPaddingX;
            c.translate((float) (this.mPaddingLeft + innerPaddingX), (float) this.mPaddingTop);
            int lineCount = layout.getLineCount();
            Paint textPaint = this.mTextPaint;
            Paint paint = this.mPaint;
            RectF bounds = this.mLineBounds;
            if (Color.alpha(this.mBackgroundColor) > 0) {
                float cornerRadius = this.mCornerRadius;
                float previousBottom = (float) layout.getLineTop(0);
                paint.setColor(this.mBackgroundColor);
                paint.setStyle(Style.FILL);
                for (i = 0; i < lineCount; i++) {
                    bounds.left = layout.getLineLeft(i) - ((float) innerPaddingX);
                    bounds.right = layout.getLineRight(i) + ((float) innerPaddingX);
                    bounds.top = previousBottom;
                    bounds.bottom = (float) layout.getLineBottom(i);
                    previousBottom = bounds.bottom;
                    c.drawRoundRect(bounds, cornerRadius, cornerRadius, paint);
                }
            }
            int edgeType = this.mEdgeType;
            if (edgeType == 1) {
                textPaint.setStrokeJoin(Join.ROUND);
                textPaint.setStrokeWidth(this.mOutlineWidth);
                textPaint.setColor(this.mEdgeColor);
                textPaint.setStyle(Style.FILL_AND_STROKE);
                for (i = 0; i < lineCount; i++) {
                    layout.drawText(c, i, i);
                }
            } else if (edgeType == 2) {
                textPaint.setShadowLayer(this.mShadowRadius, this.mShadowOffsetX, this.mShadowOffsetY, this.mEdgeColor);
            } else if (edgeType == 3 || edgeType == 4) {
                boolean raised = edgeType == 3;
                int colorUp = raised ? -1 : this.mEdgeColor;
                int colorDown = raised ? this.mEdgeColor : -1;
                float offset = this.mShadowRadius / 2.0f;
                textPaint.setColor(this.mForegroundColor);
                textPaint.setStyle(Style.FILL);
                textPaint.setShadowLayer(this.mShadowRadius, -offset, -offset, colorUp);
                for (i = 0; i < lineCount; i++) {
                    layout.drawText(c, i, i);
                }
                textPaint.setShadowLayer(this.mShadowRadius, offset, offset, colorDown);
            }
            textPaint.setColor(this.mForegroundColor);
            textPaint.setStyle(Style.FILL);
            for (i = 0; i < lineCount; i++) {
                layout.drawText(c, i, i);
            }
            textPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            c.restoreToCount(saveCount);
        }
    }
}

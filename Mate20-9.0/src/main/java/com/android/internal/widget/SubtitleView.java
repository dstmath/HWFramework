package com.android.internal.widget;

import android.R;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.CaptioningManager;
import com.android.internal.util.AsyncService;

public class SubtitleView extends View {
    private static final int COLOR_BEVEL_DARK = Integer.MIN_VALUE;
    private static final int COLOR_BEVEL_LIGHT = -2130706433;
    private static final float INNER_PADDING_RATIO = 0.125f;
    private Layout.Alignment mAlignment;
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
        CharSequence text = "";
        int textSize = 15;
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == 0) {
                textSize = a.getDimensionPixelSize(attr, textSize);
            } else if (attr != 18) {
                switch (attr) {
                    case 53:
                        this.mSpacingAdd = (float) a.getDimensionPixelSize(attr, (int) this.mSpacingAdd);
                        break;
                    case 54:
                        this.mSpacingMult = a.getFloat(attr, this.mSpacingMult);
                        break;
                }
            } else {
                text = a.getText(attr);
            }
        }
        Resources res = getContext().getResources();
        this.mCornerRadius = (float) res.getDimensionPixelSize(17105322);
        this.mOutlineWidth = (float) res.getDimensionPixelSize(17105323);
        this.mShadowRadius = (float) res.getDimensionPixelSize(17105325);
        this.mShadowOffsetX = (float) res.getDimensionPixelSize(17105324);
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

    public void setAlignment(Layout.Alignment textAlignment) {
        if (this.mAlignment != textAlignment) {
            this.mAlignment = textAlignment;
            this.mHasMeasurements = false;
            requestLayout();
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (computeMeasurements(View.MeasureSpec.getSize(widthMeasureSpec))) {
            StaticLayout layout = this.mLayout;
            setMeasuredDimension(layout.getWidth() + this.mPaddingLeft + this.mPaddingRight + (this.mInnerPaddingX * 2), layout.getHeight() + this.mPaddingTop + this.mPaddingBottom);
            return;
        }
        setMeasuredDimension(AsyncService.CMD_ASYNC_SERVICE_DESTROY, AsyncService.CMD_ASYNC_SERVICE_DESTROY);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        computeMeasurements(r - l);
    }

    private boolean computeMeasurements(int maxWidth) {
        if (this.mHasMeasurements && maxWidth == this.mLastMeasuredWidth) {
            return true;
        }
        int maxWidth2 = maxWidth - ((this.mPaddingLeft + this.mPaddingRight) + (this.mInnerPaddingX * 2));
        if (maxWidth2 <= 0) {
            return false;
        }
        this.mHasMeasurements = true;
        this.mLastMeasuredWidth = maxWidth2;
        this.mLayout = StaticLayout.Builder.obtain(this.mText, 0, this.mText.length(), this.mTextPaint, maxWidth2).setAlignment(this.mAlignment).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setUseLineSpacingFromFallbacks(true).build();
        return true;
    }

    public void setStyle(int styleId) {
        CaptioningManager.CaptionStyle style;
        ContentResolver cr = this.mContext.getContentResolver();
        if (styleId == -1) {
            style = CaptioningManager.CaptionStyle.getCustomStyle(cr);
        } else {
            style = CaptioningManager.CaptionStyle.PRESETS[styleId];
        }
        CaptioningManager.CaptionStyle defStyle = CaptioningManager.CaptionStyle.DEFAULT;
        this.mForegroundColor = style.hasForegroundColor() ? style.foregroundColor : defStyle.foregroundColor;
        this.mBackgroundColor = style.hasBackgroundColor() ? style.backgroundColor : defStyle.backgroundColor;
        this.mEdgeType = style.hasEdgeType() ? style.edgeType : defStyle.edgeType;
        this.mEdgeColor = style.hasEdgeColor() ? style.edgeColor : defStyle.edgeColor;
        this.mHasMeasurements = false;
        setTypeface(style.getTypeface());
        requestLayout();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00de A[LOOP:3: B:34:0x00dc->B:35:0x00de, LOOP_END] */
    public void onDraw(Canvas c) {
        int i;
        Canvas canvas = c;
        StaticLayout layout = this.mLayout;
        if (layout != null) {
            int saveCount = c.save();
            int innerPaddingX = this.mInnerPaddingX;
            canvas.translate((float) (this.mPaddingLeft + innerPaddingX), (float) this.mPaddingTop);
            int lineCount = layout.getLineCount();
            Paint textPaint = this.mTextPaint;
            Paint paint = this.mPaint;
            RectF bounds = this.mLineBounds;
            if (Color.alpha(this.mBackgroundColor) > 0) {
                float cornerRadius = this.mCornerRadius;
                paint.setColor(this.mBackgroundColor);
                paint.setStyle(Paint.Style.FILL);
                float previousBottom = (float) layout.getLineTop(0);
                for (int i2 = 0; i2 < lineCount; i2++) {
                    bounds.left = layout.getLineLeft(i2) - ((float) innerPaddingX);
                    bounds.right = layout.getLineRight(i2) + ((float) innerPaddingX);
                    bounds.top = previousBottom;
                    bounds.bottom = (float) layout.getLineBottom(i2);
                    previousBottom = bounds.bottom;
                    canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint);
                }
            }
            int edgeType = this.mEdgeType;
            boolean raised = true;
            if (edgeType == 1) {
                textPaint.setStrokeJoin(Paint.Join.ROUND);
                textPaint.setStrokeWidth(this.mOutlineWidth);
                textPaint.setColor(this.mEdgeColor);
                textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                for (int i3 = 0; i3 < lineCount; i3++) {
                    layout.drawText(canvas, i3, i3);
                }
            } else if (edgeType == 2) {
                textPaint.setShadowLayer(this.mShadowRadius, this.mShadowOffsetX, this.mShadowOffsetY, this.mEdgeColor);
            } else if (edgeType == 3 || edgeType == 4) {
                if (edgeType != 3) {
                    raised = false;
                }
                int colorDown = -1;
                int colorUp = raised ? -1 : this.mEdgeColor;
                if (raised) {
                    colorDown = this.mEdgeColor;
                }
                float offset = this.mShadowRadius / 2.0f;
                textPaint.setColor(this.mForegroundColor);
                textPaint.setStyle(Paint.Style.FILL);
                int i4 = innerPaddingX;
                textPaint.setShadowLayer(this.mShadowRadius, -offset, -offset, colorUp);
                for (int i5 = 0; i5 < lineCount; i5++) {
                    layout.drawText(canvas, i5, i5);
                }
                textPaint.setShadowLayer(this.mShadowRadius, offset, offset, colorDown);
                textPaint.setColor(this.mForegroundColor);
                textPaint.setStyle(Paint.Style.FILL);
                for (i = 0; i < lineCount; i++) {
                    layout.drawText(canvas, i, i);
                }
                textPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                canvas.restoreToCount(saveCount);
            }
            textPaint.setColor(this.mForegroundColor);
            textPaint.setStyle(Paint.Style.FILL);
            while (i < lineCount) {
            }
            textPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            canvas.restoreToCount(saveCount);
        }
    }
}

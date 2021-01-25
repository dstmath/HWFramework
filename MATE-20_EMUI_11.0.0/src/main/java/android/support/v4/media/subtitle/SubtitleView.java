package android.support.v4.media.subtitle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.RequiresApi;
import android.support.mediacompat.R;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

@RequiresApi(28)
class SubtitleView extends View {
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

    SubtitleView(Context context) {
        this(context, null);
    }

    SubtitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    SubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    SubtitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs);
        this.mLineBounds = new RectF();
        this.mText = new SpannableStringBuilder();
        this.mSpacingMult = 1.0f;
        this.mSpacingAdd = 0.0f;
        this.mInnerPaddingX = 0;
        Resources res = getContext().getResources();
        this.mCornerRadius = (float) res.getDimensionPixelSize(R.dimen.subtitle_corner_radius);
        this.mOutlineWidth = (float) res.getDimensionPixelSize(R.dimen.subtitle_outline_width);
        this.mShadowRadius = (float) res.getDimensionPixelSize(R.dimen.subtitle_shadow_radius);
        this.mShadowOffsetX = (float) res.getDimensionPixelSize(R.dimen.subtitle_shadow_offset);
        this.mShadowOffsetY = this.mShadowOffsetX;
        this.mTextPaint = new TextPaint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setSubpixelText(true);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
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

    @Override // android.view.View
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
        if (typeface != null && !typeface.equals(this.mTextPaint.getTypeface())) {
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
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (computeMeasurements(View.MeasureSpec.getSize(widthMeasureSpec))) {
            StaticLayout layout = this.mLayout;
            setMeasuredDimension(layout.getWidth() + getPaddingLeft() + getPaddingRight() + (this.mInnerPaddingX * 2), layout.getHeight() + getPaddingTop() + getPaddingBottom());
            return;
        }
        setMeasuredDimension(16777216, 16777216);
    }

    @Override // android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        computeMeasurements(r - l);
    }

    private boolean computeMeasurements(int maxWidth) {
        if (this.mHasMeasurements && maxWidth == this.mLastMeasuredWidth) {
            return true;
        }
        int maxWidth2 = maxWidth - ((getPaddingLeft() + getPaddingRight()) + (this.mInnerPaddingX * 2));
        if (maxWidth2 <= 0) {
            return false;
        }
        this.mHasMeasurements = true;
        this.mLastMeasuredWidth = maxWidth2;
        this.mLayout = StaticLayout.Builder.obtain(this.mText, 0, this.mText.length(), this.mTextPaint, maxWidth2).setAlignment(this.mAlignment).setLineSpacing(this.mSpacingAdd, this.mSpacingMult).setUseLineSpacingFromFallbacks(true).build();
        return true;
    }

    /* JADX INFO: Multiple debug info for r9v2 int: [D('edgeType' int), D('cornerRadius' float)] */
    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas c) {
        StaticLayout layout = this.mLayout;
        if (layout != null) {
            int saveCount = c.save();
            int innerPaddingX = this.mInnerPaddingX;
            c.translate((float) (getPaddingLeft() + innerPaddingX), (float) getPaddingTop());
            int lineCount = layout.getLineCount();
            Paint textPaint = this.mTextPaint;
            Paint paint = this.mPaint;
            RectF bounds = this.mLineBounds;
            if (Color.alpha(this.mBackgroundColor) > 0) {
                float cornerRadius = this.mCornerRadius;
                paint.setColor(this.mBackgroundColor);
                paint.setStyle(Paint.Style.FILL);
                float previousBottom = (float) layout.getLineTop(0);
                for (int i = 0; i < lineCount; i++) {
                    bounds.left = layout.getLineLeft(i) - ((float) innerPaddingX);
                    bounds.right = layout.getLineRight(i) + ((float) innerPaddingX);
                    bounds.top = previousBottom;
                    bounds.bottom = (float) layout.getLineBottom(i);
                    previousBottom = bounds.bottom;
                    c.drawRoundRect(bounds, cornerRadius, cornerRadius, paint);
                }
            }
            int edgeType = this.mEdgeType;
            boolean raised = true;
            if (edgeType == 1) {
                textPaint.setStrokeJoin(Paint.Join.ROUND);
                textPaint.setStrokeWidth(this.mOutlineWidth);
                textPaint.setColor(this.mEdgeColor);
                textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                layout.draw(c);
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
                textPaint.setShadowLayer(this.mShadowRadius, -offset, -offset, colorUp);
                layout.draw(c);
                textPaint.setShadowLayer(this.mShadowRadius, offset, offset, colorDown);
                textPaint.setColor(this.mForegroundColor);
                textPaint.setStyle(Paint.Style.FILL);
                layout.draw(c);
                textPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
                c.restoreToCount(saveCount);
            }
            textPaint.setColor(this.mForegroundColor);
            textPaint.setStyle(Paint.Style.FILL);
            layout.draw(c);
            textPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            c.restoreToCount(saveCount);
        }
    }
}

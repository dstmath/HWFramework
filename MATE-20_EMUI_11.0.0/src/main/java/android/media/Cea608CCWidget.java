package android.media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Cea608CCParser;
import android.media.ClosedCaptionWidget;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.CaptioningManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;

/* compiled from: ClosedCaptionRenderer */
class Cea608CCWidget extends ClosedCaptionWidget implements Cea608CCParser.DisplayListener {
    private static final String mDummyText = "1234567890123456789012345678901234";
    private static final Rect mTextBounds = new Rect();

    public Cea608CCWidget(Context context) {
        this(context, null);
    }

    public Cea608CCWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Cea608CCWidget(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public Cea608CCWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override // android.media.ClosedCaptionWidget
    public ClosedCaptionWidget.ClosedCaptionLayout createCaptionLayout(Context context) {
        return new CCLayout(context);
    }

    @Override // android.media.Cea608CCParser.DisplayListener
    public void onDisplayChanged(SpannableStringBuilder[] styledTexts) {
        ((CCLayout) this.mClosedCaptionLayout).update(styledTexts);
        if (this.mListener != null) {
            this.mListener.onChanged(this);
        }
    }

    @Override // android.media.Cea608CCParser.DisplayListener
    public CaptioningManager.CaptionStyle getCaptionStyle() {
        return this.mCaptionStyle;
    }

    /* access modifiers changed from: private */
    /* compiled from: ClosedCaptionRenderer */
    public static class CCLineBox extends TextView {
        private static final float EDGE_OUTLINE_RATIO = 0.1f;
        private static final float EDGE_SHADOW_RATIO = 0.05f;
        private static final float FONT_PADDING_RATIO = 0.75f;
        private int mBgColor = -16777216;
        private int mEdgeColor = 0;
        private int mEdgeType = 0;
        private float mOutlineWidth;
        private float mShadowOffset;
        private float mShadowRadius;
        private int mTextColor = -1;

        CCLineBox(Context context) {
            super(context);
            setGravity(17);
            setBackgroundColor(0);
            setTextColor(-1);
            setTypeface(Typeface.MONOSPACE);
            setVisibility(4);
            Resources res = getContext().getResources();
            this.mOutlineWidth = (float) res.getDimensionPixelSize(R.dimen.subtitle_outline_width);
            this.mShadowRadius = (float) res.getDimensionPixelSize(R.dimen.subtitle_shadow_radius);
            this.mShadowOffset = (float) res.getDimensionPixelSize(R.dimen.subtitle_shadow_offset);
        }

        /* access modifiers changed from: package-private */
        public void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle) {
            this.mTextColor = captionStyle.foregroundColor;
            this.mBgColor = captionStyle.backgroundColor;
            this.mEdgeType = captionStyle.edgeType;
            this.mEdgeColor = captionStyle.edgeColor;
            setTextColor(this.mTextColor);
            if (this.mEdgeType == 2) {
                float f = this.mShadowRadius;
                float f2 = this.mShadowOffset;
                setShadowLayer(f, f2, f2, this.mEdgeColor);
            } else {
                setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            }
            invalidate();
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.TextView, android.view.View
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float fontSize = ((float) View.MeasureSpec.getSize(heightMeasureSpec)) * FONT_PADDING_RATIO;
            setTextSize(0, fontSize);
            this.mOutlineWidth = (EDGE_OUTLINE_RATIO * fontSize) + 1.0f;
            this.mShadowRadius = (EDGE_SHADOW_RATIO * fontSize) + 1.0f;
            this.mShadowOffset = this.mShadowRadius;
            setScaleX(1.0f);
            getPaint().getTextBounds(Cea608CCWidget.mDummyText, 0, Cea608CCWidget.mDummyText.length(), Cea608CCWidget.mTextBounds);
            setScaleX(((float) View.MeasureSpec.getSize(widthMeasureSpec)) / ((float) Cea608CCWidget.mTextBounds.width()));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.TextView, android.view.View
        public void onDraw(Canvas c) {
            int i = this.mEdgeType;
            if (i == -1 || i == 0 || i == 2) {
                super.onDraw(c);
            } else if (i == 1) {
                drawEdgeOutline(c);
            } else {
                drawEdgeRaisedOrDepressed(c);
            }
        }

        private void drawEdgeOutline(Canvas c) {
            TextPaint textPaint = getPaint();
            Paint.Style previousStyle = textPaint.getStyle();
            Paint.Join previousJoin = textPaint.getStrokeJoin();
            float previousWidth = textPaint.getStrokeWidth();
            setTextColor(this.mEdgeColor);
            textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            textPaint.setStrokeJoin(Paint.Join.ROUND);
            textPaint.setStrokeWidth(this.mOutlineWidth);
            super.onDraw(c);
            setTextColor(this.mTextColor);
            textPaint.setStyle(previousStyle);
            textPaint.setStrokeJoin(previousJoin);
            textPaint.setStrokeWidth(previousWidth);
            setBackgroundSpans(0);
            super.onDraw(c);
            setBackgroundSpans(this.mBgColor);
        }

        private void drawEdgeRaisedOrDepressed(Canvas c) {
            TextPaint textPaint = getPaint();
            Paint.Style previousStyle = textPaint.getStyle();
            textPaint.setStyle(Paint.Style.FILL);
            boolean raised = this.mEdgeType == 3;
            int colorDown = -1;
            int colorUp = raised ? -1 : this.mEdgeColor;
            if (raised) {
                colorDown = this.mEdgeColor;
            }
            float f = this.mShadowRadius;
            float offset = f / 2.0f;
            setShadowLayer(f, -offset, -offset, colorUp);
            super.onDraw(c);
            setBackgroundSpans(0);
            setShadowLayer(this.mShadowRadius, offset, offset, colorDown);
            super.onDraw(c);
            textPaint.setStyle(previousStyle);
            setBackgroundSpans(this.mBgColor);
        }

        private void setBackgroundSpans(int color) {
            Cea608CCParser.MutableBackgroundColorSpan[] bgSpans;
            CharSequence text = getText();
            if (text instanceof Spannable) {
                Spannable spannable = (Spannable) text;
                for (Cea608CCParser.MutableBackgroundColorSpan mutableBackgroundColorSpan : (Cea608CCParser.MutableBackgroundColorSpan[]) spannable.getSpans(0, spannable.length(), Cea608CCParser.MutableBackgroundColorSpan.class)) {
                    mutableBackgroundColorSpan.setBackgroundColor(color);
                }
            }
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class CCLayout extends LinearLayout implements ClosedCaptionWidget.ClosedCaptionLayout {
        private static final int MAX_ROWS = 15;
        private static final float SAFE_AREA_RATIO = 0.9f;
        private final CCLineBox[] mLineBoxes = new CCLineBox[15];

        CCLayout(Context context) {
            super(context);
            setGravity(Gravity.START);
            setOrientation(1);
            for (int i = 0; i < 15; i++) {
                this.mLineBoxes[i] = new CCLineBox(getContext());
                addView(this.mLineBoxes[i], -2, -2);
            }
        }

        @Override // android.media.ClosedCaptionWidget.ClosedCaptionLayout
        public void setCaptionStyle(CaptioningManager.CaptionStyle captionStyle) {
            for (int i = 0; i < 15; i++) {
                this.mLineBoxes[i].setCaptionStyle(captionStyle);
            }
        }

        @Override // android.media.ClosedCaptionWidget.ClosedCaptionLayout
        public void setFontScale(float fontScale) {
        }

        /* access modifiers changed from: package-private */
        public void update(SpannableStringBuilder[] textBuffer) {
            for (int i = 0; i < 15; i++) {
                if (textBuffer[i] != null) {
                    this.mLineBoxes[i].setText(textBuffer[i], TextView.BufferType.SPANNABLE);
                    this.mLineBoxes[i].setVisibility(0);
                } else {
                    this.mLineBoxes[i].setVisibility(4);
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.LinearLayout, android.view.View
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int safeWidth = getMeasuredWidth();
            int safeHeight = getMeasuredHeight();
            if (safeWidth * 3 >= safeHeight * 4) {
                safeWidth = (safeHeight * 4) / 3;
            } else {
                safeHeight = (safeWidth * 3) / 4;
            }
            int lineHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(((int) (((float) safeHeight) * 0.9f)) / 15, 1073741824);
            int lineWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) (((float) safeWidth) * 0.9f), 1073741824);
            for (int i = 0; i < 15; i++) {
                this.mLineBoxes[i].measure(lineWidthMeasureSpec, lineHeightMeasureSpec);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
        public void onLayout(boolean changed, int l, int t, int r, int b) {
            int safeHeight;
            int safeWidth;
            int viewPortWidth = r - l;
            int viewPortHeight = b - t;
            if (viewPortWidth * 3 >= viewPortHeight * 4) {
                safeWidth = (viewPortHeight * 4) / 3;
                safeHeight = viewPortHeight;
            } else {
                safeWidth = viewPortWidth;
                safeHeight = (viewPortWidth * 3) / 4;
            }
            int safeWidth2 = (int) (((float) safeWidth) * 0.9f);
            int safeHeight2 = (int) (((float) safeHeight) * 0.9f);
            int left = (viewPortWidth - safeWidth2) / 2;
            int top = (viewPortHeight - safeHeight2) / 2;
            for (int i = 0; i < 15; i++) {
                this.mLineBoxes[i].layout(left, ((safeHeight2 * i) / 15) + top, left + safeWidth2, (((i + 1) * safeHeight2) / 15) + top);
            }
        }
    }
}

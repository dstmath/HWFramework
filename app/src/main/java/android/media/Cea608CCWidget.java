package android.media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.Cea608CCParser.MutableBackgroundColorSpan;
import android.security.keymaster.KeymasterDefs;
import android.speech.tts.TextToSpeech.Engine;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.accessibility.CaptioningManager.CaptionStyle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/* compiled from: ClosedCaptionRenderer */
class Cea608CCWidget extends ClosedCaptionWidget implements DisplayListener {
    private static final String mDummyText = "1234567890123456789012345678901234";
    private static final Rect mTextBounds = null;

    /* compiled from: ClosedCaptionRenderer */
    private static class CCLayout extends LinearLayout implements ClosedCaptionLayout {
        private static final int MAX_ROWS = 15;
        private static final float SAFE_AREA_RATIO = 0.9f;
        private final CCLineBox[] mLineBoxes;

        CCLayout(Context context) {
            super(context);
            this.mLineBoxes = new CCLineBox[MAX_ROWS];
            setGravity(8388611);
            setOrientation(1);
            for (int i = 0; i < MAX_ROWS; i++) {
                this.mLineBoxes[i] = new CCLineBox(getContext());
                addView(this.mLineBoxes[i], -2, -2);
            }
        }

        public void setCaptionStyle(CaptionStyle captionStyle) {
            for (int i = 0; i < MAX_ROWS; i++) {
                this.mLineBoxes[i].setCaptionStyle(captionStyle);
            }
        }

        public void setFontScale(float fontScale) {
        }

        void update(SpannableStringBuilder[] textBuffer) {
            for (int i = 0; i < MAX_ROWS; i++) {
                if (textBuffer[i] != null) {
                    this.mLineBoxes[i].setText(textBuffer[i], BufferType.SPANNABLE);
                    this.mLineBoxes[i].setVisibility(0);
                } else {
                    this.mLineBoxes[i].setVisibility(4);
                }
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int safeWidth = getMeasuredWidth();
            int safeHeight = getMeasuredHeight();
            if (safeWidth * 3 >= safeHeight * 4) {
                safeWidth = (safeHeight * 4) / 3;
            } else {
                safeHeight = (safeWidth * 3) / 4;
            }
            safeWidth = (int) (((float) safeWidth) * SAFE_AREA_RATIO);
            int lineHeightMeasureSpec = MeasureSpec.makeMeasureSpec(((int) (((float) safeHeight) * SAFE_AREA_RATIO)) / MAX_ROWS, KeymasterDefs.KM_UINT_REP);
            int lineWidthMeasureSpec = MeasureSpec.makeMeasureSpec(safeWidth, KeymasterDefs.KM_UINT_REP);
            for (int i = 0; i < MAX_ROWS; i++) {
                this.mLineBoxes[i].measure(lineWidthMeasureSpec, lineHeightMeasureSpec);
            }
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int safeWidth;
            int safeHeight;
            int viewPortWidth = r - l;
            int viewPortHeight = b - t;
            if (viewPortWidth * 3 >= viewPortHeight * 4) {
                safeWidth = (viewPortHeight * 4) / 3;
                safeHeight = viewPortHeight;
            } else {
                safeWidth = viewPortWidth;
                safeHeight = (viewPortWidth * 3) / 4;
            }
            safeWidth = (int) (((float) safeWidth) * SAFE_AREA_RATIO);
            safeHeight = (int) (((float) safeHeight) * SAFE_AREA_RATIO);
            int left = (viewPortWidth - safeWidth) / 2;
            int top = (viewPortHeight - safeHeight) / 2;
            for (int i = 0; i < MAX_ROWS; i++) {
                this.mLineBoxes[i].layout(left, ((safeHeight * i) / MAX_ROWS) + top, left + safeWidth, (((i + 1) * safeHeight) / MAX_ROWS) + top);
            }
        }
    }

    /* compiled from: ClosedCaptionRenderer */
    private static class CCLineBox extends TextView {
        private static final float EDGE_OUTLINE_RATIO = 0.1f;
        private static final float EDGE_SHADOW_RATIO = 0.05f;
        private static final float FONT_PADDING_RATIO = 0.75f;
        private int mBgColor;
        private int mEdgeColor;
        private int mEdgeType;
        private float mOutlineWidth;
        private float mShadowOffset;
        private float mShadowRadius;
        private int mTextColor;

        CCLineBox(Context context) {
            super(context);
            this.mTextColor = -1;
            this.mBgColor = Color.BLACK;
            this.mEdgeType = 0;
            this.mEdgeColor = 0;
            setGravity(17);
            setBackgroundColor(0);
            setTextColor(-1);
            setTypeface(Typeface.MONOSPACE);
            setVisibility(4);
            Resources res = getContext().getResources();
            this.mOutlineWidth = (float) res.getDimensionPixelSize(17105046);
            this.mShadowRadius = (float) res.getDimensionPixelSize(17105044);
            this.mShadowOffset = (float) res.getDimensionPixelSize(17105045);
        }

        void setCaptionStyle(CaptionStyle captionStyle) {
            this.mTextColor = captionStyle.foregroundColor;
            this.mBgColor = captionStyle.backgroundColor;
            this.mEdgeType = captionStyle.edgeType;
            this.mEdgeColor = captionStyle.edgeColor;
            setTextColor(this.mTextColor);
            if (this.mEdgeType == 2) {
                setShadowLayer(this.mShadowRadius, this.mShadowOffset, this.mShadowOffset, this.mEdgeColor);
            } else {
                setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            }
            invalidate();
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            float fontSize = ((float) MeasureSpec.getSize(heightMeasureSpec)) * FONT_PADDING_RATIO;
            setTextSize(0, fontSize);
            this.mOutlineWidth = (EDGE_OUTLINE_RATIO * fontSize) + Engine.DEFAULT_VOLUME;
            this.mShadowRadius = (EDGE_SHADOW_RATIO * fontSize) + Engine.DEFAULT_VOLUME;
            this.mShadowOffset = this.mShadowRadius;
            setScaleX(Engine.DEFAULT_VOLUME);
            getPaint().getTextBounds(Cea608CCWidget.mDummyText, 0, Cea608CCWidget.mDummyText.length(), Cea608CCWidget.mTextBounds);
            setScaleX(((float) MeasureSpec.getSize(widthMeasureSpec)) / ((float) Cea608CCWidget.mTextBounds.width()));
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        protected void onDraw(Canvas c) {
            if (this.mEdgeType == -1 || this.mEdgeType == 0 || this.mEdgeType == 2) {
                super.onDraw(c);
                return;
            }
            if (this.mEdgeType == 1) {
                drawEdgeOutline(c);
            } else {
                drawEdgeRaisedOrDepressed(c);
            }
        }

        private void drawEdgeOutline(Canvas c) {
            TextPaint textPaint = getPaint();
            Style previousStyle = textPaint.getStyle();
            Join previousJoin = textPaint.getStrokeJoin();
            float previousWidth = textPaint.getStrokeWidth();
            setTextColor(this.mEdgeColor);
            textPaint.setStyle(Style.FILL_AND_STROKE);
            textPaint.setStrokeJoin(Join.ROUND);
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
            boolean raised;
            TextPaint textPaint = getPaint();
            Style previousStyle = textPaint.getStyle();
            textPaint.setStyle(Style.FILL);
            if (this.mEdgeType == 3) {
                raised = true;
            } else {
                raised = false;
            }
            int colorUp = raised ? -1 : this.mEdgeColor;
            int colorDown = raised ? this.mEdgeColor : -1;
            float offset = this.mShadowRadius / 2.0f;
            setShadowLayer(this.mShadowRadius, -offset, -offset, colorUp);
            super.onDraw(c);
            setBackgroundSpans(0);
            setShadowLayer(this.mShadowRadius, offset, offset, colorDown);
            super.onDraw(c);
            textPaint.setStyle(previousStyle);
            setBackgroundSpans(this.mBgColor);
        }

        private void setBackgroundSpans(int color) {
            CharSequence text = getText();
            if (text instanceof Spannable) {
                Spannable spannable = (Spannable) text;
                MutableBackgroundColorSpan[] bgSpans = (MutableBackgroundColorSpan[]) spannable.getSpans(0, spannable.length(), MutableBackgroundColorSpan.class);
                for (MutableBackgroundColorSpan backgroundColor : bgSpans) {
                    backgroundColor.setBackgroundColor(color);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.Cea608CCWidget.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.Cea608CCWidget.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.Cea608CCWidget.<clinit>():void");
    }

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

    public ClosedCaptionLayout createCaptionLayout(Context context) {
        return new CCLayout(context);
    }

    public void onDisplayChanged(SpannableStringBuilder[] styledTexts) {
        ((CCLayout) this.mClosedCaptionLayout).update(styledTexts);
        if (this.mListener != null) {
            this.mListener.onChanged(this);
        }
    }

    public CaptionStyle getCaptionStyle() {
        return this.mCaptionStyle;
    }
}

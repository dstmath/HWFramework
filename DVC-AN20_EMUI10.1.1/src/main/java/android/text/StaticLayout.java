package android.text;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Paint;
import android.graphics.text.LineBreaker;
import android.text.Layout;
import android.text.PrecomputedText;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.util.Pools;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.util.Arrays;

public class StaticLayout extends Layout {
    private static final char CHAR_NEW_LINE = '\n';
    private static final int COLUMNS_ELLIPSIZE = 7;
    private static final int COLUMNS_NORMAL = 5;
    private static final int DEFAULT_MAX_LINE_HEIGHT = -1;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 6;
    @UnsupportedAppUsage
    private static final int ELLIPSIS_START = 5;
    private static final int END_HYPHEN_MASK = 7;
    private static final int EXTRA = 3;
    private static final double EXTRA_ROUNDING = 0.5d;
    private static final int HYPHEN = 4;
    private static final int HYPHEN_MASK = 255;
    private static final int START = 0;
    private static final int START_HYPHEN_BITS_SHIFT = 3;
    private static final int START_HYPHEN_MASK = 24;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final float TAB_INCREMENT = 20.0f;
    private static final int TAB_MASK = 536870912;
    static final String TAG = "StaticLayout";
    private static final int TOP = 1;
    private int mBottomPadding;
    @UnsupportedAppUsage
    private int mColumns;
    private boolean mEllipsized;
    private int mEllipsizedWidth;
    private int[] mLeftIndents;
    @UnsupportedAppUsage
    private int mLineCount;
    @UnsupportedAppUsage
    private Layout.Directions[] mLineDirections;
    @UnsupportedAppUsage
    private int[] mLines;
    private int mMaxLineHeight;
    @UnsupportedAppUsage
    private int mMaximumVisibleLineCount;
    private int[] mRightIndents;
    private int mTopPadding;

    public static final class Builder {
        private static final Pools.SynchronizedPool<Builder> sPool = new Pools.SynchronizedPool<>(3);
        private boolean mAddLastLineLineSpacing;
        private Layout.Alignment mAlignment;
        private int mBreakStrategy;
        private TextUtils.TruncateAt mEllipsize;
        private int mEllipsizedWidth;
        private int mEnd;
        private boolean mFallbackLineSpacing;
        private final Paint.FontMetricsInt mFontMetricsInt = new Paint.FontMetricsInt();
        private int mHyphenationFrequency;
        private boolean mIncludePad;
        private int mJustificationMode;
        private int[] mLeftIndents;
        private int mMaxLines;
        private TextPaint mPaint;
        private int[] mRightIndents;
        private float mSpacingAdd;
        private float mSpacingMult;
        private int mStart;
        private CharSequence mText;
        private TextDirectionHeuristic mTextDir;
        private int mWidth;

        private Builder() {
        }

        public static Builder obtain(CharSequence source, int start, int end, TextPaint paint, int width) {
            Builder b = sPool.acquire();
            if (b == null) {
                b = new Builder();
            }
            b.mText = source;
            b.mStart = start;
            b.mEnd = end;
            b.mPaint = paint;
            b.mWidth = width;
            b.mAlignment = Layout.Alignment.ALIGN_NORMAL;
            b.mTextDir = TextDirectionHeuristics.FIRSTSTRONG_LTR;
            b.mSpacingMult = 1.0f;
            b.mSpacingAdd = 0.0f;
            b.mIncludePad = true;
            b.mFallbackLineSpacing = false;
            b.mEllipsizedWidth = width;
            b.mEllipsize = null;
            b.mMaxLines = Integer.MAX_VALUE;
            b.mBreakStrategy = 0;
            b.mHyphenationFrequency = 0;
            b.mJustificationMode = 0;
            return b;
        }

        /* access modifiers changed from: private */
        public static void recycle(Builder b) {
            b.mPaint = null;
            b.mText = null;
            b.mLeftIndents = null;
            b.mRightIndents = null;
            sPool.release(b);
        }

        /* access modifiers changed from: package-private */
        public void finish() {
            this.mText = null;
            this.mPaint = null;
            this.mLeftIndents = null;
            this.mRightIndents = null;
        }

        public Builder setText(CharSequence source) {
            return setText(source, 0, source.length());
        }

        public Builder setText(CharSequence source, int start, int end) {
            this.mText = source;
            this.mStart = start;
            this.mEnd = end;
            return this;
        }

        public Builder setPaint(TextPaint paint) {
            this.mPaint = paint;
            return this;
        }

        public Builder setWidth(int width) {
            this.mWidth = width;
            if (this.mEllipsize == null) {
                this.mEllipsizedWidth = width;
            }
            return this;
        }

        public Builder setAlignment(Layout.Alignment alignment) {
            this.mAlignment = alignment;
            return this;
        }

        public Builder setTextDirection(TextDirectionHeuristic textDir) {
            this.mTextDir = textDir;
            return this;
        }

        public Builder setLineSpacing(float spacingAdd, float spacingMult) {
            this.mSpacingAdd = spacingAdd;
            this.mSpacingMult = spacingMult;
            return this;
        }

        public Builder setIncludePad(boolean includePad) {
            this.mIncludePad = includePad;
            return this;
        }

        public Builder setUseLineSpacingFromFallbacks(boolean useLineSpacingFromFallbacks) {
            this.mFallbackLineSpacing = useLineSpacingFromFallbacks;
            return this;
        }

        public Builder setEllipsizedWidth(int ellipsizedWidth) {
            this.mEllipsizedWidth = ellipsizedWidth;
            return this;
        }

        public Builder setEllipsize(TextUtils.TruncateAt ellipsize) {
            this.mEllipsize = ellipsize;
            return this;
        }

        public Builder setMaxLines(int maxLines) {
            this.mMaxLines = maxLines;
            return this;
        }

        public Builder setBreakStrategy(int breakStrategy) {
            this.mBreakStrategy = breakStrategy;
            return this;
        }

        public Builder setHyphenationFrequency(int hyphenationFrequency) {
            this.mHyphenationFrequency = hyphenationFrequency;
            return this;
        }

        public Builder setIndents(int[] leftIndents, int[] rightIndents) {
            this.mLeftIndents = leftIndents;
            this.mRightIndents = rightIndents;
            return this;
        }

        public Builder setJustificationMode(int justificationMode) {
            this.mJustificationMode = justificationMode;
            return this;
        }

        /* access modifiers changed from: package-private */
        public Builder setAddLastLineLineSpacing(boolean value) {
            this.mAddLastLineLineSpacing = value;
            return this;
        }

        public StaticLayout build() {
            StaticLayout result = new StaticLayout(this);
            recycle(this);
            return result;
        }
    }

    @Deprecated
    public StaticLayout(CharSequence source, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, 0, source.length(), paint, width, align, spacingmult, spacingadd, includepad);
    }

    @Deprecated
    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, spacingmult, spacingadd, includepad, null, 0);
    }

    @Deprecated
    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
        this(source, bufstart, bufend, paint, outerwidth, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, ellipsize, ellipsizedWidth, Integer.MAX_VALUE);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 117521430)
    @Deprecated
    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth, int maxLines) {
        super(r1, paint, outerwidth, align, textDir, spacingmult, spacingadd);
        Layout.SpannedEllipsizer spannedEllipsizer;
        if (ellipsize == null) {
            spannedEllipsizer = source;
        } else if (source instanceof Spanned) {
            spannedEllipsizer = new Layout.SpannedEllipsizer(source);
        } else {
            spannedEllipsizer = new Layout.Ellipsizer(source);
        }
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        Builder b = Builder.obtain(source, bufstart, bufend, paint, outerwidth).setAlignment(align).setTextDirection(textDir).setLineSpacing(spacingadd, spacingmult).setIncludePad(includepad).setEllipsizedWidth(ellipsizedWidth).setEllipsize(ellipsize).setMaxLines(maxLines);
        if (ellipsize != null) {
            Layout.Ellipsizer e = (Layout.Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            this.mEllipsizedWidth = ellipsizedWidth;
            this.mColumns = 7;
        } else {
            this.mColumns = 5;
            this.mEllipsizedWidth = outerwidth;
        }
        this.mLineDirections = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, 2);
        this.mLines = ArrayUtils.newUnpaddedIntArray(this.mColumns * 2);
        this.mMaximumVisibleLineCount = maxLines;
        generate(b, b.mIncludePad, b.mIncludePad);
        Builder.recycle(b);
    }

    StaticLayout(CharSequence text) {
        super(text, null, 0, null, 0.0f, 0.0f);
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        this.mColumns = 7;
        this.mLineDirections = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, 2);
        this.mLines = ArrayUtils.newUnpaddedIntArray(this.mColumns * 2);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    private StaticLayout(Builder b) {
        super(r3, b.mPaint, b.mWidth, b.mAlignment, b.mTextDir, b.mSpacingMult, b.mSpacingAdd);
        Layout.SpannedEllipsizer spannedEllipsizer;
        if (b.mEllipsize == null) {
            spannedEllipsizer = b.mText;
        } else if (b.mText instanceof Spanned) {
            spannedEllipsizer = new Layout.SpannedEllipsizer(b.mText);
        } else {
            spannedEllipsizer = new Layout.Ellipsizer(b.mText);
        }
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        if (b.mEllipsize != null) {
            Layout.Ellipsizer e = (Layout.Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = b.mEllipsizedWidth;
            e.mMethod = b.mEllipsize;
            this.mEllipsizedWidth = b.mEllipsizedWidth;
            this.mColumns = 7;
        } else {
            this.mColumns = 5;
            this.mEllipsizedWidth = b.mWidth;
        }
        this.mLineDirections = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, 2);
        this.mLines = ArrayUtils.newUnpaddedIntArray(this.mColumns * 2);
        this.mMaximumVisibleLineCount = b.mMaxLines;
        this.mLeftIndents = b.mLeftIndents;
        this.mRightIndents = b.mRightIndents;
        setJustificationMode(b.mJustificationMode);
        generate(b, b.mIncludePad, b.mIncludePad);
    }

    /* JADX INFO: Multiple debug info for r12v6 android.graphics.text.LineBreaker: [D('firstWidthLineCount' int), D('lineBreaker' android.graphics.text.LineBreaker)] */
    /* JADX INFO: Multiple debug info for r2v10 'fm'  android.graphics.Paint$FontMetricsInt: [D('fm' android.graphics.Paint$FontMetricsInt), D('spanStart' int)] */
    /* JADX INFO: Multiple debug info for r1v9 'bufEnd'  int: [D('paraStart' int), D('bufEnd' int)] */
    /* JADX INFO: Multiple debug info for r12v9 android.graphics.Paint$FontMetricsInt: [D('lineBreaker' android.graphics.text.LineBreaker), D('fm' android.graphics.Paint$FontMetricsInt)] */
    /* JADX INFO: Multiple debug info for r22v5 int: [D('fmBottom' int), D('fmDescent' int)] */
    /* JADX INFO: Multiple debug info for r3v22 int[]: [D('paragraphInfo' android.text.PrecomputedText$ParagraphInfo[]), D('breaks' int[])] */
    /* JADX INFO: Multiple debug info for r3v23 float[]: [D('breaks' int[]), D('lineWidths' float[])] */
    /* JADX INFO: Multiple debug info for r3v24 float[]: [D('lineWidths' float[]), D('ascents' float[])] */
    /* JADX INFO: Multiple debug info for r3v25 float[]: [D('descents' float[]), D('ascents' float[])] */
    /* JADX INFO: Multiple debug info for r3v26 boolean[]: [D('descents' float[]), D('hasTabs' boolean[])] */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x03aa  */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x05f6 A[LOOP:2: B:49:0x0172->B:176:0x05f6, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:191:0x05e4 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x02a1  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x02dc A[LOOP:6: B:97:0x02da->B:98:0x02dc, LOOP_END] */
    public void generate(Builder b, boolean includepad, boolean trackpad) {
        int[] indents;
        LineBreaker lineBreaker;
        Paint.FontMetricsInt fm;
        LineBreaker.ParagraphConstraints constraints;
        Spanned spanned;
        PrecomputedText.ParagraphInfo[] paragraphInfo;
        TextDirectionHeuristic textDir;
        TextPaint paint;
        int bufEnd;
        int bufStart;
        CharSequence source;
        StaticLayout staticLayout;
        TextUtils.TruncateAt ellipsize;
        float ellipsizedWidth;
        Paint.FontMetricsInt fm2;
        int[] chooseHtv;
        int v;
        CharSequence source2;
        LineHeightSpan[] chooseHt;
        int[] chooseHtv2;
        CharSequence source3;
        int bufStart2;
        TextPaint paint2;
        TextDirectionHeuristic textDir2;
        int firstWidthLineCount;
        int firstWidth;
        int restWidth;
        float[] variableTabStops;
        LineBreaker.Result res;
        int breakCount;
        int lineBreakCapacity;
        int[] hyphenEdits;
        boolean[] hasTabs;
        float[] descents;
        float[] ascents;
        float[] lineWidths;
        int[] breaks;
        PrecomputedText.ParagraphInfo[] paragraphInfo2;
        int i;
        int remainingLineCount;
        Spanned spanned2;
        LineBreaker.Result res2;
        int breakCount2;
        int spanStart;
        int breakIndex;
        int fmTop;
        int ascent;
        int fmAscent;
        int fmAscent2;
        int fmBottom;
        Paint.FontMetricsInt fm3;
        Spanned spanned3;
        float[] variableTabStops2;
        StaticLayout staticLayout2 = this;
        CharSequence source4 = b.mText;
        int bufStart3 = b.mStart;
        int bufEnd2 = b.mEnd;
        TextPaint paint3 = b.mPaint;
        int outerWidth = b.mWidth;
        TextDirectionHeuristic textDir3 = b.mTextDir;
        boolean fallbackLineSpacing = b.mFallbackLineSpacing;
        float spacingmult = b.mSpacingMult;
        float spacingadd = b.mSpacingAdd;
        float ellipsizedWidth2 = (float) b.mEllipsizedWidth;
        TextUtils.TruncateAt ellipsize2 = b.mEllipsize;
        boolean addLastLineSpacing = b.mAddLastLineLineSpacing;
        int lineBreakCapacity2 = 0;
        int[] breaks2 = null;
        float[] lineWidths2 = null;
        float[] ascents2 = null;
        float[] descents2 = null;
        boolean[] hasTabs2 = null;
        int[] hyphenEdits2 = null;
        staticLayout2.mLineCount = 0;
        staticLayout2.mEllipsized = false;
        staticLayout2.mMaxLineHeight = staticLayout2.mMaximumVisibleLineCount < 1 ? 0 : -1;
        int v2 = 0;
        boolean needMultiply = (spacingmult == 1.0f && spacingadd == 0.0f) ? false : true;
        Paint.FontMetricsInt fm4 = b.mFontMetricsInt;
        if (staticLayout2.mLeftIndents == null && staticLayout2.mRightIndents == null) {
            indents = null;
        } else {
            int[] indents2 = staticLayout2.mLeftIndents;
            int leftLen = indents2 == null ? 0 : indents2.length;
            int[] iArr = staticLayout2.mRightIndents;
            int rightLen = iArr == null ? 0 : iArr.length;
            int[] indents3 = new int[Math.max(leftLen, rightLen)];
            for (int i2 = 0; i2 < leftLen; i2++) {
                indents3[i2] = staticLayout2.mLeftIndents[i2];
            }
            int i3 = 0;
            while (i3 < rightLen) {
                indents3[i3] = indents3[i3] + staticLayout2.mRightIndents[i3];
                i3++;
                leftLen = leftLen;
            }
            indents = indents3;
        }
        LineBreaker lineBreaker2 = new LineBreaker.Builder().setBreakStrategy(b.mBreakStrategy).setHyphenationFrequency(b.mHyphenationFrequency).setJustificationMode(b.mJustificationMode).setIndents(indents).build();
        LineBreaker.ParagraphConstraints constraints2 = new LineBreaker.ParagraphConstraints();
        PrecomputedText.ParagraphInfo[] paragraphInfo3 = null;
        Spanned spanned4 = source4 instanceof Spanned ? (Spanned) source4 : null;
        float ellipsizedWidth3 = ellipsizedWidth2;
        if (source4 instanceof PrecomputedText) {
            PrecomputedText precomputed = (PrecomputedText) source4;
            spanned = spanned4;
            constraints = constraints2;
            fm = fm4;
            lineBreaker = lineBreaker2;
            int checkResult = precomputed.checkResultUsable(bufStart3, bufEnd2, textDir3, paint3, b.mBreakStrategy, b.mHyphenationFrequency);
            if (checkResult != 0) {
                if (checkResult == 1) {
                    paragraphInfo3 = PrecomputedText.create(precomputed, new PrecomputedText.Params.Builder(paint3).setBreakStrategy(b.mBreakStrategy).setHyphenationFrequency(b.mHyphenationFrequency).setTextDirection(textDir3).build()).getParagraphInfo();
                } else if (checkResult == 2) {
                    paragraphInfo3 = precomputed.getParagraphInfo();
                }
            }
        } else {
            spanned = spanned4;
            constraints = constraints2;
            fm = fm4;
            lineBreaker = lineBreaker2;
        }
        if (paragraphInfo3 == null) {
            paragraphInfo = PrecomputedText.createMeasuredParagraphs(source4, new PrecomputedText.Params(paint3, textDir3, b.mBreakStrategy, b.mHyphenationFrequency), bufStart3, bufEnd2, false);
        } else {
            paragraphInfo = paragraphInfo3;
        }
        int paraIndex = 0;
        int[] chooseHtv3 = null;
        while (true) {
            if (paraIndex >= paragraphInfo.length) {
                textDir = textDir3;
                paint = paint3;
                bufEnd = bufEnd2;
                bufStart = bufStart3;
                source = source4;
                staticLayout = staticLayout2;
                ellipsize = ellipsize2;
                ellipsizedWidth = ellipsizedWidth3;
                fm2 = fm;
                chooseHtv = breaks2;
                v = v2;
                break;
            }
            int paraStart = paraIndex == 0 ? bufStart3 : paragraphInfo[paraIndex - 1].paragraphEnd;
            int paraEnd = paragraphInfo[paraIndex].paragraphEnd;
            int firstWidth2 = outerWidth;
            int restWidth2 = outerWidth;
            Spanned spanned5 = spanned;
            if (spanned5 != null) {
                LeadingMarginSpan[] sp = (LeadingMarginSpan[]) getParagraphSpans(spanned5, paraStart, paraEnd, LeadingMarginSpan.class);
                textDir2 = textDir3;
                int firstWidthLineCount2 = 1;
                int i4 = 0;
                while (true) {
                    paint2 = paint3;
                    if (i4 >= sp.length) {
                        break;
                    }
                    LeadingMarginSpan lms = sp[i4];
                    firstWidth2 -= sp[i4].getLeadingMargin(true);
                    restWidth2 -= sp[i4].getLeadingMargin(false);
                    if (lms instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                        firstWidthLineCount2 = Math.max(firstWidthLineCount2, ((LeadingMarginSpan.LeadingMarginSpan2) lms).getLeadingMarginLineCount());
                    }
                    i4++;
                    paint3 = paint2;
                    bufStart3 = bufStart3;
                    source4 = source4;
                }
                bufStart2 = bufStart3;
                source3 = source4;
                LineHeightSpan[] chooseHt2 = (LineHeightSpan[]) getParagraphSpans(spanned5, paraStart, paraEnd, LineHeightSpan.class);
                if (chooseHt2.length == 0) {
                    chooseHtv2 = chooseHtv3;
                    chooseHt = null;
                    firstWidthLineCount = firstWidthLineCount2;
                    firstWidth = firstWidth2;
                    restWidth = restWidth2;
                } else {
                    if (chooseHtv3 == null || chooseHtv3.length < chooseHt2.length) {
                        chooseHtv3 = ArrayUtils.newUnpaddedIntArray(chooseHt2.length);
                    }
                    for (int i5 = 0; i5 < chooseHt2.length; i5++) {
                        int o = spanned5.getSpanStart(chooseHt2[i5]);
                        if (o < paraStart) {
                            chooseHtv3[i5] = staticLayout2.getLineTop(staticLayout2.getLineForOffset(o));
                        } else {
                            chooseHtv3[i5] = v2;
                        }
                    }
                    chooseHtv2 = chooseHtv3;
                    chooseHt = chooseHt2;
                    firstWidthLineCount = firstWidthLineCount2;
                    firstWidth = firstWidth2;
                    restWidth = restWidth2;
                }
            } else {
                textDir2 = textDir3;
                paint2 = paint3;
                bufStart2 = bufStart3;
                source3 = source4;
                chooseHtv2 = chooseHtv3;
                firstWidthLineCount = 1;
                firstWidth = firstWidth2;
                restWidth = restWidth2;
                chooseHt = null;
            }
            float[] variableTabStops3 = null;
            if (spanned5 != null) {
                TabStopSpan[] spans = (TabStopSpan[]) getParagraphSpans(spanned5, paraStart, paraEnd, TabStopSpan.class);
                if (spans.length > 0) {
                    float[] stops = new float[spans.length];
                    int i6 = 0;
                    while (i6 < spans.length) {
                        stops[i6] = (float) spans[i6].getTabStop();
                        i6++;
                        variableTabStops3 = variableTabStops3;
                    }
                    Arrays.sort(stops, 0, stops.length);
                    variableTabStops = stops;
                    MeasuredParagraph measuredPara = paragraphInfo[paraIndex].measured;
                    char[] chs = measuredPara.getChars();
                    int[] spanEndCache = measuredPara.getSpanEndCache().getRawArray();
                    int[] fmCache = measuredPara.getFontMetrics().getRawArray();
                    LineBreaker.ParagraphConstraints constraints3 = constraints;
                    constraints3.setWidth((float) restWidth);
                    constraints3.setIndent((float) firstWidth, firstWidthLineCount);
                    constraints3.setTabStops(variableTabStops, TAB_INCREMENT);
                    int paraIndex2 = paraIndex;
                    int firstWidthLineCount3 = firstWidthLineCount;
                    LineBreaker lineBreaker3 = lineBreaker;
                    res = lineBreaker3.computeLineBreaks(measuredPara.getMeasuredText(), constraints3, staticLayout2.mLineCount);
                    breakCount = res.getLineCount();
                    if (lineBreakCapacity2 >= breakCount) {
                        paragraphInfo2 = paragraphInfo;
                        hyphenEdits = new int[breakCount];
                        lineBreakCapacity = breakCount;
                        breaks = new int[breakCount];
                        lineWidths = new float[breakCount];
                        ascents = new float[breakCount];
                        descents = new float[breakCount];
                        hasTabs = new boolean[breakCount];
                    } else {
                        paragraphInfo2 = paragraphInfo;
                        lineBreakCapacity = lineBreakCapacity2;
                        breaks = breaks2;
                        lineWidths = lineWidths2;
                        ascents = ascents2;
                        descents = descents2;
                        hasTabs = hasTabs2;
                        hyphenEdits = hyphenEdits2;
                    }
                    i = 0;
                    while (i < breakCount) {
                        breaks[i] = res.getLineBreakOffset(i);
                        lineWidths[i] = res.getLineWidth(i);
                        ascents[i] = res.getLineAscent(i);
                        descents[i] = res.getLineDescent(i);
                        hasTabs[i] = res.hasLineTab(i);
                        hyphenEdits[i] = packHyphenEdit(res.getStartLineHyphenEdit(i), res.getEndLineHyphenEdit(i));
                        i++;
                        constraints3 = constraints3;
                    }
                    LineBreaker.ParagraphConstraints constraints4 = constraints3;
                    remainingLineCount = staticLayout2.mMaximumVisibleLineCount - staticLayout2.mLineCount;
                    boolean ellipsisMayBeApplied = ellipsize2 == null && (ellipsize2 == TextUtils.TruncateAt.END || (staticLayout2.mMaximumVisibleLineCount == 1 && ellipsize2 != TextUtils.TruncateAt.MARQUEE));
                    if (remainingLineCount > 0 || remainingLineCount >= breakCount || !ellipsisMayBeApplied) {
                        res2 = res;
                        spanned2 = spanned5;
                        breakCount2 = breakCount;
                    } else {
                        float width = 0.0f;
                        int i7 = remainingLineCount - 1;
                        boolean hasTab = false;
                        while (i7 < breakCount) {
                            if (i7 != breakCount - 1) {
                                int j = i7 == 0 ? 0 : breaks[i7 - 1];
                                while (true) {
                                    spanned3 = spanned5;
                                    if (j >= breaks[i7]) {
                                        break;
                                    }
                                    width += measuredPara.getCharWidthAt(j);
                                    j++;
                                    spanned5 = spanned3;
                                }
                            } else {
                                width += lineWidths[i7];
                                spanned3 = spanned5;
                            }
                            hasTab |= hasTabs[i7];
                            i7++;
                            res = res;
                            spanned5 = spanned3;
                        }
                        res2 = res;
                        spanned2 = spanned5;
                        breaks[remainingLineCount - 1] = breaks[breakCount - 1];
                        lineWidths[remainingLineCount - 1] = width;
                        hasTabs[remainingLineCount - 1] = hasTab;
                        breakCount2 = remainingLineCount;
                    }
                    int fmCacheIndex = 0;
                    int spanEndCacheIndex = 0;
                    int breakIndex2 = paraStart;
                    int fmTop2 = 0;
                    spanStart = paraStart;
                    int breakIndex3 = 0;
                    int remainingLineCount2 = remainingLineCount;
                    int fmAscent3 = 0;
                    int fmBottom2 = 0;
                    int fmDescent = 0;
                    while (spanStart < paraEnd) {
                        int spanEndCacheIndex2 = spanEndCacheIndex + 1;
                        int spanEnd = spanEndCache[spanEndCacheIndex];
                        int fmDescent2 = 0;
                        int paraEnd2 = paraEnd;
                        LineBreaker lineBreaker4 = lineBreaker3;
                        Paint.FontMetricsInt fm5 = fm;
                        fm5.top = fmCache[(fmCacheIndex * 4) + 0];
                        boolean z = true;
                        fm5.bottom = fmCache[(fmCacheIndex * 4) + 1];
                        char c = 2;
                        fm5.ascent = fmCache[(fmCacheIndex * 4) + 2];
                        fm5.descent = fmCache[(fmCacheIndex * 4) + 3];
                        int fmCacheIndex2 = fmCacheIndex + 1;
                        if (fm5.top < fmTop2) {
                            fmTop2 = fm5.top;
                        }
                        if (fm5.ascent < fmAscent3) {
                            fmAscent3 = fm5.ascent;
                        }
                        if (fm5.descent > fmDescent) {
                            fmDescent = fm5.descent;
                        }
                        if (fm5.bottom > fmBottom2) {
                            fmBottom2 = fm5.bottom;
                            breakIndex = breakIndex3;
                        } else {
                            breakIndex = breakIndex3;
                        }
                        while (true) {
                            if (breakIndex >= breakCount2) {
                                fmTop = fmTop2;
                                break;
                            }
                            fmTop = fmTop2;
                            if (paraStart + breaks[breakIndex] >= spanStart) {
                                break;
                            }
                            breakIndex++;
                            fmTop2 = fmTop;
                        }
                        int spanEnd2 = fmAscent3;
                        int fmAscent4 = breakIndex;
                        int fmBottom3 = fmBottom2;
                        int here = breakIndex2;
                        int v3 = v2;
                        int fmTop3 = fmTop;
                        int fmDescent3 = fmDescent;
                        while (fmAscent4 < breakCount2 && breaks[fmAscent4] + paraStart <= spanEnd) {
                            int endPos = paraStart + breaks[fmAscent4];
                            boolean moreChars = endPos < bufEnd2 ? z : fmDescent2;
                            if (fallbackLineSpacing) {
                                ascent = Math.min(spanEnd2, Math.round(ascents[fmAscent4]));
                            } else {
                                ascent = spanEnd2;
                            }
                            if (fallbackLineSpacing) {
                                fmAscent = spanEnd2;
                                fmAscent2 = Math.max(fmDescent3, Math.round(descents[fmAscent4]));
                            } else {
                                fmAscent = spanEnd2;
                                fmAscent2 = fmDescent3;
                            }
                            v3 = out(source3, here, endPos, ascent, fmAscent2, fmTop3, fmBottom3, v3, spacingmult, spacingadd, chooseHt, chooseHtv2, fm5, hasTabs[fmAscent4], hyphenEdits[fmAscent4], needMultiply, measuredPara, bufEnd2, includepad, trackpad, addLastLineSpacing, chs, paraStart, ellipsize2, ellipsizedWidth3, lineWidths[fmAscent4], paint2, moreChars);
                            if (endPos < spanEnd) {
                                fm3 = fm5;
                                fmTop3 = fm3.top;
                                fmBottom3 = fm3.bottom;
                                fmBottom = fm3.ascent;
                                fmDescent3 = fm3.descent;
                            } else {
                                fm3 = fm5;
                                fmBottom = fmDescent2;
                                fmDescent3 = fmDescent2;
                                fmBottom3 = fmDescent2;
                                fmTop3 = fmDescent2;
                            }
                            here = endPos;
                            fmAscent4++;
                            if (this.mLineCount < this.mMaximumVisibleLineCount || !this.mEllipsized) {
                                spanEnd = spanEnd;
                                fm5 = fm3;
                                spanEnd2 = fmBottom;
                                measuredPara = measuredPara;
                                variableTabStops = variableTabStops;
                                restWidth = restWidth;
                                firstWidth = firstWidth;
                                firstWidthLineCount3 = firstWidthLineCount3;
                                remainingLineCount2 = remainingLineCount2;
                                paraIndex2 = paraIndex2;
                                constraints4 = constraints4;
                                paraEnd2 = paraEnd2;
                                ellipsize2 = ellipsize2;
                                paraStart = paraStart;
                                res2 = res2;
                                spanStart = spanStart;
                                paragraphInfo2 = paragraphInfo2;
                                breakCount2 = breakCount2;
                                fmDescent2 = fmDescent2;
                                ellipsizedWidth3 = ellipsizedWidth3;
                                c = c;
                                z = z;
                                textDir2 = textDir2;
                                paint2 = paint2;
                                bufEnd2 = bufEnd2;
                                bufStart2 = bufStart2;
                                source3 = source3;
                                lineBreaker4 = lineBreaker4;
                            } else {
                                return;
                            }
                        }
                        staticLayout2 = this;
                        measuredPara = measuredPara;
                        variableTabStops = variableTabStops;
                        restWidth = restWidth;
                        fmCacheIndex = fmCacheIndex2;
                        firstWidth = firstWidth;
                        firstWidthLineCount3 = firstWidthLineCount3;
                        remainingLineCount2 = remainingLineCount2;
                        v2 = v3;
                        paraIndex2 = paraIndex2;
                        spanEndCacheIndex = spanEndCacheIndex2;
                        breakIndex2 = here;
                        fmTop2 = fmTop3;
                        fmBottom2 = fmBottom3;
                        constraints4 = constraints4;
                        paraEnd = paraEnd2;
                        fmDescent = fmDescent3;
                        ellipsize2 = ellipsize2;
                        fmAscent3 = spanEnd2;
                        res2 = res2;
                        paragraphInfo2 = paragraphInfo2;
                        breakIndex3 = fmAscent4;
                        breakCount2 = breakCount2;
                        ellipsizedWidth3 = ellipsizedWidth3;
                        textDir2 = textDir2;
                        paint2 = paint2;
                        bufEnd2 = bufEnd2;
                        bufStart2 = bufStart2;
                        source3 = source3;
                        lineBreaker3 = lineBreaker4;
                        fm = fm5;
                        spanStart = spanEnd;
                        paraStart = paraStart;
                    }
                    ellipsize = ellipsize2;
                    ellipsizedWidth = ellipsizedWidth3;
                    textDir = textDir2;
                    paint = paint2;
                    bufStart = bufStart2;
                    source = source3;
                    fm2 = fm;
                    staticLayout = staticLayout2;
                    bufEnd = bufEnd2;
                    if (paraEnd != bufEnd) {
                        v = v2;
                        chooseHtv = breaks;
                        break;
                    }
                    bufEnd2 = bufEnd;
                    fm = fm2;
                    staticLayout2 = staticLayout;
                    breaks2 = breaks;
                    lineWidths2 = lineWidths;
                    ascents2 = ascents;
                    descents2 = descents;
                    hasTabs2 = hasTabs;
                    hyphenEdits2 = hyphenEdits;
                    lineBreakCapacity2 = lineBreakCapacity;
                    spanned = spanned2;
                    constraints = constraints4;
                    ellipsize2 = ellipsize;
                    paragraphInfo = paragraphInfo2;
                    ellipsizedWidth3 = ellipsizedWidth;
                    textDir3 = textDir;
                    paint3 = paint;
                    bufStart3 = bufStart;
                    source4 = source;
                    lineBreaker = lineBreaker3;
                    paraIndex = paraIndex2 + 1;
                    chooseHtv3 = chooseHtv2;
                } else {
                    variableTabStops2 = null;
                }
            } else {
                variableTabStops2 = null;
            }
            variableTabStops = variableTabStops2;
            MeasuredParagraph measuredPara2 = paragraphInfo[paraIndex].measured;
            char[] chs2 = measuredPara2.getChars();
            int[] spanEndCache2 = measuredPara2.getSpanEndCache().getRawArray();
            int[] fmCache2 = measuredPara2.getFontMetrics().getRawArray();
            LineBreaker.ParagraphConstraints constraints32 = constraints;
            constraints32.setWidth((float) restWidth);
            constraints32.setIndent((float) firstWidth, firstWidthLineCount);
            constraints32.setTabStops(variableTabStops, TAB_INCREMENT);
            int paraIndex22 = paraIndex;
            int firstWidthLineCount32 = firstWidthLineCount;
            LineBreaker lineBreaker32 = lineBreaker;
            res = lineBreaker32.computeLineBreaks(measuredPara2.getMeasuredText(), constraints32, staticLayout2.mLineCount);
            breakCount = res.getLineCount();
            if (lineBreakCapacity2 >= breakCount) {
            }
            i = 0;
            while (i < breakCount) {
            }
            LineBreaker.ParagraphConstraints constraints42 = constraints32;
            remainingLineCount = staticLayout2.mMaximumVisibleLineCount - staticLayout2.mLineCount;
            if (ellipsize2 == null) {
            }
            if (remainingLineCount > 0) {
            }
            res2 = res;
            spanned2 = spanned5;
            breakCount2 = breakCount;
            int fmCacheIndex3 = 0;
            int spanEndCacheIndex3 = 0;
            int breakIndex22 = paraStart;
            int fmTop22 = 0;
            spanStart = paraStart;
            int breakIndex32 = 0;
            int remainingLineCount22 = remainingLineCount;
            int fmAscent32 = 0;
            int fmBottom22 = 0;
            int fmDescent4 = 0;
            while (spanStart < paraEnd) {
            }
            ellipsize = ellipsize2;
            ellipsizedWidth = ellipsizedWidth3;
            textDir = textDir2;
            paint = paint2;
            bufStart = bufStart2;
            source = source3;
            fm2 = fm;
            staticLayout = staticLayout2;
            bufEnd = bufEnd2;
            if (paraEnd != bufEnd) {
            }
        }
        if (bufEnd != bufStart) {
            source2 = source;
            if (source2.charAt(bufEnd - 1) != '\n') {
                return;
            }
        } else {
            source2 = source;
        }
        if (staticLayout.mLineCount < staticLayout.mMaximumVisibleLineCount) {
            MeasuredParagraph measuredPara3 = MeasuredParagraph.buildForBidi(source2, bufEnd, bufEnd, textDir, null);
            paint.getFontMetricsInt(fm2);
            out(source2, bufEnd, bufEnd, fm2.ascent, fm2.descent, fm2.top, fm2.bottom, v, spacingmult, spacingadd, null, null, fm2, false, 0, needMultiply, measuredPara3, bufEnd, includepad, trackpad, addLastLineSpacing, null, bufStart, ellipsize, ellipsizedWidth, 0.0f, paint, false);
        }
    }

    private int out(CharSequence text, int start, int end, int above, int below, int top, int bottom, int v, float spacingmult, float spacingadd, LineHeightSpan[] chooseHt, int[] chooseHtv, Paint.FontMetricsInt fm, boolean hasTab, int hyphenEdit, boolean needMultiply, MeasuredParagraph measured, int bufEnd, boolean includePad, boolean trackPad, boolean addLastLineLineSpacing, char[] chs, int widthStart, TextUtils.TruncateAt ellipsize, float ellipsisWidth, float textWidth, TextPaint paint, boolean moreChars) {
        int[] lines;
        int bottom2;
        int top2;
        int below2;
        int above2;
        int j;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        boolean lastCharIsNewLine;
        int extra;
        boolean lastCharIsNewLine2;
        int j2;
        int want;
        boolean z;
        int i7;
        int j3 = this.mLineCount;
        int i8 = this.mColumns;
        int off = j3 * i8;
        int i9 = 1;
        int want2 = off + i8 + 1;
        int[] lines2 = this.mLines;
        int dir = measured.getParagraphDir();
        if (want2 >= lines2.length) {
            int[] grow = ArrayUtils.newUnpaddedIntArray(GrowingArrayUtils.growSize(want2));
            System.arraycopy(lines2, 0, grow, 0, lines2.length);
            this.mLines = grow;
            lines = grow;
        } else {
            lines = lines2;
        }
        if (j3 >= this.mLineDirections.length) {
            Layout.Directions[] grow2 = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, GrowingArrayUtils.growSize(j3));
            Layout.Directions[] directionsArr = this.mLineDirections;
            System.arraycopy(directionsArr, 0, grow2, 0, directionsArr.length);
            this.mLineDirections = grow2;
        }
        if (chooseHt != null) {
            fm.ascent = above;
            fm.descent = below;
            fm.top = top;
            fm.bottom = bottom;
            int i10 = 0;
            while (i10 < chooseHt.length) {
                if (chooseHt[i10] instanceof LineHeightSpan.WithDensity) {
                    z = false;
                    want = want2;
                    i7 = i9;
                    j2 = j3;
                    ((LineHeightSpan.WithDensity) chooseHt[i10]).chooseHeight(text, start, end, chooseHtv[i10], v, fm, paint);
                } else {
                    want = want2;
                    i7 = i9;
                    j2 = j3;
                    z = false;
                    chooseHt[i10].chooseHeight(text, start, end, chooseHtv[i10], v, fm);
                }
                i10++;
                i9 = i7;
                want2 = want;
                j3 = j2;
            }
            i2 = i9;
            j = j3;
            i = 0;
            above2 = fm.ascent;
            below2 = fm.descent;
            top2 = fm.top;
            bottom2 = fm.bottom;
        } else {
            i = false;
            i2 = 1;
            j = j3;
            above2 = above;
            below2 = below;
            top2 = top;
            bottom2 = bottom;
        }
        boolean firstLine = j == 0 ? i2 : i;
        boolean currentLineIsTheLastVisibleOne = j + 1 == this.mMaximumVisibleLineCount ? i2 : i;
        if (ellipsize != null) {
            boolean forceEllipsis = (!moreChars || this.mLineCount + i2 != this.mMaximumVisibleLineCount) ? i : i2;
            if ((((((this.mMaximumVisibleLineCount != i2 || !moreChars) && (!firstLine || moreChars)) || ellipsize == TextUtils.TruncateAt.MARQUEE) && (firstLine || ((!currentLineIsTheLastVisibleOne && moreChars) || ellipsize != TextUtils.TruncateAt.END))) ? i : i2) != 0) {
                i4 = widthStart;
                i3 = bufEnd;
                calculateEllipsis(start, end, measured, widthStart, ellipsisWidth, ellipsize, j, textWidth, paint, forceEllipsis);
            } else {
                i4 = widthStart;
                i3 = bufEnd;
            }
        } else {
            i4 = widthStart;
            i3 = bufEnd;
        }
        if (this.mEllipsized) {
            lastCharIsNewLine = true;
            i6 = start;
            i5 = 1;
        } else {
            if (i4 != i3 && i3 > 0) {
                if (text.charAt(i3 - 1) == '\n') {
                    lastCharIsNewLine2 = true;
                    if (end == i3 || lastCharIsNewLine2) {
                        i6 = start;
                        i5 = 1;
                        if (i6 == i3 || !lastCharIsNewLine2) {
                            lastCharIsNewLine = false;
                        } else {
                            lastCharIsNewLine = true;
                        }
                    } else {
                        lastCharIsNewLine = true;
                        i5 = 1;
                        i6 = start;
                    }
                }
            }
            lastCharIsNewLine2 = i;
            if (end == i3) {
            }
            i6 = start;
            i5 = 1;
            if (i6 == i3) {
            }
            lastCharIsNewLine = false;
        }
        if (firstLine) {
            if (trackPad) {
                this.mTopPadding = top2 - above2;
            }
            if (includePad) {
                above2 = top2;
            }
        }
        if (lastCharIsNewLine) {
            if (trackPad) {
                this.mBottomPadding = bottom2 - below2;
            }
            if (includePad) {
                below2 = bottom2;
            }
        }
        if (!needMultiply || (!addLastLineLineSpacing && lastCharIsNewLine)) {
            extra = 0;
        } else {
            double ex = (double) ((((float) (below2 - above2)) * (spacingmult - 1.0f)) + spacingadd);
            if (ex >= 0.0d) {
                extra = (int) (EXTRA_ROUNDING + ex);
            } else {
                extra = -((int) ((-ex) + EXTRA_ROUNDING));
            }
        }
        lines[off + 0] = i6;
        lines[off + 1] = v;
        lines[off + 2] = below2 + extra;
        lines[off + 3] = extra;
        if (!this.mEllipsized && currentLineIsTheLastVisibleOne) {
            this.mMaxLineHeight = v + ((includePad ? bottom2 : below2) - above2);
        }
        int v2 = v + (below2 - above2) + extra;
        int i11 = this.mColumns;
        lines[off + i11 + 0] = end;
        lines[off + i11 + i5] = v2;
        int i12 = off + 0;
        int i13 = lines[i12];
        if (hasTab) {
            i = true;
        }
        lines[i12] = i13 | i;
        lines[off + 4] = hyphenEdit;
        int i14 = off + 0;
        lines[i14] = lines[i14] | (dir << 30);
        this.mLineDirections[j] = measured.getDirections(i6 - i4, end - i4);
        this.mLineCount += i5;
        return v2;
    }

    private void calculateEllipsis(int lineStart, int lineEnd, MeasuredParagraph measured, int widthStart, float avail, TextUtils.TruncateAt where, int line, float textWidth, TextPaint paint, boolean forceEllipsis) {
        int right;
        float avail2 = avail - getTotalInsets(line);
        if (textWidth > avail2 || forceEllipsis) {
            float ellipsisWidth = paint.measureText(TextUtils.getEllipsisString(where));
            int ellipsisStart = 0;
            int ellipsisCount = 0;
            int len = lineEnd - lineStart;
            if (where == TextUtils.TruncateAt.START) {
                if (this.mMaximumVisibleLineCount == 1) {
                    float sum = 0.0f;
                    int i = len;
                    while (true) {
                        if (i <= 0) {
                            break;
                        }
                        float w = measured.getCharWidthAt(((i - 1) + lineStart) - widthStart);
                        if (w + sum + ellipsisWidth > avail2) {
                            while (i < len && measured.getCharWidthAt((i + lineStart) - widthStart) == 0.0f) {
                                i++;
                            }
                        } else {
                            sum += w;
                            i--;
                        }
                    }
                    ellipsisStart = 0;
                    ellipsisCount = i;
                } else if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "Start Ellipsis only supported with one line");
                }
            } else if (where == TextUtils.TruncateAt.END || where == TextUtils.TruncateAt.MARQUEE || where == TextUtils.TruncateAt.END_SMALL) {
                float sum2 = 0.0f;
                int i2 = 0;
                while (i2 < len) {
                    float w2 = measured.getCharWidthAt((i2 + lineStart) - widthStart);
                    if (w2 + sum2 + ellipsisWidth > avail2) {
                        break;
                    }
                    sum2 += w2;
                    i2++;
                }
                ellipsisStart = i2;
                ellipsisCount = len - i2;
                if (forceEllipsis && ellipsisCount == 0 && len > 0) {
                    ellipsisStart = len - 1;
                    ellipsisCount = 1;
                }
            } else if (this.mMaximumVisibleLineCount == 1) {
                float lsum = 0.0f;
                float rsum = 0.0f;
                int right2 = len;
                float ravail = (avail2 - ellipsisWidth) / 2.0f;
                while (true) {
                    if (right <= 0) {
                        break;
                    }
                    float w3 = measured.getCharWidthAt(((right - 1) + lineStart) - widthStart);
                    if (w3 + rsum > ravail) {
                        right++;
                        while (right < len && measured.getCharWidthAt((right + lineStart) - widthStart) == 0.0f) {
                            right++;
                        }
                    } else {
                        rsum += w3;
                        right2 = right - 1;
                    }
                }
                float lavail = (avail2 - ellipsisWidth) - rsum;
                int left = 0;
                while (true) {
                    if (left >= right) {
                        break;
                    }
                    float w4 = measured.getCharWidthAt((left + lineStart) - widthStart);
                    if (w4 + lsum > lavail) {
                        left--;
                        break;
                    } else {
                        lsum += w4;
                        left++;
                    }
                }
                ellipsisStart = left;
                ellipsisCount = right - left;
            } else if (Log.isLoggable(TAG, 5)) {
                Log.w(TAG, "Middle Ellipsis only supported with one line");
            }
            this.mEllipsized = true;
            int[] iArr = this.mLines;
            int i3 = this.mColumns;
            iArr[(i3 * line) + 5] = ellipsisStart;
            iArr[(i3 * line) + 6] = ellipsisCount;
            return;
        }
        int[] iArr2 = this.mLines;
        int i4 = this.mColumns;
        iArr2[(i4 * line) + 5] = 0;
        iArr2[(i4 * line) + 6] = 0;
    }

    private float getTotalInsets(int line) {
        int totalIndent = 0;
        int[] iArr = this.mLeftIndents;
        if (iArr != null) {
            totalIndent = iArr[Math.min(line, iArr.length - 1)];
        }
        int[] iArr2 = this.mRightIndents;
        if (iArr2 != null) {
            totalIndent += iArr2[Math.min(line, iArr2.length - 1)];
        }
        return (float) totalIndent;
    }

    @Override // android.text.Layout
    public int getLineForVertical(int vertical) {
        int high = this.mLineCount;
        int low = -1;
        int[] lines = this.mLines;
        while (high - low > 1) {
            int guess = (high + low) >> 1;
            if (lines[(this.mColumns * guess) + 1] > vertical) {
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return 0;
        }
        return low;
    }

    @Override // android.text.Layout
    public int getLineCount() {
        return this.mLineCount;
    }

    @Override // android.text.Layout
    public int getLineTop(int line) {
        return this.mLines[(this.mColumns * line) + 1];
    }

    @Override // android.text.Layout
    public int getLineExtra(int line) {
        return this.mLines[(this.mColumns * line) + 3];
    }

    @Override // android.text.Layout
    public int getLineDescent(int line) {
        return this.mLines[(this.mColumns * line) + 2];
    }

    @Override // android.text.Layout
    public int getLineStart(int line) {
        return this.mLines[(this.mColumns * line) + 0] & 536870911;
    }

    @Override // android.text.Layout
    public int getParagraphDirection(int line) {
        return this.mLines[(this.mColumns * line) + 0] >> 30;
    }

    @Override // android.text.Layout
    public boolean getLineContainsTab(int line) {
        return (this.mLines[(this.mColumns * line) + 0] & 536870912) != 0;
    }

    @Override // android.text.Layout
    public final Layout.Directions getLineDirections(int line) {
        if (line <= getLineCount()) {
            return this.mLineDirections[line];
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    @Override // android.text.Layout
    public int getTopPadding() {
        return this.mTopPadding;
    }

    @Override // android.text.Layout
    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    static int packHyphenEdit(int start, int end) {
        return (start << 3) | end;
    }

    static int unpackStartHyphenEdit(int packedHyphenEdit) {
        return (packedHyphenEdit & 24) >> 3;
    }

    static int unpackEndHyphenEdit(int packedHyphenEdit) {
        return packedHyphenEdit & 7;
    }

    @Override // android.text.Layout
    public int getStartHyphenEdit(int lineNumber) {
        return unpackStartHyphenEdit(this.mLines[(this.mColumns * lineNumber) + 4] & 255);
    }

    @Override // android.text.Layout
    public int getEndHyphenEdit(int lineNumber) {
        return unpackEndHyphenEdit(this.mLines[(this.mColumns * lineNumber) + 4] & 255);
    }

    @Override // android.text.Layout
    public int getIndentAdjust(int line, Layout.Alignment align) {
        if (align == Layout.Alignment.ALIGN_LEFT) {
            int[] iArr = this.mLeftIndents;
            if (iArr == null) {
                return 0;
            }
            return iArr[Math.min(line, iArr.length - 1)];
        } else if (align == Layout.Alignment.ALIGN_RIGHT) {
            int[] iArr2 = this.mRightIndents;
            if (iArr2 == null) {
                return 0;
            }
            return -iArr2[Math.min(line, iArr2.length - 1)];
        } else if (align == Layout.Alignment.ALIGN_CENTER) {
            int left = 0;
            int[] iArr3 = this.mLeftIndents;
            if (iArr3 != null) {
                left = iArr3[Math.min(line, iArr3.length - 1)];
            }
            int right = 0;
            int[] iArr4 = this.mRightIndents;
            if (iArr4 != null) {
                right = iArr4[Math.min(line, iArr4.length - 1)];
            }
            return (left - right) >> 1;
        } else {
            throw new AssertionError("unhandled alignment " + align);
        }
    }

    @Override // android.text.Layout
    public int getEllipsisCount(int line) {
        int i = this.mColumns;
        if (i < 7) {
            return 0;
        }
        return this.mLines[(i * line) + 6];
    }

    @Override // android.text.Layout
    public int getEllipsisStart(int line) {
        int i = this.mColumns;
        if (i < 7) {
            return 0;
        }
        return this.mLines[(i * line) + 5];
    }

    @Override // android.text.Layout
    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    @Override // android.text.Layout
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public int getHeight(boolean cap) {
        int i;
        if (cap && this.mLineCount > this.mMaximumVisibleLineCount && this.mMaxLineHeight == -1 && Log.isLoggable(TAG, 5)) {
            Log.w(TAG, "maxLineHeight should not be -1.  maxLines:" + this.mMaximumVisibleLineCount + " lineCount:" + this.mLineCount);
        }
        if (!cap || this.mLineCount <= this.mMaximumVisibleLineCount || (i = this.mMaxLineHeight) == -1) {
            return super.getHeight();
        }
        return i;
    }

    static class LineBreaks {
        private static final int INITIAL_SIZE = 16;
        @UnsupportedAppUsage
        public float[] ascents = new float[16];
        @UnsupportedAppUsage
        public int[] breaks = new int[16];
        @UnsupportedAppUsage
        public float[] descents = new float[16];
        @UnsupportedAppUsage
        public int[] flags = new int[16];
        @UnsupportedAppUsage
        public float[] widths = new float[16];

        LineBreaks() {
        }
    }
}

package android.text;

import android.graphics.Paint;
import android.text.AutoGrowArray;
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
import java.util.HashMap;
import java.util.Locale;

public class StaticLayout extends Layout {
    private static final char CHAR_NEW_LINE = '\n';
    private static final int COLUMNS_ELLIPSIZE = 7;
    private static final int COLUMNS_NORMAL = 5;
    private static final int DEFAULT_MAX_LINE_HEIGHT = -1;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 6;
    private static final int ELLIPSIS_START = 5;
    private static final int EXTRA = 3;
    private static final double EXTRA_ROUNDING = 0.5d;
    private static final int HYPHEN = 4;
    private static final int HYPHEN_MASK = 255;
    private static final int ROWHEIGHTOFFSET_BO = 7;
    private static final int ROWHEIGHTOFFSET_MY = 2;
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_INCREMENT = 20;
    private static final int TAB_MASK = 536870912;
    static final String TAG = "StaticLayout";
    private static final int TOP = 1;
    private int mBottomPadding;
    private int mColumns;
    private boolean mEllipsized;
    private int mEllipsizedWidth;
    private int[] mLeftIndents;
    private int[] mLeftPaddings;
    private int mLineCount;
    private Layout.Directions[] mLineDirections;
    private int[] mLines;
    private int mMaxLineHeight;
    private int mMaximumVisibleLineCount;
    private int[] mRightIndents;
    private int[] mRightPaddings;
    private int mTopPadding;

    public static final class Builder {
        private static final Pools.SynchronizedPool<Builder> sPool = new Pools.SynchronizedPool<>(3);
        /* access modifiers changed from: private */
        public boolean mAddLastLineLineSpacing;
        /* access modifiers changed from: private */
        public Layout.Alignment mAlignment;
        /* access modifiers changed from: private */
        public int mBreakStrategy;
        /* access modifiers changed from: private */
        public TextUtils.TruncateAt mEllipsize;
        /* access modifiers changed from: private */
        public int mEllipsizedWidth;
        /* access modifiers changed from: private */
        public int mEnd;
        /* access modifiers changed from: private */
        public boolean mFallbackLineSpacing;
        /* access modifiers changed from: private */
        public final Paint.FontMetricsInt mFontMetricsInt = new Paint.FontMetricsInt();
        /* access modifiers changed from: private */
        public int mHyphenationFrequency;
        /* access modifiers changed from: private */
        public boolean mIncludePad;
        /* access modifiers changed from: private */
        public int mJustificationMode;
        /* access modifiers changed from: private */
        public int[] mLeftIndents;
        /* access modifiers changed from: private */
        public int[] mLeftPaddings;
        /* access modifiers changed from: private */
        public int mMaxLines;
        /* access modifiers changed from: private */
        public TextPaint mPaint;
        /* access modifiers changed from: private */
        public int[] mRightIndents;
        /* access modifiers changed from: private */
        public int[] mRightPaddings;
        /* access modifiers changed from: private */
        public float mSpacingAdd;
        /* access modifiers changed from: private */
        public float mSpacingMult;
        /* access modifiers changed from: private */
        public int mStart;
        /* access modifiers changed from: private */
        public CharSequence mText;
        /* access modifiers changed from: private */
        public TextDirectionHeuristic mTextDir;
        /* access modifiers changed from: private */
        public int mWidth;

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
            b.mLeftPaddings = null;
            b.mRightPaddings = null;
            sPool.release(b);
        }

        /* access modifiers changed from: package-private */
        public void finish() {
            this.mText = null;
            this.mPaint = null;
            this.mLeftIndents = null;
            this.mRightIndents = null;
            this.mLeftPaddings = null;
            this.mRightPaddings = null;
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

        public Builder setAvailablePaddings(int[] leftPaddings, int[] rightPaddings) {
            this.mLeftPaddings = leftPaddings;
            this.mRightPaddings = rightPaddings;
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

    static class LineBreaks {
        private static final int INITIAL_SIZE = 16;
        public float[] ascents = new float[16];
        public int[] breaks = new int[16];
        public float[] descents = new float[16];
        public int[] flags = new int[16];
        public float[] widths = new float[16];

        LineBreaks() {
        }
    }

    private static native int nComputeLineBreaks(long j, char[] cArr, long j2, int i, float f, int i2, float f2, int[] iArr, int i3, int i4, LineBreaks lineBreaks, int i5, int[] iArr2, float[] fArr, float[] fArr2, float[] fArr3, int[] iArr3, float[] fArr4);

    private static native void nFinish(long j);

    private static native long nInit(int i, int i2, boolean z, int[] iArr, int[] iArr2, int[] iArr3);

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
    @Deprecated
    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Layout.Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth, int maxLines) {
        super(r1, paint, outerwidth, align, textDir, spacingmult, spacingadd);
        CharSequence charSequence;
        CharSequence ellipsizer;
        CharSequence charSequence2 = source;
        TextUtils.TruncateAt truncateAt = ellipsize;
        int i = ellipsizedWidth;
        int i2 = maxLines;
        if (truncateAt == null) {
            charSequence = charSequence2;
        } else {
            if (charSequence2 instanceof Spanned) {
                ellipsizer = new Layout.SpannedEllipsizer(charSequence2);
            } else {
                ellipsizer = new Layout.Ellipsizer(charSequence2);
            }
            charSequence = ellipsizer;
        }
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        Builder b = Builder.obtain(source, bufstart, bufend, paint, outerwidth).setAlignment(align).setTextDirection(textDir).setLineSpacing(spacingadd, spacingmult).setIncludePad(includepad).setEllipsizedWidth(i).setEllipsize(truncateAt).setMaxLines(i2);
        if (truncateAt != null) {
            Layout.Ellipsizer e = (Layout.Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = i;
            e.mMethod = truncateAt;
            this.mEllipsizedWidth = i;
            this.mColumns = 7;
            int i3 = outerwidth;
        } else {
            this.mColumns = 5;
            this.mEllipsizedWidth = outerwidth;
        }
        this.mLineDirections = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, 2);
        this.mLines = ArrayUtils.newUnpaddedIntArray(2 * this.mColumns);
        this.mMaximumVisibleLineCount = i2;
        generate(b, b.mIncludePad, b.mIncludePad);
        Builder.recycle(b);
    }

    StaticLayout(CharSequence text) {
        super(text, null, 0, null, 0.0f, 0.0f);
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        this.mColumns = 7;
        this.mLineDirections = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, 2);
        this.mLines = ArrayUtils.newUnpaddedIntArray(2 * this.mColumns);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    private StaticLayout(Builder b) {
        super(r0, b.mPaint, b.mWidth, b.mAlignment, b.mTextDir, b.mSpacingMult, b.mSpacingAdd);
        CharSequence ellipsizer;
        if (b.mEllipsize == null) {
            ellipsizer = b.mText;
        } else if (b.mText instanceof Spanned) {
            ellipsizer = new Layout.SpannedEllipsizer(b.mText);
        } else {
            ellipsizer = new Layout.Ellipsizer(b.mText);
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
        this.mLines = ArrayUtils.newUnpaddedIntArray(2 * this.mColumns);
        this.mMaximumVisibleLineCount = b.mMaxLines;
        this.mLeftIndents = b.mLeftIndents;
        this.mRightIndents = b.mRightIndents;
        this.mLeftPaddings = b.mLeftPaddings;
        this.mRightPaddings = b.mRightPaddings;
        setJustificationMode(b.mJustificationMode);
        generate(b, b.mIncludePad, b.mIncludePad);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0399 A[Catch:{ all -> 0x036d }] */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x03ed  */
    /* JADX WARNING: Removed duplicated region for block: B:280:0x0762 A[LOOP:2: B:49:0x0173->B:280:0x0762, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0871  */
    /* JADX WARNING: Removed duplicated region for block: B:306:0x088d  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0895  */
    /* JADX WARNING: Removed duplicated region for block: B:321:0x08f3  */
    /* JADX WARNING: Removed duplicated region for block: B:332:0x075d A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:333:0x0852 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0176  */
    public void generate(Builder b, boolean includepad, boolean trackpad) {
        int[] indents;
        Paint.FontMetricsInt fm;
        TextUtils.TruncateAt ellipsize;
        LineBreaks lineBreaks;
        AutoGrowArray.FloatArray widths;
        float ellipsizedWidth;
        long nativePtr;
        Paint.FontMetricsInt fm2;
        Spanned spanned;
        boolean z;
        PrecomputedText.ParagraphInfo[] paragraphInfo;
        PrecomputedText.ParagraphInfo[] paragraphInfo2;
        int paraEnd;
        int v;
        int[] chooseHtv;
        long nativePtr2;
        int v2;
        TextDirectionHeuristic textDir;
        TextPaint paint;
        int bufEnd;
        int bufStart;
        CharSequence source;
        Paint.FontMetricsInt fm3;
        StaticLayout staticLayout;
        float ellipsizedWidth2;
        TextUtils.TruncateAt ellipsize2;
        long nativePtr3;
        int bufStart2;
        CharSequence source2;
        int paraStart;
        CharSequence source3;
        int bufStart3;
        TextPaint paint2;
        TextDirectionHeuristic textDir2;
        int[] chooseHtv2;
        int firstWidthLineCount;
        LineHeightSpan[] chooseHt;
        int firstWidth;
        int restWidth;
        MeasuredParagraph measuredPara;
        char[] chs;
        int[] spanEndCache;
        int[] fmCache;
        AutoGrowArray.FloatArray widths2;
        PrecomputedText.ParagraphInfo[] paragraphInfo3;
        Spanned spanned2;
        LineBreaks lineBreaks2;
        int paraIndex;
        boolean z2;
        int spanStart;
        AutoGrowArray.FloatArray widths3;
        LineBreaks lineBreaks3;
        Spanned spanned3;
        PrecomputedText.ParagraphInfo[] paragraphInfo4;
        int paraEnd2;
        Paint.FontMetricsInt fm4;
        Locale loc;
        int breakCount;
        int paraStart2;
        int fmDescent;
        AutoGrowArray.FloatArray widths4;
        char[] chs2;
        int fmAscent;
        int breakIndex;
        TextUtils.TruncateAt ellipsize3;
        int remainingLineCount;
        LineBreaks lineBreaks4;
        Locale loc2;
        StaticLayout staticLayout2;
        long nativePtr4;
        float ellipsizedWidth3;
        TextDirectionHeuristic textDir3;
        TextPaint paint3;
        int bufStart4;
        CharSequence source4;
        Spanned spanned4;
        PrecomputedText.ParagraphInfo[] paragraphInfo5;
        MeasuredParagraph measuredPara2;
        int ellipsizedWidth4;
        int firstWidth2;
        int bufEnd2;
        int breakCount2;
        int breakCount3;
        int breakCount4;
        int spanEnd;
        int spanStart2;
        int endPos;
        int paraStart3;
        PrecomputedText.ParagraphInfo[] paragraphInfo6;
        AutoGrowArray.FloatArray widths5;
        boolean z3;
        boolean z4;
        float ellipsizedWidth5;
        char[] chs3;
        TextDirectionHeuristic textDir4;
        TextPaint paint4;
        MeasuredParagraph measuredPara3;
        int breakIndex2;
        int bufEnd3;
        TextUtils.TruncateAt ellipsize4;
        int bufStart5;
        int restWidth2;
        int remainingLineCount2;
        CharSequence source5;
        int firstWidth3;
        LineBreaks lineBreaks5;
        char c;
        Spanned spanned5;
        Locale loc3;
        Paint.FontMetricsInt fm5;
        int spanEnd2;
        int endPos2;
        int fmDescent2;
        int fmAscent2;
        int fmBottom;
        Paint.FontMetricsInt fm6;
        int fmTop;
        Locale loc4;
        long nativePtr5;
        int i;
        PrecomputedText.ParagraphInfo[] paragraphInfo7;
        StaticLayout staticLayout3 = this;
        String language = Locale.getDefault().getLanguage();
        HashMap<String, Integer> languageMap = new HashMap<>();
        languageMap.put("bo", 7);
        languageMap.put("my", 2);
        CharSequence source6 = b.mText;
        int bufStart6 = b.mStart;
        int bufEnd4 = b.mEnd;
        TextPaint paint5 = b.mPaint;
        int outerWidth = b.mWidth;
        TextDirectionHeuristic textDir5 = b.mTextDir;
        boolean fallbackLineSpacing = b.mFallbackLineSpacing;
        float spacingmult = b.mSpacingMult;
        float spacingadd = b.mSpacingAdd;
        float ellipsizedWidth6 = (float) b.mEllipsizedWidth;
        TextUtils.TruncateAt ellipsize5 = b.mEllipsize;
        boolean addLastLineSpacing = b.mAddLastLineLineSpacing;
        LineBreaks lineBreaks6 = new LineBreaks();
        AutoGrowArray.FloatArray widths6 = new AutoGrowArray.FloatArray();
        staticLayout3.mLineCount = 0;
        staticLayout3.mEllipsized = false;
        staticLayout3.mMaxLineHeight = staticLayout3.mMaximumVisibleLineCount < 1 ? 0 : -1;
        boolean needMultiply = (spacingmult == 1.0f && spacingadd == 0.0f) ? false : true;
        Paint.FontMetricsInt fm7 = b.mFontMetricsInt;
        if (staticLayout3.mLeftIndents == null && staticLayout3.mRightIndents == null) {
            fm = fm7;
            indents = null;
        } else {
            int leftLen = staticLayout3.mLeftIndents == null ? 0 : staticLayout3.mLeftIndents.length;
            int rightLen = staticLayout3.mRightIndents == null ? 0 : staticLayout3.mRightIndents.length;
            int[] indents2 = new int[Math.max(leftLen, rightLen)];
            int i2 = 0;
            while (true) {
                fm = fm7;
                int i3 = i2;
                if (i3 >= leftLen) {
                    break;
                }
                indents2[i3] = staticLayout3.mLeftIndents[i3];
                i2 = i3 + 1;
                fm7 = fm;
                leftLen = leftLen;
            }
            int i4 = 0;
            while (i4 < rightLen) {
                indents2[i4] = indents2[i4] + staticLayout3.mRightIndents[i4];
                i4++;
                rightLen = rightLen;
            }
            indents = indents2;
        }
        long nativePtr6 = nInit(b.mBreakStrategy, b.mHyphenationFrequency, b.mJustificationMode != 0, indents, staticLayout3.mLeftPaddings, staticLayout3.mRightPaddings);
        Spanned spanned6 = source6 instanceof Spanned ? (Spanned) source6 : null;
        if (source6 instanceof PrecomputedText) {
            PrecomputedText precomputed = (PrecomputedText) source6;
            PrecomputedText precomputed2 = precomputed;
            paragraphInfo7 = null;
            HashMap<String, Integer> hashMap = languageMap;
            fm2 = fm;
            spanned = spanned6;
            nativePtr = nativePtr6;
            ellipsizedWidth = ellipsizedWidth6;
            z = false;
            widths = widths6;
            lineBreaks = lineBreaks6;
            ellipsize = ellipsize5;
            if (precomputed.canUseMeasuredResult(bufStart6, bufEnd4, textDir5, paint5, b.mBreakStrategy, b.mHyphenationFrequency)) {
                paragraphInfo = precomputed2.getParagraphInfo();
                if (paragraphInfo == null) {
                    paragraphInfo = PrecomputedText.createMeasuredParagraphs(source6, new PrecomputedText.Params(paint5, textDir5, b.mBreakStrategy, b.mHyphenationFrequency), bufStart6, bufEnd4, z);
                }
                paragraphInfo2 = paragraphInfo;
                paraEnd = z;
                v = 0;
                chooseHtv = null;
                while (true) {
                    try {
                        if (paraEnd < paragraphInfo2.length) {
                            v2 = v;
                            PrecomputedText.ParagraphInfo[] paragraphInfoArr = paragraphInfo2;
                            textDir = textDir5;
                            paint = paint5;
                            bufEnd = bufEnd4;
                            bufStart = bufStart6;
                            source = source6;
                            fm3 = fm2;
                            staticLayout = staticLayout3;
                            ellipsizedWidth2 = ellipsizedWidth;
                            AutoGrowArray.FloatArray floatArray = widths;
                            LineBreaks lineBreaks7 = lineBreaks;
                            ellipsize2 = ellipsize;
                            Spanned spanned7 = spanned;
                            nativePtr3 = nativePtr;
                            break;
                        }
                        if (paraEnd == 0) {
                            paraStart = bufStart6;
                        } else {
                            try {
                                paraStart = paragraphInfo2[paraEnd - 1].paragraphEnd;
                            } catch (Throwable th) {
                                th = th;
                                int i5 = v;
                                PrecomputedText.ParagraphInfo[] paragraphInfoArr2 = paragraphInfo2;
                                int i6 = bufEnd4;
                                CharSequence charSequence = source6;
                                Paint.FontMetricsInt fontMetricsInt = fm2;
                                StaticLayout staticLayout4 = staticLayout3;
                                float f = ellipsizedWidth;
                                AutoGrowArray.FloatArray floatArray2 = widths;
                                LineBreaks lineBreaks8 = lineBreaks;
                                TextUtils.TruncateAt truncateAt = ellipsize;
                                Spanned spanned8 = spanned;
                                nativePtr2 = nativePtr;
                                int[] iArr = chooseHtv;
                                TextDirectionHeuristic textDirectionHeuristic = textDir5;
                                TextPaint textPaint = paint5;
                                int i7 = bufStart6;
                                CharSequence charSequence2 = charSequence;
                                nFinish(nativePtr2);
                                throw th;
                            }
                        }
                        int paraEnd3 = paragraphInfo2[paraEnd].paragraphEnd;
                        int firstWidth4 = outerWidth;
                        int restWidth3 = outerWidth;
                        if (spanned != null) {
                            try {
                                LeadingMarginSpan[] sp = (LeadingMarginSpan[]) getParagraphSpans(spanned, paraStart, paraEnd3, LeadingMarginSpan.class);
                                textDir2 = textDir5;
                                int firstWidthLineCount2 = 1;
                                int i8 = 0;
                                while (true) {
                                    paint2 = paint5;
                                    try {
                                        if (i8 >= sp.length) {
                                            break;
                                        }
                                        LeadingMarginSpan lms = sp[i8];
                                        bufStart3 = bufStart6;
                                        try {
                                            source3 = source6;
                                            firstWidth4 -= sp[i8].getLeadingMargin(true);
                                            restWidth3 -= sp[i8].getLeadingMargin(false);
                                            if (lms instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                                                firstWidthLineCount2 = Math.max(firstWidthLineCount2, ((LeadingMarginSpan.LeadingMarginSpan2) lms).getLeadingMarginLineCount());
                                            }
                                            i8++;
                                            paint5 = paint2;
                                            bufStart6 = bufStart3;
                                            source6 = source3;
                                        } catch (Throwable th2) {
                                            th = th2;
                                            int[] iArr2 = chooseHtv;
                                            StaticLayout staticLayout5 = staticLayout3;
                                            nativePtr2 = nativePtr;
                                            nFinish(nativePtr2);
                                            throw th;
                                        }
                                    } catch (Throwable th3) {
                                        th = th3;
                                        int[] iArr3 = chooseHtv;
                                        int i9 = v;
                                        PrecomputedText.ParagraphInfo[] paragraphInfoArr3 = paragraphInfo2;
                                        int i10 = bufEnd4;
                                        int i11 = bufStart6;
                                        CharSequence charSequence3 = source6;
                                        Paint.FontMetricsInt fontMetricsInt2 = fm2;
                                        StaticLayout staticLayout6 = staticLayout3;
                                        float f2 = ellipsizedWidth;
                                        AutoGrowArray.FloatArray floatArray3 = widths;
                                        LineBreaks lineBreaks9 = lineBreaks;
                                        TextUtils.TruncateAt truncateAt2 = ellipsize;
                                        TextDirectionHeuristic textDirectionHeuristic2 = textDir2;
                                        TextPaint textPaint2 = paint2;
                                        Spanned spanned9 = spanned;
                                        nativePtr2 = nativePtr;
                                        nFinish(nativePtr2);
                                        throw th;
                                    }
                                }
                                bufStart3 = bufStart6;
                                source3 = source6;
                                LineHeightSpan[] chooseHt2 = (LineHeightSpan[]) getParagraphSpans(spanned, paraStart, paraEnd3, LineHeightSpan.class);
                                if (chooseHt2.length == 0) {
                                    chooseHt2 = null;
                                } else {
                                    if (chooseHtv == null || chooseHtv.length < chooseHt2.length) {
                                        chooseHtv = ArrayUtils.newUnpaddedIntArray(chooseHt2.length);
                                    }
                                    for (int i12 = 0; i12 < chooseHt2.length; i12++) {
                                        int o = spanned.getSpanStart(chooseHt2[i12]);
                                        if (o < paraStart) {
                                            chooseHtv[i12] = staticLayout3.getLineTop(staticLayout3.getLineForOffset(o));
                                        } else {
                                            chooseHtv[i12] = v;
                                        }
                                    }
                                }
                                chooseHtv2 = chooseHtv;
                                chooseHt = chooseHt2;
                                firstWidthLineCount = firstWidthLineCount2;
                                firstWidth = firstWidth4;
                                restWidth = restWidth3;
                            } catch (Throwable th4) {
                                th = th4;
                                int[] iArr4 = chooseHtv;
                                int i13 = v;
                                PrecomputedText.ParagraphInfo[] paragraphInfoArr4 = paragraphInfo2;
                                TextDirectionHeuristic textDirectionHeuristic3 = textDir5;
                                TextPaint textPaint3 = paint5;
                                int i14 = bufEnd4;
                                int i15 = bufStart6;
                                CharSequence charSequence4 = source6;
                                Paint.FontMetricsInt fontMetricsInt3 = fm2;
                                StaticLayout staticLayout7 = staticLayout3;
                                float f3 = ellipsizedWidth;
                                AutoGrowArray.FloatArray floatArray4 = widths;
                                LineBreaks lineBreaks10 = lineBreaks;
                                TextUtils.TruncateAt truncateAt3 = ellipsize;
                                Spanned spanned10 = spanned;
                                nativePtr2 = nativePtr;
                                nFinish(nativePtr2);
                                throw th;
                            }
                        } else {
                            textDir2 = textDir5;
                            paint2 = paint5;
                            bufStart3 = bufStart6;
                            source3 = source6;
                            chooseHtv2 = chooseHtv;
                            firstWidthLineCount = 1;
                            firstWidth = firstWidth4;
                            restWidth = restWidth3;
                            chooseHt = null;
                        }
                        int[] variableTabStops = null;
                        if (spanned != null) {
                            try {
                                TabStopSpan[] spans = (TabStopSpan[]) getParagraphSpans(spanned, paraStart, paraEnd3, TabStopSpan.class);
                                if (spans.length > 0) {
                                    int[] stops = new int[spans.length];
                                    for (int i16 = 0; i16 < spans.length; i16++) {
                                        stops[i16] = spans[i16].getTabStop();
                                    }
                                    Arrays.sort(stops, 0, stops.length);
                                    variableTabStops = stops;
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                StaticLayout staticLayout52 = staticLayout3;
                                nativePtr2 = nativePtr;
                                nFinish(nativePtr2);
                                throw th;
                            }
                        }
                        int[] variableTabStops2 = variableTabStops;
                        try {
                            measuredPara = paragraphInfo2[paraEnd].measured;
                            chs = measuredPara.getChars();
                            spanEndCache = measuredPara.getSpanEndCache().getRawArray();
                            fmCache = measuredPara.getFontMetrics().getRawArray();
                            widths2 = widths;
                            try {
                                widths2.resize(chs.length);
                                v2 = v;
                                try {
                                    paragraphInfo3 = paragraphInfo2;
                                    spanned2 = spanned;
                                    lineBreaks2 = lineBreaks;
                                } catch (Throwable th6) {
                                    th = th6;
                                    PrecomputedText.ParagraphInfo[] paragraphInfoArr5 = paragraphInfo2;
                                    AutoGrowArray.FloatArray floatArray5 = widths2;
                                    int i17 = bufEnd4;
                                    Paint.FontMetricsInt fontMetricsInt4 = fm2;
                                    StaticLayout staticLayout8 = staticLayout3;
                                    float f4 = ellipsizedWidth;
                                    LineBreaks lineBreaks11 = lineBreaks;
                                    TextUtils.TruncateAt truncateAt4 = ellipsize;
                                    Spanned spanned11 = spanned;
                                    nativePtr2 = nativePtr;
                                    TextDirectionHeuristic textDirectionHeuristic4 = textDir2;
                                    TextPaint textPaint4 = paint2;
                                    int i18 = bufStart3;
                                    CharSequence charSequence5 = source3;
                                    nFinish(nativePtr2);
                                    throw th;
                                }
                            } catch (Throwable th7) {
                                th = th7;
                                int i19 = v;
                                PrecomputedText.ParagraphInfo[] paragraphInfoArr6 = paragraphInfo2;
                                AutoGrowArray.FloatArray floatArray6 = widths2;
                                int i20 = bufEnd4;
                                Paint.FontMetricsInt fontMetricsInt5 = fm2;
                                StaticLayout staticLayout9 = staticLayout3;
                                float f5 = ellipsizedWidth;
                                LineBreaks lineBreaks12 = lineBreaks;
                                TextUtils.TruncateAt truncateAt5 = ellipsize;
                                Spanned spanned12 = spanned;
                                nativePtr2 = nativePtr;
                                TextDirectionHeuristic textDirectionHeuristic5 = textDir2;
                                TextPaint textPaint5 = paint2;
                                int i21 = bufStart3;
                                CharSequence charSequence6 = source3;
                                nFinish(nativePtr2);
                                throw th;
                            }
                        } catch (Throwable th8) {
                            th = th8;
                            int i22 = v;
                            PrecomputedText.ParagraphInfo[] paragraphInfoArr7 = paragraphInfo2;
                            int i23 = bufEnd4;
                            Paint.FontMetricsInt fontMetricsInt6 = fm2;
                            StaticLayout staticLayout10 = staticLayout3;
                            float f6 = ellipsizedWidth;
                            AutoGrowArray.FloatArray floatArray7 = widths;
                            LineBreaks lineBreaks13 = lineBreaks;
                            TextUtils.TruncateAt truncateAt6 = ellipsize;
                            Spanned spanned13 = spanned;
                            nativePtr2 = nativePtr;
                            TextDirectionHeuristic textDirectionHeuristic6 = textDir2;
                            TextPaint textPaint6 = paint2;
                            int i24 = bufStart3;
                            CharSequence charSequence7 = source3;
                            nFinish(nativePtr2);
                            throw th;
                        }
                        try {
                            MeasuredParagraph measuredPara4 = measuredPara;
                            int restWidth4 = restWidth;
                            int firstWidth5 = firstWidth;
                            paraIndex = paraEnd;
                            int bufEnd5 = bufEnd4;
                            try {
                                int breakCount5 = nComputeLineBreaks(nativePtr, chs, measuredPara.getNativePtr(), paraEnd3 - paraStart, (float) firstWidth, firstWidthLineCount, (float) restWidth, variableTabStops2, 20, staticLayout3.mLineCount, lineBreaks2, lineBreaks2.breaks.length, lineBreaks2.breaks, lineBreaks2.widths, lineBreaks2.ascents, lineBreaks2.descents, lineBreaks2.flags, widths2.getRawArray());
                                int[] breaks = lineBreaks2.breaks;
                                float[] lineWidths = lineBreaks2.widths;
                                float[] ascents = lineBreaks2.ascents;
                                float[] descents = lineBreaks2.descents;
                                int[] flags = lineBreaks2.flags;
                                int remainingLineCount3 = staticLayout3.mMaximumVisibleLineCount - staticLayout3.mLineCount;
                                TextUtils.TruncateAt ellipsize6 = ellipsize;
                                if (ellipsize6 != null) {
                                    try {
                                        if (ellipsize6 == TextUtils.TruncateAt.END || (staticLayout3.mMaximumVisibleLineCount == 1 && ellipsize6 != TextUtils.TruncateAt.MARQUEE)) {
                                            z2 = true;
                                            boolean ellipsisMayBeApplied = z2;
                                            if (remainingLineCount3 > 0 && remainingLineCount3 < breakCount5 && ellipsisMayBeApplied) {
                                                float width = 0.0f;
                                                int flag = 0;
                                                i = remainingLineCount3 - 1;
                                                while (i < breakCount5) {
                                                    if (i == breakCount5 - 1) {
                                                        width += lineWidths[i];
                                                    } else {
                                                        for (int j = i == 0 ? 0 : breaks[i - 1]; j < breaks[i]; j++) {
                                                            width += widths2.get(j);
                                                        }
                                                    }
                                                    flag |= flags[i] & 536870912;
                                                    i++;
                                                }
                                                breaks[remainingLineCount3 - 1] = breaks[breakCount5 - 1];
                                                lineWidths[remainingLineCount3 - 1] = width;
                                                flags[remainingLineCount3 - 1] = flag;
                                                breakCount5 = remainingLineCount3;
                                            }
                                            int here = paraStart;
                                            int spanEndCacheIndex = 0;
                                            Locale loc5 = Locale.getDefault();
                                            int breakIndex3 = 0;
                                            int breakIndex4 = 0;
                                            int fmDescent3 = 0;
                                            int fmAscent3 = 0;
                                            int fmBottom2 = 0;
                                            int fmTop2 = 0;
                                            int here2 = here;
                                            spanStart = paraStart;
                                            while (spanStart < paraEnd3) {
                                                int spanEndCacheIndex2 = spanEndCacheIndex + 1;
                                                int spanEnd3 = spanEndCache[spanEndCacheIndex];
                                                boolean z5 = false;
                                                int here3 = here2;
                                                fm2.top = fmCache[(breakIndex4 * 4) + 0];
                                                boolean z6 = true;
                                                fm2.bottom = fmCache[(breakIndex4 * 4) + 1];
                                                char c2 = 2;
                                                fm2.ascent = fmCache[(breakIndex4 * 4) + 2];
                                                fm2.descent = fmCache[(breakIndex4 * 4) + 3];
                                                int fmCacheIndex = breakIndex4 + 1;
                                                if (fm2.top < fmTop2) {
                                                    fmTop2 = fm2.top;
                                                }
                                                if (fm2.ascent < fmAscent3) {
                                                    fmAscent3 = fm2.ascent;
                                                }
                                                if (fm2.descent > fmDescent3) {
                                                    fmDescent3 = fm2.descent;
                                                }
                                                try {
                                                    if (fm2.bottom > fmBottom2) {
                                                        fmBottom2 = fm2.bottom;
                                                    }
                                                    paraEnd2 = paraEnd3;
                                                    fm4 = fm2;
                                                    loc = loc5;
                                                } catch (Throwable th9) {
                                                    th = th9;
                                                    AutoGrowArray.FloatArray floatArray8 = widths2;
                                                    TextUtils.TruncateAt truncateAt7 = ellipsize6;
                                                    LineBreaks lineBreaks14 = lineBreaks2;
                                                    Paint.FontMetricsInt fontMetricsInt7 = fm2;
                                                    StaticLayout staticLayout11 = staticLayout3;
                                                    nativePtr2 = nativePtr;
                                                    float f7 = ellipsizedWidth;
                                                    Spanned spanned14 = spanned2;
                                                    PrecomputedText.ParagraphInfo[] paragraphInfoArr8 = paragraphInfo3;
                                                    TextDirectionHeuristic textDirectionHeuristic7 = textDir2;
                                                    TextPaint textPaint7 = paint2;
                                                    int i25 = bufStart3;
                                                    CharSequence charSequence8 = source3;
                                                    int i26 = bufEnd5;
                                                    nFinish(nativePtr2);
                                                    throw th;
                                                }
                                                try {
                                                    if ("bo".equals(loc.getLanguage())) {
                                                        fmTop2 -= 5;
                                                        fmAscent3 -= 5;
                                                        fmBottom2 += 5;
                                                        fmDescent3 += 5;
                                                    }
                                                    int breakIndex5 = breakIndex3;
                                                    while (breakIndex5 < breakCount5) {
                                                        try {
                                                            if (breaks[breakIndex5] + paraStart >= spanStart) {
                                                                break;
                                                            }
                                                            breakIndex5++;
                                                        } catch (Throwable th10) {
                                                            th = th10;
                                                            AutoGrowArray.FloatArray floatArray9 = widths2;
                                                            TextUtils.TruncateAt truncateAt8 = ellipsize6;
                                                            LineBreaks lineBreaks15 = lineBreaks2;
                                                            StaticLayout staticLayout12 = staticLayout3;
                                                            nativePtr2 = nativePtr;
                                                            float f8 = ellipsizedWidth;
                                                            TextDirectionHeuristic textDirectionHeuristic8 = textDir2;
                                                            TextPaint textPaint8 = paint2;
                                                            int i27 = bufStart3;
                                                            CharSequence charSequence9 = source3;
                                                            Spanned spanned15 = spanned2;
                                                            PrecomputedText.ParagraphInfo[] paragraphInfoArr9 = paragraphInfo3;
                                                            int i28 = bufEnd5;
                                                            nFinish(nativePtr2);
                                                            throw th;
                                                        }
                                                    }
                                                    int fmTop3 = fmTop2;
                                                    int fmBottom3 = fmBottom2;
                                                    int fmDescent4 = fmDescent3;
                                                    int fmDescent5 = breakIndex5;
                                                    while (true) {
                                                        if (fmDescent5 >= breakCount5) {
                                                            breakCount = breakCount5;
                                                            int i29 = spanStart;
                                                            paraStart2 = paraStart;
                                                            fmDescent = fmDescent4;
                                                            widths4 = widths2;
                                                            chs2 = chs;
                                                            fmAscent = fmAscent3;
                                                            breakIndex = fmDescent5;
                                                            ellipsize3 = ellipsize6;
                                                            remainingLineCount = remainingLineCount3;
                                                            lineBreaks4 = lineBreaks2;
                                                            loc2 = loc;
                                                            staticLayout2 = staticLayout3;
                                                            boolean z7 = z6;
                                                            boolean z8 = z5;
                                                            char c3 = c2;
                                                            nativePtr4 = nativePtr;
                                                            ellipsizedWidth3 = ellipsizedWidth;
                                                            textDir3 = textDir2;
                                                            paint3 = paint2;
                                                            bufStart4 = bufStart3;
                                                            source4 = source3;
                                                            spanned4 = spanned2;
                                                            paragraphInfo5 = paragraphInfo3;
                                                            measuredPara2 = measuredPara4;
                                                            ellipsizedWidth4 = restWidth4;
                                                            firstWidth2 = firstWidth5;
                                                            bufEnd2 = bufEnd5;
                                                            breakCount2 = spanEnd3;
                                                            break;
                                                        }
                                                        int spanEnd4 = spanEnd3;
                                                        if (breaks[fmDescent5] + paraStart > spanEnd4) {
                                                            breakCount = breakCount5;
                                                            int i30 = spanStart;
                                                            paraStart2 = paraStart;
                                                            fmDescent = fmDescent4;
                                                            breakCount2 = spanEnd4;
                                                            widths4 = widths2;
                                                            chs2 = chs;
                                                            fmAscent = fmAscent3;
                                                            breakIndex = fmDescent5;
                                                            ellipsize3 = ellipsize6;
                                                            remainingLineCount = remainingLineCount3;
                                                            lineBreaks4 = lineBreaks2;
                                                            loc2 = loc;
                                                            staticLayout2 = staticLayout3;
                                                            boolean z9 = z6;
                                                            boolean z10 = z5;
                                                            char c4 = c2;
                                                            nativePtr4 = nativePtr;
                                                            ellipsizedWidth3 = ellipsizedWidth;
                                                            textDir3 = textDir2;
                                                            paint3 = paint2;
                                                            bufStart4 = bufStart3;
                                                            source4 = source3;
                                                            spanned4 = spanned2;
                                                            paragraphInfo5 = paragraphInfo3;
                                                            measuredPara2 = measuredPara4;
                                                            ellipsizedWidth4 = restWidth4;
                                                            firstWidth2 = firstWidth5;
                                                            bufEnd2 = bufEnd5;
                                                            break;
                                                        }
                                                        int endPos3 = breaks[fmDescent5] + paraStart;
                                                        int bufEnd6 = bufEnd5;
                                                        boolean moreChars = endPos3 < bufEnd6 ? z6 : z5;
                                                        if (fallbackLineSpacing) {
                                                            breakCount3 = breakCount5;
                                                            try {
                                                                breakCount4 = Math.min(fmAscent3, Math.round(ascents[fmDescent5]));
                                                            } catch (Throwable th11) {
                                                                th = th11;
                                                                int i31 = bufEnd6;
                                                                AutoGrowArray.FloatArray floatArray10 = widths2;
                                                                TextUtils.TruncateAt truncateAt9 = ellipsize6;
                                                                LineBreaks lineBreaks16 = lineBreaks2;
                                                                StaticLayout staticLayout13 = staticLayout3;
                                                                nativePtr2 = nativePtr;
                                                                nFinish(nativePtr2);
                                                                throw th;
                                                            }
                                                        } else {
                                                            breakCount3 = breakCount5;
                                                            breakCount4 = fmAscent3;
                                                        }
                                                        int bufEnd7 = bufEnd6;
                                                        int ascent = breakCount4;
                                                        if (fallbackLineSpacing) {
                                                            try {
                                                                spanEnd = Math.max(fmDescent4, Math.round(descents[fmDescent5]));
                                                            } catch (Throwable th12) {
                                                                th = th12;
                                                                AutoGrowArray.FloatArray floatArray11 = widths2;
                                                                TextUtils.TruncateAt truncateAt10 = ellipsize6;
                                                                LineBreaks lineBreaks17 = lineBreaks2;
                                                                StaticLayout staticLayout14 = staticLayout3;
                                                                int i32 = bufEnd7;
                                                                nativePtr2 = nativePtr;
                                                                nFinish(nativePtr2);
                                                                throw th;
                                                            }
                                                        } else {
                                                            spanEnd = fmDescent4;
                                                        }
                                                        int spanEnd5 = spanEnd4;
                                                        int paraEnd4 = paraEnd2;
                                                        try {
                                                            spanStart2 = spanStart;
                                                            endPos = endPos3;
                                                            paraStart3 = paraStart;
                                                            int i33 = fmDescent4;
                                                            paragraphInfo6 = paragraphInfo3;
                                                            widths5 = widths2;
                                                            z3 = z6;
                                                            z4 = z5;
                                                            ellipsizedWidth5 = ellipsizedWidth;
                                                            chs3 = chs;
                                                            textDir4 = textDir2;
                                                            int i34 = fmAscent3;
                                                            paint4 = paint2;
                                                            measuredPara3 = measuredPara4;
                                                            breakIndex2 = fmDescent5;
                                                            bufEnd3 = bufEnd7;
                                                            ellipsize4 = ellipsize6;
                                                            bufStart5 = bufStart3;
                                                            restWidth2 = restWidth4;
                                                            remainingLineCount2 = remainingLineCount3;
                                                            source5 = source3;
                                                            firstWidth3 = firstWidth5;
                                                            lineBreaks5 = lineBreaks2;
                                                            c = c2;
                                                            spanned5 = spanned2;
                                                            loc3 = loc;
                                                            fm5 = fm4;
                                                        } catch (Throwable th13) {
                                                            th = th13;
                                                            AutoGrowArray.FloatArray floatArray12 = widths2;
                                                            TextUtils.TruncateAt truncateAt11 = ellipsize6;
                                                            LineBreaks lineBreaks18 = lineBreaks2;
                                                            StaticLayout staticLayout15 = staticLayout3;
                                                            nativePtr2 = nativePtr;
                                                            float f9 = ellipsizedWidth;
                                                            Spanned spanned16 = spanned2;
                                                            PrecomputedText.ParagraphInfo[] paragraphInfoArr10 = paragraphInfo3;
                                                            Paint.FontMetricsInt fontMetricsInt8 = fm4;
                                                            int i35 = bufEnd7;
                                                            TextDirectionHeuristic textDirectionHeuristic9 = textDir2;
                                                            TextPaint textPaint9 = paint2;
                                                            int i36 = bufStart3;
                                                            CharSequence charSequence10 = source3;
                                                            nFinish(nativePtr2);
                                                            throw th;
                                                        }
                                                        try {
                                                            v2 = staticLayout3.out(source3, here3, endPos, ascent, spanEnd, fmTop3, fmBottom3, v2, spacingmult, spacingadd, chooseHt, chooseHtv2, fm5, flags[fmDescent5], needMultiply, measuredPara3, bufEnd7, includepad, trackpad, addLastLineSpacing, chs3, widths2.getRawArray(), paraStart3, ellipsize4, ellipsizedWidth5, lineWidths[fmDescent5], paint4, moreChars);
                                                            spanEnd2 = spanEnd5;
                                                            endPos2 = endPos;
                                                            if (endPos2 < spanEnd2) {
                                                                fm6 = fm5;
                                                                try {
                                                                    fmTop = fm6.top;
                                                                    fmBottom = fm6.bottom;
                                                                    fmAscent2 = fm6.ascent;
                                                                    fmDescent2 = fm6.descent;
                                                                } catch (Throwable th14) {
                                                                    th = th14;
                                                                    nativePtr2 = nativePtr;
                                                                    TextDirectionHeuristic textDirectionHeuristic10 = textDir4;
                                                                    TextPaint textPaint10 = paint4;
                                                                    int i37 = bufEnd3;
                                                                    int i38 = bufStart5;
                                                                    CharSequence charSequence11 = source5;
                                                                }
                                                            } else {
                                                                fm6 = fm5;
                                                                fmDescent2 = z4;
                                                                fmAscent2 = z4;
                                                                fmBottom = z4;
                                                                fmTop = z4;
                                                            }
                                                            loc4 = loc3;
                                                        } catch (Throwable th15) {
                                                            th = th15;
                                                            nativePtr2 = nativePtr;
                                                            Paint.FontMetricsInt fontMetricsInt9 = fm5;
                                                            TextDirectionHeuristic textDirectionHeuristic11 = textDir4;
                                                            TextPaint textPaint11 = paint4;
                                                            int i39 = bufEnd3;
                                                            int i40 = bufStart5;
                                                            CharSequence charSequence12 = source5;
                                                            nFinish(nativePtr2);
                                                            throw th;
                                                        }
                                                        try {
                                                            if ("bo".equals(loc4.getLanguage())) {
                                                                fmTop -= 5;
                                                                fmAscent2 -= 5;
                                                                fmBottom += 5;
                                                                fmDescent2 += 5;
                                                            }
                                                            fmTop3 = fmTop;
                                                            fmBottom3 = fmBottom;
                                                            fmAscent3 = fmAscent2;
                                                            fmDescent4 = fmDescent2;
                                                            here3 = endPos2;
                                                            fmDescent5 = breakIndex2 + 1;
                                                            try {
                                                                if (this.mLineCount < this.mMaximumVisibleLineCount || !this.mEllipsized) {
                                                                    spanEnd3 = spanEnd2;
                                                                    fm4 = fm6;
                                                                    staticLayout3 = this;
                                                                    loc = loc4;
                                                                    nativePtr = nativePtr;
                                                                    spanStart = spanStart2;
                                                                    paraStart = paraStart3;
                                                                    lineBreaks2 = lineBreaks5;
                                                                    paragraphInfo3 = paragraphInfo6;
                                                                    widths2 = widths5;
                                                                    z6 = z3;
                                                                    z5 = z4;
                                                                    chs = chs3;
                                                                    measuredPara4 = measuredPara3;
                                                                    ellipsize6 = ellipsize4;
                                                                    restWidth4 = restWidth2;
                                                                    firstWidth5 = firstWidth3;
                                                                    remainingLineCount3 = remainingLineCount2;
                                                                    spanned2 = spanned5;
                                                                    c2 = c;
                                                                    breakCount5 = breakCount3;
                                                                    paraEnd2 = paraEnd4;
                                                                    textDir2 = textDir4;
                                                                    paint2 = paint4;
                                                                    bufEnd5 = bufEnd3;
                                                                    bufStart3 = bufStart5;
                                                                    source3 = source5;
                                                                    ellipsizedWidth = ellipsizedWidth5;
                                                                } else {
                                                                    nFinish(nativePtr);
                                                                    return;
                                                                }
                                                            } catch (Throwable th16) {
                                                                th = th16;
                                                                nativePtr5 = nativePtr;
                                                                nFinish(nativePtr2);
                                                                throw th;
                                                            }
                                                        } catch (Throwable th17) {
                                                            th = th17;
                                                            nativePtr5 = nativePtr;
                                                            nFinish(nativePtr2);
                                                            throw th;
                                                        }
                                                    }
                                                    spanStart = breakCount2;
                                                    fm2 = fm4;
                                                    staticLayout3 = staticLayout2;
                                                    loc5 = loc2;
                                                    nativePtr = nativePtr4;
                                                    spanEndCacheIndex = spanEndCacheIndex2;
                                                    breakIndex4 = fmCacheIndex;
                                                    fmTop2 = fmTop3;
                                                    fmBottom2 = fmBottom3;
                                                    paraStart = paraStart2;
                                                    lineBreaks2 = lineBreaks4;
                                                    paragraphInfo3 = paragraphInfo5;
                                                    fmDescent3 = fmDescent;
                                                    widths2 = widths4;
                                                    chs = chs2;
                                                    measuredPara4 = measuredPara2;
                                                    fmAscent3 = fmAscent;
                                                    breakIndex3 = breakIndex;
                                                    ellipsize6 = ellipsize3;
                                                    restWidth4 = ellipsizedWidth4;
                                                    firstWidth5 = firstWidth2;
                                                    remainingLineCount3 = remainingLineCount;
                                                    spanned2 = spanned4;
                                                    here2 = here3;
                                                    breakCount5 = breakCount;
                                                    paraEnd3 = paraEnd2;
                                                    textDir2 = textDir3;
                                                    paint2 = paint3;
                                                    bufEnd5 = bufEnd2;
                                                    bufStart3 = bufStart4;
                                                    source3 = source4;
                                                    ellipsizedWidth = ellipsizedWidth3;
                                                } catch (Throwable th18) {
                                                    th = th18;
                                                    AutoGrowArray.FloatArray floatArray13 = widths2;
                                                    TextUtils.TruncateAt truncateAt12 = ellipsize6;
                                                    LineBreaks lineBreaks19 = lineBreaks2;
                                                    StaticLayout staticLayout16 = staticLayout3;
                                                    nativePtr2 = nativePtr;
                                                    float f10 = ellipsizedWidth;
                                                    Spanned spanned17 = spanned2;
                                                    PrecomputedText.ParagraphInfo[] paragraphInfoArr11 = paragraphInfo3;
                                                    Paint.FontMetricsInt fontMetricsInt10 = fm4;
                                                    TextDirectionHeuristic textDirectionHeuristic12 = textDir2;
                                                    TextPaint textPaint12 = paint2;
                                                    int i41 = bufStart3;
                                                    CharSequence charSequence13 = source3;
                                                    int i42 = bufEnd5;
                                                    nFinish(nativePtr2);
                                                    throw th;
                                                }
                                            }
                                            int i43 = breakCount5;
                                            int i44 = here2;
                                            int i45 = paraStart;
                                            widths3 = widths2;
                                            char[] cArr = chs;
                                            ellipsize2 = ellipsize6;
                                            int i46 = remainingLineCount3;
                                            lineBreaks3 = lineBreaks2;
                                            fm3 = fm2;
                                            staticLayout = staticLayout3;
                                            nativePtr3 = nativePtr;
                                            ellipsizedWidth2 = ellipsizedWidth;
                                            textDir = textDir2;
                                            paint = paint2;
                                            bufStart = bufStart3;
                                            source = source3;
                                            spanned3 = spanned2;
                                            paragraphInfo4 = paragraphInfo3;
                                            MeasuredParagraph measuredParagraph = measuredPara4;
                                            int ellipsizedWidth7 = restWidth4;
                                            int i47 = firstWidth5;
                                            Locale locale = loc5;
                                            bufEnd = bufEnd5;
                                            if (paraEnd3 != bufEnd) {
                                                chooseHtv = chooseHtv2;
                                                break;
                                            }
                                            paraEnd = paraIndex + 1;
                                            fm2 = fm3;
                                            bufEnd4 = bufEnd;
                                            staticLayout3 = staticLayout;
                                            nativePtr = nativePtr3;
                                            chooseHtv = chooseHtv2;
                                            paragraphInfo2 = paragraphInfo4;
                                            ellipsizedWidth = ellipsizedWidth2;
                                            widths = widths3;
                                            ellipsize = ellipsize2;
                                            spanned = spanned3;
                                            v = v2;
                                            textDir5 = textDir;
                                            paint5 = paint;
                                            bufStart6 = bufStart;
                                            source6 = source;
                                            lineBreaks = lineBreaks3;
                                        }
                                    } catch (Throwable th19) {
                                        th = th19;
                                        AutoGrowArray.FloatArray floatArray14 = widths2;
                                        TextUtils.TruncateAt truncateAt13 = ellipsize6;
                                        LineBreaks lineBreaks20 = lineBreaks2;
                                        Paint.FontMetricsInt fontMetricsInt11 = fm2;
                                        StaticLayout staticLayout17 = staticLayout3;
                                        nativePtr2 = nativePtr;
                                        float f11 = ellipsizedWidth;
                                        TextDirectionHeuristic textDirectionHeuristic13 = textDir2;
                                        TextPaint textPaint13 = paint2;
                                        int i48 = bufStart3;
                                        CharSequence charSequence14 = source3;
                                        Spanned spanned18 = spanned2;
                                        PrecomputedText.ParagraphInfo[] paragraphInfoArr12 = paragraphInfo3;
                                        int i49 = bufEnd5;
                                        nFinish(nativePtr2);
                                        throw th;
                                    }
                                }
                                z2 = false;
                                boolean ellipsisMayBeApplied2 = z2;
                                float width2 = 0.0f;
                                int flag2 = 0;
                                i = remainingLineCount3 - 1;
                                while (i < breakCount5) {
                                }
                                breaks[remainingLineCount3 - 1] = breaks[breakCount5 - 1];
                                lineWidths[remainingLineCount3 - 1] = width2;
                                flags[remainingLineCount3 - 1] = flag2;
                                breakCount5 = remainingLineCount3;
                                int here4 = paraStart;
                                int spanEndCacheIndex3 = 0;
                                try {
                                    Locale loc52 = Locale.getDefault();
                                    int breakIndex32 = 0;
                                    int breakIndex42 = 0;
                                    int fmDescent32 = 0;
                                    int fmAscent32 = 0;
                                    int fmBottom22 = 0;
                                    int fmTop22 = 0;
                                    int here22 = here4;
                                    spanStart = paraStart;
                                    while (spanStart < paraEnd3) {
                                    }
                                    int i432 = breakCount5;
                                    int i442 = here22;
                                    int i452 = paraStart;
                                    widths3 = widths2;
                                    char[] cArr2 = chs;
                                    ellipsize2 = ellipsize6;
                                    int i462 = remainingLineCount3;
                                    lineBreaks3 = lineBreaks2;
                                    fm3 = fm2;
                                    staticLayout = staticLayout3;
                                    nativePtr3 = nativePtr;
                                    ellipsizedWidth2 = ellipsizedWidth;
                                    textDir = textDir2;
                                    paint = paint2;
                                    bufStart = bufStart3;
                                    source = source3;
                                    spanned3 = spanned2;
                                    paragraphInfo4 = paragraphInfo3;
                                    MeasuredParagraph measuredParagraph2 = measuredPara4;
                                    int ellipsizedWidth72 = restWidth4;
                                    int i472 = firstWidth5;
                                    Locale locale2 = loc52;
                                    bufEnd = bufEnd5;
                                    if (paraEnd3 != bufEnd) {
                                    }
                                } catch (Throwable th20) {
                                    th = th20;
                                    AutoGrowArray.FloatArray floatArray15 = widths2;
                                    TextUtils.TruncateAt truncateAt14 = ellipsize6;
                                    LineBreaks lineBreaks21 = lineBreaks2;
                                    Paint.FontMetricsInt fontMetricsInt12 = fm2;
                                    StaticLayout staticLayout18 = staticLayout3;
                                    nativePtr2 = nativePtr;
                                    float f12 = ellipsizedWidth;
                                    Spanned spanned19 = spanned2;
                                    PrecomputedText.ParagraphInfo[] paragraphInfoArr13 = paragraphInfo3;
                                    int i50 = bufEnd5;
                                    TextDirectionHeuristic textDirectionHeuristic14 = textDir2;
                                    TextPaint textPaint14 = paint2;
                                    int i51 = bufStart3;
                                    CharSequence charSequence15 = source3;
                                    nFinish(nativePtr2);
                                    throw th;
                                }
                            } catch (Throwable th21) {
                                th = th21;
                                AutoGrowArray.FloatArray floatArray16 = widths2;
                                LineBreaks lineBreaks22 = lineBreaks2;
                                Paint.FontMetricsInt fontMetricsInt13 = fm2;
                                StaticLayout staticLayout19 = staticLayout3;
                                nativePtr2 = nativePtr;
                                float f13 = ellipsizedWidth;
                                TextUtils.TruncateAt truncateAt15 = ellipsize;
                                Spanned spanned20 = spanned2;
                                PrecomputedText.ParagraphInfo[] paragraphInfoArr14 = paragraphInfo3;
                                int i52 = bufEnd5;
                                TextDirectionHeuristic textDirectionHeuristic15 = textDir2;
                                TextPaint textPaint15 = paint2;
                                int i53 = bufStart3;
                                CharSequence charSequence16 = source3;
                                nFinish(nativePtr2);
                                throw th;
                            }
                        } catch (Throwable th22) {
                            th = th22;
                            AutoGrowArray.FloatArray floatArray17 = widths2;
                            int i54 = bufEnd4;
                            LineBreaks lineBreaks23 = lineBreaks2;
                            Paint.FontMetricsInt fontMetricsInt14 = fm2;
                            StaticLayout staticLayout20 = staticLayout3;
                            nativePtr2 = nativePtr;
                            float f14 = ellipsizedWidth;
                            TextUtils.TruncateAt truncateAt16 = ellipsize;
                            Spanned spanned21 = spanned2;
                            PrecomputedText.ParagraphInfo[] paragraphInfoArr15 = paragraphInfo3;
                            TextDirectionHeuristic textDirectionHeuristic16 = textDir2;
                            TextPaint textPaint16 = paint2;
                            int i55 = bufStart3;
                            CharSequence charSequence17 = source3;
                            nFinish(nativePtr2);
                            throw th;
                        }
                    } catch (Throwable th23) {
                        th = th23;
                        int i56 = v;
                        PrecomputedText.ParagraphInfo[] paragraphInfoArr16 = paragraphInfo2;
                        TextDirectionHeuristic textDirectionHeuristic17 = textDir5;
                        TextPaint textPaint17 = paint5;
                        int i57 = bufEnd4;
                        int i58 = bufStart6;
                        CharSequence charSequence18 = source6;
                        Paint.FontMetricsInt fontMetricsInt15 = fm2;
                        StaticLayout staticLayout21 = staticLayout3;
                        float f15 = ellipsizedWidth;
                        AutoGrowArray.FloatArray floatArray18 = widths;
                        LineBreaks lineBreaks24 = lineBreaks;
                        TextUtils.TruncateAt truncateAt17 = ellipsize;
                        Spanned spanned22 = spanned;
                        nativePtr2 = nativePtr;
                        int[] iArr5 = chooseHtv;
                        nFinish(nativePtr2);
                        throw th;
                    }
                }
                bufStart2 = bufStart;
                if (bufEnd == bufStart2) {
                    source2 = source;
                    try {
                        if (source2.charAt(bufEnd - 1) != 10) {
                            TextDirectionHeuristic textDirectionHeuristic18 = textDir;
                            TextPaint textPaint18 = paint;
                            nFinish(nativePtr2);
                        }
                    } catch (Throwable th24) {
                        th = th24;
                        int[] iArr6 = chooseHtv;
                        TextDirectionHeuristic textDirectionHeuristic19 = textDir;
                        TextPaint textPaint19 = paint;
                        nFinish(nativePtr2);
                        throw th;
                    }
                } else {
                    source2 = source;
                }
                if (staticLayout.mLineCount >= staticLayout.mMaximumVisibleLineCount) {
                    try {
                        MeasuredParagraph measuredPara5 = MeasuredParagraph.buildForBidi(source2, bufEnd, bufEnd, textDir, null);
                        TextPaint paint6 = paint;
                        try {
                            paint6.getFontMetricsInt(fm3);
                            int v3 = staticLayout.out(source2, bufEnd, bufEnd, fm3.ascent, fm3.descent, fm3.top, fm3.bottom, v2, spacingmult, spacingadd, null, null, fm3, 0, needMultiply, measuredPara5, bufEnd, includepad, trackpad, addLastLineSpacing, null, null, bufStart2, ellipsize2, ellipsizedWidth2, 0.0f, paint6, false);
                        } catch (Throwable th25) {
                            th = th25;
                            int[] iArr7 = chooseHtv;
                            nFinish(nativePtr2);
                            throw th;
                        }
                    } catch (Throwable th26) {
                        th = th26;
                        TextPaint textPaint20 = paint;
                        int[] iArr8 = chooseHtv;
                        nFinish(nativePtr2);
                        throw th;
                    }
                } else {
                    TextPaint textPaint21 = paint;
                }
                nFinish(nativePtr2);
            }
        } else {
            nativePtr = nativePtr6;
            widths = widths6;
            lineBreaks = lineBreaks6;
            ellipsize = ellipsize5;
            ellipsizedWidth = ellipsizedWidth6;
            paragraphInfo7 = null;
            HashMap<String, Integer> hashMap2 = languageMap;
            fm2 = fm;
            z = false;
            spanned = spanned6;
        }
        paragraphInfo = paragraphInfo7;
        if (paragraphInfo == null) {
        }
        paragraphInfo2 = paragraphInfo;
        paraEnd = z;
        v = 0;
        chooseHtv = null;
        while (true) {
            if (paraEnd < paragraphInfo2.length) {
            }
            paraEnd = paraIndex + 1;
            fm2 = fm3;
            bufEnd4 = bufEnd;
            staticLayout3 = staticLayout;
            nativePtr = nativePtr3;
            chooseHtv = chooseHtv2;
            paragraphInfo2 = paragraphInfo4;
            ellipsizedWidth = ellipsizedWidth2;
            widths = widths3;
            ellipsize = ellipsize2;
            spanned = spanned3;
            v = v2;
            textDir5 = textDir;
            paint5 = paint;
            bufStart6 = bufStart;
            source6 = source;
            lineBreaks = lineBreaks3;
        }
        bufStart2 = bufStart;
        if (bufEnd == bufStart2) {
        }
        try {
            if (staticLayout.mLineCount >= staticLayout.mMaximumVisibleLineCount) {
            }
            nFinish(nativePtr2);
        } catch (Throwable th27) {
            th = th27;
            TextDirectionHeuristic textDirectionHeuristic20 = textDir;
            TextPaint textPaint22 = paint;
            int[] iArr9 = chooseHtv;
            nFinish(nativePtr2);
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x012f  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0147  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01e6  */
    private int out(CharSequence text, int start, int end, int above, int below, int top, int bottom, int v, float spacingmult, float spacingadd, LineHeightSpan[] chooseHt, int[] chooseHtv, Paint.FontMetricsInt fm, int flags, boolean needMultiply, MeasuredParagraph measured, int bufEnd, boolean includePad, boolean trackPad, boolean addLastLineLineSpacing, char[] chs, float[] widths, int widthStart, TextUtils.TruncateAt ellipsize, float ellipsisWidth, float textWidth, TextPaint paint, boolean moreChars) {
        int bottom2;
        int top2;
        int below2;
        int above2;
        boolean z;
        int j;
        TextUtils.TruncateAt truncateAt;
        boolean z2;
        int i;
        int i2;
        boolean lastLine;
        int extra;
        int extra2;
        boolean lastCharIsNewLine;
        boolean z3;
        boolean z4;
        boolean z5;
        TextUtils.TruncateAt truncateAt2;
        int want;
        boolean z6;
        int j2;
        TextUtils.TruncateAt truncateAt3;
        int i3 = start;
        int i4 = end;
        LineHeightSpan[] lineHeightSpanArr = chooseHt;
        Paint.FontMetricsInt fontMetricsInt = fm;
        int i5 = bufEnd;
        int i6 = widthStart;
        TextUtils.TruncateAt truncateAt4 = ellipsize;
        int j3 = this.mLineCount;
        int off = j3 * this.mColumns;
        boolean z7 = true;
        int want2 = off + this.mColumns + 1;
        int[] lines = this.mLines;
        int dir = measured.getParagraphDir();
        if (want2 >= lines.length) {
            int[] grow = ArrayUtils.newUnpaddedIntArray(GrowingArrayUtils.growSize(want2));
            System.arraycopy(lines, 0, grow, 0, lines.length);
            this.mLines = grow;
            lines = grow;
        }
        int[] lines2 = lines;
        if (j3 >= this.mLineDirections.length) {
            Layout.Directions[] grow2 = (Layout.Directions[]) ArrayUtils.newUnpaddedArray(Layout.Directions.class, GrowingArrayUtils.growSize(j3));
            System.arraycopy(this.mLineDirections, 0, grow2, 0, this.mLineDirections.length);
            this.mLineDirections = grow2;
        }
        if (lineHeightSpanArr != null) {
            fontMetricsInt.ascent = above;
            fontMetricsInt.descent = below;
            fontMetricsInt.top = top;
            fontMetricsInt.bottom = bottom;
            int j4 = 0;
            while (true) {
                int i7 = j4;
                if (i7 >= lineHeightSpanArr.length) {
                    break;
                }
                if (lineHeightSpanArr[i7] instanceof LineHeightSpan.WithDensity) {
                    z6 = false;
                    want = want2;
                    boolean z8 = z7;
                    j2 = j3;
                    truncateAt3 = truncateAt4;
                    ((LineHeightSpan.WithDensity) lineHeightSpanArr[i7]).chooseHeight(text, i3, i4, chooseHtv[i7], v, fontMetricsInt, paint);
                } else {
                    want = want2;
                    j2 = j3;
                    truncateAt3 = truncateAt4;
                    z6 = false;
                    lineHeightSpanArr[i7].chooseHeight(text, i3, i4, chooseHtv[i7], v, fontMetricsInt);
                }
                int i8 = i7 + 1;
                int i9 = above;
                int i10 = below;
                truncateAt4 = truncateAt3;
                j3 = j2;
                boolean z9 = z6;
                want2 = want;
                z7 = true;
                int i11 = bottom;
                int i12 = widthStart;
                j4 = i8;
                int i13 = top;
            }
            j = j3;
            truncateAt = truncateAt4;
            z = false;
            above2 = fontMetricsInt.ascent;
            below2 = fontMetricsInt.descent;
            top2 = fontMetricsInt.top;
            bottom2 = fontMetricsInt.bottom;
        } else {
            z = false;
            int i14 = want2;
            j = j3;
            truncateAt = truncateAt4;
            above2 = above;
            below2 = below;
            top2 = top;
            bottom2 = bottom;
        }
        boolean firstLine = j == 0 ? true : z;
        boolean currentLineIsTheLastVisibleOne = j + 1 == this.mMaximumVisibleLineCount ? true : z;
        if (truncateAt != null) {
            if (moreChars) {
                z3 = true;
                if (this.mLineCount + 1 == this.mMaximumVisibleLineCount) {
                    z4 = true;
                    z5 = z3;
                    truncateAt2 = truncateAt;
                    int i15 = widthStart;
                    boolean forceEllipsis = z4;
                    if (!(((((this.mMaximumVisibleLineCount == z5 || !moreChars) && (!firstLine || moreChars)) || truncateAt2 == TextUtils.TruncateAt.MARQUEE) && (firstLine || ((!currentLineIsTheLastVisibleOne && moreChars) || truncateAt2 != TextUtils.TruncateAt.END))) ? z : z5)) {
                        z2 = z5;
                        i2 = i15;
                        i = bufEnd;
                        calculateEllipsis(i3, i4, widths, i15, ellipsisWidth, truncateAt2, j, textWidth, paint, forceEllipsis);
                    } else {
                        z2 = z5;
                        i2 = i15;
                        i = bufEnd;
                    }
                }
            } else {
                z3 = true;
            }
            z4 = z;
            z5 = z3;
            truncateAt2 = truncateAt;
            int i152 = widthStart;
            boolean forceEllipsis2 = z4;
            if (!(((((this.mMaximumVisibleLineCount == z5 || !moreChars) && (!firstLine || moreChars)) || truncateAt2 == TextUtils.TruncateAt.MARQUEE) && (firstLine || ((!currentLineIsTheLastVisibleOne && moreChars) || truncateAt2 != TextUtils.TruncateAt.END))) ? z : z5)) {
            }
        } else {
            i2 = widthStart;
            i = bufEnd;
            z2 = true;
        }
        if (this.mEllipsized) {
            lastLine = true;
            CharSequence charSequence = text;
        } else {
            if (i2 == i || i <= 0) {
                CharSequence charSequence2 = text;
            } else {
                if (text.charAt(i - 1) == 10) {
                    lastCharIsNewLine = z2;
                    if (i4 != i && !lastCharIsNewLine) {
                        lastLine = true;
                    } else if (i3 == i || !lastCharIsNewLine) {
                        lastLine = z;
                    } else {
                        lastLine = true;
                    }
                }
            }
            lastCharIsNewLine = z;
            if (i4 != i) {
            }
            if (i3 == i) {
            }
            lastLine = z;
        }
        boolean lastLine2 = lastLine;
        if (firstLine) {
            if (trackPad) {
                this.mTopPadding = top2 - above2;
            }
            if (includePad) {
                above2 = top2;
            }
        }
        if (lastLine2) {
            if (trackPad) {
                this.mBottomPadding = bottom2 - below2;
            }
            if (includePad) {
                below2 = bottom2;
            }
        }
        if (!needMultiply) {
        } else if (addLastLineLineSpacing || !lastLine2) {
            double ex = (double) ((((float) (below2 - above2)) * (spacingmult - 1.0f)) + spacingadd);
            if (ex >= 0.0d) {
                extra2 = (int) (EXTRA_ROUNDING + ex);
                boolean z10 = lastLine2;
            } else {
                boolean z11 = lastLine2;
                extra2 = -((int) ((-ex) + EXTRA_ROUNDING));
            }
            extra = extra2;
            lines2[off + 0] = i3;
            int i16 = i2;
            lines2[off + 1] = v;
            lines2[off + 2] = below2 + extra;
            lines2[off + 3] = extra;
            if (!this.mEllipsized && currentLineIsTheLastVisibleOne) {
                this.mMaxLineHeight = v + ((!includePad ? bottom2 : below2) - above2);
            }
            int v2 = v + (below2 - above2) + extra;
            lines2[off + this.mColumns + 0] = i4;
            lines2[off + this.mColumns + 1] = v2;
            int i17 = off + 0;
            lines2[i17] = lines2[i17] | (flags & 536870912);
            lines2[off + 4] = flags;
            int i18 = off + 0;
            lines2[i18] = lines2[i18] | (dir << 30);
            this.mLineDirections[j] = measured.getDirections(i3 - i16, i4 - i16);
            this.mLineCount++;
            return v2;
        } else {
            boolean z12 = lastLine2;
        }
        extra = z;
        lines2[off + 0] = i3;
        int i162 = i2;
        lines2[off + 1] = v;
        lines2[off + 2] = below2 + extra;
        lines2[off + 3] = extra;
        this.mMaxLineHeight = v + ((!includePad ? bottom2 : below2) - above2);
        int v22 = v + (below2 - above2) + extra;
        lines2[off + this.mColumns + 0] = i4;
        lines2[off + this.mColumns + 1] = v22;
        int i172 = off + 0;
        lines2[i172] = lines2[i172] | (flags & 536870912);
        lines2[off + 4] = flags;
        int i182 = off + 0;
        lines2[i182] = lines2[i182] | (dir << 30);
        this.mLineDirections[j] = measured.getDirections(i3 - i162, i4 - i162);
        this.mLineCount++;
        return v22;
    }

    private void calculateEllipsis(int lineStart, int lineEnd, float[] widths, int widthStart, float avail, TextUtils.TruncateAt where, int line, float textWidth, TextPaint paint, boolean forceEllipsis) {
        int i;
        int right;
        TextUtils.TruncateAt truncateAt = where;
        int i2 = line;
        float avail2 = avail - getTotalInsets(i2);
        if (textWidth > avail2 || forceEllipsis) {
            float ellipsisWidth = paint.measureText(TextUtils.getEllipsisString(where));
            int ellipsisStart = 0;
            int ellipsisCount = 0;
            int len = lineEnd - lineStart;
            if (truncateAt == TextUtils.TruncateAt.START) {
                if (this.mMaximumVisibleLineCount == 1) {
                    float sum = 0.0f;
                    int i3 = len;
                    while (true) {
                        if (i3 <= 0) {
                            break;
                        }
                        float w = widths[((i3 - 1) + lineStart) - widthStart];
                        if (w + sum + ellipsisWidth > avail2) {
                            while (i3 < len && widths[(i3 + lineStart) - widthStart] == 0.0f) {
                                i3++;
                            }
                        } else {
                            sum += w;
                            i3--;
                        }
                    }
                    ellipsisStart = 0;
                    ellipsisCount = i3;
                } else if (Log.isLoggable(TAG, 5)) {
                    Log.w(TAG, "Start Ellipsis only supported with one line");
                }
            } else if (truncateAt == TextUtils.TruncateAt.END || truncateAt == TextUtils.TruncateAt.MARQUEE || truncateAt == TextUtils.TruncateAt.END_SMALL) {
                float sum2 = 0.0f;
                int i4 = 0;
                while (true) {
                    i = i4;
                    if (i >= len) {
                        break;
                    }
                    float w2 = widths[(i + lineStart) - widthStart];
                    if (w2 + sum2 + ellipsisWidth > avail2) {
                        break;
                    }
                    sum2 += w2;
                    i4 = i + 1;
                }
                int ellipsisStart2 = i;
                int ellipsisCount2 = len - i;
                if (!forceEllipsis || ellipsisCount2 != 0 || len <= 0) {
                    ellipsisCount = ellipsisCount2;
                } else {
                    ellipsisStart2 = len - 1;
                    ellipsisCount = 1;
                }
                ellipsisStart = ellipsisStart2;
            } else if (this.mMaximumVisibleLineCount == 1) {
                float rsum = 0.0f;
                int right2 = len;
                float ravail = (avail2 - ellipsisWidth) / 2.0f;
                while (true) {
                    if (right2 <= 0) {
                        right = right2;
                        break;
                    }
                    float w3 = widths[((right2 - 1) + lineStart) - widthStart];
                    if (w3 + rsum > ravail) {
                        right = right2 + 1;
                        while (right < len && widths[(right + lineStart) - widthStart] == 0.0f) {
                            right++;
                        }
                    } else {
                        rsum += w3;
                        right2--;
                        TextUtils.TruncateAt truncateAt2 = where;
                    }
                }
                float lavail = (avail2 - ellipsisWidth) - rsum;
                float lsum = 0.0f;
                int left = 0;
                while (true) {
                    if (left >= right) {
                        break;
                    }
                    float w4 = widths[(left + lineStart) - widthStart];
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
            this.mLines[(this.mColumns * i2) + 5] = ellipsisStart;
            this.mLines[(this.mColumns * i2) + 6] = ellipsisCount;
            return;
        }
        this.mLines[(this.mColumns * i2) + 5] = 0;
        this.mLines[(this.mColumns * i2) + 6] = 0;
    }

    private float getTotalInsets(int line) {
        int totalIndent = 0;
        if (this.mLeftIndents != null) {
            totalIndent = this.mLeftIndents[Math.min(line, this.mLeftIndents.length - 1)];
        }
        if (this.mRightIndents != null) {
            totalIndent += this.mRightIndents[Math.min(line, this.mRightIndents.length - 1)];
        }
        return (float) totalIndent;
    }

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

    public int getLineCount() {
        return this.mLineCount;
    }

    public int getLineTop(int line) {
        return this.mLines[(this.mColumns * line) + 1];
    }

    public int getLineExtra(int line) {
        return this.mLines[(this.mColumns * line) + 3];
    }

    public int getLineDescent(int line) {
        return this.mLines[(this.mColumns * line) + 2];
    }

    public int getLineStart(int line) {
        return this.mLines[(this.mColumns * line) + 0] & START_MASK;
    }

    public int getParagraphDirection(int line) {
        return this.mLines[(this.mColumns * line) + 0] >> 30;
    }

    public boolean getLineContainsTab(int line) {
        return (this.mLines[(this.mColumns * line) + 0] & 536870912) != 0;
    }

    public final Layout.Directions getLineDirections(int line) {
        if (line <= getLineCount()) {
            return this.mLineDirections[line];
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    public int getHyphen(int line) {
        return this.mLines[(this.mColumns * line) + 4] & 255;
    }

    public int getIndentAdjust(int line, Layout.Alignment align) {
        if (align == Layout.Alignment.ALIGN_LEFT) {
            if (this.mLeftIndents == null) {
                return 0;
            }
            return this.mLeftIndents[Math.min(line, this.mLeftIndents.length - 1)];
        } else if (align == Layout.Alignment.ALIGN_RIGHT) {
            if (this.mRightIndents == null) {
                return 0;
            }
            return -this.mRightIndents[Math.min(line, this.mRightIndents.length - 1)];
        } else if (align == Layout.Alignment.ALIGN_CENTER) {
            int left = 0;
            if (this.mLeftIndents != null) {
                left = this.mLeftIndents[Math.min(line, this.mLeftIndents.length - 1)];
            }
            int right = 0;
            if (this.mRightIndents != null) {
                right = this.mRightIndents[Math.min(line, this.mRightIndents.length - 1)];
            }
            return (left - right) >> 1;
        } else {
            throw new AssertionError("unhandled alignment " + align);
        }
    }

    public int getEllipsisCount(int line) {
        if (this.mColumns < 7) {
            return 0;
        }
        return this.mLines[(this.mColumns * line) + 6];
    }

    public int getEllipsisStart(int line) {
        if (this.mColumns < 7) {
            return 0;
        }
        return this.mLines[(this.mColumns * line) + 5];
    }

    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }

    public int getHeight(boolean cap) {
        if (cap && this.mLineCount >= this.mMaximumVisibleLineCount && this.mMaxLineHeight == -1 && Log.isLoggable(TAG, 5)) {
            Log.w(TAG, "maxLineHeight should not be -1.  maxLines:" + this.mMaximumVisibleLineCount + " lineCount:" + this.mLineCount);
        }
        return (!cap || this.mLineCount < this.mMaximumVisibleLineCount || this.mMaxLineHeight == -1) ? super.getHeight() : this.mMaxLineHeight;
    }
}

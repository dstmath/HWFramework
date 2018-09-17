package android.text;

import android.graphics.Paint.FontMetricsInt;
import android.text.Layout.Alignment;
import android.text.Layout.Directions;
import android.text.TextUtils.TruncateAt;
import android.text.style.LeadingMarginSpan;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;
import android.text.style.LineHeightSpan;
import android.text.style.LineHeightSpan.WithDensity;
import android.text.style.MetricAffectingSpan;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.util.Pools.SynchronizedPool;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public class StaticLayout extends Layout {
    private static final char CHAR_NEW_LINE = '\n';
    private static final int COLUMNS_ELLIPSIZE = 6;
    private static final int COLUMNS_NORMAL = 4;
    private static final int DESCENT = 2;
    private static final int DIR = 0;
    private static final int DIR_SHIFT = 30;
    private static final int ELLIPSIS_COUNT = 5;
    private static final int ELLIPSIS_START = 4;
    private static final double EXTRA_ROUNDING = 0.5d;
    private static final int HYPHEN = 3;
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
    private int mLineCount;
    private Directions[] mLineDirections;
    private int[] mLines;
    private int mMaxLineHeight;
    private int mMaximumVisibleLineCount;
    private int[] mRightIndents;
    private int mTopPadding;

    public static final class Builder {
        private static final SynchronizedPool<Builder> sPool = new SynchronizedPool(3);
        Alignment mAlignment;
        int mBreakStrategy;
        TruncateAt mEllipsize;
        int mEllipsizedWidth;
        int mEnd;
        FontMetricsInt mFontMetricsInt = new FontMetricsInt();
        int mHyphenationFrequency;
        boolean mIncludePad;
        int mJustificationMode;
        int[] mLeftIndents;
        Locale mLocale;
        int mMaxLines;
        MeasuredText mMeasuredText;
        long mNativePtr = StaticLayout.nNewBuilder();
        TextPaint mPaint;
        int[] mRightIndents;
        float mSpacingAdd;
        float mSpacingMult;
        int mStart;
        CharSequence mText;
        TextDirectionHeuristic mTextDir;
        int mWidth;

        private Builder() {
        }

        public static Builder obtain(CharSequence source, int start, int end, TextPaint paint, int width) {
            Builder b = (Builder) sPool.acquire();
            if (b == null) {
                b = new Builder();
            }
            b.mText = source;
            b.mStart = start;
            b.mEnd = end;
            b.mPaint = paint;
            b.mWidth = width;
            b.mAlignment = Alignment.ALIGN_NORMAL;
            b.mTextDir = TextDirectionHeuristics.FIRSTSTRONG_LTR;
            b.mSpacingMult = 1.0f;
            b.mSpacingAdd = 0.0f;
            b.mIncludePad = true;
            b.mEllipsizedWidth = width;
            b.mEllipsize = null;
            b.mMaxLines = Integer.MAX_VALUE;
            b.mBreakStrategy = 0;
            b.mHyphenationFrequency = 0;
            b.mJustificationMode = 0;
            b.mMeasuredText = MeasuredText.obtain();
            return b;
        }

        private static void recycle(Builder b) {
            b.mPaint = null;
            b.mText = null;
            MeasuredText.recycle(b.mMeasuredText);
            b.mMeasuredText = null;
            b.mLeftIndents = null;
            b.mRightIndents = null;
            StaticLayout.nFinishBuilder(b.mNativePtr);
            sPool.release(b);
        }

        void finish() {
            StaticLayout.nFinishBuilder(this.mNativePtr);
            this.mText = null;
            this.mPaint = null;
            this.mLeftIndents = null;
            this.mRightIndents = null;
            this.mMeasuredText.finish();
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

        public Builder setAlignment(Alignment alignment) {
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

        public Builder setEllipsizedWidth(int ellipsizedWidth) {
            this.mEllipsizedWidth = ellipsizedWidth;
            return this;
        }

        public Builder setEllipsize(TruncateAt ellipsize) {
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
            int leftLen = leftIndents == null ? 0 : leftIndents.length;
            int rightLen = rightIndents == null ? 0 : rightIndents.length;
            int[] indents = new int[Math.max(leftLen, rightLen)];
            int i = 0;
            while (i < indents.length) {
                indents[i] = (i < leftLen ? leftIndents[i] : 0) + (i < rightLen ? rightIndents[i] : 0);
                i++;
            }
            StaticLayout.nSetIndents(this.mNativePtr, indents);
            return this;
        }

        public Builder setJustificationMode(int justificationMode) {
            this.mJustificationMode = justificationMode;
            return this;
        }

        private void setLocale(Locale locale) {
            if (!locale.equals(this.mLocale)) {
                StaticLayout.nSetLocale(this.mNativePtr, locale.toLanguageTag(), Hyphenator.get(locale).getNativePtr());
                this.mLocale = locale;
            }
        }

        float addStyleRun(TextPaint paint, int start, int end, boolean isRtl) {
            return StaticLayout.nAddStyleRun(this.mNativePtr, paint.getNativeInstance(), paint.mNativeTypeface, start, end, isRtl);
        }

        void addMeasuredRun(int start, int end, float[] widths) {
            StaticLayout.nAddMeasuredRun(this.mNativePtr, start, end, widths);
        }

        void addReplacementRun(int start, int end, float width) {
            StaticLayout.nAddReplacementRun(this.mNativePtr, start, end, width);
        }

        public StaticLayout build() {
            StaticLayout result = new StaticLayout(this, null);
            recycle(this);
            return result;
        }

        protected void finalize() throws Throwable {
            try {
                StaticLayout.nFreeBuilder(this.mNativePtr);
            } finally {
                super.finalize();
            }
        }
    }

    static class LineBreaks {
        private static final int INITIAL_SIZE = 16;
        public int[] breaks = new int[16];
        public int[] flags = new int[16];
        public float[] widths = new float[16];

        LineBreaks() {
        }
    }

    /* synthetic */ StaticLayout(Builder b, StaticLayout -this1) {
        this(b);
    }

    private static native void nAddMeasuredRun(long j, int i, int i2, float[] fArr);

    private static native void nAddReplacementRun(long j, int i, int i2, float f);

    private static native float nAddStyleRun(long j, long j2, long j3, int i, int i2, boolean z);

    private static native int nComputeLineBreaks(long j, LineBreaks lineBreaks, int[] iArr, float[] fArr, int[] iArr2, int i);

    private static native void nFinishBuilder(long j);

    private static native void nFreeBuilder(long j);

    private static native void nGetWidths(long j, float[] fArr);

    static native long nLoadHyphenator(ByteBuffer byteBuffer, int i, int i2, int i3);

    private static native long nNewBuilder();

    private static native void nSetIndents(long j, int[] iArr);

    private static native void nSetLocale(long j, String str, long j2);

    private static native void nSetupParagraph(long j, char[] cArr, int i, float f, int i2, float f2, int[] iArr, int i3, int i4, int i5, boolean z);

    public StaticLayout(CharSequence source, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, 0, source.length(), paint, width, align, spacingmult, spacingadd, includepad);
    }

    public StaticLayout(CharSequence source, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad) {
        this(source, 0, source.length(), paint, width, align, textDir, spacingmult, spacingadd, includepad);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, spacingmult, spacingadd, includepad, null, 0);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, textDir, spacingmult, spacingadd, includepad, null, 0, Integer.MAX_VALUE);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth) {
        this(source, bufstart, bufend, paint, outerwidth, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, ellipsize, ellipsizedWidth, Integer.MAX_VALUE);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth, int maxLines) {
        CharSequence charSequence;
        if (ellipsize == null) {
            charSequence = source;
        } else if (source instanceof Spanned) {
            charSequence = new SpannedEllipsizer(source);
        } else {
            charSequence = new Ellipsizer(source);
        }
        super(charSequence, paint, outerwidth, align, textDir, spacingmult, spacingadd);
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        Builder b = Builder.obtain(source, bufstart, bufend, paint, outerwidth).setAlignment(align).setTextDirection(textDir).setLineSpacing(spacingadd, spacingmult).setIncludePad(includepad).setEllipsizedWidth(ellipsizedWidth).setEllipsize(ellipsize).setMaxLines(maxLines);
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            this.mEllipsizedWidth = ellipsizedWidth;
            this.mColumns = 6;
        } else {
            this.mColumns = 4;
            this.mEllipsizedWidth = outerwidth;
        }
        this.mLineDirections = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, this.mColumns * 2);
        this.mLines = new int[this.mLineDirections.length];
        this.mMaximumVisibleLineCount = maxLines;
        generate(b, b.mIncludePad, b.mIncludePad);
        Builder.recycle(b);
    }

    StaticLayout(CharSequence text) {
        super(text, null, 0, null, 0.0f, 0.0f);
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        this.mColumns = 6;
        this.mLineDirections = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, this.mColumns * 2);
        this.mLines = new int[this.mLineDirections.length];
    }

    private StaticLayout(Builder b) {
        CharSequence charSequence;
        if (b.mEllipsize == null) {
            charSequence = b.mText;
        } else if (b.mText instanceof Spanned) {
            charSequence = new SpannedEllipsizer(b.mText);
        } else {
            charSequence = new Ellipsizer(b.mText);
        }
        super(charSequence, b.mPaint, b.mWidth, b.mAlignment, b.mSpacingMult, b.mSpacingAdd);
        this.mMaxLineHeight = -1;
        this.mMaximumVisibleLineCount = Integer.MAX_VALUE;
        if (b.mEllipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = b.mEllipsizedWidth;
            e.mMethod = b.mEllipsize;
            this.mEllipsizedWidth = b.mEllipsizedWidth;
            this.mColumns = 6;
        } else {
            this.mColumns = 4;
            this.mEllipsizedWidth = b.mWidth;
        }
        this.mLineDirections = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, this.mColumns * 2);
        this.mLines = new int[this.mLineDirections.length];
        this.mMaximumVisibleLineCount = b.mMaxLines;
        this.mLeftIndents = b.mLeftIndents;
        this.mRightIndents = b.mRightIndents;
        setJustificationMode(b.mJustificationMode);
        generate(b, b.mIncludePad, b.mIncludePad);
    }

    /* JADX WARNING: Missing block: B:164:0x0506, code:
            if (r7 != r37) goto L_0x058d;
     */
    /* JADX WARNING: Missing block: B:173:0x058d, code:
            r6 = r7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void generate(Builder b, boolean includepad, boolean trackpad) {
        String language = Locale.getDefault().getLanguage();
        HashMap<String, Integer> languageMap = new HashMap();
        languageMap.put("bo", Integer.valueOf(7));
        languageMap.put("my", Integer.valueOf(2));
        CharSequence source = b.mText;
        int bufStart = b.mStart;
        int bufEnd = b.mEnd;
        TextPaint paint = b.mPaint;
        int outerWidth = b.mWidth;
        TextDirectionHeuristic textDir = b.mTextDir;
        float spacingmult = b.mSpacingMult;
        float spacingadd = b.mSpacingAdd;
        float ellipsizedWidth = (float) b.mEllipsizedWidth;
        TruncateAt ellipsize = b.mEllipsize;
        LineBreaks lineBreaks = new LineBreaks();
        int[] spanEndCache = new int[4];
        int[] fmCache = new int[16];
        b.setLocale(paint.getTextLocale());
        this.mLineCount = 0;
        int v = 0;
        boolean needMultiply = (spacingmult == 1.0f && spacingadd == 0.0f) ? false : true;
        FontMetricsInt fm = b.mFontMetricsInt;
        int[] chooseHtv = null;
        MeasuredText measured = b.mMeasuredText;
        Spanned spanned = null;
        if (source instanceof Spanned) {
            spanned = (Spanned) source;
        }
        int paraStart = bufStart;
        while (paraStart <= bufEnd) {
            int i;
            int spanStart;
            int spanEnd;
            int paraEnd = TextUtils.indexOf(source, (char) CHAR_NEW_LINE, paraStart, bufEnd);
            if (paraEnd < 0) {
                paraEnd = bufEnd;
            } else {
                paraEnd++;
            }
            int firstWidthLineCount = 1;
            int firstWidth = outerWidth;
            int restWidth = outerWidth;
            LineHeightSpan[] chooseHt = null;
            if (spanned != null) {
                LeadingMarginSpan[] sp = (LeadingMarginSpan[]) Layout.getParagraphSpans(spanned, paraStart, paraEnd, LeadingMarginSpan.class);
                for (i = 0; i < sp.length; i++) {
                    LeadingMarginSpan lms = sp[i];
                    firstWidth -= sp[i].getLeadingMargin(true);
                    restWidth -= sp[i].getLeadingMargin(false);
                    if (lms instanceof LeadingMarginSpan2) {
                        firstWidthLineCount = Math.max(firstWidthLineCount, ((LeadingMarginSpan2) lms).getLeadingMarginLineCount());
                    }
                }
                chooseHt = (LineHeightSpan[]) Layout.getParagraphSpans(spanned, paraStart, paraEnd, LineHeightSpan.class);
                if (chooseHt.length == 0) {
                    chooseHt = null;
                } else {
                    if (chooseHtv == null || chooseHtv.length < chooseHt.length) {
                        chooseHtv = ArrayUtils.newUnpaddedIntArray(chooseHt.length);
                    }
                    for (i = 0; i < chooseHt.length; i++) {
                        int o = spanned.getSpanStart(chooseHt[i]);
                        if (o < paraStart) {
                            chooseHtv[i] = getLineTop(getLineForOffset(o));
                        } else {
                            chooseHtv[i] = v;
                        }
                    }
                }
            }
            measured.setPara(source, paraStart, paraEnd, textDir, b);
            char[] chs = measured.mChars;
            float[] widths = measured.mWidths;
            byte[] chdirs = measured.mLevels;
            int dir = measured.mDir;
            boolean easy = measured.mEasy;
            int[] variableTabStops = null;
            if (spanned != null) {
                TabStopSpan[] spans = (TabStopSpan[]) Layout.getParagraphSpans(spanned, paraStart, paraEnd, TabStopSpan.class);
                if (spans.length > 0) {
                    int[] stops = new int[spans.length];
                    for (i = 0; i < spans.length; i++) {
                        stops[i] = spans[i].getTabStop();
                    }
                    Arrays.sort(stops, 0, stops.length);
                    variableTabStops = stops;
                }
            }
            nSetupParagraph(b.mNativePtr, chs, paraEnd - paraStart, (float) firstWidth, firstWidthLineCount, (float) restWidth, variableTabStops, 20, b.mBreakStrategy, b.mHyphenationFrequency, b.mJustificationMode != 0);
            if (!(this.mLeftIndents == null && this.mRightIndents == null)) {
                int leftLen = this.mLeftIndents == null ? 0 : this.mLeftIndents.length;
                int rightLen = this.mRightIndents == null ? 0 : this.mRightIndents.length;
                int indentsLen = Math.max(1, Math.max(leftLen, rightLen) - this.mLineCount);
                int[] indents = new int[indentsLen];
                for (i = 0; i < indentsLen; i++) {
                    int leftMargin;
                    int rightMargin;
                    if (this.mLeftIndents == null) {
                        leftMargin = 0;
                    } else {
                        leftMargin = this.mLeftIndents[Math.min(this.mLineCount + i, leftLen - 1)];
                    }
                    if (this.mRightIndents == null) {
                        rightMargin = 0;
                    } else {
                        rightMargin = this.mRightIndents[Math.min(this.mLineCount + i, rightLen - 1)];
                    }
                    indents[i] = leftMargin + rightMargin;
                }
                nSetIndents(b.mNativePtr, indents);
            }
            int fmCacheCount = 0;
            int spanEndCacheCount = 0;
            for (spanStart = paraStart; spanStart < paraEnd; spanStart = spanEnd) {
                int[] grow;
                if (fmCacheCount * 4 >= fmCache.length) {
                    grow = new int[((fmCacheCount * 4) * 2)];
                    System.arraycopy(fmCache, 0, grow, 0, fmCacheCount * 4);
                    fmCache = grow;
                }
                if (spanEndCacheCount >= spanEndCache.length) {
                    grow = new int[(spanEndCacheCount * 2)];
                    System.arraycopy(spanEndCache, 0, grow, 0, spanEndCacheCount);
                    spanEndCache = grow;
                }
                if (spanned == null) {
                    spanEnd = paraEnd;
                    measured.addStyleRun(paint, paraEnd - spanStart, fm);
                } else {
                    spanEnd = spanned.nextSpanTransition(spanStart, paraEnd, MetricAffectingSpan.class);
                    measured.addStyleRun(paint, (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) spanned.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), spanned, MetricAffectingSpan.class), spanEnd - spanStart, fm);
                }
                if (languageMap.containsKey(language)) {
                    fm.bottom = ((Integer) languageMap.get(language)).intValue() + fm.bottom;
                    fm.descent = ((Integer) languageMap.get(language)).intValue() + fm.descent;
                }
                fmCache[(fmCacheCount * 4) + 0] = fm.top;
                fmCache[(fmCacheCount * 4) + 1] = fm.bottom;
                fmCache[(fmCacheCount * 4) + 2] = fm.ascent;
                fmCache[(fmCacheCount * 4) + 3] = fm.descent;
                fmCacheCount++;
                spanEndCache[spanEndCacheCount] = spanEnd;
                spanEndCacheCount++;
            }
            nGetWidths(b.mNativePtr, widths);
            int breakCount = nComputeLineBreaks(b.mNativePtr, lineBreaks, lineBreaks.breaks, lineBreaks.widths, lineBreaks.flags, lineBreaks.breaks.length);
            int[] breaks = lineBreaks.breaks;
            float[] lineWidths = lineBreaks.widths;
            int[] flags = lineBreaks.flags;
            int remainingLineCount = this.mMaximumVisibleLineCount - this.mLineCount;
            boolean ellipsisMayBeApplied = ellipsize != null ? ellipsize != TruncateAt.END ? this.mMaximumVisibleLineCount == 1 ? ellipsize != TruncateAt.MARQUEE : false : true : false;
            if (remainingLineCount > 0 && remainingLineCount < breakCount && ellipsisMayBeApplied) {
                float width = 0.0f;
                int flag = 0;
                i = remainingLineCount - 1;
                while (i < breakCount) {
                    if (i == breakCount - 1) {
                        width += lineWidths[i];
                    } else {
                        int j = i == 0 ? 0 : breaks[i - 1];
                        while (j < breaks[i]) {
                            width += widths[j];
                            j++;
                        }
                    }
                    flag |= flags[i] & 536870912;
                    i++;
                }
                breaks[remainingLineCount - 1] = breaks[breakCount - 1];
                lineWidths[remainingLineCount - 1] = width;
                flags[remainingLineCount - 1] = flag;
                breakCount = remainingLineCount;
            }
            int here = paraStart;
            int fmTop = 0;
            int fmBottom = 0;
            int fmAscent = 0;
            int fmDescent = 0;
            int fmCacheIndex = 0;
            int spanEndCacheIndex = 0;
            int breakIndex = 0;
            spanStart = paraStart;
            while (true) {
                int spanEndCacheIndex2 = spanEndCacheIndex;
                if (spanStart >= paraEnd) {
                    break;
                }
                spanEndCacheIndex = spanEndCacheIndex2 + 1;
                spanEnd = spanEndCache[spanEndCacheIndex2];
                fm.top = fmCache[(fmCacheIndex * 4) + 0];
                fm.bottom = fmCache[(fmCacheIndex * 4) + 1];
                fm.ascent = fmCache[(fmCacheIndex * 4) + 2];
                fm.descent = fmCache[(fmCacheIndex * 4) + 3];
                fmCacheIndex++;
                if (fm.top < fmTop) {
                    fmTop = fm.top;
                }
                if (fm.ascent < fmAscent) {
                    fmAscent = fm.ascent;
                }
                if (fm.descent > fmDescent) {
                    fmDescent = fm.descent;
                }
                if (fm.bottom > fmBottom) {
                    fmBottom = fm.bottom;
                }
                while (breakIndex < breakCount && breaks[breakIndex] + paraStart < spanStart) {
                    breakIndex++;
                }
                while (breakIndex < breakCount && breaks[breakIndex] + paraStart <= spanEnd) {
                    int endPos = paraStart + breaks[breakIndex];
                    v = out(source, here, endPos, fmAscent, fmDescent, fmTop, fmBottom, v, spacingmult, spacingadd, chooseHt, chooseHtv, fm, flags[breakIndex], needMultiply, chdirs, dir, easy, bufEnd, includepad, trackpad, chs, widths, paraStart, ellipsize, ellipsizedWidth, lineWidths[breakIndex], paint, endPos < bufEnd);
                    if (endPos < spanEnd) {
                        fmTop = fm.top;
                        fmBottom = fm.bottom;
                        fmAscent = fm.ascent;
                        fmDescent = fm.descent;
                    } else {
                        fmDescent = 0;
                        fmAscent = 0;
                        fmBottom = 0;
                        fmTop = 0;
                    }
                    here = endPos;
                    breakIndex++;
                    if (this.mLineCount >= this.mMaximumVisibleLineCount && this.mEllipsized) {
                        return;
                    }
                }
                spanStart = spanEnd;
            }
        }
        if ((bufEnd == bufStart || source.charAt(bufEnd - 1) == CHAR_NEW_LINE) && this.mLineCount < this.mMaximumVisibleLineCount) {
            measured.setPara(source, bufEnd, bufEnd, textDir, b);
            paint.getFontMetricsInt(fm);
            v = out(source, bufEnd, bufEnd, fm.ascent, fm.descent, fm.top, fm.bottom, v, spacingmult, spacingadd, null, null, fm, 0, needMultiply, measured.mLevels, measured.mDir, measured.mEasy, bufEnd, includepad, trackpad, null, null, bufStart, ellipsize, ellipsizedWidth, 0.0f, paint, false);
        }
    }

    private int out(CharSequence text, int start, int end, int above, int below, int top, int bottom, int v, float spacingmult, float spacingadd, LineHeightSpan[] chooseHt, int[] chooseHtv, FontMetricsInt fm, int flags, boolean needMultiply, byte[] chdirs, int dir, boolean easy, int bufEnd, boolean includePad, boolean trackPad, char[] chs, float[] widths, int widthStart, TruncateAt ellipsize, float ellipsisWidth, float textWidth, TextPaint paint, boolean moreChars) {
        int extra;
        int j = this.mLineCount;
        int off = j * this.mColumns;
        int want = (this.mColumns + off) + 1;
        int[] lines = this.mLines;
        if (want >= lines.length) {
            Object grow2 = new Directions[GrowingArrayUtils.growSize(want)];
            System.arraycopy(this.mLineDirections, 0, grow2, 0, this.mLineDirections.length);
            this.mLineDirections = grow2;
            int[] grow = new int[grow2.length];
            System.arraycopy(lines, 0, grow, 0, lines.length);
            this.mLines = grow;
            lines = grow;
        }
        if (chooseHt != null) {
            fm.ascent = above;
            fm.descent = below;
            fm.top = top;
            fm.bottom = bottom;
            for (int i = 0; i < chooseHt.length; i++) {
                if (chooseHt[i] instanceof WithDensity) {
                    ((WithDensity) chooseHt[i]).chooseHeight(text, start, end, chooseHtv[i], v, fm, paint);
                } else {
                    chooseHt[i].chooseHeight(text, start, end, chooseHtv[i], v, fm);
                }
            }
            above = fm.ascent;
            below = fm.descent;
            top = fm.top;
            bottom = fm.bottom;
        }
        boolean firstLine = j == 0;
        boolean currentLineIsTheLastVisibleOne = j + 1 == this.mMaximumVisibleLineCount;
        if (ellipsize != null) {
            boolean forceEllipsis = moreChars && this.mLineCount + 1 == this.mMaximumVisibleLineCount;
            boolean doEllipsis = ((!(this.mMaximumVisibleLineCount == 1 && moreChars) && (!firstLine || (moreChars ^ 1) == 0)) || ellipsize == TruncateAt.MARQUEE) ? (firstLine || (!currentLineIsTheLastVisibleOne && (moreChars ^ 1) == 0)) ? false : ellipsize == TruncateAt.END : true;
            if (doEllipsis) {
                calculateEllipsis(start, end, widths, widthStart, ellipsisWidth, ellipsize, j, textWidth, paint, forceEllipsis);
            }
        }
        boolean lastLine = this.mEllipsized || end == bufEnd;
        if (firstLine) {
            if (trackPad) {
                this.mTopPadding = top - above;
            }
            if (includePad) {
                above = top;
            }
        }
        if (lastLine) {
            if (trackPad) {
                this.mBottomPadding = bottom - below;
            }
            if (includePad) {
                below = bottom;
            }
        }
        if (!needMultiply || (lastLine ^ 1) == 0) {
            extra = 0;
        } else {
            double ex = (double) ((((float) (below - above)) * (spacingmult - 1.0f)) + spacingadd);
            if (ex >= 0.0d) {
                extra = (int) (EXTRA_ROUNDING + ex);
            } else {
                extra = -((int) ((-ex) + EXTRA_ROUNDING));
            }
        }
        lines[off + 0] = start;
        lines[off + 1] = v;
        lines[off + 2] = below + extra;
        if (!this.mEllipsized && currentLineIsTheLastVisibleOne) {
            this.mMaxLineHeight = ((includePad ? bottom : below) - above) + v;
        }
        v += (below - above) + extra;
        lines[(this.mColumns + off) + 0] = end;
        lines[(this.mColumns + off) + 1] = v;
        int i2 = off + 0;
        lines[i2] = lines[i2] | (536870912 & flags);
        lines[off + 3] = flags;
        i2 = off + 0;
        lines[i2] = lines[i2] | (dir << 30);
        Directions linedirs = DIRS_ALL_LEFT_TO_RIGHT;
        if (easy) {
            this.mLineDirections[j] = linedirs;
        } else {
            this.mLineDirections[j] = AndroidBidi.directions(dir, chdirs, start - widthStart, chs, start - widthStart, end - start);
        }
        this.mLineCount++;
        return v;
    }

    /* JADX WARNING: Removed duplicated region for block: B:58:0x0162  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void calculateEllipsis(int lineStart, int lineEnd, float[] widths, int widthStart, float avail, TruncateAt where, int line, float textWidth, TextPaint paint, boolean forceEllipsis) {
        avail -= getTotalInsets(line);
        if (textWidth > avail || (forceEllipsis ^ 1) == 0) {
            float ellipsisWidth = paint.measureText(where == TruncateAt.END_SMALL ? TextUtils.ELLIPSIS_TWO_DOTS : TextUtils.ELLIPSIS_NORMAL, 0, 1);
            int ellipsisStart = 0;
            int ellipsisCount = 0;
            int len = lineEnd - lineStart;
            float sum;
            int i;
            float w;
            if (where == TruncateAt.START) {
                if (this.mMaximumVisibleLineCount == 1) {
                    sum = 0.0f;
                    i = len;
                    while (i > 0) {
                        w = widths[((i - 1) + lineStart) - widthStart];
                        if ((w + sum) + ellipsisWidth > avail) {
                            while (i < len && widths[(i + lineStart) - widthStart] == 0.0f) {
                                i++;
                            }
                            ellipsisStart = 0;
                            ellipsisCount = i;
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
            } else if (where == TruncateAt.END || where == TruncateAt.MARQUEE || where == TruncateAt.END_SMALL) {
                sum = 0.0f;
                i = 0;
                while (i < len) {
                    w = widths[(i + lineStart) - widthStart];
                    if ((w + sum) + ellipsisWidth > avail) {
                        break;
                    }
                    sum += w;
                    i++;
                }
                ellipsisStart = i;
                ellipsisCount = len - i;
                if (forceEllipsis && ellipsisCount == 0 && len > 0) {
                    ellipsisStart = len - 1;
                    ellipsisCount = 1;
                }
            } else if (this.mMaximumVisibleLineCount == 1) {
                float lavail;
                int left;
                float lsum = 0.0f;
                float rsum = 0.0f;
                int right = len;
                float ravail = (avail - ellipsisWidth) / 2.0f;
                right = len;
                while (right > 0) {
                    w = widths[((right - 1) + lineStart) - widthStart];
                    if (w + rsum > ravail) {
                        while (right < len && widths[(right + lineStart) - widthStart] == 0.0f) {
                            right++;
                        }
                        lavail = (avail - ellipsisWidth) - rsum;
                        left = 0;
                        while (left < right) {
                            w = widths[(left + lineStart) - widthStart];
                            if (w + lsum > lavail) {
                                break;
                            }
                            lsum += w;
                            left++;
                        }
                        ellipsisStart = left;
                        ellipsisCount = right - left;
                    } else {
                        rsum += w;
                        right--;
                    }
                }
                lavail = (avail - ellipsisWidth) - rsum;
                left = 0;
                while (left < right) {
                }
                ellipsisStart = left;
                ellipsisCount = right - left;
            } else if (Log.isLoggable(TAG, 5)) {
                Log.w(TAG, "Middle Ellipsis only supported with one line");
            }
            this.mEllipsized = true;
            this.mLines[(this.mColumns * line) + 4] = ellipsisStart;
            this.mLines[(this.mColumns * line) + 5] = ellipsisCount;
            return;
        }
        this.mLines[(this.mColumns * line) + 4] = 0;
        this.mLines[(this.mColumns * line) + 5] = 0;
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

    public final Directions getLineDirections(int line) {
        return this.mLineDirections[line];
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    public int getBottomPadding() {
        return this.mBottomPadding;
    }

    public int getHyphen(int line) {
        return this.mLines[(this.mColumns * line) + 3] & 255;
    }

    public int getIndentAdjust(int line, Alignment align) {
        if (align == Alignment.ALIGN_LEFT) {
            if (this.mLeftIndents == null) {
                return 0;
            }
            return this.mLeftIndents[Math.min(line, this.mLeftIndents.length - 1)];
        } else if (align == Alignment.ALIGN_RIGHT) {
            if (this.mRightIndents == null) {
                return 0;
            }
            return -this.mRightIndents[Math.min(line, this.mRightIndents.length - 1)];
        } else if (align == Alignment.ALIGN_CENTER) {
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
        if (this.mColumns < 6) {
            return 0;
        }
        return this.mLines[(this.mColumns * line) + 5];
    }

    public int getEllipsisStart(int line) {
        if (this.mColumns < 6) {
            return 0;
        }
        return this.mLines[(this.mColumns * line) + 4];
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

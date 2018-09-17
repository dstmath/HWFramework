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
import android.view.WindowManager.LayoutParams;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
    private static final int START = 0;
    private static final int START_MASK = 536870911;
    private static final int TAB = 0;
    private static final int TAB_INCREMENT = 20;
    private static final int TAB_MASK = 536870912;
    static final String TAG = "StaticLayout";
    private static final int TOP = 1;
    private int mBottomPadding;
    private int mColumns;
    private int mEllipsizedWidth;
    private int[] mLeftIndents;
    private int mLineCount;
    private Directions[] mLineDirections;
    private int[] mLines;
    private int mMaximumVisibleLineCount;
    private int[] mRightIndents;
    private int mTopPadding;

    public static final class Builder {
        private static final SynchronizedPool<Builder> sPool = null;
        Alignment mAlignment;
        int mBreakStrategy;
        TruncateAt mEllipsize;
        int mEllipsizedWidth;
        int mEnd;
        FontMetricsInt mFontMetricsInt;
        int mHyphenationFrequency;
        boolean mIncludePad;
        int[] mLeftIndents;
        Locale mLocale;
        int mMaxLines;
        MeasuredText mMeasuredText;
        long mNativePtr;
        TextPaint mPaint;
        int[] mRightIndents;
        float mSpacingAdd;
        float mSpacingMult;
        int mStart;
        CharSequence mText;
        TextDirectionHeuristic mTextDir;
        int mWidth;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.StaticLayout.Builder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.StaticLayout.Builder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.text.StaticLayout.Builder.<clinit>():void");
        }

        private Builder() {
            this.mFontMetricsInt = new FontMetricsInt();
            this.mNativePtr = StaticLayout.nNewBuilder();
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
            b.mSpacingMult = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            b.mSpacingAdd = 0.0f;
            b.mIncludePad = true;
            b.mEllipsizedWidth = width;
            b.mEllipsize = null;
            b.mMaxLines = HwBootFail.STAGE_BOOT_SUCCESS;
            b.mBreakStrategy = StaticLayout.TAB;
            b.mHyphenationFrequency = StaticLayout.TAB;
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
            return setText(source, StaticLayout.TAB, source.length());
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
            int leftLen = leftIndents == null ? StaticLayout.TAB : leftIndents.length;
            int rightLen = rightIndents == null ? StaticLayout.TAB : rightIndents.length;
            int[] indents = new int[Math.max(leftLen, rightLen)];
            int i = StaticLayout.TAB;
            while (i < indents.length) {
                int rightMargin;
                int leftMargin = i < leftLen ? leftIndents[i] : StaticLayout.TAB;
                if (i < rightLen) {
                    rightMargin = rightIndents[i];
                } else {
                    rightMargin = StaticLayout.TAB;
                }
                indents[i] = leftMargin + rightMargin;
                i += StaticLayout.TOP;
            }
            StaticLayout.nSetIndents(this.mNativePtr, indents);
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
            StaticLayout result = new StaticLayout();
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
        public int[] breaks;
        public int[] flags;
        public float[] widths;

        LineBreaks() {
            this.breaks = new int[INITIAL_SIZE];
            this.widths = new float[INITIAL_SIZE];
            this.flags = new int[INITIAL_SIZE];
        }
    }

    /* synthetic */ StaticLayout(Builder b, StaticLayout staticLayout) {
        this(b);
    }

    private static native void nAddMeasuredRun(long j, int i, int i2, float[] fArr);

    private static native void nAddReplacementRun(long j, int i, int i2, float f);

    private static native float nAddStyleRun(long j, long j2, long j3, int i, int i2, boolean z);

    private static native int nComputeLineBreaks(long j, LineBreaks lineBreaks, int[] iArr, float[] fArr, int[] iArr2, int i);

    private static native void nFinishBuilder(long j);

    private static native void nFreeBuilder(long j);

    private static native void nGetWidths(long j, float[] fArr);

    static native long nLoadHyphenator(ByteBuffer byteBuffer, int i);

    private static native long nNewBuilder();

    private static native void nSetIndents(long j, int[] iArr);

    private static native void nSetLocale(long j, String str, long j2);

    private static native void nSetupParagraph(long j, char[] cArr, int i, float f, int i2, float f2, int[] iArr, int i3, int i4, int i5);

    public StaticLayout(CharSequence source, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, TAB, source.length(), paint, width, align, spacingmult, spacingadd, includepad);
    }

    public StaticLayout(CharSequence source, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad) {
        this(source, TAB, source.length(), paint, width, align, textDir, spacingmult, spacingadd, includepad);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, spacingmult, spacingadd, includepad, null, TAB);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, TextDirectionHeuristic textDir, float spacingmult, float spacingadd, boolean includepad) {
        this(source, bufstart, bufend, paint, outerwidth, align, textDir, spacingmult, spacingadd, includepad, null, TAB, HwBootFail.STAGE_BOOT_SUCCESS);
    }

    public StaticLayout(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad, TruncateAt ellipsize, int ellipsizedWidth) {
        this(source, bufstart, bufend, paint, outerwidth, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingmult, spacingadd, includepad, ellipsize, ellipsizedWidth, HwBootFail.STAGE_BOOT_SUCCESS);
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
        this.mMaximumVisibleLineCount = HwBootFail.STAGE_BOOT_SUCCESS;
        Builder b = Builder.obtain(source, bufstart, bufend, paint, outerwidth).setAlignment(align).setTextDirection(textDir).setLineSpacing(spacingadd, spacingmult).setIncludePad(includepad).setEllipsizedWidth(ellipsizedWidth).setEllipsize(ellipsize).setMaxLines(maxLines);
        if (ellipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = ellipsizedWidth;
            e.mMethod = ellipsize;
            this.mEllipsizedWidth = ellipsizedWidth;
            this.mColumns = COLUMNS_ELLIPSIZE;
        } else {
            this.mColumns = ELLIPSIS_START;
            this.mEllipsizedWidth = outerwidth;
        }
        this.mLineDirections = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, this.mColumns * DESCENT);
        this.mLines = new int[this.mLineDirections.length];
        this.mMaximumVisibleLineCount = maxLines;
        generate(b, b.mIncludePad, b.mIncludePad);
        Builder.recycle(b);
    }

    StaticLayout(CharSequence text) {
        super(text, null, TAB, null, 0.0f, 0.0f);
        this.mMaximumVisibleLineCount = HwBootFail.STAGE_BOOT_SUCCESS;
        this.mColumns = COLUMNS_ELLIPSIZE;
        this.mLineDirections = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, this.mColumns * DESCENT);
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
        this.mMaximumVisibleLineCount = HwBootFail.STAGE_BOOT_SUCCESS;
        if (b.mEllipsize != null) {
            Ellipsizer e = (Ellipsizer) getText();
            e.mLayout = this;
            e.mWidth = b.mEllipsizedWidth;
            e.mMethod = b.mEllipsize;
            this.mEllipsizedWidth = b.mEllipsizedWidth;
            this.mColumns = COLUMNS_ELLIPSIZE;
        } else {
            this.mColumns = ELLIPSIS_START;
            this.mEllipsizedWidth = b.mWidth;
        }
        this.mLineDirections = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, this.mColumns * DESCENT);
        this.mLines = new int[this.mLineDirections.length];
        this.mMaximumVisibleLineCount = b.mMaxLines;
        this.mLeftIndents = b.mLeftIndents;
        this.mRightIndents = b.mRightIndents;
        generate(b, b.mIncludePad, b.mIncludePad);
    }

    void generate(Builder b, boolean includepad, boolean trackpad) {
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
        int[] spanEndCache = new int[ELLIPSIS_START];
        int[] fmCache = new int[16];
        b.setLocale(paint.getTextLocale());
        this.mLineCount = TAB;
        int v = TAB;
        boolean needMultiply = (spacingmult == LayoutParams.BRIGHTNESS_OVERRIDE_FULL && spacingadd == 0.0f) ? false : true;
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
            int spanEnd;
            int paraEnd = TextUtils.indexOf(source, (char) CHAR_NEW_LINE, paraStart, bufEnd);
            if (paraEnd < 0) {
                paraEnd = bufEnd;
            } else {
                paraEnd += TOP;
            }
            int firstWidthLineCount = TOP;
            int firstWidth = outerWidth;
            int restWidth = outerWidth;
            LineHeightSpan[] lineHeightSpanArr = null;
            if (spanned != null) {
                LeadingMarginSpan[] sp = (LeadingMarginSpan[]) Layout.getParagraphSpans(spanned, paraStart, paraEnd, LeadingMarginSpan.class);
                for (i = TAB; i < sp.length; i += TOP) {
                    LeadingMarginSpan lms = sp[i];
                    firstWidth -= sp[i].getLeadingMargin(true);
                    restWidth -= sp[i].getLeadingMargin(false);
                    if (lms instanceof LeadingMarginSpan2) {
                        firstWidthLineCount = Math.max(firstWidthLineCount, ((LeadingMarginSpan2) lms).getLeadingMarginLineCount());
                    }
                }
                lineHeightSpanArr = (LineHeightSpan[]) Layout.getParagraphSpans(spanned, paraStart, paraEnd, LineHeightSpan.class);
                if (lineHeightSpanArr.length == 0) {
                    lineHeightSpanArr = null;
                } else {
                    if (chooseHtv == null || chooseHtv.length < lineHeightSpanArr.length) {
                        chooseHtv = ArrayUtils.newUnpaddedIntArray(lineHeightSpanArr.length);
                    }
                    for (i = TAB; i < lineHeightSpanArr.length; i += TOP) {
                        int o = spanned.getSpanStart(lineHeightSpanArr[i]);
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
            int[] iArr = null;
            if (spanned != null) {
                TabStopSpan[] spans = (TabStopSpan[]) Layout.getParagraphSpans(spanned, paraStart, paraEnd, TabStopSpan.class);
                if (spans.length > 0) {
                    int[] stops = new int[spans.length];
                    for (i = TAB; i < spans.length; i += TOP) {
                        stops[i] = spans[i].getTabStop();
                    }
                    Arrays.sort(stops, TAB, stops.length);
                    iArr = stops;
                }
            }
            nSetupParagraph(b.mNativePtr, chs, paraEnd - paraStart, (float) firstWidth, firstWidthLineCount, (float) restWidth, iArr, TAB_INCREMENT, b.mBreakStrategy, b.mHyphenationFrequency);
            if (!(this.mLeftIndents == null && this.mRightIndents == null)) {
                int leftLen = this.mLeftIndents == null ? TAB : this.mLeftIndents.length;
                int rightLen = this.mRightIndents == null ? TAB : this.mRightIndents.length;
                int indentsLen = Math.max(TOP, Math.max(leftLen, rightLen) - this.mLineCount);
                int[] indents = new int[indentsLen];
                for (i = TAB; i < indentsLen; i += TOP) {
                    int leftMargin;
                    int rightMargin;
                    if (this.mLeftIndents == null) {
                        leftMargin = TAB;
                    } else {
                        leftMargin = this.mLeftIndents[Math.min(this.mLineCount + i, leftLen - 1)];
                    }
                    if (this.mRightIndents == null) {
                        rightMargin = TAB;
                    } else {
                        rightMargin = this.mRightIndents[Math.min(this.mLineCount + i, rightLen - 1)];
                    }
                    indents[i] = leftMargin + rightMargin;
                }
                nSetIndents(b.mNativePtr, indents);
            }
            int fmCacheCount = TAB;
            int spanEndCacheCount = TAB;
            int spanStart = paraStart;
            while (spanStart < paraEnd) {
                if (fmCacheCount * ELLIPSIS_START >= fmCache.length) {
                    int[] grow = new int[((fmCacheCount * ELLIPSIS_START) * DESCENT)];
                    System.arraycopy(fmCache, TAB, grow, TAB, fmCacheCount * ELLIPSIS_START);
                    fmCache = grow;
                }
                if (spanEndCacheCount >= spanEndCache.length) {
                    grow = new int[(spanEndCacheCount * DESCENT)];
                    System.arraycopy(spanEndCache, TAB, grow, TAB, spanEndCacheCount);
                    spanEndCache = grow;
                }
                if (spanned == null) {
                    spanEnd = paraEnd;
                    measured.addStyleRun(paint, paraEnd - spanStart, fm);
                } else {
                    spanEnd = spanned.nextSpanTransition(spanStart, paraEnd, MetricAffectingSpan.class);
                    measured.addStyleRun(paint, (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) spanned.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), spanned, MetricAffectingSpan.class), spanEnd - spanStart, fm);
                }
                fmCache[(fmCacheCount * ELLIPSIS_START) + TAB] = fm.top;
                fmCache[(fmCacheCount * ELLIPSIS_START) + TOP] = fm.bottom;
                fmCache[(fmCacheCount * ELLIPSIS_START) + DESCENT] = fm.ascent;
                fmCache[(fmCacheCount * ELLIPSIS_START) + HYPHEN] = fm.descent;
                fmCacheCount += TOP;
                spanEndCache[spanEndCacheCount] = spanEnd;
                spanEndCacheCount += TOP;
                spanStart = spanEnd;
            }
            nGetWidths(b.mNativePtr, widths);
            int breakCount = nComputeLineBreaks(b.mNativePtr, lineBreaks, lineBreaks.breaks, lineBreaks.widths, lineBreaks.flags, lineBreaks.breaks.length);
            int[] breaks = lineBreaks.breaks;
            float[] lineWidths = lineBreaks.widths;
            int[] flags = lineBreaks.flags;
            int remainingLineCount = this.mMaximumVisibleLineCount - this.mLineCount;
            boolean ellipsisMayBeApplied = ellipsize != null ? ellipsize != TruncateAt.END ? this.mMaximumVisibleLineCount == TOP ? ellipsize != TruncateAt.MARQUEE : false : true : false;
            if (remainingLineCount > 0 && remainingLineCount < breakCount && ellipsisMayBeApplied) {
                float width = 0.0f;
                int flag = TAB;
                i = remainingLineCount - 1;
                while (i < breakCount) {
                    if (i == breakCount - 1) {
                        width += lineWidths[i];
                    } else {
                        int j = i == 0 ? TAB : breaks[i - 1];
                        while (j < breaks[i]) {
                            width += widths[j];
                            j += TOP;
                        }
                    }
                    flag |= flags[i] & TAB_MASK;
                    i += TOP;
                }
                breaks[remainingLineCount - 1] = breaks[breakCount - 1];
                lineWidths[remainingLineCount - 1] = width;
                flags[remainingLineCount - 1] = flag;
                breakCount = remainingLineCount;
            }
            int here = paraStart;
            int fmTop = TAB;
            int fmBottom = TAB;
            int fmAscent = TAB;
            int fmDescent = TAB;
            int fmCacheIndex = TAB;
            int breakIndex = TAB;
            spanStart = paraStart;
            int spanEndCacheIndex = TAB;
            while (spanStart < paraEnd) {
                int spanEndCacheIndex2 = spanEndCacheIndex + TOP;
                spanEnd = spanEndCache[spanEndCacheIndex];
                fm.top = fmCache[(fmCacheIndex * ELLIPSIS_START) + TAB];
                fm.bottom = fmCache[(fmCacheIndex * ELLIPSIS_START) + TOP];
                fm.ascent = fmCache[(fmCacheIndex * ELLIPSIS_START) + DESCENT];
                fm.descent = fmCache[(fmCacheIndex * ELLIPSIS_START) + HYPHEN];
                fmCacheIndex += TOP;
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
                    breakIndex += TOP;
                }
                while (breakIndex < breakCount && breaks[breakIndex] + paraStart <= spanEnd) {
                    int endPos = paraStart + breaks[breakIndex];
                    v = out(source, here, endPos, fmAscent, fmDescent, fmTop, fmBottom, v, spacingmult, spacingadd, lineHeightSpanArr, chooseHtv, fm, flags[breakIndex], needMultiply, chdirs, dir, easy, bufEnd, includepad, trackpad, chs, widths, paraStart, ellipsize, ellipsizedWidth, lineWidths[breakIndex], paint, endPos < bufEnd);
                    if (endPos < spanEnd) {
                        fmTop = fm.top;
                        fmBottom = fm.bottom;
                        fmAscent = fm.ascent;
                        fmDescent = fm.descent;
                    } else {
                        fmDescent = TAB;
                        fmAscent = TAB;
                        fmBottom = TAB;
                        fmTop = TAB;
                    }
                    here = endPos;
                    breakIndex += TOP;
                    if (this.mLineCount >= this.mMaximumVisibleLineCount) {
                        return;
                    }
                }
                spanStart = spanEnd;
                spanEndCacheIndex = spanEndCacheIndex2;
            }
            if (paraEnd == bufEnd) {
                break;
            }
            paraStart = paraEnd;
        }
        if ((bufEnd == bufStart || source.charAt(bufEnd - 1) == CHAR_NEW_LINE) && this.mLineCount < this.mMaximumVisibleLineCount) {
            measured.setPara(source, bufEnd, bufEnd, textDir, b);
            paint.getFontMetricsInt(fm);
            v = out(source, bufEnd, bufEnd, fm.ascent, fm.descent, fm.top, fm.bottom, v, spacingmult, spacingadd, null, null, fm, TAB, needMultiply, measured.mLevels, measured.mDir, measured.mEasy, bufEnd, includepad, trackpad, null, null, bufStart, ellipsize, ellipsizedWidth, 0.0f, paint, false);
        }
    }

    private int out(CharSequence text, int start, int end, int above, int below, int top, int bottom, int v, float spacingmult, float spacingadd, LineHeightSpan[] chooseHt, int[] chooseHtv, FontMetricsInt fm, int flags, boolean needMultiply, byte[] chdirs, int dir, boolean easy, int bufEnd, boolean includePad, boolean trackPad, char[] chs, float[] widths, int widthStart, TruncateAt ellipsize, float ellipsisWidth, float textWidth, TextPaint paint, boolean moreChars) {
        int extra;
        int j = this.mLineCount;
        int off = j * this.mColumns;
        int want = (this.mColumns + off) + TOP;
        int[] lines = this.mLines;
        if (want >= lines.length) {
            Object grow2 = (Directions[]) ArrayUtils.newUnpaddedArray(Directions.class, GrowingArrayUtils.growSize(want));
            System.arraycopy(this.mLineDirections, TAB, grow2, TAB, this.mLineDirections.length);
            this.mLineDirections = grow2;
            int[] grow = new int[grow2.length];
            System.arraycopy(lines, TAB, grow, TAB, lines.length);
            this.mLines = grow;
            lines = grow;
        }
        if (chooseHt != null) {
            fm.ascent = above;
            fm.descent = below;
            fm.top = top;
            fm.bottom = bottom;
            for (int i = TAB; i < chooseHt.length; i += TOP) {
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
        boolean currentLineIsTheLastVisibleOne = j + TOP == this.mMaximumVisibleLineCount;
        boolean lastLine = currentLineIsTheLastVisibleOne || end == bufEnd;
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
        if (!needMultiply || (lastLine && (!lastLine || TextUtils.indexOf(text, (char) CHAR_NEW_LINE, start, end) == -1))) {
            extra = TAB;
        } else {
            double ex = (double) ((((float) (below - above)) * (spacingmult - LayoutParams.BRIGHTNESS_OVERRIDE_FULL)) + spacingadd);
            if (ex >= 0.0d) {
                extra = (int) (EXTRA_ROUNDING + ex);
            } else {
                extra = -((int) ((-ex) + EXTRA_ROUNDING));
            }
        }
        lines[off + TAB] = start;
        lines[off + TOP] = v;
        lines[off + DESCENT] = below + extra;
        v += (below - above) + extra;
        lines[(this.mColumns + off) + TAB] = end;
        lines[(this.mColumns + off) + TOP] = v;
        int i2 = off + TAB;
        lines[i2] = lines[i2] | (TAB_MASK & flags);
        lines[off + HYPHEN] = flags;
        i2 = off + TAB;
        lines[i2] = lines[i2] | (dir << DIR_SHIFT);
        Directions linedirs = DIRS_ALL_LEFT_TO_RIGHT;
        if (easy) {
            this.mLineDirections[j] = linedirs;
        } else {
            this.mLineDirections[j] = AndroidBidi.directions(dir, chdirs, start - widthStart, chs, start - widthStart, end - start);
        }
        if (ellipsize != null) {
            boolean doEllipsis;
            boolean forceEllipsis = moreChars && this.mLineCount + TOP == this.mMaximumVisibleLineCount;
            if (((this.mMaximumVisibleLineCount == TOP && moreChars) || (firstLine && !moreChars)) && ellipsize != TruncateAt.MARQUEE) {
                doEllipsis = true;
            } else if (firstLine || (!currentLineIsTheLastVisibleOne && moreChars)) {
                doEllipsis = false;
            } else {
                doEllipsis = ellipsize == TruncateAt.END;
            }
            if (doEllipsis) {
                calculateEllipsis(start, end, widths, widthStart, ellipsisWidth, ellipsize, j, textWidth, paint, forceEllipsis);
            }
        }
        this.mLineCount += TOP;
        return v;
    }

    private void calculateEllipsis(int lineStart, int lineEnd, float[] widths, int widthStart, float avail, TruncateAt where, int line, float textWidth, TextPaint paint, boolean forceEllipsis) {
        if (textWidth > avail || forceEllipsis) {
            float ellipsisWidth = paint.measureText(where == TruncateAt.END_SMALL ? TextUtils.ELLIPSIS_TWO_DOTS : TextUtils.ELLIPSIS_NORMAL, TAB, TOP);
            int ellipsisStart = TAB;
            int ellipsisCount = TAB;
            int len = lineEnd - lineStart;
            int i;
            float sum;
            int i2;
            float w;
            if (where == TruncateAt.START) {
                i = this.mMaximumVisibleLineCount;
                if (r0 == TOP) {
                    sum = 0.0f;
                    i2 = len;
                    while (i2 > 0) {
                        w = widths[((i2 - 1) + lineStart) - widthStart];
                        if ((w + sum) + ellipsisWidth > avail) {
                            break;
                        }
                        sum += w;
                        i2--;
                    }
                    ellipsisStart = TAB;
                    ellipsisCount = i2;
                } else if (Log.isLoggable(TAG, ELLIPSIS_COUNT)) {
                    Log.w(TAG, "Start Ellipsis only supported with one line");
                }
            } else if (where == TruncateAt.END || where == TruncateAt.MARQUEE || where == TruncateAt.END_SMALL) {
                sum = 0.0f;
                i2 = TAB;
                while (i2 < len) {
                    w = widths[(i2 + lineStart) - widthStart];
                    if ((w + sum) + ellipsisWidth > avail) {
                        break;
                    }
                    sum += w;
                    i2 += TOP;
                }
                ellipsisStart = i2;
                ellipsisCount = len - i2;
                if (forceEllipsis && ellipsisCount == 0 && len > 0) {
                    ellipsisStart = len - 1;
                    ellipsisCount = TOP;
                }
            } else {
                i = this.mMaximumVisibleLineCount;
                if (r0 == TOP) {
                    float lsum = 0.0f;
                    float rsum = 0.0f;
                    int right = len;
                    float ravail = (avail - ellipsisWidth) / 2.0f;
                    right = len;
                    while (right > 0) {
                        w = widths[((right - 1) + lineStart) - widthStart];
                        if (w + rsum > ravail) {
                            break;
                        }
                        rsum += w;
                        right--;
                    }
                    float lavail = (avail - ellipsisWidth) - rsum;
                    int left = TAB;
                    while (left < right) {
                        w = widths[(left + lineStart) - widthStart];
                        if (w + lsum > lavail) {
                            break;
                        }
                        lsum += w;
                        left += TOP;
                    }
                    ellipsisStart = left;
                    ellipsisCount = right - left;
                } else if (Log.isLoggable(TAG, ELLIPSIS_COUNT)) {
                    Log.w(TAG, "Middle Ellipsis only supported with one line");
                }
            }
            this.mLines[(this.mColumns * line) + ELLIPSIS_START] = ellipsisStart;
            this.mLines[(this.mColumns * line) + ELLIPSIS_COUNT] = ellipsisCount;
            return;
        }
        this.mLines[(this.mColumns * line) + ELLIPSIS_START] = TAB;
        this.mLines[(this.mColumns * line) + ELLIPSIS_COUNT] = TAB;
    }

    public int getLineForVertical(int vertical) {
        int high = this.mLineCount;
        int low = -1;
        int[] lines = this.mLines;
        while (high - low > TOP) {
            int guess = (high + low) >> TOP;
            if (lines[(this.mColumns * guess) + TOP] > vertical) {
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return TAB;
        }
        return low;
    }

    public int getLineCount() {
        return this.mLineCount;
    }

    public int getLineTop(int line) {
        return this.mLines[(this.mColumns * line) + TOP];
    }

    public int getLineDescent(int line) {
        return this.mLines[(this.mColumns * line) + DESCENT];
    }

    public int getLineStart(int line) {
        return this.mLines[(this.mColumns * line) + TAB] & START_MASK;
    }

    public int getParagraphDirection(int line) {
        return this.mLines[(this.mColumns * line) + TAB] >> DIR_SHIFT;
    }

    public boolean getLineContainsTab(int line) {
        return (this.mLines[(this.mColumns * line) + TAB] & TAB_MASK) != 0;
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
        return this.mLines[(this.mColumns * line) + HYPHEN] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
    }

    public int getIndentAdjust(int line, Alignment align) {
        if (align == Alignment.ALIGN_LEFT) {
            if (this.mLeftIndents == null) {
                return TAB;
            }
            return this.mLeftIndents[Math.min(line, this.mLeftIndents.length - 1)];
        } else if (align == Alignment.ALIGN_RIGHT) {
            if (this.mRightIndents == null) {
                return TAB;
            }
            return -this.mRightIndents[Math.min(line, this.mRightIndents.length - 1)];
        } else if (align == Alignment.ALIGN_CENTER) {
            int left = TAB;
            if (this.mLeftIndents != null) {
                left = this.mLeftIndents[Math.min(line, this.mLeftIndents.length - 1)];
            }
            int right = TAB;
            if (this.mRightIndents != null) {
                right = this.mRightIndents[Math.min(line, this.mRightIndents.length - 1)];
            }
            return (left - right) >> TOP;
        } else {
            throw new AssertionError("unhandled alignment " + align);
        }
    }

    public int getEllipsisCount(int line) {
        if (this.mColumns < COLUMNS_ELLIPSIZE) {
            return TAB;
        }
        return this.mLines[(this.mColumns * line) + ELLIPSIS_COUNT];
    }

    public int getEllipsisStart(int line) {
        if (this.mColumns < COLUMNS_ELLIPSIZE) {
            return TAB;
        }
        return this.mLines[(this.mColumns * line) + ELLIPSIS_START];
    }

    public int getEllipsizedWidth() {
        return this.mEllipsizedWidth;
    }
}

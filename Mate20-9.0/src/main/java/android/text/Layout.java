package android.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.ParagraphStyle;
import android.text.style.ReplacementSpan;
import android.text.style.TabStopSpan;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public abstract class Layout {
    public static final int BREAK_STRATEGY_BALANCED = 2;
    public static final int BREAK_STRATEGY_HIGH_QUALITY = 1;
    public static final int BREAK_STRATEGY_SIMPLE = 0;
    public static final float DEFAULT_LINESPACING_ADDITION = 0.0f;
    public static final float DEFAULT_LINESPACING_MULTIPLIER = 1.0f;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new int[]{0, RUN_LENGTH_MASK});
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static final Directions DIRS_ALL_RIGHT_TO_LEFT = new Directions(new int[]{0, 134217727});
    public static final int DIR_LEFT_TO_RIGHT = 1;
    static final int DIR_REQUEST_DEFAULT_LTR = 2;
    static final int DIR_REQUEST_DEFAULT_RTL = -2;
    static final int DIR_REQUEST_LTR = 1;
    static final int DIR_REQUEST_RTL = -1;
    public static final int DIR_RIGHT_TO_LEFT = -1;
    public static final int HYPHENATION_FREQUENCY_FULL = 2;
    public static final int HYPHENATION_FREQUENCY_NONE = 0;
    public static final int HYPHENATION_FREQUENCY_NORMAL = 1;
    public static final int JUSTIFICATION_MODE_INTER_WORD = 1;
    public static final int JUSTIFICATION_MODE_NONE = 0;
    private static final ParagraphStyle[] NO_PARA_SPANS = ((ParagraphStyle[]) ArrayUtils.emptyArray(ParagraphStyle.class));
    static final int RUN_LENGTH_MASK = 67108863;
    static final int RUN_LEVEL_MASK = 63;
    static final int RUN_LEVEL_SHIFT = 26;
    static final int RUN_RTL_FLAG = 67108864;
    private static final int TAB_INCREMENT = 20;
    public static final int TEXT_SELECTION_LAYOUT_LEFT_TO_RIGHT = 1;
    public static final int TEXT_SELECTION_LAYOUT_RIGHT_TO_LEFT = 0;
    private static final Rect sTempRect = new Rect();
    private Alignment mAlignment;
    private int mJustificationMode;
    private SpanSet<LineBackgroundSpan> mLineBackgroundSpans;
    private TextPaint mPaint;
    private float mSpacingAdd;
    private float mSpacingMult;
    private boolean mSpannedText;
    private CharSequence mText;
    private TextDirectionHeuristic mTextDir;
    private int mWidth;
    private TextPaint mWorkPaint;

    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        ALIGN_LEFT,
        ALIGN_RIGHT
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface BreakStrategy {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Direction {
    }

    public static class Directions {
        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public int[] mDirections;

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public Directions(int[] dirs) {
            this.mDirections = dirs;
        }
    }

    static class Ellipsizer implements CharSequence, GetChars {
        Layout mLayout;
        TextUtils.TruncateAt mMethod;
        CharSequence mText;
        int mWidth;

        public Ellipsizer(CharSequence s) {
            this.mText = s;
        }

        public char charAt(int off) {
            char[] buf = TextUtils.obtain(1);
            getChars(off, off + 1, buf, 0);
            char ret = buf[0];
            TextUtils.recycle(buf);
            return ret;
        }

        public void getChars(int start, int end, char[] dest, int destoff) {
            int line1 = this.mLayout.getLineForOffset(start);
            int line2 = this.mLayout.getLineForOffset(end);
            TextUtils.getChars(this.mText, start, end, dest, destoff);
            for (int i = line1; i <= line2; i++) {
                this.mLayout.ellipsize(start, end, i, dest, destoff, this.mMethod);
            }
        }

        public int length() {
            return this.mText.length();
        }

        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, 0);
            return new String(s);
        }

        public String toString() {
            char[] s = new char[length()];
            getChars(0, length(), s, 0);
            return new String(s);
        }
    }

    private class HorizontalMeasurementProvider {
        private float[] mHorizontals;
        private final int mLine;
        private int mLineStartOffset;
        private final boolean mPrimary;

        HorizontalMeasurementProvider(int line, boolean primary) {
            this.mLine = line;
            this.mPrimary = primary;
            init();
        }

        private void init() {
            if (Layout.this.getLineDirections(this.mLine) != Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                this.mHorizontals = Layout.this.getLineHorizontals(this.mLine, false, this.mPrimary);
                this.mLineStartOffset = Layout.this.getLineStart(this.mLine);
            }
        }

        /* access modifiers changed from: package-private */
        public float get(int offset) {
            if (this.mHorizontals == null || offset < this.mLineStartOffset || offset >= this.mLineStartOffset + this.mHorizontals.length) {
                return Layout.this.getHorizontal(offset, this.mPrimary);
            }
            return this.mHorizontals[offset - this.mLineStartOffset];
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface HyphenationFrequency {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface JustificationMode {
    }

    @FunctionalInterface
    public interface SelectionRectangleConsumer {
        void accept(float f, float f2, float f3, float f4, int i);
    }

    static class SpannedEllipsizer extends Ellipsizer implements Spanned {
        private Spanned mSpanned;

        public SpannedEllipsizer(CharSequence display) {
            super(display);
            this.mSpanned = (Spanned) display;
        }

        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return this.mSpanned.getSpans(start, end, type);
        }

        public int getSpanStart(Object tag) {
            return this.mSpanned.getSpanStart(tag);
        }

        public int getSpanEnd(Object tag) {
            return this.mSpanned.getSpanEnd(tag);
        }

        public int getSpanFlags(Object tag) {
            return this.mSpanned.getSpanFlags(tag);
        }

        public int nextSpanTransition(int start, int limit, Class type) {
            return this.mSpanned.nextSpanTransition(start, limit, type);
        }

        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, 0);
            SpannableString ss = new SpannableString(new String(s));
            TextUtils.copySpansFrom(this.mSpanned, start, end, Object.class, ss, 0);
            return ss;
        }
    }

    static class TabStops {
        private int mIncrement;
        private int mNumStops;
        private int[] mStops;

        TabStops(int increment, Object[] spans) {
            reset(increment, spans);
        }

        /* access modifiers changed from: package-private */
        public void reset(int increment, Object[] spans) {
            this.mIncrement = increment;
            int ns = 0;
            if (spans != null) {
                int[] stops = this.mStops;
                int[] stops2 = stops;
                int ns2 = 0;
                for (TabStopSpan tabStopSpan : spans) {
                    if (tabStopSpan instanceof TabStopSpan) {
                        if (stops2 == null) {
                            stops2 = new int[10];
                        } else if (ns2 == stops2.length) {
                            int[] nstops = new int[(ns2 * 2)];
                            for (int i = 0; i < ns2; i++) {
                                nstops[i] = stops2[i];
                            }
                            stops2 = nstops;
                        }
                        stops2[ns2] = tabStopSpan.getTabStop();
                        ns2++;
                    }
                }
                if (ns2 > 1) {
                    Arrays.sort(stops2, 0, ns2);
                }
                if (stops2 != this.mStops) {
                    this.mStops = stops2;
                }
                ns = ns2;
            }
            this.mNumStops = ns;
        }

        /* access modifiers changed from: package-private */
        public float nextTab(float h) {
            int ns = this.mNumStops;
            if (ns > 0) {
                int[] stops = this.mStops;
                for (int i = 0; i < ns; i++) {
                    int stop = stops[i];
                    if (((float) stop) > h) {
                        return (float) stop;
                    }
                }
            }
            return nextDefaultStop(h, this.mIncrement);
        }

        public static float nextDefaultStop(float h, int inc) {
            return (float) (((int) ((((float) inc) + h) / ((float) inc))) * inc);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TextSelectionLayout {
    }

    public abstract int getBottomPadding();

    public abstract int getEllipsisCount(int i);

    public abstract int getEllipsisStart(int i);

    public abstract boolean getLineContainsTab(int i);

    public abstract int getLineCount();

    public abstract int getLineDescent(int i);

    public abstract Directions getLineDirections(int i);

    public abstract int getLineStart(int i);

    public abstract int getLineTop(int i);

    public abstract int getParagraphDirection(int i);

    public abstract int getTopPadding();

    public static float getDesiredWidth(CharSequence source, TextPaint paint) {
        return getDesiredWidth(source, 0, source.length(), paint);
    }

    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint) {
        return getDesiredWidth(source, start, end, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint, TextDirectionHeuristic textDir) {
        return getDesiredWidthWithLimit(source, start, end, paint, textDir, Float.MAX_VALUE);
    }

    public static float getDesiredWidthWithLimit(CharSequence source, int start, int end, TextPaint paint, TextDirectionHeuristic textDir, float upperLimit) {
        float need = 0.0f;
        int i = start;
        while (i <= end) {
            int next = TextUtils.indexOf(source, 10, i, end);
            if (next < 0) {
                next = end;
            }
            float w = measurePara(paint, source, i, next, textDir);
            if (w > upperLimit) {
                return upperLimit;
            }
            if (w > need) {
                need = w;
            }
            i = next + 1;
        }
        return need;
    }

    protected Layout(CharSequence text, TextPaint paint, int width, Alignment align, float spacingMult, float spacingAdd) {
        this(text, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR, spacingMult, spacingAdd);
    }

    protected Layout(CharSequence text, TextPaint paint, int width, Alignment align, TextDirectionHeuristic textDir, float spacingMult, float spacingAdd) {
        this.mWorkPaint = new TextPaint();
        this.mAlignment = Alignment.ALIGN_NORMAL;
        if (width >= 0) {
            if (paint != null) {
                paint.bgColor = 0;
                paint.baselineShift = 0;
            }
            this.mText = text;
            this.mPaint = paint;
            this.mWidth = width;
            this.mAlignment = align;
            this.mSpacingMult = spacingMult;
            this.mSpacingAdd = spacingAdd;
            this.mSpannedText = text instanceof Spanned;
            this.mTextDir = textDir;
            return;
        }
        throw new IllegalArgumentException("Layout: " + width + " < 0");
    }

    /* access modifiers changed from: protected */
    public void setJustificationMode(int justificationMode) {
        this.mJustificationMode = justificationMode;
    }

    /* access modifiers changed from: package-private */
    public void replaceWith(CharSequence text, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd) {
        if (width >= 0) {
            this.mText = text;
            this.mPaint = paint;
            this.mWidth = width;
            this.mAlignment = align;
            this.mSpacingMult = spacingmult;
            this.mSpacingAdd = spacingadd;
            this.mSpannedText = text instanceof Spanned;
            return;
        }
        throw new IllegalArgumentException("Layout: " + width + " < 0");
    }

    public void draw(Canvas c) {
        draw(c, null, null, 0);
    }

    public void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical) {
        long lineRange = getLineRangeForDraw(canvas);
        int firstLine = TextUtils.unpackRangeStartFromLong(lineRange);
        int lastLine = TextUtils.unpackRangeEndFromLong(lineRange);
        if (lastLine >= 0) {
            drawBackground(canvas, highlight, highlightPaint, cursorOffsetVertical, firstLine, lastLine);
            drawText(canvas, firstLine, lastLine);
        }
    }

    private boolean isJustificationRequired(int lineNum) {
        boolean z = false;
        if (this.mJustificationMode == 0) {
            return false;
        }
        int lineEnd = getLineEnd(lineNum);
        if (lineEnd < this.mText.length() && this.mText.charAt(lineEnd - 1) != 10) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: type inference failed for: r13v5, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    private float getJustifyWidth(int lineNum) {
        Alignment align;
        int indentWidth;
        int i = lineNum;
        Alignment paraAlign = this.mAlignment;
        int left = 0;
        int right = this.mWidth;
        int dir = getParagraphDirection(lineNum);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        if (this.mSpannedText) {
            Spanned sp = (Spanned) this.mText;
            int start = getLineStart(lineNum);
            boolean isFirstParaLine = start == 0 || this.mText.charAt(start + -1) == 10;
            if (isFirstParaLine) {
                spans = getParagraphSpans(sp, start, sp.nextSpanTransition(start, this.mText.length(), ParagraphStyle.class), ParagraphStyle.class);
                int n = spans.length - 1;
                while (true) {
                    if (n < 0) {
                        break;
                    } else if (spans[n] instanceof AlignmentSpan) {
                        paraAlign = ((AlignmentSpan) spans[n]).getAlignment();
                        break;
                    } else {
                        n--;
                    }
                }
            }
            int length = spans.length;
            boolean useFirstLineMargin = isFirstParaLine;
            int n2 = 0;
            while (true) {
                if (n2 >= length) {
                    break;
                }
                if (spans[n2] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                    if (i < getLineForOffset(sp.getSpanStart(spans[n2])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans[n2]).getLeadingMarginLineCount()) {
                        useFirstLineMargin = true;
                        break;
                    }
                }
                n2++;
            }
            int n3 = 0;
            while (true) {
                int n4 = n3;
                if (n4 >= length) {
                    break;
                }
                if (spans[n4] instanceof LeadingMarginSpan) {
                    LeadingMarginSpan margin = (LeadingMarginSpan) spans[n4];
                    if (dir == -1) {
                        right -= margin.getLeadingMargin(useFirstLineMargin);
                    } else {
                        left += margin.getLeadingMargin(useFirstLineMargin);
                    }
                }
                n3 = n4 + 1;
            }
        }
        if (paraAlign == Alignment.ALIGN_LEFT) {
            align = dir == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
        } else if (paraAlign == Alignment.ALIGN_RIGHT) {
            align = dir == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
        } else {
            align = paraAlign;
        }
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == 1) {
                indentWidth = getIndentAdjust(i, Alignment.ALIGN_LEFT);
            } else {
                indentWidth = -getIndentAdjust(i, Alignment.ALIGN_RIGHT);
            }
        } else if (align != Alignment.ALIGN_OPPOSITE) {
            indentWidth = getIndentAdjust(i, Alignment.ALIGN_CENTER);
        } else if (dir == 1) {
            indentWidth = -getIndentAdjust(i, Alignment.ALIGN_RIGHT);
        } else {
            indentWidth = getIndentAdjust(i, Alignment.ALIGN_LEFT);
        }
        return (float) ((right - left) - indentWidth);
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0108 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x01ed A[EDGE_INSN: B:109:0x01ed->B:48:0x01ed ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00c9  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d9  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0116  */
    public void drawText(Canvas canvas, int firstLine, int lastLine) {
        CharSequence buf;
        TextLine tl;
        TabStops tabStops;
        int lineNum;
        int lbaseline;
        int dir;
        int right;
        int start;
        int spanEnd;
        boolean tabStopsIsInitialized;
        int left;
        int ltop;
        Layout layout;
        ParagraphStyle[] spans;
        TextPaint paint;
        TabStops tabStops2;
        int dir2;
        int i;
        int indentWidth;
        int x;
        ParagraphStyle[] spans2;
        TextLine tl2;
        int indentWidth2;
        int x2;
        int indentWidth3;
        TabStops tabStops3;
        Alignment paraAlign;
        ParagraphStyle[] spans3;
        boolean z;
        ParagraphStyle[] spans4;
        int length;
        boolean useFirstLineMargin;
        int n;
        Spanned sp;
        boolean useFirstLineMargin2;
        int n2;
        int n3;
        CharSequence buf2;
        TextLine tl3;
        TabStops tabStops4;
        int lineNum2;
        ParagraphStyle[] spans5;
        int lbaseline2;
        int dir3;
        int length2;
        int n4;
        int ltop2;
        Spanned sp2;
        int textLength;
        Layout layout2;
        boolean useFirstLineMargin3;
        TextPaint paint2;
        boolean useFirstLineMargin4;
        Layout layout3 = this;
        int i2 = firstLine;
        int previousLineBottom = layout3.getLineTop(i2);
        int lineNum3 = layout3.getLineStart(i2);
        ParagraphStyle[] spans6 = NO_PARA_SPANS;
        TextPaint paint3 = layout3.mWorkPaint;
        paint3.set(layout3.mPaint);
        CharSequence buf3 = layout3.mText;
        Alignment paraAlign2 = layout3.mAlignment;
        TextLine tl4 = TextLine.obtain();
        TabStops tabStops5 = null;
        boolean tabStopsIsInitialized2 = false;
        int spanEnd2 = 0;
        ParagraphStyle[] spans7 = spans6;
        int previousLineBottom2 = previousLineBottom;
        int lineNum4 = i2;
        while (true) {
            int lineNum5 = lineNum4;
            if (lineNum5 <= lastLine) {
                int start2 = lineNum3;
                int previousLineEnd = layout3.getLineStart(lineNum5 + 1);
                boolean justify = layout3.isJustificationRequired(lineNum5);
                int end = layout3.getLineVisibleEnd(lineNum5, start2, previousLineEnd);
                paint3.setHyphenEdit(layout3.getHyphen(lineNum5));
                int ltop3 = previousLineBottom2;
                int previousLineEnd2 = previousLineEnd;
                int lbottom = layout3.getLineTop(lineNum5 + 1);
                int previousLineBottom3 = lbottom;
                int lbaseline3 = lbottom - layout3.getLineDescent(lineNum5);
                int n5 = layout3.getParagraphDirection(lineNum5);
                int lbaseline4 = lbaseline3;
                int right2 = layout3.mWidth;
                int textLength2 = ltop3;
                if (layout3.mSpannedText != 0) {
                    Spanned sp3 = (Spanned) buf3;
                    int ltop4 = buf3.length();
                    if (start2 != 0) {
                        spans3 = spans7;
                        paraAlign = paraAlign2;
                        if (buf3.charAt(start2 - 1) != 10) {
                            z = false;
                            boolean isFirstParaLine = z;
                            if (start2 >= spanEnd2) {
                                start = start2;
                            } else if (lineNum5 == i2 || isFirstParaLine) {
                                spanEnd2 = sp3.nextSpanTransition(start2, ltop4, ParagraphStyle.class);
                                ParagraphStyle[] spans8 = (ParagraphStyle[]) getParagraphSpans(sp3, start2, spanEnd2, ParagraphStyle.class);
                                Alignment paraAlign3 = layout3.mAlignment;
                                start = start2;
                                int n6 = spans8.length - 1;
                                while (true) {
                                    if (n6 < 0) {
                                        break;
                                    }
                                    Alignment paraAlign4 = paraAlign3;
                                    if (spans8[n6] instanceof AlignmentSpan) {
                                        paraAlign3 = ((AlignmentSpan) spans8[n6]).getAlignment();
                                        break;
                                    } else {
                                        n6--;
                                        paraAlign3 = paraAlign4;
                                    }
                                }
                                tabStopsIsInitialized = false;
                                spans4 = spans8;
                                paraAlign = paraAlign3;
                                spanEnd = spanEnd2;
                                length = spans4.length;
                                useFirstLineMargin = isFirstParaLine;
                                n = 0;
                                while (true) {
                                    if (n >= length) {
                                        sp = sp3;
                                        useFirstLineMargin2 = useFirstLineMargin;
                                        break;
                                    }
                                    if (spans4[n] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                                        useFirstLineMargin4 = useFirstLineMargin;
                                        sp = sp3;
                                        if (lineNum5 < layout3.getLineForOffset(sp3.getSpanStart(spans4[n])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans4[n]).getLeadingMarginLineCount()) {
                                            useFirstLineMargin2 = true;
                                            break;
                                        }
                                    } else {
                                        useFirstLineMargin4 = useFirstLineMargin;
                                        sp = sp3;
                                    }
                                    n++;
                                    useFirstLineMargin = useFirstLineMargin4;
                                    sp3 = sp;
                                }
                                right = right2;
                                n2 = 0;
                                left = 0;
                                while (true) {
                                    n3 = n2;
                                    if (n3 >= length) {
                                        break;
                                    }
                                    if (spans4[n3] instanceof LeadingMarginSpan) {
                                        LeadingMarginSpan margin = (LeadingMarginSpan) spans4[n3];
                                        if (n5 == -1) {
                                            dir3 = n5;
                                            lbaseline2 = lbaseline4;
                                            sp2 = sp;
                                            n4 = n3;
                                            TextPaint paint4 = paint3;
                                            boolean useFirstLineMargin5 = useFirstLineMargin2;
                                            length2 = length;
                                            spans5 = spans4;
                                            lineNum2 = lineNum5;
                                            tabStops4 = tabStops5;
                                            tl3 = tl4;
                                            buf2 = buf3;
                                            int i3 = textLength2;
                                            ltop2 = ltop4;
                                            textLength = i3;
                                            margin.drawLeadingMargin(canvas, paint3, right, dir3, textLength, lbaseline2, lbottom, buf3, start, end, isFirstParaLine, layout3);
                                            right -= margin.getLeadingMargin(useFirstLineMargin5);
                                            useFirstLineMargin3 = useFirstLineMargin5;
                                            layout2 = layout3;
                                            paint2 = paint4;
                                        } else {
                                            dir3 = n5;
                                            LeadingMarginSpan margin2 = margin;
                                            n4 = n3;
                                            spans5 = spans4;
                                            lineNum2 = lineNum5;
                                            tabStops4 = tabStops5;
                                            tl3 = tl4;
                                            buf2 = buf3;
                                            TextPaint paint5 = paint3;
                                            lbaseline2 = lbaseline4;
                                            sp2 = sp;
                                            length2 = length;
                                            int i4 = textLength2;
                                            ltop2 = ltop4;
                                            textLength = i4;
                                            useFirstLineMargin3 = useFirstLineMargin2;
                                            paint2 = paint5;
                                            layout2 = layout3;
                                            margin2.drawLeadingMargin(canvas, paint5, left, dir3, textLength, lbaseline2, lbottom, buf2, start, end, isFirstParaLine, layout2);
                                            left += margin2.getLeadingMargin(useFirstLineMargin3);
                                        }
                                    } else {
                                        dir3 = n5;
                                        n4 = n3;
                                        useFirstLineMargin3 = useFirstLineMargin2;
                                        spans5 = spans4;
                                        lineNum2 = lineNum5;
                                        tabStops4 = tabStops5;
                                        tl3 = tl4;
                                        buf2 = buf3;
                                        paint2 = paint3;
                                        layout2 = layout3;
                                        lbaseline2 = lbaseline4;
                                        sp2 = sp;
                                        length2 = length;
                                        int i5 = textLength2;
                                        ltop2 = ltop4;
                                        textLength = i5;
                                    }
                                    int i6 = firstLine;
                                    int i7 = lastLine;
                                    paint3 = paint2;
                                    n2 = n4 + 1;
                                    useFirstLineMargin2 = useFirstLineMargin3;
                                    layout3 = layout2;
                                    length = length2;
                                    n5 = dir3;
                                    spans4 = spans5;
                                    lineNum5 = lineNum2;
                                    tabStops5 = tabStops4;
                                    tl4 = tl3;
                                    buf3 = buf2;
                                    sp = sp2;
                                    lbaseline4 = lbaseline2;
                                    int i8 = ltop2;
                                    textLength2 = textLength;
                                    ltop4 = i8;
                                }
                                dir = n5;
                                lineNum = lineNum5;
                                tabStops = tabStops5;
                                tl = tl4;
                                buf = buf3;
                                paint = paint3;
                                layout = layout3;
                                lbaseline = lbaseline4;
                                ltop = textLength2;
                                paraAlign2 = paraAlign;
                                spans = spans4;
                            } else {
                                start = start2;
                            }
                            tabStopsIsInitialized = tabStopsIsInitialized2;
                            spans4 = spans3;
                            spanEnd = spanEnd2;
                            length = spans4.length;
                            useFirstLineMargin = isFirstParaLine;
                            n = 0;
                            while (true) {
                                if (n >= length) {
                                }
                                n++;
                                useFirstLineMargin = useFirstLineMargin4;
                                sp3 = sp;
                            }
                            right = right2;
                            n2 = 0;
                            left = 0;
                            while (true) {
                                n3 = n2;
                                if (n3 >= length) {
                                }
                                int i62 = firstLine;
                                int i72 = lastLine;
                                paint3 = paint2;
                                n2 = n4 + 1;
                                useFirstLineMargin2 = useFirstLineMargin3;
                                layout3 = layout2;
                                length = length2;
                                n5 = dir3;
                                spans4 = spans5;
                                lineNum5 = lineNum2;
                                tabStops5 = tabStops4;
                                tl4 = tl3;
                                buf3 = buf2;
                                sp = sp2;
                                lbaseline4 = lbaseline2;
                                int i82 = ltop2;
                                textLength2 = textLength;
                                ltop4 = i82;
                            }
                            dir = n5;
                            lineNum = lineNum5;
                            tabStops = tabStops5;
                            tl = tl4;
                            buf = buf3;
                            paint = paint3;
                            layout = layout3;
                            lbaseline = lbaseline4;
                            ltop = textLength2;
                            paraAlign2 = paraAlign;
                            spans = spans4;
                        }
                    } else {
                        spans3 = spans7;
                        paraAlign = paraAlign2;
                    }
                    z = true;
                    boolean isFirstParaLine2 = z;
                    if (start2 >= spanEnd2) {
                    }
                    tabStopsIsInitialized = tabStopsIsInitialized2;
                    spans4 = spans3;
                    spanEnd = spanEnd2;
                    length = spans4.length;
                    useFirstLineMargin = isFirstParaLine2;
                    n = 0;
                    while (true) {
                        if (n >= length) {
                        }
                        n++;
                        useFirstLineMargin = useFirstLineMargin4;
                        sp3 = sp;
                    }
                    right = right2;
                    n2 = 0;
                    left = 0;
                    while (true) {
                        n3 = n2;
                        if (n3 >= length) {
                        }
                        int i622 = firstLine;
                        int i722 = lastLine;
                        paint3 = paint2;
                        n2 = n4 + 1;
                        useFirstLineMargin2 = useFirstLineMargin3;
                        layout3 = layout2;
                        length = length2;
                        n5 = dir3;
                        spans4 = spans5;
                        lineNum5 = lineNum2;
                        tabStops5 = tabStops4;
                        tl4 = tl3;
                        buf3 = buf2;
                        sp = sp2;
                        lbaseline4 = lbaseline2;
                        int i822 = ltop2;
                        textLength2 = textLength;
                        ltop4 = i822;
                    }
                    dir = n5;
                    lineNum = lineNum5;
                    tabStops = tabStops5;
                    tl = tl4;
                    buf = buf3;
                    paint = paint3;
                    layout = layout3;
                    lbaseline = lbaseline4;
                    ltop = textLength2;
                    paraAlign2 = paraAlign;
                    spans = spans4;
                } else {
                    start = start2;
                    dir = n5;
                    ParagraphStyle[] paragraphStyleArr = spans7;
                    Alignment alignment = paraAlign2;
                    lineNum = lineNum5;
                    tabStops = tabStops5;
                    tl = tl4;
                    buf = buf3;
                    paint = paint3;
                    layout = layout3;
                    lbaseline = lbaseline4;
                    ltop = textLength2;
                    tabStopsIsInitialized = tabStopsIsInitialized2;
                    right = right2;
                    spans = paragraphStyleArr;
                    spanEnd = spanEnd2;
                    left = 0;
                }
                int lineNum6 = lineNum;
                boolean hasTab = layout.getLineContainsTab(lineNum6);
                if (!hasTab || tabStopsIsInitialized) {
                    tabStops2 = tabStops;
                } else {
                    TabStops tabStops6 = tabStops;
                    if (tabStops6 == null) {
                        tabStops3 = new TabStops(20, spans);
                    } else {
                        tabStops6.reset(20, spans);
                        tabStops3 = tabStops6;
                    }
                    tabStopsIsInitialized = true;
                    tabStops2 = tabStops3;
                }
                Alignment align = paraAlign2;
                if (align == Alignment.ALIGN_LEFT) {
                    dir2 = dir;
                    i = 1;
                    align = dir2 == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
                } else {
                    dir2 = dir;
                    i = 1;
                    if (align == Alignment.ALIGN_RIGHT) {
                        align = dir2 == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
                    }
                }
                Alignment align2 = align;
                if (align2 == Alignment.ALIGN_NORMAL) {
                    if (dir2 == i) {
                        indentWidth3 = layout.getIndentAdjust(lineNum6, Alignment.ALIGN_LEFT);
                        x = left + indentWidth3;
                    } else {
                        indentWidth3 = -layout.getIndentAdjust(lineNum6, Alignment.ALIGN_RIGHT);
                        x = right - indentWidth3;
                    }
                    indentWidth = indentWidth3;
                } else {
                    int max = (int) layout.getLineExtent(lineNum6, tabStops2, false);
                    if (align2 == Alignment.ALIGN_OPPOSITE) {
                        if (dir2 == i) {
                            indentWidth2 = -layout.getIndentAdjust(lineNum6, Alignment.ALIGN_RIGHT);
                            x2 = (right - max) - indentWidth2;
                        } else {
                            indentWidth2 = layout.getIndentAdjust(lineNum6, Alignment.ALIGN_LEFT);
                            x2 = (left - max) + indentWidth2;
                        }
                        indentWidth = indentWidth2;
                        x = x2;
                    } else {
                        int indentWidth4 = layout.getIndentAdjust(lineNum6, Alignment.ALIGN_CENTER);
                        x = (((right + left) - (max & -2)) >> 1) + indentWidth4;
                        indentWidth = indentWidth4;
                    }
                }
                int x3 = x;
                Directions directions = layout.getLineDirections(lineNum6);
                if (directions != DIRS_ALL_LEFT_TO_RIGHT || layout.mSpannedText || hasTab || justify) {
                    spans2 = spans;
                    int x4 = x3;
                    int lbaseline5 = lbaseline;
                    Alignment alignment2 = align2;
                    int i9 = dir2;
                    tl.set(paint, buf, start, end, dir2, directions, hasTab, tabStops2);
                    if (justify) {
                        tl2 = tl;
                        tl2.justify((float) ((right - left) - indentWidth));
                    } else {
                        tl2 = tl;
                    }
                    tabStops5 = tabStops2;
                    tl2.draw(canvas, (float) x4, ltop, lbaseline5, lbottom);
                } else {
                    int lbaseline6 = lbaseline;
                    int i10 = lbaseline6;
                    Directions directions2 = directions;
                    spans2 = spans;
                    int i11 = x3;
                    canvas.drawText(buf, start, end, (float) x3, (float) lbaseline6, paint);
                    tabStops5 = tabStops2;
                    tl2 = tl;
                }
                i2 = firstLine;
                paint3 = paint;
                lineNum4 = lineNum6 + 1;
                layout3 = layout;
                tl4 = tl2;
                lineNum3 = previousLineEnd2;
                previousLineBottom2 = previousLineBottom3;
                tabStopsIsInitialized2 = tabStopsIsInitialized;
                spanEnd2 = spanEnd;
                buf3 = buf;
                spans7 = spans2;
            } else {
                ParagraphStyle[] paragraphStyleArr2 = spans7;
                Alignment alignment3 = paraAlign2;
                TabStops tabStops7 = tabStops5;
                CharSequence charSequence = buf3;
                TextPaint textPaint = paint3;
                Layout layout4 = layout3;
                TextLine.recycle(tl4);
                return;
            }
        }
    }

    public void drawBackground(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int firstLine, int lastLine) {
        int spanEnd;
        int i;
        ParagraphStyle[] spans;
        int spansLength;
        Canvas canvas2 = canvas;
        int i2 = cursorOffsetVertical;
        int i3 = firstLine;
        if (this.mSpannedText) {
            if (this.mLineBackgroundSpans == null) {
                this.mLineBackgroundSpans = new SpanSet<>(LineBackgroundSpan.class);
            }
            Spanned buffer = (Spanned) this.mText;
            int textLength = buffer.length();
            this.mLineBackgroundSpans.init(buffer, 0, textLength);
            if (this.mLineBackgroundSpans.numberOfSpans > 0) {
                int previousLineBottom = getLineTop(i3);
                int previousLineEnd = getLineStart(i3);
                ParagraphStyle[] spans2 = NO_PARA_SPANS;
                TextPaint paint = this.mPaint;
                int width = this.mWidth;
                int spanEnd2 = 0;
                int spanEnd3 = 0;
                int spansLength2 = previousLineEnd;
                int previousLineEnd2 = previousLineBottom;
                int i4 = i3;
                while (i4 <= lastLine) {
                    int end = getLineStart(i4 + 1);
                    int previousLineEnd3 = end;
                    int ltop = previousLineEnd2;
                    int lbottom = getLineTop(i4 + 1);
                    int previousLineBottom2 = lbottom;
                    int lbaseline = lbottom - getLineDescent(i4);
                    int start = spansLength2;
                    if (start >= spanEnd2) {
                        int spanEnd4 = this.mLineBackgroundSpans.getNextTransition(start, textLength);
                        int spansLength3 = 0;
                        if (start != end || start == 0) {
                            ParagraphStyle[] spans3 = spans2;
                            int j = 0;
                            while (true) {
                                i = i4;
                                if (j >= this.mLineBackgroundSpans.numberOfSpans) {
                                    break;
                                }
                                if (this.mLineBackgroundSpans.spanStarts[j] < end && this.mLineBackgroundSpans.spanEnds[j] > start) {
                                    spansLength3++;
                                    spans3 = (ParagraphStyle[]) GrowingArrayUtils.append(spans3, spansLength3, ((LineBackgroundSpan[]) this.mLineBackgroundSpans.spans)[j]);
                                }
                                j++;
                                i4 = i;
                            }
                            spanEnd = spanEnd4;
                            spans = spans3;
                        } else {
                            i = i4;
                            spanEnd = spanEnd4;
                            spans = spans2;
                        }
                        spansLength = spansLength3;
                    } else {
                        i = i4;
                        spans = spans2;
                        spanEnd = spanEnd2;
                        spansLength = spanEnd3;
                    }
                    int n = 0;
                    while (true) {
                        int n2 = n;
                        if (n2 >= spansLength) {
                            break;
                        }
                        int start2 = start;
                        int end2 = end;
                        ((LineBackgroundSpan) spans[n2]).drawBackground(canvas2, paint, 0, width, ltop, lbaseline, lbottom, buffer, start2, end2, i);
                        n = n2 + 1;
                        end = end2;
                        start = start2;
                        spansLength = spansLength;
                        width = width;
                        paint = paint;
                        textLength = textLength;
                        buffer = buffer;
                    }
                    int spansLength4 = spansLength;
                    int i5 = width;
                    TextPaint textPaint = paint;
                    int i6 = textLength;
                    Spanned spanned = buffer;
                    i4 = i + 1;
                    spans2 = spans;
                    spansLength2 = previousLineEnd3;
                    previousLineEnd2 = previousLineBottom2;
                    spanEnd2 = spanEnd;
                    spanEnd3 = spansLength4;
                }
            }
            Spanned spanned2 = buffer;
            this.mLineBackgroundSpans.recycle();
        }
        if (highlight != null) {
            if (i2 != 0) {
                canvas2.translate(0.0f, (float) i2);
            }
            canvas.drawPath(highlight, highlightPaint);
            if (i2 != 0) {
                canvas2.translate(0.0f, (float) (-i2));
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
        r0 = java.lang.Math.max(r1, 0);
        r5 = java.lang.Math.min(getLineTop(getLineCount()), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002c, code lost:
        if (r0 < r5) goto L_0x0033;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        return android.text.TextUtils.packRangeInLong(0, -1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003f, code lost:
        return android.text.TextUtils.packRangeInLong(getLineForVertical(r0), getLineForVertical(r5));
     */
    public long getLineRangeForDraw(Canvas canvas) {
        synchronized (sTempRect) {
            if (!canvas.getClipBounds(sTempRect)) {
                long packRangeInLong = TextUtils.packRangeInLong(0, -1);
                return packRangeInLong;
            }
            int dtop = sTempRect.top;
            int dbottom = sTempRect.bottom;
        }
    }

    private int getLineStartPos(int line, int left, int right) {
        int x;
        Alignment align = getParagraphAlignment(line);
        int dir = getParagraphDirection(line);
        if (align == Alignment.ALIGN_LEFT) {
            align = dir == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
        } else if (align == Alignment.ALIGN_RIGHT) {
            align = dir == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
        }
        if (align != Alignment.ALIGN_NORMAL) {
            TabStops tabStops = null;
            if (this.mSpannedText && getLineContainsTab(line)) {
                Spanned spanned = (Spanned) this.mText;
                int start = getLineStart(line);
                TabStopSpan[] tabSpans = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, spanned.length(), TabStopSpan.class), TabStopSpan.class);
                if (tabSpans.length > 0) {
                    tabStops = new TabStops(20, tabSpans);
                }
            }
            int max = (int) getLineExtent(line, tabStops, false);
            if (align == Alignment.ALIGN_OPPOSITE) {
                if (dir == 1) {
                    x = (right - max) + getIndentAdjust(line, Alignment.ALIGN_RIGHT);
                } else {
                    x = (left - max) + getIndentAdjust(line, Alignment.ALIGN_LEFT);
                }
                return x;
            }
            return ((left + right) - (max & -2)) >> (1 + getIndentAdjust(line, Alignment.ALIGN_CENTER));
        } else if (dir == 1) {
            return getIndentAdjust(line, Alignment.ALIGN_LEFT) + left;
        } else {
            return getIndentAdjust(line, Alignment.ALIGN_RIGHT) + right;
        }
    }

    public final CharSequence getText() {
        return this.mText;
    }

    public final TextPaint getPaint() {
        return this.mPaint;
    }

    public final int getWidth() {
        return this.mWidth;
    }

    public int getEllipsizedWidth() {
        return this.mWidth;
    }

    public final void increaseWidthTo(int wid) {
        if (wid >= this.mWidth) {
            this.mWidth = wid;
            return;
        }
        throw new RuntimeException("attempted to reduce Layout width");
    }

    public int getHeight() {
        return getLineTop(getLineCount());
    }

    public int getHeight(boolean cap) {
        return getHeight();
    }

    public final Alignment getAlignment() {
        return this.mAlignment;
    }

    public final float getSpacingMultiplier() {
        return this.mSpacingMult;
    }

    public final float getSpacingAdd() {
        return this.mSpacingAdd;
    }

    public final TextDirectionHeuristic getTextDirectionHeuristic() {
        return this.mTextDir;
    }

    public int getLineBounds(int line, Rect bounds) {
        if (bounds != null) {
            bounds.left = 0;
            bounds.top = getLineTop(line);
            bounds.right = this.mWidth;
            bounds.bottom = getLineTop(line + 1);
        }
        return getLineBaseline(line);
    }

    public int getHyphen(int line) {
        return 0;
    }

    public int getIndentAdjust(int line, Alignment alignment) {
        return 0;
    }

    public boolean isLevelBoundary(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        boolean z = false;
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT || dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return false;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        if (offset == lineStart || offset == lineEnd) {
            if (((runs[(offset == lineStart ? 0 : runs.length - 2) + 1] >>> 26) & 63) != (getParagraphDirection(line) == 1 ? 0 : 1)) {
                z = true;
            }
            return z;
        }
        int offset2 = offset - lineStart;
        for (int i = 0; i < runs.length; i += 2) {
            if (offset2 == runs[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean isRtlCharAt(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        boolean z = false;
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT) {
            return false;
        }
        if (dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return true;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int i = 0;
        while (i < runs.length) {
            int start = runs[i] + lineStart;
            int limit = (runs[i + 1] & RUN_LENGTH_MASK) + start;
            if (offset < start || offset >= limit) {
                i += 2;
            } else {
                if (((runs[i + 1] >>> 26) & 63 & 1) != 0) {
                    z = true;
                }
                return z;
            }
        }
        return false;
    }

    public long getRunRange(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT || dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return TextUtils.packRangeInLong(0, getLineEnd(line));
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        for (int i = 0; i < runs.length; i += 2) {
            int start = runs[i] + lineStart;
            int limit = (runs[i + 1] & RUN_LENGTH_MASK) + start;
            if (offset >= start && offset < limit) {
                return TextUtils.packRangeInLong(start, limit);
            }
        }
        return TextUtils.packRangeInLong(0, getLineEnd(line));
    }

    private boolean primaryIsTrailingPrevious(int offset) {
        int line = getLineForOffset(offset);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        int levelAt = -1;
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= runs.length) {
                break;
            }
            int start = runs[i] + lineStart;
            int limit = (runs[i + 1] & RUN_LENGTH_MASK) + start;
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (offset < start || offset >= limit) {
                i += 2;
            } else if (offset > start) {
                return false;
            } else {
                levelAt = (runs[i + 1] >>> 26) & 63;
            }
        }
        if (levelAt == -1) {
            levelAt = getParagraphDirection(line) == 1 ? 0 : 1;
        }
        int levelBefore = -1;
        if (offset != lineStart) {
            int offset2 = offset - 1;
            int i2 = 0;
            while (true) {
                if (i2 >= runs.length) {
                    break;
                }
                int start2 = runs[i2] + lineStart;
                int limit2 = (runs[i2 + 1] & RUN_LENGTH_MASK) + start2;
                if (limit2 > lineEnd) {
                    limit2 = lineEnd;
                }
                if (offset2 >= start2 && offset2 < limit2) {
                    levelBefore = (runs[i2 + 1] >>> 26) & 63;
                    break;
                }
                i2 += 2;
            }
        } else {
            levelBefore = getParagraphDirection(line) == 1 ? 0 : 1;
        }
        if (levelBefore < levelAt) {
            z = true;
        }
        return z;
    }

    private boolean[] primaryIsTrailingPreviousAllLineOffsets(int line) {
        byte b;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        boolean[] trailing = new boolean[((lineEnd - lineStart) + 1)];
        byte[] level = new byte[((lineEnd - lineStart) + 1)];
        for (int i = 0; i < runs.length; i += 2) {
            int limit = (runs[i + 1] & RUN_LENGTH_MASK) + runs[i] + lineStart;
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            level[(limit - lineStart) - 1] = (byte) ((runs[i + 1] >>> 26) & 63);
        }
        for (int i2 = 0; i2 < runs.length; i2 += 2) {
            int start = runs[i2] + lineStart;
            byte currentLevel = (byte) ((runs[i2 + 1] >>> 26) & 63);
            int i3 = start - lineStart;
            if (start == lineStart) {
                b = getParagraphDirection(line) == 1 ? (byte) 0 : 1;
            } else {
                b = level[(start - lineStart) - 1];
            }
            trailing[i3] = currentLevel > b;
        }
        return trailing;
    }

    public float getPrimaryHorizontal(int offset) {
        return getPrimaryHorizontal(offset, false);
    }

    public float getPrimaryHorizontal(int offset, boolean clamped) {
        return getHorizontal(offset, primaryIsTrailingPrevious(offset), clamped);
    }

    public float getSecondaryHorizontal(int offset) {
        return getSecondaryHorizontal(offset, false);
    }

    public float getSecondaryHorizontal(int offset, boolean clamped) {
        return getHorizontal(offset, !primaryIsTrailingPrevious(offset), clamped);
    }

    /* access modifiers changed from: private */
    public float getHorizontal(int offset, boolean primary) {
        return primary ? getPrimaryHorizontal(offset) : getSecondaryHorizontal(offset);
    }

    private float getHorizontal(int offset, boolean trailing, boolean clamped) {
        return getHorizontal(offset, trailing, getLineForOffset(offset), clamped);
    }

    private float getHorizontal(int offset, boolean trailing, int line, boolean clamped) {
        int i = line;
        int start = getLineStart(i);
        int end = getLineEnd(i);
        int dir = getParagraphDirection(i);
        boolean hasTab = getLineContainsTab(i);
        Directions directions = getLineDirections(i);
        TabStops tabStops = null;
        if (hasTab && (this.mText instanceof Spanned)) {
            TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) this.mText, start, end, TabStopSpan.class);
            if (tabs.length > 0) {
                tabStops = new TabStops(20, tabs);
            }
        }
        TabStops tabStops2 = tabStops;
        TextLine tl = TextLine.obtain();
        int i2 = end;
        TextLine tl2 = tl;
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops2);
        float wid = tl2.measure(offset - start, trailing, null);
        TextLine.recycle(tl2);
        if (clamped && wid > ((float) this.mWidth)) {
            wid = (float) this.mWidth;
        }
        return ((float) getLineStartPos(i, getParagraphLeft(i), getParagraphRight(i))) + wid;
    }

    /* access modifiers changed from: private */
    public float[] getLineHorizontals(int line, boolean clamped, boolean primary) {
        int start = getLineStart(line);
        int end = getLineEnd(line);
        int dir = getParagraphDirection(line);
        boolean hasTab = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        TabStops tabStops = null;
        if (hasTab && (this.mText instanceof Spanned)) {
            TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) this.mText, start, end, TabStopSpan.class);
            if (tabs.length > 0) {
                tabStops = new TabStops(20, tabs);
            }
        }
        TabStops tabStops2 = tabStops;
        TextLine tl = TextLine.obtain();
        int i = dir;
        TextLine tl2 = tl;
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops2);
        boolean[] trailings = primaryIsTrailingPreviousAllLineOffsets(line);
        if (!primary) {
            for (int offset = 0; offset < trailings.length; offset++) {
                trailings[offset] = !trailings[offset];
            }
        }
        float[] wid = tl2.measureAllOffsets(trailings, null);
        TextLine.recycle(tl2);
        if (clamped) {
            for (int offset2 = 0; offset2 <= wid.length; offset2++) {
                if (wid[offset2] > ((float) this.mWidth)) {
                    wid[offset2] = (float) this.mWidth;
                }
            }
        }
        int lineStartPos = getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line));
        float[] horizontal = new float[((end - start) + 1)];
        int offset3 = 0;
        while (true) {
            boolean[] trailings2 = trailings;
            int offset4 = offset3;
            if (offset4 >= horizontal.length) {
                return horizontal;
            }
            horizontal[offset4] = ((float) lineStartPos) + wid[offset4];
            offset3 = offset4 + 1;
            trailings = trailings2;
        }
    }

    public float getLineLeft(int line) {
        int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        if (align == Alignment.ALIGN_LEFT) {
            return 0.0f;
        }
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == -1) {
                return ((float) getParagraphRight(line)) - getLineMax(line);
            }
            return 0.0f;
        } else if (align == Alignment.ALIGN_RIGHT) {
            return ((float) this.mWidth) - getLineMax(line);
        } else {
            if (align != Alignment.ALIGN_OPPOSITE) {
                int left = getParagraphLeft(line);
                return (float) ((((getParagraphRight(line) - left) - (((int) getLineMax(line)) & -2)) / 2) + left);
            } else if (dir == -1) {
                return 0.0f;
            } else {
                return ((float) this.mWidth) - getLineMax(line);
            }
        }
    }

    public float getLineRight(int line) {
        int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        if (align == Alignment.ALIGN_LEFT) {
            return ((float) getParagraphLeft(line)) + getLineMax(line);
        }
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == -1) {
                return (float) this.mWidth;
            }
            return ((float) getParagraphLeft(line)) + getLineMax(line);
        } else if (align == Alignment.ALIGN_RIGHT) {
            return (float) this.mWidth;
        } else {
            if (align != Alignment.ALIGN_OPPOSITE) {
                int left = getParagraphLeft(line);
                int right = getParagraphRight(line);
                return (float) (right - (((right - left) - (((int) getLineMax(line)) & -2)) / 2));
            } else if (dir == -1) {
                return getLineMax(line);
            } else {
                return (float) this.mWidth;
            }
        }
    }

    public float getLineMax(int line) {
        float margin = (float) getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, false);
        return (signedExtent >= 0.0f ? signedExtent : -signedExtent) + margin;
    }

    public float getLineWidth(int line) {
        float margin = (float) getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, true);
        return (signedExtent >= 0.0f ? signedExtent : -signedExtent) + margin;
    }

    private float getLineExtent(int line, boolean full) {
        int start = getLineStart(line);
        int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        boolean hasTabs = getLineContainsTab(line);
        TabStops tabStops = null;
        if (hasTabs && (this.mText instanceof Spanned)) {
            TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) this.mText, start, end, TabStopSpan.class);
            if (tabs.length > 0) {
                tabStops = new TabStops(20, tabs);
            }
        }
        TabStops tabStops2 = tabStops;
        Directions directions = getLineDirections(line);
        if (directions == null) {
            return 0.0f;
        }
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        TextPaint paint = this.mWorkPaint;
        paint.set(this.mPaint);
        paint.setHyphenEdit(getHyphen(line));
        TextPaint textPaint = paint;
        int i = start;
        TextLine tl2 = tl;
        tl.set(paint, this.mText, start, end, dir, directions, hasTabs, tabStops2);
        if (isJustificationRequired(line)) {
            tl2.justify(getJustifyWidth(line));
        }
        float width = tl2.metrics(null);
        TextLine.recycle(tl2);
        return width;
    }

    private float getLineExtent(int line, TabStops tabStops, boolean full) {
        int start = getLineStart(line);
        int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        boolean hasTabs = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        TextPaint paint = this.mWorkPaint;
        paint.set(this.mPaint);
        paint.setHyphenEdit(getHyphen(line));
        TextPaint textPaint = paint;
        tl.set(paint, this.mText, start, end, dir, directions, hasTabs, tabStops);
        if (isJustificationRequired(line)) {
            tl.justify(getJustifyWidth(line));
        }
        float width = tl.metrics(null);
        TextLine.recycle(tl);
        return width;
    }

    public int getLineForVertical(int vertical) {
        int high = getLineCount();
        int low = -1;
        while (high - low > 1) {
            int guess = (high + low) / 2;
            if (getLineTop(guess) > vertical) {
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

    public int getLineForOffset(int offset) {
        int high = getLineCount();
        int low = -1;
        while (high - low > 1) {
            int guess = (high + low) / 2;
            if (getLineStart(guess) > offset) {
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

    public int getOffsetForHorizontal(int line, float horiz) {
        return getOffsetForHorizontal(line, horiz, true);
    }

    public int getOffsetForHorizontal(int line, float horiz, boolean primary) {
        int max;
        int best;
        int best2;
        int low;
        boolean z;
        Layout layout = this;
        int i = line;
        int lineEndOffset = getLineEnd(line);
        int lineStartOffset = getLineStart(line);
        Directions dirs = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        tl.set(layout.mPaint, layout.mText, lineStartOffset, lineEndOffset, getParagraphDirection(line), dirs, false, null);
        HorizontalMeasurementProvider horizontal = new HorizontalMeasurementProvider(i, primary);
        if (i == getLineCount() - 1) {
            max = lineEndOffset;
        } else {
            max = tl.getOffsetToLeftRightOf(lineEndOffset - lineStartOffset, !layout.isRtlCharAt(lineEndOffset - 1)) + lineStartOffset;
        }
        float bestdist = Math.abs(horizontal.get(lineStartOffset) - horiz);
        int guess = lineStartOffset;
        int i2 = 0;
        while (i2 < dirs.mDirections.length) {
            int here = dirs.mDirections[i2] + lineStartOffset;
            int there = (dirs.mDirections[i2 + 1] & RUN_LENGTH_MASK) + here;
            boolean isRtl = (dirs.mDirections[i2 + 1] & 67108864) != 0;
            int swap = isRtl ? -1 : 1;
            if (there > max) {
                there = max;
            }
            int i3 = 1;
            int high = (there - 1) + 1;
            int low2 = (here + 1) - 1;
            while (true) {
                best2 = guess;
                low = low2;
                if (high - low <= i3) {
                    break;
                }
                int guess2 = (high + low) / 2;
                int adguess = layout.getOffsetAtStartOf(guess2);
                int i4 = adguess;
                int swap2 = swap;
                if (horizontal.get(adguess) * ((float) swap2) >= ((float) swap2) * horiz) {
                    high = guess2;
                    low2 = low;
                } else {
                    low2 = guess2;
                }
                swap = swap2;
                guess = best2;
                layout = this;
                i3 = 1;
                boolean z2 = primary;
            }
            int swap3 = swap;
            if (low < here + 1) {
                low = here + 1;
            }
            if (low < there) {
                int aft = tl.getOffsetToLeftRightOf(low - lineStartOffset, isRtl) + lineStartOffset;
                int i5 = aft - lineStartOffset;
                if (!isRtl) {
                    int i6 = swap3;
                    z = true;
                } else {
                    int i7 = swap3;
                    z = false;
                }
                int low3 = tl.getOffsetToLeftRightOf(i5, z) + lineStartOffset;
                if (low3 >= here && low3 < there) {
                    float dist = Math.abs(horizontal.get(low3) - horiz);
                    if (aft < there) {
                        float other = Math.abs(horizontal.get(aft) - horiz);
                        if (other < dist) {
                            dist = other;
                            low3 = aft;
                        }
                    }
                    if (dist < bestdist) {
                        bestdist = dist;
                        best2 = low3;
                    }
                }
            }
            float dist2 = Math.abs(horizontal.get(here) - horiz);
            if (dist2 < bestdist) {
                guess = here;
                bestdist = dist2;
            } else {
                guess = best2;
            }
            i2 += 2;
            layout = this;
            int i8 = line;
            boolean z3 = primary;
        }
        int best3 = guess;
        if (Math.abs(horizontal.get(max) - horiz) <= bestdist) {
            best = max;
        } else {
            best = best3;
        }
        TextLine.recycle(tl);
        return best;
    }

    public final int getLineEnd(int line) {
        return getLineStart(line + 1);
    }

    public int getLineVisibleEnd(int line) {
        return getLineVisibleEnd(line, getLineStart(line), getLineStart(line + 1));
    }

    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = this.mText;
        if (line == getLineCount() - 1) {
            return end;
        }
        while (end > start) {
            char ch = text.charAt(end - 1);
            if (ch == 10) {
                return end - 1;
            }
            if (!TextLine.isLineEndSpace(ch)) {
                break;
            }
            end--;
        }
        return end;
    }

    public final int getLineBottom(int line) {
        return getLineTop(line + 1);
    }

    public final int getLineBottomWithoutSpacing(int line) {
        return getLineTop(line + 1) - getLineExtra(line);
    }

    public final int getLineBaseline(int line) {
        return getLineTop(line + 1) - getLineDescent(line);
    }

    public final int getLineAscent(int line) {
        return getLineTop(line) - (getLineTop(line + 1) - getLineDescent(line));
    }

    public int getLineExtra(int line) {
        return 0;
    }

    public int getOffsetToLeftOf(int offset) {
        return getOffsetToLeftRightOf(offset, true);
    }

    public int getOffsetToRightOf(int offset) {
        return getOffsetToLeftRightOf(offset, false);
    }

    private int getOffsetToLeftRightOf(int caret, boolean toLeft) {
        int i = caret;
        boolean toLeft2 = toLeft;
        int line = getLineForOffset(caret);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int lineDir = getParagraphDirection(line);
        boolean lineChanged = false;
        boolean advance = false;
        if (toLeft2 == (lineDir == -1)) {
            advance = true;
        }
        if (advance) {
            if (i == lineEnd) {
                if (line >= getLineCount() - 1) {
                    return i;
                }
                lineChanged = true;
                line++;
            }
        } else if (i == lineStart) {
            if (line <= 0) {
                return i;
            }
            lineChanged = true;
            line--;
        }
        if (lineChanged) {
            lineStart = getLineStart(line);
            lineEnd = getLineEnd(line);
            int newDir = getParagraphDirection(line);
            if (newDir != lineDir) {
                toLeft2 = !toLeft2;
                lineDir = newDir;
            }
        }
        Directions directions = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        TextLine tl2 = tl;
        tl.set(this.mPaint, this.mText, lineStart, lineEnd, lineDir, directions, false, null);
        int caret2 = tl2.getOffsetToLeftRightOf(i - lineStart, toLeft2) + lineStart;
        if (caret2 > lineEnd) {
            caret2 = lineEnd;
        }
        if (caret2 < lineStart) {
            caret2 = lineStart;
        }
        TextLine.recycle(tl2);
        return caret2;
    }

    private int getOffsetAtStartOf(int offset) {
        if (offset == 0) {
            return 0;
        }
        CharSequence text = this.mText;
        char c = text.charAt(offset);
        if (c >= 56320 && c <= 57343) {
            char c1 = text.charAt(offset - 1);
            if (c1 >= 55296 && c1 <= 56319) {
                offset--;
            }
        }
        if (this.mSpannedText != 0) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset, offset, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int start = ((Spanned) text).getSpanStart(spans[i]);
                int end = ((Spanned) text).getSpanEnd(spans[i]);
                if (start < offset && end > offset) {
                    offset = start;
                }
            }
        }
        return offset;
    }

    public boolean shouldClampCursor(int line) {
        boolean z = true;
        switch (getParagraphAlignment(line)) {
            case ALIGN_LEFT:
                return true;
            case ALIGN_NORMAL:
                if (getParagraphDirection(line) <= 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void getCursorPath(int point, Path dest, CharSequence editingBuffer) {
        int i = point;
        Path path = dest;
        CharSequence charSequence = editingBuffer;
        dest.reset();
        int line = getLineForOffset(point);
        int top = getLineTop(line);
        int bottom = getLineBottomWithoutSpacing(line);
        boolean clamped = shouldClampCursor(line);
        float h1 = getPrimaryHorizontal(i, clamped) - 0.5f;
        float h2 = isLevelBoundary(point) ? getSecondaryHorizontal(i, clamped) - 0.5f : h1;
        int caps = TextKeyListener.getMetaState(charSequence, 1) | TextKeyListener.getMetaState(charSequence, 2048);
        int fn = TextKeyListener.getMetaState(charSequence, 2);
        int dist = 0;
        if (!(caps == 0 && fn == 0)) {
            dist = (bottom - top) >> 2;
            if (fn != 0) {
                top += dist;
            }
            if (caps != 0) {
                bottom -= dist;
            }
        }
        if (h1 < 0.5f) {
            h1 = 0.5f;
        }
        if (h2 < 0.5f) {
            h2 = 0.5f;
        }
        if (Float.compare(h1, h2) == 0) {
            path.moveTo(h1, (float) top);
            path.lineTo(h1, (float) bottom);
        } else {
            path.moveTo(h1, (float) top);
            path.lineTo(h1, (float) ((top + bottom) >> 1));
            path.moveTo(h2, (float) ((top + bottom) >> 1));
            path.lineTo(h2, (float) bottom);
        }
        if (caps == 2) {
            path.moveTo(h2, (float) bottom);
            path.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            path.lineTo(h2, (float) bottom);
            path.lineTo(((float) dist) + h2, (float) (bottom + dist));
        } else if (caps == 1) {
            path.moveTo(h2, (float) bottom);
            path.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            path.moveTo(h2 - ((float) dist), ((float) (bottom + dist)) - 0.5f);
            path.lineTo(((float) dist) + h2, ((float) (bottom + dist)) - 0.5f);
            path.moveTo(((float) dist) + h2, (float) (bottom + dist));
            path.lineTo(h2, (float) bottom);
        }
        if (fn == 2) {
            path.moveTo(h1, (float) top);
            path.lineTo(h1 - ((float) dist), (float) (top - dist));
            path.lineTo(h1, (float) top);
            path.lineTo(((float) dist) + h1, (float) (top - dist));
        } else if (fn == 1) {
            path.moveTo(h1, (float) top);
            path.lineTo(h1 - ((float) dist), (float) (top - dist));
            path.moveTo(h1 - ((float) dist), ((float) (top - dist)) + 0.5f);
            path.lineTo(((float) dist) + h1, ((float) (top - dist)) + 0.5f);
            path.moveTo(((float) dist) + h1, (float) (top - dist));
            path.lineTo(h1, (float) top);
        }
    }

    private void addSelection(int line, int start, int end, int top, int bottom, SelectionRectangleConsumer consumer) {
        Layout layout = this;
        int i = line;
        int i2 = start;
        int i3 = end;
        int linestart = getLineStart(line);
        int lineend = getLineEnd(line);
        Directions dirs = getLineDirections(line);
        if (lineend > linestart && layout.mText.charAt(lineend - 1) == 10) {
            lineend--;
        }
        boolean z = false;
        int i4 = 0;
        while (i4 < dirs.mDirections.length) {
            int here = dirs.mDirections[i4] + linestart;
            int there = (dirs.mDirections[i4 + 1] & RUN_LENGTH_MASK) + here;
            if (there > lineend) {
                there = lineend;
            }
            if (i2 <= there && i3 >= here) {
                int st = Math.max(i2, here);
                int en = Math.min(i3, there);
                if (st != en) {
                    float h1 = layout.getHorizontal(st, z, i, z);
                    float h2 = layout.getHorizontal(en, true, i, z);
                    consumer.accept(Math.min(h1, h2), (float) top, Math.max(h1, h2), (float) bottom, (dirs.mDirections[i4 + 1] & 67108864) != 0 ? 0 : 1);
                    i4 += 2;
                    layout = this;
                    i = line;
                    z = false;
                }
            }
            int i5 = top;
            int i6 = bottom;
            i4 += 2;
            layout = this;
            i = line;
            z = false;
        }
        int i7 = top;
        int i8 = bottom;
    }

    public void getSelectionPath(int start, int end, Path dest) {
        dest.reset();
        getSelection(start, end, new SelectionRectangleConsumer(dest) {
            private final /* synthetic */ Path f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(float f, float f2, float f3, float f4, int i) {
                this.f$0.addRect(f, f2, f3, f4, Path.Direction.CW);
            }
        });
    }

    public final void getSelection(int start, int end, SelectionRectangleConsumer consumer) {
        int i = start;
        int end2 = end;
        if (i != end2) {
            if (end2 < i) {
                int temp = end2;
                end2 = i;
                i = temp;
            }
            int start2 = i;
            int end3 = end2;
            int startline = getLineForOffset(start2);
            int endline = getLineForOffset(end3);
            int top = getLineTop(startline);
            int bottom = getLineBottomWithoutSpacing(endline);
            if (startline == endline) {
                addSelection(startline, start2, end3, top, bottom, consumer);
            } else {
                float width = (float) this.mWidth;
                addSelection(startline, start2, getLineEnd(startline), top, getLineBottom(startline), consumer);
                if (getParagraphDirection(startline) == -1) {
                    consumer.accept(getLineLeft(startline), (float) top, 0.0f, (float) getLineBottom(startline), 0);
                } else {
                    consumer.accept(getLineRight(startline), (float) top, width, (float) getLineBottom(startline), 1);
                }
                for (int i2 = startline + 1; i2 < endline; i2++) {
                    int top2 = getLineTop(i2);
                    int bottom2 = getLineBottom(i2);
                    if (getParagraphDirection(i2) == -1) {
                        consumer.accept(0.0f, (float) top2, width, (float) bottom2, 0);
                    } else {
                        consumer.accept(0.0f, (float) top2, width, (float) bottom2, 1);
                    }
                }
                int top3 = getLineTop(endline);
                int bottom3 = getLineBottomWithoutSpacing(endline);
                addSelection(endline, getLineStart(endline), end3, top3, bottom3, consumer);
                if (getParagraphDirection(endline) == -1) {
                    consumer.accept(width, (float) top3, getLineRight(endline), (float) bottom3, 0);
                } else {
                    consumer.accept(0.0f, (float) top3, getLineLeft(endline), (float) bottom3, 1);
                }
            }
        }
    }

    public final Alignment getParagraphAlignment(int line) {
        Alignment align = this.mAlignment;
        if (!this.mSpannedText) {
            return align;
        }
        AlignmentSpan[] spans = (AlignmentSpan[]) getParagraphSpans((Spanned) this.mText, getLineStart(line), getLineEnd(line), AlignmentSpan.class);
        int spanLength = spans.length;
        if (spanLength > 0) {
            return spans[spanLength - 1].getAlignment();
        }
        return align;
    }

    public final int getParagraphLeft(int line) {
        if (getParagraphDirection(line) == -1 || !this.mSpannedText) {
            return 0;
        }
        return getParagraphLeadingMargin(line);
    }

    public final int getParagraphRight(int line) {
        int right = this.mWidth;
        if (getParagraphDirection(line) == 1 || !this.mSpannedText) {
            return right;
        }
        return right - getParagraphLeadingMargin(line);
    }

    private int getParagraphLeadingMargin(int line) {
        if (!this.mSpannedText) {
            return 0;
        }
        Spanned spanned = (Spanned) this.mText;
        int lineStart = getLineStart(line);
        LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans(spanned, lineStart, spanned.nextSpanTransition(lineStart, getLineEnd(line), LeadingMarginSpan.class), LeadingMarginSpan.class);
        if (spans.length == 0) {
            return 0;
        }
        int margin = 0;
        boolean useFirstLineMargin = lineStart == 0 || spanned.charAt(lineStart + -1) == 10;
        for (int i = 0; i < spans.length; i++) {
            if (spans[i] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                useFirstLineMargin |= line < getLineForOffset(spanned.getSpanStart(spans[i])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans[i]).getLeadingMarginLineCount();
            }
        }
        for (LeadingMarginSpan span : spans) {
            margin += span.getLeadingMargin(useFirstLineMargin);
        }
        return margin;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d4  */
    private static float measurePara(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        MeasuredParagraph mt;
        boolean hasTabs;
        int margin;
        TabStops tabStops;
        boolean hasTabs2;
        CharSequence charSequence = text;
        int i = start;
        int i2 = end;
        TextLine tl = TextLine.obtain();
        try {
            mt = MeasuredParagraph.buildForBidi(charSequence, i, i2, textDir, null);
            try {
                char[] chars = mt.getChars();
                int len = chars.length;
                Directions directions = mt.getDirections(0, len);
                int dir = mt.getParagraphDir();
                boolean hasTabs3 = false;
                TabStops tabStops2 = null;
                if (charSequence instanceof Spanned) {
                    LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans((Spanned) charSequence, i, i2, LeadingMarginSpan.class);
                    int length = spans.length;
                    int margin2 = 0;
                    int margin3 = 0;
                    while (margin3 < length) {
                        margin2 += spans[margin3].getLeadingMargin(true);
                        margin3++;
                        length = length;
                        hasTabs3 = hasTabs3;
                    }
                    hasTabs = hasTabs3;
                    margin = margin2;
                } else {
                    hasTabs = false;
                    margin = 0;
                }
                int i3 = 0;
                while (true) {
                    int i4 = i3;
                    if (i4 >= len) {
                        tabStops = null;
                        hasTabs2 = hasTabs;
                        break;
                    } else if (chars[i4] != 9) {
                        i3 = i4 + 1;
                    } else if (charSequence instanceof Spanned) {
                        Spanned spanned = (Spanned) charSequence;
                        char[] cArr = chars;
                        TabStopSpan[] spans2 = (TabStopSpan[]) getParagraphSpans(spanned, i, spanned.nextSpanTransition(i, i2, TabStopSpan.class), TabStopSpan.class);
                        hasTabs2 = true;
                        if (spans2.length > 0) {
                            tabStops2 = new TabStops(20, spans2);
                        }
                        tabStops = tabStops2;
                    } else {
                        hasTabs2 = true;
                        tabStops = null;
                    }
                }
                int margin4 = margin;
                int i5 = len;
                tl.set(paint, charSequence, i, i2, dir, directions, hasTabs2, tabStops);
                float abs = ((float) margin4) + Math.abs(tl.metrics(null));
                TextLine.recycle(tl);
                if (mt != null) {
                    mt.recycle();
                }
                return abs;
            } catch (Throwable th) {
                th = th;
                TextLine.recycle(tl);
                if (mt != null) {
                    mt.recycle();
                }
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            mt = null;
            TextLine.recycle(tl);
            if (mt != null) {
            }
            throw th;
        }
    }

    static float nextTab(CharSequence text, int start, int end, float h, Object[] tabs) {
        float nh = Float.MAX_VALUE;
        boolean alltabs = false;
        if (text instanceof Spanned) {
            if (tabs == null) {
                tabs = getParagraphSpans((Spanned) text, start, end, TabStopSpan.class);
                alltabs = true;
            }
            for (int i = 0; i < tabs.length; i++) {
                if (alltabs || (tabs[i] instanceof TabStopSpan)) {
                    int where = ((TabStopSpan) tabs[i]).getTabStop();
                    if (((float) where) < nh && ((float) where) > h) {
                        nh = (float) where;
                    }
                }
            }
            if (nh != Float.MAX_VALUE) {
                return nh;
            }
        }
        return (float) (((int) ((h + 20.0f) / 20.0f)) * 20);
    }

    /* access modifiers changed from: protected */
    public final boolean isSpanned() {
        return this.mSpannedText;
    }

    static <T> T[] getParagraphSpans(Spanned text, int start, int end, Class<T> type) {
        if (start == end && start > 0) {
            return ArrayUtils.emptyArray(type);
        }
        if (text instanceof SpannableStringBuilder) {
            return ((SpannableStringBuilder) text).getSpans(start, end, type, false);
        }
        return text.getSpans(start, end, type);
    }

    /* access modifiers changed from: private */
    public void ellipsize(int start, int end, int line, char[] dest, int destoff, TextUtils.TruncateAt method) {
        char c;
        int i = start;
        int i2 = line;
        int ellipsisCount = getEllipsisCount(i2);
        if (ellipsisCount != 0) {
            int ellipsisStart = getEllipsisStart(i2);
            int lineStart = getLineStart(i2);
            String ellipsisString = TextUtils.getEllipsisString(method);
            int ellipsisStringLen = ellipsisString.length();
            boolean useEllipsisString = ellipsisCount >= ellipsisStringLen;
            for (int i3 = 0; i3 < ellipsisCount; i3++) {
                if (!useEllipsisString || i3 >= ellipsisStringLen) {
                    c = 65279;
                } else {
                    c = ellipsisString.charAt(i3);
                }
                int a = i3 + ellipsisStart + lineStart;
                if (i > a) {
                    int i4 = end;
                } else if (a < end) {
                    dest[(destoff + a) - i] = c;
                }
            }
            int i5 = end;
        }
    }
}

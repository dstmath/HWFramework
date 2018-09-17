package android.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.text.TextUtils.TruncateAt;
import android.text.method.MetaKeyKeyListener;
import android.text.style.AlignmentSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2;
import android.text.style.LineBackgroundSpan;
import android.text.style.ParagraphStyle;
import android.text.style.ReplacementSpan;
import android.text.style.TabStopSpan;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.GrowingArrayUtils;
import java.util.Arrays;

public abstract class Layout {
    private static final /* synthetic */ int[] -android-text-Layout$AlignmentSwitchesValues = null;
    public static final int BREAK_STRATEGY_BALANCED = 2;
    public static final int BREAK_STRATEGY_HIGH_QUALITY = 1;
    public static final int BREAK_STRATEGY_SIMPLE = 0;
    public static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new int[]{0, RUN_LENGTH_MASK});
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

    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        ALIGN_LEFT,
        ALIGN_RIGHT
    }

    public static class Directions {
        public int[] mDirections;

        public Directions(int[] dirs) {
            this.mDirections = dirs;
        }
    }

    static class Ellipsizer implements CharSequence, GetChars {
        Layout mLayout;
        TruncateAt mMethod;
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

        void reset(int increment, Object[] spans) {
            this.mIncrement = increment;
            int ns = 0;
            if (spans != null) {
                int[] stops = this.mStops;
                int length = spans.length;
                int i = 0;
                int ns2 = 0;
                while (i < length) {
                    Object o = spans[i];
                    if (o instanceof TabStopSpan) {
                        if (stops == null) {
                            stops = new int[10];
                        } else if (ns2 == stops.length) {
                            int[] nstops = new int[(ns2 * 2)];
                            for (int i2 = 0; i2 < ns2; i2++) {
                                nstops[i2] = stops[i2];
                            }
                            stops = nstops;
                        }
                        ns = ns2 + 1;
                        stops[ns2] = ((TabStopSpan) o).getTabStop();
                    } else {
                        ns = ns2;
                    }
                    i++;
                    ns2 = ns;
                }
                if (ns2 > 1) {
                    Arrays.sort(stops, 0, ns2);
                }
                if (stops != this.mStops) {
                    this.mStops = stops;
                    ns = ns2;
                } else {
                    ns = ns2;
                }
            }
            this.mNumStops = ns;
        }

        float nextTab(float h) {
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

    private static /* synthetic */ int[] -getandroid-text-Layout$AlignmentSwitchesValues() {
        if (-android-text-Layout$AlignmentSwitchesValues != null) {
            return -android-text-Layout$AlignmentSwitchesValues;
        }
        int[] iArr = new int[Alignment.values().length];
        try {
            iArr[Alignment.ALIGN_CENTER.ordinal()] = 3;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Alignment.ALIGN_LEFT.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Alignment.ALIGN_NORMAL.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Alignment.ALIGN_OPPOSITE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Alignment.ALIGN_RIGHT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        -android-text-Layout$AlignmentSwitchesValues = iArr;
        return iArr;
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
        float need = 0.0f;
        int i = start;
        while (i <= end) {
            int next = TextUtils.indexOf(source, 10, i, end);
            if (next < 0) {
                next = end;
            }
            float w = measurePara(paint, source, i, next, textDir);
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
        this.mAlignment = Alignment.ALIGN_NORMAL;
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }
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
    }

    protected void setJustificationMode(int justificationMode) {
        this.mJustificationMode = justificationMode;
    }

    void replaceWith(CharSequence text, TextPaint paint, int width, Alignment align, float spacingmult, float spacingadd) {
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }
        this.mText = text;
        this.mPaint = paint;
        this.mWidth = width;
        this.mAlignment = align;
        this.mSpacingMult = spacingmult;
        this.mSpacingAdd = spacingadd;
        this.mSpannedText = text instanceof Spanned;
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

    private float getJustifyWidth(int lineNum) {
        int indentWidth;
        Alignment paraAlign = this.mAlignment;
        int left = 0;
        int right = this.mWidth;
        int dir = getParagraphDirection(lineNum);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        if (this.mSpannedText) {
            int n;
            Spanned sp = this.mText;
            int start = getLineStart(lineNum);
            boolean isFirstParaLine = start == 0 || this.mText.charAt(start - 1) == 10;
            if (isFirstParaLine) {
                spans = (ParagraphStyle[]) getParagraphSpans(sp, start, sp.nextSpanTransition(start, this.mText.length(), ParagraphStyle.class), ParagraphStyle.class);
                for (n = spans.length - 1; n >= 0; n--) {
                    if (spans[n] instanceof AlignmentSpan) {
                        paraAlign = ((AlignmentSpan) spans[n]).getAlignment();
                        break;
                    }
                }
            }
            int length = spans.length;
            boolean useFirstLineMargin = isFirstParaLine;
            for (n = 0; n < length; n++) {
                if (spans[n] instanceof LeadingMarginSpan2) {
                    if (lineNum < getLineForOffset(sp.getSpanStart(spans[n])) + ((LeadingMarginSpan2) spans[n]).getLeadingMarginLineCount()) {
                        useFirstLineMargin = true;
                        break;
                    }
                }
            }
            for (n = 0; n < length; n++) {
                if (spans[n] instanceof LeadingMarginSpan) {
                    LeadingMarginSpan margin = spans[n];
                    if (dir == -1) {
                        right -= margin.getLeadingMargin(useFirstLineMargin);
                    } else {
                        left += margin.getLeadingMargin(useFirstLineMargin);
                    }
                }
            }
        }
        if (getLineContainsTab(lineNum)) {
            TabStops tabStops = new TabStops(20, spans);
        }
        Alignment align = paraAlign == Alignment.ALIGN_LEFT ? dir == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE : paraAlign == Alignment.ALIGN_RIGHT ? dir == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL : paraAlign;
        if (align == Alignment.ALIGN_NORMAL) {
            if (dir == 1) {
                indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
            } else {
                indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
            }
        } else if (align != Alignment.ALIGN_OPPOSITE) {
            indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_CENTER);
        } else if (dir == 1) {
            indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
        } else {
            indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
        }
        return (float) ((right - left) - indentWidth);
    }

    public void drawText(Canvas canvas, int firstLine, int lastLine) {
        int previousLineBottom = getLineTop(firstLine);
        int previousLineEnd = getLineStart(firstLine);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        int spanEnd = 0;
        Paint paint = this.mPaint;
        CharSequence buf = this.mText;
        Alignment paraAlign = this.mAlignment;
        TabStops tabStops = null;
        boolean tabStopsIsInitialized = false;
        TextLine tl = TextLine.obtain();
        int lineNum = firstLine;
        while (true) {
            TabStops tabStops2 = tabStops;
            if (lineNum <= lastLine) {
                int indentWidth;
                int x;
                int start = previousLineEnd;
                previousLineEnd = getLineStart(lineNum + 1);
                boolean justify = isJustificationRequired(lineNum);
                int end = getLineVisibleEnd(lineNum, start, previousLineEnd);
                int ltop = previousLineBottom;
                int lbottom = getLineTop(lineNum + 1);
                previousLineBottom = lbottom;
                int lbaseline = lbottom - getLineDescent(lineNum);
                int dir = getParagraphDirection(lineNum);
                int left = 0;
                int right = this.mWidth;
                if (this.mSpannedText) {
                    boolean isFirstParaLine;
                    int n;
                    Spanned sp = (Spanned) buf;
                    int textLength = buf.length();
                    if (start == 0 || buf.charAt(start - 1) == 10) {
                        isFirstParaLine = true;
                    } else {
                        isFirstParaLine = false;
                    }
                    if (start >= spanEnd && (lineNum == firstLine || isFirstParaLine)) {
                        spanEnd = sp.nextSpanTransition(start, textLength, ParagraphStyle.class);
                        spans = (ParagraphStyle[]) getParagraphSpans(sp, start, spanEnd, ParagraphStyle.class);
                        paraAlign = this.mAlignment;
                        for (n = spans.length - 1; n >= 0; n--) {
                            if (spans[n] instanceof AlignmentSpan) {
                                paraAlign = ((AlignmentSpan) spans[n]).getAlignment();
                                break;
                            }
                        }
                        tabStopsIsInitialized = false;
                    }
                    int length = spans.length;
                    boolean useFirstLineMargin = isFirstParaLine;
                    for (n = 0; n < length; n++) {
                        if (spans[n] instanceof LeadingMarginSpan2) {
                            if (lineNum < getLineForOffset(sp.getSpanStart(spans[n])) + ((LeadingMarginSpan2) spans[n]).getLeadingMarginLineCount()) {
                                useFirstLineMargin = true;
                                break;
                            }
                        }
                    }
                    for (n = 0; n < length; n++) {
                        if (spans[n] instanceof LeadingMarginSpan) {
                            LeadingMarginSpan margin = spans[n];
                            if (dir == -1) {
                                margin.drawLeadingMargin(canvas, paint, right, dir, ltop, lbaseline, lbottom, buf, start, end, isFirstParaLine, this);
                                right -= margin.getLeadingMargin(useFirstLineMargin);
                            } else {
                                margin.drawLeadingMargin(canvas, paint, left, dir, ltop, lbaseline, lbottom, buf, start, end, isFirstParaLine, this);
                                left += margin.getLeadingMargin(useFirstLineMargin);
                            }
                        }
                    }
                }
                boolean hasTab = getLineContainsTab(lineNum);
                if (!hasTab) {
                    tabStops = tabStops2;
                } else if ((tabStopsIsInitialized ^ 1) != 0) {
                    if (tabStops2 == null) {
                        TabStops tabStops3 = new TabStops(20, spans);
                    } else {
                        tabStops2.reset(20, spans);
                        tabStops = tabStops2;
                    }
                    tabStopsIsInitialized = true;
                } else {
                    tabStops = tabStops2;
                }
                Alignment align = paraAlign;
                if (align == Alignment.ALIGN_LEFT) {
                    align = dir == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
                } else if (align == Alignment.ALIGN_RIGHT) {
                    align = dir == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
                }
                if (align != Alignment.ALIGN_NORMAL) {
                    int max = (int) getLineExtent(lineNum, tabStops, false);
                    if (align != Alignment.ALIGN_OPPOSITE) {
                        indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_CENTER);
                        x = (((right + left) - (max & -2)) >> 1) + indentWidth;
                    } else if (dir == 1) {
                        indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                        x = (right - max) - indentWidth;
                    } else {
                        indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                        x = (left - max) + indentWidth;
                    }
                } else if (dir == 1) {
                    indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                    x = left + indentWidth;
                } else {
                    indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                    x = right - indentWidth;
                }
                paint.setHyphenEdit(getHyphen(lineNum));
                Directions directions = getLineDirections(lineNum);
                if (directions != DIRS_ALL_LEFT_TO_RIGHT || (this.mSpannedText ^ 1) == 0 || (hasTab ^ 1) == 0 || (justify ^ 1) == 0) {
                    tl.set(paint, buf, start, end, dir, directions, hasTab, tabStops);
                    if (justify) {
                        tl.justify((float) ((right - left) - indentWidth));
                    }
                    tl.draw(canvas, (float) x, ltop, lbaseline, lbottom);
                } else {
                    canvas.drawText(buf, start, end, (float) x, (float) lbaseline, paint);
                }
                paint.setHyphenEdit(0);
                lineNum++;
            } else {
                TextLine.recycle(tl);
                return;
            }
        }
    }

    public void drawBackground(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int firstLine, int lastLine) {
        if (this.mSpannedText) {
            if (this.mLineBackgroundSpans == null) {
                this.mLineBackgroundSpans = new SpanSet(LineBackgroundSpan.class);
            }
            Spanned buffer = this.mText;
            int textLength = buffer.length();
            this.mLineBackgroundSpans.init(buffer, 0, textLength);
            if (this.mLineBackgroundSpans.numberOfSpans > 0) {
                int previousLineBottom = getLineTop(firstLine);
                int previousLineEnd = getLineStart(firstLine);
                ParagraphStyle[] spans = NO_PARA_SPANS;
                int spansLength = 0;
                TextPaint paint = this.mPaint;
                int spanEnd = 0;
                int width = this.mWidth;
                for (int i = firstLine; i <= lastLine; i++) {
                    int start = previousLineEnd;
                    int end = getLineStart(i + 1);
                    previousLineEnd = end;
                    int ltop = previousLineBottom;
                    int lbottom = getLineTop(i + 1);
                    previousLineBottom = lbottom;
                    int lbaseline = lbottom - getLineDescent(i);
                    if (start >= spanEnd) {
                        spanEnd = this.mLineBackgroundSpans.getNextTransition(start, textLength);
                        spansLength = 0;
                        if (start != end || start == 0) {
                            int j = 0;
                            while (j < this.mLineBackgroundSpans.numberOfSpans) {
                                if (this.mLineBackgroundSpans.spanStarts[j] < end && this.mLineBackgroundSpans.spanEnds[j] > start) {
                                    spans = (ParagraphStyle[]) GrowingArrayUtils.append((Object[]) spans, spansLength, ((LineBackgroundSpan[]) this.mLineBackgroundSpans.spans)[j]);
                                    spansLength++;
                                }
                                j++;
                            }
                        }
                    }
                    for (int n = 0; n < spansLength; n++) {
                        spans[n].drawBackground(canvas, paint, 0, width, ltop, lbaseline, lbottom, buffer, start, end, i);
                    }
                }
            }
            this.mLineBackgroundSpans.recycle();
        }
        if (highlight != null) {
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) cursorOffsetVertical);
            }
            canvas.drawPath(highlight, highlightPaint);
            if (cursorOffsetVertical != 0) {
                canvas.translate(0.0f, (float) (-cursorOffsetVertical));
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x001e, code:
            r3 = java.lang.Math.max(r2, 0);
            r0 = java.lang.Math.min(getLineTop(getLineCount()), r1);
     */
    /* JADX WARNING: Missing block: B:12:0x002e, code:
            if (r3 < r0) goto L_0x0038;
     */
    /* JADX WARNING: Missing block: B:14:0x0034, code:
            return android.text.TextUtils.packRangeInLong(0, -1);
     */
    /* JADX WARNING: Missing block: B:19:0x0044, code:
            return android.text.TextUtils.packRangeInLong(getLineForVertical(r3), getLineForVertical(r0));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getLineRangeForDraw(Canvas canvas) {
        synchronized (sTempRect) {
            if (canvas.getClipBounds(sTempRect)) {
                int dtop = sTempRect.top;
                int dbottom = sTempRect.bottom;
            } else {
                long packRangeInLong = TextUtils.packRangeInLong(0, -1);
                return packRangeInLong;
            }
        }
    }

    private int getLineStartPos(int line, int left, int right) {
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
                Spanned spanned = this.mText;
                int start = getLineStart(line);
                TabStopSpan[] tabSpans = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, spanned.length(), TabStopSpan.class), TabStopSpan.class);
                if (tabSpans.length > 0) {
                    tabStops = new TabStops(20, tabSpans);
                }
            }
            int max = (int) getLineExtent(line, tabStops, false);
            if (align != Alignment.ALIGN_OPPOSITE) {
                return ((left + right) - (max & -2)) >> (getIndentAdjust(line, Alignment.ALIGN_CENTER) + 1);
            } else if (dir == 1) {
                return (right - max) + getIndentAdjust(line, Alignment.ALIGN_RIGHT);
            } else {
                return (left - max) + getIndentAdjust(line, Alignment.ALIGN_LEFT);
            }
        } else if (dir == 1) {
            return left + getIndentAdjust(line, Alignment.ALIGN_LEFT);
        } else {
            return right + getIndentAdjust(line, Alignment.ALIGN_RIGHT);
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
        if (wid < this.mWidth) {
            throw new RuntimeException("attempted to reduce Layout width");
        }
        this.mWidth = wid;
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
        boolean z = true;
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT || dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return false;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        if (offset == lineStart || offset == lineEnd) {
            if (((runs[(offset == lineStart ? 0 : runs.length - 2) + 1] >>> 26) & 63) == (getParagraphDirection(line) == 1 ? 0 : 1)) {
                z = false;
            }
            return z;
        }
        offset -= lineStart;
        for (int i = 0; i < runs.length; i += 2) {
            if (offset == runs[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean isRtlCharAt(int offset) {
        boolean z = true;
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
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
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & RUN_LENGTH_MASK);
            if (offset < start || offset >= limit) {
                i += 2;
            } else {
                if ((((runs[i + 1] >>> 26) & 63) & 1) == 0) {
                    z = false;
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
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & RUN_LENGTH_MASK);
            if (offset >= start && offset < limit) {
                return TextUtils.packRangeInLong(start, limit);
            }
        }
        return TextUtils.packRangeInLong(0, getLineEnd(line));
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x003c  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0057  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x007b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean primaryIsTrailingPrevious(int offset) {
        int levelBefore;
        boolean z = true;
        int line = getLineForOffset(offset);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        int levelAt = -1;
        int i = 0;
        while (i < runs.length) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + 1] & RUN_LENGTH_MASK);
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (offset < start || offset >= limit) {
                i += 2;
            } else if (offset > start) {
                return false;
            } else {
                levelAt = (runs[i + 1] >>> 26) & 63;
                if (levelAt == -1) {
                    levelAt = getParagraphDirection(line) == 1 ? 0 : 1;
                }
                levelBefore = -1;
                if (offset == lineStart) {
                    offset--;
                    for (i = 0; i < runs.length; i += 2) {
                        start = lineStart + runs[i];
                        limit = start + (runs[i + 1] & RUN_LENGTH_MASK);
                        if (limit > lineEnd) {
                            limit = lineEnd;
                        }
                        if (offset >= start && offset < limit) {
                            levelBefore = (runs[i + 1] >>> 26) & 63;
                            break;
                        }
                    }
                } else if (getParagraphDirection(line) == 1) {
                    levelBefore = 0;
                } else {
                    levelBefore = 1;
                }
                if (levelBefore >= levelAt) {
                    z = false;
                }
                return z;
            }
        }
        if (levelAt == -1) {
        }
        levelBefore = -1;
        if (offset == lineStart) {
        }
        if (levelBefore >= levelAt) {
        }
        return z;
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
        return getHorizontal(offset, primaryIsTrailingPrevious(offset) ^ 1, clamped);
    }

    private float getHorizontal(int offset, boolean primary) {
        return primary ? getPrimaryHorizontal(offset) : getSecondaryHorizontal(offset);
    }

    private float getHorizontal(int offset, boolean trailing, boolean clamped) {
        return getHorizontal(offset, trailing, getLineForOffset(offset), clamped);
    }

    private float getHorizontal(int offset, boolean trailing, int line, boolean clamped) {
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
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops);
        float wid = tl.measure(offset - start, trailing, null);
        TextLine.recycle(tl);
        if (clamped && wid > ((float) this.mWidth)) {
            wid = (float) this.mWidth;
        }
        return ((float) getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line))) + wid;
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
        if (signedExtent < 0.0f) {
            signedExtent = -signedExtent;
        }
        return margin + signedExtent;
    }

    public float getLineWidth(int line) {
        float margin = (float) getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, true);
        if (signedExtent < 0.0f) {
            signedExtent = -signedExtent;
        }
        return margin + signedExtent;
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
        Directions directions = getLineDirections(line);
        if (directions == null) {
            return 0.0f;
        }
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        this.mPaint.setHyphenEdit(getHyphen(line));
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabs, tabStops);
        if (isJustificationRequired(line)) {
            tl.justify(getJustifyWidth(line));
        }
        float width = tl.metrics(null);
        this.mPaint.setHyphenEdit(0);
        TextLine.recycle(tl);
        return width;
    }

    private float getLineExtent(int line, TabStops tabStops, boolean full) {
        int start = getLineStart(line);
        int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        boolean hasTabs = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        this.mPaint.setHyphenEdit(getHyphen(line));
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabs, tabStops);
        if (isJustificationRequired(line)) {
            tl.justify(getJustifyWidth(line));
        }
        float width = tl.metrics(null);
        this.mPaint.setHyphenEdit(0);
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
        float dist;
        int lineEndOffset = getLineEnd(line);
        int lineStartOffset = getLineStart(line);
        Directions dirs = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, lineStartOffset, lineEndOffset, getParagraphDirection(line), dirs, false, null);
        if (line == getLineCount() - 1) {
            max = lineEndOffset;
        } else {
            max = tl.getOffsetToLeftRightOf(lineEndOffset - lineStartOffset, isRtlCharAt(lineEndOffset - 1) ^ 1) + lineStartOffset;
        }
        int best = lineStartOffset;
        float bestdist = Math.abs(getHorizontal(lineStartOffset, primary) - horiz);
        for (int i = 0; i < dirs.mDirections.length; i += 2) {
            int here = lineStartOffset + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + 1] & RUN_LENGTH_MASK);
            boolean isRtl = (dirs.mDirections[i + 1] & 67108864) != 0;
            int swap = isRtl ? -1 : 1;
            if (there > max) {
                there = max;
            }
            int high = (there - 1) + 1;
            int low = (here + 1) - 1;
            while (high - low > 1) {
                int guess = (high + low) / 2;
                if (getHorizontal(getOffsetAtStartOf(guess), primary) * ((float) swap) >= ((float) swap) * horiz) {
                    high = guess;
                } else {
                    low = guess;
                }
            }
            if (low < here + 1) {
                low = here + 1;
            }
            if (low < there) {
                int aft = tl.getOffsetToLeftRightOf(low - lineStartOffset, isRtl) + lineStartOffset;
                low = tl.getOffsetToLeftRightOf(aft - lineStartOffset, isRtl ^ 1) + lineStartOffset;
                if (low >= here && low < there) {
                    dist = Math.abs(getHorizontal(low, primary) - horiz);
                    if (aft < there) {
                        float other = Math.abs(getHorizontal(aft, primary) - horiz);
                        if (other < dist) {
                            dist = other;
                            low = aft;
                        }
                    }
                    if (dist < bestdist) {
                        bestdist = dist;
                        best = low;
                    }
                }
            }
            dist = Math.abs(getHorizontal(here, primary) - horiz);
            if (dist < bestdist) {
                bestdist = dist;
                best = here;
            }
        }
        dist = Math.abs(getHorizontal(max, primary) - horiz);
        if (dist <= bestdist) {
            bestdist = dist;
            best = max;
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

    public final int getLineBaseline(int line) {
        return getLineTop(line + 1) - getLineDescent(line);
    }

    public final int getLineAscent(int line) {
        return getLineTop(line) - (getLineTop(line + 1) - getLineDescent(line));
    }

    public int getOffsetToLeftOf(int offset) {
        return getOffsetToLeftRightOf(offset, true);
    }

    public int getOffsetToRightOf(int offset) {
        return getOffsetToLeftRightOf(offset, false);
    }

    private int getOffsetToLeftRightOf(int caret, boolean toLeft) {
        boolean z;
        int line = getLineForOffset(caret);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int lineDir = getParagraphDirection(line);
        boolean lineChanged = false;
        if (lineDir == -1) {
            z = true;
        } else {
            z = false;
        }
        if (toLeft == z) {
            if (caret == lineEnd) {
                if (line >= getLineCount() - 1) {
                    return caret;
                }
                lineChanged = true;
                line++;
            }
        } else if (caret == lineStart) {
            if (line <= 0) {
                return caret;
            }
            lineChanged = true;
            line--;
        }
        if (lineChanged) {
            lineStart = getLineStart(line);
            lineEnd = getLineEnd(line);
            int newDir = getParagraphDirection(line);
            if (newDir != lineDir) {
                toLeft ^= 1;
                lineDir = newDir;
            }
        }
        Directions directions = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, lineStart, lineEnd, lineDir, directions, false, null);
        caret = lineStart + tl.getOffsetToLeftRightOf(caret - lineStart, toLeft);
        if (caret > lineEnd) {
            caret = lineEnd;
        }
        if (caret < lineStart) {
            caret = lineStart;
        }
        tl = TextLine.recycle(tl);
        return caret;
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
        if (this.mSpannedText) {
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
        switch (-getandroid-text-Layout$AlignmentSwitchesValues()[getParagraphAlignment(line).ordinal()]) {
            case 1:
                return true;
            case 2:
                if (getParagraphDirection(line) <= 0) {
                    z = false;
                }
                return z;
            default:
                return false;
        }
    }

    public void getCursorPath(int point, Path dest, CharSequence editingBuffer) {
        dest.reset();
        int line = getLineForOffset(point);
        int top = getLineTop(line);
        int bottom = getLineTop(line + 1);
        boolean clamped = shouldClampCursor(line);
        float h1 = getPrimaryHorizontal(point, clamped) - 0.5f;
        float h2 = isLevelBoundary(point) ? getSecondaryHorizontal(point, clamped) - 0.5f : h1;
        int caps = MetaKeyKeyListener.getMetaState(editingBuffer, 1) | MetaKeyKeyListener.getMetaState(editingBuffer, 2048);
        int fn = MetaKeyKeyListener.getMetaState(editingBuffer, 2);
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
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1, (float) bottom);
        } else {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1, (float) ((top + bottom) >> 1));
            dest.moveTo(h2, (float) ((top + bottom) >> 1));
            dest.lineTo(h2, (float) bottom);
        }
        if (caps == 2) {
            dest.moveTo(h2, (float) bottom);
            dest.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            dest.lineTo(h2, (float) bottom);
            dest.lineTo(((float) dist) + h2, (float) (bottom + dist));
        } else if (caps == 1) {
            dest.moveTo(h2, (float) bottom);
            dest.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            dest.moveTo(h2 - ((float) dist), ((float) (bottom + dist)) - 0.5f);
            dest.lineTo(((float) dist) + h2, ((float) (bottom + dist)) - 0.5f);
            dest.moveTo(((float) dist) + h2, (float) (bottom + dist));
            dest.lineTo(h2, (float) bottom);
        }
        if (fn == 2) {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1 - ((float) dist), (float) (top - dist));
            dest.lineTo(h1, (float) top);
            dest.lineTo(((float) dist) + h1, (float) (top - dist));
        } else if (fn == 1) {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1 - ((float) dist), (float) (top - dist));
            dest.moveTo(h1 - ((float) dist), ((float) (top - dist)) + 0.5f);
            dest.lineTo(((float) dist) + h1, ((float) (top - dist)) + 0.5f);
            dest.moveTo(((float) dist) + h1, (float) (top - dist));
            dest.lineTo(h1, (float) top);
        }
    }

    private void addSelection(int line, int start, int end, int top, int bottom, Path dest) {
        int linestart = getLineStart(line);
        int lineend = getLineEnd(line);
        Directions dirs = getLineDirections(line);
        if (lineend > linestart && this.mText.charAt(lineend - 1) == 10) {
            lineend--;
        }
        for (int i = 0; i < dirs.mDirections.length; i += 2) {
            int here = linestart + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + 1] & RUN_LENGTH_MASK);
            if (there > lineend) {
                there = lineend;
            }
            if (start <= there && end >= here) {
                int st = Math.max(start, here);
                int en = Math.min(end, there);
                if (st != en) {
                    float h1 = getHorizontal(st, false, line, false);
                    float h2 = getHorizontal(en, true, line, false);
                    Path path = dest;
                    path.addRect(Math.min(h1, h2), (float) top, Math.max(h1, h2), (float) bottom, Direction.CW);
                }
            }
        }
    }

    public void getSelectionPath(int start, int end, Path dest) {
        dest.reset();
        if (start != end) {
            if (end < start) {
                int temp = end;
                end = start;
                start = temp;
            }
            int startline = getLineForOffset(start);
            int endline = getLineForOffset(end);
            int top = getLineTop(startline);
            int bottom = getLineBottom(endline);
            if (startline == endline) {
                addSelection(startline, start, end, top, bottom, dest);
            } else {
                float width = (float) this.mWidth;
                addSelection(startline, start, getLineEnd(startline), top, getLineBottom(startline), dest);
                if (getParagraphDirection(startline) == -1) {
                    dest.addRect(getLineLeft(startline), (float) top, 0.0f, (float) getLineBottom(startline), Direction.CW);
                } else {
                    dest.addRect(getLineRight(startline), (float) top, width, (float) getLineBottom(startline), Direction.CW);
                }
                for (int i = startline + 1; i < endline; i++) {
                    Path path = dest;
                    float f = width;
                    path.addRect(0.0f, (float) getLineTop(i), f, (float) getLineBottom(i), Direction.CW);
                }
                top = getLineTop(endline);
                bottom = getLineBottom(endline);
                addSelection(endline, getLineStart(endline), end, top, bottom, dest);
                if (getParagraphDirection(endline) == -1) {
                    dest.addRect(width, (float) top, getLineRight(endline), (float) bottom, Direction.CW);
                } else {
                    dest.addRect(0.0f, (float) top, getLineLeft(endline), (float) bottom, Direction.CW);
                }
            }
        }
    }

    public final Alignment getParagraphAlignment(int line) {
        Alignment align = this.mAlignment;
        if (!this.mSpannedText) {
            return align;
        }
        AlignmentSpan[] spans = (AlignmentSpan[]) getParagraphSpans(this.mText, getLineStart(line), getLineEnd(line), AlignmentSpan.class);
        int spanLength = spans.length;
        if (spanLength > 0) {
            return spans[spanLength - 1].getAlignment();
        }
        return align;
    }

    public final int getParagraphLeft(int line) {
        if (getParagraphDirection(line) == -1 || (this.mSpannedText ^ 1) != 0) {
            return 0;
        }
        return getParagraphLeadingMargin(line);
    }

    public final int getParagraphRight(int line) {
        int right = this.mWidth;
        if (getParagraphDirection(line) == 1 || (this.mSpannedText ^ 1) != 0) {
            return right;
        }
        return right - getParagraphLeadingMargin(line);
    }

    private int getParagraphLeadingMargin(int line) {
        if (!this.mSpannedText) {
            return 0;
        }
        Spanned spanned = this.mText;
        int lineStart = getLineStart(line);
        LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans(spanned, lineStart, spanned.nextSpanTransition(lineStart, getLineEnd(line), LeadingMarginSpan.class), LeadingMarginSpan.class);
        if (spans.length == 0) {
            return 0;
        }
        int i;
        int margin = 0;
        boolean isFirstParaLine = lineStart != 0 ? spanned.charAt(lineStart + -1) == 10 : true;
        boolean useFirstLineMargin = isFirstParaLine;
        for (i = 0; i < spans.length; i++) {
            if (spans[i] instanceof LeadingMarginSpan2) {
                int i2;
                if (line < getLineForOffset(spanned.getSpanStart(spans[i])) + ((LeadingMarginSpan2) spans[i]).getLeadingMarginLineCount()) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                useFirstLineMargin |= i2;
            }
        }
        for (LeadingMarginSpan span : spans) {
            margin += span.getLeadingMargin(useFirstLineMargin);
        }
        return margin;
    }

    static float measurePara(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        MeasuredText mt = MeasuredText.obtain();
        TextLine tl = TextLine.obtain();
        try {
            Directions directions;
            int dir;
            float abs;
            mt.setPara(text, start, end, textDir, null);
            if (mt.mEasy) {
                directions = DIRS_ALL_LEFT_TO_RIGHT;
                dir = 1;
            } else {
                directions = AndroidBidi.directions(mt.mDir, mt.mLevels, 0, mt.mChars, 0, mt.mLen);
                dir = mt.mDir;
            }
            char[] chars = mt.mChars;
            int len = mt.mLen;
            boolean hasTabs = false;
            TabStops tabStops = null;
            int margin = 0;
            if (text instanceof Spanned) {
                for (LeadingMarginSpan lms : (LeadingMarginSpan[]) getParagraphSpans((Spanned) text, start, end, LeadingMarginSpan.class)) {
                    margin += lms.getLeadingMargin(true);
                }
            }
            for (int i = 0; i < len; i++) {
                if (chars[i] == 9) {
                    hasTabs = true;
                    if (text instanceof Spanned) {
                        Spanned spanned = (Spanned) text;
                        TabStopSpan[] spans = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, end, TabStopSpan.class), TabStopSpan.class);
                        if (spans.length > 0) {
                            tabStops = new TabStops(20, spans);
                        }
                    }
                    tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops);
                    abs = ((float) margin) + Math.abs(tl.metrics(null));
                    return abs;
                }
            }
            tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops);
            abs = ((float) margin) + Math.abs(tl.metrics(null));
            return abs;
        } finally {
            TextLine.recycle(tl);
            MeasuredText.recycle(mt);
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
            int i = 0;
            while (i < tabs.length) {
                if (alltabs || (tabs[i] instanceof TabStopSpan)) {
                    int where = ((TabStopSpan) tabs[i]).getTabStop();
                    if (((float) where) < nh && ((float) where) > h) {
                        nh = (float) where;
                    }
                }
                i++;
            }
            if (nh != Float.MAX_VALUE) {
                return nh;
            }
        }
        return (float) (((int) ((h + 20.0f) / 20.0f)) * 20);
    }

    protected final boolean isSpanned() {
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

    private char getEllipsisChar(TruncateAt method) {
        if (method == TruncateAt.END_SMALL) {
            return TextUtils.ELLIPSIS_TWO_DOTS[0];
        }
        return TextUtils.ELLIPSIS_NORMAL[0];
    }

    private void ellipsize(int start, int end, int line, char[] dest, int destoff, TruncateAt method) {
        int ellipsisCount = getEllipsisCount(line);
        if (ellipsisCount != 0) {
            int ellipsisStart = getEllipsisStart(line);
            int linestart = getLineStart(line);
            for (int i = ellipsisStart; i < ellipsisStart + ellipsisCount; i++) {
                char c;
                if (i == ellipsisStart) {
                    c = getEllipsisChar(method);
                } else {
                    c = 65279;
                }
                int a = i + linestart;
                if (a >= start && a < end) {
                    dest[(destoff + a) - start] = c;
                }
            }
        }
    }
}

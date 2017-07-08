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
import com.android.internal.widget.AutoScrollHelper;
import java.util.Arrays;
import javax.microedition.khronos.opengles.GL10;

public abstract class Layout {
    private static final /* synthetic */ int[] -android-text-Layout$AlignmentSwitchesValues = null;
    public static final int BREAK_STRATEGY_BALANCED = 2;
    public static final int BREAK_STRATEGY_HIGH_QUALITY = 1;
    public static final int BREAK_STRATEGY_SIMPLE = 0;
    static final Directions DIRS_ALL_LEFT_TO_RIGHT = null;
    static final Directions DIRS_ALL_RIGHT_TO_LEFT = null;
    public static final int DIR_LEFT_TO_RIGHT = 1;
    static final int DIR_REQUEST_DEFAULT_LTR = 2;
    static final int DIR_REQUEST_DEFAULT_RTL = -2;
    static final int DIR_REQUEST_LTR = 1;
    static final int DIR_REQUEST_RTL = -1;
    public static final int DIR_RIGHT_TO_LEFT = -1;
    public static final int HYPHENATION_FREQUENCY_FULL = 2;
    public static final int HYPHENATION_FREQUENCY_NONE = 0;
    public static final int HYPHENATION_FREQUENCY_NORMAL = 1;
    private static final ParagraphStyle[] NO_PARA_SPANS = null;
    static final int RUN_LENGTH_MASK = 67108863;
    static final int RUN_LEVEL_MASK = 63;
    static final int RUN_LEVEL_SHIFT = 26;
    static final int RUN_RTL_FLAG = 67108864;
    private static final int TAB_INCREMENT = 20;
    private static final Rect sTempRect = null;
    private Alignment mAlignment;
    private SpanSet<LineBackgroundSpan> mLineBackgroundSpans;
    private TextPaint mPaint;
    private float mSpacingAdd;
    private float mSpacingMult;
    private boolean mSpannedText;
    private CharSequence mText;
    private TextDirectionHeuristic mTextDir;
    private int mWidth;

    public enum Alignment {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.Layout.Alignment.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.Layout.Alignment.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.text.Layout.Alignment.<clinit>():void");
        }
    }

    public static class Directions {
        int[] mDirections;

        Directions(int[] dirs) {
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
            char[] buf = TextUtils.obtain(Layout.HYPHENATION_FREQUENCY_NORMAL);
            getChars(off, off + Layout.HYPHENATION_FREQUENCY_NORMAL, buf, Layout.HYPHENATION_FREQUENCY_NONE);
            char ret = buf[Layout.HYPHENATION_FREQUENCY_NONE];
            TextUtils.recycle(buf);
            return ret;
        }

        public void getChars(int start, int end, char[] dest, int destoff) {
            int line1 = this.mLayout.getLineForOffset(start);
            int line2 = this.mLayout.getLineForOffset(end);
            TextUtils.getChars(this.mText, start, end, dest, destoff);
            for (int i = line1; i <= line2; i += Layout.HYPHENATION_FREQUENCY_NORMAL) {
                this.mLayout.ellipsize(start, end, i, dest, destoff, this.mMethod);
            }
        }

        public int length() {
            return this.mText.length();
        }

        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, Layout.HYPHENATION_FREQUENCY_NONE);
            return new String(s);
        }

        public String toString() {
            char[] s = new char[length()];
            getChars(Layout.HYPHENATION_FREQUENCY_NONE, length(), s, Layout.HYPHENATION_FREQUENCY_NONE);
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
            getChars(start, end, s, Layout.HYPHENATION_FREQUENCY_NONE);
            SpannableString ss = new SpannableString(new String(s));
            TextUtils.copySpansFrom(this.mSpanned, start, end, Object.class, ss, Layout.HYPHENATION_FREQUENCY_NONE);
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
            int ns = Layout.HYPHENATION_FREQUENCY_NONE;
            if (spans != null) {
                int[] stops = this.mStops;
                int length = spans.length;
                int i = Layout.HYPHENATION_FREQUENCY_NONE;
                int ns2 = Layout.HYPHENATION_FREQUENCY_NONE;
                while (i < length) {
                    Object o = spans[i];
                    if (o instanceof TabStopSpan) {
                        if (stops == null) {
                            stops = new int[10];
                        } else if (ns2 == stops.length) {
                            int[] nstops = new int[(ns2 * Layout.HYPHENATION_FREQUENCY_FULL)];
                            for (int i2 = Layout.HYPHENATION_FREQUENCY_NONE; i2 < ns2; i2 += Layout.HYPHENATION_FREQUENCY_NORMAL) {
                                nstops[i2] = stops[i2];
                            }
                            stops = nstops;
                        }
                        ns = ns2 + Layout.HYPHENATION_FREQUENCY_NORMAL;
                        stops[ns2] = ((TabStopSpan) o).getTabStop();
                    } else {
                        ns = ns2;
                    }
                    i += Layout.HYPHENATION_FREQUENCY_NORMAL;
                    ns2 = ns;
                }
                if (ns2 > Layout.HYPHENATION_FREQUENCY_NORMAL) {
                    Arrays.sort(stops, Layout.HYPHENATION_FREQUENCY_NONE, ns2);
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
                for (int i = Layout.HYPHENATION_FREQUENCY_NONE; i < ns; i += Layout.HYPHENATION_FREQUENCY_NORMAL) {
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
            iArr[Alignment.ALIGN_LEFT.ordinal()] = HYPHENATION_FREQUENCY_NORMAL;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Alignment.ALIGN_NORMAL.ordinal()] = HYPHENATION_FREQUENCY_FULL;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.Layout.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.Layout.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.Layout.<clinit>():void");
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
        return getDesiredWidth(source, HYPHENATION_FREQUENCY_NONE, source.length(), paint);
    }

    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint) {
        float need = 0.0f;
        int i = start;
        while (i <= end) {
            int next = TextUtils.indexOf(source, '\n', i, end);
            if (next < 0) {
                next = end;
            }
            float w = measurePara(paint, source, i, next);
            if (w > need) {
                need = w;
            }
            i = next + HYPHENATION_FREQUENCY_NORMAL;
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
            paint.bgColor = HYPHENATION_FREQUENCY_NONE;
            paint.baselineShift = HYPHENATION_FREQUENCY_NONE;
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
        draw(c, null, null, HYPHENATION_FREQUENCY_NONE);
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

    public void drawText(Canvas canvas, int firstLine, int lastLine) {
        int previousLineBottom = getLineTop(firstLine);
        int previousLineEnd = getLineStart(firstLine);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        int spanEnd = HYPHENATION_FREQUENCY_NONE;
        Paint paint = this.mPaint;
        CharSequence buf = this.mText;
        Alignment paraAlign = this.mAlignment;
        boolean tabStopsIsInitialized = false;
        TextLine tl = TextLine.obtain();
        int lineNum = firstLine;
        TabStops tabStops = null;
        while (lineNum <= lastLine) {
            TabStops tabStops2;
            int x;
            int start = previousLineEnd;
            previousLineEnd = getLineStart(lineNum + HYPHENATION_FREQUENCY_NORMAL);
            int end = getLineVisibleEnd(lineNum, start, previousLineEnd);
            int ltop = previousLineBottom;
            int lbottom = getLineTop(lineNum + HYPHENATION_FREQUENCY_NORMAL);
            previousLineBottom = lbottom;
            int lbaseline = lbottom - getLineDescent(lineNum);
            int dir = getParagraphDirection(lineNum);
            int left = HYPHENATION_FREQUENCY_NONE;
            int right = this.mWidth;
            if (this.mSpannedText) {
                boolean isFirstParaLine;
                int n;
                Spanned sp = (Spanned) buf;
                int textLength = buf.length();
                if (start == 0 || buf.charAt(start + DIR_RIGHT_TO_LEFT) == '\n') {
                    isFirstParaLine = true;
                } else {
                    isFirstParaLine = false;
                }
                if (start >= spanEnd && (lineNum == firstLine || isFirstParaLine)) {
                    spanEnd = sp.nextSpanTransition(start, textLength, ParagraphStyle.class);
                    spans = (ParagraphStyle[]) getParagraphSpans(sp, start, spanEnd, ParagraphStyle.class);
                    paraAlign = this.mAlignment;
                    for (n = spans.length + DIR_RIGHT_TO_LEFT; n >= 0; n += DIR_RIGHT_TO_LEFT) {
                        if (spans[n] instanceof AlignmentSpan) {
                            paraAlign = ((AlignmentSpan) spans[n]).getAlignment();
                            break;
                        }
                    }
                    tabStopsIsInitialized = false;
                }
                int length = spans.length;
                boolean useFirstLineMargin = isFirstParaLine;
                for (n = HYPHENATION_FREQUENCY_NONE; n < length; n += HYPHENATION_FREQUENCY_NORMAL) {
                    if (spans[n] instanceof LeadingMarginSpan2) {
                        if (lineNum < getLineForOffset(sp.getSpanStart(spans[n])) + ((LeadingMarginSpan2) spans[n]).getLeadingMarginLineCount()) {
                            useFirstLineMargin = true;
                            break;
                        }
                    }
                }
                for (n = HYPHENATION_FREQUENCY_NONE; n < length; n += HYPHENATION_FREQUENCY_NORMAL) {
                    if (spans[n] instanceof LeadingMarginSpan) {
                        LeadingMarginSpan margin = spans[n];
                        if (dir == DIR_RIGHT_TO_LEFT) {
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
            if (!hasTab || tabStopsIsInitialized) {
                tabStops2 = tabStops;
            } else {
                if (tabStops == null) {
                    TabStops tabStops3 = new TabStops(TAB_INCREMENT, spans);
                } else {
                    tabStops.reset(TAB_INCREMENT, spans);
                    tabStops2 = tabStops;
                }
                tabStopsIsInitialized = true;
            }
            Alignment align = paraAlign;
            if (align == Alignment.ALIGN_LEFT) {
                align = dir == HYPHENATION_FREQUENCY_NORMAL ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
            } else if (align == Alignment.ALIGN_RIGHT) {
                align = dir == HYPHENATION_FREQUENCY_NORMAL ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
            }
            if (align != Alignment.ALIGN_NORMAL) {
                int max = (int) getLineExtent(lineNum, tabStops2, false);
                if (align != Alignment.ALIGN_OPPOSITE) {
                    x = (((right + left) - (max & DIR_REQUEST_DEFAULT_RTL)) >> HYPHENATION_FREQUENCY_NORMAL) + getIndentAdjust(lineNum, Alignment.ALIGN_CENTER);
                } else if (dir == HYPHENATION_FREQUENCY_NORMAL) {
                    x = (right - max) + getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                } else {
                    x = (left - max) + getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                }
            } else if (dir == HYPHENATION_FREQUENCY_NORMAL) {
                x = left + getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
            } else {
                x = right + getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
            }
            paint.setHyphenEdit(getHyphen(lineNum));
            Directions directions = getLineDirections(lineNum);
            if (directions != DIRS_ALL_LEFT_TO_RIGHT || this.mSpannedText || hasTab) {
                tl.set(paint, buf, start, end, dir, directions, hasTab, tabStops2);
                tl.draw(canvas, (float) x, ltop, lbaseline, lbottom);
            } else {
                canvas.drawText(buf, start, end, (float) x, (float) lbaseline, paint);
            }
            paint.setHyphenEdit(HYPHENATION_FREQUENCY_NONE);
            lineNum += HYPHENATION_FREQUENCY_NORMAL;
            tabStops = tabStops2;
        }
        TextLine.recycle(tl);
    }

    public void drawBackground(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int firstLine, int lastLine) {
        if (this.mSpannedText) {
            if (this.mLineBackgroundSpans == null) {
                this.mLineBackgroundSpans = new SpanSet(LineBackgroundSpan.class);
            }
            Spanned buffer = this.mText;
            int textLength = buffer.length();
            this.mLineBackgroundSpans.init(buffer, HYPHENATION_FREQUENCY_NONE, textLength);
            if (this.mLineBackgroundSpans.numberOfSpans > 0) {
                int previousLineBottom = getLineTop(firstLine);
                int previousLineEnd = getLineStart(firstLine);
                ParagraphStyle[] spans = NO_PARA_SPANS;
                int spansLength = HYPHENATION_FREQUENCY_NONE;
                TextPaint paint = this.mPaint;
                int spanEnd = HYPHENATION_FREQUENCY_NONE;
                int width = this.mWidth;
                for (int i = firstLine; i <= lastLine; i += HYPHENATION_FREQUENCY_NORMAL) {
                    int start = previousLineEnd;
                    int end = getLineStart(i + HYPHENATION_FREQUENCY_NORMAL);
                    previousLineEnd = end;
                    int ltop = previousLineBottom;
                    int lbottom = getLineTop(i + HYPHENATION_FREQUENCY_NORMAL);
                    previousLineBottom = lbottom;
                    int lbaseline = lbottom - getLineDescent(i);
                    if (start >= spanEnd) {
                        spanEnd = this.mLineBackgroundSpans.getNextTransition(start, textLength);
                        spansLength = HYPHENATION_FREQUENCY_NONE;
                        if (start != end || start == 0) {
                            int j = HYPHENATION_FREQUENCY_NONE;
                            while (j < this.mLineBackgroundSpans.numberOfSpans) {
                                if (this.mLineBackgroundSpans.spanStarts[j] < end && this.mLineBackgroundSpans.spanEnds[j] > start) {
                                    spans = (ParagraphStyle[]) GrowingArrayUtils.append((Object[]) spans, spansLength, ((LineBackgroundSpan[]) this.mLineBackgroundSpans.spans)[j]);
                                    spansLength += HYPHENATION_FREQUENCY_NORMAL;
                                }
                                j += HYPHENATION_FREQUENCY_NORMAL;
                            }
                        }
                    }
                    for (int n = HYPHENATION_FREQUENCY_NONE; n < spansLength; n += HYPHENATION_FREQUENCY_NORMAL) {
                        spans[n].drawBackground(canvas, paint, HYPHENATION_FREQUENCY_NONE, width, ltop, lbaseline, lbottom, buffer, start, end, i);
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

    public long getLineRangeForDraw(Canvas canvas) {
        synchronized (sTempRect) {
            if (canvas.getClipBounds(sTempRect)) {
                int dtop = sTempRect.top;
                int dbottom = sTempRect.bottom;
                int top = Math.max(dtop, HYPHENATION_FREQUENCY_NONE);
                int bottom = Math.min(getLineTop(getLineCount()), dbottom);
                if (top >= bottom) {
                    return TextUtils.packRangeInLong(HYPHENATION_FREQUENCY_NONE, DIR_RIGHT_TO_LEFT);
                }
                return TextUtils.packRangeInLong(getLineForVertical(top), getLineForVertical(bottom));
            }
            long packRangeInLong = TextUtils.packRangeInLong(HYPHENATION_FREQUENCY_NONE, DIR_RIGHT_TO_LEFT);
            return packRangeInLong;
        }
    }

    private int getLineStartPos(int line, int left, int right) {
        Alignment align = getParagraphAlignment(line);
        int dir = getParagraphDirection(line);
        if (align == Alignment.ALIGN_LEFT) {
            align = dir == HYPHENATION_FREQUENCY_NORMAL ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
        } else if (align == Alignment.ALIGN_RIGHT) {
            align = dir == HYPHENATION_FREQUENCY_NORMAL ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
        }
        if (align != Alignment.ALIGN_NORMAL) {
            TabStops tabStops = null;
            if (this.mSpannedText && getLineContainsTab(line)) {
                Spanned spanned = this.mText;
                int start = getLineStart(line);
                TabStopSpan[] tabSpans = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, spanned.length(), TabStopSpan.class), TabStopSpan.class);
                if (tabSpans.length > 0) {
                    tabStops = new TabStops(TAB_INCREMENT, tabSpans);
                }
            }
            int max = (int) getLineExtent(line, tabStops, false);
            if (align != Alignment.ALIGN_OPPOSITE) {
                return ((left + right) - (max & DIR_REQUEST_DEFAULT_RTL)) >> (getIndentAdjust(line, Alignment.ALIGN_CENTER) + HYPHENATION_FREQUENCY_NORMAL);
            } else if (dir == HYPHENATION_FREQUENCY_NORMAL) {
                return (right - max) + getIndentAdjust(line, Alignment.ALIGN_RIGHT);
            } else {
                return (left - max) + getIndentAdjust(line, Alignment.ALIGN_LEFT);
            }
        } else if (dir == HYPHENATION_FREQUENCY_NORMAL) {
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
            bounds.left = HYPHENATION_FREQUENCY_NONE;
            bounds.top = getLineTop(line);
            bounds.right = this.mWidth;
            bounds.bottom = getLineTop(line + HYPHENATION_FREQUENCY_NORMAL);
        }
        return getLineBaseline(line);
    }

    public int getHyphen(int line) {
        return HYPHENATION_FREQUENCY_NONE;
    }

    public int getIndentAdjust(int line, Alignment alignment) {
        return HYPHENATION_FREQUENCY_NONE;
    }

    public boolean isLevelBoundary(int offset) {
        boolean z = false;
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT || dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return false;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        if (offset == lineStart || offset == lineEnd) {
            if (((runs[(offset == lineStart ? HYPHENATION_FREQUENCY_NONE : runs.length + DIR_REQUEST_DEFAULT_RTL) + HYPHENATION_FREQUENCY_NORMAL] >>> RUN_LEVEL_SHIFT) & RUN_LEVEL_MASK) != (getParagraphDirection(line) == HYPHENATION_FREQUENCY_NORMAL ? HYPHENATION_FREQUENCY_NONE : HYPHENATION_FREQUENCY_NORMAL)) {
                z = true;
            }
            return z;
        }
        offset -= lineStart;
        for (int i = HYPHENATION_FREQUENCY_NONE; i < runs.length; i += HYPHENATION_FREQUENCY_FULL) {
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
        int i = HYPHENATION_FREQUENCY_NONE;
        while (i < runs.length) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
            if (offset < start || offset >= limit) {
                i += HYPHENATION_FREQUENCY_FULL;
            } else {
                if ((((runs[i + HYPHENATION_FREQUENCY_NORMAL] >>> RUN_LEVEL_SHIFT) & RUN_LEVEL_MASK) & HYPHENATION_FREQUENCY_NORMAL) == 0) {
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
            return TextUtils.packRangeInLong(HYPHENATION_FREQUENCY_NONE, getLineEnd(line));
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        for (int i = HYPHENATION_FREQUENCY_NONE; i < runs.length; i += HYPHENATION_FREQUENCY_FULL) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
            if (offset >= start && offset < limit) {
                return TextUtils.packRangeInLong(start, limit);
            }
        }
        return TextUtils.packRangeInLong(HYPHENATION_FREQUENCY_NONE, getLineEnd(line));
    }

    private boolean primaryIsTrailingPrevious(int offset) {
        int levelBefore;
        boolean z = true;
        int line = getLineForOffset(offset);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        int levelAt = DIR_RIGHT_TO_LEFT;
        int i = HYPHENATION_FREQUENCY_NONE;
        while (i < runs.length) {
            int start = lineStart + runs[i];
            int limit = start + (runs[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (offset < start || offset >= limit) {
                i += HYPHENATION_FREQUENCY_FULL;
            } else if (offset > start) {
                return false;
            } else {
                levelAt = (runs[i + HYPHENATION_FREQUENCY_NORMAL] >>> RUN_LEVEL_SHIFT) & RUN_LEVEL_MASK;
                if (levelAt == DIR_RIGHT_TO_LEFT) {
                    levelAt = getParagraphDirection(line) != HYPHENATION_FREQUENCY_NORMAL ? HYPHENATION_FREQUENCY_NONE : HYPHENATION_FREQUENCY_NORMAL;
                }
                levelBefore = DIR_RIGHT_TO_LEFT;
                if (offset == lineStart) {
                    offset += DIR_RIGHT_TO_LEFT;
                    for (i = HYPHENATION_FREQUENCY_NONE; i < runs.length; i += HYPHENATION_FREQUENCY_FULL) {
                        start = lineStart + runs[i];
                        limit = start + (runs[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
                        if (limit > lineEnd) {
                            limit = lineEnd;
                        }
                        if (offset < start && offset < limit) {
                            levelBefore = (runs[i + HYPHENATION_FREQUENCY_NORMAL] >>> RUN_LEVEL_SHIFT) & RUN_LEVEL_MASK;
                            break;
                        }
                    }
                } else if (getParagraphDirection(line) != HYPHENATION_FREQUENCY_NORMAL) {
                    levelBefore = HYPHENATION_FREQUENCY_NONE;
                } else {
                    levelBefore = HYPHENATION_FREQUENCY_NORMAL;
                }
                if (levelBefore >= levelAt) {
                    z = false;
                }
                return z;
            }
        }
        if (levelAt == DIR_RIGHT_TO_LEFT) {
            if (getParagraphDirection(line) != HYPHENATION_FREQUENCY_NORMAL) {
            }
        }
        levelBefore = DIR_RIGHT_TO_LEFT;
        if (offset == lineStart) {
            offset += DIR_RIGHT_TO_LEFT;
            while (i < runs.length) {
                start = lineStart + runs[i];
                limit = start + (runs[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
                if (limit > lineEnd) {
                    limit = lineEnd;
                }
                if (offset < start) {
                }
            }
        } else if (getParagraphDirection(line) != HYPHENATION_FREQUENCY_NORMAL) {
            levelBefore = HYPHENATION_FREQUENCY_NORMAL;
        } else {
            levelBefore = HYPHENATION_FREQUENCY_NONE;
        }
        if (levelBefore >= levelAt) {
            z = false;
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
        return getHorizontal(offset, !primaryIsTrailingPrevious(offset), clamped);
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
                tabStops = new TabStops(TAB_INCREMENT, tabs);
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
            if (dir == DIR_RIGHT_TO_LEFT) {
                return ((float) getParagraphRight(line)) - getLineMax(line);
            }
            return 0.0f;
        } else if (align == Alignment.ALIGN_RIGHT) {
            return ((float) this.mWidth) - getLineMax(line);
        } else {
            if (align != Alignment.ALIGN_OPPOSITE) {
                int left = getParagraphLeft(line);
                return (float) ((((getParagraphRight(line) - left) - (((int) getLineMax(line)) & DIR_REQUEST_DEFAULT_RTL)) / HYPHENATION_FREQUENCY_FULL) + left);
            } else if (dir == DIR_RIGHT_TO_LEFT) {
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
            if (dir == DIR_RIGHT_TO_LEFT) {
                return (float) this.mWidth;
            }
            return ((float) getParagraphLeft(line)) + getLineMax(line);
        } else if (align == Alignment.ALIGN_RIGHT) {
            return (float) this.mWidth;
        } else {
            if (align != Alignment.ALIGN_OPPOSITE) {
                int left = getParagraphLeft(line);
                int right = getParagraphRight(line);
                return (float) (right - (((right - left) - (((int) getLineMax(line)) & DIR_REQUEST_DEFAULT_RTL)) / HYPHENATION_FREQUENCY_FULL));
            } else if (dir == DIR_RIGHT_TO_LEFT) {
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
                tabStops = new TabStops(TAB_INCREMENT, tabs);
            }
        }
        Directions directions = getLineDirections(line);
        if (directions == null) {
            return 0.0f;
        }
        int dir = getParagraphDirection(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabs, tabStops);
        float width = tl.metrics(null);
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
        tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTabs, tabStops);
        float width = tl.metrics(null);
        TextLine.recycle(tl);
        return width;
    }

    public int getLineForVertical(int vertical) {
        int high = getLineCount();
        int low = DIR_RIGHT_TO_LEFT;
        while (high - low > HYPHENATION_FREQUENCY_NORMAL) {
            int guess = (high + low) / HYPHENATION_FREQUENCY_FULL;
            if (getLineTop(guess) > vertical) {
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return HYPHENATION_FREQUENCY_NONE;
        }
        return low;
    }

    public int getLineForOffset(int offset) {
        int high = getLineCount();
        int low = DIR_RIGHT_TO_LEFT;
        while (high - low > HYPHENATION_FREQUENCY_NORMAL) {
            int guess = (high + low) / HYPHENATION_FREQUENCY_FULL;
            if (getLineStart(guess) > offset) {
                high = guess;
            } else {
                low = guess;
            }
        }
        if (low < 0) {
            return HYPHENATION_FREQUENCY_NONE;
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
        if (line == getLineCount() + DIR_RIGHT_TO_LEFT) {
            max = lineEndOffset;
        } else {
            max = tl.getOffsetToLeftRightOf(lineEndOffset - lineStartOffset, !isRtlCharAt(lineEndOffset + DIR_RIGHT_TO_LEFT)) + lineStartOffset;
        }
        int best = lineStartOffset;
        float bestdist = Math.abs(getHorizontal(lineStartOffset, primary) - horiz);
        for (int i = HYPHENATION_FREQUENCY_NONE; i < dirs.mDirections.length; i += HYPHENATION_FREQUENCY_FULL) {
            int here = lineStartOffset + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
            boolean isRtl = (dirs.mDirections[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_RTL_FLAG) != 0;
            int swap = isRtl ? DIR_RIGHT_TO_LEFT : HYPHENATION_FREQUENCY_NORMAL;
            if (there > max) {
                there = max;
            }
            int high = (there + DIR_RIGHT_TO_LEFT) + HYPHENATION_FREQUENCY_NORMAL;
            int low = (here + HYPHENATION_FREQUENCY_NORMAL) + DIR_RIGHT_TO_LEFT;
            while (high - low > HYPHENATION_FREQUENCY_NORMAL) {
                int guess = (high + low) / HYPHENATION_FREQUENCY_FULL;
                if (getHorizontal(getOffsetAtStartOf(guess), primary) * ((float) swap) >= ((float) swap) * horiz) {
                    high = guess;
                } else {
                    low = guess;
                }
            }
            if (low < here + HYPHENATION_FREQUENCY_NORMAL) {
                low = here + HYPHENATION_FREQUENCY_NORMAL;
            }
            if (low < there) {
                int aft = tl.getOffsetToLeftRightOf(low - lineStartOffset, isRtl) + lineStartOffset;
                low = tl.getOffsetToLeftRightOf(aft - lineStartOffset, !isRtl) + lineStartOffset;
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
        return getLineStart(line + HYPHENATION_FREQUENCY_NORMAL);
    }

    public int getLineVisibleEnd(int line) {
        return getLineVisibleEnd(line, getLineStart(line), getLineStart(line + HYPHENATION_FREQUENCY_NORMAL));
    }

    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = this.mText;
        if (line == getLineCount() + DIR_RIGHT_TO_LEFT) {
            return end;
        }
        while (end > start) {
            char ch = text.charAt(end + DIR_RIGHT_TO_LEFT);
            if (ch == '\n') {
                return end + DIR_RIGHT_TO_LEFT;
            }
            if (!(ch == ' ' || ch == '\t' || ch == '\u1680')) {
                if ('\u2000' <= ch && ch <= '\u200a') {
                    if (ch != '\u2007') {
                        continue;
                    }
                }
                if (!(ch == '\u205f' || ch == '\u3000')) {
                    break;
                }
            }
            end += DIR_RIGHT_TO_LEFT;
        }
        return end;
    }

    public final int getLineBottom(int line) {
        return getLineTop(line + HYPHENATION_FREQUENCY_NORMAL);
    }

    public final int getLineBaseline(int line) {
        return getLineTop(line + HYPHENATION_FREQUENCY_NORMAL) - getLineDescent(line);
    }

    public final int getLineAscent(int line) {
        return getLineTop(line) - (getLineTop(line + HYPHENATION_FREQUENCY_NORMAL) - getLineDescent(line));
    }

    public int getOffsetToLeftOf(int offset) {
        return getOffsetToLeftRightOf(offset, true);
    }

    public int getOffsetToRightOf(int offset) {
        return getOffsetToLeftRightOf(offset, false);
    }

    private int getOffsetToLeftRightOf(int caret, boolean toLeft) {
        boolean z;
        boolean advance = true;
        int line = getLineForOffset(caret);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int lineDir = getParagraphDirection(line);
        boolean lineChanged = false;
        if (lineDir == DIR_RIGHT_TO_LEFT) {
            z = true;
        } else {
            z = false;
        }
        if (toLeft != z) {
            advance = false;
        }
        if (advance) {
            if (caret == lineEnd) {
                if (line >= getLineCount() + DIR_RIGHT_TO_LEFT) {
                    return caret;
                }
                lineChanged = true;
                line += HYPHENATION_FREQUENCY_NORMAL;
            }
        } else if (caret == lineStart) {
            if (line <= 0) {
                return caret;
            }
            lineChanged = true;
            line += DIR_RIGHT_TO_LEFT;
        }
        if (lineChanged) {
            lineStart = getLineStart(line);
            lineEnd = getLineEnd(line);
            int newDir = getParagraphDirection(line);
            if (newDir != lineDir) {
                toLeft = !toLeft;
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
            return HYPHENATION_FREQUENCY_NONE;
        }
        CharSequence text = this.mText;
        char c = text.charAt(offset);
        if (c >= '\udc00' && c <= '\udfff') {
            char c1 = text.charAt(offset + DIR_RIGHT_TO_LEFT);
            if (c1 >= '\ud800' && c1 <= '\udbff') {
                offset += DIR_RIGHT_TO_LEFT;
            }
        }
        if (this.mSpannedText) {
            ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset, offset, ReplacementSpan.class);
            for (int i = HYPHENATION_FREQUENCY_NONE; i < spans.length; i += HYPHENATION_FREQUENCY_NORMAL) {
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
            case HYPHENATION_FREQUENCY_NORMAL /*1*/:
                return true;
            case HYPHENATION_FREQUENCY_FULL /*2*/:
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
        int bottom = getLineTop(line + HYPHENATION_FREQUENCY_NORMAL);
        boolean clamped = shouldClampCursor(line);
        float h1 = getPrimaryHorizontal(point, clamped) - 0.5f;
        float h2 = isLevelBoundary(point) ? getSecondaryHorizontal(point, clamped) - 0.5f : h1;
        int caps = MetaKeyKeyListener.getMetaState(editingBuffer, (int) HYPHENATION_FREQUENCY_NORMAL) | MetaKeyKeyListener.getMetaState(editingBuffer, (int) GL10.GL_EXP);
        int fn = MetaKeyKeyListener.getMetaState(editingBuffer, (int) HYPHENATION_FREQUENCY_FULL);
        int dist = HYPHENATION_FREQUENCY_NONE;
        if (!(caps == 0 && fn == 0)) {
            dist = (bottom - top) >> HYPHENATION_FREQUENCY_FULL;
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
            dest.lineTo(h1, (float) ((top + bottom) >> HYPHENATION_FREQUENCY_NORMAL));
            dest.moveTo(h2, (float) ((top + bottom) >> HYPHENATION_FREQUENCY_NORMAL));
            dest.lineTo(h2, (float) bottom);
        }
        if (caps == HYPHENATION_FREQUENCY_FULL) {
            dest.moveTo(h2, (float) bottom);
            dest.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            dest.lineTo(h2, (float) bottom);
            dest.lineTo(((float) dist) + h2, (float) (bottom + dist));
        } else if (caps == HYPHENATION_FREQUENCY_NORMAL) {
            dest.moveTo(h2, (float) bottom);
            dest.lineTo(h2 - ((float) dist), (float) (bottom + dist));
            dest.moveTo(h2 - ((float) dist), ((float) (bottom + dist)) - 0.5f);
            dest.lineTo(((float) dist) + h2, ((float) (bottom + dist)) - 0.5f);
            dest.moveTo(((float) dist) + h2, (float) (bottom + dist));
            dest.lineTo(h2, (float) bottom);
        }
        if (fn == HYPHENATION_FREQUENCY_FULL) {
            dest.moveTo(h1, (float) top);
            dest.lineTo(h1 - ((float) dist), (float) (top - dist));
            dest.lineTo(h1, (float) top);
            dest.lineTo(((float) dist) + h1, (float) (top - dist));
        } else if (fn == HYPHENATION_FREQUENCY_NORMAL) {
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
        if (lineend > linestart && this.mText.charAt(lineend + DIR_RIGHT_TO_LEFT) == '\n') {
            lineend += DIR_RIGHT_TO_LEFT;
        }
        for (int i = HYPHENATION_FREQUENCY_NONE; i < dirs.mDirections.length; i += HYPHENATION_FREQUENCY_FULL) {
            int here = linestart + dirs.mDirections[i];
            int there = here + (dirs.mDirections[i + HYPHENATION_FREQUENCY_NORMAL] & RUN_LENGTH_MASK);
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
                if (getParagraphDirection(startline) == DIR_RIGHT_TO_LEFT) {
                    dest.addRect(getLineLeft(startline), (float) top, 0.0f, (float) getLineBottom(startline), Direction.CW);
                } else {
                    dest.addRect(getLineRight(startline), (float) top, width, (float) getLineBottom(startline), Direction.CW);
                }
                for (int i = startline + HYPHENATION_FREQUENCY_NORMAL; i < endline; i += HYPHENATION_FREQUENCY_NORMAL) {
                    Path path = dest;
                    float f = width;
                    path.addRect(0.0f, (float) getLineTop(i), f, (float) getLineBottom(i), Direction.CW);
                }
                top = getLineTop(endline);
                bottom = getLineBottom(endline);
                addSelection(endline, getLineStart(endline), end, top, bottom, dest);
                if (getParagraphDirection(endline) == DIR_RIGHT_TO_LEFT) {
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
            return spans[spanLength + DIR_RIGHT_TO_LEFT].getAlignment();
        }
        return align;
    }

    public final int getParagraphLeft(int line) {
        if (getParagraphDirection(line) == DIR_RIGHT_TO_LEFT || !this.mSpannedText) {
            return HYPHENATION_FREQUENCY_NONE;
        }
        return getParagraphLeadingMargin(line);
    }

    public final int getParagraphRight(int line) {
        int right = this.mWidth;
        if (getParagraphDirection(line) == HYPHENATION_FREQUENCY_NORMAL || !this.mSpannedText) {
            return right;
        }
        return right - getParagraphLeadingMargin(line);
    }

    private int getParagraphLeadingMargin(int line) {
        if (!this.mSpannedText) {
            return HYPHENATION_FREQUENCY_NONE;
        }
        Spanned spanned = this.mText;
        int lineStart = getLineStart(line);
        LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans(spanned, lineStart, spanned.nextSpanTransition(lineStart, getLineEnd(line), LeadingMarginSpan.class), LeadingMarginSpan.class);
        if (spans.length == 0) {
            return HYPHENATION_FREQUENCY_NONE;
        }
        int i;
        int margin = HYPHENATION_FREQUENCY_NONE;
        boolean isFirstParaLine = lineStart != 0 ? spanned.charAt(lineStart + DIR_RIGHT_TO_LEFT) == '\n' : true;
        boolean useFirstLineMargin = isFirstParaLine;
        for (i = HYPHENATION_FREQUENCY_NONE; i < spans.length; i += HYPHENATION_FREQUENCY_NORMAL) {
            if (spans[i] instanceof LeadingMarginSpan2) {
                int i2;
                if (line < getLineForOffset(spanned.getSpanStart(spans[i])) + ((LeadingMarginSpan2) spans[i]).getLeadingMarginLineCount()) {
                    i2 = HYPHENATION_FREQUENCY_NORMAL;
                } else {
                    i2 = HYPHENATION_FREQUENCY_NONE;
                }
                useFirstLineMargin |= i2;
            }
        }
        for (i = HYPHENATION_FREQUENCY_NONE; i < spans.length; i += HYPHENATION_FREQUENCY_NORMAL) {
            margin += spans[i].getLeadingMargin(useFirstLineMargin);
        }
        return margin;
    }

    static float measurePara(TextPaint paint, CharSequence text, int start, int end) {
        MeasuredText mt = MeasuredText.obtain();
        TextLine tl = TextLine.obtain();
        try {
            Directions directions;
            int dir;
            Spanned spanned;
            float metrics;
            mt.setPara(text, start, end, TextDirectionHeuristics.LTR, null);
            if (mt.mEasy) {
                directions = DIRS_ALL_LEFT_TO_RIGHT;
                dir = HYPHENATION_FREQUENCY_NORMAL;
            } else {
                directions = AndroidBidi.directions(mt.mDir, mt.mLevels, HYPHENATION_FREQUENCY_NONE, mt.mChars, HYPHENATION_FREQUENCY_NONE, mt.mLen);
                dir = mt.mDir;
            }
            char[] chars = mt.mChars;
            int len = mt.mLen;
            boolean hasTabs = false;
            TabStops tabStops = null;
            int margin = HYPHENATION_FREQUENCY_NONE;
            if (text instanceof Spanned) {
                spanned = (Spanned) text;
                LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans(r0, start, end, LeadingMarginSpan.class);
                int length = spans.length;
                for (int i = HYPHENATION_FREQUENCY_NONE; i < length; i += HYPHENATION_FREQUENCY_NORMAL) {
                    margin += spans[i].getLeadingMargin(true);
                }
            }
            for (int i2 = HYPHENATION_FREQUENCY_NONE; i2 < len; i2 += HYPHENATION_FREQUENCY_NORMAL) {
                if (chars[i2] == '\t') {
                    hasTabs = true;
                    if (text instanceof Spanned) {
                        spanned = (Spanned) text;
                        int spanEnd = spanned.nextSpanTransition(start, end, TabStopSpan.class);
                        TabStopSpan[] spans2 = (TabStopSpan[]) getParagraphSpans(spanned, start, spanEnd, TabStopSpan.class);
                        if (spans2.length > 0) {
                            tabStops = new TabStops(TAB_INCREMENT, spans2);
                        }
                    }
                    tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops);
                    metrics = ((float) margin) + tl.metrics(null);
                    return metrics;
                }
            }
            tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops);
            metrics = ((float) margin) + tl.metrics(null);
            return metrics;
        } finally {
            TextLine.recycle(tl);
            MeasuredText.recycle(mt);
        }
    }

    static float nextTab(CharSequence text, int start, int end, float h, Object[] tabs) {
        float nh = AutoScrollHelper.NO_MAX;
        boolean alltabs = false;
        if (text instanceof Spanned) {
            if (tabs == null) {
                tabs = getParagraphSpans((Spanned) text, start, end, TabStopSpan.class);
                alltabs = true;
            }
            int i = HYPHENATION_FREQUENCY_NONE;
            while (i < tabs.length) {
                if (alltabs || (tabs[i] instanceof TabStopSpan)) {
                    int where = ((TabStopSpan) tabs[i]).getTabStop();
                    if (((float) where) < nh && ((float) where) > h) {
                        nh = (float) where;
                    }
                }
                i += HYPHENATION_FREQUENCY_NORMAL;
            }
            if (nh != AutoScrollHelper.NO_MAX) {
                return nh;
            }
        }
        return (float) (((int) ((h + 20.0f) / 20.0f)) * TAB_INCREMENT);
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
            return TextUtils.ELLIPSIS_TWO_DOTS[HYPHENATION_FREQUENCY_NONE];
        }
        return TextUtils.ELLIPSIS_NORMAL[HYPHENATION_FREQUENCY_NONE];
    }

    private void ellipsize(int start, int end, int line, char[] dest, int destoff, TruncateAt method) {
        int ellipsisCount = getEllipsisCount(line);
        if (ellipsisCount != 0) {
            int ellipsisStart = getEllipsisStart(line);
            int linestart = getLineStart(line);
            for (int i = ellipsisStart; i < ellipsisStart + ellipsisCount; i += HYPHENATION_FREQUENCY_NORMAL) {
                char ellipsisChar;
                if (i == ellipsisStart) {
                    ellipsisChar = getEllipsisChar(method);
                } else {
                    ellipsisChar = '\ufeff';
                }
                int a = i + linestart;
                if (a >= start && a < end) {
                    dest[(destoff + a) - start] = ellipsisChar;
                }
            }
        }
    }
}

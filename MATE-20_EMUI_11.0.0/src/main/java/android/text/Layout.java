package android.text;

import android.annotation.UnsupportedAppUsage;
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
    @UnsupportedAppUsage
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new int[]{0, RUN_LENGTH_MASK});
    @UnsupportedAppUsage
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static final Directions DIRS_ALL_RIGHT_TO_LEFT = new Directions(new int[]{0, 134217727});
    public static final int DIR_LEFT_TO_RIGHT = 1;
    @UnsupportedAppUsage
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
    private static final float TAB_INCREMENT = 20.0f;
    public static final int TEXT_SELECTION_LAYOUT_LEFT_TO_RIGHT = 1;
    public static final int TEXT_SELECTION_LAYOUT_RIGHT_TO_LEFT = 0;
    private static final Rect sTempRect = new Rect();
    private Alignment mAlignment;
    private int mJustificationMode;
    private SpanSet<LineBackgroundSpan> mLineBackgroundSpans;
    @UnsupportedAppUsage
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
            int next = TextUtils.indexOf(source, '\n', i, end);
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
        int lineEnd;
        if (this.mJustificationMode == 0 || (lineEnd = getLineEnd(lineNum)) >= this.mText.length() || this.mText.charAt(lineEnd - 1) == '\n') {
            return false;
        }
        return true;
    }

    /* JADX INFO: Multiple debug info for r11v0 int: [D('length' int), D('spanEnd' int)] */
    private float getJustifyWidth(int lineNum) {
        Alignment align;
        int indentWidth;
        Alignment paraAlign = this.mAlignment;
        int left = 0;
        int right = this.mWidth;
        int dir = getParagraphDirection(lineNum);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        if (this.mSpannedText) {
            Spanned sp = (Spanned) this.mText;
            int start = getLineStart(lineNum);
            boolean isFirstParaLine = start == 0 || this.mText.charAt(start + -1) == '\n';
            if (isFirstParaLine) {
                spans = (ParagraphStyle[]) getParagraphSpans(sp, start, sp.nextSpanTransition(start, this.mText.length(), ParagraphStyle.class), ParagraphStyle.class);
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
            int spanEnd = spans.length;
            boolean useFirstLineMargin = isFirstParaLine;
            int n2 = 0;
            while (true) {
                if (n2 >= spanEnd) {
                    break;
                }
                if (spans[n2] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                    if (lineNum < getLineForOffset(sp.getSpanStart(spans[n2])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans[n2]).getLeadingMarginLineCount()) {
                        useFirstLineMargin = true;
                        break;
                    }
                }
                n2++;
            }
            for (int n3 = 0; n3 < spanEnd; n3++) {
                if (spans[n3] instanceof LeadingMarginSpan) {
                    LeadingMarginSpan margin = (LeadingMarginSpan) spans[n3];
                    if (dir == -1) {
                        right -= margin.getLeadingMargin(useFirstLineMargin);
                    } else {
                        left += margin.getLeadingMargin(useFirstLineMargin);
                    }
                }
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

    /* JADX INFO: Multiple debug info for r0v5 int: [D('right' int), D('lbaseline' int)] */
    /* JADX INFO: Multiple debug info for r4v4 int: [D('paraAlign' android.text.Layout$Alignment), D('length' int)] */
    /* JADX INFO: Multiple debug info for r5v13 'dir'  int: [D('ltop' int), D('dir' int)] */
    /* JADX INFO: Multiple debug info for r5v14 'dir'  int: [D('ltop' int), D('dir' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x010f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x011d  */
    @UnsupportedAppUsage
    public void drawText(Canvas canvas, int firstLine, int lastLine) {
        TextLine tl;
        int lineNum;
        TabStops tabStops;
        int start;
        int lbaseline;
        int right;
        int left;
        boolean tabStopsIsInitialized;
        int previousLineEnd;
        int dir;
        CharSequence buf;
        int dir2;
        Layout layout;
        ParagraphStyle[] spans;
        TextPaint paint;
        TabStops tabStops2;
        int dir3;
        Alignment align;
        int i;
        int indentWidth;
        int x;
        int lineNum2;
        TabStops tabStops3;
        ParagraphStyle[] spans2;
        TextLine tl2;
        ParagraphStyle[] spans3;
        boolean isFirstParaLine;
        int spanEnd;
        Alignment paraAlign;
        ParagraphStyle[] spans4;
        int length;
        boolean useFirstLineMargin;
        int n;
        boolean useFirstLineMargin2;
        int n2;
        TextLine tl3;
        int lineNum3;
        TabStops tabStops4;
        int start2;
        ParagraphStyle[] spans5;
        int length2;
        int n3;
        int lbaseline2;
        int dir4;
        CharSequence buf2;
        int textLength;
        int dir5;
        Layout layout2;
        boolean useFirstLineMargin3;
        TextPaint paint2;
        Spanned sp;
        boolean useFirstLineMargin4;
        Alignment paraAlign2;
        Layout layout3 = this;
        int i2 = firstLine;
        int previousLineBottom = layout3.getLineTop(i2);
        int previousLineEnd2 = layout3.getLineStart(i2);
        ParagraphStyle[] spans6 = NO_PARA_SPANS;
        int spanEnd2 = 0;
        TextPaint paint3 = layout3.mWorkPaint;
        paint3.set(layout3.mPaint);
        CharSequence buf3 = layout3.mText;
        Alignment paraAlign3 = layout3.mAlignment;
        boolean tabStopsIsInitialized2 = false;
        TextLine tl4 = TextLine.obtain();
        TabStops tabStops5 = null;
        int lineNum4 = firstLine;
        while (lineNum4 <= lastLine) {
            int start3 = previousLineEnd2;
            int previousLineEnd3 = layout3.getLineStart(lineNum4 + 1);
            boolean justify = layout3.isJustificationRequired(lineNum4);
            int end = layout3.getLineVisibleEnd(lineNum4, start3, previousLineEnd3);
            paint3.setStartHyphenEdit(layout3.getStartHyphenEdit(lineNum4));
            paint3.setEndHyphenEdit(layout3.getEndHyphenEdit(lineNum4));
            int lbottom = layout3.getLineTop(lineNum4 + 1);
            int ltop = previousLineBottom;
            int ltop2 = layout3.getParagraphDirection(lineNum4);
            int lbaseline3 = lbottom - layout3.getLineDescent(lineNum4);
            int lbaseline4 = layout3.mWidth;
            TextLine tl5 = tl4;
            if (layout3.mSpannedText) {
                Spanned sp2 = (Spanned) buf3;
                int textLength2 = buf3.length();
                if (start3 != 0) {
                    previousLineEnd = previousLineEnd3;
                    spans3 = spans6;
                    if (buf3.charAt(start3 - 1) != '\n') {
                        isFirstParaLine = false;
                        if (start3 < spanEnd2) {
                            if (lineNum4 == i2 || isFirstParaLine) {
                                int spanEnd3 = sp2.nextSpanTransition(start3, textLength2, ParagraphStyle.class);
                                ParagraphStyle[] spans7 = (ParagraphStyle[]) getParagraphSpans(sp2, start3, spanEnd3, ParagraphStyle.class);
                                Alignment paraAlign4 = layout3.mAlignment;
                                int n4 = spans7.length - 1;
                                while (true) {
                                    if (n4 < 0) {
                                        paraAlign2 = paraAlign4;
                                        break;
                                    } else if (spans7[n4] instanceof AlignmentSpan) {
                                        paraAlign2 = ((AlignmentSpan) spans7[n4]).getAlignment();
                                        break;
                                    } else {
                                        n4--;
                                        paraAlign4 = paraAlign4;
                                    }
                                }
                                tabStopsIsInitialized = false;
                                spans4 = spans7;
                                spanEnd = spanEnd3;
                                paraAlign = paraAlign2;
                                length = spans4.length;
                                useFirstLineMargin = isFirstParaLine;
                                n = 0;
                                while (true) {
                                    if (n >= length) {
                                        useFirstLineMargin2 = useFirstLineMargin;
                                        break;
                                    }
                                    if (spans4[n] instanceof LeadingMarginSpan.LeadingMarginSpan2) {
                                        useFirstLineMargin4 = useFirstLineMargin;
                                        sp = sp2;
                                        if (lineNum4 < layout3.getLineForOffset(sp2.getSpanStart(spans4[n])) + ((LeadingMarginSpan.LeadingMarginSpan2) spans4[n]).getLeadingMarginLineCount()) {
                                            useFirstLineMargin2 = true;
                                            break;
                                        }
                                    } else {
                                        sp = sp2;
                                        useFirstLineMargin4 = useFirstLineMargin;
                                    }
                                    n++;
                                    useFirstLineMargin = useFirstLineMargin4;
                                    sp2 = sp;
                                }
                                n2 = 0;
                                left = 0;
                                right = lbaseline4;
                                while (n2 < length) {
                                    if (spans4[n2] instanceof LeadingMarginSpan) {
                                        LeadingMarginSpan margin = (LeadingMarginSpan) spans4[n2];
                                        if (ltop2 == -1) {
                                            lbaseline2 = lbaseline3;
                                            n3 = n2;
                                            length2 = length;
                                            spans5 = spans4;
                                            start2 = start3;
                                            tabStops4 = tabStops5;
                                            lineNum3 = lineNum4;
                                            tl3 = tl5;
                                            textLength = textLength2;
                                            buf2 = buf3;
                                            dir4 = ltop2;
                                            dir5 = ltop;
                                            margin.drawLeadingMargin(canvas, paint3, right, ltop2, dir5, lbaseline2, lbottom, buf3, start2, end, isFirstParaLine, this);
                                            right -= margin.getLeadingMargin(useFirstLineMargin2);
                                            layout2 = this;
                                            paint2 = paint3;
                                            useFirstLineMargin3 = useFirstLineMargin2;
                                        } else {
                                            n3 = n2;
                                            length2 = length;
                                            dir4 = ltop2;
                                            spans5 = spans4;
                                            start2 = start3;
                                            tabStops4 = tabStops5;
                                            lineNum3 = lineNum4;
                                            buf2 = buf3;
                                            lbaseline2 = lbaseline3;
                                            tl3 = tl5;
                                            dir5 = ltop;
                                            textLength = textLength2;
                                            paint2 = paint3;
                                            useFirstLineMargin3 = useFirstLineMargin2;
                                            layout2 = this;
                                            margin.drawLeadingMargin(canvas, paint3, left, dir4, dir5, lbaseline2, lbottom, buf2, start2, end, isFirstParaLine, this);
                                            left += margin.getLeadingMargin(useFirstLineMargin3);
                                        }
                                    } else {
                                        n3 = n2;
                                        useFirstLineMargin3 = useFirstLineMargin2;
                                        length2 = length;
                                        dir4 = ltop2;
                                        spans5 = spans4;
                                        start2 = start3;
                                        tabStops4 = tabStops5;
                                        lineNum3 = lineNum4;
                                        buf2 = buf3;
                                        paint2 = paint3;
                                        layout2 = layout3;
                                        lbaseline2 = lbaseline3;
                                        tl3 = tl5;
                                        dir5 = ltop;
                                        textLength = textLength2;
                                    }
                                    paint3 = paint2;
                                    layout3 = layout2;
                                    n2 = n3 + 1;
                                    ltop = dir5;
                                    textLength2 = textLength;
                                    buf3 = buf2;
                                    ltop2 = dir4;
                                    lbaseline3 = lbaseline2;
                                    length = length2;
                                    spans4 = spans5;
                                    start3 = start2;
                                    tabStops5 = tabStops4;
                                    lineNum4 = lineNum3;
                                    tl5 = tl3;
                                    useFirstLineMargin2 = useFirstLineMargin3;
                                }
                                dir = ltop2;
                                start = start3;
                                tabStops = tabStops5;
                                lineNum = lineNum4;
                                buf = buf3;
                                paint = paint3;
                                layout = layout3;
                                lbaseline = lbaseline3;
                                tl = tl5;
                                dir2 = ltop;
                                paraAlign3 = paraAlign;
                                spanEnd2 = spanEnd;
                                spans = spans4;
                            }
                        }
                        spanEnd = spanEnd2;
                        tabStopsIsInitialized = tabStopsIsInitialized2;
                        spans4 = spans3;
                        paraAlign = paraAlign3;
                        length = spans4.length;
                        useFirstLineMargin = isFirstParaLine;
                        n = 0;
                        while (true) {
                            if (n >= length) {
                            }
                            n++;
                            useFirstLineMargin = useFirstLineMargin4;
                            sp2 = sp;
                        }
                        n2 = 0;
                        left = 0;
                        right = lbaseline4;
                        while (n2 < length) {
                        }
                        dir = ltop2;
                        start = start3;
                        tabStops = tabStops5;
                        lineNum = lineNum4;
                        buf = buf3;
                        paint = paint3;
                        layout = layout3;
                        lbaseline = lbaseline3;
                        tl = tl5;
                        dir2 = ltop;
                        paraAlign3 = paraAlign;
                        spanEnd2 = spanEnd;
                        spans = spans4;
                    }
                } else {
                    previousLineEnd = previousLineEnd3;
                    spans3 = spans6;
                }
                isFirstParaLine = true;
                if (start3 < spanEnd2) {
                }
                spanEnd = spanEnd2;
                tabStopsIsInitialized = tabStopsIsInitialized2;
                spans4 = spans3;
                paraAlign = paraAlign3;
                length = spans4.length;
                useFirstLineMargin = isFirstParaLine;
                n = 0;
                while (true) {
                    if (n >= length) {
                    }
                    n++;
                    useFirstLineMargin = useFirstLineMargin4;
                    sp2 = sp;
                }
                n2 = 0;
                left = 0;
                right = lbaseline4;
                while (n2 < length) {
                }
                dir = ltop2;
                start = start3;
                tabStops = tabStops5;
                lineNum = lineNum4;
                buf = buf3;
                paint = paint3;
                layout = layout3;
                lbaseline = lbaseline3;
                tl = tl5;
                dir2 = ltop;
                paraAlign3 = paraAlign;
                spanEnd2 = spanEnd;
                spans = spans4;
            } else {
                previousLineEnd = previousLineEnd3;
                dir = ltop2;
                start = start3;
                tabStops = tabStops5;
                lineNum = lineNum4;
                buf = buf3;
                paint = paint3;
                layout = layout3;
                lbaseline = lbaseline3;
                tl = tl5;
                dir2 = ltop;
                tabStopsIsInitialized = tabStopsIsInitialized2;
                left = 0;
                right = lbaseline4;
                spans = spans6;
            }
            boolean hasTab = layout.getLineContainsTab(lineNum);
            if (!hasTab || tabStopsIsInitialized) {
                tabStops2 = tabStops;
            } else {
                TabStops tabStops6 = tabStops;
                if (tabStops6 == null) {
                    tabStops6 = new TabStops(TAB_INCREMENT, spans);
                } else {
                    tabStops6.reset(TAB_INCREMENT, spans);
                }
                tabStopsIsInitialized = true;
                tabStops2 = tabStops6;
            }
            if (paraAlign3 == Alignment.ALIGN_LEFT) {
                dir3 = dir;
                i = 1;
                align = dir3 == 1 ? Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
            } else {
                dir3 = dir;
                i = 1;
                if (paraAlign3 == Alignment.ALIGN_RIGHT) {
                    align = dir3 == 1 ? Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
                } else {
                    align = paraAlign3;
                }
            }
            if (align != Alignment.ALIGN_NORMAL) {
                int max = (int) layout.getLineExtent(lineNum, tabStops2, false);
                if (align != Alignment.ALIGN_OPPOSITE) {
                    int indentWidth2 = layout.getIndentAdjust(lineNum, Alignment.ALIGN_CENTER);
                    x = (((right + left) - (max & -2)) >> 1) + indentWidth2;
                    indentWidth = indentWidth2;
                } else if (dir3 == i) {
                    int indentWidth3 = -layout.getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                    indentWidth = indentWidth3;
                    x = (right - max) - indentWidth3;
                } else {
                    int indentWidth4 = layout.getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                    indentWidth = indentWidth4;
                    x = (left - max) + indentWidth4;
                }
            } else if (dir3 == i) {
                int indentWidth5 = layout.getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                indentWidth = indentWidth5;
                x = left + indentWidth5;
            } else {
                int indentWidth6 = -layout.getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                indentWidth = indentWidth6;
                x = right - indentWidth6;
            }
            Directions directions = layout.getLineDirections(lineNum);
            if (directions != DIRS_ALL_LEFT_TO_RIGHT || layout.mSpannedText || hasTab || justify) {
                spans2 = spans;
                tabStops3 = tabStops2;
                lineNum2 = lineNum;
                tl.set(paint, buf, start, end, dir3, directions, hasTab, tabStops2, layout.getEllipsisStart(lineNum), layout.getEllipsisStart(lineNum) + layout.getEllipsisCount(lineNum));
                if (justify) {
                    tl2 = tl;
                    tl2.justify((float) ((right - left) - indentWidth));
                } else {
                    tl2 = tl;
                }
                tl2.draw(canvas, (float) x, dir2, lbaseline, lbottom);
            } else {
                spans2 = spans;
                canvas.drawText(buf, start, end, (float) x, (float) lbaseline, paint);
                tabStops3 = tabStops2;
                lineNum2 = lineNum;
                tl2 = tl;
            }
            lineNum4 = lineNum2 + 1;
            i2 = firstLine;
            paint3 = paint;
            layout3 = layout;
            tl4 = tl2;
            previousLineBottom = lbottom;
            buf3 = buf;
            previousLineEnd2 = previousLineEnd;
            tabStopsIsInitialized2 = tabStopsIsInitialized;
            spans6 = spans2;
            tabStops5 = tabStops3;
        }
        TextLine.recycle(tl4);
    }

    /* JADX INFO: Multiple debug info for r4v2 int: [D('start' int), D('spansLength' int)] */
    @UnsupportedAppUsage
    public void drawBackground(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical, int firstLine, int lastLine) {
        int spanEnd;
        ParagraphStyle[] spans;
        int spansLength;
        if (this.mSpannedText) {
            if (this.mLineBackgroundSpans == null) {
                this.mLineBackgroundSpans = new SpanSet<>(LineBackgroundSpan.class);
            }
            Spanned buffer = (Spanned) this.mText;
            int textLength = buffer.length();
            this.mLineBackgroundSpans.init(buffer, 0, textLength);
            if (this.mLineBackgroundSpans.numberOfSpans > 0) {
                int previousLineBottom = getLineTop(firstLine);
                int previousLineEnd = getLineStart(firstLine);
                ParagraphStyle[] spans2 = NO_PARA_SPANS;
                int spansLength2 = 0;
                TextPaint paint = this.mPaint;
                int spanEnd2 = 0;
                int width = this.mWidth;
                int i = firstLine;
                while (i <= lastLine) {
                    int end = getLineStart(i + 1);
                    int start = previousLineEnd;
                    int lbottom = getLineTop(i + 1);
                    int lbaseline = lbottom - getLineDescent(i);
                    if (end >= spanEnd2) {
                        int spanEnd3 = this.mLineBackgroundSpans.getNextTransition(start, textLength);
                        int spansLength3 = 0;
                        if (start != end || start == 0) {
                            spanEnd = spanEnd3;
                            ParagraphStyle[] spans3 = spans2;
                            for (int j = 0; j < this.mLineBackgroundSpans.numberOfSpans; j++) {
                                if (this.mLineBackgroundSpans.spanStarts[j] < end && this.mLineBackgroundSpans.spanEnds[j] > start) {
                                    spans3 = (ParagraphStyle[]) GrowingArrayUtils.append((E[]) spans3, spansLength3, this.mLineBackgroundSpans.spans[j]);
                                    spansLength3++;
                                }
                            }
                            spans = spans3;
                            spansLength = spansLength3;
                        } else {
                            spanEnd = spanEnd3;
                            spans = spans2;
                            spansLength = 0;
                        }
                    } else {
                        spanEnd = spanEnd2;
                        spansLength = spansLength2;
                        spans = spans2;
                    }
                    int n = 0;
                    while (n < spansLength) {
                        ((LineBackgroundSpan) spans[n]).drawBackground(canvas, paint, 0, width, previousLineBottom, lbaseline, lbottom, buffer, start, end, i);
                        n++;
                        end = end;
                        start = start;
                        spansLength = spansLength;
                        i = i;
                        width = width;
                        paint = paint;
                        textLength = textLength;
                        buffer = buffer;
                    }
                    i++;
                    previousLineEnd = end;
                    spans2 = spans;
                    previousLineBottom = lbottom;
                    spanEnd2 = spanEnd;
                    spansLength2 = spansLength;
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

    @UnsupportedAppUsage
    public long getLineRangeForDraw(Canvas canvas) {
        int dtop;
        int dbottom;
        synchronized (sTempRect) {
            if (!canvas.getClipBounds(sTempRect)) {
                return TextUtils.packRangeInLong(0, -1);
            }
            dtop = sTempRect.top;
            dbottom = sTempRect.bottom;
        }
        int top = Math.max(dtop, 0);
        int bottom = Math.min(getLineTop(getLineCount()), dbottom);
        if (top >= bottom) {
            return TextUtils.packRangeInLong(0, -1);
        }
        return TextUtils.packRangeInLong(getLineForVertical(top), getLineForVertical(bottom));
    }

    /* JADX INFO: Multiple debug info for r2v4 int: [D('tabStops' android.text.Layout$TabStops), D('x' int)] */
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
                Spanned spanned = (Spanned) this.mText;
                int start = getLineStart(line);
                TabStopSpan[] tabSpans = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, spanned.length(), TabStopSpan.class), TabStopSpan.class);
                if (tabSpans.length > 0) {
                    tabStops = new TabStops(TAB_INCREMENT, tabSpans);
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

    public int getStartHyphenEdit(int line) {
        return 0;
    }

    public int getEndHyphenEdit(int line) {
        return 0;
    }

    public int getIndentAdjust(int line, Alignment alignment) {
        return 0;
    }

    @UnsupportedAppUsage
    public boolean isLevelBoundary(int offset) {
        int line = getLineForOffset(offset);
        Directions dirs = getLineDirections(line);
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT || dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return false;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        if (offset == lineStart || offset == lineEnd) {
            if (((runs[(offset == lineStart ? 0 : runs.length - 2) + 1] >>> 26) & 63) != (getParagraphDirection(line) == 1 ? 0 : 1)) {
                return true;
            }
            return false;
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
        if (dirs == DIRS_ALL_LEFT_TO_RIGHT) {
            return false;
        }
        if (dirs == DIRS_ALL_RIGHT_TO_LEFT) {
            return true;
        }
        int[] runs = dirs.mDirections;
        int lineStart = getLineStart(line);
        for (int i = 0; i < runs.length; i += 2) {
            int start = runs[i] + lineStart;
            int limit = (runs[i + 1] & RUN_LENGTH_MASK) + start;
            if (offset >= start && offset < limit) {
                if (((runs[i + 1] >>> 26) & 63 & 1) != 0) {
                    return true;
                } else {
                    return false;
                }
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

    @VisibleForTesting
    public boolean primaryIsTrailingPrevious(int offset) {
        int line = getLineForOffset(offset);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        int levelAt = -1;
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
                if (i2 < runs.length) {
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
                } else {
                    break;
                }
            }
        } else {
            levelBefore = getParagraphDirection(line) == 1 ? 0 : 1;
        }
        if (levelBefore < levelAt) {
            return true;
        }
        return false;
    }

    @VisibleForTesting
    public boolean[] primaryIsTrailingPreviousAllLineOffsets(int line) {
        byte b;
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int[] runs = getLineDirections(line).mDirections;
        boolean[] trailing = new boolean[((lineEnd - lineStart) + 1)];
        byte[] level = new byte[((lineEnd - lineStart) + 1)];
        for (int i = 0; i < runs.length; i += 2) {
            int start = runs[i] + lineStart;
            int limit = (runs[i + 1] & RUN_LENGTH_MASK) + start;
            if (limit > lineEnd) {
                limit = lineEnd;
            }
            if (limit != start) {
                level[(limit - lineStart) - 1] = (byte) ((runs[i + 1] >>> 26) & 63);
            }
        }
        for (int i2 = 0; i2 < runs.length; i2 += 2) {
            int start2 = runs[i2] + lineStart;
            byte currentLevel = (byte) ((runs[i2 + 1] >>> 26) & 63);
            int i3 = start2 - lineStart;
            boolean z = false;
            if (start2 == lineStart) {
                b = getParagraphDirection(line) == 1 ? (byte) 0 : 1;
            } else {
                b = level[(start2 - lineStart) - 1];
            }
            if (currentLevel > b) {
                z = true;
            }
            trailing[i3] = z;
        }
        return trailing;
    }

    public float getPrimaryHorizontal(int offset) {
        return getPrimaryHorizontal(offset, false);
    }

    @UnsupportedAppUsage
    public float getPrimaryHorizontal(int offset, boolean clamped) {
        return getHorizontal(offset, primaryIsTrailingPrevious(offset), clamped);
    }

    public float getSecondaryHorizontal(int offset) {
        return getSecondaryHorizontal(offset, false);
    }

    @UnsupportedAppUsage
    public float getSecondaryHorizontal(int offset, boolean clamped) {
        return getHorizontal(offset, !primaryIsTrailingPrevious(offset), clamped);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getHorizontal(int offset, boolean primary) {
        return primary ? getPrimaryHorizontal(offset) : getSecondaryHorizontal(offset);
    }

    private float getHorizontal(int offset, boolean trailing, boolean clamped) {
        return getHorizontal(offset, trailing, getLineForOffset(offset), clamped);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0071  */
    private float getHorizontal(int offset, boolean trailing, int line, boolean clamped) {
        TabStops tabStops;
        int start = getLineStart(line);
        int end = getLineEnd(line);
        int dir = getParagraphDirection(line);
        boolean hasTab = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        if (hasTab) {
            CharSequence charSequence = this.mText;
            if (charSequence instanceof Spanned) {
                TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) charSequence, start, end, TabStopSpan.class);
                if (tabs.length > 0) {
                    tabStops = new TabStops(TAB_INCREMENT, tabs);
                    TextLine tl = TextLine.obtain();
                    tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
                    float wid = tl.measure(offset - start, trailing, null);
                    TextLine.recycle(tl);
                    if (clamped) {
                        int i = this.mWidth;
                        if (wid > ((float) i)) {
                            wid = (float) i;
                        }
                    }
                    return ((float) getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line))) + wid;
                }
            }
        }
        tabStops = null;
        TextLine tl2 = TextLine.obtain();
        tl2.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        float wid2 = tl2.measure(offset - start, trailing, null);
        TextLine.recycle(tl2);
        if (clamped) {
        }
        return ((float) getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line))) + wid2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0066  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x007d  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00a8 A[LOOP:2: B:24:0x00a5->B:26:0x00a8, LOOP_END] */
    private float[] getLineHorizontals(int line, boolean clamped, boolean primary) {
        TabStops tabStops;
        float[] horizontal;
        int offset;
        int start = getLineStart(line);
        int end = getLineEnd(line);
        int dir = getParagraphDirection(line);
        boolean hasTab = getLineContainsTab(line);
        Directions directions = getLineDirections(line);
        if (hasTab) {
            CharSequence charSequence = this.mText;
            if (charSequence instanceof Spanned) {
                TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) charSequence, start, end, TabStopSpan.class);
                if (tabs.length > 0) {
                    tabStops = new TabStops(TAB_INCREMENT, tabs);
                    TextLine tl = TextLine.obtain();
                    tl.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
                    boolean[] trailings = primaryIsTrailingPreviousAllLineOffsets(line);
                    if (!primary) {
                        for (int offset2 = 0; offset2 < trailings.length; offset2++) {
                            trailings[offset2] = !trailings[offset2];
                        }
                    }
                    float[] wid = tl.measureAllOffsets(trailings, null);
                    TextLine.recycle(tl);
                    if (clamped) {
                        for (int offset3 = 0; offset3 < wid.length; offset3++) {
                            float f = wid[offset3];
                            int i = this.mWidth;
                            if (f > ((float) i)) {
                                wid[offset3] = (float) i;
                            }
                        }
                    }
                    int lineStartPos = getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line));
                    horizontal = new float[((end - start) + 1)];
                    for (offset = 0; offset < horizontal.length; offset++) {
                        horizontal[offset] = ((float) lineStartPos) + wid[offset];
                    }
                    return horizontal;
                }
            }
        }
        tabStops = null;
        TextLine tl2 = TextLine.obtain();
        tl2.set(this.mPaint, this.mText, start, end, dir, directions, hasTab, tabStops, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        boolean[] trailings2 = primaryIsTrailingPreviousAllLineOffsets(line);
        if (!primary) {
        }
        float[] wid2 = tl2.measureAllOffsets(trailings2, null);
        TextLine.recycle(tl2);
        if (clamped) {
        }
        int lineStartPos2 = getLineStartPos(line, getParagraphLeft(line), getParagraphRight(line));
        horizontal = new float[((end - start) + 1)];
        while (offset < horizontal.length) {
        }
        return horizontal;
    }

    public float getLineLeft(int line) {
        Alignment resultAlign;
        int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        if (align == null) {
            align = Alignment.ALIGN_CENTER;
        }
        int i = AnonymousClass1.$SwitchMap$android$text$Layout$Alignment[align.ordinal()];
        if (i != 1) {
            resultAlign = i != 2 ? i != 3 ? i != 4 ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT : Alignment.ALIGN_CENTER : dir == -1 ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
        } else {
            resultAlign = dir == -1 ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
        }
        int i2 = AnonymousClass1.$SwitchMap$android$text$Layout$Alignment[resultAlign.ordinal()];
        if (i2 == 3) {
            return (float) Math.floor((double) (((float) getParagraphLeft(line)) + ((((float) this.mWidth) - getLineMax(line)) / 2.0f)));
        } else if (i2 != 4) {
            return 0.0f;
        } else {
            return ((float) this.mWidth) - getLineMax(line);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.text.Layout$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$text$Layout$Alignment = new int[Alignment.values().length];

        static {
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_NORMAL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_OPPOSITE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_RIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$text$Layout$Alignment[Alignment.ALIGN_LEFT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public float getLineRight(int line) {
        Alignment resultAlign;
        int dir = getParagraphDirection(line);
        Alignment align = getParagraphAlignment(line);
        if (align == null) {
            align = Alignment.ALIGN_CENTER;
        }
        int i = AnonymousClass1.$SwitchMap$android$text$Layout$Alignment[align.ordinal()];
        if (i != 1) {
            resultAlign = i != 2 ? i != 3 ? i != 4 ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT : Alignment.ALIGN_CENTER : dir == -1 ? Alignment.ALIGN_LEFT : Alignment.ALIGN_RIGHT;
        } else {
            resultAlign = dir == -1 ? Alignment.ALIGN_RIGHT : Alignment.ALIGN_LEFT;
        }
        int i2 = AnonymousClass1.$SwitchMap$android$text$Layout$Alignment[resultAlign.ordinal()];
        if (i2 == 3) {
            return (float) Math.ceil((double) (((float) getParagraphRight(line)) - ((((float) this.mWidth) - getLineMax(line)) / 2.0f)));
        } else if (i2 != 4) {
            return getLineMax(line);
        } else {
            return (float) this.mWidth;
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

    /* JADX WARNING: Removed duplicated region for block: B:14:0x003d A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003f  */
    private float getLineExtent(int line, boolean full) {
        TabStops tabStops;
        Directions directions;
        int start = getLineStart(line);
        int end = full ? getLineEnd(line) : getLineVisibleEnd(line);
        boolean hasTabs = getLineContainsTab(line);
        if (hasTabs) {
            CharSequence charSequence = this.mText;
            if (charSequence instanceof Spanned) {
                TabStopSpan[] tabs = (TabStopSpan[]) getParagraphSpans((Spanned) charSequence, start, end, TabStopSpan.class);
                if (tabs.length > 0) {
                    tabStops = new TabStops(TAB_INCREMENT, tabs);
                    directions = getLineDirections(line);
                    if (directions != null) {
                        return 0.0f;
                    }
                    int dir = getParagraphDirection(line);
                    TextLine tl = TextLine.obtain();
                    TextPaint paint = this.mWorkPaint;
                    paint.set(this.mPaint);
                    paint.setStartHyphenEdit(getStartHyphenEdit(line));
                    paint.setEndHyphenEdit(getEndHyphenEdit(line));
                    tl.set(paint, this.mText, start, end, dir, directions, hasTabs, tabStops, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
                    if (isJustificationRequired(line)) {
                        tl.justify(getJustifyWidth(line));
                    }
                    float width = tl.metrics(null);
                    TextLine.recycle(tl);
                    return width;
                }
            }
        }
        tabStops = null;
        directions = getLineDirections(line);
        if (directions != null) {
        }
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
        paint.setStartHyphenEdit(getStartHyphenEdit(line));
        paint.setEndHyphenEdit(getEndHyphenEdit(line));
        tl.set(paint, this.mText, start, end, dir, directions, hasTabs, tabStops, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
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
        Layout layout = this;
        int lineEndOffset = getLineEnd(line);
        int lineStartOffset = getLineStart(line);
        Directions dirs = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        Directions dirs2 = dirs;
        tl.set(layout.mPaint, layout.mText, lineStartOffset, lineEndOffset, getParagraphDirection(line), dirs, false, null, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        HorizontalMeasurementProvider horizontal = new HorizontalMeasurementProvider(line, primary);
        int i = true;
        if (line == getLineCount() - 1) {
            max = lineEndOffset;
        } else {
            max = tl.getOffsetToLeftRightOf(lineEndOffset - lineStartOffset, !layout.isRtlCharAt(lineEndOffset - 1)) + lineStartOffset;
        }
        int best = lineStartOffset;
        float bestdist = Math.abs(horizontal.get(lineStartOffset) - horiz);
        int i2 = 0;
        while (i2 < dirs2.mDirections.length) {
            int here = dirs2.mDirections[i2] + lineStartOffset;
            int there = (dirs2.mDirections[i2 + 1] & RUN_LENGTH_MASK) + here;
            boolean isRtl = (dirs2.mDirections[i2 + 1] & 67108864) != 0 ? i : false;
            int swap = isRtl ? -1 : i;
            if (there > max) {
                there = max;
            }
            int high = (there - 1) + 1;
            int low = (here + 1) - 1;
            while (high - low > 1) {
                int guess = (high + low) / 2;
                if (horizontal.get(layout.getOffsetAtStartOf(guess)) * ((float) swap) >= ((float) swap) * horiz) {
                    high = guess;
                } else {
                    low = guess;
                }
                swap = swap;
                layout = this;
            }
            if (low < here + 1) {
                low = here + 1;
            }
            if (low < there) {
                int aft = tl.getOffsetToLeftRightOf(low - lineStartOffset, isRtl) + lineStartOffset;
                int low2 = tl.getOffsetToLeftRightOf(aft - lineStartOffset, !isRtl) + lineStartOffset;
                if (low2 >= here && low2 < there) {
                    float dist = Math.abs(horizontal.get(low2) - horiz);
                    if (aft < there) {
                        float other = Math.abs(horizontal.get(aft) - horiz);
                        if (other < dist) {
                            dist = other;
                            low2 = aft;
                        }
                    }
                    if (dist < bestdist) {
                        bestdist = dist;
                        best = low2;
                    }
                }
            }
            float dist2 = Math.abs(horizontal.get(here) - horiz);
            if (dist2 < bestdist) {
                bestdist = dist2;
                best = here;
            }
            i2 += 2;
            layout = this;
            dirs2 = dirs2;
            i = true;
        }
        if (Math.abs(horizontal.get(max) - horiz) <= bestdist) {
            best = max;
        }
        TextLine.recycle(tl);
        return best;
    }

    /* access modifiers changed from: private */
    public class HorizontalMeasurementProvider {
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
            int index = offset - this.mLineStartOffset;
            float[] fArr = this.mHorizontals;
            if (fArr == null || index < 0 || index >= fArr.length) {
                return Layout.this.getHorizontal(offset, this.mPrimary);
            }
            return fArr[index];
        }
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
            if (ch == '\n') {
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
                toLeft2 = !toLeft2;
                lineDir = newDir;
            }
        }
        Directions directions = getLineDirections(line);
        TextLine tl = TextLine.obtain();
        tl.set(this.mPaint, this.mText, lineStart, lineEnd, lineDir, directions, false, null, getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        int caret2 = tl.getOffsetToLeftRightOf(caret - lineStart, toLeft2) + lineStart;
        TextLine.recycle(tl);
        return caret2;
    }

    private int getOffsetAtStartOf(int offset) {
        char c1;
        if (offset == 0) {
            return 0;
        }
        CharSequence text = this.mText;
        char c = text.charAt(offset);
        if (c >= 56320 && c <= 57343 && (c1 = text.charAt(offset - 1)) >= 55296 && c1 <= 56319) {
            offset--;
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

    @UnsupportedAppUsage
    public boolean shouldClampCursor(int line) {
        int i = AnonymousClass1.$SwitchMap$android$text$Layout$Alignment[getParagraphAlignment(line).ordinal()];
        return i != 1 ? i == 5 : getParagraphDirection(line) > 0;
    }

    public void getCursorPath(int point, Path dest, CharSequence editingBuffer) {
        dest.reset();
        int line = getLineForOffset(point);
        int top = getLineTop(line);
        int bottom = getLineBottomWithoutSpacing(line);
        float h1 = getPrimaryHorizontal(point, shouldClampCursor(line)) - 0.5f;
        int caps = TextKeyListener.getMetaState(editingBuffer, 1) | TextKeyListener.getMetaState(editingBuffer, 2048);
        int fn = TextKeyListener.getMetaState(editingBuffer, 2);
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
        dest.moveTo(h1, (float) top);
        dest.lineTo(h1, (float) bottom);
        if (caps == 2) {
            dest.moveTo(h1, (float) bottom);
            dest.lineTo(h1 - ((float) dist), (float) (bottom + dist));
            dest.lineTo(h1, (float) bottom);
            dest.lineTo(((float) dist) + h1, (float) (bottom + dist));
        } else if (caps == 1) {
            dest.moveTo(h1, (float) bottom);
            dest.lineTo(h1 - ((float) dist), (float) (bottom + dist));
            dest.moveTo(h1 - ((float) dist), ((float) (bottom + dist)) - 0.5f);
            dest.lineTo(((float) dist) + h1, ((float) (bottom + dist)) - 0.5f);
            dest.moveTo(((float) dist) + h1, (float) (bottom + dist));
            dest.lineTo(h1, (float) bottom);
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

    private void addSelection(int line, int start, int end, int top, int bottom, SelectionRectangleConsumer consumer) {
        int layout;
        Layout layout2 = this;
        int i = line;
        int linestart = getLineStart(line);
        int lineend = getLineEnd(line);
        Directions dirs = getLineDirections(line);
        if (lineend > linestart && layout2.mText.charAt(lineend - 1) == '\n') {
            lineend--;
        }
        int i2 = 0;
        while (i2 < dirs.mDirections.length) {
            int here = dirs.mDirections[i2] + linestart;
            int there = (dirs.mDirections[i2 + 1] & RUN_LENGTH_MASK) + here;
            if (there > lineend) {
                there = lineend;
            }
            if (start <= there && end >= here) {
                int st = Math.max(start, here);
                int en = Math.min(end, there);
                if (st != en) {
                    float h1 = layout2.getHorizontal(st, false, i, false);
                    float h2 = layout2.getHorizontal(en, true, i, false);
                    float left = Math.min(h1, h2);
                    float right = Math.max(h1, h2);
                    if ((dirs.mDirections[i2 + 1] & 67108864) != 0) {
                        layout = 0;
                    } else {
                        layout = 1;
                    }
                    consumer.accept(left, (float) top, right, (float) bottom, layout);
                }
            }
            i2 += 2;
            layout2 = this;
            i = line;
        }
    }

    public void getSelectionPath(int start, int end, Path dest) {
        dest.reset();
        getSelection(start, end, new SelectionRectangleConsumer() {
            /* class android.text.$$Lambda$Layout$MzjK2UE2G8VG0asK8_KWY3gHAmY */

            @Override // android.text.Layout.SelectionRectangleConsumer
            public final void accept(float f, float f2, float f3, float f4, int i) {
                Path.this.addRect(f, f2, f3, f4, Path.Direction.CW);
            }
        });
    }

    public final void getSelection(int start, int end, SelectionRectangleConsumer consumer) {
        int end2;
        int start2;
        int start3;
        int bottom;
        if (start != end) {
            if (end < start) {
                start2 = end;
                end2 = start;
            } else {
                start2 = start;
                end2 = end;
            }
            int startline = getLineForOffset(start2);
            int endline = getLineForOffset(end2);
            int top = getLineTop(startline);
            int bottom2 = getLineBottomWithoutSpacing(endline);
            if (startline == endline) {
                addSelection(startline, start2, end2, top, bottom2, consumer);
                return;
            }
            float width = (float) this.mWidth;
            boolean isTwoLineEnd = true;
            boolean isLineEnd = getLineForOffset(end2 + -1) + 1 == endline;
            if (!isLineEnd || startline + 1 != endline) {
                isTwoLineEnd = false;
            }
            int bottom3 = isTwoLineEnd ? getLineBottomWithoutSpacing(startline) : getLineBottom(startline);
            addSelection(startline, start2, getLineEnd(startline), top, bottom3, consumer);
            if (getParagraphDirection(startline) == -1) {
                consumer.accept(getLineLeft(startline), (float) top, 0.0f, (float) bottom3, 0);
                start3 = -1;
            } else {
                start3 = -1;
                consumer.accept(getLineRight(startline), (float) top, width, (float) bottom3, 1);
            }
            for (int i = startline + 1; i < endline; i++) {
                int top2 = getLineTop(i);
                int bottom4 = getLineBottom(i);
                if (!isLineEnd || i + 1 != endline) {
                    bottom = bottom4;
                } else {
                    bottom = getLineBottomWithoutSpacing(i);
                }
                if (getParagraphDirection(i) == start3) {
                    consumer.accept(0.0f, (float) top2, width, (float) bottom, 0);
                } else {
                    consumer.accept(0.0f, (float) top2, width, (float) bottom, 1);
                }
            }
            int top3 = getLineTop(endline);
            int bottom5 = getLineBottomWithoutSpacing(endline);
            addSelection(endline, getLineStart(endline), end2, top3, bottom5, consumer);
            if (getParagraphDirection(endline) == start3) {
                consumer.accept(width, (float) top3, getLineRight(endline), (float) bottom5, 0);
            } else {
                consumer.accept(0.0f, (float) top3, getLineLeft(endline), (float) bottom5, 1);
            }
        }
    }

    public final Alignment getParagraphAlignment(int line) {
        AlignmentSpan[] spans;
        int spanLength;
        Alignment align = this.mAlignment;
        if (!this.mSpannedText || (spanLength = (spans = (AlignmentSpan[]) getParagraphSpans((Spanned) this.mText, getLineStart(line), getLineEnd(line), AlignmentSpan.class)).length) <= 0) {
            return align;
        }
        return spans[spanLength - 1].getAlignment();
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
        boolean useFirstLineMargin = lineStart == 0 || spanned.charAt(lineStart + -1) == '\n';
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

    /* JADX WARNING: Removed duplicated region for block: B:43:0x00e6  */
    private static float measurePara(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        MeasuredParagraph mt;
        Throwable th;
        boolean hasTabs;
        int margin;
        TabStops tabStops;
        boolean hasTabs2;
        TextLine tl = TextLine.obtain();
        try {
            mt = MeasuredParagraph.buildForBidi(text, start, end, textDir, null);
            try {
                char[] chars = mt.getChars();
                int len = chars.length;
                Directions directions = mt.getDirections(0, len);
                int dir = mt.getParagraphDir();
                boolean hasTabs3 = false;
                if (text instanceof Spanned) {
                    try {
                        LeadingMarginSpan[] spans = (LeadingMarginSpan[]) getParagraphSpans((Spanned) text, start, end, LeadingMarginSpan.class);
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
                    } catch (Throwable th2) {
                        th = th2;
                        TextLine.recycle(tl);
                        if (mt != null) {
                            mt.recycle();
                        }
                        throw th;
                    }
                } else {
                    hasTabs = false;
                    margin = 0;
                }
                int i = 0;
                while (true) {
                    if (i >= len) {
                        tabStops = null;
                        hasTabs2 = hasTabs;
                        break;
                    } else if (chars[i] != '\t') {
                        i++;
                    } else if (text instanceof Spanned) {
                        Spanned spanned = (Spanned) text;
                        TabStopSpan[] spans2 = (TabStopSpan[]) getParagraphSpans(spanned, start, spanned.nextSpanTransition(start, end, TabStopSpan.class), TabStopSpan.class);
                        hasTabs2 = true;
                        if (spans2.length > 0) {
                            tabStops = new TabStops(TAB_INCREMENT, spans2);
                        } else {
                            tabStops = null;
                        }
                    } else {
                        hasTabs2 = true;
                        tabStops = null;
                    }
                }
                try {
                    tl.set(paint, text, start, end, dir, directions, hasTabs2, tabStops, 0, 0);
                    float abs = ((float) margin) + Math.abs(tl.metrics(null));
                    TextLine.recycle(tl);
                    mt.recycle();
                    return abs;
                } catch (Throwable th3) {
                    th = th3;
                    mt = mt;
                    TextLine.recycle(tl);
                    if (mt != null) {
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                TextLine.recycle(tl);
                if (mt != null) {
                }
                throw th;
            }
        } catch (Throwable th5) {
            th = th5;
            mt = null;
            TextLine.recycle(tl);
            if (mt != null) {
            }
            throw th;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static class TabStops {
        private float mIncrement;
        private int mNumStops;
        private float[] mStops;

        public TabStops(float increment, Object[] spans) {
            reset(increment, spans);
        }

        /* JADX INFO: Multiple debug info for r6v1 int: [D('nstops' float[]), D('ns' int)] */
        /* access modifiers changed from: package-private */
        public void reset(float increment, Object[] spans) {
            this.mIncrement = increment;
            int ns = 0;
            if (spans != null) {
                float[] stops = this.mStops;
                int ns2 = 0;
                for (Object o : spans) {
                    if (o instanceof TabStopSpan) {
                        if (stops == null) {
                            stops = new float[10];
                        } else if (ns2 == stops.length) {
                            float[] nstops = new float[(ns2 * 2)];
                            for (int i = 0; i < ns2; i++) {
                                nstops[i] = stops[i];
                            }
                            stops = nstops;
                        }
                        stops[ns2] = (float) ((TabStopSpan) o).getTabStop();
                        ns2++;
                    }
                }
                if (ns2 > 1) {
                    Arrays.sort(stops, 0, ns2);
                }
                if (stops != this.mStops) {
                    this.mStops = stops;
                }
                ns = ns2;
            }
            this.mNumStops = ns;
        }

        /* access modifiers changed from: package-private */
        public float nextTab(float h) {
            int ns = this.mNumStops;
            if (ns > 0) {
                float[] stops = this.mStops;
                for (int i = 0; i < ns; i++) {
                    float stop = stops[i];
                    if (stop > h) {
                        return stop;
                    }
                }
            }
            return nextDefaultStop(h, this.mIncrement);
        }

        public static float nextDefaultStop(float h, float inc) {
            return ((float) ((int) ((h + inc) / inc))) * inc;
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
        return ((float) ((int) ((h + TAB_INCREMENT) / TAB_INCREMENT))) * TAB_INCREMENT;
    }

    /* access modifiers changed from: protected */
    public final boolean isSpanned() {
        return this.mSpannedText;
    }

    static <T> T[] getParagraphSpans(Spanned text, int start, int end, Class<T> type) {
        return (start != end || start <= 0) ? text instanceof SpannableStringBuilder ? (T[]) ((SpannableStringBuilder) text).getSpans(start, end, type, false) : (T[]) text.getSpans(start, end, type) : (T[]) ArrayUtils.emptyArray(type);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void ellipsize(int start, int end, int line, char[] dest, int destoff, TextUtils.TruncateAt method) {
        char c;
        int ellipsisCount = getEllipsisCount(line);
        if (ellipsisCount != 0) {
            int ellipsisStart = getEllipsisStart(line);
            int lineStart = getLineStart(line);
            String ellipsisString = TextUtils.getEllipsisString(method);
            int ellipsisStringLen = ellipsisString.length();
            boolean useEllipsisString = ellipsisCount >= ellipsisStringLen;
            for (int i = 0; i < ellipsisCount; i++) {
                if (!useEllipsisString || i >= ellipsisStringLen) {
                    c = 65279;
                } else {
                    c = ellipsisString.charAt(i);
                }
                int a = i + ellipsisStart + lineStart;
                if (start <= a) {
                    if (a < end) {
                        dest[(destoff + a) - start] = c;
                    }
                }
            }
        }
    }

    public static class Directions {
        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public int[] mDirections;

        @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
        public Directions(int[] dirs) {
            this.mDirections = dirs;
        }

        public int getRunCount() {
            return this.mDirections.length / 2;
        }

        public int getRunStart(int runIndex) {
            return this.mDirections[runIndex * 2];
        }

        public int getRunLength(int runIndex) {
            return this.mDirections[(runIndex * 2) + 1] & Layout.RUN_LENGTH_MASK;
        }

        public boolean isRunRtl(int runIndex) {
            return (this.mDirections[(runIndex * 2) + 1] & 67108864) != 0;
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

        @Override // java.lang.CharSequence
        public char charAt(int off) {
            char[] buf = TextUtils.obtain(1);
            getChars(off, off + 1, buf, 0);
            char ret = buf[0];
            TextUtils.recycle(buf);
            return ret;
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] dest, int destoff) {
            int line1 = this.mLayout.getLineForOffset(start);
            int line2 = this.mLayout.getLineForOffset(end);
            TextUtils.getChars(this.mText, start, end, dest, destoff);
            for (int i = line1; i <= line2; i++) {
                this.mLayout.ellipsize(start, end, i, dest, destoff, this.mMethod);
            }
        }

        @Override // java.lang.CharSequence
        public int length() {
            return this.mText.length();
        }

        @Override // java.lang.CharSequence
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, 0);
            return new String(s);
        }

        @Override // java.lang.CharSequence, java.lang.Object
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

        @Override // android.text.Spanned
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return (T[]) this.mSpanned.getSpans(start, end, type);
        }

        @Override // android.text.Spanned
        public int getSpanStart(Object tag) {
            return this.mSpanned.getSpanStart(tag);
        }

        @Override // android.text.Spanned
        public int getSpanEnd(Object tag) {
            return this.mSpanned.getSpanEnd(tag);
        }

        @Override // android.text.Spanned
        public int getSpanFlags(Object tag) {
            return this.mSpanned.getSpanFlags(tag);
        }

        @Override // android.text.Spanned
        public int nextSpanTransition(int start, int limit, Class type) {
            return this.mSpanned.nextSpanTransition(start, limit, type);
        }

        @Override // android.text.Layout.Ellipsizer, java.lang.CharSequence
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[(end - start)];
            getChars(start, end, s, 0);
            SpannableString ss = new SpannableString(new String(s));
            TextUtils.copySpansFrom(this.mSpanned, start, end, Object.class, ss, 0);
            return ss;
        }
    }
}

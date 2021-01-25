package android.text;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import java.util.ArrayList;

@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public class TextLine {
    private static final boolean DEBUG = false;
    private static final char TAB_CHAR = '\t';
    private static final int TAB_INCREMENT = 20;
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static final TextLine[] sCached = new TextLine[3];
    private final TextPaint mActivePaint = new TextPaint();
    private float mAddedWidthForJustify;
    @UnsupportedAppUsage
    private final SpanSet<CharacterStyle> mCharacterStyleSpanSet = new SpanSet<>(CharacterStyle.class);
    private char[] mChars;
    private boolean mCharsValid;
    private PrecomputedText mComputed;
    private final DecorationInfo mDecorationInfo = new DecorationInfo();
    private final ArrayList<DecorationInfo> mDecorations = new ArrayList<>();
    private int mDir;
    private Layout.Directions mDirections;
    private int mEllipsisEnd;
    private int mEllipsisStart;
    private boolean mHasTabs;
    private boolean mIsJustifying;
    private int mLen;
    @UnsupportedAppUsage
    private final SpanSet<MetricAffectingSpan> mMetricAffectingSpanSpanSet = new SpanSet<>(MetricAffectingSpan.class);
    private TextPaint mPaint;
    @UnsupportedAppUsage
    private final SpanSet<ReplacementSpan> mReplacementSpanSpanSet = new SpanSet<>(ReplacementSpan.class);
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private Spanned mSpanned;
    private int mStart;
    private Layout.TabStops mTabs;
    @UnsupportedAppUsage
    private CharSequence mText;
    private final TextPaint mWorkPaint = new TextPaint();

    @UnsupportedAppUsage
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static TextLine obtain() {
        synchronized (sCached) {
            int i = sCached.length;
            do {
                i--;
                if (i < 0) {
                    return new TextLine();
                }
            } while (sCached[i] == null);
            TextLine tl = sCached[i];
            sCached[i] = null;
            return tl;
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public static TextLine recycle(TextLine tl) {
        tl.mText = null;
        tl.mPaint = null;
        tl.mDirections = null;
        tl.mSpanned = null;
        tl.mTabs = null;
        tl.mChars = null;
        tl.mComputed = null;
        tl.mMetricAffectingSpanSpanSet.recycle();
        tl.mCharacterStyleSpanSet.recycle();
        tl.mReplacementSpanSpanSet.recycle();
        synchronized (sCached) {
            int i = 0;
            while (true) {
                if (i >= sCached.length) {
                    break;
                } else if (sCached[i] == null) {
                    sCached[i] = tl;
                    break;
                } else {
                    i++;
                }
            }
        }
        return null;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void set(TextPaint paint, CharSequence text, int start, int limit, int dir, Layout.Directions directions, boolean hasTabs, Layout.TabStops tabStops, int ellipsisStart, int ellipsisEnd) {
        this.mPaint = paint;
        this.mText = text;
        this.mStart = start;
        this.mLen = limit - start;
        this.mDir = dir;
        this.mDirections = directions;
        if (this.mDirections != null) {
            this.mHasTabs = hasTabs;
            this.mSpanned = null;
            boolean hasReplacement = false;
            int i = 1;
            if (text instanceof Spanned) {
                this.mSpanned = (Spanned) text;
                this.mReplacementSpanSpanSet.init(this.mSpanned, start, limit);
                hasReplacement = this.mReplacementSpanSpanSet.numberOfSpans > 0;
            }
            this.mComputed = null;
            if (text instanceof PrecomputedText) {
                this.mComputed = (PrecomputedText) text;
                if (!this.mComputed.getParams().getTextPaint().equalsForTextMeasurement(paint)) {
                    this.mComputed = null;
                }
            }
            this.mCharsValid = hasReplacement;
            if (this.mCharsValid) {
                char[] cArr = this.mChars;
                if (cArr == null || cArr.length < this.mLen) {
                    this.mChars = ArrayUtils.newUnpaddedCharArray(this.mLen);
                }
                TextUtils.getChars(text, start, limit, this.mChars, 0);
                if (hasReplacement) {
                    char[] chars = this.mChars;
                    int i2 = start;
                    while (i2 < limit) {
                        int inext = this.mReplacementSpanSpanSet.getNextTransition(i2, limit);
                        if (this.mReplacementSpanSpanSet.hasSpansIntersecting(i2, inext) && (i2 - start >= ellipsisEnd || inext - start <= ellipsisStart)) {
                            chars[i2 - start] = 65532;
                            int e = inext - start;
                            for (int j = (i2 - start) + i; j < e; j++) {
                                chars[j] = 65279;
                            }
                        }
                        i2 = inext;
                        i = 1;
                    }
                }
            }
            this.mTabs = tabStops;
            this.mAddedWidthForJustify = 0.0f;
            int i3 = 0;
            this.mIsJustifying = false;
            this.mEllipsisStart = ellipsisStart != ellipsisEnd ? ellipsisStart : 0;
            if (ellipsisStart != ellipsisEnd) {
                i3 = ellipsisEnd;
            }
            this.mEllipsisEnd = i3;
            return;
        }
        throw new IllegalArgumentException("Directions cannot be null");
    }

    private char charAt(int i) {
        return this.mCharsValid ? this.mChars[i] : this.mText.charAt(this.mStart + i);
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void justify(float justifyWidth) {
        int end = this.mLen;
        while (end > 0 && isLineEndSpace(this.mText.charAt((this.mStart + end) - 1))) {
            end--;
        }
        int spaces = countStretchableSpaces(0, end);
        if (spaces != 0) {
            this.mAddedWidthForJustify = (justifyWidth - Math.abs(measure(end, false, null))) / ((float) spaces);
            this.mIsJustifying = true;
        }
    }

    /* JADX INFO: Multiple debug info for r11v7 'j'  int: [D('runCount' int), D('j' int)] */
    /* access modifiers changed from: package-private */
    public void draw(Canvas c, float x, int top, int y, int bottom) {
        int runCount;
        int runCount2;
        float h = 0.0f;
        int j = this.mDirections.getRunCount();
        int runIndex = 0;
        while (runIndex < j) {
            int runStart = this.mDirections.getRunStart(runIndex);
            if (runStart <= this.mLen) {
                int runLimit = Math.min(this.mDirections.getRunLength(runIndex) + runStart, this.mLen);
                boolean runIsRtl = this.mDirections.isRunRtl(runIndex);
                float h2 = h;
                int segStart = runStart;
                int j2 = this.mHasTabs ? runStart : runLimit;
                while (j2 <= runLimit) {
                    if (j2 == runLimit || charAt(j2) == '\t') {
                        float f = x + h2;
                        boolean z = (runIndex == j + -1 && j2 == this.mLen) ? false : true;
                        runCount = j;
                        runCount2 = j2;
                        h2 += drawRun(c, segStart, j2, runIsRtl, f, top, y, bottom, z);
                        if (runCount2 != runLimit) {
                            int i = this.mDir;
                            h2 = ((float) i) * nextTab(((float) i) * h2);
                        }
                        segStart = runCount2 + 1;
                    } else {
                        runCount = j;
                        runCount2 = j2;
                    }
                    j2 = runCount2 + 1;
                    j = runCount;
                }
                runIndex++;
                h = h2;
                j = j;
            } else {
                return;
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public float metrics(Paint.FontMetricsInt fmi) {
        return measure(this.mLen, false, fmi);
    }

    public float measure(int offset, boolean trailing, Paint.FontMetricsInt fmi) {
        int runStart;
        if (offset <= this.mLen) {
            int target = trailing ? offset - 1 : offset;
            if (target < 0) {
                return 0.0f;
            }
            float h = 0.0f;
            int runIndex = 0;
            while (runIndex < this.mDirections.getRunCount() && (runStart = this.mDirections.getRunStart(runIndex)) <= this.mLen) {
                int runLimit = Math.min(this.mDirections.getRunLength(runIndex) + runStart, this.mLen);
                boolean runIsRtl = this.mDirections.isRunRtl(runIndex);
                float h2 = h;
                int segStart = runStart;
                int j = this.mHasTabs ? runStart : runLimit;
                while (j <= runLimit) {
                    if (j == runLimit || charAt(j) == '\t') {
                        boolean sameDirection = false;
                        boolean targetIsInThisSegment = target >= segStart && target < j;
                        if ((this.mDir == -1) == runIsRtl) {
                            sameDirection = true;
                        }
                        if (targetIsInThisSegment && sameDirection) {
                            return measureRun(segStart, offset, j, runIsRtl, fmi) + h2;
                        }
                        float segmentWidth = measureRun(segStart, j, j, runIsRtl, fmi);
                        h2 += sameDirection ? segmentWidth : -segmentWidth;
                        if (targetIsInThisSegment) {
                            return measureRun(segStart, offset, j, runIsRtl, null) + h2;
                        }
                        if (j != runLimit) {
                            if (offset == j) {
                                return h2;
                            }
                            int i = this.mDir;
                            float h3 = ((float) i) * nextTab(((float) i) * h2);
                            if (target == j) {
                                return h3;
                            }
                            h2 = h3;
                        }
                        segStart = j + 1;
                    }
                    j++;
                }
                runIndex++;
                h = h2;
            }
            return h;
        }
        throw new IndexOutOfBoundsException("offset(" + offset + ") should be less than line limit(" + this.mLen + ")");
    }

    @VisibleForTesting
    public float[] measureAllOffsets(boolean[] trailing, Paint.FontMetricsInt fmi) {
        int runStart;
        float w;
        int offset;
        int i = this.mLen;
        float[] measurement = new float[(i + 1)];
        int[] target = new int[(i + 1)];
        for (int offset2 = 0; offset2 < target.length; offset2++) {
            target[offset2] = trailing[offset2] ? offset2 - 1 : offset2;
        }
        if (target[0] < 0) {
            measurement[0] = 0.0f;
        }
        float h = 0.0f;
        int runIndex = 0;
        while (runIndex < this.mDirections.getRunCount() && (runStart = this.mDirections.getRunStart(runIndex)) <= this.mLen) {
            int runLimit = Math.min(this.mDirections.getRunLength(runIndex) + runStart, this.mLen);
            boolean runIsRtl = this.mDirections.isRunRtl(runIndex);
            float h2 = h;
            int segStart = runStart;
            for (int j = this.mHasTabs ? runStart : runLimit; j <= runLimit; j++) {
                if (j == runLimit || charAt(j) == '\t') {
                    boolean advance = (this.mDir == -1) == runIsRtl;
                    int segStart2 = segStart;
                    float w2 = measureRun(segStart, j, j, runIsRtl, fmi);
                    h2 += advance ? w2 : -w2;
                    float baseh = advance ? h2 : h2;
                    Paint.FontMetricsInt crtfmi = advance ? fmi : null;
                    int offset3 = segStart2;
                    while (offset3 <= j && offset3 <= this.mLen) {
                        if (target[offset3] < segStart2 || target[offset3] >= j) {
                            segStart2 = segStart2;
                            offset = offset3;
                            w = w2;
                        } else {
                            segStart2 = segStart2;
                            offset = offset3;
                            w = w2;
                            measurement[offset] = baseh + measureRun(segStart2, offset3, j, runIsRtl, crtfmi);
                        }
                        offset3 = offset + 1;
                        w2 = w;
                    }
                    if (j != runLimit) {
                        if (target[j] == j) {
                            measurement[j] = h2;
                        }
                        int i2 = this.mDir;
                        float h3 = ((float) i2) * nextTab(((float) i2) * h2);
                        if (target[j + 1] == j) {
                            measurement[j + 1] = h3;
                        }
                        h2 = h3;
                    }
                    segStart = j + 1;
                }
            }
            runIndex++;
            h = h2;
        }
        int i3 = this.mLen;
        if (target[i3] == i3) {
            measurement[i3] = h;
        }
        return measurement;
    }

    private float drawRun(Canvas c, int start, int limit, boolean runIsRtl, float x, int top, int y, int bottom, boolean needWidth) {
        boolean z = true;
        if (this.mDir != 1) {
            z = false;
        }
        if (z != runIsRtl) {
            return handleRun(start, limit, limit, runIsRtl, c, x, top, y, bottom, null, needWidth);
        }
        float w = -measureRun(start, limit, limit, runIsRtl, null);
        handleRun(start, limit, limit, runIsRtl, c, x + w, top, y, bottom, null, false);
        return w;
    }

    private float measureRun(int start, int offset, int limit, boolean runIsRtl, Paint.FontMetricsInt fmi) {
        return handleRun(start, offset, limit, runIsRtl, null, 0.0f, 0, 0, 0, fmi, true);
    }

    /* JADX INFO: Multiple debug info for r1v41 int: [D('runStart' int), D('prevRunLevel' int)] */
    /* access modifiers changed from: package-private */
    public int getOffsetToLeftRightOf(int cursor, boolean toLeft) {
        int runLimit;
        int runLevel;
        int runIndex;
        int[] runs;
        boolean advance;
        int otherRunLimit;
        int prevRunIndex;
        int runStart;
        int runLimit2;
        boolean trailing;
        int runStart2;
        int runStart3;
        int lineEnd = this.mLen;
        boolean paraIsRtl = this.mDir == -1;
        int[] runs2 = this.mDirections.mDirections;
        int runStart4 = 0;
        int runLimit3 = lineEnd;
        boolean trailing2 = false;
        if (cursor == 0) {
            runLimit = 0;
            runIndex = -2;
            runs = runs2;
            runLevel = -1;
        } else if (cursor == lineEnd) {
            runLimit = 0;
            runIndex = runs2.length;
            runs = runs2;
            runLevel = -1;
        } else {
            int runIndex2 = 0;
            while (true) {
                if (runIndex2 >= runs2.length) {
                    prevRunIndex = 0;
                    runStart = runStart4;
                    runLimit2 = runLimit3;
                    trailing = false;
                    runIndex = runIndex2;
                    break;
                }
                int runStart5 = 0 + runs2[runIndex2];
                if (cursor >= runStart5) {
                    int runLimit4 = (runs2[runIndex2 + 1] & 67108863) + runStart5;
                    if (runLimit4 > lineEnd) {
                        runLimit4 = lineEnd;
                    }
                    if (cursor < runLimit4) {
                        int runLevel2 = (runs2[runIndex2 + 1] >>> 26) & 63;
                        if (cursor == runStart5) {
                            int pos = cursor - 1;
                            int prevRunIndex2 = 0;
                            while (true) {
                                if (prevRunIndex2 >= runs2.length) {
                                    runStart = runStart5;
                                    break;
                                }
                                int prevRunStart = runs2[prevRunIndex2] + 0;
                                if (pos >= prevRunStart) {
                                    int prevRunLimit = prevRunStart + (runs2[prevRunIndex2 + 1] & 67108863);
                                    if (prevRunLimit > lineEnd) {
                                        prevRunLimit = lineEnd;
                                    }
                                    if (pos < prevRunLimit) {
                                        runStart3 = runStart5;
                                        int prevRunLevel = (runs2[prevRunIndex2 + 1] >>> 26) & 63;
                                        if (prevRunLevel < runLevel2) {
                                            runIndex2 = prevRunIndex2;
                                            runLevel2 = prevRunLevel;
                                            runStart = prevRunStart;
                                            runLimit4 = prevRunLimit;
                                            trailing2 = true;
                                            break;
                                        }
                                    } else {
                                        runStart3 = runStart5;
                                    }
                                } else {
                                    runStart3 = runStart5;
                                }
                                prevRunIndex2 += 2;
                                runStart5 = runStart3;
                            }
                            prevRunIndex = runLevel2;
                            runIndex = runIndex2;
                            runLimit2 = runLimit4;
                            trailing = trailing2;
                        } else {
                            prevRunIndex = runLevel2;
                            runIndex = runIndex2;
                            runLimit2 = runLimit4;
                            runStart = runStart5;
                            trailing = false;
                        }
                    } else {
                        runStart2 = runStart5;
                        runLimit3 = runLimit4;
                    }
                } else {
                    runStart2 = runStart5;
                }
                runIndex2 += 2;
                runStart4 = runStart2;
            }
            if (runIndex != runs2.length) {
                boolean runIsRtl = (prevRunIndex & 1) != 0;
                boolean advance2 = toLeft == runIsRtl;
                if (cursor == (advance2 ? runLimit2 : runStart) && advance2 == trailing) {
                    runs = runs2;
                } else {
                    runs = runs2;
                    int newCaret = getOffsetBeforeAfter(runIndex, runStart, runLimit2, runIsRtl, cursor, advance2);
                    if (newCaret != (advance2 ? runLimit2 : runStart)) {
                        return newCaret;
                    }
                    runLimit = prevRunIndex;
                    runLevel = newCaret;
                }
            } else {
                runs = runs2;
            }
            runLimit = prevRunIndex;
            runLevel = -1;
        }
        while (true) {
            advance = toLeft == paraIsRtl;
            int otherRunIndex = runIndex + (advance ? 2 : -2);
            if (otherRunIndex < 0 || otherRunIndex >= runs.length) {
                break;
            }
            int otherRunStart = 0 + runs[otherRunIndex];
            int otherRunLimit2 = otherRunStart + (runs[otherRunIndex + 1] & 67108863);
            if (otherRunLimit2 > lineEnd) {
                otherRunLimit = lineEnd;
            } else {
                otherRunLimit = otherRunLimit2;
            }
            int otherRunLevel = (runs[otherRunIndex + 1] >>> 26) & 63;
            boolean otherRunIsRtl = (otherRunLevel & 1) != 0;
            boolean advance3 = toLeft == otherRunIsRtl;
            if (runLevel == -1) {
                runLevel = getOffsetBeforeAfter(otherRunIndex, otherRunStart, otherRunLimit, otherRunIsRtl, advance3 ? otherRunStart : otherRunLimit, advance3);
                if (runLevel != (advance3 ? otherRunLimit : otherRunStart)) {
                    return runLevel;
                }
                runIndex = otherRunIndex;
                runLimit = otherRunLevel;
            } else if (otherRunLevel >= runLimit) {
                return runLevel;
            } else {
                return advance3 ? otherRunStart : otherRunLimit;
            }
        }
        int newCaret2 = -1;
        if (runLevel == -1) {
            if (advance) {
                newCaret2 = this.mLen + 1;
            }
            return newCaret2;
        } else if (runLevel > lineEnd) {
            return runLevel;
        } else {
            return advance ? lineEnd : 0;
        }
    }

    private int getOffsetBeforeAfter(int runIndex, int runStart, int runLimit, boolean runIsRtl, int offset, boolean after) {
        int spanLimit;
        int spanStart;
        int i;
        int spanLimit2;
        if (runIndex >= 0) {
            int cursorOpt = 0;
            if (offset != (after ? this.mLen : 0)) {
                TextPaint wp = this.mWorkPaint;
                wp.set(this.mPaint);
                if (this.mIsJustifying) {
                    wp.setWordSpacing(this.mAddedWidthForJustify);
                }
                int spanStart2 = runStart;
                if (this.mSpanned == null) {
                    spanStart = spanStart2;
                    spanLimit = runLimit;
                } else {
                    int target = after ? offset + 1 : offset;
                    int limit = this.mStart + runLimit;
                    while (true) {
                        int nextSpanTransition = this.mSpanned.nextSpanTransition(this.mStart + spanStart2, limit, MetricAffectingSpan.class);
                        i = this.mStart;
                        spanLimit2 = nextSpanTransition - i;
                        if (spanLimit2 >= target) {
                            break;
                        }
                        spanStart2 = spanLimit2;
                    }
                    MetricAffectingSpan[] spans = (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) this.mSpanned.getSpans(i + spanStart2, i + spanLimit2, MetricAffectingSpan.class), this.mSpanned, MetricAffectingSpan.class);
                    if (spans.length > 0) {
                        ReplacementSpan replacement = null;
                        for (MetricAffectingSpan span : spans) {
                            if (span instanceof ReplacementSpan) {
                                replacement = (ReplacementSpan) span;
                            } else {
                                span.updateMeasureState(wp);
                            }
                        }
                        if (replacement != null) {
                            return after ? spanLimit2 : spanStart2;
                        }
                    }
                    spanStart = spanStart2;
                    spanLimit = spanLimit2;
                }
                if (!after) {
                    cursorOpt = 2;
                }
                if (this.mCharsValid) {
                    return wp.getTextRunCursor(this.mChars, spanStart, spanLimit - spanStart, runIsRtl, offset, cursorOpt);
                }
                CharSequence charSequence = this.mText;
                int i2 = this.mStart;
                return wp.getTextRunCursor(charSequence, i2 + spanStart, i2 + spanLimit, runIsRtl, i2 + offset, cursorOpt) - this.mStart;
            }
        }
        if (after) {
            return TextUtils.getOffsetAfter(this.mText, this.mStart + offset) - this.mStart;
        }
        return TextUtils.getOffsetBefore(this.mText, this.mStart + offset) - this.mStart;
    }

    private static void expandMetricsFromPaint(Paint.FontMetricsInt fmi, TextPaint wp) {
        int previousTop = fmi.top;
        int previousAscent = fmi.ascent;
        int previousDescent = fmi.descent;
        int previousBottom = fmi.bottom;
        int previousLeading = fmi.leading;
        wp.getFontMetricsInt(fmi);
        updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
    }

    static void updateMetrics(Paint.FontMetricsInt fmi, int previousTop, int previousAscent, int previousDescent, int previousBottom, int previousLeading) {
        fmi.top = Math.min(fmi.top, previousTop);
        fmi.ascent = Math.min(fmi.ascent, previousAscent);
        fmi.descent = Math.max(fmi.descent, previousDescent);
        fmi.bottom = Math.max(fmi.bottom, previousBottom);
        fmi.leading = Math.max(fmi.leading, previousLeading);
    }

    private static void drawStroke(TextPaint wp, Canvas c, int color, float position, float thickness, float xleft, float xright, float baseline) {
        float strokeTop = baseline + ((float) wp.baselineShift) + position;
        int previousColor = wp.getColor();
        Paint.Style previousStyle = wp.getStyle();
        boolean previousAntiAlias = wp.isAntiAlias();
        wp.setStyle(Paint.Style.FILL);
        wp.setAntiAlias(true);
        wp.setColor(color);
        c.drawRect(xleft, strokeTop, xright, strokeTop + thickness, wp);
        wp.setStyle(previousStyle);
        wp.setColor(previousColor);
        wp.setAntiAlias(previousAntiAlias);
    }

    private float getRunAdvance(TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, int offset) {
        if (this.mCharsValid) {
            return wp.getRunAdvance(this.mChars, start, end, contextStart, contextEnd, runIsRtl, offset);
        }
        int delta = this.mStart;
        PrecomputedText precomputedText = this.mComputed;
        if (precomputedText == null) {
            return wp.getRunAdvance(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, runIsRtl, delta + offset);
        }
        return precomputedText.getWidth(start + delta, end + delta);
    }

    private float handleText(TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth, int offset, ArrayList<DecorationInfo> decorations) {
        int numDecorations;
        float totalWidth;
        float rightX;
        float leftX;
        float decorationXRight;
        float decorationXLeft;
        int numDecorations2;
        int numDecorations3;
        if (this.mIsJustifying) {
            wp.setWordSpacing(this.mAddedWidthForJustify);
        }
        if (fmi != null) {
            expandMetricsFromPaint(fmi, wp);
        }
        if (end == start) {
            return 0.0f;
        }
        float totalWidth2 = 0.0f;
        int numDecorations4 = decorations == null ? 0 : decorations.size();
        if (needWidth || !(c == null || (wp.bgColor == 0 && numDecorations4 == 0 && !runIsRtl))) {
            numDecorations = numDecorations4;
            totalWidth2 = getRunAdvance(wp, start, end, contextStart, contextEnd, runIsRtl, offset);
        } else {
            numDecorations = numDecorations4;
        }
        if (c != null) {
            if (runIsRtl) {
                leftX = x - totalWidth2;
                rightX = x;
            } else {
                leftX = x;
                rightX = x + totalWidth2;
            }
            if (wp.bgColor != 0) {
                int previousColor = wp.getColor();
                Paint.Style previousStyle = wp.getStyle();
                wp.setColor(wp.bgColor);
                wp.setStyle(Paint.Style.FILL);
                c.drawRect(leftX, (float) top, rightX, (float) bottom, wp);
                wp.setStyle(previousStyle);
                wp.setColor(previousColor);
            }
            totalWidth = totalWidth2;
            drawTextRun(c, wp, start, end, contextStart, contextEnd, runIsRtl, leftX, y + wp.baselineShift);
            if (numDecorations != 0) {
                int i = 0;
                while (i < numDecorations) {
                    DecorationInfo info = decorations.get(i);
                    int decorationStart = Math.max(info.start, start);
                    int decorationEnd = Math.min(info.end, offset);
                    float decorationStartAdvance = getRunAdvance(wp, start, end, contextStart, contextEnd, runIsRtl, decorationStart);
                    float decorationEndAdvance = getRunAdvance(wp, start, end, contextStart, contextEnd, runIsRtl, decorationEnd);
                    if (runIsRtl) {
                        decorationXLeft = rightX - decorationEndAdvance;
                        decorationXRight = rightX - decorationStartAdvance;
                    } else {
                        decorationXLeft = leftX + decorationStartAdvance;
                        decorationXRight = leftX + decorationEndAdvance;
                    }
                    if (info.underlineColor != 0) {
                        drawStroke(wp, c, info.underlineColor, wp.getUnderlinePosition(), info.underlineThickness, decorationXLeft, decorationXRight, (float) y);
                    }
                    if (info.isUnderlineText) {
                        numDecorations2 = numDecorations;
                        numDecorations3 = 1065353216;
                        drawStroke(wp, c, wp.getColor(), wp.getUnderlinePosition(), Math.max(wp.getUnderlineThickness(), 1.0f), decorationXLeft, decorationXRight, (float) y);
                    } else {
                        numDecorations2 = numDecorations;
                        numDecorations3 = 1065353216;
                    }
                    if (info.isStrikeThruText) {
                        drawStroke(wp, c, wp.getColor(), wp.getStrikeThruPosition(), Math.max(wp.getStrikeThruThickness(), (float) numDecorations3), decorationXLeft, decorationXRight, (float) y);
                    }
                    i++;
                    numDecorations = numDecorations2;
                }
            }
        } else {
            totalWidth = totalWidth2;
        }
        return runIsRtl ? -totalWidth : totalWidth;
    }

    private float handleReplacement(ReplacementSpan replacement, TextPaint wp, int start, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        int previousLeading;
        int previousBottom;
        int previousDescent;
        int previousAscent;
        int previousTop;
        float x2;
        float ret = 0.0f;
        int i = this.mStart;
        int textStart = i + start;
        int textLimit = i + limit;
        if (needWidth || (c != null && runIsRtl)) {
            boolean needUpdateMetrics = fmi != null;
            if (needUpdateMetrics) {
                previousTop = fmi.top;
                previousAscent = fmi.ascent;
                previousDescent = fmi.descent;
                previousBottom = fmi.bottom;
                previousLeading = fmi.leading;
            } else {
                previousTop = 0;
                previousAscent = 0;
                previousDescent = 0;
                previousBottom = 0;
                previousLeading = 0;
            }
            ret = (float) replacement.getSize(wp, this.mText, textStart, textLimit, fmi);
            if (needUpdateMetrics) {
                updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
            }
        }
        if (c != null) {
            if (runIsRtl) {
                x2 = x - ret;
            } else {
                x2 = x;
            }
            replacement.draw(c, this.mText, textStart, textLimit, x2, top, y, bottom, wp);
        }
        return runIsRtl ? -ret : ret;
    }

    private int adjustStartHyphenEdit(int start, int startHyphenEdit) {
        if (start > 0) {
            return 0;
        }
        return startHyphenEdit;
    }

    private int adjustEndHyphenEdit(int limit, int endHyphenEdit) {
        if (limit < this.mLen) {
            return 0;
        }
        return endHyphenEdit;
    }

    /* access modifiers changed from: private */
    public static final class DecorationInfo {
        public int end;
        public boolean isStrikeThruText;
        public boolean isUnderlineText;
        public int start;
        public int underlineColor;
        public float underlineThickness;

        private DecorationInfo() {
            this.start = -1;
            this.end = -1;
        }

        public boolean hasDecoration() {
            return this.isStrikeThruText || this.isUnderlineText || this.underlineColor != 0;
        }

        public DecorationInfo copyInfo() {
            DecorationInfo copy = new DecorationInfo();
            copy.isStrikeThruText = this.isStrikeThruText;
            copy.isUnderlineText = this.isUnderlineText;
            copy.underlineColor = this.underlineColor;
            copy.underlineThickness = this.underlineThickness;
            return copy;
        }
    }

    private void extractDecorationInfo(TextPaint paint, DecorationInfo info) {
        info.isStrikeThruText = paint.isStrikeThruText();
        if (info.isStrikeThruText) {
            paint.setStrikeThruText(false);
        }
        info.isUnderlineText = paint.isUnderlineText();
        if (info.isUnderlineText) {
            paint.setUnderlineText(false);
        }
        info.underlineColor = paint.underlineColor;
        info.underlineThickness = paint.underlineThickness;
        paint.setUnderlineText(0, 0.0f);
    }

    /* JADX WARNING: Removed duplicated region for block: B:79:0x026f  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0281  */
    private float handleRun(int start, int measureLimit, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, Paint.FontMetricsInt fmi, boolean needWidth) {
        boolean needsSpanMeasurement;
        int inext;
        boolean z;
        int jnext;
        int j;
        DecorationInfo decorationInfo;
        int mlimit;
        int i;
        TextPaint wp;
        TextPaint activePaint;
        TextLine textLine;
        int jnext2;
        int activeStart;
        boolean z2;
        TextPaint wp2;
        if (measureLimit < start || measureLimit > limit) {
            throw new IndexOutOfBoundsException("measureLimit (" + measureLimit + ") is out of start (" + start + ") and limit (" + limit + ") bounds");
        } else if (start == measureLimit) {
            TextPaint wp3 = this.mWorkPaint;
            wp3.set(this.mPaint);
            if (fmi == null) {
                return 0.0f;
            }
            expandMetricsFromPaint(fmi, wp3);
            return 0.0f;
        } else {
            Spanned spanned = this.mSpanned;
            if (spanned == null) {
                needsSpanMeasurement = false;
            } else {
                SpanSet<MetricAffectingSpan> spanSet = this.mMetricAffectingSpanSpanSet;
                int i2 = this.mStart;
                spanSet.init(spanned, i2 + start, i2 + limit);
                SpanSet<CharacterStyle> spanSet2 = this.mCharacterStyleSpanSet;
                Spanned spanned2 = this.mSpanned;
                int i3 = this.mStart;
                spanSet2.init(spanned2, i3 + start, i3 + limit);
                needsSpanMeasurement = (this.mMetricAffectingSpanSpanSet.numberOfSpans == 0 && this.mCharacterStyleSpanSet.numberOfSpans == 0) ? false : true;
            }
            if (!needsSpanMeasurement) {
                TextPaint wp4 = this.mWorkPaint;
                wp4.set(this.mPaint);
                wp4.setStartHyphenEdit(adjustStartHyphenEdit(start, wp4.getStartHyphenEdit()));
                wp4.setEndHyphenEdit(adjustEndHyphenEdit(limit, wp4.getEndHyphenEdit()));
                return handleText(wp4, start, limit, start, limit, runIsRtl, c, x, top, y, bottom, fmi, needWidth, measureLimit, null);
            }
            float x2 = x;
            int i4 = start;
            while (i4 < measureLimit) {
                TextPaint wp5 = this.mWorkPaint;
                wp5.set(this.mPaint);
                SpanSet<MetricAffectingSpan> spanSet3 = this.mMetricAffectingSpanSpanSet;
                int i5 = this.mStart;
                int inext2 = spanSet3.getNextTransition(i5 + i4, i5 + limit) - this.mStart;
                int mlimit2 = Math.min(inext2, measureLimit);
                ReplacementSpan replacement = null;
                for (int j2 = 0; j2 < this.mMetricAffectingSpanSpanSet.numberOfSpans; j2++) {
                    if (this.mMetricAffectingSpanSpanSet.spanStarts[j2] < this.mStart + mlimit2) {
                        int i6 = this.mMetricAffectingSpanSpanSet.spanEnds[j2];
                        int i7 = this.mStart;
                        if (i6 > i7 + i4) {
                            boolean insideEllipsis = i7 + this.mEllipsisStart <= this.mMetricAffectingSpanSpanSet.spanStarts[j2] && this.mMetricAffectingSpanSpanSet.spanEnds[j2] <= this.mStart + this.mEllipsisEnd;
                            MetricAffectingSpan span = this.mMetricAffectingSpanSpanSet.spans[j2];
                            if (span instanceof ReplacementSpan) {
                                replacement = !insideEllipsis ? (ReplacementSpan) span : null;
                            } else {
                                span.updateDrawState(wp5);
                            }
                        }
                    }
                }
                if (replacement != null) {
                    inext = inext2;
                    x2 += handleReplacement(replacement, wp5, i4, mlimit2, runIsRtl, c, x2, top, y, bottom, fmi, needWidth || mlimit2 < measureLimit);
                } else {
                    int mlimit3 = mlimit2;
                    inext = inext2;
                    TextPaint wp6 = wp5;
                    TextLine textLine2 = this;
                    TextPaint activePaint2 = textLine2.mActivePaint;
                    activePaint2.set(textLine2.mPaint);
                    DecorationInfo decorationInfo2 = textLine2.mDecorationInfo;
                    textLine2.mDecorations.clear();
                    int activeStart2 = i4;
                    int activeEnd = mlimit3;
                    float x3 = x2;
                    int j3 = i4;
                    while (j3 < mlimit3) {
                        SpanSet<CharacterStyle> spanSet4 = textLine2.mCharacterStyleSpanSet;
                        int i8 = textLine2.mStart;
                        int jnext3 = spanSet4.getNextTransition(i8 + j3, i8 + inext) - textLine2.mStart;
                        int offset = Math.min(jnext3, mlimit3);
                        wp6.set(textLine2.mPaint);
                        for (int k = 0; k < textLine2.mCharacterStyleSpanSet.numberOfSpans; k++) {
                            if (textLine2.mCharacterStyleSpanSet.spanStarts[k] < textLine2.mStart + offset && textLine2.mCharacterStyleSpanSet.spanEnds[k] > textLine2.mStart + j3) {
                                textLine2.mCharacterStyleSpanSet.spans[k].updateDrawState(wp6);
                            }
                        }
                        textLine2.extractDecorationInfo(wp6, decorationInfo2);
                        if (j3 == i4) {
                            activePaint2.set(wp6);
                            jnext = jnext3;
                            mlimit = mlimit3;
                            activeStart = activeStart2;
                            decorationInfo = decorationInfo2;
                            j = j3;
                            textLine = textLine2;
                            i = i4;
                            wp = wp6;
                            activePaint = activePaint2;
                        } else if (!equalAttributes(wp6, activePaint2)) {
                            activePaint2.setStartHyphenEdit(textLine2.adjustStartHyphenEdit(activeStart2, textLine2.mPaint.getStartHyphenEdit()));
                            activePaint2.setEndHyphenEdit(textLine2.adjustEndHyphenEdit(activeEnd, textLine2.mPaint.getEndHyphenEdit()));
                            if (!needWidth) {
                                wp2 = wp6;
                                if (activeEnd >= measureLimit) {
                                    z2 = false;
                                    jnext = jnext3;
                                    mlimit = mlimit3;
                                    decorationInfo = decorationInfo2;
                                    j = j3;
                                    i = i4;
                                    x3 += handleText(activePaint2, activeStart2, activeEnd, i4, inext, runIsRtl, c, x3, top, y, bottom, fmi, z2, Math.min(activeEnd, mlimit3), textLine2.mDecorations);
                                    activeStart2 = j;
                                    wp = wp2;
                                    activePaint = activePaint2;
                                    activePaint.set(wp);
                                    textLine = this;
                                    textLine.mDecorations.clear();
                                    activeEnd = jnext;
                                    if (decorationInfo.hasDecoration()) {
                                        DecorationInfo copy = decorationInfo.copyInfo();
                                        copy.start = j;
                                        jnext2 = jnext;
                                        copy.end = jnext2;
                                        textLine.mDecorations.add(copy);
                                    } else {
                                        jnext2 = jnext;
                                    }
                                    activePaint2 = activePaint;
                                    mlimit3 = mlimit;
                                    decorationInfo2 = decorationInfo;
                                    textLine2 = textLine;
                                    j3 = jnext2;
                                    wp6 = wp;
                                    i4 = i;
                                }
                            } else {
                                wp2 = wp6;
                            }
                            z2 = true;
                            jnext = jnext3;
                            mlimit = mlimit3;
                            decorationInfo = decorationInfo2;
                            j = j3;
                            i = i4;
                            x3 += handleText(activePaint2, activeStart2, activeEnd, i4, inext, runIsRtl, c, x3, top, y, bottom, fmi, z2, Math.min(activeEnd, mlimit3), textLine2.mDecorations);
                            activeStart2 = j;
                            wp = wp2;
                            activePaint = activePaint2;
                            activePaint.set(wp);
                            textLine = this;
                            textLine.mDecorations.clear();
                            activeEnd = jnext;
                            if (decorationInfo.hasDecoration()) {
                            }
                            activePaint2 = activePaint;
                            mlimit3 = mlimit;
                            decorationInfo2 = decorationInfo;
                            textLine2 = textLine;
                            j3 = jnext2;
                            wp6 = wp;
                            i4 = i;
                        } else {
                            jnext = jnext3;
                            mlimit = mlimit3;
                            activeStart = activeStart2;
                            decorationInfo = decorationInfo2;
                            j = j3;
                            textLine = textLine2;
                            i = i4;
                            wp = wp6;
                            activePaint = activePaint2;
                        }
                        activeStart2 = activeStart;
                        activeEnd = jnext;
                        if (decorationInfo.hasDecoration()) {
                        }
                        activePaint2 = activePaint;
                        mlimit3 = mlimit;
                        decorationInfo2 = decorationInfo;
                        textLine2 = textLine;
                        j3 = jnext2;
                        wp6 = wp;
                        i4 = i;
                    }
                    activePaint2.setStartHyphenEdit(textLine2.adjustStartHyphenEdit(activeStart2, textLine2.mPaint.getStartHyphenEdit()));
                    activePaint2.setEndHyphenEdit(textLine2.adjustEndHyphenEdit(activeEnd, textLine2.mPaint.getEndHyphenEdit()));
                    if (!needWidth) {
                        if (activeEnd >= measureLimit) {
                            z = false;
                            x2 = x3 + handleText(activePaint2, activeStart2, activeEnd, i4, inext, runIsRtl, c, x3, top, y, bottom, fmi, z, Math.min(activeEnd, mlimit3), textLine2.mDecorations);
                        }
                    }
                    z = true;
                    x2 = x3 + handleText(activePaint2, activeStart2, activeEnd, i4, inext, runIsRtl, c, x3, top, y, bottom, fmi, z, Math.min(activeEnd, mlimit3), textLine2.mDecorations);
                }
                i4 = inext;
            }
            return x2 - x;
        }
    }

    private void drawTextRun(Canvas c, TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, float x, int y) {
        if (this.mCharsValid) {
            c.drawTextRun(this.mChars, start, end - start, contextStart, contextEnd - contextStart, x, (float) y, runIsRtl, wp);
            return;
        }
        int delta = this.mStart;
        c.drawTextRun(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, x, (float) y, runIsRtl, wp);
    }

    /* access modifiers changed from: package-private */
    public float nextTab(float h) {
        Layout.TabStops tabStops = this.mTabs;
        if (tabStops != null) {
            return tabStops.nextTab(h);
        }
        return Layout.TabStops.nextDefaultStop(h, 20.0f);
    }

    private boolean isStretchableWhitespace(int ch) {
        return ch == 32;
    }

    private int countStretchableSpaces(int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) {
            if (isStretchableWhitespace(this.mCharsValid ? this.mChars[i] : this.mText.charAt(this.mStart + i))) {
                count++;
            }
        }
        return count;
    }

    public static boolean isLineEndSpace(char ch) {
        return ch == ' ' || ch == '\t' || ch == 5760 || (8192 <= ch && ch <= 8202 && ch != 8199) || ch == 8287 || ch == 12288;
    }

    private static boolean equalAttributes(TextPaint lp, TextPaint rp) {
        return lp.getColorFilter() == rp.getColorFilter() && lp.getMaskFilter() == rp.getMaskFilter() && lp.getShader() == rp.getShader() && lp.getTypeface() == rp.getTypeface() && lp.getXfermode() == rp.getXfermode() && lp.getTextLocales().equals(rp.getTextLocales()) && TextUtils.equals(lp.getFontFeatureSettings(), rp.getFontFeatureSettings()) && TextUtils.equals(lp.getFontVariationSettings(), rp.getFontVariationSettings()) && lp.getShadowLayerRadius() == rp.getShadowLayerRadius() && lp.getShadowLayerDx() == rp.getShadowLayerDx() && lp.getShadowLayerDy() == rp.getShadowLayerDy() && lp.getShadowLayerColor() == rp.getShadowLayerColor() && lp.getFlags() == rp.getFlags() && lp.getHinting() == rp.getHinting() && lp.getStyle() == rp.getStyle() && lp.getColor() == rp.getColor() && lp.getStrokeWidth() == rp.getStrokeWidth() && lp.getStrokeMiter() == rp.getStrokeMiter() && lp.getStrokeCap() == rp.getStrokeCap() && lp.getStrokeJoin() == rp.getStrokeJoin() && lp.getTextAlign() == rp.getTextAlign() && lp.isElegantTextHeight() == rp.isElegantTextHeight() && lp.getTextSize() == rp.getTextSize() && lp.getTextScaleX() == rp.getTextScaleX() && lp.getTextSkewX() == rp.getTextSkewX() && lp.getLetterSpacing() == rp.getLetterSpacing() && lp.getWordSpacing() == rp.getWordSpacing() && lp.getStartHyphenEdit() == rp.getStartHyphenEdit() && lp.getEndHyphenEdit() == rp.getEndHyphenEdit() && lp.bgColor == rp.bgColor && lp.baselineShift == rp.baselineShift && lp.linkColor == rp.linkColor && lp.drawableState == rp.drawableState && lp.density == rp.density && lp.underlineColor == rp.underlineColor && lp.underlineThickness == rp.underlineThickness;
    }
}

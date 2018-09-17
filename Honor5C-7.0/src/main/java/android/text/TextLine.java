package android.text;

import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.text.Layout.Directions;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import android.view.inputmethod.EditorInfo;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Protocol;

class TextLine {
    private static final boolean DEBUG = false;
    private static final int TAB_INCREMENT = 20;
    private static final TextLine[] sCached = null;
    private final SpanSet<CharacterStyle> mCharacterStyleSpanSet;
    private char[] mChars;
    private boolean mCharsValid;
    private int mDir;
    private Directions mDirections;
    private boolean mHasTabs;
    private int mLen;
    private final SpanSet<MetricAffectingSpan> mMetricAffectingSpanSpanSet;
    private TextPaint mPaint;
    private final SpanSet<ReplacementSpan> mReplacementSpanSpanSet;
    private Spanned mSpanned;
    private int mStart;
    private TabStops mTabs;
    private CharSequence mText;
    private final TextPaint mWorkPaint;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.TextLine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.TextLine.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.TextLine.<clinit>():void");
    }

    TextLine() {
        this.mWorkPaint = new TextPaint();
        this.mMetricAffectingSpanSpanSet = new SpanSet(MetricAffectingSpan.class);
        this.mCharacterStyleSpanSet = new SpanSet(CharacterStyle.class);
        this.mReplacementSpanSpanSet = new SpanSet(ReplacementSpan.class);
    }

    static TextLine obtain() {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static TextLine recycle(TextLine tl) {
        tl.mText = null;
        tl.mPaint = null;
        tl.mDirections = null;
        tl.mSpanned = null;
        tl.mTabs = null;
        tl.mChars = null;
        tl.mMetricAffectingSpanSpanSet.recycle();
        tl.mCharacterStyleSpanSet.recycle();
        tl.mReplacementSpanSpanSet.recycle();
        synchronized (sCached) {
            int i = 0;
            while (true) {
                if (i >= sCached.length) {
                    break;
                } else if (sCached[i] == null) {
                    break;
                } else {
                    i++;
                }
            }
            sCached[i] = tl;
        }
        return null;
    }

    void set(TextPaint paint, CharSequence text, int start, int limit, int dir, Directions directions, boolean hasTabs, TabStops tabStops) {
        this.mPaint = paint;
        this.mText = text;
        this.mStart = start;
        this.mLen = limit - start;
        this.mDir = dir;
        this.mDirections = directions;
        if (this.mDirections == null) {
            throw new IllegalArgumentException("Directions cannot be null");
        }
        this.mHasTabs = hasTabs;
        this.mSpanned = null;
        boolean hasReplacement = DEBUG;
        if (text instanceof Spanned) {
            this.mSpanned = (Spanned) text;
            this.mReplacementSpanSpanSet.init(this.mSpanned, start, limit);
            hasReplacement = this.mReplacementSpanSpanSet.numberOfSpans > 0 ? true : DEBUG;
        }
        boolean z = (hasReplacement || hasTabs || directions != Layout.DIRS_ALL_LEFT_TO_RIGHT) ? true : DEBUG;
        this.mCharsValid = z;
        if (this.mCharsValid) {
            if (this.mChars == null || this.mChars.length < this.mLen) {
                this.mChars = ArrayUtils.newUnpaddedCharArray(this.mLen);
            }
            TextUtils.getChars(text, start, limit, this.mChars, 0);
            if (hasReplacement) {
                char[] chars = this.mChars;
                int i = start;
                while (i < limit) {
                    int inext = this.mReplacementSpanSpanSet.getNextTransition(i, limit);
                    if (this.mReplacementSpanSpanSet.hasSpansIntersecting(i, inext)) {
                        chars[i - start] = '\ufffc';
                        int e = inext - start;
                        for (int j = (i - start) + 1; j < e; j++) {
                            chars[j] = '\ufeff';
                        }
                    }
                    i = inext;
                }
            }
        }
        this.mTabs = tabStops;
    }

    void draw(Canvas c, float x, int top, int y, int bottom) {
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                drawRun(c, 0, this.mLen, DEBUG, x, top, y, bottom, DEBUG);
                return;
            } else if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                drawRun(c, 0, this.mLen, true, x, top, y, bottom, DEBUG);
                return;
            }
        }
        float h = 0.0f;
        int[] runs = this.mDirections.mDirections;
        int lastRunIndex = runs.length - 2;
        int i = 0;
        while (i < runs.length) {
            int runStart = runs[i];
            int runLimit = runStart + (runs[i + 1] & 67108863);
            if (runLimit > this.mLen) {
                runLimit = this.mLen;
            }
            boolean runIsRtl = (runs[i + 1] & EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS) != 0 ? true : DEBUG;
            int segstart = runStart;
            int j = this.mHasTabs ? runStart : runLimit;
            while (j <= runLimit) {
                int codept = 0;
                if (this.mHasTabs && j < runLimit) {
                    codept = this.mChars[j];
                    if (codept >= 55296 && codept < 56320 && j + 1 < runLimit) {
                        codept = Character.codePointAt(this.mChars, j);
                        if (codept > Protocol.MAX_MESSAGE) {
                            j++;
                            j++;
                        }
                    }
                }
                if (j == runLimit || codept == 9) {
                    float f = x + h;
                    boolean z = (i == lastRunIndex && j == this.mLen) ? DEBUG : true;
                    h += drawRun(c, segstart, j, runIsRtl, f, top, y, bottom, z);
                    if (codept == 9) {
                        h = ((float) this.mDir) * nextTab(((float) this.mDir) * h);
                    }
                    segstart = j + 1;
                    j++;
                } else {
                    j++;
                }
            }
            i += 2;
        }
    }

    float metrics(FontMetricsInt fmi) {
        return measure(this.mLen, DEBUG, fmi);
    }

    float measure(int offset, boolean trailing, FontMetricsInt fmi) {
        int target;
        if (trailing) {
            target = offset - 1;
        } else {
            target = offset;
        }
        if (target < 0) {
            return 0.0f;
        }
        float h = 0.0f;
        if (!this.mHasTabs) {
            if (this.mDirections == Layout.DIRS_ALL_LEFT_TO_RIGHT) {
                return measureRun(0, offset, this.mLen, DEBUG, fmi);
            } else if (this.mDirections == Layout.DIRS_ALL_RIGHT_TO_LEFT) {
                return measureRun(0, offset, this.mLen, true, fmi);
            }
        }
        char[] chars = this.mChars;
        int[] runs = this.mDirections.mDirections;
        for (int i = 0; i < runs.length; i += 2) {
            int runStart = runs[i];
            int runLimit = runStart + (runs[i + 1] & 67108863);
            if (runLimit > this.mLen) {
                runLimit = this.mLen;
            }
            boolean runIsRtl = (runs[i + 1] & EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS) != 0 ? true : DEBUG;
            int segstart = runStart;
            int j = this.mHasTabs ? runStart : runLimit;
            while (j <= runLimit) {
                int codept = 0;
                if (this.mHasTabs && j < runLimit) {
                    codept = chars[j];
                    if (codept >= 55296 && codept < 56320 && j + 1 < runLimit) {
                        codept = Character.codePointAt(chars, j);
                        if (codept > Protocol.MAX_MESSAGE) {
                            j++;
                            j++;
                        }
                    }
                }
                if (j == runLimit || codept == 9) {
                    boolean inSegment = (target < segstart || target >= j) ? DEBUG : true;
                    boolean advance = (this.mDir == -1 ? true : DEBUG) == runIsRtl ? true : DEBUG;
                    if (inSegment && advance) {
                        return h + measureRun(segstart, offset, j, runIsRtl, fmi);
                    }
                    float w = measureRun(segstart, j, j, runIsRtl, fmi);
                    if (!advance) {
                        w = -w;
                    }
                    h += w;
                    if (inSegment) {
                        return h + measureRun(segstart, offset, j, runIsRtl, null);
                    }
                    if (codept == 9) {
                        if (offset == j) {
                            return h;
                        }
                        h = ((float) this.mDir) * nextTab(((float) this.mDir) * h);
                        if (target == j) {
                            return h;
                        }
                    }
                    segstart = j + 1;
                    j++;
                } else {
                    j++;
                }
            }
        }
        return h;
    }

    private float drawRun(Canvas c, int start, int limit, boolean runIsRtl, float x, int top, int y, int bottom, boolean needWidth) {
        if ((this.mDir == 1 ? true : DEBUG) != runIsRtl) {
            return handleRun(start, limit, limit, runIsRtl, c, x, top, y, bottom, null, needWidth);
        }
        float w = -measureRun(start, limit, limit, runIsRtl, null);
        handleRun(start, limit, limit, runIsRtl, c, x + w, top, y, bottom, null, DEBUG);
        return w;
    }

    private float measureRun(int start, int offset, int limit, boolean runIsRtl, FontMetricsInt fmi) {
        return handleRun(start, offset, limit, runIsRtl, null, 0.0f, 0, 0, 0, fmi, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int getOffsetToLeftRightOf(int cursor, boolean toLeft) {
        int runIndex;
        boolean advance;
        int lineEnd = this.mLen;
        boolean paraIsRtl = this.mDir == -1 ? true : DEBUG;
        int[] runs = this.mDirections.mDirections;
        int runLevel = 0;
        int runStart = 0;
        int runLimit = lineEnd;
        int newCaret = -1;
        boolean trailing = DEBUG;
        if (cursor == 0) {
            runIndex = -2;
        } else if (cursor == lineEnd) {
            runIndex = runs.length;
        } else {
            boolean runIsRtl;
            int i;
            runIndex = 0;
            while (runIndex < runs.length) {
                runStart = runs[runIndex] + 0;
                if (cursor >= runStart) {
                    runLimit = runStart + (runs[runIndex + 1] & 67108863);
                    if (runLimit > lineEnd) {
                        runLimit = lineEnd;
                    }
                    if (cursor < runLimit) {
                        runLevel = (runs[runIndex + 1] >>> 26) & 63;
                        if (cursor == runStart) {
                            int pos = cursor - 1;
                            for (int prevRunIndex = 0; prevRunIndex < runs.length; prevRunIndex += 2) {
                                int prevRunStart = runs[prevRunIndex] + 0;
                                if (pos >= prevRunStart) {
                                    int prevRunLimit = prevRunStart + (runs[prevRunIndex + 1] & 67108863);
                                    if (prevRunLimit > lineEnd) {
                                        prevRunLimit = lineEnd;
                                    }
                                    if (pos < prevRunLimit) {
                                        int prevRunLevel = (runs[prevRunIndex + 1] >>> 26) & 63;
                                        if (prevRunLevel < runLevel) {
                                            runIndex = prevRunIndex;
                                            runLevel = prevRunLevel;
                                            runStart = prevRunStart;
                                            runLimit = prevRunLimit;
                                            trailing = true;
                                            break;
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        }
                        if (runIndex != runs.length) {
                            runIsRtl = (runLevel & 1) == 0 ? true : DEBUG;
                            advance = toLeft != runIsRtl ? true : DEBUG;
                            if (advance) {
                                i = runStart;
                            } else {
                                i = runLimit;
                            }
                            if (!(cursor == i && advance == trailing)) {
                                newCaret = getOffsetBeforeAfter(runIndex, runStart, runLimit, runIsRtl, cursor, advance);
                                if (advance) {
                                    i = runStart;
                                } else {
                                    i = runLimit;
                                }
                                if (newCaret != i) {
                                    return newCaret;
                                }
                            }
                        }
                    }
                }
                runIndex += 2;
            }
            if (runIndex != runs.length) {
                if ((runLevel & 1) == 0) {
                }
                if (toLeft != runIsRtl) {
                }
                if (advance) {
                    i = runStart;
                } else {
                    i = runLimit;
                }
                newCaret = getOffsetBeforeAfter(runIndex, runStart, runLimit, runIsRtl, cursor, advance);
                if (advance) {
                    i = runStart;
                } else {
                    i = runLimit;
                }
                if (newCaret != i) {
                    return newCaret;
                }
            }
        }
        while (true) {
            advance = toLeft == paraIsRtl ? true : DEBUG;
            int otherRunIndex = runIndex + (advance ? 2 : -2);
            if (otherRunIndex >= 0 && otherRunIndex < runs.length) {
                int otherRunStart = runs[otherRunIndex] + 0;
                int otherRunLimit = otherRunStart + (runs[otherRunIndex + 1] & 67108863);
                if (otherRunLimit > lineEnd) {
                    otherRunLimit = lineEnd;
                }
                int otherRunLevel = (runs[otherRunIndex + 1] >>> 26) & 63;
                boolean otherRunIsRtl = (otherRunLevel & 1) != 0 ? true : DEBUG;
                advance = toLeft == otherRunIsRtl ? true : DEBUG;
                if (newCaret != -1) {
                    break;
                }
                int i2;
                if (advance) {
                    i2 = otherRunStart;
                } else {
                    i2 = otherRunLimit;
                }
                newCaret = getOffsetBeforeAfter(otherRunIndex, otherRunStart, otherRunLimit, otherRunIsRtl, i2, advance);
                if (!advance) {
                    otherRunLimit = otherRunStart;
                }
                if (newCaret != otherRunLimit) {
                    break;
                }
                runIndex = otherRunIndex;
                runLevel = otherRunLevel;
            }
        }
        if (newCaret == -1) {
            newCaret = advance ? this.mLen + 1 : -1;
        } else if (newCaret <= lineEnd) {
            newCaret = advance ? lineEnd : 0;
        }
        return newCaret;
    }

    private int getOffsetBeforeAfter(int runIndex, int runStart, int runLimit, boolean runIsRtl, int offset, boolean after) {
        if (runIndex >= 0) {
            if (offset != (after ? this.mLen : 0)) {
                int spanLimit;
                TextPaint wp = this.mWorkPaint;
                wp.set(this.mPaint);
                int spanStart = runStart;
                if (this.mSpanned == null) {
                    spanLimit = runLimit;
                } else {
                    int limit = this.mStart + runLimit;
                    while (true) {
                        spanLimit = this.mSpanned.nextSpanTransition(this.mStart + spanStart, limit, MetricAffectingSpan.class) - this.mStart;
                        if (spanLimit >= (after ? offset + 1 : offset)) {
                            break;
                        }
                        spanStart = spanLimit;
                    }
                    MetricAffectingSpan[] spans = (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) this.mSpanned.getSpans(this.mStart + spanStart, this.mStart + spanLimit, MetricAffectingSpan.class), this.mSpanned, MetricAffectingSpan.class);
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
                            if (!after) {
                                spanLimit = spanStart;
                            }
                            return spanLimit;
                        }
                    }
                }
                int dir = runIsRtl ? 1 : 0;
                int cursorOpt = after ? 0 : 2;
                if (this.mCharsValid) {
                    return wp.getTextRunCursor(this.mChars, spanStart, spanLimit - spanStart, dir, offset, cursorOpt);
                }
                return wp.getTextRunCursor(this.mText, this.mStart + spanStart, this.mStart + spanLimit, dir, this.mStart + offset, cursorOpt) - this.mStart;
            }
        }
        if (after) {
            return TextUtils.getOffsetAfter(this.mText, this.mStart + offset) - this.mStart;
        }
        return TextUtils.getOffsetBefore(this.mText, this.mStart + offset) - this.mStart;
    }

    private static void expandMetricsFromPaint(FontMetricsInt fmi, TextPaint wp) {
        int previousTop = fmi.top;
        int previousAscent = fmi.ascent;
        int previousDescent = fmi.descent;
        int previousBottom = fmi.bottom;
        int previousLeading = fmi.leading;
        wp.getFontMetricsInt(fmi);
        updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
    }

    static void updateMetrics(FontMetricsInt fmi, int previousTop, int previousAscent, int previousDescent, int previousBottom, int previousLeading) {
        fmi.top = Math.min(fmi.top, previousTop);
        fmi.ascent = Math.min(fmi.ascent, previousAscent);
        fmi.descent = Math.max(fmi.descent, previousDescent);
        fmi.bottom = Math.max(fmi.bottom, previousBottom);
        fmi.leading = Math.max(fmi.leading, previousLeading);
    }

    private float handleText(TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, FontMetricsInt fmi, boolean needWidth, int offset) {
        if (fmi != null) {
            expandMetricsFromPaint(fmi, wp);
        }
        if (end - start == 0) {
            return 0.0f;
        }
        int previousColor;
        Style previousStyle;
        float ret = 0.0f;
        if (!needWidth) {
            if (c != null) {
                if (wp.bgColor == 0 && wp.underlineColor == 0) {
                    if (runIsRtl) {
                    }
                }
            }
            if (c != null) {
                if (runIsRtl) {
                    x -= ret;
                }
                if (wp.bgColor != 0) {
                    previousColor = wp.getColor();
                    previousStyle = wp.getStyle();
                    wp.setColor(wp.bgColor);
                    wp.setStyle(Style.FILL);
                    c.drawRect(x, (float) top, x + ret, (float) bottom, wp);
                    wp.setStyle(previousStyle);
                    wp.setColor(previousColor);
                }
                if (wp.underlineColor != 0) {
                    float underlineTop = ((float) (wp.baselineShift + y)) + (wp.getTextSize() * 0.11111111f);
                    previousColor = wp.getColor();
                    previousStyle = wp.getStyle();
                    boolean previousAntiAlias = wp.isAntiAlias();
                    wp.setStyle(Style.FILL);
                    wp.setAntiAlias(true);
                    wp.setColor(wp.underlineColor);
                    c.drawRect(x, underlineTop, x + ret, underlineTop + wp.underlineThickness, wp);
                    wp.setStyle(previousStyle);
                    wp.setColor(previousColor);
                    wp.setAntiAlias(previousAntiAlias);
                }
                drawTextRun(c, wp, start, end, contextStart, contextEnd, runIsRtl, x, y + wp.baselineShift);
            }
            if (runIsRtl) {
                ret = -ret;
            }
            return ret;
        }
        if (this.mCharsValid) {
            ret = wp.getRunAdvance(this.mChars, start, end, contextStart, contextEnd, runIsRtl, offset);
        } else {
            int delta = this.mStart;
            ret = wp.getRunAdvance(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, runIsRtl, delta + offset);
        }
        if (c != null) {
            if (runIsRtl) {
                x -= ret;
            }
            if (wp.bgColor != 0) {
                previousColor = wp.getColor();
                previousStyle = wp.getStyle();
                wp.setColor(wp.bgColor);
                wp.setStyle(Style.FILL);
                c.drawRect(x, (float) top, x + ret, (float) bottom, wp);
                wp.setStyle(previousStyle);
                wp.setColor(previousColor);
            }
            if (wp.underlineColor != 0) {
                float underlineTop2 = ((float) (wp.baselineShift + y)) + (wp.getTextSize() * 0.11111111f);
                previousColor = wp.getColor();
                previousStyle = wp.getStyle();
                boolean previousAntiAlias2 = wp.isAntiAlias();
                wp.setStyle(Style.FILL);
                wp.setAntiAlias(true);
                wp.setColor(wp.underlineColor);
                c.drawRect(x, underlineTop2, x + ret, underlineTop2 + wp.underlineThickness, wp);
                wp.setStyle(previousStyle);
                wp.setColor(previousColor);
                wp.setAntiAlias(previousAntiAlias2);
            }
            drawTextRun(c, wp, start, end, contextStart, contextEnd, runIsRtl, x, y + wp.baselineShift);
        }
        if (runIsRtl) {
            ret = -ret;
        }
        return ret;
    }

    private float handleReplacement(ReplacementSpan replacement, TextPaint wp, int start, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, FontMetricsInt fmi, boolean needWidth) {
        float ret = 0.0f;
        int textStart = this.mStart + start;
        int textLimit = this.mStart + limit;
        if (needWidth || (c != null && runIsRtl)) {
            int previousTop = 0;
            int previousAscent = 0;
            int previousDescent = 0;
            int previousBottom = 0;
            int previousLeading = 0;
            boolean needUpdateMetrics = fmi != null ? true : DEBUG;
            if (needUpdateMetrics) {
                previousTop = fmi.top;
                previousAscent = fmi.ascent;
                previousDescent = fmi.descent;
                previousBottom = fmi.bottom;
                previousLeading = fmi.leading;
            }
            ret = (float) replacement.getSize(wp, this.mText, textStart, textLimit, fmi);
            if (needUpdateMetrics) {
                updateMetrics(fmi, previousTop, previousAscent, previousDescent, previousBottom, previousLeading);
            }
        }
        if (c != null) {
            if (runIsRtl) {
                x -= ret;
            }
            replacement.draw(c, this.mText, textStart, textLimit, x, top, y, bottom, wp);
        }
        return runIsRtl ? -ret : ret;
    }

    private float handleRun(int start, int measureLimit, int limit, boolean runIsRtl, Canvas c, float x, int top, int y, int bottom, FontMetricsInt fmi, boolean needWidth) {
        TextPaint wp;
        if (start == measureLimit) {
            wp = this.mWorkPaint;
            wp.set(this.mPaint);
            if (fmi != null) {
                expandMetricsFromPaint(fmi, wp);
            }
            return 0.0f;
        } else if (this.mSpanned == null) {
            wp = this.mWorkPaint;
            wp.set(this.mPaint);
            mlimit = measureLimit;
            boolean z = (needWidth || measureLimit < measureLimit) ? true : DEBUG;
            return handleText(wp, start, limit, start, limit, runIsRtl, c, x, top, y, bottom, fmi, z, measureLimit);
        } else {
            this.mMetricAffectingSpanSpanSet.init(this.mSpanned, this.mStart + start, this.mStart + limit);
            this.mCharacterStyleSpanSet.init(this.mSpanned, this.mStart + start, this.mStart + limit);
            float originalX = x;
            int i = start;
            while (i < measureLimit) {
                wp = this.mWorkPaint;
                wp.set(this.mPaint);
                int inext = this.mMetricAffectingSpanSpanSet.getNextTransition(this.mStart + i, this.mStart + limit) - this.mStart;
                mlimit = Math.min(inext, measureLimit);
                ReplacementSpan replacement = null;
                int j = 0;
                while (j < this.mMetricAffectingSpanSpanSet.numberOfSpans) {
                    if (this.mMetricAffectingSpanSpanSet.spanStarts[j] < this.mStart + mlimit && this.mMetricAffectingSpanSpanSet.spanEnds[j] > this.mStart + i) {
                        MetricAffectingSpan span = ((MetricAffectingSpan[]) this.mMetricAffectingSpanSpanSet.spans)[j];
                        if (span instanceof ReplacementSpan) {
                            replacement = (ReplacementSpan) span;
                        } else {
                            span.updateDrawState(wp);
                        }
                    }
                    j++;
                }
                if (replacement != null) {
                    boolean z2 = (needWidth || mlimit < measureLimit) ? true : DEBUG;
                    x += handleReplacement(replacement, wp, i, mlimit, runIsRtl, c, x, top, y, bottom, fmi, z2);
                } else {
                    j = i;
                    while (j < mlimit) {
                        int jnext = this.mCharacterStyleSpanSet.getNextTransition(this.mStart + j, this.mStart + inext) - this.mStart;
                        int offset = Math.min(jnext, mlimit);
                        wp.set(this.mPaint);
                        int k = 0;
                        while (k < this.mCharacterStyleSpanSet.numberOfSpans) {
                            if (this.mCharacterStyleSpanSet.spanStarts[k] < this.mStart + offset && this.mCharacterStyleSpanSet.spanEnds[k] > this.mStart + j) {
                                ((CharacterStyle[]) this.mCharacterStyleSpanSet.spans)[k].updateDrawState(wp);
                            }
                            k++;
                        }
                        if (jnext < this.mLen) {
                            wp.setHyphenEdit(0);
                        }
                        boolean z3 = (needWidth || jnext < measureLimit) ? true : DEBUG;
                        x += handleText(wp, j, jnext, i, inext, runIsRtl, c, x, top, y, bottom, fmi, z3, offset);
                        j = jnext;
                    }
                }
                i = inext;
            }
            return x - originalX;
        }
    }

    private void drawTextRun(Canvas c, TextPaint wp, int start, int end, int contextStart, int contextEnd, boolean runIsRtl, float x, int y) {
        if (this.mCharsValid) {
            Canvas canvas = c;
            int i = start;
            int i2 = contextStart;
            canvas.drawTextRun(this.mChars, i, end - start, i2, contextEnd - contextStart, x, (float) y, runIsRtl, wp);
            return;
        }
        int delta = this.mStart;
        c.drawTextRun(this.mText, delta + start, delta + end, delta + contextStart, delta + contextEnd, x, (float) y, runIsRtl, wp);
    }

    float nextTab(float h) {
        if (this.mTabs != null) {
            return this.mTabs.nextTab(h);
        }
        return TabStops.nextDefaultStop(h, TAB_INCREMENT);
    }
}

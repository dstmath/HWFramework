package android.text;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.text.MeasuredText;
import android.text.AutoGrowArray;
import android.text.Layout;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import android.util.Pools;
import java.util.Arrays;

public class MeasuredParagraph {
    private static final char OBJECT_REPLACEMENT_CHARACTER = 65532;
    private static final Pools.SynchronizedPool<MeasuredParagraph> sPool = new Pools.SynchronizedPool<>(1);
    private Paint.FontMetricsInt mCachedFm;
    private TextPaint mCachedPaint = new TextPaint();
    private char[] mCopiedBuffer;
    private AutoGrowArray.IntArray mFontMetrics = new AutoGrowArray.IntArray(16);
    private AutoGrowArray.ByteArray mLevels = new AutoGrowArray.ByteArray();
    private boolean mLtrWithoutBidi;
    private MeasuredText mMeasuredText;
    private int mParaDir;
    private AutoGrowArray.IntArray mSpanEndCache = new AutoGrowArray.IntArray(4);
    private Spanned mSpanned;
    private int mTextLength;
    private int mTextStart;
    private float mWholeWidth;
    private AutoGrowArray.FloatArray mWidths = new AutoGrowArray.FloatArray();

    private MeasuredParagraph() {
    }

    private static MeasuredParagraph obtain() {
        MeasuredParagraph mt = sPool.acquire();
        return mt != null ? mt : new MeasuredParagraph();
    }

    public void recycle() {
        release();
        sPool.release(this);
    }

    public void release() {
        reset();
        this.mLevels.clearWithReleasingLargeArray();
        this.mWidths.clearWithReleasingLargeArray();
        this.mFontMetrics.clearWithReleasingLargeArray();
        this.mSpanEndCache.clearWithReleasingLargeArray();
    }

    private void reset() {
        this.mSpanned = null;
        this.mCopiedBuffer = null;
        this.mWholeWidth = 0.0f;
        this.mLevels.clear();
        this.mWidths.clear();
        this.mFontMetrics.clear();
        this.mSpanEndCache.clear();
        this.mMeasuredText = null;
    }

    public int getTextLength() {
        return this.mTextLength;
    }

    public char[] getChars() {
        return this.mCopiedBuffer;
    }

    public int getParagraphDir() {
        return this.mParaDir;
    }

    public Layout.Directions getDirections(int start, int end) {
        if (this.mLtrWithoutBidi) {
            return Layout.DIRS_ALL_LEFT_TO_RIGHT;
        }
        return AndroidBidi.directions(this.mParaDir, this.mLevels.getRawArray(), start, this.mCopiedBuffer, start, end - start);
    }

    public float getWholeWidth() {
        return this.mWholeWidth;
    }

    public AutoGrowArray.FloatArray getWidths() {
        return this.mWidths;
    }

    public AutoGrowArray.IntArray getSpanEndCache() {
        return this.mSpanEndCache;
    }

    public AutoGrowArray.IntArray getFontMetrics() {
        return this.mFontMetrics;
    }

    public MeasuredText getMeasuredText() {
        return this.mMeasuredText;
    }

    public float getWidth(int start, int end) {
        MeasuredText measuredText = this.mMeasuredText;
        if (measuredText != null) {
            return measuredText.getWidth(start, end);
        }
        float[] widths = this.mWidths.getRawArray();
        float r = 0.0f;
        for (int i = start; i < end; i++) {
            r += widths[i];
        }
        return r;
    }

    public void getBounds(int start, int end, Rect bounds) {
        this.mMeasuredText.getBounds(start, end, bounds);
    }

    public float getCharWidthAt(int offset) {
        return this.mMeasuredText.getCharWidthAt(offset);
    }

    public static MeasuredParagraph buildForBidi(CharSequence text, int start, int end, TextDirectionHeuristic textDir, MeasuredParagraph recycle) {
        MeasuredParagraph mt = recycle == null ? obtain() : recycle;
        mt.resetAndAnalyzeBidi(text, start, end, textDir);
        return mt;
    }

    public static MeasuredParagraph buildForMeasurement(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir, MeasuredParagraph recycle) {
        MeasuredParagraph mt = recycle == null ? obtain() : recycle;
        mt.resetAndAnalyzeBidi(text, start, end, textDir);
        mt.mWidths.resize(mt.mTextLength);
        if (mt.mTextLength == 0) {
            return mt;
        }
        if (mt.mSpanned == null) {
            mt.applyMetricsAffectingSpan(paint, null, start, end, null);
        } else {
            int spanStart = start;
            while (spanStart < end) {
                int spanEnd = mt.mSpanned.nextSpanTransition(spanStart, end, MetricAffectingSpan.class);
                mt.applyMetricsAffectingSpan(paint, (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) mt.mSpanned.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), mt.mSpanned, MetricAffectingSpan.class), spanStart, spanEnd, null);
                spanStart = spanEnd;
            }
        }
        return mt;
    }

    public static MeasuredParagraph buildForStaticLayout(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir, boolean computeHyphenation, boolean computeLayout, MeasuredParagraph hint, MeasuredParagraph recycle) {
        MeasuredText.Builder builder;
        MeasuredParagraph mt = recycle == null ? obtain() : recycle;
        mt.resetAndAnalyzeBidi(text, start, end, textDir);
        if (hint == null) {
            builder = new MeasuredText.Builder(mt.mCopiedBuffer).setComputeHyphenation(computeHyphenation).setComputeLayout(computeLayout);
        } else {
            builder = new MeasuredText.Builder(hint.mMeasuredText);
        }
        if (mt.mTextLength == 0) {
            mt.mMeasuredText = builder.build();
        } else {
            if (mt.mSpanned == null) {
                mt.applyMetricsAffectingSpan(paint, null, start, end, builder);
                mt.mSpanEndCache.append(end);
            } else {
                int spanStart = start;
                while (spanStart < end) {
                    int spanEnd = mt.mSpanned.nextSpanTransition(spanStart, end, MetricAffectingSpan.class);
                    mt.applyMetricsAffectingSpan(paint, (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) mt.mSpanned.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), mt.mSpanned, MetricAffectingSpan.class), spanStart, spanEnd, builder);
                    mt.mSpanEndCache.append(spanEnd);
                    spanStart = spanEnd;
                }
            }
            mt.mMeasuredText = builder.build();
        }
        return mt;
    }

    private void resetAndAnalyzeBidi(CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        int bidiRequest;
        reset();
        this.mSpanned = text instanceof Spanned ? (Spanned) text : null;
        this.mTextStart = start;
        this.mTextLength = end - start;
        char[] cArr = this.mCopiedBuffer;
        if (cArr == null || cArr.length != this.mTextLength) {
            this.mCopiedBuffer = new char[this.mTextLength];
        }
        TextUtils.getChars(text, start, end, this.mCopiedBuffer, 0);
        Spanned spanned = this.mSpanned;
        if (spanned != null) {
            ReplacementSpan[] spans = (ReplacementSpan[]) spanned.getSpans(start, end, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int startInPara = this.mSpanned.getSpanStart(spans[i]) - start;
                int endInPara = this.mSpanned.getSpanEnd(spans[i]) - start;
                if (startInPara < 0) {
                    startInPara = 0;
                }
                if (endInPara > this.mTextLength) {
                    endInPara = this.mTextLength;
                }
                Arrays.fill(this.mCopiedBuffer, startInPara, endInPara, (char) OBJECT_REPLACEMENT_CHARACTER);
            }
        }
        int i2 = 1;
        if ((textDir == TextDirectionHeuristics.LTR || textDir == TextDirectionHeuristics.FIRSTSTRONG_LTR || textDir == TextDirectionHeuristics.ANYRTL_LTR) && TextUtils.doesNotNeedBidi(this.mCopiedBuffer, 0, this.mTextLength)) {
            this.mLevels.clear();
            this.mParaDir = 1;
            this.mLtrWithoutBidi = true;
            return;
        }
        if (textDir == TextDirectionHeuristics.LTR) {
            bidiRequest = 1;
        } else if (textDir == TextDirectionHeuristics.RTL) {
            bidiRequest = -1;
        } else if (textDir == TextDirectionHeuristics.FIRSTSTRONG_LTR) {
            bidiRequest = 2;
        } else if (textDir == TextDirectionHeuristics.FIRSTSTRONG_RTL) {
            bidiRequest = -2;
        } else {
            if (textDir.isRtl(this.mCopiedBuffer, 0, this.mTextLength)) {
                i2 = -1;
            }
            bidiRequest = i2;
        }
        this.mLevels.resize(this.mTextLength);
        this.mParaDir = AndroidBidi.bidi(bidiRequest, this.mCopiedBuffer, this.mLevels.getRawArray());
        this.mLtrWithoutBidi = false;
    }

    private void applyReplacementRun(ReplacementSpan replacement, int start, int end, MeasuredText.Builder builder) {
        TextPaint textPaint = this.mCachedPaint;
        Spanned spanned = this.mSpanned;
        int i = this.mTextStart;
        float width = (float) replacement.getSize(textPaint, spanned, start + i, end + i, this.mCachedFm);
        if (builder == null) {
            this.mWidths.set(start, width);
            if (end > start + 1) {
                Arrays.fill(this.mWidths.getRawArray(), start + 1, end, 0.0f);
            }
            this.mWholeWidth += width;
            return;
        }
        builder.appendReplacementRun(this.mCachedPaint, end - start, width);
    }

    private void applyStyleRun(int start, int end, MeasuredText.Builder builder) {
        if (!this.mLtrWithoutBidi) {
            byte level = this.mLevels.get(start);
            int levelStart = start;
            int levelEnd = start + 1;
            while (true) {
                if (levelEnd == end || this.mLevels.get(levelEnd) != level) {
                    boolean isRtl = (level & 1) != 0;
                    if (builder == null) {
                        int levelLength = levelEnd - levelStart;
                        this.mWholeWidth += this.mCachedPaint.getTextRunAdvances(this.mCopiedBuffer, levelStart, levelLength, levelStart, levelLength, isRtl, this.mWidths.getRawArray(), levelStart);
                    } else {
                        builder.appendStyleRun(this.mCachedPaint, levelEnd - levelStart, isRtl);
                    }
                    if (levelEnd != end) {
                        levelStart = levelEnd;
                        level = this.mLevels.get(levelEnd);
                    } else {
                        return;
                    }
                }
                levelEnd++;
            }
        } else if (builder == null) {
            this.mWholeWidth += this.mCachedPaint.getTextRunAdvances(this.mCopiedBuffer, start, end - start, start, end - start, false, this.mWidths.getRawArray(), start);
        } else {
            builder.appendStyleRun(this.mCachedPaint, end - start, false);
        }
    }

    private void applyMetricsAffectingSpan(TextPaint paint, MetricAffectingSpan[] spans, int start, int end, MeasuredText.Builder builder) {
        this.mCachedPaint.set(paint);
        boolean needFontMetrics = false;
        this.mCachedPaint.baselineShift = 0;
        if (builder != null) {
            needFontMetrics = true;
        }
        if (needFontMetrics && this.mCachedFm == null) {
            this.mCachedFm = new Paint.FontMetricsInt();
        }
        ReplacementSpan replacement = null;
        if (spans != null) {
            for (MetricAffectingSpan span : spans) {
                if (span instanceof ReplacementSpan) {
                    replacement = (ReplacementSpan) span;
                } else {
                    span.updateMeasureState(this.mCachedPaint);
                }
            }
        }
        int i = this.mTextStart;
        int startInCopiedBuffer = start - i;
        int endInCopiedBuffer = end - i;
        if (builder != null) {
            this.mCachedPaint.getFontMetricsInt(this.mCachedFm);
        }
        if (replacement != null) {
            applyReplacementRun(replacement, startInCopiedBuffer, endInCopiedBuffer, builder);
        } else {
            applyStyleRun(startInCopiedBuffer, endInCopiedBuffer, builder);
        }
        if (needFontMetrics) {
            if (this.mCachedPaint.baselineShift < 0) {
                this.mCachedFm.ascent += this.mCachedPaint.baselineShift;
                this.mCachedFm.top += this.mCachedPaint.baselineShift;
            } else {
                this.mCachedFm.descent += this.mCachedPaint.baselineShift;
                this.mCachedFm.bottom += this.mCachedPaint.baselineShift;
            }
            this.mFontMetrics.append(this.mCachedFm.top);
            this.mFontMetrics.append(this.mCachedFm.bottom);
            this.mFontMetrics.append(this.mCachedFm.ascent);
            this.mFontMetrics.append(this.mCachedFm.descent);
        }
    }

    /* access modifiers changed from: package-private */
    public int breakText(int limit, boolean forwards, float width) {
        float[] w = this.mWidths.getRawArray();
        if (forwards) {
            int i = 0;
            while (i < limit) {
                width -= w[i];
                if (width < 0.0f) {
                    break;
                }
                i++;
            }
            while (i > 0 && this.mCopiedBuffer[i - 1] == ' ') {
                i--;
            }
            return i;
        }
        int i2 = limit - 1;
        while (i2 >= 0) {
            width -= w[i2];
            if (width < 0.0f) {
                break;
            }
            i2--;
        }
        while (i2 < limit - 1 && (this.mCopiedBuffer[i2 + 1] == ' ' || w[i2 + 1] == 0.0f)) {
            i2++;
        }
        return (limit - i2) - 1;
    }

    /* access modifiers changed from: package-private */
    public float measure(int start, int limit) {
        float width = 0.0f;
        float[] w = this.mWidths.getRawArray();
        for (int i = start; i < limit; i++) {
            width += w[i];
        }
        return width;
    }

    public int getMemoryUsage() {
        return this.mMeasuredText.getMemoryUsage();
    }
}

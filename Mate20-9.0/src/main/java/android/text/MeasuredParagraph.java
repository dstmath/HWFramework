package android.text;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Trace;
import android.text.AutoGrowArray;
import android.text.Layout;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import android.util.Pools;
import java.util.Arrays;
import libcore.util.NativeAllocationRegistry;

public class MeasuredParagraph {
    private static final char OBJECT_REPLACEMENT_CHARACTER = '￼';
    private static final Pools.SynchronizedPool<MeasuredParagraph> sPool = new Pools.SynchronizedPool<>(1);
    private static final NativeAllocationRegistry sRegistry;
    private Paint.FontMetricsInt mCachedFm;
    private TextPaint mCachedPaint = new TextPaint();
    private char[] mCopiedBuffer;
    private AutoGrowArray.IntArray mFontMetrics = new AutoGrowArray.IntArray(16);
    private AutoGrowArray.ByteArray mLevels = new AutoGrowArray.ByteArray();
    private boolean mLtrWithoutBidi;
    private Runnable mNativeObjectCleaner;
    private long mNativePtr = 0;
    private int mParaDir;
    private AutoGrowArray.IntArray mSpanEndCache = new AutoGrowArray.IntArray(4);
    private Spanned mSpanned;
    private int mTextLength;
    private int mTextStart;
    private float mWholeWidth;
    private AutoGrowArray.FloatArray mWidths = new AutoGrowArray.FloatArray();

    private static native void nAddReplacementRun(long j, long j2, int i, int i2, float f);

    private static native void nAddStyleRun(long j, long j2, int i, int i2, boolean z);

    private static native long nBuildNativeMeasuredParagraph(long j, char[] cArr, boolean z, boolean z2);

    private static native void nFreeBuilder(long j);

    private static native void nGetBounds(long j, char[] cArr, int i, int i2, Rect rect);

    private static native int nGetMemoryUsage(long j);

    private static native long nGetReleaseFunc();

    private static native float nGetWidth(long j, int i, int i2);

    private static native long nInitBuilder();

    static {
        NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(MeasuredParagraph.class.getClassLoader(), nGetReleaseFunc(), Trace.TRACE_TAG_CAMERA);
        sRegistry = nativeAllocationRegistry;
    }

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

    private void bindNativeObject(long nativePtr) {
        this.mNativePtr = nativePtr;
        this.mNativeObjectCleaner = sRegistry.registerNativeAllocation(this, nativePtr);
    }

    private void unbindNativeObject() {
        if (this.mNativePtr != 0) {
            this.mNativeObjectCleaner.run();
            this.mNativePtr = 0;
        }
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
        unbindNativeObject();
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

    public long getNativePtr() {
        return this.mNativePtr;
    }

    public float getWidth(int start, int end) {
        if (this.mNativePtr != 0) {
            return nGetWidth(this.mNativePtr, start, end);
        }
        float[] widths = this.mWidths.getRawArray();
        float r = 0.0f;
        for (int i = start; i < end; i++) {
            r += widths[i];
        }
        return r;
    }

    public void getBounds(int start, int end, Rect bounds) {
        nGetBounds(this.mNativePtr, this.mCopiedBuffer, start, end, bounds);
    }

    public static MeasuredParagraph buildForBidi(CharSequence text, int start, int end, TextDirectionHeuristic textDir, MeasuredParagraph recycle) {
        MeasuredParagraph mt = recycle == null ? obtain() : recycle;
        mt.resetAndAnalyzeBidi(text, start, end, textDir);
        return mt;
    }

    public static MeasuredParagraph buildForMeasurement(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir, MeasuredParagraph recycle) {
        MeasuredParagraph mt;
        int i = end;
        MeasuredParagraph mt2 = recycle == null ? obtain() : recycle;
        int i2 = start;
        mt2.resetAndAnalyzeBidi(text, i2, i, textDir);
        mt2.mWidths.resize(mt2.mTextLength);
        if (mt2.mTextLength == 0) {
            return mt2;
        }
        if (mt2.mSpanned == null) {
            mt2.applyMetricsAffectingSpan(paint, null, i2, i, 0);
            mt = mt2;
        } else {
            int spanStart = i2;
            while (spanStart < i) {
                int spanEnd = mt2.mSpanned.nextSpanTransition(spanStart, i, MetricAffectingSpan.class);
                mt2.applyMetricsAffectingSpan(paint, (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) mt2.mSpanned.getSpans(spanStart, spanEnd, MetricAffectingSpan.class), mt2.mSpanned, MetricAffectingSpan.class), spanStart, spanEnd, 0);
                spanStart = spanEnd;
                CharSequence charSequence = text;
                int i3 = start;
                TextDirectionHeuristic textDirectionHeuristic = textDir;
                mt2 = mt2;
            }
            mt = mt2;
        }
        return mt;
    }

    public static MeasuredParagraph buildForStaticLayout(TextPaint paint, CharSequence text, int start, int end, TextDirectionHeuristic textDir, boolean computeHyphenation, boolean computeLayout, MeasuredParagraph recycle) {
        long nativeBuilderPtr;
        long nativeBuilderPtr2;
        int i = end;
        boolean z = computeHyphenation;
        boolean z2 = computeLayout;
        MeasuredParagraph mt = recycle == null ? obtain() : recycle;
        int i2 = start;
        mt.resetAndAnalyzeBidi(text, i2, i, textDir);
        if (mt.mTextLength == 0) {
            long nativeBuilderPtr3 = nInitBuilder();
            try {
                mt.bindNativeObject(nBuildNativeMeasuredParagraph(nativeBuilderPtr3, mt.mCopiedBuffer, z, z2));
                return mt;
            } finally {
                nFreeBuilder(nativeBuilderPtr3);
            }
        } else {
            long nativeBuilderPtr4 = nInitBuilder();
            try {
                if (mt.mSpanned == null) {
                    nativeBuilderPtr2 = nativeBuilderPtr4;
                    try {
                        mt.applyMetricsAffectingSpan(paint, null, i2, i, nativeBuilderPtr4);
                        mt.mSpanEndCache.append(i);
                    } catch (Throwable th) {
                        th = th;
                        nativeBuilderPtr = nativeBuilderPtr2;
                        nFreeBuilder(nativeBuilderPtr);
                        throw th;
                    }
                } else {
                    nativeBuilderPtr2 = nativeBuilderPtr4;
                    int spanEnd = i2;
                    while (spanEnd < i) {
                        int spanEnd2 = mt.mSpanned.nextSpanTransition(spanEnd, i, MetricAffectingSpan.class);
                        int i3 = spanEnd;
                        spanEnd = spanEnd2;
                        mt.applyMetricsAffectingSpan(paint, (MetricAffectingSpan[]) TextUtils.removeEmptySpans((MetricAffectingSpan[]) mt.mSpanned.getSpans(spanEnd, spanEnd2, MetricAffectingSpan.class), mt.mSpanned, MetricAffectingSpan.class), spanEnd, spanEnd2, nativeBuilderPtr2);
                        mt.mSpanEndCache.append(spanEnd);
                        int i4 = spanEnd;
                    }
                }
                try {
                    nativeBuilderPtr = nativeBuilderPtr2;
                } catch (Throwable th2) {
                    th = th2;
                    nativeBuilderPtr = nativeBuilderPtr2;
                    nFreeBuilder(nativeBuilderPtr);
                    throw th;
                }
                try {
                    mt.bindNativeObject(nBuildNativeMeasuredParagraph(nativeBuilderPtr, mt.mCopiedBuffer, z, z2));
                    nFreeBuilder(nativeBuilderPtr);
                    return mt;
                } catch (Throwable th3) {
                    th = th3;
                    nFreeBuilder(nativeBuilderPtr);
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                nativeBuilderPtr = nativeBuilderPtr4;
                nFreeBuilder(nativeBuilderPtr);
                throw th;
            }
        }
    }

    private void resetAndAnalyzeBidi(CharSequence text, int start, int end, TextDirectionHeuristic textDir) {
        int bidiRequest;
        reset();
        this.mSpanned = text instanceof Spanned ? (Spanned) text : null;
        this.mTextStart = start;
        this.mTextLength = end - start;
        if (this.mCopiedBuffer == null || this.mCopiedBuffer.length != this.mTextLength) {
            this.mCopiedBuffer = new char[this.mTextLength];
        }
        TextUtils.getChars(text, start, end, this.mCopiedBuffer, 0);
        if (this.mSpanned != null) {
            ReplacementSpan[] spans = (ReplacementSpan[]) this.mSpanned.getSpans(start, end, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int startInPara = this.mSpanned.getSpanStart(spans[i]) - start;
                int endInPara = this.mSpanned.getSpanEnd(spans[i]) - start;
                if (startInPara < 0) {
                    startInPara = 0;
                }
                if (endInPara > this.mTextLength) {
                    endInPara = this.mTextLength;
                }
                Arrays.fill(this.mCopiedBuffer, startInPara, endInPara, OBJECT_REPLACEMENT_CHARACTER);
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

    private void applyReplacementRun(ReplacementSpan replacement, int start, int end, long nativeBuilderPtr) {
        float width = (float) replacement.getSize(this.mCachedPaint, this.mSpanned, start + this.mTextStart, end + this.mTextStart, this.mCachedFm);
        if (nativeBuilderPtr == 0) {
            this.mWidths.set(start, width);
            if (end > start + 1) {
                Arrays.fill(this.mWidths.getRawArray(), start + 1, end, 0.0f);
            }
            this.mWholeWidth += width;
            return;
        }
        nAddReplacementRun(nativeBuilderPtr, this.mCachedPaint.getNativeInstance(), start, end, width);
    }

    private void applyStyleRun(int start, int end, long nativeBuilderPtr) {
        int levelEnd;
        int levelStart = start;
        int i = end;
        if (!this.mLtrWithoutBidi) {
            int levelEnd2 = levelStart + 1;
            byte level = this.mLevels.get(levelStart);
            int levelStart2 = levelStart;
            while (true) {
                int levelEnd3 = levelEnd2;
                if (levelEnd3 == i || this.mLevels.get(levelEnd3) != level) {
                    boolean isRtl = (level & 1) != 0;
                    if (nativeBuilderPtr == 0) {
                        int levelLength = levelEnd3 - levelStart2;
                        this.mWholeWidth += this.mCachedPaint.getTextRunAdvances(this.mCopiedBuffer, levelStart2, levelLength, levelStart2, levelLength, isRtl, this.mWidths.getRawArray(), levelStart2);
                        levelEnd = levelEnd3;
                    } else {
                        levelEnd = levelEnd3;
                        nAddStyleRun(nativeBuilderPtr, this.mCachedPaint.getNativeInstance(), levelStart2, levelEnd3, isRtl);
                    }
                    if (levelEnd != i) {
                        levelStart2 = levelEnd;
                        level = this.mLevels.get(levelEnd);
                    } else {
                        return;
                    }
                } else {
                    levelEnd = levelEnd3;
                }
                levelEnd2 = levelEnd + 1;
            }
        } else if (nativeBuilderPtr == 0) {
            this.mWholeWidth += this.mCachedPaint.getTextRunAdvances(this.mCopiedBuffer, levelStart, i - levelStart, levelStart, i - levelStart, false, this.mWidths.getRawArray(), levelStart);
        } else {
            nAddStyleRun(nativeBuilderPtr, this.mCachedPaint.getNativeInstance(), levelStart, i, false);
        }
    }

    private void applyMetricsAffectingSpan(TextPaint paint, MetricAffectingSpan[] spans, int start, int end, long nativeBuilderPtr) {
        MetricAffectingSpan[] metricAffectingSpanArr = spans;
        long j = nativeBuilderPtr;
        this.mCachedPaint.set(paint);
        this.mCachedPaint.baselineShift = 0;
        boolean needFontMetrics = j != 0;
        if (needFontMetrics && this.mCachedFm == null) {
            this.mCachedFm = new Paint.FontMetricsInt();
        }
        ReplacementSpan replacement = null;
        if (metricAffectingSpanArr != null) {
            for (MetricAffectingSpan span : metricAffectingSpanArr) {
                if (span instanceof ReplacementSpan) {
                    replacement = (ReplacementSpan) span;
                } else {
                    span.updateMeasureState(this.mCachedPaint);
                }
            }
        }
        ReplacementSpan replacement2 = replacement;
        int startInCopiedBuffer = start - this.mTextStart;
        int endInCopiedBuffer = end - this.mTextStart;
        if (j != 0) {
            this.mCachedPaint.getFontMetricsInt(this.mCachedFm);
        }
        if (replacement2 != null) {
            int i = endInCopiedBuffer;
            applyReplacementRun(replacement2, startInCopiedBuffer, endInCopiedBuffer, j);
        } else {
            applyStyleRun(startInCopiedBuffer, endInCopiedBuffer, j);
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
        float[] w = this.mWidths.getRawArray();
        float width = 0.0f;
        for (int i = start; i < limit; i++) {
            width += w[i];
        }
        return width;
    }

    public int getMemoryUsage() {
        return nGetMemoryUsage(this.mNativePtr);
    }
}

package android.text;

import android.graphics.Paint.FontMetricsInt;
import android.text.StaticLayout.Builder;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import com.android.internal.util.ArrayUtils;

class MeasuredText {
    private static final boolean localLOGV = false;
    private static final MeasuredText[] sCached = new MeasuredText[3];
    private static final Object[] sLock = new Object[0];
    private Builder mBuilder;
    char[] mChars;
    int mDir;
    boolean mEasy;
    int mLen;
    byte[] mLevels;
    private int mPos;
    CharSequence mText;
    int mTextStart;
    float[] mWidths;
    private TextPaint mWorkPaint = new TextPaint();

    private MeasuredText() {
    }

    static MeasuredText obtain() {
        synchronized (sLock) {
            int i = sCached.length;
            do {
                i--;
                if (i < 0) {
                    return new MeasuredText();
                }
            } while (sCached[i] == null);
            MeasuredText mt = sCached[i];
            sCached[i] = null;
            return mt;
        }
    }

    static MeasuredText recycle(MeasuredText mt) {
        mt.finish();
        synchronized (sLock) {
            for (int i = 0; i < sCached.length; i++) {
                if (sCached[i] == null) {
                    sCached[i] = mt;
                    mt.mText = null;
                    break;
                }
            }
        }
        return null;
    }

    void finish() {
        this.mText = null;
        this.mBuilder = null;
        if (this.mLen > 1000) {
            this.mWidths = null;
            this.mChars = null;
            this.mLevels = null;
        }
    }

    void setPos(int pos) {
        this.mPos = pos - this.mTextStart;
    }

    void setPara(CharSequence text, int start, int end, TextDirectionHeuristic textDir, Builder builder) {
        this.mBuilder = builder;
        this.mText = text;
        this.mTextStart = start;
        int len = end - start;
        this.mLen = len;
        this.mPos = 0;
        if (this.mWidths == null || this.mWidths.length < len) {
            this.mWidths = ArrayUtils.newUnpaddedFloatArray(len);
        }
        if (this.mChars == null || this.mChars.length < len) {
            this.mChars = ArrayUtils.newUnpaddedCharArray(len);
        }
        TextUtils.getChars(text, start, end, this.mChars, 0);
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            ReplacementSpan[] spans = (ReplacementSpan[]) spanned.getSpans(start, end, ReplacementSpan.class);
            for (int i = 0; i < spans.length; i++) {
                int startInPara = spanned.getSpanStart(spans[i]) - start;
                int endInPara = spanned.getSpanEnd(spans[i]) - start;
                if (startInPara < 0) {
                    startInPara = 0;
                }
                if (endInPara > len) {
                    endInPara = len;
                }
                for (int j = startInPara; j < endInPara; j++) {
                    this.mChars[j] = 65532;
                }
            }
        }
        if ((textDir == TextDirectionHeuristics.LTR || textDir == TextDirectionHeuristics.FIRSTSTRONG_LTR || textDir == TextDirectionHeuristics.ANYRTL_LTR) && TextUtils.doesNotNeedBidi(this.mChars, 0, len)) {
            this.mDir = 1;
            this.mEasy = true;
            return;
        }
        int bidiRequest;
        if (this.mLevels == null || this.mLevels.length < len) {
            this.mLevels = ArrayUtils.newUnpaddedByteArray(len);
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
            bidiRequest = textDir.isRtl(this.mChars, 0, len) ? -1 : 1;
        }
        this.mDir = AndroidBidi.bidi(bidiRequest, this.mChars, this.mLevels, len, false);
        this.mEasy = false;
    }

    float addStyleRun(TextPaint paint, int len, FontMetricsInt fm) {
        if (fm != null) {
            paint.getFontMetricsInt(fm);
        }
        int p = this.mPos;
        this.mPos = p + len;
        float[] widths = null;
        if (this.mBuilder == null || paint.getClass() != TextPaint.class) {
            widths = this.mWidths;
        }
        boolean isRtl;
        if (this.mEasy) {
            float width;
            isRtl = this.mDir != 1;
            if (widths != null) {
                width = paint.getTextRunAdvances(this.mChars, p, len, p, len, isRtl, widths, p);
                if (this.mBuilder != null) {
                    this.mBuilder.addMeasuredRun(p, p + len, widths);
                }
            } else {
                width = this.mBuilder.addStyleRun(paint, p, p + len, isRtl);
            }
            return width;
        }
        float totalAdvance = 0.0f;
        int level = this.mLevels[p];
        int q = p;
        int i = p + 1;
        int e = p + len;
        while (true) {
            if (i == e || this.mLevels[i] != level) {
                isRtl = (level & 1) != 0;
                if (widths != null) {
                    totalAdvance += paint.getTextRunAdvances(this.mChars, q, i - q, q, i - q, isRtl, widths, q);
                    if (this.mBuilder != null) {
                        this.mBuilder.addMeasuredRun(q, i, widths);
                    }
                } else {
                    totalAdvance += this.mBuilder.addStyleRun(paint, q, i, isRtl);
                }
                if (i == e) {
                    return totalAdvance;
                }
                q = i;
                level = this.mLevels[i];
            }
            i++;
        }
    }

    float addStyleRun(TextPaint paint, MetricAffectingSpan[] spans, int len, FontMetricsInt fm) {
        int i;
        float wid;
        TextPaint workPaint = this.mWorkPaint;
        workPaint.set(paint);
        workPaint.baselineShift = 0;
        ReplacementSpan replacement = null;
        for (MetricAffectingSpan span : spans) {
            if (span instanceof ReplacementSpan) {
                replacement = (ReplacementSpan) span;
            } else {
                span.updateMeasureState(workPaint);
            }
        }
        if (replacement == null) {
            wid = addStyleRun(workPaint, len, fm);
        } else {
            wid = (float) replacement.getSize(workPaint, this.mText, this.mTextStart + this.mPos, (this.mTextStart + this.mPos) + len, fm);
            if (this.mBuilder == null) {
                float[] w = this.mWidths;
                w[this.mPos] = wid;
                int e = this.mPos + len;
                for (i = this.mPos + 1; i < e; i++) {
                    w[i] = 0.0f;
                }
            } else {
                this.mBuilder.addReplacementRun(this.mPos, this.mPos + len, wid);
            }
            this.mPos += len;
        }
        if (fm != null) {
            if (workPaint.baselineShift < 0) {
                fm.ascent += workPaint.baselineShift;
                fm.top += workPaint.baselineShift;
            } else {
                fm.descent += workPaint.baselineShift;
                fm.bottom += workPaint.baselineShift;
            }
        }
        return wid;
    }

    int breakText(int limit, boolean forwards, float width) {
        float[] w = this.mWidths;
        int i;
        if (forwards) {
            i = 0;
            while (i < limit) {
                width -= w[i];
                if (width < 0.0f) {
                    break;
                }
                i++;
            }
            while (i > 0 && this.mChars[i - 1] == ' ') {
                i--;
            }
            return i;
        }
        i = limit - 1;
        while (i >= 0) {
            width -= w[i];
            if (width < 0.0f) {
                break;
            }
            i--;
        }
        while (i < limit - 1 && (this.mChars[i + 1] == ' ' || w[i + 1] == 0.0f)) {
            i++;
        }
        return (limit - i) - 1;
    }

    float measure(int start, int limit) {
        float width = 0.0f;
        float[] w = this.mWidths;
        for (int i = start; i < limit; i++) {
            width += w[i];
        }
        return width;
    }
}

package android.text;

import android.graphics.Rect;
import android.text.style.MetricAffectingSpan;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Objects;

public class PrecomputedText implements Spannable {
    private static final char LINE_FEED = '\n';
    private final int mEnd;
    private final ParagraphInfo[] mParagraphInfo;
    private final Params mParams;
    private final int mStart;
    private final SpannableString mText;

    public static class ParagraphInfo {
        public final MeasuredParagraph measured;
        public final int paragraphEnd;

        public ParagraphInfo(int paraEnd, MeasuredParagraph measured2) {
            this.paragraphEnd = paraEnd;
            this.measured = measured2;
        }
    }

    public static final class Params {
        private final int mBreakStrategy;
        private final int mHyphenationFrequency;
        private final TextPaint mPaint;
        private final TextDirectionHeuristic mTextDir;

        public static class Builder {
            private int mBreakStrategy = 1;
            private int mHyphenationFrequency = 1;
            private final TextPaint mPaint;
            private TextDirectionHeuristic mTextDir = TextDirectionHeuristics.FIRSTSTRONG_LTR;

            public Builder(TextPaint paint) {
                this.mPaint = paint;
            }

            public Builder setBreakStrategy(int strategy) {
                this.mBreakStrategy = strategy;
                return this;
            }

            public Builder setHyphenationFrequency(int frequency) {
                this.mHyphenationFrequency = frequency;
                return this;
            }

            public Builder setTextDirection(TextDirectionHeuristic textDir) {
                this.mTextDir = textDir;
                return this;
            }

            public Params build() {
                return new Params(this.mPaint, this.mTextDir, this.mBreakStrategy, this.mHyphenationFrequency);
            }
        }

        public Params(TextPaint paint, TextDirectionHeuristic textDir, int strategy, int frequency) {
            this.mPaint = paint;
            this.mTextDir = textDir;
            this.mBreakStrategy = strategy;
            this.mHyphenationFrequency = frequency;
        }

        public TextPaint getTextPaint() {
            return this.mPaint;
        }

        public TextDirectionHeuristic getTextDirection() {
            return this.mTextDir;
        }

        public int getBreakStrategy() {
            return this.mBreakStrategy;
        }

        public int getHyphenationFrequency() {
            return this.mHyphenationFrequency;
        }

        public boolean isSameTextMetricsInternal(TextPaint paint, TextDirectionHeuristic textDir, int strategy, int frequency) {
            return this.mTextDir == textDir && this.mBreakStrategy == strategy && this.mHyphenationFrequency == frequency && this.mPaint.equalsForTextMeasurement(paint);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null || !(o instanceof Params)) {
                return false;
            }
            Params param = (Params) o;
            return isSameTextMetricsInternal(param.mPaint, param.mTextDir, param.mBreakStrategy, param.mHyphenationFrequency);
        }

        public int hashCode() {
            return Objects.hash(new Object[]{Float.valueOf(this.mPaint.getTextSize()), Float.valueOf(this.mPaint.getTextScaleX()), Float.valueOf(this.mPaint.getTextSkewX()), Float.valueOf(this.mPaint.getLetterSpacing()), Float.valueOf(this.mPaint.getWordSpacing()), Integer.valueOf(this.mPaint.getFlags()), this.mPaint.getTextLocales(), this.mPaint.getTypeface(), this.mPaint.getFontVariationSettings(), Boolean.valueOf(this.mPaint.isElegantTextHeight()), this.mTextDir, Integer.valueOf(this.mBreakStrategy), Integer.valueOf(this.mHyphenationFrequency)});
        }

        public String toString() {
            return "{textSize=" + this.mPaint.getTextSize() + ", textScaleX=" + this.mPaint.getTextScaleX() + ", textSkewX=" + this.mPaint.getTextSkewX() + ", letterSpacing=" + this.mPaint.getLetterSpacing() + ", textLocale=" + this.mPaint.getTextLocales() + ", typeface=" + this.mPaint.getTypeface() + ", variationSettings=" + this.mPaint.getFontVariationSettings() + ", elegantTextHeight=" + this.mPaint.isElegantTextHeight() + ", textDir=" + this.mTextDir + ", breakStrategy=" + this.mBreakStrategy + ", hyphenationFrequency=" + this.mHyphenationFrequency + "}";
        }
    }

    public static PrecomputedText create(CharSequence text, Params params) {
        CharSequence charSequence = text;
        PrecomputedText precomputedText = new PrecomputedText(charSequence, 0, text.length(), params, createMeasuredParagraphs(text, params, 0, text.length(), true));
        return precomputedText;
    }

    public static ParagraphInfo[] createMeasuredParagraphs(CharSequence text, Params params, int start, int end, boolean computeLayout) {
        int paraEnd;
        int i = end;
        ArrayList<ParagraphInfo> result = new ArrayList<>();
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(params);
        boolean needHyphenation = (params.getBreakStrategy() == 0 || params.getHyphenationFrequency() == 0) ? false : true;
        int paraStart = start;
        while (paraStart < i) {
            CharSequence charSequence = text;
            int paraEnd2 = TextUtils.indexOf(charSequence, (char) LINE_FEED, paraStart, i);
            if (paraEnd2 < 0) {
                paraEnd = i;
            } else {
                paraEnd = paraEnd2 + 1;
            }
            int paraEnd3 = paraEnd;
            result.add(new ParagraphInfo(paraEnd3, MeasuredParagraph.buildForStaticLayout(params.getTextPaint(), charSequence, paraStart, paraEnd3, params.getTextDirection(), needHyphenation, computeLayout, null)));
            paraStart = paraEnd3;
            int i2 = paraEnd3;
        }
        CharSequence charSequence2 = text;
        return (ParagraphInfo[]) result.toArray(new ParagraphInfo[result.size()]);
    }

    private PrecomputedText(CharSequence text, int start, int end, Params params, ParagraphInfo[] paraInfo) {
        this.mText = new SpannableString(text, true);
        this.mStart = start;
        this.mEnd = end;
        this.mParams = params;
        this.mParagraphInfo = paraInfo;
    }

    public CharSequence getText() {
        return this.mText;
    }

    public int getStart() {
        return this.mStart;
    }

    public int getEnd() {
        return this.mEnd;
    }

    public Params getParams() {
        return this.mParams;
    }

    public int getParagraphCount() {
        return this.mParagraphInfo.length;
    }

    public int getParagraphStart(int paraIndex) {
        Preconditions.checkArgumentInRange(paraIndex, 0, getParagraphCount(), "paraIndex");
        return paraIndex == 0 ? this.mStart : getParagraphEnd(paraIndex - 1);
    }

    public int getParagraphEnd(int paraIndex) {
        Preconditions.checkArgumentInRange(paraIndex, 0, getParagraphCount(), "paraIndex");
        return this.mParagraphInfo[paraIndex].paragraphEnd;
    }

    public MeasuredParagraph getMeasuredParagraph(int paraIndex) {
        return this.mParagraphInfo[paraIndex].measured;
    }

    public ParagraphInfo[] getParagraphInfo() {
        return this.mParagraphInfo;
    }

    public boolean canUseMeasuredResult(int start, int end, TextDirectionHeuristic textDir, TextPaint paint, int strategy, int frequency) {
        TextPaint textPaint = this.mParams.getTextPaint();
        return this.mStart == start && this.mEnd == end && this.mParams.isSameTextMetricsInternal(paint, textDir, strategy, frequency);
    }

    public int findParaIndex(int pos) {
        for (int i = 0; i < this.mParagraphInfo.length; i++) {
            if (pos < this.mParagraphInfo[i].paragraphEnd) {
                return i;
            }
        }
        throw new IndexOutOfBoundsException("pos must be less than " + this.mParagraphInfo[this.mParagraphInfo.length - 1].paragraphEnd + ", gave " + pos);
    }

    public float getWidth(int start, int end) {
        boolean z = false;
        Preconditions.checkArgument(start >= 0 && start <= this.mText.length(), "invalid start offset");
        Preconditions.checkArgument(end >= 0 && end <= this.mText.length(), "invalid end offset");
        if (start <= end) {
            z = true;
        }
        Preconditions.checkArgument(z, "start offset can not be larger than end offset");
        if (start == end) {
            return 0.0f;
        }
        int paraIndex = findParaIndex(start);
        int paraStart = getParagraphStart(paraIndex);
        int paraEnd = getParagraphEnd(paraIndex);
        if (start >= paraStart && paraEnd >= end) {
            return getMeasuredParagraph(paraIndex).getWidth(start - paraStart, end - paraStart);
        }
        throw new IllegalArgumentException("Cannot measured across the paragraph:para: (" + paraStart + ", " + paraEnd + "), request: (" + start + ", " + end + ")");
    }

    public void getBounds(int start, int end, Rect bounds) {
        boolean z = true;
        Preconditions.checkArgument(start >= 0 && start <= this.mText.length(), "invalid start offset");
        Preconditions.checkArgument(end >= 0 && end <= this.mText.length(), "invalid end offset");
        if (start > end) {
            z = false;
        }
        Preconditions.checkArgument(z, "start offset can not be larger than end offset");
        Preconditions.checkNotNull(bounds);
        if (start == end) {
            bounds.set(0, 0, 0, 0);
            return;
        }
        int paraIndex = findParaIndex(start);
        int paraStart = getParagraphStart(paraIndex);
        int paraEnd = getParagraphEnd(paraIndex);
        if (start < paraStart || paraEnd < end) {
            throw new IllegalArgumentException("Cannot measured across the paragraph:para: (" + paraStart + ", " + paraEnd + "), request: (" + start + ", " + end + ")");
        }
        getMeasuredParagraph(paraIndex).getBounds(start - paraStart, end - paraStart, bounds);
    }

    public int getMemoryUsage() {
        int r = 0;
        for (int i = 0; i < getParagraphCount(); i++) {
            r += getMeasuredParagraph(i).getMemoryUsage();
        }
        return r;
    }

    public void setSpan(Object what, int start, int end, int flags) {
        if (!(what instanceof MetricAffectingSpan)) {
            this.mText.setSpan(what, start, end, flags);
            return;
        }
        throw new IllegalArgumentException("MetricAffectingSpan can not be set to PrecomputedText.");
    }

    public void removeSpan(Object what) {
        if (!(what instanceof MetricAffectingSpan)) {
            this.mText.removeSpan(what);
            return;
        }
        throw new IllegalArgumentException("MetricAffectingSpan can not be removed from PrecomputedText.");
    }

    public <T> T[] getSpans(int start, int end, Class<T> type) {
        return this.mText.getSpans(start, end, type);
    }

    public int getSpanStart(Object tag) {
        return this.mText.getSpanStart(tag);
    }

    public int getSpanEnd(Object tag) {
        return this.mText.getSpanEnd(tag);
    }

    public int getSpanFlags(Object tag) {
        return this.mText.getSpanFlags(tag);
    }

    public int nextSpanTransition(int start, int limit, Class type) {
        return this.mText.nextSpanTransition(start, limit, type);
    }

    public int length() {
        return this.mText.length();
    }

    public char charAt(int index) {
        return this.mText.charAt(index);
    }

    public CharSequence subSequence(int start, int end) {
        return create(this.mText.subSequence(start, end), this.mParams);
    }

    public String toString() {
        return this.mText.toString();
    }
}

package android.text;

import java.lang.reflect.Array;
import java.util.Arrays;

public class SpanSet<E> {
    private final Class<? extends E> classType;
    int numberOfSpans;
    int[] spanEnds;
    int[] spanFlags;
    int[] spanStarts;
    E[] spans;

    SpanSet(Class<? extends E> type) {
        this.classType = type;
        this.numberOfSpans = 0;
    }

    public void init(Spanned spanned, int start, int limit) {
        if (length > 0 && (this.spans == null || this.spans.length < length)) {
            this.spans = (Object[]) Array.newInstance(this.classType, length);
            this.spanStarts = new int[length];
            this.spanEnds = new int[length];
            this.spanFlags = new int[length];
        }
        int prevNumberOfSpans = this.numberOfSpans;
        this.numberOfSpans = 0;
        for (E span : spanned.getSpans(start, limit, this.classType)) {
            int spanStart = spanned.getSpanStart(span);
            int spanEnd = spanned.getSpanEnd(span);
            if (spanStart != spanEnd) {
                int spanFlag = spanned.getSpanFlags(span);
                this.spans[this.numberOfSpans] = span;
                this.spanStarts[this.numberOfSpans] = spanStart;
                this.spanEnds[this.numberOfSpans] = spanEnd;
                this.spanFlags[this.numberOfSpans] = spanFlag;
                this.numberOfSpans++;
            }
        }
        if (this.numberOfSpans < prevNumberOfSpans) {
            Arrays.fill(this.spans, this.numberOfSpans, prevNumberOfSpans, null);
        }
    }

    public boolean hasSpansIntersecting(int start, int end) {
        int i = 0;
        while (i < this.numberOfSpans) {
            if (this.spanStarts[i] < end && this.spanEnds[i] > start) {
                return true;
            }
            i++;
        }
        return false;
    }

    int getNextTransition(int start, int limit) {
        for (int i = 0; i < this.numberOfSpans; i++) {
            int spanStart = this.spanStarts[i];
            int spanEnd = this.spanEnds[i];
            if (spanStart > start && spanStart < limit) {
                limit = spanStart;
            }
            if (spanEnd > start && spanEnd < limit) {
                limit = spanEnd;
            }
        }
        return limit;
    }

    public void recycle() {
        if (this.spans != null) {
            Arrays.fill(this.spans, 0, this.numberOfSpans, null);
        }
    }
}

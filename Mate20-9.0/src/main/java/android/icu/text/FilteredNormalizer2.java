package android.icu.text;

import android.icu.text.Normalizer;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUUncheckedIOException;
import java.io.IOException;

public class FilteredNormalizer2 extends Normalizer2 {
    private Normalizer2 norm2;
    private UnicodeSet set;

    public FilteredNormalizer2(Normalizer2 n2, UnicodeSet filterSet) {
        this.norm2 = n2;
        this.set = filterSet;
    }

    public StringBuilder normalize(CharSequence src, StringBuilder dest) {
        if (dest != src) {
            dest.setLength(0);
            normalize(src, dest, UnicodeSet.SpanCondition.SIMPLE);
            return dest;
        }
        throw new IllegalArgumentException();
    }

    public Appendable normalize(CharSequence src, Appendable dest) {
        if (dest != src) {
            return normalize(src, dest, UnicodeSet.SpanCondition.SIMPLE);
        }
        throw new IllegalArgumentException();
    }

    public StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second) {
        return normalizeSecondAndAppend(first, second, true);
    }

    public StringBuilder append(StringBuilder first, CharSequence second) {
        return normalizeSecondAndAppend(first, second, false);
    }

    public String getDecomposition(int c) {
        if (this.set.contains(c)) {
            return this.norm2.getDecomposition(c);
        }
        return null;
    }

    public String getRawDecomposition(int c) {
        if (this.set.contains(c)) {
            return this.norm2.getRawDecomposition(c);
        }
        return null;
    }

    public int composePair(int a, int b) {
        if (!this.set.contains(a) || !this.set.contains(b)) {
            return -1;
        }
        return this.norm2.composePair(a, b);
    }

    public int getCombiningClass(int c) {
        if (this.set.contains(c)) {
            return this.norm2.getCombiningClass(c);
        }
        return 0;
    }

    public boolean isNormalized(CharSequence s) {
        UnicodeSet.SpanCondition spanCondition = UnicodeSet.SpanCondition.SIMPLE;
        int prevSpanLimit = 0;
        while (prevSpanLimit < s.length()) {
            int spanLimit = this.set.span(s, prevSpanLimit, spanCondition);
            if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                spanCondition = UnicodeSet.SpanCondition.SIMPLE;
            } else if (!this.norm2.isNormalized(s.subSequence(prevSpanLimit, spanLimit))) {
                return false;
            } else {
                spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
            }
            prevSpanLimit = spanLimit;
        }
        return true;
    }

    public Normalizer.QuickCheckResult quickCheck(CharSequence s) {
        Normalizer.QuickCheckResult result = Normalizer.YES;
        UnicodeSet.SpanCondition spanCondition = UnicodeSet.SpanCondition.SIMPLE;
        int prevSpanLimit = 0;
        while (prevSpanLimit < s.length()) {
            int spanLimit = this.set.span(s, prevSpanLimit, spanCondition);
            if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                spanCondition = UnicodeSet.SpanCondition.SIMPLE;
            } else {
                Normalizer.QuickCheckResult qcResult = this.norm2.quickCheck(s.subSequence(prevSpanLimit, spanLimit));
                if (qcResult == Normalizer.NO) {
                    return qcResult;
                }
                if (qcResult == Normalizer.MAYBE) {
                    result = qcResult;
                }
                spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
            }
            prevSpanLimit = spanLimit;
        }
        return result;
    }

    public int spanQuickCheckYes(CharSequence s) {
        UnicodeSet.SpanCondition spanCondition = UnicodeSet.SpanCondition.SIMPLE;
        int prevSpanLimit = 0;
        while (prevSpanLimit < s.length()) {
            int spanLimit = this.set.span(s, prevSpanLimit, spanCondition);
            if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                spanCondition = UnicodeSet.SpanCondition.SIMPLE;
            } else {
                int yesLimit = this.norm2.spanQuickCheckYes(s.subSequence(prevSpanLimit, spanLimit)) + prevSpanLimit;
                if (yesLimit < spanLimit) {
                    return yesLimit;
                }
                spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
            }
            prevSpanLimit = spanLimit;
        }
        return s.length();
    }

    public boolean hasBoundaryBefore(int c) {
        return !this.set.contains(c) || this.norm2.hasBoundaryBefore(c);
    }

    public boolean hasBoundaryAfter(int c) {
        return !this.set.contains(c) || this.norm2.hasBoundaryAfter(c);
    }

    public boolean isInert(int c) {
        return !this.set.contains(c) || this.norm2.isInert(c);
    }

    private Appendable normalize(CharSequence src, Appendable dest, UnicodeSet.SpanCondition spanCondition) {
        StringBuilder tempDest = new StringBuilder();
        int prevSpanLimit = 0;
        while (prevSpanLimit < src.length()) {
            try {
                int spanLimit = this.set.span(src, prevSpanLimit, spanCondition);
                int spanLength = spanLimit - prevSpanLimit;
                if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                    if (spanLength != 0) {
                        dest.append(src, prevSpanLimit, spanLimit);
                    }
                    spanCondition = UnicodeSet.SpanCondition.SIMPLE;
                } else {
                    if (spanLength != 0) {
                        dest.append(this.norm2.normalize(src.subSequence(prevSpanLimit, spanLimit), tempDest));
                    }
                    spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
                }
                prevSpanLimit = spanLimit;
            } catch (IOException e) {
                throw new ICUUncheckedIOException((Throwable) e);
            }
        }
        return dest;
    }

    private StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second, boolean doNormalize) {
        if (first == second) {
            throw new IllegalArgumentException();
        } else if (first.length() != 0) {
            int prefixLimit = this.set.span(second, 0, UnicodeSet.SpanCondition.SIMPLE);
            if (prefixLimit != 0) {
                CharSequence prefix = second.subSequence(0, prefixLimit);
                int suffixStart = this.set.spanBack(first, Integer.MAX_VALUE, UnicodeSet.SpanCondition.SIMPLE);
                if (suffixStart != 0) {
                    StringBuilder middle = new StringBuilder(first.subSequence(suffixStart, first.length()));
                    if (doNormalize) {
                        this.norm2.normalizeSecondAndAppend(middle, prefix);
                    } else {
                        this.norm2.append(middle, prefix);
                    }
                    first.delete(suffixStart, Integer.MAX_VALUE).append(middle);
                } else if (doNormalize) {
                    this.norm2.normalizeSecondAndAppend(first, prefix);
                } else {
                    this.norm2.append(first, prefix);
                }
            }
            if (prefixLimit < second.length()) {
                CharSequence rest = second.subSequence(prefixLimit, second.length());
                if (doNormalize) {
                    normalize(rest, first, UnicodeSet.SpanCondition.NOT_CONTAINED);
                } else {
                    first.append(rest);
                }
            }
            return first;
        } else if (doNormalize) {
            return normalize(second, first);
        } else {
            first.append(second);
            return first;
        }
    }
}

package android.icu.text;

import android.icu.text.Normalizer.QuickCheckResult;
import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.ICUUncheckedIOException;

public class FilteredNormalizer2 extends Normalizer2 {
    private Normalizer2 norm2;
    private UnicodeSet set;

    public FilteredNormalizer2(Normalizer2 n2, UnicodeSet filterSet) {
        this.norm2 = n2;
        this.set = filterSet;
    }

    public StringBuilder normalize(CharSequence src, StringBuilder dest) {
        if (dest == src) {
            throw new IllegalArgumentException();
        }
        dest.setLength(0);
        normalize(src, dest, SpanCondition.SIMPLE);
        return dest;
    }

    public Appendable normalize(CharSequence src, Appendable dest) {
        if (dest != src) {
            return normalize(src, dest, SpanCondition.SIMPLE);
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
        return this.set.contains(c) ? this.norm2.getDecomposition(c) : null;
    }

    public String getRawDecomposition(int c) {
        return this.set.contains(c) ? this.norm2.getRawDecomposition(c) : null;
    }

    public int composePair(int a, int b) {
        return (this.set.contains(a) && this.set.contains(b)) ? this.norm2.composePair(a, b) : -1;
    }

    public int getCombiningClass(int c) {
        return this.set.contains(c) ? this.norm2.getCombiningClass(c) : 0;
    }

    public boolean isNormalized(CharSequence s) {
        SpanCondition spanCondition = SpanCondition.SIMPLE;
        int prevSpanLimit = 0;
        while (prevSpanLimit < s.length()) {
            int spanLimit = this.set.span(s, prevSpanLimit, spanCondition);
            if (spanCondition == SpanCondition.NOT_CONTAINED) {
                spanCondition = SpanCondition.SIMPLE;
            } else if (!this.norm2.isNormalized(s.subSequence(prevSpanLimit, spanLimit))) {
                return false;
            } else {
                spanCondition = SpanCondition.NOT_CONTAINED;
            }
            prevSpanLimit = spanLimit;
        }
        return true;
    }

    public QuickCheckResult quickCheck(CharSequence s) {
        QuickCheckResult result = Normalizer.YES;
        SpanCondition spanCondition = SpanCondition.SIMPLE;
        int prevSpanLimit = 0;
        while (prevSpanLimit < s.length()) {
            int spanLimit = this.set.span(s, prevSpanLimit, spanCondition);
            if (spanCondition == SpanCondition.NOT_CONTAINED) {
                spanCondition = SpanCondition.SIMPLE;
            } else {
                QuickCheckResult qcResult = this.norm2.quickCheck(s.subSequence(prevSpanLimit, spanLimit));
                if (qcResult == Normalizer.NO) {
                    return qcResult;
                }
                if (qcResult == Normalizer.MAYBE) {
                    result = qcResult;
                }
                spanCondition = SpanCondition.NOT_CONTAINED;
            }
            prevSpanLimit = spanLimit;
        }
        return result;
    }

    public int spanQuickCheckYes(CharSequence s) {
        SpanCondition spanCondition = SpanCondition.SIMPLE;
        int prevSpanLimit = 0;
        while (prevSpanLimit < s.length()) {
            int spanLimit = this.set.span(s, prevSpanLimit, spanCondition);
            if (spanCondition == SpanCondition.NOT_CONTAINED) {
                spanCondition = SpanCondition.SIMPLE;
            } else {
                int yesLimit = prevSpanLimit + this.norm2.spanQuickCheckYes(s.subSequence(prevSpanLimit, spanLimit));
                if (yesLimit < spanLimit) {
                    return yesLimit;
                }
                spanCondition = SpanCondition.NOT_CONTAINED;
            }
            prevSpanLimit = spanLimit;
        }
        return s.length();
    }

    public boolean hasBoundaryBefore(int c) {
        return this.set.contains(c) ? this.norm2.hasBoundaryBefore(c) : true;
    }

    public boolean hasBoundaryAfter(int c) {
        return this.set.contains(c) ? this.norm2.hasBoundaryAfter(c) : true;
    }

    public boolean isInert(int c) {
        return this.set.contains(c) ? this.norm2.isInert(c) : true;
    }

    private Appendable normalize(CharSequence src, Appendable dest, SpanCondition spanCondition) {
        StringBuilder tempDest = new StringBuilder();
        int prevSpanLimit = 0;
        while (prevSpanLimit < src.length()) {
            try {
                int spanLimit = this.set.span(src, prevSpanLimit, spanCondition);
                int spanLength = spanLimit - prevSpanLimit;
                if (spanCondition == SpanCondition.NOT_CONTAINED) {
                    if (spanLength != 0) {
                        dest.append(src, prevSpanLimit, spanLimit);
                    }
                    spanCondition = SpanCondition.SIMPLE;
                } else {
                    if (spanLength != 0) {
                        dest.append(this.norm2.normalize(src.subSequence(prevSpanLimit, spanLimit), tempDest));
                    }
                    spanCondition = SpanCondition.NOT_CONTAINED;
                }
                prevSpanLimit = spanLimit;
            } catch (Throwable e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        return dest;
    }

    private StringBuilder normalizeSecondAndAppend(StringBuilder first, CharSequence second, boolean doNormalize) {
        if (first == second) {
            throw new IllegalArgumentException();
        } else if (first.length() != 0) {
            int prefixLimit = this.set.span(second, 0, SpanCondition.SIMPLE);
            if (prefixLimit != 0) {
                CharSequence prefix = second.subSequence(0, prefixLimit);
                int suffixStart = this.set.spanBack(first, Integer.MAX_VALUE, SpanCondition.SIMPLE);
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
                    normalize(rest, first, SpanCondition.NOT_CONTAINED);
                } else {
                    first.append(rest);
                }
            }
            return first;
        } else if (doNormalize) {
            return normalize(second, first);
        } else {
            return first.append(second);
        }
    }
}

package ohos.global.icu.text;

import java.io.IOException;
import ohos.global.icu.text.Normalizer;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ICUUncheckedIOException;

public class FilteredNormalizer2 extends Normalizer2 {
    private Normalizer2 norm2;
    private UnicodeSet set;

    public FilteredNormalizer2(Normalizer2 normalizer2, UnicodeSet unicodeSet) {
        this.norm2 = normalizer2;
        this.set = unicodeSet;
    }

    @Override // ohos.global.icu.text.Normalizer2
    public StringBuilder normalize(CharSequence charSequence, StringBuilder sb) {
        if (sb != charSequence) {
            sb.setLength(0);
            normalize(charSequence, sb, UnicodeSet.SpanCondition.SIMPLE);
            return sb;
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.global.icu.text.Normalizer2
    public Appendable normalize(CharSequence charSequence, Appendable appendable) {
        if (appendable != charSequence) {
            return normalize(charSequence, appendable, UnicodeSet.SpanCondition.SIMPLE);
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.global.icu.text.Normalizer2
    public StringBuilder normalizeSecondAndAppend(StringBuilder sb, CharSequence charSequence) {
        return normalizeSecondAndAppend(sb, charSequence, true);
    }

    @Override // ohos.global.icu.text.Normalizer2
    public StringBuilder append(StringBuilder sb, CharSequence charSequence) {
        return normalizeSecondAndAppend(sb, charSequence, false);
    }

    @Override // ohos.global.icu.text.Normalizer2
    public String getDecomposition(int i) {
        if (this.set.contains(i)) {
            return this.norm2.getDecomposition(i);
        }
        return null;
    }

    @Override // ohos.global.icu.text.Normalizer2
    public String getRawDecomposition(int i) {
        if (this.set.contains(i)) {
            return this.norm2.getRawDecomposition(i);
        }
        return null;
    }

    @Override // ohos.global.icu.text.Normalizer2
    public int composePair(int i, int i2) {
        if (!this.set.contains(i) || !this.set.contains(i2)) {
            return -1;
        }
        return this.norm2.composePair(i, i2);
    }

    @Override // ohos.global.icu.text.Normalizer2
    public int getCombiningClass(int i) {
        if (this.set.contains(i)) {
            return this.norm2.getCombiningClass(i);
        }
        return 0;
    }

    @Override // ohos.global.icu.text.Normalizer2
    public boolean isNormalized(CharSequence charSequence) {
        UnicodeSet.SpanCondition spanCondition;
        UnicodeSet.SpanCondition spanCondition2 = UnicodeSet.SpanCondition.SIMPLE;
        int i = 0;
        while (i < charSequence.length()) {
            int span = this.set.span(charSequence, i, spanCondition2);
            if (spanCondition2 == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                spanCondition = UnicodeSet.SpanCondition.SIMPLE;
            } else if (!this.norm2.isNormalized(charSequence.subSequence(i, span))) {
                return false;
            } else {
                spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
            }
            spanCondition2 = spanCondition;
            i = span;
        }
        return true;
    }

    @Override // ohos.global.icu.text.Normalizer2
    public Normalizer.QuickCheckResult quickCheck(CharSequence charSequence) {
        Normalizer.QuickCheckResult quickCheckResult = Normalizer.YES;
        UnicodeSet.SpanCondition spanCondition = UnicodeSet.SpanCondition.SIMPLE;
        int i = 0;
        while (i < charSequence.length()) {
            int span = this.set.span(charSequence, i, spanCondition);
            if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                spanCondition = UnicodeSet.SpanCondition.SIMPLE;
            } else {
                Normalizer.QuickCheckResult quickCheck = this.norm2.quickCheck(charSequence.subSequence(i, span));
                if (quickCheck == Normalizer.NO) {
                    return quickCheck;
                }
                if (quickCheck == Normalizer.MAYBE) {
                    quickCheckResult = quickCheck;
                }
                spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
            }
            i = span;
        }
        return quickCheckResult;
    }

    @Override // ohos.global.icu.text.Normalizer2
    public int spanQuickCheckYes(CharSequence charSequence) {
        UnicodeSet.SpanCondition spanCondition = UnicodeSet.SpanCondition.SIMPLE;
        int i = 0;
        while (i < charSequence.length()) {
            int span = this.set.span(charSequence, i, spanCondition);
            if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                spanCondition = UnicodeSet.SpanCondition.SIMPLE;
            } else {
                int spanQuickCheckYes = i + this.norm2.spanQuickCheckYes(charSequence.subSequence(i, span));
                if (spanQuickCheckYes < span) {
                    return spanQuickCheckYes;
                }
                spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
            }
            i = span;
        }
        return charSequence.length();
    }

    @Override // ohos.global.icu.text.Normalizer2
    public boolean hasBoundaryBefore(int i) {
        return !this.set.contains(i) || this.norm2.hasBoundaryBefore(i);
    }

    @Override // ohos.global.icu.text.Normalizer2
    public boolean hasBoundaryAfter(int i) {
        return !this.set.contains(i) || this.norm2.hasBoundaryAfter(i);
    }

    @Override // ohos.global.icu.text.Normalizer2
    public boolean isInert(int i) {
        return !this.set.contains(i) || this.norm2.isInert(i);
    }

    private Appendable normalize(CharSequence charSequence, Appendable appendable, UnicodeSet.SpanCondition spanCondition) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < charSequence.length()) {
            try {
                int span = this.set.span(charSequence, i, spanCondition);
                int i2 = span - i;
                if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                    if (i2 != 0) {
                        appendable.append(charSequence, i, span);
                    }
                    spanCondition = UnicodeSet.SpanCondition.SIMPLE;
                } else {
                    if (i2 != 0) {
                        appendable.append(this.norm2.normalize(charSequence.subSequence(i, span), sb));
                    }
                    spanCondition = UnicodeSet.SpanCondition.NOT_CONTAINED;
                }
                i = span;
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        return appendable;
    }

    private StringBuilder normalizeSecondAndAppend(StringBuilder sb, CharSequence charSequence, boolean z) {
        if (sb == charSequence) {
            throw new IllegalArgumentException();
        } else if (sb.length() != 0) {
            int span = this.set.span(charSequence, 0, UnicodeSet.SpanCondition.SIMPLE);
            if (span != 0) {
                CharSequence subSequence = charSequence.subSequence(0, span);
                int spanBack = this.set.spanBack(sb, Integer.MAX_VALUE, UnicodeSet.SpanCondition.SIMPLE);
                if (spanBack != 0) {
                    StringBuilder sb2 = new StringBuilder(sb.subSequence(spanBack, sb.length()));
                    if (z) {
                        this.norm2.normalizeSecondAndAppend(sb2, subSequence);
                    } else {
                        this.norm2.append(sb2, subSequence);
                    }
                    sb.delete(spanBack, Integer.MAX_VALUE).append((CharSequence) sb2);
                } else if (z) {
                    this.norm2.normalizeSecondAndAppend(sb, subSequence);
                } else {
                    this.norm2.append(sb, subSequence);
                }
            }
            if (span < charSequence.length()) {
                CharSequence subSequence2 = charSequence.subSequence(span, charSequence.length());
                if (z) {
                    normalize(subSequence2, sb, UnicodeSet.SpanCondition.NOT_CONTAINED);
                } else {
                    sb.append(subSequence2);
                }
            }
            return sb;
        } else if (z) {
            return normalize(charSequence, sb);
        } else {
            sb.append(charSequence);
            return sb;
        }
    }
}

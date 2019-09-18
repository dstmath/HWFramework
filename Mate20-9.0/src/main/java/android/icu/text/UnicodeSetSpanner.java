package android.icu.text;

import android.icu.text.UnicodeSet;
import android.icu.util.OutputInt;

public class UnicodeSetSpanner {
    private final UnicodeSet unicodeSet;

    public enum CountMethod {
        WHOLE_SPAN,
        MIN_ELEMENTS
    }

    public enum TrimOption {
        LEADING,
        BOTH,
        TRAILING
    }

    public UnicodeSetSpanner(UnicodeSet source) {
        this.unicodeSet = source;
    }

    public UnicodeSet getUnicodeSet() {
        return this.unicodeSet;
    }

    public boolean equals(Object other) {
        return (other instanceof UnicodeSetSpanner) && this.unicodeSet.equals(((UnicodeSetSpanner) other).unicodeSet);
    }

    public int hashCode() {
        return this.unicodeSet.hashCode();
    }

    public int countIn(CharSequence sequence) {
        return countIn(sequence, CountMethod.MIN_ELEMENTS, UnicodeSet.SpanCondition.SIMPLE);
    }

    public int countIn(CharSequence sequence, CountMethod countMethod) {
        return countIn(sequence, countMethod, UnicodeSet.SpanCondition.SIMPLE);
    }

    public int countIn(CharSequence sequence, CountMethod countMethod, UnicodeSet.SpanCondition spanCondition) {
        UnicodeSet.SpanCondition skipSpan;
        int count = 0;
        int start = 0;
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            skipSpan = UnicodeSet.SpanCondition.SIMPLE;
        } else {
            skipSpan = UnicodeSet.SpanCondition.NOT_CONTAINED;
        }
        int length = sequence.length();
        OutputInt spanCount = null;
        while (start != length) {
            int endOfSpan = this.unicodeSet.span(sequence, start, skipSpan);
            if (endOfSpan == length) {
                break;
            } else if (countMethod == CountMethod.WHOLE_SPAN) {
                start = this.unicodeSet.span(sequence, endOfSpan, spanCondition);
                count++;
            } else {
                if (spanCount == null) {
                    spanCount = new OutputInt();
                }
                start = this.unicodeSet.spanAndCount(sequence, endOfSpan, spanCondition, spanCount);
                count += spanCount.value;
            }
        }
        return count;
    }

    public String deleteFrom(CharSequence sequence) {
        return replaceFrom(sequence, "", CountMethod.WHOLE_SPAN, UnicodeSet.SpanCondition.SIMPLE);
    }

    public String deleteFrom(CharSequence sequence, UnicodeSet.SpanCondition spanCondition) {
        return replaceFrom(sequence, "", CountMethod.WHOLE_SPAN, spanCondition);
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement) {
        return replaceFrom(sequence, replacement, CountMethod.MIN_ELEMENTS, UnicodeSet.SpanCondition.SIMPLE);
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement, CountMethod countMethod) {
        return replaceFrom(sequence, replacement, countMethod, UnicodeSet.SpanCondition.SIMPLE);
    }

    public String replaceFrom(CharSequence sequence, CharSequence replacement, CountMethod countMethod, UnicodeSet.SpanCondition spanCondition) {
        UnicodeSet.SpanCondition copySpan;
        int endModify;
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            copySpan = UnicodeSet.SpanCondition.SIMPLE;
        } else {
            copySpan = UnicodeSet.SpanCondition.NOT_CONTAINED;
        }
        int endCopy = 0;
        boolean remove = replacement.length() == 0;
        StringBuilder result = new StringBuilder();
        int length = sequence.length();
        OutputInt spanCount = null;
        while (endCopy != length) {
            if (countMethod == CountMethod.WHOLE_SPAN) {
                endModify = this.unicodeSet.span(sequence, endCopy, spanCondition);
            } else {
                if (spanCount == null) {
                    spanCount = new OutputInt();
                }
                endModify = this.unicodeSet.spanAndCount(sequence, endCopy, spanCondition, spanCount);
            }
            if (!remove && endModify != 0) {
                if (countMethod == CountMethod.WHOLE_SPAN) {
                    result.append(replacement);
                } else {
                    for (int i = spanCount.value; i > 0; i--) {
                        result.append(replacement);
                    }
                }
            }
            if (endModify == length) {
                break;
            }
            endCopy = this.unicodeSet.span(sequence, endModify, copySpan);
            result.append(sequence.subSequence(endModify, endCopy));
        }
        return result.toString();
    }

    public CharSequence trim(CharSequence sequence) {
        return trim(sequence, TrimOption.BOTH, UnicodeSet.SpanCondition.SIMPLE);
    }

    public CharSequence trim(CharSequence sequence, TrimOption trimOption) {
        return trim(sequence, trimOption, UnicodeSet.SpanCondition.SIMPLE);
    }

    public CharSequence trim(CharSequence sequence, TrimOption trimOption, UnicodeSet.SpanCondition spanCondition) {
        int endLeadContained;
        int startTrailContained;
        int length = sequence.length();
        if (trimOption != TrimOption.TRAILING) {
            endLeadContained = this.unicodeSet.span(sequence, spanCondition);
            if (endLeadContained == length) {
                return "";
            }
        } else {
            endLeadContained = 0;
        }
        if (trimOption != TrimOption.LEADING) {
            startTrailContained = this.unicodeSet.spanBack(sequence, spanCondition);
        } else {
            startTrailContained = length;
        }
        return (endLeadContained == 0 && startTrailContained == length) ? sequence : sequence.subSequence(endLeadContained, startTrailContained);
    }
}

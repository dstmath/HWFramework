package ohos.global.icu.text;

import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.OutputInt;

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

    public UnicodeSetSpanner(UnicodeSet unicodeSet2) {
        this.unicodeSet = unicodeSet2;
    }

    public UnicodeSet getUnicodeSet() {
        return this.unicodeSet;
    }

    public boolean equals(Object obj) {
        return (obj instanceof UnicodeSetSpanner) && this.unicodeSet.equals(((UnicodeSetSpanner) obj).unicodeSet);
    }

    public int hashCode() {
        return this.unicodeSet.hashCode();
    }

    public int countIn(CharSequence charSequence) {
        return countIn(charSequence, CountMethod.MIN_ELEMENTS, UnicodeSet.SpanCondition.SIMPLE);
    }

    public int countIn(CharSequence charSequence, CountMethod countMethod) {
        return countIn(charSequence, countMethod, UnicodeSet.SpanCondition.SIMPLE);
    }

    public int countIn(CharSequence charSequence, CountMethod countMethod, UnicodeSet.SpanCondition spanCondition) {
        UnicodeSet.SpanCondition spanCondition2;
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            spanCondition2 = UnicodeSet.SpanCondition.SIMPLE;
        } else {
            spanCondition2 = UnicodeSet.SpanCondition.NOT_CONTAINED;
        }
        int length = charSequence.length();
        int i = 0;
        OutputInt outputInt = null;
        int i2 = 0;
        while (i != length) {
            int span = this.unicodeSet.span(charSequence, i, spanCondition2);
            if (span == length) {
                break;
            } else if (countMethod == CountMethod.WHOLE_SPAN) {
                i = this.unicodeSet.span(charSequence, span, spanCondition);
                i2++;
            } else {
                if (outputInt == null) {
                    outputInt = new OutputInt();
                }
                i = this.unicodeSet.spanAndCount(charSequence, span, spanCondition, outputInt);
                i2 += outputInt.value;
            }
        }
        return i2;
    }

    public String deleteFrom(CharSequence charSequence) {
        return replaceFrom(charSequence, "", CountMethod.WHOLE_SPAN, UnicodeSet.SpanCondition.SIMPLE);
    }

    public String deleteFrom(CharSequence charSequence, UnicodeSet.SpanCondition spanCondition) {
        return replaceFrom(charSequence, "", CountMethod.WHOLE_SPAN, spanCondition);
    }

    public String replaceFrom(CharSequence charSequence, CharSequence charSequence2) {
        return replaceFrom(charSequence, charSequence2, CountMethod.MIN_ELEMENTS, UnicodeSet.SpanCondition.SIMPLE);
    }

    public String replaceFrom(CharSequence charSequence, CharSequence charSequence2, CountMethod countMethod) {
        return replaceFrom(charSequence, charSequence2, countMethod, UnicodeSet.SpanCondition.SIMPLE);
    }

    public String replaceFrom(CharSequence charSequence, CharSequence charSequence2, CountMethod countMethod, UnicodeSet.SpanCondition spanCondition) {
        UnicodeSet.SpanCondition spanCondition2;
        int i;
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            spanCondition2 = UnicodeSet.SpanCondition.SIMPLE;
        } else {
            spanCondition2 = UnicodeSet.SpanCondition.NOT_CONTAINED;
        }
        int i2 = 0;
        boolean z = charSequence2.length() == 0;
        StringBuilder sb = new StringBuilder();
        int length = charSequence.length();
        OutputInt outputInt = null;
        while (i2 != length) {
            if (countMethod == CountMethod.WHOLE_SPAN) {
                i = this.unicodeSet.span(charSequence, i2, spanCondition);
            } else {
                if (outputInt == null) {
                    outputInt = new OutputInt();
                }
                i = this.unicodeSet.spanAndCount(charSequence, i2, spanCondition, outputInt);
            }
            if (!z && i != 0) {
                if (countMethod == CountMethod.WHOLE_SPAN) {
                    sb.append(charSequence2);
                } else {
                    for (int i3 = outputInt.value; i3 > 0; i3--) {
                        sb.append(charSequence2);
                    }
                }
            }
            if (i == length) {
                break;
            }
            int span = this.unicodeSet.span(charSequence, i, spanCondition2);
            sb.append(charSequence.subSequence(i, span));
            i2 = span;
        }
        return sb.toString();
    }

    public CharSequence trim(CharSequence charSequence) {
        return trim(charSequence, TrimOption.BOTH, UnicodeSet.SpanCondition.SIMPLE);
    }

    public CharSequence trim(CharSequence charSequence, TrimOption trimOption) {
        return trim(charSequence, trimOption, UnicodeSet.SpanCondition.SIMPLE);
    }

    public CharSequence trim(CharSequence charSequence, TrimOption trimOption, UnicodeSet.SpanCondition spanCondition) {
        int i;
        int length = charSequence.length();
        if (trimOption != TrimOption.TRAILING) {
            i = this.unicodeSet.span(charSequence, spanCondition);
            if (i == length) {
                return "";
            }
        } else {
            i = 0;
        }
        int spanBack = trimOption != TrimOption.LEADING ? this.unicodeSet.spanBack(charSequence, spanCondition) : length;
        return (i == 0 && spanBack == length) ? charSequence : charSequence.subSequence(i, spanBack);
    }
}

package android.icu.text;

import android.icu.impl.number.Parse;
import android.icu.lang.UCharacter;
import android.icu.text.NumberFormat;
import android.icu.util.ULocale;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public final class ScientificNumberFormatter {
    private static final Style SUPER_SCRIPT = new SuperscriptStyle();
    private final DecimalFormat fmt;
    private final String preExponent;
    private final Style style;

    private static class MarkupStyle extends Style {
        private final String beginMarkup;
        private final String endMarkup;

        MarkupStyle(String beginMarkup2, String endMarkup2) {
            super();
            this.beginMarkup = beginMarkup2;
            this.endMarkup = endMarkup2;
        }

        /* access modifiers changed from: package-private */
        public String format(AttributedCharacterIterator iterator, String preExponent) {
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            iterator.first();
            while (iterator.current() != 65535) {
                Map<AttributedCharacterIterator.Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    append(iterator, copyFromOffset, iterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL), result);
                    copyFromOffset = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                    result.append(this.beginMarkup);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT)) {
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(iterator, copyFromOffset, limit, result);
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                    result.append(this.endMarkup);
                } else {
                    iterator.next();
                }
            }
            append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }
    }

    private static abstract class Style {
        /* access modifiers changed from: package-private */
        public abstract String format(AttributedCharacterIterator attributedCharacterIterator, String str);

        private Style() {
        }

        static void append(AttributedCharacterIterator iterator, int start, int limit, StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            for (int i = start; i < limit; i++) {
                result.append(iterator.current());
                iterator.next();
            }
            iterator.setIndex(oldIndex);
        }
    }

    private static class SuperscriptStyle extends Style {
        private static final char[] SUPERSCRIPT_DIGITS = {8304, 185, 178, 179, 8308, 8309, 8310, 8311, 8312, 8313};
        private static final char SUPERSCRIPT_MINUS_SIGN = '⁻';
        private static final char SUPERSCRIPT_PLUS_SIGN = '⁺';

        private SuperscriptStyle() {
            super();
        }

        /* access modifiers changed from: package-private */
        public String format(AttributedCharacterIterator iterator, String preExponent) {
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            iterator.first();
            while (iterator.current() != 65535) {
                Map<AttributedCharacterIterator.Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    append(iterator, copyFromOffset, iterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL), result);
                    copyFromOffset = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SIGN)) {
                    int start = iterator.getRunStart(NumberFormat.Field.EXPONENT_SIGN);
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SIGN);
                    int aChar = char32AtAndAdvance(iterator);
                    if (Parse.UNISET_MINUS.contains(aChar)) {
                        append(iterator, copyFromOffset, start, result);
                        result.append(SUPERSCRIPT_MINUS_SIGN);
                    } else if (Parse.UNISET_PLUS.contains(aChar)) {
                        append(iterator, copyFromOffset, start, result);
                        result.append(SUPERSCRIPT_PLUS_SIGN);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT)) {
                    int start2 = iterator.getRunStart(NumberFormat.Field.EXPONENT);
                    int limit2 = iterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(iterator, copyFromOffset, start2, result);
                    copyAsSuperscript(iterator, start2, limit2, result);
                    copyFromOffset = limit2;
                    iterator.setIndex(copyFromOffset);
                } else {
                    iterator.next();
                }
            }
            append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }

        private static void copyAsSuperscript(AttributedCharacterIterator iterator, int start, int limit, StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            while (iterator.getIndex() < limit) {
                int digit = UCharacter.digit(char32AtAndAdvance(iterator));
                if (digit >= 0) {
                    result.append(SUPERSCRIPT_DIGITS[digit]);
                } else {
                    throw new IllegalArgumentException();
                }
            }
            iterator.setIndex(oldIndex);
        }

        private static int char32AtAndAdvance(AttributedCharacterIterator iterator) {
            char c1 = iterator.current();
            char c2 = iterator.next();
            if (!UCharacter.isHighSurrogate(c1) || !UCharacter.isLowSurrogate(c2)) {
                return c1;
            }
            iterator.next();
            return UCharacter.toCodePoint(c1, c2);
        }
    }

    public static ScientificNumberFormatter getSuperscriptInstance(ULocale locale) {
        return getInstanceForLocale(locale, SUPER_SCRIPT);
    }

    public static ScientificNumberFormatter getSuperscriptInstance(DecimalFormat df) {
        return getInstance(df, SUPER_SCRIPT);
    }

    public static ScientificNumberFormatter getMarkupInstance(ULocale locale, String beginMarkup, String endMarkup) {
        return getInstanceForLocale(locale, new MarkupStyle(beginMarkup, endMarkup));
    }

    public static ScientificNumberFormatter getMarkupInstance(DecimalFormat df, String beginMarkup, String endMarkup) {
        return getInstance(df, new MarkupStyle(beginMarkup, endMarkup));
    }

    public String format(Object number) {
        String format;
        synchronized (this.fmt) {
            format = this.style.format(this.fmt.formatToCharacterIterator(number), this.preExponent);
        }
        return format;
    }

    private static String getPreExponent(DecimalFormatSymbols dfs) {
        StringBuilder preExponent2 = new StringBuilder();
        preExponent2.append(dfs.getExponentMultiplicationSign());
        char[] digits = dfs.getDigits();
        preExponent2.append(digits[1]);
        preExponent2.append(digits[0]);
        return preExponent2.toString();
    }

    private static ScientificNumberFormatter getInstance(DecimalFormat decimalFormat, Style style2) {
        return new ScientificNumberFormatter((DecimalFormat) decimalFormat.clone(), getPreExponent(decimalFormat.getDecimalFormatSymbols()), style2);
    }

    private static ScientificNumberFormatter getInstanceForLocale(ULocale locale, Style style2) {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getScientificInstance(locale);
        return new ScientificNumberFormatter(decimalFormat, getPreExponent(decimalFormat.getDecimalFormatSymbols()), style2);
    }

    private ScientificNumberFormatter(DecimalFormat decimalFormat, String preExponent2, Style style2) {
        this.fmt = decimalFormat;
        this.preExponent = preExponent2;
        this.style = style2;
    }
}

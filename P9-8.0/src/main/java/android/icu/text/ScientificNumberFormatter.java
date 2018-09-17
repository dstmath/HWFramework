package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.text.NumberFormat.Field;
import android.icu.util.ULocale;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

public final class ScientificNumberFormatter {
    private static final Style SUPER_SCRIPT = new SuperscriptStyle();
    private final DecimalFormat fmt;
    private final String preExponent;
    private final Style style;

    private static abstract class Style {
        /* synthetic */ Style(Style -this0) {
            this();
        }

        abstract String format(AttributedCharacterIterator attributedCharacterIterator, String str);

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

    private static class MarkupStyle extends Style {
        private final String beginMarkup;
        private final String endMarkup;

        MarkupStyle(String beginMarkup, String endMarkup) {
            super();
            this.beginMarkup = beginMarkup;
            this.endMarkup = endMarkup;
        }

        String format(AttributedCharacterIterator iterator, String preExponent) {
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            iterator.first();
            while (iterator.current() != 65535) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(Field.EXPONENT_SYMBOL)) {
                    Style.append(iterator, copyFromOffset, iterator.getRunStart(Field.EXPONENT_SYMBOL), result);
                    copyFromOffset = iterator.getRunLimit(Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                    result.append(this.beginMarkup);
                } else if (attributeSet.containsKey(Field.EXPONENT)) {
                    int limit = iterator.getRunLimit(Field.EXPONENT);
                    Style.append(iterator, copyFromOffset, limit, result);
                    copyFromOffset = limit;
                    iterator.setIndex(limit);
                    result.append(this.endMarkup);
                } else {
                    iterator.next();
                }
            }
            Style.append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }
    }

    private static class SuperscriptStyle extends Style {
        private static final char[] SUPERSCRIPT_DIGITS = new char[]{8304, 185, 178, 179, 8308, 8309, 8310, 8311, 8312, 8313};
        private static final char SUPERSCRIPT_MINUS_SIGN = '⁻';
        private static final char SUPERSCRIPT_PLUS_SIGN = '⁺';

        /* synthetic */ SuperscriptStyle(SuperscriptStyle -this0) {
            this();
        }

        private SuperscriptStyle() {
            super();
        }

        String format(AttributedCharacterIterator iterator, String preExponent) {
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            iterator.first();
            while (iterator.current() != 65535) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                int start;
                int limit;
                if (attributeSet.containsKey(Field.EXPONENT_SYMBOL)) {
                    Style.append(iterator, copyFromOffset, iterator.getRunStart(Field.EXPONENT_SYMBOL), result);
                    copyFromOffset = iterator.getRunLimit(Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                } else if (attributeSet.containsKey(Field.EXPONENT_SIGN)) {
                    start = iterator.getRunStart(Field.EXPONENT_SIGN);
                    limit = iterator.getRunLimit(Field.EXPONENT_SIGN);
                    int aChar = char32AtAndAdvance(iterator);
                    if (DecimalFormat.minusSigns.contains(aChar)) {
                        Style.append(iterator, copyFromOffset, start, result);
                        result.append(SUPERSCRIPT_MINUS_SIGN);
                    } else if (DecimalFormat.plusSigns.contains(aChar)) {
                        Style.append(iterator, copyFromOffset, start, result);
                        result.append(SUPERSCRIPT_PLUS_SIGN);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    copyFromOffset = limit;
                    iterator.setIndex(limit);
                } else if (attributeSet.containsKey(Field.EXPONENT)) {
                    start = iterator.getRunStart(Field.EXPONENT);
                    limit = iterator.getRunLimit(Field.EXPONENT);
                    Style.append(iterator, copyFromOffset, start, result);
                    copyAsSuperscript(iterator, start, limit, result);
                    copyFromOffset = limit;
                    iterator.setIndex(limit);
                } else {
                    iterator.next();
                }
            }
            Style.append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }

        private static void copyAsSuperscript(AttributedCharacterIterator iterator, int start, int limit, StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            while (iterator.getIndex() < limit) {
                int digit = UCharacter.digit(char32AtAndAdvance(iterator));
                if (digit < 0) {
                    throw new IllegalArgumentException();
                }
                result.append(SUPERSCRIPT_DIGITS[digit]);
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
        StringBuilder preExponent = new StringBuilder();
        preExponent.append(dfs.getExponentMultiplicationSign());
        char[] digits = dfs.getDigits();
        preExponent.append(digits[1]).append(digits[0]);
        return preExponent.toString();
    }

    private static ScientificNumberFormatter getInstance(DecimalFormat decimalFormat, Style style) {
        return new ScientificNumberFormatter((DecimalFormat) decimalFormat.clone(), getPreExponent(decimalFormat.getDecimalFormatSymbols()), style);
    }

    private static ScientificNumberFormatter getInstanceForLocale(ULocale locale, Style style) {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getScientificInstance(locale);
        return new ScientificNumberFormatter(decimalFormat, getPreExponent(decimalFormat.getDecimalFormatSymbols()), style);
    }

    private ScientificNumberFormatter(DecimalFormat decimalFormat, String preExponent, Style style) {
        this.fmt = decimalFormat;
        this.preExponent = preExponent;
        this.style = style;
    }
}

package ohos.global.icu.text;

import java.text.AttributedCharacterIterator;
import java.util.Map;
import ohos.global.icu.impl.StaticUnicodeSets;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.util.ULocale;

public final class ScientificNumberFormatter {
    private static final Style SUPER_SCRIPT = new SuperscriptStyle();
    private final DecimalFormat fmt;
    private final String preExponent;
    private final Style style;

    public static ScientificNumberFormatter getSuperscriptInstance(ULocale uLocale) {
        return getInstanceForLocale(uLocale, SUPER_SCRIPT);
    }

    public static ScientificNumberFormatter getSuperscriptInstance(DecimalFormat decimalFormat) {
        return getInstance(decimalFormat, SUPER_SCRIPT);
    }

    public static ScientificNumberFormatter getMarkupInstance(ULocale uLocale, String str, String str2) {
        return getInstanceForLocale(uLocale, new MarkupStyle(str, str2));
    }

    public static ScientificNumberFormatter getMarkupInstance(DecimalFormat decimalFormat, String str, String str2) {
        return getInstance(decimalFormat, new MarkupStyle(str, str2));
    }

    public String format(Object obj) {
        String format;
        synchronized (this.fmt) {
            format = this.style.format(this.fmt.formatToCharacterIterator(obj), this.preExponent);
        }
        return format;
    }

    /* access modifiers changed from: private */
    public static abstract class Style {
        /* access modifiers changed from: package-private */
        public abstract String format(AttributedCharacterIterator attributedCharacterIterator, String str);

        private Style() {
        }

        static void append(AttributedCharacterIterator attributedCharacterIterator, int i, int i2, StringBuilder sb) {
            int index = attributedCharacterIterator.getIndex();
            attributedCharacterIterator.setIndex(i);
            while (i < i2) {
                sb.append(attributedCharacterIterator.current());
                attributedCharacterIterator.next();
                i++;
            }
            attributedCharacterIterator.setIndex(index);
        }
    }

    private static class MarkupStyle extends Style {
        private final String beginMarkup;
        private final String endMarkup;

        MarkupStyle(String str, String str2) {
            super();
            this.beginMarkup = str;
            this.endMarkup = str2;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.ScientificNumberFormatter.Style
        public String format(AttributedCharacterIterator attributedCharacterIterator, String str) {
            StringBuilder sb = new StringBuilder();
            attributedCharacterIterator.first();
            int i = 0;
            while (attributedCharacterIterator.current() != 65535) {
                Map<AttributedCharacterIterator.Attribute, Object> attributes = attributedCharacterIterator.getAttributes();
                if (attributes.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    append(attributedCharacterIterator, i, attributedCharacterIterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL), sb);
                    i = attributedCharacterIterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    attributedCharacterIterator.setIndex(i);
                    sb.append(str);
                    sb.append(this.beginMarkup);
                } else if (attributes.containsKey(NumberFormat.Field.EXPONENT)) {
                    int runLimit = attributedCharacterIterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(attributedCharacterIterator, i, runLimit, sb);
                    attributedCharacterIterator.setIndex(runLimit);
                    sb.append(this.endMarkup);
                    i = runLimit;
                } else {
                    attributedCharacterIterator.next();
                }
            }
            append(attributedCharacterIterator, i, attributedCharacterIterator.getEndIndex(), sb);
            return sb.toString();
        }
    }

    private static class SuperscriptStyle extends Style {
        private static final char[] SUPERSCRIPT_DIGITS = {8304, 185, 178, 179, 8308, 8309, 8310, 8311, 8312, 8313};
        private static final char SUPERSCRIPT_MINUS_SIGN = 8315;
        private static final char SUPERSCRIPT_PLUS_SIGN = 8314;

        private SuperscriptStyle() {
            super();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.text.ScientificNumberFormatter.Style
        public String format(AttributedCharacterIterator attributedCharacterIterator, String str) {
            int i;
            StringBuilder sb = new StringBuilder();
            attributedCharacterIterator.first();
            int i2 = 0;
            while (attributedCharacterIterator.current() != 65535) {
                Map<AttributedCharacterIterator.Attribute, Object> attributes = attributedCharacterIterator.getAttributes();
                if (attributes.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    append(attributedCharacterIterator, i2, attributedCharacterIterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL), sb);
                    i2 = attributedCharacterIterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    attributedCharacterIterator.setIndex(i2);
                    sb.append(str);
                } else {
                    if (attributes.containsKey(NumberFormat.Field.EXPONENT_SIGN)) {
                        int runStart = attributedCharacterIterator.getRunStart(NumberFormat.Field.EXPONENT_SIGN);
                        i = attributedCharacterIterator.getRunLimit(NumberFormat.Field.EXPONENT_SIGN);
                        int char32AtAndAdvance = char32AtAndAdvance(attributedCharacterIterator);
                        if (StaticUnicodeSets.get(StaticUnicodeSets.Key.MINUS_SIGN).contains(char32AtAndAdvance)) {
                            append(attributedCharacterIterator, i2, runStart, sb);
                            sb.append(SUPERSCRIPT_MINUS_SIGN);
                        } else if (StaticUnicodeSets.get(StaticUnicodeSets.Key.PLUS_SIGN).contains(char32AtAndAdvance)) {
                            append(attributedCharacterIterator, i2, runStart, sb);
                            sb.append(SUPERSCRIPT_PLUS_SIGN);
                        } else {
                            throw new IllegalArgumentException();
                        }
                        attributedCharacterIterator.setIndex(i);
                    } else if (attributes.containsKey(NumberFormat.Field.EXPONENT)) {
                        int runStart2 = attributedCharacterIterator.getRunStart(NumberFormat.Field.EXPONENT);
                        i = attributedCharacterIterator.getRunLimit(NumberFormat.Field.EXPONENT);
                        append(attributedCharacterIterator, i2, runStart2, sb);
                        copyAsSuperscript(attributedCharacterIterator, runStart2, i, sb);
                        attributedCharacterIterator.setIndex(i);
                    } else {
                        attributedCharacterIterator.next();
                    }
                    i2 = i;
                }
            }
            append(attributedCharacterIterator, i2, attributedCharacterIterator.getEndIndex(), sb);
            return sb.toString();
        }

        private static void copyAsSuperscript(AttributedCharacterIterator attributedCharacterIterator, int i, int i2, StringBuilder sb) {
            int index = attributedCharacterIterator.getIndex();
            attributedCharacterIterator.setIndex(i);
            while (attributedCharacterIterator.getIndex() < i2) {
                int digit = UCharacter.digit(char32AtAndAdvance(attributedCharacterIterator));
                if (digit >= 0) {
                    sb.append(SUPERSCRIPT_DIGITS[digit]);
                } else {
                    throw new IllegalArgumentException();
                }
            }
            attributedCharacterIterator.setIndex(index);
        }

        private static int char32AtAndAdvance(AttributedCharacterIterator attributedCharacterIterator) {
            char current = attributedCharacterIterator.current();
            char next = attributedCharacterIterator.next();
            if (!UCharacter.isHighSurrogate(current) || !UCharacter.isLowSurrogate(next)) {
                return current;
            }
            attributedCharacterIterator.next();
            return UCharacter.toCodePoint(current, next);
        }
    }

    private static String getPreExponent(DecimalFormatSymbols decimalFormatSymbols) {
        StringBuilder sb = new StringBuilder();
        sb.append(decimalFormatSymbols.getExponentMultiplicationSign());
        char[] digits = decimalFormatSymbols.getDigits();
        sb.append(digits[1]);
        sb.append(digits[0]);
        return sb.toString();
    }

    private static ScientificNumberFormatter getInstance(DecimalFormat decimalFormat, Style style2) {
        return new ScientificNumberFormatter((DecimalFormat) decimalFormat.clone(), getPreExponent(decimalFormat.getDecimalFormatSymbols()), style2);
    }

    private static ScientificNumberFormatter getInstanceForLocale(ULocale uLocale, Style style2) {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getScientificInstance(uLocale);
        return new ScientificNumberFormatter(decimalFormat, getPreExponent(decimalFormat.getDecimalFormatSymbols()), style2);
    }

    private ScientificNumberFormatter(DecimalFormat decimalFormat, String str, Style style2) {
        this.fmt = decimalFormat;
        this.preExponent = str;
        this.style = style2;
    }
}

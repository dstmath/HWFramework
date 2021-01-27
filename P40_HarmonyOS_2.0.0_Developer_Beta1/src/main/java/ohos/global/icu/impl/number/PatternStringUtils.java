package ohos.global.icu.impl.number;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.impl.number.Padder;
import ohos.global.icu.number.NumberFormatter;
import ohos.global.icu.text.DecimalFormatSymbols;

public class PatternStringUtils {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public static boolean ignoreRoundingIncrement(BigDecimal bigDecimal, int i) {
        double doubleValue = bigDecimal.doubleValue();
        if (doubleValue == XPath.MATCH_SCORE_QNAME) {
            return true;
        }
        if (i < 0) {
            return false;
        }
        double d = doubleValue * 2.0d;
        int i2 = 0;
        while (i2 <= i && d <= 1.0d) {
            i2++;
            d *= 10.0d;
        }
        return i2 > i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x010e A[LOOP:2: B:21:0x0105->B:23:0x010e, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0116 A[LOOP:3: B:24:0x0113->B:26:0x0116, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0129  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0131  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0137  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x013a  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01ac A[LOOP:6: B:66:0x01ac->B:68:0x01b4, LOOP_START, PHI: r1 
      PHI: (r1v9 int) = (r1v5 int), (r1v13 int) binds: [B:65:0x01aa, B:68:0x01b4] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x020b  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0213  */
    public static String propertiesToPatternString(DecimalFormatProperties decimalFormatProperties) {
        int i;
        int i2;
        int min;
        int i3;
        int i4;
        StringBuilder sb = new StringBuilder();
        int max = Math.max(0, Math.min(decimalFormatProperties.getGroupingSize(), 100));
        int max2 = Math.max(0, Math.min(decimalFormatProperties.getSecondaryGroupingSize(), 100));
        boolean groupingUsed = decimalFormatProperties.getGroupingUsed();
        int min2 = Math.min(decimalFormatProperties.getFormatWidth(), 100);
        Padder.PadPosition padPosition = decimalFormatProperties.getPadPosition();
        String padString = decimalFormatProperties.getPadString();
        int max3 = Math.max(0, Math.min(decimalFormatProperties.getMinimumIntegerDigits(), 100));
        int min3 = Math.min(decimalFormatProperties.getMaximumIntegerDigits(), 100);
        int max4 = Math.max(0, Math.min(decimalFormatProperties.getMinimumFractionDigits(), 100));
        int min4 = Math.min(decimalFormatProperties.getMaximumFractionDigits(), 100);
        int min5 = Math.min(decimalFormatProperties.getMinimumSignificantDigits(), 100);
        int min6 = Math.min(decimalFormatProperties.getMaximumSignificantDigits(), 100);
        boolean decimalSeparatorAlwaysShown = decimalFormatProperties.getDecimalSeparatorAlwaysShown();
        int min7 = Math.min(decimalFormatProperties.getMinimumExponentDigits(), 100);
        boolean exponentSignAlwaysShown = decimalFormatProperties.getExponentSignAlwaysShown();
        PropertiesAffixPatternProvider propertiesAffixPatternProvider = new PropertiesAffixPatternProvider(decimalFormatProperties);
        sb.append(propertiesAffixPatternProvider.getString(256));
        int length = sb.length();
        if (!groupingUsed) {
            max = 0;
            max2 = 0;
        } else if (max == max2) {
            max = 0;
        }
        int i5 = max + max2 + 1;
        BigDecimal roundingIncrement = decimalFormatProperties.getRoundingIncrement();
        StringBuilder sb2 = new StringBuilder();
        if (min6 != Math.min(100, -1)) {
            while (sb2.length() < min5) {
                sb2.append('@');
            }
            while (sb2.length() < min6) {
                sb2.append('#');
            }
        } else if (roundingIncrement != null && !ignoreRoundingIncrement(roundingIncrement, min4)) {
            i = -roundingIncrement.scale();
            String plainString = roundingIncrement.scaleByPowerOfTen(roundingIncrement.scale()).toPlainString();
            if (plainString.charAt(0) == '-') {
                sb2.append((CharSequence) plainString, 1, plainString.length());
            } else {
                sb2.append(plainString);
            }
            while (sb2.length() + i < max3) {
                sb2.insert(0, '0');
            }
            while ((-i) < max4) {
                sb2.append('0');
                i--;
            }
            int max5 = Math.max(i5, sb2.length() + i);
            if (min3 != 100) {
                max5 = Math.max(min3, max5);
            }
            min = min4 == 100 ? Math.min(-min4, i) : i;
            for (i2 = max5 - 1; i2 >= min; i2--) {
                int length2 = ((sb2.length() + i) - i2) - 1;
                if (length2 < 0 || length2 >= sb2.length()) {
                    sb.append('#');
                } else {
                    sb.append(sb2.charAt(length2));
                }
                if (i2 == 0 && (decimalSeparatorAlwaysShown || min < 0)) {
                    sb.append('.');
                }
                if (groupingUsed) {
                    if (i2 > 0 && i2 == max) {
                        sb.append(',');
                    }
                    if (i2 > max && max2 > 0 && (i2 - max) % max2 == 0) {
                        sb.append(',');
                    }
                }
            }
            if (min7 != Math.min(100, -1)) {
                sb.append('E');
                if (exponentSignAlwaysShown) {
                    sb.append('+');
                }
                for (int i6 = 0; i6 < min7; i6++) {
                    sb.append('0');
                }
            }
            int length3 = sb.length();
            sb.append(propertiesAffixPatternProvider.getString(0));
            if (min2 <= 0) {
                while (min2 - sb.length() > 0) {
                    sb.insert(length, '#');
                    length3++;
                }
                i3 = length;
                int i7 = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[padPosition.ordinal()];
                if (i7 == 1) {
                    int escapePaddingString = escapePaddingString(padString, sb, 0);
                    sb.insert(0, '*');
                    int i8 = escapePaddingString + 1;
                    i4 = i3 + i8;
                    length3 += i8;
                } else if (i7 == 2) {
                    int escapePaddingString2 = escapePaddingString(padString, sb, i3);
                    sb.insert(i3, '*');
                    int i9 = escapePaddingString2 + 1;
                    i4 = i3 + i9;
                    length3 += i9;
                } else if (i7 == 3) {
                    escapePaddingString(padString, sb, length3);
                    sb.insert(length3, '*');
                } else if (i7 == 4) {
                    sb.append('*');
                    escapePaddingString(padString, sb, sb.length());
                }
                i3 = i4;
            } else {
                i3 = length;
            }
            if (propertiesAffixPatternProvider.hasNegativeSubpattern()) {
                sb.append(';');
                sb.append(propertiesAffixPatternProvider.getString(768));
                sb.append((CharSequence) sb, i3, length3);
                sb.append(propertiesAffixPatternProvider.getString(512));
            }
            return sb.toString();
        }
        i = 0;
        while (sb2.length() + i < max3) {
        }
        while ((-i) < max4) {
        }
        int max52 = Math.max(i5, sb2.length() + i);
        if (min3 != 100) {
        }
        if (min4 == 100) {
        }
        while (i2 >= min) {
        }
        if (min7 != Math.min(100, -1)) {
        }
        int length32 = sb.length();
        sb.append(propertiesAffixPatternProvider.getString(0));
        if (min2 <= 0) {
        }
        if (propertiesAffixPatternProvider.hasNegativeSubpattern()) {
        }
        return sb.toString();
    }

    /* renamed from: ohos.global.icu.impl.number.PatternStringUtils$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition = new int[Padder.PadPosition.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[Padder.PadPosition.BEFORE_PREFIX.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[Padder.PadPosition.AFTER_PREFIX.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[Padder.PadPosition.BEFORE_SUFFIX.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$number$Padder$PadPosition[Padder.PadPosition.AFTER_SUFFIX.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    private static int escapePaddingString(CharSequence charSequence, StringBuilder sb, int i) {
        if (charSequence == null || charSequence.length() == 0) {
            charSequence = " ";
        }
        int length = sb.length();
        int i2 = 1;
        if (charSequence.length() != 1) {
            sb.insert(i, PatternTokenizer.SINGLE_QUOTE);
            for (int i3 = 0; i3 < charSequence.length(); i3++) {
                char charAt = charSequence.charAt(i3);
                if (charAt == '\'') {
                    sb.insert(i + i2, "''");
                    i2 += 2;
                } else {
                    sb.insert(i + i2, charAt);
                    i2++;
                }
            }
            sb.insert(i + i2, PatternTokenizer.SINGLE_QUOTE);
        } else if (charSequence.equals("'")) {
            sb.insert(i, "''");
        } else {
            sb.insert(i, charSequence);
        }
        return sb.length() - length;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r19v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public static String convertLocalized(String str, DecimalFormatSymbols decimalFormatSymbols, boolean z) {
        if (str == null) {
            return null;
        }
        char c = 2;
        String[][] strArr = (String[][]) Array.newInstance(String.class, 21, 2);
        int i = !z ? 1 : 0;
        char c2 = 0;
        strArr[0][i] = "%";
        strArr[0][z ? 1 : 0] = decimalFormatSymbols.getPercentString();
        strArr[1][i] = "‰";
        strArr[1][z] = decimalFormatSymbols.getPerMillString();
        strArr[2][i] = ".";
        strArr[2][z] = decimalFormatSymbols.getDecimalSeparatorString();
        strArr[3][i] = ",";
        strArr[3][z] = decimalFormatSymbols.getGroupingSeparatorString();
        strArr[4][i] = LanguageTag.SEP;
        strArr[4][z] = decimalFormatSymbols.getMinusSignString();
        char c3 = 5;
        strArr[5][i] = "+";
        strArr[5][z] = decimalFormatSymbols.getPlusSignString();
        strArr[6][i] = DMSDPConfig.LIST_TO_STRING_SPLIT;
        strArr[6][z] = Character.toString(decimalFormatSymbols.getPatternSeparator());
        strArr[7][i] = "@";
        strArr[7][z] = Character.toString(decimalFormatSymbols.getSignificantDigit());
        strArr[8][i] = "E";
        strArr[8][z] = decimalFormatSymbols.getExponentSeparator();
        strArr[9][i] = "*";
        strArr[9][z] = Character.toString(decimalFormatSymbols.getPadEscape());
        strArr[10][i] = DMSDPConfig.SPLIT;
        strArr[10][z] = Character.toString(decimalFormatSymbols.getDigit());
        for (int i2 = 0; i2 < 10; i2++) {
            int i3 = i2 + 11;
            strArr[i3][i] = Character.toString((char) (i2 + 48));
            strArr[i3][z] = decimalFormatSymbols.getDigitStringsLocal()[i2];
        }
        for (int i4 = 0; i4 < strArr.length; i4++) {
            strArr[i4][z] = strArr[i4][z].replace(PatternTokenizer.SINGLE_QUOTE, (char) 8217);
        }
        StringBuilder sb = new StringBuilder();
        int i5 = 0;
        char c4 = 0;
        while (i5 < str.length()) {
            char charAt = str.charAt(i5);
            if (charAt == '\'') {
                if (c4 == 0) {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                } else if (c4 == 1) {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    c4 = 0;
                } else if (c4 == c) {
                    c4 = 3;
                } else if (c4 == 3) {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                } else if (c4 == 4) {
                    c4 = c3;
                } else {
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                    c4 = 4;
                }
                c4 = 1;
            } else {
                if (c4 == 0 || c4 == 3 || c4 == 4) {
                    int length = strArr.length;
                    int i6 = 0;
                    while (true) {
                        if (i6 < length) {
                            String[] strArr2 = strArr[i6];
                            if (str.regionMatches(i5, strArr2[0], 0, strArr2[0].length())) {
                                i5 += strArr2[0].length() - 1;
                                if (c4 == 3 || c4 == 4) {
                                    sb.append(PatternTokenizer.SINGLE_QUOTE);
                                    c = 0;
                                } else {
                                    c = c4;
                                }
                                sb.append(strArr2[1]);
                            } else {
                                i6++;
                            }
                        } else {
                            int length2 = strArr.length;
                            int i7 = 0;
                            while (true) {
                                if (i7 < length2) {
                                    String[] strArr3 = strArr[i7];
                                    if (str.regionMatches(i5, strArr3[1], 0, strArr3[1].length())) {
                                        if (c4 == 0) {
                                            sb.append(PatternTokenizer.SINGLE_QUOTE);
                                            c = 4;
                                        } else {
                                            c = c4;
                                        }
                                        sb.append(charAt);
                                    } else {
                                        i7++;
                                    }
                                } else {
                                    if (c4 == 3 || c4 == 4) {
                                        sb.append(PatternTokenizer.SINGLE_QUOTE);
                                        c = 0;
                                    } else {
                                        c = c4;
                                    }
                                    sb.append(charAt);
                                }
                            }
                        }
                    }
                } else {
                    sb.append(charAt);
                }
                c4 = c;
            }
            i5++;
            c = 2;
            c3 = 5;
        }
        if (c4 == 3 || c4 == 4) {
            sb.append(PatternTokenizer.SINGLE_QUOTE);
        } else {
            c2 = c4;
        }
        if (c2 == 0) {
            return sb.toString();
        }
        throw new IllegalArgumentException("Malformed localized pattern: unterminated quote");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x004d, code lost:
        if (r9 != ohos.global.icu.number.NumberFormatter.SignDisplay.NEVER) goto L_0x0053;
     */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x005d  */
    public static void patternInfoToStringBuilder(AffixPatternProvider affixPatternProvider, boolean z, int i, NumberFormatter.SignDisplay signDisplay, StandardPlural standardPlural, boolean z2, StringBuilder sb) {
        int length;
        char c;
        int i2 = 1;
        int i3 = (i == -1 || !(signDisplay == NumberFormatter.SignDisplay.ALWAYS || signDisplay == NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS || (i == 1 && (signDisplay == NumberFormatter.SignDisplay.EXCEPT_ZERO || signDisplay == NumberFormatter.SignDisplay.ACCOUNTING_EXCEPT_ZERO))) || affixPatternProvider.positiveHasPlusSign()) ? 0 : 1;
        boolean z3 = affixPatternProvider.hasNegativeSubpattern() && (i == -1 || (affixPatternProvider.negativeHasMinusSign() && i3 != 0));
        int i4 = z3 ? 512 : 0;
        if (z) {
            i4 |= 256;
        }
        if (standardPlural != null) {
            i4 |= standardPlural.ordinal();
        }
        if (z && !z3) {
            if (i != -1) {
                i2 = i3;
            }
            length = affixPatternProvider.length(i4) + i2;
            sb.setLength(0);
            for (int i5 = 0; i5 < length; i5++) {
                if (i2 != 0 && i5 == 0) {
                    c = '-';
                } else if (i2 != 0) {
                    c = affixPatternProvider.charAt(i4, i5 - 1);
                } else {
                    c = affixPatternProvider.charAt(i4, i5);
                }
                if (i3 != 0 && c == '-') {
                    c = '+';
                }
                if (z2 && c == '%') {
                    c = 8240;
                }
                sb.append(c);
            }
        }
        i2 = 0;
        length = affixPatternProvider.length(i4) + i2;
        sb.setLength(0);
        while (i5 < length) {
        }
    }
}

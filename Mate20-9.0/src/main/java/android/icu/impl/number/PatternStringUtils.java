package android.icu.impl.number;

import android.icu.impl.PatternTokenizer;
import android.icu.impl.locale.LanguageTag;
import android.icu.impl.number.Padder;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormatSymbols;
import java.lang.reflect.Array;
import java.math.BigDecimal;

public class PatternStringUtils {
    static final /* synthetic */ boolean $assertionsDisabled = false;

    public static String propertiesToPatternString(DecimalFormatProperties properties) {
        int grouping2;
        int grouping1;
        int grouping;
        String psp;
        int afterPrefixPos;
        String nsp;
        String npp;
        String paddingString;
        int groupingLength;
        StringBuilder sb = new StringBuilder();
        int groupingSize = Math.min(properties.getSecondaryGroupingSize(), 100);
        int firstGroupingSize = Math.min(properties.getGroupingSize(), 100);
        int paddingWidth = Math.min(properties.getFormatWidth(), 100);
        Padder.PadPosition paddingLocation = properties.getPadPosition();
        String paddingString2 = properties.getPadString();
        int minInt = Math.max(Math.min(properties.getMinimumIntegerDigits(), 100), 0);
        int maxInt = Math.min(properties.getMaximumIntegerDigits(), 100);
        int minFrac = Math.max(Math.min(properties.getMinimumFractionDigits(), 100), 0);
        int maxFrac = Math.min(properties.getMaximumFractionDigits(), 100);
        int minSig = Math.min(properties.getMinimumSignificantDigits(), 100);
        int maxSig = Math.min(properties.getMaximumSignificantDigits(), 100);
        boolean alwaysShowDecimal = properties.getDecimalSeparatorAlwaysShown();
        int exponentDigits = Math.min(properties.getMinimumExponentDigits(), 100);
        boolean exponentShowPlusSign = properties.getExponentSignAlwaysShown();
        String pp = properties.getPositivePrefix();
        String paddingString3 = paddingString2;
        String ppp = properties.getPositivePrefixPattern();
        Padder.PadPosition paddingLocation2 = paddingLocation;
        String ps = properties.getPositiveSuffix();
        int paddingWidth2 = paddingWidth;
        String psp2 = properties.getPositiveSuffixPattern();
        String ps2 = ps;
        String np = properties.getNegativePrefix();
        String npp2 = properties.getNegativePrefixPattern();
        String ns = properties.getNegativeSuffix();
        String nsp2 = properties.getNegativeSuffixPattern();
        if (ppp != null) {
            sb.append(ppp);
        }
        AffixUtils.escape(pp, sb);
        String str = ppp;
        int afterPrefixPos2 = sb.length();
        String str2 = pp;
        String nsp3 = nsp2;
        if (groupingSize != Math.min(100, -1) && firstGroupingSize != Math.min(100, -1) && groupingSize != firstGroupingSize) {
            grouping = groupingSize;
            grouping1 = groupingSize;
            grouping2 = firstGroupingSize;
        } else if (groupingSize != Math.min(100, -1)) {
            grouping = groupingSize;
            grouping1 = 0;
            grouping2 = groupingSize;
        } else if (firstGroupingSize != Math.min(100, -1)) {
            grouping = groupingSize;
            grouping1 = 0;
            grouping2 = firstGroupingSize;
        } else {
            grouping = 0;
            grouping1 = 0;
            grouping2 = 0;
        }
        int grouping22 = grouping2;
        int i = groupingSize;
        int i2 = firstGroupingSize;
        int groupingLength2 = grouping1 + grouping22 + 1;
        BigDecimal roundingInterval = properties.getRoundingIncrement();
        int afterPrefixPos3 = afterPrefixPos2;
        StringBuilder digitsString = new StringBuilder();
        int digitsStringScale = 0;
        String psp3 = psp2;
        int exponentDigits2 = exponentDigits;
        if (maxSig != Math.min(100, -1)) {
            while (digitsString.length() < minSig) {
                digitsString.append('@');
            }
            while (digitsString.length() < maxSig) {
                digitsString.append('#');
            }
            BigDecimal bigDecimal = roundingInterval;
            int i3 = minSig;
        } else if (roundingInterval != null) {
            int digitsStringScale2 = -roundingInterval.scale();
            String str3 = roundingInterval.scaleByPowerOfTen(roundingInterval.scale()).toPlainString();
            BigDecimal bigDecimal2 = roundingInterval;
            int i4 = minSig;
            if (str3.charAt(0) == 45) {
                digitsString.append(str3, 1, str3.length());
            } else {
                digitsString.append(str3);
            }
            digitsStringScale = digitsStringScale2;
        } else {
            int i5 = minSig;
        }
        while (digitsString.length() + digitsStringScale < minInt) {
            digitsString.insert(0, '0');
        }
        int digitsStringScale3 = digitsStringScale;
        while ((-digitsStringScale3) < minFrac) {
            digitsString.append('0');
            digitsStringScale3--;
        }
        int m0 = Math.max(groupingLength2, digitsString.length() + digitsStringScale3);
        int m02 = maxInt != 100 ? Math.max(maxInt, m0) - 1 : m0 - 1;
        int mN = maxFrac != 100 ? Math.min(-maxFrac, digitsStringScale3) : digitsStringScale3;
        int magnitude = m02;
        while (true) {
            int magnitude2 = magnitude;
            if (magnitude2 < mN) {
                break;
            }
            int digitsStringScale4 = digitsStringScale3;
            int di = ((digitsString.length() + digitsStringScale3) - magnitude2) - 1;
            if (di >= 0) {
                groupingLength = groupingLength2;
                if (di < digitsString.length()) {
                    sb.append(digitsString.charAt(di));
                    if (magnitude2 <= grouping22 && grouping > 0 && (magnitude2 - grouping22) % grouping == 0) {
                        sb.append(',');
                    } else if (magnitude2 <= 0 && magnitude2 == grouping22) {
                        sb.append(',');
                    } else if (magnitude2 == 0 && (alwaysShowDecimal || mN < 0)) {
                        sb.append('.');
                    }
                    magnitude = magnitude2 - 1;
                    digitsStringScale3 = digitsStringScale4;
                    groupingLength2 = groupingLength;
                }
            } else {
                groupingLength = groupingLength2;
            }
            sb.append('#');
            if (magnitude2 <= grouping22) {
            }
            if (magnitude2 <= 0) {
            }
            sb.append('.');
            magnitude = magnitude2 - 1;
            digitsStringScale3 = digitsStringScale4;
            groupingLength2 = groupingLength;
        }
        int i6 = groupingLength2;
        int exponentDigits3 = exponentDigits2;
        if (exponentDigits3 != Math.min(100, -1)) {
            sb.append('E');
            if (exponentShowPlusSign) {
                sb.append('+');
            }
            for (int i7 = 0; i7 < exponentDigits3; i7++) {
                sb.append('0');
            }
        }
        int beforeSuffixPos = sb.length();
        if (psp3 != null) {
            psp = psp3;
            sb.append(psp);
        } else {
            psp = psp3;
        }
        String ps3 = ps2;
        AffixUtils.escape(ps3, sb);
        String str4 = ps3;
        int i8 = exponentDigits3;
        int paddingWidth3 = paddingWidth2;
        if (paddingWidth3 != -1) {
            while (paddingWidth3 - sb.length() > 0) {
                sb.insert(afterPrefixPos3, '#');
                beforeSuffixPos++;
                paddingWidth3 = paddingWidth3;
            }
            afterPrefixPos = afterPrefixPos3;
            String str5 = psp;
            Padder.PadPosition paddingLocation3 = paddingLocation2;
            Padder.PadPosition padPosition = paddingLocation3;
            switch (paddingLocation3) {
                case BEFORE_PREFIX:
                    String paddingString4 = paddingString3;
                    int addedLength = escapePaddingString(paddingString4, sb, 0);
                    String str6 = paddingString4;
                    sb.insert(0, '*');
                    beforeSuffixPos += addedLength + 1;
                    afterPrefixPos = addedLength + 1 + afterPrefixPos;
                    break;
                case AFTER_PREFIX:
                    paddingString = paddingString3;
                    int addedLength2 = escapePaddingString(paddingString, sb, afterPrefixPos);
                    sb.insert(afterPrefixPos, '*');
                    afterPrefixPos += addedLength2 + 1;
                    beforeSuffixPos += addedLength2 + 1;
                    break;
                case BEFORE_SUFFIX:
                    paddingString = paddingString3;
                    escapePaddingString(paddingString, sb, beforeSuffixPos);
                    sb.insert(beforeSuffixPos, '*');
                    break;
                case AFTER_SUFFIX:
                    sb.append('*');
                    String paddingString5 = paddingString3;
                    escapePaddingString(paddingString5, sb, sb.length());
                    String str7 = paddingString5;
                    break;
                default:
                    String str8 = paddingString3;
                    break;
            }
        } else {
            String str9 = psp;
            String str10 = paddingString3;
            Padder.PadPosition padPosition2 = paddingLocation2;
            afterPrefixPos = afterPrefixPos3;
        }
        if (np != null || ns != null) {
            npp = npp2;
            nsp = nsp3;
        } else if (npp2 != null || nsp3 == null) {
            if (npp2 != null) {
                npp = npp2;
                int i9 = grouping;
                if (npp.length() == 1 && npp.charAt(0) == '-') {
                    nsp = nsp3;
                    if (nsp.length() == 0) {
                        String str11 = npp;
                        String str12 = np;
                        String npp3 = ns;
                    }
                } else {
                    nsp = nsp3;
                }
            } else {
                String str13 = nsp3;
                String str14 = np;
                String str15 = npp2;
                String str16 = ns;
            }
            return sb.toString();
        } else {
            int i10 = grouping;
            npp = npp2;
            nsp = nsp3;
        }
        sb.append(';');
        if (npp != null) {
            sb.append(npp);
        }
        AffixUtils.escape(np, sb);
        sb.append(sb, afterPrefixPos, beforeSuffixPos);
        if (nsp != null) {
            sb.append(nsp);
        }
        String str17 = npp;
        AffixUtils.escape(ns, sb);
        return sb.toString();
    }

    private static int escapePaddingString(CharSequence input, StringBuilder output, int startIndex) {
        if (input == null || input.length() == 0) {
            input = Padder.FALLBACK_PADDING_STRING;
        }
        int startLength = output.length();
        if (input.length() != 1) {
            output.insert(startIndex, PatternTokenizer.SINGLE_QUOTE);
            int offset = 1;
            for (int i = 0; i < input.length(); i++) {
                char ch = input.charAt(i);
                if (ch == '\'') {
                    output.insert(startIndex + offset, "''");
                    offset += 2;
                } else {
                    output.insert(startIndex + offset, ch);
                    offset++;
                }
            }
            output.insert(startIndex + offset, PatternTokenizer.SINGLE_QUOTE);
        } else if (input.equals("'")) {
            output.insert(startIndex, "''");
        } else {
            output.insert(startIndex, input);
        }
        return output.length() - startLength;
    }

    public static String convertLocalized(String input, DecimalFormatSymbols symbols, boolean toLocalized) {
        char c;
        String str = input;
        if (str == null) {
            return null;
        }
        int i = 2;
        String[][] table = (String[][]) Array.newInstance(String.class, new int[]{21, 2});
        int standIdx = !toLocalized;
        int localIdx = toLocalized;
        table[0][standIdx] = "%";
        table[0][localIdx] = symbols.getPercentString();
        table[1][standIdx] = "‰";
        table[1][localIdx] = symbols.getPerMillString();
        table[2][standIdx] = ".";
        table[2][localIdx] = symbols.getDecimalSeparatorString();
        int i2 = 3;
        table[3][standIdx] = ",";
        table[3][localIdx] = symbols.getGroupingSeparatorString();
        int i3 = 4;
        table[4][standIdx] = LanguageTag.SEP;
        table[4][localIdx] = symbols.getMinusSignString();
        table[5][standIdx] = "+";
        table[5][localIdx] = symbols.getPlusSignString();
        table[6][standIdx] = ";";
        table[6][localIdx] = Character.toString(symbols.getPatternSeparator());
        table[7][standIdx] = "@";
        table[7][localIdx] = Character.toString(symbols.getSignificantDigit());
        table[8][standIdx] = DateFormat.ABBR_WEEKDAY;
        table[8][localIdx] = symbols.getExponentSeparator();
        table[9][standIdx] = "*";
        table[9][localIdx] = Character.toString(symbols.getPadEscape());
        table[10][standIdx] = "#";
        table[10][localIdx] = Character.toString(symbols.getDigit());
        for (int i4 = 0; i4 < 10; i4++) {
            table[11 + i4][standIdx] = Character.toString((char) (48 + i4));
            table[11 + i4][localIdx] = symbols.getDigitStringsLocal()[i4];
        }
        int i5 = 0;
        while (true) {
            int length = table.length;
            c = PatternTokenizer.SINGLE_QUOTE;
            if (i5 >= length) {
                break;
            }
            table[i5][localIdx] = table[i5][localIdx].replace(PatternTokenizer.SINGLE_QUOTE, 8217);
            i5++;
        }
        StringBuilder result = new StringBuilder();
        int state = 0;
        int offset = 0;
        while (offset < input.length()) {
            char ch = str.charAt(offset);
            if (ch == c) {
                if (state == 0) {
                    result.append(c);
                    state = 1;
                } else if (state == 1) {
                    result.append(c);
                    state = 0;
                } else if (state == i) {
                    state = 3;
                } else if (state == i2) {
                    result.append(c);
                    result.append(c);
                    state = 1;
                } else if (state == i3) {
                    state = 5;
                } else {
                    result.append(c);
                    result.append(c);
                    state = 4;
                }
            } else if (state == 0 || state == i2 || state == i3) {
                int length2 = table.length;
                int i6 = 0;
                while (true) {
                    if (i6 < length2) {
                        String[] pair = table[i6];
                        if (str.regionMatches(offset, pair[0], 0, pair[0].length())) {
                            offset += pair[0].length() - 1;
                            if (state == i2 || state == 4) {
                                result.append(PatternTokenizer.SINGLE_QUOTE);
                                state = 0;
                            }
                            result.append(pair[1]);
                        } else {
                            i6++;
                        }
                    } else {
                        int length3 = table.length;
                        int i7 = 0;
                        while (true) {
                            if (i7 < length3) {
                                String[] pair2 = table[i7];
                                if (str.regionMatches(offset, pair2[1], 0, pair2[1].length())) {
                                    if (state == 0) {
                                        result.append(PatternTokenizer.SINGLE_QUOTE);
                                        state = 4;
                                    }
                                    result.append(ch);
                                } else {
                                    i7++;
                                }
                            } else {
                                if (state == 3 || state == 4) {
                                    result.append(PatternTokenizer.SINGLE_QUOTE);
                                    state = 0;
                                }
                                result.append(ch);
                            }
                        }
                    }
                }
            } else {
                result.append(ch);
                state = 2;
            }
            offset++;
            i = 2;
            i2 = 3;
            i3 = 4;
            c = PatternTokenizer.SINGLE_QUOTE;
        }
        if (state == 3 || state == 4) {
            result.append(PatternTokenizer.SINGLE_QUOTE);
            state = 0;
        }
        if (state == 0) {
            return result.toString();
        }
        throw new IllegalArgumentException("Malformed localized pattern: unterminated quote");
    }
}

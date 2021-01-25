package ohos.global.icu.impl.number.parse;

import java.util.Iterator;
import ohos.global.icu.impl.StandardPlural;
import ohos.global.icu.impl.StringSegment;
import ohos.global.icu.impl.TextTrieMap;
import ohos.global.icu.text.DecimalFormatSymbols;
import ohos.global.icu.util.Currency;

public class CombinedCurrencyMatcher implements NumberParseMatcher {
    private final String afterPrefixInsert;
    private final String beforeSuffixInsert;
    private final String currency1;
    private final String currency2;
    private final String isoCode;
    private final String[] localLongNames;
    private final TextTrieMap<Currency.CurrencyStringInfo> longNameTrie;
    private final TextTrieMap<Currency.CurrencyStringInfo> symbolTrie;

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public void postProcess(ParsedNumber parsedNumber) {
    }

    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean smokeTest(StringSegment stringSegment) {
        return true;
    }

    public static CombinedCurrencyMatcher getInstance(Currency currency, DecimalFormatSymbols decimalFormatSymbols, int i) {
        return new CombinedCurrencyMatcher(currency, decimalFormatSymbols, i);
    }

    private CombinedCurrencyMatcher(Currency currency, DecimalFormatSymbols decimalFormatSymbols, int i) {
        this.isoCode = currency.getSubtype();
        this.currency1 = currency.getSymbol(decimalFormatSymbols.getULocale());
        this.currency2 = currency.getCurrencyCode();
        this.afterPrefixInsert = decimalFormatSymbols.getPatternForCurrencySpacing(2, false);
        this.beforeSuffixInsert = decimalFormatSymbols.getPatternForCurrencySpacing(2, true);
        if ((i & 8192) == 0) {
            this.longNameTrie = Currency.getParsingTrie(decimalFormatSymbols.getULocale(), 1);
            this.symbolTrie = Currency.getParsingTrie(decimalFormatSymbols.getULocale(), 0);
            this.localLongNames = null;
            return;
        }
        this.longNameTrie = null;
        this.symbolTrie = null;
        this.localLongNames = new String[StandardPlural.COUNT];
        for (int i2 = 0; i2 < StandardPlural.COUNT; i2++) {
            this.localLongNames[i2] = currency.getName(decimalFormatSymbols.getLocale(), 2, StandardPlural.VALUES.get(i2).getKeyword(), (boolean[]) null);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0043  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0047  */
    @Override // ohos.global.icu.impl.number.parse.NumberParseMatcher
    public boolean match(StringSegment stringSegment, ParsedNumber parsedNumber) {
        boolean z;
        if (parsedNumber.currencyCode != null) {
            return false;
        }
        int offset = stringSegment.getOffset();
        if (parsedNumber.seenNumber() && !this.beforeSuffixInsert.isEmpty()) {
            int commonPrefixLength = stringSegment.getCommonPrefixLength(this.beforeSuffixInsert);
            if (commonPrefixLength == this.beforeSuffixInsert.length()) {
                stringSegment.adjustOffset(commonPrefixLength);
            }
            if (commonPrefixLength == stringSegment.length()) {
                z = true;
                boolean z2 = !z || matchCurrency(stringSegment, parsedNumber);
                if (parsedNumber.currencyCode != null) {
                    stringSegment.setOffset(offset);
                    return z2;
                } else if (parsedNumber.seenNumber() || this.afterPrefixInsert.isEmpty()) {
                    return z2;
                } else {
                    int commonPrefixLength2 = stringSegment.getCommonPrefixLength(this.afterPrefixInsert);
                    if (commonPrefixLength2 == this.afterPrefixInsert.length()) {
                        stringSegment.adjustOffset(commonPrefixLength2);
                    }
                    if (z2 || commonPrefixLength2 == stringSegment.length()) {
                        return true;
                    }
                    return false;
                }
            }
        }
        z = false;
        if (!z) {
        }
        if (parsedNumber.currencyCode != null) {
        }
    }

    private boolean matchCurrency(StringSegment stringSegment, ParsedNumber parsedNumber) {
        int i = -1;
        int caseSensitivePrefixLength = !this.currency1.isEmpty() ? stringSegment.getCaseSensitivePrefixLength(this.currency1) : -1;
        boolean z = caseSensitivePrefixLength == stringSegment.length();
        if (caseSensitivePrefixLength == this.currency1.length()) {
            parsedNumber.currencyCode = this.isoCode;
            stringSegment.adjustOffset(caseSensitivePrefixLength);
            parsedNumber.setCharsConsumed(stringSegment);
            return z;
        }
        if (!this.currency2.isEmpty()) {
            i = stringSegment.getCommonPrefixLength(this.currency2);
        }
        boolean z2 = z || i == stringSegment.length();
        if (i == this.currency2.length()) {
            parsedNumber.currencyCode = this.isoCode;
            stringSegment.adjustOffset(i);
            parsedNumber.setCharsConsumed(stringSegment);
            return z2;
        } else if (this.longNameTrie != null) {
            TextTrieMap.Output output = new TextTrieMap.Output();
            Iterator<Currency.CurrencyStringInfo> it = this.longNameTrie.get(stringSegment, 0, output);
            boolean z3 = z2 || output.partialMatch;
            if (it == null) {
                it = this.symbolTrie.get(stringSegment, 0, output);
                z3 = z3 || output.partialMatch;
            }
            if (it == null) {
                return z3;
            }
            parsedNumber.currencyCode = it.next().getISOCode();
            stringSegment.adjustOffset(output.matchLength);
            parsedNumber.setCharsConsumed(stringSegment);
            return z3;
        } else {
            boolean z4 = z2;
            int i2 = 0;
            for (int i3 = 0; i3 < StandardPlural.COUNT; i3++) {
                String str = this.localLongNames[i3];
                if (!str.isEmpty()) {
                    int commonPrefixLength = stringSegment.getCommonPrefixLength(str);
                    if (commonPrefixLength == str.length() && str.length() > i2) {
                        i2 = str.length();
                    }
                    z4 = z4 || commonPrefixLength > 0;
                }
            }
            if (i2 <= 0) {
                return z4;
            }
            parsedNumber.currencyCode = this.isoCode;
            stringSegment.adjustOffset(i2);
            parsedNumber.setCharsConsumed(stringSegment);
            return z4;
        }
    }

    public String toString() {
        return "<CombinedCurrencyMatcher " + this.isoCode + ">";
    }
}

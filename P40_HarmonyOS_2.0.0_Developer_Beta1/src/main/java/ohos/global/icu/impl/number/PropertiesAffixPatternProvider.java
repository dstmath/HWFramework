package ohos.global.icu.impl.number;

import ohos.dmsdp.sdk.DMSDPConfig;
import ohos.global.icu.impl.locale.LanguageTag;

public class PropertiesAffixPatternProvider implements AffixPatternProvider {
    private final boolean isCurrencyPattern;
    private final String negPrefix;
    private final String negSuffix;
    private final String posPrefix;
    private final String posSuffix;

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public boolean hasBody() {
        return true;
    }

    public PropertiesAffixPatternProvider(DecimalFormatProperties decimalFormatProperties) {
        String escape = AffixUtils.escape(decimalFormatProperties.getPositivePrefix());
        String escape2 = AffixUtils.escape(decimalFormatProperties.getPositiveSuffix());
        String escape3 = AffixUtils.escape(decimalFormatProperties.getNegativePrefix());
        String escape4 = AffixUtils.escape(decimalFormatProperties.getNegativeSuffix());
        String positivePrefixPattern = decimalFormatProperties.getPositivePrefixPattern();
        String positiveSuffixPattern = decimalFormatProperties.getPositiveSuffixPattern();
        String negativePrefixPattern = decimalFormatProperties.getNegativePrefixPattern();
        String negativeSuffixPattern = decimalFormatProperties.getNegativeSuffixPattern();
        String str = "";
        if (escape != null) {
            this.posPrefix = escape;
        } else if (positivePrefixPattern != null) {
            this.posPrefix = positivePrefixPattern;
        } else {
            this.posPrefix = str;
        }
        if (escape2 != null) {
            this.posSuffix = escape2;
        } else if (positiveSuffixPattern != null) {
            this.posSuffix = positiveSuffixPattern;
        } else {
            this.posSuffix = str;
        }
        if (escape3 != null) {
            this.negPrefix = escape3;
        } else if (negativePrefixPattern != null) {
            this.negPrefix = negativePrefixPattern;
        } else {
            String str2 = LanguageTag.SEP;
            if (positivePrefixPattern != null) {
                str2 = str2 + positivePrefixPattern;
            }
            this.negPrefix = str2;
        }
        if (escape4 != null) {
            this.negSuffix = escape4;
        } else if (negativeSuffixPattern != null) {
            this.negSuffix = negativeSuffixPattern;
        } else {
            this.negSuffix = positiveSuffixPattern != null ? positiveSuffixPattern : str;
        }
        this.isCurrencyPattern = AffixUtils.hasCurrencySymbols(positivePrefixPattern) || AffixUtils.hasCurrencySymbols(positiveSuffixPattern) || AffixUtils.hasCurrencySymbols(negativePrefixPattern) || AffixUtils.hasCurrencySymbols(negativeSuffixPattern);
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public char charAt(int i, int i2) {
        return getString(i).charAt(i2);
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public int length(int i) {
        return getString(i).length();
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public String getString(int i) {
        boolean z = true;
        boolean z2 = (i & 256) != 0;
        if ((i & 512) == 0) {
            z = false;
        }
        if (z2 && z) {
            return this.negPrefix;
        }
        if (z2) {
            return this.posPrefix;
        }
        if (z) {
            return this.negSuffix;
        }
        return this.posSuffix;
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public boolean positiveHasPlusSign() {
        return AffixUtils.containsType(this.posPrefix, -2) || AffixUtils.containsType(this.posSuffix, -2);
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public boolean hasNegativeSubpattern() {
        if (this.negSuffix == this.posSuffix && this.negPrefix.length() == this.posPrefix.length() + 1) {
            String str = this.negPrefix;
            String str2 = this.posPrefix;
            if (str.regionMatches(1, str2, 0, str2.length()) && this.negPrefix.charAt(0) == '-') {
                return false;
            }
        }
        return true;
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public boolean negativeHasMinusSign() {
        return AffixUtils.containsType(this.negPrefix, -1) || AffixUtils.containsType(this.negSuffix, -1);
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public boolean hasCurrencySign() {
        return this.isCurrencyPattern;
    }

    @Override // ohos.global.icu.impl.number.AffixPatternProvider
    public boolean containsSymbolType(int i) {
        return AffixUtils.containsType(this.posPrefix, i) || AffixUtils.containsType(this.posSuffix, i) || AffixUtils.containsType(this.negPrefix, i) || AffixUtils.containsType(this.negSuffix, i);
    }

    public String toString() {
        return super.toString() + " {" + this.posPrefix + DMSDPConfig.SPLIT + this.posSuffix + DMSDPConfig.LIST_TO_STRING_SPLIT + this.negPrefix + DMSDPConfig.SPLIT + this.negSuffix + "}";
    }
}

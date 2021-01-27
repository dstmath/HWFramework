package ohos.global.icu.text;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import ohos.global.icu.impl.CurrencyData;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ICUCloneNotSupportedException;
import ohos.global.icu.util.ULocale;

public class CurrencyPluralInfo implements Cloneable, Serializable {
    private static final String defaultCurrencyPluralPattern = new String(defaultCurrencyPluralPatternChar);
    private static final char[] defaultCurrencyPluralPatternChar = {0, '.', '#', '#', ' ', 164, 164, 164};
    private static final long serialVersionUID = 1;
    private static final char[] tripleCurrencySign = {164, 164, 164};
    private static final String tripleCurrencyStr = new String(tripleCurrencySign);
    private Map<String, String> pluralCountToCurrencyUnitPattern = null;
    private PluralRules pluralRules = null;
    private ULocale ulocale = null;

    public CurrencyPluralInfo() {
        initialize(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public CurrencyPluralInfo(Locale locale) {
        initialize(ULocale.forLocale(locale));
    }

    public CurrencyPluralInfo(ULocale uLocale) {
        initialize(uLocale);
    }

    public static CurrencyPluralInfo getInstance() {
        return new CurrencyPluralInfo();
    }

    public static CurrencyPluralInfo getInstance(Locale locale) {
        return new CurrencyPluralInfo(locale);
    }

    public static CurrencyPluralInfo getInstance(ULocale uLocale) {
        return new CurrencyPluralInfo(uLocale);
    }

    public PluralRules getPluralRules() {
        return this.pluralRules;
    }

    public String getCurrencyPluralPattern(String str) {
        String str2 = this.pluralCountToCurrencyUnitPattern.get(str);
        if (str2 != null) {
            return str2;
        }
        if (!str.equals("other")) {
            str2 = this.pluralCountToCurrencyUnitPattern.get("other");
        }
        return str2 == null ? defaultCurrencyPluralPattern : str2;
    }

    public ULocale getLocale() {
        return this.ulocale;
    }

    public void setPluralRules(String str) {
        this.pluralRules = PluralRules.createRules(str);
    }

    public void setCurrencyPluralPattern(String str, String str2) {
        this.pluralCountToCurrencyUnitPattern.put(str, str2);
    }

    public void setLocale(ULocale uLocale) {
        this.ulocale = uLocale;
        initialize(uLocale);
    }

    @Override // java.lang.Object
    public Object clone() {
        try {
            CurrencyPluralInfo currencyPluralInfo = (CurrencyPluralInfo) super.clone();
            currencyPluralInfo.ulocale = (ULocale) this.ulocale.clone();
            currencyPluralInfo.pluralCountToCurrencyUnitPattern = new HashMap();
            for (String str : this.pluralCountToCurrencyUnitPattern.keySet()) {
                currencyPluralInfo.pluralCountToCurrencyUnitPattern.put(str, this.pluralCountToCurrencyUnitPattern.get(str));
            }
            return currencyPluralInfo;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof CurrencyPluralInfo)) {
            return false;
        }
        CurrencyPluralInfo currencyPluralInfo = (CurrencyPluralInfo) obj;
        if (!this.pluralRules.equals(currencyPluralInfo.pluralRules) || !this.pluralCountToCurrencyUnitPattern.equals(currencyPluralInfo.pluralCountToCurrencyUnitPattern)) {
            return false;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.ulocale.hashCode() ^ (this.pluralCountToCurrencyUnitPattern.hashCode() ^ this.pluralRules.hashCode());
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public String select(double d) {
        return this.pluralRules.select(d);
    }

    @Deprecated
    public String select(PluralRules.FixedDecimal fixedDecimal) {
        return this.pluralRules.select(fixedDecimal);
    }

    @Deprecated
    public Iterator<String> pluralPatternIterator() {
        return this.pluralCountToCurrencyUnitPattern.keySet().iterator();
    }

    private void initialize(ULocale uLocale) {
        this.ulocale = uLocale;
        this.pluralRules = PluralRules.forLocale(uLocale);
        setupCurrencyPluralPattern(uLocale);
    }

    private void setupCurrencyPluralPattern(ULocale uLocale) {
        String str;
        this.pluralCountToCurrencyUnitPattern = new HashMap();
        String pattern = NumberFormat.getPattern(uLocale, 0);
        int indexOf = pattern.indexOf(";");
        if (indexOf != -1) {
            str = pattern.substring(indexOf + 1);
            pattern = pattern.substring(0, indexOf);
        } else {
            str = null;
        }
        for (Map.Entry entry : CurrencyData.provider.getInstance(uLocale, true).getUnitPatterns().entrySet()) {
            String str2 = (String) entry.getKey();
            String str3 = (String) entry.getValue();
            String replace = str3.replace("{0}", pattern).replace("{1}", tripleCurrencyStr);
            if (indexOf != -1) {
                replace = replace + ";" + str3.replace("{0}", str).replace("{1}", tripleCurrencyStr);
            }
            this.pluralCountToCurrencyUnitPattern.put(str2, replace);
        }
    }
}

package android.icu.text;

import android.icu.impl.CurrencyData;
import android.icu.text.PluralRules;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

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

    public CurrencyPluralInfo(ULocale locale) {
        initialize(locale);
    }

    public static CurrencyPluralInfo getInstance() {
        return new CurrencyPluralInfo();
    }

    public static CurrencyPluralInfo getInstance(Locale locale) {
        return new CurrencyPluralInfo(locale);
    }

    public static CurrencyPluralInfo getInstance(ULocale locale) {
        return new CurrencyPluralInfo(locale);
    }

    public PluralRules getPluralRules() {
        return this.pluralRules;
    }

    public String getCurrencyPluralPattern(String pluralCount) {
        String currencyPluralPattern = this.pluralCountToCurrencyUnitPattern.get(pluralCount);
        if (currencyPluralPattern != null) {
            return currencyPluralPattern;
        }
        if (!pluralCount.equals(PluralRules.KEYWORD_OTHER)) {
            currencyPluralPattern = this.pluralCountToCurrencyUnitPattern.get(PluralRules.KEYWORD_OTHER);
        }
        if (currencyPluralPattern == null) {
            return defaultCurrencyPluralPattern;
        }
        return currencyPluralPattern;
    }

    public ULocale getLocale() {
        return this.ulocale;
    }

    public void setPluralRules(String ruleDescription) {
        this.pluralRules = PluralRules.createRules(ruleDescription);
    }

    public void setCurrencyPluralPattern(String pluralCount, String pattern) {
        this.pluralCountToCurrencyUnitPattern.put(pluralCount, pattern);
    }

    public void setLocale(ULocale loc) {
        this.ulocale = loc;
        initialize(loc);
    }

    public Object clone() {
        try {
            CurrencyPluralInfo other = (CurrencyPluralInfo) super.clone();
            other.ulocale = (ULocale) this.ulocale.clone();
            other.pluralCountToCurrencyUnitPattern = new HashMap();
            for (String pluralCount : this.pluralCountToCurrencyUnitPattern.keySet()) {
                other.pluralCountToCurrencyUnitPattern.put(pluralCount, this.pluralCountToCurrencyUnitPattern.get(pluralCount));
            }
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException((Throwable) e);
        }
    }

    public boolean equals(Object a) {
        boolean z = false;
        if (!(a instanceof CurrencyPluralInfo)) {
            return false;
        }
        CurrencyPluralInfo other = (CurrencyPluralInfo) a;
        if (this.pluralRules.equals(other.pluralRules) && this.pluralCountToCurrencyUnitPattern.equals(other.pluralCountToCurrencyUnitPattern)) {
            z = true;
        }
        return z;
    }

    @Deprecated
    public int hashCode() {
        return (this.pluralCountToCurrencyUnitPattern.hashCode() ^ this.pluralRules.hashCode()) ^ this.ulocale.hashCode();
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public String select(double number) {
        return this.pluralRules.select(number);
    }

    /* access modifiers changed from: package-private */
    @Deprecated
    public String select(PluralRules.FixedDecimal numberInfo) {
        return this.pluralRules.select((PluralRules.IFixedDecimal) numberInfo);
    }

    /* access modifiers changed from: package-private */
    public Iterator<String> pluralPatternIterator() {
        return this.pluralCountToCurrencyUnitPattern.keySet().iterator();
    }

    private void initialize(ULocale uloc) {
        this.ulocale = uloc;
        this.pluralRules = PluralRules.forLocale(uloc);
        setupCurrencyPluralPattern(uloc);
    }

    private void setupCurrencyPluralPattern(ULocale uloc) {
        ULocale uLocale = uloc;
        this.pluralCountToCurrencyUnitPattern = new HashMap();
        String numberStylePattern = NumberFormat.getPattern(uLocale, 0);
        int separatorIndex = numberStylePattern.indexOf(";");
        String negNumberPattern = null;
        int i = -1;
        if (separatorIndex != -1) {
            negNumberPattern = numberStylePattern.substring(separatorIndex + 1);
            numberStylePattern = numberStylePattern.substring(0, separatorIndex);
        }
        for (Map.Entry<String, String> e : CurrencyData.provider.getInstance(uLocale, true).getUnitPatterns().entrySet()) {
            String pluralCount = e.getKey();
            String pattern = e.getValue();
            String patternWithCurrencySign = pattern.replace("{0}", numberStylePattern).replace("{1}", tripleCurrencyStr);
            if (separatorIndex != i) {
                String negWithCurrSign = pattern.replace("{0}", negNumberPattern).replace("{1}", tripleCurrencyStr);
                patternWithCurrencySign = patternWithCurrencySign + ";" + negWithCurrSign;
            }
            this.pluralCountToCurrencyUnitPattern.put(pluralCount, patternWithCurrencySign);
            ULocale uLocale2 = uloc;
            i = -1;
        }
    }
}

package android.icu.text;

import android.icu.impl.CurrencyData;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.ICUCloneNotSupportedException;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class CurrencyPluralInfo implements Cloneable, Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = (CurrencyPluralInfo.class.desiredAssertionStatus() ^ 1);
    private static final String defaultCurrencyPluralPattern = new String(defaultCurrencyPluralPatternChar);
    private static final char[] defaultCurrencyPluralPatternChar = new char[]{0, '.', '#', '#', ' ', 164, 164, 164};
    private static final long serialVersionUID = 1;
    private static final char[] tripleCurrencySign = new char[]{164, 164, 164};
    private static final String tripleCurrencyStr = new String(tripleCurrencySign);
    private Map<String, String> pluralCountToCurrencyUnitPattern = null;
    private PluralRules pluralRules = null;
    private ULocale ulocale = null;

    public CurrencyPluralInfo() {
        initialize(ULocale.getDefault(Category.FORMAT));
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
        String currencyPluralPattern = (String) this.pluralCountToCurrencyUnitPattern.get(pluralCount);
        if (currencyPluralPattern != null) {
            return currencyPluralPattern;
        }
        if (!pluralCount.equals("other")) {
            currencyPluralPattern = (String) this.pluralCountToCurrencyUnitPattern.get("other");
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
                other.pluralCountToCurrencyUnitPattern.put(pluralCount, (String) this.pluralCountToCurrencyUnitPattern.get(pluralCount));
            }
            return other;
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    public boolean equals(Object a) {
        boolean z = false;
        if (!(a instanceof CurrencyPluralInfo)) {
            return false;
        }
        CurrencyPluralInfo other = (CurrencyPluralInfo) a;
        if (this.pluralRules.equals(other.pluralRules)) {
            z = this.pluralCountToCurrencyUnitPattern.equals(other.pluralCountToCurrencyUnitPattern);
        }
        return z;
    }

    @Deprecated
    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    @Deprecated
    String select(double number) {
        return this.pluralRules.select(number);
    }

    @Deprecated
    String select(FixedDecimal numberInfo) {
        return this.pluralRules.select(numberInfo);
    }

    Iterator<String> pluralPatternIterator() {
        return this.pluralCountToCurrencyUnitPattern.keySet().iterator();
    }

    private void initialize(ULocale uloc) {
        this.ulocale = uloc;
        this.pluralRules = PluralRules.forLocale(uloc);
        setupCurrencyPluralPattern(uloc);
    }

    private void setupCurrencyPluralPattern(ULocale uloc) {
        this.pluralCountToCurrencyUnitPattern = new HashMap();
        String numberStylePattern = NumberFormat.getPattern(uloc, 0);
        int separatorIndex = numberStylePattern.indexOf(";");
        CharSequence negNumberPattern = null;
        if (separatorIndex != -1) {
            negNumberPattern = numberStylePattern.substring(separatorIndex + 1);
            numberStylePattern = numberStylePattern.substring(0, separatorIndex);
        }
        for (Entry<String, String> e : CurrencyData.provider.getInstance(uloc, true).getUnitPatterns().entrySet()) {
            String pluralCount = (String) e.getKey();
            String pattern = (String) e.getValue();
            String patternWithCurrencySign = pattern.replace("{0}", numberStylePattern).replace("{1}", tripleCurrencyStr);
            if (separatorIndex != -1) {
                String negPattern = pattern;
                String negWithCurrSign = pattern.replace("{0}", negNumberPattern).replace("{1}", tripleCurrencyStr);
                StringBuilder posNegPatterns = new StringBuilder(patternWithCurrencySign);
                posNegPatterns.append(";");
                posNegPatterns.append(negWithCurrSign);
                patternWithCurrencySign = posNegPatterns.toString();
            }
            this.pluralCountToCurrencyUnitPattern.put(pluralCount, patternWithCurrencySign);
        }
    }
}

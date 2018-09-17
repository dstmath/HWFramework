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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final String defaultCurrencyPluralPattern = null;
    private static final char[] defaultCurrencyPluralPatternChar = null;
    private static final long serialVersionUID = 1;
    private static final char[] tripleCurrencySign = null;
    private static final String tripleCurrencyStr = null;
    private Map<String, String> pluralCountToCurrencyUnitPattern;
    private PluralRules pluralRules;
    private ULocale ulocale;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CurrencyPluralInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CurrencyPluralInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CurrencyPluralInfo.<clinit>():void");
    }

    public CurrencyPluralInfo() {
        this.pluralCountToCurrencyUnitPattern = null;
        this.pluralRules = null;
        this.ulocale = null;
        initialize(ULocale.getDefault(Category.FORMAT));
    }

    public CurrencyPluralInfo(Locale locale) {
        this.pluralCountToCurrencyUnitPattern = null;
        this.pluralRules = null;
        this.ulocale = null;
        initialize(ULocale.forLocale(locale));
    }

    public CurrencyPluralInfo(ULocale locale) {
        this.pluralCountToCurrencyUnitPattern = null;
        this.pluralRules = null;
        this.ulocale = null;
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
        if (!pluralCount.equals(PluralRules.KEYWORD_OTHER)) {
            currencyPluralPattern = (String) this.pluralCountToCurrencyUnitPattern.get(PluralRules.KEYWORD_OTHER);
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
        boolean z = -assertionsDisabled;
        if (!(a instanceof CurrencyPluralInfo)) {
            return -assertionsDisabled;
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
        CharSequence charSequence = null;
        if (separatorIndex != -1) {
            charSequence = numberStylePattern.substring(separatorIndex + 1);
            numberStylePattern = numberStylePattern.substring(0, separatorIndex);
        }
        for (Entry<String, String> e : CurrencyData.provider.getInstance(uloc, true).getUnitPatterns().entrySet()) {
            String pluralCount = (String) e.getKey();
            String pattern = (String) e.getValue();
            String str = "{1}";
            String str2 = tripleCurrencyStr;
            String patternWithCurrencySign = pattern.replace("{0}", numberStylePattern).replace(r17, r18);
            if (separatorIndex != -1) {
                String negPattern = pattern;
                str = "{1}";
                str2 = tripleCurrencyStr;
                String negWithCurrSign = pattern.replace("{0}", charSequence).replace(r17, r18);
                StringBuilder posNegPatterns = new StringBuilder(patternWithCurrencySign);
                posNegPatterns.append(";");
                posNegPatterns.append(negWithCurrSign);
                patternWithCurrencySign = posNegPatterns.toString();
            }
            this.pluralCountToCurrencyUnitPattern.put(pluralCount, patternWithCurrencySign);
        }
    }
}

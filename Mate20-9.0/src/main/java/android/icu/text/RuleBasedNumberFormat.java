package android.icu.text;

import android.icu.impl.ICUData;
import android.icu.impl.ICUDebug;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.PatternProps;
import android.icu.impl.coll.CollationSettings;
import android.icu.lang.UCharacter;
import android.icu.math.BigDecimal;
import android.icu.text.DisplayContext;
import android.icu.text.PluralRules;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

public class RuleBasedNumberFormat extends NumberFormat {
    private static final boolean DEBUG = ICUDebug.enabled("rbnf");
    public static final int DURATION = 3;
    private static final BigDecimal MAX_VALUE = BigDecimal.valueOf(Long.MAX_VALUE);
    private static final BigDecimal MIN_VALUE = BigDecimal.valueOf(Long.MIN_VALUE);
    public static final int NUMBERING_SYSTEM = 4;
    public static final int ORDINAL = 2;
    public static final int SPELLOUT = 1;
    private static final String[] locnames = {"SpelloutLocalizations", "OrdinalLocalizations", "DurationLocalizations", "NumberingSystemLocalizations"};
    private static final String[] rulenames = {"SpelloutRules", "OrdinalRules", "DurationRules", "NumberingSystemRules"};
    static final long serialVersionUID = -7664252765575395068L;
    private transient BreakIterator capitalizationBrkIter;
    private boolean capitalizationForListOrMenu;
    private boolean capitalizationForStandAlone;
    private boolean capitalizationInfoIsSet;
    private transient DecimalFormat decimalFormat;
    private transient DecimalFormatSymbols decimalFormatSymbols;
    private transient NFRule defaultInfinityRule;
    private transient NFRule defaultNaNRule;
    private transient NFRuleSet defaultRuleSet;
    private boolean lenientParse;
    private transient String lenientParseRules;
    private ULocale locale;
    private transient boolean lookedForScanner;
    private transient String postProcessRules;
    private transient RBNFPostProcessor postProcessor;
    private String[] publicRuleSetNames;
    private int roundingMode;
    private Map<String, String[]> ruleSetDisplayNames;
    private transient NFRuleSet[] ruleSets;
    private transient Map<String, NFRuleSet> ruleSetsMap;
    private transient RbnfLenientScannerProvider scannerProvider;

    public RuleBasedNumberFormat(String description) {
        this.ruleSets = null;
        this.ruleSetsMap = null;
        this.defaultRuleSet = null;
        this.locale = null;
        this.roundingMode = 7;
        this.scannerProvider = null;
        this.decimalFormatSymbols = null;
        this.decimalFormat = null;
        this.defaultInfinityRule = null;
        this.defaultNaNRule = null;
        this.lenientParse = false;
        this.capitalizationInfoIsSet = false;
        this.capitalizationForListOrMenu = false;
        this.capitalizationForStandAlone = false;
        this.capitalizationBrkIter = null;
        this.locale = ULocale.getDefault(ULocale.Category.FORMAT);
        init(description, null);
    }

    public RuleBasedNumberFormat(String description, String[][] localizations) {
        this.ruleSets = null;
        this.ruleSetsMap = null;
        this.defaultRuleSet = null;
        this.locale = null;
        this.roundingMode = 7;
        this.scannerProvider = null;
        this.decimalFormatSymbols = null;
        this.decimalFormat = null;
        this.defaultInfinityRule = null;
        this.defaultNaNRule = null;
        this.lenientParse = false;
        this.capitalizationInfoIsSet = false;
        this.capitalizationForListOrMenu = false;
        this.capitalizationForStandAlone = false;
        this.capitalizationBrkIter = null;
        this.locale = ULocale.getDefault(ULocale.Category.FORMAT);
        init(description, localizations);
    }

    public RuleBasedNumberFormat(String description, Locale locale2) {
        this(description, ULocale.forLocale(locale2));
    }

    public RuleBasedNumberFormat(String description, ULocale locale2) {
        this.ruleSets = null;
        this.ruleSetsMap = null;
        this.defaultRuleSet = null;
        this.locale = null;
        this.roundingMode = 7;
        this.scannerProvider = null;
        this.decimalFormatSymbols = null;
        this.decimalFormat = null;
        this.defaultInfinityRule = null;
        this.defaultNaNRule = null;
        this.lenientParse = false;
        this.capitalizationInfoIsSet = false;
        this.capitalizationForListOrMenu = false;
        this.capitalizationForStandAlone = false;
        this.capitalizationBrkIter = null;
        this.locale = locale2;
        init(description, null);
    }

    public RuleBasedNumberFormat(String description, String[][] localizations, ULocale locale2) {
        this.ruleSets = null;
        this.ruleSetsMap = null;
        this.defaultRuleSet = null;
        this.locale = null;
        this.roundingMode = 7;
        this.scannerProvider = null;
        this.decimalFormatSymbols = null;
        this.decimalFormat = null;
        this.defaultInfinityRule = null;
        this.defaultNaNRule = null;
        this.lenientParse = false;
        this.capitalizationInfoIsSet = false;
        this.capitalizationForListOrMenu = false;
        this.capitalizationForStandAlone = false;
        this.capitalizationBrkIter = null;
        this.locale = locale2;
        init(description, localizations);
    }

    public RuleBasedNumberFormat(Locale locale2, int format) {
        this(ULocale.forLocale(locale2), format);
    }

    public RuleBasedNumberFormat(ULocale locale2, int format) {
        String[][] localizations = null;
        this.ruleSets = null;
        this.ruleSetsMap = null;
        this.defaultRuleSet = null;
        this.locale = null;
        this.roundingMode = 7;
        this.scannerProvider = null;
        this.decimalFormatSymbols = null;
        this.decimalFormat = null;
        this.defaultInfinityRule = null;
        this.defaultNaNRule = null;
        this.lenientParse = false;
        this.capitalizationInfoIsSet = false;
        this.capitalizationForListOrMenu = false;
        this.capitalizationForStandAlone = false;
        this.capitalizationBrkIter = null;
        this.locale = locale2;
        ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_RBNF_BASE_NAME, locale2);
        ULocale uloc = bundle.getULocale();
        setLocale(uloc, uloc);
        StringBuilder description = new StringBuilder();
        try {
            UResourceBundleIterator it = bundle.getWithFallback("RBNFRules/" + rulenames[format - 1]).getIterator();
            while (it.hasNext()) {
                description.append(it.nextString());
            }
        } catch (MissingResourceException e) {
        }
        UResourceBundle locNamesBundle = bundle.findTopLevel(locnames[format - 1]);
        if (locNamesBundle != null) {
            localizations = new String[locNamesBundle.getSize()][];
            for (int i = 0; i < localizations.length; i++) {
                localizations[i] = locNamesBundle.get(i).getStringArray();
            }
        }
        init(description.toString(), localizations);
    }

    public RuleBasedNumberFormat(int format) {
        this(ULocale.getDefault(ULocale.Category.FORMAT), format);
    }

    public Object clone() {
        return super.clone();
    }

    public boolean equals(Object that) {
        if (!(that instanceof RuleBasedNumberFormat)) {
            return false;
        }
        RuleBasedNumberFormat that2 = (RuleBasedNumberFormat) that;
        if (!this.locale.equals(that2.locale) || this.lenientParse != that2.lenientParse || this.ruleSets.length != that2.ruleSets.length) {
            return false;
        }
        for (int i = 0; i < this.ruleSets.length; i++) {
            if (!this.ruleSets[i].equals(that2.ruleSets[i])) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (NFRuleSet ruleSet : this.ruleSets) {
            result.append(ruleSet.toString());
        }
        return result.toString();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(toString());
        out.writeObject(this.locale);
        out.writeInt(this.roundingMode);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        ULocale loc;
        String description = in.readUTF();
        try {
            loc = (ULocale) in.readObject();
        } catch (Exception e) {
            loc = ULocale.getDefault(ULocale.Category.FORMAT);
        }
        try {
            this.roundingMode = in.readInt();
        } catch (Exception e2) {
        }
        RuleBasedNumberFormat temp = new RuleBasedNumberFormat(description, loc);
        this.ruleSets = temp.ruleSets;
        this.ruleSetsMap = temp.ruleSetsMap;
        this.defaultRuleSet = temp.defaultRuleSet;
        this.publicRuleSetNames = temp.publicRuleSetNames;
        this.decimalFormatSymbols = temp.decimalFormatSymbols;
        this.decimalFormat = temp.decimalFormat;
        this.locale = temp.locale;
        this.defaultInfinityRule = temp.defaultInfinityRule;
        this.defaultNaNRule = temp.defaultNaNRule;
    }

    public String[] getRuleSetNames() {
        return (String[]) this.publicRuleSetNames.clone();
    }

    public ULocale[] getRuleSetDisplayNameLocales() {
        if (this.ruleSetDisplayNames == null) {
            return null;
        }
        Set<String> s = this.ruleSetDisplayNames.keySet();
        String[] locales = (String[]) s.toArray(new String[s.size()]);
        Arrays.sort(locales, String.CASE_INSENSITIVE_ORDER);
        ULocale[] result = new ULocale[locales.length];
        for (int i = 0; i < locales.length; i++) {
            result[i] = new ULocale(locales[i]);
        }
        return result;
    }

    private String[] getNameListForLocale(ULocale loc) {
        if (!(loc == null || this.ruleSetDisplayNames == null)) {
            String[] localeNames = {loc.getBaseName(), ULocale.getDefault(ULocale.Category.DISPLAY).getBaseName()};
            int length = localeNames.length;
            for (int i = 0; i < length; i++) {
                for (String lname = localeNames[i]; lname.length() > 0; lname = ULocale.getFallback(lname)) {
                    String[] names = this.ruleSetDisplayNames.get(lname);
                    if (names != null) {
                        return names;
                    }
                }
            }
        }
        return null;
    }

    public String[] getRuleSetDisplayNames(ULocale loc) {
        String[] names = getNameListForLocale(loc);
        if (names != null) {
            return (String[]) names.clone();
        }
        String[] names2 = getRuleSetNames();
        for (int i = 0; i < names2.length; i++) {
            names2[i] = names2[i].substring(1);
        }
        return names2;
    }

    public String[] getRuleSetDisplayNames() {
        return getRuleSetDisplayNames(ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getRuleSetDisplayName(String ruleSetName, ULocale loc) {
        String[] rsnames = this.publicRuleSetNames;
        for (int ix = 0; ix < rsnames.length; ix++) {
            if (rsnames[ix].equals(ruleSetName)) {
                String[] names = getNameListForLocale(loc);
                if (names != null) {
                    return names[ix];
                }
                return rsnames[ix].substring(1);
            }
        }
        throw new IllegalArgumentException("unrecognized rule set name: " + ruleSetName);
    }

    public String getRuleSetDisplayName(String ruleSetName) {
        return getRuleSetDisplayName(ruleSetName, ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String format(double number, String ruleSet) throws IllegalArgumentException {
        if (!ruleSet.startsWith("%%")) {
            return adjustForContext(format(number, findRuleSet(ruleSet)));
        }
        throw new IllegalArgumentException("Can't use internal rule set");
    }

    public String format(long number, String ruleSet) throws IllegalArgumentException {
        if (!ruleSet.startsWith("%%")) {
            return adjustForContext(format(number, findRuleSet(ruleSet)));
        }
        throw new IllegalArgumentException("Can't use internal rule set");
    }

    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition ignore) {
        if (toAppendTo.length() == 0) {
            toAppendTo.append(adjustForContext(format(number, this.defaultRuleSet)));
        } else {
            toAppendTo.append(format(number, this.defaultRuleSet));
        }
        return toAppendTo;
    }

    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition ignore) {
        if (toAppendTo.length() == 0) {
            toAppendTo.append(adjustForContext(format(number, this.defaultRuleSet)));
        } else {
            toAppendTo.append(format(number, this.defaultRuleSet));
        }
        return toAppendTo;
    }

    public StringBuffer format(BigInteger number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(new BigDecimal(number), toAppendTo, pos);
    }

    public StringBuffer format(java.math.BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(new BigDecimal(number), toAppendTo, pos);
    }

    public StringBuffer format(BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        if (MIN_VALUE.compareTo(number) > 0 || MAX_VALUE.compareTo(number) < 0) {
            return getDecimalFormat().format(number, toAppendTo, pos);
        }
        if (number.scale() == 0) {
            return format(number.longValue(), toAppendTo, pos);
        }
        return format(number.doubleValue(), toAppendTo, pos);
    }

    public Number parse(String text, ParsePosition parsePosition) {
        String workingText = text.substring(parsePosition.getIndex());
        ParsePosition workingPos = new ParsePosition(0);
        Number result = NFRule.ZERO;
        ParsePosition highWaterMark = new ParsePosition(workingPos.getIndex());
        for (int i = this.ruleSets.length - 1; i >= 0; i--) {
            if (this.ruleSets[i].isPublic() && this.ruleSets[i].isParseable()) {
                Number tempResult = this.ruleSets[i].parse(workingText, workingPos, Double.MAX_VALUE);
                if (workingPos.getIndex() > highWaterMark.getIndex()) {
                    result = tempResult;
                    highWaterMark.setIndex(workingPos.getIndex());
                }
                if (highWaterMark.getIndex() == workingText.length()) {
                    break;
                }
                workingPos.setIndex(0);
            }
        }
        parsePosition.setIndex(parsePosition.getIndex() + highWaterMark.getIndex());
        return result;
    }

    public void setLenientParseMode(boolean enabled) {
        this.lenientParse = enabled;
    }

    public boolean lenientParseEnabled() {
        return this.lenientParse;
    }

    public void setLenientScannerProvider(RbnfLenientScannerProvider scannerProvider2) {
        this.scannerProvider = scannerProvider2;
    }

    public RbnfLenientScannerProvider getLenientScannerProvider() {
        if (this.scannerProvider == null && this.lenientParse && !this.lookedForScanner) {
            try {
                this.lookedForScanner = true;
                setLenientScannerProvider((RbnfLenientScannerProvider) Class.forName("android.icu.impl.text.RbnfScannerProviderImpl").newInstance());
            } catch (Exception e) {
            }
        }
        return this.scannerProvider;
    }

    public void setDefaultRuleSet(String ruleSetName) {
        String currentName;
        if (ruleSetName == null) {
            if (this.publicRuleSetNames.length > 0) {
                this.defaultRuleSet = findRuleSet(this.publicRuleSetNames[0]);
            } else {
                this.defaultRuleSet = null;
                int n = this.ruleSets.length;
                do {
                    n--;
                    if (n < 0) {
                        int n2 = this.ruleSets.length;
                        while (true) {
                            n2--;
                            if (n2 >= 0) {
                                if (this.ruleSets[n2].isPublic()) {
                                    this.defaultRuleSet = this.ruleSets[n2];
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        currentName = this.ruleSets[n].getName();
                        if (currentName.equals("%spellout-numbering") || currentName.equals("%digits-ordinal")) {
                            this.defaultRuleSet = this.ruleSets[n];
                        }
                    }
                } while (!currentName.equals("%duration"));
                this.defaultRuleSet = this.ruleSets[n];
            }
        } else if (!ruleSetName.startsWith("%%")) {
            this.defaultRuleSet = findRuleSet(ruleSetName);
        } else {
            throw new IllegalArgumentException("cannot use private rule set: " + ruleSetName);
        }
    }

    public String getDefaultRuleSetName() {
        if (this.defaultRuleSet == null || !this.defaultRuleSet.isPublic()) {
            return "";
        }
        return this.defaultRuleSet.getName();
    }

    public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
        if (newSymbols != null) {
            this.decimalFormatSymbols = (DecimalFormatSymbols) newSymbols.clone();
            if (this.decimalFormat != null) {
                this.decimalFormat.setDecimalFormatSymbols(this.decimalFormatSymbols);
            }
            if (this.defaultInfinityRule != null) {
                this.defaultInfinityRule = null;
                getDefaultInfinityRule();
            }
            if (this.defaultNaNRule != null) {
                this.defaultNaNRule = null;
                getDefaultNaNRule();
            }
            for (NFRuleSet ruleSet : this.ruleSets) {
                ruleSet.setDecimalFormatSymbols(this.decimalFormatSymbols);
            }
        }
    }

    public void setContext(DisplayContext context) {
        super.setContext(context);
        if (!this.capitalizationInfoIsSet && (context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || context == DisplayContext.CAPITALIZATION_FOR_STANDALONE)) {
            initCapitalizationContextInfo(this.locale);
            this.capitalizationInfoIsSet = true;
        }
        if (this.capitalizationBrkIter != null) {
            return;
        }
        if (context == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE || ((context == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && this.capitalizationForListOrMenu) || (context == DisplayContext.CAPITALIZATION_FOR_STANDALONE && this.capitalizationForStandAlone))) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
        }
    }

    public int getRoundingMode() {
        return this.roundingMode;
    }

    public void setRoundingMode(int roundingMode2) {
        if (roundingMode2 < 0 || roundingMode2 > 7) {
            throw new IllegalArgumentException("Invalid rounding mode: " + roundingMode2);
        }
        this.roundingMode = roundingMode2;
    }

    /* access modifiers changed from: package-private */
    public NFRuleSet getDefaultRuleSet() {
        return this.defaultRuleSet;
    }

    /* access modifiers changed from: package-private */
    public RbnfLenientScanner getLenientScanner() {
        if (this.lenientParse) {
            RbnfLenientScannerProvider provider = getLenientScannerProvider();
            if (provider != null) {
                return provider.get(this.locale, this.lenientParseRules);
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        if (this.decimalFormatSymbols == null) {
            this.decimalFormatSymbols = new DecimalFormatSymbols(this.locale);
        }
        return this.decimalFormatSymbols;
    }

    /* access modifiers changed from: package-private */
    public DecimalFormat getDecimalFormat() {
        if (this.decimalFormat == null) {
            this.decimalFormat = new DecimalFormat(getPattern(this.locale, 0), getDecimalFormatSymbols());
        }
        return this.decimalFormat;
    }

    /* access modifiers changed from: package-private */
    public PluralFormat createPluralFormat(PluralRules.PluralType pluralType, String pattern) {
        return new PluralFormat(this.locale, pluralType, pattern, getDecimalFormat());
    }

    /* access modifiers changed from: package-private */
    public NFRule getDefaultInfinityRule() {
        if (this.defaultInfinityRule == null) {
            this.defaultInfinityRule = new NFRule(this, "Inf: " + getDecimalFormatSymbols().getInfinity());
        }
        return this.defaultInfinityRule;
    }

    /* access modifiers changed from: package-private */
    public NFRule getDefaultNaNRule() {
        if (this.defaultNaNRule == null) {
            this.defaultNaNRule = new NFRule(this, "NaN: " + getDecimalFormatSymbols().getNaN());
        }
        return this.defaultNaNRule;
    }

    private String extractSpecial(StringBuilder description, String specialName) {
        int lp = description.indexOf(specialName);
        if (lp == -1) {
            return null;
        }
        if (lp != 0 && description.charAt(lp - 1) != ';') {
            return null;
        }
        int lpEnd = description.indexOf(";%", lp);
        if (lpEnd == -1) {
            lpEnd = description.length() - 1;
        }
        int lpStart = specialName.length() + lp;
        while (lpStart < lpEnd && PatternProps.isWhiteSpace(description.charAt(lpStart))) {
            lpStart++;
        }
        String result = description.substring(lpStart, lpEnd);
        description.delete(lp, lpEnd + 1);
        return result;
    }

    private void init(String description, String[][] localizations) {
        int p;
        initLocalizations(localizations);
        StringBuilder descBuf = stripWhitespace(description);
        this.lenientParseRules = extractSpecial(descBuf, "%%lenient-parse:");
        this.postProcessRules = extractSpecial(descBuf, "%%post-process:");
        int numRuleSets = 1;
        int p2 = 0;
        while (true) {
            int indexOf = descBuf.indexOf(";%", p2);
            p = indexOf;
            if (indexOf == -1) {
                break;
            }
            numRuleSets++;
            p2 = p + 2;
        }
        this.ruleSets = new NFRuleSet[numRuleSets];
        this.ruleSetsMap = new HashMap((numRuleSets * 2) + 1);
        this.defaultRuleSet = null;
        String[] ruleSetDescriptions = new String[numRuleSets];
        int curRuleSet = 0;
        int publicRuleSetCount = 0;
        int publicRuleSetCount2 = p;
        int start = 0;
        while (curRuleSet < this.ruleSets.length) {
            int p3 = descBuf.indexOf(";%", start);
            if (p3 < 0) {
                p3 = descBuf.length() - 1;
            }
            ruleSetDescriptions[curRuleSet] = descBuf.substring(start, p3 + 1);
            NFRuleSet ruleSet = new NFRuleSet(this, ruleSetDescriptions, curRuleSet);
            this.ruleSets[curRuleSet] = ruleSet;
            String currentName = ruleSet.getName();
            this.ruleSetsMap.put(currentName, ruleSet);
            if (!currentName.startsWith("%%")) {
                publicRuleSetCount++;
                if ((this.defaultRuleSet == null && currentName.equals("%spellout-numbering")) || currentName.equals("%digits-ordinal") || currentName.equals("%duration")) {
                    this.defaultRuleSet = ruleSet;
                }
            }
            curRuleSet++;
            start = p3 + 1;
        }
        if (this.defaultRuleSet == null) {
            int i = this.ruleSets.length - 1;
            while (true) {
                if (i < 0) {
                    break;
                } else if (!this.ruleSets[i].getName().startsWith("%%")) {
                    this.defaultRuleSet = this.ruleSets[i];
                    break;
                } else {
                    i--;
                }
            }
        }
        if (this.defaultRuleSet == null) {
            this.defaultRuleSet = this.ruleSets[this.ruleSets.length - 1];
        }
        for (int i2 = 0; i2 < this.ruleSets.length; i2++) {
            this.ruleSets[i2].parseRules(ruleSetDescriptions[i2]);
        }
        String[] publicRuleSetTemp = new String[publicRuleSetCount];
        int publicRuleSetCount3 = 0;
        for (int i3 = this.ruleSets.length - 1; i3 >= 0; i3--) {
            if (!this.ruleSets[i3].getName().startsWith("%%")) {
                publicRuleSetTemp[publicRuleSetCount3] = this.ruleSets[i3].getName();
                publicRuleSetCount3++;
            }
        }
        if (this.publicRuleSetNames != null) {
            int i4 = 0;
            while (i4 < this.publicRuleSetNames.length) {
                String name = this.publicRuleSetNames[i4];
                int j = 0;
                while (j < publicRuleSetTemp.length) {
                    if (name.equals(publicRuleSetTemp[j])) {
                        i4++;
                    } else {
                        j++;
                    }
                }
                throw new IllegalArgumentException("did not find public rule set: " + name);
            }
            this.defaultRuleSet = findRuleSet(this.publicRuleSetNames[0]);
            return;
        }
        this.publicRuleSetNames = publicRuleSetTemp;
    }

    private void initLocalizations(String[][] localizations) {
        if (localizations != null) {
            this.publicRuleSetNames = (String[]) localizations[0].clone();
            Map<String, String[]> m = new HashMap<>();
            int i = 1;
            while (i < localizations.length) {
                String[] data = localizations[i];
                String loc = data[0];
                String[] names = new String[(data.length - 1)];
                if (names.length == this.publicRuleSetNames.length) {
                    System.arraycopy(data, 1, names, 0, names.length);
                    m.put(loc, names);
                    i++;
                } else {
                    throw new IllegalArgumentException("public name length: " + this.publicRuleSetNames.length + " != localized names[" + i + "] length: " + names.length);
                }
            }
            if (!m.isEmpty()) {
                this.ruleSetDisplayNames = m;
            }
        }
    }

    private void initCapitalizationContextInfo(ULocale theLocale) {
        try {
            int[] intVector = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, theLocale)).getWithFallback("contextTransforms/number-spellout").getIntVector();
            if (intVector.length >= 2) {
                boolean z = false;
                this.capitalizationForListOrMenu = intVector[0] != 0;
                if (intVector[1] != 0) {
                    z = true;
                }
                this.capitalizationForStandAlone = z;
            }
        } catch (MissingResourceException e) {
        }
    }

    private StringBuilder stripWhitespace(String description) {
        StringBuilder result = new StringBuilder();
        int descriptionLength = description.length();
        int start = 0;
        while (true) {
            if (start >= descriptionLength) {
                break;
            }
            while (start < descriptionLength && PatternProps.isWhiteSpace(description.charAt(start))) {
                start++;
            }
            if (start >= descriptionLength || description.charAt(start) != ';') {
                int p = description.indexOf(59, start);
                if (p != -1) {
                    if (p >= descriptionLength) {
                        break;
                    }
                    result.append(description.substring(start, p + 1));
                    start = p + 1;
                } else {
                    result.append(description.substring(start));
                    break;
                }
            } else {
                start++;
            }
        }
        return result;
    }

    private String format(double number, NFRuleSet ruleSet) {
        StringBuilder result = new StringBuilder();
        if (getRoundingMode() != 7 && !Double.isNaN(number) && !Double.isInfinite(number)) {
            number = new BigDecimal(Double.toString(number)).setScale(getMaximumFractionDigits(), this.roundingMode).doubleValue();
        }
        ruleSet.format(number, result, 0, 0);
        postProcess(result, ruleSet);
        return result.toString();
    }

    private String format(long number, NFRuleSet ruleSet) {
        StringBuilder result = new StringBuilder();
        if (number == Long.MIN_VALUE) {
            result.append(getDecimalFormat().format(Long.MIN_VALUE));
        } else {
            ruleSet.format(number, result, 0, 0);
        }
        postProcess(result, ruleSet);
        return result.toString();
    }

    private void postProcess(StringBuilder result, NFRuleSet ruleSet) {
        if (this.postProcessRules != null) {
            if (this.postProcessor == null) {
                int ix = this.postProcessRules.indexOf(";");
                if (ix == -1) {
                    ix = this.postProcessRules.length();
                }
                String ppClassName = this.postProcessRules.substring(0, ix).trim();
                try {
                    this.postProcessor = (RBNFPostProcessor) Class.forName(ppClassName).newInstance();
                    this.postProcessor.init(this, this.postProcessRules);
                } catch (Exception e) {
                    if (DEBUG) {
                        PrintStream printStream = System.out;
                        printStream.println("could not locate " + ppClassName + ", error " + e.getClass().getName() + ", " + e.getMessage());
                    }
                    this.postProcessor = null;
                    this.postProcessRules = null;
                    return;
                }
            }
            this.postProcessor.process(result, ruleSet);
        }
    }

    private String adjustForContext(String result) {
        DisplayContext capitalization = getContext(DisplayContext.Type.CAPITALIZATION);
        if (capitalization == DisplayContext.CAPITALIZATION_NONE || result == null || result.length() <= 0 || !UCharacter.isLowerCase(result.codePointAt(0)) || (capitalization != DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE && ((capitalization != DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || !this.capitalizationForListOrMenu) && (capitalization != DisplayContext.CAPITALIZATION_FOR_STANDALONE || !this.capitalizationForStandAlone)))) {
            return result;
        }
        if (this.capitalizationBrkIter == null) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
        }
        return UCharacter.toTitleCase(this.locale, result, this.capitalizationBrkIter, (int) CollationSettings.CASE_FIRST_AND_UPPER_MASK);
    }

    /* access modifiers changed from: package-private */
    public NFRuleSet findRuleSet(String name) throws IllegalArgumentException {
        NFRuleSet result = this.ruleSetsMap.get(name);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException("No rule set named " + name);
    }
}

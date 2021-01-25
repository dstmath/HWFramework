package ohos.global.icu.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.impl.CurrencyData;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.impl.locale.AsciiUtil;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.text.CaseMap;
import ohos.global.icu.text.DisplayContext;
import ohos.global.icu.text.LocaleDisplayNames;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class LocaleDisplayNamesImpl extends LocaleDisplayNames {
    private static final CaseMap.Title TO_TITLE_WHOLE_STRING_NO_LOWERCASE = CaseMap.toTitle().wholeString().noLowercase();
    private static final Cache cache = new Cache(null);
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap = new HashMap();
    private final DisplayContext capitalization;
    private transient BreakIterator capitalizationBrkIter;
    private boolean[] capitalizationUsage;
    private final CurrencyData.CurrencyDisplayInfo currencyDisplayInfo;
    private final LocaleDisplayNames.DialectHandling dialectHandling;
    private final String format;
    private final char formatCloseParen;
    private final char formatOpenParen;
    private final char formatReplaceCloseParen;
    private final char formatReplaceOpenParen;
    private final String keyTypeFormat;
    private final DataTable langData;
    private final ULocale locale;
    private final DisplayContext nameLength;
    private final DataTable regionData;
    private final String separatorFormat;
    private final DisplayContext substituteHandling;

    /* access modifiers changed from: private */
    public enum CapitalizationContextUsage {
        LANGUAGE,
        SCRIPT,
        TERRITORY,
        VARIANT,
        KEY,
        KEYVALUE
    }

    public enum DataTableType {
        LANG,
        REGION
    }

    static {
        contextUsageTypeMap.put("languages", CapitalizationContextUsage.LANGUAGE);
        contextUsageTypeMap.put(Constants.ELEMNAME_SCRIPT_STRING, CapitalizationContextUsage.SCRIPT);
        contextUsageTypeMap.put("territory", CapitalizationContextUsage.TERRITORY);
        contextUsageTypeMap.put("variant", CapitalizationContextUsage.VARIANT);
        contextUsageTypeMap.put("key", CapitalizationContextUsage.KEY);
        contextUsageTypeMap.put("keyValue", CapitalizationContextUsage.KEYVALUE);
    }

    private static String toTitleWholeStringNoLowercase(ULocale uLocale, String str) {
        return TO_TITLE_WHOLE_STRING_NO_LOWERCASE.apply(uLocale.toLocale(), null, str);
    }

    public static LocaleDisplayNames getInstance(ULocale uLocale, LocaleDisplayNames.DialectHandling dialectHandling2) {
        LocaleDisplayNames localeDisplayNames;
        synchronized (cache) {
            localeDisplayNames = cache.get(uLocale, dialectHandling2);
        }
        return localeDisplayNames;
    }

    public static LocaleDisplayNames getInstance(ULocale uLocale, DisplayContext... displayContextArr) {
        LocaleDisplayNames localeDisplayNames;
        synchronized (cache) {
            localeDisplayNames = cache.get(uLocale, displayContextArr);
        }
        return localeDisplayNames;
    }

    private final class CapitalizationContextSink extends UResource.Sink {
        boolean hasCapitalizationUsage;

        private CapitalizationContextSink() {
            this.hasCapitalizationUsage = false;
        }

        /* synthetic */ CapitalizationContextSink(LocaleDisplayNamesImpl localeDisplayNamesImpl, AnonymousClass1 r2) {
            this();
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                CapitalizationContextUsage capitalizationContextUsage = (CapitalizationContextUsage) LocaleDisplayNamesImpl.contextUsageTypeMap.get(key.toString());
                if (capitalizationContextUsage != null) {
                    int[] intVector = value.getIntVector();
                    if (intVector.length >= 2) {
                        if ((LocaleDisplayNamesImpl.this.capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? intVector[0] : intVector[1]) != 0) {
                            LocaleDisplayNamesImpl.this.capitalizationUsage[capitalizationContextUsage.ordinal()] = true;
                            this.hasCapitalizationUsage = true;
                        }
                    }
                }
            }
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x000d: APUT  
      (r0v1 ohos.global.icu.text.DisplayContext[])
      (0 ??[int, short, byte, char])
      (r4v1 ohos.global.icu.text.DisplayContext)
     */
    /* JADX WARNING: Illegal instructions before constructor call */
    public LocaleDisplayNamesImpl(ULocale uLocale, LocaleDisplayNames.DialectHandling dialectHandling2) {
        this(uLocale, r0);
        DisplayContext[] displayContextArr = new DisplayContext[2];
        displayContextArr[0] = dialectHandling2 == LocaleDisplayNames.DialectHandling.STANDARD_NAMES ? DisplayContext.STANDARD_NAMES : DisplayContext.DIALECT_NAMES;
        displayContextArr[1] = DisplayContext.CAPITALIZATION_NONE;
    }

    public LocaleDisplayNamesImpl(ULocale uLocale, DisplayContext... displayContextArr) {
        boolean z;
        ULocale uLocale2;
        boolean z2;
        this.capitalizationUsage = null;
        this.capitalizationBrkIter = null;
        LocaleDisplayNames.DialectHandling dialectHandling2 = LocaleDisplayNames.DialectHandling.STANDARD_NAMES;
        DisplayContext displayContext = DisplayContext.CAPITALIZATION_NONE;
        DisplayContext displayContext2 = DisplayContext.LENGTH_FULL;
        DisplayContext displayContext3 = DisplayContext.SUBSTITUTE;
        int length = displayContextArr.length;
        DisplayContext displayContext4 = displayContext3;
        DisplayContext displayContext5 = displayContext2;
        DisplayContext displayContext6 = displayContext;
        LocaleDisplayNames.DialectHandling dialectHandling3 = dialectHandling2;
        int i = 0;
        while (true) {
            z = true;
            if (i >= length) {
                break;
            }
            DisplayContext displayContext7 = displayContextArr[i];
            int i2 = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext$Type[displayContext7.type().ordinal()];
            if (i2 != 1) {
                if (i2 == 2) {
                    displayContext6 = displayContext7;
                } else if (i2 == 3) {
                    displayContext5 = displayContext7;
                } else if (i2 == 4) {
                    displayContext4 = displayContext7;
                }
            } else if (displayContext7.value() == DisplayContext.STANDARD_NAMES.value()) {
                dialectHandling3 = LocaleDisplayNames.DialectHandling.STANDARD_NAMES;
            } else {
                dialectHandling3 = LocaleDisplayNames.DialectHandling.DIALECT_NAMES;
            }
            i++;
        }
        this.dialectHandling = dialectHandling3;
        this.capitalization = displayContext6;
        this.nameLength = displayContext5;
        this.substituteHandling = displayContext4;
        this.langData = LangDataTables.impl.get(uLocale, displayContext4 == DisplayContext.NO_SUBSTITUTE);
        this.regionData = RegionDataTables.impl.get(uLocale, displayContext4 != DisplayContext.NO_SUBSTITUTE ? false : z);
        if (ULocale.ROOT.equals(this.langData.getLocale())) {
            uLocale2 = this.regionData.getLocale();
        } else {
            uLocale2 = this.langData.getLocale();
        }
        this.locale = uLocale2;
        String str = this.langData.get("localeDisplayPattern", "separator");
        str = (str == null || "separator".equals(str)) ? "{0}, {1}" : str;
        StringBuilder sb = new StringBuilder();
        this.separatorFormat = SimpleFormatterImpl.compileToStringMinMaxArguments(str, sb, 2, 2);
        String str2 = this.langData.get("localeDisplayPattern", "pattern");
        str2 = (str2 == null || "pattern".equals(str2)) ? "{0} ({1})" : str2;
        this.format = SimpleFormatterImpl.compileToStringMinMaxArguments(str2, sb, 2, 2);
        if (str2.contains("（")) {
            this.formatOpenParen = 65288;
            this.formatCloseParen = 65289;
            this.formatReplaceOpenParen = 65339;
            this.formatReplaceCloseParen = 65341;
        } else {
            this.formatOpenParen = '(';
            this.formatCloseParen = ')';
            this.formatReplaceOpenParen = '[';
            this.formatReplaceCloseParen = ']';
        }
        String str3 = this.langData.get("localeDisplayPattern", "keyTypePattern");
        this.keyTypeFormat = SimpleFormatterImpl.compileToStringMinMaxArguments((str3 == null || "keyTypePattern".equals(str3)) ? "{0}={1}" : str3, sb, 2, 2);
        if (displayContext6 == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || displayContext6 == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            this.capitalizationUsage = new boolean[CapitalizationContextUsage.values().length];
            ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
            CapitalizationContextSink capitalizationContextSink = new CapitalizationContextSink(this, null);
            try {
                bundleInstance.getAllItemsWithFallback("contextTransforms", capitalizationContextSink);
            } catch (MissingResourceException unused) {
            }
            z2 = capitalizationContextSink.hasCapitalizationUsage;
        } else {
            z2 = false;
        }
        if (z2 || displayContext6 == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(uLocale);
        }
        this.currencyDisplayInfo = CurrencyData.provider.getInstance(uLocale, false);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public ULocale getLocale() {
        return this.locale;
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public LocaleDisplayNames.DialectHandling getDialectHandling() {
        return this.dialectHandling;
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public DisplayContext getContext(DisplayContext.Type type) {
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext$Type[type.ordinal()];
        if (i == 1) {
            return this.dialectHandling == LocaleDisplayNames.DialectHandling.STANDARD_NAMES ? DisplayContext.STANDARD_NAMES : DisplayContext.DIALECT_NAMES;
        }
        if (i == 2) {
            return this.capitalization;
        }
        if (i == 3) {
            return this.nameLength;
        }
        if (i != 4) {
            return DisplayContext.STANDARD_NAMES;
        }
        return this.substituteHandling;
    }

    private String adjustForUsageAndContext(CapitalizationContextUsage capitalizationContextUsage, String str) {
        String titleCase;
        boolean[] zArr;
        if (str == null || str.length() <= 0 || !UCharacter.isLowerCase(str.codePointAt(0)) || (this.capitalization != DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE && ((zArr = this.capitalizationUsage) == null || !zArr[capitalizationContextUsage.ordinal()]))) {
            return str;
        }
        synchronized (this) {
            if (this.capitalizationBrkIter == null) {
                this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
            }
            titleCase = UCharacter.toTitleCase(this.locale, str, this.capitalizationBrkIter, 768);
        }
        return titleCase;
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String localeDisplayName(ULocale uLocale) {
        return localeDisplayNameInternal(uLocale);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String localeDisplayName(Locale locale2) {
        return localeDisplayNameInternal(ULocale.forLocale(locale2));
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String localeDisplayName(String str) {
        return localeDisplayNameInternal(new ULocale(str));
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00ec  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0108  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0129 A[LOOP:0: B:61:0x0129->B:83:0x0129, LOOP_START] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x019a  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x01a0  */
    private String localeDisplayNameInternal(ULocale uLocale) {
        String str;
        StringBuilder sb;
        Iterator keywords;
        String language = uLocale.getLanguage();
        if (language.isEmpty()) {
            language = "und";
        }
        String script = uLocale.getScript();
        String country = uLocale.getCountry();
        String variant = uLocale.getVariant();
        boolean z = script.length() > 0;
        boolean z2 = country.length() > 0;
        boolean z3 = variant.length() > 0;
        String str2 = null;
        if (this.dialectHandling == LocaleDisplayNames.DialectHandling.DIALECT_NAMES) {
            if (z && z2) {
                String str3 = language + '_' + script + '_' + country;
                str = localeIdName(str3);
                if (str != null && !str.equals(str3)) {
                    z = false;
                    z2 = false;
                    if (str == null) {
                        String localeIdName = localeIdName(language);
                        if (localeIdName == null) {
                            return null;
                        }
                        str = localeIdName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                    }
                    sb = new StringBuilder();
                    if (z) {
                        String scriptDisplayNameInContext = scriptDisplayNameInContext(script, true);
                        if (scriptDisplayNameInContext == null) {
                            return null;
                        }
                        sb.append(scriptDisplayNameInContext.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen));
                    }
                    if (z2) {
                        String regionDisplayName = regionDisplayName(country, true);
                        if (regionDisplayName == null) {
                            return null;
                        }
                        appendWithSep(regionDisplayName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen), sb);
                    }
                    if (z3) {
                        String variantDisplayName = variantDisplayName(variant, true);
                        if (variantDisplayName == null) {
                            return null;
                        }
                        appendWithSep(variantDisplayName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen), sb);
                    }
                    keywords = uLocale.getKeywords();
                    if (keywords != null) {
                        while (keywords.hasNext()) {
                            String str4 = (String) keywords.next();
                            String keywordValue = uLocale.getKeywordValue(str4);
                            String keyDisplayName = keyDisplayName(str4, true);
                            if (keyDisplayName == null) {
                                return null;
                            }
                            String replace = keyDisplayName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                            String keyValueDisplayName = keyValueDisplayName(str4, keywordValue, true);
                            if (keyValueDisplayName == null) {
                                return null;
                            }
                            String replace2 = keyValueDisplayName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                            if (!replace2.equals(keywordValue)) {
                                appendWithSep(replace2, sb);
                            } else if (!str4.equals(replace)) {
                                appendWithSep(SimpleFormatterImpl.formatCompiledPattern(this.keyTypeFormat, replace, replace2), sb);
                            } else {
                                StringBuilder appendWithSep = appendWithSep(replace, sb);
                                appendWithSep.append("=");
                                appendWithSep.append(replace2);
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        str2 = sb.toString();
                    }
                    if (str2 != null) {
                        str = SimpleFormatterImpl.formatCompiledPattern(this.format, str, str2);
                    }
                    return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, str);
                }
            }
            if (z) {
                String str5 = language + '_' + script;
                str = localeIdName(str5);
                if (str != null && !str.equals(str5)) {
                    z = false;
                    if (str == null) {
                    }
                    sb = new StringBuilder();
                    if (z) {
                    }
                    if (z2) {
                    }
                    if (z3) {
                    }
                    keywords = uLocale.getKeywords();
                    if (keywords != null) {
                    }
                    if (sb.length() > 0) {
                    }
                    if (str2 != null) {
                    }
                    return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, str);
                }
            }
            if (z2) {
                String str6 = language + '_' + country;
                String localeIdName2 = localeIdName(str6);
                if (localeIdName2 != null && !localeIdName2.equals(str6)) {
                    z2 = false;
                    str = localeIdName2;
                    if (str == null) {
                    }
                    sb = new StringBuilder();
                    if (z) {
                    }
                    if (z2) {
                    }
                    if (z3) {
                    }
                    keywords = uLocale.getKeywords();
                    if (keywords != null) {
                    }
                    if (sb.length() > 0) {
                    }
                    if (str2 != null) {
                    }
                    return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, str);
                }
            }
        }
        str = null;
        if (str == null) {
        }
        sb = new StringBuilder();
        if (z) {
        }
        if (z2) {
        }
        if (z3) {
        }
        keywords = uLocale.getKeywords();
        if (keywords != null) {
        }
        if (sb.length() > 0) {
        }
        if (str2 != null) {
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, str);
    }

    private String localeIdName(String str) {
        String str2;
        if (this.nameLength != DisplayContext.LENGTH_SHORT || (str2 = this.langData.get("Languages%short", str)) == null || str2.equals(str)) {
            return this.langData.get("Languages", str);
        }
        return str2;
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String languageDisplayName(String str) {
        String str2;
        if (str.equals(Constants.ELEMNAME_ROOT_STRING) || str.indexOf(95) != -1) {
            if (this.substituteHandling == DisplayContext.SUBSTITUTE) {
                return str;
            }
            return null;
        } else if (this.nameLength != DisplayContext.LENGTH_SHORT || (str2 = this.langData.get("Languages%short", str)) == null || str2.equals(str)) {
            return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, this.langData.get("Languages", str));
        } else {
            return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, str2);
        }
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String scriptDisplayName(String str) {
        String str2;
        String str3 = this.langData.get("Scripts%stand-alone", str);
        if (str3 == null || str3.equals(str)) {
            if (this.nameLength == DisplayContext.LENGTH_SHORT && (str2 = this.langData.get("Scripts%short", str)) != null && !str2.equals(str)) {
                return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str2);
            }
            str3 = this.langData.get("Scripts", str);
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str3);
    }

    private String scriptDisplayNameInContext(String str, boolean z) {
        String str2;
        if (this.nameLength == DisplayContext.LENGTH_SHORT && (str2 = this.langData.get("Scripts%short", str)) != null && !str2.equals(str)) {
            return z ? str2 : adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str2);
        }
        String str3 = this.langData.get("Scripts", str);
        return z ? str3 : adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str3);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String scriptDisplayNameInContext(String str) {
        return scriptDisplayNameInContext(str, false);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String scriptDisplayName(int i) {
        return scriptDisplayName(UScript.getShortName(i));
    }

    private String regionDisplayName(String str, boolean z) {
        String str2;
        if (this.nameLength == DisplayContext.LENGTH_SHORT && (str2 = this.regionData.get("Countries%short", str)) != null && !str2.equals(str)) {
            return z ? str2 : adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, str2);
        }
        String str3 = this.regionData.get("Countries", str);
        return z ? str3 : adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, str3);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String regionDisplayName(String str) {
        return regionDisplayName(str, false);
    }

    private String variantDisplayName(String str, boolean z) {
        String str2 = this.langData.get("Variants", str);
        return z ? str2 : adjustForUsageAndContext(CapitalizationContextUsage.VARIANT, str2);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String variantDisplayName(String str) {
        return variantDisplayName(str, false);
    }

    private String keyDisplayName(String str, boolean z) {
        String str2 = this.langData.get("Keys", str);
        return z ? str2 : adjustForUsageAndContext(CapitalizationContextUsage.KEY, str2);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String keyDisplayName(String str) {
        return keyDisplayName(str, false);
    }

    private String keyValueDisplayName(String str, String str2, boolean z) {
        String str3;
        if (str.equals("currency")) {
            String name = this.currencyDisplayInfo.getName(AsciiUtil.toUpperString(str2));
            if (name != null) {
                str2 = name;
            }
        } else {
            if (this.nameLength != DisplayContext.LENGTH_SHORT || (str3 = this.langData.get("Types%short", str, str2)) == null || str3.equals(str2)) {
                str3 = null;
            }
            str2 = str3 == null ? this.langData.get("Types", str, str2) : str3;
        }
        return z ? str2 : adjustForUsageAndContext(CapitalizationContextUsage.KEYVALUE, str2);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public String keyValueDisplayName(String str, String str2) {
        return keyValueDisplayName(str, str2, false);
    }

    @Override // ohos.global.icu.text.LocaleDisplayNames
    public List<LocaleDisplayNames.UiListItem> getUiListCompareWholeItems(Set<ULocale> set, Comparator<LocaleDisplayNames.UiListItem> comparator) {
        DisplayContext context = getContext(DisplayContext.Type.CAPITALIZATION);
        ArrayList arrayList = new ArrayList();
        HashMap hashMap = new HashMap();
        ULocale.Builder builder = new ULocale.Builder();
        for (ULocale uLocale : set) {
            builder.setLocale(uLocale);
            ULocale addLikelySubtags = ULocale.addLikelySubtags(uLocale);
            ULocale uLocale2 = new ULocale(addLikelySubtags.getLanguage());
            Set set2 = (Set) hashMap.get(uLocale2);
            if (set2 == null) {
                set2 = new HashSet();
                hashMap.put(uLocale2, set2);
            }
            set2.add(addLikelySubtags);
        }
        for (Map.Entry entry : hashMap.entrySet()) {
            ULocale uLocale3 = (ULocale) entry.getKey();
            Set<ULocale> set3 = (Set) entry.getValue();
            if (set3.size() == 1) {
                arrayList.add(newRow(ULocale.minimizeSubtags((ULocale) set3.iterator().next(), ULocale.Minimize.FAVOR_SCRIPT), context));
            } else {
                HashSet hashSet = new HashSet();
                HashSet hashSet2 = new HashSet();
                ULocale addLikelySubtags2 = ULocale.addLikelySubtags(uLocale3);
                hashSet.add(addLikelySubtags2.getScript());
                hashSet2.add(addLikelySubtags2.getCountry());
                for (ULocale uLocale4 : set3) {
                    hashSet.add(uLocale4.getScript());
                    hashSet2.add(uLocale4.getCountry());
                }
                int size = hashSet.size();
                boolean z = false;
                boolean z2 = size > 1;
                if (hashSet2.size() > 1) {
                    z = true;
                }
                for (ULocale uLocale5 : set3) {
                    ULocale.Builder locale2 = builder.setLocale(uLocale5);
                    if (!z2) {
                        locale2.setScript("");
                    }
                    if (!z) {
                        locale2.setRegion("");
                    }
                    arrayList.add(newRow(locale2.build(), context));
                }
            }
        }
        Collections.sort(arrayList, comparator);
        return arrayList;
    }

    private LocaleDisplayNames.UiListItem newRow(ULocale uLocale, DisplayContext displayContext) {
        ULocale minimizeSubtags = ULocale.minimizeSubtags(uLocale, ULocale.Minimize.FAVOR_SCRIPT);
        String displayName = uLocale.getDisplayName(this.locale);
        if (displayContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU) {
            displayName = toTitleWholeStringNoLowercase(this.locale, displayName);
        }
        String displayName2 = uLocale.getDisplayName(uLocale);
        if (displayContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU) {
            displayName2 = toTitleWholeStringNoLowercase(uLocale, displayName2);
        }
        return new LocaleDisplayNames.UiListItem(minimizeSubtags, uLocale, displayName, displayName2);
    }

    public static class DataTable {
        final boolean nullIfNotFound;

        DataTable(boolean z) {
            this.nullIfNotFound = z;
        }

        /* access modifiers changed from: package-private */
        public ULocale getLocale() {
            return ULocale.ROOT;
        }

        /* access modifiers changed from: package-private */
        public String get(String str, String str2) {
            return get(str, null, str2);
        }

        /* access modifiers changed from: package-private */
        public String get(String str, String str2, String str3) {
            if (this.nullIfNotFound) {
                return null;
            }
            return str3;
        }
    }

    static class ICUDataTable extends DataTable {
        private final ICUResourceBundle bundle;

        public ICUDataTable(String str, ULocale uLocale, boolean z) {
            super(z);
            this.bundle = UResourceBundle.getBundleInstance(str, uLocale.getBaseName());
        }

        @Override // ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTable
        public ULocale getLocale() {
            return this.bundle.getULocale();
        }

        @Override // ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTable
        public String get(String str, String str2, String str3) {
            return ICUResourceTableAccess.getTableString(this.bundle, str, str2, str3, this.nullIfNotFound ? null : str3);
        }
    }

    static abstract class DataTables {
        public abstract DataTable get(ULocale uLocale, boolean z);

        DataTables() {
        }

        public static DataTables load(String str) {
            try {
                return (DataTables) Class.forName(str).newInstance();
            } catch (Throwable unused) {
                return new DataTables() {
                    /* class ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTables.AnonymousClass1 */

                    @Override // ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTables
                    public DataTable get(ULocale uLocale, boolean z) {
                        return new DataTable(z);
                    }
                };
            }
        }
    }

    static abstract class ICUDataTables extends DataTables {
        private final String path;

        protected ICUDataTables(String str) {
            this.path = str;
        }

        @Override // ohos.global.icu.impl.LocaleDisplayNamesImpl.DataTables
        public DataTable get(ULocale uLocale, boolean z) {
            return new ICUDataTable(this.path, uLocale, z);
        }
    }

    static class LangDataTables {
        static final DataTables impl = DataTables.load("ohos.global.icu.impl.ICULangDataTables");

        LangDataTables() {
        }
    }

    static class RegionDataTables {
        static final DataTables impl = DataTables.load("ohos.global.icu.impl.ICURegionDataTables");

        RegionDataTables() {
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.LocaleDisplayNamesImpl$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$LocaleDisplayNamesImpl$DataTableType = new int[DataTableType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$DisplayContext$Type = new int[DisplayContext.Type.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$LocaleDisplayNamesImpl$DataTableType[DataTableType.LANG.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$LocaleDisplayNamesImpl$DataTableType[DataTableType.REGION.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext$Type[DisplayContext.Type.DIALECT_HANDLING.ordinal()] = 1;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext$Type[DisplayContext.Type.CAPITALIZATION.ordinal()] = 2;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext$Type[DisplayContext.Type.DISPLAY_LENGTH.ordinal()] = 3;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$DisplayContext$Type[DisplayContext.Type.SUBSTITUTE_HANDLING.ordinal()] = 4;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    public static boolean haveData(DataTableType dataTableType) {
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$LocaleDisplayNamesImpl$DataTableType[dataTableType.ordinal()];
        if (i == 1) {
            return LangDataTables.impl instanceof ICUDataTables;
        }
        if (i == 2) {
            return RegionDataTables.impl instanceof ICUDataTables;
        }
        throw new IllegalArgumentException("unknown type: " + dataTableType);
    }

    private StringBuilder appendWithSep(String str, StringBuilder sb) {
        if (sb.length() == 0) {
            sb.append(str);
        } else {
            SimpleFormatterImpl.formatAndReplace(this.separatorFormat, sb, null, sb, str);
        }
        return sb;
    }

    private static class Cache {
        private LocaleDisplayNames cache;
        private DisplayContext capitalization;
        private LocaleDisplayNames.DialectHandling dialectHandling;
        private ULocale locale;
        private DisplayContext nameLength;
        private DisplayContext substituteHandling;

        private Cache() {
        }

        /* synthetic */ Cache(AnonymousClass1 r1) {
            this();
        }

        public LocaleDisplayNames get(ULocale uLocale, LocaleDisplayNames.DialectHandling dialectHandling2) {
            if (!(dialectHandling2 == this.dialectHandling && DisplayContext.CAPITALIZATION_NONE == this.capitalization && DisplayContext.LENGTH_FULL == this.nameLength && DisplayContext.SUBSTITUTE == this.substituteHandling && uLocale.equals(this.locale))) {
                this.locale = uLocale;
                this.dialectHandling = dialectHandling2;
                this.capitalization = DisplayContext.CAPITALIZATION_NONE;
                this.nameLength = DisplayContext.LENGTH_FULL;
                this.substituteHandling = DisplayContext.SUBSTITUTE;
                this.cache = new LocaleDisplayNamesImpl(uLocale, dialectHandling2);
            }
            return this.cache;
        }

        public LocaleDisplayNames get(ULocale uLocale, DisplayContext... displayContextArr) {
            LocaleDisplayNames.DialectHandling dialectHandling2 = LocaleDisplayNames.DialectHandling.STANDARD_NAMES;
            DisplayContext displayContext = DisplayContext.CAPITALIZATION_NONE;
            DisplayContext displayContext2 = DisplayContext.LENGTH_FULL;
            DisplayContext displayContext3 = DisplayContext.SUBSTITUTE;
            for (DisplayContext displayContext4 : displayContextArr) {
                int i = AnonymousClass1.$SwitchMap$ohos$global$icu$text$DisplayContext$Type[displayContext4.type().ordinal()];
                if (i != 1) {
                    if (i == 2) {
                        displayContext = displayContext4;
                    } else if (i == 3) {
                        displayContext2 = displayContext4;
                    } else if (i == 4) {
                        displayContext3 = displayContext4;
                    }
                } else if (displayContext4.value() == DisplayContext.STANDARD_NAMES.value()) {
                    dialectHandling2 = LocaleDisplayNames.DialectHandling.STANDARD_NAMES;
                } else {
                    dialectHandling2 = LocaleDisplayNames.DialectHandling.DIALECT_NAMES;
                }
            }
            if (!(dialectHandling2 == this.dialectHandling && displayContext == this.capitalization && displayContext2 == this.nameLength && displayContext3 == this.substituteHandling && uLocale.equals(this.locale))) {
                this.locale = uLocale;
                this.dialectHandling = dialectHandling2;
                this.capitalization = displayContext;
                this.nameLength = displayContext2;
                this.substituteHandling = displayContext3;
                this.cache = new LocaleDisplayNamesImpl(uLocale, displayContextArr);
            }
            return this.cache;
        }
    }
}

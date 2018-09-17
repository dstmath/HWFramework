package android.icu.impl;

import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.impl.locale.AsciiUtil;
import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.BreakIterator;
import android.icu.text.DisplayContext;
import android.icu.text.DisplayContext.Type;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import android.icu.text.LocaleDisplayNames.UiListItem;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;
import android.icu.util.ULocale.Minimize;
import android.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

public class LocaleDisplayNamesImpl extends LocaleDisplayNames {
    private static final /* synthetic */ int[] -android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-text-DisplayContext$TypeSwitchesValues = null;
    private static final Cache cache = new Cache();
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap = new HashMap();
    private final DisplayContext capitalization;
    private transient BreakIterator capitalizationBrkIter;
    private boolean[] capitalizationUsage;
    private final CurrencyDisplayInfo currencyDisplayInfo;
    private final DialectHandling dialectHandling;
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

    static abstract class DataTables {
        public abstract DataTable get(ULocale uLocale, boolean z);

        DataTables() {
        }

        public static DataTables load(String className) {
            try {
                return (DataTables) Class.forName(className).newInstance();
            } catch (Throwable th) {
                return new DataTables() {
                    public DataTable get(ULocale locale, boolean nullIfNotFound) {
                        return new DataTable(nullIfNotFound);
                    }
                };
            }
        }
    }

    static abstract class ICUDataTables extends DataTables {
        private final String path;

        protected ICUDataTables(String path) {
            this.path = path;
        }

        public DataTable get(ULocale locale, boolean nullIfNotFound) {
            return new ICUDataTable(this.path, locale, nullIfNotFound);
        }
    }

    private static class Cache {
        private static final /* synthetic */ int[] -android-icu-text-DisplayContext$TypeSwitchesValues = null;
        private LocaleDisplayNames cache;
        private DisplayContext capitalization;
        private DialectHandling dialectHandling;
        private ULocale locale;
        private DisplayContext nameLength;
        private DisplayContext substituteHandling;

        private static /* synthetic */ int[] -getandroid-icu-text-DisplayContext$TypeSwitchesValues() {
            if (-android-icu-text-DisplayContext$TypeSwitchesValues != null) {
                return -android-icu-text-DisplayContext$TypeSwitchesValues;
            }
            int[] iArr = new int[Type.values().length];
            try {
                iArr[Type.CAPITALIZATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Type.DIALECT_HANDLING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Type.DISPLAY_LENGTH.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[Type.SUBSTITUTE_HANDLING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            -android-icu-text-DisplayContext$TypeSwitchesValues = iArr;
            return iArr;
        }

        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
        }

        public LocaleDisplayNames get(ULocale locale, DialectHandling dialectHandling) {
            boolean equals;
            if (dialectHandling == this.dialectHandling && DisplayContext.CAPITALIZATION_NONE == this.capitalization && DisplayContext.LENGTH_FULL == this.nameLength && DisplayContext.SUBSTITUTE == this.substituteHandling) {
                equals = locale.equals(this.locale);
            } else {
                equals = false;
            }
            if (!equals) {
                this.locale = locale;
                this.dialectHandling = dialectHandling;
                this.capitalization = DisplayContext.CAPITALIZATION_NONE;
                this.nameLength = DisplayContext.LENGTH_FULL;
                this.substituteHandling = DisplayContext.SUBSTITUTE;
                this.cache = new LocaleDisplayNamesImpl(locale, dialectHandling);
            }
            return this.cache;
        }

        public LocaleDisplayNames get(ULocale locale, DisplayContext... contexts) {
            boolean z = false;
            DialectHandling dialectHandlingIn = DialectHandling.STANDARD_NAMES;
            DisplayContext capitalizationIn = DisplayContext.CAPITALIZATION_NONE;
            DisplayContext nameLengthIn = DisplayContext.LENGTH_FULL;
            DisplayContext substituteHandling = DisplayContext.SUBSTITUTE;
            for (DisplayContext contextItem : contexts) {
                switch (-getandroid-icu-text-DisplayContext$TypeSwitchesValues()[contextItem.type().ordinal()]) {
                    case 1:
                        capitalizationIn = contextItem;
                        break;
                    case 2:
                        if (contextItem.value() != DisplayContext.STANDARD_NAMES.value()) {
                            dialectHandlingIn = DialectHandling.DIALECT_NAMES;
                            break;
                        }
                        dialectHandlingIn = DialectHandling.STANDARD_NAMES;
                        break;
                    case 3:
                        nameLengthIn = contextItem;
                        break;
                    case 4:
                        substituteHandling = contextItem;
                        break;
                    default:
                        break;
                }
            }
            if (dialectHandlingIn == this.dialectHandling && capitalizationIn == this.capitalization && nameLengthIn == this.nameLength && substituteHandling == this.substituteHandling) {
                z = locale.equals(this.locale);
            }
            if (!z) {
                this.locale = locale;
                this.dialectHandling = dialectHandlingIn;
                this.capitalization = capitalizationIn;
                this.nameLength = nameLengthIn;
                this.substituteHandling = substituteHandling;
                this.cache = new LocaleDisplayNamesImpl(locale, contexts);
            }
            return this.cache;
        }
    }

    private final class CapitalizationContextSink extends Sink {
        boolean hasCapitalizationUsage;

        /* synthetic */ CapitalizationContextSink(LocaleDisplayNamesImpl this$0, CapitalizationContextSink -this1) {
            this();
        }

        private CapitalizationContextSink() {
            this.hasCapitalizationUsage = false;
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table contextsTable = value.getTable();
            for (int i = 0; contextsTable.getKeyAndValue(i, key, value); i++) {
                CapitalizationContextUsage usage = (CapitalizationContextUsage) LocaleDisplayNamesImpl.contextUsageTypeMap.get(key.toString());
                if (usage != null) {
                    int[] intVector = value.getIntVector();
                    if (intVector.length >= 2) {
                        if ((LocaleDisplayNamesImpl.this.capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? intVector[0] : intVector[1]) != 0) {
                            LocaleDisplayNamesImpl.this.capitalizationUsage[usage.ordinal()] = true;
                            this.hasCapitalizationUsage = true;
                        }
                    }
                }
            }
        }
    }

    private enum CapitalizationContextUsage {
        LANGUAGE,
        SCRIPT,
        TERRITORY,
        VARIANT,
        KEY,
        KEYVALUE
    }

    public static class DataTable {
        final boolean nullIfNotFound;

        DataTable(boolean nullIfNotFound) {
            this.nullIfNotFound = nullIfNotFound;
        }

        ULocale getLocale() {
            return ULocale.ROOT;
        }

        String get(String tableName, String code) {
            return get(tableName, null, code);
        }

        String get(String tableName, String subTableName, String code) {
            return this.nullIfNotFound ? null : code;
        }
    }

    public enum DataTableType {
        LANG,
        REGION
    }

    static class ICUDataTable extends DataTable {
        private final ICUResourceBundle bundle;

        public ICUDataTable(String path, ULocale locale, boolean nullIfNotFound) {
            super(nullIfNotFound);
            this.bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(path, locale.getBaseName());
        }

        public ULocale getLocale() {
            return this.bundle.getULocale();
        }

        public String get(String tableName, String subTableName, String code) {
            String str;
            ICUResourceBundle iCUResourceBundle = this.bundle;
            if (this.nullIfNotFound) {
                str = null;
            } else {
                str = code;
            }
            return ICUResourceTableAccess.getTableString(iCUResourceBundle, tableName, subTableName, code, str);
        }
    }

    static class LangDataTables {
        static final DataTables impl = DataTables.load("android.icu.impl.ICULangDataTables");

        LangDataTables() {
        }
    }

    static class RegionDataTables {
        static final DataTables impl = DataTables.load("android.icu.impl.ICURegionDataTables");

        RegionDataTables() {
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues() {
        if (-android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues != null) {
            return -android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues;
        }
        int[] iArr = new int[DataTableType.values().length];
        try {
            iArr[DataTableType.LANG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataTableType.REGION.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-text-DisplayContext$TypeSwitchesValues() {
        if (-android-icu-text-DisplayContext$TypeSwitchesValues != null) {
            return -android-icu-text-DisplayContext$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.CAPITALIZATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.DIALECT_HANDLING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.DISPLAY_LENGTH.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Type.SUBSTITUTE_HANDLING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-icu-text-DisplayContext$TypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        contextUsageTypeMap.put("languages", CapitalizationContextUsage.LANGUAGE);
        contextUsageTypeMap.put("script", CapitalizationContextUsage.SCRIPT);
        contextUsageTypeMap.put("territory", CapitalizationContextUsage.TERRITORY);
        contextUsageTypeMap.put("variant", CapitalizationContextUsage.VARIANT);
        contextUsageTypeMap.put("key", CapitalizationContextUsage.KEY);
        contextUsageTypeMap.put("keyValue", CapitalizationContextUsage.KEYVALUE);
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DialectHandling dialectHandling) {
        LocaleDisplayNames localeDisplayNames;
        synchronized (cache) {
            localeDisplayNames = cache.get(locale, dialectHandling);
        }
        return localeDisplayNames;
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DisplayContext... contexts) {
        LocaleDisplayNames localeDisplayNames;
        synchronized (cache) {
            localeDisplayNames = cache.get(locale, contexts);
        }
        return localeDisplayNames;
    }

    public LocaleDisplayNamesImpl(ULocale locale, DialectHandling dialectHandling) {
        DisplayContext[] displayContextArr = new DisplayContext[2];
        displayContextArr[0] = dialectHandling == DialectHandling.STANDARD_NAMES ? DisplayContext.STANDARD_NAMES : DisplayContext.DIALECT_NAMES;
        displayContextArr[1] = DisplayContext.CAPITALIZATION_NONE;
        this(locale, displayContextArr);
    }

    public LocaleDisplayNamesImpl(ULocale locale, DisplayContext... contexts) {
        ULocale locale2;
        this.capitalizationUsage = null;
        this.capitalizationBrkIter = null;
        DialectHandling dialectHandling = DialectHandling.STANDARD_NAMES;
        DisplayContext capitalization = DisplayContext.CAPITALIZATION_NONE;
        DisplayContext nameLength = DisplayContext.LENGTH_FULL;
        DisplayContext substituteHandling = DisplayContext.SUBSTITUTE;
        for (DisplayContext contextItem : contexts) {
            switch (-getandroid-icu-text-DisplayContext$TypeSwitchesValues()[contextItem.type().ordinal()]) {
                case 1:
                    capitalization = contextItem;
                    break;
                case 2:
                    if (contextItem.value() != DisplayContext.STANDARD_NAMES.value()) {
                        dialectHandling = DialectHandling.DIALECT_NAMES;
                        break;
                    } else {
                        dialectHandling = DialectHandling.STANDARD_NAMES;
                        break;
                    }
                case 3:
                    nameLength = contextItem;
                    break;
                case 4:
                    substituteHandling = contextItem;
                    break;
                default:
                    break;
            }
        }
        this.dialectHandling = dialectHandling;
        this.capitalization = capitalization;
        this.nameLength = nameLength;
        this.substituteHandling = substituteHandling;
        this.langData = LangDataTables.impl.get(locale, substituteHandling == DisplayContext.NO_SUBSTITUTE);
        this.regionData = RegionDataTables.impl.get(locale, substituteHandling == DisplayContext.NO_SUBSTITUTE);
        if (ULocale.ROOT.equals(this.langData.getLocale())) {
            locale2 = this.regionData.getLocale();
        } else {
            locale2 = this.langData.getLocale();
        }
        this.locale = locale2;
        String sep = this.langData.get("localeDisplayPattern", "separator");
        if (sep == null || "separator".equals(sep)) {
            sep = "{0}, {1}";
        }
        StringBuilder sb = new StringBuilder();
        this.separatorFormat = SimpleFormatterImpl.compileToStringMinMaxArguments(sep, sb, 2, 2);
        String pattern = this.langData.get("localeDisplayPattern", "pattern");
        if (pattern == null || "pattern".equals(pattern)) {
            pattern = "{0} ({1})";
        }
        this.format = SimpleFormatterImpl.compileToStringMinMaxArguments(pattern, sb, 2, 2);
        if (pattern.contains("（")) {
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
        String keyTypePattern = this.langData.get("localeDisplayPattern", "keyTypePattern");
        if (keyTypePattern == null || "keyTypePattern".equals(keyTypePattern)) {
            keyTypePattern = "{0}={1}";
        }
        this.keyTypeFormat = SimpleFormatterImpl.compileToStringMinMaxArguments(keyTypePattern, sb, 2, 2);
        boolean needBrkIter = false;
        if (capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || capitalization == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            this.capitalizationUsage = new boolean[CapitalizationContextUsage.values().length];
            ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
            CapitalizationContextSink sink = new CapitalizationContextSink(this, null);
            try {
                rb.getAllItemsWithFallback("contextTransforms", sink);
            } catch (MissingResourceException e) {
            }
            needBrkIter = sink.hasCapitalizationUsage;
        }
        if (needBrkIter || capitalization == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(locale);
        }
        this.currencyDisplayInfo = CurrencyData.provider.getInstance(locale, false);
    }

    public ULocale getLocale() {
        return this.locale;
    }

    public DialectHandling getDialectHandling() {
        return this.dialectHandling;
    }

    public DisplayContext getContext(Type type) {
        switch (-getandroid-icu-text-DisplayContext$TypeSwitchesValues()[type.ordinal()]) {
            case 1:
                return this.capitalization;
            case 2:
                return this.dialectHandling == DialectHandling.STANDARD_NAMES ? DisplayContext.STANDARD_NAMES : DisplayContext.DIALECT_NAMES;
            case 3:
                return this.nameLength;
            case 4:
                return this.substituteHandling;
            default:
                return DisplayContext.STANDARD_NAMES;
        }
    }

    private String adjustForUsageAndContext(CapitalizationContextUsage usage, String name) {
        if (name == null || name.length() <= 0 || !UCharacter.isLowerCase(name.codePointAt(0)) || (this.capitalization != DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE && (this.capitalizationUsage == null || !this.capitalizationUsage[usage.ordinal()]))) {
            return name;
        }
        String toTitleCase;
        synchronized (this) {
            if (this.capitalizationBrkIter == null) {
                this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
            }
            toTitleCase = UCharacter.toTitleCase(this.locale, name, this.capitalizationBrkIter, 768);
        }
        return toTitleCase;
    }

    public String localeDisplayName(ULocale locale) {
        return localeDisplayNameInternal(locale);
    }

    public String localeDisplayName(Locale locale) {
        return localeDisplayNameInternal(ULocale.forLocale(locale));
    }

    public String localeDisplayName(String localeId) {
        return localeDisplayNameInternal(new ULocale(localeId));
    }

    private String localeDisplayNameInternal(ULocale locale) {
        String result;
        String resultName = null;
        String lang = locale.getLanguage();
        if (locale.getBaseName().length() == 0) {
            lang = "root";
        }
        String script = locale.getScript();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        boolean hasScript = script.length() > 0;
        boolean hasCountry = country.length() > 0;
        boolean hasVariant = variant.length() > 0;
        if (this.dialectHandling == DialectHandling.DIALECT_NAMES) {
            if (hasScript && hasCountry) {
                String langScriptCountry = lang + '_' + script + '_' + country;
                result = localeIdName(langScriptCountry);
                if (!(result == null || (result.equals(langScriptCountry) ^ 1) == 0)) {
                    resultName = result;
                    hasScript = false;
                    hasCountry = false;
                }
            }
            if (hasScript) {
                String langScript = lang + '_' + script;
                result = localeIdName(langScript);
                if (!(result == null || (result.equals(langScript) ^ 1) == 0)) {
                    resultName = result;
                    hasScript = false;
                }
            }
            if (hasCountry) {
                String langCountry = lang + '_' + country;
                result = localeIdName(langCountry);
                if (!(result == null || (result.equals(langCountry) ^ 1) == 0)) {
                    resultName = result;
                    hasCountry = false;
                }
            }
        }
        if (resultName == null) {
            result = localeIdName(lang);
            if (result == null) {
                return null;
            }
            resultName = result.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
        }
        StringBuilder buf = new StringBuilder();
        if (hasScript) {
            result = scriptDisplayNameInContext(script, true);
            if (result == null) {
                return null;
            }
            buf.append(result.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen));
        }
        if (hasCountry) {
            result = regionDisplayName(country, true);
            if (result == null) {
                return null;
            }
            appendWithSep(result.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen), buf);
        }
        if (hasVariant) {
            result = variantDisplayName(variant, true);
            if (result == null) {
                return null;
            }
            appendWithSep(result.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen), buf);
        }
        Iterator<String> keys = locale.getKeywords();
        if (keys != null) {
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = locale.getKeywordValue(key);
                String keyDisplayName = keyDisplayName(key, true);
                if (keyDisplayName == null) {
                    return null;
                }
                keyDisplayName = keyDisplayName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                String valueDisplayName = keyValueDisplayName(key, value, true);
                if (valueDisplayName == null) {
                    return null;
                }
                valueDisplayName = valueDisplayName.replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                if (!valueDisplayName.equals(value)) {
                    appendWithSep(valueDisplayName, buf);
                } else if (key.equals(keyDisplayName)) {
                    appendWithSep(keyDisplayName, buf).append("=").append(valueDisplayName);
                } else {
                    appendWithSep(SimpleFormatterImpl.formatCompiledPattern(this.keyTypeFormat, keyDisplayName, valueDisplayName), buf);
                }
            }
        }
        String resultRemainder = null;
        if (buf.length() > 0) {
            resultRemainder = buf.toString();
        }
        if (resultRemainder != null) {
            resultName = SimpleFormatterImpl.formatCompiledPattern(this.format, resultName, resultRemainder);
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, resultName);
    }

    private String localeIdName(String localeId) {
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            String locIdName = this.langData.get("Languages%short", localeId);
            if (!(locIdName == null || (locIdName.equals(localeId) ^ 1) == 0)) {
                return locIdName;
            }
        }
        return this.langData.get("Languages", localeId);
    }

    public String languageDisplayName(String lang) {
        if (lang.equals("root") || lang.indexOf(95) != -1) {
            if (this.substituteHandling != DisplayContext.SUBSTITUTE) {
                lang = null;
            }
            return lang;
        }
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            String langName = this.langData.get("Languages%short", lang);
            if (!(langName == null || (langName.equals(lang) ^ 1) == 0)) {
                return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, langName);
            }
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, this.langData.get("Languages", lang));
    }

    public String scriptDisplayName(String script) {
        String str = this.langData.get("Scripts%stand-alone", script);
        if (str == null || str.equals(script)) {
            if (this.nameLength == DisplayContext.LENGTH_SHORT) {
                str = this.langData.get("Scripts%short", script);
                if (!(str == null || (str.equals(script) ^ 1) == 0)) {
                    return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str);
                }
            }
            str = this.langData.get("Scripts", script);
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str);
    }

    private String scriptDisplayNameInContext(String script, boolean skipAdjust) {
        String scriptName;
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            scriptName = this.langData.get("Scripts%short", script);
            if (!(scriptName == null || (scriptName.equals(script) ^ 1) == 0)) {
                if (!skipAdjust) {
                    scriptName = adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, scriptName);
                }
                return scriptName;
            }
        }
        scriptName = this.langData.get("Scripts", script);
        if (!skipAdjust) {
            scriptName = adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, scriptName);
        }
        return scriptName;
    }

    public String scriptDisplayNameInContext(String script) {
        return scriptDisplayNameInContext(script, false);
    }

    public String scriptDisplayName(int scriptCode) {
        return scriptDisplayName(UScript.getShortName(scriptCode));
    }

    private String regionDisplayName(String region, boolean skipAdjust) {
        String regionName;
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            regionName = this.regionData.get("Countries%short", region);
            if (!(regionName == null || (regionName.equals(region) ^ 1) == 0)) {
                if (!skipAdjust) {
                    regionName = adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, regionName);
                }
                return regionName;
            }
        }
        regionName = this.regionData.get("Countries", region);
        if (!skipAdjust) {
            regionName = adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, regionName);
        }
        return regionName;
    }

    public String regionDisplayName(String region) {
        return regionDisplayName(region, false);
    }

    private String variantDisplayName(String variant, boolean skipAdjust) {
        String variantName = this.langData.get("Variants", variant);
        return skipAdjust ? variantName : adjustForUsageAndContext(CapitalizationContextUsage.VARIANT, variantName);
    }

    public String variantDisplayName(String variant) {
        return variantDisplayName(variant, false);
    }

    private String keyDisplayName(String key, boolean skipAdjust) {
        String keyName = this.langData.get("Keys", key);
        return skipAdjust ? keyName : adjustForUsageAndContext(CapitalizationContextUsage.KEY, keyName);
    }

    public String keyDisplayName(String key) {
        return keyDisplayName(key, false);
    }

    private String keyValueDisplayName(String key, String value, boolean skipAdjust) {
        String keyValueName = null;
        if (key.equals("currency")) {
            keyValueName = this.currencyDisplayInfo.getName(AsciiUtil.toUpperString(value));
            if (keyValueName == null) {
                keyValueName = value;
            }
        } else {
            if (this.nameLength == DisplayContext.LENGTH_SHORT) {
                String tmp = this.langData.get("Types%short", key, value);
                if (!(tmp == null || (tmp.equals(value) ^ 1) == 0)) {
                    keyValueName = tmp;
                }
            }
            if (keyValueName == null) {
                keyValueName = this.langData.get("Types", key, value);
            }
        }
        if (skipAdjust) {
            return keyValueName;
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.KEYVALUE, keyValueName);
    }

    public String keyValueDisplayName(String key, String value) {
        return keyValueDisplayName(key, value, false);
    }

    public List<UiListItem> getUiListCompareWholeItems(Set<ULocale> localeSet, Comparator<UiListItem> comparator) {
        ULocale base;
        DisplayContext capContext = getContext(Type.CAPITALIZATION);
        List<UiListItem> result = new ArrayList();
        Map<ULocale, Set<ULocale>> baseToLocales = new HashMap();
        Builder builder = new Builder();
        for (ULocale locOriginal : localeSet) {
            builder.setLocale(locOriginal);
            ULocale loc = ULocale.addLikelySubtags(locOriginal);
            base = new ULocale(loc.getLanguage());
            Set<ULocale> locales = (Set) baseToLocales.get(base);
            if (locales == null) {
                locales = new HashSet();
                baseToLocales.put(base, locales);
            }
            locales.add(loc);
        }
        for (Entry<ULocale, Set<ULocale>> entry : baseToLocales.entrySet()) {
            base = (ULocale) entry.getKey();
            Set<ULocale> values = (Set) entry.getValue();
            if (values.size() == 1) {
                result.add(newRow(ULocale.minimizeSubtags((ULocale) values.iterator().next(), Minimize.FAVOR_SCRIPT), capContext));
            } else {
                Set<String> scripts = new HashSet();
                Set<String> regions = new HashSet();
                ULocale maxBase = ULocale.addLikelySubtags(base);
                scripts.add(maxBase.getScript());
                regions.add(maxBase.getCountry());
                for (ULocale locale : values) {
                    scripts.add(locale.getScript());
                    regions.add(locale.getCountry());
                }
                boolean hasScripts = scripts.size() > 1;
                boolean hasRegions = regions.size() > 1;
                for (ULocale locale2 : values) {
                    Builder modified = builder.setLocale(locale2);
                    if (!hasScripts) {
                        modified.setScript("");
                    }
                    if (!hasRegions) {
                        modified.setRegion("");
                    }
                    result.add(newRow(modified.build(), capContext));
                }
            }
        }
        Collections.sort(result, comparator);
        return result;
    }

    private UiListItem newRow(ULocale modified, DisplayContext capContext) {
        ULocale minimized = ULocale.minimizeSubtags(modified, Minimize.FAVOR_SCRIPT);
        String tempName = modified.getDisplayName(this.locale);
        String nameInDisplayLocale = capContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? UCharacter.toTitleFirst(this.locale, tempName) : tempName;
        tempName = modified.getDisplayName(modified);
        return new UiListItem(minimized, modified, nameInDisplayLocale, capContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? UCharacter.toTitleFirst(modified, tempName) : tempName);
    }

    public static boolean haveData(DataTableType type) {
        switch (-getandroid-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                return LangDataTables.impl instanceof ICUDataTables;
            case 2:
                return RegionDataTables.impl instanceof ICUDataTables;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    private StringBuilder appendWithSep(String s, StringBuilder b) {
        if (b.length() == 0) {
            b.append(s);
        } else {
            SimpleFormatterImpl.formatAndReplace(this.separatorFormat, b, null, b, s);
        }
        return b;
    }
}

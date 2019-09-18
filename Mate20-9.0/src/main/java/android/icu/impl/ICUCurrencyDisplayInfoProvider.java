package android.icu.impl;

import android.icu.impl.CurrencyData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.UResource;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

public class ICUCurrencyDisplayInfoProvider implements CurrencyData.CurrencyDisplayInfoProvider {
    private volatile ICUCurrencyDisplayInfo currencyDisplayInfoCache = null;

    static class ICUCurrencyDisplayInfo extends CurrencyData.CurrencyDisplayInfo {
        final boolean fallback;
        private volatile FormattingData formattingDataCache = null;
        final ULocale locale;
        private volatile NarrowSymbol narrowSymbolCache = null;
        private volatile SoftReference<ParsingData> parsingDataCache = new SoftReference<>(null);
        private volatile String[] pluralsDataCache = null;
        private final ICUResourceBundle rb;
        private volatile CurrencyData.CurrencySpacingInfo spacingInfoCache = null;
        private volatile Map<String, String> unitPatternsCache = null;

        private static final class CurrencySink extends UResource.Sink {
            static final /* synthetic */ boolean $assertionsDisabled = false;
            final EntrypointTable entrypointTable;
            FormattingData formattingData = null;
            NarrowSymbol narrowSymbol = null;
            final boolean noRoot;
            ParsingData parsingData = null;
            String[] pluralsData = null;
            CurrencyData.CurrencySpacingInfo spacingInfo = null;
            Map<String, String> unitPatterns = null;

            enum EntrypointTable {
                TOP,
                CURRENCIES,
                CURRENCY_PLURALS,
                CURRENCY_NARROW,
                CURRENCY_SPACING,
                CURRENCY_UNIT_PATTERNS
            }

            static {
                Class<ICUCurrencyDisplayInfoProvider> cls = ICUCurrencyDisplayInfoProvider.class;
            }

            CurrencySink(boolean noRoot2, EntrypointTable entrypointTable2) {
                this.noRoot = noRoot2;
                this.entrypointTable = entrypointTable2;
            }

            public void put(UResource.Key key, UResource.Value value, boolean isRoot) {
                if (!this.noRoot || !isRoot) {
                    switch (this.entrypointTable) {
                        case TOP:
                            consumeTopTable(key, value);
                            break;
                        case CURRENCIES:
                            consumeCurrenciesEntry(key, value);
                            break;
                        case CURRENCY_PLURALS:
                            consumeCurrencyPluralsEntry(key, value);
                            break;
                        case CURRENCY_NARROW:
                            consumeCurrenciesNarrowEntry(key, value);
                            break;
                        case CURRENCY_SPACING:
                            consumeCurrencySpacingTable(key, value);
                            break;
                        case CURRENCY_UNIT_PATTERNS:
                            consumeCurrencyUnitPatternsTable(key, value);
                            break;
                    }
                }
            }

            private void consumeTopTable(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    if (key.contentEquals("Currencies")) {
                        consumeCurrenciesTable(key, value);
                    } else if (key.contentEquals("Currencies%variant")) {
                        consumeCurrenciesVariantTable(key, value);
                    } else if (key.contentEquals("CurrencyPlurals")) {
                        consumeCurrencyPluralsTable(key, value);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrenciesTable(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                int i = 0;
                while (table.getKeyAndValue(i, key, value)) {
                    String isoCode = key.toString();
                    if (value.getType() == 8) {
                        UResource.Array array = value.getArray();
                        this.parsingData.symbolToIsoCode.put(isoCode, isoCode);
                        array.getValue(0, value);
                        this.parsingData.symbolToIsoCode.put(value.getString(), isoCode);
                        array.getValue(1, value);
                        this.parsingData.nameToIsoCode.put(value.getString(), isoCode);
                        i++;
                    } else {
                        throw new ICUException("Unexpected data type in Currencies table for " + isoCode);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrenciesEntry(UResource.Key key, UResource.Value value) {
                String isoCode = key.toString();
                if (value.getType() == 8) {
                    UResource.Array array = value.getArray();
                    if (this.formattingData.symbol == null) {
                        array.getValue(0, value);
                        this.formattingData.symbol = value.getString();
                    }
                    if (this.formattingData.displayName == null) {
                        array.getValue(1, value);
                        this.formattingData.displayName = value.getString();
                    }
                    if (array.getSize() > 2 && this.formattingData.formatInfo == null) {
                        array.getValue(2, value);
                        UResource.Array formatArray = value.getArray();
                        formatArray.getValue(0, value);
                        String formatPattern = value.getString();
                        formatArray.getValue(1, value);
                        String decimalSeparator = value.getString();
                        formatArray.getValue(2, value);
                        String groupingSeparator = value.getString();
                        this.formattingData.formatInfo = new CurrencyData.CurrencyFormatInfo(isoCode, formatPattern, decimalSeparator, groupingSeparator);
                        return;
                    }
                    return;
                }
                throw new ICUException("Unexpected data type in Currencies table for " + isoCode);
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrenciesNarrowEntry(UResource.Key key, UResource.Value value) {
                if (this.narrowSymbol.narrowSymbol == null) {
                    this.narrowSymbol.narrowSymbol = value.getString();
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrenciesVariantTable(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    this.parsingData.symbolToIsoCode.put(value.getString(), key.toString());
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencyPluralsTable(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    String isoCode = key.toString();
                    UResource.Table pluralsTable = value.getTable();
                    int j = 0;
                    while (pluralsTable.getKeyAndValue(j, key, value)) {
                        if (StandardPlural.orNullFromString(key.toString()) != null) {
                            this.parsingData.nameToIsoCode.put(value.getString(), isoCode);
                            j++;
                        } else {
                            throw new ICUException("Could not make StandardPlural from keyword " + key);
                        }
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencyPluralsEntry(UResource.Key key, UResource.Value value) {
                UResource.Table pluralsTable = value.getTable();
                int j = 0;
                while (pluralsTable.getKeyAndValue(j, key, value)) {
                    StandardPlural plural = StandardPlural.orNullFromString(key.toString());
                    if (plural != null) {
                        if (this.pluralsData[plural.ordinal() + 1] == null) {
                            this.pluralsData[1 + plural.ordinal()] = value.getString();
                        }
                        j++;
                    } else {
                        throw new ICUException("Could not make StandardPlural from keyword " + key);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencySpacingTable(UResource.Key key, UResource.Value value) {
                CurrencyData.CurrencySpacingInfo.SpacingType type;
                CurrencyData.CurrencySpacingInfo.SpacingPattern pattern;
                UResource.Table spacingTypesTable = value.getTable();
                for (int i = 0; spacingTypesTable.getKeyAndValue(i, key, value); i++) {
                    if (key.contentEquals("beforeCurrency")) {
                        type = CurrencyData.CurrencySpacingInfo.SpacingType.BEFORE;
                        this.spacingInfo.hasBeforeCurrency = true;
                    } else if (key.contentEquals("afterCurrency")) {
                        type = CurrencyData.CurrencySpacingInfo.SpacingType.AFTER;
                        this.spacingInfo.hasAfterCurrency = true;
                    }
                    UResource.Table patternsTable = value.getTable();
                    for (int j = 0; patternsTable.getKeyAndValue(j, key, value); j++) {
                        if (key.contentEquals("currencyMatch")) {
                            pattern = CurrencyData.CurrencySpacingInfo.SpacingPattern.CURRENCY_MATCH;
                        } else if (key.contentEquals("surroundingMatch")) {
                            pattern = CurrencyData.CurrencySpacingInfo.SpacingPattern.SURROUNDING_MATCH;
                        } else if (key.contentEquals("insertBetween")) {
                            pattern = CurrencyData.CurrencySpacingInfo.SpacingPattern.INSERT_BETWEEN;
                        }
                        this.spacingInfo.setSymbolIfNull(type, pattern, value.getString());
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencyUnitPatternsTable(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    String pluralKeyword = key.toString();
                    if (this.unitPatterns.get(pluralKeyword) == null) {
                        this.unitPatterns.put(pluralKeyword, value.getString());
                    }
                }
            }
        }

        static class FormattingData {
            String displayName = null;
            CurrencyData.CurrencyFormatInfo formatInfo = null;
            final String isoCode;
            String symbol = null;

            FormattingData(String isoCode2) {
                this.isoCode = isoCode2;
            }
        }

        static class NarrowSymbol {
            final String isoCode;
            String narrowSymbol = null;

            NarrowSymbol(String isoCode2) {
                this.isoCode = isoCode2;
            }
        }

        static class ParsingData {
            Map<String, String> nameToIsoCode = new HashMap();
            Map<String, String> symbolToIsoCode = new HashMap();

            ParsingData() {
            }
        }

        public ICUCurrencyDisplayInfo(ULocale locale2, ICUResourceBundle rb2, boolean fallback2) {
            this.locale = locale2;
            this.fallback = fallback2;
            this.rb = rb2;
        }

        public ULocale getULocale() {
            return this.rb.getULocale();
        }

        public String getName(String isoCode) {
            FormattingData formattingData = fetchFormattingData(isoCode);
            if (formattingData.displayName != null || !this.fallback) {
                return formattingData.displayName;
            }
            return isoCode;
        }

        public String getSymbol(String isoCode) {
            FormattingData formattingData = fetchFormattingData(isoCode);
            if (formattingData.symbol != null || !this.fallback) {
                return formattingData.symbol;
            }
            return isoCode;
        }

        public String getNarrowSymbol(String isoCode) {
            NarrowSymbol narrowSymbol = fetchNarrowSymbol(isoCode);
            if (narrowSymbol.narrowSymbol != null || !this.fallback) {
                return narrowSymbol.narrowSymbol;
            }
            return isoCode;
        }

        public String getPluralName(String isoCode, String pluralKey) {
            StandardPlural plural = StandardPlural.orNullFromString(pluralKey);
            String[] pluralsData = fetchPluralsData(isoCode);
            String result = null;
            if (plural != null) {
                result = pluralsData[plural.ordinal() + 1];
            }
            if (result == null && this.fallback) {
                result = pluralsData[1 + StandardPlural.OTHER.ordinal()];
            }
            if (result == null && this.fallback) {
                result = fetchFormattingData(isoCode).displayName;
            }
            if (result != null || !this.fallback) {
                return result;
            }
            return isoCode;
        }

        public Map<String, String> symbolMap() {
            return fetchParsingData().symbolToIsoCode;
        }

        public Map<String, String> nameMap() {
            return fetchParsingData().nameToIsoCode;
        }

        public Map<String, String> getUnitPatterns() {
            return fetchUnitPatterns();
        }

        public CurrencyData.CurrencyFormatInfo getFormatInfo(String isoCode) {
            return fetchFormattingData(isoCode).formatInfo;
        }

        public CurrencyData.CurrencySpacingInfo getSpacingInfo() {
            CurrencyData.CurrencySpacingInfo spacingInfo = fetchSpacingInfo();
            if ((!spacingInfo.hasBeforeCurrency || !spacingInfo.hasAfterCurrency) && this.fallback) {
                return CurrencyData.CurrencySpacingInfo.DEFAULT;
            }
            return spacingInfo;
        }

        /* access modifiers changed from: package-private */
        public FormattingData fetchFormattingData(String isoCode) {
            FormattingData result = this.formattingDataCache;
            if (result != null && result.isoCode.equals(isoCode)) {
                return result;
            }
            FormattingData result2 = new FormattingData(isoCode);
            CurrencySink sink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCIES);
            sink.formattingData = result2;
            ICUResourceBundle iCUResourceBundle = this.rb;
            iCUResourceBundle.getAllItemsWithFallbackNoFail("Currencies/" + isoCode, sink);
            this.formattingDataCache = result2;
            return result2;
        }

        /* access modifiers changed from: package-private */
        public NarrowSymbol fetchNarrowSymbol(String isoCode) {
            NarrowSymbol result = this.narrowSymbolCache;
            if (result != null && result.isoCode.equals(isoCode)) {
                return result;
            }
            NarrowSymbol result2 = new NarrowSymbol(isoCode);
            CurrencySink sink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_NARROW);
            sink.narrowSymbol = result2;
            ICUResourceBundle iCUResourceBundle = this.rb;
            iCUResourceBundle.getAllItemsWithFallbackNoFail("Currencies%narrow/" + isoCode, sink);
            this.narrowSymbolCache = result2;
            return result2;
        }

        /* access modifiers changed from: package-private */
        public String[] fetchPluralsData(String isoCode) {
            String[] result = this.pluralsDataCache;
            if (result != null && result[0].equals(isoCode)) {
                return result;
            }
            String[] result2 = new String[(StandardPlural.COUNT + 1)];
            result2[0] = isoCode;
            CurrencySink sink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_PLURALS);
            sink.pluralsData = result2;
            ICUResourceBundle iCUResourceBundle = this.rb;
            iCUResourceBundle.getAllItemsWithFallbackNoFail("CurrencyPlurals/" + isoCode, sink);
            this.pluralsDataCache = result2;
            return result2;
        }

        /* access modifiers changed from: package-private */
        public ParsingData fetchParsingData() {
            ParsingData result = this.parsingDataCache.get();
            if (result != null) {
                return result;
            }
            ParsingData result2 = new ParsingData();
            CurrencySink sink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.TOP);
            sink.parsingData = result2;
            this.rb.getAllItemsWithFallback("", sink);
            this.parsingDataCache = new SoftReference<>(result2);
            return result2;
        }

        /* access modifiers changed from: package-private */
        public Map<String, String> fetchUnitPatterns() {
            Map<String, String> result = this.unitPatternsCache;
            if (result != null) {
                return result;
            }
            Map<String, String> result2 = new HashMap<>();
            CurrencySink sink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_UNIT_PATTERNS);
            sink.unitPatterns = result2;
            this.rb.getAllItemsWithFallback("CurrencyUnitPatterns", sink);
            this.unitPatternsCache = result2;
            return result2;
        }

        /* access modifiers changed from: package-private */
        public CurrencyData.CurrencySpacingInfo fetchSpacingInfo() {
            CurrencyData.CurrencySpacingInfo result = this.spacingInfoCache;
            if (result != null) {
                return result;
            }
            CurrencyData.CurrencySpacingInfo result2 = new CurrencyData.CurrencySpacingInfo();
            CurrencySink sink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_SPACING);
            sink.spacingInfo = result2;
            this.rb.getAllItemsWithFallback("currencySpacing", sink);
            this.spacingInfoCache = result2;
            return result2;
        }
    }

    public CurrencyData.CurrencyDisplayInfo getInstance(ULocale locale, boolean withFallback) {
        ICUResourceBundle rb;
        if (locale == null) {
            locale = ULocale.ROOT;
        }
        ICUCurrencyDisplayInfo instance = this.currencyDisplayInfoCache;
        if (instance == null || !instance.locale.equals(locale) || instance.fallback != withFallback) {
            if (withFallback) {
                rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale, ICUResourceBundle.OpenType.LOCALE_DEFAULT_ROOT);
            } else {
                try {
                    rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale, ICUResourceBundle.OpenType.LOCALE_ONLY);
                } catch (MissingResourceException e) {
                    return null;
                }
            }
            instance = new ICUCurrencyDisplayInfo(locale, rb, withFallback);
            this.currencyDisplayInfoCache = instance;
        }
        return instance;
    }

    public boolean hasData() {
        return true;
    }
}

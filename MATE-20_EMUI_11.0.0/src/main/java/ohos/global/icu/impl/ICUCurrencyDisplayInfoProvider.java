package ohos.global.icu.impl;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import ohos.global.icu.impl.CurrencyData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ULocale;

public class ICUCurrencyDisplayInfoProvider implements CurrencyData.CurrencyDisplayInfoProvider {
    private volatile ICUCurrencyDisplayInfo currencyDisplayInfoCache = null;

    @Override // ohos.global.icu.impl.CurrencyData.CurrencyDisplayInfoProvider
    public boolean hasData() {
        return true;
    }

    @Override // ohos.global.icu.impl.CurrencyData.CurrencyDisplayInfoProvider
    public CurrencyData.CurrencyDisplayInfo getInstance(ULocale uLocale, boolean z) {
        ICUResourceBundle iCUResourceBundle;
        if (uLocale == null) {
            uLocale = ULocale.ROOT;
        }
        ICUCurrencyDisplayInfo iCUCurrencyDisplayInfo = this.currencyDisplayInfoCache;
        if (iCUCurrencyDisplayInfo != null && iCUCurrencyDisplayInfo.locale.equals(uLocale) && iCUCurrencyDisplayInfo.fallback == z) {
            return iCUCurrencyDisplayInfo;
        }
        if (z) {
            iCUResourceBundle = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, uLocale, ICUResourceBundle.OpenType.LOCALE_DEFAULT_ROOT);
        } else {
            try {
                iCUResourceBundle = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, uLocale, ICUResourceBundle.OpenType.LOCALE_ONLY);
            } catch (MissingResourceException unused) {
                return null;
            }
        }
        ICUCurrencyDisplayInfo iCUCurrencyDisplayInfo2 = new ICUCurrencyDisplayInfo(uLocale, iCUResourceBundle, z);
        this.currencyDisplayInfoCache = iCUCurrencyDisplayInfo2;
        return iCUCurrencyDisplayInfo2;
    }

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

        /* access modifiers changed from: package-private */
        public static class FormattingData {
            String displayName = null;
            CurrencyData.CurrencyFormatInfo formatInfo = null;
            final String isoCode;
            String symbol = null;

            FormattingData(String str) {
                this.isoCode = str;
            }
        }

        /* access modifiers changed from: package-private */
        public static class NarrowSymbol {
            final String isoCode;
            String narrowSymbol = null;

            NarrowSymbol(String str) {
                this.isoCode = str;
            }
        }

        /* access modifiers changed from: package-private */
        public static class ParsingData {
            Map<String, String> nameToIsoCode = new HashMap();
            Map<String, String> symbolToIsoCode = new HashMap();

            ParsingData() {
            }
        }

        public ICUCurrencyDisplayInfo(ULocale uLocale, ICUResourceBundle iCUResourceBundle, boolean z) {
            this.locale = uLocale;
            this.fallback = z;
            this.rb = iCUResourceBundle;
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public ULocale getULocale() {
            return this.rb.getULocale();
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public String getName(String str) {
            FormattingData fetchFormattingData = fetchFormattingData(str);
            if (fetchFormattingData.displayName != null || !this.fallback) {
                return fetchFormattingData.displayName;
            }
            return str;
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public String getSymbol(String str) {
            FormattingData fetchFormattingData = fetchFormattingData(str);
            if (fetchFormattingData.symbol != null || !this.fallback) {
                return fetchFormattingData.symbol;
            }
            return str;
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public String getNarrowSymbol(String str) {
            NarrowSymbol fetchNarrowSymbol = fetchNarrowSymbol(str);
            if (fetchNarrowSymbol.narrowSymbol != null || !this.fallback) {
                return fetchNarrowSymbol.narrowSymbol;
            }
            return getSymbol(str);
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public String getPluralName(String str, String str2) {
            StandardPlural orNullFromString = StandardPlural.orNullFromString(str2);
            String[] fetchPluralsData = fetchPluralsData(str);
            String str3 = orNullFromString != null ? fetchPluralsData[orNullFromString.ordinal() + 1] : null;
            if (str3 == null && this.fallback) {
                str3 = fetchPluralsData[StandardPlural.OTHER.ordinal() + 1];
            }
            if (str3 == null && this.fallback) {
                str3 = fetchFormattingData(str).displayName;
            }
            return (str3 != null || !this.fallback) ? str3 : str;
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public Map<String, String> symbolMap() {
            return fetchParsingData().symbolToIsoCode;
        }

        @Override // ohos.global.icu.text.CurrencyDisplayNames
        public Map<String, String> nameMap() {
            return fetchParsingData().nameToIsoCode;
        }

        @Override // ohos.global.icu.impl.CurrencyData.CurrencyDisplayInfo
        public Map<String, String> getUnitPatterns() {
            return fetchUnitPatterns();
        }

        @Override // ohos.global.icu.impl.CurrencyData.CurrencyDisplayInfo
        public CurrencyData.CurrencyFormatInfo getFormatInfo(String str) {
            return fetchFormattingData(str).formatInfo;
        }

        @Override // ohos.global.icu.impl.CurrencyData.CurrencyDisplayInfo
        public CurrencyData.CurrencySpacingInfo getSpacingInfo() {
            CurrencyData.CurrencySpacingInfo fetchSpacingInfo = fetchSpacingInfo();
            return ((!fetchSpacingInfo.hasBeforeCurrency || !fetchSpacingInfo.hasAfterCurrency) && this.fallback) ? CurrencyData.CurrencySpacingInfo.DEFAULT : fetchSpacingInfo;
        }

        /* access modifiers changed from: package-private */
        public FormattingData fetchFormattingData(String str) {
            FormattingData formattingData = this.formattingDataCache;
            if (formattingData != null && formattingData.isoCode.equals(str)) {
                return formattingData;
            }
            FormattingData formattingData2 = new FormattingData(str);
            CurrencySink currencySink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCIES);
            currencySink.formattingData = formattingData2;
            ICUResourceBundle iCUResourceBundle = this.rb;
            iCUResourceBundle.getAllItemsWithFallbackNoFail("Currencies/" + str, currencySink);
            this.formattingDataCache = formattingData2;
            return formattingData2;
        }

        /* access modifiers changed from: package-private */
        public NarrowSymbol fetchNarrowSymbol(String str) {
            NarrowSymbol narrowSymbol = this.narrowSymbolCache;
            if (narrowSymbol != null && narrowSymbol.isoCode.equals(str)) {
                return narrowSymbol;
            }
            NarrowSymbol narrowSymbol2 = new NarrowSymbol(str);
            CurrencySink currencySink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_NARROW);
            currencySink.narrowSymbol = narrowSymbol2;
            ICUResourceBundle iCUResourceBundle = this.rb;
            iCUResourceBundle.getAllItemsWithFallbackNoFail("Currencies%narrow/" + str, currencySink);
            this.narrowSymbolCache = narrowSymbol2;
            return narrowSymbol2;
        }

        /* access modifiers changed from: package-private */
        public String[] fetchPluralsData(String str) {
            String[] strArr = this.pluralsDataCache;
            if (strArr != null && strArr[0].equals(str)) {
                return strArr;
            }
            String[] strArr2 = new String[(StandardPlural.COUNT + 1)];
            strArr2[0] = str;
            CurrencySink currencySink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_PLURALS);
            currencySink.pluralsData = strArr2;
            ICUResourceBundle iCUResourceBundle = this.rb;
            iCUResourceBundle.getAllItemsWithFallbackNoFail("CurrencyPlurals/" + str, currencySink);
            this.pluralsDataCache = strArr2;
            return strArr2;
        }

        /* access modifiers changed from: package-private */
        public ParsingData fetchParsingData() {
            ParsingData parsingData = this.parsingDataCache.get();
            if (parsingData != null) {
                return parsingData;
            }
            ParsingData parsingData2 = new ParsingData();
            CurrencySink currencySink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.TOP);
            currencySink.parsingData = parsingData2;
            this.rb.getAllItemsWithFallback("", currencySink);
            this.parsingDataCache = new SoftReference<>(parsingData2);
            return parsingData2;
        }

        /* access modifiers changed from: package-private */
        public Map<String, String> fetchUnitPatterns() {
            Map<String, String> map = this.unitPatternsCache;
            if (map != null) {
                return map;
            }
            HashMap hashMap = new HashMap();
            CurrencySink currencySink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_UNIT_PATTERNS);
            currencySink.unitPatterns = hashMap;
            this.rb.getAllItemsWithFallback("CurrencyUnitPatterns", currencySink);
            this.unitPatternsCache = hashMap;
            return hashMap;
        }

        /* access modifiers changed from: package-private */
        public CurrencyData.CurrencySpacingInfo fetchSpacingInfo() {
            CurrencyData.CurrencySpacingInfo currencySpacingInfo = this.spacingInfoCache;
            if (currencySpacingInfo != null) {
                return currencySpacingInfo;
            }
            CurrencyData.CurrencySpacingInfo currencySpacingInfo2 = new CurrencyData.CurrencySpacingInfo();
            CurrencySink currencySink = new CurrencySink(!this.fallback, CurrencySink.EntrypointTable.CURRENCY_SPACING);
            currencySink.spacingInfo = currencySpacingInfo2;
            this.rb.getAllItemsWithFallback("currencySpacing", currencySink);
            this.spacingInfoCache = currencySpacingInfo2;
            return currencySpacingInfo2;
        }

        /* access modifiers changed from: private */
        public static final class CurrencySink extends UResource.Sink {
            static final /* synthetic */ boolean $assertionsDisabled = false;
            final EntrypointTable entrypointTable;
            FormattingData formattingData = null;
            NarrowSymbol narrowSymbol = null;
            final boolean noRoot;
            ParsingData parsingData = null;
            String[] pluralsData = null;
            CurrencyData.CurrencySpacingInfo spacingInfo = null;
            Map<String, String> unitPatterns = null;

            /* access modifiers changed from: package-private */
            public enum EntrypointTable {
                TOP,
                CURRENCIES,
                CURRENCY_PLURALS,
                CURRENCY_NARROW,
                CURRENCY_SPACING,
                CURRENCY_UNIT_PATTERNS
            }

            CurrencySink(boolean z, EntrypointTable entrypointTable2) {
                this.noRoot = z;
                this.entrypointTable = entrypointTable2;
            }

            @Override // ohos.global.icu.impl.UResource.Sink
            public void put(UResource.Key key, UResource.Value value, boolean z) {
                if (!this.noRoot || !z) {
                    switch (this.entrypointTable) {
                        case TOP:
                            consumeTopTable(key, value);
                            return;
                        case CURRENCIES:
                            consumeCurrenciesEntry(key, value);
                            return;
                        case CURRENCY_PLURALS:
                            consumeCurrencyPluralsEntry(key, value);
                            return;
                        case CURRENCY_NARROW:
                            consumeCurrenciesNarrowEntry(key, value);
                            return;
                        case CURRENCY_SPACING:
                            consumeCurrencySpacingTable(key, value);
                            return;
                        case CURRENCY_UNIT_PATTERNS:
                            consumeCurrencyUnitPatternsTable(key, value);
                            return;
                        default:
                            return;
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
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    String key2 = key.toString();
                    if (value.getType() == 8) {
                        UResource.Array array = value.getArray();
                        this.parsingData.symbolToIsoCode.put(key2, key2);
                        array.getValue(0, value);
                        this.parsingData.symbolToIsoCode.put(value.getString(), key2);
                        array.getValue(1, value);
                        this.parsingData.nameToIsoCode.put(value.getString(), key2);
                    } else {
                        throw new ICUException("Unexpected data type in Currencies table for " + key2);
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrenciesEntry(UResource.Key key, UResource.Value value) {
                String key2 = key.toString();
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
                        UResource.Array array2 = value.getArray();
                        array2.getValue(0, value);
                        String string = value.getString();
                        array2.getValue(1, value);
                        String string2 = value.getString();
                        array2.getValue(2, value);
                        String string3 = value.getString();
                        this.formattingData.formatInfo = new CurrencyData.CurrencyFormatInfo(key2, string, string2, string3);
                        return;
                    }
                    return;
                }
                throw new ICUException("Unexpected data type in Currencies table for " + key2);
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
                    String key2 = key.toString();
                    UResource.Table table2 = value.getTable();
                    for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                        if (StandardPlural.orNullFromString(key.toString()) != null) {
                            this.parsingData.nameToIsoCode.put(value.getString(), key2);
                        } else {
                            throw new ICUException("Could not make StandardPlural from keyword " + ((Object) key));
                        }
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencyPluralsEntry(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    StandardPlural orNullFromString = StandardPlural.orNullFromString(key.toString());
                    if (orNullFromString != null) {
                        if (this.pluralsData[orNullFromString.ordinal() + 1] == null) {
                            this.pluralsData[orNullFromString.ordinal() + 1] = value.getString();
                        }
                    } else {
                        throw new ICUException("Could not make StandardPlural from keyword " + ((Object) key));
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencySpacingTable(UResource.Key key, UResource.Value value) {
                CurrencyData.CurrencySpacingInfo.SpacingType spacingType;
                CurrencyData.CurrencySpacingInfo.SpacingPattern spacingPattern;
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    if (key.contentEquals("beforeCurrency")) {
                        spacingType = CurrencyData.CurrencySpacingInfo.SpacingType.BEFORE;
                        this.spacingInfo.hasBeforeCurrency = true;
                    } else if (key.contentEquals("afterCurrency")) {
                        spacingType = CurrencyData.CurrencySpacingInfo.SpacingType.AFTER;
                        this.spacingInfo.hasAfterCurrency = true;
                    }
                    UResource.Table table2 = value.getTable();
                    for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                        if (key.contentEquals("currencyMatch")) {
                            spacingPattern = CurrencyData.CurrencySpacingInfo.SpacingPattern.CURRENCY_MATCH;
                        } else if (key.contentEquals("surroundingMatch")) {
                            spacingPattern = CurrencyData.CurrencySpacingInfo.SpacingPattern.SURROUNDING_MATCH;
                        } else if (key.contentEquals("insertBetween")) {
                            spacingPattern = CurrencyData.CurrencySpacingInfo.SpacingPattern.INSERT_BETWEEN;
                        }
                        this.spacingInfo.setSymbolIfNull(spacingType, spacingPattern, value.getString());
                    }
                }
            }

            /* access modifiers changed from: package-private */
            public void consumeCurrencyUnitPatternsTable(UResource.Key key, UResource.Value value) {
                UResource.Table table = value.getTable();
                for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                    String key2 = key.toString();
                    if (this.unitPatterns.get(key2) == null) {
                        this.unitPatterns.put(key2, value.getString());
                    }
                }
            }
        }
    }
}

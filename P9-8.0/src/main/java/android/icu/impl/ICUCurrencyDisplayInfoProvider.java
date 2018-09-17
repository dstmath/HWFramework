package android.icu.impl;

import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.CurrencyData.CurrencyDisplayInfoProvider;
import android.icu.impl.CurrencyData.CurrencyFormatInfo;
import android.icu.impl.CurrencyData.CurrencySpacingInfo;
import android.icu.impl.CurrencyData.CurrencySpacingInfo.SpacingPattern;
import android.icu.impl.CurrencyData.CurrencySpacingInfo.SpacingType;
import android.icu.impl.ICUResourceBundle.OpenType;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

public class ICUCurrencyDisplayInfoProvider implements CurrencyDisplayInfoProvider {

    static class ICUCurrencyDisplayInfo extends CurrencyDisplayInfo {
        private SoftReference<Map<String, String>> _nameMapRef;
        private SoftReference<Map<String, String>> _symbolMapRef;
        private final ICUResourceBundle currencies;
        private final boolean fallback;
        private final ICUResourceBundle plurals;
        private final ICUResourceBundle rb;

        private final class SpacingInfoSink extends Sink {
            boolean hasAfterCurrency;
            boolean hasBeforeCurrency;
            CurrencySpacingInfo spacingInfo;

            /* synthetic */ SpacingInfoSink(ICUCurrencyDisplayInfo this$1, SpacingInfoSink -this1) {
                this();
            }

            private SpacingInfoSink() {
                this.spacingInfo = new CurrencySpacingInfo();
                this.hasBeforeCurrency = false;
                this.hasAfterCurrency = false;
            }

            public void put(Key key, Value value, boolean noFallback) {
                Table spacingTypesTable = value.getTable();
                for (int i = 0; spacingTypesTable.getKeyAndValue(i, key, value); i++) {
                    SpacingType type;
                    if (key.contentEquals("beforeCurrency")) {
                        type = SpacingType.BEFORE;
                        this.hasBeforeCurrency = true;
                    } else if (key.contentEquals("afterCurrency")) {
                        type = SpacingType.AFTER;
                        this.hasAfterCurrency = true;
                    } else {
                    }
                    Table patternsTable = value.getTable();
                    for (int j = 0; patternsTable.getKeyAndValue(j, key, value); j++) {
                        SpacingPattern pattern;
                        if (key.contentEquals("currencyMatch")) {
                            pattern = SpacingPattern.CURRENCY_MATCH;
                        } else if (key.contentEquals("surroundingMatch")) {
                            pattern = SpacingPattern.SURROUNDING_MATCH;
                        } else if (key.contentEquals("insertBetween")) {
                            pattern = SpacingPattern.INSERT_BETWEEN;
                        } else {
                        }
                        this.spacingInfo.setSymbolIfNull(type, pattern, value.getString());
                    }
                }
            }

            CurrencySpacingInfo getSpacingInfo(boolean fallback) {
                if (this.hasBeforeCurrency && this.hasAfterCurrency) {
                    return this.spacingInfo;
                }
                if (fallback) {
                    return CurrencySpacingInfo.DEFAULT;
                }
                return null;
            }
        }

        public ICUCurrencyDisplayInfo(ICUResourceBundle rb, boolean fallback) {
            this.fallback = fallback;
            this.rb = rb;
            this.currencies = rb.findTopLevel("Currencies");
            this.plurals = rb.findTopLevel("CurrencyPlurals");
        }

        public ULocale getULocale() {
            return this.rb.getULocale();
        }

        public String getName(String isoCode) {
            return getName(isoCode, false);
        }

        public String getSymbol(String isoCode) {
            return getName(isoCode, true);
        }

        private String getName(String isoCode, boolean symbolName) {
            if (this.currencies != null) {
                ICUResourceBundle result = this.currencies.findWithFallback(isoCode);
                if (result != null) {
                    if (!this.fallback && (this.rb.isRoot() ^ 1) != 0 && result.isRoot()) {
                        return null;
                    }
                    return result.getString(symbolName ? 0 : 1);
                }
            }
            if (!this.fallback) {
                isoCode = null;
            }
            return isoCode;
        }

        public String getPluralName(String isoCode, String pluralKey) {
            String str = null;
            if (this.plurals != null) {
                ICUResourceBundle pluralsBundle = this.plurals.findWithFallback(isoCode);
                if (pluralsBundle != null) {
                    String pluralName = pluralsBundle.findStringWithFallback(pluralKey);
                    if (pluralName == null) {
                        if (!this.fallback) {
                            return null;
                        }
                        pluralName = pluralsBundle.findStringWithFallback("other");
                        if (pluralName == null) {
                            return getName(isoCode);
                        }
                    }
                    return pluralName;
                }
            }
            if (this.fallback) {
                str = getName(isoCode);
            }
            return str;
        }

        public Map<String, String> symbolMap() {
            Map<String, String> map = this._symbolMapRef == null ? null : (Map) this._symbolMapRef.get();
            if (map != null) {
                return map;
            }
            map = _createSymbolMap();
            this._symbolMapRef = new SoftReference(map);
            return map;
        }

        public Map<String, String> nameMap() {
            Map<String, String> map = this._nameMapRef == null ? null : (Map) this._nameMapRef.get();
            if (map != null) {
                return map;
            }
            map = _createNameMap();
            this._nameMapRef = new SoftReference(map);
            return map;
        }

        public Map<String, String> getUnitPatterns() {
            Map<String, String> result = new HashMap();
            for (ULocale locale = this.rb.getULocale(); locale != null; locale = locale.getFallback()) {
                ICUResourceBundle r = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale);
                if (r != null) {
                    ICUResourceBundle cr = r.findWithFallback("CurrencyUnitPatterns");
                    if (cr != null) {
                        int size = cr.getSize();
                        for (int index = 0; index < size; index++) {
                            ICUResourceBundle b = (ICUResourceBundle) cr.get(index);
                            String key = b.getKey();
                            if (!result.containsKey(key)) {
                                result.put(key, b.getString());
                            }
                        }
                    }
                }
            }
            return Collections.unmodifiableMap(result);
        }

        public CurrencyFormatInfo getFormatInfo(String isoCode) {
            ICUResourceBundle crb = this.currencies.findWithFallback(isoCode);
            if (crb != null && crb.getSize() > 2) {
                crb = crb.at(2);
                if (crb != null) {
                    return new CurrencyFormatInfo(crb.getString(0), crb.getString(1), crb.getString(2));
                }
            }
            return null;
        }

        public CurrencySpacingInfo getSpacingInfo() {
            SpacingInfoSink sink = new SpacingInfoSink(this, null);
            this.rb.getAllItemsWithFallback("currencySpacing", sink);
            return sink.getSpacingInfo(this.fallback);
        }

        private Map<String, String> _createSymbolMap() {
            Map<String, String> result = new HashMap();
            for (ULocale locale = this.rb.getULocale(); locale != null; locale = locale.getFallback()) {
                ICUResourceBundle curr = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale)).findTopLevel("Currencies");
                if (curr != null) {
                    for (int i = 0; i < curr.getSize(); i++) {
                        ICUResourceBundle item = curr.at(i);
                        String isoCode = item.getKey();
                        if (!result.containsKey(isoCode)) {
                            result.put(isoCode, isoCode);
                            result.put(item.getString(0), isoCode);
                        }
                    }
                }
            }
            return Collections.unmodifiableMap(result);
        }

        private Map<String, String> _createNameMap() {
            Map<String, String> result = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            Set<String> visited = new HashSet();
            Map<String, Set<String>> visitedPlurals = new HashMap();
            for (ULocale locale = this.rb.getULocale(); locale != null; locale = locale.getFallback()) {
                int i;
                ICUResourceBundle item;
                String isoCode;
                ICUResourceBundle bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale);
                ICUResourceBundle curr = bundle.findTopLevel("Currencies");
                if (curr != null) {
                    for (i = 0; i < curr.getSize(); i++) {
                        item = curr.at(i);
                        isoCode = item.getKey();
                        if (!visited.contains(isoCode)) {
                            visited.add(isoCode);
                            result.put(item.getString(1), isoCode);
                        }
                    }
                }
                ICUResourceBundle plurals = bundle.findTopLevel("CurrencyPlurals");
                if (plurals != null) {
                    for (i = 0; i < plurals.getSize(); i++) {
                        item = plurals.at(i);
                        isoCode = item.getKey();
                        Set<String> pluralSet = (Set) visitedPlurals.get(isoCode);
                        if (pluralSet == null) {
                            pluralSet = new HashSet();
                            visitedPlurals.put(isoCode, pluralSet);
                        }
                        for (int j = 0; j < item.getSize(); j++) {
                            ICUResourceBundle plural = item.at(j);
                            String pluralType = plural.getKey();
                            if (!pluralSet.contains(pluralType)) {
                                result.put(plural.getString(), isoCode);
                                pluralSet.add(pluralType);
                            }
                        }
                    }
                }
            }
            return Collections.unmodifiableMap(result);
        }
    }

    public CurrencyDisplayInfo getInstance(ULocale locale, boolean withFallback) {
        ICUResourceBundle rb;
        if (withFallback) {
            rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale, OpenType.LOCALE_DEFAULT_ROOT);
        } else {
            try {
                rb = ICUResourceBundle.getBundleInstance(ICUData.ICU_CURR_BASE_NAME, locale, OpenType.LOCALE_ONLY);
            } catch (MissingResourceException e) {
                return null;
            }
        }
        return new ICUCurrencyDisplayInfo(rb, withFallback);
    }

    public boolean hasData() {
        return true;
    }
}

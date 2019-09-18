package android.icu.util;

import android.icu.impl.CacheBase;
import android.icu.impl.CurrencyData;
import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUDebug;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import android.icu.impl.SoftCache;
import android.icu.impl.TextTrieMap;
import android.icu.impl.locale.LanguageTag;
import android.icu.text.CurrencyDisplayNames;
import android.icu.text.CurrencyMetaInfo;
import android.icu.util.MeasureUnit;
import android.icu.util.ULocale;
import dalvik.system.VMRuntime;
import java.io.ObjectStreamException;
import java.lang.ref.SoftReference;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

public class Currency extends MeasureUnit {
    private static SoftReference<Set<String>> ALL_CODES_AS_SET = null;
    private static SoftReference<List<String>> ALL_TENDER_CODES = null;
    private static ICUCache<ULocale, List<TextTrieMap<CurrencyStringInfo>>> CURRENCY_NAME_CACHE = new SimpleCache();
    private static final boolean DEBUG = ICUDebug.enabled("currency");
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final EquivalenceRelation<String> EQUIVALENT_CURRENCY_SYMBOLS = new EquivalenceRelation().add("¥", "￥").add("$", "﹩", "＄").add("₨", "₹").add("£", "₤");
    private static final String EUR_STR = "EUR";
    public static final int LONG_NAME = 1;
    @Deprecated
    public static final int NARROW_SYMBOL_NAME = 3;
    public static final int PLURAL_LONG_NAME = 2;
    private static final int[] POW10 = {1, 10, 100, 1000, VMRuntime.SDK_VERSION_CUR_DEVELOPMENT, 100000, 1000000, 10000000, 100000000, 1000000000};
    public static final int SYMBOL_NAME = 0;
    private static final ULocale UND = new ULocale("und");
    private static final CacheBase<String, Currency, Void> regionCurrencyCache = new SoftCache<String, Currency, Void>() {
        /* access modifiers changed from: protected */
        public Currency createInstance(String key, Void unused) {
            return Currency.loadCurrency(key);
        }
    };
    private static final long serialVersionUID = -5839973855554750484L;
    private static ServiceShim shim;
    private final String isoCode;

    private static class CurrencyNameResultHandler implements TextTrieMap.ResultHandler<CurrencyStringInfo> {
        private String bestCurrencyISOCode;
        private int bestMatchLength;

        private CurrencyNameResultHandler() {
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<CurrencyStringInfo> values) {
            if (values.hasNext()) {
                this.bestCurrencyISOCode = values.next().getISOCode();
                this.bestMatchLength = matchLength;
            }
            return true;
        }

        public String getBestCurrencyISOCode() {
            return this.bestCurrencyISOCode;
        }

        public int getBestMatchLength() {
            return this.bestMatchLength;
        }
    }

    @Deprecated
    public static final class CurrencyStringInfo {
        private String currencyString;
        private String isoCode;

        @Deprecated
        public CurrencyStringInfo(String isoCode2, String currencyString2) {
            this.isoCode = isoCode2;
            this.currencyString = currencyString2;
        }

        @Deprecated
        public String getISOCode() {
            return this.isoCode;
        }

        @Deprecated
        public String getCurrencyString() {
            return this.currencyString;
        }
    }

    public enum CurrencyUsage {
        STANDARD,
        CASH
    }

    private static final class EquivalenceRelation<T> {
        private Map<T, Set<T>> data;

        private EquivalenceRelation() {
            this.data = new HashMap();
        }

        public EquivalenceRelation<T> add(T... items) {
            Set<T> group = new HashSet<>();
            int length = items.length;
            int i = 0;
            while (i < length) {
                T item = items[i];
                if (!this.data.containsKey(item)) {
                    group.add(item);
                    i++;
                } else {
                    throw new IllegalArgumentException("All groups passed to add must be disjoint.");
                }
            }
            for (T item2 : items) {
                this.data.put(item2, group);
            }
            return this;
        }

        public Set<T> get(T item) {
            Set<T> result = this.data.get(item);
            if (result == null) {
                return Collections.singleton(item);
            }
            return Collections.unmodifiableSet(result);
        }
    }

    static abstract class ServiceShim {
        /* access modifiers changed from: package-private */
        public abstract Currency createInstance(ULocale uLocale);

        /* access modifiers changed from: package-private */
        public abstract Locale[] getAvailableLocales();

        /* access modifiers changed from: package-private */
        public abstract ULocale[] getAvailableULocales();

        /* access modifiers changed from: package-private */
        public abstract Object registerInstance(Currency currency, ULocale uLocale);

        /* access modifiers changed from: package-private */
        public abstract boolean unregister(Object obj);

        ServiceShim() {
        }
    }

    private static ServiceShim getShim() {
        if (shim == null) {
            try {
                shim = (ServiceShim) Class.forName("android.icu.util.CurrencyServiceShim").newInstance();
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
                throw new RuntimeException(e.getMessage());
            }
        }
        return shim;
    }

    public static Currency getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    public static Currency getInstance(ULocale locale) {
        String currency = locale.getKeywordValue("currency");
        if (currency != null) {
            return getInstance(currency);
        }
        if (shim == null) {
            return createCurrency(locale);
        }
        return shim.createInstance(locale);
    }

    public static String[] getAvailableCurrencyCodes(ULocale loc, Date d) {
        List<String> list = getTenderCurrencies(CurrencyMetaInfo.CurrencyFilter.onDate(d).withRegion(ULocale.getRegionForSupplementalData(loc, false)));
        if (list.isEmpty()) {
            return null;
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static String[] getAvailableCurrencyCodes(Locale loc, Date d) {
        return getAvailableCurrencyCodes(ULocale.forLocale(loc), d);
    }

    public static Set<Currency> getAvailableCurrencies() {
        List<String> list = CurrencyMetaInfo.getInstance().currencies(CurrencyMetaInfo.CurrencyFilter.all());
        HashSet<Currency> resultSet = new HashSet<>(list.size());
        for (String code : list) {
            resultSet.add(getInstance(code));
        }
        return resultSet;
    }

    static Currency createCurrency(ULocale loc) {
        String variant = loc.getVariant();
        if ("EURO".equals(variant)) {
            return getInstance(EUR_STR);
        }
        String key = ULocale.getRegionForSupplementalData(loc, false);
        if ("PREEURO".equals(variant)) {
            key = key + '-';
        }
        return regionCurrencyCache.getInstance(key, null);
    }

    /* access modifiers changed from: private */
    public static Currency loadCurrency(String key) {
        boolean isPreEuro;
        String region;
        if (key.endsWith(LanguageTag.SEP)) {
            region = key.substring(0, key.length() - 1);
            isPreEuro = true;
        } else {
            region = key;
            isPreEuro = false;
        }
        List<String> list = CurrencyMetaInfo.getInstance().currencies(CurrencyMetaInfo.CurrencyFilter.onRegion(region));
        if (list.isEmpty()) {
            return null;
        }
        String code = list.get(0);
        if (isPreEuro && EUR_STR.equals(code)) {
            if (list.size() < 2) {
                return null;
            }
            code = list.get(1);
        }
        return getInstance(code);
    }

    public static Currency getInstance(String theISOCode) {
        if (theISOCode == null) {
            throw new NullPointerException("The input currency code is null.");
        } else if (isAlpha3Code(theISOCode)) {
            return (Currency) MeasureUnit.internalGetInstance("currency", theISOCode.toUpperCase(Locale.ENGLISH));
        } else {
            throw new IllegalArgumentException("The input currency code is not 3-letter alphabetic code.");
        }
    }

    private static boolean isAlpha3Code(String code) {
        if (code.length() != 3) {
            return false;
        }
        for (int i = 0; i < 3; i++) {
            char ch = code.charAt(i);
            if (ch < 'A' || ((ch > 'Z' && ch < 'a') || ch > 'z')) {
                return false;
            }
        }
        return true;
    }

    public static Currency fromJavaCurrency(java.util.Currency currency) {
        return getInstance(currency.getCurrencyCode());
    }

    public java.util.Currency toJavaCurrency() {
        return java.util.Currency.getInstance(getCurrencyCode());
    }

    public static Object registerInstance(Currency currency, ULocale locale) {
        return getShim().registerInstance(currency, locale);
    }

    public static boolean unregister(Object registryKey) {
        if (registryKey == null) {
            throw new IllegalArgumentException("registryKey must not be null");
        } else if (shim == null) {
            return false;
        } else {
            return shim.unregister(registryKey);
        }
    }

    public static Locale[] getAvailableLocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableLocales();
        }
        return shim.getAvailableLocales();
    }

    public static ULocale[] getAvailableULocales() {
        if (shim == null) {
            return ICUResourceBundle.getAvailableULocales();
        }
        return shim.getAvailableULocales();
    }

    public static final String[] getKeywordValuesForLocale(String key, ULocale locale, boolean commonlyUsed) {
        if (!"currency".equals(key)) {
            return EMPTY_STRING_ARRAY;
        }
        if (!commonlyUsed) {
            return (String[]) getAllTenderCurrencies().toArray(new String[0]);
        }
        if (UND.equals(locale)) {
            return EMPTY_STRING_ARRAY;
        }
        List<String> result = getTenderCurrencies(CurrencyMetaInfo.CurrencyFilter.now().withRegion(ULocale.getRegionForSupplementalData(locale, true)));
        if (result.size() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public String getCurrencyCode() {
        return this.subType;
    }

    public int getNumericCode() {
        try {
            return UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "currencyNumericCodes", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("codeMap").get(this.subType).getInt();
        } catch (MissingResourceException e) {
            return 0;
        }
    }

    public String getSymbol() {
        return getSymbol(ULocale.getDefault(ULocale.Category.DISPLAY));
    }

    public String getSymbol(Locale loc) {
        return getSymbol(ULocale.forLocale(loc));
    }

    public String getSymbol(ULocale uloc) {
        return getName(uloc, 0, new boolean[1]);
    }

    public String getName(Locale locale, int nameStyle, boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, isChoiceFormat);
    }

    public String getName(ULocale locale, int nameStyle, boolean[] isChoiceFormat) {
        if (isChoiceFormat != null) {
            isChoiceFormat[0] = false;
        }
        CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
        if (nameStyle != 3) {
            switch (nameStyle) {
                case 0:
                    return names.getSymbol(this.subType);
                case 1:
                    return names.getName(this.subType);
                default:
                    throw new IllegalArgumentException("bad name style: " + nameStyle);
            }
        } else if (names instanceof CurrencyData.CurrencyDisplayInfo) {
            return ((CurrencyData.CurrencyDisplayInfo) names).getNarrowSymbol(this.subType);
        } else {
            throw new UnsupportedOperationException("Cannot get narrow symbol from custom currency display name provider");
        }
    }

    public String getName(Locale locale, int nameStyle, String pluralCount, boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, pluralCount, isChoiceFormat);
    }

    public String getName(ULocale locale, int nameStyle, String pluralCount, boolean[] isChoiceFormat) {
        if (nameStyle != 2) {
            return getName(locale, nameStyle, isChoiceFormat);
        }
        if (isChoiceFormat != null) {
            isChoiceFormat[0] = false;
        }
        return CurrencyDisplayNames.getInstance(locale).getPluralName(this.subType, pluralCount);
    }

    public String getDisplayName() {
        return getName(Locale.getDefault(), 1, (boolean[]) null);
    }

    public String getDisplayName(Locale locale) {
        return getName(locale, 1, (boolean[]) null);
    }

    @Deprecated
    public static String parse(ULocale locale, String text, int type, ParsePosition pos) {
        List<TextTrieMap<CurrencyStringInfo>> currencyTrieVec = getCurrencyTrieVec(locale);
        CurrencyNameResultHandler handler = new CurrencyNameResultHandler();
        currencyTrieVec.get(1).find((CharSequence) text, pos.getIndex(), handler);
        String isoResult = handler.getBestCurrencyISOCode();
        int maxLength = handler.getBestMatchLength();
        if (type != 1) {
            CurrencyNameResultHandler handler2 = new CurrencyNameResultHandler();
            currencyTrieVec.get(0).find((CharSequence) text, pos.getIndex(), handler2);
            if (handler2.getBestMatchLength() > maxLength) {
                isoResult = handler2.getBestCurrencyISOCode();
                maxLength = handler2.getBestMatchLength();
            }
        }
        pos.setIndex(pos.getIndex() + maxLength);
        return isoResult;
    }

    @Deprecated
    public static TextTrieMap<CurrencyStringInfo>.ParseState openParseState(ULocale locale, int startingCp, int type) {
        List<TextTrieMap<CurrencyStringInfo>> currencyTrieVec = getCurrencyTrieVec(locale);
        if (type == 1) {
            return currencyTrieVec.get(0).openParseState(startingCp);
        }
        return currencyTrieVec.get(1).openParseState(startingCp);
    }

    private static List<TextTrieMap<CurrencyStringInfo>> getCurrencyTrieVec(ULocale locale) {
        List<TextTrieMap<CurrencyStringInfo>> currencyTrieVec = CURRENCY_NAME_CACHE.get(locale);
        if (currencyTrieVec != null) {
            return currencyTrieVec;
        }
        TextTrieMap<CurrencyStringInfo> currencyNameTrie = new TextTrieMap<>(true);
        TextTrieMap<CurrencyStringInfo> currencySymbolTrie = new TextTrieMap<>(false);
        List<TextTrieMap<CurrencyStringInfo>> currencyTrieVec2 = new ArrayList<>();
        currencyTrieVec2.add(currencySymbolTrie);
        currencyTrieVec2.add(currencyNameTrie);
        setupCurrencyTrieVec(locale, currencyTrieVec2);
        CURRENCY_NAME_CACHE.put(locale, currencyTrieVec2);
        return currencyTrieVec2;
    }

    private static void setupCurrencyTrieVec(ULocale locale, List<TextTrieMap<CurrencyStringInfo>> trieVec) {
        TextTrieMap<CurrencyStringInfo> symTrie = trieVec.get(0);
        TextTrieMap<CurrencyStringInfo> trie = trieVec.get(1);
        CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
        for (Map.Entry<String, String> e : names.symbolMap().entrySet()) {
            String symbol = e.getKey();
            String isoCode2 = e.getValue();
            for (String equivalentSymbol : EQUIVALENT_CURRENCY_SYMBOLS.get(symbol)) {
                symTrie.put(equivalentSymbol, new CurrencyStringInfo(isoCode2, symbol));
            }
        }
        for (Map.Entry<String, String> e2 : names.nameMap().entrySet()) {
            String name = e2.getKey();
            trie.put(name, new CurrencyStringInfo(e2.getValue(), name));
        }
    }

    public int getDefaultFractionDigits() {
        return getDefaultFractionDigits(CurrencyUsage.STANDARD);
    }

    public int getDefaultFractionDigits(CurrencyUsage Usage) {
        return CurrencyMetaInfo.getInstance().currencyDigits(this.subType, Usage).fractionDigits;
    }

    public double getRoundingIncrement() {
        return getRoundingIncrement(CurrencyUsage.STANDARD);
    }

    public double getRoundingIncrement(CurrencyUsage Usage) {
        CurrencyMetaInfo.CurrencyDigits digits = CurrencyMetaInfo.getInstance().currencyDigits(this.subType, Usage);
        int data1 = digits.roundingIncrement;
        if (data1 == 0) {
            return 0.0d;
        }
        int data0 = digits.fractionDigits;
        if (data0 < 0 || data0 >= POW10.length) {
            return 0.0d;
        }
        return ((double) data1) / ((double) POW10[data0]);
    }

    public String toString() {
        return this.subType;
    }

    protected Currency(String theISOCode) {
        super("currency", theISOCode);
        this.isoCode = theISOCode;
    }

    private static synchronized List<String> getAllTenderCurrencies() {
        List<String> all;
        synchronized (Currency.class) {
            all = ALL_TENDER_CODES == null ? null : ALL_TENDER_CODES.get();
            if (all == null) {
                all = Collections.unmodifiableList(getTenderCurrencies(CurrencyMetaInfo.CurrencyFilter.all()));
                ALL_TENDER_CODES = new SoftReference<>(all);
            }
        }
        return all;
    }

    private static synchronized Set<String> getAllCurrenciesAsSet() {
        Set<String> all;
        synchronized (Currency.class) {
            all = ALL_CODES_AS_SET == null ? null : ALL_CODES_AS_SET.get();
            if (all == null) {
                all = Collections.unmodifiableSet(new HashSet(CurrencyMetaInfo.getInstance().currencies(CurrencyMetaInfo.CurrencyFilter.all())));
                ALL_CODES_AS_SET = new SoftReference<>(all);
            }
        }
        return all;
    }

    public static boolean isAvailable(String code, Date from, Date to) {
        if (!isAlpha3Code(code)) {
            return false;
        }
        if (from == null || to == null || !from.after(to)) {
            String code2 = code.toUpperCase(Locale.ENGLISH);
            if (!getAllCurrenciesAsSet().contains(code2)) {
                return false;
            }
            if (from == null && to == null) {
                return true;
            }
            return CurrencyMetaInfo.getInstance().currencies(CurrencyMetaInfo.CurrencyFilter.onDateRange(from, to).withCurrency(code2)).contains(code2);
        }
        throw new IllegalArgumentException("To is before from");
    }

    private static List<String> getTenderCurrencies(CurrencyMetaInfo.CurrencyFilter filter) {
        return CurrencyMetaInfo.getInstance().currencies(filter.withTender());
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnit.MeasureUnitProxy(this.type, this.subType);
    }

    private Object readResolve() throws ObjectStreamException {
        return getInstance(this.isoCode);
    }
}

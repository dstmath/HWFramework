package android.icu.util;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.TextTrieMap;
import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.text.CurrencyDisplayNames;
import android.icu.text.CurrencyMetaInfo;
import android.icu.text.CurrencyMetaInfo.CurrencyDigits;
import android.icu.text.CurrencyMetaInfo.CurrencyFilter;
import android.icu.util.ULocale.Category;
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
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

public class Currency extends MeasureUnit {
    private static SoftReference<Set<String>> ALL_CODES_AS_SET = null;
    private static SoftReference<List<String>> ALL_TENDER_CODES = null;
    private static ICUCache<ULocale, List<TextTrieMap<CurrencyStringInfo>>> CURRENCY_NAME_CACHE = null;
    private static final boolean DEBUG = false;
    private static final String[] EMPTY_STRING_ARRAY = null;
    private static final EquivalenceRelation<String> EQUIVALENT_CURRENCY_SYMBOLS = null;
    private static final String EUR_STR = "EUR";
    public static final int LONG_NAME = 1;
    public static final int PLURAL_LONG_NAME = 2;
    private static final int[] POW10 = null;
    public static final int SYMBOL_NAME = 0;
    private static final ULocale UND = null;
    private static final ICUCache<ULocale, String> currencyCodeCache = null;
    private static final long serialVersionUID = -5839973855554750484L;
    private static ServiceShim shim;
    private final String isoCode;

    private static class CurrencyNameResultHandler implements ResultHandler<CurrencyStringInfo> {
        private String bestCurrencyISOCode;
        private int bestMatchLength;

        private CurrencyNameResultHandler() {
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<CurrencyStringInfo> values) {
            if (values.hasNext()) {
                this.bestCurrencyISOCode = ((CurrencyStringInfo) values.next()).getISOCode();
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

    private static final class CurrencyStringInfo {
        private String currencyString;
        private String isoCode;

        public CurrencyStringInfo(String isoCode, String currencyString) {
            this.isoCode = isoCode;
            this.currencyString = currencyString;
        }

        public String getISOCode() {
            return this.isoCode;
        }

        public String getCurrencyString() {
            return this.currencyString;
        }
    }

    public enum CurrencyUsage {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Currency.CurrencyUsage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Currency.CurrencyUsage.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Currency.CurrencyUsage.<clinit>():void");
        }
    }

    private static final class EquivalenceRelation<T> {
        private Map<T, Set<T>> data;

        /* synthetic */ EquivalenceRelation(EquivalenceRelation equivalenceRelation) {
            this();
        }

        private EquivalenceRelation() {
            this.data = new HashMap();
        }

        public EquivalenceRelation<T> add(T... items) {
            int i;
            int i2 = Currency.SYMBOL_NAME;
            Set<T> group = new HashSet();
            int length = items.length;
            for (i = Currency.SYMBOL_NAME; i < length; i += Currency.LONG_NAME) {
                T item = items[i];
                if (this.data.containsKey(item)) {
                    throw new IllegalArgumentException("All groups passed to add must be disjoint.");
                }
                group.add(item);
            }
            i = items.length;
            while (i2 < i) {
                this.data.put(items[i2], group);
                i2 += Currency.LONG_NAME;
            }
            return this;
        }

        public Set<T> get(T item) {
            Set<T> result = (Set) this.data.get(item);
            if (result == null) {
                return Collections.singleton(item);
            }
            return Collections.unmodifiableSet(result);
        }
    }

    static abstract class ServiceShim {
        abstract Currency createInstance(ULocale uLocale);

        abstract Locale[] getAvailableLocales();

        abstract ULocale[] getAvailableULocales();

        abstract Object registerInstance(Currency currency, ULocale uLocale);

        abstract boolean unregister(Object obj);

        ServiceShim() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Currency.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Currency.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Currency.<clinit>():void");
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
        List<String> list = getTenderCurrencies(CurrencyFilter.onDate(d).withRegion(loc.getCountry()));
        if (list.isEmpty()) {
            return null;
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static String[] getAvailableCurrencyCodes(Locale loc, Date d) {
        return getAvailableCurrencyCodes(ULocale.forLocale(loc), d);
    }

    public static Set<Currency> getAvailableCurrencies() {
        List<String> list = CurrencyMetaInfo.getInstance().currencies(CurrencyFilter.all());
        HashSet<Currency> resultSet = new HashSet(list.size());
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
        String code = (String) currencyCodeCache.get(loc);
        if (code == null) {
            List<String> list = CurrencyMetaInfo.getInstance().currencies(CurrencyFilter.onRegion(loc.getCountry()));
            if (list.size() <= 0) {
                return null;
            }
            code = (String) list.get(SYMBOL_NAME);
            if ("PREEURO".equals(variant) && EUR_STR.equals(code)) {
                if (list.size() < PLURAL_LONG_NAME) {
                    return null;
                }
                code = (String) list.get(LONG_NAME);
            }
            currencyCodeCache.put(loc, code);
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
            return DEBUG;
        }
        for (int i = SYMBOL_NAME; i < 3; i += LONG_NAME) {
            char ch = code.charAt(i);
            if (ch < 'A' || ((ch > 'Z' && ch < 'a') || ch > 'z')) {
                return DEBUG;
            }
        }
        return true;
    }

    public static Object registerInstance(Currency currency, ULocale locale) {
        return getShim().registerInstance(currency, locale);
    }

    public static boolean unregister(Object registryKey) {
        if (registryKey == null) {
            throw new IllegalArgumentException("registryKey must not be null");
        } else if (shim == null) {
            return DEBUG;
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
            return (String[]) getAllTenderCurrencies().toArray(new String[SYMBOL_NAME]);
        }
        String prefRegion = locale.getCountry();
        if (prefRegion.length() == 0) {
            if (UND.equals(locale)) {
                return EMPTY_STRING_ARRAY;
            }
            prefRegion = ULocale.addLikelySubtags(locale).getCountry();
        }
        List<String> result = getTenderCurrencies(CurrencyFilter.now().withRegion(prefRegion));
        if (result.size() == 0) {
            return EMPTY_STRING_ARRAY;
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public String getCurrencyCode() {
        return this.subType;
    }

    public int getNumericCode() {
        int result = SYMBOL_NAME;
        try {
            result = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "currencyNumericCodes", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("codeMap").get(this.subType).getInt();
        } catch (MissingResourceException e) {
        }
        return result;
    }

    public String getSymbol() {
        return getSymbol(ULocale.getDefault(Category.DISPLAY));
    }

    public String getSymbol(Locale loc) {
        return getSymbol(ULocale.forLocale(loc));
    }

    public String getSymbol(ULocale uloc) {
        return getName(uloc, (int) SYMBOL_NAME, new boolean[LONG_NAME]);
    }

    public String getName(Locale locale, int nameStyle, boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, isChoiceFormat);
    }

    public String getName(ULocale locale, int nameStyle, boolean[] isChoiceFormat) {
        if (nameStyle == 0 || nameStyle == LONG_NAME) {
            if (isChoiceFormat != null) {
                isChoiceFormat[SYMBOL_NAME] = DEBUG;
            }
            CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
            return nameStyle == 0 ? names.getSymbol(this.subType) : names.getName(this.subType);
        } else {
            throw new IllegalArgumentException("bad name style: " + nameStyle);
        }
    }

    public String getName(Locale locale, int nameStyle, String pluralCount, boolean[] isChoiceFormat) {
        return getName(ULocale.forLocale(locale), nameStyle, pluralCount, isChoiceFormat);
    }

    public String getName(ULocale locale, int nameStyle, String pluralCount, boolean[] isChoiceFormat) {
        if (nameStyle != PLURAL_LONG_NAME) {
            return getName(locale, nameStyle, isChoiceFormat);
        }
        if (isChoiceFormat != null) {
            isChoiceFormat[SYMBOL_NAME] = DEBUG;
        }
        return CurrencyDisplayNames.getInstance(locale).getPluralName(this.subType, pluralCount);
    }

    public String getDisplayName() {
        return getName(Locale.getDefault(), (int) LONG_NAME, null);
    }

    public String getDisplayName(Locale locale) {
        return getName(locale, (int) LONG_NAME, null);
    }

    @Deprecated
    public static String parse(ULocale locale, String text, int type, ParsePosition pos) {
        TextTrieMap<CurrencyStringInfo> currencyNameTrie;
        List<TextTrieMap<CurrencyStringInfo>> currencyTrieVec = (List) CURRENCY_NAME_CACHE.get(locale);
        if (currencyTrieVec == null) {
            currencyNameTrie = new TextTrieMap(true);
            TextTrieMap<CurrencyStringInfo> currencySymbolTrie = new TextTrieMap(DEBUG);
            currencyTrieVec = new ArrayList();
            currencyTrieVec.add(currencySymbolTrie);
            currencyTrieVec.add(currencyNameTrie);
            setupCurrencyTrieVec(locale, currencyTrieVec);
            CURRENCY_NAME_CACHE.put(locale, currencyTrieVec);
        }
        currencyNameTrie = (TextTrieMap) currencyTrieVec.get(LONG_NAME);
        ResultHandler handler = new CurrencyNameResultHandler();
        currencyNameTrie.find((CharSequence) text, pos.getIndex(), handler);
        String isoResult = handler.getBestCurrencyISOCode();
        int maxLength = handler.getBestMatchLength();
        if (type != LONG_NAME) {
            currencySymbolTrie = (TextTrieMap) currencyTrieVec.get(SYMBOL_NAME);
            handler = new CurrencyNameResultHandler();
            currencySymbolTrie.find((CharSequence) text, pos.getIndex(), handler);
            if (handler.getBestMatchLength() > maxLength) {
                isoResult = handler.getBestCurrencyISOCode();
                maxLength = handler.getBestMatchLength();
            }
        }
        pos.setIndex(pos.getIndex() + maxLength);
        return isoResult;
    }

    private static void setupCurrencyTrieVec(ULocale locale, List<TextTrieMap<CurrencyStringInfo>> trieVec) {
        TextTrieMap<CurrencyStringInfo> symTrie = (TextTrieMap) trieVec.get(SYMBOL_NAME);
        TextTrieMap<CurrencyStringInfo> trie = (TextTrieMap) trieVec.get(LONG_NAME);
        CurrencyDisplayNames names = CurrencyDisplayNames.getInstance(locale);
        for (Entry<String, String> e : names.symbolMap().entrySet()) {
            String symbol = (String) e.getKey();
            String isoCode = (String) e.getValue();
            for (String equivalentSymbol : EQUIVALENT_CURRENCY_SYMBOLS.get(symbol)) {
                symTrie.put(equivalentSymbol, new CurrencyStringInfo(isoCode, symbol));
            }
        }
        for (Entry<String, String> e2 : names.nameMap().entrySet()) {
            String name = (String) e2.getKey();
            trie.put(name, new CurrencyStringInfo((String) e2.getValue(), name));
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
        CurrencyDigits digits = CurrencyMetaInfo.getInstance().currencyDigits(this.subType, Usage);
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
            all = ALL_TENDER_CODES == null ? null : (List) ALL_TENDER_CODES.get();
            if (all == null) {
                all = Collections.unmodifiableList(getTenderCurrencies(CurrencyFilter.all()));
                ALL_TENDER_CODES = new SoftReference(all);
            }
        }
        return all;
    }

    private static synchronized Set<String> getAllCurrenciesAsSet() {
        Set<String> all;
        synchronized (Currency.class) {
            all = ALL_CODES_AS_SET == null ? null : (Set) ALL_CODES_AS_SET.get();
            if (all == null) {
                all = Collections.unmodifiableSet(new HashSet(CurrencyMetaInfo.getInstance().currencies(CurrencyFilter.all())));
                ALL_CODES_AS_SET = new SoftReference(all);
            }
        }
        return all;
    }

    public static boolean isAvailable(String code, Date from, Date to) {
        if (!isAlpha3Code(code)) {
            return DEBUG;
        }
        if (from == null || to == null || !from.after(to)) {
            code = code.toUpperCase(Locale.ENGLISH);
            if (!getAllCurrenciesAsSet().contains(code)) {
                return DEBUG;
            }
            if (from == null && to == null) {
                return true;
            }
            return CurrencyMetaInfo.getInstance().currencies(CurrencyFilter.onDateRange(from, to).withCurrency(code)).contains(code);
        }
        throw new IllegalArgumentException("To is before from");
    }

    private static List<String> getTenderCurrencies(CurrencyFilter filter) {
        return CurrencyMetaInfo.getInstance().currencies(filter.withTender());
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnitProxy(this.type, this.subType);
    }

    private Object readResolve() throws ObjectStreamException {
        return getInstance(this.isoCode);
    }
}

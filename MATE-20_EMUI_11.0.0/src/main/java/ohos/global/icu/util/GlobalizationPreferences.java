package ohos.global.icu.util;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.text.BreakIterator;
import ohos.global.icu.text.Collator;
import ohos.global.icu.text.DateFormat;
import ohos.global.icu.text.NumberFormat;
import ohos.global.icu.text.SimpleDateFormat;
import ohos.security.keystore.KeyStoreConstants;

public class GlobalizationPreferences implements Freezable<GlobalizationPreferences> {
    public static final int BI_CHARACTER = 0;
    private static final int BI_LIMIT = 5;
    public static final int BI_LINE = 2;
    public static final int BI_SENTENCE = 3;
    public static final int BI_TITLE = 4;
    public static final int BI_WORD = 1;
    public static final int DF_FULL = 0;
    private static final int DF_LIMIT = 5;
    public static final int DF_LONG = 1;
    public static final int DF_MEDIUM = 2;
    public static final int DF_NONE = 4;
    public static final int DF_SHORT = 3;
    public static final int ID_CURRENCY = 7;
    public static final int ID_CURRENCY_SYMBOL = 8;
    public static final int ID_KEYWORD = 5;
    public static final int ID_KEYWORD_VALUE = 6;
    public static final int ID_LANGUAGE = 1;
    public static final int ID_LOCALE = 0;
    public static final int ID_SCRIPT = 2;
    public static final int ID_TERRITORY = 3;
    public static final int ID_TIMEZONE = 9;
    public static final int ID_VARIANT = 4;
    public static final int NF_CURRENCY = 1;
    public static final int NF_INTEGER = 4;
    private static final int NF_LIMIT = 5;
    public static final int NF_NUMBER = 0;
    public static final int NF_PERCENT = 2;
    public static final int NF_SCIENTIFIC = 3;
    private static final int TYPE_BREAKITERATOR = 5;
    private static final int TYPE_CALENDAR = 1;
    private static final int TYPE_COLLATOR = 4;
    private static final int TYPE_DATEFORMAT = 2;
    private static final int TYPE_GENERIC = 0;
    private static final int TYPE_LIMIT = 6;
    private static final int TYPE_NUMBERFORMAT = 3;
    private static final HashMap<ULocale, BitSet> available_locales = new HashMap<>();
    private static final String[][] language_territory_hack = {new String[]{"af", "ZA"}, new String[]{"am", "ET"}, new String[]{"ar", "SA"}, new String[]{"as", "IN"}, new String[]{"ay", "PE"}, new String[]{"az", "AZ"}, new String[]{"bal", "PK"}, new String[]{"be", "BY"}, new String[]{"bg", "BG"}, new String[]{"bn", "IN"}, new String[]{"bs", "BA"}, new String[]{"ca", "ES"}, new String[]{"ch", "MP"}, new String[]{"cpe", "SL"}, new String[]{"cs", "CZ"}, new String[]{"cy", "GB"}, new String[]{"da", "DK"}, new String[]{"de", "DE"}, new String[]{"dv", "MV"}, new String[]{"dz", "BT"}, new String[]{"el", "GR"}, new String[]{"en", "US"}, new String[]{"es", "ES"}, new String[]{"et", "EE"}, new String[]{"eu", "ES"}, new String[]{"fa", "IR"}, new String[]{"fi", "FI"}, new String[]{"fil", "PH"}, new String[]{"fj", "FJ"}, new String[]{"fo", "FO"}, new String[]{"fr", "FR"}, new String[]{"ga", "IE"}, new String[]{"gd", "GB"}, new String[]{"gl", "ES"}, new String[]{"gn", "PY"}, new String[]{"gu", "IN"}, new String[]{"gv", "GB"}, new String[]{"ha", "NG"}, new String[]{"he", "IL"}, new String[]{"hi", "IN"}, new String[]{"ho", "PG"}, new String[]{"hr", "HR"}, new String[]{"ht", "HT"}, new String[]{"hu", "HU"}, new String[]{"hy", "AM"}, new String[]{"id", "ID"}, new String[]{"is", "IS"}, new String[]{"it", "IT"}, new String[]{"ja", "JP"}, new String[]{"ka", "GE"}, new String[]{"kk", "KZ"}, new String[]{"kl", "GL"}, new String[]{"km", "KH"}, new String[]{"kn", "IN"}, new String[]{"ko", "KR"}, new String[]{"kok", "IN"}, new String[]{"ks", "IN"}, new String[]{"ku", "TR"}, new String[]{"ky", "KG"}, new String[]{"la", "VA"}, new String[]{"lb", "LU"}, new String[]{"ln", "CG"}, new String[]{"lo", "LA"}, new String[]{"lt", "LT"}, new String[]{"lv", "LV"}, new String[]{"mai", "IN"}, new String[]{"men", "GN"}, new String[]{"mg", "MG"}, new String[]{"mh", "MH"}, new String[]{"mk", "MK"}, new String[]{"ml", "IN"}, new String[]{"mn", "MN"}, new String[]{"mni", "IN"}, new String[]{"mo", "MD"}, new String[]{"mr", "IN"}, new String[]{"ms", "MY"}, new String[]{"mt", "MT"}, new String[]{"my", "MM"}, new String[]{"na", "NR"}, new String[]{"nb", "NO"}, new String[]{"nd", "ZA"}, new String[]{"ne", "NP"}, new String[]{"niu", "NU"}, new String[]{"nl", "NL"}, new String[]{"nn", "NO"}, new String[]{"no", "NO"}, new String[]{"nr", "ZA"}, new String[]{"nso", "ZA"}, new String[]{"ny", "MW"}, new String[]{"om", "KE"}, new String[]{"or", "IN"}, new String[]{"pa", "IN"}, new String[]{"pau", "PW"}, new String[]{"pl", "PL"}, new String[]{"ps", "PK"}, new String[]{"pt", "BR"}, new String[]{"qu", "PE"}, new String[]{"rn", "BI"}, new String[]{"ro", "RO"}, new String[]{"ru", "RU"}, new String[]{"rw", "RW"}, new String[]{"sd", "IN"}, new String[]{"sg", "CF"}, new String[]{"si", "LK"}, new String[]{"sk", "SK"}, new String[]{"sl", "SI"}, new String[]{"sm", "WS"}, new String[]{"so", "DJ"}, new String[]{"sq", "CS"}, new String[]{"sr", "CS"}, new String[]{"ss", "ZA"}, new String[]{"st", "ZA"}, new String[]{"sv", "SE"}, new String[]{"sw", "KE"}, new String[]{"ta", "IN"}, new String[]{"te", "IN"}, new String[]{"tem", "SL"}, new String[]{"tet", "TL"}, new String[]{"th", "TH"}, new String[]{"ti", "ET"}, new String[]{"tg", "TJ"}, new String[]{"tk", "TM"}, new String[]{"tkl", "TK"}, new String[]{"tvl", "TV"}, new String[]{"tl", "PH"}, new String[]{"tn", "ZA"}, new String[]{"to", "TO"}, new String[]{"tpi", "PG"}, new String[]{"tr", "TR"}, new String[]{"ts", "ZA"}, new String[]{"uk", "UA"}, new String[]{"ur", "IN"}, new String[]{"uz", "UZ"}, new String[]{"ve", "ZA"}, new String[]{"vi", "VN"}, new String[]{"wo", "SN"}, new String[]{"xh", "ZA"}, new String[]{"zh", "CN"}, new String[]{"zh_Hant", "TW"}, new String[]{"zu", "ZA"}, new String[]{"aa", "ET"}, new String[]{"byn", "ER"}, new String[]{"eo", "DE"}, new String[]{"gez", "ET"}, new String[]{"haw", "US"}, new String[]{"iu", "CA"}, new String[]{"kw", "GB"}, new String[]{"sa", "IN"}, new String[]{"sh", "HR"}, new String[]{"sid", "ET"}, new String[]{"syr", "SY"}, new String[]{"tig", "ER"}, new String[]{"tt", "RU"}, new String[]{"wal", "ET"}};
    private static final Map<String, String> language_territory_hack_map = new HashMap();
    static final String[][] territory_tzid_hack = {new String[]{"AQ", "Antarctica/McMurdo"}, new String[]{"AR", "America/Buenos_Aires"}, new String[]{"AU", "Australia/Sydney"}, new String[]{"BR", "America/Sao_Paulo"}, new String[]{"CA", "America/Toronto"}, new String[]{"CD", "Africa/Kinshasa"}, new String[]{"CL", "America/Santiago"}, new String[]{"CN", "Asia/Shanghai"}, new String[]{KeyStoreConstants.SEC_KEY_ALGORITHM_EC, "America/Guayaquil"}, new String[]{"ES", "Europe/Madrid"}, new String[]{"GB", "Europe/London"}, new String[]{"GL", "America/Godthab"}, new String[]{"ID", "Asia/Jakarta"}, new String[]{"ML", "Africa/Bamako"}, new String[]{"MX", "America/Mexico_City"}, new String[]{"MY", "Asia/Kuala_Lumpur"}, new String[]{"NZ", "Pacific/Auckland"}, new String[]{"PT", "Europe/Lisbon"}, new String[]{"RU", "Europe/Moscow"}, new String[]{"UA", "Europe/Kiev"}, new String[]{"US", "America/New_York"}, new String[]{"UZ", "Asia/Tashkent"}, new String[]{"PF", "Pacific/Tahiti"}, new String[]{"FM", "Pacific/Kosrae"}, new String[]{"KI", "Pacific/Tarawa"}, new String[]{"KZ", "Asia/Almaty"}, new String[]{"MH", "Pacific/Majuro"}, new String[]{"MN", "Asia/Ulaanbaatar"}, new String[]{"SJ", "Arctic/Longyearbyen"}, new String[]{"UM", "Pacific/Midway"}};
    static final Map<String, String> territory_tzid_hack_map = new HashMap();
    private BreakIterator[] breakIterators;
    private Calendar calendar;
    private Collator collator;
    private Currency currency;
    private DateFormat[][] dateFormats;
    private volatile boolean frozen;
    private List<ULocale> implicitLocales;
    private List<ULocale> locales;
    private NumberFormat[] numberFormats;
    private String territory;
    private TimeZone timezone;

    public GlobalizationPreferences() {
        reset();
    }

    public GlobalizationPreferences setLocales(List<ULocale> list) {
        if (!isFrozen()) {
            this.locales = processLocales(list);
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public List<ULocale> getLocales() {
        if (this.locales == null) {
            return guessLocales();
        }
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.locales);
        return arrayList;
    }

    public ULocale getLocale(int i) {
        List<ULocale> list = this.locales;
        if (list == null) {
            list = guessLocales();
        }
        if (i < 0 || i >= list.size()) {
            return null;
        }
        return list.get(i);
    }

    public GlobalizationPreferences setLocales(ULocale[] uLocaleArr) {
        if (!isFrozen()) {
            return setLocales(Arrays.asList(uLocaleArr));
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public GlobalizationPreferences setLocale(ULocale uLocale) {
        if (!isFrozen()) {
            return setLocales(new ULocale[]{uLocale});
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public GlobalizationPreferences setLocales(String str) {
        if (!isFrozen()) {
            try {
                return setLocales(ULocale.parseAcceptLanguage(str, true));
            } catch (ParseException unused) {
                throw new IllegalArgumentException("Invalid Accept-Language string");
            }
        } else {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
    }

    public ResourceBundle getResourceBundle(String str) {
        return getResourceBundle(str, null);
    }

    public ResourceBundle getResourceBundle(String str, ClassLoader classLoader) {
        List<ULocale> locales2 = getLocales();
        int i = 0;
        String str2 = null;
        UResourceBundle uResourceBundle = null;
        UResourceBundle uResourceBundle2 = null;
        while (true) {
            if (i >= locales2.size()) {
                uResourceBundle = uResourceBundle2;
                break;
            }
            String uLocale = locales2.get(i).toString();
            if (str2 != null && uLocale.equals(str2)) {
                break;
            }
            if (classLoader == null) {
                try {
                    uResourceBundle = UResourceBundle.getBundleInstance(str, uLocale);
                } catch (MissingResourceException unused) {
                    str2 = null;
                }
            } else {
                uResourceBundle = UResourceBundle.getBundleInstance(str, uLocale, classLoader);
            }
            if (uResourceBundle != null) {
                str2 = uResourceBundle.getULocale().getName();
                if (str2.equals(uLocale)) {
                    break;
                } else if (uResourceBundle2 == null) {
                    uResourceBundle2 = uResourceBundle;
                }
            } else {
                continue;
            }
            i++;
        }
        if (uResourceBundle != null) {
            return uResourceBundle;
        }
        throw new MissingResourceException("Can't find bundle for base name " + str, str, "");
    }

    public GlobalizationPreferences setTerritory(String str) {
        if (!isFrozen()) {
            this.territory = str;
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public String getTerritory() {
        String str = this.territory;
        return str == null ? guessTerritory() : str;
    }

    public GlobalizationPreferences setCurrency(Currency currency2) {
        if (!isFrozen()) {
            this.currency = currency2;
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public Currency getCurrency() {
        Currency currency2 = this.currency;
        return currency2 == null ? guessCurrency() : currency2;
    }

    public GlobalizationPreferences setCalendar(Calendar calendar2) {
        if (!isFrozen()) {
            this.calendar = (Calendar) calendar2.clone();
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public Calendar getCalendar() {
        Calendar calendar2 = this.calendar;
        if (calendar2 == null) {
            return guessCalendar();
        }
        Calendar calendar3 = (Calendar) calendar2.clone();
        calendar3.setTimeZone(getTimeZone());
        calendar3.setTimeInMillis(System.currentTimeMillis());
        return calendar3;
    }

    public GlobalizationPreferences setTimeZone(TimeZone timeZone) {
        if (!isFrozen()) {
            this.timezone = (TimeZone) timeZone.clone();
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public TimeZone getTimeZone() {
        TimeZone timeZone = this.timezone;
        if (timeZone == null) {
            return guessTimeZone();
        }
        return timeZone.cloneAsThawed();
    }

    public Collator getCollator() {
        Collator collator2 = this.collator;
        if (collator2 == null) {
            return guessCollator();
        }
        try {
            return (Collator) collator2.clone();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Error in cloning collator", e);
        }
    }

    public GlobalizationPreferences setCollator(Collator collator2) {
        if (!isFrozen()) {
            try {
                this.collator = (Collator) collator2.clone();
                return this;
            } catch (CloneNotSupportedException e) {
                throw new ICUCloneNotSupportedException("Error in cloning collator", e);
            }
        } else {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
    }

    public BreakIterator getBreakIterator(int i) {
        if (i < 0 || i >= 5) {
            throw new IllegalArgumentException("Illegal break iterator type");
        }
        BreakIterator[] breakIteratorArr = this.breakIterators;
        if (breakIteratorArr == null || breakIteratorArr[i] == null) {
            return guessBreakIterator(i);
        }
        return (BreakIterator) breakIteratorArr[i].clone();
    }

    public GlobalizationPreferences setBreakIterator(int i, BreakIterator breakIterator) {
        if (i < 0 || i >= 5) {
            throw new IllegalArgumentException("Illegal break iterator type");
        } else if (!isFrozen()) {
            if (this.breakIterators == null) {
                this.breakIterators = new BreakIterator[5];
            }
            this.breakIterators[i] = (BreakIterator) breakIterator.clone();
            return this;
        } else {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x011b A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0009 A[SYNTHETIC] */
    public String getDisplayName(String str, int i) {
        String str2 = str;
        for (ULocale uLocale : getLocales()) {
            int i2 = 0;
            if (isAvailableLocale(uLocale, 0)) {
                switch (i) {
                    case 0:
                        str2 = ULocale.getDisplayName(str, uLocale);
                        if (str.equals(str2)) {
                            break;
                        } else {
                            return str2;
                        }
                    case 1:
                        str2 = ULocale.getDisplayLanguage(str, uLocale);
                        if (str.equals(str2)) {
                        }
                        break;
                    case 2:
                        str2 = ULocale.getDisplayScript("und-" + str, uLocale);
                        if (str.equals(str2)) {
                        }
                        break;
                    case 3:
                        str2 = ULocale.getDisplayCountry("und-" + str, uLocale);
                        if (str.equals(str2)) {
                        }
                        break;
                    case 4:
                        str2 = ULocale.getDisplayVariant("und-QQ-" + str, uLocale);
                        if (str.equals(str2)) {
                        }
                        break;
                    case 5:
                        str2 = ULocale.getDisplayKeyword(str, uLocale);
                        if (str.equals(str2)) {
                        }
                        break;
                    case 6:
                        String[] strArr = new String[2];
                        Utility.split(str, '=', strArr);
                        String displayKeywordValue = ULocale.getDisplayKeywordValue("und@" + str, strArr[0], uLocale);
                        if (displayKeywordValue.equals(strArr[1])) {
                            str2 = displayKeywordValue;
                            break;
                        } else {
                            str2 = displayKeywordValue;
                            if (str.equals(str2)) {
                            }
                        }
                        break;
                    case 7:
                    case 8:
                        Currency currency2 = new Currency(str);
                        if (i == 7) {
                            i2 = 1;
                        }
                        str2 = currency2.getName(uLocale, i2, (boolean[]) null);
                        if (str.equals(str2)) {
                        }
                        break;
                    case 9:
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("vvvv", uLocale);
                        simpleDateFormat.setTimeZone(TimeZone.getFrozenTimeZone(str));
                        str2 = simpleDateFormat.format(new Date());
                        int indexOf = str2.indexOf(40);
                        int indexOf2 = str2.indexOf(41);
                        String substring = (indexOf == -1 || indexOf2 == -1 || indexOf2 - indexOf != 3) ? str2 : str2.substring(indexOf + 1, indexOf2);
                        if (substring.length() == 2) {
                            int i3 = 0;
                            while (true) {
                                if (i3 >= 2) {
                                    i2 = 1;
                                } else {
                                    char charAt = substring.charAt(i3);
                                    if (charAt >= 'A' && 'Z' >= charAt) {
                                        i3++;
                                    }
                                }
                            }
                        }
                        if (i2 != 0) {
                            continue;
                        }
                        if (str.equals(str2)) {
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown type: " + i);
                }
            }
        }
        return str2;
    }

    public GlobalizationPreferences setDateFormat(int i, int i2, DateFormat dateFormat) {
        if (!isFrozen()) {
            if (this.dateFormats == null) {
                this.dateFormats = (DateFormat[][]) Array.newInstance(DateFormat.class, 5, 5);
            }
            this.dateFormats[i][i2] = (DateFormat) dateFormat.clone();
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public DateFormat getDateFormat(int i, int i2) {
        if (!(i == 4 && i2 == 4) && i >= 0 && i < 5 && i2 >= 0 && i2 < 5) {
            DateFormat dateFormat = null;
            DateFormat[][] dateFormatArr = this.dateFormats;
            if (dateFormatArr != null) {
                dateFormat = dateFormatArr[i][i2];
            }
            if (dateFormat == null) {
                return guessDateFormat(i, i2);
            }
            DateFormat dateFormat2 = (DateFormat) dateFormat.clone();
            dateFormat2.setTimeZone(getTimeZone());
            return dateFormat2;
        }
        throw new IllegalArgumentException("Illegal date format style arguments");
    }

    public NumberFormat getNumberFormat(int i) {
        if (i < 0 || i >= 5) {
            throw new IllegalArgumentException("Illegal number format type");
        }
        NumberFormat numberFormat = null;
        NumberFormat[] numberFormatArr = this.numberFormats;
        if (numberFormatArr != null) {
            numberFormat = numberFormatArr[i];
        }
        if (numberFormat != null) {
            return (NumberFormat) numberFormat.clone();
        }
        return guessNumberFormat(i);
    }

    public GlobalizationPreferences setNumberFormat(int i, NumberFormat numberFormat) {
        if (!isFrozen()) {
            if (this.numberFormats == null) {
                this.numberFormats = new NumberFormat[5];
            }
            this.numberFormats[i] = (NumberFormat) numberFormat.clone();
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public GlobalizationPreferences reset() {
        if (!isFrozen()) {
            this.locales = null;
            this.territory = null;
            this.calendar = null;
            this.collator = null;
            this.breakIterators = null;
            this.timezone = null;
            this.currency = null;
            this.dateFormats = null;
            this.numberFormats = null;
            this.implicitLocales = null;
            return this;
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    /* access modifiers changed from: protected */
    public List<ULocale> processLocales(List<ULocale> list) {
        boolean z;
        ArrayList arrayList = new ArrayList();
        int i = 0;
        while (true) {
            boolean z2 = true;
            if (i >= list.size()) {
                break;
            }
            ULocale uLocale = list.get(i);
            String language = uLocale.getLanguage();
            String script = uLocale.getScript();
            String country = uLocale.getCountry();
            String variant = uLocale.getVariant();
            int i2 = 0;
            while (true) {
                if (i2 >= arrayList.size()) {
                    z2 = false;
                    break;
                }
                ULocale uLocale2 = (ULocale) arrayList.get(i2);
                if (uLocale2.getLanguage().equals(language)) {
                    String script2 = uLocale2.getScript();
                    String country2 = uLocale2.getCountry();
                    String variant2 = uLocale2.getVariant();
                    if (script2.equals(script)) {
                        if (country2.equals(country) || country2.length() != 0 || variant2.length() != 0) {
                            if (!variant2.equals(variant) && variant2.length() == 0) {
                                arrayList.add(i2, uLocale);
                                break;
                            }
                        } else {
                            arrayList.add(i2, uLocale);
                            break;
                        }
                    } else if (script2.length() != 0 || country2.length() != 0 || variant2.length() != 0) {
                        if (script2.length() != 0 || !country2.equals(country)) {
                            if (script.length() == 0 && country.length() > 0 && country2.length() == 0) {
                                arrayList.add(i2, uLocale);
                                break;
                            }
                        } else {
                            arrayList.add(i2, uLocale);
                            break;
                        }
                    } else {
                        arrayList.add(i2, uLocale);
                        break;
                    }
                }
                i2++;
            }
            if (!z2) {
                arrayList.add(uLocale);
            }
            i++;
        }
        int i3 = 0;
        while (i3 < arrayList.size()) {
            ULocale uLocale3 = (ULocale) arrayList.get(i3);
            while (true) {
                uLocale3 = uLocale3.getFallback();
                if (uLocale3 == null || uLocale3.getLanguage().length() == 0) {
                    break;
                }
                i3++;
                arrayList.add(i3, uLocale3);
            }
            i3++;
        }
        int i4 = 0;
        while (i4 < arrayList.size() - 1) {
            ULocale uLocale4 = (ULocale) arrayList.get(i4);
            int i5 = i4 + 1;
            int i6 = i5;
            while (true) {
                if (i6 >= arrayList.size()) {
                    z = false;
                    break;
                } else if (uLocale4.equals(arrayList.get(i6))) {
                    arrayList.remove(i4);
                    z = true;
                    break;
                } else {
                    i6++;
                }
            }
            if (!z) {
                i4 = i5;
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public DateFormat guessDateFormat(int i, int i2) {
        ULocale availableLocale = getAvailableLocale(2);
        if (availableLocale == null) {
            availableLocale = ULocale.ROOT;
        }
        if (i2 == 4) {
            return DateFormat.getDateInstance(getCalendar(), i, availableLocale);
        }
        if (i == 4) {
            return DateFormat.getTimeInstance(getCalendar(), i2, availableLocale);
        }
        return DateFormat.getDateTimeInstance(getCalendar(), i, i2, availableLocale);
    }

    /* access modifiers changed from: protected */
    public NumberFormat guessNumberFormat(int i) {
        ULocale availableLocale = getAvailableLocale(3);
        if (availableLocale == null) {
            availableLocale = ULocale.ROOT;
        }
        if (i == 0) {
            return NumberFormat.getInstance(availableLocale);
        }
        if (i == 1) {
            NumberFormat currencyInstance = NumberFormat.getCurrencyInstance(availableLocale);
            currencyInstance.setCurrency(getCurrency());
            return currencyInstance;
        } else if (i == 2) {
            return NumberFormat.getPercentInstance(availableLocale);
        } else {
            if (i == 3) {
                return NumberFormat.getScientificInstance(availableLocale);
            }
            if (i == 4) {
                return NumberFormat.getIntegerInstance(availableLocale);
            }
            throw new IllegalArgumentException("Unknown number format style");
        }
    }

    /* access modifiers changed from: protected */
    public String guessTerritory() {
        for (ULocale uLocale : getLocales()) {
            String country = uLocale.getCountry();
            if (country.length() != 0) {
                return country;
            }
        }
        ULocale locale = getLocale(0);
        String language = locale.getLanguage();
        String script = locale.getScript();
        String str = null;
        if (script.length() != 0) {
            Map<String, String> map = language_territory_hack_map;
            str = map.get(language + "_" + script);
        }
        if (str == null) {
            str = language_territory_hack_map.get(language);
        }
        return str == null ? "US" : str;
    }

    /* access modifiers changed from: protected */
    public Currency guessCurrency() {
        return Currency.getInstance(new ULocale("und-" + getTerritory()));
    }

    /* access modifiers changed from: protected */
    public List<ULocale> guessLocales() {
        if (this.implicitLocales == null) {
            ArrayList arrayList = new ArrayList(1);
            arrayList.add(ULocale.getDefault());
            this.implicitLocales = processLocales(arrayList);
        }
        return this.implicitLocales;
    }

    /* access modifiers changed from: protected */
    public Collator guessCollator() {
        ULocale availableLocale = getAvailableLocale(4);
        if (availableLocale == null) {
            availableLocale = ULocale.ROOT;
        }
        return Collator.getInstance(availableLocale);
    }

    /* access modifiers changed from: protected */
    public BreakIterator guessBreakIterator(int i) {
        ULocale availableLocale = getAvailableLocale(5);
        if (availableLocale == null) {
            availableLocale = ULocale.ROOT;
        }
        if (i == 0) {
            return BreakIterator.getCharacterInstance(availableLocale);
        }
        if (i == 1) {
            return BreakIterator.getWordInstance(availableLocale);
        }
        if (i == 2) {
            return BreakIterator.getLineInstance(availableLocale);
        }
        if (i == 3) {
            return BreakIterator.getSentenceInstance(availableLocale);
        }
        if (i == 4) {
            return BreakIterator.getTitleInstance(availableLocale);
        }
        throw new IllegalArgumentException("Unknown break iterator type");
    }

    /* access modifiers changed from: protected */
    public TimeZone guessTimeZone() {
        String str = territory_tzid_hack_map.get(getTerritory());
        if (str == null) {
            String[] availableIDs = TimeZone.getAvailableIDs(getTerritory());
            if (availableIDs.length == 0) {
                str = "Etc/GMT";
            } else {
                int i = 0;
                int i2 = 0;
                while (i2 < availableIDs.length && availableIDs[i2].indexOf("/") < 0) {
                    i2++;
                }
                if (i2 <= availableIDs.length) {
                    i = i2;
                }
                str = availableIDs[i];
            }
        }
        return TimeZone.getTimeZone(str);
    }

    /* access modifiers changed from: protected */
    public Calendar guessCalendar() {
        ULocale availableLocale = getAvailableLocale(1);
        if (availableLocale == null) {
            availableLocale = ULocale.US;
        }
        return Calendar.getInstance(getTimeZone(), availableLocale);
    }

    private ULocale getAvailableLocale(int i) {
        List<ULocale> locales2 = getLocales();
        for (int i2 = 0; i2 < locales2.size(); i2++) {
            ULocale uLocale = locales2.get(i2);
            if (isAvailableLocale(uLocale, i)) {
                return uLocale;
            }
        }
        return null;
    }

    private boolean isAvailableLocale(ULocale uLocale, int i) {
        BitSet bitSet = available_locales.get(uLocale);
        return bitSet != null && bitSet.get(i);
    }

    static {
        ULocale[] availableULocales;
        ULocale[] availableLocales = ULocale.getAvailableLocales();
        for (ULocale uLocale : availableLocales) {
            BitSet bitSet = new BitSet(6);
            available_locales.put(uLocale, bitSet);
            bitSet.set(0);
        }
        ULocale[] availableULocales2 = Calendar.getAvailableULocales();
        for (int i = 0; i < availableULocales2.length; i++) {
            BitSet bitSet2 = available_locales.get(availableULocales2[i]);
            if (bitSet2 == null) {
                bitSet2 = new BitSet(6);
                available_locales.put(availableLocales[i], bitSet2);
            }
            bitSet2.set(1);
        }
        ULocale[] availableULocales3 = DateFormat.getAvailableULocales();
        for (int i2 = 0; i2 < availableULocales3.length; i2++) {
            BitSet bitSet3 = available_locales.get(availableULocales3[i2]);
            if (bitSet3 == null) {
                bitSet3 = new BitSet(6);
                available_locales.put(availableLocales[i2], bitSet3);
            }
            bitSet3.set(2);
        }
        ULocale[] availableULocales4 = NumberFormat.getAvailableULocales();
        for (int i3 = 0; i3 < availableULocales4.length; i3++) {
            BitSet bitSet4 = available_locales.get(availableULocales4[i3]);
            if (bitSet4 == null) {
                bitSet4 = new BitSet(6);
                available_locales.put(availableLocales[i3], bitSet4);
            }
            bitSet4.set(3);
        }
        ULocale[] availableULocales5 = Collator.getAvailableULocales();
        for (int i4 = 0; i4 < availableULocales5.length; i4++) {
            BitSet bitSet5 = available_locales.get(availableULocales5[i4]);
            if (bitSet5 == null) {
                bitSet5 = new BitSet(6);
                available_locales.put(availableLocales[i4], bitSet5);
            }
            bitSet5.set(4);
        }
        for (ULocale uLocale2 : BreakIterator.getAvailableULocales()) {
            available_locales.get(uLocale2).set(5);
        }
        int i5 = 0;
        while (true) {
            String[][] strArr = language_territory_hack;
            if (i5 >= strArr.length) {
                break;
            }
            language_territory_hack_map.put(strArr[i5][0], strArr[i5][1]);
            i5++;
        }
        int i6 = 0;
        while (true) {
            String[][] strArr2 = territory_tzid_hack;
            if (i6 < strArr2.length) {
                territory_tzid_hack_map.put(strArr2[i6][0], strArr2[i6][1]);
                i6++;
            } else {
                return;
            }
        }
    }

    @Override // ohos.global.icu.util.Freezable
    public boolean isFrozen() {
        return this.frozen;
    }

    @Override // ohos.global.icu.util.Freezable
    public GlobalizationPreferences freeze() {
        this.frozen = true;
        return this;
    }

    @Override // ohos.global.icu.util.Freezable
    public GlobalizationPreferences cloneAsThawed() {
        try {
            GlobalizationPreferences globalizationPreferences = (GlobalizationPreferences) clone();
            globalizationPreferences.frozen = false;
            return globalizationPreferences;
        } catch (CloneNotSupportedException unused) {
            return null;
        }
    }
}

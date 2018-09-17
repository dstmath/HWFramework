package android.icu.util;

import android.icu.impl.Utility;
import android.icu.impl.locale.BaseLocale;
import android.icu.text.BreakIterator;
import android.icu.text.Collator;
import android.icu.text.DateFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
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
    private static final HashMap<ULocale, BitSet> available_locales = new HashMap();
    private static final String[][] language_territory_hack;
    private static final Map<String, String> language_territory_hack_map = new HashMap();
    static final String[][] territory_tzid_hack;
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

    public GlobalizationPreferences setLocales(List<ULocale> inputLocales) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.locales = processLocales(inputLocales);
        return this;
    }

    public List<ULocale> getLocales() {
        if (this.locales == null) {
            return guessLocales();
        }
        List<ULocale> result = new ArrayList();
        result.addAll(this.locales);
        return result;
    }

    public ULocale getLocale(int index) {
        List<ULocale> lcls = this.locales;
        if (lcls == null) {
            lcls = guessLocales();
        }
        if (index < 0 || index >= lcls.size()) {
            return null;
        }
        return (ULocale) lcls.get(index);
    }

    public GlobalizationPreferences setLocales(ULocale[] uLocales) {
        if (!isFrozen()) {
            return setLocales(Arrays.asList(uLocales));
        }
        throw new UnsupportedOperationException("Attempt to modify immutable object");
    }

    public GlobalizationPreferences setLocale(ULocale uLocale) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        return setLocales(new ULocale[]{uLocale});
    }

    public GlobalizationPreferences setLocales(String acceptLanguageString) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        try {
            return setLocales(ULocale.parseAcceptLanguage(acceptLanguageString, true));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid Accept-Language string");
        }
    }

    public ResourceBundle getResourceBundle(String baseName) {
        return getResourceBundle(baseName, null);
    }

    public ResourceBundle getResourceBundle(String baseName, ClassLoader loader) {
        ResourceBundle urb = null;
        UResourceBundle candidate = null;
        Object actualLocaleName = null;
        List<ULocale> fallbacks = getLocales();
        for (int i = 0; i < fallbacks.size(); i++) {
            String localeName = ((ULocale) fallbacks.get(i)).toString();
            if (actualLocaleName != null && localeName.equals(actualLocaleName)) {
                urb = candidate;
                break;
            }
            if (loader == null) {
                try {
                    candidate = UResourceBundle.getBundleInstance(baseName, localeName);
                } catch (MissingResourceException e) {
                    actualLocaleName = null;
                }
            } else {
                candidate = UResourceBundle.getBundleInstance(baseName, localeName, loader);
            }
            if (candidate != null) {
                actualLocaleName = candidate.getULocale().getName();
                if (actualLocaleName.equals(localeName)) {
                    urb = candidate;
                    break;
                } else if (urb == null) {
                    urb = candidate;
                }
            } else {
                continue;
            }
        }
        if (urb != null) {
            return urb;
        }
        throw new MissingResourceException("Can't find bundle for base name " + baseName, baseName, "");
    }

    public GlobalizationPreferences setTerritory(String territory) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.territory = territory;
        return this;
    }

    public String getTerritory() {
        if (this.territory == null) {
            return guessTerritory();
        }
        return this.territory;
    }

    public GlobalizationPreferences setCurrency(Currency currency) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.currency = currency;
        return this;
    }

    public Currency getCurrency() {
        if (this.currency == null) {
            return guessCurrency();
        }
        return this.currency;
    }

    public GlobalizationPreferences setCalendar(Calendar calendar) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.calendar = (Calendar) calendar.clone();
        return this;
    }

    public Calendar getCalendar() {
        if (this.calendar == null) {
            return guessCalendar();
        }
        Calendar temp = (Calendar) this.calendar.clone();
        temp.setTimeZone(getTimeZone());
        temp.setTimeInMillis(System.currentTimeMillis());
        return temp;
    }

    public GlobalizationPreferences setTimeZone(TimeZone timezone) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        this.timezone = (TimeZone) timezone.clone();
        return this;
    }

    public TimeZone getTimeZone() {
        if (this.timezone == null) {
            return guessTimeZone();
        }
        return this.timezone.cloneAsThawed();
    }

    public Collator getCollator() {
        if (this.collator == null) {
            return guessCollator();
        }
        try {
            return (Collator) this.collator.clone();
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Error in cloning collator", e);
        }
    }

    public GlobalizationPreferences setCollator(Collator collator) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        try {
            this.collator = (Collator) collator.clone();
            return this;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("Error in cloning collator", e);
        }
    }

    public BreakIterator getBreakIterator(int type) {
        if (type < 0 || type >= 5) {
            throw new IllegalArgumentException("Illegal break iterator type");
        } else if (this.breakIterators == null || this.breakIterators[type] == null) {
            return guessBreakIterator(type);
        } else {
            return (BreakIterator) this.breakIterators[type].clone();
        }
    }

    public GlobalizationPreferences setBreakIterator(int type, BreakIterator iterator) {
        if (type < 0 || type >= 5) {
            throw new IllegalArgumentException("Illegal break iterator type");
        } else if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        } else {
            if (this.breakIterators == null) {
                this.breakIterators = new BreakIterator[5];
            }
            this.breakIterators[type] = (BreakIterator) iterator.clone();
            return this;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:51:0x000a A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x004c A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getDisplayName(String id, int type) {
        String result = id;
        for (ULocale locale : getLocales()) {
            if (isAvailableLocale(locale, 0)) {
                switch (type) {
                    case 0:
                        result = ULocale.getDisplayName(id, locale);
                    case 1:
                        result = ULocale.getDisplayLanguage(id, locale);
                    case 2:
                        result = ULocale.getDisplayScript("und-" + id, locale);
                    case 3:
                        result = ULocale.getDisplayCountry("und-" + id, locale);
                    case 4:
                        result = ULocale.getDisplayVariant("und-QQ-" + id, locale);
                    case 5:
                        result = ULocale.getDisplayKeyword(id, locale);
                    case 6:
                        String[] parts = new String[2];
                        Utility.split(id, '=', parts);
                        result = ULocale.getDisplayKeywordValue("und@" + id, parts[0], locale);
                        if (result.equals(parts[1])) {
                            continue;
                        }
                    case 7:
                    case 8:
                        int i;
                        Currency temp = new Currency(id);
                        if (type == 7) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        result = temp.getName(locale, i, new boolean[1]);
                        if (!id.equals(result)) {
                            break;
                        }
                        return result;
                    case 9:
                        SimpleDateFormat dtf = new SimpleDateFormat(DateFormat.GENERIC_TZ, locale);
                        dtf.setTimeZone(TimeZone.getFrozenTimeZone(id));
                        result = dtf.format(new Date());
                        boolean isBadStr = false;
                        String teststr = result;
                        int sidx = result.indexOf(40);
                        int eidx = result.indexOf(41);
                        if (!(sidx == -1 || eidx == -1 || eidx - sidx != 3)) {
                            teststr = result.substring(sidx + 1, eidx);
                        }
                        if (teststr.length() == 2) {
                            isBadStr = true;
                            int i2 = 0;
                            while (i2 < 2) {
                                char c = teststr.charAt(i2);
                                if (c < 'A' || 'Z' < c) {
                                    isBadStr = false;
                                } else {
                                    i2++;
                                }
                            }
                        }
                        if (isBadStr) {
                            continue;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown type: " + type);
                }
                if (!id.equals(result)) {
                }
            }
        }
        return result;
    }

    public GlobalizationPreferences setDateFormat(int dateStyle, int timeStyle, DateFormat format) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        if (this.dateFormats == null) {
            this.dateFormats = (DateFormat[][]) Array.newInstance(DateFormat.class, new int[]{5, 5});
        }
        this.dateFormats[dateStyle][timeStyle] = (DateFormat) format.clone();
        return this;
    }

    public DateFormat getDateFormat(int dateStyle, int timeStyle) {
        if (!(dateStyle == 4 && timeStyle == 4) && dateStyle >= 0 && dateStyle < 5 && timeStyle >= 0 && timeStyle < 5) {
            DateFormat result = null;
            if (this.dateFormats != null) {
                result = this.dateFormats[dateStyle][timeStyle];
            }
            if (result == null) {
                return guessDateFormat(dateStyle, timeStyle);
            }
            result = (DateFormat) result.clone();
            result.setTimeZone(getTimeZone());
            return result;
        }
        throw new IllegalArgumentException("Illegal date format style arguments");
    }

    public NumberFormat getNumberFormat(int style) {
        if (style < 0 || style >= 5) {
            throw new IllegalArgumentException("Illegal number format type");
        }
        NumberFormat result = null;
        if (this.numberFormats != null) {
            result = this.numberFormats[style];
        }
        if (result != null) {
            return (NumberFormat) result.clone();
        }
        return guessNumberFormat(style);
    }

    public GlobalizationPreferences setNumberFormat(int style, NumberFormat format) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
        if (this.numberFormats == null) {
            this.numberFormats = new NumberFormat[5];
        }
        this.numberFormats[style] = (NumberFormat) format.clone();
        return this;
    }

    public GlobalizationPreferences reset() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        }
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

    protected List<ULocale> processLocales(List<ULocale> inputLocales) {
        int i;
        ULocale uloc;
        List<ULocale> result = new ArrayList();
        for (i = 0; i < inputLocales.size(); i++) {
            uloc = (ULocale) inputLocales.get(i);
            String language = uloc.getLanguage();
            String script = uloc.getScript();
            String country = uloc.getCountry();
            String variant = uloc.getVariant();
            boolean bInserted = false;
            for (int j = 0; j < result.size(); j++) {
                ULocale u = (ULocale) result.get(j);
                if (u.getLanguage().equals(language)) {
                    String s = u.getScript();
                    String c = u.getCountry();
                    String v = u.getVariant();
                    if (!s.equals(script)) {
                        if (s.length() != 0 || c.length() != 0 || v.length() != 0) {
                            if (s.length() != 0 || !c.equals(country)) {
                                if (script.length() == 0 && country.length() > 0 && c.length() == 0) {
                                    result.add(j, uloc);
                                    bInserted = true;
                                    break;
                                }
                            }
                            result.add(j, uloc);
                            bInserted = true;
                            break;
                        }
                        result.add(j, uloc);
                        bInserted = true;
                        break;
                    } else if (c.equals(country) || c.length() != 0 || v.length() != 0) {
                        if (!v.equals(variant) && v.length() == 0) {
                            result.add(j, uloc);
                            bInserted = true;
                            break;
                        }
                    } else {
                        result.add(j, uloc);
                        bInserted = true;
                        break;
                    }
                }
            }
            if (!bInserted) {
                result.add(uloc);
            }
        }
        int index = 0;
        while (index < result.size()) {
            uloc = (ULocale) result.get(index);
            while (true) {
                uloc = uloc.getFallback();
                if (uloc == null || uloc.getLanguage().length() == 0) {
                    index++;
                } else {
                    index++;
                    result.add(index, uloc);
                }
            }
            index++;
        }
        index = 0;
        while (index < result.size() - 1) {
            uloc = (ULocale) result.get(index);
            boolean bRemoved = false;
            for (i = index + 1; i < result.size(); i++) {
                if (uloc.equals(result.get(i))) {
                    result.remove(index);
                    bRemoved = true;
                    break;
                }
            }
            if (!bRemoved) {
                index++;
            }
        }
        return result;
    }

    protected DateFormat guessDateFormat(int dateStyle, int timeStyle) {
        ULocale dfLocale = getAvailableLocale(2);
        if (dfLocale == null) {
            dfLocale = ULocale.ROOT;
        }
        if (timeStyle == 4) {
            return DateFormat.getDateInstance(getCalendar(), dateStyle, dfLocale);
        }
        if (dateStyle == 4) {
            return DateFormat.getTimeInstance(getCalendar(), timeStyle, dfLocale);
        }
        return DateFormat.getDateTimeInstance(getCalendar(), dateStyle, timeStyle, dfLocale);
    }

    protected NumberFormat guessNumberFormat(int style) {
        ULocale nfLocale = getAvailableLocale(3);
        if (nfLocale == null) {
            nfLocale = ULocale.ROOT;
        }
        switch (style) {
            case 0:
                return NumberFormat.getInstance(nfLocale);
            case 1:
                NumberFormat result = NumberFormat.getCurrencyInstance(nfLocale);
                result.setCurrency(getCurrency());
                return result;
            case 2:
                return NumberFormat.getPercentInstance(nfLocale);
            case 3:
                return NumberFormat.getScientificInstance(nfLocale);
            case 4:
                return NumberFormat.getIntegerInstance(nfLocale);
            default:
                throw new IllegalArgumentException("Unknown number format style");
        }
    }

    protected String guessTerritory() {
        String result;
        for (ULocale locale : getLocales()) {
            result = locale.getCountry();
            if (result.length() != 0) {
                return result;
            }
        }
        ULocale firstLocale = getLocale(0);
        String language = firstLocale.getLanguage();
        String script = firstLocale.getScript();
        result = null;
        if (script.length() != 0) {
            result = (String) language_territory_hack_map.get(language + BaseLocale.SEP + script);
        }
        if (result == null) {
            result = (String) language_territory_hack_map.get(language);
        }
        if (result == null) {
            result = "US";
        }
        return result;
    }

    protected Currency guessCurrency() {
        return Currency.getInstance(new ULocale("und-" + getTerritory()));
    }

    protected List<ULocale> guessLocales() {
        if (this.implicitLocales == null) {
            List<ULocale> result = new ArrayList(1);
            result.add(ULocale.getDefault());
            this.implicitLocales = processLocales(result);
        }
        return this.implicitLocales;
    }

    protected Collator guessCollator() {
        ULocale collLocale = getAvailableLocale(4);
        if (collLocale == null) {
            collLocale = ULocale.ROOT;
        }
        return Collator.getInstance(collLocale);
    }

    protected BreakIterator guessBreakIterator(int type) {
        ULocale brkLocale = getAvailableLocale(5);
        if (brkLocale == null) {
            brkLocale = ULocale.ROOT;
        }
        switch (type) {
            case 0:
                return BreakIterator.getCharacterInstance(brkLocale);
            case 1:
                return BreakIterator.getWordInstance(brkLocale);
            case 2:
                return BreakIterator.getLineInstance(brkLocale);
            case 3:
                return BreakIterator.getSentenceInstance(brkLocale);
            case 4:
                return BreakIterator.getTitleInstance(brkLocale);
            default:
                throw new IllegalArgumentException("Unknown break iterator type");
        }
    }

    protected TimeZone guessTimeZone() {
        String timezoneString = (String) territory_tzid_hack_map.get(getTerritory());
        if (timezoneString == null) {
            String[] attempt = TimeZone.getAvailableIDs(getTerritory());
            if (attempt.length == 0) {
                timezoneString = "Etc/GMT";
            } else {
                int i = 0;
                while (i < attempt.length && attempt[i].indexOf("/") < 0) {
                    i++;
                }
                if (i > attempt.length) {
                    i = 0;
                }
                timezoneString = attempt[i];
            }
        }
        return TimeZone.getTimeZone(timezoneString);
    }

    protected Calendar guessCalendar() {
        ULocale calLocale = getAvailableLocale(1);
        if (calLocale == null) {
            calLocale = ULocale.US;
        }
        return Calendar.getInstance(getTimeZone(), calLocale);
    }

    private ULocale getAvailableLocale(int type) {
        List<ULocale> locs = getLocales();
        for (int i = 0; i < locs.size(); i++) {
            ULocale l = (ULocale) locs.get(i);
            if (isAvailableLocale(l, type)) {
                return l;
            }
        }
        return null;
    }

    private boolean isAvailableLocale(ULocale loc, int type) {
        BitSet bits = (BitSet) available_locales.get(loc);
        if (bits == null || !bits.get(type)) {
            return false;
        }
        return true;
    }

    static {
        int i;
        BitSet bits;
        ULocale[] allLocales = ULocale.getAvailableLocales();
        for (Object put : allLocales) {
            bits = new BitSet(6);
            available_locales.put(put, bits);
            bits.set(0);
        }
        ULocale[] calLocales = Calendar.getAvailableULocales();
        for (i = 0; i < calLocales.length; i++) {
            bits = (BitSet) available_locales.get(calLocales[i]);
            if (bits == null) {
                bits = new BitSet(6);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(1);
        }
        ULocale[] dateLocales = DateFormat.getAvailableULocales();
        for (i = 0; i < dateLocales.length; i++) {
            bits = (BitSet) available_locales.get(dateLocales[i]);
            if (bits == null) {
                bits = new BitSet(6);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(2);
        }
        ULocale[] numLocales = NumberFormat.getAvailableULocales();
        for (i = 0; i < numLocales.length; i++) {
            bits = (BitSet) available_locales.get(numLocales[i]);
            if (bits == null) {
                bits = new BitSet(6);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(3);
        }
        ULocale[] collLocales = Collator.getAvailableULocales();
        for (i = 0; i < collLocales.length; i++) {
            bits = (BitSet) available_locales.get(collLocales[i]);
            if (bits == null) {
                bits = new BitSet(6);
                available_locales.put(allLocales[i], bits);
            }
            bits.set(4);
        }
        ULocale[] brkLocales = BreakIterator.getAvailableULocales();
        for (Object put2 : brkLocales) {
            ((BitSet) available_locales.get(put2)).set(5);
        }
        r8 = new String[154][];
        r8[0] = new String[]{"af", "ZA"};
        r8[1] = new String[]{"am", "ET"};
        r8[2] = new String[]{"ar", "SA"};
        r8[3] = new String[]{"as", "IN"};
        r8[4] = new String[]{"ay", "PE"};
        r8[5] = new String[]{"az", "AZ"};
        r8[6] = new String[]{"bal", "PK"};
        r8[7] = new String[]{"be", "BY"};
        r8[8] = new String[]{"bg", "BG"};
        r8[9] = new String[]{"bn", "IN"};
        r8[10] = new String[]{"bs", "BA"};
        r8[11] = new String[]{"ca", "ES"};
        r8[12] = new String[]{"ch", "MP"};
        r8[13] = new String[]{"cpe", "SL"};
        r8[14] = new String[]{"cs", "CZ"};
        r8[15] = new String[]{"cy", "GB"};
        r8[16] = new String[]{"da", "DK"};
        r8[17] = new String[]{"de", "DE"};
        r8[18] = new String[]{"dv", "MV"};
        r8[19] = new String[]{"dz", "BT"};
        r8[20] = new String[]{"el", "GR"};
        r8[21] = new String[]{"en", "US"};
        r8[22] = new String[]{"es", "ES"};
        r8[23] = new String[]{"et", "EE"};
        r8[24] = new String[]{"eu", "ES"};
        r8[25] = new String[]{"fa", "IR"};
        r8[26] = new String[]{"fi", "FI"};
        r8[27] = new String[]{"fil", "PH"};
        r8[28] = new String[]{"fj", "FJ"};
        r8[29] = new String[]{"fo", "FO"};
        r8[30] = new String[]{"fr", "FR"};
        r8[31] = new String[]{"ga", "IE"};
        r8[32] = new String[]{"gd", "GB"};
        r8[33] = new String[]{"gl", "ES"};
        r8[34] = new String[]{"gn", "PY"};
        r8[35] = new String[]{"gu", "IN"};
        r8[36] = new String[]{"gv", "GB"};
        r8[37] = new String[]{"ha", "NG"};
        r8[38] = new String[]{"he", "IL"};
        r8[39] = new String[]{"hi", "IN"};
        r8[40] = new String[]{"ho", "PG"};
        r8[41] = new String[]{"hr", "HR"};
        r8[42] = new String[]{"ht", "HT"};
        r8[43] = new String[]{"hu", "HU"};
        r8[44] = new String[]{"hy", "AM"};
        r8[45] = new String[]{"id", "ID"};
        r8[46] = new String[]{"is", "IS"};
        r8[47] = new String[]{"it", "IT"};
        r8[48] = new String[]{"ja", "JP"};
        r8[49] = new String[]{"ka", "GE"};
        r8[50] = new String[]{"kk", "KZ"};
        r8[51] = new String[]{"kl", "GL"};
        r8[52] = new String[]{"km", "KH"};
        r8[53] = new String[]{"kn", "IN"};
        r8[54] = new String[]{"ko", "KR"};
        r8[55] = new String[]{"kok", "IN"};
        r8[56] = new String[]{"ks", "IN"};
        r8[57] = new String[]{"ku", "TR"};
        r8[58] = new String[]{"ky", "KG"};
        r8[59] = new String[]{"la", "VA"};
        r8[60] = new String[]{"lb", "LU"};
        r8[61] = new String[]{"ln", "CG"};
        r8[62] = new String[]{"lo", "LA"};
        r8[63] = new String[]{"lt", "LT"};
        r8[64] = new String[]{"lv", "LV"};
        r8[65] = new String[]{"mai", "IN"};
        r8[66] = new String[]{"men", "GN"};
        r8[67] = new String[]{"mg", "MG"};
        r8[68] = new String[]{"mh", "MH"};
        r8[69] = new String[]{"mk", "MK"};
        r8[70] = new String[]{"ml", "IN"};
        r8[71] = new String[]{"mn", "MN"};
        r8[72] = new String[]{"mni", "IN"};
        r8[73] = new String[]{"mo", "MD"};
        r8[74] = new String[]{"mr", "IN"};
        r8[75] = new String[]{DateFormat.MINUTE_SECOND, "MY"};
        r8[76] = new String[]{"mt", "MT"};
        r8[77] = new String[]{"my", "MM"};
        r8[78] = new String[]{"na", "NR"};
        r8[79] = new String[]{"nb", "NO"};
        r8[80] = new String[]{"nd", "ZA"};
        r8[81] = new String[]{"ne", "NP"};
        r8[82] = new String[]{"niu", "NU"};
        r8[83] = new String[]{"nl", "NL"};
        r8[84] = new String[]{"nn", "NO"};
        r8[85] = new String[]{"no", "NO"};
        r8[86] = new String[]{"nr", "ZA"};
        r8[87] = new String[]{"nso", "ZA"};
        r8[88] = new String[]{"ny", "MW"};
        r8[89] = new String[]{"om", "KE"};
        r8[90] = new String[]{"or", "IN"};
        r8[91] = new String[]{"pa", "IN"};
        r8[92] = new String[]{"pau", "PW"};
        r8[93] = new String[]{"pl", "PL"};
        r8[94] = new String[]{"ps", "PK"};
        r8[95] = new String[]{"pt", "BR"};
        r8[96] = new String[]{"qu", "PE"};
        r8[97] = new String[]{"rn", "BI"};
        r8[98] = new String[]{"ro", "RO"};
        r8[99] = new String[]{"ru", "RU"};
        r8[100] = new String[]{"rw", "RW"};
        r8[101] = new String[]{"sd", "IN"};
        r8[102] = new String[]{"sg", "CF"};
        r8[103] = new String[]{"si", "LK"};
        r8[104] = new String[]{"sk", "SK"};
        r8[105] = new String[]{"sl", "SI"};
        r8[106] = new String[]{"sm", "WS"};
        r8[107] = new String[]{"so", "DJ"};
        r8[108] = new String[]{"sq", "CS"};
        r8[109] = new String[]{"sr", "CS"};
        r8[110] = new String[]{"ss", "ZA"};
        r8[111] = new String[]{"st", "ZA"};
        r8[112] = new String[]{"sv", "SE"};
        r8[113] = new String[]{"sw", "KE"};
        r8[114] = new String[]{"ta", "IN"};
        r8[115] = new String[]{"te", "IN"};
        r8[116] = new String[]{"tem", "SL"};
        r8[117] = new String[]{"tet", "TL"};
        r8[118] = new String[]{"th", "TH"};
        r8[119] = new String[]{"ti", "ET"};
        r8[120] = new String[]{"tg", "TJ"};
        r8[121] = new String[]{"tk", "TM"};
        r8[122] = new String[]{"tkl", "TK"};
        r8[123] = new String[]{"tvl", "TV"};
        r8[124] = new String[]{"tl", "PH"};
        r8[125] = new String[]{"tn", "ZA"};
        r8[126] = new String[]{"to", "TO"};
        r8[127] = new String[]{"tpi", "PG"};
        r8[128] = new String[]{"tr", "TR"};
        r8[129] = new String[]{"ts", "ZA"};
        r8[130] = new String[]{"uk", "UA"};
        r8[131] = new String[]{"ur", "IN"};
        r8[132] = new String[]{"uz", "UZ"};
        r8[133] = new String[]{"ve", "ZA"};
        r8[134] = new String[]{"vi", "VN"};
        r8[135] = new String[]{"wo", "SN"};
        r8[136] = new String[]{"xh", "ZA"};
        r8[137] = new String[]{"zh", "CN"};
        r8[138] = new String[]{"zh_Hant", "TW"};
        r8[139] = new String[]{"zu", "ZA"};
        r8[140] = new String[]{"aa", "ET"};
        r8[141] = new String[]{"byn", "ER"};
        r8[142] = new String[]{"eo", "DE"};
        r8[143] = new String[]{"gez", "ET"};
        r8[144] = new String[]{"haw", "US"};
        r8[145] = new String[]{"iu", "CA"};
        r8[146] = new String[]{"kw", "GB"};
        r8[147] = new String[]{"sa", "IN"};
        r8[148] = new String[]{"sh", "HR"};
        r8[149] = new String[]{"sid", "ET"};
        r8[150] = new String[]{"syr", "SY"};
        r8[151] = new String[]{"tig", "ER"};
        r8[152] = new String[]{"tt", "RU"};
        r8[153] = new String[]{"wal", "ET"};
        language_territory_hack = r8;
        for (i = 0; i < language_territory_hack.length; i++) {
            language_territory_hack_map.put(language_territory_hack[i][0], language_territory_hack[i][1]);
        }
        r8 = new String[30][];
        r8[0] = new String[]{"AQ", "Antarctica/McMurdo"};
        r8[1] = new String[]{"AR", "America/Buenos_Aires"};
        r8[2] = new String[]{"AU", "Australia/Sydney"};
        r8[3] = new String[]{"BR", "America/Sao_Paulo"};
        r8[4] = new String[]{"CA", "America/Toronto"};
        r8[5] = new String[]{"CD", "Africa/Kinshasa"};
        r8[6] = new String[]{"CL", "America/Santiago"};
        r8[7] = new String[]{"CN", "Asia/Shanghai"};
        r8[8] = new String[]{"EC", "America/Guayaquil"};
        r8[9] = new String[]{"ES", "Europe/Madrid"};
        r8[10] = new String[]{"GB", "Europe/London"};
        r8[11] = new String[]{"GL", "America/Godthab"};
        r8[12] = new String[]{"ID", "Asia/Jakarta"};
        r8[13] = new String[]{"ML", "Africa/Bamako"};
        r8[14] = new String[]{"MX", "America/Mexico_City"};
        r8[15] = new String[]{"MY", "Asia/Kuala_Lumpur"};
        r8[16] = new String[]{"NZ", "Pacific/Auckland"};
        r8[17] = new String[]{"PT", "Europe/Lisbon"};
        r8[18] = new String[]{"RU", "Europe/Moscow"};
        r8[19] = new String[]{"UA", "Europe/Kiev"};
        r8[20] = new String[]{"US", "America/New_York"};
        r8[21] = new String[]{"UZ", "Asia/Tashkent"};
        r8[22] = new String[]{"PF", "Pacific/Tahiti"};
        r8[23] = new String[]{"FM", "Pacific/Kosrae"};
        r8[24] = new String[]{"KI", "Pacific/Tarawa"};
        r8[25] = new String[]{"KZ", "Asia/Almaty"};
        r8[26] = new String[]{"MH", "Pacific/Majuro"};
        r8[27] = new String[]{"MN", "Asia/Ulaanbaatar"};
        r8[28] = new String[]{"SJ", "Arctic/Longyearbyen"};
        r8[29] = new String[]{"UM", "Pacific/Midway"};
        territory_tzid_hack = r8;
        for (i = 0; i < territory_tzid_hack.length; i++) {
            territory_tzid_hack_map.put(territory_tzid_hack[i][0], territory_tzid_hack[i][1]);
        }
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public GlobalizationPreferences freeze() {
        this.frozen = true;
        return this;
    }

    public GlobalizationPreferences cloneAsThawed() {
        try {
            GlobalizationPreferences result = (GlobalizationPreferences) clone();
            result.frozen = false;
            return result;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

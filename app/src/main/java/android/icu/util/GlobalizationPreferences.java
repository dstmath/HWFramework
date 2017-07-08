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
import org.xmlpull.v1.XmlPullParser;

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
    private static final HashMap<ULocale, BitSet> available_locales = null;
    private static final String[][] language_territory_hack = null;
    private static final Map<String, String> language_territory_hack_map = null;
    static final String[][] territory_tzid_hack = null;
    static final Map<String, String> territory_tzid_hack_map = null;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.GlobalizationPreferences.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.GlobalizationPreferences.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.GlobalizationPreferences.<clinit>():void");
    }

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
        ULocale[] uLocaleArr = new ULocale[TYPE_CALENDAR];
        uLocaleArr[TYPE_GENERIC] = uLocale;
        return setLocales(uLocaleArr);
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
        for (int i = TYPE_GENERIC; i < fallbacks.size(); i += TYPE_CALENDAR) {
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
        throw new MissingResourceException("Can't find bundle for base name " + baseName, baseName, XmlPullParser.NO_NAMESPACE);
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
        if (type < 0 || type >= TYPE_BREAKITERATOR) {
            throw new IllegalArgumentException("Illegal break iterator type");
        } else if (this.breakIterators == null || this.breakIterators[type] == null) {
            return guessBreakIterator(type);
        } else {
            return (BreakIterator) this.breakIterators[type].clone();
        }
    }

    public GlobalizationPreferences setBreakIterator(int type, BreakIterator iterator) {
        if (type < 0 || type >= TYPE_BREAKITERATOR) {
            throw new IllegalArgumentException("Illegal break iterator type");
        } else if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify immutable object");
        } else {
            if (this.breakIterators == null) {
                this.breakIterators = new BreakIterator[TYPE_BREAKITERATOR];
            }
            this.breakIterators[type] = (BreakIterator) iterator.clone();
            return this;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getDisplayName(String id, int type) {
        String result = id;
        for (ULocale locale : getLocales()) {
            if (isAvailableLocale(locale, TYPE_GENERIC)) {
                switch (type) {
                    case TYPE_GENERIC /*0*/:
                        result = ULocale.getDisplayName(id, locale);
                    case TYPE_CALENDAR /*1*/:
                        result = ULocale.getDisplayLanguage(id, locale);
                    case TYPE_DATEFORMAT /*2*/:
                        result = ULocale.getDisplayScript("und-" + id, locale);
                    case TYPE_NUMBERFORMAT /*3*/:
                        result = ULocale.getDisplayCountry("und-" + id, locale);
                    case TYPE_COLLATOR /*4*/:
                        result = ULocale.getDisplayVariant("und-QQ-" + id, locale);
                    case TYPE_BREAKITERATOR /*5*/:
                        result = ULocale.getDisplayKeyword(id, locale);
                    case TYPE_LIMIT /*6*/:
                        String[] parts = new String[TYPE_DATEFORMAT];
                        Utility.split(id, '=', parts);
                        result = ULocale.getDisplayKeywordValue("und@" + id, parts[TYPE_GENERIC], locale);
                        if (result.equals(parts[TYPE_CALENDAR])) {
                            continue;
                        }
                    case ID_CURRENCY /*7*/:
                    case ID_CURRENCY_SYMBOL /*8*/:
                        int i;
                        Currency temp = new Currency(id);
                        if (type == ID_CURRENCY) {
                            i = TYPE_CALENDAR;
                        } else {
                            i = TYPE_GENERIC;
                        }
                        result = temp.getName(locale, i, new boolean[TYPE_CALENDAR]);
                        if (id.equals(result)) {
                            break;
                        }
                        return result;
                    case ID_TIMEZONE /*9*/:
                        SimpleDateFormat dtf = new SimpleDateFormat(DateFormat.GENERIC_TZ, locale);
                        dtf.setTimeZone(TimeZone.getFrozenTimeZone(id));
                        result = dtf.format(new Date());
                        boolean isBadStr = false;
                        String teststr = result;
                        int sidx = result.indexOf(40);
                        int eidx = result.indexOf(41);
                        if (!(sidx == -1 || eidx == -1 || eidx - sidx != TYPE_NUMBERFORMAT)) {
                            teststr = result.substring(sidx + TYPE_CALENDAR, eidx);
                        }
                        if (teststr.length() == TYPE_DATEFORMAT) {
                            isBadStr = true;
                            int i2 = TYPE_GENERIC;
                            while (i2 < TYPE_DATEFORMAT) {
                                char c = teststr.charAt(i2);
                                if (c < 'A' || 'Z' < c) {
                                    isBadStr = false;
                                } else {
                                    i2 += TYPE_CALENDAR;
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
                if (id.equals(result)) {
                    return result;
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
            this.dateFormats = (DateFormat[][]) Array.newInstance(DateFormat.class, new int[]{TYPE_BREAKITERATOR, TYPE_BREAKITERATOR});
        }
        this.dateFormats[dateStyle][timeStyle] = (DateFormat) format.clone();
        return this;
    }

    public DateFormat getDateFormat(int dateStyle, int timeStyle) {
        if (!(dateStyle == TYPE_COLLATOR && timeStyle == TYPE_COLLATOR) && dateStyle >= 0 && dateStyle < TYPE_BREAKITERATOR && timeStyle >= 0 && timeStyle < TYPE_BREAKITERATOR) {
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
        if (style < 0 || style >= TYPE_BREAKITERATOR) {
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
            this.numberFormats = new NumberFormat[TYPE_BREAKITERATOR];
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
        List<ULocale> result = new ArrayList();
        for (i = TYPE_GENERIC; i < inputLocales.size(); i += TYPE_CALENDAR) {
            ULocale uloc = (ULocale) inputLocales.get(i);
            String language = uloc.getLanguage();
            String script = uloc.getScript();
            String country = uloc.getCountry();
            String variant = uloc.getVariant();
            boolean bInserted = false;
            for (int j = TYPE_GENERIC; j < result.size(); j += TYPE_CALENDAR) {
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
        int index = TYPE_GENERIC;
        while (index < result.size()) {
            uloc = (ULocale) result.get(index);
            while (true) {
                uloc = uloc.getFallback();
                if (uloc.getLanguage().length() == 0) {
                    break;
                }
                index += TYPE_CALENDAR;
                result.add(index, uloc);
            }
            index += TYPE_CALENDAR;
        }
        index = TYPE_GENERIC;
        while (index < result.size() - 1) {
            uloc = (ULocale) result.get(index);
            boolean bRemoved = false;
            for (i = index + TYPE_CALENDAR; i < result.size(); i += TYPE_CALENDAR) {
                if (uloc.equals(result.get(i))) {
                    result.remove(index);
                    bRemoved = true;
                    break;
                }
            }
            if (!bRemoved) {
                index += TYPE_CALENDAR;
            }
        }
        return result;
    }

    protected DateFormat guessDateFormat(int dateStyle, int timeStyle) {
        ULocale dfLocale = getAvailableLocale(TYPE_DATEFORMAT);
        if (dfLocale == null) {
            dfLocale = ULocale.ROOT;
        }
        if (timeStyle == TYPE_COLLATOR) {
            return DateFormat.getDateInstance(getCalendar(), dateStyle, dfLocale);
        }
        if (dateStyle == TYPE_COLLATOR) {
            return DateFormat.getTimeInstance(getCalendar(), timeStyle, dfLocale);
        }
        return DateFormat.getDateTimeInstance(getCalendar(), dateStyle, timeStyle, dfLocale);
    }

    protected NumberFormat guessNumberFormat(int style) {
        ULocale nfLocale = getAvailableLocale(TYPE_NUMBERFORMAT);
        if (nfLocale == null) {
            nfLocale = ULocale.ROOT;
        }
        switch (style) {
            case TYPE_GENERIC /*0*/:
                return NumberFormat.getInstance(nfLocale);
            case TYPE_CALENDAR /*1*/:
                NumberFormat result = NumberFormat.getCurrencyInstance(nfLocale);
                result.setCurrency(getCurrency());
                return result;
            case TYPE_DATEFORMAT /*2*/:
                return NumberFormat.getPercentInstance(nfLocale);
            case TYPE_NUMBERFORMAT /*3*/:
                return NumberFormat.getScientificInstance(nfLocale);
            case TYPE_COLLATOR /*4*/:
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
        ULocale firstLocale = getLocale(TYPE_GENERIC);
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
            List<ULocale> result = new ArrayList(TYPE_CALENDAR);
            result.add(ULocale.getDefault());
            this.implicitLocales = processLocales(result);
        }
        return this.implicitLocales;
    }

    protected Collator guessCollator() {
        ULocale collLocale = getAvailableLocale(TYPE_COLLATOR);
        if (collLocale == null) {
            collLocale = ULocale.ROOT;
        }
        return Collator.getInstance(collLocale);
    }

    protected BreakIterator guessBreakIterator(int type) {
        ULocale brkLocale = getAvailableLocale(TYPE_BREAKITERATOR);
        if (brkLocale == null) {
            brkLocale = ULocale.ROOT;
        }
        switch (type) {
            case TYPE_GENERIC /*0*/:
                return BreakIterator.getCharacterInstance(brkLocale);
            case TYPE_CALENDAR /*1*/:
                return BreakIterator.getWordInstance(brkLocale);
            case TYPE_DATEFORMAT /*2*/:
                return BreakIterator.getLineInstance(brkLocale);
            case TYPE_NUMBERFORMAT /*3*/:
                return BreakIterator.getSentenceInstance(brkLocale);
            case TYPE_COLLATOR /*4*/:
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
                int i = TYPE_GENERIC;
                while (i < attempt.length && attempt[i].indexOf("/") < 0) {
                    i += TYPE_CALENDAR;
                }
                if (i > attempt.length) {
                    i = TYPE_GENERIC;
                }
                timezoneString = attempt[i];
            }
        }
        return TimeZone.getTimeZone(timezoneString);
    }

    protected Calendar guessCalendar() {
        ULocale calLocale = getAvailableLocale(TYPE_CALENDAR);
        if (calLocale == null) {
            calLocale = ULocale.US;
        }
        return Calendar.getInstance(getTimeZone(), calLocale);
    }

    private ULocale getAvailableLocale(int type) {
        List<ULocale> locs = getLocales();
        for (int i = TYPE_GENERIC; i < locs.size(); i += TYPE_CALENDAR) {
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

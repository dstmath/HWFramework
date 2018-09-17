package java.time.format;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.UResourceBundle;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.CalendarDataUtility;

class DateTimeTextProvider {
    private static final /* synthetic */ int[] -java-time-temporal-ChronoFieldSwitchesValues = null;
    private static final ConcurrentMap<Entry<TemporalField, Locale>, Object> CACHE = new ConcurrentHashMap(16, 0.75f, 2);
    private static final Comparator<Entry<String, Long>> COMPARATOR = new Comparator<Entry<String, Long>>() {
        public int compare(Entry<String, Long> obj1, Entry<String, Long> obj2) {
            return ((String) obj2.getKey()).length() - ((String) obj1.getKey()).length();
        }
    };

    static final class LocaleStore {
        private final Map<TextStyle, List<Entry<String, Long>>> parsable;
        private final Map<TextStyle, Map<Long, String>> valueTextMap;

        LocaleStore(Map<TextStyle, Map<Long, String>> valueTextMap) {
            this.valueTextMap = valueTextMap;
            Map<TextStyle, List<Entry<String, Long>>> map = new HashMap();
            List<Entry<String, Long>> allList = new ArrayList();
            for (Entry<TextStyle, Map<Long, String>> vtmEntry : valueTextMap.entrySet()) {
                Map<String, Entry<String, Long>> reverse = new HashMap();
                for (Entry<Long, String> entry : ((Map) vtmEntry.getValue()).entrySet()) {
                    Object put = reverse.put((String) entry.getValue(), DateTimeTextProvider.createEntry((String) entry.getValue(), (Long) entry.getKey()));
                }
                List<Entry<String, Long>> list = new ArrayList(reverse.values());
                Collections.sort(list, DateTimeTextProvider.COMPARATOR);
                map.put((TextStyle) vtmEntry.getKey(), list);
                allList.addAll(list);
                map.put(null, allList);
            }
            Collections.sort(allList, DateTimeTextProvider.COMPARATOR);
            this.parsable = map;
        }

        String getText(long value, TextStyle style) {
            Map<Long, String> map = (Map) this.valueTextMap.get(style);
            if (map != null) {
                return (String) map.get(Long.valueOf(value));
            }
            return null;
        }

        Iterator<Entry<String, Long>> getTextIterator(TextStyle style) {
            List<Entry<String, Long>> list = (List) this.parsable.get(style);
            if (list != null) {
                return list.iterator();
            }
            return null;
        }
    }

    private static /* synthetic */ int[] -getjava-time-temporal-ChronoFieldSwitchesValues() {
        if (-java-time-temporal-ChronoFieldSwitchesValues != null) {
            return -java-time-temporal-ChronoFieldSwitchesValues;
        }
        int[] iArr = new int[ChronoField.values().length];
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_MONTH.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ChronoField.ALIGNED_WEEK_OF_YEAR.ordinal()] = 8;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ChronoField.AMPM_OF_DAY.ordinal()] = 1;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_AMPM.ordinal()] = 9;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ChronoField.CLOCK_HOUR_OF_DAY.ordinal()] = 10;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ChronoField.DAY_OF_MONTH.ordinal()] = 11;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ChronoField.DAY_OF_WEEK.ordinal()] = 2;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ChronoField.DAY_OF_YEAR.ordinal()] = 12;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ChronoField.EPOCH_DAY.ordinal()] = 13;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ChronoField.ERA.ordinal()] = 3;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ChronoField.HOUR_OF_AMPM.ordinal()] = 14;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ChronoField.HOUR_OF_DAY.ordinal()] = 15;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ChronoField.INSTANT_SECONDS.ordinal()] = 16;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ChronoField.MICRO_OF_DAY.ordinal()] = 17;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ChronoField.MICRO_OF_SECOND.ordinal()] = 18;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ChronoField.MILLI_OF_DAY.ordinal()] = 19;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ChronoField.MILLI_OF_SECOND.ordinal()] = 20;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_DAY.ordinal()] = 21;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ChronoField.MINUTE_OF_HOUR.ordinal()] = 22;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ChronoField.MONTH_OF_YEAR.ordinal()] = 4;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ChronoField.NANO_OF_DAY.ordinal()] = 23;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ChronoField.NANO_OF_SECOND.ordinal()] = 24;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ChronoField.OFFSET_SECONDS.ordinal()] = 25;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ChronoField.PROLEPTIC_MONTH.ordinal()] = 26;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ChronoField.SECOND_OF_DAY.ordinal()] = 27;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ChronoField.SECOND_OF_MINUTE.ordinal()] = 28;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ChronoField.YEAR.ordinal()] = 29;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ChronoField.YEAR_OF_ERA.ordinal()] = 30;
        } catch (NoSuchFieldError e30) {
        }
        -java-time-temporal-ChronoFieldSwitchesValues = iArr;
        return iArr;
    }

    DateTimeTextProvider() {
    }

    static DateTimeTextProvider getInstance() {
        return new DateTimeTextProvider();
    }

    public String getText(TemporalField field, long value, TextStyle style, Locale locale) {
        Object store = findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore) store).getText(value, style);
        }
        return null;
    }

    public String getText(Chronology chrono, TemporalField field, long value, TextStyle style, Locale locale) {
        if (chrono == IsoChronology.INSTANCE || ((field instanceof ChronoField) ^ 1) != 0) {
            return getText(field, value, style, locale);
        }
        int fieldIndex;
        int fieldValue;
        if (field == ChronoField.ERA) {
            fieldIndex = 0;
            if (chrono != JapaneseChronology.INSTANCE) {
                fieldValue = (int) value;
            } else if (value == -999) {
                fieldValue = 0;
            } else {
                fieldValue = ((int) value) + 2;
            }
        } else if (field == ChronoField.MONTH_OF_YEAR) {
            fieldIndex = 2;
            fieldValue = ((int) value) - 1;
        } else if (field == ChronoField.DAY_OF_WEEK) {
            fieldIndex = 7;
            fieldValue = ((int) value) + 1;
            if (fieldValue > 7) {
                fieldValue = 1;
            }
        } else if (field != ChronoField.AMPM_OF_DAY) {
            return null;
        } else {
            fieldIndex = 9;
            fieldValue = (int) value;
        }
        return CalendarDataUtility.retrieveJavaTimeFieldValueName(chrono.getCalendarType(), fieldIndex, fieldValue, style.toCalendarStyle(), locale);
    }

    public Iterator<Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
        Object store = findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore) store).getTextIterator(style);
        }
        return null;
    }

    public Iterator<Entry<String, Long>> getTextIterator(Chronology chrono, TemporalField field, TextStyle style, Locale locale) {
        if (chrono == IsoChronology.INSTANCE || ((field instanceof ChronoField) ^ 1) != 0) {
            return getTextIterator(field, style, locale);
        }
        int fieldIndex;
        switch (-getjava-time-temporal-ChronoFieldSwitchesValues()[((ChronoField) field).ordinal()]) {
            case 1:
                fieldIndex = 9;
                break;
            case 2:
                fieldIndex = 7;
                break;
            case 3:
                fieldIndex = 0;
                break;
            case 4:
                fieldIndex = 2;
                break;
            default:
                return null;
        }
        Map<String, Integer> map = CalendarDataUtility.retrieveJavaTimeFieldValueNames(chrono.getCalendarType(), fieldIndex, style == null ? 0 : style.toCalendarStyle(), locale);
        if (map == null) {
            return null;
        }
        List<Entry<String, Long>> list = new ArrayList(map.size());
        switch (fieldIndex) {
            case 0:
                for (Entry<String, Integer> entry : map.entrySet()) {
                    int era = ((Integer) entry.getValue()).intValue();
                    if (chrono == JapaneseChronology.INSTANCE) {
                        if (era == 0) {
                            era = -999;
                        } else {
                            era -= 2;
                        }
                    }
                    list.add(createEntry((String) entry.getKey(), Long.valueOf((long) era)));
                }
                break;
            case 2:
                for (Entry<String, Integer> entry2 : map.entrySet()) {
                    list.add(createEntry((String) entry2.getKey(), Long.valueOf((long) (((Integer) entry2.getValue()).intValue() + 1))));
                }
                break;
            case 7:
                for (Entry<String, Integer> entry22 : map.entrySet()) {
                    list.add(createEntry((String) entry22.getKey(), Long.valueOf((long) toWeekDay(((Integer) entry22.getValue()).intValue()))));
                }
                break;
            default:
                for (Entry<String, Integer> entry222 : map.entrySet()) {
                    list.add(createEntry((String) entry222.getKey(), Long.valueOf((long) ((Integer) entry222.getValue()).intValue())));
                }
                break;
        }
        return list.iterator();
    }

    private Object findStore(TemporalField field, Locale locale) {
        Entry<TemporalField, Locale> key = createEntry(field, locale);
        Object store = CACHE.get(key);
        if (store != null) {
            return store;
        }
        CACHE.putIfAbsent(key, createStore(field, locale));
        return CACHE.get(key);
    }

    private static int toWeekDay(int calWeekDay) {
        if (calWeekDay == 1) {
            return 7;
        }
        return calWeekDay - 1;
    }

    private Object createStore(TemporalField field, Locale locale) {
        Map<TextStyle, Map<Long, String>> styleMap = new HashMap();
        TextStyle[] values;
        int i;
        int length;
        int i2;
        TextStyle textStyle;
        Map<String, Integer> displayNames;
        Map<Long, String> map;
        String name;
        if (field == ChronoField.ERA) {
            values = TextStyle.values();
            i = 0;
            length = values.length;
            while (true) {
                i2 = i;
                if (i2 >= length) {
                    return new LocaleStore(styleMap);
                }
                textStyle = values[i2];
                if (!textStyle.isStandalone()) {
                    displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 0, textStyle.toCalendarStyle(), locale);
                    if (displayNames != null) {
                        map = new HashMap();
                        for (Entry<String, Integer> entry : displayNames.entrySet()) {
                            map.put(Long.valueOf((long) ((Integer) entry.getValue()).intValue()), (String) entry.getKey());
                        }
                        if (!map.isEmpty()) {
                            styleMap.put(textStyle, map);
                        }
                    }
                }
                i = i2 + 1;
            }
        } else if (field == ChronoField.MONTH_OF_YEAR) {
            values = TextStyle.values();
            i = 0;
            length = values.length;
            while (true) {
                i2 = i;
                if (i2 >= length) {
                    return new LocaleStore(styleMap);
                }
                textStyle = values[i2];
                displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 2, textStyle.toCalendarStyle(), locale);
                map = new HashMap();
                if (displayNames != null) {
                    for (Entry<String, Integer> entry2 : displayNames.entrySet()) {
                        map.put(Long.valueOf((long) (((Integer) entry2.getValue()).intValue() + 1)), (String) entry2.getKey());
                    }
                } else {
                    for (int month = 0; month <= 11; month++) {
                        name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 2, month, textStyle.toCalendarStyle(), locale);
                        if (name == null) {
                            break;
                        }
                        map.put(Long.valueOf((long) (month + 1)), name);
                    }
                }
                if (!map.isEmpty()) {
                    styleMap.put(textStyle, map);
                }
                i = i2 + 1;
            }
        } else if (field == ChronoField.DAY_OF_WEEK) {
            values = TextStyle.values();
            i = 0;
            length = values.length;
            while (true) {
                i2 = i;
                if (i2 >= length) {
                    return new LocaleStore(styleMap);
                }
                textStyle = values[i2];
                displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 7, textStyle.toCalendarStyle(), locale);
                map = new HashMap();
                if (displayNames != null) {
                    for (Entry<String, Integer> entry22 : displayNames.entrySet()) {
                        map.put(Long.valueOf((long) toWeekDay(((Integer) entry22.getValue()).intValue())), (String) entry22.getKey());
                    }
                } else {
                    for (int wday = 1; wday <= 7; wday++) {
                        name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 7, wday, textStyle.toCalendarStyle(), locale);
                        if (name == null) {
                            break;
                        }
                        map.put(Long.valueOf((long) toWeekDay(wday)), name);
                    }
                }
                if (!map.isEmpty()) {
                    styleMap.put(textStyle, map);
                }
                i = i2 + 1;
            }
        } else if (field == ChronoField.AMPM_OF_DAY) {
            values = TextStyle.values();
            i = 0;
            length = values.length;
            while (true) {
                i2 = i;
                if (i2 >= length) {
                    return new LocaleStore(styleMap);
                }
                textStyle = values[i2];
                if (!textStyle.isStandalone()) {
                    displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 9, textStyle.toCalendarStyle(), locale);
                    if (displayNames != null) {
                        map = new HashMap();
                        for (Entry<String, Integer> entry222 : displayNames.entrySet()) {
                            map.put(Long.valueOf((long) ((Integer) entry222.getValue()).intValue()), (String) entry222.getKey());
                        }
                        if (!map.isEmpty()) {
                            styleMap.put(textStyle, map);
                        }
                    }
                }
                i = i2 + 1;
            }
        } else if (field != IsoFields.QUARTER_OF_YEAR) {
            return "";
        } else {
            ICUResourceBundle quartersRb = ((ICUResourceBundle) UResourceBundle.getBundleInstance("android/icu/impl/data/icudt58b", locale)).getWithFallback("calendar/gregorian/quarters");
            ICUResourceBundle formatRb = quartersRb.getWithFallback("format");
            ICUResourceBundle standaloneRb = quartersRb.getWithFallback("stand-alone");
            styleMap.put(TextStyle.FULL, extractQuarters(formatRb, "wide"));
            styleMap.put(TextStyle.FULL_STANDALONE, extractQuarters(standaloneRb, "wide"));
            styleMap.put(TextStyle.SHORT, extractQuarters(formatRb, "abbreviated"));
            styleMap.put(TextStyle.SHORT_STANDALONE, extractQuarters(standaloneRb, "abbreviated"));
            styleMap.put(TextStyle.NARROW, extractQuarters(formatRb, "narrow"));
            styleMap.put(TextStyle.NARROW_STANDALONE, extractQuarters(standaloneRb, "narrow"));
            return new LocaleStore(styleMap);
        }
    }

    private static Map<Long, String> extractQuarters(ICUResourceBundle rb, String key) {
        String[] names = rb.getWithFallback(key).getStringArray();
        Map<Long, String> map = new HashMap();
        for (int q = 0; q < names.length; q++) {
            map.put(Long.valueOf((long) (q + 1)), names[q]);
        }
        return map;
    }

    private static <A, B> Entry<A, B> createEntry(A text, B field) {
        return new SimpleImmutableEntry(text, field);
    }
}

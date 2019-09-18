package java.time.format;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.UResourceBundle;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.chrono.JapaneseChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.util.locale.provider.CalendarDataUtility;

class DateTimeTextProvider {
    private static final ConcurrentMap<Map.Entry<TemporalField, Locale>, Object> CACHE = new ConcurrentHashMap(16, 0.75f, 2);
    /* access modifiers changed from: private */
    public static final Comparator<Map.Entry<String, Long>> COMPARATOR = new Comparator<Map.Entry<String, Long>>() {
        public int compare(Map.Entry<String, Long> obj1, Map.Entry<String, Long> obj2) {
            return obj2.getKey().length() - obj1.getKey().length();
        }
    };

    static final class LocaleStore {
        private final Map<TextStyle, List<Map.Entry<String, Long>>> parsable;
        private final Map<TextStyle, Map<Long, String>> valueTextMap;

        /* JADX WARNING: Removed duplicated region for block: B:6:0x003c  */
        LocaleStore(Map<TextStyle, Map<Long, String>> valueTextMap2) {
            this.valueTextMap = valueTextMap2;
            Map<TextStyle, List<Map.Entry<String, Long>>> map = new HashMap<>();
            List<Map.Entry<String, Long>> allList = new ArrayList<>();
            for (Map.Entry<TextStyle, Map<Long, String>> vtmEntry : valueTextMap2.entrySet()) {
                Map<String, Map.Entry<String, Long>> reverse = new HashMap<>();
                for (Map.Entry<Long, String> entry : vtmEntry.getValue().entrySet()) {
                    if (reverse.put(entry.getValue(), DateTimeTextProvider.createEntry(entry.getValue(), entry.getKey())) != null) {
                    }
                    while (r5.hasNext()) {
                    }
                }
                List<Map.Entry<String, Long>> list = new ArrayList<>((Collection<? extends Map.Entry<String, Long>>) reverse.values());
                Collections.sort(list, DateTimeTextProvider.COMPARATOR);
                map.put(vtmEntry.getKey(), list);
                allList.addAll(list);
                map.put(null, allList);
            }
            Collections.sort(allList, DateTimeTextProvider.COMPARATOR);
            this.parsable = map;
        }

        /* access modifiers changed from: package-private */
        public String getText(long value, TextStyle style) {
            Map<Long, String> map = this.valueTextMap.get(style);
            if (map != null) {
                return map.get(Long.valueOf(value));
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public Iterator<Map.Entry<String, Long>> getTextIterator(TextStyle style) {
            List<Map.Entry<String, Long>> list = this.parsable.get(style);
            if (list != null) {
                return list.iterator();
            }
            return null;
        }
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
        int fieldValue;
        int fieldIndex;
        if (chrono == IsoChronology.INSTANCE || !(field instanceof ChronoField)) {
            return getText(field, value, style, locale);
        }
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

    public Iterator<Map.Entry<String, Long>> getTextIterator(TemporalField field, TextStyle style, Locale locale) {
        Object store = findStore(field, locale);
        if (store instanceof LocaleStore) {
            return ((LocaleStore) store).getTextIterator(style);
        }
        return null;
    }

    public Iterator<Map.Entry<String, Long>> getTextIterator(Chronology chrono, TemporalField field, TextStyle style, Locale locale) {
        int fieldIndex;
        if (chrono == IsoChronology.INSTANCE || !(field instanceof ChronoField)) {
            return getTextIterator(field, style, locale);
        }
        switch ((ChronoField) field) {
            case ERA:
                fieldIndex = 0;
                break;
            case MONTH_OF_YEAR:
                fieldIndex = 2;
                break;
            case DAY_OF_WEEK:
                fieldIndex = 7;
                break;
            case AMPM_OF_DAY:
                fieldIndex = 9;
                break;
            default:
                return null;
        }
        Map<String, Integer> map = CalendarDataUtility.retrieveJavaTimeFieldValueNames(chrono.getCalendarType(), fieldIndex, style == null ? 0 : style.toCalendarStyle(), locale);
        if (map == null) {
            return null;
        }
        List<Map.Entry<String, Long>> list = new ArrayList<>(map.size());
        if (fieldIndex == 0) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                int era = entry.getValue().intValue();
                if (chrono == JapaneseChronology.INSTANCE) {
                    if (era == 0) {
                        era = -999;
                    } else {
                        era -= 2;
                    }
                }
                list.add(createEntry(entry.getKey(), Long.valueOf((long) era)));
            }
        } else if (fieldIndex == 2) {
            for (Map.Entry<String, Integer> entry2 : map.entrySet()) {
                list.add(createEntry(entry2.getKey(), Long.valueOf((long) (entry2.getValue().intValue() + 1))));
            }
        } else if (fieldIndex != 7) {
            for (Map.Entry<String, Integer> entry3 : map.entrySet()) {
                list.add(createEntry(entry3.getKey(), Long.valueOf((long) entry3.getValue().intValue())));
            }
        } else {
            for (Map.Entry<String, Integer> entry4 : map.entrySet()) {
                list.add(createEntry(entry4.getKey(), Long.valueOf((long) toWeekDay(entry4.getValue().intValue()))));
            }
        }
        return list.iterator();
    }

    private Object findStore(TemporalField field, Locale locale) {
        Map.Entry createEntry = createEntry(field, locale);
        Object store = CACHE.get(createEntry);
        if (store != null) {
            return store;
        }
        CACHE.putIfAbsent(createEntry, createStore(field, locale));
        return CACHE.get(createEntry);
    }

    private static int toWeekDay(int calWeekDay) {
        if (calWeekDay == 1) {
            return 7;
        }
        return calWeekDay - 1;
    }

    private Object createStore(TemporalField field, Locale locale) {
        TemporalField temporalField = field;
        Locale locale2 = locale;
        Map<TextStyle, Map<Long, String>> styleMap = new HashMap<>();
        int i = 0;
        if (temporalField == ChronoField.ERA) {
            for (TextStyle textStyle : TextStyle.values()) {
                if (!textStyle.isStandalone()) {
                    Map<String, Integer> displayNames = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 0, textStyle.toCalendarStyle(), locale2);
                    if (displayNames != null) {
                        Map<Long, String> map = new HashMap<>();
                        for (Map.Entry<String, Integer> entry : displayNames.entrySet()) {
                            map.put(Long.valueOf((long) entry.getValue().intValue()), entry.getKey());
                        }
                        if (!map.isEmpty()) {
                            styleMap.put(textStyle, map);
                        }
                    }
                }
            }
            return new LocaleStore(styleMap);
        } else if (temporalField == ChronoField.MONTH_OF_YEAR) {
            for (TextStyle textStyle2 : TextStyle.values()) {
                Map<String, Integer> displayNames2 = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 2, textStyle2.toCalendarStyle(), locale2);
                Map<Long, String> map2 = new HashMap<>();
                if (displayNames2 != null) {
                    for (Map.Entry<String, Integer> entry2 : displayNames2.entrySet()) {
                        map2.put(Long.valueOf((long) (entry2.getValue().intValue() + 1)), entry2.getKey());
                    }
                } else {
                    for (int month = 0; month <= 11; month++) {
                        String name = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 2, month, textStyle2.toCalendarStyle(), locale2);
                        if (name == null) {
                            break;
                        }
                        map2.put(Long.valueOf((long) (month + 1)), name);
                    }
                }
                if (!map2.isEmpty()) {
                    styleMap.put(textStyle2, map2);
                }
            }
            return new LocaleStore(styleMap);
        } else if (temporalField == ChronoField.DAY_OF_WEEK) {
            TextStyle[] values = TextStyle.values();
            int length = values.length;
            while (i < length) {
                TextStyle textStyle3 = values[i];
                Map<String, Integer> displayNames3 = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 7, textStyle3.toCalendarStyle(), locale2);
                Map<Long, String> map3 = new HashMap<>();
                if (displayNames3 != null) {
                    for (Map.Entry<String, Integer> entry3 : displayNames3.entrySet()) {
                        map3.put(Long.valueOf((long) toWeekDay(entry3.getValue().intValue())), entry3.getKey());
                    }
                } else {
                    for (int wday = 1; wday <= 7; wday++) {
                        String name2 = CalendarDataUtility.retrieveJavaTimeFieldValueName("gregory", 7, wday, textStyle3.toCalendarStyle(), locale2);
                        if (name2 == null) {
                            break;
                        }
                        map3.put(Long.valueOf((long) toWeekDay(wday)), name2);
                    }
                }
                if (!map3.isEmpty()) {
                    styleMap.put(textStyle3, map3);
                }
                i++;
            }
            return new LocaleStore(styleMap);
        } else if (temporalField == ChronoField.AMPM_OF_DAY) {
            TextStyle[] values2 = TextStyle.values();
            int length2 = values2.length;
            while (i < length2) {
                TextStyle textStyle4 = values2[i];
                if (!textStyle4.isStandalone()) {
                    Map<String, Integer> displayNames4 = CalendarDataUtility.retrieveJavaTimeFieldValueNames("gregory", 9, textStyle4.toCalendarStyle(), locale2);
                    if (displayNames4 != null) {
                        Map<Long, String> map4 = new HashMap<>();
                        for (Map.Entry<String, Integer> entry4 : displayNames4.entrySet()) {
                            map4.put(Long.valueOf((long) entry4.getValue().intValue()), entry4.getKey());
                        }
                        if (!map4.isEmpty()) {
                            styleMap.put(textStyle4, map4);
                        }
                    }
                }
                i++;
            }
            return new LocaleStore(styleMap);
        } else if (temporalField != IsoFields.QUARTER_OF_YEAR) {
            return "";
        } else {
            ICUResourceBundle quartersRb = UResourceBundle.getBundleInstance("android/icu/impl/data/icudt60b", locale2).getWithFallback("calendar/gregorian/quarters");
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
        Map<Long, String> map = new HashMap<>();
        for (int q = 0; q < names.length; q++) {
            map.put(Long.valueOf((long) (q + 1)), names[q]);
        }
        return map;
    }

    /* access modifiers changed from: private */
    public static <A, B> Map.Entry<A, B> createEntry(A text, B field) {
        return new AbstractMap.SimpleImmutableEntry(text, field);
    }
}

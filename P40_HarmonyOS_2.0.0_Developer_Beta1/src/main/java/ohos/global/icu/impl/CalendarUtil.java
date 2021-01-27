package ohos.global.icu.impl;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public final class CalendarUtil {
    private static final String CALKEY = "calendar";
    private static final String DEFCAL = "gregorian";

    public static String getCalendarType(ULocale uLocale) {
        String keywordValue = uLocale.getKeywordValue(CALKEY);
        if (keywordValue != null) {
            return keywordValue.toLowerCase(Locale.ROOT);
        }
        ULocale createCanonical = ULocale.createCanonical(uLocale.toString());
        String keywordValue2 = createCanonical.getKeywordValue(CALKEY);
        if (keywordValue2 != null) {
            return keywordValue2;
        }
        return CalendarPreferences.INSTANCE.getCalendarTypeForRegion(ULocale.getRegionForSupplementalData(createCanonical, true));
    }

    private static final class CalendarPreferences extends UResource.Sink {
        private static final CalendarPreferences INSTANCE = new CalendarPreferences();
        Map<String, String> prefs = new TreeMap();

        CalendarPreferences() {
            try {
                UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData").getAllItemsWithFallback("calendarPreferenceData", this);
            } catch (MissingResourceException unused) {
            }
        }

        /* access modifiers changed from: package-private */
        public String getCalendarTypeForRegion(String str) {
            String str2 = this.prefs.get(str);
            return str2 == null ? CalendarUtil.DEFCAL : str2;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (value.getArray().getValue(0, value)) {
                    String string = value.getString();
                    if (!string.equals(CalendarUtil.DEFCAL)) {
                        this.prefs.put(key.toString(), string);
                    }
                }
            }
        }
    }
}

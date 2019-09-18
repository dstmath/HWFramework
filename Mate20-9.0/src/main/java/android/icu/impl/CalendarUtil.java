package android.icu.impl;

import android.icu.impl.UResource;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;

public final class CalendarUtil {
    private static final String CALKEY = "calendar";
    private static final String DEFCAL = "gregorian";

    private static final class CalendarPreferences extends UResource.Sink {
        /* access modifiers changed from: private */
        public static final CalendarPreferences INSTANCE = new CalendarPreferences();
        Map<String, String> prefs = new TreeMap();

        CalendarPreferences() {
            try {
                ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData")).getAllItemsWithFallback("calendarPreferenceData", this);
            } catch (MissingResourceException e) {
            }
        }

        /* access modifiers changed from: package-private */
        public String getCalendarTypeForRegion(String region) {
            String type = this.prefs.get(region);
            return type == null ? CalendarUtil.DEFCAL : type;
        }

        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table calendarPreferenceData = value.getTable();
            for (int i = 0; calendarPreferenceData.getKeyAndValue(i, key, value); i++) {
                if (value.getArray().getValue(0, value)) {
                    String type = value.getString();
                    if (!type.equals(CalendarUtil.DEFCAL)) {
                        this.prefs.put(key.toString(), type);
                    }
                }
            }
        }
    }

    public static String getCalendarType(ULocale loc) {
        String calType = loc.getKeywordValue(CALKEY);
        if (calType != null) {
            return calType.toLowerCase(Locale.ROOT);
        }
        ULocale canonical = ULocale.createCanonical(loc.toString());
        String calType2 = canonical.getKeywordValue(CALKEY);
        if (calType2 != null) {
            return calType2;
        }
        return CalendarPreferences.INSTANCE.getCalendarTypeForRegion(ULocale.getRegionForSupplementalData(canonical, true));
    }
}

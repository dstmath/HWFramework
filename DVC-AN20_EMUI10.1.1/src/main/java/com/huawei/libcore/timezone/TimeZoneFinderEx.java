package com.huawei.libcore.timezone;

import android.icu.util.TimeZone;
import java.util.List;
import libcore.timezone.TimeZoneFinder;

public class TimeZoneFinderEx {
    public static List<TimeZone> lookupTimeZonesByCountry(String countryIso) {
        return TimeZoneFinder.getInstance().lookupTimeZonesByCountry(countryIso);
    }

    public static String lookupDefaultTimeZoneIdByCountry(String countryIso) {
        return TimeZoneFinder.getInstance().lookupDefaultTimeZoneIdByCountry(countryIso);
    }
}

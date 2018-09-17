package java.time.format;

import android.icu.impl.ZoneMeta;
import android.icu.text.TimeZoneNames;
import android.icu.util.ULocale;
import java.util.Locale;

class ZoneName {
    ZoneName() {
    }

    public static String toZid(String zid, Locale locale) {
        TimeZoneNames tzNames = TimeZoneNames.getInstance(locale);
        if (tzNames.getAvailableMetaZoneIDs().contains(zid)) {
            ULocale uLocale = ULocale.forLocale(locale);
            String region = uLocale.getCountry();
            if (region.length() == 0) {
                region = ULocale.addLikelySubtags(uLocale).getCountry();
            }
            zid = tzNames.getReferenceZoneID(zid, region);
        }
        return toZid(zid);
    }

    public static String toZid(String zid) {
        String canonicalCldrId = ZoneMeta.getCanonicalCLDRID(zid);
        if (canonicalCldrId != null) {
            return canonicalCldrId;
        }
        return zid;
    }
}

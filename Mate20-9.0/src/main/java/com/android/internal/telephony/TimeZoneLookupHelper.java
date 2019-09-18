package com.android.internal.telephony;

import android.icu.util.TimeZone;
import android.text.TextUtils;
import java.util.Date;
import libcore.util.CountryTimeZones;
import libcore.util.TimeZoneFinder;

public class TimeZoneLookupHelper {
    private static final int MS_PER_HOUR = 3600000;
    private CountryTimeZones mLastCountryTimeZones;

    public static final class CountryResult {
        public final boolean allZonesHaveSameOffset;
        public final long whenMillis;
        public final String zoneId;

        public CountryResult(String zoneId2, boolean allZonesHaveSameOffset2, long whenMillis2) {
            this.zoneId = zoneId2;
            this.allZonesHaveSameOffset = allZonesHaveSameOffset2;
            this.whenMillis = whenMillis2;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CountryResult that = (CountryResult) o;
            if (this.allZonesHaveSameOffset == that.allZonesHaveSameOffset && this.whenMillis == that.whenMillis) {
                return this.zoneId.equals(that.zoneId);
            }
            return false;
        }

        public int hashCode() {
            return (31 * ((31 * this.zoneId.hashCode()) + (this.allZonesHaveSameOffset ? 1 : 0))) + ((int) (this.whenMillis ^ (this.whenMillis >>> 32)));
        }

        public String toString() {
            return "CountryResult{zoneId='" + this.zoneId + '\'' + ", allZonesHaveSameOffset=" + this.allZonesHaveSameOffset + ", whenMillis=" + this.whenMillis + '}';
        }
    }

    public static final class OffsetResult {
        public final boolean isOnlyMatch;
        public final String zoneId;

        public OffsetResult(String zoneId2, boolean isOnlyMatch2) {
            this.zoneId = zoneId2;
            this.isOnlyMatch = isOnlyMatch2;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            OffsetResult result = (OffsetResult) o;
            if (this.isOnlyMatch != result.isOnlyMatch) {
                return false;
            }
            return this.zoneId.equals(result.zoneId);
        }

        public int hashCode() {
            return (31 * this.zoneId.hashCode()) + (this.isOnlyMatch ? 1 : 0);
        }

        public String toString() {
            return "Result{zoneId='" + this.zoneId + '\'' + ", isOnlyMatch=" + this.isOnlyMatch + '}';
        }
    }

    public OffsetResult lookupByNitzCountry(NitzData nitzData, String isoCountryCode) {
        CountryTimeZones countryTimeZones = getCountryTimeZones(isoCountryCode);
        if (countryTimeZones == null) {
            return null;
        }
        CountryTimeZones countryTimeZones2 = countryTimeZones;
        CountryTimeZones.OffsetResult offsetResult = countryTimeZones2.lookupByOffsetWithBias(nitzData.getLocalOffsetMillis(), nitzData.isDst(), nitzData.getCurrentTimeInMillis(), TimeZone.getDefault());
        if (offsetResult == null) {
            return null;
        }
        return new OffsetResult(offsetResult.mTimeZone.getID(), offsetResult.mOneMatch);
    }

    public OffsetResult lookupByNitz(NitzData nitzData) {
        return lookupByNitzStatic(nitzData);
    }

    public CountryResult lookupByCountry(String isoCountryCode, long whenMillis) {
        CountryTimeZones countryTimeZones = getCountryTimeZones(isoCountryCode);
        if (countryTimeZones == null || countryTimeZones.getDefaultTimeZoneId() == null) {
            return null;
        }
        return new CountryResult(countryTimeZones.getDefaultTimeZoneId(), countryTimeZones.isDefaultOkForCountryTimeZoneDetection(whenMillis), whenMillis);
    }

    static java.util.TimeZone guessZoneByNitzStatic(NitzData nitzData) {
        OffsetResult result = lookupByNitzStatic(nitzData);
        if (result != null) {
            return java.util.TimeZone.getTimeZone(result.zoneId);
        }
        return null;
    }

    private static OffsetResult lookupByNitzStatic(NitzData nitzData) {
        int utcOffsetMillis = nitzData.getLocalOffsetMillis();
        boolean isDst = nitzData.isDst();
        long timeMillis = nitzData.getCurrentTimeInMillis();
        OffsetResult match = lookupByInstantOffsetDst(timeMillis, utcOffsetMillis, isDst);
        if (match == null) {
            return lookupByInstantOffsetDst(timeMillis, utcOffsetMillis, !isDst);
        }
        return match;
    }

    private static OffsetResult lookupByInstantOffsetDst(long timeMillis, int utcOffsetMillis, boolean isDst) {
        int rawOffset = utcOffsetMillis;
        if (isDst) {
            rawOffset -= MS_PER_HOUR;
        }
        String[] zones = java.util.TimeZone.getAvailableIDs(rawOffset);
        java.util.TimeZone match = null;
        Date d = new Date(timeMillis);
        boolean isOnlyMatch = true;
        int length = zones.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            java.util.TimeZone tz = java.util.TimeZone.getTimeZone(zones[i]);
            if (tz.getOffset(timeMillis) == utcOffsetMillis && tz.inDaylightTime(d) == isDst) {
                if (match != null) {
                    isOnlyMatch = false;
                    break;
                }
                match = tz;
            }
            i++;
        }
        if (match == null) {
            return null;
        }
        return new OffsetResult(match.getID(), isOnlyMatch);
    }

    public boolean countryUsesUtc(String isoCountryCode, long whenMillis) {
        boolean z = false;
        if (TextUtils.isEmpty(isoCountryCode)) {
            return false;
        }
        CountryTimeZones countryTimeZones = getCountryTimeZones(isoCountryCode);
        if (countryTimeZones != null && countryTimeZones.hasUtcZone(whenMillis)) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001e, code lost:
        return r0;
     */
    private CountryTimeZones getCountryTimeZones(String isoCountryCode) {
        synchronized (this) {
            if (this.mLastCountryTimeZones == null || !this.mLastCountryTimeZones.isForCountryCode(isoCountryCode)) {
                CountryTimeZones countryTimeZones = TimeZoneFinder.getInstance().lookupCountryTimeZones(isoCountryCode);
                if (countryTimeZones != null) {
                    this.mLastCountryTimeZones = countryTimeZones;
                }
            } else {
                CountryTimeZones countryTimeZones2 = this.mLastCountryTimeZones;
                return countryTimeZones2;
            }
        }
    }
}

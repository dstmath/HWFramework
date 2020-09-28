package com.android.internal.telephony;

import android.icu.util.TimeZone;
import android.text.TextUtils;
import java.util.Date;
import libcore.timezone.CountryTimeZones;
import libcore.timezone.TimeZoneFinder;

public class TimeZoneLookupHelper {
    private static final int MS_PER_HOUR = 3600000;
    private CountryTimeZones mLastCountryTimeZones;

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
            return (this.zoneId.hashCode() * 31) + (this.isOnlyMatch ? 1 : 0);
        }

        public String toString() {
            return "Result{zoneId='" + this.zoneId + '\'' + ", isOnlyMatch=" + this.isOnlyMatch + '}';
        }
    }

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
            long j = this.whenMillis;
            return (((this.zoneId.hashCode() * 31) + (this.allZonesHaveSameOffset ? 1 : 0)) * 31) + ((int) (j ^ (j >>> 32)));
        }

        public String toString() {
            return "CountryResult{zoneId='" + this.zoneId + '\'' + ", allZonesHaveSameOffset=" + this.allZonesHaveSameOffset + ", whenMillis=" + this.whenMillis + '}';
        }
    }

    public OffsetResult lookupByNitzCountry(NitzData nitzData, String isoCountryCode) {
        CountryTimeZones.OffsetResult offsetResult;
        CountryTimeZones countryTimeZones = getCountryTimeZones(isoCountryCode);
        if (countryTimeZones == null || (offsetResult = countryTimeZones.lookupByOffsetWithBias(nitzData.getLocalOffsetMillis(), nitzData.isDst(), nitzData.getCurrentTimeInMillis(), TimeZone.getDefault())) == null) {
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
        CountryTimeZones countryTimeZones;
        if (!TextUtils.isEmpty(isoCountryCode) && (countryTimeZones = getCountryTimeZones(isoCountryCode)) != null && countryTimeZones.hasUtcZone(whenMillis)) {
            return true;
        }
        return false;
    }

    private CountryTimeZones getCountryTimeZones(String isoCountryCode) {
        synchronized (this) {
            if (this.mLastCountryTimeZones == null || !this.mLastCountryTimeZones.isForCountryCode(isoCountryCode)) {
                CountryTimeZones countryTimeZones = TimeZoneFinder.getInstance().lookupCountryTimeZones(isoCountryCode);
                if (countryTimeZones != null) {
                    this.mLastCountryTimeZones = countryTimeZones;
                }
                return countryTimeZones;
            }
            return this.mLastCountryTimeZones;
        }
    }
}

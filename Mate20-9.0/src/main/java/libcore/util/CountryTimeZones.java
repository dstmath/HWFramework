package libcore.util;

import android.icu.impl.PatternTokenizer;
import android.icu.util.TimeZone;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class CountryTimeZones {
    private final String countryIso;
    private final String defaultTimeZoneId;
    private final boolean everUsesUtc;
    private TimeZone icuDefaultTimeZone;
    private List<TimeZone> icuTimeZones;
    private final List<TimeZoneMapping> timeZoneMappings;

    public static final class OffsetResult {
        public final boolean mOneMatch;
        public final TimeZone mTimeZone;

        public OffsetResult(TimeZone timeZone, boolean oneMatch) {
            this.mTimeZone = (TimeZone) Objects.requireNonNull(timeZone);
            this.mOneMatch = oneMatch;
        }

        public String toString() {
            return "Result{mTimeZone='" + this.mTimeZone + PatternTokenizer.SINGLE_QUOTE + ", mOneMatch=" + this.mOneMatch + '}';
        }
    }

    public static final class TimeZoneMapping {
        public final Long notUsedAfter;
        public final boolean showInPicker;
        public final String timeZoneId;

        TimeZoneMapping(String timeZoneId2, boolean showInPicker2, Long notUsedAfter2) {
            this.timeZoneId = timeZoneId2;
            this.showInPicker = showInPicker2;
            this.notUsedAfter = notUsedAfter2;
        }

        public static TimeZoneMapping createForTests(String timeZoneId2, boolean showInPicker2, Long notUsedAfter2) {
            return new TimeZoneMapping(timeZoneId2, showInPicker2, notUsedAfter2);
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TimeZoneMapping that = (TimeZoneMapping) o;
            if (this.showInPicker != that.showInPicker || !Objects.equals(this.timeZoneId, that.timeZoneId) || !Objects.equals(this.notUsedAfter, that.notUsedAfter)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.timeZoneId, Boolean.valueOf(this.showInPicker), this.notUsedAfter});
        }

        public String toString() {
            return "TimeZoneMapping{timeZoneId='" + this.timeZoneId + PatternTokenizer.SINGLE_QUOTE + ", showInPicker=" + this.showInPicker + ", notUsedAfter=" + this.notUsedAfter + '}';
        }

        public static boolean containsTimeZoneId(List<TimeZoneMapping> timeZoneMappings, String timeZoneId2) {
            for (TimeZoneMapping timeZoneMapping : timeZoneMappings) {
                if (timeZoneMapping.timeZoneId.equals(timeZoneId2)) {
                    return true;
                }
            }
            return false;
        }
    }

    private CountryTimeZones(String countryIso2, String defaultTimeZoneId2, boolean everUsesUtc2, List<TimeZoneMapping> timeZoneMappings2) {
        this.countryIso = (String) Objects.requireNonNull(countryIso2);
        this.defaultTimeZoneId = defaultTimeZoneId2;
        this.everUsesUtc = everUsesUtc2;
        this.timeZoneMappings = Collections.unmodifiableList(new ArrayList(timeZoneMappings2));
    }

    public static CountryTimeZones createValidated(String countryIso2, String defaultTimeZoneId2, boolean everUsesUtc2, List<TimeZoneMapping> timeZoneMappings2, String debugInfo) {
        HashSet<String> validTimeZoneIdsSet = new HashSet<>(Arrays.asList(ZoneInfoDB.getInstance().getAvailableIDs()));
        List<TimeZoneMapping> validCountryTimeZoneMappings = new ArrayList<>();
        for (TimeZoneMapping timeZoneMapping : timeZoneMappings2) {
            String timeZoneId = timeZoneMapping.timeZoneId;
            if (!validTimeZoneIdsSet.contains(timeZoneId)) {
                System.logW("Skipping invalid zone: " + timeZoneId + " at " + debugInfo);
            } else {
                validCountryTimeZoneMappings.add(timeZoneMapping);
            }
        }
        if (!validTimeZoneIdsSet.contains(defaultTimeZoneId2)) {
            System.logW("Invalid default time zone ID: " + defaultTimeZoneId2 + " at " + debugInfo);
            defaultTimeZoneId2 = null;
        }
        return new CountryTimeZones(normalizeCountryIso(countryIso2), defaultTimeZoneId2, everUsesUtc2, validCountryTimeZoneMappings);
    }

    public String getCountryIso() {
        return this.countryIso;
    }

    public boolean isForCountryCode(String countryIso2) {
        return this.countryIso.equals(normalizeCountryIso(countryIso2));
    }

    public synchronized TimeZone getDefaultTimeZone() {
        TimeZone defaultTimeZone;
        if (this.icuDefaultTimeZone == null) {
            if (this.defaultTimeZoneId == null) {
                defaultTimeZone = null;
            } else {
                defaultTimeZone = getValidFrozenTimeZoneOrNull(this.defaultTimeZoneId);
            }
            this.icuDefaultTimeZone = defaultTimeZone;
        }
        return this.icuDefaultTimeZone;
    }

    public String getDefaultTimeZoneId() {
        return this.defaultTimeZoneId;
    }

    public List<TimeZoneMapping> getTimeZoneMappings() {
        return this.timeZoneMappings;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CountryTimeZones that = (CountryTimeZones) o;
        if (this.everUsesUtc != that.everUsesUtc || !this.countryIso.equals(that.countryIso)) {
            return false;
        }
        if (this.defaultTimeZoneId == null ? that.defaultTimeZoneId == null : this.defaultTimeZoneId.equals(that.defaultTimeZoneId)) {
            return this.timeZoneMappings.equals(that.timeZoneMappings);
        }
        return false;
    }

    public int hashCode() {
        return (31 * ((31 * ((31 * this.countryIso.hashCode()) + (this.defaultTimeZoneId != null ? this.defaultTimeZoneId.hashCode() : 0))) + this.timeZoneMappings.hashCode())) + (this.everUsesUtc ? 1 : 0);
    }

    public synchronized List<TimeZone> getIcuTimeZones() {
        TimeZone timeZone;
        if (this.icuTimeZones == null) {
            ArrayList<TimeZone> mutableList = new ArrayList<>(this.timeZoneMappings.size());
            for (TimeZoneMapping timeZoneMapping : this.timeZoneMappings) {
                String timeZoneId = timeZoneMapping.timeZoneId;
                if (timeZoneId.equals(this.defaultTimeZoneId)) {
                    timeZone = getDefaultTimeZone();
                } else {
                    timeZone = getValidFrozenTimeZoneOrNull(timeZoneId);
                }
                if (timeZone == null) {
                    System.logW("Skipping invalid zone: " + timeZoneId);
                } else {
                    mutableList.add(timeZone);
                }
            }
            this.icuTimeZones = Collections.unmodifiableList(mutableList);
        }
        return this.icuTimeZones;
    }

    public boolean hasUtcZone(long whenMillis) {
        if (!this.everUsesUtc) {
            return false;
        }
        for (TimeZone zone : getIcuTimeZones()) {
            if (zone.getOffset(whenMillis) == 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isDefaultOkForCountryTimeZoneDetection(long whenMillis) {
        if (this.timeZoneMappings.isEmpty()) {
            return false;
        }
        if (this.timeZoneMappings.size() == 1) {
            return true;
        }
        TimeZone countryDefault = getDefaultTimeZone();
        if (countryDefault == null) {
            return false;
        }
        int countryDefaultOffset = countryDefault.getOffset(whenMillis);
        for (TimeZone candidate : getIcuTimeZones()) {
            if (candidate != countryDefault && countryDefaultOffset != candidate.getOffset(whenMillis)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public OffsetResult lookupByOffsetWithBias(int offsetMillis, boolean isDst, long whenMillis, TimeZone bias) {
        if (this.timeZoneMappings == null || this.timeZoneMappings.isEmpty()) {
            return null;
        }
        TimeZone firstMatch = null;
        boolean biasMatched = false;
        boolean oneMatch = true;
        for (TimeZone match : getIcuTimeZones()) {
            if (offsetMatchesAtTime(match, offsetMillis, isDst, whenMillis)) {
                if (firstMatch == null) {
                    firstMatch = match;
                } else {
                    oneMatch = false;
                }
                if (bias != null && match.getID().equals(bias.getID())) {
                    biasMatched = true;
                }
                if (firstMatch != null && !oneMatch) {
                    if (bias == null || biasMatched) {
                        break;
                    }
                }
            }
        }
        if (firstMatch == null) {
            return null;
        }
        return new OffsetResult(biasMatched ? bias : firstMatch, oneMatch);
    }

    private static boolean offsetMatchesAtTime(TimeZone timeZone, int offsetMillis, boolean isDst, long whenMillis) {
        int[] offsets = new int[2];
        boolean z = false;
        timeZone.getOffset(whenMillis, false, offsets);
        if (isDst != (offsets[1] != 0)) {
            return false;
        }
        if (offsetMillis == offsets[0] + offsets[1]) {
            z = true;
        }
        return z;
    }

    public OffsetResult lookupByOffsetWithBias(int offsetMillis, Integer dstOffsetMillis, long whenMillis, TimeZone bias) {
        if (this.timeZoneMappings == null || this.timeZoneMappings.isEmpty()) {
            return null;
        }
        TimeZone firstMatch = null;
        boolean biasMatched = false;
        boolean oneMatch = true;
        for (TimeZone match : getIcuTimeZones()) {
            if (offsetMatchesAtTime(match, offsetMillis, dstOffsetMillis, whenMillis)) {
                if (firstMatch == null) {
                    firstMatch = match;
                } else {
                    oneMatch = false;
                }
                if (bias != null && match.getID().equals(bias.getID())) {
                    biasMatched = true;
                }
                if (firstMatch != null && !oneMatch) {
                    if (bias == null || biasMatched) {
                        break;
                    }
                }
            }
        }
        if (firstMatch == null) {
            return null;
        }
        return new OffsetResult(biasMatched ? bias : firstMatch, oneMatch);
    }

    private static boolean offsetMatchesAtTime(TimeZone timeZone, int offsetMillis, Integer dstOffsetMillis, long whenMillis) {
        int[] offsets = new int[2];
        boolean z = false;
        timeZone.getOffset(whenMillis, false, offsets);
        if (dstOffsetMillis != null && dstOffsetMillis.intValue() != offsets[1]) {
            return false;
        }
        if (offsetMillis == offsets[0] + offsets[1]) {
            z = true;
        }
        return z;
    }

    private static TimeZone getValidFrozenTimeZoneOrNull(String timeZoneId) {
        TimeZone timeZone = TimeZone.getFrozenTimeZone(timeZoneId);
        if (timeZone.getID().equals(TimeZone.UNKNOWN_ZONE_ID)) {
            return null;
        }
        return timeZone;
    }

    private static String normalizeCountryIso(String countryIso2) {
        return countryIso2.toLowerCase(Locale.US);
    }
}

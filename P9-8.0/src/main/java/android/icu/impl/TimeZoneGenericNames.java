package android.icu.impl;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.TimeZoneFormat.TimeType;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.BasicTimeZone;
import android.icu.util.Freezable;
import android.icu.util.Output;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.TimeZoneTransition;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

public class TimeZoneGenericNames implements Serializable, Freezable<TimeZoneGenericNames> {
    private static final /* synthetic */ int[] -android-icu-impl-TimeZoneGenericNames$GenericNameTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = (TimeZoneGenericNames.class.desiredAssertionStatus() ^ 1);
    private static final long DST_CHECK_RANGE = 15897600000L;
    private static Cache GENERIC_NAMES_CACHE = new Cache();
    private static final NameType[] GENERIC_NON_LOCATION_TYPES = new NameType[]{NameType.LONG_GENERIC, NameType.SHORT_GENERIC};
    private static final long serialVersionUID = 2729910342063468417L;
    private volatile transient boolean _frozen;
    private transient ConcurrentHashMap<String, String> _genericLocationNamesMap;
    private transient ConcurrentHashMap<String, String> _genericPartialLocationNamesMap;
    private transient TextTrieMap<NameInfo> _gnamesTrie;
    private transient boolean _gnamesTrieFullyLoaded;
    private final ULocale _locale;
    private transient WeakReference<LocaleDisplayNames> _localeDisplayNamesRef;
    private transient MessageFormat[] _patternFormatters;
    private transient String _region;
    private TimeZoneNames _tznames;

    private static class Cache extends SoftCache<String, TimeZoneGenericNames, ULocale> {
        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
        }

        protected TimeZoneGenericNames createInstance(String key, ULocale data) {
            return new TimeZoneGenericNames(data, null).freeze();
        }
    }

    public static class GenericMatchInfo {
        final int matchLength;
        final GenericNameType nameType;
        final TimeType timeType;
        final String tzID;

        /* synthetic */ GenericMatchInfo(GenericNameType nameType, String tzID, int matchLength, TimeType timeType, GenericMatchInfo -this4) {
            this(nameType, tzID, matchLength, timeType);
        }

        private GenericMatchInfo(GenericNameType nameType, String tzID, int matchLength) {
            this(nameType, tzID, matchLength, TimeType.UNKNOWN);
        }

        private GenericMatchInfo(GenericNameType nameType, String tzID, int matchLength, TimeType timeType) {
            this.nameType = nameType;
            this.tzID = tzID;
            this.matchLength = matchLength;
            this.timeType = timeType;
        }

        public GenericNameType nameType() {
            return this.nameType;
        }

        public String tzID() {
            return this.tzID;
        }

        public TimeType timeType() {
            return this.timeType;
        }

        public int matchLength() {
            return this.matchLength;
        }
    }

    private static class GenericNameSearchHandler implements ResultHandler<NameInfo> {
        private Collection<GenericMatchInfo> _matches;
        private int _maxMatchLen;
        private EnumSet<GenericNameType> _types;

        GenericNameSearchHandler(EnumSet<GenericNameType> types) {
            this._types = types;
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<NameInfo> values) {
            while (values.hasNext()) {
                NameInfo info = (NameInfo) values.next();
                if (this._types == null || (this._types.contains(info.type) ^ 1) == 0) {
                    GenericMatchInfo matchInfo = new GenericMatchInfo(info.type, info.tzID, matchLength, null);
                    if (this._matches == null) {
                        this._matches = new LinkedList();
                    }
                    this._matches.add(matchInfo);
                    if (matchLength > this._maxMatchLen) {
                        this._maxMatchLen = matchLength;
                    }
                }
            }
            return true;
        }

        public Collection<GenericMatchInfo> getMatches() {
            return this._matches;
        }

        public int getMaxMatchLen() {
            return this._maxMatchLen;
        }

        public void resetResults() {
            this._matches = null;
            this._maxMatchLen = 0;
        }
    }

    public enum GenericNameType {
        LOCATION("LONG", "SHORT"),
        LONG(new String[0]),
        SHORT(new String[0]);
        
        String[] _fallbackTypeOf;

        private GenericNameType(String... fallbackTypeOf) {
            this._fallbackTypeOf = fallbackTypeOf;
        }

        public boolean isFallbackTypeOf(GenericNameType type) {
            String typeStr = type.toString();
            for (String t : this._fallbackTypeOf) {
                if (t.equals(typeStr)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class NameInfo {
        final GenericNameType type;
        final String tzID;

        NameInfo(String tzID, GenericNameType type) {
            this.tzID = tzID;
            this.type = type;
        }
    }

    public enum Pattern {
        REGION_FORMAT("regionFormat", "({0})"),
        FALLBACK_FORMAT("fallbackFormat", "{1} ({0})");
        
        String _defaultVal;
        String _key;

        private Pattern(String key, String defaultVal) {
            this._key = key;
            this._defaultVal = defaultVal;
        }

        String key() {
            return this._key;
        }

        String defaultValue() {
            return this._defaultVal;
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-TimeZoneGenericNames$GenericNameTypeSwitchesValues() {
        if (-android-icu-impl-TimeZoneGenericNames$GenericNameTypeSwitchesValues != null) {
            return -android-icu-impl-TimeZoneGenericNames$GenericNameTypeSwitchesValues;
        }
        int[] iArr = new int[GenericNameType.values().length];
        try {
            iArr[GenericNameType.LOCATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[GenericNameType.LONG.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[GenericNameType.SHORT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-icu-impl-TimeZoneGenericNames$GenericNameTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues() {
        if (-android-icu-text-TimeZoneNames$NameTypeSwitchesValues != null) {
            return -android-icu-text-TimeZoneNames$NameTypeSwitchesValues;
        }
        int[] iArr = new int[NameType.values().length];
        try {
            iArr[NameType.EXEMPLAR_LOCATION.ordinal()] = 8;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[NameType.LONG_DAYLIGHT.ordinal()] = 9;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[NameType.LONG_GENERIC.ordinal()] = 1;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[NameType.LONG_STANDARD.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[NameType.SHORT_DAYLIGHT.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[NameType.SHORT_GENERIC.ordinal()] = 3;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[NameType.SHORT_STANDARD.ordinal()] = 4;
        } catch (NoSuchFieldError e7) {
        }
        -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = iArr;
        return iArr;
    }

    public TimeZoneGenericNames(ULocale locale, TimeZoneNames tznames) {
        this._locale = locale;
        this._tznames = tznames;
        init();
    }

    private void init() {
        if (this._tznames == null) {
            this._tznames = TimeZoneNames.getInstance(this._locale);
        }
        this._genericLocationNamesMap = new ConcurrentHashMap();
        this._genericPartialLocationNamesMap = new ConcurrentHashMap();
        this._gnamesTrie = new TextTrieMap(true);
        this._gnamesTrieFullyLoaded = false;
        String tzCanonicalID = ZoneMeta.getCanonicalCLDRID(TimeZone.getDefault());
        if (tzCanonicalID != null) {
            loadStrings(tzCanonicalID);
        }
    }

    private TimeZoneGenericNames(ULocale locale) {
        this(locale, null);
    }

    public static TimeZoneGenericNames getInstance(ULocale locale) {
        return (TimeZoneGenericNames) GENERIC_NAMES_CACHE.getInstance(locale.getBaseName(), locale);
    }

    public String getDisplayName(TimeZone tz, GenericNameType type, long date) {
        String tzCanonicalID;
        switch (-getandroid-icu-impl-TimeZoneGenericNames$GenericNameTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                tzCanonicalID = ZoneMeta.getCanonicalCLDRID(tz);
                if (tzCanonicalID != null) {
                    return getGenericLocationName(tzCanonicalID);
                }
                return null;
            case 2:
            case 3:
                String name = formatGenericNonLocationName(tz, type, date);
                if (name != null) {
                    return name;
                }
                tzCanonicalID = ZoneMeta.getCanonicalCLDRID(tz);
                if (tzCanonicalID != null) {
                    return getGenericLocationName(tzCanonicalID);
                }
                return name;
            default:
                return null;
        }
    }

    public String getGenericLocationName(String canonicalTzID) {
        if (canonicalTzID == null || canonicalTzID.length() == 0) {
            return null;
        }
        String name = (String) this._genericLocationNamesMap.get(canonicalTzID);
        if (name == null) {
            Output<Boolean> isPrimary = new Output();
            String countryCode = ZoneMeta.getCanonicalCountry(canonicalTzID, isPrimary);
            if (countryCode != null) {
                if (((Boolean) isPrimary.value).booleanValue()) {
                    String country = getLocaleDisplayNames().regionDisplayName(countryCode);
                    name = formatPattern(Pattern.REGION_FORMAT, country);
                } else {
                    String city = this._tznames.getExemplarLocationName(canonicalTzID);
                    name = formatPattern(Pattern.REGION_FORMAT, city);
                }
            }
            if (name == null) {
                this._genericLocationNamesMap.putIfAbsent(canonicalTzID.intern(), "");
            } else {
                synchronized (this) {
                    canonicalTzID = canonicalTzID.intern();
                    String tmp = (String) this._genericLocationNamesMap.putIfAbsent(canonicalTzID, name.intern());
                    if (tmp == null) {
                        this._gnamesTrie.put(name, new NameInfo(canonicalTzID, GenericNameType.LOCATION));
                    } else {
                        name = tmp;
                    }
                }
            }
            return name;
        } else if (name.length() == 0) {
            return null;
        } else {
            return name;
        }
    }

    public TimeZoneGenericNames setFormatPattern(Pattern patType, String patStr) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
        if (!this._genericLocationNamesMap.isEmpty()) {
            this._genericLocationNamesMap = new ConcurrentHashMap();
        }
        if (!this._genericPartialLocationNamesMap.isEmpty()) {
            this._genericPartialLocationNamesMap = new ConcurrentHashMap();
        }
        this._gnamesTrie = null;
        this._gnamesTrieFullyLoaded = false;
        if (this._patternFormatters == null) {
            this._patternFormatters = new MessageFormat[Pattern.values().length];
        }
        this._patternFormatters[patType.ordinal()] = new MessageFormat(patStr);
        return this;
    }

    private String formatGenericNonLocationName(TimeZone tz, GenericNameType type, long date) {
        if (-assertionsDisabled || type == GenericNameType.LONG || type == GenericNameType.SHORT) {
            String tzID = ZoneMeta.getCanonicalCLDRID(tz);
            if (tzID == null) {
                return null;
            }
            NameType nameType = type == GenericNameType.LONG ? NameType.LONG_GENERIC : NameType.SHORT_GENERIC;
            String name = this._tznames.getTimeZoneDisplayName(tzID, nameType);
            if (name != null) {
                return name;
            }
            String mzID = this._tznames.getMetaZoneID(tzID, date);
            if (mzID != null) {
                boolean useStandard = false;
                int[] offsets = new int[]{0, 0};
                tz.getOffset(date, false, offsets);
                if (offsets[1] == 0) {
                    useStandard = true;
                    if (tz instanceof BasicTimeZone) {
                        BasicTimeZone btz = (BasicTimeZone) tz;
                        TimeZoneTransition before = btz.getPreviousTransition(date, true);
                        if (before == null || date - before.getTime() >= DST_CHECK_RANGE || before.getFrom().getDSTSavings() == 0) {
                            TimeZoneTransition after = btz.getNextTransition(date, false);
                            if (!(after == null || after.getTime() - date >= DST_CHECK_RANGE || after.getTo().getDSTSavings() == 0)) {
                                useStandard = false;
                            }
                        } else {
                            useStandard = false;
                        }
                    } else {
                        int[] tmpOffsets = new int[2];
                        tz.getOffset(date - DST_CHECK_RANGE, false, tmpOffsets);
                        if (tmpOffsets[1] != 0) {
                            useStandard = false;
                        } else {
                            tz.getOffset(DST_CHECK_RANGE + date, false, tmpOffsets);
                            if (tmpOffsets[1] != 0) {
                                useStandard = false;
                            }
                        }
                    }
                }
                if (useStandard) {
                    String stdName = this._tznames.getDisplayName(tzID, nameType == NameType.LONG_GENERIC ? NameType.LONG_STANDARD : NameType.SHORT_STANDARD, date);
                    if (stdName != null) {
                        name = stdName;
                        if (stdName.equalsIgnoreCase(this._tznames.getMetaZoneDisplayName(mzID, nameType))) {
                            name = null;
                        }
                    }
                }
                if (name == null) {
                    String mzName = this._tznames.getMetaZoneDisplayName(mzID, nameType);
                    if (mzName != null) {
                        String goldenID = this._tznames.getReferenceZoneID(mzID, getTargetRegion());
                        if (goldenID == null || (goldenID.equals(tzID) ^ 1) == 0) {
                            name = mzName;
                        } else {
                            int[] offsets1 = new int[]{0, 0};
                            TimeZone.getFrozenTimeZone(goldenID).getOffset((((long) offsets[0]) + date) + ((long) offsets[1]), true, offsets1);
                            if (offsets[0] == offsets1[0] && offsets[1] == offsets1[1]) {
                                name = mzName;
                            } else {
                                name = getPartialLocationName(tzID, mzID, nameType == NameType.LONG_GENERIC, mzName);
                            }
                        }
                    }
                }
            }
            return name;
        }
        throw new AssertionError();
    }

    private synchronized String formatPattern(Pattern pat, String... args) {
        int idx;
        if (this._patternFormatters == null) {
            this._patternFormatters = new MessageFormat[Pattern.values().length];
        }
        idx = pat.ordinal();
        if (this._patternFormatters[idx] == null) {
            String patText;
            try {
                patText = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, this._locale)).getStringWithFallback("zoneStrings/" + pat.key());
            } catch (MissingResourceException e) {
                patText = pat.defaultValue();
            }
            this._patternFormatters[idx] = new MessageFormat(patText);
        }
        return this._patternFormatters[idx].format(args);
    }

    private synchronized LocaleDisplayNames getLocaleDisplayNames() {
        LocaleDisplayNames locNames;
        locNames = null;
        if (this._localeDisplayNamesRef != null) {
            locNames = (LocaleDisplayNames) this._localeDisplayNamesRef.get();
        }
        if (locNames == null) {
            locNames = LocaleDisplayNames.getInstance(this._locale);
            this._localeDisplayNamesRef = new WeakReference(locNames);
        }
        return locNames;
    }

    /* JADX WARNING: Missing block: B:7:0x000b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadStrings(String tzCanonicalID) {
        if (tzCanonicalID != null) {
            if (tzCanonicalID.length() != 0) {
                getGenericLocationName(tzCanonicalID);
                for (String mzID : this._tznames.getAvailableMetaZoneIDs(tzCanonicalID)) {
                    if (!tzCanonicalID.equals(this._tznames.getReferenceZoneID(mzID, getTargetRegion()))) {
                        for (NameType genNonLocType : GENERIC_NON_LOCATION_TYPES) {
                            String mzGenName = this._tznames.getMetaZoneDisplayName(mzID, genNonLocType);
                            if (mzGenName != null) {
                                boolean z;
                                if (genNonLocType == NameType.LONG_GENERIC) {
                                    z = true;
                                } else {
                                    z = false;
                                }
                                getPartialLocationName(tzCanonicalID, mzID, z, mzGenName);
                            }
                        }
                    }
                }
            }
        }
    }

    private synchronized String getTargetRegion() {
        if (this._region == null) {
            this._region = this._locale.getCountry();
            if (this._region.length() == 0) {
                this._region = ULocale.addLikelySubtags(this._locale).getCountry();
                if (this._region.length() == 0) {
                    this._region = "001";
                }
            }
        }
        return this._region;
    }

    private String getPartialLocationName(String tzID, String mzID, boolean isLong, String mzDisplayName) {
        String key = tzID + "&" + mzID + "#" + (isLong ? "L" : "S");
        String name = (String) this._genericPartialLocationNamesMap.get(key);
        if (name != null) {
            return name;
        }
        String location;
        String countryCode = ZoneMeta.getCanonicalCountry(tzID);
        if (countryCode == null) {
            location = this._tznames.getExemplarLocationName(tzID);
            if (location == null) {
                location = tzID;
            }
        } else if (tzID.equals(this._tznames.getReferenceZoneID(mzID, countryCode))) {
            location = getLocaleDisplayNames().regionDisplayName(countryCode);
        } else {
            location = this._tznames.getExemplarLocationName(tzID);
        }
        name = formatPattern(Pattern.FALLBACK_FORMAT, location, mzDisplayName);
        synchronized (this) {
            String tmp = (String) this._genericPartialLocationNamesMap.putIfAbsent(key.intern(), name.intern());
            if (tmp == null) {
                this._gnamesTrie.put(name, new NameInfo(tzID.intern(), isLong ? GenericNameType.LONG : GenericNameType.SHORT));
            } else {
                name = tmp;
            }
        }
        return name;
    }

    public GenericMatchInfo findBestMatch(String text, int start, EnumSet<GenericNameType> genericTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        GenericMatchInfo bestMatch = null;
        Collection<MatchInfo> tznamesMatches = findTimeZoneNames(text, start, genericTypes);
        if (tznamesMatches != null) {
            MatchInfo longestMatch = null;
            for (MatchInfo match : tznamesMatches) {
                if (longestMatch == null || match.matchLength() > longestMatch.matchLength()) {
                    longestMatch = match;
                }
            }
            if (longestMatch != null) {
                bestMatch = createGenericMatchInfo(longestMatch);
                if (bestMatch.matchLength() == text.length() - start && bestMatch.timeType != TimeType.STANDARD) {
                    return bestMatch;
                }
            }
        }
        Collection<GenericMatchInfo> localMatches = findLocal(text, start, genericTypes);
        if (localMatches != null) {
            for (GenericMatchInfo match2 : localMatches) {
                if (bestMatch == null || match2.matchLength() >= bestMatch.matchLength()) {
                    bestMatch = match2;
                }
            }
        }
        return bestMatch;
    }

    public Collection<GenericMatchInfo> find(String text, int start, EnumSet<GenericNameType> genericTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        Collection<GenericMatchInfo> results = findLocal(text, start, genericTypes);
        Collection<MatchInfo> tznamesMatches = findTimeZoneNames(text, start, genericTypes);
        if (tznamesMatches != null) {
            for (MatchInfo match : tznamesMatches) {
                if (results == null) {
                    results = new LinkedList();
                }
                results.add(createGenericMatchInfo(match));
            }
        }
        return results;
    }

    private GenericMatchInfo createGenericMatchInfo(MatchInfo matchInfo) {
        GenericNameType nameType;
        TimeType timeType = TimeType.UNKNOWN;
        switch (-getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues()[matchInfo.nameType().ordinal()]) {
            case 1:
                nameType = GenericNameType.LONG;
                break;
            case 2:
                nameType = GenericNameType.LONG;
                timeType = TimeType.STANDARD;
                break;
            case 3:
                nameType = GenericNameType.SHORT;
                break;
            case 4:
                nameType = GenericNameType.SHORT;
                timeType = TimeType.STANDARD;
                break;
            default:
                throw new IllegalArgumentException("Unexpected MatchInfo name type - " + matchInfo.nameType());
        }
        String tzID = matchInfo.tzID();
        if (tzID == null) {
            String mzID = matchInfo.mzID();
            if (-assertionsDisabled || mzID != null) {
                tzID = this._tznames.getReferenceZoneID(mzID, getTargetRegion());
            } else {
                throw new AssertionError();
            }
        }
        if (-assertionsDisabled || tzID != null) {
            return new GenericMatchInfo(nameType, tzID, matchInfo.matchLength(), timeType, null);
        }
        throw new AssertionError();
    }

    private Collection<MatchInfo> findTimeZoneNames(String text, int start, EnumSet<GenericNameType> types) {
        EnumSet<NameType> nameTypes = EnumSet.noneOf(NameType.class);
        if (types.contains(GenericNameType.LONG)) {
            nameTypes.add(NameType.LONG_GENERIC);
            nameTypes.add(NameType.LONG_STANDARD);
        }
        if (types.contains(GenericNameType.SHORT)) {
            nameTypes.add(NameType.SHORT_GENERIC);
            nameTypes.add(NameType.SHORT_STANDARD);
        }
        if (nameTypes.isEmpty()) {
            return null;
        }
        return this._tznames.find(text, start, nameTypes);
    }

    private synchronized Collection<GenericMatchInfo> findLocal(String text, int start, EnumSet<GenericNameType> types) {
        ResultHandler handler = new GenericNameSearchHandler(types);
        this._gnamesTrie.find((CharSequence) text, start, handler);
        if (handler.getMaxMatchLen() == text.length() - start || this._gnamesTrieFullyLoaded) {
            return handler.getMatches();
        }
        for (String tzID : TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null)) {
            loadStrings(tzID);
        }
        this._gnamesTrieFullyLoaded = true;
        handler.resetResults();
        this._gnamesTrie.find((CharSequence) text, start, handler);
        return handler.getMatches();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    public boolean isFrozen() {
        return this._frozen;
    }

    public TimeZoneGenericNames freeze() {
        this._frozen = true;
        return this;
    }

    public TimeZoneGenericNames cloneAsThawed() {
        TimeZoneGenericNames timeZoneGenericNames = null;
        try {
            timeZoneGenericNames = (TimeZoneGenericNames) super.clone();
            timeZoneGenericNames._frozen = false;
            return timeZoneGenericNames;
        } catch (Throwable th) {
            return timeZoneGenericNames;
        }
    }
}

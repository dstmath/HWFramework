package android.icu.impl;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.impl.UResource.Key;
import android.icu.impl.UResource.Sink;
import android.icu.impl.UResource.Table;
import android.icu.impl.UResource.Value;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.TimeZone;
import android.icu.util.TimeZone.SystemTimeZoneType;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import libcore.icu.RelativeDateTimeFormatter;

public class TimeZoneNamesImpl extends TimeZoneNames {
    private static final Pattern LOC_EXCLUSION_PATTERN = Pattern.compile("Etc/.*|SystemV/.*|.*/Riyadh8[7-9]");
    private static volatile Set<String> METAZONE_IDS = null;
    private static final String MZ_PREFIX = "meta:";
    private static final MZ2TZsCache MZ_TO_TZS_CACHE = new MZ2TZsCache();
    private static final TZ2MZsCache TZ_TO_MZS_CACHE = new TZ2MZsCache();
    private static final String ZONE_STRINGS_BUNDLE = "zoneStrings";
    private static final long serialVersionUID = -2179814848495897472L;
    private transient ConcurrentHashMap<String, ZNames> _mzNamesMap;
    private transient boolean _namesFullyLoaded;
    private transient TextTrieMap<NameInfo> _namesTrie;
    private transient boolean _namesTrieFullyLoaded;
    private transient ConcurrentHashMap<String, ZNames> _tzNamesMap;
    private transient ICUResourceBundle _zoneStrings;

    private static class MZ2TZsCache extends SoftCache<String, Map<String, String>, String> {
        /* synthetic */ MZ2TZsCache(MZ2TZsCache -this0) {
            this();
        }

        private MZ2TZsCache() {
        }

        protected Map<String, String> createInstance(String key, String data) {
            try {
                UResourceBundle regionMap = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones").get("mapTimezones").get(key);
                Set<String> regions = regionMap.keySet();
                Map<String, String> map = new HashMap(regions.size());
                try {
                    for (String region : regions) {
                        map.put(region.intern(), regionMap.getString(region).intern());
                    }
                    return map;
                } catch (MissingResourceException e) {
                    Map<String, String> map2 = map;
                    return Collections.emptyMap();
                }
            } catch (MissingResourceException e2) {
                return Collections.emptyMap();
            }
        }
    }

    private static class MZMapEntry {
        private long _from;
        private String _mzID;
        private long _to;

        MZMapEntry(String mzID, long from, long to) {
            this._mzID = mzID;
            this._from = from;
            this._to = to;
        }

        String mzID() {
            return this._mzID;
        }

        long from() {
            return this._from;
        }

        long to() {
            return this._to;
        }
    }

    private static class NameInfo {
        String mzID;
        NameType type;
        String tzID;

        /* synthetic */ NameInfo(NameInfo -this0) {
            this();
        }

        private NameInfo() {
        }
    }

    private static class NameSearchHandler implements ResultHandler<NameInfo> {
        static final /* synthetic */ boolean -assertionsDisabled = (NameSearchHandler.class.desiredAssertionStatus() ^ 1);
        private Collection<MatchInfo> _matches;
        private int _maxMatchLen;
        private EnumSet<NameType> _nameTypes;

        NameSearchHandler(EnumSet<NameType> nameTypes) {
            this._nameTypes = nameTypes;
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<NameInfo> values) {
            while (values.hasNext()) {
                NameInfo ninfo = (NameInfo) values.next();
                if (this._nameTypes == null || (this._nameTypes.contains(ninfo.type) ^ 1) == 0) {
                    MatchInfo minfo;
                    if (ninfo.tzID != null) {
                        minfo = new MatchInfo(ninfo.type, ninfo.tzID, null, matchLength);
                    } else if (-assertionsDisabled || ninfo.mzID != null) {
                        minfo = new MatchInfo(ninfo.type, null, ninfo.mzID, matchLength);
                    } else {
                        throw new AssertionError();
                    }
                    if (this._matches == null) {
                        this._matches = new LinkedList();
                    }
                    this._matches.add(minfo);
                    if (matchLength > this._maxMatchLen) {
                        this._maxMatchLen = matchLength;
                    }
                }
            }
            return true;
        }

        public Collection<MatchInfo> getMatches() {
            if (this._matches == null) {
                return Collections.emptyList();
            }
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

    private static class TZ2MZsCache extends SoftCache<String, List<MZMapEntry>, String> {
        /* synthetic */ TZ2MZsCache(TZ2MZsCache -this0) {
            this();
        }

        private TZ2MZsCache() {
        }

        protected List<MZMapEntry> createInstance(String key, String data) {
            try {
                UResourceBundle zoneBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones").get("metazoneInfo").get(data.replace('/', ':'));
                List<MZMapEntry> mzMaps = new ArrayList(zoneBundle.getSize());
                int idx = 0;
                while (idx < zoneBundle.getSize()) {
                    try {
                        UResourceBundle mz = zoneBundle.get(idx);
                        String mzid = mz.getString(0);
                        String fromStr = "1970-01-01 00:00";
                        String toStr = "9999-12-31 23:59";
                        if (mz.getSize() == 3) {
                            fromStr = mz.getString(1);
                            toStr = mz.getString(2);
                        }
                        mzMaps.add(new MZMapEntry(mzid, parseDate(fromStr), parseDate(toStr)));
                        idx++;
                    } catch (MissingResourceException e) {
                        List<MZMapEntry> list = mzMaps;
                        return Collections.emptyList();
                    }
                }
                return mzMaps;
            } catch (MissingResourceException e2) {
                return Collections.emptyList();
            }
        }

        private static long parseDate(String text) {
            int idx;
            int n;
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int min = 0;
            for (idx = 0; idx <= 3; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad year");
                }
                year = (year * 10) + n;
            }
            for (idx = 5; idx <= 6; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad month");
                }
                month = (month * 10) + n;
            }
            for (idx = 8; idx <= 9; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad day");
                }
                day = (day * 10) + n;
            }
            for (idx = 11; idx <= 12; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad hour");
                }
                hour = (hour * 10) + n;
            }
            for (idx = 14; idx <= 15; idx++) {
                n = text.charAt(idx) - 48;
                if (n < 0 || n >= 10) {
                    throw new IllegalArgumentException("Bad minute");
                }
                min = (min * 10) + n;
            }
            return ((Grego.fieldsToDay(year, month - 1, day) * 86400000) + (((long) hour) * RelativeDateTimeFormatter.HOUR_IN_MILLIS)) + (((long) min) * RelativeDateTimeFormatter.MINUTE_IN_MILLIS);
        }
    }

    private static class ZNames {
        private static final /* synthetic */ int[] -android-icu-impl-TimeZoneNamesImpl$ZNames$NameTypeIndexSwitchesValues = null;
        private static final /* synthetic */ int[] -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = null;
        static final ZNames EMPTY_ZNAMES = new ZNames(null);
        private static final int EX_LOC_INDEX = NameTypeIndex.EXEMPLAR_LOCATION.ordinal();
        public static final int NUM_NAME_TYPES = 7;
        private String[] _names;
        private boolean didAddIntoTrie;

        private enum NameTypeIndex {
            EXEMPLAR_LOCATION,
            LONG_GENERIC,
            LONG_STANDARD,
            LONG_DAYLIGHT,
            SHORT_GENERIC,
            SHORT_STANDARD,
            SHORT_DAYLIGHT;
            
            static final NameTypeIndex[] values = null;

            static {
                values = values();
            }
        }

        private static /* synthetic */ int[] -getandroid-icu-impl-TimeZoneNamesImpl$ZNames$NameTypeIndexSwitchesValues() {
            if (-android-icu-impl-TimeZoneNamesImpl$ZNames$NameTypeIndexSwitchesValues != null) {
                return -android-icu-impl-TimeZoneNamesImpl$ZNames$NameTypeIndexSwitchesValues;
            }
            int[] iArr = new int[NameTypeIndex.values().length];
            try {
                iArr[NameTypeIndex.EXEMPLAR_LOCATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[NameTypeIndex.LONG_DAYLIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[NameTypeIndex.LONG_GENERIC.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[NameTypeIndex.LONG_STANDARD.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[NameTypeIndex.SHORT_DAYLIGHT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[NameTypeIndex.SHORT_GENERIC.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[NameTypeIndex.SHORT_STANDARD.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            -android-icu-impl-TimeZoneNamesImpl$ZNames$NameTypeIndexSwitchesValues = iArr;
            return iArr;
        }

        private static /* synthetic */ int[] -getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues() {
            if (-android-icu-text-TimeZoneNames$NameTypeSwitchesValues != null) {
                return -android-icu-text-TimeZoneNames$NameTypeSwitchesValues;
            }
            int[] iArr = new int[NameType.values().length];
            try {
                iArr[NameType.EXEMPLAR_LOCATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[NameType.LONG_DAYLIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[NameType.LONG_GENERIC.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[NameType.LONG_STANDARD.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[NameType.SHORT_DAYLIGHT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[NameType.SHORT_GENERIC.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[NameType.SHORT_STANDARD.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = iArr;
            return iArr;
        }

        private static int getNameTypeIndex(NameType type) {
            switch (-getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues()[type.ordinal()]) {
                case 1:
                    return NameTypeIndex.EXEMPLAR_LOCATION.ordinal();
                case 2:
                    return NameTypeIndex.LONG_DAYLIGHT.ordinal();
                case 3:
                    return NameTypeIndex.LONG_GENERIC.ordinal();
                case 4:
                    return NameTypeIndex.LONG_STANDARD.ordinal();
                case 5:
                    return NameTypeIndex.SHORT_DAYLIGHT.ordinal();
                case 6:
                    return NameTypeIndex.SHORT_GENERIC.ordinal();
                case 7:
                    return NameTypeIndex.SHORT_STANDARD.ordinal();
                default:
                    throw new AssertionError("No NameTypeIndex match for " + type);
            }
        }

        private static NameType getNameType(int index) {
            switch (-getandroid-icu-impl-TimeZoneNamesImpl$ZNames$NameTypeIndexSwitchesValues()[NameTypeIndex.values[index].ordinal()]) {
                case 1:
                    return NameType.EXEMPLAR_LOCATION;
                case 2:
                    return NameType.LONG_DAYLIGHT;
                case 3:
                    return NameType.LONG_GENERIC;
                case 4:
                    return NameType.LONG_STANDARD;
                case 5:
                    return NameType.SHORT_DAYLIGHT;
                case 6:
                    return NameType.SHORT_GENERIC;
                case 7:
                    return NameType.SHORT_STANDARD;
                default:
                    throw new AssertionError("No NameType match for " + index);
            }
        }

        protected ZNames(String[] names) {
            this._names = names;
            this.didAddIntoTrie = names == null;
        }

        public static ZNames createMetaZoneAndPutInCache(Map<String, ZNames> cache, String[] names, String mzID) {
            ZNames value;
            String key = mzID.intern();
            if (names == null) {
                value = EMPTY_ZNAMES;
            } else {
                value = new ZNames(names);
            }
            cache.put(key, value);
            return value;
        }

        public static ZNames createTimeZoneAndPutInCache(Map<String, ZNames> cache, String[] names, String tzID) {
            if (names == null) {
                names = new String[(EX_LOC_INDEX + 1)];
            }
            if (names[EX_LOC_INDEX] == null) {
                names[EX_LOC_INDEX] = TimeZoneNamesImpl.getDefaultExemplarLocationName(tzID);
            }
            String key = tzID.intern();
            ZNames value = new ZNames(names);
            cache.put(key, value);
            return value;
        }

        public String getName(NameType type) {
            int index = getNameTypeIndex(type);
            if (this._names == null || index >= this._names.length) {
                return null;
            }
            return this._names[index];
        }

        public void addAsMetaZoneIntoTrie(String mzID, TextTrieMap<NameInfo> trie) {
            addNamesIntoTrie(mzID, null, trie);
        }

        public void addAsTimeZoneIntoTrie(String tzID, TextTrieMap<NameInfo> trie) {
            addNamesIntoTrie(null, tzID, trie);
        }

        private void addNamesIntoTrie(String mzID, String tzID, TextTrieMap<NameInfo> trie) {
            if (this._names != null && !this.didAddIntoTrie) {
                this.didAddIntoTrie = true;
                for (int i = 0; i < this._names.length; i++) {
                    String name = this._names[i];
                    if (name != null) {
                        NameInfo info = new NameInfo();
                        info.mzID = mzID;
                        info.tzID = tzID;
                        info.type = getNameType(i);
                        trie.put(name, info);
                    }
                }
            }
        }
    }

    private static final class ZNamesLoader extends Sink {
        static final /* synthetic */ boolean -assertionsDisabled = (ZNamesLoader.class.desiredAssertionStatus() ^ 1);
        private static ZNamesLoader DUMMY_LOADER = new ZNamesLoader();
        private String[] names;

        /* synthetic */ ZNamesLoader(ZNamesLoader -this0) {
            this();
        }

        private ZNamesLoader() {
        }

        void loadMetaZone(ICUResourceBundle zoneStrings, String mzID) {
            loadNames(zoneStrings, TimeZoneNamesImpl.MZ_PREFIX + mzID);
        }

        void loadTimeZone(ICUResourceBundle zoneStrings, String tzID) {
            loadNames(zoneStrings, tzID.replace('/', ':'));
        }

        void loadNames(ICUResourceBundle zoneStrings, String key) {
            if (!-assertionsDisabled && zoneStrings == null) {
                throw new AssertionError();
            } else if (!-assertionsDisabled && key == null) {
                throw new AssertionError();
            } else if (-assertionsDisabled || key.length() > 0) {
                this.names = null;
                try {
                    zoneStrings.getAllItemsWithFallback(key, this);
                } catch (MissingResourceException e) {
                }
            } else {
                throw new AssertionError();
            }
        }

        private static NameTypeIndex nameTypeIndexFromKey(Key key) {
            NameTypeIndex nameTypeIndex = null;
            if (key.length() != 2) {
                return null;
            }
            char c0 = key.charAt(0);
            char c1 = key.charAt(1);
            if (c0 == 'l') {
                if (c1 == 'g') {
                    nameTypeIndex = NameTypeIndex.LONG_GENERIC;
                } else if (c1 == 's') {
                    nameTypeIndex = NameTypeIndex.LONG_STANDARD;
                } else if (c1 == 'd') {
                    nameTypeIndex = NameTypeIndex.LONG_DAYLIGHT;
                }
                return nameTypeIndex;
            } else if (c0 == 's') {
                if (c1 == 'g') {
                    nameTypeIndex = NameTypeIndex.SHORT_GENERIC;
                } else if (c1 == 's') {
                    nameTypeIndex = NameTypeIndex.SHORT_STANDARD;
                } else if (c1 == 'd') {
                    nameTypeIndex = NameTypeIndex.SHORT_DAYLIGHT;
                }
                return nameTypeIndex;
            } else if (c0 == 'e' && c1 == 'c') {
                return NameTypeIndex.EXEMPLAR_LOCATION;
            } else {
                return null;
            }
        }

        private void setNameIfEmpty(Key key, Value value) {
            if (this.names == null) {
                this.names = new String[7];
            }
            NameTypeIndex index = nameTypeIndexFromKey(key);
            if (index != null) {
                if (-assertionsDisabled || index.ordinal() < 7) {
                    if (this.names[index.ordinal()] == null) {
                        this.names[index.ordinal()] = value.getString();
                    }
                    return;
                }
                throw new AssertionError();
            }
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table namesTable = value.getTable();
            int i = 0;
            while (namesTable.getKeyAndValue(i, key, value)) {
                if (-assertionsDisabled || value.getType() == 0) {
                    setNameIfEmpty(key, value);
                    i++;
                } else {
                    throw new AssertionError();
                }
            }
        }

        private String[] getNames() {
            if (Utility.sameObjects(this.names, null)) {
                return null;
            }
            String[] result;
            int length = 0;
            for (int i = 0; i < 7; i++) {
                String name = this.names[i];
                if (name != null) {
                    if (name.equals(ICUResourceBundle.NO_INHERITANCE_MARKER)) {
                        this.names[i] = null;
                    } else {
                        length = i + 1;
                    }
                }
            }
            if (length == 7) {
                result = this.names;
            } else if (length == 0) {
                result = null;
            } else {
                result = (String[]) Arrays.copyOfRange(this.names, 0, length);
            }
            return result;
        }
    }

    private final class ZoneStringsLoader extends Sink {
        static final /* synthetic */ boolean -assertionsDisabled = (ZoneStringsLoader.class.desiredAssertionStatus() ^ 1);
        private static final int INITIAL_NUM_ZONES = 300;
        final /* synthetic */ boolean $assertionsDisabled;
        private HashMap<Key, ZNamesLoader> keyToLoader;
        private StringBuilder sb;

        /* synthetic */ ZoneStringsLoader(TimeZoneNamesImpl this$0, ZoneStringsLoader -this1) {
            this();
        }

        private ZoneStringsLoader() {
            this.keyToLoader = new HashMap(300);
            this.sb = new StringBuilder(32);
        }

        void load() {
            TimeZoneNamesImpl.this._zoneStrings.getAllItemsWithFallback("", this);
            for (Entry<Key, ZNamesLoader> entry : this.keyToLoader.entrySet()) {
                ZNamesLoader loader = (ZNamesLoader) entry.getValue();
                if (loader != ZNamesLoader.DUMMY_LOADER) {
                    Key key = (Key) entry.getKey();
                    if (isMetaZone(key)) {
                        ZNames.createMetaZoneAndPutInCache(TimeZoneNamesImpl.this._mzNamesMap, loader.getNames(), mzIDFromKey(key));
                    } else {
                        ZNames.createTimeZoneAndPutInCache(TimeZoneNamesImpl.this._tzNamesMap, loader.getNames(), tzIDFromKey(key));
                    }
                }
            }
        }

        public void put(Key key, Value value, boolean noFallback) {
            Table timeZonesTable = value.getTable();
            int j = 0;
            while (timeZonesTable.getKeyAndValue(j, key, value)) {
                if (-assertionsDisabled || !value.isNoInheritanceMarker()) {
                    if (value.getType() == 2) {
                        consumeNamesTable(key, value, noFallback);
                    }
                    j++;
                } else {
                    throw new AssertionError();
                }
            }
        }

        private void consumeNamesTable(Key key, Value value, boolean noFallback) {
            ZNamesLoader loader = (ZNamesLoader) this.keyToLoader.get(key);
            if (loader == null) {
                if (isMetaZone(key)) {
                    if (TimeZoneNamesImpl.this._mzNamesMap.containsKey(mzIDFromKey(key))) {
                        loader = ZNamesLoader.DUMMY_LOADER;
                    } else {
                        loader = new ZNamesLoader();
                    }
                } else {
                    if (TimeZoneNamesImpl.this._tzNamesMap.containsKey(tzIDFromKey(key))) {
                        loader = ZNamesLoader.DUMMY_LOADER;
                    } else {
                        loader = new ZNamesLoader();
                    }
                }
                this.keyToLoader.put(createKey(key), loader);
            }
            if (loader != ZNamesLoader.DUMMY_LOADER) {
                loader.put(key, value, noFallback);
            }
        }

        Key createKey(Key key) {
            return key.clone();
        }

        boolean isMetaZone(Key key) {
            return key.startsWith(TimeZoneNamesImpl.MZ_PREFIX);
        }

        private String mzIDFromKey(Key key) {
            this.sb.setLength(0);
            for (int i = TimeZoneNamesImpl.MZ_PREFIX.length(); i < key.length(); i++) {
                this.sb.append(key.charAt(i));
            }
            return this.sb.toString();
        }

        private String tzIDFromKey(Key key) {
            this.sb.setLength(0);
            for (int i = 0; i < key.length(); i++) {
                char c = key.charAt(i);
                if (c == ':') {
                    c = '/';
                }
                this.sb.append(c);
            }
            return this.sb.toString();
        }
    }

    public TimeZoneNamesImpl(ULocale locale) {
        initialize(locale);
    }

    public Set<String> getAvailableMetaZoneIDs() {
        return _getAvailableMetaZoneIDs();
    }

    static Set<String> _getAvailableMetaZoneIDs() {
        if (METAZONE_IDS == null) {
            synchronized (TimeZoneNamesImpl.class) {
                if (METAZONE_IDS == null) {
                    METAZONE_IDS = Collections.unmodifiableSet(UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metaZones").get("mapTimezones").keySet());
                }
            }
        }
        return METAZONE_IDS;
    }

    public Set<String> getAvailableMetaZoneIDs(String tzID) {
        return _getAvailableMetaZoneIDs(tzID);
    }

    static Set<String> _getAvailableMetaZoneIDs(String tzID) {
        if (tzID == null || tzID.length() == 0) {
            return Collections.emptySet();
        }
        List<MZMapEntry> maps = (List) TZ_TO_MZS_CACHE.getInstance(tzID, tzID);
        if (maps.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> mzIDs = new HashSet(maps.size());
        for (MZMapEntry map : maps) {
            mzIDs.add(map.mzID());
        }
        return Collections.unmodifiableSet(mzIDs);
    }

    public String getMetaZoneID(String tzID, long date) {
        return _getMetaZoneID(tzID, date);
    }

    static String _getMetaZoneID(String tzID, long date) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        String mzID = null;
        for (MZMapEntry map : (List) TZ_TO_MZS_CACHE.getInstance(tzID, tzID)) {
            if (date >= map.from() && date < map.to()) {
                mzID = map.mzID();
                break;
            }
        }
        return mzID;
    }

    public String getReferenceZoneID(String mzID, String region) {
        return _getReferenceZoneID(mzID, region);
    }

    static String _getReferenceZoneID(String mzID, String region) {
        if (mzID == null || mzID.length() == 0) {
            return null;
        }
        String str = null;
        Map<String, String> regionTzMap = (Map) MZ_TO_TZS_CACHE.getInstance(mzID, mzID);
        if (!regionTzMap.isEmpty()) {
            str = (String) regionTzMap.get(region);
            if (str == null) {
                str = (String) regionTzMap.get("001");
            }
        }
        return str;
    }

    public String getMetaZoneDisplayName(String mzID, NameType type) {
        if (mzID == null || mzID.length() == 0) {
            return null;
        }
        return loadMetaZoneNames(mzID).getName(type);
    }

    public String getTimeZoneDisplayName(String tzID, NameType type) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        return loadTimeZoneNames(tzID).getName(type);
    }

    public String getExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0) {
            return null;
        }
        return loadTimeZoneNames(tzID).getName(NameType.EXEMPLAR_LOCATION);
    }

    public synchronized Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> nameTypes) {
        if (text != null) {
            if (text.length() != 0 && start >= 0) {
                if (start < text.length()) {
                    NameSearchHandler handler = new NameSearchHandler(nameTypes);
                    Collection<MatchInfo> matches = doFind(handler, text, start);
                    if (matches != null) {
                        return matches;
                    }
                    addAllNamesIntoTrie();
                    matches = doFind(handler, text, start);
                    if (matches != null) {
                        return matches;
                    }
                    internalLoadAllDisplayNames();
                    for (String tzID : TimeZone.getAvailableIDs(SystemTimeZoneType.CANONICAL, null, null)) {
                        if (!this._tzNamesMap.containsKey(tzID)) {
                            ZNames.createTimeZoneAndPutInCache(this._tzNamesMap, null, tzID);
                        }
                    }
                    addAllNamesIntoTrie();
                    this._namesTrieFullyLoaded = true;
                    return doFind(handler, text, start);
                }
            }
        }
        throw new IllegalArgumentException("bad input text or range");
    }

    private Collection<MatchInfo> doFind(NameSearchHandler handler, CharSequence text, int start) {
        handler.resetResults();
        this._namesTrie.find(text, start, (ResultHandler) handler);
        if (handler.getMaxMatchLen() == text.length() - start || this._namesTrieFullyLoaded) {
            return handler.getMatches();
        }
        return null;
    }

    public synchronized void loadAllDisplayNames() {
        internalLoadAllDisplayNames();
    }

    public void getDisplayNames(String tzID, NameType[] types, long date, String[] dest, int destOffset) {
        if (tzID != null && tzID.length() != 0) {
            ZNames tzNames = loadTimeZoneNames(tzID);
            ZNames mzNames = null;
            for (int i = 0; i < types.length; i++) {
                NameType type = types[i];
                String name = tzNames.getName(type);
                if (name == null) {
                    if (mzNames == null) {
                        String mzID = getMetaZoneID(tzID, date);
                        if (mzID == null || mzID.length() == 0) {
                            mzNames = ZNames.EMPTY_ZNAMES;
                        } else {
                            mzNames = loadMetaZoneNames(mzID);
                        }
                    }
                    name = mzNames.getName(type);
                }
                dest[destOffset + i] = name;
            }
        }
    }

    private void internalLoadAllDisplayNames() {
        if (!this._namesFullyLoaded) {
            this._namesFullyLoaded = true;
            new ZoneStringsLoader(this, null).load();
        }
    }

    private void addAllNamesIntoTrie() {
        for (Entry<String, ZNames> entry : this._tzNamesMap.entrySet()) {
            ((ZNames) entry.getValue()).addAsTimeZoneIntoTrie((String) entry.getKey(), this._namesTrie);
        }
        for (Entry<String, ZNames> entry2 : this._mzNamesMap.entrySet()) {
            ((ZNames) entry2.getValue()).addAsMetaZoneIntoTrie((String) entry2.getKey(), this._namesTrie);
        }
    }

    private void initialize(ULocale locale) {
        this._zoneStrings = (ICUResourceBundle) ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, locale)).get(ZONE_STRINGS_BUNDLE);
        this._tzNamesMap = new ConcurrentHashMap();
        this._mzNamesMap = new ConcurrentHashMap();
        this._namesFullyLoaded = false;
        this._namesTrie = new TextTrieMap(true);
        this._namesTrieFullyLoaded = false;
        String tzCanonicalID = ZoneMeta.getCanonicalCLDRID(TimeZone.getDefault());
        if (tzCanonicalID != null) {
            loadStrings(tzCanonicalID);
        }
    }

    /* JADX WARNING: Missing block: B:6:0x000a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void loadStrings(String tzCanonicalID) {
        if (tzCanonicalID != null) {
            if (tzCanonicalID.length() != 0) {
                loadTimeZoneNames(tzCanonicalID);
                for (String mzID : getAvailableMetaZoneIDs(tzCanonicalID)) {
                    loadMetaZoneNames(mzID);
                }
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(this._zoneStrings.getULocale());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        initialize((ULocale) in.readObject());
    }

    private synchronized ZNames loadMetaZoneNames(String mzID) {
        ZNames mznames;
        mznames = (ZNames) this._mzNamesMap.get(mzID);
        if (mznames == null) {
            ZNamesLoader loader = new ZNamesLoader();
            loader.loadMetaZone(this._zoneStrings, mzID);
            mznames = ZNames.createMetaZoneAndPutInCache(this._mzNamesMap, loader.getNames(), mzID);
        }
        return mznames;
    }

    private synchronized ZNames loadTimeZoneNames(String tzID) {
        ZNames tznames;
        tznames = (ZNames) this._tzNamesMap.get(tzID);
        if (tznames == null) {
            ZNamesLoader loader = new ZNamesLoader();
            loader.loadTimeZone(this._zoneStrings, tzID);
            tznames = ZNames.createTimeZoneAndPutInCache(this._tzNamesMap, loader.getNames(), tzID);
        }
        return tznames;
    }

    public static String getDefaultExemplarLocationName(String tzID) {
        if (tzID == null || tzID.length() == 0 || LOC_EXCLUSION_PATTERN.matcher(tzID).matches()) {
            return null;
        }
        String location = null;
        int sep = tzID.lastIndexOf(47);
        if (sep > 0 && sep + 1 < tzID.length()) {
            location = tzID.substring(sep + 1).replace('_', ' ');
        }
        return location;
    }
}

package android.icu.impl;

import android.icu.impl.TextTrieMap;
import android.icu.text.TimeZoneNames;
import android.icu.util.ULocale;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TZDBTimeZoneNames extends TimeZoneNames {
    private static final ConcurrentHashMap<String, TZDBNames> TZDB_NAMES_MAP = new ConcurrentHashMap<>();
    private static volatile TextTrieMap<TZDBNameInfo> TZDB_NAMES_TRIE = null;
    private static final ICUResourceBundle ZONESTRINGS = ((ICUResourceBundle) ICUResourceBundle.getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, "tzdbNames").get("zoneStrings"));
    private static final long serialVersionUID = 1;
    private ULocale _locale;
    private volatile transient String _region;

    private static class TZDBNameInfo {
        final boolean ambiguousType;
        final String mzID;
        final String[] parseRegions;
        final TimeZoneNames.NameType type;

        TZDBNameInfo(String mzID2, TimeZoneNames.NameType type2, boolean ambiguousType2, String[] parseRegions2) {
            this.mzID = mzID2;
            this.type = type2;
            this.ambiguousType = ambiguousType2;
            this.parseRegions = parseRegions2;
        }
    }

    private static class TZDBNameSearchHandler implements TextTrieMap.ResultHandler<TZDBNameInfo> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private Collection<TimeZoneNames.MatchInfo> _matches;
        private EnumSet<TimeZoneNames.NameType> _nameTypes;
        private String _region;

        static {
            Class<TZDBTimeZoneNames> cls = TZDBTimeZoneNames.class;
        }

        TZDBNameSearchHandler(EnumSet<TimeZoneNames.NameType> nameTypes, String region) {
            this._nameTypes = nameTypes;
            this._region = region;
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<TZDBNameInfo> values) {
            TZDBNameInfo match = null;
            TZDBNameInfo defaultRegionMatch = null;
            while (values.hasNext()) {
                TZDBNameInfo ninfo = values.next();
                if (this._nameTypes == null || this._nameTypes.contains(ninfo.type)) {
                    if (ninfo.parseRegions != null) {
                        boolean matchRegion = false;
                        String[] strArr = ninfo.parseRegions;
                        int length = strArr.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            }
                            if (this._region.equals(strArr[i])) {
                                match = ninfo;
                                matchRegion = true;
                                break;
                            }
                            i++;
                        }
                        if (matchRegion) {
                            break;
                        } else if (match == null) {
                            match = ninfo;
                        }
                    } else if (defaultRegionMatch == null) {
                        defaultRegionMatch = ninfo;
                        match = ninfo;
                    }
                }
            }
            if (match != null) {
                TimeZoneNames.NameType ntype = match.type;
                if (match.ambiguousType && ((ntype == TimeZoneNames.NameType.SHORT_STANDARD || ntype == TimeZoneNames.NameType.SHORT_DAYLIGHT) && this._nameTypes.contains(TimeZoneNames.NameType.SHORT_STANDARD) && this._nameTypes.contains(TimeZoneNames.NameType.SHORT_DAYLIGHT))) {
                    ntype = TimeZoneNames.NameType.SHORT_GENERIC;
                }
                TimeZoneNames.MatchInfo minfo = new TimeZoneNames.MatchInfo(ntype, null, match.mzID, matchLength);
                if (this._matches == null) {
                    this._matches = new LinkedList();
                }
                this._matches.add(minfo);
            }
            return true;
        }

        public Collection<TimeZoneNames.MatchInfo> getMatches() {
            if (this._matches == null) {
                return Collections.emptyList();
            }
            return this._matches;
        }
    }

    private static class TZDBNames {
        public static final TZDBNames EMPTY_TZDBNAMES = new TZDBNames(null, null);
        private static final String[] KEYS = {"ss", "sd"};
        private String[] _names;
        private String[] _parseRegions;

        private TZDBNames(String[] names, String[] parseRegions) {
            this._names = names;
            this._parseRegions = parseRegions;
        }

        static TZDBNames getInstance(ICUResourceBundle zoneStrings, String key) {
            if (zoneStrings == null || key == null || key.length() == 0) {
                return EMPTY_TZDBNAMES;
            }
            String[] parseRegions = null;
            try {
                ICUResourceBundle table = (ICUResourceBundle) zoneStrings.get(key);
                String[] names = new String[KEYS.length];
                boolean isEmpty = true;
                for (int i = 0; i < names.length; i++) {
                    try {
                        names[i] = table.getString(KEYS[i]);
                        isEmpty = false;
                    } catch (MissingResourceException e) {
                        names[i] = null;
                    }
                }
                if (isEmpty) {
                    return EMPTY_TZDBNAMES;
                }
                try {
                    ICUResourceBundle regionsRes = (ICUResourceBundle) table.get("parseRegions");
                    if (regionsRes.getType() == 0) {
                        parseRegions = new String[]{regionsRes.getString()};
                    } else if (regionsRes.getType() == 8) {
                        parseRegions = regionsRes.getStringArray();
                    }
                } catch (MissingResourceException e2) {
                }
                return new TZDBNames(names, parseRegions);
            } catch (MissingResourceException e3) {
                return EMPTY_TZDBNAMES;
            }
        }

        /* access modifiers changed from: package-private */
        public String getName(TimeZoneNames.NameType type) {
            if (this._names == null) {
                return null;
            }
            String name = null;
            switch (type) {
                case SHORT_STANDARD:
                    name = this._names[0];
                    break;
                case SHORT_DAYLIGHT:
                    name = this._names[1];
                    break;
            }
            return name;
        }

        /* access modifiers changed from: package-private */
        public String[] getParseRegions() {
            return this._parseRegions;
        }
    }

    public TZDBTimeZoneNames(ULocale loc) {
        this._locale = loc;
    }

    public Set<String> getAvailableMetaZoneIDs() {
        return TimeZoneNamesImpl._getAvailableMetaZoneIDs();
    }

    public Set<String> getAvailableMetaZoneIDs(String tzID) {
        return TimeZoneNamesImpl._getAvailableMetaZoneIDs(tzID);
    }

    public String getMetaZoneID(String tzID, long date) {
        return TimeZoneNamesImpl._getMetaZoneID(tzID, date);
    }

    public String getReferenceZoneID(String mzID, String region) {
        return TimeZoneNamesImpl._getReferenceZoneID(mzID, region);
    }

    public String getMetaZoneDisplayName(String mzID, TimeZoneNames.NameType type) {
        if (mzID == null || mzID.length() == 0 || (type != TimeZoneNames.NameType.SHORT_STANDARD && type != TimeZoneNames.NameType.SHORT_DAYLIGHT)) {
            return null;
        }
        return getMetaZoneNames(mzID).getName(type);
    }

    public String getTimeZoneDisplayName(String tzID, TimeZoneNames.NameType type) {
        return null;
    }

    public Collection<TimeZoneNames.MatchInfo> find(CharSequence text, int start, EnumSet<TimeZoneNames.NameType> nameTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        prepareFind();
        TZDBNameSearchHandler handler = new TZDBNameSearchHandler(nameTypes, getTargetRegion());
        TZDB_NAMES_TRIE.find(text, start, handler);
        return handler.getMatches();
    }

    private static TZDBNames getMetaZoneNames(String mzID) {
        TZDBNames names = TZDB_NAMES_MAP.get(mzID);
        if (names != null) {
            return names;
        }
        ICUResourceBundle iCUResourceBundle = ZONESTRINGS;
        TZDBNames names2 = TZDBNames.getInstance(iCUResourceBundle, "meta:" + mzID);
        TZDBNames tmpNames = TZDB_NAMES_MAP.putIfAbsent(mzID.intern(), names2);
        return tmpNames == null ? names2 : tmpNames;
    }

    private static void prepareFind() {
        if (TZDB_NAMES_TRIE == null) {
            synchronized (TZDBTimeZoneNames.class) {
                if (TZDB_NAMES_TRIE == null) {
                    TextTrieMap<TZDBNameInfo> trie = new TextTrieMap<>(true);
                    for (String mzID : TimeZoneNamesImpl._getAvailableMetaZoneIDs()) {
                        TZDBNames names = getMetaZoneNames(mzID);
                        String std = names.getName(TimeZoneNames.NameType.SHORT_STANDARD);
                        String dst = names.getName(TimeZoneNames.NameType.SHORT_DAYLIGHT);
                        if (std != null || dst != null) {
                            String[] parseRegions = names.getParseRegions();
                            String mzID2 = mzID.intern();
                            boolean ambiguousType = (std == null || dst == null || !std.equals(dst)) ? false : true;
                            if (std != null) {
                                trie.put(std, new TZDBNameInfo(mzID2, TimeZoneNames.NameType.SHORT_STANDARD, ambiguousType, parseRegions));
                            }
                            if (dst != null) {
                                trie.put(dst, new TZDBNameInfo(mzID2, TimeZoneNames.NameType.SHORT_DAYLIGHT, ambiguousType, parseRegions));
                            }
                        }
                    }
                    TZDB_NAMES_TRIE = trie;
                }
            }
        }
    }

    private String getTargetRegion() {
        if (this._region == null) {
            String region = this._locale.getCountry();
            if (region.length() == 0) {
                region = ULocale.addLikelySubtags(this._locale).getCountry();
                if (region.length() == 0) {
                    region = "001";
                }
            }
            this._region = region;
        }
        return this._region;
    }
}

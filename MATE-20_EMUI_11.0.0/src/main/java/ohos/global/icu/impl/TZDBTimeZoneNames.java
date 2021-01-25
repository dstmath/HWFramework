package ohos.global.icu.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import ohos.global.icu.impl.TextTrieMap;
import ohos.global.icu.text.TimeZoneNames;
import ohos.global.icu.util.ULocale;

public class TZDBTimeZoneNames extends TimeZoneNames {
    private static final ConcurrentHashMap<String, TZDBNames> TZDB_NAMES_MAP = new ConcurrentHashMap<>();
    private static volatile TextTrieMap<TZDBNameInfo> TZDB_NAMES_TRIE = null;
    private static final ICUResourceBundle ZONESTRINGS = ICUResourceBundle.getBundleInstance(ICUData.ICU_ZONE_BASE_NAME, "tzdbNames").get("zoneStrings");
    private static final long serialVersionUID = 1;
    private ULocale _locale;
    private volatile transient String _region;

    @Override // ohos.global.icu.text.TimeZoneNames
    public String getTimeZoneDisplayName(String str, TimeZoneNames.NameType nameType) {
        return null;
    }

    public TZDBTimeZoneNames(ULocale uLocale) {
        this._locale = uLocale;
    }

    @Override // ohos.global.icu.text.TimeZoneNames
    public Set<String> getAvailableMetaZoneIDs() {
        return TimeZoneNamesImpl._getAvailableMetaZoneIDs();
    }

    @Override // ohos.global.icu.text.TimeZoneNames
    public Set<String> getAvailableMetaZoneIDs(String str) {
        return TimeZoneNamesImpl._getAvailableMetaZoneIDs(str);
    }

    @Override // ohos.global.icu.text.TimeZoneNames
    public String getMetaZoneID(String str, long j) {
        return TimeZoneNamesImpl._getMetaZoneID(str, j);
    }

    @Override // ohos.global.icu.text.TimeZoneNames
    public String getReferenceZoneID(String str, String str2) {
        return TimeZoneNamesImpl._getReferenceZoneID(str, str2);
    }

    @Override // ohos.global.icu.text.TimeZoneNames
    public String getMetaZoneDisplayName(String str, TimeZoneNames.NameType nameType) {
        if (str == null || str.length() == 0) {
            return null;
        }
        if (nameType == TimeZoneNames.NameType.SHORT_STANDARD || nameType == TimeZoneNames.NameType.SHORT_DAYLIGHT) {
            return getMetaZoneNames(str).getName(nameType);
        }
        return null;
    }

    @Override // ohos.global.icu.text.TimeZoneNames
    public Collection<TimeZoneNames.MatchInfo> find(CharSequence charSequence, int i, EnumSet<TimeZoneNames.NameType> enumSet) {
        if (charSequence == null || charSequence.length() == 0 || i < 0 || i >= charSequence.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        prepareFind();
        TZDBNameSearchHandler tZDBNameSearchHandler = new TZDBNameSearchHandler(enumSet, getTargetRegion());
        TZDB_NAMES_TRIE.find(charSequence, i, tZDBNameSearchHandler);
        return tZDBNameSearchHandler.getMatches();
    }

    /* access modifiers changed from: private */
    public static class TZDBNames {
        public static final TZDBNames EMPTY_TZDBNAMES = new TZDBNames(null, null);
        private static final String[] KEYS = {"ss", "sd"};
        private String[] _names;
        private String[] _parseRegions;

        private TZDBNames(String[] strArr, String[] strArr2) {
            this._names = strArr;
            this._parseRegions = strArr2;
        }

        static TZDBNames getInstance(ICUResourceBundle iCUResourceBundle, String str) {
            String[] strArr;
            if (iCUResourceBundle == null || str == null || str.length() == 0) {
                return EMPTY_TZDBNAMES;
            }
            try {
                ICUResourceBundle iCUResourceBundle2 = iCUResourceBundle.get(str);
                String[] strArr2 = new String[KEYS.length];
                int i = 0;
                boolean z = true;
                while (true) {
                    strArr = null;
                    if (i >= strArr2.length) {
                        break;
                    }
                    try {
                        strArr2[i] = iCUResourceBundle2.getString(KEYS[i]);
                        z = false;
                    } catch (MissingResourceException unused) {
                        strArr2[i] = null;
                    }
                    i++;
                }
                if (z) {
                    return EMPTY_TZDBNAMES;
                }
                try {
                    ICUResourceBundle iCUResourceBundle3 = iCUResourceBundle2.get("parseRegions");
                    if (iCUResourceBundle3.getType() == 0) {
                        strArr = new String[]{iCUResourceBundle3.getString()};
                    } else if (iCUResourceBundle3.getType() == 8) {
                        strArr = iCUResourceBundle3.getStringArray();
                    }
                } catch (MissingResourceException unused2) {
                }
                return new TZDBNames(strArr2, strArr);
            } catch (MissingResourceException unused3) {
                return EMPTY_TZDBNAMES;
            }
        }

        /* access modifiers changed from: package-private */
        public String getName(TimeZoneNames.NameType nameType) {
            if (this._names == null) {
                return null;
            }
            int i = AnonymousClass1.$SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[nameType.ordinal()];
            if (i == 1) {
                return this._names[0];
            }
            if (i != 2) {
                return null;
            }
            return this._names[1];
        }

        /* access modifiers changed from: package-private */
        public String[] getParseRegions() {
            return this._parseRegions;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.TZDBTimeZoneNames$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType = new int[TimeZoneNames.NameType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[TimeZoneNames.NameType.SHORT_STANDARD.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$text$TimeZoneNames$NameType[TimeZoneNames.NameType.SHORT_DAYLIGHT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public static class TZDBNameInfo {
        final boolean ambiguousType;
        final String mzID;
        final String[] parseRegions;
        final TimeZoneNames.NameType type;

        TZDBNameInfo(String str, TimeZoneNames.NameType nameType, boolean z, String[] strArr) {
            this.mzID = str;
            this.type = nameType;
            this.ambiguousType = z;
            this.parseRegions = strArr;
        }
    }

    private static class TZDBNameSearchHandler implements TextTrieMap.ResultHandler<TZDBNameInfo> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private Collection<TimeZoneNames.MatchInfo> _matches;
        private EnumSet<TimeZoneNames.NameType> _nameTypes;
        private String _region;

        TZDBNameSearchHandler(EnumSet<TimeZoneNames.NameType> enumSet, String str) {
            this._nameTypes = enumSet;
            this._region = str;
        }

        @Override // ohos.global.icu.impl.TextTrieMap.ResultHandler
        public boolean handlePrefixMatch(int i, Iterator<TZDBNameInfo> it) {
            TZDBNameInfo tZDBNameInfo;
            TZDBNameInfo tZDBNameInfo2 = null;
            loop0:
            while (true) {
                tZDBNameInfo = tZDBNameInfo2;
                while (it.hasNext()) {
                    TZDBNameInfo next = it.next();
                    EnumSet<TimeZoneNames.NameType> enumSet = this._nameTypes;
                    if (enumSet == null || enumSet.contains(next.type)) {
                        if (next.parseRegions != null) {
                            String[] strArr = next.parseRegions;
                            int length = strArr.length;
                            boolean z = false;
                            int i2 = 0;
                            while (true) {
                                if (i2 >= length) {
                                    break;
                                }
                                if (this._region.equals(strArr[i2])) {
                                    tZDBNameInfo = next;
                                    z = true;
                                    break;
                                }
                                i2++;
                            }
                            if (z) {
                                break loop0;
                            } else if (tZDBNameInfo == null) {
                                tZDBNameInfo = next;
                            }
                        } else if (tZDBNameInfo2 == null) {
                            tZDBNameInfo2 = next;
                        }
                    }
                }
                break loop0;
            }
            if (tZDBNameInfo != null) {
                TimeZoneNames.NameType nameType = tZDBNameInfo.type;
                if (tZDBNameInfo.ambiguousType && ((nameType == TimeZoneNames.NameType.SHORT_STANDARD || nameType == TimeZoneNames.NameType.SHORT_DAYLIGHT) && this._nameTypes.contains(TimeZoneNames.NameType.SHORT_STANDARD) && this._nameTypes.contains(TimeZoneNames.NameType.SHORT_DAYLIGHT))) {
                    nameType = TimeZoneNames.NameType.SHORT_GENERIC;
                }
                TimeZoneNames.MatchInfo matchInfo = new TimeZoneNames.MatchInfo(nameType, null, tZDBNameInfo.mzID, i);
                if (this._matches == null) {
                    this._matches = new LinkedList();
                }
                this._matches.add(matchInfo);
            }
            return true;
        }

        public Collection<TimeZoneNames.MatchInfo> getMatches() {
            Collection<TimeZoneNames.MatchInfo> collection = this._matches;
            return collection == null ? Collections.emptyList() : collection;
        }
    }

    private static TZDBNames getMetaZoneNames(String str) {
        TZDBNames tZDBNames = TZDB_NAMES_MAP.get(str);
        if (tZDBNames != null) {
            return tZDBNames;
        }
        ICUResourceBundle iCUResourceBundle = ZONESTRINGS;
        TZDBNames instance = TZDBNames.getInstance(iCUResourceBundle, "meta:" + str);
        TZDBNames putIfAbsent = TZDB_NAMES_MAP.putIfAbsent(str.intern(), instance);
        return putIfAbsent == null ? instance : putIfAbsent;
    }

    private static void prepareFind() {
        if (TZDB_NAMES_TRIE == null) {
            synchronized (TZDBTimeZoneNames.class) {
                if (TZDB_NAMES_TRIE == null) {
                    TextTrieMap<TZDBNameInfo> textTrieMap = new TextTrieMap<>(true);
                    for (String str : TimeZoneNamesImpl._getAvailableMetaZoneIDs()) {
                        TZDBNames metaZoneNames = getMetaZoneNames(str);
                        String name = metaZoneNames.getName(TimeZoneNames.NameType.SHORT_STANDARD);
                        String name2 = metaZoneNames.getName(TimeZoneNames.NameType.SHORT_DAYLIGHT);
                        if (name != null || name2 != null) {
                            String[] parseRegions = metaZoneNames.getParseRegions();
                            String intern = str.intern();
                            boolean z = (name == null || name2 == null || !name.equals(name2)) ? false : true;
                            if (name != null) {
                                textTrieMap.put(name, new TZDBNameInfo(intern, TimeZoneNames.NameType.SHORT_STANDARD, z, parseRegions));
                            }
                            if (name2 != null) {
                                textTrieMap.put(name2, new TZDBNameInfo(intern, TimeZoneNames.NameType.SHORT_DAYLIGHT, z, parseRegions));
                            }
                        }
                    }
                    TZDB_NAMES_TRIE = textTrieMap;
                }
            }
        }
    }

    private String getTargetRegion() {
        if (this._region == null) {
            String country = this._locale.getCountry();
            if (country.length() == 0) {
                country = ULocale.addLikelySubtags(this._locale).getCountry();
                if (country.length() == 0) {
                    country = "001";
                }
            }
            this._region = country;
        }
        return this._region;
    }
}

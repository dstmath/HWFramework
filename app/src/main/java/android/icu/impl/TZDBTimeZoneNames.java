package android.icu.impl;

import android.icu.impl.TextTrieMap.ResultHandler;
import android.icu.text.TimeZoneNames;
import android.icu.text.TimeZoneNames.MatchInfo;
import android.icu.text.TimeZoneNames.NameType;
import android.icu.util.ULocale;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.w3c.dom.traversal.NodeFilter;

public class TZDBTimeZoneNames extends TimeZoneNames {
    private static final ConcurrentHashMap<String, TZDBNames> TZDB_NAMES_MAP = null;
    private static volatile TextTrieMap<TZDBNameInfo> TZDB_NAMES_TRIE = null;
    private static final ICUResourceBundle ZONESTRINGS = null;
    private static final long serialVersionUID = 1;
    private ULocale _locale;
    private volatile transient String _region;

    private static class TZDBNameInfo {
        boolean ambiguousType;
        String mzID;
        String[] parseRegions;
        NameType type;

        private TZDBNameInfo() {
        }
    }

    private static class TZDBNameSearchHandler implements ResultHandler<TZDBNameInfo> {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private Collection<MatchInfo> _matches;
        private EnumSet<NameType> _nameTypes;
        private String _region;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.TZDBTimeZoneNames.TZDBNameSearchHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.TZDBTimeZoneNames.TZDBNameSearchHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TZDBTimeZoneNames.TZDBNameSearchHandler.<clinit>():void");
        }

        TZDBNameSearchHandler(EnumSet<NameType> nameTypes, String region) {
            this._nameTypes = nameTypes;
            if (!-assertionsDisabled) {
                if ((region != null ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            this._region = region;
        }

        public boolean handlePrefixMatch(int matchLength, Iterator<TZDBNameInfo> values) {
            TZDBNameInfo match = null;
            TZDBNameInfo tZDBNameInfo = null;
            while (values.hasNext()) {
                TZDBNameInfo ninfo = (TZDBNameInfo) values.next();
                if (this._nameTypes == null || this._nameTypes.contains(ninfo.type)) {
                    if (ninfo.parseRegions != null) {
                        boolean matchRegion = false;
                        for (String region : ninfo.parseRegions) {
                            if (this._region.equals(region)) {
                                match = ninfo;
                                matchRegion = true;
                                break;
                            }
                        }
                        if (matchRegion) {
                            break;
                        } else if (match == null) {
                            match = ninfo;
                        }
                    } else if (tZDBNameInfo == null) {
                        tZDBNameInfo = ninfo;
                        match = ninfo;
                    }
                }
            }
            if (match != null) {
                NameType ntype = match.type;
                if (match.ambiguousType && ((ntype == NameType.SHORT_STANDARD || ntype == NameType.SHORT_DAYLIGHT) && this._nameTypes.contains(NameType.SHORT_STANDARD) && this._nameTypes.contains(NameType.SHORT_DAYLIGHT))) {
                    ntype = NameType.SHORT_GENERIC;
                }
                MatchInfo minfo = new MatchInfo(ntype, null, match.mzID, matchLength);
                if (this._matches == null) {
                    this._matches = new LinkedList();
                }
                this._matches.add(minfo);
            }
            return true;
        }

        public Collection<MatchInfo> getMatches() {
            if (this._matches == null) {
                return Collections.emptyList();
            }
            return this._matches;
        }
    }

    private static class TZDBNames {
        private static final /* synthetic */ int[] -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = null;
        public static final TZDBNames EMPTY_TZDBNAMES = null;
        private static final String[] KEYS = null;
        private String[] _names;
        private String[] _parseRegions;

        private static /* synthetic */ int[] -getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues() {
            if (-android-icu-text-TimeZoneNames$NameTypeSwitchesValues != null) {
                return -android-icu-text-TimeZoneNames$NameTypeSwitchesValues;
            }
            int[] iArr = new int[NameType.values().length];
            try {
                iArr[NameType.EXEMPLAR_LOCATION.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[NameType.LONG_DAYLIGHT.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[NameType.LONG_GENERIC.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[NameType.LONG_STANDARD.ordinal()] = 6;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[NameType.SHORT_DAYLIGHT.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[NameType.SHORT_GENERIC.ordinal()] = 7;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[NameType.SHORT_STANDARD.ordinal()] = 2;
            } catch (NoSuchFieldError e7) {
            }
            -android-icu-text-TimeZoneNames$NameTypeSwitchesValues = iArr;
            return iArr;
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.TZDBTimeZoneNames.TZDBNames.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.TZDBTimeZoneNames.TZDBNames.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TZDBTimeZoneNames.TZDBNames.<clinit>():void");
        }

        private TZDBNames(String[] names, String[] parseRegions) {
            this._names = names;
            this._parseRegions = parseRegions;
        }

        static TZDBNames getInstance(ICUResourceBundle zoneStrings, String key) {
            if (zoneStrings == null || key == null || key.length() == 0) {
                return EMPTY_TZDBNAMES;
            }
            try {
                ICUResourceBundle table = (ICUResourceBundle) zoneStrings.get(key);
                boolean isEmpty = true;
                String[] names = new String[KEYS.length];
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
                String[] parseRegions = null;
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

        String getName(NameType type) {
            if (this._names == null) {
                return null;
            }
            String name = null;
            switch (-getandroid-icu-text-TimeZoneNames$NameTypeSwitchesValues()[type.ordinal()]) {
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    name = this._names[1];
                    break;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    name = this._names[0];
                    break;
            }
            return name;
        }

        String[] getParseRegions() {
            return this._parseRegions;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.TZDBTimeZoneNames.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.TZDBTimeZoneNames.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.TZDBTimeZoneNames.<clinit>():void");
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

    public String getMetaZoneDisplayName(String mzID, NameType type) {
        if (mzID == null || mzID.length() == 0 || (type != NameType.SHORT_STANDARD && type != NameType.SHORT_DAYLIGHT)) {
            return null;
        }
        return getMetaZoneNames(mzID).getName(type);
    }

    public String getTimeZoneDisplayName(String tzID, NameType type) {
        return null;
    }

    public Collection<MatchInfo> find(CharSequence text, int start, EnumSet<NameType> nameTypes) {
        if (text == null || text.length() == 0 || start < 0 || start >= text.length()) {
            throw new IllegalArgumentException("bad input text or range");
        }
        prepareFind();
        ResultHandler handler = new TZDBNameSearchHandler(nameTypes, getTargetRegion());
        TZDB_NAMES_TRIE.find(text, start, handler);
        return handler.getMatches();
    }

    private static TZDBNames getMetaZoneNames(String mzID) {
        TZDBNames names = (TZDBNames) TZDB_NAMES_MAP.get(mzID);
        if (names != null) {
            return names;
        }
        names = TZDBNames.getInstance(ZONESTRINGS, "meta:" + mzID);
        TZDBNames tmpNames = (TZDBNames) TZDB_NAMES_MAP.putIfAbsent(mzID.intern(), names);
        if (tmpNames == null) {
            return names;
        }
        return tmpNames;
    }

    private static void prepareFind() {
        if (TZDB_NAMES_TRIE == null) {
            synchronized (TZDBTimeZoneNames.class) {
                if (TZDB_NAMES_TRIE == null) {
                    TextTrieMap<TZDBNameInfo> trie = new TextTrieMap(true);
                    for (String mzID : TimeZoneNamesImpl._getAvailableMetaZoneIDs()) {
                        String mzID2;
                        TZDBNames names = getMetaZoneNames(mzID2);
                        String std = names.getName(NameType.SHORT_STANDARD);
                        String dst = names.getName(NameType.SHORT_DAYLIGHT);
                        if (std != null || dst != null) {
                            String[] parseRegions = names.getParseRegions();
                            mzID2 = mzID2.intern();
                            boolean equals = (std == null || dst == null) ? false : std.equals(dst);
                            if (std != null) {
                                TZDBNameInfo stdInf = new TZDBNameInfo();
                                stdInf.mzID = mzID2;
                                stdInf.type = NameType.SHORT_STANDARD;
                                stdInf.ambiguousType = equals;
                                stdInf.parseRegions = parseRegions;
                                trie.put(std, stdInf);
                            }
                            if (dst != null) {
                                TZDBNameInfo dstInf = new TZDBNameInfo();
                                dstInf.mzID = mzID2;
                                dstInf.type = NameType.SHORT_DAYLIGHT;
                                dstInf.ambiguousType = equals;
                                dstInf.parseRegions = parseRegions;
                                trie.put(dst, dstInf);
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

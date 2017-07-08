package android.icu.util;

import android.icu.impl.ICUResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.xmlpull.v1.XmlPullParser;

public class Region implements Comparable<Region> {
    private static final String OUTLYING_OCEANIA_REGION_ID = "QO";
    private static final String UNKNOWN_REGION_ID = "ZZ";
    private static final String WORLD_ID = "001";
    private static ArrayList<Set<Region>> availableRegions;
    private static Map<Integer, Region> numericCodeMap;
    private static Map<String, Region> regionAliases;
    private static boolean regionDataIsLoaded;
    private static Map<String, Region> regionIDMap;
    private static ArrayList<Region> regions;
    private int code;
    private Set<Region> containedRegions;
    private Region containingRegion;
    private String id;
    private List<Region> preferredValues;
    private RegionType type;

    public enum RegionType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Region.RegionType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Region.RegionType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Region.RegionType.<clinit>():void");
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.Region.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.Region.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.Region.<clinit>():void");
    }

    private Region() {
        this.containingRegion = null;
        this.containedRegions = new TreeSet();
        this.preferredValues = null;
    }

    private static synchronized void loadRegionData() {
        synchronized (Region.class) {
            if (regionDataIsLoaded) {
                return;
            }
            int i;
            regionAliases = new HashMap();
            regionIDMap = new HashMap();
            numericCodeMap = new HashMap();
            availableRegions = new ArrayList(RegionType.values().length);
            UResourceBundle territoryAlias = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "metadata", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("alias").get("territory");
            UResourceBundle supplementalData = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle codeMappings = supplementalData.get("codeMappings");
            UResourceBundle regionList = supplementalData.get("idValidity").get("region");
            UResourceBundle regionRegular = regionList.get("regular");
            UResourceBundle regionMacro = regionList.get("macroregion");
            UResourceBundle regionUnknown = regionList.get("unknown");
            UResourceBundle territoryContainment = supplementalData.get("territoryContainment");
            UResourceBundle worldContainment = territoryContainment.get(WORLD_ID);
            UResourceBundle groupingContainment = territoryContainment.get("grouping");
            List<String> continents = Arrays.asList(worldContainment.getStringArray());
            List<String> groupings = Arrays.asList(groupingContainment.getStringArray());
            List<String> regionCodes = new ArrayList();
            List<String> allRegions = new ArrayList();
            allRegions.addAll(Arrays.asList(regionRegular.getStringArray()));
            allRegions.addAll(Arrays.asList(regionMacro.getStringArray()));
            allRegions.add(regionUnknown.getString());
            for (String r : allRegions) {
                int rangeMarkerLocation = r.indexOf("~");
                if (rangeMarkerLocation > 0) {
                    StringBuilder stringBuilder = new StringBuilder(r);
                    char endRange = stringBuilder.charAt(rangeMarkerLocation + 1);
                    stringBuilder.setLength(rangeMarkerLocation);
                    char lastChar = stringBuilder.charAt(rangeMarkerLocation - 1);
                    while (lastChar <= endRange) {
                        regionCodes.add(stringBuilder.toString());
                        lastChar = (char) (lastChar + 1);
                        stringBuilder.setCharAt(rangeMarkerLocation - 1, lastChar);
                    }
                } else {
                    regionCodes.add(r);
                }
            }
            regions = new ArrayList(regionCodes.size());
            for (String id : regionCodes) {
                Region r2 = new Region();
                r2.id = id;
                r2.type = RegionType.TERRITORY;
                regionIDMap.put(id, r2);
                if (id.matches("[0-9]{3}")) {
                    r2.code = Integer.valueOf(id).intValue();
                    numericCodeMap.put(Integer.valueOf(r2.code), r2);
                    r2.type = RegionType.SUBCONTINENT;
                } else {
                    r2.code = -1;
                }
                regions.add(r2);
            }
            for (i = 0; i < territoryAlias.getSize(); i++) {
                UResourceBundle res = territoryAlias.get(i);
                String aliasFrom = res.getKey();
                String aliasTo = res.get("replacement").getString();
                if (regionIDMap.containsKey(aliasTo)) {
                    if (!regionIDMap.containsKey(aliasFrom)) {
                        regionAliases.put(aliasFrom, (Region) regionIDMap.get(aliasTo));
                    }
                }
                if (regionIDMap.containsKey(aliasFrom)) {
                    r2 = (Region) regionIDMap.get(aliasFrom);
                } else {
                    r2 = new Region();
                    r2.id = aliasFrom;
                    regionIDMap.put(aliasFrom, r2);
                    if (aliasFrom.matches("[0-9]{3}")) {
                        r2.code = Integer.valueOf(aliasFrom).intValue();
                        numericCodeMap.put(Integer.valueOf(r2.code), r2);
                    } else {
                        r2.code = -1;
                    }
                    regions.add(r2);
                }
                r2.type = RegionType.DEPRECATED;
                List<String> aliasToRegionStrings = Arrays.asList(aliasTo.split(" "));
                r2.preferredValues = new ArrayList();
                for (String s : aliasToRegionStrings) {
                    if (regionIDMap.containsKey(s)) {
                        r2.preferredValues.add((Region) regionIDMap.get(s));
                    }
                }
            }
            for (i = 0; i < codeMappings.getSize(); i++) {
                UResourceBundle mapping = codeMappings.get(i);
                if (mapping.getType() == 8) {
                    String[] codeMappingStrings = mapping.getStringArray();
                    String codeMappingID = codeMappingStrings[0];
                    Integer codeMappingNumber = Integer.valueOf(codeMappingStrings[1]);
                    String codeMapping3Letter = codeMappingStrings[2];
                    if (regionIDMap.containsKey(codeMappingID)) {
                        r2 = (Region) regionIDMap.get(codeMappingID);
                        r2.code = codeMappingNumber.intValue();
                        numericCodeMap.put(Integer.valueOf(r2.code), r2);
                        regionAliases.put(codeMapping3Letter, r2);
                    }
                }
            }
            if (regionIDMap.containsKey(WORLD_ID)) {
                r2 = (Region) regionIDMap.get(WORLD_ID);
                r2.type = RegionType.WORLD;
            }
            if (regionIDMap.containsKey(UNKNOWN_REGION_ID)) {
                r2 = (Region) regionIDMap.get(UNKNOWN_REGION_ID);
                r2.type = RegionType.UNKNOWN;
            }
            for (String continent : continents) {
                if (regionIDMap.containsKey(continent)) {
                    r2 = (Region) regionIDMap.get(continent);
                    r2.type = RegionType.CONTINENT;
                }
            }
            for (String grouping : groupings) {
                if (regionIDMap.containsKey(grouping)) {
                    r2 = (Region) regionIDMap.get(grouping);
                    r2.type = RegionType.GROUPING;
                }
            }
            if (regionIDMap.containsKey(OUTLYING_OCEANIA_REGION_ID)) {
                r2 = (Region) regionIDMap.get(OUTLYING_OCEANIA_REGION_ID);
                r2.type = RegionType.SUBCONTINENT;
            }
            for (i = 0; i < territoryContainment.getSize(); i++) {
                mapping = territoryContainment.get(i);
                String parent = mapping.getKey();
                if (!parent.equals("containedGroupings")) {
                    if (!parent.equals("deprecated")) {
                        Region parentRegion = (Region) regionIDMap.get(parent);
                        for (int j = 0; j < mapping.getSize(); j++) {
                            String child = mapping.getString(j);
                            Region childRegion = (Region) regionIDMap.get(child);
                            if (!(parentRegion == null || childRegion == null)) {
                                parentRegion.containedRegions.add(childRegion);
                                if (parentRegion.getType() != RegionType.GROUPING) {
                                    childRegion.containingRegion = parentRegion;
                                }
                            }
                        }
                    }
                }
            }
            i = 0;
            while (true) {
                if (i >= RegionType.values().length) {
                    break;
                }
                availableRegions.add(new TreeSet());
                i++;
            }
            for (Region ar : regions) {
                Set<Region> currentSet = (Set) availableRegions.get(ar.type.ordinal());
                currentSet.add(ar);
                availableRegions.set(ar.type.ordinal(), currentSet);
            }
            regionDataIsLoaded = true;
        }
    }

    public static Region getInstance(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        loadRegionData();
        Region r = (Region) regionIDMap.get(id);
        if (r == null) {
            r = (Region) regionAliases.get(id);
        }
        if (r == null) {
            throw new IllegalArgumentException("Unknown region id: " + id);
        } else if (r.type == RegionType.DEPRECATED && r.preferredValues.size() == 1) {
            return (Region) r.preferredValues.get(0);
        } else {
            return r;
        }
    }

    public static Region getInstance(int code) {
        loadRegionData();
        Region r = (Region) numericCodeMap.get(Integer.valueOf(code));
        if (r == null) {
            String pad = XmlPullParser.NO_NAMESPACE;
            if (code < 10) {
                pad = "00";
            } else if (code < 100) {
                pad = AndroidHardcodedSystemProperties.JAVA_VERSION;
            }
            r = (Region) regionAliases.get(pad + Integer.toString(code));
        }
        if (r == null) {
            throw new IllegalArgumentException("Unknown region code: " + code);
        } else if (r.type == RegionType.DEPRECATED && r.preferredValues.size() == 1) {
            return (Region) r.preferredValues.get(0);
        } else {
            return r;
        }
    }

    public static Set<Region> getAvailable(RegionType type) {
        loadRegionData();
        return Collections.unmodifiableSet((Set) availableRegions.get(type.ordinal()));
    }

    public Region getContainingRegion() {
        loadRegionData();
        return this.containingRegion;
    }

    public Region getContainingRegion(RegionType type) {
        loadRegionData();
        if (this.containingRegion == null) {
            return null;
        }
        if (this.containingRegion.type.equals(type)) {
            return this.containingRegion;
        }
        return this.containingRegion.getContainingRegion(type);
    }

    public Set<Region> getContainedRegions() {
        loadRegionData();
        return Collections.unmodifiableSet(this.containedRegions);
    }

    public Set<Region> getContainedRegions(RegionType type) {
        loadRegionData();
        Set<Region> result = new TreeSet();
        for (Region r : getContainedRegions()) {
            if (r.getType() == type) {
                result.add(r);
            } else {
                result.addAll(r.getContainedRegions(type));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public List<Region> getPreferredValues() {
        loadRegionData();
        if (this.type == RegionType.DEPRECATED) {
            return Collections.unmodifiableList(this.preferredValues);
        }
        return null;
    }

    public boolean contains(Region other) {
        loadRegionData();
        if (this.containedRegions.contains(other)) {
            return true;
        }
        for (Region cr : this.containedRegions) {
            if (cr.contains(other)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return this.id;
    }

    public int getNumericCode() {
        return this.code;
    }

    public RegionType getType() {
        return this.type;
    }

    public /* bridge */ /* synthetic */ int compareTo(Object other) {
        return compareTo((Region) other);
    }

    public int compareTo(Region other) {
        return this.id.compareTo(other.id);
    }
}

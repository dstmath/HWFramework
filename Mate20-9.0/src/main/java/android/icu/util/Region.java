package android.icu.util;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.number.Padder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Region implements Comparable<Region> {
    private static final String OUTLYING_OCEANIA_REGION_ID = "QO";
    private static final String UNKNOWN_REGION_ID = "ZZ";
    private static final String WORLD_ID = "001";
    private static ArrayList<Set<Region>> availableRegions = null;
    private static Map<Integer, Region> numericCodeMap = null;
    private static Map<String, Region> regionAliases = null;
    private static boolean regionDataIsLoaded = false;
    private static Map<String, Region> regionIDMap = null;
    private static ArrayList<Region> regions = null;
    private int code;
    private Set<Region> containedRegions = new TreeSet();
    private Region containingRegion = null;
    private String id;
    private List<Region> preferredValues = null;
    private RegionType type;

    public enum RegionType {
        UNKNOWN,
        TERRITORY,
        WORLD,
        CONTINENT,
        SUBCONTINENT,
        GROUPING,
        DEPRECATED
    }

    private Region() {
    }

    private static synchronized void loadRegionData() {
        List<String> groupings;
        UResourceBundle mapping;
        List<String> groupings2;
        UResourceBundle codeMappings;
        UResourceBundle territoryAlias;
        List<String> regionCodes;
        Region r;
        Region r2;
        UResourceBundle regionUnknown;
        UResourceBundle regionMacro;
        UResourceBundle regionRegular;
        synchronized (Region.class) {
            if (!regionDataIsLoaded) {
                regionAliases = new HashMap();
                regionIDMap = new HashMap();
                numericCodeMap = new HashMap();
                availableRegions = new ArrayList<>(RegionType.values().length);
                UResourceBundle metadataAlias = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metadata", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("alias");
                UResourceBundle territoryAlias2 = metadataAlias.get("territory");
                UResourceBundle supplementalData = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                UResourceBundle codeMappings2 = supplementalData.get("codeMappings");
                UResourceBundle idValidity = supplementalData.get("idValidity");
                UResourceBundle regionList = idValidity.get("region");
                UResourceBundle regionRegular2 = regionList.get("regular");
                UResourceBundle regionMacro2 = regionList.get("macroregion");
                UResourceBundle regionUnknown2 = regionList.get("unknown");
                UResourceBundle territoryContainment = supplementalData.get("territoryContainment");
                UResourceBundle worldContainment = territoryContainment.get(WORLD_ID);
                UResourceBundle groupingContainment = territoryContainment.get("grouping");
                List<String> continents = Arrays.asList(worldContainment.getStringArray());
                UResourceBundle uResourceBundle = metadataAlias;
                String[] groupingArr = groupingContainment.getStringArray();
                List<String> groupings3 = Arrays.asList(groupingArr);
                String[] strArr = groupingArr;
                List<String> regionCodes2 = new ArrayList<>();
                UResourceBundle uResourceBundle2 = idValidity;
                List<String> allRegions = new ArrayList<>();
                UResourceBundle uResourceBundle3 = regionList;
                allRegions.addAll(Arrays.asList(regionRegular2.getStringArray()));
                allRegions.addAll(Arrays.asList(regionMacro2.getStringArray()));
                allRegions.add(regionUnknown2.getString());
                Iterator<String> it = allRegions.iterator();
                while (it.hasNext()) {
                    List<String> allRegions2 = allRegions;
                    Iterator<String> it2 = it;
                    String r3 = it.next();
                    int rangeMarkerLocation = r3.indexOf("~");
                    if (rangeMarkerLocation > 0) {
                        regionRegular = regionRegular2;
                        StringBuilder regionName = new StringBuilder(r3);
                        regionMacro = regionMacro2;
                        char endRange = regionName.charAt(rangeMarkerLocation + 1);
                        regionName.setLength(rangeMarkerLocation);
                        regionUnknown = regionUnknown2;
                        char lastChar = regionName.charAt(rangeMarkerLocation - 1);
                        while (lastChar <= endRange) {
                            char endRange2 = endRange;
                            String newRegion = regionName.toString();
                            regionCodes2.add(newRegion);
                            String str = newRegion;
                            lastChar = (char) (lastChar + 1);
                            regionName.setCharAt(rangeMarkerLocation - 1, lastChar);
                            endRange = endRange2;
                        }
                    } else {
                        regionRegular = regionRegular2;
                        regionMacro = regionMacro2;
                        regionUnknown = regionUnknown2;
                        regionCodes2.add(r3);
                    }
                    allRegions = allRegions2;
                    it = it2;
                    regionRegular2 = regionRegular;
                    regionMacro2 = regionMacro;
                    regionUnknown2 = regionUnknown;
                }
                UResourceBundle uResourceBundle4 = regionRegular2;
                UResourceBundle uResourceBundle5 = regionMacro2;
                UResourceBundle uResourceBundle6 = regionUnknown2;
                regions = new ArrayList<>(regionCodes2.size());
                for (String id2 : regionCodes2) {
                    Region r4 = new Region();
                    r4.id = id2;
                    r4.type = RegionType.TERRITORY;
                    regionIDMap.put(id2, r4);
                    if (id2.matches("[0-9]{3}")) {
                        r4.code = Integer.valueOf(id2).intValue();
                        numericCodeMap.put(Integer.valueOf(r4.code), r4);
                        r4.type = RegionType.SUBCONTINENT;
                    } else {
                        r4.code = -1;
                    }
                    regions.add(r4);
                }
                int i = 0;
                while (i < territoryAlias2.getSize()) {
                    UResourceBundle res = territoryAlias2.get(i);
                    String aliasFrom = res.getKey();
                    String aliasTo = res.get("replacement").getString();
                    if (!regionIDMap.containsKey(aliasTo) || regionIDMap.containsKey(aliasFrom)) {
                        regionCodes = regionCodes2;
                        if (regionIDMap.containsKey(aliasFrom)) {
                            r = regionIDMap.get(aliasFrom);
                            territoryAlias = territoryAlias2;
                        } else {
                            r = new Region();
                            r.id = aliasFrom;
                            regionIDMap.put(aliasFrom, r);
                            if (aliasFrom.matches("[0-9]{3}")) {
                                r.code = Integer.valueOf(aliasFrom).intValue();
                                territoryAlias = territoryAlias2;
                                numericCodeMap.put(Integer.valueOf(r.code), r);
                            } else {
                                territoryAlias = territoryAlias2;
                                r.code = -1;
                            }
                            regions.add(r);
                        }
                        r.type = RegionType.DEPRECATED;
                        List<String> aliasToRegionStrings = Arrays.asList(aliasTo.split(Padder.FALLBACK_PADDING_STRING));
                        r.preferredValues = new ArrayList();
                        Iterator<String> it3 = aliasToRegionStrings.iterator();
                        while (it3.hasNext()) {
                            Iterator<String> it4 = it3;
                            String aliasTo2 = aliasTo;
                            String s = it3.next();
                            if (regionIDMap.containsKey(s)) {
                                r2 = r;
                                r.preferredValues.add(regionIDMap.get(s));
                            } else {
                                r2 = r;
                            }
                            it3 = it4;
                            aliasTo = aliasTo2;
                            r = r2;
                        }
                    } else {
                        regionCodes = regionCodes2;
                        regionAliases.put(aliasFrom, regionIDMap.get(aliasTo));
                        territoryAlias = territoryAlias2;
                    }
                    i++;
                    regionCodes2 = regionCodes;
                    territoryAlias2 = territoryAlias;
                }
                UResourceBundle uResourceBundle7 = territoryAlias2;
                int i2 = 0;
                while (i2 < codeMappings2.getSize()) {
                    UResourceBundle mapping2 = codeMappings2.get(i2);
                    if (mapping2.getType() == 8) {
                        String[] codeMappingStrings = mapping2.getStringArray();
                        String codeMappingID = codeMappingStrings[0];
                        Integer codeMappingNumber = Integer.valueOf(codeMappingStrings[1]);
                        String codeMapping3Letter = codeMappingStrings[2];
                        if (regionIDMap.containsKey(codeMappingID)) {
                            Region r5 = regionIDMap.get(codeMappingID);
                            UResourceBundle uResourceBundle8 = mapping2;
                            r5.code = codeMappingNumber.intValue();
                            codeMappings = codeMappings2;
                            numericCodeMap.put(Integer.valueOf(r5.code), r5);
                            regionAliases.put(codeMapping3Letter, r5);
                            i2++;
                            codeMappings2 = codeMappings;
                        }
                    }
                    codeMappings = codeMappings2;
                    i2++;
                    codeMappings2 = codeMappings;
                }
                if (regionIDMap.containsKey(WORLD_ID)) {
                    regionIDMap.get(WORLD_ID).type = RegionType.WORLD;
                }
                if (regionIDMap.containsKey(UNKNOWN_REGION_ID)) {
                    regionIDMap.get(UNKNOWN_REGION_ID).type = RegionType.UNKNOWN;
                }
                for (String continent : continents) {
                    if (regionIDMap.containsKey(continent)) {
                        regionIDMap.get(continent).type = RegionType.CONTINENT;
                    }
                }
                List<String> groupings4 = groupings3;
                for (String grouping : groupings4) {
                    if (regionIDMap.containsKey(grouping)) {
                        regionIDMap.get(grouping).type = RegionType.GROUPING;
                    }
                }
                if (regionIDMap.containsKey(OUTLYING_OCEANIA_REGION_ID)) {
                    regionIDMap.get(OUTLYING_OCEANIA_REGION_ID).type = RegionType.SUBCONTINENT;
                }
                int i3 = 0;
                while (i3 < territoryContainment.getSize()) {
                    UResourceBundle mapping3 = territoryContainment.get(i3);
                    String parent = mapping3.getKey();
                    if (!parent.equals("containedGroupings")) {
                        if (parent.equals("deprecated")) {
                            groupings = groupings4;
                            i3++;
                            groupings4 = groupings;
                        } else {
                            Region parentRegion = regionIDMap.get(parent);
                            int j = 0;
                            while (j < mapping3.getSize()) {
                                Region childRegion = regionIDMap.get(mapping3.getString(j));
                                if (parentRegion == null || childRegion == null) {
                                    groupings2 = groupings4;
                                    mapping = mapping3;
                                } else {
                                    groupings2 = groupings4;
                                    parentRegion.containedRegions.add(childRegion);
                                    mapping = mapping3;
                                    if (parentRegion.getType() != RegionType.GROUPING) {
                                        childRegion.containingRegion = parentRegion;
                                    }
                                }
                                j++;
                                groupings4 = groupings2;
                                mapping3 = mapping;
                            }
                        }
                    }
                    groupings = groupings4;
                    i3++;
                    groupings4 = groupings;
                }
                int i4 = 0;
                while (true) {
                    int i5 = i4;
                    if (i5 >= RegionType.values().length) {
                        break;
                    }
                    availableRegions.add(new TreeSet());
                    i4 = i5 + 1;
                }
                Iterator<Region> it5 = regions.iterator();
                while (it5.hasNext()) {
                    Region ar = it5.next();
                    Set<Region> currentSet = availableRegions.get(ar.type.ordinal());
                    currentSet.add(ar);
                    availableRegions.set(ar.type.ordinal(), currentSet);
                }
                regionDataIsLoaded = true;
            }
        }
    }

    public static Region getInstance(String id2) {
        if (id2 != null) {
            loadRegionData();
            Region r = regionIDMap.get(id2);
            if (r == null) {
                r = regionAliases.get(id2);
            }
            if (r == null) {
                throw new IllegalArgumentException("Unknown region id: " + id2);
            } else if (r.type == RegionType.DEPRECATED && r.preferredValues.size() == 1) {
                return r.preferredValues.get(0);
            } else {
                return r;
            }
        } else {
            throw new NullPointerException();
        }
    }

    public static Region getInstance(int code2) {
        loadRegionData();
        Region r = numericCodeMap.get(Integer.valueOf(code2));
        if (r == null) {
            String pad = "";
            if (code2 < 10) {
                pad = "00";
            } else if (code2 < 100) {
                pad = AndroidHardcodedSystemProperties.JAVA_VERSION;
            }
            r = regionAliases.get(pad + Integer.toString(code2));
        }
        if (r == null) {
            throw new IllegalArgumentException("Unknown region code: " + code2);
        } else if (r.type == RegionType.DEPRECATED && r.preferredValues.size() == 1) {
            return r.preferredValues.get(0);
        } else {
            return r;
        }
    }

    public static Set<Region> getAvailable(RegionType type2) {
        loadRegionData();
        return Collections.unmodifiableSet(availableRegions.get(type2.ordinal()));
    }

    public Region getContainingRegion() {
        loadRegionData();
        return this.containingRegion;
    }

    public Region getContainingRegion(RegionType type2) {
        loadRegionData();
        if (this.containingRegion == null) {
            return null;
        }
        if (this.containingRegion.type.equals(type2)) {
            return this.containingRegion;
        }
        return this.containingRegion.getContainingRegion(type2);
    }

    public Set<Region> getContainedRegions() {
        loadRegionData();
        return Collections.unmodifiableSet(this.containedRegions);
    }

    public Set<Region> getContainedRegions(RegionType type2) {
        loadRegionData();
        Set<Region> result = new TreeSet<>();
        for (Region r : getContainedRegions()) {
            if (r.getType() == type2) {
                result.add(r);
            } else {
                result.addAll(r.getContainedRegions(type2));
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

    public int compareTo(Region other) {
        return this.id.compareTo(other.id);
    }
}

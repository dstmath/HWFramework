package android.icu.util;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
        synchronized (Region.class) {
            if (regionDataIsLoaded) {
                return;
            }
            Region r;
            int i;
            UResourceBundle mapping;
            regionAliases = new HashMap();
            regionIDMap = new HashMap();
            numericCodeMap = new HashMap();
            availableRegions = new ArrayList(RegionType.values().length);
            UResourceBundle territoryAlias = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "metadata", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("alias").get("territory");
            UResourceBundle supplementalData = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
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
            for (String r2 : allRegions) {
                int rangeMarkerLocation = r2.indexOf("~");
                if (rangeMarkerLocation > 0) {
                    StringBuilder stringBuilder = new StringBuilder(r2);
                    char endRange = stringBuilder.charAt(rangeMarkerLocation + 1);
                    stringBuilder.setLength(rangeMarkerLocation);
                    char lastChar = stringBuilder.charAt(rangeMarkerLocation - 1);
                    while (lastChar <= endRange) {
                        regionCodes.add(stringBuilder.toString());
                        lastChar = (char) (lastChar + 1);
                        stringBuilder.setCharAt(rangeMarkerLocation - 1, lastChar);
                    }
                } else {
                    regionCodes.add(r2);
                }
            }
            regions = new ArrayList(regionCodes.size());
            for (String id : regionCodes) {
                r = new Region();
                r.id = id;
                r.type = RegionType.TERRITORY;
                regionIDMap.put(id, r);
                if (id.matches("[0-9]{3}")) {
                    r.code = Integer.valueOf(id).intValue();
                    numericCodeMap.put(Integer.valueOf(r.code), r);
                    r.type = RegionType.SUBCONTINENT;
                } else {
                    r.code = -1;
                }
                regions.add(r);
            }
            for (i = 0; i < territoryAlias.getSize(); i++) {
                UResourceBundle res = territoryAlias.get(i);
                String aliasFrom = res.getKey();
                String aliasTo = res.get("replacement").getString();
                if (!regionIDMap.containsKey(aliasTo) || (regionIDMap.containsKey(aliasFrom) ^ 1) == 0) {
                    if (regionIDMap.containsKey(aliasFrom)) {
                        r = (Region) regionIDMap.get(aliasFrom);
                    } else {
                        r = new Region();
                        r.id = aliasFrom;
                        regionIDMap.put(aliasFrom, r);
                        if (aliasFrom.matches("[0-9]{3}")) {
                            r.code = Integer.valueOf(aliasFrom).intValue();
                            numericCodeMap.put(Integer.valueOf(r.code), r);
                        } else {
                            r.code = -1;
                        }
                        regions.add(r);
                    }
                    r.type = RegionType.DEPRECATED;
                    List<String> aliasToRegionStrings = Arrays.asList(aliasTo.split(" "));
                    r.preferredValues = new ArrayList();
                    for (String s : aliasToRegionStrings) {
                        if (regionIDMap.containsKey(s)) {
                            r.preferredValues.add((Region) regionIDMap.get(s));
                        }
                    }
                } else {
                    regionAliases.put(aliasFrom, (Region) regionIDMap.get(aliasTo));
                }
            }
            for (i = 0; i < codeMappings.getSize(); i++) {
                mapping = codeMappings.get(i);
                if (mapping.getType() == 8) {
                    String[] codeMappingStrings = mapping.getStringArray();
                    String codeMappingID = codeMappingStrings[0];
                    Integer codeMappingNumber = Integer.valueOf(codeMappingStrings[1]);
                    String codeMapping3Letter = codeMappingStrings[2];
                    if (regionIDMap.containsKey(codeMappingID)) {
                        r = (Region) regionIDMap.get(codeMappingID);
                        r.code = codeMappingNumber.intValue();
                        numericCodeMap.put(Integer.valueOf(r.code), r);
                        regionAliases.put(codeMapping3Letter, r);
                    }
                }
            }
            if (regionIDMap.containsKey(WORLD_ID)) {
                ((Region) regionIDMap.get(WORLD_ID)).type = RegionType.WORLD;
            }
            if (regionIDMap.containsKey(UNKNOWN_REGION_ID)) {
                ((Region) regionIDMap.get(UNKNOWN_REGION_ID)).type = RegionType.UNKNOWN;
            }
            for (String continent : continents) {
                if (regionIDMap.containsKey(continent)) {
                    ((Region) regionIDMap.get(continent)).type = RegionType.CONTINENT;
                }
            }
            for (String grouping : groupings) {
                if (regionIDMap.containsKey(grouping)) {
                    ((Region) regionIDMap.get(grouping)).type = RegionType.GROUPING;
                }
            }
            if (regionIDMap.containsKey(OUTLYING_OCEANIA_REGION_ID)) {
                ((Region) regionIDMap.get(OUTLYING_OCEANIA_REGION_ID)).type = RegionType.SUBCONTINENT;
            }
            for (i = 0; i < territoryContainment.getSize(); i++) {
                mapping = territoryContainment.get(i);
                String parent = mapping.getKey();
                if (!(parent.equals("containedGroupings") || parent.equals("deprecated"))) {
                    Region parentRegion = (Region) regionIDMap.get(parent);
                    for (int j = 0; j < mapping.getSize(); j++) {
                        Region childRegion = (Region) regionIDMap.get(mapping.getString(j));
                        if (!(parentRegion == null || childRegion == null)) {
                            parentRegion.containedRegions.add(childRegion);
                            if (parentRegion.getType() != RegionType.GROUPING) {
                                childRegion.containingRegion = parentRegion;
                            }
                        }
                    }
                }
            }
            for (i = 0; i < RegionType.values().length; i++) {
                availableRegions.add(new TreeSet());
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
            String pad = "";
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

    public int compareTo(Region other) {
        return this.id.compareTo(other.id);
    }
}

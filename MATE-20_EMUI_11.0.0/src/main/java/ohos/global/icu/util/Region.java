package ohos.global.icu.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.light.bean.LightEffect;

public class Region implements Comparable<Region> {
    private static final String OUTLYING_OCEANIA_REGION_ID = "QO";
    private static final String UNKNOWN_REGION_ID = "ZZ";
    private static final String WORLD_ID = "001";
    private static ArrayList<Set<Region>> availableRegions = null;
    private static Map<Integer, Region> numericCodeMap = null;
    private static Map<String, Region> regionAliases = null;
    private static boolean regionDataIsLoaded = false;
    private static Map<String, Region> regionIDMap;
    private static ArrayList<Region> regions;
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
        Region region;
        synchronized (Region.class) {
            if (!regionDataIsLoaded) {
                regionAliases = new HashMap();
                regionIDMap = new HashMap();
                numericCodeMap = new HashMap();
                availableRegions = new ArrayList<>(RegionType.values().length);
                UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "metadata", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("alias").get("territory");
                UResourceBundle bundleInstance = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
                UResourceBundle uResourceBundle2 = bundleInstance.get("codeMappings");
                UResourceBundle uResourceBundle3 = bundleInstance.get("idValidity").get("region");
                UResourceBundle uResourceBundle4 = uResourceBundle3.get("regular");
                UResourceBundle uResourceBundle5 = uResourceBundle3.get("macroregion");
                UResourceBundle uResourceBundle6 = uResourceBundle3.get("unknown");
                UResourceBundle uResourceBundle7 = bundleInstance.get("territoryContainment");
                UResourceBundle uResourceBundle8 = uResourceBundle7.get(WORLD_ID);
                UResourceBundle uResourceBundle9 = uResourceBundle7.get("grouping");
                List<String> asList = Arrays.asList(uResourceBundle8.getStringArray());
                Enumeration<String> keys = uResourceBundle9.getKeys();
                ArrayList<String> arrayList = new ArrayList();
                ArrayList<String> arrayList2 = new ArrayList();
                arrayList2.addAll(Arrays.asList(uResourceBundle4.getStringArray()));
                arrayList2.addAll(Arrays.asList(uResourceBundle5.getStringArray()));
                arrayList2.add(uResourceBundle6.getString());
                for (String str : arrayList2) {
                    int indexOf = str.indexOf("~");
                    if (indexOf > 0) {
                        StringBuilder sb = new StringBuilder(str);
                        char charAt = sb.charAt(indexOf + 1);
                        sb.setLength(indexOf);
                        int i = indexOf - 1;
                        char charAt2 = sb.charAt(i);
                        while (charAt2 <= charAt) {
                            arrayList.add(sb.toString());
                            charAt2 = (char) (charAt2 + 1);
                            sb.setCharAt(i, charAt2);
                        }
                    } else {
                        arrayList.add(str);
                    }
                }
                regions = new ArrayList<>(arrayList.size());
                for (String str2 : arrayList) {
                    Region region2 = new Region();
                    region2.id = str2;
                    region2.type = RegionType.TERRITORY;
                    regionIDMap.put(str2, region2);
                    if (str2.matches("[0-9]{3}")) {
                        region2.code = Integer.valueOf(str2).intValue();
                        numericCodeMap.put(Integer.valueOf(region2.code), region2);
                        region2.type = RegionType.SUBCONTINENT;
                    } else {
                        region2.code = -1;
                    }
                    regions.add(region2);
                }
                for (int i2 = 0; i2 < uResourceBundle.getSize(); i2++) {
                    UResourceBundle uResourceBundle10 = uResourceBundle.get(i2);
                    String key = uResourceBundle10.getKey();
                    String string = uResourceBundle10.get("replacement").getString();
                    if (!regionIDMap.containsKey(string) || regionIDMap.containsKey(key)) {
                        if (regionIDMap.containsKey(key)) {
                            region = regionIDMap.get(key);
                        } else {
                            Region region3 = new Region();
                            region3.id = key;
                            regionIDMap.put(key, region3);
                            if (key.matches("[0-9]{3}")) {
                                region3.code = Integer.valueOf(key).intValue();
                                numericCodeMap.put(Integer.valueOf(region3.code), region3);
                            } else {
                                region3.code = -1;
                            }
                            regions.add(region3);
                            region = region3;
                        }
                        region.type = RegionType.DEPRECATED;
                        List<String> asList2 = Arrays.asList(string.split(" "));
                        region.preferredValues = new ArrayList();
                        for (String str3 : asList2) {
                            if (regionIDMap.containsKey(str3)) {
                                region.preferredValues.add(regionIDMap.get(str3));
                            }
                        }
                    } else {
                        regionAliases.put(key, regionIDMap.get(string));
                    }
                }
                for (int i3 = 0; i3 < uResourceBundle2.getSize(); i3++) {
                    UResourceBundle uResourceBundle11 = uResourceBundle2.get(i3);
                    if (uResourceBundle11.getType() == 8) {
                        String[] stringArray = uResourceBundle11.getStringArray();
                        String str4 = stringArray[0];
                        Integer valueOf = Integer.valueOf(stringArray[1]);
                        String str5 = stringArray[2];
                        if (regionIDMap.containsKey(str4)) {
                            Region region4 = regionIDMap.get(str4);
                            region4.code = valueOf.intValue();
                            numericCodeMap.put(Integer.valueOf(region4.code), region4);
                            regionAliases.put(str5, region4);
                        }
                    }
                }
                if (regionIDMap.containsKey(WORLD_ID)) {
                    regionIDMap.get(WORLD_ID).type = RegionType.WORLD;
                }
                if (regionIDMap.containsKey(UNKNOWN_REGION_ID)) {
                    regionIDMap.get(UNKNOWN_REGION_ID).type = RegionType.UNKNOWN;
                }
                for (String str6 : asList) {
                    if (regionIDMap.containsKey(str6)) {
                        regionIDMap.get(str6).type = RegionType.CONTINENT;
                    }
                }
                while (keys.hasMoreElements()) {
                    String nextElement = keys.nextElement();
                    if (regionIDMap.containsKey(nextElement)) {
                        regionIDMap.get(nextElement).type = RegionType.GROUPING;
                    }
                }
                if (regionIDMap.containsKey(OUTLYING_OCEANIA_REGION_ID)) {
                    regionIDMap.get(OUTLYING_OCEANIA_REGION_ID).type = RegionType.SUBCONTINENT;
                }
                for (int i4 = 0; i4 < uResourceBundle7.getSize(); i4++) {
                    UResourceBundle uResourceBundle12 = uResourceBundle7.get(i4);
                    String key2 = uResourceBundle12.getKey();
                    if (!key2.equals("containedGroupings") && !key2.equals("deprecated") && !key2.equals("grouping")) {
                        Region region5 = regionIDMap.get(key2);
                        for (int i5 = 0; i5 < uResourceBundle12.getSize(); i5++) {
                            Region region6 = regionIDMap.get(uResourceBundle12.getString(i5));
                            if (!(region5 == null || region6 == null)) {
                                region5.containedRegions.add(region6);
                                if (region5.getType() != RegionType.GROUPING) {
                                    region6.containingRegion = region5;
                                }
                            }
                        }
                    }
                }
                for (int i6 = 0; i6 < RegionType.values().length; i6++) {
                    availableRegions.add(new TreeSet());
                }
                Iterator<Region> it = regions.iterator();
                while (it.hasNext()) {
                    Region next = it.next();
                    Set<Region> set = availableRegions.get(next.type.ordinal());
                    set.add(next);
                    availableRegions.set(next.type.ordinal(), set);
                }
                regionDataIsLoaded = true;
            }
        }
    }

    public static Region getInstance(String str) {
        if (str != null) {
            loadRegionData();
            Region region = regionIDMap.get(str);
            if (region == null) {
                region = regionAliases.get(str);
            }
            if (region != null) {
                return (region.type == RegionType.DEPRECATED && region.preferredValues.size() == 1) ? region.preferredValues.get(0) : region;
            }
            throw new IllegalArgumentException("Unknown region id: " + str);
        }
        throw new NullPointerException();
    }

    public static Region getInstance(int i) {
        loadRegionData();
        Region region = numericCodeMap.get(Integer.valueOf(i));
        if (region == null) {
            String str = i < 10 ? "00" : i < 100 ? LightEffect.LIGHT_ID_LED : "";
            region = regionAliases.get(str + Integer.toString(i));
        }
        if (region != null) {
            return (region.type == RegionType.DEPRECATED && region.preferredValues.size() == 1) ? region.preferredValues.get(0) : region;
        }
        throw new IllegalArgumentException("Unknown region code: " + i);
    }

    public static Set<Region> getAvailable(RegionType regionType) {
        loadRegionData();
        return Collections.unmodifiableSet(availableRegions.get(regionType.ordinal()));
    }

    public Region getContainingRegion() {
        loadRegionData();
        return this.containingRegion;
    }

    public Region getContainingRegion(RegionType regionType) {
        loadRegionData();
        Region region = this.containingRegion;
        if (region == null) {
            return null;
        }
        if (region.type.equals(regionType)) {
            return this.containingRegion;
        }
        return this.containingRegion.getContainingRegion(regionType);
    }

    public Set<Region> getContainedRegions() {
        loadRegionData();
        return Collections.unmodifiableSet(this.containedRegions);
    }

    public Set<Region> getContainedRegions(RegionType regionType) {
        loadRegionData();
        TreeSet treeSet = new TreeSet();
        for (Region region : getContainedRegions()) {
            if (region.getType() == regionType) {
                treeSet.add(region);
            } else {
                treeSet.addAll(region.getContainedRegions(regionType));
            }
        }
        return Collections.unmodifiableSet(treeSet);
    }

    public List<Region> getPreferredValues() {
        loadRegionData();
        if (this.type == RegionType.DEPRECATED) {
            return Collections.unmodifiableList(this.preferredValues);
        }
        return null;
    }

    public boolean contains(Region region) {
        loadRegionData();
        if (this.containedRegions.contains(region)) {
            return true;
        }
        for (Region region2 : this.containedRegions) {
            if (region2.contains(region)) {
                return true;
            }
        }
        return false;
    }

    @Override // java.lang.Object
    public String toString() {
        return this.id;
    }

    public int getNumericCode() {
        return this.code;
    }

    public RegionType getType() {
        return this.type;
    }

    public int compareTo(Region region) {
        return this.id.compareTo(region.id);
    }
}

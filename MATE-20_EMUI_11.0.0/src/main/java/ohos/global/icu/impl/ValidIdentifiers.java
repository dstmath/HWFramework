package ohos.global.icu.impl;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ohos.global.icu.impl.locale.AsciiUtil;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.UResourceBundleIterator;

public class ValidIdentifiers {

    public enum Datasubtype {
        deprecated,
        private_use,
        regular,
        special,
        unknown,
        macroregion,
        reserved
    }

    public enum Datatype {
        currency,
        language,
        region,
        script,
        subdivision,
        unit,
        variant,
        u,
        t,
        x,
        illegal
    }

    public static class ValiditySet {
        public final Set<String> regularData;
        public final Map<String, Set<String>> subdivisionData;

        public ValiditySet(Set<String> set, boolean z) {
            Set set2;
            if (z) {
                HashMap hashMap = new HashMap();
                for (String str : set) {
                    int indexOf = str.indexOf(45);
                    int i = indexOf + 1;
                    if (indexOf < 0) {
                        indexOf = str.charAt(0) < 'A' ? 3 : 2;
                        i = indexOf;
                    }
                    String substring = str.substring(0, indexOf);
                    String substring2 = str.substring(i);
                    Set set3 = (Set) hashMap.get(substring);
                    if (set3 == null) {
                        set3 = new HashSet();
                        hashMap.put(substring, set3);
                    }
                    set3.add(substring2);
                }
                this.regularData = null;
                HashMap hashMap2 = new HashMap();
                for (Map.Entry entry : hashMap.entrySet()) {
                    Set set4 = (Set) entry.getValue();
                    if (set4.size() == 1) {
                        set2 = Collections.singleton((String) set4.iterator().next());
                    } else {
                        set2 = Collections.unmodifiableSet(set4);
                    }
                    hashMap2.put((String) entry.getKey(), set2);
                }
                this.subdivisionData = Collections.unmodifiableMap(hashMap2);
                return;
            }
            this.regularData = Collections.unmodifiableSet(set);
            this.subdivisionData = null;
        }

        public boolean contains(String str) {
            Set<String> set = this.regularData;
            if (set != null) {
                return set.contains(str);
            }
            int indexOf = str.indexOf(45);
            return contains(str.substring(0, indexOf), str.substring(indexOf + 1));
        }

        public boolean contains(String str, String str2) {
            Set<String> set = this.subdivisionData.get(str);
            return set != null && set.contains(str2);
        }

        public String toString() {
            Set<String> set = this.regularData;
            if (set != null) {
                return set.toString();
            }
            return this.subdivisionData.toString();
        }
    }

    private static class ValidityData {
        static final Map<Datatype, Map<Datasubtype, ValiditySet>> data;

        private ValidityData() {
        }

        static {
            EnumMap enumMap = new EnumMap(Datatype.class);
            UResourceBundleIterator iterator = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("idValidity").getIterator();
            while (iterator.hasNext()) {
                UResourceBundle next = iterator.next();
                Datatype valueOf = Datatype.valueOf(next.getKey());
                EnumMap enumMap2 = new EnumMap(Datasubtype.class);
                UResourceBundleIterator iterator2 = next.getIterator();
                while (iterator2.hasNext()) {
                    UResourceBundle next2 = iterator2.next();
                    Datasubtype valueOf2 = Datasubtype.valueOf(next2.getKey());
                    HashSet hashSet = new HashSet();
                    boolean z = false;
                    if (next2.getType() == 0) {
                        addRange(next2.getString(), hashSet);
                    } else {
                        for (String str : next2.getStringArray()) {
                            addRange(str, hashSet);
                        }
                    }
                    if (valueOf == Datatype.subdivision) {
                        z = true;
                    }
                    enumMap2.put((EnumMap) valueOf2, (Datasubtype) new ValiditySet(hashSet, z));
                }
                enumMap.put((EnumMap) valueOf, (Datatype) Collections.unmodifiableMap(enumMap2));
            }
            data = Collections.unmodifiableMap(enumMap);
        }

        private static void addRange(String str, Set<String> set) {
            String lowerString = AsciiUtil.toLowerString(str);
            int indexOf = lowerString.indexOf(126);
            if (indexOf < 0) {
                set.add(lowerString);
            } else {
                StringRange.expand(lowerString.substring(0, indexOf), lowerString.substring(indexOf + 1), false, set);
            }
        }
    }

    public static Map<Datatype, Map<Datasubtype, ValiditySet>> getData() {
        return ValidityData.data;
    }

    public static Datasubtype isValid(Datatype datatype, Set<Datasubtype> set, String str) {
        Map<Datasubtype, ValiditySet> map = ValidityData.data.get(datatype);
        if (map == null) {
            return null;
        }
        for (Datasubtype datasubtype : set) {
            ValiditySet validitySet = map.get(datasubtype);
            if (validitySet != null && validitySet.contains(AsciiUtil.toLowerString(str))) {
                return datasubtype;
            }
        }
        return null;
    }

    public static Datasubtype isValid(Datatype datatype, Set<Datasubtype> set, String str, String str2) {
        Map<Datasubtype, ValiditySet> map = ValidityData.data.get(datatype);
        if (map == null) {
            return null;
        }
        String lowerString = AsciiUtil.toLowerString(str);
        String lowerString2 = AsciiUtil.toLowerString(str2);
        for (Datasubtype datasubtype : set) {
            ValiditySet validitySet = map.get(datasubtype);
            if (validitySet != null && validitySet.contains(lowerString, lowerString2)) {
                return datasubtype;
            }
        }
        return null;
    }
}

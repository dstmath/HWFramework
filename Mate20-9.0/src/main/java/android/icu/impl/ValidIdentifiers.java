package android.icu.impl;

import android.icu.impl.locale.AsciiUtil;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValidIdentifiers {

    public enum Datasubtype {
        deprecated,
        private_use,
        regular,
        special,
        unknown,
        macroregion
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

    private static class ValidityData {
        static final Map<Datatype, Map<Datasubtype, ValiditySet>> data;

        private ValidityData() {
        }

        static {
            UResourceBundle suppData;
            Map<Datatype, Map<Datasubtype, ValiditySet>> _data = new EnumMap<>(Datatype.class);
            UResourceBundle suppData2 = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundleIterator datatypeIterator = suppData2.get("idValidity").getIterator();
            while (datatypeIterator.hasNext()) {
                UResourceBundle datatype = datatypeIterator.next();
                Datatype key = Datatype.valueOf(datatype.getKey());
                Map<Datasubtype, ValiditySet> values = new EnumMap<>(Datasubtype.class);
                UResourceBundleIterator datasubtypeIterator = datatype.getIterator();
                while (datasubtypeIterator.hasNext()) {
                    UResourceBundle datasubtype = datasubtypeIterator.next();
                    Datasubtype subkey = Datasubtype.valueOf(datasubtype.getKey());
                    Set<String> subvalues = new HashSet<>();
                    if (datasubtype.getType() == 0) {
                        addRange(datasubtype.getString(), subvalues);
                        suppData = suppData2;
                    } else {
                        String[] stringArray = datasubtype.getStringArray();
                        int length = stringArray.length;
                        int i = 0;
                        while (i < length) {
                            addRange(stringArray[i], subvalues);
                            i++;
                            suppData2 = suppData2;
                        }
                        suppData = suppData2;
                    }
                    values.put(subkey, new ValiditySet(subvalues, key == Datatype.subdivision));
                    suppData2 = suppData;
                }
                _data.put(key, Collections.unmodifiableMap(values));
                suppData2 = suppData2;
            }
            data = Collections.unmodifiableMap(_data);
        }

        private static void addRange(String string, Set<String> subvalues) {
            String string2 = AsciiUtil.toLowerString(string);
            int pos = string2.indexOf(126);
            if (pos < 0) {
                subvalues.add(string2);
            } else {
                StringRange.expand(string2.substring(0, pos), string2.substring(pos + 1), false, subvalues);
            }
        }
    }

    public static class ValiditySet {
        public final Set<String> regularData;
        public final Map<String, Set<String>> subdivisionData;

        public ValiditySet(Set<String> plainData, boolean makeMap) {
            Set<String> set;
            if (makeMap) {
                HashMap<String, Set<String>> _subdivisionData = new HashMap<>();
                for (String s : plainData) {
                    int pos = s.indexOf(45);
                    int pos2 = pos + 1;
                    if (pos < 0) {
                        int i = s.charAt(0) < 'A' ? 3 : 2;
                        pos = i;
                        pos2 = i;
                    }
                    String key = s.substring(0, pos);
                    String subdivision = s.substring(pos2);
                    Set<String> oldSet = _subdivisionData.get(key);
                    if (oldSet == null) {
                        Set<String> hashSet = new HashSet<>();
                        oldSet = hashSet;
                        _subdivisionData.put(key, hashSet);
                    }
                    oldSet.add(subdivision);
                }
                this.regularData = null;
                HashMap<String, Set<String>> _subdivisionData2 = new HashMap<>();
                for (Map.Entry<String, Set<String>> e : _subdivisionData.entrySet()) {
                    Set<String> value = e.getValue();
                    if (value.size() == 1) {
                        set = Collections.singleton(value.iterator().next());
                    } else {
                        set = Collections.unmodifiableSet(value);
                    }
                    _subdivisionData2.put(e.getKey(), set);
                }
                this.subdivisionData = Collections.unmodifiableMap(_subdivisionData2);
                return;
            }
            this.regularData = Collections.unmodifiableSet(plainData);
            this.subdivisionData = null;
        }

        public boolean contains(String code) {
            if (this.regularData != null) {
                return this.regularData.contains(code);
            }
            int pos = code.indexOf(45);
            return contains(code.substring(0, pos), code.substring(pos + 1));
        }

        public boolean contains(String key, String value) {
            Set<String> oldSet = this.subdivisionData.get(key);
            return oldSet != null && oldSet.contains(value);
        }

        public String toString() {
            if (this.regularData != null) {
                return this.regularData.toString();
            }
            return this.subdivisionData.toString();
        }
    }

    public static Map<Datatype, Map<Datasubtype, ValiditySet>> getData() {
        return ValidityData.data;
    }

    public static Datasubtype isValid(Datatype datatype, Set<Datasubtype> datasubtypes, String code) {
        Map<Datasubtype, ValiditySet> subtable = ValidityData.data.get(datatype);
        if (subtable != null) {
            for (Datasubtype datasubtype : datasubtypes) {
                ValiditySet validitySet = subtable.get(datasubtype);
                if (validitySet != null && validitySet.contains(AsciiUtil.toLowerString(code))) {
                    return datasubtype;
                }
            }
        }
        return null;
    }

    public static Datasubtype isValid(Datatype datatype, Set<Datasubtype> datasubtypes, String code, String value) {
        Map<Datasubtype, ValiditySet> subtable = ValidityData.data.get(datatype);
        if (subtable != null) {
            String code2 = AsciiUtil.toLowerString(code);
            String value2 = AsciiUtil.toLowerString(value);
            for (Datasubtype datasubtype : datasubtypes) {
                ValiditySet validitySet = subtable.get(datasubtype);
                if (validitySet != null && validitySet.contains(code2, value2)) {
                    return datasubtype;
                }
            }
        }
        return null;
    }
}

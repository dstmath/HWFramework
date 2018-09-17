package android.icu.impl.locale;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.Output;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Pattern;

public class KeyTypeData {
    private static final /* synthetic */ int[] -android-icu-impl-locale-KeyTypeData$KeyInfoTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-impl-locale-KeyTypeData$TypeInfoTypeSwitchesValues = null;
    static final /* synthetic */ boolean -assertionsDisabled = (KeyTypeData.class.desiredAssertionStatus() ^ 1);
    private static Map<String, Set<String>> BCP47_KEYS;
    static Set<String> DEPRECATED_KEYS = Collections.emptySet();
    static Map<String, Set<String>> DEPRECATED_KEY_TYPES = Collections.emptyMap();
    private static final Map<String, KeyData> KEYMAP = new HashMap();
    private static final Object[][] KEY_DATA = new Object[0][];
    static Map<String, ValueType> VALUE_TYPES = Collections.emptyMap();

    private static abstract class SpecialTypeHandler {
        /* synthetic */ SpecialTypeHandler(SpecialTypeHandler -this0) {
            this();
        }

        abstract boolean isWellFormed(String str);

        private SpecialTypeHandler() {
        }

        String canonicalize(String value) {
            return AsciiUtil.toLowerString(value);
        }
    }

    private static class CodepointsTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[0-9a-fA-F]{4,6}(-[0-9a-fA-F]{4,6})*");

        /* synthetic */ CodepointsTypeHandler(CodepointsTypeHandler -this0) {
            this();
        }

        private CodepointsTypeHandler() {
            super();
        }

        boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class KeyData {
        String bcpId;
        String legacyId;
        EnumSet<SpecialType> specialTypes;
        Map<String, Type> typeMap;

        KeyData(String legacyId, String bcpId, Map<String, Type> typeMap, EnumSet<SpecialType> specialTypes) {
            this.legacyId = legacyId;
            this.bcpId = bcpId;
            this.typeMap = typeMap;
            this.specialTypes = specialTypes;
        }
    }

    private enum KeyInfoType {
        deprecated,
        valueType
    }

    private static class PrivateUseKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z0-9]{3,8}(-[a-zA-Z0-9]{3,8})*");

        /* synthetic */ PrivateUseKeyValueTypeHandler(PrivateUseKeyValueTypeHandler -this0) {
            this();
        }

        private PrivateUseKeyValueTypeHandler() {
            super();
        }

        boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class ReorderCodeTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z]{3,8}(-[a-zA-Z]{3,8})*");

        /* synthetic */ ReorderCodeTypeHandler(ReorderCodeTypeHandler -this0) {
            this();
        }

        private ReorderCodeTypeHandler() {
            super();
        }

        boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class RgKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("([a-zA-Z]{2}|[0-9]{3})[zZ]{4}");

        /* synthetic */ RgKeyValueTypeHandler(RgKeyValueTypeHandler -this0) {
            this();
        }

        private RgKeyValueTypeHandler() {
            super();
        }

        boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private enum SpecialType {
        CODEPOINTS(new CodepointsTypeHandler()),
        REORDER_CODE(new ReorderCodeTypeHandler()),
        RG_KEY_VALUE(new RgKeyValueTypeHandler()),
        SUBDIVISION_CODE(new SubdivisionKeyValueTypeHandler()),
        PRIVATE_USE(new PrivateUseKeyValueTypeHandler());
        
        SpecialTypeHandler handler;

        private SpecialType(SpecialTypeHandler handler) {
            this.handler = handler;
        }
    }

    private static class SubdivisionKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("([a-zA-Z]{2}|[0-9]{3})");

        /* synthetic */ SubdivisionKeyValueTypeHandler(SubdivisionKeyValueTypeHandler -this0) {
            this();
        }

        private SubdivisionKeyValueTypeHandler() {
            super();
        }

        boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class Type {
        String bcpId;
        String legacyId;

        Type(String legacyId, String bcpId) {
            this.legacyId = legacyId;
            this.bcpId = bcpId;
        }
    }

    private enum TypeInfoType {
        deprecated
    }

    public enum ValueType {
        single,
        multiple,
        incremental,
        any
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-locale-KeyTypeData$KeyInfoTypeSwitchesValues() {
        if (-android-icu-impl-locale-KeyTypeData$KeyInfoTypeSwitchesValues != null) {
            return -android-icu-impl-locale-KeyTypeData$KeyInfoTypeSwitchesValues;
        }
        int[] iArr = new int[KeyInfoType.values().length];
        try {
            iArr[KeyInfoType.deprecated.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[KeyInfoType.valueType.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-impl-locale-KeyTypeData$KeyInfoTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-locale-KeyTypeData$TypeInfoTypeSwitchesValues() {
        if (-android-icu-impl-locale-KeyTypeData$TypeInfoTypeSwitchesValues != null) {
            return -android-icu-impl-locale-KeyTypeData$TypeInfoTypeSwitchesValues;
        }
        int[] iArr = new int[TypeInfoType.values().length];
        try {
            iArr[TypeInfoType.deprecated.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        -android-icu-impl-locale-KeyTypeData$TypeInfoTypeSwitchesValues = iArr;
        return iArr;
    }

    public static String toBcpKey(String key) {
        KeyData keyData = (KeyData) KEYMAP.get(AsciiUtil.toLowerString(key));
        if (keyData != null) {
            return keyData.bcpId;
        }
        return null;
    }

    public static String toLegacyKey(String key) {
        KeyData keyData = (KeyData) KEYMAP.get(AsciiUtil.toLowerString(key));
        if (keyData != null) {
            return keyData.legacyId;
        }
        return null;
    }

    public static String toBcpType(String key, String type, Output<Boolean> isKnownKey, Output<Boolean> isSpecialType) {
        if (isKnownKey != null) {
            isKnownKey.value = Boolean.valueOf(false);
        }
        if (isSpecialType != null) {
            isSpecialType.value = Boolean.valueOf(false);
        }
        key = AsciiUtil.toLowerString(key);
        type = AsciiUtil.toLowerString(type);
        KeyData keyData = (KeyData) KEYMAP.get(key);
        if (keyData != null) {
            if (isKnownKey != null) {
                isKnownKey.value = Boolean.TRUE;
            }
            Type t = (Type) keyData.typeMap.get(type);
            if (t != null) {
                return t.bcpId;
            }
            if (keyData.specialTypes != null) {
                for (SpecialType st : keyData.specialTypes) {
                    if (st.handler.isWellFormed(type)) {
                        if (isSpecialType != null) {
                            isSpecialType.value = Boolean.valueOf(true);
                        }
                        return st.handler.canonicalize(type);
                    }
                }
            }
        }
        return null;
    }

    public static String toLegacyType(String key, String type, Output<Boolean> isKnownKey, Output<Boolean> isSpecialType) {
        if (isKnownKey != null) {
            isKnownKey.value = Boolean.valueOf(false);
        }
        if (isSpecialType != null) {
            isSpecialType.value = Boolean.valueOf(false);
        }
        key = AsciiUtil.toLowerString(key);
        type = AsciiUtil.toLowerString(type);
        KeyData keyData = (KeyData) KEYMAP.get(key);
        if (keyData != null) {
            if (isKnownKey != null) {
                isKnownKey.value = Boolean.TRUE;
            }
            Type t = (Type) keyData.typeMap.get(type);
            if (t != null) {
                return t.legacyId;
            }
            if (keyData.specialTypes != null) {
                for (SpecialType st : keyData.specialTypes) {
                    if (st.handler.isWellFormed(type)) {
                        if (isSpecialType != null) {
                            isSpecialType.value = Boolean.valueOf(true);
                        }
                        return st.handler.canonicalize(type);
                    }
                }
            }
        }
        return null;
    }

    private static void initFromResourceBundle() {
        UResourceBundle keyTypeDataRes = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        getKeyInfo(keyTypeDataRes.get("keyInfo"));
        getTypeInfo(keyTypeDataRes.get("typeInfo"));
        UResourceBundle keyMapRes = keyTypeDataRes.get("keyMap");
        UResourceBundle typeMapRes = keyTypeDataRes.get("typeMap");
        UResourceBundle typeAliasRes = null;
        UResourceBundle bcpTypeAliasRes = null;
        try {
            typeAliasRes = keyTypeDataRes.get("typeAlias");
        } catch (MissingResourceException e) {
        }
        try {
            bcpTypeAliasRes = keyTypeDataRes.get("bcpTypeAlias");
        } catch (MissingResourceException e2) {
        }
        UResourceBundleIterator keyMapItr = keyMapRes.getIterator();
        Map<String, Set<String>> _Bcp47Keys = new LinkedHashMap();
        while (keyMapItr.hasNext()) {
            String from;
            String to;
            Set<String> aliasSet;
            UResourceBundle keyMapEntry = keyMapItr.next();
            String legacyKeyId = keyMapEntry.getKey();
            String bcpKeyId = keyMapEntry.getString();
            boolean hasSameKey = false;
            if (bcpKeyId.length() == 0) {
                bcpKeyId = legacyKeyId;
                hasSameKey = true;
            }
            LinkedHashSet<String> _bcp47Types = new LinkedHashSet();
            _Bcp47Keys.put(bcpKeyId, Collections.unmodifiableSet(_bcp47Types));
            boolean isTZ = legacyKeyId.equals("timezone");
            Map<String, Set<String>> typeAliasMap = null;
            if (typeAliasRes != null) {
                UResourceBundle typeAliasResByKey = null;
                try {
                    typeAliasResByKey = typeAliasRes.get(legacyKeyId);
                } catch (MissingResourceException e3) {
                }
                if (typeAliasResByKey != null) {
                    typeAliasMap = new HashMap();
                    UResourceBundleIterator typeAliasResItr = typeAliasResByKey.getIterator();
                    while (typeAliasResItr.hasNext()) {
                        UResourceBundle typeAliasDataEntry = typeAliasResItr.next();
                        from = typeAliasDataEntry.getKey();
                        to = typeAliasDataEntry.getString();
                        if (isTZ) {
                            from = from.replace(':', '/');
                        }
                        aliasSet = (Set) typeAliasMap.get(to);
                        if (aliasSet == null) {
                            aliasSet = new HashSet();
                            typeAliasMap.put(to, aliasSet);
                        }
                        aliasSet.add(from);
                    }
                }
            }
            Map bcpTypeAliasMap = null;
            if (bcpTypeAliasRes != null) {
                UResourceBundle bcpTypeAliasResByKey = null;
                try {
                    bcpTypeAliasResByKey = bcpTypeAliasRes.get(bcpKeyId);
                } catch (MissingResourceException e4) {
                }
                if (bcpTypeAliasResByKey != null) {
                    bcpTypeAliasMap = new HashMap();
                    UResourceBundleIterator bcpTypeAliasResItr = bcpTypeAliasResByKey.getIterator();
                    while (bcpTypeAliasResItr.hasNext()) {
                        UResourceBundle bcpTypeAliasDataEntry = bcpTypeAliasResItr.next();
                        from = bcpTypeAliasDataEntry.getKey();
                        to = bcpTypeAliasDataEntry.getString();
                        aliasSet = (Set) bcpTypeAliasMap.get(to);
                        if (aliasSet == null) {
                            aliasSet = new HashSet();
                            bcpTypeAliasMap.put(to, aliasSet);
                        }
                        aliasSet.add(from);
                    }
                }
            }
            Map<String, Type> typeDataMap = new HashMap();
            EnumSet enumSet = null;
            UResourceBundle typeMapResByKey = null;
            try {
                typeMapResByKey = typeMapRes.get(legacyKeyId);
            } catch (MissingResourceException e5) {
                if (!-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            if (typeMapResByKey != null) {
                UResourceBundleIterator typeMapResByKeyItr = typeMapResByKey.getIterator();
                while (typeMapResByKeyItr.hasNext()) {
                    UResourceBundle typeMapEntry = typeMapResByKeyItr.next();
                    String legacyTypeId = typeMapEntry.getKey();
                    String bcpTypeId = typeMapEntry.getString();
                    char first = legacyTypeId.charAt(0);
                    boolean isSpecialType = '9' < first && first < 'a' && bcpTypeId.length() == 0;
                    if (isSpecialType) {
                        if (enumSet == null) {
                            enumSet = EnumSet.noneOf(SpecialType.class);
                        }
                        enumSet.add(SpecialType.valueOf(legacyTypeId));
                        _bcp47Types.add(legacyTypeId);
                    } else {
                        if (isTZ) {
                            legacyTypeId = legacyTypeId.replace(':', '/');
                        }
                        boolean hasSameType = false;
                        if (bcpTypeId.length() == 0) {
                            bcpTypeId = legacyTypeId;
                            hasSameType = true;
                        }
                        _bcp47Types.add(bcpTypeId);
                        Type type = new Type(legacyTypeId, bcpTypeId);
                        typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), type);
                        if (!hasSameType) {
                            typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), type);
                        }
                        if (typeAliasMap != null) {
                            Set<String> typeAliasSet = (Set) typeAliasMap.get(legacyTypeId);
                            if (typeAliasSet != null) {
                                for (String alias : typeAliasSet) {
                                    typeDataMap.put(AsciiUtil.toLowerString(alias), type);
                                }
                            }
                        }
                        if (bcpTypeAliasMap != null) {
                            Set<String> bcpTypeAliasSet = (Set) bcpTypeAliasMap.get(bcpTypeId);
                            if (bcpTypeAliasSet != null) {
                                for (String alias2 : bcpTypeAliasSet) {
                                    typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                                }
                            }
                        }
                    }
                }
            }
            KeyData keyData = new KeyData(legacyKeyId, bcpKeyId, typeDataMap, enumSet);
            KEYMAP.put(AsciiUtil.toLowerString(legacyKeyId), keyData);
            if (!hasSameKey) {
                KEYMAP.put(AsciiUtil.toLowerString(bcpKeyId), keyData);
            }
        }
        BCP47_KEYS = Collections.unmodifiableMap(_Bcp47Keys);
    }

    static {
        initFromResourceBundle();
    }

    private static void getKeyInfo(UResourceBundle keyInfoRes) {
        Set<String> _deprecatedKeys = new LinkedHashSet();
        Map<String, ValueType> _valueTypes = new LinkedHashMap();
        UResourceBundleIterator keyInfoIt = keyInfoRes.getIterator();
        while (keyInfoIt.hasNext()) {
            UResourceBundle keyInfoEntry = keyInfoIt.next();
            KeyInfoType keyInfo = KeyInfoType.valueOf(keyInfoEntry.getKey());
            UResourceBundleIterator keyInfoIt2 = keyInfoEntry.getIterator();
            while (keyInfoIt2.hasNext()) {
                UResourceBundle keyInfoEntry2 = keyInfoIt2.next();
                String key2 = keyInfoEntry2.getKey();
                String value2 = keyInfoEntry2.getString();
                switch (-getandroid-icu-impl-locale-KeyTypeData$KeyInfoTypeSwitchesValues()[keyInfo.ordinal()]) {
                    case 1:
                        _deprecatedKeys.add(key2);
                        break;
                    case 2:
                        _valueTypes.put(key2, ValueType.valueOf(value2));
                        break;
                    default:
                        break;
                }
            }
        }
        DEPRECATED_KEYS = Collections.unmodifiableSet(_deprecatedKeys);
        VALUE_TYPES = Collections.unmodifiableMap(_valueTypes);
    }

    private static void getTypeInfo(UResourceBundle typeInfoRes) {
        Map<String, Set<String>> _deprecatedKeyTypes = new LinkedHashMap();
        UResourceBundleIterator keyInfoIt = typeInfoRes.getIterator();
        while (keyInfoIt.hasNext()) {
            UResourceBundle keyInfoEntry = keyInfoIt.next();
            TypeInfoType typeInfo = TypeInfoType.valueOf(keyInfoEntry.getKey());
            UResourceBundleIterator keyInfoIt2 = keyInfoEntry.getIterator();
            while (keyInfoIt2.hasNext()) {
                UResourceBundle keyInfoEntry2 = keyInfoIt2.next();
                String key2 = keyInfoEntry2.getKey();
                Set<String> _deprecatedTypes = new LinkedHashSet();
                UResourceBundleIterator keyInfoIt3 = keyInfoEntry2.getIterator();
                while (keyInfoIt3.hasNext()) {
                    String key3 = keyInfoIt3.next().getKey();
                    switch (-getandroid-icu-impl-locale-KeyTypeData$TypeInfoTypeSwitchesValues()[typeInfo.ordinal()]) {
                        case 1:
                            _deprecatedTypes.add(key3);
                            break;
                        default:
                            break;
                    }
                }
                _deprecatedKeyTypes.put(key2, Collections.unmodifiableSet(_deprecatedTypes));
            }
        }
        DEPRECATED_KEY_TYPES = Collections.unmodifiableMap(_deprecatedKeyTypes);
    }

    /* JADX WARNING: Removed duplicated region for block: B:44:0x0115  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x010d A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void initFromTables() {
        Object[][] objArr = KEY_DATA;
        int i = 0;
        int length = objArr.length;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                int i3;
                String from;
                String to;
                Set<String> aliasSet;
                Object[] keyDataEntry = objArr[i2];
                String legacyKeyId = (String) keyDataEntry[0];
                String bcpKeyId = (String) keyDataEntry[1];
                String[][] typeData = (String[][]) keyDataEntry[2];
                String[][] typeAliasData = (String[][]) keyDataEntry[3];
                String[][] bcpTypeAliasData = (String[][]) keyDataEntry[4];
                boolean hasSameKey = false;
                if (bcpKeyId == null) {
                    bcpKeyId = legacyKeyId;
                    hasSameKey = true;
                }
                Map<String, Set<String>> typeAliasMap = null;
                if (typeAliasData != null) {
                    typeAliasMap = new HashMap();
                    for (String[] typeAliasDataEntry : typeAliasData) {
                        from = typeAliasDataEntry[0];
                        to = typeAliasDataEntry[1];
                        aliasSet = (Set) typeAliasMap.get(to);
                        if (aliasSet == null) {
                            aliasSet = new HashSet();
                            typeAliasMap.put(to, aliasSet);
                        }
                        aliasSet.add(from);
                    }
                }
                Map bcpTypeAliasMap = null;
                if (bcpTypeAliasData != null) {
                    bcpTypeAliasMap = new HashMap();
                    for (String[] bcpTypeAliasDataEntry : bcpTypeAliasData) {
                        from = bcpTypeAliasDataEntry[0];
                        to = bcpTypeAliasDataEntry[1];
                        aliasSet = (Set) bcpTypeAliasMap.get(to);
                        if (aliasSet == null) {
                            aliasSet = new HashSet();
                            bcpTypeAliasMap.put(to, aliasSet);
                        }
                        aliasSet.add(from);
                    }
                }
                if (-assertionsDisabled || typeData != null) {
                    Map<String, Type> typeDataMap = new HashMap();
                    Collection specialTypeSet = null;
                    i = 0;
                    int length2 = typeData.length;
                    while (true) {
                        i3 = i;
                        if (i3 >= length2) {
                            break;
                        }
                        String[] typeDataEntry = typeData[i3];
                        String legacyTypeId = typeDataEntry[0];
                        String bcpTypeId = typeDataEntry[1];
                        boolean isSpecialType = false;
                        SpecialType[] values = SpecialType.values();
                        i = 0;
                        int length3 = values.length;
                        while (i < length3) {
                            SpecialType st = values[i];
                            if (legacyTypeId.equals(st.toString())) {
                                isSpecialType = true;
                                if (specialTypeSet == null) {
                                    specialTypeSet = new HashSet();
                                }
                                specialTypeSet.add(st);
                                if (isSpecialType) {
                                    boolean hasSameType = false;
                                    if (bcpTypeId == null) {
                                        bcpTypeId = legacyTypeId;
                                        hasSameType = true;
                                    }
                                    Type type = new Type(legacyTypeId, bcpTypeId);
                                    typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), type);
                                    if (!hasSameType) {
                                        typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), type);
                                    }
                                    Set<String> typeAliasSet = (Set) typeAliasMap.get(legacyTypeId);
                                    if (typeAliasSet != null) {
                                        for (String alias : typeAliasSet) {
                                            typeDataMap.put(AsciiUtil.toLowerString(alias), type);
                                        }
                                    }
                                    Set<String> bcpTypeAliasSet = (Set) bcpTypeAliasMap.get(bcpTypeId);
                                    if (bcpTypeAliasSet != null) {
                                        for (String alias2 : bcpTypeAliasSet) {
                                            typeDataMap.put(AsciiUtil.toLowerString(alias2), type);
                                        }
                                    }
                                }
                                i = i3 + 1;
                            } else {
                                i++;
                            }
                        }
                        if (isSpecialType) {
                        }
                        i = i3 + 1;
                    }
                    EnumSet<SpecialType> specialTypes = null;
                    if (specialTypeSet != null) {
                        specialTypes = EnumSet.copyOf(specialTypeSet);
                    }
                    KeyData keyData = new KeyData(legacyKeyId, bcpKeyId, typeDataMap, specialTypes);
                    KEYMAP.put(AsciiUtil.toLowerString(legacyKeyId), keyData);
                    if (!hasSameKey) {
                        KEYMAP.put(AsciiUtil.toLowerString(bcpKeyId), keyData);
                    }
                    i = i2 + 1;
                } else {
                    throw new AssertionError();
                }
            }
            return;
        }
    }

    public static Set<String> getBcp47Keys() {
        return BCP47_KEYS.keySet();
    }

    public static Set<String> getBcp47KeyTypes(String key) {
        return (Set) BCP47_KEYS.get(key);
    }

    public static boolean isDeprecated(String key) {
        return DEPRECATED_KEYS.contains(key);
    }

    public static boolean isDeprecated(String key, String type) {
        Set<String> deprecatedTypes = (Set) DEPRECATED_KEY_TYPES.get(key);
        if (deprecatedTypes == null) {
            return false;
        }
        return deprecatedTypes.contains(type);
    }

    public static ValueType getValueType(String key) {
        ValueType type = (ValueType) VALUE_TYPES.get(key);
        return type == null ? ValueType.single : type;
    }
}

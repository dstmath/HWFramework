package android.icu.impl.locale;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.Output;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.regex.Pattern;

public class KeyTypeData {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static Map<String, Set<String>> BCP47_KEYS;
    static Set<String> DEPRECATED_KEYS = Collections.emptySet();
    static Map<String, Set<String>> DEPRECATED_KEY_TYPES = Collections.emptyMap();
    private static final Map<String, KeyData> KEYMAP = new HashMap();
    private static final Object[][] KEY_DATA = new Object[0][];
    static Map<String, ValueType> VALUE_TYPES = Collections.emptyMap();

    /* renamed from: android.icu.impl.locale.KeyTypeData$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$icu$impl$locale$KeyTypeData$TypeInfoType = new int[TypeInfoType.values().length];

        static {
            try {
                $SwitchMap$android$icu$impl$locale$KeyTypeData$TypeInfoType[TypeInfoType.deprecated.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            $SwitchMap$android$icu$impl$locale$KeyTypeData$KeyInfoType = new int[KeyInfoType.values().length];
            try {
                $SwitchMap$android$icu$impl$locale$KeyTypeData$KeyInfoType[KeyInfoType.deprecated.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$icu$impl$locale$KeyTypeData$KeyInfoType[KeyInfoType.valueType.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private static class CodepointsTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[0-9a-fA-F]{4,6}(-[0-9a-fA-F]{4,6})*");

        private CodepointsTypeHandler() {
            super(null);
        }

        /* synthetic */ CodepointsTypeHandler(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class KeyData {
        String bcpId;
        String legacyId;
        EnumSet<SpecialType> specialTypes;
        Map<String, Type> typeMap;

        KeyData(String legacyId2, String bcpId2, Map<String, Type> typeMap2, EnumSet<SpecialType> specialTypes2) {
            this.legacyId = legacyId2;
            this.bcpId = bcpId2;
            this.typeMap = typeMap2;
            this.specialTypes = specialTypes2;
        }
    }

    private enum KeyInfoType {
        deprecated,
        valueType
    }

    private static class PrivateUseKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z0-9]{3,8}(-[a-zA-Z0-9]{3,8})*");

        private PrivateUseKeyValueTypeHandler() {
            super(null);
        }

        /* synthetic */ PrivateUseKeyValueTypeHandler(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class ReorderCodeTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z]{3,8}(-[a-zA-Z]{3,8})*");

        private ReorderCodeTypeHandler() {
            super(null);
        }

        /* synthetic */ ReorderCodeTypeHandler(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class RgKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("([a-zA-Z]{2}|[0-9]{3})[zZ]{4}");

        private RgKeyValueTypeHandler() {
            super(null);
        }

        /* synthetic */ RgKeyValueTypeHandler(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private enum SpecialType {
        CODEPOINTS(new CodepointsTypeHandler(null)),
        REORDER_CODE(new ReorderCodeTypeHandler(null)),
        RG_KEY_VALUE(new RgKeyValueTypeHandler(null)),
        SUBDIVISION_CODE(new SubdivisionKeyValueTypeHandler(null)),
        PRIVATE_USE(new PrivateUseKeyValueTypeHandler(null));
        
        SpecialTypeHandler handler;

        private SpecialType(SpecialTypeHandler handler2) {
            this.handler = handler2;
        }
    }

    private static abstract class SpecialTypeHandler {
        /* access modifiers changed from: package-private */
        public abstract boolean isWellFormed(String str);

        private SpecialTypeHandler() {
        }

        /* synthetic */ SpecialTypeHandler(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public String canonicalize(String value) {
            return AsciiUtil.toLowerString(value);
        }
    }

    private static class SubdivisionKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("([a-zA-Z]{2}|[0-9]{3})");

        private SubdivisionKeyValueTypeHandler() {
            super(null);
        }

        /* synthetic */ SubdivisionKeyValueTypeHandler(AnonymousClass1 x0) {
            this();
        }

        /* access modifiers changed from: package-private */
        public boolean isWellFormed(String value) {
            return pat.matcher(value).matches();
        }
    }

    private static class Type {
        String bcpId;
        String legacyId;

        Type(String legacyId2, String bcpId2) {
            this.legacyId = legacyId2;
            this.bcpId = bcpId2;
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

    static {
        initFromResourceBundle();
    }

    public static String toBcpKey(String key) {
        KeyData keyData = KEYMAP.get(AsciiUtil.toLowerString(key));
        if (keyData != null) {
            return keyData.bcpId;
        }
        return null;
    }

    public static String toLegacyKey(String key) {
        KeyData keyData = KEYMAP.get(AsciiUtil.toLowerString(key));
        if (keyData != null) {
            return keyData.legacyId;
        }
        return null;
    }

    public static String toBcpType(String key, String type, Output<Boolean> isKnownKey, Output<Boolean> isSpecialType) {
        if (isKnownKey != null) {
            isKnownKey.value = false;
        }
        if (isSpecialType != null) {
            isSpecialType.value = false;
        }
        String key2 = AsciiUtil.toLowerString(key);
        String type2 = AsciiUtil.toLowerString(type);
        KeyData keyData = KEYMAP.get(key2);
        if (keyData != null) {
            if (isKnownKey != null) {
                isKnownKey.value = Boolean.TRUE;
            }
            Type t = keyData.typeMap.get(type2);
            if (t != null) {
                return t.bcpId;
            }
            if (keyData.specialTypes != null) {
                Iterator it = keyData.specialTypes.iterator();
                while (it.hasNext()) {
                    SpecialType st = (SpecialType) it.next();
                    if (st.handler.isWellFormed(type2)) {
                        if (isSpecialType != null) {
                            isSpecialType.value = true;
                        }
                        return st.handler.canonicalize(type2);
                    }
                }
            }
        }
        return null;
    }

    public static String toLegacyType(String key, String type, Output<Boolean> isKnownKey, Output<Boolean> isSpecialType) {
        if (isKnownKey != null) {
            isKnownKey.value = false;
        }
        if (isSpecialType != null) {
            isSpecialType.value = false;
        }
        String key2 = AsciiUtil.toLowerString(key);
        String type2 = AsciiUtil.toLowerString(type);
        KeyData keyData = KEYMAP.get(key2);
        if (keyData != null) {
            if (isKnownKey != null) {
                isKnownKey.value = Boolean.TRUE;
            }
            Type t = keyData.typeMap.get(type2);
            if (t != null) {
                return t.legacyId;
            }
            if (keyData.specialTypes != null) {
                Iterator it = keyData.specialTypes.iterator();
                while (it.hasNext()) {
                    SpecialType st = (SpecialType) it.next();
                    if (st.handler.isWellFormed(type2)) {
                        if (isSpecialType != null) {
                            isSpecialType.value = true;
                        }
                        return st.handler.canonicalize(type2);
                    }
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:0x0218  */
    private static void initFromResourceBundle() {
        UResourceBundle bcpTypeAliasRes;
        UResourceBundle typeMapRes;
        UResourceBundle typeMapResByKey;
        UResourceBundle bcpTypeAliasRes2;
        UResourceBundle typeMapRes2;
        EnumSet<SpecialType> specialTypeSet;
        UResourceBundle typeMapResByKey2;
        Set<String> aliasSet;
        UResourceBundle typeAliasResByKey;
        UResourceBundleIterator typeAliasResItr;
        UResourceBundle typeAliasRes;
        UResourceBundle keyTypeDataRes = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        getKeyInfo(keyTypeDataRes.get("keyInfo"));
        getTypeInfo(keyTypeDataRes.get("typeInfo"));
        UResourceBundle keyMapRes = keyTypeDataRes.get("keyMap");
        UResourceBundle typeMapRes3 = keyTypeDataRes.get("typeMap");
        UResourceBundle typeAliasRes2 = null;
        UResourceBundle bcpTypeAliasRes3 = null;
        try {
            typeAliasRes2 = keyTypeDataRes.get("typeAlias");
        } catch (MissingResourceException e) {
        }
        try {
            bcpTypeAliasRes3 = keyTypeDataRes.get("bcpTypeAlias");
        } catch (MissingResourceException e2) {
        }
        UResourceBundleIterator keyMapItr = keyMapRes.getIterator();
        Map<String, Set<String>> _Bcp47Keys = new LinkedHashMap<>();
        while (true) {
            Map<String, Set<String>> _Bcp47Keys2 = _Bcp47Keys;
            if (keyMapItr.hasNext()) {
                UResourceBundle keyMapEntry = keyMapItr.next();
                String legacyKeyId = keyMapEntry.getKey();
                String bcpKeyId = keyMapEntry.getString();
                boolean hasSameKey = false;
                if (bcpKeyId.length() == 0) {
                    bcpKeyId = legacyKeyId;
                    hasSameKey = true;
                }
                boolean hasSameKey2 = hasSameKey;
                String bcpKeyId2 = bcpKeyId;
                LinkedHashSet linkedHashSet = new LinkedHashSet();
                _Bcp47Keys2.put(bcpKeyId2, Collections.unmodifiableSet(linkedHashSet));
                boolean isTZ = legacyKeyId.equals("timezone");
                Map<String, Set<String>> typeAliasMap = null;
                if (typeAliasRes2 != null) {
                    try {
                        typeAliasResByKey = typeAliasRes2.get(legacyKeyId);
                    } catch (MissingResourceException e3) {
                        typeAliasResByKey = null;
                    }
                    if (typeAliasResByKey != null) {
                        typeAliasMap = new HashMap<>();
                        UResourceBundleIterator typeAliasResItr2 = typeAliasResByKey.getIterator();
                        while (typeAliasResItr2.hasNext()) {
                            UResourceBundle typeAliasResByKey2 = typeAliasResByKey;
                            UResourceBundle typeAliasDataEntry = typeAliasResItr2.next();
                            UResourceBundle keyTypeDataRes2 = keyTypeDataRes;
                            String from = typeAliasDataEntry.getKey();
                            UResourceBundle keyMapRes2 = keyMapRes;
                            String to = typeAliasDataEntry.getString();
                            if (isTZ) {
                                typeAliasRes = typeAliasRes2;
                                typeAliasResItr = typeAliasResItr2;
                                from = from.replace(':', '/');
                            } else {
                                typeAliasRes = typeAliasRes2;
                                typeAliasResItr = typeAliasResItr2;
                            }
                            Set<String> aliasSet2 = typeAliasMap.get(to);
                            if (aliasSet2 == null) {
                                aliasSet2 = new HashSet<>();
                                typeAliasMap.put(to, aliasSet2);
                            }
                            aliasSet2.add(from);
                            typeAliasResByKey = typeAliasResByKey2;
                            keyTypeDataRes = keyTypeDataRes2;
                            keyMapRes = keyMapRes2;
                            typeAliasRes2 = typeAliasRes;
                            typeAliasResItr2 = typeAliasResItr;
                        }
                    }
                }
                UResourceBundle keyTypeDataRes3 = keyTypeDataRes;
                UResourceBundle keyMapRes3 = keyMapRes;
                UResourceBundle typeAliasRes3 = typeAliasRes2;
                Map<String, Set<String>> bcpTypeAliasMap = null;
                if (bcpTypeAliasRes != null) {
                    UResourceBundle bcpTypeAliasResByKey = null;
                    try {
                        bcpTypeAliasResByKey = bcpTypeAliasRes.get(bcpKeyId2);
                    } catch (MissingResourceException e4) {
                    }
                    if (bcpTypeAliasResByKey != null) {
                        bcpTypeAliasMap = new HashMap<>();
                        UResourceBundleIterator bcpTypeAliasResItr = bcpTypeAliasResByKey.getIterator();
                        while (bcpTypeAliasResItr.hasNext()) {
                            UResourceBundle bcpTypeAliasDataEntry = bcpTypeAliasResItr.next();
                            String from2 = bcpTypeAliasDataEntry.getKey();
                            UResourceBundleIterator bcpTypeAliasResItr2 = bcpTypeAliasResItr;
                            String to2 = bcpTypeAliasDataEntry.getString();
                            Set<String> aliasSet3 = bcpTypeAliasMap.get(to2);
                            if (aliasSet3 == null) {
                                UResourceBundle uResourceBundle = bcpTypeAliasDataEntry;
                                aliasSet = new HashSet<>();
                                bcpTypeAliasMap.put(to2, aliasSet);
                            } else {
                                aliasSet = aliasSet3;
                            }
                            aliasSet.add(from2);
                            bcpTypeAliasResItr = bcpTypeAliasResItr2;
                        }
                    }
                }
                Map<String, Type> typeDataMap = new HashMap<>();
                EnumSet<SpecialType> specialTypeSet2 = null;
                try {
                    typeMapResByKey = typeMapRes.get(legacyKeyId);
                } catch (MissingResourceException e5) {
                    MissingResourceException missingResourceException = e5;
                    typeMapResByKey = null;
                }
                if (typeMapResByKey != null) {
                    UResourceBundleIterator typeMapResByKeyItr = typeMapResByKey.getIterator();
                    while (true) {
                        UResourceBundleIterator typeMapResByKeyItr2 = typeMapResByKeyItr;
                        if (!typeMapResByKeyItr2.hasNext()) {
                            break;
                        }
                        UResourceBundle typeMapResByKey3 = typeMapResByKey;
                        UResourceBundle typeMapResByKey4 = typeMapResByKeyItr2.next();
                        UResourceBundle typeMapRes4 = typeMapRes;
                        String legacyTypeId = typeMapResByKey4.getKey();
                        UResourceBundleIterator typeMapResByKeyItr3 = typeMapResByKeyItr2;
                        String bcpTypeId = typeMapResByKey4.getString();
                        UResourceBundle uResourceBundle2 = typeMapResByKey4;
                        UResourceBundle bcpTypeAliasRes4 = bcpTypeAliasRes;
                        char first = legacyTypeId.charAt(0);
                        boolean isSpecialType = '9' < first && first < 'a' && bcpTypeId.length() == 0;
                        if (isSpecialType) {
                            if (specialTypeSet2 == null) {
                                boolean z = isSpecialType;
                                specialTypeSet2 = EnumSet.noneOf(SpecialType.class);
                            }
                            specialTypeSet2.add(SpecialType.valueOf(legacyTypeId));
                            linkedHashSet.add(legacyTypeId);
                            typeMapResByKey2 = typeMapResByKey3;
                            typeMapRes = typeMapRes4;
                            typeMapResByKeyItr = typeMapResByKeyItr3;
                            bcpTypeAliasRes = bcpTypeAliasRes4;
                        } else {
                            boolean z2 = isSpecialType;
                            if (isTZ) {
                                specialTypeSet = specialTypeSet2;
                                char c = first;
                                legacyTypeId = legacyTypeId.replace(':', '/');
                            } else {
                                specialTypeSet = specialTypeSet2;
                                char c2 = first;
                            }
                            boolean hasSameType = false;
                            if (bcpTypeId.length() == 0) {
                                bcpTypeId = legacyTypeId;
                                hasSameType = true;
                            }
                            linkedHashSet.add(bcpTypeId);
                            Type t = new Type(legacyTypeId, bcpTypeId);
                            typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), t);
                            if (!hasSameType) {
                                typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId), t);
                            }
                            if (typeAliasMap != null) {
                                Set<String> typeAliasSet = typeAliasMap.get(legacyTypeId);
                                if (typeAliasSet != null) {
                                    boolean z3 = hasSameType;
                                    Iterator<String> it = typeAliasSet.iterator();
                                    while (it.hasNext()) {
                                        typeDataMap.put(AsciiUtil.toLowerString(it.next()), t);
                                        it = it;
                                        legacyTypeId = legacyTypeId;
                                    }
                                    if (bcpTypeAliasMap != null) {
                                        Set<String> bcpTypeAliasSet = bcpTypeAliasMap.get(bcpTypeId);
                                        if (bcpTypeAliasSet != null) {
                                            for (String alias : bcpTypeAliasSet) {
                                                typeDataMap.put(AsciiUtil.toLowerString(alias), t);
                                                bcpTypeAliasSet = bcpTypeAliasSet;
                                            }
                                        }
                                    }
                                    typeMapResByKey2 = typeMapResByKey3;
                                    typeMapRes = typeMapRes4;
                                    typeMapResByKeyItr = typeMapResByKeyItr3;
                                    bcpTypeAliasRes = bcpTypeAliasRes4;
                                    specialTypeSet2 = specialTypeSet;
                                }
                            }
                            String str = legacyTypeId;
                            if (bcpTypeAliasMap != null) {
                            }
                            typeMapResByKey2 = typeMapResByKey3;
                            typeMapRes = typeMapRes4;
                            typeMapResByKeyItr = typeMapResByKeyItr3;
                            bcpTypeAliasRes = bcpTypeAliasRes4;
                            specialTypeSet2 = specialTypeSet;
                        }
                    }
                    typeMapRes2 = typeMapRes;
                    EnumSet<SpecialType> enumSet = specialTypeSet2;
                    bcpTypeAliasRes2 = bcpTypeAliasRes;
                } else {
                    typeMapRes2 = typeMapRes;
                    bcpTypeAliasRes2 = bcpTypeAliasRes;
                }
                KeyData keyData = new KeyData(legacyKeyId, bcpKeyId2, typeDataMap, specialTypeSet2);
                KEYMAP.put(AsciiUtil.toLowerString(legacyKeyId), keyData);
                if (!hasSameKey2) {
                    KEYMAP.put(AsciiUtil.toLowerString(bcpKeyId2), keyData);
                }
                _Bcp47Keys = _Bcp47Keys2;
                keyTypeDataRes = keyTypeDataRes3;
                keyMapRes = keyMapRes3;
                typeAliasRes2 = typeAliasRes3;
                typeMapRes3 = typeMapRes2;
                bcpTypeAliasRes3 = bcpTypeAliasRes2;
            } else {
                UResourceBundle uResourceBundle3 = keyMapRes;
                UResourceBundle uResourceBundle4 = typeMapRes;
                UResourceBundle uResourceBundle5 = typeAliasRes2;
                UResourceBundle uResourceBundle6 = bcpTypeAliasRes;
                BCP47_KEYS = Collections.unmodifiableMap(_Bcp47Keys2);
                return;
            }
        }
    }

    private static void getKeyInfo(UResourceBundle keyInfoRes) {
        Set<String> _deprecatedKeys = new LinkedHashSet<>();
        Map<String, ValueType> _valueTypes = new LinkedHashMap<>();
        UResourceBundleIterator keyInfoIt = keyInfoRes.getIterator();
        while (keyInfoIt.hasNext()) {
            UResourceBundle keyInfoEntry = keyInfoIt.next();
            KeyInfoType keyInfo = KeyInfoType.valueOf(keyInfoEntry.getKey());
            UResourceBundleIterator keyInfoIt2 = keyInfoEntry.getIterator();
            while (keyInfoIt2.hasNext()) {
                UResourceBundle keyInfoEntry2 = keyInfoIt2.next();
                String key2 = keyInfoEntry2.getKey();
                String value2 = keyInfoEntry2.getString();
                switch (keyInfo) {
                    case deprecated:
                        _deprecatedKeys.add(key2);
                        break;
                    case valueType:
                        _valueTypes.put(key2, ValueType.valueOf(value2));
                        break;
                }
            }
        }
        DEPRECATED_KEYS = Collections.unmodifiableSet(_deprecatedKeys);
        VALUE_TYPES = Collections.unmodifiableMap(_valueTypes);
    }

    private static void getTypeInfo(UResourceBundle typeInfoRes) {
        Map<String, Set<String>> _deprecatedKeyTypes = new LinkedHashMap<>();
        UResourceBundleIterator keyInfoIt = typeInfoRes.getIterator();
        while (keyInfoIt.hasNext()) {
            UResourceBundle keyInfoEntry = keyInfoIt.next();
            TypeInfoType typeInfo = TypeInfoType.valueOf(keyInfoEntry.getKey());
            UResourceBundleIterator keyInfoIt2 = keyInfoEntry.getIterator();
            while (keyInfoIt2.hasNext()) {
                UResourceBundle keyInfoEntry2 = keyInfoIt2.next();
                String key2 = keyInfoEntry2.getKey();
                Set<String> _deprecatedTypes = new LinkedHashSet<>();
                UResourceBundleIterator keyInfoIt3 = keyInfoEntry2.getIterator();
                while (keyInfoIt3.hasNext()) {
                    String key3 = keyInfoIt3.next().getKey();
                    if (AnonymousClass1.$SwitchMap$android$icu$impl$locale$KeyTypeData$TypeInfoType[typeInfo.ordinal()] == 1) {
                        _deprecatedTypes.add(key3);
                    }
                }
                _deprecatedKeyTypes.put(key2, Collections.unmodifiableSet(_deprecatedTypes));
            }
        }
        DEPRECATED_KEY_TYPES = Collections.unmodifiableMap(_deprecatedKeyTypes);
    }

    private static void initFromTables() {
        String[][] typeAliasData;
        int i;
        Set<String> aliasSet;
        Object[][] objArr = KEY_DATA;
        int length = objArr.length;
        int i2 = 0;
        int i3 = 0;
        while (i3 < length) {
            Object[] keyDataEntry = objArr[i3];
            String legacyKeyId = (String) keyDataEntry[i2];
            char c = 1;
            String bcpKeyId = (String) keyDataEntry[1];
            String[][] typeData = (String[][]) keyDataEntry[2];
            String[][] typeAliasData2 = (String[][]) keyDataEntry[3];
            String[][] bcpTypeAliasData = (String[][]) keyDataEntry[4];
            boolean hasSameKey = false;
            if (bcpKeyId == null) {
                bcpKeyId = legacyKeyId;
                hasSameKey = true;
            }
            Map<String, Set<String>> typeAliasMap = null;
            if (typeAliasData2 != null) {
                typeAliasMap = new HashMap<>();
                int length2 = typeAliasData2.length;
                int i4 = i2;
                while (i4 < length2) {
                    String[] typeAliasDataEntry = typeAliasData2[i4];
                    Object[][] objArr2 = objArr;
                    String from = typeAliasDataEntry[i2];
                    String to = typeAliasDataEntry[c];
                    Set<String> aliasSet2 = typeAliasMap.get(to);
                    if (aliasSet2 == null) {
                        aliasSet = new HashSet<>();
                        typeAliasMap.put(to, aliasSet);
                    } else {
                        aliasSet = aliasSet2;
                    }
                    aliasSet.add(from);
                    i4++;
                    objArr = objArr2;
                    i2 = 0;
                    c = 1;
                }
            }
            Object[][] objArr3 = objArr;
            Map<String, Set<String>> bcpTypeAliasMap = null;
            if (bcpTypeAliasData != null) {
                bcpTypeAliasMap = new HashMap<>();
                int length3 = bcpTypeAliasData.length;
                int i5 = 0;
                while (i5 < length3) {
                    String[] bcpTypeAliasDataEntry = bcpTypeAliasData[i5];
                    String from2 = bcpTypeAliasDataEntry[0];
                    int i6 = length;
                    String to2 = bcpTypeAliasDataEntry[1];
                    Set<String> aliasSet3 = bcpTypeAliasMap.get(to2);
                    if (aliasSet3 == null) {
                        i = length3;
                        aliasSet3 = new HashSet<>();
                        bcpTypeAliasMap.put(to2, aliasSet3);
                    } else {
                        i = length3;
                    }
                    aliasSet3.add(from2);
                    i5++;
                    length = i6;
                    length3 = i;
                }
            }
            int i7 = length;
            Map<String, Type> typeDataMap = new HashMap<>();
            int length4 = typeData.length;
            Set<SpecialType> specialTypeSet = null;
            int i8 = 0;
            while (i8 < length4) {
                String[] typeDataEntry = typeData[i8];
                Object[] keyDataEntry2 = keyDataEntry;
                String legacyTypeId = typeDataEntry[0];
                String bcpTypeId = typeDataEntry[1];
                boolean isSpecialType = false;
                SpecialType[] values = SpecialType.values();
                int i9 = length4;
                int length5 = values.length;
                String[][] typeData2 = typeData;
                int i10 = 0;
                while (true) {
                    if (i10 >= length5) {
                        typeAliasData = typeAliasData2;
                        break;
                    }
                    int i11 = length5;
                    SpecialType st = values[i10];
                    typeAliasData = typeAliasData2;
                    if (legacyTypeId.equals(st.toString())) {
                        isSpecialType = true;
                        if (specialTypeSet == null) {
                            specialTypeSet = new HashSet<>();
                        }
                        specialTypeSet.add(st);
                    } else {
                        i10++;
                        length5 = i11;
                        typeAliasData2 = typeAliasData;
                    }
                }
                if (!isSpecialType) {
                    boolean hasSameType = false;
                    if (bcpTypeId == null) {
                        bcpTypeId = legacyTypeId;
                        hasSameType = true;
                    }
                    boolean hasSameType2 = hasSameType;
                    String bcpTypeId2 = bcpTypeId;
                    Type t = new Type(legacyTypeId, bcpTypeId2);
                    typeDataMap.put(AsciiUtil.toLowerString(legacyTypeId), t);
                    if (!hasSameType2) {
                        typeDataMap.put(AsciiUtil.toLowerString(bcpTypeId2), t);
                    }
                    Set<String> typeAliasSet = typeAliasMap.get(legacyTypeId);
                    if (typeAliasSet != null) {
                        String str = legacyTypeId;
                        Iterator<String> it = typeAliasSet.iterator();
                        while (it.hasNext()) {
                            typeDataMap.put(AsciiUtil.toLowerString(it.next()), t);
                            it = it;
                            hasSameType2 = hasSameType2;
                        }
                    } else {
                        boolean z = hasSameType2;
                    }
                    Set<String> bcpTypeAliasSet = bcpTypeAliasMap.get(bcpTypeId2);
                    if (bcpTypeAliasSet != null) {
                        for (String alias : bcpTypeAliasSet) {
                            typeDataMap.put(AsciiUtil.toLowerString(alias), t);
                            bcpTypeAliasMap = bcpTypeAliasMap;
                            bcpTypeAliasSet = bcpTypeAliasSet;
                        }
                    }
                }
                i8++;
                keyDataEntry = keyDataEntry2;
                length4 = i9;
                typeData = typeData2;
                typeAliasData2 = typeAliasData;
                bcpTypeAliasMap = bcpTypeAliasMap;
            }
            Object[] objArr4 = keyDataEntry;
            String[][] strArr = typeData;
            String[][] strArr2 = typeAliasData2;
            EnumSet<SpecialType> specialTypes = null;
            if (specialTypeSet != null) {
                specialTypes = EnumSet.copyOf(specialTypeSet);
            }
            KeyData keyData = new KeyData(legacyKeyId, bcpKeyId, typeDataMap, specialTypes);
            KEYMAP.put(AsciiUtil.toLowerString(legacyKeyId), keyData);
            if (!hasSameKey) {
                KEYMAP.put(AsciiUtil.toLowerString(bcpKeyId), keyData);
            }
            i3++;
            objArr = objArr3;
            length = i7;
            i2 = 0;
        }
    }

    public static Set<String> getBcp47Keys() {
        return BCP47_KEYS.keySet();
    }

    public static Set<String> getBcp47KeyTypes(String key) {
        return BCP47_KEYS.get(key);
    }

    public static boolean isDeprecated(String key) {
        return DEPRECATED_KEYS.contains(key);
    }

    public static boolean isDeprecated(String key, String type) {
        Set<String> deprecatedTypes = DEPRECATED_KEY_TYPES.get(key);
        if (deprecatedTypes == null) {
            return false;
        }
        return deprecatedTypes.contains(type);
    }

    public static ValueType getValueType(String key) {
        ValueType type = VALUE_TYPES.get(key);
        return type == null ? ValueType.single : type;
    }
}

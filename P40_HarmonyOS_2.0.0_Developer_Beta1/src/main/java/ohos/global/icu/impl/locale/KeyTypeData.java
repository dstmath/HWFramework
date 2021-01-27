package ohos.global.icu.impl.locale;

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
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.UResourceBundle;
import ohos.global.icu.util.UResourceBundleIterator;

public class KeyTypeData {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static Map<String, Set<String>> BCP47_KEYS;
    static Set<String> DEPRECATED_KEYS = Collections.emptySet();
    static Map<String, Set<String>> DEPRECATED_KEY_TYPES = Collections.emptyMap();
    private static final Map<String, KeyData> KEYMAP = new HashMap();
    private static final Object[][] KEY_DATA = new Object[0][];
    static Map<String, ValueType> VALUE_TYPES = Collections.emptyMap();

    /* access modifiers changed from: private */
    public enum KeyInfoType {
        deprecated,
        valueType
    }

    /* access modifiers changed from: private */
    public enum TypeInfoType {
        deprecated
    }

    public enum ValueType {
        single,
        multiple,
        incremental,
        any
    }

    private static abstract class SpecialTypeHandler {
        /* access modifiers changed from: package-private */
        public abstract boolean isWellFormed(String str);

        private SpecialTypeHandler() {
        }

        /* synthetic */ SpecialTypeHandler(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: package-private */
        public String canonicalize(String str) {
            return AsciiUtil.toLowerString(str);
        }
    }

    private static class CodepointsTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[0-9a-fA-F]{4,6}(-[0-9a-fA-F]{4,6})*");

        private CodepointsTypeHandler() {
            super(null);
        }

        /* synthetic */ CodepointsTypeHandler(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.locale.KeyTypeData.SpecialTypeHandler
        public boolean isWellFormed(String str) {
            return pat.matcher(str).matches();
        }
    }

    private static class ReorderCodeTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z]{3,8}(-[a-zA-Z]{3,8})*");

        private ReorderCodeTypeHandler() {
            super(null);
        }

        /* synthetic */ ReorderCodeTypeHandler(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.locale.KeyTypeData.SpecialTypeHandler
        public boolean isWellFormed(String str) {
            return pat.matcher(str).matches();
        }
    }

    private static class RgKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("([a-zA-Z]{2}|[0-9]{3})[zZ]{4}");

        private RgKeyValueTypeHandler() {
            super(null);
        }

        /* synthetic */ RgKeyValueTypeHandler(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.locale.KeyTypeData.SpecialTypeHandler
        public boolean isWellFormed(String str) {
            return pat.matcher(str).matches();
        }
    }

    private static class SubdivisionKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("([a-zA-Z]{2}|[0-9]{3})");

        private SubdivisionKeyValueTypeHandler() {
            super(null);
        }

        /* synthetic */ SubdivisionKeyValueTypeHandler(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.locale.KeyTypeData.SpecialTypeHandler
        public boolean isWellFormed(String str) {
            return pat.matcher(str).matches();
        }
    }

    private static class PrivateUseKeyValueTypeHandler extends SpecialTypeHandler {
        private static final Pattern pat = Pattern.compile("[a-zA-Z0-9]{3,8}(-[a-zA-Z0-9]{3,8})*");

        private PrivateUseKeyValueTypeHandler() {
            super(null);
        }

        /* synthetic */ PrivateUseKeyValueTypeHandler(AnonymousClass1 r1) {
            this();
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.impl.locale.KeyTypeData.SpecialTypeHandler
        public boolean isWellFormed(String str) {
            return pat.matcher(str).matches();
        }
    }

    /* access modifiers changed from: private */
    public enum SpecialType {
        CODEPOINTS(new CodepointsTypeHandler(null)),
        REORDER_CODE(new ReorderCodeTypeHandler(null)),
        RG_KEY_VALUE(new RgKeyValueTypeHandler(null)),
        SUBDIVISION_CODE(new SubdivisionKeyValueTypeHandler(null)),
        PRIVATE_USE(new PrivateUseKeyValueTypeHandler(null));
        
        SpecialTypeHandler handler;

        private SpecialType(SpecialTypeHandler specialTypeHandler) {
            this.handler = specialTypeHandler;
        }
    }

    /* access modifiers changed from: private */
    public static class KeyData {
        String bcpId;
        String legacyId;
        EnumSet<SpecialType> specialTypes;
        Map<String, Type> typeMap;

        KeyData(String str, String str2, Map<String, Type> map, EnumSet<SpecialType> enumSet) {
            this.legacyId = str;
            this.bcpId = str2;
            this.typeMap = map;
            this.specialTypes = enumSet;
        }
    }

    /* access modifiers changed from: private */
    public static class Type {
        String bcpId;
        String legacyId;

        Type(String str, String str2) {
            this.legacyId = str;
            this.bcpId = str2;
        }
    }

    public static String toBcpKey(String str) {
        KeyData keyData = KEYMAP.get(AsciiUtil.toLowerString(str));
        if (keyData != null) {
            return keyData.bcpId;
        }
        return null;
    }

    public static String toLegacyKey(String str) {
        KeyData keyData = KEYMAP.get(AsciiUtil.toLowerString(str));
        if (keyData != null) {
            return keyData.legacyId;
        }
        return null;
    }

    public static String toBcpType(String str, String str2, Output<Boolean> output, Output<Boolean> output2) {
        if (output != null) {
            output.value = false;
        }
        if (output2 != null) {
            output2.value = false;
        }
        String lowerString = AsciiUtil.toLowerString(str);
        String lowerString2 = AsciiUtil.toLowerString(str2);
        KeyData keyData = KEYMAP.get(lowerString);
        if (keyData == null) {
            return null;
        }
        if (output != null) {
            output.value = Boolean.TRUE;
        }
        Type type = keyData.typeMap.get(lowerString2);
        if (type != null) {
            return type.bcpId;
        }
        if (keyData.specialTypes == null) {
            return null;
        }
        Iterator it = keyData.specialTypes.iterator();
        while (it.hasNext()) {
            SpecialType specialType = (SpecialType) it.next();
            if (specialType.handler.isWellFormed(lowerString2)) {
                if (output2 != null) {
                    output2.value = true;
                }
                return specialType.handler.canonicalize(lowerString2);
            }
        }
        return null;
    }

    public static String toLegacyType(String str, String str2, Output<Boolean> output, Output<Boolean> output2) {
        if (output != null) {
            output.value = false;
        }
        if (output2 != null) {
            output2.value = false;
        }
        String lowerString = AsciiUtil.toLowerString(str);
        String lowerString2 = AsciiUtil.toLowerString(str2);
        KeyData keyData = KEYMAP.get(lowerString);
        if (keyData == null) {
            return null;
        }
        if (output != null) {
            output.value = Boolean.TRUE;
        }
        Type type = keyData.typeMap.get(lowerString2);
        if (type != null) {
            return type.legacyId;
        }
        if (keyData.specialTypes == null) {
            return null;
        }
        Iterator it = keyData.specialTypes.iterator();
        while (it.hasNext()) {
            SpecialType specialType = (SpecialType) it.next();
            if (specialType.handler.isWellFormed(lowerString2)) {
                if (output2 != null) {
                    output2.value = true;
                }
                return specialType.handler.canonicalize(lowerString2);
            }
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:101:0x01e9  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x01f2 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c1 A[SYNTHETIC, Splitter:B:37:0x00c1] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x010b  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01d2  */
    private static void initFromResourceBundle() {
        UResourceBundle uResourceBundle;
        UResourceBundle uResourceBundle2;
        boolean z;
        HashMap hashMap;
        HashMap hashMap2;
        UResourceBundle uResourceBundle3;
        UResourceBundleIterator uResourceBundleIterator;
        UResourceBundle uResourceBundle4;
        UResourceBundle uResourceBundle5;
        EnumSet enumSet;
        boolean z2;
        Set<String> set;
        Set<String> set2;
        UResourceBundle uResourceBundle6;
        Set set3;
        UResourceBundle uResourceBundle7;
        Set set4;
        ICUResourceBundle bundleInstance = ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "keyTypeData", ICUResourceBundle.ICU_DATA_CLASS_LOADER, ICUResourceBundle.OpenType.DIRECT);
        getKeyInfo(bundleInstance.get("keyInfo"));
        getTypeInfo(bundleInstance.get("typeInfo"));
        UResourceBundle uResourceBundle8 = bundleInstance.get("keyMap");
        UResourceBundle uResourceBundle9 = bundleInstance.get("typeMap");
        try {
            uResourceBundle = bundleInstance.get("typeAlias");
        } catch (MissingResourceException unused) {
            uResourceBundle = null;
        }
        try {
            uResourceBundle2 = bundleInstance.get("bcpTypeAlias");
        } catch (MissingResourceException unused2) {
            uResourceBundle2 = null;
        }
        UResourceBundleIterator iterator = uResourceBundle8.getIterator();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        while (iterator.hasNext()) {
            UResourceBundle next = iterator.next();
            String key = next.getKey();
            String string = next.getString();
            if (string.length() == 0) {
                string = key;
                z = true;
            } else {
                z = false;
            }
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            linkedHashMap.put(string, Collections.unmodifiableSet(linkedHashSet));
            boolean equals = key.equals("timezone");
            char c = '/';
            if (uResourceBundle != null) {
                try {
                    uResourceBundle7 = uResourceBundle.get(key);
                } catch (MissingResourceException unused3) {
                    uResourceBundle7 = null;
                }
                if (uResourceBundle7 != null) {
                    hashMap = new HashMap();
                    UResourceBundleIterator iterator2 = uResourceBundle7.getIterator();
                    while (iterator2.hasNext()) {
                        UResourceBundle next2 = iterator2.next();
                        String key2 = next2.getKey();
                        String string2 = next2.getString();
                        if (equals) {
                            key2 = key2.replace(':', c);
                        }
                        Set set5 = (Set) hashMap.get(string2);
                        if (set5 == null) {
                            set4 = new HashSet();
                            hashMap.put(string2, set4);
                        } else {
                            set4 = set5;
                        }
                        set4.add(key2);
                        c = '/';
                    }
                    if (uResourceBundle2 != null) {
                        try {
                            uResourceBundle6 = uResourceBundle2.get(string);
                        } catch (MissingResourceException unused4) {
                            uResourceBundle6 = null;
                        }
                        if (uResourceBundle6 != null) {
                            hashMap2 = new HashMap();
                            UResourceBundleIterator iterator3 = uResourceBundle6.getIterator();
                            while (iterator3.hasNext()) {
                                UResourceBundle next3 = iterator3.next();
                                String key3 = next3.getKey();
                                String string3 = next3.getString();
                                Set set6 = (Set) hashMap2.get(string3);
                                if (set6 == null) {
                                    set3 = new HashSet();
                                    hashMap2.put(string3, set3);
                                } else {
                                    set3 = set6;
                                }
                                set3.add(key3);
                            }
                            HashMap hashMap3 = new HashMap();
                            uResourceBundle3 = uResourceBundle9.get(key);
                            if (uResourceBundle3 != null) {
                                UResourceBundleIterator iterator4 = uResourceBundle3.getIterator();
                                enumSet = null;
                                while (iterator4.hasNext()) {
                                    UResourceBundle next4 = iterator4.next();
                                    String key4 = next4.getKey();
                                    String string4 = next4.getString();
                                    char charAt = key4.charAt(0);
                                    if ('9' < charAt && charAt < 'a' && string4.length() == 0) {
                                        if (enumSet == null) {
                                            enumSet = EnumSet.noneOf(SpecialType.class);
                                        }
                                        enumSet.add(SpecialType.valueOf(key4));
                                        linkedHashSet.add(key4);
                                    } else {
                                        if (equals) {
                                            key4 = key4.replace(':', '/');
                                        }
                                        if (string4.length() == 0) {
                                            z2 = true;
                                            string4 = key4;
                                        } else {
                                            z2 = false;
                                        }
                                        linkedHashSet.add(string4);
                                        Type type = new Type(key4, string4);
                                        hashMap3.put(AsciiUtil.toLowerString(key4), type);
                                        if (!z2) {
                                            hashMap3.put(AsciiUtil.toLowerString(string4), type);
                                        }
                                        if (!(hashMap == null || (set2 = (Set) hashMap.get(key4)) == null)) {
                                            for (String str : set2) {
                                                hashMap3.put(AsciiUtil.toLowerString(str), type);
                                            }
                                        }
                                        if (!(hashMap2 == null || (set = (Set) hashMap2.get(string4)) == null)) {
                                            for (String str2 : set) {
                                                hashMap3.put(AsciiUtil.toLowerString(str2), type);
                                            }
                                        }
                                    }
                                    uResourceBundle2 = uResourceBundle2;
                                    uResourceBundle9 = uResourceBundle9;
                                    iterator = iterator;
                                }
                                uResourceBundle5 = uResourceBundle2;
                                uResourceBundleIterator = iterator;
                                uResourceBundle4 = uResourceBundle9;
                            } else {
                                uResourceBundle5 = uResourceBundle2;
                                uResourceBundleIterator = iterator;
                                uResourceBundle4 = uResourceBundle9;
                                enumSet = null;
                            }
                            KeyData keyData = new KeyData(key, string, hashMap3, enumSet);
                            KEYMAP.put(AsciiUtil.toLowerString(key), keyData);
                            if (!z) {
                                KEYMAP.put(AsciiUtil.toLowerString(string), keyData);
                            }
                            uResourceBundle2 = uResourceBundle5;
                            uResourceBundle9 = uResourceBundle4;
                            iterator = uResourceBundleIterator;
                        }
                    }
                    hashMap2 = null;
                    HashMap hashMap32 = new HashMap();
                    uResourceBundle3 = uResourceBundle9.get(key);
                    if (uResourceBundle3 != null) {
                    }
                    KeyData keyData2 = new KeyData(key, string, hashMap32, enumSet);
                    KEYMAP.put(AsciiUtil.toLowerString(key), keyData2);
                    if (!z) {
                    }
                    uResourceBundle2 = uResourceBundle5;
                    uResourceBundle9 = uResourceBundle4;
                    iterator = uResourceBundleIterator;
                }
            }
            hashMap = null;
            if (uResourceBundle2 != null) {
            }
            hashMap2 = null;
            HashMap hashMap322 = new HashMap();
            try {
                uResourceBundle3 = uResourceBundle9.get(key);
            } catch (MissingResourceException unused5) {
                uResourceBundle3 = null;
            }
            if (uResourceBundle3 != null) {
            }
            KeyData keyData22 = new KeyData(key, string, hashMap322, enumSet);
            KEYMAP.put(AsciiUtil.toLowerString(key), keyData22);
            if (!z) {
            }
            uResourceBundle2 = uResourceBundle5;
            uResourceBundle9 = uResourceBundle4;
            iterator = uResourceBundleIterator;
        }
        BCP47_KEYS = Collections.unmodifiableMap(linkedHashMap);
    }

    static {
        initFromResourceBundle();
    }

    private static void getKeyInfo(UResourceBundle uResourceBundle) {
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        UResourceBundleIterator iterator = uResourceBundle.getIterator();
        while (iterator.hasNext()) {
            UResourceBundle next = iterator.next();
            KeyInfoType valueOf = KeyInfoType.valueOf(next.getKey());
            UResourceBundleIterator iterator2 = next.getIterator();
            while (iterator2.hasNext()) {
                UResourceBundle next2 = iterator2.next();
                String key = next2.getKey();
                String string = next2.getString();
                int i = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$KeyInfoType[valueOf.ordinal()];
                if (i == 1) {
                    linkedHashSet.add(key);
                } else if (i == 2) {
                    linkedHashMap.put(key, ValueType.valueOf(string));
                }
            }
        }
        DEPRECATED_KEYS = Collections.unmodifiableSet(linkedHashSet);
        VALUE_TYPES = Collections.unmodifiableMap(linkedHashMap);
    }

    private static void getTypeInfo(UResourceBundle uResourceBundle) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        UResourceBundleIterator iterator = uResourceBundle.getIterator();
        while (iterator.hasNext()) {
            UResourceBundle next = iterator.next();
            TypeInfoType valueOf = TypeInfoType.valueOf(next.getKey());
            UResourceBundleIterator iterator2 = next.getIterator();
            while (iterator2.hasNext()) {
                UResourceBundle next2 = iterator2.next();
                String key = next2.getKey();
                LinkedHashSet linkedHashSet = new LinkedHashSet();
                UResourceBundleIterator iterator3 = next2.getIterator();
                while (iterator3.hasNext()) {
                    String key2 = iterator3.next().getKey();
                    if (AnonymousClass1.$SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$TypeInfoType[valueOf.ordinal()] == 1) {
                        linkedHashSet.add(key2);
                    }
                }
                linkedHashMap.put(key, Collections.unmodifiableSet(linkedHashSet));
            }
        }
        DEPRECATED_KEY_TYPES = Collections.unmodifiableMap(linkedHashMap);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.locale.KeyTypeData$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$KeyInfoType = new int[KeyInfoType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$TypeInfoType = new int[TypeInfoType.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$TypeInfoType[TypeInfoType.deprecated.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$KeyInfoType[KeyInfoType.deprecated.ordinal()] = 1;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$KeyInfoType[KeyInfoType.valueType.ordinal()] = 2;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private static void initFromTables() {
        String str;
        int i;
        HashMap hashMap;
        HashMap hashMap2;
        boolean z;
        boolean z2;
        Set set;
        Object[][] objArr = KEY_DATA;
        int length = objArr.length;
        int i2 = 0;
        int i3 = 0;
        while (i3 < length) {
            Object[] objArr2 = objArr[i3];
            String str2 = (String) objArr2[i2];
            char c = 1;
            String str3 = (String) objArr2[1];
            String[][] strArr = (String[][]) objArr2[2];
            String[][] strArr2 = (String[][]) objArr2[3];
            String[][] strArr3 = (String[][]) objArr2[4];
            if (str3 == null) {
                str = str2;
                i = 1;
            } else {
                str = str3;
                i = i2;
            }
            if (strArr2 != null) {
                hashMap = new HashMap();
                int length2 = strArr2.length;
                int i4 = i2;
                while (i4 < length2) {
                    String[] strArr4 = strArr2[i4];
                    String str4 = strArr4[i2];
                    String str5 = strArr4[c];
                    Set set2 = (Set) hashMap.get(str5);
                    if (set2 == null) {
                        set = new HashSet();
                        hashMap.put(str5, set);
                    } else {
                        set = set2;
                    }
                    set.add(str4);
                    i4++;
                    c = 1;
                }
            } else {
                hashMap = null;
            }
            if (strArr3 != null) {
                hashMap2 = new HashMap();
                int length3 = strArr3.length;
                for (int i5 = i2; i5 < length3; i5++) {
                    String[] strArr5 = strArr3[i5];
                    String str6 = strArr5[i2];
                    String str7 = strArr5[1];
                    Set set3 = (Set) hashMap2.get(str7);
                    if (set3 == null) {
                        set3 = new HashSet();
                        hashMap2.put(str7, set3);
                    }
                    set3.add(str6);
                }
            } else {
                hashMap2 = null;
            }
            HashMap hashMap3 = new HashMap();
            int length4 = strArr.length;
            int i6 = i2;
            HashSet hashSet = null;
            while (i6 < length4) {
                String[] strArr6 = strArr[i6];
                String str8 = strArr6[i2];
                String str9 = strArr6[1];
                SpecialType[] values = SpecialType.values();
                int length5 = values.length;
                int i7 = 0;
                while (true) {
                    if (i7 >= length5) {
                        z = false;
                        break;
                    }
                    SpecialType specialType = values[i7];
                    if (str8.equals(specialType.toString())) {
                        if (hashSet == null) {
                            hashSet = new HashSet();
                        }
                        hashSet.add(specialType);
                        z = true;
                    } else {
                        i7++;
                        length5 = length5;
                        values = values;
                    }
                }
                if (!z) {
                    if (str9 == null) {
                        str9 = str8;
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    Type type = new Type(str8, str9);
                    hashMap3.put(AsciiUtil.toLowerString(str8), type);
                    if (!z2) {
                        hashMap3.put(AsciiUtil.toLowerString(str9), type);
                    }
                    Set<String> set4 = (Set) hashMap.get(str8);
                    if (set4 != null) {
                        for (String str10 : set4) {
                            hashMap3.put(AsciiUtil.toLowerString(str10), type);
                        }
                    }
                    Set<String> set5 = (Set) hashMap2.get(str9);
                    if (set5 != null) {
                        for (String str11 : set5) {
                            hashMap3.put(AsciiUtil.toLowerString(str11), type);
                        }
                    }
                }
                i6++;
                objArr = objArr;
                length = length;
                i2 = 0;
            }
            KeyData keyData = new KeyData(str2, str, hashMap3, hashSet != null ? EnumSet.copyOf(hashSet) : null);
            KEYMAP.put(AsciiUtil.toLowerString(str2), keyData);
            if (i == 0) {
                KEYMAP.put(AsciiUtil.toLowerString(str), keyData);
            }
            i3++;
            objArr = objArr;
            length = length;
            i2 = 0;
        }
    }

    public static Set<String> getBcp47Keys() {
        return BCP47_KEYS.keySet();
    }

    public static Set<String> getBcp47KeyTypes(String str) {
        return BCP47_KEYS.get(str);
    }

    public static boolean isDeprecated(String str) {
        return DEPRECATED_KEYS.contains(str);
    }

    public static boolean isDeprecated(String str, String str2) {
        Set<String> set = DEPRECATED_KEY_TYPES.get(str);
        if (set == null) {
            return false;
        }
        return set.contains(str2);
    }

    public static ValueType getValueType(String str) {
        ValueType valueType = VALUE_TYPES.get(str);
        return valueType == null ? ValueType.single : valueType;
    }
}

package ohos.global.icu.impl.locale;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.util.BytesTrie;
import ohos.global.icu.util.ULocale;

public final class XLikelySubtags {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean DEBUG_OUTPUT = false;
    public static final XLikelySubtags INSTANCE = new XLikelySubtags(Data.load());
    private static final String PSEUDO_ACCENTS_PREFIX = "'";
    private static final String PSEUDO_BIDI_PREFIX = "+";
    private static final String PSEUDO_CRACKED_PREFIX = ",";
    public static final int SKIP_SCRIPT = 1;
    private final int defaultLsrIndex;
    private final Map<String, String> languageAliases;
    private final LSR[] lsrs;
    private final Map<String, String> regionAliases;
    private final BytesTrie trie;
    private final long[] trieFirstLetterStates = new long[26];
    private final long trieUndState;
    private final long trieUndZzzzState;

    public static final class Data {
        public final Map<String, String> languageAliases;
        public final LSR[] lsrs;
        public final Map<String, String> regionAliases;
        public final byte[] trie;

        public Data(Map<String, String> map, Map<String, String> map2, byte[] bArr, LSR[] lsrArr) {
            this.languageAliases = map;
            this.regionAliases = map2;
            this.trie = bArr;
            this.lsrs = lsrArr;
        }

        private static UResource.Value getValue(UResource.Table table, String str, UResource.Value value) {
            if (table.findValue(str, value)) {
                return value;
            }
            throw new MissingResourceException("langInfo.res missing data", "", "likely/" + str);
        }

        public static Data load() throws MissingResourceException {
            Map map;
            Map map2;
            UResource.Value valueWithFallback = ICUResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "langInfo", ICUResourceBundle.ICU_DATA_CLASS_LOADER, ICUResourceBundle.OpenType.DIRECT).getValueWithFallback("likely");
            UResource.Table table = valueWithFallback.getTable();
            int i = 0;
            if (table.findValue("languageAliases", valueWithFallback)) {
                String[] stringArray = valueWithFallback.getStringArray();
                map = new HashMap(stringArray.length / 2);
                for (int i2 = 0; i2 < stringArray.length; i2 += 2) {
                    map.put(stringArray[i2], stringArray[i2 + 1]);
                }
            } else {
                map = Collections.emptyMap();
            }
            if (table.findValue("regionAliases", valueWithFallback)) {
                String[] stringArray2 = valueWithFallback.getStringArray();
                map2 = new HashMap(stringArray2.length / 2);
                for (int i3 = 0; i3 < stringArray2.length; i3 += 2) {
                    map2.put(stringArray2[i3], stringArray2[i3 + 1]);
                }
            } else {
                map2 = Collections.emptyMap();
            }
            ByteBuffer binary = getValue(table, "trie", valueWithFallback).getBinary();
            byte[] bArr = new byte[binary.remaining()];
            binary.get(bArr);
            String[] stringArray3 = getValue(table, "lsrs", valueWithFallback).getStringArray();
            LSR[] lsrArr = new LSR[(stringArray3.length / 3)];
            int i4 = 0;
            while (i < stringArray3.length) {
                lsrArr[i4] = new LSR(stringArray3[i], stringArray3[i + 1], stringArray3[i + 2]);
                i += 3;
                i4++;
            }
            return new Data(map, map2, bArr, lsrArr);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!getClass().equals(obj.getClass())) {
                return false;
            }
            Data data = (Data) obj;
            return this.languageAliases.equals(data.languageAliases) && this.regionAliases.equals(data.regionAliases) && Arrays.equals(this.trie, data.trie) && Arrays.equals(this.lsrs, data.lsrs);
        }
    }

    private XLikelySubtags(Data data) {
        this.languageAliases = data.languageAliases;
        this.regionAliases = data.regionAliases;
        this.trie = new BytesTrie(data.trie, 0);
        this.lsrs = data.lsrs;
        this.trie.next(42);
        this.trieUndState = this.trie.getState64();
        this.trie.next(42);
        this.trieUndZzzzState = this.trie.getState64();
        this.trie.next(42);
        this.defaultLsrIndex = this.trie.getValue();
        this.trie.reset();
        for (char c = 'a'; c <= 'z'; c = (char) (c + 1)) {
            if (this.trie.next(c) == BytesTrie.Result.NO_VALUE) {
                this.trieFirstLetterStates[c - 'a'] = this.trie.getState64();
            }
            this.trie.reset();
        }
    }

    public ULocale canonicalize(ULocale uLocale) {
        String language = uLocale.getLanguage();
        String str = this.languageAliases.get(language);
        String country = uLocale.getCountry();
        String str2 = this.regionAliases.get(country);
        if (str == null && str2 == null) {
            return uLocale;
        }
        if (str != null) {
            language = str;
        }
        String script = uLocale.getScript();
        if (str2 == null) {
            str2 = country;
        }
        return new ULocale(language, script, str2);
    }

    private static String getCanonical(Map<String, String> map, String str) {
        String str2 = map.get(str);
        return str2 == null ? str : str2;
    }

    public LSR makeMaximizedLsrFrom(ULocale uLocale) {
        if (uLocale.getName().startsWith("@x=")) {
            return new LSR(uLocale.toLanguageTag(), "", "");
        }
        return makeMaximizedLsr(uLocale.getLanguage(), uLocale.getScript(), uLocale.getCountry(), uLocale.getVariant());
    }

    public LSR makeMaximizedLsrFrom(Locale locale) {
        String languageTag = locale.toLanguageTag();
        if (languageTag.startsWith("x-")) {
            return new LSR(languageTag, "", "");
        }
        return makeMaximizedLsr(locale.getLanguage(), locale.getScript(), locale.getCountry(), locale.getVariant());
    }

    private LSR makeMaximizedLsr(String str, String str2, String str3, String str4) {
        if (str3.length() == 2 && str3.charAt(0) == 'X') {
            switch (str3.charAt(1)) {
                case 'A':
                    return new LSR(PSEUDO_ACCENTS_PREFIX + str, PSEUDO_ACCENTS_PREFIX + str2, str3);
                case 'B':
                    return new LSR(PSEUDO_BIDI_PREFIX + str, PSEUDO_BIDI_PREFIX + str2, str3);
                case 'C':
                    return new LSR(PSEUDO_CRACKED_PREFIX + str, PSEUDO_CRACKED_PREFIX + str2, str3);
            }
        }
        if (str4.startsWith("PS")) {
            char c = 65535;
            int hashCode = str4.hashCode();
            if (hashCode != -1925944433) {
                if (hashCode != 264103053) {
                    if (hashCode == 426453367 && str4.equals("PSCRACK")) {
                        c = 2;
                    }
                } else if (str4.equals("PSACCENT")) {
                    c = 0;
                }
            } else if (str4.equals("PSBIDI")) {
                c = 1;
            }
            if (c == 0) {
                String str5 = PSEUDO_ACCENTS_PREFIX + str;
                String str6 = PSEUDO_ACCENTS_PREFIX + str2;
                if (str3.isEmpty()) {
                    str3 = "XA";
                }
                return new LSR(str5, str6, str3);
            } else if (c == 1) {
                String str7 = PSEUDO_BIDI_PREFIX + str;
                String str8 = PSEUDO_BIDI_PREFIX + str2;
                if (str3.isEmpty()) {
                    str3 = "XB";
                }
                return new LSR(str7, str8, str3);
            } else if (c == 2) {
                String str9 = PSEUDO_CRACKED_PREFIX + str;
                String str10 = PSEUDO_CRACKED_PREFIX + str2;
                if (str3.isEmpty()) {
                    str3 = "XC";
                }
                return new LSR(str9, str10, str3);
            }
        }
        return maximize(getCanonical(this.languageAliases, str), str2, getCanonical(this.regionAliases, str3));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00c5, code lost:
        if (r6.isEmpty() == false) goto L_0x00c7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d4, code lost:
        if (r6.isEmpty() == false) goto L_0x00c7;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0081  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00c1  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x00f7 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00f8  */
    private LSR maximize(String str, String str2, String str3) {
        int i;
        int i2;
        long j;
        int charAt;
        String str4 = "und";
        String str5 = str;
        if (str5.equals(str4)) {
            str5 = "";
        }
        String str6 = str2;
        if (str6.equals("Zzzz")) {
            str6 = "";
        }
        String str7 = str3;
        if (str7.equals("ZZ")) {
            str7 = "";
        }
        if (!str6.isEmpty() && !str7.isEmpty() && !str5.isEmpty()) {
            return new LSR(str5, str6, str7);
        }
        BytesTrie bytesTrie = new BytesTrie(this.trie);
        if (str5.length() >= 2 && str5.charAt(0) - 'a' >= 0 && charAt <= 25) {
            long j2 = this.trieFirstLetterStates[charAt];
            if (j2 != 0) {
                i = trieNext(bytesTrie.resetToState64(j2), str5, 1);
                i2 = 4;
                if (i < 0) {
                    if (str5.isEmpty()) {
                        i2 = 0;
                    }
                    j = bytesTrie.getState64();
                } else {
                    bytesTrie.resetToState64(this.trieUndState);
                    j = 0;
                }
                if (i <= 0) {
                    if (i == 1) {
                        i = 0;
                    }
                    if (!str6.isEmpty()) {
                        i2 |= 2;
                    }
                } else {
                    i = trieNext(bytesTrie, str6, 0);
                    if (i >= 0) {
                        if (!str6.isEmpty()) {
                            i2 |= 2;
                        }
                        j = bytesTrie.getState64();
                    } else {
                        i2 |= 2;
                        if (j == 0) {
                            bytesTrie.resetToState64(this.trieUndZzzzState);
                        } else {
                            bytesTrie.resetToState64(j);
                            i = trieNext(bytesTrie, "", 0);
                            j = bytesTrie.getState64();
                        }
                    }
                }
                if (i > 0) {
                    i = trieNext(bytesTrie, str7, 0);
                    if (i < 0) {
                        i2 |= 1;
                        if (j == 0) {
                            i = this.defaultLsrIndex;
                        } else {
                            bytesTrie.resetToState64(j);
                            i = trieNext(bytesTrie, "", 0);
                        }
                    }
                    LSR lsr = this.lsrs[i];
                    if (!str5.isEmpty()) {
                        str4 = str5;
                    }
                    if (i2 == 0) {
                        return lsr;
                    }
                    if ((i2 & 4) == 0) {
                        str4 = lsr.language;
                    }
                    if ((i2 & 2) == 0) {
                        str6 = lsr.script;
                    }
                    if ((i2 & 1) == 0) {
                        str7 = lsr.region;
                    }
                    return new LSR(str4, str6, str7);
                }
                i2 |= 1;
                LSR lsr2 = this.lsrs[i];
                if (!str5.isEmpty()) {
                }
                if (i2 == 0) {
                }
            }
        }
        i = trieNext(bytesTrie, str5, 0);
        i2 = 4;
        if (i < 0) {
        }
        if (i <= 0) {
        }
        if (i > 0) {
        }
        i2 |= 1;
        LSR lsr22 = this.lsrs[i];
        if (!str5.isEmpty()) {
        }
        if (i2 == 0) {
        }
    }

    private static final int trieNext(BytesTrie bytesTrie, String str, int i) {
        BytesTrie.Result result;
        if (!str.isEmpty()) {
            int length = str.length() - 1;
            while (true) {
                char charAt = str.charAt(i);
                if (i >= length) {
                    result = bytesTrie.next(charAt | 128);
                    break;
                } else if (!bytesTrie.next(charAt).hasNext()) {
                    return -1;
                } else {
                    i++;
                }
            }
        } else {
            result = bytesTrie.next(42);
        }
        int i2 = AnonymousClass1.$SwitchMap$ohos$global$icu$util$BytesTrie$Result[result.ordinal()];
        if (i2 == 1) {
            return -1;
        }
        if (i2 == 2) {
            return 0;
        }
        if (i2 == 3) {
            return 1;
        }
        if (i2 != 4) {
            return -1;
        }
        return bytesTrie.getValue();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.locale.XLikelySubtags$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$BytesTrie$Result = new int[BytesTrie.Result.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$util$BytesTrie$Result[BytesTrie.Result.NO_MATCH.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$BytesTrie$Result[BytesTrie.Result.NO_VALUE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$BytesTrie$Result[BytesTrie.Result.INTERMEDIATE_VALUE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$BytesTrie$Result[BytesTrie.Result.FINAL_VALUE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public LSR minimizeSubtags(String str, String str2, String str3, ULocale.Minimize minimize) {
        LSR maximize = maximize(str, str2, str3);
        BytesTrie bytesTrie = new BytesTrie(this.trie);
        boolean z = false;
        int trieNext = trieNext(bytesTrie, maximize.language, 0);
        if (trieNext == 0 && (trieNext = trieNext(bytesTrie, "", 0)) == 0) {
            trieNext = trieNext(bytesTrie, "", 0);
        }
        LSR lsr = this.lsrs[trieNext];
        if (maximize.script.equals(lsr.script)) {
            if (maximize.region.equals(lsr.region)) {
                return new LSR(maximize.language, "", "");
            }
            if (minimize == ULocale.Minimize.FAVOR_REGION) {
                return new LSR(maximize.language, "", maximize.region);
            }
            z = true;
        }
        if (maximize(str, str2, "").equals(maximize)) {
            return new LSR(maximize.language, maximize.script, "");
        }
        return z ? new LSR(maximize.language, "", maximize.region) : maximize;
    }

    private Map<String, LSR> getTable() {
        TreeMap treeMap = new TreeMap();
        StringBuilder sb = new StringBuilder();
        BytesTrie.Iterator it = this.trie.iterator();
        while (it.hasNext()) {
            BytesTrie.Entry entry = (BytesTrie.Entry) it.next();
            int i = 0;
            sb.setLength(0);
            int bytesLength = entry.bytesLength();
            while (i < bytesLength) {
                int i2 = i + 1;
                byte byteAt = entry.byteAt(i);
                if (byteAt == 42) {
                    sb.append("*-");
                } else if (byteAt >= 0) {
                    sb.append((char) byteAt);
                } else {
                    sb.append((char) (byteAt & Byte.MAX_VALUE));
                    sb.append(LocaleUtility.IETF_SEPARATOR);
                }
                i = i2;
            }
            sb.setLength(sb.length() - 1);
            treeMap.put(sb.toString(), this.lsrs[entry.value]);
        }
        return treeMap;
    }

    public String toString() {
        return getTable().toString();
    }
}

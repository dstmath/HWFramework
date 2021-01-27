package ohos.global.icu.impl.locale;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.ValidIdentifiers;
import ohos.global.icu.impl.locale.KeyTypeData;
import ohos.global.icu.util.IllformedLocaleException;
import ohos.global.icu.util.Output;
import ohos.global.icu.util.ULocale;

public class LocaleValidityChecker {
    static final Set<ValidIdentifiers.Datasubtype> REGULAR_ONLY = EnumSet.of(ValidIdentifiers.Datasubtype.regular);
    static final Set<String> REORDERING_EXCLUDE = new HashSet(Arrays.asList("zinh", "zyyy"));
    static final Set<String> REORDERING_INCLUDE = new HashSet(Arrays.asList("space", "punct", "symbol", "currency", Constants.ATTRNAME_DIGIT, "others", "zzzz"));
    static Pattern SEPARATOR = Pattern.compile("[-_]");
    private static final Pattern VALID_X = Pattern.compile("[a-zA-Z0-9]{2,8}(-[a-zA-Z0-9]{2,8})*");
    private final boolean allowsDeprecated;
    private final Set<ValidIdentifiers.Datasubtype> datasubtypes;

    public static class Where {
        public String codeFailure;
        public ValidIdentifiers.Datatype fieldFailure;

        public boolean set(ValidIdentifiers.Datatype datatype, String str) {
            this.fieldFailure = datatype;
            this.codeFailure = str;
            return false;
        }

        public String toString() {
            if (this.fieldFailure == null) {
                return "OK";
            }
            return "{" + this.fieldFailure + ", " + this.codeFailure + "}";
        }
    }

    public LocaleValidityChecker(Set<ValidIdentifiers.Datasubtype> set) {
        this.datasubtypes = EnumSet.copyOf(set);
        this.allowsDeprecated = set.contains(ValidIdentifiers.Datasubtype.deprecated);
    }

    public LocaleValidityChecker(ValidIdentifiers.Datasubtype... datasubtypeArr) {
        this.datasubtypes = EnumSet.copyOf(Arrays.asList(datasubtypeArr));
        this.allowsDeprecated = this.datasubtypes.contains(ValidIdentifiers.Datasubtype.deprecated);
    }

    public Set<ValidIdentifiers.Datasubtype> getDatasubtypes() {
        return EnumSet.copyOf(this.datasubtypes);
    }

    public boolean isValid(ULocale uLocale, Where where) {
        where.set(null, null);
        String language = uLocale.getLanguage();
        String script = uLocale.getScript();
        String country = uLocale.getCountry();
        String variant = uLocale.getVariant();
        Set<Character> extensionKeys = uLocale.getExtensionKeys();
        if (!isValid(ValidIdentifiers.Datatype.language, language, where)) {
            if (!language.equals(LanguageTag.PRIVATEUSE)) {
                return false;
            }
            where.set(null, null);
            return true;
        } else if (!isValid(ValidIdentifiers.Datatype.script, script, where) || !isValid(ValidIdentifiers.Datatype.region, country, where)) {
            return false;
        } else {
            if (!variant.isEmpty()) {
                for (String str : SEPARATOR.split(variant)) {
                    if (!isValid(ValidIdentifiers.Datatype.variant, str, where)) {
                        return false;
                    }
                }
            }
            for (Character ch : extensionKeys) {
                try {
                    ValidIdentifiers.Datatype valueOf = ValidIdentifiers.Datatype.valueOf(ch + "");
                    int i = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$ValidIdentifiers$Datatype[valueOf.ordinal()];
                    if (i == 1) {
                        return true;
                    }
                    if (i == 2 || i == 3) {
                        if (!isValidU(uLocale, valueOf, uLocale.getExtension(ch.charValue()), where)) {
                            return false;
                        }
                    }
                } catch (Exception unused) {
                    return where.set(ValidIdentifiers.Datatype.illegal, ch + "");
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public enum SpecialCase {
        normal,
        anything,
        reorder,
        codepoints,
        subdivision,
        rgKey;

        static SpecialCase get(String str) {
            if (str.equals("kr")) {
                return reorder;
            }
            if (str.equals("vt")) {
                return codepoints;
            }
            if (str.equals("sd")) {
                return subdivision;
            }
            if (str.equals("rg")) {
                return rgKey;
            }
            if (str.equals("x0")) {
                return anything;
            }
            return normal;
        }
    }

    private boolean isValidU(ULocale uLocale, ValidIdentifiers.Datatype datatype, String str, Where where) {
        String[] strArr;
        StringBuilder sb;
        StringBuilder sb2 = new StringBuilder();
        HashSet hashSet = new HashSet();
        StringBuilder sb3 = datatype == ValidIdentifiers.Datatype.t ? new StringBuilder() : null;
        String[] split = SEPARATOR.split(str);
        int length = split.length;
        String str2 = "";
        KeyTypeData.ValueType valueType = null;
        int i = 0;
        SpecialCase specialCase = null;
        StringBuilder sb4 = sb3;
        int i2 = 0;
        while (i2 < length) {
            String str3 = split[i2];
            if (str3.length() == 2 && (sb4 == null || str3.charAt(1) <= '9')) {
                if (sb4 != null) {
                    if (sb4.length() != 0 && !isValidLocale(sb4.toString(), where)) {
                        return false;
                    }
                    sb4 = null;
                }
                String bcpKey = KeyTypeData.toBcpKey(str3);
                if (bcpKey == null) {
                    return where.set(datatype, str3);
                }
                if (!this.allowsDeprecated && KeyTypeData.isDeprecated(bcpKey)) {
                    return where.set(datatype, bcpKey);
                }
                KeyTypeData.ValueType valueType2 = KeyTypeData.getValueType(bcpKey);
                sb = sb2;
                strArr = split;
                str2 = bcpKey;
                specialCase = SpecialCase.get(bcpKey);
                i = 0;
                valueType = valueType2;
            } else if (sb4 != null) {
                if (sb4.length() != 0) {
                    sb4.append(LocaleUtility.IETF_SEPARATOR);
                }
                sb4.append(str3);
                sb = sb2;
                strArr = split;
            } else {
                i++;
                int i3 = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$ValueType[valueType.ordinal()];
                strArr = split;
                if (i3 != 1) {
                    if (i3 != 2) {
                        if (i3 == 3 && i == 1) {
                            hashSet.clear();
                        }
                    } else if (i == 1) {
                        sb2.setLength(0);
                        sb2.append(str3);
                    } else {
                        sb2.append(LocaleUtility.IETF_SEPARATOR);
                        sb2.append(str3);
                        str3 = sb2.toString();
                    }
                } else if (i > 1) {
                    return where.set(datatype, str2 + LanguageTag.SEP + str3);
                }
                int i4 = AnonymousClass1.$SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase[specialCase.ordinal()];
                if (i4 == 1) {
                    sb = sb2;
                } else if (i4 != 2) {
                    sb = sb2;
                    if (i4 == 3) {
                        if (!hashSet.add(str3.equals("zzzz") ? "others" : str3) || !isScriptReorder(str3)) {
                            return where.set(datatype, str2 + LanguageTag.SEP + str3);
                        }
                    } else if (i4 != 4) {
                        if (i4 != 5) {
                            if (KeyTypeData.toBcpType(str2, str3, new Output(), new Output()) == null) {
                                return where.set(datatype, str2 + LanguageTag.SEP + str3);
                            } else if (!this.allowsDeprecated && KeyTypeData.isDeprecated(str2, str3)) {
                                return where.set(datatype, str2 + LanguageTag.SEP + str3);
                            }
                        } else if (str3.length() < 6 || !str3.endsWith("zzzz")) {
                            return where.set(datatype, str3);
                        } else {
                            if (!isValid(ValidIdentifiers.Datatype.region, str3.substring(0, str3.length() - 4), where)) {
                                return false;
                            }
                        }
                    } else if (!isSubdivision(uLocale, str3)) {
                        return where.set(datatype, str2 + LanguageTag.SEP + str3);
                    }
                } else {
                    sb = sb2;
                    try {
                        if (Integer.parseInt(str3, 16) > 1114111) {
                            return where.set(datatype, str2 + LanguageTag.SEP + str3);
                        }
                    } catch (NumberFormatException unused) {
                        return where.set(datatype, str2 + LanguageTag.SEP + str3);
                    }
                }
            }
            i2++;
            sb2 = sb;
            split = strArr;
        }
        return sb4 == null || sb4.length() == 0 || isValidLocale(sb4.toString(), where);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.locale.LocaleValidityChecker$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$ValidIdentifiers$Datatype = new int[ValidIdentifiers.Datatype.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$ValueType = new int[KeyTypeData.ValueType.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase = new int[SpecialCase.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase[SpecialCase.anything.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase[SpecialCase.codepoints.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase[SpecialCase.reorder.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase[SpecialCase.subdivision.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$LocaleValidityChecker$SpecialCase[SpecialCase.rgKey.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$ValueType[KeyTypeData.ValueType.single.ordinal()] = 1;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$ValueType[KeyTypeData.ValueType.incremental.ordinal()] = 2;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$locale$KeyTypeData$ValueType[KeyTypeData.ValueType.multiple.ordinal()] = 3;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$ValidIdentifiers$Datatype[ValidIdentifiers.Datatype.x.ordinal()] = 1;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$ValidIdentifiers$Datatype[ValidIdentifiers.Datatype.t.ordinal()] = 2;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$ValidIdentifiers$Datatype[ValidIdentifiers.Datatype.u.ordinal()] = 3;
            } catch (NoSuchFieldError unused11) {
            }
        }
    }

    private boolean isSubdivision(ULocale uLocale, String str) {
        int i = 3;
        if (str.length() < 3) {
            return false;
        }
        if (str.charAt(0) > '9') {
            i = 2;
        }
        String substring = str.substring(0, i);
        if (ValidIdentifiers.isValid(ValidIdentifiers.Datatype.subdivision, this.datasubtypes, substring, str.substring(substring.length())) == null) {
            return false;
        }
        String country = uLocale.getCountry();
        if (country.isEmpty()) {
            country = ULocale.addLikelySubtags(uLocale).getCountry();
        }
        if (!substring.equalsIgnoreCase(country)) {
            return false;
        }
        return true;
    }

    private boolean isScriptReorder(String str) {
        String lowerString = AsciiUtil.toLowerString(str);
        if (REORDERING_INCLUDE.contains(lowerString)) {
            return true;
        }
        if (REORDERING_EXCLUDE.contains(lowerString)) {
            return false;
        }
        if (ValidIdentifiers.isValid(ValidIdentifiers.Datatype.script, REGULAR_ONLY, lowerString) != null) {
            return true;
        }
        return false;
    }

    private boolean isValidLocale(String str, Where where) {
        try {
            return isValid(new ULocale.Builder().setLanguageTag(str).build(), where);
        } catch (IllformedLocaleException e) {
            return where.set(ValidIdentifiers.Datatype.t, SEPARATOR.split(str.substring(e.getErrorIndex()))[0]);
        } catch (Exception e2) {
            return where.set(ValidIdentifiers.Datatype.t, e2.getMessage());
        }
    }

    private boolean isValid(ValidIdentifiers.Datatype datatype, String str, Where where) {
        if (str.isEmpty()) {
            return true;
        }
        if ((datatype == ValidIdentifiers.Datatype.variant && "posix".equalsIgnoreCase(str)) || ValidIdentifiers.isValid(datatype, this.datasubtypes, str) != null) {
            return true;
        }
        if (where == null) {
            return false;
        }
        return where.set(datatype, str);
    }
}

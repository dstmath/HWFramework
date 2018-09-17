package android.icu.impl.locale;

import android.icu.impl.ValidIdentifiers;
import android.icu.impl.ValidIdentifiers.Datasubtype;
import android.icu.impl.ValidIdentifiers.Datatype;
import android.icu.impl.locale.KeyTypeData.ValueType;
import android.icu.text.DateFormat;
import android.icu.util.IllformedLocaleException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class LocaleValidityChecker {
    private static final /* synthetic */ int[] -android-icu-impl-ValidIdentifiers$DatatypeSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-impl-locale-KeyTypeData$ValueTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-impl-locale-LocaleValidityChecker$SpecialCaseSwitchesValues = null;
    static final Set<Datasubtype> REGULAR_ONLY = EnumSet.of(Datasubtype.regular);
    static final Set<String> REORDERING_EXCLUDE = new HashSet(Arrays.asList(new String[]{"zinh", "zyyy"}));
    static final Set<String> REORDERING_INCLUDE = new HashSet(Arrays.asList(new String[]{"space", "punct", "symbol", "currency", "digit", "others", DateFormat.SPECIFIC_TZ}));
    static Pattern SEPARATOR = Pattern.compile("[-_]");
    private static final Pattern VALID_X = Pattern.compile("[a-zA-Z0-9]{2,8}(-[a-zA-Z0-9]{2,8})*");
    private final boolean allowsDeprecated;
    private final Set<Datasubtype> datasubtypes;

    enum SpecialCase {
        normal,
        anything,
        reorder,
        codepoints,
        subdivision,
        rgKey;

        static SpecialCase get(String key) {
            if (key.equals("kr")) {
                return reorder;
            }
            if (key.equals("vt")) {
                return codepoints;
            }
            if (key.equals("sd")) {
                return subdivision;
            }
            if (key.equals("rg")) {
                return rgKey;
            }
            if (key.equals("x0")) {
                return anything;
            }
            return normal;
        }
    }

    public static class Where {
        public String codeFailure;
        public Datatype fieldFailure;

        public boolean set(Datatype datatype, String code) {
            this.fieldFailure = datatype;
            this.codeFailure = code;
            return false;
        }

        public String toString() {
            return this.fieldFailure == null ? "OK" : "{" + this.fieldFailure + ", " + this.codeFailure + "}";
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-ValidIdentifiers$DatatypeSwitchesValues() {
        if (-android-icu-impl-ValidIdentifiers$DatatypeSwitchesValues != null) {
            return -android-icu-impl-ValidIdentifiers$DatatypeSwitchesValues;
        }
        int[] iArr = new int[Datatype.values().length];
        try {
            iArr[Datatype.currency.ordinal()] = 12;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Datatype.illegal.ordinal()] = 13;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Datatype.language.ordinal()] = 14;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Datatype.region.ordinal()] = 15;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Datatype.script.ordinal()] = 16;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Datatype.subdivision.ordinal()] = 17;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Datatype.t.ordinal()] = 1;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[Datatype.u.ordinal()] = 2;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[Datatype.unit.ordinal()] = 18;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[Datatype.variant.ordinal()] = 19;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[Datatype.x.ordinal()] = 3;
        } catch (NoSuchFieldError e11) {
        }
        -android-icu-impl-ValidIdentifiers$DatatypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-locale-KeyTypeData$ValueTypeSwitchesValues() {
        if (-android-icu-impl-locale-KeyTypeData$ValueTypeSwitchesValues != null) {
            return -android-icu-impl-locale-KeyTypeData$ValueTypeSwitchesValues;
        }
        int[] iArr = new int[ValueType.values().length];
        try {
            iArr[ValueType.any.ordinal()] = 12;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ValueType.incremental.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ValueType.multiple.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ValueType.single.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        -android-icu-impl-locale-KeyTypeData$ValueTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-locale-LocaleValidityChecker$SpecialCaseSwitchesValues() {
        if (-android-icu-impl-locale-LocaleValidityChecker$SpecialCaseSwitchesValues != null) {
            return -android-icu-impl-locale-LocaleValidityChecker$SpecialCaseSwitchesValues;
        }
        int[] iArr = new int[SpecialCase.values().length];
        try {
            iArr[SpecialCase.anything.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SpecialCase.codepoints.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SpecialCase.normal.ordinal()] = 12;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SpecialCase.reorder.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SpecialCase.rgKey.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SpecialCase.subdivision.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        -android-icu-impl-locale-LocaleValidityChecker$SpecialCaseSwitchesValues = iArr;
        return iArr;
    }

    public LocaleValidityChecker(Set<Datasubtype> datasubtypes) {
        this.datasubtypes = EnumSet.copyOf(datasubtypes);
        this.allowsDeprecated = datasubtypes.contains(Datasubtype.deprecated);
    }

    public LocaleValidityChecker(Datasubtype... datasubtypes) {
        this.datasubtypes = EnumSet.copyOf(Arrays.asList(datasubtypes));
        this.allowsDeprecated = this.datasubtypes.contains(Datasubtype.deprecated);
    }

    public Set<Datasubtype> getDatasubtypes() {
        return EnumSet.copyOf(this.datasubtypes);
    }

    public boolean isValid(ULocale locale, Where where) {
        where.set(null, null);
        String language = locale.getLanguage();
        String script = locale.getScript();
        String region = locale.getCountry();
        String variantString = locale.getVariant();
        Set<Character> extensionKeys = locale.getExtensionKeys();
        if (isValid(Datatype.language, language, where)) {
            if (!isValid(Datatype.script, script, where)) {
                return false;
            }
            if (!isValid(Datatype.region, region, where)) {
                return false;
            }
            if (!variantString.isEmpty()) {
                for (String variant : SEPARATOR.split(variantString)) {
                    if (!isValid(Datatype.variant, variant, where)) {
                        return false;
                    }
                }
            }
            for (Character c : extensionKeys) {
                try {
                    Datatype datatype = Datatype.valueOf(c + "");
                    switch (-getandroid-icu-impl-ValidIdentifiers$DatatypeSwitchesValues()[datatype.ordinal()]) {
                        case 1:
                        case 2:
                            if (isValidU(locale, datatype, locale.getExtension(c.charValue()), where)) {
                                break;
                            }
                            return false;
                        case 3:
                            return true;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    return where.set(Datatype.illegal, c + "");
                }
            }
            return true;
        } else if (!language.equals(LanguageTag.PRIVATEUSE)) {
            return false;
        } else {
            where.set(null, null);
            return true;
        }
    }

    /* JADX WARNING: Missing block: B:84:0x026a, code:
            return r27.set(r25, r12);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isValidU(ULocale locale, Datatype datatype, String extensionString, Where where) {
        String subtag;
        String key = "";
        int typeCount = 0;
        ValueType valueType = null;
        SpecialCase specialCase = null;
        StringBuilder prefix = new StringBuilder();
        Set<String> seen = new HashSet();
        StringBuilder tBuffer = datatype == Datatype.t ? new StringBuilder() : null;
        String[] split = SEPARATOR.split(extensionString);
        int i = 0;
        int length = split.length;
        while (true) {
            int i2 = i;
            if (i2 < length) {
                subtag = split[i2];
                if (subtag.length() == 2 && (tBuffer == null || subtag.charAt(1) <= '9')) {
                    if (tBuffer != null) {
                        if (tBuffer.length() != 0 && (isValidLocale(tBuffer.toString(), where) ^ 1) != 0) {
                            return false;
                        }
                        tBuffer = null;
                    }
                    key = KeyTypeData.toBcpKey(subtag);
                    if (key == null) {
                        return where.set(datatype, subtag);
                    }
                    if (!this.allowsDeprecated && KeyTypeData.isDeprecated(key)) {
                        return where.set(datatype, key);
                    }
                    valueType = KeyTypeData.getValueType(key);
                    specialCase = SpecialCase.get(key);
                    typeCount = 0;
                } else if (tBuffer != null) {
                    if (tBuffer.length() != 0) {
                        tBuffer.append('-');
                    }
                    tBuffer.append(subtag);
                } else {
                    typeCount++;
                    switch (-getandroid-icu-impl-locale-KeyTypeData$ValueTypeSwitchesValues()[valueType.ordinal()]) {
                        case 1:
                            if (typeCount != 1) {
                                prefix.append('-').append(subtag);
                                subtag = prefix.toString();
                                break;
                            }
                            prefix.setLength(0);
                            prefix.append(subtag);
                            break;
                        case 2:
                            if (typeCount == 1) {
                                seen.clear();
                                break;
                            }
                            break;
                        case 3:
                            if (typeCount > 1) {
                                return where.set(datatype, key + LanguageTag.SEP + subtag);
                            }
                            break;
                    }
                    switch (-getandroid-icu-impl-locale-LocaleValidityChecker$SpecialCaseSwitchesValues()[specialCase.ordinal()]) {
                        case 1:
                            continue;
                        case 2:
                            try {
                                if (Integer.parseInt(subtag, 16) <= 1114111) {
                                    break;
                                }
                                return where.set(datatype, key + LanguageTag.SEP + subtag);
                            } catch (NumberFormatException e) {
                                return where.set(datatype, key + LanguageTag.SEP + subtag);
                            }
                        case 3:
                            String str;
                            if (subtag.equals(DateFormat.SPECIFIC_TZ)) {
                                str = "others";
                            } else {
                                str = subtag;
                            }
                            if (!seen.add(str) || (isScriptReorder(subtag) ^ 1) != 0) {
                                break;
                            }
                            break;
                            break;
                        case 4:
                            if (subtag.length() >= 6 && (subtag.endsWith(DateFormat.SPECIFIC_TZ) ^ 1) == 0) {
                                if (isValid(Datatype.region, subtag.substring(0, subtag.length() - 4), where)) {
                                    break;
                                }
                                return false;
                            }
                            break;
                            break;
                        case 5:
                            if (isSubdivision(locale, subtag)) {
                                break;
                            }
                            return where.set(datatype, key + LanguageTag.SEP + subtag);
                        default:
                            if (KeyTypeData.toBcpType(key, subtag, new Output(), new Output()) == null) {
                                return where.set(datatype, key + LanguageTag.SEP + subtag);
                            }
                            if (!this.allowsDeprecated && KeyTypeData.isDeprecated(key, subtag)) {
                                return where.set(datatype, key + LanguageTag.SEP + subtag);
                            }
                    }
                }
                i = i2 + 1;
            } else if (tBuffer == null || tBuffer.length() == 0 || (isValidLocale(tBuffer.toString(), where) ^ 1) == 0) {
                return true;
            } else {
                return false;
            }
        }
        return where.set(datatype, key + LanguageTag.SEP + subtag);
    }

    private boolean isSubdivision(ULocale locale, String subtag) {
        int i = 3;
        if (subtag.length() < 3) {
            return false;
        }
        if (subtag.charAt(0) > '9') {
            i = 2;
        }
        String region = subtag.substring(0, i);
        if (ValidIdentifiers.isValid(Datatype.subdivision, this.datasubtypes, region, subtag.substring(region.length())) == null) {
            return false;
        }
        String localeRegion = locale.getCountry();
        if (localeRegion.isEmpty()) {
            localeRegion = ULocale.addLikelySubtags(locale).getCountry();
        }
        if (region.equalsIgnoreCase(localeRegion)) {
            return true;
        }
        return false;
    }

    private boolean isScriptReorder(String subtag) {
        boolean z = true;
        subtag = AsciiUtil.toLowerString(subtag);
        if (REORDERING_INCLUDE.contains(subtag)) {
            return true;
        }
        if (REORDERING_EXCLUDE.contains(subtag)) {
            return false;
        }
        if (ValidIdentifiers.isValid(Datatype.script, REGULAR_ONLY, subtag) == null) {
            z = false;
        }
        return z;
    }

    private boolean isValidLocale(String extensionString, Where where) {
        try {
            return isValid(new Builder().setLanguageTag(extensionString).build(), where);
        } catch (IllformedLocaleException e) {
            return where.set(Datatype.t, SEPARATOR.split(extensionString.substring(e.getErrorIndex()))[0]);
        } catch (Exception e2) {
            return where.set(Datatype.t, e2.getMessage());
        }
    }

    private boolean isValid(Datatype datatype, String code, Where where) {
        if (code.isEmpty() || ValidIdentifiers.isValid(datatype, this.datasubtypes, code) != null) {
            return true;
        }
        if (where == null) {
            return false;
        }
        return where.set(datatype, code);
    }
}

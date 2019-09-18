package android.icu.impl.locale;

import android.icu.impl.ValidIdentifiers;
import android.icu.impl.locale.KeyTypeData;
import android.icu.text.DateFormat;
import android.icu.util.IllformedLocaleException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class LocaleValidityChecker {
    static final Set<ValidIdentifiers.Datasubtype> REGULAR_ONLY = EnumSet.of(ValidIdentifiers.Datasubtype.regular);
    static final Set<String> REORDERING_EXCLUDE = new HashSet(Arrays.asList(new String[]{"zinh", "zyyy"}));
    static final Set<String> REORDERING_INCLUDE = new HashSet(Arrays.asList(new String[]{"space", "punct", "symbol", "currency", "digit", "others", DateFormat.SPECIFIC_TZ}));
    static Pattern SEPARATOR = Pattern.compile("[-_]");
    private static final Pattern VALID_X = Pattern.compile("[a-zA-Z0-9]{2,8}(-[a-zA-Z0-9]{2,8})*");
    private final boolean allowsDeprecated;
    private final Set<ValidIdentifiers.Datasubtype> datasubtypes;

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
        public ValidIdentifiers.Datatype fieldFailure;

        public boolean set(ValidIdentifiers.Datatype datatype, String code) {
            this.fieldFailure = datatype;
            this.codeFailure = code;
            return false;
        }

        public String toString() {
            if (this.fieldFailure == null) {
                return "OK";
            }
            return "{" + this.fieldFailure + ", " + this.codeFailure + "}";
        }
    }

    public LocaleValidityChecker(Set<ValidIdentifiers.Datasubtype> datasubtypes2) {
        this.datasubtypes = EnumSet.copyOf(datasubtypes2);
        this.allowsDeprecated = datasubtypes2.contains(ValidIdentifiers.Datasubtype.deprecated);
    }

    public LocaleValidityChecker(ValidIdentifiers.Datasubtype... datasubtypes2) {
        this.datasubtypes = EnumSet.copyOf(Arrays.asList(datasubtypes2));
        this.allowsDeprecated = this.datasubtypes.contains(ValidIdentifiers.Datasubtype.deprecated);
    }

    public Set<ValidIdentifiers.Datasubtype> getDatasubtypes() {
        return EnumSet.copyOf(this.datasubtypes);
    }

    public boolean isValid(ULocale locale, Where where) {
        Character c;
        where.set(null, null);
        String language = locale.getLanguage();
        String script = locale.getScript();
        String region = locale.getCountry();
        String variantString = locale.getVariant();
        Set<Character> extensionKeys = locale.getExtensionKeys();
        if (!isValid(ValidIdentifiers.Datatype.language, language, where)) {
            if (!language.equals(LanguageTag.PRIVATEUSE)) {
                return false;
            }
            where.set(null, null);
            return true;
        } else if (!isValid(ValidIdentifiers.Datatype.script, script, where) || !isValid(ValidIdentifiers.Datatype.region, region, where)) {
            return false;
        } else {
            if (!variantString.isEmpty()) {
                for (String variant : SEPARATOR.split(variantString)) {
                    if (!isValid(ValidIdentifiers.Datatype.variant, variant, where)) {
                        return false;
                    }
                }
            }
            Iterator<Character> it = extensionKeys.iterator();
            while (it.hasNext()) {
                try {
                    ValidIdentifiers.Datatype datatype = ValidIdentifiers.Datatype.valueOf(c + "");
                    switch (datatype) {
                        case x:
                            return true;
                        case t:
                        case u:
                            if (isValidU(locale, datatype, locale.getExtension(c.charValue()), where)) {
                                break;
                            } else {
                                return false;
                            }
                    }
                } catch (Exception e) {
                    return where.set(ValidIdentifiers.Datatype.illegal, c + "");
                }
            }
            return true;
        }
    }

    private boolean isValidU(ULocale locale, ValidIdentifiers.Datatype datatype, String extensionString, Where where) {
        int typeCount;
        ValidIdentifiers.Datatype datatype2 = datatype;
        Where where2 = where;
        int typeCount2 = 0;
        KeyTypeData.ValueType valueType = null;
        StringBuilder prefix = new StringBuilder();
        Set<String> seen = new HashSet<>();
        StringBuilder tBuffer = datatype2 == ValidIdentifiers.Datatype.t ? new StringBuilder() : null;
        String[] split = SEPARATOR.split(extensionString);
        int length = split.length;
        SpecialCase specialCase = null;
        String key = "";
        int i = 0;
        while (i < length) {
            String subtag = split[i];
            String[] strArr = split;
            if (subtag.length() == 2 && (tBuffer == null || subtag.charAt(1) <= '9')) {
                if (tBuffer != null) {
                    if (tBuffer.length() != 0 && !isValidLocale(tBuffer.toString(), where2)) {
                        return false;
                    }
                    tBuffer = null;
                }
                key = KeyTypeData.toBcpKey(subtag);
                if (key == null) {
                    return where2.set(datatype2, subtag);
                }
                if (!this.allowsDeprecated && KeyTypeData.isDeprecated(key)) {
                    return where2.set(datatype2, key);
                }
                valueType = KeyTypeData.getValueType(key);
                typeCount2 = 0;
                specialCase = SpecialCase.get(key);
            } else if (tBuffer != null) {
                if (tBuffer.length() != 0) {
                    tBuffer.append('-');
                }
                tBuffer.append(subtag);
            } else {
                int typeCount3 = typeCount2 + 1;
                switch (valueType) {
                    case single:
                        if (typeCount3 > 1) {
                            return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                        }
                        break;
                    case incremental:
                        if (typeCount3 != 1) {
                            prefix.append('-');
                            prefix.append(subtag);
                            subtag = prefix.toString();
                            break;
                        } else {
                            prefix.setLength(0);
                            prefix.append(subtag);
                            break;
                        }
                    case multiple:
                        if (typeCount3 == 1) {
                            seen.clear();
                            break;
                        }
                        break;
                }
                switch (specialCase) {
                    case anything:
                        ULocale uLocale = locale;
                        typeCount = typeCount3;
                        break;
                    case codepoints:
                        ULocale uLocale2 = locale;
                        typeCount = typeCount3;
                        try {
                            if (Integer.parseInt(subtag, 16) > 1114111) {
                                return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                            }
                        } catch (NumberFormatException e) {
                            return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                        }
                        break;
                    case reorder:
                        ULocale uLocale3 = locale;
                        typeCount = typeCount3;
                        if (!seen.add(subtag.equals(DateFormat.SPECIFIC_TZ) ? "others" : subtag) || !isScriptReorder(subtag)) {
                            return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                        }
                    case subdivision:
                        typeCount = typeCount3;
                        if (!isSubdivision(locale, subtag)) {
                            return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                        }
                        break;
                    case rgKey:
                        if (subtag.length() >= 6) {
                            if (subtag.endsWith(DateFormat.SPECIFIC_TZ)) {
                                typeCount = typeCount3;
                                if (isValid(ValidIdentifiers.Datatype.region, subtag.substring(0, subtag.length() - 4), where2)) {
                                    ULocale uLocale4 = locale;
                                    break;
                                } else {
                                    return false;
                                }
                            } else {
                                int i2 = typeCount3;
                            }
                        }
                        return where2.set(datatype2, subtag);
                    default:
                        ULocale uLocale5 = locale;
                        typeCount = typeCount3;
                        Output<Boolean> isKnownKey = new Output<>();
                        if (KeyTypeData.toBcpType(key, subtag, isKnownKey, new Output<>()) == null) {
                            return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                        }
                        Output<Boolean> output = isKnownKey;
                        if (!this.allowsDeprecated && KeyTypeData.isDeprecated(key, subtag)) {
                            return where2.set(datatype2, key + LanguageTag.SEP + subtag);
                        }
                }
                typeCount2 = typeCount;
                i++;
                split = strArr;
            }
            ULocale uLocale6 = locale;
            i++;
            split = strArr;
        }
        ULocale uLocale7 = locale;
        if (tBuffer == null || tBuffer.length() == 0 || isValidLocale(tBuffer.toString(), where2)) {
            return true;
        }
        return false;
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
        if (ValidIdentifiers.isValid(ValidIdentifiers.Datatype.subdivision, this.datasubtypes, region, subtag.substring(region.length())) == null) {
            return false;
        }
        String localeRegion = locale.getCountry();
        if (localeRegion.isEmpty()) {
            localeRegion = ULocale.addLikelySubtags(locale).getCountry();
        }
        if (!region.equalsIgnoreCase(localeRegion)) {
            return false;
        }
        return true;
    }

    private boolean isScriptReorder(String subtag) {
        String subtag2 = AsciiUtil.toLowerString(subtag);
        boolean z = true;
        if (REORDERING_INCLUDE.contains(subtag2)) {
            return true;
        }
        if (REORDERING_EXCLUDE.contains(subtag2)) {
            return false;
        }
        if (ValidIdentifiers.isValid(ValidIdentifiers.Datatype.script, REGULAR_ONLY, subtag2) == null) {
            z = false;
        }
        return z;
    }

    private boolean isValidLocale(String extensionString, Where where) {
        try {
            return isValid(new ULocale.Builder().setLanguageTag(extensionString).build(), where);
        } catch (IllformedLocaleException e) {
            return where.set(ValidIdentifiers.Datatype.t, SEPARATOR.split(extensionString.substring(e.getErrorIndex()))[0]);
        } catch (Exception e2) {
            return where.set(ValidIdentifiers.Datatype.t, e2.getMessage());
        }
    }

    private boolean isValid(ValidIdentifiers.Datatype datatype, String code, Where where) {
        boolean z = true;
        if (code.isEmpty()) {
            return true;
        }
        if (datatype == ValidIdentifiers.Datatype.variant && "posix".equalsIgnoreCase(code)) {
            return true;
        }
        if (ValidIdentifiers.isValid(datatype, this.datasubtypes, code) == null) {
            z = where == null ? false : where.set(datatype, code);
        }
        return z;
    }
}

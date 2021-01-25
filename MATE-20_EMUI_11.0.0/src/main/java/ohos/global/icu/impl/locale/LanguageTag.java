package ohos.global.icu.impl.locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.global.icu.impl.locale.AsciiUtil;

public class LanguageTag {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final Map<AsciiUtil.CaseInsensitiveKey, String[]> GRANDFATHERED = new HashMap();
    private static final boolean JDKIMPL = false;
    public static final String PRIVATEUSE = "x";
    public static final String PRIVUSE_VARIANT_PREFIX = "lvariant";
    public static final String SEP = "-";
    public static String UNDETERMINED = "und";
    private List<String> _extensions = Collections.emptyList();
    private List<String> _extlangs = Collections.emptyList();
    private String _language = "";
    private String _privateuse = "";
    private String _region = "";
    private String _script = "";
    private List<String> _variants = Collections.emptyList();

    static {
        String[][] strArr = {new String[]{"art-lojban", "jbo"}, new String[]{"cel-gaulish", "xtg-x-cel-gaulish"}, new String[]{"en-GB-oed", "en-GB-x-oed"}, new String[]{"i-ami", "ami"}, new String[]{"i-bnn", "bnn"}, new String[]{"i-default", "en-x-i-default"}, new String[]{"i-enochian", "und-x-i-enochian"}, new String[]{"i-hak", "hak"}, new String[]{"i-klingon", "tlh"}, new String[]{"i-lux", "lb"}, new String[]{"i-mingo", "see-x-i-mingo"}, new String[]{"i-navajo", "nv"}, new String[]{"i-pwn", "pwn"}, new String[]{"i-tao", "tao"}, new String[]{"i-tay", "tay"}, new String[]{"i-tsu", "tsu"}, new String[]{"no-bok", "nb"}, new String[]{"no-nyn", "nn"}, new String[]{"sgn-BE-FR", "sfb"}, new String[]{"sgn-BE-NL", "vgt"}, new String[]{"sgn-CH-DE", "sgg"}, new String[]{"zh-guoyu", "cmn"}, new String[]{"zh-hakka", "hak"}, new String[]{"zh-min", "nan-x-zh-min"}, new String[]{"zh-min-nan", "nan"}, new String[]{"zh-xiang", "hsn"}};
        for (String[] strArr2 : strArr) {
            GRANDFATHERED.put(new AsciiUtil.CaseInsensitiveKey(strArr2[0]), strArr2);
        }
    }

    private LanguageTag() {
    }

    public static LanguageTag parse(String str, ParseStatus parseStatus) {
        boolean z;
        StringTokenIterator stringTokenIterator;
        if (parseStatus == null) {
            parseStatus = new ParseStatus();
        } else {
            parseStatus.reset();
        }
        String[] strArr = GRANDFATHERED.get(new AsciiUtil.CaseInsensitiveKey(str));
        int i = 2;
        while (true) {
            z = false;
            if (strArr != null || (i = str.indexOf(45, i + 1)) == -1) {
                break;
            }
            strArr = GRANDFATHERED.get(new AsciiUtil.CaseInsensitiveKey(str.substring(0, i)));
        }
        if (strArr != null) {
            if (strArr[0].length() == str.length()) {
                stringTokenIterator = new StringTokenIterator(strArr[1], SEP);
            } else {
                stringTokenIterator = new StringTokenIterator(strArr[1] + str.substring(i), SEP);
            }
            z = true;
        } else {
            stringTokenIterator = new StringTokenIterator(str, SEP);
        }
        LanguageTag languageTag = new LanguageTag();
        if (languageTag.parseLanguage(stringTokenIterator, parseStatus)) {
            if (languageTag._language.length() <= 3) {
                languageTag.parseExtlangs(stringTokenIterator, parseStatus);
            }
            languageTag.parseScript(stringTokenIterator, parseStatus);
            languageTag.parseRegion(stringTokenIterator, parseStatus);
            languageTag.parseVariants(stringTokenIterator, parseStatus);
            languageTag.parseExtensions(stringTokenIterator, parseStatus);
        }
        languageTag.parsePrivateuse(stringTokenIterator, parseStatus);
        if (z) {
            parseStatus._parseLength = str.length();
        } else if (!stringTokenIterator.isDone() && !parseStatus.isError()) {
            String current = stringTokenIterator.current();
            parseStatus._errorIndex = stringTokenIterator.currentStart();
            if (current.length() == 0) {
                parseStatus._errorMsg = "Empty subtag";
            } else {
                parseStatus._errorMsg = "Invalid subtag: " + current;
            }
        }
        return languageTag;
    }

    private boolean parseLanguage(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        if (stringTokenIterator.isDone() || parseStatus.isError()) {
            return false;
        }
        String current = stringTokenIterator.current();
        if (!isLanguage(current)) {
            return false;
        }
        this._language = current;
        parseStatus._parseLength = stringTokenIterator.currentEnd();
        stringTokenIterator.next();
        return true;
    }

    private boolean parseExtlangs(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        boolean z = false;
        if (!stringTokenIterator.isDone() && !parseStatus.isError()) {
            while (!stringTokenIterator.isDone()) {
                String current = stringTokenIterator.current();
                if (isExtlang(current)) {
                    z = true;
                    if (this._extlangs.isEmpty()) {
                        this._extlangs = new ArrayList(3);
                    }
                    this._extlangs.add(current);
                    parseStatus._parseLength = stringTokenIterator.currentEnd();
                    stringTokenIterator.next();
                    if (this._extlangs.size() == 3) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return z;
    }

    private boolean parseScript(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        if (stringTokenIterator.isDone() || parseStatus.isError()) {
            return false;
        }
        String current = stringTokenIterator.current();
        if (!isScript(current)) {
            return false;
        }
        this._script = current;
        parseStatus._parseLength = stringTokenIterator.currentEnd();
        stringTokenIterator.next();
        return true;
    }

    private boolean parseRegion(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        if (stringTokenIterator.isDone() || parseStatus.isError()) {
            return false;
        }
        String current = stringTokenIterator.current();
        if (!isRegion(current)) {
            return false;
        }
        this._region = current;
        parseStatus._parseLength = stringTokenIterator.currentEnd();
        stringTokenIterator.next();
        return true;
    }

    private boolean parseVariants(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        boolean z = false;
        if (!stringTokenIterator.isDone() && !parseStatus.isError()) {
            while (!stringTokenIterator.isDone()) {
                String current = stringTokenIterator.current();
                if (!isVariant(current)) {
                    break;
                }
                z = true;
                if (this._variants.isEmpty()) {
                    this._variants = new ArrayList(3);
                }
                String upperCase = current.toUpperCase();
                if (!this._variants.contains(upperCase)) {
                    this._variants.add(upperCase);
                }
                parseStatus._parseLength = stringTokenIterator.currentEnd();
                stringTokenIterator.next();
            }
        }
        return z;
    }

    private boolean parseExtensions(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        if (stringTokenIterator.isDone() || parseStatus.isError()) {
            return false;
        }
        boolean z = false;
        while (true) {
            if (stringTokenIterator.isDone()) {
                break;
            }
            String current = stringTokenIterator.current();
            if (!isExtensionSingleton(current)) {
                break;
            }
            int currentStart = stringTokenIterator.currentStart();
            String lowerCase = current.toLowerCase();
            StringBuilder sb = new StringBuilder(lowerCase);
            stringTokenIterator.next();
            while (!stringTokenIterator.isDone()) {
                String current2 = stringTokenIterator.current();
                if (!isExtensionSubtag(current2)) {
                    break;
                }
                sb.append(SEP);
                sb.append(current2);
                parseStatus._parseLength = stringTokenIterator.currentEnd();
                stringTokenIterator.next();
            }
            if (parseStatus._parseLength <= currentStart) {
                parseStatus._errorIndex = currentStart;
                parseStatus._errorMsg = "Incomplete extension '" + lowerCase + "'";
                break;
            }
            if (this._extensions.size() == 0) {
                this._extensions = new ArrayList(4);
            }
            boolean z2 = false;
            for (String str : this._extensions) {
                z2 |= str.charAt(0) == sb.charAt(0);
            }
            if (!z2) {
                this._extensions.add(sb.toString());
            }
            z = true;
        }
        return z;
    }

    private boolean parsePrivateuse(StringTokenIterator stringTokenIterator, ParseStatus parseStatus) {
        if (stringTokenIterator.isDone() || parseStatus.isError()) {
            return false;
        }
        String current = stringTokenIterator.current();
        if (!isPrivateusePrefix(current)) {
            return false;
        }
        int currentStart = stringTokenIterator.currentStart();
        StringBuilder sb = new StringBuilder(current);
        stringTokenIterator.next();
        while (!stringTokenIterator.isDone()) {
            String current2 = stringTokenIterator.current();
            if (!isPrivateuseSubtag(current2)) {
                break;
            }
            sb.append(SEP);
            sb.append(current2);
            parseStatus._parseLength = stringTokenIterator.currentEnd();
            stringTokenIterator.next();
        }
        if (parseStatus._parseLength <= currentStart) {
            parseStatus._errorIndex = currentStart;
            parseStatus._errorMsg = "Incomplete privateuse";
            return false;
        }
        this._privateuse = sb.toString();
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00f1  */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x0131  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x016a  */
    public static LanguageTag parseLocale(BaseLocale baseLocale, LocaleExtensions localeExtensions) {
        boolean z;
        String str;
        String str2;
        LanguageTag languageTag = new LanguageTag();
        String language = baseLocale.getLanguage();
        String script = baseLocale.getScript();
        String region = baseLocale.getRegion();
        String variant = baseLocale.getVariant();
        if (language.length() > 0 && isLanguage(language)) {
            if (language.equals("iw")) {
                language = "he";
            } else if (language.equals("ji")) {
                language = "yi";
            } else if (language.equals("in")) {
                language = "id";
            }
            languageTag._language = language;
        }
        if (script.length() <= 0 || !isScript(script)) {
            z = false;
        } else {
            languageTag._script = canonicalizeScript(script);
            z = true;
        }
        if (region.length() > 0 && isRegion(region)) {
            languageTag._region = canonicalizeRegion(region);
            z = true;
        }
        ArrayList arrayList = null;
        if (variant.length() > 0) {
            StringTokenIterator stringTokenIterator = new StringTokenIterator(variant, "_");
            ArrayList arrayList2 = null;
            while (!stringTokenIterator.isDone()) {
                String current = stringTokenIterator.current();
                if (!isVariant(current)) {
                    break;
                }
                if (arrayList2 == null) {
                    arrayList2 = new ArrayList();
                }
                arrayList2.add(canonicalizeVariant(current));
                stringTokenIterator.next();
            }
            if (arrayList2 != null) {
                languageTag._variants = arrayList2;
                z = true;
            }
            if (!stringTokenIterator.isDone()) {
                StringBuilder sb = new StringBuilder();
                while (!stringTokenIterator.isDone()) {
                    String current2 = stringTokenIterator.current();
                    if (!isPrivateuseSubtag(current2)) {
                        break;
                    }
                    if (sb.length() > 0) {
                        sb.append(SEP);
                    }
                    sb.append(AsciiUtil.toLowerString(current2));
                    stringTokenIterator.next();
                }
                if (sb.length() > 0) {
                    str = sb.toString();
                    str2 = null;
                    for (Character ch : localeExtensions.getKeys()) {
                        Extension extension = localeExtensions.getExtension(ch);
                        if (isPrivateusePrefixChar(ch.charValue())) {
                            str2 = extension.getValue();
                        } else {
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                            }
                            arrayList.add(ch.toString() + SEP + extension.getValue());
                        }
                    }
                    if (arrayList != null) {
                        languageTag._extensions = arrayList;
                        z = true;
                    }
                    if (str != null) {
                        str2 = str2 == null ? "lvariant-" + str : str2 + SEP + PRIVUSE_VARIANT_PREFIX + SEP + str.replace("_", SEP);
                    }
                    if (str2 != null) {
                        languageTag._privateuse = str2;
                    }
                    if (languageTag._language.length() == 0 && (z || str2 == null)) {
                        languageTag._language = UNDETERMINED;
                    }
                    return languageTag;
                }
            }
        }
        str = null;
        str2 = null;
        while (r2.hasNext()) {
        }
        if (arrayList != null) {
        }
        if (str != null) {
        }
        if (str2 != null) {
        }
        languageTag._language = UNDETERMINED;
        return languageTag;
    }

    public String getLanguage() {
        return this._language;
    }

    public List<String> getExtlangs() {
        return Collections.unmodifiableList(this._extlangs);
    }

    public String getScript() {
        return this._script;
    }

    public String getRegion() {
        return this._region;
    }

    public List<String> getVariants() {
        return Collections.unmodifiableList(this._variants);
    }

    public List<String> getExtensions() {
        return Collections.unmodifiableList(this._extensions);
    }

    public String getPrivateuse() {
        return this._privateuse;
    }

    public static boolean isLanguage(String str) {
        return str.length() >= 2 && str.length() <= 8 && AsciiUtil.isAlphaString(str);
    }

    public static boolean isExtlang(String str) {
        return str.length() == 3 && AsciiUtil.isAlphaString(str);
    }

    public static boolean isScript(String str) {
        return str.length() == 4 && AsciiUtil.isAlphaString(str);
    }

    public static boolean isRegion(String str) {
        return (str.length() == 2 && AsciiUtil.isAlphaString(str)) || (str.length() == 3 && AsciiUtil.isNumericString(str));
    }

    public static boolean isVariant(String str) {
        int length = str.length();
        if (length >= 5 && length <= 8) {
            return AsciiUtil.isAlphaNumericString(str);
        }
        if (length != 4) {
            return false;
        }
        if (!AsciiUtil.isNumeric(str.charAt(0)) || !AsciiUtil.isAlphaNumeric(str.charAt(1)) || !AsciiUtil.isAlphaNumeric(str.charAt(2)) || !AsciiUtil.isAlphaNumeric(str.charAt(3))) {
            return false;
        }
        return true;
    }

    public static boolean isExtensionSingleton(String str) {
        if (str.length() != 1 || !AsciiUtil.isAlphaNumericString(str) || AsciiUtil.caseIgnoreMatch(PRIVATEUSE, str)) {
            return false;
        }
        return true;
    }

    public static boolean isExtensionSingletonChar(char c) {
        return isExtensionSingleton(String.valueOf(c));
    }

    public static boolean isExtensionSubtag(String str) {
        return str.length() >= 2 && str.length() <= 8 && AsciiUtil.isAlphaNumericString(str);
    }

    public static boolean isPrivateusePrefix(String str) {
        if (str.length() != 1 || !AsciiUtil.caseIgnoreMatch(PRIVATEUSE, str)) {
            return false;
        }
        return true;
    }

    public static boolean isPrivateusePrefixChar(char c) {
        return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c));
    }

    public static boolean isPrivateuseSubtag(String str) {
        return str.length() >= 1 && str.length() <= 8 && AsciiUtil.isAlphaNumericString(str);
    }

    public static String canonicalizeLanguage(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizeExtlang(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizeScript(String str) {
        return AsciiUtil.toTitleString(str);
    }

    public static String canonicalizeRegion(String str) {
        return AsciiUtil.toUpperString(str);
    }

    public static String canonicalizeVariant(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizeExtension(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizeExtensionSingleton(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizeExtensionSubtag(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizePrivateuse(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public static String canonicalizePrivateuseSubtag(String str) {
        return AsciiUtil.toLowerString(str);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this._language.length() > 0) {
            sb.append(this._language);
            for (String str : this._extlangs) {
                sb.append(SEP);
                sb.append(str);
            }
            if (this._script.length() > 0) {
                sb.append(SEP);
                sb.append(this._script);
            }
            if (this._region.length() > 0) {
                sb.append(SEP);
                sb.append(this._region);
            }
            for (String str2 : this._variants) {
                sb.append(SEP);
                sb.append(str2);
            }
            for (String str3 : this._extensions) {
                sb.append(SEP);
                sb.append(str3);
            }
        }
        if (this._privateuse.length() > 0) {
            if (sb.length() > 0) {
                sb.append(SEP);
            }
            sb.append(this._privateuse);
        }
        return sb.toString();
    }
}

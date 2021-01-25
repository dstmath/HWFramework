package ohos.global.icu.impl.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InternalLocaleBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final boolean JDKIMPL = false;
    private static final CaseInsensitiveChar PRIVUSE_KEY = new CaseInsensitiveChar(LanguageTag.PRIVATEUSE.charAt(0));
    private HashMap<CaseInsensitiveChar, String> _extensions;
    private String _language = "";
    private String _region = "";
    private String _script = "";
    private HashSet<CaseInsensitiveString> _uattributes;
    private HashMap<CaseInsensitiveString, String> _ukeywords;
    private String _variant = "";

    public InternalLocaleBuilder setLanguage(String str) throws LocaleSyntaxException {
        if (str == null || str.length() == 0) {
            this._language = "";
        } else if (LanguageTag.isLanguage(str)) {
            this._language = str;
        } else {
            throw new LocaleSyntaxException("Ill-formed language: " + str, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setScript(String str) throws LocaleSyntaxException {
        if (str == null || str.length() == 0) {
            this._script = "";
        } else if (LanguageTag.isScript(str)) {
            this._script = str;
        } else {
            throw new LocaleSyntaxException("Ill-formed script: " + str, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setRegion(String str) throws LocaleSyntaxException {
        if (str == null || str.length() == 0) {
            this._region = "";
        } else if (LanguageTag.isRegion(str)) {
            this._region = str;
        } else {
            throw new LocaleSyntaxException("Ill-formed region: " + str, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setVariant(String str) throws LocaleSyntaxException {
        if (str == null || str.length() == 0) {
            this._variant = "";
        } else {
            String replaceAll = str.replaceAll(LanguageTag.SEP, "_");
            int checkVariants = checkVariants(replaceAll, "_");
            if (checkVariants == -1) {
                this._variant = replaceAll;
            } else {
                throw new LocaleSyntaxException("Ill-formed variant: " + str, checkVariants);
            }
        }
        return this;
    }

    public InternalLocaleBuilder addUnicodeLocaleAttribute(String str) throws LocaleSyntaxException {
        if (str == null || !UnicodeLocaleExtension.isAttribute(str)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + str);
        }
        if (this._uattributes == null) {
            this._uattributes = new HashSet<>(4);
        }
        this._uattributes.add(new CaseInsensitiveString(str));
        return this;
    }

    public InternalLocaleBuilder removeUnicodeLocaleAttribute(String str) throws LocaleSyntaxException {
        if (str == null || !UnicodeLocaleExtension.isAttribute(str)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + str);
        }
        HashSet<CaseInsensitiveString> hashSet = this._uattributes;
        if (hashSet != null) {
            hashSet.remove(new CaseInsensitiveString(str));
        }
        return this;
    }

    public InternalLocaleBuilder setUnicodeLocaleKeyword(String str, String str2) throws LocaleSyntaxException {
        if (UnicodeLocaleExtension.isKey(str)) {
            CaseInsensitiveString caseInsensitiveString = new CaseInsensitiveString(str);
            if (str2 == null) {
                HashMap<CaseInsensitiveString, String> hashMap = this._ukeywords;
                if (hashMap != null) {
                    hashMap.remove(caseInsensitiveString);
                }
            } else {
                if (str2.length() != 0) {
                    StringTokenIterator stringTokenIterator = new StringTokenIterator(str2.replaceAll("_", LanguageTag.SEP), LanguageTag.SEP);
                    while (!stringTokenIterator.isDone()) {
                        if (UnicodeLocaleExtension.isTypeSubtag(stringTokenIterator.current())) {
                            stringTokenIterator.next();
                        } else {
                            throw new LocaleSyntaxException("Ill-formed Unicode locale keyword type: " + str2, stringTokenIterator.currentStart());
                        }
                    }
                }
                if (this._ukeywords == null) {
                    this._ukeywords = new HashMap<>(4);
                }
                this._ukeywords.put(caseInsensitiveString, str2);
            }
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed Unicode locale keyword key: " + str);
    }

    public InternalLocaleBuilder setExtension(char c, String str) throws LocaleSyntaxException {
        boolean z;
        boolean isPrivateusePrefixChar = LanguageTag.isPrivateusePrefixChar(c);
        if (isPrivateusePrefixChar || LanguageTag.isExtensionSingletonChar(c)) {
            boolean z2 = str == null || str.length() == 0;
            CaseInsensitiveChar caseInsensitiveChar = new CaseInsensitiveChar(c);
            if (!z2) {
                String replaceAll = str.replaceAll("_", LanguageTag.SEP);
                StringTokenIterator stringTokenIterator = new StringTokenIterator(replaceAll, LanguageTag.SEP);
                while (!stringTokenIterator.isDone()) {
                    String current = stringTokenIterator.current();
                    if (isPrivateusePrefixChar) {
                        z = LanguageTag.isPrivateuseSubtag(current);
                    } else {
                        z = LanguageTag.isExtensionSubtag(current);
                    }
                    if (z) {
                        stringTokenIterator.next();
                    } else {
                        throw new LocaleSyntaxException("Ill-formed extension value: " + current, stringTokenIterator.currentStart());
                    }
                }
                if (UnicodeLocaleExtension.isSingletonChar(caseInsensitiveChar.value())) {
                    setUnicodeLocaleExtension(replaceAll);
                } else {
                    if (this._extensions == null) {
                        this._extensions = new HashMap<>(4);
                    }
                    this._extensions.put(caseInsensitiveChar, replaceAll);
                }
            } else if (UnicodeLocaleExtension.isSingletonChar(caseInsensitiveChar.value())) {
                HashSet<CaseInsensitiveString> hashSet = this._uattributes;
                if (hashSet != null) {
                    hashSet.clear();
                }
                HashMap<CaseInsensitiveString, String> hashMap = this._ukeywords;
                if (hashMap != null) {
                    hashMap.clear();
                }
            } else {
                HashMap<CaseInsensitiveChar, String> hashMap2 = this._extensions;
                if (hashMap2 != null && hashMap2.containsKey(caseInsensitiveChar)) {
                    this._extensions.remove(caseInsensitiveChar);
                }
            }
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed extension key: " + c);
    }

    public InternalLocaleBuilder setExtensions(String str) throws LocaleSyntaxException {
        if (str == null || str.length() == 0) {
            clearExtensions();
            return this;
        }
        String replaceAll = str.replaceAll("_", LanguageTag.SEP);
        StringTokenIterator stringTokenIterator = new StringTokenIterator(replaceAll, LanguageTag.SEP);
        String str2 = null;
        int i = 0;
        ArrayList arrayList = null;
        while (!stringTokenIterator.isDone()) {
            String current = stringTokenIterator.current();
            if (!LanguageTag.isExtensionSingleton(current)) {
                break;
            }
            int currentStart = stringTokenIterator.currentStart();
            StringBuilder sb = new StringBuilder(current);
            stringTokenIterator.next();
            while (!stringTokenIterator.isDone()) {
                String current2 = stringTokenIterator.current();
                if (!LanguageTag.isExtensionSubtag(current2)) {
                    break;
                }
                sb.append(LanguageTag.SEP);
                sb.append(current2);
                i = stringTokenIterator.currentEnd();
                stringTokenIterator.next();
            }
            if (i >= currentStart) {
                if (arrayList == null) {
                    arrayList = new ArrayList(4);
                }
                arrayList.add(sb.toString());
            } else {
                throw new LocaleSyntaxException("Incomplete extension '" + current + "'", currentStart);
            }
        }
        if (!stringTokenIterator.isDone()) {
            String current3 = stringTokenIterator.current();
            if (LanguageTag.isPrivateusePrefix(current3)) {
                int currentStart2 = stringTokenIterator.currentStart();
                StringBuilder sb2 = new StringBuilder(current3);
                stringTokenIterator.next();
                while (!stringTokenIterator.isDone()) {
                    String current4 = stringTokenIterator.current();
                    if (!LanguageTag.isPrivateuseSubtag(current4)) {
                        break;
                    }
                    sb2.append(LanguageTag.SEP);
                    sb2.append(current4);
                    i = stringTokenIterator.currentEnd();
                    stringTokenIterator.next();
                }
                if (i > currentStart2) {
                    str2 = sb2.toString();
                } else {
                    throw new LocaleSyntaxException("Incomplete privateuse:" + replaceAll.substring(currentStart2), currentStart2);
                }
            }
        }
        if (stringTokenIterator.isDone()) {
            return setExtensions(arrayList, str2);
        }
        throw new LocaleSyntaxException("Ill-formed extension subtags:" + replaceAll.substring(stringTokenIterator.currentStart()), stringTokenIterator.currentStart());
    }

    private InternalLocaleBuilder setExtensions(List<String> list, String str) {
        clearExtensions();
        if (list != null && list.size() > 0) {
            HashSet hashSet = new HashSet(list.size());
            for (String str2 : list) {
                CaseInsensitiveChar caseInsensitiveChar = new CaseInsensitiveChar(str2.charAt(0));
                if (!hashSet.contains(caseInsensitiveChar)) {
                    if (UnicodeLocaleExtension.isSingletonChar(caseInsensitiveChar.value())) {
                        setUnicodeLocaleExtension(str2.substring(2));
                    } else {
                        if (this._extensions == null) {
                            this._extensions = new HashMap<>(4);
                        }
                        this._extensions.put(caseInsensitiveChar, str2.substring(2));
                    }
                }
            }
        }
        if (str != null && str.length() > 0) {
            if (this._extensions == null) {
                this._extensions = new HashMap<>(1);
            }
            this._extensions.put(new CaseInsensitiveChar(str.charAt(0)), str.substring(2));
        }
        return this;
    }

    public InternalLocaleBuilder setLanguageTag(LanguageTag languageTag) {
        clear();
        if (languageTag.getExtlangs().size() > 0) {
            this._language = languageTag.getExtlangs().get(0);
        } else {
            String language = languageTag.getLanguage();
            if (!language.equals(LanguageTag.UNDETERMINED)) {
                this._language = language;
            }
        }
        this._script = languageTag.getScript();
        this._region = languageTag.getRegion();
        List<String> variants = languageTag.getVariants();
        if (variants.size() > 0) {
            StringBuilder sb = new StringBuilder(variants.get(0));
            for (int i = 1; i < variants.size(); i++) {
                sb.append("_");
                sb.append(variants.get(i));
            }
            this._variant = sb.toString();
        }
        setExtensions(languageTag.getExtensions(), languageTag.getPrivateuse());
        return this;
    }

    public InternalLocaleBuilder setLocale(BaseLocale baseLocale, LocaleExtensions localeExtensions) throws LocaleSyntaxException {
        Set<Character> set;
        int checkVariants;
        String language = baseLocale.getLanguage();
        String script = baseLocale.getScript();
        String region = baseLocale.getRegion();
        String variant = baseLocale.getVariant();
        if (language.length() > 0 && !LanguageTag.isLanguage(language)) {
            throw new LocaleSyntaxException("Ill-formed language: " + language);
        } else if (script.length() > 0 && !LanguageTag.isScript(script)) {
            throw new LocaleSyntaxException("Ill-formed script: " + script);
        } else if (region.length() > 0 && !LanguageTag.isRegion(region)) {
            throw new LocaleSyntaxException("Ill-formed region: " + region);
        } else if (variant.length() <= 0 || (checkVariants = checkVariants(variant, "_")) == -1) {
            this._language = language;
            this._script = script;
            this._region = region;
            this._variant = variant;
            clearExtensions();
            if (localeExtensions == null) {
                set = null;
            } else {
                set = localeExtensions.getKeys();
            }
            if (set != null) {
                for (Character ch : set) {
                    Extension extension = localeExtensions.getExtension(ch);
                    if (extension instanceof UnicodeLocaleExtension) {
                        UnicodeLocaleExtension unicodeLocaleExtension = (UnicodeLocaleExtension) extension;
                        for (String str : unicodeLocaleExtension.getUnicodeLocaleAttributes()) {
                            if (this._uattributes == null) {
                                this._uattributes = new HashSet<>(4);
                            }
                            this._uattributes.add(new CaseInsensitiveString(str));
                        }
                        for (String str2 : unicodeLocaleExtension.getUnicodeLocaleKeys()) {
                            if (this._ukeywords == null) {
                                this._ukeywords = new HashMap<>(4);
                            }
                            this._ukeywords.put(new CaseInsensitiveString(str2), unicodeLocaleExtension.getUnicodeLocaleType(str2));
                        }
                    } else {
                        if (this._extensions == null) {
                            this._extensions = new HashMap<>(4);
                        }
                        this._extensions.put(new CaseInsensitiveChar(ch.charValue()), extension.getValue());
                    }
                }
            }
            return this;
        } else {
            throw new LocaleSyntaxException("Ill-formed variant: " + variant, checkVariants);
        }
    }

    public InternalLocaleBuilder clear() {
        this._language = "";
        this._script = "";
        this._region = "";
        this._variant = "";
        clearExtensions();
        return this;
    }

    public InternalLocaleBuilder clearExtensions() {
        HashMap<CaseInsensitiveChar, String> hashMap = this._extensions;
        if (hashMap != null) {
            hashMap.clear();
        }
        HashSet<CaseInsensitiveString> hashSet = this._uattributes;
        if (hashSet != null) {
            hashSet.clear();
        }
        HashMap<CaseInsensitiveString, String> hashMap2 = this._ukeywords;
        if (hashMap2 != null) {
            hashMap2.clear();
        }
        return this;
    }

    public BaseLocale getBaseLocale() {
        String str;
        int i;
        String str2 = this._language;
        String str3 = this._script;
        String str4 = this._region;
        String str5 = this._variant;
        HashMap<CaseInsensitiveChar, String> hashMap = this._extensions;
        if (!(hashMap == null || (str = hashMap.get(PRIVUSE_KEY)) == null)) {
            StringTokenIterator stringTokenIterator = new StringTokenIterator(str, LanguageTag.SEP);
            boolean z = false;
            while (true) {
                if (stringTokenIterator.isDone()) {
                    i = -1;
                    break;
                } else if (z) {
                    i = stringTokenIterator.currentStart();
                    break;
                } else {
                    if (AsciiUtil.caseIgnoreMatch(stringTokenIterator.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                        z = true;
                    }
                    stringTokenIterator.next();
                }
            }
            if (i != -1) {
                StringBuilder sb = new StringBuilder(str5);
                if (sb.length() != 0) {
                    sb.append("_");
                }
                sb.append(str.substring(i).replaceAll(LanguageTag.SEP, "_"));
                str5 = sb.toString();
            }
        }
        return BaseLocale.getInstance(str2, str3, str4, str5);
    }

    public LocaleExtensions getLocaleExtensions() {
        HashSet<CaseInsensitiveString> hashSet;
        HashMap<CaseInsensitiveString, String> hashMap;
        HashMap<CaseInsensitiveChar, String> hashMap2 = this._extensions;
        if ((hashMap2 == null || hashMap2.size() == 0) && (((hashSet = this._uattributes) == null || hashSet.size() == 0) && ((hashMap = this._ukeywords) == null || hashMap.size() == 0))) {
            return LocaleExtensions.EMPTY_EXTENSIONS;
        }
        return new LocaleExtensions(this._extensions, this._uattributes, this._ukeywords);
    }

    static String removePrivateuseVariant(String str) {
        boolean z;
        StringTokenIterator stringTokenIterator = new StringTokenIterator(str, LanguageTag.SEP);
        int i = -1;
        while (true) {
            if (stringTokenIterator.isDone()) {
                z = false;
                break;
            } else if (i != -1) {
                z = true;
                break;
            } else {
                if (AsciiUtil.caseIgnoreMatch(stringTokenIterator.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                    i = stringTokenIterator.currentStart();
                }
                stringTokenIterator.next();
            }
        }
        if (!z) {
            return str;
        }
        if (i == 0) {
            return null;
        }
        return str.substring(0, i - 1);
    }

    private int checkVariants(String str, String str2) {
        StringTokenIterator stringTokenIterator = new StringTokenIterator(str, str2);
        while (!stringTokenIterator.isDone()) {
            if (!LanguageTag.isVariant(stringTokenIterator.current())) {
                return stringTokenIterator.currentStart();
            }
            stringTokenIterator.next();
        }
        return -1;
    }

    private void setUnicodeLocaleExtension(String str) {
        String str2;
        HashSet<CaseInsensitiveString> hashSet = this._uattributes;
        if (hashSet != null) {
            hashSet.clear();
        }
        HashMap<CaseInsensitiveString, String> hashMap = this._ukeywords;
        if (hashMap != null) {
            hashMap.clear();
        }
        StringTokenIterator stringTokenIterator = new StringTokenIterator(str, LanguageTag.SEP);
        while (!stringTokenIterator.isDone() && UnicodeLocaleExtension.isAttribute(stringTokenIterator.current())) {
            if (this._uattributes == null) {
                this._uattributes = new HashSet<>(4);
            }
            this._uattributes.add(new CaseInsensitiveString(stringTokenIterator.current()));
            stringTokenIterator.next();
        }
        CaseInsensitiveString caseInsensitiveString = null;
        int i = -1;
        int i2 = -1;
        while (!stringTokenIterator.isDone()) {
            String str3 = "";
            if (caseInsensitiveString != null) {
                if (UnicodeLocaleExtension.isKey(stringTokenIterator.current())) {
                    if (i == -1) {
                        str2 = str3;
                    } else {
                        str2 = str.substring(i, i2);
                    }
                    if (this._ukeywords == null) {
                        this._ukeywords = new HashMap<>(4);
                    }
                    this._ukeywords.put(caseInsensitiveString, str2);
                    caseInsensitiveString = new CaseInsensitiveString(stringTokenIterator.current());
                    if (this._ukeywords.containsKey(caseInsensitiveString)) {
                        caseInsensitiveString = null;
                    }
                    i = -1;
                    i2 = -1;
                } else {
                    if (i == -1) {
                        i = stringTokenIterator.currentStart();
                    }
                    i2 = stringTokenIterator.currentEnd();
                }
            } else if (UnicodeLocaleExtension.isKey(stringTokenIterator.current())) {
                caseInsensitiveString = new CaseInsensitiveString(stringTokenIterator.current());
                HashMap<CaseInsensitiveString, String> hashMap2 = this._ukeywords;
                if (hashMap2 != null && hashMap2.containsKey(caseInsensitiveString)) {
                    caseInsensitiveString = null;
                }
            }
            if (stringTokenIterator.hasNext()) {
                stringTokenIterator.next();
            } else if (caseInsensitiveString != null) {
                if (i != -1) {
                    str3 = str.substring(i, i2);
                }
                if (this._ukeywords == null) {
                    this._ukeywords = new HashMap<>(4);
                }
                this._ukeywords.put(caseInsensitiveString, str3);
                return;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class CaseInsensitiveString {
        private String _s;

        CaseInsensitiveString(String str) {
            this._s = str;
        }

        public String value() {
            return this._s;
        }

        public int hashCode() {
            return AsciiUtil.toLowerString(this._s).hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveString)) {
                return false;
            }
            return AsciiUtil.caseIgnoreMatch(this._s, ((CaseInsensitiveString) obj).value());
        }
    }

    /* access modifiers changed from: package-private */
    public static class CaseInsensitiveChar {
        private char _c;

        CaseInsensitiveChar(char c) {
            this._c = c;
        }

        public char value() {
            return this._c;
        }

        public int hashCode() {
            return AsciiUtil.toLower(this._c);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveChar)) {
                return false;
            }
            return this._c == AsciiUtil.toLower(((CaseInsensitiveChar) obj).value());
        }
    }
}

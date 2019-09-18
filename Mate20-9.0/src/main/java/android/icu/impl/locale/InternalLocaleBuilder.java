package android.icu.impl.locale;

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

    static class CaseInsensitiveChar {
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
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveChar)) {
                return false;
            }
            if (this._c != AsciiUtil.toLower(((CaseInsensitiveChar) obj).value())) {
                z = false;
            }
            return z;
        }
    }

    static class CaseInsensitiveString {
        private String _s;

        CaseInsensitiveString(String s) {
            this._s = s;
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

    public InternalLocaleBuilder setLanguage(String language) throws LocaleSyntaxException {
        if (language == null || language.length() == 0) {
            this._language = "";
        } else if (LanguageTag.isLanguage(language)) {
            this._language = language;
        } else {
            throw new LocaleSyntaxException("Ill-formed language: " + language, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setScript(String script) throws LocaleSyntaxException {
        if (script == null || script.length() == 0) {
            this._script = "";
        } else if (LanguageTag.isScript(script)) {
            this._script = script;
        } else {
            throw new LocaleSyntaxException("Ill-formed script: " + script, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setRegion(String region) throws LocaleSyntaxException {
        if (region == null || region.length() == 0) {
            this._region = "";
        } else if (LanguageTag.isRegion(region)) {
            this._region = region;
        } else {
            throw new LocaleSyntaxException("Ill-formed region: " + region, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setVariant(String variant) throws LocaleSyntaxException {
        if (variant == null || variant.length() == 0) {
            this._variant = "";
        } else {
            String var = variant.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
            int errIdx = checkVariants(var, BaseLocale.SEP);
            if (errIdx == -1) {
                this._variant = var;
            } else {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
            }
        }
        return this;
    }

    public InternalLocaleBuilder addUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (attribute == null || !UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        if (this._uattributes == null) {
            this._uattributes = new HashSet<>(4);
        }
        this._uattributes.add(new CaseInsensitiveString(attribute));
        return this;
    }

    public InternalLocaleBuilder removeUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (attribute == null || !UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        if (this._uattributes != null) {
            this._uattributes.remove(new CaseInsensitiveString(attribute));
        }
        return this;
    }

    public InternalLocaleBuilder setUnicodeLocaleKeyword(String key, String type) throws LocaleSyntaxException {
        if (UnicodeLocaleExtension.isKey(key)) {
            CaseInsensitiveString cikey = new CaseInsensitiveString(key);
            if (type != null) {
                if (type.length() != 0) {
                    StringTokenIterator itr = new StringTokenIterator(type.replaceAll(BaseLocale.SEP, LanguageTag.SEP), LanguageTag.SEP);
                    while (!itr.isDone()) {
                        if (UnicodeLocaleExtension.isTypeSubtag(itr.current())) {
                            itr.next();
                        } else {
                            throw new LocaleSyntaxException("Ill-formed Unicode locale keyword type: " + type, itr.currentStart());
                        }
                    }
                }
                if (this._ukeywords == null) {
                    this._ukeywords = new HashMap<>(4);
                }
                this._ukeywords.put(cikey, type);
            } else if (this._ukeywords != null) {
                this._ukeywords.remove(cikey);
            }
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed Unicode locale keyword key: " + key);
    }

    public InternalLocaleBuilder setExtension(char singleton, String value) throws LocaleSyntaxException {
        boolean validSubtag;
        boolean isBcpPrivateuse = LanguageTag.isPrivateusePrefixChar(singleton);
        if (isBcpPrivateuse || LanguageTag.isExtensionSingletonChar(singleton)) {
            boolean remove = value == null || value.length() == 0;
            CaseInsensitiveChar key = new CaseInsensitiveChar(singleton);
            if (!remove) {
                String val = value.replaceAll(BaseLocale.SEP, LanguageTag.SEP);
                StringTokenIterator itr = new StringTokenIterator(val, LanguageTag.SEP);
                while (!itr.isDone()) {
                    String s = itr.current();
                    if (isBcpPrivateuse) {
                        validSubtag = LanguageTag.isPrivateuseSubtag(s);
                    } else {
                        validSubtag = LanguageTag.isExtensionSubtag(s);
                    }
                    if (validSubtag) {
                        itr.next();
                    } else {
                        throw new LocaleSyntaxException("Ill-formed extension value: " + s, itr.currentStart());
                    }
                }
                if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                    setUnicodeLocaleExtension(val);
                } else {
                    if (this._extensions == null) {
                        this._extensions = new HashMap<>(4);
                    }
                    this._extensions.put(key, val);
                }
            } else if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                if (this._uattributes != null) {
                    this._uattributes.clear();
                }
                if (this._ukeywords != null) {
                    this._ukeywords.clear();
                }
            } else if (this._extensions != null && this._extensions.containsKey(key)) {
                this._extensions.remove(key);
            }
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed extension key: " + singleton);
    }

    public InternalLocaleBuilder setExtensions(String subtags) throws LocaleSyntaxException {
        if (subtags == null || subtags.length() == 0) {
            clearExtensions();
            return this;
        }
        String subtags2 = subtags.replaceAll(BaseLocale.SEP, LanguageTag.SEP);
        StringTokenIterator itr = new StringTokenIterator(subtags2, LanguageTag.SEP);
        List<String> extensions = null;
        String privateuse = null;
        int parsed = 0;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!LanguageTag.isExtensionSingleton(s)) {
                break;
            }
            int start = itr.currentStart();
            String singleton = s;
            StringBuilder sb = new StringBuilder(singleton);
            itr.next();
            while (!itr.isDone()) {
                String s2 = itr.current();
                if (!LanguageTag.isExtensionSubtag(s2)) {
                    break;
                }
                sb.append(LanguageTag.SEP);
                sb.append(s2);
                parsed = itr.currentEnd();
                itr.next();
            }
            if (parsed >= start) {
                if (extensions == null) {
                    extensions = new ArrayList<>(4);
                }
                extensions.add(sb.toString());
            } else {
                throw new LocaleSyntaxException("Incomplete extension '" + singleton + "'", start);
            }
        }
        if (!itr.isDone()) {
            String s3 = itr.current();
            if (LanguageTag.isPrivateusePrefix(s3)) {
                int start2 = itr.currentStart();
                StringBuilder sb2 = new StringBuilder(s3);
                itr.next();
                while (!itr.isDone()) {
                    String s4 = itr.current();
                    if (!LanguageTag.isPrivateuseSubtag(s4)) {
                        break;
                    }
                    sb2.append(LanguageTag.SEP);
                    sb2.append(s4);
                    parsed = itr.currentEnd();
                    itr.next();
                }
                if (parsed > start2) {
                    privateuse = sb2.toString();
                } else {
                    throw new LocaleSyntaxException("Incomplete privateuse:" + subtags2.substring(start2), start2);
                }
            }
        }
        if (itr.isDone()) {
            return setExtensions(extensions, privateuse);
        }
        throw new LocaleSyntaxException("Ill-formed extension subtags:" + subtags2.substring(itr.currentStart()), itr.currentStart());
    }

    private InternalLocaleBuilder setExtensions(List<String> bcpExtensions, String privateuse) {
        clearExtensions();
        if (bcpExtensions != null && bcpExtensions.size() > 0) {
            HashSet<CaseInsensitiveChar> processedExtensions = new HashSet<>(bcpExtensions.size());
            for (String bcpExt : bcpExtensions) {
                CaseInsensitiveChar key = new CaseInsensitiveChar(bcpExt.charAt(0));
                if (!processedExtensions.contains(key)) {
                    if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                        setUnicodeLocaleExtension(bcpExt.substring(2));
                    } else {
                        if (this._extensions == null) {
                            this._extensions = new HashMap<>(4);
                        }
                        this._extensions.put(key, bcpExt.substring(2));
                    }
                }
            }
        }
        if (privateuse != null && privateuse.length() > 0) {
            if (this._extensions == null) {
                this._extensions = new HashMap<>(1);
            }
            this._extensions.put(new CaseInsensitiveChar(privateuse.charAt(0)), privateuse.substring(2));
        }
        return this;
    }

    public InternalLocaleBuilder setLanguageTag(LanguageTag langtag) {
        clear();
        if (langtag.getExtlangs().size() > 0) {
            this._language = langtag.getExtlangs().get(0);
        } else {
            String language = langtag.getLanguage();
            if (!language.equals(LanguageTag.UNDETERMINED)) {
                this._language = language;
            }
        }
        this._script = langtag.getScript();
        this._region = langtag.getRegion();
        List<String> bcpVariants = langtag.getVariants();
        if (bcpVariants.size() > 0) {
            StringBuilder var = new StringBuilder(bcpVariants.get(0));
            for (int i = 1; i < bcpVariants.size(); i++) {
                var.append(BaseLocale.SEP);
                var.append(bcpVariants.get(i));
            }
            this._variant = var.toString();
        }
        setExtensions(langtag.getExtensions(), langtag.getPrivateuse());
        return this;
    }

    public InternalLocaleBuilder setLocale(BaseLocale base, LocaleExtensions extensions) throws LocaleSyntaxException {
        LocaleExtensions localeExtensions = extensions;
        String language = base.getLanguage();
        String script = base.getScript();
        String region = base.getRegion();
        String variant = base.getVariant();
        if (language.length() > 0 && !LanguageTag.isLanguage(language)) {
            throw new LocaleSyntaxException("Ill-formed language: " + language);
        } else if (script.length() > 0 && !LanguageTag.isScript(script)) {
            throw new LocaleSyntaxException("Ill-formed script: " + script);
        } else if (region.length() <= 0 || LanguageTag.isRegion(region)) {
            if (variant.length() > 0) {
                int errIdx = checkVariants(variant, BaseLocale.SEP);
                if (errIdx != -1) {
                    throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
                }
            }
            this._language = language;
            this._script = script;
            this._region = region;
            this._variant = variant;
            clearExtensions();
            Set<Character> extKeys = localeExtensions == null ? null : extensions.getKeys();
            if (extKeys != null) {
                for (Character key : extKeys) {
                    Extension e = localeExtensions.getExtension(key);
                    int i = 4;
                    if (e instanceof UnicodeLocaleExtension) {
                        UnicodeLocaleExtension ue = (UnicodeLocaleExtension) e;
                        for (String uatr : ue.getUnicodeLocaleAttributes()) {
                            if (this._uattributes == null) {
                                this._uattributes = new HashSet<>(4);
                            }
                            this._uattributes.add(new CaseInsensitiveString(uatr));
                        }
                        for (String ukey : ue.getUnicodeLocaleKeys()) {
                            if (this._ukeywords == null) {
                                this._ukeywords = new HashMap<>(i);
                            }
                            this._ukeywords.put(new CaseInsensitiveString(ukey), ue.getUnicodeLocaleType(ukey));
                            i = 4;
                        }
                    } else {
                        if (this._extensions == null) {
                            this._extensions = new HashMap<>(4);
                        }
                        this._extensions.put(new CaseInsensitiveChar(key.charValue()), e.getValue());
                    }
                }
            }
            return this;
        } else {
            throw new LocaleSyntaxException("Ill-formed region: " + region);
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
        if (this._extensions != null) {
            this._extensions.clear();
        }
        if (this._uattributes != null) {
            this._uattributes.clear();
        }
        if (this._ukeywords != null) {
            this._ukeywords.clear();
        }
        return this;
    }

    public BaseLocale getBaseLocale() {
        String language = this._language;
        String script = this._script;
        String region = this._region;
        String variant = this._variant;
        if (this._extensions != null) {
            String privuse = this._extensions.get(PRIVUSE_KEY);
            if (privuse != null) {
                StringTokenIterator itr = new StringTokenIterator(privuse, LanguageTag.SEP);
                boolean sawPrefix = false;
                int privVarStart = -1;
                while (true) {
                    if (itr.isDone()) {
                        break;
                    } else if (sawPrefix) {
                        privVarStart = itr.currentStart();
                        break;
                    } else {
                        if (AsciiUtil.caseIgnoreMatch(itr.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                            sawPrefix = true;
                        }
                        itr.next();
                    }
                }
                if (privVarStart != -1) {
                    StringBuilder sb = new StringBuilder(variant);
                    if (sb.length() != 0) {
                        sb.append(BaseLocale.SEP);
                    }
                    sb.append(privuse.substring(privVarStart).replaceAll(LanguageTag.SEP, BaseLocale.SEP));
                    variant = sb.toString();
                }
            }
        }
        return BaseLocale.getInstance(language, script, region, variant);
    }

    public LocaleExtensions getLocaleExtensions() {
        if ((this._extensions == null || this._extensions.size() == 0) && ((this._uattributes == null || this._uattributes.size() == 0) && (this._ukeywords == null || this._ukeywords.size() == 0))) {
            return LocaleExtensions.EMPTY_EXTENSIONS;
        }
        return new LocaleExtensions(this._extensions, this._uattributes, this._ukeywords);
    }

    static String removePrivateuseVariant(String privuseVal) {
        StringTokenIterator itr = new StringTokenIterator(privuseVal, LanguageTag.SEP);
        int prefixStart = -1;
        boolean sawPrivuseVar = false;
        while (true) {
            if (itr.isDone()) {
                break;
            } else if (prefixStart != -1) {
                sawPrivuseVar = true;
                break;
            } else {
                if (AsciiUtil.caseIgnoreMatch(itr.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                    prefixStart = itr.currentStart();
                }
                itr.next();
            }
        }
        if (!sawPrivuseVar) {
            return privuseVal;
        }
        return prefixStart == 0 ? null : privuseVal.substring(0, prefixStart - 1);
    }

    private int checkVariants(String variants, String sep) {
        StringTokenIterator itr = new StringTokenIterator(variants, sep);
        while (!itr.isDone()) {
            if (!LanguageTag.isVariant(itr.current())) {
                return itr.currentStart();
            }
            itr.next();
        }
        return -1;
    }

    private void setUnicodeLocaleExtension(String subtags) {
        if (this._uattributes != null) {
            this._uattributes.clear();
        }
        if (this._ukeywords != null) {
            this._ukeywords.clear();
        }
        StringTokenIterator itr = new StringTokenIterator(subtags, LanguageTag.SEP);
        while (!itr.isDone() && UnicodeLocaleExtension.isAttribute(itr.current())) {
            if (this._uattributes == null) {
                this._uattributes = new HashSet<>(4);
            }
            this._uattributes.add(new CaseInsensitiveString(itr.current()));
            itr.next();
        }
        int typeStart = -1;
        CaseInsensitiveString key = null;
        int typeEnd = -1;
        while (!itr.isDone()) {
            if (key != null) {
                if (UnicodeLocaleExtension.isKey(itr.current())) {
                    String type = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                    if (this._ukeywords == null) {
                        this._ukeywords = new HashMap<>(4);
                    }
                    this._ukeywords.put(key, type);
                    CaseInsensitiveString tmpKey = new CaseInsensitiveString(itr.current());
                    key = this._ukeywords.containsKey(tmpKey) ? null : tmpKey;
                    typeEnd = -1;
                    typeStart = -1;
                } else {
                    if (typeStart == -1) {
                        typeStart = itr.currentStart();
                    }
                    typeEnd = itr.currentEnd();
                }
            } else if (UnicodeLocaleExtension.isKey(itr.current())) {
                key = new CaseInsensitiveString(itr.current());
                if (this._ukeywords != null && this._ukeywords.containsKey(key)) {
                    key = null;
                }
            }
            if (itr.hasNext()) {
                itr.next();
            } else if (key != null) {
                String type2 = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                if (this._ukeywords == null) {
                    this._ukeywords = new HashMap<>(4);
                }
                this._ukeywords.put(key, type2);
                return;
            } else {
                return;
            }
        }
    }
}

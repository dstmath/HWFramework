package sun.util.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InternalLocaleBuilder {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final CaseInsensitiveChar PRIVATEUSE_KEY = new CaseInsensitiveChar(LanguageTag.PRIVATEUSE);
    private Map<CaseInsensitiveChar, String> extensions;
    private String language = "";
    private String region = "";
    private String script = "";
    private Set<CaseInsensitiveString> uattributes;
    private Map<CaseInsensitiveString, String> ukeywords;
    private String variant = "";

    static final class CaseInsensitiveChar {
        private final char ch;
        private final char lowerCh;

        private CaseInsensitiveChar(String s) {
            this(s.charAt(0));
        }

        CaseInsensitiveChar(char c) {
            this.ch = c;
            this.lowerCh = LocaleUtils.toLower(this.ch);
        }

        public char value() {
            return this.ch;
        }

        public int hashCode() {
            return this.lowerCh;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveChar)) {
                return false;
            }
            if (this.lowerCh != ((CaseInsensitiveChar) obj).lowerCh) {
                z = false;
            }
            return z;
        }
    }

    static final class CaseInsensitiveString {
        private final String lowerStr;
        private final String str;

        CaseInsensitiveString(String s) {
            this.str = s;
            this.lowerStr = LocaleUtils.toLowerString(s);
        }

        public String value() {
            return this.str;
        }

        public int hashCode() {
            return this.lowerStr.hashCode();
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CaseInsensitiveString)) {
                return false;
            }
            return this.lowerStr.equals(((CaseInsensitiveString) obj).lowerStr);
        }
    }

    public InternalLocaleBuilder setLanguage(String language2) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(language2)) {
            this.language = "";
        } else if (LanguageTag.isLanguage(language2)) {
            this.language = language2;
        } else {
            throw new LocaleSyntaxException("Ill-formed language: " + language2, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setScript(String script2) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(script2)) {
            this.script = "";
        } else if (LanguageTag.isScript(script2)) {
            this.script = script2;
        } else {
            throw new LocaleSyntaxException("Ill-formed script: " + script2, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setRegion(String region2) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(region2)) {
            this.region = "";
        } else if (LanguageTag.isRegion(region2)) {
            this.region = region2;
        } else {
            throw new LocaleSyntaxException("Ill-formed region: " + region2, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setVariant(String variant2) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(variant2)) {
            this.variant = "";
        } else {
            String var = variant2.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
            int errIdx = checkVariants(var, BaseLocale.SEP);
            if (errIdx == -1) {
                this.variant = var;
            } else {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant2, errIdx);
            }
        }
        return this;
    }

    public InternalLocaleBuilder addUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (UnicodeLocaleExtension.isAttribute(attribute)) {
            if (this.uattributes == null) {
                this.uattributes = new HashSet(4);
            }
            this.uattributes.add(new CaseInsensitiveString(attribute));
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
    }

    public InternalLocaleBuilder removeUnicodeLocaleAttribute(String attribute) throws LocaleSyntaxException {
        if (attribute == null || !UnicodeLocaleExtension.isAttribute(attribute)) {
            throw new LocaleSyntaxException("Ill-formed Unicode locale attribute: " + attribute);
        }
        if (this.uattributes != null) {
            this.uattributes.remove(new CaseInsensitiveString(attribute));
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
                if (this.ukeywords == null) {
                    this.ukeywords = new HashMap(4);
                }
                this.ukeywords.put(cikey, type);
            } else if (this.ukeywords != null) {
                this.ukeywords.remove(cikey);
            }
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed Unicode locale keyword key: " + key);
    }

    public InternalLocaleBuilder setExtension(char singleton, String value) throws LocaleSyntaxException {
        boolean validSubtag;
        boolean isBcpPrivateuse = LanguageTag.isPrivateusePrefixChar(singleton);
        if (isBcpPrivateuse || LanguageTag.isExtensionSingletonChar(singleton)) {
            boolean remove = LocaleUtils.isEmpty(value);
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
                    if (this.extensions == null) {
                        this.extensions = new HashMap(4);
                    }
                    this.extensions.put(key, val);
                }
            } else if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                if (this.uattributes != null) {
                    this.uattributes.clear();
                }
                if (this.ukeywords != null) {
                    this.ukeywords.clear();
                }
            } else if (this.extensions != null && this.extensions.containsKey(key)) {
                this.extensions.remove(key);
            }
            return this;
        }
        throw new LocaleSyntaxException("Ill-formed extension key: " + singleton);
    }

    public InternalLocaleBuilder setExtensions(String subtags) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(subtags)) {
            clearExtensions();
            return this;
        }
        String subtags2 = subtags.replaceAll(BaseLocale.SEP, LanguageTag.SEP);
        StringTokenIterator itr = new StringTokenIterator(subtags2, LanguageTag.SEP);
        List<String> extensions2 = null;
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
                if (extensions2 == null) {
                    extensions2 = new ArrayList<>(4);
                }
                extensions2.add(sb.toString());
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
            return setExtensions(extensions2, privateuse);
        }
        throw new LocaleSyntaxException("Ill-formed extension subtags:" + subtags2.substring(itr.currentStart()), itr.currentStart());
    }

    private InternalLocaleBuilder setExtensions(List<String> bcpExtensions, String privateuse) {
        clearExtensions();
        if (!LocaleUtils.isEmpty((List<?>) bcpExtensions)) {
            Set<CaseInsensitiveChar> done = new HashSet<>(bcpExtensions.size());
            for (String bcpExt : bcpExtensions) {
                CaseInsensitiveChar key = new CaseInsensitiveChar(bcpExt);
                if (!done.contains(key)) {
                    if (UnicodeLocaleExtension.isSingletonChar(key.value())) {
                        setUnicodeLocaleExtension(bcpExt.substring(2));
                    } else {
                        if (this.extensions == null) {
                            this.extensions = new HashMap(4);
                        }
                        this.extensions.put(key, bcpExt.substring(2));
                    }
                }
                done.add(key);
            }
        }
        if (privateuse != null && privateuse.length() > 0) {
            if (this.extensions == null) {
                this.extensions = new HashMap(1);
            }
            this.extensions.put(new CaseInsensitiveChar(privateuse), privateuse.substring(2));
        }
        return this;
    }

    public InternalLocaleBuilder setLanguageTag(LanguageTag langtag) {
        clear();
        if (!langtag.getExtlangs().isEmpty()) {
            this.language = langtag.getExtlangs().get(0);
        } else {
            String lang = langtag.getLanguage();
            if (!lang.equals(LanguageTag.UNDETERMINED)) {
                this.language = lang;
            }
        }
        this.script = langtag.getScript();
        this.region = langtag.getRegion();
        List<String> bcpVariants = langtag.getVariants();
        if (!bcpVariants.isEmpty()) {
            StringBuilder var = new StringBuilder(bcpVariants.get(0));
            int size = bcpVariants.size();
            for (int i = 1; i < size; i++) {
                var.append(BaseLocale.SEP);
                var.append(bcpVariants.get(i));
            }
            this.variant = var.toString();
        }
        setExtensions(langtag.getExtensions(), langtag.getPrivateuse());
        return this;
    }

    public InternalLocaleBuilder setLocale(BaseLocale base, LocaleExtensions localeExtensions) throws LocaleSyntaxException {
        LocaleExtensions localeExtensions2 = localeExtensions;
        String language2 = base.getLanguage();
        String script2 = base.getScript();
        String region2 = base.getRegion();
        String variant2 = base.getVariant();
        if (language2.equals("ja") && region2.equals("JP") && variant2.equals("JP")) {
            variant2 = "";
        } else if (language2.equals("th") && region2.equals("TH") && variant2.equals("TH")) {
            variant2 = "";
        } else if (language2.equals("no") && region2.equals("NO") && variant2.equals("NY")) {
            language2 = "nn";
            variant2 = "";
        }
        if (language2.length() > 0 && !LanguageTag.isLanguage(language2)) {
            throw new LocaleSyntaxException("Ill-formed language: " + language2);
        } else if (script2.length() > 0 && !LanguageTag.isScript(script2)) {
            throw new LocaleSyntaxException("Ill-formed script: " + script2);
        } else if (region2.length() <= 0 || LanguageTag.isRegion(region2)) {
            if (variant2.length() > 0) {
                variant2 = variant2.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
                int errIdx = checkVariants(variant2, BaseLocale.SEP);
                if (errIdx != -1) {
                    throw new LocaleSyntaxException("Ill-formed variant: " + variant2, errIdx);
                }
            }
            this.language = language2;
            this.script = script2;
            this.region = region2;
            this.variant = variant2;
            clearExtensions();
            Set<Character> extKeys = localeExtensions2 == null ? null : localeExtensions.getKeys();
            if (extKeys != null) {
                for (Character key : extKeys) {
                    Extension e = localeExtensions2.getExtension(key);
                    int i = 4;
                    if (e instanceof UnicodeLocaleExtension) {
                        UnicodeLocaleExtension ue = (UnicodeLocaleExtension) e;
                        for (String uatr : ue.getUnicodeLocaleAttributes()) {
                            if (this.uattributes == null) {
                                this.uattributes = new HashSet(4);
                            }
                            this.uattributes.add(new CaseInsensitiveString(uatr));
                        }
                        for (String ukey : ue.getUnicodeLocaleKeys()) {
                            if (this.ukeywords == null) {
                                this.ukeywords = new HashMap(i);
                            }
                            this.ukeywords.put(new CaseInsensitiveString(ukey), ue.getUnicodeLocaleType(ukey));
                            i = 4;
                        }
                    } else {
                        if (this.extensions == null) {
                            this.extensions = new HashMap(4);
                        }
                        this.extensions.put(new CaseInsensitiveChar(key.charValue()), e.getValue());
                    }
                }
            }
            return this;
        } else {
            throw new LocaleSyntaxException("Ill-formed region: " + region2);
        }
    }

    public InternalLocaleBuilder clear() {
        this.language = "";
        this.script = "";
        this.region = "";
        this.variant = "";
        clearExtensions();
        return this;
    }

    public InternalLocaleBuilder clearExtensions() {
        if (this.extensions != null) {
            this.extensions.clear();
        }
        if (this.uattributes != null) {
            this.uattributes.clear();
        }
        if (this.ukeywords != null) {
            this.ukeywords.clear();
        }
        return this;
    }

    public BaseLocale getBaseLocale() {
        String language2 = this.language;
        String script2 = this.script;
        String region2 = this.region;
        String variant2 = this.variant;
        if (this.extensions != null) {
            String privuse = this.extensions.get(PRIVATEUSE_KEY);
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
                        if (LocaleUtils.caseIgnoreMatch(itr.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                            sawPrefix = true;
                        }
                        itr.next();
                    }
                }
                if (privVarStart != -1) {
                    StringBuilder sb = new StringBuilder(variant2);
                    if (sb.length() != 0) {
                        sb.append(BaseLocale.SEP);
                    }
                    sb.append(privuse.substring(privVarStart).replaceAll(LanguageTag.SEP, BaseLocale.SEP));
                    variant2 = sb.toString();
                }
            }
        }
        return BaseLocale.getInstance(language2, script2, region2, variant2);
    }

    public LocaleExtensions getLocaleExtensions() {
        LocaleExtensions localeExtensions = null;
        if (LocaleUtils.isEmpty((Map<?, ?>) this.extensions) && LocaleUtils.isEmpty((Set<?>) this.uattributes) && LocaleUtils.isEmpty((Map<?, ?>) this.ukeywords)) {
            return null;
        }
        LocaleExtensions lext = new LocaleExtensions(this.extensions, this.uattributes, this.ukeywords);
        if (!lext.isEmpty()) {
            localeExtensions = lext;
        }
        return localeExtensions;
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
                if (LocaleUtils.caseIgnoreMatch(itr.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
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
        if (this.uattributes != null) {
            this.uattributes.clear();
        }
        if (this.ukeywords != null) {
            this.ukeywords.clear();
        }
        StringTokenIterator itr = new StringTokenIterator(subtags, LanguageTag.SEP);
        while (!itr.isDone() && UnicodeLocaleExtension.isAttribute(itr.current())) {
            if (this.uattributes == null) {
                this.uattributes = new HashSet(4);
            }
            this.uattributes.add(new CaseInsensitiveString(itr.current()));
            itr.next();
        }
        int typeStart = -1;
        CaseInsensitiveString key = null;
        int typeEnd = -1;
        while (!itr.isDone()) {
            if (key != null) {
                if (UnicodeLocaleExtension.isKey(itr.current())) {
                    String type = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                    if (this.ukeywords == null) {
                        this.ukeywords = new HashMap(4);
                    }
                    this.ukeywords.put(key, type);
                    CaseInsensitiveString tmpKey = new CaseInsensitiveString(itr.current());
                    key = this.ukeywords.containsKey(tmpKey) ? null : tmpKey;
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
                if (this.ukeywords != null && this.ukeywords.containsKey(key)) {
                    key = null;
                }
            }
            if (itr.hasNext()) {
                itr.next();
            } else if (key != null) {
                String type2 = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                if (this.ukeywords == null) {
                    this.ukeywords = new HashMap(4);
                }
                this.ukeywords.put(key, type2);
                return;
            } else {
                return;
            }
        }
    }
}

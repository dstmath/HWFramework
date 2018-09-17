package sun.util.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InternalLocaleBuilder {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final CaseInsensitiveChar PRIVATEUSE_KEY = null;
    private Map<CaseInsensitiveChar, String> extensions;
    private String language;
    private String region;
    private String script;
    private Set<CaseInsensitiveString> uattributes;
    private Map<CaseInsensitiveString, String> ukeywords;
    private String variant;

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
            if (obj instanceof CaseInsensitiveString) {
                return this.lowerStr.equals(((CaseInsensitiveString) obj).lowerStr);
            }
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.locale.InternalLocaleBuilder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.locale.InternalLocaleBuilder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.InternalLocaleBuilder.<clinit>():void");
    }

    public InternalLocaleBuilder() {
        this.language = "";
        this.script = "";
        this.region = "";
        this.variant = "";
    }

    public InternalLocaleBuilder setLanguage(String language) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(language)) {
            this.language = "";
        } else if (LanguageTag.isLanguage(language)) {
            this.language = language;
        } else {
            throw new LocaleSyntaxException("Ill-formed language: " + language, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setScript(String script) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(script)) {
            this.script = "";
        } else if (LanguageTag.isScript(script)) {
            this.script = script;
        } else {
            throw new LocaleSyntaxException("Ill-formed script: " + script, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setRegion(String region) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(region)) {
            this.region = "";
        } else if (LanguageTag.isRegion(region)) {
            this.region = region;
        } else {
            throw new LocaleSyntaxException("Ill-formed region: " + region, 0);
        }
        return this;
    }

    public InternalLocaleBuilder setVariant(String variant) throws LocaleSyntaxException {
        if (LocaleUtils.isEmpty(variant)) {
            this.variant = "";
        } else {
            String var = variant.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
            int errIdx = checkVariants(var, BaseLocale.SEP);
            if (errIdx != -1) {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
            }
            this.variant = var;
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
        boolean isBcpPrivateuse = LanguageTag.isPrivateusePrefixChar(singleton);
        if (isBcpPrivateuse || LanguageTag.isExtensionSingletonChar(singleton)) {
            boolean remove = LocaleUtils.isEmpty(value);
            CaseInsensitiveChar key = new CaseInsensitiveChar(singleton);
            if (!remove) {
                String val = value.replaceAll(BaseLocale.SEP, LanguageTag.SEP);
                StringTokenIterator itr = new StringTokenIterator(val, LanguageTag.SEP);
                while (!itr.isDone()) {
                    boolean validSubtag;
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
        subtags = subtags.replaceAll(BaseLocale.SEP, LanguageTag.SEP);
        StringTokenIterator itr = new StringTokenIterator(subtags, LanguageTag.SEP);
        List extensions = null;
        String privateuse = null;
        int parsed = 0;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!LanguageTag.isExtensionSingleton(s)) {
                break;
            }
            int start = itr.currentStart();
            String singleton = s;
            StringBuilder sb = new StringBuilder(s);
            itr.next();
            while (!itr.isDone()) {
                s = itr.current();
                if (!LanguageTag.isExtensionSubtag(s)) {
                    break;
                }
                sb.append(LanguageTag.SEP).append(s);
                parsed = itr.currentEnd();
                itr.next();
            }
            if (parsed < start) {
                throw new LocaleSyntaxException("Incomplete extension '" + singleton + "'", start);
            }
            if (extensions == null) {
                extensions = new ArrayList(4);
            }
            extensions.add(sb.toString());
        }
        if (!itr.isDone()) {
            s = itr.current();
            if (LanguageTag.isPrivateusePrefix(s)) {
                start = itr.currentStart();
                sb = new StringBuilder(s);
                itr.next();
                while (!itr.isDone()) {
                    s = itr.current();
                    if (!LanguageTag.isPrivateuseSubtag(s)) {
                        break;
                    }
                    sb.append(LanguageTag.SEP).append(s);
                    parsed = itr.currentEnd();
                    itr.next();
                }
                if (parsed <= start) {
                    throw new LocaleSyntaxException("Incomplete privateuse:" + subtags.substring(start), start);
                }
                privateuse = sb.toString();
            }
        }
        if (itr.isDone()) {
            return setExtensions(extensions, privateuse);
        }
        throw new LocaleSyntaxException("Ill-formed extension subtags:" + subtags.substring(itr.currentStart()), itr.currentStart());
    }

    private InternalLocaleBuilder setExtensions(List<String> bcpExtensions, String privateuse) {
        clearExtensions();
        if (!LocaleUtils.isEmpty((List) bcpExtensions)) {
            Set<CaseInsensitiveChar> done = new HashSet(bcpExtensions.size());
            for (String bcpExt : bcpExtensions) {
                CaseInsensitiveChar key = new CaseInsensitiveChar(null);
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
            this.extensions.put(new CaseInsensitiveChar(null), privateuse.substring(2));
        }
        return this;
    }

    public InternalLocaleBuilder setLanguageTag(LanguageTag langtag) {
        clear();
        if (langtag.getExtlangs().isEmpty()) {
            String lang = langtag.getLanguage();
            if (!lang.equals(LanguageTag.UNDETERMINED)) {
                this.language = lang;
            }
        } else {
            this.language = (String) langtag.getExtlangs().get(0);
        }
        this.script = langtag.getScript();
        this.region = langtag.getRegion();
        List<String> bcpVariants = langtag.getVariants();
        if (!bcpVariants.isEmpty()) {
            StringBuilder var = new StringBuilder((String) bcpVariants.get(0));
            int size = bcpVariants.size();
            for (int i = 1; i < size; i++) {
                var.append(BaseLocale.SEP).append((String) bcpVariants.get(i));
            }
            this.variant = var.toString();
        }
        setExtensions(langtag.getExtensions(), langtag.getPrivateuse());
        return this;
    }

    public InternalLocaleBuilder setLocale(BaseLocale base, LocaleExtensions localeExtensions) throws LocaleSyntaxException {
        int errIdx;
        Set<Character> extKeys;
        Extension e;
        UnicodeLocaleExtension ue;
        String language = base.getLanguage();
        String script = base.getScript();
        String region = base.getRegion();
        String variant = base.getVariant();
        if (language.equals("ja")) {
            if (region.equals("JP") && variant.equals("JP")) {
                if (!-assertionsDisabled) {
                    if (!"japanese".equals(localeExtensions.getUnicodeLocaleType("ca"))) {
                        throw new AssertionError();
                    }
                }
                variant = "";
                if (language.length() <= 0 && !LanguageTag.isLanguage(language)) {
                    throw new LocaleSyntaxException("Ill-formed language: " + language);
                } else if (script.length() <= 0 && !LanguageTag.isScript(script)) {
                    throw new LocaleSyntaxException("Ill-formed script: " + script);
                } else if (region.length() > 0 || LanguageTag.isRegion(region)) {
                    if (variant.length() > 0) {
                        variant = variant.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
                        errIdx = checkVariants(variant, BaseLocale.SEP);
                        if (errIdx != -1) {
                            throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
                        }
                    }
                    this.language = language;
                    this.script = script;
                    this.region = region;
                    this.variant = variant;
                    clearExtensions();
                    extKeys = localeExtensions == null ? null : localeExtensions.getKeys();
                    if (extKeys != null) {
                        for (Character key : extKeys) {
                            e = localeExtensions.getExtension(key);
                            if (e instanceof UnicodeLocaleExtension) {
                                ue = (UnicodeLocaleExtension) e;
                                for (String uatr : ue.getUnicodeLocaleAttributes()) {
                                    if (this.uattributes != null) {
                                        this.uattributes = new HashSet(4);
                                    }
                                    this.uattributes.add(new CaseInsensitiveString(uatr));
                                }
                                for (String ukey : ue.getUnicodeLocaleKeys()) {
                                    if (this.ukeywords != null) {
                                        this.ukeywords = new HashMap(4);
                                    }
                                    this.ukeywords.put(new CaseInsensitiveString(ukey), ue.getUnicodeLocaleType(ukey));
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
                    throw new LocaleSyntaxException("Ill-formed region: " + region);
                }
            }
        }
        if (language.equals("th")) {
            if (region.equals("TH") && variant.equals("TH")) {
                if (!-assertionsDisabled) {
                    if (!"thai".equals(localeExtensions.getUnicodeLocaleType("nu"))) {
                        throw new AssertionError();
                    }
                }
                variant = "";
                if (language.length() <= 0) {
                }
                if (script.length() <= 0) {
                }
                if (region.length() > 0) {
                }
                if (variant.length() > 0) {
                    variant = variant.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
                    errIdx = checkVariants(variant, BaseLocale.SEP);
                    if (errIdx != -1) {
                        throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
                    }
                }
                this.language = language;
                this.script = script;
                this.region = region;
                this.variant = variant;
                clearExtensions();
                if (localeExtensions == null) {
                }
                if (extKeys != null) {
                    for (Character key2 : extKeys) {
                        e = localeExtensions.getExtension(key2);
                        if (e instanceof UnicodeLocaleExtension) {
                            if (this.extensions == null) {
                                this.extensions = new HashMap(4);
                            }
                            this.extensions.put(new CaseInsensitiveChar(key2.charValue()), e.getValue());
                        } else {
                            ue = (UnicodeLocaleExtension) e;
                            for (String uatr2 : ue.getUnicodeLocaleAttributes()) {
                                if (this.uattributes != null) {
                                    this.uattributes = new HashSet(4);
                                }
                                this.uattributes.add(new CaseInsensitiveString(uatr2));
                            }
                            for (String ukey2 : ue.getUnicodeLocaleKeys()) {
                                if (this.ukeywords != null) {
                                    this.ukeywords = new HashMap(4);
                                }
                                this.ukeywords.put(new CaseInsensitiveString(ukey2), ue.getUnicodeLocaleType(ukey2));
                            }
                        }
                    }
                }
                return this;
            }
        }
        if (language.equals("no")) {
            if (region.equals("NO") && variant.equals("NY")) {
                language = "nn";
                variant = "";
            }
        }
        if (language.length() <= 0) {
        }
        if (script.length() <= 0) {
        }
        if (region.length() > 0) {
        }
        if (variant.length() > 0) {
            variant = variant.replaceAll(LanguageTag.SEP, BaseLocale.SEP);
            errIdx = checkVariants(variant, BaseLocale.SEP);
            if (errIdx != -1) {
                throw new LocaleSyntaxException("Ill-formed variant: " + variant, errIdx);
            }
        }
        this.language = language;
        this.script = script;
        this.region = region;
        this.variant = variant;
        clearExtensions();
        if (localeExtensions == null) {
        }
        if (extKeys != null) {
            for (Character key22 : extKeys) {
                e = localeExtensions.getExtension(key22);
                if (e instanceof UnicodeLocaleExtension) {
                    ue = (UnicodeLocaleExtension) e;
                    for (String uatr22 : ue.getUnicodeLocaleAttributes()) {
                        if (this.uattributes != null) {
                            this.uattributes = new HashSet(4);
                        }
                        this.uattributes.add(new CaseInsensitiveString(uatr22));
                    }
                    for (String ukey22 : ue.getUnicodeLocaleKeys()) {
                        if (this.ukeywords != null) {
                            this.ukeywords = new HashMap(4);
                        }
                        this.ukeywords.put(new CaseInsensitiveString(ukey22), ue.getUnicodeLocaleType(ukey22));
                    }
                } else {
                    if (this.extensions == null) {
                        this.extensions = new HashMap(4);
                    }
                    this.extensions.put(new CaseInsensitiveChar(key22.charValue()), e.getValue());
                }
            }
        }
        return this;
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
        String language = this.language;
        String script = this.script;
        String region = this.region;
        String variant = this.variant;
        if (this.extensions != null) {
            String privuse = (String) this.extensions.get(PRIVATEUSE_KEY);
            if (privuse != null) {
                StringTokenIterator itr = new StringTokenIterator(privuse, LanguageTag.SEP);
                boolean sawPrefix = false;
                int privVarStart = -1;
                while (!itr.isDone()) {
                    if (sawPrefix) {
                        privVarStart = itr.currentStart();
                        break;
                    }
                    if (LocaleUtils.caseIgnoreMatch(itr.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                        sawPrefix = true;
                    }
                    itr.next();
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
        if (LocaleUtils.isEmpty(this.extensions) && LocaleUtils.isEmpty(this.uattributes) && LocaleUtils.isEmpty(this.ukeywords)) {
            return null;
        }
        LocaleExtensions lext = new LocaleExtensions(this.extensions, this.uattributes, this.ukeywords);
        if (lext.isEmpty()) {
            lext = null;
        }
        return lext;
    }

    static String removePrivateuseVariant(String privuseVal) {
        int i = 1;
        StringTokenIterator itr = new StringTokenIterator(privuseVal, LanguageTag.SEP);
        int prefixStart = -1;
        boolean sawPrivuseVar = false;
        while (!itr.isDone()) {
            if (prefixStart != -1) {
                sawPrivuseVar = true;
                break;
            }
            if (LocaleUtils.caseIgnoreMatch(itr.current(), LanguageTag.PRIVUSE_VARIANT_PREFIX)) {
                prefixStart = itr.currentStart();
            }
            itr.next();
        }
        if (!sawPrivuseVar) {
            return privuseVal;
        }
        if (!-assertionsDisabled) {
            if (prefixStart != 0 && prefixStart <= 1) {
                i = 0;
            }
            if (i == 0) {
                throw new AssertionError();
            }
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
        Object obj = 1;
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
        Object key = null;
        int typeStart = -1;
        int typeEnd = -1;
        while (!itr.isDone()) {
            if (key != null) {
                if (UnicodeLocaleExtension.isKey(itr.current())) {
                    if (!-assertionsDisabled) {
                        Object obj2;
                        if (typeStart == -1 || typeEnd != -1) {
                            obj2 = 1;
                        } else {
                            obj2 = null;
                        }
                        if (obj2 == null) {
                            throw new AssertionError();
                        }
                    }
                    String type = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                    if (this.ukeywords == null) {
                        this.ukeywords = new HashMap(4);
                    }
                    this.ukeywords.put(key, type);
                    CaseInsensitiveString tmpKey = new CaseInsensitiveString(itr.current());
                    if (this.ukeywords.containsKey(tmpKey)) {
                        key = null;
                    } else {
                        CaseInsensitiveString key2 = tmpKey;
                    }
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
                if (!-assertionsDisabled) {
                    if (typeStart != -1 && typeEnd == -1) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                type = typeStart == -1 ? "" : subtags.substring(typeStart, typeEnd);
                if (this.ukeywords == null) {
                    this.ukeywords = new HashMap(4);
                }
                this.ukeywords.put(key, type);
                return;
            } else {
                return;
            }
        }
    }
}

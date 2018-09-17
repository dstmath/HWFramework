package sun.util.locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import sun.security.x509.PolicyInformation;

public class LanguageTag {
    private static final Map<String, String[]> GRANDFATHERED = null;
    public static final String PRIVATEUSE = "x";
    public static final String PRIVUSE_VARIANT_PREFIX = "lvariant";
    public static final String SEP = "-";
    public static final String UNDETERMINED = "und";
    private List<String> extensions;
    private List<String> extlangs;
    private String language;
    private String privateuse;
    private String region;
    private String script;
    private List<String> variants;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.locale.LanguageTag.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.locale.LanguageTag.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.LanguageTag.<clinit>():void");
    }

    private LanguageTag() {
        this.language = "";
        this.script = "";
        this.region = "";
        this.privateuse = "";
        this.extlangs = Collections.emptyList();
        this.variants = Collections.emptyList();
        this.extensions = Collections.emptyList();
    }

    public static LanguageTag parse(String languageTag, ParseStatus sts) {
        StringTokenIterator itr;
        if (sts == null) {
            sts = new ParseStatus();
        } else {
            sts.reset();
        }
        String[] gfmap = (String[]) GRANDFATHERED.get(LocaleUtils.toLowerString(languageTag));
        if (gfmap != null) {
            itr = new StringTokenIterator(gfmap[1], SEP);
        } else {
            itr = new StringTokenIterator(languageTag, SEP);
        }
        LanguageTag tag = new LanguageTag();
        if (tag.parseLanguage(itr, sts)) {
            tag.parseExtlangs(itr, sts);
            tag.parseScript(itr, sts);
            tag.parseRegion(itr, sts);
            tag.parseVariants(itr, sts);
            tag.parseExtensions(itr, sts);
        }
        tag.parsePrivateuse(itr, sts);
        if (!(itr.isDone() || sts.isError())) {
            String s = itr.current();
            sts.errorIndex = itr.currentStart();
            if (s.length() == 0) {
                sts.errorMsg = "Empty subtag";
            } else {
                sts.errorMsg = "Invalid subtag: " + s;
            }
        }
        return tag;
    }

    private boolean parseLanguage(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isLanguage(s)) {
            found = true;
            this.language = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseExtlangs(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isExtlang(s)) {
                break;
            }
            found = true;
            if (this.extlangs.isEmpty()) {
                this.extlangs = new ArrayList(3);
            }
            this.extlangs.add(s);
            sts.parseLength = itr.currentEnd();
            itr.next();
            if (this.extlangs.size() == 3) {
                break;
            }
        }
        return found;
    }

    private boolean parseScript(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isScript(s)) {
            found = true;
            this.script = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseRegion(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isRegion(s)) {
            found = true;
            this.region = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseVariants(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isVariant(s)) {
                break;
            }
            found = true;
            if (this.variants.isEmpty()) {
                this.variants = new ArrayList(3);
            }
            this.variants.add(s);
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseExtensions(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isExtensionSingleton(s)) {
                break;
            }
            int start = itr.currentStart();
            String singleton = s;
            StringBuilder sb = new StringBuilder(s);
            itr.next();
            while (!itr.isDone()) {
                s = itr.current();
                if (!isExtensionSubtag(s)) {
                    break;
                }
                sb.append(SEP).append(s);
                sts.parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts.parseLength <= start) {
                sts.errorIndex = start;
                sts.errorMsg = "Incomplete extension '" + singleton + "'";
                break;
            }
            if (this.extensions.isEmpty()) {
                this.extensions = new ArrayList(4);
            }
            this.extensions.add(sb.toString());
            found = true;
        }
        return found;
    }

    private boolean parsePrivateuse(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isPrivateusePrefix(s)) {
            int start = itr.currentStart();
            StringBuilder sb = new StringBuilder(s);
            itr.next();
            while (!itr.isDone()) {
                s = itr.current();
                if (!isPrivateuseSubtag(s)) {
                    break;
                }
                sb.append(SEP).append(s);
                sts.parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts.parseLength <= start) {
                sts.errorIndex = start;
                sts.errorMsg = "Incomplete privateuse";
            } else {
                this.privateuse = sb.toString();
                found = true;
            }
        }
        return found;
    }

    public static LanguageTag parseLocale(BaseLocale baseLocale, LocaleExtensions localeExtensions) {
        LanguageTag tag = new LanguageTag();
        String language = baseLocale.getLanguage();
        String script = baseLocale.getScript();
        String region = baseLocale.getRegion();
        String variant = baseLocale.getVariant();
        boolean hasSubtag = false;
        String privuseVar = null;
        if (isLanguage(language)) {
            if (language.equals("iw")) {
                language = "he";
            } else {
                if (language.equals("ji")) {
                    language = "yi";
                } else {
                    if (language.equals("in")) {
                        language = PolicyInformation.ID;
                    }
                }
            }
            tag.language = language;
        }
        if (isScript(script)) {
            tag.script = canonicalizeScript(script);
            hasSubtag = true;
        }
        if (isRegion(region)) {
            tag.region = canonicalizeRegion(region);
            hasSubtag = true;
        }
        if (tag.language.equals("no")) {
            if (tag.region.equals("NO")) {
                if (variant.equals("NY")) {
                    tag.language = "nn";
                    variant = "";
                }
            }
        }
        if (variant.length() > 0) {
            List<String> variants = null;
            StringTokenIterator stringTokenIterator = new StringTokenIterator(variant, BaseLocale.SEP);
            while (!stringTokenIterator.isDone()) {
                String var = stringTokenIterator.current();
                if (!isVariant(var)) {
                    break;
                }
                if (variants == null) {
                    variants = new ArrayList();
                }
                variants.add(var);
                stringTokenIterator.next();
            }
            if (variants != null) {
                tag.variants = variants;
                hasSubtag = true;
            }
            if (!stringTokenIterator.isDone()) {
                StringBuilder buf = new StringBuilder();
                while (!stringTokenIterator.isDone()) {
                    String prvv = stringTokenIterator.current();
                    if (!isPrivateuseSubtag(prvv)) {
                        break;
                    }
                    if (buf.length() > 0) {
                        buf.append(SEP);
                    }
                    buf.append(prvv);
                    stringTokenIterator.next();
                }
                if (buf.length() > 0) {
                    privuseVar = buf.toString();
                }
            }
        }
        List list = null;
        String str = null;
        if (localeExtensions != null) {
            for (Character locextKey : localeExtensions.getKeys()) {
                Extension ext = localeExtensions.getExtension(locextKey);
                if (isPrivateusePrefixChar(locextKey.charValue())) {
                    str = ext.getValue();
                } else {
                    if (list == null) {
                        list = new ArrayList();
                    }
                    list.add(locextKey.toString() + SEP + ext.getValue());
                }
            }
        }
        if (list != null) {
            tag.extensions = list;
            hasSubtag = true;
        }
        if (privuseVar != null) {
            if (str == null) {
                str = "lvariant-" + privuseVar;
            } else {
                str = str + SEP + PRIVUSE_VARIANT_PREFIX + SEP + privuseVar.replace((CharSequence) BaseLocale.SEP, SEP);
            }
        }
        if (str != null) {
            tag.privateuse = str;
        }
        if (tag.language.length() == 0 && (hasSubtag || str == null)) {
            tag.language = UNDETERMINED;
        }
        return tag;
    }

    public String getLanguage() {
        return this.language;
    }

    public List<String> getExtlangs() {
        if (this.extlangs.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.extlangs);
    }

    public String getScript() {
        return this.script;
    }

    public String getRegion() {
        return this.region;
    }

    public List<String> getVariants() {
        if (this.variants.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.variants);
    }

    public List<String> getExtensions() {
        if (this.extensions.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.extensions);
    }

    public String getPrivateuse() {
        return this.privateuse;
    }

    public static boolean isLanguage(String s) {
        int len = s.length();
        return (len < 2 || len > 8) ? false : LocaleUtils.isAlphaString(s);
    }

    public static boolean isExtlang(String s) {
        return s.length() == 3 ? LocaleUtils.isAlphaString(s) : false;
    }

    public static boolean isScript(String s) {
        return s.length() == 4 ? LocaleUtils.isAlphaString(s) : false;
    }

    public static boolean isRegion(String s) {
        if (s.length() == 2 && LocaleUtils.isAlphaString(s)) {
            return true;
        }
        return s.length() == 3 ? LocaleUtils.isNumericString(s) : false;
    }

    public static boolean isVariant(String s) {
        boolean z = false;
        int len = s.length();
        if (len >= 5 && len <= 8) {
            return LocaleUtils.isAlphaNumericString(s);
        }
        if (len != 4) {
            return false;
        }
        if (LocaleUtils.isNumeric(s.charAt(0)) && LocaleUtils.isAlphaNumeric(s.charAt(1)) && LocaleUtils.isAlphaNumeric(s.charAt(2))) {
            z = LocaleUtils.isAlphaNumeric(s.charAt(3));
        }
        return z;
    }

    public static boolean isExtensionSingleton(String s) {
        return s.length() == 1 && LocaleUtils.isAlphaString(s) && !LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s);
    }

    public static boolean isExtensionSingletonChar(char c) {
        return isExtensionSingleton(String.valueOf(c));
    }

    public static boolean isExtensionSubtag(String s) {
        int len = s.length();
        return (len < 2 || len > 8) ? false : LocaleUtils.isAlphaNumericString(s);
    }

    public static boolean isPrivateusePrefix(String s) {
        if (s.length() == 1) {
            return LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s);
        }
        return false;
    }

    public static boolean isPrivateusePrefixChar(char c) {
        return LocaleUtils.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c));
    }

    public static boolean isPrivateuseSubtag(String s) {
        int len = s.length();
        return (len < 1 || len > 8) ? false : LocaleUtils.isAlphaNumericString(s);
    }

    public static String canonicalizeLanguage(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtlang(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeScript(String s) {
        return LocaleUtils.toTitleString(s);
    }

    public static String canonicalizeRegion(String s) {
        return LocaleUtils.toUpperString(s);
    }

    public static String canonicalizeVariant(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtension(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtensionSingleton(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizeExtensionSubtag(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizePrivateuse(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public static String canonicalizePrivateuseSubtag(String s) {
        return LocaleUtils.toLowerString(s);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.language.length() > 0) {
            sb.append(this.language);
            for (String extlang : this.extlangs) {
                sb.append(SEP).append(extlang);
            }
            if (this.script.length() > 0) {
                sb.append(SEP).append(this.script);
            }
            if (this.region.length() > 0) {
                sb.append(SEP).append(this.region);
            }
            for (String variant : this.variants) {
                sb.append(SEP).append(variant);
            }
            for (String extension : this.extensions) {
                sb.append(SEP).append(extension);
            }
        }
        if (this.privateuse.length() > 0) {
            if (sb.length() > 0) {
                sb.append(SEP);
            }
            sb.append(this.privateuse);
        }
        return sb.toString();
    }
}

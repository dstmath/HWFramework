package android.icu.impl.locale;

import android.icu.impl.locale.AsciiUtil.CaseInsensitiveKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class LanguageTag {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final Map<CaseInsensitiveKey, String[]> GRANDFATHERED = null;
    private static final boolean JDKIMPL = false;
    public static final String PRIVATEUSE = "x";
    public static final String PRIVUSE_VARIANT_PREFIX = "lvariant";
    public static final String SEP = "-";
    public static String UNDETERMINED;
    private List<String> _extensions;
    private List<String> _extlangs;
    private String _language;
    private String _privateuse;
    private String _region;
    private String _script;
    private List<String> _variants;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.locale.LanguageTag.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.locale.LanguageTag.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.locale.LanguageTag.<clinit>():void");
    }

    private LanguageTag() {
        this._language = XmlPullParser.NO_NAMESPACE;
        this._script = XmlPullParser.NO_NAMESPACE;
        this._region = XmlPullParser.NO_NAMESPACE;
        this._privateuse = XmlPullParser.NO_NAMESPACE;
        this._extlangs = Collections.emptyList();
        this._variants = Collections.emptyList();
        this._extensions = Collections.emptyList();
    }

    public static LanguageTag parse(String languageTag, ParseStatus sts) {
        StringTokenIterator itr;
        int i = 0;
        if (sts == null) {
            sts = new ParseStatus();
        } else {
            sts.reset();
        }
        boolean isGrandfathered = JDKIMPL;
        String[] gfmap = (String[]) GRANDFATHERED.get(new CaseInsensitiveKey(languageTag));
        if (gfmap != null) {
            itr = new StringTokenIterator(gfmap[1], SEP);
            isGrandfathered = true;
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
        if (isGrandfathered) {
            if (-assertionsDisabled || itr.isDone()) {
                if (!-assertionsDisabled) {
                    if (!sts.isError()) {
                        i = 1;
                    }
                    if (i == 0) {
                        throw new AssertionError();
                    }
                }
                sts._parseLength = languageTag.length();
            } else {
                throw new AssertionError();
            }
        } else if (!(itr.isDone() || sts.isError())) {
            String s = itr.current();
            sts._errorIndex = itr.currentStart();
            if (s.length() == 0) {
                sts._errorMsg = "Empty subtag";
            } else {
                sts._errorMsg = "Invalid subtag: " + s;
            }
        }
        return tag;
    }

    private boolean parseLanguage(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
        String s = itr.current();
        if (isLanguage(s)) {
            found = true;
            this._language = s;
            sts._parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseExtlangs(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isExtlang(s)) {
                break;
            }
            found = true;
            if (this._extlangs.isEmpty()) {
                this._extlangs = new ArrayList(3);
            }
            this._extlangs.add(s);
            sts._parseLength = itr.currentEnd();
            itr.next();
            if (this._extlangs.size() == 3) {
                break;
            }
        }
        return found;
    }

    private boolean parseScript(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
        String s = itr.current();
        if (isScript(s)) {
            found = true;
            this._script = s;
            sts._parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseRegion(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
        String s = itr.current();
        if (isRegion(s)) {
            found = true;
            this._region = s;
            sts._parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseVariants(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isVariant(s)) {
                break;
            }
            found = true;
            if (this._variants.isEmpty()) {
                this._variants = new ArrayList(3);
            }
            this._variants.add(s);
            sts._parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }

    private boolean parseExtensions(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
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
                sts._parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts._parseLength <= start) {
                sts._errorIndex = start;
                sts._errorMsg = "Incomplete extension '" + singleton + "'";
                break;
            }
            if (this._extensions.size() == 0) {
                this._extensions = new ArrayList(4);
            }
            this._extensions.add(sb.toString());
            found = true;
        }
        return found;
    }

    private boolean parsePrivateuse(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return JDKIMPL;
        }
        boolean found = JDKIMPL;
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
                sts._parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts._parseLength <= start) {
                sts._errorIndex = start;
                sts._errorMsg = "Incomplete privateuse";
            } else {
                this._privateuse = sb.toString();
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
        boolean hasSubtag = JDKIMPL;
        String privuseVar = null;
        if (language.length() > 0 && isLanguage(language)) {
            if (language.equals("iw")) {
                language = "he";
            } else {
                if (language.equals("ji")) {
                    language = "yi";
                } else {
                    if (language.equals("in")) {
                        language = "id";
                    }
                }
            }
            tag._language = language;
        }
        if (script.length() > 0 && isScript(script)) {
            tag._script = canonicalizeScript(script);
            hasSubtag = true;
        }
        if (region.length() > 0 && isRegion(region)) {
            tag._region = canonicalizeRegion(region);
            hasSubtag = true;
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
                variants.add(canonicalizeVariant(var));
                stringTokenIterator.next();
            }
            if (variants != null) {
                tag._variants = variants;
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
                    buf.append(AsciiUtil.toLowerString(prvv));
                    stringTokenIterator.next();
                }
                if (buf.length() > 0) {
                    privuseVar = buf.toString();
                }
            }
        }
        List extensions = null;
        String privateuse = null;
        for (Character locextKey : localeExtensions.getKeys()) {
            Extension ext = localeExtensions.getExtension(locextKey);
            if (isPrivateusePrefixChar(locextKey.charValue())) {
                privateuse = ext.getValue();
            } else {
                if (extensions == null) {
                    extensions = new ArrayList();
                }
                extensions.add(locextKey.toString() + SEP + ext.getValue());
            }
        }
        if (extensions != null) {
            tag._extensions = extensions;
            hasSubtag = true;
        }
        if (privuseVar != null) {
            if (privateuse == null) {
                privateuse = "lvariant-" + privuseVar;
            } else {
                privateuse = privateuse + SEP + PRIVUSE_VARIANT_PREFIX + SEP + privuseVar.replace(BaseLocale.SEP, SEP);
            }
        }
        if (privateuse != null) {
            tag._privateuse = privateuse;
        }
        if (tag._language.length() == 0 && (hasSubtag || privateuse == null)) {
            tag._language = UNDETERMINED;
        }
        return tag;
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

    public static boolean isLanguage(String s) {
        return (s.length() < 2 || s.length() > 8) ? JDKIMPL : AsciiUtil.isAlphaString(s);
    }

    public static boolean isExtlang(String s) {
        return s.length() == 3 ? AsciiUtil.isAlphaString(s) : JDKIMPL;
    }

    public static boolean isScript(String s) {
        return s.length() == 4 ? AsciiUtil.isAlphaString(s) : JDKIMPL;
    }

    public static boolean isRegion(String s) {
        if (s.length() == 2 && AsciiUtil.isAlphaString(s)) {
            return true;
        }
        return s.length() == 3 ? AsciiUtil.isNumericString(s) : JDKIMPL;
    }

    public static boolean isVariant(String s) {
        boolean z = JDKIMPL;
        int len = s.length();
        if (len >= 5 && len <= 8) {
            return AsciiUtil.isAlphaNumericString(s);
        }
        if (len != 4) {
            return JDKIMPL;
        }
        if (AsciiUtil.isNumeric(s.charAt(0)) && AsciiUtil.isAlphaNumeric(s.charAt(1)) && AsciiUtil.isAlphaNumeric(s.charAt(2))) {
            z = AsciiUtil.isAlphaNumeric(s.charAt(3));
        }
        return z;
    }

    public static boolean isExtensionSingleton(String s) {
        return (s.length() == 1 && AsciiUtil.isAlphaString(s) && !AsciiUtil.caseIgnoreMatch(PRIVATEUSE, s)) ? true : JDKIMPL;
    }

    public static boolean isExtensionSingletonChar(char c) {
        return isExtensionSingleton(String.valueOf(c));
    }

    public static boolean isExtensionSubtag(String s) {
        return (s.length() < 2 || s.length() > 8) ? JDKIMPL : AsciiUtil.isAlphaNumericString(s);
    }

    public static boolean isPrivateusePrefix(String s) {
        if (s.length() == 1) {
            return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, s);
        }
        return JDKIMPL;
    }

    public static boolean isPrivateusePrefixChar(char c) {
        return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c));
    }

    public static boolean isPrivateuseSubtag(String s) {
        return (s.length() < 1 || s.length() > 8) ? JDKIMPL : AsciiUtil.isAlphaNumericString(s);
    }

    public static String canonicalizeLanguage(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizeExtlang(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizeScript(String s) {
        return AsciiUtil.toTitleString(s);
    }

    public static String canonicalizeRegion(String s) {
        return AsciiUtil.toUpperString(s);
    }

    public static String canonicalizeVariant(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizeExtension(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizeExtensionSingleton(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizeExtensionSubtag(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizePrivateuse(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public static String canonicalizePrivateuseSubtag(String s) {
        return AsciiUtil.toLowerString(s);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this._language.length() > 0) {
            sb.append(this._language);
            for (String extlang : this._extlangs) {
                sb.append(SEP).append(extlang);
            }
            if (this._script.length() > 0) {
                sb.append(SEP).append(this._script);
            }
            if (this._region.length() > 0) {
                sb.append(SEP).append(this._region);
            }
            for (String variant : this._variants) {
                sb.append(SEP).append(variant);
            }
            for (String extension : this._extensions) {
                sb.append(SEP).append(extension);
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

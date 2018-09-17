package android.icu.impl.locale;

import android.icu.impl.locale.AsciiUtil.CaseInsensitiveKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageTag {
    static final /* synthetic */ boolean -assertionsDisabled = (LanguageTag.class.desiredAssertionStatus() ^ 1);
    private static final Map<CaseInsensitiveKey, String[]> GRANDFATHERED = new HashMap();
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
        entries = new String[26][];
        entries[0] = new String[]{"art-lojban", "jbo"};
        entries[1] = new String[]{"cel-gaulish", "xtg-x-cel-gaulish"};
        entries[2] = new String[]{"en-GB-oed", "en-GB-x-oed"};
        entries[3] = new String[]{"i-ami", "ami"};
        entries[4] = new String[]{"i-bnn", "bnn"};
        entries[5] = new String[]{"i-default", "en-x-i-default"};
        entries[6] = new String[]{"i-enochian", "und-x-i-enochian"};
        entries[7] = new String[]{"i-hak", "hak"};
        entries[8] = new String[]{"i-klingon", "tlh"};
        entries[9] = new String[]{"i-lux", "lb"};
        entries[10] = new String[]{"i-mingo", "see-x-i-mingo"};
        entries[11] = new String[]{"i-navajo", "nv"};
        entries[12] = new String[]{"i-pwn", "pwn"};
        entries[13] = new String[]{"i-tao", "tao"};
        entries[14] = new String[]{"i-tay", "tay"};
        entries[15] = new String[]{"i-tsu", "tsu"};
        entries[16] = new String[]{"no-bok", "nb"};
        entries[17] = new String[]{"no-nyn", "nn"};
        entries[18] = new String[]{"sgn-BE-FR", "sfb"};
        entries[19] = new String[]{"sgn-BE-NL", "vgt"};
        entries[20] = new String[]{"sgn-CH-DE", "sgg"};
        entries[21] = new String[]{"zh-guoyu", "cmn"};
        entries[22] = new String[]{"zh-hakka", "hak"};
        entries[23] = new String[]{"zh-min", "nan-x-zh-min"};
        entries[24] = new String[]{"zh-min-nan", "nan"};
        entries[25] = new String[]{"zh-xiang", "hsn"};
        for (String[] e : entries) {
            GRANDFATHERED.put(new CaseInsensitiveKey(e[0]), e);
        }
    }

    private LanguageTag() {
    }

    public static LanguageTag parse(String languageTag, ParseStatus sts) {
        StringTokenIterator itr;
        if (sts == null) {
            sts = new ParseStatus();
        } else {
            sts.reset();
        }
        boolean isGrandfathered = false;
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
            if (!-assertionsDisabled && !itr.isDone()) {
                throw new AssertionError();
            } else if (-assertionsDisabled || !sts.isError()) {
                sts._parseLength = languageTag.length();
            } else {
                throw new AssertionError();
            }
        } else if (!(itr.isDone() || (sts.isError() ^ 1) == 0)) {
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
            return false;
        }
        boolean found = false;
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
            return false;
        }
        boolean found = false;
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
            return false;
        }
        boolean found = false;
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
            return false;
        }
        boolean found = false;
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
            return false;
        }
        boolean found = false;
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
        boolean hasSubtag = false;
        String privuseVar = null;
        if (language.length() > 0 && isLanguage(language)) {
            if (language.equals("iw")) {
                language = "he";
            } else if (language.equals("ji")) {
                language = "yi";
            } else if (language.equals("in")) {
                language = "id";
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
        return (s.length() < 2 || s.length() > 8) ? false : AsciiUtil.isAlphaString(s);
    }

    public static boolean isExtlang(String s) {
        return s.length() == 3 ? AsciiUtil.isAlphaString(s) : false;
    }

    public static boolean isScript(String s) {
        return s.length() == 4 ? AsciiUtil.isAlphaString(s) : false;
    }

    public static boolean isRegion(String s) {
        if (s.length() == 2 && AsciiUtil.isAlphaString(s)) {
            return true;
        }
        return s.length() == 3 ? AsciiUtil.isNumericString(s) : false;
    }

    public static boolean isVariant(String s) {
        boolean z = false;
        int len = s.length();
        if (len >= 5 && len <= 8) {
            return AsciiUtil.isAlphaNumericString(s);
        }
        if (len != 4) {
            return false;
        }
        if (AsciiUtil.isNumeric(s.charAt(0)) && AsciiUtil.isAlphaNumeric(s.charAt(1)) && AsciiUtil.isAlphaNumeric(s.charAt(2))) {
            z = AsciiUtil.isAlphaNumeric(s.charAt(3));
        }
        return z;
    }

    public static boolean isExtensionSingleton(String s) {
        if (s.length() == 1 && AsciiUtil.isAlphaString(s)) {
            return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, s) ^ 1;
        }
        return false;
    }

    public static boolean isExtensionSingletonChar(char c) {
        return isExtensionSingleton(String.valueOf(c));
    }

    public static boolean isExtensionSubtag(String s) {
        return (s.length() < 2 || s.length() > 8) ? false : AsciiUtil.isAlphaNumericString(s);
    }

    public static boolean isPrivateusePrefix(String s) {
        if (s.length() == 1) {
            return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, s);
        }
        return false;
    }

    public static boolean isPrivateusePrefixChar(char c) {
        return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c));
    }

    public static boolean isPrivateuseSubtag(String s) {
        return (s.length() < 1 || s.length() > 8) ? false : AsciiUtil.isAlphaNumericString(s);
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

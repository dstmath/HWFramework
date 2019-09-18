package android.icu.impl.locale;

import android.icu.impl.locale.AsciiUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        for (String[] e : new String[][]{new String[]{"art-lojban", "jbo"}, new String[]{"cel-gaulish", "xtg-x-cel-gaulish"}, new String[]{"en-GB-oed", "en-GB-x-oed"}, new String[]{"i-ami", "ami"}, new String[]{"i-bnn", "bnn"}, new String[]{"i-default", "en-x-i-default"}, new String[]{"i-enochian", "und-x-i-enochian"}, new String[]{"i-hak", "hak"}, new String[]{"i-klingon", "tlh"}, new String[]{"i-lux", "lb"}, new String[]{"i-mingo", "see-x-i-mingo"}, new String[]{"i-navajo", "nv"}, new String[]{"i-pwn", "pwn"}, new String[]{"i-tao", "tao"}, new String[]{"i-tay", "tay"}, new String[]{"i-tsu", "tsu"}, new String[]{"no-bok", "nb"}, new String[]{"no-nyn", "nn"}, new String[]{"sgn-BE-FR", "sfb"}, new String[]{"sgn-BE-NL", "vgt"}, new String[]{"sgn-CH-DE", "sgg"}, new String[]{"zh-guoyu", "cmn"}, new String[]{"zh-hakka", "hak"}, new String[]{"zh-min", "nan-x-zh-min"}, new String[]{"zh-min-nan", "nan"}, new String[]{"zh-xiang", "hsn"}}) {
            GRANDFATHERED.put(new AsciiUtil.CaseInsensitiveKey(e[0]), e);
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
        String[] gfmap = GRANDFATHERED.get(new AsciiUtil.CaseInsensitiveKey(languageTag));
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
            sts._parseLength = languageTag.length();
        } else if (!itr.isDone() && !sts.isError()) {
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
        boolean found;
        boolean found2 = false;
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        while (true) {
            found = found2;
            if (itr.isDone()) {
                break;
            }
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
            found2 = true;
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
        boolean found;
        boolean found2 = false;
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        while (true) {
            found = found2;
            if (itr.isDone()) {
                break;
            }
            String s = itr.current();
            if (!isVariant(s)) {
                break;
            }
            if (this._variants.isEmpty()) {
                this._variants = new ArrayList(3);
            }
            this._variants.add(s);
            sts._parseLength = itr.currentEnd();
            itr.next();
            found2 = true;
        }
        return found;
    }

    private boolean parseExtensions(StringTokenIterator itr, ParseStatus sts) {
        boolean found;
        boolean found2 = false;
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        while (true) {
            found = found2;
            if (itr.isDone()) {
                break;
            }
            String s = itr.current();
            if (!isExtensionSingleton(s)) {
                break;
            }
            int start = itr.currentStart();
            String singleton = s;
            StringBuilder sb = new StringBuilder(singleton);
            itr.next();
            while (!itr.isDone()) {
                String s2 = itr.current();
                if (!isExtensionSubtag(s2)) {
                    break;
                }
                sb.append(SEP);
                sb.append(s2);
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
            found2 = true;
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
                String s2 = itr.current();
                if (!isPrivateuseSubtag(s2)) {
                    break;
                }
                sb.append(SEP);
                sb.append(s2);
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
            StringTokenIterator varitr = new StringTokenIterator(variant, BaseLocale.SEP);
            while (!varitr.isDone()) {
                String var = varitr.current();
                if (!isVariant(var)) {
                    break;
                }
                if (variants == null) {
                    variants = new ArrayList<>();
                }
                variants.add(canonicalizeVariant(var));
                varitr.next();
            }
            if (variants != null) {
                tag._variants = variants;
                hasSubtag = true;
            }
            if (!varitr.isDone()) {
                StringBuilder buf = new StringBuilder();
                while (!varitr.isDone()) {
                    String prvv = varitr.current();
                    if (!isPrivateuseSubtag(prvv)) {
                        break;
                    }
                    if (buf.length() > 0) {
                        buf.append(SEP);
                    }
                    buf.append(AsciiUtil.toLowerString(prvv));
                    varitr.next();
                }
                if (buf.length() > 0) {
                    privuseVar = buf.toString();
                }
            }
        }
        List<String> extensions = null;
        String privateuse = null;
        for (Character locextKey : localeExtensions.getKeys()) {
            Extension ext = localeExtensions.getExtension(locextKey);
            if (isPrivateusePrefixChar(locextKey.charValue())) {
                privateuse = ext.getValue();
            } else {
                if (extensions == null) {
                    extensions = new ArrayList<>();
                }
                extensions.add(locextKey.toString() + SEP + ext.getValue());
            }
        }
        LocaleExtensions localeExtensions2 = localeExtensions;
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
        return s.length() >= 2 && s.length() <= 8 && AsciiUtil.isAlphaString(s);
    }

    public static boolean isExtlang(String s) {
        return s.length() == 3 && AsciiUtil.isAlphaString(s);
    }

    public static boolean isScript(String s) {
        return s.length() == 4 && AsciiUtil.isAlphaString(s);
    }

    public static boolean isRegion(String s) {
        return (s.length() == 2 && AsciiUtil.isAlphaString(s)) || (s.length() == 3 && AsciiUtil.isNumericString(s));
    }

    public static boolean isVariant(String s) {
        int len = s.length();
        if (len >= 5 && len <= 8) {
            return AsciiUtil.isAlphaNumericString(s);
        }
        boolean z = false;
        if (len != 4) {
            return false;
        }
        if (AsciiUtil.isNumeric(s.charAt(0)) && AsciiUtil.isAlphaNumeric(s.charAt(1)) && AsciiUtil.isAlphaNumeric(s.charAt(2)) && AsciiUtil.isAlphaNumeric(s.charAt(3))) {
            z = true;
        }
        return z;
    }

    public static boolean isExtensionSingleton(String s) {
        if (s.length() != 1 || !AsciiUtil.isAlphaString(s) || AsciiUtil.caseIgnoreMatch(PRIVATEUSE, s)) {
            return false;
        }
        return true;
    }

    public static boolean isExtensionSingletonChar(char c) {
        return isExtensionSingleton(String.valueOf(c));
    }

    public static boolean isExtensionSubtag(String s) {
        return s.length() >= 2 && s.length() <= 8 && AsciiUtil.isAlphaNumericString(s);
    }

    public static boolean isPrivateusePrefix(String s) {
        if (s.length() != 1 || !AsciiUtil.caseIgnoreMatch(PRIVATEUSE, s)) {
            return false;
        }
        return true;
    }

    public static boolean isPrivateusePrefixChar(char c) {
        return AsciiUtil.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c));
    }

    public static boolean isPrivateuseSubtag(String s) {
        return s.length() >= 1 && s.length() <= 8 && AsciiUtil.isAlphaNumericString(s);
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
                sb.append(SEP);
                sb.append(extlang);
            }
            if (this._script.length() > 0) {
                sb.append(SEP);
                sb.append(this._script);
            }
            if (this._region.length() > 0) {
                sb.append(SEP);
                sb.append(this._region);
            }
            for (String variant : this._variants) {
                sb.append(SEP);
                sb.append(variant);
            }
            for (String extension : this._extensions) {
                sb.append(SEP);
                sb.append(extension);
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

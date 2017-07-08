package android.icu.impl.locale;

import org.xmlpull.v1.XmlPullParser;

public final class BaseLocale {
    private static final Cache CACHE = null;
    private static final boolean JDKIMPL = false;
    public static final BaseLocale ROOT = null;
    public static final String SEP = "_";
    private volatile transient int _hash;
    private String _language;
    private String _region;
    private String _script;
    private String _variant;

    private static class Cache extends LocaleObjectCache<Key, BaseLocale> {
        protected Key normalizeKey(Key key) {
            return Key.normalize(key);
        }

        protected BaseLocale createObject(Key key) {
            return new BaseLocale(key._scrt, key._regn, key._vart, null);
        }
    }

    private static class Key implements Comparable<Key> {
        private volatile int _hash;
        private String _lang;
        private String _regn;
        private String _scrt;
        private String _vart;

        public Key(String language, String script, String region, String variant) {
            this._lang = XmlPullParser.NO_NAMESPACE;
            this._scrt = XmlPullParser.NO_NAMESPACE;
            this._regn = XmlPullParser.NO_NAMESPACE;
            this._vart = XmlPullParser.NO_NAMESPACE;
            if (language != null) {
                this._lang = language;
            }
            if (script != null) {
                this._scrt = script;
            }
            if (region != null) {
                this._regn = region;
            }
            if (variant != null) {
                this._vart = variant;
            }
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj instanceof Key) && AsciiUtil.caseIgnoreMatch(((Key) obj)._lang, this._lang) && AsciiUtil.caseIgnoreMatch(((Key) obj)._scrt, this._scrt) && AsciiUtil.caseIgnoreMatch(((Key) obj)._regn, this._regn)) {
                return AsciiUtil.caseIgnoreMatch(((Key) obj)._vart, this._vart);
            }
            return BaseLocale.JDKIMPL;
        }

        public int compareTo(Key other) {
            int res = AsciiUtil.caseIgnoreCompare(this._lang, other._lang);
            if (res != 0) {
                return res;
            }
            res = AsciiUtil.caseIgnoreCompare(this._scrt, other._scrt);
            if (res != 0) {
                return res;
            }
            res = AsciiUtil.caseIgnoreCompare(this._regn, other._regn);
            if (res == 0) {
                return AsciiUtil.caseIgnoreCompare(this._vart, other._vart);
            }
            return res;
        }

        public int hashCode() {
            int h = this._hash;
            if (h == 0) {
                int i;
                for (i = 0; i < this._lang.length(); i++) {
                    h = (h * 31) + AsciiUtil.toLower(this._lang.charAt(i));
                }
                for (i = 0; i < this._scrt.length(); i++) {
                    h = (h * 31) + AsciiUtil.toLower(this._scrt.charAt(i));
                }
                for (i = 0; i < this._regn.length(); i++) {
                    h = (h * 31) + AsciiUtil.toLower(this._regn.charAt(i));
                }
                for (i = 0; i < this._vart.length(); i++) {
                    h = (h * 31) + AsciiUtil.toLower(this._vart.charAt(i));
                }
                this._hash = h;
            }
            return h;
        }

        public static Key normalize(Key key) {
            return new Key(AsciiUtil.toLowerString(key._lang).intern(), AsciiUtil.toTitleString(key._scrt).intern(), AsciiUtil.toUpperString(key._regn).intern(), AsciiUtil.toUpperString(key._vart).intern());
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.locale.BaseLocale.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.locale.BaseLocale.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.locale.BaseLocale.<clinit>():void");
    }

    private BaseLocale(String language, String script, String region, String variant) {
        this._language = XmlPullParser.NO_NAMESPACE;
        this._script = XmlPullParser.NO_NAMESPACE;
        this._region = XmlPullParser.NO_NAMESPACE;
        this._variant = XmlPullParser.NO_NAMESPACE;
        this._hash = 0;
        if (language != null) {
            this._language = AsciiUtil.toLowerString(language).intern();
        }
        if (script != null) {
            this._script = AsciiUtil.toTitleString(script).intern();
        }
        if (region != null) {
            this._region = AsciiUtil.toUpperString(region).intern();
        }
        if (variant != null) {
            this._variant = AsciiUtil.toUpperString(variant).intern();
        }
    }

    public static BaseLocale getInstance(String language, String script, String region, String variant) {
        return (BaseLocale) CACHE.get(new Key(language, script, region, variant));
    }

    public String getLanguage() {
        return this._language;
    }

    public String getScript() {
        return this._script;
    }

    public String getRegion() {
        return this._region;
    }

    public String getVariant() {
        return this._variant;
    }

    public boolean equals(Object obj) {
        boolean z = JDKIMPL;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BaseLocale)) {
            return JDKIMPL;
        }
        BaseLocale other = (BaseLocale) obj;
        if (hashCode() == other.hashCode() && this._language.equals(other._language) && this._script.equals(other._script) && this._region.equals(other._region)) {
            z = this._variant.equals(other._variant);
        }
        return z;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (this._language.length() > 0) {
            buf.append("language=");
            buf.append(this._language);
        }
        if (this._script.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("script=");
            buf.append(this._script);
        }
        if (this._region.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("region=");
            buf.append(this._region);
        }
        if (this._variant.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("variant=");
            buf.append(this._variant);
        }
        return buf.toString();
    }

    public int hashCode() {
        int h = this._hash;
        if (h == 0) {
            int i;
            for (i = 0; i < this._language.length(); i++) {
                h = (h * 31) + this._language.charAt(i);
            }
            for (i = 0; i < this._script.length(); i++) {
                h = (h * 31) + this._script.charAt(i);
            }
            for (i = 0; i < this._region.length(); i++) {
                h = (h * 31) + this._region.charAt(i);
            }
            for (i = 0; i < this._variant.length(); i++) {
                h = (h * 31) + this._variant.charAt(i);
            }
            this._hash = h;
        }
        return h;
    }
}

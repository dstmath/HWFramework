package android.icu.impl.locale;

public final class BaseLocale {
    private static final Cache CACHE = new Cache();
    private static final boolean JDKIMPL = false;
    public static final BaseLocale ROOT = getInstance("", "", "", "");
    public static final String SEP = "_";
    private volatile transient int _hash;
    private String _language;
    private String _region;
    private String _script;
    private String _variant;

    private static class Cache extends LocaleObjectCache<Key, BaseLocale> {
        /* access modifiers changed from: protected */
        public Key normalizeKey(Key key) {
            return Key.normalize(key);
        }

        /* access modifiers changed from: protected */
        public BaseLocale createObject(Key key) {
            BaseLocale baseLocale = new BaseLocale(key._lang, key._scrt, key._regn, key._vart);
            return baseLocale;
        }
    }

    private static class Key implements Comparable<Key> {
        private volatile int _hash;
        /* access modifiers changed from: private */
        public String _lang = "";
        /* access modifiers changed from: private */
        public String _regn = "";
        /* access modifiers changed from: private */
        public String _scrt = "";
        /* access modifiers changed from: private */
        public String _vart = "";

        public Key(String language, String script, String region, String variant) {
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
            return this == obj || ((obj instanceof Key) && AsciiUtil.caseIgnoreMatch(((Key) obj)._lang, this._lang) && AsciiUtil.caseIgnoreMatch(((Key) obj)._scrt, this._scrt) && AsciiUtil.caseIgnoreMatch(((Key) obj)._regn, this._regn) && AsciiUtil.caseIgnoreMatch(((Key) obj)._vart, this._vart));
        }

        public int compareTo(Key other) {
            int res = AsciiUtil.caseIgnoreCompare(this._lang, other._lang);
            if (res != 0) {
                return res;
            }
            int res2 = AsciiUtil.caseIgnoreCompare(this._scrt, other._scrt);
            if (res2 != 0) {
                return res2;
            }
            int res3 = AsciiUtil.caseIgnoreCompare(this._regn, other._regn);
            if (res3 == 0) {
                return AsciiUtil.caseIgnoreCompare(this._vart, other._vart);
            }
            return res3;
        }

        public int hashCode() {
            int h = this._hash;
            if (h == 0) {
                int h2 = h;
                for (int i = 0; i < this._lang.length(); i++) {
                    h2 = (31 * h2) + AsciiUtil.toLower(this._lang.charAt(i));
                }
                for (int i2 = 0; i2 < this._scrt.length(); i2++) {
                    h2 = (31 * h2) + AsciiUtil.toLower(this._scrt.charAt(i2));
                }
                for (int i3 = 0; i3 < this._regn.length(); i3++) {
                    h2 = (31 * h2) + AsciiUtil.toLower(this._regn.charAt(i3));
                }
                h = h2;
                for (int i4 = 0; i4 < this._vart.length(); i4++) {
                    h = (31 * h) + AsciiUtil.toLower(this._vart.charAt(i4));
                }
                this._hash = h;
            }
            return h;
        }

        public static Key normalize(Key key) {
            return new Key(AsciiUtil.toLowerString(key._lang).intern(), AsciiUtil.toTitleString(key._scrt).intern(), AsciiUtil.toUpperString(key._regn).intern(), AsciiUtil.toUpperString(key._vart).intern());
        }
    }

    private BaseLocale(String language, String script, String region, String variant) {
        this._language = "";
        this._script = "";
        this._region = "";
        this._variant = "";
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
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BaseLocale)) {
            return false;
        }
        BaseLocale other = (BaseLocale) obj;
        if (hashCode() != other.hashCode() || !this._language.equals(other._language) || !this._script.equals(other._script) || !this._region.equals(other._region) || !this._variant.equals(other._variant)) {
            z = false;
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
            int h2 = h;
            for (int i = 0; i < this._language.length(); i++) {
                h2 = (31 * h2) + this._language.charAt(i);
            }
            for (int i2 = 0; i2 < this._script.length(); i2++) {
                h2 = (31 * h2) + this._script.charAt(i2);
            }
            for (int i3 = 0; i3 < this._region.length(); i3++) {
                h2 = (31 * h2) + this._region.charAt(i3);
            }
            h = h2;
            for (int i4 = 0; i4 < this._variant.length(); i4++) {
                h = (31 * h) + this._variant.charAt(i4);
            }
            this._hash = h;
        }
        return h;
    }
}

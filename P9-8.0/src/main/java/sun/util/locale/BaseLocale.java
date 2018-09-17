package sun.util.locale;

import sun.security.x509.PolicyInformation;

public final class BaseLocale {
    private static final Cache CACHE = new Cache();
    public static final String SEP = "_";
    private volatile int hash;
    private final String language;
    private final String region;
    private final String script;
    private final String variant;

    private static class Cache extends LocaleObjectCache<Key, BaseLocale> {
        protected Key normalizeKey(Key key) {
            return Key.normalize(key);
        }

        protected BaseLocale createObject(Key key) {
            return new BaseLocale(key.lang, key.scrt, key.regn, key.vart, null);
        }
    }

    private static final class Key implements Comparable<Key> {
        static final /* synthetic */ boolean -assertionsDisabled = (Key.class.desiredAssertionStatus() ^ 1);
        private final int hash;
        private final String lang;
        private final boolean normalized;
        private final String regn;
        private final String scrt;
        private final String vart;

        /* synthetic */ Key(String language, String region, Key -this2) {
            this(language, region);
        }

        private Key(String language, String region) {
            if (-assertionsDisabled || (language.intern() == language && region.intern() == region)) {
                this.lang = language;
                this.scrt = "";
                this.regn = region;
                this.vart = "";
                this.normalized = true;
                int h = language.hashCode();
                if (region != "") {
                    for (int i = 0; i < region.length(); i++) {
                        h = (h * 31) + LocaleUtils.toLower(region.charAt(i));
                    }
                }
                this.hash = h;
                return;
            }
            throw new AssertionError();
        }

        public Key(String language, String script, String region, String variant) {
            this(language, script, region, variant, false);
        }

        private Key(String language, String script, String region, String variant, boolean normalized) {
            int i;
            int h = 0;
            if (language != null) {
                this.lang = language;
                for (i = 0; i < language.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(language.charAt(i));
                }
            } else {
                this.lang = "";
            }
            if (script != null) {
                this.scrt = script;
                for (i = 0; i < script.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(script.charAt(i));
                }
            } else {
                this.scrt = "";
            }
            if (region != null) {
                this.regn = region;
                for (i = 0; i < region.length(); i++) {
                    h = (h * 31) + LocaleUtils.toLower(region.charAt(i));
                }
            } else {
                this.regn = "";
            }
            if (variant != null) {
                this.vart = variant;
                for (i = 0; i < variant.length(); i++) {
                    h = (h * 31) + variant.charAt(i);
                }
            } else {
                this.vart = "";
            }
            this.hash = h;
            this.normalized = normalized;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if ((obj instanceof Key) && this.hash == ((Key) obj).hash && LocaleUtils.caseIgnoreMatch(((Key) obj).lang, this.lang) && LocaleUtils.caseIgnoreMatch(((Key) obj).scrt, this.scrt) && LocaleUtils.caseIgnoreMatch(((Key) obj).regn, this.regn)) {
                return ((Key) obj).vart.equals(this.vart);
            }
            return false;
        }

        public int compareTo(Key other) {
            int res = LocaleUtils.caseIgnoreCompare(this.lang, other.lang);
            if (res != 0) {
                return res;
            }
            res = LocaleUtils.caseIgnoreCompare(this.scrt, other.scrt);
            if (res != 0) {
                return res;
            }
            res = LocaleUtils.caseIgnoreCompare(this.regn, other.regn);
            if (res == 0) {
                return this.vart.compareTo(other.vart);
            }
            return res;
        }

        public int hashCode() {
            return this.hash;
        }

        public static Key normalize(Key key) {
            if (key.normalized) {
                return key;
            }
            return new Key(LocaleUtils.toLowerString(key.lang).intern(), LocaleUtils.toTitleString(key.scrt).intern(), LocaleUtils.toUpperString(key.regn).intern(), key.vart.intern(), true);
        }
    }

    /* synthetic */ BaseLocale(String language, String script, String region, String variant, BaseLocale -this4) {
        this(language, script, region, variant);
    }

    private BaseLocale(String language, String region) {
        this.hash = 0;
        this.language = language;
        this.script = "";
        this.region = region;
        this.variant = "";
    }

    private BaseLocale(String language, String script, String region, String variant) {
        this.hash = 0;
        this.language = language != null ? LocaleUtils.toLowerString(language).intern() : "";
        this.script = script != null ? LocaleUtils.toTitleString(script).intern() : "";
        this.region = region != null ? LocaleUtils.toUpperString(region).intern() : "";
        this.variant = variant != null ? variant.intern() : "";
    }

    public static BaseLocale createInstance(String language, String region) {
        BaseLocale base = new BaseLocale(language, region);
        CACHE.put(new Key(language, region, null), base);
        return base;
    }

    public static BaseLocale getInstance(String language, String script, String region, String variant) {
        if (language != null) {
            if (LocaleUtils.caseIgnoreMatch(language, "he")) {
                language = "iw";
            } else if (LocaleUtils.caseIgnoreMatch(language, "yi")) {
                language = "ji";
            } else if (LocaleUtils.caseIgnoreMatch(language, PolicyInformation.ID)) {
                language = "in";
            }
        }
        return (BaseLocale) CACHE.get(new Key(language, script, region, variant));
    }

    public String getLanguage() {
        return this.language;
    }

    public String getScript() {
        return this.script;
    }

    public String getRegion() {
        return this.region;
    }

    public String getVariant() {
        return this.variant;
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
        if (this.language != other.language || this.script != other.script || this.region != other.region) {
            z = false;
        } else if (this.variant != other.variant) {
            z = false;
        }
        return z;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (this.language.length() > 0) {
            buf.append("language=");
            buf.append(this.language);
        }
        if (this.script.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("script=");
            buf.append(this.script);
        }
        if (this.region.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("region=");
            buf.append(this.region);
        }
        if (this.variant.length() > 0) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("variant=");
            buf.append(this.variant);
        }
        return buf.-java_util_stream_Collectors-mthref-7();
    }

    public int hashCode() {
        int h = this.hash;
        if (h != 0) {
            return h;
        }
        h = (((((this.language.hashCode() * 31) + this.script.hashCode()) * 31) + this.region.hashCode()) * 31) + this.variant.hashCode();
        this.hash = h;
        return h;
    }
}

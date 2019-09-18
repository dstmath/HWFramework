package sun.util.locale;

import java.lang.ref.SoftReference;
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
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<BaseLocale> cls = BaseLocale.class;
        }

        /* access modifiers changed from: protected */
        public Key normalizeKey(Key key) {
            return Key.normalize(key);
        }

        /* access modifiers changed from: protected */
        public BaseLocale createObject(Key key) {
            BaseLocale baseLocale = new BaseLocale((String) key.lang.get(), (String) key.scrt.get(), (String) key.regn.get(), (String) key.vart.get());
            return baseLocale;
        }
    }

    private static final class Key {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final int hash;
        /* access modifiers changed from: private */
        public final SoftReference<String> lang;
        private final boolean normalized;
        /* access modifiers changed from: private */
        public final SoftReference<String> regn;
        /* access modifiers changed from: private */
        public final SoftReference<String> scrt;
        /* access modifiers changed from: private */
        public final SoftReference<String> vart;

        static {
            Class<BaseLocale> cls = BaseLocale.class;
        }

        private Key(String language, String region) {
            this.lang = new SoftReference<>(language);
            this.scrt = new SoftReference<>("");
            this.regn = new SoftReference<>(region);
            this.vart = new SoftReference<>("");
            this.normalized = true;
            int h = language.hashCode();
            if (region != "") {
                int len = region.length();
                for (int i = 0; i < len; i++) {
                    h = (31 * h) + LocaleUtils.toLower(region.charAt(i));
                }
            }
            this.hash = h;
        }

        public Key(String language, String script, String region, String variant) {
            this(language, script, region, variant, false);
        }

        private Key(String language, String script, String region, String variant, boolean normalized2) {
            int h = 0;
            if (language != null) {
                this.lang = new SoftReference<>(language);
                int len = language.length();
                int h2 = 0;
                for (int i = 0; i < len; i++) {
                    h2 = (31 * h2) + LocaleUtils.toLower(language.charAt(i));
                }
                h = h2;
            } else {
                this.lang = new SoftReference<>("");
            }
            if (script != null) {
                this.scrt = new SoftReference<>(script);
                int len2 = script.length();
                int h3 = h;
                for (int i2 = 0; i2 < len2; i2++) {
                    h3 = (31 * h3) + LocaleUtils.toLower(script.charAt(i2));
                }
                h = h3;
            } else {
                this.scrt = new SoftReference<>("");
            }
            if (region != null) {
                this.regn = new SoftReference<>(region);
                int len3 = region.length();
                int h4 = h;
                for (int i3 = 0; i3 < len3; i3++) {
                    h4 = (31 * h4) + LocaleUtils.toLower(region.charAt(i3));
                }
                h = h4;
            } else {
                this.regn = new SoftReference<>("");
            }
            if (variant != null) {
                this.vart = new SoftReference<>(variant);
                int len4 = variant.length();
                for (int i4 = 0; i4 < len4; i4++) {
                    h = (31 * h) + variant.charAt(i4);
                }
            } else {
                this.vart = new SoftReference<>("");
            }
            this.hash = h;
            this.normalized = normalized2;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if ((obj instanceof Key) && this.hash == ((Key) obj).hash) {
                String tl = this.lang.get();
                String ol = ((Key) obj).lang.get();
                if (!(tl == null || ol == null || !LocaleUtils.caseIgnoreMatch(ol, tl))) {
                    String ts = this.scrt.get();
                    String os = ((Key) obj).scrt.get();
                    if (!(ts == null || os == null || !LocaleUtils.caseIgnoreMatch(os, ts))) {
                        String tr = this.regn.get();
                        String or = ((Key) obj).regn.get();
                        if (!(tr == null || or == null || !LocaleUtils.caseIgnoreMatch(or, tr))) {
                            String tv = this.vart.get();
                            String ov = ((Key) obj).vart.get();
                            if (ov == null || !ov.equals(tv)) {
                                z = false;
                            }
                            return z;
                        }
                    }
                }
            }
            return false;
        }

        public int hashCode() {
            return this.hash;
        }

        public static Key normalize(Key key) {
            if (key.normalized) {
                return key;
            }
            Key key2 = new Key(LocaleUtils.toLowerString(key.lang.get()).intern(), LocaleUtils.toTitleString(key.scrt.get()).intern(), LocaleUtils.toUpperString(key.regn.get()).intern(), key.vart.get().intern(), true);
            return key2;
        }
    }

    private BaseLocale(String language2, String region2) {
        this.hash = 0;
        this.language = language2;
        this.script = "";
        this.region = region2;
        this.variant = "";
    }

    private BaseLocale(String language2, String script2, String region2, String variant2) {
        this.hash = 0;
        this.language = language2 != null ? LocaleUtils.toLowerString(language2).intern() : "";
        this.script = script2 != null ? LocaleUtils.toTitleString(script2).intern() : "";
        this.region = region2 != null ? LocaleUtils.toUpperString(region2).intern() : "";
        this.variant = variant2 != null ? variant2.intern() : "";
    }

    public static BaseLocale createInstance(String language2, String region2) {
        BaseLocale base = new BaseLocale(language2, region2);
        CACHE.put(new Key(language2, region2), base);
        return base;
    }

    public static BaseLocale getInstance(String language2, String script2, String region2, String variant2) {
        if (language2 != null) {
            if (LocaleUtils.caseIgnoreMatch(language2, "he")) {
                language2 = "iw";
            } else if (LocaleUtils.caseIgnoreMatch(language2, "yi")) {
                language2 = "ji";
            } else if (LocaleUtils.caseIgnoreMatch(language2, PolicyInformation.ID)) {
                language2 = "in";
            }
        }
        return (BaseLocale) CACHE.get(new Key(language2, script2, region2, variant2));
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
        if (!(this.language == other.language && this.script == other.script && this.region == other.region && this.variant == other.variant)) {
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
        return buf.toString();
    }

    public int hashCode() {
        int h = this.hash;
        if (h != 0) {
            return h;
        }
        int h2 = (31 * ((31 * ((31 * this.language.hashCode()) + this.script.hashCode())) + this.region.hashCode())) + this.variant.hashCode();
        this.hash = h2;
        return h2;
    }
}

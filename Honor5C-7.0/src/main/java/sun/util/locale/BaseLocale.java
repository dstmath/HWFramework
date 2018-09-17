package sun.util.locale;

import sun.security.x509.PolicyInformation;

public final class BaseLocale {
    private static final Cache CACHE = null;
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
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private final int hash;
        private final String lang;
        private final boolean normalized;
        private final String regn;
        private final String scrt;
        private final String vart;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.locale.BaseLocale.Key.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.locale.BaseLocale.Key.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.BaseLocale.Key.<clinit>():void");
        }

        private Key(String language, String region) {
            boolean z = false;
            if (!-assertionsDisabled) {
                if (language.intern() == language && region.intern() == region) {
                    z = true;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.util.locale.BaseLocale.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.util.locale.BaseLocale.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.util.locale.BaseLocale.<clinit>():void");
    }

    /* synthetic */ BaseLocale(String language, String script, String region, String variant, BaseLocale baseLocale) {
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
        CACHE.put(new Key(region, null), base);
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
        return buf.toString();
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

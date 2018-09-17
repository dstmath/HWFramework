package huawei.android.hwutil;

import android.icu.text.AlphabeticIndex;
import android.icu.text.AlphabeticIndex.ImmutableIndex;
import android.text.TextUtils;
import java.lang.Character.UnicodeBlock;
import java.util.Locale;
import java.util.Set;
import libcore.icu.ICU;

public class SectionLocaleUtils {
    private static final int DEFAULTNUMBERBUCKETINDEX = 0;
    private static final String JAPANESE_LANGUAGE = null;
    public static final Locale LOCALE_ARABIC = null;
    public static final Locale LOCALE_FARSI = null;
    public static final Locale LOCALE_GREEK = null;
    public static final Locale LOCALE_HEBREW = null;
    public static final Locale LOCALE_HINDI = null;
    public static final Locale LOCALE_THAI = null;
    public static final Locale LOCALE_UKRAINIAN = null;
    private static SectionLocaleUtils sSingleton;
    private String mLanguage;
    private final Locale mLocale;
    private final SectionLocaleUtilsBase mUtils;

    private static class SectionLocaleUtilsBase {
        private static final String EMPTY_STRING = "";
        private static final String NUMBER_STRING = "#";
        protected final ImmutableIndex mAlphabeticIndex;
        private final int mAlphabeticIndexBucketCount;
        private final Locale mCurrentLocale;
        private final int mNumberBucketIndex;

        public SectionLocaleUtilsBase(Locale locale) {
            this.mCurrentLocale = locale;
            Locale arabicScriptLocale = SectionLocaleUtils.LOCALE_ARABIC;
            if ("fa".equals(locale.getLanguage())) {
                arabicScriptLocale = SectionLocaleUtils.LOCALE_FARSI;
            }
            this.mAlphabeticIndex = new AlphabeticIndex(locale).setMaxLabelCount(300).addLabels(new Locale[]{Locale.ENGLISH}).addLabels(new Locale[]{Locale.JAPANESE}).addLabels(new Locale[]{Locale.KOREAN}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_THAI}).addLabels(new Locale[]{arabicScriptLocale}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_HEBREW}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_GREEK}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_UKRAINIAN}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_HINDI}).buildImmutableIndex();
            this.mAlphabeticIndexBucketCount = this.mAlphabeticIndex.getBucketCount();
            this.mNumberBucketIndex = this.mAlphabeticIndexBucketCount - 1;
        }

        public int getBucketIndex(String displayName) {
            int length = displayName.length();
            int offset = 0;
            while (offset < length) {
                int codePoint = Character.codePointAt(displayName, offset);
                if (!Character.isDigit(codePoint)) {
                    if (!Character.isSpaceChar(codePoint) && codePoint != 43 && codePoint != 40 && codePoint != 41 && codePoint != 46 && codePoint != 45 && codePoint != 35) {
                        break;
                    }
                    offset += Character.charCount(codePoint);
                } else {
                    break;
                }
            }
            int bucket = this.mAlphabeticIndex.getBucketIndex(displayName);
            if (bucket < 0) {
                return -1;
            }
            if (bucket >= this.mNumberBucketIndex) {
                return bucket + 1;
            }
            if ("TW".equals(this.mCurrentLocale.getCountry())) {
                codePoint = Character.codePointAt(displayName, offset);
                if (codePoint >= 12549 && codePoint <= 12573) {
                    return (codePoint - 12549) + 1;
                }
                if (codePoint >= 12573 && codePoint <= 12585) {
                    return (codePoint - 12550) + 1;
                }
            }
            return bucket;
        }

        public int getBucketCount() {
            return this.mAlphabeticIndexBucketCount + 1;
        }

        public String getBucketLabel(int bucketIndex) {
            if (bucketIndex < 0 || bucketIndex >= getBucketCount()) {
                return EMPTY_STRING;
            }
            if (bucketIndex == 0) {
                return NUMBER_STRING;
            }
            String str;
            if (bucketIndex > this.mNumberBucketIndex) {
                bucketIndex--;
            }
            if (this.mAlphabeticIndex.getBucket(bucketIndex) == null) {
                str = EMPTY_STRING;
            } else {
                str = this.mAlphabeticIndex.getBucket(bucketIndex).getLabel();
            }
            return str;
        }

        public String getSortKey(String name) {
            return name;
        }
    }

    private static class JapaneseSectionUtils extends SectionLocaleUtilsBase {
        private static final Set<UnicodeBlock> CJ_BLOCKS = null;
        private static final String JAPANESE_MISC_LABEL = "\u4ed6";
        private final int mMiscBucketIndex;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwutil.SectionLocaleUtils.JapaneseSectionUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwutil.SectionLocaleUtils.JapaneseSectionUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwutil.SectionLocaleUtils.JapaneseSectionUtils.<clinit>():void");
        }

        public JapaneseSectionUtils(Locale locale) {
            super(locale);
            this.mMiscBucketIndex = super.getBucketIndex("\u65e5");
        }

        private static boolean isChineseOrJapanese(int codePoint) {
            return CJ_BLOCKS.contains(UnicodeBlock.of(codePoint));
        }

        public int getBucketIndex(String displayName) {
            int bucketIndex = super.getBucketIndex(displayName);
            if ((bucketIndex != this.mMiscBucketIndex || isChineseOrJapanese(Character.codePointAt(displayName, 0))) && bucketIndex <= this.mMiscBucketIndex) {
                return bucketIndex;
            }
            return bucketIndex + 1;
        }

        public int getBucketCount() {
            return super.getBucketCount() + 1;
        }

        public String getBucketLabel(int bucketIndex) {
            if (bucketIndex == this.mMiscBucketIndex) {
                return JAPANESE_MISC_LABEL;
            }
            if (bucketIndex > this.mMiscBucketIndex) {
                bucketIndex--;
            }
            return super.getBucketLabel(bucketIndex);
        }
    }

    private static class SimplifiedChineseSectionUtils extends SectionLocaleUtilsBase {
        public SimplifiedChineseSectionUtils(Locale locale) {
            super(locale);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwutil.SectionLocaleUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwutil.SectionLocaleUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwutil.SectionLocaleUtils.<clinit>():void");
    }

    private SectionLocaleUtils(Locale locale) {
        if (locale == null) {
            this.mLocale = Locale.getDefault();
        } else {
            this.mLocale = locale;
        }
        this.mLanguage = this.mLocale.getLanguage();
        if (this.mLanguage.equals(JAPANESE_LANGUAGE)) {
            this.mUtils = new JapaneseSectionUtils(this.mLocale);
        } else if (this.mLocale.equals(Locale.CHINA)) {
            this.mUtils = new SimplifiedChineseSectionUtils(this.mLocale);
        } else {
            this.mUtils = new SectionLocaleUtilsBase(this.mLocale);
        }
    }

    public static synchronized SectionLocaleUtils getInstance() {
        SectionLocaleUtils sectionLocaleUtils;
        synchronized (SectionLocaleUtils.class) {
            if (sSingleton == null || !sSingleton.isLocale(Locale.getDefault())) {
                sSingleton = new SectionLocaleUtils(null);
            }
            sectionLocaleUtils = sSingleton;
        }
        return sectionLocaleUtils;
    }

    public static synchronized void setLocale(Locale locale) {
        synchronized (SectionLocaleUtils.class) {
            if (sSingleton == null || !sSingleton.isLocale(locale)) {
                sSingleton = new SectionLocaleUtils(locale);
            }
        }
    }

    public boolean isLocale(Locale locale) {
        return this.mLocale.equals(locale);
    }

    public String getLabel(String displayName) {
        if (!TextUtils.isEmpty(displayName)) {
            char ch;
            if (this.mLocale.getCountry().equals("TW")) {
                ch = getSortKey(displayName).charAt(0);
                if (ch >= '\u3105' && ch <= '\u3129') {
                    return String.valueOf(ch);
                }
            } else if (this.mLocale.getLanguage().equals("ar")) {
                ch = getSortKey(displayName).charAt(0);
                if (ch < '\u0626' && ch > '\u0621') {
                    return "\u0622";
                }
            }
        }
        return getBucketLabel(getBucketIndex(getSortKey(displayName)));
    }

    public int getBucketIndex(String displayName) {
        return this.mUtils.getBucketIndex(displayName);
    }

    public String getBucketLabel(int bucketIndex) {
        return this.mUtils.getBucketLabel(bucketIndex);
    }

    public String getSortKey(String name) {
        return this.mUtils.getSortKey(name);
    }

    public static String getLocaleScript(Locale locale) {
        String[] subtags = ICU.addLikelySubtags(locale.toString().replace("_", "-")).split("_");
        return subtags.length > 1 ? subtags[1] : "";
    }

    public static String getScript() {
        return getLocaleScript(Locale.getDefault());
    }
}

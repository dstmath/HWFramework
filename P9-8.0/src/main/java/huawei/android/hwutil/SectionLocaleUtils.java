package huawei.android.hwutil;

import android.icu.text.AlphabeticIndex;
import android.icu.text.AlphabeticIndex.ImmutableIndex;
import android.rms.iaware.AppTypeInfo;
import android.text.TextUtils;
import java.lang.Character.UnicodeBlock;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import libcore.icu.ICU;

public class SectionLocaleUtils {
    private static final int DEFAULTNUMBERBUCKETINDEX = 0;
    private static final String JAPANESE_LANGUAGE = Locale.JAPANESE.getLanguage();
    public static final Locale LOCALE_ARABIC = new Locale("ar");
    public static final Locale LOCALE_FARSI = new Locale("fa");
    public static final Locale LOCALE_GREEK = new Locale("el");
    public static final Locale LOCALE_HEBREW = new Locale("he");
    public static final Locale LOCALE_HINDI = new Locale("hi");
    public static final Locale LOCALE_THAI = new Locale("th");
    public static final Locale LOCALE_UKRAINIAN = new Locale("uk");
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
            this.mAlphabeticIndex = new AlphabeticIndex(locale).setMaxLabelCount(AppTypeInfo.PG_TYPE_BASE).addLabels(new Locale[]{Locale.ENGLISH}).addLabels(new Locale[]{Locale.JAPANESE}).addLabels(new Locale[]{Locale.KOREAN}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_THAI}).addLabels(new Locale[]{arabicScriptLocale}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_HEBREW}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_GREEK}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_UKRAINIAN}).addLabels(new Locale[]{SectionLocaleUtils.LOCALE_HINDI}).buildImmutableIndex();
            this.mAlphabeticIndexBucketCount = this.mAlphabeticIndex.getBucketCount();
            this.mNumberBucketIndex = this.mAlphabeticIndexBucketCount - 1;
        }

        private boolean isCodePointIllegal(int codePoint) {
            if (Character.isSpaceChar(codePoint) || codePoint == 43 || codePoint == 40 || codePoint == 41 || codePoint == 46 || codePoint == 45) {
                return false;
            }
            return codePoint != 35;
        }

        public int getBucketIndex(String displayName) {
            int codePoint;
            int length = displayName.length();
            int offset = 0;
            while (offset < length) {
                codePoint = Character.codePointAt(displayName, offset);
                if (!Character.isDigit(codePoint)) {
                    if (isCodePointIllegal(codePoint)) {
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
        private static final Set<UnicodeBlock> CJ_BLOCKS;
        private static final String JAPANESE_MISC_LABEL = "他";
        private final int mMiscBucketIndex = super.getBucketIndex("日");

        public JapaneseSectionUtils(Locale locale) {
            super(locale);
        }

        static {
            Set<UnicodeBlock> set = new HashSet();
            set.add(UnicodeBlock.HIRAGANA);
            set.add(UnicodeBlock.KATAKANA);
            set.add(UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS);
            set.add(UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
            set.add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
            set.add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
            set.add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
            set.add(UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
            set.add(UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
            set.add(UnicodeBlock.CJK_COMPATIBILITY);
            set.add(UnicodeBlock.CJK_COMPATIBILITY_FORMS);
            set.add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
            set.add(UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
            CJ_BLOCKS = Collections.unmodifiableSet(set);
        }

        private static boolean isChineseOrJapanese(int codePoint) {
            return CJ_BLOCKS.contains(UnicodeBlock.of(codePoint));
        }

        public int getBucketIndex(String displayName) {
            int bucketIndex = super.getBucketIndex(displayName);
            if ((bucketIndex != this.mMiscBucketIndex || (isChineseOrJapanese(Character.codePointAt(displayName, 0)) ^ 1) == 0) && bucketIndex <= this.mMiscBucketIndex) {
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
            if (sSingleton == null || (sSingleton.isLocale(Locale.getDefault()) ^ 1) != 0) {
                sSingleton = new SectionLocaleUtils(null);
            }
            sectionLocaleUtils = sSingleton;
        }
        return sectionLocaleUtils;
    }

    public static synchronized void setLocale(Locale locale) {
        synchronized (SectionLocaleUtils.class) {
            if (sSingleton == null || (sSingleton.isLocale(locale) ^ 1) != 0) {
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
                if (ch >= 12549 && ch <= 12585) {
                    return String.valueOf(ch);
                }
            } else if (this.mLocale.getLanguage().equals("ar")) {
                ch = getSortKey(displayName).charAt(0);
                if (ch < 1574 && ch > 1569) {
                    return "آ";
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

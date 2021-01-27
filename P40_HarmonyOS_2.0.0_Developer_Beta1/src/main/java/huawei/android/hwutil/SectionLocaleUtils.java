package huawei.android.hwutil;

import android.icu.text.AlphabeticIndex;
import android.text.TextUtils;
import com.huawei.android.os.storage.StorageManagerExt;
import java.lang.Character;
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
        } else if ("zh-Hant-TW".equals(this.mLocale.toLanguageTag())) {
            Locale locale2 = this.mLocale;
            this.mUtils = new SectionLocaleUtilsBase(Locale.forLanguageTag(this.mLocale.toLanguageTag() + "-u-co-zhuyin"));
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
        char ch;
        if (!TextUtils.isEmpty(displayName)) {
            if ("TW".equals(this.mLocale.getCountry())) {
                char ch2 = getSortKey(displayName).charAt(0);
                if (ch2 >= 12549 && ch2 <= 12585) {
                    return String.valueOf(ch2);
                }
            } else if ("ar".equals(this.mLocale.getLanguage()) && (ch = getSortKey(displayName).charAt(0)) < 1574 && ch > 1569) {
                return "آ";
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
        String[] subtags = ICU.addLikelySubtags(locale).toLanguageTag().split("_");
        return subtags.length > 1 ? subtags[1] : StorageManagerExt.INVALID_KEY_DESC;
    }

    public static String getScript() {
        return getLocaleScript(Locale.getDefault());
    }

    /* access modifiers changed from: private */
    public static class SectionLocaleUtilsBase {
        private static final String EMPTY_STRING = "";
        private static final String NUMBER_STRING = "#";
        protected final AlphabeticIndex.ImmutableIndex mAlphabeticIndex;
        private final int mAlphabeticIndexBucketCount;
        private final Locale mCurrentLocale;
        private final int mNumberBucketIndex;

        SectionLocaleUtilsBase(Locale locale) {
            this.mCurrentLocale = locale;
            this.mAlphabeticIndex = new AlphabeticIndex(locale).setMaxLabelCount(300).addLabels(Locale.ENGLISH).addLabels(Locale.JAPANESE).addLabels(Locale.KOREAN).addLabels(SectionLocaleUtils.LOCALE_THAI).addLabels("fa".equals(locale.getLanguage()) ? SectionLocaleUtils.LOCALE_FARSI : SectionLocaleUtils.LOCALE_ARABIC).addLabels(SectionLocaleUtils.LOCALE_HEBREW).addLabels(SectionLocaleUtils.LOCALE_GREEK).addLabels(SectionLocaleUtils.LOCALE_UKRAINIAN).addLabels(SectionLocaleUtils.LOCALE_HINDI).buildImmutableIndex();
            this.mAlphabeticIndexBucketCount = this.mAlphabeticIndex.getBucketCount();
            this.mNumberBucketIndex = this.mAlphabeticIndexBucketCount - 1;
        }

        private boolean isCodePointIllegal(int codePoint) {
            return (Character.isSpaceChar(codePoint) || codePoint == 43 || codePoint == 40 || codePoint == 41 || codePoint == 46 || codePoint == 45 || codePoint == 35) ? false : true;
        }

        public int getBucketIndex(String displayName) {
            int length = displayName.length();
            int offset = 0;
            while (true) {
                if (offset >= length) {
                    break;
                }
                int codePoint = Character.codePointAt(displayName, offset);
                if (Character.isDigit(codePoint)) {
                    break;
                } else if (isCodePointIllegal(codePoint)) {
                    break;
                } else {
                    offset += Character.charCount(codePoint);
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
                int codePoint2 = Character.codePointAt(displayName, offset);
                if (codePoint2 >= 12549 && codePoint2 <= 12573) {
                    return (codePoint2 - 12549) + 1;
                }
                if (codePoint2 >= 12573 && codePoint2 <= 12585) {
                    return (codePoint2 - 12550) + 1;
                }
            }
            return bucket;
        }

        public int getBucketCount() {
            return this.mAlphabeticIndexBucketCount + 1;
        }

        public String getBucketLabel(int bucketIndex) {
            if (bucketIndex < 0 || bucketIndex >= getBucketCount()) {
                return "";
            }
            if (bucketIndex == 0) {
                return "#";
            }
            if (bucketIndex > this.mNumberBucketIndex) {
                bucketIndex--;
            }
            if (this.mAlphabeticIndex.getBucket(bucketIndex) == null) {
                return "";
            }
            return this.mAlphabeticIndex.getBucket(bucketIndex).getLabel();
        }

        public String getSortKey(String name) {
            return name;
        }
    }

    private static class JapaneseSectionUtils extends SectionLocaleUtilsBase {
        private static final Set<Character.UnicodeBlock> CJ_BLOCKS;
        private static final String JAPANESE_MISC_LABEL = "他";
        private final int mMiscBucketIndex = super.getBucketIndex("日");

        JapaneseSectionUtils(Locale locale) {
            super(locale);
        }

        static {
            Set<Character.UnicodeBlock> set = new HashSet<>();
            set.add(Character.UnicodeBlock.HIRAGANA);
            set.add(Character.UnicodeBlock.KATAKANA);
            set.add(Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS);
            set.add(Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS);
            set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
            set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
            set.add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
            set.add(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
            set.add(Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
            set.add(Character.UnicodeBlock.CJK_COMPATIBILITY);
            set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
            set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
            set.add(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
            CJ_BLOCKS = Collections.unmodifiableSet(set);
        }

        private static boolean isChineseOrJapanese(int codePoint) {
            return CJ_BLOCKS.contains(Character.UnicodeBlock.of(codePoint));
        }

        @Override // huawei.android.hwutil.SectionLocaleUtils.SectionLocaleUtilsBase
        public int getBucketIndex(String displayName) {
            int bucketIndex = super.getBucketIndex(displayName);
            if ((bucketIndex != this.mMiscBucketIndex || isChineseOrJapanese(Character.codePointAt(displayName, 0))) && bucketIndex <= this.mMiscBucketIndex) {
                return bucketIndex;
            }
            return bucketIndex + 1;
        }

        @Override // huawei.android.hwutil.SectionLocaleUtils.SectionLocaleUtilsBase
        public int getBucketCount() {
            return super.getBucketCount() + 1;
        }

        @Override // huawei.android.hwutil.SectionLocaleUtils.SectionLocaleUtilsBase
        public String getBucketLabel(int bucketIndex) {
            int i = this.mMiscBucketIndex;
            if (bucketIndex == i) {
                return JAPANESE_MISC_LABEL;
            }
            if (bucketIndex > i) {
                bucketIndex--;
            }
            return super.getBucketLabel(bucketIndex);
        }
    }

    private static class SimplifiedChineseSectionUtils extends SectionLocaleUtilsBase {
        SimplifiedChineseSectionUtils(Locale locale) {
            super(locale);
        }
    }
}

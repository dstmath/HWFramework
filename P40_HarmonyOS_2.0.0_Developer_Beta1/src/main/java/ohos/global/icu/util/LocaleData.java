package ohos.global.icu.util;

import java.util.MissingResourceException;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ULocale;

public final class LocaleData {
    public static final int ALT_QUOTATION_END = 3;
    public static final int ALT_QUOTATION_START = 2;
    @Deprecated
    public static final int DELIMITER_COUNT = 4;
    private static final String[] DELIMITER_TYPES = {"quotationStart", "quotationEnd", "alternateQuotationStart", "alternateQuotationEnd"};
    public static final int ES_AUXILIARY = 1;
    @Deprecated
    public static final int ES_COUNT = 5;
    @Deprecated
    public static final int ES_CURRENCY = 3;
    public static final int ES_INDEX = 2;
    public static final int ES_PUNCTUATION = 4;
    public static final int ES_STANDARD = 0;
    private static final String LOCALE_DISPLAY_PATTERN = "localeDisplayPattern";
    private static final String MEASUREMENT_SYSTEM = "MeasurementSystem";
    private static final String PAPER_SIZE = "PaperSize";
    private static final String PATTERN = "pattern";
    public static final int QUOTATION_END = 1;
    public static final int QUOTATION_START = 0;
    private static final String SEPARATOR = "separator";
    private static VersionInfo gCLDRVersion = null;
    private ICUResourceBundle bundle;
    private ICUResourceBundle langBundle;
    private boolean noSubstitute;

    private LocaleData() {
    }

    public static UnicodeSet getExemplarSet(ULocale uLocale, int i) {
        return getInstance(uLocale).getExemplarSet(i, 0);
    }

    public static UnicodeSet getExemplarSet(ULocale uLocale, int i, int i2) {
        return getInstance(uLocale).getExemplarSet(i, i2);
    }

    public UnicodeSet getExemplarSet(int i, int i2) {
        String[] strArr = {"ExemplarCharacters", "AuxExemplarCharacters", "ExemplarCharactersIndex", "ExemplarCharactersCurrency", "ExemplarCharactersPunctuation"};
        if (i2 != 3) {
            try {
                ICUResourceBundle iCUResourceBundle = this.bundle.get(strArr[i2]);
                if (!this.noSubstitute || this.bundle.isRoot() || !iCUResourceBundle.isRoot()) {
                    return new UnicodeSet(iCUResourceBundle.getString(), i | 1);
                }
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException(e);
            } catch (Exception unused) {
                if (this.noSubstitute) {
                    return null;
                }
                return UnicodeSet.EMPTY;
            }
        } else if (this.noSubstitute) {
            return null;
        } else {
            return UnicodeSet.EMPTY;
        }
    }

    public static final LocaleData getInstance(ULocale uLocale) {
        LocaleData localeData = new LocaleData();
        localeData.bundle = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", uLocale);
        localeData.langBundle = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b/lang", uLocale);
        localeData.noSubstitute = false;
        return localeData;
    }

    public static final LocaleData getInstance() {
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public void setNoSubstitute(boolean z) {
        this.noSubstitute = z;
    }

    public boolean getNoSubstitute() {
        return this.noSubstitute;
    }

    public String getDelimiter(int i) {
        ICUResourceBundle withFallback = this.bundle.get("delimiters").getWithFallback(DELIMITER_TYPES[i]);
        if (!this.noSubstitute || this.bundle.isRoot() || !withFallback.isRoot()) {
            return withFallback.getString();
        }
        return null;
    }

    private static UResourceBundle measurementTypeBundleForLocale(ULocale uLocale, String str) {
        String regionForSupplementalData = ULocale.getRegionForSupplementalData(uLocale, true);
        try {
            UResourceBundle uResourceBundle = UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("measurementData");
            try {
                return uResourceBundle.get(regionForSupplementalData).get(str);
            } catch (MissingResourceException unused) {
                return uResourceBundle.get("001").get(str);
            }
        } catch (MissingResourceException unused2) {
            return null;
        }
    }

    public static final class MeasurementSystem {
        public static final MeasurementSystem SI = new MeasurementSystem();
        public static final MeasurementSystem UK = new MeasurementSystem();
        public static final MeasurementSystem US = new MeasurementSystem();

        private MeasurementSystem() {
        }
    }

    public static final MeasurementSystem getMeasurementSystem(ULocale uLocale) {
        int i = measurementTypeBundleForLocale(uLocale, MEASUREMENT_SYSTEM).getInt();
        if (i == 0) {
            return MeasurementSystem.SI;
        }
        if (i == 1) {
            return MeasurementSystem.US;
        }
        if (i != 2) {
            return null;
        }
        return MeasurementSystem.UK;
    }

    public static final class PaperSize {
        private int height;
        private int width;

        private PaperSize(int i, int i2) {
            this.height = i;
            this.width = i2;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }
    }

    public static final PaperSize getPaperSize(ULocale uLocale) {
        int[] intVector = measurementTypeBundleForLocale(uLocale, PAPER_SIZE).getIntVector();
        return new PaperSize(intVector[0], intVector[1]);
    }

    public String getLocaleDisplayPattern() {
        return this.langBundle.get(LOCALE_DISPLAY_PATTERN).getStringWithFallback(PATTERN);
    }

    public String getLocaleSeparator() {
        String stringWithFallback = this.langBundle.get(LOCALE_DISPLAY_PATTERN).getStringWithFallback(SEPARATOR);
        int indexOf = stringWithFallback.indexOf("{0}");
        int indexOf2 = stringWithFallback.indexOf("{1}");
        return (indexOf < 0 || indexOf2 < 0 || indexOf > indexOf2) ? stringWithFallback : stringWithFallback.substring(indexOf + 3, indexOf2);
    }

    public static VersionInfo getCLDRVersion() {
        if (gCLDRVersion == null) {
            gCLDRVersion = VersionInfo.getInstance(UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("cldrVersion").getString());
        }
        return gCLDRVersion;
    }
}

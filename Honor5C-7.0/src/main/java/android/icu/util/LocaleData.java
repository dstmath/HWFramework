package android.icu.util;

import android.icu.impl.ICUResourceBundle;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale.Category;
import java.util.MissingResourceException;

public final class LocaleData {
    public static final int ALT_QUOTATION_END = 3;
    public static final int ALT_QUOTATION_START = 2;
    public static final int DELIMITER_COUNT = 4;
    private static final String[] DELIMITER_TYPES = null;
    public static final int ES_AUXILIARY = 1;
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
    private static VersionInfo gCLDRVersion;
    private ICUResourceBundle bundle;
    private ICUResourceBundle langBundle;
    private boolean noSubstitute;

    public static final class MeasurementSystem {
        public static final MeasurementSystem SI = null;
        public static final MeasurementSystem UK = null;
        public static final MeasurementSystem US = null;
        private int systemID;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.LocaleData.MeasurementSystem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.LocaleData.MeasurementSystem.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.LocaleData.MeasurementSystem.<clinit>():void");
        }

        private MeasurementSystem(int id) {
            this.systemID = id;
        }

        private boolean equals(int id) {
            return this.systemID == id;
        }
    }

    public static final class PaperSize {
        private int height;
        private int width;

        /* synthetic */ PaperSize(int h, int w, PaperSize paperSize) {
            this(h, w);
        }

        private PaperSize(int h, int w) {
            this.height = h;
            this.width = w;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.LocaleData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.LocaleData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.LocaleData.<clinit>():void");
    }

    private LocaleData() {
    }

    public static UnicodeSet getExemplarSet(ULocale locale, int options) {
        return getInstance(locale).getExemplarSet(options, (int) QUOTATION_START);
    }

    public static UnicodeSet getExemplarSet(ULocale locale, int options, int extype) {
        return getInstance(locale).getExemplarSet(options, extype);
    }

    public UnicodeSet getExemplarSet(int options, int extype) {
        UnicodeSet unicodeSet = null;
        String[] exemplarSetTypes = new String[ES_COUNT];
        exemplarSetTypes[QUOTATION_START] = "ExemplarCharacters";
        exemplarSetTypes[QUOTATION_END] = "AuxExemplarCharacters";
        exemplarSetTypes[ES_INDEX] = "ExemplarCharactersIndex";
        exemplarSetTypes[ES_CURRENCY] = "ExemplarCharactersCurrency";
        exemplarSetTypes[ES_PUNCTUATION] = "ExemplarCharactersPunctuation";
        if (extype == ES_CURRENCY) {
            if (!this.noSubstitute) {
                unicodeSet = UnicodeSet.EMPTY;
            }
            return unicodeSet;
        }
        try {
            ICUResourceBundle stringBundle = (ICUResourceBundle) this.bundle.get(exemplarSetTypes[extype]);
            if (this.noSubstitute && stringBundle.getLoadingStatus() == ES_INDEX) {
                return null;
            }
            return new UnicodeSet(stringBundle.getString(), options | QUOTATION_END);
        } catch (ArrayIndexOutOfBoundsException aiooe) {
            throw new IllegalArgumentException(aiooe);
        } catch (Exception e) {
            if (!this.noSubstitute) {
                unicodeSet = UnicodeSet.EMPTY;
            }
            return unicodeSet;
        }
    }

    public static final LocaleData getInstance(ULocale locale) {
        LocaleData ld = new LocaleData();
        ld.bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale);
        ld.langBundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_LANG_BASE_NAME, locale);
        ld.noSubstitute = false;
        return ld;
    }

    public static final LocaleData getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    public void setNoSubstitute(boolean setting) {
        this.noSubstitute = setting;
    }

    public boolean getNoSubstitute() {
        return this.noSubstitute;
    }

    public String getDelimiter(int type) {
        ICUResourceBundle stringBundle = ((ICUResourceBundle) this.bundle.get("delimiters")).getWithFallback(DELIMITER_TYPES[type]);
        if (this.noSubstitute && stringBundle.getLoadingStatus() == ES_INDEX) {
            return null;
        }
        return stringBundle.getString();
    }

    private static UResourceBundle measurementTypeBundleForLocale(ULocale locale, String measurementType) {
        UResourceBundle measTypeBundle = null;
        String region = ULocale.addLikelySubtags(locale).getCountry();
        try {
            UResourceBundle measurementData = UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("measurementData");
            try {
                measTypeBundle = measurementData.get(region).get(measurementType);
            } catch (MissingResourceException e) {
                measTypeBundle = measurementData.get("001").get(measurementType);
            }
        } catch (MissingResourceException e2) {
        }
        return measTypeBundle;
    }

    public static final MeasurementSystem getMeasurementSystem(ULocale locale) {
        int system = measurementTypeBundleForLocale(locale, MEASUREMENT_SYSTEM).getInt();
        if (MeasurementSystem.US.equals(system)) {
            return MeasurementSystem.US;
        }
        if (MeasurementSystem.UK.equals(system)) {
            return MeasurementSystem.UK;
        }
        if (MeasurementSystem.SI.equals(system)) {
            return MeasurementSystem.SI;
        }
        return null;
    }

    public static final PaperSize getPaperSize(ULocale locale) {
        int[] size = measurementTypeBundleForLocale(locale, PAPER_SIZE).getIntVector();
        return new PaperSize(size[QUOTATION_START], size[QUOTATION_END], null);
    }

    public String getLocaleDisplayPattern() {
        return ((ICUResourceBundle) this.langBundle.get(LOCALE_DISPLAY_PATTERN)).getStringWithFallback(PATTERN);
    }

    public String getLocaleSeparator() {
        String sub0 = "{0}";
        String localeSeparator = ((ICUResourceBundle) this.langBundle.get(LOCALE_DISPLAY_PATTERN)).getStringWithFallback(SEPARATOR);
        int index0 = localeSeparator.indexOf(sub0);
        int index1 = localeSeparator.indexOf("{1}");
        if (index0 < 0 || index1 < 0 || index0 > index1) {
            return localeSeparator;
        }
        return localeSeparator.substring(sub0.length() + index0, index1);
    }

    public static VersionInfo getCLDRVersion() {
        if (gCLDRVersion == null) {
            gCLDRVersion = VersionInfo.getInstance(UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER).get("cldrVersion").getString());
        }
        return gCLDRVersion;
    }
}

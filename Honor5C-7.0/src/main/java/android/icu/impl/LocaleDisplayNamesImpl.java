package android.icu.impl;

import android.icu.impl.CurrencyData.CurrencyDisplayInfo;
import android.icu.impl.coll.CollationSettings;
import android.icu.impl.locale.AsciiUtil;
import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.BreakIterator;
import android.icu.text.DisplayContext;
import android.icu.text.DisplayContext.Type;
import android.icu.text.LocaleDisplayNames;
import android.icu.text.LocaleDisplayNames.DialectHandling;
import android.icu.text.LocaleDisplayNames.UiListItem;
import android.icu.text.MessageFormat;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Builder;
import android.icu.util.ULocale.Minimize;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class LocaleDisplayNamesImpl extends LocaleDisplayNames {
    private static final /* synthetic */ int[] -android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-text-DisplayContext$TypeSwitchesValues = null;
    private static final Cache cache = null;
    private static final Map<String, CapitalizationContextUsage> contextUsageTypeMap = null;
    private final DisplayContext capitalization;
    private transient BreakIterator capitalizationBrkIter;
    private boolean[] capitalizationUsage;
    private final CurrencyDisplayInfo currencyDisplayInfo;
    private final DialectHandling dialectHandling;
    private final MessageFormat format;
    private final char formatCloseParen;
    private final char formatOpenParen;
    private final char formatReplaceCloseParen;
    private final char formatReplaceOpenParen;
    private final MessageFormat keyTypeFormat;
    private final DataTable langData;
    private final ULocale locale;
    private final DisplayContext nameLength;
    private final DataTable regionData;
    private final MessageFormat separatorFormat;

    static abstract class DataTables {

        /* renamed from: android.icu.impl.LocaleDisplayNamesImpl.DataTables.1 */
        static class AnonymousClass1 extends DataTables {
            final /* synthetic */ DataTable val$NO_OP;

            AnonymousClass1(DataTable val$NO_OP) {
                this.val$NO_OP = val$NO_OP;
            }

            public DataTable get(ULocale locale) {
                return this.val$NO_OP;
            }
        }

        public abstract DataTable get(ULocale uLocale);

        DataTables() {
        }

        public static DataTables load(String className) {
            try {
                return (DataTables) Class.forName(className).newInstance();
            } catch (Throwable th) {
                return new AnonymousClass1(new DataTable());
            }
        }
    }

    static abstract class ICUDataTables extends DataTables {
        private final String path;

        protected ICUDataTables(String path) {
            this.path = path;
        }

        public DataTable get(ULocale locale) {
            return new ICUDataTable(this.path, locale);
        }
    }

    private static class Cache {
        private static final /* synthetic */ int[] -android-icu-text-DisplayContext$TypeSwitchesValues = null;
        private LocaleDisplayNames cache;
        private DisplayContext capitalization;
        private DialectHandling dialectHandling;
        private ULocale locale;
        private DisplayContext nameLength;

        private static /* synthetic */ int[] -getandroid-icu-text-DisplayContext$TypeSwitchesValues() {
            if (-android-icu-text-DisplayContext$TypeSwitchesValues != null) {
                return -android-icu-text-DisplayContext$TypeSwitchesValues;
            }
            int[] iArr = new int[Type.values().length];
            try {
                iArr[Type.CAPITALIZATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Type.DIALECT_HANDLING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Type.DISPLAY_LENGTH.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -android-icu-text-DisplayContext$TypeSwitchesValues = iArr;
            return iArr;
        }

        private Cache() {
        }

        public LocaleDisplayNames get(ULocale locale, DialectHandling dialectHandling) {
            boolean equals;
            if (dialectHandling == this.dialectHandling && DisplayContext.CAPITALIZATION_NONE == this.capitalization && DisplayContext.LENGTH_FULL == this.nameLength) {
                equals = locale.equals(this.locale);
            } else {
                equals = false;
            }
            if (!equals) {
                this.locale = locale;
                this.dialectHandling = dialectHandling;
                this.capitalization = DisplayContext.CAPITALIZATION_NONE;
                this.nameLength = DisplayContext.LENGTH_FULL;
                this.cache = new LocaleDisplayNamesImpl(locale, dialectHandling);
            }
            return this.cache;
        }

        public LocaleDisplayNames get(ULocale locale, DisplayContext... contexts) {
            boolean z = false;
            DialectHandling dialectHandlingIn = DialectHandling.STANDARD_NAMES;
            DisplayContext capitalizationIn = DisplayContext.CAPITALIZATION_NONE;
            DisplayContext nameLengthIn = DisplayContext.LENGTH_FULL;
            for (DisplayContext contextItem : contexts) {
                switch (-getandroid-icu-text-DisplayContext$TypeSwitchesValues()[contextItem.type().ordinal()]) {
                    case NodeFilter.SHOW_ELEMENT /*1*/:
                        capitalizationIn = contextItem;
                        break;
                    case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                        if (contextItem.value() != DisplayContext.STANDARD_NAMES.value()) {
                            dialectHandlingIn = DialectHandling.DIALECT_NAMES;
                            break;
                        }
                        dialectHandlingIn = DialectHandling.STANDARD_NAMES;
                        break;
                    case XmlPullParser.END_TAG /*3*/:
                        nameLengthIn = contextItem;
                        break;
                    default:
                        break;
                }
            }
            if (dialectHandlingIn == this.dialectHandling && capitalizationIn == this.capitalization && nameLengthIn == this.nameLength) {
                z = locale.equals(this.locale);
            }
            if (!z) {
                this.locale = locale;
                this.dialectHandling = dialectHandlingIn;
                this.capitalization = capitalizationIn;
                this.nameLength = nameLengthIn;
                this.cache = new LocaleDisplayNamesImpl(locale, contexts);
            }
            return this.cache;
        }
    }

    private enum CapitalizationContextUsage {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.LocaleDisplayNamesImpl.CapitalizationContextUsage.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.LocaleDisplayNamesImpl.CapitalizationContextUsage.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleDisplayNamesImpl.CapitalizationContextUsage.<clinit>():void");
        }
    }

    public static class DataTable {
        public DataTable() {
        }

        ULocale getLocale() {
            return ULocale.ROOT;
        }

        String get(String tableName, String code) {
            return get(tableName, null, code);
        }

        String get(String tableName, String subTableName, String code) {
            return code;
        }
    }

    public enum DataTableType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.LocaleDisplayNamesImpl.DataTableType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.LocaleDisplayNamesImpl.DataTableType.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleDisplayNamesImpl.DataTableType.<clinit>():void");
        }
    }

    static class ICUDataTable extends DataTable {
        private final ICUResourceBundle bundle;

        public ICUDataTable(String path, ULocale locale) {
            this.bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(path, locale.getBaseName());
        }

        public ULocale getLocale() {
            return this.bundle.getULocale();
        }

        public String get(String tableName, String subTableName, String code) {
            return ICUResourceTableAccess.getTableString(this.bundle, tableName, subTableName, code);
        }
    }

    static class LangDataTables {
        static final DataTables impl = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.LocaleDisplayNamesImpl.LangDataTables.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.LocaleDisplayNamesImpl.LangDataTables.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleDisplayNamesImpl.LangDataTables.<clinit>():void");
        }

        LangDataTables() {
        }
    }

    static class RegionDataTables {
        static final DataTables impl = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.LocaleDisplayNamesImpl.RegionDataTables.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.LocaleDisplayNamesImpl.RegionDataTables.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleDisplayNamesImpl.RegionDataTables.<clinit>():void");
        }

        RegionDataTables() {
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues() {
        if (-android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues != null) {
            return -android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues;
        }
        int[] iArr = new int[DataTableType.values().length];
        try {
            iArr[DataTableType.LANG.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DataTableType.REGION.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        -android-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-text-DisplayContext$TypeSwitchesValues() {
        if (-android-icu-text-DisplayContext$TypeSwitchesValues != null) {
            return -android-icu-text-DisplayContext$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.CAPITALIZATION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.DIALECT_HANDLING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.DISPLAY_LENGTH.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-icu-text-DisplayContext$TypeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.LocaleDisplayNamesImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.LocaleDisplayNamesImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.LocaleDisplayNamesImpl.<clinit>():void");
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DialectHandling dialectHandling) {
        LocaleDisplayNames localeDisplayNames;
        synchronized (cache) {
            localeDisplayNames = cache.get(locale, dialectHandling);
        }
        return localeDisplayNames;
    }

    public static LocaleDisplayNames getInstance(ULocale locale, DisplayContext... contexts) {
        LocaleDisplayNames localeDisplayNames;
        synchronized (cache) {
            localeDisplayNames = cache.get(locale, contexts);
        }
        return localeDisplayNames;
    }

    public LocaleDisplayNamesImpl(ULocale locale, DialectHandling dialectHandling) {
        DisplayContext[] displayContextArr = new DisplayContext[2];
        displayContextArr[0] = dialectHandling == DialectHandling.STANDARD_NAMES ? DisplayContext.STANDARD_NAMES : DisplayContext.DIALECT_NAMES;
        displayContextArr[1] = DisplayContext.CAPITALIZATION_NONE;
        this(locale, displayContextArr);
    }

    public LocaleDisplayNamesImpl(ULocale locale, DisplayContext... contexts) {
        int length;
        ULocale locale2;
        this.capitalizationUsage = null;
        this.capitalizationBrkIter = null;
        DialectHandling dialectHandling = DialectHandling.STANDARD_NAMES;
        DisplayContext capitalization = DisplayContext.CAPITALIZATION_NONE;
        DisplayContext nameLength = DisplayContext.LENGTH_FULL;
        for (DisplayContext contextItem : contexts) {
            switch (-getandroid-icu-text-DisplayContext$TypeSwitchesValues()[contextItem.type().ordinal()]) {
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    capitalization = contextItem;
                    break;
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    if (contextItem.value() != DisplayContext.STANDARD_NAMES.value()) {
                        dialectHandling = DialectHandling.DIALECT_NAMES;
                        break;
                    } else {
                        dialectHandling = DialectHandling.STANDARD_NAMES;
                        break;
                    }
                case XmlPullParser.END_TAG /*3*/:
                    nameLength = contextItem;
                    break;
                default:
                    break;
            }
        }
        this.dialectHandling = dialectHandling;
        this.capitalization = capitalization;
        this.nameLength = nameLength;
        this.langData = LangDataTables.impl.get(locale);
        this.regionData = RegionDataTables.impl.get(locale);
        if (ULocale.ROOT.equals(this.langData.getLocale())) {
            locale2 = this.regionData.getLocale();
        } else {
            locale2 = this.langData.getLocale();
        }
        this.locale = locale2;
        String sep = this.langData.get("localeDisplayPattern", "separator");
        if ("separator".equals(sep)) {
            sep = "{0}, {1}";
        }
        this.separatorFormat = new MessageFormat(sep);
        String pattern = this.langData.get("localeDisplayPattern", "pattern");
        if ("pattern".equals(pattern)) {
            pattern = "{0} ({1})";
        }
        this.format = new MessageFormat(pattern);
        if (pattern.contains("\uff08")) {
            this.formatOpenParen = '\uff08';
            this.formatCloseParen = '\uff09';
            this.formatReplaceOpenParen = '\uff3b';
            this.formatReplaceCloseParen = '\uff3d';
        } else {
            this.formatOpenParen = '(';
            this.formatCloseParen = ')';
            this.formatReplaceOpenParen = '[';
            this.formatReplaceCloseParen = ']';
        }
        String keyTypePattern = this.langData.get("localeDisplayPattern", "keyTypePattern");
        if ("keyTypePattern".equals(keyTypePattern)) {
            keyTypePattern = "{0}={1}";
        }
        this.keyTypeFormat = new MessageFormat(keyTypePattern);
        boolean needBrkIter = false;
        if (capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || capitalization == DisplayContext.CAPITALIZATION_FOR_STANDALONE) {
            UResourceBundle contextTransformsBundle;
            this.capitalizationUsage = new boolean[CapitalizationContextUsage.values().length];
            try {
                contextTransformsBundle = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, locale)).getWithFallback("contextTransforms");
            } catch (MissingResourceException e) {
                contextTransformsBundle = null;
            }
            if (contextTransformsBundle != null) {
                UResourceBundleIterator ctIterator = contextTransformsBundle.getIterator();
                while (ctIterator.hasNext()) {
                    UResourceBundle contextTransformUsage = ctIterator.next();
                    int[] intVector = contextTransformUsage.getIntVector();
                    length = intVector.length;
                    if (r0 >= 2) {
                        CapitalizationContextUsage usage = (CapitalizationContextUsage) contextUsageTypeMap.get(contextTransformUsage.getKey());
                        if (usage != null) {
                            int titlecaseInt;
                            if (capitalization == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU) {
                                titlecaseInt = intVector[0];
                            } else {
                                titlecaseInt = intVector[1];
                            }
                            if (titlecaseInt != 0) {
                                this.capitalizationUsage[usage.ordinal()] = true;
                                needBrkIter = true;
                            }
                        }
                    }
                }
            }
        }
        if (needBrkIter || capitalization == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE) {
            this.capitalizationBrkIter = BreakIterator.getSentenceInstance(locale);
        }
        this.currencyDisplayInfo = CurrencyData.provider.getInstance(locale, false);
    }

    public ULocale getLocale() {
        return this.locale;
    }

    public DialectHandling getDialectHandling() {
        return this.dialectHandling;
    }

    public DisplayContext getContext(Type type) {
        switch (-getandroid-icu-text-DisplayContext$TypeSwitchesValues()[type.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return this.capitalization;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return this.dialectHandling == DialectHandling.STANDARD_NAMES ? DisplayContext.STANDARD_NAMES : DisplayContext.DIALECT_NAMES;
            case XmlPullParser.END_TAG /*3*/:
                return this.nameLength;
            default:
                return DisplayContext.STANDARD_NAMES;
        }
    }

    private String adjustForUsageAndContext(CapitalizationContextUsage usage, String name) {
        if (name == null || name.length() <= 0 || !UCharacter.isLowerCase(name.codePointAt(0)) || (this.capitalization != DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE && (this.capitalizationUsage == null || !this.capitalizationUsage[usage.ordinal()]))) {
            return name;
        }
        String toTitleCase;
        synchronized (this) {
            if (this.capitalizationBrkIter == null) {
                this.capitalizationBrkIter = BreakIterator.getSentenceInstance(this.locale);
            }
            toTitleCase = UCharacter.toTitleCase(this.locale, name, this.capitalizationBrkIter, (int) CollationSettings.CASE_FIRST_AND_UPPER_MASK);
        }
        return toTitleCase;
    }

    public String localeDisplayName(ULocale locale) {
        return localeDisplayNameInternal(locale);
    }

    public String localeDisplayName(Locale locale) {
        return localeDisplayNameInternal(ULocale.forLocale(locale));
    }

    public String localeDisplayName(String localeId) {
        return localeDisplayNameInternal(new ULocale(localeId));
    }

    private String localeDisplayNameInternal(ULocale locale) {
        String str = null;
        String lang = locale.getLanguage();
        if (locale.getBaseName().length() == 0) {
            lang = "root";
        }
        String script = locale.getScript();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        boolean hasScript = script.length() > 0;
        boolean hasCountry = country.length() > 0;
        boolean hasVariant = variant.length() > 0;
        if (this.dialectHandling == DialectHandling.DIALECT_NAMES) {
            String result;
            if (hasScript && hasCountry) {
                String langScriptCountry = lang + '_' + script + '_' + country;
                result = localeIdName(langScriptCountry);
                if (!result.equals(langScriptCountry)) {
                    str = result;
                    hasScript = false;
                    hasCountry = false;
                }
            }
            if (hasScript) {
                String langScript = lang + '_' + script;
                result = localeIdName(langScript);
                if (!result.equals(langScript)) {
                    str = result;
                    hasScript = false;
                }
            }
            if (hasCountry) {
                String langCountry = lang + '_' + country;
                result = localeIdName(langCountry);
                if (!result.equals(langCountry)) {
                    str = result;
                    hasCountry = false;
                }
            }
        }
        if (str == null) {
            str = localeIdName(lang).replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
        }
        StringBuilder buf = new StringBuilder();
        if (hasScript) {
            buf.append(scriptDisplayNameInContext(script).replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen));
        }
        if (hasCountry) {
            appendWithSep(regionDisplayName(country).replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen), buf);
        }
        if (hasVariant) {
            appendWithSep(variantDisplayName(variant).replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen), buf);
        }
        Iterator<String> keys = locale.getKeywords();
        if (keys != null) {
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = locale.getKeywordValue(key);
                String keyDisplayName = keyDisplayName(key).replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                String valueDisplayName = keyValueDisplayName(key, value).replace(this.formatOpenParen, this.formatReplaceOpenParen).replace(this.formatCloseParen, this.formatReplaceCloseParen);
                if (!valueDisplayName.equals(value)) {
                    appendWithSep(valueDisplayName, buf);
                } else if (key.equals(keyDisplayName)) {
                    appendWithSep(keyDisplayName, buf).append("=").append(valueDisplayName);
                } else {
                    appendWithSep(this.keyTypeFormat.format(new String[]{keyDisplayName, valueDisplayName}), buf);
                }
            }
        }
        String resultRemainder = null;
        if (buf.length() > 0) {
            resultRemainder = buf.toString();
        }
        if (resultRemainder != null) {
            str = this.format.format(new Object[]{str, resultRemainder});
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, str);
    }

    private String localeIdName(String localeId) {
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            String locIdName = this.langData.get("Languages%short", localeId);
            if (!locIdName.equals(localeId)) {
                return locIdName;
            }
        }
        return this.langData.get("Languages", localeId);
    }

    public String languageDisplayName(String lang) {
        if (lang.equals("root") || lang.indexOf(95) != -1) {
            return lang;
        }
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            String langName = this.langData.get("Languages%short", lang);
            if (!langName.equals(lang)) {
                return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, langName);
            }
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.LANGUAGE, this.langData.get("Languages", lang));
    }

    public String scriptDisplayName(String script) {
        String str = this.langData.get("Scripts%stand-alone", script);
        if (str.equals(script)) {
            if (this.nameLength == DisplayContext.LENGTH_SHORT) {
                str = this.langData.get("Scripts%short", script);
                if (!str.equals(script)) {
                    return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str);
                }
            }
            str = this.langData.get("Scripts", script);
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, str);
    }

    public String scriptDisplayNameInContext(String script) {
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            String scriptName = this.langData.get("Scripts%short", script);
            if (!scriptName.equals(script)) {
                return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, scriptName);
            }
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.SCRIPT, this.langData.get("Scripts", script));
    }

    public String scriptDisplayName(int scriptCode) {
        return scriptDisplayName(UScript.getShortName(scriptCode));
    }

    public String regionDisplayName(String region) {
        if (this.nameLength == DisplayContext.LENGTH_SHORT) {
            String regionName = this.regionData.get("Countries%short", region);
            if (!regionName.equals(region)) {
                return adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, regionName);
            }
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.TERRITORY, this.regionData.get("Countries", region));
    }

    public String variantDisplayName(String variant) {
        return adjustForUsageAndContext(CapitalizationContextUsage.VARIANT, this.langData.get("Variants", variant));
    }

    public String keyDisplayName(String key) {
        return adjustForUsageAndContext(CapitalizationContextUsage.KEY, this.langData.get("Keys", key));
    }

    public String keyValueDisplayName(String key, String value) {
        String keyValueName = null;
        if (key.equals("currency")) {
            keyValueName = this.currencyDisplayInfo.getName(AsciiUtil.toUpperString(value));
            if (keyValueName == null) {
                keyValueName = value;
            }
        } else {
            if (this.nameLength == DisplayContext.LENGTH_SHORT) {
                String tmp = this.langData.get("Types%short", key, value);
                if (!tmp.equals(value)) {
                    keyValueName = tmp;
                }
            }
            if (keyValueName == null) {
                keyValueName = this.langData.get("Types", key, value);
            }
        }
        return adjustForUsageAndContext(CapitalizationContextUsage.KEYVALUE, keyValueName);
    }

    public List<UiListItem> getUiListCompareWholeItems(Set<ULocale> localeSet, Comparator<UiListItem> comparator) {
        DisplayContext capContext = getContext(Type.CAPITALIZATION);
        List<UiListItem> result = new ArrayList();
        Map<ULocale, Set<ULocale>> baseToLocales = new HashMap();
        Builder builder = new Builder();
        for (ULocale locOriginal : localeSet) {
            builder.setLocale(locOriginal);
            ULocale loc = ULocale.addLikelySubtags(locOriginal);
            ULocale base = new ULocale(loc.getLanguage());
            Set<ULocale> locales = (Set) baseToLocales.get(base);
            if (locales == null) {
                locales = new HashSet();
                baseToLocales.put(base, locales);
            }
            locales.add(loc);
        }
        for (Entry<ULocale, Set<ULocale>> entry : baseToLocales.entrySet()) {
            base = (ULocale) entry.getKey();
            Set<ULocale> values = (Set) entry.getValue();
            if (values.size() == 1) {
                result.add(newRow(ULocale.minimizeSubtags((ULocale) values.iterator().next(), Minimize.FAVOR_SCRIPT), capContext));
            } else {
                Set<String> scripts = new HashSet();
                Set<String> regions = new HashSet();
                ULocale maxBase = ULocale.addLikelySubtags(base);
                scripts.add(maxBase.getScript());
                regions.add(maxBase.getCountry());
                for (ULocale locale : values) {
                    scripts.add(locale.getScript());
                    regions.add(locale.getCountry());
                }
                boolean hasScripts = scripts.size() > 1;
                boolean hasRegions = regions.size() > 1;
                for (ULocale locale2 : values) {
                    Builder modified = builder.setLocale(locale2);
                    if (!hasScripts) {
                        modified.setScript(XmlPullParser.NO_NAMESPACE);
                    }
                    if (!hasRegions) {
                        modified.setRegion(XmlPullParser.NO_NAMESPACE);
                    }
                    result.add(newRow(modified.build(), capContext));
                }
            }
        }
        Collections.sort(result, comparator);
        return result;
    }

    private UiListItem newRow(ULocale modified, DisplayContext capContext) {
        ULocale minimized = ULocale.minimizeSubtags(modified, Minimize.FAVOR_SCRIPT);
        String tempName = modified.getDisplayName(this.locale);
        String nameInDisplayLocale = capContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? UCharacter.toTitleFirst(this.locale, tempName) : tempName;
        tempName = modified.getDisplayName(modified);
        return new UiListItem(minimized, modified, nameInDisplayLocale, capContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU ? UCharacter.toTitleFirst(modified, tempName) : tempName);
    }

    public static boolean haveData(DataTableType type) {
        switch (-getandroid-icu-impl-LocaleDisplayNamesImpl$DataTableTypeSwitchesValues()[type.ordinal()]) {
            case NodeFilter.SHOW_ELEMENT /*1*/:
                return LangDataTables.impl instanceof ICUDataTables;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                return RegionDataTables.impl instanceof ICUDataTables;
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    private StringBuilder appendWithSep(String s, StringBuilder b) {
        if (b.length() == 0) {
            b.append(s);
        } else {
            b.replace(0, b.length(), this.separatorFormat.format(new String[]{b.toString(), s}));
        }
        return b;
    }
}

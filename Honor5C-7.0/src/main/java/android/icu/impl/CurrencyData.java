package android.icu.impl;

import android.icu.text.CurrencyDisplayNames;
import android.icu.util.ULocale;
import java.util.Collections;
import java.util.Map;

public class CurrencyData {
    public static final CurrencyDisplayInfoProvider provider = null;

    public interface CurrencyDisplayInfoProvider {
        CurrencyDisplayInfo getInstance(ULocale uLocale, boolean z);

        boolean hasData();
    }

    public static abstract class CurrencyDisplayInfo extends CurrencyDisplayNames {
        public abstract CurrencyFormatInfo getFormatInfo(String str);

        public abstract CurrencySpacingInfo getSpacingInfo();

        public abstract Map<String, String> getUnitPatterns();
    }

    public static final class CurrencyFormatInfo {
        public final String currencyPattern;
        public final char monetaryGroupingSeparator;
        public final char monetarySeparator;

        public CurrencyFormatInfo(String currencyPattern, char monetarySeparator, char monetaryGroupingSeparator) {
            this.currencyPattern = currencyPattern;
            this.monetarySeparator = monetarySeparator;
            this.monetaryGroupingSeparator = monetaryGroupingSeparator;
        }
    }

    public static final class CurrencySpacingInfo {
        public static final CurrencySpacingInfo DEFAULT = null;
        private static final String DEFAULT_CTX_MATCH = "[:digit:]";
        private static final String DEFAULT_CUR_MATCH = "[:letter:]";
        private static final String DEFAULT_INSERT = " ";
        public final String afterContextMatch;
        public final String afterCurrencyMatch;
        public final String afterInsert;
        public final String beforeContextMatch;
        public final String beforeCurrencyMatch;
        public final String beforeInsert;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CurrencyData.CurrencySpacingInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CurrencyData.CurrencySpacingInfo.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CurrencyData.CurrencySpacingInfo.<clinit>():void");
        }

        public CurrencySpacingInfo(String beforeCurrencyMatch, String beforeContextMatch, String beforeInsert, String afterCurrencyMatch, String afterContextMatch, String afterInsert) {
            this.beforeCurrencyMatch = beforeCurrencyMatch;
            this.beforeContextMatch = beforeContextMatch;
            this.beforeInsert = beforeInsert;
            this.afterCurrencyMatch = afterCurrencyMatch;
            this.afterContextMatch = afterContextMatch;
            this.afterInsert = afterInsert;
        }
    }

    public static class DefaultInfo extends CurrencyDisplayInfo {
        private static final CurrencyDisplayInfo FALLBACK_INSTANCE = null;
        private static final CurrencyDisplayInfo NO_FALLBACK_INSTANCE = null;
        private final boolean fallback;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CurrencyData.DefaultInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CurrencyData.DefaultInfo.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CurrencyData.DefaultInfo.<clinit>():void");
        }

        private DefaultInfo(boolean fallback) {
            this.fallback = fallback;
        }

        public static final CurrencyDisplayInfo getWithFallback(boolean fallback) {
            return fallback ? FALLBACK_INSTANCE : NO_FALLBACK_INSTANCE;
        }

        public String getName(String isoCode) {
            return this.fallback ? isoCode : null;
        }

        public String getPluralName(String isoCode, String pluralType) {
            return this.fallback ? isoCode : null;
        }

        public String getSymbol(String isoCode) {
            return this.fallback ? isoCode : null;
        }

        public Map<String, String> symbolMap() {
            return Collections.emptyMap();
        }

        public Map<String, String> nameMap() {
            return Collections.emptyMap();
        }

        public ULocale getULocale() {
            return ULocale.ROOT;
        }

        public Map<String, String> getUnitPatterns() {
            if (this.fallback) {
                return Collections.emptyMap();
            }
            return null;
        }

        public CurrencyFormatInfo getFormatInfo(String isoCode) {
            return null;
        }

        public CurrencySpacingInfo getSpacingInfo() {
            return this.fallback ? CurrencySpacingInfo.DEFAULT : null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CurrencyData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CurrencyData.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CurrencyData.<clinit>():void");
    }

    public CurrencyData() {
    }
}

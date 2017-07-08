package android.icu.impl.duration.impl;

import android.icu.text.PluralRules;
import java.util.ArrayList;
import java.util.List;

public class DataRecord {
    boolean allowZero;
    String countSep;
    byte decimalHandling;
    char decimalSep;
    String digitPrefix;
    String fifteenMinutes;
    String fiveMinutes;
    byte fractionHandling;
    byte[] genders;
    String[] halfNames;
    byte[] halfPlacements;
    byte[] halfSupport;
    String[] halves;
    String[] measures;
    String[] mediumNames;
    String[] numberNames;
    byte numberSystem;
    boolean omitDualCount;
    boolean omitSingularCount;
    String[] optSuffixes;
    byte pl;
    String[][] pluralNames;
    boolean requiresDigitSeparator;
    boolean[] requiresSkipMarker;
    String[] rqdSuffixes;
    ScopeData[] scopeData;
    String[] shortNames;
    String shortUnitSep;
    String[] singularNames;
    String skippedUnitMarker;
    String[] unitSep;
    boolean[] unitSepRequiresDP;
    byte useMilliseconds;
    boolean weeksAloneOnly;
    char zero;
    byte zeroHandling;

    public interface ECountVariant {
        public static final byte DECIMAL1 = (byte) 3;
        public static final byte DECIMAL2 = (byte) 4;
        public static final byte DECIMAL3 = (byte) 5;
        public static final byte HALF_FRACTION = (byte) 2;
        public static final byte INTEGER = (byte) 0;
        public static final byte INTEGER_CUSTOM = (byte) 1;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.ECountVariant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.ECountVariant.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.ECountVariant.<clinit>():void");
        }
    }

    public interface EDecimalHandling {
        public static final byte DPAUCAL = (byte) 3;
        public static final byte DPLURAL = (byte) 0;
        public static final byte DSINGULAR = (byte) 1;
        public static final byte DSINGULAR_SUBONE = (byte) 2;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EDecimalHandling.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EDecimalHandling.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EDecimalHandling.<clinit>():void");
        }
    }

    public interface EFractionHandling {
        public static final byte FPAUCAL = (byte) 3;
        public static final byte FPLURAL = (byte) 0;
        public static final byte FSINGULAR_PLURAL = (byte) 1;
        public static final byte FSINGULAR_PLURAL_ANDAHALF = (byte) 2;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EFractionHandling.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EFractionHandling.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EFractionHandling.<clinit>():void");
        }
    }

    public interface EGender {
        public static final byte F = (byte) 1;
        public static final byte M = (byte) 0;
        public static final byte N = (byte) 2;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EGender.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EGender.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EGender.<clinit>():void");
        }
    }

    public interface EHalfPlacement {
        public static final byte AFTER_FIRST = (byte) 1;
        public static final byte LAST = (byte) 2;
        public static final byte PREFIX = (byte) 0;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EHalfPlacement.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EHalfPlacement.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EHalfPlacement.<clinit>():void");
        }
    }

    public interface EHalfSupport {
        public static final byte NO = (byte) 1;
        public static final byte ONE_PLUS = (byte) 2;
        public static final byte YES = (byte) 0;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EHalfSupport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EHalfSupport.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EHalfSupport.<clinit>():void");
        }
    }

    public interface EMilliSupport {
        public static final byte NO = (byte) 1;
        public static final byte WITH_SECONDS = (byte) 2;
        public static final byte YES = (byte) 0;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EMilliSupport.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EMilliSupport.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EMilliSupport.<clinit>():void");
        }
    }

    public interface ENumberSystem {
        public static final byte CHINESE_SIMPLIFIED = (byte) 2;
        public static final byte CHINESE_TRADITIONAL = (byte) 1;
        public static final byte DEFAULT = (byte) 0;
        public static final byte KOREAN = (byte) 3;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.ENumberSystem.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.ENumberSystem.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.ENumberSystem.<clinit>():void");
        }
    }

    public interface EPluralization {
        public static final byte ARABIC = (byte) 5;
        public static final byte DUAL = (byte) 2;
        public static final byte HEBREW = (byte) 4;
        public static final byte NONE = (byte) 0;
        public static final byte PAUCAL = (byte) 3;
        public static final byte PLURAL = (byte) 1;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EPluralization.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EPluralization.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EPluralization.<clinit>():void");
        }
    }

    public interface ESeparatorVariant {
        public static final byte FULL = (byte) 2;
        public static final byte NONE = (byte) 0;
        public static final byte SHORT = (byte) 1;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.ESeparatorVariant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.ESeparatorVariant.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.ESeparatorVariant.<clinit>():void");
        }
    }

    public interface ETimeDirection {
        public static final byte FUTURE = (byte) 2;
        public static final byte NODIRECTION = (byte) 0;
        public static final byte PAST = (byte) 1;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.ETimeDirection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.ETimeDirection.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.ETimeDirection.<clinit>():void");
        }
    }

    public interface ETimeLimit {
        public static final byte LT = (byte) 1;
        public static final byte MT = (byte) 2;
        public static final byte NOLIMIT = (byte) 0;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.ETimeLimit.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.ETimeLimit.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.ETimeLimit.<clinit>():void");
        }
    }

    public interface EUnitVariant {
        public static final byte MEDIUM = (byte) 1;
        public static final byte PLURALIZED = (byte) 0;
        public static final byte SHORT = (byte) 2;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EUnitVariant.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EUnitVariant.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EUnitVariant.<clinit>():void");
        }
    }

    public interface EZeroHandling {
        public static final byte ZPLURAL = (byte) 0;
        public static final byte ZSINGULAR = (byte) 1;
        public static final String[] names = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.duration.impl.DataRecord.EZeroHandling.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.duration.impl.DataRecord.EZeroHandling.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.duration.impl.DataRecord.EZeroHandling.<clinit>():void");
        }
    }

    public static class ScopeData {
        String prefix;
        boolean requiresDigitPrefix;
        String suffix;

        public ScopeData() {
        }

        public void write(RecordWriter out) {
            out.open("ScopeData");
            out.string("prefix", this.prefix);
            out.bool("requiresDigitPrefix", this.requiresDigitPrefix);
            out.string("suffix", this.suffix);
            out.close();
        }

        public static ScopeData read(RecordReader in) {
            if (in.open("ScopeData")) {
                ScopeData scope = new ScopeData();
                scope.prefix = in.string("prefix");
                scope.requiresDigitPrefix = in.bool("requiresDigitPrefix");
                scope.suffix = in.string("suffix");
                if (in.close()) {
                    return scope;
                }
            }
            return null;
        }
    }

    public DataRecord() {
    }

    public static DataRecord read(String ln, RecordReader in) {
        if (in.open("DataRecord")) {
            DataRecord record = new DataRecord();
            record.pl = in.namedIndex("pl", EPluralization.names);
            record.pluralNames = in.stringTable("pluralName");
            record.genders = in.namedIndexArray("gender", EGender.names);
            record.singularNames = in.stringArray("singularName");
            record.halfNames = in.stringArray("halfName");
            record.numberNames = in.stringArray("numberName");
            record.mediumNames = in.stringArray("mediumName");
            record.shortNames = in.stringArray("shortName");
            record.measures = in.stringArray("measure");
            record.rqdSuffixes = in.stringArray("rqdSuffix");
            record.optSuffixes = in.stringArray("optSuffix");
            record.halves = in.stringArray("halves");
            record.halfPlacements = in.namedIndexArray("halfPlacement", EHalfPlacement.names);
            record.halfSupport = in.namedIndexArray("halfSupport", EHalfSupport.names);
            record.fifteenMinutes = in.string("fifteenMinutes");
            record.fiveMinutes = in.string("fiveMinutes");
            record.requiresDigitSeparator = in.bool("requiresDigitSeparator");
            record.digitPrefix = in.string("digitPrefix");
            record.countSep = in.string("countSep");
            record.shortUnitSep = in.string("shortUnitSep");
            record.unitSep = in.stringArray("unitSep");
            record.unitSepRequiresDP = in.boolArray("unitSepRequiresDP");
            record.requiresSkipMarker = in.boolArray("requiresSkipMarker");
            record.numberSystem = in.namedIndex("numberSystem", ENumberSystem.names);
            record.zero = in.character(PluralRules.KEYWORD_ZERO);
            record.decimalSep = in.character("decimalSep");
            record.omitSingularCount = in.bool("omitSingularCount");
            record.omitDualCount = in.bool("omitDualCount");
            record.zeroHandling = in.namedIndex("zeroHandling", EZeroHandling.names);
            record.decimalHandling = in.namedIndex("decimalHandling", EDecimalHandling.names);
            record.fractionHandling = in.namedIndex("fractionHandling", EFractionHandling.names);
            record.skippedUnitMarker = in.string("skippedUnitMarker");
            record.allowZero = in.bool("allowZero");
            record.weeksAloneOnly = in.bool("weeksAloneOnly");
            record.useMilliseconds = in.namedIndex("useMilliseconds", EMilliSupport.names);
            if (in.open("ScopeDataList")) {
                List<ScopeData> list = new ArrayList();
                while (true) {
                    ScopeData data = ScopeData.read(in);
                    if (data == null) {
                        break;
                    }
                    list.add(data);
                }
                if (in.close()) {
                    record.scopeData = (ScopeData[]) list.toArray(new ScopeData[list.size()]);
                }
            }
            if (in.close()) {
                return record;
            }
            throw new InternalError("null data read while reading " + ln);
        }
        throw new InternalError("did not find DataRecord while reading " + ln);
    }

    public void write(RecordWriter out) {
        out.open("DataRecord");
        out.namedIndex("pl", EPluralization.names, this.pl);
        out.stringTable("pluralName", this.pluralNames);
        out.namedIndexArray("gender", EGender.names, this.genders);
        out.stringArray("singularName", this.singularNames);
        out.stringArray("halfName", this.halfNames);
        out.stringArray("numberName", this.numberNames);
        out.stringArray("mediumName", this.mediumNames);
        out.stringArray("shortName", this.shortNames);
        out.stringArray("measure", this.measures);
        out.stringArray("rqdSuffix", this.rqdSuffixes);
        out.stringArray("optSuffix", this.optSuffixes);
        out.stringArray("halves", this.halves);
        out.namedIndexArray("halfPlacement", EHalfPlacement.names, this.halfPlacements);
        out.namedIndexArray("halfSupport", EHalfSupport.names, this.halfSupport);
        out.string("fifteenMinutes", this.fifteenMinutes);
        out.string("fiveMinutes", this.fiveMinutes);
        out.bool("requiresDigitSeparator", this.requiresDigitSeparator);
        out.string("digitPrefix", this.digitPrefix);
        out.string("countSep", this.countSep);
        out.string("shortUnitSep", this.shortUnitSep);
        out.stringArray("unitSep", this.unitSep);
        out.boolArray("unitSepRequiresDP", this.unitSepRequiresDP);
        out.boolArray("requiresSkipMarker", this.requiresSkipMarker);
        out.namedIndex("numberSystem", ENumberSystem.names, this.numberSystem);
        out.character(PluralRules.KEYWORD_ZERO, this.zero);
        out.character("decimalSep", this.decimalSep);
        out.bool("omitSingularCount", this.omitSingularCount);
        out.bool("omitDualCount", this.omitDualCount);
        out.namedIndex("zeroHandling", EZeroHandling.names, this.zeroHandling);
        out.namedIndex("decimalHandling", EDecimalHandling.names, this.decimalHandling);
        out.namedIndex("fractionHandling", EFractionHandling.names, this.fractionHandling);
        out.string("skippedUnitMarker", this.skippedUnitMarker);
        out.bool("allowZero", this.allowZero);
        out.bool("weeksAloneOnly", this.weeksAloneOnly);
        out.namedIndex("useMilliseconds", EMilliSupport.names, this.useMilliseconds);
        if (this.scopeData != null) {
            out.open("ScopeDataList");
            for (ScopeData write : this.scopeData) {
                write.write(out);
            }
            out.close();
        }
        out.close();
    }
}

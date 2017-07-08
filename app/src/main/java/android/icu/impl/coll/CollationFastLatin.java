package android.icu.impl.coll;

import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import libcore.icu.DateUtilsBridge;

public final class CollationFastLatin {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final int BAIL_OUT = 1;
    public static final int BAIL_OUT_RESULT = -2;
    static final int CASE_AND_TERTIARY_MASK = 31;
    static final int CASE_MASK = 24;
    static final int COMMON_SEC = 160;
    static final int COMMON_SEC_PLUS_OFFSET = 192;
    static final int COMMON_TER = 0;
    static final int COMMON_TER_PLUS_OFFSET = 32;
    static final int CONTRACTION = 1024;
    static final int CONTR_CHAR_MASK = 511;
    static final int CONTR_LENGTH_SHIFT = 9;
    static final int EOS = 2;
    static final int EXPANSION = 2048;
    static final int INDEX_MASK = 1023;
    public static final int LATIN_LIMIT = 384;
    public static final int LATIN_MAX = 383;
    static final int LATIN_MAX_UTF8_LEAD = 197;
    static final int LONG_INC = 8;
    static final int LONG_PRIMARY_MASK = 65528;
    static final int LOWER_CASE = 8;
    static final int MAX_LONG = 4088;
    static final int MAX_SEC_AFTER = 352;
    static final int MAX_SEC_BEFORE = 128;
    static final int MAX_SEC_HIGH = 992;
    static final int MAX_SHORT = 64512;
    static final int MAX_TER_AFTER = 7;
    static final int MERGE_WEIGHT = 3;
    static final int MIN_LONG = 3072;
    static final int MIN_SEC_AFTER = 192;
    static final int MIN_SEC_BEFORE = 0;
    static final int MIN_SEC_HIGH = 384;
    static final int MIN_SHORT = 4096;
    static final int NUM_FAST_CHARS = 448;
    static final int PUNCT_LIMIT = 8256;
    static final int PUNCT_START = 8192;
    static final int SECONDARY_MASK = 992;
    static final int SEC_INC = 32;
    static final int SEC_OFFSET = 32;
    static final int SHORT_INC = 1024;
    static final int SHORT_PRIMARY_MASK = 64512;
    static final int TERTIARY_MASK = 7;
    static final int TER_OFFSET = 32;
    static final int TWO_CASES_MASK = 1572888;
    static final int TWO_COMMON_SEC_PLUS_OFFSET = 12583104;
    static final int TWO_COMMON_TER_PLUS_OFFSET = 2097184;
    static final int TWO_LONG_PRIMARIES_MASK = -458760;
    static final int TWO_LOWER_CASES = 524296;
    static final int TWO_SECONDARIES_MASK = 65012704;
    static final int TWO_SEC_OFFSETS = 2097184;
    static final int TWO_SHORT_PRIMARIES_MASK = -67044352;
    static final int TWO_TERTIARIES_MASK = 458759;
    static final int TWO_TER_OFFSETS = 2097184;
    public static final int VERSION = 2;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationFastLatin.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationFastLatin.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationFastLatin.<clinit>():void");
    }

    public static int compareUTF16(char[] r1, char[] r2, int r3, java.lang.CharSequence r4, java.lang.CharSequence r5, int r6) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationFastLatin.compareUTF16(char[], char[], int, java.lang.CharSequence, java.lang.CharSequence, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationFastLatin.compareUTF16(char[], char[], int, java.lang.CharSequence, java.lang.CharSequence, int):int");
    }

    private static long nextPair(char[] r1, int r2, int r3, java.lang.CharSequence r4, int r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationFastLatin.nextPair(char[], int, int, java.lang.CharSequence, int):long
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationFastLatin.nextPair(char[], int, int, java.lang.CharSequence, int):long");
    }

    static int getCharIndex(char c) {
        if (c <= '\u017f') {
            return c;
        }
        if ('\u2000' > c || c >= '\u2040') {
            return -1;
        }
        return c - 7808;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int getOptions(CollationData data, CollationSettings settings, char[] primaries) {
        char[] header = data.fastLatinTableHeader;
        if (header == null) {
            return -1;
        }
        int length;
        if (!-assertionsDisabled) {
            if (((header[MIN_SEC_BEFORE] >> LOWER_CASE) == VERSION ? BAIL_OUT : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            length = primaries.length;
            if ((r0 == MIN_SEC_HIGH ? BAIL_OUT : null) == null) {
                throw new AssertionError();
            }
        }
        length = primaries.length;
        if (r0 != MIN_SEC_HIGH) {
            return -1;
        }
        int miniVarTop;
        int c;
        if ((settings.options & 12) == 0) {
            miniVarTop = Opcodes.OP_IGET_CHAR_JUMBO;
        } else {
            int i = settings.getMaxVariable() + BAIL_OUT;
            if (i >= (header[MIN_SEC_BEFORE] & Opcodes.OP_CONST_CLASS_JUMBO)) {
                return -1;
            }
            miniVarTop = header[i];
        }
        boolean digitsAreReordered = -assertionsDisabled;
        if (settings.hasReordering()) {
            long prevStart = 0;
            long beforeDigitStart = 0;
            long digitStart = 0;
            long afterDigitStart = 0;
            for (int group = MIN_SHORT; group < 4104; group += BAIL_OUT) {
                long start = settings.reorder(data.getFirstPrimaryForGroup(group));
                if (group == 4100) {
                    beforeDigitStart = prevStart;
                    digitStart = start;
                } else if (start == 0) {
                    continue;
                } else if (start < prevStart) {
                    return -1;
                } else {
                    if (digitStart != 0 && afterDigitStart == 0 && prevStart == beforeDigitStart) {
                        afterDigitStart = start;
                    }
                    prevStart = start;
                }
            }
            long latinStart = settings.reorder(data.getFirstPrimaryForGroup(25));
            if (latinStart < prevStart) {
                return -1;
            }
            if (afterDigitStart == 0) {
                afterDigitStart = latinStart;
            }
            if (beforeDigitStart >= digitStart || digitStart >= afterDigitStart) {
                digitsAreReordered = true;
            }
        }
        char[] table = data.fastLatinTable;
        for (c = MIN_SEC_BEFORE; c < MIN_SEC_HIGH; c += BAIL_OUT) {
            int p = table[c];
            if (p >= MIN_SHORT) {
                p &= SHORT_PRIMARY_MASK;
            } else if (p > miniVarTop) {
                p &= LONG_PRIMARY_MASK;
            } else {
                p = MIN_SEC_BEFORE;
            }
            primaries[c] = (char) p;
        }
        if (!digitsAreReordered) {
        }
        for (c = 48; c <= 57; c += BAIL_OUT) {
            primaries[c] = '\u0000';
        }
        return (miniVarTop << 16) | settings.options;
    }

    private static int lookup(char[] table, int c) {
        if (!-assertionsDisabled) {
            if ((c > LATIN_MAX ? BAIL_OUT : MIN_SEC_BEFORE) == 0) {
                throw new AssertionError();
            }
        }
        if (PUNCT_START <= c && c < PUNCT_LIMIT) {
            return table[(c - 8192) + MIN_SEC_HIGH];
        }
        if (c == 65534) {
            return MERGE_WEIGHT;
        }
        if (c == DexFormat.MAX_TYPE_IDX) {
            return 64680;
        }
        return BAIL_OUT;
    }

    private static int getPrimaries(int variableTop, int pair) {
        int ce = pair & DexFormat.MAX_TYPE_IDX;
        if (ce >= MIN_SHORT) {
            return TWO_SHORT_PRIMARIES_MASK & pair;
        }
        if (ce > variableTop) {
            return TWO_LONG_PRIMARIES_MASK & pair;
        }
        if (ce >= MIN_LONG) {
            return MIN_SEC_BEFORE;
        }
        return pair;
    }

    private static int getSecondariesFromOneShortCE(int ce) {
        ce &= SECONDARY_MASK;
        if (ce < MIN_SEC_HIGH) {
            return ce + TER_OFFSET;
        }
        return ((ce + TER_OFFSET) << 16) | MIN_SEC_AFTER;
    }

    private static int getSecondaries(int variableTop, int pair) {
        if (pair > DexFormat.MAX_TYPE_IDX) {
            int ce = pair & DexFormat.MAX_TYPE_IDX;
            if (ce >= MIN_SHORT) {
                return (TWO_SECONDARIES_MASK & pair) + TWO_TER_OFFSETS;
            }
            if (ce > variableTop) {
                return TWO_COMMON_SEC_PLUS_OFFSET;
            }
            if (!-assertionsDisabled) {
                if ((ce >= MIN_LONG ? BAIL_OUT : null) == null) {
                    throw new AssertionError();
                }
            }
            return MIN_SEC_BEFORE;
        } else if (pair >= MIN_SHORT) {
            return getSecondariesFromOneShortCE(pair);
        } else {
            if (pair > variableTop) {
                return MIN_SEC_AFTER;
            }
            if (pair >= MIN_LONG) {
                return MIN_SEC_BEFORE;
            }
            return pair;
        }
    }

    private static int getCases(int variableTop, boolean strengthIsPrimary, int pair) {
        Object obj = null;
        int ce;
        if (pair > DexFormat.MAX_TYPE_IDX) {
            ce = pair & DexFormat.MAX_TYPE_IDX;
            if (ce >= MIN_SHORT) {
                if (strengthIsPrimary && (-67108864 & pair) == 0) {
                    return pair & CASE_MASK;
                }
                return pair & TWO_CASES_MASK;
            } else if (ce > variableTop) {
                return TWO_LOWER_CASES;
            } else {
                if (!-assertionsDisabled) {
                    if (ce >= MIN_LONG) {
                        obj = BAIL_OUT;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                return MIN_SEC_BEFORE;
            }
        } else if (pair >= MIN_SHORT) {
            ce = pair;
            pair &= CASE_MASK;
            return (strengthIsPrimary || (ce & SECONDARY_MASK) < MIN_SEC_HIGH) ? pair : pair | DateUtilsBridge.FORMAT_ABBREV_ALL;
        } else if (pair > variableTop) {
            return LOWER_CASE;
        } else {
            if (pair >= MIN_LONG) {
                return MIN_SEC_BEFORE;
            }
            return pair;
        }
    }

    private static int getTertiaries(int variableTop, boolean withCaseBits, int pair) {
        int ce;
        if (pair > DexFormat.MAX_TYPE_IDX) {
            ce = pair & DexFormat.MAX_TYPE_IDX;
            if (ce >= MIN_SHORT) {
                if (withCaseBits) {
                    pair &= 2031647;
                } else {
                    pair &= TWO_TERTIARIES_MASK;
                }
                return pair + TWO_TER_OFFSETS;
            } else if (ce > variableTop) {
                pair = (pair & TWO_TERTIARIES_MASK) + TWO_TER_OFFSETS;
                if (withCaseBits) {
                    return pair | TWO_LOWER_CASES;
                }
                return pair;
            } else {
                if (!-assertionsDisabled) {
                    if ((ce >= MIN_LONG ? BAIL_OUT : null) == null) {
                        throw new AssertionError();
                    }
                }
                return MIN_SEC_BEFORE;
            }
        } else if (pair >= MIN_SHORT) {
            ce = pair;
            if (withCaseBits) {
                pair = (pair & CASE_AND_TERTIARY_MASK) + TER_OFFSET;
                return (ce & SECONDARY_MASK) >= MIN_SEC_HIGH ? pair | 2621440 : pair;
            } else {
                pair = (pair & TERTIARY_MASK) + TER_OFFSET;
                if ((ce & SECONDARY_MASK) >= MIN_SEC_HIGH) {
                    return pair | VMDebug.KIND_THREAD_CLASS_INIT_COUNT;
                }
                return pair;
            }
        } else if (pair > variableTop) {
            pair = (pair & TERTIARY_MASK) + TER_OFFSET;
            if (withCaseBits) {
                return pair | LOWER_CASE;
            }
            return pair;
        } else if (pair >= MIN_LONG) {
            return MIN_SEC_BEFORE;
        } else {
            return pair;
        }
    }

    private static int getQuaternaries(int variableTop, int pair) {
        if (pair > DexFormat.MAX_TYPE_IDX) {
            int ce = pair & DexFormat.MAX_TYPE_IDX;
            if (ce > variableTop) {
                return TWO_SHORT_PRIMARIES_MASK;
            }
            if (!-assertionsDisabled) {
                if ((ce >= MIN_LONG ? BAIL_OUT : null) == null) {
                    throw new AssertionError();
                }
            }
            return pair & TWO_LONG_PRIMARIES_MASK;
        } else if (pair >= MIN_SHORT) {
            if ((pair & SECONDARY_MASK) >= MIN_SEC_HIGH) {
                return TWO_SHORT_PRIMARIES_MASK;
            }
            return SHORT_PRIMARY_MASK;
        } else if (pair > variableTop) {
            return SHORT_PRIMARY_MASK;
        } else {
            if (pair >= MIN_LONG) {
                return pair & LONG_PRIMARY_MASK;
            }
            return pair;
        }
    }

    private CollationFastLatin() {
    }
}

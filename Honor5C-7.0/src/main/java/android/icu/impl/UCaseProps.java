package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie2.Range;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import libcore.io.IoBridge;

public final class UCaseProps {
    private static final int ABOVE = 64;
    private static final int CLOSURE_MAX_LENGTH = 15;
    private static final String DATA_FILE_NAME = "ucase.icu";
    private static final String DATA_NAME = "ucase";
    private static final String DATA_TYPE = "icu";
    private static final int DELTA_SHIFT = 7;
    private static final int DOT_MASK = 96;
    private static final int EXCEPTION = 16;
    private static final int EXC_CLOSURE = 6;
    private static final int EXC_CONDITIONAL_FOLD = 32768;
    private static final int EXC_CONDITIONAL_SPECIAL = 16384;
    private static final int EXC_DOT_SHIFT = 7;
    private static final int EXC_DOUBLE_SLOTS = 256;
    private static final int EXC_FOLD = 1;
    private static final int EXC_FULL_MAPPINGS = 7;
    private static final int EXC_LOWER = 0;
    private static final int EXC_SHIFT = 5;
    private static final int EXC_TITLE = 3;
    private static final int EXC_UPPER = 2;
    private static final int FMT = 1665225541;
    private static final int FOLD_CASE_OPTIONS_MASK = 255;
    private static final int FULL_LOWER = 15;
    public static final UCaseProps INSTANCE = null;
    private static final int IX_EXC_LENGTH = 3;
    private static final int IX_TOP = 16;
    private static final int IX_TRIE_SIZE = 2;
    private static final int IX_UNFOLD_LENGTH = 4;
    private static final int LOC_LITHUANIAN = 3;
    private static final int LOC_ROOT = 1;
    private static final int LOC_TURKISH = 2;
    private static final int LOC_UNKNOWN = 0;
    public static final int LOWER = 1;
    public static final int MAX_STRING_LENGTH = 31;
    public static final int NONE = 0;
    private static final int OTHER_ACCENT = 96;
    private static final int SENSITIVE = 8;
    private static final int SOFT_DOTTED = 32;
    public static final int TITLE = 3;
    public static final int TYPE_MASK = 3;
    private static final int UNFOLD_ROWS = 0;
    private static final int UNFOLD_ROW_WIDTH = 1;
    private static final int UNFOLD_STRING_WIDTH = 2;
    public static final int UPPER = 2;
    public static final StringBuilder dummyStringBuilder = null;
    private static final byte[] flagsOffset = null;
    private static final String iDot = "i\u0307";
    private static final String iDotAcute = "i\u0307\u0301";
    private static final String iDotGrave = "i\u0307\u0300";
    private static final String iDotTilde = "i\u0307\u0303";
    private static final String iOgonekDot = "\u012f\u0307";
    private static final String jDot = "j\u0307";
    private static final int[] rootLocCache = null;
    private char[] exceptions;
    private int[] indexes;
    private Trie2_16 trie;
    private char[] unfold;

    public interface ContextIterator {
        int next();

        void reset(int i);
    }

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[UCaseProps.UNFOLD_ROWS] == UCaseProps.TYPE_MASK;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UCaseProps.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.<clinit>():void");
    }

    private final int toUpperOrTitle(int r1, android.icu.impl.UCaseProps.ContextIterator r2, java.lang.StringBuilder r3, android.icu.util.ULocale r4, int[] r5, boolean r6) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.toUpperOrTitle(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[], boolean):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.toUpperOrTitle(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[], boolean):int");
    }

    public final int toFullFolding(int r1, java.lang.StringBuilder r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.toFullFolding(int, java.lang.StringBuilder, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.toFullFolding(int, java.lang.StringBuilder, int):int");
    }

    public final int toFullLower(int r1, android.icu.impl.UCaseProps.ContextIterator r2, java.lang.StringBuilder r3, android.icu.util.ULocale r4, int[] r5) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UCaseProps.toFullLower(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[]):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UCaseProps.toFullLower(int, android.icu.impl.UCaseProps$ContextIterator, java.lang.StringBuilder, android.icu.util.ULocale, int[]):int");
    }

    private UCaseProps() throws IOException {
        readData(ICUBinary.getRequiredData(DATA_FILE_NAME));
    }

    private final void readData(ByteBuffer bytes) throws IOException {
        ICUBinary.readHeader(bytes, FMT, new IsAcceptable());
        int count = bytes.getInt();
        if (count < IX_TOP) {
            throw new IOException("indexes[0] too small in ucase.icu");
        }
        this.indexes = new int[count];
        this.indexes[UNFOLD_ROWS] = count;
        for (int i = UNFOLD_ROW_WIDTH; i < count; i += UNFOLD_ROW_WIDTH) {
            this.indexes[i] = bytes.getInt();
        }
        this.trie = Trie2_16.createFromSerialized(bytes);
        int expectedTrieLength = this.indexes[UPPER];
        int trieLength = this.trie.getSerializedLength();
        if (trieLength > expectedTrieLength) {
            throw new IOException("ucase.icu: not enough bytes for the trie");
        }
        ICUBinary.skipBytes(bytes, expectedTrieLength - trieLength);
        count = this.indexes[TYPE_MASK];
        if (count > 0) {
            this.exceptions = ICUBinary.getChars(bytes, count, UNFOLD_ROWS);
        }
        count = this.indexes[IX_UNFOLD_LENGTH];
        if (count > 0) {
            this.unfold = ICUBinary.getChars(bytes, count, UNFOLD_ROWS);
        }
    }

    public final void addPropertyStarts(UnicodeSet set) {
        Iterator<Range> trieIterator = this.trie.iterator();
        while (trieIterator.hasNext()) {
            Range range = (Range) trieIterator.next();
            if (!range.leadSurrogate) {
                set.add(range.startCodePoint);
            } else {
                return;
            }
        }
    }

    private static final int getExceptionsOffset(int props) {
        return props >> EXC_SHIFT;
    }

    private static final boolean propsHasException(int props) {
        return (props & IX_TOP) != 0;
    }

    private static final boolean hasSlot(int flags, int index) {
        return ((UNFOLD_ROW_WIDTH << index) & flags) != 0;
    }

    private static final byte slotOffset(int flags, int index) {
        return flagsOffset[((UNFOLD_ROW_WIDTH << index) - 1) & flags];
    }

    private final long getSlotValueAndOffset(int excWord, int index, int excOffset) {
        long value;
        if ((excWord & EXC_DOUBLE_SLOTS) == 0) {
            excOffset += slotOffset(excWord, index);
            value = (long) this.exceptions[excOffset];
        } else {
            excOffset += slotOffset(excWord, index) * UPPER;
            int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
            value = (((long) this.exceptions[excOffset]) << IX_TOP) | ((long) this.exceptions[excOffset2]);
            excOffset = excOffset2;
        }
        return (((long) excOffset) << SOFT_DOTTED) | value;
    }

    private final int getSlotValue(int excWord, int index, int excOffset) {
        if ((excWord & EXC_DOUBLE_SLOTS) == 0) {
            return this.exceptions[excOffset + slotOffset(excWord, index)];
        }
        excOffset += slotOffset(excWord, index) * UPPER;
        int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
        int value = (this.exceptions[excOffset] << IX_TOP) | this.exceptions[excOffset2];
        excOffset = excOffset2;
        return value;
    }

    public final int tolower(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
            int excWord = this.exceptions[excOffset];
            if (hasSlot(excWord, UNFOLD_ROWS)) {
                return getSlotValue(excWord, UNFOLD_ROWS, excOffset2);
            }
            return c;
        } else if (getTypeFromProps(props) >= UPPER) {
            return c + getDelta(props);
        } else {
            return c;
        }
    }

    public final int toupper(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
            int excWord = this.exceptions[excOffset];
            if (hasSlot(excWord, UPPER)) {
                return getSlotValue(excWord, UPPER, excOffset2);
            }
            return c;
        } else if (getTypeFromProps(props) == UNFOLD_ROW_WIDTH) {
            return c + getDelta(props);
        } else {
            return c;
        }
    }

    public final int totitle(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int index;
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
            int excWord = this.exceptions[excOffset];
            if (hasSlot(excWord, TYPE_MASK)) {
                index = TYPE_MASK;
            } else if (!hasSlot(excWord, UPPER)) {
                return c;
            } else {
                index = UPPER;
            }
            c = getSlotValue(excWord, index, excOffset2);
        } else if (getTypeFromProps(props) == UNFOLD_ROW_WIDTH) {
            c += getDelta(props);
        }
        return c;
    }

    public final void addCaseClosure(int c, UnicodeSet set) {
        switch (c) {
            case Opcodes.OP_AGET_CHAR /*73*/:
                set.add((int) Opcodes.OP_SPUT_OBJECT);
            case Opcodes.OP_SPUT_OBJECT /*105*/:
                set.add(73);
            case 304:
                set.add(iDot);
            case 305:
            default:
                int props = this.trie.get(c);
                if (propsHasException(props)) {
                    int index;
                    long value;
                    int closureLength;
                    int closureOffset;
                    int excOffset = getExceptionsOffset(props);
                    int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
                    int excWord = this.exceptions[excOffset];
                    int excOffset0 = excOffset2;
                    excOffset = excOffset2;
                    for (index = UNFOLD_ROWS; index <= TYPE_MASK; index += UNFOLD_ROW_WIDTH) {
                        if (hasSlot(excWord, index)) {
                            excOffset = excOffset0;
                            set.add(getSlotValue(excWord, index, excOffset0));
                        }
                    }
                    if (hasSlot(excWord, EXC_CLOSURE)) {
                        excOffset = excOffset0;
                        value = getSlotValueAndOffset(excWord, EXC_CLOSURE, excOffset0);
                        closureLength = ((int) value) & FULL_LOWER;
                        closureOffset = ((int) (value >> SOFT_DOTTED)) + UNFOLD_ROW_WIDTH;
                    } else {
                        closureLength = UNFOLD_ROWS;
                        closureOffset = UNFOLD_ROWS;
                    }
                    if (hasSlot(excWord, EXC_FULL_MAPPINGS)) {
                        excOffset = excOffset0;
                        value = getSlotValueAndOffset(excWord, EXC_FULL_MAPPINGS, excOffset0);
                        int fullLength = ((int) value) & DexFormat.MAX_TYPE_IDX;
                        excOffset = (((int) (value >> SOFT_DOTTED)) + UNFOLD_ROW_WIDTH) + (fullLength & FULL_LOWER);
                        fullLength >>= IX_UNFOLD_LENGTH;
                        int length = fullLength & FULL_LOWER;
                        if (length != 0) {
                            set.add(new String(this.exceptions, excOffset, length));
                            excOffset += length;
                        }
                        fullLength >>= IX_UNFOLD_LENGTH;
                        closureOffset = (excOffset + (fullLength & FULL_LOWER)) + (fullLength >> IX_UNFOLD_LENGTH);
                    }
                    index = UNFOLD_ROWS;
                    while (index < closureLength) {
                        c = UTF16.charAt(this.exceptions, closureOffset, this.exceptions.length, index);
                        set.add(c);
                        index += UTF16.getCharCount(c);
                    }
                } else if (getTypeFromProps(props) != 0) {
                    int delta = getDelta(props);
                    if (delta != 0) {
                        set.add(c + delta);
                    }
                }
        }
    }

    private final int strcmpMax(String s, int unfoldOffset, int max) {
        int length = s.length();
        max -= length;
        int i1 = UNFOLD_ROWS;
        while (true) {
            int i12 = i1 + UNFOLD_ROW_WIDTH;
            int c1 = s.charAt(i1);
            int unfoldOffset2 = unfoldOffset + UNFOLD_ROW_WIDTH;
            int c2 = this.unfold[unfoldOffset];
            if (c2 != 0) {
                c1 -= c2;
                if (c1 == 0) {
                    length--;
                    if (length <= 0) {
                        break;
                    }
                    i1 = i12;
                    unfoldOffset = unfoldOffset2;
                } else {
                    return c1;
                }
            }
            return UNFOLD_ROW_WIDTH;
        }
        if (max == 0 || this.unfold[unfoldOffset2] == '\u0000') {
            return UNFOLD_ROWS;
        }
        return -max;
    }

    public final boolean addStringCaseClosure(String s, UnicodeSet set) {
        if (this.unfold == null || s == null) {
            return false;
        }
        int length = s.length();
        if (length <= UNFOLD_ROW_WIDTH) {
            return false;
        }
        int unfoldRows = this.unfold[UNFOLD_ROWS];
        int unfoldRowWidth = this.unfold[UNFOLD_ROW_WIDTH];
        int unfoldStringWidth = this.unfold[UPPER];
        if (length > unfoldStringWidth) {
            return false;
        }
        int start = UNFOLD_ROWS;
        int limit = unfoldRows;
        while (start < limit) {
            int i = (start + limit) / UPPER;
            int unfoldOffset = (i + UNFOLD_ROW_WIDTH) * unfoldRowWidth;
            int result = strcmpMax(s, unfoldOffset, unfoldStringWidth);
            if (result == 0) {
                i = unfoldStringWidth;
                while (i < unfoldRowWidth && this.unfold[unfoldOffset + i] != '\u0000') {
                    int c = UTF16.charAt(this.unfold, unfoldOffset, this.unfold.length, i);
                    set.add(c);
                    addCaseClosure(c, set);
                    i += UTF16.getCharCount(c);
                }
                return true;
            } else if (result < 0) {
                limit = i;
            } else {
                start = i + UNFOLD_ROW_WIDTH;
            }
        }
        return false;
    }

    public final int getType(int c) {
        return getTypeFromProps(this.trie.get(c));
    }

    public final int getTypeOrIgnorable(int c) {
        return getTypeAndIgnorableFromProps(this.trie.get(c));
    }

    public final int getDotType(int c) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            return (this.exceptions[getExceptionsOffset(props)] >> EXC_FULL_MAPPINGS) & OTHER_ACCENT;
        }
        return props & OTHER_ACCENT;
    }

    public final boolean isSoftDotted(int c) {
        return getDotType(c) == SOFT_DOTTED;
    }

    public final boolean isCaseSensitive(int c) {
        return (this.trie.get(c) & SENSITIVE) != 0;
    }

    private static final int getCaseLocale(ULocale locale, int[] locCache) {
        int result;
        if (locCache != null) {
            result = locCache[UNFOLD_ROWS];
            if (result != 0) {
                return result;
            }
        }
        result = UNFOLD_ROW_WIDTH;
        String language = locale.getLanguage();
        if (language.equals("tr") || language.equals("tur") || language.equals("az") || language.equals("aze")) {
            result = UPPER;
        } else if (language.equals("lt") || language.equals("lit")) {
            result = TYPE_MASK;
        }
        if (locCache != null) {
            locCache[UNFOLD_ROWS] = result;
        }
        return result;
    }

    private final boolean isFollowedByCasedLetter(ContextIterator iter, int dir) {
        if (iter == null) {
            return false;
        }
        iter.reset(dir);
        int type;
        do {
            int c = iter.next();
            if (c < 0) {
                return false;
            }
            type = getTypeOrIgnorable(c);
        } while ((type & IX_UNFOLD_LENGTH) != 0);
        if (type != 0) {
            return true;
        }
        return false;
    }

    private final boolean isPrecededBySoftDotted(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(-1);
        int dotType;
        do {
            int c = iter.next();
            if (c < 0) {
                return false;
            }
            dotType = getDotType(c);
            if (dotType == SOFT_DOTTED) {
                return true;
            }
        } while (dotType == OTHER_ACCENT);
        return false;
    }

    private final boolean isPrecededBy_I(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(-1);
        int c;
        do {
            c = iter.next();
            if (c < 0) {
                return false;
            }
            if (c == 73) {
                return true;
            }
        } while (getDotType(c) == OTHER_ACCENT);
        return false;
    }

    private final boolean isFollowedByMoreAbove(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(UNFOLD_ROW_WIDTH);
        int dotType;
        do {
            int c = iter.next();
            if (c < 0) {
                return false;
            }
            dotType = getDotType(c);
            if (dotType == ABOVE) {
                return true;
            }
        } while (dotType == OTHER_ACCENT);
        return false;
    }

    private final boolean isFollowedByDotAbove(ContextIterator iter) {
        if (iter == null) {
            return false;
        }
        iter.reset(UNFOLD_ROW_WIDTH);
        int c;
        do {
            c = iter.next();
            if (c < 0) {
                return false;
            }
            if (c == 775) {
                return true;
            }
        } while (getDotType(c) == OTHER_ACCENT);
        return false;
    }

    public final int toFullUpper(int c, ContextIterator iter, StringBuilder out, ULocale locale, int[] locCache) {
        return toUpperOrTitle(c, iter, out, locale, locCache, true);
    }

    public final int toFullTitle(int c, ContextIterator iter, StringBuilder out, ULocale locale, int[] locCache) {
        return toUpperOrTitle(c, iter, out, locale, locCache, false);
    }

    public final int fold(int c, int options) {
        int props = this.trie.get(c);
        if (propsHasException(props)) {
            int index;
            int excOffset = getExceptionsOffset(props);
            int excOffset2 = excOffset + UNFOLD_ROW_WIDTH;
            int excWord = this.exceptions[excOffset];
            if ((EXC_CONDITIONAL_FOLD & excWord) != 0) {
                if ((options & FOLD_CASE_OPTIONS_MASK) == 0) {
                    if (c == 73) {
                        return Opcodes.OP_SPUT_OBJECT;
                    }
                    if (c == 304) {
                        return c;
                    }
                } else if (c == 73) {
                    return 305;
                } else {
                    if (c == 304) {
                        return Opcodes.OP_SPUT_OBJECT;
                    }
                }
            }
            if (hasSlot(excWord, UNFOLD_ROW_WIDTH)) {
                index = UNFOLD_ROW_WIDTH;
            } else if (!hasSlot(excWord, UNFOLD_ROWS)) {
                return c;
            } else {
                index = UNFOLD_ROWS;
            }
            c = getSlotValue(excWord, index, excOffset2);
        } else if (getTypeFromProps(props) >= UPPER) {
            c += getDelta(props);
        }
        return c;
    }

    public final boolean hasBinaryProperty(int c, int which) {
        boolean z = true;
        switch (which) {
            case IoBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP /*22*/:
                return UNFOLD_ROW_WIDTH == getType(c);
            case Opcodes.OP_CONST_STRING_JUMBO /*27*/:
                return isSoftDotted(c);
            case Opcodes.OP_MONITOR_EXIT /*30*/:
                if (UPPER != getType(c)) {
                    z = false;
                }
                return z;
            case Opcodes.OP_NEW_INSTANCE /*34*/:
                return isCaseSensitive(c);
            case Opcodes.OP_CMP_LONG /*49*/:
                if (getType(c) == 0) {
                    z = false;
                }
                return z;
            case Opcodes.OP_IF_EQ /*50*/:
                if ((getTypeOrIgnorable(c) >> UPPER) == 0) {
                    z = false;
                }
                return z;
            case Opcodes.OP_IF_NE /*51*/:
                dummyStringBuilder.setLength(UNFOLD_ROWS);
                if (toFullLower(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    z = false;
                }
                return z;
            case Opcodes.OP_IF_LT /*52*/:
                dummyStringBuilder.setLength(UNFOLD_ROWS);
                if (toFullUpper(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    z = false;
                }
                return z;
            case Opcodes.OP_IF_GE /*53*/:
                dummyStringBuilder.setLength(UNFOLD_ROWS);
                if (toFullTitle(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    z = false;
                }
                return z;
            case Opcodes.OP_IF_LE /*55*/:
                dummyStringBuilder.setLength(UNFOLD_ROWS);
                if (toFullLower(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                    if (toFullUpper(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                        if (toFullTitle(c, null, dummyStringBuilder, ULocale.ROOT, rootLocCache) < 0) {
                            z = false;
                        }
                    }
                }
                return z;
            default:
                return false;
        }
    }

    private static final int getTypeFromProps(int props) {
        return props & TYPE_MASK;
    }

    private static final int getTypeAndIgnorableFromProps(int props) {
        return props & EXC_FULL_MAPPINGS;
    }

    private static final int getDelta(int props) {
        return ((short) props) >> EXC_FULL_MAPPINGS;
    }
}

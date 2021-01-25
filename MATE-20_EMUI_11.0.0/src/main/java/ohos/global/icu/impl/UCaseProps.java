package ohos.global.icu.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Locale;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.Trie2;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.ULocale;

public final class UCaseProps {
    private static final int ABOVE = 64;
    private static final int CLOSURE_MAX_LENGTH = 15;
    private static final String DATA_FILE_NAME = "ucase.icu";
    private static final String DATA_NAME = "ucase";
    private static final String DATA_TYPE = "icu";
    private static final int DELTA_SHIFT = 7;
    private static final int DOT_MASK = 96;
    private static final int EXCEPTION = 8;
    private static final int EXC_CLOSURE = 6;
    private static final int EXC_CONDITIONAL_FOLD = 32768;
    private static final int EXC_CONDITIONAL_SPECIAL = 16384;
    private static final int EXC_DELTA = 4;
    private static final int EXC_DELTA_IS_NEGATIVE = 1024;
    private static final int EXC_DOT_SHIFT = 7;
    private static final int EXC_DOUBLE_SLOTS = 256;
    private static final int EXC_FOLD = 1;
    private static final int EXC_FULL_MAPPINGS = 7;
    private static final int EXC_LOWER = 0;
    private static final int EXC_NO_SIMPLE_CASE_FOLDING = 512;
    private static final int EXC_SENSITIVE = 2048;
    private static final int EXC_SHIFT = 4;
    private static final int EXC_TITLE = 3;
    private static final int EXC_UPPER = 2;
    private static final int FMT = 1665225541;
    static final int FOLD_CASE_OPTIONS_MASK = 7;
    private static final int FULL_LOWER = 15;
    static final int IGNORABLE = 4;
    public static final UCaseProps INSTANCE;
    private static final int IX_EXC_LENGTH = 3;
    private static final int IX_TOP = 16;
    private static final int IX_TRIE_SIZE = 2;
    private static final int IX_UNFOLD_LENGTH = 4;
    public static final int LOC_DUTCH = 5;
    static final int LOC_GREEK = 4;
    static final int LOC_LITHUANIAN = 3;
    public static final int LOC_ROOT = 1;
    static final int LOC_TURKISH = 2;
    public static final int LOWER = 1;
    public static final int MAX_STRING_LENGTH = 31;
    public static final int NONE = 0;
    private static final int OTHER_ACCENT = 96;
    private static final int SENSITIVE = 16;
    private static final int SOFT_DOTTED = 32;
    public static final int TITLE = 3;
    public static final int TYPE_MASK = 3;
    private static final int UNFOLD_ROWS = 0;
    private static final int UNFOLD_ROW_WIDTH = 1;
    private static final int UNFOLD_STRING_WIDTH = 2;
    public static final int UPPER = 2;
    public static final StringBuilder dummyStringBuilder = new StringBuilder();
    private static final byte[] flagsOffset = {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8};
    private static final String iDot = "i̇";
    private static final String iDotAcute = "i̇́";
    private static final String iDotGrave = "i̇̀";
    private static final String iDotTilde = "i̇̃";
    private static final String iOgonekDot = "į̇";
    private static final String jDot = "j̇";
    private String exceptions;
    private int[] indexes;
    private Trie2_16 trie;
    private char[] unfold;

    public interface ContextIterator {
        int next();

        void reset(int i);
    }

    static final int getDelta(int i) {
        return ((short) i) >> 7;
    }

    private static final int getExceptionsOffset(int i) {
        return i >> 4;
    }

    private static final int getTypeAndIgnorableFromProps(int i) {
        return i & 7;
    }

    static final int getTypeFromProps(int i) {
        return i & 3;
    }

    private static final boolean hasSlot(int i, int i2) {
        return (i & (1 << i2)) != 0;
    }

    static final boolean isUpperOrTitleFromProps(int i) {
        return (i & 2) != 0;
    }

    static final boolean propsHasException(int i) {
        return (i & 8) != 0;
    }

    private UCaseProps() throws IOException {
        readData(ICUBinary.getRequiredData(DATA_FILE_NAME));
    }

    private final void readData(ByteBuffer byteBuffer) throws IOException {
        ICUBinary.readHeader(byteBuffer, FMT, new IsAcceptable());
        int i = byteBuffer.getInt();
        if (i >= 16) {
            this.indexes = new int[i];
            this.indexes[0] = i;
            for (int i2 = 1; i2 < i; i2++) {
                this.indexes[i2] = byteBuffer.getInt();
            }
            this.trie = Trie2_16.createFromSerialized(byteBuffer);
            int i3 = this.indexes[2];
            int serializedLength = this.trie.getSerializedLength();
            if (serializedLength <= i3) {
                ICUBinary.skipBytes(byteBuffer, i3 - serializedLength);
                int i4 = this.indexes[3];
                if (i4 > 0) {
                    this.exceptions = ICUBinary.getString(byteBuffer, i4, 0);
                }
                int i5 = this.indexes[4];
                if (i5 > 0) {
                    this.unfold = ICUBinary.getChars(byteBuffer, i5, 0);
                    return;
                }
                return;
            }
            throw new IOException("ucase.icu: not enough bytes for the trie");
        }
        throw new IOException("indexes[0] too small in ucase.icu");
    }

    /* access modifiers changed from: private */
    public static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override // ohos.global.icu.impl.ICUBinary.Authenticate
        public boolean isDataVersionAcceptable(byte[] bArr) {
            return bArr[0] == 4;
        }
    }

    public final void addPropertyStarts(UnicodeSet unicodeSet) {
        Iterator<Trie2.Range> it = this.trie.iterator();
        while (it.hasNext()) {
            Trie2.Range next = it.next();
            if (!next.leadSurrogate) {
                unicodeSet.add(next.startCodePoint);
            } else {
                return;
            }
        }
    }

    static {
        try {
            INSTANCE = new UCaseProps();
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static final byte slotOffset(int i, int i2) {
        return flagsOffset[i & ((1 << i2) - 1)];
    }

    private final long getSlotValueAndOffset(int i, int i2, int i3) {
        int i4;
        long j;
        if ((i & 256) == 0) {
            int slotOffset = i3 + slotOffset(i, i2);
            j = (long) this.exceptions.charAt(slotOffset);
            i4 = slotOffset;
        } else {
            int slotOffset2 = i3 + (slotOffset(i, i2) * 2);
            i4 = slotOffset2 + 1;
            j = ((long) this.exceptions.charAt(i4)) | (((long) this.exceptions.charAt(slotOffset2)) << 16);
        }
        return j | (((long) i4) << 32);
    }

    private final int getSlotValue(int i, int i2, int i3) {
        if ((i & 256) == 0) {
            return this.exceptions.charAt(i3 + slotOffset(i, i2));
        }
        int slotOffset = i3 + (slotOffset(i, i2) * 2);
        return this.exceptions.charAt(slotOffset + 1) | (this.exceptions.charAt(slotOffset) << 16);
    }

    public final int tolower(int i) {
        int i2 = this.trie.get(i);
        if (propsHasException(i2)) {
            int exceptionsOffset = getExceptionsOffset(i2);
            int i3 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            if (!hasSlot(charAt, 4) || !isUpperOrTitleFromProps(i2)) {
                return hasSlot(charAt, 0) ? getSlotValue(charAt, 0, i3) : i;
            }
            int slotValue = getSlotValue(charAt, 4, i3);
            return (charAt & 1024) == 0 ? i + slotValue : i - slotValue;
        } else if (isUpperOrTitleFromProps(i2)) {
            return i + getDelta(i2);
        } else {
            return i;
        }
    }

    public final int toupper(int i) {
        int i2 = this.trie.get(i);
        if (propsHasException(i2)) {
            int exceptionsOffset = getExceptionsOffset(i2);
            int i3 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            if (!hasSlot(charAt, 4) || getTypeFromProps(i2) != 1) {
                return hasSlot(charAt, 2) ? getSlotValue(charAt, 2, i3) : i;
            }
            int slotValue = getSlotValue(charAt, 4, i3);
            return (charAt & 1024) == 0 ? i + slotValue : i - slotValue;
        } else if (getTypeFromProps(i2) == 1) {
            return i + getDelta(i2);
        } else {
            return i;
        }
    }

    public final int totitle(int i) {
        int i2 = this.trie.get(i);
        if (propsHasException(i2)) {
            int exceptionsOffset = getExceptionsOffset(i2);
            int i3 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            if (!hasSlot(charAt, 4) || getTypeFromProps(i2) != 1) {
                int i4 = 3;
                if (!hasSlot(charAt, 3)) {
                    if (!hasSlot(charAt, 2)) {
                        return i;
                    }
                    i4 = 2;
                }
                return getSlotValue(charAt, i4, i3);
            }
            int slotValue = getSlotValue(charAt, 4, i3);
            return (charAt & 1024) == 0 ? i + slotValue : i - slotValue;
        } else if (getTypeFromProps(i2) == 1) {
            return i + getDelta(i2);
        } else {
            return i;
        }
    }

    public final void addCaseClosure(int i, UnicodeSet unicodeSet) {
        int i2;
        int delta;
        if (i == 73) {
            unicodeSet.add(105);
        } else if (i == 105) {
            unicodeSet.add(73);
        } else if (i == 304) {
            unicodeSet.add(iDot);
        } else if (i != 305) {
            int i3 = this.trie.get(i);
            if (propsHasException(i3)) {
                int exceptionsOffset = getExceptionsOffset(i3);
                int i4 = exceptionsOffset + 1;
                char charAt = this.exceptions.charAt(exceptionsOffset);
                int i5 = 0;
                int i6 = i;
                for (int i7 = 0; i7 <= 3; i7++) {
                    if (hasSlot(charAt, i7)) {
                        i6 = getSlotValue(charAt, i7, i4);
                        unicodeSet.add(i6);
                    }
                }
                if (hasSlot(charAt, 4)) {
                    int slotValue = getSlotValue(charAt, 4, i4);
                    unicodeSet.add((charAt & 1024) == 0 ? i6 + slotValue : i6 - slotValue);
                }
                if (hasSlot(charAt, 6)) {
                    long slotValueAndOffset = getSlotValueAndOffset(charAt, 6, i4);
                    int i8 = ((int) (slotValueAndOffset >> 32)) + 1;
                    i2 = ((int) slotValueAndOffset) & 15;
                    i5 = i8;
                } else {
                    i2 = 0;
                }
                if (hasSlot(charAt, 7)) {
                    long slotValueAndOffset2 = getSlotValueAndOffset(charAt, 7, i4);
                    int i9 = 65535 & ((int) slotValueAndOffset2);
                    int i10 = ((int) (slotValueAndOffset2 >> 32)) + 1 + (i9 & 15);
                    int i11 = i9 >> 4;
                    int i12 = i11 & 15;
                    if (i12 != 0) {
                        int i13 = i12 + i10;
                        unicodeSet.add(this.exceptions.substring(i10, i13));
                        i10 = i13;
                    }
                    int i14 = i11 >> 4;
                    i5 = i10 + (i14 & 15) + (i14 >> 4);
                }
                int i15 = i2 + i5;
                while (i5 < i15) {
                    int codePointAt = this.exceptions.codePointAt(i5);
                    unicodeSet.add(codePointAt);
                    i5 += UTF16.getCharCount(codePointAt);
                }
            } else if (getTypeFromProps(i3) != 0 && (delta = getDelta(i3)) != 0) {
                unicodeSet.add(i + delta);
            }
        }
    }

    private final int strcmpMax(String str, int i, int i2) {
        int length = str.length();
        int i3 = i2 - length;
        int i4 = length;
        int i5 = i;
        int i6 = 0;
        while (true) {
            int i7 = i6 + 1;
            char charAt = str.charAt(i6);
            char[] cArr = this.unfold;
            int i8 = i5 + 1;
            char c = cArr[i5];
            if (c == 0) {
                return 1;
            }
            int i9 = charAt - c;
            if (i9 != 0) {
                return i9;
            }
            i4--;
            if (i4 > 0) {
                i6 = i7;
                i5 = i8;
            } else if (i3 == 0 || cArr[i8] == 0) {
                return 0;
            } else {
                return -i3;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:22:0x001d */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:23:0x002c */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:26:0x001d */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v0, types: [char[]] */
    /* JADX WARN: Type inference failed for: r4v0, types: [char] */
    /* JADX WARN: Type inference failed for: r5v0, types: [char, int] */
    /* JADX WARN: Type inference failed for: r3v1, types: [char, int] */
    /* JADX WARN: Type inference failed for: r4v1 */
    /* JADX WARN: Type inference failed for: r3v2, types: [int] */
    /* JADX WARN: Type inference failed for: r4v2 */
    public final boolean addStringCaseClosure(String str, UnicodeSet unicodeSet) {
        int length;
        if (this.unfold == null || str == null || (length = str.length()) <= 1) {
            return false;
        }
        char[] cArr = this.unfold;
        char c = cArr[0];
        char c2 = cArr[1];
        char c3 = cArr[2];
        if (length > c3) {
            return false;
        }
        int i = 0;
        while (i < c) {
            int i2 = (i + (c == true ? 1 : 0)) / 2;
            int i3 = i2 + 1;
            int i4 = i3 * c2;
            int strcmpMax = strcmpMax(str, i4, c3);
            if (strcmpMax == 0) {
                while (c3 < c2) {
                    char[] cArr2 = this.unfold;
                    if (cArr2[i4 + (c3 == true ? 1 : 0)] == 0) {
                        break;
                    }
                    int charAt = UTF16.charAt(cArr2, i4, cArr2.length, c3);
                    unicodeSet.add(charAt);
                    addCaseClosure(charAt, unicodeSet);
                    c3 += UTF16.getCharCount(charAt);
                }
                return true;
            } else if (strcmpMax < 0) {
                c = i2;
            } else {
                i = i3;
            }
        }
        return false;
    }

    public final int getType(int i) {
        return getTypeFromProps(this.trie.get(i));
    }

    public final int getTypeOrIgnorable(int i) {
        return getTypeAndIgnorableFromProps(this.trie.get(i));
    }

    public final int getDotType(int i) {
        int i2 = this.trie.get(i);
        if (!propsHasException(i2)) {
            return i2 & 96;
        }
        return (this.exceptions.charAt(getExceptionsOffset(i2)) >> 7) & 96;
    }

    public final boolean isSoftDotted(int i) {
        return getDotType(i) == 32;
    }

    public final boolean isCaseSensitive(int i) {
        int i2 = this.trie.get(i);
        return !propsHasException(i2) ? (i2 & 16) != 0 : (this.exceptions.charAt(getExceptionsOffset(i2)) & 2048) != 0;
    }

    static final class LatinCase {
        static final byte EXC = Byte.MIN_VALUE;
        static final char LIMIT = 384;
        static final char LONG_S = 383;
        static final byte[] TO_LOWER_NORMAL = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 0, 32, 32, 32, 32, 32, 32, 32, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, Byte.MIN_VALUE, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, Byte.MIN_VALUE, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, -121, 1, 0, 1, 0, 1, 0, Byte.MIN_VALUE};
        static final byte[] TO_LOWER_TR_LT = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 32, 32, 32, 32, 32, 32, 32, Byte.MIN_VALUE, Byte.MIN_VALUE, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, Byte.MIN_VALUE, Byte.MIN_VALUE, 32, 32, 32, 32, 32, 32, 32, 32, 32, 0, 32, 32, 32, 32, 32, 32, 32, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, Byte.MIN_VALUE, 0, 1, 0, 1, 0, Byte.MIN_VALUE, 0, Byte.MIN_VALUE, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, Byte.MIN_VALUE, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, -121, 1, 0, 1, 0, 1, 0, Byte.MIN_VALUE};
        static final byte[] TO_UPPER_NORMAL = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Byte.MIN_VALUE, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, 0, -32, -32, -32, -32, -32, -32, -32, 121, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, Byte.MIN_VALUE, 0, -1, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, Byte.MIN_VALUE, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, Byte.MIN_VALUE};
        static final byte[] TO_UPPER_TR = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -32, -32, -32, -32, -32, -32, -32, -32, Byte.MIN_VALUE, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Byte.MIN_VALUE, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Byte.MIN_VALUE, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, -32, 0, -32, -32, -32, -32, -32, -32, -32, 121, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, Byte.MIN_VALUE, 0, -1, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, Byte.MIN_VALUE, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, 0, -1, 0, -1, 0, -1, Byte.MIN_VALUE};

        LatinCase() {
        }
    }

    public static final int getCaseLocale(Locale locale) {
        return getCaseLocale(locale.getLanguage());
    }

    public static final int getCaseLocale(ULocale uLocale) {
        return getCaseLocale(uLocale.getLanguage());
    }

    private static final int getCaseLocale(String str) {
        if (str.length() == 2) {
            if (str.equals("en") || str.charAt(0) > 't') {
                return 1;
            }
            if (str.equals("tr") || str.equals("az")) {
                return 2;
            }
            if (str.equals("el")) {
                return 4;
            }
            if (str.equals("lt")) {
                return 3;
            }
            if (str.equals("nl")) {
                return 5;
            }
        } else if (str.length() == 3) {
            if (str.equals("tur") || str.equals("aze")) {
                return 2;
            }
            if (str.equals("ell")) {
                return 4;
            }
            if (str.equals("lit")) {
                return 3;
            }
            if (str.equals("nld")) {
                return 5;
            }
        }
        return 1;
    }

    private final boolean isFollowedByCasedLetter(ContextIterator contextIterator, int i) {
        if (contextIterator == null) {
            return false;
        }
        contextIterator.reset(i);
        while (true) {
            int next = contextIterator.next();
            if (next < 0) {
                break;
            }
            int typeOrIgnorable = getTypeOrIgnorable(next);
            if ((typeOrIgnorable & 4) == 0) {
                if (typeOrIgnorable != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private final boolean isPrecededBySoftDotted(ContextIterator contextIterator) {
        int dotType;
        if (contextIterator == null) {
            return false;
        }
        contextIterator.reset(-1);
        do {
            int next = contextIterator.next();
            if (next < 0) {
                break;
            }
            dotType = getDotType(next);
            if (dotType == 32) {
                return true;
            }
        } while (dotType == 96);
        return false;
    }

    private final boolean isPrecededBy_I(ContextIterator contextIterator) {
        int next;
        if (contextIterator == null) {
            return false;
        }
        contextIterator.reset(-1);
        do {
            next = contextIterator.next();
            if (next < 0) {
                break;
            } else if (next == 73) {
                return true;
            }
        } while (getDotType(next) == 96);
        return false;
    }

    private final boolean isFollowedByMoreAbove(ContextIterator contextIterator) {
        int dotType;
        if (contextIterator == null) {
            return false;
        }
        contextIterator.reset(1);
        do {
            int next = contextIterator.next();
            if (next < 0) {
                break;
            }
            dotType = getDotType(next);
            if (dotType == 64) {
                return true;
            }
        } while (dotType == 96);
        return false;
    }

    private final boolean isFollowedByDotAbove(ContextIterator contextIterator) {
        int next;
        if (contextIterator == null) {
            return false;
        }
        contextIterator.reset(1);
        do {
            next = contextIterator.next();
            if (next < 0) {
                break;
            } else if (next == 775) {
                return true;
            }
        } while (getDotType(next) == 96);
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x012b  */
    public final int toFullLower(int i, ContextIterator contextIterator, Appendable appendable, int i2) {
        int i3;
        int i4 = this.trie.get(i);
        if (propsHasException(i4)) {
            int exceptionsOffset = getExceptionsOffset(i4);
            int i5 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            if ((charAt & 16384) != 0) {
                if (i2 == 3 && (((i == 73 || i == 74 || i == 302) && isFollowedByMoreAbove(contextIterator)) || i == 204 || i == 205 || i == 296)) {
                    if (i == 73) {
                        appendable.append(iDot);
                        return 2;
                    } else if (i == 74) {
                        appendable.append(jDot);
                        return 2;
                    } else if (i == 204) {
                        appendable.append(iDotGrave);
                        return 3;
                    } else if (i == 205) {
                        appendable.append(iDotAcute);
                        return 3;
                    } else if (i == 296) {
                        appendable.append(iDotTilde);
                        return 3;
                    } else if (i != 302) {
                        return 0;
                    } else {
                        try {
                            appendable.append(iOgonekDot);
                            return 2;
                        } catch (IOException e) {
                            throw new ICUUncheckedIOException(e);
                        }
                    }
                } else if (i2 == 2 && i == 304) {
                    return 105;
                } else {
                    if (i2 == 2 && i == 775 && isPrecededBy_I(contextIterator)) {
                        return 0;
                    }
                    if (i2 == 2 && i == 73 && !isFollowedByDotAbove(contextIterator)) {
                        return 305;
                    }
                    if (i == 304) {
                        try {
                            appendable.append(iDot);
                            return 2;
                        } catch (IOException e2) {
                            throw new ICUUncheckedIOException(e2);
                        }
                    } else if (i == 931 && !isFollowedByCasedLetter(contextIterator, 1) && isFollowedByCasedLetter(contextIterator, -1)) {
                        return 962;
                    }
                }
            } else if (hasSlot(charAt, 7)) {
                long slotValueAndOffset = getSlotValueAndOffset(charAt, 7, i5);
                int i6 = ((int) slotValueAndOffset) & 15;
                if (i6 != 0) {
                    int i7 = ((int) (slotValueAndOffset >> 32)) + 1;
                    try {
                        appendable.append(this.exceptions, i7, i7 + i6);
                        return i6;
                    } catch (IOException e3) {
                        throw new ICUUncheckedIOException(e3);
                    }
                }
            }
            if (hasSlot(charAt, 4) && isUpperOrTitleFromProps(i4)) {
                int slotValue = getSlotValue(charAt, 4, i5);
                return (charAt & 1024) == 0 ? slotValue + i : i - slotValue;
            } else if (hasSlot(charAt, 0)) {
                i3 = getSlotValue(charAt, 0, i5);
                if (i3 == i) {
                }
            }
        } else if (isUpperOrTitleFromProps(i4)) {
            i3 = getDelta(i4) + i;
            return i3 == i ? ~i3 : i3;
        }
        i3 = i;
        if (i3 == i) {
        }
    }

    private final int toUpperOrTitle(int i, ContextIterator contextIterator, Appendable appendable, int i2, boolean z) {
        int i3;
        int i4 = this.trie.get(i);
        if (!propsHasException(i4)) {
            i3 = getTypeFromProps(i4) == 1 ? getDelta(i4) + i : i;
        } else {
            int exceptionsOffset = getExceptionsOffset(i4);
            int i5 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            int i6 = 3;
            if ((charAt & 16384) != 0) {
                if (i2 == 2 && i == 105) {
                    return 304;
                }
                if (i2 == 3 && i == 775 && isPrecededBySoftDotted(contextIterator)) {
                    return 0;
                }
            } else if (hasSlot(charAt, 7)) {
                long slotValueAndOffset = getSlotValueAndOffset(charAt, 7, i5);
                int i7 = ((int) slotValueAndOffset) & 65535;
                int i8 = i7 >> 4;
                int i9 = ((int) (slotValueAndOffset >> 32)) + 1 + (i7 & 15) + (i8 & 15);
                int i10 = i8 >> 4;
                if (!z) {
                    i9 += i10 & 15;
                    i10 >>= 4;
                }
                int i11 = i10 & 15;
                if (i11 != 0) {
                    try {
                        appendable.append(this.exceptions, i9, i9 + i11);
                        return i11;
                    } catch (IOException e) {
                        throw new ICUUncheckedIOException(e);
                    }
                }
            }
            if (!hasSlot(charAt, 4) || getTypeFromProps(i4) != 1) {
                if (z || !hasSlot(charAt, 3)) {
                    if (!hasSlot(charAt, 2)) {
                        return ~i;
                    }
                    i6 = 2;
                }
                i3 = getSlotValue(charAt, i6, i5);
            } else {
                int slotValue = getSlotValue(charAt, 4, i5);
                return (charAt & 1024) == 0 ? i + slotValue : i - slotValue;
            }
        }
        return i3 == i ? ~i3 : i3;
    }

    public final int toFullUpper(int i, ContextIterator contextIterator, Appendable appendable, int i2) {
        return toUpperOrTitle(i, contextIterator, appendable, i2, true);
    }

    public final int toFullTitle(int i, ContextIterator contextIterator, Appendable appendable, int i2) {
        return toUpperOrTitle(i, contextIterator, appendable, i2, false);
    }

    public final int fold(int i, int i2) {
        int i3 = this.trie.get(i);
        if (propsHasException(i3)) {
            int exceptionsOffset = getExceptionsOffset(i3);
            int i4 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            if ((32768 & charAt) != 0) {
                if ((i2 & 7) == 0) {
                    if (i == 73) {
                        return 105;
                    }
                    if (i == 304) {
                        return i;
                    }
                } else if (i == 73) {
                    return 305;
                } else {
                    if (i == 304) {
                        return 105;
                    }
                }
            }
            if ((charAt & 512) != 0) {
                return i;
            }
            if (!hasSlot(charAt, 4) || !isUpperOrTitleFromProps(i3)) {
                int i5 = 1;
                if (!hasSlot(charAt, 1)) {
                    if (!hasSlot(charAt, 0)) {
                        return i;
                    }
                    i5 = 0;
                }
                return getSlotValue(charAt, i5, i4);
            }
            int slotValue = getSlotValue(charAt, 4, i4);
            return (charAt & 1024) == 0 ? i + slotValue : i - slotValue;
        } else if (isUpperOrTitleFromProps(i3)) {
            return i + getDelta(i3);
        } else {
            return i;
        }
    }

    public final int toFullFolding(int i, Appendable appendable, int i2) {
        int i3;
        int i4 = this.trie.get(i);
        if (!propsHasException(i4)) {
            i3 = isUpperOrTitleFromProps(i4) ? getDelta(i4) + i : i;
        } else {
            int exceptionsOffset = getExceptionsOffset(i4);
            int i5 = exceptionsOffset + 1;
            char charAt = this.exceptions.charAt(exceptionsOffset);
            if ((32768 & charAt) != 0) {
                if ((i2 & 7) == 0) {
                    if (i == 73) {
                        return 105;
                    }
                    if (i == 304) {
                        try {
                            appendable.append(iDot);
                            return 2;
                        } catch (IOException e) {
                            throw new ICUUncheckedIOException(e);
                        }
                    }
                } else if (i == 73) {
                    return 305;
                } else {
                    if (i == 304) {
                        return 105;
                    }
                }
            } else if (hasSlot(charAt, 7)) {
                long slotValueAndOffset = getSlotValueAndOffset(charAt, 7, i5);
                int i6 = ((int) slotValueAndOffset) & 65535;
                int i7 = ((int) (slotValueAndOffset >> 32)) + 1 + (i6 & 15);
                int i8 = (i6 >> 4) & 15;
                if (i8 != 0) {
                    try {
                        appendable.append(this.exceptions, i7, i7 + i8);
                        return i8;
                    } catch (IOException e2) {
                        throw new ICUUncheckedIOException(e2);
                    }
                }
            }
            if ((charAt & 512) != 0) {
                return ~i;
            }
            if (!hasSlot(charAt, 4) || !isUpperOrTitleFromProps(i4)) {
                int i9 = 0;
                if (hasSlot(charAt, 1)) {
                    i9 = 1;
                } else if (!hasSlot(charAt, 0)) {
                    return ~i;
                }
                i3 = getSlotValue(charAt, i9, i5);
            } else {
                int slotValue = getSlotValue(charAt, 4, i5);
                return (charAt & 1024) == 0 ? i + slotValue : i - slotValue;
            }
        }
        return i3 == i ? ~i3 : i3;
    }

    public final boolean hasBinaryProperty(int i, int i2) {
        if (i2 == 22) {
            return 1 == getType(i);
        }
        if (i2 == 27) {
            return isSoftDotted(i);
        }
        if (i2 == 30) {
            return 2 == getType(i);
        }
        if (i2 == 34) {
            return isCaseSensitive(i);
        }
        if (i2 != 55) {
            switch (i2) {
                case 49:
                    return getType(i) != 0;
                case 50:
                    return (getTypeOrIgnorable(i) >> 2) != 0;
                case 51:
                    dummyStringBuilder.setLength(0);
                    return toFullLower(i, null, dummyStringBuilder, 1) >= 0;
                case 52:
                    dummyStringBuilder.setLength(0);
                    return toFullUpper(i, null, dummyStringBuilder, 1) >= 0;
                case 53:
                    dummyStringBuilder.setLength(0);
                    return toFullTitle(i, null, dummyStringBuilder, 1) >= 0;
                default:
                    return false;
            }
        } else {
            dummyStringBuilder.setLength(0);
            return toFullLower(i, null, dummyStringBuilder, 1) >= 0 || toFullUpper(i, null, dummyStringBuilder, 1) >= 0 || toFullTitle(i, null, dummyStringBuilder, 1) >= 0;
        }
    }

    static Trie2_16 getTrie() {
        return INSTANCE.trie;
    }
}

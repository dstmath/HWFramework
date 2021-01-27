package ohos.global.icu.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.util.CodePointMap;
import ohos.hiaivision.visionutil.internal.DetectBaseType;

public abstract class CodePointTrie extends CodePointMap {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ASCII_LIMIT = 128;
    private static final int BMP_INDEX_LENGTH = 1024;
    static final int CP_PER_INDEX_2_ENTRY = 512;
    private static final int ERROR_VALUE_NEG_DATA_OFFSET = 1;
    static final int FAST_DATA_BLOCK_LENGTH = 64;
    private static final int FAST_DATA_MASK = 63;
    static final int FAST_SHIFT = 6;
    private static final int HIGH_VALUE_NEG_DATA_OFFSET = 2;
    static final int INDEX_2_BLOCK_LENGTH = 32;
    static final int INDEX_2_MASK = 31;
    static final int INDEX_3_BLOCK_LENGTH = 32;
    private static final int INDEX_3_MASK = 31;
    private static final int MAX_UNICODE = 1114111;
    static final int NO_DATA_NULL_OFFSET = 1048575;
    static final int NO_INDEX3_NULL_OFFSET = 32767;
    private static final int OMITTED_BMP_INDEX_1_LENGTH = 4;
    private static final int OPTIONS_DATA_LENGTH_MASK = 61440;
    private static final int OPTIONS_DATA_NULL_OFFSET_MASK = 3840;
    private static final int OPTIONS_RESERVED_MASK = 56;
    private static final int OPTIONS_VALUE_BITS_MASK = 7;
    private static final int SHIFT_1 = 14;
    static final int SHIFT_1_2 = 5;
    private static final int SHIFT_2 = 9;
    static final int SHIFT_2_3 = 5;
    static final int SHIFT_3 = 4;
    static final int SMALL_DATA_BLOCK_LENGTH = 16;
    static final int SMALL_DATA_MASK = 15;
    private static final int SMALL_INDEX_LENGTH = 64;
    static final int SMALL_LIMIT = 4096;
    private static final int SMALL_MAX = 4095;
    private final int[] ascii;
    @Deprecated
    protected final Data data;
    @Deprecated
    protected final int dataLength;
    private final int dataNullOffset;
    @Deprecated
    protected final int highStart;
    private final char[] index;
    private final int index3NullOffset;
    private final int nullValue;

    public enum Type {
        FAST,
        SMALL
    }

    public enum ValueWidth {
        BITS_16,
        BITS_32,
        BITS_8
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public abstract int cpIndex(int i);

    public abstract Type getType();

    /* synthetic */ CodePointTrie(char[] cArr, Data data2, int i, int i2, int i3, AnonymousClass1 r6) {
        this(cArr, data2, i, i2, i3);
    }

    private CodePointTrie(char[] cArr, Data data2, int i, int i2, int i3) {
        this.ascii = new int[128];
        this.index = cArr;
        this.data = data2;
        this.dataLength = data2.getDataLength();
        this.highStart = i;
        this.index3NullOffset = i2;
        this.dataNullOffset = i3;
        for (int i4 = 0; i4 < 128; i4++) {
            this.ascii[i4] = data2.getFromIndex(i4);
        }
        int i5 = this.dataLength;
        this.nullValue = data2.getFromIndex(i3 >= i5 ? i5 - 2 : i3);
    }

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00b9 A[Catch:{ all -> 0x0144 }] */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0124  */
    public static CodePointTrie fromBinary(Type type, ValueWidth valueWidth, ByteBuffer byteBuffer) {
        Type type2;
        ValueWidth valueWidth2;
        int i;
        CodePointTrie codePointTrie;
        CodePointTrie codePointTrie2;
        CodePointTrie codePointTrie3;
        int i2;
        ByteOrder byteOrder;
        ByteOrder order = byteBuffer.order();
        try {
            if (byteBuffer.remaining() >= 16) {
                int i3 = byteBuffer.getInt();
                if (i3 == 862548564) {
                    if (order == ByteOrder.BIG_ENDIAN) {
                        byteOrder = ByteOrder.LITTLE_ENDIAN;
                    } else {
                        byteOrder = ByteOrder.BIG_ENDIAN;
                    }
                    byteBuffer.order(byteOrder);
                } else if (i3 != 1416784179) {
                    throw new ICUUncheckedIOException("Buffer does not contain a serialized CodePointTrie");
                }
                char c = byteBuffer.getChar();
                char c2 = byteBuffer.getChar();
                char c3 = byteBuffer.getChar();
                char c4 = byteBuffer.getChar();
                char c5 = byteBuffer.getChar();
                char c6 = byteBuffer.getChar();
                int i4 = (c >> 6) & 3;
                if (i4 == 0) {
                    type2 = Type.FAST;
                } else if (i4 == 1) {
                    type2 = Type.SMALL;
                } else {
                    throw new ICUUncheckedIOException("CodePointTrie data header has an unsupported type");
                }
                int i5 = c & 7;
                if (i5 == 0) {
                    valueWidth2 = ValueWidth.BITS_16;
                } else if (i5 == 1) {
                    valueWidth2 = ValueWidth.BITS_32;
                } else if (i5 == 2) {
                    valueWidth2 = ValueWidth.BITS_8;
                } else {
                    throw new ICUUncheckedIOException("CodePointTrie data header has an unsupported value width");
                }
                if ((c & '8') == 0) {
                    Type type3 = type == null ? type2 : type;
                    ValueWidth valueWidth3 = valueWidth == null ? valueWidth2 : valueWidth;
                    if (type3 == type2 && valueWidth3 == valueWidth2) {
                        int i6 = c3 | ((OPTIONS_DATA_LENGTH_MASK & c) << 4);
                        int i7 = c5 | ((c & 3840) << 8);
                        int i8 = c6 << '\t';
                        int i9 = c2 * 2;
                        if (valueWidth3 == ValueWidth.BITS_16) {
                            i2 = i6 * 2;
                        } else if (valueWidth3 == ValueWidth.BITS_32) {
                            i2 = i6 * 4;
                        } else {
                            i = i9 + i6;
                            if (byteBuffer.remaining() < i) {
                                char[] chars = ICUBinary.getChars(byteBuffer, c2, 0);
                                int i10 = AnonymousClass1.$SwitchMap$ohos$global$icu$util$CodePointTrie$ValueWidth[valueWidth3.ordinal()];
                                if (i10 == 1) {
                                    char[] chars2 = ICUBinary.getChars(byteBuffer, i6, 0);
                                    if (type3 == Type.FAST) {
                                        codePointTrie = new Fast16(chars, chars2, i8, c4, i7);
                                    } else {
                                        codePointTrie = new Small16(chars, chars2, i8, c4, i7);
                                    }
                                    byteBuffer.order(order);
                                    return codePointTrie;
                                } else if (i10 == 2) {
                                    int[] ints = ICUBinary.getInts(byteBuffer, i6, 0);
                                    if (type3 == Type.FAST) {
                                        codePointTrie2 = new Fast32(chars, ints, i8, c4, i7);
                                    } else {
                                        codePointTrie2 = new Small32(chars, ints, i8, c4, i7);
                                    }
                                    byteBuffer.order(order);
                                    return codePointTrie2;
                                } else if (i10 == 3) {
                                    byte[] bytes = ICUBinary.getBytes(byteBuffer, i6, 0);
                                    if (type3 == Type.FAST) {
                                        codePointTrie3 = new Fast8(chars, bytes, i8, c4, i7);
                                    } else {
                                        codePointTrie3 = new Small8(chars, bytes, i8, c4, i7);
                                    }
                                    return codePointTrie3;
                                } else {
                                    throw new AssertionError("should be unreachable");
                                }
                            } else {
                                throw new ICUUncheckedIOException("Buffer too short for the CodePointTrie data");
                            }
                        }
                        i = i9 + i2;
                        if (byteBuffer.remaining() < i) {
                        }
                    } else {
                        throw new ICUUncheckedIOException("CodePointTrie data header has a different type or value width than required");
                    }
                } else {
                    throw new ICUUncheckedIOException("CodePointTrie data header has unsupported options");
                }
            } else {
                throw new ICUUncheckedIOException("Buffer too short for a CodePointTrie header");
            }
        } finally {
            byteBuffer.order(order);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.util.CodePointTrie$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$CodePointTrie$ValueWidth = new int[ValueWidth.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$util$CodePointTrie$ValueWidth[ValueWidth.BITS_16.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$CodePointTrie$ValueWidth[ValueWidth.BITS_32.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$CodePointTrie$ValueWidth[ValueWidth.BITS_8.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public final ValueWidth getValueWidth() {
        return this.data.getValueWidth();
    }

    @Override // ohos.global.icu.util.CodePointMap
    public int get(int i) {
        return this.data.getFromIndex(cpIndex(i));
    }

    public final int asciiGet(int i) {
        return this.ascii[i];
    }

    private static final int maybeFilterValue(int i, int i2, int i3, CodePointMap.ValueFilter valueFilter) {
        if (i == i2) {
            return i3;
        }
        return valueFilter != null ? valueFilter.apply(i) : i;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:67:0x011a, code lost:
        r26.set(r24, r10 - 1, r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0121, code lost:
        return true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x015f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00b6  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x00e8  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x00fd  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0138  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x0186 A[LOOP:1: B:44:0x00a6->B:95:0x0186, LOOP_END] */
    @Override // ohos.global.icu.util.CodePointMap
    public final boolean getRange(int i, CodePointMap.ValueFilter valueFilter, CodePointMap.Range range) {
        char c;
        int i2;
        int i3;
        int i4;
        char c2;
        char c3;
        char c4;
        boolean z;
        boolean z2;
        int fromIndex;
        if (i < 0 || 1114111 < i) {
            return false;
        }
        boolean z3 = true;
        if (i >= this.highStart) {
            int fromIndex2 = this.data.getFromIndex(this.dataLength - 2);
            if (valueFilter != null) {
                fromIndex2 = valueFilter.apply(fromIndex2);
            }
            range.set(i, 1114111, fromIndex2);
            return true;
        }
        int i5 = this.nullValue;
        if (valueFilter != null) {
            i5 = valueFilter.apply(i5);
        }
        Type type = getType();
        int i6 = i;
        char c5 = 65535;
        boolean z4 = false;
        int i7 = 0;
        int i8 = -1;
        int i9 = 0;
        loop0:
        while (true) {
            if (i6 > 65535 || (type != Type.FAST && i6 > SMALL_MAX)) {
                int i10 = i6 >> 14;
                int i11 = type == Type.FAST ? i10 + 1020 : i10 + 64;
                char[] cArr = this.index;
                c2 = cArr[cArr[i11] + ((i6 >> 9) & 31)];
                if (c2 == c5 && i6 - i >= 512) {
                    i6 += 512;
                } else if (c2 == this.index3NullOffset) {
                    if (!z4) {
                        i9 = this.nullValue;
                        i7 = i5;
                        boolean z5 = z3 ? 1 : 0;
                        boolean z6 = z3 ? 1 : 0;
                        boolean z7 = z3 ? 1 : 0;
                        z4 = z5;
                    } else if (i5 != i7) {
                        int i12 = z3 ? 1 : 0;
                        int i13 = z3 ? 1 : 0;
                        int i14 = z3 ? 1 : 0;
                        range.set(i, i6 - i12, i7);
                        return z3;
                    }
                    c5 = c2;
                    i8 = this.dataNullOffset;
                    i6 = (i6 + 512) & -512;
                } else {
                    i4 = (i6 >> 4) & 31;
                    i3 = 32;
                    i2 = 16;
                    c = c2;
                    while (true) {
                        if ((c2 & 32768) != 0) {
                            c3 = c2;
                            c4 = this.index[c2 + i4];
                        } else {
                            int i15 = (c2 & 32767) + (i4 & -8) + (i4 >> 3);
                            int i16 = i4 & 7;
                            c3 = c2;
                            char[] cArr2 = this.index;
                            c4 = ((cArr2[i15] << ((i16 * 2) + 2)) & DetectBaseType.BASE_TYPE_TEXT_DETECT) | cArr2[i15 + 1 + i16];
                        }
                        if (c4 == i8 || i6 - i < i2) {
                            int i17 = i2 - 1;
                            if (c4 != this.dataNullOffset) {
                                if (!z4) {
                                    i9 = this.nullValue;
                                    i7 = i5;
                                    z4 = true;
                                } else if (i5 != i7) {
                                    range.set(i, i6 - 1, i7);
                                    return true;
                                }
                                i6 = (~i17) & (i6 + i2);
                                i8 = c4;
                            } else {
                                int i18 = (i6 & i17) + c4;
                                int fromIndex3 = this.data.getFromIndex(i18);
                                if (z4) {
                                    if (fromIndex3 == i9) {
                                        z = true;
                                        z2 = z4;
                                        while (true) {
                                            i6++;
                                            if ((i6 & i17) != 0) {
                                                i18++;
                                                fromIndex = this.data.getFromIndex(i18);
                                                if (fromIndex == i9) {
                                                    break;
                                                }
                                                if (valueFilter == null || maybeFilterValue(fromIndex, this.nullValue, i5, valueFilter) != i7) {
                                                    break loop0;
                                                }
                                                i9 = fromIndex;
                                            }
                                            z4 = z2;
                                            i8 = c4;
                                            break;
                                        }
                                    } else if (valueFilter == null || maybeFilterValue(fromIndex3, this.nullValue, i5, valueFilter) != i7) {
                                        break loop0;
                                    } else {
                                        i9 = fromIndex3;
                                        z2 = z4;
                                    }
                                } else {
                                    z = true;
                                    i9 = fromIndex3;
                                    i7 = maybeFilterValue(fromIndex3, this.nullValue, i5, valueFilter);
                                    z2 = true;
                                    while (true) {
                                        i6++;
                                        if ((i6 & i17) != 0) {
                                        }
                                        z4 = z2;
                                        i8 = c4;
                                        i9 = fromIndex;
                                    }
                                }
                                z = true;
                                while (true) {
                                    i6++;
                                    if ((i6 & i17) != 0) {
                                    }
                                    z4 = z2;
                                    i8 = c4;
                                    i9 = fromIndex;
                                }
                            }
                        } else {
                            i6 += i2;
                        }
                        i4++;
                        if (i4 < i3) {
                            c5 = c;
                            break;
                        }
                        c2 = c3;
                    }
                }
            } else {
                i4 = i6 >> 6;
                i3 = type == Type.FAST ? 1024 : 64;
                c = c5;
                i2 = 64;
                c2 = 0;
                while (true) {
                    if ((c2 & 32768) != 0) {
                    }
                    if (c4 == i8) {
                    }
                    int i172 = i2 - 1;
                    if (c4 != this.dataNullOffset) {
                    }
                    i4++;
                    if (i4 < i3) {
                    }
                    c2 = c3;
                }
            }
            if (i6 >= this.highStart) {
                range.set(i, maybeFilterValue(this.data.getFromIndex(this.dataLength + -2), this.nullValue, i5, valueFilter) != i7 ? i6 - 1 : 1114111, i7);
                return true;
            }
            z3 = true;
        }
        range.set(i, i6 - 1, i7);
        return z;
    }

    public final int toBinary(OutputStream outputStream) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeInt(1416784179);
            dataOutputStream.writeChar(((this.dataLength & 983040) >> 4) | ((983040 & this.dataNullOffset) >> 8) | (getType().ordinal() << 6) | getValueWidth().ordinal());
            dataOutputStream.writeChar(this.index.length);
            dataOutputStream.writeChar(this.dataLength);
            dataOutputStream.writeChar(this.index3NullOffset);
            dataOutputStream.writeChar(this.dataNullOffset);
            dataOutputStream.writeChar(this.highStart >> 9);
            for (char c : this.index) {
                dataOutputStream.writeChar(c);
            }
            return 16 + (this.index.length * 2) + this.data.write(dataOutputStream);
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    /* access modifiers changed from: private */
    public static abstract class Data {
        /* access modifiers changed from: package-private */
        public abstract int getDataLength();

        /* access modifiers changed from: package-private */
        public abstract int getFromIndex(int i);

        /* access modifiers changed from: package-private */
        public abstract ValueWidth getValueWidth();

        /* access modifiers changed from: package-private */
        public abstract int write(DataOutputStream dataOutputStream) throws IOException;

        private Data() {
        }

        /* synthetic */ Data(AnonymousClass1 r1) {
            this();
        }
    }

    private static final class Data16 extends Data {
        char[] array;

        Data16(char[] cArr) {
            super(null);
            this.array = cArr;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public ValueWidth getValueWidth() {
            return ValueWidth.BITS_16;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int getDataLength() {
            return this.array.length;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int getFromIndex(int i) {
            return this.array[i];
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int write(DataOutputStream dataOutputStream) throws IOException {
            for (char c : this.array) {
                dataOutputStream.writeChar(c);
            }
            return this.array.length * 2;
        }
    }

    private static final class Data32 extends Data {
        int[] array;

        Data32(int[] iArr) {
            super(null);
            this.array = iArr;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public ValueWidth getValueWidth() {
            return ValueWidth.BITS_32;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int getDataLength() {
            return this.array.length;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int getFromIndex(int i) {
            return this.array[i];
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int write(DataOutputStream dataOutputStream) throws IOException {
            for (int i : this.array) {
                dataOutputStream.writeInt(i);
            }
            return this.array.length * 4;
        }
    }

    private static final class Data8 extends Data {
        byte[] array;

        Data8(byte[] bArr) {
            super(null);
            this.array = bArr;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public ValueWidth getValueWidth() {
            return ValueWidth.BITS_8;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int getDataLength() {
            return this.array.length;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int getFromIndex(int i) {
            return this.array[i] & 255;
        }

        /* access modifiers changed from: package-private */
        @Override // ohos.global.icu.util.CodePointTrie.Data
        public int write(DataOutputStream dataOutputStream) throws IOException {
            for (byte b : this.array) {
                dataOutputStream.writeByte(b);
            }
            return this.array.length;
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public final int fastIndex(int i) {
        return this.index[i >> 6] + (i & FAST_DATA_MASK);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public final int smallIndex(Type type, int i) {
        if (i >= this.highStart) {
            return this.dataLength - 2;
        }
        return internalSmallIndex(type, i);
    }

    private final int internalSmallIndex(Type type, int i) {
        char c;
        int i2 = i >> 14;
        int i3 = type == Type.FAST ? i2 + 1020 : i2 + 64;
        char[] cArr = this.index;
        char c2 = cArr[cArr[i3] + ((i >> 9) & 31)];
        int i4 = (i >> 4) & 31;
        if ((32768 & c2) == 0) {
            c = cArr[c2 + i4];
        } else {
            int i5 = (c2 & 32767) + (i4 & -8) + (i4 >> 3);
            int i6 = i4 & 7;
            c = cArr[i5 + 1 + i6] | ((cArr[i5] << ((i6 * 2) + 2)) & DetectBaseType.BASE_TYPE_TEXT_DETECT);
        }
        return c + (i & 15);
    }

    public static abstract class Fast extends CodePointTrie {
        public abstract int bmpGet(int i);

        public abstract int suppGet(int i);

        /* synthetic */ Fast(char[] cArr, Data data, int i, int i2, int i3, AnonymousClass1 r6) {
            this(cArr, data, i, i2, i3);
        }

        private Fast(char[] cArr, Data data, int i, int i2, int i3) {
            super(cArr, data, i, i2, i3, null);
        }

        public static Fast fromBinary(ValueWidth valueWidth, ByteBuffer byteBuffer) {
            return (Fast) CodePointTrie.fromBinary(Type.FAST, valueWidth, byteBuffer);
        }

        @Override // ohos.global.icu.util.CodePointTrie
        public final Type getType() {
            return Type.FAST;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.util.CodePointTrie
        @Deprecated
        public final int cpIndex(int i) {
            if (i >= 0) {
                if (i <= 65535) {
                    return fastIndex(i);
                }
                if (i <= 1114111) {
                    return smallIndex(Type.FAST, i);
                }
            }
            return this.dataLength - 1;
        }

        @Override // ohos.global.icu.util.CodePointMap
        public final CodePointMap.StringIterator stringIterator(CharSequence charSequence, int i) {
            return new FastStringIterator(this, charSequence, i, null);
        }

        private final class FastStringIterator extends CodePointMap.StringIterator {
            /* synthetic */ FastStringIterator(Fast fast, CharSequence charSequence, int i, AnonymousClass1 r4) {
                this(charSequence, i);
            }

            private FastStringIterator(CharSequence charSequence, int i) {
                super(charSequence, i);
            }

            @Override // ohos.global.icu.util.CodePointMap.StringIterator
            public boolean next() {
                int i;
                if (this.sIndex >= this.s.length()) {
                    return false;
                }
                CharSequence charSequence = this.s;
                int i2 = this.sIndex;
                this.sIndex = i2 + 1;
                char charAt = charSequence.charAt(i2);
                this.c = charAt;
                if (!Character.isSurrogate(charAt)) {
                    i = Fast.this.fastIndex(this.c);
                } else {
                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(charAt) && this.sIndex < this.s.length()) {
                        char charAt2 = this.s.charAt(this.sIndex);
                        if (Character.isLowSurrogate(charAt2)) {
                            this.sIndex++;
                            this.c = Character.toCodePoint(charAt, charAt2);
                            i = Fast.this.smallIndex(Type.FAST, this.c);
                        }
                    }
                    i = Fast.this.dataLength - 1;
                }
                this.value = Fast.this.data.getFromIndex(i);
                return true;
            }

            @Override // ohos.global.icu.util.CodePointMap.StringIterator
            public boolean previous() {
                int i;
                if (this.sIndex <= 0) {
                    return false;
                }
                CharSequence charSequence = this.s;
                int i2 = this.sIndex - 1;
                this.sIndex = i2;
                char charAt = charSequence.charAt(i2);
                this.c = charAt;
                if (!Character.isSurrogate(charAt)) {
                    i = Fast.this.fastIndex(this.c);
                } else {
                    if (!Normalizer2Impl.UTF16Plus.isSurrogateLead(charAt) && this.sIndex > 0) {
                        char charAt2 = this.s.charAt(this.sIndex - 1);
                        if (Character.isHighSurrogate(charAt2)) {
                            this.sIndex--;
                            this.c = Character.toCodePoint(charAt2, charAt);
                            i = Fast.this.smallIndex(Type.FAST, this.c);
                        }
                    }
                    i = Fast.this.dataLength - 1;
                }
                this.value = Fast.this.data.getFromIndex(i);
                return true;
            }
        }
    }

    public static abstract class Small extends CodePointTrie {
        /* synthetic */ Small(char[] cArr, Data data, int i, int i2, int i3, AnonymousClass1 r6) {
            this(cArr, data, i, i2, i3);
        }

        private Small(char[] cArr, Data data, int i, int i2, int i3) {
            super(cArr, data, i, i2, i3, null);
        }

        public static Small fromBinary(ValueWidth valueWidth, ByteBuffer byteBuffer) {
            return (Small) CodePointTrie.fromBinary(Type.SMALL, valueWidth, byteBuffer);
        }

        @Override // ohos.global.icu.util.CodePointTrie
        public final Type getType() {
            return Type.SMALL;
        }

        /* access modifiers changed from: protected */
        @Override // ohos.global.icu.util.CodePointTrie
        @Deprecated
        public final int cpIndex(int i) {
            if (i >= 0) {
                if (i <= CodePointTrie.SMALL_MAX) {
                    return fastIndex(i);
                }
                if (i <= 1114111) {
                    return smallIndex(Type.SMALL, i);
                }
            }
            return this.dataLength - 1;
        }

        @Override // ohos.global.icu.util.CodePointMap
        public final CodePointMap.StringIterator stringIterator(CharSequence charSequence, int i) {
            return new SmallStringIterator(this, charSequence, i, null);
        }

        private final class SmallStringIterator extends CodePointMap.StringIterator {
            /* synthetic */ SmallStringIterator(Small small, CharSequence charSequence, int i, AnonymousClass1 r4) {
                this(charSequence, i);
            }

            private SmallStringIterator(CharSequence charSequence, int i) {
                super(charSequence, i);
            }

            @Override // ohos.global.icu.util.CodePointMap.StringIterator
            public boolean next() {
                int i;
                if (this.sIndex >= this.s.length()) {
                    return false;
                }
                CharSequence charSequence = this.s;
                int i2 = this.sIndex;
                this.sIndex = i2 + 1;
                char charAt = charSequence.charAt(i2);
                this.c = charAt;
                if (!Character.isSurrogate(charAt)) {
                    i = Small.this.cpIndex(this.c);
                } else {
                    if (Normalizer2Impl.UTF16Plus.isSurrogateLead(charAt) && this.sIndex < this.s.length()) {
                        char charAt2 = this.s.charAt(this.sIndex);
                        if (Character.isLowSurrogate(charAt2)) {
                            this.sIndex++;
                            this.c = Character.toCodePoint(charAt, charAt2);
                            i = Small.this.smallIndex(Type.SMALL, this.c);
                        }
                    }
                    i = Small.this.dataLength - 1;
                }
                this.value = Small.this.data.getFromIndex(i);
                return true;
            }

            @Override // ohos.global.icu.util.CodePointMap.StringIterator
            public boolean previous() {
                int i;
                if (this.sIndex <= 0) {
                    return false;
                }
                CharSequence charSequence = this.s;
                int i2 = this.sIndex - 1;
                this.sIndex = i2;
                char charAt = charSequence.charAt(i2);
                this.c = charAt;
                if (!Character.isSurrogate(charAt)) {
                    i = Small.this.cpIndex(this.c);
                } else {
                    if (!Normalizer2Impl.UTF16Plus.isSurrogateLead(charAt) && this.sIndex > 0) {
                        char charAt2 = this.s.charAt(this.sIndex - 1);
                        if (Character.isHighSurrogate(charAt2)) {
                            this.sIndex--;
                            this.c = Character.toCodePoint(charAt2, charAt);
                            i = Small.this.smallIndex(Type.SMALL, this.c);
                        }
                    }
                    i = Small.this.dataLength - 1;
                }
                this.value = Small.this.data.getFromIndex(i);
                return true;
            }
        }
    }

    public static final class Fast16 extends Fast {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final char[] dataArray;

        Fast16(char[] cArr, char[] cArr2, int i, int i2, int i3) {
            super(cArr, new Data16(cArr2), i, i2, i3, null);
            this.dataArray = cArr2;
        }

        public static Fast16 fromBinary(ByteBuffer byteBuffer) {
            return (Fast16) CodePointTrie.fromBinary(Type.FAST, ValueWidth.BITS_16, byteBuffer);
        }

        @Override // ohos.global.icu.util.CodePointTrie, ohos.global.icu.util.CodePointMap
        public final int get(int i) {
            return this.dataArray[cpIndex(i)];
        }

        @Override // ohos.global.icu.util.CodePointTrie.Fast
        public final int bmpGet(int i) {
            return this.dataArray[fastIndex(i)];
        }

        @Override // ohos.global.icu.util.CodePointTrie.Fast
        public final int suppGet(int i) {
            return this.dataArray[smallIndex(Type.FAST, i)];
        }
    }

    public static final class Fast32 extends Fast {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final int[] dataArray;

        Fast32(char[] cArr, int[] iArr, int i, int i2, int i3) {
            super(cArr, new Data32(iArr), i, i2, i3, null);
            this.dataArray = iArr;
        }

        public static Fast32 fromBinary(ByteBuffer byteBuffer) {
            return (Fast32) CodePointTrie.fromBinary(Type.FAST, ValueWidth.BITS_32, byteBuffer);
        }

        @Override // ohos.global.icu.util.CodePointTrie, ohos.global.icu.util.CodePointMap
        public final int get(int i) {
            return this.dataArray[cpIndex(i)];
        }

        @Override // ohos.global.icu.util.CodePointTrie.Fast
        public final int bmpGet(int i) {
            return this.dataArray[fastIndex(i)];
        }

        @Override // ohos.global.icu.util.CodePointTrie.Fast
        public final int suppGet(int i) {
            return this.dataArray[smallIndex(Type.FAST, i)];
        }
    }

    public static final class Fast8 extends Fast {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final byte[] dataArray;

        Fast8(char[] cArr, byte[] bArr, int i, int i2, int i3) {
            super(cArr, new Data8(bArr), i, i2, i3, null);
            this.dataArray = bArr;
        }

        public static Fast8 fromBinary(ByteBuffer byteBuffer) {
            return (Fast8) CodePointTrie.fromBinary(Type.FAST, ValueWidth.BITS_8, byteBuffer);
        }

        @Override // ohos.global.icu.util.CodePointTrie, ohos.global.icu.util.CodePointMap
        public final int get(int i) {
            return this.dataArray[cpIndex(i)] & 255;
        }

        @Override // ohos.global.icu.util.CodePointTrie.Fast
        public final int bmpGet(int i) {
            return this.dataArray[fastIndex(i)] & 255;
        }

        @Override // ohos.global.icu.util.CodePointTrie.Fast
        public final int suppGet(int i) {
            return this.dataArray[smallIndex(Type.FAST, i)] & 255;
        }
    }

    public static final class Small16 extends Small {
        Small16(char[] cArr, char[] cArr2, int i, int i2, int i3) {
            super(cArr, new Data16(cArr2), i, i2, i3, null);
        }

        public static Small16 fromBinary(ByteBuffer byteBuffer) {
            return (Small16) CodePointTrie.fromBinary(Type.SMALL, ValueWidth.BITS_16, byteBuffer);
        }
    }

    public static final class Small32 extends Small {
        Small32(char[] cArr, int[] iArr, int i, int i2, int i3) {
            super(cArr, new Data32(iArr), i, i2, i3, null);
        }

        public static Small32 fromBinary(ByteBuffer byteBuffer) {
            return (Small32) CodePointTrie.fromBinary(Type.SMALL, ValueWidth.BITS_32, byteBuffer);
        }
    }

    public static final class Small8 extends Small {
        Small8(char[] cArr, byte[] bArr, int i, int i2, int i3) {
            super(cArr, new Data8(bArr), i, i2, i3, null);
        }

        public static Small8 fromBinary(ByteBuffer byteBuffer) {
            return (Small8) CodePointTrie.fromBinary(Type.SMALL, ValueWidth.BITS_8, byteBuffer);
        }
    }
}

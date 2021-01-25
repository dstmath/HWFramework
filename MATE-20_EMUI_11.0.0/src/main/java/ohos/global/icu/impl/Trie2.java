package ohos.global.icu.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.NoSuchElementException;
import ohos.global.icu.text.UTF16;

public abstract class Trie2 implements Iterable<Range> {
    static final int UNEWTRIE2_INDEX_1_LENGTH = 544;
    static final int UNEWTRIE2_INDEX_GAP_LENGTH = 576;
    static final int UNEWTRIE2_INDEX_GAP_OFFSET = 2080;
    static final int UNEWTRIE2_MAX_DATA_LENGTH = 1115264;
    static final int UNEWTRIE2_MAX_INDEX_2_LENGTH = 35488;
    static final int UTRIE2_BAD_UTF8_DATA_OFFSET = 128;
    static final int UTRIE2_CP_PER_INDEX_1_ENTRY = 2048;
    static final int UTRIE2_DATA_BLOCK_LENGTH = 32;
    static final int UTRIE2_DATA_GRANULARITY = 4;
    static final int UTRIE2_DATA_MASK = 31;
    static final int UTRIE2_DATA_START_OFFSET = 192;
    static final int UTRIE2_INDEX_1_OFFSET = 2112;
    static final int UTRIE2_INDEX_2_BLOCK_LENGTH = 64;
    static final int UTRIE2_INDEX_2_BMP_LENGTH = 2080;
    static final int UTRIE2_INDEX_2_MASK = 63;
    static final int UTRIE2_INDEX_2_OFFSET = 0;
    static final int UTRIE2_INDEX_SHIFT = 2;
    static final int UTRIE2_LSCP_INDEX_2_LENGTH = 32;
    static final int UTRIE2_LSCP_INDEX_2_OFFSET = 2048;
    static final int UTRIE2_MAX_INDEX_1_LENGTH = 512;
    static final int UTRIE2_OMITTED_BMP_INDEX_1_LENGTH = 32;
    static final int UTRIE2_OPTIONS_VALUE_BITS_MASK = 15;
    static final int UTRIE2_SHIFT_1 = 11;
    static final int UTRIE2_SHIFT_1_2 = 6;
    static final int UTRIE2_SHIFT_2 = 5;
    static final int UTRIE2_UTF8_2B_INDEX_2_LENGTH = 32;
    static final int UTRIE2_UTF8_2B_INDEX_2_OFFSET = 2080;
    private static ValueMapper defaultValueMapper = new ValueMapper() {
        /* class ohos.global.icu.impl.Trie2.AnonymousClass1 */

        @Override // ohos.global.icu.impl.Trie2.ValueMapper
        public int map(int i) {
            return i;
        }
    };
    int data16;
    int[] data32;
    int dataLength;
    int dataNullOffset;
    int errorValue;
    int fHash;
    UTrie2Header header;
    int highStart;
    int highValueIndex;
    char[] index;
    int index2NullOffset;
    int indexLength;
    int initialValue;

    public static class CharSequenceValues {
        public int codePoint;
        public int index;
        public int value;
    }

    public interface ValueMapper {
        int map(int i);
    }

    /* access modifiers changed from: package-private */
    public enum ValueWidth {
        BITS_16,
        BITS_32
    }

    /* access modifiers changed from: private */
    public static int hashByte(int i, int i2) {
        return (i * 16777619) ^ i2;
    }

    /* access modifiers changed from: private */
    public static int initHash() {
        return -2128831035;
    }

    public abstract int get(int i);

    public abstract int getFromU16SingleLead(char c);

    public static Trie2 createFromSerialized(ByteBuffer byteBuffer) throws IOException {
        Trie2 trie2;
        ValueWidth valueWidth;
        ByteOrder order = byteBuffer.order();
        try {
            UTrie2Header uTrie2Header = new UTrie2Header();
            uTrie2Header.signature = byteBuffer.getInt();
            int i = uTrie2Header.signature;
            if (i == 845771348) {
                byteBuffer.order(order == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                uTrie2Header.signature = 1416784178;
            } else if (i != 1416784178) {
                throw new IllegalArgumentException("Buffer does not contain a serialized UTrie2");
            }
            uTrie2Header.options = byteBuffer.getChar();
            uTrie2Header.indexLength = byteBuffer.getChar();
            uTrie2Header.shiftedDataLength = byteBuffer.getChar();
            uTrie2Header.index2NullOffset = byteBuffer.getChar();
            uTrie2Header.dataNullOffset = byteBuffer.getChar();
            uTrie2Header.shiftedHighStart = byteBuffer.getChar();
            if ((uTrie2Header.options & 15) <= 1) {
                if ((uTrie2Header.options & 15) == 0) {
                    valueWidth = ValueWidth.BITS_16;
                    trie2 = new Trie2_16();
                } else {
                    valueWidth = ValueWidth.BITS_32;
                    trie2 = new Trie2_32();
                }
                trie2.header = uTrie2Header;
                trie2.indexLength = uTrie2Header.indexLength;
                trie2.dataLength = uTrie2Header.shiftedDataLength << 2;
                trie2.index2NullOffset = uTrie2Header.index2NullOffset;
                trie2.dataNullOffset = uTrie2Header.dataNullOffset;
                trie2.highStart = uTrie2Header.shiftedHighStart << 11;
                trie2.highValueIndex = trie2.dataLength - 4;
                if (valueWidth == ValueWidth.BITS_16) {
                    trie2.highValueIndex += trie2.indexLength;
                }
                int i2 = trie2.indexLength;
                if (valueWidth == ValueWidth.BITS_16) {
                    i2 += trie2.dataLength;
                }
                trie2.index = ICUBinary.getChars(byteBuffer, i2, 0);
                if (valueWidth == ValueWidth.BITS_16) {
                    trie2.data16 = trie2.indexLength;
                } else {
                    trie2.data32 = ICUBinary.getInts(byteBuffer, trie2.dataLength, 0);
                }
                int i3 = AnonymousClass2.$SwitchMap$ohos$global$icu$impl$Trie2$ValueWidth[valueWidth.ordinal()];
                if (i3 == 1) {
                    trie2.data32 = null;
                    trie2.initialValue = trie2.index[trie2.dataNullOffset];
                    trie2.errorValue = trie2.index[trie2.data16 + 128];
                } else if (i3 == 2) {
                    trie2.data16 = 0;
                    trie2.initialValue = trie2.data32[trie2.dataNullOffset];
                    trie2.errorValue = trie2.data32[128];
                } else {
                    throw new IllegalArgumentException("UTrie2 serialized format error.");
                }
                return trie2;
            }
            throw new IllegalArgumentException("UTrie2 serialized format error.");
        } finally {
            byteBuffer.order(order);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.impl.Trie2$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$impl$Trie2$ValueWidth = new int[ValueWidth.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$impl$Trie2$ValueWidth[ValueWidth.BITS_16.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$impl$Trie2$ValueWidth[ValueWidth.BITS_32.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public static int getVersion(InputStream inputStream, boolean z) throws IOException {
        if (inputStream.markSupported()) {
            inputStream.mark(4);
            byte[] bArr = new byte[4];
            int read = inputStream.read(bArr);
            inputStream.reset();
            if (read != bArr.length) {
                return 0;
            }
            if (bArr[0] == 84 && bArr[1] == 114 && bArr[2] == 105 && bArr[3] == 101) {
                return 1;
            }
            if (bArr[0] == 84 && bArr[1] == 114 && bArr[2] == 105 && bArr[3] == 50) {
                return 2;
            }
            if (z) {
                if (bArr[0] == 101 && bArr[1] == 105 && bArr[2] == 114 && bArr[3] == 84) {
                    return 1;
                }
                if (bArr[0] == 50 && bArr[1] == 105 && bArr[2] == 114 && bArr[3] == 84) {
                    return 2;
                }
            }
            return 0;
        }
        throw new IllegalArgumentException("Input stream must support mark().");
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x0016  */
    @Override // java.lang.Object
    public final boolean equals(Object obj) {
        if (!(obj instanceof Trie2)) {
            return false;
        }
        Trie2 trie2 = (Trie2) obj;
        Iterator<Range> it = trie2.iterator();
        Iterator<Range> it2 = iterator();
        while (it2.hasNext()) {
            Range next = it2.next();
            if (!it.hasNext() || !next.equals(it.next())) {
                return false;
            }
            while (it2.hasNext()) {
            }
        }
        if (!it.hasNext() && this.errorValue == trie2.errorValue && this.initialValue == trie2.initialValue) {
            return true;
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        if (this.fHash == 0) {
            int initHash = initHash();
            Iterator<Range> it = iterator();
            while (it.hasNext()) {
                initHash = hashInt(initHash, it.next().hashCode());
            }
            if (initHash == 0) {
                initHash = 1;
            }
            this.fHash = initHash;
        }
        return this.fHash;
    }

    public static class Range {
        public int endCodePoint;
        public boolean leadSurrogate;
        public int startCodePoint;
        public int value;

        public boolean equals(Object obj) {
            if (obj == null || !obj.getClass().equals(getClass())) {
                return false;
            }
            Range range = (Range) obj;
            if (this.startCodePoint == range.startCodePoint && this.endCodePoint == range.endCodePoint && this.value == range.value && this.leadSurrogate == range.leadSurrogate) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Trie2.hashByte(Trie2.hashInt(Trie2.hashUChar32(Trie2.hashUChar32(Trie2.initHash(), this.startCodePoint), this.endCodePoint), this.value), this.leadSurrogate ? 1 : 0);
        }
    }

    @Override // java.lang.Iterable
    public Iterator<Range> iterator() {
        return iterator(defaultValueMapper);
    }

    public Iterator<Range> iterator(ValueMapper valueMapper) {
        return new Trie2Iterator(valueMapper);
    }

    public Iterator<Range> iteratorForLeadSurrogate(char c, ValueMapper valueMapper) {
        return new Trie2Iterator(c, valueMapper);
    }

    public Iterator<Range> iteratorForLeadSurrogate(char c) {
        return new Trie2Iterator(c, defaultValueMapper);
    }

    /* access modifiers changed from: protected */
    public int serializeHeader(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(this.header.signature);
        dataOutputStream.writeShort(this.header.options);
        dataOutputStream.writeShort(this.header.indexLength);
        dataOutputStream.writeShort(this.header.shiftedDataLength);
        dataOutputStream.writeShort(this.header.index2NullOffset);
        dataOutputStream.writeShort(this.header.dataNullOffset);
        dataOutputStream.writeShort(this.header.shiftedHighStart);
        for (int i = 0; i < this.header.indexLength; i++) {
            dataOutputStream.writeChar(this.index[i]);
        }
        return 16 + this.header.indexLength;
    }

    public CharSequenceIterator charSequenceIterator(CharSequence charSequence, int i) {
        return new CharSequenceIterator(charSequence, i);
    }

    public class CharSequenceIterator implements Iterator<CharSequenceValues> {
        private CharSequenceValues fResults = new CharSequenceValues();
        private int index;
        private CharSequence text;
        private int textLength;

        CharSequenceIterator(CharSequence charSequence, int i) {
            this.text = charSequence;
            this.textLength = this.text.length();
            set(i);
        }

        public void set(int i) {
            if (i < 0 || i > this.textLength) {
                throw new IndexOutOfBoundsException();
            }
            this.index = i;
        }

        @Override // java.util.Iterator
        public final boolean hasNext() {
            return this.index < this.textLength;
        }

        public final boolean hasPrevious() {
            return this.index > 0;
        }

        @Override // java.util.Iterator
        public CharSequenceValues next() {
            int codePointAt = Character.codePointAt(this.text, this.index);
            int i = Trie2.this.get(codePointAt);
            CharSequenceValues charSequenceValues = this.fResults;
            int i2 = this.index;
            charSequenceValues.index = i2;
            charSequenceValues.codePoint = codePointAt;
            charSequenceValues.value = i;
            this.index = i2 + 1;
            if (codePointAt >= 65536) {
                this.index++;
            }
            return this.fResults;
        }

        public CharSequenceValues previous() {
            int codePointBefore = Character.codePointBefore(this.text, this.index);
            int i = Trie2.this.get(codePointBefore);
            this.index--;
            if (codePointBefore >= 65536) {
                this.index--;
            }
            CharSequenceValues charSequenceValues = this.fResults;
            charSequenceValues.index = this.index;
            charSequenceValues.codePoint = codePointBefore;
            charSequenceValues.value = i;
            return charSequenceValues;
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException("Trie2.CharSequenceIterator does not support remove().");
        }
    }

    /* access modifiers changed from: package-private */
    public static class UTrie2Header {
        int dataNullOffset;
        int index2NullOffset;
        int indexLength;
        int options;
        int shiftedDataLength;
        int shiftedHighStart;
        int signature;

        UTrie2Header() {
        }
    }

    /* access modifiers changed from: package-private */
    public class Trie2Iterator implements Iterator<Range> {
        private boolean doLeadSurrogates = true;
        private boolean doingCodePoints = true;
        private int limitCP;
        private ValueMapper mapper;
        private int nextStart;
        private Range returnValue = new Range();

        Trie2Iterator(ValueMapper valueMapper) {
            this.mapper = valueMapper;
            this.nextStart = 0;
            this.limitCP = 1114112;
            this.doLeadSurrogates = true;
        }

        Trie2Iterator(char c, ValueMapper valueMapper) {
            if (c < 55296 || c > 56319) {
                throw new IllegalArgumentException("Bad lead surrogate value.");
            }
            this.mapper = valueMapper;
            this.nextStart = (c - 55232) << 10;
            this.limitCP = this.nextStart + 1024;
            this.doLeadSurrogates = false;
        }

        @Override // java.util.Iterator
        public Range next() {
            int i;
            int i2;
            if (hasNext()) {
                if (this.nextStart >= this.limitCP) {
                    this.doingCodePoints = false;
                    this.nextStart = 55296;
                }
                if (this.doingCodePoints) {
                    int i3 = Trie2.this.get(this.nextStart);
                    i = this.mapper.map(i3);
                    i2 = Trie2.this.rangeEnd(this.nextStart, this.limitCP, i3);
                    while (i2 < this.limitCP - 1) {
                        int i4 = i2 + 1;
                        int i5 = Trie2.this.get(i4);
                        if (this.mapper.map(i5) != i) {
                            break;
                        }
                        i2 = Trie2.this.rangeEnd(i4, this.limitCP, i5);
                    }
                } else {
                    i = this.mapper.map(Trie2.this.getFromU16SingleLead((char) this.nextStart));
                    i2 = rangeEndLS((char) this.nextStart);
                    while (i2 < 56319) {
                        char c = (char) (i2 + 1);
                        if (this.mapper.map(Trie2.this.getFromU16SingleLead(c)) != i) {
                            break;
                        }
                        i2 = rangeEndLS(c);
                    }
                }
                Range range = this.returnValue;
                range.startCodePoint = this.nextStart;
                range.endCodePoint = i2;
                range.value = i;
                range.leadSurrogate = !this.doingCodePoints;
                this.nextStart = i2 + 1;
                return range;
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return (this.doingCodePoints && (this.doLeadSurrogates || this.nextStart < this.limitCP)) || this.nextStart < 56320;
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int rangeEndLS(char c) {
            int i;
            int fromU16SingleLead;
            if (c >= 56319) {
                return UTF16.LEAD_SURROGATE_MAX_VALUE;
            }
            int fromU16SingleLead2 = Trie2.this.getFromU16SingleLead(c);
            char c2 = c;
            do {
                i = (c2 == 1 ? 1 : 0) + 1;
                if (i > 56319) {
                    break;
                }
                fromU16SingleLead = Trie2.this.getFromU16SingleLead((char) i);
                c2 = i;
            } while (fromU16SingleLead == fromU16SingleLead2);
            return i - 1;
        }
    }

    /* access modifiers changed from: package-private */
    public int rangeEnd(int i, int i2, int i3) {
        int min = Math.min(this.highStart, i2);
        do {
            i++;
            if (i >= min) {
                break;
            }
        } while (get(i) == i3);
        if (i >= this.highStart) {
            i = i2;
        }
        return i - 1;
    }

    /* access modifiers changed from: private */
    public static int hashUChar32(int i, int i2) {
        return hashByte(hashByte(hashByte(i, i2 & 255), (i2 >> 8) & 255), i2 >> 16);
    }

    /* access modifiers changed from: private */
    public static int hashInt(int i, int i2) {
        return hashByte(hashByte(hashByte(hashByte(i, i2 & 255), (i2 >> 8) & 255), (i2 >> 16) & 255), (i2 >> 24) & 255);
    }
}

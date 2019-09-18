package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.PrimitiveIterator;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BitSet implements Cloneable, Serializable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 64;
    private static final int BIT_INDEX_MASK = 63;
    private static final long WORD_MASK = -1;
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("bits", long[].class)};
    private static final long serialVersionUID = 7997698588986878753L;
    private transient boolean sizeIsSticky = $assertionsDisabled;
    private long[] words;
    private transient int wordsInUse = 0;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> 6;
    }

    private void checkInvariants() {
    }

    private void recalculateWordsInUse() {
        int i = this.wordsInUse - 1;
        while (i >= 0 && this.words[i] == 0) {
            i--;
        }
        this.wordsInUse = i + 1;
    }

    public BitSet() {
        initWords(64);
        this.sizeIsSticky = $assertionsDisabled;
    }

    public BitSet(int nbits) {
        if (nbits >= 0) {
            initWords(nbits);
            this.sizeIsSticky = true;
            return;
        }
        throw new NegativeArraySizeException("nbits < 0: " + nbits);
    }

    private void initWords(int nbits) {
        this.words = new long[(wordIndex(nbits - 1) + 1)];
    }

    private BitSet(long[] words2) {
        this.words = words2;
        this.wordsInUse = words2.length;
        checkInvariants();
    }

    public static BitSet valueOf(long[] longs) {
        int n = longs.length;
        while (n > 0 && longs[n - 1] == 0) {
            n--;
        }
        return new BitSet(Arrays.copyOf(longs, n));
    }

    public static BitSet valueOf(LongBuffer lb) {
        LongBuffer lb2 = lb.slice();
        int n = lb2.remaining();
        while (n > 0 && lb2.get(n - 1) == 0) {
            n--;
        }
        long[] words2 = new long[n];
        lb2.get(words2);
        return new BitSet(words2);
    }

    public static BitSet valueOf(byte[] bytes) {
        return valueOf(ByteBuffer.wrap(bytes));
    }

    public static BitSet valueOf(ByteBuffer bb) {
        ByteBuffer bb2 = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
        int n = bb2.remaining();
        while (n > 0 && bb2.get(n - 1) == 0) {
            n--;
        }
        long[] words2 = new long[((n + 7) / 8)];
        bb2.limit(n);
        int i = 0;
        while (bb2.remaining() >= 8) {
            words2[i] = bb2.getLong();
            i++;
        }
        int i2 = bb2.remaining();
        for (int j = 0; j < i2; j++) {
            words2[i] = words2[i] | ((((long) bb2.get()) & 255) << (8 * j));
        }
        return new BitSet(words2);
    }

    public byte[] toByteArray() {
        int n = this.wordsInUse;
        if (n == 0) {
            return new byte[0];
        }
        int len = (n - 1) * 8;
        for (long x = this.words[n - 1]; x != 0; x >>>= 8) {
            len++;
        }
        byte[] bytes = new byte[len];
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n - 1; i++) {
            bb.putLong(this.words[i]);
        }
        for (long x2 = this.words[n - 1]; x2 != 0; x2 >>>= 8) {
            bb.put((byte) ((int) (255 & x2)));
        }
        return bytes;
    }

    public long[] toLongArray() {
        return Arrays.copyOf(this.words, this.wordsInUse);
    }

    private void ensureCapacity(int wordsRequired) {
        if (this.words.length < wordsRequired) {
            this.words = Arrays.copyOf(this.words, Math.max(2 * this.words.length, wordsRequired));
            this.sizeIsSticky = $assertionsDisabled;
        }
    }

    private void expandTo(int wordIndex) {
        int wordsRequired = wordIndex + 1;
        if (this.wordsInUse < wordsRequired) {
            ensureCapacity(wordsRequired);
            this.wordsInUse = wordsRequired;
        }
    }

    private static void checkRange(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        } else if (toIndex < 0) {
            throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
        } else if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
        }
    }

    public void flip(int bitIndex) {
        if (bitIndex >= 0) {
            int wordIndex = wordIndex(bitIndex);
            expandTo(wordIndex);
            long[] jArr = this.words;
            jArr[wordIndex] = jArr[wordIndex] ^ (1 << bitIndex);
            recalculateWordsInUse();
            checkInvariants();
            return;
        }
        throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }

    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            int endWordIndex = wordIndex(toIndex - 1);
            expandTo(endWordIndex);
            long firstWordMask = -1 << fromIndex;
            long lastWordMask = -1 >>> (-toIndex);
            if (startWordIndex == endWordIndex) {
                long[] jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] ^ (firstWordMask & lastWordMask);
            } else {
                long[] jArr2 = this.words;
                jArr2[startWordIndex] = jArr2[startWordIndex] ^ firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    long[] jArr3 = this.words;
                    jArr3[i] = ~jArr3[i];
                }
                long[] jArr4 = this.words;
                jArr4[endWordIndex] = jArr4[endWordIndex] ^ lastWordMask;
            }
            recalculateWordsInUse();
            checkInvariants();
        }
    }

    public void set(int bitIndex) {
        if (bitIndex >= 0) {
            int wordIndex = wordIndex(bitIndex);
            expandTo(wordIndex);
            long[] jArr = this.words;
            jArr[wordIndex] = jArr[wordIndex] | (1 << bitIndex);
            checkInvariants();
            return;
        }
        throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }

    public void set(int bitIndex, boolean value) {
        if (value) {
            set(bitIndex);
        } else {
            clear(bitIndex);
        }
    }

    public void set(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            int endWordIndex = wordIndex(toIndex - 1);
            expandTo(endWordIndex);
            long firstWordMask = -1 << fromIndex;
            long lastWordMask = -1 >>> (-toIndex);
            if (startWordIndex == endWordIndex) {
                long[] jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] | (firstWordMask & lastWordMask);
            } else {
                long[] jArr2 = this.words;
                jArr2[startWordIndex] = jArr2[startWordIndex] | firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    this.words[i] = -1;
                }
                long[] jArr3 = this.words;
                jArr3[endWordIndex] = jArr3[endWordIndex] | lastWordMask;
            }
            checkInvariants();
        }
    }

    public void set(int fromIndex, int toIndex, boolean value) {
        if (value) {
            set(fromIndex, toIndex);
        } else {
            clear(fromIndex, toIndex);
        }
    }

    public void clear(int bitIndex) {
        if (bitIndex >= 0) {
            int wordIndex = wordIndex(bitIndex);
            if (wordIndex < this.wordsInUse) {
                long[] jArr = this.words;
                jArr[wordIndex] = jArr[wordIndex] & (~(1 << bitIndex));
                recalculateWordsInUse();
                checkInvariants();
                return;
            }
            return;
        }
        throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }

    public void clear(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            if (startWordIndex < this.wordsInUse) {
                int endWordIndex = wordIndex(toIndex - 1);
                if (endWordIndex >= this.wordsInUse) {
                    toIndex = length();
                    endWordIndex = this.wordsInUse - 1;
                }
                long firstWordMask = -1 << fromIndex;
                long lastWordMask = -1 >>> (-toIndex);
                if (startWordIndex == endWordIndex) {
                    long[] jArr = this.words;
                    jArr[startWordIndex] = jArr[startWordIndex] & (~(firstWordMask & lastWordMask));
                } else {
                    long[] jArr2 = this.words;
                    jArr2[startWordIndex] = jArr2[startWordIndex] & (~firstWordMask);
                    for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                        this.words[i] = 0;
                    }
                    long[] jArr3 = this.words;
                    jArr3[endWordIndex] = jArr3[endWordIndex] & (~lastWordMask);
                }
                recalculateWordsInUse();
                checkInvariants();
            }
        }
    }

    public void clear() {
        while (this.wordsInUse > 0) {
            long[] jArr = this.words;
            int i = this.wordsInUse - 1;
            this.wordsInUse = i;
            jArr[i] = 0;
        }
    }

    public boolean get(int bitIndex) {
        if (bitIndex >= 0) {
            checkInvariants();
            int wordIndex = wordIndex(bitIndex);
            if (wordIndex >= this.wordsInUse || (this.words[wordIndex] & (1 << bitIndex)) == 0) {
                return $assertionsDisabled;
            }
            return true;
        }
        throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
    }

    public BitSet get(int fromIndex, int toIndex) {
        long j;
        long j2;
        int i = fromIndex;
        int toIndex2 = toIndex;
        checkRange(fromIndex, toIndex);
        checkInvariants();
        int len = length();
        int i2 = 0;
        if (len <= i || i == toIndex2) {
            return new BitSet(0);
        }
        if (toIndex2 > len) {
            toIndex2 = len;
        }
        BitSet result = new BitSet(toIndex2 - i);
        boolean wordAligned = true;
        int targetWords = wordIndex((toIndex2 - i) - 1) + 1;
        int sourceIndex = wordIndex(fromIndex);
        if ((i & BIT_INDEX_MASK) != 0) {
            wordAligned = false;
        }
        while (i2 < targetWords - 1) {
            long[] jArr = result.words;
            if (wordAligned) {
                j2 = this.words[sourceIndex];
            } else {
                j2 = (this.words[sourceIndex] >>> i) | (this.words[sourceIndex + 1] << (-i));
            }
            jArr[i2] = j2;
            i2++;
            sourceIndex++;
        }
        long lastWordMask = -1 >>> (-toIndex2);
        long[] jArr2 = result.words;
        int i3 = targetWords - 1;
        if (((toIndex2 - 1) & BIT_INDEX_MASK) < (i & BIT_INDEX_MASK)) {
            int i4 = toIndex2;
            j = (this.words[sourceIndex] >>> i) | ((this.words[sourceIndex + 1] & lastWordMask) << (-i));
        } else {
            j = (this.words[sourceIndex] & lastWordMask) >>> i;
        }
        jArr2[i3] = j;
        result.wordsInUse = targetWords;
        result.recalculateWordsInUse();
        result.checkInvariants();
        return result;
    }

    public int nextSetBit(int fromIndex) {
        if (fromIndex >= 0) {
            checkInvariants();
            int u = wordIndex(fromIndex);
            if (u >= this.wordsInUse) {
                return -1;
            }
            long word = this.words[u] & (-1 << fromIndex);
            while (word == 0) {
                u++;
                if (u == this.wordsInUse) {
                    return -1;
                }
                word = this.words[u];
            }
            return (u * 64) + Long.numberOfTrailingZeros(word);
        }
        throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    }

    public int nextClearBit(int fromIndex) {
        if (fromIndex >= 0) {
            checkInvariants();
            int u = wordIndex(fromIndex);
            if (u >= this.wordsInUse) {
                return fromIndex;
            }
            long word = (~this.words[u]) & (-1 << fromIndex);
            while (word == 0) {
                u++;
                if (u == this.wordsInUse) {
                    return this.wordsInUse * 64;
                }
                word = ~this.words[u];
            }
            return (u * 64) + Long.numberOfTrailingZeros(word);
        }
        throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
    }

    public int previousSetBit(int fromIndex) {
        if (fromIndex >= 0) {
            checkInvariants();
            int u = wordIndex(fromIndex);
            if (u >= this.wordsInUse) {
                return length() - 1;
            }
            long word = this.words[u] & (-1 >>> (-(fromIndex + 1)));
            while (word == 0) {
                int u2 = u - 1;
                if (u == 0) {
                    return -1;
                }
                word = this.words[u2];
                u = u2;
            }
            return (((u + 1) * 64) - 1) - Long.numberOfLeadingZeros(word);
        } else if (fromIndex == -1) {
            return -1;
        } else {
            throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
        }
    }

    public int previousClearBit(int fromIndex) {
        if (fromIndex >= 0) {
            checkInvariants();
            int u = wordIndex(fromIndex);
            if (u >= this.wordsInUse) {
                return fromIndex;
            }
            long word = (~this.words[u]) & (-1 >>> (-(fromIndex + 1)));
            while (word == 0) {
                int u2 = u - 1;
                if (u == 0) {
                    return -1;
                }
                word = ~this.words[u2];
                u = u2;
            }
            return (((u + 1) * 64) - 1) - Long.numberOfLeadingZeros(word);
        } else if (fromIndex == -1) {
            return -1;
        } else {
            throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
        }
    }

    public int length() {
        if (this.wordsInUse == 0) {
            return 0;
        }
        return ((this.wordsInUse - 1) * 64) + (64 - Long.numberOfLeadingZeros(this.words[this.wordsInUse - 1]));
    }

    public boolean isEmpty() {
        if (this.wordsInUse == 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    public boolean intersects(BitSet set) {
        for (int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; i--) {
            if ((this.words[i] & set.words[i]) != 0) {
                return true;
            }
        }
        return $assertionsDisabled;
    }

    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < this.wordsInUse; i++) {
            sum += Long.bitCount(this.words[i]);
        }
        return sum;
    }

    public void and(BitSet set) {
        if (this != set) {
            while (this.wordsInUse > set.wordsInUse) {
                long[] jArr = this.words;
                int i = this.wordsInUse - 1;
                this.wordsInUse = i;
                jArr[i] = 0;
            }
            for (int i2 = 0; i2 < this.wordsInUse; i2++) {
                long[] jArr2 = this.words;
                jArr2[i2] = jArr2[i2] & set.words[i2];
            }
            recalculateWordsInUse();
            checkInvariants();
        }
    }

    public void or(BitSet set) {
        if (this != set) {
            int wordsInCommon = Math.min(this.wordsInUse, set.wordsInUse);
            if (this.wordsInUse < set.wordsInUse) {
                ensureCapacity(set.wordsInUse);
                this.wordsInUse = set.wordsInUse;
            }
            for (int i = 0; i < wordsInCommon; i++) {
                long[] jArr = this.words;
                jArr[i] = jArr[i] | set.words[i];
            }
            if (wordsInCommon < set.wordsInUse) {
                System.arraycopy((Object) set.words, wordsInCommon, (Object) this.words, wordsInCommon, this.wordsInUse - wordsInCommon);
            }
            checkInvariants();
        }
    }

    public void xor(BitSet set) {
        int wordsInCommon = Math.min(this.wordsInUse, set.wordsInUse);
        if (this.wordsInUse < set.wordsInUse) {
            ensureCapacity(set.wordsInUse);
            this.wordsInUse = set.wordsInUse;
        }
        for (int i = 0; i < wordsInCommon; i++) {
            long[] jArr = this.words;
            jArr[i] = jArr[i] ^ set.words[i];
        }
        if (wordsInCommon < set.wordsInUse) {
            System.arraycopy((Object) set.words, wordsInCommon, (Object) this.words, wordsInCommon, set.wordsInUse - wordsInCommon);
        }
        recalculateWordsInUse();
        checkInvariants();
    }

    public void andNot(BitSet set) {
        for (int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; i--) {
            long[] jArr = this.words;
            jArr[i] = jArr[i] & (~set.words[i]);
        }
        recalculateWordsInUse();
        checkInvariants();
    }

    public int hashCode() {
        long h = 1234;
        int i = this.wordsInUse;
        while (true) {
            i--;
            if (i < 0) {
                return (int) ((h >> 32) ^ h);
            }
            h ^= this.words[i] * ((long) (i + 1));
        }
    }

    public int size() {
        return this.words.length * 64;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof BitSet)) {
            return $assertionsDisabled;
        }
        if (this == obj) {
            return true;
        }
        BitSet set = (BitSet) obj;
        checkInvariants();
        set.checkInvariants();
        if (this.wordsInUse != set.wordsInUse) {
            return $assertionsDisabled;
        }
        for (int i = 0; i < this.wordsInUse; i++) {
            if (this.words[i] != set.words[i]) {
                return $assertionsDisabled;
            }
        }
        return true;
    }

    public Object clone() {
        if (!this.sizeIsSticky) {
            trimToSize();
        }
        try {
            BitSet result = (BitSet) super.clone();
            result.words = (long[]) this.words.clone();
            result.checkInvariants();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    private void trimToSize() {
        if (this.wordsInUse != this.words.length) {
            this.words = Arrays.copyOf(this.words, this.wordsInUse);
            checkInvariants();
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        checkInvariants();
        if (!this.sizeIsSticky) {
            trimToSize();
        }
        s.putFields().put("bits", (Object) this.words);
        s.writeFields();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        this.words = (long[]) s.readFields().get("bits", (Object) null);
        this.wordsInUse = this.words.length;
        recalculateWordsInUse();
        boolean z = true;
        if (this.words.length <= 0 || this.words[this.words.length - 1] != 0) {
            z = $assertionsDisabled;
        }
        this.sizeIsSticky = z;
        checkInvariants();
    }

    public String toString() {
        checkInvariants();
        StringBuilder b = new StringBuilder((6 * (this.wordsInUse > 128 ? cardinality() : this.wordsInUse * 64)) + 2);
        b.append('{');
        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            while (true) {
                int i2 = i + 1;
                if (i2 < 0) {
                    break;
                }
                int nextSetBit = nextSetBit(i2);
                i = nextSetBit;
                if (nextSetBit < 0) {
                    break;
                }
                int endOfRun = nextClearBit(i);
                do {
                    b.append(", ");
                    b.append(i);
                    i++;
                } while (i != endOfRun);
            }
        }
        b.append('}');
        return b.toString();
    }

    public IntStream stream() {
        return StreamSupport.intStream(new Supplier() {
            public final Object get() {
                return Spliterators.spliterator((PrimitiveIterator.OfInt) new PrimitiveIterator.OfInt() {
                    int next = BitSet.this.nextSetBit(0);

                    public boolean hasNext() {
                        if (this.next != -1) {
                            return true;
                        }
                        return BitSet.$assertionsDisabled;
                    }

                    public int nextInt() {
                        if (this.next != -1) {
                            int ret = this.next;
                            this.next = BitSet.this.nextSetBit(this.next + 1);
                            return ret;
                        }
                        throw new NoSuchElementException();
                    }
                }, (long) BitSet.this.cardinality(), 21);
            }
        }, 16469, $assertionsDisabled);
    }
}

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Spliterator.OfInt;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BitSet implements Cloneable, Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = (BitSet.class.desiredAssertionStatus() ^ 1);
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 64;
    private static final int BIT_INDEX_MASK = 63;
    private static final long WORD_MASK = -1;
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("bits", long[].class)};
    private static final long serialVersionUID = 7997698588986878753L;
    private transient boolean sizeIsSticky = -assertionsDisabled;
    private long[] words;
    private transient int wordsInUse = 0;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> 6;
    }

    private void checkInvariants() {
        if (!-assertionsDisabled && this.wordsInUse != 0 && this.words[this.wordsInUse - 1] == 0) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && (this.wordsInUse < 0 || this.wordsInUse > this.words.length)) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && this.wordsInUse != this.words.length && this.words[this.wordsInUse] != 0) {
            throw new AssertionError();
        }
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
        this.sizeIsSticky = -assertionsDisabled;
    }

    public BitSet(int nbits) {
        if (nbits < 0) {
            throw new NegativeArraySizeException("nbits < 0: " + nbits);
        }
        initWords(nbits);
        this.sizeIsSticky = true;
    }

    private void initWords(int nbits) {
        this.words = new long[(wordIndex(nbits - 1) + 1)];
    }

    private BitSet(long[] words) {
        this.words = words;
        this.wordsInUse = words.length;
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
        lb = lb.slice();
        int n = lb.remaining();
        while (n > 0 && lb.get(n - 1) == 0) {
            n--;
        }
        long[] words = new long[n];
        lb.get(words);
        return new BitSet(words);
    }

    public static BitSet valueOf(byte[] bytes) {
        return valueOf(ByteBuffer.wrap(bytes));
    }

    public static BitSet valueOf(ByteBuffer bb) {
        bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
        int n = bb.remaining();
        while (n > 0 && bb.get(n - 1) == (byte) 0) {
            n--;
        }
        long[] words = new long[((n + 7) / 8)];
        bb.limit(n);
        int i = 0;
        while (bb.remaining() >= 8) {
            int i2 = i + 1;
            words[i] = bb.getLong();
            i = i2;
        }
        int remaining = bb.remaining();
        for (int j = 0; j < remaining; j++) {
            words[i] = words[i] | ((((long) bb.get()) & 255) << (j * 8));
        }
        return new BitSet(words);
    }

    public byte[] toByteArray() {
        int n = this.wordsInUse;
        if (n == 0) {
            return new byte[0];
        }
        long x;
        int len = (n - 1) * 8;
        for (x = this.words[n - 1]; x != 0; x >>>= 8) {
            len++;
        }
        byte[] bytes = new byte[len];
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n - 1; i++) {
            bb.putLong(this.words[i]);
        }
        for (x = this.words[n - 1]; x != 0; x >>>= 8) {
            bb.put((byte) ((int) (255 & x)));
        }
        return bytes;
    }

    public long[] toLongArray() {
        return Arrays.copyOf(this.words, this.wordsInUse);
    }

    private void ensureCapacity(int wordsRequired) {
        if (this.words.length < wordsRequired) {
            this.words = Arrays.copyOf(this.words, Math.max(this.words.length * 2, wordsRequired));
            this.sizeIsSticky = -assertionsDisabled;
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
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);
        long[] jArr = this.words;
        jArr[wordIndex] = jArr[wordIndex] ^ (1 << bitIndex);
        recalculateWordsInUse();
        checkInvariants();
    }

    public void flip(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        if (fromIndex != toIndex) {
            int startWordIndex = wordIndex(fromIndex);
            int endWordIndex = wordIndex(toIndex - 1);
            expandTo(endWordIndex);
            long firstWordMask = -1 << fromIndex;
            long lastWordMask = -1 >>> (-toIndex);
            long[] jArr;
            if (startWordIndex == endWordIndex) {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] ^ (firstWordMask & lastWordMask);
            } else {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] ^ firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    jArr = this.words;
                    jArr[i] = jArr[i] ^ -1;
                }
                jArr = this.words;
                jArr[endWordIndex] = jArr[endWordIndex] ^ lastWordMask;
            }
            recalculateWordsInUse();
            checkInvariants();
        }
    }

    public void set(int bitIndex) {
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = wordIndex(bitIndex);
        expandTo(wordIndex);
        long[] jArr = this.words;
        jArr[wordIndex] = jArr[wordIndex] | (1 << bitIndex);
        checkInvariants();
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
            long[] jArr;
            if (startWordIndex == endWordIndex) {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] | (firstWordMask & lastWordMask);
            } else {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] | firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    this.words[i] = -1;
                }
                jArr = this.words;
                jArr[endWordIndex] = jArr[endWordIndex] | lastWordMask;
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
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        int wordIndex = wordIndex(bitIndex);
        if (wordIndex < this.wordsInUse) {
            long[] jArr = this.words;
            jArr[wordIndex] = jArr[wordIndex] & (~(1 << bitIndex));
            recalculateWordsInUse();
            checkInvariants();
        }
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
                long[] jArr;
                if (startWordIndex == endWordIndex) {
                    jArr = this.words;
                    jArr[startWordIndex] = jArr[startWordIndex] & (~(firstWordMask & lastWordMask));
                } else {
                    jArr = this.words;
                    jArr[startWordIndex] = jArr[startWordIndex] & (~firstWordMask);
                    for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                        this.words[i] = 0;
                    }
                    jArr = this.words;
                    jArr[endWordIndex] = jArr[endWordIndex] & (~lastWordMask);
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
        if (bitIndex < 0) {
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
        }
        checkInvariants();
        int wordIndex = wordIndex(bitIndex);
        if (wordIndex >= this.wordsInUse || (this.words[wordIndex] & (1 << bitIndex)) == 0) {
            return -assertionsDisabled;
        }
        return true;
    }

    public BitSet get(int fromIndex, int toIndex) {
        checkRange(fromIndex, toIndex);
        checkInvariants();
        int len = length();
        if (len <= fromIndex || fromIndex == toIndex) {
            return new BitSet(0);
        }
        long[] jArr;
        long j;
        if (toIndex > len) {
            toIndex = len;
        }
        BitSet result = new BitSet(toIndex - fromIndex);
        int targetWords = wordIndex((toIndex - fromIndex) - 1) + 1;
        int sourceIndex = wordIndex(fromIndex);
        boolean wordAligned = (fromIndex & BIT_INDEX_MASK) == 0 ? true : -assertionsDisabled;
        int i = 0;
        while (i < targetWords - 1) {
            jArr = result.words;
            if (wordAligned) {
                j = this.words[sourceIndex];
            } else {
                j = (this.words[sourceIndex] >>> fromIndex) | (this.words[sourceIndex + 1] << (-fromIndex));
            }
            jArr[i] = j;
            i++;
            sourceIndex++;
        }
        long lastWordMask = -1 >>> (-toIndex);
        jArr = result.words;
        int i2 = targetWords - 1;
        if (((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)) {
            j = (this.words[sourceIndex] >>> fromIndex) | ((this.words[sourceIndex + 1] & lastWordMask) << (-fromIndex));
        } else {
            j = (this.words[sourceIndex] & lastWordMask) >>> fromIndex;
        }
        jArr[i2] = j;
        result.wordsInUse = targetWords;
        result.recalculateWordsInUse();
        result.checkInvariants();
        return result;
    }

    public int nextSetBit(int fromIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
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

    public int nextClearBit(int fromIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
        }
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

    public int previousSetBit(int fromIndex) {
        if (fromIndex >= 0) {
            checkInvariants();
            int u = wordIndex(fromIndex);
            if (u >= this.wordsInUse) {
                return length() - 1;
            }
            long word = this.words[u] & (-1 >>> (-(fromIndex + 1)));
            while (true) {
                int u2 = u;
                if (word != 0) {
                    return (((u2 + 1) * 64) - 1) - Long.numberOfLeadingZeros(word);
                }
                u = u2 - 1;
                if (u2 == 0) {
                    return -1;
                }
                word = this.words[u];
            }
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
            while (true) {
                int u2 = u;
                if (word != 0) {
                    return (((u2 + 1) * 64) - 1) - Long.numberOfLeadingZeros(word);
                }
                u = u2 - 1;
                if (u2 == 0) {
                    return -1;
                }
                word = ~this.words[u];
            }
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
        return this.wordsInUse == 0 ? true : -assertionsDisabled;
    }

    public boolean intersects(BitSet set) {
        for (int i = Math.min(this.wordsInUse, set.wordsInUse) - 1; i >= 0; i--) {
            if ((this.words[i] & set.words[i]) != 0) {
                return true;
            }
        }
        return -assertionsDisabled;
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
            long[] jArr;
            while (this.wordsInUse > set.wordsInUse) {
                jArr = this.words;
                int i = this.wordsInUse - 1;
                this.wordsInUse = i;
                jArr[i] = 0;
            }
            for (int i2 = 0; i2 < this.wordsInUse; i2++) {
                jArr = this.words;
                jArr[i2] = jArr[i2] & set.words[i2];
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
                System.arraycopy(set.words, wordsInCommon, this.words, wordsInCommon, this.wordsInUse - wordsInCommon);
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
            System.arraycopy(set.words, wordsInCommon, this.words, wordsInCommon, set.wordsInUse - wordsInCommon);
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
            return -assertionsDisabled;
        }
        if (this == obj) {
            return true;
        }
        BitSet set = (BitSet) obj;
        checkInvariants();
        set.checkInvariants();
        if (this.wordsInUse != set.wordsInUse) {
            return -assertionsDisabled;
        }
        for (int i = 0; i < this.wordsInUse; i++) {
            if (this.words[i] != set.words[i]) {
                return -assertionsDisabled;
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
        } catch (Throwable e) {
            throw new InternalError(e);
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
        s.putFields().put("bits", this.words);
        s.writeFields();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        this.words = (long[]) s.readFields().get("bits", null);
        this.wordsInUse = this.words.length;
        recalculateWordsInUse();
        boolean z = (this.words.length <= 0 || this.words[this.words.length - 1] != 0) ? -assertionsDisabled : true;
        this.sizeIsSticky = z;
        checkInvariants();
    }

    public String toString() {
        checkInvariants();
        StringBuilder b = new StringBuilder(((this.wordsInUse > 128 ? cardinality() : this.wordsInUse * 64) * 6) + 2);
        b.append('{');
        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            while (true) {
                i++;
                if (i >= 0) {
                    i = nextSetBit(i);
                    if (i < 0) {
                        break;
                    }
                    int endOfRun = nextClearBit(i);
                    while (true) {
                        b.append(", ").append(i);
                        i++;
                        if (i != endOfRun) {
                        }
                    }
                } else {
                    break;
                }
            }
        }
        b.append('}');
        return b.toString();
    }

    public IntStream stream() {
        return StreamSupport.intStream(new -$Lambda$_AqhqkT4X2P3dexV7i-4bO_fGpk(this), 16469, -assertionsDisabled);
    }

    /* synthetic */ OfInt lambda$-java_util_BitSet_41972() {
        return Spliterators.spliterator(new PrimitiveIterator.OfInt() {
            int next = BitSet.this.nextSetBit(0);

            public boolean hasNext() {
                return this.next != -1 ? true : BitSet.-assertionsDisabled;
            }

            public int nextInt() {
                if (this.next != -1) {
                    int ret = this.next;
                    this.next = BitSet.this.nextSetBit(this.next + 1);
                    return ret;
                }
                throw new NoSuchElementException();
            }
        }, (long) cardinality(), 21);
    }
}

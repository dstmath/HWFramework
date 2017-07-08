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
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BitSet implements Cloneable, Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int ADDRESS_BITS_PER_WORD = 6;
    private static final int BITS_PER_WORD = 64;
    private static final int BIT_INDEX_MASK = 63;
    private static final long WORD_MASK = -1;
    private static final ObjectStreamField[] serialPersistentFields = null;
    private static final long serialVersionUID = 7997698588986878753L;
    private transient boolean sizeIsSticky;
    private long[] words;
    private transient int wordsInUse;

    final /* synthetic */ class -java_util_stream_IntStream_stream__LambdaImpl0 implements Supplier {
        private /* synthetic */ BitSet val$this;

        public /* synthetic */ -java_util_stream_IntStream_stream__LambdaImpl0(BitSet bitSet) {
            this.val$this = bitSet;
        }

        public Object get() {
            return this.val$this.-java_util_BitSet_lambda$1();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.BitSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.BitSet.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.BitSet.<clinit>():void");
    }

    public void andNot(java.util.BitSet r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.BitSet.andNot(java.util.BitSet):void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.BitSet.andNot(java.util.BitSet):void");
    }

    public void clear(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.BitSet.clear(int):void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.BitSet.clear(int):void");
    }

    public void clear(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.BitSet.clear(int, int):void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.BitSet.clear(int, int):void");
    }

    public int nextClearBit(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.BitSet.nextClearBit(int):int
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.BitSet.nextClearBit(int):int");
    }

    public int previousClearBit(int r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.BitSet.previousClearBit(int):int
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.BitSet.previousClearBit(int):int");
    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    private void checkInvariants() {
        Object obj;
        Object obj2 = 1;
        if (!-assertionsDisabled) {
            obj = (this.wordsInUse == 0 || this.words[this.wordsInUse - 1] != 0) ? 1 : null;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            obj = (this.wordsInUse < 0 || this.wordsInUse > this.words.length) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if (!(this.wordsInUse == this.words.length || this.words[this.wordsInUse] == 0)) {
                obj2 = null;
            }
            if (obj2 == null) {
                throw new AssertionError();
            }
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
        this.wordsInUse = 0;
        this.sizeIsSticky = -assertionsDisabled;
        initWords(BITS_PER_WORD);
        this.sizeIsSticky = -assertionsDisabled;
    }

    public BitSet(int nbits) {
        this.wordsInUse = 0;
        this.sizeIsSticky = -assertionsDisabled;
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
        this.wordsInUse = 0;
        this.sizeIsSticky = -assertionsDisabled;
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
        while (n > 0 && bb.get(n - 1) == null) {
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
            long firstWordMask = WORD_MASK << fromIndex;
            long lastWordMask = WORD_MASK >>> (-toIndex);
            long[] jArr;
            if (startWordIndex == endWordIndex) {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] ^ (firstWordMask & lastWordMask);
            } else {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] ^ firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    jArr = this.words;
                    jArr[i] = jArr[i] ^ WORD_MASK;
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
            long firstWordMask = WORD_MASK << fromIndex;
            long lastWordMask = WORD_MASK >>> (-toIndex);
            long[] jArr;
            if (startWordIndex == endWordIndex) {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] | (firstWordMask & lastWordMask);
            } else {
                jArr = this.words;
                jArr[startWordIndex] = jArr[startWordIndex] | firstWordMask;
                for (int i = startWordIndex + 1; i < endWordIndex; i++) {
                    this.words[i] = WORD_MASK;
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
        long lastWordMask = WORD_MASK >>> (-toIndex);
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
        long word = this.words[u] & (WORD_MASK << fromIndex);
        while (word == 0) {
            u++;
            if (u == this.wordsInUse) {
                return -1;
            }
            word = this.words[u];
        }
        return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
    }

    public int previousSetBit(int fromIndex) {
        if (fromIndex >= 0) {
            checkInvariants();
            int u = wordIndex(fromIndex);
            if (u >= this.wordsInUse) {
                return length() - 1;
            }
            long word = this.words[u] & (WORD_MASK >>> (-(fromIndex + 1)));
            int u2 = u;
            while (word == 0) {
                u = u2 - 1;
                if (u2 == 0) {
                    return -1;
                }
                word = this.words[u];
                u2 = u;
            }
            return (((u2 + 1) * BITS_PER_WORD) - 1) - Long.numberOfLeadingZeros(word);
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
        return ((this.wordsInUse - 1) * BITS_PER_WORD) + (64 - Long.numberOfLeadingZeros(this.words[this.wordsInUse - 1]));
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
        return this.words.length * BITS_PER_WORD;
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
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
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
        boolean z;
        this.words = (long[]) s.readFields().get("bits", null);
        this.wordsInUse = this.words.length;
        recalculateWordsInUse();
        if (this.words.length <= 0 || this.words[this.words.length - 1] != 0) {
            z = -assertionsDisabled;
        } else {
            z = true;
        }
        this.sizeIsSticky = z;
        checkInvariants();
    }

    public String toString() {
        checkInvariants();
        StringBuilder b = new StringBuilder(((this.wordsInUse > Pattern.CANON_EQ ? cardinality() : this.wordsInUse * BITS_PER_WORD) * ADDRESS_BITS_PER_WORD) + 2);
        b.append('{');
        int i = nextSetBit(0);
        if (i != -1) {
            b.append(i);
            i = nextSetBit(i + 1);
            while (i >= 0) {
                int endOfRun = nextClearBit(i);
                do {
                    b.append(", ").append(i);
                    i++;
                } while (i < endOfRun);
                i = nextSetBit(i + 1);
            }
        }
        b.append('}');
        return b.toString();
    }

    public IntStream stream() {
        return StreamSupport.intStream(new -java_util_stream_IntStream_stream__LambdaImpl0(), 16469, -assertionsDisabled);
    }

    /* synthetic */ OfInt -java_util_BitSet_lambda$1() {
        return Spliterators.spliterator(new PrimitiveIterator.OfInt() {
            int next;

            public /* bridge */ /* synthetic */ void forEachRemaining(Object action) {
                forEachRemaining((IntConsumer) action);
            }

            public /* bridge */ /* synthetic */ Object next() {
                return next();
            }

            {
                this.next = BitSet.this.nextSetBit(0);
            }

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

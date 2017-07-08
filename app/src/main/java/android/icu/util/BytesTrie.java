package android.icu.util;

import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.w3c.dom.traversal.NodeFilter;

public final class BytesTrie implements Cloneable, Iterable<Entry> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    static final int kFiveByteDeltaLead = 255;
    static final int kFiveByteValueLead = 127;
    static final int kFourByteDeltaLead = 254;
    static final int kFourByteValueLead = 126;
    static final int kMaxBranchLinearSubNodeLength = 5;
    static final int kMaxLinearMatchLength = 16;
    static final int kMaxOneByteDelta = 191;
    static final int kMaxOneByteValue = 64;
    static final int kMaxThreeByteDelta = 917503;
    static final int kMaxThreeByteValue = 1179647;
    static final int kMaxTwoByteDelta = 12287;
    static final int kMaxTwoByteValue = 6911;
    static final int kMinLinearMatch = 16;
    static final int kMinOneByteValueLead = 16;
    static final int kMinThreeByteDeltaLead = 240;
    static final int kMinThreeByteValueLead = 108;
    static final int kMinTwoByteDeltaLead = 192;
    static final int kMinTwoByteValueLead = 81;
    static final int kMinValueLead = 32;
    private static final int kValueIsFinal = 1;
    private static Result[] valueResults_;
    private byte[] bytes_;
    private int pos_;
    private int remainingMatchLength_;
    private int root_;

    public static final class Entry {
        private byte[] bytes;
        private int length;
        public int value;

        private Entry(int capacity) {
            this.bytes = new byte[capacity];
        }

        public int bytesLength() {
            return this.length;
        }

        public byte byteAt(int index) {
            return this.bytes[index];
        }

        public void copyBytesTo(byte[] dest, int destOffset) {
            System.arraycopy(this.bytes, 0, dest, destOffset, this.length);
        }

        public ByteBuffer bytesAsByteBuffer() {
            return ByteBuffer.wrap(this.bytes, 0, this.length).asReadOnlyBuffer();
        }

        private void ensureCapacity(int len) {
            if (this.bytes.length < len) {
                byte[] newBytes = new byte[Math.min(this.bytes.length * 2, len * 2)];
                System.arraycopy(this.bytes, 0, newBytes, 0, this.length);
                this.bytes = newBytes;
            }
        }

        private void append(byte b) {
            ensureCapacity(this.length + BytesTrie.kValueIsFinal);
            byte[] bArr = this.bytes;
            int i = this.length;
            this.length = i + BytesTrie.kValueIsFinal;
            bArr[i] = b;
        }

        private void append(byte[] b, int off, int len) {
            ensureCapacity(this.length + len);
            System.arraycopy(b, off, this.bytes, this.length, len);
            this.length += len;
        }

        private void truncateString(int newLength) {
            this.length = newLength;
        }
    }

    public static final class Iterator implements java.util.Iterator<Entry> {
        private byte[] bytes_;
        private Entry entry_;
        private int initialPos_;
        private int initialRemainingMatchLength_;
        private int maxLength_;
        private int pos_;
        private int remainingMatchLength_;
        private ArrayList<Long> stack_;

        private Iterator(byte[] trieBytes, int offset, int remainingMatchLength, int maxStringLength) {
            this.stack_ = new ArrayList();
            this.bytes_ = trieBytes;
            this.initialPos_ = offset;
            this.pos_ = offset;
            this.initialRemainingMatchLength_ = remainingMatchLength;
            this.remainingMatchLength_ = remainingMatchLength;
            this.maxLength_ = maxStringLength;
            this.entry_ = new Entry(null);
            int length = this.remainingMatchLength_;
            if (length >= 0) {
                length += BytesTrie.kValueIsFinal;
                if (this.maxLength_ > 0 && length > this.maxLength_) {
                    length = this.maxLength_;
                }
                this.entry_.append(this.bytes_, this.pos_, length);
                this.pos_ += length;
                this.remainingMatchLength_ -= length;
            }
        }

        public Iterator reset() {
            this.pos_ = this.initialPos_;
            this.remainingMatchLength_ = this.initialRemainingMatchLength_;
            int length = this.remainingMatchLength_ + BytesTrie.kValueIsFinal;
            if (this.maxLength_ > 0 && length > this.maxLength_) {
                length = this.maxLength_;
            }
            this.entry_.truncateString(length);
            this.pos_ += length;
            this.remainingMatchLength_ -= length;
            this.stack_.clear();
            return this;
        }

        public boolean hasNext() {
            return (this.pos_ >= 0 || !this.stack_.isEmpty()) ? true : BytesTrie.-assertionsDisabled;
        }

        public Entry next() {
            int length;
            int i = this.pos_;
            if (i < 0) {
                if (this.stack_.isEmpty()) {
                    throw new NoSuchElementException();
                }
                long top = ((Long) this.stack_.remove(this.stack_.size() - 1)).longValue();
                length = (int) top;
                i = (int) (top >> 32);
                this.entry_.truncateString(DexFormat.MAX_TYPE_IDX & length);
                length >>>= BytesTrie.kMinOneByteValueLead;
                if (length > BytesTrie.kValueIsFinal) {
                    i = branchNext(i, length);
                    if (i < 0) {
                        return this.entry_;
                    }
                }
                int pos = i + BytesTrie.kValueIsFinal;
                this.entry_.append(this.bytes_[i]);
                i = pos;
            }
            if (this.remainingMatchLength_ >= 0) {
                return truncateAndStop();
            }
            int node;
            while (true) {
                pos = i + BytesTrie.kValueIsFinal;
                node = this.bytes_[i] & BytesTrie.kFiveByteDeltaLead;
                if (node >= BytesTrie.kMinValueLead) {
                    break;
                } else if (this.maxLength_ > 0 && this.entry_.length == this.maxLength_) {
                    return truncateAndStop();
                } else {
                    if (node < BytesTrie.kMinOneByteValueLead) {
                        if (node == 0) {
                            i = pos + BytesTrie.kValueIsFinal;
                            node = this.bytes_[pos] & BytesTrie.kFiveByteDeltaLead;
                        } else {
                            i = pos;
                        }
                        i = branchNext(i, node + BytesTrie.kValueIsFinal);
                        if (i < 0) {
                            return this.entry_;
                        }
                    } else {
                        length = (node - 16) + BytesTrie.kValueIsFinal;
                        if (this.maxLength_ <= 0 || this.entry_.length + length <= this.maxLength_) {
                            this.entry_.append(this.bytes_, pos, length);
                            i = pos + length;
                        } else {
                            this.entry_.append(this.bytes_, pos, this.maxLength_ - this.entry_.length);
                            return truncateAndStop();
                        }
                    }
                }
            }
            boolean isFinal = (node & BytesTrie.kValueIsFinal) != 0 ? true : BytesTrie.-assertionsDisabled;
            this.entry_.value = BytesTrie.readValue(this.bytes_, pos, node >> BytesTrie.kValueIsFinal);
            if (isFinal || (this.maxLength_ > 0 && this.entry_.length == this.maxLength_)) {
                this.pos_ = -1;
            } else {
                this.pos_ = BytesTrie.skipValue(pos, node);
            }
            return this.entry_;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Entry truncateAndStop() {
            this.pos_ = -1;
            this.entry_.value = -1;
            return this.entry_;
        }

        private int branchNext(int pos, int length) {
            int pos2 = pos;
            while (length > BytesTrie.kMaxBranchLinearSubNodeLength) {
                pos = pos2 + BytesTrie.kValueIsFinal;
                this.stack_.add(Long.valueOf(((((long) BytesTrie.skipDelta(this.bytes_, pos)) << 32) | ((long) ((length - (length >> BytesTrie.kValueIsFinal)) << BytesTrie.kMinOneByteValueLead))) | ((long) this.entry_.length)));
                length >>= BytesTrie.kValueIsFinal;
                pos2 = BytesTrie.jumpByDelta(this.bytes_, pos);
            }
            pos = pos2 + BytesTrie.kValueIsFinal;
            byte trieByte = this.bytes_[pos2];
            pos2 = pos + BytesTrie.kValueIsFinal;
            int node = this.bytes_[pos] & BytesTrie.kFiveByteDeltaLead;
            boolean isFinal = (node & BytesTrie.kValueIsFinal) != 0 ? true : BytesTrie.-assertionsDisabled;
            int value = BytesTrie.readValue(this.bytes_, pos2, node >> BytesTrie.kValueIsFinal);
            pos = BytesTrie.skipValue(pos2, node);
            this.stack_.add(Long.valueOf(((((long) pos) << 32) | ((long) ((length - 1) << BytesTrie.kMinOneByteValueLead))) | ((long) this.entry_.length)));
            this.entry_.append(trieByte);
            if (!isFinal) {
                return pos + value;
            }
            this.pos_ = -1;
            this.entry_.value = value;
            return -1;
        }
    }

    public enum Result {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.BytesTrie.Result.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.BytesTrie.Result.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.BytesTrie.Result.<clinit>():void");
        }

        public boolean matches() {
            return this != NO_MATCH ? true : BytesTrie.-assertionsDisabled;
        }

        public boolean hasValue() {
            return ordinal() >= 2 ? true : BytesTrie.-assertionsDisabled;
        }

        public boolean hasNext() {
            return (ordinal() & BytesTrie.kValueIsFinal) != 0 ? true : BytesTrie.-assertionsDisabled;
        }
    }

    public static final class State {
        private byte[] bytes;
        private int pos;
        private int remainingMatchLength;
        private int root;

        public State() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.BytesTrie.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.BytesTrie.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.BytesTrie.<clinit>():void");
    }

    public BytesTrie(byte[] trieBytes, int offset) {
        this.bytes_ = trieBytes;
        this.root_ = offset;
        this.pos_ = offset;
        this.remainingMatchLength_ = -1;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public BytesTrie reset() {
        this.pos_ = this.root_;
        this.remainingMatchLength_ = -1;
        return this;
    }

    public BytesTrie saveState(State state) {
        state.bytes = this.bytes_;
        state.root = this.root_;
        state.pos = this.pos_;
        state.remainingMatchLength = this.remainingMatchLength_;
        return this;
    }

    public BytesTrie resetToState(State state) {
        if (this.bytes_ == state.bytes && this.bytes_ != null && this.root_ == state.root) {
            this.pos_ = state.pos;
            this.remainingMatchLength_ = state.remainingMatchLength;
            return this;
        }
        throw new IllegalArgumentException("incompatible trie state");
    }

    public Result current() {
        int pos = this.pos_;
        if (pos < 0) {
            return Result.NO_MATCH;
        }
        Result result;
        if (this.remainingMatchLength_ < 0) {
            int node = this.bytes_[pos] & kFiveByteDeltaLead;
            if (node >= kMinValueLead) {
                result = valueResults_[node & kValueIsFinal];
                return result;
            }
        }
        result = Result.NO_VALUE;
        return result;
    }

    public Result first(int inByte) {
        this.remainingMatchLength_ = -1;
        if (inByte < 0) {
            inByte += NodeFilter.SHOW_DOCUMENT;
        }
        return nextImpl(this.root_, inByte);
    }

    public Result next(int inByte) {
        int pos = this.pos_;
        if (pos < 0) {
            return Result.NO_MATCH;
        }
        if (inByte < 0) {
            inByte += NodeFilter.SHOW_DOCUMENT;
        }
        int length = this.remainingMatchLength_;
        if (length < 0) {
            return nextImpl(pos, inByte);
        }
        int pos2 = pos + kValueIsFinal;
        if (inByte == (this.bytes_[pos] & kFiveByteDeltaLead)) {
            Result result;
            length--;
            this.remainingMatchLength_ = length;
            this.pos_ = pos2;
            if (length < 0) {
                int node = this.bytes_[pos2] & kFiveByteDeltaLead;
                if (node >= kMinValueLead) {
                    result = valueResults_[node & kValueIsFinal];
                    return result;
                }
            }
            result = Result.NO_VALUE;
            return result;
        }
        stop();
        return Result.NO_MATCH;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Result next(byte[] s, int sIndex, int sLimit) {
        if (sIndex >= sLimit) {
            return current();
        }
        int pos = this.pos_;
        if (pos < 0) {
            return Result.NO_MATCH;
        }
        Result result;
        int length = this.remainingMatchLength_;
        loop0:
        for (int sIndex2 = sIndex; sIndex2 != sLimit; sIndex2 = sIndex) {
            int node;
            sIndex = sIndex2 + kValueIsFinal;
            byte inByte = s[sIndex2];
            if (length < 0) {
                int pos2;
                this.remainingMatchLength_ = length;
                while (true) {
                    pos2 = pos + kValueIsFinal;
                    node = this.bytes_[pos] & kFiveByteDeltaLead;
                    if (node < kMinOneByteValueLead) {
                        Result result2 = branchNext(pos2, node, inByte & kFiveByteDeltaLead);
                        if (result2 == Result.NO_MATCH) {
                            return Result.NO_MATCH;
                        }
                        if (sIndex == sLimit) {
                            return result2;
                        }
                        if (result2 == Result.FINAL_VALUE) {
                            stop();
                            return Result.NO_MATCH;
                        }
                        sIndex2 = sIndex + kValueIsFinal;
                        inByte = s[sIndex];
                        pos = this.pos_;
                        sIndex = sIndex2;
                    } else if (node < kMinValueLead) {
                        break;
                    } else if ((node & kValueIsFinal) != 0) {
                        stop();
                        return Result.NO_MATCH;
                    } else {
                        pos = skipValue(pos2, node);
                        if (-assertionsDisabled) {
                            continue;
                        } else {
                            if (((this.bytes_[pos] & kFiveByteDeltaLead) < kMinValueLead ? kValueIsFinal : null) == null) {
                                break loop0;
                            }
                        }
                    }
                }
                length = node - 16;
                if (inByte != this.bytes_[pos2]) {
                    stop();
                    return Result.NO_MATCH;
                }
                pos = pos2 + kValueIsFinal;
                length--;
            } else if (inByte != this.bytes_[pos]) {
                stop();
                return Result.NO_MATCH;
            } else {
                pos += kValueIsFinal;
                length--;
            }
        }
        this.remainingMatchLength_ = length;
        this.pos_ = pos;
        if (length < 0) {
            node = this.bytes_[pos] & kFiveByteDeltaLead;
            if (node >= kMinValueLead) {
                result = valueResults_[node & kValueIsFinal];
                return result;
            }
        }
        result = Result.NO_VALUE;
        return result;
    }

    public int getValue() {
        int pos = this.pos_;
        int pos2 = pos + kValueIsFinal;
        int leadByte = this.bytes_[pos] & kFiveByteDeltaLead;
        if (!-assertionsDisabled) {
            if ((leadByte >= kMinValueLead ? kValueIsFinal : null) == null) {
                throw new AssertionError();
            }
        }
        return readValue(this.bytes_, pos2, leadByte >> kValueIsFinal);
    }

    public long getUniqueValue() {
        int pos = this.pos_;
        if (pos < 0) {
            return 0;
        }
        return (findUniqueValue(this.bytes_, (this.remainingMatchLength_ + pos) + kValueIsFinal, 0) << 31) >> 31;
    }

    public int getNextBytes(Appendable out) {
        int i = 0;
        int pos = this.pos_;
        if (pos < 0) {
            return 0;
        }
        if (this.remainingMatchLength_ >= 0) {
            append(out, this.bytes_[pos] & kFiveByteDeltaLead);
            return kValueIsFinal;
        }
        int pos2 = pos + kValueIsFinal;
        int node = this.bytes_[pos] & kFiveByteDeltaLead;
        if (node >= kMinValueLead) {
            if ((node & kValueIsFinal) != 0) {
                return 0;
            }
            pos = skipValue(pos2, node);
            pos2 = pos + kValueIsFinal;
            node = this.bytes_[pos] & kFiveByteDeltaLead;
            if (!-assertionsDisabled) {
                if (node < kMinValueLead) {
                    i = kValueIsFinal;
                }
                if (i == 0) {
                    throw new AssertionError();
                }
            }
        }
        if (node < kMinOneByteValueLead) {
            if (node == 0) {
                pos = pos2 + kValueIsFinal;
                node = this.bytes_[pos2] & kFiveByteDeltaLead;
            } else {
                pos = pos2;
            }
            node += kValueIsFinal;
            getNextBranchBytes(this.bytes_, pos, node, out);
            return node;
        }
        append(out, this.bytes_[pos2] & kFiveByteDeltaLead);
        return kValueIsFinal;
    }

    public /* bridge */ /* synthetic */ java.util.Iterator m12iterator() {
        return iterator();
    }

    public Iterator iterator() {
        return new Iterator(this.pos_, this.remainingMatchLength_, 0, null);
    }

    public Iterator iterator(int maxStringLength) {
        return new Iterator(this.pos_, this.remainingMatchLength_, maxStringLength, null);
    }

    public static Iterator iterator(byte[] trieBytes, int offset, int maxStringLength) {
        return new Iterator(offset, -1, maxStringLength, null);
    }

    private void stop() {
        this.pos_ = -1;
    }

    private static int readValue(byte[] bytes, int pos, int leadByte) {
        if (leadByte < kMinTwoByteValueLead) {
            return leadByte - 16;
        }
        if (leadByte < kMinThreeByteValueLead) {
            return ((leadByte - 81) << 8) | (bytes[pos] & kFiveByteDeltaLead);
        }
        if (leadByte < kFourByteValueLead) {
            return (((leadByte - 108) << kMinOneByteValueLead) | ((bytes[pos] & kFiveByteDeltaLead) << 8)) | (bytes[pos + kValueIsFinal] & kFiveByteDeltaLead);
        }
        if (leadByte == kFourByteValueLead) {
            return (((bytes[pos] & kFiveByteDeltaLead) << kMinOneByteValueLead) | ((bytes[pos + kValueIsFinal] & kFiveByteDeltaLead) << 8)) | (bytes[pos + 2] & kFiveByteDeltaLead);
        }
        return (((bytes[pos] << 24) | ((bytes[pos + kValueIsFinal] & kFiveByteDeltaLead) << kMinOneByteValueLead)) | ((bytes[pos + 2] & kFiveByteDeltaLead) << 8)) | (bytes[pos + 3] & kFiveByteDeltaLead);
    }

    private static int skipValue(int pos, int leadByte) {
        if (!-assertionsDisabled) {
            if ((leadByte >= kMinValueLead ? kValueIsFinal : null) == null) {
                throw new AssertionError();
            }
        }
        if (leadByte < Opcodes.OP_XOR_LONG) {
            return pos;
        }
        if (leadByte < Opcodes.OP_ADD_INT_LIT8) {
            return pos + kValueIsFinal;
        }
        if (leadByte < SCSU.ARMENIANINDEX) {
            return pos + 2;
        }
        return pos + (((leadByte >> kValueIsFinal) & kValueIsFinal) + 3);
    }

    private static int skipValue(byte[] bytes, int pos) {
        return skipValue(pos + kValueIsFinal, bytes[pos] & kFiveByteDeltaLead);
    }

    private static int jumpByDelta(byte[] bytes, int pos) {
        int pos2 = pos + kValueIsFinal;
        int delta = bytes[pos] & kFiveByteDeltaLead;
        if (delta < kMinTwoByteDeltaLead) {
            pos = pos2;
        } else if (delta < kMinThreeByteDeltaLead) {
            pos = pos2 + kValueIsFinal;
            delta = ((delta - 192) << 8) | (bytes[pos2] & kFiveByteDeltaLead);
        } else if (delta < kFourByteDeltaLead) {
            delta = (((delta - 240) << kMinOneByteValueLead) | ((bytes[pos2] & kFiveByteDeltaLead) << 8)) | (bytes[pos2 + kValueIsFinal] & kFiveByteDeltaLead);
            pos = pos2 + 2;
        } else if (delta == kFourByteDeltaLead) {
            delta = (((bytes[pos2] & kFiveByteDeltaLead) << kMinOneByteValueLead) | ((bytes[pos2 + kValueIsFinal] & kFiveByteDeltaLead) << 8)) | (bytes[pos2 + 2] & kFiveByteDeltaLead);
            pos = pos2 + 3;
        } else {
            delta = (((bytes[pos2] << 24) | ((bytes[pos2 + kValueIsFinal] & kFiveByteDeltaLead) << kMinOneByteValueLead)) | ((bytes[pos2 + 2] & kFiveByteDeltaLead) << 8)) | (bytes[pos2 + 3] & kFiveByteDeltaLead);
            pos = pos2 + 4;
        }
        return pos + delta;
    }

    private static int skipDelta(byte[] bytes, int pos) {
        int pos2 = pos + kValueIsFinal;
        int delta = bytes[pos] & kFiveByteDeltaLead;
        if (delta < kMinTwoByteDeltaLead) {
            return pos2;
        }
        if (delta < kMinThreeByteDeltaLead) {
            return pos2 + kValueIsFinal;
        }
        if (delta < kFourByteDeltaLead) {
            return pos2 + 2;
        }
        return pos2 + ((delta & kValueIsFinal) + 3);
    }

    private Result branchNext(int pos, int length, int inByte) {
        Object obj = kValueIsFinal;
        if (length == 0) {
            length = this.bytes_[pos] & kFiveByteDeltaLead;
            pos += kValueIsFinal;
        }
        length += kValueIsFinal;
        int pos2 = pos;
        while (length > kMaxBranchLinearSubNodeLength) {
            pos = pos2 + kValueIsFinal;
            if (inByte < (this.bytes_[pos2] & kFiveByteDeltaLead)) {
                length >>= kValueIsFinal;
                pos = jumpByDelta(this.bytes_, pos);
            } else {
                length -= length >> kValueIsFinal;
                pos = skipDelta(this.bytes_, pos);
            }
            pos2 = pos;
        }
        pos = pos2;
        do {
            pos2 = pos + kValueIsFinal;
            int node;
            if (inByte == (this.bytes_[pos] & kFiveByteDeltaLead)) {
                Result result;
                node = this.bytes_[pos2] & kFiveByteDeltaLead;
                if (!-assertionsDisabled) {
                    if (node < kMinValueLead) {
                        obj = null;
                    }
                    if (obj == null) {
                        throw new AssertionError();
                    }
                }
                if ((node & kValueIsFinal) != 0) {
                    result = Result.FINAL_VALUE;
                    pos = pos2;
                } else {
                    int delta;
                    pos = pos2 + kValueIsFinal;
                    node >>= kValueIsFinal;
                    if (node < kMinTwoByteValueLead) {
                        delta = node - 16;
                    } else if (node < kMinThreeByteValueLead) {
                        delta = ((node - 81) << 8) | (this.bytes_[pos] & kFiveByteDeltaLead);
                        pos += kValueIsFinal;
                    } else if (node < kFourByteValueLead) {
                        delta = (((node - 108) << kMinOneByteValueLead) | ((this.bytes_[pos] & kFiveByteDeltaLead) << 8)) | (this.bytes_[pos + kValueIsFinal] & kFiveByteDeltaLead);
                        pos += 2;
                    } else if (node == kFourByteValueLead) {
                        delta = (((this.bytes_[pos] & kFiveByteDeltaLead) << kMinOneByteValueLead) | ((this.bytes_[pos + kValueIsFinal] & kFiveByteDeltaLead) << 8)) | (this.bytes_[pos + 2] & kFiveByteDeltaLead);
                        pos += 3;
                    } else {
                        delta = (((this.bytes_[pos] << 24) | ((this.bytes_[pos + kValueIsFinal] & kFiveByteDeltaLead) << kMinOneByteValueLead)) | ((this.bytes_[pos + 2] & kFiveByteDeltaLead) << 8)) | (this.bytes_[pos + 3] & kFiveByteDeltaLead);
                        pos += 4;
                    }
                    pos += delta;
                    node = this.bytes_[pos] & kFiveByteDeltaLead;
                    result = node >= kMinValueLead ? valueResults_[node & kValueIsFinal] : Result.NO_VALUE;
                }
                this.pos_ = pos;
                return result;
            }
            length--;
            pos = skipValue(this.bytes_, pos2);
        } while (length > kValueIsFinal);
        pos2 = pos + kValueIsFinal;
        if (inByte == (this.bytes_[pos] & kFiveByteDeltaLead)) {
            this.pos_ = pos2;
            node = this.bytes_[pos2] & kFiveByteDeltaLead;
            return node >= kMinValueLead ? valueResults_[node & kValueIsFinal] : Result.NO_VALUE;
        }
        stop();
        return Result.NO_MATCH;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Result nextImpl(int pos, int inByte) {
        int pos2;
        while (true) {
            pos2 = pos + kValueIsFinal;
            int node = this.bytes_[pos] & kFiveByteDeltaLead;
            if (node < kMinOneByteValueLead) {
                return branchNext(pos2, node, inByte);
            }
            if (node < kMinValueLead) {
                break;
            } else if ((node & kValueIsFinal) != 0) {
                break;
            } else {
                pos = skipValue(pos2, node);
                if (!-assertionsDisabled) {
                    if (((this.bytes_[pos] & kFiveByteDeltaLead) < kMinValueLead ? kValueIsFinal : null) == null) {
                        break;
                    }
                }
            }
            stop();
            return Result.NO_MATCH;
        }
        pos = pos2;
        stop();
        return Result.NO_MATCH;
    }

    private static long findUniqueValueFromBranch(byte[] bytes, int pos, int length, long uniqueValue) {
        while (length > kMaxBranchLinearSubNodeLength) {
            pos += kValueIsFinal;
            uniqueValue = findUniqueValueFromBranch(bytes, jumpByDelta(bytes, pos), length >> kValueIsFinal, uniqueValue);
            if (uniqueValue == 0) {
                return 0;
            }
            length -= length >> kValueIsFinal;
            pos = skipDelta(bytes, pos);
        }
        do {
            pos += kValueIsFinal;
            int pos2 = pos + kValueIsFinal;
            int node = bytes[pos] & kFiveByteDeltaLead;
            boolean isFinal = (node & kValueIsFinal) != 0 ? true : -assertionsDisabled;
            int value = readValue(bytes, pos2, node >> kValueIsFinal);
            pos = skipValue(pos2, node);
            if (!isFinal) {
                uniqueValue = findUniqueValue(bytes, pos + value, uniqueValue);
                if (uniqueValue == 0) {
                    return 0;
                }
            } else if (uniqueValue == 0) {
                uniqueValue = (((long) value) << 1) | 1;
            } else if (value != ((int) (uniqueValue >> 1))) {
                return 0;
            }
            length--;
        } while (length > kValueIsFinal);
        return (((long) (pos + kValueIsFinal)) << 33) | (8589934591L & uniqueValue);
    }

    private static long findUniqueValue(byte[] bytes, int pos, long uniqueValue) {
        while (true) {
            int pos2 = pos + kValueIsFinal;
            int node = bytes[pos] & kFiveByteDeltaLead;
            if (node < kMinOneByteValueLead) {
                if (node == 0) {
                    pos = pos2 + kValueIsFinal;
                    node = bytes[pos2] & kFiveByteDeltaLead;
                } else {
                    pos = pos2;
                }
                uniqueValue = findUniqueValueFromBranch(bytes, pos, node + kValueIsFinal, uniqueValue);
                if (uniqueValue == 0) {
                    return 0;
                }
                pos = (int) (uniqueValue >>> 33);
            } else if (node < kMinValueLead) {
                pos = pos2 + ((node - 16) + kValueIsFinal);
            } else {
                boolean isFinal = (node & kValueIsFinal) != 0 ? true : -assertionsDisabled;
                int value = readValue(bytes, pos2, node >> kValueIsFinal);
                if (uniqueValue == 0) {
                    uniqueValue = (((long) value) << 1) | 1;
                } else if (value != ((int) (uniqueValue >> 1))) {
                    return 0;
                }
                if (isFinal) {
                    return uniqueValue;
                }
                pos = skipValue(pos2, node);
            }
        }
    }

    private static void getNextBranchBytes(byte[] bytes, int pos, int length, Appendable out) {
        while (length > kMaxBranchLinearSubNodeLength) {
            pos += kValueIsFinal;
            getNextBranchBytes(bytes, jumpByDelta(bytes, pos), length >> kValueIsFinal, out);
            length -= length >> kValueIsFinal;
            pos = skipDelta(bytes, pos);
        }
        do {
            int pos2 = pos + kValueIsFinal;
            append(out, bytes[pos] & kFiveByteDeltaLead);
            pos = skipValue(bytes, pos2);
            length--;
        } while (length > kValueIsFinal);
        append(out, bytes[pos] & kFiveByteDeltaLead);
    }

    private static void append(Appendable out, int c) {
        try {
            out.append((char) c);
        } catch (Throwable e) {
            throw new ICUUncheckedIOException(e);
        }
    }
}

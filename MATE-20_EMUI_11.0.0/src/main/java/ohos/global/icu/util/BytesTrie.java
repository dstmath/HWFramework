package ohos.global.icu.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import ohos.media.camera.params.Metadata;
import ohos.media.camera.params.adapter.camera2ex.CameraMetadataEx;

public final class BytesTrie implements Cloneable, Iterable<Entry> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
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
    private static Result[] valueResults_ = {Result.INTERMEDIATE_VALUE, Result.FINAL_VALUE};
    private byte[] bytes_;
    private int pos_;
    private int remainingMatchLength_;
    private int root_;

    public static final class State {
        private byte[] bytes;
        private int pos;
        private int remainingMatchLength;
        private int root;
    }

    /* access modifiers changed from: private */
    public static int skipValue(int i, int i2) {
        return i2 >= 162 ? i2 < 216 ? i + 1 : i2 < 252 ? i + 2 : i + ((i2 >> 1) & 1) + 3 : i;
    }

    public BytesTrie(byte[] bArr, int i) {
        this.bytes_ = bArr;
        this.root_ = i;
        this.pos_ = i;
        this.remainingMatchLength_ = -1;
    }

    public BytesTrie(BytesTrie bytesTrie) {
        this.bytes_ = bytesTrie.bytes_;
        this.root_ = bytesTrie.root_;
        this.pos_ = bytesTrie.pos_;
        this.remainingMatchLength_ = bytesTrie.remainingMatchLength_;
    }

    @Override // java.lang.Object
    public BytesTrie clone() throws CloneNotSupportedException {
        return (BytesTrie) super.clone();
    }

    public BytesTrie reset() {
        this.pos_ = this.root_;
        this.remainingMatchLength_ = -1;
        return this;
    }

    public long getState64() {
        return (((long) this.remainingMatchLength_) << 32) | ((long) this.pos_);
    }

    public BytesTrie resetToState64(long j) {
        this.remainingMatchLength_ = (int) (j >> 32);
        this.pos_ = (int) j;
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

    public enum Result {
        NO_MATCH,
        NO_VALUE,
        FINAL_VALUE,
        INTERMEDIATE_VALUE;

        public boolean matches() {
            return this != NO_MATCH;
        }

        public boolean hasValue() {
            return ordinal() >= 2;
        }

        public boolean hasNext() {
            return (ordinal() & 1) != 0;
        }
    }

    public Result current() {
        int i;
        int i2 = this.pos_;
        if (i2 < 0) {
            return Result.NO_MATCH;
        }
        return (this.remainingMatchLength_ >= 0 || (i = this.bytes_[i2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) < 32) ? Result.NO_VALUE : valueResults_[i & 1];
    }

    public Result first(int i) {
        this.remainingMatchLength_ = -1;
        if (i < 0) {
            i += 256;
        }
        return nextImpl(this.root_, i);
    }

    public Result next(int i) {
        int i2;
        int i3 = this.pos_;
        if (i3 < 0) {
            return Result.NO_MATCH;
        }
        if (i < 0) {
            i += 256;
        }
        int i4 = this.remainingMatchLength_;
        if (i4 < 0) {
            return nextImpl(i3, i);
        }
        byte[] bArr = this.bytes_;
        int i5 = i3 + 1;
        if (i == (bArr[i3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE)) {
            int i6 = i4 - 1;
            this.remainingMatchLength_ = i6;
            this.pos_ = i5;
            return (i6 >= 0 || (i2 = bArr[i5] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) < 32) ? Result.NO_VALUE : valueResults_[i2 & 1];
        }
        stop();
        return Result.NO_MATCH;
    }

    public Result next(byte[] bArr, int i, int i2) {
        int i3;
        if (i >= i2) {
            return current();
        }
        int i4 = this.pos_;
        if (i4 < 0) {
            return Result.NO_MATCH;
        }
        int i5 = this.remainingMatchLength_;
        while (i != i2) {
            int i6 = i + 1;
            byte b = bArr[i];
            if (i5 < 0) {
                this.remainingMatchLength_ = i5;
                while (true) {
                    byte[] bArr2 = this.bytes_;
                    int i7 = i4 + 1;
                    int i8 = bArr2[i4] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
                    if (i8 < 16) {
                        Result branchNext = branchNext(i7, i8, b & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                        if (branchNext == Result.NO_MATCH) {
                            return Result.NO_MATCH;
                        }
                        if (i6 == i2) {
                            return branchNext;
                        }
                        if (branchNext == Result.FINAL_VALUE) {
                            stop();
                            return Result.NO_MATCH;
                        }
                        byte b2 = bArr[i6];
                        i6++;
                        b = b2;
                        i4 = this.pos_;
                    } else if (i8 < 32) {
                        int i9 = i8 - 16;
                        if (b != bArr2[i7]) {
                            stop();
                            return Result.NO_MATCH;
                        }
                        i5 = i9 - 1;
                        i4 = i7 + 1;
                    } else if ((i8 & 1) != 0) {
                        stop();
                        return Result.NO_MATCH;
                    } else {
                        i4 = skipValue(i7, i8);
                    }
                }
            } else if (b != this.bytes_[i4]) {
                stop();
                return Result.NO_MATCH;
            } else {
                i4++;
                i5--;
            }
            i = i6;
        }
        this.remainingMatchLength_ = i5;
        this.pos_ = i4;
        return (i5 >= 0 || (i3 = this.bytes_[i4] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) < 32) ? Result.NO_VALUE : valueResults_[i3 & 1];
    }

    public int getValue() {
        int i = this.pos_;
        byte[] bArr = this.bytes_;
        return readValue(bArr, i + 1, (bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) >> 1);
    }

    public long getUniqueValue() {
        int i = this.pos_;
        if (i < 0) {
            return 0;
        }
        return (findUniqueValue(this.bytes_, (i + this.remainingMatchLength_) + 1, 0) << 31) >> 31;
    }

    public int getNextBytes(Appendable appendable) {
        int i;
        int i2 = this.pos_;
        if (i2 < 0) {
            return 0;
        }
        if (this.remainingMatchLength_ >= 0) {
            append(appendable, this.bytes_[i2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
            return 1;
        }
        int i3 = i2 + 1;
        int i4 = this.bytes_[i2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
        if (i4 >= 32) {
            if ((i4 & 1) != 0) {
                return 0;
            }
            int skipValue = skipValue(i3, i4);
            i3 = skipValue + 1;
            i4 = this.bytes_[skipValue] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
        }
        if (i4 < 16) {
            if (i4 == 0) {
                i = i3 + 1;
                i4 = this.bytes_[i3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            } else {
                i = i3;
            }
            int i5 = i4 + 1;
            getNextBranchBytes(this.bytes_, i, i5, appendable);
            return i5;
        }
        append(appendable, this.bytes_[i3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
        return 1;
    }

    /* Return type fixed from 'ohos.global.icu.util.BytesTrie$Iterator' to match base method */
    @Override // java.lang.Iterable
    public java.util.Iterator<Entry> iterator() {
        return new Iterator(this.bytes_, this.pos_, this.remainingMatchLength_, 0);
    }

    public Iterator iterator(int i) {
        return new Iterator(this.bytes_, this.pos_, this.remainingMatchLength_, i);
    }

    public static Iterator iterator(byte[] bArr, int i, int i2) {
        return new Iterator(bArr, i, -1, i2);
    }

    public static final class Entry {
        private byte[] bytes;
        private int length;
        public int value;

        private Entry(int i) {
            this.bytes = new byte[i];
        }

        public int bytesLength() {
            return this.length;
        }

        public byte byteAt(int i) {
            return this.bytes[i];
        }

        public void copyBytesTo(byte[] bArr, int i) {
            System.arraycopy(this.bytes, 0, bArr, i, this.length);
        }

        public ByteBuffer bytesAsByteBuffer() {
            return ByteBuffer.wrap(this.bytes, 0, this.length).asReadOnlyBuffer();
        }

        private void ensureCapacity(int i) {
            byte[] bArr = this.bytes;
            if (bArr.length < i) {
                byte[] bArr2 = new byte[Math.min(bArr.length * 2, i * 2)];
                System.arraycopy(this.bytes, 0, bArr2, 0, this.length);
                this.bytes = bArr2;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void append(byte b) {
            ensureCapacity(this.length + 1);
            byte[] bArr = this.bytes;
            int i = this.length;
            this.length = i + 1;
            bArr[i] = b;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void append(byte[] bArr, int i, int i2) {
            ensureCapacity(this.length + i2);
            System.arraycopy(bArr, i, this.bytes, this.length, i2);
            this.length += i2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void truncateString(int i) {
            this.length = i;
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

        private Iterator(byte[] bArr, int i, int i2, int i3) {
            this.stack_ = new ArrayList<>();
            this.bytes_ = bArr;
            this.initialPos_ = i;
            this.pos_ = i;
            this.initialRemainingMatchLength_ = i2;
            this.remainingMatchLength_ = i2;
            this.maxLength_ = i3;
            int i4 = this.maxLength_;
            this.entry_ = new Entry(i4 == 0 ? 32 : i4);
            int i5 = this.remainingMatchLength_;
            if (i5 >= 0) {
                int i6 = i5 + 1;
                int i7 = this.maxLength_;
                if (i7 > 0 && i6 > i7) {
                    i6 = i7;
                }
                this.entry_.append(this.bytes_, this.pos_, i6);
                this.pos_ += i6;
                this.remainingMatchLength_ -= i6;
            }
        }

        public Iterator reset() {
            this.pos_ = this.initialPos_;
            this.remainingMatchLength_ = this.initialRemainingMatchLength_;
            int i = this.remainingMatchLength_ + 1;
            int i2 = this.maxLength_;
            if (i2 > 0 && i > i2) {
                i = i2;
            }
            this.entry_.truncateString(i);
            this.pos_ += i;
            this.remainingMatchLength_ -= i;
            this.stack_.clear();
            return this;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.pos_ >= 0 || !this.stack_.isEmpty();
        }

        @Override // java.util.Iterator
        public Entry next() {
            int i;
            int i2;
            int i3 = this.pos_;
            boolean z = true;
            if (i3 < 0) {
                if (!this.stack_.isEmpty()) {
                    ArrayList<Long> arrayList = this.stack_;
                    long longValue = arrayList.remove(arrayList.size() - 1).longValue();
                    int i4 = (int) longValue;
                    int i5 = (int) (longValue >> 32);
                    this.entry_.truncateString(65535 & i4);
                    int i6 = i4 >>> 16;
                    if (i6 > 1) {
                        i3 = branchNext(i5, i6);
                        if (i3 < 0) {
                            return this.entry_;
                        }
                    } else {
                        this.entry_.append(this.bytes_[i5]);
                        i3 = i5 + 1;
                    }
                } else {
                    throw new NoSuchElementException();
                }
            }
            if (this.remainingMatchLength_ >= 0) {
                return truncateAndStop();
            }
            while (true) {
                int i7 = i3 + 1;
                int i8 = this.bytes_[i3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
                if (i8 >= 32) {
                    if ((i8 & 1) == 0) {
                        z = false;
                    }
                    this.entry_.value = BytesTrie.readValue(this.bytes_, i7, i8 >> 1);
                    if (z || (this.maxLength_ > 0 && this.entry_.length == this.maxLength_)) {
                        this.pos_ = -1;
                    } else {
                        this.pos_ = BytesTrie.skipValue(i7, i8);
                    }
                    return this.entry_;
                } else if (this.maxLength_ > 0 && this.entry_.length == this.maxLength_) {
                    return truncateAndStop();
                } else {
                    if (i8 < 16) {
                        if (i8 == 0) {
                            i2 = i7 + 1;
                            i8 = this.bytes_[i7] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
                        } else {
                            i2 = i7;
                        }
                        i3 = branchNext(i2, i8 + 1);
                        if (i3 < 0) {
                            return this.entry_;
                        }
                    } else {
                        int i9 = (i8 - 16) + 1;
                        if (this.maxLength_ <= 0 || this.entry_.length + i9 <= (i = this.maxLength_)) {
                            this.entry_.append(this.bytes_, i7, i9);
                            i3 = i7 + i9;
                        } else {
                            Entry entry = this.entry_;
                            entry.append(this.bytes_, i7, i - entry.length);
                            return truncateAndStop();
                        }
                    }
                }
            }
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Entry truncateAndStop() {
            this.pos_ = -1;
            Entry entry = this.entry_;
            entry.value = -1;
            return entry;
        }

        private int branchNext(int i, int i2) {
            while (i2 > 5) {
                int i3 = i + 1;
                int i4 = i2 >> 1;
                this.stack_.add(Long.valueOf((((long) BytesTrie.skipDelta(this.bytes_, i3)) << 32) | ((long) ((i2 - i4) << 16)) | ((long) this.entry_.length)));
                i = BytesTrie.jumpByDelta(this.bytes_, i3);
                i2 = i4;
            }
            byte[] bArr = this.bytes_;
            int i5 = i + 1;
            byte b = bArr[i];
            int i6 = i5 + 1;
            int i7 = bArr[i5] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            boolean z = (i7 & 1) != 0;
            int readValue = BytesTrie.readValue(this.bytes_, i6, i7 >> 1);
            int skipValue = BytesTrie.skipValue(i6, i7);
            this.stack_.add(Long.valueOf((((long) skipValue) << 32) | ((long) ((i2 - 1) << 16)) | ((long) this.entry_.length)));
            this.entry_.append(b);
            if (!z) {
                return skipValue + readValue;
            }
            this.pos_ = -1;
            this.entry_.value = readValue;
            return -1;
        }
    }

    private void stop() {
        this.pos_ = -1;
    }

    /* access modifiers changed from: private */
    public static int readValue(byte[] bArr, int i, int i2) {
        int i3;
        byte b;
        if (i2 < kMinTwoByteValueLead) {
            return i2 - 16;
        }
        if (i2 < kMinThreeByteValueLead) {
            i3 = (i2 - kMinTwoByteValueLead) << 8;
            b = bArr[i];
        } else if (i2 < kFourByteValueLead) {
            i3 = ((i2 - kMinThreeByteValueLead) << 16) | ((bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8);
            b = bArr[i + 1];
        } else if (i2 == kFourByteValueLead) {
            i3 = ((bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 16) | ((bArr[i + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8);
            b = bArr[i + 2];
        } else {
            i3 = (bArr[i] << CameraMetadataEx.HUAWEI_EXPOSURE_28) | ((bArr[i + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 16) | ((bArr[i + 2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8);
            b = bArr[i + 3];
        }
        return i3 | (b & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
    }

    private static int skipValue(byte[] bArr, int i) {
        return skipValue(i + 1, bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
    }

    /* access modifiers changed from: private */
    public static int jumpByDelta(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
        if (i3 >= kMinTwoByteDeltaLead) {
            if (i3 < 240) {
                i3 = ((i3 - 192) << 8) | (bArr[i2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                i2++;
            } else if (i3 < kFourByteDeltaLead) {
                i3 = ((i3 - 240) << 16) | ((bArr[i2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8) | (bArr[i2 + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                i2 += 2;
            } else if (i3 == kFourByteDeltaLead) {
                i3 = ((bArr[i2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 16) | ((bArr[i2 + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8) | (bArr[i2 + 2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                i2 += 3;
            } else {
                i3 = (bArr[i2] << CameraMetadataEx.HUAWEI_EXPOSURE_28) | ((bArr[i2 + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 16) | ((bArr[i2 + 2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8) | (bArr[i2 + 3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                i2 += 4;
            }
        }
        return i2 + i3;
    }

    /* access modifiers changed from: private */
    public static int skipDelta(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
        if (i3 < kMinTwoByteDeltaLead) {
            return i2;
        }
        if (i3 < 240) {
            return i2 + 1;
        }
        return i3 < kFourByteDeltaLead ? i2 + 2 : i2 + (i3 & 1) + 3;
    }

    private Result branchNext(int i, int i2, int i3) {
        Result result;
        int i4;
        if (i2 == 0) {
            i2 = this.bytes_[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            i++;
        }
        int i5 = i2 + 1;
        while (i5 > 5) {
            byte[] bArr = this.bytes_;
            int i6 = i + 1;
            if (i3 < (bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE)) {
                i5 >>= 1;
                i = jumpByDelta(bArr, i6);
            } else {
                i5 -= i5 >> 1;
                i = skipDelta(bArr, i6);
            }
        }
        do {
            byte[] bArr2 = this.bytes_;
            int i7 = i + 1;
            if (i3 == (bArr2[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE)) {
                int i8 = bArr2[i7] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
                if ((i8 & 1) != 0) {
                    result = Result.FINAL_VALUE;
                } else {
                    int i9 = i7 + 1;
                    int i10 = i8 >> 1;
                    if (i10 < kMinTwoByteValueLead) {
                        i4 = i10 - 16;
                    } else if (i10 < kMinThreeByteValueLead) {
                        i4 = ((i10 - kMinTwoByteValueLead) << 8) | (bArr2[i9] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                        i9++;
                    } else if (i10 < kFourByteValueLead) {
                        i4 = ((i10 - kMinThreeByteValueLead) << 16) | ((bArr2[i9] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8) | (bArr2[i9 + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                        i9 += 2;
                    } else if (i10 == kFourByteValueLead) {
                        i4 = ((bArr2[i9] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 16) | ((bArr2[i9 + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8) | (bArr2[i9 + 2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                        i9 += 3;
                    } else {
                        i4 = (bArr2[i9] << CameraMetadataEx.HUAWEI_EXPOSURE_28) | ((bArr2[i9 + 1] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 16) | ((bArr2[i9 + 2] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) << 8) | (bArr2[i9 + 3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
                        i9 += 4;
                    }
                    i7 = i9 + i4;
                    int i11 = this.bytes_[i7] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
                    result = i11 >= 32 ? valueResults_[i11 & 1] : Result.NO_VALUE;
                }
                this.pos_ = i7;
                return result;
            }
            i5--;
            i = skipValue(bArr2, i7);
        } while (i5 > 1);
        byte[] bArr3 = this.bytes_;
        int i12 = i + 1;
        if (i3 == (bArr3[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE)) {
            this.pos_ = i12;
            int i13 = bArr3[i12] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            return i13 >= 32 ? valueResults_[i13 & 1] : Result.NO_VALUE;
        }
        stop();
        return Result.NO_MATCH;
    }

    private Result nextImpl(int i, int i2) {
        int i3;
        while (true) {
            byte[] bArr = this.bytes_;
            int i4 = i + 1;
            int i5 = bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            if (i5 < 16) {
                return branchNext(i4, i5, i2);
            }
            if (i5 < 32) {
                int i6 = i5 - 16;
                int i7 = i4 + 1;
                if (i2 == (bArr[i4] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE)) {
                    int i8 = i6 - 1;
                    this.remainingMatchLength_ = i8;
                    this.pos_ = i7;
                    return (i8 >= 0 || (i3 = bArr[i7] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE) < 32) ? Result.NO_VALUE : valueResults_[i3 & 1];
                }
            } else if ((i5 & 1) != 0) {
                break;
            } else {
                i = skipValue(i4, i5);
            }
        }
        stop();
        return Result.NO_MATCH;
    }

    private static long findUniqueValueFromBranch(byte[] bArr, int i, int i2, long j) {
        while (i2 > 5) {
            int i3 = i + 1;
            int i4 = i2 >> 1;
            j = findUniqueValueFromBranch(bArr, jumpByDelta(bArr, i3), i4, j);
            if (j == 0) {
                return 0;
            }
            i2 -= i4;
            i = skipDelta(bArr, i3);
        }
        do {
            int i5 = i + 1;
            int i6 = i5 + 1;
            int i7 = bArr[i5] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            boolean z = (i7 & 1) != 0;
            int readValue = readValue(bArr, i6, i7 >> 1);
            i = skipValue(i6, i7);
            if (!z) {
                j = findUniqueValue(bArr, readValue + i, j);
                if (j == 0) {
                    return 0;
                }
            } else if (j == 0) {
                j = (((long) readValue) << 1) | 1;
            } else if (readValue != ((int) (j >> 1))) {
                return 0;
            }
            i2--;
        } while (i2 > 1);
        return (((long) (i + 1)) << 33) | (j & 8589934591L);
    }

    private static long findUniqueValue(byte[] bArr, int i, long j) {
        int i2;
        int i3;
        while (true) {
            int i4 = i + 1;
            int i5 = bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
            if (i5 < 16) {
                if (i5 == 0) {
                    i2 = i4 + 1;
                    i3 = bArr[i4] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE;
                } else {
                    i3 = i5;
                    i2 = i4;
                }
                long findUniqueValueFromBranch = findUniqueValueFromBranch(bArr, i2, i3 + 1, j);
                if (findUniqueValueFromBranch == 0) {
                    return 0;
                }
                i = (int) (findUniqueValueFromBranch >>> 33);
                j = findUniqueValueFromBranch;
            } else if (i5 < 32) {
                i = i4 + (i5 - 16) + 1;
            } else {
                boolean z = (i5 & 1) != 0;
                int readValue = readValue(bArr, i4, i5 >> 1);
                if (j == 0) {
                    j = (((long) readValue) << 1) | 1;
                } else if (readValue != ((int) (j >> 1))) {
                    return 0;
                }
                if (z) {
                    return j;
                }
                i = skipValue(i4, i5);
            }
        }
    }

    private static void getNextBranchBytes(byte[] bArr, int i, int i2, Appendable appendable) {
        while (i2 > 5) {
            int i3 = i + 1;
            int i4 = i2 >> 1;
            getNextBranchBytes(bArr, jumpByDelta(bArr, i3), i4, appendable);
            i2 -= i4;
            i = skipDelta(bArr, i3);
        }
        do {
            append(appendable, bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
            i = skipValue(bArr, i + 1);
            i2--;
        } while (i2 > 1);
        append(appendable, bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE);
    }

    private static void append(Appendable appendable, int i) {
        try {
            appendable.append((char) i);
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }
}

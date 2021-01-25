package ohos.global.icu.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.util.BytesTrie;

public final class CharsTrie implements Cloneable, Iterable<Entry> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int kMaxBranchLinearSubNodeLength = 5;
    static final int kMaxLinearMatchLength = 16;
    static final int kMaxOneUnitDelta = 64511;
    static final int kMaxOneUnitNodeValue = 255;
    static final int kMaxOneUnitValue = 16383;
    static final int kMaxTwoUnitDelta = 67043327;
    static final int kMaxTwoUnitNodeValue = 16646143;
    static final int kMaxTwoUnitValue = 1073676287;
    static final int kMinLinearMatch = 48;
    static final int kMinTwoUnitDeltaLead = 64512;
    static final int kMinTwoUnitNodeValueLead = 16448;
    static final int kMinTwoUnitValueLead = 16384;
    static final int kMinValueLead = 64;
    static final int kNodeTypeMask = 63;
    static final int kThreeUnitDeltaLead = 65535;
    static final int kThreeUnitNodeValueLead = 32704;
    static final int kThreeUnitValueLead = 32767;
    static final int kValueIsFinal = 32768;
    private static BytesTrie.Result[] valueResults_ = {BytesTrie.Result.INTERMEDIATE_VALUE, BytesTrie.Result.FINAL_VALUE};
    private CharSequence chars_;
    private int pos_;
    private int remainingMatchLength_;
    private int root_;

    public static final class State {
        private CharSequence chars;
        private int pos;
        private int remainingMatchLength;
        private int root;
    }

    /* access modifiers changed from: private */
    public static int skipNodeValue(int i, int i2) {
        return i2 >= kMinTwoUnitNodeValueLead ? i2 < kThreeUnitNodeValueLead ? i + 1 : i + 2 : i;
    }

    /* access modifiers changed from: private */
    public static int skipValue(int i, int i2) {
        return i2 >= 16384 ? i2 < kThreeUnitValueLead ? i + 1 : i + 2 : i;
    }

    public CharsTrie(CharSequence charSequence, int i) {
        this.chars_ = charSequence;
        this.root_ = i;
        this.pos_ = i;
        this.remainingMatchLength_ = -1;
    }

    public CharsTrie(CharsTrie charsTrie) {
        this.chars_ = charsTrie.chars_;
        this.root_ = charsTrie.root_;
        this.pos_ = charsTrie.pos_;
        this.remainingMatchLength_ = charsTrie.remainingMatchLength_;
    }

    @Override // java.lang.Object
    public CharsTrie clone() throws CloneNotSupportedException {
        return (CharsTrie) super.clone();
    }

    public CharsTrie reset() {
        this.pos_ = this.root_;
        this.remainingMatchLength_ = -1;
        return this;
    }

    public long getState64() {
        return (((long) this.remainingMatchLength_) << 32) | ((long) this.pos_);
    }

    public CharsTrie resetToState64(long j) {
        this.remainingMatchLength_ = (int) (j >> 32);
        this.pos_ = (int) j;
        return this;
    }

    public CharsTrie saveState(State state) {
        state.chars = this.chars_;
        state.root = this.root_;
        state.pos = this.pos_;
        state.remainingMatchLength = this.remainingMatchLength_;
        return this;
    }

    public CharsTrie resetToState(State state) {
        if (this.chars_ == state.chars && this.chars_ != null && this.root_ == state.root) {
            this.pos_ = state.pos;
            this.remainingMatchLength_ = state.remainingMatchLength;
            return this;
        }
        throw new IllegalArgumentException("incompatible trie state");
    }

    public BytesTrie.Result current() {
        char charAt;
        int i = this.pos_;
        if (i < 0) {
            return BytesTrie.Result.NO_MATCH;
        }
        return (this.remainingMatchLength_ >= 0 || (charAt = this.chars_.charAt(i)) < '@') ? BytesTrie.Result.NO_VALUE : valueResults_[charAt >> 15];
    }

    public BytesTrie.Result first(int i) {
        this.remainingMatchLength_ = -1;
        return nextImpl(this.root_, i);
    }

    public BytesTrie.Result firstForCodePoint(int i) {
        if (i <= kThreeUnitDeltaLead) {
            return first(i);
        }
        if (first(UTF16.getLeadSurrogate(i)).hasNext()) {
            return next(UTF16.getTrailSurrogate(i));
        }
        return BytesTrie.Result.NO_MATCH;
    }

    public BytesTrie.Result next(int i) {
        char charAt;
        int i2 = this.pos_;
        if (i2 < 0) {
            return BytesTrie.Result.NO_MATCH;
        }
        int i3 = this.remainingMatchLength_;
        if (i3 < 0) {
            return nextImpl(i2, i);
        }
        int i4 = i2 + 1;
        if (i == this.chars_.charAt(i2)) {
            int i5 = i3 - 1;
            this.remainingMatchLength_ = i5;
            this.pos_ = i4;
            return (i5 >= 0 || (charAt = this.chars_.charAt(i4)) < '@') ? BytesTrie.Result.NO_VALUE : valueResults_[charAt >> 15];
        }
        stop();
        return BytesTrie.Result.NO_MATCH;
    }

    public BytesTrie.Result nextForCodePoint(int i) {
        if (i <= kThreeUnitDeltaLead) {
            return next(i);
        }
        if (next(UTF16.getLeadSurrogate(i)).hasNext()) {
            return next(UTF16.getTrailSurrogate(i));
        }
        return BytesTrie.Result.NO_MATCH;
    }

    public BytesTrie.Result next(CharSequence charSequence, int i, int i2) {
        char charAt;
        if (i >= i2) {
            return current();
        }
        int i3 = this.pos_;
        if (i3 < 0) {
            return BytesTrie.Result.NO_MATCH;
        }
        int i4 = this.remainingMatchLength_;
        while (i != i2) {
            int i5 = i + 1;
            char charAt2 = charSequence.charAt(i);
            if (i4 < 0) {
                this.remainingMatchLength_ = i4;
                int i6 = i3 + 1;
                char charAt3 = this.chars_.charAt(i3);
                while (true) {
                    if (charAt3 < kMinLinearMatch) {
                        BytesTrie.Result branchNext = branchNext(i6, charAt3, charAt2);
                        if (branchNext == BytesTrie.Result.NO_MATCH) {
                            return BytesTrie.Result.NO_MATCH;
                        }
                        if (i5 == i2) {
                            return branchNext;
                        }
                        if (branchNext == BytesTrie.Result.FINAL_VALUE) {
                            stop();
                            return BytesTrie.Result.NO_MATCH;
                        }
                        char charAt4 = charSequence.charAt(i5);
                        int i7 = this.pos_;
                        i6 = i7 + 1;
                        i5++;
                        charAt2 = charAt4;
                        charAt3 = this.chars_.charAt(i7);
                    } else if (charAt3 < 64) {
                        int i8 = charAt3 - 48;
                        if (charAt2 != this.chars_.charAt(i6)) {
                            stop();
                            return BytesTrie.Result.NO_MATCH;
                        }
                        i4 = i8 - 1;
                        i3 = i6 + 1;
                    } else if ((32768 & charAt3) != 0) {
                        stop();
                        return BytesTrie.Result.NO_MATCH;
                    } else {
                        i6 = skipNodeValue(i6, charAt3);
                        charAt3 &= kNodeTypeMask;
                    }
                }
            } else if (charAt2 != this.chars_.charAt(i3)) {
                stop();
                return BytesTrie.Result.NO_MATCH;
            } else {
                i3++;
                i4--;
            }
            i = i5;
        }
        this.remainingMatchLength_ = i4;
        this.pos_ = i3;
        return (i4 >= 0 || (charAt = this.chars_.charAt(i3)) < '@') ? BytesTrie.Result.NO_VALUE : valueResults_[charAt >> 15];
    }

    public int getValue() {
        int i = this.pos_;
        int i2 = i + 1;
        char charAt = this.chars_.charAt(i);
        return (32768 & charAt) != 0 ? readValue(this.chars_, i2, charAt & 32767) : readNodeValue(this.chars_, i2, charAt);
    }

    public long getUniqueValue() {
        int i = this.pos_;
        if (i < 0) {
            return 0;
        }
        return (findUniqueValue(this.chars_, (i + this.remainingMatchLength_) + 1, 0) << 31) >> 31;
    }

    public int getNextChars(Appendable appendable) {
        int i;
        int i2 = this.pos_;
        if (i2 < 0) {
            return 0;
        }
        if (this.remainingMatchLength_ >= 0) {
            append(appendable, this.chars_.charAt(i2));
            return 1;
        }
        int i3 = i2 + 1;
        char charAt = this.chars_.charAt(i2);
        if (charAt >= 64) {
            if ((32768 & charAt) != 0) {
                return 0;
            }
            i3 = skipNodeValue(i3, charAt);
            charAt &= kNodeTypeMask;
        }
        if (charAt < kMinLinearMatch) {
            if (charAt == 0) {
                i = i3 + 1;
                charAt = this.chars_.charAt(i3);
            } else {
                i = i3;
            }
            int i4 = charAt + 1;
            getNextBranchChars(this.chars_, i, i4, appendable);
            return i4;
        }
        append(appendable, this.chars_.charAt(i3));
        return 1;
    }

    /* Return type fixed from 'ohos.global.icu.util.CharsTrie$Iterator' to match base method */
    @Override // java.lang.Iterable
    public java.util.Iterator<Entry> iterator() {
        return new Iterator(this.chars_, this.pos_, this.remainingMatchLength_, 0);
    }

    public Iterator iterator(int i) {
        return new Iterator(this.chars_, this.pos_, this.remainingMatchLength_, i);
    }

    public static Iterator iterator(CharSequence charSequence, int i, int i2) {
        return new Iterator(charSequence, i, -1, i2);
    }

    public static final class Entry {
        public CharSequence chars;
        public int value;

        private Entry() {
        }
    }

    public static final class Iterator implements java.util.Iterator<Entry> {
        private CharSequence chars_;
        private Entry entry_;
        private int initialPos_;
        private int initialRemainingMatchLength_;
        private int maxLength_;
        private int pos_;
        private int remainingMatchLength_;
        private boolean skipValue_;
        private ArrayList<Long> stack_;
        private StringBuilder str_;

        private Iterator(CharSequence charSequence, int i, int i2, int i3) {
            this.str_ = new StringBuilder();
            this.entry_ = new Entry();
            this.stack_ = new ArrayList<>();
            this.chars_ = charSequence;
            this.initialPos_ = i;
            this.pos_ = i;
            this.initialRemainingMatchLength_ = i2;
            this.remainingMatchLength_ = i2;
            this.maxLength_ = i3;
            int i4 = this.remainingMatchLength_;
            if (i4 >= 0) {
                int i5 = i4 + 1;
                int i6 = this.maxLength_;
                if (i6 > 0 && i5 > i6) {
                    i5 = i6;
                }
                StringBuilder sb = this.str_;
                CharSequence charSequence2 = this.chars_;
                int i7 = this.pos_;
                sb.append(charSequence2, i7, i7 + i5);
                this.pos_ += i5;
                this.remainingMatchLength_ -= i5;
            }
        }

        public Iterator reset() {
            this.pos_ = this.initialPos_;
            this.remainingMatchLength_ = this.initialRemainingMatchLength_;
            this.skipValue_ = false;
            int i = this.remainingMatchLength_ + 1;
            int i2 = this.maxLength_;
            if (i2 > 0 && i > i2) {
                i = i2;
            }
            this.str_.setLength(i);
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
            if (i3 < 0) {
                if (!this.stack_.isEmpty()) {
                    ArrayList<Long> arrayList = this.stack_;
                    long longValue = arrayList.remove(arrayList.size() - 1).longValue();
                    int i4 = (int) longValue;
                    int i5 = (int) (longValue >> 32);
                    this.str_.setLength(CharsTrie.kThreeUnitDeltaLead & i4);
                    int i6 = i4 >>> 16;
                    if (i6 > 1) {
                        i3 = branchNext(i5, i6);
                        if (i3 < 0) {
                            return this.entry_;
                        }
                    } else {
                        this.str_.append(this.chars_.charAt(i5));
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
                char charAt = this.chars_.charAt(i3);
                if (charAt >= 64) {
                    boolean z = false;
                    if (this.skipValue_) {
                        i7 = CharsTrie.skipNodeValue(i7, charAt);
                        charAt &= CharsTrie.kNodeTypeMask;
                        this.skipValue_ = false;
                    } else {
                        if ((32768 & charAt) != 0) {
                            z = true;
                        }
                        if (z) {
                            this.entry_.value = CharsTrie.readValue(this.chars_, i7, charAt & CharsTrie.kThreeUnitValueLead);
                        } else {
                            this.entry_.value = CharsTrie.readNodeValue(this.chars_, i7, charAt);
                        }
                        if (z || (this.maxLength_ > 0 && this.str_.length() == this.maxLength_)) {
                            this.pos_ = -1;
                        } else {
                            this.pos_ = i7 - 1;
                            this.skipValue_ = true;
                        }
                        Entry entry = this.entry_;
                        entry.chars = this.str_;
                        return entry;
                    }
                }
                if (this.maxLength_ > 0 && this.str_.length() == this.maxLength_) {
                    return truncateAndStop();
                }
                if (charAt < CharsTrie.kMinLinearMatch) {
                    if (charAt == 0) {
                        i2 = i7 + 1;
                        charAt = this.chars_.charAt(i7);
                    } else {
                        i2 = i7;
                    }
                    i3 = branchNext(i2, charAt + 1);
                    if (i3 < 0) {
                        return this.entry_;
                    }
                } else {
                    int i8 = (charAt - 48) + 1;
                    if (this.maxLength_ <= 0 || this.str_.length() + i8 <= (i = this.maxLength_)) {
                        i3 = i8 + i7;
                        this.str_.append(this.chars_, i7, i3);
                    } else {
                        StringBuilder sb = this.str_;
                        sb.append(this.chars_, i7, (i + i7) - sb.length());
                        return truncateAndStop();
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
            entry.chars = this.str_;
            entry.value = -1;
            return entry;
        }

        private int branchNext(int i, int i2) {
            while (i2 > 5) {
                int i3 = i + 1;
                int i4 = i2 >> 1;
                this.stack_.add(Long.valueOf((((long) CharsTrie.skipDelta(this.chars_, i3)) << 32) | ((long) ((i2 - i4) << 16)) | ((long) this.str_.length())));
                i = CharsTrie.jumpByDelta(this.chars_, i3);
                i2 = i4;
            }
            int i5 = i + 1;
            char charAt = this.chars_.charAt(i);
            int i6 = i5 + 1;
            char charAt2 = this.chars_.charAt(i5);
            boolean z = (32768 & charAt2) != 0;
            int i7 = charAt2 & 32767;
            int readValue = CharsTrie.readValue(this.chars_, i6, i7);
            int skipValue = CharsTrie.skipValue(i6, i7);
            this.stack_.add(Long.valueOf((((long) skipValue) << 32) | ((long) ((i2 - 1) << 16)) | ((long) this.str_.length())));
            this.str_.append(charAt);
            if (!z) {
                return skipValue + readValue;
            }
            this.pos_ = -1;
            Entry entry = this.entry_;
            entry.chars = this.str_;
            entry.value = readValue;
            return -1;
        }
    }

    private void stop() {
        this.pos_ = -1;
    }

    /* access modifiers changed from: private */
    public static int readValue(CharSequence charSequence, int i, int i2) {
        int i3;
        char c;
        if (i2 < 16384) {
            return i2;
        }
        if (i2 < kThreeUnitValueLead) {
            i3 = (i2 - 16384) << 16;
            c = charSequence.charAt(i);
        } else {
            i3 = charSequence.charAt(i) << 16;
            c = charSequence.charAt(i + 1);
        }
        return i3 | c;
    }

    private static int skipValue(CharSequence charSequence, int i) {
        return skipValue(i + 1, charSequence.charAt(i) & 32767);
    }

    /* access modifiers changed from: private */
    public static int readNodeValue(CharSequence charSequence, int i, int i2) {
        int i3;
        char c;
        if (i2 < kMinTwoUnitNodeValueLead) {
            return (i2 >> 6) - 1;
        }
        if (i2 < kThreeUnitNodeValueLead) {
            i3 = ((i2 & kThreeUnitNodeValueLead) - kMinTwoUnitNodeValueLead) << 10;
            c = charSequence.charAt(i);
        } else {
            i3 = charSequence.charAt(i) << 16;
            c = charSequence.charAt(i + 1);
        }
        return c | i3;
    }

    /* access modifiers changed from: private */
    public static int jumpByDelta(CharSequence charSequence, int i) {
        int i2 = i + 1;
        int charAt = charSequence.charAt(i);
        if (charAt >= kMinTwoUnitDeltaLead) {
            if (charAt == kThreeUnitDeltaLead) {
                charAt = (charSequence.charAt(i2) << 16) | charSequence.charAt(i2 + 1);
                i2 += 2;
            } else {
                charAt = ((charAt - kMinTwoUnitDeltaLead) << 16) | charSequence.charAt(i2);
                i2++;
            }
        }
        return i2 + charAt;
    }

    /* access modifiers changed from: private */
    public static int skipDelta(CharSequence charSequence, int i) {
        int i2 = i + 1;
        char charAt = charSequence.charAt(i);
        if (charAt >= kMinTwoUnitDeltaLead) {
            return charAt == kThreeUnitDeltaLead ? i2 + 2 : i2 + 1;
        }
        return i2;
    }

    private BytesTrie.Result branchNext(int i, int i2, int i3) {
        BytesTrie.Result result;
        if (i2 == 0) {
            i2 = this.chars_.charAt(i);
            i++;
        }
        int i4 = i2 + 1;
        while (i4 > 5) {
            int i5 = i + 1;
            if (i3 < this.chars_.charAt(i)) {
                i4 >>= 1;
                i = jumpByDelta(this.chars_, i5);
            } else {
                i4 -= i4 >> 1;
                i = skipDelta(this.chars_, i5);
            }
        }
        do {
            int i6 = i + 1;
            if (i3 == this.chars_.charAt(i)) {
                int charAt = this.chars_.charAt(i6);
                if ((32768 & charAt) != 0) {
                    result = BytesTrie.Result.FINAL_VALUE;
                } else {
                    int i7 = i6 + 1;
                    if (charAt >= 16384) {
                        if (charAt < kThreeUnitValueLead) {
                            charAt = ((charAt - 16384) << 16) | this.chars_.charAt(i7);
                            i7++;
                        } else {
                            charAt = (this.chars_.charAt(i7) << 16) | this.chars_.charAt(i7 + 1);
                            i7 += 2;
                        }
                    }
                    i6 = i7 + charAt;
                    char charAt2 = this.chars_.charAt(i6);
                    result = charAt2 >= '@' ? valueResults_[charAt2 >> 15] : BytesTrie.Result.NO_VALUE;
                }
                this.pos_ = i6;
                return result;
            }
            i4--;
            i = skipValue(this.chars_, i6);
        } while (i4 > 1);
        int i8 = i + 1;
        if (i3 == this.chars_.charAt(i)) {
            this.pos_ = i8;
            char charAt3 = this.chars_.charAt(i8);
            return charAt3 >= '@' ? valueResults_[charAt3 >> 15] : BytesTrie.Result.NO_VALUE;
        }
        stop();
        return BytesTrie.Result.NO_MATCH;
    }

    private BytesTrie.Result nextImpl(int i, int i2) {
        char charAt;
        int i3 = i + 1;
        int charAt2 = this.chars_.charAt(i);
        while (charAt2 >= kMinLinearMatch) {
            if (charAt2 < 64) {
                int i4 = charAt2 - kMinLinearMatch;
                int i5 = i3 + 1;
                if (i2 == this.chars_.charAt(i3)) {
                    int i6 = i4 - 1;
                    this.remainingMatchLength_ = i6;
                    this.pos_ = i5;
                    return (i6 >= 0 || (charAt = this.chars_.charAt(i5)) < '@') ? BytesTrie.Result.NO_VALUE : valueResults_[charAt >> 15];
                }
            } else if ((32768 & charAt2) == 0) {
                i3 = skipNodeValue(i3, charAt2);
                charAt2 &= kNodeTypeMask;
            }
            stop();
            return BytesTrie.Result.NO_MATCH;
        }
        return branchNext(i3, charAt2, i2);
    }

    private static long findUniqueValueFromBranch(CharSequence charSequence, int i, int i2, long j) {
        while (i2 > 5) {
            int i3 = i + 1;
            int i4 = i2 >> 1;
            j = findUniqueValueFromBranch(charSequence, jumpByDelta(charSequence, i3), i4, j);
            if (j == 0) {
                return 0;
            }
            i2 -= i4;
            i = skipDelta(charSequence, i3);
        }
        do {
            int i5 = i + 1;
            int i6 = i5 + 1;
            char charAt = charSequence.charAt(i5);
            boolean z = (32768 & charAt) != 0;
            int i7 = charAt & 32767;
            int readValue = readValue(charSequence, i6, i7);
            i = skipValue(i6, i7);
            if (!z) {
                j = findUniqueValue(charSequence, readValue + i, j);
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

    private static long findUniqueValue(CharSequence charSequence, int i, long j) {
        int i2;
        int i3;
        int i4;
        int i5 = i + 1;
        int charAt = charSequence.charAt(i);
        while (true) {
            if (charAt < kMinLinearMatch) {
                if (charAt == 0) {
                    i3 = i5 + 1;
                    i4 = charSequence.charAt(i5);
                } else {
                    i4 = charAt;
                    i3 = i5;
                }
                j = findUniqueValueFromBranch(charSequence, i3, i4 + 1, j);
                if (j == 0) {
                    return 0;
                }
                int i6 = (int) (j >>> 33);
                i5 = i6 + 1;
                charAt = charSequence.charAt(i6);
            } else if (charAt < 64) {
                int i7 = i5 + (charAt - 48) + 1;
                int i8 = i7 + 1;
                char charAt2 = charSequence.charAt(i7);
                i5 = i8;
                charAt = charAt2;
            } else {
                boolean z = (32768 & charAt) != 0;
                if (z) {
                    i2 = readValue(charSequence, i5, charAt & kThreeUnitValueLead);
                } else {
                    i2 = readNodeValue(charSequence, i5, charAt);
                }
                if (j == 0) {
                    j = (((long) i2) << 1) | 1;
                } else if (i2 != ((int) (j >> 1))) {
                    return 0;
                }
                if (z) {
                    return j;
                }
                i5 = skipNodeValue(i5, charAt);
                charAt &= kNodeTypeMask;
            }
        }
    }

    private static void getNextBranchChars(CharSequence charSequence, int i, int i2, Appendable appendable) {
        while (i2 > 5) {
            int i3 = i + 1;
            int i4 = i2 >> 1;
            getNextBranchChars(charSequence, jumpByDelta(charSequence, i3), i4, appendable);
            i2 -= i4;
            i = skipDelta(charSequence, i3);
        }
        do {
            append(appendable, charSequence.charAt(i));
            i = skipValue(charSequence, i + 1);
            i2--;
        } while (i2 > 1);
        append(appendable, charSequence.charAt(i));
    }

    private static void append(Appendable appendable, int i) {
        try {
            appendable.append((char) i);
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }
}

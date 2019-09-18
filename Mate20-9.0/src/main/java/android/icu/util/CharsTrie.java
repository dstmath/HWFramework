package android.icu.util;

import android.icu.text.UTF16;
import android.icu.util.BytesTrie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

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
    private int remainingMatchLength_ = -1;
    private int root_;

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

        private Iterator(CharSequence trieChars, int offset, int remainingMatchLength, int maxStringLength) {
            this.str_ = new StringBuilder();
            this.entry_ = new Entry();
            this.stack_ = new ArrayList<>();
            this.chars_ = trieChars;
            this.initialPos_ = offset;
            this.pos_ = offset;
            this.initialRemainingMatchLength_ = remainingMatchLength;
            this.remainingMatchLength_ = remainingMatchLength;
            this.maxLength_ = maxStringLength;
            int length = this.remainingMatchLength_;
            if (length >= 0) {
                int length2 = length + 1;
                if (this.maxLength_ > 0 && length2 > this.maxLength_) {
                    length2 = this.maxLength_;
                }
                this.str_.append(this.chars_, this.pos_, this.pos_ + length2);
                this.pos_ += length2;
                this.remainingMatchLength_ -= length2;
            }
        }

        public Iterator reset() {
            this.pos_ = this.initialPos_;
            this.remainingMatchLength_ = this.initialRemainingMatchLength_;
            this.skipValue_ = false;
            int length = this.remainingMatchLength_ + 1;
            if (this.maxLength_ > 0 && length > this.maxLength_) {
                length = this.maxLength_;
            }
            this.str_.setLength(length);
            this.pos_ += length;
            this.remainingMatchLength_ -= length;
            this.stack_.clear();
            return this;
        }

        public boolean hasNext() {
            return this.pos_ >= 0 || !this.stack_.isEmpty();
        }

        public Entry next() {
            int node;
            int node2 = this.pos_;
            if (node2 < 0) {
                if (!this.stack_.isEmpty()) {
                    long top = this.stack_.remove(this.stack_.size() - 1).longValue();
                    int length = (int) top;
                    int pos = (int) (top >> 32);
                    this.str_.setLength(65535 & length);
                    int length2 = length >>> 16;
                    if (length2 > 1) {
                        node2 = branchNext(pos, length2);
                        if (node2 < 0) {
                            return this.entry_;
                        }
                    } else {
                        this.str_.append(this.chars_.charAt(pos));
                        node2 = pos + 1;
                    }
                } else {
                    throw new NoSuchElementException();
                }
            }
            if (this.remainingMatchLength_ >= 0) {
                return truncateAndStop();
            }
            while (true) {
                int pos2 = node + 1;
                int node3 = this.chars_.charAt(node);
                if (node3 >= 64) {
                    boolean z = false;
                    if (this.skipValue_) {
                        pos2 = CharsTrie.skipNodeValue(pos2, node3);
                        node3 &= 63;
                        this.skipValue_ = false;
                    } else {
                        if ((32768 & node3) != 0) {
                            z = true;
                        }
                        boolean isFinal = z;
                        if (isFinal) {
                            this.entry_.value = CharsTrie.readValue(this.chars_, pos2, node3 & CharsTrie.kThreeUnitValueLead);
                        } else {
                            this.entry_.value = CharsTrie.readNodeValue(this.chars_, pos2, node3);
                        }
                        if (isFinal || (this.maxLength_ > 0 && this.str_.length() == this.maxLength_)) {
                            this.pos_ = -1;
                        } else {
                            this.pos_ = pos2 - 1;
                            this.skipValue_ = true;
                        }
                        this.entry_.chars = this.str_;
                        return this.entry_;
                    }
                }
                if (this.maxLength_ > 0 && this.str_.length() == this.maxLength_) {
                    return truncateAndStop();
                }
                if (node3 < 48) {
                    if (node3 == 0) {
                        node3 = this.chars_.charAt(pos2);
                        pos2++;
                    }
                    int pos3 = branchNext(pos2, node3 + 1);
                    if (pos3 < 0) {
                        return this.entry_;
                    }
                    node = pos3;
                } else {
                    int length3 = (node3 - 48) + 1;
                    if (this.maxLength_ <= 0 || this.str_.length() + length3 <= this.maxLength_) {
                        this.str_.append(this.chars_, pos2, pos2 + length3);
                        node = pos2 + length3;
                    } else {
                        this.str_.append(this.chars_, pos2, (this.maxLength_ + pos2) - this.str_.length());
                        return truncateAndStop();
                    }
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private Entry truncateAndStop() {
            this.pos_ = -1;
            this.entry_.chars = this.str_;
            this.entry_.value = -1;
            return this.entry_;
        }

        private int branchNext(int pos, int length) {
            while (length > 5) {
                int pos2 = pos + 1;
                this.stack_.add(Long.valueOf((((long) CharsTrie.skipDelta(this.chars_, pos2)) << 32) | ((long) ((length - (length >> 1)) << 16)) | ((long) this.str_.length())));
                length >>= 1;
                pos = CharsTrie.jumpByDelta(this.chars_, pos2);
            }
            int pos3 = pos + 1;
            char trieUnit = this.chars_.charAt(pos);
            int pos4 = pos3 + 1;
            int node = this.chars_.charAt(pos3);
            boolean isFinal = (32768 & node) != 0;
            CharSequence charSequence = this.chars_;
            int node2 = node & CharsTrie.kThreeUnitValueLead;
            int value = CharsTrie.readValue(charSequence, pos4, node2);
            int pos5 = CharsTrie.skipValue(pos4, node2);
            this.stack_.add(Long.valueOf((((long) pos5) << 32) | ((long) ((length - 1) << 16)) | ((long) this.str_.length())));
            this.str_.append(trieUnit);
            if (!isFinal) {
                return pos5 + value;
            }
            this.pos_ = -1;
            this.entry_.chars = this.str_;
            this.entry_.value = value;
            return -1;
        }
    }

    public static final class State {
        /* access modifiers changed from: private */
        public CharSequence chars;
        /* access modifiers changed from: private */
        public int pos;
        /* access modifiers changed from: private */
        public int remainingMatchLength;
        /* access modifiers changed from: private */
        public int root;
    }

    public CharsTrie(CharSequence trieChars, int offset) {
        this.chars_ = trieChars;
        this.root_ = offset;
        this.pos_ = offset;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public CharsTrie reset() {
        this.pos_ = this.root_;
        this.remainingMatchLength_ = -1;
        return this;
    }

    public CharsTrie saveState(State state) {
        CharSequence unused = state.chars = this.chars_;
        int unused2 = state.root = this.root_;
        int unused3 = state.pos = this.pos_;
        int unused4 = state.remainingMatchLength = this.remainingMatchLength_;
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
        BytesTrie.Result result;
        int pos = this.pos_;
        if (pos < 0) {
            return BytesTrie.Result.NO_MATCH;
        }
        if (this.remainingMatchLength_ < 0) {
            int charAt = this.chars_.charAt(pos);
            int node = charAt;
            if (charAt >= 64) {
                result = valueResults_[node >> 15];
                return result;
            }
        }
        result = BytesTrie.Result.NO_VALUE;
        return result;
    }

    public BytesTrie.Result first(int inUnit) {
        this.remainingMatchLength_ = -1;
        return nextImpl(this.root_, inUnit);
    }

    public BytesTrie.Result firstForCodePoint(int cp) {
        if (cp <= 65535) {
            return first(cp);
        }
        if (first(UTF16.getLeadSurrogate(cp)).hasNext()) {
            return next(UTF16.getTrailSurrogate(cp));
        }
        return BytesTrie.Result.NO_MATCH;
    }

    public BytesTrie.Result next(int inUnit) {
        BytesTrie.Result result;
        int pos = this.pos_;
        if (pos < 0) {
            return BytesTrie.Result.NO_MATCH;
        }
        int length = this.remainingMatchLength_;
        if (length < 0) {
            return nextImpl(pos, inUnit);
        }
        int pos2 = pos + 1;
        if (inUnit == this.chars_.charAt(pos)) {
            int length2 = length - 1;
            this.remainingMatchLength_ = length2;
            this.pos_ = pos2;
            if (length2 < 0) {
                int charAt = this.chars_.charAt(pos2);
                int node = charAt;
                if (charAt >= 64) {
                    result = valueResults_[node >> 15];
                    return result;
                }
            }
            result = BytesTrie.Result.NO_VALUE;
            return result;
        }
        stop();
        return BytesTrie.Result.NO_MATCH;
    }

    public BytesTrie.Result nextForCodePoint(int cp) {
        if (cp <= 65535) {
            return next(cp);
        }
        if (next(UTF16.getLeadSurrogate(cp)).hasNext()) {
            return next(UTF16.getTrailSurrogate(cp));
        }
        return BytesTrie.Result.NO_MATCH;
    }

    public BytesTrie.Result next(CharSequence s, int sIndex, int sLimit) {
        BytesTrie.Result result;
        if (sIndex >= sLimit) {
            return current();
        }
        int pos = this.pos_;
        if (pos < 0) {
            return BytesTrie.Result.NO_MATCH;
        }
        int length = this.remainingMatchLength_;
        while (sIndex != sLimit) {
            int sIndex2 = sIndex + 1;
            char inUnit = s.charAt(sIndex);
            if (length < 0) {
                this.remainingMatchLength_ = length;
                int pos2 = pos + 1;
                int node = this.chars_.charAt(pos);
                while (true) {
                    if (node < 48) {
                        BytesTrie.Result result2 = branchNext(pos2, node, inUnit);
                        if (result2 == BytesTrie.Result.NO_MATCH) {
                            return BytesTrie.Result.NO_MATCH;
                        }
                        if (sIndex2 == sLimit) {
                            return result2;
                        }
                        if (result2 == BytesTrie.Result.FINAL_VALUE) {
                            stop();
                            return BytesTrie.Result.NO_MATCH;
                        }
                        int sIndex3 = sIndex2 + 1;
                        inUnit = s.charAt(sIndex2);
                        int sIndex4 = this.pos_;
                        int pos3 = sIndex4 + 1;
                        node = this.chars_.charAt(sIndex4);
                        sIndex2 = sIndex3;
                        pos2 = pos3;
                    } else if (node < 64) {
                        int length2 = node - 48;
                        if (inUnit != this.chars_.charAt(pos2)) {
                            stop();
                            return BytesTrie.Result.NO_MATCH;
                        }
                        length = length2 - 1;
                        pos = pos2 + 1;
                    } else if ((32768 & node) != 0) {
                        stop();
                        return BytesTrie.Result.NO_MATCH;
                    } else {
                        pos2 = skipNodeValue(pos2, node);
                        node &= 63;
                    }
                }
            } else if (inUnit != this.chars_.charAt(pos)) {
                stop();
                return BytesTrie.Result.NO_MATCH;
            } else {
                pos++;
                length--;
            }
            sIndex = sIndex2;
        }
        this.remainingMatchLength_ = length;
        this.pos_ = pos;
        if (length < 0) {
            int charAt = this.chars_.charAt(pos);
            int node2 = charAt;
            if (charAt >= 64) {
                result = valueResults_[node2 >> 15];
                return result;
            }
        }
        result = BytesTrie.Result.NO_VALUE;
        return result;
    }

    public int getValue() {
        int pos = this.pos_;
        int pos2 = pos + 1;
        int leadUnit = this.chars_.charAt(pos);
        return (32768 & leadUnit) != 0 ? readValue(this.chars_, pos2, leadUnit & kThreeUnitValueLead) : readNodeValue(this.chars_, pos2, leadUnit);
    }

    public long getUniqueValue() {
        int pos = this.pos_;
        if (pos < 0) {
            return 0;
        }
        return (findUniqueValue(this.chars_, (this.remainingMatchLength_ + pos) + 1, 0) << 31) >> 31;
    }

    public int getNextChars(Appendable out) {
        int pos;
        int pos2 = this.pos_;
        if (pos2 < 0) {
            return 0;
        }
        if (this.remainingMatchLength_ >= 0) {
            append(out, this.chars_.charAt(pos2));
            return 1;
        }
        int pos3 = pos2 + 1;
        int node = this.chars_.charAt(pos2);
        if (node >= 64) {
            if ((32768 & node) != 0) {
                return 0;
            }
            pos3 = skipNodeValue(pos3, node);
            node &= 63;
        }
        if (node < 48) {
            if (node == 0) {
                pos = pos3 + 1;
                node = this.chars_.charAt(pos3);
            } else {
                pos = pos3;
            }
            int node2 = node + 1;
            getNextBranchChars(this.chars_, pos, node2, out);
            return node2;
        }
        append(out, this.chars_.charAt(pos3));
        return 1;
    }

    public Iterator iterator() {
        Iterator iterator = new Iterator(this.chars_, this.pos_, this.remainingMatchLength_, 0);
        return iterator;
    }

    public Iterator iterator(int maxStringLength) {
        Iterator iterator = new Iterator(this.chars_, this.pos_, this.remainingMatchLength_, maxStringLength);
        return iterator;
    }

    public static Iterator iterator(CharSequence trieChars, int offset, int maxStringLength) {
        Iterator iterator = new Iterator(trieChars, offset, -1, maxStringLength);
        return iterator;
    }

    private void stop() {
        this.pos_ = -1;
    }

    /* access modifiers changed from: private */
    public static int readValue(CharSequence chars, int pos, int leadUnit) {
        if (leadUnit < 16384) {
            return leadUnit;
        }
        if (leadUnit < kThreeUnitValueLead) {
            return ((leadUnit - 16384) << 16) | chars.charAt(pos);
        }
        return (chars.charAt(pos) << 16) | chars.charAt(pos + 1);
    }

    /* access modifiers changed from: private */
    public static int skipValue(int pos, int leadUnit) {
        if (leadUnit < 16384) {
            return pos;
        }
        if (leadUnit < kThreeUnitValueLead) {
            return pos + 1;
        }
        return pos + 2;
    }

    private static int skipValue(CharSequence chars, int pos) {
        return skipValue(pos + 1, chars.charAt(pos) & kThreeUnitValueLead);
    }

    /* access modifiers changed from: private */
    public static int readNodeValue(CharSequence chars, int pos, int leadUnit) {
        if (leadUnit < kMinTwoUnitNodeValueLead) {
            return (leadUnit >> 6) - 1;
        }
        if (leadUnit < kThreeUnitNodeValueLead) {
            return (((leadUnit & kThreeUnitNodeValueLead) - kMinTwoUnitNodeValueLead) << 10) | chars.charAt(pos);
        }
        return (chars.charAt(pos) << 16) | chars.charAt(pos + 1);
    }

    /* access modifiers changed from: private */
    public static int skipNodeValue(int pos, int leadUnit) {
        if (leadUnit < kMinTwoUnitNodeValueLead) {
            return pos;
        }
        if (leadUnit < kThreeUnitNodeValueLead) {
            return pos + 1;
        }
        return pos + 2;
    }

    /* access modifiers changed from: private */
    public static int jumpByDelta(CharSequence chars, int pos) {
        int pos2 = pos + 1;
        int delta = chars.charAt(pos);
        if (delta >= 64512) {
            if (delta == 65535) {
                delta = (chars.charAt(pos2) << 16) | chars.charAt(pos2 + 1);
                pos2 += 2;
            } else {
                delta = ((delta - 64512) << 16) | chars.charAt(pos2);
                pos2++;
            }
        }
        return pos2 + delta;
    }

    /* access modifiers changed from: private */
    public static int skipDelta(CharSequence chars, int pos) {
        int pos2 = pos + 1;
        int delta = chars.charAt(pos);
        if (delta < 64512) {
            return pos2;
        }
        if (delta == 65535) {
            return pos2 + 2;
        }
        return pos2 + 1;
    }

    private BytesTrie.Result branchNext(int pos, int length, int inUnit) {
        BytesTrie.Result result;
        int delta;
        if (length == 0) {
            length = this.chars_.charAt(pos);
            pos++;
        }
        int length2 = length + 1;
        while (length2 > 5) {
            int pos2 = pos + 1;
            if (inUnit < this.chars_.charAt(pos)) {
                length2 >>= 1;
                pos = jumpByDelta(this.chars_, pos2);
            } else {
                length2 -= length2 >> 1;
                pos = skipDelta(this.chars_, pos2);
            }
        }
        do {
            int pos3 = pos + 1;
            if (inUnit == this.chars_.charAt(pos)) {
                int node = this.chars_.charAt(pos3);
                if ((32768 & node) != 0) {
                    result = BytesTrie.Result.FINAL_VALUE;
                } else {
                    int pos4 = pos3 + 1;
                    if (node < 16384) {
                        delta = node;
                    } else if (node < kThreeUnitValueLead) {
                        delta = ((node - 16384) << 16) | this.chars_.charAt(pos4);
                        pos4++;
                    } else {
                        delta = (this.chars_.charAt(pos4) << 16) | this.chars_.charAt(pos4 + 1);
                        pos4 += 2;
                    }
                    pos3 = pos4 + delta;
                    int node2 = this.chars_.charAt(pos3);
                    result = node2 >= 64 ? valueResults_[node2 >> 15] : BytesTrie.Result.NO_VALUE;
                }
                this.pos_ = pos3;
                return result;
            }
            length2--;
            pos = skipValue(this.chars_, pos3);
        } while (length2 > 1);
        int pos5 = pos + 1;
        if (inUnit == this.chars_.charAt(pos)) {
            this.pos_ = pos5;
            int node3 = this.chars_.charAt(pos5);
            return node3 >= 64 ? valueResults_[node3 >> 15] : BytesTrie.Result.NO_VALUE;
        }
        stop();
        return BytesTrie.Result.NO_MATCH;
    }

    private BytesTrie.Result nextImpl(int pos, int inUnit) {
        BytesTrie.Result result;
        int pos2 = pos + 1;
        int node = this.chars_.charAt(pos);
        while (node >= 48) {
            if (node < 64) {
                int length = node - 48;
                int pos3 = pos2 + 1;
                if (inUnit == this.chars_.charAt(pos2)) {
                    int length2 = length - 1;
                    this.remainingMatchLength_ = length2;
                    this.pos_ = pos3;
                    if (length2 < 0) {
                        int charAt = this.chars_.charAt(pos3);
                        int node2 = charAt;
                        if (charAt >= 64) {
                            result = valueResults_[node2 >> 15];
                            return result;
                        }
                    }
                    result = BytesTrie.Result.NO_VALUE;
                    return result;
                }
                int i = pos3;
            } else if ((32768 & node) == 0) {
                pos2 = skipNodeValue(pos2, node);
                node &= 63;
            }
            stop();
            return BytesTrie.Result.NO_MATCH;
        }
        return branchNext(pos2, node, inUnit);
    }

    private static long findUniqueValueFromBranch(CharSequence chars, int pos, int length, long uniqueValue) {
        while (length > 5) {
            int pos2 = pos + 1;
            uniqueValue = findUniqueValueFromBranch(chars, jumpByDelta(chars, pos2), length >> 1, uniqueValue);
            if (uniqueValue == 0) {
                return 0;
            }
            length -= length >> 1;
            pos = skipDelta(chars, pos2);
        }
        while (true) {
            int pos3 = pos + 1;
            int pos4 = pos3 + 1;
            int node = chars.charAt(pos3);
            boolean isFinal = (32768 & node) != 0;
            int node2 = node & kThreeUnitValueLead;
            int value = readValue(chars, pos4, node2);
            int pos5 = skipValue(pos4, node2);
            if (!isFinal) {
                uniqueValue = findUniqueValue(chars, pos5 + value, uniqueValue);
                if (uniqueValue == 0) {
                    return 0;
                }
            } else if (uniqueValue == 0) {
                uniqueValue = (((long) value) << 1) | 1;
            } else if (value != ((int) (uniqueValue >> 1))) {
                return 0;
            }
            length--;
            if (length <= 1) {
                return (((long) (pos5 + 1)) << 33) | (8589934591L & uniqueValue);
            }
            pos = pos5;
        }
    }

    private static long findUniqueValue(CharSequence chars, int pos, long uniqueValue) {
        int pos2;
        int value;
        int pos3 = pos + 1;
        int node = chars.charAt(pos);
        while (true) {
            if (node < 48) {
                if (node == 0) {
                    node = chars.charAt(pos3);
                    pos3++;
                }
                uniqueValue = findUniqueValueFromBranch(chars, pos3, node + 1, uniqueValue);
                if (uniqueValue == 0) {
                    return 0;
                }
                int pos4 = (int) (uniqueValue >>> 33);
                pos2 = pos4 + 1;
                node = chars.charAt(pos4);
            } else if (node < 64) {
                int pos5 = pos3 + (node - 48) + 1;
                pos2 = pos5 + 1;
                node = chars.charAt(pos5);
            } else {
                boolean isFinal = (32768 & node) != 0;
                if (isFinal) {
                    value = readValue(chars, pos3, node & kThreeUnitValueLead);
                } else {
                    value = readNodeValue(chars, pos3, node);
                }
                if (uniqueValue == 0) {
                    uniqueValue = (((long) value) << 1) | 1;
                } else if (value != ((int) (uniqueValue >> 1))) {
                    return 0;
                }
                if (isFinal) {
                    return uniqueValue;
                }
                pos3 = skipNodeValue(pos3, node);
                node &= 63;
            }
            pos3 = pos2;
        }
    }

    private static void getNextBranchChars(CharSequence chars, int pos, int length, Appendable out) {
        while (length > 5) {
            int pos2 = pos + 1;
            getNextBranchChars(chars, jumpByDelta(chars, pos2), length >> 1, out);
            length -= length >> 1;
            pos = skipDelta(chars, pos2);
        }
        do {
            append(out, chars.charAt(pos));
            pos = skipValue(chars, pos + 1);
            length--;
        } while (length > 1);
        append(out, chars.charAt(pos));
    }

    private static void append(Appendable out, int c) {
        try {
            out.append((char) c);
        } catch (IOException e) {
            throw new ICUUncheckedIOException((Throwable) e);
        }
    }
}

package android.icu.util;

import android.icu.text.UTF16;
import android.icu.util.BytesTrie.Result;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public final class CharsTrie implements Cloneable, Iterable<Entry> {
    static final /* synthetic */ boolean -assertionsDisabled = (CharsTrie.class.desiredAssertionStatus() ^ 1);
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
    private static Result[] valueResults_ = new Result[]{Result.INTERMEDIATE_VALUE, Result.FINAL_VALUE};
    private CharSequence chars_;
    private int pos_;
    private int remainingMatchLength_ = -1;
    private int root_;

    public static final class Entry {
        public CharSequence chars;
        public int value;

        /* synthetic */ Entry(Entry -this0) {
            this();
        }

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

        /* synthetic */ Iterator(CharSequence trieChars, int offset, int remainingMatchLength, int maxStringLength, Iterator -this4) {
            this(trieChars, offset, remainingMatchLength, maxStringLength);
        }

        private Iterator(CharSequence trieChars, int offset, int remainingMatchLength, int maxStringLength) {
            this.str_ = new StringBuilder();
            this.entry_ = new Entry();
            this.stack_ = new ArrayList();
            this.chars_ = trieChars;
            this.initialPos_ = offset;
            this.pos_ = offset;
            this.initialRemainingMatchLength_ = remainingMatchLength;
            this.remainingMatchLength_ = remainingMatchLength;
            this.maxLength_ = maxStringLength;
            int length = this.remainingMatchLength_;
            if (length >= 0) {
                length++;
                if (this.maxLength_ > 0 && length > this.maxLength_) {
                    length = this.maxLength_;
                }
                this.str_.append(this.chars_, this.pos_, this.pos_ + length);
                this.pos_ += length;
                this.remainingMatchLength_ -= length;
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
            return this.pos_ < 0 ? this.stack_.isEmpty() ^ 1 : true;
        }

        public Entry next() {
            int length;
            int pos;
            int pos2 = this.pos_;
            if (pos2 < 0) {
                if (this.stack_.isEmpty()) {
                    throw new NoSuchElementException();
                }
                long top = ((Long) this.stack_.remove(this.stack_.size() - 1)).longValue();
                length = (int) top;
                pos2 = (int) (top >> 32);
                this.str_.setLength(65535 & length);
                length >>>= 16;
                if (length > 1) {
                    pos2 = branchNext(pos2, length);
                    if (pos2 < 0) {
                        return this.entry_;
                    }
                }
                pos = pos2 + 1;
                this.str_.append(this.chars_.charAt(pos2));
                pos2 = pos;
            }
            if (this.remainingMatchLength_ >= 0) {
                return truncateAndStop();
            }
            while (true) {
                pos = pos2 + 1;
                int node = this.chars_.charAt(pos2);
                if (node < 64) {
                    pos2 = pos;
                } else if (this.skipValue_) {
                    pos2 = CharsTrie.skipNodeValue(pos, node);
                    node &= 63;
                    this.skipValue_ = false;
                } else {
                    boolean isFinal = (32768 & node) != 0;
                    if (isFinal) {
                        this.entry_.value = CharsTrie.readValue(this.chars_, pos, node & CharsTrie.kThreeUnitValueLead);
                    } else {
                        this.entry_.value = CharsTrie.readNodeValue(this.chars_, pos, node);
                    }
                    if (isFinal || (this.maxLength_ > 0 && this.str_.length() == this.maxLength_)) {
                        this.pos_ = -1;
                    } else {
                        this.pos_ = pos - 1;
                        this.skipValue_ = true;
                    }
                    this.entry_.chars = this.str_;
                    return this.entry_;
                }
                if (this.maxLength_ > 0 && this.str_.length() == this.maxLength_) {
                    return truncateAndStop();
                }
                if (node < 48) {
                    if (node == 0) {
                        pos = pos2 + 1;
                        node = this.chars_.charAt(pos2);
                        pos2 = pos;
                    }
                    pos2 = branchNext(pos2, node + 1);
                    if (pos2 < 0) {
                        return this.entry_;
                    }
                } else {
                    length = (node - 48) + 1;
                    if (this.maxLength_ <= 0 || this.str_.length() + length <= this.maxLength_) {
                        this.str_.append(this.chars_, pos2, pos2 + length);
                        pos2 += length;
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
            int pos2;
            while (true) {
                pos2 = pos;
                if (length <= 5) {
                    break;
                }
                pos = pos2 + 1;
                this.stack_.add(Long.valueOf(((((long) CharsTrie.skipDelta(this.chars_, pos)) << 32) | ((long) ((length - (length >> 1)) << 16))) | ((long) this.str_.length())));
                length >>= 1;
                pos = CharsTrie.jumpByDelta(this.chars_, pos);
            }
            pos = pos2 + 1;
            char trieUnit = this.chars_.charAt(pos2);
            pos2 = pos + 1;
            int node = this.chars_.charAt(pos);
            boolean isFinal = (32768 & node) != 0;
            node &= CharsTrie.kThreeUnitValueLead;
            int value = CharsTrie.readValue(this.chars_, pos2, node);
            pos = CharsTrie.skipValue(pos2, node);
            this.stack_.add(Long.valueOf(((((long) pos) << 32) | ((long) ((length - 1) << 16))) | ((long) this.str_.length())));
            this.str_.append(trieUnit);
            if (!isFinal) {
                return pos + value;
            }
            this.pos_ = -1;
            this.entry_.chars = this.str_;
            this.entry_.value = value;
            return -1;
        }
    }

    public static final class State {
        private CharSequence chars;
        private int pos;
        private int remainingMatchLength;
        private int root;
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

    public Result current() {
        int pos = this.pos_;
        if (pos < 0) {
            return Result.NO_MATCH;
        }
        Result result;
        if (this.remainingMatchLength_ < 0) {
            int node = this.chars_.charAt(pos);
            if (node >= 64) {
                result = valueResults_[node >> 15];
                return result;
            }
        }
        result = Result.NO_VALUE;
        return result;
    }

    public Result first(int inUnit) {
        this.remainingMatchLength_ = -1;
        return nextImpl(this.root_, inUnit);
    }

    public Result firstForCodePoint(int cp) {
        if (cp <= 65535) {
            return first(cp);
        }
        if (first(UTF16.getLeadSurrogate(cp)).hasNext()) {
            return next(UTF16.getTrailSurrogate(cp));
        }
        return Result.NO_MATCH;
    }

    public Result next(int inUnit) {
        int pos = this.pos_;
        if (pos < 0) {
            return Result.NO_MATCH;
        }
        int length = this.remainingMatchLength_;
        if (length < 0) {
            return nextImpl(pos, inUnit);
        }
        int pos2 = pos + 1;
        if (inUnit == this.chars_.charAt(pos)) {
            Result result;
            length--;
            this.remainingMatchLength_ = length;
            this.pos_ = pos2;
            if (length < 0) {
                int node = this.chars_.charAt(pos2);
                if (node >= 64) {
                    result = valueResults_[node >> 15];
                    return result;
                }
            }
            result = Result.NO_VALUE;
            return result;
        }
        stop();
        return Result.NO_MATCH;
    }

    public Result nextForCodePoint(int cp) {
        if (cp <= 65535) {
            return next(cp);
        }
        if (next(UTF16.getLeadSurrogate(cp)).hasNext()) {
            return next(UTF16.getTrailSurrogate(cp));
        }
        return Result.NO_MATCH;
    }

    public Result next(CharSequence s, int sIndex, int sLimit) {
        if (sIndex >= sLimit) {
            return current();
        }
        int pos = this.pos_;
        if (pos < 0) {
            return Result.NO_MATCH;
        }
        int node;
        Result result;
        int length = this.remainingMatchLength_;
        int pos2 = pos;
        int sIndex2 = sIndex;
        while (sIndex2 != sLimit) {
            sIndex = sIndex2 + 1;
            char inUnit = s.charAt(sIndex2);
            if (length < 0) {
                this.remainingMatchLength_ = length;
                pos = pos2 + 1;
                node = this.chars_.charAt(pos2);
                while (true) {
                    sIndex2 = sIndex;
                    if (node < 48) {
                        Result result2 = branchNext(pos, node, inUnit);
                        if (result2 == Result.NO_MATCH) {
                            return Result.NO_MATCH;
                        }
                        if (sIndex2 == sLimit) {
                            return result2;
                        }
                        if (result2 == Result.FINAL_VALUE) {
                            stop();
                            return Result.NO_MATCH;
                        }
                        sIndex = sIndex2 + 1;
                        inUnit = s.charAt(sIndex2);
                        pos = this.pos_;
                        pos2 = pos + 1;
                        node = this.chars_.charAt(pos);
                        pos = pos2;
                    } else if (node < 64) {
                        length = node - 48;
                        if (inUnit != this.chars_.charAt(pos)) {
                            stop();
                            return Result.NO_MATCH;
                        }
                        length--;
                        pos2 = pos + 1;
                    } else if ((32768 & node) != 0) {
                        stop();
                        return Result.NO_MATCH;
                    } else {
                        pos = skipNodeValue(pos, node);
                        node &= 63;
                        sIndex = sIndex2;
                    }
                }
            } else if (inUnit != this.chars_.charAt(pos2)) {
                stop();
                return Result.NO_MATCH;
            } else {
                length--;
                pos2++;
                sIndex2 = sIndex;
            }
        }
        this.remainingMatchLength_ = length;
        this.pos_ = pos2;
        if (length < 0) {
            node = this.chars_.charAt(pos2);
            if (node >= 64) {
                result = valueResults_[node >> 15];
                return result;
            }
        }
        result = Result.NO_VALUE;
        return result;
    }

    public int getValue() {
        int pos = this.pos_;
        int pos2 = pos + 1;
        int leadUnit = this.chars_.charAt(pos);
        if (-assertionsDisabled || leadUnit >= 64) {
            return (32768 & leadUnit) != 0 ? readValue(this.chars_, pos2, leadUnit & kThreeUnitValueLead) : readNodeValue(this.chars_, pos2, leadUnit);
        } else {
            throw new AssertionError();
        }
    }

    public long getUniqueValue() {
        int pos = this.pos_;
        if (pos < 0) {
            return 0;
        }
        return (findUniqueValue(this.chars_, (this.remainingMatchLength_ + pos) + 1, 0) << 31) >> 31;
    }

    public int getNextChars(Appendable out) {
        int pos = this.pos_;
        if (pos < 0) {
            return 0;
        }
        if (this.remainingMatchLength_ >= 0) {
            append(out, this.chars_.charAt(pos));
            return 1;
        }
        int pos2 = pos + 1;
        int node = this.chars_.charAt(pos);
        if (node >= 64) {
            if ((32768 & node) != 0) {
                return 0;
            }
            pos = skipNodeValue(pos2, node);
            node &= 63;
            pos2 = pos;
        }
        if (node < 48) {
            if (node == 0) {
                pos = pos2 + 1;
                node = this.chars_.charAt(pos2);
            } else {
                pos = pos2;
            }
            node++;
            getNextBranchChars(this.chars_, pos, node, out);
            return node;
        }
        append(out, this.chars_.charAt(pos2));
        return 1;
    }

    public Iterator iterator() {
        return new Iterator(this.chars_, this.pos_, this.remainingMatchLength_, 0, null);
    }

    public Iterator iterator(int maxStringLength) {
        return new Iterator(this.chars_, this.pos_, this.remainingMatchLength_, maxStringLength, null);
    }

    public static Iterator iterator(CharSequence trieChars, int offset, int maxStringLength) {
        return new Iterator(trieChars, offset, -1, maxStringLength, null);
    }

    private void stop() {
        this.pos_ = -1;
    }

    private static int readValue(CharSequence chars, int pos, int leadUnit) {
        if (leadUnit < 16384) {
            return leadUnit;
        }
        if (leadUnit < kThreeUnitValueLead) {
            return ((leadUnit - 16384) << 16) | chars.charAt(pos);
        }
        return (chars.charAt(pos) << 16) | chars.charAt(pos + 1);
    }

    private static int skipValue(int pos, int leadUnit) {
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

    private static int readNodeValue(CharSequence chars, int pos, int leadUnit) {
        if (!-assertionsDisabled && (64 > leadUnit || leadUnit >= 32768)) {
            throw new AssertionError();
        } else if (leadUnit < kMinTwoUnitNodeValueLead) {
            return (leadUnit >> 6) - 1;
        } else {
            if (leadUnit < kThreeUnitNodeValueLead) {
                return (((leadUnit & kThreeUnitNodeValueLead) - 16448) << 10) | chars.charAt(pos);
            }
            return (chars.charAt(pos) << 16) | chars.charAt(pos + 1);
        }
    }

    private static int skipNodeValue(int pos, int leadUnit) {
        if (!-assertionsDisabled && (64 > leadUnit || leadUnit >= 32768)) {
            throw new AssertionError();
        } else if (leadUnit < kMinTwoUnitNodeValueLead) {
            return pos;
        } else {
            if (leadUnit < kThreeUnitNodeValueLead) {
                return pos + 1;
            }
            return pos + 2;
        }
    }

    private static int jumpByDelta(CharSequence chars, int pos) {
        int pos2 = pos + 1;
        int delta = chars.charAt(pos);
        if (delta < kMinTwoUnitDeltaLead) {
            pos = pos2;
        } else if (delta == 65535) {
            delta = (chars.charAt(pos2) << 16) | chars.charAt(pos2 + 1);
            pos = pos2 + 2;
        } else {
            pos = pos2 + 1;
            delta = ((delta - kMinTwoUnitDeltaLead) << 16) | chars.charAt(pos2);
        }
        return pos + delta;
    }

    private static int skipDelta(CharSequence chars, int pos) {
        int pos2 = pos + 1;
        int delta = chars.charAt(pos);
        if (delta < kMinTwoUnitDeltaLead) {
            return pos2;
        }
        if (delta == 65535) {
            return pos2 + 2;
        }
        return pos2 + 1;
    }

    private Result branchNext(int pos, int length, int inUnit) {
        int pos2;
        int node;
        if (length == 0) {
            pos2 = pos + 1;
            length = this.chars_.charAt(pos);
            pos = pos2;
        }
        length++;
        while (true) {
            pos2 = pos;
            if (length <= 5) {
                break;
            }
            pos = pos2 + 1;
            if (inUnit < this.chars_.charAt(pos2)) {
                length >>= 1;
                pos = jumpByDelta(this.chars_, pos);
            } else {
                length -= length >> 1;
                pos = skipDelta(this.chars_, pos);
            }
        }
        pos = pos2;
        do {
            pos2 = pos + 1;
            if (inUnit == this.chars_.charAt(pos)) {
                Result result;
                node = this.chars_.charAt(pos2);
                if ((32768 & node) != 0) {
                    result = Result.FINAL_VALUE;
                    pos = pos2;
                } else {
                    int delta;
                    pos = pos2 + 1;
                    if (node < 16384) {
                        delta = node;
                    } else if (node < kThreeUnitValueLead) {
                        delta = ((node - 16384) << 16) | this.chars_.charAt(pos);
                        pos++;
                    } else {
                        delta = (this.chars_.charAt(pos) << 16) | this.chars_.charAt(pos + 1);
                        pos += 2;
                    }
                    pos += delta;
                    node = this.chars_.charAt(pos);
                    result = node >= 64 ? valueResults_[node >> 15] : Result.NO_VALUE;
                }
                this.pos_ = pos;
                return result;
            }
            length--;
            pos = skipValue(this.chars_, pos2);
        } while (length > 1);
        pos2 = pos + 1;
        if (inUnit == this.chars_.charAt(pos)) {
            this.pos_ = pos2;
            node = this.chars_.charAt(pos2);
            return node >= 64 ? valueResults_[node >> 15] : Result.NO_VALUE;
        }
        stop();
        return Result.NO_MATCH;
    }

    private Result nextImpl(int pos, int inUnit) {
        int pos2 = pos + 1;
        int node = this.chars_.charAt(pos);
        while (node >= 48) {
            if (node < 64) {
                int length = node - 48;
                pos = pos2 + 1;
                if (inUnit == this.chars_.charAt(pos2)) {
                    Result result;
                    length--;
                    this.remainingMatchLength_ = length;
                    this.pos_ = pos;
                    if (length < 0) {
                        node = this.chars_.charAt(pos);
                        if (node >= 64) {
                            result = valueResults_[node >> 15];
                            return result;
                        }
                    }
                    result = Result.NO_VALUE;
                    return result;
                }
            } else if ((32768 & node) != 0) {
                pos = pos2;
            } else {
                pos = skipNodeValue(pos2, node);
                node &= 63;
                pos2 = pos;
            }
            stop();
            return Result.NO_MATCH;
        }
        return branchNext(pos2, node, inUnit);
    }

    private static long findUniqueValueFromBranch(CharSequence chars, int pos, int length, long uniqueValue) {
        while (length > 5) {
            pos++;
            uniqueValue = findUniqueValueFromBranch(chars, jumpByDelta(chars, pos), length >> 1, uniqueValue);
            if (uniqueValue == 0) {
                return 0;
            }
            length -= length >> 1;
            pos = skipDelta(chars, pos);
        }
        do {
            pos++;
            int pos2 = pos + 1;
            int node = chars.charAt(pos);
            boolean isFinal = (32768 & node) != 0;
            node &= kThreeUnitValueLead;
            int value = readValue(chars, pos2, node);
            pos = skipValue(pos2, node);
            if (!isFinal) {
                uniqueValue = findUniqueValue(chars, pos + value, uniqueValue);
                if (uniqueValue == 0) {
                    return 0;
                }
            } else if (uniqueValue == 0) {
                uniqueValue = (((long) value) << 1) | 1;
            } else if (value != ((int) (uniqueValue >> 1))) {
                return 0;
            }
            length--;
        } while (length > 1);
        return (((long) (pos + 1)) << 33) | (8589934591L & uniqueValue);
    }

    private static long findUniqueValue(CharSequence chars, int pos, long uniqueValue) {
        int pos2 = pos + 1;
        int node = chars.charAt(pos);
        while (true) {
            if (node < 48) {
                if (node == 0) {
                    pos = pos2 + 1;
                    node = chars.charAt(pos2);
                } else {
                    pos = pos2;
                }
                uniqueValue = findUniqueValueFromBranch(chars, pos, node + 1, uniqueValue);
                if (uniqueValue == 0) {
                    return 0;
                }
                pos = (int) (uniqueValue >>> 33);
                pos2 = pos + 1;
                node = chars.charAt(pos);
                pos = pos2;
            } else if (node < 64) {
                pos = pos2 + ((node - 48) + 1);
                pos2 = pos + 1;
                node = chars.charAt(pos);
                pos = pos2;
            } else {
                int value;
                boolean isFinal = (32768 & node) != 0;
                if (isFinal) {
                    value = readValue(chars, pos2, node & kThreeUnitValueLead);
                } else {
                    value = readNodeValue(chars, pos2, node);
                }
                if (uniqueValue == 0) {
                    uniqueValue = (((long) value) << 1) | 1;
                } else if (value != ((int) (uniqueValue >> 1))) {
                    return 0;
                }
                if (isFinal) {
                    return uniqueValue;
                }
                pos = skipNodeValue(pos2, node);
                node &= 63;
            }
            pos2 = pos;
        }
    }

    private static void getNextBranchChars(CharSequence chars, int pos, int length, Appendable out) {
        while (length > 5) {
            pos++;
            getNextBranchChars(chars, jumpByDelta(chars, pos), length >> 1, out);
            length -= length >> 1;
            pos = skipDelta(chars, pos);
        }
        do {
            int pos2 = pos + 1;
            append(out, chars.charAt(pos));
            pos = skipValue(chars, pos2);
            length--;
        } while (length > 1);
        append(out, chars.charAt(pos));
    }

    private static void append(Appendable out, int c) {
        try {
            out.append((char) c);
        } catch (Throwable e) {
            throw new ICUUncheckedIOException(e);
        }
    }
}

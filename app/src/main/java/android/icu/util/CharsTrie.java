package android.icu.util;

import android.icu.text.UTF16;
import android.icu.util.BytesTrie.Result;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public final class CharsTrie implements Cloneable, Iterable<Entry> {
    static final /* synthetic */ boolean -assertionsDisabled = false;
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
    private static Result[] valueResults_;
    private CharSequence chars_;
    private int pos_;
    private int remainingMatchLength_;
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
            this.skipValue_ = CharsTrie.-assertionsDisabled;
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
            return (this.pos_ >= 0 || !this.stack_.isEmpty()) ? true : CharsTrie.-assertionsDisabled;
        }

        public Entry next() {
            int length;
            boolean isFinal = CharsTrie.-assertionsDisabled;
            int i = this.pos_;
            if (i < 0) {
                if (this.stack_.isEmpty()) {
                    throw new NoSuchElementException();
                }
                long top = ((Long) this.stack_.remove(this.stack_.size() - 1)).longValue();
                length = (int) top;
                i = (int) (top >> 32);
                this.str_.setLength(CharsTrie.kThreeUnitDeltaLead & length);
                length >>>= CharsTrie.kMaxLinearMatchLength;
                if (length > 1) {
                    i = branchNext(i, length);
                    if (i < 0) {
                        return this.entry_;
                    }
                }
                int pos = i + 1;
                this.str_.append(this.chars_.charAt(i));
                i = pos;
            }
            if (this.remainingMatchLength_ >= 0) {
                return truncateAndStop();
            }
            while (true) {
                pos = i + 1;
                int node = this.chars_.charAt(i);
                if (node >= CharsTrie.kMinValueLead) {
                    if (!this.skipValue_) {
                        break;
                    }
                    i = CharsTrie.skipNodeValue(pos, node);
                    node &= CharsTrie.kNodeTypeMask;
                    this.skipValue_ = CharsTrie.-assertionsDisabled;
                } else {
                    i = pos;
                }
                if (this.maxLength_ > 0 && this.str_.length() == this.maxLength_) {
                    return truncateAndStop();
                }
                if (node < CharsTrie.kMinLinearMatch) {
                    if (node == 0) {
                        pos = i + 1;
                        node = this.chars_.charAt(i);
                        i = pos;
                    }
                    i = branchNext(i, node + 1);
                    if (i < 0) {
                        return this.entry_;
                    }
                } else {
                    length = (node - 48) + 1;
                    if (this.maxLength_ <= 0 || this.str_.length() + length <= this.maxLength_) {
                        this.str_.append(this.chars_, i, i + length);
                        i += length;
                    } else {
                        this.str_.append(this.chars_, i, (this.maxLength_ + i) - this.str_.length());
                        return truncateAndStop();
                    }
                }
            }
            if ((CharsTrie.kValueIsFinal & node) != 0) {
                isFinal = true;
            }
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
            int pos2 = pos;
            while (length > CharsTrie.kMaxBranchLinearSubNodeLength) {
                pos = pos2 + 1;
                this.stack_.add(Long.valueOf(((((long) CharsTrie.skipDelta(this.chars_, pos)) << 32) | ((long) ((length - (length >> 1)) << CharsTrie.kMaxLinearMatchLength))) | ((long) this.str_.length())));
                length >>= 1;
                pos2 = CharsTrie.jumpByDelta(this.chars_, pos);
            }
            pos = pos2 + 1;
            char trieUnit = this.chars_.charAt(pos2);
            pos2 = pos + 1;
            int node = this.chars_.charAt(pos);
            boolean isFinal = (CharsTrie.kValueIsFinal & node) != 0 ? true : CharsTrie.-assertionsDisabled;
            node &= CharsTrie.kThreeUnitValueLead;
            int value = CharsTrie.readValue(this.chars_, pos2, node);
            pos = CharsTrie.skipValue(pos2, node);
            this.stack_.add(Long.valueOf(((((long) pos) << 32) | ((long) ((length - 1) << CharsTrie.kMaxLinearMatchLength))) | ((long) this.str_.length())));
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.CharsTrie.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.CharsTrie.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.CharsTrie.<clinit>():void");
    }

    public CharsTrie(CharSequence trieChars, int offset) {
        this.chars_ = trieChars;
        this.root_ = offset;
        this.pos_ = offset;
        this.remainingMatchLength_ = -1;
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
            if (node >= kMinValueLead) {
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
        if (cp <= kThreeUnitDeltaLead) {
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
                if (node >= kMinValueLead) {
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
        if (cp <= kThreeUnitDeltaLead) {
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
        Result result;
        int length = this.remainingMatchLength_;
        int pos2 = pos;
        int sIndex2 = sIndex;
        while (sIndex2 != sLimit) {
            int node;
            sIndex = sIndex2 + 1;
            char inUnit = s.charAt(sIndex2);
            if (length < 0) {
                this.remainingMatchLength_ = length;
                pos = pos2 + 1;
                node = this.chars_.charAt(pos2);
                sIndex2 = sIndex;
                while (true) {
                    if (node < kMinLinearMatch) {
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
                    } else if (node < kMinValueLead) {
                        break;
                    } else if ((kValueIsFinal & node) != 0) {
                        stop();
                        return Result.NO_MATCH;
                    } else {
                        pos = skipNodeValue(pos, node);
                        node &= kNodeTypeMask;
                        sIndex = sIndex2;
                    }
                    sIndex2 = sIndex;
                }
                length = node - 48;
                if (inUnit != this.chars_.charAt(pos)) {
                    stop();
                    return Result.NO_MATCH;
                }
                length--;
                pos2 = pos + 1;
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
            if (node >= kMinValueLead) {
                result = valueResults_[node >> 15];
                return result;
            }
        }
        result = Result.NO_VALUE;
        return result;
    }

    public int getValue() {
        Object obj = null;
        int pos = this.pos_;
        int pos2 = pos + 1;
        int leadUnit = this.chars_.charAt(pos);
        if (!-assertionsDisabled) {
            if (leadUnit >= kMinValueLead) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return (kValueIsFinal & leadUnit) != 0 ? readValue(this.chars_, pos2, leadUnit & kThreeUnitValueLead) : readNodeValue(this.chars_, pos2, leadUnit);
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
        if (node >= kMinValueLead) {
            if ((kValueIsFinal & node) != 0) {
                return 0;
            }
            pos = skipNodeValue(pos2, node);
            node &= kNodeTypeMask;
            pos2 = pos;
        }
        if (node < kMinLinearMatch) {
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
        return new Iterator(this.pos_, this.remainingMatchLength_, 0, null);
    }

    public Iterator iterator(int maxStringLength) {
        return new Iterator(this.pos_, this.remainingMatchLength_, maxStringLength, null);
    }

    public static Iterator iterator(CharSequence trieChars, int offset, int maxStringLength) {
        return new Iterator(offset, -1, maxStringLength, null);
    }

    private void stop() {
        this.pos_ = -1;
    }

    private static int readValue(CharSequence chars, int pos, int leadUnit) {
        if (leadUnit < kMinTwoUnitValueLead) {
            return leadUnit;
        }
        if (leadUnit < kThreeUnitValueLead) {
            return ((leadUnit - 16384) << kMaxLinearMatchLength) | chars.charAt(pos);
        }
        return (chars.charAt(pos) << kMaxLinearMatchLength) | chars.charAt(pos + 1);
    }

    private static int skipValue(int pos, int leadUnit) {
        if (leadUnit < kMinTwoUnitValueLead) {
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
        Object obj = null;
        if (!-assertionsDisabled) {
            if (kMinValueLead <= leadUnit && leadUnit < kValueIsFinal) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (leadUnit < kMinTwoUnitNodeValueLead) {
            return (leadUnit >> 6) - 1;
        }
        if (leadUnit < kThreeUnitNodeValueLead) {
            return (((leadUnit & kThreeUnitNodeValueLead) - 16448) << 10) | chars.charAt(pos);
        }
        return (chars.charAt(pos) << kMaxLinearMatchLength) | chars.charAt(pos + 1);
    }

    private static int skipNodeValue(int pos, int leadUnit) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (kMinValueLead <= leadUnit && leadUnit < kValueIsFinal) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (leadUnit < kMinTwoUnitNodeValueLead) {
            return pos;
        }
        if (leadUnit < kThreeUnitNodeValueLead) {
            return pos + 1;
        }
        return pos + 2;
    }

    private static int jumpByDelta(CharSequence chars, int pos) {
        int pos2 = pos + 1;
        int delta = chars.charAt(pos);
        if (delta < kMinTwoUnitDeltaLead) {
            pos = pos2;
        } else if (delta == kThreeUnitDeltaLead) {
            delta = (chars.charAt(pos2) << kMaxLinearMatchLength) | chars.charAt(pos2 + 1);
            pos = pos2 + 2;
        } else {
            pos = pos2 + 1;
            delta = ((delta - kMinTwoUnitDeltaLead) << kMaxLinearMatchLength) | chars.charAt(pos2);
        }
        return pos + delta;
    }

    private static int skipDelta(CharSequence chars, int pos) {
        int pos2 = pos + 1;
        int delta = chars.charAt(pos);
        if (delta < kMinTwoUnitDeltaLead) {
            return pos2;
        }
        if (delta == kThreeUnitDeltaLead) {
            return pos2 + 2;
        }
        return pos2 + 1;
    }

    private Result branchNext(int pos, int length, int inUnit) {
        int pos2;
        if (length == 0) {
            pos2 = pos + 1;
            length = this.chars_.charAt(pos);
            pos = pos2;
        }
        length++;
        pos2 = pos;
        while (length > kMaxBranchLinearSubNodeLength) {
            pos = pos2 + 1;
            if (inUnit < this.chars_.charAt(pos2)) {
                length >>= 1;
                pos = jumpByDelta(this.chars_, pos);
            } else {
                length -= length >> 1;
                pos = skipDelta(this.chars_, pos);
            }
            pos2 = pos;
        }
        pos = pos2;
        do {
            pos2 = pos + 1;
            int node;
            if (inUnit == this.chars_.charAt(pos)) {
                Result result;
                node = this.chars_.charAt(pos2);
                if ((kValueIsFinal & node) != 0) {
                    result = Result.FINAL_VALUE;
                    pos = pos2;
                } else {
                    int delta;
                    pos = pos2 + 1;
                    if (node < kMinTwoUnitValueLead) {
                        delta = node;
                    } else if (node < kThreeUnitValueLead) {
                        delta = ((node - 16384) << kMaxLinearMatchLength) | this.chars_.charAt(pos);
                        pos++;
                    } else {
                        delta = (this.chars_.charAt(pos) << kMaxLinearMatchLength) | this.chars_.charAt(pos + 1);
                        pos += 2;
                    }
                    pos += delta;
                    node = this.chars_.charAt(pos);
                    result = node >= kMinValueLead ? valueResults_[node >> 15] : Result.NO_VALUE;
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
            return node >= kMinValueLead ? valueResults_[node >> 15] : Result.NO_VALUE;
        }
        stop();
        return Result.NO_MATCH;
    }

    private Result nextImpl(int pos, int inUnit) {
        int pos2 = pos + 1;
        int node = this.chars_.charAt(pos);
        while (node >= kMinLinearMatch) {
            if (node < kMinValueLead) {
                int length = node - 48;
                pos = pos2 + 1;
                if (inUnit == this.chars_.charAt(pos2)) {
                    Result result;
                    length--;
                    this.remainingMatchLength_ = length;
                    this.pos_ = pos;
                    if (length < 0) {
                        node = this.chars_.charAt(pos);
                        if (node >= kMinValueLead) {
                            result = valueResults_[node >> 15];
                            return result;
                        }
                    }
                    result = Result.NO_VALUE;
                    return result;
                }
            } else if ((kValueIsFinal & node) != 0) {
                pos = pos2;
            } else {
                pos = skipNodeValue(pos2, node);
                node &= kNodeTypeMask;
                pos2 = pos;
            }
            stop();
            return Result.NO_MATCH;
        }
        return branchNext(pos2, node, inUnit);
    }

    private static long findUniqueValueFromBranch(CharSequence chars, int pos, int length, long uniqueValue) {
        while (length > kMaxBranchLinearSubNodeLength) {
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
            boolean isFinal = (kValueIsFinal & node) != 0 ? true : -assertionsDisabled;
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
            if (node < kMinLinearMatch) {
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
            } else if (node < kMinValueLead) {
                pos = pos2 + ((node - 48) + 1);
                pos2 = pos + 1;
                node = chars.charAt(pos);
                pos = pos2;
            } else {
                boolean isFinal;
                int value;
                if ((kValueIsFinal & node) != 0) {
                    isFinal = true;
                } else {
                    isFinal = -assertionsDisabled;
                }
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
                node &= kNodeTypeMask;
            }
            pos2 = pos;
        }
    }

    private static void getNextBranchChars(CharSequence chars, int pos, int length, Appendable out) {
        while (length > kMaxBranchLinearSubNodeLength) {
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

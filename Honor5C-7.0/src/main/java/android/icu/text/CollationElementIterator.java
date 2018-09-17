package android.icu.text;

import android.icu.impl.CharacterIteratorWrapper;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.coll.Collation;
import android.icu.impl.coll.CollationData;
import android.icu.impl.coll.CollationIterator;
import android.icu.impl.coll.CollationSettings;
import android.icu.impl.coll.ContractionsAndExpansions;
import android.icu.impl.coll.ContractionsAndExpansions.CESink;
import android.icu.impl.coll.FCDIterCollationIterator;
import android.icu.impl.coll.FCDUTF16CollationIterator;
import android.icu.impl.coll.IterCollationIterator;
import android.icu.impl.coll.UTF16CollationIterator;
import android.icu.impl.coll.UVector32;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;

public final class CollationElementIterator {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static final int IGNORABLE = 0;
    public static final int NULLORDER = -1;
    private byte dir_;
    private CollationIterator iter_;
    private UVector32 offsets_;
    private int otherHalf_;
    private RuleBasedCollator rbc_;
    private String string_;

    private static final class MaxExpSink implements CESink {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private Map<Integer, Integer> maxExpansions;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CollationElementIterator.MaxExpSink.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CollationElementIterator.MaxExpSink.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CollationElementIterator.MaxExpSink.<clinit>():void");
        }

        MaxExpSink(Map<Integer, Integer> h) {
            this.maxExpansions = h;
        }

        public void handleCE(long ce) {
        }

        public void handleExpansion(long[] ces, int start, int length) {
            if (length > 1) {
                int count = CollationElementIterator.IGNORABLE;
                for (int i = CollationElementIterator.IGNORABLE; i < length; i++) {
                    count += CollationElementIterator.ceNeedsTwoParts(ces[start + i]) ? 2 : 1;
                }
                long ce = ces[(start + length) + CollationElementIterator.NULLORDER];
                long p = ce >>> 32;
                int lower32 = (int) ce;
                int lastHalf = CollationElementIterator.getSecondHalf(p, lower32);
                if (lastHalf == 0) {
                    lastHalf = CollationElementIterator.getFirstHalf(p, lower32);
                    if (!-assertionsDisabled) {
                        if ((lastHalf != 0 ? 1 : null) == null) {
                            throw new AssertionError();
                        }
                    }
                }
                lastHalf |= Opcodes.OP_AND_LONG_2ADDR;
                Integer oldCount = (Integer) this.maxExpansions.get(Integer.valueOf(lastHalf));
                if (oldCount == null || count > oldCount.intValue()) {
                    this.maxExpansions.put(Integer.valueOf(lastHalf), Integer.valueOf(count));
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.CollationElementIterator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.CollationElementIterator.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.CollationElementIterator.<clinit>():void");
    }

    public static final int primaryOrder(int ce) {
        return (ce >>> 16) & DexFormat.MAX_TYPE_IDX;
    }

    public static final int secondaryOrder(int ce) {
        return (ce >>> 8) & Opcodes.OP_CONST_CLASS_JUMBO;
    }

    public static final int tertiaryOrder(int ce) {
        return ce & Opcodes.OP_CONST_CLASS_JUMBO;
    }

    private static final int getFirstHalf(long p, int lower32) {
        return ((((int) p) & -65536) | ((lower32 >> 16) & Normalizer2Impl.JAMO_VT)) | ((lower32 >> 8) & Opcodes.OP_CONST_CLASS_JUMBO);
    }

    private static final int getSecondHalf(long p, int lower32) {
        return ((((int) p) << 16) | ((lower32 >> 8) & Normalizer2Impl.JAMO_VT)) | (lower32 & 63);
    }

    private static final boolean ceNeedsTwoParts(long ce) {
        return (281470698455103L & ce) != 0 ? true : -assertionsDisabled;
    }

    private CollationElementIterator(RuleBasedCollator collator) {
        this.iter_ = null;
        this.rbc_ = collator;
        this.otherHalf_ = IGNORABLE;
        this.dir_ = (byte) 0;
        this.offsets_ = null;
    }

    CollationElementIterator(String source, RuleBasedCollator collator) {
        this(collator);
        setText(source);
    }

    CollationElementIterator(CharacterIterator source, RuleBasedCollator collator) {
        this(collator);
        setText(source);
    }

    CollationElementIterator(UCharacterIterator source, RuleBasedCollator collator) {
        this(collator);
        setText(source);
    }

    public int getOffset() {
        Object obj = null;
        if (this.dir_ >= null || this.offsets_ == null || this.offsets_.isEmpty()) {
            return this.iter_.getOffset();
        }
        int i = this.iter_.getCEsLength();
        if (this.otherHalf_ != 0) {
            i++;
        }
        if (!-assertionsDisabled) {
            if (i < this.offsets_.size()) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        return this.offsets_.elementAti(i);
    }

    public int next() {
        if (this.dir_ > (byte) 1) {
            if (this.otherHalf_ != 0) {
                int oh = this.otherHalf_;
                this.otherHalf_ = IGNORABLE;
                return oh;
            }
        } else if (this.dir_ == (byte) 1) {
            this.dir_ = (byte) 2;
        } else if (this.dir_ == null) {
            this.dir_ = (byte) 2;
        } else {
            throw new IllegalStateException("Illegal change of direction");
        }
        this.iter_.clearCEsIfNoneRemaining();
        long ce = this.iter_.nextCE();
        if (ce == Collation.NO_CE) {
            return NULLORDER;
        }
        long p = ce >>> 32;
        int lower32 = (int) ce;
        int firstHalf = getFirstHalf(p, lower32);
        int secondHalf = getSecondHalf(p, lower32);
        if (secondHalf != 0) {
            this.otherHalf_ = secondHalf | Opcodes.OP_AND_LONG_2ADDR;
        }
        return firstHalf;
    }

    public int previous() {
        if (this.dir_ < null) {
            if (this.otherHalf_ != 0) {
                int oh = this.otherHalf_;
                this.otherHalf_ = IGNORABLE;
                return oh;
            }
        } else if (this.dir_ == null) {
            this.iter_.resetToOffset(this.string_.length());
            this.dir_ = (byte) -1;
        } else if (this.dir_ == 1) {
            this.dir_ = (byte) -1;
        } else {
            throw new IllegalStateException("Illegal change of direction");
        }
        if (this.offsets_ == null) {
            this.offsets_ = new UVector32();
        }
        int limitOffset = this.iter_.getCEsLength() == 0 ? this.iter_.getOffset() : IGNORABLE;
        long ce = this.iter_.previousCE(this.offsets_);
        if (ce == Collation.NO_CE) {
            return NULLORDER;
        }
        long p = ce >>> 32;
        int lower32 = (int) ce;
        int firstHalf = getFirstHalf(p, lower32);
        int secondHalf = getSecondHalf(p, lower32);
        if (secondHalf == 0) {
            return firstHalf;
        }
        if (this.offsets_.isEmpty()) {
            this.offsets_.addElement(this.iter_.getOffset());
            this.offsets_.addElement(limitOffset);
        }
        this.otherHalf_ = firstHalf;
        return secondHalf | Opcodes.OP_AND_LONG_2ADDR;
    }

    public void reset() {
        this.iter_.resetToOffset(IGNORABLE);
        this.otherHalf_ = IGNORABLE;
        this.dir_ = (byte) 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setOffset(int newOffset) {
        if (newOffset > 0 && newOffset < this.string_.length()) {
            int lastSafeOffset;
            int offset = newOffset;
            do {
                char c = this.string_.charAt(offset);
                if (this.rbc_.isUnsafe(c) && (!Character.isHighSurrogate(c) || this.rbc_.isUnsafe(this.string_.codePointAt(offset)))) {
                    offset += NULLORDER;
                } else if (offset < newOffset) {
                    lastSafeOffset = offset;
                    do {
                        this.iter_.resetToOffset(lastSafeOffset);
                        do {
                            this.iter_.nextCE();
                            offset = this.iter_.getOffset();
                        } while (offset == lastSafeOffset);
                        if (offset <= newOffset) {
                            lastSafeOffset = offset;
                            continue;
                        }
                    } while (offset < newOffset);
                    newOffset = lastSafeOffset;
                }
            } while (offset > 0);
            if (offset < newOffset) {
                lastSafeOffset = offset;
                do {
                    this.iter_.resetToOffset(lastSafeOffset);
                    do {
                        this.iter_.nextCE();
                        offset = this.iter_.getOffset();
                    } while (offset == lastSafeOffset);
                    if (offset <= newOffset) {
                        lastSafeOffset = offset;
                        continue;
                    }
                } while (offset < newOffset);
                newOffset = lastSafeOffset;
            }
        }
        this.iter_.resetToOffset(newOffset);
        this.otherHalf_ = IGNORABLE;
        this.dir_ = (byte) 1;
    }

    public void setText(String source) {
        CollationIterator newIter;
        this.string_ = source;
        boolean numeric = ((CollationSettings) this.rbc_.settings.readOnly()).isNumeric();
        if (((CollationSettings) this.rbc_.settings.readOnly()).dontCheckFCD()) {
            newIter = new UTF16CollationIterator(this.rbc_.data, numeric, this.string_, IGNORABLE);
        } else {
            newIter = new FCDUTF16CollationIterator(this.rbc_.data, numeric, this.string_, IGNORABLE);
        }
        this.iter_ = newIter;
        this.otherHalf_ = IGNORABLE;
        this.dir_ = (byte) 0;
    }

    public void setText(UCharacterIterator source) {
        this.string_ = source.getText();
        try {
            CollationIterator newIter;
            UCharacterIterator src = (UCharacterIterator) source.clone();
            src.setToStart();
            boolean numeric = ((CollationSettings) this.rbc_.settings.readOnly()).isNumeric();
            if (((CollationSettings) this.rbc_.settings.readOnly()).dontCheckFCD()) {
                newIter = new IterCollationIterator(this.rbc_.data, numeric, src);
            } else {
                newIter = new FCDIterCollationIterator(this.rbc_.data, numeric, src, IGNORABLE);
            }
            this.iter_ = newIter;
            this.otherHalf_ = IGNORABLE;
            this.dir_ = (byte) 0;
        } catch (CloneNotSupportedException e) {
            setText(source.getText());
        }
    }

    public void setText(CharacterIterator source) {
        CollationIterator newIter;
        UCharacterIterator src = new CharacterIteratorWrapper(source);
        src.setToStart();
        this.string_ = src.getText();
        boolean numeric = ((CollationSettings) this.rbc_.settings.readOnly()).isNumeric();
        if (((CollationSettings) this.rbc_.settings.readOnly()).dontCheckFCD()) {
            newIter = new IterCollationIterator(this.rbc_.data, numeric, src);
        } else {
            newIter = new FCDIterCollationIterator(this.rbc_.data, numeric, src, IGNORABLE);
        }
        this.iter_ = newIter;
        this.otherHalf_ = IGNORABLE;
        this.dir_ = (byte) 0;
    }

    int strengthOrder(int order) {
        int s = ((CollationSettings) this.rbc_.settings.readOnly()).getStrength();
        if (s == 0) {
            return order & -65536;
        }
        if (s == 1) {
            return order & -256;
        }
        return order;
    }

    static final Map<Integer, Integer> computeMaxExpansions(CollationData data) {
        Map<Integer, Integer> maxExpansions = new HashMap();
        new ContractionsAndExpansions(null, null, new MaxExpSink(maxExpansions), true).forData(data);
        return maxExpansions;
    }

    public int getMaxExpansion(int ce) {
        return getMaxExpansion(this.rbc_.tailoring.maxExpansions, ce);
    }

    static int getMaxExpansion(Map<Integer, Integer> maxExpansions, int order) {
        if (order == 0) {
            return 1;
        }
        if (maxExpansions != null) {
            Integer max = (Integer) maxExpansions.get(Integer.valueOf(order));
            if (max != null) {
                return max.intValue();
            }
        }
        if ((order & Opcodes.OP_AND_LONG_2ADDR) == Opcodes.OP_AND_LONG_2ADDR) {
            return 2;
        }
        return 1;
    }

    private byte normalizeDir() {
        return this.dir_ == 1 ? (byte) 0 : this.dir_;
    }

    public boolean equals(Object that) {
        boolean z = -assertionsDisabled;
        if (that == this) {
            return true;
        }
        if (!(that instanceof CollationElementIterator)) {
            return -assertionsDisabled;
        }
        CollationElementIterator thatceiter = (CollationElementIterator) that;
        if (this.rbc_.equals(thatceiter.rbc_) && this.otherHalf_ == thatceiter.otherHalf_ && normalizeDir() == thatceiter.normalizeDir() && this.string_.equals(thatceiter.string_)) {
            z = this.iter_.equals(thatceiter.iter_);
        }
        return z;
    }

    @Deprecated
    public int hashCode() {
        if (-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    @Deprecated
    public RuleBasedCollator getRuleBasedCollator() {
        return this.rbc_;
    }
}

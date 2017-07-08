package android.icu.text;

import android.icu.impl.coll.Collation;
import android.icu.text.SearchIterator.ElementComparisonType;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import com.android.dex.DexFormat;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

public final class StringSearch extends SearchIterator {
    private static int CE_LEVEL2_BASE = 0;
    private static int CE_LEVEL3_BASE = 0;
    private static final int CE_MATCH = -1;
    private static final int CE_NO_MATCH = 0;
    private static final int CE_SKIP_PATN = 2;
    private static final int CE_SKIP_TARG = 1;
    private static final int INITIAL_ARRAY_SIZE_ = 256;
    private static final int PRIMARYORDERMASK = -65536;
    private static final int SECONDARYORDERMASK = 65280;
    private static final int TERTIARYORDERMASK = 255;
    int ceMask_;
    private RuleBasedCollator collator_;
    private Normalizer2 nfd_;
    private Pattern pattern_;
    private int strength_;
    private CollationElementIterator textIter_;
    private CollationPCE textProcessedIter_;
    private boolean toShift_;
    private CollationElementIterator utilIter_;
    int variableTop_;

    private static class CEBuffer {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        static final int CEBUFFER_EXTRA = 32;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L = 8;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_OTHER = 3;
        int bufSize_;
        CEI[] buf_;
        int firstIx_;
        int limitIx_;
        StringSearch strSearch_;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.StringSearch.CEBuffer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.StringSearch.CEBuffer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringSearch.CEBuffer.<clinit>():void");
        }

        CEBuffer(StringSearch ss) {
            this.strSearch_ = ss;
            this.bufSize_ = ss.pattern_.PCELength_ + CEBUFFER_EXTRA;
            if (ss.search_.elementComparisonType_ != ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                String patText = ss.pattern_.text_;
                if (patText != null) {
                    for (int i = StringSearch.CE_NO_MATCH; i < patText.length(); i += StringSearch.CE_SKIP_TARG) {
                        if (MIGHT_BE_JAMO_L(patText.charAt(i))) {
                            this.bufSize_ += MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L;
                        } else {
                            this.bufSize_ += MAX_TARGET_IGNORABLES_PER_PAT_OTHER;
                        }
                    }
                }
            }
            this.firstIx_ = StringSearch.CE_NO_MATCH;
            this.limitIx_ = StringSearch.CE_NO_MATCH;
            if (ss.initTextProcessedIter()) {
                this.buf_ = new CEI[this.bufSize_];
            }
        }

        CEI get(int index) {
            int i = index % this.bufSize_;
            if (index >= this.firstIx_ && index < this.limitIx_) {
                return this.buf_[i];
            }
            if (index == this.limitIx_) {
                this.limitIx_ += StringSearch.CE_SKIP_TARG;
                if (this.limitIx_ - this.firstIx_ >= this.bufSize_) {
                    this.firstIx_ += StringSearch.CE_SKIP_TARG;
                }
                Range range = new Range();
                if (this.buf_[i] == null) {
                    this.buf_[i] = new CEI();
                }
                this.buf_[i].ce_ = this.strSearch_.textProcessedIter_.nextProcessed(range);
                this.buf_[i].lowIndex_ = range.ixLow_;
                this.buf_[i].highIndex_ = range.ixHigh_;
                return this.buf_[i];
            } else if (-assertionsDisabled) {
                return null;
            } else {
                throw new AssertionError();
            }
        }

        CEI getPrevious(int index) {
            int i = index % this.bufSize_;
            if (index >= this.firstIx_ && index < this.limitIx_) {
                return this.buf_[i];
            }
            if (index == this.limitIx_) {
                this.limitIx_ += StringSearch.CE_SKIP_TARG;
                if (this.limitIx_ - this.firstIx_ >= this.bufSize_) {
                    this.firstIx_ += StringSearch.CE_SKIP_TARG;
                }
                Range range = new Range();
                if (this.buf_[i] == null) {
                    this.buf_[i] = new CEI();
                }
                this.buf_[i].ce_ = this.strSearch_.textProcessedIter_.previousProcessed(range);
                this.buf_[i].lowIndex_ = range.ixLow_;
                this.buf_[i].highIndex_ = range.ixHigh_;
                return this.buf_[i];
            } else if (-assertionsDisabled) {
                return null;
            } else {
                throw new AssertionError();
            }
        }

        static boolean MIGHT_BE_JAMO_L(char c) {
            if (c >= '\u1100' && c <= '\u115e') {
                return true;
            }
            if (c >= '\u3131' && c <= '\u314e') {
                return true;
            }
            if (c < '\u3165' || c > '\u3186') {
                return -assertionsDisabled;
            }
            return true;
        }
    }

    private static class CEI {
        long ce_;
        int highIndex_;
        int lowIndex_;

        /* synthetic */ CEI(CEI cei) {
            this();
        }

        private CEI() {
        }
    }

    private static class CollationPCE {
        private static final int BUFFER_GROW = 8;
        private static final int CONTINUATION_MARKER = 192;
        private static final int DEFAULT_BUFFER_SIZE = 16;
        private static final int PRIMARYORDERMASK = -65536;
        public static final long PROCESSED_NULLORDER = -1;
        private CollationElementIterator cei_;
        private boolean isShifted_;
        private PCEBuffer pceBuffer_;
        private int strength_;
        private boolean toShift_;
        private int variableTop_;

        private static final class PCEBuffer {
            private int bufferIndex_;
            private PCEI[] buffer_;

            /* synthetic */ PCEBuffer(PCEBuffer pCEBuffer) {
                this();
            }

            private PCEBuffer() {
                this.buffer_ = new PCEI[CollationPCE.DEFAULT_BUFFER_SIZE];
                this.bufferIndex_ = StringSearch.CE_NO_MATCH;
            }

            void reset() {
                this.bufferIndex_ = StringSearch.CE_NO_MATCH;
            }

            boolean empty() {
                return this.bufferIndex_ <= 0;
            }

            void put(long ce, int ixLow, int ixHigh) {
                if (this.bufferIndex_ >= this.buffer_.length) {
                    PCEI[] newBuffer = new PCEI[(this.buffer_.length + CollationPCE.BUFFER_GROW)];
                    System.arraycopy(this.buffer_, StringSearch.CE_NO_MATCH, newBuffer, StringSearch.CE_NO_MATCH, this.buffer_.length);
                    this.buffer_ = newBuffer;
                }
                this.buffer_[this.bufferIndex_] = new PCEI();
                this.buffer_[this.bufferIndex_].ce_ = ce;
                this.buffer_[this.bufferIndex_].low_ = ixLow;
                this.buffer_[this.bufferIndex_].high_ = ixHigh;
                this.bufferIndex_ += StringSearch.CE_SKIP_TARG;
            }

            PCEI get() {
                if (this.bufferIndex_ <= 0) {
                    return null;
                }
                PCEI[] pceiArr = this.buffer_;
                int i = this.bufferIndex_ + StringSearch.CE_MATCH;
                this.bufferIndex_ = i;
                return pceiArr[i];
            }
        }

        private static final class PCEI {
            long ce_;
            int high_;
            int low_;

            /* synthetic */ PCEI(PCEI pcei) {
                this();
            }

            private PCEI() {
            }
        }

        private static final class RCEBuffer {
            private int bufferIndex_;
            private RCEI[] buffer_;

            /* synthetic */ RCEBuffer(RCEBuffer rCEBuffer) {
                this();
            }

            private RCEBuffer() {
                this.buffer_ = new RCEI[CollationPCE.DEFAULT_BUFFER_SIZE];
                this.bufferIndex_ = StringSearch.CE_NO_MATCH;
            }

            boolean empty() {
                return this.bufferIndex_ <= 0;
            }

            void put(int ce, int ixLow, int ixHigh) {
                if (this.bufferIndex_ >= this.buffer_.length) {
                    RCEI[] newBuffer = new RCEI[(this.buffer_.length + CollationPCE.BUFFER_GROW)];
                    System.arraycopy(this.buffer_, StringSearch.CE_NO_MATCH, newBuffer, StringSearch.CE_NO_MATCH, this.buffer_.length);
                    this.buffer_ = newBuffer;
                }
                this.buffer_[this.bufferIndex_] = new RCEI();
                this.buffer_[this.bufferIndex_].ce_ = ce;
                this.buffer_[this.bufferIndex_].low_ = ixLow;
                this.buffer_[this.bufferIndex_].high_ = ixHigh;
                this.bufferIndex_ += StringSearch.CE_SKIP_TARG;
            }

            RCEI get() {
                if (this.bufferIndex_ <= 0) {
                    return null;
                }
                RCEI[] rceiArr = this.buffer_;
                int i = this.bufferIndex_ + StringSearch.CE_MATCH;
                this.bufferIndex_ = i;
                return rceiArr[i];
            }
        }

        private static final class RCEI {
            int ce_;
            int high_;
            int low_;

            /* synthetic */ RCEI(RCEI rcei) {
                this();
            }

            private RCEI() {
            }
        }

        public static final class Range {
            int ixHigh_;
            int ixLow_;

            public Range() {
            }
        }

        public CollationPCE(CollationElementIterator iter) {
            this.pceBuffer_ = new PCEBuffer();
            init(iter);
        }

        public void init(CollationElementIterator iter) {
            this.cei_ = iter;
            init(iter.getRuleBasedCollator());
        }

        private void init(RuleBasedCollator coll) {
            this.strength_ = coll.getStrength();
            this.toShift_ = coll.isAlternateHandlingShifted();
            this.isShifted_ = false;
            this.variableTop_ = coll.getVariableTop();
        }

        private long processCE(int ce) {
            long secondary = 0;
            long tertiary = 0;
            long quaternary = 0;
            switch (this.strength_) {
                case StringSearch.CE_NO_MATCH /*0*/:
                    break;
                case StringSearch.CE_SKIP_TARG /*1*/:
                    break;
                default:
                    tertiary = (long) CollationElementIterator.tertiaryOrder(ce);
                    break;
            }
            secondary = (long) CollationElementIterator.secondaryOrder(ce);
            long primary = (long) CollationElementIterator.primaryOrder(ce);
            if ((!this.toShift_ || this.variableTop_ <= ce || primary == 0) && !(this.isShifted_ && primary == 0)) {
                if (this.strength_ >= 3) {
                    quaternary = 65535;
                }
                this.isShifted_ = false;
            } else if (primary == 0) {
                return 0;
            } else {
                if (this.strength_ >= 3) {
                    quaternary = primary;
                }
                tertiary = 0;
                secondary = 0;
                primary = 0;
                this.isShifted_ = true;
            }
            return (((primary << 48) | (secondary << 32)) | (tertiary << DEFAULT_BUFFER_SIZE)) | quaternary;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long nextProcessed(Range range) {
            long result;
            this.pceBuffer_.reset();
            while (true) {
                int low = this.cei_.getOffset();
                int ce = this.cei_.next();
                int high = this.cei_.getOffset();
                if (ce != StringSearch.CE_MATCH) {
                    result = processCE(ce);
                    if (result != 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (range != null) {
                range.ixLow_ = low;
                range.ixHigh_ = high;
            }
            return result;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long previousProcessed(Range range) {
            while (this.pceBuffer_.empty()) {
                RCEBuffer rceb = new RCEBuffer();
                boolean finish = false;
                while (true) {
                    int high = this.cei_.getOffset();
                    int ce = this.cei_.previous();
                    int low = this.cei_.getOffset();
                    if (ce != StringSearch.CE_MATCH) {
                        rceb.put(ce, low, high);
                        if ((PRIMARYORDERMASK & ce) != 0) {
                            if (!isContinuation(ce)) {
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                if (finish) {
                    break;
                }
                while (!rceb.empty()) {
                    RCEI rcei = rceb.get();
                    long result = processCE(rcei.ce_);
                    if (result != 0) {
                        this.pceBuffer_.put(result, rcei.low_, rcei.high_);
                    }
                }
            }
            if (this.pceBuffer_.empty()) {
                if (range != null) {
                    range.ixLow_ = StringSearch.CE_MATCH;
                    range.ixHigh_ = StringSearch.CE_MATCH;
                }
                return PROCESSED_NULLORDER;
            }
            PCEI pcei = this.pceBuffer_.get();
            if (range != null) {
                range.ixLow_ = pcei.low_;
                range.ixHigh_ = pcei.high_;
            }
            return pcei.ce_;
        }

        private static boolean isContinuation(int ce) {
            return (ce & CONTINUATION_MARKER) == CONTINUATION_MARKER;
        }
    }

    private static class Match {
        int limit_;
        int start_;

        /* synthetic */ Match(Match match) {
            this();
        }

        private Match() {
            this.start_ = StringSearch.CE_MATCH;
            this.limit_ = StringSearch.CE_MATCH;
        }
    }

    private static final class Pattern {
        int CELength_;
        int[] CE_;
        int PCELength_;
        long[] PCE_;
        String text_;

        protected Pattern(String pattern) {
            this.PCELength_ = StringSearch.CE_NO_MATCH;
            this.CELength_ = StringSearch.CE_NO_MATCH;
            this.text_ = pattern;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.StringSearch.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.StringSearch.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringSearch.<clinit>():void");
    }

    public StringSearch(String pattern, CharacterIterator target, RuleBasedCollator collator, BreakIterator breakiter) {
        super(target, breakiter);
        if (collator.getNumericCollation()) {
            throw new UnsupportedOperationException("Numeric collation is not supported by StringSearch");
        }
        this.collator_ = collator;
        this.strength_ = collator.getStrength();
        this.ceMask_ = getMask(this.strength_);
        this.toShift_ = collator.isAlternateHandlingShifted();
        this.variableTop_ = collator.getVariableTop();
        this.nfd_ = Normalizer2.getNFDInstance();
        this.pattern_ = new Pattern(pattern);
        this.search_.setMatchedLength(CE_NO_MATCH);
        this.search_.matchedIndex_ = CE_MATCH;
        this.utilIter_ = null;
        this.textIter_ = new CollationElementIterator(target, collator);
        this.textProcessedIter_ = null;
        ULocale collLocale = collator.getLocale(ULocale.VALID_LOCALE);
        Search search = this.search_;
        if (collLocale == null) {
            collLocale = ULocale.ROOT;
        }
        search.internalBreakIter_ = BreakIterator.getCharacterInstance(collLocale);
        this.search_.internalBreakIter_.setText((CharacterIterator) target.clone());
        initialize();
    }

    public StringSearch(String pattern, CharacterIterator target, RuleBasedCollator collator) {
        this(pattern, target, collator, null);
    }

    public StringSearch(String pattern, CharacterIterator target, Locale locale) {
        this(pattern, target, ULocale.forLocale(locale));
    }

    public StringSearch(String pattern, CharacterIterator target, ULocale locale) {
        this(pattern, target, (RuleBasedCollator) Collator.getInstance(locale), null);
    }

    public StringSearch(String pattern, String target) {
        this(pattern, new StringCharacterIterator(target), (RuleBasedCollator) Collator.getInstance(), null);
    }

    public RuleBasedCollator getCollator() {
        return this.collator_;
    }

    public void setCollator(RuleBasedCollator collator) {
        if (collator == null) {
            throw new IllegalArgumentException("Collator can not be null");
        }
        this.collator_ = collator;
        this.ceMask_ = getMask(this.collator_.getStrength());
        ULocale collLocale = collator.getLocale(ULocale.VALID_LOCALE);
        Search search = this.search_;
        if (collLocale == null) {
            collLocale = ULocale.ROOT;
        }
        search.internalBreakIter_ = BreakIterator.getCharacterInstance(collLocale);
        this.search_.internalBreakIter_.setText((CharacterIterator) this.search_.text().clone());
        this.toShift_ = collator.isAlternateHandlingShifted();
        this.variableTop_ = collator.getVariableTop();
        this.textIter_ = new CollationElementIterator(this.pattern_.text_, collator);
        this.utilIter_ = new CollationElementIterator(this.pattern_.text_, collator);
        initialize();
    }

    public String getPattern() {
        return this.pattern_.text_;
    }

    public void setPattern(String pattern) {
        if (pattern == null || pattern.length() <= 0) {
            throw new IllegalArgumentException("Pattern to search for can not be null or of length 0");
        }
        this.pattern_.text_ = pattern;
        initialize();
    }

    public boolean isCanonical() {
        return this.search_.isCanonicalMatch_;
    }

    public void setCanonical(boolean allowCanonical) {
        this.search_.isCanonicalMatch_ = allowCanonical;
    }

    public void setTarget(CharacterIterator text) {
        super.setTarget(text);
        this.textIter_.setText(text);
    }

    public int getIndex() {
        int result = this.textIter_.getOffset();
        if (isOutOfBounds(this.search_.beginIndex(), this.search_.endIndex(), result)) {
            return CE_MATCH;
        }
        return result;
    }

    public void setIndex(int position) {
        super.setIndex(position);
        this.textIter_.setOffset(position);
    }

    public void reset() {
        int ceMask;
        boolean shift;
        int varTop;
        boolean sameCollAttribute = true;
        int newStrength = this.collator_.getStrength();
        if (this.strength_ >= 3 || newStrength < 3) {
            if (this.strength_ >= 3 && newStrength < 3) {
            }
            this.strength_ = this.collator_.getStrength();
            ceMask = getMask(this.strength_);
            if (this.ceMask_ != ceMask) {
                this.ceMask_ = ceMask;
                sameCollAttribute = false;
            }
            shift = this.collator_.isAlternateHandlingShifted();
            if (this.toShift_ != shift) {
                this.toShift_ = shift;
                sameCollAttribute = false;
            }
            varTop = this.collator_.getVariableTop();
            if (this.variableTop_ != varTop) {
                this.variableTop_ = varTop;
                sameCollAttribute = false;
            }
            if (!sameCollAttribute) {
                initialize();
            }
            this.textIter_.setText(this.search_.text());
            this.search_.setMatchedLength(CE_NO_MATCH);
            this.search_.matchedIndex_ = CE_MATCH;
            this.search_.isOverlap_ = false;
            this.search_.isCanonicalMatch_ = false;
            this.search_.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
            this.search_.isForwardSearching_ = true;
            this.search_.reset_ = true;
        }
        sameCollAttribute = false;
        this.strength_ = this.collator_.getStrength();
        ceMask = getMask(this.strength_);
        if (this.ceMask_ != ceMask) {
            this.ceMask_ = ceMask;
            sameCollAttribute = false;
        }
        shift = this.collator_.isAlternateHandlingShifted();
        if (this.toShift_ != shift) {
            this.toShift_ = shift;
            sameCollAttribute = false;
        }
        varTop = this.collator_.getVariableTop();
        if (this.variableTop_ != varTop) {
            this.variableTop_ = varTop;
            sameCollAttribute = false;
        }
        if (sameCollAttribute) {
            initialize();
        }
        this.textIter_.setText(this.search_.text());
        this.search_.setMatchedLength(CE_NO_MATCH);
        this.search_.matchedIndex_ = CE_MATCH;
        this.search_.isOverlap_ = false;
        this.search_.isCanonicalMatch_ = false;
        this.search_.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        this.search_.isForwardSearching_ = true;
        this.search_.reset_ = true;
    }

    protected int handleNext(int position) {
        if (this.pattern_.CELength_ == 0) {
            this.search_.matchedIndex_ = this.search_.matchedIndex_ == CE_MATCH ? getIndex() : this.search_.matchedIndex_ + CE_SKIP_TARG;
            this.search_.setMatchedLength(CE_NO_MATCH);
            this.textIter_.setOffset(this.search_.matchedIndex_);
            if (this.search_.matchedIndex_ == this.search_.endIndex()) {
                this.search_.matchedIndex_ = CE_MATCH;
            }
            return CE_MATCH;
        }
        if (this.search_.matchedLength() <= 0) {
            this.search_.matchedIndex_ = position + CE_MATCH;
        }
        this.textIter_.setOffset(position);
        if (this.search_.isCanonicalMatch_) {
            handleNextCanonical();
        } else {
            handleNextExact();
        }
        if (this.search_.matchedIndex_ == CE_MATCH) {
            this.textIter_.setOffset(this.search_.endIndex());
        } else {
            this.textIter_.setOffset(this.search_.matchedIndex_);
        }
        return this.search_.matchedIndex_;
    }

    protected int handlePrevious(int position) {
        if (this.pattern_.CELength_ == 0) {
            this.search_.matchedIndex_ = this.search_.matchedIndex_ == CE_MATCH ? getIndex() : this.search_.matchedIndex_;
            if (this.search_.matchedIndex_ == this.search_.beginIndex()) {
                setMatchNotFound();
            } else {
                Search search = this.search_;
                search.matchedIndex_ += CE_MATCH;
                this.textIter_.setOffset(this.search_.matchedIndex_);
                this.search_.setMatchedLength(CE_NO_MATCH);
            }
        } else {
            this.textIter_.setOffset(position);
            if (this.search_.isCanonicalMatch_) {
                handlePreviousCanonical();
            } else {
                handlePreviousExact();
            }
        }
        return this.search_.matchedIndex_;
    }

    private static int getMask(int strength) {
        switch (strength) {
            case CE_NO_MATCH /*0*/:
                return PRIMARYORDERMASK;
            case CE_SKIP_TARG /*1*/:
                return -256;
            default:
                return CE_MATCH;
        }
    }

    private int getCE(int sourcece) {
        sourcece &= this.ceMask_;
        if (this.toShift_) {
            if (this.variableTop_ <= sourcece) {
                return sourcece;
            }
            if (this.strength_ >= 3) {
                return sourcece & PRIMARYORDERMASK;
            }
            return CE_NO_MATCH;
        } else if (this.strength_ < 3 || sourcece != 0) {
            return sourcece;
        } else {
            return DexFormat.MAX_TYPE_IDX;
        }
    }

    private static int[] addToIntArray(int[] destination, int offset, int destinationlength, int value, int increments) {
        int newlength = destinationlength;
        if (offset + CE_SKIP_TARG == destinationlength) {
            int[] temp = new int[(destinationlength + increments)];
            System.arraycopy(destination, CE_NO_MATCH, temp, CE_NO_MATCH, offset);
            destination = temp;
        }
        destination[offset] = value;
        return destination;
    }

    private static long[] addToLongArray(long[] destination, int offset, int destinationlength, long value, int increments) {
        int newlength = destinationlength;
        if (offset + CE_SKIP_TARG == destinationlength) {
            long[] temp = new long[(destinationlength + increments)];
            System.arraycopy(destination, CE_NO_MATCH, temp, CE_NO_MATCH, offset);
            destination = temp;
        }
        destination[offset] = value;
        return destination;
    }

    private int initializePatternCETable() {
        int[] cetable = new int[INITIAL_ARRAY_SIZE_];
        int cetablesize = cetable.length;
        int patternlength = this.pattern_.text_.length();
        CollationElementIterator coleiter = this.utilIter_;
        if (coleiter == null) {
            coleiter = new CollationElementIterator(this.pattern_.text_, this.collator_);
            this.utilIter_ = coleiter;
        } else {
            coleiter.setText(this.pattern_.text_);
        }
        int offset = CE_NO_MATCH;
        int result = CE_NO_MATCH;
        while (true) {
            int ce = coleiter.next();
            if (ce != CE_MATCH) {
                int newce = getCE(ce);
                if (newce != 0) {
                    int[] temp = addToIntArray(cetable, offset, cetablesize, newce, (patternlength - coleiter.getOffset()) + CE_SKIP_TARG);
                    offset += CE_SKIP_TARG;
                    cetable = temp;
                }
                result += coleiter.getMaxExpansion(ce) + CE_MATCH;
            } else {
                cetable[offset] = CE_NO_MATCH;
                this.pattern_.CE_ = cetable;
                this.pattern_.CELength_ = offset;
                return result;
            }
        }
    }

    private int initializePatternPCETable() {
        long[] pcetable = new long[INITIAL_ARRAY_SIZE_];
        int pcetablesize = pcetable.length;
        int patternlength = this.pattern_.text_.length();
        CollationElementIterator coleiter = this.utilIter_;
        if (coleiter == null) {
            coleiter = new CollationElementIterator(this.pattern_.text_, this.collator_);
            this.utilIter_ = coleiter;
        } else {
            coleiter.setText(this.pattern_.text_);
        }
        int offset = CE_NO_MATCH;
        CollationPCE iter = new CollationPCE(coleiter);
        while (true) {
            long pce = iter.nextProcessed(null);
            if (pce != -1) {
                long[] temp = addToLongArray(pcetable, offset, pcetablesize, pce, (patternlength - coleiter.getOffset()) + CE_SKIP_TARG);
                offset += CE_SKIP_TARG;
                pcetable = temp;
            } else {
                pcetable[offset] = 0;
                this.pattern_.PCE_ = pcetable;
                this.pattern_.PCELength_ = offset;
                return CE_NO_MATCH;
            }
        }
    }

    private int initializePattern() {
        this.pattern_.PCE_ = null;
        return initializePatternCETable();
    }

    private void initialize() {
        initializePattern();
    }

    @Deprecated
    protected void setMatchNotFound() {
        super.setMatchNotFound();
        if (this.search_.isForwardSearching_) {
            this.textIter_.setOffset(this.search_.text().getEndIndex());
        } else {
            this.textIter_.setOffset(CE_NO_MATCH);
        }
    }

    private static final boolean isOutOfBounds(int textstart, int textlimit, int offset) {
        return offset < textstart || offset > textlimit;
    }

    private boolean checkIdentical(int start, int end) {
        if (this.strength_ != 15) {
            return true;
        }
        String textstr = getString(this.targetText, start, end - start);
        if (Normalizer.quickCheck(textstr, Normalizer.NFD, (int) CE_NO_MATCH) == Normalizer.NO) {
            textstr = Normalizer.decompose(textstr, false);
        }
        String patternstr = this.pattern_.text_;
        if (Normalizer.quickCheck(patternstr, Normalizer.NFD, (int) CE_NO_MATCH) == Normalizer.NO) {
            patternstr = Normalizer.decompose(patternstr, false);
        }
        return textstr.equals(patternstr);
    }

    private boolean initTextProcessedIter() {
        if (this.textProcessedIter_ == null) {
            this.textProcessedIter_ = new CollationPCE(this.textIter_);
        } else {
            this.textProcessedIter_.init(this.textIter_);
        }
        return true;
    }

    private int nextBoundaryAfter(int startIndex) {
        BreakIterator breakiterator = this.search_.breakIter();
        if (breakiterator == null) {
            breakiterator = this.search_.internalBreakIter_;
        }
        if (breakiterator != null) {
            return breakiterator.following(startIndex);
        }
        return startIndex;
    }

    private boolean isBreakBoundary(int index) {
        BreakIterator breakiterator = this.search_.breakIter();
        if (breakiterator == null) {
            breakiterator = this.search_.internalBreakIter_;
        }
        return breakiterator != null ? breakiterator.isBoundary(index) : false;
    }

    private static int compareCE64s(long targCE, long patCE, ElementComparisonType compareType) {
        if (targCE == patCE) {
            return CE_MATCH;
        }
        if (compareType == ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
            return CE_NO_MATCH;
        }
        long targCEshifted = targCE >>> 32;
        long patCEshifted = patCE >>> 32;
        int targLev1 = (int) (Collation.MAX_PRIMARY & targCEshifted);
        int patLev1 = (int) (Collation.MAX_PRIMARY & patCEshifted);
        if (targLev1 == patLev1) {
            int targLev2 = (int) (65535 & targCEshifted);
            int patLev2 = (int) (65535 & patCEshifted);
            int i;
            if (targLev2 == patLev2) {
                int targLev3 = (int) (Collation.MAX_PRIMARY & targCE);
                int patLev3 = (int) (Collation.MAX_PRIMARY & patCE);
                if (targLev3 == patLev3) {
                    return CE_MATCH;
                }
                i = (patLev3 == CE_LEVEL3_BASE || (compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD && targLev3 == CE_LEVEL3_BASE)) ? CE_MATCH : CE_NO_MATCH;
                return i;
            } else if (targLev2 == 0) {
                return CE_SKIP_TARG;
            } else {
                if (patLev2 == 0 && compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                    return CE_SKIP_PATN;
                }
                i = (patLev2 == CE_LEVEL2_BASE || (compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD && targLev2 == CE_LEVEL2_BASE)) ? CE_MATCH : CE_NO_MATCH;
                return i;
            }
        } else if (targLev1 == 0) {
            return CE_SKIP_TARG;
        } else {
            if (patLev1 == 0 && compareType == ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                return CE_SKIP_PATN;
            }
            return CE_NO_MATCH;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean search(int startIdx, Match m) {
        if (this.pattern_.CELength_ != 0) {
            if (startIdx >= this.search_.beginIndex()) {
                if (startIdx <= this.search_.endIndex()) {
                    boolean found;
                    if (this.pattern_.PCE_ == null) {
                        initializePatternPCETable();
                    }
                    this.textIter_.setOffset(startIdx);
                    CEBuffer ceb = new CEBuffer(this);
                    CEI targetCEI = null;
                    int mStart = CE_MATCH;
                    int mLimit = CE_MATCH;
                    int targetIx = CE_NO_MATCH;
                    while (true) {
                        found = true;
                        int targetIxOffset = CE_NO_MATCH;
                        long patCE = 0;
                        CEI firstCEI = ceb.get(targetIx);
                        if (firstCEI != null) {
                            int ceMatch;
                            int patIx = CE_NO_MATCH;
                            while (patIx < this.pattern_.PCELength_) {
                                patCE = this.pattern_.PCE_[patIx];
                                targetCEI = ceb.get((targetIx + patIx) + targetIxOffset);
                                ceMatch = compareCE64s(targetCEI.ce_, patCE, this.search_.elementComparisonType_);
                                if (ceMatch == 0) {
                                    found = false;
                                    break;
                                }
                                if (ceMatch > 0) {
                                    if (ceMatch == CE_SKIP_TARG) {
                                        patIx += CE_MATCH;
                                        targetIxOffset += CE_SKIP_TARG;
                                    } else {
                                        targetIxOffset += CE_MATCH;
                                    }
                                }
                                patIx += CE_SKIP_TARG;
                            }
                            targetIxOffset += this.pattern_.PCELength_;
                            if (!found) {
                                if (targetCEI != null) {
                                    if (targetCEI.ce_ != -1) {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                                targetIx += CE_SKIP_TARG;
                            }
                            if (!found) {
                                break;
                            }
                            CEI nextCEI;
                            int maxLimit;
                            int i;
                            boolean allowMidclusterMatch;
                            int nba;
                            CEI lastCEI = ceb.get((targetIx + targetIxOffset) + CE_MATCH);
                            mStart = firstCEI.lowIndex_;
                            int minLimit = lastCEI.lowIndex_;
                            if (this.search_.elementComparisonType_ != ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                                while (true) {
                                    nextCEI = ceb.get(targetIx + targetIxOffset);
                                    maxLimit = nextCEI.lowIndex_;
                                    if (nextCEI.ce_ == -1) {
                                        break;
                                    }
                                    if (((nextCEI.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0) {
                                        break;
                                    }
                                    ceMatch = compareCE64s(nextCEI.ce_, patCE, this.search_.elementComparisonType_);
                                    if (ceMatch == 0 || ceMatch == CE_SKIP_PATN) {
                                        found = false;
                                    } else {
                                        targetIxOffset += CE_SKIP_TARG;
                                    }
                                }
                            } else {
                                nextCEI = ceb.get(targetIx + targetIxOffset);
                                maxLimit = nextCEI.lowIndex_;
                                if (nextCEI.lowIndex_ == nextCEI.highIndex_) {
                                    if (nextCEI.ce_ != -1) {
                                        found = false;
                                    }
                                }
                            }
                            if (!isBreakBoundary(mStart)) {
                                found = false;
                            }
                            int secondIx = firstCEI.highIndex_;
                            if (mStart == r0) {
                                found = false;
                            }
                            if (this.breakIterator == null && nextCEI != null) {
                                if (((nextCEI.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0) {
                                    i = lastCEI.highIndex_;
                                    if (maxLimit >= r0) {
                                        i = nextCEI.highIndex_;
                                        if (r0 > maxLimit) {
                                            if (this.nfd_.hasBoundaryBefore(codePointAt(this.targetText, maxLimit))) {
                                                allowMidclusterMatch = true;
                                            } else {
                                                allowMidclusterMatch = this.nfd_.hasBoundaryAfter(codePointBefore(this.targetText, maxLimit));
                                            }
                                            mLimit = maxLimit;
                                            if (minLimit < maxLimit) {
                                                i = lastCEI.highIndex_;
                                                if (minLimit == r0 || !isBreakBoundary(minLimit)) {
                                                    nba = nextBoundaryAfter(minLimit);
                                                    if (nba >= lastCEI.highIndex_ && (!allowMidclusterMatch || nba < maxLimit)) {
                                                        mLimit = nba;
                                                    }
                                                } else {
                                                    mLimit = minLimit;
                                                }
                                            }
                                            if (!allowMidclusterMatch) {
                                                if (mLimit > maxLimit) {
                                                    found = false;
                                                }
                                                if (!isBreakBoundary(mLimit)) {
                                                    found = false;
                                                }
                                            }
                                            if (!checkIdentical(mStart, mLimit)) {
                                                found = false;
                                            }
                                            if (found) {
                                                break;
                                            }
                                            targetIx += CE_SKIP_TARG;
                                        }
                                    }
                                }
                            }
                            allowMidclusterMatch = false;
                            mLimit = maxLimit;
                            if (minLimit < maxLimit) {
                                i = lastCEI.highIndex_;
                                if (minLimit == r0) {
                                }
                                nba = nextBoundaryAfter(minLimit);
                                mLimit = nba;
                            }
                            if (allowMidclusterMatch) {
                                if (mLimit > maxLimit) {
                                    found = false;
                                }
                                if (isBreakBoundary(mLimit)) {
                                    found = false;
                                }
                            }
                            if (checkIdentical(mStart, mLimit)) {
                                found = false;
                            }
                            if (found) {
                                break;
                            }
                            targetIx += CE_SKIP_TARG;
                        } else {
                            break;
                        }
                    }
                    if (!found) {
                        mLimit = CE_MATCH;
                        mStart = CE_MATCH;
                    }
                    if (m != null) {
                        m.start_ = mStart;
                        m.limit_ = mLimit;
                    }
                    return found;
                }
            }
        }
        throw new IllegalArgumentException("search(" + startIdx + ", m) - expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
    }

    private static int codePointAt(CharacterIterator iter, int index) {
        int currentIterIndex = iter.getIndex();
        char codeUnit = iter.setIndex(index);
        int cp = codeUnit;
        if (Character.isHighSurrogate(codeUnit)) {
            char nextUnit = iter.next();
            if (Character.isLowSurrogate(nextUnit)) {
                cp = Character.toCodePoint(codeUnit, nextUnit);
            }
        }
        iter.setIndex(currentIterIndex);
        return cp;
    }

    private static int codePointBefore(CharacterIterator iter, int index) {
        int currentIterIndex = iter.getIndex();
        iter.setIndex(index);
        char codeUnit = iter.previous();
        int cp = codeUnit;
        if (Character.isLowSurrogate(codeUnit)) {
            char prevUnit = iter.previous();
            if (Character.isHighSurrogate(prevUnit)) {
                cp = Character.toCodePoint(prevUnit, codeUnit);
            }
        }
        iter.setIndex(currentIterIndex);
        return cp;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean searchBackwards(int startIdx, Match m) {
        if (this.pattern_.CELength_ != 0) {
            if (startIdx >= this.search_.beginIndex()) {
                if (startIdx <= this.search_.endIndex()) {
                    int i;
                    if (this.pattern_.PCE_ == null) {
                        initializePatternPCETable();
                    }
                    CEBuffer ceb = new CEBuffer(this);
                    int targetIx = CE_NO_MATCH;
                    if (startIdx < this.search_.endIndex()) {
                        this.textIter_.setOffset(this.search_.internalBreakIter_.following(startIdx));
                        targetIx = CE_NO_MATCH;
                        while (true) {
                            i = ceb.getPrevious(targetIx).lowIndex_;
                            if (r0 < startIdx) {
                                break;
                            }
                            targetIx += CE_SKIP_TARG;
                        }
                    } else {
                        this.textIter_.setOffset(startIdx);
                    }
                    CEI targetCEI = null;
                    int limitIx = targetIx;
                    int mStart = CE_MATCH;
                    int mLimit = CE_MATCH;
                    while (true) {
                        boolean found = true;
                        CEI lastCEI = ceb.getPrevious(targetIx);
                        if (lastCEI == null) {
                            break;
                        }
                        int targetIxOffset = CE_NO_MATCH;
                        int patIx = this.pattern_.PCELength_ + CE_MATCH;
                        while (patIx >= 0) {
                            long patCE = this.pattern_.PCE_[patIx];
                            targetCEI = ceb.getPrevious((((this.pattern_.PCELength_ + targetIx) + CE_MATCH) - patIx) + targetIxOffset);
                            int ceMatch = compareCE64s(targetCEI.ce_, patCE, this.search_.elementComparisonType_);
                            if (ceMatch == 0) {
                                found = false;
                                break;
                            }
                            if (ceMatch > 0) {
                                if (ceMatch == CE_SKIP_TARG) {
                                    patIx += CE_SKIP_TARG;
                                    targetIxOffset += CE_SKIP_TARG;
                                } else {
                                    targetIxOffset += CE_MATCH;
                                }
                            }
                            patIx += CE_MATCH;
                        }
                        if (!found) {
                            if (targetCEI != null) {
                                if (targetCEI.ce_ != -1) {
                                    continue;
                                }
                            } else {
                                continue;
                            }
                            targetIx += CE_SKIP_TARG;
                        }
                        if (!found) {
                            break;
                        }
                        CEI firstCEI = ceb.getPrevious(((this.pattern_.PCELength_ + targetIx) + CE_MATCH) + targetIxOffset);
                        mStart = firstCEI.lowIndex_;
                        if (!isBreakBoundary(mStart)) {
                            found = false;
                        }
                        i = firstCEI.highIndex_;
                        if (mStart == r0) {
                            found = false;
                        }
                        int minLimit = lastCEI.lowIndex_;
                        int maxLimit;
                        int nba;
                        if (targetIx > 0) {
                            boolean allowMidclusterMatch;
                            CEI nextCEI = ceb.getPrevious(targetIx + CE_MATCH);
                            if (nextCEI.lowIndex_ == nextCEI.highIndex_) {
                                if (nextCEI.ce_ != -1) {
                                    found = false;
                                }
                            }
                            maxLimit = nextCEI.lowIndex_;
                            mLimit = maxLimit;
                            if (this.breakIterator == null && nextCEI != null) {
                                if (((nextCEI.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0 && maxLimit >= lastCEI.highIndex_) {
                                    i = nextCEI.highIndex_;
                                    if (r0 > maxLimit) {
                                        if (this.nfd_.hasBoundaryBefore(codePointAt(this.targetText, maxLimit))) {
                                            allowMidclusterMatch = true;
                                        } else {
                                            allowMidclusterMatch = this.nfd_.hasBoundaryAfter(codePointBefore(this.targetText, maxLimit));
                                        }
                                        if (minLimit < maxLimit) {
                                            nba = nextBoundaryAfter(minLimit);
                                            if (nba >= lastCEI.highIndex_ && (!allowMidclusterMatch || nba < maxLimit)) {
                                                mLimit = nba;
                                            }
                                        }
                                        if (!allowMidclusterMatch) {
                                            if (mLimit > maxLimit) {
                                                found = false;
                                            }
                                            if (!isBreakBoundary(mLimit)) {
                                                found = false;
                                            }
                                        }
                                    }
                                }
                            }
                            allowMidclusterMatch = false;
                            if (minLimit < maxLimit) {
                                nba = nextBoundaryAfter(minLimit);
                                mLimit = nba;
                            }
                            if (allowMidclusterMatch) {
                                if (mLimit > maxLimit) {
                                    found = false;
                                }
                                if (isBreakBoundary(mLimit)) {
                                    found = false;
                                }
                            }
                        } else {
                            nba = nextBoundaryAfter(minLimit);
                            maxLimit = (nba <= 0 || startIdx <= nba) ? startIdx : nba;
                            mLimit = maxLimit;
                        }
                        if (!checkIdentical(mStart, mLimit)) {
                            found = false;
                        }
                        if (found) {
                            break;
                        }
                        targetIx += CE_SKIP_TARG;
                    }
                    throw new ICUException("CEBuffer.getPrevious(" + targetIx + ") returned null.");
                }
            }
        }
        throw new IllegalArgumentException("searchBackwards(" + startIdx + ", m) - expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
    }

    private boolean handleNextExact() {
        return handleNextCommonImpl();
    }

    private boolean handleNextCanonical() {
        return handleNextCommonImpl();
    }

    private boolean handleNextCommonImpl() {
        int textOffset = this.textIter_.getOffset();
        Match match = new Match();
        if (search(textOffset, match)) {
            this.search_.matchedIndex_ = match.start_;
            this.search_.setMatchedLength(match.limit_ - match.start_);
            return true;
        }
        setMatchNotFound();
        return false;
    }

    private boolean handlePreviousExact() {
        return handlePreviousCommonImpl();
    }

    private boolean handlePreviousCanonical() {
        return handlePreviousCommonImpl();
    }

    private boolean handlePreviousCommonImpl() {
        int textOffset;
        if (!this.search_.isOverlap_) {
            textOffset = this.textIter_.getOffset();
        } else if (this.search_.matchedIndex_ != CE_MATCH) {
            textOffset = (this.search_.matchedIndex_ + this.search_.matchedLength()) + CE_MATCH;
        } else {
            initializePatternPCETable();
            if (initTextProcessedIter()) {
                for (int nPCEs = CE_NO_MATCH; nPCEs < this.pattern_.PCELength_ + CE_MATCH && this.textProcessedIter_.nextProcessed(null) != -1; nPCEs += CE_SKIP_TARG) {
                }
                textOffset = this.textIter_.getOffset();
            } else {
                setMatchNotFound();
                return false;
            }
        }
        Match match = new Match();
        if (searchBackwards(textOffset, match)) {
            this.search_.matchedIndex_ = match.start_;
            this.search_.setMatchedLength(match.limit_ - match.start_);
            return true;
        }
        setMatchNotFound();
        return false;
    }

    private static final String getString(CharacterIterator text, int start, int length) {
        StringBuilder result = new StringBuilder(length);
        int offset = text.getIndex();
        text.setIndex(start);
        for (int i = CE_NO_MATCH; i < length; i += CE_SKIP_TARG) {
            result.append(text.current());
            text.next();
        }
        text.setIndex(offset);
        return result.toString();
    }
}

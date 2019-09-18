package android.icu.text;

import android.icu.impl.coll.Collation;
import android.icu.text.SearchIterator;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

public final class StringSearch extends SearchIterator {
    private static int CE_LEVEL2_BASE = 5;
    private static int CE_LEVEL3_BASE = 327680;
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
    /* access modifiers changed from: private */
    public Pattern pattern_;
    private int strength_;
    private CollationElementIterator textIter_;
    /* access modifiers changed from: private */
    public CollationPCE textProcessedIter_;
    private boolean toShift_;
    private CollationElementIterator utilIter_;
    int variableTop_;

    private static class CEBuffer {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        static final int CEBUFFER_EXTRA = 32;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L = 8;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_OTHER = 3;
        int bufSize_;
        CEI[] buf_;
        int firstIx_;
        int limitIx_;
        StringSearch strSearch_;

        static {
            Class<StringSearch> cls = StringSearch.class;
        }

        CEBuffer(StringSearch ss) {
            this.strSearch_ = ss;
            this.bufSize_ = ss.pattern_.PCELength_ + 32;
            if (ss.search_.elementComparisonType_ != SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                String patText = ss.pattern_.text_;
                if (patText != null) {
                    for (int i = 0; i < patText.length(); i++) {
                        if (MIGHT_BE_JAMO_L(patText.charAt(i))) {
                            this.bufSize_ += 8;
                        } else {
                            this.bufSize_ += 3;
                        }
                    }
                }
            }
            this.firstIx_ = 0;
            this.limitIx_ = 0;
            if (ss.initTextProcessedIter()) {
                this.buf_ = new CEI[this.bufSize_];
            }
        }

        /* access modifiers changed from: package-private */
        public CEI get(int index) {
            int i = index % this.bufSize_;
            if (index >= this.firstIx_ && index < this.limitIx_) {
                return this.buf_[i];
            }
            if (index != this.limitIx_) {
                return null;
            }
            this.limitIx_++;
            if (this.limitIx_ - this.firstIx_ >= this.bufSize_) {
                this.firstIx_++;
            }
            CollationPCE.Range range = new CollationPCE.Range();
            if (this.buf_[i] == null) {
                this.buf_[i] = new CEI();
            }
            this.buf_[i].ce_ = this.strSearch_.textProcessedIter_.nextProcessed(range);
            this.buf_[i].lowIndex_ = range.ixLow_;
            this.buf_[i].highIndex_ = range.ixHigh_;
            return this.buf_[i];
        }

        /* access modifiers changed from: package-private */
        public CEI getPrevious(int index) {
            int i = index % this.bufSize_;
            if (index >= this.firstIx_ && index < this.limitIx_) {
                return this.buf_[i];
            }
            if (index != this.limitIx_) {
                return null;
            }
            this.limitIx_++;
            if (this.limitIx_ - this.firstIx_ >= this.bufSize_) {
                this.firstIx_++;
            }
            CollationPCE.Range range = new CollationPCE.Range();
            if (this.buf_[i] == null) {
                this.buf_[i] = new CEI();
            }
            this.buf_[i].ce_ = this.strSearch_.textProcessedIter_.previousProcessed(range);
            this.buf_[i].lowIndex_ = range.ixLow_;
            this.buf_[i].highIndex_ = range.ixHigh_;
            return this.buf_[i];
        }

        static boolean MIGHT_BE_JAMO_L(char c) {
            return (c >= 4352 && c <= 4446) || (c >= 12593 && c <= 12622) || (c >= 12645 && c <= 12678);
        }
    }

    private static class CEI {
        long ce_;
        int highIndex_;
        int lowIndex_;

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
        private PCEBuffer pceBuffer_ = new PCEBuffer();
        private int strength_;
        private boolean toShift_;
        private int variableTop_;

        private static final class PCEBuffer {
            private int bufferIndex_;
            private PCEI[] buffer_;

            private PCEBuffer() {
                this.buffer_ = new PCEI[16];
                this.bufferIndex_ = 0;
            }

            /* access modifiers changed from: package-private */
            public void reset() {
                this.bufferIndex_ = 0;
            }

            /* access modifiers changed from: package-private */
            public boolean empty() {
                return this.bufferIndex_ <= 0;
            }

            /* access modifiers changed from: package-private */
            public void put(long ce, int ixLow, int ixHigh) {
                if (this.bufferIndex_ >= this.buffer_.length) {
                    PCEI[] newBuffer = new PCEI[(this.buffer_.length + 8)];
                    System.arraycopy(this.buffer_, 0, newBuffer, 0, this.buffer_.length);
                    this.buffer_ = newBuffer;
                }
                this.buffer_[this.bufferIndex_] = new PCEI();
                this.buffer_[this.bufferIndex_].ce_ = ce;
                this.buffer_[this.bufferIndex_].low_ = ixLow;
                this.buffer_[this.bufferIndex_].high_ = ixHigh;
                this.bufferIndex_++;
            }

            /* access modifiers changed from: package-private */
            public PCEI get() {
                if (this.bufferIndex_ <= 0) {
                    return null;
                }
                PCEI[] pceiArr = this.buffer_;
                int i = this.bufferIndex_ - 1;
                this.bufferIndex_ = i;
                return pceiArr[i];
            }
        }

        private static final class PCEI {
            long ce_;
            int high_;
            int low_;

            private PCEI() {
            }
        }

        private static final class RCEBuffer {
            private int bufferIndex_;
            private RCEI[] buffer_;

            private RCEBuffer() {
                this.buffer_ = new RCEI[16];
                this.bufferIndex_ = 0;
            }

            /* access modifiers changed from: package-private */
            public boolean empty() {
                return this.bufferIndex_ <= 0;
            }

            /* access modifiers changed from: package-private */
            public void put(int ce, int ixLow, int ixHigh) {
                if (this.bufferIndex_ >= this.buffer_.length) {
                    RCEI[] newBuffer = new RCEI[(this.buffer_.length + 8)];
                    System.arraycopy(this.buffer_, 0, newBuffer, 0, this.buffer_.length);
                    this.buffer_ = newBuffer;
                }
                this.buffer_[this.bufferIndex_] = new RCEI();
                this.buffer_[this.bufferIndex_].ce_ = ce;
                this.buffer_[this.bufferIndex_].low_ = ixLow;
                this.buffer_[this.bufferIndex_].high_ = ixHigh;
                this.bufferIndex_++;
            }

            /* access modifiers changed from: package-private */
            public RCEI get() {
                if (this.bufferIndex_ <= 0) {
                    return null;
                }
                RCEI[] rceiArr = this.buffer_;
                int i = this.bufferIndex_ - 1;
                this.bufferIndex_ = i;
                return rceiArr[i];
            }
        }

        private static final class RCEI {
            int ce_;
            int high_;
            int low_;

            private RCEI() {
            }
        }

        public static final class Range {
            int ixHigh_;
            int ixLow_;
        }

        public CollationPCE(CollationElementIterator iter) {
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
                case 0:
                    break;
                case 1:
                    break;
                default:
                    tertiary = (long) CollationElementIterator.tertiaryOrder(ce);
                    break;
            }
            secondary = (long) CollationElementIterator.secondaryOrder(ce);
            long primary = (long) CollationElementIterator.primaryOrder(ce);
            if ((!this.toShift_ || this.variableTop_ <= ce || primary == 0) && (!this.isShifted_ || primary != 0)) {
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
            return (primary << 48) | (secondary << 32) | (tertiary << 16) | quaternary;
        }

        public long nextProcessed(Range range) {
            int low;
            int high;
            long result;
            this.pceBuffer_.reset();
            while (true) {
                low = this.cei_.getOffset();
                int ce = this.cei_.next();
                high = this.cei_.getOffset();
                if (ce != -1) {
                    result = processCE(ce);
                    if (result != 0) {
                        break;
                    }
                } else {
                    result = -1;
                    break;
                }
            }
            if (range != null) {
                range.ixLow_ = low;
                range.ixHigh_ = high;
            }
            return result;
        }

        public long previousProcessed(Range range) {
            int high;
            int low;
            int high2 = 0;
            int high3 = 0;
            while (true) {
                if (!this.pceBuffer_.empty()) {
                    break;
                }
                RCEBuffer rceb = new RCEBuffer();
                int i = high2;
                int low2 = high3;
                boolean finish = false;
                while (true) {
                    high = this.cei_.getOffset();
                    int ce = this.cei_.previous();
                    low = this.cei_.getOffset();
                    if (ce != -1) {
                        rceb.put(ce, low, high);
                        if ((PRIMARYORDERMASK & ce) != 0 && !isContinuation(ce)) {
                            break;
                        }
                    } else if (rceb.empty()) {
                        finish = true;
                    }
                }
                if (finish) {
                    int i2 = high;
                    int high4 = low;
                    break;
                }
                while (!rceb.empty()) {
                    RCEI rcei = rceb.get();
                    long result = processCE(rcei.ce_);
                    if (result != 0) {
                        this.pceBuffer_.put(result, rcei.low_, rcei.high_);
                    }
                }
                high3 = high;
                high2 = low;
            }
            if (this.pceBuffer_.empty()) {
                if (range != null) {
                    range.ixLow_ = -1;
                    range.ixHigh_ = -1;
                }
                return -1;
            }
            PCEI pcei = this.pceBuffer_.get();
            if (range != null) {
                range.ixLow_ = pcei.low_;
                range.ixHigh_ = pcei.high_;
            }
            return pcei.ce_;
        }

        private static boolean isContinuation(int ce) {
            return (ce & 192) == 192;
        }
    }

    private static class Match {
        int limit_;
        int start_;

        private Match() {
            this.start_ = -1;
            this.limit_ = -1;
        }
    }

    private static final class Pattern {
        int CELength_ = 0;
        int[] CE_;
        int PCELength_ = 0;
        long[] PCE_;
        String text_;

        protected Pattern(String pattern) {
            this.text_ = pattern;
        }
    }

    public StringSearch(String pattern, CharacterIterator target, RuleBasedCollator collator, BreakIterator breakiter) {
        super(target, breakiter);
        if (!collator.getNumericCollation()) {
            this.collator_ = collator;
            this.strength_ = collator.getStrength();
            this.ceMask_ = getMask(this.strength_);
            this.toShift_ = collator.isAlternateHandlingShifted();
            this.variableTop_ = collator.getVariableTop();
            this.nfd_ = Normalizer2.getNFDInstance();
            this.pattern_ = new Pattern(pattern);
            this.search_.setMatchedLength(0);
            this.search_.matchedIndex_ = -1;
            this.utilIter_ = null;
            this.textIter_ = new CollationElementIterator(target, collator);
            this.textProcessedIter_ = null;
            ULocale collLocale = collator.getLocale(ULocale.VALID_LOCALE);
            this.search_.internalBreakIter_ = BreakIterator.getCharacterInstance(collLocale == null ? ULocale.ROOT : collLocale);
            this.search_.internalBreakIter_.setText((CharacterIterator) target.clone());
            initialize();
            return;
        }
        throw new UnsupportedOperationException("Numeric collation is not supported by StringSearch");
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
        if (collator != null) {
            this.collator_ = collator;
            this.ceMask_ = getMask(this.collator_.getStrength());
            ULocale collLocale = collator.getLocale(ULocale.VALID_LOCALE);
            this.search_.internalBreakIter_ = BreakIterator.getCharacterInstance(collLocale == null ? ULocale.ROOT : collLocale);
            this.search_.internalBreakIter_.setText((CharacterIterator) this.search_.text().clone());
            this.toShift_ = collator.isAlternateHandlingShifted();
            this.variableTop_ = collator.getVariableTop();
            this.textIter_ = new CollationElementIterator(this.pattern_.text_, collator);
            this.utilIter_ = new CollationElementIterator(this.pattern_.text_, collator);
            initialize();
            return;
        }
        throw new IllegalArgumentException("Collator can not be null");
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
            return -1;
        }
        return result;
    }

    public void setIndex(int position) {
        super.setIndex(position);
        this.textIter_.setOffset(position);
    }

    public void reset() {
        boolean sameCollAttribute = true;
        int newStrength = this.collator_.getStrength();
        if ((this.strength_ < 3 && newStrength >= 3) || (this.strength_ >= 3 && newStrength < 3)) {
            sameCollAttribute = false;
        }
        this.strength_ = this.collator_.getStrength();
        int ceMask = getMask(this.strength_);
        if (this.ceMask_ != ceMask) {
            this.ceMask_ = ceMask;
            sameCollAttribute = false;
        }
        boolean shift = this.collator_.isAlternateHandlingShifted();
        if (this.toShift_ != shift) {
            this.toShift_ = shift;
            sameCollAttribute = false;
        }
        int varTop = this.collator_.getVariableTop();
        if (this.variableTop_ != varTop) {
            this.variableTop_ = varTop;
            sameCollAttribute = false;
        }
        if (!sameCollAttribute) {
            initialize();
        }
        this.textIter_.setText(this.search_.text());
        this.search_.setMatchedLength(0);
        this.search_.matchedIndex_ = -1;
        this.search_.isOverlap_ = false;
        this.search_.isCanonicalMatch_ = false;
        this.search_.elementComparisonType_ = SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        this.search_.isForwardSearching_ = true;
        this.search_.reset_ = true;
    }

    /* access modifiers changed from: protected */
    public int handleNext(int position) {
        if (this.pattern_.CELength_ == 0) {
            this.search_.matchedIndex_ = this.search_.matchedIndex_ == -1 ? getIndex() : this.search_.matchedIndex_ + 1;
            this.search_.setMatchedLength(0);
            this.textIter_.setOffset(this.search_.matchedIndex_);
            if (this.search_.matchedIndex_ == this.search_.endIndex()) {
                this.search_.matchedIndex_ = -1;
            }
            return -1;
        }
        if (this.search_.matchedLength() <= 0) {
            this.search_.matchedIndex_ = position - 1;
        }
        this.textIter_.setOffset(position);
        if (this.search_.isCanonicalMatch_) {
            handleNextCanonical();
        } else {
            handleNextExact();
        }
        if (this.search_.matchedIndex_ == -1) {
            this.textIter_.setOffset(this.search_.endIndex());
        } else {
            this.textIter_.setOffset(this.search_.matchedIndex_);
        }
        return this.search_.matchedIndex_;
    }

    /* access modifiers changed from: protected */
    public int handlePrevious(int position) {
        if (this.pattern_.CELength_ == 0) {
            this.search_.matchedIndex_ = this.search_.matchedIndex_ == -1 ? getIndex() : this.search_.matchedIndex_;
            if (this.search_.matchedIndex_ == this.search_.beginIndex()) {
                setMatchNotFound();
            } else {
                SearchIterator.Search search = this.search_;
                search.matchedIndex_--;
                this.textIter_.setOffset(this.search_.matchedIndex_);
                this.search_.setMatchedLength(0);
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
            case 0:
                return PRIMARYORDERMASK;
            case 1:
                return -256;
            default:
                return -1;
        }
    }

    private int getCE(int sourcece) {
        int sourcece2 = sourcece & this.ceMask_;
        if (this.toShift_) {
            if (this.variableTop_ <= sourcece2) {
                return sourcece2;
            }
            if (this.strength_ >= 3) {
                return sourcece2 & PRIMARYORDERMASK;
            }
            return 0;
        } else if (this.strength_ < 3 || sourcece2 != 0) {
            return sourcece2;
        } else {
            return DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
        }
    }

    private static int[] addToIntArray(int[] destination, int offset, int value, int increments) {
        int newlength = destination.length;
        if (offset + 1 == newlength) {
            int[] temp = new int[(newlength + increments)];
            System.arraycopy(destination, 0, temp, 0, offset);
            destination = temp;
        }
        destination[offset] = value;
        return destination;
    }

    private static long[] addToLongArray(long[] destination, int offset, int destinationlength, long value, int increments) {
        int newlength = destinationlength;
        if (offset + 1 == newlength) {
            long[] temp = new long[(newlength + increments)];
            System.arraycopy(destination, 0, temp, 0, offset);
            destination = temp;
        }
        destination[offset] = value;
        return destination;
    }

    private int initializePatternCETable() {
        int[] cetable = new int[256];
        int patternlength = this.pattern_.text_.length();
        CollationElementIterator coleiter = this.utilIter_;
        if (coleiter == null) {
            coleiter = new CollationElementIterator(this.pattern_.text_, this.collator_);
            this.utilIter_ = coleiter;
        } else {
            coleiter.setText(this.pattern_.text_);
        }
        int offset = 0;
        int[] cetable2 = cetable;
        int result = 0;
        while (true) {
            int next = coleiter.next();
            int ce = next;
            if (next != -1) {
                int newce = getCE(ce);
                if (newce != 0) {
                    int[] temp = addToIntArray(cetable2, offset, newce, (patternlength - coleiter.getOffset()) + 1);
                    offset++;
                    cetable2 = temp;
                }
                result += coleiter.getMaxExpansion(ce) - 1;
            } else {
                cetable2[offset] = 0;
                this.pattern_.CE_ = cetable2;
                this.pattern_.CELength_ = offset;
                return result;
            }
        }
    }

    private int initializePatternPCETable() {
        long[] pcetable = new long[256];
        int pcetablesize = pcetable.length;
        int patternlength = this.pattern_.text_.length();
        CollationElementIterator coleiter = this.utilIter_;
        if (coleiter == null) {
            coleiter = new CollationElementIterator(this.pattern_.text_, this.collator_);
            this.utilIter_ = coleiter;
        } else {
            coleiter.setText(this.pattern_.text_);
        }
        CollationElementIterator coleiter2 = coleiter;
        CollationPCE iter = new CollationPCE(coleiter2);
        int offset = 0;
        while (true) {
            CollationPCE iter2 = iter;
            long nextProcessed = iter2.nextProcessed(null);
            long pce = nextProcessed;
            if (nextProcessed != -1) {
                offset++;
                pcetable = addToLongArray(pcetable, offset, pcetablesize, pce, (patternlength - coleiter2.getOffset()) + 1);
                iter = iter2;
            } else {
                pcetable[offset] = 0;
                this.pattern_.PCE_ = pcetable;
                this.pattern_.PCELength_ = offset;
                return 0;
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

    /* access modifiers changed from: protected */
    @Deprecated
    public void setMatchNotFound() {
        super.setMatchNotFound();
        if (this.search_.isForwardSearching_) {
            this.textIter_.setOffset(this.search_.text().getEndIndex());
        } else {
            this.textIter_.setOffset(0);
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
        if (Normalizer.quickCheck(textstr, Normalizer.NFD, 0) == Normalizer.NO) {
            textstr = Normalizer.decompose(textstr, false);
        }
        String patternstr = this.pattern_.text_;
        if (Normalizer.quickCheck(patternstr, Normalizer.NFD, 0) == Normalizer.NO) {
            patternstr = Normalizer.decompose(patternstr, false);
        }
        return textstr.equals(patternstr);
    }

    /* access modifiers changed from: private */
    public boolean initTextProcessedIter() {
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
        return breakiterator != null && breakiterator.isBoundary(index);
    }

    private static int compareCE64s(long targCE, long patCE, SearchIterator.ElementComparisonType compareType) {
        SearchIterator.ElementComparisonType elementComparisonType = compareType;
        if (targCE == patCE) {
            return -1;
        }
        if (elementComparisonType == SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
            return 0;
        }
        long targCEshifted = targCE >>> 32;
        long patCEshifted = patCE >>> 32;
        int targLev1 = (int) (targCEshifted & Collation.MAX_PRIMARY);
        int patLev1 = (int) (patCEshifted & Collation.MAX_PRIMARY);
        if (targLev1 == patLev1) {
            int targLev2 = (int) (targCEshifted & 65535);
            long j = targCEshifted;
            int patLev2 = (int) (patCEshifted & 65535);
            if (targLev2 == patLev2) {
                int targLev3 = (int) (targCE & Collation.MAX_PRIMARY);
                int patLev3 = (int) (patCE & Collation.MAX_PRIMARY);
                if (targLev3 == patLev3) {
                    return -1;
                }
                return (patLev3 == CE_LEVEL3_BASE || (elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD && targLev3 == CE_LEVEL3_BASE)) ? -1 : 0;
            } else if (targLev2 == 0) {
                return 1;
            } else {
                if (patLev2 == 0 && elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                    return 2;
                }
                return (patLev2 == CE_LEVEL2_BASE || (elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD && targLev2 == CE_LEVEL2_BASE)) ? -1 : 0;
            }
        } else if (targLev1 == 0) {
            return 1;
        } else {
            if (patLev1 == 0 && elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                return 2;
            }
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x01c3  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x01ab A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x012c  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0131  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0166  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x016f  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0191  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01a7  */
    private boolean search(int startIdx, Match m) {
        int mStart;
        int mLimit;
        boolean found;
        CEBuffer ceb;
        Match match;
        int maxLimit;
        CEI nextCEI;
        int secondIx;
        boolean z;
        boolean allowMidclusterMatch;
        boolean found2;
        int i = startIdx;
        Match match2 = m;
        if (this.pattern_.CELength_ == 0 || i < this.search_.beginIndex() || i > this.search_.endIndex()) {
            Match match3 = match2;
            throw new IllegalArgumentException("search(" + startIdx + ", m) - expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
        }
        if (this.pattern_.PCE_ == null) {
            initializePatternPCETable();
        }
        this.textIter_.setOffset(i);
        CEBuffer ceb2 = new CEBuffer(this);
        CEI targetCEI = null;
        int mStart2 = -1;
        int mLimit2 = -1;
        int targetIx = 0;
        while (true) {
            boolean found3 = true;
            int targetIxOffset = 0;
            long patCE = 0;
            CEI firstCEI = ceb2.get(targetIx);
            if (firstCEI != null) {
                CEI targetCEI2 = targetCEI;
                int patIx = 0;
                while (true) {
                    if (patIx >= this.pattern_.PCELength_) {
                        mStart = mStart2;
                        mLimit = mLimit2;
                        break;
                    }
                    patCE = this.pattern_.PCE_[patIx];
                    targetCEI2 = ceb2.get(targetIx + patIx + targetIxOffset);
                    mStart = mStart2;
                    mLimit = mLimit2;
                    int ceMatch = compareCE64s(targetCEI2.ce_, patCE, this.search_.elementComparisonType_);
                    if (ceMatch == 0) {
                        found3 = false;
                        break;
                    }
                    if (ceMatch > 0) {
                        if (ceMatch == 1) {
                            patIx--;
                            targetIxOffset++;
                        } else {
                            targetIxOffset--;
                        }
                    }
                    patIx++;
                    mStart2 = mStart;
                    mLimit2 = mLimit;
                }
                int targetIxOffset2 = targetIxOffset + this.pattern_.PCELength_;
                if (!found && (targetCEI2 == null || targetCEI2.ce_ != -1)) {
                    ceb = ceb2;
                    int i2 = patIx;
                    mStart2 = mStart;
                    mLimit2 = mLimit;
                    match = m;
                } else if (!found) {
                    CEBuffer cEBuffer = ceb2;
                    int i3 = patIx;
                    break;
                } else {
                    CEI lastCEI = ceb2.get((targetIx + targetIxOffset2) - 1);
                    int mStart3 = firstCEI.lowIndex_;
                    int minLimit = lastCEI.lowIndex_;
                    if (this.search_.elementComparisonType_ == SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                        CEI nextCEI2 = ceb2.get(targetIx + targetIxOffset2);
                        maxLimit = nextCEI2.lowIndex_;
                        int i4 = patIx;
                        if (nextCEI2.lowIndex_ == nextCEI2.highIndex_) {
                            found2 = found;
                            if (nextCEI2.ce_ != -1) {
                                found = false;
                                nextCEI = nextCEI2;
                                if (!isBreakBoundary(mStart3)) {
                                    found = false;
                                }
                                secondIx = firstCEI.highIndex_;
                                if (mStart3 == secondIx) {
                                    found = false;
                                }
                                if (this.breakIterator == null) {
                                    int i5 = secondIx;
                                    if (((nextCEI.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0 && maxLimit >= lastCEI.highIndex_ && nextCEI.highIndex_ > maxLimit && (this.nfd_.hasBoundaryBefore(codePointAt(this.targetText, maxLimit)) || this.nfd_.hasBoundaryAfter(codePointBefore(this.targetText, maxLimit)))) {
                                        z = true;
                                        allowMidclusterMatch = z;
                                        mLimit2 = maxLimit;
                                        if (minLimit >= maxLimit) {
                                            ceb = ceb2;
                                            if (minLimit != lastCEI.highIndex_ || !isBreakBoundary(minLimit)) {
                                                int nba = nextBoundaryAfter(minLimit);
                                                CEI cei = nextCEI;
                                                if (nba >= lastCEI.highIndex_ && (!allowMidclusterMatch || nba < maxLimit)) {
                                                    mLimit2 = nba;
                                                }
                                            } else {
                                                mLimit2 = minLimit;
                                                CEI cei2 = nextCEI;
                                            }
                                        } else {
                                            ceb = ceb2;
                                            CEI cei3 = nextCEI;
                                        }
                                        if (!allowMidclusterMatch) {
                                            if (mLimit2 > maxLimit) {
                                                found = false;
                                            }
                                            if (!isBreakBoundary(mLimit2)) {
                                                found = false;
                                            }
                                        }
                                        if (!checkIdentical(mStart3, mLimit2)) {
                                            found = false;
                                        }
                                        if (!found) {
                                            mStart = mStart3;
                                            mLimit = mLimit2;
                                            break;
                                        }
                                        match = m;
                                        mStart2 = mStart3;
                                    }
                                }
                                z = false;
                                allowMidclusterMatch = z;
                                mLimit2 = maxLimit;
                                if (minLimit >= maxLimit) {
                                }
                                if (!allowMidclusterMatch) {
                                }
                                if (!checkIdentical(mStart3, mLimit2)) {
                                }
                                if (!found) {
                                }
                            }
                        } else {
                            found2 = found;
                        }
                        nextCEI = nextCEI2;
                    } else {
                        found2 = found;
                        while (true) {
                            nextCEI = ceb2.get(targetIx + targetIxOffset2);
                            maxLimit = nextCEI.lowIndex_;
                            if (nextCEI.ce_ == -1) {
                                break;
                            } else if (((nextCEI.ce_ >>> 32) & Collation.MAX_PRIMARY) == 0) {
                                int ceMatch2 = compareCE64s(nextCEI.ce_, patCE, this.search_.elementComparisonType_);
                                if (ceMatch2 == 0 || ceMatch2 == 2) {
                                    found = false;
                                } else {
                                    targetIxOffset2++;
                                    CEI cei4 = nextCEI;
                                }
                            } else if (nextCEI.lowIndex_ == nextCEI.highIndex_) {
                                found = false;
                            }
                        }
                        if (!isBreakBoundary(mStart3)) {
                        }
                        secondIx = firstCEI.highIndex_;
                        if (mStart3 == secondIx) {
                        }
                        if (this.breakIterator == null) {
                        }
                        z = false;
                        allowMidclusterMatch = z;
                        mLimit2 = maxLimit;
                        if (minLimit >= maxLimit) {
                        }
                        if (!allowMidclusterMatch) {
                        }
                        if (!checkIdentical(mStart3, mLimit2)) {
                        }
                        if (!found) {
                        }
                    }
                    found = found2;
                    if (!isBreakBoundary(mStart3)) {
                    }
                    secondIx = firstCEI.highIndex_;
                    if (mStart3 == secondIx) {
                    }
                    if (this.breakIterator == null) {
                    }
                    z = false;
                    allowMidclusterMatch = z;
                    mLimit2 = maxLimit;
                    if (minLimit >= maxLimit) {
                    }
                    if (!allowMidclusterMatch) {
                    }
                    if (!checkIdentical(mStart3, mLimit2)) {
                    }
                    if (!found) {
                    }
                }
                targetIx++;
                match2 = match;
                targetCEI = targetCEI2;
                ceb2 = ceb;
                int i6 = startIdx;
            } else {
                int i7 = mStart2;
                int i8 = mLimit2;
                Match match4 = match2;
                throw new ICUException("CEBuffer.get(" + targetIx + ") returned null.");
            }
        }
        if (!found) {
            mLimit = -1;
            mStart = -1;
        }
        int mStart4 = mStart;
        int mLimit3 = mLimit;
        Match match5 = m;
        if (match5 != null) {
            match5.start_ = mStart4;
            match5.limit_ = mLimit3;
        }
        return found;
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

    /* JADX WARNING: Code restructure failed: missing block: B:64:0x013d, code lost:
        if (r0.nfd_.hasBoundaryAfter(codePointBefore(r0.targetText, r7)) != false) goto L_0x0145;
     */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x015f  */
    private boolean searchBackwards(int startIdx, Match m) {
        boolean found;
        int limitIx;
        int mStart;
        CEBuffer ceb;
        int mStart2;
        int mLimit;
        boolean found2;
        boolean allowMidclusterMatch;
        int mLimit2;
        int i = startIdx;
        Match match = m;
        if (this.pattern_.CELength_ == 0 || i < this.search_.beginIndex() || i > this.search_.endIndex()) {
            throw new IllegalArgumentException("searchBackwards(" + i + ", m) - expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
        }
        if (this.pattern_.PCE_ == null) {
            initializePatternPCETable();
        }
        CEBuffer ceb2 = new CEBuffer(this);
        int targetIx = 0;
        if (i < this.search_.endIndex()) {
            this.textIter_.setOffset(this.search_.internalBreakIter_.following(i));
            targetIx = 0;
            while (ceb2.getPrevious(targetIx).lowIndex_ >= i) {
                targetIx++;
            }
        } else {
            this.textIter_.setOffset(i);
        }
        CEI targetCEI = null;
        int limitIx2 = targetIx;
        int mStart3 = -1;
        int mLimit3 = -1;
        while (true) {
            found = true;
            CEI lastCEI = ceb2.getPrevious(targetIx);
            if (lastCEI != null) {
                int targetIxOffset = 0;
                int patIx = this.pattern_.PCELength_ - 1;
                while (true) {
                    if (patIx < 0) {
                        limitIx = limitIx2;
                        mStart = mStart3;
                        break;
                    }
                    long patCE = this.pattern_.PCE_[patIx];
                    targetCEI = ceb2.getPrevious((((this.pattern_.PCELength_ + targetIx) - 1) - patIx) + targetIxOffset);
                    limitIx = limitIx2;
                    mStart = mStart3;
                    int ceMatch = compareCE64s(targetCEI.ce_, patCE, this.search_.elementComparisonType_);
                    if (ceMatch == 0) {
                        found = false;
                        break;
                    }
                    if (ceMatch > 0) {
                        if (ceMatch == 1) {
                            patIx++;
                            targetIxOffset++;
                        } else {
                            targetIxOffset--;
                        }
                    }
                    patIx--;
                    limitIx2 = limitIx;
                    mStart3 = mStart;
                }
                if (!found && (targetCEI == null || targetCEI.ce_ != -1)) {
                    ceb = ceb2;
                    mStart3 = mStart;
                } else if (!found) {
                    CEBuffer cEBuffer = ceb2;
                    mStart2 = mStart;
                    break;
                } else {
                    boolean z = true;
                    CEI firstCEI = ceb2.getPrevious(((this.pattern_.PCELength_ + targetIx) - 1) + targetIxOffset);
                    mStart2 = firstCEI.lowIndex_;
                    if (!isBreakBoundary(mStart2)) {
                        found = false;
                    }
                    if (mStart2 == firstCEI.highIndex_) {
                        found = false;
                    }
                    int minLimit = lastCEI.lowIndex_;
                    if (targetIx > 0) {
                        CEI nextCEI = ceb2.getPrevious(targetIx - 1);
                        ceb = ceb2;
                        if (nextCEI.lowIndex_ == nextCEI.highIndex_) {
                            int i2 = mLimit3;
                            if (nextCEI.ce_ != -1) {
                                found = false;
                            }
                        }
                        int mLimit4 = nextCEI.lowIndex_;
                        int maxLimit = mLimit4;
                        if (this.breakIterator == null) {
                            found2 = found;
                            if (((nextCEI.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0) {
                                if (maxLimit >= lastCEI.highIndex_) {
                                    if (nextCEI.highIndex_ > maxLimit) {
                                        if (!this.nfd_.hasBoundaryBefore(codePointAt(this.targetText, maxLimit))) {
                                        }
                                        allowMidclusterMatch = z;
                                        if (minLimit >= maxLimit) {
                                            int nba = nextBoundaryAfter(minLimit);
                                            mLimit2 = mLimit4;
                                            if (nba >= lastCEI.highIndex_ && (!allowMidclusterMatch || nba < maxLimit)) {
                                                mLimit = nba;
                                                if (!allowMidclusterMatch) {
                                                    if (mLimit > maxLimit) {
                                                        found2 = false;
                                                    }
                                                    if (!isBreakBoundary(mLimit)) {
                                                        found = false;
                                                    }
                                                }
                                                found = found2;
                                            }
                                        } else {
                                            mLimit2 = mLimit4;
                                        }
                                        mLimit = mLimit2;
                                        if (!allowMidclusterMatch) {
                                        }
                                        found = found2;
                                    }
                                }
                            }
                        } else {
                            found2 = found;
                        }
                        z = false;
                        allowMidclusterMatch = z;
                        if (minLimit >= maxLimit) {
                        }
                        mLimit = mLimit2;
                        if (!allowMidclusterMatch) {
                        }
                        found = found2;
                    } else {
                        ceb = ceb2;
                        int i3 = mLimit3;
                        int nba2 = nextBoundaryAfter(minLimit);
                        int i4 = (nba2 <= 0 || i <= nba2) ? i : nba2;
                        int i5 = i4;
                        mLimit = i4;
                    }
                    mLimit3 = mLimit;
                    if (!checkIdentical(mStart2, mLimit3)) {
                        found = false;
                    }
                    if (found) {
                        break;
                    }
                    mStart3 = mStart2;
                }
                targetIx++;
                limitIx2 = limitIx;
                ceb2 = ceb;
            } else {
                int i6 = limitIx2;
                int i7 = mStart3;
                int i8 = mLimit3;
                throw new ICUException("CEBuffer.getPrevious(" + targetIx + ") returned null.");
            }
        }
        if (!found) {
            mLimit3 = -1;
            mStart2 = -1;
        }
        if (match != null) {
            match.start_ = mStart2;
            match.limit_ = mLimit3;
        }
        return found;
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
        } else if (this.search_.matchedIndex_ != -1) {
            textOffset = (this.search_.matchedIndex_ + this.search_.matchedLength()) - 1;
        } else {
            initializePatternPCETable();
            if (!initTextProcessedIter()) {
                setMatchNotFound();
                return false;
            }
            for (int nPCEs = 0; nPCEs < this.pattern_.PCELength_ - 1 && this.textProcessedIter_.nextProcessed(null) != -1; nPCEs++) {
            }
            textOffset = this.textIter_.getOffset();
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
        for (int i = 0; i < length; i++) {
            result.append(text.current());
            text.next();
        }
        text.setIndex(offset);
        return result.toString();
    }
}

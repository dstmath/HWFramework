package ohos.global.icu.text;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;
import ohos.global.icu.impl.coll.Collation;
import ohos.global.icu.text.SearchIterator;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ULocale;

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
    private Pattern pattern_;
    private int strength_;
    private CollationElementIterator textIter_;
    private CollationPCE textProcessedIter_;
    private boolean toShift_;
    private CollationElementIterator utilIter_;
    int variableTop_;

    private static int getMask(int i) {
        if (i != 0) {
            return i != 1 ? -1 : -256;
        }
        return -65536;
    }

    private static final boolean isOutOfBounds(int i, int i2, int i3) {
        return i3 < i || i3 > i2;
    }

    public StringSearch(String str, CharacterIterator characterIterator, RuleBasedCollator ruleBasedCollator, BreakIterator breakIterator) {
        super(characterIterator, breakIterator);
        if (!ruleBasedCollator.getNumericCollation()) {
            this.collator_ = ruleBasedCollator;
            this.strength_ = ruleBasedCollator.getStrength();
            this.ceMask_ = getMask(this.strength_);
            this.toShift_ = ruleBasedCollator.isAlternateHandlingShifted();
            this.variableTop_ = ruleBasedCollator.getVariableTop();
            this.nfd_ = Normalizer2.getNFDInstance();
            this.pattern_ = new Pattern(str);
            this.search_.setMatchedLength(0);
            this.search_.matchedIndex_ = -1;
            this.utilIter_ = null;
            this.textIter_ = new CollationElementIterator(characterIterator, ruleBasedCollator);
            this.textProcessedIter_ = null;
            ULocale locale = ruleBasedCollator.getLocale(ULocale.VALID_LOCALE);
            this.search_.internalBreakIter_ = BreakIterator.getCharacterInstance(locale == null ? ULocale.ROOT : locale);
            this.search_.internalBreakIter_.setText((CharacterIterator) characterIterator.clone());
            initialize();
            return;
        }
        throw new UnsupportedOperationException("Numeric collation is not supported by StringSearch");
    }

    public StringSearch(String str, CharacterIterator characterIterator, RuleBasedCollator ruleBasedCollator) {
        this(str, characterIterator, ruleBasedCollator, null);
    }

    public StringSearch(String str, CharacterIterator characterIterator, Locale locale) {
        this(str, characterIterator, ULocale.forLocale(locale));
    }

    public StringSearch(String str, CharacterIterator characterIterator, ULocale uLocale) {
        this(str, characterIterator, (RuleBasedCollator) Collator.getInstance(uLocale), null);
    }

    public StringSearch(String str, String str2) {
        this(str, new StringCharacterIterator(str2), (RuleBasedCollator) Collator.getInstance(), null);
    }

    public RuleBasedCollator getCollator() {
        return this.collator_;
    }

    public void setCollator(RuleBasedCollator ruleBasedCollator) {
        if (ruleBasedCollator != null) {
            this.collator_ = ruleBasedCollator;
            this.ceMask_ = getMask(this.collator_.getStrength());
            ULocale locale = ruleBasedCollator.getLocale(ULocale.VALID_LOCALE);
            SearchIterator.Search search = this.search_;
            if (locale == null) {
                locale = ULocale.ROOT;
            }
            search.internalBreakIter_ = BreakIterator.getCharacterInstance(locale);
            this.search_.internalBreakIter_.setText((CharacterIterator) this.search_.text().clone());
            this.toShift_ = ruleBasedCollator.isAlternateHandlingShifted();
            this.variableTop_ = ruleBasedCollator.getVariableTop();
            this.textIter_ = new CollationElementIterator(this.pattern_.text_, ruleBasedCollator);
            this.utilIter_ = new CollationElementIterator(this.pattern_.text_, ruleBasedCollator);
            initialize();
            return;
        }
        throw new IllegalArgumentException("Collator can not be null");
    }

    public String getPattern() {
        return this.pattern_.text_;
    }

    public void setPattern(String str) {
        if (str == null || str.length() <= 0) {
            throw new IllegalArgumentException("Pattern to search for can not be null or of length 0");
        }
        this.pattern_.text_ = str;
        initialize();
    }

    public boolean isCanonical() {
        return this.search_.isCanonicalMatch_;
    }

    public void setCanonical(boolean z) {
        this.search_.isCanonicalMatch_ = z;
    }

    @Override // ohos.global.icu.text.SearchIterator
    public void setTarget(CharacterIterator characterIterator) {
        super.setTarget(characterIterator);
        this.textIter_.setText(characterIterator);
    }

    @Override // ohos.global.icu.text.SearchIterator
    public int getIndex() {
        int offset = this.textIter_.getOffset();
        if (isOutOfBounds(this.search_.beginIndex(), this.search_.endIndex(), offset)) {
            return -1;
        }
        return offset;
    }

    @Override // ohos.global.icu.text.SearchIterator
    public void setIndex(int i) {
        super.setIndex(i);
        this.textIter_.setOffset(i);
    }

    @Override // ohos.global.icu.text.SearchIterator
    public void reset() {
        int strength = this.collator_.getStrength();
        boolean z = (this.strength_ >= 3 || strength < 3) && (this.strength_ < 3 || strength >= 3);
        this.strength_ = this.collator_.getStrength();
        int mask = getMask(this.strength_);
        if (this.ceMask_ != mask) {
            this.ceMask_ = mask;
            z = false;
        }
        boolean isAlternateHandlingShifted = this.collator_.isAlternateHandlingShifted();
        if (this.toShift_ != isAlternateHandlingShifted) {
            this.toShift_ = isAlternateHandlingShifted;
            z = false;
        }
        int variableTop = this.collator_.getVariableTop();
        if (this.variableTop_ != variableTop) {
            this.variableTop_ = variableTop;
            z = false;
        }
        if (!z) {
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
    @Override // ohos.global.icu.text.SearchIterator
    public int handleNext(int i) {
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
            this.search_.matchedIndex_ = i - 1;
        }
        this.textIter_.setOffset(i);
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
    @Override // ohos.global.icu.text.SearchIterator
    public int handlePrevious(int i) {
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
            this.textIter_.setOffset(i);
            if (this.search_.isCanonicalMatch_) {
                handlePreviousCanonical();
            } else {
                handlePreviousExact();
            }
        }
        return this.search_.matchedIndex_;
    }

    private int getCE(int i) {
        int i2 = i & this.ceMask_;
        if (this.toShift_) {
            if (this.variableTop_ <= i2) {
                return i2;
            }
            if (this.strength_ >= 3) {
                return i2 & -65536;
            }
            return 0;
        } else if (this.strength_ < 3 || i2 != 0) {
            return i2;
        } else {
            return 65535;
        }
    }

    private static int[] addToIntArray(int[] iArr, int i, int i2, int i3) {
        int length = iArr.length;
        if (i + 1 == length) {
            int[] iArr2 = new int[(length + i3)];
            System.arraycopy(iArr, 0, iArr2, 0, i);
            iArr = iArr2;
        }
        iArr[i] = i2;
        return iArr;
    }

    private static long[] addToLongArray(long[] jArr, int i, int i2, long j, int i3) {
        if (i + 1 == i2) {
            long[] jArr2 = new long[(i2 + i3)];
            System.arraycopy(jArr, 0, jArr2, 0, i);
            jArr = jArr2;
        }
        jArr[i] = j;
        return jArr;
    }

    private int initializePatternCETable() {
        int[] iArr = new int[256];
        int length = this.pattern_.text_.length();
        CollationElementIterator collationElementIterator = this.utilIter_;
        if (collationElementIterator == null) {
            collationElementIterator = new CollationElementIterator(this.pattern_.text_, this.collator_);
            this.utilIter_ = collationElementIterator;
        } else {
            collationElementIterator.setText(this.pattern_.text_);
        }
        int i = 0;
        int i2 = 0;
        while (true) {
            int next = collationElementIterator.next();
            if (next != -1) {
                int ce = getCE(next);
                if (ce != 0) {
                    iArr = addToIntArray(iArr, i, ce, (length - collationElementIterator.getOffset()) + 1);
                    i++;
                }
                i2 += collationElementIterator.getMaxExpansion(next) - 1;
            } else {
                iArr[i] = 0;
                Pattern pattern = this.pattern_;
                pattern.CE_ = iArr;
                pattern.CELength_ = i;
                return i2;
            }
        }
    }

    private int initializePatternPCETable() {
        long[] jArr = new long[256];
        int length = jArr.length;
        int length2 = this.pattern_.text_.length();
        CollationElementIterator collationElementIterator = this.utilIter_;
        if (collationElementIterator == null) {
            collationElementIterator = new CollationElementIterator(this.pattern_.text_, this.collator_);
            this.utilIter_ = collationElementIterator;
        } else {
            collationElementIterator.setText(this.pattern_.text_);
        }
        CollationPCE collationPCE = new CollationPCE(collationElementIterator);
        long[] jArr2 = jArr;
        int i = 0;
        while (true) {
            long nextProcessed = collationPCE.nextProcessed(null);
            if (nextProcessed != -1) {
                jArr2 = addToLongArray(jArr2, i, length, nextProcessed, (length2 - collationElementIterator.getOffset()) + 1);
                i++;
            } else {
                jArr2[i] = 0;
                Pattern pattern = this.pattern_;
                pattern.PCE_ = jArr2;
                pattern.PCELength_ = i;
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
    @Override // ohos.global.icu.text.SearchIterator
    @Deprecated
    public void setMatchNotFound() {
        super.setMatchNotFound();
        if (this.search_.isForwardSearching_) {
            this.textIter_.setOffset(this.search_.text().getEndIndex());
        } else {
            this.textIter_.setOffset(0);
        }
    }

    private boolean checkIdentical(int i, int i2) {
        if (this.strength_ != 15) {
            return true;
        }
        String string = getString(this.targetText, i, i2 - i);
        if (Normalizer.quickCheck(string, Normalizer.NFD, 0) == Normalizer.NO) {
            string = Normalizer.decompose(string, false);
        }
        String str = this.pattern_.text_;
        if (Normalizer.quickCheck(str, Normalizer.NFD, 0) == Normalizer.NO) {
            str = Normalizer.decompose(str, false);
        }
        return string.equals(str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean initTextProcessedIter() {
        CollationPCE collationPCE = this.textProcessedIter_;
        if (collationPCE == null) {
            this.textProcessedIter_ = new CollationPCE(this.textIter_);
            return true;
        }
        collationPCE.init(this.textIter_);
        return true;
    }

    private int nextBoundaryAfter(int i) {
        BreakIterator breakIter = this.search_.breakIter();
        if (breakIter == null) {
            breakIter = this.search_.internalBreakIter_;
        }
        return breakIter != null ? breakIter.following(i) : i;
    }

    private boolean isBreakBoundary(int i) {
        BreakIterator breakIter = this.search_.breakIter();
        if (breakIter == null) {
            breakIter = this.search_.internalBreakIter_;
        }
        return breakIter != null && breakIter.isBoundary(i);
    }

    private static int compareCE64s(long j, long j2, SearchIterator.ElementComparisonType elementComparisonType) {
        if (j == j2) {
            return -1;
        }
        if (elementComparisonType == SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
            return 0;
        }
        long j3 = j >>> 32;
        long j4 = j2 >>> 32;
        int i = (int) (j3 & Collation.MAX_PRIMARY);
        int i2 = (int) (j4 & Collation.MAX_PRIMARY);
        if (i == i2) {
            int i3 = (int) (j3 & 65535);
            int i4 = (int) (j4 & 65535);
            if (i3 == i4) {
                int i5 = (int) (j & Collation.MAX_PRIMARY);
                int i6 = (int) (j2 & Collation.MAX_PRIMARY);
                if (i5 == i6 || i6 == CE_LEVEL3_BASE) {
                    return -1;
                }
                if (elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD && i5 == CE_LEVEL3_BASE) {
                    return -1;
                }
                return 0;
            } else if (i3 == 0) {
                return 1;
            } else {
                if (i4 == 0 && elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) {
                    return 2;
                }
                if (i4 == CE_LEVEL2_BASE) {
                    return -1;
                }
                if (elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD && i3 == CE_LEVEL2_BASE) {
                    return -1;
                }
                return 0;
            }
        } else if (i == 0) {
            return 1;
        } else {
            return (i2 == 0 && elementComparisonType == SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD) ? 2 : 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Match {
        int limit_;
        int start_;

        private Match() {
            this.start_ = -1;
            this.limit_ = -1;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0104, code lost:
        if (r7.lowIndex_ == r7.highIndex_) goto L_0x0106;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0106, code lost:
        r10 = r10;
     */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x0190  */
    /* JADX WARNING: Removed duplicated region for block: B:110:0x0180 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0116  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x011b  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x016d  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x017d  */
    private boolean search(int i, Match match) {
        boolean z;
        int i2;
        int i3;
        CEBuffer cEBuffer;
        int i4;
        int i5;
        CEI cei;
        boolean z2;
        int i6;
        if (this.pattern_.CELength_ == 0 || i < this.search_.beginIndex() || i > this.search_.endIndex()) {
            throw new IllegalArgumentException("search(" + i + ", m) - expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
        }
        if (this.pattern_.PCE_ == null) {
            initializePatternPCETable();
        }
        this.textIter_.setOffset(i);
        CEBuffer cEBuffer2 = new CEBuffer(this);
        CEI cei2 = null;
        int i7 = 0;
        int i8 = -1;
        int i9 = -1;
        while (true) {
            CEI cei3 = cEBuffer2.get(i7);
            if (cei3 != null) {
                CEI cei4 = cei2;
                int i10 = 0;
                int i11 = 0;
                long j = 0;
                while (true) {
                    if (i10 >= this.pattern_.PCELength_) {
                        z = true;
                        cei2 = cei4;
                        break;
                    }
                    j = this.pattern_.PCE_[i10];
                    cei4 = cEBuffer2.get(i7 + i10 + i11);
                    int compareCE64s = compareCE64s(cei4.ce_, j, this.search_.elementComparisonType_);
                    if (compareCE64s == 0) {
                        cei2 = cei4;
                        z = false;
                        break;
                    }
                    if (compareCE64s > 0) {
                        if (compareCE64s == 1) {
                            i10--;
                            i11++;
                        } else {
                            i11--;
                        }
                    }
                    i10++;
                }
                int i12 = i11 + this.pattern_.PCELength_;
                i2 = i8;
                i3 = i9;
                if (!z && (cei2 == null || cei2.ce_ != -1)) {
                    cEBuffer = cEBuffer2;
                    i8 = i2;
                    i9 = i3;
                } else if (!z) {
                    break;
                } else {
                    int i13 = i7 + i12;
                    CEI cei5 = cEBuffer2.get(i13 - 1);
                    i2 = cei3.lowIndex_;
                    int i14 = cei5.lowIndex_;
                    if (this.search_.elementComparisonType_ == SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON) {
                        cei = cEBuffer2.get(i13);
                        i5 = cei.lowIndex_;
                        if (cei.lowIndex_ != cei.highIndex_ || cei.ce_ == -1) {
                            cEBuffer = cEBuffer2;
                            if (!isBreakBoundary(i2)) {
                                z = false;
                            }
                            if (i2 == cei3.highIndex_) {
                                z = false;
                            }
                            z2 = this.breakIterator != null && ((cei.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0 && i5 >= cei5.highIndex_ && cei.highIndex_ > i5 && (this.nfd_.hasBoundaryBefore(codePointAt(this.targetText, i5)) || this.nfd_.hasBoundaryAfter(codePointBefore(this.targetText, i5)));
                            if (i14 >= i5 || ((i14 != cei5.highIndex_ || !isBreakBoundary(i14)) && ((i14 = nextBoundaryAfter(i14)) < cei5.highIndex_ || (z2 && i14 >= i5)))) {
                                i14 = i5;
                            }
                            if (!z2) {
                                if (i14 > i5) {
                                    z = false;
                                }
                                if (!isBreakBoundary(i14)) {
                                    z = false;
                                }
                            }
                            if (!checkIdentical(i2, i14)) {
                                z = false;
                            }
                            if (z) {
                                i3 = i14;
                                break;
                            }
                            i9 = i14;
                            i8 = i2;
                        } else {
                            cEBuffer = cEBuffer2;
                        }
                    } else {
                        while (true) {
                            cei = cEBuffer2.get(i7 + i12);
                            i6 = cei.lowIndex_;
                            if (cei.ce_ == -1) {
                                cEBuffer = cEBuffer2;
                                break;
                            } else if (((cei.ce_ >>> 32) & Collation.MAX_PRIMARY) == 0) {
                                cEBuffer = cEBuffer2;
                                int compareCE64s2 = compareCE64s(cei.ce_, j, this.search_.elementComparisonType_);
                                if (compareCE64s2 == 0 || compareCE64s2 == 2) {
                                    break;
                                }
                                i12++;
                                cEBuffer2 = cEBuffer;
                            } else {
                                cEBuffer = cEBuffer2;
                            }
                        }
                        i5 = i6;
                        if (!isBreakBoundary(i2)) {
                        }
                        if (i2 == cei3.highIndex_) {
                        }
                        if (this.breakIterator != null) {
                        }
                        i14 = i5;
                        if (!z2) {
                        }
                        if (!checkIdentical(i2, i14)) {
                        }
                        if (z) {
                        }
                    }
                    z = false;
                    if (!isBreakBoundary(i2)) {
                    }
                    if (i2 == cei3.highIndex_) {
                    }
                    if (this.breakIterator != null) {
                    }
                    i14 = i5;
                    if (!z2) {
                    }
                    if (!checkIdentical(i2, i14)) {
                    }
                    if (z) {
                    }
                }
                i7++;
                cEBuffer2 = cEBuffer;
            } else {
                throw new ICUException("CEBuffer.get(" + i7 + ") returned null.");
            }
        }
        if (!z) {
            i4 = -1;
            i2 = -1;
        } else {
            i4 = i3;
        }
        if (match != null) {
            match.start_ = i2;
            match.limit_ = i4;
        }
        return z;
    }

    private static int codePointAt(CharacterIterator characterIterator, int i) {
        int index = characterIterator.getIndex();
        char index2 = characterIterator.setIndex(i);
        boolean isHighSurrogate = Character.isHighSurrogate(index2);
        int i2 = index2;
        if (isHighSurrogate) {
            char next = characterIterator.next();
            i2 = index2;
            if (Character.isLowSurrogate(next)) {
                i2 = Character.toCodePoint(index2, next);
            }
        }
        characterIterator.setIndex(index);
        return i2 == 1 ? 1 : 0;
    }

    private static int codePointBefore(CharacterIterator characterIterator, int i) {
        int index = characterIterator.getIndex();
        characterIterator.setIndex(i);
        char previous = characterIterator.previous();
        boolean isLowSurrogate = Character.isLowSurrogate(previous);
        int i2 = previous;
        if (isLowSurrogate) {
            char previous2 = characterIterator.previous();
            i2 = previous;
            if (Character.isHighSurrogate(previous2)) {
                i2 = Character.toCodePoint(previous2, previous);
            }
        }
        characterIterator.setIndex(index);
        return i2 == 1 ? 1 : 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a9, code lost:
        if (r13.ce_ != -1) goto L_0x00ab;
     */
    private boolean searchBackwards(int i, Match match) {
        int i2;
        int i3;
        boolean z;
        CEI cei;
        int i4;
        boolean z2;
        int i5;
        int i6;
        if (this.pattern_.CELength_ == 0 || i < this.search_.beginIndex() || i > this.search_.endIndex()) {
            throw new IllegalArgumentException("searchBackwards(" + i + ", m) - expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
        }
        if (this.pattern_.PCE_ == null) {
            initializePatternPCETable();
        }
        CEBuffer cEBuffer = new CEBuffer(this);
        if (i < this.search_.endIndex()) {
            this.textIter_.setOffset(this.search_.internalBreakIter_.following(i));
            i2 = 0;
            while (cEBuffer.getPrevious(i2).lowIndex_ >= i) {
                i2++;
            }
        } else {
            this.textIter_.setOffset(i);
            i2 = 0;
        }
        CEI cei2 = null;
        int i7 = -1;
        int i8 = -1;
        while (true) {
            CEI previous = cEBuffer.getPrevious(i2);
            if (previous != null) {
                int i9 = this.pattern_.PCELength_ - 1;
                CEI cei3 = cei2;
                int i10 = 0;
                while (true) {
                    if (i9 < 0) {
                        i3 = i7;
                        z = true;
                        break;
                    }
                    long j = this.pattern_.PCE_[i9];
                    CEI previous2 = cEBuffer.getPrevious((((this.pattern_.PCELength_ + i2) - 1) - i9) + i10);
                    i3 = i7;
                    int compareCE64s = compareCE64s(previous2.ce_, j, this.search_.elementComparisonType_);
                    if (compareCE64s == 0) {
                        cei3 = previous2;
                        z = false;
                        break;
                    }
                    if (compareCE64s > 0) {
                        if (compareCE64s == 1) {
                            i9++;
                            i10++;
                        } else {
                            i10--;
                        }
                    }
                    i9--;
                    cei3 = previous2;
                    i7 = i3;
                }
                if (!z) {
                    if (cei3 != null) {
                        i4 = i10;
                    }
                    cei = cei3;
                    i7 = i3;
                    i2++;
                    cei2 = cei;
                } else {
                    i4 = i10;
                }
                if (!z) {
                    z2 = z;
                    i5 = i3;
                    break;
                }
                CEI previous3 = cEBuffer.getPrevious(((this.pattern_.PCELength_ + i2) - 1) + i4);
                int i11 = previous3.lowIndex_;
                if (!isBreakBoundary(i11)) {
                    z = false;
                }
                z2 = i11 == previous3.highIndex_ ? false : z;
                int i12 = previous.lowIndex_;
                if (i2 > 0) {
                    CEI previous4 = cEBuffer.getPrevious(i2 - 1);
                    if (previous4.lowIndex_ == previous4.highIndex_) {
                        cei = cei3;
                        if (previous4.ce_ != -1) {
                            z2 = false;
                        }
                    } else {
                        cei = cei3;
                    }
                    int i13 = previous4.lowIndex_;
                    boolean z3 = this.breakIterator == null && ((previous4.ce_ >>> 32) & Collation.MAX_PRIMARY) != 0 && i13 >= previous.highIndex_ && previous4.highIndex_ > i13 && (this.nfd_.hasBoundaryBefore(codePointAt(this.targetText, i13)) || this.nfd_.hasBoundaryAfter(codePointBefore(this.targetText, i13)));
                    if (i12 >= i13 || (i6 = nextBoundaryAfter(i12)) < previous.highIndex_ || (z3 && i6 >= i13)) {
                        i6 = i13;
                    }
                    if (!z3) {
                        if (i6 > i13) {
                            z2 = false;
                        }
                        if (!isBreakBoundary(i6)) {
                            z2 = false;
                        }
                    }
                } else {
                    cei = cei3;
                    i6 = nextBoundaryAfter(i12);
                    if (i6 <= 0 || i <= i6) {
                        i6 = i;
                    }
                }
                if (!checkIdentical(i11, i6)) {
                    z2 = false;
                }
                if (z2) {
                    i8 = i6;
                    i5 = i11;
                    break;
                }
                i7 = i11;
                i8 = i6;
                i2++;
                cei2 = cei;
            } else {
                throw new ICUException("CEBuffer.getPrevious(" + i2 + ") returned null.");
            }
        }
        if (!z2) {
            i5 = -1;
            i8 = -1;
        }
        if (match != null) {
            match.start_ = i5;
            match.limit_ = i8;
        }
        return z2;
    }

    private boolean handleNextExact() {
        return handleNextCommonImpl();
    }

    private boolean handleNextCanonical() {
        return handleNextCommonImpl();
    }

    private boolean handleNextCommonImpl() {
        int offset = this.textIter_.getOffset();
        Match match = new Match();
        if (search(offset, match)) {
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
        int i;
        if (!this.search_.isOverlap_) {
            i = this.textIter_.getOffset();
        } else if (this.search_.matchedIndex_ != -1) {
            i = (this.search_.matchedIndex_ + this.search_.matchedLength()) - 1;
        } else {
            initializePatternPCETable();
            if (!initTextProcessedIter()) {
                setMatchNotFound();
                return false;
            }
            for (int i2 = 0; i2 < this.pattern_.PCELength_ - 1 && this.textProcessedIter_.nextProcessed(null) != -1; i2++) {
            }
            i = this.textIter_.getOffset();
        }
        Match match = new Match();
        if (searchBackwards(i, match)) {
            this.search_.matchedIndex_ = match.start_;
            this.search_.setMatchedLength(match.limit_ - match.start_);
            return true;
        }
        setMatchNotFound();
        return false;
    }

    private static final String getString(CharacterIterator characterIterator, int i, int i2) {
        StringBuilder sb = new StringBuilder(i2);
        int index = characterIterator.getIndex();
        characterIterator.setIndex(i);
        for (int i3 = 0; i3 < i2; i3++) {
            sb.append(characterIterator.current());
            characterIterator.next();
        }
        characterIterator.setIndex(index);
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static final class Pattern {
        int CELength_ = 0;
        int[] CE_;
        int PCELength_ = 0;
        long[] PCE_;
        String text_;

        protected Pattern(String str) {
            this.text_ = str;
        }
    }

    /* access modifiers changed from: private */
    public static class CollationPCE {
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

        public static final class Range {
            int ixHigh_;
            int ixLow_;
        }

        private static boolean isContinuation(int i) {
            return (i & 192) == 192;
        }

        public CollationPCE(CollationElementIterator collationElementIterator) {
            init(collationElementIterator);
        }

        public void init(CollationElementIterator collationElementIterator) {
            this.cei_ = collationElementIterator;
            init(collationElementIterator.getRuleBasedCollator());
        }

        private void init(RuleBasedCollator ruleBasedCollator) {
            this.strength_ = ruleBasedCollator.getStrength();
            this.toShift_ = ruleBasedCollator.isAlternateHandlingShifted();
            this.isShifted_ = false;
            this.variableTop_ = ruleBasedCollator.getVariableTop();
        }

        private long processCE(int i) {
            long j;
            long j2;
            int i2 = this.strength_;
            long j3 = 0;
            if (i2 != 0) {
                j2 = i2 != 1 ? (long) CollationElementIterator.tertiaryOrder(i) : 0;
                j = (long) CollationElementIterator.secondaryOrder(i);
            } else {
                j2 = 0;
                j = 0;
            }
            long primaryOrder = (long) CollationElementIterator.primaryOrder(i);
            if ((!this.toShift_ || this.variableTop_ <= i || primaryOrder == 0) && (!this.isShifted_ || primaryOrder != 0)) {
                if (this.strength_ >= 3) {
                    j3 = 65535;
                }
                this.isShifted_ = false;
                j3 = primaryOrder;
                primaryOrder = j3;
            } else if (primaryOrder == 0) {
                return 0;
            } else {
                if (this.strength_ < 3) {
                    primaryOrder = 0;
                }
                this.isShifted_ = true;
                j2 = 0;
                j = 0;
            }
            return (j3 << 48) | (j << 32) | (j2 << 16) | primaryOrder;
        }

        public long nextProcessed(Range range) {
            int offset;
            int offset2;
            long j;
            this.pceBuffer_.reset();
            while (true) {
                offset = this.cei_.getOffset();
                int next = this.cei_.next();
                offset2 = this.cei_.getOffset();
                if (next != -1) {
                    j = processCE(next);
                    if (j != 0) {
                        break;
                    }
                } else {
                    j = -1;
                    break;
                }
            }
            if (range != null) {
                range.ixLow_ = offset;
                range.ixHigh_ = offset2;
            }
            return j;
        }

        public long previousProcessed(Range range) {
            while (this.pceBuffer_.empty()) {
                RCEBuffer rCEBuffer = new RCEBuffer();
                boolean z = false;
                while (true) {
                    int offset = this.cei_.getOffset();
                    int previous = this.cei_.previous();
                    int offset2 = this.cei_.getOffset();
                    if (previous != -1) {
                        rCEBuffer.put(previous, offset2, offset);
                        if ((-65536 & previous) != 0 && !isContinuation(previous)) {
                            break;
                        }
                    } else if (rCEBuffer.empty()) {
                        z = true;
                    }
                }
                if (z) {
                    break;
                }
                while (!rCEBuffer.empty()) {
                    RCEI rcei = rCEBuffer.get();
                    long processCE = processCE(rcei.ce_);
                    if (processCE != 0) {
                        this.pceBuffer_.put(processCE, rcei.low_, rcei.high_);
                    }
                }
            }
            if (!this.pceBuffer_.empty()) {
                PCEI pcei = this.pceBuffer_.get();
                if (range != null) {
                    range.ixLow_ = pcei.low_;
                    range.ixHigh_ = pcei.high_;
                }
                return pcei.ce_;
            } else if (range == null) {
                return -1;
            } else {
                range.ixLow_ = -1;
                range.ixHigh_ = -1;
                return -1;
            }
        }

        /* access modifiers changed from: private */
        public static final class PCEI {
            long ce_;
            int high_;
            int low_;

            private PCEI() {
            }
        }

        /* access modifiers changed from: private */
        public static final class PCEBuffer {
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
            public void put(long j, int i, int i2) {
                int i3 = this.bufferIndex_;
                PCEI[] pceiArr = this.buffer_;
                if (i3 >= pceiArr.length) {
                    PCEI[] pceiArr2 = new PCEI[(pceiArr.length + 8)];
                    System.arraycopy(pceiArr, 0, pceiArr2, 0, pceiArr.length);
                    this.buffer_ = pceiArr2;
                }
                this.buffer_[this.bufferIndex_] = new PCEI();
                PCEI[] pceiArr3 = this.buffer_;
                int i4 = this.bufferIndex_;
                pceiArr3[i4].ce_ = j;
                pceiArr3[i4].low_ = i;
                pceiArr3[i4].high_ = i2;
                this.bufferIndex_ = i4 + 1;
            }

            /* access modifiers changed from: package-private */
            public PCEI get() {
                int i = this.bufferIndex_;
                if (i <= 0) {
                    return null;
                }
                PCEI[] pceiArr = this.buffer_;
                int i2 = i - 1;
                this.bufferIndex_ = i2;
                return pceiArr[i2];
            }
        }

        /* access modifiers changed from: private */
        public static final class RCEI {
            int ce_;
            int high_;
            int low_;

            private RCEI() {
            }
        }

        /* access modifiers changed from: private */
        public static final class RCEBuffer {
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
            public void put(int i, int i2, int i3) {
                int i4 = this.bufferIndex_;
                RCEI[] rceiArr = this.buffer_;
                if (i4 >= rceiArr.length) {
                    RCEI[] rceiArr2 = new RCEI[(rceiArr.length + 8)];
                    System.arraycopy(rceiArr, 0, rceiArr2, 0, rceiArr.length);
                    this.buffer_ = rceiArr2;
                }
                this.buffer_[this.bufferIndex_] = new RCEI();
                RCEI[] rceiArr3 = this.buffer_;
                int i5 = this.bufferIndex_;
                rceiArr3[i5].ce_ = i;
                rceiArr3[i5].low_ = i2;
                rceiArr3[i5].high_ = i3;
                this.bufferIndex_ = i5 + 1;
            }

            /* access modifiers changed from: package-private */
            public RCEI get() {
                int i = this.bufferIndex_;
                if (i <= 0) {
                    return null;
                }
                RCEI[] rceiArr = this.buffer_;
                int i2 = i - 1;
                this.bufferIndex_ = i2;
                return rceiArr[i2];
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CEI {
        long ce_;
        int highIndex_;
        int lowIndex_;

        private CEI() {
        }
    }

    /* access modifiers changed from: private */
    public static class CEBuffer {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        static final int CEBUFFER_EXTRA = 32;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_JAMO_L = 8;
        static final int MAX_TARGET_IGNORABLES_PER_PAT_OTHER = 3;
        int bufSize_;
        CEI[] buf_;
        int firstIx_;
        int limitIx_;
        StringSearch strSearch_;

        static boolean MIGHT_BE_JAMO_L(char c) {
            return (c >= 4352 && c <= 4446) || (c >= 12593 && c <= 12622) || (c >= 12645 && c <= 12678);
        }

        CEBuffer(StringSearch stringSearch) {
            String str;
            this.strSearch_ = stringSearch;
            this.bufSize_ = stringSearch.pattern_.PCELength_ + 32;
            if (!(stringSearch.search_.elementComparisonType_ == SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON || (str = stringSearch.pattern_.text_) == null)) {
                for (int i = 0; i < str.length(); i++) {
                    if (MIGHT_BE_JAMO_L(str.charAt(i))) {
                        this.bufSize_ += 8;
                    } else {
                        this.bufSize_ += 3;
                    }
                }
            }
            this.firstIx_ = 0;
            this.limitIx_ = 0;
            if (stringSearch.initTextProcessedIter()) {
                this.buf_ = new CEI[this.bufSize_];
            }
        }

        /* access modifiers changed from: package-private */
        public CEI get(int i) {
            int i2 = i % this.bufSize_;
            if (i >= this.firstIx_ && i < this.limitIx_) {
                return this.buf_[i2];
            }
            int i3 = this.limitIx_;
            if (i != i3) {
                return null;
            }
            this.limitIx_ = i3 + 1;
            int i4 = this.limitIx_;
            int i5 = this.firstIx_;
            if (i4 - i5 >= this.bufSize_) {
                this.firstIx_ = i5 + 1;
            }
            CollationPCE.Range range = new CollationPCE.Range();
            CEI[] ceiArr = this.buf_;
            if (ceiArr[i2] == null) {
                ceiArr[i2] = new CEI();
            }
            this.buf_[i2].ce_ = this.strSearch_.textProcessedIter_.nextProcessed(range);
            this.buf_[i2].lowIndex_ = range.ixLow_;
            this.buf_[i2].highIndex_ = range.ixHigh_;
            return this.buf_[i2];
        }

        /* access modifiers changed from: package-private */
        public CEI getPrevious(int i) {
            int i2 = i % this.bufSize_;
            if (i >= this.firstIx_ && i < this.limitIx_) {
                return this.buf_[i2];
            }
            int i3 = this.limitIx_;
            if (i != i3) {
                return null;
            }
            this.limitIx_ = i3 + 1;
            int i4 = this.limitIx_;
            int i5 = this.firstIx_;
            if (i4 - i5 >= this.bufSize_) {
                this.firstIx_ = i5 + 1;
            }
            CollationPCE.Range range = new CollationPCE.Range();
            CEI[] ceiArr = this.buf_;
            if (ceiArr[i2] == null) {
                ceiArr[i2] = new CEI();
            }
            this.buf_[i2].ce_ = this.strSearch_.textProcessedIter_.previousProcessed(range);
            this.buf_[i2].lowIndex_ = range.ixLow_;
            this.buf_[i2].highIndex_ = range.ixHigh_;
            return this.buf_[i2];
        }
    }
}

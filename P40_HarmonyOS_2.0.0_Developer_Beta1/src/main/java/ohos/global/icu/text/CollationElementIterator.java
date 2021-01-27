package ohos.global.icu.text;

import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;
import ohos.global.icu.impl.CharacterIteratorWrapper;
import ohos.global.icu.impl.coll.CollationData;
import ohos.global.icu.impl.coll.CollationIterator;
import ohos.global.icu.impl.coll.ContractionsAndExpansions;
import ohos.global.icu.impl.coll.FCDIterCollationIterator;
import ohos.global.icu.impl.coll.FCDUTF16CollationIterator;
import ohos.global.icu.impl.coll.IterCollationIterator;
import ohos.global.icu.impl.coll.UTF16CollationIterator;
import ohos.global.icu.impl.coll.UVector32;

public final class CollationElementIterator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int IGNORABLE = 0;
    public static final int NULLORDER = -1;
    private byte dir_;
    private CollationIterator iter_;
    private UVector32 offsets_;
    private int otherHalf_;
    private RuleBasedCollator rbc_;
    private String string_;

    /* access modifiers changed from: private */
    public static final boolean ceNeedsTwoParts(long j) {
        return (j & 281470698455103L) != 0;
    }

    /* access modifiers changed from: private */
    public static final int getFirstHalf(long j, int i) {
        return (((int) j) & -65536) | ((i >> 16) & 65280) | ((i >> 8) & 255);
    }

    /* access modifiers changed from: private */
    public static final int getSecondHalf(long j, int i) {
        return (((int) j) << 16) | ((i >> 8) & 65280) | (i & 63);
    }

    public static final int primaryOrder(int i) {
        return (i >>> 16) & DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
    }

    public static final int secondaryOrder(int i) {
        return (i >>> 8) & 255;
    }

    public static final int tertiaryOrder(int i) {
        return i & 255;
    }

    public int hashCode() {
        return 42;
    }

    private CollationElementIterator(RuleBasedCollator ruleBasedCollator) {
        this.iter_ = null;
        this.rbc_ = ruleBasedCollator;
        this.otherHalf_ = 0;
        this.dir_ = 0;
        this.offsets_ = null;
    }

    CollationElementIterator(String str, RuleBasedCollator ruleBasedCollator) {
        this(ruleBasedCollator);
        setText(str);
    }

    CollationElementIterator(CharacterIterator characterIterator, RuleBasedCollator ruleBasedCollator) {
        this(ruleBasedCollator);
        setText(characterIterator);
    }

    CollationElementIterator(UCharacterIterator uCharacterIterator, RuleBasedCollator ruleBasedCollator) {
        this(ruleBasedCollator);
        setText(uCharacterIterator);
    }

    public int getOffset() {
        UVector32 uVector32;
        if (this.dir_ >= 0 || (uVector32 = this.offsets_) == null || uVector32.isEmpty()) {
            return this.iter_.getOffset();
        }
        int cEsLength = this.iter_.getCEsLength();
        if (this.otherHalf_ != 0) {
            cEsLength++;
        }
        return this.offsets_.elementAti(cEsLength);
    }

    public int next() {
        byte b = this.dir_;
        if (b > 1) {
            int i = this.otherHalf_;
            if (i != 0) {
                this.otherHalf_ = 0;
                return i;
            }
        } else if (b == 1) {
            this.dir_ = 2;
        } else if (b == 0) {
            this.dir_ = 2;
        } else {
            throw new IllegalStateException("Illegal change of direction");
        }
        this.iter_.clearCEsIfNoneRemaining();
        long nextCE = this.iter_.nextCE();
        if (nextCE == 4311744768L) {
            return -1;
        }
        long j = nextCE >>> 32;
        int i2 = (int) nextCE;
        int firstHalf = getFirstHalf(j, i2);
        int secondHalf = getSecondHalf(j, i2);
        if (secondHalf != 0) {
            this.otherHalf_ = secondHalf | 192;
        }
        return firstHalf;
    }

    public int previous() {
        byte b = this.dir_;
        int i = 0;
        if (b < 0) {
            int i2 = this.otherHalf_;
            if (i2 != 0) {
                this.otherHalf_ = 0;
                return i2;
            }
        } else if (b == 0) {
            this.iter_.resetToOffset(this.string_.length());
            this.dir_ = -1;
        } else if (b == 1) {
            this.dir_ = -1;
        } else {
            throw new IllegalStateException("Illegal change of direction");
        }
        if (this.offsets_ == null) {
            this.offsets_ = new UVector32();
        }
        if (this.iter_.getCEsLength() == 0) {
            i = this.iter_.getOffset();
        }
        long previousCE = this.iter_.previousCE(this.offsets_);
        if (previousCE == 4311744768L) {
            return -1;
        }
        long j = previousCE >>> 32;
        int i3 = (int) previousCE;
        int firstHalf = getFirstHalf(j, i3);
        int secondHalf = getSecondHalf(j, i3);
        if (secondHalf == 0) {
            return firstHalf;
        }
        if (this.offsets_.isEmpty()) {
            this.offsets_.addElement(this.iter_.getOffset());
            this.offsets_.addElement(i);
        }
        this.otherHalf_ = firstHalf;
        return secondHalf | 192;
    }

    public void reset() {
        this.iter_.resetToOffset(0);
        this.otherHalf_ = 0;
        this.dir_ = 0;
    }

    public void setOffset(int i) {
        int offset;
        if (i > 0 && i < this.string_.length()) {
            int i2 = i;
            do {
                char charAt = this.string_.charAt(i2);
                if (!this.rbc_.isUnsafe(charAt) || (Character.isHighSurrogate(charAt) && !this.rbc_.isUnsafe(this.string_.codePointAt(i2)))) {
                    break;
                }
                i2--;
            } while (i2 > 0);
            if (i2 < i) {
                do {
                    this.iter_.resetToOffset(i2);
                    do {
                        this.iter_.nextCE();
                        offset = this.iter_.getOffset();
                    } while (offset == i2);
                    if (offset <= i) {
                        i2 = offset;
                        continue;
                    }
                } while (offset < i);
                i = i2;
            }
        }
        this.iter_.resetToOffset(i);
        this.otherHalf_ = 0;
        this.dir_ = 1;
    }

    public void setText(String str) {
        UTF16CollationIterator uTF16CollationIterator;
        this.string_ = str;
        boolean isNumeric = this.rbc_.settings.readOnly().isNumeric();
        if (this.rbc_.settings.readOnly().dontCheckFCD()) {
            uTF16CollationIterator = new UTF16CollationIterator(this.rbc_.data, isNumeric, this.string_, 0);
        } else {
            uTF16CollationIterator = new FCDUTF16CollationIterator(this.rbc_.data, isNumeric, this.string_, 0);
        }
        this.iter_ = uTF16CollationIterator;
        this.otherHalf_ = 0;
        this.dir_ = 0;
    }

    public void setText(UCharacterIterator uCharacterIterator) {
        IterCollationIterator iterCollationIterator;
        this.string_ = uCharacterIterator.getText();
        try {
            UCharacterIterator uCharacterIterator2 = (UCharacterIterator) uCharacterIterator.clone();
            uCharacterIterator2.setToStart();
            boolean isNumeric = this.rbc_.settings.readOnly().isNumeric();
            if (this.rbc_.settings.readOnly().dontCheckFCD()) {
                iterCollationIterator = new IterCollationIterator(this.rbc_.data, isNumeric, uCharacterIterator2);
            } else {
                iterCollationIterator = new FCDIterCollationIterator(this.rbc_.data, isNumeric, uCharacterIterator2, 0);
            }
            this.iter_ = iterCollationIterator;
            this.otherHalf_ = 0;
            this.dir_ = 0;
        } catch (CloneNotSupportedException unused) {
            setText(uCharacterIterator.getText());
        }
    }

    public void setText(CharacterIterator characterIterator) {
        IterCollationIterator iterCollationIterator;
        CharacterIteratorWrapper characterIteratorWrapper = new CharacterIteratorWrapper(characterIterator);
        characterIteratorWrapper.setToStart();
        this.string_ = characterIteratorWrapper.getText();
        boolean isNumeric = this.rbc_.settings.readOnly().isNumeric();
        if (this.rbc_.settings.readOnly().dontCheckFCD()) {
            iterCollationIterator = new IterCollationIterator(this.rbc_.data, isNumeric, characterIteratorWrapper);
        } else {
            iterCollationIterator = new FCDIterCollationIterator(this.rbc_.data, isNumeric, characterIteratorWrapper, 0);
        }
        this.iter_ = iterCollationIterator;
        this.otherHalf_ = 0;
        this.dir_ = 0;
    }

    /* access modifiers changed from: private */
    public static final class MaxExpSink implements ContractionsAndExpansions.CESink {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private Map<Integer, Integer> maxExpansions;

        public void handleCE(long j) {
        }

        MaxExpSink(Map<Integer, Integer> map) {
            this.maxExpansions = map;
        }

        public void handleExpansion(long[] jArr, int i, int i2) {
            if (i2 > 1) {
                int i3 = 0;
                for (int i4 = 0; i4 < i2; i4++) {
                    i3 += CollationElementIterator.ceNeedsTwoParts(jArr[i + i4]) ? 2 : 1;
                }
                long j = jArr[(i + i2) - 1];
                long j2 = j >>> 32;
                int i5 = (int) j;
                int secondHalf = CollationElementIterator.getSecondHalf(j2, i5);
                int firstHalf = secondHalf == 0 ? CollationElementIterator.getFirstHalf(j2, i5) : secondHalf | 192;
                Integer num = this.maxExpansions.get(Integer.valueOf(firstHalf));
                if (num == null || i3 > num.intValue()) {
                    this.maxExpansions.put(Integer.valueOf(firstHalf), Integer.valueOf(i3));
                }
            }
        }
    }

    static final Map<Integer, Integer> computeMaxExpansions(CollationData collationData) {
        HashMap hashMap = new HashMap();
        new ContractionsAndExpansions((UnicodeSet) null, (UnicodeSet) null, new MaxExpSink(hashMap), true).forData(collationData);
        return hashMap;
    }

    public int getMaxExpansion(int i) {
        return getMaxExpansion(this.rbc_.tailoring.maxExpansions, i);
    }

    static int getMaxExpansion(Map<Integer, Integer> map, int i) {
        Integer num;
        if (i == 0) {
            return 1;
        }
        if (map != null && (num = map.get(Integer.valueOf(i))) != null) {
            return num.intValue();
        }
        if ((i & 192) == 192) {
            return 2;
        }
        return 1;
    }

    private byte normalizeDir() {
        byte b = this.dir_;
        if (b == 1) {
            return 0;
        }
        return b;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CollationElementIterator)) {
            return false;
        }
        CollationElementIterator collationElementIterator = (CollationElementIterator) obj;
        return this.rbc_.equals(collationElementIterator.rbc_) && this.otherHalf_ == collationElementIterator.otherHalf_ && normalizeDir() == collationElementIterator.normalizeDir() && this.string_.equals(collationElementIterator.string_) && this.iter_.equals(collationElementIterator.iter_);
    }

    @Deprecated
    public RuleBasedCollator getRuleBasedCollator() {
        return this.rbc_;
    }
}

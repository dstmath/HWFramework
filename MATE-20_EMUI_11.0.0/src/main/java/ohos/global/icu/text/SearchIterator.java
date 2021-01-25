package ohos.global.icu.text;

import java.text.CharacterIterator;

public abstract class SearchIterator {
    public static final int DONE = -1;
    protected BreakIterator breakIterator;
    protected int matchLength;
    Search search_ = new Search();
    protected CharacterIterator targetText;

    public enum ElementComparisonType {
        STANDARD_ELEMENT_COMPARISON,
        PATTERN_BASE_WEIGHT_IS_WILDCARD,
        ANY_BASE_WEIGHT_IS_WILDCARD
    }

    public abstract int getIndex();

    /* access modifiers changed from: protected */
    public abstract int handleNext(int i);

    /* access modifiers changed from: protected */
    public abstract int handlePrevious(int i);

    /* access modifiers changed from: package-private */
    public final class Search {
        ElementComparisonType elementComparisonType_;
        BreakIterator internalBreakIter_;
        boolean isCanonicalMatch_;
        boolean isForwardSearching_;
        boolean isOverlap_;
        int matchedIndex_;
        boolean reset_;

        Search() {
        }

        /* access modifiers changed from: package-private */
        public CharacterIterator text() {
            return SearchIterator.this.targetText;
        }

        /* access modifiers changed from: package-private */
        public void setTarget(CharacterIterator characterIterator) {
            SearchIterator.this.targetText = characterIterator;
        }

        /* access modifiers changed from: package-private */
        public BreakIterator breakIter() {
            return SearchIterator.this.breakIterator;
        }

        /* access modifiers changed from: package-private */
        public void setBreakIter(BreakIterator breakIterator) {
            SearchIterator.this.breakIterator = breakIterator;
        }

        /* access modifiers changed from: package-private */
        public int matchedLength() {
            return SearchIterator.this.matchLength;
        }

        /* access modifiers changed from: package-private */
        public void setMatchedLength(int i) {
            SearchIterator.this.matchLength = i;
        }

        /* access modifiers changed from: package-private */
        public int beginIndex() {
            if (SearchIterator.this.targetText == null) {
                return 0;
            }
            return SearchIterator.this.targetText.getBeginIndex();
        }

        /* access modifiers changed from: package-private */
        public int endIndex() {
            if (SearchIterator.this.targetText == null) {
                return 0;
            }
            return SearchIterator.this.targetText.getEndIndex();
        }
    }

    public void setIndex(int i) {
        if (i < this.search_.beginIndex() || i > this.search_.endIndex()) {
            throw new IndexOutOfBoundsException("setIndex(int) expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
        }
        Search search = this.search_;
        search.reset_ = false;
        search.setMatchedLength(0);
        this.search_.matchedIndex_ = -1;
    }

    public void setOverlapping(boolean z) {
        this.search_.isOverlap_ = z;
    }

    public void setBreakIterator(BreakIterator breakIterator2) {
        this.search_.setBreakIter(breakIterator2);
        if (this.search_.breakIter() != null && this.search_.text() != null) {
            this.search_.breakIter().setText((CharacterIterator) this.search_.text().clone());
        }
    }

    public void setTarget(CharacterIterator characterIterator) {
        if (characterIterator == null || characterIterator.getEndIndex() == characterIterator.getIndex()) {
            throw new IllegalArgumentException("Illegal null or empty text");
        }
        characterIterator.setIndex(characterIterator.getBeginIndex());
        this.search_.setTarget(characterIterator);
        Search search = this.search_;
        search.matchedIndex_ = -1;
        search.setMatchedLength(0);
        Search search2 = this.search_;
        search2.reset_ = true;
        search2.isForwardSearching_ = true;
        if (search2.breakIter() != null) {
            this.search_.breakIter().setText((CharacterIterator) characterIterator.clone());
        }
        if (this.search_.internalBreakIter_ != null) {
            this.search_.internalBreakIter_.setText((CharacterIterator) characterIterator.clone());
        }
    }

    public int getMatchStart() {
        return this.search_.matchedIndex_;
    }

    public int getMatchLength() {
        return this.search_.matchedLength();
    }

    public BreakIterator getBreakIterator() {
        return this.search_.breakIter();
    }

    public CharacterIterator getTarget() {
        return this.search_.text();
    }

    public String getMatchedText() {
        if (this.search_.matchedLength() <= 0) {
            return null;
        }
        int matchedLength = this.search_.matchedIndex_ + this.search_.matchedLength();
        StringBuilder sb = new StringBuilder(this.search_.matchedLength());
        CharacterIterator text = this.search_.text();
        text.setIndex(this.search_.matchedIndex_);
        while (text.getIndex() < matchedLength) {
            sb.append(text.current());
            text.next();
        }
        text.setIndex(this.search_.matchedIndex_);
        return sb.toString();
    }

    public int next() {
        int index = getIndex();
        int i = this.search_.matchedIndex_;
        int matchedLength = this.search_.matchedLength();
        Search search = this.search_;
        search.reset_ = false;
        if (search.isForwardSearching_) {
            int endIndex = this.search_.endIndex();
            if (index == endIndex || i == endIndex || (i != -1 && i + matchedLength >= endIndex)) {
                setMatchNotFound();
                return -1;
            }
        } else {
            Search search2 = this.search_;
            search2.isForwardSearching_ = true;
            if (search2.matchedIndex_ != -1) {
                return i;
            }
        }
        if (matchedLength > 0) {
            index = this.search_.isOverlap_ ? index + 1 : index + matchedLength;
        }
        return handleNext(index);
    }

    public int previous() {
        int i;
        if (this.search_.reset_) {
            i = this.search_.endIndex();
            Search search = this.search_;
            search.isForwardSearching_ = false;
            search.reset_ = false;
            setIndex(i);
        } else {
            i = getIndex();
        }
        int i2 = this.search_.matchedIndex_;
        if (this.search_.isForwardSearching_) {
            this.search_.isForwardSearching_ = false;
            if (i2 != -1) {
                return i2;
            }
        } else {
            int beginIndex = this.search_.beginIndex();
            if (i == beginIndex || i2 == beginIndex) {
                setMatchNotFound();
                return -1;
            }
        }
        if (i2 == -1) {
            return handlePrevious(i);
        }
        if (this.search_.isOverlap_) {
            i2 += this.search_.matchedLength() - 2;
        }
        return handlePrevious(i2);
    }

    public boolean isOverlapping() {
        return this.search_.isOverlap_;
    }

    public void reset() {
        setMatchNotFound();
        setIndex(this.search_.beginIndex());
        Search search = this.search_;
        search.isOverlap_ = false;
        search.isCanonicalMatch_ = false;
        search.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        Search search2 = this.search_;
        search2.isForwardSearching_ = true;
        search2.reset_ = true;
    }

    public final int first() {
        int beginIndex = this.search_.beginIndex();
        setIndex(beginIndex);
        return handleNext(beginIndex);
    }

    public final int following(int i) {
        setIndex(i);
        return handleNext(i);
    }

    public final int last() {
        int endIndex = this.search_.endIndex();
        setIndex(endIndex);
        return handlePrevious(endIndex);
    }

    public final int preceding(int i) {
        setIndex(i);
        return handlePrevious(i);
    }

    protected SearchIterator(CharacterIterator characterIterator, BreakIterator breakIterator2) {
        if (characterIterator == null || characterIterator.getEndIndex() - characterIterator.getBeginIndex() == 0) {
            throw new IllegalArgumentException("Illegal argument target.  Argument can not be null or of length 0");
        }
        this.search_.setTarget(characterIterator);
        this.search_.setBreakIter(breakIterator2);
        if (this.search_.breakIter() != null) {
            this.search_.breakIter().setText((CharacterIterator) characterIterator.clone());
        }
        Search search = this.search_;
        search.isOverlap_ = false;
        search.isCanonicalMatch_ = false;
        search.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        Search search2 = this.search_;
        search2.isForwardSearching_ = true;
        search2.reset_ = true;
        search2.matchedIndex_ = -1;
        search2.setMatchedLength(0);
    }

    /* access modifiers changed from: protected */
    public void setMatchLength(int i) {
        this.search_.setMatchedLength(i);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void setMatchNotFound() {
        Search search = this.search_;
        search.matchedIndex_ = -1;
        search.setMatchedLength(0);
    }

    public void setElementComparisonType(ElementComparisonType elementComparisonType) {
        this.search_.elementComparisonType_ = elementComparisonType;
    }

    public ElementComparisonType getElementComparisonType() {
        return this.search_.elementComparisonType_;
    }
}

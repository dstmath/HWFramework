package android.icu.text;

import java.text.CharacterIterator;

public abstract class SearchIterator {
    public static final int DONE = -1;
    protected BreakIterator breakIterator;
    protected int matchLength;
    Search search_;
    protected CharacterIterator targetText;

    public enum ElementComparisonType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.SearchIterator.ElementComparisonType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.SearchIterator.ElementComparisonType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.SearchIterator.ElementComparisonType.<clinit>():void");
        }
    }

    final class Search {
        ElementComparisonType elementComparisonType_;
        BreakIterator internalBreakIter_;
        boolean isCanonicalMatch_;
        boolean isForwardSearching_;
        boolean isOverlap_;
        int matchedIndex_;
        boolean reset_;
        final /* synthetic */ SearchIterator this$0;

        Search(SearchIterator this$0) {
            this.this$0 = this$0;
        }

        CharacterIterator text() {
            return this.this$0.targetText;
        }

        void setTarget(CharacterIterator text) {
            this.this$0.targetText = text;
        }

        BreakIterator breakIter() {
            return this.this$0.breakIterator;
        }

        void setBreakIter(BreakIterator breakIter) {
            this.this$0.breakIterator = breakIter;
        }

        int matchedLength() {
            return this.this$0.matchLength;
        }

        void setMatchedLength(int matchedLength) {
            this.this$0.matchLength = matchedLength;
        }

        int beginIndex() {
            if (this.this$0.targetText == null) {
                return 0;
            }
            return this.this$0.targetText.getBeginIndex();
        }

        int endIndex() {
            if (this.this$0.targetText == null) {
                return 0;
            }
            return this.this$0.targetText.getEndIndex();
        }
    }

    public abstract int getIndex();

    protected abstract int handleNext(int i);

    protected abstract int handlePrevious(int i);

    public void setIndex(int position) {
        if (position < this.search_.beginIndex() || position > this.search_.endIndex()) {
            throw new IndexOutOfBoundsException("setIndex(int) expected position to be between " + this.search_.beginIndex() + " and " + this.search_.endIndex());
        }
        this.search_.reset_ = false;
        this.search_.setMatchedLength(0);
        this.search_.matchedIndex_ = DONE;
    }

    public void setOverlapping(boolean allowOverlap) {
        this.search_.isOverlap_ = allowOverlap;
    }

    public void setBreakIterator(BreakIterator breakiter) {
        this.search_.setBreakIter(breakiter);
        if (this.search_.breakIter() != null && this.search_.text() != null) {
            this.search_.breakIter().setText((CharacterIterator) this.search_.text().clone());
        }
    }

    public void setTarget(CharacterIterator text) {
        if (text == null || text.getEndIndex() == text.getIndex()) {
            throw new IllegalArgumentException("Illegal null or empty text");
        }
        text.setIndex(text.getBeginIndex());
        this.search_.setTarget(text);
        this.search_.matchedIndex_ = DONE;
        this.search_.setMatchedLength(0);
        this.search_.reset_ = true;
        this.search_.isForwardSearching_ = true;
        if (this.search_.breakIter() != null) {
            this.search_.breakIter().setText((CharacterIterator) text.clone());
        }
        if (this.search_.internalBreakIter_ != null) {
            this.search_.internalBreakIter_.setText((CharacterIterator) text.clone());
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
        int limit = this.search_.matchedIndex_ + this.search_.matchedLength();
        StringBuilder result = new StringBuilder(this.search_.matchedLength());
        CharacterIterator it = this.search_.text();
        it.setIndex(this.search_.matchedIndex_);
        while (it.getIndex() < limit) {
            result.append(it.current());
            it.next();
        }
        it.setIndex(this.search_.matchedIndex_);
        return result.toString();
    }

    public int next() {
        int index = getIndex();
        int matchindex = this.search_.matchedIndex_;
        int matchlength = this.search_.matchedLength();
        this.search_.reset_ = false;
        if (this.search_.isForwardSearching_) {
            int endIdx = this.search_.endIndex();
            if (index == endIdx || matchindex == endIdx || (matchindex != DONE && matchindex + matchlength >= endIdx)) {
                setMatchNotFound();
                return DONE;
            }
        }
        this.search_.isForwardSearching_ = true;
        if (this.search_.matchedIndex_ != DONE) {
            return matchindex;
        }
        if (matchlength > 0) {
            if (this.search_.isOverlap_) {
                index++;
            } else {
                index += matchlength;
            }
        }
        return handleNext(index);
    }

    public int previous() {
        int index;
        if (this.search_.reset_) {
            index = this.search_.endIndex();
            this.search_.isForwardSearching_ = false;
            this.search_.reset_ = false;
            setIndex(index);
        } else {
            index = getIndex();
        }
        int matchindex = this.search_.matchedIndex_;
        if (this.search_.isForwardSearching_) {
            this.search_.isForwardSearching_ = false;
            if (matchindex != DONE) {
                return matchindex;
            }
        }
        int startIdx = this.search_.beginIndex();
        if (index == startIdx || matchindex == startIdx) {
            setMatchNotFound();
            return DONE;
        }
        if (matchindex == DONE) {
            return handlePrevious(index);
        }
        if (this.search_.isOverlap_) {
            matchindex += this.search_.matchedLength() - 2;
        }
        return handlePrevious(matchindex);
    }

    public boolean isOverlapping() {
        return this.search_.isOverlap_;
    }

    public void reset() {
        setMatchNotFound();
        setIndex(this.search_.beginIndex());
        this.search_.isOverlap_ = false;
        this.search_.isCanonicalMatch_ = false;
        this.search_.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        this.search_.isForwardSearching_ = true;
        this.search_.reset_ = true;
    }

    public final int first() {
        int startIdx = this.search_.beginIndex();
        setIndex(startIdx);
        return handleNext(startIdx);
    }

    public final int following(int position) {
        setIndex(position);
        return handleNext(position);
    }

    public final int last() {
        int endIdx = this.search_.endIndex();
        setIndex(endIdx);
        return handlePrevious(endIdx);
    }

    public final int preceding(int position) {
        setIndex(position);
        return handlePrevious(position);
    }

    protected SearchIterator(CharacterIterator target, BreakIterator breaker) {
        this.search_ = new Search(this);
        if (target == null || target.getEndIndex() - target.getBeginIndex() == 0) {
            throw new IllegalArgumentException("Illegal argument target.  Argument can not be null or of length 0");
        }
        this.search_.setTarget(target);
        this.search_.setBreakIter(breaker);
        if (this.search_.breakIter() != null) {
            this.search_.breakIter().setText((CharacterIterator) target.clone());
        }
        this.search_.isOverlap_ = false;
        this.search_.isCanonicalMatch_ = false;
        this.search_.elementComparisonType_ = ElementComparisonType.STANDARD_ELEMENT_COMPARISON;
        this.search_.isForwardSearching_ = true;
        this.search_.reset_ = true;
        this.search_.matchedIndex_ = DONE;
        this.search_.setMatchedLength(0);
    }

    protected void setMatchLength(int length) {
        this.search_.setMatchedLength(length);
    }

    @Deprecated
    protected void setMatchNotFound() {
        this.search_.matchedIndex_ = DONE;
        this.search_.setMatchedLength(0);
    }

    public void setElementComparisonType(ElementComparisonType type) {
        this.search_.elementComparisonType_ = type;
    }

    public ElementComparisonType getElementComparisonType() {
        return this.search_.elementComparisonType_;
    }
}

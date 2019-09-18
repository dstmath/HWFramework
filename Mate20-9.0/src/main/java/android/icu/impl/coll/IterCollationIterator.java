package android.icu.impl.coll;

import android.icu.text.UCharacterIterator;

public class IterCollationIterator extends CollationIterator {
    protected UCharacterIterator iter;

    public IterCollationIterator(CollationData d, boolean numeric, UCharacterIterator ui) {
        super(d, numeric);
        this.iter = ui;
    }

    public void resetToOffset(int newOffset) {
        reset();
        this.iter.setIndex(newOffset);
    }

    public int getOffset() {
        return this.iter.getIndex();
    }

    public int nextCodePoint() {
        return this.iter.nextCodePoint();
    }

    public int previousCodePoint() {
        return this.iter.previousCodePoint();
    }

    /* access modifiers changed from: protected */
    public long handleNextCE32() {
        int c = this.iter.next();
        if (c < 0) {
            return -4294967104L;
        }
        return makeCodePointAndCE32Pair(c, this.trie.getFromU16SingleLead((char) c));
    }

    /* access modifiers changed from: protected */
    public char handleGetTrailSurrogate() {
        int trail = this.iter.next();
        if (!isTrailSurrogate(trail) && trail >= 0) {
            this.iter.previous();
        }
        return (char) trail;
    }

    /* access modifiers changed from: protected */
    public void forwardNumCodePoints(int num) {
        this.iter.moveCodePointIndex(num);
    }

    /* access modifiers changed from: protected */
    public void backwardNumCodePoints(int num) {
        this.iter.moveCodePointIndex(-num);
    }
}

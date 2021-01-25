package ohos.global.icu.impl.coll;

import ohos.global.icu.text.UCharacterIterator;

public class IterCollationIterator extends CollationIterator {
    protected UCharacterIterator iter;

    public IterCollationIterator(CollationData collationData, boolean z, UCharacterIterator uCharacterIterator) {
        super(collationData, z);
        this.iter = uCharacterIterator;
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public void resetToOffset(int i) {
        reset();
        this.iter.setIndex(i);
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int getOffset() {
        return this.iter.getIndex();
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int nextCodePoint() {
        return this.iter.nextCodePoint();
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int previousCodePoint() {
        return this.iter.previousCodePoint();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public long handleNextCE32() {
        int next = this.iter.next();
        if (next < 0) {
            return -4294967104L;
        }
        return makeCodePointAndCE32Pair(next, this.trie.getFromU16SingleLead((char) next));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public char handleGetTrailSurrogate() {
        int next = this.iter.next();
        if (!isTrailSurrogate(next) && next >= 0) {
            this.iter.previous();
        }
        return (char) next;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public void forwardNumCodePoints(int i) {
        this.iter.moveCodePointIndex(i);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public void backwardNumCodePoints(int i) {
        this.iter.moveCodePointIndex(-i);
    }
}

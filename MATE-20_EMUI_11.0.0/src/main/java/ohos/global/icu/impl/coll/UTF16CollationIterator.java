package ohos.global.icu.impl.coll;

public class UTF16CollationIterator extends CollationIterator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    protected int limit;
    protected int pos;
    protected CharSequence seq;
    protected int start;

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int hashCode() {
        return 42;
    }

    public UTF16CollationIterator(CollationData collationData) {
        super(collationData);
    }

    public UTF16CollationIterator(CollationData collationData, boolean z, CharSequence charSequence, int i) {
        super(collationData, z);
        this.seq = charSequence;
        this.start = 0;
        this.pos = i;
        this.limit = charSequence.length();
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        UTF16CollationIterator uTF16CollationIterator = (UTF16CollationIterator) obj;
        if (this.pos - this.start == uTF16CollationIterator.pos - uTF16CollationIterator.start) {
            return true;
        }
        return false;
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public void resetToOffset(int i) {
        reset();
        this.pos = this.start + i;
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int getOffset() {
        return this.pos - this.start;
    }

    public void setText(boolean z, CharSequence charSequence, int i) {
        reset(z);
        this.seq = charSequence;
        this.start = 0;
        this.pos = i;
        this.limit = charSequence.length();
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int nextCodePoint() {
        int i;
        int i2 = this.pos;
        if (i2 == this.limit) {
            return -1;
        }
        CharSequence charSequence = this.seq;
        this.pos = i2 + 1;
        char charAt = charSequence.charAt(i2);
        if (Character.isHighSurrogate(charAt) && (i = this.pos) != this.limit) {
            char charAt2 = this.seq.charAt(i);
            if (Character.isLowSurrogate(charAt2)) {
                this.pos++;
                return Character.toCodePoint(charAt, charAt2);
            }
        }
        return charAt;
    }

    @Override // ohos.global.icu.impl.coll.CollationIterator
    public int previousCodePoint() {
        int i;
        int i2 = this.pos;
        if (i2 == this.start) {
            return -1;
        }
        CharSequence charSequence = this.seq;
        int i3 = i2 - 1;
        this.pos = i3;
        char charAt = charSequence.charAt(i3);
        if (Character.isLowSurrogate(charAt) && (i = this.pos) != this.start) {
            char charAt2 = this.seq.charAt(i - 1);
            if (Character.isHighSurrogate(charAt2)) {
                this.pos--;
                return Character.toCodePoint(charAt2, charAt);
            }
        }
        return charAt;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public long handleNextCE32() {
        int i = this.pos;
        if (i == this.limit) {
            return -4294967104L;
        }
        CharSequence charSequence = this.seq;
        this.pos = i + 1;
        char charAt = charSequence.charAt(i);
        return makeCodePointAndCE32Pair(charAt, this.trie.getFromU16SingleLead(charAt));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public char handleGetTrailSurrogate() {
        int i = this.pos;
        if (i == this.limit) {
            return 0;
        }
        char charAt = this.seq.charAt(i);
        if (Character.isLowSurrogate(charAt)) {
            this.pos++;
        }
        return charAt;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public void forwardNumCodePoints(int i) {
        int i2;
        while (i > 0) {
            int i3 = this.pos;
            if (i3 != this.limit) {
                CharSequence charSequence = this.seq;
                this.pos = i3 + 1;
                i--;
                if (Character.isHighSurrogate(charSequence.charAt(i3)) && (i2 = this.pos) != this.limit && Character.isLowSurrogate(this.seq.charAt(i2))) {
                    this.pos++;
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.CollationIterator
    public void backwardNumCodePoints(int i) {
        int i2;
        while (i > 0) {
            int i3 = this.pos;
            if (i3 != this.start) {
                CharSequence charSequence = this.seq;
                int i4 = i3 - 1;
                this.pos = i4;
                i--;
                if (Character.isLowSurrogate(charSequence.charAt(i4)) && (i2 = this.pos) != this.start && Character.isHighSurrogate(this.seq.charAt(i2 - 1))) {
                    this.pos--;
                }
            } else {
                return;
            }
        }
    }
}

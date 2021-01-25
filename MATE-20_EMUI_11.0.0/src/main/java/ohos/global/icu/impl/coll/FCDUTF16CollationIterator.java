package ohos.global.icu.impl.coll;

import ohos.global.icu.impl.Normalizer2Impl;

public final class FCDUTF16CollationIterator extends UTF16CollationIterator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int rawStart = 0;
    private int checkDir;
    private final Normalizer2Impl nfcImpl;
    private StringBuilder normalized;
    private int rawLimit;
    private CharSequence rawSeq;
    private int segmentLimit;
    private int segmentStart;

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int hashCode() {
        return 42;
    }

    public FCDUTF16CollationIterator(CollationData collationData) {
        super(collationData);
        this.nfcImpl = collationData.nfcImpl;
    }

    public FCDUTF16CollationIterator(CollationData collationData, boolean z, CharSequence charSequence, int i) {
        super(collationData, z, charSequence, i);
        this.rawSeq = charSequence;
        this.segmentStart = i;
        this.rawLimit = charSequence.length();
        this.nfcImpl = collationData.nfcImpl;
        this.checkDir = 1;
    }

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public boolean equals(Object obj) {
        if (!(obj instanceof CollationIterator) || !equals(obj) || !(obj instanceof FCDUTF16CollationIterator)) {
            return false;
        }
        FCDUTF16CollationIterator fCDUTF16CollationIterator = (FCDUTF16CollationIterator) obj;
        int i = this.checkDir;
        if (i != fCDUTF16CollationIterator.checkDir) {
            return false;
        }
        if (i == 0) {
            if ((this.seq == this.rawSeq) != (fCDUTF16CollationIterator.seq == fCDUTF16CollationIterator.rawSeq)) {
                return false;
            }
        }
        if (this.checkDir != 0 || this.seq == this.rawSeq) {
            if (this.pos - 0 == fCDUTF16CollationIterator.pos - 0) {
                return true;
            }
            return false;
        } else if (this.segmentStart - 0 == fCDUTF16CollationIterator.segmentStart - 0 && this.pos - this.start == fCDUTF16CollationIterator.pos - fCDUTF16CollationIterator.start) {
            return true;
        } else {
            return false;
        }
    }

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public void resetToOffset(int i) {
        reset();
        this.seq = this.rawSeq;
        int i2 = i + 0;
        this.pos = i2;
        this.segmentStart = i2;
        this.start = i2;
        this.limit = this.rawLimit;
        this.checkDir = 1;
    }

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int getOffset() {
        int i;
        if (this.checkDir != 0 || this.seq == this.rawSeq) {
            i = this.pos;
        } else if (this.pos == this.start) {
            i = this.segmentStart;
        } else {
            i = this.segmentLimit;
        }
        return i + 0;
    }

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator
    public void setText(boolean z, CharSequence charSequence, int i) {
        super.setText(z, charSequence, i);
        this.rawSeq = charSequence;
        this.segmentStart = i;
        int length = charSequence.length();
        this.limit = length;
        this.rawLimit = length;
        this.checkDir = 1;
    }

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int nextCodePoint() {
        char charAt;
        while (true) {
            int i = this.checkDir;
            if (i <= 0) {
                if (i == 0 && this.pos != this.limit) {
                    CharSequence charSequence = this.seq;
                    int i2 = this.pos;
                    this.pos = i2 + 1;
                    charAt = charSequence.charAt(i2);
                    break;
                }
                switchToForward();
            } else if (this.pos == this.limit) {
                return -1;
            } else {
                CharSequence charSequence2 = this.seq;
                int i3 = this.pos;
                this.pos = i3 + 1;
                charAt = charSequence2.charAt(i3);
                if (CollationFCD.hasTccc(charAt) && (CollationFCD.maybeTibetanCompositeVowel(charAt) || (this.pos != this.limit && CollationFCD.hasLccc(this.seq.charAt(this.pos))))) {
                    this.pos--;
                    nextSegment();
                    CharSequence charSequence3 = this.seq;
                    int i4 = this.pos;
                    this.pos = i4 + 1;
                    charAt = charSequence3.charAt(i4);
                }
            }
        }
        if (Character.isHighSurrogate(charAt) && this.pos != this.limit) {
            char charAt2 = this.seq.charAt(this.pos);
            if (Character.isLowSurrogate(charAt2)) {
                this.pos++;
                return Character.toCodePoint(charAt, charAt2);
            }
        }
        return charAt;
    }

    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int previousCodePoint() {
        char charAt;
        while (true) {
            int i = this.checkDir;
            if (i >= 0) {
                if (i == 0 && this.pos != this.start) {
                    CharSequence charSequence = this.seq;
                    int i2 = this.pos - 1;
                    this.pos = i2;
                    charAt = charSequence.charAt(i2);
                    break;
                }
                switchToBackward();
            } else if (this.pos == this.start) {
                return -1;
            } else {
                CharSequence charSequence2 = this.seq;
                int i3 = this.pos - 1;
                this.pos = i3;
                charAt = charSequence2.charAt(i3);
                if (CollationFCD.hasLccc(charAt) && (CollationFCD.maybeTibetanCompositeVowel(charAt) || (this.pos != this.start && CollationFCD.hasTccc(this.seq.charAt(this.pos - 1))))) {
                    this.pos++;
                    previousSegment();
                    CharSequence charSequence3 = this.seq;
                    int i4 = this.pos - 1;
                    this.pos = i4;
                    charAt = charSequence3.charAt(i4);
                }
            }
        }
        if (Character.isLowSurrogate(charAt) && this.pos != this.start) {
            char charAt2 = this.seq.charAt(this.pos - 1);
            if (Character.isHighSurrogate(charAt2)) {
                this.pos--;
                return Character.toCodePoint(charAt2, charAt);
            }
        }
        return charAt;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public long handleNextCE32() {
        char charAt;
        while (true) {
            int i = this.checkDir;
            if (i <= 0) {
                if (i == 0 && this.pos != this.limit) {
                    CharSequence charSequence = this.seq;
                    int i2 = this.pos;
                    this.pos = i2 + 1;
                    charAt = charSequence.charAt(i2);
                    break;
                }
                switchToForward();
            } else if (this.pos == this.limit) {
                return -4294967104L;
            } else {
                CharSequence charSequence2 = this.seq;
                int i3 = this.pos;
                this.pos = i3 + 1;
                charAt = charSequence2.charAt(i3);
                if (CollationFCD.hasTccc(charAt) && (CollationFCD.maybeTibetanCompositeVowel(charAt) || (this.pos != this.limit && CollationFCD.hasLccc(this.seq.charAt(this.pos))))) {
                    this.pos--;
                    nextSegment();
                    CharSequence charSequence3 = this.seq;
                    int i4 = this.pos;
                    this.pos = i4 + 1;
                    charAt = charSequence3.charAt(i4);
                }
            }
        }
        return makeCodePointAndCE32Pair(charAt, this.trie.getFromU16SingleLead(charAt));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public void forwardNumCodePoints(int i) {
        while (i > 0 && nextCodePoint() >= 0) {
            i--;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.UTF16CollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public void backwardNumCodePoints(int i) {
        while (i > 0 && previousCodePoint() >= 0) {
            i--;
        }
    }

    private void switchToForward() {
        if (this.checkDir < 0) {
            int i = this.pos;
            this.segmentStart = i;
            this.start = i;
            if (this.pos == this.segmentLimit) {
                this.limit = this.rawLimit;
                this.checkDir = 1;
                return;
            }
            this.checkDir = 0;
            return;
        }
        CharSequence charSequence = this.seq;
        CharSequence charSequence2 = this.rawSeq;
        if (charSequence != charSequence2) {
            this.seq = charSequence2;
            int i2 = this.segmentLimit;
            this.segmentStart = i2;
            this.start = i2;
            this.pos = i2;
        }
        this.limit = this.rawLimit;
        this.checkDir = 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002e, code lost:
        if (r4 != r7.rawLimit) goto L_0x0031;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
        r0 = java.lang.Character.codePointAt(r7.seq, r4);
        r2 = java.lang.Character.charCount(r0) + r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0044, code lost:
        if (r7.nfcImpl.getFCD16(r0) > 255) goto L_0x0050;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0046, code lost:
        normalize(r7.pos, r4);
        r7.pos = r7.start;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0050, code lost:
        r4 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005d, code lost:
        r7.segmentLimit = r4;
        r7.limit = r4;
     */
    private void nextSegment() {
        int i = this.pos;
        int i2 = 0;
        while (true) {
            int codePointAt = Character.codePointAt(this.seq, i);
            int charCount = Character.charCount(codePointAt) + i;
            int fcd16 = this.nfcImpl.getFCD16(codePointAt);
            int i3 = fcd16 >> 8;
            if (i3 == 0 && i != this.pos) {
                this.segmentLimit = i;
                this.limit = i;
                break;
            } else if (i3 == 0 || (i2 <= i3 && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                i2 = fcd16 & 255;
                if (charCount == this.rawLimit || i2 == 0) {
                    break;
                }
                i = charCount;
            }
        }
        this.checkDir = 0;
    }

    private void switchToBackward() {
        if (this.checkDir > 0) {
            int i = this.pos;
            this.segmentLimit = i;
            this.limit = i;
            if (this.pos == this.segmentStart) {
                this.start = 0;
                this.checkDir = -1;
                return;
            }
            this.checkDir = 0;
            return;
        }
        CharSequence charSequence = this.seq;
        CharSequence charSequence2 = this.rawSeq;
        if (charSequence != charSequence2) {
            this.seq = charSequence2;
            int i2 = this.segmentStart;
            this.segmentLimit = i2;
            this.limit = i2;
            this.pos = i2;
        }
        this.start = 0;
        this.checkDir = -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0031, code lost:
        if (r3 <= 255) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0033, code lost:
        if (r4 != 0) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0036, code lost:
        r0 = java.lang.Character.codePointBefore(r7.seq, r4);
        r2 = r4 - java.lang.Character.charCount(r0);
        r3 = r7.nfcImpl.getFCD16(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0048, code lost:
        if (r3 != 0) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
        r4 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004d, code lost:
        normalize(r4, r7.pos);
        r7.pos = r7.limit;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        r7.segmentStart = r4;
        r7.start = r4;
     */
    private void previousSegment() {
        int i = this.pos;
        int i2 = 0;
        while (true) {
            int codePointBefore = Character.codePointBefore(this.seq, i);
            int charCount = i - Character.charCount(codePointBefore);
            int fcd16 = this.nfcImpl.getFCD16(codePointBefore);
            int i3 = fcd16 & 255;
            if (i3 == 0 && i != this.pos) {
                this.segmentStart = i;
                this.start = i;
                break;
            } else if (i3 == 0 || ((i2 == 0 || i3 <= i2) && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                i2 = fcd16 >> 8;
                if (charCount == 0 || i2 == 0) {
                    break;
                }
                i = charCount;
            }
        }
        this.checkDir = 0;
    }

    private void normalize(int i, int i2) {
        if (this.normalized == null) {
            this.normalized = new StringBuilder();
        }
        this.nfcImpl.decompose(this.rawSeq, i, i2, this.normalized, i2 - i);
        this.segmentStart = i;
        this.segmentLimit = i2;
        this.seq = this.normalized;
        this.start = 0;
        this.limit = this.start + this.normalized.length();
    }
}

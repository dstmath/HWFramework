package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;

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

    public FCDUTF16CollationIterator(CollationData d) {
        super(d);
        this.nfcImpl = d.nfcImpl;
    }

    public FCDUTF16CollationIterator(CollationData data, boolean numeric, CharSequence s, int p) {
        super(data, numeric, s, p);
        this.rawSeq = s;
        this.segmentStart = p;
        this.rawLimit = s.length();
        this.nfcImpl = data.nfcImpl;
        this.checkDir = 1;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof CollationIterator) || !equals(other) || !(other instanceof FCDUTF16CollationIterator)) {
            return false;
        }
        FCDUTF16CollationIterator o = (FCDUTF16CollationIterator) other;
        if (this.checkDir != o.checkDir) {
            return false;
        }
        if (this.checkDir == 0) {
            if ((this.seq == this.rawSeq) != (o.seq == o.rawSeq)) {
                return false;
            }
        }
        if (this.checkDir != 0 || this.seq == this.rawSeq) {
            if (this.pos - 0 == o.pos - 0) {
                z = true;
            }
            return z;
        }
        if (this.segmentStart - 0 == o.segmentStart - 0 && this.pos - this.start == o.pos - o.start) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return 42;
    }

    public void resetToOffset(int newOffset) {
        reset();
        this.seq = this.rawSeq;
        int i = 0 + newOffset;
        this.pos = i;
        this.segmentStart = i;
        this.start = i;
        this.limit = this.rawLimit;
        this.checkDir = 1;
    }

    public int getOffset() {
        if (this.checkDir != 0 || this.seq == this.rawSeq) {
            return this.pos + 0;
        }
        if (this.pos == this.start) {
            return this.segmentStart + 0;
        }
        return this.segmentLimit + 0;
    }

    public void setText(boolean numeric, CharSequence s, int p) {
        super.setText(numeric, s, p);
        this.rawSeq = s;
        this.segmentStart = p;
        int length = s.length();
        this.limit = length;
        this.rawLimit = length;
        this.checkDir = 1;
    }

    public int nextCodePoint() {
        char c;
        while (true) {
            if (this.checkDir <= 0) {
                if (this.checkDir == 0 && this.pos != this.limit) {
                    CharSequence charSequence = this.seq;
                    int i = this.pos;
                    this.pos = i + 1;
                    c = charSequence.charAt(i);
                    break;
                }
                switchToForward();
            } else if (this.pos == this.limit) {
                return -1;
            } else {
                CharSequence charSequence2 = this.seq;
                int i2 = this.pos;
                this.pos = i2 + 1;
                c = charSequence2.charAt(i2);
                if (CollationFCD.hasTccc(c) && (CollationFCD.maybeTibetanCompositeVowel(c) || (this.pos != this.limit && CollationFCD.hasLccc(this.seq.charAt(this.pos))))) {
                    this.pos--;
                    nextSegment();
                    CharSequence charSequence3 = this.seq;
                    int i3 = this.pos;
                    this.pos = i3 + 1;
                    c = charSequence3.charAt(i3);
                }
            }
        }
        if (Character.isHighSurrogate(c) && this.pos != this.limit) {
            char charAt = this.seq.charAt(this.pos);
            char trail = charAt;
            if (Character.isLowSurrogate(charAt)) {
                this.pos++;
                return Character.toCodePoint(c, trail);
            }
        }
        return c;
    }

    public int previousCodePoint() {
        char c;
        while (true) {
            if (this.checkDir >= 0) {
                if (this.checkDir == 0 && this.pos != this.start) {
                    CharSequence charSequence = this.seq;
                    int i = this.pos - 1;
                    this.pos = i;
                    c = charSequence.charAt(i);
                    break;
                }
                switchToBackward();
            } else if (this.pos == this.start) {
                return -1;
            } else {
                CharSequence charSequence2 = this.seq;
                int i2 = this.pos - 1;
                this.pos = i2;
                c = charSequence2.charAt(i2);
                if (CollationFCD.hasLccc(c) && (CollationFCD.maybeTibetanCompositeVowel(c) || (this.pos != this.start && CollationFCD.hasTccc(this.seq.charAt(this.pos - 1))))) {
                    this.pos++;
                    previousSegment();
                    CharSequence charSequence3 = this.seq;
                    int i3 = this.pos - 1;
                    this.pos = i3;
                    c = charSequence3.charAt(i3);
                }
            }
        }
        if (Character.isLowSurrogate(c) && this.pos != this.start) {
            char charAt = this.seq.charAt(this.pos - 1);
            char lead = charAt;
            if (Character.isHighSurrogate(charAt)) {
                this.pos--;
                return Character.toCodePoint(lead, c);
            }
        }
        return c;
    }

    /* access modifiers changed from: protected */
    public long handleNextCE32() {
        char c;
        while (true) {
            if (this.checkDir <= 0) {
                if (this.checkDir == 0 && this.pos != this.limit) {
                    CharSequence charSequence = this.seq;
                    int i = this.pos;
                    this.pos = i + 1;
                    c = charSequence.charAt(i);
                    break;
                }
                switchToForward();
            } else if (this.pos == this.limit) {
                return -4294967104L;
            } else {
                CharSequence charSequence2 = this.seq;
                int i2 = this.pos;
                this.pos = i2 + 1;
                c = charSequence2.charAt(i2);
                if (CollationFCD.hasTccc(c) && (CollationFCD.maybeTibetanCompositeVowel(c) || (this.pos != this.limit && CollationFCD.hasLccc(this.seq.charAt(this.pos))))) {
                    this.pos--;
                    nextSegment();
                    CharSequence charSequence3 = this.seq;
                    int i3 = this.pos;
                    this.pos = i3 + 1;
                    c = charSequence3.charAt(i3);
                }
            }
        }
        return makeCodePointAndCE32Pair(c, this.trie.getFromU16SingleLead(c));
    }

    /* access modifiers changed from: protected */
    public void forwardNumCodePoints(int num) {
        while (num > 0 && nextCodePoint() >= 0) {
            num--;
        }
    }

    /* access modifiers changed from: protected */
    public void backwardNumCodePoints(int num) {
        while (num > 0 && previousCodePoint() >= 0) {
            num--;
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
        if (this.seq != this.rawSeq) {
            this.seq = this.rawSeq;
            int i2 = this.segmentLimit;
            this.segmentStart = i2;
            this.start = i2;
            this.pos = i2;
        }
        this.limit = this.rawLimit;
        this.checkDir = 1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002f, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0032, code lost:
        if (r2 != r9.rawLimit) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0035, code lost:
        r4 = java.lang.Character.codePointAt(r9.seq, r2);
        r2 = r2 + java.lang.Character.charCount(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0048, code lost:
        if (r9.nfcImpl.getFCD16(r4) > 255) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x004a, code lost:
        normalize(r9.pos, r3);
        r9.pos = r9.start;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005e, code lost:
        r9.segmentLimit = r2;
        r9.limit = r2;
     */
    private void nextSegment() {
        int p = this.pos;
        int prevCC = 0;
        while (true) {
            int q = p;
            int c = Character.codePointAt(this.seq, p);
            p += Character.charCount(c);
            int fcd16 = this.nfcImpl.getFCD16(c);
            int leadCC = fcd16 >> 8;
            if (leadCC != 0 || q == this.pos) {
                if (leadCC == 0 || (prevCC <= leadCC && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                    prevCC = fcd16 & 255;
                    if (p != this.rawLimit) {
                        if (prevCC == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                this.segmentLimit = q;
                this.limit = q;
                break;
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
        if (this.seq != this.rawSeq) {
            this.seq = this.rawSeq;
            int i2 = this.segmentStart;
            this.segmentLimit = i2;
            this.limit = i2;
            this.pos = i2;
        }
        this.start = 0;
        this.checkDir = -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005f, code lost:
        r8.segmentStart = r2;
        r8.start = r2;
     */
    private void previousSegment() {
        int fcd16;
        int q;
        int fcd162;
        int p = this.pos;
        int nextCC = 0;
        while (true) {
            int q2 = p;
            int c = Character.codePointBefore(this.seq, p);
            p -= Character.charCount(c);
            fcd16 = this.nfcImpl.getFCD16(c);
            int trailCC = fcd16 & 255;
            if (trailCC != 0 || q2 == this.pos) {
                if (trailCC == 0 || ((nextCC == 0 || trailCC <= nextCC) && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                    nextCC = fcd16 >> 8;
                    if (p != 0) {
                        if (nextCC == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else {
                this.segmentStart = q2;
                this.start = q2;
                break;
            }
        }
        do {
            q = p;
            if (fcd16 <= 255 || p == 0) {
                normalize(q, this.pos);
                this.pos = this.limit;
            } else {
                int c2 = Character.codePointBefore(this.seq, p);
                p -= Character.charCount(c2);
                fcd162 = this.nfcImpl.getFCD16(c2);
                fcd16 = fcd162;
            }
        } while (fcd162 != 0);
        normalize(q, this.pos);
        this.pos = this.limit;
        this.checkDir = 0;
    }

    private void normalize(int from, int to) {
        if (this.normalized == null) {
            this.normalized = new StringBuilder();
        }
        this.nfcImpl.decompose(this.rawSeq, from, to, this.normalized, to - from);
        this.segmentStart = from;
        this.segmentLimit = to;
        this.seq = this.normalized;
        this.start = 0;
        this.limit = this.start + this.normalized.length();
    }
}

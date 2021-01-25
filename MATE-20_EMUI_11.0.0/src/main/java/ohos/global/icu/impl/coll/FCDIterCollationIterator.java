package ohos.global.icu.impl.coll;

import ohos.global.icu.impl.Normalizer2Impl;
import ohos.global.icu.text.UCharacterIterator;

public final class FCDIterCollationIterator extends IterCollationIterator {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private int limit;
    private final Normalizer2Impl nfcImpl;
    private StringBuilder normalized;
    private int pos;
    private StringBuilder s;
    private int start;
    private State state = State.ITER_CHECK_FWD;

    /* access modifiers changed from: private */
    public enum State {
        ITER_CHECK_FWD,
        ITER_CHECK_BWD,
        ITER_IN_FCD_SEGMENT,
        IN_NORM_ITER_AT_LIMIT,
        IN_NORM_ITER_AT_START
    }

    public FCDIterCollationIterator(CollationData collationData, boolean z, UCharacterIterator uCharacterIterator, int i) {
        super(collationData, z, uCharacterIterator);
        this.start = i;
        this.nfcImpl = collationData.nfcImpl;
    }

    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public void resetToOffset(int i) {
        super.resetToOffset(i);
        this.start = i;
        this.state = State.ITER_CHECK_FWD;
    }

    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int getOffset() {
        if (this.state.compareTo(State.ITER_CHECK_BWD) <= 0) {
            return this.iter.getIndex();
        }
        if (this.state == State.ITER_IN_FCD_SEGMENT) {
            return this.pos;
        }
        if (this.pos == 0) {
            return this.start;
        }
        return this.limit;
    }

    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int nextCodePoint() {
        int next;
        while (true) {
            if (this.state == State.ITER_CHECK_FWD) {
                next = this.iter.next();
                if (next < 0) {
                    return next;
                }
                if (!CollationFCD.hasTccc(next) || (!CollationFCD.maybeTibetanCompositeVowel(next) && !CollationFCD.hasLccc(this.iter.current()))) {
                    break;
                }
                this.iter.previous();
                if (!nextSegment()) {
                    return -1;
                }
            } else if (this.state == State.ITER_IN_FCD_SEGMENT && this.pos != this.limit) {
                int nextCodePoint = this.iter.nextCodePoint();
                this.pos += Character.charCount(nextCodePoint);
                return nextCodePoint;
            } else if (this.state.compareTo(State.IN_NORM_ITER_AT_LIMIT) < 0 || this.pos == this.normalized.length()) {
                switchToForward();
            } else {
                int codePointAt = this.normalized.codePointAt(this.pos);
                this.pos += Character.charCount(codePointAt);
                return codePointAt;
            }
        }
        if (isLeadSurrogate(next)) {
            int next2 = this.iter.next();
            if (isTrailSurrogate(next2)) {
                return Character.toCodePoint((char) next, (char) next2);
            }
            if (next2 >= 0) {
                this.iter.previous();
            }
        }
        return next;
    }

    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public int previousCodePoint() {
        int i;
        int previous;
        int i2;
        while (true) {
            if (this.state == State.ITER_CHECK_BWD) {
                previous = this.iter.previous();
                if (previous < 0) {
                    this.pos = 0;
                    this.start = 0;
                    this.state = State.ITER_IN_FCD_SEGMENT;
                    return -1;
                } else if (!CollationFCD.hasLccc(previous)) {
                    break;
                } else {
                    if (!CollationFCD.maybeTibetanCompositeVowel(previous)) {
                        i2 = this.iter.previous();
                        if (!CollationFCD.hasTccc(i2)) {
                            if (isTrailSurrogate(previous)) {
                                if (i2 < 0) {
                                    i2 = this.iter.previous();
                                }
                                if (isLeadSurrogate(i2)) {
                                    return Character.toCodePoint((char) i2, (char) previous);
                                }
                            }
                            if (i2 >= 0) {
                                this.iter.next();
                            }
                        }
                    } else {
                        i2 = -1;
                    }
                    this.iter.next();
                    if (i2 >= 0) {
                        this.iter.next();
                    }
                    if (!previousSegment()) {
                        return -1;
                    }
                }
            } else if (this.state == State.ITER_IN_FCD_SEGMENT && this.pos != this.start) {
                int previousCodePoint = this.iter.previousCodePoint();
                this.pos -= Character.charCount(previousCodePoint);
                return previousCodePoint;
            } else if (this.state.compareTo(State.IN_NORM_ITER_AT_LIMIT) < 0 || (i = this.pos) == 0) {
                switchToBackward();
            } else {
                int codePointBefore = this.normalized.codePointBefore(i);
                this.pos -= Character.charCount(codePointBefore);
                return codePointBefore;
            }
        }
        return previous;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public long handleNextCE32() {
        int i;
        while (true) {
            if (this.state != State.ITER_CHECK_FWD) {
                if (this.state != State.ITER_IN_FCD_SEGMENT || this.pos == this.limit) {
                    if (this.state.compareTo(State.IN_NORM_ITER_AT_LIMIT) >= 0 && this.pos != this.normalized.length()) {
                        StringBuilder sb = this.normalized;
                        int i2 = this.pos;
                        this.pos = i2 + 1;
                        i = sb.charAt(i2);
                        break;
                    }
                    switchToForward();
                } else {
                    i = this.iter.next();
                    this.pos++;
                    break;
                }
            } else {
                i = this.iter.next();
                if (i >= 0) {
                    if (!CollationFCD.hasTccc(i) || (!CollationFCD.maybeTibetanCompositeVowel(i) && !CollationFCD.hasLccc(this.iter.current()))) {
                        break;
                    }
                    this.iter.previous();
                    if (!nextSegment()) {
                        return 192;
                    }
                } else {
                    return -4294967104L;
                }
            }
        }
        return makeCodePointAndCE32Pair(i, this.trie.getFromU16SingleLead((char) i));
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public char handleGetTrailSurrogate() {
        if (this.state.compareTo(State.ITER_IN_FCD_SEGMENT) <= 0) {
            int next = this.iter.next();
            if (isTrailSurrogate(next)) {
                if (this.state == State.ITER_IN_FCD_SEGMENT) {
                    this.pos++;
                }
            } else if (next >= 0) {
                this.iter.previous();
            }
            return (char) next;
        }
        char charAt = this.normalized.charAt(this.pos);
        if (Character.isLowSurrogate(charAt)) {
            this.pos++;
        }
        return charAt;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public void forwardNumCodePoints(int i) {
        while (i > 0 && nextCodePoint() >= 0) {
            i--;
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.global.icu.impl.coll.IterCollationIterator, ohos.global.icu.impl.coll.CollationIterator
    public void backwardNumCodePoints(int i) {
        while (i > 0 && previousCodePoint() >= 0) {
            i--;
        }
    }

    private void switchToForward() {
        if (this.state == State.ITER_CHECK_BWD) {
            int index = this.iter.getIndex();
            this.pos = index;
            this.start = index;
            if (this.pos == this.limit) {
                this.state = State.ITER_CHECK_FWD;
            } else {
                this.state = State.ITER_IN_FCD_SEGMENT;
            }
        } else {
            if (this.state != State.ITER_IN_FCD_SEGMENT) {
                if (this.state == State.IN_NORM_ITER_AT_START) {
                    this.iter.moveIndex(this.limit - this.start);
                }
                this.start = this.limit;
            }
            this.state = State.ITER_CHECK_FWD;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004a, code lost:
        r0 = r7.iter.nextCodePoint();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0050, code lost:
        if (r0 >= 0) goto L_0x0053;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x005b, code lost:
        if (r7.nfcImpl.getFCD16(r0) > 255) goto L_0x007b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x005d, code lost:
        r7.iter.previousCodePoint();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0062, code lost:
        normalize(r7.s);
        r0 = r7.pos;
        r7.start = r0;
        r7.limit = r0 + r7.s.length();
        r7.state = ohos.global.icu.impl.coll.FCDIterCollationIterator.State.IN_NORM_ITER_AT_LIMIT;
        r7.pos = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007a, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007b, code lost:
        r7.s.appendCodePoint(r0);
     */
    private boolean nextSegment() {
        this.pos = this.iter.getIndex();
        StringBuilder sb = this.s;
        if (sb == null) {
            this.s = new StringBuilder();
        } else {
            sb.setLength(0);
        }
        int i = 0;
        while (true) {
            int nextCodePoint = this.iter.nextCodePoint();
            if (nextCodePoint < 0) {
                break;
            }
            int fcd16 = this.nfcImpl.getFCD16(nextCodePoint);
            int i2 = fcd16 >> 8;
            if (i2 == 0 && this.s.length() != 0) {
                this.iter.previousCodePoint();
                break;
            }
            this.s.appendCodePoint(nextCodePoint);
            if (i2 == 0 || (i <= i2 && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                i = fcd16 & 255;
                if (i == 0) {
                    break;
                }
            }
        }
        this.limit = this.pos + this.s.length();
        this.iter.moveIndex(-this.s.length());
        this.state = State.ITER_IN_FCD_SEGMENT;
        return true;
    }

    private void switchToBackward() {
        if (this.state == State.ITER_CHECK_FWD) {
            int index = this.iter.getIndex();
            this.pos = index;
            this.limit = index;
            if (this.pos == this.start) {
                this.state = State.ITER_CHECK_BWD;
            } else {
                this.state = State.ITER_IN_FCD_SEGMENT;
            }
        } else {
            if (this.state != State.ITER_IN_FCD_SEGMENT) {
                if (this.state == State.IN_NORM_ITER_AT_LIMIT) {
                    this.iter.moveIndex(this.start - this.limit);
                }
                this.limit = this.start;
            }
            this.state = State.ITER_CHECK_BWD;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004e, code lost:
        if (r3 <= 255) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0050, code lost:
        r0 = r6.iter.previousCodePoint();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0056, code lost:
        if (r0 >= 0) goto L_0x0059;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0059, code lost:
        r3 = r6.nfcImpl.getFCD16(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005f, code lost:
        if (r3 != 0) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0061, code lost:
        r6.iter.nextCodePoint();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0067, code lost:
        r6.s.appendCodePoint(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x006d, code lost:
        r6.s.reverse();
        normalize(r6.s);
        r0 = r6.pos;
        r6.limit = r0;
        r6.start = r0 - r6.s.length();
        r6.state = ohos.global.icu.impl.coll.FCDIterCollationIterator.State.IN_NORM_ITER_AT_START;
        r6.pos = r6.normalized.length();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0090, code lost:
        return true;
     */
    private boolean previousSegment() {
        this.pos = this.iter.getIndex();
        StringBuilder sb = this.s;
        int i = 0;
        if (sb == null) {
            this.s = new StringBuilder();
        } else {
            sb.setLength(0);
        }
        while (true) {
            int previousCodePoint = this.iter.previousCodePoint();
            if (previousCodePoint < 0) {
                break;
            }
            int fcd16 = this.nfcImpl.getFCD16(previousCodePoint);
            int i2 = fcd16 & 255;
            if (i2 == 0 && this.s.length() != 0) {
                this.iter.nextCodePoint();
                break;
            }
            this.s.appendCodePoint(previousCodePoint);
            if (i2 == 0 || ((i == 0 || i2 <= i) && !CollationFCD.isFCD16OfTibetanCompositeVowel(fcd16))) {
                i = fcd16 >> 8;
                if (i == 0) {
                    break;
                }
            }
        }
        this.start = this.pos - this.s.length();
        this.iter.moveIndex(this.s.length());
        this.state = State.ITER_IN_FCD_SEGMENT;
        return true;
    }

    private void normalize(CharSequence charSequence) {
        if (this.normalized == null) {
            this.normalized = new StringBuilder();
        }
        this.nfcImpl.decompose(charSequence, this.normalized);
    }
}

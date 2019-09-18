package android.icu.text;

import java.nio.BufferOverflowException;
import java.util.Arrays;

public final class Edits {
    private static final int LENGTH_IN_1TRAIL = 61;
    private static final int LENGTH_IN_2TRAIL = 62;
    private static final int MAX_SHORT_CHANGE = 28671;
    private static final int MAX_SHORT_CHANGE_NEW_LENGTH = 7;
    private static final int MAX_SHORT_CHANGE_OLD_LENGTH = 6;
    private static final int MAX_UNCHANGED = 4095;
    private static final int MAX_UNCHANGED_LENGTH = 4096;
    private static final int SHORT_CHANGE_NUM_MASK = 511;
    private static final int STACK_CAPACITY = 100;
    private char[] array = new char[100];
    private int delta;
    private int length;
    private int numChanges;

    public static final class Iterator {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final char[] array;
        private boolean changed;
        private final boolean coarse;
        private int destIndex;
        private int dir;
        private int index;
        private final int length;
        private int newLength_;
        private int oldLength_;
        private final boolean onlyChanges_;
        private int remaining;
        private int replIndex;
        private int srcIndex;

        static {
            Class<Edits> cls = Edits.class;
        }

        private Iterator(char[] a, int len, boolean oc, boolean crs) {
            this.array = a;
            this.length = len;
            this.onlyChanges_ = oc;
            this.coarse = crs;
        }

        private int readLength(int head) {
            if (head < 61) {
                return head;
            }
            if (head < 62) {
                char[] cArr = this.array;
                int i = this.index;
                this.index = i + 1;
                return cArr[i] & 32767;
            }
            int len = ((head & 1) << 30) | ((this.array[this.index] & 32767) << 15) | (this.array[this.index + 1] & 32767);
            this.index += 2;
            return len;
        }

        private void updateNextIndexes() {
            this.srcIndex += this.oldLength_;
            if (this.changed) {
                this.replIndex += this.newLength_;
            }
            this.destIndex += this.newLength_;
        }

        private void updatePreviousIndexes() {
            this.srcIndex -= this.oldLength_;
            if (this.changed) {
                this.replIndex -= this.newLength_;
            }
            this.destIndex -= this.newLength_;
        }

        private boolean noNext() {
            this.dir = 0;
            this.changed = false;
            this.newLength_ = 0;
            this.oldLength_ = 0;
            return false;
        }

        public boolean next() {
            return next(this.onlyChanges_);
        }

        /* JADX WARNING: Incorrect type for immutable var: ssa=char, code=int, for r0v4, types: [char, int] */
        private boolean next(boolean onlyChanges) {
            if (this.dir > 0) {
                updateNextIndexes();
            } else if (this.dir >= 0 || this.remaining <= 0) {
                this.dir = 1;
            } else {
                this.index++;
                this.dir = 1;
                return true;
            }
            if (this.remaining >= 1) {
                if (this.remaining > 1) {
                    this.remaining--;
                    return true;
                }
                this.remaining = 0;
            }
            if (this.index >= this.length) {
                return noNext();
            }
            char[] cArr = this.array;
            int i = this.index;
            this.index = i + 1;
            int u = cArr[i];
            if (u <= 4095) {
                this.changed = false;
                this.oldLength_ = u + 1;
                while (this.index < this.length) {
                    char c = this.array[this.index];
                    u = c;
                    if (c > 4095) {
                        break;
                    }
                    this.index++;
                    this.oldLength_ += u + 1;
                }
                this.newLength_ = this.oldLength_;
                if (!onlyChanges) {
                    return true;
                }
                updateNextIndexes();
                if (this.index >= this.length) {
                    return noNext();
                }
                this.index++;
            }
            this.changed = true;
            if (u <= Edits.MAX_SHORT_CHANGE) {
                int oldLen = u >> 12;
                int newLen = (u >> 9) & 7;
                int num = (u & 511) + 1;
                if (this.coarse) {
                    this.oldLength_ = num * oldLen;
                    this.newLength_ = num * newLen;
                } else {
                    this.oldLength_ = oldLen;
                    this.newLength_ = newLen;
                    if (num > 1) {
                        this.remaining = num;
                    }
                    return true;
                }
            } else {
                this.oldLength_ = readLength((u >> 6) & 63);
                this.newLength_ = readLength(u & 63);
                if (!this.coarse) {
                    return true;
                }
            }
            while (this.index < this.length) {
                char c2 = this.array[this.index];
                int u2 = c2;
                if (c2 <= 4095) {
                    break;
                }
                this.index++;
                if (u2 <= Edits.MAX_SHORT_CHANGE) {
                    int num2 = (u2 & 511) + 1;
                    this.oldLength_ += (u2 >> 12) * num2;
                    this.newLength_ += ((u2 >> 9) & 7) * num2;
                } else {
                    this.oldLength_ += readLength((u2 >> 6) & 63);
                    this.newLength_ += readLength(u2 & 63);
                }
            }
            return true;
        }

        private boolean previous() {
            char c;
            int u;
            if (this.dir >= 0) {
                if (this.dir > 0) {
                    if (this.remaining > 0) {
                        this.index--;
                        this.dir = -1;
                        return true;
                    }
                    updateNextIndexes();
                }
                this.dir = -1;
            }
            if (this.remaining > 0) {
                if (this.remaining <= (this.array[this.index] & 511)) {
                    this.remaining++;
                    updatePreviousIndexes();
                    return true;
                }
                this.remaining = 0;
            }
            if (this.index <= 0) {
                return noNext();
            }
            char[] cArr = this.array;
            int i = this.index - 1;
            this.index = i;
            char u2 = cArr[i];
            if (u2 <= 4095) {
                this.changed = false;
                this.oldLength_ = u2 + 1;
                while (this.index > 0) {
                    char c2 = this.array[this.index - 1];
                    int u3 = c2;
                    if (c2 > 4095) {
                        break;
                    }
                    this.index--;
                    this.oldLength_ += u3 + 1;
                }
                this.newLength_ = this.oldLength_;
                updatePreviousIndexes();
                return true;
            }
            this.changed = true;
            if (u2 <= Edits.MAX_SHORT_CHANGE) {
                int oldLen = u2 >> 12;
                int newLen = (u2 >> 9) & 7;
                int num = (u2 & 511) + 1;
                if (this.coarse) {
                    this.oldLength_ = num * oldLen;
                    this.newLength_ = num * newLen;
                } else {
                    this.oldLength_ = oldLen;
                    this.newLength_ = newLen;
                    if (num > 1) {
                        this.remaining = 1;
                    }
                    updatePreviousIndexes();
                    return true;
                }
            } else {
                if (u2 <= 32767) {
                    this.oldLength_ = readLength((u2 >> 6) & 63);
                    this.newLength_ = readLength(u2 & '?');
                } else {
                    do {
                        char[] cArr2 = this.array;
                        int i2 = this.index - 1;
                        this.index = i2;
                        c = cArr2[i2];
                        u = c;
                    } while (c > 32767);
                    int headIndex = this.index;
                    this.index = headIndex + 1;
                    this.oldLength_ = readLength((u >> 6) & 63);
                    this.newLength_ = readLength(u & 63);
                    this.index = headIndex;
                }
                if (this.coarse == 0) {
                    updatePreviousIndexes();
                    return true;
                }
            }
            while (this.index > 0) {
                char c3 = this.array[this.index - 1];
                int u4 = c3;
                if (c3 <= 4095) {
                    break;
                }
                this.index--;
                if (u4 <= Edits.MAX_SHORT_CHANGE) {
                    int num2 = (u4 & 511) + 1;
                    this.oldLength_ += (u4 >> 12) * num2;
                    this.newLength_ += ((u4 >> 9) & 7) * num2;
                } else if (u4 <= 32767) {
                    int headIndex2 = this.index;
                    this.index = headIndex2 + 1;
                    this.oldLength_ += readLength((u4 >> 6) & 63);
                    this.newLength_ += readLength(u4 & 63);
                    this.index = headIndex2;
                }
            }
            updatePreviousIndexes();
            return true;
        }

        public boolean findSourceIndex(int i) {
            return findIndex(i, true) == 0;
        }

        public boolean findDestinationIndex(int i) {
            return findIndex(i, false) == 0;
        }

        private int findIndex(int i, boolean findSource) {
            int spanLength;
            int spanStart;
            int spanLength2;
            int spanStart2;
            if (i < 0) {
                return -1;
            }
            if (findSource) {
                spanStart = this.srcIndex;
                spanLength = this.oldLength_;
            } else {
                spanStart = this.destIndex;
                spanLength = this.newLength_;
            }
            if (i < spanStart) {
                if (i >= spanStart / 2) {
                    while (true) {
                        boolean previous = previous();
                        int spanStart3 = findSource ? this.srcIndex : this.destIndex;
                        if (i >= spanStart3) {
                            return 0;
                        }
                        if (this.remaining > 0) {
                            int spanLength3 = findSource ? this.oldLength_ : this.newLength_;
                            int num = ((this.array[this.index] & 511) + 1) - this.remaining;
                            if (i >= spanStart3 - (num * spanLength3)) {
                                int n = (((spanStart3 - i) - 1) / spanLength3) + 1;
                                this.srcIndex -= this.oldLength_ * n;
                                this.replIndex -= this.newLength_ * n;
                                this.destIndex -= this.newLength_ * n;
                                this.remaining += n;
                                return 0;
                            }
                            this.srcIndex -= this.oldLength_ * num;
                            this.replIndex -= this.newLength_ * num;
                            this.destIndex -= this.newLength_ * num;
                            this.remaining = 0;
                        }
                    }
                } else {
                    this.dir = 0;
                    this.destIndex = 0;
                    this.replIndex = 0;
                    this.srcIndex = 0;
                    this.newLength_ = 0;
                    this.oldLength_ = 0;
                    this.remaining = 0;
                    this.index = 0;
                }
            } else if (i < spanStart + spanLength) {
                return 0;
            }
            while (next(false)) {
                if (findSource) {
                    spanStart2 = this.srcIndex;
                    spanLength2 = this.oldLength_;
                } else {
                    spanStart2 = this.destIndex;
                    spanLength2 = this.newLength_;
                }
                if (i < spanStart2 + spanLength2) {
                    return 0;
                }
                if (this.remaining > 1) {
                    if (i < spanStart2 + (this.remaining * spanLength2)) {
                        int n2 = (i - spanStart2) / spanLength2;
                        this.srcIndex += this.oldLength_ * n2;
                        this.replIndex += this.newLength_ * n2;
                        this.destIndex += this.newLength_ * n2;
                        this.remaining -= n2;
                        return 0;
                    }
                    this.oldLength_ *= this.remaining;
                    this.newLength_ *= this.remaining;
                    this.remaining = 0;
                }
            }
            return 1;
        }

        public int destinationIndexFromSourceIndex(int i) {
            int where = findIndex(i, true);
            if (where < 0) {
                return 0;
            }
            if (where > 0 || i == this.srcIndex) {
                return this.destIndex;
            }
            if (this.changed) {
                return this.destIndex + this.newLength_;
            }
            return this.destIndex + (i - this.srcIndex);
        }

        public int sourceIndexFromDestinationIndex(int i) {
            int where = findIndex(i, false);
            if (where < 0) {
                return 0;
            }
            if (where > 0 || i == this.destIndex) {
                return this.srcIndex;
            }
            if (this.changed) {
                return this.srcIndex + this.oldLength_;
            }
            return this.srcIndex + (i - this.destIndex);
        }

        public boolean hasChange() {
            return this.changed;
        }

        public int oldLength() {
            return this.oldLength_;
        }

        public int newLength() {
            return this.newLength_;
        }

        public int sourceIndex() {
            return this.srcIndex;
        }

        public int replacementIndex() {
            return this.replIndex;
        }

        public int destinationIndex() {
            return this.destIndex;
        }
    }

    public void reset() {
        this.numChanges = 0;
        this.delta = 0;
        this.length = 0;
    }

    private void setLastUnit(int last) {
        this.array[this.length - 1] = (char) last;
    }

    private int lastUnit() {
        return this.length > 0 ? this.array[this.length - 1] : DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
    }

    public void addUnchanged(int unchangedLength) {
        if (unchangedLength >= 0) {
            int last = lastUnit();
            if (last < 4095) {
                int remaining = 4095 - last;
                if (remaining >= unchangedLength) {
                    setLastUnit(last + unchangedLength);
                    return;
                } else {
                    setLastUnit(4095);
                    unchangedLength -= remaining;
                }
            }
            while (unchangedLength >= 4096) {
                append(4095);
                unchangedLength -= 4096;
            }
            if (unchangedLength > 0) {
                append(unchangedLength - 1);
            }
            return;
        }
        throw new IllegalArgumentException("addUnchanged(" + unchangedLength + "): length must not be negative");
    }

    public void addReplace(int oldLength, int newLength) {
        int head;
        int head2;
        int head3;
        if (oldLength < 0 || newLength < 0) {
            throw new IllegalArgumentException("addReplace(" + oldLength + ", " + newLength + "): both lengths must be non-negative");
        } else if (oldLength != 0 || newLength != 0) {
            this.numChanges++;
            int newDelta = newLength - oldLength;
            if (newDelta != 0) {
                if ((newDelta <= 0 || this.delta < 0 || newDelta <= Integer.MAX_VALUE - this.delta) && (newDelta >= 0 || this.delta >= 0 || newDelta >= Integer.MIN_VALUE - this.delta)) {
                    this.delta += newDelta;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
            if (oldLength <= 0 || oldLength > 6 || newLength > 7) {
                if (oldLength < 61 && newLength < 61) {
                    append((oldLength << 6) | 28672 | newLength);
                } else if (this.array.length - this.length >= 5 || growArray()) {
                    int limit = this.length + 1;
                    if (oldLength < 61) {
                        head = (oldLength << 6) | 28672;
                    } else if (oldLength <= 32767) {
                        head = 28672 | 3904;
                        this.array[limit] = (char) (32768 | oldLength);
                        limit++;
                    } else {
                        head = (((oldLength >> 30) + 62) << 6) | 28672;
                        int limit2 = limit + 1;
                        this.array[limit] = (char) ((oldLength >> 15) | 32768);
                        limit = limit2 + 1;
                        this.array[limit2] = (char) (32768 | oldLength);
                    }
                    if (newLength < 61) {
                        head3 = head | newLength;
                    } else if (newLength <= 32767) {
                        this.array[limit] = (char) (32768 | newLength);
                        head2 = head | 61;
                        limit++;
                        this.array[this.length] = (char) head2;
                        this.length = limit;
                    } else {
                        head3 = head | (62 + (newLength >> 30));
                        int limit3 = limit + 1;
                        this.array[limit] = (char) ((newLength >> 15) | 32768);
                        limit = limit3 + 1;
                        this.array[limit3] = (char) (32768 | newLength);
                    }
                    head2 = head3;
                    this.array[this.length] = (char) head2;
                    this.length = limit;
                }
                return;
            }
            int u = (oldLength << 12) | (newLength << 9);
            int last = lastUnit();
            if (4095 >= last || last >= MAX_SHORT_CHANGE || (last & -512) != u || (last & 511) >= 511) {
                append(u);
            } else {
                setLastUnit(last + 1);
            }
        }
    }

    private void append(int r) {
        if (this.length < this.array.length || growArray()) {
            char[] cArr = this.array;
            int i = this.length;
            this.length = i + 1;
            cArr[i] = (char) r;
        }
    }

    private boolean growArray() {
        int newCapacity;
        if (this.array.length == 100) {
            newCapacity = 2000;
        } else if (this.array.length == Integer.MAX_VALUE) {
            throw new BufferOverflowException();
        } else if (this.array.length >= 1073741823) {
            newCapacity = Integer.MAX_VALUE;
        } else {
            newCapacity = 2 * this.array.length;
        }
        if (newCapacity - this.array.length >= 5) {
            this.array = Arrays.copyOf(this.array, newCapacity);
            return true;
        }
        throw new BufferOverflowException();
    }

    public int lengthDelta() {
        return this.delta;
    }

    public boolean hasChanges() {
        return this.numChanges != 0;
    }

    public int numberOfChanges() {
        return this.numChanges;
    }

    public Iterator getCoarseChangesIterator() {
        Iterator iterator = new Iterator(this.array, this.length, true, true);
        return iterator;
    }

    public Iterator getCoarseIterator() {
        Iterator iterator = new Iterator(this.array, this.length, false, true);
        return iterator;
    }

    public Iterator getFineChangesIterator() {
        Iterator iterator = new Iterator(this.array, this.length, true, false);
        return iterator;
    }

    public Iterator getFineIterator() {
        Iterator iterator = new Iterator(this.array, this.length, false, false);
        return iterator;
    }

    public Edits mergeAndAppend(Edits ab, Edits bc) {
        Iterator abIter = ab.getFineIterator();
        Iterator bcIter = bc.getFineIterator();
        boolean bcHasNext = true;
        int ab_bLength = 0;
        int bc_bLength = 0;
        int cLength = 0;
        int pending_aLength = 0;
        int aLength = 0;
        boolean abHasNext = true;
        int pending_cLength = 0;
        while (true) {
            if (bc_bLength == 0 && bcHasNext) {
                boolean next = bcIter.next();
                bcHasNext = next;
                if (next) {
                    bc_bLength = bcIter.oldLength();
                    cLength = bcIter.newLength();
                    if (bc_bLength == 0) {
                        if (ab_bLength == 0 || !abIter.hasChange()) {
                            addReplace(pending_aLength, pending_cLength + cLength);
                            pending_cLength = 0;
                            pending_aLength = 0;
                        } else {
                            pending_cLength += cLength;
                        }
                    }
                }
            }
            if (ab_bLength == 0) {
                if (!abHasNext) {
                    break;
                }
                boolean next2 = abIter.next();
                abHasNext = next2;
                if (!next2) {
                    break;
                }
                aLength = abIter.oldLength();
                ab_bLength = abIter.newLength();
                if (ab_bLength == 0) {
                    if (bc_bLength == bcIter.oldLength() || !bcIter.hasChange()) {
                        addReplace(pending_aLength + aLength, pending_cLength);
                        pending_cLength = 0;
                        pending_aLength = 0;
                    } else {
                        pending_aLength += aLength;
                    }
                }
            }
            if (bc_bLength == 0) {
                throw new IllegalArgumentException("The bc input string is shorter than the ab output string.");
            } else if (abIter.hasChange() || bcIter.hasChange()) {
                if (abIter.hasChange() != 0 || !bcIter.hasChange()) {
                    if (!abIter.hasChange() || bcIter.hasChange()) {
                        if (ab_bLength == bc_bLength) {
                            addReplace(pending_aLength + aLength, pending_cLength + cLength);
                            pending_cLength = 0;
                            pending_aLength = 0;
                            bc_bLength = 0;
                            ab_bLength = 0;
                        }
                    } else if (ab_bLength <= bc_bLength) {
                        addReplace(pending_aLength + aLength, pending_cLength + ab_bLength);
                        pending_cLength = 0;
                        pending_aLength = 0;
                        int i = bc_bLength - ab_bLength;
                        bc_bLength = i;
                        cLength = i;
                        ab_bLength = 0;
                    }
                } else if (ab_bLength >= bc_bLength) {
                    addReplace(pending_aLength + bc_bLength, pending_cLength + cLength);
                    pending_cLength = 0;
                    pending_aLength = 0;
                    int i2 = ab_bLength - bc_bLength;
                    ab_bLength = i2;
                    aLength = i2;
                    bc_bLength = 0;
                }
                pending_aLength += aLength;
                pending_cLength += cLength;
                if (ab_bLength < bc_bLength) {
                    bc_bLength -= ab_bLength;
                    ab_bLength = 0;
                    cLength = 0;
                } else {
                    ab_bLength -= bc_bLength;
                    bc_bLength = 0;
                    aLength = 0;
                }
            } else {
                if (!(pending_aLength == 0 && pending_cLength == 0)) {
                    addReplace(pending_aLength, pending_cLength);
                    pending_cLength = 0;
                    pending_aLength = 0;
                }
                int unchangedLength = aLength <= cLength ? aLength : cLength;
                addUnchanged(unchangedLength);
                int i3 = aLength - unchangedLength;
                aLength = i3;
                ab_bLength = i3;
                int i4 = cLength - unchangedLength;
                cLength = i4;
                bc_bLength = i4;
            }
        }
        if (bc_bLength == 0) {
            if (!(pending_aLength == 0 && pending_cLength == 0)) {
                addReplace(pending_aLength, pending_cLength);
            }
            return this;
        }
        throw new IllegalArgumentException("The ab output string is shorter than the bc input string.");
    }
}

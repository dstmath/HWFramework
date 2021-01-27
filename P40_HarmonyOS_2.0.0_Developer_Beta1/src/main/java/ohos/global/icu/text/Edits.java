package ohos.global.icu.text;

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

    public void reset() {
        this.numChanges = 0;
        this.delta = 0;
        this.length = 0;
    }

    private void setLastUnit(int i) {
        this.array[this.length - 1] = (char) i;
    }

    private int lastUnit() {
        int i = this.length;
        return i > 0 ? this.array[i - 1] : DateTimePatternGenerator.MATCH_ALL_FIELDS_LENGTH;
    }

    public void addUnchanged(int i) {
        if (i >= 0) {
            int lastUnit = lastUnit();
            if (lastUnit < MAX_UNCHANGED) {
                int i2 = 4095 - lastUnit;
                if (i2 >= i) {
                    setLastUnit(lastUnit + i);
                    return;
                } else {
                    setLastUnit(MAX_UNCHANGED);
                    i -= i2;
                }
            }
            while (i >= 4096) {
                append(MAX_UNCHANGED);
                i -= 4096;
            }
            if (i > 0) {
                append(i - 1);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("addUnchanged(" + i + "): length must not be negative");
    }

    public void addReplace(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        if (i < 0 || i2 < 0) {
            throw new IllegalArgumentException("addReplace(" + i + ", " + i2 + "): both lengths must be non-negative");
        } else if (i != 0 || i2 != 0) {
            this.numChanges++;
            int i7 = i2 - i;
            if (i7 != 0) {
                if ((i7 <= 0 || (i6 = this.delta) < 0 || i7 <= Integer.MAX_VALUE - i6) && (i7 >= 0 || (i5 = this.delta) >= 0 || i7 >= Integer.MIN_VALUE - i5)) {
                    this.delta += i7;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
            if (i > 0 && i <= 6 && i2 <= 7) {
                int i8 = (i << 12) | (i2 << 9);
                int lastUnit = lastUnit();
                if (MAX_UNCHANGED >= lastUnit || lastUnit >= MAX_SHORT_CHANGE || (lastUnit & -512) != i8 || (lastUnit & SHORT_CHANGE_NUM_MASK) >= SHORT_CHANGE_NUM_MASK) {
                    append(i8);
                } else {
                    setLastUnit(lastUnit + 1);
                }
            } else if (i < LENGTH_IN_1TRAIL && i2 < LENGTH_IN_1TRAIL) {
                append((i << 6) | 28672 | i2);
            } else if (this.array.length - this.length >= 5 || growArray()) {
                int i9 = this.length + 1;
                if (i < LENGTH_IN_1TRAIL) {
                    i3 = (i << 6) | 28672;
                } else if (i <= 32767) {
                    i3 = 32576;
                    this.array[i9] = (char) (i | 32768);
                    i9++;
                } else {
                    i3 = (((i >> 30) + LENGTH_IN_2TRAIL) << 6) | 28672;
                    char[] cArr = this.array;
                    int i10 = i9 + 1;
                    cArr[i9] = (char) ((i >> 15) | 32768);
                    i9 = i10 + 1;
                    cArr[i10] = (char) (i | 32768);
                }
                if (i2 < LENGTH_IN_1TRAIL) {
                    i4 = i3 | i2;
                } else if (i2 <= 32767) {
                    i4 = i3 | LENGTH_IN_1TRAIL;
                    this.array[i9] = (char) (i2 | 32768);
                    i9++;
                } else {
                    i4 = ((i2 >> 30) + LENGTH_IN_2TRAIL) | i3;
                    char[] cArr2 = this.array;
                    int i11 = i9 + 1;
                    cArr2[i9] = (char) ((i2 >> 15) | 32768);
                    i9 = i11 + 1;
                    cArr2[i11] = (char) (i2 | 32768);
                }
                this.array[this.length] = (char) i4;
                this.length = i9;
            }
        }
    }

    private void append(int i) {
        if (this.length < this.array.length || growArray()) {
            char[] cArr = this.array;
            int i2 = this.length;
            this.length = i2 + 1;
            cArr[i2] = (char) i;
        }
    }

    private boolean growArray() {
        char[] cArr = this.array;
        int i = Integer.MAX_VALUE;
        if (cArr.length == 100) {
            i = 2000;
        } else if (cArr.length == Integer.MAX_VALUE) {
            throw new BufferOverflowException();
        } else if (cArr.length < 1073741823) {
            i = cArr.length * 2;
        }
        char[] cArr2 = this.array;
        if (i - cArr2.length >= 5) {
            this.array = Arrays.copyOf(cArr2, i);
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

        private Iterator(char[] cArr, int i, boolean z, boolean z2) {
            this.array = cArr;
            this.length = i;
            this.onlyChanges_ = z;
            this.coarse = z2;
        }

        private int readLength(int i) {
            if (i < Edits.LENGTH_IN_1TRAIL) {
                return i;
            }
            if (i < Edits.LENGTH_IN_2TRAIL) {
                char[] cArr = this.array;
                int i2 = this.index;
                this.index = i2 + 1;
                return cArr[i2] & 32767;
            }
            char[] cArr2 = this.array;
            int i3 = this.index;
            int i4 = ((i & 1) << 30) | ((cArr2[i3] & 32767) << 15) | (cArr2[i3 + 1] & 32767);
            this.index = i3 + 2;
            return i4;
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

        private boolean next(boolean z) {
            char c;
            int i = this.dir;
            if (i > 0) {
                updateNextIndexes();
            } else if (i >= 0 || this.remaining <= 0) {
                this.dir = 1;
            } else {
                this.index++;
                this.dir = 1;
                return true;
            }
            int i2 = this.remaining;
            if (i2 >= 1) {
                if (i2 > 1) {
                    this.remaining = i2 - 1;
                    return true;
                }
                this.remaining = 0;
            }
            int i3 = this.index;
            if (i3 >= this.length) {
                return noNext();
            }
            char[] cArr = this.array;
            this.index = i3 + 1;
            char c2 = cArr[i3];
            if (c2 <= Edits.MAX_UNCHANGED) {
                this.changed = false;
                this.oldLength_ = c2 + 1;
                while (true) {
                    int i4 = this.index;
                    if (i4 >= this.length || (c2 = this.array[i4]) > Edits.MAX_UNCHANGED) {
                        break;
                    }
                    this.index = i4 + 1;
                    this.oldLength_ += c2 + 1;
                }
                this.newLength_ = this.oldLength_;
                if (!z) {
                    return true;
                }
                updateNextIndexes();
                int i5 = this.index;
                if (i5 >= this.length) {
                    return noNext();
                }
                this.index = i5 + 1;
            }
            this.changed = true;
            if (c2 <= Edits.MAX_SHORT_CHANGE) {
                int i6 = c2 >> '\f';
                int i7 = (c2 >> '\t') & 7;
                int i8 = (c2 & 511) + 1;
                if (this.coarse) {
                    this.oldLength_ = i6 * i8;
                    this.newLength_ = i8 * i7;
                } else {
                    this.oldLength_ = i6;
                    this.newLength_ = i7;
                    if (i8 > 1) {
                        this.remaining = i8;
                    }
                    return true;
                }
            } else {
                this.oldLength_ = readLength((c2 >> 6) & 63);
                this.newLength_ = readLength(c2 & '?');
                if (!this.coarse) {
                    return true;
                }
            }
            while (true) {
                int i9 = this.index;
                if (i9 >= this.length || (c = this.array[i9]) <= Edits.MAX_UNCHANGED) {
                    break;
                }
                this.index = i9 + 1;
                if (c <= Edits.MAX_SHORT_CHANGE) {
                    int i10 = (c & 511) + 1;
                    this.oldLength_ += (c >> '\f') * i10;
                    this.newLength_ += ((c >> '\t') & 7) * i10;
                } else {
                    this.oldLength_ += readLength((c >> 6) & 63);
                    this.newLength_ += readLength(c & '?');
                }
            }
            return true;
        }

        private boolean previous() {
            char c;
            char c2;
            char c3;
            int i = this.dir;
            if (i >= 0) {
                if (i > 0) {
                    if (this.remaining > 0) {
                        this.index--;
                        this.dir = -1;
                        return true;
                    }
                    updateNextIndexes();
                }
                this.dir = -1;
            }
            int i2 = this.remaining;
            if (i2 > 0) {
                if (i2 <= (this.array[this.index] & 511)) {
                    this.remaining = i2 + 1;
                    updatePreviousIndexes();
                    return true;
                }
                this.remaining = 0;
            }
            int i3 = this.index;
            if (i3 <= 0) {
                return noNext();
            }
            char[] cArr = this.array;
            int i4 = i3 - 1;
            this.index = i4;
            char c4 = cArr[i4];
            if (c4 <= Edits.MAX_UNCHANGED) {
                this.changed = false;
                this.oldLength_ = c4 + 1;
                while (true) {
                    int i5 = this.index;
                    if (i5 <= 0 || (c3 = this.array[i5 - 1]) > Edits.MAX_UNCHANGED) {
                        break;
                    }
                    this.index = i5 - 1;
                    this.oldLength_ += c3 + 1;
                }
                this.newLength_ = this.oldLength_;
                updatePreviousIndexes();
                return true;
            }
            this.changed = true;
            if (c4 <= Edits.MAX_SHORT_CHANGE) {
                int i6 = c4 >> '\f';
                int i7 = (c4 >> '\t') & 7;
                int i8 = (c4 & 511) + 1;
                if (this.coarse) {
                    this.oldLength_ = i6 * i8;
                    this.newLength_ = i8 * i7;
                } else {
                    this.oldLength_ = i6;
                    this.newLength_ = i7;
                    if (i8 > 1) {
                        this.remaining = 1;
                    }
                    updatePreviousIndexes();
                    return true;
                }
            } else {
                if (c4 <= 32767) {
                    this.oldLength_ = readLength((c4 >> 6) & 63);
                    this.newLength_ = readLength(c4 & '?');
                } else {
                    do {
                        char[] cArr2 = this.array;
                        int i9 = this.index - 1;
                        this.index = i9;
                        c2 = cArr2[i9];
                    } while (c2 > 32767);
                    int i10 = this.index;
                    this.index = i10 + 1;
                    this.oldLength_ = readLength((c2 >> 6) & 63);
                    this.newLength_ = readLength(c2 & '?');
                    this.index = i10;
                }
                if (!this.coarse) {
                    updatePreviousIndexes();
                    return true;
                }
            }
            while (true) {
                int i11 = this.index;
                if (i11 <= 0 || (c = this.array[i11 - 1]) <= Edits.MAX_UNCHANGED) {
                    break;
                }
                this.index = i11 - 1;
                if (c <= Edits.MAX_SHORT_CHANGE) {
                    int i12 = (c & 511) + 1;
                    this.oldLength_ += (c >> '\f') * i12;
                    this.newLength_ += ((c >> '\t') & 7) * i12;
                } else if (c <= 32767) {
                    int i13 = this.index;
                    this.index = i13 + 1;
                    this.oldLength_ += readLength((c >> 6) & 63);
                    this.newLength_ += readLength(c & '?');
                    this.index = i13;
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

        private int findIndex(int i, boolean z) {
            int i2;
            int i3;
            int i4;
            int i5;
            if (i < 0) {
                return -1;
            }
            if (z) {
                i3 = this.srcIndex;
                i2 = this.oldLength_;
            } else {
                i3 = this.destIndex;
                i2 = this.newLength_;
            }
            if (i < i3) {
                if (i >= i3 / 2) {
                    while (true) {
                        previous();
                        int i6 = z ? this.srcIndex : this.destIndex;
                        if (i >= i6) {
                            return 0;
                        }
                        if (this.remaining > 0) {
                            int i7 = z ? this.oldLength_ : this.newLength_;
                            int i8 = this.remaining;
                            int i9 = ((this.array[this.index] & 511) + 1) - i8;
                            if (i >= i6 - (i9 * i7)) {
                                int i10 = (((i6 - i) - 1) / i7) + 1;
                                this.srcIndex -= this.oldLength_ * i10;
                                int i11 = this.replIndex;
                                int i12 = this.newLength_;
                                this.replIndex = i11 - (i10 * i12);
                                this.destIndex -= i12 * i10;
                                this.remaining = i8 + i10;
                                return 0;
                            }
                            this.srcIndex -= this.oldLength_ * i9;
                            int i13 = this.replIndex;
                            int i14 = this.newLength_;
                            this.replIndex = i13 - (i9 * i14);
                            this.destIndex -= i9 * i14;
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
            } else if (i < i3 + i2) {
                return 0;
            }
            while (next(false)) {
                if (z) {
                    i5 = this.srcIndex;
                    i4 = this.oldLength_;
                } else {
                    i5 = this.destIndex;
                    i4 = this.newLength_;
                }
                if (i < i5 + i4) {
                    return 0;
                }
                int i15 = this.remaining;
                if (i15 > 1) {
                    if (i < (i15 * i4) + i5) {
                        int i16 = (i - i5) / i4;
                        this.srcIndex += this.oldLength_ * i16;
                        int i17 = this.replIndex;
                        int i18 = this.newLength_;
                        this.replIndex = i17 + (i16 * i18);
                        this.destIndex += i18 * i16;
                        this.remaining = i15 - i16;
                        return 0;
                    }
                    this.oldLength_ *= i15;
                    this.newLength_ *= i15;
                    this.remaining = 0;
                }
            }
            return 1;
        }

        public int destinationIndexFromSourceIndex(int i) {
            int i2;
            int findIndex = findIndex(i, true);
            if (findIndex < 0) {
                return 0;
            }
            if (findIndex > 0 || i == (i2 = this.srcIndex)) {
                return this.destIndex;
            }
            if (this.changed) {
                return this.destIndex + this.newLength_;
            }
            return this.destIndex + (i - i2);
        }

        public int sourceIndexFromDestinationIndex(int i) {
            int i2;
            int findIndex = findIndex(i, false);
            if (findIndex < 0) {
                return 0;
            }
            if (findIndex > 0 || i == (i2 = this.destIndex)) {
                return this.srcIndex;
            }
            if (this.changed) {
                return this.srcIndex + this.oldLength_;
            }
            return this.srcIndex + (i - i2);
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

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append("{ src[");
            sb.append(this.srcIndex);
            sb.append("..");
            sb.append(this.srcIndex + this.oldLength_);
            if (this.changed) {
                sb.append("] ⇝ dest[");
            } else {
                sb.append("] ≡ dest[");
            }
            sb.append(this.destIndex);
            sb.append("..");
            sb.append(this.destIndex + this.newLength_);
            if (this.changed) {
                sb.append("], repl[");
                sb.append(this.replIndex);
                sb.append("..");
                sb.append(this.replIndex + this.newLength_);
                sb.append("] }");
            } else {
                sb.append("] (no-change) }");
            }
            return sb.toString();
        }
    }

    public Iterator getCoarseChangesIterator() {
        return new Iterator(this.array, this.length, true, true);
    }

    public Iterator getCoarseIterator() {
        return new Iterator(this.array, this.length, false, true);
    }

    public Iterator getFineChangesIterator() {
        return new Iterator(this.array, this.length, true, false);
    }

    public Iterator getFineIterator() {
        return new Iterator(this.array, this.length, false, false);
    }

    public Edits mergeAndAppend(Edits edits, Edits edits2) {
        Iterator fineIterator = edits.getFineIterator();
        Iterator fineIterator2 = edits2.getFineIterator();
        boolean z = true;
        boolean z2 = true;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        while (true) {
            if (i == 0 && z) {
                z = fineIterator2.next();
                if (z) {
                    i = fineIterator2.oldLength();
                    i6 = fineIterator2.newLength();
                    if (i == 0) {
                        if (i2 == 0 || !fineIterator.hasChange()) {
                            addReplace(i3, i4 + i6);
                            i3 = 0;
                            i4 = i3;
                        } else {
                            i4 += i6;
                        }
                    }
                }
            }
            if (i2 == 0) {
                if (!z2 || !(z2 = fineIterator.next())) {
                    break;
                }
                i5 = fineIterator.oldLength();
                i2 = fineIterator.newLength();
                if (i2 == 0) {
                    if (i == fineIterator2.oldLength() || !fineIterator2.hasChange()) {
                        addReplace(i3 + i5, i4);
                        i3 = 0;
                        i4 = i3;
                    } else {
                        i3 += i5;
                    }
                }
            }
            if (i != 0) {
                if (fineIterator.hasChange() || fineIterator2.hasChange()) {
                    if (fineIterator.hasChange() || !fineIterator2.hasChange()) {
                        if (!fineIterator.hasChange() || fineIterator2.hasChange()) {
                            if (i2 == i) {
                                addReplace(i3 + i5, i4 + i6);
                                i = 0;
                                i2 = 0;
                                i3 = 0;
                                i4 = i3;
                            }
                        } else if (i2 <= i) {
                            addReplace(i3 + i5, i4 + i2);
                            i6 = i - i2;
                            i2 = 0;
                            i3 = 0;
                            i4 = 0;
                        }
                    } else if (i2 >= i) {
                        addReplace(i3 + i, i4 + i6);
                        i5 = i2 - i;
                        i = 0;
                        i3 = 0;
                        i4 = 0;
                        i2 = i5;
                    }
                    i3 += i5;
                    i4 += i6;
                    if (i2 < i) {
                        i -= i2;
                        i2 = 0;
                        i6 = 0;
                    } else {
                        i2 -= i;
                        i = 0;
                        i5 = 0;
                    }
                } else {
                    if (!(i3 == 0 && i4 == 0)) {
                        addReplace(i3, i4);
                        i3 = 0;
                        i4 = 0;
                    }
                    int i7 = i5 <= i6 ? i5 : i6;
                    addUnchanged(i7);
                    i5 -= i7;
                    i6 -= i7;
                    i2 = i5;
                }
                i = i6;
            } else {
                throw new IllegalArgumentException("The bc input string is shorter than the ab output string.");
            }
        }
        if (i == 0) {
            if (!(i3 == 0 && i4 == 0)) {
                addReplace(i3, i4);
            }
            return this;
        }
        throw new IllegalArgumentException("The ab output string is shorter than the bc input string.");
    }
}

package android.icu.text;

import dalvik.bytecode.Opcodes;
import java.nio.BufferOverflowException;
import java.util.Arrays;

public final class Edits {
    private static final int LENGTH_IN_1TRAIL = 61;
    private static final int LENGTH_IN_2TRAIL = 62;
    private static final int MAX_SHORT_CHANGE = 28671;
    private static final int MAX_SHORT_CHANGE_LENGTH = 4095;
    private static final int MAX_SHORT_WIDTH = 6;
    private static final int MAX_UNCHANGED = 4095;
    private static final int MAX_UNCHANGED_LENGTH = 4096;
    private static final int STACK_CAPACITY = 100;
    private char[] array = new char[100];
    private int delta;
    private int length;

    public static final class Iterator {
        static final /* synthetic */ boolean -assertionsDisabled = (Iterator.class.desiredAssertionStatus() ^ 1);
        private final char[] array;
        private boolean changed;
        private final boolean coarse;
        private int destIndex;
        private int index;
        private final int length;
        private int newLength_;
        private int oldLength_;
        private final boolean onlyChanges_;
        private int remaining;
        private int replIndex;
        private int srcIndex;

        /* synthetic */ Iterator(char[] a, int len, boolean oc, boolean crs, Iterator -this4) {
            this(a, len, oc, crs);
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
                if (!-assertionsDisabled && this.index >= this.length) {
                    throw new AssertionError();
                } else if (-assertionsDisabled || this.array[this.index] >= 32768) {
                    char[] cArr = this.array;
                    int i = this.index;
                    this.index = i + 1;
                    return cArr[i] & 32767;
                } else {
                    throw new AssertionError();
                }
            } else if (!-assertionsDisabled && this.index + 2 > this.length) {
                throw new AssertionError();
            } else if (!-assertionsDisabled && this.array[this.index] < 32768) {
                throw new AssertionError();
            } else if (-assertionsDisabled || this.array[this.index + 1] >= 32768) {
                int len = (((head & 1) << 30) | ((this.array[this.index] & 32767) << 15)) | (this.array[this.index + 1] & 32767);
                this.index += 2;
                return len;
            } else {
                throw new AssertionError();
            }
        }

        private void updateIndexes() {
            this.srcIndex += this.oldLength_;
            if (this.changed) {
                this.replIndex += this.newLength_;
            }
            this.destIndex += this.newLength_;
        }

        private boolean noNext() {
            this.changed = false;
            this.newLength_ = 0;
            this.oldLength_ = 0;
            return false;
        }

        public boolean next() {
            return next(this.onlyChanges_);
        }

        private boolean next(boolean onlyChanges) {
            updateIndexes();
            if (this.remaining > 0) {
                this.remaining--;
                return true;
            } else if (this.index >= this.length) {
                return noNext();
            } else {
                char[] cArr = this.array;
                int i = this.index;
                this.index = i + 1;
                int u = cArr[i];
                if (u <= Opcodes.OP_IPUT_OBJECT_JUMBO) {
                    this.changed = false;
                    this.oldLength_ = u + 1;
                    while (this.index < this.length) {
                        u = this.array[this.index];
                        if (u > Opcodes.OP_IPUT_OBJECT_JUMBO) {
                            break;
                        }
                        this.index++;
                        this.oldLength_ += u + 1;
                    }
                    this.newLength_ = this.oldLength_;
                    if (!onlyChanges) {
                        return true;
                    }
                    updateIndexes();
                    if (this.index >= this.length) {
                        return noNext();
                    }
                    this.index++;
                }
                this.changed = true;
                if (u <= Edits.MAX_SHORT_CHANGE) {
                    int i2;
                    if (this.coarse) {
                        i2 = ((u & Opcodes.OP_IPUT_OBJECT_JUMBO) + 1) * (u >> 12);
                        this.newLength_ = i2;
                        this.oldLength_ = i2;
                    } else {
                        i2 = u >> 12;
                        this.newLength_ = i2;
                        this.oldLength_ = i2;
                        this.remaining = u & Opcodes.OP_IPUT_OBJECT_JUMBO;
                        return true;
                    }
                } else if (-assertionsDisabled || u <= 32767) {
                    this.oldLength_ = readLength((u >> 6) & 63);
                    this.newLength_ = readLength(u & 63);
                    if (!this.coarse) {
                        return true;
                    }
                } else {
                    throw new AssertionError();
                }
                while (this.index < this.length) {
                    u = this.array[this.index];
                    if (u <= Opcodes.OP_IPUT_OBJECT_JUMBO) {
                        break;
                    }
                    this.index++;
                    if (u <= Edits.MAX_SHORT_CHANGE) {
                        int len = ((u & Opcodes.OP_IPUT_OBJECT_JUMBO) + 1) * (u >> 12);
                        this.oldLength_ += len;
                        this.newLength_ += len;
                    } else if (-assertionsDisabled || u <= 32767) {
                        int oldLen = readLength((u >> 6) & 63);
                        int newLen = readLength(u & 63);
                        this.oldLength_ += oldLen;
                        this.newLength_ += newLen;
                    } else {
                        throw new AssertionError();
                    }
                }
                return true;
            }
        }

        public boolean findSourceIndex(int i) {
            if (i < 0) {
                return false;
            }
            if (i < this.srcIndex) {
                this.destIndex = 0;
                this.replIndex = 0;
                this.srcIndex = 0;
                this.newLength_ = 0;
                this.oldLength_ = 0;
                this.remaining = 0;
                this.index = 0;
            } else if (i < this.srcIndex + this.oldLength_) {
                return true;
            }
            while (next(false)) {
                if (i < this.srcIndex + this.oldLength_) {
                    return true;
                }
                if (this.remaining > 0) {
                    int len = (this.remaining + 1) * this.oldLength_;
                    if (i < this.srcIndex + len) {
                        int n = (i - this.srcIndex) / this.oldLength_;
                        len = n * this.oldLength_;
                        this.srcIndex += len;
                        this.replIndex += len;
                        this.destIndex += len;
                        this.remaining -= n;
                        return true;
                    }
                    this.newLength_ = len;
                    this.oldLength_ = len;
                    this.remaining = 0;
                }
            }
            return false;
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
        if (unchangedLength < 0) {
            throw new IllegalArgumentException("addUnchanged(" + unchangedLength + "): length must not be negative");
        }
        int last = lastUnit();
        if (last < Opcodes.OP_IPUT_OBJECT_JUMBO) {
            int remaining = 4095 - last;
            if (remaining >= unchangedLength) {
                setLastUnit(last + unchangedLength);
                return;
            } else {
                setLastUnit(Opcodes.OP_IPUT_OBJECT_JUMBO);
                unchangedLength -= remaining;
            }
        }
        while (unchangedLength >= 4096) {
            append(Opcodes.OP_IPUT_OBJECT_JUMBO);
            unchangedLength -= 4096;
        }
        if (unchangedLength > 0) {
            append(unchangedLength - 1);
        }
    }

    public void addReplace(int oldLength, int newLength) {
        if (oldLength == newLength && oldLength > 0 && oldLength <= 6) {
            int last = lastUnit();
            if (Opcodes.OP_IPUT_OBJECT_JUMBO >= last || last >= MAX_SHORT_CHANGE || (last >> 12) != oldLength || (last & Opcodes.OP_IPUT_OBJECT_JUMBO) >= Opcodes.OP_IPUT_OBJECT_JUMBO) {
                append(oldLength << 12);
            } else {
                setLastUnit(last + 1);
            }
        } else if (oldLength < 0 || newLength < 0) {
            throw new IllegalArgumentException("addReplace(" + oldLength + ", " + newLength + "): both lengths must be non-negative");
        } else if (oldLength != 0 || newLength != 0) {
            int newDelta = newLength - oldLength;
            if (newDelta != 0) {
                if ((newDelta <= 0 || this.delta < 0 || newDelta <= Integer.MAX_VALUE - this.delta) && (newDelta >= 0 || this.delta >= 0 || newDelta >= Integer.MIN_VALUE - this.delta)) {
                    this.delta += newDelta;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            }
            if (oldLength < 61 && newLength < 61) {
                append(((oldLength << 6) | 28672) | newLength);
            } else if (this.array.length - this.length >= 5 || growArray()) {
                int head;
                int limit;
                int i = this.length + 1;
                if (oldLength < 61) {
                    head = (oldLength << 6) | 28672;
                    limit = i;
                } else if (oldLength <= 32767) {
                    head = 32576;
                    limit = i + 1;
                    this.array[i] = (char) (32768 | oldLength);
                } else {
                    head = (((oldLength >> 30) + 62) << 6) | 28672;
                    limit = i + 1;
                    this.array[i] = (char) ((oldLength >> 15) | 32768);
                    i = limit + 1;
                    this.array[limit] = (char) (32768 | oldLength);
                    limit = i;
                }
                if (newLength < 61) {
                    head |= newLength;
                    i = limit;
                } else if (newLength <= 32767) {
                    head |= 61;
                    i = limit + 1;
                    this.array[limit] = (char) (32768 | newLength);
                } else {
                    head |= (newLength >> 30) + 62;
                    i = limit + 1;
                    this.array[limit] = (char) ((newLength >> 15) | 32768);
                    limit = i + 1;
                    this.array[i] = (char) (32768 | newLength);
                    i = limit;
                }
                this.array[this.length] = (char) head;
                this.length = i;
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
            newCapacity = this.array.length * 2;
        }
        if (newCapacity - this.array.length < 5) {
            throw new BufferOverflowException();
        }
        this.array = Arrays.copyOf(this.array, newCapacity);
        return true;
    }

    public int lengthDelta() {
        return this.delta;
    }

    public boolean hasChanges() {
        if (this.delta != 0) {
            return true;
        }
        for (int i = 0; i < this.length; i++) {
            if (this.array[i] > 4095) {
                return true;
            }
        }
        return false;
    }

    public Iterator getCoarseChangesIterator() {
        return new Iterator(this.array, this.length, true, true, null);
    }

    public Iterator getCoarseIterator() {
        return new Iterator(this.array, this.length, false, true, null);
    }

    public Iterator getFineChangesIterator() {
        return new Iterator(this.array, this.length, true, false, null);
    }

    public Iterator getFineIterator() {
        return new Iterator(this.array, this.length, false, false, null);
    }
}

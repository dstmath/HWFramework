package android.icu.util;

import android.icu.impl.Utility;

@Deprecated
public final class CompactCharArray implements Cloneable {
    static final int BLOCKCOUNT = 32;
    static final int BLOCKMASK = 31;
    @Deprecated
    public static final int BLOCKSHIFT = 5;
    static final int INDEXCOUNT = 2048;
    static final int INDEXSHIFT = 11;
    @Deprecated
    public static final int UNICODECOUNT = 65536;
    char defaultValue;
    private int[] hashes;
    private char[] indices;
    private boolean isCompact;
    private char[] values;

    @Deprecated
    public CompactCharArray() {
        this(0);
    }

    @Deprecated
    public CompactCharArray(char defaultValue2) {
        this.values = new char[65536];
        this.indices = new char[2048];
        this.hashes = new int[2048];
        for (int i = 0; i < 65536; i++) {
            this.values[i] = defaultValue2;
        }
        for (int i2 = 0; i2 < 2048; i2++) {
            this.indices[i2] = (char) (i2 << 5);
            this.hashes[i2] = 0;
        }
        this.isCompact = false;
        this.defaultValue = defaultValue2;
    }

    @Deprecated
    public CompactCharArray(char[] indexArray, char[] newValues) {
        if (indexArray.length == 2048) {
            int i = 0;
            while (i < 2048) {
                if (indexArray[i] < newValues.length + 32) {
                    i++;
                } else {
                    throw new IllegalArgumentException("Index out of bounds.");
                }
            }
            this.indices = indexArray;
            this.values = newValues;
            this.isCompact = true;
            return;
        }
        throw new IllegalArgumentException("Index out of bounds.");
    }

    @Deprecated
    public CompactCharArray(String indexArray, String valueArray) {
        this(Utility.RLEStringToCharArray(indexArray), Utility.RLEStringToCharArray(valueArray));
    }

    @Deprecated
    public char elementAt(char index) {
        int ix = (this.indices[index >> 5] & 65535) + (index & 31);
        return ix >= this.values.length ? this.defaultValue : this.values[ix];
    }

    @Deprecated
    public void setElementAt(char index, char value) {
        if (this.isCompact) {
            expand();
        }
        this.values[index] = value;
        touchBlock(index >> 5, value);
    }

    @Deprecated
    public void setElementAt(char start, char end, char value) {
        if (this.isCompact) {
            expand();
        }
        for (int i = start; i <= end; i++) {
            this.values[i] = value;
            touchBlock(i >> 5, value);
        }
    }

    @Deprecated
    public void compact() {
        compact(true);
    }

    @Deprecated
    public void compact(boolean exhaustive) {
        int dest;
        if (!this.isCompact) {
            int newSize = 0;
            char[] target = exhaustive ? new char[65536] : this.values;
            char iUntouched = 65535;
            int iBlockStart = 0;
            int i = 0;
            while (i < this.indices.length) {
                this.indices[i] = 65535;
                boolean touched = blockTouched(i);
                if (touched || iUntouched == 65535) {
                    int jBlockStart = 0;
                    int j = 0;
                    while (j < i) {
                        if (this.hashes[i] == this.hashes[j] && arrayRegionMatches(this.values, iBlockStart, this.values, jBlockStart, 32)) {
                            this.indices[i] = this.indices[j];
                        }
                        j++;
                        jBlockStart += 32;
                    }
                    if (this.indices[i] == 65535) {
                        if (exhaustive) {
                            dest = FindOverlappingPosition(iBlockStart, target, newSize);
                        } else {
                            dest = newSize;
                        }
                        int limit = dest + 32;
                        if (limit > newSize) {
                            for (int j2 = newSize; j2 < limit; j2++) {
                                target[j2] = this.values[(iBlockStart + j2) - dest];
                            }
                            newSize = limit;
                        }
                        this.indices[i] = (char) dest;
                        if (!touched) {
                            iUntouched = (char) jBlockStart;
                        }
                    }
                } else {
                    this.indices[i] = iUntouched;
                }
                i++;
                iBlockStart += 32;
            }
            char[] result = new char[newSize];
            System.arraycopy(target, 0, result, 0, newSize);
            this.values = result;
            this.isCompact = true;
            this.hashes = null;
        }
    }

    private int FindOverlappingPosition(int start, char[] tempValues, int tempCount) {
        for (int i = 0; i < tempCount; i++) {
            int currentCount = 32;
            if (i + 32 > tempCount) {
                currentCount = tempCount - i;
            }
            if (arrayRegionMatches(this.values, start, tempValues, i, currentCount)) {
                return i;
            }
        }
        return tempCount;
    }

    static final boolean arrayRegionMatches(char[] source, int sourceStart, char[] target, int targetStart, int len) {
        int sourceEnd = sourceStart + len;
        int delta = targetStart - sourceStart;
        for (int i = sourceStart; i < sourceEnd; i++) {
            if (source[i] != target[i + delta]) {
                return false;
            }
        }
        return true;
    }

    private final void touchBlock(int i, int value) {
        this.hashes[i] = (this.hashes[i] + (value << 1)) | 1;
    }

    private final boolean blockTouched(int i) {
        return this.hashes[i] != 0;
    }

    @Deprecated
    public char[] getIndexArray() {
        return this.indices;
    }

    @Deprecated
    public char[] getValueArray() {
        return this.values;
    }

    @Deprecated
    public Object clone() {
        try {
            CompactCharArray other = (CompactCharArray) super.clone();
            other.values = (char[]) this.values.clone();
            other.indices = (char[]) this.indices.clone();
            if (this.hashes != null) {
                other.hashes = (int[]) this.hashes.clone();
            }
            return other;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException((Throwable) e);
        }
    }

    @Deprecated
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CompactCharArray other = (CompactCharArray) obj;
        for (int i = 0; i < 65536; i++) {
            if (elementAt((char) i) != other.elementAt((char) i)) {
                return false;
            }
        }
        return true;
    }

    @Deprecated
    public int hashCode() {
        int result = 0;
        int increment = Math.min(3, this.values.length / 16);
        for (int i = 0; i < this.values.length; i += increment) {
            result = (result * 37) + this.values[i];
        }
        return result;
    }

    private void expand() {
        if (this.isCompact) {
            this.hashes = new int[2048];
            char[] tempArray = new char[65536];
            for (int i = 0; i < 65536; i++) {
                tempArray[i] = elementAt((char) i);
            }
            for (int i2 = 0; i2 < 2048; i2++) {
                this.indices[i2] = (char) (i2 << 5);
            }
            this.values = null;
            this.values = tempArray;
            this.isCompact = false;
        }
    }
}

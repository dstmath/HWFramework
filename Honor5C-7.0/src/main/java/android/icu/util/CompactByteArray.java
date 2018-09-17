package android.icu.util;

import android.icu.impl.Utility;
import android.icu.text.UnicodeMatcher;
import com.android.dex.DexFormat;

@Deprecated
public final class CompactByteArray implements Cloneable {
    private static final int BLOCKCOUNT = 128;
    private static final int BLOCKMASK = 127;
    private static final int BLOCKSHIFT = 7;
    private static final int INDEXCOUNT = 512;
    private static final int INDEXSHIFT = 9;
    @Deprecated
    public static final int UNICODECOUNT = 65536;
    byte defaultValue;
    private int[] hashes;
    private char[] indices;
    private boolean isCompact;
    private byte[] values;

    @Deprecated
    public CompactByteArray() {
        this((byte) 0);
    }

    @Deprecated
    public CompactByteArray(byte defaultValue) {
        int i;
        this.values = new byte[UNICODECOUNT];
        this.indices = new char[INDEXCOUNT];
        this.hashes = new int[INDEXCOUNT];
        for (i = 0; i < UNICODECOUNT; i++) {
            this.values[i] = defaultValue;
        }
        for (i = 0; i < INDEXCOUNT; i++) {
            this.indices[i] = (char) (i << BLOCKSHIFT);
            this.hashes[i] = 0;
        }
        this.isCompact = false;
        this.defaultValue = defaultValue;
    }

    @Deprecated
    public CompactByteArray(char[] indexArray, byte[] newValues) {
        if (indexArray.length != INDEXCOUNT) {
            throw new IllegalArgumentException("Index out of bounds.");
        }
        for (int i = 0; i < INDEXCOUNT; i++) {
            char index = indexArray[i];
            if (index < '\u0000' || index >= newValues.length + BLOCKCOUNT) {
                throw new IllegalArgumentException("Index out of bounds.");
            }
        }
        this.indices = indexArray;
        this.values = newValues;
        this.isCompact = true;
    }

    @Deprecated
    public CompactByteArray(String indexArray, String valueArray) {
        this(Utility.RLEStringToCharArray(indexArray), Utility.RLEStringToByteArray(valueArray));
    }

    @Deprecated
    public byte elementAt(char index) {
        return this.values[(this.indices[index >> BLOCKSHIFT] & DexFormat.MAX_TYPE_IDX) + (index & BLOCKMASK)];
    }

    @Deprecated
    public void setElementAt(char index, byte value) {
        if (this.isCompact) {
            expand();
        }
        this.values[index] = value;
        touchBlock(index >> BLOCKSHIFT, value);
    }

    @Deprecated
    public void setElementAt(char start, char end, byte value) {
        if (this.isCompact) {
            expand();
        }
        for (char i = start; i <= end; i++) {
            this.values[i] = value;
            touchBlock(i >> BLOCKSHIFT, value);
        }
    }

    @Deprecated
    public void compact() {
        compact(false);
    }

    @Deprecated
    public void compact(boolean exhaustive) {
        if (!this.isCompact) {
            int limitCompacted = 0;
            int iBlockStart = 0;
            char iUntouched = UnicodeMatcher.ETHER;
            int i = 0;
            while (i < this.indices.length) {
                this.indices[i] = UnicodeMatcher.ETHER;
                boolean touched = blockTouched(i);
                if (touched || iUntouched == UnicodeMatcher.ETHER) {
                    int jBlockStart = 0;
                    int j = 0;
                    while (j < limitCompacted) {
                        if (this.hashes[i] == this.hashes[j] && arrayRegionMatches(this.values, iBlockStart, this.values, jBlockStart, BLOCKCOUNT)) {
                            this.indices[i] = (char) jBlockStart;
                            break;
                        } else {
                            j++;
                            jBlockStart += BLOCKCOUNT;
                        }
                    }
                    if (this.indices[i] == UnicodeMatcher.ETHER) {
                        System.arraycopy(this.values, iBlockStart, this.values, jBlockStart, BLOCKCOUNT);
                        this.indices[i] = (char) jBlockStart;
                        this.hashes[j] = this.hashes[i];
                        limitCompacted++;
                        if (!touched) {
                            iUntouched = (char) jBlockStart;
                        }
                    }
                } else {
                    this.indices[i] = iUntouched;
                }
                i++;
                iBlockStart += BLOCKCOUNT;
            }
            int newSize = limitCompacted * BLOCKCOUNT;
            byte[] result = new byte[newSize];
            System.arraycopy(this.values, 0, result, 0, newSize);
            this.values = result;
            this.isCompact = true;
            this.hashes = null;
        }
    }

    static final boolean arrayRegionMatches(byte[] source, int sourceStart, byte[] target, int targetStart, int len) {
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
    public byte[] getValueArray() {
        return this.values;
    }

    @Deprecated
    public Object clone() {
        try {
            CompactByteArray other = (CompactByteArray) super.clone();
            other.values = (byte[]) this.values.clone();
            other.indices = (char[]) this.indices.clone();
            if (this.hashes != null) {
                other.hashes = (int[]) this.hashes.clone();
            }
            return other;
        } catch (Throwable e) {
            throw new ICUCloneNotSupportedException(e);
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
        CompactByteArray other = (CompactByteArray) obj;
        for (int i = 0; i < UNICODECOUNT; i++) {
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
            int i;
            this.hashes = new int[INDEXCOUNT];
            byte[] tempArray = new byte[UNICODECOUNT];
            for (i = 0; i < UNICODECOUNT; i++) {
                byte value = elementAt((char) i);
                tempArray[i] = value;
                touchBlock(i >> BLOCKSHIFT, value);
            }
            for (i = 0; i < INDEXCOUNT; i++) {
                this.indices[i] = (char) (i << BLOCKSHIFT);
            }
            this.values = null;
            this.values = tempArray;
            this.isCompact = false;
        }
    }
}

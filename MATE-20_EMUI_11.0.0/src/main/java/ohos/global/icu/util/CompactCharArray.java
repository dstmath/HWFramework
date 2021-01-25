package ohos.global.icu.util;

import ohos.global.icu.impl.Utility;

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
    public CompactCharArray(char c) {
        this.values = new char[65536];
        this.indices = new char[2048];
        this.hashes = new int[2048];
        for (int i = 0; i < 65536; i++) {
            this.values[i] = c;
        }
        for (int i2 = 0; i2 < 2048; i2++) {
            this.indices[i2] = (char) (i2 << 5);
            this.hashes[i2] = 0;
        }
        this.isCompact = false;
        this.defaultValue = c;
    }

    @Deprecated
    public CompactCharArray(char[] cArr, char[] cArr2) {
        if (cArr.length == 2048) {
            for (int i = 0; i < 2048; i++) {
                if (cArr[i] >= cArr2.length + 32) {
                    throw new IllegalArgumentException("Index out of bounds.");
                }
            }
            this.indices = cArr;
            this.values = cArr2;
            this.isCompact = true;
            return;
        }
        throw new IllegalArgumentException("Index out of bounds.");
    }

    @Deprecated
    public CompactCharArray(String str, String str2) {
        this(Utility.RLEStringToCharArray(str), Utility.RLEStringToCharArray(str2));
    }

    @Deprecated
    public char elementAt(char c) {
        int i = (this.indices[c >> 5] & 65535) + (c & 31);
        char[] cArr = this.values;
        return i >= cArr.length ? this.defaultValue : cArr[i];
    }

    @Deprecated
    public void setElementAt(char c, char c2) {
        if (this.isCompact) {
            expand();
        }
        this.values[c] = c2;
        touchBlock(c >> 5, c2);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:7:0x0007 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [char] */
    /* JADX WARN: Type inference failed for: r2v1, types: [int] */
    /* JADX WARN: Type inference failed for: r2v2, types: [int] */
    /* JADX WARN: Type inference failed for: r2v3 */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Deprecated
    public void setElementAt(char c, char c2, char c3) {
        if (this.isCompact) {
            expand();
        }
        while (c <= c2) {
            this.values[c == true ? 1 : 0] = c3;
            touchBlock((c == true ? 1 : 0) >> 5, c3);
            c++;
        }
    }

    @Deprecated
    public void compact() {
        compact(true);
    }

    @Deprecated
    public void compact(boolean z) {
        char[] cArr;
        if (!this.isCompact) {
            if (z) {
                cArr = new char[65536];
            } else {
                cArr = this.values;
            }
            char c = 65535;
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            while (true) {
                char[] cArr2 = this.indices;
                if (i < cArr2.length) {
                    cArr2[i] = 65535;
                    boolean blockTouched = blockTouched(i);
                    if (blockTouched || c == 65535) {
                        int i4 = 0;
                        int i5 = 0;
                        while (i4 < i) {
                            int[] iArr = this.hashes;
                            if (iArr[i] == iArr[i4]) {
                                char[] cArr3 = this.values;
                                if (arrayRegionMatches(cArr3, i3, cArr3, i5, 32)) {
                                    char[] cArr4 = this.indices;
                                    cArr4[i] = cArr4[i4];
                                }
                            }
                            i4++;
                            i5 += 32;
                        }
                        if (this.indices[i] == 65535) {
                            int FindOverlappingPosition = z ? FindOverlappingPosition(i3, cArr, i2) : i2;
                            int i6 = FindOverlappingPosition + 32;
                            if (i6 > i2) {
                                while (i2 < i6) {
                                    cArr[i2] = this.values[(i3 + i2) - FindOverlappingPosition];
                                    i2++;
                                }
                                i2 = i6;
                            }
                            this.indices[i] = (char) FindOverlappingPosition;
                            if (!blockTouched) {
                                c = (char) i5;
                            }
                        }
                    } else {
                        this.indices[i] = c;
                    }
                    i++;
                    i3 += 32;
                } else {
                    char[] cArr5 = new char[i2];
                    System.arraycopy(cArr, 0, cArr5, 0, i2);
                    this.values = cArr5;
                    this.isCompact = true;
                    this.hashes = null;
                    return;
                }
            }
        }
    }

    private int FindOverlappingPosition(int i, char[] cArr, int i2) {
        for (int i3 = 0; i3 < i2; i3++) {
            if (arrayRegionMatches(this.values, i, cArr, i3, i3 + 32 > i2 ? i2 - i3 : 32)) {
                return i3;
            }
        }
        return i2;
    }

    static final boolean arrayRegionMatches(char[] cArr, int i, char[] cArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (cArr[i] != cArr2[i + i5]) {
                return false;
            }
            i++;
        }
        return true;
    }

    private final void touchBlock(int i, int i2) {
        int[] iArr = this.hashes;
        iArr[i] = (iArr[i] + (i2 << 1)) | 1;
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

    @Override // java.lang.Object
    @Deprecated
    public Object clone() {
        try {
            CompactCharArray compactCharArray = (CompactCharArray) super.clone();
            compactCharArray.values = (char[]) this.values.clone();
            compactCharArray.indices = (char[]) this.indices.clone();
            if (this.hashes != null) {
                compactCharArray.hashes = (int[]) this.hashes.clone();
            }
            return compactCharArray;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

    @Override // java.lang.Object
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
        CompactCharArray compactCharArray = (CompactCharArray) obj;
        for (int i = 0; i < 65536; i++) {
            char c = (char) i;
            if (elementAt(c) != compactCharArray.elementAt(c)) {
                return false;
            }
        }
        return true;
    }

    @Override // java.lang.Object
    @Deprecated
    public int hashCode() {
        int min = Math.min(3, this.values.length / 16);
        int i = 0;
        int i2 = 0;
        while (true) {
            char[] cArr = this.values;
            if (i >= cArr.length) {
                return i2;
            }
            i2 = (i2 * 37) + cArr[i];
            i += min;
        }
    }

    private void expand() {
        if (this.isCompact) {
            this.hashes = new int[2048];
            char[] cArr = new char[65536];
            for (int i = 0; i < 65536; i++) {
                cArr[i] = elementAt((char) i);
            }
            for (int i2 = 0; i2 < 2048; i2++) {
                this.indices[i2] = (char) (i2 << 5);
            }
            this.values = null;
            this.values = cArr;
            this.isCompact = false;
        }
    }
}

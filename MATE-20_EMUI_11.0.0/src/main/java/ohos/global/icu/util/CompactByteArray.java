package ohos.global.icu.util;

import ohos.global.icu.impl.Utility;

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
    public CompactByteArray(byte b) {
        this.values = new byte[65536];
        this.indices = new char[512];
        this.hashes = new int[512];
        for (int i = 0; i < 65536; i++) {
            this.values[i] = b;
        }
        for (int i2 = 0; i2 < 512; i2++) {
            this.indices[i2] = (char) (i2 << 7);
            this.hashes[i2] = 0;
        }
        this.isCompact = false;
        this.defaultValue = b;
    }

    @Deprecated
    public CompactByteArray(char[] cArr, byte[] bArr) {
        if (cArr.length == 512) {
            for (int i = 0; i < 512; i++) {
                if (cArr[i] >= bArr.length + 128) {
                    throw new IllegalArgumentException("Index out of bounds.");
                }
            }
            this.indices = cArr;
            this.values = bArr;
            this.isCompact = true;
            return;
        }
        throw new IllegalArgumentException("Index out of bounds.");
    }

    @Deprecated
    public CompactByteArray(String str, String str2) {
        this(Utility.RLEStringToCharArray(str), Utility.RLEStringToByteArray(str2));
    }

    @Deprecated
    public byte elementAt(char c) {
        return this.values[(this.indices[c >> 7] & 65535) + (c & 127)];
    }

    @Deprecated
    public void setElementAt(char c, byte b) {
        if (this.isCompact) {
            expand();
        }
        this.values[c] = b;
        touchBlock(c >> 7, b);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:7:0x0007 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v0, types: [char] */
    /* JADX WARN: Type inference failed for: r2v1, types: [int] */
    /* JADX WARN: Type inference failed for: r2v2, types: [int] */
    /* JADX WARN: Type inference failed for: r2v3 */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Deprecated
    public void setElementAt(char c, char c2, byte b) {
        if (this.isCompact) {
            expand();
        }
        while (c <= c2) {
            this.values[c == true ? 1 : 0] = b;
            touchBlock((c == true ? 1 : 0) >> 7, b);
            c++;
        }
    }

    @Deprecated
    public void compact() {
        compact(false);
    }

    @Deprecated
    public void compact(boolean z) {
        if (!this.isCompact) {
            char c = 65535;
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            while (true) {
                char[] cArr = this.indices;
                if (i < cArr.length) {
                    cArr[i] = 65535;
                    boolean blockTouched = blockTouched(i);
                    if (blockTouched || c == 65535) {
                        int i4 = 0;
                        int i5 = 0;
                        while (true) {
                            if (i4 >= i2) {
                                break;
                            }
                            int[] iArr = this.hashes;
                            if (iArr[i] == iArr[i4]) {
                                byte[] bArr = this.values;
                                if (arrayRegionMatches(bArr, i3, bArr, i5, 128)) {
                                    this.indices[i] = (char) i5;
                                    break;
                                }
                            }
                            i4++;
                            i5 += 128;
                        }
                        if (this.indices[i] == 65535) {
                            byte[] bArr2 = this.values;
                            System.arraycopy(bArr2, i3, bArr2, i5, 128);
                            char c2 = (char) i5;
                            this.indices[i] = c2;
                            int[] iArr2 = this.hashes;
                            iArr2[i4] = iArr2[i];
                            i2++;
                            if (!blockTouched) {
                                c = c2;
                            }
                        }
                    } else {
                        this.indices[i] = c;
                    }
                    i++;
                    i3 += 128;
                } else {
                    int i6 = i2 * 128;
                    byte[] bArr3 = new byte[i6];
                    System.arraycopy(this.values, 0, bArr3, 0, i6);
                    this.values = bArr3;
                    this.isCompact = true;
                    this.hashes = null;
                    return;
                }
            }
        }
    }

    static final boolean arrayRegionMatches(byte[] bArr, int i, byte[] bArr2, int i2, int i3) {
        int i4 = i3 + i;
        int i5 = i2 - i;
        while (i < i4) {
            if (bArr[i] != bArr2[i + i5]) {
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
    public byte[] getValueArray() {
        return this.values;
    }

    @Override // java.lang.Object
    @Deprecated
    public Object clone() {
        try {
            CompactByteArray compactByteArray = (CompactByteArray) super.clone();
            compactByteArray.values = (byte[]) this.values.clone();
            compactByteArray.indices = (char[]) this.indices.clone();
            if (this.hashes != null) {
                compactByteArray.hashes = (int[]) this.hashes.clone();
            }
            return compactByteArray;
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
        CompactByteArray compactByteArray = (CompactByteArray) obj;
        for (int i = 0; i < 65536; i++) {
            char c = (char) i;
            if (elementAt(c) != compactByteArray.elementAt(c)) {
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
            byte[] bArr = this.values;
            if (i >= bArr.length) {
                return i2;
            }
            i2 = (i2 * 37) + bArr[i];
            i += min;
        }
    }

    private void expand() {
        if (this.isCompact) {
            this.hashes = new int[512];
            byte[] bArr = new byte[65536];
            for (int i = 0; i < 65536; i++) {
                byte elementAt = elementAt((char) i);
                bArr[i] = elementAt;
                touchBlock(i >> 7, elementAt);
            }
            for (int i2 = 0; i2 < 512; i2++) {
                this.indices[i2] = (char) (i2 << 7);
            }
            this.values = null;
            this.values = bArr;
            this.isCompact = false;
        }
    }
}

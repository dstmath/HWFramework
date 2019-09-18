package sun.security.util;

import java.io.IOException;
import java.util.ArrayList;

class DerIndefLenConverter {
    private static final int CLASS_MASK = 192;
    private static final int FORM_MASK = 32;
    private static final int LEN_LONG = 128;
    private static final int LEN_MASK = 127;
    private static final int SKIP_EOC_BYTES = 2;
    private static final int TAG_MASK = 31;
    private byte[] data;
    private int dataPos;
    private int dataSize;
    private int index;
    private ArrayList<Object> ndefsList = new ArrayList<>();
    private byte[] newData;
    private int newDataPos;
    private int numOfTotalLenBytes = 0;
    private int unresolved = 0;

    private boolean isEOC(int tag) {
        return (tag & 31) == 0 && (tag & 32) == 0 && (tag & CLASS_MASK) == 0;
    }

    static boolean isLongForm(int lengthByte) {
        return (lengthByte & 128) == 128;
    }

    DerIndefLenConverter() {
    }

    static boolean isIndefinite(int lengthByte) {
        return isLongForm(lengthByte) && (lengthByte & 127) == 0;
    }

    private void parseTag() throws IOException {
        if (this.dataPos != this.dataSize) {
            if (isEOC(this.data[this.dataPos]) && this.data[this.dataPos + 1] == 0) {
                int numOfEncapsulatedLenBytes = 0;
                Object elem = null;
                int index2 = this.ndefsList.size() - 1;
                while (index2 >= 0) {
                    elem = this.ndefsList.get(index2);
                    if (elem instanceof Integer) {
                        break;
                    }
                    numOfEncapsulatedLenBytes += ((byte[]) elem).length - 3;
                    index2--;
                }
                if (index2 >= 0) {
                    this.ndefsList.set(index2, getLengthBytes((this.dataPos - ((Integer) elem).intValue()) + numOfEncapsulatedLenBytes));
                    this.unresolved--;
                    this.numOfTotalLenBytes += sectionLenBytes.length - 3;
                } else {
                    throw new IOException("EOC does not have matching indefinite-length tag");
                }
            }
            this.dataPos++;
        }
    }

    private void writeTag() {
        if (this.dataPos != this.dataSize) {
            byte[] bArr = this.data;
            int i = this.dataPos;
            this.dataPos = i + 1;
            byte tag = bArr[i];
            if (!isEOC(tag) || this.data[this.dataPos] != 0) {
                byte[] bArr2 = this.newData;
                int i2 = this.newDataPos;
                this.newDataPos = i2 + 1;
                bArr2[i2] = (byte) tag;
            } else {
                this.dataPos++;
                writeTag();
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    private int parseLength() throws IOException {
        int curLen = 0;
        if (this.dataPos == this.dataSize) {
            return 0;
        }
        byte[] bArr = this.data;
        int i = this.dataPos;
        this.dataPos = i + 1;
        int lenByte = bArr[i] & 255;
        if (isIndefinite(lenByte)) {
            this.ndefsList.add(new Integer(this.dataPos));
            this.unresolved++;
            return 0;
        }
        if (isLongForm(lenByte)) {
            int lenByte2 = lenByte & 127;
            if (lenByte2 > 4) {
                throw new IOException("Too much data");
            } else if (this.dataSize - this.dataPos >= lenByte2 + 1) {
                for (int i2 = 0; i2 < lenByte2; i2++) {
                    byte[] bArr2 = this.data;
                    int i3 = this.dataPos;
                    this.dataPos = i3 + 1;
                    curLen = (curLen << 8) + (bArr2[i3] & 255);
                }
                if (curLen < 0) {
                    throw new IOException("Invalid length bytes");
                }
            } else {
                throw new IOException("Too little data");
            }
        } else {
            curLen = lenByte & 127;
        }
        return curLen;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v3, resolved type: byte} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v8, resolved type: byte} */
    /* JADX WARNING: Multi-variable type inference failed */
    private void writeLengthAndValue() throws IOException {
        if (this.dataPos != this.dataSize) {
            int curLen = 0;
            byte[] bArr = this.data;
            int i = this.dataPos;
            this.dataPos = i + 1;
            int lenByte = bArr[i] & 255;
            int i2 = 0;
            if (isIndefinite(lenByte)) {
                ArrayList<Object> arrayList = this.ndefsList;
                int i3 = this.index;
                this.index = i3 + 1;
                byte[] lenBytes = (byte[]) arrayList.get(i3);
                System.arraycopy(lenBytes, 0, this.newData, this.newDataPos, lenBytes.length);
                this.newDataPos += lenBytes.length;
                return;
            }
            if (isLongForm(lenByte)) {
                int lenByte2 = lenByte & 127;
                while (true) {
                    int i4 = i2;
                    if (i4 >= lenByte2) {
                        break;
                    }
                    byte[] bArr2 = this.data;
                    int i5 = this.dataPos;
                    this.dataPos = i5 + 1;
                    curLen = (curLen << 8) + (bArr2[i5] & 255);
                    i2 = i4 + 1;
                }
                if (curLen < 0) {
                    throw new IOException("Invalid length bytes");
                }
            } else {
                curLen = lenByte & 127;
            }
            writeLength(curLen);
            writeValue(curLen);
        }
    }

    private void writeLength(int curLen) {
        if (curLen < 128) {
            byte[] bArr = this.newData;
            int i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) curLen;
        } else if (curLen < 256) {
            byte[] bArr2 = this.newData;
            int i2 = this.newDataPos;
            this.newDataPos = i2 + 1;
            bArr2[i2] = -127;
            byte[] bArr3 = this.newData;
            int i3 = this.newDataPos;
            this.newDataPos = i3 + 1;
            bArr3[i3] = (byte) curLen;
        } else if (curLen < 65536) {
            byte[] bArr4 = this.newData;
            int i4 = this.newDataPos;
            this.newDataPos = i4 + 1;
            bArr4[i4] = -126;
            byte[] bArr5 = this.newData;
            int i5 = this.newDataPos;
            this.newDataPos = i5 + 1;
            bArr5[i5] = (byte) (curLen >> 8);
            byte[] bArr6 = this.newData;
            int i6 = this.newDataPos;
            this.newDataPos = i6 + 1;
            bArr6[i6] = (byte) curLen;
        } else if (curLen < 16777216) {
            byte[] bArr7 = this.newData;
            int i7 = this.newDataPos;
            this.newDataPos = i7 + 1;
            bArr7[i7] = -125;
            byte[] bArr8 = this.newData;
            int i8 = this.newDataPos;
            this.newDataPos = i8 + 1;
            bArr8[i8] = (byte) (curLen >> 16);
            byte[] bArr9 = this.newData;
            int i9 = this.newDataPos;
            this.newDataPos = i9 + 1;
            bArr9[i9] = (byte) (curLen >> 8);
            byte[] bArr10 = this.newData;
            int i10 = this.newDataPos;
            this.newDataPos = i10 + 1;
            bArr10[i10] = (byte) curLen;
        } else {
            byte[] bArr11 = this.newData;
            int i11 = this.newDataPos;
            this.newDataPos = i11 + 1;
            bArr11[i11] = -124;
            byte[] bArr12 = this.newData;
            int i12 = this.newDataPos;
            this.newDataPos = i12 + 1;
            bArr12[i12] = (byte) (curLen >> 24);
            byte[] bArr13 = this.newData;
            int i13 = this.newDataPos;
            this.newDataPos = i13 + 1;
            bArr13[i13] = (byte) (curLen >> 16);
            byte[] bArr14 = this.newData;
            int i14 = this.newDataPos;
            this.newDataPos = i14 + 1;
            bArr14[i14] = (byte) (curLen >> 8);
            byte[] bArr15 = this.newData;
            int i15 = this.newDataPos;
            this.newDataPos = i15 + 1;
            bArr15[i15] = (byte) curLen;
        }
    }

    private byte[] getLengthBytes(int curLen) {
        byte[] lenBytes;
        int index2;
        if (curLen < 128) {
            int i = 0 + 1;
            return new byte[]{(byte) curLen};
        }
        if (curLen < 256) {
            lenBytes = new byte[2];
            int index3 = 0 + 1;
            lenBytes[0] = -127;
            index2 = index3 + 1;
            lenBytes[index3] = (byte) curLen;
        } else if (curLen < 65536) {
            byte[] lenBytes2 = new byte[3];
            int index4 = 0 + 1;
            lenBytes2[0] = -126;
            int index5 = index4 + 1;
            lenBytes2[index4] = (byte) (curLen >> 8);
            int index6 = index5 + 1;
            lenBytes2[index5] = (byte) curLen;
            return lenBytes2;
        } else if (curLen < 16777216) {
            lenBytes = new byte[4];
            int index7 = 0 + 1;
            lenBytes[0] = -125;
            int index8 = index7 + 1;
            lenBytes[index7] = (byte) (curLen >> 16);
            int index9 = index8 + 1;
            lenBytes[index8] = (byte) (curLen >> 8);
            index2 = index9 + 1;
            lenBytes[index9] = (byte) curLen;
        } else {
            byte[] lenBytes3 = new byte[5];
            int index10 = 0 + 1;
            lenBytes3[0] = -124;
            int index11 = index10 + 1;
            lenBytes3[index10] = (byte) (curLen >> 24);
            int index12 = index11 + 1;
            lenBytes3[index11] = (byte) (curLen >> 16);
            int index13 = index12 + 1;
            lenBytes3[index12] = (byte) (curLen >> 8);
            int index14 = index13 + 1;
            lenBytes3[index13] = (byte) curLen;
            return lenBytes3;
        }
        return lenBytes;
    }

    private int getNumOfLenBytes(int len) {
        if (len < 128) {
            return 1;
        }
        if (len < 256) {
            return 2;
        }
        if (len < 65536) {
            return 3;
        }
        if (len < 16777216) {
            return 4;
        }
        return 5;
    }

    private void parseValue(int curLen) {
        this.dataPos += curLen;
    }

    private void writeValue(int curLen) {
        for (int i = 0; i < curLen; i++) {
            byte[] bArr = this.newData;
            int i2 = this.newDataPos;
            this.newDataPos = i2 + 1;
            byte[] bArr2 = this.data;
            int i3 = this.dataPos;
            this.dataPos = i3 + 1;
            bArr[i2] = bArr2[i3];
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] convert(byte[] indefData) throws IOException {
        this.data = indefData;
        this.dataPos = 0;
        this.index = 0;
        this.dataSize = this.data.length;
        int unused = 0;
        while (true) {
            if (this.dataPos >= this.dataSize) {
                break;
            }
            parseTag();
            parseValue(parseLength());
            if (this.unresolved == 0) {
                unused = this.dataSize - this.dataPos;
                this.dataSize = this.dataPos;
                break;
            }
        }
        if (this.unresolved == 0) {
            this.newData = new byte[(this.dataSize + this.numOfTotalLenBytes + unused)];
            this.dataPos = 0;
            this.newDataPos = 0;
            this.index = 0;
            while (this.dataPos < this.dataSize) {
                writeTag();
                writeLengthAndValue();
            }
            System.arraycopy(indefData, this.dataSize, this.newData, this.dataSize + this.numOfTotalLenBytes, unused);
            return this.newData;
        }
        throw new IOException("not all indef len BER resolved");
    }
}

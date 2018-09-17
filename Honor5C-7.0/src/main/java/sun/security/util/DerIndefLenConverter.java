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
    private ArrayList<Object> ndefsList;
    private byte[] newData;
    private int newDataPos;
    private int numOfTotalLenBytes;
    private int unresolved;

    private boolean isEOC(int tag) {
        if ((tag & TAG_MASK) == 0 && (tag & FORM_MASK) == 0 && (tag & CLASS_MASK) == 0) {
            return true;
        }
        return false;
    }

    static boolean isLongForm(int lengthByte) {
        return (lengthByte & LEN_LONG) == LEN_LONG;
    }

    DerIndefLenConverter() {
        this.unresolved = 0;
        this.ndefsList = new ArrayList();
        this.numOfTotalLenBytes = 0;
    }

    static boolean isIndefinite(int lengthByte) {
        return isLongForm(lengthByte) && (lengthByte & LEN_MASK) == 0;
    }

    private void parseTag() throws IOException {
        if (this.dataPos != this.dataSize) {
            if (isEOC(this.data[this.dataPos]) && this.data[this.dataPos + 1] == null) {
                int numOfEncapsulatedLenBytes = 0;
                Object elem = null;
                int index = this.ndefsList.size() - 1;
                while (index >= 0) {
                    elem = this.ndefsList.get(index);
                    if (elem instanceof Integer) {
                        break;
                    }
                    numOfEncapsulatedLenBytes += ((byte[]) elem).length - 3;
                    index--;
                }
                if (index < 0) {
                    throw new IOException("EOC does not have matching indefinite-length tag");
                }
                byte[] sectionLenBytes = getLengthBytes((this.dataPos - ((Integer) elem).intValue()) + numOfEncapsulatedLenBytes);
                this.ndefsList.set(index, sectionLenBytes);
                this.unresolved--;
                this.numOfTotalLenBytes += sectionLenBytes.length - 3;
            }
            this.dataPos++;
        }
    }

    private void writeTag() {
        if (this.dataPos != this.dataSize) {
            byte[] bArr = this.data;
            int i = this.dataPos;
            this.dataPos = i + 1;
            int tag = bArr[i];
            if (isEOC(tag) && this.data[this.dataPos] == null) {
                this.dataPos++;
                writeTag();
            } else {
                bArr = this.newData;
                i = this.newDataPos;
                this.newDataPos = i + 1;
                bArr[i] = (byte) tag;
            }
        }
    }

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
            lenByte &= LEN_MASK;
            if (lenByte > 4) {
                throw new IOException("Too much data");
            } else if (this.dataSize - this.dataPos < lenByte + 1) {
                throw new IOException("Too little data");
            } else {
                for (int i2 = 0; i2 < lenByte; i2++) {
                    int i3 = curLen << 8;
                    byte[] bArr2 = this.data;
                    int i4 = this.dataPos;
                    this.dataPos = i4 + 1;
                    curLen = i3 + (bArr2[i4] & 255);
                }
                if (curLen < 0) {
                    throw new IOException("Invalid length bytes");
                }
            }
        }
        curLen = lenByte & LEN_MASK;
        return curLen;
    }

    private void writeLengthAndValue() throws IOException {
        if (this.dataPos != this.dataSize) {
            int curLen = 0;
            byte[] bArr = this.data;
            int i = this.dataPos;
            this.dataPos = i + 1;
            int lenByte = bArr[i] & 255;
            if (isIndefinite(lenByte)) {
                ArrayList arrayList = this.ndefsList;
                i = this.index;
                this.index = i + 1;
                byte[] lenBytes = (byte[]) arrayList.get(i);
                System.arraycopy(lenBytes, 0, this.newData, this.newDataPos, lenBytes.length);
                this.newDataPos += lenBytes.length;
                return;
            }
            if (isLongForm(lenByte)) {
                lenByte &= LEN_MASK;
                for (int i2 = 0; i2 < lenByte; i2++) {
                    int i3 = curLen << 8;
                    byte[] bArr2 = this.data;
                    int i4 = this.dataPos;
                    this.dataPos = i4 + 1;
                    curLen = i3 + (bArr2[i4] & 255);
                }
                if (curLen < 0) {
                    throw new IOException("Invalid length bytes");
                }
            }
            curLen = lenByte & LEN_MASK;
            writeLength(curLen);
            writeValue(curLen);
        }
    }

    private void writeLength(int curLen) {
        byte[] bArr;
        int i;
        if (curLen < LEN_LONG) {
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) curLen;
        } else if (curLen < Record.maxPadding) {
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) -127;
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) curLen;
        } else if (curLen < Record.OVERFLOW_OF_INT16) {
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) -126;
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) (curLen >> 8);
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) curLen;
        } else if (curLen < Record.OVERFLOW_OF_INT24) {
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) -125;
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) (curLen >> 16);
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) (curLen >> 8);
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) curLen;
        } else {
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) -124;
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) (curLen >> 24);
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) (curLen >> 16);
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) (curLen >> 8);
            bArr = this.newData;
            i = this.newDataPos;
            this.newDataPos = i + 1;
            bArr[i] = (byte) curLen;
        }
    }

    private byte[] getLengthBytes(int curLen) {
        if (curLen < LEN_LONG) {
            return new byte[]{(byte) curLen};
        } else if (curLen < Record.maxPadding) {
            lenBytes = new byte[SKIP_EOC_BYTES];
            lenBytes[0] = (byte) -127;
            index = 1 + 1;
            lenBytes[1] = (byte) curLen;
            r0 = index;
            return lenBytes;
        } else if (curLen < Record.OVERFLOW_OF_INT16) {
            lenBytes = new byte[3];
            index = 1 + 1;
            lenBytes[1] = (byte) (curLen >> 8);
            r0 = index + 1;
            lenBytes[index] = (byte) curLen;
            return lenBytes;
        } else if (curLen < Record.OVERFLOW_OF_INT24) {
            lenBytes = new byte[4];
            lenBytes[0] = (byte) -125;
            index = 1 + 1;
            lenBytes[1] = (byte) (curLen >> 16);
            r0 = index + 1;
            lenBytes[index] = (byte) (curLen >> 8);
            index = r0 + 1;
            lenBytes[r0] = (byte) curLen;
            r0 = index;
            return lenBytes;
        } else {
            lenBytes = new byte[5];
            index = 1 + 1;
            lenBytes[1] = (byte) (curLen >> 24);
            r0 = index + 1;
            lenBytes[index] = (byte) (curLen >> 16);
            index = r0 + 1;
            lenBytes[r0] = (byte) (curLen >> 8);
            r0 = index + 1;
            lenBytes[index] = (byte) curLen;
            return lenBytes;
        }
    }

    private int getNumOfLenBytes(int len) {
        if (len < LEN_LONG) {
            return 1;
        }
        if (len < Record.maxPadding) {
            return SKIP_EOC_BYTES;
        }
        if (len < Record.OVERFLOW_OF_INT16) {
            return 3;
        }
        if (len < Record.OVERFLOW_OF_INT24) {
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

    byte[] convert(byte[] indefData) throws IOException {
        this.data = indefData;
        this.dataPos = 0;
        this.index = 0;
        this.dataSize = this.data.length;
        int unused = 0;
        while (this.dataPos < this.dataSize) {
            parseTag();
            parseValue(parseLength());
            if (this.unresolved == 0) {
                unused = this.dataSize - this.dataPos;
                this.dataSize = this.dataPos;
                break;
            }
        }
        if (this.unresolved != 0) {
            throw new IOException("not all indef len BER resolved");
        }
        this.newData = new byte[((this.dataSize + this.numOfTotalLenBytes) + unused)];
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
}
